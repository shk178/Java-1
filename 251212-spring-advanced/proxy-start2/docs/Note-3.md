좋은 타이밍의 질문이야.
**빈 후처리기(BeanPostProcessor)** 는 지금까지 이야기한 **프록시 / AOP가 “언제, 어떻게 끼어드는가”**를 이해하는 데 핵심이다.
개념 → 실행 시점 → 프록시와의 관계 → 실무 예제 순서로 설명할게.

---

## 1. 빈 후처리기란 무엇인가

### 한 줄 정의

> **BeanPostProcessor는
> 스프링이 빈을 생성한 “직후”에
> 그 빈을 가로채서 수정·교체할 수 있게 해주는 확장 지점이다.**

핵심 포인트:

* 스프링 컨테이너 내부 훅(hook)
* **빈 인스턴스 자체를 바꿀 수 있음**
* 프록시 생성의 출발점

---

## 2. 왜 “후처리기”인가 (Lifecycle 위치)

스프링의 빈 생성 흐름을 단순화하면:

```text
1. 객체 생성 (new)
2. 의존성 주입
3. @PostConstruct
4. BeanPostProcessor.beforeInitialization
5. 초기화 메서드
6. BeanPostProcessor.afterInitialization
7. 컨테이너 등록
```

### 핵심 포인트

* **BeanPostProcessor는 모든 빈에 대해 호출**
* 특히 **afterInitialization** 단계에서

    * 원본 빈 대신 **프록시를 반환**할 수 있음

---

## 3. BeanPostProcessor 인터페이스

```java
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean, String beanName);

    Object postProcessAfterInitialization(Object bean, String beanName);
}
```

중요:

* **반환값이 다음 단계로 전달됨**
* `bean` 그대로 돌려줘도 되고
* **완전히 다른 객체(프록시)** 를 돌려줘도 된다

---

## 4. 프록시는 여기서 만들어진다 (아주 중요)

> **Spring AOP의 프록시는
> BeanPostProcessor 안에서 생성된다.**

대표적인 구현체:

* `AbstractAutoProxyCreator`
* `AnnotationAwareAspectJAutoProxyCreator`

이 클래스들이 하는 일은 개념적으로 이거다:

```java
if (이 빈에 적용할 Advisor가 있으면) {
    프록시 생성
    return 프록시;
}
return 원본 빈;
```

즉:

* AOP는 “런타임 마법”이 아니라
* **빈 등록 시점에 객체를 바꿔치기**하는 구조다

---

## 5. 그래서 이런 현상이 생긴다

### 1) 컨테이너 밖에서 new 하면 AOP 안 됨

```java
new OrderServiceImpl(); // 프록시 안 됨
```

이유:

* BeanPostProcessor는 **컨테이너 관리 빈만 처리**

---

### 2) self-invocation 문제

```java
this.save();
```

* 프록시는 **외부 호출만 가로챔**
* 내부 호출은 이미 프록시를 벗어난 상태

---

### 3) 빈 타입이 달라지는 이유

```java
OrderService service = context.getBean(OrderService.class);
```

실제 객체:

```text
$ProxyXX or OrderService$$EnhancerBySpringCGLIB
```

원본 빈:

```text
OrderServiceImpl
```

이게 가능한 이유가:

* BeanPostProcessor가 **빈을 교체**했기 때문

---

## 6. 간단한 커스텀 BeanPostProcessor 예제

### 특정 빈을 프록시로 감싸기

```java
@Component
public class SimpleProxyPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (!(bean instanceof OrderService)) {
            return bean;
        }

        ProxyFactory factory = new ProxyFactory(bean);
        factory.addAdvice((MethodInterceptor) invocation -> {
            System.out.println("before");
            Object result = invocation.proceed();
            System.out.println("after");
            return result;
        });

        return factory.getProxy();
    }
}
```

이 코드의 의미:

* OrderService 빈만 골라서
* ProxyFactory로 프록시 생성
* 원본 빈 대신 프록시를 컨테이너에 등록

---

## 7. BeanPostProcessor와 ProxyFactory의 관계

| 역할                | 위치               |
| ----------------- | ---------------- |
| BeanPostProcessor | **언제** 개입할지      |
| ProxyFactory      | **어떻게** 프록시를 만들지 |
| Advisor / Advice  | **무엇을** 적용할지     |

Spring AOP는 이 셋을 조합한 결과다.

---

## 8. 왜 BeanPostProcessor가 이렇게 강력한가

* 모든 빈 생성에 개입 가능
* 프록시, 데코레이터, 래퍼 패턴 구현 가능
* AOP, @Transactional, @Async 전부 이 위에서 동작

그래서:

> **스프링 확장의 최상위 훅 중 하나**

---

## 9. 실무에서의 주의점

1. **모든 빈에 호출됨**

    * 무거운 로직 금지
2. 순서 중요

    * `Ordered` / `PriorityOrdered`
3. 상태 저장 금지

    * 싱글톤, 멀티스레드
4. 디버깅 난이도 높음

    * 로그 없으면 추적 힘듦

---

## 10. 한 문장으로 정리

> **BeanPostProcessor는
> 스프링이 빈을 컨테이너에 등록하기 직전에
> 객체 자체를 바꿀 수 있는 확장 지점이며,
> Spring AOP 프록시는 바로 여기서 만들어진다.**

---

이제 흐름이 이렇게 연결될 거야:

