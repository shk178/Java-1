package network3;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        System.out.println("=== 클라이언트 시작 ===");

        // try-with-resources로 자동 자원 관리
        try (Socket socket = new Socket(HOST, PORT);
             ChatConnection connection = new ChatConnection(socket, "Client")) {

            System.out.println("서버에 연결되었습니다");

            // 통신 시작
            connection.start();

            // 통신이 끝날 때까지 대기
            connection.waitForCompletion();

        } catch (IOException e) {
            System.err.println("클라이언트 오류: " + e.getMessage());
        }

        System.out.println("=== 클라이언트 종료 ===");
    }
}
