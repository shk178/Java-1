# 스프링 부트 3.2부터 Build and run using에 Gradle을 선택
# 비즈니스 요구사항과 설계
- 회원
    - 회원을 가입하고 조회할 수 있다.
    - 회원은 일반과 VIP 두 가지 등급이 있다.
    - 회원 데이터는 자체 DB를 구축할 수 있고, 외부 시스템과 연동할 수 있다. (미확정)
- 주문과 할인 정책
    - 회원은 상품을 주문할 수 있다.
    - 회원 등급에 따라 할인 정책을 적용할 수 있다.
    - 할인 정책은 모든 VIP는 1000원을 할인해주는 고정 금액 할인을 적용해달라.
    - 할인 정책은 변경 가능성이 높다. 회사의 기본 할인 정책을 아직 정하지 못했고, 오픈 직전까지 고민을 미루고 싶다. 최악의 경우 할인을 적용하지 않을 수도 있다.
# 회원 도메인 설계
## 회원 도메인 협력 관계
* 클라이언트 ㅡ> 회원서비스(회원가입, 회원조회) ㅡ> 회원저장소
* 메모리회원저장소, DB회원저장소, 외부시스템연동회원저장소 --> 회원저장소
## 회원 클래스 다이어그램
* MemberServiceImpl --> `<<interface>>`MemberService
* MemberServiceImpl ㅡ> `<<interface>>`MemberRepository
* MemoryMemberRepository, DbMemberRepository --> `<<interface>>`MemberRepository
## 회원 객체 다이어그램
* 클라이언트 ㅡ> 회원서비스(MemberServiceImpl) ㅡ> 메모리회원저장소
# 회원 도메인 개발
## 회원 엔티티
### 회원 등급
```java
package hello.core.member;

public enum Grade {
    BASIC,
    VIP
}
```
### 회원 엔티티
```java
package hello.core.member;

public class Member {
    private Long id;
    private String name;
    private Grade grade;
    public Member(Long id, String name, Grade grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Grade getGrade() {
        return grade;
    }
    public void setGrade(Grade grade) {
        this.grade = grade;
    }
}
```
## 회원 저장소
### 회원 저장소 인터페이스
```java
package hello.core.member;

public interface MemberRepository {
    void save(Member member);
    Member findById(Long memberId);
}
```
### 메모리 회원 저장소 구현체
```java
package hello.core.member;

import java.util.HashMap;

public class MemoryMemberRepository implements MemberRepository {
    private static Map<Long, Member> store = new HashMap<>(); // 동시성 해결하려면 ConcurrentHashMap
    @Override
    public void save(Member member) {
        store.put(member.getId(), member);
    }
    @Override
    public Member findById(Long memberId) {
        return store.get(memberId);
    }
}
```
## 회원 서비스
### 회원 서비스 인터페이스
```java
package hello.core.member;

public interface MemberService {
    void join(Member member);
    Member findMember(Long memberId);
}
```
### 회원 서비스 구현체
```java
package hello.core.member;

public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    public void join(Member member) {
        memberRepository.save(member);
    }
    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
```
# 회원 도메인 실행과 테스트
## 회원 도메인 - 회원 가입 main
```java
package hello.core;

public class MemberApp {
    public static void main(String[] args) {
        MemberService memberService = new MemberServiceImpl();
        Member member = new Member(1L, "memberA", Grade.VIP);
        memberService.join(member);
        Member findMember = memberService.findMember(1L);
        System.out.println(member.getName());
        System.out.println(findMember.getName());
    }
}
```
## 회원 도메인 - 회원 가입 test

