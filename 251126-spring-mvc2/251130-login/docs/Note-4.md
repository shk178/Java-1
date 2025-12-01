아래 코드는 **Spring MVC의 HandlerInterceptor**를 이용해 **요청(Request)이 컨트롤러에 도달하기 전에 실행되는 전처리 작업을 수행하는 코드**다.
특히, **요청 하나마다 고유한 식별자(UUID)를 부여해서 로그를 일관되게 추적하기 위한 용도**라고 보면 된다.

---

# 전체 동작 흐름 해설

## 1. preHandle은 언제 호출되나?

* 클라이언트가 서버로 HTTP 요청을 보냄
* **DispatcherServlet → HandlerMapping → Interceptor(preHandle)** 순서로 진행
* 즉, **컨트롤러 메서드가 실행되기 직전**에 호출됨

---

# 코드 한 줄씩 의미

```java
@Slf4j
public class LogInterceptor implements HandlerInterceptor {
```

* 이 클래스는 Interceptor 역할을 수행
* Lombok의 `@Slf4j`로 `log.info(...)` 같은 로깅 사용 가능

---

## ① `String uuid = UUID.randomUUID().toString();`

* 요청마다 **유일한 문자열(식별자)** 생성
* 같은 요청의 로그를 식별하기 위해 사용

---

## ② `String requestURI = request.getRequestURI();`

* 요청한 URL 경로.

    * 예: `/api/users/1`

---

## ③ `request.setAttribute(LOG_ID, uuid);`

* 생성한 UUID를 요청에 저장
* 이후 다른 interceptor, controller, filter에서도 꺼내 쓸 수 있음

예:

```java
String logId = (String) request.getAttribute(LOG_ID);
```

---

## ④ handler가 HandlerMethod인지 확인

```java
if (handler instanceof HandlerMethod) {
    HandlerMethod hm = (HandlerMethod) handler;
}
```

* Spring MVC는 컨트롤러 매핑 정보(handler)를 두 가지 방식으로 담음

    1. **HandlerMethod**: @Controller/@RestController의 메서드
    2. **ResourceHttpRequestHandler**: 정적 리소스(css/js 등)

* 여기서 HandlerMethod인지 확인하는 이유:

    * 필요하면 컨트롤러 메서드 정보, 파라미터, 애노테이션 등을 추출할 수 있도록

---

## ⑤ 로그 출력

```java
log.info("REQUEST [{}][{}][{}]", uuid, requestURI, handler);
```

출력 예시:

```
REQUEST [e1c2f-...] [/api/users] com.example.UserController#getUser
```

즉,

* 어떤 요청인지
* 어느 컨트롤러 메서드로 매핑됐는지
* 요청마다 유일한 식별자를 부여해 로그를 추적

---

# 이 코드가 실제로 하는 일 한 줄 요약

**“들어오는 HTTP 요청마다 UUID를 부여해, 어떤 요청이 어떤 컨트롤러로 들어왔는지 로그로 남기고 흐름을 추적하는 Interceptor”**

---

# 왜 필요한가?

* 서버 로그는 여러 요청이 뒤섞여 순서가 엉킬 수 있음
* 하나의 요청이 필터 → 인터셉터 → 컨트롤러 → 서비스 → 뷰 렌더링을 거칠 때
  **같은 uuid를 공유하면 전체 흐름을 추적하기 쉬움**

그래서 일반적으로 아래처럼 *preHandle → postHandle → afterCompletion* 모두에서 같은 uuid를 출력함.

---

원하면 **필터/인터셉터/AOP 전체 흐름 속에서 이 Interceptor가 어떤 위치인지**,
혹은 **스프링 MVC의 handler와 handlerMethod 차이**,
또는 **uuid를 넣는 더 실무적인 패턴(예: MDC)**까지 더 설명해줄게.

Spring MVC에서 **“handler”**라는 단어가 나오면,
**“이 요청을 처리할 실제 실행 대상(=컨트롤러 메서드 혹은 리소스 처리 객체)”**
를 의미한다.

