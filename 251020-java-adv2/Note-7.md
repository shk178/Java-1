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