```
Bean 생성
 → BeanPostProcessor
   → 프록시 생성
     → InvocationHandler / MethodInterceptor
       → Advice 체인
```

다음으로 이어서 보면 딱 좋은 주제는:

* BeanFactoryPostProcessor와의 차이
* AutoProxyCreator 내부 동작
* @Transactional이 언제 프록시로 바뀌는지
* 빈 후처리기 순서가 꼬이면 생기는 문제

어디까지 더 볼지 말해줘.

좋은 질문이야.
이 로그를 이해하면 **스프링 로그 + 테스트 실행 환경**이 한 번에 정리된다.

---

## 결론부터

```
18:13:23.349 [Test worker] DEBUG ...
```

여기서

> **`[Test worker]`는
> 이 로그를 출력한 “스레드 이름(thread name)”이다.**

스프링 전용 개념도 아니고, BeanPostProcessor와 직접적인 관련도 없다.

---

## 로그 한 줄의 구조부터 해석해 보자

예시:

```
18:13:23.349 [Test worker] DEBUG org.springframework.context.annotation.AnnotationConfigApplicationContext - Refreshing ...
```

이건 일반적인 **SLF4J / Logback 로그 포맷**이다.

### 구성 요소 분해

| 부분                               | 의미                  |
| -------------------------------- | ------------------- |
| `18:13:23.349`                   | 로그 출력 시각            |
| `[Test worker]`                  | **현재 실행 중인 스레드 이름** |
| `DEBUG`                          | 로그 레벨               |
| `org.springframework.context...` | 로그를 남긴 클래스          |
| 메시지                              | 실제 로그 내용            |

---

## 그럼 `Test worker`는 누구인가?

### JUnit 테스트 실행 환경에서 만들어진 스레드 이름이다.

지금 테스트 코드:

```java
@Test
void test() {
    ApplicationContext ac =
        new AnnotationConfigApplicationContext(BPConfig.class);
    ...
}
```

이 코드는:

* JUnit이 만든 **테스트 실행용 스레드**에서 실행되고
* 그 스레드 이름이 `Test worker`로 설정돼 있다

---

## 왜 이름이 `Test worker`일까?

이건 **JUnit + 빌드 도구(Gradle / IntelliJ)** 조합에서 흔히 보인다.

### 보통 이런 규칙이다

* 테스트 실행 전용 스레드 풀 생성
* 각 스레드에 이름 부여

    * `Test worker`
    * `Test worker @coroutine#1` 같은 변형도 있음

즉:

> **“이 로그는 테스트 스레드에서 찍힌 것이다”**
> 라는 정보만 담고 있다.

---

## 중요한 점: BeanPostProcessor와는 무관

아래 로그들:

```
[Test worker] DEBUG DefaultListableBeanFactory - Creating shared instance of singleton bean 'one'
```

이 의미는:

* `Test worker` 스레드에서
* 스프링 컨테이너 초기화 중
* `one` 빈을 생성하고 있다

이지,

* BeanPostProcessor가 다른 스레드에서 동작한다
* 프록시가 멀티스레드다

이런 뜻은 **전혀 아니다**.

---

## 네 코드 흐름과 정확히 연결해 보면

### 실행 흐름

1. JUnit이 `Test worker` 스레드 생성
2. `test()` 메서드 실행
3. `new AnnotationConfigApplicationContext(...)`
4. 스프링 컨테이너 초기화
5. 빈 생성 & BeanPostProcessor 호출
6. 로그 출력 (모두 같은 스레드)

그래서 로그 전부에:

```
[Test worker]
```

가 붙어 있는 게 정상이다.

---

## 멀티스레드일 때는 어떻게 보일까?

예를 들어 비동기 실행이면:

```
[Test worker] ...
[task-1] ...
[http-nio-8080-exec-1] ...
```

이렇게 섞여 나온다.

그래서 로그의 `[스레드 이름]`은:

> **“이 코드가 어떤 실행 맥락에서 돌아가고 있나”**
> 를 파악하는 힌트다.

---

## 정리 한 문장

> **`[Test worker]`는
> JUnit 테스트를 실행하는 스레드 이름이며,
> 스프링이나 BeanPostProcessor의 동작 의미와는 직접적인 관련은 없다.**

---

지금 이 로그를 읽고 질문한 건
이미 **스프링 내부 동작을 로그로 추적하는 단계**에 들어갔다는 뜻이야.

다음으로 이어서 보면 딱 좋은 주제는:

* BeanPostProcessorChecker 로그의 정확한 의미
* “not eligible for getting processed” 경고가 왜 뜨는지
* 왜 BPConfig가 프록시 대상이 아닌지
* 빈 생성 순서가 바뀌면 어떤 문제가 생기는지

이 메시지는 처음 보면 헷갈리지만, **오류도 아니고 문제 상황도 아니다.**
정확한 의미를 문장 단위로 풀어줄게.

---

## 원문 로그 다시 보기

```
Bean 'BPConfig' of type
[hello.proxy.beanprocess.BPConfig$$EnhancerBySpringCGLIB$$ae9ea7d4]
is not eligible for getting processed by all BeanPostProcessors
(for example: not eligible for auto-proxying)
```

---

## 한 줄 요약 (먼저 결론)

> **`BPConfig` 빈은 “너무 이른 시점에 생성되었기 때문에”
> 모든 BeanPostProcessor를 적용받을 수 없고,
> 특히 자동 프록시(AOP) 대상이 될 수 없다는 뜻이다.**

