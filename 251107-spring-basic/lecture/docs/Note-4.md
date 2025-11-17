아래 코드는 Spring의 컴포넌트 스캔 메커니즘, @Configuration 클래스의 역할, CGLIB 프록시 생성 과정, 중복 빈 등록 문제, 순환 구성 문제가 얽혀 있어서 이렇게 작성한다.
각 요소를 Spring 내부 동작 원리 기준으로 단계별로 설명하겠다.

---

# 1. 코드 분석

```java
@Configuration
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION, classes = Configuration.class
        )
)
public class AutoAppConfig {

}
```

이 클래스는 다음 두 기능을 동시에 한다.

1. @Configuration: 이 클래스가 Java 기반 설정 클래스임을 의미하며, 여기에 등록된 @Bean 메서드는 CGLIB을 통해 싱글톤을 보장한다.
2. @ComponentScan: 특정 패키지 이하의 컴포넌트를 자동으로 스캔해 빈으로 등록한다.

그런데 컴포넌트 스캔을 수행하면서 @Configuration 클래스들은 스캔 대상에서 제외한다고 명시해놓았다.
이게 바로 핵심이다.

---

# 2. 왜 @Configuration을 스캔에서 제외해야 하는가?

## 2-1. @Configuration 클래스는 원래 컴포넌트 스캔 대상이다

Spring은 다음 애노테이션을 가진 클래스들을 컴포넌트로 본다.

* @Component
* @Service
* @Repository
* @Controller
* @Configuration (여기엔 @Component 포함)

즉, 별도로 excludeFilters를 하지 않으면 아래 두 경로로 중복 등록될 가능성이 있다.

* 직접 등록: `new AnnotationConfigApplicationContext(AutoAppConfig.class)`
* 스캔되어 등록: 컴포넌트 스캔이 @Configuration 클래스를 발견해 빈으로 등록하려고 시도

이로 인해 두 번 등록하려는 충돌 상황이 발생할 수 있다.

---

# 3. 내부 동작 이해: @Configuration의 CGLIB 프록시 문제

Spring은 @Configuration 클래스에 대해 다음을 수행한다.

1. 등록된 @Configuration 클래스의 바이트코드를 읽는다.
2. CGLIB으로 프록시 클래스를 만든다.
3. @Bean 메서드가 호출될 때 항상 싱글톤이 보장되도록 메서드 호출을 가로채 캐시된 빈을 반환한다.

예시:

```java
@Bean
public MemberService memberService() {
    return new MemberServiceImpl();  // 여러 번 호출해도 항상 같은 객체 반환
}
```

이건 @Configuration이 proxyBeanMethods = true (default) 이기 때문에 가능한 동작이다.

### 컴포넌트 스캔에 의해 @Configuration이 두 번 등록되면?

프록시 구조가 깨지고 이런 일이 일어난다.

* 프록시가 아닌 원본 클래스가 빈으로 등록
* CGLIB이 생성한 프록시도 등록될 수 있음
* @Bean 메서드 호출 시 싱글톤 보장이 깨질 위험
* “이미 동일 이름의 빈이 존재합니다” 오류 발생 가능

따라서 스프링에서는 수동 등록된 @Configuration은 스캔 대상에서 반드시 제외해야 한다.

---

# 4. 컴포넌트 스캔은 어떤 기준으로 빈을 찾는가?

@ComponentScan은 다음 과정을 거친다.

1. 지정 패키지의 클래스 파일을 스캔
2. ASM으로 클래스 메타데이터 읽기
3. @Component 계열 애노테이션이 붙은 클래스만 후보로 선정
4. 빈 정의(BeanDefinition) 생성
5. BeanFactory에 등록

여기서 @Configuration은 @Component이므로 원래는 3번에 포함된다.

### 따라서 excludeFilters가 없으면

@ComponentScan이 자기가 속한 @Configuration 클래스까지 빈으로 등록하려고 시도한다.

---

# 5. 결론: 왜 이렇게 작성하는가?

정리하면 다음 이유 때문이다.

---

