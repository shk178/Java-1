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
                Thread.sleep(1000); // 자원 정리 대기
            } catch (Exception e) {
                e.printStackTrace();
                log(e);
            }
        }
    }
}
