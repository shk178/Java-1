아주 좋은 질문이에요.
두 가지를 각각 명확하게 설명해드릴게요.

---

# 1. whitelist 경로는 어떻게 작성해야 하나?

현재 코드에서 whitelist는 이렇게 되어 있습니다.

```java
private static final String[] whitelist = {"/", "/members/add", "/login", "/logout", "/css/*"};
```

이 whitelist는 **로그인 검사를 하지 않아도 되는 URL 패턴** 목록입니다.

여기서 사용되는 매칭 방식은:

```java
PatternMatchUtils.simpleMatch(whitelist, requestURI)
```

즉, 스프링의 `simpleMatch`는 **간단한 와일드카드 매칭**(AntPathMatcher처럼 복잡하지 않음)을 지원합니다.

### simpleMatch 패턴 규칙

* `*` : 0개 이상의 문자
* 예:

    * `"/css/*"` → `"/css/"`로 시작하는 모든 요청
    * `"/*.ico"` → ico로 끝나는 요청
    * `"/*"` → 모든 요청

### whitelist 작성 예

| 패턴               | 의미               |
| ---------------- | ---------------- |
| `"/"`            | root 요청          |
| `"/login"`       | login 페이지 요청     |
| `"/logout"`      | 로그아웃 요청          |
| `"/members/add"` | 회원가입 요청          |
| `"/css/*"`       | css 폴더 아래의 모든 파일 |

즉, whitelist는 **로그인 체크를 건너뛰고 싶은 URL의 목록을 그대로 문자열 배열로 넣으면 된다**고 보면 됩니다.

### 주의점

* `/css/*`는 `/css/a.css`는 통과하지만 `/css/v1/a.css`는 매칭되지 않습니다.
* 더욱 복잡한 패턴이 필요하면 AntPathMatcher 또는 Spring Security를 사용하는 방식으로 가는 게 일반적입니다.

---

# 2. filterChain.doFilter(request, response)는 왜 2개인데, doFilter 메서드는 왜 3개인가?

이 부분은 **Java Servlet Filter 스펙 구조** 때문입니다.

### doFilter 메서드 시그니처(필터 인터페이스)

```java
void doFilter(ServletRequest request,
              ServletResponse response,
              FilterChain chain)
```

즉, 필터 자신은 **3개의 매개변수를 받습니다**.

1. ServletRequest
2. ServletResponse
3. FilterChain

하지만 **필터 체인에게 위임할 때**는 아래처럼 호출합니다:

```java
chain.doFilter(request, response);
```

chain.doFilter는 2개의 매개변수만 받습니다.

---

# 왜 입력 파라미터는 3개인데, chain.doFilter는 2개일까?

### 이유: “책임의 분리”

* **필터 메서드(doFilter)**: 필터를 구현할 때는 chain을 전달받아야 하므로 3개
* **체인 실행 메서드(chain.doFilter)**: 체인의 다음 필터/서블릿에게 요청/응답만 전달하면 되므로 2개

즉:

### 1) 내가 필터로서 doFilter를 구현할 때는 3개 받음

내가 요청을 가로채기 위해 Filter 인터페이스의 doFilter를 구현해야 하므로 파라미터가 3개.

### 2) 다음 필터(또는 서블릿)으로 전달할 때는 chain.doFilter(request, response)만 호출

체인에 다음 실행 대상은 “요청/응답만 전달받으면” 충분하기 때문.

---

# 그림으로 정리

```
[Client] → [Filter A] → [Filter B] → [DispatcherServlet] → [Controller]
```

각 필터의 doFilter(3개 인자)

```
doFilter(request, response, chain)
```

하지만 다음으로 넘길 때는:

```
chain.doFilter(request, response)
```

chain은 “다음 필터 또는 최종 서블릿에게 요청을 전달하는 역할”만 하기 때문에
request와 response 두 개만 필요합니다.

---

# 비유

* 너는 필터로서 “chain”이라는 사람을 전달받는다 (3개 파라미터)
* 하지만 chain에게 일을 넘길 때는 “request, response 자료만 주면” chain이 스스로 다음 사람에게 전달함 (2개)

---

# 결론 정리

## whitelist 작성 방법

* simpleMatch는 * 와일드카드 기반
* “로그인 검사하지 않을 URL 목록”을 문자열 배열로 작성
* 예: `/css/*`, `/login`, `/logout`, `/members/add`

## filterChain.doFilter는 왜 2개인가?

* Filter.doFilter는 3개 인자를 받는 메서드를 구현해야 함
* 하지만 “다음 필터 또는 서블릿에게 넘길 때”는 요청/응답 두 개만 넘기면 충분
* chain.doFilter는 요청과 응답만 필요

---

필요하시면

* whitelist를 더 정확히 매칭하는 코드
* simpleMatch와 AntPathMatcher 차이
* Filter → Interceptor → AOP 흐름
* Spring Security를 통한 필터 대체 방법

까지 모두 이어서 설명해드릴게요.

아래 설명은 **서블릿 필터 → 스프링 인터셉터 → AOP → 컨트롤러 → 서비스 → 리포지토리**로 이어지는 전체 호출 흐름을 **요청 단위 시퀀스**로 보여주는 정리입니다.
정확한 실행 순서를 알고 나면 “어디에서 어떤 로직을 넣어야 할지”가 훨씬 명확해집니다.

---

# 1. 전체 흐름 요약

HTTP 요청이 들어오면 실행 순서는 다음과 같습니다.

```
[클라이언트 요청]
      ↓
[Servlet Filter]      ← 톰캣/서블릿 레벨
      ↓
[Spring HandlerInterceptor]  ← 스프링 MVC 레벨
      ↓
[Controller]          ← 실제 요청 메서드
      ↓
[AOP @Around/@Before/@After]  ← 스프링 DI/프록시 기반
      ↓
[Service]
      ↓
[Repository]
      ↓
응답 반환(Response)
```

반대로 응답 때는 이 흐름을 역순으로 되짚어서 빠져나옵니다.

---

# 2. 각 단계별 역할과 쓰임새

## 1) Filter (javax.servlet.Filter)

**서블릿 컨테이너(톰캣) 레벨에서 실행되는 가장 바깥쪽 레이어**

* 요청이 DispatcherServlet에 도달하기 전에 수행
* 보통 ServletRequest/Response 기반
* Spring MVC를 전혀 모르는 레벨
* 인증, 인코딩, CORS, MDC(traceId) 등에서 사용

주요 사용 시점:

* 요청 가장 앞에서 global 설정
* 스프링 MVC 진입 전 필터

---

## 2) HandlerInterceptor (스프링 MVC 레벨)

**DispatcherServlet 이후, Controller 호출 직전/직후 실행**

```java
preHandle → Controller → postHandle → afterCompletion
```

