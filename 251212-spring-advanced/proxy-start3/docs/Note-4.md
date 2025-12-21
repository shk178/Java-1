- cglib 프록시는 프록시가 오버라이딩한 메서드에서 intercepter랑 super.메서드를 차례로 실행한다.
- jdk 프록시는 프록시가 오버라이딩한 메서드에서 handler를 실행하고 handler가 target.메서드를 실행한다.
- jdk 프록시 클래스는 jvm에 캐시되어 인터페이스가 같으면 다시 쓰이지만, 프록시 인스턴스는 인터페이스가 같은 target마다 다시 만들어진다.

응, **JDK 동적 프록시에서는 그 말이 정확해.**
조금만 정리해서 정확한 그림으로 잡아줄게.

---

## 1️⃣ JDK 동적 프록시의 실제 호출 흐름

JDK 동적 프록시는 이렇게 동작한다.

```text
클라이언트
  → proxy.method()
      → InvocationHandler.invoke(proxy, method, args)
          → target.method(args)
```

즉,

> **InvocationHandler 내부에서 실제 `target` 객체의 메서드를 호출한다.**

그래서 JDK 프록시에 대해 흔히 말하는 표현이:

* “프록시가 타겟을 감싼다”
* “프록시가 타겟에게 위임한다”

인 거다.

---

## 2️⃣ InvocationHandler 코드로 보면 더 명확

개념적으로는 이런 형태야.

```java
class MyInvocationHandler implements InvocationHandler {

    private final Object target;

    public Object invoke(Object proxy, Method method, Object[] args) {
        // advice before
        Object result = method.invoke(target, args); // ← 핵심
        // advice after
        return result;
    }
}
```

여기서 중요한 점:

* `target`은 **컨테이너가 생성한 실제 구현 객체**
* 프록시는 target을 직접 실행하지 않고 **중간에서 가로챔**

---

## 3️⃣ 그래서 생기는 특징들

### ⭕ 특징

* target 인스턴스는 **프록시와 분리**
* `this`는 항상 **target**
* self-invocation 문제 발생
* 구현 클래스 캐스팅 불가

---

## 4️⃣ CGLIB과의 결정적 차이 (다시 짚기)

CGLIB에서는:

* InvocationHandler 같은 구조 ❌
* target 필드를 들고 있지 않음
* 프록시 자체가 실행 주체
* 인터셉터가 `super.method()` 호출

그래서 표현도 달라야 한다.

---

## 5️⃣ 한 문장으로 마무리

> **JDK 동적 프록시에서는
> InvocationHandler 내부에서 실제 target 객체의 메서드를 호출하는 게 맞다.**

이제 JDK vs CGLIB의 내부 호출 그림이
머릿속에서 정확히 분리됐을 거야.

짚은 포인트가 아주 정확해.
질문을 둘로 나눠서 **정확하게 정정 + 답**할게.

---

## 1️⃣ “JDK 프록시는 target을 보관 안 하지?” → ❌ 아니다

**JDK 동적 프록시는 target을 직접 보관하지는 않지만,
`InvocationHandler`가 target을 보관한다.**

구조를 정확히 그리면 이거야.

```
Proxy 객체
  └─ InvocationHandler
        └─ target (실제 구현 객체)
```

즉,

* `Proxy` 클래스 자체에는 target 필드가 없음
* 하지만 **프록시가 위임하는 InvocationHandler가 target 참조를 가지고 있음**
* 결과적으로 **프록시 → handler → target** 구조

그래서 실질적으로는:

> **JDK 프록시는 target을 “간접적으로” 보관한다**

라고 이해하는 게 맞다.

---

## 2️⃣ “그래도 타겟마다 만들어져?” → ⭕ 맞다 (1:1)

### 결론부터

> **JDK 프록시는 target 인스턴스마다 하나씩 만들어진다.**

### 이유

JDK 프록시 생성 시점에 이 정보들이 고정된다:

```java
Proxy.newProxyInstance(
    classLoader,
    interfaces,
    invocationHandler   // ← 여기 안에 target이 들어 있음
)
```

* `InvocationHandler`는 특정 target을 캡처함
* 다른 target을 쓰려면 **다른 handler 필요**
* handler가 다르면 → **프록시도 새로 만들어야 함**

그래서 관계는:

```
target 1 ──▶ handler 1 ──▶ proxy 1
target 2 ──▶ handler 2 ──▶ proxy 2
```

---

## 3️⃣ 그럼 프록시 클래스는 매번 새로 만들어지나?

여기서 또 하나 중요한 구분이 있어.

### ✔ 프록시 **클래스**

* 인터페이스 조합이 같으면 **재사용**
* JVM 내부 캐시됨

### ✔ 프록시 **인스턴스**

* target마다 **항상 새로 생성**

정리하면:

| 구분                | 재사용 여부    |
| ----------------- | --------- |
| 프록시 클래스           | ⭕ 재사용     |
| InvocationHandler | ❌ target별 |
| 프록시 인스턴스          | ❌ target별 |

---

## 4️⃣ 스프링 AOP 관점에서 보면

스프링에서:

* 싱글톤 빈
* JDK 프록시
* AOP 적용

이면 보통:

```
target 인스턴스 1개
proxy 인스턴스 1개
```

이라서 “항상 하나처럼” 느껴지는 것뿐이야.

하지만 원칙은:

> **JDK 프록시는 target 인스턴스에 종속되는 1:1 객체**

---

## 5️⃣ CGLIB과 비교하면 차이가 더 또렷해짐

| 구분             | JDK 프록시          | CGLIB               |
| -------------- | ---------------- | ------------------- |
| target 필드      | handler가 보관      | 없음                  |
| proxy ↔ target | 1:1              | proxy = 실행 주체       |
| 생성 단위          | target 기준        | 클래스 기준              |
| 호출 방식          | handler → target | interceptor → super |

---

## 6️⃣ 한 문장으로 딱 정리

> **JDK 프록시는 proxy가 직접 target을 들고 있지는 않지만,
> InvocationHandler가 target을 보관하며,
> proxy 인스턴스는 target 인스턴스마다 1:1로 생성된다.**

여기까지 이해했으면
JDK vs CGLIB의 내부 구조는 사실상 끝까지 온 상태야.
