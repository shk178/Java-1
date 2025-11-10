package basic.lecture;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BeanFind2 {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
    @Test
    @DisplayName("빈 이름으로 조회 테스트")
    void findBeanByName() {
        MemberService memberService = ac.getBean("memberService", MemberService.class);
        Assertions.assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }
    @Test
    @DisplayName("빈 타입만으로 조회 테스트")
    void findBeanByType() {
        MemberService memberService = ac.getBean(MemberService.class);
        Assertions.assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }
    @Test
    @DisplayName("조회 불가 테스트")
    void noSuchBeanDefined() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> ac.getBean("xxx", MemberService.class));
    }
    @Test
    @DisplayName("타입 조회 중복 오류")
    void noUniqueBeanDefined() {
        assertThrows(NoUniqueBeanDefinitionException.class,
                () -> ac.getBean(DiscountPolicy.class));
    }
    @Test
    @DisplayName("타입으로 여러 빈 조회")
    void findBeansByType() {
        Map<String, DiscountPolicy> discountPolicies = ac.getBeansOfType(DiscountPolicy.class);
        for (Map.Entry<String, DiscountPolicy> entry : discountPolicies.entrySet()) {
            System.out.println(entry);
        }
    }
    @Test
    @DisplayName("상속관계")
    void findBeansByType2() {
        Map<String, Object> objects = ac.getBeansOfType(Object.class);
        for (Map.Entry<String, Object> entry : objects.entrySet()) {
            System.out.println(entry);
        }
    }
}
