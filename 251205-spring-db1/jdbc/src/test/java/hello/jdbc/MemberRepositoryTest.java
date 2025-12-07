package hello.jdbc;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MemberRepositoryTest {
    MemberRepository repository = new MemberRepository();
    @Test
    void crud() throws SQLException {
        Member member = new Member("name1", 10_000);
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> repository.findById(member.getMemberId())).isInstanceOf(NoSuchElementException.class);
        repository.save(member);
        Member findMember = repository.findById(member.getMemberId());
        repository.update(member.getMemberId(), 20_000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember.getMoney()).isEqualTo(20_000);
    }
}
