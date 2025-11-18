- 핸들러 어댑터는 다양한 인터페이스를 가진 컨트롤러를 프론트 컨트롤러가 호출할 수 있도록 중간에서 연결하고, 컨트롤러 실행 후 결과를 프론트 컨트롤러가 처리할 수 있는 형태(ModelAndView 등)로 변환합니다.
- HttpServletRequest 의존성을 제거하면 컨트롤러의 로직만을 순수하게 테스트하기 쉬워지며, 코드 또한 단순해집니다. 이는 V3 설계의 핵심 목표 중 하나였습니다.
- 스프링 MVC의 DispatcherServlet은 모든 클라이언트 요청을 하나의 진입점에서 중앙 집중식으로 받아 처리하고 적절한 핸들러(컨트롤러)에게 위임하는 프론트 컨트롤러 패턴을 구현한 것입니다.
- http://localhost:8080/s6/old-controller 하면 OldController.handleRequest 실행된다.

`@Component("/s6/old-controller")` 때문입니다.

정확히 말하면, 스프링에서 `@Component` 애노테이션의 value 값은 “빈 이름(bean name)”으로 사용됩니다.
즉, `@Component("빈이름")` 형태로 지정하면 스프링 컨테이너에 등록될 때 그 이름이 바로 빈 이름이 됩니다.

---

# 왜 URL처럼 보일까?

`@Component("/s6/old-controller")` 라고 적혀 있으면
스프링은 이를 그대로 bean name = "/s6/old-controller" 로 사용합니다.

즉, URL처럼 보이지만, 사실 스프링 MVC URL 매핑과는 전혀 관계없습니다.

단지 빈 이름이 URL 문자열일 뿐입니다.

---

# 정리

* `@Component("이름")` → 이 값이 그대로 빈 이름이 됨
* 그래서 `@Component("/s6/old-controller")` → 빈 이름 = "/s6/old-controller"
* 이 이름이 우연히 URL처럼 생겼을 뿐, 매핑을 의미하지는 않음
* `OldController`는 `Controller` 인터페이스를 구현했지만,
  요즘 스프링에서는 사용되지 않는 구식 핸들러 방식 (HandlerAdapter 1번)이므로
  URL 매핑을 하려면 `HandlerMapping` 설정이 따로 필요함.

---

필요하시면

> 왜 `Controller` 인터페이스 구현체는 스프링 MVC에서 바로 URL 매핑이 안 되는지
>
> * `BeanNameUrlHandlerMapping`이 어떤 역할인지
    >   상세히 설명드릴게요.

그렇다면 `BeanNameUrlHandlerMapping` 이 동작하고 있다는 뜻입니다.

스프링 MVC에는 여러 HandlerMapping 전략이 있는데, 그중 하나가 바로:

# BeanNameUrlHandlerMapping

이 매핑 전략은 다음 규칙을 가집니다:

> 빈 이름이 URL 패턴과 일치하면 그 빈을 컨트롤러로 사용한다.

즉:

```java
@Component("/s6/old-controller")
public class OldController implements Controller { ... }
```

여기서 빈 이름이 "/s6/old-controller" 이기 때문에
요청 URL:

```
/s6/old-controller
```

과 정확히 일치합니다.

그래서 스프링은 이 컨트롤러를 찾아 실행하게 됩니다.

---

# 스프링이 어떤 HandlerMapping을 등록했기에 그런가?

스프링 MVC가 초기화되면 기본 HandlerMapping들이 자동 등록되는데, 그중 하나가 바로:

### 1. RequestMappingHandlerMapping

→ @RequestMapping 기반 컨트롤러 매핑

### 2. BeanNameUrlHandlerMapping

→ 빈 이름이 URL이면 그 URL로 매핑

그래서, 특별한 설정을 안 해도 BeanNameUrlHandlerMapping이 빈 이름을 보고 URL로 매핑합니다.

---

# 실행되는 흐름

1. 요청 URL: `/s6/old-controller`
2. BeanNameUrlHandlerMapping이 모든 빈 이름 확인
3. 빈 이름 중 `/s6/old-controller` 와 동일한 이름을 가진 컴포넌트 발견
4. 해당 빈(OldController)을 핸들러로 결정
5. OldController.handleRequest() 실행

