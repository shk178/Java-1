아래 애노테이션 정의는 커스텀 @Qualifier 애노테이션이다.
각 요소가 무엇을 의미하는지, 스프링과 자바 애노테이션 시스템에서 어떤 역할을 하는지 하나씩 정확히 설명해줄게.

---

# 전체 코드 다시 보기

```java
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier("annotationForQualifier")
public @interface AnnotationForQualifier {
}
```

---

# 1. @Target

```java
@Target({
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PARAMETER,
    ElementType.TYPE,
    ElementType.ANNOTATION_TYPE
})
```

## 의미

이 애노테이션을 어디에 적용할 수 있는지를 정하는 메타 애노테이션이다.

여기 지정된 다섯 가지는 다음을 의미한다.

* FIELD
  필드 주입에서 사용 가능

  ```java
  @AnnotationForQualifier
  private DiscountPolicy discountPolicy;
  ```

* METHOD
  setter 주입 또는 일반 메서드에서도 사용 가능

  ```java
  @AnnotationForQualifier
  public void setDiscountPolicy(DiscountPolicy dp) {}
  ```

* PARAMETER
  생성자 파라미터에서 사용 가능

  ```java
  public MemberService(@AnnotationForQualifier DiscountPolicy dp) {}
  ```

* TYPE
  클래스 레벨에서도 사용 가능

  ```java
  @AnnotationForQualifier
  @Component
  public class FixDiscountPolicy implements DiscountPolicy {}
  ```

* ANNOTATION_TYPE
  다른 애노테이션에서 사용하는 “메타 애노테이션”으로도 활용 가능
  즉, 커스텀 애노테이션을 위한 애노테이션으로도 쓸 수 있게 함.

정리하면 애노테이션을 거의 모든 곳에 붙일 수 있도록 허용한 것.

---

# 2. @Retention(RetentionPolicy.RUNTIME)

## 의미

애노테이션이 언제까지 살아있을지를 결정한다.

RetentionPolicy는 세 가지가 있다.

* SOURCE
  컴파일되면 사라짐(예: @Override)

* CLASS
  class 파일에는 남지만 런타임에는 사라짐

* RUNTIME
  class 파일에도 남고, 런타임에서도 Reflection으로 읽을 수 있음

여기서 RUNTIME을 사용한 이유는 다음 때문이다.

스프링은 @Qualifier를 런타임에 리플렉션으로 읽어서
DI 과정에 반영하기 때문에 런타임 유지가 필수다.

---

# 3. @Documented

## 의미

이 애노테이션을 사용한 요소가 javadoc 문서에 표시되도록 한다.

예:

```java
/
 * @AnnotationForQualifier
 */
```

이 문서 생성 시 @AnnotationForQualifier도 표시된다.

기능 자체는 스프링 DI와 무관하지만
애노테이션을 공식 문서 형태로 보이게 하는 용도이다.

---

# 4. @Qualifier("annotationForQualifier")

## 의미

스프링 DI에서 이 커스텀 애노테이션을
Qualifier 역할로 사용할 수 있게 만드는 핵심 기능이다.

즉,

```java
@Qualifier("annotationForQualifier")
```

이 의미는:

* 이 애노테이션을 붙인 빈을
* “annotationForQualifier”라는 Qualifier 그룹으로 분류한다.

이런 식으로 사용할 수 있게 된다.

## 사용 예

구현체에 적용:

```java
@Component
@AnnotationForQualifier
public class FixDiscountPolicy implements DiscountPolicy {}
```

주입부에서 적용:

```java
@Autowired
public OrderService(@AnnotationForQualifier DiscountPolicy discountPolicy) {
    this.discountPolicy = discountPolicy;
}
```

이때 스프링은 내부적으로 이렇게 처리한다.

1. DiscountPolicy 빈 목록 조회
2. @AnnotationForQualifier 붙은 빈만 필터링
3. 그 빈을 주입 대상으로 선택

즉,
"이 커스텀 애노테이션은 @Qualifier("annotationForQualifier")를 나타낸다"
라는 의미이다.

---

# 5. public @interface AnnotationForQualifier

여기서 핵심은 다음 두 가지이다.

* @interface → 자바 애노테이션 정의
* public → 어디서나 사용할 수 있는 공개 애노테이션

즉, 이 전체 코드는 “Qualifier 용도로 사용하기 위한 커스텀 애노테이션”을 만든 것.

---

# 전체 정리

| 항목                | 의미                                     |
| ----------------- | -------------------------------------- |
| @Target           | 애노테이션을 어느 위치에 적용할 수 있는지 결정             |
| @Retention        | 런타임에 사용하기 위해 애노테이션 정보를 유지              |
| @Documented       | javadoc 문서에서 표시되도록 함                   |
| @Qualifier("...") | 스프링 DI에서 특정 빈을 구분하기 위한 Qualifier 역할 수행 |
| @interface        | 사용자 정의 애노테이션 선언                        |

