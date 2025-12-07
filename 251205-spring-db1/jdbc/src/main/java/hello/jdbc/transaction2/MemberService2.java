package hello.jdbc.transaction2;

import hello.jdbc.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class MemberService2 {
    private final PlatformTransactionManager transactionManager;
    private final MemberRepository3 memberRepository3;
    public void serviceOne() throws SQLException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Member member = new Member("memberA", 100);
            memberRepository3.saveFind(member);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            System.out.println(e);
        }
    }
}
