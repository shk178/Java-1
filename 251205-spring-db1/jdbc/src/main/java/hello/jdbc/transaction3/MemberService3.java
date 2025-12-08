package hello.jdbc.transaction3;

import hello.jdbc.Member;
import hello.jdbc.transaction2.MemberRepository3;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class MemberService3 {
    private final TransactionTemplate transactionTemplate;
    private final MemberRepository3 memberRepository3;
    public void bizLogic() throws SQLException {
        transactionTemplate.executeWithoutResult((status) -> {
            try {
                Member member2 = new Member("id-1", 1_000);
                Member resultMember2 = memberRepository3.dataLogic(member2);
                System.out.println("member2.equals(resultMember2) = " + member2.equals(resultMember2));
            } catch (SQLException e) {
                System.out.println(e);
            }
        });
    }
    @Transactional
    public void bizLogic2() throws SQLException {
        Member member3 = new Member("id-1", 1_000);
        Member resultMember3 = memberRepository3.dataLogic(member3);
        System.out.println("member3.equals(resultMember3) = " + member3.equals(resultMember3));
    }
}