따라서 다음 출력이 찍힌 것:

```
OldController.handleRequest
```

---

# 내가 의도하지 않아도 URL로 인식되는 이유

* `@Component` 의 value = 빈 이름
* 빈 이름이 `/` 로 시작하면 URL로 보임
* BeanNameUrlHandlerMapping이 빈 이름을 URL 매핑 규칙으로 사용하기 때문

즉, URL 매핑을 만든 것이 아니라, 빈 이름이 우연히 URL 형식이라서 스프링이 URL로 인식한 것입니다.

---

원하시면

* BeanNameUrlHandlerMapping이 언제 등록되는지
* 요즘 스프링에서 왜 사실상 사용되지 않는지
* 대신 @RequestMapping 기반 매핑이 어떻게 동작하는지
  더 깊게 설명해드릴게요.

아주 핵심적인 스프링 MVC 동작 구조를 보여주는 내용입니다.
스프링 MVC는 클라이언트 요청을 처리할 때 HandlerMapping 과 HandlerAdapter 두 단계를 거칩니다.

당신이 보여준 리스트는

> 스프링 컨테이너가 기본으로 등록하는 HandlerMapping, HandlerAdapter 목록
> 을 의미합니다.

아래에서 아주 명확하게 정리해드릴게요.

---

# 1. HandlerMapping (핸들러 매핑)

요청 URL이 들어왔을 때, “어떤 컨트롤러를 호출할까?” 를 결정하는 역할입니다.

스프링 기본 HandlerMapping 목록:

```
0 = RequestMappingHandlerMapping   : @RequestMapping 기반 컨트롤러
1 = BeanNameUrlHandlerMapping      : 빈 이름이 URL과 같은 컨트롤러
```

각각 설명하면:

## (1) RequestMappingHandlerMapping

* 우리가 가장 많이 사용하는 방식
* `@Controller` + `@RequestMapping`, `@GetMapping`, `@PostMapping` 등을 가진 메서드를 분석해서 매핑
* 최신 스프링의 기본이며 주력 방식

예:

```java
@GetMapping("/hello")
public String hello() { ... }
```

## (2) BeanNameUrlHandlerMapping

* 빈 이름(= beanName)을 URL로 인식하는 매핑
* 빈 이름이 "/something" 형태라면 요청 URL "/something"과 자동 매핑
* 과거 방식
* 지금은 일반적으로 사용하지 않지만, 스프링이 여전히 제공

예:

```java
@Component("/s6/old-controller")
public class OldController implements Controller { ... }
```

---

# 2. HandlerAdapter (핸들러 어댑터)

HandlerMapping이 “어떤 컨트롤러를 호출할지” 찾으면,
HandlerAdapter는 “그 컨트롤러를 어떻게 실행할지” 를 알려주는 역할입니다.

핸들러는 타입이 다양할 수 있기 때문에 하나의 공통 인터페이스로 실행할 수 없음.
그래서 핸들러마다 맞는 Adapter가 필요함.

스프링 기본 HandlerAdapter 목록:

```
0 = RequestMappingHandlerAdapter   : @RequestMapping 방식 컨트롤러 실행
1 = HttpRequestHandlerAdapter      : HttpRequestHandler 구현체 실행
2 = SimpleControllerHandlerAdapter : Controller 인터페이스 구현체 실행
```

각각 설명하면:

## (1) RequestMappingHandlerAdapter

* `@RequestMapping` 기반 컨트롤러를 처리
* 최신 스프링 MVC의 중심
* 파라미터 바인딩, 모델 처리, 응답 렌더링 등이 여기에서 이루어짐

예:

```java
@GetMapping("/hello")
public String hello() { ... }
```

## (2) HttpRequestHandlerAdapter

* `HttpRequestHandler` 인터페이스를 구현한 컨트롤러 처리
* `Servlet` 과 유사한 방식
* 예: `ResourceHttpRequestHandler`

