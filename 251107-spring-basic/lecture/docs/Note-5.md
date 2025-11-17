아래 코드를 보면 핵심은 MemberServiceImpl이 MemberRepository 타입을 생성자에서 요구한다는 점이다.

```java
@Component
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
}
```

여기서 자동 주입이 제대로 작동하려면 스프링은 두 가지 클래스를 빈으로 등록해야 한다.

---

# 1. MemberServiceImpl

여기 이미 @Component가 붙어 있으므로 스캔되며 빈으로 등록된다.

따로 할 일이 없다.

---

# 2. MemberRepository의 구현 클래스에 @Component가 필요하다

여기가 핵심이다.

스프링은 인터페이스(MemberRepository)를 빈으로 만들 수 없다.
따라서 MemberRepository를 구현한 클래스를 빈으로 등록해야 한다.

예를 들어 다음 중 하나가 있을 수 있다.

```java
@Component
public class MemoryMemberRepository implements MemberRepository { ... }
```

또는

```java
@Repository
public class JpaMemberRepository implements MemberRepository { ... }
```

즉,

> MemberRepository 인터페이스 자체가 아니라
> MemberRepository를 구현한 실제 클래스에 @Component(또는 @Repository)를 붙여야 한다.

---

# 왜 구현체에 @Component가 필요할까?

MemberServiceImpl 생성자에서 다음과 같이 의존성을 요구한다.

```java
@Autowired
public MemberServiceImpl(MemberRepository memberRepository)
```

스프링이 MemberServiceImpl을 생성하기 위해서는
MemberRepository 타입의 빈이 반드시 필요하다.

그 빈은 다음 중 하나가 되어야 한다.

* @Component 붙은 구현 클래스
* @Repository 붙은 구현 클래스
* @Configuration의 @Bean이 반환한 구현 객체

이 중 어느 것도 없다면 스프링은 다음 예외를 발생시킨다.

```
No qualifying bean of type 'xxx.MemberRepository' available
```

---

# 결론

MemberServiceImpl에는 이미 @Component가 붙어 있으므로 OK
MemberRepository의 구현 클래스에 @Component 또는 @Repository를 붙여야 DI가 정상 작동한다

즉:

```
MemberRepository O → 인터페이스라 스캔해도 아무 의미 없음  
MemoryMemberRepository O → 구현체라서 여기에 @Component 필요함  
```

---

원하면

* 여러 Repository 구현이 있을 때 어떻게 주입되는지
* @Primary, @Qualifier 동작 원리
* 생성자 주입이 제일 좋은 이유
  같은 것도 함께 설명해줄 수 있다.

핵심만 정확하게 정리해줄게.
당신이 혼란스러워하는 지점은 다음 한 줄로 요약된다.

인터페이스는 빈이 될 수 없고, 구현체만 빈이 될 수 있다.

그래서 @ComponentScan을 쓰려면 반드시 “구현 클래스”가 존재해야 하고, 그 클래스에 @Component(또는 @Repository, @Service)를 붙여야 한다.

아래에서 단계별로 구조를 정리해줄게.

---

# 1. 현재 상황 정리

당신이 말한 구조는 아래와 같다.

* `MemberService` → 인터페이스
* `MemberRepository` → 인터페이스
* `MemberServiceImpl` → 구현체 (@Component 있음)
* `MemberRepository`의 “구현체가 없음”

이 상태에서 @ComponentScan을 사용하면 스프링은 다음을 시도한다:

1. MemberServiceImpl을 스캔 → 빈 등록
2. MemberServiceImpl 생성자에서 MemberRepository를 요구
3. MemberRepository의 구현체 빈을 찾지 못해 오류 발생

스프링이 에러를 던지는 이유는 명확하다.

```
No qualifying bean of type 'MemberRepository' available
```

즉, 인터페이스는 빈이 될 수 없기 때문이다.

---

# 2. @ComponentScan 방식을 사용하려면 반드시 구현체에 @Component가 있어야 한다

예:

```java
@Component
public class MemoryMemberRepository implements MemberRepository {
    // 구현
}
```

또는

```java
@Repository
public class JpaMemberRepository implements MemberRepository {
    // 구현
}
```

