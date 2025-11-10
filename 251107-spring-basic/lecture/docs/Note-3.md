싱글톤 패턴의 문제점들을 하나씩 쉽게 설명해드릴게요! 🎯

## 1️⃣ 싱글톤 패턴을 구현하는 코드 자체가 많이 들어간다

```java
public class SingletonService {
    // 1. static 영역에 객체를 딱 1개만 생성
    private static final SingletonService instance = new SingletonService();
    
    // 2. public으로 열어서 객체 인스턴스가 필요하면 이 메서드를 통해서만 조회
    public static SingletonService getInstance() {
        return instance;
    }
    
    // 3. 생성자를 private으로 선언해서 외부에서 new 키워드 사용을 막음
    private SingletonService() {
    }
    
    public void logic() {
        System.out.println("싱글톤 객체 로직 호출");
    }
}
```
단순히 객체 하나 만드는데 이렇게 많은 보일러플레이트 코드가 필요해요! 😫

## 2️⃣ 의존관계상 클라이언트가 구체 클래스에 의존한다 → DIP 위반

```java
public class OrderService {
    // ❌ 나쁜 예: 구체 클래스에 직접 의존
    private SingletonService service = SingletonService.getInstance();
    
    // 인터페이스가 아닌 구체 클래스(SingletonService)에 의존!
    // DIP 위반: 추상화에 의존해야 하는데 구체화에 의존
}
```

DIP(의존관계 역전 원칙): 구체적인 것이 아닌 추상적인 것에 의존해야 한다
- ❌ `SingletonService.getInstance()` ← 구체 클래스에 직접 의존
- ✅ 인터페이스를 통해 의존해야 함

## 3️⃣ OCP 원칙을 위반할 가능성이 높다

```java
public class OrderService {
    // SingletonService를 다른 구현체로 변경하려면?
    // private SingletonService service = SingletonService.getInstance();
    private NewService service = NewService.getInstance(); // 코드 수정 필요!
    
    // OCP 위반: 확장에는 열려있고 변경에는 닫혀있어야 하는데
    // 구현체를 바꾸려면 클라이언트 코드를 수정해야 함
}
```

OCP(개방-폐쇄 원칙): 확장에는 열려있고 변경에는 닫혀있어야 한다
- 싱글톤 패턴은 구현체 변경 시 클라이언트 코드를 수정해야 함

## 4️⃣ 테스트하기 어렵다

```java
public class OrderServiceTest {
    @Test
    void test1() {
        SingletonService service = SingletonService.getInstance();
        service.setState("test1"); // 전역 상태 변경
    }
    
    @Test
    void test2() {
        SingletonService service = SingletonService.getInstance();
        // test1에서 변경한 상태가 그대로 남아있음!
        // 테스트 간 격리가 안 됨 😱
    }
}
```

문제점:
- Mock 객체로 대체하기 어려움
- 테스트마다 독립적인 인스턴스를 만들 수 없음
- 테스트 간 상태가 공유되어 서로 영향을 줌

## 5️⃣ 내부 속성을 변경하거나 초기화하기 어렵다

```java
public class SingletonService {
    private static final SingletonService instance = new SingletonService();
    
    private String config = "default"; // 이걸 외부에서 바꾸고 싶다면?
    
    private SingletonService() {
        // 생성자가 private이라 파라미터로 설정값을 받을 수 없음!
    }
    
    // 초기화 로직을 추가하기도 어려움
}
```

## 6️⃣ private 생성자로 자식 클래스를 만들기 어렵다

```java
public class SingletonService {
    private SingletonService() {} // private 생성자
}

// ❌ 컴파일 에러!
public class ExtendedService extends SingletonService {
    // 부모 클래스의 생성자가 private이라 상속 불가!
}
```

상속이 막혀서 유연한 확장이 불가능합니다.

## 🎯 결론: 안티패턴

이런 문제들 때문에 전통적인 싱글톤 패턴은 안티패턴으로 불립니다.

### ✅ Spring의 해결책

Spring 컨테이너는 이런 문제들을 모두 해결해줍니다!

