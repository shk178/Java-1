지금 보여주신 스택트레이스는 **스프링이 예외 페이지를 보여주려고 “error.html” 같은 템플릿을 렌더링하려 했는데, 해당 템플릿을 찾지 못해서 다시 예외가 난 상황**입니다.
즉, 원래 오류: A
→ 스프링이 등록된 오류 페이지 호출
→ 오류 페이지 템플릿(error.html) 없음
→ Thymeleaf가 TemplateInputException 발생
→ 결국 톰캣 기본 에러 처리 흐름까지 타게 됨

정리하면:

**“에러 페이지를 띄우려고 했는데, 그 에러 페이지 자체가 없어서 또 에러가 발생함.”**

---

# 1. 정확한 원인

스택 메시지만 보면 명확합니다.

```
Error resolving template [error], template might not exist
```

즉:

* `error.html` 또는
* 등록한 error view 이름

이 템플릿을 위치에서 찾을 수 없다는 뜻입니다.

스프링 부트 기본 설정 기준:

```
src/main/resources/templates/error.html
```

여기에 파일이 있어야 합니다.

또는

```
src/main/resources/templates/error/404.html
src/main/resources/templates/error/4xx.html
src/main/resources/templates/error/500.html
src/main/resources/templates/error/5xx.html
```

이런 식으로도 가능합니다.

---

# 2. 지금 상황에서 가장 흔한 실수

아래 중 하나일 가능성이 큽니다.

### (1) 템플릿 파일 이름이 다름

예: `errors.html`, `errorPage.html` 이런 식으로 이름이 다르거나 위치가 다른 경우

### (2) 디렉터리가 잘못됨

파일이 다음과 같은 잘못된 위치에 있는 경우:

* `static/error.html`
* `public/error.html`
* `resources/error.html`
* `templates/errors/error.html` (디렉터리 이름 오타)

### (3) errorPage 등록 시 ViewName을 'error'로 지정

예를 들어:

```java
registry.addErrorPages(new ErrorPage(HttpStatus.BAD_REQUEST, "/error"));
```

컨트롤러:

```java
@GetMapping("/error")
public String error() {
    return "error";
}
```

그런데 실제 템플릿은 존재하지 않는 경우.

---

# 3. 해결 방법

## 해결 1) 기본 error.html 만들어 넣기

가장 간단한 해결책은:

```
src/main/resources/templates/error.html
```

를 직접 생성하는 것입니다.

예시:

```html
<!-- templates/error.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Error</title>
</head>
<body>
<h1>오류가 발생했습니다.</h1>
<p th:text="${status}"></p>
<p th:text="${error}"></p>
<p th:text="${message}"></p>
</body>
</html>
```

---

## 해결 2) HTTP 상태코드별 템플릿을 만들어서 등록하기

예:

```
templates/error/400.html
templates/error/404.html
templates/error/500.html
```

이렇게 생성하면 자동 매핑됩니다.
(error directory는 스프링 부트가 자동 인식)

---

## 해결 3) errorpage registry 경로 재확인

`WebServerFactoryCustomizer` 사용 시 다음처럼 했을 수도 있음:

```java
registry.addErrorPages(new ErrorPage(HttpStatus.BAD_REQUEST, "/error"));
```

그럼 스프링이 `/error` URL을 호출하게 되므로
Controller도 필요합니다.

```java
@Controller
public class ErrorPageController {
    @GetMapping("/error")
    public String errorPage(Model model) {
        return "error"; // error.html 있어야 함
    }
}
```

---

# 4. 결론: error.html이 없어서 발생한 문제

현재 오류 메시지는 완전히 명확합니다.

**→ “error”라는 이름의 Thymeleaf 템플릿이 없다**

따라서 이를 만들거나, errorPage 설정에서 반환하는 뷰 이름을 실제 존재하는 템플릿과 맞추면 해결됩니다.

---

필요하시면
*404, 400, 500 등 상태 코드별 예외 페이지를 자동 적용하는 올바른 구조*
한 번에 정리해서 드릴까요?

