## 병렬 스트림
- 스트림 객체에 .parallel()만 설정하면 ForkJoinPool.commonPool-worker가 연산을 실행한다.
```java
IntStream.rangeClosed(1, 10)
    .parallel()  // ← 병렬 스트림으로 변환
    .map(HeavyJob::heavyTask)
    .reduce(0, Integer::sum);
// 내부적으로 이렇게 됨
ForkJoinPool commonPool = ForkJoinPool.commonPool();
// commonPool은 JVM당 하나만 존재하는 공용 풀
// 기본 크기: Runtime.getRuntime().availableProcessors() - 1
/* 작업 분할 및 실행 */
// 데이터: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
// 내부적으로 Spliterator가 데이터를 분할
//     [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
//                      ↓ 분할
//     [1, 2, 3, 4, 5] | [6, 7, 8, 9, 10]
//              ↓              ↓
//       [1,2,3] [4,5]  [6,7,8] [9,10]
```
```
// 실행 예상 로그
// CPU 코어가 8개라면, commonPool 크기는 7 (8-1)
// 첫 번째 배치 (7개 스레드가 동시 실행)
14:30:00.001 [ForkJoinPool.commonPool-worker-1] calculate 1 -> 10
14:30:00.001 [ForkJoinPool.commonPool-worker-2] calculate 2 -> 20
14:30:00.001 [ForkJoinPool.commonPool-worker-3] calculate 3 -> 30
14:30:00.001 [ForkJoinPool.commonPool-worker-4] calculate 4 -> 40
14:30:00.001 [ForkJoinPool.commonPool-worker-5] calculate 5 -> 50
14:30:00.001 [ForkJoinPool.commonPool-worker-6] calculate 6 -> 60
14:30:00.001 [ForkJoinPool.commonPool-worker-7] calculate 7 -> 70
// 1초 후 - 두 번째 배치 (3개 남음)
14:30:01.005 [ForkJoinPool.commonPool-worker-1] calculate 8 -> 80
14:30:01.005 [ForkJoinPool.commonPool-worker-2] calculate 9 -> 90
14:30:01.005 [ForkJoinPool.commonPool-worker-3] calculate 10 -> 100
// 총 실행 시간: 약 2초 (순차적이면 10초)
```
- parallelStream()의 내부 동작
```java
// Java 내부 코드 (간략화)
public final IntStream parallel() {
    // 병렬 플래그만 설정
    this.parallel = true;
    return this;
}

// reduce()가 실제 실행을 트리거
public final int reduce(int identity, IntBinaryOperator op) {
    if (isParallel()) {
        // ForkJoinPool.commonPool() 사용
        return evaluateParallel(identity, op);
    } else {
        return evaluateSequential(identity, op);
    }
}

private int evaluateParallel(int identity, IntBinaryOperator op) {
    // 내부적으로 ForkJoinTask 생성
    return new ReduceTask<>(
        spliterator,  // 데이터 분할기
        identity,
        op
    ).invoke();  // commonPool에서 실행
}
```
- ReduceTask (내부 구현 - 개념)
```java
// 실제 내부 구현은 더 복잡하지만, 개념적으로는 이런 식
class ReduceTask extends RecursiveTask<Integer> {
    private Spliterator<Integer> spliterator;
    private int identity;
    private IntBinaryOperator op;
    
    @Override
    protected Integer compute() {
        // THRESHOLD 체크
        if (spliterator.estimateSize() <= THRESHOLD) {
            // 직접 처리
            int result = identity;
            spliterator.forEachRemaining(value -> {
                result = op.applyAsInt(result, heavyTask(value));
            });
            return result;
        }
        
        // 분할
        Spliterator<Integer> left = spliterator.trySplit();
        if (left == null) {
            // 더 이상 분할 불가
            return compute();
        }
        
        // Fork/Join 패턴
        ReduceTask leftTask = new ReduceTask(left, identity, op);
        ReduceTask rightTask = new ReduceTask(spliterator, identity, op);
        
        leftTask.fork();
        int rightResult = rightTask.compute();
        int leftResult = leftTask.join();
        
        return op.applyAsInt(leftResult, rightResult);
    }
}
```
```
// CPU 4코어 시스템 가정 (commonPool 크기: 3)

IntStream.rangeClosed(1, 10).parallel()
    .map(HeavyJob::heavyTask)
    .reduce(0, Integer::sum);

// === 실행 과정 ===

// 메인 스레드: reduce() 호출
ForkJoinPool.commonPool().invoke(new ReduceTask([1~10]))

// [1~10] 분할
  left: [1~5]
  right: [6~10]
  
  leftTask[1~5].fork()  // commonPool에 제출
  rightTask[6~10].compute()  // 현재 스레드에서 실행
  
    // [6~10] 분할
    left: [6~7]
    right: [8~10]
    
    leftTask[6~7].fork()
    rightTask[8~10].compute()
    
      // [8~10] 직접 실행 (THRESHOLD 도달)
      worker-1: heavyTask(8) -> 80
      worker-1: heavyTask(9) -> 90
      worker-1: heavyTask(10) -> 100
      return 270

// 동시에 다른 워커들
worker-2: [1~5] 처리 중
  [1~2].fork()
  [3~5].compute()
    heavyTask(3) -> 30
    heavyTask(4) -> 40
    heavyTask(5) -> 50

worker-3: [1~2] 처리
  heavyTask(1) -> 10
  heavyTask(2) -> 20

worker-4: [6~7] 처리
  heavyTask(6) -> 60
  heavyTask(7) -> 70

// 타이밍 다이어그램
시간 →
0초    1초    2초

메인    ━━━━━━━━━━━ (invoke, 일하면서 대기)
W-1    [8][9][10]━━
W-2    [3][4][5]━━━
W-3    [1][2]━━━━━━
W-4    [6][7]━━━━━━

// 만약 워커가 7개라면
메인    ━━━━━━━━━━━
W-1    [1]━[8]━━━━━
W-2    [2]━[9]━━━━━
W-3    [3]━[10]━━━━
W-4    [4]━━━━━━━━━
W-5    [5]━━━━━━━━━
W-6    [6]━━━━━━━━━
W-7    [7]━━━━━━━━━
```
## 병렬 스트림 주의사항
- commonPool 공유 문제
```java
// 문제 상황
public void method1() {
    // 이 메서드가 commonPool 사용
    list.parallelStream()
        .map(HeavyJob::heavyTask)
        .forEach(System.out::println);
}

public void method2() {
    // 이 메서드도 commonPool 사용
    anotherList.parallelStream()
        .map(HeavyJob::heavyTask2)
        .forEach(System.out::println);
}

// method1()과 method2()가 동시에 실행되면
// 같은 commonPool을 두고 경쟁
```
- 블로킹 작업 주의
```java
// 나쁜 예: commonPool에서 블로킹
IntStream.rangeClosed(1, 10)
    .parallel()
    .map(i -> {
        Thread.sleep(1000);  // commonPool 스레드 블로킹
        return i * 10;
    })
    .sum();

// 다른 parallelStream()도 영향받음
```
- 해결책: parallelStream() 아닌 전용 ForkJoinPool 사용
```
- parallelStream()의 실행 방식:
ForkJoinPool.commonPool() 사용
데이터를 재귀적으로 분할 (Spliterator)
각 워커가 분할된 데이터 처리
결과를 병합 (reduce)
메인 스레드도 일하면서 대기 (invoke)
- parallelStream()의 실행 순서:
순서는 보장되지 않음
병렬로 동시 실행
CPU 코어 수만큼 동시에 처리
```
## Fork/Join 프레임워크는 CPU 바운드 작업에 사용
- I/O 작업처럼 블로킹 시간이 긴 작업을 ForkJoinPool에서 처리하면
    - 스레드 블로킹에 따른 CPU 낭비
    - 컨텍스트 스위칭 오버헤드 증가
    - 작업 훔치기 기법 무력화
    - 분할 정복 이점 감소