```java
package hello.core.member;

import java.util.Map;

class MemberServiceTest {
    MemberService memberService = new MemberServiceImpl();
    @Test
    void join() {
        //given
        Member member = new Member(1L, "memberA", Grade.VIP);
        //when
        memberService.join(member);
        Member findMember = memberService.findMember(1L);
        //then
        Assertions.assertThat(member).isEqualTo(findMember);
    }
}
```
# 주문과 할인 도메인 설계
## 주문 도메인 협력, 역할, 책임
* 클라이언트 ㅡ><-- 주문 서비스 역할
1. 주문 생성 (회원 id, 상품명, 상품 가격)
4. 주문 결과 반환
* 주문 서비스 역할 ㅡ> 회원 저장소 역할
2. 회원 조회
* 주문 서비스 역할 ㅡ> 할인 정책 역할
3. 할인 적용
### 역할과 구현을 분리
* 주문 서비스 구현체 --> 주문 서비스 역할
* 메모리 회원 저장소, DB 회원 저장소 --> 회원 저장소 역할
* 정액 할인 정책, 정률 할인 정책 --> 할인 정책 역할
## 주문 도메인 클래스 다이어그램
* OrderServiceImpl --> `<<interface>>`OrderService
* OrderServiceImpl ㅡ> `<<interface>>`MemberRepository
* MemoryMemberRepository, DbMemberRepository --> `<<interface>>`MemberRepository
* OrderServiceImpl ㅡ> `<<interface>>`DiscountPolicy
* FixDiscountPolicy, RateDiscountPolicy --> `<<interface>>`DiscountPolicy
### 주문 도메인 객체 다이어그램1
* 클라이언트 ㅡ> 주문 서비스 구현체
* 주문 서비스 구현체 ㅡ> 메모리 회원 저장소
* 주문 서비스 구현체 ㅡ> 정액 할인 정책
### 주문 도메인 객체 다이어그램2
* 클라이언트 ㅡ> 주문 서비스 구현체
* 주문 서비스 구현체 ㅡ> DB 회원 저장소
* 주문 서비스 구현체 ㅡ> 정률 할인 정책
# 주문과 할인 도메인 개발
## 할인 정책 인터페이스
```java
package hello.core.discount;

public interface DiscountPolicy {
    /**
     * @return 할인 대상 금액
     */
    int discount(Member member, int price);
}
```
### 정액 할인 정책 구현체
```java
package hello.core.discount;

public class FixDiscountPolicy implements DiscountPolicy {
    private int discountFixAmount = 1000;
    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return discountFixAmount;
        } else {
            return 0;
        }
    }
}
```
### 주문 엔티티
```java
package hello.core.order;

public class Order {
    private Long memberId;
    private String itemName;
    private int itemPrice;
    private int discountPrice;
    public Order(Long memberId, String itemName, int itemPrice, int discountPrice) {
        this.memberId = memberId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.discountPrice = discountPrice;
    }
    public int calculatePrice() {
        return itemPrice - discountPrice;
    }
    public Long getMemberId() {
        return memberId;
    }
    public String getItemName() {
        return itemName;
    }
    public int getItemPrice() {
        return itemPrice;
    }
    public int getDiscountPrice() {
        return discountPrice;
    }
    @Override
    public String toString() {
        return "Order{" + 
                "memberId=" + memberId +
                ", itemName='" + itemName + '\'' +
                ", itemPrice=" + itemPrice +
                ", discountPrice=" + discountPrice +
                '}';
    }
}
```
## 주문 서비스 인터페이스
```java
package hello.core.order;

public interface OrderService {
    Order createOrder(Long memberId, String itemName, int itemPrice);
}
```
### 주문 서비스 구현체
```java
package hello.core.order;

public class OrderServiceImpl implements OrderService {
    private final MemberRepository memberRepository = new MemoryMemberReporitory();
    private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);
        return new Order(memberId, itemName, itemPrice, discountPrice);
    }
}
```
# 주문과 할인 도메인 실행과 테스트
## 주문과 할인 정책 실행
```java
package hello.core;

public class OrderApp {
    public static void main(String[] args) {
        MemberService memberService = new MemberServiceImpl();
        OrderService orderService = new OrderServiceImpl();
        long memberId = 1L;
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.join(member);
        Order order = orderService.createOrder(memberId, "itemA", 10_000);
        System.out.println(order);
    }
}
```
## 주문과 할인 정책 테스트

```java
package hello.core.order;

class OrderServiceTest {
    MemberService memberService = new MemberServiceImpl();
    OrderService orderService = new OrderServiceImpl();
    @Test
    void createOrder() {
        long memberId = 1L;
        Member member = new Member(memebrId, "memberA", Grade.VIP);
        memberService.join(member);
        Order order = orderService.createOrder(memberId, "itemA", 10_000);
        Assertions.assertThat(order.getDiscountPrice()).isEqualTo(1_000);
    }
}
```