```java
// Spring이 관리하는 빈은
@Service
public class OrderService {
    private final MemberRepository memberRepository;
    
    // ✅ 인터페이스에 의존 (DIP 준수)
    // ✅ 생성자 주입으로 테스트 용이
    // ✅ Spring이 싱글톤으로 관리
    public OrderService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
}
```

Spring 컨테이너의 장점:
- 싱글톤의 장점(메모리 효율)은 유지
- 싱글톤 패턴의 모든 단점은 해결
- DIP, OCP 원칙 준수 가능
- 테스트하기 쉬움
- 유연한 구조

이래서 Spring을 사용하는 거예요! 😊

이 내용을 쉽게 풀어서 설명해드릴게요! 🎯

## 🔑 핵심 개념: Spring이 싱글톤 문제를 해결하는 방법

### 📌 전통적인 싱글톤 패턴 vs Spring 컨테이너

#### ❌ 전통적인 싱글톤 패턴
```java
public class MemberService {
    // 1. static으로 인스턴스 보관
    private static final MemberService instance = new MemberService();
    
    // 2. getInstance() 메서드 필요
    public static MemberService getInstance() {
        return instance;
    }
    
    // 3. private 생성자로 외부 생성 막기
    private MemberService() {}
    
    // 실제 비즈니스 로직
    public void doSomething() {}
}

// 사용할 때
MemberService service = MemberService.getInstance(); // 구체 클래스에 의존!
```

문제점:
- 지저분한 코드 많음
- DIP 위반 (구체 클래스에 의존)
- OCP 위반 (변경 어려움)
- 테스트 어려움
- private 생성자 때문에 상속 불가

#### ✅ Spring 컨테이너 방식

```java
// 그냥 평범한 클래스!
@Service
public class MemberService {
    // static도 없고
    // getInstance()도 없고
    // private 생성자도 없고
    
    // 그냥 평범한 생성자
    public MemberService() {}
    
    // 비즈니스 로직만 집중
    public void doSomething() {}
}

// 사용할 때
@Service
public class OrderService {
    private final MemberService memberService;
    
    // 생성자 주입 - 인터페이스에 의존 가능!
    public OrderService(MemberService memberService) {
        this.memberService = memberService;
    }
}
```

## 🎯 "싱글톤 레지스트리"란?

Spring 컨테이너가 객체를 하나만 생성해서 관리하는 저장소 역할을 한다는 의미예요.

```java
// Spring 컨테이너의 내부 동작 (개념적으로)
public class SpringContainer {
    // 싱글톤 레지스트리 (저장소)
    private Map<String, Object> singletonObjects = new HashMap<>();
    
    public Object getBean(String name) {
        // 이미 생성된 객체가 있으면 그걸 반환
        if (singletonObjects.containsKey(name)) {
            return singletonObjects.get(name);
        }
        
        // 없으면 새로 생성하고 저장
        Object bean = createBean(name);
        singletonObjects.put(name, bean);
        return bean;
    }
}
```

## 📊 동작 과정 비교

### 전통적인 싱글톤 패턴
```java
MemberService service1 = MemberService.getInstance();
MemberService service2 = MemberService.getInstance();
// service1 == service2 (같은 인스턴스)
// 하지만 코드가 지저분하고 유연성이 없음
```

### Spring 컨테이너
```java
ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

MemberService service1 = ac.getBean("memberService", MemberService.class);
MemberService service2 = ac.getBean("memberService", MemberService.class);
// service1 == service2 (같은 인스턴스)
// 코드는 깔끔하고 유연함!
```

## 💡 Spring이 해결한 문제들

### 1. 지저분한 코드 제거
```java
// ❌ 전통적 방식: 보일러플레이트 코드 필요
private static final instance = ...
public static getInstance() ...
private 생성자() ...

// ✅ Spring: 그냥 평범한 클래스
@Component
public class MyService { }
```

### 2. DIP 준수 가능
```java
// ❌ 전통적 방식: 구체 클래스에 의존
MemberService service = MemberService.getInstance();

// ✅ Spring: 인터페이스에 의존 가능
public OrderService(MemberRepository repository) {
    this.repository = repository; // 인터페이스 타입!
}
```