---

## 이 로그가 왜 나오는가 (핵심 배경)

### 핵심 키워드

* **BeanPostProcessor**
* **auto-proxying**
* **너무 이른 시점(early initialization)**

스프링은 다음 두 단계를 거친다:

1. **BeanPostProcessor 자신들을 먼저 등록**
2. 그 다음에 **일반 빈들에 BeanPostProcessor를 적용**

그런데 문제는:

> **BeanPostProcessor를 등록하는 과정에서
> 일부 빈이 “먼저 생성”될 수 있다**

바로 그 빈이 지금의 `BPConfig`다.

---

## 왜 BPConfig가 먼저 생성됐을까?

네 설정을 보자:

```java
@Configuration
public class BPConfig {

    @Bean
    public One one() {
        return new One();
    }

    @Bean
    public BP bP() {
        return new BP();
    }
}
```

여기서 중요한 점:

* `BP`는 **BeanPostProcessor**
* BeanPostProcessor는 **다른 빈들보다 먼저 생성·등록**되어야 함
* 그러기 위해 스프링은 **`BPConfig`를 먼저 인스턴스화**한다

즉 순서가 이렇게 된다:

```
1. BPConfig 생성 (CGLIB 프록시로)
2. BP 빈 생성 (BeanPostProcessor)
3. 그 다음부터 일반 빈들 생성
```

---

## 그런데 왜 “모든 BeanPostProcessor를 적용 못 한다”고 하나?

### 이유는 단순하다

> **`BPConfig`가 생성될 시점에는
> 아직 “모든” BeanPostProcessor가 준비되지 않았다**

그래서 스프링이 말하는 것:

> “이 빈은
> BeanPostProcessor 등록 단계 중에 만들어졌기 때문에
> 모든 후처리를 다 거칠 수는 없다”

---

## 특히 “auto-proxying”이 안 된다는 말의 의미

### auto-proxying이란?

* `@Transactional`
* `@Aspect`
* AOP 자동 프록시 생성

이건 전부 **BeanPostProcessor 기반**이다
(대표적으로 `AbstractAutoProxyCreator`).

그런데 `BPConfig`는:

* **그 AutoProxyCreator 자체를 등록하는 과정에서 생성됨**
* 자기 자신에게 AOP를 적용할 수 없음

그래서 로그에 이렇게 예를 든다:

```
(for example: not eligible for auto-proxying)
```

뜻:

> “예를 들면 AOP 프록시 같은 건 안 걸린다”

---

## 이게 문제인가?

### ❌ 전혀 아니다

이 로그는:

* 경고(warn)도 아니고
* 에러(error)도 아니고
* 디버그/정보성 메시지다

스프링이 그냥 친절하게 알려주는 것:

> “이 빈은 컨테이너 내부 준비 과정에서 만들어진 특수한 빈이다”

---

## 왜 하필 BPConfig만 나오나?

다른 빈들은 로그가 안 뜨는데
`BPConfig`만 뜨는 이유:

* `@Configuration` 클래스
* BeanPostProcessor를 정의함
* 컨테이너 부트스트랩에 관여

즉 **“인프라 빈”**이기 때문이다.

---

## 네 테스트 흐름과 정확히 연결하면

1. 테스트 시작 (`Test worker`)
2. ApplicationContext 생성
3. BeanPostProcessor 등록 필요
4. → BPConfig 먼저 생성
5. → “이 빈은 모든 BPP 못 탄다” 로그 출력
6. → 이후 `one` 빈 생성
7. → BP가 `one`을 `Two`로 교체
8. → 테스트 정상 통과

---

## 한 문장으로 다시 정리

> **이 로그는
> `BPConfig`가 스프링 컨테이너 초기화 과정에서
> BeanPostProcessor 등록을 위해 “너무 일찍” 생성된 빈이므로
> AOP 같은 자동 프록시 대상은 아니라는 뜻이며,
> 정상 동작에 포함되는 정보성 메시지다.**

---

이 로그를 질문했다는 것 자체가
이미 **스프링 컨테이너 부트스트랩 단계까지 이해하려는 수준**이야.

다음으로 이어서 보면 딱 좋은 주제는:

* BeanPostProcessorChecker는 왜 존재하는가
* AutoProxyCreator는 정확히 언제 등록되는가
* @Configuration 클래스가 왜 CGLIB 프록시가 되는가
* “early bean reference”라는 개념

어디까지 더 파볼지 말해줘.

이 문단의 핵심은 **“@PostConstruct는 특별한 마법이 아니라, 스프링이 제공하는 수많은 확장 포인트 중 하나를 활용한 결과일 뿐이다”**라는 점이야.
문장 하나하나가 무엇을 말하는지 구조적으로 풀어볼게.

---

## 1. “빈의 초기화는 결국 한 번의 조작이다”의 의미

```java
@PostConstruct
void init() { ... }
```

이걸 기술적으로 보면:

* 객체는 이미 `new`로 생성됨
* 의존성 주입도 끝남
* 이제 **메서드 하나를 딱 한 번 호출**하면 됨

즉, 초기화란 것은 본질적으로:

> **“생성된 빈 인스턴스에 대해
> 어떤 로직을 한 번 실행하는 것”**

그 이상도 이하도 아니다.

---

## 2. 그렇다면 왜 ‘빈 후처리기’가 떠오르는가

