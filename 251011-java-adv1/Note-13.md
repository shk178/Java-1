# 12. 동시성 컬렉션
# 자바 동기화 라이브러리와 컬렉션
## 동기화 필요성과 기본 개념
### 경쟁 조건 (Race Condition)
- 여러 스레드가 공유 자원에 동시에 접근할 때
- 실행 순서에 따라 결과가 달라지는 상황
- 예를 들어, 두 스레드가 동시에 카운터 증가시키면
- 예상과 다른 값이 나올 수 있다.
- 읽기-수정-쓰기 작업의 원자성 부재
- CPU 캐시와 메모리 일관성 문제
- 컴파일러 최적화로 인한 명령어 재배치
### 가시성 문제 (Visibility)
- 한 스레드에서 변경한 값이 다른 스레드에게 즉시 보이지 않는 형상
- CPU 캐시와 메인 메모리 간의 동기화 지연 때문에 발생
- 각 스레드의 로컬 캐시 사용
- 메모리 배리어의 필요성
- happens-before 관계 보장
## synchronized 키워드의 내부 동작
- 1. 모니터 락 획득
- 스레드가 synchronized 블록에 진입하려면 먼저 객체의 모니터 락을 획득해야 한다.
- JVM은 객체 헤더의 마크 워드를 사용해 락 상태를 관리한다.
- 2. 바이어스드 락킹
- 락 경합이 거의 없는 경우, JVM은 바이어스드 락킹을 통해 오버헤드를 줄인다.
- 첫 번째 스레드가 락을 획득하면 객체 헤더에 스레드 ID가 기록된다.
- 3. 경량 락으로 인플레이션
- 다른 스레드가 락을 요청하면, 바이어스드 락은 경량 락으로 전환된다.
- 이때 스택 프레임에 락 레코드가 생성되고 CAS 연산을 통해 락을 획득한다.
- 4. 중량 락으로 인플레이션
- 경합이 심해지면 OS 레벨의 뮤텍스를 사용하는 중량 락으로 전환된다.
- 이 과정에서 대기 스레드는 블로킹 상태가 되어 CPU 자원을 소비하지 않는다.
- 성능 고려사항
- sunchronized는 편리하지만 과도한 사용은 성능 저하를 초래한다.
- 임계 영역을 최소화하고, 필요시 ReentrantLock 같은 명시적 락을 고려해야 한다.
- 또한 락 순서를 일관되게 유지하여 데드락을 방지해야 한다.
## volatile과 메모리 가시성
### volatile의 동작 원리
- volatile 키워드는 변수에 대한 읽기와 쓰기를 메인 메모리에서 직접 수행하도록 강제한다.
- 이를 통해 모든 스레드가 항상 최신 값을 볼 수 있다.
- 메모리 배리어 효과
- 쓰기 배리어: volatile 변수에 쓰기 전에 수행된 모든 쓰기 연산이 먼저 메모리에 플러시된다.
- 읽기 배리어: volatile 변수를 읽은 후의 모든 읽기 연산은 최신 값을 가져온다.
- happens-before 보장: volatile 쓰기는 이후의 모든 volatile 읽기보다 먼저 발생한다.
- 제한사항: volatile은 가시성만 보장하며, 복합 연산의 원자성은 보장하지 않는다.
- 복합 연산의 원자성 보장하려면 AtomicInteger를 사용한다.
### volatile 사용 시나리오
- 플래그 변수: 스레드 간 상태 공유에 이상적이다.
```java
volatile boolean running = true;
while (running) {
    //작업 수행
}
```
- 더블 체크 락킹: 싱글톤 패턴에서 안전한 지연 초기화를 구현한다.
```java
private volatile static Singleton instance;
public static Singleton getInstance() {
    if (instance == null) {
        synchronized(Singleton.class) {
            if (instance == null) {
                instance = new Singleton();
            }
        }
    }
    return instance;
}
```
## java.util.concurrent 핵심 도구
### ReentrantLock
- synchronized보다 유연한 명시적 락 메커니즘
- 공정성 모드: 대기 시간이 긴 스레드에게 우선권 부여
- 타임아웃: tryLock(timeout)으로 데드락 방지
- 인터럽트 가능: lockInterruptibly() 지원
- 조건 변수: 여러 Condition 객체 생성 가능
- 내부적으로 AbstractQueuedSynchronizer(AQS)를 사용해서 대기 큐를 관리
### Semaphore
- 제한된 수의 리소스에 대한 접근을 제어
- 퍼밋 기반: acquire()로 퍼밋 획득, release()로 반환
- 공정성: FIFO 순서로 퍼밋 분배 가능
- 활용 사례: 커넥션 풀, 스레드 풀 크기 제한
- 카운팅 세마포어로, 내부 카운터가 0이 되면 스레드는 블로킹된다.
### CountDownLatch
- 여러 스레드가 특정 지점까지 진행하기를 기다린다.
- 일회성: 한 번만 사용 가능한 동기화 도구
- countDown(): 카운터 감소
- await(): 카운터가 0이 될 때까지 대기
- 병렬 작업의 시작점 동기화나 완료 대기에 유용
### CyclicBarrier
- 여러 스레드가 특정 지점에서 서로를 기다림
- 재사용 가능: 리셋 후 다시 사용 가능
- 배리어 액션: 모든 스레드 도착 시 실행할 작업 지정
- 병렬 알고리즘: 반복적인 단계별 동기화에 적합
- 내부적으로 ReentrantLock과 Condition을 사용해 구현
## Atomic 클래스와 CAS 연산
- Compare-And-Swap의 원리
- CAS는 락 없이 원자적 업데이트를 수행하는 하드웨어 명령어
- 메모리 위치의 값이 예상 결과와 같으면 새 값으로 교체하고, 그렇지 않으면 실패한다.
- AtomicInteger 내부 구현
- Unsafe 클래스를 통해 네이티브 CAS 연산을 호출한다.
- value 필드는 volatile로 선언되어 가시성이 보장된다.
- ABA 문제와 해결책
- 값이 A->B->A로 변경되어도 CAS는 성공한다.
- AtomicStampedReference는 버전 번호를 함께 관리해서 이 문제를 해결한다.
### 성능 특성
- 장점:
- 락 획득/해제 오버헤드 없음
- 데드락 발생 불가능
- 경합이 낮을 때 매우 빠름
- 스레드 스케줄링 불필요
- 단점:
- 경합이 높으면 반복 시도로 CPU 낭비
- 복잡한 알고리즘 구현 어려움
- ABA 문제 발생 가능
### 주요 Atomic 클래스
- AtomicInteger/Long: 정수형 원자적 연산
- AtomicBoolean: 불린 플래그 관리
- AtomicReference: 객체 참조 원자적 업데이터
- AtomicIntegerArray: 배열 요소 원자적 연산
- LongAdder: 높은 경합 상황에서 카운팅 최적화
- DoubleAdder: 부동소수점 카운터
## 동시성 컬렉션의 진화
### 레거시: 동기화 래퍼
- Collections.synchronizedList/Map
- 모든 메서드를 synchronized로 래핑
- 전체 컬렉션에 락을 걸어 성능이 매우 낮다.
- 반복 중 외부 동기화가 필요하다.
### 개선: ConcurrentHashMap
- 세그먼트 기반 락킹
- 자바 7까지는 16개 세그먼트로 분할해서 병렬 처리 성능을 향상시켰다.
- 각 세그먼트는 독립적으로 락을 관리한다.
### 최신: 락 프리 알고리즘
- CAS 기반 버킷 동기화
- 자바 8부터는 버킷 단위 CAS와 synchronized를 조합해 더 세밀한 동시성 제어를 제공한다.
- 읽기는 완전히 락 프리이다.
### 정리
- 동시성 컬렉션의 발전은 전체 락->부분 락->세밀한 락과 CAS로 진화했다.
- 이는 락 경합을 줄이고 병렬 처리 효율을 극대화하는 방향이다.
## ConcurrentHashMap 내부 구조 분석 (자바 8+)
### 노드 구조와 트리화
- 기본적으로 배열 + 연결 리스트 구조를 사용한다.
- 버킷의 체인 길이가 8을 초과하면 레드-블랙 트리로 전환된다.
- 최악의 경우 O(n)에서 O(logn)으로 성능을 개선한다.
- Node: 일반 연결 리스트 노드
- TreeNode: 트리 노드로 전환
- TreeBin: 트리의 루트를 관리하는 래퍼
- ForwardingNode: 리사이징 중 사용
### 동기화 전략
- ConcurrentHashMap은 여러 레벨의 동기화를 조합한다.
- CAS 연산: 빈 버킷에 첫 노드 삽입
- synchronized: 버킷 내부 수정 시 헤드 노드 락
- volatile: 배열 참조 가시성 보장
- Unsafe: 직접 메모리 접근으로 성능 최적화
- 읽기 연산은 대부분 락 없이 수행된다.
- 쓰기는 버킷 단위로만 동기화된다.
### 동적 리사이징 메커니즘
- ConcurrentHashMap의 가장 복잡한 부분은 여러 스레드가 협력해 동시에 리사이징을 수행하는 메커니즘
- 용량이 부족하면 새 배열을 할당하고, 각 스레드는 일정 범위의 버킷을 이전하는 작업을 분담
- 1. 전이 시작: 첫 번째 스레드가 새 배열을 할당하고 transferIndex를 설정한다.
- 2. 작업 분담: 각 스레드는 stride 크기만큼의 버킷을 처리하며, CAS로 작업 범위를 할당받는다.
- 3. ForwardingNode 설치: 이전 완료된 버킷에는 ForwardingNode를 배치해 새 배열로 리다이렉트한다.
- 4. 전환 완료: 모든 버킷 이전 후 테이블 참조를 원자적으로 교체한다.
- 이 과정에서 읽기 연산은 ForwardingNode를 만나면 새 배열에서 검색을 계속하므로, 리사이징 중에도 데이터 접근이 가능하다.
## 기타 동시성 라이브러리
### CopyOnWriteArrayList
- 불변 스냅샷 전략: 모든 수정 작업 시 내부 배열을 복사한다.
- 읽기는 락 없이 수행되어 매우 빠르지만, 쓰기는 비용이 크다.
- 사용 사례: 읽기가 압도적으로 많고 쓰기가 드문 경우 (예: 이벤트 리스너 목록, 설정 데이터)
- 반복자는 생성 시점의 스냅샷을 사용
- ConcurrentModificationException 발생하지 않음
- 메모리 오버헤드 고려 필요
### BlockingQueue 구현체
- 생산자-소비자 패턴: 스레드 안전한 큐로 작업을 전달한다.
- ArrayBlockingQueue:
- 고정 크기 배열 기반, ReentrantLock 사용
- FIFO 순서 보장, 용량 제한으로 배압(backpressure) 제어
- LinkedBlockingQueue:
- 연결 리스트 기반, 선택적 용량 제한
- head와 tail에 별도 락 사용
- 높은 처리량
- PriorityBlockingQueue:
- 우선순위 힙 기반, 무제한 크기
### ConcurrentLinkedQueue
- 완전 락 프리: Michael-Scott 알고리즘 기반으로 CAS만 사용하여 구현된 무제한 큐
- wait-free 읽기와 쓰기
- 높은 동시성 환경에 최적
- size() 연산은 O(n) 소요
- 약한 일관성 반복자 제공
- 활용: 메시지 큐, 작업 스케줄링, 이벤트 버스
### ConcurrentSkipListMap
- 정렬된 맵: 스킵 리스트 자료구조를 사용하여 O(logn) 성능으로 정렬된 키를 관리
- 락 프리 탐색, CAS 기반 수정
- 범위 쿼리 지원 (subMap, headMap, tailMap)
- TreeMap의 동시성 대안
- 내부 구조: 다층 연결 리스트로 빠른 탐색을 위한 인덱스 레벨 유지
## 베스트 프랙티스
### 동기화 도구 선택 기준
- 단순 카운터/플래그: Atomic 클래스 또는 volatile
- 임계 영역 보호: synchronized (간단할 때) 또는 ReentrantLock (고급 기능 필요 시)
- 리소스 제한: Semaphore
- 스레드 조율: CountDownLatch, CyclicBarrier, Phaser
### 컬렉션 선택 기준
- 맵: ConcurrentHashMap
- 정렬된 맵: ConcurrentSkipListMap
- 리스트 (읽기 위주): CopyOnWriteArrayList
- 리스트 (균형): Collections.synchronziedList
- 큐 (생산자-소비자): BlockingQueue 구현체
- 큐 (높은 동시성): ConcurrentLinkedQueue
### 성능 최적화
- 락 범위를 최소화하여 경합 감소
- 불변 객체 활용으로 동기화 회피
- 스레드 로컬 변수로 공유 상태 제거
- 락 순서를 일관되게 유지하여 데드락 방지
- 프로파일링으로 실제 병목 지점 파악
### 안티패턴
- 과도한 동기화: synchronized 남용 대신 불변 객체나 스레드 로컬을 먼저 고려
- 이중 확인 락킹 오류:
- volatile 없이 DCL 패턴을 사용하면 부분 초기화된 객체를 볼 수 있다.
- volatile을 사용하거나 LazyHolder 패턴을 활용한다.
- 복합 연산 원자성:
- get() 후 put()은 원자적이지 않다.
- ConcurrentHashMap의 computeIfAbsent() 같은 원자적 복합 메서드를 사용한다.
### 실전에서는 단순함을 우선하되, 성능이 중요한 핫스팟에서 고급 기법을 적용한다.
# 자바 동시성 컬렉션
## 동시성 컬렉션의 필요성
- 일반 컬렉션을 Collections.synchronizedList()로 래핑하는 방식은 전체 객체에 대한 락을 사용하여 성능 병목을 초래한다.
- 자바는 java.util.concurrent 패키지에 특화된 동시성 컬렉션을 제공한다.
- 동시성 컬렉션은 더욱 정교한 락킹 메커니즘과 비블로킹 알고리즘을 통해 높은 동시성과 성능을 제공한다.
### 데이터 무결성
- Race Condition을 방지하고 일관된 상태를 유지
- 동시 수정으로 인한 데이터 손실 방지
- ConcurrentModificationException 회피
- 원자적 연산 보장
### 성능 최적화
- 락 경합을 최소화하여 처리량 극대화
- 세분화된 락킹 (Lock Striping)
- 비블로킹 알고리즘 (CAS 연산)
- 읽기 작업의 락 프리 구현
### 확장성
- 스레드 수가 증가해도 안정적 성능
- Lock Contention 감소
- 병렬 처리 효율성 향상
- 대규모 시스템 구축 가능
### 동시성 컬렉션은 fail-fast 대신 weakly consistent iterator를 제공한다.
- 반복 중 컬렉션이 수정되어도 예외를 던지지 않고, 수정 전후의 요소를 보여준다.
- 실시간 시스템에서 안정성이 향상된다.
## ConcurrentHashMap 내부 구조
- HashMap의 동시성 버전이다.
- 자바 7까지는 Segment 기반 락킹을 사용했다.
- 자바 8부터는 더욱 정교한 Node 기반 락킹과 CAS(Compare-And-Swap) 연산을 결합한 구조로 재설계되었다.
### Java 8+ 구현 메커니즘
- 내부 구조:
- ConcurrentHashMap은 내부적으로 Node[] table 배열을 사용한다.
- 각 버킷은 연결 리스트 또는 트리 구조(TREEIFY_THRESHOLD=8 이상)로 관리된다.
- 락킹 전략:
- 버킷 단위 락킹: 전체 맵이 아닌 개별 버킷에만 synchronized 블록 적용
- CAS 연산: 빈 버킷에 첫 노드 추가 시 lock 없이 conpareAndSet 사용
- 읽기 락 프리: volatile 변수와 메모리 가시성 보장으로 읽기는 락 불필요
- 주요 필드:
```java
transient volatile Node<K,V>[] table;
private transient volatile int sizeCtl;
private transient volatile Node<K,V>[] nextTable;
//sizeCtl은 초기화와 리사이징을 제어하는 핵심 변수
//음수일 때는 초기화/리사이징 진행 중을 의미한다.
```
### 리사이징 프로세스
- ConcurrentHashMap의 가장 복잡한 부분은 동시 리사이징이다.
- 여러 스레드가 협럭하여 리사이징을 수행할 수 있다.
- 트리거: 로드 팩터 초과 시 transfer() 메서드 호출
- 협력 메커니즘: 각 스레드는 stride 단위로 버킷 이전 담당
- ForwardingNode: 이전 완료된 버킷에 특수 노드 배치하여 다른 스레드에게 리다이렉션 신호
- 일관성 유지: 이전 중에도 get/put 연산 정상 동작
- 성능 특성:
- Get: O(1) 평균, 락 없음
- Put: O(1) 평균, 버킷 단위 락
- Resize: 여러 스레드 협력으로 병렬화
### 최적화 포인트
- ConcurrentHashMap의 성능은 적절한 초기 용량(initialCapacity)과 로드 팩터 설정에 크게 의존한다.
- 예상 요소 수를 알고 있다면 new ConcurrentHashMap<>(expectedSize / 0.75f + 1)로 초기화하여 리사이징 오버헤드를 방지할 수 있다.
## CopyOnWriteArrayList 구현 원리
### Copy-On-Write 패턴
- CopyOnWriteArrayList는 쓰기 작업이 드물고 읽기 작업이 빈번한 시나리오에 최적화된 List 구현체다.
- 모든 수정 작업(add, set, remove)은 내부 배열의 복사본을 생성하여 수행한다.
- 이를 통해 읽기 작업의 락 프리를 달성한다.
- 내부 구조:
```java
private transient volatile Object[] array;
final transient ReentrantLock lock = new ReentrantLock();
```
- array 필드는 volatile로 선언되어 모든 스레드가 최신 버전의 배열을 볼 수 있도록 보장한다.
- 쓰기 연산 과정:
- 1. ReentrantLock 획득
- 2. 현재 배열을 새 배열로 복사
- 3. 새 배열에 변경 사항 적용
- 4. array 참조를 새 배열로 교체 (원자적)
- 5. 락 해제
### Iterator와 Snapshot
- iterator()는 생성 시점의 배열 스냅샷을 참조한다.
- 반복 중 컬렉션이 수정되어도 ConcurrentModificationException이 발생하지 않는다.
```java
public Iterator<E> iterator() {
    return new COWIterator<>(getArray(), 0);
}
```
- 적용 시나리오:
- 이벤트 리스너 목록 (추가/제거 드묾, 순회 빈번)
- 설정 데이터 (갱신 드묾, 조회 빈번)
- 캐시 무효화 목록
- 트레이드오프:
- 장점: 읽기 성능 극대화, 락 없는 순회
- 단점: 쓰기 비용 높음 (O(n) 복사), 메모리 오버헤드
- 대규모 리스트에서 빈번한 수정이 필요하다면 CopyOnWriteArrayList는 부적합하다.
- Collection.synchronizedList나 ConcurrentLinkedQueue 같은 대안을 고려한다.
## ConcurrentLinkedQueue와 비블로킹 알고리즘
- Michael-Scott 비블로킹 큐 알고리즘을 기반으로 한 무한 용량의 스레드 안전 큐다.
- 락을 전혀 사용하지 않고 CAS 연산만으로 동시성을 제어한다.
- 매우 높은 처리량과 확장성을 제공한다.
### Node 구조
- 각 노드는 volatile 필드로 item과 next를 포함한다.
```java
static class Node<E> {
    volatile E item;
    volatile Node<E> next;
}
```
### CAS 연산
- head와 tail의 원자적 업데이트를 위해 Unsafe.compareAndSwap을 사용
```java
casHead(Node<E> cmp, Node<E> val)
```
### Offer 알고리즘
- 꼬리에 새 노드를 추가하며, 실패 시 재시도하는 낙관적 동시성 제어
- slack 전략을 사용하는 알고리즘이다.
- tail을 매번 업데이트하지 않고 성능을 최적화한다.
- tail이 실제 마지막 노드보다 한두 노드 뒤처질 수 있으며, 이는 CAS 연산 횟수를 줄여준다.
```java
public boolean offer(E e) {
    final Node<E> newNode = new Node<E>(e);
    for (Node<E> t = tail, p = t;;) {
        Node<E> q = p.next;
        if (q == null) {
            //p가 마지막 노드
            if (p.casNext(null, newNode)) {
                if (p != t) //tail 업데이트 필요
                    casTail(t, newNode);
                return true;
            }
        } else if (p == q) {
            //tail이 뒤처진 경우
            p = (t != (t = tail)) ? t : head;
        } else {
            //tail을 따라가기
            p = (p != t && t != (t = tail)) ? t : q;
        }
    }
}
```
### Poll 알고리즘
- 머리에서 노드를 제거하며, item을 null로 설정하여 GC 지원
### 성능 특성과 적용
- 시간 복잡도:
- Offer: O(1) 상각 시간
- Poll: O(1) 상각 시간
- Size: O(n) 정확한 크기 계산 비용 높음
- 메모리 일관성:
- offer 이전의 모든 작업이 poll 이후 작업보다 happens-before 관계다.
- volatile 변수와 CAS가 메모리 배리어 역할을 수행한다.
- 적용 사례:
- 생산자-소비자 패턴의 작업 큐
- 비동기 이벤트 처리 파이프라인
- 락 프리 메시지 버퍼
- 주의사항:
- size() 연산이 O(n)이므로 빈번한 크기 확인은 피해야 한다.
- 대신 isEmpty()를 사용하거나 다른 메커니즘으로 크기를 추적한다.
## BlockingQueue 인터페이스와 구현체들
- BlockingQueue는 큐가 비었을 때 take() 연산을 블로킹한다.
- 큐가 가득 찼을 때 put() 연산을 블로킹한다.
- 생산자-소비자 패턴의 핵심 인터페이스다.
- 스레드 풀, 작업 스케줄링, 비동기 처리 등 다양한 동시성 프로그래밍 시나리오에 사용된다.
### ArrayBlockingQueue
- 구조: 고정 크기 배열 기반, 단일 ReentrantLock으로 보호
- 특징: FIFO 순서 보장, 공정성 선택 가능 (fair=true 시 대기 순서 보장)
- 적용: 크기가 제한된 버퍼, 백프레셔가 필요한 시나리오
```java
private final Object[] items;
private final ReentrantLock lock;
private final Condition notEmpty;
private final Condition notFull;
```
### LinkedBlockingQueue
- 구조: 연결 리스트 기반, 선택적 용량 제한, 두 개의 락 (takeLock, putLock)
- 특징: put과 take가 별도 락으로 병렬 실행 가능, 더 높은 동시성
- 적용: 대용량 작업 큐, ThreadPoolExecutor의 기본 큐
```java
private final ReentrantLock takeLock;
private final ReentrantLock putLock;
private final AtomicInteger count;
```
### PriorityBlockingQueue
- 구조: 우선순위 힙(힙 정렬) 기반, 무한 용량, 단일 ReentrantLock
- 특징: Comparator로 우선순위 결정, take는 최소값 반환
- 적용: 작업 스케줄링, 우선순위 기반 이벤트 처리
### SynchronousQueue
- 구조: 용량 0인 특수 큐, 각 put은 대응하는 take와 직접 핸드오프
- 특징: 생산자-소비자 간 직접 전달, 캐싱 없음
- 적용: Cached thread pool, 즉시 처리가 필요한 작업 전달
### DelayQueue
- 구조: Delayed 인터페이스 요소만 저장, 만료 시간 기준 정렬
- 특징: 지연된 요소는 take 시 블로킹, getDelay() 기준 우선순위
- 적용: 타이머 작업, 캐시 만료, 스케줄 기반 작업
### LinkedTransferQueue
- 구조: 듀얼 데이터 구조, 데이터 노드와 요청 노드 혼재
- 특징: transfer() 메서드로 소비자가 수신할 때까지 블로킹
- 적용: 메시지 전달 보장이 필요한 시스템
## ConcurrentSkipListMap과 SkipList 알고리즘
### SkipList 자료구조
- ConcurrentSkipListMap은 정렬된 맴의 동시성 구현이다.
- 내부적으로 SkipList(스킵 리스트) 자료구조를 사용한다.
- SkipList는 확률적 균형 트리다.
- 여러 레벨의 연결 리스트를 쌓아 올린 구조다.
- 최하위 레벨(레벨 0): 모든 요소를 포함하는 정렬된 연결 리스트
- 상위 레벨: 하위 레벨의 부분집합 (확률 p=0.5로 승격)
- 최상위 레벨: 적은 노드만 포함하여 빠른 탐색 가능
- Index 노드는 실제 데이터 Node를 기리키는 인덱스 역할을 한다.
- 수직(down)과 수평(right) 링크로 연결된다.
```java
static final class Node<K,V> {
    final K key;
    volatile Object value;
    volatile Node<K,V> next;
}
static final class Index<K,V> {
    final Node<K,V> node;
    final Index<K,V> down;
    volatile Index<K,V> right;
}
```
### 동시성 제어 메커니즘
- 검색 알고리즘:
- 1. 최상위 레벨의 head에서 시작
- 2. 현재 노드보다 크거나 같은 다음 노드를 찾을 때까지 오른쪽으로 이동
- 3. 찾으면 한 레벨 아래로 내려가 반복
- 4. 레벨 0에서 정확한 키를 찾음
- 삽입 알고리즘:
- 1. 먼저 레벨0에 노드 삽입 (CAS로 원자적 수행)
- 2. randomLevel()로 승격 레벨 결정
- 3. 각 레벨에 Index 노드 삽입 (CAS 재시도)
- 4. 실패 시 일관성 유지를 위해 재탐색
- 삭제 알고리즘:
- 논리적 삭제(value를 null로 설정)와 물리적 삭제(링크 제거)를 분리
- 동시 순회자가 일관된 상태를 볼 수 있도록 보장
- 성능 특성:
- 평균 검색/삽입/삭제: O(logn)
- 공간 복잡도: O(n) 평균
- 락 프리 읽기, 낙관적 쓰기
- TreeMap이 단일 스레드에서 약간 더 빠르다.
- 멀티스레드 환경에서는 ConcurrentSkipListMap이 압도적으로 우수
- 타임스탬프 기반 이벤트 저장소, 순위 시스템, 범위 쿼리가 필요한 캐시 등에 적합
## 동시성 컬렉션 성능 비교
- ConcurrentHashMap: 전반적으로 균형잡힌 성능
```
최적 시나리오: 빈번한 읽기와 쓰기가 혼재, 키-값 저장소
확장성: 세분화된 락킹으로 뛰어난 확장성
메모리: 합리적인 메모리 사용
```
- ConcurrentSkipListMap
```
최적 시나리오: 정렬된 순서 필요, 범위 쿼리
확장성: 락 프리 읽기, 낙관적 쓰기
메모리: 인덱스 레벨로 인한 추가 메모리
```
- CopyOnWriteArrayList: 읽기에서 압도적이지만 쓰기가 매우 느리다.
```
최적 시나리오: 읽기 중심, 드문 수정 (이벤트 리스너)
확장성: 읽기는 완벽한 확장성
메모리: 쓰기 시 전체 복사로 높은 메모리 사용
```
- ConcurrentLinkedQueue
```
최적 시나리오: 높은 처리량의 큐, 락 프리 필요
확장성: 비블로킹으로서 최고 확장성
메모리: 노드당 추가 포인터만
```
- LinkedBlockingQueue
```
최적 시나리오: 생산자-소비자, 블로킹 필요
확장성: 두 개 락으로 양방향 병렬성
메모리: 연결 리스트 오버헤드
```
## 베스트 프랙티스
- 1. 적절한 컬렉션 선택
- 워크로드 특성(읽기/쓰기 비율, 순서 필요 여부, 블로킹 필요 여부)을 분석해 최적의 컬렉션 선택
- 2. 초기 용량 설정
- 예상 크기를 알고 있다면 초기 용량을 설정하여 리사이징 오버헤드를 방지
- 3. 원자적 복합 연산 활용
- putIfAbsent, computeIfAbsent 같은 원자적 메서드를 사용해 race condition을 방지
- 4. iterator 일관성 이해
- weakly consistent iterator의 동작을 이해하고, 필요하다면 스냅샷을 생성
- 5. 성능 테스트와 프로파일링
- 실제 워크로드로 성능을 측정, 병목 지점을 프로파일링해서 최적화
### 안티패턴
- 1. 외부 동기화와 혼용
- 동시성 컬렉션은 이미 스레드 안전하므로 추가 동기화가 성능을 저하시킴
```java
//나쁜 예: 불필요한 동기화
synchronized(map) {
    map.putIfAbsent(key, value);
}
```
- 2. size()를 신뢰하는 로직
- 동시성 컬렉션의 size()는 근사값일 수 있다.
- 대신 poll()의 반환값을 확인해라
```java
//나쁜 예: 정확하지 않은 크기
if (queue.size() > 0) {
    queue.poll(); //이미 비어있을 수 있음
}
```
- 3. CopyOnWriteArrayList 오용
- 대량 추가는 addAll()을 사용하거나 다른 컬렉션 고려
```java
//나쁜 예: 빈번한 수정
for (int i = 0; i < 10000; i++) {
    list.add(i); //매번 복사 발생
}
```
- 4. 메모리 가시성 주의
- 동시성 컬렉션은 내부 상태의 가시성을 보장한다.
- 하지만 컬렉션에 저장된 객체 내부의 필드는 보장하지 않는다.
- 객체 내부 상태도 스레드 안전하게 만들거나, 불변 객체를 사용해라.
### 고급 최적화 기법
- 1. 로컬 집계 후 병합
- 스레드 로컬로 집계 후 한 번에 병합하면 경합을 줄일 수 있다.
```java
//좋은 예: ThreadLocal 집계
ThreadLocal<Map<K,V>> localMap = ...;
//작업 완료 후 ConcurrentHashMap에 병합
localMap.get().forEach(globalMap::merge);
```
- 2. 배치 연산 활용
- 개별 연산보다 배치 연산이 더 효율적
```java
//좋은 예: 배치 삽입
List<E> batch = new ArrayList<>();
//... 배치 생성
queue.addAll(batch);
```
- 3. 스트림 API와 병렬 처리
- ConcurrentHashMap의 병렬 스트림은 세그먼트 단위로 병렬 처리됨
```java
//좋은 예: 병렬 집계
Map<K, Long> counts = map.values().parallelStream().collect(groupingByConcurrent(classifier, counting()));
```