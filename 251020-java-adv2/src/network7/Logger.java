package network7;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 로깅 유틸리티 클래스
 * 일관된 형식으로 로그 메시지를 출력합니다.
 * 타임스탬프와 스레드 이름을 포함하여 디버깅과 추적을 용이하게 합니다.
 */
public class Logger {
    /**
     * 날짜와 시간 포맷터
     * "년-월-일 시:분:초" 형식으로 시간을 표시합니다.
     */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 메시지를 로그로 출력하는 메서드
     * 형식: [타임스탬프] [스레드이름] 메시지
     * 예시: [2025-10-25 14:30:15] [main] 서버 시작 - 포트: 12345
     *
     * @param message 출력할 로그 메시지
     */
    public static void log(String message) {
        // 현재 시간을 지정된 형식으로 포맷
        String timestamp = LocalDateTime.now().format(FORMATTER);
        // 현재 실행 중인 스레드의 이름 가져오기
        String threadName = Thread.currentThread().getName();
        // [타임스탬프] [스레드이름] 메시지 형식으로 출력
        System.out.printf("[%s] [%s] %s%n", timestamp, threadName, message);
    }

    /**
     * 예외 객체를 로그로 출력하는 메서드
     * 예외의 클래스 이름과 메시지를 간단하게 출력합니다.
     *
     * @param e 출력할 예외 객체
     */
    public static void log(Exception e) {
        // 예외 클래스의 단순 이름 (패키지 경로 제외)과 메시지 출력
        log("예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
    }
}
