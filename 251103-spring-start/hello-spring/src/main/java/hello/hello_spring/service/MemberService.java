package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import java.util.List;
import java.util.Optional;

public class MemberService {
    private final MemberRepository memberRepository;
    
    //회원 서비스 코드를 DI 가능하도록
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    //회원 가입
    public Long join(Member member) {
        validateDuplicateMember(member); //중복 검증
        memberRepository.save(member);
        return member.getId();
    }
    
    private void validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원");
                });
    }
    
    //전체 회원 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
    
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}



