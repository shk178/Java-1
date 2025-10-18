### ExecutorService - 작업 컬렉션 처리
- invokeAll(tasks)
- 모든 Callable 작업을 제출하고, 모든 작업이 완료될 때까지 기다린다.
- invokeAll(tasks, timeout, unit)
- 지정된 시간 내에 모든 Callable 작업을 제출하고 완료될 때까지 기다린다.
- invokeAny(tasks)
- 하나의 Callable 작업이 완료될 때까지 기다리고, 가장 먼저 완료된 작업의 결과를 반환한다.
- 완료되지 않은 나머지 작업은 취소한다.
- invokeAny(tasks, timeout, unit)
- 지정된 시간 내에 하나의 Callable 작업이 완료될 때까지 기다리고, 가장 먼저 완료된 작업의 결과를 반환한다.
- 완료되지 않은 나머지 작업은 취소한다.
- 251011-java-adv1/src/executor/InvokeMain.java
```java
/*
21:41:22.682 [pool-1-thread-2] B 시작
21:41:22.682 [pool-1-thread-1] A 시작
21:41:22.682 [pool-1-thread-3] C 시작
21:41:23.704 [pool-1-thread-1] A 완료, return=1000
21:41:24.690 [pool-1-thread-2] B 완료, return=2000
21:41:25.697 [pool-1-thread-3] C 완료, return=3000
21:41:25.697 [     main] result=1000
21:41:25.697 [     main] result=2000
21:41:25.697 [     main] result=3000
21:41:25.700 [pool-1-thread-1] D 시작
21:41:25.700 [pool-1-thread-3] F 시작
21:41:25.700 [pool-1-thread-2] E 시작
21:41:29.710 [pool-1-thread-1] D 완료, return=4000
21:41:29.710 [pool-1-thread-1] D 시작
21:41:30.709 [pool-1-thread-2] E 완료, return=5000
21:41:30.709 [pool-1-thread-2] E 시작
21:41:31.708 [pool-1-thread-3] F 완료, return=6000
21:41:31.709 [pool-1-thread-3] F 시작
21:41:33.714 [pool-1-thread-1] D 완료, return=4000
//submit으로 받은 Future는
//invokeAny가 내부적으로 생성한 Future와 다른 객체다.
21:41:33.716 [     main] result=4000
21:41:33.716 [pool-1-thread-3] 인터럽트 발생: sleep interrupted
21:41:33.716 [pool-1-thread-2] 인터럽트 발생: sleep interrupted
//invokeAny하면 내부적으로 Future 만들어서 전부 실행하고
//그중 하나가 완료하면 나머지를 cancel(true)한다.
21:41:33.721 [     main] isCancelled=false
21:41:33.721 [     main] isCancelled=false
21:41:33.721 [     main] isCancelled=false
//submit으로 받은 Future은 별개의 Future다.
//cancel 호출이 전파되지 않는다.
- ExecutorService는 풀에 있는 워커 스레드를 활용해 모든 작업을 병렬로 실행합니다.
- 가장 먼저 call()을 성공적으로 완료한 작업의 결과를 반환합니다.
- 나머지 작업은 cancel(true)로 인터럽트를 걸어 중단시킵니다.
 */
```
- 즉, ExecutorService에서 단일 실행:
- submit(Callable / Runnable 다 쓸 수 있다. Runnable 결과 저장할 인자도 넘길 수 있다.)
- 다수 실행: invokeAll, invokeAny
# 14. 스레드 풀과 Executor 프레임워크2
- 서버 기능 업데이트하는 등 서버를 재시작해야 하는 경우가 있다.
- 새로운 주문 요청은 막고, 접수된 주문은 모두 완료한 다음에 종료하고 시작하는 것이 이상적이다.
- 문제 없이 안정적 종료 = graceful shutdown이라고 한다.
- ExecutorService 종료 메서드
`void shutdown()`
- 새로운 태스크 제출을 거부하고, 이미 제출된 태스크들은 모두 실행
- 블로킹: 논블로킹 - 즉시 반환
- 실행 중인 태스크나 대기 중인 태스크가 완료될 때까지 기다리지 않는다.
- shutdown 호출 후에는 새로운 태스크 제출 시 `RejectedExecutionException` 발생
```java
executor.shutdown(); //즉시 반환
//하지만 태스크들은 백그라운드에서 계속 실행됨
```
`List<Runnable> shutdownNow()`
- 동작: 실행 대기 중인 태스크들을 취소하고, 실행 중인 태스크들을 interrupt 시도
- 블로킹: 논블로킹 - 즉시 반환
- 반환값: 실행되지 않은 채 대기 중이던 태스크 리스트
- 실행 중인 태스크는 interrupt되지만, interrupt에 응답하지 않으면 계속 실행될 수 있다.
```java
List<Runnable> notExecuted = executor.shutdownNow(); //즉시 반환
//대기 중이던 태스크 리스트를 받음
```
`void close()` (Java 19+)
- 동작: `shutdown()`을 호출하고, 태스크들이 완료될 때까지 기다린다.
- 블로킹: 블로킹 - 태스크 완료까지 대기
- AutoCloseable 인터페이스 구현으로 try-with-resources 사용 가능
- 1일 동안 기다린 후에도 종료되지 않으면 `shutdownNow()` 호출
```java
try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
    //작업 수행
} //close()가 자동 호출되어 종료 대기
```
`boolean isShutdown()`
- 동작: shutdown 또는 shutdownNow가 호출되었는지 확인
- 블로킹: 논블로킹 - 즉시 반환
- 반환값: shutdown이 시작되었으면 true, 아니면 false
- 주의: true라고 해서 모든 태스크가 완료된 것은 아니다.
```java
if (executor.isShutdown()) {
    System.out.println("종료 프로세스 시작됨");
}
```
`boolean isTerminated()`
- 동작: shutdown 후 모든 태스크가 완료되었는지 확인
- 블로킹: 논블로킹 - 즉시 반환
- 반환값: 모든 태스크가 완료되었으면 true
- `isShutdown()`이 true이고 모든 태스크가 완료되어야 true 반환
```java
if (executor.isTerminated()) {
    System.out.println("모든 태스크 완료됨");
}
```
`boolean awaitTermination(long timeout, TimeUnit unit)`
- 동작: shutdown 후 모든 태스크가 완료되거나, 타임아웃이 발생하거나, 현재 스레드가 interrupt될 때까지 대기
- 블로킹: 블로킹 - 조건 충족까지 대기
- 반환값: 타임아웃 전에 종료되면 true, 타임아웃 발생 시 false
- `InterruptedException` 발생 가능
```java
executor.shutdown();
try {
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow(); //타임아웃 시 강제 종료
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
}
```
- 일반적인 종료 패턴
```java
//우아한 종료 패턴
executor.shutdown(); //새 태스크 거부
try {
    //60초 대기
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow(); //강제 종료
        //다시 한번 대기
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            System.err.println("종료 실패");
        }
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
    Thread.currentThread().interrupt();
}
```
- 논블로킹 메서드들(shutdown, shutdownNow)은 상태만 변경하고 즉시 반환
- shutdown() 동작 순서
```
1. ExecutorService의 상태를 SHUTDOWN으로 변경
2. 새로운 태스크 제출을 거부하도록 설정
3. 이미 제출된 태스크들은 그대로 유지
4. 즉시 반환 (블로킹 없음)
   ↓
[백그라운드에서]
5. 큐에 대기 중인 태스크들이 순차적으로 실행됨
6. 실행 중인 태스크들이 완료될 때까지 계속 실행
7. 모든 태스크 완료 시 상태가 TERMINATED로 변경
//큐에 10개 태스크가 대기 중
executor.shutdown(); //<- 여기서 즉시 반환
//하지만 10개 태스크는 모두 실행됨
```
- shutdownNow() 동작 순서
```
1. ExecutorService의 상태를 STOP으로 변경
2. 새로운 태스크 제출을 거부하도록 설정
3. 큐에서 대기 중인 태스크들을 모두 제거
4. 제거된 태스크들을 List로 수집
5. 현재 실행 중인 모든 워커 스레드에 interrupt() 호출
6. 제거된 태스크 List 반환 (즉시 반환)
   ↓
[백그라운드에서]
7. interrupt에 응답하는 태스크들은 중단됨
8. interrupt를 무시하는 태스크들은 계속 실행될 수 있음
9. 모든 스레드 종료 시 상태가 TERMINATED로 변경
//큐에 100개 대기, 5개 실행 중
List<Runnable> notExecuted = executor.shutdownNow();
//notExecuted.size() == 100 (대기 중이던 것들)
//실행 중인 5개는 interrupt 신호를 받음
```
- 블로킹 메서드들(awaitTermination, close)은 실제로 종료될 때까지 대기
- awaitTermination(timeout, unit) 동작 순서
```
1. 현재 상태가 TERMINATED인지 확인
   └─ 이미 TERMINATED면 즉시 true 반환
2. 조건 변수(Condition)에 대기 등록
3. 지정된 timeout 동안 대기 (블로킹)
   ├─ 모든 태스크 완료 시 notify 받음
   ├─ timeout 경과
   └─ interrupt 발생
4. 대기 해제 사유 확인:
   ├─ TERMINATED 상태 도달 → true 반환
   ├─ timeout 경과 → false 반환
   └─ InterruptedException 발생 → 예외 throw
executor.shutdown(); //t=0ms, 즉시 반환
boolean done = executor.awaitTermination(5, SECONDS); //t=0ms, 블로킹 시작
[백그라운드]
- 태스크 A 완료 //t=1000ms
- 태스크 B 완료 //t=2000ms
- 태스크 C 완료 //t=3000ms
- 모든 태스크 완료, TERMINATED 상태 //t=3000ms
awaitTermination 반환 true //t=3000ms (5초 전에 완료)
```
- close() 동작 순서
```
1. shutdown() 호출
   ├─ 상태를 SHUTDOWN으로 변경
   └─ 새 태스크 거부
2. awaitTermination(1, TimeUnit.DAYS) 호출
   ├─ 최대 1일 동안 대기 (블로킹)
   └─ 모든 태스크 완료 대기
3. 1일 내 완료되면:
   └─ 정상 반환
4. 1일이 지나도 미완료 시:
   ├─ shutdownNow() 호출
   ├─ 대기 중인 태스크 취소
   ├─ 실행 중인 태스크 interrupt
   └─ awaitTermination(1, TimeUnit.DAYS) 다시 호출
5. 여전히 미완료 시:
   └─ ExecutionException 발생
try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
    executor.submit(() -> longTask());
} //<- 여기서 close()가 호출되고, 태스크 완료까지 블로킹됨
```
- isShutdown() 동작 순서
```
1. ExecutorService의 현재 상태를 확인
2. 상태가 SHUTDOWN, STOP, TIDYING, TERMINATED 중 하나인지 체크
3. 해당되면 true, 아니면 false 즉시 반환
RUNNING → isShutdown() = false
SHUTDOWN → isShutdown() = true
STOP → isShutdown() = true
TERMINATED → isShutdown() = true
```
- isTerminated() 동작 순서
```
1. ExecutorService의 현재 상태를 확인
2. 상태가 정확히 TERMINATED인지 체크
3. TERMINATED면 true, 아니면 false 즉시 반환
RUNNING → isTerminated() = false
SHUTDOWN (태스크 실행 중) → isTerminated() = false
STOP (스레드 정리 중) → isTerminated() = false
TERMINATED → isTerminated() = true
```
- 상태 전이
```java
/*
RUNNING
  ↓ shutdown()
SHUTDOWN (큐의 태스크 실행 중)
  ↓ 모든 태스크 완료
TIDYING (정리 작업)
  ↓
TERMINATED

or

RUNNING
  ↓ shutdownNow()
STOP (실행 중인 태스크 interrupt)
  ↓ 모든 스레드 종료
TIDYING
  ↓
TERMINATED
*/
ExecutorService executor = Executors.newFixedThreadPool(2);
//상태: RUNNING
executor.isShutdown(); //false
executor.isTerminated(); //false
executor.submit(() -> sleep(1000)); //태스크 1
executor.submit(() -> sleep(2000)); //태스크 2
executor.submit(() -> sleep(3000)); //태스크 3 (큐 대기)
executor.shutdown(); //즉시 반환
//상태: SHUTDOWN
executor.isShutdown(); //true (shutdown 호출됨)
executor.isTerminated(); //false (아직 실행 중)
//1초 후: 태스크 1 완료
executor.isShutdown(); //true
executor.isTerminated(); //false (태스크 2, 3 실행 중)
//2초 후: 태스크 2 완료
executor.isShutdown(); //true
executor.isTerminated(); //false (태스크 3 실행 중)
//3초 후: 태스크 3 완료
//상태: TERMINATED
executor.isShutdown(); //true
executor.isTerminated(); //true (모두 완료)
```
- 우아한 종료 구현
```java
public class Main {
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(2); //워커 스레드 2개
        es.execute(new RunnableTask("A")); //태스크 A → 즉시 실행 (스레드1)
        es.execute(new RunnableTask("B")); //태스크 B → 즉시 실행 (스레드2)
        es.execute(new RunnableTask("C")); //태스크 C → 큐 대기
        es.execute(new RunnableTask("D", 100_000)); //태스크 D (100초 소요) → 큐 대기
        printState(es); //상태: RUNNING, isShutdown=false, isTerminated=false
        shutdownAndAwaitTermination(es); //우아한 종료 시도
        printState(es); //상태: TERMINATED (또는 SHUTDOWN)
        es.close(); //이미 종료되었으면 아무 일도 안 함
    }
    private static void shutdownAndAwaitTermination(ExecutorService es) {
        //새로운 작업 제출을 거부 - execute(), submit() 호출 시 RejectedExecutionException 발생
        //이미 큐에 들어간 작업들은 모두 끝까지 수행 - 즉시 스레드를 중단하지는 않는다.
        //상태는 RUNNING → SHUTDOWN으로 바뀜
        es.shutdown();
        try {
            //모든 작업이 끝나고 스레드풀 종료될 때까지 최대 10초간 기다림
            //true: 모든 작업이 10초 내에 끝남 → 정상 종료
            //false: 아직 안 끝남 → 다음 단계로
            log("첫 번째 await 시작");
            if (!es.awaitTermination(10, TimeUnit.SECONDS)) {
                log("shutdown 타임아웃");
                //강제 종료 시도
                //실행 중이던 스레드에 interrupt()를 걸어 중단 요청
                //큐에 남은 대기 작업들을 모두 꺼내서 반환
                //대부분의 Runnable / Callable이 InterruptedException 감지 시 종료하도록 작성됨
                es.shutdownNow();
                //강제 종료 명령 후 다시 한 번 10초 기다림
                log("두 번째 await 시작");
                if(!es.awaitTermination(10, TimeUnit.SECONDS)) {
                    //그래도 종료가 안 되면, 로그 남기는 등만 하고 다음으로 넘어감
                    log("shutdownNow 타임아웃");
                } else {
                    log("shutdownNow 완료");
                }
            }
        } catch (InterruptedException e) { //만약 await 대기 중 현재 스레드가 인터럽트되면 예외 발생
            es.shutdownNow(); //보통 shutdownNow()를 다시 호출한 후
            Thread.currentThread().interrupt(); //스레드의 인터럽트 상태를 복원
        } //현재 스레드의 예외만 잡을 수 있다. 다른 스레드의 예외는 못 잡는다.
    }
}
/*
14:05:20.840 [     main] [pool=2, active=2, queuedTasks=2, completedTasks=0]
14:05:20.840 [pool-1-thread-1] A 시작
14:05:20.840 [pool-1-thread-2] B 시작
14:05:20.843 [     main] 첫 번째 await 시작
14:05:21.856 [pool-1-thread-1] A 종료
14:05:21.856 [pool-1-thread-2] B 종료
14:05:21.856 [pool-1-thread-1] C 시작
14:05:21.856 [pool-1-thread-2] D 시작
14:05:22.859 [pool-1-thread-1] C 종료
14:05:30.844 [     main] shutdown 타임아웃
14:05:30.844 [     main] 두 번째 await 시작
14:05:30.844 [pool-1-thread-2] sleep 중 인터럽트 발생해서 잡음
14:05:30.844 [pool-1-thread-2] D 종료
14:05:30.844 [     main] shutdownNow 완료
14:05:30.844 [     main] [pool=0, active=0, queuedTasks=0, completedTasks=4]
 */
```
```java
//인터럽트를 건다: thread.interrupt() 메소드를 호출하는 것
//인터럽트 상태가 설정된다: 스레드 내부의 interrupt flag가 true로 바뀜
//InterruptedException이 발생한다: 특정 메소드(sleep, wait, join 등)가 interrupt 상태를 감지하고 예외를 던짐
//이런 메소드들은 InterruptedException을 던짐
Thread.sleep(1000); //TIMED_WAITING → 인터럽트 감지 → 예외
Object.wait(); //WAITING → 인터럽트 감지 → 예외
Thread.join(); //WAITING → 인터럽트 감지 → 예외
BlockingQueue.take(); //WAITING → 인터럽트 감지 → 예외
Lock.lockInterruptibly(); //WAITING → 인터럽트 감지 → 예외
//다른 경우 직접 체크해서 인터럽트 처리 권장 - 종료하도록 작성
while (!Thread.currentThread().isInterrupted()) {
    count++;
}
log("인터럽트 감지, 종료");
```
- shutdownNow 호출 후 await하는 이유
- 작업 중인 스레드에 인터럽트 예외가 발생하면, finally 등 간단한 작업이 수행될 수도 있어서 기다린다.
- 하지만 RUNNABLE 상태에서는 예외가 자동으로 발생하지 않는다.
- interrupt flag는 true로 설정되는데, 수동으로 확인하고 종료하는 코드가 아니면 계속 수행된다.
- 이런 경우 로그를 남기고 JVM 강제 종료하는 수밖에 없다.
### ThreadPoolExecutor 동작 방식
```java
ExecutorService es = new ThreadPoolExecutor(
    2,      //corePoolSize: 기본 스레드 수
    4,      //maximumPoolSize: 최대 스레드 수
    60,     //keepAliveTime: 초과 스레드 유지 시간
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(2) //큐 용량 2
);
```
- 1단계: Core 스레드 생성 (0 → corePoolSize)
```
작업 요청 1 → 스레드1 생성 + 즉시 실행
작업 요청 2 → 스레드2 생성 + 즉시 실행
```
- 현재 스레드 수 < corePoolSize이면 새 스레드를 만들어서 즉시 실행
- 2단계: 큐에 작업 대기 (core 스레드가 모두 사용 중)
```
작업 요청 3 → 큐에 저장 (대기)
작업 요청 4 → 큐에 저장 (대기)
```
- Core 스레드가 모두 바쁘면 → 큐에 작업을 넣고 대기
- 스레드1이나 스레드2가 작업을 끝내면 큐에서 꺼내서 실행
- 3단계: 초과 스레드 생성 (큐가 가득 참)
```
작업 요청 5 → 큐 가득참 → 스레드3 생성 + 즉시 실행
작업 요청 6 → 큐 가득찬 상태 유지 → 스레드4 생성 + 즉시 실행
```
- 큐가 가득 찼는데 작업이 더 들어오면
- maximumPoolSize까지 추가 스레드(초과 스레드) 생성
- 새로 만든 스레드가 새 작업을 즉시 실행
- 4단계: 작업 거절 (max 스레드 도달 + 큐 가득참)
```
작업 요청 7 → 스레드 4개 모두 사용 중 + 큐 가득참
            → RejectedExecutionException 발생
```
- 스레드를 미리 만든다는 의미
- 기본 동작 (lazy 생성)
```java
ExecutorService es = Executors.newFixedThreadPool(10);
//이 시점에는 스레드가 0개 (아직 안 만들어짐)
es.execute(task1); //이제 스레드1 생성
es.execute(task2); //이제 스레드2 생성
```
- Prestart (미리 생성)
```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, ...);
//방법1: 모든 core 스레드를 미리 생성
executor.prestartAllCoreThreads(); 
//즉시 10개 스레드 생성 (작업 없이 대기 상태)
//방법2: core 스레드 1개만 미리 생성
executor.prestartCoreThread();
//1개 스레드만 생성
//첫 번째 작업이 들어올 때 스레드 생성 시간 절약
//즉시 작업 처리 가능 (latency 감소)
```
- 초과 스레드의 생명주기
```java
ExecutorService es = new ThreadPoolExecutor(
    2,      //corePoolSize
    4,      //maximumPoolSize
    60,     //keepAliveTime: 60초
    TimeUnit.SECONDS,
    new LinkedBlockingQueue<>(2)
);
//1. 초과 스레드(스레드3, 스레드4) 생성
es.execute(() -> sleep(100)); //스레드3 생성 → 0.1초 후 작업 완료
es.execute(() -> sleep(100)); //스레드4 생성 → 0.1초 후 작업 완료
//2. 작업 완료 후
//스레드3, 스레드4는 즉시 종료되지 않음
//60초 동안 대기
//3-1. 60초 이내에 새 작업이 들어오면
es.execute(() -> sleep(100)); //스레드3 재사용 (새로 안 만듦)
//3-2. 60초 동안 작업이 안 들어오면
//60초 후 스레드3, 스레드4 자동 종료
```
- Core 스레드도 종료되게 하려면
```java
//Core 스레드는 기본적으로 종료 안 됨
executor.allowCoreThreadTimeOut(true);
//이제 core 스레드도 keepAliveTime 후 종료됨
//작업이 없으면 스레드 수가 0이 될 수 있음
```
### 스레드풀 전략
- Executors.newSingleThreadPool()
- `new ThreadPoolExecutor(1, 1,0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())`
- 단일 스레드 풀 전략
- 스레드 풀에 기본 스레드 1개만 사용
- 큐 사이즈에 제한이 없음 (LinkedBlockingQueue)
- 주로 간단 사용, 테스트 용도 사용
- Executors.newFixedThreadPool(nThreads)
- `new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())`
- 고정 풀 전략
- 스레드 풀에 기본 스레드 nThreads개 생성
- 큐 사이즈에 제한이 없음 (LinkedBlockingQueue)
- CPU, 메모리 사용 예측 가능한 안정적 방식
- 하지만 요청 늘어 큐가 늘어나 있어도, CPU, 메모리 사용량이 늘지 않아서 확인 늦을 수 있다.
- 서버 자원은 여유가 있는데 사용자에게 응답이 느려지는 문제다.
- Executors.newCachedThreadPool()
- `new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>())`
- 캐시 풀 전략
- 스레드 풀에 기본 스레드 사용x
- 60초 생존 주기 가진 초과 스레드만 사용
- 초과 스레드 수는 제한이 없음 (CPU, 메모리 최대 사용 가능)
- 큐에 작업을 저장하지 않음 (SynchronousQueue)
- 초과 스레드가 작업 바로 받아서 빠른 처리
- CPU, 메모리 너무 많이 사용하면 시스템 다운될 수 있다.
- 사용자 정의 풀 전략
- 일반적인 상황에는 고정 풀
- 긴급 상황에는 (갑자기 요청 증가) 추가 스레드 생성
- 긴급 대응이 어려우면 사용자의 요청을 거절
- `new ThreadPoolExecutor(100, 200, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000))`
```java
public static void main(String[] args) { //메인 스레드
    ThreadPoolExecutor executor = new ThreadPoolExecutor(
        100, 200, 60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(50)
    );
    //메인 스레드가 execute() 호출
    for (int i = 1; i <= 100; i++) {
        executor.execute(() -> sleep(10000)); //여기서 스레드 생성
    }
    //메인 스레드가 100개의 워커 스레드를 생성함
}
/*
[main 스레드]
  → executor.execute(작업1) 호출
    → ThreadPoolExecutor 내부에서 new Thread() 실행
    → pool-1-thread-1 생성
    → thread-1.start() 호출
  → executor.execute(작업2) 호출
    → new Thread() 실행
    → pool-1-thread-2 생성
    → thread-2.start() 호출
  ... 반복 ...
  → executor.execute(작업100) 호출
    → pool-1-thread-100 생성
    → thread-100.start() 호출
 */
```
- 100개 기본 스레드 생성하고 즉시 실행 (큐에 안 담김)
- 큐에 1000개 작업까지 담김
- 이후에 100개 추가 스레드 생성하고 즉시 실행
- 추가 스레드 작업 완료마다 60초 대기
- 200개 스레드 실행 + 큐 1000개 작업인데도 요청이 들어오면 RejectedExecutionException 발생
- RejectedExecutionException은 메인 스레드(또는 execute()를 호출한 스레드)에서 발생
```java
try {
    executor.execute(() -> sleep(10000)); //여기서
} catch (RejectedExecutionException e) { //메인 스레드가 catch
    log("작업 거절됨: " + e.getMessage());
}
/*
[메인 스레드]
    ↓
executor.execute(1201번째 작업)
    ↓
ThreadPoolExecutor 내부 체크:
    - 실행 중 스레드: 200개 (max 도달)
    - 큐: 1000개 (가득 찬 상태)
    - 더 이상 받을 수 없음
    ↓
throw new RejectedExecutionException("Task ... rejected")
    ↓
[메인 스레드]로 예외 전파
    ↓
메인 스레드에서 catch 또는 프로그램 종료
 */
```
### 스레드풀 예외 정책
- 새로운 작업을 거절하는 정책
- AbortPolicy: RejectedExecutionException 발생 (기본)
- DiscardPolicy: 조용히 거절
```java
ExecutorService executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.DiscardPolicy());
```
- CallerRunsPolicy: 작업 요청한 스레드가 직접 작업 실행
```java
ExecutorService executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
```
- AbortPolicy, DiscardPolicy, CallerRunsPolicy 다 RejectedExecutionHandler를 구현한다.
- 사용자 정의 (RejectedExecutionHandler 구현해서 생성)
```java
ExecutorService executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.MyRejectedExecutionPolicy());
static class MyRejectedExecutionHandler implements RejectedExecutionHandler {
    static AtomicInteger count = new AtomicInteger(0);
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        int i = count.incrementAndGet();
        log("[경고] 거절된 누적 작업 수: " + i);
    }
}
```