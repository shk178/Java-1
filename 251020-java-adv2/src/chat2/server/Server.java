package chat2.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static network.MyLogger.log;

public class Server {
    private final int port;
    private final CommandManager commandManager;
    private final SessionManager sessionManager;
    private ServerSocket serverSocket;
    public Server(int port, CommandManager commandManager, SessionManager sessionManager) {
        this.port = port;
        this.commandManager = commandManager;
        this.sessionManager = sessionManager;
    }
    public void start() throws IOException {
        log("서버 시작");
        serverSocket = new ServerSocket(port);
        addShutdownHook();
        running();
    }
    private void running() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                Session session = new Session(socket, commandManager, sessionManager);
                Thread st = new Thread(session);
                st.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log("서버 종료");
    }
    private void addShutdownHook() {
        ShutdownHook shutdownHook = new ShutdownHook(serverSocket, sessionManager);
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook));
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
            log("shutdownHook 실행");
            try {
                sessionManager.closeAll();
                serverSocket.close();
                Thread.sleep(1000); // 자원 정리 대기
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
