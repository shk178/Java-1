지금 상황을 단계별로 정리해주면, **Spring의 예외 → 톰캣 오류 처리 → ErrorPage 매핑 → 뷰 렌더링** 전체 흐름을 정확히 이해할 수 있어.
왜 콘솔에는 *"멤버 런타임 에러"*가 찍히는데 **결과 화면(response)은 Re.html**이 나오는지 이유도 이 과정에서 나온다.

---

# 전체 흐름 정리

---

# 1. `getMember()`에서 예외 발생

```java
if (id.equals("re")) {
    throw new RuntimeException("멤버 런타임 에러");
}
```

요청

```
GET /members/re
```

→ 여기서 **RuntimeException**이 발생.
→ DispatcherServlet까지 예외가 전달됨.

---

# 2. DispatcherServlet은 예외를 처리하지 않음 → 톰캣 컨테이너로 전달

DispatcherServlet 단계에서 핸들러가 없거나 예외가 처리되지 않으면:

1. 컨트롤러 메서드에서 던진 예외 발생
2. HandlerAdapter → DispatcherServlet까지 올라감
3. DispatcherServlet에서 해결할 수 없으면
4. 예외는 **WAS(톰캣)**으로 넘겨짐

---

# 3. 톰캣의 오류 처리(오류 페이지 매핑 적용)

Spring Boot에서는 `ErrorPageCustomizer`를 통해 톰캣에 오류 페이지를 등록한다:

```java
ErrorPage errorPageRe = new ErrorPage(RuntimeException.class, "/error-page/Re");
```

즉:

* RuntimeException 발생 시
  → 톰캣이 `/error-page/Re`로 **forward** 한다

forward이기 때문에 새로운 요청처럼 보이지만
**톰캣은 오류 관련 정보를 RequestDispatcher의 표준 속성으로 담아 보낸다.**

---

# 4. `/error-page/Re` 컨트롤러 실행됨

```java
@RequestMapping("/error-page/Re")
public String secondRe(HttpServletRequest request) {
    Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    return "Re";
}
```

여기서 출력되는 값이

* exception = java.lang.RuntimeException: 멤버 런타임 에러
* message = 멤버 런타임 에러
* uri = /members/re
* status = 500

이렇게 나온다.

---

# 5. 컨트롤러는 뷰 이름 `"Re"`를 반환

`return "Re";`

이게 핵심이다.

* 이 컨트롤러는 에러 정보를 출력만 하고
* 최종적으로 **Re.html**이라는 뷰를 렌더링하라고 DispatcherServlet에 지시한다.

Spring MVC의 뷰 리졸버가 `templates/Re.html` 등을 찾아서 렌더링한다.

---

# 그래서 “멤버 런타임 에러”가 나왔는데도 화면은 Re.html이 나오는 이유

원인 한 줄 요약

**예외 발생 → 톰캣이 /error-page/Re 로 forward → 에러 페이지 컨트롤러 실행 → 이 컨트롤러가 Re.html 뷰를 리턴하기 때문**

즉 flow는 이렇게 된다:

1. `/members/re`
2. RuntimeException 발생
3. 톰캣이 `/error-page/Re` 로 이동
4. `secondRe()` 실행
5. `return "Re"`
   → 최종 화면은 Re.html

예외 메시지는 로그에만 출력되고
사용자 브라우저에는 **에러 페이지 뷰(Re.html)**만 보이게 되는 것이다.

---

# 당신이 궁금해 할 만한 부분 정리

### Q1. 왜 response에 에러 메시지가 직접 안 찍히지?

DispatcherServlet이나 Tomcat이 예외 정보를 브라우저에 직접 출력하지 않고
**errorPage 매핑된 컨트롤러가 반환한 뷰만** 사용자에게 보여주기 때문이다.

Spring Boot는 기본적으로 error page를 HTML 뷰로 처리하도록 디자인되어 있다.

---

### Q2. 원래 톰캣 기본 에러 페이지(Whitelabel Error Page) 대신 내가 만든 Re.html이 뜨는 이유?

