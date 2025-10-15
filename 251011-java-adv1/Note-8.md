# 8. 고급 동기화 - concurrent.Lock
## LockSupport
- synchronized 무한 대기 문제 해결
- 각 스레드마다 1개의 퍼밋(통행권)을 관리
- 퍼밋은 스레드가 멈추지 않고 통과해도 된다는 표시
- 퍼밋이 0 - 통행권 없음, park() 하면 멈춤
- 퍼밋이 1 - 통행권 있음, park() 해도 바로 통과
```
Java에서 여러 스레드가 있을 때
어떤 스레드는 일을 하다가 잠깐 멈춰야 하고
다른 스레드는 그걸 깨워줘야 할 때가 있다.
하나의 스레드가 신호가 올 때까지 기다리기(park) 역할을,
다른 스레드가 신호를 보냄(unpark) 역할을 한다.
한 스레드가 park()으로 멈추고
다른 스레드가 unpark()으로 깨운다.
```
- Worker (일꾼 스레드)
```java
Thread worker = new Thread(() -> {
    System.out.println("worker: 일하다가 대기 시작");
    LockSupport.park(); //자기 자신을 멈춤
    System.out.println("worker: 깨어나서 다시 일함");
});
```
- Main (메인 스레드)
```java
worker.start(); //worker 실행
Thread.sleep(2000); //2초 기다림
System.out.println("main: 이제 worker 깨움");
LockSupport.unpark(worker); //worker에게 퍼밋(신호) 전달
//메인 스레드가 worker에게 unpark()을 호출한다.
//이제 깨어나도 된다는 신호
```
- 퍼밋의 이동
```
[worker]permit = 0
worker park() → permit이 없으므로 WAITING
main unpark(worker) → [worker]permit = 1
JVM이 worker를 깨움 → permit 소비 ([worker]permit = 0)
```
- unpark(): 다른 스레드가 퍼밋 1개를 건네주는 행위 (여러 번 건네도 최대 1개)
- park(): 자기 스레드가 그 퍼밋을 소비하는 행위
```java
Thread {
    ...
    LockSupport_permit: 0 or 1
}
/* Step 1. LockSupport.park() 호출 */
LockSupport.park();
//현재 스레드의 permit 값 확인
//퍼밋은 park() 한 번을 무시(즉시 통과)하는 데 사용
//permit == 1이면, 멈추지 않고 바로 리턴
//    그 퍼밋은 즉시 소비(0으로 리셋)
//    대기 후 바로 깨어나는 게 아니다. 대기를 하지 않음
//permit == 0이면, “퍼밋이 없네? 그럼 기다려야겠다.”
//    JVM이 스레드를 WAITING 상태로 전환
/* Step 2. main 스레드가 unpark(worker) 호출 */
LockSupport.unpark(worker);
//main 스레드가 worker에게 신호를 주는 행위
//JVM 내부적으로 worker 객체를 찾아서 permit = 1로 설정
//만약 worker가 지금 park()으로 대기 중이라면?
    //JVM이 worker를 즉시 깨워서 RUNNABLE 상태로 되돌림
    //깨워지는 순간, 다시 permit = 0으로 설정
```
- parkNanos() / parkUntil()
```
permit()과 동일한 permit 규칙
퍼밋이 있으면 즉시 리턴 (대기하지 않음, 퍼밋 1->0 소비)
퍼밋이 없으면 지정된 시간만큼 대기 후 자동 리턴 (퍼밋 그대로 0으로 유지)
대기 중에 누군가 unpark()를 호출하면 즉시 깨어남 (퍼밋 0->1->0 소비)
```
- 퍼밋은 기본 0이다.
- 퍼밋이 1이란 건 미리 unpark()가 호출됐다는 의미다.
## LockSupport와 interrupt
- Thread.sleep() 등
```
1. interrupt() 호출
   ↓
2. interrupt 플래그 = true 설정
   ↓
3. TIME_WAITING → RUNNABLE 상태 변경
   ↓
4. InterruptedException 생성 및 throw
   ↓
5. 예외를 던지기 직전에 interrupt 플래그 = false로 초기화
   ↓
6. catch 블록에서 예외 처리
//InterruptedException을 던지는 메서드는
//예외를 던질 때 플래그를 자동으로 clear한다.
//예외를 던지는 = 인터럽트를 소비한다 = 플래그 clear
```
- LockSupport.park() 등
```
1. interrupt() 호출
   ↓
2. interrupt 플래그 = true 설정
   ↓
3. WAITING → RUNNABLE 상태 변경
   ↓
4. park() 메서드가 그냥 리턴 (예외 없음)
   ↓
5. interrupt 플래그 = true 그대로 유지
//park()는 예외를 던지지 않으므로
//플래그를 clear하지 않는다.
//예외를 안 던지는 = 인터럽트를 보존한다 = 플래그 유지
```
- park()의 내부 동작
```java
//park()의 개념적인 내부 동작
public static void park() {
    //1. 먼저 interrupt 플래그 체크
    if (Thread.currentThread().isInterrupted()) {
        return; //즉시 리턴 (대기 안 함)
    }
    //2. permit 체크
    if (permit > 0) {
        permit = 0;
        return; //즉시 리턴
    }
    //3. 둘 다 아니면 실제로 대기
    //WAITING 상태로 진입
    실제_대기();
}
//예시: 연속으로 park() 호출
Thread.currentThread().interrupt(); //플래그 = true
LockSupport.park(); //즉시 리턴 (플래그가 true니까)
LockSupport.park(); //또 즉시 리턴 (플래그 여전히 true)
LockSupport.park(); //또 즉시 리턴...
//대기가 하나도 안 일어남
```
- 동기화 락
```
synchronized (모니터 락, BLOCKED)
    ↓
ReentrantLock (내부 구현 락, WAITING/TIMED_WAITING)
    ↓
LockSupport (낮은 수준의 park/unpark가 락, WAITING/TIMED_WAITING)
    ↓
OS 수준(예: mutex) (커널 락 등, OS 스케줄러에 따라 다름)
```
- 초 단위
```
parkNanos(나노초): 나노초 동안 TIMED_WAITING 상태로 변경
parkUntil(밀리초): 미래에 깨어날 에포크 시점(밀리초)을 지정
1초 = 1000밀리초(ms)
1밀리초 = 1,000,000나노초(ns)
1초 = 1,000,000,000나노초(ns)
```
## Lock 인터페이스
- 명시적인 락 획득과 해제를 통해 synchronized보다 더 많은 제어권을 제공
```java
void lock()
//락을 획득한다.
//다른 스레드가 이미 락을 보유 중이면, 락이 해제될 때까지 현재 스레드는 WAITING 상태가 된다.
//인터럽트에 응답하지 않는다. (인터럽트가 발생해도 대기를 계속한다)
void lockInterruptibly() throws InterruptedException
//락을 획득한다.
//다른 스레드가 이미 락을 보유 중이면, 락이 해제될 때까지 현재 스레드는 WAITING 상태가 된다.
//대기 중 인터럽트가 발생하면 InterruptedException을 던지고 락 획득을 포기한다.
boolean tryLock()
//락 획득을 즉시 시도한다.
//락을 사용할 수 있으면 즉시 획득하고 true를 반환한다.
//락을 사용할 수 없으면 대기하지 않고 즉시 false를 반환한다.
boolean tryLock(long time, TimeUnit unit) throws InterruptedException
//지정된 시간 동안 락 획득을 시도한다.
//지정된 시간 내에 락을 획득하면 true를 반환한다.
//시간이 초과되면 false를 반환한다.
//대기 중 인터럽트가 발생하면 InterruptedException을 던진다.
void unlock()
//락을 해제한다.
//락을 보유한 스레드만 호출해야 한다.
//안 그러면 IllegalMonitorStateException을 던진다.
//일반적으로 finally 블록에서 호출하여 락이 반드시 해제되도록 보장한다.
Condition newCondition()
//Condition 객체를 생성하여 반환한다.
//Object의 wait()/notify()/notifyAll()과 유사하지만 더 강력한 기능을 제공한다.
//하나의 Lock에 여러 개의 Condition을 생성할 수 있다.
```
- Lock 구현체
```
- ReentrantLock - 공정성o
- ReadWriteLock - 읽기/쓰기 락을 분리하여 다중 읽기 허용, 쓰기 단독 허용
- StampedLock - 낙관적 락(Optimistic Locking) 지원, 성능 향상에 유리
- ReentrantReadWriteLock: ReadWriteLock의 대표 구현체
```
- 구현체 ReentrantLock
```java
//ReentrantLock → AbstractQueuedSynchronizer → AbstractOwnableSynchronizer
public class ReentrantLock implements Lock {
    private final Sync sync; //AbstractQueuedSynchronizer(AQS) 기반
    abstract static class Sync extends AbstractQueuedSynchronizer {
        //state 변수로 락 상태 관리
        //state = 0 : 락이 해제된 상태
        //state > 0 : 락이 획득된 상태 (재진입 횟수)
        //exclusiveOwnerThread 변수로 락을 획득한 스레드 객체 관리
        //exclusiveOwnerThread = null: 락이 해제된 상태
    }
}
abstract class AbstractQueuedSynchronizer {
    private volatile int state; //락 상태
    //대기 큐 (FIFO)
    static final class Node {
        Thread thread;
        Node prev;
        Node next;
        volatile int waitStatus;
    }
    private transient volatile Node head;
    private transient volatile Node tail;
}
public abstract class AbstractOwnableSynchronizer implements java.io.Serializable {
    private transient Thread exclusiveOwnerThread; //락을 획득한 스레드 객체
    protected final void setExclusiveOwnerThread(Thread thread) {
        exclusiveOwnerThread = thread;
    }
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
}
```
```
상태 전이 과정
1. 초기 상태
state = 0 (락 해제 상태)
대기 큐 = 비어있음
2. 스레드 A가 lock() 호출
state = 0 → 1 (CAS 연산으로 변경 성공)
락 소유자 = Thread A
상태: RUNNABLE (락 획득 성공, 즉시 진행)
3. 스레드 B가 lock() 호출 (락 이미 사용 중)
state = 1 (이미 락이 사용 중)
→ AQS의 대기 큐에 진입
→ LockSupport.park() 호출
→ 상태: WAITING (OS 레벨에서 대기)
[대기 큐 구조]
head → [Thread B] → tail
4. 스레드 C도 lock() 호출
[대기 큐 구조]
head → [Thread B] → [Thread C] → tail
둘 다 WAITING 상태로 park됨
5. 스레드 A가 unlock() 호출
state = 1 → 0
→ 대기 큐의 head.next (Thread B)를 깨움
→ LockSupport.unpark(Thread B) 호출
→ Thread B: WAITING → RUNNABLE
→ Thread B가 CAS로 state를 0→1로 변경 시도
→ 성공하면 락 획득, 큐에서 제거
→ Thread A는 unlock() 이후 자신의 코드 계속 실행
```
```
동작 흐름
[lock() 호출]
1. CAS로 state를 0→1로 변경 시도
   성공 → 락 획득, 메서드 계속 실행
   실패 ↓
2. AQS 대기 큐에 Node 추가
3. LockSupport.park(this) 호출
   → OS에게 스레드 대기 요청
   → WAITING 상태로 전환
   → CPU 스케줄링에서 제외
[unlock() 호출]
1. state를 1→0으로 변경
2. 대기 큐의 첫 번째 노드 확인
3. LockSupport.unpark(다음 스레드) 호출
   → OS에게 스레드 깨우기 요청
   → WAITING → RUNNABLE
   → CPU 스케줄링 대상이 됨
4. unlock() 호출한 스레드는 그대로 진행
```
```java
ReentrantLock lock = new ReentrantLock();
//Thread A
lock.lock(); //state: 0→1, 소유자: A
try {
    //작업 수행
} finally {
    lock.unlock(); //state: 1→0, 대기 중인 스레드 깨움
}
//Thread B (A가 락 보유 중일 때 호출)
lock.lock(); //state=1 확인 → 큐 진입 → park() → WAITING
//A가 unlock()하면 → unpark() → RUNNABLE → CAS 시도
```
```
- Lock의 락은 AQS의 state 변수와 대기 큐로 구현
- 대기는 LockSupport.park()로 WAITING 상태
- 깨우기는 LockSupport.unpark()로 RUNNABLE 복귀
- 초기 상태는 state=0, 큐 비어 있음
- unlock 후 호출 스레드는 자신의 코드 계속 실행
- unlock 후 깨워진 스레드는 큐에서 나와서 락 획득 경쟁
재진입 가능 (Reentrant) - 동일한 스레드가 여러 번 락을 획득할 수 있음
공정성 설정 가능 - 생성자에서 true를 전달하면 FIFO 순서로 락을 부여
tryLock()과 timeout 지원 - 데드락 회피에 유리
Condition 객체 사용 가능 - await()/signal()로 세밀한 조건 동기화 가능
```
- synchronized와 다른 점
```
[synchronized]
모니터 락 → JVM이 관리
- 대기: 모니터의 EntryList
- 복귀: BLOCKED → RUNNABLE → 임계 영역 진입
[Lock (ReentrantLock)]
AQS 큐 → 라이브러리 레벨에서 관리
- 대기: AQS의 대기 큐 (연결 리스트)
- 복귀: WAITING → RUNNABLE → CAS 경쟁 → 락 획득
```

