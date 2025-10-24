package network3;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import static network.MyLogger.log;

public class ChatConnection implements AutoCloseable {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final String name; // "Client" 또는 "Server"

    private Thread receiverThread;
    private Thread senderThread;
    private volatile boolean running = true;

    // 생성자: 소켓과 이름을 받아서 스트림 초기화
    public ChatConnection(Socket socket, String name) throws IOException {
        this.socket = socket;
        this.name = name;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    // 통신 시작: receiver와 sender 스레드를 시작
    public void start() {
        startReceiver();
        startSender();
    }

    // 메시지 수신 스레드 시작
    private void startReceiver() {
        receiverThread = new Thread(() -> {
            try {
                while (running && !Thread.currentThread().isInterrupted()) {
                    String msg = input.readUTF();

                    if ("종료".equals(msg)) {
                        log("상대방이 연결을 종료했습니다");
                        stop();
                        break;
                    }

                    log("받음: " + msg);
                }
            } catch (IOException e) {
                if (running) {
                    log("연결 오류: " + e.getMessage());
                }
            }
        }, name + "-Receiver");

        receiverThread.start();
    }

    // 메시지 송신 스레드 시작
    private void startSender() {
        senderThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (running && !Thread.currentThread().isInterrupted()) {
                    log("보낼 메시지: ");
                    String msg = scanner.nextLine();

                    output.writeUTF(msg);

                    if ("종료".equals(msg)) {
                        log("연결을 종료합니다");
                        stop();
                        break;
                    }
                }
            } catch (IOException e) {
                if (running) {
                    log("전송 오류: " + e.getMessage());
                }
            }
        }, name + "-Sender");

        senderThread.start();
    }

    // 연결 종료
    public void stop() {
        running = false;
    }

    // 스레드가 종료될 때까지 대기
    public void waitForCompletion() {
        try {
            if (receiverThread != null) {
                receiverThread.join();
            }
            if (senderThread != null) {
                senderThread.join();
            }
        } catch (InterruptedException e) {
            log("대기 중 인터럽트: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    // AutoCloseable 구현: try-with-resources 사용 가능
    @Override
    public void close() {
        stop();
        closeQuietly(output);
        closeQuietly(input);
        closeQuietly(socket);
        log("자원 정리 완료");
    }

    // 예외를 무시하고 조용히 닫기
    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // 무시
            }
        }
    }
}