정리하면:

* MemberServiceImpl → 이미 @Component 달려 있으므로 OK
* MemberRepository의 구현체 → 반드시 @Component 또는 @Repository 달아야 함

이 두 개가 스캔되면 스프링이 자동으로 다음을 한다.

1. 구현체를 빈으로 등록
2. MemberServiceImpl 생성자 주입 시
   MemberRepository 타입에 맞는 구현체를 찾아서 넣어줌

즉, 의존관계 주입이 자동으로 완료된다.

---

# 3. @ComponentScan만으로 빈 생성 + 의존관계 주입하는 패턴

아래처럼 하면 된다.

```
@ComponentScan
└─ @Component class MemberServiceImpl
└─ @Component class MemoryMemberRepository
```

회원 서비스 → 회원 저장소로 연결됨.

이거면 @Configuration에서 @Bean 메서드를 하나도 만들지 않아도 정상 작동한다.

---

# 4. @Configuration + @Bean 방식과의 차이

## @Bean 방식

설정 클래스에서 명시적으로 이렇게 코드를 작성한다.

```java
@Bean
public MemberRepository memberRepository() {
    return new MemoryMemberRepository();
}

@Bean
public MemberService memberService() {
    return new MemberServiceImpl(memberRepository());
}
```

핵심 특징:

* 개발자가 직접 객체 생성 코드를 작성
* 무엇이 주입되는지 코드로 명확하게 보임
* DI 구성이 코드 레벨에서 드러남

---

## @ComponentScan 방식

구현체에 @Component만 붙이면 된다.

```java
@Component
public class MemoryMemberRepository implements MemberRepository {}
```

특징:

* 구현체만 있으면 스프링이 자동으로 빈 생성
* 자동 생성 + 자동 주입
* 설정 파일 따로 필요 없음
* 패키지 구조가 중요함 (스캔 대상)

---

# 5. 그래서 당신의 상황에서는 무엇을 해야 하는가?

## “MemberRepository의 구현체를 하나 만들고 @Component를 붙인다.”

예:

```java
@Component
public class MemoryMemberRepository implements MemberRepository {

    private final Map<Long, Member> store = new HashMap<>();

    @Override
    public void save(Member member) {
        store.put(member.getId(), member);
    }

    @Override
    public Member findById(Long id) {
        return store.get(id);
    }
}
```

이러면 모든 게 해결된다.

---

# 6. 인터페이스만 선언된 구조에서 @ComponentScan이 불가능한 이유

* 스프링은 인터페이스를 인스턴스화할 수 없다
* 구현체가 없으면 주입 불가능
* @ComponentScan은 구현체를 자동으로 객체화할 뿐,
  “어떤 구현체를 쓸지 결정해주지 않는다”

따라서 구현체가 반드시 있어야 한다.

---

# 최종 정리

> @ComponentScan을 사용할 때
> 구현체를 @Component로 등록해야
> 빈 생성과 의존관계 주입이 자동으로 이루어진다.

즉,

* MemberServiceImpl → @Component OK
* MemberRepositoryImpl(구현체) → @Component 필요

이 두 개만 준비되면
@Configuration, @Bean 없이도 자동 DI가 잘 동작한다.

---

원하면

* 구현체가 여러 개일 때 어떤 구현이 주입되는지 (@Primary / @Qualifier)
* Spring이 자동 주입 시 타입 매칭 과정을 어떻게 수행하는지
* ComponentScan과 Bean 방식의 구체적 차이
  도 자세히 설명해줄 수 있다.

아래 내용은 스프링 DI에서 가장 핵심적인 개념들이다.
“구현체가 여러 개일 때 어떻게 주입되는지”,
“스프링이 자동 주입 시 어떤 규칙으로 빈을 선택하는지”,
“@Primary와 @Qualifier가 실제로 어떻게 동작하는지”까지 내부 알고리즘 중심으로 정확하게 설명해줄게.

---

# 1. 스프링이 @Autowired 생성자 주입 시 빈을 찾는 과정

스프링은 의존성 타입에 맞는 빈을 자동으로 선택한다.
내부적으로 다음 순서대로 매칭한다.