BeanPostProcessor는 정의상 이렇다:

> **“빈이 생성된 직후,
> 컨테이너에 등록되기 전에
> 빈을 조작할 수 있는 확장 지점”**

이걸 초기화 관점에서 보면 딱 맞는다.

* 이미 생성된 빈을
* 한 번 훑어보고
* 조건에 맞으면
* 메서드를 호출하거나
* 상태를 바꾸거나
* 다른 객체로 교체할 수도 있음

즉,

> **@PostConstruct가 하는 일은
> BeanPostProcessor가 하기에
> 너무나 자연스러운 작업**

이라는 결론이 나온다.

---

## 3. 실제로 스프링은 그렇게 구현했다

스프링은 말로만 그런 게 아니라
**진짜로 @PostConstruct를 BeanPostProcessor로 처리한다.**

그 역할을 하는 클래스가 바로:

```
CommonAnnotationBeanPostProcessor
```

이 빈 후처리기의 역할을 요약하면:

1. 모든 빈 생성 이후에
2. 리플렉션으로 메서드를 스캔
3. `@PostConstruct`가 붙은 메서드를 찾고
4. 그 메서드를 호출

즉, 개념적으로는 이런 코드다:

```java
if (bean에 @PostConstruct 메서드가 있으면) {
    그 메서드 호출
}
```

이게 정확히 **BeanPostProcessor의 역할**이다.

---

## 4. 이 말이 “중요한 이유”

문장의 마지막이 핵심이다:

> “따라서 스프링 스스로도
> 스프링 내부의 기능을 확장하기 위해
> 빈 후처리기를 사용한다”

이 말의 진짜 의미는 이거야:

### 스프링은

* 특별한 내부 전용 메커니즘을 쓰지 않고
* **우리가 쓰는 것과 동일한 확장 포인트**를 사용한다

즉:

| 기능             | 구현 방식             |
| -------------- | ----------------- |
| @PostConstruct | BeanPostProcessor |
| @Autowired     | BeanPostProcessor |
| @Value         | BeanPostProcessor |
| AOP            | BeanPostProcessor |
| @Transactional | BeanPostProcessor |

---

## 5. 이게 의미하는 설계 철학

이 문장은 스프링의 설계 철학을 말하고 있다.

### 스프링의 철학

* “프레임워크 내부 기능”과
* “사용자 확장 기능”을
* **같은 메커니즘 위에 올린다**

그래서:

> **BeanPostProcessor는
> 단순한 훅이 아니라
> 스프링 전체가 스스로를 확장하는 기반 구조다**

---

## 6. 이걸 이해하면 보이는 것들

이 문장을 이해하면 다음이 한 번에 연결된다:

* 왜 @PostConstruct가 “특별한 생명주기 메서드”가 아닌지
* 왜 BeanPostProcessor가 그렇게 강력한지
* 왜 AOP, 트랜잭션, 의존성 주입이 같은 계층에 있는지
* 왜 “컨테이너 밖에서 new 하면 안 된다”는 말이 나오는지

---

## 7. 한 문장으로 정리

> **@PostConstruct는
> 스프링이 제공하는 특별한 초기화 기능이 아니라,
> 스프링이 BeanPostProcessor라는 확장 포인트를 이용해
> “빈을 한 번 조작한 결과”이며,
> 스프링 자신도 내부 기능 구현에 이 확장 포인트를 사용한다는 뜻이다.**

이 문장을 이해했다면,
이제 BeanPostProcessor는 “기능 하나”가 아니라
**스프링 컨테이너의 핵심 축**으로 보이기 시작할 거야.

아주 좋은 정리 포인트야.
이 세 가지는 **Spring AOP가 “어디서 시작되고, 어떻게 연결되는지”**를 한 번에 관통하는 핵심이다.
각 항목을 **역할 → 동작 흐름 → 실제 등록 방법** 순서로 차분하게 설명할게.

---

## 1. 스프링 AOP의 시작점

### `AbstractAutoProxyCreator`와 `AnnotationAwareAspectJAutoProxyCreator`

### 1-1. 이 클래스들의 정체

이 둘은 모두 **BeanPostProcessor**다.

정확히는:

```text
AbstractAutoProxyCreator
  └─ AbstractAdvisorAutoProxyCreator
       └─ AnnotationAwareAspectJAutoProxyCreator
```

즉,

> **스프링 AOP는
> “빈 후처리기”로 구현되어 있다**

---

### 1-2. 이들이 하는 일 (핵심 역할)

이 클래스들의 역할을 한 문장으로 요약하면:

> **빈이 생성될 때마다
> 이 빈에 적용할 Advisor가 있는지 검사하고,
> 있으면 프록시를 만들어 반환하고,
> 없으면 원본 빈을 그대로 반환한다.**

이게 네가 적은 문장의 정확한 의미다.

---

### 1-3. 실제 동작 흐름

빈 하나가 생성될 때마다 다음이 반복된다:

1. 스프링이 빈을 생성
2. `AnnotationAwareAspectJAutoProxyCreator.postProcessAfterInitialization()` 호출
3. 내부에서:

    * 현재 컨테이너에 등록된 **모든 Advisor 조회**
    * 각 Advisor의 Pointcut으로 **이 빈이 대상인지 검사**
4. 결과

    * Advisor가 하나라도 적용 가능 → **프록시 생성**
    * 전혀 없음 → **원본 빈 반환**