## 이유 1. 설정 클래스(@Configuration) 중복 등록 방지

수동으로 등록되는 설정 클래스가 컴포넌트 스캔 대상에 포함되면
같은 클래스가 두 번 빈 정의로 등록되며 충돌이 발생할 수 있다.

---

## 이유 2. @Bean 싱글톤 보장을 위한 CGLIB 프록시 유지

@Configuration 클래스는 프록시를 통해 @Bean 메서드의 싱글톤 동작을 보장하는데,
스캔으로 인해 원본 클래스가 등록되면 프록시 동작이 깨진다.

---

## 이유 3. 컴포넌트 스캔의 의도된 대상은 “애플리케이션 로직"

설정 파일은 “로직"이 아니라 “설정"이며 스캔 대상이 아니다.
설정 클래스를 스캔하면 개발자가 의도하지 않은 빈 충돌이 일어날 수 있다.

---

## 이유 4. Spring Boot와 동일한 패턴

Spring Boot도 설정 클래스들의 중복 등록을 막기 위해
@Configuration 클래스는 컴포넌트 스캔의 기본 대상에서 제외하는 패턴을 사용한다.

---

# 최종 요약

위 코드는 컴포넌트 스캔을 실행할 때
“현재 설정 클래스(@Configuration)는 스캔하지 마라”
라는 의미이며, 그 이유는 다음과 같다.

1. 설정 클래스 중복 등록 방지
2. @Bean 싱글톤 보장을 위한 CGLIB 프록시 보호
3. 설정 파일은 스캔 대상이 아님을 명확히 하기 위함
4. Spring Boot 기본 설계와 동일한 원칙

---

원리까지 포함해 더 깊은 수준에서 설명해줄 수도 있다.
예를 들어 BeanDefinitionRegistry에 어떻게 등록되는지, ClassPathScanningCandidateComponentProvider가 어떻게 클래스 메타데이터를 읽는지, ConfigurationClassPostProcessor가 어떤 순서로 어노테이션을 처리하는지 등도 설명할 수 있다.
필요하면 알려달라.

하나씩 차근히 풀어서 설명해볼게.

---

# 1. `FilterType.ANNOTATION`은 enum 타입이다

Spring에서 `@ComponentScan.Filter`를 구성할 때
스캔 제외/포함 규칙을 어떤 기준(type)으로 적용할 것인지 지정하는 enum이 바로 `FilterType`이다.

`FilterType`은 다음 5가지 값을 가진 enum이다.

```java
public enum FilterType {
    ANNOTATION,    // 특정 애노테이션이 붙은 클래스를 대상으로 필터링
    ASSIGNABLE_TYPE, // 특정 타입(클래스/인터페이스)을 상속/구현한 클래스 대상
    ASPECTJ,       // AspectJ 표현식 기반
    REGEX,         // 정규식 기반 클래스 이름 매칭
    CUSTOM         // 개발자가 직접 만든 필터 사용
}
```

그러므로 `FilterType.ANNOTATION`은
애노테이션 기반으로 스캔 제외 대상을 지정하겠다는 뜻이다.

---

# 2. `classes = Configuration.class`의 의미

이건 필터링의 “기준 대상"을 설정하는 부분이다.

* `classes`에는 대상 애노테이션 또는 클래스 타입이 들어간다.
* 우리가 `type = ANNOTATION`을 썼기 때문에
  `classes` 안에 제외할 애노테이션을 넣어야 한다.

여기서는 `Configuration.class`이므로
@Configuration 애노테이션이 붙은 모든 클래스를 스캔에서 제외해라
라는 의미가 된다.

---

# 3. 전체 문장은 이렇게 작동한다

```java
excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = Configuration.class
)
```

해석하면 이렇게 된다.

> 컴포넌트 스캔을 수행할 때
> @Configuration 애노테이션이 붙은 클래스들은 스캔 대상으로 포함하지 마라.

---

# 4. 왜 `classes = Configuration.class`라고 적히나?

애노테이션 클래스도 결국 `.class` 파일을 가진 Class 타입이다.