## (3) SimpleControllerHandlerAdapter

* 스프링의 오래된 방식인 `Controller` 인터페이스 구현체 처리
* `handleRequest()` 메서드 하나만 있음
* Annotation 기반 이전에 사용되던 구조

예:

```java
public class OldController implements Controller {
    public ModelAndView handleRequest(...) { ... }
}
```

---

# 이 두 개가 함께 동작하는 방식

요청이 들어왔을 때:

1. HandlerMapping 이 URL을 보고 핸들러(컨트롤러)를 찾는다.
2. HandlerAdapter 가 해당 핸들러 타입에 맞는 실행 방법을 선택한다.
3. 컨트롤러 실행 → ModelAndView 생성 → ViewResolver → 화면 렌더링

---

# 왜 `/s6/old-controller` 실행됐던 건가?

그 이유는 다음 조합 때문입니다:

1. BeanNameUrlHandlerMapping
   → 빈 이름 "/s6/old-controller" 을 보고 OldController를 핸들러로 선택

2. SimpleControllerHandlerAdapter
   → Controller 인터페이스를 구현한 OldController 실행

그래서 handleRequest() 가 실행된 것.

---

# 정리

* HandlerMapping = “어떤 컨트롤러?”
* HandlerAdapter = “그 컨트롤러를 어떻게 실행?”

이 구조 덕분에 스프링은 다양한 방식의 컨트롤러를 유연하게 지원할 수 있음.

---

원하시면

> DispatcherServlet 내부에서 HandlerMapping → HandlerAdapter → ViewResolver로 이어지는 전체 요청 처리 흐름
> 을 그림으로 정리해서 설명해드릴까요?

아주 기초적인 HTTP 관점에서, DispatcherServlet이 어떤 역할을 하는지, 그리고 요청이 들어왔을 때 내부에서 어떤 순서로 흐르는지를 말로만 명확하게 설명해줄게.

---

# 1. DispatcherServlet이란 무엇인가?

가장 간단하게 말하면:

> 스프링 MVC의 프론트 컨트롤러(Front Controller)

즉,

* 모든 HTTP 요청을 가장 먼저 받아서
* 어디로 보내야 할지,
* 어떤 컨트롤러를 실행해야 할지,
* 어떤 방식으로 데이터를 처리해야 할지,
* 어떤 뷰를 렌더링해야 할지

전부 결정하는 "중앙 관리자"이다.

HTTP 요청이 오면 무조건 DispatcherServlet부터 거쳐간다.

---

# 2. HTTP 요청이 왔을 때 내부에서 일어나는 흐름

HTTP 요청을 기준으로 아주 직관적으로 설명한다.

---

## (1) 클라이언트가 HTTP 요청을 보낸다

예:

```
GET /s6/old-controller
```

---

## (2) 톰캣이 요청을 받는다

톰캣은 서버니까, 모든 HTTP 요청이 가장 먼저 도달한다.

그리고 톰캣은 스프링 MVC를 이렇게 설정해둔다:

> 모든 요청을 DispatcherServlet으로 보낸다.

보통 web.xml 또는 자동 설정으로 다음과 같이 등록되어 있다:

```
/*  → DispatcherServlet에게 넘긴다
```

즉, DispatcherServlet이 HTTP 요청의 첫 관문이다.

---

## (3) DispatcherServlet이 요청을 받는다

이제 스프링 MVC가 본격적으로 동작하기 시작한다.

DispatcherServlet의 첫 번째 역할:

### “이 URL을 처리할 핸들러(컨트롤러)는 누구인가?”

---

## (4) HandlerMapping 들에게 물어본다

DispatcherServlet은 여러 개의 HandlerMapping을 가지고 있다.

예:

* RequestMappingHandlerMapping
* BeanNameUrlHandlerMapping
  등

DispatcherServlet은 이들에게 순서대로 묻는다:

> “/s6/old-controller 이 URL을 처리할 컨트롤러 너희 중 누구냐?”

그러면 BeanNameUrlHandlerMapping이 답한다:

> “제 빈 이름이 /s6/old-controller 이므로 제가 처리할 수 있습니다.”

