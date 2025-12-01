아래 네 가지 개념은 **스프링 부트의 서블릿/필터 자동 등록**, **서블릿 필터 구성 방식**, 그리고 **로그백 MDC(Context)** 와 관련된 기능들입니다. 각각을 맥락과 함께 정리해드릴게요.

---

# 1. `@ServletComponentScan`

### 개념

스프링 부트에서 **서블릿, 필터, 리스너를 @WebServlet, @WebFilter, @WebListener로 등록할 수 있게 스캔해주는 어노테이션**입니다.

### 특징

* 기본적으로 스프링 부트는 `@WebFilter` 등 서블릿 스펙의 어노테이션을 스캔하지 않습니다.
* 이를 활성화하려면 **스프링 부트 메인 클래스**에 `@ServletComponentScan`을 추가해야 합니다.

### 예시

```java
@SpringBootApplication
@ServletComponentScan
public class MyApplication {
}
```

이제 클래스에 `@WebFilter`만 붙여도 필터가 자동 등록됩니다.

---

# 2. `@WebFilter`

### 개념

Servlet API에서 제공하는 필터 등록용 어노테이션입니다.
Spring이 아닌 **서블릿 컨테이너 수준**에서 필터를 등록합니다.

### 특징

* URL 패턴을 지정할 수 있음
* 필터 체인의 일부로 작동
* 스프링 빈이 아니어도 동작함(단, 스프링 기능을 직접 주입받기는 어렵다)

### 예시

```java
@WebFilter(urlPatterns = "/*")
public class MyFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("WebFilter 실행");
        chain.doFilter(request, response);
    }
}
```

> 단, 위 어노테이션이 동작하려면 프로젝트에 `@ServletComponentScan`이 필요합니다.

---

# 3. `FilterRegistrationBean`

### 개념

스프링 부트에서 **필터를 빈으로 등록하기 위한 Spring 방식의 필터 등록 클래스**입니다.

### 특징

* 스프링 빈으로 등록 → 스프링 DI 활용 가능
* 필터의 순서(order) 지정 가능
* URL 패턴 설정 가능
* @WebFilter 방식보다 더 유연하고 실무에서 선호됨

### 예시

```java
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<MyFilter> loggingFilter() {
        FilterRegistrationBean<MyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MyFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);  // 필터 순서 설정
        return registration;
    }
}
```

`@WebFilter`는 **서블릿 방식**,
`FilterRegistrationBean`은 **스프링 방식**이라고 기억하면 됩니다.

---

# 4. Logback MDC (Mapped Diagnostic Context)

### 개념

로그백(Logback)에서 제공하는 **스레드 로컬 기반의 진단 컨텍스트**입니다.

### 목적

* 하나의 요청 흐름(request flow)에 대해 **추적 ID**, 사용자 정보 등을 로그에 자동 포함시키기 위해 사용합니다.
* 예: 로그마다 traceId를 자동으로 넣기

### 예시: 필터에서 MDC 설정

```java
@WebFilter("/*")
public class MdcFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            MDC.put("traceId", UUID.randomUUID().toString());
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

### logback.xml에서 사용

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - [%X{traceId}] %msg%n</pattern>
```

로그 출력 예:

```
2025-01-10 12:00:00 [http-nio-8080-exec-1] INFO com.example.MyController - [e2bc3...] 요청 시작
```

---

# 요약

| 항목                       | 역할                        | 등록 방식                |
| ------------------------ | ------------------------- | -------------------- |
| `@ServletComponentScan`  | @WebFilter/@WebServlet 스캔 | 스프링 부트 메인 클래스에 선언    |
| `@WebFilter`             | 서블릿 필터 등록                 | 서블릿 컨테이너 방식          |
| `FilterRegistrationBean` | 스프링 방식의 필터 등록             | 빈 등록 방식, 순서 설정 가능    |
| Logback MDC              | 로그 컨텍스트 저장                | traceId 등 요청별 데이터 보관 |

---

필요하시면

* 각각의 예제를 더 자세히 설명
* 스프링에서 가장 권장되는 필터 등록 방식
* 웹Flux 환경에서의 차이
* MDC를 Sleuth / Spring Cloud / Mapped Diagnostic Context와 함께 쓰는 방법

등도 도와드릴게요.

가능합니다. 단, **조건이 하나** 있습니다.
`MDC.put()`을 쓰려면 프로젝트에 **Logback 또는 SLF4J MDC 구현체가 classpath에 있어야 합니다.**

---

# 1. 결론

스프링 부트 일반 웹 애플리케이션(Starter 포함)이라면 **바로 사용할 수 있습니다.**

```java
import org.slf4j.MDC;

MDC.put("key", "value");
```

위 코드가 바로 동작합니다.

스프링 부트는 기본적으로 `spring-boot-starter-logging`을 포함하며
그 안에 Logback + SLF4J MDC 구현이 모두 들어 있기 때문입니다.

---

# 2. MDC.put 사용 조건

| 조건                       | 설명                              |
| ------------------------ | ------------------------------- |
| SLF4J API                | `org.slf4j.MDC`                 |
| 실제 MDC 구현체               | Logback 또는 Log4j2 등             |
| 프로젝트에 logging starter 존재 | spring-boot-starter-logging(기본) |

스프링 부트에서는 기본적으로 로그백이 포함되므로 따로 설정할 필요가 없습니다.

---

# 3. MDC.put을 사용하는 일반적인 위치

보통은 **필터**, **인터셉터**, **AOP** 에서 사용합니다.

### 필터 예

```java
@Override
public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {

    try {
        MDC.put("traceId", UUID.randomUUID().toString());
        chain.doFilter(req, res);
    } finally {
        MDC.clear();
    }
}
```

---