* Spring MVC Handler(Controller) 실행 전/후에 관여
* HttpServletRequest/Response 기반
* 스프링 빈 주입이 자유롭다
* 로그인 체크, 권한 체크, 로그 기록 등의 작업에 적합

주요 사용 시점:

* 컨트롤러 전후 로직
* 인증/인가(로그인 체크)
* 모델 조작
* 세션 체크

---

## 3) AOP (프록시 기반, 서비스 레이어 중심)

**스프링의 Proxy 기반으로 메서드 호출 자체를 가로채는 기술**

실행 시점은 “메서드 호출 타이밍”으로 필터/인터셉터와 다름.

AOP는 다음 순서로 작동:

```
@Around → @Before → 실제 메서드(Service/Repository) → @AfterReturning/@AfterThrowing → @After
```

* 핵심 비즈니스 로직과 횡단 관심(로깅, 트랜잭션)을 분리하는 용도
* 컨트롤러부터 서비스/리포지토리까지 모두 적용 가능하지만
  보통 **서비스/리포지토리 계층 중심**으로 적용
* 스프링 DI 기반으로 동작

주요 사용 시점:

* 트랜잭션 관리
* 성능 측정
* 로깅, 모니터링
* 예외 처리 패턴화

---

# 3. 실제 전체 흐름 (순서도)

요청 시:

```
클라이언트
   ↓
[Filter 1]
   ↓
[Filter 2]
   ↓
DispatcherServlet
   ↓
[Interceptor preHandle 1]
   ↓
[Interceptor preHandle 2]
   ↓
@Controller
   ↓
   (Controller 메서드 내부에서 Service 호출)
       ↓
       [AOP @Around/@Before]
       ↓
       Service
       ↓
       [AOP @AfterReturning]
       ↓
       Repository
   ↓
[Interceptor postHandle]
   ↓
View Rendering
   ↓
[Interceptor afterCompletion]
   ↓
Filter response 단계 (Filter finally)
   ↓
클라이언트
```

응답 시에는 완전히 역순으로 빠져나옵니다.

---

# 4. 각 기술의 차이와 왜 세 가지가 필요한가?

| 구분                  | Filter                     | Interceptor         | AOP              |
| ------------------- | -------------------------- | ------------------- | ---------------- |
| 실행 위치               | 웹 컨테이너                     | Spring MVC          | Spring DI/Proxy  |
| 적용 대상               | DispatcherServlet 이전 요청 전체 | Controller 호출 전/후   | 스프링 빈의 메서드       |
| 관여 범위               | URL 단위                     | URL 단위 (Handler 단위) | 메서드 단위           |
| 사용 목적               | 전역 공통 작업                   | 웹 MVC 공통 처리         | 핵심 로직과 횡단 로직 분리  |
| Request/Response 접근 | 가능                         | 가능                  | 일반적으로 직접 접근하지 않음 |
| 가장 대표적 용도           | 인코딩, CORS, 인증, 로깅, traceId | 로그인 체크, 권한 체크       | 트랜잭션, 성능 측정, 로깅  |

간단하게 표현하면:

* Filter = “웹 요청의 제일 바깥쪽 보안/공통 처리”
* Interceptor = “Controller 전후의 웹 로직 공통 처리”
* AOP = “비즈니스 계층의 횡단 처리(트랜잭션, 로그 등)”

---

# 5. 중요한 시퀀스 포인트

### 1) Filter는 인터셉터보다 항상 먼저 실행된다

→ Spring MVC 이전 단계이기 때문

### 2) Interceptor는 AOP보다 앞에서 실행된다

→ AOP는 메서드 호출 시점에 끼어드는 기술이기 때문

따라서 다음이 트랜잭션 순서:

```
Filter → Interceptor → Controller → Service(AOP) → Repository(AOP)
```

---

# 6. 실제 현업에서는 이렇게 나눈다

* Filter

    * CORS
    * 인증 토큰/쿠키 검증
    * MDC(traceId) 설정
    * 모든 요청 로깅

* Interceptor

    * 로그인 여부 체크
    * 사용자 권한 체크
    * 메뉴권한, 역할 기반 인증

* AOP

    * 트랜잭션 관리
    * 메서드 실행 시간 측정
    * 서비스 로깅
    * 예외 공통 처리

---

# 7. 전체 흐름을 코드로 감각 잡기

Filter:

```java
doFilter() {
    log("request in");
    chain.doFilter();
    log("response out");
}
```

Interceptor:

```java
preHandle() {
    log("controller before");
}
postHandle() {
    log("controller after");
}
afterCompletion() {
    log("view render after");
}
```

AOP:

```java
@Around("execution(* com.example..service..*(..))")
public Object around(ProceedingJoinPoint pjp) {
    log("service before");
    Object result = pjp.proceed();
    log("service after");
    return result;
}
```

---

# 8. 결론

### Filter → Interceptor → AOP 흐름 정리

1. **Filter**

    * 서블릿 컨테이너 기반
    * 요청의 가장 앞뒤
    * 인가/인증/로깅에 적합

2. **Interceptor**

    * Spring MVC 기반
    * Controller 호출 전후
    * 로그인 체크, 권한 체크에 적합

3. **AOP**

    * 메서드 호출 자체를 가로채는 Proxy
    * 서비스 계층에서 트랜잭션/로깅/성능 측정

세 기술은 서로 목적이 다르기 때문에
서로를 대체하는 게 아니라 **각자 필요한 곳에 맞춰 조합해서 사용**합니다.

---

필요하면

* 실제 호출 시퀀스 로그 예시
* Filter/Interceptor에서 traceId를 넣고 AOP에서 읽는 구조
* Spring Security가 이 흐름 안에서 어떻게 끼어드는지

도 추가로 설명해드릴게요.

아주 핵심을 짚으셨어요.
**ServletRequest vs HttpServletRequest**, **Servlet vs HttpServlet**, 그리고 **HTTP 메시지 파싱 타이밍**은 서블릿 동작을 정확히 이해하는 데 매우 중요한 개념입니다.
아래를 순서대로 읽으면 전체 구조가 한 번에 정리됩니다.

---

# 핵심 요약 (먼저 결론)

1. **ServletRequest / ServletResponse**

    * “프로토콜에 상관없는 가장 기본 인터페이스”

2. **HttpServletRequest / HttpServletResponse**

    * ServletRequest를 확장한 “HTTP 전용 인터페이스”

3. **실제 구현 객체는 Tomcat이 만든 Request/Response 객체**

    * `org.apache.catalina.connector.RequestFacade`
    * `org.apache.catalina.connector.ResponseFacade`