### 3. OCP 준수 가능
```java
// Spring 설정만 바꾸면 구현체 변경 가능
@Configuration
public class AppConfig {
    @Bean
    public MemberRepository memberRepository() {
        // return new MemoryMemberRepository(); // 메모리 버전
        return new JdbcMemberRepository(); // DB 버전으로 변경
        // 클라이언트 코드는 수정 안 해도 됨!
    }
}
```

### 4. 테스트 용이
```java
@Test
void test() {
    // Mock 객체 주입 가능!
    MemberRepository mockRepo = mock(MemberRepository.class);
    OrderService service = new OrderService(mockRepo);
    // 테스트 가능!
}
```

### 5. 상속 가능
```java
// 일반 public 생성자 사용 가능
@Service
public class MemberService { 
    public MemberService() {} // public 생성자
}

// 상속 가능!
public class ExtendedMemberService extends MemberService {
    // 문제 없음!
}
```

## 🎉 결론

"Spring 컨테이너 = 똑똑한 싱글톤 관리자"

- ✅ 싱글톤의 장점(메모리 효율): 유지
- ✅ 싱글톤의 단점들: 모두 해결
- ✅ 개발자는 비즈니스 로직에만 집중
- ✅ Spring이 알아서 객체를 하나만 만들어서 관리

이게 바로 "싱글톤 레지스트리" 기능이고, Spring을 쓰는 가장 큰 이유 중 하나예요! 😊

싱글톤의 가장 중요한 주의사항이에요! 실제 예제로 쉽게 설명해드릴게요! 😱

## 🚨 문제 상황: Stateful(상태 유지) 설계

### ❌ 나쁜 예: 공유 필드 사용

```java
@Service
public class StatefulService {
    private int price; // 🚨 인스턴스 필드 (공유됨!)
    
    public void order(String name, int price) {
        System.out.println("name = " + name + " price = " + price);
        this.price = price; // 🚨 문제 발생 지점!
    }
    
    public int getPrice() {
        return price;
    }
}
```

### 💥 실제 발생하는 문제

```java
// Spring 컨테이너에서 같은 인스턴스를 반환
StatefulService service1 = ac.getBean(StatefulService.class);
StatefulService service2 = ac.getBean(StatefulService.class);
// service1 == service2 (같은 객체!)

// 사용자 A가 10000원 주문
service1.order("userA", 10000);

// 사용자 B가 20000원 주문
service2.order("userB", 20000); // 🚨 price 필드를 덮어씀!

// 사용자 A가 주문 금액 조회
int priceA = service1.getPrice();
System.out.println("priceA = " + priceA); // 기대: 10000원, 실제: 20000원! 😱
```

왜 이런 일이?
- `service1`과 `service2`는 같은 객체
- 둘 다 같은 `price` 필드를 공유
- B가 `price`를 변경하면 A의 값도 바뀜!

## 🎯 해결책: Stateless(무상태) 설계

### ✅ 좋은 예 1: 지역변수 사용

```java
@Service
public class StatelessService {
    // 인스턴스 필드 없음!
    
    public int order(String name, int price) {
        System.out.println("name = " + name + " price = " + price);
        return price; // 🎉 지역변수로 바로 반환!
    }
}
```

사용:
```java
StatelessService service1 = ac.getBean(StatelessService.class);
StatelessService service2 = ac.getBean(StatelessService.class);

// 각각 독립적으로 실행
int priceA = service1.order("userA", 10000); // 10000
int priceB = service2.order("userB", 20000); // 20000

System.out.println("priceA = " + priceA); // 10000 ✅
System.out.println("priceB = " + priceB); // 20000 ✅
```

### ✅ 좋은 예 2: 파라미터로 전달

```java
@Service
public class OrderService {
    private final MemberRepository memberRepository; // ✅ 읽기 전용 OK!
    
    public OrderService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    public Order createOrder(Long memberId, String itemName, int price) {
        // 파라미터로 받아서 사용
        Member member = memberRepository.findById(memberId);
        
        // 지역변수 사용
        int discountPrice = calculateDiscount(price);
        
        // 새로운 객체 반환
        return new Order(member, itemName, discountPrice);
    }
}
```

