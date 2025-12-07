package hello.jdbc.transaction;

import hello.jdbc.Member;
import hello.jdbc.MemberRepository2;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final DataSource dataSource;
    private final MemberRepository2 memberRepository2;
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try {
            //트랜잭션 시작
            con.setAutoCommit(false);
            //비즈니스 로직
            Member fromMember = memberRepository2.findById(con, fromId);
            Member toMember = memberRepository2.findById(con, toId);
            memberRepository2.update(con, fromId, fromMember.getMoney() - money);
            //validation(toMember, toId);
            memberRepository2.update(con, toId, toMember.getMoney() + money);
            //트랜잭션 커밋
            con.commit();
        } catch (Exception e) {
            con.rollback();
            System.out.println(e);
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
    private void validation(Member toMember, String toId) {
        if (!toMember.getMemberId().equals(toId)) {
            throw new IllegalStateException(toMember.getMemberId());
        }
    }
}