---

원한다면 다음도 설명해줄 수 있다.

* 왜 커스텀 Qualifier를 만들 때 @Qualifier를 붙이는지 내부 구조
* 스프링이 @Qualifier 정보를 어떻게 읽고 DI 매칭을 수행하는지
* @Primary와의 동작 우선순위 비교
* 여러 개의 custom qualifier를 조합하는 방법

말해줘.

1. 필드 주입(Field Injection) 방식이 일반적으로 권장되지 않는 이유는 무엇일까요?
- A 불변성을 보장하기 어려워서
- B 의존성 변경이 쉬워서
- (답) 순수 Java 코드로 테스트하기 어려워서
- D 설정 코드 작성이 복잡해서
해설
필드 주입은 외부에서 의존성을 주입하기 어려워 순수 Java 테스트가 힘들어집니다. 또한 final 키워드를 사용할 수 없어 불변성을 보장하기 어렵다는 단점도 있습니다.
2. @Autowired로 의존성을 주입할 때, Bean이 없는 경우 예외가 발생하지 않도록 처리하는 방법과 거리가 먼 것은?
- A required=false 옵션 사용
- B @Nullable 애노테이션 사용
- C Optional (Java 8) 사용
- (답) @Primary 애노테이션 사용
해설
@Primary는 동일 타입 Bean이 여러 개일 때 우선적으로 선택될 Bean을 지정합니다. Bean이 아예 없을 때 오류를 막는 역할은 required=false, @Nullable, Optional이 수행합니다.
3. @Autowired 사용 시 같은 타입의 Bean이 여러 개 있을 때, Spring이 의존성을 해결하는 방법 중 우선순위가 가장 높은 것은 무엇일까요?
- A 타입 매칭
- B 필드/파라미터 이름 매칭
- C @Primary 사용
- (답) @Qualifier 사용
해설
Spring은 동일 타입 Bean이 여러 개일 때 @Qualifier가 붙은 Bean을 가장 먼저 찾습니다. 그 다음 @Primary, 마지막으로 필드나 파라미터 이름으로 매칭합니다.
4. final 필드를 매개변수로 하는 생성자를 자동 생성하여 생성자 주입 코드를 간소화해주는 Lombok 애노테이션은 무엇인가요?
- A @Getter
- B @Setter
- (답) @RequiredArgsConstructor
- D @Autowired
해설
이 애노테이션은 클래스의 final 필드들을 모아 자동으로 생성자를 만들어 줍니다. 반복적인 생성자 코드를 줄여주어 코드를 간결하게 만들 수 있습니다.
5. @Autowired를 사용하여 특정 타입의 모든 Spring Bean들을 한번에 주입받고자 할 때, 주로 사용되는 컬렉션 타입은 무엇일까요?
- A Set 또는 Queue
- (답) List 또는 Map
- C Array 또는 Deque
- D Stack 또는 Vector
해설
Spring 컨테이너는 특정 타입의 모든 Bean들을 List나 Map 형태로 주입하는 기능을 지원합니다. 이를 통해 여러 구현체 중 하나를 동적으로 선택하는 전략 패턴 등을 쉽게 구현할 수 있습니다.

스프링 Bean Lifecycle(빈 생명주기) 은 스프링 컨테이너가 빈을 생성 → 의존관계 주입 → 초기화 → 사용 → 소멸 하는 전체 과정을 말한다. 각 단계에서 스프링이 어떤 일을 하고 개발자가 어떤 훅(hook)을 넣을 수 있는지 정리해줄게.

---

# 1. 전체 흐름 요약

스프링 컨테이너는 빈을 관리할 때 아래 순서로 진행한다.

1. Bean 인스턴스 생성
2. 의존관계 주입(DI)
3. 초기화(Call init methods)
4. 사용
5. 소멸(Call destroy methods)

---

# 2. 단계별 상세 설명

## 2-1. Bean 생성

스프링이 빈을 스캔하거나 @Configuration 안의 @Bean 메서드를 읽고 인스턴스를 생성한다.

* 일반적으로 생성자는 호출만 하고 아무 로직도 넣지 않는 것이 좋다.
* 이 시점에는 의존관계가 아직 주입되지 않았기 때문에 사용하기 이르다.

## 2-2. 의존관계 주입

생성된 빈에 대해 @Autowired, 생성자 주입 등을 통해 필요한 다른 빈을 연결한다.

* 생성자 주입을 사용하면 생성과 의존관계 주입이 동시에 일어나지만, 라이프사이클 상에서 개념적으로는 생성 → DI 단계로 구분된다.