## 📋 Stateless 설계 원칙 정리

### 1️⃣ 특정 클라이언트에 의존적인 필드가 있으면 안됨

```java
// ❌ 나쁜 예
@Service
public class UserService {
    private String currentUser; // 🚨 현재 사용자 정보를 필드에 저장
    
    public void login(String username) {
        this.currentUser = username; // 위험!
    }
}

// ✅ 좋은 예
@Service
public class UserService {
    public User login(String username) {
        User user = findUser(username);
        return user; // 반환해서 사용
    }
}
```

### 2️⃣ 특정 클라이언트가 값을 변경할 수 있는 필드가 있으면 안됨

```java
// ❌ 나쁜 예
@Service
public class CountService {
    private int count = 0; // 🚨 공유 카운터
    
    public void increment() {
        count++; // 여러 사용자가 동시에 접근하면 꼬임!
    }
}

// ✅ 좋은 예 - AtomicInteger 사용 (진짜 공유 카운터가 필요하다면)
@Service
public class CountService {
    private final AtomicInteger count = new AtomicInteger(0);
    
    public int increment() {
        return count.incrementAndGet(); // Thread-safe
    }
}
```

### 3️⃣ 가급적 읽기만 가능해야 함

```java
// ✅ 읽기 전용 필드는 OK!
@Service
public class DiscountService {
    private final int DISCOUNT_RATE = 10; // 상수 - OK!
    private final DiscountPolicy policy;   // 불변 객체 - OK!
    
    public DiscountService(DiscountPolicy policy) {
        this.policy = policy; // 생성자에서 한 번만 설정
    }
    
    public int discount(int price) {
        return policy.discount(price); // 읽기만 함
    }
}
```

## 🔧 공유되지 않는 안전한 방법들

### 1. 지역변수
```java
public void process() {
    int localVar = 10; // 스택 메모리에 저장 - 각 스레드마다 독립적
}
```

### 2. 파라미터
```java
public int calculate(int param) { // 각 호출마다 독립적
    return param * 2;
}
```

### 3. ThreadLocal (사용자별 데이터 격리)
```java
@Service
public class UserContextService {
    private ThreadLocal<String> userContext = new ThreadLocal<>();
    
    public void setUser(String username) {
        userContext.set(username); // 현재 스레드에만 저장
    }
    
    public String getUser() {
        return userContext.get(); // 현재 스레드의 값만 조회
    }
}
```

## 💥 실제 장애 사례

```java
// 실제로 발생한 버그 시나리오
@Service
public class PaymentService {
    private int amount; // 🚨 공유 필드
    
    public void pay(int amount) {
        this.amount = amount;
        // ... 결제 처리 중 (시간이 좀 걸림)
        processPayment(this.amount); // 다른 사용자가 amount를 바꿔버릴 수 있음!
    }
}
```

결과:
- 사용자 A: 10,000원 결제 시작
- 사용자 B: 20,000원 결제 시작 (A의 amount를 덮어씀)
- 사용자 A: 20,000원이 결제됨! 😱😱😱

## ✅ 올바른 코드

```java
@Service
public class PaymentService {
    // 필드에 주문 정보를 저장하지 않음!
    
    public void pay(int amount) {
        // 파라미터로 받아서 지역변수처럼 사용
        processPayment(amount); // 안전!
    }
    
    private void processPayment(int amount) {
        // amount는 이 메서드 안에서만 유효
        System.out.println("결제 금액: " + amount);
    }
}
```

## 🎯 핵심 정리

싱글톤 빈에서는:
- ❌ 인스턴스 필드에 변경 가능한 값 저장 금지
- ✅ 읽기 전용 필드만 사용 (상수, final 의존성)
- ✅ 지역변수, 파라미터, 반환값 활용
- ✅ 상태를 저장하지 말고, 입력값을 받아 결과를 반환

