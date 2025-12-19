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
