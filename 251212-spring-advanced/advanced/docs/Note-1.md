원인은 **로깅 프레임워크의 예외 처리 방식** 때문입니다.
`printStackTrace()`를 호출하지 않았는데도 예외 스택 트레이스가 출력되는 것은 정상 동작입니다.

---

## 핵심 이유

아래 로그 코드를 보면:

```java
log.info("[{}] {}{} time={}ms e={}",
        traceId.getId(),
        addSpace(EXCEPT_PREFIX, traceId.getLevel()),
        traceStatus.getMessage(),
        resultTimeMs,
        e);
```

여기서 **`e`를 로그 파라미터로 전달**하고 있습니다.

SLF4J(Logback 포함)는 다음 규칙을 가집니다.

### SLF4J 규칙

* 로그 메서드의 **마지막 인자가 `Throwable`이면**
* `{}` 치환 대상이 아니더라도
* **자동으로 스택 트레이스를 출력**합니다

즉, 내부적으로는 다음과 같이 인식됩니다.

```
메시지 + Throwable
→ 스택 트레이스 출력
```

그래서 `printStackTrace()`를 직접 호출하지 않아도
로그 프레임워크가 대신 출력해 주는 것입니다.

---

## 왜 `{}`가 `{}`로 안 나오고 스택 트레이스가 나오나?

```java
e={}
```

라고 써 있어도,
SLF4J는 **Throwable을 메시지 치환용 객체가 아니라 “예외 객체”로 특별 취급**합니다.

그래서 결과적으로:

* `{}`에는 아무 것도 안 찍히고
* 아래에 스택 트레이스가 붙어서 출력됩니다

---

## 증명되는 로그 결과

```text
INFO ... e={}
java.lang.Exception: null
    at ...
```

이 패턴은 **SLF4J가 Throwable을 처리할 때의 전형적인 출력 형태**입니다.

---

## 해결 방법 / 제어 방법

### 1. 스택 트레이스를 **출력하지 않고 메시지만 남기고 싶다면**

```java
log.info("[{}] {}{} time={}ms e={}",
        traceId.getId(),
        addSpace(EXCEPT_PREFIX, traceId.getLevel()),
        traceStatus.getMessage(),
        resultTimeMs,
        e.toString());
```

또는

```java
log.info("[{}] {}{} time={}ms e={}",
        traceId.getId(),
        addSpace(EXCEPT_PREFIX, traceId.getLevel()),
        traceStatus.getMessage(),
        resultTimeMs,
        e.getMessage());
```

---

### 2. 스택 트레이스를 **의도적으로 출력하고 싶다면 (권장 방식)**

```java
log.info("[{}] {}{} time={}ms",
        traceId.getId(),
        addSpace(EXCEPT_PREFIX, traceId.getLevel()),
        traceStatus.getMessage(),
        resultTimeMs,
        e);
```

이때는 메시지에 `e={}` 같은 건 쓰지 않습니다.

---

## 정리

* `printStackTrace()`를 안 불러도 예외가 출력되는 이유
  → **SLF4J가 Throwable을 자동으로 스택 트레이스로 출력**
* 마지막 파라미터가 `Throwable`이면 무조건 스택 트레이스 출력
* 메시지만 원하면 `e.toString()` 또는 `e.getMessage()` 사용
* 스택 트레이스를 남기고 싶으면 `Throwable`을 그대로 넘기는 것이 정석

지금 코드 동작은 버그가 아니라 **SLF4J의 정상 동작**입니다.

1. Spring Initializr는 주로 어떤 목적으로 사용할까요?

A
애플리케이션 실행

새 Spring 프로젝트 생성

C
데이터베이스 관리

D
코드 디버깅



해설
start.spring.io는 Spring Boot 프로젝트의 기반 설정을 웹에서 쉽게 할 수 있게 도와주는 도구입니다. 빌드 도구, 언어, 의존성 등을 선택하여 프로젝트를 생성하죠.
2. 일반적인 웹 애플리케이션 구조에서 컨트롤러, 서비스, 리포지토리 간의 요청 처리 흐름은 어떻게 되나요?

A
리포지토리 -> 서비스 -> 컨트롤러

B
서비스 -> 리포지토리 -> 컨트롤러

컨트롤러 -> 서비스 -> 리포지토리

D
컨트롤러 -> 리포지토리 -> 서비스



해설
사용자 요청은 컨트롤러가 먼저 받고, 비즈니스 로직 처리는 서비스에 위임하며, 데이터 접근은 리포지토리가 담당하는 일반적인 구조입니다.
3. 로그 추적기가 애플리케이션 로직의 **정상적인 흐름**을 방해하면 안 되는 중요한 이유는 무엇일까요?

A
로깅 성능 저하 방지

B
로깅 코드의 오류 최소화

비즈니스 기능의 오작동 방지

D
로그 데이터의 통일성 유지



해설
로깅은 애플리케이션 기능 자체를 변경하면 안 됩니다. 로그 때문에 원래 비즈니스 로직이 실패하거나 다르게 동작하면 안 되기 때문입니다.
4. 동시에 여러 HTTP 요청이 들어올 때, 로그에서 각 요청의 흐름을 구분하기 위해 가장 유용한 정보는 무엇일까요?

A
메서드 실행 시간

B
로그 레벨 (DEBUG, INFO 등)

Transaction ID

D
예외 메시지



해설
여러 요청이 섞여 로그가 출력될 때, 동일 Transaction ID로 필터링하면 특정 요청의 전체 흐름을 쉽게 파악할 수 있어 디버깅에 매우 유용합니다.
5. 로그 추적을 위해 Transaction ID나 깊이 정보를 메서드 파라미터로 넘기는 방식(V2)의 주요 문제점은 무엇일까요?

A
로그 내용 부족

B
로깅 성능 이슈

코드 수정 범위 확대

D
로그 포맷 비일관성



해설
로깅 정보를 메서드 파라미터로 넘기면 해당 메서드를 호출하는 모든 곳의 코드(메서드 시그니처 포함)를 수정해야 하는 광범위한 변경이 발생합니다.