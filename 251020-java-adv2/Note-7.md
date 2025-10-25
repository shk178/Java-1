## Suppressed Exception
- 한 예외가 이미 발생했는데, 그 뒤에 또 다른 예외가 생겼을 때, 나중의 예외 때문에 첫 번째 예외가 사라지지 않도록 저장해두는 메커니즘
- 자바 7부터 추가된 기능
- try-with-resources 문법에서 자동으로 사용
### 설명
- 자바는 기본적으로 마지막 예외만 던진다는 규칙이 있다.
- 하지만 첫 번째 예외가 더 중요한 원인일 수도 있다.
- 나중Exception을 던지되, 처음Exception를 suppressed exception으로 저장해 둔다.
### 확인하는 방법
```java
// try-with-resources에서는 자동 처리된다.
try {
    logic();
} catch (Exception e) {
    e.printStackTrace(); // 메인 예외 출력
    for (Throwable t : e.getSuppressed()) {
        System.out.println("Suppressed: " + t);
    }
}
//CloseException: r2 closeEx
//    ...
//Suppressed: CallException: r2 callEx
```
- finally 블록에서 예외 발생 시 나머지 실행 안 됨
```java
// 이렇게 방지한다.
finally {
    try { one.close(); } catch (Exception e) { /* 로그 */ }
    try { two.close(); } catch (Exception e) { /* 로그 */ }
    try { three.close(); } catch (Exception e) { /* 로그 */ }
}
```
- 251020-java-adv2/src/autoclose/Main3.java
```java
/*
r1 call
r2 callEx
r1 close
r2 closeEx
autoclose.CloseException: r2 ex // autoclose(패키지 이름)에 정의된 CloseException 클래스
// 패키지 포함된 클래스 이름을 클래스의 전체 이름(fully qualified name)이라고 한다.
// r2 ex: 예외 메시지 (super(msg)에 전달된 값)
r2 callEx
autoclose.CallException: r2 ex
callEx 예외 처리 // finally에서 새로운 예외가 던져지지 않는 한 try에서 발생했던 예외가 main으로 전파된다.
*/
```
- 251020-java-adv2/src/network4 // SocketCloseUtil
- 251020-java-adv2/src/network5 // try-with-resources
- 251020-java-adv2/src/network6 // Shutdown Hook
- 자바는 프로세스가 종료될 때 자원 정리나 로그 기록과 같은 종료 작업을 마무리할 수 있는 셧다운 훅이라는 기능을 지원한다.
- 1. 프로세스 종료 - 정상 종료
- 모든 non-demon 스레드의 실행 완료로 자바 프로세스 정상 종료
- 사용자가 Ctrl + C를 눌러서 프로그램을 중단
- kill 명령 전달 (kill -9 제외)
- IntelliJ의 stop 버튼
- 2. 프로세스 종료 - 강제 종료
- 운영체제에서 프로세스를 유지할 수 없다고 판단할 때 사용
- 리눅스/유닉스의 kill -9나 Windows의 taskkill /F
- 정상 종료: 셧다운 훅이 작동해서 프로세스 종료 전에 필요한 처리를 할 수 있다.
- 강제 종료: 셧다운 훅이 작동하지 않는다.
```java
/*
11:16:16.949 [   셧다운스레드] 셧다운 훅 실행
Exception in thread "main" java.net.SocketException: Socket closed
	at java.base/sun.nio.ch.NioSocketImpl.endAccept(NioSocketImpl.java:682)
	at java.base/sun.nio.ch.NioSocketImpl.accept(NioSocketImpl.java:755)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:698)
	at java.base/java.net.ServerSocket.platformImplAccept(ServerSocket.java:663)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:639)
	at java.base/java.net.ServerSocket.implAccept(ServerSocket.java:585)
	at java.base/java.net.ServerSocket.accept(ServerSocket.java:543)
	at network6.Server.main(Server.java:17)
- main() 메서드는 while (true) 루프에서 계속 serverSocket.accept()를 호출하며 클라이언트 연결을 기다립니다.
- JVM이 종료되면 ShutdownHook이 실행됩니다.
- ShutdownHook.run()에서 serverSocket.close()가 호출되어 서버 소켓이 닫힙니다.
- 하지만 main() 스레드는 여전히 accept()를 호출 중이거나 다음 루프에서 accept()를 호출하려고 합니다.
- 이때 serverSocket이 이미 닫혀 있으므로 accept()는 SocketException: Socket closed를 던집니다.
 */
```
- 이렇게 하면 셧다운 훅이 serverSocket.close()를 호출한 이후 발생하는 예외를
- 정상적인 종료 신호로 간주하고 루프를 종료할 수 있다.
```java
while (true) {
    try {
        Socket socket = serverSocket.accept();
        log("소켓 연결: " + socket);
        Session session = new Session(socket, sessionManager);
        Thread thread = new Thread(session);
        thread.start();
    } catch (SocketException e) {
        log("서버 소켓이 닫혀 accept 중단됨: " + e.getMessage());
        break; // 루프 종료
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```
- 251020-java-adv2/src/network7
## 서버 시작 과정
### 1. 서버 초기화 (Server.main)
```
Server 객체 생성
  ↓
ServerSocket을 12345 포트에 바인딩
  ↓
SessionManager 생성 (세션 관리자)
  ↓
스레드 풀(ExecutorService) 생성 (10개 스레드)
  ↓
셧다운 훅 등록 (Ctrl+C 대비)
  ↓
서버 준비 완료
```
### 2. 클라이언트 연결 대기 (Server.start)
```
while (running) {
    serverSocket.accept() ← 여기서 대기 중...
    클라이언트 연결 올 때까지 블로킹
}
```
## 클라이언트 연결 및 통신 과정
### 3. 클라이언트 시작 (Client.run)
```
Client 프로그램 실행
  ↓
localhost:12345로 Socket 연결 시도
  ↓
DataInputStream, DataOutputStream 생성
  ↓
서버와 연결 성공
```
### 4. 서버가 연결 수락
```
serverSocket.accept()가 반환됨 (새 Socket 생성)
  ↓
Session 객체 생성
  ├─ Socket, InputStream, OutputStream 초기화
  └─ SessionManager에 세션 등록 (sessions 리스트에 추가)
  ↓
executorService.submit(session) ← 스레드 풀에 작업 제출
  ↓
스레드 풀의 한 스레드가 session.run() 실행
```
### 5. 메시지 송수신 루프
클라이언트 측:
```
사용자가 "Hello" 입력
  ↓
output.writeUTF("Hello") → 서버로 전송
  ↓
input.readUTF() → 서버 응답 대기 (블로킹)
  ↓
"Hello World" 수신
  ↓
화면에 출력: "받은 문자: Hello World"
```
서버 측 (Session.run):
```
while (!closed.get()) {
    input.readUTF() ← 클라이언트 메시지 대기 (블로킹)
      ↓
    "Hello" 수신
      ↓
    로그 출력: "수신: Hello"
      ↓
    받은 메시지에 " World" 추가
      ↓
    output.writeUTF("Hello World") → 클라이언트로 전송
      ↓
    output.flush() (즉시 전송)
      ↓
    로그 출력: "송신: Hello World"
      ↓
    다시 input.readUTF() 대기...
}
```
## 동시 다중 클라이언트 처리
### 6. 여러 클라이언트가 동시 접속하면
```
클라이언트 A 연결
  ↓
Session A 생성 → 스레드 1에서 실행
  ↓
클라이언트 B 연결
  ↓
Session B 생성 → 스레드 2에서 실행
  ↓
클라이언트 C 연결
  ↓
Session C 생성 → 스레드 3에서 실행
※ 각 세션은 독립적으로 동작
※ 최대 10개까지 동시 처리 (스레드 풀 크기)
```
SessionManager의 역할:
```
sessions = [Session A, Session B, Session C]
  ↓
각 세션을 추적하여 일괄 종료 가능
```
## 정상 종료 과정
### 7. 클라이언트가 "exit" 입력
클라이언트 측:
```
"exit" 입력
  ↓
output.writeUTF("exit") → 서버로 전송
  ↓
while 루프 종료 (break)
  ↓
try-with-resources가 자동으로 Socket, Stream 닫기
  ↓
프로그램 종료
```
서버 측:
```
input.readUTF()가 "exit" 반환
  ↓
if (received.equals("exit")) 조건 만족
  ↓
while 루프 종료 (break)
  ↓
finally 블록 실행
  ├─ sessionManager.remove(this) ← 리스트에서 제거
  └─ close() 메서드 호출
      ├─ output.close()
      ├─ input.close()
      └─ socket.close()
  ↓
해당 세션 스레드 종료
```
## 서버 강제 종료 (Ctrl+C)
### 8. 셧다운 훅 실행
```
사용자가 Ctrl+C 입력
  ↓
JVM이 셧다운 훅 스레드 실행
  ↓
shutdown() 메서드 실행:
1단계: running = false
  ├─ accept() 루프 중단
  └─ serverSocket.close() ← 새 연결 차단
2단계: sessionManager.closeAll()
  ├─ for (Session session : sessions)
  │     session.close() ← 모든 클라이언트 연결 종료
  └─ sessions.clear()
3단계: executorService.shutdown()
  ├─ 새 작업 제출 차단
  ├─ 실행 중인 작업 완료 대기 (최대 5초)
  └─ 타임아웃 시 강제 종료 (shutdownNow)
  ↓
모든 리소스 정리 완료
  ↓
JVM 종료
```
### 블로킹 지점들:
1. `serverSocket.accept()` - 클라이언트 연결 대기
2. `input.readUTF()` - 메시지 수신 대기
3. `executorService.awaitTermination()` - 스레드 종료 대기
### 스레드 구조:
```
Main Thread: accept() 루프 실행
  ↓
Thread Pool (10개):
  ├─ Thread 1: Session A 실행
  ├─ Thread 2: Session B 실행
  ├─ Thread 3: Session C 실행
  └─ ...
Shutdown Hook Thread: 종료 처리
```
### 동시성 처리:
- `CopyOnWriteArrayList`: 세션 리스트 안전하게 관리
- `AtomicBoolean`: 세션 종료 상태 원자적 관리
- `volatile boolean`: 서버 실행 상태 가시성 보장
## 1. 하드웨어 레벨: 네트워크 카드부터 시작
### 네트워크 카드(NIC)의 역할
```
클라이언트 컴퓨터                          서버 컴퓨터
      ↓                                        ↓
  네트워크 카드                            네트워크 카드
  (MAC: aa:bb:cc...)                     (MAC: dd:ee:ff...)
      ↓                                        ↓
   이더넷 케이블 / WiFi
      ↓                                        ↓
         전기 신호 or 전파로 데이터 전송
```
- 네트워크 카드가 하는 일:
- 1. 송신: 상위 계층(OS)에서 받은 디지털 데이터를 전기 신호로 변환
- 2. 수신: 전기 신호를 디지털 데이터로 변환하여 OS에 전달
- 3. MAC 주소 확인: 이 패킷이 나한테 온 건지 확인
- 4. DMA(Direct Memory Access): CPU 개입 없이 메모리에 직접 데이터 쓰기
## 2. 네트워크 계층: OSI 7 Layer
### 데이터가 전송되는 과정
```
클라이언트                                    서버
─────────────────────────────────────────────────────
Application Layer (Java 코드)
  "Hello" 문자열
      ↓
Transport Layer (TCP)
  [TCP Header][Hello]
  - 출발 포트: 54321 (임의)
  - 목적 포트: 12345
  - Sequence Number
  - ACK Number
      ↓
Network Layer (IP)
  [IP Header][TCP Header][Hello]
  - 출발 IP: 192.168.0.10
  - 목적 IP: 192.168.0.20
      ↓
Data Link Layer (Ethernet)
  [Ethernet Header][IP][TCP][Hello][CRC]
  - 출발 MAC
  - 목적 MAC
      ↓
Physical Layer (전기 신호)
  010110101010... → 네트워크 카드로 전송
      ↓
  ─────── 물리적 전송 ───────
      ↓
서버 네트워크 카드 수신
      ↓
Physical → Data Link → Network → Transport → Application
      ↓
서버의 Java 코드가 "Hello" 수신
```
## 3. 포트와 소켓의 원리
### 왜 포트가 필요한가
```
서버 컴퓨터 (IP: 192.168.0.20)
├─ 웹 브라우저 (포트: 80)
├─ 이메일 서버 (포트: 25)
├─ 우리 Java 서버 (포트: 12345)
└─ SSH (포트: 22)
하나의 IP 주소에 여러 프로그램이 동시에 실행됨
→ 포트 번호로 어느 프로그램으로 갈지 구분
```
### 포트 번호의 범위
- 0-1023: Well-known ports (HTTP:80, HTTPS:443, SSH:22)
- 1024-49151: Registered ports (애플리케이션이 등록)
- 49152-65535: Dynamic ports (클라이언트가 임시로 사용)
## 4. 왜 하나는 서버용, 하나는 연결용
### ServerSocket의 역할
```java
ServerSocket serverSocket = new ServerSocket(12345);
```
이게 하는 일:
```
1. OS에게 요청: "12345 포트를 내가 쓸게요"
   ↓
2. OS 커널의 네트워크 스택에 등록
   ┌─────────────────────────────┐
   │  OS Kernel Network Stack    │
   ├─────────────────────────────┤
   │  Port 12345: LISTEN 상태    │
   │  → Java Process와 연결됨    │
   └─────────────────────────────┘
   ↓
3. TCP 3-way handshake 대기
   클라이언트: SYN →
              ← SYN-ACK
              ACK →
   ↓
4. 연결 완료되면 accept()가 새로운 Socket 반환
```
### 왜 ServerSocket은 하나인가
```
현실 비유: 식당
ServerSocket = 식당 입구 (하나)
├─ 손님1 들어옴 → 테이블1(Socket1) 배정
├─ 손님2 들어옴 → 테이블2(Socket2) 배정
├─ 손님3 들어옴 → 테이블3(Socket3) 배정
└─ ...
입구는 하나지만, 테이블은 여러 개
```
코드로 보면:
```java
ServerSocket serverSocket = new ServerSocket(12345); // 입구 하나
while (true) {
    Socket socket1 = serverSocket.accept(); // 손님1 → 테이블1
    Socket socket2 = serverSocket.accept(); // 손님2 → 테이블2
    Socket socket3 = serverSocket.accept(); // 손님3 → 테이블3
}
```
### 각 Socket의 정체
```
Socket = (클라이언트 IP, 클라이언트 포트, 서버 IP, 서버 포트)
Socket1: (192.168.0.10:54321, 192.168.0.20:12345)
Socket2: (192.168.0.11:54322, 192.168.0.20:12345)
Socket3: (192.168.0.12:54323, 192.168.0.20:12345)
서버 포트는 모두 12345로 같지만,
클라이언트 포트가 다르므로 구분 가능
```
## 5. 스트림(Stream)이 필요한 이유
### Socket은 왜 바이트 배열만 다루나
```
네트워크는 바이트의 연속적인 흐름일 뿐
01010101 01010101 01010101...
문제:
- 어디서 끊어야 하는지 모름
- "Hello"인지 "Hel" "lo"인지 구분 안 됨
- 타입 정보 없음 (String? int? Object?)
```
### 스트림의 역할
```java
// Low Level - Socket만 사용
Socket socket = new Socket(...);
byte[] data = socket.getInputStream().read(); // 바이트 배열
// 문제: 문자열로 변환 - 어디까지가 한 메시지
// High Level - DataInputStream 사용
DataInputStream input = new DataInputStream(socket.getInputStream());
String message = input.readUTF(); // 완전한 문자열 하나를 읽음
```
### DataInputStream/DataOutputStream의 내부 동작
```
writeUTF("Hello") 실행 시:
1. 문자열 길이 계산: 5바이트
2. 먼저 길이 전송: [00 05]  ← 2바이트 (길이 정보)
3. 그 다음 데이터: [48 65 6C 6C 6F]  ← "Hello"의 UTF-8 바이트
수신 측에서:
1. 처음 2바이트 읽기 → 길이 5 확인
2. 5바이트 읽기 → "Hello" 복원
3. 완전한 문자열 반환
```
### BufferedInputStream/OutputStream의 역할
```
버퍼 없이:
write("H") → 네트워크로 즉시 전송 (오버헤드 큼)
write("e") → 네트워크로 즉시 전송
write("l") → 네트워크로 즉시 전송
write("l") → 네트워크로 즉시 전송
write("o") → 네트워크로 즉시 전송
버퍼 사용:
write("H") → 버퍼에 저장
write("e") → 버퍼에 저장
write("l") → 버퍼에 저장
write("l") → 버퍼에 저장
write("o") → 버퍼에 저장
flush()    → "Hello" 한 번에 전송 (효율적)
버퍼 크기: 보통 8KB (8192 바이트)
```
## 6. 프로세스/스레드
### 프로세스(Process)
```
┌─────────────────────────────────┐
│  Java 서버 프로세스 (PID: 1234)  │
├─────────────────────────────────┤
│  Code Segment (프로그램 코드)     │
│  Data Segment (전역 변수)        │
│  Heap (동적 할당 메모리)          │
│  Stack (함수 호출 정보)          │
├─────────────────────────────────┤
│  파일 디스크립터 (열린 파일들)     │
│  - Socket FD: 3                 │
│  - ServerSocket FD: 4           │
└─────────────────────────────────┘
독립적인 메모리 공간
다른 프로세스와 메모리 공유 안 됨
```
### 스레드(Thread)
```
프로세스 내부:
┌─────────────────────────────────┐
│  공유 영역                       │
│  ├─ Code (모든 스레드가 같은 코드)│
│  ├─ Data (전역 변수 공유)        │
│  └─ Heap (객체들 공유)           │
├─────────────────────────────────┤
│  스레드1: Main Thread            │
│  └─ Stack1 (지역 변수)           │
├─────────────────────────────────┤
│  스레드2: Session Thread A       │
│  └─ Stack2 (지역 변수)           │
├─────────────────────────────────┤
│  스레드3: Session Thread B       │
│  └─ Stack3 (지역 변수)           │
└─────────────────────────────────┘
같은 메모리 공유
→ 빠르지만 동기화 문제 발생 가능
```
### 서버에서의 스레드 구조
```java
public static void main(String[] args) { // ← Main Thread
    ServerSocket serverSocket = new ServerSocket(12345);
    ExecutorService pool = Executors.newFixedThreadPool(10);
    while (true) {
        Socket socket = serverSocket.accept(); // Main Thread가 대기
        Session session = new Session(socket);
        pool.submit(session); // ← 스레드 풀의 Worker Thread가 실행
    }
}
```
메모리 구조:
```
Main Thread:
├─ Stack: serverSocket, pool, socket 변수
└─ 계속 accept() 루프 실행
Worker Thread 1:
├─ Stack: Session A의 지역 변수
└─ Session A.run() 실행 중
Worker Thread 2:
├─ Stack: Session B의 지역 변수
└─ Session B.run() 실행 중
공유 Heap:
├─ SessionManager 객체 (모든 스레드가 접근)
├─ sessions 리스트 (동시성 문제 발생 가능)
└─ 각 Session 객체
```
## 7. 동기(Blocking) vs 비동기(Non-blocking)
### 동기식(Blocking I/O)
Blocking의 의미:
```java
// Thread 1
Socket socket = serverSocket.accept(); // ← 여기서 멈춤 (블로킹)
// 클라이언트가 연결할 때까지 CPU는 이 스레드를 실행 안 함
// Thread 2
String message = input.readUTF(); // ← 여기서 멈춤 (블로킹)
// 클라이언트가 메시지 보낼 때까지 대기
```
OS 레벨에서 무슨 일이
```
Java Thread가 accept() 호출
      ↓
JVM → OS 시스템 콜: accept()
      ↓
OS: "아직 연결 없음"
      ↓
OS가 이 스레드를 WAITING 상태로 변경
      ↓
┌─────────────────────┐
│ OS Thread Scheduler │
├─────────────────────┤
│ RUNNING: Thread A   │
│ WAITING: Thread B   │ ← 우리 스레드 (대기 중)
│ RUNNING: Thread C   │
└─────────────────────┘
      ↓
네트워크 카드에 SYN 패킷 도착
      ↓
네트워크 카드 → OS 커널에 인터럽트 발생
      ↓
OS: "연결 왔다"
      ↓
OS가 Thread B를 RUNNABLE 상태로 변경
      ↓
CPU가 Thread B 실행 재개
      ↓
accept()가 새 Socket 반환
```
### 비동기식(Non-blocking I/O)
```java
// Java NIO (Non-blocking I/O) 예시
Selector selector = Selector.open();
ServerSocketChannel channel = ServerSocketChannel.open();
channel.configureBlocking(false); // ← 비동기 모드
channel.register(selector, SelectionKey.OP_ACCEPT);
while (true) {
    selector.select(); // 이벤트 대기
    Set<SelectionKey> keys = selector.selectedKeys();
    for (SelectionKey key : keys) {
        if (key.isAcceptable()) {
            // 연결 준비됨
        }
        if (key.isReadable()) {
            // 읽을 데이터 있음
        }
    }
}
```
차이점:
```
Blocking I/O (우리 코드):
- 클라이언트 1000명 → 스레드 1000개 필요
- 각 스레드는 대부분 시간을 WAITING 상태로 소비
- 메모리: 스레드당 1MB × 1000 = 1GB 사용
Non-blocking I/O:
- 클라이언트 1000명 → 스레드 1-10개로 처리 가능
- 하나의 스레드가 여러 연결을 감시
- 메모리 효율적
- 하지만 코드 복잡도 증가
```
## 8. 동기화(Synchronization) 문제
### 왜 CopyOnWriteArrayList를 사용했나
문제 상황:
```java
// 일반 ArrayList 사용 시
List<Session> sessions = new ArrayList<>();
// Thread 1: 세션 추가 중
sessions.add(session); 
// 내부적으로: 배열 크기 확인 → 복사 → 새 요소 추가
// 동시에 Thread 2: 세션 순회 중
for (Session s : sessions) { // ConcurrentModificationException
    s.close();
}
```
메모리 레벨에서 무슨 일이
```
초기 상태:
sessions = [A, B, C] (배열 크기: 3)
           ↑
        0x1000 (메모리 주소)
Thread 1이 D 추가 시도:
1. 배열 크기 부족 확인
2. 새 배열 생성: [A, B, C, D] (크기: 6)
                 ↑
              0x2000 (새 주소)
3. sessions 포인터를 0x2000으로 변경
이 순간 Thread 2가 0x1000을 읽고 있으면
→ 오래된 데이터 읽음
→ 또는 크래시
```
CopyOnWriteArrayList의 해법:
```java
// 쓰기 작업마다 전체 복사
sessions.add(session);
내부 동작:
1. 현재 배열 전체 복사
2. 복사본에 새 요소 추가
3. 포인터를 새 배열로 변경 (원자적 연산)
읽기는 복사 없이 현재 배열 그대로 읽음
→ 읽기 중에 쓰기가 일어나도 안전
```
### AtomicBoolean의 필요성
```java
// 문제가 되는 코드
private boolean closed = false;
public void close() {
    if (closed) return; // Thread 1이 여기 실행 중
    closed = true;      // Thread 2가 동시에 여기 실행
    // 둘 다 close 로직 실행 (중복 실행)
}
// 해결책
private AtomicBoolean closed = new AtomicBoolean(false);
public void close() {
    if (!closed.compareAndSet(false, true)) return;
    // CPU의 원자적 명령어(CAS)로 한 스레드만 통과
}
```
CPU 레벨의 CAS(Compare-And-Swap):
```
Assembly 수준:
LOCK CMPXCHG [메모리], [레지스터]
이 명령어는 원자적(Atomic):
1. 메모리 값이 예상 값(false)과 같은지 확인
2. 같으면 새 값(true)으로 변경
3. 1-2가 중간에 중단되지 않음 (CPU 레벨에서 보장)
```
## 9. 전체 데이터 흐름 (패킷 레벨까지)
### "Hello" 한 단어가 전송되는 전 과정
```
클라이언트 Java 코드:
output.writeUTF("Hello");
      ↓
DataOutputStream:
[00 05 48 65 6C 6C 6F] (길이 2바이트 + 데이터 5바이트)
      ↓
BufferedOutputStream:
버퍼에 저장 → flush() 시 전송
      ↓
Socket (Java):
JVM → OS 시스템 콜: send()
      ↓
OS Kernel (TCP Layer):
┌──────────────────────────┐
│ TCP Header               │
├──────────────────────────┤
│ Source Port: 54321       │
│ Dest Port: 12345         │
│ Seq Number: 1000         │
│ ACK Number: 2000         │
│ Flags: PSH, ACK          │
│ Window Size: 65535       │
│ Checksum: 0xABCD         │
└──────────────────────────┘
[00 05 48 65 6C 6C 6F] ← 데이터
      ↓
OS Kernel (IP Layer):
┌──────────────────────────┐
│ IP Header                │
├──────────────────────────┤
│ Version: 4               │
│ Source IP: 192.168.0.10  │
│ Dest IP: 192.168.0.20    │
│ Protocol: TCP (6)        │
│ TTL: 64                  │
└──────────────────────────┘
[TCP Header][Data]
      ↓
Network Driver:
┌──────────────────────────┐
│ Ethernet Frame           │
├──────────────────────────┤
│ Dest MAC: dd:ee:ff:...   │
│ Source MAC: aa:bb:cc:... │
│ Type: IPv4 (0x0800)      │
└──────────────────────────┘
[IP Header][TCP Header][Data]
[CRC Checksum] ← 오류 검출용
      ↓
Network Card (NIC):
디지털 신호 → 전기 신호 변환
01001000 01100101... → ~~~~전압 변화~~~~
      ↓
Physical Medium:
이더넷 케이블 / WiFi 전파
      ↓
─────── 전송 ───────
      ↓
서버 Network Card:
전기 신호 → 디지털 신호
~~~~전압 변화~~~~ → 01001000 01100101...
      ↓
인터럽트 발생 → OS Kernel 깨움
      ↓
OS Kernel:
1. Ethernet 헤더 확인: "MAC 주소 맞네"
2. IP 헤더 확인: "내 IP 맞네"
3. TCP 헤더 확인: "12345 포트로 가야겠다"
4. 데이터를 Socket 버퍼에 저장
      ↓
Java Thread (WAITING 상태였음):
OS가 Thread를 RUNNABLE로 변경
      ↓
input.readUTF() 반환
      ↓
String received = "Hello";
```
## 10. 왜 이런 복잡한 구조가 필요한가
### 계층화의 이점
```
Application (Java):
- "Hello" 문자열만 신경 쓰면 됨
- 네트워크 세부사항 몰라도 OK
Transport (TCP):
- 신뢰성 보장 (패킷 손실 시 재전송)
- 순서 보장
- 흐름 제어
Network (IP):
- 라우팅 (어느 경로로 갈지)
- 주소 지정
Data Link (Ethernet):
- 물리적 주소 (MAC)
- 오류 검출 (CRC)
Physical:
- 전기 신호 변환
```
### 각 계층은 독립적
```
Application을 바꿔도 (HTTP → WebSocket)
→ TCP는 변경 불필요
TCP를 바꿔도 (TCP → UDP)
→ IP는 변경 불필요
유선을 WiFi로 바꿔도
→ 상위 계층은 모름
```
## 요약
```
하드웨어:
네트워크 카드 → 전기 신호 송수신
OS:
├─ 네트워크 스택 (TCP/IP)
├─ 소켓 API (파일 디스크립터)
└─ 스레드 스케줄러
JVM:
├─ 네이티브 메서드로 OS 시스템 콜
└─ 스레드 관리
Java 코드:
├─ ServerSocket (포트 12345 리스닝)
├─ ExecutorService (스레드 풀 10개)
├─ Session × N (각 클라이언트 연결)
└─ SessionManager (동시성 제어)
동작 방식:
├─ Blocking I/O (스레드 블로킹)
├─ 멀티스레드 (동시 처리)
└─ Synchronization (CopyOnWriteArrayList, AtomicBoolean)
```
## 프로세스와 포트는 전혀 별개
### 1. 하나의 프로세스가 여러 포트 사용 가능
```
Java 서버 프로세스 (PID: 1234)
├─ 포트 12345 (ServerSocket) ← 듣기만 함
├─ 포트 54321 (Socket1) ← 클라이언트A와 통신
├─ 포트 54322 (Socket2) ← 클라이언트B와 통신
└─ 포트 54323 (Socket3) ← 클라이언트C와 통신
프로세스는 하나
포트는 여러 개
```
## 실제 동작 원리
### ServerSocket.accept()가 하는 일
```java
ServerSocket serverSocket = new ServerSocket(12345);
Socket socket = serverSocket.accept(); // 새 포트를 만들지 않습니다.
```
```
OS 레벨에서 보면:
연결 전:
┌─────────────────────────────────┐
│ OS Network Stack                │
├─────────────────────────────────┤
│ LISTENING                       │
│ *:12345 (모든 인터페이스)        │
│ → Java Process (PID 1234)       │
└─────────────────────────────────┘
클라이언트A 연결 후:
┌─────────────────────────────────┐
│ OS Network Stack                │
├─────────────────────────────────┤
│ LISTENING                       │
│ *:12345 → Java Process          │ ← 여전히 존재
├─────────────────────────────────┤
│ ESTABLISHED                     │
│ (서버IP:12345 ↔ 클라A:54321)    │ ← 새로 생성됨
│ → Java Process (같은 PID 1234)  │
└─────────────────────────────────┘
클라이언트B 연결 후:
┌─────────────────────────────────┐
│ OS Network Stack                │
├─────────────────────────────────┤
│ LISTENING                       │
│ *:12345 → Java Process          │
├─────────────────────────────────┤
│ ESTABLISHED                     │
│ (서버IP:12345 ↔ 클라A:54321)    │
│ → Java Process                  │
├─────────────────────────────────┤
│ ESTABLISHED                     │
│ (서버IP:12345 ↔ 클라B:54322)    │ ← 또 생성됨
│ → Java Process (여전히 같은 PID)│
└─────────────────────────────────┘
```
## 소켓의 5-Tuple
### 소켓은 5가지 정보로 구분됩니다
```
(프로토콜, 서버IP, 서버포트, 클라이언트IP, 클라이언트포트)
예시:
Socket1: (TCP, 192.168.0.20, 12345, 192.168.0.10, 54321)
Socket2: (TCP, 192.168.0.20, 12345, 192.168.0.11, 54322)
Socket3: (TCP, 192.168.0.20, 12345, 192.168.0.10, 54323)
서버 포트는 모두 12345로 동일
하지만 클라이언트 정보가 다르므로 구분 가능
```
### OS는 이렇게 패킷을 구분합니다
```
서버에 패킷 도착:
┌─────────────────────────────────┐
│ IP Header                       │
│ Source: 192.168.0.10            │
│ Dest: 192.168.0.20              │
├─────────────────────────────────┤
│ TCP Header                      │
│ Source Port: 54321              │
│ Dest Port: 12345                │
├─────────────────────────────────┤
│ Data: "Hello"                   │
└─────────────────────────────────┘
OS의 판단:
1. 목적지 포트 12345 확인
2. 출발지 IP:Port (192.168.0.10:54321) 확인
3. 5-Tuple 매칭: 
   → (TCP, 192.168.0.20, 12345, 192.168.0.10, 54321)
4. 해당 Socket의 수신 버퍼에 데이터 저장
5. 해당 Socket을 읽고 있는 스레드를 깨움
```
## 포트와 파일 디스크립터
### OS 입장에서 소켓은 "파일"입니다
```java
ServerSocket serverSocket = new ServerSocket(12345);
Socket socket1 = serverSocket.accept();
Socket socket2 = serverSocket.accept();
```
OS에서 실제로 일어나는 일:
```
Java Process (PID 1234)의 파일 디스크립터 테이블:
FD 0: stdin
FD 1: stdout
FD 2: stderr
FD 3: serverSocket (LISTENING on *:12345)  ← ServerSocket
FD 4: socket1 (192.168.0.20:12345 ↔ 192.168.0.10:54321)  ← Socket
FD 5: socket2 (192.168.0.20:12345 ↔ 192.168.0.11:54322)  ← Socket
...
프로세스는 하나
파일 디스크립터는 여러 개
```
Linux에서 확인하는 방법:
```bash
# 서버 실행 중에
lsof -p 1234  # 또는 netstat -anp | grep 1234
출력:
java    1234 user    3u  IPv4  LISTEN      *:12345
java    1234 user    4u  IPv4  ESTABLISHED 192.168.0.20:12345->192.168.0.10:54321
java    1234 user    5u  IPv4  ESTABLISHED 192.168.0.20:12345->192.168.0.11:54322
PID는 모두 1234로 동일
```
## 메모리 구조로 이해하기
### 하나의 Java 프로세스 내부:
```
┌─────────────────────────────────────────────┐
│ Java Process (PID: 1234)                    │
├─────────────────────────────────────────────┤
│ Heap (공유 메모리)                           │
│ ├─ ServerSocket 객체                        │
│ │   └─ OS FD: 3 (LISTENING)                │
│ ├─ Socket 객체 1                            │
│ │   └─ OS FD: 4 (ESTABLISHED)              │
│ ├─ Socket 객체 2                            │
│ │   └─ OS FD: 5 (ESTABLISHED)              │
│ └─ SessionManager                           │
│     └─ sessions = [Session1, Session2]     │
├─────────────────────────────────────────────┤
│ Thread 1: Main Thread                       │
│ ├─ Stack: serverSocket 변수                │
│ └─ accept() 실행 중 (FD 3에서 대기)         │
├─────────────────────────────────────────────┤
│ Thread 2: Worker Thread (Session1)         │
│ ├─ Stack: socket1 변수                     │
│ └─ readUTF() 실행 중 (FD 4에서 대기)       │
├─────────────────────────────────────────────┤
│ Thread 3: Worker Thread (Session2)         │
│ ├─ Stack: socket2 변수                     │
│ └─ readUTF() 실행 중 (FD 5에서 대기)       │
└─────────────────────────────────────────────┘
프로세스는 하나
스레드는 여러 개
소켓(FD)도 여러 개
```
### 클라이언트도 마찬가지입니다
```java
// 클라이언트 코드
Socket socket = new Socket("192.168.0.20", 12345);
```
OS가 하는 일:
```
1. 클라이언트용 임시 포트 할당 (예: 54321)
   - 49152-65535 범위에서 사용 가능한 포트 찾기
2. TCP 3-way handshake:
   클라이언트:54321 → SYN → 서버:12345
   서버:12345 → SYN-ACK → 클라이언트:54321
   클라이언트:54321 → ACK → 서버:12345
3. 연결 성공
   클라이언트의 FD 테이블에 추가:
   Client Process (PID 5678)
   FD 3: (192.168.0.10:54321 ↔ 192.168.0.20:12345)
```
여러 클라이언트를 동시에 실행하면
```bash
# Terminal 1
java Client  # PID 5678, 포트 54321 사용
# Terminal 2
java Client  # PID 5679, 포트 54322 사용 (다른 프로세스)
# Terminal 3
java Client  # PID 5680, 포트 54323 사용 (또 다른 프로세스)
```
각 클라이언트는 별도의 프로세스

