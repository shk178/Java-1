package hello.jdbc.transaction2;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RunService2 implements CommandLineRunner {
    private final MemberService2 memberService2;
    @Override
    public void run(String... args) throws Exception {
        memberService2.serviceOne();
    }
}
