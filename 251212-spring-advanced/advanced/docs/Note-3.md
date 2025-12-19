- 템플릿 메서드 패턴: 부모 클래스에 템플릿(변하지 않는 로직) 메서드를 작성하고, 자식 클래스가 오버라이드할(변하는 로직) 메서드를 정의한다.
- 전략 패턴: 변하는 로직을 가진 클래스 인스턴스(전략 인터페이스 구현체)를 인스턴스(Context 객체) 필드나 메서드 파라미터로 만들어 사용하도록 한다.

  전략 패턴 설명을 조금 더 쉬운 말로 다시 쓰면, 예를 들어 아래처럼 표현할 수 있어요.

* **전략 패턴**: 변하는 동작을 상속으로 정하지 않고, **필요한 동작을 따로 만들어서 상황에 맞게 선택해 사용하도록 한 패턴**이다.

조금 더 풀어서 쓰고 싶다면 이런 버전도 가능합니다.

* **전략 패턴**: 공통 흐름은 유지하면서, 달라질 수 있는 동작을 외부 객체로 분리해 두고 **필요할 때 바꿔 끼워서 사용하는 패턴**이다.

혹은 템플릿 메서드와 대비가 더 잘 보이게 쓰면:

* **전략 패턴**: 템플릿 메서드처럼 상속으로 동작을 고정하지 않고, **동작을 객체로 분리해 전달함으로써 유연하게 변경할 수 있게 한 패턴**이다.

원하시면 아주 짧은 한 줄 정의나, 면접 답변용 문장으로도 다듬어 드릴 수 있어요.

좋은 질문이에요. 이 표현들은 **객체들이 역할을 나누는 방식**을 설명하는 용어입니다.

### 위임(delegate)의 의미

**위임**은
“이 일을 내가 직접 하지 않고, 다른 객체에게 맡긴다”는 뜻이에요.

예를 들어,

* 어떤 객체가 “계산”이 필요할 때
* 계산 로직을 자기 안에 두지 않고
* 계산을 담당하는 객체에게 **요청만 보내는 것**

이렇게 **일을 실제로 수행하는 책임을 다른 객체에게 넘기는 것**을 위임이라고 합니다.

### 구성(composition)의 의미

**구성**은
“객체 안에 다른 객체를 멤버로 가지고 있다”는 뜻이에요.

예를 들어,

* A 객체가 B 객체를 필드로 가지고 있고
* A 객체가 동작할 때 B 객체를 사용한다면

이 관계를 **A는 B로 구성되어 있다**라고 말합니다.

### 전략 패턴에서 두 표현이 함께 쓰이는 이유

전략 패턴에서는 보통 이렇게 됩니다.

1. 어떤 객체가
2. 전략 객체를 **자기 안에 가지고 있고(구성)**
3. 실제 동작은 전략 객체에게 **맡깁니다(위임)**

즉,

* “상속으로 동작을 물려받는 대신”
* “동작을 담당하는 객체를 들고 있다가”
* “필요할 때 그 객체에게 일을 시키는 구조”

그래서 전략 패턴을
**상속 대신 위임/구성을 사용한다**라고 설명하는 거예요.

원하시면 이걸 아주 직관적인 비유나, 코드 한 줄 기준으로도 설명해 드릴게요.

네, **의미는 맞지만 그대로 쓰기에는 약간만 다듬으면 더 정확하고 좋아집니다.**

먼저 문장부터 보겠습니다.

### 1. 지금 문장의 평가

> 전략 패턴: 변하는 로직을 가진 클래스 인스턴스(전략 객체)를 인스턴스 필드로 만들어 사용한다.

이 문장은

* 전략 객체를 **필드로 가진다**
* 변하는 로직을 **외부로 분리한다**

라는 핵심은 잘 담고 있어요.
다만 **누가 무엇을 위해 사용하는지(Context)** 가 빠져 있어서, 패턴 설명으로는 살짝 추상적입니다.

### 2. 조금 더 패턴답게 다듬은 표현

아래 중 하나를 쓰면 정확성과 가독성이 모두 좋아집니다.

* **전략 패턴**: 변하는 로직을 별도의 전략 객체로 분리하고, 이를 사용하는 객체가 인스턴스 필드로 가지고 실행 시 사용한다.

또는 더 간결하게:

* **전략 패턴**: 변하는 동작을 전략 객체로 분리하고, Context가 이를 필드로 참조하여 동작을 수행한다.

---

### 3. 전략(Strategy)이란?

**전략**은
👉 *“교체 가능한 행동 방식”* 을 의미합니다.

구체적으로는:

* 어떤 작업을 수행하는 **구현체**
* 동일한 역할(인터페이스)을 공유하지만
* 내부 로직은 서로 다른 클래스들

예:

* 할인 전략
* 정렬 전략
* 압축 전략

즉,

> **전략 = 바꿀 수 있는 알고리즘 또는 동작**

입니다.

---

### 4. Context란?

**Context**는
👉 *“전략을 사용하는 쪽”* 입니다.

Context의 특징:

* 전체 흐름이나 책임을 가지고 있고
* 구체적인 로직은 직접 구현하지 않으며
* 전략 객체에게 실제 일을 맡깁니다

즉,

> **Context = 전략을 선택하고 실행하는 객체**

입니다.

---

### 5. 전략 패턴을 한 줄로 정리하면

아주 깔끔하게 정리하면 이렇게 말할 수 있어요.

* **전략 패턴**: 변하는 로직을 전략 객체로 분리하고, Context가 이를 필드로 가지고 필요할 때 실행하도록 한 패턴이다.