당신이 errorPage를 등록했기 때문이다:

```java
ErrorPage(RuntimeException.class, "/error-page/Re")
```

이게 우선권을 가진다.

---

### Q3. 에러 페이지 컨트롤러에서도 ModelAndView를 쓰는 게 아니라 return "Re"만 하면 왜 동작하는가?

Spring MVC는

* 문자열 = 뷰 이름으로 처리
* ViewResolver가 HTML 파일을 찾아 렌더링
  하기 때문에, ModelAndView가 없어도 된다.

---

# 요약

* RuntimeException 발생
* errorPage 매핑 때문에 `/error-page/Re` 로 forward
* 그 컨트롤러가 최종적으로 `Re.html`을 보여주기 때문에
* 사용자 화면에는 Re.html이 뜨는 것

즉, **에러 발생 자체는 정상적으로 잡혔고, 에러 페이지 컨트롤러가 반환한 뷰가 정상적으로 렌더링된 것이다.**

---

더 원하시면 다음도 설명해줄 수 있어:

* forward 과정에서 Spring이 관여하는 부분
* DispatcherType.ERROR 흐름
* 일반 controller 요청과 error page 요청의 차이
* ErrorMvcAutoConfiguration 구성 분석
* Re.html 대신 JSON을 반환하는 방식

원하는 부분 알려주면 더 깊게 설명해줄게.

Spring Boot에서 **error-page 매핑을 유지하면서도 JSON으로 반환**하려면, 핵심은 **뷰 이름을 리턴하지 않고, @ResponseBody 또는 @RestController 를 사용**하는 것이다.

현재 코드는 이렇게 되어 있다:

```java
@RequestMapping("/error-page/Re")
public String secondRe(HttpServletRequest request) {
    return "Re"; // 뷰 이름 → HTML 렌더링
}
```

여기서 `"Re"` 때문에 템플릿 뷰(예: Re.html)가 렌더링된다.
**JSON으로 반환하려면 이 부분을 JSON Response로 바꾸면 된다.**

---

# 1. 가장 간단한 방식: @ResponseBody 사용

```java
@RequestMapping("/error-page/Re")
@ResponseBody
public Map<String, Object> secondRe(HttpServletRequest request) {

    Map<String, Object> result = new HashMap<>();

    result.put("exception", request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
    result.put("message", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
    result.put("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
    result.put("status", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));

    return result;   // JSON 변환
}
```

이렇게 하면 JSON으로 자동 변환되어 다음과 같이 응답된다:

```json
{
  "exception": "java.lang.RuntimeException: 멤버 런타임 에러",
  "message": "멤버 런타임 에러",
  "path": "/members/re",
  "status": 500
}
```

---

# 2. @RestController 사용

컨트롤러 클래스 전체가 JSON만 반환한다면 다음처럼 변경해도 된다:

```java
@RestController
public class ErrorPageController {

    @RequestMapping("/error-page/Re")
    public Map<String, Object> secondRe(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        result.put("exception", request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
        result.put("message", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
        result.put("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        result.put("status", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        return result;
    }
}
```

`@RestController` 안에서는 뷰를 찾지 않고 항상 JSON으로 응답한다.

---

# 3. 반환 타입을 DTO로 바꾸기

JSON 구조를 명확하게 만들고 싶다면 DTO를 사용:

```java
public class ErrorResult {
    private int status;
    private String message;
    private String exception;
    private String path;

    public ErrorResult(int status, String message, String exception, String path) {
        this.status = status;
        this.message = message;
        this.exception = exception;
        this.path = path;
    }
}
```

컨트롤러:

```java
@RequestMapping("/error-page/Re")
@ResponseBody
public ErrorResult secondRe(HttpServletRequest request) {

    int status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

    return new ErrorResult(status, message, ex.toString(), path);
}
```

---

# 4. 주의: 템플릿 파일(Re.html)이 있어도 무시됨

@ResponseBody 또는 @RestController를 사용하면:

* 뷰 리졸버가 작동하지 않음
* 템플릿 파일은 로딩되지 않음
* 대신 JSON으로 직렬화됨