`@Configuration`이라는 애노테이션은 런타임에 아래와 같은 Class 객체로 존재한다.

```java
@Configuration → org.springframework.context.annotation.Configuration
```

따라서 다음 두 표현 모두 같은 타입을 나타낸다.

* `@Configuration`
* `Configuration.class`

애노테이션 기반 필터링에서는
그 애노테이션의 Class 객체를 지정해야 하기 때문에 `.class`로 표기하는 것이다.

---

# 5. 결론 요약

| 부분                              | 의미                                      |
| ------------------------------- | --------------------------------------- |
| `type = FilterType.ANNOTATION`  | 애노테이션 기반 필터링을 하겠다는 뜻 (enum 값)           |
| `classes = Configuration.class` | @Configuration이 붙은 클래스를 대상으로 필터링하겠다는 의미 |
| 전체                              | @Configuration을 가진 클래스를 컴포넌트 스캔에서 제외    |

---

# 더 원리적인 설명도 가능하다

예를 들어:

* `ClassPathScanningCandidateComponentProvider`가 어떻게 클래스를 스캔하고
  `MetadataReader`로 bytecode 레벨에서 애노테이션 정보를 읽는지
* excludeFilters가 BeanDefinition 후보 단계에서 어떻게 제거되는지
* 왜 Annotation은 Class 타입이 될 수 있는지

이런 내부 구조까지 깊게 설명해줄 수도 있다. 원하면 더 자세히 알려줄게.

결론부터 말하면
둘은 “형식적으로는 같은 애노테이션이지만, 역할과 처리 주체가 다르다.”

즉,

* 자바 표준 애노테이션(@Override, @FunctionalInterface, @Deprecated 등)은 JDK가 처리한다.
* 스프링 애노테이션(@Component, @Configuration 등)은 스프링 프레임워크가 처리한다.

둘 다 문법적으로는 같은 애노테이션 시스템 위에서 작동하지만,
누가 해석하고 어떤 동작을 만들어내는지가 완전히 다르다.

아래에서 정리해줄게.

---

# 1. 둘은 동일한 자바 애노테이션 문법을 사용한다

자바 애노테이션은 다음과 같은 문법과 메타데이터 구조를 가진다.

* `@interface`로 정의
* `.class` 파일에 컴파일되어 메타데이터로 포함
* RetentionPolicy에 따라 SOURCE / CLASS / RUNTIME 레벨로 유지

스프링 애노테이션도 이 문법을 사용한다.