이렇게 해서 핸들러(컨트롤러) 객체가 선택된다.

---

## (5) 선택된 핸들러를 실행할 HandlerAdapter 선택

각 컨트롤러는 형태가 다 다르다.

* @RequestMapping 기반의 메서드
* 옛날 Controller 인터페이스 기반
* HttpRequestHandler 기반

그래서 DispatcherServlet은 핸들러 종류를 보고 다시 물어본다:

> “이 컨트롤러를 실행하려면 어떤 방식으로 호출해야 하지?”

이걸 담당하는 것이 HandlerAdapter.

OldController는 Controller 인터페이스이므로
SimpleControllerHandlerAdapter 가 선택된다.

---

## (6) HandlerAdapter가 컨트롤러 실행

HandlerAdapter는 컨트롤러를 실제로 호출한다.

OldController라면:

```
handleRequest(request, response)
```

이 메서드를 실행한다.

컨트롤러는 보통 ModelAndView 를 반환한다.

---

## (7) ViewResolver에게 뷰 이름 전달

컨트롤러 실행 결과가 ModelAndView라면:

* model = 화면에 넘길 데이터
* viewName = “home”, “members”, “list” 같은 논리적 이름

DispatcherServlet은 다음으로 ViewResolver에게 묻는다:

> “viewName = members 를 실제 JSP나 템플릿 파일로 바꿔줘.”

ViewResolver는 보통 이렇게 동작한다:

```
/WEB-INF/views/ + viewName + .jsp
```

즉:

```
/WEB-INF/views/members.jsp
```

로 변환한다.

---

## (8) View 객체가 렌더링(render)

ViewResolver가 찾은 View는 JSP든 Thymeleaf든 HTML을 렌더링한다.

즉, 화면을 만드는 과정이 여기서 이뤄진다.

---

## (9) DispatcherServlet이 최종 HTTP 응답 반환

렌더링된 HTML을 HTTP 응답 바디에 실어서 클라이언트에게 되돌려준다.

---

# 전체 흐름 요약 (HTTP 관점 설명)

1. HTTP 요청 → 톰캣
2. 톰캣 → DispatcherServlet
3. DispatcherServlet → "어떤 컨트롤러 처리?" → HandlerMapping들에게 질문
4. 적절한 컨트롤러 선택
5. DispatcherServlet → "이 컨트롤러는 어떻게 실행?" → HandlerAdapter 선택
6. HandlerAdapter → 컨트롤러 실행
7. 컨트롤러 → ModelAndView 반환
8. DispatcherServlet → ViewResolver로 viewName을 물리 경로로 변경
9. View → 렌더링
10. DispatcherServlet → 렌더링 결과를 HTTP 응답으로 반환
11. 클라이언트가 HTML을 받음

---

원한다면

* 이 흐름을 순수 HTTP 기반 서버와 비교해서
* "스프링이 왜 이렇게 복잡한 구조를 만들었는지"
  도 설명해줄 수 있어.

세 단어는 비슷한 맥락에서 쓰이지만, 가리키는 개념은 분명히 다릅니다.
특히 스프링을 배우는 과정에서 많이 혼동하는 부분이라 깔끔하게 정리해드릴게요.

---

# 1. Domain (도메인)

가장 넓고 추상적인 개념.

> 해당 애플리케이션이 다루는 “업무 영역” 전체

예:

* 주문, 결제, 회원, 배송
* 게시글, 댓글, 좋아요
* 쇼핑몰, 예약 시스템 등

즉, “무엇을 다루는 시스템인가?” 를 설명하는 말.

도메인 안에는 여러 개념들이 존재하고, 그 개념들을 구현한 클래스들이 도메인 객체라 불린다.

---

# 2. Entity (엔티티)

도메인의 구성 요소 중에서도 데이터베이스 테이블과 1:1로 매핑되는 객체.

주로 JPA에서 @Entity 를 붙인 클래스를 말한다.

특징:

* DB 테이블 구조를 기반으로 함
* 식별자(id) 를 가짐
* 영속성 컨텍스트에서 관리됨
* 저장/조회/수정 같은 데이터 중심 모델

