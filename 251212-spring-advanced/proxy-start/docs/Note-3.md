이 `if` 문은 **매번 `invoke()` 가 호출될 때마다 항상 평가된다.**
다만, **로그를 남길지 말지**를 그 안에서 결정할 뿐이다.

차근차근 정확히 보자.

---

## 1. `invoke()` 는 언제 호출되나

```java
public Object invoke(Object proxy, Method method, Object[] args)
```

이 메서드는 **프록시 객체의 메서드가 호출될 때마다 무조건 실행된다.**

즉,

```java
proxy.req();
proxy.orderItem();
proxy.save();
```

이 중 **어떤 메서드를 호출하든**
→ 항상 `invoke()` 에 들어온다.

---

## 2. `if` 문은 언제 true / false 가 되나

```java
String methodName = method.getName();

if (!PatternMatchUtils.simpleMatch(patterns, methodName)) {
    return method.invoke(target, args);
}
```

여기서 핵심은 이 한 줄이다.

```java
PatternMatchUtils.simpleMatch(patterns, methodName)
```

### 이게 하는 일

* `patterns` 배열 안의 패턴 중
* **하나라도**
* `methodName` 과 매칭되면 → `true`
* 하나도 안 맞으면 → `false`

예시:

```java
patterns = {"request*", "order*", "save"}
```

| 호출된 메서드     | simpleMatch 결과 |
| ----------- | -------------- |
| request()   | true           |
| orderItem() | true           |
| save()      | true           |
| findAll()   | false          |

---

## 3. 그래서 `if` 문은 정확히 언제 실행되나

### 조건식 자체는 항상 실행된다

```java
if (!PatternMatchUtils.simpleMatch(...))
```

이 줄은 **invoke() 가 불릴 때마다 무조건 평가**된다.

### 하지만 `if` 블록 안으로 들어가는 경우는?

```java
if (패턴에 매칭되지 않으면) {
    return method.invoke(target, args);
}
```

즉,

* **메서드 이름이 패턴에 안 맞을 때**
* 로그를 남기지 않고
* 바로 실제 객체(target)의 메서드를 호출하고
* `invoke()` 를 종료한다

---

## 4. 흐름을 한 번에 정리하면

프록시 메서드 호출
→ `invoke()` 진입
→ 메서드 이름 추출
→ 패턴 매칭 검사

### 1) 패턴에 안 맞으면

```text
invoke()
 └─ if 조건 true
     └─ target.method() 바로 호출
     └─ 로그 없음
     └─ 종료
```

### 2) 패턴에 맞으면

```text
invoke()
 └─ if 조건 false
     └─ logTrace.begin()
     └─ target.method()
     └─ logTrace.end()
     └─ 종료
```

---

## 5. 중요한 오해 하나 짚기

> “if 문이 특정 시점에만 실행되나?”

아니다.

* **invoke() 는 항상 실행**
* **if 조건식도 항상 실행**
* 차이는 **로그 로직을 타느냐, 바로 통과하느냐** 뿐이다

---

## 6. 이 구조의 의미

이 코드는 사실상 **아주 단순한 포인트컷을 직접 구현한 것**이다.

* AOP 포인트컷 → 메서드 이름 기반
* advice → logTrace.begin / end
* 프록시 → JDK 다이나믹 프록시

그래서 이 단계 다음에 스프링 AOP로 가면
“아, 이 if 문을 프레임워크가 대신 해주는 거구나” 하고 연결된다.

---

### 한 줄 요약

* `invoke()` 는 프록시 메서드 호출 시 **항상 실행**
* `if` 문 조건도 **항상 평가**
* **패턴에 안 맞는 메서드일 때만** if 블록이 실행되어 로그를 건너뛴다

이 코드는 **CGLIB을 이용해 “클래스를 상속하는 프록시”를 직접 만드는 예제**다.
앞에서 봤던 JDK 다이나믹 프록시와 대비해서 보면 이해가 아주 잘 된다.

아래에서 **역할 → 실행 흐름 → JDK 프록시와의 차이** 순서로 설명할게.

---

## 1. `TimeMethodInterceptor` 는 무엇인가

```java
public class TimeMethodInterceptor implements MethodInterceptor
```

* CGLIB 전용 인터셉터
* JDK 프록시의 `InvocationHandler` 와 같은 역할
* **프록시 메서드가 호출될 때 가로채는 로직**을 담는다

