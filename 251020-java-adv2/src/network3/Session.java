package network3;
import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;
    private final int sessionId;

    public Session(Socket socket, int sessionId) {
        this.socket = socket;
        this.sessionId = sessionId;
    }

    @Override
    public void run() {
        System.out.println("[Session-" + sessionId + "] 클라이언트 연결됨");

        // try-with-resources로 자동 자원 관리
        try (ChatConnection connection = new ChatConnection(socket, "Session-" + sessionId)) {

            // 통신 시작
            connection.start();

            // 통신이 끝날 때까지 대기
            connection.waitForCompletion();

        } catch (IOException e) {
            System.err.println("[Session-" + sessionId + "] 오류: " + e.getMessage());
        }

        System.out.println("[Session-" + sessionId + "] 클라이언트 연결 종료");
    }
}
