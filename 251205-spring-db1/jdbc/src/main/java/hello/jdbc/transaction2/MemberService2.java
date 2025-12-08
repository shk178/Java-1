package hello.jdbc.transaction2;

import hello.jdbc.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

@Service
public class MemberService2 {
    private final PlatformTransactionManager transactionManager;
    private final MemberRepository3 memberRepository3;
    public MemberService2(PlatformTransactionManager transactionManager, MemberRepository3 memberRepository3) {
        this.transactionManager = transactionManager;
        this.memberRepository3 = memberRepository3;
    }
    public void bizLogic() throws SQLException {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Member member = new Member("id-1", 1_000);
            Member resultMember = memberRepository3.dataLogic(member);
            System.out.println("member.equals(resultMember) = " + member.equals(resultMember));
            transactionManager.commit(status);
        } catch (SQLException e) {
            System.out.println(e);
            transactionManager.rollback(status);
        }
    }
}