# 4. 주의할 점

### (1) 스레드 로컬 기반

MDC는 **ThreadLocal 기반**이라 요청이 다른 쓰레드로 넘어가면 값이 유지되지 않습니다.
특히 `@Async`, WebClient(Reactor), 비동기 로직에서는 주의해야 합니다.

### (2) cleanup 필수

값 넣었으면 반드시 제거해야 합니다.

```java
MDC.clear();
```

필터라면 예외 여부와 상관없이 clear해야 합니다.

---

필요하시면

* 비동기 환경에서 MDC를 전파하는 방법
* traceId 자동 생성 필터 템플릿
* logback 패턴 설정법

도 자세히 알려드릴게요.

MDC는 **로그를 남길 때 ‘현재 요청과 관련된 추가 정보’를 함께 기록하기 위한 저장소**라고 이해하면 됩니다.
가장 핵심적인 개념은 **요청 단위로 데이터를 보관하는 일시적인 메모리 공간**이라는 점입니다.

---

# 1. MDC(Mapped Diagnostic Context)란?

### 한 줄 정의

**로그백(Logback)이나 SLF4J에서 제공하는, ThreadLocal 기반의 Key-Value 저장소.**

### 왜 필요한가?

서버는 동시에 여러 요청을 처리합니다.
로그가 섞여버리면 어떤 로그가 어떤 요청에서 나온 것인지 구분하기 어렵습니다.

그래서 요청마다 다음과 같은 정보를 MDC에 넣어두고
로그 패턴에서 꺼내 써서 **요청별로 로그를 식별**하게 합니다.

예:

* traceId
* userId
* requestURI
* sessionId

---

# 2. 동작 방식

### 간단한 흐름

1. 요청이 들어온다
2. 필터 또는 인터셉터에서 MDC에 값을 저장한다

   ```java
   MDC.put("traceId", "1234-5678");
   ```
3. 로그백에서 로그를 남길 때 `%X{traceId}`로 값을 꺼내 쓴다

   ```xml
   [%X{traceId}] %msg%n
   ```
4. 로그 출력

   ```
   [1234-5678] 로그인 요청 처리 완료
   ```
5. 요청 처리가 끝나면 `MDC.clear()`로 정리한다

### 중요한 점

MDC는 “ThreadLocal 기반”이라
**한 요청을 처리하는 스레드에 묶여서 값이 저장됩니다.**

그래서 다른 요청의 스레드와 섞이지 않습니다.

---

# 3. MDC가 없으면?

로그는 다음처럼 섞여 버립니다:

```
로그인 시도
상품 조회 요청
로그인 성공
상품 조회 성공
```

MDC로 traceId를 넣으면:

```
[req1] 로그인 시도
[req2] 상품 조회 요청
[req1] 로그인 성공
[req2] 상품 조회 성공
```

요청별 흐름이 선명하게 보이므로
**운영/트러블슈팅에서 필수적**입니다.

---

# 4. 어디에서 주로 쓰나?

* 서블릿 필터(HTTP 요청 시작 시)
* 스프링 인터셉터(preHandle)
* AOP
* 전역 예외 처리(예외 발생 시)
* WebFlux/비동기 환경에서는 별도 설정 필요

---

# 5. 아주 짧은 비유

서버는 동시에 여러 사람의 일을 처리하는 “사무실”이라고 보면,

MDC는
**각 작업서류 맨 위에 붙이는 ‘요청 번호 스티커’**입니다.

그 스티커(traceId) 덕분에
어떤 로그가 어떤 사람(요청)의 것인지 헷갈리지 않게 됩니다.

---

필요하시면

* 필터로 traceId 자동 생성 코드
* logback.xml 설정 예
* 비동기 환경에서 MDC가 사라지는 이유와 해결 방법

도 정리해드릴게요.

전혀 달라요.
겉으로 보면 “어떤 값을 저장한다”는 점에서 비슷해 보일 수 있지만, **MDC는 서버의 세션 저장 방식과 개념 자체가 완전히 다릅니다.**

아래 표로 먼저 감을 잡고, 뒤에서 자세히 설명할게요.

| 항목      | 세션(Session)            | MDC                          |
| ------- | ---------------------- | ---------------------------- |
| 저장 위치   | 서버 메모리/외부 저장소(Redis 등) | **ThreadLocal (스레드 내부 메모리)** |
| 지속성     | 요청 간 계속 유지됨            | **오직 한 요청 동안만 유지됨**          |
| 목적      | 사용자 식별 및 상태 저장         | **로그에 쓸 일시적 진단 정보 저장**       |
| 누가 접근?  | 서버 전역 코드               | **현재 스레드에서만 접근 가능**          |
| 언제 지워짐? | 세션 만료/로그아웃             | **요청 종료 시 즉시 clear**         |
| 사용 예    | 로그인 상태, 장바구니           | traceId, requestURI, userId  |

---

# 1. 세션은 “사용자 상태를 서버에 저장하는 것”

세션은 다음처럼 **서버가 지속적으로 기억해야 할 사용자 상태**를 저장합니다.

* 로그인 정보
* 장바구니 내용
* 권한 정보
* 사용자 기본 설정 등

즉, **요청이 여러 번 오고 가도 계속 유지되는 값**입니다.

세션은 서버 메모리, DB, Redis 같은 외부 저장소까지 다양하게 보관할 수 있습니다.

---

# 2. MDC는 “현재 요청을 처리하는 스레드에만 붙는 임시 메모”

반면 MDC는:

* **요청이 들어와서 처리되는 동안만 존재**합니다.
* 요청이 끝나면 `MDC.clear()`로 반드시 지웁니다.
* 서버의 상태를 저장하기 위한 개념이 아닙니다.
* 사용자가 다시 요청하면 MDC는 **처음부터 비어 있는 상태**입니다.