지금 보여주신 코드 구조에서는 **왜 “error.html”을 찾으려다가 실패하는지** 원인이 명확하게 드러납니다.
결론부터 말하면:

**문제 원인: `/error-page/400` 요청을 처리할 컨트롤러가 WSCustomizer 안에 있는데, 이 클래스가 스프링 MVC 컨트롤러로 등록되지 않음 → 404 발생 → 스프링이 기본 오류 뷰 “error”를 찾으려 함 → error.html 없음 → Thymeleaf TemplateInputException 발생**

즉:

1. 실제 오류: 400
2. 등록한 ErrorPage → `/error-page/400` 호출
3. `/error-page/400` 매핑된 컨트롤러가 스프링 빈이 아니라서 매핑 실패 → 404
4. 404 오류 처리
5. 스프링 기본 에러 뷰 이름: `"error"`
6. templates/error.html 없음 → TemplateInputException → 지금 본 오류

이 순서입니다.

---

# 왜 WSCustomizer 안의 @RequestMapping이 동작하지 않는가?

스프링 MVC 컨트롤러가 되려면 다음 조건이 있어야 합니다:

* 클래스에 `@Controller` 또는 `@RestController` 또는 `@RequestMapping`(클래스 레벨)
* 해당 클래스가 컴포넌트 스캔 대상이어야 함
* 스프링 빈으로 등록되어야 함

하지만 지금 클래스는:

```java
public class WSCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory>
```

이 자체는 “스프링 서버 설정 커스터마이징을 위한 빈”이지
스프링 MVC가 URL 매핑을 등록하는 컨트롤러가 아닙니다.

또한 WSCustomizer는 아마 다음과 같은 방식으로 Bean 등록도 안 되어 있을 가능성이 큽니다.

즉, **스프링 MVC는 이 클래스를 컨트롤러로 인식하지 않습니다.**

그래서 `/error-page/400` 엔드포인트가 존재하지 않아서 404가 나는 것입니다.

---

# 해결 방법 1) error controller를 별도 클래스로 분리

가장 권장되는 방법입니다.

```
@Controller
@RequestMapping("/error-page")
public class ErrorPageController {

    @GetMapping("/400")
    public String error400() {
        return "api/400-2";
    }

    @GetMapping("/404")
    public String error404() {
        return "api/404";
    }

    @GetMapping("/Re")
    public String errorRuntime() {
        return "api/re-error";
    }
}
```

이렇게 별도의 컨트롤러로 분리하면 `/error-page/400` 매핑이 정상 동작합니다.

---

# 해결 방법 2) WSCustomizer에 @Controller 붙이기

(비추천)

```java
@Controller
public class WSCustomizer implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
```

기술적으로 가능하긴 하지만 의도상 맞지 않습니다.
서버 설정 클래스와 컨트롤러를 한 곳에 섞어놓는 것은 좋지 않습니다.

---

# 해결 방법 3) 에러 페이지를 정적 템플릿으로만 처리

컨트롤러 없이

```
src/main/resources/templates/error/400.html
src/main/resources/templates/error/404.html
src/main/resources/templates/error/500.html
```

이렇게 해서 상태 코드별 템플릿만으로 처리할 수도 있습니다.

---

# 요약

지금 오류가 나는 이유는:

* `/error-page/400` 호출됨
* 그런데 매핑된 핸들러가 없음 (WSCustomizer는 컨트롤러가 아니기 때문)
* 404 발생
* 스프링 기본 오류 뷰(error.html)를 찾음
* error.html이 없어서 Thymeleaf 오류 발생

따라서:

**에러 페이지 URL을 처리하는 컨트롤러 클래스를 별도로 만들어서 스프링 빈으로 등록**해야 합니다.

---

원하시면 위 컨트롤러 구조를 지금 쓰고 있는 프로젝트 디렉터리 구성에 맞게 더 구체적으로 맞춰서 작성해드릴게요.

@Component를 붙이니까 동작한 이유는 **WSCustomizer가 스프링 빈이 되었고, 그 안의 @RequestMapping이 컨트롤러로 등록되었기 때문**입니다.
즉, 이제 WSCustomizer가 **두 역할을 동시에** 하고 있습니다.