"공유하는 객체에 공유하는 값을 넣으면 안 된다!" 이게 핵심이에요! 😊

좋아, 이건 스프링의 핵심적인 마법 중 하나를 이해하는 아주 중요한 포인트야. 여기서 말하는 `@Configuration`과 CGLIB 바이트코드 조작은 스프링이 어떻게 싱글톤을 보장하는지를 보여주는 대표적인 예지.

## `@Configuration`의 마법: CGLIB 바이트코드 조작

### 🔍 문제 상황
자바 코드에서 `@Bean` 메서드를 여러 번 호출하면, 자바의 기본 동작으로는 매번 새로운 객체가 생성돼. 예를 들어:

```java
@Bean
public MemberService memberService() {
    return new MemberService(memberRepository());
}

@Bean
public MemberRepository memberRepository() {
    return new MemberRepository();
}
```

이 코드에서 `memberRepository()`가 두 번 호출되면, 자바는 두 개의 서로 다른 `MemberRepository` 인스턴스를 생성하게 돼. 하지만 스프링은 싱글톤을 보장해야 하잖아?

### 🧪 실험 결과
```java
System.out.println("bean = " + bean.getClass());
```

출력 결과:
```
bean = class hello.core.AppConfig$$EnhancerBySpringCGLIB$$bd479d70
```

이건 네가 만든 `AppConfig` 클래스가 아니라, 스프링이 CGLIB를 이용해 만든 프록시 클래스라는 뜻이야.

### 🧠 그럼 이게 왜 중요한가?

스프링은 `@Configuration`이 붙은 클래스를 CGLIB으로 상속받아 프록시 객체를 생성하고, 그 프록시 객체가 `@Bean` 메서드를 호출할 때 이미 생성된 빈이 있으면 그것을 반환하도록 오버라이딩해. 즉, 다음과 같은 흐름이 돼:

1. `memberService()`가 호출됨
2. 내부적으로 `memberRepository()`를 호출함
3. 프록시가 `memberRepository()` 호출을 가로채서, 이미 생성된 싱글톤 빈이 있으면 그걸 반환함

이렇게 해서 모든 `@Bean` 메서드 호출이 실제로는 싱글톤 빈을 반환하게 되는 것이야.

### 🧩 만약 `@Configuration`이 없다면?
스프링은 CGLIB 프록시를 만들지 않아. 그러면 `@Bean` 메서드가 호출될 때마다 새로운 객체가 생성돼서 싱글톤이 깨지게 돼.

---

## ✨ 요약

| 항목 | 설명 |
|------|------|
| `@Configuration` | CGLIB 프록시를 생성하도록 지시 |
| CGLIB 프록시 클래스 | 원래 클래스의 상속 버전으로, `@Bean` 메서드 호출을 가로채서 싱글톤을 보장 |
| 출력된 클래스 이름 | `AppConfig$$EnhancerBySpringCGLIB$$...`는 프록시 클래스임을 나타냄 |
| 목적 | `@Bean` 메서드가 여러 번 호출돼도 같은 객체를 반환하게 하기 위함 |

---

혹시 `@Component`와 `@Configuration`의 차이점이나, CGLIB 대신 다른 프록시 방식이 궁금하다면 더 깊게 들어가볼 수 있어!

좋은 질문이야! `@Component`와 `@Configuration`은 둘 다 스프링 컨테이너에 빈을 등록할 수 있게 해주는 애노테이션이지만, 역할과 동작 방식에 중요한 차이점이 있어. 아래에서 핵심 차이를 정리해볼게.

---

## 🧩 공통점

| 항목 | 설명 |
|------|------|
| 스프링 빈 등록 | 둘 다 스프링 컨테이너에 빈으로 등록됨 |
| 자동 감지 대상 | `@ComponentScan`의 대상이 됨 |
| 클래스 수준 애노테이션 | 클래스에 붙여서 사용함 |

---

## ⚔️ 차이점