---

# 5. JSON 기반 오류 페이지를 완성하는 공식적인 방식

Spring Boot의 DefaultErrorAttributes를 참고해
custom error controller를 구성할 수도 있다. 예:

```java
@Component
public class JsonErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(
            WebRequest webRequest,
            ErrorAttributeOptions options) {

        Map<String, Object> attributes = new HashMap<>();
        Throwable error = getError(webRequest);

        attributes.put("message", error.getMessage());
        attributes.put("type", error.getClass().getName());
        attributes.put("status", webRequest.getAttribute("javax.servlet.error.status_code", 0));
        attributes.put("path", webRequest.getAttribute("javax.servlet.error.request_uri", 0));

        return attributes;
    }
}
```

이 방식은 `/error`를 JSON으로 통일해주는 정석적인 접근이다.

---

# 결론

현재 코드에서 JSON으로 응답하려면 아래 한 줄만 추가해도 된다:

```java
@ResponseBody
```

또는 컨트롤러를 `@RestController`로 변경.
이렇게 하면 오류 페이지 매핑이 그대로 작동하면서도 HTML 대신 JSON으로 응답된다.

원하면 “상태 코드별 JSON 응답 통일하기”, “필터/인터셉터에서 JSON 오류 처리하기” 같은 더 진보된 형태도 설명해줄게.

바꿀 수는 있지만, **이 부분만 바꿔서는 JSON 응답을 직접 만들어주지 않는다.**
왜냐하면 `WSCustomizer`는 **톰캣(서버)에 “에러가 발생하면 어느 URL로 보내라”만 알려주는 역할**이기 때문이다.

즉 이 설정 파일의 책임은 단 하나다:

* *“RuntimeException → /error-page/Re 로 forward 해라”*
* *“404 → /error-page/404 로 forward 해라”*

forward가 일어난 뒤 실제 JSON/HTML을 결정하는 것은
**그 URL을 처리하는 컨트롤러가 무엇을 리턴하느냐**다.

그래서 “이 부분만 바꿔서 JSON을 만들 수 있는가?”라고 묻는다면
**직접 JSON을 만들어주지는 못한다**가 정확한 답이다.

하지만 **간접적으로 JSON을 사용할 수 있도록 바꾸는 방법은 있다.**

아래 3가지가 가능하다.

---

# 방법 1: ErrorPage에서 HTML이 아닌 JSON용 엔드포인트로 매핑

지금은 이렇게 되어 있다:

```java
ErrorPage errorPageRe = new ErrorPage(RuntimeException.class, "/error-page/Re");
```

이 대신 JSON 처리용 엔드포인트를 따로 지정할 수 있다:

```java
ErrorPage errorPageRe = new ErrorPage(RuntimeException.class, "/error-json/re");
```

그리고 컨트롤러에서:

```java
@RestController
public class ErrorJsonController {

    @RequestMapping("/error-json/re")
    public Map<String, Object> errorRe(HttpServletRequest request) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
        error.put("message", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
        error.put("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
        return error;
    }
}
```

**장점**

* ErrorPage 설정은 그대로 유지
* JSON 컨트롤러만 따로 분리
* 가장 정석적이고 직관적이다

**단점**

* 주소가 바뀐다(HTML 전용, JSON 전용 각각 따로 필요)

---

# 방법 2: 기존 `/error-page/Re`가 JSON을 반환하도록 강제

WSCustomizer는 그대로 두고,
컨트롤러만 아래처럼 바꾸는 방법이다:

```java
@RequestMapping("/error-page/Re")
@ResponseBody
public Map<String, Object> secondRe(HttpServletRequest request) {
    Map<String, Object> map = new HashMap<>();
    map.put("status", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
    map.put("message", request.getAttribute(RequestDispatcher.ERROR_MESSAGE));
    return map;
}
```

그러면 기존 HTML 뷰는 없어지고 JSON만 나온다.

---

# 방법 3: 아예 Spring Boot의 기본 /error(JSON)를 사용하게 만드는 방식