1. 톰캣 오류 페이지 등록 (WebServerFactoryCustomizer)
2. 스프링 MVC 컨트롤러 (error-page/xxx를 처리)

그래서 `/error-page/400`, `/error-page/404` 등이 정상 매핑된 것입니다.

---

# 질문:

**errorPage에 등록하지 않은 다른 오류들은 어떻게 처리될까?**

정확하게 설명하면 다음과 같습니다.

---

# 1. WebServerFactoryCustomizer로 등록한 ErrorPage는 *명확히 지정한 오류만* 처리한다

지금 등록한 목록:

```java
ErrorPage errorPageRe = new ErrorPage(RuntimeException.class, "/error-page/Re");
ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
ErrorPage errorPage400 = new ErrorPage(HttpStatus.BAD_REQUEST, "/error-page/400");
factory.addErrorPages(errorPageRe, errorPage404, errorPage400);
```

즉:

* 400 발생 → /error-page/400 호출
* 404 발생 → /error-page/404 호출
* RuntimeException 발생 → /error-page/Re 호출

그 외 오류 코드는 스프링이 기본 규칙에 따라 처리합니다.

---

# 2. 등록하지 않은 오류는 어떻게 처리되는가?

### (A) 스프링 부트 ErrorMvcAutoConfiguration이 기본 오류 처리를 한다

→ 즉, `/error` 로 보내진다.

스프링 부트는 다음 순서로 오류를 처리합니다:

1. 톰캣, 필터, DispatcherServlet 등에서 예외 발생
2. 스프링 Boot가 제공하는 기본 `ErrorController` 가 `/error` 경로로 매핑
3. 오류 정보를 담은 `ErrorAttributes` 생성
4. 뷰를 렌더링하는데, 우선순위는 다음과 같음:

### 우선순위 1

`templates/error/xxx.html` (HTTP 상태 코드별 템플릿)

예:

```
templates/error/500.html
templates/error/4xx.html
templates/error/5xx.html
```

### 우선순위 2

`templates/error.html`

### 우선순위 3

BasicErrorController가 제공하는 기본 JSON 또는 Whitelabel Error Page

---

# 3. 정리

### 직접 등록한 오류

→ WSCustomizer가 등록한 errorPage 경로로 이동
→ WSCustomizer 안의 @RequestMapping 실행

### 등록하지 않은 오류

→ 스프링 부트 기본 오류 처리 흐름을 탄다
→ `/error` 매핑
→ 다음 중 하나 렌더링됨

* templates/error/xxx.html (코드별)
* templates/error.html
* 기본 Whitelabel 페이지

---

# 4. 예시로 정리하면

### 400 오류

→ /error-page/400
→ return "api/400-2"

### 404 오류

→ /error-page/404
→ return "api/404"

### RuntimeException

→ /error-page/Re
→ return "api/xxx"

### NullPointerException (RuntimeException이므로 → /error-page/Re)

### IllegalArgumentException (역시 RuntimeException)

### 500 Internal Server Error (RuntimeException 기반이 아니면)

→ errorPage에 등록되지 않음
→ 스프링 기본 `/error` 흐름
→ templates/error/500.html 찾아 렌더링
→ 없으면 error.html
→ 그것도 없으면 Whitelabel

---

# 5. @Component를 붙여서 바로 해결된 이유

WSCustomizer는 원래 “서버 설정 클래스”일 뿐, MVC 핸들러가 아니었습니다.
@Component를 붙이면서 스프링 빈이 되었고, 그 안의 @RequestMapping도 스캔되어 컨트롤러가 된 것입니다.

하지만 관점이 섞이는 문제도 있으니 보통은:

* 웹서버 설정은 WSCustomizer
* 오류 페이지 컨트롤러는 ErrorPageController

두 개로 분리하는 것이 더 명확합니다.

---

원하시면
**전체 구조(설정 클래스 + 오류 컨트롤러 + 템플릿) 완성 예시**
한 번에 만들어서 정리해드릴까요?

