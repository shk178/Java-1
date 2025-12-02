좋아.
지금 네가 "dispatchException 처리가 어디서 이루어지는지"를 정확히 파악하려고 `doService → doDispatch` 흐름을 보고 있는데, 핵심 포인트를 딱 한 가지 놓치고 있어서 헷갈리는 거야.

**dispatchException을 “잡아서 처리하는 곳”은 doDispatch가 아니라 doDispatch 내부에서 호출하는 processDispatchResult()다.**

즉:

* doService: doDispatch()만 호출 → 예외를 절대 처리하지 않음
* doDispatch: 예외를 inner try에서 “잡아서 변수에 저장만 함(dispatchException)”
* processDispatchResult: dispatchException을 실제로 처리하는 곳
  → ExceptionResolver에게 넘김
  → ModelAndView 생성하거나 sendError 호출
  → render, afterCompletion 처리

순서를 아주 정확하게 다시 짚어줄게.

---

# 1) doService → doDispatch

`doService()`는 예외 처리에 전혀 관여하지 않는다.

딱 한 줄:

```java
doDispatch(request, response);
```

즉, 예외 처리는 전부 **doDispatch 내부**에서 이루어진다.

---

# 2) doDispatch 내부 구조 (핵심)

### (1) inner try/catch ― 예외를 “잡아서 변수에 저장만” 한다

```java
try {  // inner try
    mv = ha.handle(...);
    mappedHandler.applyPostHandle(..., mv);

} catch (Exception ex) {
    dispatchException = ex;   // 예외를 저장만 하고 던지지 않음
} catch (Throwable err) {
    dispatchException = new ServletException(...);
}
```

⚠️ 여기서 예외는 “절대 던지지 않는다.”

즉, 실제 흐름이 끊기지 않는다.

→ **그래서 바로 아래로 이어서 실행된다.**

---

### (2) inner try/catch 바로 다음에 이 코드가 항상 실행됨

```java
processDispatchResult(request, response, mappedHandler, mv, dispatchException);
```

### 이 코드가 바로 **예외를 처리하는 곳**이다.

---

# 3) processDispatchResult()가 어떤 일을 하는지 다시 보자

네가 이미 올렸던 코드 중 일부:

```java
if (exception != null) {
    mv = processHandlerException(...);  // ← 예외 처리 핵심 부분
    errorView = (mv != null);
}

if (mv != null && !mv.wasCleared()) {
    render(mv, request, response);       // 에러 view 렌더링
}

mappedHandler.triggerAfterCompletion(...);  // 항상 실행
```

여기서:

### ✔ exception != null이면 → processHandlerException()이 실행됨

이게 Spring MVC의 ExceptionResolver들을 호출하는 부분이다.

### ✔ ModelAndView가 만들어지면

→ render() 실행

### ✔ ModelAndView가 null이면

→ sendError를 그대로 유지하거나 JSON 응답일 경우 렌더링 생략됨

---

# 결론

다시 정리하면:

### **dispatchException은 doDispatch 내부에서 만들어지고**,

### **processDispatchResult가 dispatchException을 실제로 처리한다.**

doService()에는 예외 처리 로직이 없다.
doService()는 doDispatch()를 호출하고, cleanup만 담당할 뿐이다.

---

# 흐름을 그림으로 전체 정리

```
DispatcherServlet.doService()
    └── doDispatch()
            ├── inner try → handler 실행
            │       ├── handler 정상 종료 → postHandle 실행
            │       └── handler 예외 발생 → inner catch에서 dispatchException 저장
            │
            ├── processDispatchResult(mv, dispatchException)
            │       ├── exception != null → ExceptionResolver 호출
            │       ├── mv != null → render()
            │       └── mv == null → render 생략 (JSON/sendError)
            │
            └── outer catch → 아주 심각한 예외만 처리
```

---

# 왜 doService에서는 dispatchException 처리가 보이지 않을까?

**왜냐면 예외 처리 완전체는 전부 doDispatch 내부에서 끝나기 때문이다.**

doService는 딱 두 가지 역할만 한다:

1. doDispatch 호출
2. cleanup 수행 (flashMap, include, multipart 등)

실제 요청 처리와 예외 처리는 100% doDispatch에서 이루어짐.

---

# 마지막으로 핵심 요약

1. **inner try/catch**

    * 예외를 잡아서 dispatchException에 저장
    * 예외를 던지지 않음 → 흐름 계속

2. **processDispatchResult** ← 예외 처리 핵심

    * dispatchException이 있다면 ExceptionResolver 호출
    * ModelAndView 처리 → render
    * afterCompletion 실행

3. **doService**

    * doDispatch 호출
    * 예외 처리에는 관여하지 않음

따라서 doService에서는 dispatchException 관련 로직을 볼 수 없는 것이 정상이다.

---

원하면:

* processHandlerException 내부가 어떻게 ExceptionResolver를 호출하는지
* HandlerExceptionResolver가 여러 개일 때 어떤 순서로 동작하는지
* sendError 호출된 경우 ExceptionResolver가 어떻게 반응하는지

이런 것들도 이어서 자세히 설명해줄 수 있다.

```java
private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
        @Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
        @Nullable Exception exception) throws Exception {

    boolean errorView = false;

    if (exception != null) {
        if (exception instanceof ModelAndViewDefiningException mavDefiningException) {
            logger.debug("ModelAndViewDefiningException encountered", exception);
            mv = mavDefiningException.getModelAndView();
        }
        else {
            Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
            mv = processHandlerException(request, response, handler, exception);
            errorView = (mv != null);
        }
    }

    // Did the handler return a view to render?
    if (mv != null && !mv.wasCleared()) {
        render(mv, request, response);
        if (errorView) {
            WebUtils.clearErrorRequestAttributes(request);
        }
    }
    else {
        if (logger.isTraceEnabled()) {
            logger.trace("No view rendering, null ModelAndView returned.");
        }
    }

    if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
        // Concurrent handling started during a forward
        return;
    }

    if (mappedHandler != null) {
        // Exception (if any) is already handled..
        mappedHandler.triggerAfterCompletion(request, response, null);
    }
}
```
```java
protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response,
        @Nullable Object handler, Exception ex) throws Exception {

    // Success and error responses may use different content types
    request.removeAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);
    // Reset the response content-type header and body buffer if the response is not committed already,
    // leaving the other response headers in place.
    try {
        response.setHeader(HttpHeaders.CONTENT_TYPE, null);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, null);
        response.resetBuffer();
    }
    catch (IllegalStateException illegalStateException) {
        // the response is already committed, leave it to exception handlers anyway
    }

    // Check registered HandlerExceptionResolvers...
    ModelAndView exMv = null;
    if (this.handlerExceptionResolvers != null) {
        for (HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
            exMv = resolver.resolveException(request, response, handler, ex);
            if (exMv != null) {
                break;
            }
        }
    }
    if (exMv != null) {
        if (exMv.isEmpty()) {
            request.setAttribute(EXCEPTION_ATTRIBUTE, ex);
            return null;
        }
        // We might still need view name translation for a plain error model...
        if (!exMv.hasView()) {
            String defaultViewName = getDefaultViewName(request);
            if (defaultViewName != null) {
                exMv.setViewName(defaultViewName);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Using resolved error view: " + exMv, ex);
        }
        else if (logger.isDebugEnabled()) {
            logger.debug("Using resolved error view: " + exMv);
        }
        WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
        return exMv;
    }

    throw ex;
}
```

좋아.
이제 **Spring MVC 예외 처리의 ‘가장 핵심적인 부분’**인
`processHandlerException()`을 정확히 파악할 수 있는 코드가 나온 거야.

이 코드는 Spring MVC에서 예외가 발생했을 때 **ModelAndView 기반으로 오류 화면(또는 JSON 오류)을 만들지**
혹은 **예외를 다시 던질지**를 결정하는 최종 단계이다.

지금 네가 보고 있는 코드에 대한 핵심 포인트를 차례대로 정리해줄게.

---

