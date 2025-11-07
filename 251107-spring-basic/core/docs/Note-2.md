## RateDiscountPolicy 추가
### 코드 추가
```java
package hello.core.discount;

public class RateDiscountPolicy implements DiscountPolicy {
    private int discountPercent = 10;
    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return price * (discountPercent / 100);
        } else {
            return 0;
        }
    }
}
```
### 테스트 작성
```java
package hello.core.discount;

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
```
## 현재 코드 문제점
- Service = new ServiceImpl()로 DIP 위반 -> OCP 위반으로 이어짐
- DIP 위반을 추상(인터페이스)에만 의존하도록 변경해서 해결해야 한다.
- 구현 객체를 생성하고 연결하는 책임을 가지는 별도의 설정 클래스(AppConfig)를 만든다.
### AppConfig
```java
package hello.core;

public class AppConfig {
    public MemberService memberService() {
        return new MemberServiceImpl(new MemoryMemberRepository());
    }
    public OrderService orderService() {
        return new OrderServiceImpl(
                new MemoryMemberRepository(),
                new FixDiscountPolicy()
        );
    }
}
```
- AppConfig는 애플리케이션 동작에 필요한 구현 객체를 생성
- AppConfig는 생성한 객체 인스턴스의 참조를 생성자를 통해서 주입해준다.
### MemberServiceImpl - 생성자 주입
```java
package hello.core.member;

public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void join(Member member) {
        memberRepository.save(member);
    }

    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
```
### OrderServiceImpl - 생성자 주입
```java
package hello.core.order;

import hello.core.discount.DiscountPolicy;
import hello.core.member.Member;
import hello.core.member.MemberRepository;

public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;
    
    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
    
    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);
        return new Order(memberId, itemName, itemPrice, discountPrice);
    }
}
```
## 회원 클래스 다이어그램
* AppConfig (생성)--> MemberServiceImpl
* AppConfig (생성)--> MemoryMemberRepository
* 객체 생성하고 연결하는 역할 / 실행하는 역할이 분리됨
## 회원 인스턴스 다이어그램
* appConfig (2.생성+주입)--> memberServiceImpl
* appConfig (1.생성)--> memberServiceImpl
* 2. DI=의존관계 주입=의존성 주입
## AppConfig 실행
### MemberApp
```java
package hello.core;

public class MemberApp {
    public static void main(String[] args) {
        AppConfig appConfig = new AppConfig();
        MemberService memberService = appConfig.memberService();
        Member member = new Member(1L, "memberA", Grade.VIP);
        memberService.join(member);
        Member findMember = memberService.findMember(1L);
        System.out.println(member.getName());
        System.out.println(findMember.getName());
    }
}
```
### OrderApp
```java
package hello.core;

public class OrderApp {
    public static void main(String[] args) {
        AppConfig appConfig = new AppConfig();
        MemberService memberService = appConfig.memberService();
        OrderService orderService = appConfig.orderService();
        long memberId = 1L;
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.join(member);
        Order order = orderService.createOrder(memberId, "itemA", 10_000);
        System.out.println(order);
    }
}
```
## 테스트 코드 변경
```java
package hello.core.member;

import hello.core.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class MemberServiceTest {
    MemberService memberService;
    @BeforeEach
    public void beforeEach() {
        AppConfig appConfig = new AppConfig();
        memberService = appConfig.memberService();
    }
    @Test
    void join() {
        //given
        Member member = new Member(1L, "memberA", Grade.VIP);
        //when
        memberService.join(member);
        Member findMember = memberService.findMember(1L);
        //then
        assertThat(member).isEqualTo(findMember);
    }
}
```
```java
package hello.core.order;

import hello.core.AppConfig;
import hello.core.member.Grade;
import hello.core.member.Member;
import hello.core.member.MemberService;
import hello.core.member.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderServiceTest {
    MemberService memberService;
    OrderService orderService;
    @BeforeEach
    public void beforeEach() {
        AppConfig appConfig = new AppConfig();
        memberService = appConfig.memberService();
        orderService = appConfig.orderService();
    }
    
    @Test
    void createOrder() {
        long memberId = 1L;
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.join(member);
        Order order = orderService.createOrder(memberId, "itemA", 10_000);
        assertThat(order.getDiscountPrice()).isEqualTo(1_000);
    }
}
```
## AppConfig 리팩토링

```java
public class AppConfig {
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }

    public OrderService orderService() {
        return new OrderServiceImpl(
                memberRepository(),
                discountPolicy()
        );
    }

    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }

    public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }
}
```
## 사용, 구성 영역을 분리
- 객체 생성 및 구성 영역: AppConfig
- 사용 영역: 그 외 인터페이스, 구현체
# 객체 지향 설계 원칙
- SRP 단일 책임 원칙: 하나의 클래스는 하나의 책임
- DIP 의존관계 역전 원칙: 클라이언트 코드는 인터페이스만으로 구성
- OCP 확장 열림-변경 닫힘 원칙: 소프트웨어 요소를 확장해도 사용 영역의 변경이 닫혀 있다.
- Inversion of Control 제어의 역전: 프로그램 제어 흐름을 외부에서 관리
- IoC 컨테이너, DI 컨테이너: AppConfig처럼 객체를 생성하고 관리하면서 의존관계를 연결
# AppConfig 스프링 기반으로 변경
```java
@Configuration
public class AppConfig {
    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl(memberRepository());
    }
    @Bean
    public OrderService orderService() {
        return new OrderServiceImpl(
                memberRepository(),
                discountPolicy()
        );
    }
    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
    @Bean
    public DiscountPolicy discountPolicy() {
        return new FixDiscountPolicy();
    }
}
```
## MemberApp에 스프링 컨테이너 적용
```java
public class MemberApp {
    public static void main(String[] args) {
        //AppConfig appConfig = new AppConfig();
        //MemberService memberService = appConfig.memberService();
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        MemberService memberService = applicationContext.getBean("memberService", MemberService.class);
        Member member = new Member(1L, "memberA", Grade.VIP);
        memberService.join(member);
        Member findMember = memberService.findMember(1L);
        System.out.println(member.getName());
        System.out.println(findMember.getName());
    }
}
```
## OrderApp에 적용
```java
public class OrderApp {
    public static void main(String[] args) {
        //AppConfig appConfig = new AppConfig();
        //MemberService memberService = appConfig.memberService();
        //OrderService orderService = appConfig.orderService();
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        MemberService memberService = applicationContext.getBean("memberService", MemberService.class);
        OrderService orderService = applicationContext.getBean("orderService", OrderService.class);
        long memberId = 1L;
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.join(member);
        Order order = orderService.createOrder(memberId, "itemA", 10_000);
        System.out.println(order);
    }
}
```