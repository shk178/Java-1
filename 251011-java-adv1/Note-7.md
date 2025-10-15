- Java의 Thread.State enum은 6가지 상태를 정의
```java
public enum State {
    NEW,           // 생성됨, 아직 시작 안됨
    RUNNABLE,      // 실행 가능 (실행 중 또는 실행 대기)
    BLOCKED,       // 모니터 락 대기
    WAITING,       // 무한정 대기
    TIMED_WAITING, // 제한 시간 대기
    TERMINATED     // 종료됨
}
```
## wait/notify의 스레드 상태 영향
- 상태 전이 다이어그램
```
RUNNABLE (synchronized 블록 내)
    ↓ wait() 호출
WAITING (또는 TIMED_WAITING)
    ↓ notify()/notifyAll() 호출됨
BLOCKED (모니터 락 재획득 시도)
    ↓ 락 획득 성공
RUNNABLE (synchronized 블록 재진입)
```
- wait() 호출 시:
```java
synchronized (obj) {
    //현재: RUNNABLE
    obj.wait();
    //-> WAITING 상태로 전이
    //모니터 락 자동 해제
}
```
- 상태: RUNNABLE → WAITING
- 락 상태: 모니터 락 해제됨
- 위치: 객체의 wait set에 들어감
- wait(long timeout) 호출 시:
```java
synchronized (obj) {
    obj.wait(1000); //1초 대기
    //-> TIMED_WAITING 상태로 전이
}
```
- 상태: RUNNABLE → TIMED_WAITING
- 타임아웃 후: 자동으로 깨어남 (notify 없이도)
- notify() 호출 시 (대기 스레드 관점):
```
//스레드 A가 wait set에서 선택됨
//WAITING -> BLOCKED (락 경쟁)
//락 획득 후 -> RUNNABLE
```
- 상태: WAITING → BLOCKED → RUNNABLE
- 중요: notify 직후 바로 실행되는 게 아님
- 이유: 모니터 락을 재획득해야 함
- BLOCKED 상태의 중요성
```java
Thread A                    Thread B
synchronized(obj) {         synchronized(obj) {
    obj.notify();               //wait() 중
    //A가 아직 락 보유        
    //시간 소모 작업...       
}                           } //<- 이 시점에 B가 RUNNABLE로
//A가 락 해제                //BLOCKED -> RUNNABLE
```
- notify()를 받은 스레드는 즉시 WAITING에서 BLOCKED로 변경
- 하지만 notify()를 호출한 스레드가 synchronized 블록을 나갈 때까지 대기
- 이후 락 획득 경쟁에서 이기면 RUNNABLE로 전이
## LockSupport의 스레드 상태 영향
- 상태 전이 다이어그램
```
RUNNABLE
    ↓ park() 호출
WAITING (또는 TIMED_WAITING)
    ↓ unpark() 호출 또는 인터럽트
RUNNABLE (즉시, BLOCKED 없음)
```
- park() 호출 시:
```java
//현재: RUNNABLE
LockSupport.park();
//-> WAITING 상태로 전이
//permit이 없으면 블로킹
```
- 상태: RUNNABLE → WAITING
- 락 상태: 락과 무관 (락 해제 안 함)
- Permit: 소비됨
- parkNanos(long nanos) 호출 시:
```java
LockSupport.parkNanos(1000000000L); //1초
//-> TIMED_WAITING 상태로 전이
```
- 상태: RUNNABLE → TIMED_WAITING
- 타임아웃 후: 자동으로 RUNNABLE로 복귀
- unpark(Thread) 호출 시:
```java
//다른 스레드에서
LockSupport.unpark(targetThread);
//targetThread: WAITING -> RUNNABLE (즉시)
```
- 상태: WAITING → RUNNABLE (직접 전이)
- BLOCKED 상태 없음: 모니터 락과 무관하므로
- 즉시 실행 가능: 스케줄러가 스케줄하면 바로 실행
- wait/notify와의 핵심 차이점
```java
//wait/notify 케이스
Thread threadA = new Thread(() -> {
    synchronized (lock) {
        lock.wait(); //WAITING
        //notify 받음 -> BLOCKED (락 재획득 대기)
        //락 획득 -> RUNNABLE
    }
});
//LockSupport 케이스
Thread threadB = new Thread(() -> {
    LockSupport.park(); //WAITING
    //unpark 받음 -> RUNNABLE (즉시)
    //BLOCKED 상태를 거치지 않음
});
```
- LockSupport의 장점:
- BLOCKED 상태를 거치지 않음
- 더 빠른 깨어남 가능
- 스레드 덤프에서 상태 파악이 더 명확
## ReentrantLock + Condition의 스레드 상태 영향
- 상태 전이 다이어그램
```
RUNNABLE (락 획득 상태)
    ↓ condition.await() 호출
WAITING (또는 TIMED_WAITING)
    ↓ condition.signal() 호출됨
WAITING (락 재획득 큐에서 대기)
    ↓ 락 획득 성공
RUNNABLE
```
- await() 호출 시:
```java
lock.lock();
try {
    //현재: RUNNABLE
    condition.await();
    //-> WAITING 상태로 전이
    //락 자동 해제
} finally {
    lock.unlock();
}
```
- 상태: RUNNABLE → WAITING
- 락 상태: ReentrantLock 해제됨
- 내부: LockSupport.park() 사용
- await(long time, TimeUnit unit) 호출 시:
```java
condition.await(1, TimeUnit.SECONDS);
//-> TIMED_WAITING 상태로 전이
```
- 상태: RUNNABLE → TIMED_WAITING
- signal() 호출 시 (대기 스레드 관점):
```java
//스레드가 condition 큐에서 sync 큐로 이동
//WAITING 상태 유지
//락 획득 시도 시작
//락 획득 후 -> RUNNABLE
/*
//내부 동작 상세
[Condition Queue]          [Sync Queue]
     (대기)      signal()     (락 대기)
    WAITING    --------->    WAITING
                             (LockSupport로 구현)
                                  ↓ 락 획득
                              RUNNABLE
 */
```
- await() 호출 → Condition Queue에 진입 → WAITING
- signal() 받음 → Sync Queue로 이동 → 여전히 WAITING
- 락 획득 성공 → RUNNABLE
- wait/notify와의 차이점
```
//wait/notify: BLOCKED 상태 사용
Thread.State: WAITING -> BLOCKED -> RUNNABLE
//Condition: WAITING 상태 유지
Thread.State: WAITING -> WAITING -> RUNNABLE
```
- 스레드 덤프에서의 차이:
```
//wait()로 대기 중
"Thread-1" #12 prio=5 os_prio=0 tid=0x... nid=0x... 
in Object.wait() [0x...]
   java.lang.Thread.State: WAITING (on object monitor)
//notify() 받고 락 대기 중  
"Thread-1" #12 prio=5 os_prio=0 tid=0x... nid=0x...
waiting for monitor entry [0x...]
   java.lang.Thread.State: BLOCKED (on object monitor)
//condition.await()로 대기 중
"Thread-2" #13 prio=5 os_prio=0 tid=0x... nid=0x...
waiting on condition [0x...]
   java.lang.Thread.State: WAITING (parking)
//signal() 받고 락 대기 중
"Thread-2" #13 prio=5 os_prio=0 tid=0x... nid=0x...
waiting on condition [0x...]
   java.lang.Thread.State: WAITING (parking)
```
## 상태 비교