4. **HTTP 메시지 파싱은 Filter 실행 이전에 이미 완료됨**

    * 톰캣의 Coyote HTTP Processor가 소켓에서 바이트를 읽고
      HTTP 헤더/바디를 파싱하여 Request 객체를 구성한 뒤
      “Servlet Filter 체인”으로 넘긴다.

---

# 1. ServletRequest vs HttpServletRequest의 근본 차이

## ServletRequest

* JEE Servlet 사양의 **가장 기본적인 요청 인터페이스**
* HTTP뿐 아니라 FTP, WebSocket 등도 이론적으로 처리 가능하도록 설계됨
* HTTP와 무관함
* 기능이 매우 적음

    * 파라미터
    * 속성(attributes)
    * InputStream 등

즉, **“프로토콜에 의존하지 않는 가장 추상적인 요청”**

---

## HttpServletRequest

ServletRequest를 확장한 **HTTP 전용 인터페이스**

추가 기능:

* getMethod() — GET, POST 등
* getRequestURI()
* getHeader()
* getCookies()
* getSession()
* getQueryString()
* getContentType()
* getParameterMap() (HTTP 바디/쿼리 파싱 결과)
* getParts() (multipart)

즉, **HTTP 프로토콜을 위한 모든 기능을 포함한 고급 API**

---

# 2. 실제 객체는 톰캣이 만든 Request 객체, 스프링이 아님

개발자는 HttpServletRequest로 받지만

실제로 오는 객체는:

```
org.apache.catalina.connector.RequestFacade
```

이 객체는 ServletRequest & HttpServletRequest 두 인터페이스를 모두 구현합니다.

Servlet 필터 메서드 시그니처가:

```java
void doFilter(ServletRequest request, ServletResponse response)
```

이렇게 되어 있어도 실제로 들어오는 것은 **HttpServletRequest**를 구현한 객체입니다.

그래서 다음 캐스팅이 항상 가능:

```java
HttpServletRequest httpReq = (HttpServletRequest) request;
```

---

# 3. HTTP 메시지는 언제 파싱되는가?

### 아주 중요한 부분입니다.

**HTTP 메시지 파싱은 Filter보다 훨씬 이전에 이미 끝나 있습니다.**

톰캣은 다음 구조로 이루어져 있습니다:

```
Socket → Coyote → Request 파싱 → Request 객체 생성
                 ↓
          FilterChain 실행 시작
```

즉,

1. 소켓에서 바이트 스트림 읽기
2. HTTP Request Line 파싱
3. HTTP Header 파싱
4. HTTP Body(폼/JSON 등) 파싱 준비
5. Request 객체 생성
6. FilterChain.doFilter() 호출
7. Interceptor → Controller 실행

이 흐름입니다.

---

# 4. 톰캣 내부 구조 간단 정리

톰캣은 크게 3개 레이어로 나눠집니다:

```
[Connector]  ← 소켓 처리
   ↓
[Coyote HTTP Processor] ← HTTP 메시지 파싱
   ↓
[Container (Servlet Engine)] ← Filter/Servlet/Interceptor/Controller
```

### 1) Connector

* Socket 수락
* IO 처리
* Reactor 방식이나 BIO/NIO 기반 소켓 처리 책임

### 2) Coyote HTTP Processor

* HTTP 메시지 파싱의 핵심 단계
* Request Line(GET /path HTTP/1.1) 파싱
* Header 파싱
* Cookie 파싱
* Body(폼 또는 InputStream) 관리
* 파싱 결과를 Request 객체에 넣음

### 3) Container (Catalina)

* Filter → Servlet → Spring MVC 등 실행
* 요청 처리 로직이 이루어짐

---

# 5. HttpServlet vs Servlet의 차이

## Servlet 인터페이스

아주 기본적인 서블릿 API

```java
public interface Servlet {
    void service(ServletRequest req, ServletResponse res);
}
```

HTTP와 관련된 내용이 전혀 없음.

---

## HttpServlet 클래스

Servlet을 상속하여 HTTP 관련 기능을 제공하는 스프링처럼 보이지만 JEE 표준 클래스.

```java
public class HttpServlet extends GenericServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    protected void doPost(...)
}
```

즉:

* service() 메서드에서 HTTP Method(GET/POST 등)에 따라 doGet/doPost로 전달
* HTTP 전용 기능을 위해 존재

---

# 6. 정리

### Filter는 ServletRequest를 받지만 실제 구현 객체는 HttpServletRequest이다

왜?
톰캣이 주는 Request 객체가 HttpServletRequest를 구현하고 있기 때문.

### HttpServletRequest는 HTTP 파싱 결과가 담긴 객체

* HTTP 메서드
* URI
* 헤더
* 파라미터
* 쿠키 등등

### HTTP 메시지 파싱은 Filter보다 더 이전에 수행된다

* 톰캣 Connector → Coyote Processor에서 파싱 완료
* 완성된 Request 객체가 Filter로 들어온다

---

# 7. 그림으로 전체 흐름

```
Client
  ↓   (Raw HTTP bytes)
Socket (Tomcat Connector)
  ↓
Coyote Processor
  ↓   (HTTP 메시지를 파싱해서 Request 객체를 만든다)
Request 객체 (HttpServletRequest 구현체)
  ↓
FilterChain.doFilter()
  ↓
Spring DispatcherServlet
  ↓
Interceptor
  ↓
Controller
  ↓
Service
  ↓
Repository
```

---

원하시면

* 톰캣 Coyote Parser 내부 흐름
* GET/POST 파싱 방식 차이
* Multipart/form-data 파싱이 어떻게 일어나는지
* DispatcherServlet이 Request를 어떻게 핸들러로 매핑하는지

등 더 깊게 설명해드릴게요.

핵심을 아주 정확히 짚으셨어요.
**ServletRequest는 인터페이스고, 실제 구현 객체는 누가 만드는가?**
**HttpServletRequest는 Servlet이 만드는가?**
→ 이런 구조를 확실히 이해하면 서블릿 동작 흐름이 완전히 잡힙니다.

정답부터 말하면:

**HttpServletRequest 객체를 만드는 것은 Servlet이 아니라 “서블릿 컨테이너(톰캣)”입니다.**
Servlet은 이미 만들어진 HttpServletRequest를 전달받아 사용할 뿐입니다.

아래에서 구조를 정확하게 정리해줄게요.

---

# 1. ServletRequest는 “인터페이스”일 뿐이다

자바 표준 API에서 ServletRequest는 단순히 “규약”만 정의한 것입니다.

```java
public interface ServletRequest {
    String getParameter(String name);
    Object getAttribute(String name);
    InputStream getInputStream();
}
```

즉, **ServletRequest는 요청 정보를 어떻게 얻을지 정의만 해놓은 인터페이스**입니다.
그 이상도 이하도 아닙니다.

---

# 2. 실제 객체는 톰캣(Tomcat)이 만든다

