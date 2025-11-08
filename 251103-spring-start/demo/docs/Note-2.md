Spring AOP에서 `@Aspect`를 사용한 방식은 프록시 기반 AOP예요. 즉, `@EnableAspectJAutoProxy`를 쓰는 순간 Spring은 내부적으로 JDK 동적 프록시 또는 CGLIB 프록시를 생성해서 AOP를 적용합니다. 그래서 질문하신 것처럼 “프록시 없이” AOP를 적용하는 건 불가능합니다.
### 왜 프록시가 필요한가?
- `@Aspect` 기반 AOP는 관심사를 분리하기 위해 메서드 호출을 가로채야 해요.
- 이 가로채는 동작은 프록시 객체를 통해 이루어집니다.
- 프록시 없이 메서드 호출을 가로채는 건 자바 언어 차원에서는 불가능하기 때문에, Spring은 프록시를 써요.
### 대안이 있을까?
프록시 없이 AOP처럼 동작하려면 다음과 같은 방법을 고려할 수 있지만, 완전히 같은 건 아니에요:

| 방법 | 설명 |
|------|------|
| AspectJ 컴파일 타임 위빙 | 자바 바이트코드를 직접 수정해서 AOP를 적용. 프록시가 아니라 직접 클래스에 코드 삽입 |
| 직접 메서드 호출 래핑 | AOP 없이 직접 메서드 호출 전에 로깅 등 삽입. 유연성은 낮고 재사용 어려움 |
| 스프링 이벤트 리스너 | 특정 이벤트에 반응하는 방식으로 관심사 분리. AOP와는 목적이 다름 |

- `@Aspect`를 쓰는 순간 Spring은 프록시를 반드시 사용합니다.
- 프록시 없이 AOP를 구현하려면 AspectJ 컴파일 타임 위빙 같은 다른 기술을 써야 해요.
- 하지만 Spring Boot나 Spring Framework에서는 대부분의 경우 프록시 기반 AOP가 가장 간편하고 안정적입니다.
`@Pointcut`은 Advice의 실행 시점과는 무관하게 단순히 대상 메서드를 지정하는 역할만 해요. 즉, `@Before`, `@After`, `@Around` 등 어떤 Advice에서든 같은 Pointcut을 공유할 수 있어요.

### 핵심 개념 정리

| 애너테이션 | 실행 시점 | 설명 |
|------------|------------|------|
| `@Before`  | 메서드 실행 직전 | 대상 메서드가 호출되기 전에 실행됨 |
| `@After`   | 메서드 실행 직후 | 대상 메서드가 정상/예외 관계없이 끝난 후 실행됨 |
| `@AfterReturning` | 메서드가 정상적으로 반환된 후 | 예외 없이 끝났을 때만 실행됨 |
| `@AfterThrowing` | 메서드가 예외를 던진 후 | 예외 발생 시 실행됨 |
| `@Around`  | 메서드 실행 전후 모두 | 직접 메서드 실행을 제어할 수 있음 |

### 왜 `@After`가 `@Before`처럼 보일까?
1. 대상 메서드가 너무 빨리 끝나서 차이를 못 느끼는 경우
    - `getName()` 같은 단순 getter는 실행 시간이 거의 없기 때문에 `@Before`와 `@After`가 거의 동시에 출력됨
2. Advice 출력 순서가 JVM 스케줄링에 따라 바뀔 수 있음
    - 콘솔 출력 순서가 실제 실행 순서와 다르게 보일 수 있어요
3. 예외 발생 여부에 따라 `@After`가 실행되지 않을 수도 있음
    - `@After`는 예외가 발생해도 실행되지만, `@AfterReturning`은 예외가 발생하면 실행되지 않음
### 확인 방법
```java
@Before("allGetters()")
public void beforeAdvice() {
    System.out.println("Before Advice");
}
@After("allGetters()")
public void afterAdvice() {
    System.out.println("After Advice");
}
```
그리고 `getName()`을 호출하는 코드를 실행해보면:
```java
System.out.println(obj.getName());
```
출력 결과는 보통:
```
Before Advice
After Advice
t1
```
이런 식으로 나와야 정상이에요. 만약 순서가 이상하다면 `System.out.println()`이 버퍼링되거나, 로그가 섞여서 보일 수도 있어요.
`getName()` 호출 시 출력되는 순서가:
```
LoggingAspect.LoggingAdvice run   // @Before
LoggingAspect.ThirdAdvice run     // @After (Circle 전체 메서드 대상)
LoggingAspect.SecondAdvice run    // @After (get*() 대상)
```
이 순서가 나오는 이유는 Advice 실행 시점과 Pointcut 범위의 차이 때문이야.
### 각 Advice의 실행 시점과 대상