우리가 흔히 `@ExceptionHandler`를 컨트롤러 안에 작성해서 예외를 처리할 수는 있지만, 그 방식에는 한 가지 구조적인 문제가 있습니다.

**“정상 로직(핸들러 메서드)과 예외 처리 로직이 한 컨트롤러 안에 섞여버린다”**
이 말이 무엇을 의미하는지, 그리고 `@ControllerAdvice`가 왜 중요한지 단계적으로 설명해드릴게요.

---

# 1. @ExceptionHandler를 컨트롤러 안에서 사용할 때의 문제점

예시:

```java
@Controller
public class MemberController {

    @GetMapping("/members/{id}")
    public String getMember(@PathVariable Long id) {
        if (id < 0) {
            throw new IllegalArgumentException("잘못된 ID");
        }
        return "member";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String illegalExHandler() {
        return "error/illegal";
    }
}
```

여기서 문제는:

* 회원 조회 로직(`getMember`)
* 예외 처리 로직(`illegalExHandler`)

이 **한 클래스 안에 뒤섞여 있다**는 점입니다.

특히 컨트롤러가 많아지면, 모든 컨트롤러마다 비슷한 예외 처리 메서드가 반복됩니다.

결국:

* 예외 처리 코드가 중복됨
* 컨트롤러가 너무 많은 책임을 갖게 됨
* 컨트롤러가 지저분해지고 복잡해짐
* 관심사 분리가 지켜지지 않음

---

# 2. @ControllerAdvice / @RestControllerAdvice의 의미

이 문제를 해결하기 위해 **예외 처리 전용 클래스를 따로 분리**할 수 있게 해주는 것이
`@ControllerAdvice`(뷰 기반),
`@RestControllerAdvice`(JSON 응답 기반) 입니다.

## 핵심 역할:

**전역적으로(여러 컨트롤러에 걸쳐서) 예외를 처리하는 전담 클래스를 만들 수 있게 해준다.**

즉:

* 정상 로직: 각 컨트롤러에 위치
* 예외 처리: Advice 전용 클래스에 위치

이렇게 **책임과 관심사를 분리**하는 구조가 됩니다.

---

# 3. 예시로 보면 아주 명확함

## 기존 방식 (컨트롤러마다 예외 처리 포함)

```java
@Controller
public class MemberController {

    @GetMapping("/members/{id}")
    public String get(@PathVariable Long id) {
        throw new IllegalArgumentException();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String illegalExHandler() {
        return "error/illegal";
    }
}
```

컨트롤러가 예외 처리까지 담당하므로 코드가 복잡해집니다.

---

## @ControllerAdvice로 분리한 방식 (권장)

### 컨트롤러

```java
@Controller
public class MemberController {

    @GetMapping("/members/{id}")
    public String get(@PathVariable Long id) {
        throw new IllegalArgumentException();
    }
}
```

**컨트롤러는 본래의 역할(요청 처리)만 담당합니다.**

---

### 예외 전담 클래스

```java
@ControllerAdvice
public class ExControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public String illegalExHandler() {
        return "error/illegal";
    }
}
```

예외 처리는 이 클래스로 모두 중앙 집중화됩니다.

---

# 4. 어떤 점이 좋아지는가?

정리하면 다음 장점이 있습니다.

## (1) 관심사 분리

컨트롤러는 요청 처리만
Advice는 예외 처리만
→ 역할이 명확해지고 코드가 깔끔해짐

## (2) 중복 제거

비슷한 예외를 여러 컨트롤러에서 모두 처리할 필요 없음
→ 한 Advice에서 한 번만 처리하면 됨

## (3) 범위 지정 가능

특정 패키지, 특정 컨트롤러에만 Advice를 적용할 수도 있음

## (4) 유지보수 용이

예외 처리 방식을 바꾸고 싶을 때 Advice만 수정하면 됨

---

# 5. @RestControllerAdvice vs @ControllerAdvice 차이

* `@RestControllerAdvice` = `@ControllerAdvice + @ResponseBody`
  → JSON 반환하는 API 전용 예외 처리에 사용