---

## 1단계. 타입(Type)으로 1차 매칭

예:

```java
@Autowired
public MemberServiceImpl(MemberRepository memberRepository) {}
```

스프링은 먼저 이렇게 찾는다.

```
MemberRepository 타입의 빈 목록을 모두 가져온다.
```

예를 들어 다음이 있다고 하자:

```java
@Component
public class MemoryMemberRepository implements MemberRepository {}

@Component
public class JpaMemberRepository implements MemberRepository {}
```

그러면 스프링 입장에서는:

```
MemberRepository 후보 = {MemoryMemberRepository, JpaMemberRepository}
```

이 상태면 두 개라 선택할 수 없어서 오류 발생한다.

```
No qualifying bean of type 'MemberRepository' available:
expected single matching bean but found 2
```

---

## 2단계. @Primary가 있는 경우 우선 선택

예:

```java
@Component
@Primary
public class JpaMemberRepository implements MemberRepository {}
```

그럼 스프링은:

```
JpaMemberRepository가 @Primary니까 이걸 쓰자
```

Result: 오류 해결.

---

## 3단계. @Qualifier가 있는 경우 명시적 선택

서비스 쪽에서 이렇게 적는다.

```java
@Autowired
public MemberServiceImpl(@Qualifier("memoryMemberRepository") MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
}
```

또는 생성자 파라미터 이름 기반 자동 매칭도 가능하다.

스프링은 다음 규칙으로 매칭한다.

1. @Qualifier가 지정되면 이름이 일치하는 빈을 찾는다
2. 정확히 일치하면 그 빈을 사용한다
3. 없으면 오류 발생

즉, @Primary보다 우선 적용된다.

---

## 4단계. 이름(name) 매칭 (마지막 단계)

스프링은 자동 주입 시 다음도 고려한다.

```java
@Autowired
public MemberServiceImpl(MemberRepository memoryMemberRepository)
```

여기서 변수 이름이 “memoryMemberRepository”면
스프링이 이 이름과 동일한 빈 이름을 먼저 체크한다.

즉,

1. 타입 매칭
2. 이름 매칭
3. 후보가 줄면 선택

빈 이름은 보통 클래스명을 lowerCamelCase로 생성한다.

```
MemoryMemberRepository → memoryMemberRepository
JpaMemberRepository → jpaMemberRepository
```

---

# 2. 구현체가 여러 개일 때 DI 선택 규칙 정리

위 내용을 표로 정리하면 다음과 같다.

| 우선순위          | 설명                            |
| ------------- | ----------------------------- |
| 1. @Qualifier | 가장 강한 규칙. 지정한 이름과 반드시 일치해야 한다 |
| 2. @Primary   | 여러 개 있을 때 기본값처럼 사용되도록 지정      |
| 3. 파라미터 이름 매칭 | 파라미터 이름과 빈 이름이 같을 경우 사용       |
| 4. 타입 매칭      | 기본 규칙. 타입으로 가장 먼저 후보 리스트 생성   |

정리:

> @Qualifier가 있으면 @Primary는 무시된다.
> @Primary가 있으면 선택 과정을 단순화할 수 있다.

---

# 3. @ComponentScan 방식 vs @Bean 방식의 차이 (내부 동작 기준)

## 3-1. @ComponentScan 방식

* 스프링이 클래스 파일을 스캔해 빈 등록
* 생성자 주입 시 자동으로 필요한 타입을 찾아서 주입
* 설정 파일 없이도 자동 구성 가능
* 패키지 구조 중요
* 구현체가 여러 개면 충돌 발생 가능 → @Primary/@Qualifier 필요

## 3-2. @Bean 방식

```java
@Configuration
public class AppConfig {
    @Bean
    MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
}
```

* 빈 이름이 메서드명으로 결정됨
* 어떤 구현체가 쓰이는지 코드로 명확하게 보임
* 빈 객체 생성 시점을 개발자가 100% 통제 가능
* @Primary / @Qualifier 없이도 중복 방지 쉬움

---

# 4. 예시로 정리

## 상황 A: 구현체 하나만 있을 때