MDC는 **로그에 넣을 정보를 일시적으로 저장하는 목적** 외에는 아무 기능도 없습니다.

---

# 3. 비유로 설명하면

서버 세션 = “회사 사무실에서 고객별로 만들어 놓은 장부”

* 며칠, 몇 주 동안 계속 열어서 확인 가능

MDC = “그날 처리하는 티켓 번호를 메모지에 적어 현재 작성 중인 서류에 붙이는 것”

* 그 요청이 끝나면 버려짐
* 다른 요청에서는 새로운 메모지 사용

둘은 역할이 완전히 다릅니다.

---

# 4. 왜 MDC에 traceId 같은 걸 넣을까?

요청 A와 B가 동시에 서버로 들어오는 상황에서

```
[A] 로그인 처리
[B] 상품 조회
[A] 로그인 성공
[B] 상품 조회 성공
```

이렇게 구분되게 하기 위해서입니다.

MDC는 요청 과정에서 “로그 식별을 위해 잠깐 보관하는 정보”일 뿐입니다.

---

원하면

* 세션과 MDC의 내부 구조 비교
* ThreadLocal로 구현된 이유
* 비동기 환경에서 MDC가 사라지는 이유

도 설명해드릴게요.

아래 설명은 **서버 세션(Session)**과 **MDC(Mapped Diagnostic Context)**가 내부적으로 어떻게 구성되어 있고 어떻게 동작하는지를 구조 관점에서 비교한 것입니다.
두 개념은 이름이 비슷해 보이지만 내부 구조는 완전히 다릅니다.

---

# 1. 서버 세션(Session)의 내부 구조

## 1) 핵심 개념

서버 세션은 **클라이언트(사용자)별로 상태를 저장하는 서버 측 저장소**입니다.
세션은 일반적으로 서버가 내부적으로 아래 구조를 가집니다.

## 2) 내부 구조 (개략도)

```
SessionStore (HashMap 또는 외부 저장소)
└── sessionId -> SessionData(Map)
       ├── "userId" -> "shk123"
       ├── "cart" -> [1001, 1002]
       └── ...
```

### 각 요소 설명

### (1) SessionId

* 임의의 문자열(토큰)
* 클라이언트가 쿠키(JSESSIONID)로 들고 다님
* 요청이 도착하면 이 ID로 서버의 세션을 찾음

### (2) SessionStore

세션 저장소의 구현 방식은 다양함:

| 저장소               | 방식                       |
| ----------------- | ------------------------ |
| **서버 메모리(local)** | HashMap<String, Session> |
| **Redis**         | Key-Value 저장소 기반         |
| **DB**            | RDB나 NoSQL               |
| **세션 클러스터링**      | 여러 노드 간 세션 공유            |

### (3) SessionData

실제 세션에 저장하는 정보들

* 일반적으로 **Map<String, Object>** 형태

예:

```java
HttpSession session = request.getSession();
session.setAttribute("userId", 10);
session.setAttribute("role", "admin");
```

SessionData 구조:

```
{
  "userId": 10,
  "role": "admin"
}
```

### 3) 특징 요약

* 서버 전체에서 사용자 상태를 추적
* 여러 요청에 걸쳐 유지(persistent)
* 사용자별로 하나씩 존재
* 저장과 조회는 SessionId 기반

---

# 2. MDC(Mapped Diagnostic Context)의 내부 구조

## 1) 핵심 개념

MDC는 **로그에 필요한 정보를 현재 쓰레드(Thread)**에만 저장하는 구조입니다.
즉, 사용자가 아니라 **스레드 단위 ThreadLocal Map**입니다.

## 2) 내부 구조 (개략도)

```
Thread
└── ThreadLocal
        └── MDCMap(Map<String, String>)
                 ├── "traceId" -> "a1b2c3"
                 ├── "userId" -> "10"
                 └── ...
```

### 각 요소 설명

### (1) ThreadLocal

* 자바가 제공하는 스레드 전용 저장소
* 스레드마다 독립된 공간을 가지고 있음
* 다른 스레드는 이 공간을 절대 볼 수 없음
* MDC의 핵심 기반 구조

### (2) MDC Map (Map<String, String>)

SLF4J/Logback은 내부적으로 `ThreadLocal<Map<String, String>>`을 갖고 있습니다.

JDK ThreadLocal 구조:

```
Thread
└── threadLocals (ThreadLocalMap)
       └── entry[ThreadLocal] = Map<String, String>
```

MDC put 과정:

```java
MDC.put("traceId", "abc123");
```

이는 내부적으로 다음과 같이 저장됩니다:

```
threadLocals
  └── MDCThreadLocal -> {"traceId": "abc123"}
```

### 3) 요청이 끝나면 왜 clear 해야 하나?

스레드 풀(Thread Pool) 때문입니다.

* 톰캣은 요청을 처리할 때 **스레드를 재사용**합니다.
* MDC를 지우지 않으면 다음 요청이 동일 스레드를 사용할 때 이전 값이 섞입니다.

따라서 다음이 필수:

```java
finally {
    MDC.clear();
}
```

### 4) 특징 요약

* 사용자 단위가 아니라 **스레드 단위 저장소**
* 요청 동안만 유지되는 임시 정보
* 로그를 위한 key-value 저장
* 스레드 풀에서는 반드시 clear 필요
* 비동기/리액티브는 별도 MDC 전파 모듈 필요

---

# 3. 세션 vs MDC 내부 구조 비교 요약