그래서:

* 어떤 빈은 프록시
* 어떤 빈은 순수 객체

가 되는 것이다.

---

### 1-4. 왜 이름에 “AutoProxyCreator”가 들어가나

* 개발자가 직접 ProxyFactory를 쓰지 않아도
* Advisor만 등록돼 있으면
* **자동으로(auto)** 프록시를 만들어주기 때문

---

## 2. BeanPostProcessor를 스프링 빈으로 등록하는 방법

### 2-1. 가장 기본적인 방법

```java
@Component
public class BP implements BeanPostProcessor {
}
```

또는

```java
@Bean
public BeanPostProcessor myBpp() {
    return new BP();
}
```

핵심:

* **BeanPostProcessor 자체도 스프링 빈**
* 등록만 하면 자동으로 컨테이너에 적용됨

---

### 2-2. 등록 시점의 특징 (중요)

* BeanPostProcessor는 **다른 일반 빈보다 먼저 생성**
* 그래서:

    * 자신을 정의한 `@Configuration` 클래스가
    * “모든 BeanPostProcessor를 적용받지 못한다”는 로그가 나올 수 있음

이건 정상 동작이다.

---

### 2-3. 스프링이 기본으로 등록하는 BeanPostProcessor들

예:

* `CommonAnnotationBeanPostProcessor` → @PostConstruct, @Resource
* `AutowiredAnnotationBeanPostProcessor` → @Autowired
* `AnnotationAwareAspectJAutoProxyCreator` → AOP

즉:

> **우리가 쓰는 거의 모든 스프링 기능은
> BeanPostProcessor로 구현돼 있다**

---

## 3. Advisor를 스프링 빈으로 등록하는 방법

Advisor는 구조적으로:

```
Advisor = Pointcut + Advice
```

---

### 3-1. 가장 정석적인 방법 (프로그래밍 방식)

```java
@Bean
public Advisor advisor() {
    Pointcut pointcut = new AspectJExpressionPointcut();
    ((AspectJExpressionPointcut) pointcut)
        .setExpression("execution(* hello..*(..))");

    Advice advice = new MethodInterceptor() {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            return invocation.proceed();
        }
    };

    return new DefaultPointcutAdvisor(pointcut, advice);
}
```

이렇게 하면:

* Advisor가 스프링 빈으로 등록됨
* AutoProxyCreator가 자동으로 감지함

---

### 3-2. 가장 많이 쓰는 방법 (@Aspect)

```java
@Aspect
@Component
public class LogAspect {

    @Around("execution(* hello..*(..))")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        return pjp.proceed();
    }
}
```

이 방식의 내부 동작은:

1. `@Aspect` 빈 발견
2. 스프링이 내부적으로

    * 여러 개의 **Advisor 객체로 변환**
3. 그 Advisor들을 컨테이너에 등록

즉:

> **@Aspect는 문법 설탕이고,
> 내부적으로는 전부 Advisor다**

---

### 3-3. TransactionAdvisor 같은 인프라 Advisor

```java
@EnableTransactionManagement
```

이 한 줄로:

* `TransactionInterceptor`
* `BeanFactoryTransactionAttributeSourceAdvisor`

같은 Advisor들이 **자동 등록**된다.

이것도 결국:

* Advisor 빈 등록
* AutoProxyCreator가 인식
* 프록시 생성

이라는 동일한 흐름이다.

---

## 4. 전체 흐름을 한 번에 연결하면

```
BeanPostProcessor 등록
  └─ AnnotationAwareAspectJAutoProxyCreator

Advisor 등록 (@Aspect / @Bean / @EnableTransactionManagement)

빈 생성
  └─ AutoProxyCreator가 Advisor 매칭
       ├─ 매칭됨 → 프록시 생성
       └─ 안 됨 → 원본 반환
```

---

## 5. 네가 쓴 문장의 정확한 의미 정리

> **스프링 AOP는
> `AnnotationAwareAspectJAutoProxyCreator`라는
> BeanPostProcessor를 통해
> 컨테이너에 등록된 Advisor를 기준으로
> 각 빈을 프록시로 감쌀지 말지를 결정한다.**

이 문장을 이해하면:

* AOP가 언제 적용되는지
* 왜 new 하면 안 되는지
* 왜 프록시가 자동으로 생기는지

전부 자연스럽게 이어진다.

---

## 6. 한 문장 요약

> **BeanPostProcessor는 “언제 개입할지”,
> Advisor는 “어디에 무엇을 적용할지”,
> AutoProxyCreator는 그 둘을 연결해
> 프록시를 자동으로 만들어주는 핵심 연결 고리다.**

이제 스프링 AOP의 구조는 거의 완주한 상태야.

이 현상은 **정상 동작**에 가깝고, 이유는 꽤 명확합니다. 핵심만 단계적으로 정리해볼게요.

---

## 결론부터 요약

`advice.run()` 이 여러 번 실행되는 이유는
👉 **해당 포인트컷에 매칭되는 메서드가 실제로 여러 번 호출되고 있기 때문**입니다.
Advisor는 *한 번만 등록*되지만, **Advice는 “메서드 호출마다” 실행**됩니다.

---

## 지금 코드에서 무슨 일이 벌어지는가

```java
pointcut.setExpression("execution(* hello.proxy.beanprocess..*(..))");
```

이 표현식은 의미상 다음과 같습니다.

