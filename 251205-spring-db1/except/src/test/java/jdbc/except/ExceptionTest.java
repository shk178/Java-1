package jdbc.except;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ExceptionTest {
    @Test
    void catchThrow() {
        try {
            throw new Exception();
        } catch (Exception e) {
            System.out.println(e);
        }
        assertThatThrownBy(() -> {
            throw new RuntimeException();
        }).isInstanceOf(RuntimeException.class);
    }
    @Test
    void 복구불가예외() {
        // SQLException: DB 서버에서 발생하면 복구 불가
        // 서비스, 컨트롤러가 해결할 수 없는 것
        // 오류 로그 남긴다.
        // 서블릿 필터, 스프링 인터셉터, ControllerAdvice 사용한다.
        // ConnectException도 복구불가예외, 체크 예외
    }
    @Test
    void 의존관계문제() {
        // 대부분 예외는 복구불가예외
        // 체크 예외이면 복구불가해도 서비스, 컨트롤러에서 throws를 선언한다.
        // 코드가 java.sql.SQLException을 의존하게 된다.
        // JPAException을 의존하도록 바꿔야 한다.
        // Exception을 던졌다가는 다른 예외를 못 잡는다.
    }
    @Test
    void 언체크예외활용() {
        // SQLException -> RuntimeSQLException
        // ConnectException -> RuntimeConnectException
        // 체크 예외를 언체크 예외로 변환한다.
        // 코드가 throws 선언하지 않아도 된다.
        // 체크 예외 때와 마찬가지로 공통 처리한다.
        // 런타임 예외는 놓칠 수 있어서 문서화한다.
        try {
            throw new SQLException();
        } catch (SQLException e) {
            /*
            throw new RuntimeSQLException(e); // e 포함
             */
        }
    }

    private class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException(SQLException e) {
        }
    }
}
