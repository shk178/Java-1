이 코드는 AOP에서 `JoinPoint`를 활용해서 Advice가 적용된 메서드의 실행 정보를 출력하는 예제야.
## 이 코드가 하는 일
```java
@After("allCircleMethods()")
public void one(JoinPoint joinPoint) {
    System.out.println(joinPoint.toString());
    System.out.println(joinPoint.getTarget());
}
```
- `@After("allCircleMethods()")` → `Circle` 클래스의 모든 메서드 실행 후에 실행되는 Advice
- `JoinPoint` → Advice가 적용된 메서드 실행 지점의 정보를 담고 있는 객체
## `JoinPoint`로 할 수 있는 일
`JoinPoint`는 AOP Advice에서 사용할 수 있는 런타임 정보를 제공해줘. 주요 기능은 아래와 같아:

| 메서드 | 설명 | 예시 출력 |
|--------|------|-----------|
| `toString()` | 실행된 메서드의 시그니처 | `execution(String aop.demo.model.Circle.getName())` |
| `getTarget()` | 실제 대상 객체 (프록시가 아닌 원본) | `aop.demo.model.Circle@479460a6` |
| `getThis()` | 프록시 객체 | `com.sun.proxy.$Proxy12` |
| `getSignature()` | 메서드 이름, 리턴 타입 등 | `getName()` |
| `getArgs()` | 메서드 인자 배열 | `[“hello”, 42]` |
| `getKind()` | JoinPoint 종류 (보통 "method-execution") | `"method-execution"` |

## 활용 예시
```java
@After("allCircleMethods()")
public void logDetails(JoinPoint joinPoint) {
    System.out.println("▶ 메서드 이름: " + joinPoint.getSignature().getName());
    System.out.println("▶ 인자들: " + Arrays.toString(joinPoint.getArgs()));
    System.out.println("▶ 대상 객체: " + joinPoint.getTarget());
}
```
## 언제 쓰면 좋을까?
- 로깅: 어떤 메서드가 호출됐는지, 어떤 인자가 들어왔는지 기록
- 디버깅: Advice가 정확히 어디에 적용됐는지 확인
- 조건 분기: 특정 인자나 메서드 이름에 따라 Advice 동작을 다르게 하고 싶을 때
> `JoinPoint`로는 메서드 실행이나 데이터 조작을 직접 할 수 없습니다.
> 하지만 `ProceedingJoinPoint`를 사용하면 메서드 실행을 제어할 수 있습니다.
## `JoinPoint` vs `ProceedingJoinPoint`

| 타입 | 사용 가능한 Advice | 메서드 실행 가능 여부 | 설명 |
|------|--------------------|--------------------|------|
| `JoinPoint` | `@Before`, `@After`, `@AfterReturning`, `@AfterThrowing` | 실행 불가 | 단순히 실행 지점의 정보만 제공 |
| `ProceedingJoinPoint` | `@Around` | 실행 가능 (`proceed()`) | 메서드 실행을 감싸고 직접 실행 여부를 결정할 수 있음 |

## `ProceedingJoinPoint`로 메서드 실행 제어 예시
```java
@Around("execution(* get*())")
public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
    System.out.println("Before method: " + pjp.getSignature().getName());
    Object result = pjp.proceed(); // 실제 메서드 실행
    System.out.println("After method: " + pjp.getSignature().getName());
    return result;
}
```
- `proceed()`를 호출해야 실제 대상 메서드가 실행돼요.
- 호출하지 않으면 메서드 실행을 막을 수도 있어요.
- `result`를 가공해서 리턴하면 리턴값을 조작할 수도 있어요.
## `JoinPoint`로는 불가능한 것들
- `joinPoint.proceed()` 같은 메서드 없음
- 메서드 실행을 막거나 조건부 실행 불가
- 리턴값 변경 불가
## 언제 `ProceedingJoinPoint`를 써야 할까?
- 성능 측정 (메서드 실행 전후 시간 비교)
- 트랜잭션 처리
- 리턴값 가공
- 예외 처리
- 조건부 실행 (예: 특정 인자일 때만 실행)
Spring AOP에서 프록시가 아닌 원본 객체(타겟 객체)를 참조하는 건 가능합니다 — 하지만 프록시 기반 AOP에서는 직접 바꾸는 건 불가능하고, 참조만 할 수 있어요.
## `JoinPoint.getTarget()` vs `JoinPoint.getThis()`

