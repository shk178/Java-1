# 13. 병렬 스트림
- Fork/Join 멀티스레딩 패턴 at 병렬 프로그래밍
- Fork 분할 -> Execute 처리 -> Join 모음
## Fork/Join 프레임워크
- java.util.concurrent 패키지의 일부
- 멀티코어 프로세서 활용 위한 병렬 처리 제공
- Fork/Join은 다음과 같은 경우에 적합:
```
재귀적으로 분할 가능한 작업
CPU 집약적인 계산 작업
배열이나 컬렉션의 대량 데이터 처리
병렬 정렬, 병렬 검색 등
```
- 분할 임계값을 적절히 설정: 너무 작으면 오버헤드가 커지고, 너무 크면 병렬화 이점이 줄어듦
- Stream API의 parallelStream(): 내부적으로 Fork/Join 프레임워크를 사용
- 분할 정복 (Divide and Conquer)
    - Fork(분할): 큰 작업을 작은 하위 작업으로 재귀적으로 분할
    - Join(병합): 분할된 하위 작업들의 결과를 다시 합쳐서 최종 결과를 만듦
- 작업 훔치기 (Work Stealing)
    - 각 스레드는 자신의 작업 큐를 가지고 있다.
    - 작업이 없는 유휴 스레드는 다른 스레드의 큐에서 작업을 훔쳐와서 실행
- ForkJoinPool
    - Fork/Join 프레임워크의 핵심 실행 엔진
    - ExecutorService의 특수한 구현체
```java
// 방법 1: 기본 생성 (CPU 코어 수만큼 스레드 생성)
ForkJoinPool pool = new ForkJoinPool(); // Runtime.getRuntime().availableProcessors() 개수
// 방법 2: 스레드 개수 지정
ForkJoinPool pool = new ForkJoinPool(8);
// 방법 3: 공통 풀 사용 (권장)
ForkJoinPool commonPool = ForkJoinPool.commonPool(); // Java 8 이후, parallelStream()도 이걸 사용함
```
```
ForkJoinPool
│
├─ WorkQueue[] (각 워커 스레드마다 하나씩)
│  │
│  ├─ WorkQueue 0 (Thread 0)
│  │   └─ Deque: [Task A] [Task B] [Task C]
│  │              머리 ←           → 꼬리
│  │
│  ├─ WorkQueue 1 (Thread 1)
│  │   └─ Deque: [Task D] [Task E]
│  │
│  └─ WorkQueue 2 (Thread 2)
│      └─ Deque: []  ← 비어있음 (훔칠 준비)
│
└─ Submission Queue (외부에서 제출된 작업)
    └─ [Initial Task]
```
```java
ForkJoinPool pool = new ForkJoinPool();
// ① invoke: 동기 실행 (결과를 기다림)
Long result = pool.invoke(task);
// ② submit: 비동기 실행 (Future 반환)
ForkJoinTask<Long> future = pool.submit(task);
Long result = future.get(); // 나중에 결과 가져옴
// ③ execute: 비동기 실행 (반환값 없음)
pool.execute(task);
// 상태 확인
int parallelism = pool.getParallelism(); // 병렬 수준
int activeThreads = pool.getActiveThreadCount(); // 활성 스레드 수
long stealCount = pool.getStealCount(); // 훔친 작업 수
boolean isQuiescent = pool.isQuiescent(); // 모든 작업 완료 확인
// 종료
pool.shutdown();
pool.awaitTermination(10, TimeUnit.SECONDS);
```
```java
// Common Pool
// Java 8 이후, 전역 공통 풀 사용 가능
ForkJoinPool commonPool = ForkJoinPool.commonPool();
// parallelStream()도 내부적으로 commonPool 사용
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
int sum = numbers.parallelStream()
        .mapToInt(Integer::intValue)
        .sum(); // ForkJoinPool.commonPool()에서 실행됨
// Common Pool 설정
// # JVM 옵션으로 common pool 크기 조정
// -Djava.util.concurrent.ForkJoinPool.common.parallelism=8
```
```
Common Pool 설정
# JVM 옵션으로 common pool 크기 조정
-Djava.util.concurrent.ForkJoinPool.common.parallelism=8
```
- ForkJoinTask
    - Fork/Join 프레임워크에서 실행되는 작업의 추상 클래스