## 2-3. 초기화 단계

의존관계가 모두 준비된 뒤 “이제 사용할 준비가 끝났습니다” 를 의미하는 단계.

스프링이 제공하는 초기화 방식은 세 가지다.

### 1) `InitializingBean.afterPropertiesSet()`

* 스프링 전용 인터페이스
* 추천되지 않음

### 2) `@Bean(initMethod="init")`

* 자바 설정에서 메서드를 지정
* 스프링에 종속되지 않음

### 3) `@PostConstruct`

* 가장 권장되는 방식
* JSR-250 표준
* 간결하고 스프링 독립적

```java
@PostConstruct
public void init() {
    // 초기화 작업
}
```

## 2-4. 사용

Application 실행 중에 빈은 정상적인 역할을 수행한다.
이 단계는 스프링이 특별한 처리를 하지는 않는다.

## 2-5. 소멸 단계

컨테이너가 내려갈 때 빈이 정리(clean-up) 작업을 수행하는 단계.

소멸도 초기화와 마찬가지로 다양한 방식이 있다.

### 1) `DisposableBean.destroy()`

* 스프링 전용 인터페이스
* 권장되지 않음

### 2) `@Bean(destroyMethod="close" 또는 "shutdown")`

* 메서드 이름을 지정해 호출하도록 설정
* 라이브러리의 close()를 호출해야 할 때 유용

### 3) `@PreDestroy`

* 가장 권장됨
* 표준 어노테이션

```java
@PreDestroy
public void close() {
    // 종료 작업
}
```

---

# 3. 라이프사이클에 BeanPostProcessor가 끼어드는 과정

BeanPostProcessor는 빈 생성 과정 중 아래 시점에서 개입한다.

1. 의존관계 주입이 끝난 후, 초기화 메서드가 호출되기 전에
2. 초기화 메서드가 끝난 후

이 덕분에 AOP 프록시 생성, @Autowired 처리 같은 기능을 구현할 수 있다.

흐름을 단계 포함해서 표현하면:

1. 생성
2. 의존관계 주입
3. BeanPostProcessor: before initialization
4. 초기화 메서드 (@PostConstruct 등)
5. BeanPostProcessor: after initialization
6. 빈 사용
7. 소멸 단계(@PreDestroy 등)

---

# 4. 전체를 시각적으로 정리

```
[빈 생성]
      ↓
[의존관계 주입]
      ↓
BeanPostProcessor(before)
      ↓
[초기화(@PostConstruct)] 
      ↓
BeanPostProcessor(after)
      ↓
[사용]
      ↓
[소멸(@PreDestroy)]
```

---

필요하다면

* 실제 동작 로그를 보면서 설명
* BeanPostProcessor 직접 구현
* 스프링 부트에서의 라이프사이클
* 객체 생성 시점과 DI 시점 차이
  같은 것도 더 자세히 설명해줄 수 있어.

초기화(init)와 소멸(destroy)을 콜백(callback) 이라고 부르는 이유는 개발자가 직접 호출하지 않고, 스프링 컨테이너가 특정 시점에 자동으로 호출해주는 메서드이기 때문이다.

조금 더 원리적으로 설명해줄게.

---

# 1. 호출 주체가 다르기 때문에 콜백이다

일반적으로 메서드는 개발자가 직접 호출한다.

```java
MyObject obj = new MyObject();
obj.init();   // 직접 호출
```

하지만 스프링 빈의 초기화/소멸 메서드는 개발자가 절대 직접 부르지 않는다.

```
Container → 빈 생성
Container → 의존관계 주입
Container → 초기화 메서드 호출(콜백)
...
Container → 소멸 메서드 호출(콜백)
```

즉,

> “내가 필요한 타이밍에 스프링이 대신 불러줘.”

이 메커니즘이 바로 콜백의 본질이다.

---

# 2. 생명주기 훅(hook)의 의미

스프링은 빈 생명주기 과정에서 특정 지점을 마련해두고, 그 지점마다 개발자 코드가 끼어들 수 있는 구멍(hook)을 제공한다.

초기화/소멸 메서드는 바로 이 훅 지점에서 컨테이너에 의해 자동으로 호출되는 메서드라서 콜백이라고 부른다.

다시 말해,

* 빈이 생성된 직후 → 초기화 콜백
* 컨테이너 종료 직전 → 소멸 콜백

이런 시점이 정해져 있는 것.

---

# 3. 일반적인 "콜백" 정의와 스프링 빈의 초기화/소멸 비교

일반적인 콜백(callback)의 정의:

> 내가 직접 호출하지 않고 프레임워크나 라이브러리가 나중에 특정 시점에 호출해주는 함수.

스프링의 초기화/소멸:

