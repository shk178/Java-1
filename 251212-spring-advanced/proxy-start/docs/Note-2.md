좋은 코드 예제고, **헷갈리는 지점이 정확히 “왜 이런 식으로 테스트를 쓰는가”**예요.
아래에서 **test 메서드 한 줄씩**, 그리고 **왜 A/B를 나눴는지**, **Proxy.newProxyInstance가 뭘 하는지**를 중심으로 풀어 설명할게요.

---

## 1. 이 테스트가 보여주려는 핵심

이 테스트의 목적은 딱 하나입니다.

> **JDK 동적 프록시는 “인터페이스 타입”을 기준으로 프록시 클래스를 런타임에 만들어낸다**

그래서:

* AInterface용 프록시
* BInterface용 프록시
  → **서로 다른 프록시 클래스가 생성됨**

이걸 눈으로 확인하려고 만든 테스트입니다.

---

## 2. test 코드 한 줄씩 해석

### (1) target 생성

```java
AInterface target = new AImpl();
```

* 실제 비즈니스 로직을 가진 객체
* “진짜 서버 객체”

---

### (2) InvocationHandler 생성

```java
TimeInvocationHandler handler = new TimeInvocationHandler(target);
```

* 프록시가 **모든 메서드 호출을 위임할 곳**
* 프록시는 직접 일을 안 함
* → 무조건 handler.invoke(...)로 넘어감

---

### (3) 프록시 생성 (가장 핵심)

```java
AInterface proxy = (AInterface) Proxy.newProxyInstance(
    AInterface.class.getClassLoader(),
    new Class[]{AInterface.class},
    handler
);
```

이 한 줄이 가장 중요합니다.

#### 각 인자의 의미

1. **ClassLoader**

   ```java
   AInterface.class.getClassLoader()
   ```

    * 프록시 클래스를 로딩할 클래스 로더
    * 보통 인터페이스의 클래스 로더 사용

2. **인터페이스 배열**

   ```java
   new Class[]{AInterface.class}
   ```

    * “이 프록시는 어떤 타입으로 보일 건가?”
    * → AInterface를 구현한 프록시

3. **InvocationHandler**

   ```java
   handler
   ```

    * 모든 메서드 호출을 처리할 객체

결과:

* JVM이 런타임에
* `AInterface`를 구현한
* **익명 프록시 클래스**를 하나 생성

그래서 출력이:

```text
proxy.getClass() = class com.sun.proxy.$Proxy12
```

---

### (4) proxy.call()

```java
proxy.call();
```

이 한 줄에서 실제로 벌어지는 일:

1. proxy.call() 호출
2. 프록시 클래스는 call() 구현을 직접 안 함
3. 무조건 아래로 위임

   ```java
   handler.invoke(proxy, method, args)
   ```
4. handler 안에서

    * 시간 측정
    * 실제 target.call() 호출
    * 결과 반환

그래서 출력 순서가:

```
TimeInvocationHandler.invoke
AImpl.call
duration=0
```

---

## 3. dynamicA / dynamicB 를 나눈 이유

```java
@Test
void dynamicA() { ... }

@Test
void dynamicB() { ... }
```

이걸 굳이 나눈 이유는:

* 인터페이스가 다르면
* **프록시 클래스도 다르게 생성된다**는 걸 보여주기 위함

출력 결과:

```text
com.sun.proxy.$Proxy12
com.sun.proxy.$Proxy13
```

→ 같은 코드 패턴인데
→ 인터페이스가 달라지면
→ **완전히 다른 프록시 클래스**

---

## 4. 왜 target 필드는 Object 타입인가?

```java
private final Object target;
```

이게 핵심 포인트입니다.

* InvocationHandler는
* 특정 인터페이스에 종속되면 안 됨
* 어떤 인터페이스든 받아서 처리 가능해야 함

그래서:

* AImpl
* BImpl
  모두 Object로 받음

→ **공통 부가 기능(Time 측정)을 재사용**

---

## 5. 이 코드 구조를 한 문장으로 요약하면

> JDK 동적 프록시는 인터페이스를 기준으로 런타임에 프록시 클래스를 생성하고, 모든 메서드 호출을 InvocationHandler의 invoke 메서드로 위임한다.