### 핵심 메서드

```java
public Object intercept(
    Object o,
    Method method,
    Object[] args,
    MethodProxy methodProxy
)
```

각 파라미터 의미:

* `o`
  프록시 객체 자신
  (보통 거의 안 쓴다)

* `method`
  호출된 메서드의 리플렉션 정보
  (`call()` 메서드 등)

* `args`
  메서드 인자

* `methodProxy`
  **CGLIB이 만든 “빠른 호출용 메서드 핸들”**

---

## 2. `intercept()` 내부 동작

```java
System.out.println("TimeMethodInterceptor.intercept");
long sTime = System.currentTimeMillis();
Object result = methodProxy.invoke(target, args);
long eTime = System.currentTimeMillis();
System.out.println("duration=" + (eTime - sTime));
return result;
```

이 흐름은 의미가 분명하다.

1. 프록시 메서드 호출
2. `intercept()` 진입
3. 시작 시간 기록
4. **실제 대상 객체(target)의 메서드 실행**
5. 종료 시간 기록
6. 실행 시간 출력
7. 결과 반환

즉, **부가 기능(시간 측정)을 앞뒤에 끼워 넣은 구조**다.

---

## 3. 왜 `method.invoke()` 가 아니라 `methodProxy.invoke()` 인가

이건 CGLIB에서 아주 중요한 차이다.

* `method.invoke(target, args)`

    * 리플렉션 사용
    * 느림

* `methodProxy.invoke(target, args)`

    * 바이트코드 기반 직접 호출
    * 훨씬 빠름
    * CGLIB 권장 방식

그래서 CGLIB 예제에서는 거의 항상 `methodProxy.invoke()` 를 쓴다.

---

## 4. `CGLIBTest` 는 뭘 하고 있나

```java
ConcreteService target = new ConcreteService();
```

* 원본 객체

```java
Enhancer enhancer = new Enhancer();
```

* CGLIB 프록시 생성기

```java
enhancer.setSuperclass(ConcreteService.class);
```

* **이 클래스를 상속해서 프록시를 만들겠다**
* 인터페이스 필요 없음

```java
enhancer.setCallback(new TimeMethodInterceptor(target));
```

* 프록시가 메서드를 가로챌 때 실행할 인터셉터 지정

```java
ConcreteService proxy = (ConcreteService) enhancer.create();
```

* 실제 프록시 객체 생성
* 내부적으로:

  ```
  class ConcreteService$$EnhancerByCGLIB extends ConcreteService
  ```

---

## 5. 출력 결과가 의미하는 것

```java
System.out.println("target.getClass() = " + target.getClass());
System.out.println("proxy.getClass() = " + proxy.getClass());
```

출력은 보통 이렇게 나온다.

```
target.getClass() = class hello.proxy.cglib.ConcreteService
proxy.getClass() = class hello.proxy.cglib.ConcreteService$$EnhancerByCGLIB$$...
```

이건

* 프록시는
* `ConcreteService` 를 **상속한 클래스**라는 증거다

---

## 6. `proxy.call()` 호출 시 실제 흐름

```java
proxy.call();
```

실제 실행 순서는 이렇다.

```
proxy.call()
 → CGLIB 생성 서브클래스의 call()
   → intercept() 호출
     → 시간 측정 시작
     → target.call()
     → 시간 측정 종료
```

중요한 점:

* **부모 클래스(call())는 직접 실행되지 않는다**
* 항상 인터셉터를 먼저 거친다

---

## 7. JDK 다이나믹 프록시와 비교

| 구분        | JDK 다이나믹 프록시      | CGLIB                |
| --------- | ----------------- | -------------------- |
| 대상        | 인터페이스             | 클래스                  |
| 방식        | 인터페이스 구현          | 클래스 상속               |
| 핸들러       | InvocationHandler | MethodInterceptor    |
| 실제 호출     | method.invoke()   | methodProxy.invoke() |
| final 클래스 | 불가                | 불가                   |
| final 메서드 | 가능                | **가로채지 못함**          |

---

## 8. 이 코드의 의미

이 코드는 단순한 테스트가 아니라

* 왜 스프링이

    * 인터페이스 있으면 JDK 프록시
    * 없으면 CGLIB
