package network7;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 클라이언트와의 개별 세션을 관리하는 클래스
 * 각 클라이언트 연결마다 하나의 Session 인스턴스가 생성되며,
 * 별도의 스레드에서 실행됩니다.
 */
public class Session implements Runnable, AutoCloseable {
    private final Socket socket; // 클라이언트와 연결된 소켓
    private final DataInputStream input; // 데이터 수신용 스트림
    private final DataOutputStream output; // 데이터 송신용 스트림
    private final SessionManager sessionManager; // 세션 관리자 참조
    private final AtomicBoolean closed = new AtomicBoolean(false); // 스레드 안전한 종료 상태 플래그

    /**
     * Session 생성자
     * @param socket 클라이언트와 연결된 소켓
     * @param sessionManager 이 세션을 관리할 SessionManager
     * @throws IOException 스트림 생성 실패 시
     */
    public Session(Socket socket, SessionManager sessionManager) throws IOException {
        this.socket = socket;
        // BufferedInputStream/OutputStream을 사용하여 I/O 성능 향상
        this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.sessionManager = sessionManager;
        // 세션 매니저에 현재 세션 등록
        this.sessionManager.add(this);
    }

    /**
     * 스레드 실행 메서드
     * 클라이언트로부터 메시지를 받아 처리하는 메인 루프
     */
    @Override
    public void run() {
        Logger.log("세션 시작: " + socket.getRemoteSocketAddress());

        try {
            // 메시지 처리 루프 실행
            processMessages();
        } catch (IOException e) {
            // 정상 종료가 아닌 경우에만 에러 로그 출력
            if (!closed.get()) {
                Logger.log("세션 에러: " + e.getMessage());
            }
        } finally {
            // 모든 경우에 세션 종료 처리
            close();
        }
    }

    /**
     * 클라이언트로부터 메시지를 수신하고 응답하는 메서드
     * @throws IOException 통신 중 오류 발생 시
     */
    private void processMessages() throws IOException {
        // 세션이 종료되지 않은 동안 메시지 처리
        while (!closed.get()) {
            // UTF 형식으로 메시지 수신
            String received = input.readUTF();
            Logger.log("수신: " + received + " (from " + socket.getRemoteSocketAddress() + ")");

            // "exit" 명령어 수신 시 루프 종료
            if ("exit".equalsIgnoreCase(received)) {
                Logger.log("클라이언트 종료 요청");
                break;
            }

            // 수신한 메시지에 대한 응답 전송
            sendResponse(received);
        }
    }

    /**
     * 클라이언트에게 응답 메시지를 전송하는 메서드
     * @param message 수신한 원본 메시지
     * @throws IOException 전송 실패 시
     */
    private void sendResponse(String message) throws IOException {
        // 수신한 메시지에 " World"를 붙여서 응답 생성
        String response = message + " World";
        output.writeUTF(response);
        output.flush(); // 버퍼에 있는 데이터를 즉시 전송
        Logger.log("송신: " + response);
    }

    /**
     * 세션을 종료하는 메서드
     * AutoCloseable 인터페이스 구현 및 명시적 종료에 사용
     * 중복 호출 방지를 위해 AtomicBoolean 사용
     */
    @Override
    public void close() {
        // compareAndSet으로 원자적으로 상태 변경 및 중복 실행 방지
        if (!closed.compareAndSet(false, true)) {
            return; // 이미 종료된 경우 즉시 반환
        }

        // 세션 매니저에서 현재 세션 제거
        sessionManager.remove(this);
        // 모든 리소스 정리
        closeResources();
        Logger.log("세션 종료: " + socket.getRemoteSocketAddress());
    }

    /**
     * 모든 I/O 리소스를 정리하는 메서드
     * 출력 → 입력 → 소켓 순서로 정리 (데이터 손실 방지)
     */
    private void closeResources() {
        closeQuietly(output); // 출력 스트림 먼저 종료
        closeQuietly(input); // 입력 스트림 종료
        closeQuietly(socket); // 마지막으로 소켓 종료
    }

    /**
     * 리소스를 안전하게 종료하는 유틸리티 메서드
     * 예외가 발생해도 무시하여 다른 리소스 정리를 방해하지 않음
     * @param resource 종료할 리소스
     */
    private void closeQuietly(Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (IOException e) {
                // 종료 시 예외는 무시 (이미 종료 중이므로)
            }
        }
    }
}