- I/O 작업은 Fork/Join 시 공용 풀 대신 별도의 풀 사용이 안전
## 251028-java-adv3/src/parallel/ParallelMain3.java
```java
// requestPool: 100개 스레드
ExecutorService requestPool = Executors.newFixedThreadPool(100);
// commonPool: 3개 스레드만
System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "3");
// 2개의 요청이 동시에 실행
for (int i = 1; i < 3; i++) {  // request-1, request-2
    requestPool.submit(() -> logic(requestName));
}
```
- requestPool 스레드(100개)가 요청을 처리
- 하지만 내부의 parallelStream()은 같은 commonPool(3개)을 공유
- 리소스 경쟁 발생 (스레드 충돌)
```
[requestPool 스레드들]
pool-1-thread-1: request-1 처리 중
pool-1-thread-2: request-2 처리 중
       ↓                    ↓
   parallelStream()    parallelStream()
       ↓                    ↓
    [같은 commonPool을 공유!]
    ┌─────────────────────┐
    │ commonPool (3개)    │
    │ worker-1            │
    │ worker-2            │
    │ worker-3            │
    └─────────────────────┘
         ↕ 경쟁
```
```
시간 →
0초         1초         2초         3초
request-1:
  pool-1-thread-1 ━━━━━━━━━━━━━━━━━━━━
  worker-1        ━[2]━[3]━━━━━━━━━━━━
  worker-2        [wait][wait][wait]━━━  ← request-2 때문에 대기
  worker-3        ━[6]━[5]━━━━━━━━━━━━

request-2:
  pool-1-thread-2 ━━━━━━━━━━━━━━━━━━━━━━━━━
  worker-2        ━[2]━[3]━[1]━━━━━━━━━━━━  ← request-1과 충돌
  (request-1이 worker-1, worker-3 사용 중이라 request-2는 worker-2만 주로 사용)
```
```
// 0초 시점: 두 요청이 commonPool 경쟁
15:46:54.775 [pool-1-thread-1] [request-1] 4 -> 40  ← 외부 스레드
15:46:54.775 [worker-2] [request-2] 2 -> 20         ← request-2가 차지
15:46:54.775 [worker-1] [request-1] 2 -> 20         ← request-1이 차지
15:46:54.775 [pool-1-thread-2] [request-2] 4 -> 40  ← 외부 스레드
15:46:54.775 [worker-3] [request-1] 6 -> 60         ← request-1이 차지

// 문제: request-1이 worker-1, worker-3을 선점
// request-2는 worker-2 하나만 주로 사용 → request-2가 더 느려짐
```
- commonPool 경쟁 (가장 큰 문제)
```java
// request-1의 parallelStream()
IntStream.rangeClosed(1, 6)
    .parallel()  // commonPool 사용
    .map(...)
    
// request-2의 parallelStream()
IntStream.rangeClosed(1, 6)
    .parallel()  // 같은 commonPool 사용
```
- 외부 스레드(pool-1-thread-X)도 일함
```java
// invoke() 동작:
// 외부 스레드(pool-1-thread-1)도 작업에 참여
pool-1-thread-1: [request-1] 4 -> 40
pool-1-thread-2: [request-2] 4 -> 40

// 문제: 외부 스레드가 블로킹되면서 작업도 처리
// → requestPool의 스레드가 오래 점유됨
```
- 예측 불가능한 성능
```
// 실행할 때마다 다른 결과
// 실행 1:
request-1: 2초
request-2: 3초
// 실행 2:
request-1: 3초
request-2: 2초

// 어떤 요청이 commonPool을 많이 차지하느냐에 따라 달라짐
```
- 더 많은 요청이 들어오면
```
// 100개 요청이 동시에 들어온다면
for (int i = 1; i <= 100; i++) {
    requestPool.submit(() -> logic(requestName));
}

// 100개 요청이 commonPool(3개) 공유
request-1: 2초
request-2: 3초
request-3: 4초
request-4: 5초
...
request-100: 200초
```
- 해결책: 각 요청마다 전용 ForkJoinPool 사용
```java
private static void logic(String requestName) {
    long sTime = System.currentTimeMillis();
    
    // 각 요청마다 전용 풀 생성
    ForkJoinPool customPool = new ForkJoinPool(3);
    
    try {
        int sum = customPool.submit(() ->
            IntStream.rangeClosed(1, 6)
                .parallel()
                .map(i -> HeavyJob.heavyTask(i, requestName))
                .reduce(0, Integer::sum)
        ).get();
        
        long eTime = System.currentTimeMillis();
        MyLogger.log("(" + requestName + ") sum=" + sum + 
                     ", duration=" + (eTime - sTime));
    } catch (Exception e) {
        throw new RuntimeException(e);
    } finally {
        customPool.shutdown();
    }
}
```
- 해결책 2: 순차 처리 (데이터가 적으면 병렬 처리 오버헤드가 더 클 수 있음)
- 해결책 3: 직접 Fork/Join 구현해서 int sum = customPool.invoke(task);
- 즉, 멀티 스레드 환경(서버)에서 parallelStream()을 사용하면
- 모든 요청이 같은 commonPool을 공유하므로 성능 문제가 발생할 수 있다.
- 해결: 251028-java-adv3/src/parallel/ParallelMain4.java
## CompletableFuture
- 비동기 프로그래밍을 지원하기 위해 java.util.concurrent 패키지에 포함된 클래스
- 비동기 작업을 실행하고, 조합하고, 처리 결과를 이어받아 처리할 수 있도록 해준다.
- 1. Future 역할 한다.: 비동기 작업의 결과를 나중에 받을 수 있는 객체
- 2. CompletionStage 역할 한다.: 작업이 끝난 후 이어서 다른 작업을 실행할 수 있는 객체
- 비동기 작업의 결과를 다루는 + 그 결과로 후속 작업을 연결할 수 있는 도구
- 1. 비동기 실행하기
```java
// runAsync(): 반환값이 없는 비동기 작업 실행
// supplyAsync(): 결과값을 반환하는 비동기 작업 실행
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    System.out.println("비동기 작업 실행 중...");
});
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    return "결과 데이터";
});
```
- 2. 결과 얻기
```java
// get()은 InterruptedException, ExecutionException을 던짐
// join()은 언체크 예외(CompletionException)로 감싸서 던짐
String result = future.get();      // 블로킹 (작업 끝날 때까지 대기)
String result2 = future.join();    // 블로킹 (예외 처리 다름)
```
- 3. 콜백(후속 작업) 연결하기
```java
// thenApply(): 이전 결과를 받아서 가공 후 반환
// thenAccept(): 결과를 소비하지만 반환하지 않음
// thenRun(): 결과를 사용하지 않고 다른 작업만 실행
CompletableFuture.supplyAsync(() -> {
    return "Hello";
}).thenApply(s -> {
    return s + " World";
}).thenAccept(System.out::println); // Hello World
```
- 4. 여러 비동기 작업 조합하기
```java
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "A");
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "B");
// thenCombine(): 두 CompletableFuture의 결과를 합침
CompletableFuture<String> combined =
    f1.thenCombine(f2, (a, b) -> a + b);
System.out.println(combined.join()); // "AB"
// allOf(): 여러 작업이 모두 끝날 때까지 기다림
// anyOf(): 하나라도 끝나면 완료
CompletableFuture.allOf(f1, f2).join(); // 모든 작업 완료 대기
```
- 5. 예외 처리
```java
CompletableFuture.supplyAsync(() -> {
    if (true) throw new RuntimeException("에러 발생");
    return "OK";
}).exceptionally(ex -> {
    System.out.println("예외 처리: " + ex.getMessage());
    return "기본값";
});
// 또는
.handle((result, ex) -> {
    if (ex != null) return "에러 처리됨";
    return result;
});
```
- 기본적으로 CompletableFuture는 스레드 풀을 사용한다.
```
CompletableFuture의 비동기 작업은 내부적으로 스레드 풀(Thread Pool)에서 실행됩니다.
즉, 새로운 스레드를 직접 만들지 않고, 미리 만들어 둔 스레드 집합 중 하나를 빌려서 작업을 처리하는 방식이에요.
기본적으로 CompletableFuture.runAsync()나 supplyAsync()를 호출하면
아무 Executor(스레드 풀)를 지정하지 않아도 됩니다.
CompletableFuture.runAsync(() -> {
    System.out.println("비동기 작업 실행 중: " + Thread.currentThread().getName());
});
이 코드를 실행하면 출력은 대략 이런 식일 거예요:
비동기 작업 실행 중: ForkJoinPool.commonPool-worker-1
즉, 기본적으로 ForkJoinPool.commonPool()이라는 전역 공유 스레드 풀을 사용합니다.
```
- 별도의 스레드 풀을 지정하라는 의미
```
하지만 모든 비동기 작업이 공용 스레드 풀(commonPool)을 사용하면,
다음과 같은 문제가 생길 수 있어요:
여러 컴포넌트가 동시에 이 풀을 사용하면 경쟁이 발생 (느려짐)
CPU 바운드 작업과 IO 바운드 작업이 섞이면 성능 저하
서버 환경(Spring 등)에서는 공용 풀을 오염시켜 예상치 못한 지연 발생
그래서 작업의 성격에 따라 별도의 스레드 풀을 지정하는 게 좋습니다.
```
- 별도의 스레드 풀 지정 방법
```java
//예제 1: 고정 크기 스레드 풀 사용
ExecutorService executor = Executors.newFixedThreadPool(5);
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    System.out.println("작업 스레드: " + Thread.currentThread().getName());
}, executor); // 작업 스레드: pool-1-thread-1

//예제 2: 반환값이 있는 경우
ExecutorService executor = Executors.newFixedThreadPool(3);
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    return "Hello from " + Thread.currentThread().getName();
}, executor);
System.out.println(future.join());

//예제 3: 후속 작업(thenApply 등)에도 스레드 풀 지정 가능
CompletableFuture.supplyAsync(() -> "Hello", executor)
    .thenApplyAsync(s -> s + " World", executor)
    .thenAcceptAsync(System.out::println, executor);
// thenApplyAsync, thenAcceptAsync 등은 기본적으로 commonPool을 사용하지만,
// Executor를 지정하면 해당 풀에서 이어서 실행
```
- 예를 들어, I/O 작업(API 호출, DB 쿼리)과 CPU 작업(데이터 가공, 계산)을 분리하고 싶을 때
```java
ExecutorService ioExecutor = Executors.newFixedThreadPool(10);
ExecutorService cpuExecutor = Executors.newWorkStealingPool();
CompletableFuture.supplyAsync(() -> {
        // I/O 작업
        return callApi();
    }, ioExecutor)
    .thenApplyAsync(data -> {
        // CPU 집중 작업
        return process(data);
    }, cpuExecutor);
// I/O와 CPU 작업이 서로 다른 풀에서 수행되어 성능이 안정적
```