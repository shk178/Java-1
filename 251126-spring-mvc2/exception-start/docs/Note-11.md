결론부터 말하면 **Spring MVC는 `ResponseStatusException`을 던지는 순간 그것을 “예외(exception)”로 취급해서 예외 처리 흐름으로 들어가지만, HTTP 상태 코드가 4xx/5xx가 아니면 클라이언트 입장에서 “에러 응답”은 아니다.**
즉 **서버 내부에서는 예외 처리 경로로 흐르지만, HTTP 스펙 상 에러는 아니다**라는 두 흐름이 동시에 성립한다.

## 1. 왜 예외 경로로 들어가는가?

`ResponseStatusException`은 이름 그대로 **예외 클래스**다.
Spring MVC는 컨트롤러에서 예외가 터지면 아래 순서로 움직인다.

1. `processHandlerException` 실행
2. 등록된 `HandlerExceptionResolver` 들에게 위임
3. 해결되면 상태 코드/바디 설정
4. `afterCompletion` 호출

이때 전달된 HTTP 상태 코드가 **200이든 208이든 400이든 500이든 무조건 “예외 흐름”**으로 처리된다.
왜냐하면 Spring 내부 입장에서 중요한 건 “예외가 발생했는가?”이지 “상태 코드가 에러 코드인가?”가 아니기 때문이다.

## 2. 208이기 때문에 문제가 되는가?

전혀 아니다.

HTTP 상태 코드 208(Already Reported)은 WebDAV 확장 코드로, 의미는 에러가 아니고 상태를 알려주는 응답이다.
HTTP 규격에서 **4xx/5xx만 에러로 분류**되므로 208은 정상 응답의 한 종류다.

그래서 결과적으로:

* **서버 내부(Spring)**: *예외 기반 흐름*
* **HTTP 응답 관점**: *정상 응답 (2xx)*

두 개가 구분된다.

## 3. 그럼 “에러처럼” 보이는 이유는?

개발자 도구나 로깅에서 “Exception handled” 같은 메시지를 볼 수 있기 때문이다.
이건 어디까지나 **ResponseStatusException이 예외를 사용해 상태 코드를 반환하는 구조 때문**이다.
로그/Stacktrace가 찍히지 않거나, 에러 페이지로 리다이렉트되지 않는다면 이는 에러가 아니라 단순 상태 코드 응답이다.

## 4. 실제로 클라이언트는 어떻게 받아들이나?

브라우저나 HTTP 클라이언트는 단순히:

* Status Code: 208
* Body: “message 내용-2”

를 가진 정상 응답으로 처리한다.

브라우저의 "Status Code" 컬러가 **빨간색이 아니라면** 에러로 간주되지 않는다.
Postman에서도 **2xx는 성공으로 분류**된다.

## 5. 정리

**ResponseStatusException을 쓰는 순간 Spring MVC 내부에서는 예외 처리 루틴으로 처리되지만, 상태 코드가 2xx라면 HTTP 응답으로서 에러는 아니다.**

즉:

* 208 상태 코드는 에러가 아니다.
* 하지만 Spring 입장에서 예외로 던졌기 때문에 예외 처리 단계로 이동한다.
* 클라이언트는 정상 응답으로 이해한다.

원하면 다음도 설명해줄게:

* 왜 Spring은 상태 코드만 설정하는 기능이 아니라 아예 예외를 사용하는지
* ResponseStatusException 구조로 인해 afterCompletion이 어떻게 영향을 받는지
* 로그 출력 제어하는 방법

`ResponseStatusException`의 세 번째 파라미터에 `new IllegalArgumentException()` 같은 **cause 예외**를 넣는 이유는 단 하나다.

## 1. “원인 예외(cause)”를 명시하기 위해 감싸는 것

`ResponseStatusException`은 내부적으로 **다른 예외를 감싸서** 던질 수 있게 설계되어 있다.

```java
throw new ResponseStatusException(
        HttpStatus.ALREADY_REPORTED,
        "message 내용-2",
        new IllegalArgumentException()
);
```

여기서 `new IllegalArgumentException()`은
“이 ResponseStatusException이 어떤 이유 때문에 발생했는가?”
를 **예외 체인(exception cause chain)**으로 남기기 위한 것이다.

즉, 아래 기능을 위한 것:

* 서버 로그에서 stacktrace를 보면
  `ResponseStatusException → IllegalArgumentException` 순으로 이어짐
* 모니터링/로깅 시스템(예: Sentry, ELK)이 “원인 예외”를 함께 기록함

## 2. 실제 HTTP 응답에는 아무 영향이 없다

중요한 점은:

* HTTP 상태 코드 208
* 메시지 “message 내용-2”

이 두 가지는 **HTTP 응답에 포함되지만**,
`IllegalArgumentException` 내용은 **클라이언트에 노출되지 않는다**.