* @PostConstruct / @PreDestroy
* InitializingBean.afterPropertiesSet()
* DisposableBean.destroy()
* @Bean(initMethod, destroyMethod)

위 메서드들은 모두
프레임워크(스프링)가 알아서 호출하기 때문에 콜백 조건을 충족한다.

---

# 4. 개발자가 호출하지 않기 때문에 "프레임워크 제어 흐름"에 속한다

스프링은 IoC 컨테이너이다.
즉, 객체 생성·주입·라이프사이클 제어 흐름을 스프링이 가져간다.

이 흐름 속에서 초기화/소멸이 자동 호출되므로
개발자는 다음만 정하면 된다.

* “이 메서드가 초기화 시점에 불리면 좋겠어”
* “이 메서드가 종료 시점에 불리면 좋겠어”

이걸 표시하는 것만으로 자동 호출되니까 콜백이다.

---

# 정리

초기화/소멸을 콜백이라고 부르는 이유는:

1. 개발자가 직접 호출하지 않기 때문
2. 컨테이너가 특정 생명주기 시점에 자동 호출하기 때문
3. 프레임워크가 제공하는 라이프사이클 훅(hook)에 코드가 등록되는 방식이기 때문
4. 일반적인 콜백 정의에 정확히 부합하기 때문

원한다면

* @PostConstruct 호출 시점의 실제 로그 흐름
* Init 메서드/Destroy 메서드를 직접 호출하면 왜 위험한지
* BeanPostProcessor와 콜백의 차이
  같은 것도 더 깊게 설명해줄게.

스프링 전용 인터페이스(InitializingBean, DisposableBean)를 권장하지 않는 이유는 크게 세 가지다.
핵심은 객체가 스프링에 종속되기 때문이며, 이는 애플리케이션 설계의 유연성을 해친다.

---

# 1. 스프링에 강하게 종속되는 코드가 되기 때문

클래스가 InitializingBean, DisposableBean을 구현하면 다음과 같은 문제가 생긴다.

* 그 클래스는 스프링 환경에서만 정상적으로 동작하도록 설계된다.
* 다른 DI 컨테이너나 환경에서 재사용하기 어렵다.
* 순수 자바 객체(POJO)라는 장점을 잃는다.

예:

```java
public class NetworkClient implements InitializingBean, DisposableBean {
    // 이 클래스는 스프링에 의존적이게 됨
}
```

초기화/종료 로직이 스프링 API 인터페이스에 묶여 있기 때문에, 객체는 더 이상 순수 자바 객체(POJO) 아니며 프레임워크 의존 객체가 된다.

이는 스프링이 지향하는 POJO 기반 개발 철학과 맞지 않는다.

---

# 2. 생명주기 로직을 외부 설정(@Bean, @PostConstruct)으로 분리할 수 없기 때문

객체의 초기화/소멸 로직을 클래스 내부에 넣어야 하기 때문에 유연성이 떨어진다.

반면 @PostConstruct 또는 @Bean(initMethod, destroyMethod)을 사용하면 다음이 가능하다.

* 초기화/소멸 로직을 클래스 바깥에서 설정 가능
* 외부 라이브러리에 초기화/종료 메서드를 등록할 수 있음
* 스프링 종속성을 제거할 수 있음

예: 외부 라이브러리 클래스에 콜백을 적용하려면 인터페이스를 구현할 수 없다.
하지만 initMethod, destroyMethod는 가능하다.

---

# 3. 표준이 아니기 때문

InitializingBean과 DisposableBean은 스프링 프레임워크 전용 인터페이스다.

반면에 @PostConstruct, @PreDestroy는 JSR-250 자바 표준이다.

표준 어노테이션의 장점:

* 다른 프레임워크에서도 대부분 사용 가능(예: Jakarta EE)
* 자바 기반 DI 컨테이너 공통 규칙
* 학습 비용 감소

표준을 쓰는 것이 더 넓은 환경에서 호환성이 좋다.

---

# 4. 코드가 불필요하게 복잡해지고 테스트가 어려워짐

스프링 전용 인터페이스를 구현하면 테스트 환경에서 스프링 컨테이너 없이 객체를 테스트할 때 이런 일이 생긴다.

* afterPropertiesSet()을 테스트에서 직접 호출해줘야 함
* destroy()도 직접 호출해야 함

즉, 순수 자바 객체라면 신경 쓸 필요 없는 로직이 테스트에서 부담이 된다.

반면 @PostConstruct는 테스트에서 그냥 객체를 만들면 호출되지 않기 때문에
테스트 코드에서 객체를 초기화하거나 별도로 관리하도록 자연스럽게 설계할 수 있다.

---

# 5. 실제 스프링 가이드에서도 POJO 유지가 핵심 철학

