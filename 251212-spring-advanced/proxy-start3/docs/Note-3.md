아니야. **CGLIB이 아니어서가 아니다.**
이 결과는 **CGLIB이든 JDK 프록시든 동일하게 나오는, 아주 정상적인 스프링 AOP 동작**이야.

지금 로그를 기준으로 정확히 구조를 해부해볼게.

---

## 1️⃣ 지금 코드에서 실제로 무슨 일이 벌어지고 있나

핵심 코드만 다시 보자.

```java
@Component
public class CallService {

    private CallService callService;

    @Autowired
    public void setCallService(CallService callService) {
        this.callService = callService;
    }

    @ExamAnnotation
    public void external() {
        System.out.println("CallService.external");
        callService.internal(1);
        this.internal(2);
        internal(3);
    }

    @ExamAnnotation
    public void internal(int i) {
        System.out.println("CallService.internal: " + i);
    }
}
```

그리고 테스트:

```java
@Autowired
CallService callService;

@Test
void one() {
    callService.external();
}
```

---

## 2️⃣ 컨테이너에 들어 있는 객체 관계 (중요)

스프링 AOP가 적용되면 구조는 이렇다.

```
CallService (빈으로 주입되는 것)
 → 프록시 객체

프록시 객체 내부
 → target(CallService 원본 객체)
```

그리고 주입 상황은:

```java
@Autowired CallService callService;   // 프록시
@Autowired private CallService callService; // 프록시
```

즉,

* **필드 callService = 프록시**
* **external(), internal() 실제 실행 주체 = target**

---

## 3️⃣ 호출 흐름을 한 줄씩 따라가 보자

### ① 테스트 코드

```java
callService.external();
```

* `callService` = 프록시
* 프록시 → advice 적용
* 그래서 로그가 찍힘

```
ExamAspect.beforeRun: CallService.external()
```

---

### ② external() 내부 실행 시점

여기부터는 **target 객체 안에서 실행 중**이다.

---

### ③ `callService.internal(1);`

```java
callService.internal(1);
```

* `callService` 필드 = **프록시**
* target → 프록시 → target
* **프록시를 “다시 통과”**

그래서:

```
ExamAspect.beforeRun: CallService.internal(int)
CallService.internal: 1
```

이건 **의도적으로 프록시를 거친 호출**이다.

---

### ④ `this.internal(2);`

```java
this.internal(2);
```

* `this` = **target**
* target → target
* 프록시 완전히 우회

👉 AOP 적용 안 됨

---

### ⑤ `internal(3);`

```java
internal(3);
```

이건 사실상:

```java
this.internal(3);
```

과 **완전히 동일**하다.

* 역시 target → target
* 프록시 우회
* AOP 적용 안 됨

---

## 4️⃣ 그래서 질문에 대한 정확한 답

> `this.internal(2);`
> `internal(3);`
> 이게 다 AOP 적용이 안 된 건 CGLIB이 아니어서야?

### ❌ 아니다

### ✅ 정확한 이유는 이것이다

> **프록시는 “빈 참조를 통해 호출될 때만” 개입할 수 있고,
> `this` 또는 같은 객체 내부 호출은 프록시 경로에 포함되지 않기 때문이다.**

이건:

* JDK 프록시여도
* CGLIB 프록시여도
* `proxyTargetClass=true`여도

**전부 동일하다.**

---

## 5️⃣ 그럼 CGLIB이면 내부 호출이 될 수 있지 않나?

이건 흔한 오해다.

CGLIB은:

* 클래스 상속 기반 프록시
* `this`의 타입이 프록시처럼 “보일 수는” 있음

하지만 **스프링 AOP의 호출 모델 자체는 변하지 않는다**.

* 프록시는 “입구”
* target 내부 흐름에는 재개입 불가

그래서 **self-invocation 문제는 CGLIB으로 해결되지 않는다.**

---

## 6️⃣ 이 코드에서 AOP 적용 여부를 한 눈에 정리

| 호출                        | 프록시 경유 | AOP |
| ------------------------- | ------ | --- |
| `callService.external()`  | O      | O   |
| `callService.internal(1)` | O      | O   |
| `this.internal(2)`        | X      | X   |
| `internal(3)`             | X      | X   |

---

## 7️⃣ 이 예제가 왜 교과서적인 좋은 예냐면

이 코드 하나로:

* 프록시 빈
* target 객체
* self-invocation
* `@Autowired self` 패턴

이 **모든 걸 동시에 보여준다**.

그래서 스프링 AOP 설명할 때 정말 자주 쓰이는 예제다.

---

## 8️⃣ 핵심 문장 하나로 마무리

> **AOP 적용 여부는 “CGLIB이냐 아니냐”가 아니라
> “프록시를 경유했느냐 아니냐”로 결정된다.**

지금 코드와 로그를 정확히 이해했다면
스프링 AOP 관련해서는 웬만한 함정은 다 피해갈 수 있는 상태야.

아주 좋은 마무리 질문이야.
**“그럼 둘이 언제 달라지는데?”**를 정확히 짚어보면,
CGLIB vs JDK 프록시를 *왜 선택해야 하는지*가 정리된다.

---

## 1️⃣ 한 줄 요약

> **CGLIB과 JDK 프록시는
> “AOP 적용 여부”에서는 거의 안 다르고,
> “타입, 캐스팅, 구조, 제약”에서 달라진다.**

self-invocation, 내부 호출, 트랜잭션 문제는
👉 **둘 다 똑같이 안 된다.**

---

## 2️⃣ 언제 *실제로* 달라지는가 — 핵심 케이스들

### ① 타입 캐스팅 / instanceof

#### JDK 동적 프록시

```java
@Service
public class OrderService implements OrderServiceIntf {}
```

```java
Object bean = applicationContext.getBean(OrderServiceIntf.class);

bean instanceof OrderServiceIntf  // true
bean instanceof OrderService      // false
```