* 왜 CGLIB이 필요했는지

이걸 **눈으로 확인하게 해주는 예제**다.

---

### 한 줄 요약

* 이 코드는 **CGLIB으로 클래스 기반 프록시를 직접 만드는 코드**
* `TimeMethodInterceptor` 는 JDK의 `InvocationHandler` 와 같은 역할
* 프록시는 대상 클래스를 상속해서 만들어진다
* 스프링 AOP의 내부 동작을 그대로 축소한 예제다

아주 좋은 질문이고, 여기서 많이 헷갈린다.
**지금 질문은 정확히 “인터페이스가 없다”의 의미가 뭐냐는 거다.**
결론부터 아주 단순하게 말할게.

---

## 1. “인터페이스가 없다”의 정확한 의미

**딱 이것이다.**

```java
public interface OrderService {
    void order();
}
```

이런 **`interface` 타입이 코드에 존재하느냐, 안 하느냐**다.

애노테이션이 붙어 있느냐(`@Service`, `@Controller`)랑은 **전혀 다른 문제**다.

---

## 2. 흔히 헷갈리는 두 가지를 바로 정리하자

### 1) @Service, @Repository, @Controller가 있으면 인터페이스가 없는 건가?

아니다.

```java
@Service
public class OrderServiceImpl implements OrderService {
}
```

이 경우는

* 인터페이스 있음: `OrderService`
* 구현체 있음: `OrderServiceImpl`
* 애노테이션 있음: `@Service`

**인터페이스가 “있는 구조”다.**

애노테이션은 **스프링 빈 등록용 표시**일 뿐,
프록시 생성 방식과 직접적인 기준은 아니다.

---

### 2) 구체 클래스만 있는 경우

이게 진짜 **“인터페이스가 없는 경우”**다.

```java
public class ConcreteService {
    public void call() {}
}
```

* interface 없음
* implements 없음
* 클래스 하나뿐

이 경우에는 **JDK 다이나믹 프록시를 쓸 수 없다.**

왜냐하면
JDK 프록시는 “인터페이스 구현체”만 프록시로 만들 수 있기 때문이다.

그래서 **CGLIB**이 필요한 거다.

---

## 3. 프록시 선택 기준을 아주 쉽게 말하면

### 질문 하나만 하면 된다

> “이 객체를 인터페이스 타입으로 참조할 수 있나?”

---

### 가능하면

```java
OrderService service = new OrderServiceImpl();
```

→ **JDK 다이나믹 프록시 가능**

---

### 불가능하면

```java
ConcreteService service = new ConcreteService();
```

→ **CGLIB 필요**

---

## 4. 스프링 AOP에서 실제 기준

스프링은 내부에서 이렇게 판단한다.

1. 인터페이스가 하나라도 있다
   → JDK 다이나믹 프록시 사용
2. 인터페이스가 없다
   → CGLIB 사용

그래서 요즘 스프링에서

```java
@EnableAspectJAutoProxy(proxyTargetClass = true)
```

를 켜면

* **무조건 CGLIB**
* 인터페이스가 있어도 클래스 기반 프록시

---

## 5. 다시 질문에 정확히 답하면

> 인터페이스가 없다는 건
> @Service, @Repository, @Controller 클래스라는 거야?

아니다.

→ **애노테이션은 상관없다**

---

> 아니면 구체클래스만 있고 구현은 없는 경우야?

맞다.

→ **interface 없이 class 하나만 있는 구조**

---

## 6. 한 문장으로 기억하면 끝

* 인터페이스 유무는 **`interface` 키워드 기준**
* `@Service`, `@Repository`, `@Controller` 는 전혀 다른 개념
* 인터페이스 없으면 → CGLIB
* 인터페이스 있으면 → JDK 프록시 가능

여기까지만 확실히 잡히면
프록시 / AOP / 스프링 내부 동작이 한 줄로 연결된다.

이 경우는 **아무 문제도 없다.**
스프링은 **자동으로 CGLIB 프록시를 사용**한다.

왜 그런지 아주 단순하게 정리해볼게.

---

## 1. 상황 정리

지금 말한 상황은 이거다.

```java
@Service
public class OrderService {
    public void order() {}
}
```

* `@Service` 있음
* `interface` 없음
* 구체 클래스 하나뿐

