아주 중요한 포인트라서 정확하게 정리해줄게.
예외 처리 과정에서 **preHandle → handler → postHandle → afterCompletion** 중
**postHandle이 언제 생략되고 HandlerExceptionResolver가 개입하는지**를 명확하게 설명할게.

---

# 핵심 결론 먼저

1. **핸들러(컨트롤러) 내부에서 예외가 발생하면 `postHandle()`은 실행되지 않는다.**
   대신 **즉시 `HandlerExceptionResolver`가 호출된다.**

2. **오류 페이지(`/error`) 요청이 발생해도 `postHandle()`은 실행되지 않는다.**
   이유: `/error` 요청은 **기존 요청의 postHandle 지점이 아니라, 새로운 REQUEST 또는 FORWARD 흐름으로 DispatcherServlet이 다시 실행되기 때문**.

---

---

# 1. postHandler가 호출되는 조건

`postHandle()`은 **핸들러(컨트롤러)가 정상적으로 반환된 경우에만** 실행된다.

즉,

* 컨트롤러에서 return 정상 수행
* ModelAndView 생성 성공
* 예외 없이 처리됨

이 세 조건이 충족되어야 실행된다.

---

# 2. 예외가 발생하면 postHandler가 왜 건너뛰어질까?

핸들러 과정에서 예외가 발생하면 DispatcherServlet은 아래처럼 흐른다.

```
preHandle()
handler 호출 → 예외 발생
→ postHandle() 생략
→ HandlerExceptionResolver 호출
→ error 처리
→ afterCompletion()
```

**postHandle은 “정상 처리된 이후” 단계이므로 예외가 발생하면 존재하지 않는 단계가 되어 호출되지 않는다.**

---

---

# 3. HandlerExceptionResolver는 어떤 상황에서 호출될까?

**핸들러 실행 중 예외가 발생했을 때 항상 호출된다.**

예:

* 컨트롤러에서 RuntimeException 발생
* 컨트롤러에서 파라미터 바인딩 오류 (특정 경우)
* @ResponseStatusException 발생
* 필터나 인터셉터 postHandle 전에 예외 발생
* MessageConverter에서 예외 발생

이 경우는 모두 postHandle 대신 **HandlerExceptionResolver가 먼저 호출된다**.

---

---

# 4. error 페이지로 FORWARD가 발생하면 postHandler가 실행되나?

**기존 요청의 postHandle은 실행되지 않는다.**

이게 핵심이다.

오류 흐름은 이렇게 진행된다:

```
1요청
→ preHandle
→ handler 호출
→ 예외 발생
→ postHandle 건너뜀
→ HandlerExceptionResolver
→ InternalErrorViewResolver가 "/error"로 FORWARD
```

그리고 FORWARD된 `/error` 요청은 **새로운 DispatcherServlet 흐름을 탄다**.

즉:

### 1차 요청 흐름

```
preHandle
handler
X postHandle
afterCompletion
```

### 2차 요청(/error) 흐름

```
preHandle (새로운 요청처럼)
handler(/error 페이지 컨트롤러 또는 BasicErrorController)
postHandle (여기서야 실행됨)
afterCompletion
```

그래서 다음이 성립한다:

* **기존 요청의 postHandle은 절대 실행되지 않는다.**
* **오류 페이지(/error)를 렌더링하는 흐름의 postHandle은 실행된다.**

---

# 5. 흔히 잘못 이해하는 부분을 정리

| 상황                       | postHandle 실행 여부          |
| ------------------------ | ------------------------- |
| 컨트롤러 정상 실행               | 실행됨                       |
| 컨트롤러에서 예외 발생             | 실행 안 됨                    |
| 예외 → /error 페이지로 forward | 원래 요청의 postHandle은 실행 안 됨 |
| /error 페이지 렌더링 단계        | postHandle 실행됨 (새 흐름)     |

---

# 최종 정리

### Q1. 예외가 발생하면 postHandler 대신 HandlerExceptionResolver가 호출되는 경우는 언제인가?

