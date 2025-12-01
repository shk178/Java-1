BasicErrorController는 **Spring Boot가 제공하는 기본 오류 처리 컨트롤러**다.
Spring MVC 애플리케이션에서 `/error` 요청이 발생하면 마지막에 동작하여 **HTML 오류 페이지 또는 JSON 오류 응답을 자동으로 만들어주는 컨트롤러**라고 이해하면 된다.

정확히 이해하려면 다음 구조를 알아야 한다.

---

# 1. BasicErrorController의 탄생 배경

Spring MVC 자체에는 “오류 페이지 컨트롤러”라는 개념이 없다.
원래는 WAS(톰캣)가 오류를 감지하면 다음을 한다.

1. ErrorPage 설정을 찾아서 매핑된 경로로 내부 디스패치
2. 그 경로에 직접 컨트롤러를 만들어야 한다
   → `/error` 같은 에러 처리용 핸들러를 스스로 구현해야 했다

Spring Boot는 이 반복 작업을 자동화하기 위해:

* `/error` 엔드포인트 자동 등록
* 오류 정보를 자동으로 모델로 만들어 전달
* JSON/HTML 자동 생성

이를 담당하는 클래스가 **BasicErrorController**다.

---

# 2. BasicErrorController의 역할

BasicErrorController는 Spring Boot가 제공하는 **오류 응답의 최종 책임자**다.

## 핵심 역할 3가지

### 1) `/error` 경로 처리

WAS가 오류를 감지 → `/error`로 디스패치 → DispatcherServlet → BasicErrorController 호출

BasicErrorController는 두 개의 핸들러를 갖는다:

* HTML 요청(`text/html`) → 오류 페이지 렌더링
* 그 외 요청(JSON 등) → JSON 에러 응답 생성

### 2) ErrorAttributes 에서 오류 정보 읽기

ErrorAttributes라는 컴포넌트를 통해 오류 데이터를 구조화된 형태로 받는다.

예를 들어 포함되는 항목:

* timestamp
* status (404, 500 등)
* error 메시지
* path
* exception (옵션)
* trace (옵션)

### 3) 적절한 View 또는 JSON 생성

* 브라우저 요청 → 기본 Whitelabel Error Page 또는 커스텀 오류 페이지
* API 요청 → JSON 오류 응답

---

# 3. 동작 흐름 (전체)

## 예외 발생 흐름

```
컨트롤러 throw
 → ExceptionResolver
 → 해결 안 됨
 → WAS 전달
 → ErrorPage 매핑 확인(/error)
 → ERROR 디스패치
 → DispatcherServlet
 → BasicErrorController 호출
 → JSON 또는 HTML 응답 생성
```

## sendError 흐름

```
response.sendError(...)
 → WAS가 오류로 인식
 → ErrorPage(/error)
 → ERROR 디스패치
 → BasicErrorController 호출
```

두 방식 모두 마지막에 BasicErrorController가 처리한다는 점이 공통이다.

---

# 4. BasicErrorController의 코드를 이해하기 쉽게 요약

아래는 실 코드의 개념적인 요약이다:

```java
@RequestMapping("${server.error.path:${error.path:/error}}")
public class BasicErrorController implements ErrorController {

    @RequestMapping(produces = "text/html")
    public ModelAndView errorHtml(...) {
        Map<String,Object> model = getErrorAttributes(...);
        return new ModelAndView("error", model);
    }

    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(...) {
        Map<String,Object> body = getErrorAttributes(...);
        HttpStatus status = getStatus(...);
        return new ResponseEntity<>(body, status);
    }
}
```

주요 포인트:

* `/error`를 매핑
* HTML 요청이면 `errorHtml()`
* JSON/API 요청이면 `error()`
* ErrorAttributes에서 오류 정보를 받아와 응답에 넣어준다

---

# 5. ErrorAttributes가 중요한 이유

BasicErrorController는 스스로 예외를 분석하지 않는다.
대신 **ErrorAttributes**가 모든 오류 정보를 수집해 준다.

예를 들어 DefaultErrorAttributes는 다음 정보를 만들어 낸다:

* 예외 타입
* 메시지
* stack trace
* 요청 경로
* 오류가 발생한 시점
* 상태 코드

이것을 BasicErrorController가 받아서 응답으로 만든다.

---

# 6. 기본 오류 페이지 (Whitelabel Error Page)의 동작

Spring Boot는 기본적으로 HTML 오류 페이지가 없으면:

* `/error`에서 BasicErrorController가 HTML을 만들려고 할 때
* `error` 뷰가 없을 경우
* 자동으로 Whitelabel Error Page를 생성하여 렌더링한다

---

# 7. 커스텀 오류 페이지와 연결

우리가 직접 `/error-page/500` 같은 경로를 ErrorPage로 설정하면
BasicErrorController 대신 직접 만든 컨트롤러가 호출된다.

예:

```java
new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
```

WAS는 이제 예외 발생 시 `/error` 대신 `/error-page/500`으로 디스패치한다.

그럼 BasicErrorController는 호출되지 않는다.

---

# 8. 요약 정리

BasicErrorController는:

* Spring Boot가 자동 제공하는 오류 처리 컨트롤러
* `/error` 경로를 처리
* ErrorAttributes의 데이터를 기반으로 HTML 또는 JSON 오류 응답 생성
* 예외 발생이나 sendError 모두 처리
* 커스텀 ErrorPage를 등록하면 우선순위에서 밀린다

