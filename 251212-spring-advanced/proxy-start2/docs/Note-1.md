아래 개념들은 **프록시 기반 AOP**와 **동적 프록시 구현**을 이해할 때 핵심이 되는 요소들이다. 서로 연결되어 있지만 역할과 사용 위치가 다르므로, 구조적으로 나눠 설명하겠다.

---

## 1. 프록시(Proxy)와 프록시 팩토리(Proxy Factory)

### 프록시란?

* **실제 객체(Target)를 대신하는 객체**
* 메서드 호출을 가로채서 **부가 기능을 실행한 뒤 실제 객체로 위임**할 수 있다
* 핵심 관심사(비즈니스 로직)와 부가 관심사(로깅, 트랜잭션, 보안 등)를 분리하는 데 사용된다

### 프록시 팩토리란?

* **프록시 객체를 생성해주는 공장**
* 어떤 방식의 프록시를 만들지(JDK 동적 프록시 vs CGLIB)를 내부에서 결정한다

Spring 기준:

* 인터페이스가 있으면 → **JDK 동적 프록시**
* 인터페이스가 없으면 → **CGLIB 프록시**

즉, 개발자는 프록시 생성 전략을 직접 신경 쓰지 않고 팩토리에 맡긴다.

---

## 2. 어드바이스(Advice)

### 어드바이스란?

* **프록시가 실행하는 “부가 기능 코드”**
* 언제, 어떤 시점에 실행할지를 정의한다

### 대표적인 어드바이스 종류

* **Before**: 메서드 실행 전
* **After Returning**: 정상 종료 후
* **After Throwing**: 예외 발생 시
* **After (Finally)**: 무조건 실행
* **Around**: 메서드 실행 전후를 모두 제어 (가장 강력)

Spring AOP에서 가장 많이 쓰이는 것은 **Around Advice**다.

---

## 3. InvocationHandler (JDK 동적 프록시)

### 역할

* **JDK 동적 프록시에서 호출을 가로채는 핵심 인터페이스**
* 인터페이스 기반 프록시에서만 사용 가능

```java
public interface InvocationHandler {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
```

### 동작 흐름

1. 클라이언트가 프록시 객체의 메서드 호출
2. JVM이 `invoke()` 호출
3. invoke 내부에서

    * 부가 기능 실행
    * 실제 타깃 객체의 메서드 호출
    * 결과 반환

### 특징

* 반드시 **인터페이스가 있어야 함**
* 리플렉션 기반 (`Method.invoke`)

---

## 4. MethodInterceptor (CGLIB / Spring AOP)

### 역할

* **CGLIB 기반 프록시에서 메서드 호출을 가로채는 인터페이스**
* 클래스 상속 기반 프록시에서 사용
* Spring AOP의 핵심 인터페이스 중 하나

```java
public interface MethodInterceptor {
    Object invoke(MethodInvocation invocation) throws Throwable;
}
```

### MethodInvocation

* 실제 메서드 정보 + 실행 제어 기능을 포함
* `proceed()`를 호출해야 다음 단계(또는 실제 메서드)가 실행됨

```java
Object result = invocation.proceed();
```

### 특징

* 인터페이스 없이도 가능
* 프록시가 타깃 클래스를 상속
* Spring의 **Around Advice 구현의 기반**

---

## 5. InvocationHandler vs MethodInterceptor 비교

| 구분            | InvocationHandler | MethodInterceptor      |
| ------------- | ----------------- | ---------------------- |
| 사용 기술         | JDK 동적 프록시        | CGLIB / Spring AOP     |
| 인터페이스 필요      | 필요                | 불필요                    |
| 메서드 실행        | `method.invoke()` | `invocation.proceed()` |
| Spring AOP 사용 | 간접적               | 핵심                     |
| 유연성           | 상대적으로 낮음          | 높음                     |

---

## 6. 전체 구조 연결

```text
클라이언트
   ↓
프록시 객체
   ↓
[ InvocationHandler / MethodInterceptor ]
   ↓
어드바이스(부가 기능)
   ↓
타깃 객체
```

Spring에서는:

* **어드바이스** → 부가 로직
* **MethodInterceptor** → 어드바이스를 감싸 실행
* **프록시 팩토리** → 적절한 프록시 생성

---

## 핵심 요약

* **프록시 팩토리**: 프록시 생성 책임
* **어드바이스**: 실행할 부가 기능
* **InvocationHandler**: JDK 인터페이스 기반 프록시의 호출 가로채기
* **MethodInterceptor**: Spring/CGLIB 기반 프록시의 핵심 인터셉터

원하면

* 실제 Spring AOP 내부 동작 흐름
* 트랜잭션 어드바이스가 어떻게 적용되는지
* InvocationHandler → MethodInterceptor로 발전한 이유

같은 주제로 더 깊게 설명해줄 수도 있다.

이 말의 의미를 한 문장으로 요약하면 다음과 같다.

**개발자는 “무엇을 할지(Advice)”만 정의하면, “어떻게 가로챌지(InvocationHandler / MethodInterceptor)”는 프레임워크가 대신 선택하고 연결해 준다**는 뜻이다.

아래에서 단계적으로 풀어 설명하겠다.

---

## 1. 원래는 handler / interceptor를 직접 만들어야 했다

프록시를 직접 만들던 시절에는 이런 작업을 개발자가 해야 했다.

* JDK 동적 프록시 → `InvocationHandler` 직접 구현
* CGLIB 프록시 → `MethodInterceptor` 직접 구현
* 어떤 프록시 기술을 쓸지도 직접 결정
* 타깃 메서드 호출 시점 제어도 직접 작성

즉, **부가 기능을 만들고 싶을 뿐인데 기술적인 코드가 너무 많았다**.

---

## 2. Spring의 핵심 아이디어: 역할 분리

Spring은 이 문제를 이렇게 나눴다.

| 역할          | 담당                            |
| ----------- | ----------------------------- |
| 무엇을 할 것인가   | Advice                        |
| 언제 적용할 것인가  | Pointcut                      |
| 어떻게 가로챌 것인가 | Handler / Interceptor (내부 구현) |
| 프록시 생성      | ProxyFactory                  |

개발자는 **위에서 위쪽(의도)**만 작성하고,
아래쪽(기술 구현)은 Spring이 맡는다.

---

## 3. “프록시 팩토리 + Advice만 있으면 된다”의 정확한 의미

### ProxyFactory가 하는 일

ProxyFactory는 다음을 자동으로 판단한다.

1. 타깃 클래스에 인터페이스가 있는가?

    * 있다 → JDK 동적 프록시 사용
    * 없다 → CGLIB 사용
2. 프록시 방식에 맞는 호출 가로채기 기술 선택

    * JDK → `InvocationHandler`
    * CGLIB → `MethodInterceptor`
3. Advice를 해당 기술에 맞게 **어댑터로 감싼다**

---

## 4. Advice는 handler / interceptor가 아니다

여기서 중요한 포인트가 있다.

* **Advice ≠ InvocationHandler**
* **Advice ≠ MethodInterceptor**

Advice는 그보다 훨씬 추상적이다.

예를 들어 Around Advice는 이렇게 생겼다.

```java
Object invoke(MethodInvocation invocation)
```

Spring은 내부에서 다음 작업을 한다.

```text
Advice
   ↓ (어댑터)
MethodInterceptor
   ↓
InvocationHandler (필요 시)
```

이 과정을 개발자는 전혀 보지 않는다.

---

## 5. 내부에서 실제로 일어나는 일 (개념 흐름)

```text
1. 개발자
   - Advice 등록
   - ProxyFactory에 타깃 전달

2. ProxyFactory
   - 프록시 방식 결정 (JDK / CGLIB)
   - Advice를 Interceptor로 변환

3. 프록시 생성
   - InvocationHandler 또는 MethodInterceptor 자동 생성

4. 실행 시
   - 프록시 → 인터셉터 → Advice → 타깃 메서드
```

즉,

**“Advice만 있으면 된다” =
Advice를 실행하기 위한 모든 기술적 장치를 Spring이 대신 구성한다**

라는 뜻이다.

---

## 6. 왜 이런 구조가 중요한가

이 구조 덕분에 얻는 이점은 명확하다.

### 1) 기술 독립성

* JDK 프록시인지 CGLIB인지 신경 쓸 필요 없음

### 2) 관심사 분리

* 비즈니스 로직과 부가 로직에만 집중
* 프록시 기술은 완전히 숨겨짐

### 3) 확장성

* 같은 Advice를

    * 인터페이스 기반 클래스
    * 인터페이스 없는 클래스
      모두에 적용 가능

---

## 7. 한 문장으로 다시 정리

> 프록시 팩토리와 Advice만 있으면 handler, interceptor를 자동으로 써준다는 말은
> **프록시 기술(JDK/CGLIB)과 호출 가로채기 구현을 개발자가 직접 작성하지 않아도,
> Spring이 Advice를 실행하기에 적절한 형태로 자동 변환하고 연결해 준다**는 의미다.

---