```
@Component
public class MemoryMemberRepository implements MemberRepository {}
```

MemberServiceImpl 생성자에서 MemberRepository 요청 → MemoryMemberRepository 자동 주입
문제 없음.

---

## 상황 B: 구현체 두 개 있을 때

```
@Component
public class MemoryMemberRepository implements MemberRepository {}

@Component
public class JpaMemberRepository implements MemberRepository {}
```

이 상태면 오류발생.

---

## 해결방법 1: @Primary

```
@Component
@Primary
public class JpaMemberRepository implements MemberRepository {}
```

→ JpaMemberRepository가 자동 주입 대상이 됨.

---

## 해결방법 2: @Qualifier 지정

구현체에 @Qualifier 붙이고:

```java
@Component
@Qualifier("memoryRepo")
public class MemoryMemberRepository implements MemberRepository {}
```

서비스에 명시:

```java
@Autowired
public MemberServiceImpl(@Qualifier("memoryRepo") MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
}
```

→ MemoryMemberRepository가 선택됨.

---

## 해결방법 3: 생성자 파라미터 이름 매칭

```java
@Autowired
public MemberServiceImpl(MemberRepository memoryMemberRepository)
```

스프링은 이름이 일치하면 해당 빈을 우선 주입한다.

---

# 최종 핵심 정리

1. 인터페이스만 있으면 빈이 만들어질 수 없다
   → 반드시 @Component 붙은 “구현 클래스”가 필요함

2. 구현체가 하나면 자동으로 그 구현체를 주입한다

3. 구현체가 여러 개면 매칭 규칙은 다음 순서로 적용된다

    1. @Qualifier
    2. @Primary
    3. 파라미터 이름으로 매칭
    4. 타입 매칭

4. @ComponentScan 방식은 자동 설정이므로 편하지만,
   구현체가 많아지면 선택 규칙 설정이 필요하다

5. @Bean 방식은 수동 설정이므로 명확하고 충돌이 없다

---

원하면 다음 내용도 설명해줄 수 있어:

* 빈 이름 충돌 방지 전략 (스프링의 자동 이름 생성 규칙)
* Constructor vs Field vs Setter 주입 방식 차이
* @Autowired(required = false)가 어떻게 동작하는지
* Map/List로 여러 빈 주입 받는 방법
* AOP 프록시 주입 시 선택 규칙

말만 해주면 이어서 더 설명할게.

아래는 요청한 모든 주제를 스프링 내부 동작 원리 기준으로 정리한 내용이다.
단순 암기가 아니라 “스프링이 실제로 어떻게 동작하는지”를 기반으로 설명했다.

---

# 1. 빈 이름 충돌 방지 전략 (스프링의 빈 이름 생성 규칙)

## 1-1. @ComponentScan 시 스프링의 기본 빈 이름 규칙

스프링은 기본적으로 클래스명을 lowerCamelCase로 바꿔서 빈 이름을 만든다.

예:

```
MemoryMemberRepository → memoryMemberRepository
JpaMemberRepository → jpaMemberRepository
```

즉, 클래스명 앞 글자만 소문자로 바꾼다.

이 규칙은 BeanNameGenerator가 담당한다.

### 하지만, 클래스명이 같은 경우?

예:

```
com.a.MemoryRepository
com.b.MemoryRepository
```

이 두 클래스를 스캔하면 이름 충돌이 발생한다.

스프링은 이 경우 다음 예외를 던진다.

```
Annotation-specified bean name 'memoryRepository' for bean class [com.a.MemoryRepository]
conflicts with existing, non-compatible bean definition...
```

해결 방법:

* @Component("aMemoryRepository")처럼 이름을 직접 지정
* @Qualifier로 구분
* @Primary 지정
* 패키지 구조 조정

---

# 2. 생성자/필드/Setter 주입 방식 차이와 내부 동작

## 2-1. 생성자 주입 (가장 권장)

```
public MemberServiceImpl(MemberRepository memberRepository)
```

특징:

* 의존성이 없으면 객체를 생성할 수 없어 “필수 의존성” 보장
* 불변성을 강하게 유지할 수 있음
* 테스트 코드에서 명확함
* 스프링이 가장 먼저 처리하는 주입 방식