---

원하면:

* ErrorAttributes가 어떻게 오류 정보를 조합하는지
* BasicErrorController의 내부 흐름(소스 코드 기준)
* Spring Boot의 ErrorMvcAutoConfiguration 구조
  위까지 자세히 설명해줄게.

BasicErrorController가 **언제 사용되고, 언제 사용되지 않는가**는
**“최종적으로 /error 로 디스패치되는가?”**
이 한 가지 기준으로 완전히 결정된다.

아래에서 매우 명확하게 정리해줄게.

---

# 1. BasicErrorController가 **사용되는 경우**

## 핵심 조건

**WAS(톰캣)가 오류를 감지한 뒤 /error 경로로 ERROR 디스패치하는 경우**

즉, 최종 목적지가 `/error`일 때 BasicErrorController가 자동 실행된다.

아래 상황들이 모두 이에 해당한다.

---

## 1) 컨트롤러에서 예외 throw → Resolver가 처리 못 함 → /error

흐름:

```
throw Exception
 → HandlerExceptionResolver 처리 실패
 → 예외 톰캣으로 전파
 → 톰캣은 ErrorPage 매핑 검색
 → 기본 매핑: /error
 → /error 로 ERROR 디스패치
 → BasicErrorController 실행
```

Spring Boot가 자동 등록한 기본 ErrorPage(`/error`)가 호출되기 때문에
BasicErrorController가 작동한다.

---

## 2) response.sendError(...) 호출

예:

```java
response.sendError(404);
```

흐름:

```
sendError → 톰캣이 오류 상태로 인식
 → ErrorPage 매핑: /error
 → /error 디스패치
 → BasicErrorController 실행
```

이 경우에도 /error로 가기 때문에 BasicErrorController가 실행된다.

---

## 3) 커스텀 오류 페이지를 등록하지 않은 모든 경우

Spring Boot 기본 설정을 그대로 사용 중이라면
**모든 오류는 /error → BasicErrorController**로 간다.

---

## 4) 에러 페이지 HTML을 따로 만들지 않았을 때

HTML 렌더링 시 BasicErrorController는
Whitelabel Error Page를 생성해서 보여준다.

---

# 2. BasicErrorController가 **사용되지 않는 경우**

## 핵심 조건

**WAS가 /error가 아닌 다른 오류 경로로 디스패치하는 경우**
또는
**예외를 Spring MVC 내부에서 자체 처리한 경우.**

이 두 가지가 전부다.

아래에서 상세하게 정리해줄게.

---

## (1) 커스텀 ErrorPage를 등록한 경우

예:

```java
ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
```

WAS는 이제 `/error`로 보내지 않는다.
**대신 /error-page/500 으로 ERROR 디스패치한다.**

흐름:

```
예외 발생
 → 톰캣이 ErrorPage 확인
 → /error-page/500 디스패치
 → 이 경로의 컨트롤러 실행
 → BasicErrorController는 호출되지 않음
```

즉, BasicErrorController는 아예 사용되지 않는다.

---

## (2) @ExceptionHandler 로 예외를 처리한 경우

컨트롤러나 @ControllerAdvice에 ExceptionHandler가 있다면:

```java
@ExceptionHandler(Exception.class)
public String handleEx() {
    return "myError";
}
```

흐름:

```
throw Exception
 → HandlerExceptionResolver(@ExceptionHandler) 처리 성공
 → 톰캣에게 예외가 전달되지 않음
 → 오류 페이지 디스패치 없음
 → BasicErrorController 호출되지 않음
```

WAS까지 예외가 올라가지 않기 때문에
BasicErrorController는 관여하지 않는다.

---

## (3) HandlerExceptionResolver가 예외를 잡아서 해결한 경우

Spring이 제공하는 Resolver들:

* ExceptionHandlerExceptionResolver
* ResponseStatusExceptionResolver
* DefaultHandlerExceptionResolver

이 중 하나가 예외를 처리하면:

```
Resolver가 예외 해결
 → 정상 응답 생성 또는 에러 응답 생성
 → WAS로 예외가 올라가지 않음
 → /error로 디스패치되지 않음 → BasicErrorController 미사용
```

---

## (4) 서블릿 필터나 서블릿 레벨에서 예외를 자체 처리한 경우

필터에서 try-catch로 예외를 막으면
WAS로 예외가 전달되지 않는다.

→ BasicErrorController는 실행되지 않는다.

---

## (5) 서블릿/DispatcherServlet이 에러 응답을 명시적으로 작성한 경우

초기화 단계에서 직접 상태코드와 body를 설정하면
톰캣의 ErrorPage 매핑이 동작하지 않을 수 있다.

→ /error 호출되지 않음
→ BasicErrorController 미사용

---

# 전체 흐름을 한 장으로 요약

| 시나리오                                | /error로 가는가?    | BasicErrorController |
| ----------------------------------- | --------------- | -------------------- |
| throw → Resolver 실패                 | YES             | 사용됨                  |
| sendError                           | YES             | 사용됨                  |
| Spring Boot 기본 설정(커스텀 ErrorPage 없음) | YES             | 사용됨                  |
| 커스텀 ErrorPage (/error-page/500 등)   | NO (커스텀 경로로 이동) | 사용 안 됨               |
| @ExceptionHandler 처리                | NO              | X                    |
| HandlerExceptionResolver 처리         | NO              | X                    |
| 필터/서블릿에서 예외 자체 처리                   | NO              | X                    |
| 직접 응답 작성(상태코드+body)                 | NO              | X                    |