스프링이 프레임워크로 성장할 수 있었던 이유는 객체를 프레임워크 종속에서 최대한 분리했기 때문이다.

스프링 개발 팀도 이 철학에 따라
InitializingBean / DisposableBean을 legacy 방식으로 분류하고
@PostConstruct / @PreDestroy 사용을 공식 권장한다.

---

# 정리

스프링 전용 인터페이스를 권장하지 않는 이유는:

1. 객체가 스프링에 종속되어 POJO가 아니게 된다.
2. 초기화/소멸 로직을 외부 설정으로 분리할 수 없다.
3. 자바 표준이 아니며 호환성과 유연성이 떨어진다.
4. 테스트를 어렵게 하고 코드 재사용성을 떨어뜨린다.
5. 스프링 철학(POJO 지향)에 어긋난다.

추가로 원하면

* @Bean(initMethod, destroyMethod) 자동 메서드 추론 원리
* @PostConstruct 동작 순서
* 인터페이스 기반 콜백이 실제로 호출되는 내부 로직
  도 설명할 수 있어.

`ConfigurableApplicationContext`을 사용하는 이유는 close() 메서드를 호출하기 위해서다.
스프링 빈의 소멸(destroy) 콜백을 실행하려면 컨테이너를 닫아야 하고, close()는 `ApplicationContext`의 기본 기능이 아니라 ConfigurableApplicationContext에만 존재하는 기능이다.

아래에서 단계별로 정리해줄게.

---

# 1. ApplicationContext에는 close()가 없다

`ApplicationContext`는 스프링 컨테이너의 기본 인터페이스이지만,
여기에는 종료 기능이 포함되어 있지 않다.

즉, 아래처럼 하면 컴파일 오류가 난다.

```java
ApplicationContext ac = new AnnotationConfigApplicationContext(...);
ac.close(); // 불가능
```

왜냐하면 ApplicationContext는 읽기 전용(Read-Only) 컨테이너로 정의돼 있기 때문이다.

---

# 2. 그래서 close()를 제공하는 ConfigurableApplicationContext 사용

`ConfigurableApplicationContext`는 `ApplicationContext`의 하위 인터페이스이며,
스프링 컨테이너를 시작/종료할 수 있는 기능을 제공한다.

특히 중요한 메서드:

* close()
* refresh()

이 기능 덕분에 destroy 콜백이 동작한다.

```java
ConfigurableApplicationContext ac =
        new AnnotationConfigApplicationContext(LifeCycleConfig.class);

ac.close(); // 소멸 콜백 실행
```

이 close() 호출이 있어야만:

* @PreDestroy
* DisposableBean.destroy()
* @Bean(destroyMethod)
  이 정상적으로 작동한다.

---

# 3. AnnotationConfigApplicationContext는 ConfigurableApplicationContext를 구현한다

클래스 구조를 보면:

```
ApplicationContext
 └── ConfigurableApplicationContext
      └── AnnotationConfigApplicationContext
```

즉, AnnotationConfigApplicationContext 자체는 close()를 지원하지만
타입을 ApplicationContext로 받으면 close()를 호출할 수 없게 된다.

그래서 타입을 ConfigurableApplicationContext로 잡은 것이다.

---

# 4. 테스트 코드에서 특히 중요

테스트에서는 컨테이너를 직접 열고 닫는 흐름이 필요하다.

* 초기화 로그 확인
* 소멸 로그 확인
* 리소스 정상 정리 확인

close() 호출 없이는 destroy()가 실행되지 않으므로
라이프사이클 테스트에서는 반드시 ConfigurableApplicationContext를 사용한다.

---

# 정리

ConfigurableApplicationContext을 사용하는 이유는:

1. ApplicationContext에는 close()가 없기 때문
2. close()를 호출해야 destroy 콜백이 실행되기 때문
3. AnnotationConfigApplicationContext가 ConfigurableApplicationContext를 구현하기 때문
4. 특히 테스트에서는 lifecycle 확인을 위해 close() 호출이 필수이기 때문

원하면

* refresh()와 close()의 내부 동작 원리
* BeanFactory와 ApplicationContext의 관계
* 스프링 컨테이너 종료 시 실제 destroy 호출 순서
  같은 것도 더 자세히 설명해줄 수 있어.

결론부터 말하면,

ApplicationContext 타입으로 변수를 잡으면 destroy는 “자동으로” 실행되지 않는다.
하지만 구체 구현체가 close()를 지원한다면, 형변환(cast)해서 호출하면 destroy는 실행된다.

정리하면:

* 타입을 ApplicationContext로 선언 → close()를 호출할 수 없음 → destroy 실행 불가
* 하지만 실제 객체가 ConfigurableApplicationContext라면 → 형변환으로 close()는 실행 가능 → destroy도 정상 실행됨