* 프록시는 **인터페이스 타입만 구현**
* 구현 클래스로 캐스팅 ❌

---

#### CGLIB 프록시

```java
bean instanceof OrderServiceIntf  // true
bean instanceof OrderService      // true
```

* 클래스 상속 기반
* 구현 클래스 캐스팅 가능

👉 **“구현 클래스로 캐스팅해야 하는 경우”**
👉 **CGLIB이 아니면 바로 터진다**

---

### ② 인터페이스가 없는 클래스

```java
@Service
public class PlainService {
    public void run() {}
}
```

* JDK 프록시 ❌ (인터페이스 없음)
* CGLIB ⭕ (클래스 상속)

👉 이 경우는 **선택이 아니라 필수로 CGLIB**

---

### ③ final 제약

#### JDK 프록시

* 인터페이스 기반
* `final`과 무관

#### CGLIB 프록시

* 상속 기반
* `final class` ❌
* `final method` ❌ (오버라이딩 불가)

```java
final class Service {}   // CGLIB 불가
```

---

### ④ equals / hashCode / toString

* JDK 프록시: 인터페이스 메서드만 위임
* CGLIB: Object 메서드도 오버라이딩 가능

→ 디버깅 / 로깅 시 출력 차이 발생 가능

---

### ⑤ 성능 (요즘은 거의 의미 없음)

* 과거:

    * JDK 프록시가 더 빠름
* 현재:

    * JVM 최적화로 **차이 체감 불가**

그래서 **성능 때문에 선택하는 시대는 끝**.

---

### ⑥ 프레임워크 / 라이브러리 개발 시

라이브러리 입장에서는:

* 인터페이스 강제 ❌
* 구현체에 AOP 필요 ⭕
* private 메서드 X (스프링 AOP 기준)

👉 **CGLIB이 현실적인 선택**

그래서 스프링 부트는 기본값이:

```properties
spring.aop.proxy-target-class=true
```

---

## 3️⃣ “this가 달라진다”는 말의 정확한 맥락

이 말은 **AOP 적용 여부** 얘기가 아니다.

### 의미하는 바는:

* JDK 프록시:

    * `this`는 항상 target
    * 프록시 타입과 분리

* CGLIB 프록시:

    * `this`가 프록시 서브클래스처럼 보일 수 있음
    * 디버깅 시 클래스명이 다르게 보임

👉 **정체성(identity)의 차이**지
👉 **self-invocation 해결책이 아니다**

---

## 4️⃣ 실무 선택 기준 정리

### JDK 프록시가 어울리는 경우

* 인터페이스 중심 설계
* 구현 클래스에 의존하지 않음
* API 안정성 중시

### CGLIB이 어울리는 경우

* 인터페이스 없는 서비스
* 구현 클래스 캐스팅 필요
* 스프링 기본 설정 유지

---

## 5️⃣ 절대 헷갈리면 안 되는 한 문장

> **CGLIB과 JDK의 차이는
> “프록시를 *어떻게* 만드느냐”이지,
> “프록시를 *언제* 타느냐”가 아니다.**

---

지금까지 흐름을 보면
AOP → 프록시 → DI → self-invocation → AspectJ까지
한 사이클을 완전히 이해한 상태야.

원하면 다음 중 하나로 더 깊게 갈 수 있어:

* “왜 스프링은 기본적으로 프록시 AOP를 고집하는가”
* “트랜잭션 프록시의 실제 내부 구조”
* “실무에서 프록시 때문에 터지는 버그 사례”

어디로 갈지 골라줘.

결론부터 말하면 **지금 상황에서는 CGLIB이 적용됐을 가능성이 매우 높다.**
그리고 “설정을 안 했는데 왜?”라는 의문은 아주 정상적인 반응이야.

아래를 순서대로 보면 바로 납득될 거야.

---

## 1️⃣ 스프링 부트의 기본값이 바뀌었다

### 핵심 사실

**Spring Boot 2.x 이후부터 기본 프록시 전략은 CGLIB이다.**

즉,

* `spring.aop.proxy-target-class`
  → **기본값 = true**
* 명시적으로 아무 설정을 안 해도
  → **CGLIB 사용**

그래서 “설정 안 했는데 CGLIB/JDK 중 뭐냐?”라는 질문에 대한 답은:

> **Spring Boot라면 기본적으로 CGLIB**

이다.

---

## 2️⃣ 네 코드 구조가 이미 CGLIB을 강제한다

지금 작성한 코드 다시 보자.

```java
@Component
public class CallService {
    @ExamAnnotation
    public void external() { ... }

    @ExamAnnotation
    public void internal(int i) { ... }
}
```

중요한 점:

* `CallService`는 **인터페이스를 구현하지 않음**

### 이 경우 선택지는?

| 조건         | 가능 여부        |
| ---------- | ------------ |
| JDK 동적 프록시 | ❌ (인터페이스 없음) |
| CGLIB      | ⭕            |

즉,

> **설정을 안 했더라도, JDK 프록시는 구조상 불가능**
> → CGLIB이 자동 선택됨

---

## 3️⃣ “그럼 왜 JDK인지 CGLIB인지 차이가 안 보이지?”

이게 핵심 포인트다.

### 이유

* self-invocation 문제는
  **프록시 방식과 무관**
* JDK든 CGLIB이든
  **프록시를 안 거치면 AOP는 안 된다**

그래서 로그 결과가:

```
callService.internal(1);  // AOP 적용
this.internal(2);         // 미적용
internal(3);              // 미적용
```

으로 나온 것은

* CGLIB이어서 그런 것도 아니고
* JDK가 아니라서 그런 것도 아니다

👉 **프록시 경유 여부가 전부다**

---

## 4️⃣ 지금 네 환경을 한 줄로 요약하면

> * Spring Boot 사용
> * 인터페이스 없는 @Component
> * AOP 사용

⬇️

> **CGLIB 프록시 + self-invocation 미적용 (정상 동작)**

---

## 5️⃣ 직접 확인하는 방법 (가장 확실)