| 항목 | `@Component` | `@Configuration` |
|------|--------------|------------------|
| 목적 | 일반적인 컴포넌트 등록 | 설정 클래스(빈 설정용) 등록 |
| 내부 동작 | 단순한 빈 등록 | CGLIB 프록시를 통해 `@Bean` 메서드 호출을 가로채서 싱글톤 보장 |
| `@Bean` 메서드 지원 | 사용하지 않음 | `@Bean` 메서드 정의 가능 |
| 싱글톤 보장 | 직접 호출 시 새로운 객체 생성 가능성 있음 | 항상 같은 객체 반환 (싱글톤 보장) |

---

## 🔬 예시 비교

### `@Component` 사용 시

```java
@Component
public class AppConfig {
    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        return new MemberRepository();
    }
}
```

➡ 이 경우 `memberRepository()`가 두 번 호출되면 서로 다른 객체가 생성될 수 있어. 싱글톤이 깨질 수 있다는 뜻이야.

### `@Configuration` 사용 시

```java
@Configuration
public class AppConfig {
    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }

    @Bean
    public MemberRepository memberRepository() {
        return new MemberRepository();
    }
}
```

➡ 이 경우 CGLIB 프록시가 `memberRepository()` 호출을 가로채서 항상 같은 객체를 반환해. 싱글톤이 유지돼.

---

## 🧠 결론

- `@Component`는 일반적인 컴포넌트 등록용
- `@Configuration`은 빈 설정 전용 클래스로, 내부적으로 CGLIB 프록시를 통해 `@Bean` 메서드의 싱글톤을 보장함

---

혹시 `@Bean` 없이 빈을 등록하는 다른 방법이나, `@Import`, `@ComponentScan` 같은 고급 설정이 궁금하다면 더 알려줄게!

좋아요 😊
`AppConfig@CGLIB`은 스프링이 `@Configuration` 클래스(AppConfig) 를 CGLIB(바이트코드 조작 라이브러리) 로 프록시(Proxy) 객체로 변환한 결과예요.

즉, 우리가 만든 `AppConfig` 클래스를 그대로 쓰는 게 아니라, 스프링이 런타임에 “조작된 자식 클래스”를 동적으로 만들어서 등록합니다.
그래서 출력할 때 보면 이런 식으로 나와요 👇

```
AppConfig$$SpringCGLIB$$0x123abc
```

---

## 🧩 원래 코드

예를 들어 원본 `AppConfig`가 이렇게 생겼다고 합시다:

```java
@Configuration
public class AppConfig {

    @Bean
    public MemberRepository memberRepository() {
        System.out.println("call AppConfig.memberRepository");
        return new MemoryMemberRepository();
    }

    @Bean
    public MemberService memberService() {
        System.out.println("call AppConfig.memberService");
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public OrderService orderService() {
        System.out.println("call AppConfig.orderService");
        return new OrderServiceImpl(memberRepository());
    }
}
```

---

## 🧙‍♂️ 스프링이 CGLIB으로 변환한 예상 코드 (의사 코드)

스프링이 내부적으로는 이런 식으로 AppConfig의 자식 클래스를 동적으로 만들어 동작하게 합니다:

```java
public class AppConfig$$SpringCGLIB$$0 extends AppConfig {

    private MemberRepository memberRepository;

    @Override
    public MemberRepository memberRepository() {
        // 이미 생성된 빈이 있으면 반환 (싱글톤 보장)
        if (this.memberRepository == null) {
            this.memberRepository = super.memberRepository(); // 실제 원본 메서드 호출
        }
        return this.memberRepository;
    }

    @Override
    public MemberService memberService() {
        // 이미 memberRepository()가 CGLIB 프록시로 감싸져 있어서
        // 여러 번 호출해도 같은 객체 반환
        return super.memberService();
    }

    @Override
    public OrderService orderService() {
        return super.orderService();
    }
}
```

---

## 🧠 핵심 요약

