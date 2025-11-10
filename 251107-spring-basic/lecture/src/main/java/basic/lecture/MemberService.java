package basic.lecture;

public interface MemberService {
    void join(Member member);
    Member findMember(Long id);
}