| 항목       | 세션(Session) 내부 구조        | MDC 내부 구조                |
| -------- | ------------------------ | ------------------------ |
| 저장 위치    | 서버 메모리 또는 Redis 등 외부 저장소 | **ThreadLocal (스레드 내부)** |
| 저장 형태    | Map<String, Object>      | Map<String, String>      |
| 키        | sessionId                | ThreadLocal key          |
| 데이터 수명   | 브라우저 지속, 로그인 유지          | 요청 처리 중 잠깐               |
| 공유 범위    | 사용자 단위                   | 스레드 단위                   |
| 클라이언트 영향 | sessionId를 쿠키로 전달        | 없음                       |
| 주 목적     | 사용자 상태 유지                | 로그 정보 제공                 |
| 누가 접근?   | 서버 어디서나 sessionId 기반     | 같은 스레드만 접근 가능            |

---

필요하다면

* 실제 Logback MDC 내부 구현 코드 구조
* ThreadLocalMap의 메모리 레이아웃
* 서블릿 컨테이너(Tomcat)의 스레드와 MDC 관계

까지 더 깊게 파고들어 설명해드릴 수 있어요.

여기서 말하는 **ThreadLocal<Map<String, String>>을 갖고 있는 스레드**는
“SLF4J나 Logback 자체 스레드”가 아니라,
**‘현재 실행 중인 어플리케이션 스레드(Tomcat worker thread 등)’**를 의미합니다.

즉, SLF4J/Logback이 스레드를 따로 만들어 MDC를 저장하는 것이 아닙니다.

* MDC 저장 위치 → SLF4J/Logback이 제공하는 **ThreadLocal 객체**
* 실제 저장되는 곳 → 그 ThreadLocal을 보유한 **현재 실행 스레드**

이 구조가 핵심입니다.

---

# 1. SLF4J/Logback이 스레드를 만드는 게 아니다

다음 표현을 정확히 해석하면:

> SLF4J/Logback은 내부적으로 ThreadLocal<Map<String, String>>을 갖고 있다

이는 다음과 같은 의미입니다:

* SLF4J/Logback은 **ThreadLocal 변수를 정의**한다.
* 하지만 ThreadLocal을 실제로 “보관”하는 것은 **각 스레드(Thread)**이다.
* SLF4J/Logback은 그 ThreadLocal을 통해 스레드별 MDC Map을 읽고 쓴다.

즉:

```
ThreadLocal 변수 정의 = 로그백
ThreadLocal의 실제 저장 위치 = 현재 실행 중인 스레드
```

---

# 2. ThreadLocal이 동작하는 구조

ThreadLocal은 JVM 레벨에서 이렇게 구성됩니다:

```
Thread
└── ThreadLocalMap
        └── [ThreadLocal_instance] = Map<String, String> (MDC 저장소)
```

즉, 각 스레드가 자신만의 공간을 갖는 구조입니다.

그래서:

* 요청 A를 처리하는 스레드 T1 → T1의 ThreadLocalMap에 MDC 저장
* 요청 B를 처리하는 스레드 T2 → T2의 ThreadLocalMap에 MDC 저장

둘 사이에 MDC 정보가 섞이지 않습니다.

---

# 3. 실제로 MDC.put()은 어디에 저장되나?

예를 들어:

```java
MDC.put("traceId", "aaa-bbb");
```

이 호출은 다음 순서를 따릅니다:

1. SLF4J MDC 클래스 → 내부 ThreadLocal 참조
2. 해당 ThreadLocal은 현재 스레드의 ThreadLocalMap에서 값을 저장
3. ThreadLocalMap에 `"traceId" = "aaa-bbb"`가 들어감

이때 현재 스레드는:

* 톰캣 요청 처리 스레드
* 배치 작업 스레드
* 스프링 @Async 스레드
* 스케줄러 스레드

등 어떤 스레드든 상관없지만, **calls MDC.put()을 실행한 그 스레드**입니다.

---

# 4. SLF4J/Logback이 하는 역할

프레임워크가 하는 일은 다음뿐입니다:

✔ ThreadLocal을 정의하고
✔ 값을 저장하거나 꺼내는 API(MDC.put, MDC.get)를 제공하고
✔ Logback 패턴에서 `%X{key}`로 값을 꺼내도록 연결할 뿐

**MDC 값을 저장할 스레드를 따로 만들지 않습니다.**

---

# 5. 쉽게 비유하면

* SLF4J/Logback = “각 스레드마다 사용할 수 있는 빈 노트(ThreadLocal)를 준비해둔 사람”
* 스레드 = “그 노트를 실제로 들고 있는 사람”
* MDC.put() = “그 노트에 ‘traceId=abc’를 적음”
* 로그 출력 = “노트에서 traceId를 읽어서 로그에 포함”

SLF4J/Logback이 **노트를 보관하는 것이 아니라**,
스레드가 그 노트를 갖고 있습니다.

---

# 6. 그래서 clear()가 필수인 이유

톰캣의 스레드 풀에서는 스레드를 재사용합니다.

즉 다음 요청에서 같은 스레드가 재사용되면,
ThreadLocalMap에 남아 있는 **이전 요청의 MDC 값이 그대로 남아 있는 상태**가 됩니다.

그래서 반드시:

```java
finally {
    MDC.clear();
}
```

로 정리해야 합니다.

---

원하면

* ThreadLocalMap의 실제 구조
* Logback MDC 구현 코드 분석
* 톰캣 스레드 풀과 MDC의 정확한 상호작용

도 설명해드릴게요.

아래 설명은 **톰캣 스레드 풀(Tomcat Executor / Worker Thread)**과 **MDC(ThreadLocal 기반)**가 실제 요청 처리 과정에서 어떻게 상호작용하는지 흐름 중심으로 정리한 것입니다.
이 부분을 정확히 이해하면 “왜 MDC.clear()가 필수인지”, “왜 비동기에서 MDC가 깨지는지”가 명확해집니다.

