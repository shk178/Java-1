package basic.lecture;

public interface MemberRepository {
    void save(Member member);
    Member findById(Long id);
}