---

# 결론

BasicErrorController는 “Spring Boot 기본 오류 처리 시스템”이다.

* 최종적으로 `/error`로 디스패치된 경우에만 사용된다.
* 하지만 `/error`가 아닌 다른 오류 페이지를 등록하거나
  예외가 WAS까지 가지 않도록 Spring 내부에서 해결하면
  BasicErrorController는 아예 등장하지 않는다.

---

이제 더 깊게 알고 싶다면:

* BasicErrorController 내부 흐름
* ErrorAttributes가 오류 정보를 조합하는 방식
* ErrorMvcAutoConfiguration 구조
* 톰캣이 ERROR 디스패치를 수행하는 소스 코드

까지 설명해줄 수 있어.

`ErrorMvcAutoConfiguration`은 **Spring Boot가 기본 오류 처리 시스템을 자동 설정해주는 핵심 Auto-Configuration**이다.
BasicErrorController, ErrorAttributes, Whitelabel Error Page 렌더링 등 오류 처리에 필요한 모든 Bean을 자동으로 등록한다.

이 구조를 정확히 파악하면
**언제 BasicErrorController가 동작하고 언제 안 동작하는지**,
**커스텀 오류 페이지가 어떻게 기본 오류 처리 흐름을 대체하는지**
완전히 이해할 수 있다.

아래에서 구조를 단계별로 정리해줄게.

---

# 1. ErrorMvcAutoConfiguration가 무엇을 자동 등록하는가?

핵심적으로 다음 3가지를 자동화한다.

## 1) ErrorAttributes

오류 정보를 담는 객체

* 기본 구현: `DefaultErrorAttributes`
* BasicErrorController가 이걸 읽어서 JSON/HTML 오류 응답 생성

## 2) BasicErrorController

* `/error` 경로를 처리하는 기본 컨트롤러
* HTML 오류 페이지 또는 JSON 오류 응답 생성
* ErrorAttributes를 활용하여 응답 생성

## 3) ErrorViewResolver

