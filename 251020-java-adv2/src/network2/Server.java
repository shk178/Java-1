package network2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static network.MyLogger.log;

public class Server {
    // 서버가 사용할 포트 번호를 상수로 정의
    private static final int PORT = 12345;

    public static void main(String[] args) {
        // 클라이언트 연결을 받아들일 ServerSocket 객체
        ServerSocket serverSocket = null;

        try {
            // 지정된 포트로 ServerSocket 생성 (서버 시작)
            serverSocket = new ServerSocket(PORT);
            // 서버 시작 로그 출력
            log("서버 시작, 포트: " + PORT);

            // 무한 루프: 계속해서 클라이언트 연결을 받아들임
            while (true) {
                // 클라이언트 연결을 기다리고, 연결되면 Socket 객체 반환
                Socket socket = serverSocket.accept();
                // 클라이언트 연결 성공 로그 출력
                log("클라이언트 연결됨");
                // 연결된 클라이언트를 처리할 Session 객체 생성
                Session session = new Session(socket);
                // Session을 실행할 새로운 Thread 객체 생성
                Thread thread = new Thread(session);
                // 스레드 시작 (Session의 run 메서드 실행)
                thread.start();
            }
        } catch (IOException e) {
            // 입출력 예외 발생 시 로그 출력
            log(e);
        } finally {
            // ServerSocket이 null이 아니면 닫기
            if (serverSocket != null) {
                try {
                    // ServerSocket 닫기
                    serverSocket.close();
                } catch (IOException e) {
                    // 닫기 중 예외 발생 시 로그 출력
                    log(e);
                }
            }
        }
    }
}