→ **컨트롤러(핸들러)에서 예외가 던져진 모든 경우**.

### Q2. ERROR 요청이 발생하면 postHandler는 실행 안 돼?

→ **기존 요청의 postHandle은 실행되지 않는다.**
→ **하지만 /error로 FORWARD된 후 새 요청 단계에서 postHandle은 정상적으로 실행된다.**

---

필요하면 전체 흐름을 요청 별로 ASCII 다이어그램으로 만들어줄까?

정확히 구분해야 하는 부분이 있어.
**ERROR 포워드가 일어나면 “필터는 다시 실행되지 않지만, 인터셉터는 다시 실행된다.”**
이게 핵심이다.

아래에서 왜 그런지 흐름을 기준으로 설명할게.

---

# 1. 포워드(forward)는 필터 체인을 다시 타지 않는다

서블릿 스펙에 따라 **forward는 기존 FilterChain 내부에서 발생**한다.

즉:

```
FilterChain.doFilter()
   → (DispatcherServlet 실행)
       → 예외 발생
       → /error로 forward
       → forward는 기존 FilterChain 안에서 처리됨
   → 필터 종료
```

여기서 중요한 점은:

* **필터는 DispatcherType.REQUEST일 때만 실행**하도록 설정하는 경우가 대부분
* forward는 DispatcherType.FORWARD
* forward 시 FilterChain이 다시 시작되지 않음

그래서:

### 결론

**ERROR 포워드 후에는 필터는 다시 실행되지 않는다.**

---

# 2. 하지만 인터셉터는 다시 실행된다

인터셉터는 **DispatcherServlet 내부 동작에 걸려 있는 컴포넌트**다.

즉, DispatcherServlet이 새로운 요청 흐름을 시작할 때마다 실행된다.

포워드 해도 DispatcherServlet은 새 “핸들러 찾기 → 인터셉터 체인 실행”을 수행한다.

흐름은 이렇게 된다.

### 1차 요청(일반 요청)

```
필터
인터셉터 preHandle
핸들러 실행 중 예외 발생
인터셉터 postHandle 실행 안 됨
HandlerExceptionResolver 실행
→ /error로 forward 지시
인터셉터 afterCompletion(1차 요청 종료)
```

### 2차 요청(/error 포워드)

```
필터 실행 없음  ← 포워드이기 때문에
인터셉터 preHandle(다시 실행됨)
핸들러(BasicErrorController)
인터셉터 postHandle
인터셉터 afterCompletion
```

---

# 결론

## ERROR 포워드 후에는?

| 컴포넌트                  | 다시 실행되는가?   | 이유                                                           |
| --------------------- | ----------- | ------------------------------------------------------------ |
| **필터(Filter)**        | **실행되지 않음** | forward는 기존 FilterChain 안에서 일어나며 DispatcherType.FORWARD이기 때문 |
| **인터셉터(Interceptor)** | **실행됨**     | DispatcherServlet이 새로운 요청 흐름을 다시 시작하기 때문                     |

---

# 정리 한 줄

**ERROR 포워드 후에는 필터는 다시 실행되지 않고, 인터셉터는 다시 실행된다.**

필요하면 실제 Spring 로그 흐름을 기반으로 디버그 레벨 트레이스를 보여줄 수도 있어.

여기서 말하는 **인터셉터(HandlerInterceptor)**는 하나의 메서드가 아니라 **3단계 훅(hook)**으로 구성돼.
즉, `preHandle`, `postHandle`, `afterCompletion`이 모두 “인터셉터”다.

따라서 **인터셉터 = postHandle**이 아니라
**인터셉터 안에 postHandle이 포함되어 있을 뿐**이다.

아주 명확하게 구조를 정리해줄게.

---

# 1. HandlerInterceptor의 3단계

스프링 MVC 인터셉터는 다음과 같이 동작 순서가 나뉜다.

