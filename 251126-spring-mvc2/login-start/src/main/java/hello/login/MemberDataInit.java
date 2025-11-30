package hello.login;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class MemberDataInit {
    private final MemberRepository memberRepository;
    @PostConstruct
    public void init() {
        Member m1 = new Member();
        m1.setLoginId("m1");
        m1.setLoginPwd("m1");
        memberRepository.save(m1);
        Member m2 = new Member();
        m2.setLoginId("m2");
        m2.setLoginPwd("m2");
        memberRepository.save(m2);
    }
}