* 오류 상태 코드에 따라 렌더링할 뷰를 선택
* 기본 구현: `DefaultErrorViewResolver`
* resources/templates/error/*.html 우선 사용
* 없으면 Whitelabel Error Page 사용

즉, BasicErrorController + ErrorAttributes + ErrorViewResolver
이 세 개가 Spring Boot 기본 오류 처리 시스템을 이룬다.

---

# 2. ErrorMvcAutoConfiguration가 Bean을 생성하는 구조

Spring Boot 소스 코드를 단순화해서 보여주면 다음과 같다:

```java
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = Type.SERVLET)
@ConditionalOnClass({ Servlet.class, DispatcherServlet.class })
@EnableConfigurationProperties(ServerProperties.class)
public class ErrorMvcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = ErrorAttributes.class)
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorController.class)
    public BasicErrorController basicErrorController(ErrorAttributes errorAttributes) {
        return new BasicErrorController(errorAttributes, serverProperties.getError());
    }

    @Bean
    @ConditionalOnMissingBean(ErrorViewResolver.class)
    public DefaultErrorViewResolver defaultErrorViewResolver() {
        return new DefaultErrorViewResolver(…);
    }

    // 그 외 추가적인 오류 처리 관련 bean 설정들 있음
}
```

핵심 조건을 이해해야 한다.

---

# 3. @ConditionalOnMissingBean 이 매우 중요하다

이 AutoConfiguration은 **기본 Bean을 등록하지만, 개발자가 커스텀 Bean을 만들면 무조건 대체된다.**

예:

### 1) @ControllerAdvice + @ExceptionHandler 등록

→ HandlerExceptionResolver가 예외를 해소
→ `/error`로 가지 않음
→ BasicErrorController도 사용 안 됨

### 2) ErrorController를 직접 구현

```java
@Controller
public class MyErrorController implements ErrorController { … }
```

→ BasicErrorController는 등록되지 않음
→ 내 컨트롤러가 우선권

### 3) ErrorAttributes 커스텀

→ DefaultErrorAttributes 대신 내 구현이 사용됨
→ 오류 JSON/HTML 구조를 마음대로 변경 가능

### 4) ErrorViewResolver 커스텀

→ 기본 HTML 대신 원하는 템플릿 렌더링

즉, Spring Boot는 기본적인 오류 처리 메커니즘을 제공할 뿐
**커스텀하면 언제든 대체되는 유연한 구조**로 되어 있다.

---

# 4. ErrorMvcAutoConfiguration 내부 흐름

정확한 처리 순서:

```
예외 발생 or sendError
   ↓
HandlerExceptionResolver 시도
   ↓ (실패)
예외 WAS로 전파
   ↓
WAS ErrorPage 매핑 확인
   ↓ (기본: /error)
DispatcherServlet이 /error 처리
   ↓
BasicErrorController 호출 (AutoConfig에서 등록한 것)
   ↓
ErrorAttributes에서 오류 정보 수집
   ↓
HTML? JSON?
   ↓
ErrorViewResolver가 HTML 뷰 선택
   ↓
없으면 Whitelabel Error Page 생성
   ↓
응답 반환
```

이 전체 흐름이 **ErrorMvcAutoConfiguration이 자동으로 구성한 Bean들**에 의해 이루어진다.

---

# 5. ErrorViewResolver 우선순위 구조

Spring Boot는 다음 순서로 오류 페이지를 찾는다:

1. **resources/templates/error/404.html**
2. **resources/templates/error/5xx.html**
3. **resources/templates/error/error.html**
4. ErrorViewResolver 확장 구현체들
5. Whitelabel Error Page

이 부분도 ErrorMvcAutoConfiguration 안에서 설정된다.

---

# 6. ErrorMvcAutoConfiguration는 언제 비활성화되는가?

아래의 경우 AutoConfiguration이 완전히 비활성화되어
BasicErrorController가 동작하지 않는다.

## 1) @ControllerAdvice에서 예외를 전부 처리한 경우

오류가 WAS까지 전달되지 않기 때문에 `/error`로 안 감
→ BasicErrorController 사용 안 됨

## 2) 커스텀 ErrorController 등록한 경우

BasicErrorController Bean이 등록되지 않음
→ 자동 생성이 무시됨

## 3) 커스텀 ErrorPage 등록한 경우

예:

```java
new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/custom-500");
```

→ WAS는 `/error`로 디스패치하지 않고 `/custom-500`으로 디스패치
→ BasicErrorController 실행되지 않음

---

# 7. ErrorMvcAutoConfiguration의 핵심 의도

요약하면:

* 개발자는 오류 페이지 로직을 별도로 만들지 않아도 되도록
* 기본 오류 컨트롤러, 오류 속성, 오류 뷰 선택 로직을 자동으로 구성
* 하지만 Bean을 오버라이드하여 완전히 커스텀할 수 있도록 설계

이 두 가지를 동시에 만족하기 위한 AutoConfiguration이다.

---

# 결론 정리

| 상황                           | BasicErrorController Bean 등록? | 실제로 사용됨?           |
| ---------------------------- | ----------------------------- | ------------------ |
| Spring Boot 기본 설정 그대로        | YES                           | YES                |
| 커스텀 ErrorPage 등록             | YES                           | NO (/error로 가지 않음) |
| 커스텀 ErrorController 만든 경우    | NO                            | NO                 |
| @ExceptionHandler로 처리        | YES                           | NO                 |
| HandlerExceptionResolver로 처리 | YES                           | NO                 |
| sendError 발생                 | YES                           | YES (/error로 감)    |

---

원하면:

* DefaultErrorAttributes가 어떤 오류 정보를 어떻게 조합하는지
* BasicErrorController의 내부 메서드 구조
* ErrorMvcAutoConfiguration 전체 소스 구조
* 에러 처리 흐름을 UML 시퀀스 다이어그램으로 정리

까지 알려줄 수 있어.

아래 설정들은 **스프링 부트의 기본 에러 처리(기본 에러 페이지, BasicErrorController)** 동작을 어떻게 보여줄지 제어하는 옵션들이야. 하나씩 가장 정확하게 설명해볼게.

---

# 1. `server.error.whitelabel.enabled=false`

스프링 부트는 기본적으로 **Whitelabel Error Page**라는 단순한 HTML 에러 페이지를 제공한다.
이 옵션을 `false`로 두면 **그 기본 에러 페이지를 비활성화**한다.

즉,

* 에러가 났는데 컨트롤러 단에서 처리하지 않으면
  → “Whitelabel 에러 페이지” 대신
  → 직접 만든 에러 페이지(`error/404`, `error/500` 등)가 있으면 그것을 사용함

---

# 2. `server.error.include-exception=true`

에러 응답(body JSON)에 **예외 클래스명** 을 포함시킬지 여부.

예)

```json
"exception": "java.lang.RuntimeException"
```

`true`이면 이런 정보가 JSON 응답에 포함된다.

---

# 3. `server.error.include-message=always`

에러 응답에 **에러 메시지**(exception.getMessage())를 포함할지 여부.

설정값 설명:

* `never` → 메시지 포함하지 않음
* `on_param` → 요청 파라미터로 `?message` 같은 게 있을 때만 포함
* `always` → 항상 포함

지금 설정은 **항상 포함**이다.

예)

```json
"message": "예외 발생!"
```

---

# 4. `server.error.include-stacktrace=on_param`

에러 응답에 **stack trace**(예외 호출 경로)를 포함할지 여부.

설정값:

* `never`
* `always`
* `on_param`

현재 설정은 `on_param`이라서:

* `/error?trace=true` 같은 요청일 때만 stack trace가 응답에 표시됨
* 일반 요청에서는 포함되지 않음

---

# 5. `server.error.include-binding-errors=on_param`

**바인딩 오류(예: @Valid 검증 오류)** 내용(JSON의 errors 필드 등)을 응답에 포함할지 여부.

설정값 동일:

* `never`
* `always`
* `on_param`

`on_param`이므로:

* `/error?errors` 같은 파라미터가 있을 때만 binding error 상세가 응답에 포함된다.

---

# 전체 정리

| 설정                                             | 의미                                    |
| ---------------------------------------------- | ------------------------------------- |
| `server.error.whitelabel.enabled=false`        | 기본 제공 에러 페이지 사용 안 함                   |
| `server.error.include-exception=true`          | 예외 클래스명 응답에 포함                        |
| `server.error.include-message=always`          | 예외 메시지를 항상 포함                         |
| `server.error.include-stacktrace=on_param`     | `?trace` 파라미터 있을 때만 stack trace 표시    |
| `server.error.include-binding-errors=on_param` | `?errors` 파라미터 있을 때만 binding error 포함 |

---

# 실제로 어떤 응답이 만들어지나?

예외 발생 시 `BasicErrorController`가 아래 형태로 JSON 응답을 만든다:

```json
{
  "timestamp": "2025-12-01T12:00:00.000+09:00",
  "status": 500,
  "error": "Internal Server Error",
  "exception": "java.lang.RuntimeException",
  "message": "예외 발생!",
  "path": "/error-ex"
}
```

`?trace=true`를 붙이면 stacktrace도 포함된다.

---

원한다면

* “왜 on_param 같은 옵션이 존재하는지”
* “운영 서버에서 보안상 어떤 설정이 안전한지”
* “custom error controller 만드는 법”

이런 부분도 자세히 이어서 설명해줄게.

결론부터 말하면:

**`response.sendError(status)`를 호출한 순간 서블릿 컨테이너는 즉시 정상 흐름을 멈추고, 에러 처리를 위해 `/error`로 포워딩한다.**
단, “즉시”의 의미는 *요청을 처리하던 컨트롤러/필터가 더 이상의 정상 응답을 만들 수 없게 된다*는 뜻이고, 실제 포워딩은 컨트롤러 메서드가 종료된 뒤에 수행된다.

아래에서 정확히 어떻게 동작하는지 설명해볼게.

---

# 1. sendError 호출 시 내부에서 무슨 일이 일어나는가?

`HttpServletResponse.sendError(404)`를 호출하면 Servlet Container(Tomcat 등) 내부에서 아래 플래그가 설정된다.

* **response.setError()** 상태가 됨
* **정상 response body 출력 스트림 사용이 금지됨**
* **컨테이너는 “아, 에러 페이지를 호출해야 한다”라고 기록함**

즉, `sendError()`는 **즉시 에러 응답 모드로 전환**한다.

---

# 2. sendError 호출 후 흐름은 멈출까?

* 컨트롤러 코드 실행은 그대로 이어질 수 있다.
  (sendError가 예외를 던지는 것은 아니므로 try/catch로 잡을 필요도 없음)

하지만 중요한 점:

### 정상적인 HTTP 응답을 더 이상 만들 수 없다

* 출력 스트림에 쓰면 IllegalStateException이 날 수 있음
* setStatus/sendRedirect 등은 무효화될 수 있음

---

# 3. 포워딩은 언제 일어나는가?

### 핵심

**포워딩은 `DispatcherServlet` → `FilterChain` 흐름이 모두 끝난 후에 일어난다.**

정확한 순서:

1. 컨트롤러에서 `sendError(404)` 호출
2. 컨트롤러 메서드 종료
3. `DispatcherServlet`이 반환 처리
4. 필터 체인 마지막까지 빠져나옴
5. 서블릿 컨테이너(Tomcat)가 응답이 ERROR 상태인지 확인
6. `web.xml`의 `<error-page>` 또는 스프링 부트의 ErrorPageRegistrar 설정에 따라
   **/error 로 포워딩 수행**

