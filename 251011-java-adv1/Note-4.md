- sleep이 아니었던 때 interrupt()를 해서 flag=true 유지됨
- sleep을 나중에 만나자마자 인터럽트 발생, flag=false가 됐다.
- 251011-java-adv1/src/interrupt/MyPrinter.java
- 251011-java-adv1/src/interrupt/MyPrinter2.java
- 251011-java-adv1/src/yield/Main.java 세 가지 케이스
- 1. Empty (아무것도 없음)
```
Thread-0 - 0
Thread-0 - 1 //Thread-0이 연속으로 실행
Thread-4 - 0
Thread-4 - 1 //Thread-4가 연속으로 실행
...
```
- 스레드가 자발적으로 CPU를 양보하지 않음
- OS 스케줄러가 타임 슬라이스(time slice)가 끝날 때만 context switch 발생
- 한 스레드가 타임 슬라이스 동안 계속 실행되므로 연속된 출력이 많음
- 2. Thread.sleep(1)
```
Thread-4 - 0
Thread-0 - 0
Thread-3 - 0 //여러 스레드가 골고루 섞여서 실행
Thread-2 - 0
Thread-1 - 0
...
```
- 스레드가 1ms 동안 TIMED_WAITING 상태로 전환
- 스케줄링 큐에서 완전히 제거되어 다른 스레드에게 기회를 줌
- sleep이 끝나면 다시 RUNNABLE 상태가 되어 큐에 들어감
- 가장 공평하게 실행 기회가 분배됨
- 3. Thread.yield()
```
Thread-0 - 0
Thread-0 - 1
Thread-0 - 2 //Thread-0이 모든 작업을 한 번에 완료
Thread-1 - 0
Thread-3 - 0
...
```
- 스레드가 "나 CPU 양보할게, 하지만 다시 바로 실행될 준비는 되어있어"라고 힌트를 줌
- RUNNABLE 상태를 유지하며 스케줄링 큐에 남아있음
- OS 스케줄러가 yield를 무시할 수도 있음 (단순히 힌트일 뿐)
- 같은 우선순위의 다른 스레드가 없거나 스케줄러 정책에 따라 다시 즉시 선택될 수 있음
```
CPU 양보의 강도:
sleep(1) > yield() > empty
스케줄링 큐 재진입:
- sleep: 큐에서 제거 → 1ms 후 다시 큐에 진입
- yield: 큐에 계속 있지만 "양보 힌트"만 줌
- empty: 아무것도 안 함, 타임 슬라이스 다 쓸 때까지 실행
결과적인 CPU 점유 시간:
empty ≥ yield() > sleep(1)
```
- yield()의 실제 동작
```java
//yield()는 이렇게 동작:
Thread.yield(); 
//JVM: "스케줄러야, 나 양보할게. 하지만 내가 제일 좋은 선택이면 다시 나 돌려도 돼"
//OS 스케줄러: "알았어, 근데 너 말고 실행할 게 별로 없네? 너 계속 해"
```
- yield()가 효과가 적은 이유:
- 단순히 "양보하고 싶다"는 힌트일 뿐 강제성 없음
- 같은 우선순위의 대기 중인 스레드가 없으면 다시 선택됨
- 현대 OS 스케줄러들은 yield를 거의 무시하거나 최소한으로만 반영
- 출력 결과에서 yield()를 써도 Thread-0이 0→1→2를 연속으로 실행한 건
- 스케줄러가 yield 힌트를 받았지만 "Thread-0이 여전히 최선의 선택"이라고 판단했기 때문
- 스케줄링 큐의 동작:
- 이상적인 개념 (단순 큐 모델):
```
실행 전 큐: [Thread-0, Thread-1, Thread-2, Thread-3, Thread-4]
                ↓ Thread-0 선택되어 실행
Thread-0 실행 중...
Thread-0.yield() 호출!
실행 후 큐: [Thread-1, Thread-2, Thread-3, Thread-4, Thread-0]
             ↑ Thread-0이 맨 뒤로 이동
```
- 실제 OS 스케줄러 동작
- OS는 단순한 FIFO 큐가 아니라 더 복잡한 구조를 사용:
```
- Linux CFS (Completely Fair Scheduler) 예시:
- Red-Black Tree 구조 사용
- 각 스레드의 "실행 시간"을 추적
- yield() 호출 시:
- 1. 현재 스레드의 가상 실행 시간(vruntime)을 약간 증가
- 2. Tree에서 다음으로 실행 시간이 적은 스레드 선택
- 3. 만약 yield한 스레드가 여전히 vruntime이 가장 적으면? → 다시 선택됨
```
- 왜 yield()가 효과가 없을 수 있나?
```
//케이스 1: yield가 효과 있는 경우
Thread-0: vruntime = 100ms
Thread-1: vruntime = 95ms
Thread-2: vruntime = 98ms
Thread-0.yield() 
→ Thread-0의 vruntime을 105ms로 증가
→ 다음 선택: Thread-1 (가장 적은 95ms)
```
```
//케이스 2: yield가 효과 없는 경우
Thread-0: vruntime = 100ms
Thread-1: vruntime = 200ms (이미 많이 실행됨)
Thread-2: vruntime = 300ms
Thread-0.yield() 
→ Thread-0의 vruntime을 105ms로 증가
→ 다음 선택: Thread-0 (여전히 가장 적은 105ms)
```
- 실제 동작 비교 정리
```
- empty (아무것도 안 함): 타임슬라이스 다 쓰고 자연스럽게 뒤로
큐: [T0, T1, T2, T3, T4]
T0 실행 → 타임슬라이스 끝날 때까지 계속 실행
→ T0의 실행시간 크게 증가
→ 다음: [T1, T2, T3, T4, T0] (자연스럽게 뒤로)
- yield(): 뒤로 가고 싶어"라고 요청만 함 (힌트)
큐: [T0, T1, T2, T3, T4]
T0 실행 → yield() → "양보할게"
→ T0의 실행시간 약간 증가 (페널티)
→ 다음: [T1, T2, T3, T4, T0] (보통 뒤로 가지만...)
→ 만약 다른 스레드들도 실행시간 많으면 T0이 다시 선택
- sleep(1): 큐에서 제거되었다가 나중에 뒤에 재진입
큐: [T0, T1, T2, T3, T4]
T0 실행 → sleep(1) → 큐에서 제거
대기 큐: [T0-1ms후 깨움]
실행 큐: [T1, T2, T3, T4]
→ 1ms 후 T0이 실행 큐 맨 뒤에 재진입
```
- yield하는 스레드가 RUNNABLE을 유지한다고 했는데,
- OS 스케줄링에서 yield하는 스레드의 상태 변화는
- Running -> Ready다. 거기서 다시 실행되면 Running이 된다.
- yield()의 동작 = Running → Ready로의 전환만 발생
- yield()는 CPU만 양보하고 바로 실행 대기열로 돌아간다.
- 다른 동일하거나 더 높은 우선순위의 스레드에게 실행 기회를 주려는 의도다.
- 다른 상태(Blocked, Waiting 등)로는 가지 않는다.
```java
//yield() - 항상 Ready 상태
Thread.yield(); //Running → Ready (무조건)
//sleep() - Timed Waiting 상태
Thread.sleep(1000); //Running → Timed Waiting → Ready
//wait() - Waiting 상태
object.wait(); //Running → Waiting → Ready (notify 후)
//join() - Waiting 상태
thread.join(); //Running → Waiting → Ready (대상 스레드 종료 후)
```
- 최근에는 10코어 이상의 CPU가 많다.
- 스레드 10개 정도만 만들어서 실행하면, 양보헤도 계속 수행될 수 있다.
- CPU 코어 수 이상의 스레드를 만들어야 양보하는 상황을 확인할 수 있다.
```java
while (!Thread.interrupted()) {
    if (jobQueue.isEmpty()) {
        continue;
    }
    //...
}
//인터럽트가 발생하기 전까지 계속
//인터럽트의 상태를 체크, jobQueue의 상태를 확인
//CPU에서 1초에 while문을 수억 번 반복할 수 있다.
//작동하는 스레드가 아주 많은데
//이런 체크 로직에 CPU 자원을 많이 사용하면
//필요한 스레드들의 효율이 낮을 수 있다.
while (!Thread.interrupted()) {
    if (jobQueue.isEmpty()) {
        Thread.yield();
        continue;
    }
    //...
}
//jobQueue에 작업이 없으면 yield()로
//다른 스레드에 작업을 양보하는 게 효율적이다.
```
- 251011-java-adv1/src/yield/SimpleWorker.java
- ConcurrentLinkedQueue는 thread-safe하지만
- Race Condition의 영향 받는다.
- (여러 스레드가 동시에 공유 자원에 접근할 때, 실행 순서에 따라 결과가 달라지는 상황)
```java
if (jobQueue.isEmpty()) { //A 시점: 체크
    //...
} else {
    String job = jobQueue.poll(); //B 시점: 실행 (A와 시간차 있음)
}
/*
워커c가 A 시점에 isEmpty() 체크 → 큐에 데이터 있음 확인
그 사이 워커a, b가 먼저 poll()로 데이터 가져감
워커c가 B 시점에 poll() 실행 → 이미 비어있어서 null 반환
즉, A와 B 사이의 타이밍 차이 때문에 예상과 다른 결과 발생
 */
```
- 해결 방법: isEmpty() 체크 없이 바로 poll()을 사용
```java
String job = jobQueue.poll(); //한 번에 실행
if (job == null) {
    //처리
}
/*
여전히 race condition은 있다.
poll() 자체는 원자적 연산이므로 항상 정확한 결과를 반환
null을 받을 수는 있지만, 예상 가능한 정상 동작
race condition으로 인한 버그를 예방
 */
```
- `Thread.interrupted()`
- 현재 스레드의 인터럽트 상태를 확인하면서
- 확인 후 그 상태를 자동으로 false로 초기화해주는 메서드
# 6. 메모리 가시성
```java
public class Main {
    public static void main(String[] args) {
        MyRunnable r = new MyRunnable();
        Thread t = new Thread(r);
        log("runFlag=" + r.runFlag);
        t.start();
        sleep(1000);
        r.runFlag = false;
        log("runFlag=" + r.runFlag);
    }
    static class MyRunnable implements Runnable {
        boolean runFlag = true;
        @Override
        public void run() {
            log("작업 시작");
            while(runFlag) {
            }
            log("작업 종료");
        }
    }
}
/*
14:58:53.766 [     main] runFlag=true
14:58:53.767 [ Thread-0] 작업 시작
14:58:54.781 [     main] runFlag=false
(main은 종료됨)
(while문이 종료가 안 됨)
 */
```
- 현재 코드에서 while(runFlag) 루프가 무한히 도는 이유:
- 메인 스레드가 runFlag = false로 변경
- 하지만 작업 스레드는 이 변경을 볼 수 없음
- 작업 스레드는 캐시된 runFlag = true 값을 계속 사용
```java
//while문에 출력문을 추가하면 종료될 가능성이 높다.
while(runFlag) {
    System.out.println("실행 중");
}
```
- 출력문 추가 시 종료 이유:
- (1) 동기화 메커니즘
- System.out.println()은 내부적으로 synchronized 블록을 사용
- synchronized는 메모리 배리어(memory barrier)를 발생시킴
- 이로 인해 캐시가 갱신되고 메인 메모리에서 최신 값을 읽어올 수 있음
- (2) JIT 컴파일러 최적화 방지
- 빈 while문은 JIT 컴파일러가 이 변수는 루프 안에서 변경되지 않는다고 최적화할 수 있음
- 출력문이 있으면 최적화가 달라져서 매번 변수를 다시 확인할 가능성이 높아짐
- 확실한 방법: volatile 키워드 추가
- 항상 메인 메모리에서 최신 값을 읽음
- 캐시를 사용하지 않음
- 모든 스레드가 동일한 값을 봄을 보장
- 왜 각 스레드(코어)마다 캐시가 다를까:
```
[CPU Core 1]          [CPU Core 2]
    ↓                     ↓
 L1 캐시               L1 캐시        (가장 빠름, 각 코어 전용)
    ↓                     ↓
 L2 캐시               L2 캐시        (빠름, 각 코어 전용)
    ↓                     ↓
        L3 캐시                       (보통, 여러 코어 공유)
            ↓
      메인 메모리(RAM)                (느림, 모든 코어 공유)
/* 성능: 빠른 접근을 위해 */
//메인 메모리 접근: ~100ns (느림)
//L1 캐시 접근: ~1ns (100배 빠름)
/* 하드웨어 구조: 각 코어가 독립적인 캐시 보유 */
//각 CPU 코어는 자신만의 L1/L2 캐시를 가짐
//물리적으로 코어 바로 옆에 위치해서 접근이 빠름
/* 효율성: 동시 작업 시 경합 방지 */
//여러 코어가 동시에 작업할 때 각자의 캐시가 필요
//캐시를 공유하면 경합(contention)이 발생해서 느려짐
/*
시작 상태: 메인 메모리의 runFlag = true
1. Thread-0 (Core 1):
- runFlag를 읽음 → L1 캐시에 true 저장
- while(runFlag) 반복할 때마다 L1 캐시의 값 사용
2. Main Thread (Core 2):
- runFlag = false로 변경
- Core 2의 캐시와 메인 메모리는 업데이트됨
3. 문제:
- Core 1의 L1 캐시는 여전히 true
- Thread-0은 계속 true를 보고 무한루프
 */
/* 캐시 일관성 프로토콜 */
//CPU는 MESI 프로토콜 같은 캐시 일관성 메커니즘 있다.
//성능을 위해 즉시 동기화하지는 않고
//특정 조건(메모리 배리어, volatile 접근 등)에서만 동기화
/* 이러한 이유들로, 멀티스레드 프로그래밍 시 */
//volatile, synchronized 같은 동기화 메커니즘이 필요하다.
/* volatile의 역할 */
//컴파일러와 CPU에게 명령:
//이 변수는 캐시하지 마
//항상 메인 메모리에서 읽고 써
//메모리 배리어를 설정해서 다른 코어도 즉시 볼 수 있게 해
```
- 컨텍스트 스위칭 시:
- 레지스터 값, 프로그램 카운터 등은 저장/복원됨
- 캐시 메모리는 자동으로 업데이트되지 않음
```java
//Thread-0이 Core 1에서 실행 중
while(runFlag) { //Core 1의 L1 캐시에 runFlag=true
}
//Thread-0이 Core 2로 컨텍스트 스위칭되면
while(runFlag) { //Core 2의 L1 캐시에 runFlag 값이 없음
    //메인 메모리에서 새로 읽어옴 (false를 발견)
}
```
- 컨텍스트 스위칭으로 다른 코어로 이동하면:
- 새로운 코어의 캐시에는 그 변수가 없을 가능성이 높음
- 따라서 메인 메모리에서 다시 읽어와야 함
- 결과적으로 최신 값을 볼 수 있음
- 하지만 이것도 보장되지 않음
```java
/* 문제 1: 같은 코어에 계속 있을 수 있음 */
//Thread-0이 계속 Core 1에서만 실행되면
//컨텍스트 스위칭이 여러 번 일어나도
//Core 1의 캐시는 그대로 runFlag=true
/* 문제 2: 캐시 일관성 문제 */
//설령 다른 코어로 이동해도
//해당 코어의 L2/L3 캐시에 오래된 값이 있을 수 있음
```
- 출력문이 도움이 되는 이유:
- 컨텍스트 스위칭이 발생해서 (부수 효과)
- println의 synchronized가 메모리 배리어를 만들어서 (직접 효과)
- I/O 작업으로 CPU가 다른 일을 하면서 캐시 갱신 기회가 생겨서
- 메모리 배리어가 진짜 해결책:
```java
volatile boolean runFlag = true; //메모리 배리어 보장
//읽을 때마다 메모리 배리어 발생
//캐시 무효화 및 메인 메모리 동기화 강제
//컨텍스트 스위칭과 무관하게 동작 보장
```
- 메모리 배리어는 CPU와 컴파일러에게 주는 명령:
- 여기서 멈춰 (메모리 작업을 재배치하지 마)
- 캐시와 메인 메모리를 동기화해
- CPU와 컴파일러는 성능을 위해 명령어 순서를 바꾼다:
```java
//작성한 코드
int a = 1;
int b = 2;
flag = true;
//CPU가 실제 실행하는 순서 (최적화)
flag = true; //이게 먼저 실행될 수 있음
int a = 1;
int b = 2;
```
```java
int a = 1;
int b = 2;
//여기에 메모리 배리어
flag = true; //이제 반드시 a, b 다음에 실행됨
```
- 메모리 배리어의 종류
- (1) Store Barrier (쓰기 배리어)
- 배리어 이전의 모든 쓰기 -> 캐시에서 메인 메모리로 flush
- 다른 코어들이 볼 수 있게 함
- (2) Load Barrier (읽기 배리어)
- 배리어 이후의 모든 읽기 -> 캐시 무효화하고 메인 메모리에서 읽기
- 최신 값을 가져옴
- (3) Full Barrier (전체 배리어)
- Store + Load 배리어 모두
- volatile의 메모리 배리어:
- 쓰기 시: Store Barrier (배리어 이전 모든 쓰기를 메모리로)
- 읽기 시: Load Barrier (배리어 이후 모든 읽기를 메모리에서)
- 가볍고 빠름
```java
volatile boolean flag;
int a = 0;
//Thread 1
a = 1;              //1. 일반 쓰기
flag = true;        //2. volatile 쓰기
                    //↑ Store Barrier 발생
                    //이전의 모든 쓰기(a=1)가 메인 메모리로
//Thread 2
if (flag) {         //3. volatile 읽기
                    //↑ Load Barrier 발생
                    //이후의 모든 읽기는 메인 메모리에서
    int b = a;      //4. a의 최신 값(1)을 읽음 보장
}
```
- synchronized의 메모리 배리어:
- 락 획득 시: Full Barrier (모든 읽기/쓰기 동기화)
- 락 해제 시: Full Barrier (모든 읽기/쓰기 동기화)
- 무겁고 느림 (락 획득/해제 오버헤드)
```java
int a = 0;
Object lock = new Object();
//Thread 1
a = 1;                      //1. 일반 쓰기
synchronized(lock) {        //2. 락 획득
                            //↑ Full Barrier 발생
    //a = 1이 메인 메모리에 보장됨
}                           //3. 락 해제
                            //↑ Full Barrier 발생
//Thread 2
synchronized(lock) {        //4. 락 획득
                            //↑ Full Barrier 발생
    int b = a;              //5. a의 최신 값(1)을 읽음
}
```
```java
//1. volatile - 가벼운 동기화
volatile int counter = 0;
counter++;  //문제: 읽기+쓰기가 원자적이지 않음
            //Thread-safe 하지 않음
//2. synchronized - 무거운 동기화
int counter = 0;
synchronized(lock) {
    counter++;  //락으로 보호됨
                //Thread-safe 함
}
//System.out.println()의 경우
public void println(String x) {
    synchronized (this) {   //← synchronized 블록
        print(x);
        newLine();
    }
}
//println이 메모리 배리어를 만드는 이유:
//내부적으로 synchronized 사용
//Full Barrier 발생
//다른 변수들도 동기화될 수 있음
```
```java
//volatile - 단순 flag에 적합
volatile boolean runFlag = true;
//synchronized - 복합 연산에 적합
synchronized(lock) {
    if (balance >= amount) {
        balance -= amount;
    }
}
//Atomic 클래스 - 카운터에 적합
AtomicInteger counter = new AtomicInteger();
counter.incrementAndGet(); //원자적 증가
```
- 251011-java-adv1/src/volatile1/Count.java
- flag에 volatile을 안 붙이면 성능이 증가한다.
- count가 더 많이 세어진다.
- 스레드 하나여서 count가 중복으로 세어지지는 않는다.
```java
//volatile flag의 부수 효과
volatile boolean flag = true;
long count; //volatile 아님
//Thread-0
count = 999;        //1. 일반 쓰기
flag = true;        //2. volatile 쓰기
                    //  ↑ Store Barrier
                    //  count=999도 메인 메모리로
//Main Thread
boolean f = c.flag; //3. volatile 읽기
                    //  ↑ Load Barrier
int x = c.count;    //4. count의 최신 값을 읽을 가능성 높음
//happens-before 관계:
//volatile 쓰기 이전의 모든 쓰기 → volatile 읽기 이후의 모든 읽기
//따라서 flag를 읽었다면, 그 이전의 count 값도 볼 수 있음
```
- 하지만 완벽한 보장은 아님 (타이밍 이슈)
```java
//Thread-0
while(flag) {
    count++;                    //1. count 증가
    //← 여기서 Main이 flag를 false로 변경
    if (count % 100_000_000 == 0) {
        log("count=" + count);  //2. 출력
    }
}
log("count=" + count);          //3. 최종 출력
//Main Thread
sleep(1000);
c.flag = false;                 //flag를 false로
log("count=" + c.count);        //4. count 읽기
/*
시간 1: Thread-0이 count = 1000 증가
시간 2: Main이 count 읽음 (1000)
시간 3: Thread-0이 count = 1001, 1002... 증가
시간 4: Thread-0 종료, 최종 count = 1500
Main이 읽은 값(1000) ≠ 최종 값(1500)
 */
```
- count에 문제가 생기는 경우:
- flag에 volatile이 없고, print 없으면 무한 루프
- count가 long 64비트면 JVM 32비트라서 원자적x
```java
static class Counter implements Runnable {
    volatile boolean flag = true;
    long count; //64비트, 원자적이지 않을 수 있음 (32비트 JVM)
    @Override
    public void run() {
        while(flag) {
            count++;
        }
    }
}
//Main에서 읽을 때
long value = c.count;  //찢어진 읽기(torn read) 가능
                       //상위 32비트와 하위 32비트가 다른 시점의 값
```
- count까지 volatile이어야 완전 보장
```java
static class Counter implements Runnable {
    volatile boolean flag = true;
    volatile long count; //← volatile 추가
    @Override
    public void run() {
        while(flag) {
            count++;
        }
        //종료 시점의 count가 메인 메모리에 보장됨
    }
}
```
- 또는 AtomicLong을 사용
```java
static class Counter implements Runnable {
    volatile boolean flag = true;
    AtomicLong count = new AtomicLong(); //더 안전
    @Override
    public void run() {
        while(flag) {
            count.incrementAndGet();
        }
    }
}
```
- JMM = 자바 메모리 모델
- 스레드 간의 메모리 접근과 가시성(visibility), 명령어 재배치(reordering)에 대한 규칙을 정의한 모델
- CPU 캐시, 컴파일러 최적화, CPU의 명령어 재배치 때문에
- Out of order execution: 컴파일러, JVM, CPU에 적용
- Field Visibility: Concurrency 상황에 적용
```java
public class FieldVisibility {
    int x = 0;
    public void write() {
        x = 1;
    }
    public void read() {
        int r2 = x;
    }
}
//코어 2개 이상 컴퓨터에
//두 스레드를 만들어서
//동일한 인스턴스에 대해
//한 스레드는 write 메서드를
//다른 스레드는 read 메서드를
//동시에 호출(병렬 실행)한다고 하자.
//처음에 x=0은
//두 코어의 shared cache에 저장된다.
//writer 메서드를 실행하는 코어-1는
//x=1을 local cache-1에 쓴다.
//read 메서드를 실행하는 코어-2는
//x=0, r2=x를 local cache-2에 쓴다.
//FieldVisibility라고 한다.
volatile int x = 0;
//이 경우에는
//write 메서드 실행 시
//x=1이 플러시되거나 공유 캐시에 푸시된다.
//read 메서드는 x=1, r2=x를 하게 된다.
//VolatileVisibility라고 한다.
```
- 한 스레드가 쓴 값을 다른 스레드가 바로 못 보거나,
- 프로그래머가 기대한 순서와 다르게 코드가 실행될 수 있다.
- JMM은 가시성과 순서 보장을 명확히 정의해 이를 방지한다.
- 가시성=어떤 경우에 스레드 간에 변경 사항이 보이는지
- 순서 보장=어떤 순서로 명령이 실행된 것으로 보이는지
> JMM is a specification which guarantees visibility of fields (aka happens before) amidst reordering of instructions.
- happens-before relationship
- 어떤 작업이 다른 작업보다 먼저 실행되고, 그 결과가 보장된다는 의미
```java
x = 1; //쓰기
int y = x; //읽기
/*
이 두 줄이 같은 스레드에서 순차적으로 실행되면
x = 1이 y = x보다 먼저 실행되므로
happens-before 관계가 성립
하지만 다른 스레드에서 실행된다면
이 관계가 없으면 y가 0을 읽을 수도 있다.
 */
```
```java
//자바에서 volatile 키워드는 가시성을 보장
//한 스레드가 volatile 변수에 값을 쓰면, 다른 스레드가 그 값을 즉시 볼 수 있다.
volatile int x = 0;
//write() 메서드에서 x = 1을 하면
//다른 스레드가 read()에서 x를 읽을 때 반드시 1을 본다.
/* volatile write의 중요한 점 */
//그 이전의 모든 일반 변수 쓰기와 happens-before 관계를 형성
//write()에서 a = 1; b = 1; c = 1; 하고 x = 1;을 하면
//x = 1이 메모리 배리어(memory barrier) 역할을 해서
//a, b, c의 값도 다른 스레드에서 볼 수 있게 된다.
//Field Visibility:
//일반 필드(int a, int b, int c)는 스레드 간에 값이 즉시 공유되지 않음. 캐시에 남아 있을 수 있다.
//Volatile Visibility:
//volatile 필드는 모든 스레드가 항상 최신 값을 보장 - 읽기/쓰기는 메인 메모리에서 직접 이루어진다.
//x = 1은 volatile write이므로
//그 이전의 a, b, c에 대한 쓰기와 happens-before 관계를 형성
//따라서 read()에서 x를 읽어 1이라면, a, b, c도 반드시 1이다.
```
```java
public class VolatileFieldsVisibility {
    int a = 0;, b = 0; c = 0;
    volatile int x = 0;
    public void write() {
        a = 1; b = 1; c = 1;
        x = 1; //volatile write
    }
    public void read() {
        int r2 = x; //volatile read
        int d1 = a; int d2 = b; int d3= c;
    }
}
```
```java
//자바에서 a = 1; b = 1; c = 1; 같은 코드가 있으면
//컴파일러나 CPU가 내부적으로 순서를 바꿀 수 있다.
//왜냐하면 이들 사이에 데이터 의존성이 없기 때문이다.
c = 1;
a = 1;
b = 1;
//이건 동일한 스레드 내에서는 아무 문제 없다.
//하지만 다른 스레드가 동시에 읽는다면 문제가 생길 수 있다.
//a, b, c는 일반 변수라서 캐시에 남아 있을 수 있다.
//즉, 값을 바꿔도 다른 스레드가 그 변경을 즉시 볼 수 없다.
//하지만 x는 volatile이니까, x = 1을 하면:
//a, b, c의 쓰기 작업이 x = 1보다 앞서 실행되도록 보장됨 (happens-before)
//x = 1이 메모리 배리어 역할을 해서
//그 이전의 모든 쓰기 작업이 공유 메모리에 flush됨
//다른 스레드가 x == 1을 읽으면, 그 시점에서 a, b, c도 최신 값(1)을 보게 됨
```
- a, b, c 간에는 reordering 가능하다.
- 컴파일러나 CPU는 a = 1; b = 1; c = 1; 순서를 바꿀 수 있다.
- 왜냐하면 이들 사이에는 데이터 의존성이 없기 때문
- 단, x = 1;이 volatile이기 때문에 그 아래로는 못 내려간다.
- JMM이 그 이전의 모든 쓰기가 x = 1; 보다 먼저 실행되도록 보장한다.
- x = 1은 쓰기(write)이기 때문에, 그 이전의 변수들(a, b, c)을 공유 메모리로 flush한다.
- 다른 스레드가 x == 1을 읽으면, 그 시점에서 a, b, c도 공유 메모리에서 fetch하게 된다.
- JMM에서 공유 메모리는 L3 캐시가 아니라 메인 메모리다.
- flush: CPU 캐시 → 메인 메모리(공유 메모리)
- fetch: 메인 메모리(공유 메모리) → CPU 캐시
- JMM happens-before 적용:
- volatile, Synchronized,
- Locks, Concurrent collections,
- Thread operations(join, start), final fields(special behavior)
- happens-before 관계 발생 예:
- (1) 프로그램 순서 규칙
- 단일 스레드에서, 프로그램 순서대로 작성된
- 모든 명령문은 happens-before 순서로 실행된다.
- int a = 1; int b = 1;에서
- 항상 a=1;이 b=1;보다 먼저 실행된다.
- (2) volatile 변수 규칙
- 한 스레드에서 volatile 변수에 대한 쓰기 작업은
- 해당 변수를 읽는 모든 스레드에 보이도록 한다.
- 즉, volatile 변수에 대한 쓰기 작업은
- 항상 그 변수를 읽는 작업보다 먼저 실행된다.
- (3) 스레드 시작 규칙
- 스레드A에서 스레드B.start()를 호출할 때
- 호출 이전의 스레드A의 모든 작업은
- 스레드B의 작업보다 항상 먼저 실행된다.
- (4) 스레드 종료 규칙
- 스레드A에서 스레드B.join()을 호출할 때
- 스레드B의 모든 작업은
- 호출 이후의 스레드A의 모든 작업보다 항상 먼저 실행된다.
- (5) 인터럽트 규칙
- 스레드A에서 스레드B.interrupt()를 호출할 때
- 스레드B가 인터럽트를 감지하는 작업이
- 스레드A가 인터럽트를 확인하는 작업보다 항상 먼저 실행된다.
- (6) 객체 생성 규칙
- 객체가 스레드A에서 생성자에 의해 생성되고 초기화되는 작업이
- 객체가 스레드B에서 참조되는 작업보다 항상 먼저 실행된다.
- (7) 모니터 락 규칙
- 스레드A에서 synchronized 블록을 종료하는 작업이
- 스레드B가 모니터 락을 얻는 작업보다 항상 먼저 실행된다.
- 즉, 스레드B는 스레드A가 동기화된 블록에서 실행한
- 모든 작업을 볼 수 있다.
- ReentrantLock과 같은 락을 사용해도 이 규칙이 성립된다.
- (8) 전이 규칙
- A->B, B->C: A->C
```
규칙 - 설명
Program Order Rule - 한 스레드 내에서는 코드가 작성된 순서대로 실행된 것처럼 보인다. (컴파일러 최적화는 가능하지만 결과는 동일해야 함)
Monitor Lock Rule - 한 스레드가 락을 unlock하면, 이후에 그 락을 lock하는 스레드는 그 전에 일어난 모든 변경을 본다.
Volatile Variable Rule - 한 스레드가 volatile 변수에 write하면, 이후 다른 스레드가 그 변수를 read할 때 그 값을 반드시 본다.
Thread Start Rule - Thread.start() 이전의 모든 동작은 새 스레드 안에서 보인다.
Thread Join Rule - 스레드가 join()에서 반환되면, 그 스레드 안에서 일어난 모든 동작이 join() 이후에 보인다.
Transitivity Rule - A happens-before B이고, B happens-before C라면, A happens-before C이다.
정리
JMM - 자바에서 스레드 간 메모리 가시성과 재배치 규칙을 정의
happens-before - “A의 결과가 B에서 보인다”는 논리적 순서 관계
volatile / synchronized / start / join - happens-before를 형성하는 주요 메커니즘
Transitivity - A→B, B→C이면 A→C
JMM은 멀티스레드에서 메모리가 언제, 어떻게 보이는지를 정의하고
happens-before는 그 가시성의 순서를 결정한다.
```