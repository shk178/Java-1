- OS Backlog Queue에 클라이언트와 서버의 TCP 연결 정보를 보관한다.
- 서버소켓은 클라이언트와 서버의 TCP 연결만 지원
- 클라이언트와 서버가 정보를 주고 받으려면 소켓 객체가 필요
- 서버소켓.accept하면 OS 백로그 큐에 연결 정보가 생성될 때까지 대기한다. (블로킹)
- 백로그 큐에 연결 정보가 생성되면 그걸 바탕으로 소켓 객체를 생성한다. (연결 정보는 제거됨)
- TCP 연결 정보가 생성된다: OS가 연결하는 것까지 한다.
- 그걸 소켓 객체가 가져오는 것이다.
- 소켓 객체: 클라이언트와 서버가 데이터를 주고 받기 위한 스트림을 제공
- InputStream: 서버 입장에서 보면 클라이언트가 전달한 데이터를 서버가 받을 때 사용한다.
- OutputStream: 서버에서 클라이언트에 데이터를 전달할 때 사용한다.
- 클라이언트의 Output은 서버의 Input, 서버의 Output은 클라이언트의 Input이다.
- Client2 / Server2
```java
System.out.println(input.readUTF());
//클라이언트2 종료했는데 서버가 종료 안 된 경우
//Exception in thread "main" java.io.EOFException
//at java.base/java.io.DataInputStream.readFully(DataInputStream.java:210)
//at java.base/java.io.DataInputStream.readUnsignedShort(DataInputStream.java:341)
//at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:575)
//at java.base/java.io.DataInputStream.readUTF(DataInputStream.java:550)
//at network.Server2.main(Server2.java:22)
```
- java.io.EOFException: 한쪽이 소켓을 닫았는데, 다른 쪽은 계속 읽으려 할 때 발생
- SocketException: Connection reset: 상대가 연결을 끊었을 때
- StreamCorruptedException: write/read 순서가 맞지 않거나, UTF 데이터 스트림 깨졌을 때
- 해결 방법 ① 단방향 통신으로 설계
- 클라이언트만 보내고 서버만 받는다 (혹은 반대로)
```java
// Server2.java
while (true) {
    String msg = input.readUTF();
    if (msg.equals("종료")) break;
    System.out.println("클라이언트: " + msg);
}
// Client2.java
while (true) {
    System.out.print("전송할 내용: ");
    String msg = content.nextLine();
    output.writeUTF(msg);
    if (msg.equals("종료")) break;
}
```
- ② 양방향 채팅으로 만들고 싶다면 (Thread 사용)
- 서버와 클라이언트 모두 읽기와 쓰기를 서로 다른 스레드에서 처리
- 예시:
```java
import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Client3 {
    public static final int PORT = 12345;
    public static void main(String[] args) {
        System.out.println("[클라이언트 시작]");
        try (Socket socket = new Socket("localhost", PORT)) {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            // 서버 메시지 수신 스레드
            Thread receiver = new Thread(() -> {
                try {
                    while (true) {
                        String msg = input.readUTF();
                        if (msg.equals("종료")) {
                            System.out.println("\n[서버가 연결을 종료했습니다]");
                            break;
                        }
                        System.out.println("\n서버: " + msg);
                        System.out.print("전송할 내용: ");
                    }
                } catch (IOException e) {
                    System.out.println("[서버 연결 종료]");
                }
            });
            receiver.start();
            // 사용자 입력 송신 루프
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("전송할 내용: ");
                String msg = sc.nextLine();
                output.writeUTF(msg);
                if (msg.equals("종료")) {
                    System.out.println("[클라이언트 종료]");
                    break;
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
```java
import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Server3 {
    private static final int PORT = 12345;
    public static void main(String[] args) {
        System.out.println("[서버 시작]");
        try (ServerSocket server = new ServerSocket(PORT)) {
            Socket socket = server.accept();
            System.out.println("[클라이언트 연결됨]");
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            // 수신 스레드 (클라이언트 메시지 수신)
            Thread receiver = new Thread(() -> {
                try {
                    while (true) {
                        String msg = input.readUTF();
                        if (msg.equals("종료")) {
                            System.out.println("\n[클라이언트가 연결을 종료했습니다]");
                            break;
                        }
                        System.out.println("\n클라이언트: " + msg);
                        System.out.print("전송할 내용: "); // 사용자 입력 안내 다시 표시
                    }
                } catch (IOException e) {
                    System.out.println("[클라이언트 연결 종료]");
                }
            });
            receiver.start();
            // 송신 루프 (서버 -> 클라이언트)
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.print("전송할 내용: ");
                String msg = sc.nextLine();
                output.writeUTF(msg);
                if (msg.equals("종료")) {
                    System.out.println("[서버 종료]");
                    break;
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```
- 클라이언트
- 애플리케이션 소켓 - OS TCP 송신 버퍼 - 클라이언트 네트워크 카드
- 서버
- 서버 네트워크 카드 - OS TCP 수신 버퍼 - 애플리케이션 소켓
- 클라이언트가 서버에 TCP 연결 요청해서 완료했을 때 클라이언트의 소켓은 생성돼있다.
- 서버의 소켓은 .accpet해야 생성되는데, 그전에도 연결은 완료돼있고 클라이언트가 메시지 보내면 OS TCP 수신 버퍼에 추가된다.
- TCP 연결이 완료됐다. 서버의 서버소켓 (서버의 소켓과 다름) + 클라이언트 소켓으로 되는 것이다.
- .accept() = 백로그 큐에 연결 정보가 도착할 때까지 블로킹 상태로 대기하게 하는 메서드
- .readXxx() = 클라이언트의 메시지를 받을 때까지 블로킹 상태로 대기하게 하는 메서드
- 클라이언트가 2개 이상 접속: 여러 스레드를 만들어서 해결해야 한다.
### 1단계: 서버 시작
```
[서버]
- Server 프로그램 실행
- ServerSocket이 12345 포트를 열고 대기 상태로 들어감
- "서버 시작, 포트: 12345" 메시지 출력
- while(true) 루프로 클라이언트 연결을 무한 대기
```
### 2단계: 클라이언트 접속
```
[클라이언트]
- Client 프로그램 실행
- localhost:12345로 서버에 연결 시도
- Socket 연결 성공
[서버]
- serverSocket.accept()가 클라이언트 연결을 감지
- "클라이언트 연결됨" 메시지 출력
- 새로운 Session 객체 생성
- Session을 실행할 새 Thread 시작
- 다시 다음 클라이언트를 기다림 (여러 클라이언트 동시 처리 가능)
```
### 3단계: 양방향 통신 준비
```
[클라이언트]
- DataInputStream과 DataOutputStream 생성
- 두 개의 역할로 나뉨:
  ① receiver 스레드: 서버가 보낸 메시지를 계속 듣고 있음
  ② main 스레드: 콘솔에서 입력받아 서버로 전송
[서버 - Session]
- DataInputStream과 DataOutputStream 생성
- 마찬가지로 두 개의 역할:
  ① receiver 스레드: 클라이언트가 보낸 메시지를 계속 듣고 있음
  ② main 스레드: 콘솔에서 입력받아 클라이언트로 전송
```
### 4단계: 실제 대화 시나리오
시나리오 A: 클라이언트가 먼저 말함
```
[클라이언트 콘솔]
보낼 내용: 안녕하세요!
(Enter 입력)
[클라이언트 main 스레드]
- "안녕하세요!"를 output.writeUTF()로 서버에 전송
- 계속 다음 입력을 기다림
[서버 Session의 receiver 스레드]
- input.readUTF()가 "안녕하세요!"를 받음
- "받은 내용: 안녕하세요!" 출력
[서버 콘솔]
받은 내용: 안녕하세요!
보낼 내용: _
```
시나리오 B: 서버가 답장함
```
[서버 콘솔]
보낼 내용: 반갑습니다!
(Enter 입력)
[서버 Session의 main 스레드]
- "반갑습니다!"를 output.writeUTF()로 클라이언트에 전송
[클라이언트 receiver 스레드]
- input.readUTF()가 "반갑습니다!"를 받음
- "받은 내용: 반갑습니다!" 출력
[클라이언트 콘솔]
받은 내용: 반갑습니다!
보낼 내용: _
```
### 5단계: 종료 시나리오
케이스 1: 클라이언트가 종료
```
[클라이언트 콘솔]
보낼 내용: 종료
(Enter 입력)
[클라이언트 main 스레드]
- "종료"를 서버에 전송
- "연결 종료 시도" 출력
- while 루프 탈출
[서버 Session의 receiver 스레드]
- "종료" 메시지 수신
- "클라이언트가 연결 종료함" 출력
- while 루프 탈출
[클라이언트]
- receiver.join(3000) 실행: receiver 스레드가 끝날 때까지 최대 3초 대기
- finally 블록 실행:
  1. Scanner 닫기
  2. DataOutputStream 닫기
  3. DataInputStream 닫기
  4. Socket 닫기
- 프로그램 종료
[서버 Session]
- 마찬가지로 finally 블록에서 모든 자원 정리
- Session 스레드 종료
- 하지만 Server의 main은 계속 실행 중 (다른 클라이언트 대기)
```
케이스 2: 서버가 종료
```
[서버 콘솔]
보낼 내용: 종료
(Enter 입력)
[서버 Session의 main 스레드]
- "종료"를 클라이언트에 전송
- "연결 종료 시도" 출력
- while 루프 탈출
[클라이언트 receiver 스레드]
- "종료" 메시지 수신
- "서버가 연결 종료함" 출력
- while 루프 탈출
[양쪽 모두]
- finally 블록에서 자원 정리 후 연결 종료
```
## 스레드 흐름도
```
클라이언트                          서버
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[main 스레드]                    [main 스레드]
   └─ 입력 대기                    └─ accept() 대기
   └─ 메시지 전송 ───────────>        └─ Session 생성
                                         └─ Thread 시작
[receiver 스레드]                           
   └─ 수신 대기    <───────────    [Session의 main 스레드]
   └─ 메시지 출력                      └─ 입력 대기
                                       └─ 메시지 전송
                                    [Session의 receiver 스레드]
                                       └─ 수신 대기
                                       └─ 메시지 출력
시간 ─────────────────────────────────────>
Server Main Thread:
├─ 시작
├─ ServerSocket 생성
├─ while(true) 진입
├─ accept() ─> 클라이언트1 연결 ─> Session1 Thread 생성
├─ accept() ─> 클라이언트2 연결 ─> Session2 Thread 생성
├─ accept() ─> 대기중...
├─ accept() ─> 대기중...
├─ accept() ─> 대기중...
└─ [무한 계속...]
Session1 Thread:
    ├─ 시작
    ├─ 통신
    └─ 종료 (클라이언트1이 "종료" 보냄)
Session2 Thread:
    ├─ 시작
    ├─ 통신
    └─ 종료 (클라이언트2가 "종료" 보냄)
→ Session들은 종료되어도 Server Main Thread는 계속 실행
- 종료 방법: Ctrl+C 또는 강제 종료뿐
```
- 동시성: 각 프로그램이 2개의 스레드로 동시에 송신/수신
- 독립성: 보내는 것과 받는 것이 서로 방해하지 않음
- 비동기: 언제든지 메시지를 주고받을 수 있음
- 자원 관리: 종료 시 finally 블록에서 모든 자원을 안전하게 닫음
- 다중 접속: 서버는 여러 클라이언트를 동시에 처리 가능 (각각 새 Thread)
## network3
- 1. 중복 제거: Client와 Session의 공통 로직을 별도 클래스로 분리
- 2. 단일 책임: 각 클래스가 하나의 역할만 수행
- 3. 명확한 흐름: 위에서 아래로 읽으면 이해되는 구조
- 4. 안전한 종료: 자원 관리를 한 곳에서 처리
### 1. 중복 제거
```
기존: Client와 Session이 똑같은 코드 반복
개선: ChatConnection 하나로 통합
```
### 2. 명확한 책임
```
ChatConnection → 양방향 통신 담당
Client        → 서버 연결만 담당
Session       → 클라이언트별 처리만 담당
Server        → 연결 수락만 담당
```
### 3. 자동 자원 관리
```java
// try-with-resources로 자동 close()
try (ChatConnection connection = new ChatConnection(socket, "Client")) {
    connection.start();
    connection.waitForCompletion();
} // 자동으로 connection.close() 호출
```
### 4. 안전한 종료
```java
// volatile로 스레드 간 안전한 플래그
private volatile boolean running = true;
// Ctrl+C 처리
Runtime.getRuntime().addShutdownHook(...)
```
### 5. 확장성
```java
// 새로운 기능 추가하고 싶다면
// → ChatConnection만 수정하면 됨
// 예: 메시지 암호화
private String encrypt(String msg) { ... }
private String decrypt(String msg) { ... }
// 예: 메시지 로깅
private void logMessage(String msg) { ... }
// 예: 파일 전송
public void sendFile(File file) { ... }
```
## 실행 흐름
```
[서버 시작]
Server.main()
  → ServerSocket 생성 (포트 12345)
  → while(running) 대기
[클라이언트 연결]
Client.main()
  → Socket 연결
  → ChatConnection 생성
  → start() → receiver/sender 스레드 시작
Server
  → accept() 성공
  → Session 생성 및 Thread 시작
Session.run()
  → ChatConnection 생성
  → start() → receiver/sender 스레드 시작
[통신]
Client의 sender → 메시지 전송 → Session의 receiver
Session의 sender → 메시지 전송 → Client의 receiver
[종료]
Client 또는 Session이 "종료" 입력
  → stop() 호출
  → running = false
  → 스레드 종료
  → close() 자동 호출
  → 자원 정리
```
- Server는 종료되지 않았습니다. Session만 종료되었을 뿐이에요.
- Server는 `while(running)` 루프로 계속 새로운 클라이언트를 기다리고 있습니다.
- 이건 의도된 동작입니다.
## 방법 1: Ctrl+C로 종료 (권장)
콘솔에서 Ctrl+C를 누르면 이미 ShutdownHook이 등록되어 있어서 우아하게 종료됩니다.
## 방법 2: 별도 관리 콘솔 추가
```java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class Server {
    private static final int PORT = 12345;
    private static volatile boolean running = true;
    private static int sessionCounter = 0;
    
    public static void main(String[] args) {
        System.out.println("=== 서버 시작 ===");
        System.out.println("'quit' 입력 시 서버 종료");
        System.out.println();
        
        // Ctrl+C로 우아하게 종료
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n서버 종료 신호 받음");
            running = false;
        }));
        
        // 서버 관리 콘솔 스레드 (quit 명령 대기)
        Thread consoleThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (running) {
                    String command = scanner.nextLine().trim();
                    
                    if ("quit".equalsIgnoreCase(command)) {
                        System.out.println("서버를 종료합니다...");
                        running = false;
                        break;
                    } else if ("status".equalsIgnoreCase(command)) {
                        System.out.println("현재 세션 수: " + sessionCounter);
                    } else if (!command.isEmpty()) {
                        System.out.println("알 수 없는 명령어: " + command);
                        System.out.println("사용 가능 명령: quit, status");
                    }
                }
            }
        }, "ServerConsole");
        consoleThread.setDaemon(true); // 메인 스레드 종료 시 함께 종료
        consoleThread.start();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            // 1초마다 타임아웃으로 running 체크 가능하게
            serverSocket.setSoTimeout(1000);
            
            System.out.println("포트 " + PORT + "에서 대기 중...");
            
            // running이 true인 동안 계속 클라이언트 받기
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    // 새로운 세션 생성 및 시작
                    int sessionId = ++sessionCounter;
                    Session session = new Session(clientSocket, sessionId);
                    Thread thread = new Thread(session, "Session-" + sessionId);
                    thread.start();
                    
                } catch (SocketTimeoutException e) {
                    // 타임아웃은 정상 - running 체크를 위함
                    continue;
                }
            }
            
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
        }
        
        System.out.println("=== 서버 종료 ===");
    }
}
```
```
=== 서버 시작 ===
'quit' 입력 시 서버 종료
포트 12345에서 대기 중...
[여기서 명령어 입력 가능]
status          ← 현재 세션 수 확인
현재 세션 수: 1
quit            ← 서버 종료
서버를 종료합니다...
=== 서버 종료 ===
```
```
Server 시작
  ├─ Main Thread: accept() 대기
  └─ Console Thread: 명령어 입력 대기
Client 연결
  ├─ Main Thread: Session 생성 → 계속 대기
  └─ Session Thread: 통신 시작
Session 종료 (클라이언트 "종료")
  ├─ Session Thread: 종료됨
  └─ Main Thread: 여전히 실행 중 (다음 클라이언트 대기)
Server 종료 (콘솔에서 "quit" 입력 또는 Ctrl+C)
  ├─ running = false
  ├─ Main Thread: while 탈출 → 종료
  └─ Console Thread: daemon이라 자동 종료
```