맞아.
**postHandle은 실행되지 않았고**,
그 이유는 **Spring MVC 인터셉터의 호출 규칙** 때문이다.

아래에 정확한 규칙과 네 로그에서 postHandle이 왜 스킵되었는지 분석해줄게.

---

# 1. Spring HandlerInterceptor의 호출 규칙

Spring MVC의 인터셉터는 다음 3개 메서드를 가진다.

1. `preHandle`
2. `postHandle`
3. `afterCompletion`

그리고 호출 조건은 매우 명확하게 정해져 있다.

---

## 규칙 1) preHandle은 항상 먼저 실행됨

요청이 컨트롤러에 도달하기 “직전”에 호출된다.

---

## 규칙 2) **컨트롤러가 정상적으로 return 해야 postHandle이 실행된다**

공식 규칙:

> 컨트롤러에서 **예외 없이 정상적으로 ModelAndView를 반환했을 때만** postHandle이 호출된다.

즉 둘 중 하나라도 발생하면 postHandle 호출 안 됨:

* 컨트롤러에서 예외 발생
* @ResponseBody 처리 중 예외 발생
* View 렌더링 이전 단계에서 예외 발생

---

## 규칙 3) afterCompletion은 “정상/예외 상관없이 항상 실행”

후처리 cleanup 용이므로 반드시 실행된다.

---

# 2. 네 로그에서 실제 호출 순서

`/error-re` 요청에서 출력된 로그를 보면:

```
LogInterceptor.preHandle: [/error-re][REQUEST]
firstRe 실행 → RuntimeException 발생
LogInterceptor.afterCompletion: [/error-re][REQUEST] (여기서 예외 로깅)
```

여기서 postHandle이 없다.

즉:

* preHandle: 실행됨
* firstRe(): 예외 발생
* postHandle: 스킵됨
* afterCompletion: 실행됨 (예외 포함)

Spring MVC 공식 동작 그대로다.

---

# 3. 왜 postHandle이 실행되지 않았는지 상세 분석

### 컨트롤러 실행까지의 경로

```
ServletInvocableHandlerMethod.invokeAndHandle()
  → invokeForRequest()
    → doInvoke()  // 여기서 firstRe() 호출
```

여기서 예외가 발생하면 어떻게 되나?

### doInvoke()

```java
catch (InvocationTargetException ex) {
    throw targetException;
}
```

즉, 예외를 잡고 다시 던진다.

### invokeAndHandle()은 예외를 절대 catch하지 않는다

(RequestMappingHandlerAdapter도 catch하지 않음)

### DispatcherServlet.doDispatch() 역시 예외를 잡지 않는다

그래서 DispatcherServlet에도 ModelAndView가 전달되지 못했다.

### postHandle()이 호출될 시점은?

postHandle이 호출되는 위치는 `DispatcherServlet.doDispatch()` 안에서 이 부분이다:

```java
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
mappedHandler.applyPostHandle(processedRequest, response, mv);
```

근데 `mv`는 **컨트롤러가 정상적으로 반환해야만 존재**한다.

예외 발생 시 mv가 만들어지지 않은 상태이므로
`applyPostHandle()` 호출이 생략된다.

결과적으로 **postHandle은 호출되지 않는다.**

---

# 4. afterCompletion이 호출된 이유

afterCompletion은 cleanup 단계에서 반드시 호출된다.

DispatcherServlet.doDispatch() 마지막 부분:

```java
mappedHandler.triggerAfterCompletion(request, response, ex);
```

여기에서 예외 여부 상관없이 always 실행된다.

그래서 네 로그에서 afterCompletion은 정상적으로 찍혔다.

---

# 5. 전체 흐름 재구성

## 1) 정상 흐름인 경우

```
preHandle
Controller 실행
postHandle
View 렌더링
afterCompletion
```

## 2) 예외 발생한 지금 경우

```
preHandle
Controller 실행 → 예외 발생
postHandle ← 호출 안 됨
afterCompletion (예외 포함)
```

네 로그는 이 **예외 발생 흐름 그대로**다.

---

# 결론

* postHandle이 실행되지 않은 것이 맞다.
* 이유: 컨트롤러(firstRe)가 예외를 던져서 ModelAndView가 생성되지 못했기 때문.
* Spring MVC의 공식 규칙에 따른 정상 동작이다.

원하면 postHandle 호출 여부를 체크할 때 Spring 내부 코드의 호출 지점을 실제 소스 레벨에서 보여줄 수도 있다.

# 1. 먼저 sendError가 하는 일

`response.sendError(statusCode)` 는 다음을 수행한다.