| 메서드 | 설명 | 객체 |
|--------|------|------|
| `getTarget()` | 실제 원본 객체 (타겟) | `Circle@123abc` |
| `getThis()` | 프록시 객체 | `com.sun.proxy.$Proxy12` |

- `getTarget()`은 프록시가 감싸고 있는 실제 객체를 반환해요.
- `getThis()`는 프록시 자체를 반환해요.
## 원본 객체를 참조해서 할 수 있는 것
- 원본 객체의 `toString()`이나 필드에 접근
- 리플렉션으로 원본 클래스 정보 확인
- 로깅, 디버깅, 조건 분기 등에 활용
```java
@After("execution(* aop.demo.model.Circle.*(..))")
public void logTarget(JoinPoint joinPoint) {
    Object target = joinPoint.getTarget();
    System.out.println("원본 객체 클래스: " + target.getClass().getName());
}
```
## 바꿀 수 없는 것
- 프록시를 제거하거나 우회해서 직접 메서드를 실행하는 건 불가능
- Advice 내부에서 원본 객체의 필드를 직접 바꾸는 건 권장되지 않음
- Spring AOP는 프록시 기반이기 때문에, 모든 호출은 프록시를 통해야 Advice가 적용됨
## 원본 객체를 직접 다루고 싶다면?
- AspectJ (컴파일 타임 AOP)를 사용하면 더 깊은 제어가 가능해요
- 또는 프록시가 아닌 직접 호출 방식으로 설계하거나, BeanPostProcessor 같은 다른 메커니즘을 활용할 수 있어요
`joinPoint.getTarget()`을 통해 프록시가 감싸고 있는 실제 원본 객체를 가져올 수는 있지만, 그 객체를 조작하는 건 제한적이고 주의가 필요해요.
## 가능은 하지만 "조작"의 의미에 따라 다름
### 1. 원본 객체의 메서드 호출
```java
Object target = joinPoint.getTarget();
if (target instanceof Circle) {
    ((Circle) target).setName("modified");
}
```
- 이렇게 하면 실제 객체의 상태를 바꿀 수 있어요.
- 하지만 Advice가 실행되는 시점에 따라 변경이 반영되지 않을 수도 있어요 (예: `@After`에서는 이미 리턴값이 결정된 후일 수 있음).
### 2. 필드 직접 수정 (비추천)
```java
Field field = target.getClass().getDeclaredField("name");
field.setAccessible(true);
field.set(target, "modified");
```
- 리플렉션으로 필드를 직접 바꾸는 것도 가능하지만, 안정성과 유지보수 측면에서 매우 위험해요.
- Spring의 DI, 트랜잭션, 캐싱 등과 충돌할 수 있어요.
### 조작이 불가능하거나 위험한 경우

| 시도 | 설명 |
|------|------|
| 프록시 우회 실행 | Advice가 적용되지 않음 |
| 리턴값 변경 | `@After`에서는 불가능 (`@Around`에서만 가능) |
| 객체 교체 | `getTarget()`은 참조만 제공, 교체는 불가능 |
| 상태 변경 후 반영 기대 | 시점에 따라 반영되지 않을 수 있음 |