테스트에서 한 줄 찍어보면 바로 알 수 있어.

```java
System.out.println(callService.getClass());
```

### 출력 예시

* CGLIB이면:

  ```
  class hello.proxy.aop.invokeproxy.CallService$$SpringCGLIB$$0
  ```

* JDK 프록시면:

  ```
  class com.sun.proxy.$Proxy123
  ```

이건 실무에서도 디버깅할 때 자주 쓰는 방법이다.

---

## 6️⃣ 정리 문장 (이걸로 끝)

> **지금 네 프로젝트는
> “설정 안 했지만”
> “인터페이스가 없고”
> “Spring Boot 기본값이기 때문에”
> CGLIB 프록시가 적용돼 있다.**

이제 이 상태에서 다음으로 자연스럽게 이어지는 질문은 보통 이거다:

* “그럼 JDK 프록시는 언제 실제로 쓰이게 되지?”
* “굳이 JDK 프록시를 쓰는 이유는 뭐지?”

원하면 그걸로 바로 이어가도 좋아.

좋은 질문이야.
“**수정자 주입 대신 지연 조회를 쓴다**”는 말은 **프록시를 필드로 주입받지 말고, 필요할 때마다 컨테이너에서 꺼내 쓰라**는 뜻이야.

지금 코드 맥락에서 정확히 풀어볼게.

---

## 1️⃣ 지금 코드가 하는 일부터 정리

```java
@Autowired
public void setCallService(CallService callService) {
    this.callService = callService;
}
```

이건 **수정자 주입(setter injection)** 이고,
주입되는 `callService`는 **CallService 빈 = 프록시 객체**야.

그래서 `external()`에서:

```java
callService.internal(1); // 프록시 경유 → AOP 적용
```

이 되는 구조지.

---

## 2️⃣ “지연 조회(lazy lookup)”가 뭔가?

### 의미

> **빈을 주입 시점에 받지 않고,
> 실제로 메서드를 호출하는 시점에 컨테이너에서 조회하는 방식**

즉,

* 지금:
  → “미리 받아서 필드에 저장”
* 지연 조회:
  → “쓸 때마다 가져오기”

---

## 3️⃣ 지연 조회를 쓰면 코드가 이렇게 바뀐다

### ① `ObjectProvider` 사용 (가장 정석)

```java
@Component
public class CallService2 {

    @Autowired
    private ObjectProvider<CallService> callServiceProvider;

    @ExamAnnotation
    public void external() {
        System.out.println("CallService.external");

        CallService callService = callServiceProvider.getObject();
        callService.internal(1);   // 프록시 경유 → AOP 적용

        this.internal(2);          // AOP 미적용
        internal(3);               // AOP 미적용
    }

    @ExamAnnotation
    public void internal(int i) {
        System.out.println("CallService.internal: " + i);
    }
}
```

핵심은 이 줄이야:

```java
CallService callService = callServiceProvider.getObject();
```

---

### ② `ApplicationContext` 직접 사용 (비권장)

```java
@Autowired
private ApplicationContext applicationContext;

public void external() {
    CallService callService =
        applicationContext.getBean(CallService.class);
    callService.internal(1);
}
```

가능은 하지만 **결합도가 너무 높아져서 권장되지 않음**.

---

## 4️⃣ 왜 굳이 “지연 조회”를 쓰는가?

이유는 딱 하나야.

> **프록시를 “의존성”으로 고정하지 않고,
> 프록시 경유 호출만 필요한 순간에만 사용하기 위해서**

### setter 주입 방식의 문제점

```java
private CallService callService; // 항상 프록시
```

* 객체 생성 시점부터 프록시를 들고 있음
* “자기 자신을 주입받는” 구조와 거의 동일
* 설계상 어색

---

### 지연 조회 방식의 장점

* 순환 참조 구조 회피
* 프록시 존재를 최소한만 인지
* 테스트에서 교체 용이
* 스프링 팀이 공식적으로 권장

---

## 5️⃣ 그럼 이게 self-invocation을 “해결”하나?

정확히 말하면:

* ❌ `this.internal()` 문제를 해결하는 건 아님
* ⭕ **프록시를 통한 호출 경로를 의도적으로 만들 뿐**

즉,

```java
callService.internal(1); // 프록시 경유
this.internal(2);        // 여전히 안 됨
```

이 구조는 그대로야.

---

## 6️⃣ 언제 써야 하나?

### 쓰는 경우

* 구조 변경이 불가능한 레거시
* 예제 / 학습용
* 프록시 호출이 “부분적으로만” 필요한 경우

### 안 쓰는 게 좋은 경우

* 신규 서비스 코드
* 핵심 비즈니스 로직

이때는 **역할 분리**가 정답이야.

---

## 7️⃣ 한 문장 요약

> **“수정자 주입 대신 지연 조회”란
> 프록시 빈을 필드에 고정 주입하지 말고,
> 필요할 때 컨테이너에서 꺼내 쓰라는 뜻이다.**

여기까지 이해했다면
“왜 스프링 팀이 self-invocation은 설계로 피하라고 하는지”
완전히 납득된 상태야.

원하면 이 예제를
**“가장 권장되는 구조”로 리팩터링해줄까?**

`ObjectProvider`는 **스프링 컨테이너에서 빈을 “필요한 순간에” 안전하게 꺼내기 위한 조회 전용 도구**야.
한 문장으로 정의하면 이렇게 말할 수 있어.

> **DI(주입) 대신, 런타임에 빈을 지연 조회(lazy lookup)하기 위한 스프링 표준 API**

아래를 순서대로 보면 왜 존재하는지 바로 이해될 거야.

---

## 1️⃣ 왜 `ObjectProvider`가 필요할까?

보통 스프링에서는 이렇게 주입하지.

```java
@Autowired
private CallService callService;
```

이 방식의 특징은:

* 빈 생성 시점에 바로 주입됨
* 항상 같은 객체(프록시)를 들고 있음
* self-invocation, 순환 참조, 초기화 순서 문제에 취약