```
preHandle
    ↓
handler(컨트롤러)
    ↓
postHandle      (핸들러가 정상 반환된 경우에만 실행)
    ↓
view rendering
    ↓
afterCompletion (예외 발생 여부와 관계없이 마지막에 실행)
```

즉:

* **인터셉터 전체 = preHandle + postHandle + afterCompletion**
* **postHandle은 인터셉터의 일부일 뿐**

---

# 2. ERROR 포워드 흐름에서 실행되는 것들

예외가 발생한 경우:

### 1차 요청

```
preHandle
handler → 예외 발생
postHandle 실행 안 됨
HandlerExceptionResolver
afterCompletion (실행됨)
```

### /error FORWARD(2차 요청)

여기서 다시 **인터셉터 전체가 새로 실행됨**.

```
preHandle(2차)
handler(BasicErrorController)
postHandle(2차)
afterCompletion(2차)
```

즉:

* postHandle이 “인터셉터가 아니다”가 아니라
* “예외가 나면 postHandle 단계를 건너뛸 뿐, 인터셉터 자체는 계속 존재한다”
* 오류 페이지로 포워드되면 해당 요청에서도 인터셉터의 3단계가 다시 동작한다

---

# 3. 다시 질문의 의미에 직접 답하면

**Q. “인터셉터가 postHandle 아니야?”**
→ 아니다. 인터셉터는 세 개의 메서드를 가진 구조 전체를 의미한다.
→ postHandle은 그중 하나일 뿐이다.

**Q. ERROR 포워드 후엔 인터셉터가 없어지는 건가?**
→ 아니다. 인터셉터는 포워드된 요청에서도 동일하게 다시 실행된다.

---

원하면 preHandle/postHandle/afterCompletion의 실행 시점을 순서도 형태로 그려줄게.

맞아. **스프링 MVC 인터셉터는 기본적으로 ERROR 포워드 요청에서도 다시 실행되지만**,
실무에서는 **대부분 이를 원치 않기 때문에 “ERROR 요청에서는 실행되지 않도록” 필터나 인터셉터에 설정을 추가한다**는 의미다.

즉,
**기본 동작 = ERROR 포워드에도 인터셉터는 다시 실행됨**
하지만
**실무 패턴 = ERROR 요청은 제외하도록 명시적으로 막음**

왜냐하면 ERROR 포워드는 "핸들러 예외 이후 자동으로 발생하는 내부 요청"이기 때문에,
여기서까지 인터셉터가 작동하면 중복 로깅, 중복 인증 처리, 잘못된 로직 실행 등이 벌어지기 때문이야.

아래에서 실제로 어떻게 “대체로 실행되지 않도록 설정하는지”를 정리해줄게.

---

# 1. 필터(Filter)에서 ERROR 요청을 제외하는 방법

필터는 DispatcherType을 직접 제어할 수 있으므로 가장 명확하게 설정 가능해.

```java
@Bean
public FilterRegistrationBean<Filter> logFilter() {
    FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new LogFilter());
    bean.addUrlPatterns("/*");
    bean.setDispatcherTypes(DispatcherType.REQUEST); // ERROR, FORWARD 등 제외
    return bean;
}
```

이렇게 하면:

* 정상 요청(REQUEST)만 필터 통과
* ERROR, FORWARD 요청은 필터 실행 안 됨

---

# 2. 인터셉터(HandlerInterceptor)는 DispatcherType 설정이 없으므로 직접 조건을 넣어 제외

스프링 인터셉터는 ServletDispatcherType 설정 기능이 없다.
그래서 대부분 "조건 코드를 직접 넣어서 제외"한다.