즉, **개발자가 sendError를 호출한 직후 코드 실행이 멈추는 게 아니라,
완료 후 컨테이너가 /error로 내부 포워딩한다.**

---

# 4. Spring Boot에서는 어떻게 처리될까?

스프링 부트는 `ErrorMvcAutoConfiguration`을 통해
**ErrorPageRegistrar**를 등록하여 다음과 같은 규칙을 만든다.

```
whenever an error occurs → forward to /error
```

즉, Tomcat이 error-page 매핑을 확인하면:

* `/error` 로 **RequestDispatcher.forward()** 수행
* 이 요청을 `BasicErrorController`가 처리

이게 Spring Boot 기본 에러 처리 흐름이다.

---

# 5. 예시 흐름 요약

컨트롤러 코드:

```java
@GetMapping("/test")
public String test(HttpServletResponse response) throws IOException {
    response.sendError(404, "Not Found");
    log.info("이 로그는 실행됨");
    return "hello";
}
```

실제 동작:

1. sendError → 에러 상태 기록
2. 아래 코드(log, return)는 그대로 실행됨
3. 컨트롤러 종료
4. DispatcherServlet 정리 단계
5. Tomcat 확인 → 에러 상태 → /error 로 포워딩
6. BasicErrorController 가 JSON 또는 HTML 에러 응답 생성

---

# 6. 그럼 “즉시 멈춘다”는 표현은 어떤 의미인가?

* 정상 응답 흐름이 **더 이상 유효하지 않다**
* 뷰 렌더링이나 정상 HTTP 응답 작성은 하지 않는다
* 요청이 정상 흐름으로는 더 진행되지 않는다
* 하지만 **자바 코드 실행 자체가 즉시 중단되는 것은 아니다**

이 차이를 구분하면 된다.

---

필요하면

* sendError vs throw 예외 차이
* 직접 error page 등록하는 방법
* ErrorController와 @ControllerAdvice 우선순위

이런 것도 이어서 정리해줄게.