원하면 다음도 이어서 설명할 수 있다.

* Advice → MethodInterceptor로 변환하는 **어댑터 패턴**
* 왜 Spring AOP의 핵심 인터페이스가 MethodInterceptor인지
* @Transactional이 이 구조 위에서 어떻게 동작하는지

보통 **pointcut 구현**이라고 하면 **Spring AOP(AspectJ 스타일)** 기준으로 설명하는 경우가 많아서, 그 흐름에 맞춰 단계별로 정리해 줄게.
(개념 → 선언 방식 → 예제 → 실무 팁 순서)

---

## 1. Pointcut이란 무엇인가

**Pointcut**은

> *어떤 지점(join point)에 부가 기능(advice)을 적용할지*를 정의하는 **조건식**이다.

즉,

* **Join point**: 메서드 실행 시점
* **Pointcut**: 그중 *어떤 메서드에 적용할지*
* **Advice**: 실제 실행할 부가 로직
* **Aspect**: Pointcut + Advice 묶음

---

## 2. 가장 기본적인 Pointcut 구현 (@Aspect 방식)

### 2-1. 의존성

```gradle
implementation 'org.springframework.boot:spring-boot-starter-aop'
```

---

### 2-2. Aspect 클래스 작성

```java
@Aspect
@Component
public class LogAspect {
}
```

---

## 3. Pointcut 선언 방법 1: 직접 Advice에 작성

가장 많이 쓰는 방식이다.

```java
@Aspect
@Component
public class LogAspect {

    @Before("execution(* com.example.service..*(..))")
    public void beforeLog() {
        System.out.println("메서드 실행 전");
    }
}
```

### 의미 해석

```
execution(
    *                     // 반환 타입
    com.example.service.. // 패키지 (하위 포함)
    *                     // 메서드명
    (..)                  // 파라미터
)
```

---

## 4. Pointcut 선언 방법 2: @Pointcut으로 분리 (권장)

재사용성과 가독성이 좋아서 실무에서 가장 많이 사용된다.

```java
@Aspect
@Component
public class LogAspect {

    @Pointcut("execution(* com.example.service..*(..))")
    public void serviceMethods() {}

    @Before("serviceMethods()")
    public void beforeLog() {
        System.out.println("메서드 실행 전");
    }

    @AfterReturning("serviceMethods()")
    public void afterLog() {
        System.out.println("메서드 정상 종료 후");
    }
}
```

---

## 5. 자주 쓰는 Pointcut 표현식 종류

### 5-1. execution (가장 중요)

```java
execution(* com.example..*(..))
```

### 5-2. 특정 어노테이션 기준

```java
@Pointcut("@annotation(com.example.annotation.Loggable)")
public void loggableMethods() {}
```

```java
@Before("loggableMethods()")
public void log() {}
```

---

### 5-3. 클래스 어노테이션 기준

```java
@Pointcut("@within(org.springframework.stereotype.Service)")
public void serviceClasses() {}
```

---

### 5-4. 파라미터 조건

```java
@Pointcut("execution(* *(String, ..))")
public void firstParamIsString() {}
```

---

## 6. Advice 종류별 예제

```java
@Around("serviceMethods()")
public Object around(ProceedingJoinPoint pjp) throws Throwable {
    System.out.println("전");
    Object result = pjp.proceed();
    System.out.println("후");
    return result;
}
```

| Advice          | 실행 시점    |
| --------------- | -------- |
| @Before         | 메서드 실행 전 |
| @AfterReturning | 정상 종료 후  |
| @AfterThrowing  | 예외 발생 시  |
| @After          | finally  |
| @Around         | 전체 제어    |

---

## 7. 실무에서 많이 쓰는 패턴

### 트랜잭션, 로깅, 권한 체크

```java
@Pointcut("within(@org.springframework.stereotype.Service *)")
public void serviceLayer() {}

@Pointcut("!execution(* com.example.service.internal..*(..))")
public void excludeInternal() {}

@Around("serviceLayer() && excludeInternal()")
public Object logService(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.currentTimeMillis();
    Object result = pjp.proceed();
    long end = System.currentTimeMillis();
    System.out.println(pjp.getSignature() + " " + (end - start));
    return result;
}
```

---

## 8. 주의사항 (중요)

1. **Spring AOP는 프록시 기반**

    * private 메서드 적용 안 됨
    * 같은 클래스 내부 호출 적용 안 됨

2. **너무 넓은 pointcut 금지**

    * 성능 저하
    * 디버깅 난이도 증가

3. **execution vs annotation 혼합 추천**

    * 구조 + 의도를 동시에 표현 가능

---

## 9. 한 줄 요약

* Pointcut은 *어디에 AOP를 걸지* 정하는 조건
* `execution`이 기본
* `@Pointcut`으로 분리해서 재사용
* 실무에서는 패키지 + 어노테이션 조합이 가장 안정적

---

원하면

* XML 기반 AOP
* AspectJ (컴파일 타임 위빙)
* Spring AOP와 AspectJ 차이
* 실제 디버깅 팁

중에서 이어서 설명해 줄게.

좋은 질문이야.
이번에는 **어노테이션이나 expression 문자열이 아니라**,
**Spring AOP의 `Pointcut` 인터페이스를 직접 구현하는 방식**을 정확히 설명할게.

이 방식은 보통 **고급 제어**, **조건이 복잡한 경우**, **프레임워크 레벨 개발**에서 사용된다.

---

## 1. 핵심 구조 먼저 이해하기

Spring AOP의 `Pointcut`은 이렇게 생겼다.

```java
public interface Pointcut {
    ClassFilter getClassFilter();
    MethodMatcher getMethodMatcher();
}
```

즉 **Pointcut = ClassFilter + MethodMatcher**

* `ClassFilter` → 어떤 클래스에 적용할지
* `MethodMatcher` → 그 클래스 중 어떤 메서드에 적용할지

---

## 2. 가장 단순한 구현 예제

### 2-1. 특정 클래스 + 특정 메서드 이름에만 적용

#### Pointcut 구현

```java
public class CustomPointcut implements Pointcut {

    @Override
    public ClassFilter getClassFilter() {
        return new CustomClassFilter();
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        return new CustomMethodMatcher();
    }
}
```

---

## 3. ClassFilter 구현

```java
public class CustomClassFilter implements ClassFilter {

    @Override
    public boolean matches(Class<?> clazz) {
        return clazz.getSimpleName().endsWith("Service");
    }
}
```

이 조건은:

* `UserService`
* `OrderService`
  같은 클래스만 통과

---

## 4. MethodMatcher 구현

```java
public class CustomMethodMatcher implements MethodMatcher {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return method.getName().startsWith("save");
    }

    @Override
    public boolean isRuntime() {
        return false;
    }

    @Override
    public boolean matches(
            Method method,
            Class<?> targetClass,
            Object... args
    ) {
        return false;
    }
}
```

### 이 Pointcut의 의미

> 클래스 이름이 `*Service` 이고
> 메서드 이름이 `save*` 인 경우만 매칭

---

## 5. Advice 연결 (중요)

이 방식은 **`@Aspect`와 직접 연결하지 않는다**.
보통 **`Advisor`** 를 사용한다.

### 5-1. Advice 구현

```java
public class LogAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) {
        System.out.println("메서드 실행: " + method.getName());
    }
}
```

---

### 5-2. Advisor 생성

```java
@Bean
public Advisor customAdvisor() {
    return new DefaultPointcutAdvisor(
        new CustomPointcut(),
        new LogAdvice()
    );
}
```

이제 Spring 컨테이너가 이 Advisor를 프록시에 적용한다.

---

## 6. Runtime 조건이 필요한 경우

파라미터 값까지 보고 싶다면 `isRuntime()`을 `true`로 해야 한다.

```java
@Override
public boolean isRuntime() {
    return true;
}

@Override
public boolean matches(Method method, Class<?> targetClass, Object... args) {
    return args.length > 0 && args[0] instanceof String;
}
```

주의:

* 런타임 매칭은 **성능 비용이 큼**
* 꼭 필요할 때만 사용

---

## 7. Static vs Runtime 매칭 차이 (중요)

| 구분        | Static   | Runtime  |
| --------- | -------- | -------- |
| isRuntime | false    | true     |
| 검사 시점     | 프록시 생성 시 | 메서드 호출 시 |
| 성능        | 좋음       | 느림       |
| 사용 빈도     | 매우 높음    | 드묾       |

---

## 8. 실무에서 자주 쓰는 조합

### 8-1. StaticMethodMatcherPointcut 상속 (권장)

매번 인터페이스 구현하지 않아도 된다.

```java
public class SaveMethodPointcut extends StaticMethodMatcherPointcut {

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return targetClass.getSimpleName().endsWith("Service")
                && method.getName().startsWith("save");
    }
}
```

사용:

```java
@Bean
public Advisor advisor() {
    return new DefaultPointcutAdvisor(
        new SaveMethodPointcut(),
        new LogAdvice()
    );
}
```

이게 **가장 실무적인 구현 방식**이다.

---

