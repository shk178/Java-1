# 9. 생산자 소비자 문제1
- 생산자 소비자 문제
- 멀티스레드 프로그래밍에서 자주 등장하는 동시성 문제 중 하나다.
- 여러 스레드가 동시에 데이터를 생산하고 소비하는 상황을 다룬다.
- 생산자 Producer
- 데이터를 생성하는 역할
- 파일에서 데이터를 읽어오거나
- 네트워크에서 데이터를 받아오는
- 스레드가 생산자 역할
- 소비자 Consumer
- 생성된 데이터를 소비하는 역할
- 데이터를 처리하거나
- 데이터를 저장하는
- 스레드가 소비자 역할
- 버퍼 Buffer
- 생산자가 생성한 데이터를 일시적으로 저장하는 공간
- 버퍼는 한정된 크기를 가지며
- 생산자와 소비자가 이 버퍼를 통해 데이터를 주고받는다.
- 프린터 예제
- main 스레드
- 사용자 입력 반복
- Printer 인스턴스
- work=true or false //메인 스레드
- jobQueue=사용자 입력 offer() //메인 스레드
- while(work==true) //프린터 스레드
- 프린터 큐에서 poll() //프린터 스레드
- 3초 sleep //프린터 스레드
- 사용자 입력을 프린터 큐에 전달하는 스레드가 생산자 스레드
- 프린터 큐에서 데이터를 꺼내서 출력하는 스레드가 소비자 스레드
- 프린터 큐가 버퍼
- 문제 상황
- 생산자가 너무 빠름: 버퍼가 다 찼는데 생산자가 데이터를 생성하려 한다.
- 버퍼에 빈 공간이 생길 때까지 생산자는 기다려야 한다.
- 소비자가 너무 빠름: 버퍼가 비어서 더 소비할 데이터가 없을 때까지 소비자가 데이터를 처리한다.
- 버퍼에 데이터가 들어올 때까지 소비자는 기다려야 한다.
- 문제 상황 비유
- 생산자는 주방 요리사, 소비자는 레스토랑 손님, 버퍼는 주문된 음식이 놓이는 서빙 카운터
- 요리사는 음식 준비해 서빙 카운터에 놓는다.
- 손님은 서빙 카운터에서 음식을 가져가 먹는다.
- 서빙 카운터가 가득 차거나 비어 있으면 요리사나 손님이 기다린다.
- 생산자는 음료 공장, 소비자는 상점, 버퍼는 창고
- 음료 공장은 음료를 생산하고 창고에 보관한다.
- 상점은 창고에서 음료를 가져다 판다.
- 창고가 가득 차거나 비어 있으면 음료 공장이나 상점이 기다린다.
- 생산자 소비자 문제=한정된 버퍼 문제
- 생산자 소비자 문제는, 생산자 스레드와 소비자 스레드가 특정 자원을 동시에 생산하고 소비하면서 발생하는 문제이다.
- 한정된 버퍼 문제는, 중간에 버퍼의 크기가 한정되어 있기 때문에 발생하는 문제이다.
```java
public class BoundedQueue1 implements BoundedQueue {
    private final Queue<String> queue = new ArrayDeque<>();
    private final int max;
    public BoundedQueue1(int max) {
        this.max = max;
    }
    @Override
    public synchronized void put(String data) {
        if (queue.size() == max) {
            log("[put] 큐가 가득 참, 버림: " + data);
            return;
        }
        queue.offer(data);
    }
    @Override
    public synchronized String take() {
        if (queue.isEmpty()) {
            log("[take] 큐가 비어 있음, 반환: null");
            return null;
        }
        return queue.poll();
    }
    @Override
    public synchronized String toString() {
        return queue.toString();
    }
}
/*
ConcurrentModificationException은 컬렉션을 순회(iterate)하는 도중에 그 컬렉션의 구조가 바뀌면 생기는 예외
예를 들어, 리스트나 큐를 돌면서 출력하는 중에 다른 스레드가 그 컬렉션에 데이터를 추가하거나 제거하면 터질 수 있다.
ArrayDeque의 toString() 메서드는 내부적으로 iterator(반복자)를 써서 큐의 모든 원소를 순회하며 문자열로 만드는 방식이라,
그 시점에 다른 스레드가 put()이나 take()로 큐를 수정하면 구조가 바뀌어서 예외가 날 가능성이 있다.
그래서 toString()에도 synchronized를 붙여서 put()·take()와 같은 락(this)을 공유하게 하면,
여러 스레드가 동시에 큐를 바꾸거나 읽는 일을 막을 수 있고,
그 결과 예외도 안 나고 항상 일관된 상태의 문자열을 얻을 수 있다.
다만 synchronized를 붙이면 toString()이 실행되는 동안 다른 스레드의 put()이나 take()가 잠깐 대기하게 되긴 하지만,
toString()이 짧게 끝나는 연산이라 보통은 성능 문제는 거의 없다.
 */
```
- 251011-java-adv1/src/bounded/BoundedQueue1.java //버퍼 용량
- 251011-java-adv1/src/bounded/BoundedQueue2.java //무한 대기
- Object.wait()
- 현재 스레드가 가진 락 반납하고 WAITING 상태로 전환
- 현재 스레드가 synchronized 블록에서 락을 소유하고 있을 때만 호출 가능
- wait()에서 깨어나면 wait() 바로 다음 줄부터 계속 실행
- 251011-java-adv1/src/bounded/BoundedQueue3.java //WAITING 안 깨어남, 프로그램 안 멈춤
- Object.notify()
- synchronized 블록에서 호출되어야 한다.
- 대기 중인 스레드 중 하나를 깨움
- Object.notifyAll()
- synchronized 블록에서 호출되어야 한다.
- 대기 중인 모든 스레드를 깨움
- 깨어난 스레드들이 동기화 블록에 진입 경쟁
- notify()나 notifyAll()을 호출해도 자기 자신은 잠들지 않고, 계속 블록 실행하다가 락을 반납한다.
- wait()로 잠든 스레드는 notify()나 notifyAll() (또는 interrupt)에 의해 깨지지 않으면 깨어날 수 없다.
```
wait() → notify() 이후의 스레드 상태 변화
1. wait() 호출 시점
- 스레드는 monitor lock(객체의 락)을 가진 상태에서 wait()를 호출
- wait()를 호출하면 락을 놓고, WAITING 상태로 전환
- 이때 스레드는 스케줄링 큐에서 제외되고, 해당 객체의 wait set에 들어감
2. notify() 또는 notifyAll() 호출
- 다른 스레드가 같은 객체에 대해 호출하면, wait set에 있는 스레드 중 하나(또는 모두)가 WAITING → BLOCKED 상태로 전환됨
- 이때 스레드는 Runnable 상태가 아니라, monitor lock을 얻기 위해 BLOCKED 상태로 대기
3. monitor lock 획득
- notify()로 깨운 스레드는 다시 monitor lock을 얻어야만 실제로 실행(Runnable) 상태로 갈 수 있다.
- lock을 얻는 순간 스케줄링 큐에 등록되어 Runnable 상태가 된다.
정리
- wait() → 락을 놓고 wait set에 들어감 (스케줄링 큐에서 제외됨)
- notify() → wait set에서 깨움 (하지만 락을 얻기 전까지 BLOCKED)
- 락을 얻는 순간 → 스케줄링 큐에 등록되고 Runnable 상태로 전환됨
```
- while(생산/소비-검증) { this.wait(); } 생산/소비-실행; this.notify/notifyAll();
- 251011-java-adv1/src/bounded/BoundedQueue4.java
```
19:17:51.656 [     main] [consumerFirst] queueName=BoundedQueue4
19:17:51.656 [     main] [startConsumer]
19:17:51.657 [     소비자1] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:17:51.765 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:17:51.872 [     소비자3] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:17:51.981 [     main] [startProducer]
19:17:51.982 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
//생산자1이 소비자1 호출
19:17:51.982 [     소비자1] [take] poll result=data1, while문 실행 횟수=1
//소비자1이 소비자2 호출
19:17:51.982 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=2
19:17:52.090 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
//생산자2가 소비자3 호출
19:17:52.090 [     소비자3] [take] poll result=data2, while문 실행 횟수=1
//소비자3이 소비자2 호출
19:17:52.091 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=3
19:17:52.198 [     생산자3] [put] offer data=data3, while문 실행 횟수=0
//생산자3이 소비자2 호출
19:17:52.199 [     소비자2] [take] poll result=data3, while문 실행 횟수=3
notify()를 써도 되는 경우
- 단일 소비자/생산자 구조에서
- 조건이 단순하고 명확할 때
- 성능이 중요한 경우 (불필요한 깨어남 방지)
```
- 251011-java-adv1/src/bounded/BoundedQueue5.java
```
19:18:16.383 [     main] [consumerFirst] queueName=BoundedQueue5
19:18:16.383 [     main] [startConsumer]
19:18:16.384 [     소비자1] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:18:16.492 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:18:16.602 [     소비자3] [take] 큐가 비어 있음, 대기, while문 실행 횟수=1
19:18:16.712 [     main] [startProducer]
19:18:16.712 [     생산자1] [put] offer data=data1, while문 실행 횟수=0
//소비자1, 소비자2, 소비자3 깨어남 (소비자1이 락을 얻어 data1 소비)
19:18:16.713 [     소비자1] [take] poll result=data1, while문 실행 횟수=1
19:18:16.713 [     소비자3] [take] 큐가 비어 있음, 대기, while문 실행 횟수=2
19:18:16.713 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=2
19:18:16.818 [     생산자2] [put] offer data=data2, while문 실행 횟수=0
//소비자2, 소비자3 깨어남 (소비자3이 락을 얻어 data2 소비)
19:18:16.818 [     소비자3] [take] poll result=data2, while문 실행 횟수=2
19:18:16.820 [     소비자2] [take] 큐가 비어 있음, 대기, while문 실행 횟수=3
19:18:16.928 [     생산자3] [put] offer data=data3, while문 실행 횟수=0
//소비자2 깨어남 (소비자2가 락을 얻어 data3 소비)
19:18:16.929 [     소비자2] [take] poll result=data3, while문 실행 횟수=3
notifyAll()이 더 안전한 경우
- 다수의 스레드가 wait 중인 구조
- 조건이 복잡하거나 여러 종류의 wait이 섞여 있는 경우
- 디버깅이나 유지보수가 중요한 경우
- Deadlock을 피하고 싶을 때
```