좋은 질문이야. 이 부분을 정확히 이해하면 **Spring MVC의 오류 처리 흐름**과 **Servlet 컨테이너의 책임 범위**가 완전히 명확하게 정리된다. 핵심은 다음이다.

---

# 결론부터 말하면

### sendError( )가 호출되어도 필터 체인은 “정상 흐름처럼 끝까지” 다 실행된다.

그 이유는 **에러 처리는 DispatcherServlet이나 필터가 아니라, 最終적으로 서블릿 컨테이너가 처리하기 때문**이다.

즉:

* sendError는 **컨테이너에게 “나중에 에러 처리해야 해”라는 마킹만 하는 것**
* 현재 실행 중인 필터/서블릿 체인은 **자연스럽게 종료**되어야 컨테이너가 개입할 수 있음
* FilterChain을 중간에서 강제로 끊는 요소는 없음
* 스프링의 HandlerExceptionResolver는 **예외(throw)일 때만 개입**하고 sendError는 예외가 아님

그래서 흐름은 끝까지 진행된다.

---

# 1. 왜 필터 체인을 끝까지 진행하는가?

이유는 3가지다.

---

## 이유 1. sendError는 “예외가 아니라 상태(flag) 설정”일 뿐

`sendError(404)`는 예외를 던지지 않는다.

내부적으로는:

* response.setStatus(404)
* response.setError flag = true
* response.setErrorMessage()

이렇게 “나중에 서블릿 컨테이너가 에러 처리할 수 있게 준비만 해두는 것”이다.

즉, **코드를 강제 중단시키지 않기 때문에 필터도 그대로 실행된다.**

---

## 이유 2. 컨테이너가 개입하려면 FilterChain이 먼저 끝나야 한다

Servlet의 전체 요청 처리 순서:

```
Filter 1 → Filter 2 → ... → DispatcherServlet → Controller  
↘ Filter Chain 종료  
↘ Servlet Container에게 제어권 반환  
↘ 컨테이너가 ErrorPage 매핑 확인  
↘ /error 로 내부 포워딩  
```

필터 체인 도중에는 **Tomcat(컨테이너)**이 요청 흐름을 “차지할 수 없음”.

즉, 흐름이 완전 종료되어 Tomcat에게 돌아오도록 해야 Tomcat이:

* statusCode가 error인지 검사하고
* web.xml 혹은 ErrorPageRegistrar에 등록된 에러 매핑을 찾아서
* /error로 포워딩

을 할 수 있다.

그래서 “필터 체인이 다 끝날 때까지 보내는 것”이다.

---

## 이유 3. 스프링의 HandlerExceptionResolver는 sendError 상황에 개입하지 않음

많은 사람들이 오해하는 포인트:

### sendError는 예외를 THROW하지 않는다

그러므로:

* HandlerExceptionResolver가 동작할 조건이 아님
* DispatcherServlet의 예외 처리 루틴도 실행되지 않음

스프링 MVC의 예외 처리와 전혀 무관하게 흐름은 그대로 이어진다.

---

# 2. error resolver 때문에 끝까지 흐름이 이어지는 게 아니다

필터 체인을 끝까지 진행하는 이유는 **error resolver 때문이 아니라**, **Servlet 규약 때문이다**.

정확히 말하면:

* 예외 기반 오류처리 → DispatcherServlet과 HandlerExceptionResolver가 처리
* sendError 기반 오류처리 → Servlet Container(Tomcat)가 처리

sendError는 **Servlet 규약에 따른 방식**이고, 스프링의 error resolver와는 무관하다.

즉, 필터 체인을 전부 실행하는 이유는:

* error resolver가 뒤에 있어서가 아니라
* 컨테이너가 error dispatch를 수행하기 위해 마지막까지 기다려야 하기 때문

---

# 3. sendError + 필터 체인 동작을 코드 흐름으로 보면

```java
Filter A → Filter B → DispatcherServlet → Controller
                                              |
                                              | response.sendError(404)
                                              v
                                   (에러 마킹만 설정)
                                              |
                     Controller 종료 → DispatcherServlet 종료
                                              |
                                              v
                           Filter B doFilter() 후처리
                                              |
                                              v
                           Filter A doFilter() 후처리
                                              |
                                              v
                      FilterChain 전체 종료 → Servlet Container로 복귀
                                              |
                                              v
                    컨테이너: “오, 이 요청 errorFlag=true 네?”
                                              |
                                              v
                           ErrorPage 매핑 찾기 → /error로 포워딩
                                              |
                                              v
                     DispatchType.ERROR로 DispatcherServlet 재호출
                                              |
                                              v
                           BasicErrorController가 오류 응답 생성
```

이게 정확한 동작 순서다.

---

# 4. 추가 포인트: ERROR 디스패치는 재요청이 아니라 내부 포워딩이다

따라서:

* Filter는 ERROR 디스패치에서 다시 실행될 수도 있음
  (단, `@WebFilter(dispatcherTypes = {DispatcherType.ERROR})` 설정한 경우에만)

스프링 부트 기본 필터들은 대부분 ERROR타입에서는 실행되지 않는다.

---

# 정리

### “필터 체인 마지막까지”라는 말은