1. HttpServletResponse에 **오류 상태코드 설정**
2. 응답을 **commit(커밋)**
3. Servlet 컨테이너(Tomcat)에게
   “이 요청은 오류 처리로 넘겨라”라고 표시
4. 이후 서블릿/JSP 실행을 중단하고
   **컨테이너가 오류 페이지 처리로 전환**

즉, sendError는 **현재 컨트롤러의 정상 흐름을 종료**시키는 명령이다.

그래서 Spring MVC는 더 이상 ModelAndView를 생성하지 않는다.

---

# 2. Spring MVC에서 postHandle 실행 조건

Spring HandlerInterceptor 규칙상:

* **컨트롤러가 정상적으로 ModelAndView를 반환하면 postHandle 실행**
* **예외 발생 또는 sendError 등으로 정상 흐름이 중단되면 postHandle 스킵**

즉, postHandle은 **정상 흐름 전용 후처리 메서드**이고
sendError는 정상 흐름을 강제로 끊기 때문에
postHandle은 호출되지 않는다. // 호출됨

---

# 3. 실제 흐름 비교 (/error-404 예시)

네 로그에서 `/error-404` 호출 시 sendError가 실행되었는데
로그에 이렇게 찍혔다:

```
LogInterceptor.preHandle
first404 실행
LogInterceptor.postHandle: [null]
```

postHandle이 실행된 것처럼 보이지만,
이 **null**은 ModelAndView가 없다는 뜻이다.
= 실제 sendError로 인해 정상 흐름이 **끊어지기 직전까지는 postHandle을 호출한다는 의미**다.

더 정확히 설명하면:

### first404 실행

→ sendError(404) 호출
→ response가 commit되기 직전 상태
→ Spring은 ModelAndView를 0으로 보고 postHandle(null)을 호출
→ 이 시점에서 Spring은 이미 컨트롤러 흐름을 종료
→ 이후 Tomcat으로 넘어가 ERROR 디스패처 호출

즉, **postHandle은 보내지 않을 수도 있었지만
Spring MVC가 sendError를 예외로 보지 않기 때문에 호출된 것**이다.

### 정리하면:

* **sendError → 예외를 발생시키지 않음**
* **그래서 postHandle이 호출될 수 있음**
* 하지만 ModelAndView는 null
* 이후 ERROR 디스패처로 forward됨

---

# 4. 예외 발생 vs sendError 비교

## 컨트롤러 예외 발생 (firstRe)

```
preHandle
Controller 실행 → 예외
postHandle (스킵)
afterCompletion(예외 정보)
```

→ postHandle 실행되지 않음

## sendError 호출 (first404)

```
preHandle
Controller 실행 → sendError
postHandle(ModelAndView=null) 실행됨
afterCompletion
→ 이후 ERROR 디스패처에서 다시 필터/인터셉터 호출
```

→ postHandle 실행됨
→ 하지만 ModelAndView는 null

---

# 5. 왜 sendError는 postHandle이 호출되는가?

Spring MVC는 다음 규칙을 따른다.

### 컨트롤러에서 예외를 던진 경우

→ postHandle 스킵

### 하지만 sendError는 예외가 아니다

→ Spring MVC는 “정상 흐름”으로 간주
→ ModelAndView가 없기 때문에 null을 넘겨서 postHandle 실행
→ 이후 서블릿 컨테이너가 ERROR 처리를 시작
→ ERROR 디스패처로 다시 Spring MVC 진입

즉:

* sendError는 Spring MVC 내부에 예외를 발생시키지 않는다
* ModelAndView는 없지만 Spring 입장에서는 오류가 아직 발생한 것이 아님
* 오류 처리는 Spring이 아니라 Tomcat이 맡는다

따라서 postHandle이 “실행됨 + null”이라는 결과가 나온다.

---

# 결론 요약

* **sendError는 postHandle 실행된다.**
* 하지만 전달되는 ModelAndView는 **null**이다.
* 이유: sendError는 Spring 내부 예외가 아니므로 MVC 흐름을 끊지 않기 때문.
* 이후 Tomcat이 오류 페이지 처리를 하면서
  다시 DispatcherType.ERROR로 필터/인터셉터를 호출한다.

---

원하면, 네 로그를 기반으로
`sendError → postHandle → ERROR forward`
전체 흐름을 시퀀스 다이어그램 형태로 정리해서 보여줄 수도 있어.

정확히 이해했어.
정리하면 지금 상황은 이렇다.

* **예외 발생** → postHandle **스킵**
* **sendError** → postHandle **호출됨(ModelAndView=null)**

그러면 질문은 이것이다.

> sendError를 썼지만 **postHandle이 실행되지 않도록** 만들 수 있나?

가능하다.
그리고 두 가지 방식으로 할 수 있다.