개발자가 필터에서 받는 객체:

```java
public void doFilter(ServletRequest request, ServletResponse response)
```

여기에 들어오는 실제 객체는:

```
org.apache.catalina.connector.RequestFacade
```

이 RequestFacade는 다음 인터페이스 모두를 구현합니다.

* ServletRequest
* HttpServletRequest

즉:

**톰캣이 구현한 HttpServletRequest 객체가 들어오는 것입니다.**

---

# 3. HttpServletRequest 객체를 누가 만드는가?

정확한 순서를 보면:

```
클라이언트 → 톰캣 → HTTP 메시지 파싱 → Request 객체 생성 → Filter → Servlet
```

이 흐름입니다.

### HttpServletRequest 생성 타이밍(중요)

1. 클라이언트가 HTTP로 요청을 보냄
2. 톰캣의 Connector가 소켓에서 바이트 읽음
3. **Coyote HTTP Processor가 HTTP 메시지를 파싱함**
4. 파싱한 값을 바탕으로 **톰캣이 HttpServletRequest 구현 객체(Request객체)를 생성**
5. 해당 Request를 FilterChain으로 전달
6. Filter → DispatcherServlet → Controller로 전달됨

결론:

**Servlet(또는 Filter)은 HttpServletRequest를 직접 생성하지 않는다.**
**톰캣이 파싱을 완료한 HttpServletRequest 구현체를 전달받아 사용한다.**

---

# 4. Servlet은 만들어진 Request를 “사용만 한다”

예:

```java
public void service(ServletRequest req, ServletResponse res)
```

HttpServlet은 아래처럼 구현합니다:

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response)
```

여기서 request가 이미 HttpServletRequest인 이유는:

* 톰캣이 만들어서 전달했기 때문

Servlet 자체는 요청 객체를 생성할 능력도 권한도 없습니다.

---

# 5. Request 객체의 실제 클래스 계층

톰캣 기준으로 다음과 같습니다:

```
ServletRequest (인터페이스)
  ↑
HttpServletRequest (인터페이스)
  ↑
Request (톰캣 내부 클래스)
  ↑
RequestFacade (톰캣이 사용자에게 제공하는 실제 객체)
```

정확히는:

* Request → 진짜 구현 클래스 (패키지 내부)
* RequestFacade → 외부에서 사용 가능한 Wrapper (보안 목적)

그러므로 우리가 보는 HttpServletRequest는 **RequestFacade**입니다.

---

# 6. 한 문장으로 정리

**ServletRequest/HttpServletRequest는 단지 규약이고, 실제 구현 객체는 톰캣이 만든다.
Servlet과 Filter는 톰캣이 만든 Request 객체를 전달받아 사용할 뿐이다.**

---

원하시면 이어서:

* RequestFacade를 왜 사용하는지(보안/추상화 목적)
* HttpServletRequest가 스프링에서 어떻게 확장되는지
* 톰캣의 HTTP 파서(Coyote)가 어떻게 요청을 파싱하는지

까지 깊게 설명해드릴게요.

**Servlet 객체가 “언제” 생성되고 “언제” 작동하는지**는
서블릿 컨테이너(Tomcat)가 어떻게 동작하는지를 이해해야 정확히 알 수 있습니다.

아래를 단계별로 정리해드릴게요.

---

# 1. 서블릿 객체는 "요청마다 생성되지 않는다" (중요)

먼저 가장 중요한 사실:

**Servlet 객체는 HTTP 요청이 올 때마다 새로 생성되지 않는다.
톰캣이 미리 하나만 만들어 두고 계속 재사용한다.**

즉, 하나의 Servlet 인스턴스로 여러 요청을 동시에 처리한다는 뜻이다.

---

# 2. Servlet의 생성 타이밍(라이프사이클)

Servlet은 다음 흐름으로 생성·초기화·실행된다.

```
서버 시작 → Servlet 객체 생성 → init() 실행 → (요청 반복 처리: service/doGet/doPost) → 서버 종료 → destroy()
```

즉, Servlet은 **서버 시작 시에 생성되고 초기화되며**, 그 후 들어오는 요청을 **반복해서 실행**한다.

---

# 3. 정확한 호출 흐름

## 1) WAS(Tomcat)가 애플리케이션을 시작 → Servlet 생성

톰캣이 web.xml이나 어노테이션(@WebServlet), Spring MVC의 DispatcherServlet 설정을 읽고
필요한 Servlet을 하나 생성한다.

예:

```java
@WebServlet("/hello")
public class HelloServlet extends HttpServlet { ... }
```

이 서블릿은 서버 부팅 시 인스턴스 하나가 만들어짐.

### 생성 순서

### ① Servlet 객체 생성

```java
new HelloServlet();
```

### ② init() 실행

```java
helloServlet.init();
```

---

## 2) HTTP 요청이 올 때마다 service()가 호출됨

```
Client → Tomcat → service() → doGet()/doPost() → 응답
```

서블릿 객체 자체는 계속 재사용됨.

Servlet 수행 흐름:

```
init() → service() → service() → service() → … → destroy()
```

즉 실질적인 작업은:

* service()가 호출되면서
* doGet(), doPost(), doPut(), doDelete() 등이 실행됨

---

## 3) 서버 종료 시 destroy() 호출

애플리케이션이 종료될 때:

```java
helloServlet.destroy();
```

자원 정리를 위한 마지막 단계.

---

# 4. Servlet이 “작동”하는 순간(요청 처리 흐름)

1. 클라이언트가 HTTP 요청을 보냄
2. 톰캣의 Connector가 소켓에서 바이트 스트림 읽음
3. Coyote Processor가 HTTP 메시지를 파싱
4. HttpServletRequest / HttpServletResponse 객체 생성
5. FilterChain 호출
6. **FilterChain 끝에 Servlet이 배치됨**
7. ServletContainer가 service() 호출
8. HTTP 메서드(GET, POST 등)에 따라 doGet(), doPost() 실행
9. 응답 작성
10. Response 출력 후 흐름 종료

즉,

**Servlet이 작동하는 시점은 “FilterChain을 지나서 service()가 호출될 때”이다.**

---

# 5. Spring MVC에서는 Servlet이 무엇인가?

Spring MVC에서도 Servlet은 존재합니다.
그게 바로 **DispatcherServlet**입니다.

DispatcherServlet도 동일 라이프사이클.

* 톰캣이 서버 시작 시 DispatcherServlet 객체 1개 생성
* init() 호출 → HandlerMapping, HandlerAdapter 등 초기화
* 이후 모든 HTTP 요청은 DispatcherServlet.service() → doDispatch()로 들어감

요청 흐름:

```
Filter → DispatcherServlet → Interceptor → Controller
```

즉, Spring MVC에서 Servlet은 **DispatcherServlet 1개**입니다.

---

# 6. 그림으로 전체 동작 정리

```
[서버 시작]
    ↓