예:

```java
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private int age;
}
```

Entity는 “도메인 객체”의 한 종류이지만
모든 도메인이 엔티티인 것은 아니다.

예: 값 객체(Value Object)는 엔티티가 아니다.

---

# 3. Model (스프링 MVC의 Model)

이건 개념이 완전히 다름.

> 컨트롤러에서 뷰(JSP, Thymeleaf 등)로 데이터를 넘기는 바구니.

예:

```java
model.addAttribute("member", member);
return "memberView";
```

Model은 비즈니스 영역과 관계없고 DB와도 관계없다.
단순히 화면 렌더링용 데이터 전달 도구다.

---

# 차이를 한 줄씩만 요약하면

* Domain: 애플리케이션이 다루는 전체 개념과 문제 공간
* Entity: 도메인 중 DB에 저장되는 객체 (@Entity)
* Model: 뷰로 전달하기 위한 데이터 컨테이너 (MVC에서만 사용)

---

# 비유로 아주 쉽게 설명하면

웹사이트를 만든다고 가정해보자.

## Domain

“회원 가입, 로그인, 글 작성, 댓글 같은 기능이 있는 서비스”
→ ‘무엇을 하는 서비스인가’라는 업무 영역

## Entity

DB에 저장하는 것들

* MEMBER 테이블에 저장되는 Member 엔티티
* ARTICLE 테이블에 저장되는 Article 엔티티

## Model

화면에 보여주기 위해 전달되는 데이터

* JSP/Thymeleaf에 넘기는 값

예:

```java
model.addAttribute("username", member.getName());
```

이건 화면에 출력하기 위한 데이터일 뿐, DB와는 무관하다.

---

# 하나 더 중요한 사실

Entity를 Model에 넣어서 뷰에 넘길 수도 있다:

```java
model.addAttribute("member", memberEntity);
```

이건 가능하지만,

* Entity는 DB와 맞닿아 있는 “영속 객체”
* Model은 화면 렌더링용 “데이터 전달자”

역할은 완전히 다르다.

---

원하면

* Entity vs DTO 차이
* Domain 객체 디자인 원칙
* Model과 ModelAndView 차이
  이것도 추가로 자세히 설명해줄게.

좋아.
Domain, Entity, Model, DTO까지 스프링에서 자주 나오는 개념들을 전체적으로, 서로의 관계까지 명확하게 정리해줄게.
혼란이 싹 사라지도록 구조적으로 설명하겠다.

---

# 1. Domain (도메인)

## 정의

애플리케이션이 다루는 업무 영역 전체.

예:

* 쇼핑몰: 상품, 회원, 주문, 배송
* 게시판: 게시글, 댓글, 태그
* 예약 서비스: 예약, 결제, 일정

## Domain의 구성 요소

* Entity (엔티티)
* Value Object (값 객체)
* Domain Service (도메인 서비스)
* Aggregate (애그리거트)

즉, Entity는 Domain의 일부이다.

---

# 2. Entity (엔티티)

## 정의

DB 테이블과 매핑되고 식별자(id)를 가지며, 영속성 컨텍스트에서 관리되는 객체.

주요 특징:

* @Entity 붙음
* @Id 필드 존재
* DB에 저장됨
* 동일성(identity) 개념이 중요

예:

```java
@Entity
public class Member {
   @Id @GeneratedValue
   private Long id;
   private String name;
   private int age;
}
```

## Entity는 Domain 객체인가?

→ 예. 하지만 Domain 전체 중 일부.
도메인 모델링은 DB 중심이 아니라, “업무 규칙” 중심이기 때문.

---

# 3. Value Object (값 객체, VO)

Entity와 비교되는 Domain 구성 요소.

특징:

* @Embeddable 로 사용하기도 함
* 불변(immutable) 특징
* 식별자 없음
* 같은 값이면 동일

예:

```java
@Embeddable
public class Address {
   private String city;
   private String street;
}
```

---

# 4. Domain Service

엔티티 하나 안에 넣기 애매한 비즈니스 로직을 담당하는 도메인 계층의 서비스.

