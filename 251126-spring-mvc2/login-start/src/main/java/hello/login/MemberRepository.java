package hello.login;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MemberRepository {
    private static Map<Long, Member> memberMap = new HashMap<>();
    private static long sequence = 0L;
    public Member save(Member member) {
        member.setSequenceId(++sequence);
        memberMap.put(member.getSequenceId(), member);
        return member;
    }
    public Member findBySequenceId(Long sequenceId) {
        return memberMap.get(sequenceId);
    }
    public List<Member> findAll() {
        return new ArrayList<>(memberMap.values());
    }
    public Optional<Member> findByLoginId(String loginId) {
        return findAll().stream()
                .filter(m -> m.getLoginId().equals(loginId))
                .findFirst();
    }
}
