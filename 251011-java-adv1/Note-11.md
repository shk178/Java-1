# 10. 생산자 소비자 문제2
```java
public class Bounded2Queue implements BoundedQueue {
    private final Lock lock = new ReentrantLock(true);
    private final Condition condition = lock.newCondition();
    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;
    public Bounded2Queue(int max) {
        this.max = max;
    }
    @Override
    public void put(String data) {
        int count = 0;
        lock.lock();
        try {
            while (queue.size() == max) {
                log("[put] 큐가 가득 참, 대기, while문 실행 횟수=" + ++count);
                try {
                    condition.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.offer(data);
            log("[put] offer data=" + data + ", while문 실행 횟수=" + count);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
    //...
}
```
```java
//Bounded2Queue 클래스의 멤버 필드들은 동기화된 제한 큐(Bounded Queue)를 구현하기 위해 사용
private final Lock lock = new ReentrantLock(true);
//역할: 큐에 접근하는 스레드 간의 동기화를 위해 사용되는 재진입 가능 락
//true 인자: 공정성(fairness) 설정
//true로 설정하면 락을 기다리는 스레드들이 FIFO 순서대로 락을 획득
//즉, 먼저 기다린 스레드가 먼저 락을 얻음
private final Condition condition = lock.newCondition();
//역할: Lock과 함께 사용하는 조건 변수
//용도: 큐가 가득 찼을 때 put()을 호출한 스레드를 대기(wait) 상태로 만들고
//큐에 공간이 생기면 신호(signal)를 보내 다시 작업을 진행할 수 있도록 한다.
//condition.await() → 대기
//condition.signal() → 대기 중인 스레드 하나 깨움
private final Queue<String> queue = new ArrayDeque<>();
//역할: 실제 데이터를 저장하는 큐(Queue)
//구현체: ArrayDeque는 빠르고 효율적인 비동기 큐 구현체로, FIFO 방식으로 데이터를 저장
//제네릭 타입: String 타입의 데이터를 저장
private final int max;
//역할: 큐의 최대 크기(용량)를 지정
//제한 큐(Bounded Queue)이므로, 이 값보다 많은 데이터를 넣을 수 없다.
//put() 메서드에서 queue.size() == max 조건으로 큐가 가득 찼는지 확인
```
```
- Bounded2Queue 클래스에서 데이터를 저장하는 큐는 queue 필드
- Condition 객체가 내부적으로 대기 중인 스레드들을 관리하는 대기 큐(wait queue)를 가지고 있다.
- 자바의 Condition은 Lock과 함께 사용되는 고급 동기화 도구
- await()를 호출한 스레드들은 다음과 같은 방식으로 처리:
- condition.await()를 호출하면 현재 스레드는 lock을 반납하고, Condition 객체의 대기 큐(wait set)에 들어간다.
- 이 대기 큐는 JVM 내부에서 관리되며, Condition 객체가 직접 노출하지는 않는다.
- 이후 다른 스레드가 condition.signal() 또는 condition.signalAll()을 호출하면
- 대기 큐에 있는 스레드 중 하나 또는 모두가 깨워져서 다시 lock을 얻으려 시도한다.
```
```
19:51:07.063 [     main] [startProducer]
19:51:07.063 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
////생산자1이 소비자1 호출
19:51:07.065 [     소비자1] [take] poll result=data1, while문 실행 횟수=1
////소비자1이 소비자2 호출
19:51:07.065 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=2
19:51:07.180 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
////생산자2가 소비자3 호출
19:51:07.180 [     소비자3] [take] poll result=data2, while문 실행 횟수=1
////소비자3이 소비자2 호출
19:51:07.180 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=3
19:51:07.286 [     생산자3] [put] offer data=data3, while문 실행 횟수=0
////생산자3이 소비자2 호출
19:51:07.286 [     소비자2] [take] poll result=data3, while문 실행 횟수=3
//notify 썼을 때랑 결과가 같다.
```
```java
public class Bounded2Queue2 implements BoundedQueue {
    private final Lock lock = new ReentrantLock(true);
    private final Condition condProducer = lock.newCondition();
    private final Condition condConsumer = lock.newCondition();
    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;

    public Bounded2Queue2(int max) {
        this.max = max;
    }

    @Override
    public void put(String data) {
        int count = 0;
        lock.lock();
        try {
            while (queue.size() == max) {
                log("[put] 큐가 가득 참, 대기, while문 실행 횟수=" + ++count);
                try {
                    condProducer.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.offer(data);
            log("[put] offer data=" + data + ", while문 실행 횟수=" + count);
            condConsumer.signal();
        } finally {
            lock.unlock();
        }
    }
    //...
}
```
```
20:07:07.389 [     main] [consumerFirst] queueName=Bounded2Queue2
20:07:07.718 [     main] [startProducer]
20:07:07.718 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
20:07:07.718 [     소비자1] [take] poll result=data1, while문 실행 횟수=1
20:07:07.827 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
20:07:07.827 [     소비자2] [take] poll result=data2, while문 실행 횟수=1
20:07:07.937 [     생산자3] [put] offer data=data3, while문 실행 횟수=0
20:07:07.937 [     소비자3] [take] poll result=data3, while문 실행 횟수=1
```
- 같은 condition을 썼을 때는 소비자가 소비자를 깨웠지만 생산은 안 된 상황이어서
- 다시 대기하는 상황이 있었지만 다른 condition을 쓰니까 소비자가 소비자를 안 깨움
- 생산-소비 1:1 대응되는 상황에서는 signalAll()을 안 쓰는 게 낫다.
## 2단계 대기 구조 - synchronized + wait/notify 기반
- 1. 락 획득 대기
- synchronized 블록에 진입하려는 스레드는 해당 모니터 락(monitor lock)을 먼저 획득해야 한다.
- 이미 다른 스레드가 락을 보유 중이면, 락이 해제될 때까지 대기한다.
- 2. wait() 대기
- 락을 획득한 후, wait()를 호출하면:
- 락을 반납하고, 해당 모니터의 wait set에 들어가 대기한다.
- 이후 notify() 또는 notifyAll()로 깨워야 다시 락을 얻고 실행된다.
- 1. 스레드가 synchronized 블록 진입 시도
- 상태: RUNNABLE → BLOCKED (다른 스레드가 락을 보유 중이면)
- 락을 획득하면 다시 RUNNABLE
- 2. wait() 호출
- 상태: RUNNABLE → WAITING
- 현재 락을 반납하고, 모니터의 wait set에 들어감
- 3. notify() 또는 notifyAll() 호출됨
- 상태: WAITING → BLOCKED (다시 락을 얻기 위해 대기)
- 락을 획득하면 RUNNABLE로 전환
## 2단계 대기 구조 - ReentrantLock + Condition 기반
- 1. 락 획득 대기
- lock.lock() 호출 시, 다른 스레드가 락을 보유 중이면 락이 해제될 때까지 대기한다.
- ReentrantLock은 공정성 설정(true)에 따라 FIFO 순서 보장도 가능하다.
- 2. condition.await() 대기
- 락을 획득한 후 condition.await()를 호출하면:
- 락을 반납하고, 해당 Condition 객체의 대기 큐에 들어가 대기한다.
- 이후 condition.signal() 또는 signalAll()로 깨워야 다시 락을 얻고 실행된다.
- 1. lock.lock() 시도
- 상태: RUNNABLE → BLOCKED (다른 스레드가 락을 보유 중이면)
- 락을 획득하면 다시 RUNNABLE
- 2. condition.await() 호출
- 상태: RUNNABLE → WAITING
- 락을 반납하고, 해당 Condition의 대기 큐에 들어감
- 3. condition.signal() 또는 signalAll() 호출됨
- 상태: WAITING → BLOCKED (다시 락을 얻기 위해 대기)
- 락을 획득하면 RUNNABLE로 전환
## BlockingQueue 인터페이스
- java.util.concurrent 패키지에 포함된 인터페이스
- 스레드 간 안전하게 데이터를 주고받을 수 있는 큐를 정의
- 큐가 비어 있거나 가득 찼을 때 자동으로 대기(blocking)
- 스레드 안전(thread-safe): 내부적으로 락과 조건 변수로 동기화 처리됨
- FIFO 순서 보장: 대부분의 구현체는 기본적으로 FIFO 방식