스프링 내부 동작:

* 빈 생성 → 생성자 보고 필요한 타입 매칭 → 의존성 빈 생성 → 주입 순서 조정
* 순환 의존성 예방에 유리

---

## 2-2. 필드 주입 (권장하지 않음)

```
@Autowired
private MemberRepository memberRepository;
```

문제점:

* 변경 불가능 (final 불가)
* 외부에서 주입할 방법 없음 → 테스트하기 어려움
* DI 프레임워크 없이는 객체 사용 불가

스프링 동작:

* 객체 먼저 생성 → 리플렉션으로 private 필드에 주입

---

## 2-3. Setter 주입

```
@Autowired
public void setMemberRepository(MemberRepository repo) { ... }
```

특징:

* 선택적 의존성 주입이 가능
* 객체 생성 후 나중에 의존성 주입 가능
* 테스트에서 Mock 주입이 쉬움

스프링 동작:

* 객체 생성 → setter 메서드 찾아 호출 → 의존성 주입

---

# 3. @Autowired(required = false)의 동작 방식

기본값은 required = true이다.

즉, 다음과 같으면 의존성을 반드시 찾아야 한다.

```
@Autowired(required = true)
```

없으면 오류.

---

## required = false이면?

```java
@Autowired(required = false)
public void setX(X x) { }
```

스프링 동작:

* 빈이 존재하면 주입
* 없으면 이 메서드 자체를 호출하지 않는다

즉, 선택적 의존성 주입이다.

중요: 생성자에서는 required=false가 지원되지 않는다.
선택적 주입을 하려면 메서드 또는 필드 주입에서만 가능하다.

---

# 4. 여러 빈을 한꺼번에 주입 받기 (Map/List)

스프링은 타입이 일치하는 모든 빈을 컬렉션으로 주입해준다.

예:

```java
@Autowired
private List<MemberRepository> repositories;
```

이러면 등록된 모든 MemberRepository 빈이 리스트로 들어온다.

또는:

```java
@Autowired
private Map<String, MemberRepository> repoMap;
```

Map의 key는 빈 이름이다.

스프링 동작 순서:

1. MemberRepository 타입의 빈 전체 조회
2. List라면 순서대로 넣고
3. Map이라면 {빈 이름: 빈 객체} 구조로 넣는다

이 기능은 전략 패턴을 구현할 때 가장 많이 쓰인다.

---

# 5. AOP 프록시 주입 시 선택 규칙

AOP가 적용되면 스프링은 원본 객체 대신 프록시 객체를 빈으로 등록한다.

예: @Transactional

```
public class MemberServiceImpl → Proxy(MemberServiceImpl)
```

주입될 때는 항상 프록시 객체가 들어간다.

기준은 다음과 같다.

### 1) 타입을 먼저 체크한다

프록시는 원본 객체를 상속하거나 인터페이스를 구현하므로
타입 매칭이 문제없이 이루어진다.

### 2) 주입되는 것은 반드시 프록시 객체

즉, AOP가 적용되면 MemberServiceImpl 객체는 실제로는 컨테이너 안에 없다.
대신 “프록시 객체”가 빈으로 등록되어 주입된다.

원본이 필요하면 AOP가 내부에서 관리하는 target source에서 가져온다.

---

# 6. 스프링의 주입 선택 규칙 전체 종합

스프링의 자동 주입 규칙을 종합하면 다음과 같다.

1. 타입으로 후보군 생성
2. 후보가 1개면 끝
3. 여러 개면 다음 규칙 적용

    * @Qualifier 우선
    * 없으면 @Primary
    * 없으면 파라미터 이름 기반 매칭
4. 그래도 여러 개면 오류
5. 컬렉션 타입이면 전부 넣음
6. AOP 적용된 경우 프록시가 주입됨

---

# 7. 최종 요약

## 빈 선택 규칙

1. @Qualifier
2. @Primary
3. 파라미터 이름
4. 타입 매칭

## 주입 방식

* 생성자 주입 권장
* 필드 주입 지양
* Setter 주입 선택 의존성에 유리