---

# 1. 톰캣 스레드 풀 구조

톰캣은 들어오는 HTTP 요청을 처리하기 위해 **고정된 개수의 스레드(worker thread)**를 미리 만들어 놓습니다.
이 스레드들은 요청이 오면 일을 하고, 완료되면 다시 반납되어 재사용됩니다.

단순 구조:

```
Tomcat
└── Executor (ThreadPool)
      ├── WorkerThread-1
      ├── WorkerThread-2
      ├── WorkerThread-3
      └── ...
```

각 스레드는 **ThreadLocalMap**을 하나씩 갖고 있습니다.

---

# 2. 요청 처리 시 MDC 작동 흐름

서블릿 기반 환경에서 요청이 들어오면 톰캣 스레드 풀의 스레드가 하나 배정됩니다.

흐름:

### (1) 요청 도착

```
HTTP Request → Tomcat → WorkerThread-N에게 할당
```

### (2) WorkerThread-N이 스레드 실행 시작

이 스레드가 사용됨:

```
ThreadLocalMap (비어 있음)
```

### (3) 필터 또는 인터셉터에서 MDC.put 실행

예:

```java
MDC.put("traceId", "abc-123");
```

내부적으로:

```
WorkerThread-N
└── ThreadLocalMap
        └── MDC_ThreadLocal -> {"traceId": "abc-123"}
```

즉, **스레드 안에만 저장됨**.

### (4) 로그 출력 시 Logback이 이 ThreadLocalMap에서 traceId를 읽음

```
[%X{traceId}] 요청 처리 중...
```

로그 출력 예:

```
[abc-123] 사용자 로그인 시작
```

### (5) 필터 finally에서 MDC.clear()

요청이 끝나면:

```java
MDC.clear();
```

ThreadLocalMap에서 MDC 값을 제거:

```
WorkerThread-N
└── ThreadLocalMap
        └── MDC_ThreadLocal -> {}
```

### (6) WorkerThread-N이 다시 스레드 풀로 반납됨

---

# 3. clear()가 왜 필수인가?

스레드 재사용 때문입니다.

만약 clear 하지 않으면:

요청 A 처리:

```
MDC.put("traceId", "AAA")
```

WorkerThread-5의 ThreadLocalMap:

```
{"traceId": "AAA"}
```

clear() 누락됨 → 값이 그대로 남음

다음 요청 B가 WorkerThread-5에 배정되면:

```
MDC.get("traceId") → "AAA"
```

전혀 다른 요청 B의 로그에 요청 A의 traceId가 출력됨.

이렇게 되면 **로그가 완전히 잘못되고 추적 불가능**해집니다.

그래서 MDC는 **수명 = 반드시 요청 하나**로 관리해야 함.

---

# 4. 왜 비동기/쓰레드 이동에서 MDC가 깨지는가?

MDC는 **ThreadLocal 기반**이므로 다음과 같은 상황에서 문제 발생:

## (1) @Async 사용

스프링 @Async는 별도의 Executor 스레드에서 실행됩니다.

그러면:

```
필터 실행 → traceId 저장 (Thread-A)
↓
비동기 실행 → Thread-B에서 실행 (ThreadLocal 값 없음)
```

→ Thread-B에서는 MDC 값 없음 → 로그에서 traceId 사라짐.

## (2) CompletableFuture supplyAsync

ForkJoinPool 같은 다른 스레드 사용
→ ThreadLocal값이 복사되지 않음

## (3) WebFlux(Reactor)

Reactor는 스레드가 계속 바뀜 → ThreadLocal 기반 MDC가 전혀 유지되지 않음
→ Reactor Context 사용 필요.

---

# 5. 톰캣 스레드 풀이 스레드를 재활용하는 방식 (정확한 메커니즘)

톰캣의 WorkerThread 기본 구조는 다음과 같습니다:

### WorkerThread 실행 루프(단순화)

```java
while (true) {
    Runnable task = getTaskFromQueue();
    task.run();                 // 요청 처리
    // run()이 끝나면 다음 요청을 기다림
}
```

여기서 중요한 점:

* `ThreadLocalMap`은 스레드가 죽기 전까지 유지된다.
* 스레드는 요청마다 새로 생성되지 않는다.
* 따라서 MDC(Map)는 clear하지 않으면 계속 남아 있다.

즉:

```
ThreadLocalMap은 스레드가 살아 있는 동안 유지되는 ‘메모지’  
요청은 그 메모지를 빌려 쓰는 방식
```

---

# 6. 실제 톰캣 처리 흐름 이미지로 요약

```
[요청 A]
    ↓
WorkerThread-7 배정
    ↓
MDC.put("A")
    ↓
로그 → [A] ...
    ↓
MDC.clear()
    ↓
스레드 풀로 반납

[요청 B]
    ↓
WorkerThread-7 재사용
    ↓
MDC.put("B")
    ↓
로그 → [B] ...
    ↓
MDC.clear()
```

스레드가 같아도 **ThreadLocalMap을 초기화(clear)**했기 때문에
요청 간 값이 섞이지 않는다.

---

# 7. 결론 정리

* MDC는 ThreadLocal 기반이라 “스레드 = 저장 공간”이다.
* 톰캣은 스레드를 재사용하므로 MDC 값은 반드시 요청마다 clear해야 한다.
* 로그백이 스레드를 따로 만드는 것이 아니라, 톰캣 worker thread의 ThreadLocalMap에 저장하는 구조이다.
* 비동기나 다른 스레드에서 실행되는 코드에서는 MDC 값이 자연스럽게 전파되지 않는다.
* 비동기로 MDC를 전파하려면 Decorator나 Mapped Diagnostic Context Wrapper 같은 별도 설정이 필요하다.

