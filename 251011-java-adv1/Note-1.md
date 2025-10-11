# 2. 프로세스와 스레드 소개
- (1) 멀티태스킹: 하나의 CPU 코어로 여러 프로그램 실행
- CPU는 초당 수십억 번 이상 연산을 수행
- 여러 프로그램 코드들을 번갈아 수행하면
- 사용자에게 프로그램들이 동시에 실행되는 것처럼 보인다.
- 번갈아 수행 = 실행 시간 분할 = 시분할 = Time Sharing
- 어떤 프로그램이 얼마만큼 실행될지 = OS의 스케줄링
- 시분할 + 우선순위 + 최적화 + 스케줄링 ...으로 수행한다.
- (2) 멀티프로세싱: CPU 코어가 여러 개
- 멀티태스킹은 OS SW 관점, 멀티프로레싱은 CPU HW 관점
- (3) 프로세스와 스레드
- 프로그램은 실행 전 파일이다.
- 프로그램 실행 시 프로세스가 만들어진다.
- 운영체제 안에서 실행 중인 프로그램 = 프로세스
- 프로그램 = 클래스 같다.
- 프로세스 = 실행 중인 프로그램의 인스턴스 같다.
- 각 프로세스는 독립적인 메모리 공간 갖는다.
```
프로세스: 운영체제 안에 있다.
- (코드, 데이터, 힙)<-스레드들이 공유하는 메모리
- (스레드 1개 이상)<-스레드마다 스택
```
- 프로세스는 독립적인 메모리 공간 갖는다.
- 프로세스 간에 메모리 간섭x (직접 접근 불가)
- 프로세스는 1개 이상 스레드를 포함한다.
- 스레드 간에는 코드, 데이터, 힙 공유한다.
- 프로세스보다 스레드 생성 및 관리가 쉽다.
- 프로세스 = 실행 환경, 자원 제공하는 컨테이너 역할
- 스레드 = CPU 사용해 프로세스의 코드를 하나씩 실행
```
워드 프로그램 - 프로세스A
- 스레드1: 문서 편집
- 스레드2: 자동 저장
- 스레드3: 맞춤법 검사
유튜브 - 프로세스B
- 스레드1: 영상 재생
- 스레드2: 댓글
```
- (4) 스레드와 스케줄링
- 단일 코어 스케줄링
- 운영체제: 내부에 스케줄링 큐 가짐
- 각각의 스레드는 스케줄링 큐에서 대기
- 큐(b2, b1, a1) -> 코어1()
- OS는 스레드a1을 큐에서 꺼내서 코어1로 실행
- 큐(b2, b1) -> 코어1(a1)
- OS는 a1을 멈추고 큐에 넣고, b1을 꺼내서 실행
- 큐(a1, b2) -> 코어1(b1)
- OS는 b1을 멈추고 큐에 넣고, b2를 꺼내서 실행
- 큐(b1, a1) -> 코어1(b2)
- 반복
- 멀티 코어 스케줄링: 물리적 동시 실행 가능
- 큐(b2, b1, a1) -> 코어1(), 코어2()
- 큐(b2) -> 코어1(a1), 코어2(b1)
- 큐(a1) -> 코어1(b2), 코어2(b1)
- 반복
- 프로세스가 실행 환경을 제공한다:
- 메모리 공간, 파일 핸들, 시스템 자원(네트워크 연결 등) 등
- OS 스케줄러는 프로세스 아닌 프로세스의 스레드를 스케줄링
- (5) 컨텍스트 스위칭
- CPU가 스레드 실행 중 멈추면,
- CPU에서 사용하던 값들을 메모리에 저장해야 된다.
- 다시 멈춘 스레드 실행하려면,
- 메모리에 저장된 값들을 CPU에 불러와야 한다.
- 이런 과정을 컨텍스트 스위칭(문맥 변경)이라고 한다.
- 가벼운 계산만 하면 스위칭 비용은 적다. (CPU 사용 값 적다.)
- 코어가 여러 개면 병렬 처리니까 멀티스레드가 효율적이다.
- 코어가 1개고 무거운 계산이면 컨텍스트 스위칭 오버헤드를 고려한다.
- (6) CPU 바운드 작업, I/O 바운드 작업
- 복잡한 계산, 데이터 처리, 알고리즘 실행 등 CPU 처리 속도가 작업 완료 시간 결정 작업 = CPU 바운드 작업
- 디스크, 네트워크, 파일 시스템 등 입출력 많이 요구하는 작업
- 입출력 완료까지 대기 시간이 많이 발생 = I/O 바운드 작업
- (CPU 유휴 시간 - 스레드가 CPU를 사용하지 않고 대기)
- I/O 바운드 예: DB 쿼리, 파일 읽기/쓰기, 네트워크 통신, 사용자 입력 처리
- (6) 웹 애플리케이션 서버
- 사용자 입력 기다리거나, db 호출하고 결과 기다리는 등 I/O 바운드가 많다.
- 자바 웹 애플리케이션 서버: 사용자 요청 하나 처리에 1개 스레드가 필요하다.
- CPU 코어 수 = 스레드 수로 만들면 안 된다.
- 1개 스레드가 점유하는 시간이 짧다. 스레드 숫자를 늘린다.
- CPU 바운드 작업이 많아지면, CPU 숫자를 늘린다.
- 숫자 늘릴 때 성능 테스트를 거친다.
# 3. 스레드 생성과 실행
- (1) 자바 프로그램 실행 흐름
```scss
자바 소스코드(.java)
        ↓ (javac 컴파일러)
바이트코드(.class)
        ↓ (JVM 실행)
JVM 프로세스(Java Virtual Machine)
        ↓
운영체제 프로세스(자바 프로세스)
자바 프로그램 자체가 직접 OS 프로세스가 되는 것은 아니고,
JVM이 OS 위에서 하나의 프로세스로 실행되고,
그 안에서 자바 코드가 스레드 단위로 동작
```
```
구성 요소 - 역할 - OS 관점에서
JVM 프로세스 - 자바 코드를 실행하는 가상머신 - 하나의 OS 프로세스
자바 스레드 - 실제 실행 단위 (main, GC 등) - OS의 스레드로 매핑됨
힙 - 객체들이 저장되는 메모리 공간 - 프로세스 메모리 공간 일부
메서드 영역(Metaspace) - 클래스 메타데이터 저장 - 프로세스 메모리 영역
스택 - 각 스레드별 호출 스택 - OS 스레드 스택과 1:1 대응
```
```
1. 사용자가 java MyProgram 실행
OS는 java 명령을 실행하면서 JVM 프로세스를 시작
2. JVM 로드
JVM이 메모리에 올라와 초기화됨
JIT 컴파일러, GC, 클래스 로더 등 내부 컴포넌트 준비
3. 클래스 로딩
MyProgram.class와 관련된 클래스들을 로드
JVM 메서드 영역에 클래스 메타데이터 저장
4. main() 메서드 실행
JVM이 OS 스레드를 하나 만들어 main thread로 실행시킴
이후 프로그램에서 만든 new Thread()들은 OS 스레드로 1:1 매핑됨
5. 실행 중
각 스레드가 CPU 스케줄러에 의해 운영체제 차원에서 실행
메모리(Heap, Stack 등)는 JVM 내부에서 관리하지만
전체는 OS 프로세스의 메모리 공간 안에 있음
6. 종료
main() 스레드가 종료되고, 모든 non-daemon 스레드가 종료되면
JVM 프로세스 자체가 OS에서 종료 → 프로세스 해제
```
```
JVM 내부 메모리 구조
┌───────────────────────────┐
│   JVM 프로세스 메모리     │  ← 운영체제가 할당
│ ┌───────────────────────┐ │
│ │ Method Area / Metaspace│ 클래스 정보, static 변수
│ ├───────────────────────┤ │
│ │ Heap                  │ new 객체 저장
│ ├───────────────────────┤ │
│ │ JVM Stack (per thread)│ 지역 변수, 호출 정보
│ ├───────────────────────┤ │
│ │ Native Method Stack   │ JNI 호출용
│ ├───────────────────────┤ │
│ │ PC Register (per thr.)│ 실행 위치 정보
│ └───────────────────────┘ │
└───────────────────────────┘
```
- (2) 스택
```
스레드(Thread)
 └── JVM 스택 (하나)
       ├── 스택 프레임 (main 메서드)
       ├── 스택 프레임 (methodA 호출)
       ├── 스택 프레임 (methodB 호출)
       └── ...
스택 프레임이 메서드가 호출될 때마다 생성되고 리턴될 때 제거(pop)됨
<스택 프레임 하나>
구성 요소 - 내용
로컬 변수 테이블 (Local Variables) - int, double, 참조 변수 등
피연산자 스택 (Operand Stack) - 계산 중간 결과 저장용
메서드 반환 주소(Return Address) - 호출이 끝나면 돌아갈 위치
상수 풀 참조(Constant Pool Reference) - 클래스 상수, 메서드 참조 등
스레드는 코드 실행 단위일 뿐 아니라, JVM이 스레드당 여러 데이터를 별도로 유지
<스레드 하나>
구성 요소 - 역할
JVM Stack - 메서드 호출을 위한 스택 프레임 저장
PC Register (Program Counter) - 현재 실행 중인 명령어 주소
Native Stack (필요할 때만) - JNI (네이티브 코드) 호출 시 사용
Thread Object (java.lang.Thread) - 자바 코드에서 제어하는 스레드 객체
┌──────────────────────────────────────────┐
│ 스레드(Thread)                           │
│ ├── PC 레지스터 (현재 명령어 위치)        │
│ ├── JVM 스택 (메서드 호출 프레임들)       │
│ │    ├─ main() 프레임                    │
│ │    ├─ foo() 프레임                     │
│ │    └─ bar() 프레임                     │
│ ├── 네이티브 스택 (JNI 호출 시)           │
│ └── Thread 객체 (자바 레벨)               │
└──────────────────────────────────────────┘
```
- (3) CPU PC 레지스터와 스레드 PC 레지스터는 다르다.
```
<CPU의 PC 레지스터 - HW 개념>
항목 - 설명
위치 - CPU 내부 실제 레지스터
역할 - 다음에 실행할 명령어의 주소를 저장
단위 - 기계어 명령 (예: x86, ARM 명령)
제어 주체 - 운영체제와 CPU 하드웨어
예시 - 어셈블리 코드 실행 시 mov eax, 1 다음 명령이 어딘지 추적
CPU가 실제로 기계 명령을 실행하기 위해 쓰는 주소 저장소이다.
<JVM의 PC Register - JVM 내부 가상 PC 개념>
항목 - 설명
위치 - JVM 내부 (스레드별로 존재)
역할 - JVM 바이트코드의 “다음 명령어 주소” 저장
단위 - JVM 바이트코드 명령 (예: iload_0, invokestatic)
제어 주체 - JVM 인터프리터
특징 - 스레드마다 독립적 (각 스레드가 자기 명령어 위치 추적)
CPU의 실제 레지스터가 아니라, JVM이 각 스레드별로 논리적으로 관리하는 데이터이다.
<비교>
구분 - CPU PC 레지스터 - JVM PC 레지스터
실제 위치 - 하드웨어(CPU 내부) - JVM 소프트웨어 내부
추적하는 대상 - 실제 기계 명령어 주소 - JVM 바이트코드 명령 주소
실행 주체 - CPU - JVM 인터프리터 / JIT 코드
스레드 관계 - OS 스레드 1개당 CPU 레지스터 1개 - JVM 스레드 1개당 JVM PC 레지스터 1개
```
```java
void foo() {
    int x = 10;
    int y = x + 5;
    System.out.println(y);
}
/*
JVM 바이트코드는 iconst_10, istore_1, iload_1, iconst_5, iadd, … 이런 식으로 되어 있고,
JVM의 PC 레지스터는 실행 중인 바이트코드 명령의 인덱스 번호를 가리킨다.
실제로 이 JVM 명령을 CPU가 실행할 때는,
CPU의 PC 레지스터가 네이티브 기계 명령어 주소를 추적하고 있다.
 */
/*
JIT(Just-In-Time) 컴파일 상황에서는
JIT이 바이트코드를 기계어로 바꾸면,
실제 실행은 CPU의 PC 레지스터가 담당하게 되고,
JVM의 논리적 PC 레지스터는 그 구간에서는 사실상 의미가 약해진다.
(JIT 컴파일된 코드 블록 단위로 제어 흐름을 관리하기 때문)
 */
```
- (4) 기계 명령어가 저장된 물리적 파일
```
자바의 경우 .class 파일에는 JVM용 바이트코드가 들어 있다.
실제 실행은 JVM이 이 바이트코드를 읽고 → 기계 명령어로 변환(JIT) → CPU 실행
그래서 자바 프로그램은:
실행 시점에 JVM이 메모리에 기계 명령어를 생성해서 실행한다.
(JVM 내부의 코드 캐시(Code Cache) 영역에 저장)
JVM의 코드 캐시는 프로세스 안에 있다.
코드 캐시 = 운영체제가 JVM 프로세스에 할당한 가상 메모리 공간의 일부다.
[운영체제 메모리]
└── [JVM 프로세스 메모리 공간]
       ├── 힙(Heap)
       ├── 스택(Stack)
       ├── 메서드 영역(Metaspace)
       ├── 코드 캐시(Code Cache) ← 바로 이 부분
       ├── JNI 네이티브 메모리
       └── 기타 내부 버퍼 등
코드 캐시:
항목 - 내용
위치 - JVM 프로세스 내부의 “네이티브 메모리” 일부
저장 내용 - JIT이 생성한 CPU 기계 명령어
접근 방식 - CPU가 직접 실행 가능한 코드 형태
관리 주체 - JVM의 HotSpot 컴파일러 (C1, C2, Graal 등)
공간 크기 - 보통 수십 MB~수백 MB (-XX:ReservedCodeCacheSize) 옵션으로 설정 가능
운영체제 입장에서는 JVM은 그냥 하나의 일반 프로세스
JVM이 코드 캐시를 만들면 OS는 다음과 같이 처리
1. JVM이 mmap() 또는 VirtualAlloc() 같은 시스템 호출로 메모리를 요청함
2. OS가 JVM 프로세스의 가상 주소 공간 중 일부를 할당
3. JVM이 그 공간에 기계 명령어(바이너리 코드) 를 써 넣음
4. CPU가 이 영역의 코드를 실제 명령으로 실행
즉, JIT으로 생성된 코드는 디스크에 파일로 저장되지 않고
RAM 안의 JVM 프로세스 메모리 속에 들어가서 CPU가 직접 실행
Code Cache의 실제 주소 확인
$ jcmd <pid> VM.native_memory summary
$ cat /proc/<pid>/maps | grep "CodeHeap" //JVM의 Code Cache 영역들
7f8c10000000-7f8c12000000 r-xp 00000000 00:00 0 [CodeHeap 'non-profiled nmethods']
7f8c12000000-7f8c14000000 r-xp 00000000 00:00 0 [CodeHeap 'profiled nmethods']
7f8c14000000-7f8c16000000 r-xp 00000000 00:00 0 [CodeHeap 'non-nmethods']
```
- (5) 스레드 생성
- 자바가 예외를 객체로 다루듯, 스레드도 객체로 다룬다.
- 스레드가 필요하면 스레드 객체를 생성해 사용한다.
- 방법 1. Thread 클래스를 상속 받음
- 스레드 객체를 생성하고, start()를 호출해야
- 스택 공간을 할당받고 스레드가 작동한다.
- 스레드에 이름은 안 주면 Thread-0, Thread-1 등 이름 부여한다.
- Thread-0 스레드는 run() 메서드의 스택 프레임을 스택에 올린다.
```
순서 - 시점 - 사건
① - t1.start() 호출 직후 - JVM이 OS 스레드 생성 요청 → OS가 스레드 제어 블록과 스택 메모리 공간 할당
② - OS 스레드가 준비 상태로 대기 - 아직 run() 안 함, 스택은 비어있지만 이미 존재함
③ - OS 스케줄러가 Thread-0에게 CPU 할당 - 새 스레드가 JVM 엔트리 포인트(native)에서 시작
④ - JVM이 자바 레벨 진입 (Thread.run() 호출 준비) - run() 호출용 스택 프레임을 스택에 push
⑤ - run() 메서드 실제 실행 시작 - 이제 run() 내부 코드가 실행됨
```
- 251011-java-adv1/src/thread/HelloThread.java
- t1.run();
- 일반 메서드 호출이다. 현재 스레드(main 스레드)가 그대로 실행된다.
- Thread.currentThread().getName()은 항상 main이고,
- Thread.currentThread().threadId()도 항상 1(main thread의 id)
- t1.start();
- 새로운 스레드를 생성해서, 그 안에서 run() 메서드를 실행시킨다.
- 이때 출력되는 name 과 id 는 실제로 새로 만들어진 스레드의 이름/ID가 된다.
- start()는 새 스레드를 비동기적으로 등록만 하고
- 실제 실행(run 메서드 호출)은 언제 될지 보장 안 된다.
```
start()를 호출하면 JVM이 하는 일:
1. OS에 새 스레드를 하나 만들어 달라고 요청 (pthread_create 같은 시스템 콜)
2. 새 스레드가 만들어지고 나서야 그 안에서 run()을 호출
3. 하지만 이건 비동기적이라 — main 스레드는 기다리지 않음
즉,
t1.start();
t2.start();
를 해도 main 스레드는 바로 다음 줄로 넘어간다.
OS 스케줄러가 Thread-0과 Thread-1을 CPU에 언제 태울지는 그때그때 다르다.
출력 순서상 항상 맨 뒤쪽에 Thread-0, Thread-1이 찍히는 건
main 스레드는 계속 CPU를 점유한 상태
Thread-0, Thread-1은 CPU를 아직 배정받지 못한 상태일 가능성이 높다.
```
- start(): 생성 요청은 즉시 실행, 실행 요청은 등록만 하고 나중에 실행
```java
//start() JVM 내부적으로 이렇게 작동 (HotSpot 기준):
//1. Java → JVM native 코드로 진입
public synchronized void start() {
    //이미 실행된 적 있으면 예외
    if (started) throw new IllegalThreadStateException();
    started = true;
    start0(); //native 메서드 호출
}
//2. start0() → JVM의 native 구현 (C++)
//JVM 내부에서 OS에 스레드 생성을 요청
//예를 들어 리눅스에서는 대략 이런 과정:
JVM_StartThread(JavaThread *thread) {
    //OS 스레드 생성, cpp 코드
    pthread_create(&tid, NULL, thread_native_entry, thread);
    //OS는 새로운 커널 스레드(Kernel Thread)를 만든다.
    //스택 메모리(예: 1MB 기본) 를 커널이 즉시 할당한다.
    //스택 메모리 pthread_attr_setstacksize로 지정 가능
    //스택은 빈 상태이지만 물리적으로 이미 확보됨
    //스레드 상태는 NEW → READY로 변경
}
//OS 레벨의 스레드 객체가 바로 만들어진다.
//이 시점에서 생성 완료
//3. OS 스케줄러의 ready queue 등록
//새 스레드는 ready(준비 완료) 상태로 큐에 올라감
//하지만 CPU 코어가 배정되어야만 실제 실행됨
//스택 프레임을 push 하지도 않고, run()도 호출되지 않았다.
//4. OS가 CPU를 배정 → JVM 엔트리 실행 시작
//OS가 지금 Thread-0 실행해도 되겠다 하면
//스레드는 OS가 지정한 시작 함수(thread_native_entry)로 들어온다.
static void* thread_native_entry(void* args) {
    //이제 JVM 내부의 C++ 코드가 실행 중
    JavaThread* thread = (JavaThread*) args;
    thread->run();
}
//5. JVM이 자바 스택 프레임 준비 (run() 호출 직전)
//thread->run() 안에서 이렇게 자바 레벨 진입
JavaCalls::call_virtual(&result, thread_oop, klass, vmSymbols::run_name());
//cpp 코드, 이 순간 JVM 인터프리터(혹은 JIT 컴파일된 코드)가
//자바 메서드 호출 규약에 따라 새 스택 프레임을 push한다.
//스택에: 지역 변수 슬롯 공간, return address, operand stack 영역 올라간다.
//즉, run()의 스택 프레임이 만들어지는 시점은 바로 여기
//6. run() 메서드 실제 실행 시작
//이제 run() 메서드의 첫 번째 바이트코드(aload_0)부터 실행된다.
//스택 프레임은 이미 생성되어 있고
//PC 레지스터는 run()의 첫 명령어를 가리킨다.
//Thread.currentThread() 호출 시 현재 스레드는 Thread-0 객체를 가리킨다.
/*
OS는 모든 스레드 생성 요청을 즉시 처리하지만
언제 실행할지는 CPU 스케줄러가 결정
자바는 OS의 스케줄링을 통제할 수 없으므로
자바 레벨에서는 start() 이후 실제 실행 시점을 보장하지 않는다고 명시
그러므로 스레드 여러 개 생성했을 때 그 스레드 간에도 실행 순서가 달라질 수 있다.
 */
public class StartTiming {
    public static void main(String[] args) {
        Thread t = new Thread(() -> {
            System.out.println("Thread started at: " + System.nanoTime());
        });
        System.out.println("Calling start() at: " + System.nanoTime());
        t.start();
        System.out.println("After start() at: " + System.nanoTime());
    }
}
//Calling start() at: 123456000
//After start() at:   123456050 //start() 호출 직후 main은 다음 줄로 넘어감
//Thread started at:  123456400 //새 스레드는 늦게(run 호출되는 시점에) 시작됨
```
- Thread 객체는 한 번 실행이 끝나면 재사용 불가
- JVM 내부적으로 이미 실행된 스레드로 표시, 다시 start()하면 런타임 오류 난다.
- (6) 데몬 스레드
- 사용자 (non-daemon) 스레드와 데몬 스레드가 있다.
- 사용자 스레드:
- 프로그램의 주요 작업을 수행
- 작업이 완료될 때가지 실행
- 모든 user 스레드가 종료되면 JVM도 종료됨
- 데몬 스레드:
- 다른(일반) 스레드의 작업을 돕기 위한 보조용 스레드
- 모든 user 스레드가 종료되면 데몬 스레드도 종료됨
```java
public class DaemonExample {
    public static void main(String[] args) {
        Thread worker = new Thread(() -> {
            while (true) {
                System.out.println("Working...");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {}
            }
        });
        worker.setDaemon(true); //데몬 스레드로 설정
        worker.start();
        System.out.println("Main finished");
    }
}
/*
Main finished
Working...
Working...
(곧 종료됨)
 */
//main 스레드가 종료되면
//worker 스레드는 아직 무한 루프 중이어도
//JVM이 바로 프로세스를 종료
```
- JVM의 종료 조건:
- 하나 이상의 일반(non-daemon) 스레드가 살아 있음 - 계속 실행
- 일반 스레드가 모두 종료되고, 데몬 스레드만 남음 - 즉시 JVM 종료
- 데몬 스레드는 JVM 생명주기에 종속적
- 데몬 스레드는 백그라운드 작업에 유용:
- 예시 - 설명
- GC 스레드 - 가비지 컬렉터는 데몬 스레드
- JIT Compiler Thread - 바이트코드 최적화용 데몬
- 로그 자동 플러시 - 백그라운드에서 로그 버퍼 비우기
- 모니터링, 통계 수집 - 주 프로그램의 보조 역할 수행
- 데몬 스레드 주의할 점:
- JVM 종료 시 아무런 정리(cleanup) 없이 즉시 강제 종료됨
- 파일 닫기, DB 커넥션 정리 등은 데몬 스레드에서 하면 안 됨
- setDaemon(true)는 start() 이전에만 호출 가능
- 이미 시작된 스레드는 변경 불가
- ThreadPoolExecutor 등에서는 기본적으로 non-daemon 스레드
- 명시적으로 설정하지 않으면 데몬 아님
- 데몬 스레드도 OS 입장에서는 일반 스레드:
- JVM이 스레드를 생성할 때, 스레드 객체의 daemon 플래그를 보고
- 이 스레드는 프로세스 종료 시 기다리지 않아도 된다고 표시
- JVM이 자체적으로 non-daemon 스레드의 생존 여부만 보고 생명주기를 관리
```java
public static void main(String[] args) throws InterruptedException {
    System.out.println("main() start");      // (1)
    DaemonThread daemonThread = new DaemonThread();
    daemonThread.setDaemon(true);
    daemonThread.start();                    // (2)
    Thread.sleep(10000);                     // (3)
    System.out.println("main() end");        // (4)
}
static class DaemonThread extends Thread {
    @Override
    public void run() {
        String s = Thread.currentThread().getName();
        System.out.println(s + ": run() start"); // (A)
        Thread.sleep(5000);                      // (B)
        System.out.println(s + ": run() end");   // (C)
    }
}
/*
시점 - 스레드 - 동작 - 출력
0s - main - main 시작 - main() start
~0s - main - start() 호출 - (스레드 생성)
//t = 0~10초 구간
//main 스레드는 10초 동안 Thread.sleep(10000) 중
~0.01s - Thread-0 - run() 진입 - Thread-0: run() start
0~5s - Thread-0 - sleep(5000) - (대기)
5s - Thread-0 - 깨어남 - Thread-0: run() end
//데몬 스레드는 그 안에서 5초 동안 잠자고, 깨어나서
//Thread-0: run() end를 출력하고 종료됨
10s - main - main 종료 - main() end
//main 스레드의 sleep이 끝남
//main() end 출력하고 종료됨
10s 직후 - JVM - 일반 스레드 없음 → 종료 - (프로세스 종료)
 */
```
- (7) 자바 스레드의 상태 전이(state transition)와 생성 방식
- 스레드의 생명주기(Lifecycle) 5단계
- NEW → RUNNABLE ↔ WAITING/TIMED_WAITING/BLOCKED → TERMINATED
```
상태 - 의미
NEW - 아직 start()를 호출하지 않은 상태
RUNNABLE - 실행 준비 완료 (CPU 스케줄링 대기 or 실행 중)
WAITING - 다른 스레드의 신호를 기다리는 중 (wait(), join() 등)
TIMED_WAITING - 일정 시간 기다리는 중 (sleep(ms) 등)
BLOCKED - synchronized 락이 풀리길 기다리는 중
TERMINATED - run() 종료로 스레드 실행 끝남
```
- RUNNABLE은 스레드 생성 방식과 직접 연결됨
- `Thread t = new Thread();` 호출 시
- JVM이 Thread 객체를 힙(Heap)에 만들고
- 아직 OS 스레드를 생성하지 않는다. (New 상태: 스택, OS 스레드 없음)
- `t.start()` 호출 시
- 이때가 바로 스레드 생성과 RUNNABLE 전이의 핵심
- JVM이 OS에 스레드 생성 요청 (pthread_create 등)
- OS 스레드 스택, 레지스터 등 리소스 할당
- 스레드가 ready queue에 등록됨
- 아직 CPU를 받지 않았더라도 RUNNABLE 상태로 간주
- 스택, 스레드 있음 + 스케줄링 대기 중
- `OS가 실제로 CPU를 주면`
- 이제 run() 메서드가 실행되며 RUNNABLE 상태 유지 중
- 자바는 실행 중과 실행 대기 중을 모두 RUNNABLE이라고 부른다.
- OS는 실행 대기 중은 READY, 실행 중은 RUNNING
- `Thread.sleep(5000)` 호출 시
- TIMED_WAITING 상태: 스레드가 CPU를 반납하고 5초 동안 대기
- 스레드가 sleep queue에 등록됨
- `sleep()` 끝나고 다시 깨어남
- RUNNABLE 상태
- `run()` 메서드 종료 시
- OS 스레드 해제, TERMINATED (재시작 불가능)
- (8) 스레드 생성 방법 2. Runnable 인터페이스 구현
- Thread 상속보다 일반적이고 권장되는 방식
- Runnable은 실행할 코드 블록(run)을 전달하기 위한 인터페이스
```java
@FunctionalInterface
public interface Runnable {
    void run();
}
//이 안에 실행할 일을 적어 주세요라는 약속
/*
Thread 상속 vs Runnable 구현
방식 - 특징
Thread 상속 - run() 재정의
Runnable 구현 - run() 구현 후, new Thread(runnable)로 실행
 */
//Thread 상속
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread 상속 방식 실행: " + Thread.currentThread().getName());
    }
}
public class Example1 {
    public static void main(String[] args) {
        MyThread t = new MyThread();
        t.start();
    }
}
//Runnable 구현
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable 구현 방식 실행: " + Thread.currentThread().getName());
    }
}
public class Example2 {
    public static void main(String[] args) {
        MyRunnable task = new MyRunnable(); //Runnable 객체 생성
        Thread t = new Thread(task); //Thread에 전달
        t.start(); //실행 시작
    }
}
//Thread 객체 안에는 Runnable 타입의 필드(target)가 하나 있다.
//start()가 호출되면 내부적으로 이렇게 실행
//Thread.java 내부 일부 (간단히 표현)
public void run() {
    if (target != null) {
        target.run(); //전달한 Runnable의 run() 실행
    }
}
//즉, Thread는 Runnable의 run()을 대신 호출해주는 Wrapper 역할
//Runnable은 함수형 인터페이스
//익명 클래스
Thread t = new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("익명 클래스 실행: " + Thread.currentThread().getName());
    }
});
t.start();
//람다식 (자바 8+)
Thread t = new Thread(() -> {
    System.out.println("람다식 실행: " + Thread.currentThread().getName());
});
t.start();
```
- (9) 왜 Runnable이 더 좋은가
- Thread를 직접 상속받으면 다른 클래스를 상속받을 수 없어서
- Runnable을 쓰면 실행 로직과 스레드 제어가 분리되어서도 권장된다.
- Runnable은 스레드의 작업(무엇을 할지)을 정의하는 인터페이스
- Thread는 그 작업을 실제로 실행시키는 스레드 객체
- `new Thread(runnable).start()` → `runnable.run()` 실행
```java
public class Thread implements Runnable {
    //Thread 클래스 자체가 Runnable 인터페이스를 구현한다.
    //자기 자신의 run()을 오버라이드해서 실행하거나
    //외부에서 전달받은 Runnable target의 run()을 대신 실행한다.
    private Runnable target; //외부에서 전달받은 Runnable 저장
    public Thread() { }
    public Thread(Runnable target) {
        this.target = target;
    }
    @Override
    public void run() {
        if (target != null) {
            target.run(); //외부 Runnable의 run 실행
        }
    }
    public synchronized void start() {
        //네이티브로 OS 스레드 생성 → run() 호출
    }
}
```
- (10) 로거 만들기
```java
public abstract class MyLogger {
    private static final DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    public static void log(Object obj) {
        String t = LocalTime.now().format(f);
        System.out.printf("%s [%9s] %s\n", t, Thread.currentThread().getName(), obj);
    }
}
MyLogger.log("hello thread");
MyLogger.log(123);
//21:14:56.075 [     main] hello thread
//21:14:56.077 [     main] 123
```