Spring의 @Component도 이렇게 정의되어 있다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component { }
```

즉, 문법적으로는 완전히 동일한 자바 애노테이션이다.

---

# 2. 하지만 “누가 해석하는가”가 다르다

## 자바 표준 애노테이션

자바 컴파일러나 JVM이 직접 처리한다.

| 애노테이션                  | 처리 주체    | 동작           |
| ---------------------- | -------- | ------------ |
| `@Override`            | 컴파일러     | 메서드 오버라이딩 체크 |
| `@Deprecated`          | 컴파일러/JVM | 사용 시 경고 출력   |
| `@SuppressWarnings`    | 컴파일러     | 경고 숨김        |
| `@FunctionalInterface` | 컴파일러     | 메서드가 1개인지 검사 |

즉, 이들은 컴파일러 레벨에서 의미가 있는 애노테이션이다.

---

## Spring 애노테이션

스프링 컨테이너가 런타임에 해석한다.

| 애노테이션             | 처리 주체                                      | 실제 동작                |
| ----------------- | ------------------------------------------ | -------------------- |
| `@Component`      | ComponentScan                              | 빈으로 등록               |
| `@Autowired`      | BeanFactory / AutowiredAnnotationProcessor | 의존성 주입               |
| `@Configuration`  | ConfigurationClassPostProcessor            | @Bean 메서드 처리, 프록시 생성 |
| `@Transactional`  | AOP 프록시                                    | 트랜잭션 시작/커밋/롤백        |
| `@RestController` | Spring MVC                                 | HTTP 요청 처리 핸들러 등록    |

Spring 애노테이션은 스프링 컨테이너가 런타임에 리플렉션으로 읽고 동작을 유발한다.

---

# 3. 애노테이션은 같은데 동작이 다른 이유

애노테이션 자체는 단순한 메타데이터이다.
애노테이션이 뭔가를 “실행”하지 않는다.

대신 다음과 같은 흐름으로 작동한다.

1. 애노테이션은 클래스의 메타데이터로 남고
2. 어떤 프레임워크(Spring, JPA, Lombok 등)가
3. 이 메타데이터를
4. 특정한 방식으로 해석해서
5. 실제 동작을 만들어낸다

애노테이션 = 정보
프레임워크 = 행동

이 관계를 이해하는 것이 핵심이다.

---

# 4. 예시 비교

## @Override는 자바 컴파일러가 처리

```java
@Override
public void run() { }
```

컴파일러가 “부모 메서드가 진짜 있는지” 검사하고 없는 경우 에러를 낸다.

---

## @Component는 스프링이 처리

```java
@Component
public class MemberService { }
```

Spring이 실행될 때:

1. ClassPathScanner가 @Component가 붙은 클래스를 찾고
2. BeanDefinition을 만들고
3. 스프링 컨테이너에 등록

자바 자체는 이 애노테이션을 보고 아무 일도 하지 않는다.

---

# 5. 한 문장으로 정리

> 애노테이션의 모양은 같지만, 의미와 동작을 만드는 주체가 다르다.
> 자바 표준 애노테이션은 컴파일러/JVM이,
> 스프링 애노테이션은 스프링 프레임워크가 해석한다.

---

원하면

* JPA(@Entity)
* Lombok(@Getter, @Builder)
* Jackson(@JsonProperty)

같은 애노테이션들은 어떻게 동작하는지도 원리부터 설명해줄 수 있다.

결론부터 말하면 같은 개념을 다른 관점에서 표현한 것이다.
하지만 문장을 그대로 1:1로 대응시키면 약간 의미가 다르다.
둘의 차이를 분명하게 구분해줄게.

---

# 1. `Configuration.class`의 정확한 의미

코드에서 `Configuration.class`는 자바의 Class 객체를 의미한다.

즉,

```java
@Configuration
public class Foo { }
```

이 애노테이션을 정의한 타입을 가리키는 Class 타입의 리터럴이다.

말하자면 “@Configuration 애노테이션 자체를 나타내는 Class 객체”다.

---

# 2. “`.class` 파일에 컴파일되어 메타데이터로 포함”이라는 말의 의미

이건 애노테이션이 실제로 어떻게 저장되느냐에 대한 설명이다.

자바 클래스는 컴파일되면 다음 구조로 저장된다.

* bytecode
* constant pool
* 클래스 정보
* 필드/메서드 정보
* 애노테이션 정보(메타데이터)

예를 들어:

```java
@Component
public class Hello { }
```

이 클래스는 컴파일되면 `Hello.class` 파일 내부에
“이 클래스 위에 @Component가 붙어있음”이라는 정보가 메타데이터로 포함된다.

즉, 이 문장은

> 애노테이션은 `.class` 파일(바이트코드)에 메타데이터로 저장된다

라는 의미이다.

---

# 3. 둘의 관계 정리

| 표현                               | 의미                               | 성격                  |
| -------------------------------- | -------------------------------- | ------------------- |
| `Configuration.class`            | @Configuration 애노테이션의 “Class 객체” | 개발자가 코드에서 직접 사용하는 값 |
| “애노테이션이 `.class` 파일 메타데이터로 포함된다” | 애노테이션이 실제 클래스 파일 내부에 저장되는 방식     | 기술적 설명, 바이트코드 레벨    |

즉,

* `Configuration.class`는 타입 정보
* “.class 파일에 메타데이터로 포함”은 컴파일 결과물에 저장되는 방식

두 문장은 동일한 대상(애노테이션)을 가리키지만 서로 다른 층위의 설명이다.

---

# 4. 비유로 쉽게 설명하면

* `Configuration.class` : “이 애노테이션의 설계도 원본”을 코드에서 가리키는 것
* “클래스 파일에 메타데이터로 저장된다” : “저 설계도를 사용했다고 기록된 흔적이 .class 안에 들어간다”는 뜻

---

# 5. 결론

두 문장은 같은 개념을 이야기하지만, 같은 의미는 아니다.

* `Configuration.class`는 애노테이션 타입을 지칭
* “.class 파일의 메타데이터”는 애노테이션이 클래스 파일 내부에 기록되는 방식

그래서 서로 연관은 있지만, 표현 자체는 다른 의미다.

---

필요하다면 `.class` 파일 구조(바이트코드 레벨에서 애노테이션이 어떻게 저장되는지)도 예시로 보여줄 수 있다.

정확히 이해하고 있다.
하지만 더 명확히 하자면 다음과 같은 구조다.

---

# 1. `Override.class`는 실제로 존재한다

자바의 모든 애노테이션은 결국 하나의 클래스다.

예를 들어 JDK 내부에는 이런 파일이 있다.

```
java/lang/Override.class
```

내용은 이런 식으로 정의된다.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override { }
```