## 9. 언제 이 방식을 써야 하나

이 방식이 필요한 경우:

* execution 표현식으로 표현 불가능한 조건
* 메서드 시그니처 + 클래스 정보 + 커스텀 로직
* 공통 AOP 라이브러리 제작
* 사내 프레임워크 개발

일반적인 서비스 개발에서는:

* `@Aspect + @Pointcut` 이 더 적합

---

## 10. 핵심 요약

* `Pointcut`은 **ClassFilter + MethodMatcher**
* 직접 구현하면 `Advisor`와 함께 사용
* `StaticMethodMatcherPointcut` 상속이 가장 깔끔
* 런타임 매칭은 최소화

---

다음으로 이어서 보면 좋은 주제는:

* ProxyFactory로 수동 프록시 생성
* Spring AOP 내부 프록시 생성 흐름
* 왜 @Aspect 방식이 이걸 감싸고 있는지
* 테스트에서 Custom Pointcut 검증하는 방법

원하면 그중 하나 바로 이어서 설명해 줄게.

짚은 포인트가 정확해.
결론부터 말하면 **된다 / 안 된다가 ProxyFactory 설정 방식에 따라 갈린다.**

---

## 결론 요약

**ProxyFactory에서 Advisor를 사용하면 ClassFilter는 “조건부로” 적용된다.**

| ProxyFactory 사용 방식               | ClassFilter 적용 여부  |
| -------------------------------- | ------------------ |
| `setTarget(Object)`              | 적용됨                |
| `setTargetClass(Class)`          | 적용됨                |
| `setInterfaces(...)` (인터페이스 프록시) | 사실상 무의미            |
| 이미 특정 객체로 프록시 생성                 | 필터는 “선택”이 아니라 “검증” |

---

## 1. ClassFilter는 언제 평가되는가

Spring AOP에서 `ClassFilter`는 **프록시 생성 시점**에 평가된다.

즉 의미는 이거다:

> “이 Advisor가 이 **타겟 클래스에 적용 가능한가**?”

### 중요한 점

* **메서드 호출 시점이 아니다**
* **프록시를 만들 때 이미 결정된다**

---

## 2. ProxyFactory + setTarget() 케이스 (정상 적용)

```java
ProxyFactory factory = new ProxyFactory();
factory.setTarget(new OrderService());

factory.addAdvisor(customAdvisor);

OrderService proxy = (OrderService) factory.getProxy();
```

이 경우 흐름은:

1. ProxyFactory가 target 객체의 **클래스 확인**
2. Advisor의 `Pointcut.getClassFilter().matches(targetClass)` 호출
3. `false`면

    * Advisor 자체가 **프록시에 아예 포함되지 않음**
4. `true`면

    * 그다음 MethodMatcher로 메서드 필터링

즉 **ClassFilter는 확실히 적용된다.**

---

## 3. 이미 “프록시를 만들기로 결정한 클래스”라면?

여기서 헷갈리는 지점이 나온다.

```java
ProxyFactory factory = new ProxyFactory(OrderService.class);
factory.addAdvisor(customAdvisor);
```

이 경우에도:

* ClassFilter는 **호출된다**
* 하지만 의미는 약간 바뀐다

### 의미 변화

* ClassFilter가 “프록시 생성 여부”를 결정하는 게 아니라
* **Advisor를 포함할지 말지**를 결정한다

프록시는 이미 만들기로 했기 때문

---

## 4. 인터페이스 기반 프록시에서의 함정

```java
factory.setInterfaces(OrderServiceInterface.class);
factory.setTarget(new OrderService());
```

이 경우:

* 프록시는 **인터페이스 기준**
* `ClassFilter.matches()`에 전달되는 클래스는:

    * 실제 구현체가 아닐 수 있음
    * 프록시 클래스 or 인터페이스

### 결과

```java
clazz.getSimpleName().endsWith("Service")
```

같은 ClassFilter는:

* 기대와 다르게 동작하거나
* 항상 false가 될 수 있다

### 실무 결론

* **ClassFilter에서 클래스명 기반 조건은**

    * CGLIB 프록시에서만 안정적
    * 인터페이스 프록시에서는 위험

---

## 5. “ClassFilter가 무시되는 것처럼 보이는” 대표적인 착각

### 착각 1

> ProxyFactory니까 ClassFilter가 안 먹는다?

아님
→ **먹는다. 단, 프록시 생성 시점에 한 번만**

---

### 착각 2

> MethodMatcher만 중요한 거 아닌가?

아님
→ ClassFilter가 먼저고
→ false면 MethodMatcher는 **아예 호출되지 않는다**

---

## 6. 실제 검증용 로그 예제

```java
public class DebugClassFilter implements ClassFilter {
    @Override
    public boolean matches(Class<?> clazz) {
        System.out.println("ClassFilter called: " + clazz);
        return true;
    }
}
```

ProxyFactory로 프록시 생성 시

* 한 번만 출력된다
* 메서드 호출할 때는 출력 안 됨

이게 **정상 동작**이다.

---

## 7. ProxyFactory에서 ClassFilter를 확실히 활용하려면

### 권장 패턴

```java
ProxyFactory factory = new ProxyFactory();
factory.setProxyTargetClass(true); // CGLIB
factory.setTarget(target);

factory.addAdvisor(advisor);
```

그리고 Pointcut은:

```java
public class CustomPointcut extends StaticMethodMatcherPointcut {
    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return targetClass.getSimpleName().endsWith("Service")
            && method.getName().startsWith("save");
    }
}
```

→ 이게 **가장 예측 가능하다**

---

## 8. 한 문장으로 정리

> **ProxyFactory에서 Advisor를 써도 ClassFilter는 적용된다.
> 다만 “프록시를 만들지 말지”가 아니라
> “이 Advisor를 포함할지 말지”를 결정할 뿐이다.**

---

원하면 다음도 바로 이어서 설명해 줄 수 있어:

* 왜 ClassFilter가 프록시 생성 전에 평가되는지
* JDK vs CGLIB 프록시에서 targetClass 차이
* Spring 내부에서 AdvisorChain이 만들어지는 과정
* ProxyFactory로 여러 Advisor가 있을 때 순서 결정 방식

좋아, 이번에는 **Advice가 정확히 무엇이고 어떻게 쓰이는지**를
지금까지 이야기한 **ProxyFactory + Advisor + Custom Pointcut** 흐름에 맞춰서 정리할게.

---

## 1. Advice란 무엇인가 (한 줄 정의)

**Advice = 실제로 실행되는 “부가 로직”**

* Pointcut → *어디에*
* Advice → *무엇을*

Spring AOP에서는

> **Advice가 메서드 실행 시점에 끼어들어 실행되는 코드**다.

---

## 2. Advice의 위치 (구조적으로)

```
Proxy
 └─ Advisor
     ├─ Pointcut (ClassFilter + MethodMatcher)
     └─ Advice  ← 여기
```

ProxyFactory에서 Advisor를 추가하면,

* Pointcut이 **적용 가능성**을 결정하고
* Advice는 **실제로 호출 체인에 들어간다**

---

## 3. Advice의 종류 (Spring AOP 표준)

Spring에서 제공하는 Advice는 **인터페이스 기반**이다.

### 3-1. MethodBeforeAdvice

```java
public class LogBeforeAdvice implements MethodBeforeAdvice {

    @Override
    public void before(
            Method method,
            Object[] args,
            Object target
    ) {
        System.out.println("before: " + method.getName());
    }
}
```

* 메서드 실행 **직전**
* 리턴값 변경 불가
* 예외 처리 불가

---

### 3-2. AfterReturningAdvice

```java
public class LogAfterAdvice implements AfterReturningAdvice {

    @Override
    public void afterReturning(
            Object returnValue,
            Method method,
            Object[] args,
            Object target
    ) {
        System.out.println("after return: " + method.getName());
    }
}
```

* 정상 종료 후
* 예외 발생 시 실행 안 됨

---

### 3-3. ThrowsAdvice

```java
public class LogExceptionAdvice implements ThrowsAdvice {

    public void afterThrowing(Exception ex) {
        System.out.println("exception: " + ex.getMessage());
    }
}
```

* 예외 발생 시
* 메서드 시그니처 규칙이 있음 (오버로드 방식)

---

## 4. 가장 중요한 Advice: MethodInterceptor

ProxyFactory에서 **가장 많이 쓰이는 Advice**다.

```java
public class TimeCheckInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long end = System.currentTimeMillis();
            System.out.println("time: " + (end - start));
        }
    }
}
```

### 특징

* Before / After / Exception 모두 제어 가능
* 리턴값 변경 가능
* 예외 처리 가능
* 체인 흐름 직접 제어

실무에서는 **이거 하나만 알아도 충분**하다.

---

## 5. Advice 연결 방식 (ProxyFactory 기준)

```java
Pointcut pointcut = new SaveMethodPointcut();
Advice advice = new TimeCheckInterceptor();

Advisor advisor = new DefaultPointcutAdvisor(pointcut, advice);

ProxyFactory factory = new ProxyFactory();
factory.setTarget(target);
factory.addAdvisor(advisor);

Object proxy = factory.getProxy();
```