서버 내부 로그나 디버깅에만 의미가 있음.

## 3. 대부분의 경우 굳이 넣을 필요 없다

많은 경우 굳이 cause를 지정할 필요 없기 때문에 아래처럼 단순하게 써도 된다.

```java
throw new ResponseStatusException(HttpStatus.ALREADY_REPORTED, "message 내용-2");
```

cause를 반드시 넣어야 하는 상황은 다음뿐:

* 원래 발생한 예외가 있고, 그 예외를 그대로 로그에 남기고 싶을 때
* 예외 체인을 유지해서 디버깅 정보를 더 풍부하게 하고 싶을 때
* 외부 시스템에 전파되는 로깅을 위해 예외 원인을 명확히 드러내고 싶을 때

## 4. 왜 IllegalArgumentException일까?

여기서 `IllegalArgumentException`은 단지 예시거나,

* 비즈니스 로직에서 실제로 발생했을 가능성이 있는 cause 예외를 그대로 전달하거나
* 또는 의미 부여 없이 그냥 형식적으로 넘긴 것일 수도 있다

즉 이 예외는 특정한 의미나 강제성이 있는 것이 아니다.

## 5. 정리

* 세 번째 파라미터는 **원인 예외(cause)**
* 목적은 **로그와 추적용**
* 클라이언트에는 영향을 주지 않음
* 필요 없으면 생략 가능

원하면 지금 상황에서 어떤 예외를 cause로 넘기는 것이 적절한지도 더 설명해줄게.

아주 핵심을 잘 요약해두셨어요. 여기에 조금 더 구조적으로, 전체 흐름을 이해할 수 있게 정리해드릴게요.

---

# DefaultHandlerExceptionResolver가 하는 일

`DefaultHandlerExceptionResolver`는 **스프링 MVC 내부에서 발생하는 표준적인 스프링 예외들을 적절한 HTTP 상태 코드로 변환하는 역할**을 합니다.
즉, “스프링 내부 로직에서 발생하는 예외 → HTTP 응답 코드” 이 매핑 규칙을 정해둔 내장 Resolver입니다.

---

# 왜 필요한가?

스프링 MVC는 다양한 작업을 처리하는데, 대표적으로:

* 요청 파라미터 바인딩
* 타입 변환
* 메시지 컨버팅
* HTTP 메서드 처리
* 요청 헤더 처리
* PathVariable 매핑
* 바디 JSON 파싱

이런 과정 중에 스프링 내부에서 여러 예외가 발생할 수 있습니다.

그런데 이 예외들은 **개발자가 명시적으로 던진 예외가 아니라 스프링 프레임워크 내부에서 던진 예외**이기 때문에,
기본적으로 처리되지 않으면 **500(Internal Server Error)** 로 이어집니다.

하지만 이런 오류 중 상당수는 사실 서버 잘못이 아니라 **클라이언트 요청 오류**입니다.
따라서 HTTP 표준에 따라 400 계열로 매핑해줘야 맞습니다.

이 역할을 자동으로 해주는 것이 `DefaultHandlerExceptionResolver`.

---

# 대표적인 예: TypeMismatchException → 400 Bad Request

예를 들어:

```
GET /api?age=abc
```

컨트롤러:

```java
@GetMapping("/api")
public String test(@RequestParam Integer age) { ... }
```

여기서 `age=abc`는 Integer로 변환할 수 없기 때문에
스프링 내부에서 `TypeMismatchException`이 발생합니다.

이걸 그대로 두면:

* 예외 발생 → HandlerExceptionResolver가 처리하지 못함 → 톰캣까지 전달됨 → 500 오류 발생

하지만 이 예외는 명백히 **클라이언트 잘못(잘못된 요청)** 입니다.

그래서 DefaultHandlerExceptionResolver는 이 예외를 감지하면 **400(Bad Request)** 로 상태 코드를 바꿔 응답합니다.

---

# 어떤 예외들을 처리하는가?

스프링 MVC에서 자주 발생하는 예외들을 상태 코드와 함께 처리합니다. 예를 들면:

| 예외                                        | 의미                    | 변환되는 상태 코드 |
| ----------------------------------------- | --------------------- | ---------- |
| `TypeMismatchException`                   | 타입 변환 실패              | 400        |
| `MissingServletRequestParameterException` | 필수 파라미터 없음            | 400        |
| `HttpRequestMethodNotSupportedException`  | 지원하지 않는 HTTP 메서드      | 405        |
| `HttpMediaTypeNotSupportedException`      | 지원하지 않는 Content-Type  | 415        |
| `HttpMediaTypeNotAcceptableException`     | Accept 헤더와 매칭되는 타입 없음 | 406        |
| `NoHandlerFoundException`                 | 요청한 URL 매핑 없음         | 404        |
| `HttpMessageNotReadableException`         | 요청 JSON 바디 파싱 실패      | 400        |