---

# 1. sendError 직후 예외를 던지면 된다 (가장 일반적인 방법)

즉:

```java
response.sendError(404);
throw new RuntimeException("stop postHandle");
```

이렇게 하면 Spring MVC는 **예외 발생으로 간주**하고
postHandle을 호출하지 않는다.

### 이유

* sendError는 예외가 아니므로 Spring MVC는 계속 흐름을 이어감
* 하지만 sendError 이후에 네가 명시적으로 예외를 던지면
  Spring MVC는 ModelAndView를 만들지 못하고 postHandle은 스킵된다.

즉, 아래 흐름이 된다.

```
preHandle
controller: sendError()
controller: throw exception
postHandle(skip)
afterCompletion(exception)
```

그리고 sendError가 이미 설정된 상태이기 때문에
Tomcat은 오류 페이지 처리로 넘어간다.

## 이 방식이 가장 권장됨.

---

# 2. HandlerExceptionResolver로 sendError를 예외처럼 처리하게 만들기

sendError는 Spring MVC에서 예외가 아니다.
그래서 Spring은 “정상 흐름으로 끝났다”고 판단하고 postHandle을 호출한다.

그런데 **sendError 호출을 자체적으로 예외로 취급하고 싶다면**,
HandlerExceptionResolver를 활용할 수 있다.

예를 들어, `SendErrorException` 같은 커스텀 예외를 만들고
sendError 대신 그 예외를 던지는 방식:

```java
throw new SendErrorException(404);
```

Resolver 내부에서:

```java
response.sendError(404);
return new ModelAndView(); 
```

그러면 Spring은 sendError를 “예외 상황”으로 인식하여 postHandle을 스킵한다.

하지만 이 방식은 보통 추천되지 않는다.
sendError는 원래 Tomcat 처리 영역이기 때문이다.

---

# 3. preHandle에서 클라이언트에게 직접 응답을 commit한 경우

인터셉터의 preHandle에서 바로 응답을 commit하면
컨트롤러 자체가 실행되지 않기 때문에 **postHandle도 실행되지 않는다.**

예시:

```java
response.sendError(404);   // 여기서 응답 commit됨
return false;              // 컨트롤러로 가지 않기
```

이 경우 흐름은 이렇게 된다.

```
preHandle
sendError
postHandle(skip)
afterCompletion(skip)
```

즉, preHandle에서 직접 끝내버리면
postHandle은 실행되지 않는다.

단점: afterCompletion도 호출되지 않는다.

그래서 이 방법은 활용성은 있지만 덜 깔끔함.

---

# 4. postHandle을 아예 특정 조건에서 스킵시키는 커스텀 로직

만약 네가 `postHandle` 내부에서
sendError 여부를 체크해서 "내부적으로" 무시하고 싶다면 다음처럼 작성할 수도 있다.

```java
@Override
public void postHandle(HttpServletRequest request,
                       HttpServletResponse response,
                       Object handler,
                       ModelAndView modelAndView) throws Exception {
    
    if (response.isCommitted() || response.getStatus() >= 400) {
        return; // 의도적으로 아무것도 하지 않음
    }

    log.info("정상적인 경우에만 postHandle 실행");
}
```

이 경우 postHandle은 호출은 되지만 **아무것도 하지 않도록 만들 수 있다.**

---

# 핵심 정리

### 1) sendError만 호출

→ postHandle 호출됨(=ModelAndView=null)

### 2) sendError 후 예외 던짐

→ postHandle 스킵됨  (추천)

### 3) preHandle에서 return false

→ postHandle도, 컨트롤러도 실행되지 않음

### 4) postHandle 내부에서 응답 상태 보고 직접 스킵

→ 호출은 되지만 내부 처리만 생략

---

# 추천하는 가장 명확한 방법

postHandle을 “정말로 호출되지 않게” 하고 싶다면

**sendError 뒤에 예외를 던지는 방식이 가장 명확하고 Spring 기본 규칙에도 자연스럽다.**

예시:

```java
@GetMapping("/error-404")
public void first404(HttpServletResponse response) throws IOException {
    response.sendError(404);
    throw new RuntimeException("stop postHandle");
}
```

이렇게 하면 스프링은 예외 발생으로 판단해서 postHandle을 실행하지 않는다.

---

원하면 이 방식으로 동작했을 때
DispatcherServlet → HandlerAdapter → InvocableHandlerMethod
전부 어떤 흐름으로 postHandle이 스킵되는지 더 깊게 설명해줄 수 있어.

아래 과정은 “컨트롤러에서 `sendError()`를 호출했을 때 Spring MVC와 Tomcat이 어떻게 동작하는지”를
**정확한 내부 호출 순서**로 다시 설명한 것이다.