그런데 Spring MVC는 이 handler를 **두 가지 타입 중 하나**로 들고 있다.

# 1. HandlerMethod (Controller의 메서드)

Spring MVC에서 우리가 흔히 작성하는 다음과 같은 컨트롤러 메서드가 있다:

```java
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        ...
    }
}
```

이 요청(`/users/10`)이 들어오면 Spring은
이 메서드를 다음과 같은 형태로 캡슐화해서 handler로 넘긴다:

```
HandlerMethod(UserController#getUser)
```

즉,
**컨트롤러 객체 + 실행할 메서드 + 메서드 파라미터 정보까지 포함한 객체**가 바로 HandlerMethod다.

### HandlerMethod가 담고 있는 정보

* 어떤 컨트롤러 클래스인지
* 어떤 메서드를 호출할지
* 파라미터 타입, 애노테이션(@RequestParam, @PathVariable 등)
* 리턴 타입
* 메서드에 붙은 애노테이션(@GetMapping, @ResponseBody 등)

그래서 코드에서 `handler instanceof HandlerMethod`로 검사하면,

→ **현재 요청은 컨트롤러의 메서드로 매핑된 요청이구나**
를 알 수 있다.

---

# 2. ResourceHttpRequestHandler (정적 리소스 요청)

만약 요청이

* `/images/logo.png`
* `/css/main.css`
* `/js/app.js`

처럼 **정적 파일**이면 컨트롤러 메서드가 실행될 필요가 없다.

이때 Spring MVC는
**ResourceHttpRequestHandler** 라는 특별한 handler를 사용해
정적 리소스를 읽어서 반환한다.

즉, 이런 요청에서는 handler가 다음처럼 찍힌다:

```
org.springframework.web.servlet.resource.ResourceHttpRequestHandler
```

---

# 왜 handler가 두 가지인가?

## 이유 1: 요청 대상이 다르기 때문

1. 동적 요청 (Controller 메서드 실행)
2. 정적 요청 (리소스 파일 반환)

동적 요청은 HandlerMethod로 처리해야 하고
정적 요청은 ResourceHttpRequestHandler가 처리해야 한다.

## 이유 2: Interceptor에서 구분하기 위함

Interceptor는 모든 요청을 거치므로
정적 리소스 요청까지 다 들어온다.

그래서 다음처럼 분기 처리가 필요할 때가 있다:

```java
if (handler instanceof HandlerMethod) {
    HandlerMethod hm = (HandlerMethod) handler;
    // 컨트롤러 메서드 정보 활용 가능
} else {
    // 정적 리소스 요청
}
```

예를 들면,

* 컨트롤러 메서드의 애노테이션(@LoginRequired 등) 읽기
* 특정 컨트롤러의 파라미터 정보 조회
  이런 기능은 **정적 리소스 요청에는 적용할 수 없기 때문**이다.

---

# 그림으로 정리

```
DispatcherServlet
    → HandlerMapping
        → handler를 찾아서 반환

handler는 2가지 중 하나
    1. HandlerMethod —> Controller 실행
    2. ResourceHttpRequestHandler —> 정적 파일 반환

Interceptor.preHandle(handler) 호출
    handler가 어떤 타입인지 확인 가능
```

---

원하면

* Spring MVC 요청 처리 전체 흐름(DispatcherServlet 중심)
* HandlerMapping이 HandlerMethod를 어떻게 찾는지
* ResourceHttpRequestHandler가 정확히 어떤 로직인지

이런 것들도 더 자세히 이어서 설명해줄게.

핵심은
**“왜 형변환까지 해놓고 아무것도 안 하는 것처럼 보이냐?”**
라는 질문인데, 결론부터 말하면:

**→ 이 인터셉터는 ‘지금 요청이 컨트롤러 메서드인지 확인만 하고 넘어가는 기본 예제’라서 그래.
실무에서는 HandlerMethod 객체로부터 다양한 정보를 꺼내서 활용하기 위해 형변환한다.**