이 외에도 스프링이 정의한 많은 예외를 매핑합니다.

---

# 어떻게 동작하는가?

스프링의 예외 처리 체인은 다음 순서입니다:

1. 컨트롤러에서 예외 발생
2. `HandlerExceptionResolver` 목록에서 순서대로 처리 시도
3. `ExceptionHandlerExceptionResolver`

    * @ExceptionHandler가 있으면 처리
4. `ResponseStatusExceptionResolver`

    * @ResponseStatus 또는 ResponseStatusException 처리
5. **`DefaultHandlerExceptionResolver`**

    * 스프링 내부 예외를 HTTP 상태 코드로 변환
6. 처리되지 않으면 톰캣까지 올라가서 500 발생

즉, 마지막 방어막 역할을 하는 Resolver입니다.

---

# 핵심 요약

* 스프링 내부에서 발생하는 예외를 적절한 HTTP 응답 코드로 자동 맵핑하는 역할
* 특히 클라이언트가 잘못 보낸 요청을 500이 아닌 400 계열로 바꿔줌
* 개발자가 직접 신경 쓰지 않아도 상태 코드를 표준적으로 유지할 수 있게 해줌
* 스프링 MVC의 기본 예외 처리 체인 중 "기본/최종 처리기"

---

필요하시면 `DefaultHandlerExceptionResolver`가 처리하는 상세 예외 리스트도 정리해서 드릴까요?

스프링 내부 예외인지 판단하는 방법은 크게 **세 가지 관점**으로 나눠서 보면 명확해집니다.

---

# 1. 예외의 **패키지**를 보면 된다

스프링 내부에서 사용하는 예외는 대부분 다음과 같은 패키지에 포함됩니다.

* `org.springframework.web.*`
* `org.springframework.http.*`
* `org.springframework.validation.*`
* `org.springframework.beans.*`
* `org.springframework.core.*`

예를 들어:

* `TypeMismatchException` → `org.springframework.beans.TypeMismatchException`
* `MissingServletRequestParameterException` → `org.springframework.web.bind.MissingServletRequestParameterException`
* `HttpRequestMethodNotSupportedException` → `org.springframework.web.HttpRequestMethodNotSupportedException`

이런 예외들은 순수 자바 예외도 아니고, 개발자가 던지는 사용자 정의 예외도 아니며, **스프링 프레임워크가 요청 처리 흐름 중에 던지는 예외들**입니다.

따라서 패키지를 보면 "아, 이건 스프링 내부에서 던지는 예외"라고 알 수 있습니다.

---

# 2. 발생 시점으로 판단할 수 있다

(컨트롤러 실행 전에 터지면 대부분 스프링 내부 예외)

스프링 MVC 흐름은 다음과 같습니다.

1. DispatcherServlet
2. HandlerMapping
3. HandlerAdapter
4. Argument Resolver / Type Converter
5. Message Converter
6. 컨트롤러 실행
7. 뷰 렌더링

여기서 **컨트롤러 실행까지 도달하기 전에 발생하는 모든 예외는 스프링 내부 예외입니다.**

예시:

* 요청 파라미터 바인딩 실패
* PathVariable을 타입 변환할 수 없음
* RequestBody JSON 파싱 실패
* 지원하지 않는 HTTP Method
* 지원하지 않는 Content-Type
* 필수 파라미터 없음

이런 것들은 컨트롤러 코드가 시작되기 전에 스프링이 처리하는 과정에서 터지는 것이라,
**개발자가 직접 던진 게 아니라 스프링이 자체적으로 발생시키는 "스프링 내부 예외"입니다.**

---

# 3. HandlerExceptionResolver 동작 방식이 힌트를 준다

DefaultHandlerExceptionResolver는 **스프링 내부에서 던지는 특정한 예외들만** 처리합니다.

즉, DefaultHandlerExceptionResolver가 처리했다면 → 내부 예외였습니다.

대표 예외 목록 일부:

* MissingServletRequestParameterException
* TypeMismatchException
* HttpRequestMethodNotSupportedException
* HttpMediaTypeNotSupportedException
* HttpMessageNotReadableException
* NoHandlerFoundException
* MethodArgumentNotValidException

이 목록은 전부 스프링이 요청을 핸들링하는 과정에서 자체적으로 던지는 예외입니다.

---

# 한 줄 정리

**다음 중 하나라도 해당하면 스프링 내부 예외입니다:**

1. 예외 패키지가 `org.springframework.*`
2. 컨트롤러 실행 이전 단계에서 발생
3. DefaultHandlerExceptionResolver가 처리하는 예외 목록에 포함됨

---

필요하시면
“이 예외가 스프링 내부 예외인지 직접 판단할 수 있는 기준표”
같이 만들어드릴까요?
