## 자바 소켓 프로그래밍
### 소켓
- 네트워크 통신의 엔드포인트
- 애플리케이션이 네트워크를 통해 데이터를 송수신할 수 있게 해주는 프로그래밍 인터페이스
- BSD 소켓 API에서 유래
- IP 주소와 포트 번호의 조합으로 고유하게 식별됨
- 자바에서는 java.net 패키지를 통해 소켓 프로그래밍을 지원
- 자바 소켓 클래스들은 내부적으로 네이티브 메서드를 호출해 운영체제의 소켓 API와 상호작용
- 스트림 소켓(TCP)과 데이터그램 소켓(UDP)로 구분
- Socket 클래스는 클라이언트 측 연결을 담당
- ServerSocket 클래스는 서버 측 리스닝을 담당
- IP 주소: 네트워크 상의 호스트를 식별하는 논리적 주소
- IPv4는 32비트, IPv6는 128비트 주소 체계
- 포트 번호: 호스트 내에서 특정 프로세스나 서비스를 구분하는 16비트 숫자
- 0-1023은 well-known 포트, 1024-49151은 등록된 포트
- 소켓 주소: IP 주소와 포트 번호의 조합으로 표현되는 고유의 식별자
- 예: 192.168.1.100:8080
### TCP, UDP
- TCP: 연결 지향 프로토콜로, 신뢰성 있는 데이터 전송을 보장
- 3-way handshake를 통해 연결을 수립하고, 순서 보장, 흐름 제어, 오류 검출 및 재전송 메커니즘을 제공
- UDP: 비연결형 프로토콜, 오버헤드가 적고 빠르지만 신뢰성을 보장하지 않음
- 실시간 스트리밍이나 DNS 쿼리 같은 경우에 적합
### Socket 클래스
- 클라이언트 측에서 서버에 연결하기 위한 TCP 소켓
- SocketImpl 추상 클래스를 통해 플랫폼별 구현과 분리되어 있다.
- 기본적으로 PlainSocketImpl을 사용한다.
- connect(SocketAddress endpoint): 원격 호스트에 연결
- getInputStream(): 데이터 읽기를 위한 InputStream 반환
- getOutputStream(): 데이터 쓰기를 위한 OutputStream 반환
- close(): 소켓 연결 종료 및 리소스 해제
### ServerSocket 클래스
- 서버 측에서 클라이언트 연결을 수신하기 위한 TCP 소켓
- 특정 포트에 바인딩되어 들어오는 연결 요청을 리스닝
- bind(SocketAddress endpoint): 특정 주소와 포트에 바인딩
- accept(): 클라이언트 연결 대기 및 Socket 객체 반환
- setSoTimeout(int timeout): accept() 타임아웃 설정
- close(): 서버 소켓 종료
### DatagramSocket 클래스
- UDP 통신을 위한 소켓, 비연결형 데이터그램 패킷을 송수신
- 연결 수립 과정 없이 즉시 데이터 전송
- send(DatagramPacket p): 데이터그램 패킷 전송
- receive(DatagramPacket p): 데이터그램 패킷 수신
- connect(InetAddress address, int port): 특정 주소로 제한
### InetAddress 클래스
- IP 주소를 나타내는 클래스, 호스트 이름과 IP 주소 간의 변환 기능을 제공
- DNS 조회를 수행해 호스트 이름을 IP 주소로 해석
- getByName(String host): 호스트 이름으로 InetAddress 생성
- getLocalHost(): 로컬 호스트의 InetAddress 반환
- getHostAddress(): IP 주소 문자열 반환
### TCP 소켓 연결 수립 과정
- TCP 연결은 3-way handshake 프로토콜을 통해 수립된다.
- 클라이언트와 서버는 연결을 위한 초기 시퀀스 번호를 교환하고, 양방향 통신 채널을 확립한다.
- 자바의 Socket 클래스는 이 복잡한 과정을 내부적으로 처리한다.
- 1. SYN: 클라이언트가 서버에 연결 요청을 보낸다.
- new Socket(host, port) 호출 시 SYN 패킷이 전송되며, 초기 시퀀스 번호(ISN)를 포함한다.
- 2. SYN-ACK: 서버가 클라이언트의 요청을 승인하고 자신의 시퀀스 번호를 전송한다.
- ServerSocket.accept()가 대기 중일 때 응답이 발생한다.
- 3. ACK: 클라이언트가 서버의 응답을 확인하며 연결이 확립된다.
- 이 시점에서 Socket 객체가 완전히 초기화되고 데이터 전송이 가능해진다.
- 클라이언트 코드 예제
```java
// Socket 생성 시 자동으로 연결 시도
Socket socket = new Socket("localhost", 8080);
// 또는 명시적 연결
Socket socket = new Socket();
socket.connect(new InetSocketAddress("localhost", 8080), 5000); // 타임아웃 5초
// 스트림 획득 및 데이터 송수신
OutputStream out = socket.getOutputStream();
InputStream in = socket.getInputStream();
```
- 서버 코드 예제
```java
// 포트 8080에 바인딩
ServerSocket serverSocket = new ServerSocket(8080);
// 백로드 큐 크기 지정 가능
ServerSocket serverSocket = new ServerSocket(8080, 50);
// 클라이언트 연결 대기 (블로킹)
Socket clientSocket = serverSocket.accept();
// 클라이언트와 통신
InputStream in = clientSocket.getInputStream();
OutputStream out = clientSocket.getOutputStream();
```
### 소켓의 내부 구현과 네이티브 계층
- 자바 소켓은 플랫폼 독립성을 제공하면서도 네이티브 시스템 콜을 활용해 고성능 달성
- JVM은 JNI(Java Native Interface)를 통해 C/C++로 작성된 네이티브 코드를 호출하고, 이는 다시 운영체제의 소켓 API를 호출
- 자바 소켓은 여러 계층을 거치지만, JNI 호출 오버헤드는 네트워크 지연에 비해 작다.
- 오히려 버퍼 크기 조정, 블로킹/논블로킹 모드 선택, 그리고 적절한 타임아웃 설정이 성능에 영향이 크다.
- 1. 자바 애플리케이션 레벨
- 개발자는 Socket, ServerSocket 등 고수준 API를 사용한다.
- 이 클래스들은 자바 코드로 작성돼 있고, 내부적으로 SocketImpl을 사용한다.
- 2. SocketImpl 추상화 계층
- SocketImpl은 실제 소켓 구현을 추상화하는 클래스다.
- PlainSocketImpl, SocksSocketImpl 등의 구현체가 존재하며, 각각 다른 프로토콜이나 프록시를 지원한다.
- 이 계층에서는 네이티브 메서드가 선언된다.
- 3. JNI 브리지
- SocketImpl의 네이티브 메서드(socketCreate, socketConnect, socketBind 등)는 JNI를 통해 C/C++ 코드로 연결된다.
- OpenJDK의 경우 src/java.base/unix/native/libnet 디렉토리에 구현되어 있다.
- 4. 네이티브 라이브러리
- C/C++로 작성된 네이티브 코드는 운영체제의 시스템 콜을 직접 호출한다.
- UNIX 계열에서는 socket(), bind(), listen(), accept(), connect() 등의 POSIX API를, Windows에서는 Winsock API를 사용한다.
- 5. 운영체제 커널
- 커널은 실제 네트워크 스택을 관리하며, TCP/IP 프로토콜을 구현한다.
- 소켓 버퍼 관리, 패킷 라우팅, 흐름 제어 등의 저수준 작업이 커널 공간에서 수행된다.
### 블로킹/논블로킹 소켓
- 블로킹 I/O (java.net 패키지)
- Socket, ServerSocket은 블로킹 방식으로 동작
- I/O 작업이 완료될 때까지 스레드가 대기한다.
- 구현은 간단하지만 동시 연결 수가 많을 경우 스레드 풀 고갈 문제가 발생할 수 있다.
- read(), write(), accpet() 호출 시 완료될 때까지 블로킹
- 각 연결마다 별도의 스레드 필요 (Thread-per-Connection 모델)
- 코드가 직관적이고 이해하기 쉬움
- 컨텍스트 스위칭 오버헤드 발생 가능
- 논블로킹 I/O (java.nio 패키지)
- 자바 NIO (New I/O)는 논블로킹 방식의 소켓 프로그래밍을 지원한다.
- Selector를 사용하여 단일 스레드로 여러 채널을 관리할 수 있어, C10K 문제(10,000개 동시 연결)를 해결할 수 있다.
- I/O 작업이 즉시 반환되며, 준비 상태를 폴링
- Selector를 통한 이벤트 기반 처리
- 단일 스레드로 수천 개의 연결 처리 가능
- 복잡한 코드 구조와 학습 곡선
- 블로킹 I/O - 동시 연결 수가 적고 (<1000), 각 연결이 장시간 유지되는 경우, 구현이 단순하고 디버깅이 쉬움
```java
// 클라이언트 소켓을 생성하고 "host" 주소의 8080 포트에 연결을 시도합니다.
Socket socket = new Socket("host", 8080);
// 연결된 소켓으로부터 입력 스트림을 가져옵니다. 서버로부터 데이터를 읽기 위해 사용됩니다.
InputStream in = socket.getInputStream();
// 서버로부터 데이터가 도착할 때까지 현재 스레드는 블로킹됩니다.
int data = in.read();
// 멀티스레드 서버 패턴: 클라이언트 요청을 처리하기 위해 새로운 스레드를 생성합니다.
while (true) {
    // 클라이언트의 연결 요청을 수락합니다. 이 메서드는 블로킹됩니다.
    Socket client = serverSocket.accept();
    // 클라이언트 처리를 위한 새로운 스레드를 생성하고 실행합니다.
    new Thread(() -> handleClient(client)).start();
}
```
- 논블로킹 I/O - 대량의 동시 연결을 처리해야 하는 경우, 리소스 효율성이 중요한 고성능 서버 애플리케이션
```java
// 서버 소켓 채널을 생성합니다.
ServerSocketChannel server = ServerSocketChannel.open();
// 채널을 논블로킹 모드로 설정합니다. 클라이언트 연결을 기다릴 때 블로킹되지 않도록 합니다.
server.configureBlocking(false);
// 포트 8080에 바인딩하여 클라이언트 연결을 받을 준비를 합니다.
server.bind(new InetSocketAddress(8080));
// 셀렉터를 생성합니다. 여러 채널의 이벤트를 감지할 수 있는 객체입니다.
Selector selector = Selector.open();
// 서버 채널을 셀렉터에 등록하고, 연결 수락(ACCEPT) 이벤트를 감지하도록 설정합니다.
server.register(selector, SelectionKey.OP_ACCEPT);
// 이벤트 루프를 시작합니다. 계속해서 클라이언트 연결을 감지합니다.
while (true) {
    // 셀렉터가 감지한 이벤트가 있을 때까지 블로킹됩니다.
    selector.select();
    // 셀렉터가 감지한 이벤트 키들을 가져옵니다.
    Set keys = selector.selectedKeys();
    // 준비된 채널만 처리합니다. (여기서 실제 처리 로직이 들어가야 합니다.)
}
```
### 소켓 옵션과 성능 튜닝
- SO_TIMEOUT
- 블로킹 read() 작업을 밀리초 단위로 설정한다.
- 0으로 설정 시 무한 대기하며, 타임아웃 발생 시 SocketTimeoutException이 발생한다.
```java
socket.setSoTimeout(5000); // 5초
int data = in.read(); // 5초 내 응답 없으면 예외
// 무응답 클라이언트로부터 보호, 리소스 누수 방지
```
- SO_REUSEADDER
- TIME_WAIT 상태의 소켓 주소를 재사용할 수 있게 한다.
- 서버 재시작 시 "Address already in use" 오류를 방지한다.
```java
serverSocket.setReuseAddress(true);
serverSocket.bind(new InetSocketAddress(8080));
// 서버 빠른 재시작, 개발 환경에서 유용
```
- SO_RCVBUF / SO_SNDBUF
- 수신 및 송신 버퍼의 크기를 바이트 단위로 설정한다.
- 버퍼 크기는 처리량에 직접적인 영향을 미치며, 특히 고대역폭 네트워크에서 중요하다.
```java
socket.setReceiveBufferSize(65536); // 64KB
socket.setSendBufferSize(65536);
// BDP (Bandwidth-Delay Product) = 대역폭 x RTT
```
- SO_KEEPALIVE
- 연결 유지 프로브를 활성화하여 비활성 연결을 감지한다.
- 약 2시간마다 프로브 패킷을 전송하여 상대방이 여전히 있는지 확인한다.
```java
socket.setKeepAlive(true);
// 장시간 유지되는 연결, 방화벽 타임아웃 방지
```
- TCP_NODELAY
- Nagle 알고리즘을 비활성화한다.
- Nagle은 작은 패킷들을 모아서 전송하는 최적화 기법이지만, 지연에 민감한 애플리케이션에서는 역효과가 난다.
```java
socket.setTcpNoDelay(true);
// 실시간 게임, 원격 제어, 대화형 애플리케이션
```
- SO_LINGER
- close() 호출 시 전송되지 않은 데이터의 처리 방식을 제어한다.
- linger 시간 동안 데이터 전송을 시도한 후 소켓을 종료한다.
```java
// 5초 동안 전송 시도 후 종료
socket.setSoLinger(true, 5);
// 과도한 linger 시간은 리소스 점유 시간을 늘린다.
```
### 베스트 프랙티스
- 1. 리소스 관리는 Try-With-Resources로
- Socket과 관련 스트림은 명시적으로 닫아야 하는 리소스다.
- 2. 버퍼링을 통한 성능 최적화
- raw InputStream/OutputStream을 직접 사용하면 매번 시스템 콜이 발생하여 성능이 저하된다.
- BufferedInputStream/BufferedOutputStream으로 래핑하여 데이터를 버퍼에 모았다가 한 번에 전송하면 시스템 콜 횟수를 줄일 수 있다.
- 3. 타임아웃 설정은 필수
- 네트워크는 신뢰할 수 없는 환경이다.
- 클라이언트가 응답하지 않거나, 네트워크가 끊어지거나, 서버가 과부하 상태일 수 있다.
- 모든 블로킹 작업에 적절한 타임아웃을 설정하여 무한 대기를 방지해야 한다.
- 4. 예외 처리의 세분화
- SocketTimeoutExcpetion, ConnectException, SocketException 등을 구분하여 처리하면 더 나은 에러 핸들링과 복구 전략을 구현할 수 있다.
- 5. 멀티스레딩과 스레드 풀 활용
- 서버는 여러 클라이언트를 동시에 처리해야 한다.
- 각 연결마다 새 스레드를 생성하는 것은 리소스 낭비이므로, ExecutorService를 사용한 스레드 풀 패턴을 적용한다.
- 이를 통해 스레드 생성 오버헤드를 줄이고 시스템 리소스를 효율적으로 관리할 수 있다.
### NIO.2와 비동기 소켓
- NIO.2(AIO)는 완전한 비동기 I/O를 제공한다.
- AsynchronousSocketChannel과 AsynchronousServerSocketChannel을 통해 콜백 기반의 논블로킹 프로그래밍이 가능하다.
- CompletionHandler 인터페이스를 통해 I/O 완료 시점에 처리 로직을 실행할 수 있다.
- 비동기 서버 예제
```java
// 비동기 서버 소켓 채널을 생성합니다.
AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
// 포트 8080에 바인딩하여 클라이언트 연결을 받을 준비를 합니다.
server.bind(new InetSocketAddress(8080));
// 클라이언트 연결을 비동기적으로 수락합니다.
// 첫 번째 인자는 첨부 객체 (여기선 사용하지 않으므로 null),
// 두 번째 인자는 연결 완료 시 호출될 CompletionHandler입니다.
server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
    @Override
    public void completed(AsynchronousSocketChannel client, Void attachment) {
        // 다음 클라이언트 연결을 계속해서 수락하기 위해 다시 accept를 호출합니다.
        server.accept(null, this);
        // 클라이언트로부터 데이터를 읽기 위한 버퍼를 생성합니다.
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        // 클라이언트 채널로부터 데이터를 비동기적으로 읽습니다.
        // 읽기가 완료되면 내부 CompletionHandler가 호출됩니다.
        client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer buf) {
                // 읽기 완료 후 처리 로직을 여기에 작성합니다.
                // 예: 버퍼를 플립하고 데이터를 디코딩하거나 응답 전송 등
            }
            @Override
            public void failed(Throwable exc, ByteBuffer buf) {
                // 읽기 실패 시 처리 로직을 여기에 작성합니다.
            }
        });
    }
    @Override
    public void failed(Throwable exc, Void attachment) {
        // 클라이언트 연결 수락 실패 시 처리 로직을 여기에 작성합니다.
    }
});
```
- 비동기 클라이언트 예제
```java
// 비동기 클라이언트 소켓 채널을 생성합니다.
AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
// 서버의 "host" 주소와 8080 포트에 비동기적으로 연결을 시도합니다.
// 첫 번째 인자는 첨부 객체 (여기선 null), 두 번째 인자는 연결 완료 시 호출될 CompletionHandler입니다.
client.connect(new InetSocketAddress("host", 8080), null, new CompletionHandler<Void, Void>() {
    @Override
    public void completed(Void result, Void attachment) {
        // 연결이 성공적으로 완료되었을 때 호출됩니다.
        // 서버에 보낼 데이터를 담은 ByteBuffer를 생성합니다.
        ByteBuffer buffer = ByteBuffer.wrap("Hello".getBytes());
        // 데이터를 서버에 비동기적으로 전송합니다.
        // 전송이 완료되면 내부 CompletionHandler가 호출됩니다.
        client.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesWritten, ByteBuffer buffer) {
                // 데이터 전송이 완료되었을 때 호출됩니다.
                // bytesWritten은 실제로 전송된 바이트 수입니다.
            }
            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                // 데이터 전송 중 오류가 발생했을 때 호출됩니다.
            }
        });
    }
    @Override
    public void failed(Throwable exc, Void attachment) {
        // 서버 연결 중 오류가 발생했을 때 호출됩니다.
    }
});
```
- NIO.2의 장점
- 운영체제의 비동기 I/O 기능 활용 (epoll, IOCP 등)
- 스레드 풀 오버헤드 없이 고성능 달성
- 콜백 기반으로 반응형 프로그래밍 지원
- Future 패턴으로 동기 스타일 코드 작성 가능
- 고려사항
- 콜백 체이닝으로 인한 코드 복잡도 증가
- 디버깅과 에러 추적의 어려움
- 러닝 커브가 상대적으로 높음
- 프레임워크 없이 사용 시 보일러플레이트 코드 증가
- 순수 NIO.2 API를 직접 사용하기보다는 Netty, Vert.x 같은 고수준 프레임워크를 사용하는 것이 일반적
### 실전 예제
- 멀티스레드 에코 서버 구현
```java
public class EchoServer {
    // 서버가 바인딩할 포트 번호를 상수로 정의합니다.
    private static final int PORT = 8080;
    // 사용할 스레드 풀의 크기를 정의합니다.
    private static final int POOL_SIZE = 50;
    public static void main(String[] args) {
        // 고정 크기의 스레드 풀을 생성합니다. 최대 50개의 클라이언트를 동시에 처리할 수 있습니다.
        ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
        // try-with-resources를 사용하여 ServerSocket을 자동으로 닫도록 설정합니다.
        try (ServerSocket server = new ServerSocket()) {
            // 서버 소켓이 이전에 사용된 주소를 재사용할 수 있도록 설정합니다.
            server.setReuseAddress(true);
            // 지정된 포트에 서버 소켓을 바인딩합니다.
            server.bind(new InetSocketAddress(PORT));
            // accept() 호출 시 1초 동안 클라이언트 요청이 없으면 SocketTimeoutException을 발생시킵니다.
            server.setSoTimeout(1000);
            // 서버 시작 메시지를 출력합니다.
            System.out.println("Server started on port " + PORT);
            // 메인 루프: 현재 스레드가 인터럽트되지 않은 동안 계속 실행합니다.
            while (!Thread.interrupted()) {
                try {
                    // 클라이언트 연결을 수락합니다. 블로킹 호출이며, 타임아웃이 설정되어 있습니다.
                    Socket client = server.accept();
                    // 클라이언트 처리를 스레드 풀에 제출합니다.
                    executor.submit(() -> handleClient(client));
                } catch (SocketTimeoutException e) {
                    // accept() 타임아웃 발생 시 무시하고 루프를 계속 진행합니다.
                }
            }
        } catch (IOException e) {
            // 서버 소켓 생성 또는 바인딩 중 오류 발생 시 예외를 출력합니다.
            e.printStackTrace();
        } finally {
            // 서버 종료 시 스레드 풀을 정리합니다.
            executor.shutdown();
        }
    }
    // 클라이언트 요청을 처리하는 메서드입니다.
    private static void handleClient(Socket client) {
        try (client; // try-with-resources로 소켓을 자동으로 닫습니다.
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream())); // 클라이언트로부터 입력을 받기 위한 스트림
             PrintWriter out = new PrintWriter(client.getOutputStream(), true) // 클라이언트에게 출력하기 위한 스트림
        ) {
            // 클라이언트 소켓의 읽기 타임아웃을 30초로 설정합니다.
            client.setSoTimeout(30000);
            String line;
            // 클라이언트로부터 한 줄씩 입력을 읽습니다.
            while ((line = in.readLine()) != null) {
                // 받은 메시지를 콘솔에 출력합니다.
                System.out.println("Received: " + line);
                // 클라이언트에게 받은 메시지를 그대로 돌려보냅니다.
                out.println("Echo: " + line);
                // 클라이언트가 "bye"를 입력하면 연결을 종료합니다.
                if ("bye".equalsIgnoreCase(line)) {
                    break;
                }
            }
        } catch (IOException e) {
            // 클라이언트 처리 중 오류 발생 시 메시지를 출력합니다.
            System.err.println("Client error: " + e.getMessage());
        }
    }
}
```
- 에코 클라이언트 구현
```java
public class EchoClient {
    // 서버 주소와 포트 번호를 상수로 정의합니다.
    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    public static void main(String[] args) {
        // try-with-resources를 사용하여 소켓과 스캐너를 자동으로 닫습니다.
        try (Socket socket = new Socket();
             Scanner scanner = new Scanner(System.in)) {
            // 서버에 연결을 시도합니다. 타임아웃은 3초로 설정되어 있습니다.
            socket.connect(new InetSocketAddress(HOST, PORT), 3000);
            // 서버로부터 응답을 기다리는 시간 제한을 10초로 설정합니다.
            socket.setSoTimeout(10000);
            // Nagle 알고리즘을 비활성화하여 데이터를 즉시 전송하도록 설정합니다.
            socket.setTcpNoDelay(true);
            // 서버로부터 데이터를 읽기 위한 입력 스트림을 설정합니다.
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 서버로 데이터를 보내기 위한 출력 스트림을 설정합니다.
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // 연결 성공 메시지를 출력합니다.
            System.out.println("Connected to server");
            // 사용자에게 메시지 입력 안내를 출력합니다.
            System.out.println("Type messages (bye to quit):");
            String userInput;
            // 사용자 입력을 반복적으로 읽습니다.
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                // 입력한 메시지를 서버로 전송합니다.
                out.println(userInput);
                // 서버로부터 응답을 읽습니다.
                String response = in.readLine();
                // 서버의 응답을 출력합니다.
                System.out.println("Server: " + response);
                // 사용자가 "bye"를 입력하면 연결을 종료합니다.
                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (ConnectException e) {
            // 서버에 연결할 수 없을 때의 예외 처리
            System.err.println("Cannot connect to server");
        } catch (SocketTimeoutException e) {
            // 서버 응답 대기 중 타임아웃이 발생했을 때의 예외 처리
            System.err.println("Connection timeout");
        } catch (IOException e) {
            // 기타 입출력 예외 처리
            e.printStackTrace();
        }
    }
}
```
- Try-with-resources로 자동 리소스 관리
- 스레드 풀을 통한 효율적인 멀티스레딩
- BufferedReader/PrintWriter로 버퍼링
- 명시적 타임아웃 설정으로 무한 대기 방지
- TCP_NODELAY로 지연 최소화
- 세분화된 예외 처리
- 테스트 방법: EchoServer를 먼저 실행하여 8080 포트에서 대기, 여러 개의 EchoClient를 실행하여 동시 연결 테스트, bye를 입력하면 클라이언트 연결 종료
- 확장 아이디어: 채팅 서버, 파일 전송 서버, HTTP 서버 등 네트워크 애플리케이션 구현 가능
- 프로토콜 정의, 메시지 직렬화/역직렬화, 상태 관리 등을 추가할 수 있다.