즉, 예제는 최소 형태고,
실제로는 HandlerMethod에서 **메서드와 애노테이션 정보를 쓰기 위함**이다.

---

# 1. 현재 코드에서 형변환한 이유 (예제 코드의 의도)

Spring MVC 기본 예제들은 보통 이런 흐름을 알려주기 위해 HandlerMethod를 꺼내 본다.

```java
if (handler instanceof HandlerMethod) {
    HandlerMethod hm = (HandlerMethod) handler;
}
```

여기까지만 하고 끝나는 이유는
“여기서 HandlerMethod를 활용할 수 있다”라는 포인트를 보여주기 위한 예제라서 그렇다.

즉,
**지금은 아무것도 안 하지만, 여기에 비즈니스 로직을 넣을 수 있다**라는 뜻이다.

---

# 2. 실무에서는 HandlerMethod로 무엇을 하는가?

## ① 어떤 컨트롤러/메서드인지 확인

```java
String controllerName = hm.getBeanType().getSimpleName();
String methodName = hm.getMethod().getName();
log.info("controller={} method={}", controllerName, methodName);
```

이렇게 하면 로그가 매우 명확해진다.

---

## ② 메서드 애노테이션을 읽을 수 있다

예를 들어, 특정 컨트롤러 메서드에 “로그인 필요” 애노테이션이 있다고 하자.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {}
```

컨트롤러:

```java
@GetMapping("/mypage")
@LoginRequired
public String myPage() { ... }
```

인터셉터:

```java
LoginRequired login = hm.getMethodAnnotation(LoginRequired.class);
if (login != null) {
    // 로그인 체크 로직 수행
}
```

이런 식으로
**컨트롤러 메서드에 적용된 애노테이션을 기반으로 권한 체크**를 한다.

---

## ③ API 문서 자동화, AOP-like 기능 구현

* 메서드의 파라미터 타입
* 리턴 타입
* 메서드 수준의 Swagger 애노테이션
* 트랜잭션 여부(@Transactional)
* @ResponseBody 여부

등도 전부 HandlerMethod에서 꺼낼 수 있다.

---

## ④ 요청 핸들러의 메타데이터 분석

예:
“이 메서드가 실행되기 전에 어떤 공통 처리 필요함?”
“이 메서드는 특정 정책 적용해야 함?”
같은 요구사항은 대부분 HandlerMethod 기반으로 해결한다.

---

# 3. return true와의 관계

`preHandle`의 `return true`는
**“다음 단계(컨트롤러 실행)로 진행해도 된다”**라는 의미다.

형변환은 이 return과 무관하다.
형변환을 하는 목적은 한 가지:

→ **“원한다면 handler에 대한 상세 정보를 추출할 수 있다”**

즉, return true는 흐름 제어
HandlerMethod 형변환은 정보 분석용
서로 역할이 다르다.

---

# 결론

`HandlerMethod hm = (HandlerMethod) handler;`
이 줄은 예제에서는 “보여주기용”이지만, 실제로는 아래와 같은 작업을 위해 필수다.

* 컨트롤러 클래스/메서드 이름 확인
* 메서드 애노테이션 읽기
* 권한 체크 로직
* 공통 전처리(파라미터 검증, user-agent 기반 정책 등)
* API별 로깅 전략 수행

예제에서는 단순하지만, 실무에서는 인터셉터에서 가장 많이 사용하는 핵심 기능이다.

필요하면, HandlerMethod가 내부적으로 어떤 구조인지까지 더 풀어서 설명해줄게.

아주 중요한 부분을 정확히 짚었어. **필터(Filter)**와 **인터셉터(Interceptor)** 둘 다 **order**가 있는데, 이 둘은 *동일한 레벨에서 경쟁*하는 게 아니라 **서로 다른 단계**에서 동작하기 때문에 헷갈릴 수 있어. 차근차근 정리해줄게.

---

# 1. 필터(Filter)의 order vs 인터셉터(Interceptor)의 order

## 필터(Filter)의 order

* **Servlet Container 레벨**에서 동작
* 디스패처 서블릿(DispatcherServlet)에 도달하기 *전*에 실행됨
* 여러 개의 필터가 있을 때 어떤 필터가 먼저 실행될지를 정하는 것이 `order`

예시:

* order(1) ⇒ LogFilter
* order(2) ⇒ CheckFilter
  이러면 LogFilter → CheckFilter 순서로 실행됨

### 즉, **필터의 order끼리는 서로 영향을 줌**

인터셉터의 order와는 관련 없음

---

## 인터셉터(Interceptor)의 order

* **Spring MVC 레벨(HandlerMapping 이후)**에서 동작
* 컨트롤러 호출 직전에 실행됨
* 여러 개의 인터셉터가 있을 때 실행 순서를 조정하는 것

예시:

* order(1) ⇒ LogInterceptor
* order(2) ⇒ AuthInterceptor
  이러면 LogInterceptor → AuthInterceptor 순서로 실행됨

### 즉, **인터셉터들끼리만 영향 있음**

필터와 인터셉터의 order는 서로 완전히 독립적

---

## 결론:

* Filter order는 Filter들끼리만
* Interceptor order는 Interceptor들끼리만
* 둘은 전혀 다른 레벨이라 섞여서 비교할 대상이 아님

---

# 2. excludePathPatterns이 하는 일

인터셉터는 기본적으로 지정한 경로 패턴에 대해 모두 실행되는데,
특정 경로에서는 인터셉터가 동작하지 않도록 빼고 싶을 때 사용해.

```java
excludePathPatterns("/css/**", "/*.ico", "/error");
```

### 의미:

### `/css/**`

* `/css/style.css`
* `/css/main/reset.css`
* `/css/common/layout/header.css`
  모두 인터셉터 적용 제외

### `/*.ico`

* 루트에 있는 `.ico` 파일 요청 제외
* 주로 파비콘 요청 제외

### `/error`

* 스프링 기본 에러 페이지 요청 제외
* 인터셉터가 에러 처리 흐름에 간섭하면 예기치 않은 문제가 생길 수 있으므로 보통 제외함

---

# 3. 왜 필터에는 exclude가 없고, 인터셉터에는 exclude가 있을까?

### 이유는 단순해:

### Filter는 Servlet Container 레벨이기 때문

* Spring MVC 패턴이 적용되지 않음
* URL 패턴 기반 필터링만 가능 (`addUrlPatterns`)

### Interceptor는 Spring MVC HandlerMapping 이후이기 때문

* 패턴 매칭이 더 강력함
* 단순 prefix가 아니라 스프링이 가진 **패턴 전략(/**, /*.ico 등)**을 사용 가능
* 따라서 특정 경로를 아주 세밀하게 제외 가능

---

# 4. 전체 실행 순서 정리

요청이 들어오면 실제 순서는 아래처럼 된다:

```
[필터 1(LogFilter)]
   ↓
[필터 2(CheckFilter)]
   ↓
[DispatcherServlet]
   ↓
[인터셉터 1(LogInterceptor) — preHandle]
   ↓
[인터셉터 2(다른 인터셉터) — preHandle]
   ↓
[Controller 호출]
   ↓
[인터셉터 2 — postHandle]
   ↓
[인터셉터 1 — postHandle]
   ↓
[View Render]
   ↓
[인터셉터 2 — afterCompletion]
   ↓
[인터셉터 1 — afterCompletion]
   ↓
[필터 2 — doFilter 후처리]
   ↓
[필터 1 — doFilter 후처리]
```

**필터 → 인터셉터 → 컨트롤러**
순서는 항상 이렇게 고정됨.

---

# 요약

* Filter의 order는 Filter끼리만, Interceptor의 order는 Interceptor끼리만 적용됨
* excludePathPatterns는 특정 URL을 인터셉터 동작 대상에서 제외함
* 필터와 인터셉터는 동작 레이어가 다르므로 서로 영향을 주지 않음
* 필터는 Servlet 레벨, 인터셉터는 Spring MVC 레벨

---

필요하다면:

* 필터 vs 인터셉터 vs AOP 단계별 흐름 다시 도식화해서 보여줄게
* excludePathPatterns의 패턴 규칙 더 자세히 설명해줄게

이 코드는 **스프링 MVC의 HandlerMethodArgumentResolver**를 직접 구현한 클래스야.
목적은 **컨트롤러 메서드 파라미터에 로그인된 회원 객체(Member)를 자동으로 주입**해주는 것.

정리하자면:

> 컨트롤러에서
> `@Login Member member`
> 이런 식으로 파라미터를 선언하면,
> 세션에서 로그인 회원 정보를 꺼내서 자동으로 넣어주는 기능을 한다.

전체 구조를 단계별로 설명해줄게.

---

# 1. HandlerMethodArgumentResolver란?

스프링은 컨트롤러 메서드 파라미터를 바인딩할 때,
단순 타입(String, int) 말고도
`@PathVariable`, `@RequestParam`, `@ModelAttribute`, `@RequestBody` 등 다양한 방식으로 값을 넣어줄 수 있어.

그리고 이렇게 **파라미터가 무엇인지 보고 어떤 값을 넣어줄지 결정하는 객체**가 바로
**HandlerMethodArgumentResolver**야.

우리는 이걸 직접 구현해서 커스텀 파라미터 주입을 만들 수 있어.

---

# 2. supportsParameter()

```java
@Override
public boolean supportsParameter(MethodParameter methodParameter) {
    boolean hasLoginAnnotation = methodParameter.hasParameterAnnotation(Login.class);
    boolean hasMemberType = Member.class.isAssignableFrom(methodParameter.getParameterType());
    return hasLoginAnnotation && hasMemberType;
}
```

이 메서드는 다음을 판단함:

1. 파라미터에 `@Login` 어노테이션이 붙어 있는가
2. 파라미터 타입이 Member인가

즉,

```java
public String home(@Login Member member)
```

⇒ 여기 있는 `member` 파라미터는 이 Resolver가 처리할 대상임.

만약:

```java
public String test(@Login String name)
```

⇒ Member 타입이 아니므로 처리 대상 아님.

즉, **필요한 파라미터에만 동작하도록 조건을 제한하는 메서드**.

---

# 3. resolveArgument()

```java
@Override
public Object resolveArgument(MethodParameter methodParameter,
                              ModelAndViewContainer modelAndViewContainer,
                              NativeWebRequest nativeWebRequest,
                              WebDataBinderFactory webDataBinderFactory) throws Exception {
    HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
    HttpSession session = request.getSession(false);
    if (session == null) {
        return null;
    }
    return session.getAttribute(SessionConst.LOGIN_MEMBER);
}
```

이 메서드가 실제 값을 만들어 반환함.

### 흐름:

1. HTTP 요청 객체를 꺼냄
2. 세션을 꺼냄 (`getSession(false)` → 없으면 null)
3. 세션이 없으면 로그인 안 된 상태 → null 반환
4. 세션에 저장된 로그인 회원 정보를 그대로 반환

즉, **컨트롤러 파라미터에 주입할 객체를 세션에서 가져와서 반환**하는 것.

---

# 4. 컨트롤러에서 어떻게 사용되나?

이런 컨트롤러가 있다고 보자:

```java
@GetMapping("/home")
public String home(@Login Member loginMember, Model model) {
    if (loginMember == null) {
        return "home";
    }
    model.addAttribute("member", loginMember);
    return "loginHome";
}
```

여기서 스프링은 다음을 한다:

* 파라미터를 확인
* `@Login` 붙어있고 타입이 Member인가?
  → supportsParameter()에서 true 반환
* resolveArgument() 호출
* 세션에서 로그인 정보를 꺼냄
* loginMember 파라미터에 넣어줌

즉, **세션 접근 코드가 컨트롤러에서 사라지고 자동 주입이 됨**

---

# 5. 왜 이런 구조를 사용하는가?

### 장점 1. 컨트롤러가 깔끔해짐

컨트롤러에 매번 이런 코드 넣을 필요 없음:

```java
HttpServletRequest request;
HttpSession session = request.getSession(false);
Member loginMember = (Member) session.getAttribute("loginMember");
```

### 장점 2. 인증 주입이 표준화됨

영역별 중복 제거 가능.

### 장점 3. AOP 구조에 적합

결국 인증 정보는 전역으로 사용되므로,
특정 어노테이션 + 특정 타입에 따라 자동 주입하는 방식이 매우 좋음.

---

# 6. 실제 동작 요약

정리하면 이 Resolver는 이렇게 일함:

1. 파라미터가 `@Login` 이 붙었는가
2. 타입이 Member인가
3. 둘 다 맞으면 세션에서 로그인 회원을 꺼내서 파라미터에 넣어줌

즉,
**로그인 회원을 컨트롤러 파라미터에 자동으로 주입하는 커스텀 기능**을 만든 것.

---

원하면 이 Resolver가 실제로 스프링 MVC에 등록되는 설정(WebMvcConfigurer 코드)까지 흐름을 이어서 설명해줄게.

아래에서 **LoginMemberArgumentResolver가 실제로 스프링 MVC 내부에서 언제, 어떻게 등록되고 호출되는지** 전체 흐름을 정확하고 깊게 설명해줄게.

필요한 이유, DispatcherServlet 내부 동작, HandlerAdapter 연동 구조까지 실제 스프링 MVC 호출 순서 기반으로 정리해준다.

---

# 1. LoginMemberArgumentResolver를 스프링 MVC에 등록하는 코드

일단 WebMvcConfigurer에 다음과 같은 메서드를 추가해야 해.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }
}
```

이 한 줄이 핵심이다.

**스프링에게 “내가 만든 ArgumentResolver를 MVC 파이프라인에 넣어라”**라고 등록하는 과정이다.

---

# 2. 스프링 MVC 요청 처리 전체 구조 (핵심 흐름)

요청 흐름을 전체적으로 보면 이렇게 된다:

```
DispatcherServlet
    ↓
HandlerMapping
    ↓
HandlerAdapter
    ↓
HandlerMethodArgumentResolver(여기에서 네 resolver가 동작)
    ↓
Controller 호출
```

여기서 중요한 부분은 **HandlerAdapter → HandlerMethodArgumentResolver** 단계다.

---

# 3. 실제 내부 흐름: ArgumentResolver가 언제 호출되는가?

스프링 MVC는 `HandlerMethod`(컨트롤러 메서드)를 호출하기 전에
해당 메서드의 파라미터 하나하나를 분석하면서 다음 순서대로 진행한다.

---

## 3.1 HandlerMethodArgumentResolverComposite

스프링은 ArgumentResolver들을 모두 모아서 **“리스트”**로 가지고 있다.
이걸 `HandlerMethodArgumentResolverComposite`라고 한다.

안에 들어 있는 기본 Resolver 예:

* RequestParamMethodArgumentResolver
* PathVariableMethodArgumentResolver
* RequestBodyMethodArgumentResolver
* ModelAttributeMethodArgumentResolver
* ServletRequestMethodArgumentResolver
* …

그리고 **당신이 직접 등록한 LoginMemberArgumentResolver도 여기에 추가됨**.

---

## 3.2 supportsParameter() 검사 과정

Controller 메서드의 파라미터가 예를 들어:

```java
public String home(@Login Member member)
```

스프링은 이 파라미터를 처리하기 위해 Resolver 목록을 순서대로 돌면서 다음을 확인한다.

```
각 Resolver에게 묻는다:
supportsParameter(이 파라미터) = true ?
```

즉:

1. RequestParamResolver → false
2. ModelAttributeResolver → false
3. PathVariableResolver → false
4. …
5. LoginMemberArgumentResolver → true

supportsParameter() 과정에서 조건을 만족하는 Resolver를 찾으면
그 Resolver에게 resolveArgument() 호출 권한이 넘어간다.

---

# 4. resolveArgument() 실행

당신이 만든 메서드가 호출됨:

```java
@Override
public Object resolveArgument(...)
```

여기서 실행되는 작업:

1. HttpServletRequest 가져옴
2. 세션 꺼냄
3. 세션에 LOGIN_MEMBER 속성 있는지 확인
4. 있으면 Member 객체 반환
5. 없으면 null 반환

즉, 그 값을 파라미터 값으로 쓰게 된다.

컨트롤러가 다음처럼 보이면:

```java
@GetMapping("/home")
public String home(@Login Member loginMember) {
    // loginMember는 ArgumentResolver가 넣어준 객체
}
```

loginMember는 resolveArgument()의 반환 값이 된다.

---

# 5. 컨트롤러는 그 값을 그대로 파라미터로 받게 된다

스프링은 모든 파라미터를 Resolver로부터 받은 값으로 채우고
최종적으로 컨트롤러 메서드를 호출한다.

즉:

```
loginMember 파라미터 = session.getAttribute(SessionConst.LOGIN_MEMBER)
```

이 로직이 컨트롤러 내부에 필요 없다.

---

# 6. 왜 이렇게 동작하게 설계했는가?

핵심 이유는 다음 세 가지다.

## ① 인증 회원 정보를 컨트롤러에서 중복으로 꺼내지 않기 위해

컨트롤러마다 매번:

```java
HttpSession session = request.getSession(false);
Member member = (Member) session.getAttribute("loginMember");
```

이런 코드 넣는 건 매우 비효율적이고 중복이 심함.

## ② 타입 + 어노테이션 기반으로 조건을 깔끔하게 걸 수 있음

supportsParameter() 조건을 보면:

```java
@Login && Member 타입
```

이 조건이 매우 직관적이고 안전함.

## ③ 스프링 MVC 확장 구조의 철학과 맞음

스프링은 “핸들러 파라미터 또한 확장 가능해야 한다”는 철학을 갖고 있음.

즉, 스프링 기본 동작을 그대로 쓰면서 인증 같은 부가 기능을 자연스럽게 연결.

---

# 7. 전체 흐름 그림으로 요약

```
[클라이언트 요청]
       ↓
[DispatcherServlet]
       ↓
[HandlerMapping → 컨트롤러/메서드 찾음]
       ↓
[HandlerAdapter]
       ↓
[ArgumentResolverComposite (여러 Resolver)]
       ↓
supportsParameter 검사
       ↓
LoginMemberArgumentResolver 선택됨
       ↓
resolveArgument() → 세션에서 Member 꺼냄
       ↓
컨트롤러 파라미터로 전달
       ↓
컨트롤러 실행
```

---

# 8. 이 Resolver가 동작하는 전체 코드는 어떻게 구성되어야 하나?

정확한 구성은 다음 세 파일이다.

### ① @Login 어노테이션

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Login {
}
```

### ② LoginMemberArgumentResolver

(이미 작성한 코드)

### ③ WebMvcConfigurer에 등록

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }
}
```

이 세 가지가 있어야 전체가 동작한다.

---

# 필요한 경우 확장해서 설명해줄 수 있는 목록

* 실제 ArgumentResolver가 동작하는 순서를 디버깅하는 방법
* 스프링이 제공하는 기본 Resolver 전체 목록
* HandlerMethodArgumentResolverComposite의 내부 구조
* 세션 기반 로그인뿐 아니라 JWT나 API 인증도 Resolver로 처리하는 패턴
* HandlerMethodArgumentResolver vs @ModelAttribute vs @RequestBody 비교

원하는 내용 있으면 이어서 설명할게.
