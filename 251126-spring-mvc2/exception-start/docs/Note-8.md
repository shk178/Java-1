결론적으로 **BasicErrorController를 제대로 사용하려면 지금처럼 직접 ErrorPage를 등록하는 코드를 사용하면 안 된다.**
아주 정확히 말하면:

**WebServerFactoryCustomizer로 ErrorPage를 등록하는 순간, BasicErrorController의 기본 오류 처리 흐름은 우선권을 잃는다.**

왜냐하면 ErrorPage 등록은 **톰캣 레벨 오류 처리(컨테이너 레벨)**이고,
BasicErrorController는 **스프링 MVC 레벨 오류 처리**이기 때문이다.

두 체계는 우선순위가 다르다.

---

# 왜 BasicErrorController가 안 먹히는가?

지금 있는 코드:

```java
ErrorPage errorPageRe = new ErrorPage(RuntimeException.class, "/error-page/Re");
factory.addErrorPages(errorPageRe, errorPage404);
```

이 코드가 의미하는 것은:

* RuntimeException이 발생하면
  **DispatcherServlet으로 가지 말고, Tomcat이 /error-page/Re 로 바로 forward 해라**

즉:

```
예외 발생 → DispatcherServlet → BasicErrorController
```

이 흐름이 **끊어진다.**

대신:

```
예외 발생 → Tomcat → /error-page/Re (컨트롤러)
```

이 흐름만 살아 있다.

따라서 BasicErrorController가 작동하지 않는다.

---

# BasicErrorController를 쓰고 싶다면?

### 1) WSCustomizer의 ErrorPage 등록을 제거해야 한다

즉 다음 코드를 삭제하거나 주석 처리해야 한다:

```java
@Component
public class WSCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        ErrorPage errorPageRe = new ErrorPage(RuntimeException.class, "/error-page/Re");
        ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
        factory.addErrorPages(errorPageRe, errorPage404);
    }
}
```

이걸 없애면 흐름은 아래처럼 된다:

```
예외 발생  
→ DispatcherServlet까지 전달  
→ Spring Boot의 DefaultErrorAttributes와 BasicErrorController 실행  
→ /error 로 매핑  
→ JSON 또는 HTML 자동 반환
```

이게 바로 Spring Boot의 “기본 오류 처리 시스템”.

---

# 2) BasicErrorController의 JSON이 자동으로 제공되는 조건

두 가지 조건이 필요하다:

### 조건 1

템플릿에 error 관련 HTML 파일이 없어야 한다
예:

* templates/error.html
* templates/error/500.html
* templates/error/404.html

이 파일이 하나라도 있으면 HTML이 렌더링되고 JSON이 꺼져버린다.

### 조건 2

클라이언트가 JSON을 원한다는 Accept 헤더를 보내야 한다

```
Accept: application/json
```

그럼 응답이 JSON이 된다.

---

# 3) 만약 BasicErrorController를 쓰면서 커스텀 JSON을 만들고 싶다면?

오류 속성만 재정의하면 된다.

```java
@Component
public class MyErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> map = new HashMap<>();
        Throwable ex = getError(webRequest);

        map.put("status", webRequest.getAttribute("javax.servlet.error.status_code", WebRequest.SCOPE_REQUEST));
        map.put("error", ex.getClass().getSimpleName());
        map.put("message", ex.getMessage());
        map.put("path", webRequest.getAttribute("javax.servlet.error.request_uri", WebRequest.SCOPE_REQUEST));

        return map;
    }
}
```

이러면 /error 가 JSON으로 깔끔하게 응답한다.

---

# 정리

## 질문

**BasicErrorController를 사용하려면 WSCustomizer를 쓰면 안 되느냐?**

## 답

**그렇다. WSCustomizer에서 ErrorPage를 등록하면 BasicErrorController는 동작하지 않는다.**

## 이유

* ErrorPage(컨테이너 오류 처리)가 BasicErrorController(Spring MVC 오류 처리)보다 우선하기 때문.

## 해결

* ErrorPage 등록을 제거
* /error 로 스프링의 기본 오류 처리 흐름을 복원
* BasicErrorController 기반 JSON 방식 사용 가능

---

원하면:

* BasicErrorController와 ErrorPage의 우선순위 구조
* 404/500을 완전히 커스텀하면서도 기본 구조 유지하는 방법
* ErrorController를 직접 구현하는 방식

이런 것도 이어서 설명해줄게.

