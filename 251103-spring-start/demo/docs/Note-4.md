## 로그의 null이 출력된 이유는
- circle.getName()의 실행이 AOP 어드바이스에 의해 가로채어졌지만, 어드바이스에서 메서드의 실제 실행 결과를 반환하지 않았기 때문입니다.
- 이는 주로 @Around 어드바이스에서 joinPoint.proceed()를 호출했으나 해당 결과를 반환하지 않은 경우에 발생합니다.
### 원인 분석
1. @Around 어드바이스에서 joinPoint.proceed()의 반환값이 누락된 경우
- @Around advice는 메서드 실행을 직접 제어합니다. 따라서 다음 코드가 있어야 합니다:
```java
@Around("execution(String aop.demo.model.Circle.getName())")
public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    // 전처리
    System.out.println("LoggingAspect.SecondAdvice run");

    // 실제 메서드 실행
    Object result = joinPoint.proceed(); // 실행

    // 후처리
    System.out.println(result); // 여기서 null이 발생한 것으로 추정됨

    // 중요: result를 반환하지 않으면 null 반환
    return result;
}
```
- 만약 return result;를 생략하면 getName()은 null을 반환하게 됩니다.
2. @AfterReturning 어드바이스에서 반환값을 무시한 경우
- @AfterReturning은 메서드 실행 후 반환 값을 확인할 수 있지만, 이 값을 직접 반환하지 않기 때문에 null은 어드바이스 자체에서 무언가 잘못된 경우입니다.
```java
@AfterReturning(pointcut = "execution(String aop.demo.model.Circle.getName())", returning = "result")
public void logAfterReturning(JoinPoint joinPoint, Object result) {
    System.out.println("LoggingAspect.ThirdAdvice run");
    System.out.println(result); // 여기서 null
}
```
- 이 경우 joinPoint.proceed() 자체를 호출하지 않기 때문에 메서드 실행이 영향을 받지 않으므로, 위 경우는 아닙니다.
### 해결 방안
1. @Around 어드바이스에서 반환값 보장
- @Around 어드바이스 내부에서 반드시 joinPoint.proceed()의 결과를 반환해야 합니다.
```java
@Around("execution(String aop.demo.model.Circle.getName())")
public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed(); // 실행
    System.out.println(joinPoint + " 반환값: " + result);
    return result;
}
```
2. 반환값 타입 검증
- getName()이 반환하는 값이 null일 가능성도 있으므로, circle.getName() 메서드 내부에서 null이 들어가는 로직이 없는지 추가로 확인해야 합니다.
- 이 수정을 반영한 후 실행하면 null이 아닌 원하는 String 값이 정상적으로 출력될 것입니다.
- `@Around` 어드바이스는 메서드 실행을 감싸고 직접 제어하기 때문에, `joinPoint.proceed()`의 결과를 반드시 반환(return) 해줘야 합니다.
- 그렇지 않으면, 해당 메서드를 호출한 쪽에서는 null을 반환값으로 받게 되고, 그 값을 다른 함수에 넘기거나 사용할 때 NullPointerException이 발생할 수 있어요.
### 왜 반환이 중요한가?
- 예를 들어, 다음과 같은 코드가 있다고 해볼게요:
```java
String name = circle.getName(); // AOP로 감싸진 메서드
someOtherMethod(name); // name이 null이면 문제가 생길 수 있음
```
- `circle.getName()`이 AOP의 `@Around` 어드바이스에 의해 가로채졌고,
- 그 어드바이스에서 `return result;`를 하지 않았다면,
- `getName()`은 null을 반환하게 되고,
- `someOtherMethod(null)`처럼 호출되면서 예상치 못한 오류가 발생할 수 있습니다.
### 정리하자면
- 단순히 로그만 찍고 끝내는 게 아니라, 원래 메서드의 반환값을 그대로 돌려줘야 AOP가 끼어들어도 프로그램의 흐름이 깨지지 않아요.
- 특히 다른 메서드로 값을 넘기거나, 리턴값을 기반으로 로직이 분기되는 경우에는 반드시 `return result;`가 있어야 합니다.
- `@Around` 어드바이스에서 반환 타입을 `Object`로 지정하는 이유는 호환성과 유연성 때문인데, 꼭 `Object`로만 반환해야 하는 건 아닙니다.
### 왜 기본적으로 Object를 쓰는가?
- `ProceedingJoinPoint.proceed()`는 `Object`를 반환합니다.
- 따라서 어드바이스 메서드의 반환 타입도 `Object`로 하면 어떤 메서드든 감쌀 수 있어요.
- 예를 들어 `String`, `int`, `List<?>`, `void` 등 다양한 반환 타입을 가진 메서드를 하나의 어드바이스로 처리할 수 있습니다.
### 하지만 특정 타입으로도 가능해요
- 만약 특정 메서드만 감싸고, 그 메서드의 반환 타입이 고정이라면 다음처럼 명시적으로 타입을 지정할 수 있어요:
```java
@Around("execution(String aop.demo.model.Circle.getName())")
public String logAround(ProceedingJoinPoint joinPoint) throws Throwable {
    String result = (String) joinPoint.proceed(); // 캐스팅 필요
    System.out.println("결과: " + result);
    return result;
}
```
### 주의할 점
- `joinPoint.proceed()`는 항상 `Object`를 반환하므로, 명시적 타입으로 받을 때는 캐스팅이 필요합니다.
- 여러 메서드를 감싸는 공통 어드바이스를 만들고 싶다면 `Object`가 더 안전하고 범용적입니다.
- `@Around("@annotation(...)")`는 Spring AOP (Aspect-Oriented Programming)에서 사용하는 표현으로, 특정 애노테이션이 붙은 메서드에 대해 공통 기능을 적용할 때 사용됩니다.
### 핵심 개념
- `@Around`: AOP에서 advice(공통 기능)를 정의할 때 사용하는 애노테이션입니다. 메서드 실행 전후에 원하는 로직을 삽입할 수 있어요.
- `"@annotation(YourAnnotation)"`: 포인트컷 표현식입니다. `YourAnnotation`이라는 애노테이션이 붙은 메서드를 대상으로 AOP를 적용하겠다는 뜻이에요.
### 예시 코드
```java
@Aspect
@Component
public class LoggingAspect {
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed(); // 실제 메서드 실행
        long executionTime = System.currentTimeMillis() - start;
        System.out.println(joinPoint.getSignature() + " executed in " + executionTime + "ms");
        return proceed;
    }
}
```
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExecutionTime {
}
```
```java
public class MyService {
    @LogExecutionTime
    public void doSomething() {
        // 이 메서드 실행 시간 측정됨
    }
}
```
### 정리하자면
- `@Around("@annotation(...)")`는 특정 애노테이션이 붙은 메서드에 AOP를 적용하는 방식이에요.
- `@annotation(LogExecutionTime)` → `@LogExecutionTime`이 붙은 메서드에만 적용됨.
- 메서드 실행 전후에 로깅, 트랜잭션, 보안 체크 등 다양한 공통 기능을 넣을 수 있어요.
### 이 애노테이션의 의미
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogExecutionTime {
}
```
이건 커스텀 애노테이션(Custom Annotation)을 정의하는 코드예요. 의미는 다음과 같아요:
- `@Retention(RetentionPolicy.RUNTIME)`: 이 애노테이션은 런타임에도 유지되므로, 리플렉션이나 AOP 같은 기술로 접근할 수 있어요.
- `@Target(ElementType.METHOD)`: 이 애노테이션은 메서드에만 붙일 수 있다는 뜻이에요.
- `public @interface LogExecutionTime`: `LogExecutionTime`이라는 이름의 애노테이션을 정의한 거예요.
즉, 이 애노테이션을 메서드에 붙이면, AOP에서 해당 메서드를 감지해서 실행 시간 측정 같은 공통 기능을 적용할 수 있게 되는 거죠.
### 꼭 만들어야 할까?
AOP에서 `@annotation(LogExecutionTime)` 같은 포인트컷을 쓰고 싶다면, 이 애노테이션은 반드시 필요해요.
왜냐하면:
- `@annotation(...)`은 애노테이션 기반 포인트컷이기 때문에, 그 애노테이션이 실제로 존재해야 AOP가 작동해요.
- `LogExecutionTime`이 없으면, AOP 설정은 해당 메서드를 찾지 못하고 아무 일도 일어나지 않아요.
### 대안은 없을까?
있긴 해요. 꼭 커스텀 애노테이션을 만들지 않아도 AOP는 다양한 방식으로 적용할 수 있어요:

| 방식 | 설명 |
|------|------|
| `execution(...)` | 메서드 이름, 패키지, 리턴 타입 등으로 지정 |
| `within(...)` | 특정 클래스나 패키지 전체에 적용 |
| `@annotation(...)` | 특정 애노테이션이 붙은 메서드에만 적용 |

하지만 애노테이션 기반 방식은 가장 명확하고 유지보수가 쉬운 방식이라 많이 사용돼요.
### 요약
- `LogExecutionTime`은 AOP에서 사용할 표식 역할을 하는 애노테이션이에요.
- `@Around("@annotation(LogExecutionTime)")`을 쓰려면 반드시 정의되어 있어야 해요.
- 이 방식은 코드에 명시적으로 어떤 기능이 적용되는지 드러나기 때문에 가독성과 유지보수에 유리해요.