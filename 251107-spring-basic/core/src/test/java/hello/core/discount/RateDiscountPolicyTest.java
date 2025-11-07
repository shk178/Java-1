package hello.core.discount;

import hello.core.member.Grade;
import hello.core.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateDiscountPolicyTest {
    RateDiscountPolicy discountPolicy = new Runnable();
    @Test
    @DisplayName("VIP는 10% 할인 적용")
    void vip_o() {
        //given
        Member member = new Member(1L, "memberVIP", Grade.VIP);
        //when
        int discount = discountPolicy.discount(member, 10_000);
        //then
        assertThat(discount).isEqualTo(1_000);
    }
    @Test
    @DisplayName("VIP 아니면 할인 x")
    void vip_x() {
        //given
        Member member = new Member(2L, "memberBASIC", Grade.BASIC);
        //when
        int discount = discountPolicy.discount(member, 10_000);
        //then
        assertThat(discount).isEqualTo(0);
    }
}