## 선택적 주입

* @Autowired(required=false)
* Optional<T> 사용 가능

## AOP 주입

* 항상 프록시 객체가 주입됨
* 원본 객체는 직접 주입되지 않음

## 여러 빈 주입

* List, Map 자동 주입 지원
* 전략 패턴 구현 시 매우 유용

---

여기까지가 스프링 DI에서 가장 중요한 “전체 그림”이다.

원하면 아래도 추가로 정리해서 설명해줄 수 있다.

* 빈 라이프사이클 전체 흐름 (postProcessBeforeInitialization → init → postProcessAfterInitialization)
* @PostConstruct와 InitializingBean의 차이
* BeanFactoryPostProcessor vs BeanPostProcessor
* @Transactional 내부 구조와 프록시 동작 방식

필요한 것을 알려줘.

결론부터 말하면:

Member는 @Component를 붙이지 않는다.
싱글톤 빈으로 만들 필요도 없다.

왜 그런지 원리부터 정확하게 설명해줄게.

---

# 1. Member는 “도메인 객체(엔티티)”이기 때문

Member는 보통 이런 형태다.

```java
public class Member {
    private Long id;
    private String name;
}
```

이런 클래스는 애플리케이션의 핵심 데이터 구조, 즉 도메인 모델이다.

도메인 모델은 스프링 빈이 아니다.
왜냐하면 다음과 같은 객체들은 “매번 새로 생성”되는 것이 정상적인 동작이기 때문이다.

* Member (회원)
* Order (주문)
* Product (상품)
* Delivery (배송)

이런 객체들은 요청마다, 입력마다, 비즈니스 로직마다 새로운 데이터가 생기기 때문에
싱글톤 빈으로 만들면 안 된다.

---

# 2. 스프링 빈은 “애플리케이션 서비스”를 위한 객체다

스프링 빈으로 만드는 대상:

* Service 계층
* Repository 계층
* Controller 계층
* Configuration (@Bean)
* AOP, Interceptor 등 인프라 객체

이런 애들은 공통적으로 상태를 가지지 않거나, 가지더라도 필요한 정보만 공유하는 객체다.

따라서 싱글톤으로 관리해도 문제가 없다.

---

# 3. Member를 @Component로 만들면 생기는 문제

예:

```java
@Component
public class Member { ... }
```

이러면 Member가 싱글톤 빈으로 등록됨.

스프링은 하나의 Member 객체를 애플리케이션 전체에서 공유하게 된다.

그러면 완전히 잘못된 상황이 벌어진다:

* 모든 요청이 동일한 Member 객체 하나를 공유하게 됨
* 필드에 값이 들어가면 모든 사용자가 그 값을 함께 공유
* 데이터가 덮어써지고 섞임 → 심각한 버그

Member는 각각의 회원을 표현하는 개별 객체여야 하므로
싱글톤이 되면 안 되는 것이다.

---

# 4. Member는 요청할 때마다 “직접 생성”해야 한다

예:

```java
Member member = new Member(1L, "A");
```

도메인 객체는 이렇게 new로 만들어야 한다.

스프링 컨테이너가 관리할 대상이 아니다.

---

# 5. 정리

## @Component를 붙여야 하는 경우

* 서비스 로직을 수행하는 객체
* 인프라스트럭처 구성 요소
* Repository / Service / Controller
* 설정 클래스 / 프록시 / AOP 대상

## @Component를 붙이면 안 되는 경우

* Member 같은 도메인 엔티티
* Order / Address / Product
* DTO(Data Transfer Object)
* VO(Value Object)
* 매번 달라져야 하는 데이터 객체

---

# 최종 결론

Member는 @Component를 붙이지 않아야 하고, 싱글톤 빈으로 만들어서도 안 된다.
도메인 객체는 스프링에게 관리시키는 대상이 아니다.

---

원하면:

* “어떤 객체를 스프링 빈으로 만들어야 하는지” 기준
* “왜 스프링은 무상태(stateless) 빈을 권장하는지”
* “싱글톤 빈에서 상태를 절대 가지면 안 되는 이유”
  도 더 자세히 설명해줄 수 있다.
