package hello.jdbc;

import hello.jdbc.transaction2.MemberService2;
import hello.jdbc.transaction3.MemberService3;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RunService implements CommandLineRunner {
    private final MemberService2 memberService2;
    private final MemberService3 memberService3;
    @Override
    public void run(String... args) throws Exception {
        System.out.println();
        memberService2.bizLogic();
        System.out.println();
        memberService3.bizLogic();
        System.out.println();
        memberService3.bizLogic2();
        System.out.println();
    }
}
/*

2025-12-08T21:19:30.395+09:00  INFO 1776 --- [jdbc] [           main] com.zaxxer.hikari.HikariDataSource       : two - Starting...
2025-12-08T21:19:30.496+09:00  INFO 1776 --- [jdbc] [           main] com.zaxxer.hikari.pool.HikariPool        : two - Added connection conn0: url=jdbc:h2:tcp://localhost/~/test user=SA
2025-12-08T21:19:30.498+09:00  INFO 1776 --- [jdbc] [           main] com.zaxxer.hikari.HikariDataSource       : two - Start completed.
member.equals(resultMember) = true

member2.equals(resultMember2) = true

member3.equals(resultMember3) = true

2025-12-08T21:19:30.542+09:00  INFO 1776 --- [jdbc] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : two - Shutdown initiated...
2025-12-08T21:19:30.544+09:00  INFO 1776 --- [jdbc] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : two - Shutdown completed.

 */