package network7;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * TCP 클라이언트 클래스
 * 서버에 연결하여 메시지를 주고받습니다.
 * 사용자로부터 입력을 받아 서버로 전송하고, 서버의 응답을 출력합니다.
 */
public class Client {
    private static final String HOST = "localhost"; // 연결할 서버 호스트
    private static final int PORT = 12345; // 연결할 서버 포트

    /**
     * 프로그램 진입점
     * @param args 명령행 인자 (사용하지 않음)
     */
    public static void main(String[] args) {
        try {
            new Client().run();
        } catch (IOException e) {
            Logger.log("클라이언트 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 클라이언트 실행 메인 메서드
     * 서버에 연결하고 통신을 시작합니다.
     * @throws IOException 네트워크 오류 발생 시
     */
    public void run() throws IOException {
        Logger.log("서버 연결 중: " + HOST + ":" + PORT);

        // try-with-resources를 사용하여 자동으로 리소스 정리
        try (Socket socket = new Socket(HOST, PORT);
             // BufferedInputStream/OutputStream으로 I/O 성능 향상
             DataInputStream input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             Scanner scanner = new Scanner(System.in)) {

            Logger.log("서버 연결 성공");
            // 서버와의 메시지 송수신 시작
            communicateWithServer(input, output, scanner);

        } catch (IOException e) {
            Logger.log("서버 통신 오류: " + e.getMessage());
            throw e;
        }

        Logger.log("클라이언트 종료");
    }

    /**
     * 서버와 메시지를 주고받는 메인 통신 루프
     * @param input 서버로부터 데이터를 수신하는 스트림
     * @param output 서버로 데이터를 전송하는 스트림
     * @param scanner 사용자 입력을 받는 Scanner
     * @throws IOException 통신 중 오류 발생 시
     */
    private void communicateWithServer(DataInputStream input,
                                       DataOutputStream output,
                                       Scanner scanner) throws IOException {
        while (true) {
            // 1. 사용자로부터 메시지 입력 받기
            String message = readUserInput(scanner);

            // 2. 서버로 메시지 전송
            sendMessage(output, message);

            // 3. "exit" 명령어 입력 시 루프 종료
            if ("exit".equalsIgnoreCase(message)) {
                Logger.log("종료 명령 전송");
                break;
            }

            // 4. 서버로부터 응답 수신
            String response = receiveMessage(input);

            // 5. 응답 출력
            displayResponse(response);
        }
    }

    /**
     * 사용자로부터 입력을 받는 메서드
     * @param scanner 사용자 입력을 받을 Scanner
     * @return 사용자가 입력한 문자열
     */
    private String readUserInput(Scanner scanner) {
        System.out.print("전송 문자: ");
        return scanner.nextLine();
    }

    /**
     * 서버로 메시지를 전송하는 메서드
     * @param output 출력 스트림
     * @param message 전송할 메시지
     * @throws IOException 전송 실패 시
     */
    private void sendMessage(DataOutputStream output, String message) throws IOException {
        output.writeUTF(message); // UTF 형식으로 메시지 전송
        output.flush(); // 버퍼에 있는 데이터를 즉시 전송
    }

    /**
     * 서버로부터 메시지를 수신하는 메서드
     * @param input 입력 스트림
     * @return 수신한 메시지
     * @throws IOException 수신 실패 시
     */
    private String receiveMessage(DataInputStream input) throws IOException {
        return input.readUTF(); // UTF 형식으로 메시지 수신
    }

    /**
     * 서버로부터 받은 응답을 화면에 출력하는 메서드
     * @param response 출력할 응답 메시지
     */
    private void displayResponse(String response) {
        System.out.println("받은 문자: " + response);
    }
}