Spring Boot는 원래 `/error`에서 JSON을 반환할 수도 있다.
(템플릿 error.html 같은 파일이 없을 때 자동 JSON 반환)

따라서 다음처럼 ErrorPage를 변경할 수도 있다:

```java
ErrorPage errorPageRe = new ErrorPage(RuntimeException.class, "/error");
ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error");
```

그리고 템플릿 폴더에 `error/500.html`, `error/404.html` 을 만들지 않으면
Spring Boot는 자동으로 JSON을 반환한다.

JSON 예:

```json
{
  "timestamp": "...",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/members/re"
}
```

**장점**

* 가장 단순
* Spring Boot 자동 JSON 처리 사용 가능

**단점**

* 커스텀 에러 구조를 만들기 어렵다
* 모든 예외가 `/error`로 모이므로 통제하기 애매할 수 있음

---

# 최종 결론

WSCustomizer는 **“예외 → 어떤 URL로 보낼지”만 결정할 수 있다.**
JSON을 만들지는 못한다.

하지만:

* JSON을 반환하는 URL로 변경하거나
* Spring Boot 기본 /error 를 사용하거나

**이런 식으로 우회하여 JSON 기반 오류 처리 체계를 만들 수 있다.**

원한다면 “현재 구조 유지하면서 HTML/JSON을 content-type에 따라 자동 전환하는 방식”도 만들어줄 수 있다.

지금 결과가 이렇게 나온 이유는, **예외 객체 전체를 그대로 JSON으로 직렬화해서 응답**했기 때문이다.
Jackson(ObjectMapper)이 `Throwable`을 JSON으로 변환하면 내부적으로 다음을 모두 포함한다:

* stackTrace 배열
* suppressed 예외
* localizedMessage
* message

즉 현재 컨트롤러에서 이런 식으로 하고 있을 가능성이 높다:

```java
map.put("exception", ex); // Throwable 전체
```

그럼 Jackson이 Exception을 그대로 풀어서 huge JSON을 만들게 된다.

---

# 문제: exception 전체를 JSON으로 넣으면 너무 자세한 정보가 노출됨

보안 측면에서도 위험하다.
스택트레이스까지 클라이언트에 노출하는 것은 일반적으로 금지된다.

---

# 해결 방법

## 1) exception 전체가 아니라 필요한 정보만 뽑아 넣기

예외 객체를 직접 넣는 대신 **예외 메시지나 타입만** 넣는다:

```java
Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

Map<String, Object> result = new HashMap<>();
result.put("status", request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
result.put("message", ex != null ? ex.getMessage() : null);
result.put("error", ex != null ? ex.getClass().getName() : null);
result.put("path", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

return result;
```

결과:

```json
{
  "status": 500,
  "message": "멤버 에러",
  "error": "java.lang.RuntimeException",
  "path": "/members/err"
}
```

깔끔하고 안전하다.

---

## 2) DTO를 사용해 정확히 필요한 정보만 제공

```java
public class ErrorResult {
    private int status;
    private String message;
    private String error;
    private String path;

    public ErrorResult(int status, String message, String error, String path) {
        this.status = status;
        this.message = message;
        this.error = error;
        this.path = path;
    }
}
```

컨트롤러:

```java
@RequestMapping("/error-page/Re")
@ResponseBody
public ErrorResult errorRe(HttpServletRequest request) {

    int status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

    return new ErrorResult(
        status,
        ex != null ? ex.getMessage() : "No message",
        ex != null ? ex.getClass().getSimpleName() : "N/A",
        path
    );
}
```

---

## 3) 클라이언트가 개발 환경일 때만 stackTrace 포함 (고급)

Spring Boot가 기본으로 제공하는 방식처럼:

* 개발 환경(dev): stackTrace 포함
* 운영 환경(prod): stackTrace 제거

직접 만들려면 프로필 검사:

```java
@Value("${spring.profiles.active:default}")
private String profile;

if (profile.equals("dev")) {
    result.put("trace", ExceptionUtils.getStackTrace(ex));
}
```