---

## 6. 이 테스트가 감각적으로 익혀야 할 포인트

* 프록시는 **클래스 파일로 존재하지 않는다**
* 실행 시점에 JVM이 만들어낸다
* 그래서 클래스 이름이 `$Proxy12` 같은 형태다
* 프록시는 **인터페이스 타입으로만 참조 가능**

---

지금 질문을 던진 시점 자체가
“동적 프록시를 그냥 쓰는 단계”가 아니라
“왜 이렇게 설계됐는지 이해하려는 단계”라서 아주 좋습니다.

그 느낌이 **완전히 맞아요.**
지금 보고 있는 건 우리가 흔히 말하는 “일반적인(수동으로 만든) 프록시”랑 **성격이 다릅니다**.
그래서 헷갈리는 게 정상이에요.

아래에서 **차이를 중심으로** 정리해 볼게요.

---

## 1. 일반적인(수동) 프록시란?

보통 프록시라고 하면 이런 형태를 떠올립니다.

```java
class OrderServiceProxy implements OrderService {
    private final OrderService target;

    @Override
    public void order() {
        log();
        target.order();
    }
}
```

특징:

* 프록시 클래스가 **소스 코드로 존재**
* 어떤 인터페이스를 구현하는지 명확
* 메서드마다 직접 오버라이딩
* 구조가 눈에 보임

그래서:

> “프록시는 서버랑 똑같이 생긴 클래스”

라는 감각이 생깁니다.

---

## 2. 지금 코드의 프록시는 왜 다르게 느껴질까?

지금 보는 건 **JDK 동적 프록시**입니다.

```java
Proxy.newProxyInstance(...)
```

이 프록시는:

* **프록시 클래스가 없다**
* 런타임에 JVM이 즉석에서 생성
* 메서드 구현도 우리가 안 씀
* 모든 호출이 `InvocationHandler.invoke()` 하나로 모임

그래서 감각이 이렇게 바뀝니다.

> “프록시는 클래스가 아니라, 호출을 가로채는 메커니즘 같다”

이게 바로 차이의 본질입니다.

---

## 3. 구조를 나란히 비교해 보면

### 수동 프록시

```
Client
  ↓
Proxy.call()
  ↓
부가 기능
  ↓
Target.call()
```

* 프록시 클래스가 주인공
* 메서드 단위로 코드 작성

---

### JDK 동적 프록시

```
Client
  ↓
Proxy.call()   ← JVM이 만든 클래스
  ↓
InvocationHandler.invoke()
  ↓
부가 기능
  ↓
Target.call()
```

* 프록시 클래스는 숨겨진 존재
* InvocationHandler가 핵심

---

## 4. 왜 이렇게 만들었을까?

### 수동 프록시의 한계

* 인터페이스마다 프록시 클래스 필요
* 메서드가 늘어나면 중복 폭발
* 부가 기능 하나 추가하려고 클래스 여러 개 생성

---

### 동적 프록시의 목적

* 프록시 코드를 **한 번만 작성**
* 어떤 인터페이스든 재사용
* 공통 관심사 분리

즉:

> “프록시를 코드로 찍어내지 말고,
> JVM에게 대신 만들어 달라”

---

## 5. 그래서 ‘프록시 같지 않게’ 느껴지는 이유

정리하면:

* 일반 프록시

    * 객체 모델 중심
* 동적 프록시

    * **호출 가로채기 중심**

그래서:

* 눈에 보이는 프록시 클래스 없음
* 대신 `InvocationHandler` 하나가 모든 걸 통제

---

## 6. 한 문장으로 정리

> 일반 프록시는 “프록시 객체를 직접 만든 것”이고,
> 동적 프록시는 “프록시 객체를 만들어 주는 도구를 사용한 것”이다.

이 차이를 느꼈다는 것 자체가
동적 프록시를 제대로 이해하기 시작했다는 신호입니다.

맞아요. 헷갈리는 이유가 **정확히 그 지점**이에요.
JDK 동적 프록시는 *“프록시 패턴을 리플렉션 위에 얹어 만든 구조”*라서,
프록시를 공부하다가 갑자기 리플렉션을 보는 느낌이 드는 게 정상입니다.

