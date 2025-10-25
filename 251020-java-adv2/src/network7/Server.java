package network7;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * TCP 서버 클래스
 * 클라이언트 연결을 수락하고 각 연결을 별도의 세션으로 처리합니다.
 * 스레드 풀을 사용하여 효율적으로 다중 클라이언트를 처리합니다.
 */
public class Server {
    private static final int PORT = 12345; // 서버 포트 번호
    private static final int THREAD_POOL_SIZE = 10; // 스레드 풀 크기
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5; // 종료 대기 시간 (초)

    private final ServerSocket serverSocket; // 클라이언트 연결을 수락하는 서버 소켓
    private final SessionManager sessionManager; // 모든 세션을 관리하는 매니저
    private final ExecutorService executorService; // 세션을 실행할 스레드 풀
    private volatile boolean running = true; // 서버 실행 상태 (volatile로 스레드 간 가시성 보장)

    /**
     * Server 생성자
     * @param port 서버가 리스닝할 포트 번호
     * @throws IOException 서버 소켓 생성 실패 시
     */
    public Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.sessionManager = new SessionManager();
        // 고정 크기 스레드 풀 생성 (동시에 최대 THREAD_POOL_SIZE개의 세션 처리)
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // JVM 종료 시 실행될 셧다운 훅 등록
        registerShutdownHook();
        Logger.log("서버 시작 - 포트: " + port);
    }

    /**
     * 서버 시작 메서드
     * 클라이언트 연결을 계속 수락하고 각 연결을 새로운 세션으로 처리합니다.
     */
    public void start() {
        while (running) {
            try {
                // 클라이언트 연결 대기 (blocking)
                Socket socket = serverSocket.accept();
                Logger.log("클라이언트 연결: " + socket.getRemoteSocketAddress());

                // 새로운 세션 생성 및 스레드 풀에 제출
                Session session = new Session(socket, sessionManager);
                executorService.submit(session);

            } catch (IOException e) {
                // 서버가 실행 중일 때만 에러 로그 출력
                // (종료 중이면 정상적인 예외이므로 무시)
                if (running) {
                    Logger.log("연결 수락 실패: " + e.getMessage());
                }
            }
        }
    }

    /**
     * JVM 종료 시 실행될 셧다운 훅을 등록하는 메서드
     * Ctrl+C 등으로 프로그램이 종료될 때 리소스를 정리합니다.
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.log("서버 종료 시작...");
            shutdown();
            Logger.log("서버 종료 완료");
        }, "shutdown-hook"));
    }

    /**
     * 서버를 안전하게 종료하는 메서드
     * 단계별로 리소스를 정리하여 데이터 손실을 방지합니다.
     */
    private void shutdown() {
        running = false; // 서버 실행 상태를 false로 변경

        // 1단계: 새로운 클라이언트 연결 차단
        closeServerSocket();

        // 2단계: 현재 활성화된 모든 세션 종료
        sessionManager.closeAll();

        // 3단계: 스레드 풀 종료 및 대기
        shutdownExecutor();
    }

    /**
     * 서버 소켓을 종료하는 메서드
     * 새로운 클라이언트 연결을 더 이상 받지 않습니다.
     */
    private void closeServerSocket() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                Logger.log("ServerSocket 종료");
            }
        } catch (IOException e) {
            Logger.log("ServerSocket 종료 실패: " + e.getMessage());
        }
    }

    /**
     * ExecutorService를 안전하게 종료하는 메서드
     * 실행 중인 작업이 완료될 때까지 대기하고, 타임아웃 시 강제 종료합니다.
     */
    private void shutdownExecutor() {
        // 새로운 작업 제출 차단
        executorService.shutdown();

        try {
            // 지정된 시간 동안 작업 완료 대기
            if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                // 타임아웃 시 실행 중인 작업 강제 종료
                executorService.shutdownNow();
                Logger.log("일부 작업 강제 종료");
            }
        } catch (InterruptedException e) {
            // 대기 중 인터럽트 발생 시 즉시 강제 종료
            executorService.shutdownNow();
            // 인터럽트 상태 복원
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 프로그램 진입점
     * @param args 명령행 인자 (사용하지 않음)
     */
    public static void main(String[] args) {
        try {
            Server server = new Server(PORT);
            server.start();
        } catch (IOException e) {
            Logger.log("서버 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