| Advice 메서드 | 애너테이션 | 실행 시점 | 대상 |
|---------------|------------|------------|-------|
| `LoggingAdvice` | `@Before("allGetters()")` | 메서드 실행 직전 | `get*()` 메서드 |
| `ThirdAdvice`   | `@After("allCircleMethods()")` | 메서드 실행 직후 | `Circle` 클래스의 모든 메서드 |
| `SecondAdvice`  | `@After("allGetters()")` | 메서드 실행 직후 | `get*()` 메서드 |

### 왜 `ThirdAdvice`가 `SecondAdvice`보다 먼저 실행될까?
Spring AOP는 Advice 실행 순서를 보장하지 않아요.
특히 `@After` Advice가 여러 개 걸려 있으면, Pointcut의 범위나 선언 순서에 따라 실행 순서가 달라질 수 있어요.
- `ThirdAdvice`는 `Circle.*(..)` 전체를 대상으로 하기 때문에 먼저 실행될 수 있어
- `SecondAdvice`는 `get*()`만 대상으로 하기 때문에 나중에 실행될 수도 있어
이건 Spring 내부에서 프록시 체인을 구성할 때 결정되는 순서라서, 명확하게 제어하려면 `@Order` 애너테이션을 써야 해.
### 해결 방법: `@Order`로 명시적 순서 지정
```java
@Aspect
@Order(1)
@Component
public class LoggingAspect {
    // Advice 메서드들
}
```
- `@Order(1)` → 낮은 숫자가 먼저 실행됨
- 여러 Aspect가 있을 때 순서를 제어할 수 있어
`@Around`는 Spring AOP에서 가장 강력하고 유연한 Advice 타입이야. 그리고 맞아 — `@Around`도 AOP(Aspect-Oriented Programming)의 한 종류야. 오히려 `@Before`, `@After`보다 더 정밀하게 메서드 실행을 제어할 수 있어.
### `@Around`란?
- 메서드 실행 전과 후 모두에 개입할 수 있는 Advice
- 메서드 실행을 완전히 감싸서 직접 실행 여부를 결정할 수 있음
- 리턴값을 조작하거나 예외를 처리하는 것도 가능
### 기본 예시
```java
@Aspect
@Component
public class LoggingAspect {
    @Around("execution(* get*())")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("▶▶▶ Before method: " + joinPoint.getSignature().getName());
        Object result = joinPoint.proceed(); // 실제 대상 메서드 실행
        System.out.println("◀◀◀ After method: " + joinPoint.getSignature().getName());
        return result;
    }
}
```
### 핵심 포인트

| 요소 | 설명 |
|------|------|
| `ProceedingJoinPoint` | 대상 메서드에 대한 정보와 실행 권한을 가진 객체 |
| `proceed()` | 실제 대상 메서드를 실행하는 메서드 |
| `Object result` | 대상 메서드의 반환값을 받아서 가공하거나 그대로 반환 가능 |

### 언제 쓰면 좋을까?
- 메서드 실행 전후에 공통 로직을 넣고 싶을 때 (예: 로깅, 트랜잭션, 성능 측정)
- 메서드 실행을 조건부로 막거나 변경하고 싶을 때
- 리턴값을 가공하거나 예외를 잡아서 처리하고 싶을 때
### 출력 예시
```java
System.out.println(circle.getName());
```
출력 결과:
```
▶▶▶ Before method: getName
Circle.getName run
◀◀◀ After method: getName
circle
```
Spring AOP에서 사용하는 `@Before`, `@After`, `@Around` 같은 애너테이션들은 Advice(어드바이스)의 종류를 나타내요. 이들은 관심사를 핵심 로직과 분리하기 위해 사용하는 AOP의 핵심 구성 요소 중 하나예요.
## AOP의 핵심 구성 요소