| 구현 클래스 | 특징 |
|------------|------|
| ArrayBlockingQueue | 고정 크기의 배열 기반 큐 |
| LinkedBlockingQueue | 연결 리스트 기반, 크기 제한 가능 |
| PriorityBlockingQueue | 우선순위 기반 큐 (FIFO 아님) |
| DelayQueue | 일정 시간 후에 꺼낼 수 있는 큐 |
| SynchronousQueue | 버퍼가 없는 큐, 즉시 전달 |
| LinkedTransferQueue | 고성능, 생산자-소비자 간 직접 전달 가능 |

- 1. Throws Exception - 대기 대신 예외
- Insert(추가) 메서드: add(E e)
- 지정된 요소를 큐에 추가
- 큐가 가득 차면 IllegalStateException 예외를 던진다.
- Remove(제거) 메서드: remove()
- 큐에서 요소를 제거하며 반환
- 큐가 비어 있으면 NoSuchElementException 예외를 던진다.
- Examine(관찰) 메서드: element()
- 큐의 맨 앞 요소를 반환하지만, 요소를 큐에서 제거하지 않는다.
- 큐가 비어 있으면 NoSuchElementException 예외를 던진다.
- 2. Special Value - 대기 대신 false/null 반환
- Insert(추가) 메서드: offer(E e)
- 지정된 요소를 큐에 추가
- 큐가 가득 차면 false를 반환
- Remove(제거) 메서드: poll()
- 큐에서 요소를 제거하고 반환
- 큐가 비어 있으면 null을 반환
- Examine(관찰) 메서드: peek()
- 큐의 맨 앞 요소를 반환하지만, 요소를 큐에서 제거하지 않는다.
- 큐가 비어 있으면 null을 반환
- 3. Blocks - 대기 (인터럽트 지원)
- Insert(추가) 메서드: put(E e)
- 지정된 요소를 큐에 추가
- 큐가 가득 차면 공간이 생길 때까지 대기
- Remove(제거) 메서드: take()
- 큐에서 요소를 제거하고 반환
- 큐가 비어 있으면 요소가 준비될 때까지 대기
- Examine(관찰) 메서드: x
- 4. Times Out - 시간 대기 (인터럽트 지원)
- Insert(추가) 메서드: offer(E e, long timeout, TimeUnit unit)
- 지정된 요소를 큐에 추가
- 큐가 가득 차면 timeout 동안 공간이 생길 때까지 대기하다가
- 시간이 초과되면 false를 반환
- unit: TimeUnit.SECONDS 등 timeout의 단위 지정
- Remove(제거) 메서드: poll(long timeout, TimeUnit unit)
- 큐에서 요소를 제거하고 반환
- 큐가 비어 있으면 timeout 동안 요소가 준비될 때까지 대기하다가
- 시간이 초과되면 null을 반환
- unit: TimeUnit.SECONDS 등 timeout의 단위 지정
- Examine(관찰) 메서드: x
- 251011-java-adv1/src/bounded2/Main.java
```
21:55:22.365 [     main] [startProducer]
21:55:22.375 [     생산자1] [put] data=data1, queue=[data1]
21:55:22.483 [     생산자2] [put] data=data2, queue=[data1, data2]
21:55:22.703 [     main] [startConsumer]
21:55:22.704 [     생산자3] [put] data=data3, queue=[data2, data3]
21:55:22.704 [     소비자1] [take] result=data1, queue=[data2]
21:55:22.813 [     소비자2] [take] result=data2, queue=[data3]
21:55:22.921 [     소비자3] [take] result=data3, queue=[]
21:55:23.032 [     main] [startConsumer]
21:55:23.359 [     main] [startProducer]
21:55:23.360 [     생산자1] [put] data=data1, queue=[data1]
21:55:23.360 [     소비자1] [take] result=data1, queue=[]
21:55:23.467 [     소비자2] [take] result=data2, queue=[]
//소비자2가 take 기다리다가 생산자2가 put하니까 바로 take하고 로그까지 찍었다.
21:55:23.467 [     생산자2] [put] data=data2, queue=[data2]
21:55:23.578 [     소비자3] [take] result=data3, queue=[]
21:55:23.578 [     생산자3] [put] data=data3, queue=[data3]
```