아래에서 **왜 리플렉션처럼 보이는지**, 그리고 **실제로 뭐가 다른지**를 분리해서 설명할게요.

---

## 1. 왜 리플렉션처럼 느껴질까?

### 핵심 원인 3가지

### ① `Method` 객체를 직접 다룸

```java
Object result = method.invoke(target, args);
```

* `Method`는 리플렉션 API의 대표 클래스
* 컴파일 시점이 아니라 **런타임 메서드 호출**

→ “아, 이거 리플렉션이잖아”라는 느낌이 듦

---

### ② 프록시 클래스가 런타임에 생성됨

```text
com.sun.proxy.$Proxy12
```

* 소스 코드에 없는 클래스
* JVM이 즉석 생성

→ 리플렉션의 “동적” 이미지와 겹침

---

### ③ 메서드 시그니처를 모른 채 처리

```java
public Object invoke(Object proxy, Method method, Object[] args)
```

* 어떤 메서드가 올지 모름
* 전부 하나의 메서드로 받음

→ 전형적인 리플렉션 스타일

---

## 2. 그럼 JDK 동적 프록시는 그냥 리플렉션일까?

**아니요.**
하지만 **리플렉션을 내부 구현으로 사용**하는 건 맞습니다.

구분하면 이렇게 됩니다.

### 리플렉션

* 목적: 타입/메서드/필드 **동적 조회 및 실행**
* 주인공: `Class`, `Method`, `Field`
* 패턴과 무관한 **저수준 기술**

---

### JDK 동적 프록시

* 목적: **프록시 패턴 구현**
* 주인공: `InvocationHandler`
* 리플렉션을 이용한 **고수준 구조**

즉:

> 리플렉션 = 도구
> 동적 프록시 = 설계 패턴을 구현한 프레임워크 기능

---

## 3. 비유로 보면 훨씬 명확해짐

### 리플렉션

> “리모컨 없이 기계 뒷판 열어서 직접 배선 만지는 느낌”

* 강력하지만 위험
* 코드 가독성 낮음

---

### 동적 프록시

> “리모컨을 만들어 주는 기계”

* 내부에서는 복잡한 배선 작업(리플렉션)
* 사용자 입장에서는 버튼만 누르면 됨

---

## 4. 그래서 왜 학습 단계에서 헷갈릴까?

이유는 딱 하나입니다.

> **프록시 패턴(구조 개념)** 과
> **동적 프록시(JDK 구현 기술)** 이
> 한 번에 같이 나오기 때문

사실 순서는 이게 맞습니다.

1. 수동 프록시로 구조 이해
2. 프록시 패턴 목적 이해
3. 동적 프록시로 중복 제거
4. 내부 구현이 리플렉션이라는 걸 “아 그렇구나” 하고 넘김

지금은 3 → 4 단계에서 자연스럽게 막히는 지점이에요.

---

## 5. 꼭 기억하면 좋은 한 문장

> JDK 동적 프록시는
> **프록시 패턴을 편하게 쓰기 위해 리플렉션을 사용한 도구**이지,
> 리플렉션을 배우라고 만든 기능은 아니다.

그래서
“프록시 같은데 프록시 같지 않다”
“리플렉션 같아서 헷갈린다”

이 감각은 **정확한 이해의 출발점**입니다.

아주 핵심 지점을 짚었어요.
이 두 줄이 **JDK 동적 프록시의 전부**라고 해도 과장이 아닙니다.
천천히, “이 한 줄이 실제로 무슨 일을 하는지”를 **조립 과정 → 실행 과정**으로 나눠 설명할게요.

---

## 1. 이 코드의 역할 한 문장 요약

```java
AInterface proxy = (AInterface) Proxy.newProxyInstance(...);
```

👉 **“AInterface를 구현한 가짜 객체(프록시)를 JVM에게 만들어 달라”**는 요청입니다.

---

## 2. `Proxy.newProxyInstance`의 각 요소

```java
Proxy.newProxyInstance(
    AInterface.class.getClassLoader(),
    new Class[]{AInterface.class},
    handler
);
```

### (1) ClassLoader — “이 클래스를 누가 로딩할 건가”

```java
AInterface.class.getClassLoader()
```