핵심은 이 3단계다:

1. ModelAndView = null
2. sendError 때문에 Spring은 뷰 렌더링을 하지 않음
3. Tomcat이 ERROR 처리를 시작함

이걸 시간 순서대로 풀어보면 이렇게 된다.

---

# 1. 컨트롤러 실행 → sendError 호출

컨트롤러에서 다음과 같은 코드가 있다고 하자.

```java
@GetMapping("/error-404")
public void first404(HttpServletResponse response) throws IOException {
    response.sendError(404);
}
```

여기서 `sendError(404)`가 실행되면 Servlet API 레벨에서 다음 일이 일어난다:

* response 객체에 status=404 설정
* “이 요청은 오류 처리 대상이다”라는 플래그가 내부적으로 표시됨
* 컨테이너(Tomcat)에게 오류 처리 지시
* Spring은 뷰 생성(ModelAndView)을 하지 않아도 되는 상태가 됨
  (왜냐하면 컨트롤러가 직접 응답을 끝냈으니)

---

# 2. Spring MVC 내부에서 ModelAndView는 null이 된다

컨트롤러가 void 반환하거나 sendError를 호출하면
Spring MVC는 “컨트롤러가 뷰를 직접 처리했다”고 간주한다.

그래서 다음 두 가지가 발생한다:

### (1) `RequestMappingHandlerAdapter`는 ModelAndView를 생성하지 않음

→ **ModelAndView = null**

로그에서도 `postHandle`에서 ModelAndView가 null로 찍힌 이유가 이것이다.

### (2) DispatcherServlet은 뷰 렌더링을 하지 않음

DispatcherServlet 내부에는 이런 코드 흐름이 있다:

```java
if (mv != null && !mv.wasCleared()) {
    render(mv);  
}
```

하지만 mv = null 이므로
`render()`가 호출되지 않는다.

즉 Spring MVC는 **절대 404.html을 렌더링하지 않는다.**

뷰는 한 줄도 쓰지 않음.

---

# 3. Spring MVC는 응답을 만들지 않고 그대로 종료

sendError는 컨트롤러가 직접 응답 흐름을 종료한 것이므로
DispatcherServlet은 응답에 쓸 내용이 없고
단순히 Tomcat에게 제어를 넘겨준다.

---

# 4. Tomcat이 ERROR 페이지 처리를 시작

Spring MVC가 sendError를 그대로 통과시키면,
완성된 응답이 없기 때문에
Tomcat은 다음 절차를 자동으로 수행한다.

예를 들어 다음과 같은 ErrorPage 설정이 있다고 하면:

```java
container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404"));
```

Tomcat은 아래 순서로 동작한다:

1. sendError가 호출된 요청을 감지
2. 에러 코드를 확인
3. ErrorPage 매핑에서 404에 대응하는 URL 값 확인
4. `/error-page/404` 로 **forward**
5. forward 요청의 DispatcherType을 ERROR로 설정
6. 스프링 필터/인터셉터, 컨트롤러가 다시 실행됨

Spring MVC는 이제 이 ERROR 요청을 새로운 MVC 요청으로 처리한다.

---

# 5. 새로운 요청(DispatcherType.ERROR)에서 404.html 렌더링

forward된 요청은 이렇게 처리된다:

```
/error-page/404 요청
→ LogFilter (ERROR)
→ LogInterceptor.preHandle (ERROR)
→ ErrorPageController.second404()
→ ModelAndView("404")
→ ViewResolver가 404.html 파일 위치 검색
→ Thymeleaf, JSP, FreeMarker 등으로 렌더링
→ HTML을 브라우저로 반환
```

이 최종 렌더링이 사용자에게 보이는 404 페이지이다.

---

# 최종 요약

## A. 컨트롤러 단계

* sendError(404) 호출
* Spring MVC는 ModelAndView = null
* DispatcherServlet은 render()를 호출하지 않음
* Spring은 HTML을 **하나도** 만들지 않음

## B. Spring MVC 종료 후

* sendError 상태를 감지한 Tomcat이 오류 처리 시작

## C. 오류 처리 단계

* Tomcat이 /error-page/404 로 forward
* DispatcherType.ERROR
* Spring이 ErrorPageController를 실행
* ErrorPageController가 ModelAndView("404") 반환
* Spring ViewResolver가 404.html 렌더링

## 결론

> **404.html은 Spring 컨트롤러(first404)가 아니라
> ErrorPageController(second404)가 렌더링한다.**

원하면 sendError를 호출한 순간 Spring MVC 내부 코드가 어떻게 분기하는지
DispatcherServlet 소스 레벨로 분석해서 보여줄 수 있어.