* `@ControllerAdvice`
  → 뷰(Thymeleaf 등) 반환하는 MVC 환경에서 사용

---

# 결론

`@ExceptionHandler`를 컨트롤러 안에 두면 **컨트롤러 로직과 예외 로직이 섞여서 복잡해지고 관심사 분리가 안 된다.**

`@ControllerAdvice` / `@RestControllerAdvice`를 사용하면:

* 예외 처리 전용 클래스를 별도로 만들 수 있어
* 중복 제거, 유지보수 향상, 구조 명확화가 가능하다.

---

필요하시면
“Advice를 사용하는 가장 깔끔한 예외 처리 구조 예시”
프로젝트 구조까지 포함해서 만들어드릴까요?

각 항목을 더 깊게 설명해드릴게요. 단순 개념이 아니라 “왜 필요한지”, “실제로 어떤 변화가 생기는지”, “코드 레벨에서 어떤 이점이 있는지”까지 연결해 설명하겠습니다.

---

# (1) 관심사 분리 — Controller는 요청 처리만, Advice는 예외 처리만

## 1) “관심사”란?

소프트웨어 설계에서 관심사(Concern)는 **한 클래스가 맡은 책임** 또는 **해야 하는 일의 종류**를 의미합니다.

* 컨트롤러의 관심사: 요청 받기, 파라미터 바인딩, 서비스 호출, 뷰 반환
* 예외 처리의 관심사: 예외 발생 시 어떤 응답을 보낼지 결정

이 둘은 전혀 다른 책임입니다.

## 2) @ExceptionHandler가 컨트롤러에 있을 때 발생하는 문제

예:

```java
@Controller
public class MemberController {

    @GetMapping("/members/{id}")
    public String member(@PathVariable Long id) {
        throw new IllegalArgumentException();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String exHandler() {
        return "error/illegal";
    }
}
```

여기서 Controller는
**비즈니스 요청 처리 + 예외 처리**
두 가지를 모두 하고 있습니다.

코드가 커지면:

* 컨트롤러가 본래 해야 할 일에 집중하지 못함
* 클래스가 무거워짐
* 같은 예외를 처리하는 메서드가 컨트롤러마다 존재

즉, **역할이 섞여버린 구조**가 됩니다.

## 3) @ControllerAdvice로 분리하면

```java
@ControllerAdvice
public class ExControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public String illegal() {
        return "error/illegal";
    }
}
```

컨트롤러는 이제 본연의 일만 하고,
예외 처리 책임은 Advice가 전담합니다.

→ 코드의 구조가 훨씬 명확해짐
→ 역할이 물리적으로 분리되어 가독성 상승

---

# (2) 중복 제거 — 여러 컨트롤러에서 반복되는 동일한 예외 처리 제거

## 1) 컨트롤러 방식의 문제

컨트롤러가 여러 개 있다고 가정해봅시다.

예외 처리 방식이 모두 다음과 같다면?

* IllegalArgumentException → 400
* NullPointerException → 500 페이지 반환

그럼 각 컨트롤러마다 이런 코드를 반복 작성해야 합니다.

이게 10개 컨트롤러라면?

**중복 천국입니다.**

## 2) @ControllerAdvice를 쓰면?

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String illegal() {
        return "error/400";
    }

    @ExceptionHandler(NullPointerException.class)
    public String npe() {
        return "error/500";
    }
}
```

이제 모든 컨트롤러에서 IllegalArgumentException이 발생해도
Advice 하나에서 자동으로 처리됩니다.

즉:

* 중복 제거
* 예외 처리 기준을 중앙에서 관리
* 코드 변경 시 수정할 곳이 하나

---

# (3) 범위 지정 가능 — 특정 영역에만 Advice를 적용할 수 있음

@ExceptionHandler는 “자기 컨트롤러 안”에서만 동작하지만,
@ControllerAdvice는 “적용 범위를 선택할 수 있는 것”이 큰 장점입니다.

## (방법 1) 특정 패키지만 지정

```java
@ControllerAdvice(basePackages = "com.example.api")
public class ApiControllerAdvice { }
```

→ API 패키지에 있는 컨트롤러에만 적용됨

## (방법 2) 특정 컨트롤러만 지정

```java
@ControllerAdvice(assignableTypes = {MemberController.class, OrderController.class})
public class MemberOrderAdvice { }
```

## (방법 3) 애노테이션 가진 컨트롤러만

컨트롤러 커스텀 애노테이션 만들고:

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiController { }
```