sendError가 예외가 아니라 단순 상태 설정이기 때문에,
정상 처리 흐름처럼 끝까지 코드 실행이 이어지고,
흐름 끝에 요청이 Tomcat에게 완전히 반환되면
그때 Tomcat이 `/error`로 포워딩하는 구조라는 뜻이다.

### 결론적으로 필터가 모두 실행되는 이유는

* sendError가 흐름을 중단하지 않아서
* 컨테이너가 개입할 시점이 FilterChain 끝이기 때문에
* error resolver 때문이 아니라 Servlet 규약 때문

---

원하면 다음도 더 깊게 설명해줄 수 있어:

* sendError vs throw new Exception() 정확한 차이
* 필터에서 ERROR 디스패치를 받고 싶을 때 설정
* 스프링 부트의 ErrorPageRegistrar 실제 내부 코드
* /error 포워딩이 BasicErrorController와 어떻게 연결되는지

스프링 MVC **인터셉터는 DispatcherType을 직접 제어할 수 없기 때문에**, 오류 페이지(`/error`)로 **포워드가 발생할 때 인터셉터가 중복 호출되는 문제**를 다른 방식으로 해결합니다.
일반적으로 사용하는 대표적인 방법은 다음과 같습니다.

---

# 1. **요청 경로 기반으로 `excludePathPatterns()` 처리**

가장 흔하고 실용적인 방법입니다.