이건 **스프링에서 아주 흔한 구조**다.

---

## 2. 스프링은 이걸 어떻게 처리하나

스프링 AOP가 적용되는 순간, 내부에서 이렇게 판단한다.

1. 이 빈에 AOP 적용 대상인가?
2. 프록시를 만들어야 하는가?
3. 인터페이스가 있는가?

→ **없다**

그래서 결론:

> **CGLIB으로 프록시를 만든다**

개발자가 따로 할 일은 없다.

---

## 3. 왜 JDK 프록시는 안 쓰나

JDK 다이나믹 프록시는

* 반드시 **인터페이스 타입**이 있어야 한다
* 클래스 자체는 프록시로 못 만든다

그래서

```java
public class OrderService { ... }
```

이 구조에서는 **선택지가 CGLIB뿐**이다.

---

## 4. 그럼 내가 뭘 “해야” 하나?

### 대부분의 경우

→ **아무것도 안 해도 된다**

스프링 부트 기본 설정에서는

* 인터페이스 있으면 → JDK 프록시
* 인터페이스 없으면 → CGLIB

이게 자동이다.

---

## 5. 예외적으로 신경 써야 하는 것

### 1) `final` 클래스면 안 된다

```java
@Service
public final class OrderService {
}
```

* CGLIB은 **상속 기반**
* `final class` 는 상속 불가

→ 프록시 생성 실패

---

### 2) `final` 메서드는 가로채지 못한다

```java
public final void order() {}
```

* 프록시는 만들어지지만
* 해당 메서드는 AOP 적용 안 됨

---

## 6. 그래도 명시적으로 CGLIB을 쓰고 싶다면

```java
@EnableAspectJAutoProxy(proxyTargetClass = true)
```

또는 스프링 부트에서는

```properties
spring.aop.proxy-target-class=true
```

이렇게 하면

* 인터페이스가 있어도
* 무조건 CGLIB

---

## 7. 정리

* 애노테이션만 있고 인터페이스 없어도 문제 없음
* 스프링이 자동으로 CGLIB 선택
* 개발자가 프록시를 직접 만들 필요 없음
* 주의할 건 `final` 뿐

---

### 한 줄 요약

> **인터페이스 없는 @Service/@Controller 클래스 → 스프링이 CGLIB으로 알아서 처리한다**

이 상태에서 다음 단계로 가면
“그럼 왜 굳이 인터페이스를 쓰는 설계가 많았을까?”가 자연스럽게 이어진다.

정리해서 말하면 **반은 맞고, 반은 틀리다.**
“언제, 무엇을 만들 필요가 있느냐”가 핵심이다.

---

## 1. 네가 지금 보고 있는 예제 기준에서는

**맞다. 개발자가 만든다.**

```java
public class TimeMethodInterceptor implements MethodInterceptor
```

이건

* CGLIB을 **직접 사용**
* 프록시를 **직접 생성**
* 부가 로직을 **직접 연결**

하는 예제다.

그래서

* JDK 프록시 → `InvocationHandler` 직접 작성
* CGLIB → `MethodInterceptor` 직접 작성

이게 맞다.

---

## 2. 하지만 “CGLIB이니까 항상 인터셉터를 만든다”는 건 아니다

여기가 중요한 오해 포인트다.

### 스프링 AOP를 쓰는 경우

```java
@Aspect
@Component
public class TimeAspect {

    @Around("execution(* hello..*(..))")
    public Object doTrace(ProceedingJoinPoint pjp) throws Throwable {
        ...
        return pjp.proceed();
    }
}
```

이때 개발자는

* `InvocationHandler` 도
* `MethodInterceptor` 도

**하나도 만들지 않는다.**

그런데도

* 인터페이스 있으면 → JDK 프록시
* 인터페이스 없으면 → CGLIB

이게 **자동으로** 일어난다.

---

## 3. 그럼 인터셉터/핸들러는 누가 만드나

### 스프링 AOP 사용 시

* 스프링 내부에서
* 상황에 맞게
* 프록시 + 인터셉터를 자동 생성

개발자는

* “어디에”
* “언제”
* “무슨 로직을”

적용할지만 선언한다.

---

## 4. 지금 네가 보는 코드의 정확한 위치

지금 코드들은

