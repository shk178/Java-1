package hello;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class MemberRepoTest {
    @Autowired
    MemberRepository memberRepository;
    @Transactional
    @Test
    void test() {
        Member member = new Member("id1", "name1");
        memberRepository.initTable();
        memberRepository.save(member);
        Member findMember = memberRepository.find(member.getMemberId());
        System.out.println(findMember.getMemberId());
        System.out.println(findMember.getName());
    }
}