`HandlerExceptionResolver`의 반환값은 **DispatcherServlet이 이후에 무엇을 할지 결정하는 신호**다.
그런데 `sendError()`를 호출한 뒤 **왜 `new ModelAndView()`를 반환하느냐**가 핵심이다.

---

# 1. `resolveException()` 반환 규칙 요약

| 반환값               | DispatcherServlet의 후속 행동                                                         |
| ----------------- | -------------------------------------------------------------------------------- |
| `null`            | 예외를 해결하지 않은 것으로 간주 → 다음 ExceptionResolver로 전달 → 모두 실패하면 결국 WAS 기본 오류 처리 흐름으로 넘어감 |
| `ModelAndView` 객체 | 예외가 처리된 것으로 간주 → DispatcherServlet은 **더 이상 예외를 처리하려 하지 않음**                      |

---

# 2. 그럼 왜 `new ModelAndView()`를 반환해야 하나?

### 상황:

`response.sendError(400, message);` 를 호출했기 때문에 **이미 HTTP 응답 상태와 에러 코드가 설정됨**.
하지만 sendError가 있다고 해서 DispatcherServlet이
“아, 이 예외는 해결됐구나”
라고 자동으로 이해하지 않는다.

Spring에게 “예외 처리는 내가 끝냈으니 너는 더 이상 관여하지 마” 라는 신호를 주려면:

* **null을 반환하지 않고**
* **ModelAndView 객체를 반환해야 한다**

### 그래서 `new ModelAndView()`를 반환하는 이유는:

## ✔ “예외가 *정상적으로 처리되었다*”라고 Spring에게 알리기 위함

(= 추가 예외 처리 X, 뷰 렌더링 X)

즉,

* 예외는 네가 처리하지 않고
* 응답은 이미 내가 보냈다(sendError)
* 더 이상 건드리지 마라
  라는 의미다.

---

# 3. 왜 비어있는 ModelAndView여야 하나?

`new ModelAndView()` 는 **뷰를 지정하지 않은 빈 ModelAndView**다.

* Spring MVC는 viewName이 비었으므로 렌더링 시도하지 않음
* 그리고 ModelAndView 객체가 존재하므로 “예외는 처리됨”으로 간주함

결국:

### ✔ sendError → WAS 에러 페이지 흐름으로 자동 전환

### ✔ Spring MVC의 뷰 렌더링은 시도되지 않음

### ✔ postHandle은 실행되지 않음

### ✔ afterCompletion은 실행됨 (에러 감지 가능)

---

# 4. return null vs return new ModelAndView() 차이

### return null

* “나는 이 예외를 처리하지 않음”
  → 다른 ExceptionResolver들이 처리
  → 최종적으로 처리되지 않으면 DispatcherServlet → WAS 기본 에러 처리

### return new ModelAndView()

* “예외 처리 끝났음”
  → 다른 Resolver는 호출되지 않음
  → Spring MVC는 렌더링 안 함
  → WAS error page 로직이 이어짐 (sendError 때문에)

---

# 최종 정리

`response.sendError()`는 “응답 상태 설정”일 뿐이고,
`return new ModelAndView()`는 “예외 처리 완료 신호”다.

두 개가 모두 필요하다.

* `sendError()` → HTTP 상태코드 / 메시지 설정
* `new ModelAndView()` → Spring MVC의 추가 처리 방지

---

필요하다면 이 흐름을 `DispatcherType.ERROR`까지 포함해서 더 깊이 도식으로 설명해줄게.

Spring MVC에서 예외가 발생했을 때
`sendError()` → WAS ERROR 디스패처 → ErrorPageController
이렇게 흘러가는 과정을 **전체 흐름으로** 정리해줄게.
(이모티콘 없이 기술적으로만 설명)

---

# 1. 예외 발생 → DispatcherServlet

컨트롤러 또는 핸들러에서 예외가 발생하면
`DispatcherServlet`이 해당 예외를 가로채고 등록된 `HandlerExceptionResolver` 들에게 위임한다.

순서 예시

1. ExceptionHandlerExceptionResolver
2. ResponseStatusExceptionResolver
3. DefaultHandlerExceptionResolver
4. 사용자 정의 MyHandlerExceptionResolver

---

# 2. MyHandlerExceptionResolver 실행

등록된 ExceptionResolver들이 순서대로 실행되다가
내가 만든 Resolver가 예외를 처리할 수 있으면 들어온다.

예:

```java
if (ex instanceof IllegalArgumentException) {
    response.sendError(400, ex.getMessage());
    return new ModelAndView();
}
```