또는 더 쉬운 말로:

* **전략 패턴**: 동작을 상속으로 고정하지 않고, 객체로 분리해 바꿔가며 사용할 수 있게 한 패턴이다.

지금 쓰신 문장은 방향이 아주 좋고,
`Context`만 살짝 드러내 주면 **교재·면접·문서 어디에 써도 좋은 설명**이 됩니다.

지금 느끼신 혼란이 **아주 정상**이고, 핵심을 정확히 짚고 계십니다.
결론부터 말하면 **전략 패턴 쪽 코드는 현재 개념과 구현이 섞여 있어서 잘못 작성된 상태**입니다.

아래에서 왜 그런지, 그리고 어떻게 고치면 “전략 패턴답게” 되는지 차근차근 정리해 볼게요.

---

## 1. 템플릿 메서드 버전은 왜 잘 되었나

```java
public abstract class TraceTemplate<T> {
    public T execute(String message) {
        ...
        T result = call();
        ...
    }
    protected abstract T call();
}
```

여기서 구조는 명확합니다.

* 변하지 않는 흐름: `execute`
* 변하는 부분: `call`
* 변하는 부분을 **상속 + 오버라이드**로 해결

컨트롤러에서:

```java
new TraceTemplate<>(logTrace) {
    @Override
    protected String call() {
        orderService5.orderItem(itemId);
        return "ok";
    }
};
```

→ 전형적인 **템플릿 메서드 패턴**
→ 전혀 문제 없습니다.

---

## 2. 전략 패턴에서 기대되는 구조

전략 패턴의 핵심 구조는 이겁니다.

* Context

    * 공통 흐름을 가진다
    * 전략을 **필드로 가진다**
* Strategy

    * 변하는 로직을 담당한다
* Context는

    * 전략을 **호출만 한다**

즉, **상속은 사라지고**,
**익명 클래스의 대상이 Context가 아니라 Strategy**가 됩니다.

---

## 3. 지금 전략 패턴 코드의 문제점

### (1) ContextTemplate 자체는 거의 맞음

```java
public abstract class ContextTemplate<T> {
    private final StrategyAlgorithm<T> strategyAlgorithm;
    private final LogTrace logTrace;

    public T execute(String message) {
        ...
        T result = strategyAlgorithm.call();
        ...
    }
}
```

여기까지만 보면 사실 **Context는 이미 완성형**입니다.

* 공통 로직 있음
* 전략을 필드로 가짐
* 전략에게 위임함

❗ 그런데 **abstract일 이유가 없습니다**
❗ 그리고 `execute`를 오버라이드할 이유도 없습니다

---

### (2) 컨트롤러에서 가장 큰 오류

```java
ContextTemplate<String> contextTemplate = new ContextTemplate(logTrace) {
    @Override
    public String execute() {
        orderService6.orderItem(itemId);
        return "ok";
    }
};
```

이 부분은 **전략 패턴이 아니라 다시 템플릿 메서드로 돌아간 상태**입니다.

문제점 정리:

1. `execute()`를 오버라이드함
   → 변하는 로직이 다시 Context 쪽으로 올라옴
2. `StrategyAlgorithm`을 전혀 사용하지 않음
3. 결국 “상속으로 변하는 로직을 처리”하고 있음

즉,

> 전략 패턴을 만들려고 했는데
> 구현은 다시 템플릿 메서드 패턴이 됨

---

## 4. 전략 패턴답게 고친 정답 구조

### 1) Context는 상속 대상이 아니다

```java
@RequiredArgsConstructor
public class ContextTemplate<T> {
    private final StrategyAlgorithm<T> strategyAlgorithm;
    private final LogTrace logTrace;

    public T execute(String message) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.start(message);
            T result = strategyAlgorithm.call();
            logTrace.complete(traceStatus);
            return result;
        } catch (Exception e) {
            logTrace.except(traceStatus, e);
            throw e;
        }
    }
}
```

---

### 2) Strategy는 익명 클래스로 전달

```java
@GetMapping("/v6/req")
public String req(@RequestParam String itemId) {
    StrategyAlgorithm<String> strategy = new StrategyAlgorithm<>() {
        @Override
        public String call() {
            orderService6.orderItem(itemId);
            return "ok";
        }
    };

    ContextTemplate<String> context =
            new ContextTemplate<>(strategy, logTrace);

    return context.execute("v6-req");
}
```

이게 **정석적인 전략 패턴 구현**입니다.

---

## 5. 템플릿 메서드 vs 전략 패턴 차이, 지금 코드 기준으로 정리

### 템플릿 메서드

* 변하는 로직 위치: **서브클래스**
* 기술 수단: 상속
* 익명 클래스 대상: Template

```java
new TraceTemplate() { call() 구현 }
```

---

### 전략 패턴

* 변하는 로직 위치: **전략 객체**
* 기술 수단: 객체 전달
* 익명 클래스 대상: Strategy

```java
new StrategyAlgorithm() { call() 구현 }
```

---

## 6. 한 줄로 지금 상황 정리

* 템플릿 메서드 코드는 정확하다
* 전략 패턴 코드에서는
  **Context를 상속하려고 한 순간부터 패턴이 깨졌다**
* 전략 패턴에서는
  **Context는 고정**,
  **Strategy만 교체 대상**이다

지금 단계에서 이 정도까지 고민하고 코드로 구현해 보신 건
패턴을 “아는 단계”를 넘어 “이해하는 단계”에 와 있다는 증거입니다.