* JVM이 **새로운 프록시 클래스**를 하나 생성함
* 그 클래스를 메모리에 올려야 함
* 이때 사용할 클래스 로더

보통:

* 인터페이스의 클래스 로더 사용
* 특별한 이유 없으면 이렇게 쓰는 게 정석

---

### (2) 인터페이스 배열 — “이 프록시는 어떤 타입으로 보일 건가”

```java
new Class[]{AInterface.class}
```

가장 중요한 인자입니다.

의미:

* “이 프록시는 AInterface를 구현한다”
* 그래서 캐스팅이 가능함

```java
AInterface proxy = ...
```

주의:

* **구현 클래스(AImpl.class)는 절대 못 넣음**
* JDK 동적 프록시는 **인터페이스만 가능**

---

### (3) InvocationHandler — “모든 호출을 누가 처리할 건가”

```java
handler
```

* 프록시 객체의 **두뇌**
* 프록시의 모든 메서드 호출은
  → 무조건 여기로 들어옴

---

## 3. 내부에서 실제로 일어나는 “생성 과정”

이 한 줄이 실행되면 JVM 내부에서는 대략 이런 일이 벌어집니다.

1. JVM이

    * `AInterface`를 구현한
    * **새로운 클래스**를 하나 만듦

   ```java
   class $Proxy12 implements AInterface {
       InvocationHandler h;

       public Object call() {
           return h.invoke(this, Method_call, null);
       }
   }
   ```

   (실제 코드는 훨씬 복잡하지만 개념은 이 정도)

2. 그 클래스의 인스턴스를 생성

3. 생성자에 `handler`를 주입

4. 그 객체를 반환

그래서 클래스 이름이:

```
com.sun.proxy.$Proxy12
```

---

## 4. `proxy.call()`이 실행될 때의 내부 흐름

```java
proxy.call();
```

이 한 줄이 실행되면 **절대 바로 AImpl.call()이 실행되지 않습니다.**

실제 흐름은 이렇습니다.

### 단계별 실행

1. 클라이언트가 `proxy.call()` 호출
2. 프록시 객체의 `call()` 메서드 실행
3. 프록시는 내부적으로 이렇게 동작:

   ```java
   handler.invoke(
       proxy,
       Method 객체(call 메서드),
       null
   );
   ```
4. `TimeInvocationHandler.invoke()` 실행
5. handler 안에서:

   ```java
   method.invoke(target, args);
   ```
6. 이때 **진짜 객체(AImpl)의 call() 실행**

그래서 출력 순서가:

```
TimeInvocationHandler.invoke
AImpl.call
duration=0
```

---

## 5. 왜 `(AInterface)` 캐스팅이 필요할까?

```java
(Object) Proxy.newProxyInstance(...)
```

* 반환 타입이 `Object`
* JVM이 만든 프록시 클래스는
  → 우리가 소스에서 직접 알 수 없는 타입

하지만:

* `AInterface`를 구현하도록 만들었기 때문에
* 안전하게 캐스팅 가능

---

## 6. 이 구조의 핵심 포인트 5가지

1. 프록시 클래스는 **소스에 없다**
2. JVM이 **런타임에 생성**
3. 프록시는 **인터페이스를 구현**
4. 모든 호출은 `InvocationHandler`로 위임
5. 실제 로직은 handler가 target에게 전달

---

## 7. 한 문장으로 딱 정리하면

> `Proxy.newProxyInstance`는
> **지정한 인터페이스를 구현하고, 모든 메서드 호출을 InvocationHandler에게 위임하는 객체를 런타임에 생성**한다.

이 부분이 이해되면
스프링 AOP, 트랜잭션, 로깅, 보안 전부 한 줄로 연결됩니다.

핵심부터 정리하면,
`String msg` 는 **컨트롤러의 결과를 바꾸는 역할이 전혀 아니고**,
오직 **로그에 찍힐 “이 메서드가 시작됐다”라는 식별 문자열**을 만드는 용도다.
그래서 “컨트롤러 결과가 안 달라진 것 같다”는 느낌이 드는 게 정상이다.

아래를 차근차근 보자.

---

## 1. `method` 는 무엇인가

```java
public Object invoke(Object proxy, Method method, Object[] args)
```