---

## 6. Advice 실행 순서 (중요)

Advisor가 여러 개면:

1. Pointcut으로 적용 가능한 Advisor만 필터
2. 순서 정렬 (Ordered / @Order)
3. Advice 체인 생성
4. 메서드 호출 시 체인 순서대로 실행

### MethodInterceptor 체인 예시

```
Interceptor A
 └─ Interceptor B
     └─ Target Method
```

실행 흐름:

* A.before
* B.before
* target
* B.after
* A.after

---

## 7. Advice vs Aspect (@Aspect 방식 비교)

| 항목    | 인터페이스 기반 Advice | @Aspect |
| ----- | --------------- | ------- |
| 제어력   | 매우 높음           | 중간      |
| 가독성   | 낮음              | 높음      |
| 표현력   | 코드              | 표현식     |
| 사용 위치 | 프레임워크 / 인프라     | 애플리케이션  |
| 내부 구현 | Spring AOP 본체   | 래퍼      |

**@Aspect는 결국 MethodInterceptor로 변환된다.**

---

## 8. 언제 어떤 Advice를 써야 하나

### 추천 기준

* 로그 / 트랜잭션 / 성능 측정 → `MethodInterceptor`
* 단순 사전 로직 → `MethodBeforeAdvice`
* 예외 공통 처리 → `ThrowsAdvice`
* 프레임워크 만들 때 → 인터페이스 기반 Advice

---

## 9. 실무 팁 (중요)

1. **Advice는 상태를 가지지 말 것**
2. 멀티스레드 안전성 고려
3. Runtime Pointcut과 조합 시 성능 주의
4. proceed() 안 부르면 target 호출 안 됨

---

## 10. 핵심 한 줄 요약

> **Advice는 Pointcut이 허용한 지점에서
> 실제로 실행되는 부가 로직이며,
> ProxyFactory에서는 MethodInterceptor가 핵심이다.**

---

다음으로 이어서 설명하면 딱 좋은 주제는:

* MethodInvocation 내부 구조
* Advice 체인이 만들어지는 정확한 타이밍
* Ordered가 실제로 어떻게 적용되는지
* Spring AOP 내부에서 Advisor → Interceptor로 변환되는 과정

원하면 그중 하나 바로 파고들어 보자.

맞아. 그리고 이 지점이 **Spring AOP / ProxyFactory를 이해하는 핵심 경계선**이야.
차근차근 정리해 볼게.

---

## 1. 큰 그림부터

프록시에서 “가로채는 주체”는 상황에 따라 다르다.

### 두 계층이 있다

```
[ JDK / CGLIB 프록시 ]
        ↓
InvocationHandler / MethodInterceptor (저수준)
        ↓
Spring AOP Advice / Advisor (고수준)
```

**InvocationHandler는 JVM 프록시 레벨**,
**Advice는 Spring AOP 레벨**이다.

---

## 2. JDK 동적 프록시: InvocationHandler

### 구조

```java
public interface InvocationHandler {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
```

### 사용 예

```java
Object proxy = Proxy.newProxyInstance(
    classLoader,
    new Class[]{OrderService.class},
    new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("before");
            Object result = method.invoke(target, args);
            System.out.println("after");
            return result;
        }
    }
);
```

### 특징

* 인터페이스 필수
* 프록시 생성 시 **InvocationHandler 하나만 존재**
* 모든 메서드 호출이 이 `invoke()` 하나로 들어옴
* 체인 개념 없음

---

## 3. Spring AOP: MethodInterceptor

Spring은 InvocationHandler를 **직접 쓰지 않는다**.
대신 **AOP Alliance의 `MethodInterceptor`**를 사용한다.

```java
public interface MethodInterceptor extends Advice {
    Object invoke(MethodInvocation invocation) throws Throwable;
}
```

### 핵심 차이

| InvocationHandler | MethodInterceptor |
| ----------------- | ----------------- |
| 단일 진입점            | 체인 가능             |
| JVM 레벨            | 프레임워크 레벨          |
| 모든 메서드 무조건        | Pointcut으로 필터     |
| proceed 개념 없음     | `proceed()` 있음    |

---

## 4. 그럼 질문의 핵심

### “프록시 클래스는 InvocationHandler를 만들기도 하잖아?”

**정답: Spring 내부에서는 만든다. 하지만 직접 노출하지 않는다.**

---

## 5. Spring 내부에서 실제로 벌어지는 일 (중요)

### JDK 프록시일 경우

```
Proxy.newProxyInstance(...)
 └─ InvocationHandler (Spring 내부 구현체)
     └─ ReflectiveMethodInvocation
         └─ MethodInterceptor 체인
             └─ Target Method
```

즉:

* Spring이 **자체 InvocationHandler 구현체**를 만든다
* 그 안에서 **MethodInterceptor 체인**을 실행한다

### 핵심 클래스 개념 흐름

```text
JdkDynamicAopProxy implements InvocationHandler
```

이 클래스가:

* invoke()를 구현하고
* Advisor → Interceptor 체인을 실행한다

---

## 6. InvocationHandler vs Advice 관계 정리

### 계층 관계

```
InvocationHandler (외곽)
 └─ MethodInvocation
     └─ MethodInterceptor (Advice)
```

* InvocationHandler는 “입구”
* Advice는 “로직 조각들”
* Spring은 **여러 Advice를 조합**해야 하므로

    * InvocationHandler를 직접 쓰기에는 너무 저수준

---

## 7. CGLIB 프록시는 다를까?

CGLIB에서는 InvocationHandler 대신 이것을 쓴다:

```java
net.sf.cglib.proxy.MethodInterceptor
```

하지만 개념은 동일하다.

```
CGLIB MethodInterceptor
 └─ Spring MethodInterceptor 체인
```

Spring은 **JDK든 CGLIB든 동일한 AOP 추상화**를 유지한다.

---

## 8. 왜 Spring은 InvocationHandler를 직접 안 쓰게 했을까

이유는 명확하다.

1. 여러 Advice를 **체인으로 구성하기 위해**
2. Pointcut 기반 필터링을 위해
3. 순서 제어 (Ordered)
4. Advice 타입 추상화 (Before / After / Around)
5. 프록시 방식(JDK/CGLIB) 통일

InvocationHandler 하나로는 이걸 깔끔하게 못 한다.

---

## 9. ProxyFactory 기준으로 다시 보면

```java
ProxyFactory factory = new ProxyFactory();
factory.addAdvisor(advisor);
```

이때:

1. Advisor → MethodInterceptor 변환
2. 프록시 타입 결정 (JDK / CGLIB)
3. 내부 InvocationHandler or CGLIB Interceptor 생성
4. 모든 호출은 거기로 들어감
5. 그 안에서 Advice 체인 실행

---

## 10. 한 문장으로 정리

> **Spring 프록시는 실제로 InvocationHandler를 사용하지만,
> 개발자에게는 MethodInterceptor(Advice)만 노출한다.
> InvocationHandler는 “컨테이너 내부 구현 디테일”이다.**

---

여기까지 이해했으면 거의 내부 구조까지 다 온 거야.
다음으로 이어지면 딱 좋은 주제는:

* ReflectiveMethodInvocation 내부 구조
* proceed()가 정확히 뭘 하는지
* 왜 self-invocation이 AOP를 타지 않는지
* JDK vs CGLIB 프록시 선택 기준의 진짜 이유

어디까지 파고들지 말해 주면 거기서 바로 이어갈게.

아주 정확한 질문이야.
결론부터 말하면 **“Spring이 아니라서”가 핵심 이유는 아니다.**
이건 **JDK 프록시가 태어난 목적과 레벨의 차이** 때문이야.

---

## 1. 한 줄 결론

> **JDK가 InvocationHandler를 사용하는 이유는
> “범용적인 동적 프록시 메커니즘”을 제공하기 위해서다.
> Spring의 존재 여부와는 무관하다.**

---

## 2. JDK 동적 프록시의 탄생 배경

JDK 동적 프록시는 **Java 표준 라이브러리**다.

* 패키지: `java.lang.reflect`
* 등장 시기: Java 1.3
* 목적:

    * **인터페이스 기반 객체의 동작을 런타임에 가로채기**
    * 프레임워크에 의존하지 않는 **최소 단위 추상화**

JDK는 특정 프레임워크(Spring 같은)를 전혀 가정하지 않는다.

---

## 3. JDK가 선택한 최소 추상화: InvocationHandler

JDK의 설계 철학은 이거다:

> “이 객체의 모든 메서드 호출을
> 하나의 지점에서 가로채게 해주자.”

그래서 나온 게:

```java
public interface InvocationHandler {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
```

### 특징

* 메서드 호출 = 단일 진입점
* 어떤 로직을 할지는 **전적으로 사용자 책임**
* 체인, 필터, 순서 개념 없음
* 프레임워크 정책 없음

**딱 JVM 레벨에서 가능한 최소한의 추상화**