즉, `@Override`는 자바가 제공하는 애노테이션 클래스이며
그 컴파일된 형태가 `Override.class`이다.

---

# 2. @Override를 코드에 작성하면, 컴파일러가 그 정보를 클래스에 기록한다

예를 들어:

```java
public class Child extends Parent {
    @Override
    public void run() { }
}
```

이렇게 하면 컴파일러는 다음 두 가지를 한다.

## (1) 컴파일 시 @Override 규칙을 검사

* 부모 메서드를 실제로 오버라이드한 것인지 확인
* 아니면 컴파일 오류 발생

## (2) 그 정보가 `.class` 파일에 “메타데이터”로 기록될 수도 있다

하지만 `@Override`는 `RetentionPolicy.SOURCE`이다.

정확한 의미는:

* 컴파일 시점에는 존재한다.
* .class 파일에는 기록되지 않는다.
* 런타임에도 남지 않는다.

즉, `@Override`는 SOURCE 레벨 애노테이션이라
클래스 파일의 메타데이터에는 포함되지 않는다.

---

# 3. 용어 정리: “메타데이터로 포함된다”는 말의 정확한 범위

애노테이션마다 “정보가 어디까지 남는지”가 달라진다.

| RetentionPolicy | 어디까지 남는가                          | 예                            |
| --------------- | --------------------------------- | ---------------------------- |
| SOURCE          | 컴파일러까지만, 클래스 파일에는 없음              | @Override, @SuppressWarnings |
| CLASS           | 클래스 파일에는 있음, 런타임에는 없음             | 일부 기본 애노테이션                  |
| RUNTIME         | 클래스 파일에도 있고, 런타임에 Reflection으로 읽힘 | @Component, @Configuration   |

스프링이 RUNTIME 애노테이션만 처리할 수 있는 이유도 이것이다.
런타임에 Reflection으로 읽어야 하기 때문이다.

---

# 4. 결론

당신이 말한 문장은 상황별로 다음처럼 정리된다.

1. Override.class는 실제로 존재한다.
2. 하지만 `@Override`는 SOURCE 수준 애노테이션이므로
   @Override를 적은 클래스의 .class 파일에는 Override 정보를 남기지 않는다.

즉,

> “Override.class는 존재하지만, @Override를 적은 클래스의 .class 파일에는 그 정보가 저장되지 않는다.”

반대로 스프링 애노테이션(@Component, @Configuration)은 `RetentionPolicy.RUNTIME`이기 때문에

> .class 파일에 메타데이터가 남고
> 런타임에 스프링이 Reflection으로 읽고 사용한다.

---

원하면

* RetentionPolicy마다 .class 파일에 실제로 어떤 bytecode로 기록되는지
* javap로 디컴파일해서 애노테이션 정보가 어떻게 들어 있는지
  직접 보여줄 수도 있다.
