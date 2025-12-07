package hello.jdbc.transaction;

import hello.jdbc.Member;
import hello.jdbc.MemberRepository2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository2 memberRepository2;
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository2.findById(fromId);
        Member toMember = memberRepository2.findById(toId);
        memberRepository2.update(fromId, fromMember.getMoney() - money);
        validation(toMember, toId);
        memberRepository2.update(toId, toMember.getMoney() + money);
    }
    private void validation(Member toMember, String toId) {
        if (!toMember.getMemberId().equals(toId)) {
            throw new IllegalStateException(toMember.getMemberId());
        }
    }
}