| 항목 | synchronized | Lock 인터페이스 |
|------|--------------|--------------|
| 락 해제 | 자동 (블록 종료 시) | 명시적 (unlock()) |
| 공정성 설정 | 불가 | 가능 (ReentrantLock) |
| 인터럽트 대응 | 불가 | 가능 (lockInterruptibly()) |
| 조건 변수 | wait()/notify() | Condition 객체 |
| 성능 | 단순, 안정적 | 고성능, 유연성 높음 |

## ReentrantLock
- 비공정 모드: 기본 모드, 공정성x
- `new ReentrantLock();`
- 공정 모드: 먼저 대기한 스레드가 먼저 획득, 성능 낮음
- `new ReentrantLock(true);`
- 251011-java-adv1/src/sync2/RLMain.java
- 비공정 락
- 락을 방금 놓은 스레드나, 대기열에 새로 들어온 스레드가 즉시 락을 다시 잡을 수 있음
- 앞쪽에서 섞임이 있다=대기열 무시(선점)이 일어났다.
- 공정 락
- 스레드가 대기열에 들어오면, 순서대로 실행됨
- 뒤에서 섞임이 있다=2그룹 스레드가 먼저 대기열에 들어갔다.
- `Thread.sleep(2);` //락을 유지
- 락은 스레드의 소유권 개념이다.
- ReentranLock 또는 synchronized의 락은 스레드가 소유하고 있는 동안 유지된다.
- 한 스레드가 lock()을 호출하면, 내부적으로 그 스레드가 락의 주인으로 등록된다.
- 락을 소유한 스레드가 unlock()을 호출하기 전까지 락은 해제되지 않는다.
- sleep()을 하면 owner는 TIMED_WAITING 상태지만 락은 유지된다.
- 251011-java-adv1/src/sync2/BankAccount.java
- `sleep(50); //약간 쉬었다가 재시도 (락 경합 유도)`
- 대기열에 추가하도록 한다는 의미다.
- Lock을 final로 선언하는 이유
- 한 번 생성된 Lock 객체가 변경되지 않게 보장하기 위해서
- Lock은 객체 단위 동기화를 위해 사용됨
- ReentrantLock은 특정 객체의 상태(balance)를 보호하기 위해 존재
- BankAccount 객체마다 하나의 Lock 인스턴스가 있고
- 그 Lock을 통해서 balance에 대한 접근을 직렬화한다.
```java
lock.lock(); //lock이 달라지면 동기화가 안 된다.
try {
    balance += amount;
} finally {
    lock.unlock();
}
```
- Lock 객체는 상태를 갖는 동기화 메커니즘
- ReentrantLock은 내부적으로 누가 락을 들고 있는지, 재진입 횟수 등을 저장한다.
- 즉, 상태를 가진 객체다.
- 그래서 final로 만들어 다른 인스턴스로 바뀌지 않도록 해야 한다.
- 그렇게 해야 Lock의 상태(누가 잡고 있는지 등)가 일관되게 유지된다.
- BankAccount는 멀티스레드 환경에서 공유될 가능성이 높다.
- final을 붙이면 컴파일러와 다른 개발자에게
- 이 락은 객체의 생명주기 동안 바뀌지 않는다는 의도를 전달할 수 있다.
```java
public boolean deposit(int amount) {
    if(!lock.tryLock()) {
        log("입금 불가");
        return false;
    }
    //락을 가진 상태 (다른 스레드는 balance에 접근할 수 없다.)
    try {
        balance += amount;
        log("입금 완료");
    } finally {
        //finally에서 unlock()을 호출해야 한다.
        lock.unlock();
        //unlock()에 앞서 예외가 발생해서
        //락이 해제되지 못해서 발생하는
        //교착(deadlock) 상황을 방지하기 위해서다.
    }
    //unlock() 다음 코드는 락의 보호를 받지 않는다.
    //임계 구역 (Critical Section), 비임계 구역 (Non-critical Section)이라고 한다.
    log("거래 종료");
    return true;
}
```
- try-finally가 안전한 락 해제 보장 패턴이다.
```java
if (lock.tryLock()) {
    try {
        //공유 자원 접근
    } finally {
        lock.unlock();
    }
}
```
- catch는 예외를 처리하고 복구할 때 추가한다.
```java
if (!lock.tryLock()) {
    log("입금 불가");
    return false;
}
try {
    balance += amount;
    log("입금 완료");
    return true;
} catch (Exception e) {
    log("입금 중 오류: " + e.getMessage());
    return false; //예외를 처리하고 복구
} finally {
    lock.unlock(); //락 해제
}
```
## 교착 상태
- 두 개 이상의 프로세스나 스레드가 서로가 점유한 자원을 기다리며 무한히 대기하는 상황
- 이 상태에서는 어떤 작업도 더 이상 진행되지 않으며, 시스템 일부 또는 전체가 멈춘 것처럼 보인다.
- 교착 상태가 발생하는 4가지 조건 (Coffman 조건)
- 상호 배제(Mutual Exclusion): 자원은 한 번에 하나의 프로세스만 사용할 수 있음
- 점유 및 대기(Hold and Wait): 자원을 점유한 상태에서 다른 자원을 요청하며 대기
- 비선점(No Preemption): 자원을 강제로 빼앗을 수 없음
- 순환 대기(Circular Wait): 프로세스들이 서로 자원을 요청하며 원형으로 대기
- 이 네 가지 조건이 모두 만족될 때 교착 상태가 발생할 수 있다.
- 동기화에서 교착 상태가 발생하는 경우
- 두 스레드가 서로 다른 락을 점유한 상태에서 상대방의 락을 요청할 때
- 예: 스레드 A는 락 X를 잡고 락 Y를 기다리고, 스레드 B는 락 Y를 잡고 락 X를 기다리는 경우
- 락을 잡은 후 예외가 발생하거나 unlock()을 호출하지 않아 락이 해제되지 않을 때 다른 스레드는 해당 락을 얻지 못하고 무한 대기
- 자원 요청 순서가 불규칙하거나 동적으로 바뀔 때 순환 대기 조건이 쉽게 성립되어 교착 상태 발생 가능성 증가