| 구성 요소 | 설명 |
|-----------|------|
| Aspect | 공통 기능(로깅, 보안 등)을 모듈화한 클래스 (`@Aspect`) |
| Advice | 언제 어떤 방식으로 공통 기능을 적용할지 정의 (`@Before`, `@After`, `@Around` 등) |
| Join Point | Advice가 적용될 수 있는 지점 (예: 메서드 실행) |
| Pointcut | 어떤 Join Point에 Advice를 적용할지 결정하는 표현식 |
| Weaving | Aspect를 실제 코드에 적용하는 과정 (Spring은 런타임에 프록시로 처리) |

## Advice의 종류와 실행 시점

| 애너테이션 | 실행 시점 | 설명 |
|------------|------------|------|
| `@Before` | 대상 메서드 실행 전 | 메서드 호출 전에 실행됨 |
| `@After` | 대상 메서드 실행 후 (성공/실패 무관) | 예외 발생 여부와 관계없이 실행됨 |
| `@AfterReturning` | 대상 메서드 정상 종료 후 | 예외 없이 성공적으로 끝났을 때만 실행됨 |
| `@AfterThrowing` | 대상 메서드 예외 발생 후 | 예외가 발생했을 때만 실행됨 |
| `@Around` | 대상 메서드 전후 모두 | 메서드 실행 자체를 감싸서 전후 로직을 모두 제어할 수 있음 |

## `@Around` vs 나머지

| 특징 | `@Before` / `@After` 등 | `@Around` |
|-------|--------------------------|-----------|
| 실행 시점 | 고정 (전/후/예외 등) | 유연하게 전후 모두 |
| 메서드 실행 제어 | 못함 | `proceed()`로 직접 실행 |
| 리턴값 조작 | 제한적 | 가능 |
| 예외 처리 | 제한적 | 가능 |
| 성능 측정, 트랜잭션 | 불편 | 적합 |

## 예시 비교
### `@Before`
```java
@Before("execution(* get*())")
public void beforeAdvice() {
    System.out.println("Before method");
}
```
### `@Around`
```java
@Around("execution(* get*())")
public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println("Before method");
    Object result = joinPoint.proceed(); // 실제 메서드 실행
    System.out.println("After method");
    return result;
}
```
`@Pointcut("within(aop.demo.model.*)")`는 해당 패키지 내의 클래스들(Circle, Triangle 등)의 모든 메서드 실행 지점을 포인트컷으로 지정하지만, 생성자는 포함되지 않습니다.
### 이유: `within()`은 메서드 실행 지점만 포함
- `within()` 포인트컷은 클래스 내부의 메서드 실행 지점만 캡처합니다.
- 생성자 실행은 메서드가 아니라 생성자 실행 지점이기 때문에 `within()`으로는 잡히지 않아요.
### 생성자를 포인트컷으로 지정하려면?
생성자 실행을 캡처하려면 `execution()`을 사용해야 해요:
```java
@Pointcut("execution(aop.demo.model.Circle.new(..))")
public void circleConstructor() {}
```
또는 모든 생성자:
```java
@Pointcut("execution(*.new(..))")
public void allConstructors() {}
```
### 예시: Advice 적용
```java
@Before("execution(aop.demo.model.Circle.new(..))")
public void beforeCircleConstructor() {
    System.out.println("Circle 생성자 실행 전");
}
```
### 참고: 생성자에 AOP를 적용할 때 주의할 점
- 생성자 실행 시점에는 아직 객체가 완전히 초기화되지 않았기 때문에, 의존성 주입이 완료되지 않았을 수 있어요.
- 따라서 생성자 Advice는 로깅이나 단순한 트래킹 용도로만 사용하는 것이 안전해요.
`@Pointcut("execution(* aop.demo.model.Circle.*(..))")` 이 표현식은 생성자를 포함하지 않습니다.
### 왜 생성자는 포함되지 않을까?
이 포인트컷은 다음을 의미해요:
```java
execution(* aop.demo.model.Circle.*(..))
```
- `*` → 모든 리턴 타입
- `aop.demo.model.Circle.*(..)` → `Circle` 클래스의 모든 메서드 (이름이 `*`이고, 파라미터가 `(..)`인 것)
하지만 생성자는 메서드가 아니라 생성자이기 때문에 이 표현식에 포함되지 않아요.
### 생성자를 포함시키려면?
생성자를 지정하려면 `execution` 표현식을 다음처럼 바꿔야 해요:
```java
@Pointcut("execution(aop.demo.model.Circle.new(..))")
public void circleConstructors() {}
```
또는 모든 생성자를 포괄하려면:
```java
@Pointcut("execution(*.new(..))")
public void allConstructors() {}
```
- 생성자 포인트컷은 잘 쓰이지 않지만, 로깅이나 객체 생성 추적이 필요할 때 유용해요.
- 생성자 실행 시점에는 의존성 주입이 아직 완료되지 않았을 수 있으니, 복잡한 로직은 피하는 게 좋아요.
`execution(aop.demo.model.Circle.*(..))` 이렇게 쓰면 생성자는 포함되지 않습니다.
### 왜 생성자는 포함되지 않을까?
- `execution()` 포인트컷은 메서드 실행을 지정할 때 `클래스명.메서드명` 형식으로 작성해요.
- 생성자는 메서드가 아니라 `new` 키워드로 실행되는 특수한 실행 지점이기 때문에 `클래스명.생성자명`으로 표현할 수 없어요.
- 따라서 `execution(Circle.*(..))`은 일반 메서드만 포함하고, 생성자는 포함하지 않아요.
### 생성자를 지정하려면?
생성자를 포인트컷으로 지정하려면 다음처럼 작성해야 해요:
```java
@Pointcut("execution(aop.demo.model.Circle.new(..))")
public void circleConstructors() {}
```
또는 모든 클래스의 생성자를 포괄하려면:
```java
@Pointcut("execution(*.new(..))")
public void allConstructors() {}
```
### 정리

