package msq;
import java.util.*;

public class MemberRepo {
    private Map<String, Member> map = new HashMap<>();
    public void save(Member member) {
        map.put(member.getId(), member);
    }
    public void remove(String id) {
        map.remove(id);
    }
    public Member findById(String id) {
        return map.get(id);
    }
    public Member findByName(String name) {
        Collection<Member> values = map.values();
        for (Member member : values) {
            if (member.getName().equals(name)) {
                return member;
            }
        }
        return null;
    }
}
