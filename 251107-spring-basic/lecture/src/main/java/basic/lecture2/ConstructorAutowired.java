package basic.lecture2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConstructorAutowired {
    private MemberService memberService;
    private MemberRepository memberRepository;
    @Autowired
    private ConstructorAutowired(MemberService memberService, MemberRepository memberRepository) {
        this.memberService = memberService;
    }
}