| 표현식 | 포함 대상 |
|--------|------------|
| `execution(Circle.*(..))` | Circle 클래스의 모든 메서드 (생성자 제외) |
| `execution(Circle.new(..))` | Circle 클래스의 생성자 |
| `execution(*.new(..))` | 모든 클래스의 생성자 |

혹시 생성자에 Advice를 적용하려는 이유가 로깅, 초기화 확인, 디버깅이라면 `@Before("execution(Circle.new(..))")` 같은 방식으로 적용하면 돼요.
`execution(aop.demo.model.Circle.*(..))`는 리턴 타입이 무엇이든 상관없이, `Circle` 클래스의 모든 메서드를 대상으로 지정합니다.
### 표현식 분석
```java
execution(aop.demo.model.Circle.*(..))
```
이건 문법적으로 잘못된 표현이에요. 올바른 `execution` 포인트컷은 다음과 같아야 해요:
```java
execution(* aop.demo.model.Circle.*(..))
```

| 부분 | 의미 |
|------|------|
| `*` | 리턴 타입: 어떤 것이든 허용 |
| `aop.demo.model.Circle` | 대상 클래스 |
| `*` | 메서드 이름: 어떤 것이든 허용 |
| `(..)` | 파라미터: 어떤 것이든 허용 |

### 리턴 타입이 없는 메서드만 포함하려면?
리턴 타입이 `void`인 메서드만 포함하고 싶다면 이렇게 작성해야 해요:
```java
execution(void aop.demo.model.Circle.*(..))
```
### 예시

| 표현식 | 포함되는 메서드 |
|--------|------------------|
| `execution(* aop.demo.model.Circle.*(..))` | 모든 리턴 타입의 메서드 |
| `execution(void aop.demo.model.Circle.*(..))` | 리턴 타입이 `void`인 메서드만 |
| `execution(String aop.demo.model.Circle.getName())` | 리턴 타입이 `String`이고 이름이 `getName`인 메서드 |

`@Pointcut(args(..))`는 Spring AOP에서 메서드의 인자(파라미터) 타입을 기준으로 포인트컷을 지정할 때 사용하는 표현식이에요. 즉, 어떤 타입의 인자를 가진 메서드에 Advice를 적용하고 싶을 때 쓰는 거예요.
### 기본 문법
```java
@Pointcut("args(java.lang.String)")
public void stringArgsMethods() {}
```
- 이 포인트컷은 파라미터가 `String` 타입인 메서드를 대상으로 해요.
- 메서드 이름이나 클래스와는 상관없이, 인자 타입만 기준으로 잡는 거예요.
### 예시: Advice 적용
```java
@Around("stringArgsMethods()")
public Object logStringArgs(ProceedingJoinPoint joinPoint) throws Throwable {
    System.out.println("▶▶▶ 메서드 인자에 String이 포함됨");
    return joinPoint.proceed();
}
```
### 자주 쓰는 args 표현

| 표현식 | 설명 |
|--------|------|
| `args(String)` | 인자가 하나이고 타입이 `String` |
| `args(String, int)` | 인자가 두 개이고 타입이 `String`, `int` |
| `args(..)` | 인자 개수나 타입에 상관없이 모든 메서드 |
| `args(*)` | 인자가 하나 이상 있는 모든 메서드 |