# 전체 동작 요약

`processHandlerException()`은 다음 순서로 처리한다:

1. **기존 response buffer와 Content-Type을 리셋**
2. **등록된 HandlerExceptionResolver들을 순서대로 실행**
3. **Resolver가 ModelAndView를 반환하면 그걸 사용**
4. **Resolver가 아무것도 처리하지 않으면 예외를 던짐**
   → outer try/catch로 넘어감 → triggerAfterCompletion

---

# 코드 분석 (라인 단위로 해석)

---

## ✔ 1) response content-type 및 body buffer를 초기화

```java
response.setHeader(HttpHeaders.CONTENT_TYPE, null);
response.setHeader(HttpHeaders.CONTENT_DISPOSITION, null);
response.resetBuffer();
```

### 의미:

* 기존에 JSON 응답을 만들던 중이었다면, 그 body는 버리고 새로 오류 처리 로직을 적용하기 위함
* 즉, error view를 렌더링하기 위한 “깨끗한 응답 버퍼”

### 예외:

```java
catch (IllegalStateException)
```

buffer가 이미 커밋되었다면 resetBuffer()가 실패한다 → 그대로 진행

즉:

### ✔ **응답이 이미 커밋되었더라도 Spring은 예외 처리기를 시도한다.**

(다만 render는 못 한다)

---

## ✔ 2) ExceptionResolver 목록을 순회

```java
for (HandlerExceptionResolver resolver : this.handlerExceptionResolvers) {
    exMv = resolver.resolveException(request, response, handler, ex);
    if (exMv != null) break;
}
```

### 대표적인 Resolver들:

* ExceptionHandlerExceptionResolver (@ExceptionHandler, ControllerAdvice)
* ResponseStatusExceptionResolver
* DefaultHandlerExceptionResolver

즉, 예외에 맞는 HandlerExceptionResolver가 **ModelAndView 혹은 ResponseBody를 생성할 수 있도록 하는 곳.**

---

## ✔ 3) Resolver가 ModelAndView를 반환하면 분기 처리

### (1) exMv.isEmpty()

→ “빈 ModelAndView”면 view 렌더링 아예 하지 않음
→ return null

즉, 이 경우 postHandle을 건너뛰고
**JSON 응답이거나 sendError가 설정된 상황**일 수 있음.

---

### (2) exMv.hasView() == false

→ 뷰 이름이 없는 경우 default view name을 강제로 설정
→ 예: error.jsp 같은 view로 정상 렌더링하도록 유도

---

### 마지막:

```java
return exMv;
```

즉, 에러를 **뷰 기반으로 그려줄 수 있다면 그걸 반환한다.**

---

## ✔ 4) Resolver가 아무것도 처리하지 않으면?

```java
throw ex;
```

이게 **예외가 외부로 던져지는 유일한 시점**이다.

이때 doDispatch의 outer catch가 동작한다:

```java
catch (Exception ex) {
    triggerAfterCompletion(...)
}
```

---

# 전체 흐름을 요약하면

## 1. handler(postHandle 포함)에서 예외 발생

→ doDispatch inner catch에서 dispatchException 변수에 저장

## 2. processDispatchResult() 호출

→ exception != null → processHandlerException 호출

## 3. processHandlerException()

* response buffer reset
* ExceptionResolver들에게 위임
* ModelAndView를 반환하도록 시도
* 반환되면 **render() → 정상적인 error 응답 출력**

## 4. Resolver가 아무것도 처리하지 못하면

→ throw ex
→ outer catch로 이동
→ afterCompletion 실행
→ 서블릿 컨테이너가 기본 에러 페이지 처리

---

# 왜 중요한가?

이 코드 때문에 Spring MVC는 다음이 가능하다:

* @ControllerAdvice에서 JSON 기반 에러 응답 생성
* ResponseStatusException 사용
* @ExceptionHandler 기반의 커스텀 에러 처리
* sendError 발생 시 서블릿 컨테이너가 에러 페이지를 그리는 흐름
* Error(OutOfMemoryError 등) 발생 시도 exceptionResolver에 넘겨서 처리