| 메커니즘 | 대기 진입 | 대기 중 상태 | 깨어남 후 상태 | 실행 재개 |
|---------|----------|------------|---------------|----------|
| wait() | RUNNABLE→WAITING | WAITING | BLOCKED | 락 획득 후 RUNNABLE |
| wait(timeout) | RUNNABLE→TIMED_WAITING | TIMED_WAITING | BLOCKED | 락 획득 후 RUNNABLE |
| park() | RUNNABLE→WAITING | WAITING | RUNNABLE | 즉시 |
| parkNanos() | RUNNABLE→TIMED_WAITING | TIMED_WAITING | RUNNABLE | 즉시 |
| await() | RUNNABLE→WAITING | WAITING | WAITING | 락 획득 후 RUNNABLE |
| await(timeout) | RUNNABLE→TIMED_WAITING | TIMED_WAITING | WAITING | 락 획득 후 RUNNABLE |

## 인터럽트의 영향
- wait/notify
```java
synchronized (obj) {
    try {
        obj.wait(); //WAITING
        //인터럽트 발생 시 -> RUNNABLE
        //InterruptedException 던짐
    } catch (InterruptedException e) {
        //현재 상태: RUNNABLE
        //interrupted flag는 클리어됨
    }
}
```
- 상태: WAITING → RUNNABLE
- 예외: InterruptedException 발생
- 플래그: 인터럽트 플래그 자동 클리어
- LockSupport
```java
LockSupport.park(); //WAITING
//인터럽트 발생 시 -> RUNNABLE
//예외 없음, 그냥 반환
if (Thread.interrupted()) {
    //인터럽트 플래그 확인 및 클리어
}
```
- 상태: WAITING → RUNNABLE
- 예외: 없음
- 플래그: 인터럽트 플래그 유지 (직접 확인 필요)
- Condition
```java
lock.lock();
try {
    condition.await(); //WAITING
    //인터럽트 발생 시 -> RUNNABLE
    //InterruptedException 던짐
} catch (InterruptedException e) {
    //현재 상태: RUNNABLE
} finally {
    lock.unlock();
}
```
- 상태: WAITING → RUNNABLE
- 예외: InterruptedException 발생
- 락: 예외 발생 전 락 재획득됨
## 실전 디버깅
- 데드락 의심으로 스레드 덤프 분석
```
//wait() 사용 시
"Producer" BLOCKED (on object monitor)
"Consumer" WAITING (on object monitor)
-> BLOCKED 스레드가 누구를 기다리는지 확인 필요
//LockSupport 사용 시
"Producer" WAITING (parking)
"Consumer" WAITING (parking)
-> 누가 누구를 unpark해야 하는지 로직 확인
```
- 성능 분석
```
//wait/notify: BLOCKED 시간이 길 수 있음
WAITING 100ms -> BLOCKED 50ms -> RUNNABLE
//LockSupport: BLOCKED 없음
WAITING 100ms -> RUNNABLE (즉시)
//Condition: WAITING 유지하며 락 대기
WAITING 100ms -> WAITING (락 대기) 20ms -> RUNNABLE
```
- 상태 확인 코드
```java
//디버깅용: 현재 스레드 상태 확인
Thread current = Thread.currentThread();
Thread.State state = current.getState();
System.out.println("Current state: " + state);
//다른 스레드 상태 모니터링
Thread.State otherState = otherThread.getState();
if (otherState == Thread.State.BLOCKED) {
    System.out.println("스레드가 모니터 락 대기 중");
} else if (otherState == Thread.State.WAITING) {
    System.out.println("스레드가 신호 대기 중");
}
```
- 상태 전이 최적화
```java
//비효율적인 코드:
synchronized (lock) {
    lock.notify();
    //오랜 작업 수행
    heavyComputation(); //깨어난 스레드는 BLOCKED 상태로 대기
}
//효율적인 코드:
synchronized (lock) {
    lock.notify();
} //빨리 락 해제
heavyComputation(); //락 밖에서 수행
```
## 상태 관점에서의 선택 기준
- wait/notify: BLOCKED 상태를 이해하고 받아들일 수 있다면 간단하고 효과적
- LockSupport: BLOCKED 상태 없이 가장 빠른 전이가 필요하면 사용
- ReentrantLock + Condition: 복잡한 동기화는 필요하지만 BLOCKED를 피하고 싶을 때
- BLOCKED 상태 사용 여부
```
wait/notify: 사용 (모니터 락 재획득 시)
LockSupport: 사용 안 함
Condition: 사용 안 함 (WAITING 유지)
```
- 깨어나는 속도
```
LockSupport가 가장 빠름 (즉시 RUNNABLE)
Condition이 그 다음
wait/notify가 가장 느림 (BLOCKED 거침)
```
- 스레드 덤프 가독성
```
wait/notify: WAITING과 BLOCKED로 명확히 구분
LockSupport: 항상 WAITING (parking)
Condition: 항상 WAITING (parking)
```