## 안전하게 조작하려면?
- `@Around` + `ProceedingJoinPoint`를 사용해서 메서드 실행 전후를 감싸고, 리턴값이나 인자 등을 제어하는 방식이 가장 안전하고 강력해요.
- `getTarget()`은 정보 확인이나 조건 분기에 활용하는 게 일반적이에요.
AOP에서 `args(name)` 포인트컷을 사용해서 메서드 인자 중 `String name`을 가진 메서드가 실행되기 직전에 Advice를 실행하는 예제예요.
## 코드 분석
```java
@Before("args(name)")
public void two(String name) {
    System.out.println("Advices.two run: " + name);
}
```
### 의미
- `@Before("args(name)")` → 인자가 `String name`인 메서드가 실행되기 직전에 실행됨
- `name`은 포인트컷에서 추출된 실제 인자 값이 자동으로 바인딩됨
- `System.out.println(...)`으로 인자 값을 출력
## 어떤 메서드에 적용될까?
예를 들어 이런 메서드가 있을 때:
```java
public void setName(String name) {
    this.name = name;
}
```
이 메서드를 호출하면:
```java
circle.setName("hello");
```
출력 결과:
```
Advices.two run: hello
```
## 핵심 포인트: `args()`와 파라미터 바인딩
Spring AOP는 `args()` 포인트컷을 통해 Advice 메서드의 파라미터와 대상 메서드의 인자를 자동으로 연결할 수 있어요.
### 예시: 여러 인자 바인딩
```java
@Before("args(name, age)")
public void log(String name, int age) {
    System.out.println("name: " + name + ", age: " + age);
}
```
## 주의할 점
- `args()`는 런타임 타입 기준이라서, 오버로딩된 메서드나 다형성 상황에서는 주의가 필요해요
- Advice 메서드의 파라미터 타입과 순서가 정확히 맞아야 바인딩이 돼요
지금 겪고 있는 현상은 Spring AOP의 `args()` 포인트컷 바인딩 방식과 Advice 파라미터 매칭 방식이 이름 기준이 아니라 타입 기준으로 동작하기 때문에 생긴 거예요.
## 왜 `name`과 `name2`가 구분되지 않을까?
Spring AOP에서 `args()` 포인트컷은 메서드 인자의 타입을 기준으로 Advice에 바인딩합니다.  
즉, 아래 두 메서드는 모두 `String` 타입의 인자를 가지고 있기 때문에:
```java
public void setName(String name) { ... }
public String setNameandReturn(String name2) { ... }
```
이 두 메서드 모두 `args(String)`에 매칭되고, Advice 파라미터 이름이 `name`이든 `name2`든 상관없이 타입만 맞으면 실행돼요.
## 해결 방법: 이름 기반 바인딩을 명확히 하려면?
Spring AOP는 기본적으로 타입 기반 바인딩을 사용하지만, 이름 기반으로 더 정밀하게 제어하고 싶다면 `@Pointcut`을 `execution()`과 조합하거나, `@Around`에서 `JoinPoint`로 직접 구분하는 방식이 필요해요.
### 예시 1: `execution()`으로 메서드 이름 지정
```java
@Pointcut("execution(* setName(String))")
public void setNamePointcut() {}
@Pointcut("execution(* setNameandReturn(String))")
public void setNameAndReturnPointcut() {}
```
### 예시 2: `args()`는 그대로 두고, Advice에서 `JoinPoint.getSignature().getName()`으로 구분
```java
@Before("args(name)")
public void two(JoinPoint joinPoint, String name) {
    String methodName = joinPoint.getSignature().getName();
    System.out.println("▶ method: " + methodName + ", arg: " + name);
}
```
## 요약

| 포인트컷 | 바인딩 기준 | 설명 |
|----------|--------------|------|
| `args(name)` | 타입 기반 (`String`) | 이름은 무시됨 |
| Advice 파라미터 | 이름은 자유롭게 지정 가능 | 타입만 맞으면 바인딩됨 |
| 정확한 구분 | `execution()` 또는 `JoinPoint.getSignature()` 사용 | 메서드 이름으로 구분 가능 |