여기서 중요한 작업이 두 가지다.

## (1) `sendError(400, message)`

WAS(Tomcat)에게 아래와 같이 요청하는 동작이다.

* 상태 코드 설정
* 오류 처리(dispatcher type: ERROR)를 준비시키는 플래그 설정

이 시점에 실제 응답이 나가는 것은 아니다.
“이 요청은 오류 흐름으로 전환해야 한다”는 신호를 남기는 것.

## (2) `return new ModelAndView()`

Spring MVC에게 다음을 의미한다.

* “예외는 내가 해결함”
* “DispatcherServlet은 뷰 렌더링을 하지 말 것”
* “다른 ExceptionResolver는 더 호출하지 말 것”

즉, Spring MVC 측의 예외 처리는 종료된다.

---

# 3. DispatcherServlet 후처리

Resolver가 ModelAndView를 반환했으므로
DispatcherServlet은 뷰 렌더링 단계로 넘어가지 않는다.

상황 기록 차원에서 다음 흐름이 이어진다.

* preHandle 실행됨
* controller 호출 → 예외 발생
* exceptionResolver가 처리
* postHandle 스킵
* afterCompletion 실행

postHandle이 생략되는 이유는
컨트롤러가 정상적으로 ModelAndView를 반환한 것이 아니고,
sendError로 흐름을 바꿔버렸기 때문이다.

---

# 4. DispatcherServlet이 요청을 끝냄

DispatcherServlet은 요청 처리를 종료한다.
그런데 응답은 이미 sendError로 인해 “오류로 전환되었다”는 상태만 세팅한 것이므로,
실제 오류 페이지 렌더링은 아직 아니다.

요청은 WAS(Tomcat) 단계로 넘어간다.

---

# 5. WAS(Tomcat)가 ERROR 처리 시작

sendError가 호출되면 WAS는 “오류 처리 모드”로 전환한다.

WAS의 ERROR 처리 절차:

1. 개발자가 등록한 `ErrorPage` 설정 확인

    * 예: `/error-page/400`, `/error-page/500` 등
2. 해당 경로로 내부 forward 실행
   `DispatcherType.ERROR` 로 디스패처

이때 수행되는 핵심: **forward**

WAS → `RequestDispatcher.forward(errorPath)` 실행
이 forward에는 특별한 attribute들이 실려간다.

---

# 6. ERROR 디스패처 흐름

이제 들어오는 요청은 **DispatcherType.ERROR** 로 표시된다.

이때 컨트롤러는 또다시 DispatcherServlet을 통과한다.
즉, 다음과 같은 흐름이 두 번째로 수행된다.

1. preHandle 호출
2. `/error-page/...` 매핑된 컨트롤러 실행 (예: ErrorPageController)
3. postHandle 호출
4. afterCompletion 호출

중요한 점은 이 흐름에서의 afterCompletion은 초기 요청의 afterCompletion과 별개로 한 번 더 실행될 수 있다.

---

# 7. ErrorPageController에서 request attribute 읽기

ErrorPageController에서 다음을 통해 에러 정보를 읽을 수 있다.

```java
request.getAttribute("javax.servlet.error.exception");
request.getAttribute("javax.servlet.error.message");
request.getAttribute("javax.servlet.error.request_uri");
request.getAttribute("javax.servlet.error.status_code");
```

이 값들은 WAS가 forward하면서 설정한 값이다.

이 정보를 기반으로 에러 페이지를 렌더링하거나 JSON을 만들거나 선택한다.

---

# 8. 뷰 렌더링

ErrorPageController가 반환한 뷰 이름 또는 ResponseBody를 기반으로
WAS는 정상적으로 HTML 또는 JSON 응답을 만들어 클라이언트에게 전달한다.

이 시점에서 응답이 최종적으로 commit된다.

---

# 전체 요약 흐름

1. 컨트롤러 예외 발생
2. DispatcherServlet → MyHandlerExceptionResolver
3. Resolver: sendError(400) + return new ModelAndView()
4. Spring MVC 더 이상 예외 처리 X
5. 요청이 WAS로 내려감
6. WAS가 error page 매핑 확인 → 해당 경로로 forward
7. DispatcherType.ERROR로 DispatcherServlet 재호출
8. ErrorPageController에서 에러 정보 attribute 읽음
9. JSON 또는 HTML로 응답 생성
10. 클라이언트로 전달

---

원하면 이 흐름을 **ASCII 시퀀스 다이어그램**으로 정리해줄 수도 있어.
