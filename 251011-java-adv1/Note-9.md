# 동기화 다시
## 자바 메모리 모델
- 멀티스레드 환경에서 변수의 읽기와 쓰기가 어떻게 동작하는지를 정의하는 규칙의 집합
```
JVM 메모리 구조: 자바 메모리 모델의 기초는 JVM의 런타임 데이터 영역입니다. 
- 스택 영역: 스레드마다 생성되며, 메소드 호출에 따른 지역 변수, 매개변수, 반환 값 등이 저장됩니다. 
- 힙 영역: 객체가 생성되는 공간이며, JVM의 모든 스레드가 공유합니다. 
- 메서드 영역: 클래스 수준의 데이터, 즉 static 변수, 상수, 메서드 정보 등이 저장됩니다. 
스레드 간 상호작용:
- 모든 스레드는 공유하는 힙 영역을 통해 데이터를 읽고 씁니다. 
- 스레드는 자신이 가진 스택 영역의 지역 변수를 사용하며, 이 변수들은 힙 영역에 있는 객체를 참조할 수 있습니다. 
- 메서드 영역에 있는 static 변수 역시 모든 스레드에 의해 공유됩니다. 
메모리 모델의 목적:
- 여러 스레드가 동시에 메모리에 접근할 때 발생하는 데이터 일관성 문제를 해결하기 위한 규칙을 제공합니다. 
- 컴파일러나 하드웨어의 최적화로 인해 발생하는 예측 불가능한 동작을 막고, 프로그래머가 스레드 간의 데이터 동기화를 명확하게 제어할 수 있도록 돕습니다. 
```
### 메인 메모리와 워킹 메모리
- JMM은 모든 변수가 메인 메모리에 저장되며, 각 스레드는 자신만의 워킹 메모리(로컬 캐시)를 가진다고 정의한다.
- 스레드는 워킹 메모리를 통해 변수에 접근하며, 메인 메모리와의 동기화는 명시적으로 이뤄져야 한다.
- 워킹 메모리는 CPU 레지스터와 캐시의 추상화다.
- 변수 복사본이 워킹 메모리에 저장된다.
- 스레드 간 직접적인 워킹 메모리 접근이 불가하다.
### Happens-Before 관계
- JMM에서 메모리 작업의 순서를 보장하는 원칙이다.
- 만약 작업A가 작업B보다 happens-before 관계에 있다면, A의 결과는 B에게 보이는 것이 보장된다.
- 프로그램 순서 규칙: 단일 스레드 내에서 앞선 작업이 뒤따르는 작업보다 먼저 발생한다.
- 모니터 락 규칙: unlock 작업이 이후의 lock 작업보다 먼저 발생한다.
- volatile 변수 규칙: 쓰기가 읽기보다 먼저 발생한다.
- 스레드 시작 규칙: Thread.start()가 스레드 내 모든 작업보다 먼저 발생한다.
- 스레드 종료 규칙: 스레드의 모든 작업이 Thread.join() 반환보다 먼저 발생한다.
### 가시성 문제
- 한 스레드에서 수정한 변수의 값이 다른 스레드에게 즉시 보이지 않는 현상이다.
- CPU 캐시와 컴파일러 최적화로 인해 발생한다.
- 각 스레드가 변수의 복사본을 로컬 캐시에 보관한다.
- 한 스레드의 쓰기 작업이 메인 메모리에 즉시 반영되지 않는다.
- 다른 스레드는 오래된 값을 읽게 된다.
- volatile 키워드로 해결이 가능하다.
### 원자성 문제
- 작업이 중간에 중단되지 않고 완전히 실행되거나, 전혀 실행되지 않아야 하는 특성이다.
- 복합 연산에서 문제가 발생한다.
- count++ 같은 연산은 실제로 읽기-수정-쓰기의 3단계이다.
- 여러 스레드가 동시 실행 시 경쟁 조건이 발생한다.
- 데이터 무결성이 손상될 가능성 있다.
- synchronized나 Atomic 클래스로 해결이 가능하다.
### 순서 재배치 문제
- 컴파일러와 프로세서는 성능 최적화를 위해 명령어 순서를 재배치할 수 있다.
- 단일 스레드에서는 문제가 없지만 멀티 스레드에서 예상치 못한 결과를 초래할 수 있다.
- 컴파일러 재배치: 소스 코드와 바이트 코드의 순서 차이
- 프로세서 재배치: CPU의 명령어 파이프라인 최적화
- 메모리 시스템 재배치: 캐시와 쓰기 버퍼의 영향
- 메모리 배리어로 순서를 보장할 수 있다.
## 자바 동기화
### 동기화 목적
- 자바 동기화란 멀티스레드 환경에서 공유 자원에 대한 안전한 접근을 보장하는 메커니즘이다.
- 여러 스레드가 동시에 같은 데이터를 수정하려고 할 때 발생하는 경쟁 조건, 데드락, 가시성 문제 등을 방지한다.
- 상호 배제 (Mutual Exclusion)
- 동기화의 기본 목적은 임계 영역에 한 번에 하나의 스레드만 접근하도록 보장하는 것이다.
- 이를 통해 데이터의 일관성을 유지하고 경쟁 조건을 방지할 수 있다.
- 가시성 보장
- 동기화는 한 스레드가 수정한 값이 다른 스레드에게 올바르게 보이도록 보장한다.
- 메모리 배리어를 통해 CPU 캐시를 플러시하고 메인 메모리와 동기화한다.
- 순서 보장
- 동기화 블록 내의 작업들은 정해진 순서대로 실행된다.
- happens-before 관계를 통해 다른 스레드에게도 그 순서가 보장된다.
- 올바른 동기화를 통해 멀티스레드 프로그램의 동작을 예측할 수 있다. (100%는 아님)
- 상황에 맞는 적절한 도구를 선택한다.
### 동기화 메커니즘 종류
- synchronized 키워드
- 자바의 가장 기본적인 동기화 메커니즘이다.
- 메서드나 블록 수준에서 모니터 락을 사용한다.
- 사용이 간편하고 JVM이 자동으로 락을 관리하지만, 유연성이 제한적이다.
- 재진입이 가능하고 암묵적으로 락을 획득하고 해제한다.
- Volatile 변수
- 변수를 메인 메모리에서 직접 읽고 쓰도록 강제하며 가시성을 보장한다.
- 단일 변수의 읽기/쓰기에 대한 원자성을 제공하지만, 복합 연산에는 적합하지 않다.
- 락보다 가볍고 성능이 우수하지만 제한적인 기능을 제공한다.
- java.util.concurrent 패키지
- 고수준의 동시성 유틸리티를 제공하는 패키지다.
- Lock 인터페이스, Atomic 클래스, CountDownLatch, Semaphore, ConcurrentHashMap 등 다양한 도구를 포함한다.
- 더 세밀한 제어와 향상된 성능을 제공한다.
- 타임아웃, 인터럽트 가능한 락 등 고급 기능을 지원한다.
- 선택 기준
- 단순성: synchronized가 가장 간단하고 안전
- 성능: 경합이 적으면 volatile, 많으면 Lock
- 유연성: Lock 인터페이스가 가장 다양한 기능 제공
- 원자성: 단순 카운터는 Atomic 클래스
- 성능 고려사항
- 동기화는 필연적으로 성능 오버헤드를 발생시킨다.
- 락 경합, 컨텍스트 스위칭, 메보리 배리어 등이 주요 비용 요소이다.
- 따라서 동기화 범위를 최소화하고, 적절한 도구를 선택하고, 불필요한 동기화는 피한다.
### 자바 대 다른 언어 동기화
- 자바
- 내장된 sunchronized 키워드
- 모든 객체가 모니터 역할 가능
- JVM이 자동으로 락 관리
- wait/notify 메커니즘
- java.util.concurrent 패키지
- C++
- std::mutex와 std::lock_guard
- 명시적 락 객체 생성 필요
- RAII 패턴으로 자동 해제
- 조건 변수(condition_variable)
- 더 저수준의 제어 가능
- Python
- GIL(Global Interpreter Lock)
- threading.Lock과 RLock
- with문을 통한 컨텍스트 관리
- 멀티프로세싱으로 GIL 우회
- asyncio로 비동기 처리
### 락 메커니즘
- 비관적 락 (Pessimistic Lock)
- 자바의 synchronized와 Lock이 사용하는 방식
- 항상 충돌이 발생할 것으로 가정하고 먼저 락을 획득한다.
- 구현이 간단하고 데이터 일관성을 확실히 보장한다.
- 락 대기로 인한 성능 저하 발생 가능성 있다.
- 낙관적 락 (Optimistic Lock)
- 충돌이 드물 것으로 가정하고 락 없이 작업한 후, 커밋 시점에 충돌을 확인한다.
- 버전 번호나 타임스탬프를 사용하고, 데이터베이스에서 자주 사용된다.
- 경합이 적은 환경에서 성능이 우수하다.
- 충돌 시 재시도 로직이 필요하다.
- 무락 동기화 (Lock-Free)
- Atomic 클래스가 사용하는 CAS(Compare-And-Swap) 방식이다.
- 하드웨어 수준의 원자적 연산을 활용하여 락 없이 동기화한다.
- 락 오버헤드가 없어 성능이 뛰어나다.
- ABA 문제와 복잡한 구현이라는 단점 있다.
- 각각의 락 메커니즘은 특정 상황에서 최적의 성능을 발휘한다.
- 경합 빈도, 임계 영역의 크기, 필요한 일관성 수준 등을 고려해서 적절한 방식을 선택한다.
### 자바 동기화 주의할 점
- 데드락 (Deadlock)
- 두 개 이상의 스레드가 서로가 가진 락을 기다리며 무한정 대기하는 상황이다.
- 락 획득 순서를 일관되게 유지하고, 타임아웃을 설정하고, 락 순서 그래프를 분석해서 예방한다.
- 라이브락 (Livelock)
- 스레드들이 활성 상태이지만 실제 진전이 없는 상황이다.
- 서로 양보하려다 계속 재시도만 하게 된다.
- 랜덤 백오프나 우선순위 부여로 해결한다.
- 기아 상태 (Starvation)
- 특정 스레드가 계속 락을 획득하지 못하고 실행 기회를 얻지 못하는 상황이다.
- 공정한 락(Fair Lock) 사용이나 우선순위 조정으로 방지할 수 있다.
- 성능 저하
- 과도한 동기화는 병렬성을 해치고 성능을 저하시킨다.
- 동기화 범위를 최소화하고, 읽기 전용 데이터는 동기화하지 않고, 락 분할(Lock Striping) 기법을 활용해야 한다.
- 동기화 범위를 가능한 한 작게 유지
- 공유 상태를 최소화하고 불변 객체를 활용
- 락 획득 순서를 문서화하고 일관되게 유지
- 동기화 블록 내에서 외부 메서드 호출x
- 스레드 안전성을 문서화하고 테스트
### 메모리 배리어와 가시성
- 동기화의 핵심은 메모리 배리어(Memory Barrier)를 통한 가시성 보장이다.
- 메모리 배리어는 CPU에게 캐시를 플러시하고 메인 메모리와 동기화하도록 지시하는 특수한 명령어다.
- 쓰기 배리어
- 쓰기 작업 후 캐시의 변경 사항을 메인 메모리에 반영한다.
- volatile 쓰기나 락 해제 시 실행된다.
- 읽기 배리어
- 읽기 작업 전 로컬 캐시를 무효화하고 메인 메모리에서 최신 값을 로드한다.
- volatile 읽기나 락 획득 시 실행된다.
- 완전 배리어
- 쓰기와 읽기 배리어를 모두 수행하여 완전한 동기화를 보장한다.
- synchronized 블록의 진입과 탈출 시 사용된다.
- happens-before 규칙의 실제 적용
- happens-before 관계는 메모리 가시성을 보장하는 추상적 개념이다.
- 예를 들어, 스레드A가 synchronized 블록을 빠져나간 후 스레드B가 같은 락으로 synchronized 블록에 진입하면, A의 모든 메모리 작업이 B에 보인다.
- 모니터 해제(unlock) 시: 해당 스레드가 수정한 모든 변수를 메인 메모리로 플러시
- 모니터 획득(lock) 시: 캐시를 무효화하고 메인 메모리에서 최신 값을 페치
```java
//스레드 A
synchronized(lock) {
    sharedData = 42;
    //... 다른 변수들도 수정
    otherData = 100;
} //← 모니터 해제 시점: 쓰기 배리어 (store barrier)
//CPU 캐시의 모든 변경사항을 메인 메모리로 플러시
//스레드 B  
synchronized(lock) { //← 모니터 획득 시점: 읽기 배리어 (load barrier)
    //메인 메모리로부터 최신 값을 가져옴
    int value = sharedData; //42를 읽음
    int other = otherData;  //100을 읽음
}
```
- volatile 쓰기 전의 모든 메모리 연산은 volatile 쓰기보다 먼저 완료되며, 그 중 쓰기 연산들만 함께 메인 메모리에 플러시된다.
- volatile 읽기는 해당 변수를 메인 메모리에서 가져오며, volatile 읽기 후의 모든 메모리 연산은 volatile 읽기 이전으로 재배치될 수 없다.
```java
int a = 0, b = 0;
volatile int v = 0;
//스레드 A
a = 1;          //①
b = 2;          //②
v = 3;          //③ volatile 쓰기 - 배리어 삽입됨
                //①②③ 모두 메인 메모리로 플러시
//스레드 B
int r1 = v;     //④ volatile 읽기 - 배리어 삽입됨
                //메인 메모리에서 모든 값 페치
int r2 = a;     //⑤ r2 = 1 (최신 값 보장)
int r3 = b;     //⑥ r3 = 2 (최신 값 보장)
```
- 메모리 가시성: synchronized O (블록 내 모든 변수)
- 메모리 가시성: volatile O (해당 변수 + 간접 순서 보장)
- 원자성: synchronized O (복합 연산 가능)
- 원자성: volatile X (단일 읽기/쓰기만)
- 상호 배제: synchronized O (한 번에 하나의 스레드)
- 상호 배제: volatile X
- 명시성: synchronized O (명시적 보호)
- 명시성: volatile X (간접적 보호)
- 성능: synchronized 상대적으로 무거움 (락 경쟁)
- 성능: volatile 가벼움
- Double-Checked Locking(더블 체크 락킹) 패턴
- 멀티스레드 환경에서 Singleton(단일 인스턴스)을 안전하고 효율적으로 생성하기 위해 자주 사용
- Instance 객체를 한 번만 생성하고, 이후에는 이미 만들어진 객체를 바로 반환
- 여러 스레드가 동시에 getInstance()를 호출하더라도, instance는 딱 한 번만 생성
```java
private volatile Instance instance;
public Instance getInstance() {
    if (instance == null) {                //(1) 첫 번째 검사 (락 없이)
        synchronized (this) {              //(2) 동기화 블록 진입
            if (instance == null) {        //(3) 두 번째 검사 (락 안에서)
                instance = new Instance(); //(4) 실제 생성
            }
        }
    }
    return instance;
}
//락을 최소화하면서도, 객체가 두 번 생성되는 일을 막기 위해 두 번 확인
//(1) 락을 피하려고 (이미 만들어졌다면 락 불필요)
//(3) 정말 안 만들어졌을 때만 생성 (경쟁 상태 방지)
```
- 더블 체크 락킹에서 volatile의 역할
- volatile은 메모리 가시성과 명령어 재정렬 방지를 보장
- `instance = new Instance();`는 3단계
- 1. 메모리 공간 확보 -> 2. 생성자 호출 -> 3. instance가 그 주소를 가리키게 함
- JVM이 최적화 과정에서 2번과 3번을 순서를 바꾸는 경우가 있다.
- 다른 스레드가 `instance != null`을 보고 아직 완전히 초기화되지 않은 객체를 사용할 수도 있다.
- volatile은 이런 재정렬을 막아서, 모든 스레드가 완전한 초기화가 끝난 객체만 보도록 한다.
## synchronized
- synchronized는 가장 기본적인 동기화 키워드
- 메서드나 코드 블록을 원자적으로 실행하도록 보장한다.
- 모니터 락을 사용해 상호 배제를 구현한다.
- JVM이 자동으로 락의 획득과 해제를 관리하므로 데드락 위험이 적고 안전하다.
- 재진입 가능: 같은 스레드가 이미 획득한 락을 다시 획득 가능
- 가시성 보장: 메모리 배리어를 통한 변경 사항 가시화
- 순서 보장: happens-before 관계 성립
- 자동 관리: JVM이 락 해제를 보장 (예외 발생 시에도)
- 성능 고려사항: 성능 오버헤드가 있다.
- JVM이 바이어스드 락, 경량 락, 중량 락 등의 최적화 기법으로 성능을 개선한다.
- 높은 경합 상황에서는 여전히 병목이 될 수 있다.
### 메서드 수준 동기화
- 메서드 전체를 synchronized로 선언하여 해당 메서드가 실행되는 동안 다른 스레드의 접근을 차단한다.
- 인스턴스 메서드: this 객체의 모니터 락 사용
- 정적 메서드: 클래스 객체의 모니터 락 사용
- 간결하지만 메서드 전체가 임계 영역이 된다.
```java
public synchronized void increment() {
    count++;
}
public static synchronized void staticMethod() {
    //클래스 락 사용
}
```
### 블록 수준 동기화
- 코드의 특정 부분만 동기화하여 락의 범위를 최소화하고 성능을 향상시킨다.
- 임의의 객체를 락으로 사용 가능하다.
- 세밀한 락 제어가 가능하다.
- 동기화 범위 최소화로 성능이 향상된다.
```java
public void method() {
    //비동기화 코드
    synchronized(this) {
        //임계 영역
        criticalOperation();
    }
    //비동기화 코드
}
```
### 모니터 락 메커니즘
- synchronized는 모니터라는 개념을 기반으로 동작한다.
- 자바의 모든 객체는 내부적으로 모니터를 가지고 있으며, 이는 상호 배제와 조건 동기화를 제공한다.
- 1. 락 획득 시도
- 스레드가 synchronized 블록에 진입하려고 할 때 해당 객체의 모니터 락을 획득하려고 시도한다.
- 2. 락 획득 성공
- 락이 사용 가능하면 즉시 획득하고 임계 영역에 진입한다.
- 락을 획득한 스레드는 소유자가 된다.
- 3. 락 대기
- 다른 스레드가 이미 락을 보유 중이면 현재 스레드는 대기 큐에 들어가 블록된다.
- 4. 임계 영역 실행
- 락을 획득한 스레드는 synchronized 블록 내의 코드를 실행한다.
- 다른 스레드는 접근할 수 없다.
- 5. 락 해제
- 블록 실행 완료 또는 예외 발생 시 자동으로 락을 해제하고, 대기 중인 스레드 중 하나를 깨운다.
### 재진입 메커니즘
- synchronized는 재진입 가능(Reentrant)하다.
- 이미 락을 보유한 스레드가 같은 락을 다시 요청하면 즉시 진입할 수 있다.
- 이는 재귀 호출이나 중첩된 동기화 메서드 호출을 가능하게 한다.
- 내부적으로 락은 카운터를 유지한다.
- 진입할 때마다 증가하고 탈출할 때마다 감소한다.
- 카운터가 0이 되어야 완전히 해제된다.
```java
public synchronized void outer() {
    inner(); //재진입 가능
}
public synchronized void inner() {
    //이미 락을 보유 중이므로 블록되지 않음
}
```
### wait/notify 메커니즘
- 모니터는 조건 동기화도 지원한다.
- wait() 메서드는 락을 해제하고 대기 상태로 들어간다.
- notify()는 대기 중인 스레드 하나를 깨운다.
- 이 메커니즘은 생산자-소비자 패턴 같은 복잡한 동기화 시나리오를 구현할 수 있도록 한다.
```java
synchronized(lock) {
    while (!condition) {
        lock.wait(); //락 해제 후 대기
    }
    //작업 수행
    lock.notifyAll(); //대기자 깨우기
}
```
### 컴파일 시 바이트코드
- monitorenter: 락 획득 시도
- monitorexit: 락 해제
- 예외 처리를 위한 이중 monitorexit
- try-finally 구조로 안정성 보장
```java
//자바 소스 코드
public void method() {
    synchronized(this) {
        count++;
    }
}
//생성되는 바이트코드 (단순화)
public void method();
0: aload_0         //this를 스택에 로드
1: dup             //참조 복제
2: astore_1        //로컬 변수에 저장
3: monitorenter    //모니터 진입 (락 획득)
4: aload_0         //count 증가 작업
5: dup
6: getfield count
9: iconst_1
10: iadd
11: putfield count
14: aload_1
15: monitorexit    //정상 종료 시 락 해제
16: goto 24
19: astore_2
20: aload_1
21: monitorexit    //예외 발생 시에도 락 해제
22: aload_2
23: athrow
24: return
```
### JVM의 락 최적화 전략
- 바이어스드 락
- 대부분의 경우 하나의 스레드만 락을 사용하므로, 특정 스레드에게 락을 편향시켜 획득/해제 비용을 제거한다.
- CAS 연산 없이 간단한 비교만으로 락을 획득한다.
- 경량 락
- 경합이 발생하면 바이어스를 해제하고 경량 락으로 전환한다.
- CAS를 사용해 스핀락처럼 동작하며, 짧은 대기 시간에 효율적이다.
- 중량 락
- 경합이 심하거나 대기 시간이 길면 OS 수준의 뮤텍스를 사용하는 중량 락으로 전환된다.
- 컨텍스트 스위칭 비용이 있지만 CPU를 낭비하지 않는다.
- 락 인플레이션
- JVM은 락의 상태를 동적으로 변경한다.
- 바이어스드 락 -> 경량 락 -> 중량 락으로 인플레이션되며, 상황에 따라 최적의 전략을 선택한다.
- 이러한 최적화로 synchronized는 대부분의 경우 효율적이다.
### 락 해제 보장과 예외 처리
- synchronized는 예외가 발생하더라도 항상 락이 해제된다.
- 바이트코드 수준에서 try-finally 구조로 구현되어 있다.
```java
//개발자가 작성한 코드
synchronized(lock) {
    if (error) {
        throw new Exception();
    }
    doWork();
}
//JVM이 보장하는 동작
monitorenter(lock);
try {
    if (error) {
        throw new Exception();
    }
    doWork();
} finally {
    monitorexit(lock);
}
```
- ReentrantLock을 사용할 때는 명시적으로 try-finally를 작성해야 한다.
```java
Lock lock = new ReentrantLock();
lock.lock();
try {
    doWork();
} finally {
    lock.unlock(); //필수
}
//finally 블록을 빼면 락이 해제되지 않아 데드락이 발생할 수 있다.
```
### 락 분할과 락 스트라이핑
- 락 분할 (Lock Splitting)
- 하나의 락으로 여러 독립적인 상태를 보호하는 대신, 각 상태마다 별도의 락을 사용해 경합을 줄인다.
```java
//Before: 하나의 락
synchronized(this) {
    updateX();
    updateY();
}
//After: 락 분할
synchronized(lockX) {
    updateX();
}
synchronized(lockY) {
    updateY();
}
```
- 락 스트라이핑 (Lock Striping)
- 데이터를 여러 세그먼트로 나누고 각 세그먼트마다 별도의 락을 사용한다.
- ConcurrentHashMap이 이 기법을 사용한다.
```java
//16개의 세그먼트
final Object[] locks = 
new Object[16];
void put(K key, V value) {
    int hash = key.hashCode();
    int stripe = hash % 16;
    synchronized(locks[stripe]) {
        //해당 세그먼트만 락
    }
}
```
### 동기화 블록 최적화 기법
- 동기화 범위 최소화: 임계 영역을 최대한 작게 유지하여 락 보유 시간을 줄인다.
- 읽기-쓰기 분리: 읽기 작업이 많다면 ReadWriteLock을 고려한다.
- 불변 객체 활용: 가능하면 불변 객체를 사용하여 동기화 필요성을 제거한다.
- 스레드 로컬 사용: 각 스레드가 독립적인 복사본을 가지도록 하여 공유를 피한다.
- 락 없는 알고리즘: 가능한 경우 Atomic 클래스나 ConcurrentHashMap 같은 락 없는 자료구조를 활용한다.
## LockSupport
### 저수준 스레드 차단과 깨우기
- LockSupport는 java.util.concurrent.locks 패키지의 기본 빌딩 블록이다.
- 스레드를 차단하고 깨우는 가장 기본적인 메커니즘을 제공한다.
- synchronized의 wait/notify보다 더 유연하고 정밀한 제어가 가능하다.
- 현대적인 동시성 유틸리티의 토대가 된다.
- 1. park() / unpark()
- 스레드를 안전하게 차단하고 깨우는 핵심 메서드다.
- park()는 permit이 없으면 차단
- unpark()는 permit을 제공하여 차단 해제
- permit은 누적되지 않음 (최대 1개)
```java
//현재 스레드 차단
LockSupport.park();
LockSupport.park(blocker);
//특정 스레드 깨우기
LockSupport.unpark(thread);
```
- 2. 시간 제한 차단
- 타임아웃을 지정해 무한 대기를 방지한다.
- 지정된 시간 후 자동으로 재개
- 정밀한 시간 제어 가능
- 타임아웃 패턴 구현에 유용
```java
//나노초 단위 대기
LockSupport.parkNanos(nanos);
//특정 시점까지 대기
LockSupport.parkUntil(deadline)
```
- 3. 인터럽트 처리
- park()는 인터럽트에 응답하지만, 예외를 발생시키지 않는다.
- 인터럽트 시 즉시 반환
- 예외 없이 상태만 설정
- 명시적 확인 필요
```java
LockSupport.park();
if (Thread.interrupted()) {
    //인터럽트 처리
}
```
- LockSupport는 java.util.concurrent의 모든 락과 동기화 도구의 기반이다.
- AbstractQueuedSynchronizer(AQS)는 LockSupport를 사용하여 ReentrantLock, Semaphore, CountDownLatch 등을 구현한다.
```java
//간단한 세마포어 구현
class SimpleSemaphore {
    private volatile boolean available;
    void acquire() {
        while (!available) {
            LockSupport.park(this);
        }
        available = false;
    }
    void release() {
        available = true;
        LockSupport.unpark(
                waitingThread
        );
    }
}
```
### wait/notify와의 차이점
- 락 필요: wait/notify - 필수 (synchronized)
- 락 필요: LockSupport - 불필요
- 순서 문제: wait/notify - notify 먼저 호출 시 손실
- 순서 문제: LockSupport - unpark 먼저 가능
- 대상 지정: wait/notify - 불가능 (임의)
- 대상 지정: LockSupport - 특정 스레드 지정
- 예외 처리: wait/notify - InterruptedException
- 예외 처리: LockSupport - 반환 후 확인
### Permit 기반 메커니즘
- LockSupport는 각 스레드와 연결된 binary semaphore(permit)를 사용한다.
- permit은 0 또는 1의 값을 가지며, 누적되지 않는다.
- 1. Permit 사용 가능
- 스레드가 park()를 호출하면 permit을 즉시 소비하고 계속 실행된다.
- 2. 차단 상태
- permit이 없으면 스레드는 차단되어 대기한다.
- 3. Unpark 호출
- 다른 스레드가 unpark()를 호출하면 permit이 제공된다.
- 4. 실행 재개
- 차단된 스레드는 permit을 받아 깨어나고 실행을 계속한다.
- 순서 독립성
- LockSupport의 핵심 장점은 unpark()가 park()보다 먼저 호출되어도 정상 동작한다는 것이다.
- 이는 wait/notify의 "missed signal" 문제를 해결한다.
- notify()가 wait()보다 먼저 호출되면 신호가 손실되지만, unpark()는 permit을 미리 설정할 수 있다.
```java
//unpark가 먼저 호출
LockSupport.unpark(thread); 
//permit = 1
//나중에 park 호출
LockSupport.park(); 
//permit을 소비하고 즉시 반환
```
- Blocker 객체
- park(Object blocker)는 디버깅과 모니터링을 위한 blocker 객체를 지정할 수 있다.
- 스레드 덤프나 프로파일러에서 스레드가 어떤 객체를 기다리는지 명확히 볼 수 있다.
```java
LockSupport.park(this);
//스레드 덤프에서 확인 가능
"Thread-1"
    waiting on <0x12345678>
    (MyLock)
```
### 인터럽트와 스퓨리어스 웨이크업
- 인터럽트 처리
- park()는 인터럽트 시 즉시 반환하지만 InterruptedException을 던지지 않는다.
- 인터럽트 상태만 설정하고 반환한다.
```java
LockSupport.park();
if (Thread.interrupted()) {
    //인터럽트됨
    handleInterrupt();
}
```
- 스퓨리어스 웨이크업
- park()는 아무 이유 없이 깨어날 수 있다. (spurious wakeup)
- 따라서 항상 루프 내에서 조건을 재확인해야 한다.
```java
while (!condition) {
    LockSupport.park();
}
//조건 만족 확인
```
### 네이티브 메서드와 Unsafe 클래스
- LockSupport는 내부적으로 sun.misc.Unsafe 클래스의 네이티브 메서드를 호출해 구현된다.
- 이는 JVM과 OS 기능에 직접 접근한다.
- LockSupport는 매우 가볍고 효율적이다.
- synchronized나 ReentrantLock보다 오버헤드가 적다.
- 직접적인 OS 기본 요소를 사용하므로 컨텍스트 스위칭 비용이 최소화된다.
- 너무 저수준이어서 대부분의 경우 고수준 추상화를 사용하는 것이 좋다.
- Unsafe.park() 메서드
- Unsafe.park()는 네이티브 메서드로, C/C++로 구현되어 있다.
- 운영체제의 뮤텍스나 조건 변수를 사용한다.
- Linux: pthread_mute와 pthread_cond
```c++
//C++ 의사 코드
void Parker::park() {
    mutex_lock(&_mutex);
    --_counter; //permit 소비
    if (_counter <= 0) {
        //대기
        pthread_cond_wait(&_cond, &_mutex);
    }
    mutex_unlock(&_mutex);
}
void Parker::unpark() {
    mutex_lock(&_mutex);
    ++_counter; //permit 제공
    if (_counter > 1) _counter = 1; //최대 1
    pthread_cond_signal(&_cond);
    mutex_unlock(&_mutex);
}
```
- Windows: CRITICAL_SECTION와 Event 객체
```c++
//C++ 의사 코드
void Parker::park() {
    EnterCriticalSection(&_lock);
    --_counter;
    if (_counter <= 0) {
        LeaveCriticalSection(&_lock);
        //Event 대기
        WaitForSingleObject(_event, INFINITE);
        EnterCriticalSection(&_lock);
    }
    LeaveCriticalSection(&_lock);
}
```
- Solaris: mutex_t와 cond_t
- Parker 객체
- 각 스레드는 내부적으로 Parker 객체를 가진다.
- 이것이 실제 permit과 대기 메커니즘을 관리한다.
```java
//LockSupport의 실제 구현 (단순화)
public class LockSupport {
    private static final Unsafe U = Unsafe.getUnsafe();
    public static void park() {
        U.park(false, 0L);
    }
    public static void park(Object blocker) {
        Thread t = Thread.currentThread();
        setBlocker(t, blocker);
        U.park(false, 0L);
        setBlocker(t, null);
    }
    public static void parkNanos(long nanos) {
        if (nanos > 0) U.park(false, nanos);
    }
    public static void unpark(Thread thread) {
        if (thread != null) U.unpark(thread);
    }
}
```
### AbstractQueuedSynchronizer(AQS)와의 관계
- LockSupport는 AQS의 핵심 구성 요소이다.
- AQS는 LockSupport를 사용하여 대기 큐를 관리하고 스레드를 차단/재개한다.
- 1. 락 획득 실패 시 스레드를 큐에 추가
- 2. LockSupport.park()로 스레드를 차단
- 3. 락 해제 시 후속 노드를 unpark()
- 4. 깨어난 스레드가 다시 락 획득을 시도
- 이러한 패턴은 ReentrantLock, Semaphore, CountDownLatch, ReadWriteLock 등 모든 동시성 유틸리티에 공통적으로 적용된다.
```java
//AQS의 acquireQueued 메서드
final boolean acquireQueued(Node node, int arg) {
    boolean interrupted = false;
    try {
        for (;;) {
            final Node p = node.predecessor();
            if (p == head && tryAcquire(arg)) {
                setHead(node);
                return interrupted;
            }
            if (shouldParkAfterFailedAcquire(p, node)) {
                //LockSupport 사용
                LockSupport.park(this);
                if (Thread.interrupted()) interrupted = true;
            }
        }
    } catch (Throwable t) {
        cancelAcquire(node);
        throw t;
    }
}
```
### 실전 활용 패턴
- LockSupport는 강력하지만 저수준 도구
- 가능하면 ReentrantLock, Semaphore 같은 고수준 추상화를 사용
- 직접 사용할 때는 스퓨리어스 웨이크업, 인터럽트 처리, 메모리 가시성 등을 신중히 고려
- 1. 커스텀 동기화 도구 구현
- LockSupport를 사용해 특정 요구사항에 맞는 동기화 도구를 직접 구현할 수 있다.
```java
public class CustomBarrier {
    private volatile int count;
    private final Queue waiters = new ConcurrentLinkedQueue<>();
    public void await() {
        waiters.add(Thread.currentThread());
        while (count < threshold) {
            LockSupport.park(this);
        }
        waiters.remove(Thread.currentThread());
    }
    public void signal() {
        count++;
        if (count >= threshold) {
            waiters.forEach(LockSupport::unpark);
        }
    }
}
```
- 2. 비블로킹 알고리즘
- 락 없는 자료구조에서 충돌 시 짧은 시간 대기를 위해 사용된다.
```java
public void optimisticOperation() {
    for (int retries = 0; ; retries++) {
        if (tryOperation()) {
            return;
        }
        if (retries > MAX_RETRIES) {
            //짧은 대기
            LockSupport.parkNanos(1000);
        }
    }
}
```
- 3. 이벤트 기반 프로그래밍
- 특정 이벤트가 발생할 때까지 스레드를 효율적으로 대기시킨다.
```java
public class EventWaiter {
    private volatile boolean eventOccurred;
    private Thread waiter;
    public void waitForEvent() {
        waiter = Thread.currentThread();
        while (!eventOccurred) {
            LockSupport.park(this);
        }
    }
    public void signalEvent() {
        eventOccurred = true;
        if (waiter != null) {
            LockSupport.unpark(waiter);
        }
    }
}
```
## ReentrantLock
- ReentrantLock은 synchronized의 모든 기능을 제공하면서도 더 많은 유연성과 제어 기능을 제공하는 명시적 락이다.
- java.util.concurrent.locks 패키지의 핵심 클래스다.
- 복잡한 동기화 시나리오에서 강력한 도구가 된다.
- 1. 명시적 락 제어
- lock()과 unlock()을 명시적으로 호출하여 락의 획득과 해제를 직접 제어한다.
```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    //임계 영역
    criticalSection();
} finally {
    lock.unlock(); //필수
}
```
- 2. 타임아웃 지원
- 지정된 시간 동안만 락 획득을 시도하고, 실패하면 포기할 수 있다.
```java
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try {
        //작업 수행
    } finally {
        lock.unlock();
    }
} else {
    //타임아웃 처리
}
```
- 3. 인터럽트 가능
- 락 대기 중 인터럽트에 응답할 수 있어 데드락 상황을 탈출할 수 있다.
```java
try {
    lock.lockInterruptibly();
    try {
        //작업
    } finally {
        lock.unlock();
    }
} catch (InterruptedException e) {
    //인터럽트 처리
}
```
- 4. 공정성 모드
- 대기 큐의 순서대로 락을 부여하여 기아 상태를 방지할 수 있다.
```java
//공정한 락
ReentrantLock fairLock = new ReentrantLock(true);
//비공정한 락 (기본)
ReentrantLock unfairLock = new ReentrantLock(false);
```
- 주요 메서드
```java
lock() //블로킹 방식으로 락 획득
unlock() //락 해제 (반드시 finally에서)
tryLock() //즉시 시도하고 결과 반환
tryLock(time, unit) //타임아웃 지정
lockInterruptibly() //인터럽트 가능한 획득
isHeldByCurrentThread() //현재 스레드가 소유 중인지 확인
getHoldCount() //재진입 횟수 조회
isLocked() //락 상태 확인
```
- synchronized와 비교
- 타임아웃: synchronized - 불가
- 타임아웃: ReentrantLock - 가능
- 인터럽트: synchronized - 제한적
- 인터럽트: ReentrantLock - 완전 지원
- 공정성: synchronized - 보장 안 함
- 공정성: ReentrantLock - 선택 가능
- 조건 변수: synchronized - 1개
- 조건 변수: ReentrantLock - 다중 가능
- 사용 편의성: synchronized - 간단
- 사용 편의성: ReentrantLock - 복잡