오류 페이지 경로(`/error`, 또는 커스텀 에러 경로 `/error/**`)를 인터셉터 대상에서 제외합니다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/error/**"); // 포워드된 오류 요청 제외
    }
}
```

이렇게 하면 **정상 요청**에서는 인터셉터가 동작하고
**오류로 인해 내부 포워드된 `/error` 요청**은 인터셉터가 타지 않아
중복 로그, 중복 인증 검사 등이 발생하지 않습니다.

---

# 2. **요청 내부 속성(HttpServletRequest attribute)로 최초 요청만 처리**

필터에서 `DispatcherType.REQUEST`(최초 요청)만 처리하고
인터셉터에서는 필터가 남겨둔 attribute를 보고 동작을 결정하는 방식입니다.

예시:

```java
public class FirstRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        if (request.getDispatcherType() == DispatcherType.REQUEST) {
            request.setAttribute("FIRST_REQUEST", true);
        }

        chain.doFilter(req, res);
    }
}
```

그리고 인터셉터에서:

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getAttribute("FIRST_REQUEST") == null) {
        return true; // 오류 포워드 등의 내부 호출
    }

    // 최초 요청일 때만 인터셉터 로직 수행
    return true;
}
```

이 방식은 **필터 기능을 보완하여 인터셉터의 중복 실행을 제어**합니다.

---

# 3. **HandlerInterceptor 내부에서 포워드 요청을 구분하는 방법**

서블릿 3.0 이상에서 `HttpServletRequest#getDispatcherType()`을 사용할 수 있습니다.

인터셉터 코드 내부에서 직접 구분할 수 있습니다:

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

    if (request.getDispatcherType() != DispatcherType.REQUEST) {
        // FORWARD, ERROR 시에는 처리하지 않음
        return true;
    }

    // 최초 요청일 때만 처리
    return true;
}
```

스프링은 DispatcherType을 설정할 수 없지만
**DispatcherType은 언제나 HttpServletRequest 내부에 존재**하므로
이를 직접 읽는 방식입니다.

---

# 정리

| 방법                                    | 설명                                           | 실무 빈도 |
| ------------------------------------- | -------------------------------------------- | ----- |
| **1. excludePathPatterns("/error")**  | 가장 간단한 해결책. 오류 페이지 요청 자체를 인터셉터에서 빼버림         | 매우 높음 |
| **2. 필터에서 최초 요청 여부 attribute 전달**     | DispatcherType을 활용하여 최초 요청만 인터셉터에 특수 처리하도록 함 | 중간    |
| **3. 인터셉터 내부에서 직접 DispatcherType 체크** | 인터셉터 내부에서 FORWARD/ERROR를 구분하여 처리             | 중간    |

---

# 결론

스프링 인터셉터는 서블릿 필터처럼 DispatcherType을 설정할 수 없기 때문에,
**가장 일반적으로 사용하는 해결책은 오류 경로(`/error`)를 `excludePathPatterns()`로 제외하는 방식**입니다.

필요하다면 추가로 `DispatcherType` 체크를 조합해 더 견고하게 만들기도 합니다.

스프링 부트는 **사용자 정의 오류 페이지 HTML 템플릿**을 찾을 때 **정해진 우선순위 경로**를 따라 자동으로 탐색합니다.
오류 코드별(404, 500 등), 상태별(4xx, 5xx), 기본 파일 순서로 찾습니다.

---

# 오류 페이지 템플릿 탐색 우선순위

스프링 부트는 다음 순서대로 템플릿을 찾습니다.

## 1. **정확히 일치하는 오류 코드 템플릿**

예: `src/main/resources/templates/error/404.html`

이 경로가 최우선입니다.

## 2. **범위 템플릿 (`4xx.html`, `5xx.html`)**

예:
`src/main/resources/templates/error/4xx.html`
`src/main/resources/templates/error/5xx.html`

정확한 오류 코드 파일이 없을 때 사용됩니다.

## 3. **기본 오류 템플릿 (`error.html`)**

예:
`src/main/resources/templates/error/error.html`

4xx/5xx도 없을 때 마지막으로 사용됩니다.

---

# 실제 경로 우선순위 (템플릿 기반: Thymeleaf, Mustache 등)

스프링 부트는 다음 경로들을 자동 탐색합니다.

### 1) 템플릿 경로

```
classpath:/templates/error/<status>.html
classpath:/templates/error/4xx.html
classpath:/templates/error/5xx.html
classpath:/templates/error/error.html
```

### 2) 정적 리소스 경로

```
classpath:/static/error/<status>.html
classpath:/static/error/error.html
```

### 3) `META-INF/resources`

```
classpath:/META-INF/resources/error/<status>.html
```

※ 템플릿 경로가 정적 리소스보다 우선합니다.

---

# 전체 우선순위 정리 (정확한 순서)

스프링 부트(내장 `DefaultErrorViewResolver`) 기준:

1. **`templates/error/{status}.html`**
2. **`templates/error/{series}.html`** (`4xx`, `5xx`)
3. **`templates/error/error.html`**
4. **`static/error/{status}.html`**
5. **`static/error/error.html`**
6. **`META-INF/resources/error/{status}.html`**

※ 어떤 것도 없으면 기본 Whitelabel Error Page 또는 JSON 오류 응답 사용.

---

# 예시로 보면

예를 들어 404 Not Found가 발생하면 다음 순서로 찾습니다:

1. `templates/error/404.html`
2. `templates/error/4xx.html`
3. `templates/error/error.html`
4. `static/error/404.html`
5. `static/error/error.html`
6. `META-INF/resources/error/404.html`

여기까지 없으면 기본 스프링 오류 JSON 또는 Whitelabel 에러 페이지가 나옵니다.

---

# 결론

스프링 부트는 사용자 정의 오류 페이지 HTML을 찾을 때 다음 규칙으로 우선 탐색합니다.

1. **정확한 상태 코드 템플릿**
2. **상태 코드 범위 템플릿 (`4xx`, `5xx`)**
3. **기본 템플릿 (`error.html`)**
4. **정적 리소스의 동일 구조**
5. **그 외 META-INF**

필요하면 템플릿과 정적 파일을 상황에 맞게 배치하면 됩니다.

스프링 부트가 **사용자 정의 오류 페이지**를 찾을 때,
`static resources`, `view templates`, `BasicErrorController`, `application.properties` 중에서
**무엇을 먼저 탐색하는지** 우선순위를 정확히 설명해줄게.

---

# 오류 페이지 탐색의 실제 우선순위

스프링 부트는 오류가 발생하면 아래 순서대로 처리합니다.

---

# 1. **application.properties 설정 확인**

먼저, 스프링 부트는 오류 페이지의 기본 동작을 구성하는 프로퍼티를 확인합니다.

예:

```
server.error.whitelabel.enabled
server.error.include-message
server.error.path
```

즉, **동작 방식(경로/옵션)을 먼저 설정으로 결정**합니다.
하지만 **오류 페이지 파일을 탐색하는 단계는 아님**.

---

# 2. **BasicErrorController 실행**

프로퍼티로 구성된 내용을 기반으로
스프링 부트는 먼저 **BasicErrorController를 통해 오류 요청을 처리하려고 시도**합니다.

`BasicErrorController`는 이렇게 동작합니다.

* 모델에 오류 정보 담기
* 적절한 **ViewResolver**에게 “오류 페이지 뷰를 찾으라” 요청

여기서 실제로 템플릿/정적 파일 탐색이 일어남.

---

# 3. **ViewResolver → 템플릿(view templates) 먼저 탐색**

뷰 기반 오류 페이지가 있다면 가장 먼저 템플릿을 찾습니다.

예:
`classpath:/templates/error/404.html`
`classpath:/templates/error/4xx.html`
`classpath:/templates/error/error.html`

**템플릿이 있다면 이게 최우선이며, 정적 리소스보다 우선**합니다.

---

# 4. **템플릿이 없으면 정적 리소스(static resources) 탐색**

템플릿이 없으면 다음 순서로 정적 파일을 찾습니다.

예:
`classpath:/static/error/404.html`
`classpath:/static/error/error.html`

---

# 5. **둘 다 없으면 BasicErrorController의 기본 JSON 또는 Whitelabel Error Page 반환**

* 템플릿 없음
* 정적 HTML 없음
* 이러한 경우 기본 에러 응답(JSON) 또는 Whitelabel(비활성화되지 않은 경우)을 사용합니다.

---

# 종합 우선순위

사용자 정의 **파일**을 찾는 우선순위는 다음과 같음:

1. **View templates (`templates/error/...html`)**
2. **Static resources (`static/error/...html`)**
3. **기본 오류 처리 (`BasicErrorController` 기본 렌더링)**

이 전체 흐름은
`application.properties` → `BasicErrorController` → 템플릿 → 정적 파일 → 기본 응답
으로 진행됨.

---

# 결론적으로 질문에 정확히 대답하면

**오류 페이지 파일 탐색 우선순위는 다음과 같습니다.**

1. **view templates**
2. **static resources**
3. **BasicErrorController 기본 응답**

`application.properties`는 탐색 순서가 아니라
**BasicErrorController가 어떤 경로/형식으로 처리할지를 결정하는 설정 단계**입니다.

---

필요하면 이 흐름을 그림으로도 정리해 줄게.