---

# 정리

지금처럼 거대한 JSON이 나오는 건 **Exception 전체를 JSON으로 변환했기 때문**이다.
실제 운영에서는 절대 이렇게 응답하면 안 된다.
따라서 다음과 같이 처리하는 것이 정석이다:

* message
* error type
* status
* path

이 정도만 JSON으로 응답하고
stackTrace는 로그에만 찍고, 클라이언트에는 제공하지 않는다.

---

원하면 **Spring Boot의 기본 /error 구조와 최대한 동일한 JSON 포맷으로 만들어주는 커스텀 ErrorController**도 만들어줄 수 있어.

결론부터 말하면,

**네. 저 코드 그대로면 지금처럼 “exception 전체(스택트레이스 포함)”가 JSON에 모두 들어가는 게 정상이다.**

이유는 다음 한 줄 때문이다.

```java
result.put("exception", exception);
```

여기서 `exception`은 아래 타입이다.

```java
Throwable  // 실제로는 RuntimeException
```

그리고 Jackson(ObjectMapper)은 `Throwable`을 직렬화할 때 내부 필드들을 전부 JSON으로 만든다.

그래서 다음이 모두 JSON 형태로 출력된다:

* message
* localizedMessage
* stackTrace 배열
* suppressed
* cause
* className
* moduleName
* moduleVersion
* fileName
* lineNumber
* nativeMethod 여부
* 등등…

즉 “스택트레이스 콘솔 로그에서 보던 내용 전체”가 그대로 JSON으로 들어가는 것이다.

---

# 왜 Throwable을 JSON으로 넣으면 이렇게 되는가?

Spring의 JSON 변환 라이브러리(Jackson)는
일반 객체의 모든 getter 메서드와 필드를 직렬화한다.

Throwable에는 매우 많은 필드가 들어있기 때문에:

* `getStackTrace()`
* `getSuppressed()`
* `getCause()`
* `getLocalizedMessage()`
* `toString()`

이런 것들을 죄다 JSON 구조로 풀어버린다.

따라서 그 긴 JSON은 **정상적인 Jackson 동작**이다.

---

# 어떻게 하면 간단하게 줄일 수 있을까?

## 방법 1: exception 전체를 넣지 않고 필요한 부분만 수동으로 넣기

```java
Throwable ex = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

Map<String, Object> result = new HashMap<>();
result.put("status", status);
result.put("message", ex.getMessage());
result.put("error", ex.getClass().getName());
result.put("uri", uri);

return new ResponseEntity<>(result, HttpStatus.valueOf((Integer) status));
```

이렇게 하면 출력:

```json
{
  "status": 500,
  "message": "멤버 에러",
  "error": "java.lang.RuntimeException",
  "uri": "/members/err"
}
```

---

## 방법 2: exception을 문자열로 변환해서 넣기

```java
result.put("exception", exception.toString());
```

이렇게 하면 JSON은

```json
"exception": "java.lang.RuntimeException: 멤버 에러"
```

이 정도만 들어간다.

---

## 방법 3: DTO 사용 (더 정석)

```java
public class ErrorResult {
    private int status;
    private String message;
    private String error;
    private String path;
}
```

컨트롤러에서:

```java
return new ErrorResult(status, ex.getMessage(), ex.getClass().getSimpleName(), uri);
```

---

# 정리

당신 코드가 스택트레이스 전체를 JSON화한 이유는:

1. `exception` 필드에 `Throwable` 객체를 그대로 넣었다
2. Jackson이 `Throwable`의 모든 속성을 JSON으로 직렬화했다
3. 그 결과 엄청난 양의 정보가 포함된 JSON 응답이 된 것이다

즉 오류가 아니라 **정상 동작**이다.

원하면 이제 “운영환경에서 절대 노출하면 안 되는 정보”만 추리고
커스텀 에러 JSON 객체를 만드는 방법도 알려줄게.

{
"message": "Request processing failed: java.lang.RuntimeException: 멤버 에러",
"uri": "/members/err",
"status": 500
}