| 구분                  | 설명                                                                                                         |
| ------------------- | ---------------------------------------------------------------------------------------------------------- |
| CGLIB 역할        | `@Configuration` 클래스의 빈 중복 생성을 막고 싱글톤을 유지하기 위해 프록시 클래스를 만든다                                            |
| AppConfig@CGLIB | 스프링이 런타임에 AppConfig의 프록시 버전을 만들어서 관리 중이라는 뜻                                                            |
| 효과              | `memberRepository()`가 여러 번 호출돼도 항상 같은 객체 반환                                                                |
| CGLIB 안 쓰면?     | `@Configuration` 대신 `@Component`로 등록하면 CGLIB이 적용되지 않아, `memberRepository()`가 여러 번 호출되어 서로 다른 객체가 만들어질 수 있음 |

---

원하면, CGLIB 적용 전후에 Bean 생성 로그 차이도 함께 예시로 보여드릴까요?
(이걸 보면 “프록시가 실제로 싱글톤을 어떻게 유지하는지” 한눈에 이해됩니다.)

아주 좋은 질문이에요 👏
`@Configuration`을 안 붙이면, 스프링이 CGLIB 프록시 클래스를 만들지 않습니다.
그래서 결과적으로 싱글톤이 깨집니다.
아래에서 차근차근 설명해드릴게요 👇

---

## 🧩 예시 코드

```java
@Configuration // ← 이걸 일단 없애볼게요
//@Configuration
public class AppConfig {

    @Bean
    public MemberRepository memberRepository() {
        System.out.println("call AppConfig.memberRepository");
        return new MemoryMemberRepository();
    }

    @Bean
    public MemberService memberService() {
        System.out.println("call AppConfig.memberService");
        return new MemberServiceImpl(memberRepository());
    }

    @Bean
    public OrderService orderService() {
        System.out.println("call AppConfig.orderService");
        return new OrderServiceImpl(memberRepository());
    }
}
```

---

## 🧠 1️⃣ `@Configuration` 있을 때

스프링은 내부적으로 이렇게 처리합니다 👇

```java
AppConfig appConfig = new AppConfig$$SpringCGLIB$$0();
```

CGLIB이 만든 프록시가 `memberRepository()` 호출을 가로채서,
이미 생성된 빈이 있으면 그걸 반환합니다.
그래서 로그는 이렇게 찍혀요 👇

```
call AppConfig.memberRepository
call AppConfig.memberService
call AppConfig.orderService
```

🔹 즉, memberRepository()는 단 한 번만 실행됨 → 싱글톤 보장

---

## 🧠 2️⃣ `@Configuration` 없을 때

이때는 그냥 `@Bean`만 있는 평범한 클래스가 됩니다.
즉, 스프링이 프록시로 감싸지 않아요.

이 말은 곧,
`memberService()`와 `orderService()`가 서로 다른 인스턴스를 만들 때
`memberRepository()`를 직접 새로 호출한다는 뜻입니다.

그래서 로그가 이렇게 바뀝니다 👇

```
call AppConfig.memberRepository
call AppConfig.memberService
call AppConfig.memberRepository
call AppConfig.orderService
call AppConfig.memberRepository
```

🔸 `memberRepository()`가 3번 호출됨
→ `MemoryMemberRepository`가 3개나 생성됨! (싱글톤 깨짐)

---

## 🧩 정리

| 구분                              | `@Configuration` 있음     | `@Configuration` 없음    |
| ------------------------------- | ----------------------- | ---------------------- |
| 스프링이 CGLIB 프록시 생성?              | ✅ 있음                    | ❌ 없음                   |
| `@Bean` 간 의존 호출 시 싱글톤 보장?       | ✅ O                     | ❌ X                    |
| BeanDefinition 등록 방식            | Configuration class로 등록 | 그냥 Component class로 등록 |
| 로그에서 `memberRepository()` 호출 횟수 | 1번                      | 여러 번                   |

---

## ✅ 결론

* `@Configuration` → 스프링이 CGLIB 프록시를 만들어 싱글톤을 보장
* `@Bean`만 쓰면 → 메서드 직접 호출, 싱글톤 깨짐 (다른 객체 생성)

---

원하시면 이 차이를 Junit 테스트 코드로 직접 확인할 수 있는 예제도 보여드릴까요?
(`memberRepository()`가 몇 번 호출되는지 테스트로 확인하는 방식)
