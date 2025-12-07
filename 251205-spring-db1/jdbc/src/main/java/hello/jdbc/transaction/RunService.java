package hello.jdbc.transaction;

import hello.jdbc.Member;
import hello.jdbc.MemberRepository2;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.ConnectionConst.*;

@Component
@RequiredArgsConstructor
public class RunService implements CommandLineRunner {
    private final MemberRepository2 memberRepository2;
    private final MemberService memberService;
    private void one() throws SQLException {
        Connection con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Member m1 = new Member("m1", 10_000);
        Member m2 = new Member("m2", 10_000);
        memberRepository2.delete(con, m1.getMemberId());
        memberRepository2.delete(con, m2.getMemberId());
        memberRepository2.save(con, m1);
        memberRepository2.save(con, m2);
        memberService.accountTransfer(m1.getMemberId(), m2.getMemberId(), 2_000);
        Member afterM1 = memberRepository2.findById(con, m1.getMemberId());
        Member afterM2 = memberRepository2.findById(con, m2.getMemberId());
        System.out.println("afterM1.getMoney() = " + afterM1.getMoney());
        System.out.println("afterM2.getMoney() = " + afterM2.getMoney());
        con.close();
    }
    @Override
    public void run(String... args) throws Exception {
        one();
    }
}
