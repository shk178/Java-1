# 4. 스레드 제어와 생명 주기1
```java
    public static void main(String[] args) {
        Thread maint = Thread.currentThread();
        //자바 프로그램은 항상 main 스레드에서 시작
        log(maint);
        //15:56:35.809 [     main] Thread[#1,main,5,main]
        //#1: 스레드 ID (JVM 내에서 유일한 번호)
        //main: 스레드 이름
        //5: 스레드 우선순위 (기본값 5)
        //main: 스레드가 속한 그룹 이름
        log(maint.threadId());
        //15:56:35.811 [     main] 1
        //스레드 id 고유
        log(maint.getName());
        //15:56:35.811 [     main] main
        //스레드 이름 중복 가능
        log(maint.getPriority());
        //15:56:35.813 [     main] 5
        //1~10 우선순위 (순서 보장은 x)
        log(maint.getThreadGroup());
        //15:56:35.813 [     main] java.lang.ThreadGroup[name=main,maxpri=10]
        //기본적으로 모든 스레드는 main 그룹에 속함
        //maxpri=10은 이 그룹 내에서 가질 수 있는 최대 우선순위
        log(maint.getState());
        //15:56:35.813 [     main] RUNNABLE
        //스레드의 현재 상태를 반환
        //상태는 Thread.State enum으로 정의되어 있다.
    }
```
- 운영체제마다 다르지만 우선순위가 높으면 그래도 자주 실행된다.
- 스레드를 생성한 스레드를 부모 스레드라고 한다.
- 자식 스레드는 부모 스레드와 동일한 그룹에 속한다.
- 현재는 main 스레드가 실행 중이므로 RUNNABLE 상태다.
```
상태 - 의미
NEW - 스레드가 생성됐지만 아직 시작되지 않음
RUNNABLE - 실행 중이거나 실행 가능한 상태
BLOCKED - 모니터 락을 기다리는 중
WAITING - 다른 스레드의 신호를 기다리는 중
TIMED_WAITING - 일정 시간 대기 중
TERMINATED - 실행이 완료됨
```
- 스레드 생명주기
- 1. 생성: New - 스레드가 생성되었으나 아직 시작 안 됨
- 2. 실행 가능: Runnable - 스레드가 실행 중이거나 실행될 준비가 됨
- 실행 중 일시 중지:
- Blocked(차단) - 스레드가 동기화 락을 기다림
```declarative
스레드가 다른 스레드에 의해 동기화 락을 얻기 위해 기다림
예를 들어, synchronized 블록에 진입하기 위해 락을 얻어야 하는데
다른 스레드가 이미 락을 가지고 있는 경우
```
- Waiting(대기) - 스레드가 무기한으로 다른 스레드의 작업을 기다림
```declarative
스레드가 다른 스레드의 특정 작업이 완료되기를 무기한 기다림
wait(), join() 메서드가 호출될 때 이 상태가 된다.
스레드는 다른 스레드가 notify 또는 notifyAll 메서드를 호출하거나
join이 완료될 때까지 기다린다.
```
- Timed Waiting(시간 제한 대기) - 스레드가 일정 시간 동안 다른 스레드의 작업을 기다림
```declarative
sleep(long), wait(long), join(long) 메서드가 호출될 때 이 상태가 된다.
주어진 시간이 경과하거나 다른 스레드가 해당 스레드를 깨울 때까지 기다린다.
```
- 3. 종료: Terminated - 스레드의 실행이 완료됨
```java
public class ThreadMain3 {
    public static void main(String[] args) {
        Thread maint = Thread.currentThread();
        Thread t = new Thread(new MyRunnable(maint), "myThread");
        log(maint.getState());
        log(t.getState());
        t.start();
        log("run start()");
    }
    //main 스레드는 main() 메서드 안에서만 지역 변수로 선언되어 있다.
    static class MyRunnable implements Runnable {
        private final Thread maint;
        MyRunnable(Thread maint) {
            this.maint = maint;
        }
        @Override
        public void run() {
            log(maint.getState());
            log(Thread.currentThread().getState());
            log("run run()");
        }
    }
}
/*
16:45:34.811 [     main] RUNNABLE
16:45:34.813 [     main] NEW
16:45:34.813 [     main] run start()
16:45:34.813 [ myThread] RUNNABLE
16:45:34.813 [ myThread] RUNNABLE
16:45:34.813 [ myThread] run run()
 */
``` 
- 자바에서 스레드는 독립적으로 실행된다.
- 부모 스레드가 종료되더라도 자식 스레드는 자동으로 종료되지 않는다.
- 자식 스레드를 데몬 스레드로 설정해도 마찬가지다.
- 데몬 스레드는 모든 사용자 스레드가 종료되면 자동으로 종료된다.
- 어떤 스레드든 자신의 작업을 마치면 종료된다.
- Thread.sleep()
- InterruptedException을 발생시킬 수 있는 체크 예외(checked exception)
- main()에서는 예외를 던질 수 있다.
```java
public static void main(String[] args) throws InterruptedException {
    Thread.sleep(1000);
}
//JVM이 main()을 호출할 때 예외를 처리할 수 있다.
//예외를 던지는 방식으로 처리해도 문제가 없다.
```
- run()에서는 예외를 던질 수 없다.
```java
public void run();
//Runnable 인터페이스의 run() 메서드가
//예외를 던질 수 없도록 정의되어 있다.
@Override
public void run() {
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
//InterruptedException 같은 체크 예외는 try-catch로 처리한다.
```
- Runnable.run()은 간단한 실행 단위로 설계되어 있다.
- 예외를 외부로 던지지 않도록 제한되어 있다.
- 필요하다면 Callable 인터페이스로 예외를 던질 수 있다.
```java
//Runnable 대신 Callable
Callable<Void> task = () -> {
    Thread.sleep(1000);
    return null;
};
//ExecutorService와 함께 사용할 때 유용
```
- InterruptedException은 인터럽트가 걸릴 때 발생한다.
```java
//스레드가 sleep(), wait(), join() 같은 블로킹 상태일 때
//다른 스레드가 interrupt()를 호출하면 발생
Thread t = new Thread(() -> {
    try {
        Thread.sleep(5000); //여기서 블로킹 상태
    } catch (InterruptedException e) {
        System.out.println("인터럽트 발생");
    }
});
t.start();
//interrupt()는 다른 스레드에게 중단 요청을 보내는 메서드
t.interrupt(); //여기서 InterruptedException 발생
/* 블로킹 상태가 아닐 때 interrupt()를 호출하면 예외 발생하지 않는다. */
Thread t = new Thread(() -> {
    //interrupted()나 isInterrupted()로 인터럽트 상태 확인할 수 있다.
    while (!Thread.currentThread().isInterrupted()) {
        //작업하다가 인터럽트 되면 중단
    }
    System.out.println("인터럽트 감지 후 종료"); //실행됨
});
t.start();
t.interrupt(); //예외는 없지만, 인터럽트 상태는 true로 바뀜
/* 인터럽트를 감지하지 않으면 */
Thread t = new Thread(() -> {
    while (true) {
        //무한 루프, 인터럽트 감지 안 함
    }
    System.out.println("인터럽트 감지 후 종료"); //실행 안 됨
});
```
- 부모 메서드가 체크 예외를 던지지 않으면, 자식 메서드도 체크 예외를 던질 수 없다.
- 자식 메서드는 부모 메서드가 던질 수 있는 체크 예외의 하위 타입만 던질 수 있다.
```declarative
Throwable //상위 타입
    └── Exception
        └── IOException
            └── FileNotFoundException //하위 타입
//하위 타입 = 더 좁은 범위
//다형성을 안전하게 유지하기 위한 규칙
```
```java
class Parent {
    void read() throws IOException {}
}
class Child extends Parent {
    @Override
    void read() throws Exception {} //컴파일 오류
}
Parent obj = new Child();
obj.read(); //Parent가 IOException만 던진다고 했는데, Exception이 나오면 위험
```
```java
public class ThreadMain4 {
    public static void main(String[] args) throws Exception {
    }
    static class CheckedRunnable implements Runnable {
        @Override
        public void run() throws Exception {
        }
        //java: run() in control.ThreadMain4.CheckedRunnable cannot implement run() in java.lang.Runnable
        //  overridden method does not throw java.lang.Exception
    }
}
//main throws Exception 때문이 아니라
//앞에서처럼 Runnable의 run()이 예외를 던지지 않기 때문에 컴파일 오류 난다.
```
- 언체크(런타임) 예외는 던질 수 있다. (자동으로 던져진다.)
- Sleep 유틸리티 메서드는 InterruptedException 체크 예외를 발생시킨다.
- Runnable 대신 Callable을 많이 쓰게 됐다.
- Thread.sleep() - TIMED_WAITING 상태
- Thread.join() - WAITING 상태
```java
public class JoinMain2 {
    public static void main(String[] args) {
        log("start");
        Sum s1 = new Sum(1, 50);
        Sum s2 = new Sum(51, 100);
        Thread t1 = new Thread(s1, "thread-1");
        Thread t2 = new Thread(s2, "thread-2");
        t1.start();
        t2.start();
        log("s1.sum=" + s1.getSum());
        log("s2.sum=" + s2.getSum());
        log("end");
    }
    static class Sum implements Runnable {
        private int startVal;
        private int endVal;
        private int sum;
        Sum(int x, int y) {
            log("초기화");
            startVal = x; endVal = y; sum = 0;
        }
        public int getSum() {
            return sum;
        }
        @Override
        public void run() {
            log("작업 시작");
            sleep(2000);
            for (int i = startVal; i <= endVal; i++) {
                sum += i;
            }
            log("작업 완료: sum=" + sum);
        }
    }
}
/*
14:59:51.232 [     main] start
14:59:51.232 [     main] 초기화
14:59:51.232 [     main] 초기화
14:59:51.236 [ thread-1] 작업 시작
14:59:51.236 [ thread-2] 작업 시작
14:59:51.239 [     main] s1.sum=0
14:59:51.241 [     main] s2.sum=0
14:59:51.241 [     main] end
14:59:53.248 [ thread-1] 작업 완료: sum=1275
14:59:53.248 [ thread-2] 작업 완료: sum=3775
 */
```
```java
public class JoinMain3 {
    public static void main(String[] args) {
        log("start");
        Sum s1 = new Sum(1, 50);
        Sum s2 = new Sum(51, 100);
        Thread t1 = new Thread(s1, "thread-1");
        Thread t2 = new Thread(s2, "thread-2");
        t1.start();
        t2.start();
        log("s1.sum=" + s1.getSum());
        log("s2.sum=" + s2.getSum());
        log("end");
    }
    static class Sum implements Runnable {
        private int startVal;
        private int endVal;
        private int sum;
        Sum(int x, int y) {
            log("초기화");
            startVal = x; endVal = y; sum = 0;
        }
        public int getSum() {
            return sum;
        }
        @Override
        public void run() {
            log("작업 시작");
            for (int i = startVal; i <= endVal; i++) {
                sum += i;
            }
            log("작업 완료: sum=" + sum);
        }
    }
}
/*
15:01:52.542 [     main] start
15:01:52.545 [     main] 초기화
15:01:52.545 [     main] 초기화
15:01:52.547 [ thread-1] 작업 시작
15:01:52.547 [ thread-2] 작업 시작
15:01:52.552 [ thread-2] 작업 완료: sum=3775
15:01:52.552 [     main] s1.sum=0
15:01:52.552 [ thread-1] 작업 완료: sum=1275
15:01:52.552 [     main] s2.sum=3775
15:01:52.552 [     main] end
 */
```
```
start()를 호출하면:
새 스레드가 실행 가능(Runnable) 상태가 됨
하지만 언제 실제로 실행될지는 OS 스케줄러가 결정
main 스레드는 기다리지 않고 다음 줄로 진행
보통:
main: start() 호출
main: getSum() 호출 (이 시점에 sum=0)
main: end
----- 조금 후 -----
thread-1: run() 실행 시작
thread-2: run() 실행 시작
때로는 (CPU 코어가 여러 개이고, 시스템 부하가 낮을 때):
main: start() 호출
thread-1: run() 실행 시작 (다른 코어에서 거의 동시에)
thread-2: run() 실행 시작
main: getSum() 호출
결론:
start() 호출이 즉시 실행을 보장하지는 않지만
보장하지 않을 뿐 빠르게 시작될 수는 있다.
로그의 타임스탬프를 보면 모두 밀리초 단위로 거의 동시에 일어나고 있다.
스레드 스케줄링은 비결정적이므로 실행할 때마다 순서가 달라질 수 있다.
그래서 join()을 사용해서 명시적으로 기다려야 한다.
```
```
OS 관점에서의 스레드
Process: Java Application
├─ Thread-0 (main 스레드)
├─ Thread-1 (thread-1)
└─ Thread-2 (thread-2)
OS 입장에서는:
main도 그냥 하나의 스레드일 뿐
thread-1, thread-2와 동등한 레벨
모두 같은 프로세스 내의 스레드들
main 스레드도 다른 스레드와 똑같이 스케줄링
케이스 1: main이 계속 실행됨
CPU Core 1: [main ─────────────►]
CPU Core 2: [idle ─► thread-1 ►]
CPU Core 3: [idle ─► thread-2 ►]
main이 계속 CPU를 점유
start() 호출 후 바로 다음 줄 실행
thread-1, thread-2는 조금 후에 시작
케이스 2: main이 선점당함 (context switch)
CPU Core 1: [main ► 스위칭 ► thread-1 ►]
CPU Core 2: [thread-2 ──────────────────►]
CPU Core 3: [idle ──► main 재개 ─────────►]
start() 호출 직후 main이 CPU를 빼앗김
thread-1, thread-2가 먼저 실행
main은 나중에 다시 스케줄링됨
main도 다른 스레드처럼 선점(preemption) 당할 수 있음
Runnable 상태로 대기하다가 다시 스케줄링됨
특별한 우선순위가 없음 (일반 스레드와 동일)
main 스레드의 유일한 특징:
프로그램 시작점
```
- this
- 인스턴스 메서드를 호출하면,
- 어떤 인스턴스 메서드를 호출했는지 기억하기 위해
- 해당 인스턴스 참조값을 스텍 프레임 내부에 this로 저장
```java
Sum s1 = new Sum(1, 50); //s1은 인스턴스 (객체)
Thread t1 = new Thread(s1); //t1도 인스턴스 (Thread 객체)
t1.start(); //새로운 스레드(실행 흐름)가 시작됨
//인스턴스 (객체): 힙 메모리에 있는 데이터
//s1, t1 모두 인스턴스
//스레드 (실행 흐름): CPU에서 실행되는 흐름
//t1.start()로 시작된 새로운 실행 흐름
/* 스레드와 this의 관계 */
//thread-1이 실행할 때
@Override
public void run() {
    log("작업 시작");
    for (int i = startVal; i <= endVal; i++) {
        sum += i; //이건 this.sum += i;와 같음
    }
}
//thread-1이 실행하면:
/*
스레드: thread-1 (실행 흐름)
├─ 스택 프레임
│  ├─ this → s1 인스턴스를 가리킴
│  ├─ i (지역 변수)
│  └─ ...
│
힙 메모리
└─ s1 인스턴스
   ├─ startVal = 1
   ├─ endVal = 50
   └─ sum = 0 → 1275
 */
javaSum s1 = new Sum(1, 50); //① 인스턴스 생성 (힙에)
Thread t1 = new Thread(s1); //② Thread 인스턴스 생성
t1.start(); //③ 새 스레드가 s1.run()을 실행
//③번에서 일어나는 일:
/*
thread-1 (새 실행 흐름)
└─ s1.run()을 호출
   └─ 스택 프레임에 this = s1 저장
      └─ s1 인스턴스에 접근
 */
//여러 스레드가 같은 인스턴스를 공유할 수도 있음
javaSum s1 = new Sum(1, 50);
Thread t1 = new Thread(s1);
Thread t2 = new Thread(s1); // 같은 s1 인스턴스
t1.start(); //thread-1이 s1.run() 실행
t2.start(); //thread-2도 s1.run() 실행
//이 경우 두 스레드가 같은 인스턴스(s1)를 가리킴:
/*
힙 메모리
└─ s1 인스턴스 (하나)
   └─ sum 변수 (공유됨)
thread-1 스택          thread-2 스택
├─ run() 프레임       ├─ run() 프레임
│  └─ this → s1      │  └─ this → s1
 */
javaThread t1 = new Thread(s1, "thread-1");
t1.start();
//스레드가 인스턴스가 되는 게 아니라
//새 스레드가 s1이라는 인스턴스의 run() 메서드를 실행하는 것
//그때 스택 프레임에 this = s1로 저장됨
//인스턴스: 힙에 있는 데이터 (s1, s2 등)
//스레드: 실행 흐름 (main, thread-1, thread-2 등)
//this: 각 스레드의 스택 프레임에 저장된, 현재 인스턴스의 참조값
/*
thread-1 실행 흐름 ─► s1 인스턴스의 run() 호출 ─► this = s1
thread-2 실행 흐름 ─► s2 인스턴스의 run() 호출 ─► this = s2
main 실행 흐름 ─► getSum() 호출 ─► this = s1 또는 s2
각 스레드는 독립적인 스택을 가지지만, 같은 힙의 인스턴스들을 공유
 */
```
```java
/* 두 가지 인스턴스, 두 가지 this */
Sum s1 = new Sum(1, 50); //① Sum 인스턴스
Thread t1 = new Thread(s1); //② Thread 인스턴스
t1.start();
/*
힙 메모리
├─ Thread 인스턴스 (t1)
│  ├─ name = "thread-1"
│  ├─ target = s1 참조  ← Runnable 객체를 가리킴
│  ├─ priority = 5
│  └─ ...
│
└─ Sum 인스턴스 (s1)
   ├─ startVal = 1
   ├─ endVal = 50
   └─ sum = 0
 */
/* 실제 실행 시 this의 변화 */
//thread-1 스레드가 실행될 때:
//Thread 클래스 내부 (간단히 표현)
public class Thread {
    private Runnable target;
    public void run() {
        if (target != null) {
            target.run(); //여기서 this는 Thread 인스턴스
        }
    }
}
//호출 스택:
/*
thread-1 스택
│
├─ Sum.run() 프레임
│  └─ this = s1 (Sum 인스턴스)
│     └─ sum += i 실행
│
└─ Thread.run() 프레임  
   └─ this = t1 (Thread 인스턴스)
      └─ target.run() 호출
 */
/* 구체적인 예시 */
class MyThread extends Thread {
    @Override
    public void run() {
        //여기서 this는 Thread 인스턴스
        System.out.println(this.getName()); //Thread의 메서드
        System.out.println(this.getId()); //Thread의 메서드
    }
}
//사용
MyThread t1 = new MyThread();
t1.start();
//이 경우:
/*
thread-1 스택
└─ run() 프레임
   └─ this = t1 (MyThread 인스턴스, Thread를 상속)
 */
//원래 코드의 경우:
Sum s1 = new Sum(1, 50);
Thread t1 = new Thread(s1);
t1.start();
//실행 순서와 this:
/*
1. Thread.start() 호출
   └─ 호출 스택에 this = t1 (Thread 인스턴스)
2. JVM이 새 스레드(thread-1) 생성
3. thread-1이 Thread.run() 실행
   └─ 스택 프레임에 this = t1
   └─ if (target != null) 확인
   └─ target.run() 호출 (target은 s1)
4. thread-1이 Sum.run() 실행
   └─ 새 스택 프레임에 this = s1 (Sum 인스턴스)
   └─ sum += i 실행
 */
//시각화
/*
힙 메모리                    thread-1 스택
┌─────────────────┐         ┌──────────────────┐
│ Thread (t1)     │    ┌────│ Sum.run()        │
│ ├─ name         │    │    │ └─ this → s1     │
│ ├─ target ──────┼────┘    ├──────────────────┤
│ └─ ...          │         │ Thread.run()     │
└─────────────────┘         │ └─ this → t1     │
                            └──────────────────┘
┌─────────────────┐
│ Sum (s1)        │◄────── this가 가리킴
│ ├─ startVal     │
│ ├─ endVal       │
│ └─ sum          │
└─────────────────┘
 */
//Thread 객체의 this: Thread 인스턴스 자체 (t1)
//Thread의 메서드들 (getName(), getId() 등)을 호출할 때 사용
//Runnable 객체의 this: Sum 인스턴스 (s1)
//Sum의 메서드와 필드에 접근할 때 사용
//호출 체인:
/*
t1.start() 
   → Thread.run() [this = t1]
   → target.run() [this = s1]
   → sum += i [this.sum]
 */
//실제 작업을 수행하는 run() 메서드는 보통 Runnable 객체(s1)의 것이라서
//그쪽의 this가 더 자주 사용되는 것
```
- `while(t1.getState() != TERMINATED) {}`
- `//계산 결과 출력`
- 이렇게 main이 t1 스레드를 기다리는 효과 낼 수 있지만,
- 반복문이 실행되는 것다. CPU 연산을 사용한다.
- join() 메서드를 사용할 수 있다.
```java
//join() 사용법
public class JoinExample {
    public static void main(String[] args) throws InterruptedException {
        log("start");
        Sum s1 = new Sum(1, 50);
        Sum s2 = new Sum(51, 100);
        Thread t1 = new Thread(s1, "thread-1");
        Thread t2 = new Thread(s2, "thread-2");
        t1.start();
        t2.start();
        //join() 사용 - t1이 끝날 때까지 main 스레드가 대기
        t1.join();
        //main 스레드가 WAITING 상태로 전환
        //t1이 종료될 때까지 CPU를 사용하지 않음
        //t1 종료되면 main 스레드 깨어남
        //main 스레드:
        //RUNNABLE → join() 호출 → WAITING → t1 종료 → RUNNABLE
        //CPU를 낭비하지 않음 (busy waiting과 다름)
        log("t1 완료");
        //t2가 끝날 때까지 main 스레드가 대기
        t2.join();
        log("t2 완료");
        //안전하게 결과 출력 가능
        log("s1.sum=" + s1.getSum());
        log("s2.sum=" + s2.getSum());
        log("총합=" + (s1.getSum() + s2.getSum()));
        log("end");
    }
}
/*
15:30:00.100 [     main] start
15:30:00.105 [ thread-1] 작업 시작
15:30:00.105 [ thread-2] 작업 시작
15:30:02.110 [ thread-1] 작업 완료: sum=1275
15:30:02.110 [     main] t1 완료
15:30:02.110 [ thread-2] 작업 완료: sum=3775
15:30:02.110 [     main] t2 완료
15:30:02.110 [     main] s1.sum=1275
15:30:02.110 [     main] s2.sum=3775
15:30:02.110 [     main] 총합=5050
15:30:02.110 [     main] end
 */
```
- 기본 join() - 무한정 대기
- 시간 제한 join() - 타임아웃
```java
t1.join(3000); //최대 3초만 대기
//3초 후에도 t1이 안 끝나면 main이 계속 진행
if (t1.isAlive()) {
    log("t1이 아직 실행 중");
}
```
- InterruptedException 처리 필요:
```java
try {
    t1.join();
} catch (InterruptedException e) {
    //대기 중 인터럽트 발생 시 처리
    log("대기가 중단됨");
}
//또는 메서드에 throws 선언
public static void main(String[] args) throws InterruptedException {
    t1.join();
}
```
- start() 전에 join() 호출하면 바로 리턴됨
- 여러 스레드 대기 가능: 순차적으로 join() 호출
```java
Thread t1 = new Thread(s1);
Thread t2 = new Thread(s2);
Thread t3 = new Thread(s3);
Thread t4 = new Thread(s4);
t1.start();
t2.start();
t3.start();
t4.start();
//모든 스레드가 끝날 때까지 대기
t1.join();
t2.join();
t3.join();
t4.join();
```
- join() 순서는 완료 순서가 아님: join()은 그 스레드를 기다릴 뿐
- t2.join()을 나중에 호출해도, t2는 이미 끝났을 수 있음
- 정확한 시간이 필요하면: 각 스레드가 자신의 완료 시간을 기록해야 함