그리고 너가 하고 있는 분석의 핵심:

### ✔ JSON 응답에서도 postHandle이 호출될 수 있는 이유

→ handler 처리까지는 예외가 없기 때문
→ 예외가 발생하면 inner catch로 넘어가 postHandle이 건너뛰고
→ 이후 processHandlerException 로직이 수행됨

### ✔ sendError에서도 postHandle은 호출되지만

→ 이후 processHandlerException / render 단계에서 mv=null로 인해 렌더링은 건너뛰는 구조

---

# 최종 요약

`processHandlerException()`은:

### 🔹 response를 “깨끗한 상태로 초기화”

### 🔹 ExceptionResolver 목록을 순회

### 🔹 에러 view(ModelAndView)를 만들어 반환

### 🔹 뷰가 없으면 JSON 응답(@ExceptionHandler ResponseBody)로 전환

### 🔹 아무것도 처리하지 못하면 throw ex → outer catch에서 afterCompletion만 실행

즉, **Spring MVC 예외 처리를 완전히 관장하는 핵심 부분**이다.

---

원하면 다음도 설명해줄 수 있어:

* exMv.isEmpty(), wasCleared(), null의 차이
* JSON 기반 예외 응답(@ExceptionHandler)에서 postHandle/afterCompletion 차이
* sendError와 @ExceptionHandler가 동시에 존재할 때 우선순위
* 구체적인 호출 스택

계속 알고 싶어?

정확한 이해에 거의 도달했어.
이제 **Spring MVC의 예외 처리 흐름 전체**와 **Servlet 컨테이너의 기본 예외 처리**,
그리고 **Spring Boot의 BasicErrorController** 역할이 **서로 어떻게 나뉘는지**를 아주 정확하게 정리해서 알려줄게.

너의 질문:

> 스프링 MVC 예외 처리도 여기서 다 되고 서블릿 컨테이너 예외 처리랑 forward도 basicErrorController 사용하지 않는 이상은 여기서 다 되는 거야?

**정답:**

### ✔ “Spring MVC 예외 처리”는 **100% processHandlerException() 안에서 모두 처리된다.**

### ✔ “서블릿 컨테이너 예외 처리”는 **Spring MVC가 처리 못한 경우에만** 동작한다.

### ✔ Spring Boot의 BasicErrorController는 **Servlet 컨테이너 예외 처리 대신 개입하는 Spring Boot 전용 오류 처리기**이다.

→ 이것을 사용하지 않는다면 Spring MVC의 기본 ExceptionResolver 또는 서블릿 컨테이너가 처리하게 된다.

즉, **예외 처리 우선순위는 아래처럼 서열이 있다.**

---

# 1) Spring MVC가 예외 처리할 수 있는 모든 경우

(스프링 MVC 자체 레이어)

예외가 컨트롤러 or 인터셉터 or argumentResolver or messageConverter 등에서 발생하면,
DispatcherServlet의 `processDispatchResult → processHandlerException()`에서 처리된다.

여기서 사용하는 Resolver 들:

1. **ExceptionHandlerExceptionResolver**
   → @ExceptionHandler, @ControllerAdvice

2. **ResponseStatusExceptionResolver**
   → @ResponseStatus, ResponseStatusException

3. **DefaultHandlerExceptionResolver**
   → 404/405/415 등 표준 Spring MVC 에러 매핑

여기서 ModelAndView나 ResponseBody가 생성되면
Servlet 컨테이너까지 예외가 가지 않는다.

### ✔ 즉, “Spring MVC 예외 처리”는 전부 여기서 끝난다.

---

# 2) Spring MVC에 의해 처리되지 않은 예외

→ 서블릿 컨테이너에게 넘어감

`processHandlerException()`의 끝에서 이 코드를 보았지?

```java
throw ex;
```

이게 발생하면 이제 Spring MVC는 책임을 포기하고
서블릿 엔진(Tomcat/Jetty/Undertow 등)에게 예외를 넘긴다.

그 다음은 J2EE 표준 오류 처리 순서:

1. **web.xml <error-page> 규칙에 따른 forward**
2. 규칙이 없으면 **컨테이너 기본 요청 에러 페이지** (기본 500 페이지)

즉:

### ✔ Spring MVC가 못 잡은 예외만 서블릿 컨테이너가 처리한다.

---

# 3) 그럼 Spring Boot의 BasicErrorController는 어디 개입할까?

Spring Boot는 Servlet 컨테이너의 기본 에러 페이지를 사용하지 않음.
대신 **ErrorMvcAutoConfiguration**에서 등록한 아래 두 가지 Bean이 개입한다:

* **BasicErrorController**
* **DefaultErrorViewResolver**
* **ErrorAttributes**

동작 방식은 다음과 같다:

1. Spring MVC가 예외를 잡지 못해 throw ex → 서블릿 컨테이너로 넘어감
2. Boot의 **ErrorPageCustomizer**가 컨테이너에 “모든 에러는 /error로 포워드해라” 라고 등록
3. 컨테이너가 /error로 forward
4. BasicErrorController가 호출
5. JSON or HTML 오류를 ResponseEntity 혹은 error view로 반환

즉:

### ✔ Boot의 BasicErrorController는 “서블릿 컨테이너 기본 에러 처리”를 대체한 Spring MVC Controller이다.

---

# 4) 정리: 너의 질문에 대한 정확한 답

### ✔ Spring MVC 예외 처리

→ **전부 DispatcherServlet → processHandlerException() 안에서 처리됨**

### ✔ Servlet 컨테이너 예외 처리

→ Spring MVC가 처리 못했을 때만 발생
→ web.xml의 <error-page> or 기본 컨테이너 에러 페이지

### ✔ Spring Boot BasicErrorController

→ 컨테이너 에러 페이지를 강제로 /error forward 시켜
Boot 전용 오류 JSON/HTML로 처리
→ BasicErrorController가 없으면 → 컨테이너 error 페이지 or web.xml error-page 사용됨

따라서:

### ✔ “기본 Spring MVC만 쓴다면”

: **예외는 (1) Spring MVC 처리 → (2) 처리 실패 시 컨테이너 처리**

### ✔ “Spring Boot 사용 중이고 BasicErrorController가 등록됨”

: **컨테이너 처리까지 가지 않고 /error → BasicErrorController가 처리**

---

# 5) 마지막 요약 — 한 문장으로

**Spring MVC 예외 처리는 `processHandlerException()`에서 전부 이루어지며,
Spring Boot는 BasicErrorController를 통해 Servlet 컨테이너 오류 처리 단계도 Spring MVC로 흡수한다.**

---

원하면 다음도 설명해 줄게:

* @ResponseStatusExceptionResolver와 BasicErrorController가 충돌할 때 어떤 것이 우선인지
* sendError 호출 시 flow (MVC → 컨테이너 → Boot error) 그림
* Spring Security 예외 처리까지 합쳐서 전체 그림

계속 보고 싶어?

정확한 지점을 헷갈리고 있어.
**processHandlerException()** 과 **afterCompletion()** 은
역할과 “response를 보낼 수 있는가?” 라는 점에서 **완전히 다른 단계**다.

아래 설명을 보면 두 개의 차이가 완전히 정리될 거야.

---

# ✔ 결론 먼저

### **afterCompletion에서는 response를 “보내면 안 되고”, 사실상 “보낼 수도 없다”.**

* response가 이미 commit되어 있는 경우가 99%
* render 단계 뒤에 호출되는 cleanup 콜백일 뿐
* 새로운 응답 본문/헤더를 작성할 수 없음
* response.getWriter() 등을 호출하면 IllegalStateException 또는 무시됨

반면,

### **processHandlerException()은 진짜로 응답을 생성하는 단계(본문/헤더/상태코드 가능).**

---

# ✔ 이제 각 단계의 역할 차이를 정확하게 정리해보자

---

# 1) processHandlerException() – “예외 응답(Response) 생성 단계”