## 방법 1. ERROR 경로를 excludePathPatterns에서 제외

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LogInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns("/error", "/error/**");
}
```

→ ERROR 포워드 요청은 인터셉터 전부(pre/post/afterCompletion)에서 제외됨.

## 방법 2. preHandle에서 DispatcherType으로 구분

(`HttpServletRequest#getDispatcherType()` 이용)

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getDispatcherType() != DispatcherType.REQUEST) {
        return true; // ERROR, FORWARD에서는 그냥 지나감
    }
    // 정상 요청일 때만 로직 실행
    return true;
}
```

→ FORWARD(ERROR 포함) 요청에서는 인터셉터 로직 자체를 수행하지 않음.

---

# 3. 왜 “대체로 실행되지 않도록” 설정하는가?

ERROR 포워드는 **애플리케이션이 정상적으로 처리해야 하는 요청이 아니다.**
그래서 다음 문제들이 발생할 수 있다.

* 로그 인터셉터 → 같은 요청 로그가 2번 찍힘
* 인증 인터셉터 → 인증 실패 상태에서 또 인증 검사
* 비즈니스 인터셉터 → 잘못된 동작을 ERROR 흐름에서도 반복 실행
* API 타이머 → 같은 요청 시간을 2번 측정

이 때문에 실무에서는:

* 필터 : DispatcherType.REQUEST로 제한
* 인터셉터 : `/error` 또는 DispatcherType으로 직접 제외

이 조합이 일반적이다.

---

# 결론

**맞다. 기본적으로 ERROR 포워드 시 인터셉터는 다시 실행되지만,
실무에서는 이를 막기 위해 “대체로” 필터나 인터셉터에 별도 설정을 추가한다.**

즉:

* 필터 → DispatcherType으로 제외
* 인터셉터 → excludePathPatterns 또는 request.getDispatcherType()으로 제외

그래서 “대체로 실행 안 되도록 설정한다”는 표현이 맞다.

필요하면 ERROR 포워드 전체 호출 흐름을 완성도 있게 정리해줄게.

여기서 핵심은 **“인터셉터 로직을 실행하지 않는다”**는 것이지,
**“요청 자체를 막는다”**는 뜻이 아니다.
그래서 **대부분의 경우 `false`가 아니라 `true`를 반환한다.**

왜냐하면:

* `return false`는 **요청 자체를 더 이상 진행시키지 않고 즉시 중단**시킨다.
* 우리는 ERROR/FORWARD 요청을 “막고 싶은 것”이 아니라
  “인터셉터 로직만 실행하지 않고 그대로 통과시키고 싶은 것”이다.

따라서 **ERROR, FORWARD 요청에서는 `true`로 반환해야 한다.**

---

# 흐름을 예로 들어 명확하게 설명

## 상황

예외 발생 → `/error`로 FORWARD됨
→ 이 FORWARD 요청에서 인터셉터 로직을 실행하고 싶지 않음

그러면 이렇게 처리한다:

```java
@Override
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (request.getDispatcherType() != DispatcherType.REQUEST) {
        // ERROR/FORWARD 요청이므로 인터셉터 로직을 실행하지 않고 그냥 패스
        return true;  // ★ 요청을 통과시킴
    }

    // 정상 REQUEST일 때만 인터셉터 로직 실행
    doSomething();
    return true;
}
```

이게 올바른 방식이다.

---

# 만약 `return false`를 하면 어떻게 될까?

```java
if (request.getDispatcherType() != DispatcherType.REQUEST) {
    return false;
}
```

이렇게 하면 ERROR 또는 FORWARD 요청이 **아예 중단**되어
스프링의 기본 오류 처리 자체가 작동하지 않게 된다.

결과적으로:

* 오류 페이지가 렌더링되지 않음
* BasicErrorController도 실행되지 않음
* 500 오류가 더 큰 오류로 번질 수 있음

즉, “인터셉터 로직을 하지 않는다”가 아니라
“ERROR 요청 자체를 차단”해버리는 효과가 발생한다.

대부분의 경우 **절대 원하지 않는 결과**이다.

---

# 정리

## Q. ERROR/FORWARD 요청에서 `return false` 해야 하는 거지?

→ **아니다. `return true`해야 한다.**

왜냐하면:

* 목적은 “인터셉터 로직을 스킵”이지 “요청 자체를 막는 것”이 아니기 때문이다.

## 올바른 패턴

* `true` + 로직 생략 → ★ 권장
* `false` + 요청 차단 → 오류 처리 자체가 망가짐

---

필요하면 이 부분을 전체 흐름 다이어그램으로 시각적으로 정리해줄게.

핵심을 아주 명확하게 먼저 말하면:

**오류 발생 시 `postHandle()`은 “원래 요청”에서는 애초에 호출되지 않는다.
따라서 postHandle을 막기 위한 코드를 작성할 필요가 없다.**

즉, 컨트롤러에서 예외가 발생하면 DispatcherServlet은 **postHandle 자체를 호출하지 않는다.**
이건 스프링 MVC의 기본 동작이며, 개발자가 막을 필요가 없는 영역이다.

하지만 대부분의 질문은 이걸 의미한다:

> 오류가 나서 `/error`로 forward된 **오류 처리 요청(2차 요청)**의 postHandle도 실행되지 않게 하려면?

이 경우 “오류 페이지 렌더링 요청(`/error`)에서 postHandle을 생략하는 방법”을 말한다.

그 방법을 설명해줄게.

---

# 1. postHandle은 왜 기본적으로 실행되는가?

정리하면:

* **1차 요청(정상 요청)**

    * 예외 발생 → postHandle은 자동으로 생략됨

* **2차 요청(오류 처리 요청 `/error` 또는 포워드 경로)**

    * 정상 요청이므로 postHandle이 실행됨
    * 이걸 원치 않으면 개발자가 직접 막아야 함

즉, 개발자가 막아야 하는 건 **오류 처리 요청(/error)의 postHandle**이다.

---

# 2. postHandle을 “오류 요청에서는 실행되지 않게” 만드는 방법

핵심 아이디어는 **오류 요청인지 검사하고, 오류 요청이면 아무것도 하지 않는 것**이다.

---

# 방법 1. `/error` 경로를 아예 excludePathPatterns로 제외

가장 간단하고 실무에서 가장 많이 사용.

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LogInterceptor())
            .addPathPatterns("/**")
            .excludePathPatterns("/error", "/error/**");
}
```

