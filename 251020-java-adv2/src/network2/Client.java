package network2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import static network.MyLogger.log;

public class Client {
    // 서버 연결에 사용할 포트 번호를 상수로 정의
    private static final int PORT = 12345;

    public static void main(String[] args) {
        // Socket 객체를 null로 초기화 (finally에서 닫기 위해)
        Socket socket = null;
        // 서버로부터 데이터를 읽기 위한 입력 스트림
        DataInputStream input = null;
        // 서버로 데이터를 보내기 위한 출력 스트림
        DataOutputStream output = null;
        // 사용자 입력을 받기 위한 Scanner 객체
        Scanner sc = null;

        try {
            // localhost의 지정된 포트로 서버에 연결
            socket = new Socket("localhost", PORT);
            // 소켓의 입력 스트림을 DataInputStream으로 래핑
            input = new DataInputStream(socket.getInputStream());
            // 소켓의 출력 스트림을 DataOutputStream으로 래핑
            output = new DataOutputStream(socket.getOutputStream());

            // 람다 표현식에서 사용하기 위해 final 변수로 복사
            final DataInputStream finalInput = input;
            // 람다 표현식에서 사용하기 위해 final 변수로 복사
            final DataOutputStream finalOutput = output;
            // 람다 표현식에서 사용하기 위해 final 변수로 복사
            final Socket finalSocket = socket;

            // 서버로부터 메시지를 받는 별도의 스레드 생성
            Thread receiver = new Thread(() -> {
                try {
                    // 스레드가 중단되지 않는 동안 계속 실행
                    while (!Thread.currentThread().isInterrupted()) {
                        // 서버로부터 UTF 형식의 문자열 읽기
                        String msg = finalInput.readUTF();
                        // 받은 메시지가 "종료"인지 확인
                        if (msg.equals("종료")) {
                            // 서버가 연결을 종료했음을 로그에 출력
                            log("서버가 연결 종료함");
                            // 반복문 탈출
                            break;
                        }
                        // 받은 메시지를 로그에 출력
                        log("받은 내용: " + msg);
                    }
                } catch (IOException e) {
                    // 소켓이 닫히지 않은 상태에서 예외가 발생한 경우에만 로그 출력
                    if (!finalSocket.isClosed()) {
                        log(e);
                    }
                }
            });
            // receiver 스레드 시작
            receiver.start();

            // 사용자 입력을 받기 위한 Scanner 객체 생성
            sc = new Scanner(System.in);
            // 무한 루프: 사용자가 "종료"를 입력할 때까지 계속
            while (true) {
                // 사용자에게 입력 안내 메시지 출력
                log("보낼 내용:");
                // 사용자로부터 한 줄 입력 받기
                String msg = sc.nextLine();
                // 입력받은 메시지를 서버로 전송
                output.writeUTF(msg);
                // 입력받은 메시지가 "종료"인지 확인
                if (msg.equals("종료")) {
                    // 연결 종료 시도 로그 출력
                    log("연결 종료 시도");
                    // 반복문 탈출
                    break;
                }
            }

            // receiver 스레드가 종료될 때까지 최대 3초 대기
            receiver.join(3000);

        } catch (IOException e) {
            // 입출력 예외 발생 시 로그 출력
            log(e);
        } catch (InterruptedException e) {
            // 스레드 인터럽트 예외 발생 시 로그 출력
            log(e);
        } finally {
            // Scanner 객체가 null이 아니면 닫기
            if (sc != null) {
                sc.close();
            }
            // 출력 스트림이 null이 아니면 닫기
            if (output != null) {
                try {
                    // 출력 스트림 닫기
                    output.close();
                } catch (IOException e) {
                    // 닫기 중 예외 발생 시 로그 출력
                    log(e);
                }
            }
            // 입력 스트림이 null이 아니면 닫기
            if (input != null) {
                try {
                    // 입력 스트림 닫기
                    input.close();
                } catch (IOException e) {
                    // 닫기 중 예외 발생 시 로그 출력
                    log(e);
                }
            }
            // 소켓이 null이 아니면 닫기
            if (socket != null) {
                try {
                    // 소켓 닫기
                    socket.close();
                } catch (IOException e) {
                    // 닫기 중 예외 발생 시 로그 출력
                    log(e);
                }
            }
        }
    }
}