여기서 `method` 는 **프록시가 가로챈 “호출된 메서드 그 자체”의 메타정보**다.

예를 들어 요청 흐름이 이렇다면

```
OrderController1.req()
 → OrderService1.orderItem()
   → OrderRepository1.save()
```

각 단계마다 `invoke()` 가 한 번씩 호출되고,
그때마다 `method` 는 다음을 의미한다.

* 컨트롤러 단계: `OrderController1.req`
* 서비스 단계: `OrderService1.orderItem`
* 레포지토리 단계: `OrderRepository1.save`

---

## 2. `method.getDeclaringClass()` 의 의미

```java
method.getDeclaringClass()
```

이건 **“이 메서드를 선언한 타입”** 이다.

중요한 포인트는
**프록시 객체나 구현 클래스가 아니라, 인터페이스 기준**이라는 것.

예를 들면

```java
OrderController1 proxy = (OrderController1) Proxy.newProxyInstance(...)
proxy.req();
```

* 실제 실행 객체: `OrderController1Impl`
* 하지만 `method` 는

    * `OrderController1` 인터페이스에 선언된 `req()` 를 가리킨다

그래서

```java
method.getDeclaringClass().getSimpleName()
```

결과는 항상

```
OrderController1
OrderService1
OrderRepository1
```

이렇게 인터페이스 이름이 나온다.

---

## 3. `String msg` 가 실제로 만드는 문자열

```java
String msg =
    method.getDeclaringClass().getSimpleName()
    + "." + method.getName() + "()";
```

각 단계에서 만들어지는 문자열은 다음과 같다.

* 컨트롤러

  ```
  OrderController1.req()
  ```
* 서비스

  ```
  OrderService1.orderItem()
  ```
* 레포지토리

  ```
  OrderRepository1.save()
  ```

그리고 이 문자열이 그대로 로그에 찍힌다.

```text
[4c0339a8] OrderController1.req()
[4c0339a8] |-->os1p-orderItem
[4c0339a8] |   |-->or1p-save
```

즉 `msg` 는
**“지금 어느 계층의 어떤 메서드를 실행 중인지”를 트레이싱하기 위한 이름표**다.

---

## 4. 컨트롤러 결과가 안 바뀌는 이유

이 부분이 가장 헷갈리기 쉬운 지점이다.

```java
Object result = method.invoke(target, args);
return result;
```

여기서 중요한 사실:

* `msg` 는 **로깅용 문자열**
* 비즈니스 로직에는 전혀 관여하지 않는다
* 실제 실행은 항상

  ```
  proxy → invoke() → target.method()
  ```

  이 흐름으로 그대로 진행된다

즉,

* 컨트롤러의 반환값
* 서비스의 계산 결과
* HTTP 응답

이 모든 것은 **원래 구현체와 완전히 동일**하다.

프록시는

* 호출 전: `logTrace.begin(msg)`
* 호출 후: `logTrace.end(...)`

이 두 가지만 끼워 넣을 뿐이다.

---

## 5. “왜 항상 같은 컨트롤러 로그처럼 보일까?”

하나 더 짚자면,

```text
OrderController1.req()
```

이 로그는 **요청당 한 번만** 찍힌다.
그 아래에 들여쓰기 형태로 서비스 / 레포지토리가 붙는 구조다.

그래서 결과적으로 보면

* 컨트롤러 로그는 항상 같아 보이고
* 내부 호출만 깊어지는 것처럼 느껴진다

이건 의도된 구조다.
컨트롤러는 요청의 “입구”이기 때문이다.

---

## 6. 만약 구현 클래스 이름을 찍고 싶다면

현재 코드는 “인터페이스 기준” 로그다.
구현체 기준으로 바꾸고 싶다면 이렇게 할 수도 있다.

```java
String msg = target.getClass().getSimpleName()
        + "." + method.getName() + "()";
```

그러면

```
OrderController1Impl.req()
OrderService1Impl.orderItem()
```

처럼 찍힌다.

다만,
**프록시/AOP 관점에서는 인터페이스 기준 로그가 더 정석**이다.

---

## 한 줄 요약