아래에서 정확한 이유를 설명할게.

---

# 1. ApplicationContext 자체에는 close()가 없다

ApplicationContext는 읽기 전용 컨테이너 역할만 담당하도록 설계된 상위 인터페이스다.

여기에는 종료 책임이 포함되지 않기 때문에 close() 메서드가 없다.

```java
ApplicationContext ac = new AnnotationConfigApplicationContext(...);
// ac.close();   // 컴파일 에러
```

따라서 ApplicationContext 타입 변수로는 컨테이너 종료를 수행할 수 없다.

---

# 2. 하지만 실제 객체는 close() 기능을 갖고 있다

`AnnotationConfigApplicationContext`는
`ConfigurableApplicationContext`를 구현하므로 close()가 존재한다.

즉, 타입을 좁혀버려서 close()를 못 호출하는 것뿐이지
실제 객체는 종료 기능을 갖고 있다.

형변환하면 close()를 호출할 수 있다.

```java
ApplicationContext ac = new AnnotationConfigApplicationContext(LifeCycleConfig.class);

((ConfigurableApplicationContext) ac).close(); // destroy 정상 실행
```

이렇게 하면 @PreDestroy / destroyMethod / DisposableBean.destroy()가 실행된다.

---

# 3. 그래서 “절대 실행 안 된다”는 아닌데, 기본적으로는 호출할 방법이 없다

정리하자면:

* ApplicationContext 타입 사용 → close() 호출 불가 → destroy 콜백도 실행되지 않음
* 하지만 ConfigurableApplicationContext로 캐스팅하면 → close() 호출 가능 → destroy 실행됨
* 단, 스프링이 자동으로 호출해주는 일은 절대 없다

즉, ApplicationContext는 컨테이너 종료 책임이 없기 때문에
“destroy가 자동 실행되는 일은 없다”가 정확한 표현이다.

---

# 4. 테스트나 메인에서 destroy 콜백을 확인하고 싶다면 타입을 바꿔야 한다

다음 중 하나로 선언해야 destroy 콜백이 실행된다.

1. ConfigurableApplicationContext로 받기

```java
ConfigurableApplicationContext ac =
        new AnnotationConfigApplicationContext(LifeCycleConfig.class);
ac.close();
```

2. AnnotationConfigApplicationContext로 직접 받기

```java
AnnotationConfigApplicationContext ac =
        new AnnotationConfigApplicationContext(LifeCycleConfig.class);
ac.close();
```

둘 다 close() 호출이 가능하다.

---

# 결론

* ApplicationContext로 선언하면 destroy는 기본적으로 실행되지 않는다.
* 하지만 실제 객체가 종료 기능을 지원하면, 형변환해서 close()를 호출하면 destroy는 실행된다.
* 자동으로 실행되는 일은 없다. 반드시 close()를 직접 호출해야 한다.

원하면

* close()가 무엇을 내부적으로 수행하는지
* 스프링 컨테이너 종료 시 destroy 호출 순서
* 스프링 부트에서는 언제 destroy가 실행되는지
  같은 것도 이어서 설명해줄게.

결론부터 말하면,

ApplicationContext 타입으로 만든 컨테이너는
프로그램이 종료되거나 JUnit 테스트가 끝나도 destroy 콜백이 자동으로 실행되지 않는다.

이유는 명확하다.
ApplicationContext는 컨테이너 종료를 책임지는 close() 기능이 없기 때문에,
스프링이 알아서 destroy를 호출할 기회를 갖지 못한다.

아래에서 구체적으로 설명할게.

---

# 1. 자바 프로그램이 종료될 때 자동 destroy는 실행되지 않는다

스프링 컨테이너가 destroy를 호출하려면 다음 순서가 필요하다.

1. close() 또는 registerShutdownHook() 같은 종료 요청이 들어와야 한다.
2. 컨테이너가 내부적으로 destroy 단계로 진입한다.
3. 각 빈의 destroy 메서드를 호출한다.

`ApplicationContext`는 close() 자체가 존재하지 않는다.
즉, 스프링은 컨테이너 종료 신호를 받을 방법이 없다.

결과적으로:

* main 메서드가 끝남
* JVM이 종료됨
* 컨테이너는 종료 콜백을 호출할 기회를 얻지 못함

그래서 destroy 메서드는 실행되지 않는다.

---

# 2. @Test 실행이 끝나도 destroy는 실행되지 않는다

JUnit은 테스트가 끝나도 컨테이너를 대신 종료시켜주지 않는다.
테스트 종료는 JVM이 컨테이너에게 “종료하라”고 말해주는 절차가 아니다.

따라서 다음과 같을 때 destroy는 절대 호출되지 않는다.

```java
@Test
void test() {
    ApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
    // 테스트 끝
} // destroy 실행 안 됨
```

