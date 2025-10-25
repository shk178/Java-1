package network7;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 세션 관리 클래스
 * 모든 활성화된 세션을 관리하고, 일괄 종료 기능을 제공합니다.
 * CopyOnWriteArrayList를 사용하여 동시성 문제를 해결합니다.
 */
public class SessionManager {
    /**
     * 활성화된 모든 세션을 저장하는 리스트
     * CopyOnWriteArrayList 사용 이유:
     * - 읽기 작업이 쓰기 작업보다 훨씬 많은 경우 효율적
     * - 반복 중 수정이 안전함 (ConcurrentModificationException 발생 안 함)
     * - synchronized 블록 없이도 스레드 안전성 보장
     */
    private final List<Session> sessions = new CopyOnWriteArrayList<>();

    /**
     * 새로운 세션을 리스트에 추가하는 메서드
     * 클라이언트가 연결될 때마다 호출됩니다.
     * @param session 추가할 세션 객체
     */
    public void add(Session session) {
        sessions.add(session);
        Logger.log("세션 추가 - 현재 세션 수: " + sessions.size());
    }

    /**
     * 세션을 리스트에서 제거하는 메서드
     * 클라이언트 연결이 종료될 때 호출됩니다.
     * @param session 제거할 세션 객체
     */
    public void remove(Session session) {
        // remove()는 성공 시 true, 실패 시 false 반환
        if (sessions.remove(session)) {
            Logger.log("세션 제거 - 현재 세션 수: " + sessions.size());
        }
    }

    /**
     * 모든 세션을 종료하는 메서드
     * 서버 종료 시 호출되어 모든 클라이언트 연결을 정리합니다.
     */
    public void closeAll() {
        Logger.log("모든 세션 종료 시작 - 세션 수: " + sessions.size());

        // CopyOnWriteArrayList이므로 반복 중 수정이 안전함
        for (Session session : sessions) {
            try {
                session.close();
            } catch (Exception e) {
                // 한 세션의 종료 실패가 다른 세션 종료를 방해하지 않도록
                Logger.log("세션 종료 중 오류: " + e.getMessage());
            }
        }

        // 모든 세션 제거
        sessions.clear();
        Logger.log("모든 세션 종료 완료");
    }

    /**
     * 현재 활성화된 세션 수를 반환하는 메서드
     * @return 현재 세션 수
     */
    public int getSessionCount() {
        return sessions.size();
    }
}