톰캣: Servlet 인스턴스 생성
    ↓
Servlet.init() 호출
    ↓
[요청 1 오면]
→ Servlet.service() → doGet() or doPost()
    ↓
[요청 2 오면]
→ Servlet.service() → doGet() or doPost()
    ↓
... (반복)
    ↓
[서버 종료]
Servlet.destroy()
```

Servlet 객체가 요청마다 새로 만들어지는 게 아니라
초기화된 **단일 객체가 요청을 계속 처리하는 구조**입니다.

---

# 7. Filter와 Servlet의 관계

요청의 흐름은 다음과 같습니다:

```
Client
  ↓
Tomcat
  ↓
FilterChain(doFilter)
  ↓
Servlet.service()
  ↓
Controller(service 내부)
```

즉, Filter는 Servlet 호출 전에 작동하는 “전처리(사전 관문)” 역할이고,
Servlet은 “요청을 실제 처리하는 주체”입니다.

---

# 결론

### Servlet 객체는 언제 작동?

* **서버 시작 시 1번 생성 + init() 실행**
* **HTTP 요청이 올 때마다 Servlet.service()가 호출되며 작동**
* **서버 종료 시 destroy()**

### HttpServletRequest와 Servlet 생성은 관련 없다

HttpServletRequest는 톰캣이 만들고
Servlet은 그 객체를 전달받아 처리만 한다.

---

원하시면

* Servlet.service() 내부 구조
* HttpServlet의 method 분기(doGet/doPost) 코드
* Spring DispatcherServlet의 doDispatch() 상세 흐름

까지 이어서 설명해드릴게요.

좋아, 핵심을 정확하게 짚으셨어요.
**“싱글톤 객체들이 많은데 스레드는 여러 개다? 그럼 동시에 처리하는 요청의 개수는 무엇으로 결정되는가?”**
이 부분은 Java 웹 서버 동작의 본질이기 때문에 헷갈릴 수밖에 없습니다.

아래 내용을 차근차근 보면 전체 구조가 완전히 정리됩니다.

---

# 1. “싱글톤”과 “동시성”은 서로 다른 차원의 이야기다

### 스프링의 대부분의 Bean은 **싱글톤**이다

* Controller
* Service
* Repository
* DispatcherServlet
* Filter
* Interceptor

→ “객체 인스턴스가 몇 개냐”를 의미하는 이야기.

### 반면 “동시에 몇 개의 요청을 처리하냐”는

**스레드가 몇 개냐**의 문제이다.

즉,

```
싱글톤 = 인스턴스 개수
동시 처리 수 = 스레드 개수
```

이 둘은 전혀 충돌하지 않는다.

---

# 2. 동시에 요청을 처리하는 능력은 “스레드 풀 크기”로 결정된다

## 톰캣은 내부적으로 다음과 같은 구조를 갖는다:

```
ThreadPool(예: 200개)
    ├── WorkerThread-1
    ├── WorkerThread-2
    ├── ...
    └── WorkerThread-200
```

**스레드 풀 크기 = 동시에 처리 가능한 요청 수입니다.**

* 스레드 200개면 → 최대 200건의 요청을 **동시에** 처리할 수 있음
* 201번째 요청이 오면 → 대기 큐에서 기다림

“싱글톤 객체”는 이 스레드들에 의해 **공유**되는데,
공유되어도 문제 없는 이유는 스프링 빈들이 **Request 전용 상태를 가지지 않기 때문**입니다.

---

# 3. 싱글톤 Bean은 “상태를 가지지 않기 때문에” 동시 접근에도 안전하다

Controller, Service는 보통 이렇게 생김:

```java
@Service
public class MemberService {
    // 필드에 상태를 저장하지 않음
    public void join(MemberForm form) { ... }
}
```

스프링이 싱글톤을 강력하게 권장하는 이유는:

* **필드에 요청별 상태를 저장하지 않는다면**
* **여러 스레드가 동시에 접근해도 안전**하기 때문

즉:

* 스레드 수 = 동시 요청 처리 능력
* 빈 인스턴스 수 = 요청 처리와 무관

“싱글톤이니까 동시에 한 요청밖에 못 처리한다”는 **완전히 잘못된 이해**입니다.

싱글톤 객체는 공유되지만,
각 요청별 상태는 스레드나 로컬 변수 안에 들어있기 때문에 충돌이 일어나지 않습니다.

---

# 4. 어떻게 여러 스레드가 같은 싱글톤 객체를 공유해도 안전한가?

스프링의 Controller/Service가 다음을 지키기 때문에:

### 1) 상태(State)를 가지지 않는다

즉, 필드에 request-specific 값을 절대로 저장하지 않는다.

### 2) 요청마다 필요한 모든 정보는

* HttpServletRequest
* 메서드 파라미터
* 지역 변수
* ThreadLocal(MDC, Session, SecurityContext)

여기에 들어있다.

### 3) 스레드 간 공유되는 부분은 “읽기 전용이거나 불변(immutable)”이다

따라서

```
싱글톤 <—여러 스레드—> 동시에 접근해도 충돌 없음
```

상태만 가지지 않으면 싱글톤은 본질적으로 thread-safe에 가깝다.

---

# 5. 그럼 동시에 몇 개를 처리할 수 있는가?

정확한 공식:

```
동시 처리 가능한 요청 수 = 서버 스레드 풀 크기
(= 톰캣 maxThreads)
```

기본 값은:

* Spring Boot 내장 톰캣: 200 threads

즉:

```
최대 200개의 요청을 완전히 동시에 처리 가능
201번째 요청은 대기 큐에서 기다림
```

싱글톤 সংখ্য과는 전혀 무관함.

---

# 6. 왜 서블릿 객체도 싱글톤인데 동시에 처리 가능해?

DispatcherServlet도 싱글톤 1개지만
여러 스레드가 service()를 동시에 호출한다.

그게 가능한 이유는 정직하게 말하면:

* **Servlet은 상태를 내부에 저장하지 않는다**
* 요청별 상태는 HttpServletRequest 내부(ThreadLocal 기반 자료 포함)에 들어있다

즉, 멀티스레드 환경에 적합하도록 설계되었기 때문.

---

# 7. 톰캣이 실제로 어떻게 “정말 동시에” 요청을 처리하는가?

요청 1개당 WorkerThread 1개가 작업한다.

흐름 예시:

```
요청 A → WorkerThread-12 → DispatcherServlet → Controller
요청 B → WorkerThread-27 → DispatcherServlet → Controller
요청 C → WorkerThread-43 → DispatcherServlet → Controller
```

DispatcherServlet 객체는 하나밖에 없지만:

```
DispatcherServlet.service()  
    ↑
