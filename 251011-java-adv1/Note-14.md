# 13. 스레드 풀과 Executor 프레임워크1
## 스레드를 직접 생성해서 쓰면 문제가 있다.
- 1. 스레드 생성 시간으로 인한 성능 문제
- 스레드는 자신만의 호출 스택을 가지고 있어야 한다.
- 호출 스택을 스레드가 실행되는 동안 사용한다.
- 호출 스택을 위한 메모리를 할당해야 한다.
- 스레드 생성은 OS 커널 시스템 콜로 된다.
- CPU와 메모리 리소스가 소모된다.
- 스레드는 OS 스케줄러에 의해 실행된다.
- 스케줄링 알고리즘에 따른 오버헤드가 있다.
- 스레드 하나는 보통 1MB 이상의 메모리 쓴다.
- 자바 객체 생성보다 스레드 생성이 큰 작업이다.
- 어떤 작업 하나를 수행할 때마다 스레드 생성하면
- 작업 실행 보다 스레드 생성 시간이 오래 걸릴 수도 있다.
- 만약 스레드 재사용하면 처음 생성할 때 제외하고는 생성 위한 시간이 안 들 것이다.
- 2. 스레드 관리 문제
- CPU, 메모리가 버틸 만큼의 스레드 개수를 생성해야 한다.
- 또한 프로그램 종료 시 안전한 종료를 위해
- 스레드가 남은 작업을 모두 수행하게 한다거나,
- 급하게 인터럽트 등으로 스레드 종료하게 한다거나
- 할 때도 스레드가 어딘가에 관리되어 있어야 한다.
- 3. Runnable 인터페이스의 불편함
- Runnable 인터페이스는 불편한 점이 있다.
- run 메서드가 반환 값을 가지지 않는다.
- join으로 스레드가 종료되길 기다린 다음에
- 멤버 변수에 보관한 값을 받아야 한다.
- run 메서드는 체크 예외를 던질 수 없다.
- 체크 예외를 메서드 내부에서 처리해야 한다.
## 1, 2번 문제 해결 = 스레드 풀
- 스레드 풀에서 스레드를 생성하고 관리한다.
- 스레드 풀에서 스레드를 필요한 만큼 만들어둔다.
- 작업 요청이 오면 스레드 풀에서 스레드를 하나 조회한다.
- 조회한 스레드로 작업을 처리한다.
- 스레드는 작업이 완료되면 종료가 아니라, 다시 스레드 풀에 반납된다.
- 컬렉션에 스레드를 보관한 게 스레드 풀이다.
- 재사용할 수 있도록 WAITING 상태로 둔다.
- 작업 요청이 오면 RUNNABLE 상태로 변경한다.
## Executor 프레임워크
- 자바 동시성 프로그래밍의 핵심 컴포넌트다.
- 스레드 생성과 관리의 복잡성을 추상화하여 개발자가 태스크 실행 로직에 집중할 수 있도록 한다.
### Executor 인터페이스
- 가장 기본적인 추상화 계층이다.
- 단일 메서드 executr(Runnable)만을 정의한다.
- 태스크 제출과 실행 메커니즘을 분리하는 핵심 설계 원칙을 구현한다.
```java
void execute(Runnable command)
```
### ExecutorService
- Executor를 확장해 라이프사이클 관리 기능 추가
- submit(), shutdown(), awaitTermination() 등의 메서드로 태스크 제출, 서비스 종료, 완료 대기를 제어한다.
### ThreadPoolExecutor
- ExecutorService의 핵심 구현체다.
- 스레드 풀 기반 실행 엔진이다.
- 코어 스레드, 최대 스레드, 작업 큐, 거부 정책 등을 세밀하게 설정할 수 있는 가장 유연한 구현체다.
### ScheduledExecutorService
- 지연 실행과 주기적 실행을 지원하는 특수 인터페이스다.
- schedule(), scheduleAtFixedRate() 등을 통해 시간 기반 태스크 스케줄링을 구현한다.
### ThreadPoolExecutor 핵심 파라미터
- 필수 생성자 파라미터
- corePoolSize:
- 풀에 유지할 최소 스레드 수다.
- 태스크 없이도 이 수만큼의 스레드는 살아 있다.
- 새 태스크 도착 시 즉시 실행할 수 있다.
- maximumPoolSize:
- 풀이 생성할 수 있는 최대 스레드 수다.
- 큐가 가득 찼을 때 이 한계까지 스레드를 추가 생성한다.
- keepAliveTime:
- 코어 스레드 수를 초과하는 유휴 스레드의 생존 시간이다.
- 이 시간 동안 작업이 없으면 스레드가 종료된다.
- unit:
- keepAliveTime의 시간 단위를 지정하는 TimeUnit 열거형이다.
- 작업 큐 전략
- `BlockingQueue<Runnable>`:
- 제출된 태스크를 보관하는 대기 큐다.
- 다양한 구현체가 서로 다른 순서 보장과 용량 특성을 제공한다.
- LinkedBlockingQueue:
- 무제한 큐로, 메모리가 허용하는 한 태스크를 계속 수용한다.
- 기본 Executors.newFixedThreadPool()에서 사용된다.
- ArrayBlockingQueue:
- 고정 크기 배열 기반 유한 큐로, 생성 시 용량을 지정한다.
- SynchronousQueue:
- 용량이 0인 특수 큐로, 생산자와 소비자를 직접 핸드오프한다.
- Executors.newCachedThreadPool()에서 사용된다.
- PriorityBlockingQueue:
- 우선순위 기반 무제한 큐로, Comparable 또는 Comparator로 태스크 순서를 결정한다.
- 파라미터들은 스레드 풀의 동작 특성을 제어한다.
- 애플리케이션 부하 패턴과 성능 요구사항에 맞춰 파라미터를 튜닝할 수 있다.
### ThreadPoolExecutor 동작 흐름
- 이 흐름은 ThreadPoolExecutor.execute() 메서드 내부에서 구현된다.
- 각 단계는 원자적 연산과 락을 통해 스레드 안전성을 보장한다.
- AtomicInteger ctl 필드는 워커 수와 실행 상태를 단일 32비트 정수에 패킹하여 효율적인 상태 관리를 제공한다.
- 1. 태스크 제출
- 클라이언트가 execute(Runnable) 또는 submit(Callable)을 호출해 테스크를 제출한다.
- 2. 코어 스레드 확인
- 현재 실행 중인 워커 스레드 수가 corePoolSize 미만이면 새 워커 스레드를 생성해 태스크를 즉시 실행한다.
- 3. 큐 삽입 시도
- 워커 스레드가 corePoolSize 이상이면 workQueue에 태스크를 삽입 시도한다.
- 큐에 공간이 있으면 대기한다.
- 4. 최대 스레드 확장
- 큐가 가득 차고 워커 스레드 수가 maximumPoolSize 미만이면 새 워커 스레드를 추가 생성한다.
- 5. 거부 정책 실행
- 큐가 가득 차고 스레드도 maximumPoolSize에 도달했으면 RejectedExecutionHandler가 태스크를 거부한다.
### Worker 스레드 내부 구현
- Worker 클래스 구조
- ThreadPoolExecutor의 내부 클래스인 Worker는 실제 태스크를 실행하는 스레드를 래핑한다.
- AbstractQueuedSynchronized(AQS)를 상속하여 자체적인 락 메커니즘을 구현한다.
- thread 필드: ThreadFactory가 생성한 실제 스레드 객체
- firstTask 필드: 워커 생성 시 즉시 실행할 첫 태스크
- completedTasks 필드: 이 워커가 완료한 태스크 수
```java
private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
    final Thread thread;
    Runnable firstTask;
    volatile long completedTasks;
    Worker(Runnable firstTask) {
        setState(-1); //AQS 상태 초기화
        this.firstTask = firstTask;
        this.thread = getThreadFactory().newThread(this);
    }
}
```
- runWorker 메서드
- Worker의 run()은 runWorker()를 호출해 실제 태스크 처리 루프를 실행한다.
- 이 루프는 getTask()가 null을 반환할 때까지 계속 실행된다.
- getTask()가 null을 반환하는 것이 워커 스레드 종료 조건이다.
```java
final void runWorker(Worker w) {
    Runnable task = w.firstTask;
    w.firstTask = null;
    while (task != null || (task = getTask()) != null) {
        w.lock();
        try {
            beforeExecute(w.thread, task);
            task.run();
            afterExecute(task, null);
        } finally {
            task = null;
            w.completedTasks++;
            w.unlock();
        }
    }
}
```
### 태스크 큐잉과 getTask() 메커니즘
- getTask()는 workQueue에서 다음 실행할 태스크를 가져오는 블로킹 메서드다.
- 이 메서드가 스레드 풀의 동적 크기 조정과 종료 로직 구현에 핵심이다.
- 블로킹 대기: 코어 스레드는 take()로 무한 대기하고, 초과 스레드는 poll(timeout)으로 제한 시간 대기한다.
- 타임아웃 처리: keepAliveTime 동안 태스크가 없으면 null을 반환하여 워커 스레드를 종료시킨다.
- 상태 전이: SHUTDOWN 이상 상태에서는 큐가 비면 즉시 null을 반환해 정상 종료를 진행한다.
```java
private Runnable getTask() {
    boolean timedOut = false;
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
        //종료 상태 확인
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            return null;
        }
        int wc = workerCountOf(c);
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
        //타임아웃 또는 최대 스레드 초과 시 종료
        if ((wc > maximumPoolSize || (timed && timedOut)) && (wc > 1 || workQueue.isEmpty())) {
            if (compareAndDecrementWorkerCount(c))
                return null;
            continue;
        }
        try {
            Runnable r = timed ? workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : workQueue.take();
            if (r != null)
                return r;
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```
### RejectedExecutionHandler 구현체
- ThreadPoolExecutor가 더 이상 태스크를 수용할 수 없을 때 실행되는 정책
- AbortPolicy (기본값)
- RejectedExecutionException을 던져 태스크를 즉시 거부
- 호출자가 예외를 처리하여 백프레셔를 구현할 수 있다.
```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    throw new RejectedExecutionException("Task rejected");
}
```
- CallerRunsPolicy
- 제출한 스레드가 직접 태스크를 실행하여 자연스로운 쓰로틀링을 제공한다.
- 제출 속도가 자동으로 감소된다.
```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    if (!e.isShutdown()) {
        r.run();
    }
}
```
- DiscardPolicy
- 태스크를 조용히 버린다. 예외가 발생하지 않는다.
- 손실 가능한 태스크에만 사용한다.
```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    //아무 것도 하지 않음
}
```
- DiscardOldestPolicy
- 큐의 가장 오래된 태스크를 제거하고 새 태스크를 다시 제출 시도한다.
- 우선순위 큐와 함께 사용하면 위험하다.
```java
public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    if (!e.isShutdown()) {
        e.getQueue().poll();
        e.execute(r);
    }
}
```
- 태스크 실행 중 예외 처리
- Worker의 runWorker()는 태스크 실행 중 발생한 예외를 잡아 afterExecute() 훅 메서드로 전달한다.
- 체크되지 않은 예외는 스레드를 종료시키고, ThreadPoolExecutor는 새 워커를 생성하여 대체한다.
- submit()으로 제출된 Callable은 Future에 예외를 캡슐화해서 get() 호출 시 던진다.
### Executors 팩토리 메서드와 특성
- newFixedThreadPool
- 고정된 수의 스레드로 무제한 큐를 사용한다.
- 스레드 수가 일정하여 예측 가능한 리소스 사용을 보장하지만
- 큐가 무제한이므로 메모리 부족 위험이 있다.
- 태스크 도착 속도가 처리 속도를 초과하지 않는 CPU 바운드 작업에 적합하다.
```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
}
```
- newCachedThreadPool
- 필요에 따라 스레드를 무제한 생성하며, 60초 유후 후 회수한다.
- SynchronousQueue는 큐잉 없이 즉시 핸드오프한다.
- 짧고 가벼운 비동기 태스크가 많을 때 적합하지만, 스레드 폭발 위험이 있어 주의가 필요하다.
```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
}
```
- newSingleThreadExecutor
- 단일 워커 스레드로 태스크를 순차 실행한다.
- 스레드가 종료되면 새 스레드가 대체한다.
- DelegatedExecutorService로 래핑하여 재설정을 방지한다.
- 태스크 실행 순서 보장이 중요하거나, 동시성 이슈를 피하고 싶을 때 사용한다.
```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedES(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));
}
```
- newScheduledThreadPool
- ScheduledThreadPoolExecutor 기반으로 지연 실행과 주기적 실행을 지원한다.
- DelayedWorkQueue를 사용해 실행 시간 기준으로 태스크를 정렬한다.
- 타이머 기반 태스크 스케줄링이 필요한 모든 시나리오에 적합하다.
```java
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
}
```
### ctl 필드와 상태 인코딩
- ThreadPoolExecutor는 AtomicInteger ctl 필드에 실행 상태(runState)와 워커 수(workerCount)를 비트 패킹하여 저장한다.
- 상위 3비트는 상태를, 하위 29비트는 워커 수를 나타낸다.
```java
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
private static final int COUNT_BITS = Integer.SIZE - 3;
private static final int CAPACITY = (1 << COUNT_BITS) - 1;
//상태 값 (상위 3비트)
private static final int RUNNING = -1 << COUNT_BITS; //111...
private static final int SHUTDOWN = 0 << COUNT_BITS; //000...
private static final int STOP = 1 << COUNT_BITS; //001...
private static final int TIDYING = 2 << COUNT_BITS; //010...
private static final int TERMINATED = 3 << COUNT_BITS; //011...
//유틸리티 메서드
private static int runStateOf(int c) { return c & ~CAPACITY; }
private static int workerCountOf(int c) { return c & CAPACITY; }
private static int ctlOf(int rs, int wc) { return rs | wc; }
```
- 1. RUNNING
- 새 태스크를 수용하고 큐의 태스크를 처리한다.
- 2. SHUTDOWN
- 새 태스크는 거부하지만 큐의 태스크는 처리한다.
- shutdown()은 RUNNING->SHUTDOWN 전이를 일으킨다.
- 3. STOP
- 새 태스크 거부, 큐 처리 중단, 실행 중인 태스크 인터럽트
- shutdownNow() 호출 시 즉시 STOP으로 전이된다.
- 큐에 남은 태스크 리스트를 반환한다.
- 4. TIDYING
- 모든 태스크가 종료되고 워커 수가 0일 때 전이된다.
- terminated() 훅을 실행한다.
- 5. TERMINATED
- terminated() 완료 후 최종 상태다.
- awaitTermination(timeout)은 지정된 시간 동안 TERMINATED 상태가 되기를 블로킹 대기한다.
### 스레드 풀 크기 결정 가이드
- 최적의 스레드 풀 크기는 태스크 특성에 따라 결정된다.
- CPU 바운드 태스크
- 계산 집약적 작업은 코어 수에 맞춰 스레드를 설정한다.
- 추가 1개는 페이지 폴트나 기타 대기 시간을 보상한다.
- `int poolSize = Runtime.getRuntime().availableProcessors() + 1;`
- I/O 바운드 태스크
- 대기 시간이 긴 작업은 더 많은 스레드가 필요하다.
- 예: 코어 4개, 대기/계산 비율이 3:1이면 16개 스레드가 적절하다.
- `int poolSize = cores * (1 + waitTime / computeTime);`
- 혼합 워크로드
- 서로 다른 특성의 태스크는 별도 풀로 분리한다.
- CPU 집약적 태스크와 I/O 태스크를 같은 풀에 두면 상호 방해가 발생한다.
### 모니터링과 튜닝 메트릭
- ThreadPoolExecutor는 런타임 모니터링을 위한 메서드를 제공
- getActiveCount(): 현재 태스크를 실행 중인 스레드 수
- getPoolSize(): 풀의 현재 스레드 수
- getLargestPoolSize(): 풀이 도달한 최대 크기
- getTaskCount(): 제출한 총 태스크 수 (근사값)
- getCompletedTaskCount(): 완료된 태스크 수
- getQueue().size(): 대기 중인 태스크 수
- 성능 문제 진단 지표
- 큐 크기가 지속적으로 증가: 스레드 부족 또는 태스크 처리 속도 저하
- 거부 정책 호출 빈도: 용량 초과 상황 발생 빈도
- 평균 태스크 완료 시간: 성능 저하 조기 감지
- 스레드 풀 크기 변동: 부하 패턴 분석
### 안티패턴
- 무제한 큐와 고정 스레드 결합:
- newFixedThreadPool의 기본 설정은 메모리 고갈을 유발할 수 있다.
- 유한 큐를 사용하고 적절한 거부 정책을 설정한다.
- 너무 큰 maximumPoolSize:
- 컨텍스트 스위칭 오버헤드가 증가하고 메모리 사용량이 급증한다.
- 실제 필요량을 측정해 설정한다.
- 종료 처리 누락:
- ExecutorService를 종료하지 않으면 JVM이 종료되지 않는다.
- 항상 shutdown()을 호출하거나, 데몬 스레드를 사용한다.