예:

```java
public class DiscountService {
   public Money discount(Member member, Order order) { ... }
}
```

---

# 5. Model (스프링 MVC Model)

## 정의

> 컨트롤러에서 뷰로 데이터 전달하는 통신용 객체.

DB와도, 도메인 모델과도 직접적인 관련 없음.

예:

```java
model.addAttribute("member", member);
```

그저 화면 렌더링에 필요한 데이터를 담는 용도.

---

# 6. DTO (Data Transfer Object)

## 정의

> 데이터 전달 전용 객체.
> 계층 간 이동을 위해 존재한다.

용도 예:

* API 응답/요청
* 컨트롤러 → 뷰로 전달할 데이터
* 서비스 → 컨트롤러로 전달할 데이터

DTO는 도메인 규칙을 가지면 안 된다.

예:

```java
public class MemberDto {
   private Long id;
   private String name;
}
```

## DTO를 사용하는 이유

1. Entity를 외부에 노출하면 위험
2. 필요 데이터만 보낼 수 있음
3. API 스펙을 DTO 기준으로 명확히 작성 가능
4. Entity 변경이 외부 계약(API)에 영향을 주지 않음

---

# 7. Entity vs DTO

| 비교 항목   | Entity          | DTO                     |
| ------- | --------------- | ----------------------- |
| 목적      | 비즈니스 로직, DB 저장  | 데이터 전달                  |
| 주 사용 계층 | Domain Layer    | Presentation Layer, API |
| 식별자     | 있음              | 없음(대부분)                 |
| 영속성     | JPA 영속성 환경에서 관리 | 그냥 객체                   |
| 비즈니스 규칙 | 포함됨             | 포함되면 안 됨                |
| API 노출  | 노출하면 위험         | 안전                      |

---

# 8. 그럼 Model과 DTO는 뭐가 다른가?

| Model                    | DTO                |
| ------------------------ | ------------------ |
| 스프링 MVC에서 뷰로 데이터 전달용     | 데이터를 담는 클래스        |
| 객체 아님. 인터페이스/뷰바구니        | 클래스 형태             |
| JSP/Thymeleaf 등 뷰 렌더링 용도 | JSON 응답/요청 등 모든 계층 |
| 값만 저장                    | 구조 설계 가능           |

Model은 “데이터 담는 바구니”이지만,
DTO는 “전달할 데이터의 구조” 자체이다.

---

# 9. Controller 흐름에서 각각의 위치

HTTP 요청 처리 흐름에서 어디서 쓰이는지 정리.

## (1) 요청 데이터 → DTO

요청 값 파싱할 때 DTO 사용

```java
@PostMapping("/member")
public String join(@RequestBody MemberRequestDto dto)
```

## (2) 서비스 수행 → Entity 처리

서비스 계층에서는 Entity로 도메인 로직 처리.

## (3) 응답 → DTO 사용

클라이언트에게 돌려줄 때도 DTO 사용.

## (4) Model은 뷰 렌더링 시에만 사용

JSP/Thymeleaf 같은 템플릿 엔진 사용할 때만 등장.

---

# 10. 전체 관계를 한 문장으로 설명하면

* Domain은 애플리케이션의 문제 영역 전체.
* 그 안에서 Entity는 DB와 매핑되는 핵심 객체.
* DTO는 계층 간 데이터 전달용.
* Model은 뷰 렌더링을 위해 컨트롤러에서 화면으로 값을 옮기는 도구.

---

원한다면 이걸 기반으로

* 그림으로 구조도 그려주거나
* 실제 코드 예제 포함해서 정리해줄 수도 있어.

스프링 MVC에서 Model 과 ModelAndView 는 비슷해 보이지만, 실제로는 목적과 사용 방식이 다르다.
헷갈리지 않도록 가장 명확하고 직관적으로 설명해줄게.

---

# 1. Model이란?

## 정의

> 뷰로 전달할 데이터를 담는 바구니.
> 컨트롤러에서 화면에 보여줄 데이터를 넣는 용도.

## 특징