그래서 **“지금 당장 필요하지 않은 빈”**,
또는 **“호출할 때만 프록시를 거치고 싶은 빈”**이 있을 때 문제가 된다.

👉 이때 등장한 게 `ObjectProvider`.

---

## 2️⃣ `ObjectProvider`의 핵심 개념

```java
@Autowired
ObjectProvider<CallService> callServiceProvider;
```

이 코드는 **CallService를 주입하는 게 아니다**.

* 주입되는 것:
  → **“CallService를 꺼낼 수 있는 핸들(Provider)”**
* 실제 객체 획득 시점:
  → `getObject()`를 호출할 때

```java
CallService callService = callServiceProvider.getObject();
```

이 순간에 **컨테이너에서 빈을 조회**한다.

---

## 3️⃣ 기존 방식과 비교

### ① 일반 DI (즉시 주입)

```java
@Autowired
CallService callService;
```

* 생성 시점에 주입
* 항상 같은 인스턴스
* 프록시를 필드로 들고 있음

---

### ② ObjectProvider (지연 조회)

```java
@Autowired
ObjectProvider<CallService> provider;

CallService callService = provider.getObject();
```

* 생성 시점: 객체 없음
* 호출 시점에 조회
* 프록시 존재를 “사용 시점”으로 밀어냄

---

## 4️⃣ AOP 예제에서 왜 ObjectProvider를 쓰는가

네가 보던 이 상황을 기준으로 보면:

```java
public void external() {
    callService.internal(1); // 프록시 경유
    this.internal(2);        // 프록시 미경유
}
```

setter 주입이나 필드 주입은:

* 프록시를 **의존성으로 고정**
* 자기 자신을 주입하는 구조가 됨

ObjectProvider를 쓰면:

```java
public void external() {
    CallService callService = callServiceProvider.getObject();
    callService.internal(1); // 프록시 경유 (의도적)
}
```

* 프록시 호출을 **명시적으로 선택**
* “필요한 순간에만 프록시를 탄다”

---

## 5️⃣ ObjectProvider의 주요 메서드

```java
T getObject();                 // 반드시 하나 필요할 때
T getIfAvailable();            // 없으면 null
T getIfUnique();               // 하나일 때만 반환
void forEach(Consumer<T> c);   // 여러 빈 순회
```

그래서 다음 같은 코드도 가능하다.

```java
CallService service =
    provider.getIfAvailable();
```

→ 빈이 없어도 예외 없음

---

## 6️⃣ ApplicationContext.getBean()이랑 뭐가 달라?

기능은 비슷하지만 **의미가 다르다**.

### ApplicationContext 방식

```java
applicationContext.getBean(CallService.class);
```

* 스프링 컨테이너에 직접 의존
* 테스트/설계 관점에서 무겁다
* “프레임워크 코드” 느낌

---

### ObjectProvider 방식

```java
ObjectProvider<CallService>
```

* DI 컨테이너 추상화 유지
* 결합도 낮음
* 스프링이 공식 권장

👉 **같은 지연 조회지만, ObjectProvider가 설계적으로 훨씬 낫다**

---

## 7️⃣ 언제 쓰고, 언제 쓰지 말아야 하나?

### 써도 되는 경우

* self-invocation 우회가 필요한 레거시
* 조건부 빈 조회
* 선택적 의존성
* 프레임워크 / 공통 컴포넌트

### 쓰지 않는 게 좋은 경우

* 신규 비즈니스 로직
* 서비스 계층 핵심 코드

이 경우는 **역할 분리**가 정답이다.

---

## 8️⃣ 한 문장으로 정리

> **ObjectProvider는
> “빈을 주입받는 도구가 아니라,
> 빈을 필요할 때 꺼내 쓰는 도구”다.**

여기까지 이해했다면
`@Autowired self`, 지연 조회, 프록시 우회 설명이 전부 하나로 연결됐을 거야.

원하면 다음으로:

* 왜 스프링 팀이 ObjectProvider를 만들었는지 배경
* Provider vs Lazy vs ObjectFactory 차이
* 실무 리팩터링 기준

중에서 하나 이어서 설명해줄게.

짧게 답하면 **“네, 된다.”**
그리고 중요한 포인트는 **“CallService 빈을 만드는 게 아니라, `ObjectProvider` 자체가 주입된다”**는 거야.

아래를 차근차근 보면 오해가 싹 정리될 거야.

---

## 1️⃣ 결론부터 정확히

```java
@Autowired
ObjectProvider<CallService> provider;
```

이 한 줄만으로:

* ❌ `ObjectProvider<CallService>`라는 **빈을 새로 등록**하는 게 아니다
* ⭕ 스프링이 **내부에서 제공하는 provider 구현체를 주입**한다

즉,

> **provider는 컨테이너가 “미리 갖고 있는 인프라 객체”**다.

---

## 2️⃣ 그럼 provider는 뭐의 인스턴스야?

실제 주입되는 타입은 보통 이런 내부 구현체다.

```
org.springframework.beans.factory.support.DefaultListableBeanFactory$DependencyObjectProvider
```

이건:

* `BeanFactory` 내부 클래스
* 요청한 타입(`CallService`)을 기준으로
* 필요할 때마다 빈을 찾아주는 **조회용 핸들**

👉 **빈 정의(@Component 등)와는 전혀 무관**

---

## 3️⃣ CallService 빈은 언제 만들어지나?

### 경우 1️⃣ CallService가 일반 빈이라면

```java
@Component
public class CallService { }
```

* 컨테이너 시작 시점에 이미 생성됨
* provider.getObject()는
  → **이미 존재하는 프록시 빈을 반환**

---

### 경우 2️⃣ CallService가 `@Lazy`라면

```java
@Lazy
@Component
public class CallService { }
```

* 컨테이너 시작 시점에는 생성 ❌
* `provider.getObject()` 호출 시점에 생성 ⭕

