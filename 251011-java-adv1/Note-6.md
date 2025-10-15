- 공유 자원 (Shared Resource)
- 여러 스레드가 동시에 접근할 수 있는 자원
- 변수: balance, count, list 등
- 파일: 여러 스레드가 같은 파일을 읽고 쓰기
- 데이터베이스: 여러 스레드가 같은 테이블 접근
- 객체: 여러 스레드가 공유하는 객체
```java
public class Example {
    private int balance;      //공유 자원
    private List<String> log; //공유 자원
}
```
- 임계 영역 (Critical Section)
- 공유 자원에 접근하는 코드 영역
- 특히, 동시에 실행되면 문제가 생기는 코드 부분
```java
public class BankAccount {
    private int balance; //공유 자원 (데이터)
    //임계 영역 (코드)
    public void withdraw(int amount) {
        balance = balance - amount;
    }
    //이것도 임계 영역 (코드)
    public int getBalance() {
        return balance;
    }
    //임계 영역 아님 (공유 자원 접근 안 함)
    public void printMessage() {
        System.out.println("Hello");
    }
}
//여러 스레드가 같은 BankAccount 객체를 참조하면
//그 객체는 공유 자원
BankAccount account = new BankAccount1(1000);
Thread t1 = new Thread(() -> account.withdraw(500));
Thread t2 = new Thread(() -> account.withdraw(300));
Thread t3 = new Thread(() -> account.getBalance());
//account 객체 = 공유 자원
//BankAccount 객체 (공유 자원)
//  ↓
//  └─ balance 필드 (공유 자원의 일부)
//       ↓
//       └─ withdraw() 메서드 (임계 영역 - 공유 자원 접근 코드)
```
- 모든 객체는 내부에 자기만의 락(lock)을 가진다.
- intrinsic lock (또는 monitor lock)이라고 한다.
- 객체 헤더에 숨겨져 있다.
- 락 변경: 획득/해제만 가능
- 락 역할: 동시 접근 제어
- 락 대기로 인해 스레드 상태가 BLOCKED가 된다.
```
//락은 synchronized/Lock API로 관리된다.
//스레드는 JVM/OS 스케줄러로 관리된다.
[애플리케이션 레이어]
├─ 락 (Lock): 자바 동기화 메커니즘
│   └─ synchronized, ReentrantLock 등
│
[JVM/OS 레이어]  
└─ 스레드 상태 (Thread State): 실행 스케줄링
    └─ NEW, RUNNABLE, BLOCKED, WAITING, TERMINATED
//BLOCKED 상태
//    한 스레드가 다른 스레드가 보유 중인 모니터 락을 얻기 위해 기다리고 있을 때
//    JVM은 해당 스레드를 BLOCKED 상태로 표시
//    이 상태는 synchronized 키워드를 사용하는 코드에서만 발생
//WAITING 상태
//    wait() 또는 join() 호출로 무기한 대기할 때
//    모니터 락이 아니라 notify를 기다리는 것
//TIMED_WAITING 상태
//    sleep(), wait(timeout), join(timeout) 등으로 시간 제한 대기할 때
```
- 스레드가 synchronized 블록에 진입하려면 해당 락이 있어야 한다.
```java
public class BankAccount1 {
    private int balance;
    //이 메서드는 이 객체(this)의 락을 사용
    public synchronized void withdraw(int amount) {
        balance -= amount;
    }
    //이것과 동일한 의미:
    public void withdraw2(int amount) {
        synchronized(this) { //이 객체의 락을 명시적으로 사용
            balance -= amount;
        }
    }
}
BankAccount account = new BankAccount1(1000);
//Thread A
account.withdraw(500); //account 객체의 락을 획득
//Thread B
account.withdraw(300); //account 객체의 락을 기다림 (blocked)
```
- static synchronized는 다른 락 사용
```java
public class BankAccount1 {
    private int balance;
    private static int totalAccounts;
    //인스턴스 메서드: 이 객체(this)의 락 사용
    public synchronized void withdraw(int amount) {
        balance -= amount;
    }
    //static 메서드: Class 객체의 락 사용
    public static synchronized void incrementTotal() {
        totalAccounts++;
    }
}
//서로 다른 락이므로 동시 실행 가능:
account.withdraw(500); //account 객체의 락
BankAccount1.incrementTotal(); //BankAccount1.class 객체의 락
```
- 커스텀 락 객체도 가능
```java
public class BankAccount1 {
    private int balance;
    private final Object lock = new Object(); //전용 락 객체
    public void withdraw(int amount) {
        synchronized(lock) { //커스텀 락 사용
            balance -= amount;
        }
    }
}
```
- 스레드는 객체에게 락을 획득하고 반납
- 락은 객체의 일부이므로, 스레드는 그 객체에게 락을 빌리고 돌려주는 것
- static synchronized는 Class 객체의 락을 사용
```java
public class BankAccount1 {
    private static int totalAccounts;
    //클래스 락 사용
    public static synchronized void incrementTotal() {
        totalAccounts++;
    }
}
//내부적으로는:
synchronized(BankAccount1.class) { //Class 객체의 락
    totalAccounts++;
}
```
- 락의 소유권
```java
Object lock = new Object();
//Thread A
synchronized(lock) {
    //Thread A가 lock 객체의 락을 획득
    System.out.println("Thread A가 락 소유");
} //Thread A가 lock 객체에게 락을 반납
//Thread B
synchronized(lock) {
    //이제 Thread B가 lock 객체의 락을 획득
    System.out.println("Thread B가 락 소유");
}
```
- 락을 획득하는 순서는 보장되지 않는다.
- Synchronized를 사용하면 성능이 낮다.
- 메서드 보다는 꼭 필요한 코드 구간에 블록으로 사용한다.
```java
public class BankAccount3 implements BankAccount {
    private int balance;
    public BankAccount3(int balance) {
        this.balance = balance;
    }
    //방법 2: Synchronized Blocks
    @Override
    public boolean withdraw(int amount) {
        int r;
        int b;
        //최소한의 임계 영역만 보호
        synchronized (this) {
            b = balance;
            r = b - amount;
            if (r < 0) {
                log("출금 불가(r<0): r=" + r + ", b=" + b + ", a=" + amount);
                return false;
            }
            balance = r; //balance 쓰기는 반드시 synchronized 안에
        }
        //성공 로그는 밖에서
        log("출금 완료(r>=0): r=" + r + ", b=" + b + ", a=" + amount);
        return true;
    }
    @Override
    public int getBalance() {
        synchronized (this) {
            return balance;
        }
    }
}
/*
13:08:16.913 [      t-1] 출금 완료(r>=0): r=500, b=1000, a=500
13:08:16.913 [      t-2] 출금 완료(r>=0): r=0, b=500, a=500
13:08:16.918 [     main] b=0 //join() 적용됨
 */
```
- 지역 변수는 다른 스레드와 공유되지 않는다.
- final은 변경 불가라서 안전한 공유 자원이다.
- synchronized 자동 잠금 해제가 편리하다.
- synchronized 공정성x: 락 획득 순서가 보장 안 됨
- synchronized 무한 대기: 락이 풀릴 때까지 대기한다.
- 인터럽트도 안 걸린다.
- 인터럽트는 상태를 직접 바꾸지 않는다.
- 대신 특정 상태에서 예외를 발생시켜 흐름을 바꾼다.
- BLOCKED 상태에서는 인터럽트가 무시되고 락을 얻을 때까지 기다린다.
- WAITING이나 TIMED_WAITING 상태에서는 예외가 발생하고 스레드가 깨어난다.
## wait/notify (전통적 방식)
- wait()과 notify()는 Java 1.0부터 제공된 Object 클래스의 메서드
- 모든 객체가 내재적으로 가지고 있는 모니터 락(monitor lock)을 기반으로 동작
- 모니터 락 기반: synchronized 블록 내에서만 사용 가능
- 객체 단위 동기화: 모든 객체가 wait set을 가짐
- 암묵적 락: 명시적인 락 객체가 필요 없음
- 단순한 API: 기본적인 대기/통지 메커니즘 제공
```java
스레드 A (대기)                    스레드 B (통지)
synchronized(obj) {              synchronized(obj) {
    while(!condition) {              //작업 수행
        obj.wait();                  condition = true;
    }                                obj.notify();
    //작업 수행                    }
}
//간단하고 직관적인 사용법
//JVM 레벨에서 최적화됨
//추가 import 불필요
//반드시 synchronized 블록 내에서 사용해야 함
//spurious wakeup(허위 깨움) 처리 필요
//타임아웃 없는 notify는 신호 손실 가능
//하나의 조건 변수만 사용 가능
//InterruptedException 처리 필요
```
## LockSupport (저수준 API)
- LockSupport는 Java 5에서 도입된 저수준 스레드 블로킹 유틸리티
- java.util.concurrent.locks 패키지의 기반 클래스
- Permit 기반: 각 스레드는 하나의 permit을 가짐
- 락 불필요: synchronized나 Lock 없이 사용 가능
- 스레드 직접 제어: 특정 스레드를 지정하여 깨울 수 있음
- 파킹(Parking): 스레드를 대기 상태로 만드는 메커니즘
- Permit 메커니즘:
- Permit은 이진 세마포어(0 또는 1)처럼 동작
- unpark() 호출 시 permit이 제공됨
- park() 호출 시 permit이 있으면 즉시 반환, 없으면 대기
- Permit은 누적되지 않음 (최대 1개)
```java
//현재 스레드를 블로킹
LockSupport.park();
LockSupport.parkNanos(long nanos);
LockSupport.parkUntil(long deadline);
//특정 스레드를 깨움
LockSupport.unpark(Thread thread);
//락 없이 사용 가능
//unpark()를 park() 전에 호출 가능 (신호 손실 방지)
//Spurious wakeup이 발생하지 않음
//특정 스레드를 직접 지정하여 깨울 수 있음
//InterruptedException 없음 (인터럽트 시 그냥 반환)
//저수준 API로 사용이 까다로움
//조건 체크 로직을 직접 구현해야 함
//잘못 사용하면 데드락 가능
//고수준 추상화 부족
```
## ReentrantLock + Condition (고급 방식)
- ReentrantLock은 Java 5에서 도입된 명시적 락 구현체
- Condition은 이와 함께 사용되는 조건 변수
- LockSupport를 기반으로 구현
- 명시적 락: 락의 획득과 해제를 명시적으로 제어
- 다중 조건 변수: 하나의 락에 여러 Condition 생성 가능
- 공정성 제어: 공정(fair) 또는 비공정(non-fair) 락 선택 가능
- 고급 기능: 타임아웃, 인터럽트 가능한 락 획득 등
- wait/notify와의 차이점:
- 다중 조건: 여러 Condition 객체로 서로 다른 조건 대기 가능
- 명시적 제어: try-finally로 락 해제 보장
- 유연한 락: tryLock(), lockInterruptibly() 등 제공
- 공정성: 대기 순서대로 락 획득 가능 (공정 모드)
```java
ReentrantLock lock = new ReentrantLock();
Condition condition = lock.newCondition();
lock.lock();
try {
    while (!conditionMet) {
        condition.await(); //wait()와 유사
    }
    //작업 수행
} finally {
    lock.unlock();
}
//다른 스레드에서
lock.lock();
try {
    conditionMet = true;
    condition.signal(); //notify()와 유사
    //또는 condition.signalAll();
} finally {
    lock.unlock();
}
//가장 유연하고 강력한 동기화 메커니즘
//여러 조건 변수로 복잡한 동기화 시나리오 구현 가능
//타임아웃, 인터럽트 가능한 대기 지원
//공정성 정책 선택 가능
//락 획득 시도 여부 확인 가능
//가장 복잡한 API
//명시적으로 unlock() 호출 필요 (누락 시 데드락)
//synchronized보다 오버헤드가 약간 높을 수 있음
//코드가 장황해질 수 있음
```
```
wait/notify 사용:
간단한 생산자-소비자 패턴
단일 조건 변수로 충분한 경우
레거시 코드와의 호환성 필요
synchronized와 자연스럽게 통합되는 경우
LockSupport 사용:
커스텀 동기화 도구 구현
최고 성능이 필요한 경우
프레임워크나 라이브러리 개발
락 없이 스레드 제어가 필요한 경우
ReentrantLock + Condition 사용:
여러 조건 변수 필요
타임아웃이나 인터럽트 가능한 락 필요
공정성이 중요한 경우
락 획득 시도가 필요한 경우
복잡한 동기화 시나리오
//기본은 synchronized + wait/notify: 간단한 경우 충분하고 검증됨
//고급 기능 필요 시 ReentrantLock + Condition: 여러 조건, 타임아웃, 공정성 등
//프레임워크 개발 시 LockSupport: 커스텀 동기화 도구 구현
//복잡도: wait/notify < ReentrantLock < LockSupport
//유연성: LockSupport > ReentrantLock > wait/notify
//안전성: ReentrantLock > wait/notify > LockSupport
//대부분의 애플리케이션 코드: ReentrantLock + Condition 또는 wait/notify
//고성능 라이브러리: LockSupport
//레거시 시스템: wait/notify
//새로운 프로젝트에서 복잡한 동기화: ReentrantLock + Condition
```
## 생산자-소비자 패턴 비교
- wait/notify 버전:
```java
class Buffer {
    private Queue<Integer> queue = new LinkedList<>();
    private int capacity;
    public synchronized void produce(int value) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();
        }
        queue.add(value);
        notifyAll();
    }
    public synchronized int consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        int value = queue.remove();
        notifyAll();
        return value;
    }
}
```
- ReentrantLock + Condition 버전:
```java
class Buffer {
    private Queue<Integer> queue = new LinkedList<>();
    private int capacity;
    private ReentrantLock lock = new ReentrantLock();
    private Condition notFull = lock.newCondition();
    private Condition notEmpty = lock.newCondition();
    public void produce(int value) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await();
            }
            queue.add(value);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    public int consume() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            int value = queue.remove();
            notFull.signal();
            return value;
        } finally {
            lock.unlock();
        }
    }
}
```
- LockSupport 버전 (단순화):
```java
class Buffer {
    private Queue<Integer> queue = new LinkedList<>();
    private int capacity;
    private Thread producer;
    private Thread consumer;
    public void produce(int value) {
        while (queue.size() == capacity) {
            LockSupport.park();
        }
        synchronized (this) {
            queue.add(value);
        }
        LockSupport.unpark(consumer);
    }
    public int consume() {
        while (queue.isEmpty()) {
            LockSupport.park();
        }
        int value;
        synchronized (this) {
            value = queue.remove();
        }
        LockSupport.unpark(producer);
        return value;
    }
}
```
- 내부 구현 관계:
```
LockSupport (기반 레이어)
    ↓ 사용
AbstractQueuedSynchronizer (AQS)
    ↓ 사용
ReentrantLock
    ↓ 생성
Condition
ReentrantLock과 Condition은 내부적으로 LockSupport를 사용
AbstractQueuedSynchronizer(AQS)가 중간 추상화 계층 제공
wait/notify는 독립적인 JVM 네이티브 구현
```