> `@AfterReturning`의 `returning` 속성과 Advice 메서드의 파라미터 이름은 반드시 일치해야 하고, 타입은 Spring이 자동으로 매핑할 수 있어야 합니다.
### 1. `returning="returnString"`은 Advice 메서드의 파라미터 이름과 일치해야 함
```java
@AfterReturning(pointcut="args(name2)", returning="returnString")
public void three(String name2, String returnString) { ... }
```
- 여기서 `"returnString"`은 Advice 메서드의 두 번째 파라미터 이름과 정확히 일치해야 해요.
- 이름이 다르면 바인딩이 안 되고 예외가 발생할 수 있어요.
### 2. 타입은 리턴값과 호환되면 OK
- 예: 대상 메서드가 `String`을 반환하면 `returnString`의 타입도 `String`이어야 해요.
- 만약 대상 메서드가 `Object`를 반환하면 `Object`, `Serializable`, `CharSequence` 등 상위 타입도 가능해요.
### 3. 인자 바인딩도 마찬가지
- `args(name2)`는 메서드 인자 중 `String name2`를 바인딩해요.
- 이때도 Advice 메서드의 파라미터 이름과 타입이 정확히 일치해야 해요.
## 예시: 잘못된 경우
```java
@AfterReturning(pointcut="args(name2)", returning="result")
public void three(String name2, String returnString) { ... } // 이름 불일치
```
→ `IllegalArgumentException` 발생 가능
## 요약

| 요소 | 조건 |
|------|------|
| `returning="xxx"` | Advice 메서드의 파라미터 이름과 일치해야 함 |
| 리턴 타입 | 대상 메서드의 리턴 타입과 호환되어야 함 |
| `args(...)` | 인자 이름과 타입이 정확히 일치해야 바인딩됨 |

`@AfterReturning(pointcut="...", returning="result")`에서 `result`의 타입은 Advice 메서드의 파라미터 타입을 보고 Spring이 자동으로 결정합니다. 즉:
> `returning="result"`는 Advice 메서드의 파라미터 이름과 타입을 기준으로 바인딩돼요.
## 어떻게 타입이 결정되나?
Spring AOP는 `@AfterReturning`을 처리할 때 다음을 기준으로 리턴값을 Advice에 전달해요:
1. `returning="result"` → Advice 메서드에 `result`라는 이름의 파라미터가 있어야 함
2. 해당 파라미터의 타입이 실제 리턴값과 호환돼야 함
예를 들어:
```java
@AfterReturning(pointcut="execution(* setNameandReturn(..))", returning="result")
public void logReturn(String result) { ... } // OK: 리턴값이 String
```
```java
@AfterReturning(pointcut="execution(* setNameandReturn(..))", returning="result")
public void logReturn(Object result) { ... } // OK: String은 Object의 하위 타입
```
```java
@AfterReturning(pointcut="execution(* setNameandReturn(..))", returning="result")
public void logReturn(Integer result) { ... } // 타입 불일치 → 예외 발생
```
## 요약

| 요소 | 설명 |
|------|------|
| `returning="result"` | Advice 메서드의 파라미터 이름과 일치해야 함 |
| 타입 결정 | Advice 메서드의 파라미터 타입 기준으로 Spring이 자동 매핑 |
| 타입 불일치 | 예외 발생 (`IllegalArgumentException`) |

`@Around` Advice는 AOP에서 가장 강력한 제어권을 갖는 Advice입니다.  
`@Around`는 메서드를 감싸고, 실행 여부를 결정할 수 있어요. 그리고 다른 Aspect와의 관계도 매우 중요하죠.
## 1. `@Around`에서 메서드 실행을 막을 수 있을까?
가능합니다.
`ProceedingJoinPoint.proceed()`를 호출하지 않으면, 대상 메서드는 실행되지 않습니다.
```java
@Around("execution(* setName(..))")
public Object blockSetName(ProceedingJoinPoint pjp) throws Throwable {
    System.out.println("▶ setName 차단됨");
    return null; // 메서드 실행 안 함
}
```
이렇게 하면 `setName()`은 호출되더라도 실제로는 실행되지 않아요.
## 2. 다른 Aspect가 같은 메서드에 적용되어 있다면?
Spring AOP는 여러 Aspect가 하나의 JoinPoint에 적용될 수 있습니다.
이때 실행 순서는 다음 기준에 따라 결정돼요:
### 실행 순서 결정 기준
1. `@Order` 어노테이션 또는 `Ordered` 인터페이스로 명시한 순서
2. 명시하지 않으면 등록 순서 (Spring 컨테이너에 등록된 순서)
### 예시
```java
@Aspect
@Order(1)
public class LoggingAspect {
    @Around("execution(* setName(..))")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("▶ LoggingAspect: before");
        Object result = pjp.proceed();
        System.out.println("▶ LoggingAspect: after");
        return result;
    }
}
@Aspect
@Order(2)
public class SecurityAspect {
    @Around("execution(* setName(..))")
    public Object secure(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("▶ SecurityAspect: before");
        Object result = pjp.proceed();
        System.out.println("▶ SecurityAspect: after");
        return result;
    }
}
```
실행 순서:
```
▶ LoggingAspect: before
▶ SecurityAspect: before
▶ 실제 메서드 실행
▶ SecurityAspect: after
▶ LoggingAspect: after
```
## 만약 하나의 `@Around`가 `proceed()`를 호출하지 않으면?
- 그 이후의 Aspect도 실행되지 않고, 실제 메서드도 실행되지 않아요.
- 즉, 체인을 끊는 역할을 하게 돼요.
## 요약