```
ForkJoinTask<V>  (abstract, implements Future<V>)
    │
    ├─ RecursiveTask<V>  (결과를 반환)
    │   └─ compute() 구현 필요
    │
    ├─ RecursiveAction  (결과 없음, void)
    │   └─ compute() 구현 필요
    │
    └─ CountedCompleter<V>  (완료 콜백 지원)
        └─ compute() + onCompletion() 구현
```
- RecursiveTask (결과 반환)
```java
public class SumTask extends RecursiveTask<Long> {
    private long[] array;
    private int start, end;
    private static final int THRESHOLD = 1000;
  
    public SumTask(long[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
  
    @Override
    protected Long compute() {
        int length = end - start;
    
        // Base case: 직접 계산
        if (length <= THRESHOLD) {
          long sum = 0;
          for (int i = start; i < end; i++) {
            sum += array[i];
          }
          return sum;
        }
    
        // Recursive case: 분할
        int mid = start + length / 2;
        SumTask left = new SumTask(array, start, mid);
        SumTask right = new SumTask(array, mid, end);
    
        left.fork();  // 비동기 실행
        long rightResult = right.compute();  // 현재 스레드에서 실행
        long leftResult = left.join();  // 결과 대기
    
        return leftResult + rightResult;
    }
}
```
- RecursiveAction (결과 없음)
```java
public class SortTask extends RecursiveAction {
    private int[] array;
    private int start, end;
    private static final int THRESHOLD = 1000;
    
    public SortTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected void compute() {
        int length = end - start;
        
        // Base case: 직접 정렬
        if (length <= THRESHOLD) {
            Arrays.sort(array, start, end);
            return;  // void이므로 반환값 없음
        }
        
        // Recursive case
        int mid = start + length / 2;
        SortTask left = new SortTask(array, start, mid);
        SortTask right = new SortTask(array, mid, end);
        
        invokeAll(left, right);  // 둘 다 fork & join
        
        // 병합 (merge)
        merge(start, mid, end);
    }
    
    private void merge(int start, int mid, int end) {
        // 병합 로직...
    }
}
```
- ForkJoinTask의 주요 메서드
```java
// 작업 실행 관련
ForkJoinTask<V> fork()           // 비동기 실행 (큐에 추가)
V join()                         // 결과 대기 (블로킹)
V invoke()                       // 동기 실행 (현재 스레드)
static void invokeAll(Task... tasks)  // 여러 작업 fork & join

// Future 인터페이스 메서드
V get()                          // 결과 가져오기 (InterruptedException 발생 가능)
V get(long timeout, TimeUnit unit)
boolean cancel(boolean mayInterruptIfRunning)
boolean isCancelled()
boolean isDone()

// 예외 처리
boolean isCompletedAbnormally()  // 예외로 종료됐는지 확인
Throwable getException()         // 발생한 예외 가져오기
void completeExceptionally(Throwable ex)  // 예외로 완료 처리
```
- invokeAll 패턴
```java
@Override
protected Long compute() {
    if (length <= THRESHOLD) {
        return directCompute();
    }
    
    SumTask left = new SumTask(array, start, mid);
    SumTask right = new SumTask(array, mid, end);
    
    // 방법 1: fork & join (비대칭)
    left.fork();
    long rightResult = right.compute();
    long leftResult = left.join();
    return leftResult + rightResult;
    
    // 방법 2: invokeAll (대칭, 여러 작업에 유용)
    invokeAll(left, right);
    return left.join() + right.join();
}
```
- 예외 처리
```java
public class RiskyTask extends RecursiveTask<Integer> {
    
    @Override
    protected Integer compute() {
        try {
            if (someCondition) {
                throw new RuntimeException("Error!");
            }
            return 42;
        } catch (Exception e) {
            // 예외를 task에 저장
            completeExceptionally(e);
            return null;
        }
    }
    
    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();
        RiskyTask task = new RiskyTask();
        
        try {
            Integer result = pool.invoke(task);
        } catch (Exception e) {
            // join()에서 예외가 다시 던져짐
            System.err.println("Task failed: " + e.getMessage());
        }
    }
}
```
- CountedCompleter: 완료 카운터와 콜백을 지원하는 고급 Task
```java
public class SearchTask extends CountedCompleter<Integer> {
    private final int[] array;
    private final int start, end;
    private final int target;
    private final AtomicInteger result;
    
    public SearchTask(CountedCompleter<?> parent, 
                      int[] array, int start, int end, 
                      int target, AtomicInteger result) {
        super(parent);
        this.array = array;
        this.start = start;
        this.end = end;
        this.target = target;
        this.result = result;
    }
    
    @Override
    public void compute() {
        int length = end - start;
        
        if (length < THRESHOLD) {
            for (int i = start; i < end; i++) {
                if (array[i] == target) {
                    result.set(i);
                    quietlyCompleteRoot();  // 전체 작업 종료
                    return;
                }
            }
            tryComplete();  // 이 작업 완료
            return;
        }
        
        int mid = start + length / 2;
        addToPendingCount(2);  // 2개의 하위 작업 대기
        new SearchTask(this, array, start, mid, target, result).fork();
        new SearchTask(this, array, mid, end, target, result).fork();
    }
    
    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        // 모든 하위 작업 완료 시 호출
        System.out.println("Search completed");
    }
}
```
## 실전 사용 팁
- 적절한 THRESHOLD 설정
```java
// 너무 작으면: 오버헤드 증가
private static final int THRESHOLD = 10;  // ❌ 너무 잦은 분할

// 너무 크면: 병렬화 효과 감소
private static final int THRESHOLD = 1_000_000;  // ❌ 거의 순차 실행

// 적절한 크기 (경험적으로)
private static final int THRESHOLD = 1000 ~ 10000;  // ✅ 상황에 따라 조정
```
- 작업 불균형 처리
```java
// 나쁜 예: 완전 절반 분할
int mid = start + length / 2;

// 좋은 예: 데이터 특성 고려
int mid = findBestSplitPoint(array, start, end);
```
- 메모리 효율
```java
// 나쁜 예: 배열 복사
SumTask left = new SumTask(Arrays.copyOfRange(array, start, mid));

// 좋은 예: 인덱스만 전달
SumTask left = new SumTask(array, start, mid);  // 원본 공유
```
- Common Pool vs 전용 Pool
```java
// CPU 집약적 작업: Common Pool 사용
ForkJoinPool.commonPool().invoke(task);

// I/O 작업 또는 블로킹 작업: 전용 Pool 사용
ForkJoinPool customPool = new ForkJoinPool(
    100,  // 많은 스레드
    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null,
    true  // asyncMode: FIFO 큐 사용
);
```
- 요약
```
- ForkJoinPool:
Work-Stealing 알고리즘으로 효율적인 작업 분배
Common Pool 사용 권장 (parallelStream과 공유)
CPU 코어 수만큼 워커 스레드 생성
- ForkJoinTask:
RecursiveTask: 결과 반환
RecursiveAction: 결과 없음
CountedCompleter: 완료 콜백 지원
Future 인터페이스 구현 (비동기 처리)
- 재귀적 분할: compute() 안에서 재귀 호출로 임계값까지 쪼갬
- 실행: fork()는 큐에만 넣고, 실제 실행은 워커 스레드가 꺼내갈 때
- 결과 합치기: join()으로 하위 작업 완료를 기다리고 Bottom-Up으로 합침
- 작업 훔치기: 놀고 있는 스레드가 다른 스레드의 덱 머리에서 작업 가져감
- ForkJoinTask는 Future: 비동기 결과를 다루는 Future 인터페이스 구현
```
## 재귀적 분할
```java
protected Long compute() {
    int length = end - start;
    
    // 임계값 체크: 작업이 충분히 작으면 직접 실행
    if (length <= THRESHOLD) {
        return directCompute(); // 더 이상 분할 안 함
    }
    
    // 재귀적 분할
    int mid = start + length / 2;
    SumTask leftTask = new SumTask(array, start, mid);    // 왼쪽 절반
    SumTask rightTask = new SumTask(array, mid, end);     // 오른쪽 절반
    
    leftTask.fork();  // 왼쪽을 큐에 넣음 (아직 실행 X)
    long rightResult = rightTask.compute();  // 오른쪽 재귀 호출 -> 또 분할될 수 있음
    long leftResult = leftTask.join();       // 왼쪽 결과 대기
    
    return leftResult + rightResult;
}
```
- 재귀적 분할: `compute()` 안에서 다시 `compute()`를 호출하면서 작업을 계속 쪼개는 것
```
원본 작업 [0-1000]
    ↓ compute() 호출
분할 → [0-500], [500-1000]
    ↓ 각각 compute() 재귀 호출
분할 → [0-250], [250-500], [500-750], [750-1000]
    ↓ 계속 분할...
임계값 도달 → 직접 실행
```
## 실행 시점
```java
public class SumTask extends RecursiveTask<Long> {
    
    @Override
    protected Long compute() {
        // ... 분할 로직 ...
        
        SumTask leftTask = new SumTask(array, start, mid);
        SumTask rightTask = new SumTask(array, mid, end);
        
        // ① fork() - ForkJoinTask의 메서드
        leftTask.fork();  // leftTask를 비동기로 실행 큐에 넣음
        
        // ② compute() - 현재 스레드에서 직접 실행
        long rightResult = rightTask.compute();
        
        // ③ join() - ForkJoinTask의 메서드
        long leftResult = leftTask.join();  // leftTask의 결과를 기다림
        
        return leftResult + rightResult;
    }
}
/*
- fork():
ForkJoinTask의 인스턴스 메서드
해당 작업을 ForkJoinPool의 작업 큐에 넣어서 다른 스레드가 실행하도록 함
즉시 반환 (비동기)
- join():
ForkJoinTask의 인스턴스 메서드
fork()로 실행한 작업이 완료될 때까지 대기
작업의 결과를 반환 (블로킹)
- compute():
실제 작업 로직이 들어가는 메서드
현재 스레드에서 동기적으로 실행
 */
// 잘못된 패턴 (비효율적)
leftTask.fork();
rightTask.fork();
long leftResult = leftTask.join();
long rightResult = rightTask.join();
// 올바른 패턴 (효율적)
leftTask.fork();                      // ① 큐에만 넣음 (비동기 실행)
long rightResult = rightTask.compute(); // ② 즉시 실행 (현재 스레드)
long leftResult = leftTask.join();    // ③ 이때 leftTask 실행됨
/*
- fork() 시점:
작업을 큐에만 넣습니다
실제 실행은 ForkJoinPool의 워커 스레드가 큐에서 꺼내갈 때 됩니다
- compute() 시점:
즉시 실행됩니다 (현재 스레드에서)
- join() 시점:
fork()한 작업이 아직 실행 안 됐으면, 이때 실행됩니다
이미 실행 중이면 결과를 기다립니다
 */
```
## 결과 합치기 (Join)
```java
// 리프 노드 (최하단)
if (length <= THRESHOLD) {
    return 100;  // 직접 계산한 결과
}

// 중간 노드
leftTask.fork();
long rightResult = rightTask.compute();  // 50 반환
long leftResult = leftTask.join();       // 50 반환

return leftResult + rightResult;  // 100 반환 (상위로 전달)
```
- Join은 Bottom-Up 방식으로 진행
```
레벨 3 (리프):  [10] [15] [20] [25]  ← 직접 계산
                 ↓    ↓    ↓    ↓
레벨 2:         [25]     [45]         ← join으로 합침
                 ↓         ↓
레벨 1:            [70]               ← 최종 결과
```
## Work-Stealing (작업 훔치기)
- ForkJoinPool의 각 워커 스레드는 자신의 Deque(덱) 가짐
```
// 스레드 1은 꼬리에서 가져감 (LIFO - 최근 작업)
Task A: 
  leftTask.fork();   // → 스레드 1의 덱 꼬리에 추가
  rightTask.compute(); // → 스레드 1이 계속 실행
워커 스레드 1의 덱:  [Task A] [Task B] [Task C]  ← 머리
                                           ↑
                                        꼬리 (LIFO로 꺼냄)
워커 스레드 2의 덱:  []  ← 비어있음
// 스레드 2는 머리에서 훔쳐감 (FIFO - 오래된 작업)
[스레드 1] 덱: [Task L1] [Task L2] [Task L3]
               머리 ←                   → 꼬리
                ↑
                스레드 2가 머리에서 훔쳐감
[스레드 2] 덱: []
               → Task L1을 가져와서 실행
// 충돌 최소화
```
## ForkJoinPool과 Future
- ForkJoinTask는 Future를 구현
```java
public abstract class ForkJoinTask<V> implements Future<V> {
    // Future 메서드들
    V get()
    V get(long timeout, TimeUnit unit)
    boolean cancel(boolean mayInterruptIfRunning)
    boolean isDone()
    
    // ForkJoin 전용 메서드들
    ForkJoinTask<V> fork()
    V join()
    // ...
}

ForkJoinPool pool = new ForkJoinPool();  // 스레드풀 생성

// 방법 1: invoke (동기)
Long result = pool.invoke(task);  // 결과를 기다림

// 방법 2: submit (비동기)
ForkJoinTask<Long> future = pool.submit(task);
// ... 다른 작업 ...
Long result = future.get();  // Future처럼 사용
```
## 전체 흐름 예시
```java
// 배열 합 계산: [1,2,3,4,5,6,7,8] (THRESHOLD = 2)

ForkJoinPool pool = new ForkJoinPool(4);  // 4개 워커 스레드
SumTask mainTask = new SumTask(array, 0, 8);
pool.invoke(mainTask);

// ===== 실행 과정 =====

// [워커1] mainTask.compute()
[0-8] 분할 → leftTask[0-4].fork(), rightTask[4-8].compute()
    
// [워커1] rightTask[4-8].compute()
[4-8] 분할 → leftTask[4-6].fork(), rightTask[6-8].compute()

// [워커1] rightTask[6-8].compute()  
[6-8] 직접 계산 → 15 반환

// [워커2] mainTask의 leftTask[0-4]를 훔쳐감
[0-4] 분할 → leftTask[0-2].fork(), rightTask[2-4].compute()

// [워커3] leftTask[0-2]를 훔쳐감
[0-2] 직접 계산 → 3 반환

// [워커4] leftTask[4-6]를 훔쳐감  
[4-6] 직접 계산 → 11 반환

// ===== Join 과정 (Bottom-Up) =====

// [워커1] 
rightTask[6-8] = 15
leftTask[4-6].join() = 11
[4-8] 반환: 26

// [워커2]
rightTask[2-4] = 7  
leftTask[0-2].join() = 3
[0-4] 반환: 10

// [워커1]
rightTask[4-8] = 26
leftTask[0-4].join() = 10
최종 결과: 36
```
## fork 단계와 join 단계의 실행 순서가 정반대
- 251028-java-adv3/src/parallel/SumTask.java
### 1단계: Fork (분할) - 큰 것 → 작은 것
```
13:50:48.012 [worker-1] leftTask=0~50, rightTask=50~100      // 100개
13:50:48.014 [worker-1] leftTask=50~75, rightTask=75~100     // 50개
13:50:48.014 [worker-1] leftTask=75~87, rightTask=87~100     // 25개
13:50:48.014 [worker-1] leftTask=87~93, rightTask=93~100     // 13개
```
- 왜 큰 것부터?
- worker-1이 계속 rightTask.compute()를 실행하면서 재귀적으로 들어가기 때문
- leftTask는 fork()로 큐에만 넣고, rightTask는 직접 실행
- 따라서 100 → 50 → 25 → 13 순으로 계속 분할
### 2단계: Join (병합) - 작은 것 → 큰 것
```
13:50:48.025 [worker-8] leftTask=0~6, rightTask=6~12 (6)     // 6개 완료
13:50:48.025 [worker-6] leftTask=12~18, rightTask=18~25 (7)  // 7개 완료
13:50:48.026 [worker-6] leftTask=0~12, rightTask=12~25 (13)  // 13개 완료
13:50:48.028 [worker-2] leftTask=0~25, rightTask=25~50 (25)  // 25개 완료
13:50:48.028 [worker-1] leftTask=0~50, rightTask=50~100 (50) // 50개 완료
```
- 왜 작은 것부터?
- 리프 노드(가장 작은 작업)부터 계산이 완료됨
- join()은 하위 작업이 완료되어야 반환됨
- Bottom-Up 방식으로 작은 결과들이 합쳐지면서 큰 결과로
```java
// 코드 실행 순서
pool.invoke(task)
    ↓
[0-100].compute()  // ← msg1 출력: "leftTask=0~50, rightTask=50~100"
    ↓
    leftTask[0-50].fork()  // 큐에 넣음
    rightTask[50-100].compute()  // ← msg1 출력: "leftTask=50~75..."
        ↓
        leftTask[50-75].fork()
        rightTask[75-100].compute()  // ← msg1 출력: "leftTask=75~87..."
            ↓
            leftTask[75-87].fork()
            rightTask[87-100].compute()  // ← msg1 출력: "leftTask=87~93..."
                ↓
                leftTask[87-93].fork()
                rightTask[93-100].compute()  // THRESHOLD 도달
                    ↓
                    직접 계산 → 7 반환
                ↓
                // ← msg2 출력: "... (7)"
                leftResult[87-93].join() → 6
                return 6 + 7 = 13
            ↓
            // ← msg2 출력: "... (13)"
            leftResult[75-87].join() → 6
            return 6 + 13 = 19
        ↓
        // ← msg2 출력: "... (19)"
        leftResult[50-75].join() → 25
        return 25 + 19 = 44
    ↓
    // ← msg2 출력: "... (44)"
    leftResult[0-50].join() → 50
    return 50 + 44 = 94
```
### return이 실행되어야 출력됨은 상위 호출자 입장
```
시간 →
compute[0-100] 시작
│ msg1 출력: "0~50, 50~100"
│ fork[0-50]
│ compute[50-100] 호출 ─────┐
│                           │
│                    compute[50-100] 시작
│                    │ msg1 출력: "50~75, 75~100"
│                    │ fork[50-75]
│                    │ compute[75-100] 호출 ─────┐
│                    │                           │
│                    │                    compute[75-100] 시작
│                    │                    │ ... 재귀 ...
│                    │                    │ compute[93-100] 시작
│                    │                    │ THRESHOLD 도달
│                    │                    │ return 7 ───────┐
│                    │                    │                 │
│                    │                    │ ◄───────────────┘
│                    │                    │ msg2 출력: "...(7)"  ← 첫 msg2
│                    │                    │ join() → 6
│                    │                    │ return 13 ──────┐
│                    │                    │                 │
│                    │ ◄────────────────────────────────────┘
│                    │ msg2 출력: "...(13)"  ← 두 번째 msg2
│                    │ join() → 12
│                    │ return 25 ──────────┐
│                    │                     │
│ ◄──────────────────────────────────────┘
│ msg2 출력: "...(25)"  ← 마지막 msg2
│ join() → 50
│ return 75
```
```java
@Override
protected Long compute() {
    int length = end - start;
    
    // 이 부분이 재귀의 "탈출 조건" (Base Case): 없으면 StackOverflowError 발생
    if (length <= THRESHOLD) {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += array[i];
        }
        return sum;  // ← 여기서 처음으로 실제 return
    }
    
    // 재귀 호출 (Recursive Case)
    int mid = start + length / 2;
    SumTask leftTask = new SumTask(array, start, mid);
    SumTask rightTask = new SumTask(array, mid, end);
    
    leftTask.fork();
    long rightResult = rightTask.compute();  // ← 결국 위의 return에 도달
    String msg2 = "... (" + rightResult + ")";
    MyLogger.log(msg2);
    
    long leftResult = leftTask.join();
    return leftResult + rightResult;
}
```
### THRESHOLD - 배열 크기와 분할 방식
```
- THRESHOLD = 10 (짝수)
[0-100] → [0-50], [50-100]
  [50-100] → [50-75], [75-100]
    [75-100] → [75-87], [87-100]
      [87-100] → [87-93], [93-100]
        [93-100] → length=7 ≤ 10 - 직접 계산
        [87-93] → length=6 ≤ 10 - 직접 계산
- THRESHOLD = 11 (홀수)
[0-100] → [0-50], [50-100]
  [50-100] → [50-75], [75-100]
    [75-100] → [75-87], [87-100]
      [87-100] → [87-93], [93-100]
        [93-100] → length=7 ≤ 11 - 직접 계산
        [87-93] → length=6 ≤ 11 - 직접 계산
- 배열 크기가 20일 때 THRESHOLD = 10
[0-20] → length=20 > 10, 분할
  [0-10], [10-20]
    [0-10] → length=10 ≤ 10 - 직접 계산 (딱 맞음)
    [10-20] → length=10 ≤ 10 - 직접 계산 (딱 맞음)
- 배열 크기가 20일 때 THRESHOLD = 11
[0-20] → length=20 > 11, 분할
  [0-10], [10-20]
    [0-10] → length=10 ≤ 11 - 직접 계산
    [10-20] → length=10 ≤ 11 - 직접 계산
- 차이는 딱 떨어지는가에 있다.
- 배열 크기 32, THRESHOLD = 16 (짝수 - 2의 거듭제곱)
[0-32] → [0-16], [16-32]
  [0-16] → length=16 ≤ 16 - 직접 계산
  [16-32] → length=16 ≤ 16 - 직접 계산
분할 횟수: 1회
최종 작업 개수: 2개 (완벽하게 분할)
- 배열 크기 32, THRESHOLD = 15 (홀수)
[0-32] → [0-16], [16-32]
  [0-16] → [0-8], [8-16]
    [0-8] → length=8 ≤ 15
    [8-16] → length=8 ≤ 15
  [16-32] → [16-24], [24-32]
    [16-24] → length=8 ≤ 15
    [24-32] → length=8 ≤ 15
분할 횟수: 2회
최종 작업 개수: 4개
- 2의 거듭제곱과의 관계가 중요하다.
THRESHOLD = 1024  // 2^10 - 완벽한 이진 분할
THRESHOLD = 1000  // 조금 어색함
THRESHOLD = 512   // 2^9 - 완벽한 이진 분할
- 배열 크기와의 관계가 중요하다.
// 배열 크기가 2의 거듭제곱이면 정확히 16개(2^4)로 분할됨
array.length = 1024
THRESHOLD = 64 (2^6)
// 배열 크기가 불규칙하면 불규칙하게 분할됨
array.length = 1000
THRESHOLD = 100
- 실무에서는 분할 횟수보다 적절한 작업 크기(1000~10000 정도)가 더 중요
```