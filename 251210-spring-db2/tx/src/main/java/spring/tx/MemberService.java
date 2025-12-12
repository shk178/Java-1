package spring.tx;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    public void join(String username) {
        Member member = new Member(username);
        Log log = new Log("join: " + username);
        memberRepository.save(member);
        try {
            logRepository.save(log);
        } catch (RuntimeException e) {
            System.out.println(e);
        }
        System.out.println(memberRepository.find(username));
        System.out.println(logRepository.find("join: " + username));
    }
}