| 개념 | 개수 | 역할 | 예시 |
|------|------|------|------|
| 프로세스 | 서버 1개, 클라이언트 N개 | 독립적인 실행 단위 | Java 서버 프로그램 |
| 스레드 | 프로세스당 여러 개 | 프로세스 내 실행 흐름 | Main, Worker1, Worker2... |
| 포트 | 프로세스당 여러 개 | 네트워크 통신 끝점 | 12345, 54321, 54322... |
| 소켓 | 연결당 1개 | 통신 채널 | (IP+Port 조합) |

서버 실행 후:
```bash
# 프로세스 확인
ps aux | grep java
# → PID 1234 (하나만 나옴)
# 열린 포트 확인
netstat -anp | grep 1234
# → 12345 (LISTEN)
# → 12345 (ESTABLISHED) - 클라이언트A
# → 12345 (ESTABLISHED) - 클라이언트B
# 서버 포트는 항상 12345
# 파일 디스크립터 확인
ls -l /proc/1234/fd
# → 3 -> socket:[12345] (ServerSocket)
# → 4 -> socket:[54321] (Socket1)
# → 5 -> socket:[54322] (Socket2)
# 프로세스는 하나, FD는 여러 개
```
- 포트는 프로세스와 무관하게 OS가 관리하는 통신 끝점
- 하나의 프로세스가 수천 개의 포트(소켓)를 동시에 사용할 수 있다.
### 프로세스는 언제 만들어지나
```bash
# 터미널에서 실행
java Server
이 순간
├─ OS가 새로운 프로세스 생성 (fork + exec)
├─ JVM 프로세스 시작
├─ JVM이 Server.class 로드
└─ main() 메서드 실행 ← 이게 프로세스의 "시작점"
```
## 상세한 프로세스 생성 과정
### 1. 터미널에서 `java Server` 입력 시
```
Shell (Bash) 프로세스 (PID: 100)
      ↓
fork() 시스템 콜 호출
      ↓
┌─────────────────────────────┐
│ OS Kernel                   │
│ 새 프로세스 생성             │
│ ├─ 부모: Shell (PID 100)    │
│ └─ 자식: 새 프로세스 (PID 1234) │
└─────────────────────────────┘
      ↓
자식 프로세스가 exec() 시스템 콜 호출
      ↓
"java" 실행 파일로 교체
      ↓
JVM 시작
      ↓
Server.class의 main() 실행
```
### fork() - 프로세스 복제
```c
// Shell이 내부적으로 하는 일
pid_t pid = fork();
if (pid == 0) {
    // 자식 프로세스
    exec("/usr/bin/java", "Server");
} else {
    // 부모 프로세스 (Shell)
    wait(pid); // 자식이 끝날 때까지 대기
}
```
fork()가 하는 일:
```
부모 프로세스 (Shell)
┌─────────────────────────┐
│ Code                    │
│ Data                    │
│ Heap                    │
│ Stack                   │
│ FD Table                │
└─────────────────────────┘
         ↓ fork()
         ↓ 메모리 전체 복사
         ↓
자식 프로세스 (새 PID)
┌─────────────────────────┐
│ Code (동일)              │
│ Data (동일)              │
│ Heap (동일)              │
│ Stack (동일)             │
│ FD Table (동일)          │
└─────────────────────────┘
```
### exec() - 프로그램 교체
```
자식 프로세스의 메모리 영역을 
새로운 프로그램(java)으로 완전히 교체
Before exec:
┌─────────────────────────┐
│ Shell의 Code            │
│ Shell의 Data            │
└─────────────────────────┘
After exec:
┌─────────────────────────┐
│ JVM의 Code              │
│ JVM의 Data              │
│ JVM Heap (비어있음)      │
└─────────────────────────┘
```
### main()은 "진입점"일 뿐
```java
public class Server {
    public static void main(String[] args) { // ← 여기서 프로세스가 시작되는 게 아니라
        // 이미 프로세스는 존재함
        // main()은 그냥 "어디서부터 실행할지" 알려주는 것
    }
}
```
JVM 입장에서:
```
JVM 프로세스 시작 (이미 생성됨)
      ↓
1. 클래스 로더가 Server.class 로드
      ↓
2. 클래스 초기화 (static 블록 실행)
      ↓
3. main() 메서드 찾기
      ↓
4. main() 실행 ← 프로그램의 시작 (프로세스가 시작x)
      ↓
5. main()이 종료되면 JVM 종료 → 프로세스 종료
```
### 프로세스 = 실행 중인 프로그램
```
프로그램 (Server.class):
┌─────────────────────────┐
│ 디스크에 저장된 파일     │
│ - 바이트코드            │
│ - 메타데이터            │
│ (실행 안 됨, 정적)       │
└─────────────────────────┘
프로세스:
┌─────────────────────────┐
│ 메모리에 로드된 상태     │
│ ├─ Code (바이트코드)    │
│ ├─ Data (전역 변수)     │
│ ├─ Heap (객체들)        │
│ ├─ Stack (메서드 호출)  │
│ ├─ FD Table (열린 파일) │
│ └─ PC (Program Counter) │
│ (실행 중, 동적)          │
└─────────────────────────┘
```
### 프로세스의 일생
```
┌─ 탄생 ─────────────────────────────────────┐
│ 1. Shell: fork() + exec("java Server")    │
│    → OS가 새 PID 할당 (예: 1234)          │
│    → 메모리 공간 할당                      │
│    → 스케줄러에 등록 (READY 상태)          │
└────────────────────────────────────────────┘
              ↓
┌─ 초기화 ────────────────────────────────────┐
│ 2. JVM 시작                                │
│    → JVM 내부 초기화                       │
│    → 클래스 로더 초기화                     │
│    → 메인 스레드 생성                       │
└────────────────────────────────────────────┘
              ↓
┌─ 실행 ──────────────────────────────────────┐
│ 3. main() 실행                             │
│    → ServerSocket 생성 (OS에 포트 등록)    │
│    → 스레드 풀 생성 (10개 스레드 추가 생성) │
│    → accept() 루프 (클라이언트 대기)       │
│ 프로세스 1234:                             │
│ ├─ 메인 스레드 (accept 대기)               │
│ ├─ 워커 스레드 1 (Session 처리)            │
│ ├─ 워커 스레드 2 (Session 처리)            │
│ ├─ ...                                    │
│ └─ GC 스레드들 (백그라운드)                │
└────────────────────────────────────────────┘
              ↓
┌─ 종료 ──────────────────────────────────────┐
│ 4. Ctrl+C 또는 main() 종료                 │
│    → 셧다운 훅 실행                        │
│    → 모든 스레드 정리                       │
│    → 소켓 닫기 (OS에서 포트 해제)          │
│    → JVM 종료                              │
│    → OS가 프로세스 리소스 회수              │
│    → PID 1234 제거                        │
└────────────────────────────────────────────┘
```
### OS의 프로세스 테이블
```
┌─────────────────────────────────────────────┐
│ OS Process Table                            │
├─────────────────────────────────────────────┤
│ PID: 100  (Shell)                           │
│ ├─ State: RUNNING                           │
│ ├─ Memory: 0x1000000 - 0x2000000          │
│ └─ Open Files: stdin, stdout, stderr       │
├─────────────────────────────────────────────┤
│ PID: 1234 (Java Server)                    │
│ ├─ State: RUNNING                           │
│ ├─ Parent: 100 (Shell)                     │
│ ├─ Memory: 0x3000000 - 0x5000000          │
│ ├─ Threads: 13개 (Main + 10 Worker + 2 GC)│
│ └─ Open Files:                              │
│     ├─ FD 3: ServerSocket (port 12345)     │
│     ├─ FD 4: Socket (ESTABLISHED)          │
│     └─ FD 5: Socket (ESTABLISHED)          │
├─────────────────────────────────────────────┤
│ PID: 1235 (Client 1) ← 클라이언트 프로세스   │
│ ├─ State: RUNNING                           │
│ ├─ Memory: 0x6000000 - 0x7000000          │
│ └─ Open Files:                              │
│     └─ FD 3: Socket (to 12345)             │
├─────────────────────────────────────────────┤
│ PID: 1236 (Client 2) ← 또 다른 프로세스      │
│ ...                                         │
└─────────────────────────────────────────────┘
```
### 프로세스가 생성되는 시점:
- java Server 명령어 실행 시 (정확히는 OS의 fork() + exec() 시)
### 프로세스가 하는 일:
```
1. 프로그램 실행의 "컨테이너" 역할
   └─ 메모리 공간 제공
   └─ 리소스 관리 (파일, 소켓 등)
2. 스레드들의 "집"
   └─ 메인 스레드 포함
   └─ 워커 스레드들
   └─ 모두 같은 메모리 공간 공유
3. OS가 관리하는 "실행 단위"
   └─ CPU 시간 할당받음
   └─ 우선순위 관리됨
   └─ 독립적으로 종료 가능
```
```bash
# 1. 서버 실행 전
ps aux | grep java
# → (아무것도 없음)
# 2. java Server 실행
ps aux | grep java
# → 1234 ... java Server (프로세스 생성됨)
# 3. 프로세스 상세 정보
cat /proc/1234/status
# Name: java
# State: S (sleeping - accept 대기 중)
# Threads: 13
# ...
# 4. 메모리 맵
cat /proc/1234/maps
# 3000000-3100000 r-xp ... (Code Segment)
# 3100000-3200000 rw-p ... (Data Segment)
# 4000000-5000000 rw-p ... (Heap)
# 7fff0000-7fff1000 rw-p ... (Stack - Main Thread)
# 7ffe0000-7ffe1000 rw-p ... (Stack - Worker Thread 1)
# ...
# 5. 프로세스 종료
kill 1234
# → 프로세스 완전 소멸
# → 모든 스레드 종료
# → 모든 소켓 닫힘
# → 메모리 회수
```