WorkerThread-12 / WorkerThread-27 / WorkerThread-43  
"동시에" 호출함
```

이게 가능한 이유는:

* DispatcherServlet은 **비상태(stateless)**로 설계됨

그래서 스레드 안전함.

---

# 8. 결론

1. **동시 처리 개수는 스레드 개수로 결정된다.**
   싱글톤 Bean 수와는 무관.

2. **싱글톤 Bean들은 상태를 가지지 않도록 설계하여 스레드 안전하다.**

3. **서블릿/스프링은 여러 스레드가 하나의 객체를 동시에 호출해도 문제 없도록 설계되어 있다.**

4. 그래서:

```
싱글톤 많음 → 상관없음
스레드 많음 → 동시성 증가
요청 개수 = maxThreads
```

---

원하면

* 톰캣 maxThreads 설정과 실제 스레드 실행 과정
* 멀티스레드 환경에서 안전한 싱글톤 패턴의 조건
* ThreadLocal을 사용하는 이유
* “상태 있는 싱글톤”이 위험한 실제 사례

도 자세히 이어서 설명해줄게요.

ThreadLocal을 사용하는 이유는 **멀티스레드 환경에서 “요청별로 독립된 상태”를 안전하게 보관하기 위해서**입니다.
다시 말해서,

**“동시에 여러 요청이 들어와도 서로 섞이지 않는, 스레드 전용 저장소”가 필요하기 때문입니다.**

아래 설명을 차근차근 보면 ThreadLocal의 필요성이 완전히 명확해집니다.

---

# 1. 멀티스레드 환경에서는 “전역 변수”를 쓰면 안 된다

웹 서버는 다음과 같은 구조입니다:

```
WorkerThread-1  ← 요청 A
WorkerThread-2  ← 요청 B
WorkerThread-3  ← 요청 C
...
```

동시에 여러 스레드가 돌아가기 때문에,
전역 static 변수 같은 곳에 요청마다 다른 데이터를 저장하면 **데이터가 섞여버립니다**.

예:

```
static String traceId;
```

요청 A가 traceId=AAA 를 저장
요청 B가 traceId=BBB 를 저장
요청 A가 로그 남김 → traceId=BBB 로 출력됨

이렇게 **레ース 컨디션**과 **데이터 꼬임**이 발생합니다.

---

# 2. 그렇다고 “스레드마다 Map을 따로 만들어야 하나?”

원칙적으로는 아래 같은 것이 필요합니다:

```
Thread-A → {"traceId": "AAA"}
Thread-B → {"traceId": "BBB"}
Thread-C → {"traceId": "CCC"}
```

즉, 스레드 전용 데이터 저장소가 필요합니다.

그런데 개발자가 매번 이런 스레드 전용 Map을 관리할 수 없죠.

그래서 JVM이 제공하는 기능이 바로 **ThreadLocal**입니다.

---

# 3. ThreadLocal의 핵심 — “스레드 전용 저장소”

ThreadLocal은 이렇게 동작합니다:

```
Thread-A(ThreadLocalMap)
      └─ key(ThreadLocal) → value("AAA")

Thread-B(ThreadLocalMap)
      └─ key(ThreadLocal) → value("BBB")

Thread-C(ThreadLocalMap)
      └─ key(ThreadLocal) → value("CCC")