테스트가 끝나도 스프링 컨테이너는 close()를 수행하지 않는다.

---

# 3. destroy가 실행되는 경우

### 1) 직접 close() 실행

```java
ConfigurableApplicationContext ac =
        new AnnotationConfigApplicationContext(AppConfig.class);
ac.close(); // destroy 실행
```

### 2) registerShutdownHook() 사용

```java
AnnotationConfigApplicationContext ac =
        new AnnotationConfigApplicationContext(AppConfig.class);

ac.registerShutdownHook(); // JVM 종료될 때 destroy 호출
```

이 경우 JVM 종료 시점에 destroy가 호출된다.

---

# 4. 왜 ApplicationContext는 자동 종료를 담당하지 않는가

ApplicationContext는 읽기 전용 컨테이너(lookup-only) 라는 철학을 기반으로 하며
생명주기를 직접 관리하도록 설계되지 않았다.

그래서:

* close() 없음
* shutdownHook 등록 기능도 없음
* destroy 콜백 책임 없음

생명주기 관리까지 포함하려면
ConfigurableApplicationContext를 사용해야 한다.

---

# 정리

### ApplicationContext 타입을 사용할 때:

* 프로그램이 종료돼도 destroy 실행되지 않는다.
* JUnit @Test가 끝나도 destroy 실행되지 않는다.
* 컨테이너가 종료 이벤트를 받을 방법이 없기 때문이다.

### destroy가 실행되려면 반드시 다음 중 하나가 필요:

* ac.close()
* ac.registerShutdownHook()
* 또는 ConfigurableApplicationContext/AnnotationConfigApplicationContext로 사용

필요하다면:

* registerShutdownHook의 동작 원리
* 스프링 부트에서 destroy가 자동 실행되는 이유
* 웹 애플리케이션에서 컨테이너가 자동 종료되는 구조

같은 것도 더 깊게 설명해줄게.

소멸 단계에서 말하는 “destroy”는 개념적인 소멸 단계 전체를 의미하는 것이고,
구현 방식으로서의 destroy() 메서드 하나만을 의미하는 것은 아니다.

즉, 소멸 단계 = destroy() 메서드라고 단정할 수 없다.
정확히 말하면:

> 스프링 빈의 소멸 단계는 여러 방식의 “소멸 콜백”을 실행하는 과정 전체를 말한다.
> 그 중 하나가 DisposableBean.destroy() 메서드일 뿐이다.

이걸 구조적으로 다시 정리해줄게.

---

# 1. 스프링 빈의 소멸 단계는 개념적인 “destroy phase”이다

스프링 컨테이너는 종료될 때 아래처럼 “소멸 단계(destroy phase)”를 수행한다.

1. @PreDestroy 호출
2. DisposableBean.destroy() 호출
3. @Bean(destroyMethod="…") 지정 메서드 호출
4. (스프링 부트의 경우) 종료 훅 등록된 close() 호출

따라서 소멸 단계는 여러 콜백들의 집합이다.

즉, destroy라는 단어는 단순히 소멸 과정을 총칭하는 단어이다.

---

# 2. destroy()는 여러 소멸 콜백 중 하나일 뿐

스프링이 지원하는 소멸 콜백은 총 3가지다.

## 1) @PreDestroy

가장 권장되는 표준 방식

## 2) DisposableBean.destroy()

스프링 전용 인터페이스 (비권장)

## 3) @Bean(destroyMethod="…")

외부 라이브러리 close() 등을 호출하기 위해 자주 쓰임
스프링 부트는 기본적으로 close(), shutdown()을 자동 인식한다.

이 3가지가 모두 “소멸 단계”에 해당한다.

따라서 소멸 단계 = destroy() 라고 볼 수 없다.
destroy()는 그중 하나일 뿐이다.

---

# 3. ApplicationContext로는 소멸 단계 자체가 실행되지 않는다

여기서 중요한 건:

* 소멸 단계는 close()나 shutdownHook에 의해 시작된다.
* ApplicationContext 타입으로는 close()를 호출할 수 없다.
* 그래서 destroy()뿐 아니라 @PreDestroy 등 “모든 소멸 단계 콜백이” 실행되지 않는다.

즉,

> ApplicationContext에서 destroy()가 실행되지 않는다는 말은
> 소멸 단계 자체가 시작되지 않았다는 뜻이다.

destroy 메서드만 문제가 있는 것이 아니라,
소멸 단계가 아예 시작되지 않은 것.

---

# 4. 정리

## 소멸 단계가 의미하는 것

* 소멸 단계(destroy phase) =
  @PreDestroy, DisposableBean.destroy(), @Bean(destroyMethod) 등을 호출하는 과정 전체

## destroy()는 무엇인가

