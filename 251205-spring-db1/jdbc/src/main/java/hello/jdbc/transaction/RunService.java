package hello.jdbc.transaction;

import hello.jdbc.Member;
import hello.jdbc.MemberRepository2;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class RunService implements CommandLineRunner {
    private final MemberRepository2 memberRepository2;
    private final MemberService memberService;
    private void one() throws SQLException {
        Member m1 = new Member("m1", 10_000);
        Member m2 = new Member("m2", 10_000);
        memberRepository2.delete(m1.getMemberId());
        memberRepository2.delete(m2.getMemberId());
        memberRepository2.save(m1);
        memberRepository2.save(m2);
        memberService.accountTransfer(m1.getMemberId(), m2.getMemberId(), 1_000);
        Member afterM1 = memberRepository2.findById(m1.getMemberId());
        Member afterM2 = memberRepository2.findById(m2.getMemberId());
        System.out.println("afterM1.getMoney() = " + afterM1.getMoney());
        System.out.println("afterM2.getMoney() = " + afterM2.getMoney());
    }
    @Override
    public void run(String... args) throws Exception {
        one();
    }
}