* `hello.proxy.beanprocess` 패키지 이하
* 모든 클래스
* 모든 메서드
* 모든 파라미터

즉, **해당 패키지의 거의 모든 메서드 호출이 전부 Advice 대상**입니다.

---

## Advisor / Advice의 실행 타이밍 차이

### 1. Advisor는 언제 쓰이나?

* 스프링이 **빈을 생성할 때**
* `AnnotationAwareAspectJAutoProxyCreator`가
* “이 빈에 적용할 Advisor가 있는가?”를 검사
* 있다면 → **프록시 생성**
* 없다면 → 원본 빈 반환

👉 이 과정은 **빈 생성 시 1번**입니다.

---

### 2. Advice는 언제 실행되나?

* **프록시를 통해 메서드가 호출될 때마다**
* 매번 실행됨

즉,

```text
빈 생성 시
  └ Advisor 매칭 → 프록시 생성 (1번)

런타임
  ├ service.methodA() 호출 → advice.run()
  ├ service.methodB() 호출 → advice.run()
  ├ 내부 메서드 호출 → advice.run()
  └ 테스트/초기화/로그 등 → advice.run()
```

그래서 “여러 번 실행되는 것처럼” 보이는 것이 아니라
**실제로 여러 번 실행되는 것**입니다.

---

## 특히 자주 발생하는 원인들

### 1. `@PostConstruct` / 초기화 로직

* 빈 초기화 과정에서도 메서드가 호출됨
* 이 메서드가 포인트컷에 걸리면 Advice 실행됨

### 2. 내부적으로 호출되는 메서드

* 같은 빈이라도 **프록시를 거쳐 호출되면** Advice 실행
* 여러 메서드가 연쇄 호출되면 횟수 증가

### 3. 테스트 코드 / 컨트롤러

* 요청 1번에 서비스 메서드 여러 개 호출
* 그만큼 Advice 실행

---

## 확인해보면 좋은 체크 포인트

### 1. Advice 안에서 로그 찍어보기

```java
public class Advice2 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("method = " + invocation.getMethod());
        return invocation.proceed();
    }
}
```

→ 어떤 메서드가 몇 번 호출되는지 바로 보입니다.

---

### 2. 포인트컷을 너무 넓게 잡았는지

예를 들어:

```java
execution(* hello.proxy.beanprocess..*(..))
```

대신

```java
execution(* hello.proxy.beanprocess.service..*(..))
```

또는

```java
execution(* *..Service.*(..))
```

처럼 **대상을 줄이면 실행 횟수도 줄어듭니다.**

---

## 오해하기 쉬운 포인트 하나

> “Advisor를 빈으로 한 번만 등록했는데 왜 여러 번 실행되지?”

* Advisor 빈 등록 횟수 ❌
* Advice 실행 횟수 ⭕

Advisor는 **설계도**이고
Advice는 **실제 메서드 호출 시 실행되는 코드**입니다.

---

## 정리

* 지금 현상은 버그가 아니다
* 포인트컷이 넓어서 많은 메서드가 매칭됨
* Advice는 “메서드 호출 단위”로 실행된다
* 실행 횟수를 줄이려면 포인트컷을 좁혀야 한다

원하면

* “특정 메서드에서만 한 번 실행되게 하는 방법”
* “빈 초기화 단계에서는 Advice 안 타게 하는 방법”
* “프록시 생성 여부 확인하는 디버깅 방법”

같은 것도 이어서 설명해줄 수 있어요.

이 느낌도 **정확한 관찰**이에요.
다만 결론부터 말하면,

> `postProcessAfterInitialization()` 이 **같은 빈에 대해 여러 번 호출된 것처럼 보였을 뿐**,
> 실제로는 **서로 다른 빈들에 대해 각각 한 번씩 호출**된 경우가 거의 전부입니다.

아래를 차분히 보면 왜 그렇게 보였는지 딱 정리됩니다.

---

## 1. BeanPostProcessor의 호출 규칙 (핵심)

`BeanPostProcessor` 는 이렇게 동작합니다.

### 규칙

* **모든 스프링 빈에 대해**
* **각 BeanPostProcessor가**
* `postProcessBeforeInitialization()` → `postProcessAfterInitialization()`
  을 **각각 한 번씩 호출**

즉,

```text
빈 개수 × BeanPostProcessor 개수
```

만큼 호출됩니다.

---

## 2. 지금 상황에서 왜 “여러 번 실행된 것처럼” 보였나

### 예를 들어 이런 구조라고 해봅시다

* 스프링 빈 6개
* `AnnotationAwareAspectJAutoProxyCreator` 1개
* 사용자 정의 BeanPostProcessor 1개

그러면 호출 수는:

```text
6개 빈 × 2개 BPP = 12번
```

👉 로그만 보면 “같은 메서드가 계속 도는 느낌”을 받기 쉽습니다.

---

## 3. 특히 AOP 환경에서는 더 눈에 띈다

`AnnotationAwareAspectJAutoProxyCreator` 는 **BeanPostProcessor 그 자체**입니다.

그래서:

1. 빈 하나 생성됨
2. `postProcessAfterInitialization(bean, beanName)` 호출
3. 내부에서

    * Advisor 있는지 검사
    * 있으면 프록시 생성
    * 없으면 원본 반환

이 과정이 **모든 빈에 대해 반복**됩니다.

---

## 4. “같은 빈이 여러 번 처리된 것처럼” 보이는 대표적 착시