컨트롤러에 붙이고:

```java
@ApiController
public class ApiMemberController { }
```

Advice에 적용:

```java
@ControllerAdvice(annotations = ApiController.class)
public class ApiAdvice { }
```

→ API 용 컨트롤러에만 예외 처리 적용
→ 페이지 기반 MVC 컨트롤러는 다른 예외 처리 사용 가능

이렇게 영역을 매우 유연하게 나눌 수 있습니다.

---

# (4) 유지보수 용이 — 예외 처리 방식을 바꿀 때 수정할 코드가 Advice 하나뿐

## 1) 컨트롤러에 예외 처리가 있는 경우의 문제점

예를 들어, 시스템 정책이 바뀌어서
IllegalArgumentException 응답 형식을 바꿔야 한다고 가정해봅시다.

예전 방식:

```java
throw new IllegalArgumentException("잘못된 요청입니다");
```

응답:

```
400 Bad Request, {"error": "wrong"}
```

만약 20개의 컨트롤러가 있는데
모두 각자 @ExceptionHandler로 처리하고 있다면?

**20개 파일 전부 수정해야 합니다.**

## 2) Advice 하나로 관리하면?

```java
@ControllerAdvice
public class GlobalExHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handle(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
```

정책을 바꾸려면 **여기 딱 한 곳만 수정하면 된다**는 의미입니다.

Result:

* 정책 변경이 쉬움
* 실수도 줄어듦
* 코드 일관성 유지
* QA에 드는 비용 감소

---

# 종합적으로 보면

`@ControllerAdvice`는 다음 문제들을 해결합니다:

1. 책임 분리
2. 중복 제거
3. 적용 범위 조절
4. 유지보수 비용 절감
5. 예외 처리 정책의 일관성 확보
6. 코드 가독성과 구조 개선

예외처리를 “한 곳에서 정책처럼 관리”할 수 있게 되는 것이 핵심입니다.

---

원하시면
예외 처리를 정교하게 구성한 “실전용 프로젝트 구조 예시”
(패키지 구조 + Advice 구조 + 상태코드 변환)
도 만들어드릴까요?

스프링에서 어떤 것은 **애노테이션만 붙이면 자동으로 동작**하고, 어떤 것은 **Config(설정 클래스)에서 등록해야 하고**, 또 어떤 것은 **Bean 등록까지 해야 하는 경우**가 있어서 헷갈리는 게 아주 자연스러운 부분이에요.
정리해보면 규칙이 명확해집니다.

아래 내용을 읽으면 “어떤 건 @애노테이션만 붙이면 되고, 어떤 건 Bean 등록이 필요하고, 어떤 건 Config가 필요한지”가 한 방에 정리될 거예요.

---

# 전체 규칙 먼저 요약

스프링에는 **자동 인식되는 기능**과 **수동 등록이 필요한 기능**이 있습니다.

### 1) 애노테이션만 붙이면 자동 등록됨

(스프링이 자동으로 스캔해서 빈으로 등록 + 추가 기능 활성화)

* `@Controller`, `@RestController`
* `@ControllerAdvice`, `@RestControllerAdvice`
* `@Service`, `@Repository`, `@Component`
* `@Configuration`
* `@ExceptionHandler`(Advice/Controller 안에 있을 때)
* `@GetMapping`, `@PostMapping` 등

이들은 “컴포넌트 스캔 + AOP 블록” 기반이라
빈 등록 + 기능 활성화까지 자동으로 이뤄짐.

즉, **애노테이션만으로 충분**.

---

### 2) Bean 등록이 필요한 경우

