- 251028-java-adv3/src/parallel/ParallelMain.java
```java
public class ParallelMain {
    static class SumTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 3;
        private final List<Integer> list;
        public SumTask(List<Integer> list) {
            this.list = list;
        }
        @Override
        protected Integer compute() {
            if (list.size() <= THRESHOLD) {
                int sum = list.stream()
                        .mapToInt(HeavyJob::heavyTask)
                        .sum();
                MyLogger.log("sum=" + sum + "(" + list + ")");
                return sum;
            }
            int mid = list.size() / 2;
            List<Integer> leftList = list.subList(0, mid);
            List<Integer> rightList = list.subList(mid, list.size());
            SumTask leftTask = new SumTask(leftList);
            SumTask rightTask = new SumTask(rightList);
            leftTask.fork();
            int rightResult = rightTask.compute();
            String msg = "leftTask=" + list.subList(0, mid) + ", rightTask=" + list.subList(mid, list.size()) + " (" + rightResult + ")";
            MyLogger.log(msg);
            int leftResult = leftTask.join();
            int joinSum = leftResult + rightResult;
            return joinSum;
        }
    }
    public static void main(String[] args) {
        List<Integer> data = IntStream.rangeClosed(1, 8)
                .boxed()
                .toList();
        long sTime = System.currentTimeMillis();
        ForkJoinPool fjPool = new ForkJoinPool();
        SumTask task = new SumTask(data);
        int result = fjPool.invoke(task);
        long eTime = System.currentTimeMillis();
        System.out.println("합계=" + result + ", 시간=" + (eTime - sTime));
    }
}
/*
14:28:23.637 [ForkJoinPool-1-worker-2] calculate 3 -> 30
14:28:23.637 [ForkJoinPool-1-worker-3] calculate 5 -> 50
14:28:23.637 [ForkJoinPool-1-worker-4] calculate 1 -> 10
14:28:23.637 [ForkJoinPool-1-worker-1] calculate 7 -> 70
14:28:24.643 [ForkJoinPool-1-worker-3] calculate 6 -> 60
14:28:24.643 [ForkJoinPool-1-worker-4] calculate 2 -> 20
14:28:24.643 [ForkJoinPool-1-worker-1] calculate 8 -> 80
14:28:24.643 [ForkJoinPool-1-worker-2] calculate 4 -> 40
14:28:25.657 [ForkJoinPool-1-worker-2] sum=70([3, 4])
14:28:25.657 [ForkJoinPool-1-worker-4] sum=30([1, 2])
14:28:25.657 [ForkJoinPool-1-worker-3] sum=110([5, 6])
14:28:25.657 [ForkJoinPool-1-worker-1] sum=150([7, 8])
14:28:25.663 [ForkJoinPool-1-worker-2] leftTask=[1, 2], rightTask=[3, 4] (70)
14:28:25.663 [ForkJoinPool-1-worker-1] leftTask=[5, 6], rightTask=[7, 8] (150)
14:28:25.663 [ForkJoinPool-1-worker-1] leftTask=[1, 2, 3, 4], rightTask=[5, 6, 7, 8] (260)
합계=360, 시간=2053
 */
```
- base case에서 출력해봐야 된다.
- worker2 - 3, 4 / worker4 - 1, 2 / worker3 - 5, 6 / worker1 - 7, 8 실행
- 1단계: 초기 분할 (main 스레드 또는 worker)
```java
main()
  → fjPool.invoke(task)  // [1,2,3,4,5,6,7,8]
    → [1,2,3,4,5,6,7,8].compute()
      
      mid = 4
      leftTask = [1,2,3,4]
      rightTask = [5,6,7,8]
      
      leftTask.fork()  // ← [1,2,3,4]를 큐에 넣음
      rightTask.compute()  // ← [5,6,7,8]를 직접 실행
```
- 2단계: rightTask [5,6,7,8] 실행
```java
// 어떤 워커가 이걸 실행 (예: worker-X)
[5,6,7,8].compute()
  
  mid = 2
  leftTask = [5,6]
  rightTask = [7,8]
  
  leftTask.fork()  // ← [5,6]을 큐에 넣음
  rightTask.compute()  // ← [7,8]을 직접 실행
```
- 3단계: rightTask [7,8] 실행
```java
// 같은 워커가 계속 실행 (worker-X)
[7,8].compute()
  
  size = 2 ≤ THRESHOLD(3)
  
  // heavyTask 실행
  7 -> 70  // worker-1이 실행 (로그에서 확인)
  8 -> 80  // worker-1이 실행
  
  sum = 150
  return 150
```
- 왜 worker-1이 7, 8을 실행했나?
- rightTask.compute()는 현재 스레드에서 직접 실행되기 때문
```
[초기 상태]
main 스레드가 invoke() 호출
  → ForkJoinPool이 task를 받아서 worker에게 할당
[worker-1이 초기 task를 받음]
worker-1: [1,2,3,4,5,6,7,8].compute() 시작
  leftTask[1,2,3,4].fork()  → worker-1의 덱에 추가
  rightTask[5,6,7,8].compute()  → worker-1이 직접 실행 계속
[worker-1이 계속 실행]
worker-1: [5,6,7,8].compute()
  leftTask[5,6].fork()  → worker-1의 덱에 추가
  rightTask[7,8].compute()  → worker-1이 직접 실행 계속
[worker-1이 계속 실행]
worker-1: [7,8].compute()
  size=2 ≤ 3
  heavyTask(7) → 70  ← worker-1이 실행
  heavyTask(8) → 80  ← worker-1이 실행
  return 150
```
```
[초기]
worker-1 덱: []
worker-2 덱: []
worker-3 덱: []
worker-4 덱: []
[worker-1이 [1~8] 실행]
worker-1 덱: [[1,2,3,4]]  ← fork됨
worker-1 → [5,6,7,8].compute() 실행 중
[worker-1이 [5~8] 실행]
worker-1 덱: [[1,2,3,4], [5,6]]  ← fork됨
worker-1 → [7,8].compute() 실행 중
[worker-1이 [7,8] 실행 - THRESHOLD 도달]
worker-1 → heavyTask(7) 실행
worker-1 → heavyTask(8) 실행
[동시에 다른 워커들이 훔쳐감]
worker-2 덱: [] → [[1,2,3,4]] 훔쳐옴
  → [1,2,3,4].compute()
    → [1,2].fork(), [3,4].compute()
      → heavyTask(3), heavyTask(4)
worker-3 덱: [] → [[5,6]] 훔쳐옴
  → [5,6].compute()
    → heavyTask(5), heavyTask(6)
worker-4 덱: [] → [[1,2]] 훔쳐옴
  → [1,2].compute()
    → heavyTask(1), heavyTask(2)
```
## fork()의 동작 방식
- 기본 동작 (ForkJoinPool의 워커 스레드에서 실행)
```
// worker-1이 실행 중
leftTask.fork();  
// ↓
// worker-1의 덱 꼬리에 추가됨
worker-1 덱: [... , leftTask] ← 꼬리
             머리 →        ← 꼬리
즉, 작업은 fork()한 워커의 덱에 들어간다.
```
- 다른 워커의 Work-Stealing
```
// worker-2가 할 일이 없으면
worker-2: 내 덱이 비었네 다른 워커 덱을 확인해보자
  → worker-1 덱 확인
  → worker-1 덱: [TaskA, TaskB, leftTask]
                  머리 ↑
  → 머리에서 TaskA를 훔쳐감
worker-1 덱: [TaskB, leftTask]  // TaskA는 worker-2가 가져감
worker-2가 TaskA 실행 시작
```
- fork()한 워커의 관점 (LIFO - Last In First Out)
```
worker-1 덱: [TaskA, TaskB, TaskC]
              머리 →        ← 꼬리
// worker-1이 자기 덱에서 작업을 가져올 때
worker-1: 꼬리에서 가져감 (LIFO)
  → TaskC 실행 (가장 최근에 fork한 것)
```
- 훔치는 워커의 관점 (FIFO - First In First Out)
```
worker-1 덱: [TaskA, TaskB, TaskC]
              머리 ↑        ← 꼬리
// worker-2가 훔쳐갈 때
worker-2: 머리에서 가져감 (FIFO)
  → TaskA 실행 (가장 오래된 것)
```
- 충돌 최소화: worker-1은 꼬리, worker-2는 머리에서 가져가니까 겹칠 일이 적음
- 캐시 효율성: worker-1은 최근 데이터(TaskC)를 다루므로 캐시에 있을 가능성 높음
- 예외 케이스: 외부 스레드에서 submit()
```
// main 스레드 (ForkJoinPool 워커가 아님)
ForkJoinPool pool = new ForkJoinPool();
SumTask task = new SumTask(data);

// 방법 1: invoke (동기)
pool.invoke(task);
// → task를 submission queue에 넣음
// → 워커 중 하나가 가져가서 실행

// 방법 2: submit (비동기)
Future<Integer> future = pool.submit(task);
// → submission queue에 넣음

방법 2처럼 외부에서 제출하면:
Submission Queue: [task] ← 여기 들어감 (워커의 덱이 아님)
  ↓
워커들이 자기 덱이 비면 submission queue에서 가져감
  ↓
worker-1이 task 가져감
  ↓
worker-1 덱: [task]
```