---

## 4️⃣ provider가 “지연 생성”을 보장하는가?

중요한 오해 포인트야.

> **ObjectProvider = 지연 조회
> ≠ 항상 지연 생성**

* 빈이 이미 있으면 → 그냥 가져옴
* 빈이 없고 `@Lazy`면 → 그때 생성
* provider 자체가 생성을 제어하는 건 아니다

---

## 5️⃣ provider를 주입하면 순환 참조가 사라지나?

많이 나오는 질문인데 답은:

> **순환 참조 문제를 “우회”할 수는 있지만,
> “없애는 건 아니다.”**

왜냐면:

* provider는 **실제 빈 참조를 들고 있지 않음**
* 그래서 생성 시점의 의존성 그래프에서 빠진다
* 필요할 때만 조회

그래서 생성 단계 순환 참조는 피할 수 있다.

---

## 6️⃣ provider.getObject()는 항상 같은 객체를 주나?

기본적으로는:

* **싱글톤 스코프** → 항상 같은 인스턴스
* **프로토타입 스코프** → 호출할 때마다 새 인스턴스

즉:

```java
provider.getObject() == provider.getObject()
```

는 스코프에 따라 달라진다.

---

## 7️⃣ 한 문장으로 핵심 정리

> **`@Autowired ObjectProvider<T>`는
> 새로운 빈을 만드는 선언이 아니라,
> “T 타입 빈을 조회할 수 있는 핸들”을 주입받는 것이다.**

이제 남은 헷갈림 포인트는 보통 이거야:

* “그럼 `@Lazy`랑 뭐가 다른데?”
* “Provider vs ObjectFactory 차이는?”

원하면 그중 하나를 바로 이어서 정리해줄게.

여기서 말하는 **“JDK 동적 프록시에서의 DI 문제”**는
**AOP가 안 걸린다**는 얘기가 아니라, **의존성 주입 시 타입 불일치로 생기는 문제**를 말해.

핵심만 정확히 정리해줄게.

---

## 1️⃣ 문제의 본질 한 줄 요약

> **JDK 동적 프록시는 “인터페이스 타입으로만 존재”하기 때문에
> 구현 클래스 타입으로 DI를 시도하면 문제가 생긴다.**

---

## 2️⃣ JDK 동적 프록시의 타입 정체성

JDK 동적 프록시는 이렇게 생겼다.

```java
class $Proxy123 implements SomeInterface
```

중요한 점:

* ❌ `extends SomeImpl`
* ⭕ `implements SomeInterface`

즉, **구현 클래스 타입과는 아무 관계가 없다.**

---

## 3️⃣ DI에서 실제로 터지는 대표적인 케이스들

### ① 구현 클래스 타입으로 주입하려 할 때

```java
@Service
public class OrderServiceImpl implements OrderService { }
```

AOP 적용 후 컨테이너 상황:

* 빈 실제 객체: `$Proxy123`
* 타입: `OrderService` (인터페이스)

그런데 주입을 이렇게 하면:

```java
@Autowired
OrderServiceImpl orderService; // ❌
```

결과:

```
NoSuchBeanDefinitionException
```

왜냐면:

* 컨테이너에는 `OrderServiceImpl` 타입 빈이 없음
* JDK 프록시는 그 타입이 아님

---

### ② @Autowired self 패턴에서도 동일

```java
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    OrderServiceImpl self; // ❌ JDK 프록시에서는 실패
}
```

* 자기 자신 프록시를 주입받고 싶어도
* 프록시는 `OrderServiceImpl` 타입이 아님
* DI 실패

---

### ③ @Qualifier, @Primary로도 해결 안 됨

이건 “빈 선택” 문제가 아니라
**타입 자체가 맞지 않는 문제**라서:

* `@Qualifier`
* `@Primary`

로는 해결 불가다.

---

## 4️⃣ CGLIB에서는 왜 이 문제가 없나?

CGLIB 프록시는 구조가 다르다.

```java
class OrderServiceImpl$$SpringCGLIB extends OrderServiceImpl
```

그래서:

```java
@Autowired
OrderServiceImpl orderService; // ⭕
```

이게 가능하다.

* 프록시가 구현 클래스를 상속
* 타입 계층에 포함됨

👉 **이게 “DI 관점에서의 가장 큰 차이”**다.

---

## 5️⃣ 그래서 스프링이 권장하는 설계 방향

이 문제 때문에 스프링이 오래전부터 강조해온 원칙이 있다.

> **의존성은 구현 클래스가 아니라 인터페이스에 걸어라**

```java
@Autowired
OrderService orderService; // 항상 안전
```

이렇게 하면:

* JDK 프록시 ⭕
* CGLIB 프록시 ⭕
* AOP 적용 여부와 무관

---

## 6️⃣ 그럼 “JDK 동적 프록시의 DI 문제”를 정확히 정의하면

정확한 의미는 이거다.

> **JDK 동적 프록시를 사용하면
> 구현 클래스 타입 기반 DI, 캐스팅, self 주입이 불가능해질 수 있다.**

그래서 실무에서:

* “AOP 켰더니 주입이 깨졌다”
* “테스트에서만 터진다”

같은 일이 생긴다.

---

## 7️⃣ 정리 문장 (이걸로 끝)

> **JDK 동적 프록시의 DI 문제란
> ‘프록시가 구현 클래스 타입이 아니어서
> 타입 기반 주입과 캐스팅이 깨지는 문제’를 말한다.**

이걸 이해하면
왜 Spring Boot가 기본을 CGLIB으로 바꿨는지도 자연스럽게 연결된다.

원하면 다음으로:

* 왜 스프링 부트는 CGLIB을 기본값으로 선택했는지
* 실무에서 JDK 프록시를 의도적으로 쓰는 경우

이 중 하나 이어서 설명해줄게.