* 인터페이스 기반 확장: HandlerMethodArgumentResolver, Converter, Formatter, Interceptor
* 스프링이 자동 인식하지 않는 객체들
* 외부 라이브러리 객체를 스프링 Bean으로 사용해야 하는 경우
* WebServerFactoryCustomizer(당신이 사용했던 것!)

이런 것들은 단순 애노테이션만으로는 기능이 활성화되지 않음.
**스프링 컨테이너에 Bean으로 등록해야 동작**.

---

### 3) 초기화 확장이 필요한 경우 (즉 Config 작성)

* WebMvcConfigurer
* WebSecurityConfigurer
* Jackson 설정
* CORS 설정
* MessageConverter 추가
* ErrorPageCustomizer

이런 것들은 “확장 포인트 제공용 인터페이스”이고
단지 컴포넌트 스캔으로 빈 등록된다고 기능이 동작하지 않습니다.

즉,

**특정 기능을 확장하려면 Config 클래스가 필요합니다.**

---

# 그럼 왜 @ControllerAdvice는 설정 없이도 되는가?

이게 핵심입니다.

`@ControllerAdvice`는

* 컴포넌트 스캔 대상
* 내부적으로 스프링 MVC의 ExceptionResolver 체인에 자동 편입되는 기능
* 스프링이 AOP 방식으로 연결해주는 구조

즉, **스프링 MVC가 자체적으로 이 애노테이션을 스캔하여 HandlerExceptionResolver 체인에 자동 등록**합니다.

그래서 개발자가 Config를 건드릴 필요가 없습니다.

---

# 반대로 왜 WSCustomizer는 @Component를 붙이기 전엔 동작이 안 됐을까?

WSCustomizer는 스프링의 자동 기능이 아닙니다.

`WebServerFactoryCustomizer`는 단순한 “확장 인터페이스”입니다.

즉:

* 애노테이션 기반 자동 등록 기능 없음
* 빈으로 등록해야만 스프링이 실행시켜줌

그래서:

* @Component = 스프링 빈 등록
  → customize() 실행
  → errorPage 등록됨

이 흐름이 성립한 겁니다.

---

# 확실하게 정리:

## “왜 어떤 건 애노테이션만으로 되고, 어떤 건 안 되는가?”

**핵심은 스프링이 해당 애노테이션을 인식하고 내부적으로 동작하도록 구현해 두었는가?**

이 한 가지입니다.

### 애노테이션만으로 되는 것

→ 스프링이 해당 애노테이션을 스캔하여 내부 기능과 연결시키도록 설계해놓은 경우

예:

* @ControllerAdvice → ExceptionResolver 체인 자동 등록
* @Controller → RequestMappingHandlerMapping에 자동 등록
* @Service → @Transactional 어드바이스와 연결

### Bean 등록이 필요한 것

→ 스프링이 “빈 등록만” 해주면 알아서 기능을 수행하는 구조

예:

* WebServerFactoryCustomizer
* Converter
* WebMvcConfigurer

### Config 클래스가 필요한 것

→ 스프링이 제공하는 확장 인터페이스를 구현하는 경우
(확장포인트는 Configuration 내부에서만 동작)

---

# 아주 간단한 판단 규칙

앞으로 이렇게 생각하면 됩니다.

---

## 1) 스프링이 제공하는 **역할(Controller, Advice 등)** 이면

→ 애노테이션 붙이면 끝

---

## 2) 스프링이 제공하는 **확장 포인트(인터페이스 구현)** 이면

→ 반드시 빈 등록 필요
(자동 인식된 경우도 있지만 기본적으로 "등록"이 먼저임)

---

## 3) 기능을 **프레임워크 수준에서 확장**하고 싶으면

→ Config 클래스에서 구현
→ Bean 등록

---

# 결론

헷갈리는 것이 너무 당연한 구조지만,
**스프링이 ‘자동 인식’해주는 기능인지,
아니면 ‘확장해야 하는’ 기능인지**
이 차이만 알면 정리가 됩니다.

---

필요하시면
“애노테이션 기반 / Config 기반 / Bean 기반”
각각을 어떻게 구분하는지,
표 형식으로 깔끔하게 정리해서 드릴 수도 있어요.