```

스레드는 여러 개지만
“같은 ThreadLocal 객체”를 사용해도
각 스레드의 ThreadLocalMap에 값이 따로 저장됩니다.

즉:

* Thread-A의 값 = Thread-A만 접근 가능
* Thread-B의 값 = Thread-B만 접근 가능
* Thread-C의 값 = Thread-C만 접근 가능

스레드끼리 절대 섞이지 않음.

---

# 4. ThreadLocal을 사용하는 대표적인 목적

## 1) 요청별 traceId를 저장하기 위해 (MDC)

로그마다 “어떤 요청인지 식별하기 위한 고유 ID”를 저장해야 한다.

필터에서:

```java
MDC.put("traceId", "AAA");
```

이 값은

* Thread-A의 ThreadLocalMap에만 저장됨
* Thread-A에서 찍는 모든 로그에 traceId가 자동으로 들어감
* Thread-B는 Thread-B의 traceId만 사용
* 완벽한 분리

스레드 풀 재사용 때문에 매 요청마다 clear해야 함.

---

## 2) 트랜잭션 컨텍스트 저장 (JDBC Connection, JPA 엔티티 매니저)

Spring의 트랜잭션 처리 구조는 ThreadLocal 기반입니다.

* 같은 요청 내에서는 같은 DB 커넥션을 사용해야 한다
* 같은 스레드 내에서만 트랜잭션을 유지해야 한다

그래서:

```
Thread-A → Connection-1
Thread-B → Connection-2
```

스레드별로 Connection을 보관하기 위해 ThreadLocal 사용.

---

## 3) 로그인 정보 보관 (예: SecurityContextHolder)

Spring Security:

```
SecurityContextHolder.getContext().getAuthentication()
```

→ 내부적으로 ThreadLocal 기반

요청 A의 사용자는 “홍길동”
요청 B의 사용자는 “김철수”

ThreadLocal 덕분에 두 요청의 사용자 정보가 섞이지 않음.

---

## 4) 요청 전용 Cache나 Context 저장

예:

* LocaleContext (언어 정보)
* RequestContext
* RequestAttributes

이것들도 대부분 ThreadLocal 기반입니다.

---

# 5. 그렇다면 “ThreadLocal이 없다면 어떻게 될까?”

요청마다 독립된 정보를 저장하려면 매번 아래처럼 해야 합니다:

* 컨트롤러 → 서비스 → 리포지토리까지 모든 메서드에 “traceId”를 파라미터로 계속 넘기기
* 트랜잭션을 매 메서드 호출마다 인자로 전달하기
* 인증 정보를 모든 계층에 계속 넘겨야 함

이건 불가능에 가까운 코드가 됨.

ThreadLocal이 없으면
**요청별 상태 유지를 위해 로직이 전부 오염**됩니다.

---

# 6. ThreadLocal을 사용하는 이유를 정리하면

### 1) 멀티스레드 환경에서 요청별로 데이터가 분리되어야 한다

(전역 변수 불가)

### 2) 스레드마다 독립적인 저장소가 필요하다

(ThreadLocalMap)

### 3) 컨트롤러 → 서비스 → 리포지토리까지

모든 계층에서 “전역처럼” 값을 꺼낼 수 있어야 한다
(하지만 실제로는 스레드 개인 저장소)

### 4) traceId, 인증정보, 트랜잭션 같은 값을 전달하는 부담을 줄인다

(코드 오염 방지)

### 5) 프레임워크(Spring, Logback, Security)가 내부적으로 상태를 전파할 수 있어야 한다

(AOP, Interceptor 등에서도 동일한 정보 사용)

---

# 7. 한 문장으로 요약

**ThreadLocal은 “요청마다 독립된 상태를 유지해야 하는 정보”를
깨끗하게, 안전하게, 코드 오염 없이 전달하기 위한
스레드 기반의 개인 저장소이다.**

---

원하시면

* ThreadLocalMap의 내부 구조(Entry 배열, 해시 방식)
* 왜 ThreadLocal 누수가 발생하는지
* 톰캣 스레드 재사용과 clear()의 관계
* ThreadLocal이 가능한 이유(스택 vs 힙 vs Thread 구조)

까지 더 깊이 설명해드릴게요.

지금 질문하신 내용은 **“싱글톤 객체의 메서드 코드는 어디 저장되며, 동시에 여러 스레드가 호출될 때 각각의 실행 컨텍스트는 어디에 저장되는가?”**
즉 **코드 자체 vs 실행 컨텍스트** 를 정확히 구분하고 싶다는 의도입니다.

결론적으로 **당신이 이해한 방향이 거의 정확합니다.**
하지만 다음처럼 조금 더 정교하게 정리할 필요가 있습니다.

---

# 1. 싱글톤 객체의 “코드”는 모든 스레드가 동시에 읽는다

자바에서는 클래스의 바이트코드(.class 파일)는 클래스 로더가 로딩하고:

* **메서드 코드(instructions)는 메서드 영역(메타스페이스)**에 저장됨
* JVM 내부적으로 JIT 컴파일되면 **네이티브 코드가 Code Cache에 저장**됨
* CPU는 이 “같은 코드”를 여러 스레드가 동시에 실행할 수 있다

즉:

* 하나의 Controller, Service, Repository 메서드는 **하나의 코드 조각**
* 여러 스레드가 **그 코드 조각을 동시에 실행**한다
  (CPU pipeline, instruction cache, branch predictor 등)

**코드는 공유되지만, 실행 컨텍스트는 공유되지 않는다.**

이게 핵심.

---

# 2. “실행 컨텍스트”는 스레드마다 완전히 분리된다

실행 컨텍스트란:

* 메서드 스택 프레임
* 지역 변수
* 매개변수
* 리턴 주소
* CPU 레지스터(실행 중)
* 스레드 레지스터(thread pointer)
* JIT 최적화 로컬 캐시 등

이것들은 모두 **Thread Stack** 또는 CPU 내부에 저장된다.

### 스레드가 동시에 같은 메서드를 실행해도:

```
스레드 A → 자신의 스택 프레임을 사용
스레드 B → 자신의 스택 프레임을 사용
스레드 C → 자신의 스택 프레임을 사용
```

따라서 절대 섞이지 않는다.

---

# 3. ThreadLocal은 “스택에 넣을 수 없는 요청별 상태”를 스레드 힙 공간에 저장하는 방식

지역 변수가 아니라 “모든 레이어에서 공유되어야 할 요청별 상태”가 있다.

예를 들어:

* traceId
* SecurityContext(인증 정보)
* DB Connection(트랜잭션)
* LocaleContext(언어 정보)

이건 컨트롤러 → 서비스 → 리포지토리까지 전달되어야 한다.
그런데 매개변수로 계속 넘기면 코드가 지옥이 된다.

그래서 ThreadLocal을 사용한다.

### ThreadLocalMap은 어디에 저장되나?

바로 **Thread 객체 내부**에 저장된다.

즉:

* Thread가 1개면 ThreadLocalMap도 1개
* Thread가 200개면 ThreadLocalMap도 200개
* 스레드별로 완전히 독립된 저장소

그래서 요청 A와 요청 B 데이터가 섞이지 않는다.

---

# 4. 전체 구조를 계층별로 설명하면 이렇게 된다

### 1) 코드(메서드)는 공유됨

* JVM 메타스페이스 / 코드캐시 / CPU instruction cache에 저장
* 여러 스레드가 동시에 실행 가능

### 2) 실행 컨텍스트는 스레드 스택에 저장됨

* 매개변수
* 지역변수
* 반환값
* 예외 처리 정보

각 스레드는 **자기만의 스택**을 가지므로 절대 섞이지 않는다.

### 3) ThreadLocal 데이터는 Thread 객체 내부(ThreadLocalMap)에 저장됨

* 지역변수처럼 스택에서 사라지지 않음
* 여러 메서드를 거쳐도 유지됨
* 같은 스레드 내에서만 공유됨

---

# 5. 당신이 말한 내용을 정확히 정리하면

> “싱글톤 객체의 같은 메서드 코드는 공유되고,
> 실행 컨텍스트는 각 스레드의 스택/CPU 레지스터에 저장되고,
> 스레드 간 공유하면 안 되는 요청별 상태는 ThreadLocalMap에 저장된다.”

이 표현은 **정확하게 맞습니다.**
아주 중요한 개념을 거의 완벽하게 파악하신 겁니다.

딱 한 문장으로 말하면:

**코드는 공유되지만, 실행 상태는 스레드별로 완전히 분리된다.
그리고 요청별 상태(전역처럼 보이는 정보)는 ThreadLocal에 저장한다.**

---

# 6. 그래서 싱글톤도 멀티스레드 환경에서 안전한 이유

* 싱글톤 Bean은 “상태 없는(stateless)” 객체이기 때문에
  여러 스레드가 동시에 사용해도 전혀 문제가 없음
* 모든 요청별 상태는

    * 스레드 스택
    * HttpServletRequest
    * ThreadLocal
      등에 저장되기 때문에 싱글톤의 필드가 오염되지 않음

---

# 7. 원한다면 더 깊게 설명 가능

* JVM 메서드 영역 / 코드 캐시 구조
* CPU instruction pipeline과 멀티스레드 코드 실행 방식
* ThreadLocalMap의 실제 구조(entry 배열/메모리 참조 방식)
* JIT 컴파일된 코드가 스레드 사이에서 어떻게 공유되는지
* 왜 ThreadLocal 누수가 발생하는지 (스레드 풀 재사용)

원하는 방향으로 더 깊게 파고들 수 있어요.

ThreadLocalMap은 **ThreadLocal이 스레드별로 값을 저장하기 위해 사용하는 실제 자료구조**입니다.
ThreadLocal을 제대로 이해하려면 ThreadLocalMap의 구조가 어떻게 생겼는지를 아는 게 핵심이에요.

아래는 **JDK 소스코드를 기반**으로 한 실제 구조 분석입니다.
(자바 8 기준. 자바 17도 거의 동일한 구조.)

---

# 1. ThreadLocalMap은 “Thread 안에 들어 있는 Map”

ThreadLocalMap은 ThreadLocal 안에 있는 것이 아니라:

```
Thread 객체 내부에 들어 있다.
```

정확한 구조:

```
Thread
 └── ThreadLocal.ThreadLocalMap threadLocals
