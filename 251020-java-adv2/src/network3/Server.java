package network3;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {
    private static final int PORT = 12345;
    private static volatile boolean running = true;
    private static int sessionCounter = 0;

    public static void main(String[] args) {
        System.out.println("=== 서버 시작 ===");

        // Ctrl+C로 우아하게 종료
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n서버 종료 신호 받음");
            running = false;
        }));

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