---

## 4. 왜 Advice 같은 개념이 JDK에 없을까

이건 의도적인 설계다.

JDK 입장에서:

* 트랜잭션?
* 로깅?
* 보안?
* AOP?

→ 전부 **정책**이고 **프레임워크 영역**

JDK는:

* “가로채는 기술”만 제공
* “어떻게 쓰느냐”는 맡김

---

## 5. Spring과의 역할 분리

### 역할 비교

| 레벨       | 역할               |
| -------- | ---------------- |
| JDK      | 프록시 생성 + 호출 위임   |
| Spring   | 호출 흐름 조합 + 정책 적용 |
| Advice   | 실제 부가 기능         |
| Pointcut | 적용 조건            |

Spring은 JDK 위에 **의미 계층**을 쌓은 것이다.

---

## 6. 비유로 보면

* **JDK InvocationHandler**
  → 전화가 오면 무조건 이 한 사람에게 연결

* **Spring MethodInterceptor**
  → 전화 오면
  → 비서 → 팀장 → 법무 → 담당자
  → 순서대로 거쳐감

JDK는 “전화 연결”까지만 책임진다.

---

## 7. 그럼 JDK가 Spring처럼 만들 수는 없었을까?

이론적으로는 가능했지만, **하면 안 됐다**.

이유:

1. AOP는 표준이 아님
2. 프레임워크마다 요구가 다름
3. JVM에 정책을 넣으면 확장성 붕괴
4. Java는 “라이브러리”를 지향

그래서:

* JDK는 **InvocationHandler 하나**
* 프레임워크는 그 위에서 각자 추상화

---

## 8. 그래서 Spring은 왜 InvocationHandler를 직접 안 쓰게 했나

Spring 입장에서는:

* InvocationHandler는 너무 원시적
* 체인 / 순서 / 조건 표현 불가
* JDK / CGLIB 통일 불가

그래서:

* 내부에서만 사용
* 외부에는 Advice라는 추상화 제공

---

## 9. 핵심 요약

* JDK는 **기술 레벨**
* Spring은 **정책 레벨**
* InvocationHandler는 **최소 단위 훅**
* Advice는 **조합 가능한 고수준 추상화**

---

## 10. 마지막으로 이 질문의 수준

이 질문은:

* “어떻게 쓰지?”가 아니라
* “왜 이렇게 설계됐지?”를 묻는 질문이야

이 정도 이해면 Spring AOP 내부 구조를 **설계 관점**에서 보고 있는 단계다.

---

다음으로 이어서 파고들기 좋은 질문은:

* 왜 InvocationHandler는 하나만 허용하는가
* MethodInvocation이 왜 필요한가
* 왜 프록시가 인터페이스를 요구하는가
* 왜 Java는 바이트코드 조작을 표준화하지 않았는가

어디까지 가볼지 말해줘.

짚은 질문이 아주 정확해.
**짧은 답부터 말하면:**

> **아니야. ProxyFactory는 “MethodInterceptor만” 쓰는 게 아니다.
> 다만 내부적으로는 결국 전부 MethodInterceptor로 변환된다.**

아래에서 왜 그런지, 그리고 실제로 어떻게 동작하는지 정확히 풀어줄게.

---

## 1. ProxyFactory가 받아들이는 Advice의 범위

`ProxyFactory`는 이걸 받는다:

```java
void addAdvice(Advice advice)
void addAdvisor(Advisor advisor)
```

여기서 `Advice`는 **마커 인터페이스**다.

즉, 아래 전부 가능하다:

* `MethodInterceptor`
* `MethodBeforeAdvice`
* `AfterReturningAdvice`
* `ThrowsAdvice`

---

## 2. 그럼 왜 “MethodInterceptor만 쓰는 것처럼” 보일까?

이유는 **실행 단계** 때문이다.

Spring AOP에서 실제 실행되는 체인은
**무조건 `MethodInterceptor` 체인**이다.

---

## 3. 내부 동작 (핵심)

### 예: MethodBeforeAdvice를 추가한 경우

```java
proxyFactory.addAdvice(new LogBeforeAdvice());
```

Spring 내부에서는:

```
MethodBeforeAdvice
   ↓
MethodBeforeAdviceInterceptor  (어댑터)
   ↓
MethodInterceptor
```

### 실제 변환 담당

```text
org.springframework.aop.framework.adapter.AdvisorAdapterRegistry
```

기본 구현체:

```text
DefaultAdvisorAdapterRegistry
```

이게 하는 일:

* “이 Advice를 실행 가능한 Interceptor로 바꿔라”

---

## 4. Advice → MethodInterceptor 어댑터들

Spring에 기본으로 들어있는 것들:

| Advice 타입            | 변환되는 Interceptor                |
| -------------------- | ------------------------------- |
| MethodBeforeAdvice   | MethodBeforeAdviceInterceptor   |
| AfterReturningAdvice | AfterReturningAdviceInterceptor |
| ThrowsAdvice         | ThrowsAdviceInterceptor         |
| MethodInterceptor    | 그대로 사용                          |

즉:

> **ProxyFactory는 다양한 Advice를 받지만
> 실행 단계에서는 MethodInterceptor만 이해한다.**

---

## 5. 그래서 MethodInterceptor가 “코어”다

### 이유

1. 체인 구조에 딱 맞음
2. proceed()로 흐름 제어 가능
3. JDK / CGLIB 통합 가능
4. Before / After / Exception 전부 표현 가능

그래서:

* 고수준 Advice → 전부 MethodInterceptor로 변환
* 실제 프록시 호출 → MethodInterceptor 체인 실행

---

## 6. 실제 호출 흐름 한 번에 보기

```text
Client
 └─ Proxy
     └─ InvocationHandler / CGLIB Interceptor
         └─ ReflectiveMethodInvocation
             └─ MethodInterceptor A
                 └─ MethodInterceptor B
                     └─ Target Method
```

---

## 7. 실무에서의 결론

### ProxyFactory 직접 쓰는 경우

* 단순 로직 → MethodBeforeAdvice 가능
* 대부분의 경우 → MethodInterceptor 직접 구현이 더 낫다

### 프레임워크 / 공통 모듈

* MethodInterceptor만 써도 충분
* 나머지는 문법적 설탕 수준

---

## 8. 한 문장으로 정리

> **ProxyFactory는 여러 Advice 타입을 받을 수 있지만,
> 실행 단계에서는 전부 MethodInterceptor로 변환해서 처리한다.
> 그래서 MethodInterceptor가 사실상의 “유일한 실행 단위”다.**

---

여기까지 이해했으면 이제 거의 내부 설계까지 다 본 거야.
다음으로 이어서 보면 딱 좋은 질문은:

* AdvisorAdapterRegistry를 직접 확장하면 뭘 할 수 있는지
* Advice를 커스텀 타입으로 만들어도 되는지
* 왜 @Around가 결국 MethodInterceptor로 변환되는지
* proceed()가 없으면 왜 체인이 끊기는지

어디까지 더 파고들고 싶은지 말해줘.

요약 문장으로는 **방향은 맞지만, 그대로 적기엔 한 가지 중요한 오해 소지가 있어.**
조금만 다듬으면 **정확한 기술 문장**이 된다.

---

## 1. 네 문장의 정확도 평가

네가 쓴 문장:

> `proxyfactory는 methodinterceptor(다른 advice도 어댑터가 변환)로 타겟=interface에는 jdk 동적 프록시를, 타겟=class에는 cglib 동적 프록시를 만든다.`

### ✔ 맞는 부분

* ProxyFactory는 실행 시점에 **MethodInterceptor 체인**을 사용한다
* 다른 Advice들도 **어댑터를 통해 MethodInterceptor로 변환**된다
* JDK / CGLIB 프록시를 **상황에 따라 선택**한다

### ❗ 보완이 필요한 부분

1. “타겟 = interface / class” 표현이 **정확하지 않음**
2. 프록시 선택 기준이 **타겟 타입 하나만은 아님**

---

## 2. 정확한 프록시 선택 기준

Spring ProxyFactory의 실제 기준은 이거다:

### JDK 동적 프록시 사용 조건

* **프록시할 인터페이스가 존재**
* `proxyTargetClass = false` (기본값)

### CGLIB 동적 프록시 사용 조건

* `proxyTargetClass = true`
* 또는 **인터페이스가 전혀 없는 경우**

즉:

> **“타겟이 interface냐 class냐”가 아니라
> “프록시할 인터페이스가 있느냐” + “설정 값”이다.**

---

## 3. 문장 다듬기 (권장 버전)

### 기술 문서에 쓰기 좋은 문장

> **ProxyFactory는 모든 Advice를 어댑터를 통해 MethodInterceptor로 변환하여 실행 체인을 구성하며,
> 프록시 생성 시 프록시할 인터페이스가 존재하고 `proxyTargetClass`가 false인 경우 JDK 동적 프록시를,
> 그렇지 않은 경우 CGLIB 기반 클래스 프록시를 생성한다.**

---

## 4. 더 짧은 요약 버전