* 소멸 단계에서 실행되는 “여러 콜백 중 하나”
* 스프링 전용 인터페이스 방식
* 필수도 아니고 권장 방식도 아님

## ApplicationContext에서는 왜 destroy가 실행되지 않는가

* close() 기능이 없어서 소멸 단계 자체가 시작되지 않는다.
* 따라서 destroy(), @PreDestroy 등 모든 소멸 콜백이 실행되지 않는다.

---

필요하다면

* 스프링이 소멸 순서를 어떻게 결정하는지
* BeanFactory와 ApplicationContext가 소멸을 어떻게 처리하는지
* 스프링 부트에서 왜 자동으로 destroy가 실행되는지

이런 것도 이어서 톺아줄게.

“외부 라이브러리에는 @PostConstruct, @PreDestroy를 쓸 수 없다”는 말은
내가 작성한 클래스가 아닌, 이미 만들어져서 제공되는 타사 라이브러리 코드에는 이 어노테이션을 붙일 수 없다는 의미다.

이걸 정확하게 이해하려면 상황을 나눠보면 아주 명확해진다.

---

# 1. @PostConstruct / @PreDestroy는 “내가 직접 작성한 클래스에만” 붙일 수 있다

예를 들어, 다음처럼 내가 만든 클래스에는 붙일 수 있다.

```java
public class NetworkClient {
    @PostConstruct
    public void init() { ... }

    @PreDestroy
    public void close() { ... }
}
```

이건 내가 코드 파일을 수정할 수 있으니까 가능하다.

---

# 2. 하지만 외부 라이브러리 클래스에는 붙일 수 없다

예를 들어, 아래와 같은 외부 라이브러리 클래스라면?

* HikariDataSource
* Tomcat Connection Pool
* Netty Client
* AWS SDK Client
* JDBC Driver
* Apache HttpClient

이런 클래스들은 다음과 같은 특징이 있다.

* 소스코드를 내가 가진 게 아니다
* 수정할 수도 없다
* 어노테이션을 추가하려면 직접 파일을 열어서 붙여야 하는데 그게 불가능하다

즉, 다음은 불가능하다.

```java
public class HikariDataSource {
    @PreDestroy    // 불가능
    public void shutdown() { ... }
}
```

외부 라이브러리 객체는 이렇게 어노테이션을 붙일 수 없으므로
@PostConstruct, @PreDestroy 방식으로 초기화/종료 콜백을 등록할 수 없다.

---

# 3. 그러면 외부 라이브러리의 초기화/종료는 어떻게 처리하나?

스프링이 공식적으로 권장하는 방식은 @Bean(initMethod, destroyMethod) 또는 자동 추론 기능이다.

예:

```java
@Bean(destroyMethod = "shutdown")
public HikariDataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setUrl(...);
    return ds;
}
```

이렇게 하면:

* 클래스는 수정하지 않아도 되고
* close(), shutdown() 같은 메서드를 스프링이 종료 시점에 호출해준다
* 스프링에 종속적이지도 않다 (라이브러리 자체는 스프링을 모름)

즉, 외부 라이브러리에서 가장 자주 쓰는 방법이 바로 이거다.

---

# 4. @PostConstruct / @PreDestroy는 “내 코드에만” 의미가 있다

스프링이 초기화/종료 콜백을 호출하려면
그 메서드와 어노테이션이 빈 클래스에 직접 있어야 한다.
하지만 외부 라이브러리는 다음 문제 때문에 불가능하다.

* 외부 라이브러리 코드는 수정할 수 없다
* 어노테이션을 추가하려면 클래스 자체를 바꿔야 한다
* 외부 코드에 스프링 어노테이션을 붙이는 것은 비현실적이고 잘못된 설계다

따라서 외부 라이브러리에는 @PostConstruct, @PreDestroy를 적용할 수 없다는 의미다.

---

# 5. 정리

| 방식                               | 자체 클래스 | 외부 라이브러리       |
| -------------------------------- | ------ | -------------- |
| @PostConstruct                   | 가능     | 불가능            |
| @PreDestroy                      | 가능     | 불가능            |
| @Bean(initMethod, destroyMethod) | 가능     | 가능             |
| DisposableBean/InitializingBean  | 가능     | 외부 라이브러리에는 불가능 |

이 말은 곧,

> 외부 라이브러리를 빈으로 사용할 때는 반드시 initMethod / destroyMethod 같은 “설정 기반” 초기화/종료 방식을 사용해야 한다.

---

원한다면

* 스프링이 close(), shutdown() 메서드를 자동으로 추론하는 원리
* HikariCP가 @PreDestroy 없이도 정상 종료되는 이유
* @Configuration CGLIB 프록시와 initMethod 호출 과정
  도 자세히 설명해줄게.
