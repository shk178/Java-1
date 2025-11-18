package lecture.demo.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class MemberRepositoryTest {
    MemberRepository memberRepository = MemberRepository.getInstance();
    @AfterEach
    void afterEach() {
        memberRepository.clearStore();
    }
    @Test
    void save() {
        // given
        Member member = new Member("hello", 20);
        // when
        Member savedMember = memberRepository.save(member);
        // then
        Member findMember = memberRepository.findById(savedMember.getId());
        assertThat(findMember).isEqualTo(savedMember);
    }
    @Test
    void findAll() {
        // given
        Member one = new Member("member1", 10);
        Member two = new Member("member2", 20);
        memberRepository.save(one);
        memberRepository.save(two);
        // when
        List<Member> result = memberRepository.findAll();
        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(one, two);
    }
}