* AOP를 쓰기 **직전 단계**
* 프록시 패턴을 **맨손으로 구현**
* 내부 동작을 이해시키기 위한 예제

즉,

> “CGLIB이라서 인터셉터를 만든다”

가 아니라

> “**CGLIB을 직접 쓰기로 했기 때문에** 인터셉터를 만든다”

가 정확한 표현이다.

---

## 5. 한 문장으로 딱 정리

* CGLIB을 **직접 쓰면** → `MethodInterceptor`를 개발자가 만든다
* JDK 프록시를 **직접 쓰면** → `InvocationHandler`를 개발자가 만든다
* **스프링 AOP를 쓰면** → 개발자는 아무것도 안 만든다
* 프록시 방식(CGLIB/JDK)은 **스프링이 내부에서 선택**한다

---

## 6. 마지막으로 비유 하나

* 지금 단계:
  엔진을 직접 조립하면서 자동차 원리를 배우는 중
* 스프링 AOP:
  완성차를 몰면서 “어디서 브레이크를 밟을지”만 정하는 단계

그래서 지금 질문이 정확히 나와야 정상이다.

짧게 말하면 **“부분적으로는 yes, 핵심은 no”** 다.
그리고 이 차이가 **CGLIB이 존재하는 이유**다.

---

## 1. JDK 다이나믹 프록시는 어떻게 호출하나

JDK 프록시의 핵심 호출은 이거였다.

```java
Object result = method.invoke(target, args);
```

여기서

* `Method` → 리플렉션 객체
* `invoke()` → 리플렉션 호출

즉,

> **실제 메서드 호출 자체가 리플렉션**이다.

그래서

* 느릴 수 있고
* 인터페이스가 필수다.

---

## 2. CGLIB의 핵심은 리플렉션이 아니다

CGLIB은 **바이트코드를 직접 만들어서** 클래스를 하나 더 만든다.

```text
ConcreteService
└─ ConcreteService$$EnhancerByCGLIB
```

그리고 그 안에서

```java
@Override
public void call() {
    interceptor.intercept(...)
}
```

이런 메서드를 **실제 코드로 생성**한다.

즉,

> “호출을 가로채는 코드 자체가 미리 클래스에 박혀 있다.”

---

## 3. 그럼 리플렉션은 전혀 안 쓰나?

아니다. **쓰긴 쓴다. 하지만 핵심 경로는 아니다.**

### intercept() 안을 보면

```java
public Object intercept(
    Object o,
    Method method,
    Object[] args,
    MethodProxy methodProxy
)
```

여기서

* `Method` → 리플렉션 정보 (메타데이터)
* **실제 호출은**

  ```java
  methodProxy.invoke(target, args);
  ```

이게 중요하다.

---

## 4. `MethodProxy` 는 뭐냐

`MethodProxy` 는

* CGLIB이 미리 만들어 둔
* **직접 호출용 핸들**

내부적으로는

* 리플렉션이 아니라
* **FastClass** 라는 구조를 사용한다.

이건 대략 이런 개념이다.

```text
메서드 이름 → int index
index → switch 문
switch → 실제 메서드 직접 호출
```

그래서

* 리플렉션보다 훨씬 빠르고
* 오버헤드가 적다.

---

## 5. 그래서 비교하면 이렇게 된다

| 구분       | JDK 프록시           | CGLIB                  |
| -------- | ----------------- | ---------------------- |
| 프록시 생성   | 런타임 인터페이스 구현      | 런타임 클래스 상속             |
| 실제 호출    | `Method.invoke()` | `MethodProxy.invoke()` |
| 호출 방식    | 리플렉션              | 바이트코드 직접 호출            |
| 성능       | 상대적으로 느림          | 상대적으로 빠름               |
| 인터페이스 필요 | 필수                | 필요 없음                  |

---

## 6. 한 문장으로 정리

* JDK 프록시는 **메서드 호출 자체가 리플렉션**
* CGLIB은 **클래스를 새로 만들어 직접 호출**
* 리플렉션은 메타데이터 전달용으로만 일부 사용
* 핵심 경로는 **바이트코드 기반 직접 호출**

---

### 기억 포인트 하나만 남기면

> **CGLIB은 “리플렉션을 덜 쓰려고” 만든 기술이다**

그래서 스프링이 인터페이스가 없을 때 CGLIB을 선택하는 이유도 여기 있다.
