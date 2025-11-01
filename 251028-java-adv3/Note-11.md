## 공용 풀
- `ForkJoinPool commonPool = ForkJoinPool.commonPool();`
- 자바 프로그램 내에서 단일 인스턴스로 공유되어 사용된다.
- 별도로 생성하지 않아도 ForkJoinPool.commonPool()로 접근할 수 있다.
- RecursiveTask/RecursiveAction.invoke 실행하면 이 풀이 사용된다.
- 병렬 스트림이 내부적으로 이 풀을 사용한다.
- 별도 풀을 만드는 대신 공용 풀을 사용하면 시스템 자원 관리가 편리하다.
- (가용프로세서 - 1)개 스레드로 parallelism이 설정된다.
- 공용 풀은 JVM이 관리하므로 명시적 종료 안 해도 된다.
### 메인 스레드
- 메인 스레드가 그냥 대기만
```java
public static void main(String[] args) {
    ForkJoinPool pool = new ForkJoinPool(4);  // 워커 4개
    SumTask task = new SumTask(data);
    // 만약 메인 스레드가 그냥 대기만 한다면?
    pool.submit(task);
    // 메인 스레드: "워커들아 나는 여기서 기다릴게" (블로킹됨)
    Integer result = task.get();  // 결과 대기
}
// CPU 코어가 5개 이상이면 메인 스레드가 사용하는 코어가 낭비됨
```
- 메인 스레드도 일을 도움
```java
public static void main(String[] args) {
    ForkJoinPool pool = new ForkJoinPool(4);
    SumTask task = new SumTask(data);
    // invoke()를 사용하면
    Integer result = pool.invoke(task);  
    // 메인 스레드: "나도 같이 일할게"
}
```
- invoke()의 내부 동작
```java
pool.invoke(task);
// 내부적으로:
if (Thread.currentThread() instanceof ForkJoinWorkerThread) {
    // 이미 워커 스레드면 그냥 실행
    return task.invoke();
} else {
    // 외부 스레드(메인)면
    // 1. task를 pool에 제출
    pool.externalPush(task);
    // 2. 메인 스레드가 직접 작업 도움
    return pool.awaitJoin(task);  // 기다리면서 다른 작업도 처리
}

/* awaitJoin()이 하는 일 */
// 메인 스레드의 동작
while (!task.isDone()) {
    // 방법 1: submission queue에서 작업 가져오기
    if (submissionQueue에 작업이 있으면) {
        Task t = submissionQueue.poll();
        t.exec();  // 메인 스레드가 직접 실행
    }
    // 방법 2: 워커의 덱에서 작업 훔치기
    else if (워커들 덱에 작업이 있으면) {
        Task t = stealFromWorker();
        t.exec();  // 메인 스레드가 실행
    }
    // 방법 3: 잠깐 대기
    else {
        park();  // 짧게 대기
    }
}
return task.getRawResult();
```
- submit() - Future 반환
```java
ForkJoinPool pool = new ForkJoinPool();
SumTask task = new SumTask(data);

// submit()은 Future를 반환
ForkJoinTask<Integer> future = pool.submit(task);
// 또는
Future<Integer> future = pool.submit(task);

// 메인 스레드는 다른 일 할 수 있음
System.out.println("다른 작업 중...");
doSomethingElse();

// 나중에 결과 가져오기
Integer result = future.get();  // 블로킹
```
- invoke() - 직접 결과 반환 (ForkJoinTask 자체가 Future)
```java
ForkJoinPool pool = new ForkJoinPool();
SumTask task = new SumTask(data);

// 방법 A: invoke() - 동기적으로 결과 바로 반환
Integer result = pool.invoke(task);  // 메인이 도와서 일함
// 이미 완료되어서 result에 값이 들어있음

// 방법 B: task 자체를 Future로 사용
pool.execute(task);  // 또는 task.fork()
// task는 Future이므로
Integer result = task.get();  // Future 인터페이스 사용 가능
```
- 패턴 1: submit()으로 Future 받기
```java
ForkJoinPool pool = new ForkJoinPool();

List<SumTask> tasks = Arrays.asList(
    new SumTask(data1),
    new SumTask(data2),
    new SumTask(data3)
);

// 모두 submit
List<Future<Integer>> futures = tasks.stream()
    .map(pool::submit)
    .collect(Collectors.toList());

// 다른 작업...
System.out.println("작업 제출 완료, 다른 일 진행...");

// 나중에 모든 결과 수집
List<Integer> results = futures.stream()
    .map(f -> {
        try {
            return f.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    })
    .collect(Collectors.toList());
```
- 패턴 2: fork()로 task를 Future처럼 사용
```java
SumTask task1 = new SumTask(data1);
SumTask task2 = new SumTask(data2);

// fork로 실행
task1.fork();
task2.fork();

// 다른 작업...
doSomething();

// task 자체가 Future
Integer result1 = task1.join();  // 또는 task1.get()
Integer result2 = task2.join();  // 또는 task2.get()
```
- 패턴 3: invoke()는 즉시 결과 (Future 불필요)
```java
// 바로 결과가 필요한 경우
Integer result = pool.invoke(new SumTask(data));
// 이미 완료됨, Future 필요 없음
// 비동기로 실행하고 나중에 결과 받고 싶다 → submit() 또는 fork()
// 동기적으로 결과 바로 받고 싶다 → invoke()
```
- join() vs get() 차이
```java
SumTask task = new SumTask(data);
task.fork();

// 방법 1: join() - ForkJoinTask 메서드
Integer result = task.join();
// - Unchecked exception 발생 (RuntimeException)
// - ForkJoinTask에 최적화됨

// 방법 2: get() - Future 인터페이스 메서드
try {
    Integer result = task.get();
    // - Checked exception 발생 (InterruptedException, ExecutionException)
    // - 표준 Future 인터페이스
} catch (InterruptedException | ExecutionException e) {
    e.printStackTrace();
}
```
- Future.get() - Checked Exception
```java
// Future 인터페이스 (Java 5부터)
public interface Future<V> {
    V get() throws InterruptedException, ExecutionException;
    V get(long timeout, TimeUnit unit) 
        throws InterruptedException, ExecutionException, TimeoutException;
}
// 일반적인 ExecutorService 사용
ExecutorService executor = Executors.newFixedThreadPool(4);
Future<Integer> future = executor.submit(() -> {
    // 여기서 예외 발생 가능
    if (someCondition) {
        throw new RuntimeException("Error");
    }
    return 42;
});

try {
    Integer result = future.get();  // 반드시 예외 처리 필요
} catch (InterruptedException e) {
    // 스레드가 중단됨 - 처리 필요
    e.printStackTrace();
} catch (ExecutionException e) {
    // 작업 중 예외 발생 - 처리 필요
    Throwable cause = e.getCause();
    cause.printStackTrace();
}
```
```
ExecutorService는 범용 프레임워크 - 다양한 상황에서 사용됨
작업이 실패할 수 있음 - 네트워크, I/O, 비즈니스 로직 등
중단 가능성 - Thread.interrupt() 호출 가능
명시적 처리 강제 - 개발자가 반드시 예외를 인지하고 처리하도록
```
- ForkJoinTask.join() - Unchecked Exception
```java
// ForkJoinTask (Java 7부터)
public abstract class ForkJoinTask<V> implements Future<V> {
    public final V join() {
        // 내부에서 예외를 잡아서 RuntimeException으로 변환
        int s;
        if ((s = doJoin() & DONE_MASK) != NORMAL)
            reportException(s);
        return getRawResult();
    }
    
    private void reportException(int s) {
        // Unchecked exception으로 던짐
        throw new RuntimeException(getThrowableException());
    }
}
// Fork/Join 프레임워크의 일반적인 사용
protected Long compute() {
    if (length <= THRESHOLD) {
        return directCompute();
    }

    SumTask left = new SumTask(array, start, mid);
    SumTask right = new SumTask(array, mid, end);

    left.fork();
    long rightResult = right.compute();
    long leftResult = left.join();  // try-catch 불필요

    return leftResult + rightResult;
}
```
```
CPU 집약적 작업 중심 - I/O나 중단이 거의 없음
재귀 알고리즘 - 매번 try-catch하면 코드가 지저분해짐
빠른 실패(Fail-fast) - 예외가 발생하면 빠르게 전파
일관성 - compute() 내부에서 간결한 코드 작성 가능
```
- Future.get() - 복잡하지만 안전
```java
public Integer processData() {
    ExecutorService executor = Executors.newFixedThreadPool(4);
    
    Future<Integer> future = executor.submit(() -> {
        // I/O 작업, 네트워크 호출 등
        return fetchDataFromNetwork();
    });
    
    try {
        return future.get(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
        // 스레드 중단 처리
        Thread.currentThread().interrupt();
        return -1;
    } catch (ExecutionException e) {
        // 작업 실패 처리
        logger.error("Failed", e.getCause());
        return -1;
    } catch (TimeoutException e) {
        // 타임아웃 처리
        future.cancel(true);
        return -1;
    }
}
```
- ForkJoinTask.join() - 간결
```java
protected Long compute() {
    if (length <= THRESHOLD) {
        return directCompute();
    }
    
    SumTask left = new SumTask(array, start, mid);
    SumTask right = new SumTask(array, mid, end);
    
    left.fork();
    long rightResult = right.compute();
    long leftResult = left.join();  // 간결
    
    return leftResult + rightResult;
}
// 예외가 발생하면 자동으로 상위로 전파됨
```
- join()에서 예외 처리가 필요하다면
```java
// 방법 1: join() 사용 - Unchecked
protected Long compute() {
    try {
        left.fork();
        long rightResult = right.compute();
        long leftResult = left.join();  // RuntimeException 발생 가능
        return leftResult + rightResult;
    } catch (RuntimeException e) {
        // 선택적으로 처리
        logger.error("Task failed", e);
        throw e;  // 또는 기본값 반환
    }
}

// 방법 2: get() 사용 - Checked (명시적 처리)
protected Long compute() {
    try {
        left.fork();
        long rightResult = right.compute();
        long leftResult = left.get();  // Checked exception
        return leftResult + rightResult;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
    } catch (ExecutionException e) {
        throw new RuntimeException(e.getCause());
    }
}
```
- ForkJoinTask가 Future.get()도 구현하는 이유
```java
public abstract class ForkJoinTask<V> implements Future<V> {
    // get(): Future 인터페이스 호환성 (다른 코드와 통합)
    public final V get() throws InterruptedException, ExecutionException {
        int s = (Thread.currentThread() instanceof ForkJoinWorkerThread) ?
            doJoin() : externalInterruptibleAwait();
        Throwable ex;
        if ((s &= DONE_MASK) == CANCELLED)
            throw new CancellationException();
        if (s == EXCEPTIONAL && (ex = getThrowableException()) != null)
            throw new ExecutionException(ex);
        return getRawResult();
    }
    
    // join(): Fork/Join 프레임워크 내부 편의성
    public final V join() {
        int s;
        if ((s = doJoin() & DONE_MASK) != NORMAL)
            reportException(s);
        return getRawResult();
    }
}
```
- Fork/Join 내부에서는 join()을 쓰면 코드가 깔끔하고, 외부에서는 get()으로 명시적 처리를 하도록 하는 것
```java
// Fork/Join 프레임워크 내부 - join() 사용
@Override
protected Long compute() {
    left.fork();
    long rightResult = right.compute();
    long leftResult = left.join();  // 간결
    return leftResult + rightResult;
}

// 외부에서 ForkJoinTask 사용 - get() 사용
public void externalMethod() {
    ForkJoinPool pool = new ForkJoinPool();
    SumTask task = new SumTask(data);
    
    Future<Long> future = pool.submit(task);
    
    try {
        Long result = future.get();  // 명시적 예외 처리
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
        logger.error("Task failed", e.getCause());
    }
}

// 또는 invoke()로 간단하게
public void externalMethod2() {
    ForkJoinPool pool = new ForkJoinPool();
    Long result = pool.invoke(new SumTask(data));  // 더 간단
}
```

| 구분 | get() | join() |
|----|-------|--------|
| 예외 타입 | Checked | Unchecked |
| 용도 | 범용 비동기 작업 | Fork/Join 내부 |
| 주요 상황 | I/O, 네트워크, 일반 작업 | CPU 집약적, 분할정복 |
| 코드 스타일 | 명시적 예외 처리 | 간결한 재귀 코드 |
| 철학 | 안전성 우선 | 편의성 우선 |