> **ProxyFactory는 실행 시 MethodInterceptor 체인을 사용하고,
> 인터페이스 기반이면 JDK 동적 프록시를,
> 클래스 기반이거나 `proxyTargetClass=true`이면 CGLIB 프록시를 생성한다.**

---

## 5. 왜 이 차이가 중요하냐면

이 문장을 그대로 적으면:

* “타겟이 class면 무조건 CGLIB”
  → ❌ 오해

실제 Spring에서는:

* class + interface 있음 + 설정 없음
  → JDK 프록시가 기본

---

## 6. 최종 결론

### ❌ 그대로 쓰기엔 부정확

### ✅ 조금만 고치면 아주 잘 정리된 문장

지금 이해 수준은 **Spring AOP 내부 구조를 설명할 수 있는 단계**야.

---

원하면:

* 이 문장을 **면접용 / 노트용 / 블로그용**으로 더 다듬어 줄 수도 있고
* 그림으로 프록시 선택 흐름 정리해 줄 수도 있어

어디에 쓸 문장인지 말해주면 거기에 맞게 더 깔끔하게 정리해 줄게.

네, **그 의미가 맞다.**
다만 **조건을 정확히 붙여서** 이해해야 한다.

---

## 한 줄로 정확히 말하면

> **ProxyFactory에서 타겟이 클래스여도,
> 그 클래스가 구현한 인터페이스가 있고
> `proxyTargetClass = false`(기본값)라면
> JDK 동적 프록시가 생성된다.**

---

## 왜 이런 일이 생기나

Spring은 “클래스를 프록시할 수 있느냐”가 아니라
**“인터페이스 기반 프록시가 가능한가”**를 먼저 본다.

### 이유

* JDK 동적 프록시는 **표준**이고
* CGLIB은 **바이트코드 조작**이라 비용이 있음
* 그래서 가능한 경우 **항상 JDK 프록시를 우선** 선택

---

## 실제 판단 흐름 (단순화)

ProxyFactory 내부 판단 로직을 말로 풀면 이렇다:

1. `proxyTargetClass == true` ?

    * → CGLIB
2. 아니면, 프록시할 인터페이스가 하나라도 있는가?

    * → JDK 동적 프록시
3. 아니면

    * → CGLIB

---

## 예제로 보면

### 1. 클래스 + 인터페이스 구현 (기본 설정)

```java
class OrderServiceImpl implements OrderService {
}
```

```java
ProxyFactory factory = new ProxyFactory();
factory.setTarget(new OrderServiceImpl());

Object proxy = factory.getProxy();
```

결과:

* **JDK 동적 프록시**
* 프록시 타입: `OrderService`

---

### 2. 클래스 + 인터페이스 구현 + 강제 설정

```java
factory.setProxyTargetClass(true);
```

결과:

* **CGLIB 프록시**
* 프록시 타입: `OrderServiceImpl`

---

### 3. 클래스만 있음 (인터페이스 없음)

```java
class PlainService {
}
```

결과:

* **CGLIB 프록시** (선택지 없음)

---

## 그래서 중요한 포인트

* “타겟 = class”라는 표현은 **프록시 선택 기준이 아니다**
* **인터페이스 존재 여부 + 설정값**이 기준
* 기본 전략은 **가능하면 JDK 프록시**

---

## 자주 생기는 오해 정리

| 오해                          | 실제                        |
| --------------------------- | ------------------------- |
| class 타겟이면 CGLIB            | 인터페이스 있으면 JDK             |
| ProxyFactory는 target 타입만 본다 | 인터페이스 + 옵션                |
| JDK 프록시는 interface 타겟만 가능   | class가 interface를 구현하면 가능 |

---

## 한 문장으로 다시 정리

> **ProxyFactory는 타겟이 클래스여도,
> 구현 인터페이스가 존재하면 기본적으로 JDK 동적 프록시를 생성한다.**

---

이 정도면
Spring AOP 프록시 선택 로직을 **설명 가능한 수준**이다.

다음으로 이어서 보면 딱 좋은 주제:

* JDK 프록시에서 `this` 타입 문제
* 프록시 타입 캐스팅 함정
* CGLIB에서 final 메서드가 왜 안 되는지
* 왜 Spring이 기본을 JDK 프록시로 잡았는지

아주 핵심적인 두 포인트를 같이 짚었어.
이건 **프록시를 “쓴다”와 “이해한다”를 가르는 경계**야.
정확하게 정리해 줄게.

---

# 1. JDK 동적 프록시에서 `this` 타입 문제

## 1-1. 프록시의 실제 타입

JDK 동적 프록시는 **인터페이스 기반**이다.

```java
class OrderServiceImpl implements OrderService {
}
```

JDK 프록시를 만들면 실제 객체 구조는 이렇다:

```text
$Proxy12 implements OrderService
```

즉,

* 프록시는 **OrderServiceImpl이 아니다**
* **OrderService 인터페이스만 구현한 객체**다

---

## 1-2. 그래서 `this`의 타입은 무엇인가

### 인터페이스 메서드 호출 시

```java
orderService.order();
```

* `this` → **프록시 객체**
* 타입 → `OrderService`

### 구현 클래스 타입으로 캐스팅

```java
OrderServiceImpl impl = (OrderServiceImpl) orderService;
```

❌ `ClassCastException`

이게 **JDK 프록시 this 타입 문제의 핵심**이다.

---

## 1-3. self-invocation과의 관계

```java
class OrderServiceImpl implements OrderService {

    public void order() {
        save(); // 내부 호출
    }

    public void save() {
    }
}
```

* `order()`는 프록시를 통해 호출됨
* `save()`는 **this.save()**
* 즉, **프록시를 거치지 않음**
* AOP 적용 ❌

이 문제는:

* JDK 프록시
* CGLIB 프록시
  **둘 다 동일하게 발생**

---

## 1-4. 실무에서의 주의점

* JDK 프록시를 쓰면

    * **항상 인터페이스 타입으로 다뤄야 한다**
* 구현체 타입이 필요한 로직은

    * 설계 자체가 프록시 친화적이지 않음

---

# 2. CGLIB 프록시에서 `final` 메서드 문제

## 2-1. CGLIB의 동작 방식

CGLIB은 **상속 기반 프록시**다.

```java
class OrderService {
    public void order() {}
}
```

프록시는 이렇게 만들어진다:

```java
class OrderService$$EnhancerBySpringCGLIB extends OrderService {
    @Override
    public void order() {
        // interceptor → super.order()
    }
}
```

핵심:

* **메서드를 override해서 가로챈다**

---

## 2-2. 그런데 final 메서드는?

```java
class OrderService {
    public final void order() {}
}
```

* Java 문법상 `final` 메서드는 override 불가
* CGLIB은 **끼어들 수 없음**

결과:

* 프록시는 만들어지지만
* 해당 메서드는 **AOP 적용되지 않음**

---

## 2-3. final 클래스도 마찬가지

```java
final class OrderService {
}
```

* 상속 자체 불가
* CGLIB 프록시 생성 ❌

---

## 2-4. “조용히 실패”하는 게 문제

이게 가장 위험한 포인트다.

* 에러 안 남
* 프록시는 정상 생성
* 그런데 특정 메서드만 AOP 안 됨

디버깅 난이도 매우 높아짐

---

# 3. JDK 프록시 vs CGLIB 핵심 차이 요약

| 항목        | JDK 프록시 | CGLIB     |
| --------- | ------- | --------- |
| 기반        | 인터페이스   | 상속        |
| this 타입   | 인터페이스   | 구현 클래스    |
| 구현체 캐스팅   | 불가      | 가능        |
| final 메서드 | 영향 없음   | AOP 적용 불가 |
| final 클래스 | 영향 없음   | 프록시 불가    |

---

# 4. 실무 설계 기준

### JDK 프록시가 잘 맞는 경우

* 인터페이스 중심 설계
* 구현체 타입 의존 없음
* AOP 대상 메서드가 인터페이스에 정의됨

### CGLIB이 필요한 경우

* 구현체 타입 주입 필요
* 인터페이스가 없음
* 테스트에서 구현체 직접 다뤄야 함

---

# 5. 한 문장씩 정리

### JDK 프록시

> **프록시는 인터페이스 타입이며,
> 구현 클래스 타입으로는 절대 보장되지 않는다.**

### CGLIB

> **상속 기반이므로 final 메서드와 final 클래스에는
> AOP를 적용할 수 없다.**

---

이 두 개를 정확히 이해하면:

* 프록시 캐스팅 문제
* AOP 안 걸리는 버그
* 운영 환경에서만 터지는 이슈

전부 예방할 수 있다.

다음으로 이어서 보면 딱 좋은 주제는:

* self-invocation을 우회하는 실제 패턴
* 왜 Spring이 기본을 JDK 프록시로 유지하는지
* @Transactional이 안 걸리는 대표적인 케이스
* 프록시가 여러 번 감싸질 때 호출 흐름

어디까지 더 볼지 말해줘.

