package jdbc.except;

public interface MemberRepository {
    Member save(Member member);
    Member findById(String memberId);
    void update(String memberId, int money);
    void delete(String memberId);
}
// 인터페이스에서 throws 선언하면 구현체 제한된다.