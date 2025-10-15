# 7. 동기화 - synchronized
- 여러 스레드가 접근하는 자원 = 공유 자원
- 공유 자원 예: 인스턴스 필드(멤버 변수)
- 동시성 문제 = 공유 자원에 동시 접근할 때 발생 문제
- 해결법: 공유 자원에 대한 접근을 적절하게 동기화
- 251011-java-adv1/src/sync/BankMain.java
```java
public class BankMain {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount1(1000);
        Thread t1 = new Thread(new WithdrawTask(account, 500), "t-1");
        Thread t2 = new Thread(new WithdrawTask(account, 500), "t-2");
        t1.start();
        //t1.join(); //메인 스레드가 자식 스레드의 완료를 기다리게 함
        t2.start();
        //t2.join(); //메인 스레드가 자식 스레드의 완료를 기다리게 함
        log("b=" + account.getBalance());
    }
}
/*
//주석o 실행
09:55:12.693 [      t-1] 출금 검증(r=b-a): r=500, b=1000, a=500
09:55:12.693 [     main] b=1000
09:55:12.696 [      t-1] 출금 완료(r>=0): r=500, b=500, a=500
09:55:12.693 [      t-2] 출금 검증(r=b-a): r=500, b=1000, a=500
09:55:12.696 [      t-2] 출금 완료(r>=0): r=500, b=500, a=500
//주석x 실행
09:55:57.885 [      t-1] 출금 검증(r=b-a): r=500, b=1000, a=500
09:55:57.887 [      t-1] 출금 완료(r>=0): r=500, b=500, a=500
09:55:57.888 [      t-2] 출금 검증(r=b-a): r=0, b=500, a=500
09:55:57.888 [      t-2] 출금 완료(r>=0): r=0, b=0, a=500
09:55:57.891 [     main] b=0
 */
//주석x 실행: 동기화가 아니라 순차 실행이다.
//t1이 완전히 끝난 후 t2가 시작
//동시에 실행되지 않으니 race condition이 발생할 수 없음
//BankAccount1은 여전히 thread-unsafe하다.
```
- 방법 1: synchronized 메서드
```java
@Override
public synchronized boolean withdraw(int amount) {
    int result = balance - amount;
    log("출금 검증(r=b-a): r=" + result + ", b=" + balance + ", a=" + amount);
    if (result < 0) {
        log("출금 불가(r<0): r=" + result + ", b=" + balance + ", a=" + amount);
        return false;
    }
    balance = result;
    log("출금 완료(r>=0): r=" + result + ", b=" + balance + ", a=" + amount);
    return true;
}
/*
10:09:20.515 [t-1] 출금 검증: r=500, b=1000, a=500
                   ↓ t-1이 락 보유 중
10:09:20.515 [main] b=1000  ← 아직 t-1이 완료 전
                   ↓
10:09:20.517 [t-1] 출금 완료: r=500, b=500, a=500
                   ↓ t-1이 락 해제
10:09:20.518 [t-2] 출금 검증: r=0, b=500, a=500  ← t-2가 락 획득
10:09:20.518 [t-2] 출금 완료: r=0, b=0, a=500
 */
//synchronized로 동시성 문제는 해결
```
- 동시성 문제: 여러 스레드가 balance를 동시에 읽고/쓰면 데이터 손상
```java
//synchronized 메서드
public synchronized boolean withdraw(int amount) {
    //메서드 전체가 락으로 보호됨
}
//synchronized 메서드 실제 동작
public boolean withdraw(int amount) {
    synchronized(this) { //객체(this)에 락을 걸음
        //this는 BankAccount 객체를 가리킴
        //balance 접근하는 모든 코드 보호
    }
}
//보호 대상: balance 필드 (공유 자원)
//보호 수단: 메서드에 synchronized 키워드
//실제 락: 객체(this)에 대한 락
/* 필드를 직접 보호하는 방법은 없음 */
//private synchronized int balance; //불가
//메서드로 필드 보호 가능 (메서드 전체 동기화)
//synchronized 블록으로 필드 보호 가능 (필요한 부분만 동기화)
public boolean withdraw(int amount) {
    synchronized(this) {
        balance -= amount; //이 부분만 보호
    }
}
//별도 락 객체 사용 가능 (this 대신 전용 락)
private final Object lock = new Object();
public boolean withdraw(int amount) {
    synchronized(lock) { //this 대신 전용 락
        //this는 BankAccount 객체를 가리킴
        balance -= amount;
    }
}
//메서드에 건다면 그 이유:
//balance 접근하는 모든 연산을 원자적으로 만들어야 하기 때문
@Override
public synchronized boolean withdraw(int amount) {
    int result = balance - amount; // 읽기
    if (result < 0) return false; // 검증
    balance = result; // 쓰기
    return true;
}
//잘못된 예
public boolean withdraw(int amount) {
    int result = balance - amount; //보호 안 됨
    synchronized(this) {
        balance = result; //늦음
    }
}
```
- main이 1000을 본 이유: join + 동기화 모두 필요
```java
//getBalance()가 synchronized가 아님
public int getBalance() {
    return balance;
}
/*
[t-1] withdraw() 실행 중, 락 보유
[main] getBalance() 호출 → 바로 읽음 (1000)
       ↑ 락이 필요 없어서 대기 안 함
 */
//만약 getBalance()가 synchronized라면 (1)
public synchronized int getBalance() {
    return balance;
}
/*
[t-1] withdraw() 실행 중, 락 보유
[main] getBalance() 호출 → 대기 (락이 필요함)
[t-1] balance = 500, 락 해제
[main] 락 획득, balance 읽음 (500 또는 0)
 */
//만약 getBalance()가 synchronized라면 (2)
t1.start();
t2.start();
log("b=" + account.getBalance()); //join 없음
/*
//시나리오 1: main이 가장 먼저 실행
[main] getBalance() 호출, 락 획득
[main] balance 읽음 (1000)
[main] 락 해제
[t-1] withdraw() 시작
[t-2] withdraw() 시작
//시나리오 2: t-1이 먼저, main이 두 번째
[t-1] withdraw() 시작, 락 획득
[main] getBalance() 호출 → 대기
[t-1] balance = 500, 락 해제
[main] getBalance() 실행, balance 읽음 (500)
[t-2] withdraw() 시작
//시나리오 3: 둘 다 끝난 후 main 실행
[t-1] withdraw() 완료, balance = 500
[t-2] withdraw() 완료, balance = 0
[main] getBalance() 호출, balance 읽음 (0)
 */
```
- synchronized는 동시 접근만 막고 실행 순서는 보장하지 않는다.
```
메서드 synchronized 적용o, 스레드 join 적용x
Case 1: main → t-1 → t-2
        1000   500   0
Case 2: t-1 → main → t-2
        500   500   0
Case 3: t-1 → t-2 → main
        500   0     0
모두 맞는 값이지만 어느 시점인지 다름
- synchronized가 보장하는 것
데이터 일관성 (원자성), Race condition 방지, 가시성 (최신 값)
- synchronized가 보장하지 않는 것
실행 순서, 누가 먼저 실행될지, main이 마지막에 실행
```
- 원자성(Atomicity)은 쪼갤 수 없는 하나의 작업을 의미
```java
//중간 과정 없이 완전히 실행되거나, 아예 실행 안 되거나를 의미
balance = balance - 500;
//1. balance 값을 읽음  (READ: 메모리 → CPU LOAD)
//2. 500을 뺀다. (COMPUTE: CPU SUB)
//3. 결과를 balance에 저장 (WRITE: CPU → 메모리 STORE)
//1, 2, 3 각각은 원자적이지만, 1 + 2 + 3은 원자적으로 실행x
/* 원자성 판단 기준 */
int x = 5; //원자적o: 단순 대입 (32비트 이하)
long x = 5L; //원자적x: 64비트 변수는 두 번에 쓰임
x++; //원자적x: 읽기 + 증가 + 쓰기
x = x + 1; //원자적x: 읽기 + 더하기 + 쓰기
balance = 100; //원자적o: 단순 대입
balance = balance - 500; //원자적x: 읽기 + 빼기 + 쓰기
//코드 한 줄이라고 원자성을 보장하지 않는다.
//CPU 연산이 여러 개여서, 동기화 위한 락을 걸어야 한다.
//단일 스레드일 때도 동기화 비용이 발생하게 된다.
//정확성보다 속도가 중요한 경우 동기화를 안 할 때도 있다.
//자바는 명시적 동기화를 원칙으로 한다. (성능 중시)
/* (1) Atomic 클래스로 원자성 보장 */
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet(); //원자적으로 실행
/* (2) Lock으로 원자성 보장 */
Lock lock = new ReentrantLock();
lock.lock();
try {
count++; //원자적으로 실행
        } finally {
        lock.unlock();
}
/* (3) synchronized로 원자성 보장 */
public synchronized boolean withdraw(int amount) {
    int result = balance - amount; //┐
    if (result < 0) {              //│ 이 전체가
        return false;              //│ 하나의 원자적 작업
    }                              //│
    balance = result;              //┘
    return true;
}
//[t-1] withdraw() 전체 실행 (중단 불가) → balance: 1000 → 500
//[t-2] withdraw() 전체 실행 (중단 불가) → balance: 500 → 0
//각 코드 라인에 락을 건다는 의미가 아님
//메서드 실행 전체를 하나의 스레드만 실행하도록 보호한다는 의미
public boolean withdraw(int amount) {
    객체_락_획득(this);
    try {
        int result = balance - amount;
        if (result < 0) {
            return false;
        }
        balance = result;
        return true;
    } finally {
        객체_락_해제(this);
    }
}
//JVM 바이트코드 (개념적):
//    monitorenter //락 획득
//    메서드 본문 실행 (중간에 다른 스레드 못 들어옴)
//    monitorexit //락 해제
//[Thread-2의 상태 변화]
//    NEW
//     ↓ start()
//    RUNNABLE (CPU 실행 대기/실행 중)
//     ↓ withdraw() 호출
//     ↓ monitorenter 시도
//     ↓ 락 획득 실패
//    BLOCKED
//     - CPU 할당 안 받음
//     - OS Wait Queue에 대기
//     - PC 상태 저장됨
//     - 아무 코드도 실행 안 함
//     ↓ (Thread-1이 락 해제)
//     ↓ OS가 깨움 (notify)
//    RUNNABLE
//     - CPU 스케줄링에 다시 포함
//     - CPU 할당받으면 실행 재개
//     - monitorenter 성공
//     - withdraw() 본문 실행
```
- 읽기/쓰기 원자성 - 타입에 따라 다름
```
//JVM 32비트 기준
byte - 8비트 - 원자성o
short - 16비트 - 원자성o
int - 32비트 - 원자성o
float - 32비트 - 원자성o
boolean - 원자성o
char - 16비트 - 원자성o
참조 - 32 - 원자성o
long - 64비트 - 원자성x
double - 64비트 - 원자성x
//boolean을 1바이트(8비트)로 저장함
//cpu, jvm이 메모리를 바이트 단위로 접근하기 때문
//boolean 배열 제외하고는 boolean 메모리 레이아웃 명시x
//jvm이 32비트로 실행되면, cpu가 64비트라도 객체 참조 32비트
//jvm이 자체 메모리 모델을 갖고 있어서다.
//jvm이 32비트로 실행된다. = 컴퓨터 메모리 4GB까지만 접근
//jvm이 64비트로 실행되는 경우 있다. = 수백 GB까지 접근
//jvm이 64비트고, cpu가 64비트면:
//    64비트 연산 원자적이다.
//    참조 크기는 64비트 또는 32비트(Compressed Oops)다.
```
- 읽기/쓰기 원자성x - 해결 방법
```java
//JVM 명세상 long/double은 두 번에 나눠 처리
/* 1. volatile - 가시성만 보장 */
private volatile long balance = 1000L;
//메인 메모리 직접 접근
long value = balance; //balance 읽기 원자적o
balance = 2000L; //balance 쓰기 원자적o
balance++; //복합 연산은 원자적x
/* 2. synchronized - 동기화(상호 배제) */
private long balance = 1000L;
public synchronized void increment() {
    balance++; //복합 연산도 원자적o
    //가시성 보장
}
/* 3. AtomicLong - 락 프리(lock-free) 원자성 */
private AtomicLong balance = new AtomicLong(1000L);
balance.incrementAndGet(); //복합 연산도 원자적o
//가시성 보장
//원리: Compare-And-Swap (하드웨어 명령어)
/*
특성 - volatile - synchronized - AtomicLong
원자성 - 단순 읽기/쓰기만 - 블록 전체 - CAS 연산
복합 연산 - x - o - o
락 사용 - x - o (모니터 락) - x (CAS)
성능 - 가장 빠름 - 느림 (락 경쟁) - 빠름
대기 방식 - x - blocking - busy-waiting
 */
```