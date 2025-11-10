package basic.lecture;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CheckSingleton {
    @Test
    @DisplayName("스프링 없는 순수 DI 컨테이너")
    void pureDIContainer() {
        AppConfig appConfig = new AppConfig();
        //조회: 호출할 때마다 객체 생성
        MemberService memberService1 = appConfig.memberService();
        MemberService memberService2 = appConfig.memberService();
        //확인: 생성한 두 객체의 참조값이 다름
        System.out.println(memberService1);
        System.out.println(memberService2);
        Assertions.assertThat(memberService1).isNotSameAs(memberService2);
    }
    @Test
    @DisplayName("싱글톤 패턴 적용한 객체")
    void singletonServiceTest() {
        SingletonService singletonService1 = SingletonService.getInstance();
        SingletonService singletonService2 = SingletonService.getInstance();
        System.out.println(singletonService1);
        System.out.println(singletonService2);
        Assertions.assertThat(singletonService1).isEqualTo(singletonService2);
        singletonService1.logic();
    }
}