---

원하시면

* 실제 Logback MDC 내부 구현 코드(Source) 흐름
* ThreadLocalMap의 구조(entry, hash, reference 관계)
* 비동기 Executor에 MDC를 자동 전파하는 실무 코드

도 더 깊게 설명해드릴 수 있어요.

스레드 이름(thread name)만으로는 **요청을 정확하게 추적할 수 없기 때문**입니다.
겉으로 보면 “요청을 처리하는 스레드 이름이 있으니 그걸로 추적하면 되지 않나?”라고 생각할 수 있지만, 실무에서는 스레드 이름만으로는 절대 충분하지 않습니다.

아래에서 이유를 명확하게 설명할게요.

---

# 1. 톰캣은 스레드를 **재사용**한다

가장 중요한 이유입니다.

예를 들어:

* 요청 A → WorkerThread-12가 처리
* 요청 B → WorkerThread-12가 **다시 사용**

즉:

```
[WorkerThread-12] 로그인 요청
[WorkerThread-12] 상품 조회 요청
```

이렇게 되면 **어떤 로그가 A인지 B인지 구분할 방법이 없습니다.**

스레드 이름은 “스레드 고유 식별자”일 뿐이고
요청과는 1:1 관계가 아닙니다.
**스레드 재사용 = 스레드 이름으로는 요청 식별 불가**

---

# 2. 하나의 요청이 여러 스레드를 사용할 수도 있음

일반적인 서블릿 환경은 요청 당 하나의 스레드지만, 실무에서는 다음이 쉽게 발생합니다:

## (1) 비동기 처리 (@Async, CompletableFuture)

서블릿 스레드 → 비동기 스레드로 넘어감

```
http-nio-80-exec-10  →  taskExecutor-3
```

이 경우 스레드 이름이 바뀌므로 요청 흐름이 끊깁니다.

## (2) 외부 API 호출 시 callback 기반 스레드 사용

Reactor/Netty/WebClient는 thread hopping이 빈번하게 일어납니다.

결과: 스레드 이름으로 추적 불가

---

# 3. 동일 스레드에서 여러 요청이 거의 동시에 처리될 수도 있음 (병렬 처리)

예를 들어, Executor 안에서 두 개의 비동기 작업이 같은 스레드로 스케줄링될 가능성이 있는데, 이 두 작업은 서로 다른 HTTP 요청에서 시작되었을 수 있습니다.

이 경우:

```
[task-thread-1] A의 로그
[task-thread-1] B의 로그
```

스레드 이름이 같아도 요청이 다릅니다.
스레드 이름은 “작업 스케줄링 단위”이지 “요청 단위”가 아닙니다.

---

# 4. 운영 환경에서 스레드 종류가 많다

톰캣 스레드만 있는 게 아닙니다.

예:

* http-nio-80-exec-12
* SimpleAsyncTaskExecutor-5
* scheduling-1
* webclient-reactor-http-nio-3
* ForkJoinPool-1-worker-5

각 요청은 이 중 여러 가지 스레드를 오갈 수 있습니다.
스레드 이름이 요청 ID 역할을 할 수 없습니다.

---

# 5. traceId는 “요청 단위에 대한 고유 ID”

반면 traceId는 **요청 단위**에 붙는 ID입니다.

요청 하나가 여러 스레드를 거쳐도 traceId는 전파할 수 있습니다.

```
Request A → traceId = A123
Request B → traceId = B999
```

각 로그에서 traceId만 보면 요청 흐름이 명확히 정리됩니다.

---

# 6. traceId가 있어야 하는 이유 요약

| 이유                            | 설명                  |
| ----------------------------- | ------------------- |
| 스레드는 재사용된다                    | 스레드 이름이 요청을 구별하지 못함 |
| 요청이 다른 스레드로 넘겨질 수 있다          | thread hopping 발생   |
| 비동기/Reactive 환경에서 스레드가 계속 바뀐다 | 스레드 기반 식별 불가능       |
| 여러 요청이 한 스레드에 섞일 수 있다         | Executor 스케줄링 문제    |
| 운영 로그 추적이 불가능하다               | traceId MUST        |

---

# 7. 짧게 정리하면

스레드 = “작업을 수행하는 노동자”
traceId = “각 작업서류에 붙이는 고유 번호”

노동자(worker)만 보고 작업을 구분할 수 있는 게 아니라
“서류에 붙은 번호(traceId)”가 필요합니다.

---

# 8. 실제 운영 환경에서 traceId 없으면 생기는 일

로그가 이렇게 나옵니다:

```
[http-nio-3-exec-5] 로그인 시작
[taskExecutor-7] 외부 API 호출
[taskExecutor-7] 응답 수신
[http-nio-3-exec-5] 로그인 완료
[taskExecutor-7] 상품 조회 시작  ← 다른 요청이 섞임
[taskExecutor-7] 상품 조회 실패
```

스레드 재사용 때문에
어떤 로그가 어떤 요청의 것인지 알 수 없게 됩니다.

traceId를 넣으면:

```
[A123] 로그인 시작
[A123] 외부 API 호출
[A123] 응답 수신
[A123] 로그인 완료

[B999] 상품 조회 시작
[B999] 상품 조회 실패
```

요청 흐름이 선명해집니다.

---

원하시면

* traceId를 자동 생성하고 MDC에 넣는 필터 코드
* Sleuth/Micrometer Tracing 활용 방법
* 비동기·Reactor 환경에서 traceId 전파 방법

도 이어서 설명드릴게요.

아주 정확한 포인트를 짚으셨어요.
아래 세 가지만 명확히 알면 ThreadLocal·MDC·비동기·traceId 전파가 완전히 이해됩니다.