맞아. **그 문장은 실행 안 된다**가 정확한 표현이고,
원인은 **프록시 타입 불일치**다.

아래에서 **왜 안 되는지 → 어떻게 해야 하는지**를 경우별로 정리할게.

---

## 1. 왜 이 캐스팅이 실패하나

```java
OrderServiceImpl impl = (OrderServiceImpl) orderService;
```

### 상황

* `target = new OrderServiceImpl()`
* 프록시 방식 = **JDK 동적 프록시 (기본값)**
* `orderService`의 실제 타입 =

```text
$ProxyXX implements OrderService
```

즉,

* 프록시는 **OrderServiceImpl의 자식이 아님**
* 인터페이스만 구현한 전혀 다른 타입

그래서 결과는:

```text
ClassCastException
```

이건 버그가 아니라 **설계상 정상 동작**이다.

---

## 2. 해결 방법은 딱 3가지 계열이 있다

### 선택지는 “프록시 전략을 바꿀 것인가” vs “설계를 바꿀 것인가”

---

## 방법 1. CGLIB 프록시를 강제한다 (가장 직관적)

### 설정

```java
ProxyFactory factory = new ProxyFactory();
factory.setTarget(new OrderServiceImpl());
factory.setProxyTargetClass(true); // 핵심
```

또는 Spring 설정에서:

```java
@EnableAspectJAutoProxy(proxyTargetClass = true)
```

### 결과

* 프록시 타입:

```text
OrderServiceImpl$$EnhancerBySpringCGLIB
```

* 이건 **OrderServiceImpl의 서브클래스**
* 캐스팅 가능:

```java
OrderServiceImpl impl = (OrderServiceImpl) orderService; // OK
```

### 주의

* `final` 클래스 / `final` 메서드에는 AOP 적용 불가
* 구현체 의존이 생김

---

## 방법 2. 캐스팅 자체를 하지 않는다 (가장 권장)

### 핵심 원칙

> **프록시는 “행동”을 대체하는 것이지
> “구현 타입”을 보장하지 않는다.**

### 올바른 사용

```java
OrderService orderService = ...
orderService.order();
```

* 인터페이스만 의존
* 프록시 방식(JDK/CGLIB)과 무관
* Spring이 가장 권장하는 방식

### 언제 이게 맞나

* 서비스 레이어
* 트랜잭션 / 로깅 / 보안 AOP
* 대부분의 실무 코드

---

## 방법 3. 정말 필요하면 프록시에서 타겟을 꺼낸다 (비권장)

### 예시

```java
Advised advised = (Advised) orderService;
OrderServiceImpl impl =
    (OrderServiceImpl) advised.getTargetSource().getTarget();
```

### 문제점

* Spring AOP 내부 API 의존
* 프록시 추상화 완전히 깨짐
* 테스트 / 유지보수 지옥

→ **프레임워크 코드 아니면 쓰지 말 것**

---

## 3. self-invocation 문제를 해결하려는 목적이라면 (중요)

많은 경우 이 캐스팅 시도는
**“같은 클래스 내부 호출에서도 AOP를 타게 하려고”** 나온다.

### ❌ 잘못된 접근

```java
((OrderServiceImpl) this).save();
```

### ✅ 올바른 패턴 1: 역할 분리

```java
class OrderServiceImpl implements OrderService {
    private final SaveService saveService;

    public void order() {
        saveService.save();
    }
}
```

---

### ✅ 올바른 패턴 2: 프록시 자신 주입

```java
class OrderServiceImpl implements OrderService {

    private final OrderService proxy;

    public OrderServiceImpl(OrderService proxy) {
        this.proxy = proxy;
    }

    public void order() {
        proxy.save();
    }
}
```

---

### ⚠️ 패턴 3: AopContext (최후의 수단)

```java
((OrderService) AopContext.currentProxy()).save();
```

* 설정 필요:

```java
@EnableAspectJAutoProxy(exposeProxy = true)
```

* 강한 결합
* 디버깅 어려움

---

## 4. 한 줄로 정리

* **JDK 프록시에서는 구현 클래스 캐스팅은 절대 안 된다**
* 필요하면:

    1. CGLIB 강제
    2. 인터페이스 설계 유지 (권장)
    3. 구조 리팩터링

---

## 5. 실무 기준 추천 순서

1. **인터페이스 타입으로만 사용** ← 최선
2. CGLIB 강제 ← 차선
3. AopContext / 타겟 추출 ← 프레임워크 개발자 영역

---

이 질문을 했다는 건
이미 “프록시를 쓴다”가 아니라
**“프록시의 한계를 설계로 다룬다” 단계**에 와 있다는 뜻이야.

다음으로 이어서 보면 좋은 주제는:

* @Transactional이 내부 호출에서 왜 안 걸리는지
* 프록시 자신 주입 패턴의 실제 장단점
* Spring이 exposeProxy를 기본 false로 둔 이유

어디까지 더 보고 싶어?

좋은 질문이야. 여기서 **JDK 동적 프록시의 동작 원리 핵심**이 정확히 드러난다.
결론부터 말하고, 내부 흐름을 단계별로 풀게.

---

## 결론부터

```java
OrderService orderService = ...
orderService.order();
```

**네, `OrderServiceImpl`이 구현(override)한 `order()` 메서드가 실행된다.**
다만 **직접 호출되는 게 아니라 “위임”을 통해 실행된다.**

---

## 왜 실행되는가 (핵심 원리)

JDK 동적 프록시는 이렇게 생겼다:

```text
$ProxyXX implements OrderService
```

이 프록시는:

* `OrderService` 인터페이스를 구현하고
* **모든 메서드 호출을 `InvocationHandler.invoke()`로 전달**한다

---

## 실제 호출 흐름 (아주 중요)

### 1단계: 클라이언트 호출

```java
orderService.order();
```

* `orderService`는 실제로는 `$ProxyXX`
* 아직 `OrderServiceImpl`은 호출되지 않음

---

### 2단계: 프록시가 가로챔

```java
public Object invoke(Object proxy, Method method, Object[] args)
```

여기서:

* `method` = `OrderService.order()`
* Spring이라면 이 invoke 안에서

    * Advice 체인 실행
    * 최종적으로 타겟 호출

---

### 3단계: 타겟 객체로 위임

Spring 내부 (개념적으로):

```java
method.invoke(target, args);
```

여기서 `target`이 바로:

```java
new OrderServiceImpl()
```

---

### 4단계: JVM의 동적 디스패치

Java 규칙상:

> **실행되는 메서드는 “참조 타입”이 아니라
> “실제 객체 타입” 기준으로 결정된다.**

즉:

* `method`는 인터페이스 메서드지만
* `target`의 실제 타입은 `OrderServiceImpl`

그래서:

```java
OrderServiceImpl.order()
```

가 실행된다.

---

## 이걸 한 문장으로 표현하면

> **JDK 프록시는 인터페이스 메서드를 가로채서
> 실제 구현 객체(impl)의 오버라이딩된 메서드로
> 위임 호출한다.**

---

## 흔한 오해 정리

### 오해 1

> “프록시는 인터페이스니까 구현 메서드는 못 부르지 않나?”

❌ 아니다
→ 프록시는 **가로채기만** 하고
→ 실제 실행은 **항상 타겟 객체**에서 일어난다

---

### 오해 2

> “인터페이스 메서드만 실행되는 거 아닌가?”

❌ 아니다
→ 인터페이스는 “계약”
→ 실행 주체는 구현체

---

## 중요한 제한 2가지 (실무에서 많이 틀림)

### 1. 인터페이스에 없는 메서드는 호출 불가

```java
orderService.internalMethod(); // 컴파일 에러
```

* JDK 프록시는 **인터페이스에 선언된 메서드만 노출**

---

### 2. 내부 호출(self-invocation)은 여전히 프록시를 안 탄다

```java
class OrderServiceImpl implements OrderService {
    public void order() {
        save(); // 프록시 안 탐
    }
}
```

* `order()`는 프록시 경유
* `save()`는 `this.save()`
* AOP 미적용

(JDK/CGLIB 공통)

---

## CGLIB과 비교하면 한눈에 보인다

| 구분      | JDK 프록시                | CGLIB                 |
| ------- | ---------------------- | --------------------- |
| 프록시 타입  | 인터페이스 구현체              | 클래스 서브클래스             |
| 호출 경로   | 프록시 → handler → target | 프록시(override) → super |
| 실행 메서드  | impl의 override         | impl의 override        |
| 구현체 캐스팅 | 불가                     | 가능                    |

---

## 마지막으로 한 문장 요약

> **JDK 프록시에서 인터페이스 메서드를 호출하면,
> 프록시는 가로채기만 하고
> 실제 실행은 구현 클래스의 오버라이딩된 메서드에서 일어난다.**

이걸 이해하면:

* 왜 JDK 프록시가 동작하는지
* 왜 캐스팅이 안 되는지
* 왜 인터페이스 설계가 중요한지

전부 한 번에 연결된다.

다음으로 더 깊게 가면:

