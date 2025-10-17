```java
19:19:24.424 [     main] [pool=0, active=0, queuedTasks=0, completedTasks=0] //초기 상태
19:19:24.436 [     main] [pool=2, active=2, queuedTasks=2, completedTasks=0] //작업 중
19:19:24.437 [pool-1-thread-1] A 시작
19:19:24.437 [pool-1-thread-2] B 시작
19:19:25.446 [pool-1-thread-1] A 종료
19:19:25.446 [pool-1-thread-2] B 종료
19:19:25.446 [pool-1-thread-1] C 시작
19:19:25.446 [pool-1-thread-2] D 시작
19:19:26.463 [pool-1-thread-1] C 종료
19:19:26.463 [pool-1-thread-2] D 종료
19:19:27.447 [     main] [pool=2, active=0, queuedTasks=0, completedTasks=4] //작업 완료
19:19:27.447 [     main] [pool=0, active=0, queuedTasks=0, completedTasks=4] //shutdown 완료
```
- ThreadPoolExecutor(ExecutorService의 구현체)는 2가지 요소로 구성돼 있다.
- 스레드 풀 - 스레드를 관리한다.
- BlockingQueue - 작업을 보관한다. 생산자/소비자 문제 해결을 위해 BlockingQueue를 사용한다.
- 생산자가 es.execute(new RunnableTask("A"));를 호출하면, RunnableTask("A") 인스턴스가 BlockingQueue에 보관된다.
- 생산자: es.execute(작업)을 호출하면 내부에서 BlockingQueue에 작업을 보관한다. main 스레드가 호출하니까 main 스레드가 생산자다.
- 소비자: 스레드 풀에 있는 스레드들이 소비자다. 스레드가 BlockingQueue에서 작업을 꺼내서 실행한다.
- ThreadPoolExecutor의 생성자는 다음 속성을 사용한다.
- corePoolSize(코드에서 2로 설정): 스레드 풀에서 관리되는 기본 스레드 수
- maximumPoolSize(2로 설정): 스레드 풀에서 관리되는 최대 수
- keepAliveTime(0으로 설정), TimeUnit(밀리세컨즈로 설정): 기본 스레드 수를 초과해서 만들어진 스레드가 풀에 대기 상태로 유지되는 시간
- BlockingQueue(무제한 큐인 LinkedBlockingQueue로 설정): 작업을 보관할 블로킹 큐
- 작업은 BlockingQueue에서 꺼낸다.
- 하지만 스레드는 스레드 풀에서 꺼내지 않는다.
- 스레드의 상태가 변경되는 것이다.
- close()를 호출하면 ThreadPoolExecutor가 종료되고 스레드 풀의 스레드도 함께 종료된다.
- close()는 자바 19에 나온 거고 그전 버전에서는 shutdown() 등 호출한다.
```java
19:36:07.816 [ Thread-0] Runnable 시작
19:36:09.824 [ Thread-0] create=3
//value에 값을 저장
19:36:09.824 [ Thread-0] Runnable 완료
//join() 후에 value를 읽음
19:36:09.824 [     main] result=3
//run 메서드가 리턴을 안 해서 그렇다.
//run 메서드에서 체크 예외를 안 던져서 체크 예외도 못 던진다.
```
- Callable과 Future 인터페이스 도입
```java
package java.util.concurrent;
public interface Callable<V> {
    V call() throws Exception();
}
```
```java
public static void main(String[] args) throws ExecutionException, InterruptedException {
    ExecutorService es = Executors.newFixedThreadPool(1); //new ThreadPoolExecutor(...); 대신 사용
    Future<Integer> future = es.submit(new MyCallable());
    //Callable 반환이 Future 인터페이스를 통해 된다.
    //Future의 구현체 FutureTask가 생성된다.
    Integer result = future.get();
    //future.get()은 Interrupted/ExecutionException 체크 예외를 던진다.
    log("result=" + result);
    es.close();
}
static class MyCallable implements Callable<Integer> {
    @Override
    public Integer call() {
        log("Callable 시작");
        sleep(2000);
        int value = new Random().nextInt(10);
        log("create=" + value);
        log("Callable 완료");
        return value;
    }
}
```
- 작업 처리 중이라면 future.get()이 반환할 결과가 없을 것이다.
- Future = 미래의 결과를 받을 수 있는 객체라는 뜻이다.
- `Future<Integer> future = es.submit(new MyCallable());`
- submit() 호출 시 MyCallable 인스턴스를 전달한다.
- submit() 반환은 call()이 반환하는 Integer가 아니다.
- 생각해보면 MyCallable이 이 문장에서 바로 결과 반환할 수 없다.
- main 스레드가 아니라 스레드 풀의 스레드가 실행하기 떄문에 언제 결과 반환할 지 알 수 없다.
- es.submit()은 MyCallable의 결과를 나중에 받을 수 있는 Future라는 객체를 대신 반환한다.
- 이 Future라는 객체를 통해 전달한 작업의 미래 결과를 받을 수 있다.
```java
19:59:47.793 [pool-1-thread-1] Callable 시작
19:59:47.793 [     main] es.submit() 완료, future=java.util.concurrent.FutureTask@4fca772d[Not completed, task = executor.CallableMain2$MyCallable@506e1b77]
//FutureTask 객체 안에 new MyCallable() 인스턴스 참조를 보관한다.
//Callable 작업의 완료 여부와 작업의 결과 값도 보관한다.
//FutureTask가 즉시 반환되니까 Future<Integer> future =이 실행되고
//main이 다음 코드를 실행할 수 있다.
19:59:47.796 [     main] future.get() 시작, 완료까지 main=WAITING
19:59:49.814 [pool-1-thread-1] create=8
19:59:49.814 [pool-1-thread-1] Callable 완료
19:59:49.816 [     main] future.get() 완료, result=8
```
- `Future<Integer> future = es.submit(new MyCallable());`
- es.submit()은 MyCallable 작업을 스레드 풀에 제출한다.
- 백그라운드에서 스레드가 call() 메서드를 실행하기 시작한다.
- `log("es.submit() 완료, future=" + future);`
- 아직 작업이 완료되지 않았음을 확인할 수 있다.
- `Integer result = future.get();`
- future.get()은 블로킹 메서드다.
- Callable 작업이 완료될 때까지 main 스레드가 WAITING 상태로 대기한다.
- `Callable 작업이 이미 완료된 상태면 main 스레드는 대기하지 않는다.`
- `스레드가 어떤 결과를 얻기 위해 대기하는 것을 블로킹이라 한다. (Thread.join, Future,get 등이 블로킹 메서드)`
- 작업이 완료되면 반환값(Integer)을 받는다.
- main 스레드를 깨우고 main이 Future에서 결과를 반환 받는다.
- 작업을 마친 스레드는 다음 작업을 기다리며 WAITING 상태가 된다.
```
/* main 스레드 */
RUNNABLE (future.get() 호출)
↓
WAITING (작업 완료 대기)
↓
RUNNABLE (결과 받고 깨어남)
/* 작업 스레드 (pool-1-thread-1) */
RUNNABLE (call() 실행 중)
↓
TIMED_WAITING (sleep(2000) 중)
↓
RUNNABLE (작업 완료)
↓
WAITING (스레드 풀에서 다음 작업 대기)
[스레드 풀 생성]
    ↓
Thread-1 생성 → 풀 안에서 WAITING (작업 대기 중)
    ↓
[작업 제출 - submit(callable)]
    ↓
Thread-1 상태 변화: WAITING → RUNNABLE (작업 할당됨)
    ↓
Thread-1이 call() 실행
    ↓
작업 완료
    ↓
Thread-1 상태 변화: RUNNABLE → WAITING (작업 대기로 복귀)
    ↓
Thread-1은 계속 풀 안에 존재 (위치 이동 없음)
```
```
비동기 작업 제출: submit()은 즉시 반환되고 작업은 백그라운드에서 실행
결과 대기: future.get()은 작업 완료까지 기다림 (블로킹)
값 반환: Callable은 결과값을 반환할 수 있음 (Runnable은 불가능)
스레드 분리: main 스레드와 작업 스레드가 독립적으로 실행
```
- `Integer result = es.submit(new MyCallable()); //컴파일 에러`
- es.submit()의 반환 타입이 `Future<Integer>`라서 에러 난다.
- `Integer result = es.submit(new MyCallable()).get(); //이건 된다.`
- submit()이 `Future<Integer>` 반환
- 바로 .get() 호출해서 Integer 추출
- 결과적으로 작업이 완료될 때까지 즉시 대기
- 하지만 이렇게 하면 Future의 장점을 활용하지 못한다.
```
작업 취소 불가: future.cancel()
완료 여부 확인 불가: future.isDone()
타임아웃 설정 불가: future.get(1, TimeUnit.SECONDS)
```
- Future를 변수에 저장하는 것이 표준적이고 유연한 방식이다.
```java
SumTask task1 = new SumTask(1, 50);
SumTask task2 = new SumTask(51, 100);
ExecutorService es = Executors.newFixedThreadPool(2);
Future<Integer> future1 = es.submit(task1);
Future<Integer> future2 = es.submit(task2);
Integer sum1 = future1.get(); //main이 2초 간 대기
Integer sum2 = future2.get(); //이미 완료돼서 블로킹x
```
- 만약 submit.get으로 Integer에 저장했다면 2초 + 2초 대기해야 한다.
- 즉, Future 덕분에 비동기 처리가 가능해져서 main이 대기하는 시간을 줄일 수 있다.
```java
public interface Future<V> {
    boolean cancel(boolean mayInterruptIfRunning);
    //cancel은 Future를 취소 상태로 변경한다.
    //(true) 작업이 실행 중이면 Thread.interrupt()를 호출해 중단한다.
    //(false) 작업이 실행 중이면 중단하지 않는다.
    //반환 값: 작업이 성공적으로 취소되면 true, 이미 완료 or 취소 안 되면 false
    //취소된 Future에 get() 호출하면 런타임 예외 발생한다.
    boolean isCancelled();
    //취소 여부 확인한다.
    boolean isDone();
    //완료 여부 확인한다. (정상 완료, 취소, 예외 발생 종료 모두 true)
    V get() throws InterruptedException, ExecutionException;
    //작업이 완료될 때까지 대기하고, 완료되면 결과 반환
    //InterruptException: 대기 중 요청 스레드가 인터럽트 됐을 때
    //ExecutionException: 작업 중 작업 스레드가 예외 던졌을 때
        /*
        [작업 스레드]
        call() 실행 중
            ↓
        RuntimeException 발생
            ↓
        Future 객체에 예외 저장
            ↓
        작업 완료 (예외로 인한 완료)
        [main 스레드]  
        future.get() 호출
            ↓
        Future에 저장된 예외 확인
            ↓
        ExecutionException으로 감싸서 던짐
            ↓
        e.getCause()로 원본 예외 확인 가능
         */
    V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
    //get 대기 중 시간 초과되면 TimeoutException 던짐
    enum State {
        RUNNING, // 작업 실행 중
        SUCCESS, //작업 성공 완료
        FAILED, //작업 실패 완료
        CANCELLED //작업 취소 완료
    }
    default State state() {
        if (!isDone())
            return State.RUNNING;
        if (isCancelled())
            return State.CANCELLED;
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    get();  // may throw InterruptedException when done
                    return State.SUCCESS;
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (ExecutionException e) {
                    return State.FAILED;
                }
            }
        } finally {
            if (interrupted) Thread.currentThread().interrupt();
        }
    }
}
```
```java
public class FutureCancel {
    private static boolean mayInterruptIfRunning = true;
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(1);
        Future<String> future = es.submit(new MyTask());
        log("es.submit: future.state=" + future.state());
        sleep(3000);
        boolean result = future.cancel(mayInterruptIfRunning);
        log("future.cancel: future.state=" + future.state());
        try {
            log("future.get=" + future.get());
        } catch (CancellationException e) {
            log("Future는 이미 취소 되었습니다.");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        es.close();
    }
    static class MyTask implements Callable<String> {
        @Override
        public String call() {
            for (int i = 0; i < 10; i++) {
                log("작업 중: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return "Interrupted";
                }
            }
            return "Completed";
        }
    }
}
/*
20:56:15.408 [     main] es.submit: future.state=RUNNING
20:56:15.408 [pool-1-thread-1] 작업 중: 0
20:56:16.414 [pool-1-thread-1] 작업 중: 1
20:56:17.421 [pool-1-thread-1] 작업 중: 2
20:56:18.426 [pool-1-thread-1] 작업 중: 3
20:56:18.426 [     main] future.cancel: future.state=CANCELLED
20:56:18.427 [     main] Future는 이미 취소 되었습니다.
 */
```
- future.cancel(true);를 호출하면
- ExecutorService 내부에서 MyTask가 수행 중인 스레드를 찾아서 Thread.interrupt()를 호출
- Thread.sleep(1000);이 중단되고, InterruptedException이 발생
- 예외는 catch (InterruptedException e) 블록으로 잡힌다.
- 스레드의 작업(return 값)과 Future의 상태는 별개로 관리한다.
- MyTask.call()은 InterruptedException을 잡아서 "Interrupted"를 return
- 하지만 Future의 내부 상태는 이미 취소됨 표시됨
- future.get() 호출 시, FutureTask는 결과를 반환하지 않고 CancellationException을 던짐
```
FutureTask의 내부 상태
cancel(true) 호출 시점에
실행 중이었다면 INTERRUPTING → INTERRUPTED → CANCELLED로 전이
FutureTask.get()은 내부 상태가 CANCELLED/INTERRUPTED이면 CancellationException을 던진다.
```
- "Interrupted"가 FutureTask에 저장되어 있지만, get으로 반환되지 않음
```
Callable의 반환값 저장 과정
ExecutorService es = Executors.newFixedThreadPool(1);
- ExecutorService는 스레드 풀이다.
즉, 새로운 스레드를 매번 만들지 않고 미리 만들어둔 워커 스레드를 재사용
이 코드는 워커 스레드 1개짜리 풀을 만든다.
[ThreadPoolExecutor]
  ├── Worker-1 (스레드)
  └── BlockingQueue<Runnable> (작업 대기열)
- 워커 스레드는 스레드풀이 관리하는 고정된 실제 스레드
submit()을 여러 번 호출해도 새 스레드를 계속 만드는 게 아니라,
이 워커 스레드가 여러 Runnable (여기서는 FutureTask)을 순서대로 실행한다.
- submit(Callable<T>) 호출 시
FutureTask<T> task = new FutureTask<>(callable);
execute(task);
return task;
FutureTask 객체를 만들고 그걸 execute()로 스레드풀에 던진다.
- execute()는 내부적으로 이렇게 동작
만약 워커 스레드가 놀고 있으면 → 바로 task.run()을 워커 스레드가 실행
만약 워커 스레드가 모두 바쁘면 → task를 큐에 넣고 나중에 실행
FutureTask.run()은 항상 워커 스레드 안에서 실행된다.
즉, 실제 call()을 수행하는 스레드는 run()을 실행한 워커 스레드다.
- runner.compareAndSet(null, Thread.currentThread())의 의미
이 FutureTask를 실행 중인 스레드(워커 스레드)를 기록한다.
즉, runner에 대입되는 스레드는 바로 워커 스레드 자신이다.
runner는 cancel() 때 필요해서 저장된다.
public void run() {
    if (state != NEW || !runner.compareAndSet(null, Thread.currentThread())) //실행 독점 보장 로직
        return;
    try {
        V result = callable.call(); //여기서 "Interrupted" 반환
        set(result);                //FutureTask 내부 필드에 저장
    } catch (Throwable ex) {
        setException(ex);
    }
}
FutureTask의 state = CANCELLED / INTERRUPTED가 된 시점에서 결과값이 사용되지 않는 것이다.
실행 독점 보장 (single execution guarantee) 로직
1. state != NEW
FutureTask가 NEW → 아직 시작 안 함
COMPLETING, NORMAL, EXCEPTIONAL, CANCELLED, INTERRUPTING, INTERRUPTED
state != NEW 이면 이미 실행이 끝났거나 취소된 상태이므로 더 이상 실행하면 안 됩니다.
그래서 return.
이 조건으로 “이미 실행되거나 취소된 작업은 다시 실행하지 않는다”를 보장합니다.
2. !runner.compareAndSet(null, Thread.currentThread())
runner는 AtomicReference<Thread> 타입입니다.
즉, "현재 이 FutureTask를 실행 중인 스레드"를 기록하는 변수예요.
compareAndSet(null, Thread.currentThread()) 는 원자적 CAS 연산입니다.
→ runner가 아직 비어 있다면(null),
지금 실행하려는 스레드(Thread.currentThread())를 넣고 성공(true)
→ 이미 다른 스레드가 실행 중이면 실패(false)
즉, 오직 한 스레드만이 성공적으로 이 FutureTask를 실행하도록 보장하는 거예요.
이 조건으로 “FutureTask가 두 번 이상 run()되지 않도록” 막습니다.
//run()을 한 번만 호출한다면 runner는 항상 null이고, CAS는 필요 없지 않나
워커 스레드 한 개가 task.run()을 한 번만 실행
runner.compareAndSet(null, Thread.currentThread())는 항상 성공
정상 상황에서는 항상 null → 성공 → runner에 워커 스레드 저장이 맞다.
//하지만 run()을 여러 번 호출할 수도 있다
FutureTask 객체는 동일 인스턴스를 여러 스레드가 동시에 실행할 수도 있다.
잘못된 사용이지만, 클래스는 그 상황도 고려해야 한다.
FutureTask<String> task = new FutureTask<>(() -> "Hello");
Thread t1 = new Thread(task);
Thread t2 = new Thread(task);
t1.start();
t2.start();
둘 다 state == NEW를 통과할 수 있다. (타이밍상 가능)
동시에 runner.compareAndSet(null, Thread.currentThread())로 들어온다.
여기서 CAS가 없으면, 두 스레드 모두 call()을 실행해버릴 수도 있다.
즉, 동일한 FutureTask가 두 번 실행되는데, 이는 허용되지 않는다.
한 스레드만 실행을 계속할 수 있게 보장된다.
```