| 질문 | 답변 |
|------|------|
| `@Around`에서 메서드 실행을 막을 수 있나? | `proceed()`를 호출하지 않으면 실행 안 됨 |
| 다른 Aspect도 적용되면? | 순서대로 체인처럼 실행됨 |
| 하나라도 `proceed()` 안 하면? | 이후 Aspect 및 대상 메서드 실행 안 됨 |
| 순서 제어 방법 | `@Order` 또는 `Ordered` 인터페이스 사용 |

## "체인을 끊는다"는 의미
`@Around` Advice에서 `proceed()`를 호출하지 않으면:
- 대상 메서드가 실행되지 않음
- 그 메서드에 연결된 다른 Advice들도 실행되지 않음
- 특히 `@After`, `@AfterReturning`, `@AfterThrowing`은 대상 메서드가 실행된 후에만 작동하므로, `proceed()`가 호출되지 않으면 실행되지 않음
### 예시
```java
@Around("execution(* setName(..))")
public Object block(ProceedingJoinPoint pjp) throws Throwable {
    System.out.println("▶ Around 시작");
    // proceed() 생략 → 메서드 실행 안 됨
    return null;
}
```
이 경우:
- `@Before` Advice는 실행됨
- `@Around` Advice는 실행됨
- `@After`, `@AfterReturning`은 실행 안 됨
## Advice 실행 순서
Spring AOP는 여러 Advice가 하나의 JoinPoint에 걸려 있을 때 다음 기준으로 실행 순서를 결정해요:
### 1. Advice 종류별 실행 순서

| Advice 종류 | 실행 시점 |
|-------------|------------|
| `@Before` | 대상 메서드 전 |
| `@Around` | 대상 메서드 전/후 모두 (직접 실행 여부 결정) |
| `@After` | 대상 메서드 후 |
| `@AfterReturning` | 대상 메서드가 정상 종료된 후 |
| `@AfterThrowing` | 대상 메서드가 예외 발생 시 |

### 2. Aspect 간 순서 제어
- `@Order(n)` 어노테이션으로 명시 가능
- `@Around` Advice는 중첩 구조로 실행됨 (가장 낮은 `@Order`가 가장 바깥쪽)
### 예시: 두 Aspect가 같은 메서드에 적용
```java
@Aspect
@Order(1)
public class OuterAspect {
    @Around("execution(* setName(..))")
    public Object outer(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("▶ Outer Before");
        Object result = pjp.proceed();
        System.out.println("▶ Outer After");
        return result;
    }
}
@Aspect
@Order(2)
public class InnerAspect {
    @Around("execution(* setName(..))")
    public Object inner(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("▶ Inner Before");
        Object result = pjp.proceed();
        System.out.println("▶ Inner After");
        return result;
    }
}
```
실행 순서:
```
▶ Outer Before
▶ Inner Before
▶ 실제 메서드 실행
▶ Inner After
▶ Outer After
```
## 요약

| 질문 | 답변 |
|------|------|
| `proceed()`를 생략하면? | 대상 메서드 + 이후 Advice 실행 안 됨 |
| `@Before`는 실행되나? | 실행됨 (메서드 실행 전이므로) |
| Advice 순서 제어 방법 | `@Order`, 등록 순서 |
| `@Around`의 실행 구조 | 중첩 호출 (가장 낮은 `@Order`가 가장 바깥) |
