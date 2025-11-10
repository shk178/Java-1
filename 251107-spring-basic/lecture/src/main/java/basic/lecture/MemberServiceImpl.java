package basic.lecture;

public class MemberServiceImpl implements MemberService {
    public final MemberRepository memberRepository;
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    public void join(Member member) {
        memberRepository.save(member);
    }
    public Member findMember(Long id) {
        return memberRepository.findById(id);
    }
}