* 오직 “데이터 담기” 기능만 있음
* 뷰 이름은 컨트롤러의 return 문자열로 전달
* JSP/Thymeleaf 같은 뷰 렌더링에만 사용

## 예시

```java
@GetMapping("/hello")
public String hello(Model model) {
    model.addAttribute("name", "Spring");
    return "hello"; // 논리적 뷰 이름
}
```

`hello` 라는 뷰 이름과 `model` 은 서로 별도로 전달된다.

---

# 2. ModelAndView란?

## 정의

> “데이터(Model)” + “뷰 이름(View)” 을 한번에 담는 객체.

## 특징

* 데이터 + 뷰 이름을 한 객체로 묶어서 반환
* 옛날 스프링 방식
* 주로 `Controller` 인터페이스 기반일 때 사용
* 요즘은 잘 안 쓰고, @RequestMapping 방식에서는 거의 필요 없음

## 예시

```java
@Override
public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
    ModelAndView mv = new ModelAndView("hello");
    mv.addObject("name", "Spring");
    return mv;
}
```

---

# 3. 핵심 차이 비교

| 비교 항목      | Model                           | ModelAndView         |
| ---------- | ------------------------------- | -------------------- |
| 전달하는 데이터   | 데이터만                            | 데이터 + 뷰 이름           |
| 뷰 이름 설정 방식 | return "viewName"               | 생성자/메서드로 viewName 지정 |
| 등장 위치      | @Controller, @RequestMapping 방식 | 옛날 Controller 인터페이스  |
| 사용 빈도      | 매우 높음                           | 거의 사용 안 함            |
| 목적         | 뷰 렌더링용 데이터 전달                   | 뷰 이름과 데이터를 객체 하나로 처리 |

---

# 4. 예제를 통해 더 쉽게 비교

## Model 사용하는 최신 방식

```java
@GetMapping("/members")
public String list(Model model) {
    List<Member> members = memberService.findAll();
    model.addAttribute("members", members);
    return "members";
}
```

뷰 이름: `"members"`
뷰로 넘길 데이터: `members`

---

## ModelAndView 사용하는 구 방식

```java
@Override
public ModelAndView handleRequest(...) {
    List<Member> members = repository.findAll();
    ModelAndView mv = new ModelAndView("members");
    mv.addObject("members", members);
    return mv;
}
```

여기서는 뷰 이름과 데이터를 한꺼번에 넣는다.

---

# 5. 왜 Model이 더 자주 쓰일까?

* @RequestMapping 기반 컨트롤러에서는 메서드의 반환값이 곧 뷰 이름
* Model은 파라미터로 주입받아서 바로 사용 가능
* 코드가 짧고 단순해짐
* 최신 스프링 철학은 “컨트롤러를 최대한 단순하게”

그래서 ModelAndView는 특별한 경우 아니면 잘 안 쓰임.

---

# 결론

## Model

> 뷰로 넘길 데이터만 넣는 도구.

## ModelAndView

> 뷰 이름 + 데이터까지 모두 담는 객체.
> 옛날 방식이며 현재는 거의 사용되지 않음.

---

필요하면

* “왜 스프링이 ModelAndView를 도입했었는지”
* “ModelAttribute 자동 바인딩 흐름”
* “스프링이 내부적으로 Model을 어떻게 ViewResolver로 전달하는지”
  도 더 깊게 설명해줄게.

스프링 MVC가 내부에서 Model을 어떻게 ViewResolver까지 전달하는지를 처음부터 끝까지 “흐름 중심”으로 아주 정확하게 설명해줄게.
이 부분은 스프링 MVC의 핵심 동작이라, 정확히 이해하면 전체 구조가 눈앞에서 연결된다.

---

# 전체 흐름 요약

1. 컨트롤러 실행
2. Model에 데이터 추가
3. HandlerAdapter가 Model을 담아서 ModelAndView 형태로 DispatcherServlet에게 반환
4. DispatcherServlet이 ViewResolver에게 전달
5. ViewResolver가 물리 뷰(Route)로 변환
6. View.render(model) 호출 → HTML 생성
7. 클라이언트에게 반환