---

# 핵심 요약 (먼저 답부터)

1. **ThreadLocal 인스턴스는 스레드마다 있는 것이 아니다.**
   하나의 ThreadLocal 객체를 여러 스레드가 공유하지만,
   **값은 각 스레드의 ThreadLocalMap에 각각 저장된다.**

2. **비동기에서 스레드가 바뀌면 ThreadLocalMap도 달라진다.**
   따라서 MDC/traceId 값이 사라진 것처럼 보인다.

3. **traceId 전파란, 이전 스레드의 ThreadLocalMap 값을 새로운 스레드의 ThreadLocalMap에 넣어주는 것**(복사)이다.
   즉, **ThreadLocal 값의 "스레드 간 복제" 과정**이다.

아래에서 상세히 설명할게요.

---

# 1. ThreadLocal의 정확한 구조

### ThreadLocal이 스레드마다 따로 생성되는 것이 아니다

ThreadLocal은 **딱 1개 인스턴스만 생성**됩니다.

예:

```java
static ThreadLocal<String> tl = new ThreadLocal<>();
```

여기서 `tl`은 하나뿐입니다.

### 그럼 스레드마다 다른 값을 가지는 구조는 어떻게 되나?

**ThreadLocalMap이 스레드 내부에 존재**합니다.

구조는 이렇게 됩니다:

```
ThreadLocal(Object) 1개  ─────────►  ThreadLocalMap은 각 스레드마다 분리되어 존재
                                    (Thread 내부 필드)
```

즉:

* ThreadLocal = 키 역할
* ThreadLocalMap = 스레드가 보유한 저장소
* ThreadLocalMap에 `(ThreadLocal → 값)` 형태로 저장

간단히:

```
Thread-A
└── ThreadLocalMap
       └── tl -> "AAA"

Thread-B
└── ThreadLocalMap
       └── tl -> "BBB"
```

ThreadLocal은 하나지만,
값은 스레드 내부 ThreadLocalMap마다 따로 저장됨.

---

# 2. 비동기 작업에서 값이 사라지는 이유

비동기 실행 시 스레드가 바뀐다고 할 때:

### 원래 요청 처리 스레드

```
Thread-A
└── ThreadLocalMap
       └── tl -> "traceId = 123"
```

### @Async 혹은 Executor가 새 스레드 사용

```
Thread-B
└── ThreadLocalMap
       └── tl -> (비어 있음)
```

ThreadLocal은 같지만,
Thread-B의 ThreadLocalMap에는 값이 없음.

그래서 비동기 코드에서 MDC/traceId가 **사라진 것처럼 보이는 것**입니다.

---

# 3. “traceId 전파”란 무엇인가?

정확하게 말하면:

> 이전 스레드의 ThreadLocalMap에 있던 traceId 값을
> 새로운 스레드의 ThreadLocalMap에 **직접 복사해 넣는 것**이다.

예를 들어,

Thread-A:

```
threadA.ThreadLocalMap = { tl -> "traceId=ABC" }
```

비동기 실행 전에 traceId를 변수로 꺼내고:

```java
String traceId = MDC.get("traceId");
```

비동기 스레드 Thread-B에서 다시:

```java
MDC.put("traceId", traceId);
```

결과:

Thread-B:

```
threadB.ThreadLocalMap = { tl -> "traceId=ABC" }
```

이것이 **traceId 전파(propagation)**입니다.

---

# 4. 프레임워크가 자동으로 전파해주는 방식

## 1) Spring Cloud Sleuth / Micrometer Tracing

비동기 실행 시 **Runnable/Callable을 감싸서**
ThreadLocal 값을 새 스레드로 넘겨줍니다.

즉:

```
Thread-A ThreadLocalMap → 복사 → Thread-B ThreadLocalMap
```

이걸 자동으로 해주는 것.

## 2) Executor 프록시로 전파

커스텀 Executor 만들 때 아래 방식으로 감쌈:

```java
executor.execute(() -> {
    MDC.put("traceId", oldTraceId);
    task.run();
    MDC.clear();
});
```

---

# 5. 정리

### Q1. ThreadLocal이 스레드마다 따로 만들어지는가?

아니다.
ThreadLocal은 하나지만, 값은 각 스레드의 ThreadLocalMap 안에 따로 저장된다.

### Q2. 비동기에서 스레드가 바뀌면 왜 값이 없어지나?

ThreadLocalMap이 스레드 내부 저장소이기 때문에
스레드가 달라지면 ThreadLocalMap도 다르고,
그 안에 값이 당연히 없기 때문이다.

### Q3. traceId 전파란?

예전 스레드(Thread-A)의 ThreadLocalMap에서 traceId를 꺼내서
새로운 스레드(Thread-B)의 ThreadLocalMap에 다시 MDC.put으로 넣는 것.
즉, ThreadLocal 값의 스레드 간 복사.

---

추가로 원하시면

* ThreadLocalMap의 실제 내부 구조(entry 배열, 해시, 참조 등)
* MDC가 ThreadLocal을 어떻게 감싸는지 실제 Logback 코드 흐름
* Executor에서 MDC 자동 전파 데코레이터 구현 예제

까지 보여드릴게요.

아래는 **MDC → SLF4J → Logback**으로 이어지는 실제 코드 흐름을 단계별로 추적해 설명한 것입니다.
즉, MDC.put()이 호출되면 어떤 실제 코드가 돌아가고, ThreadLocal에 어떻게 데이터가 저장되는지를 **실제 Logback 소스 기반으로** 보여드립니다.

(버전은 Logback 1.2.x 기준이지만 현재 구조도 거의 동일합니다.)

---

# 1. 전체 구조 요약

MDC.put()이 호출되면 흐름은 다음과 같습니다.

