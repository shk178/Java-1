package set;

import java.util.Objects;

public class Member {
    private String id;
    Member(String id) {
        this.id = id;
    }
    public String getId() {
        return this.id;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Member member = (Member) obj;
        return Objects.equals(id, member.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