이중 중요한 포인트는
컨트롤러는 Model만 넣지만, 실제로는 내부에서 ModelAndView로 변환된다는 점이다.

아래에서 흐름을 자세하게 설명할게.

---

# 1. 컨트롤러가 호출되고 Model이 파라미터로 들어감

예:

```java
@GetMapping("/hello")
public String hello(Model model) {
    model.addAttribute("name", "Spring");
    return "hello";
}
```

여기서 사용한 Model은 실제 구현체 `ModelMap`, `ExtendedModelMap` 같은 객체이다.

컨트롤러는 단지:

* model에 데이터 추가
* 뷰 이름 문자열 반환

만 한다.

하지만 이 상태는 불완전한 정보이다.
“뷰 이름 + 모델”이 함께 있어야 ViewResolver가 동작할 수 있다.

---

# 2. HandlerAdapter가 Model과 뷰 이름을 모아서 ModelAndView로 변환

DispatcherServlet → RequestMappingHandlerAdapter 호출
→ HandlerAdapter는 컨트롤러를 실행한다.

컨트롤러 결과:

* 뷰 이름(String)
* Model 객체 (컨트롤러 파라미터로 받은 Model)

HandlerAdapter는 이 둘을 모아서 내부적으로 아래처럼 만든다:

```
ModelAndView mv = new ModelAndView(viewName, model);
```

즉, 개발자가 ModelAndView를 명시적으로 안 써도
스프링이 자동으로 ModelAndView를 생성한다.

---

# 3. HandlerAdapter → DispatcherServlet에게 ModelAndView 반환

DispatcherServlet은 이제 다음 두 정보를 얻게 된다:

* viewName (논리적 뷰 이름)
* model (뷰로 넘길 데이터)

따라서 DispatcherServlet은 뷰 결정 단계로 넘어갈 수 있다.

---

# 4. DispatcherServlet이 ViewResolver에게 뷰 이름 전달

이제 DispatcherServlet은 viewName을 기반으로 ViewResolver를 찾는다.

예:

```
"hello"
```

ViewResolver는 설정된 규칙에 따라 물리 경로로 바꾼다.

예:
InternalResourceViewResolver

```
prefix = /WEB-INF/views/
suffix = .jsp
```

따라서:

```
"hello" -> "/WEB-INF/views/hello.jsp"
```

그리고 View 객체를 만든다:

```
InternalResourceView("/WEB-INF/views/hello.jsp")
```

---

# 5. DispatcherServlet이 View.render(model, request, response) 호출

이제 ViewResolver가 생성한 View 객체를 가지고
DispatcherServlet은 다음을 호출한다:

```
view.render(model, request, response);
```

여기서의 model은
컨트롤러에서 넣었던 model이다.

이 과정에서:

* JSP라면 request.setAttribute() 로 모델 데이터를 모두 넣는다.
* Thymeleaf라면 템플릿 엔진으로 데이터 넘긴다.

그리고 HTML 렌더링이 일어난다.

---

# 6. render 결과가 HTTP 응답 바디에 들어가서 클라이언트로 전달

렌더링된 HTML이 response의 출력 스트림에 기록된다.
그리고 클라이언트는 결과 화면을 받는다.

---

# 핵심 흐름을 한 줄로 정리

컨트롤러(Model 사용) → HandlerAdapter(Model + viewName → ModelAndView 생성)
→ DispatcherServlet → ViewResolver → View.render(model).

컨트롤러는 Model만 사용하지만,
내부에서는 항상 ModelAndView 형태로 움직인다.

---

# 즉, Model → ModelAndView 변환 시점은 어디인가?

RequestMappingHandlerAdapter 가 컨트롤러 실행 후 변환한다.

이게 가장 중요한 포인트이다.

---

원한다면

* RequestMappingHandlerAdapter 내부 코드를 의사 코드로 풀어 설명하거나
* Model이 request attribute 로 바뀌는 과정(JSP 기준)
* 스프링 MVC 호출 스택 흐름을 순서도처럼 정리해
  도 더 깊게 설명해줄게.