→ 그러면 `/error` 요청(오류 처리 요청)에서는
`preHandle`, `postHandle`, `afterCompletion` 모두 실행되지 않는다.

**이게 가장 흔한 방식.**

---

# 방법 2. postHandle 메서드 내부에서 조건으로 스킵

```java
@Override
public void postHandle(HttpServletRequest request, HttpServletResponse response,
                       Object handler, ModelAndView modelAndView) {

    if (request.getDispatcherType() != DispatcherType.REQUEST) {
        // ERROR 또는 FORWARD 요청 → postHandle 로직 실행 안 함
        return;
    }

    // 정상 REQUEST일 때만 postHandle 로직 수행
    doSomething();
}
```

→ `/error` forward 흐름에서는 DispatcherType이 FORWARD이므로 실행되지 않는다.

---

# 방법 3. 오류 경로 기반으로 조건 분기

```java
@Override
public void postHandle(HttpServletRequest request, HttpServletResponse response,
                       Object handler, ModelAndView modelAndView) {

    String uri = request.getRequestURI();

    if (uri.equals("/error") || uri.startsWith("/error/")) {
        return; // 오류 처리 요청에서는 실행하지 않음
    }

    doSomething();
}
```

---

# 정리

### 1. “오류 발생 시 postHandle을 실행 안 되게 하려면?”

→ **이미 실행되지 않는다.**
(예외가 발생하면 스프링 MVC가 postHandle 호출을 생략한다.)

### 2. “오류 처리 요청(/error)의 postHandle을 실행 안 되게 하려면?”

아래 중 선택:

* **가장 권장 → excludePathPatterns("/error")**
* 또는 **postHandle 내부에서 DispatcherType으로 예외 처리**
* 또는 **URI 기반 조건 분기**

---

원하면 상황별 흐름을 요청 1/요청 2로 나눠 ASCII 다이어그램으로 정리해줄게.