```
MDC.put()  (SLF4J API)
    ↓
MDCAdapter.put()   (Logback 구현체 LogbackMDCAdapter)
    ↓
ThreadLocal<Map<String, String>>에 저장
    ↓
로그 출력 시 %X{} 패턴에서 get()으로 읽음
```

즉,

* SLF4J MDC는 **인터페이스(추상 API)**
* Logback은 **실제 구현체(MDCAdapter)**
* 값은 **ThreadLocal<Map<String, String>>** 에 저장됨

---

# 2. SLF4J MDC.put() → Logback 구현체 호출

SLF4J의 `org.slf4j.MDC` 클래스 코드를 보면:

```java
public static void put(String key, String val) {
    MDCAdapter mdc = MDC.mdcAdapter;
    mdc.put(key, val);
}
```

여기서 `mdcAdapter`는 런타임에 Logback이 제공한 구현체(`LogbackMDCAdapter`)가 주입됩니다.

즉:

```
MDC.put() → LogbackMDCAdapter.put()
```

---

# 3. LogbackMDCAdapter 내부 구조 (가장 핵심)

Logback은 MDCAdapter를 다음 구조로 가지고 있습니다.

(Logback 1.2.x 기준)

```java
public class LogbackMDCAdapter implements MDCAdapter {

    final ThreadLocal<Map<String, String>> copyOnThreadLocal = new ThreadLocal<>();

    @Override
    public void put(String key, String val) {
        Map<String, String> map = copyOnThreadLocal.get();
        if (map == null) {
            map = new HashMap<>();
            copyOnThreadLocal.set(map);
        }
        map.put(key, val);
    }

    @Override
    public String get(String key) {
        Map<String, String> map = copyOnThreadLocal.get();
        if (map != null) {
            return map.get(key);
        }
        return null;
    }

    @Override
    public void clear() {
        copyOnThreadLocal.remove();
    }
}
```

### 여기서 중요한 점

1. `ThreadLocal<Map<String, String>> copyOnThreadLocal`
   → **ThreadLocal은 딱 1개 인스턴스**
   → 실제 값 저장은 각 스레드의 ThreadLocalMap에 있음

2. `put()` 호출 시

```
ThreadLocal.get() → 현재 스레드의 ThreadLocalMap에서 Map<String,String> 찾기
없으면 새 Map 생성 → ThreadLocal.set()
그 Map에 key/value 저장
```

3. `get()`은 그대로 현재 스레드의 Map에서 값 조회

4. `clear()`는 ThreadLocal.remove()
   → 스레드의 ThreadLocalMap 항목 제거

---

# 4. 로그 출력 시 MDC 값 읽기 (PatternLayout)

Logback에서 로그 포맷을 해석할 때 `%X{key}` 패턴을 사용합니다.

PatternLayout 코드:

```java
public String convert(ILoggingEvent event) {
    Map<String, String> mdc = event.getMDCPropertyMap();
    String value = mdc.get(key);
    return value;
}
```

ILoggingEvent 생성 시 Logback은 MDCAdapter에서 MDC 맵을 가져옵니다:

```java
Map<String, String> mdcMap = mdcAdapter.getCopyOfContextMap();
```

LogbackMDCAdapter의 구현:

```java
public Map<String, String> getCopyOfContextMap() {
    Map<String, String> map = copyOnThreadLocal.get();
    if (map == null)
        return null;
    return new HashMap<>(map);
}
```

즉,

* 로그 이벤트 생성 시, 현재 스레드의 ThreadLocalMap에서 MDC 복사
* PatternLayout이 로그 출력 시 그 복사본을 사용

---

# 5. ThreadLocal이 실제로 어떻게 저장하는가 (JDK 구조)

ThreadLocal의 실제 구조는 다음과 같습니다:

```
Thread
 └── threadLocals (ThreadLocalMap)
        └── Entry[ThreadLocal → Map<String,String>]
```

즉:

* ThreadLocal 객체는 키(key)
* 실제 값(Map<String,String>)은 Thread의 내부 Map(ThreadLocalMap)에 저장

그래서 스레드가 바뀌면 MDC 값이 없어지는 것처럼 보입니다.

---

# 6. 정리: MDC가 ThreadLocal을 감싸는 방식

### ⑴ SLF4J는 MDC API 제공

```java
MDC.put("traceId", "abc");
```

### ⑵ Logback이 MDCAdapter 구현체 제공

```java
ThreadLocal<Map<String, String>>
```

### ⑶ put()/get()/clear()를 ThreadLocal에 위임

LogbackMDCAdapter의 핵심 구현:

```java
copyOnThreadLocal.set(newMap)
copyOnThreadLocal.get()
copyOnThreadLocal.remove()
```

### ⑷ 로그 출력 시 MDC 값 복사하여 LogEvent에 포함

패턴 `%X{traceId}`로 출력

---

# 7. 왜 ThreadLocal을 사용하는가?

* 요청 스레드마다 독립된 MDC 공간을 만들어야 한다
* 다른 요청 스레드와 섞이면 안 된다
* 톰캣은 스레드를 재사용하므로 매우 빠르다
* ThreadLocal로 구현하면 오버헤드 최소화

MDC는 **“스레드별 데이터 저장”**이라는 목적에 ThreadLocal이 가장 적합하기 때문에 사용됩니다.

---

원하시면

* ThreadLocalMap 내부 엔트리 구조 (Entry[], 해시, 참조 방식)
* Logback source에서 MDC 초기화 과정
* AsyncAppender가 MDC를 어떻게 보존하는지
* Reactor(웹플럭스)에서 MDC를 전파하기 위한 Hook(reactor.util.context) 구조

까지 더 깊은 레벨로 진행해드릴게요.