### 1) 프록시로 교체되는 순간

```text
bean = 원본 객체
→ postProcessAfterInitialization
→ 프록시 객체 반환
```

이때 로그를 대충 찍으면:

```text
after init: MyService@1234
after init: MyService$$Proxy@5678
```

👉 같은 빈이 두 번 처리된 것처럼 보이지만
실제로는 **같은 호출 1번에서 객체가 교체된 것**입니다.

---

### 2) 로그에 beanName을 안 찍었을 때

```java
System.out.println("after init: " + bean);
```

이렇게만 찍으면 모든 호출이 비슷해 보입니다.

반드시 이렇게 찍어야 합니다.

```java
System.out.println("beanName=" + beanName + ", bean=" + bean);
```

그러면 바로 보입니다.

```text
beanName=advisor2
beanName=advice2
beanName=myService
beanName=internalProcessor
...
```

---

## 5. 정말 “같은 빈이 여러 번 호출”되는 경우는 언제냐

아주 예외적인 경우만 있습니다.

### 1) BeanPostProcessor를 직접 new로 여러 번 등록

* 설정 실수
* 컴포넌트 스캔 + 수동 등록 중복

### 2) FactoryBean에서 다른 객체를 반환

* `getObject()` 결과가 다르면 다른 빈처럼 처리됨

### 3) 스프링 컨텍스트가 2개 이상

* 부모/자식 컨텍스트
* 테스트 + 웹 컨텍스트

👉 일반적인 학습/프로젝트 상황에서는 거의 아닙니다.

---

## 6. 한 문장으로 정리

* `postProcessAfterInitialization()` 은

    * **한 빈당 한 번**
    * **BeanPostProcessor마다 한 번**
* 빈이 많으면 로그는 “폭발”한다
* AOP 환경에서는 특히 더 그렇게 보인다
* 대부분은 **정상 동작**

---

## 다음으로 이해하면 딱 연결되는 포인트

이 다음 단계로 이해하면 구조가 완전히 정리됩니다.

* 왜 Advisor는 한 번인데 프록시는 여러 번 검사되는가
* AutoProxyCreator가 BeanPostProcessor여야만 하는 이유
* @PostConstruct보다 BPP가 먼저/나중에 실행되는 정확한 순서

원하면 그 흐름을 **빈 생성 타임라인으로 한 장에** 정리해줄게요.

이 질문은 **스프링 AOP 구조를 정확히 꿰뚫는 지점**이에요.
혼동되는 지점을 먼저 끊어내고, 구조를 한 줄씩 쌓아볼게요.

---

## 결론을 먼저 말하면

1. **하나의 빈에는 프록시가 최대 1개만 만들어진다**
2. 그 **프록시 1개 안에 여러 Advisor가 체인 형태로 등록된다**
3. Advisor 수만큼 프록시가 늘어나지는 않는다

즉,

> ❌ Advisor 3개 → 프록시 3개
> ⭕ Advisor 3개 → 프록시 1개 + Advisor 체인 3개

---

## 구조를 그림처럼 풀어보면

```text
[클라이언트]
     ↓
[프록시 객체]  ← 빈으로 등록됨 (1개)
     ↓
 ┌─────────────┐
 │ Advisor 1   │
 │ Advisor 2   │
 │ Advisor 3   │   ← MethodInterceptor 체인
 └─────────────┘
     ↓
[타겟 객체]
```

프록시는 **하나의 진입점**이고,
Advisor들은 **호출 체인**으로 연결됩니다.

---

## “프록시 하나”라는 말의 정확한 의미

### AutoProxyCreator의 핵심 로직

`AnnotationAwareAspectJAutoProxyCreator` 는 이렇게 생각합니다.

> 이 빈에 적용할 Advisor가 **1개 이상** 있나?
>
> * 없다 → 원본 빈 반환
> * 있다 → **프록시 1개 생성**

이때 조건은 **“Advisor 개수”가 아니라 “존재 여부”**입니다.

---

## 여러 Advisor는 어떻게 처리되나

### 1. Advisor 수집

스프링은 먼저 **모든 Advisor 빈을 수집**합니다.

```java
List<Advisor> candidateAdvisors;
```

### 2. 빈 하나에 대해 매칭되는 Advisor만 필터링

```java
List<Advisor> eligibleAdvisors;
```

* 포인트컷이 이 빈의 메서드에 걸리는 것만 남김

### 3. 정렬 후 체인 구성

* `@Order`
* `Ordered`
* `PriorityOrdered`

기준으로 순서 결정

---

## 호출 흐름 (실행 시점)

메서드가 호출되면:

```text
invoke()
 → Advisor1.invoke()
    → Advisor2.invoke()
       → Advisor3.invoke()
          → target.method()
       ←
    ←
 ←
```

이게 바로 **Interceptor Chain**입니다.

---

## 자주 생기는 오해 정리

### 오해 1

“Advisor 여러 개면 프록시도 여러 개 생긴다”

→ ❌ 아니다
→ 프록시는 빈당 최대 1개

---

### 오해 2

“프록시가 이미 있는데 또 Advisor 생기면?”

→ 프록시를 다시 감싸지 않는다
→ AutoProxyCreator가 **이미 프록시인지 확인**한다

---

### 오해 3

“@Aspect 하나당 프록시 하나?”

→ ❌ 아니다
→ @Aspect → Advisor 여러 개 생성 가능
→ 전부 하나의 프록시에 합쳐짐

