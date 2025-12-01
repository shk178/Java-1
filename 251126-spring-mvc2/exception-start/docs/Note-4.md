아래 내용을 단계별로 정리해 드릴게요. 핵심은 **Spring Boot의 기본 에러 페이지 매핑 규칙** 때문입니다.

---

# 1. 왜 *직접 매핑하지 않은* 404.html이 실행되는가?

Spring Boot는 다음과 같은 **기본 에러 처리 흐름**을 가지고 있습니다.

### (1) 에러 발생

* 컨트롤러에서 `response.sendError(404)` 호출하거나
* 존재하지 않는 URL에 접근하면
  → **DispatcherServlet → BasicErrorController** 에게 404 에러가 전달됨.

### (2) BasicErrorController가 에러 페이지를 찾는 규칙

Spring Boot는 다음 우선순위로 에러 페이지를 찾습니다.

1. **WebServerFactoryCustomizer 에 등록된 ErrorPage**

    * 예: `new ErrorPage(404, "/error-page/404")`

2. **/error/**default paths 처리**
   만약 ErrorPage로 매핑된 컨트롤러가 없다면
   `/error/404` 등의 경로를 처리하려고 시도함.

3. **템플릿 / 정적 리소스의 에러 파일 검색**

    * `/resources/templates/error/404.html`
    * `/resources/static/error/404.html`
    * `/public/error/404.html`
    * 이런 위치에 있는 파일들을 자동으로 로드함.

즉, **특수 규칙으로 error/코드.html이 자동 인식됨.**

---

# 2. 지금 상황에서 무슨 일이 일어난 건가?

당신의 코드에서는 아래처럼 등록함:

```java
ErrorPage errorPageRe = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/Re");
ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
factory.addErrorPages(errorPageRe, errorPage404);
```

그런데 여기에는 **충돌 문제가 있다**는 점이 중요합니다.

### 문제 1

두 ErrorPage 모두 HttpStatus.NOT_FOUND(404)로 등록됨
→ 결국 마지막으로 등록한 `/error-page/404` 만 적용되는 경우가 많음.

즉, 사실상:

* 404 발생 → `/error-page/404`로 포워딩됨.

그런데 만약 `/error-page/404` 매핑이 존재하지 않거나 처리 실패하면?

→ Spring Boot는 자동으로 `/error/404.html` 같은 기본 파일을 찾음.

그래서 **templates/error/404.html** 또는 **static/error/404.html**에 있는 파일이 실행됨.

---

# 3. 결론

> ❗ **직접 /error-page/404 요청을 하지 않아도 404.html이 실행되는 이유는?**
> Spring Boot가 내장한 기본 에러 처리 규칙 때문에,
> 특정 URL로 매핑되지 않은 404 에러가 발생하면
> `/error/404.html` 또는 기본 제공 에러 정보를 자동으로 렌더링하기 때문이다.

즉, **404.html은 “기본 에러 페이지”로 자동 인식되는 특수 파일**이다.

---

# 4. 해결하고 싶다면?

### 커스텀 404 페이지를 강제하고 싶으면

방법 1. ErrorPage를 404 하나만 등록

```java
ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
factory.addErrorPages(errorPage404);
```

방법 2. templates/error/404.html 파일을 삭제하거나 이름 변경

방법 3. ErrorPageController에서 `/error` 자체를 오버라이드

```java
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String error() {
        return "404"; // 템플릿 이름
    }
}
```

---

필요하시면 프로젝트 구조를 기반으로 어떤 에러 페이지 매핑이 최적으로 동작하는지 구체적인 구성도 정리해드릴게요.

결론부터 말하면 **맞다.**
지금처럼 `RuntimeException`이 터져도 **서버가 멈추지 않는 이유는 스프링 MVC와 톰캣(서블릿 컨테이너)이 해당 예외를 정상적인 “요청 처리 실패”로 취급하며, 애플리케이션 전체를 죽이지 않기 때문이다.**

아래에서 흐름을 정확히 설명해줄게.

---

# 1. RuntimeException이 발생해도 서버가 종료되지 않는 이유

### 1) Spring MVC는 컨트롤러 내부에서 던져진 예외를 "표준 흐름"으로 처리

컨트롤러에서 예외가 발생하면:

```
DispatcherServlet → HandlerAdapter → InvocableHandlerMethod → 컨트롤러 메서드
```

여기서 예외를 catch하여 **오류 처리를 DispatcherServlet에게 넘긴다.**

즉, 이 에러는 서버를 죽이는 치명적 예외가 아니라:

* “요청 처리 중 오류 발생”
* “하지만 서버는 계속 실행 가능”

이라는 흐름으로 분류된다.

---

# 2. 예외는 Spring의 Error Handling 흐름으로 전달됨

예외 발생 후 Spring MVC는 아래 순서로 예외를 넘겨 처리한다.

1. `HandlerExceptionResolver`들에게 예외 처리 시도

   * `DefaultHandlerExceptionResolver`
   * `ExceptionHandlerExceptionResolver`
   * `ResponseStatusExceptionResolver`
   * 등등

2. 처리되지 않으면 Servlet Container(톰캣)에게 전달됨

3. 톰캣은 자신이 등록된 `ErrorPage` 규칙대로 호출하거나
   최종적으로 Spring Boot의 `BasicErrorController`를 호출함

따라서 **예외를 먹고 서버는 계속 살아있게 된다.**

---

# 3. ErrorPage가 실행된 이유

당신은 아래처럼 ErrorPage를 등록했지?

```java
ErrorPage errorPageRe = new ErrorPage(RuntimeException.class, "/error-page/Re");
 factory.addErrorPages(errorPageRe);
```

그러면 톰캣은 RuntimeException이 발생하면:

1. 스택트레이스를 로그에 출력하고
2. 톰캣 error 처리로 전달
3. ErrorPage 매핑(`/error-page/Re`) 실행

그래서 로그에는 이렇게 배치됨:

```
java.lang.RuntimeException: 런타임 에러 발생
...
class hello.exception.errorpage.ErrorPageController.secondRe
```

즉, 예외는 발생했지만 곧바로 잡혀서 에러 페이지로 전달되었고,
**서버가 중단될 이유가 전혀 없었던 것.**

---

# 4. 왜 서버가 절대 멈추지 않나?

톰캣과 Spring Boot는 요청 처리 중 발생하는 대부분의 예외를
애플리케이션 전체 오류가 아니라 **개별 요청의 오류**로 취급한다.

서버가 멈추는 경우는 매우 제한적이다:

* Spring Boot 초기화 실패
* 톰캣 포트 바인딩 실패
* Spring 컨텍스트 생성 실패
* OutOfMemoryError 같은 치명적 JVM 오류
* NoClassDefFoundError 등 애플리케이션 구조적 오류

**컨트롤러 내부에서 발생하는 RuntimeException은 서버를 죽일 이유가 아니다.**

---

# 5. 결론

> **RuntimeException이 터져도 서버가 죽지 않는 것은 정상이며,
> Spring MVC와 톰캣이 해당 예외를 자신들의 예외 처리 흐름으로 잡아
> 에러 페이지를 호출하고 요청 단위에서 마무리했기 때문이다.**

원래 스프링 MVC 기반 서버는 이런 구조 덕분에 안정적으로 동작한다.

---

필요하다면 예외 발생 → DispatcherServlet → ErrorPage 호출까지 전체 흐름을 그림으로 정리해줄게.

에러 메시지가 출력된 위치는 **서버가 예외를 ‘로그로 남기는 단계’**에서 출력된 것이다.
조금 더 정확히 말하면, **예외가 처리되기 전에 DispatcherServlet 또는 톰캣이 해당 예외를 catch해서 로깅하는 단계**에서 출력된 로그다.

아래에서 전체 흐름을 순서대로 설명할게.

---

# 1. 에러 로그는 어디서 출력되는가?

## 결론

**톰캣(Tomcat)과 Spring MVC(DispatcherServlet)가 각각 예외를 잡아 로그로 출력한다.**

따라서 아래 로그들이 찍힌다:

* `java.lang.RuntimeException: 런타임 에러 발생`
* stack trace

이것들은 **Spring이 출력한 것이 아니라 톰캣 또는 DispatcherServlet이 출력한 로그**다.

---

# 2. 상세 흐름 설명

## (1) 컨트롤러에서 예외 발생

```java
throw new RuntimeException("런타임 에러 발생");
```

→ 여기서는 어떤 로그도 찍지 않음.

---

## (2) Spring MVC의 InvocableHandlerMethod가 예외를 catch

Spring MVC 내부 코드 일부는 이런 구조를 가진다.

```java
try {
    handlerMethod.invoke(...)
} catch (Exception ex) {
    // 여기서 HandlerExceptionResolver로 넘김
}
```

이 단계에서 **로그를 직접 출력하지 않지만**,
스택트레이스가 예외 객체에 포함되어 다음 단계로 넘어감.

---

## (3) DispatcherServlet에서 예외를 잡아 ErrorPage로 전달

DispatcherServlet은 예외 처리 도중 **로그를 남긴다.**

Spring 6.x 이상에서는 기본적으로 WARN 레벨 또는 ERROR 레벨로 스택 트레이스를 남긴다.

이 부분이 서버 콘솔에 찍힌다:

```
java.lang.RuntimeException: 런타임 에러 발생
    at ...
```

즉, **컨트롤러에서 던진 예외를 DispatcherServlet이 로그로 출력한 것.**

---

## (4) 예외가 톰캣으로 넘어가면 톰캣도 로그를 남긴다

톰캣 또한 ErrorPage 처리 과정에서 예외를 잡고 로그를 남긴다.

톰캣의 출력 코드(`ErrorReportValve` 등)는 예외를 ERROR 레벨로 출력한다.

그 결과:

* 예외를 Spring이 한 번 출력하고
* 톰캣이 또 출력하는 경우도 있음

그래서 스택트레이스가 두 번 보일 때도 존재한다.

---

# 3. 핵심 정리

### 에러 메시지 로그가 찍히는 위치

* **DispatcherServlet 내부의 예외 처리 시점**
* 또는
* **톰캣의 Error handling 단계**

### 에러 페이지 컨트롤러(`/error-page/Re`)는

예외 발생 → ErrorPage 매핑 이후 실행되므로
**에러 페이지 컨트롤러는 로그를 출력하지 않는다**
(너의 코드에서 로그를 찍는 건 단순한 System.out일 뿐)

---

# 4. 질문의 요지에 대한 한 문장 답변

> **에러 메시지는 컨트롤러가 아니라, Spring DispatcherServlet 또는 톰캣이 예외를 처리하는 과정에서 자동으로 로그로 출력된 것이다.**

---

필요하면 DispatcherServlet 내부 흐름을 코드 수준으로 깊게 설명해줄게.

아주 핵심적인 부분이라 정리해서 설명해볼게.
**InvocableHandlerMethod**는 Spring MVC에서 “컨트롤러 메서드를 실제로 호출하는 객체”다.
Spring MVC 요청 처리 흐름의 중심에 있는 컴포넌트다.

---

# 1. InvocableHandlerMethod란 무엇인가?

정의
**HandlerMethod(=컨트롤러의 @RequestMapping 메서드)를 실제로 invoke(호출)하는 기능을 담당하는 클래스.**

즉:

* `@GetMapping`, `@PostMapping`으로 매핑된 메서드
* 그 메서드의 파라미터들
* 그 메서드를 호출하기 위해 필요한 인수 생성
* 호출 결과(리턴 값) 처리

이 모든 과정을 수행하는 객체가 **InvocableHandlerMethod**이다.

여기에서 예외가 던져지면
→ Spring MVC가 잡아서 예외 처리 흐름(HandlerExceptionResolver 등)으로 넘긴다.

---

# 2. 위치

클래스 경로는 다음과 같다:

```
org.springframework.web.method.support.InvocableHandlerMethod
```

Spring MVC 내부 패키지에 존재하는 핵심 클래스.

---

# 3. InvocableHandlerMethod의 주요 역할

Spring은 컨트롤러 메서드를 “일반 자바 메서드”가 아니라
“웹 요청을 처리하도록 확장된 메서드”로 해석한다.

이를 위해 아래 기능들을 InvocableHandlerMethod가 수행한다.

---

## 역할 1) 컨트롤러 메서드에 필요한 인자(argument) 생성

예를 들면:

```java
@GetMapping("/test")
public String test(HttpServletRequest request,
                   @RequestParam String name,
                   @ModelAttribute User user)
```

이 메서드를 호출하려면:

* HttpServletRequest
* name 파라미터
* User 객체 (바인딩 결과)

등 다양한 객체를 만들어야 한다.

이 작업을 **HandlerMethodArgumentResolver**들이 담당하며
InvocableHandlerMethod가 그들을 호출해 실제 인자를 조립한다.

---

## 역할 2) 메서드를 실제로 invoke

조립된 argument들을 사용해 Java Reflection으로 다음을 호출한다.

```java
method.invoke(controller, args…)
```

컨트롤러에 정의된 @RequestMapping 메서드가 실제로 호출되는 순간이다.

---

## 역할 3) invoke 과정에서 발생한 예외 처리

이 부분이 중요하다.

컨트롤러 내부에서 RuntimeException이 발생하면
InvocableHandlerMethod는 예외를 그대로 던진다.

이 예외는:

1. InvocableHandlerMethod →
2. ServletInvocableHandlerMethod →
3. RequestMappingHandlerAdapter →
4. DispatcherServlet

으로 전달되고
DispatcherServlet이 예외를 처리(로그 출력 → ErrorPage 전달)하게 된다.

즉, **예외가 발생해도 서버가 죽지 않고 ErrorPage가 호출되는 이유는
InvocableHandlerMethod가 예외를 처리 흐름으로 넘기기 때문**이다.

---

## 역할 4) @ResponseBody, @ModelAttribute, ResponseEntity 등 리턴값 처리

컨트롤러 메서드가 무엇을 반환했는지에 따라:

* View 이름
* ResponseEntity
* JSON 변환(@ResponseBody)
* Model 값 처리

등의 후속 작업을 조정한다.

다만 실제 리턴값 처리는 `HandlerMethodReturnValueHandler`가 맡고,
InvocableHandlerMethod는 그것을 호출하는 역할을 한다.

---

# 4. 코드로 보는 핵심 구조

InvocableHandlerMethod.invokeForRequest 내부 구조는 다음과 같다:

```java
try {
    Object[] args = getMethodArgumentValues(request, mavContainer); // argument 생성
    return doInvoke(args); // reflection으로 실제 메서드 호출
} catch (Exception ex) {
    throw ex; // 여기서 예외를 다시 던짐 → DispatcherServlet으로 전달
}
```

즉:

* 컨트롤러 호출에 필요한 준비를 하고
* 호출을 실행하고
* 예외가 발생하면 넘겨버린다

이게 에러 페이지 호출 흐름과 이어진다.

---

# 5. InvocableHandlerMethod가 없다면?

Spring MVC는 컨트롤러 메서드를 호출할 수 없다.

실제로 InvocableHandlerMethod는
“Spring MVC가 HTTP 요청을 Controller 메서드 호출로 바꾸는 핵심 엔진”이다.

---

# 6. 한 문장 요약

> **InvocableHandlerMethod는 Spring MVC에서 컨트롤러 메서드를 호출하기 위한 인자 준비 → 호출 → 예외 전달까지 전담하는 핵심 클래스다.**

---

원하면 다음을 추가로 설명해줄 수 있어:

* InvocableHandlerMethod와 ServletInvocableHandlerMethod의 차이
* HandlerMethodArgumentResolver 체인 구조
* 예외가 DispatcherServlet으로 전달되는 상세 흐름
* 스프링 MVC 요청 처리 전체 시퀀스 다이어그램

결론부터 말하면 **아니다.**
`doInvoke()`는 forward와 아무 관련이 없다.
`doInvoke()`는 **컨트롤러 메서드를 실제로 호출하는 메서드**일 뿐이다.

forward는 이 단계에서 일어나지 않는다.

아래에서 아주 명확하게 설명해볼게.

---

# 1. `doInvoke()`의 실제 역할

Spring 소스코드를 보면 `doInvoke()`는 아래와 같다.

```java
protected Object doInvoke(Object... args) throws Exception {
    return this.method.invoke(this.bean, args);
}
```

즉:

* this.bean = 컨트롤러 객체
* this.method = @RequestMapping 메서드
* args = 분석된 파라미터들

따라서 `doInvoke()`는 단순히:

**컨트롤러 메서드를 리플렉션으로 호출하는 역할**

이다.

아무 forward, redirect, view 결정 로직도 포함하지 않는다.

---

# 2. forward는 어디서 발생하나?

forward는 아래 단계에서 발생한다:

1. 컨트롤러에서 예외 발생
2. 예외가 InvocableHandlerMethod → RequestMappingHandlerAdapter → DispatcherServlet 으로 전달됨
3. DispatcherServlet이 Servlet Container(Tomcat)에 예외를 넘김
4. Tomcat이 ErrorPage 설정을 확인
5. **Tomcat이 RequestDispatcher.forward() 로 에러 페이지 URL을 호출**

즉, **forward는 Tomcat이 처리하는 동작**이다.

컨트롤러 호출을 담당하는 Spring 내부와 전혀 다른 계층이다.

---

# 3. InvocableHandlerMethod는 forward를 하지 않는다

InvocableHandlerMethod는 다음 두 가지 역할만 한다.

1. 컨트롤러 메서드를 호출한다
   (정상 호출 = doInvoke)

2. 예외 발생 시 예외를 throw하여 DispatcherServlet에 넘긴다
   (예외를 swallow 하지 않음)

forward는:

* DispatcherServlet
* HandlerExceptionResolver
* Tomcat의 ErrorPage 처리기

이쪽에서 수행하는 작업이다.

---

# 4. 그림으로 흐름 정리

```
[1] InvocableHandlerMethod.doInvoke()
     ↓ 컨트롤러 메서드 실행 중 예외 발생
[2] catch(Exception) → throw ex (예외 전달)
[3] RequestMappingHandlerAdapter에서 받아 DispatcherServlet으로 전달
[4] DispatcherServlet → container로 예외 전달
[5] Tomcat ErrorPage 매핑 확인
[6] RequestDispatcher.forward("/error-page/Re")
     ↓
[7] 에러 페이지 컨트롤러 실행
```

forward는 **5~6 단계**에서 이루어진다.

---

# 5. 한 문장 요약

> `doInvoke()`는 단순히 컨트롤러 메서드를 실행하는 리플렉션 호출일 뿐이고, forward는 Tomcat이 ErrorPage 매핑을 처리하는 과정에서 발생한다.지

이것들은 **서블릿 컨테이너(톰캣 등)가 에러를 처리하기 위해 request scope에 자동으로 넣어주는 표준 속성(attribute)**이다.

즉, **에러가 발생하면 Tomcat → Spring → ErrorPageController로 넘어가는 과정에서, 에러 정보를 담아 request에 세팅하는 키들**이다.

그래서 ErrorPageController에서 `request.getAttribute(…)`로 꺼내서
에러 발생 원인, 메시지, 상태코드 등을 확인할 수 있다.

아래에서 하나씩 정확히 설명할게.

---

# 1. 이 상수들은 어디서 온 건가?

출처는 `jakarta.servlet.RequestDispatcher` 인터페이스이다.

```java
public interface RequestDispatcher {
    String ERROR_EXCEPTION = "javax.servlet.error.exception";
    String ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";
    String ERROR_MESSAGE = "javax.servlet.error.message";
    String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";
    String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";
    String ERROR_STATUS_CODE = "javax.servlet.error.status_code";
}
```

즉, **Servlet 스펙이 정의한 '표준 에러 정보 키'들**이다.

톰캣·제티·언더토우 등 모든 서블릿 컨테이너에서 공통으로 사용된다.

---

# 2. 언제 이 값들이 채워지는가?

조건
**404, 500 등 에러가 발생해서 servlet container가 error page로 forward할 때**

서블릿 컨테이너는 ErrorPage 매핑을 처리하면서 자동으로 request attribute에 아래 내용을 넣는다.

예: RuntimeException 발생 시

| key                                  | value                                        |
| ------------------------------------ | -------------------------------------------- |
| `javax.servlet.error.exception`      | 실제 예외 객체(e.g. RuntimeException)              |
| `javax.servlet.error.exception_type` | 예외 타입(e.g. java.lang.RuntimeException.class) |
| `javax.servlet.error.message`        | 예외 메시지(“런타임 에러 발생”)                          |
| `javax.servlet.error.request_uri`    | 예외 발생한 URL                                   |
| `javax.servlet.error.servlet_name`   | 에러 발생시 실행 중이던 서블릿 이름                         |
| `javax.servlet.error.status_code`    | 에러 HTTP 상태 코드(500, 404 등)                    |

---

# 3. ErrorPageController에서 어떻게 쓰는가?

예를 들어 다음처럼 사용할 수 있다:

```java
@Controller
public class ErrorPageController {

    @RequestMapping("/error-page/Re")
    public String errorPageRe(HttpServletRequest request) {

        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        System.out.println("exception = " + exception);
        System.out.println("message = " + message);
        System.out.println("uri = " + uri);
        System.out.println("status = " + status);

        return "error/re";
    }
}
```

이렇게 하면 에러 페이지에서도 원래 발생한 정보들을 확인할 수 있다.

---

# 4. Spring Boot 자체도 이 값을 사용한다

Spring Boot의 `BasicErrorController`도 이 표준 attribute를 사용해서
JSON 에러 응답이나 HTML 에러 페이지를 생성한다.

즉, **Spring Boot 에러 메시지의 근원 데이터가 바로 이 attribute들이다.**

---

# 5. 왜 표준 스펙이 이렇게 정의해놨을까?

이유는 간단하다.

* 서블릿 컨테이너는 에러 정보를 갖고 있다.
* 에러 페이지는 JSP, HTML, Spring Controller 등 다양한 방식일 수 있다.
* 이 에러 페이지에서 **표준 방식으로** 에러 정보를 전달받으려면 공통된 키가 필요하다.

그래서 Servlet 스펙이 위와 같은 키를 미리 정해둔다.

---

# 6. 한 문장 요약

> **이 상수들은 서블릿 컨테이너가 에러 발생 시 ErrorPage로 forward하면서 request에 담아주는 표준 에러 정보이며, ErrorPageController에서 이를 읽어 에러 상세 정보를 확인할 수 있다.**

---

원하면 실제 404, 500 상황에서 ErrorPageController에 어떤 값이 들어오는지 예제 로그로 구체적으로 보여줄게.

정확히 이해하면 전혀 헷갈릴 일이 없어.
세 가지 모두 이름에 “Dispatcher”가 들어가지만 **각자 역할과 계층이 완전히 다르다.**

가장 쉬운 방식으로 정리해볼게.
**요약 → 그림 → 세부 설명** 순서로 간다.

---

# 1. 한 문장 요약으로 빠르게 구분하기

| 이름                    | 소속             | 하는 일                                                          |
| --------------------- | -------------- | ------------------------------------------------------------- |
| **RequestDispatcher** | Servlet 표준 API | 요청을 다른 리소스로 **forward / include**하는 기능                        |
| **DispatcherType**    | Servlet 표준 API | 요청이 현재 어떤 이유로 **디스패치 됐는지 구분**하는 값 (REQUEST, FORWARD, ERROR 등) |
| **DispatcherServlet** | Spring MVC     | 스프링 웹 요청을 **컨트롤러로 라우팅하고 전체 흐름을 관리**하는 핵심 Servlet              |

이제 이 기준만 기억하면 이름 때문에 혼동할 일이 없다.

---

# 2. 전체 그림으로 한 번에 이해하기

```
(웹 브라우저 요청)
      |
      v
[ Tomcat HttpServlet ]
      |
      |  DispatcherType.REQUEST
      v
[ DispatcherServlet (Spring MVC) ]
      |
      | -> 컨트롤러 실행 중 예외 발생
      v

(Tomcat이 에러 페이지 매핑 확인)
      |
      |  RequestDispatcher.forward("/error-page/500")
      |  DispatcherType.ERROR
      v
[ ErrorPageController ]
```

이 흐름 속에서 등장하는 각각의 역할이 완전히 다르다.

---

# 3. 각 Dispatcher 설명

## 3-1) RequestDispatcher (서블릿 API)

### 소속: 톰캣 등 서블릿 컨테이너

### 기능: 요청을 다른 리소스로 보내는 기능

```
request.getRequestDispatcher("/path").forward(...)
request.getRequestDispatcher("/path").include(...)
```

forward 또는 include를 실행하는 **단순한 “요청 전달기”**일 뿐이다.

에러 페이지 이동도 이것을 사용한다.

즉, “forward는 누가 하냐?”
→ **RequestDispatcher가 한다.**

---

## 3-2) DispatcherType (서블릿 API)

### 소속: 톰캣 등 서블릿 컨테이너

### 기능: 이 요청이 어떤 경로로 현재 서블릿에 도착했는지 알려주는 값

예:

* **REQUEST**: 클라이언트가 최초로 요청한 것
* **FORWARD**: RequestDispatcher.forward로 전달된 요청
* **INCLUDE**: include로 전달된 요청
* **ERROR**: 에러 처리로 전달된 요청
* **ASYNC**: 비동기 처리

에러 페이지 컨트롤러는
`DispatcherType.ERROR` 로 요청이 들어온다.

이건 단지 “이 요청의 출신”을 표시하는 값일 뿐이다. 로직을 수행하지 않는다.

---

## 3-3) DispatcherServlet (Spring MVC)

### 소속: Spring

### 기능: Spring MVC의 핵심 servlet

* 핸들러 찾기
* 핸들러 어댑터 실행
* InvocableHandlerMethod 실행
* HandlerExceptionResolver 처리
* ModelAndView 처리

Spring MVC 전체의 조타수라고 보면 된다.

RequestDispatcher, DispatcherType과는 **계층 자체가 다르다**.

### DispatcherServlet은 forward를 하지 않는다

(단지 예외를 던지고 결과를 컨테이너에게 맡긴다)

forward는 컨테이너의 역할이므로 RequestDispatcher가 처리한다.

---

# 4. 각각이 언제 등장하는지 간단 요약

### ① DispatcherServlet

클라이언트 요청 → Spring MVC 처리 단계

```
요청 → DispatcherServlet → 컨트롤러 → 뷰 렌더링
```

### ② RequestDispatcher

컨트롤러에서 예외 발생 → 컨테이너가 error page로 forward

```
Tomcat → RequestDispatcher.forward("/error-page/500")
```

### ③ DispatcherType

위 두 디스패치 과정 중 “이 요청이 어떤 유형인지” 나타냄

```
RequestDispatcher.forward → DispatcherType.FORWARD
ErrorPage → DispatcherType.ERROR
```

---

# 5. 한 문장으로 끝내는 구분

* **DispatcherServlet**: Spring MVC의 중심, 요청 전체를 조율
* **RequestDispatcher**: 톰캣이 리퀘스트를 다른 경로로 넘기는 도구
* **DispatcherType**: 요청이 어떤 경로를 통해 왔는지 표시하는 플래그

---

원하면
RequestDispatcher.forward() 호출 시 내부에서 어떤 일이 일어나는지
아키텍처 레벨의 흐름도로도 설명해줄 수 있어.

`Filter`의 `doFilter` 안에서 **try / finally**를 쓰는 이유는
**요청 전 처리(Pre-processing)** 와 **요청 후 처리(Post-processing)** 를 분리하기 위해서이다.

지금 네 코드에서는 아직 핵심 부분이 빠져 있다.
정확히는 다음 두 가지가 있어야 한다.

---

# 1. try 내부: **filterChain.doFilter() 호출**

`filterChain.doFilter(request, response)`가 **다음 필터나 서블릿으로 요청을 넘기는 핵심 로직**이다.
이걸 호출해야 실제 컨트롤러까지 요청이 간다.

즉, try 블록에는 보통 **요청 전 로그 + 실제 필터 체인 실행** 이 들어간다.

예시:

```java
try {
    log.info("LogFilter.doFilter BEFORE: [{}][{}]",
            reqURI, request.getDispatcherType());

    filterChain.doFilter(servletRequest, servletResponse); // 다음 단계로 진행
}
```

---

# 2. finally 내부: **요청 후 처리**

finally는 **요청이 정상 처리되든, 예외가 발생하든 반드시 실행**된다.
따라서 보통 **요청 후 로그**, **리소스 정리**가 들어간다.

예시:

```java
finally {
    log.info("LogFilter.doFilter AFTER: [{}][{}]",
            reqURI, request.getDispatcherType());
}
```

---

# 3. 전체적으로 정리하면 이렇게 작성된다

```java
@Override
public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                     FilterChain filterChain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    String reqURI = request.getRequestURI();

    try {
        log.info("LogFilter.doFilter BEFORE: [{}][{}]",
                reqURI, request.getDispatcherType());

        filterChain.doFilter(servletRequest, servletResponse); // 핵심
    } finally {
        log.info("LogFilter.doFilter AFTER: [{}][{}]",
                reqURI, request.getDispatcherType());
    }
}
```

---

# 4. 왜 try 안에서 doFilter를 호출해야 하나?

* doFilter 내부에서 컨트롤러 호출 → JSP 렌더링 → 내부 forward → 예외 발생 등 다양한 일이 일어난다.
* 이 과정에서 예외가 발생해도 finally 내 로그는 반드시 호출된다.
* 따라서 **요청의 시작과 끝을 정확히 로그로 남길 수 있다**.

---

# 5. 만약 finally 없이 try만 쓰면?

* 컨트롤러에서 예외가 발생하면 `filterChain.doFilter()` 이후 처리는 실행되지 않고 바로 밖으로 던져진다.
* 그러면 요청 후 처리 로직(예: 응답 시간 측정, 리소스 반환 등)을 할 수 없다.

---

필요하다면 `DispatcherType.FORWARD`, `ERROR`, `REQUEST` 의 흐름에 따라
필터가 어떻게 호출되는지 예시 로그와 함께 설명해줄 수도 있다.
