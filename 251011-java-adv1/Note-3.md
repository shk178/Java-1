# 5. 스레드 제어와 생명 주기2
- runFlag가 바뀌어도, 다음 루프에서 runFlag 체크 때 중단한다.
```java
public class Main {
    public static void main(String[] args) {
        MyTask m1 = new MyTask();
        Thread t1 = new Thread(m1, "thread-1");
        t1.start();
        sleep(4000);
        log("작업 중단 지시: runFlag=false");
        m1.runFlag = false;
    }
    static class MyTask implements Runnable {
        volatile boolean runFlag = true;
        @Override
        public void run() {
            while (runFlag) {
                log("작업 중");
                sleep(3000);
            }
            log("자원 정리, 종료");
        }
    }
}
/*
20:15:15.417 [ thread-1] 작업 중
20:15:18.420 [ thread-1] 작업 중
20:15:19.403 [     main] 작업 중단 지시: runFlag=false
20:15:21.421 [ thread-1] 자원 정리, 종료
 */
```
- MyLogger: 로그 찍는 유틸리티 (시간 + 스레드 이름 + 메시지)
- sleep(long millis): Thread.sleep()을 감싸는 함수
- 인터럽트가 발생하면 예외를 로그 찍고 RuntimeException으로 던짐
- Main2: 메인 클래스
- MyTask라는 Runnable을 스레드로 실행시키고 4초 뒤에 interrupt() 호출함
```java
public static void sleep(long millis) {
    try {
        Thread.sleep(millis);
    } catch (InterruptedException e) {
        log("인터럽트 발생: " + e.getMessage());
        throw new RuntimeException(e);
    }
}
public abstract class MyLogger {
    private static final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    public static void log(Object obj) {
        String t = LocalTime.now().format(f);
        System.out.printf("%s [%9s] %s\n", t, Thread.currentThread().getName(), obj);
    }
}
public class Main2 {
    public static void main(String[] args) {
        MyTask m1 = new MyTask();
        Thread t1 = new Thread(m1, "thread-1");
        t1.start();
        sleep(4000);
        log("작업 중단 지시: interrupt()");
        t1.interrupt();
        log("t1.isInterrupted=" + t1.isInterrupted());
    }
    static class MyTask implements Runnable {
        volatile boolean runFlag = true;
        @Override
        public void run() {
            while (runFlag) {
                log("작업 중");
                sleep(3000);
            }
            log("자원 정리, 종료");
        }
    }
}
/*
//main 4초간 잠듦
20:23:58.848 [ thread-1] 작업 중
//thread-1 3초간 잠듦
20:24:01.861 [ thread-1] 작업 중
//main 깨어남
20:24:02.841 [     main] 작업 중단 지시: interrupt()
//main thread-1에 인터럽트 신호를 보냄
//인터럽트 신호 = 스레드를 강제로 멈춰라가 아니라,
//지금 잠자고 있다면 InterruptedException을 던져라는 신호다.
20:24:02.845 [     main] t1.isInterrupted=true
//thread-1은 sleep(3000) 중이었다.
//인터럽트 신호를 받아서
//Thread.sleep()이 InterruptedException을 던짐
 */
catch (InterruptedException e) {
    log("인터럽트 발생: " + e.getMessage());
    throw new RuntimeException(e);
}
/*
//그 예외를 sleep() 메서드의 catch 블록이 잡음
20:24:02.853 [ thread-1] 인터럽트 발생: sleep interrupted
//그리고 RuntimeException을 던짐
//예외 전파 -> 스레드 종료:
//예외는 MyTask.run()으로 올라가서 처리되지 않아서
//thread-1이 비정상 종료됨
//JVM은 스택 트레이스를 찍음:
Exception in thread "thread-1" java.lang.RuntimeException: java.lang.InterruptedException: sleep interrupted
 - at control.ThreadUtils.sleep(ThreadUtils.java:11)
 - at interrupt.Main2$MyTask.run(Main2.java:22)
 - at java.base/java.lang.Thread.run(Thread.java:1583)
Caused by: java.lang.InterruptedException: sleep interrupted
 - at java.base/java.lang.Thread.sleep0(Native Method)
 - at java.base/java.lang.Thread.sleep(Thread.java:509)
 - at control.ThreadUtils.sleep(ThreadUtils.java:8)
 - ... 2 more
 */
//정리
/*
시점 - 스레드 - 동작
0초 - main - thread-1 시작
0초 - thread-1 - "작업 중" 로그 찍고 3초 잠
3초 - thread-1 - 다시 "작업 중" 로그 찍고 잠
4초 - main - interrupt() 호출
4초 - thread-1 - 자는 중이던 sleep()이 InterruptedException 던짐
4.01초 - thread-1 - “인터럽트 발생” 로그 찍고 RuntimeException 발생 → 스레드 종료
이후 - main - 종료
 */
```
- interrupt()는 스레드 내부의 인터럽트 상태 플래그를 true로 설정
```
thread-1: interrupted = true
Thread.sleep()은 이 플래그가 켜져 있으면
InterruptedException을 던진다.
그래서 MyTask.run() 안에서 sleep() 중이었다가 깨진다.
```
```java
catch (InterruptedException e) {
    log("인터럽트 발생: " + e.getMessage());
    throw new RuntimeException(e);
}
//여기서 RuntimeException이 던져지면
//스레드 내부에서 예외가 처리되지 않고 위로 올라가서
//run() 메서드가 종료된다.
//즉, 스레드가 종료된다.
//스레드 종료돼도 isInterrupted()는 true인 상태다.
t1.isInterrupted() == true
```
- sleep()에서 예외를 무시하면 스레드가 계속 실행 된다.
```java
//catch 블록에서 예외를 처리하고 다시 루프를 돌릴 때의 상황
catch (InterruptedException e) {
log("인터럽트 발생, 하지만 무시하고 계속 실행");
}
//이렇게 하면 sleep()에서 인터럽트 예외가 발생해도
//RuntimeException을 던지지 않고 while 루프를 다시 돈다.
//즉, thread-1은 중단되지 않는다.
//스레드를 멈추려면
//루프를 끊는 조건(예: runFlag = false)이 필요하다.
```
- volatile boolean runFlag의 의미
- 스레드 간의 메모리 가시성 문제를 해결하기 위한 키워드다.
- 스레드별 캐시: 자바의 각 스레드는 CPU 캐시를 사용한다.
- 그래서 다른 스레드가 변수 값을 바꿔도, 바로 보이지 않을 수 있다.
- 메인 메모리에 아직 반영이 안 되어서다.
```java
while (runFlag) { ... }
//main 스레드가 runFlag = false로 바꿔도,
//thread-1 입장에서는
//여전히 자기 캐시에 남아 있는 true를 보고
//계속 루프를 돌 수도 있다.
/* volatile은 이를 해결 */
//이 변수의 읽기/쓰기 연산은 항상 메인 메모리에서 일어난다.
//라고 보장한다.
//즉, 다른 스레드가 바뀐 값이 즉시 보인다.
//main 스레드가 runFlag = false;로 바꾸면
//thread-1에서도 바로 false로 인식할 수 있다.
```
- 인터럽트 신호를 받으면 예외를 잡고
- runFlag=false로 바꾸고 while을 빠져나오는 코드로 변경
```java
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
public class Main3 {
    //===== 로그 유틸 =====
    public static abstract class MyLogger {
        private static final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        public static void log(Object obj) {
            String t = LocalTime.now().format(f);
            System.out.printf("%s [%9s] %s\n", t, Thread.currentThread().getName(), obj);
        }
    }
    //===== sleep 유틸 =====
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //인터럽트 발생 시, 로그만 찍고 예외 던지지 않음
            MyLogger.log("인터럽트 발생: " + e.getMessage());
            //현재 스레드의 interrupted 상태를 다시 true로 만들어줌
            Thread.currentThread().interrupt();
        }
    }
    //===== Runnable 구현 클래스 =====
    static class MyTask implements Runnable {
        volatile boolean runFlag = true; //다른 스레드에서 변경 감지 가능
        @Override
        public void run() {
            while (runFlag && !Thread.currentThread().isInterrupted()) {
                MyLogger.log("작업 중");
                sleep(3000);
            }
            MyLogger.log("자원 정리, 종료");
        }
    }
    //===== main =====
    public static void main(String[] args) {
        MyTask m1 = new MyTask();
        Thread t1 = new Thread(m1, "thread-1");
        t1.start();
        sleep(4000);
        MyLogger.log("작업 중단 지시: interrupt()");
        t1.interrupt();
        MyLogger.log("t1.isInterrupted=" + t1.isInterrupted());
    }
}
/*
20:57:11.877 [ thread-1] 작업 중
20:57:14.882 [ thread-1] 작업 중
20:57:15.851 [     main] 작업 중단 지시: interrupt()
20:57:15.851 [ thread-1] 인터럽트 발생: sleep interrupted
20:57:15.851 [ thread-1] 자원 정리, 종료
20:57:15.854 [     main] t1.isInterrupted=true
 */
Thread.currentThread().interrupt();
//InterruptedException이 발생하면
//JVM이 자동으로 인터럽트 플래그를 false로 바꾼다.
//그래서 직접 다시 true로 설정해줘야 한다.
//그래야 while 조건에서 isInterrupted()가 true로 인식된다.
```
```java
@Override
public void run() {
    while (true) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log(Thread.currentThread().isInterrupted());
            log(e.getMessage());
            log(Thread.currentThread().getState());
        }
    }
}
//catch는 Thread.sleep(3000)에만 적용되어야 하므로
//try-catch는 sleep 안쪽에 있어야 한다.
```
```java
public class Main4 {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            try {
                System.out.println("잠들기 전");
                Thread.sleep(10000); //10초 동안 잠듦
                System.out.println("깨어남"); //깨어남 실행 없이 바로 인터럽트 발생
            } catch (InterruptedException e) {
                System.out.println("인터럽트 발생");
            }
        });
        t.start();
        //3초 후 인터럽트
        Thread.sleep(3000);
        t.interrupt();
    }
}
//잠들기 전
//인터럽트 발생
```
- 1. 스레드 인터럽트 메커니즘
- (1) 인터럽트 플래그
- Thread 클래스는 내부에 interrupt flag(boolean)를 가진다.
- t.interrupt() 호출 시 이 플래그가 true로 설정된다.
- 플래그는 인터럽트 상태만 표시하며, 스레드를 강제로 멈추지 않는다.
- (2) sleep() 동작 원리
- Thread.sleep(3000) 호출
- JVM이 OS에게 "3초간 스레드 블로킹" 요청
- OS가 스레드를 BLOCKED 상태로 전환
- 3초 후 OS가 스레드를 실행 가능 상태로 복귀
- (3) 인터럽트 시 즉시 깨어나는 원리
- 다른 스레드가 interrupt() 호출
- JVM이 해당 스레드의 interrupt flag를 true로 설정
- JVM이 스레드가 sleep(), wait(), join() 같은 블로킹 메서드 내부에 있는지 감시
- 블로킹 중이면:
- JVM이 OS에게 "스레드 깨워줘" 요청
- OS가 스레드를 깨움 (스레드 상태=RUNNABLE로 변경)
- JVM이 InterruptedException 발생 (interrupt flag=false로 변경)
- catch가 예외 잡아서 처리
- 2. JVM과 OS의 관계
- (1) OS-level 동작
- OS는 CPU 자원을 스레드 단위로 스케줄링
- 자바 프로그램은 OS 입장에서 하나의 프로세스
- Java Thread 객체는 OS native thread와 1:1 매핑
- (2) JVM의 역할
- JVM은 OS 기능을 감싸서 자바 개발자에게 제공:
- OS 기능: native sleep, thread blocking/waking
- 자바 기능: interrupt flag, InterruptedException 등 예외 처리
- 추상화: Thread.sleep() 같은 고수준 API 제공
```
개발자: Thread.sleep(3000)
  ↓
JVM: OS native sleep 호출 + interrupt flag 감시
  ↓
OS: 스레드를 BLOCKED 상태로 전환
```
- 3. JVM 내부 스레드
- JVM이 시작되면 자동으로 생성되는 스레드들 (모두 OS native thread):
- (1) Main Thread
- main() 메서드를 실행하는 스레드
- (2) GC Thread (Garbage Collector)
- 백그라운드에서 메모리 회수 작업 수행
- 참조되지 않는 객체를 감지하고 메모리 정리
- (3) JIT Compiler Thread
- 바이트코드를 네이티브 코드로 컴파일
- 실행 패턴을 분석해 성능 최적화
- (4) 기타 관리 스레드
- Signal Dispatcher: OS 시그널 처리
- Finalizer: finalize() 메서드 호출
- Reference Handler: WeakReference, SoftReference 관리
- (5) 내부 스레드가 계속 실행된다는 의미
- CPU를 독점하는 것이 아님
- 항상 살아있으면서 필요할 때 RUNNABLE 상태로 전환되어 작업 수행
- 4. 스레드 상태
- (1) Thread.getState()
- NEW: new Thread() 생성 후, start() 호출 전
- RUNNABLE: 실행 중이거나 실행 대기 중
- BLOCKED: 모니터 락 대기 중
- WAITING: 다른 스레드 작업을 무기한 대기
- TIMED_WAITING: 일정 시간 대기
- TERMINATED: 실행 완료
- (2) Thread.isAlive()
- true: RUNNABLE, BLOCKED, WAITING, TIMED_WAITING
- false: NEW, TERMINATED
- start() 호출 후 ~ run() 종료 전까지 true
- 5. 데몬 스레드 / 사용자 스레드
- (1) 사용자 스레드 (User Thread)
- 역할: 주요 비즈니스 로직 수행
- 종료: 직접 종료하거나 작업 완료 후 종료
- 예시: main 스레드, 비즈니스 로직 스레드
- (2) 데몬 스레드 (Daemon Thread)
- 역할: 백그라운드 보조 작업 (감시, 정리, 로깅)
- 종료: 모든 사용자 스레드 종료 시 종료되거나 작업 완료 후 종료
- 예시: GC 스레드, 로그 수집기, 캐시 정리기
- 설정: thread.setDaemon(true) (start() 호출 전에 설정)
- 6. 스레드 실행 흐름 제어 메서드
- (1) sleep()
- 지정된 시간 동안 현재 스레드를 멈춘다.
- 소속: Thread 클래스의 static 메서드
- 대상: 현재 실행 중인 스레드만 제어 가능
- 락 반환: 반환하지 않음 (synchronized 블록 내에서도)
- 호출 위치: 어디서나 가능
- 깨어나는 방법: 지정된 시간이 지나면 자동으로 RUNNABLE 상태 복귀
- 주요 용도: 단순 시간 지연, 주기적 작업
```java
javaThread.sleep(1000); //1초 대기
```
- (2) join()
- 다른 스레드가 종료될 때까지 현재 스레드를 기다리게 한다.
- 소속: Thread 클래스의 인스턴스 메서드
- 대상: t.join() 형태로 t 스레드의 종료를 대기
- 락 반환: 반환하지 않음
- 호출 위치: 어디서나 가능
- 깨어나는 방법: 대상 스레드가 종료되면 자동으로 RUNNABLE 상태 복귀
- 주요 용도: 스레드 간 작업 순서 동기화, 결과 대기
```java
javaThread t = new Thread(...);
t.start();
t.join(); //t가 끝날 때까지 대기
```
- (3) wait()
- 특정 조건이 충족될 때까지 스레드를 멈추고 다른 스레드와 통신한다.
- 소속: Object 클래스의 메서드 (모든 객체가 가짐)
- 대상: 현재 스레드를 대기 상태로 전환
- 락 반환: 반환함 (다른 스레드가 락을 획득할 수 있음)
- 호출 위치: 반드시 synchronized 블록 내에서만
- 깨어나는 방법: 다른 스레드가 notify() 또는 notifyAll() 호출
- 주요 용도: 스레드 간 협력 및 통신, 생산자-소비자 패턴
```java
javasynchronized(obj) {
    obj.wait(); //락 반환하고 대기
}
```
- 7. 메서드가 스레드 상태에 미치는 영향
- (1) sleep(long millis)
```
현재 스레드 → TIMED_WAITING 상태
              ↓ (시간 만료)
           RUNNABLE 상태
```
- 상태: TIMED_WAITING
- 지정된 시간 동안 일시 중지
- 락을 유지한 채 대기
- 시간 만료 시 자동으로 RUNNABLE 상태 복귀
- (2) join() / join(long millis)
```
현재 스레드 → WAITING (또는 TIMED_WAITING)
              ↓ (대상 스레드 종료)
           RUNNABLE 상태
```
- 상태: join(): WAITING
- 상태: join(millis): TIMED_WAITING
- 대상 스레드가 종료될 때까지 대기
- 락을 유지한 채 대기
- 대상 스레드 종료 시 자동으로 RUNNABLE 상태 복귀
- (3) wait() / wait(long millis)
```
synchronized 블록 진입
      ↓
   락 획득
      ↓
  wait() 호출
      ↓
   락 반환 → WAITING (또는 TIMED_WAITING)
      ↓ (notify/notifyAll)
   락 재획득 시도
      ↓
  RUNNABLE 상태
```
- 상태: wait(): WAITING
- 상태: wait(millis): TIMED_WAITING
- synchronized 블록 내에서만 호출 가능
- 락을 반환하고 대기
- notify() 또는 notifyAll() 호출 시 깨어남
- 깨어난 후 락을 다시 획득해야 실행 재개
- (4) BLOCKED 상태
- synchronized 블록 진입을 위해 락을 기다리는 상태
```
Thread A: synchronized 블록 진입 중 (락 보유)
Thread B: 같은 블록 진입 시도 → BLOCKED 상태
          ↓ (A가 락 반환)
       RUNNABLE 상태
```
- sleep(), join(), wait()로 인한 상태가 아님
- 다른 스레드가 이미 모니터 락을 소유하고 있어 진입하지 못할 때 발생
- 락이 반환되면 자동으로 RUNNABLE 상태로 전환
```
락 반환 여부
- 반환 안 함: sleep(), join() → 락을 계속 보유
- 반환함: wait() → 락을 놓고 다른 스레드가 사용 가능
깨어나는 조건
- sleep(): 시간 만료 (자동)
- join(): 대상 스레드 종료 (자동)
- wait(): notify/notifyAll 호출 (수동)
사용 목적
- sleep(): 단순 지연
- join(): 순서 보장
- wait(): 협력, 통신
```