예외 발생 시 **Spring MVC가 예외를 처리해서 실제 응답을 만들어내는 단계**다.

### 여기서는 “진짜 응답”을 만든다:

* response status 설정
* response header 설정
* response body(JSON, HTML, ModelAndView 렌더링)
* `sendError()` 호출
* ModelAndView 기반 렌더링

### 즉, **여기서는 정상적인 Response를 사용할 수 있다.**

새 응답 본문을 만들고, 헤더 세팅하고, 상태 코드를 변경하는 진짜 처리 단계.

---

# 2) afterCompletion() – “완전한 요청 종료 직전 Cleanup 단계”

afterCompletion의 특징은 매우 중요하다:

### ✔ 1) view 렌더링 후 실행된다

→ response는 이미 commit된 상태 (sendError나 JSON 전송 완료 후)

### ✔ 2) 예외가 있든 없든 항상 실행된다

→ cleanup 목적 (로그, 리소스 해제, 트랜잭션 종료 같은 작업)

### ✔ 3) response를 써도 의미 없다

→ 이미 커밋되었기 때문에 body를 더 쓰거나 상태코드를 바꾸는 건 불가능
→ 컨테이너는 실제로 응답을 이미 클라이언트에게 flush했음

### ✔ 4) afterCompletion은 "응답을 변경하는 단계"가 아니다

→ 응답을 바꾸려고 하면 IllegalStateException이 발생하거나 조용히 무시된다

따라서:

### **afterCompletion은 “응답 생성 단계가 아니라, 요청 후처리(cleanup) 단계”이다.**

---

# ✔ 핵심 비교 요약표

| 기능                    | processHandlerException()  | afterCompletion()     |
| --------------------- | -------------------------- | --------------------- |
| 목적                    | 예외를 Response로 변환           | 요청 종료 후 cleanup       |
| response body 작성 가능?  | **가능**                     | **불가능** (대부분 commit됨) |
| 상태 코드 변경 가능?          | 가능                         | 불가                    |
| 헤더 설정 가능?             | 가능                         | 불가                    |
| view 렌더링?             | 가능                         | 불가                    |
| 호출 시점                 | postHandle 이전 or render 이전 | 항상 render 이후          |
| 예외 핸들러?               | Yes                        | X                     |
| 응답이 클라이언트에 flush 되었나? | flush 전                    | flush 후               |

---

# ✔ 왜 afterCompletion에서 응답을 보낼 수 없나?

DispatcherServlet의 호출 순서를 보면 명확함:

```
preHandle
handler
postHandle
[ view 렌더링 / JSON 변환 / sendError ]
afterCompletion   ← 여기서는 이미 response commit됨
```

그리고 JSON 응답(@ResponseBody) 처리 로직 보면:

```java
writeWithMessageConverters(...)  // response.write()
response.flushBuffer()           // 버퍼 flush
mavContainer.setRequestHandled(true)
```

이 이후에 afterCompletion이 실행됨.

즉:

### ✔ afterCompletion은 이미 응답 플러시가 끝난 뒤다.

---

# ✔ 정리: 둘은 절대 혼동되면 안 되는 역할

## ▷ processHandlerException

**예외 응답을 실제로 만드는 단계**

* JSON 응답 만들고
* HTML 에러 페이지 만들고
* status 설정하고
* view 렌더링하고
  전부 여기서 한다.

## ▷ afterCompletion

**이미 응답이 종료된 뒤에 “마무리 작업” 하는 단계**

* 오류 로깅
* 트랜잭션 종료
* 리소스 해제
* 모니터링
  이런 것만 가능.

**응답 내용을 변경하거나 새 응답을 보낼 수는 없다.**

---

원하면 다음도 답변해줄 수 있어:

* afterCompletion에서 response 상태가 commit되었는지 확인하는 방법
* postHandle / afterCompletion 실행 순서의 edge case
* @ResponseBody 예외 처리(JSON 응답) 높은 수준 flow-chart
* Spring Boot(ErrorMvc) 포함한 전체 오류 흐름도

어떤 걸 보고 싶어?
