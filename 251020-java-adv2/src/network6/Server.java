package network6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static network.MyLogger.log;

public class Server {
    private static final int PORT = 12345;
    public static void main(String[] args) throws IOException {
        SessionManager sessionManager = new SessionManager();
        ServerSocket serverSocket = new ServerSocket(PORT);
        ShutdownHook shutdownHook = new ShutdownHook(serverSocket, sessionManager);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook, "셧다운스레드"));
        while (true) {
            Socket socket = serverSocket.accept();
            log("소켓 연결: " + socket);
            Session session = new Session(socket, sessionManager);
            Thread thread = new Thread(session);
            thread.start();
        }
    }
    static class ShutdownHook implements Runnable {
        private final ServerSocket serverSocket;
        private final SessionManager sessionManager;
        public ShutdownHook(ServerSocket serverSocket, SessionManager sessionManager) {
            this.serverSocket = serverSocket;
            this.sessionManager = sessionManager;
        }
        @Override
        public void run() {
            log("셧다운 훅 실행");
            sessionManager.closeAll();
            try {
                serverSocket.close();
                // 정상 종료 시 셧다운 훅은 항상 실행된다.
                // 다른 스레드가 자원 정리, 로그 남길 수 있도록
                // Thread.sleep으로 잠시 대기한다.
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
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