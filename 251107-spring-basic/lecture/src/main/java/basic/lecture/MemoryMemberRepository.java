package basic.lecture;

import java.util.HashMap;
import java.util.Map;

public class MemoryMemberRepository implements MemberRepository {
    public static Map<Long, Member> store = new HashMap<>();
    public void save(Member member) {
        store.put(member.id, member);
    }
    public Member findById(Long id) {
        return store.get(id);
    }
}
