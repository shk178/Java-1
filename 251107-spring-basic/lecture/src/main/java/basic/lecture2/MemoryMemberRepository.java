package basic.lecture2;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MemoryMemberRepository implements MemberRepository {
    public static Map<Long, Member> store = new HashMap<>();
    public void save(Member member) {
        store.put(member.id, member);
    }
    public Member findById(Long id) {
        return store.get(id);
    }
}