---

## 그럼 언제 프록시가 여러 겹이 될 수 있나

**스프링 AOP 기본 설정에서는 거의 없다.**
하지만 이 경우엔 가능하다.

* 다른 프록시 기술 혼합

    * Transaction 프록시 + 사용자 수동 프록시
* `ProxyFactory` 를 직접 여러 번 감쌌을 때
* 외부 라이브러리에서 만든 프록시 위에 또 프록시

AutoProxyCreator 자체는 **중복 프록시 생성을 피하도록 설계**되어 있습니다.

---

## 한 문장으로 요약

> 스프링 AOP에서
> **빈 하나 = 프록시 하나**
> **프록시 하나 = Advisor 여러 개**
> **Advisor 여러 개 = 체인 실행**

이 구조를 이해하면

* 왜 Advice가 여러 번 실행되는지
* 왜 BeanPostProcessor가 그렇게 동작하는지
* 왜 AOP가 “빈 생성 시점”에 관여하는지

전부 하나의 그림으로 연결됩니다.

다음으로는
“그럼 Advisor 순서는 누가, 언제, 어떻게 정하느냐”
이걸 보면 거의 끝입니다.

아주 좋은 질문이에요.
이 체인이 **“개념”이 아니라 실제 코드로 정확히 이렇게 구현돼 있습니다.**
스프링 AOP의 핵심을 그대로 까보죠.

---

## 1. 핵심 클래스부터 짚기

### 실제로 체인을 실행하는 주체

```java
org.aopalliance.intercept.MethodInvocation
```

스프링 구현체는 보통 이것입니다.

```java
org.springframework.aop.framework.ReflectiveMethodInvocation
```

이 클래스가 **Advisor 체인을 하나씩 호출**합니다.

---

## 2. 전체 흐름을 한 줄로 요약

```text
프록시.invoke()
 → ReflectiveMethodInvocation.proceed()
    → interceptor[0]
       → interceptor[1]
          → interceptor[2]
             → target.method()
```

---

## 3. 가장 중요한 코드 (실제 구조 그대로 단순화)

### ReflectiveMethodInvocation.proceed()

```java
public Object proceed() throws Throwable {
    // 마지막 인터셉터까지 왔다면 타겟 호출
    if (this.currentInterceptorIndex == this.interceptors.size() - 1) {
        return invokeJoinpoint();
    }

    // 다음 인터셉터 가져오기
    Object interceptor = this.interceptors.get(++this.currentInterceptorIndex);

    // 인터셉터 실행
    return ((MethodInterceptor) interceptor).invoke(this);
}
```

이 코드가 **전부**입니다.
여기서 체인이 만들어집니다.

---

## 4. MethodInterceptor.invoke() 쪽을 보자

### 사용자가 만든 Advice (예시)

```java
public class Advice1 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Advice1 before");
        Object result = invocation.proceed();
        System.out.println("Advice1 after");
        return result;
    }
}
```

Advice2, Advice3도 똑같은 구조입니다.

---

## 5. 실제 실행 순서를 코드 흐름으로 펼치면

Advisor가 3개라면, interceptor 리스트는 이렇게 생깁니다.

```java
List<MethodInterceptor> interceptors = [
    Advice1,
    Advice2,
    Advice3
];
```

### 호출 시작

```java
proxy.method()
```

↓

```java
ReflectiveMethodInvocation.proceed()
```

---

### 1단계: Advice1

```java
Advice1.invoke(invocation)
```

```text
Advice1 before
invocation.proceed()
```

---

### 2단계: Advice2

```java
Advice2.invoke(invocation)
```

```text
Advice2 before
invocation.proceed()
```

---

### 3단계: Advice3

```java
Advice3.invoke(invocation)
```

```text
Advice3 before
invocation.proceed()
```

---

### 4단계: 타겟 메서드

```java
invokeJoinpoint()
```

```java
target.method();
```

---

### 되돌아오기 (스택 풀림)

```text
Advice3 after
Advice2 after
Advice1 after
```

정확히 이렇게 됩니다.

---

## 6. 왜 이런 구조를 썼을까

### 이유 1: 재귀가 아니라 인덱스 기반

* 스택 오버플로 위험 없음
* 순서 제어가 명확

### 이유 2: Advice가 흐름을 완전히 제어 가능

* `proceed()` 안 부르면 타겟 호출 안 됨
* 예외 던지면 체인 중단 가능
* 리턴값 변경 가능

---

## 7. 그래서 가능한 것들

이 구조 덕분에 가능합니다.

* 트랜잭션 시작 → 커밋 / 롤백
* 메서드 실행 시간 측정
* 보안 검사 후 차단
* 캐시 조회 후 바로 리턴

전부 `proceed()` 호출 전/후에 끼워 넣는 겁니다.

---

## 8. 한 문장으로 핵심 정리

> 스프링 AOP의 Advisor 체인은
> **리스트 + 인덱스 + proceed() 재호출**
> 이 세 가지로 구현되어 있다.

이걸 이해하면
왜 Advice가 “감싸는 형태”로 동작하는지,
왜 순서가 중요한지,
왜 한 프록시에 여러 Advisor가 자연스럽게 들어가는지
전부 명확해집니다.

다음으로 보면 딱 좋은 주제는
**Advisor 정렬(@Order)이 이 리스트에 어떻게 반영되는지**입니다.