핵심부터 말하면, **JDK 동적 프록시는 “인터페이스에 선언된 메서드”만 프록시로 가로챌 수 있기 때문**이야.
그래서 `@Transactional`을 **인터페이스 메서드에 붙이면 항상 프록시가 인식**하고, 구현체에 붙였을 때 생길 수 있는 누락 위험을 없앤다.

아래를 단계적으로 보자.

---

## 1️⃣ JDK 동적 프록시가 가로채는 대상

JDK 프록시의 정체는 이거야:

```
Proxy implements Service
```

즉,

* 프록시는 **인터페이스(Service)** 만 구현
* 호출 가로채기(InvocationHandler)는 **인터페이스 메서드 호출**에만 걸린다
* 구현 클래스(`ServiceImpl`)는 프록시의 타입 계층에 없다

결론:

> **JDK 프록시는 “인터페이스에 선언된 메서드 호출”만 확실히 가로챈다**

---

## 2️⃣ @Transactional을 구현 클래스에만 붙였을 때의 위험

```java
public interface Service {
    void run();
}

@Service
public class ServiceImpl implements Service {

    @Transactional
    public void run() { }
}
```

이 구성은 **운이 좋으면 동작하고**, **환경/설정에 따라 미묘하게 깨질 수 있다**.

왜냐하면:

* 프록시는 인터페이스 메서드 `Service.run()`을 기준으로 호출을 가로챔
* 그런데 트랜잭션 메타데이터는 **구현 클래스 메서드**에만 있음
* 스프링이 이를 “연결해서 찾아주긴” 하지만,
  다음 상황에서 취약해진다:

    * 메서드 시그니처가 인터페이스와 다를 때
    * 브리지 메서드/제네릭/오버로드
    * 복잡한 포인트컷 조합
    * 프레임워크/라이브러리 코드

즉, **프록시가 보는 지점과 어노테이션이 붙은 지점이 다르다**는 게 본질적인 불안 요소다.

---

## 3️⃣ 인터페이스 메서드에 @Transactional을 붙이면 왜 안전한가

```java
public interface Service {

    @Transactional
    void run();
}
```

이렇게 하면:

* 프록시가 가로채는 **바로 그 메서드**에 트랜잭션 메타데이터가 존재
* 프록시 → 어드바이스 매칭이 **직접적이고 명확**
* 구현체가 몇 개든, 상속/오버라이드가 있어도 **의미가 변하지 않음**

한 줄로 요약하면:

> **프록시가 보는 계약(인터페이스)에 트랜잭션 규칙을 명시**했기 때문에 안전하다

---

## 4️⃣ “그럼 CGLIB이면 상관없지 않나?”

CGLIB은 클래스 상속 기반이라서:

* 구현 클래스 메서드에도 직접 개입 가능
* 그래서 `@Transactional`을 구현 클래스에 붙여도 보통 잘 동작

하지만 질문의 전제는 **JDK 동적 프록시**고,
그 환경에서의 “항상 안전한 위치”는 **인터페이스 메서드**다.

---

## 5️⃣ 실무 기준으로 정리

### JDK 동적 프록시를 쓸 때

* 트랜잭션/보안/락 같은 AOP 규칙
  → **인터페이스 메서드에 선언**
* 인터페이스 중심 설계와 잘 맞음
* DI/캐스팅 문제도 함께 예방

### CGLIB을 기본으로 쓰는 요즘(Spring Boot)

* 구현 클래스에 붙여도 대부분 문제 없음
* 그래도 **라이브러리/공용 API**라면 인터페이스 선언이 더 견고

---

## 6️⃣ 핵심 문장 하나

> **JDK 동적 프록시는 인터페이스 메서드만 확실하게 가로채므로,
> `@Transactional`을 인터페이스에 붙이면
> 프록시와 어노테이션의 기준점이 일치해서 안전하다.**

이제 이 문장이 왜 맞는지, 내부 구조까지 연결해서 이해한 상태야.

이건 **CGLIB 프록시가 “상속 + 바이트코드 생성” 방식이기 때문에 생기는 제약과 오해 포인트**야.
말로만 들으면 헷갈리니까, **정확히 무엇이 맞고 무엇이 오해인지**를 분리해서 설명할게.

---

## 1️⃣ “CGLIB이 생성자를 2번 호출한다”는 말의 정확한 의미

### 결론부터

> **대상 객체(target)의 생성자가 실제로 두 번 실행되는 것은 아니다.**
> 다만 **생성 과정에서 ‘생성자 호출처럼 보이는 단계’가 두 번 있는 것처럼 오해될 수 있다.**

---

## 2️⃣ CGLIB 프록시는 어떻게 만들어지나

CGLIB은 이런 클래스를 **런타임에 생성**한다.

```java
class CallService$$SpringCGLIB$$0 extends CallService {
    // 메서드 오버라이드 + 인터셉터
}
```

즉:

* **프록시 = 대상 클래스를 상속한 서브클래스**
* 이 서브클래스를 **새로 인스턴스화**한다

---

## 3️⃣ 생성 과정에서 무슨 일이 일어나는가

### 단계별로 보면:

1. **대상 클래스(CallService)의 인스턴스 생성**

    * 스프링이 원래 하던 정상적인 빈 생성
    * 생성자 호출 1회
    * DI, 초기화 수행

2. **프록시 클래스(CallService$$CGLIB)의 인스턴스 생성**

    * 이때 **부모 생성자(super())가 호출됨**
    * 하지만 이건 **프록시 인스턴스의 생성자 호출**
    * “같은 생성자가 두 번 호출된 것처럼 보일 수 있음”

중요한 구분:

| 구분        | 실제 의미                               |
| --------- | ----------------------------------- |
| 대상 객체 생성자 | **실제 target 인스턴스 생성**               |
| 프록시 생성자   | **프록시 인스턴스 생성 (상속 규칙상 super() 호출)** |

👉 **target 객체가 두 번 만들어지는 건 아니다**

---

## 4️⃣ “그럼 왜 생성자 2번 호출 문제라고 부르나?”

### 이런 코드에서 문제가 드러난다