```

ThreadLocalMap은 맵이 아니라 **특수한 해시 테이블 구조**이며,
JDK가 ThreadLocal 전용으로 만든 내부 자료구조이다.

---

# 2. ThreadLocalMap의 실제 구조 (핵심 요약)

ThreadLocalMap은 크게 다음 요소로 구성된다:

```
Entry[] table
int size
int threshold
```

가장 중요한 것이 바로 Entry 배열.

---

# 3. Entry 구조

Entry는 **ThreadLocalMap 내부 static class**이며
(ThreadLocalMap.Entry extends WeakReference<ThreadLocal<?>>)

구조는 다음과 같다:

```java
static class Entry extends WeakReference<ThreadLocal<?>> {
    Object value;
}
```

즉:

### Entry = WeakReference(ThreadLocal) + value(Object)

이 말은:

* **key = ThreadLocal 객체 자체**
* **key는 WeakReference로만 유지됨**
* **value = ThreadLocal에 저장한 값**

Entry는 항상 pair 형태:

```
key   = ThreadLocal 인스턴스(WeakReference)
value = 개발자가 넣은 값
```

예:

```java
ThreadLocal<String> tl = new ThreadLocal<>();
tl.set("ABC");
```

실제 저장 구조:

```
Entry(
  key = WeakReference(tl),
  value = "ABC"
)
```

---

# 4. 왜 WeakReference를 쓰는가?

ThreadLocal 객체가 GC로 사라지면,
ThreadLocalMap에서 key가 null이 되어 value만 남게 되는데,
스레드가 죽지 않는 한 이 value는 메모리 누수가 될 수 있다.

WeakReference를 쓰면:

* ThreadLocal 객체가 GC 대상이 되면 key가 null이 됨
* ThreadLocalMap이 clean up하면서 Entry를 제거함

즉, 메모리 누수 방지 장치.

---

# 5. 해시 테이블 구조

ThreadLocalMap은 일반적인 HashMap과 다르게
**open addressing 방식(선형 탐사 기반)**을 사용합니다.

구조:

```
Entry[] table = new Entry[초기 크기 16];
```

해시 계산:

```
index = threadLocal.hashCode() & (table.length - 1)
```

충돌 시:

```
index = (index + 1) % table.length  // 선형 probing
```

즉, HashMap처럼 체인(LinkedList)이 아니라
단순한 배열 이동 방식.

---

# 6. put()의 실제 로직 요약

ThreadLocalMap.set(ThreadLocal key, Object value)

1. key.hashCode()로 index 계산
2. 슬롯이 비었으면 Entry 저장
3. 이미 다른 ThreadLocal의 Entry가 있으면 선형 탐사
4. 비어있는 곳 또는 key가 있는 곳을 찾음
5. value 저장
6. load factor 넘으면 resize()

---

# 7. get()의 실제 로직 요약

ThreadLocalMap.getEntry(ThreadLocal key)

1. key.hashCode()로 index 계산
2. table[index]를 확인
3. entry.key == key 이면 value 반환
4. 아니면 선형 탐사로 계속 찾음
5. 못 찾으면 null

즉, 일반 HashMap보다 단순한 구조.

---

# 8. ThreadLocalMap은 왜 일반 HashMap을 사용하지 않는가?

HashMap은:

* Entry에 강한 strong reference 사용 → 메모리 누수 위험 증가
* 동시성 고려 부족
* ThreadLocalMap만을 위해 최적화된 기능이 필요

ThreadLocalMap은:

* WeakReference를 key로 사용
* “스레드 전용”이므로 동기화 필요 없음
* cleanup 로직 포함
* 선형 probing으로 매우 빠른 해시 접근
* ThreadLocalMap은 Thread 안에만 존재하므로 외부에서 접근 불가

따라서 JDK는 ThreadLocalMap을 따로 만들었다.

---

# 9. ThreadLocalMap의 핵심 철학

ThreadLocalMap은 “스레드 전용 쓰레기통” 같은 느낌입니다.

* ThreadLocal을 key로
* value는 어떤 객체든 저장
* Thread가 살아있는 동안 유지
* Thread가 종료되면 ThreadLocalMap도 GC됨
* key가 GC되면 WeakReference 덕분에 entry 제거됨

---

# 10. 그림으로 전체 구조 표현

```
[Thread Instance]
   └── threadLocals : ThreadLocalMap
          ├── Entry[0] ── key(ThreadLocal@A) → value("AAA")
          ├── Entry[1] ── null
          ├── Entry[2] ── key(ThreadLocal@B) → value("BBB")
          ├── Entry[3] ── null
          └── ...
```

각 Thread가 이런 구조를 **독자적으로** 갖는다.

즉:

* Thread-A의 ThreadLocalMap
* Thread-B의 ThreadLocalMap
* Thread-C의 ThreadLocalMap

모두 서로 독립.

이게 ThreadLocal의 “요청별 독립 상태 유지”를 가능하게 한다.

---

# 11. ThreadLocalMap이 메모리 누수를 일으키는 이유

메모리 누수 시나리오:

1. ThreadLocal 객체가 GC됨
2. key(WeakReference)는 null이 됨
3. value는 여전히 남아 있음
4. Thread는 스레드풀에 의해 계속 살아 있다
5. ThreadLocalMap 내부의 value가 GC되지 않음
6. 요청이 끝나도 value가 남아 있어서 누적됨

그래서 ThreadLocal 사용 후 반드시:

```java
ThreadLocal.remove();
```

혹은

```java
MDC.clear();
```

가 필요한 이유.

---

# 12. 결론 정리

ThreadLocalMap의 실제 구조는:

* Thread 객체 내부에 존재하는 Map
* key: WeakReference(ThreadLocal 객체)
* value: 개발자가 넣은 데이터
* 데이터 구조: open addressing 기반 Entry[] 배열
* 스레드마다 ThreadLocalMap을 따로 가짐
* ThreadLocal은 단지 “key 역할”만 한다

이 구조 덕분에:

* 같은 ThreadLocal 객체를 여러 스레드가 사용해도
  각 스레드는 자신의 ThreadLocalMap에 값이 저장됨
* 서로 전혀 충돌하지 않음

---

원하면

* ThreadLocalMap의 resize() 동작
* cleanup 로직의 소스 분석
* 해시 충돌 처리 방식 비교(HashMap vs ThreadLocalMap)
* ThreadLocal이 컨텍스트 전파에 왜 사용되는지 더 깊은 이유

도 이어서 설명해드릴게요.