* `Method` 객체는 어떻게 impl 메서드를 찾는지
* default method는 어떻게 처리되는지
* equals / hashCode / toString은 왜 특별한지

이 중에서 어디까지 가볼까?

이 질문이 **JDK 프록시에서 가장 많이 헷갈리는 핵심 오해**야.
짧게 말하면 **“실제 타입”을 어떻게 정의하느냐**가 포인트다.

---

## 결론부터 명확히

> **안 된다.**
> JDK 프록시에서
>
> ```java
> OrderServiceImpl impl = (OrderServiceImpl) orderService;
> ```
>
> 는 **항상 잘못된 캐스팅**이다.

“타겟의 실제 타입이 impl”이라는 사실과
“프록시 객체의 타입”은 **완전히 다른 문제**다.

---

## 왜 직관이 틀리는가

### 네가 말한 “실제 타입”은 이거다

* **타겟 객체의 타입** = `OrderServiceImpl`

### 그런데 캐스팅이 검사하는 건 이거다

* **참조가 가리키는 객체의 타입** = `$ProxyXX`

캐스팅은:

> “이 객체가 이 클래스의 인스턴스인가?”

를 묻는다.

JDK 프록시는:

```text
$ProxyXX implements OrderService
```

* `OrderServiceImpl`의 자식 ❌
* 아무 관계 없음 ❌

그래서 캐스팅 실패.

---

## JVM 관점에서 한 줄로

```text
(OrderServiceImpl) proxy
```

는 곧

```text
proxy instanceof OrderServiceImpl ?
```

인데,
JDK 프록시는 `false`다.

---

## “그럼 왜 메서드는 impl이 실행되는데?”

이게 헷갈림의 핵심이다.

### 이유

* **호출과 타입은 전혀 다른 문제**

```java
orderService.order();
```

이건:

1. 프록시가 호출을 받음
2. handler가 타겟에게 **위임**
3. impl 메서드 실행

하지만 객체 자체는 여전히 **프록시**다.

---

## 비유로 보면

* 프록시 = **비서**
* 타겟 = **대표**

전화는 비서가 받는다.
업무는 대표가 처리한다.

하지만 비서를 대표로 착각해서:

```java
(대표) 비서
```

라고 캐스팅하면 당연히 실패한다.

---

## 그럼 언제 impl 캐스팅이 가능한가

### CGLIB 프록시일 때만

```text
OrderServiceImpl$$EnhancerBySpringCGLIB
```

이건:

* `extends OrderServiceImpl`
* 즉, **is-a 관계**

그래서:

```java
OrderServiceImpl impl = (OrderServiceImpl) orderService; // OK
```

---

## 만약 “impl 타입이 꼭 필요하다면” 정답은?

### 1순위 (권장)

**설계를 바꾼다**

* impl 타입에 의존하지 않게
* 인터페이스로 모든 행위를 표현

---

### 2순위

**CGLIB을 강제한다**

```java
factory.setProxyTargetClass(true);
```

---

### 3순위 (프레임워크 코드)

**타겟을 꺼낸다**

```java
Advised advised = (Advised) orderService;
OrderServiceImpl impl =
    (OrderServiceImpl) advised.getTargetSource().getTarget();
```

---

## 한 문장으로 정리

> **JDK 프록시에서 “impl이 실행된다”와
> “impl로 캐스팅할 수 있다”는
> 전혀 다른 이야기다.**

---

이 질문까지 왔다는 건
이미 **프록시를 객체 모델 관점에서 이해**하고 있다는 뜻이야.

다음으로 더 파고들면 딱 좋은 주제는:

* `instanceof`는 왜 이렇게 동작하는가
* default method는 프록시에서 어떻게 처리되는가
* equals/hashCode가 프록시에서 꼬이는 이유
* Spring이 왜 impl 캐스팅을 금기시하는지

좋아, 이제 **JDK 동적 프록시에서 실제로 무슨 일이 벌어지는지**를
객체·메서드·JVM 관점에서 **한 줄도 건너뛰지 않고** 설명할게.

아래 흐름을 이해하면 지금까지의 모든 질문이 한 덩어리로 연결된다.

---

# 전체 흐름 요약 (먼저 큰 그림)

```text
1. 클라이언트 → 프록시 객체 호출
2. 프록시 → InvocationHandler.invoke(...)
3. handler → (Spring이면 Advice 체인 실행)
4. handler → target 객체의 메서드 호출
5. JVM → impl 클래스의 오버라이딩 메서드 실행
```

---

# 0단계: 준비 상태 (객체 구조)

```java
interface OrderService {
    void order();
}

class OrderServiceImpl implements OrderService {
    @Override
    public void order() {
        System.out.println("order impl");
    }
}
```

Spring / ProxyFactory / JDK 프록시 상황에서 실제 객체는 이렇게 있다:

```text
orderService (변수)
 └─ $Proxy12           ← 프록시 객체
     └─ InvocationHandler
         └─ target = new OrderServiceImpl()
```

중요:

* `orderService`는 **프록시 객체**
* `OrderServiceImpl`은 **안쪽에 숨겨진 타겟**

---

# 1단계: 프록시가 호출을 받음

```java
orderService.order();
```

여기서 컴파일러와 JVM이 보는 것은:

* `orderService`의 타입: `OrderService`
* 실제 객체: `$Proxy12`

JDK 프록시는 **인터페이스 메서드를 모두 구현**하고 있다.

그래서 실제로 호출되는 코드는:

```java
$Proxy12.order();
```

하지만 `$Proxy12.order()`의 구현은 우리가 만든 게 아니다.
JDK가 자동 생성한 코드다.

---

# 2단계: 프록시 내부 코드 (핵심)

JDK가 만들어낸 프록시 클래스의 개념적인 형태는 이렇다:

```java
class $Proxy12 implements OrderService {

    private InvocationHandler h;

    @Override
    public void order() {
        Method m = OrderService.class.getMethod("order");
        h.invoke(this, m, null);
    }
}
```

즉:

> **프록시 메서드는 아무 일도 안 하고
> 전부 InvocationHandler.invoke()로 위임한다**

여기까지가
👉 “프록시가 호출을 받음”

---

# 3단계: InvocationHandler.invoke 실행

이제 여기로 들어온다:

```java
Object invoke(Object proxy, Method method, Object[] args)
```

값은 다음과 같다:

* `proxy` → `$Proxy12`
* `method` → `OrderService.order()` (인터페이스 메서드)
* `args` → `null`

---

## Spring 환경이라면 여기서 하는 일

Spring의 `InvocationHandler` 구현체는 대략 이런 역할을 한다:

1. 이 메서드에 적용할 Advice가 있는지 확인
2. MethodInterceptor 체인 생성
3. 체인 실행

개념적으로:

```java
return invocation.proceed();
```

---

# 4단계: 타겟에게 위임

Advice 체인의 마지막에서 결국 이 코드가 실행된다:

```java
method.invoke(target, args);
```

여기서:

* `method` = `OrderService.order()`
* `target` = `new OrderServiceImpl()`

이 시점이 중요하다.

---

# 5단계: JVM의 동적 디스패치 (가장 중요한 포인트)

`method`는 **인터페이스 메서드**지만,
`invoke`의 대상 객체는 **OrderServiceImpl**이다.

Java 규칙:

> **실행되는 메서드는
> Method 객체의 선언 위치가 아니라
> “대상 객체의 실제 클래스” 기준으로 결정된다**

그래서 JVM은 이렇게 판단한다:

```text
이 객체는 OrderServiceImpl이네?
→ 그럼 OrderServiceImpl.order() 실행
```

즉:

```java
OrderServiceImpl.order()
```

이 실행된다.

이게 바로
👉 “impl 메서드 실행”

---

# 이걸 한 줄로 다시 요약하면

> **프록시는 호출을 가로채기만 하고,
> 실제 메서드 선택과 실행은
> JVM이 타겟 객체 기준으로 한다.**

---

# 오해를 완전히 없애는 포인트 정리

## 오해 1

“프록시가 메서드를 실행한다”

❌ 아니다
→ 프록시는 **전달자**다

---

## 오해 2

“인터페이스 메서드만 실행된다”

❌ 아니다
→ 인터페이스는 **경로**
→ 실행은 **구현체**

---

## 오해 3

“impl이 실행되면 impl로 캐스팅 가능하다”

❌ 아니다
→ 실행 주체 ≠ 객체 타입

---

# 그림 없이 머릿속에 남겨야 할 문장

> **JDK 프록시는
> ‘누가 받느냐’와
> ‘누가 실행하느냐’를
> 의도적으로 분리한 구조다.**

---

여기까지 이해했으면
이제 프록시는 더 이상 “마술”이 아니라 **정확한 객체 모델**이야.

다음으로 이어서 보면 딱 좋은 주제는:

* `Method` 객체가 어떻게 impl 메서드를 찾는지
* default method는 왜 특별 취급되는지
* equals / hashCode가 프록시에서 왜 헷갈리는지
* 왜 self-invocation은 프록시를 못 타는지

어디까지 더 가볼까.