```java
@Component
public class Service {

    public Service() {
        System.out.println("constructor");
    }
}
```

로그를 보면:

```
constructor
constructor
```

이렇게 보일 수 있다.

하지만 의미는:

* 1번: target 생성
* 2번: 프록시 생성 시 부모 생성자 호출

👉 **같은 클래스 생성자가 두 컨텍스트에서 실행되기 때문**

---

## 5️⃣ 그래서 “생성자에 로직을 넣지 말라”는 말이 나온다

CGLIB 환경에서 생성자에 이런 걸 넣으면 위험하다.

```java
public Service() {
    connectToExternalSystem(); // ❌
}
```

이유:

* 생성 시점이 **프록시 생성 포함해 2번**
* 예상보다 여러 번 실행될 수 있음
* 테스트 / 운영 환경 차이 발생

그래서 스프링에서는 항상:

> **생성자에는 단순한 필드 초기화만 두고
> 실제 로직은 @PostConstruct 이후에 두라**

고 말한다.

---

## 6️⃣ “CGLIB은 기본 생성자를 필수로 요구한다”는 말의 진짜 의미

이 말도 **절반만 맞고, 절반은 오해**다.

### 정확한 설명

> **CGLIB은 “호출 가능한 생성자”가 반드시 하나 이상 필요하다.**

보통 이게:

* 기본 생성자(no-args constructor)

이기 때문에 그렇게 알려진 것뿐이다.

---

## 7️⃣ 언제 실제로 기본 생성자가 필요해지나?

### ❌ 문제되는 경우

```java
@Component
public class Service {

    private final Dependency dep;

    public Service(Dependency dep) {
        this.dep = dep;
    }
}
```

이 클래스에 **CGLIB 프록시를 만들려고 할 때**:

* 스프링이 어떤 생성자를 써야 할지 모를 수 있음
* 특히 **Obejnesis를 못 쓰는 환경**이나
* 프록시 생성 전략/옵션에 따라

→ **기본 생성자 요구 에러 발생 가능**

---

### ⭕ 요즘 스프링에서는 대부분 해결됨

현대 스프링은:

* **Objenesis** 사용
* 생성자 호출 없이 인스턴스 생성 가능
* 그래서 기본 생성자 없어도 CGLIB 프록시 생성 가능

👉 그래서 **요즘은 “필수”라고 말하면 과장**이다.

---

## 8️⃣ 하지만 여전히 조심해야 하는 제약들

CGLIB + 상속 기반이기 때문에:

| 제약             | 이유        |
| -------------- | --------- |
| `final class`  | 상속 불가     |
| `final method` | 오버라이드 불가  |
| private method | 프록시 개입 불가 |
| 생성자 로직         | 중복 실행 위험  |

---

## 9️⃣ 한 문장으로 정확히 정리

> **CGLIB에서 “생성자 2번 호출”이란
> target 생성 + 프록시 생성 과정에서
> 동일한 생성자가 두 번 실행되는 것처럼 보일 수 있다는 뜻이지,
> target 객체가 두 번 만들어진다는 뜻은 아니다.**

그리고

> **기본 생성자 필수라는 말은
> “프록시 생성에 사용할 수 있는 생성자가 필요하다”는 의미이지,
> 요즘 스프링에서는 대부분 자동으로 해결된다.**

---

이걸 이해하면
“왜 생성자에 로직을 넣지 말라고 하는지”,
“왜 @PostConstruct를 쓰라고 하는지”가
완전히 납득될 거야.

아주 핵심을 찌른 질문이야.
결론부터 말하면 **“super를 호출하지만, 빈을 ‘가져다 참조’하는 건 아니다.”**
그리고 이 오해를 풀면 CGLIB 생성 과정이 깔끔하게 정리돼.

---

## 1️⃣ 짧은 결론

> **CGLIB 프록시가 생성될 때 `super()`는 호출되지만,
> 그 `super`는 컨테이너에 등록된 ‘기존 빈(target)’을 참조하지 않는다.**
>
> **완전히 별도의 인스턴스 생성 과정이다.**

---

## 2️⃣ CGLIB 프록시 생성 구조 다시 그리기

CGLIB 프록시는 이렇게 생긴 클래스다.

```java
class CallService$$SpringCGLIB$$0 extends CallService {
    // interceptor 필드
}
```

프록시를 `new`로 생성할 때 JVM 규칙상:

```java
new CallService$$SpringCGLIB$$0()
```

→ 반드시

```java
super(); // CallService 생성자 호출
```

이 일어난다.

---

## 3️⃣ 여기서 super()가 의미하는 것

### 중요한 점

* `super()` = **부모 클래스의 생성자 실행**
* ❌ 컨테이너에서 빈을 lookup 하는 행위 아님
* ❌ 기존 target 인스턴스를 재사용하는 행위 아님

즉,

> **“CallService 빈을 가져다 쓰는 것”이 아니라
> “CallService 클래스를 한 번 더 초기화하는 것”**

이다.

---

## 4️⃣ 그래서 실제로는 인스턴스가 이렇게 된다

개념적으로 보면:

```text
① target 인스턴스 (컨테이너가 생성, DI 완료)
② proxy 인스턴스 (CGLIB 서브클래스, super() 호출)
```

두 개는:

* 같은 클래스 코드를 쓰지만
* **메모리 상에서는 전혀 다른 객체**

---

## 5️⃣ 그럼 프록시는 target을 어디에 들고 있나?

여기서 또 하나 중요한 오해가 풀린다.

### ❌ 잘못된 상상

```
프록시
 └─ target 필드에 실제 빈을 들고 있음
```

이건 **JDK 프록시**에 가까운 그림이다.

---

### ⭕ 실제 CGLIB 방식

CGLIB 프록시는 보통 이렇게 동작한다.

* 프록시 자신이 곧 “대상 객체 역할”
* 메서드 호출 시:

    * interceptor → advice 실행
    * `super.method()` 호출