### `args()` vs `execution()` 차이

| 항목 | `args()` | `execution()` |
|------|----------|----------------|
| 기준 | 인자 타입 | 메서드 시그니처 전체 |
| 유연성 | 런타임 타입 기반 | 컴파일 타입 기반 |
| 활용 | 동적 프록시, 리플렉션 기반 Advice | 정적 메서드 매칭 |

### 언제 쓰면 좋을까?
- 특정 타입의 인자를 가진 메서드에만 Advice를 적용하고 싶을 때
- 예: `String`을 인자로 받는 메서드에만 로깅하거나 검증을 하고 싶을 때
`@Pointcut("args(java.lang.String)")`는 클래스 범위와는 무관하게, 메서드의 인자 타입이 `String`인 경우에만 적용됩니다. 즉, 이 포인트컷은 클래스 이름이나 패키지 경로와는 상관없이, 파라미터가 `String` 타입인 메서드를 대상으로 해요.
### 핵심 개념: `args()`는 런타임 인자 타입 기반

| 표현식 | 의미 |
|--------|------|
| `args(String)` | 인자가 하나이고 타입이 `String` |
| `args(String, int)` | 인자가 두 개이고 타입이 `String`, `int` |
| `args(..)` | 인자 개수나 타입에 상관없이 모든 메서드 |
| `args(*)` | 인자가 하나 이상 있는 모든 메서드 |

### 클래스 범위는 어떻게 결정되나?
- `args()`는 클래스 범위를 지정하지 않음
- 따라서 `@Pointcut("args(String)")`만 쓰면, 모든 클래스의 String 인자 메서드가 Advice 대상이 됨
- 특정 클래스에만 적용하고 싶다면 `execution()` 또는 `within()`과 조합해야 해요
### 클래스 범위 지정 예시
```java
@Pointcut("execution(* aop.demo.model.Circle.*(..)) && args(java.lang.String)")
public void circleStringArgsMethods() {}
```
- 이 포인트컷은 `Circle` 클래스의 String 인자를 가진 메서드만 대상으로 해요
### 요약

| 표현식 | 클래스 범위 | 대상 메서드 |
|--------|--------------|--------------|
| `args(String)` | 없음 (모든 클래스) | String 인자 메서드 |
| `execution(* Circle.*(..))` | Circle 클래스 | 모든 메서드 |
| `execution(* Circle.*(..)) && args(String)` | Circle 클래스 | String 인자 메서드 |

`System.out.println()` 자체는 AOP의 대상이 되지 않기 때문에, Advice가 적용되지 않는 게 정상이에요.
### 왜 `System.out.println()`에는 AOP가 적용되지 않을까?
Spring AOP는 메서드 실행을 Join Point로 삼는 프록시 기반 AOP예요.
즉, 다음 조건을 만족해야 AOP가 적용됩니다:

| 조건 | 설명 |
|------|------|
| 메서드 호출 | 반드시 객체의 메서드여야 함 (`getName()`, `setValue()` 등) |
| Spring 빈 | 해당 객체가 Spring 컨테이너에 의해 관리되는 빈이어야 함 |
| 프록시 경유 | 호출이 프록시를 통해서 이루어져야 함 |

### `System.out.println()`은?
- `System`은 Spring 빈이 아님
- `out.println()`은 정적 메서드 호출
- 프록시를 통해 호출되지 않음
그래서 `@Before`, `@After`, `@Around` 어떤 Advice도 적용되지 않아요.
### AOP가 적용되는 예시
```java
@Component
public class Circle {
    public String getName() {
        System.out.println("getName 실행");
        return "circle";
    }
}
```
```java
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* getName())")
    public void beforeAdvice() {
        System.out.println("▶▶▶ Advice 실행");
    }
}
```
이 경우 `getName()` 호출 시 `Advice`가 실행돼요.
하지만 `System.out.println()` 자체에는 아무런 AOP 적용이 되지 않아요.
### 요약

| 대상 | AOP 적용 여부 |
|------|--------|
| `System.out.println()` | 적용 안 됨 |
| `Spring 빈의 메서드` | 적용 가능 |
| `new`로 생성한 객체 | 적용 안 됨 |
| `정적 메서드` | 적용 안 됨 |