* `String msg` 는 **실행 결과를 바꾸는 코드가 아니다**
* 오직 “지금 실행 중인 메서드의 논리적 이름”을 만들기 위한 로그용 문자열이다
* 컨트롤러 결과가 변하지 않는 것이 정상이며, 오히려 올바른 상태다
* 이 코드는 “동작 변경”이 아니라 “횡단 관심사(로그) 추가”의 전형적인 예다

이제 이 코드가 왜 AOP 예제로 쓰이는지 감이 올 거다.

짧게 답하면 **지금 코드는 AOP가 아니다.**
다만 **AOP의 개념을 직접 구현한 상태**다.

이걸 정확히 구분해보자.

---

## 1. AOP의 핵심 조건부터 보자

AOP라고 부르려면 보통 다음이 충족된다.

1. 핵심 로직과 부가 로직이 분리되어 있음
2. 부가 로직이 공통 관심사(로깅, 트랜잭션 등)임
3. 적용 대상에 **선언적으로** 적용됨

    * 애노테이션
    * 포인트컷 표현식
4. 프록시 생성과 적용을 **프레임워크가 대신** 해줌

---

## 2. 지금 코드가 하는 일

지금 설정 클래스에서 하고 있는 일은 이거다.

```java
@Bean
public OrderController1 orderController1(LogTrace logTrace) {
    OrderController1Impl impl = new OrderController1Impl(orderService1(logTrace));

    OrderController1 proxy =
        (OrderController1) Proxy.newProxyInstance(
            OrderController1.class.getClassLoader(),
            new Class[]{OrderController1.class},
            new LogTraceInvocationHandler(impl, logTrace)
        );

    return proxy;
}
```

즉,

* 프록시를 **직접 생성**
* 어떤 클래스에 적용할지 **직접 지정**
* 어떤 핸들러를 쓸지 **직접 연결**

이건 **AOP 프레임워크를 쓰지 않고, 프록시 패턴을 수동으로 구현한 것**이다.

---

## 3. 그래서 이건 뭐라고 부르나

정확한 표현은 이거다.

* 프록시 패턴
* 다이나믹 프록시 기반 구조
* AOP의 원리를 코드로 직접 구현한 예제

**하지만 AOP 자체는 아니다.**

---

## 4. 왜 AOP가 아니라고 단정할 수 있나

가장 결정적인 이유는 이거다.

### 1) 적용이 선언적이지 않다

AOP라면 보통 이렇게 말한다.

```java
@Trace
public void orderItem() {}
```

또는

```java
execution(* hello.proxy..*(..))
```

하지만 지금은

```java
new LogTraceInvocationHandler(impl, logTrace)
```

처럼 **코드로 직접 엮고 있다**.

---

### 2) 포인트컷이 없다

* 어떤 메서드에 적용할지
* 어떤 패키지에 적용할지

이런 기준이 **설정으로 분리되어 있지 않다**.

지금은 “이 프록시로 감싼 빈만 전부 적용”이다.

---

### 3) 프록시 생성 주체가 개발자다

AOP에서는

* 스프링이
* 빈 생성 시점에
* 자동으로 프록시를 만들어준다

지금은 전부 개발자가 손으로 한다.

---

## 5. 그런데 왜 AOP 예제에서 이걸 먼저 보여주나

이게 아주 중요하다.

스프링 AOP는 내부적으로

* 프록시 패턴
* 다이나믹 프록시 / CGLIB

이걸 **그대로 사용한다**.

지금 코드는

> “스프링 AOP가 내부에서 몰래 해주는 일을
> 우리가 눈에 보이게 직접 써본 것”

이다.

그래서 이 흐름을 이해하면

* 왜 인터페이스가 필요할 수 있는지
* 왜 프록시가 생기는지
* 왜 `@Transactional` 이 실제 클래스를 바로 호출하지 않는지

이게 전부 연결된다.

---

## 6. 딱 한 줄로 정리하면

* 지금 코드는 **AOP가 아니다**
* 하지만 **AOP의 본질(프록시 + 횡단 관심사 분리)을 정확히 구현한 상태**다
* 이걸 자동화 + 선언적으로 만든 것이 **Spring AOP**다

이 다음 단계에서
“이걸 왜 더 이상 직접 만들면 안 되는지”가 바로 체감될 거다.