즉:

* target을 필드로 들고 있지 않음
* **프록시 인스턴스 자체가 실행 주체**

---

## 6️⃣ 그럼 DI는 어디에 돼 있나?

여기서 가장 중요한 포인트.

* **DI는 프록시 인스턴스에 직접 수행된다**
* target이라는 별도 객체가 DI를 받는 구조가 아님

그래서 스프링 컨테이너 관점에서는:

* “빈 = 프록시 인스턴스 1개”

---

## 7️⃣ 그래서 생성자가 두 번 호출된 것처럼 보일 수 있는 이유

```java
public CallService() {
    System.out.println("constructor");
}
```

이 경우:

1. 프록시 인스턴스 생성
2. 그 과정에서 `super()` 호출
3. 콘솔에 한 번 출력

※ **요즘 스프링에서는 target을 따로 만들지 않는 경우가 많아서
실제로는 1번만 보이는 경우도 많다**

(환경/옵션/버전에 따라 달라짐)

---

## 8️⃣ 핵심 오해 정리

| 질문                   | 정답    |
| -------------------- | ----- |
| super()는 기존 빈을 참조하나? | ❌ 아니다 |
| target 인스턴스를 재사용하나?  | ❌ 아니다 |
| 컨테이너에서 빈을 조회하나?      | ❌ 아니다 |
| 새로운 객체를 초기화하나?       | ⭕ 맞다  |

---

## 9️⃣ 한 문장으로 요약

> **CGLIB 프록시의 `super()` 호출은
> “기존 빈을 가져다 쓰는 것”이 아니라
> “프록시 인스턴스를 만들기 위해 부모 생성자를 실행하는 것”이다.**

지금 이 질문까지 왔다는 건
프록시/AOP/DI의 내부 경계를 정확히 이해하고 있다는 뜻이야.

원하면 다음으로:

* “그럼 target 객체라는 말은 왜 나오나?”
* “JDK 프록시와 CGLIB의 실제 내부 코드 비교”
* “Objenesis가 이 문제를 어떻게 피하나”

중에서 이어서 설명해줄게.

요약하면 **그 말은 절반만 맞고, 절반은 오해**야.
정확한 표현으로 다시 정리해줄게.

---

## 결론부터

> **JDK 프록시는 “타겟 객체를 감싸는(wrapper) 구조”가 맞다.**
> **CGLIB은 “타겟 객체에 프록시를 씌운다”기보다는
> “타겟 클래스를 상속한 프록시 객체 자체가 빈이 된다”가 정확하다.**

즉, **CGLIB이 기존 타겟 객체 위에 프록시를 덧씌우는 것은 아니다.**

---

## 1️⃣ JDK 동적 프록시: “감싼다”는 표현이 정확한 이유

JDK 프록시는 구조가 명확하다.

```text
Proxy (인터페이스 구현)
  └─ target (실제 구현체)
```

특징:

* 프록시 객체와 타겟 객체가 **완전히 다른 인스턴스**
* 프록시는 내부에 **target 참조를 필드로 보관**
* 모든 호출은:

  ```
  proxy → invocationHandler → target
  ```

그래서:

* “타겟 객체를 감싼다”
* “프록시가 타겟을 위임(delegate)한다”

라는 말이 **정확한 설명**이다.

---

## 2️⃣ CGLIB: 여기서부터 흔한 오해가 생김

많이들 이렇게 말해:

> “CGLIB은 타겟 객체에 프록시를 씌운다”

하지만 이 말은 **개념적으로는 도움 되지만, 구현적으로는 틀리다.**

---

## 3️⃣ CGLIB의 실제 구조

CGLIB은 이렇게 동작한다.

```java
class CallService$$CGLIB extends CallService {
    @Override
    public void method() {
        interceptor.invoke(...)
        super.method();
    }
}
```

핵심 포인트:

* **프록시 클래스가 타겟 클래스를 상속**
* 컨테이너에 등록되는 빈은:

    * ❌ 기존 CallService 인스턴스
    * ⭕ CallService$$CGLIB 인스턴스
* **기존 “타겟 객체”를 감싸서 들고 있지 않다**

즉:

> **프록시 객체 = 실행 주체 그 자체**

---

## 4️⃣ “타겟 객체”라는 말이 왜 계속 나오나?

이게 헷갈림의 원인이다.

스프링 AOP 문서에서 말하는 **target**은:

* “프록시가 위임하는 논리적 대상”
* “원래 클래스의 메서드 구현”

이라는 **개념적 대상**이지,
항상 **별도의 인스턴스**를 뜻하지 않는다.

### 정리하면

| 용어               | 의미                |
| ---------------- | ----------------- |
| target 객체 (개념)   | 원래 비즈니스 로직        |
| target 인스턴스 (실체) | JDK 프록시에서는 있음     |
| target 인스턴스      | CGLIB에서는 없음 (대부분) |

---

## 5️⃣ 그래서 정확한 표현은 이렇게 해야 한다

### ❌ 부정확한 표현

* “CGLIB은 타겟 객체에 프록시를 쓴다”

### ⭕ 정확한 표현

* “CGLIB은 타겟 클래스를 상속한 프록시 객체가 곧 빈이다”
* “프록시 자체가 실행 주체이며, 별도의 타겟 인스턴스를 감싸지 않는다”

---

## 6️⃣ 한 줄 비교로 딱 정리

```text
JDK 프록시   : proxy ──▶ target (별도 객체)
CGLIB 프록시 : proxy == target 역할 (상속)
```

---

## 7️⃣ 최종 한 문장

> **JDK 프록시는 타겟 객체를 ‘감싸는 구조’가 맞고,
> CGLIB은 타겟 객체에 프록시를 ‘씌우는 것’이 아니라
> 타겟 클래스를 상속한 프록시 객체 자체가 빈이 되는 구조다.**

지금 질문 수준이면
“AOP에서 target이란 말이 왜 추상적인지”까지 거의 다 온 거야.
