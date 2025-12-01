아래 코드는 **“컨트롤러에서 예외를 던지면 어떤 일이 벌어지는가”**를 보여주는 예제다.
줄마다 어떤 의미인지 하나씩 풀어서 설명해볼게.

---

## 1. 웹 애플리케이션은 요청마다 스레드를 하나씩 사용한다

HTTP 요청이 들어오면 톰캣 같은 서블릿 컨테이너는 **스레드 풀에서 스레드 하나를 꺼내서** 그 요청을 처리하게 한다.
즉 `/error-ex` 요청이 들어오면 스레드 하나가 이 메서드를 실행한다.

---

## 2. 웹 애플리케이션은 서블릿 컨테이너 안에서 실행된다

Spring MVC도 결국 톰캣(서블릿 컨테이너) 내부에서 작동한다.
컨트롤러 호출 전에 DispatcherServlet, 필터, 인터셉터 등 서블릿 스펙 기반 처리 과정이 모두 존재한다.

---

## 3. 애플리케이션에서 예외 처리가 안 되면 → 서블릿 밖으로 전달된다

컨트롤러에서 예외를 던졌는데, try-catch로 잡지 않으면 그대로 **Spring MVC → DispatcherServlet → 서블릿 컨테이너** 방향으로 예외가 전파된다.

코드에서:

```java
@GetMapping("/error-ex")
public void errorEx() {
    throw new RuntimeException("예외 발생!");
}
```

이 메서드는 어떤 응답도 만들지 않고 **무조건 예외를 던진다**.
그러면 DispatcherServlet도 처리할 수 없고 결국 예외는 톰캣까지 올라간다.

---

## 4. 그 결과에서 의미하는 것

예외가 서블릿 컨테이너까지 도달하면:

1. **스프링 기본 오류 처리(WhiteLabel Error Page)** 또는
2. **BasicErrorController의 /error 처리**,
3. 또는 직접 등록한 **ExceptionHandler, HandlerExceptionResolver** 가 실행된다.

즉 이 예제는:

* 컨트롤러에서 예외가 발생하면
* Spring MVC는 그 예외를 적절한 오류 시스템으로 전달하고
* 결국 사용자는 오류 페이지 또는 JSON 에러 응답을 보게 된다는 것을 보여주기 위한 코드다.

---

## 전체 흐름을 다시 정리하면

1. `/error-ex` 요청 → 톰캣 스레드 하나가 선택됨
2. DispatcherServlet → 컨트롤러 호출
3. 컨트롤러에서 예외 발생
4. 아무도 예외를 잡지 않음
5. 예외가 DispatcherServlet → 서블릿 컨테이너로 전파
6. 서블릿 컨테이너는 “오류 페이지 처리” 과정으로 이동
7. 스프링 Boot 기준으로는 BasicErrorController가 `/error` 매핑하여 에러 응답 생성

---

## 핵심 포인트

이 코드는 **“예외가 발생하면 DispatcherServlet과 서블릿 컨테이너의 오류 처리 흐름이 어떻게 진행되는지 테스트하는 예제”**다.

---

원하면,

* DispatcherServlet에서 예외를 어떻게 잡는지
* BasicErrorController가 어떻게 호출되는지
* HandlerExceptionResolver의 역할
* @ExceptionHandler가 이 과정에서 어디 들어가는지
  그 흐름도 하나씩 설명해줄게.

“서블릿 컨테이너 밖으로 예외가 나간다”는 표현이 이해 안 되는 건 아주 자연스러운 반응이야.
말 자체가 추상적이기 때문에 실제 실행 과정을 그림처럼 잡아야 감이 와.

아래에서 차근차근 풀어줄게.

---

# 1. **서블릿 컨테이너가 무엇인가?**

웹 애플리케이션은 톰캣 같은 **서블릿 컨테이너(Servlet Container)** 안에서 돌아간다.

서블릿 컨테이너는 다음 역할을 한다:

* HTTP 요청을 받는다
* HttpServletRequest/Response 객체를 만든다
* 필터 실행
* DispatcherServlet 호출
* 컨트롤러 호출
* 응답을 만들어서 브라우저로 돌려준다

즉, **스프링 MVC 또한 컨테이너가 만든 환경 안에서 움직인다.**

---

# 2. **“서블릿 컨테이너 안에서 발생하는 예외”는 컨테이너가 처리할 수 있다**

예를 들어:

* 컨트롤러 실행 중 예외 발생
* DispatcherServlet 실행 중 예외 발생
* Filter 실행 중 예외 발생

이런 예외는 모두 **서블릿 컨테이너가 감싸고 있는 범위** 안에서 일어나는 것들이다.

그래서 서블릿 컨테이너는 이런 예외가 생기면
“오류가 났군. 그러면 내 오류 처리 규칙에 따라 에러 페이지를 보여줘야겠다”
라고 판단한다.

---

# 3. “서블릿 컨테이너 밖으로 예외가 나간다”는 말의 정확한 의미

이 말은 정말로 “물리적으로 밖으로 튀어나간다”는 뜻이 아니다.

**의미:**
서블릿 컨테이너가 제공한 처리 흐름 안에서 예외가 해결되지 않고
컨테이너의 **최상위 요청 처리 루프**까지 예외가 도달했다는 뜻이다.

조금 더 구체적으로:

```
[브라우저 요청]
 → 톰캣 내부 HttpProcessor, Adapter
   → 필터 체인
   → DispatcherServlet
   → 컨트롤러
      ↳ 예외 발생!
   ← 예외 올라감
 ← 예외 올라감
 ← 예외 올라감
 (톰캣의 최상위 요청 처리 루프까지 도달)
```

예외가 **잡히지 않고 계속 위로 전달되면**,
마지막으로 톰캣의 “요청 처리 메인 루프”까지 올라간 뒤
톰캣은 이렇게 판단한다:

“아무도 예외를 핸들링하지 않았네?
그럼 내가 등록해둔 에러 페이지 규칙을 실행해야겠다.”

즉, “톰캣이 오류 페이지를 처리하게 되는 상황”이라는 의미이다.

---

# 4. 왜 이런 말이 쓰이는가?

Spring MVC 예외 처리 흐름은 이렇게 구성된다:

1. 컨트롤러에서 예외 발생
2. HandlerExceptionResolver가 처리하면 내부에서 처리된 것
3. 처리 못 하면 DispatcherServlet에게 전달
4. 그래도 처리 못 하면 서블릿 컨테이너에게 전달
5. 서블릿 컨테이너가 등록한 오류 페이지 규칙에 따라 처리

여기서 4→5 단계에서
**‘컨테이너 밖으로 예외가 나간다’**라는 말을 쓴다.

실제 의미는:

* Spring MVC 내부에서 예외 처리가 끝나지 않았다
* 그래서 톰캣(서블릿 컨테이너)의 오류 처리로 넘어간다

이게 “밖”이라는 표현의 본질이다.
컨테이너 내부 흐름이 끝나면서 컨테이너가 자체 규칙으로 처리한다는 뜻이지
어디 물리적으로 경계를 넘는다는 뜻이 아니다.

---

# 5. 비유로 더 쉽게 설명

스프링 MVC가 “건물 안의 사무실”이라고 하고,
서블릿 컨테이너는 “그 건물 전체”라고 해보자.

컨트롤러에서 예외가 난다 → 사무실 문제
HandlerExceptionResolver에서 해결 못 한다 → 사무실에서는 해결 못 함
DispatcherServlet에서 해결 못 한다 → 층에서도 해결 못 함
그럼 이 문제는 건물 관리실(톰캣)로 넘어간다.

“사무실 밖으로 나갔다”라는 표현은
문제가 **건물 전체 규칙으로 처리되는 단계**에 도달했다는 뜻이다.

---

# 핵심 요약

* “밖”이라는 표현은 물리적 경계를 뜻하지 않는다.
* Spring MVC(필터·인터셉터·DispatcherServlet) 내부에서 해결되지 않고
  **톰캣의 기본 오류 처리 단계로 예외가 넘어갔다**는 의미다.

---

원하면 흐름을 코드 단위로 아주 구체적으로 분석해서
DispatcherServlet 내부에서 예외가 어떻게 Bubble-Up 되는지도 설명해줄게.

이 코드는 **예외를 던지는 게 아니라, 컨트롤러에서 직접 “서블릿 컨테이너에게 오류 발생을 알리는 것”**이다.
`response.sendError(...)`가 핵심이다.

---

# 1. `response.sendError(...)`는 누구에게 신호를 보내는가?

정확히 말하면:

**`sendError()`는 서블릿 컨테이너(톰캣)에게 “오류가 발생했다고 기록하라”는 신호를 보낸다.**

즉, 이 메서드는 다음 역할을 한다:

1. **HttpServletResponse 내부에 오류 상태코드 기록**
2. **톰캣에게 “에러 처리 흐름을 타라”라고 알려줌**
3. 톰캣은 **오류 페이지 매핑 규칙(/error)** 을 실행함
4. 스프링 부트는 그 규칙에 맞춰 **BasicErrorController**나
   **커스텀 오류 페이지**를 호출함

---

# 2. sendError 흐름을 구체적으로 보자

예를 들어:

```java
response.sendError(404, "404 오류!");
```

실제 동작 순서:

1. response 객체에

    * status = 404
    * errorMessage = "404 오류!"
      를 기록한다.

2. 톰캣에게 “이 요청은 정상 응답이 아니라 오류 응답이다”라고 표시된다.

3. 요청 처리가 끝나는 시점에서 톰캣은:
   “오류났네? 그럼 오류 처리 규칙을 실행하자”

4. 톰캣은 **ErrorPage** 매핑을 찾는다
   (Spring Boot는 자동으로 `/error`를 등록해 둔다)

5. `/error`를 다시 호출한다 → 이때 요청은 ErrorDispatcher가 처리

6. Spring Boot는 **BasicErrorController**를 실행하여
   JSON 또는 HTML 오류 응답을 생성한다.

---

# 3. 이것은 ‘예외’와 다르다

예전 코드처럼:

```java
throw new RuntimeException();
```

이건 **예외 발생 → Spring MVC exception resolver → 톰캣**으로 전달된다.

반면 `sendError()`는:

* 예외를 던지지 않음
* 대신 **“상태코드를 강제로 오류 상태로 만들고, 오류 처리 흐름을 타게 함”**

두 방식 모두 결과적으로 톰캣이 `/error` 매핑을 호출하는 건 같다.

---

# 4. 그림으로 요약

### 컨트롤러 내부:

```
response.sendError(404)
 ↓
response에 에러 상태 기록
 ↓
요청 완료 시점에 톰캣이 오류 처리 시작
 ↓
톰캣이 오류 페이지 매핑 검색
 ↓
/error 매핑으로 재요청
 ↓
BasicErrorController 실행
```

즉, **컨트롤러 → (sendError) → 서블릿 컨테이너 오류 처리 시스템** 순서다.

---

# 5. 그래서 “어디에 전달하는가?”에 대한 정확한 답

**sendError는 예외를 스프링에게 전달하는 것이 아니라,
서블릿 컨테이너(톰캣)에게 “오류 상태”를 전달한다.**

그 다음, 톰캣이:

* 에러 페이지 규칙 확인
* `/error`로 디스패치
* 스프링이 이에 맞게 오류 응답 생성

이라는 일련의 오류 처리 흐름을 시작하게 된다.

---

원하면,

* sendError 내부 코드가 어떻게 동작하는지 (HttpServletResponseWrapper 내부)
* DispatcherType.ERROR가 어떻게 동작하는지
* ErrorPage 등록이 실제 코드에서 어떻게 이뤄지는지
  도 더 깊게 설명해줄 수 있어.

정확하게 짚어야 할 부분이라 아주 좋다.
정답은 **“경우에 따라 다르다”**이다.
오류가 어떻게 발생했는지에 따라 **필터, 인터셉터, 리졸버가 다시 실행되기도 하고, 안 되기도 한다.**

단계를 정확히 구분해서 설명해줄게.

---

# 먼저 핵심 한 줄 정리

## 1) 최초 요청 단계

**필터 → 인터셉터 → 컨트롤러 → (여기서 예외 또는 sendError)**
이 흐름은 무조건 실행된다.

## 2) 오류 페이지(`/error`)가 호출될 때

* **필터: 기본적으로 다시 실행된다 (DispatcherType에 따라 달라짐)**
* **인터셉터: 언제나 다시 실행된다**
* **ExceptionResolver는 컨트롤러에서 예외가 난 경우에만 실행된다**
  (sendError는 예외가 없으므로 Resolver는 실행되지 않는다)

---

# 오류 처리 과정별 상세 설명

## 1. 컨트롤러에서 예외 발생 (`throw new RuntimeException`)

흐름:

```
필터(REQUEST)
 → 인터셉터(preHandle)
   → 컨트롤러
      ↳ 예외 발생
   ← 인터셉터(postHandle 실행 안 됨)
 ← ExceptionResolver 동작
 → 그래도 해결 못 하면 톰캣에 예외 전달
 → 톰캣이 /error 디스패치
```

### 이때 중요한 점

* **ExceptionResolver 실행됨**
  (예외가 있으니까)

* `/error`가 다시 호출될 때는?

    * 필터: ERROR 타입에 대해 실행될 수도 있고 안 될 수도 있다
      (필터의 등록 방법에 따라 다름)
    * 인터셉터: 무조건 실행된다

---

## 2. 컨트롤러에서 `response.sendError(...)`

예외는 없다.
흐름:

```
필터(REQUEST)
 → 인터셉터(preHandle)
   → 컨트롤러
      ↳ sendError(404) 실행
   → 인터셉터(postHandle)
 ← 정상적으로 Controller 끝남
(요청 마무리 단계에서 톰캣이 오류 상태 확인)
 → 톰캣이 /error 디스패치 (ERROR)
```

### 이때 중요한 점

* ExceptionResolver는 **실행되지 않는다**
  (예외가 없으니까)

* `/error`로 재요청될 때

    * 필터: ERROR 타입이면 실행
      기본 설정에서는 실행된다
    * 인터셉터: 다시 실행된다

---

# 결국 필터/인터셉터/리졸버가 어떻게 실행되는가?

## 1) 필터(Filter)

필터는 **DispatcherType**에 따라 실행 여부가 결정된다.

DispatcherType 종류:

* REQUEST
* ERROR
* FORWARD
* INCLUDE
* ASYNC

톰캣이 `/error`로 디스패치할 때 DispatcherType.ERROR로 동작한다.

### 필터가 ERROR를 포함하도록 설정되어 있다면

`FilterRegistrationBean.setDispatcherTypes(...)`
설정 없으면 기본적으로 ERROR도 포함되므로
**/error 요청에서도 필터가 다시 실행된다.**

---

## 2) 인터셉터(HandlerInterceptor)

인터셉터는 DispatcherType을 구분하지 않는다.
Spring MVC가 `/error`를 컨트롤러로 매핑하는 순간:

**preHandle 다시 실행됨
→ 컨트롤러(BasicErrorController) 실행
→ postHandle 실행**

인터셉터는 무조건 두 번 실행된다.

---

## 3) ExceptionResolver

**컨트롤러에서 “예외가 발생한 경우에만” 실행된다.**

* 예외 발생
  → DispatcherServlet은 HandlerExceptionResolver 목록 실행
* sendError는 예외 아님
  → Resolver 동작 없음

---

# 두 경우 비교 표

| 상황            | 필터 (다시 실행?)           | 인터셉터 (다시 실행?) | ExceptionResolver 실행? |
| ------------- | --------------------- | ------------- | --------------------- |
| 예외 발생 (throw) | 대부분 YES (ERROR 타입 가능) | YES           | YES                   |
| sendError     | 대부분 YES (ERROR 타입 가능) | YES           | NO                    |

---

# 결론

* **필터는 ERROR 타입 포함 여부에 따라 다시 실행될 수 있고, 기본적으로는 실행됨**
* **인터셉터는 항상 다시 실행됨**
* **리졸버(ExceptionResolver)는 예외가 있을 때만 실행됨**
* 그래서 오류 페이지(`/error`)를 호출할 때 필터/인터셉터가 두 번 실행되는 것처럼 보이는 구조가 맞다.

---

필요하면

* 톰캣이 ERROR 디스패치할 때 내부적으로 어떤 클래스를 거쳐가는지
* DispatcherServlet#doDispatch에서 ERROR 요청 처리 분기
* BasicErrorController 동작 구조
  도 더 깊게 알려줄게.

throw 했는데도 필터나 인터셉터가 **두 번 실행되는 것처럼 보이는 이유**는
“**요청이 두 번 발생하기 때문**”이야.
이걸 이해해야 정확히 감이 온다.

아래에서 하나씩 설명할게.

---

# 1. 핵심 요약

컨트롤러에서 `throw`가 발생하면:

1. **원래 요청(REQUEST)**
   → 필터/인터셉터/컨트롤러 실행
   → 예외 발생

2. **서블릿 컨테이너가 오류 처리용으로 다시 요청(ERROR 디스패치)**
   → 필터/인터셉터가 ‘다시’ 실행됨
   → `/error` 매핑 처리

즉,
**throw → 오류 페이지 요청이 새롭게 발생 → 새 요청에 대해 필터/인터셉터가 다시 실행**
이라는 구조야.

---

# 2. 더 정확한 내부 흐름

### 1) 첫 번째 요청 (DispatcherType.REQUEST)

```
필터(REQUEST)
 → 인터셉터(preHandle)
   → 컨트롤러
      ↳ 예외 발생 (throw)
   ← ExceptionResolver 시도
 ← 그래도 해결 안 되면 예외 톰캣에 전달
```

여기까지는 예상 가능하지?

---

### 2) 두 번째 요청 (DispatcherType.ERROR)

예외가 DispatcherServlet → 톰캣으로 올라가면
톰캣이 이렇게 판단한다:

“예외가 났네? 그럼 내가 등록해둔 오류 페이지 규칙에 따라
/error 경로에 다시 디스패치해야겠다.”

그리고 **서블릿 컨테이너가 직접 `/error`를 호출한다.**

이 Error 디스패치는 하나의 완전한 새로운 요청처럼 취급된다.

```
필터(ERROR)
 → 인터셉터(preHandle)
   → BasicErrorController(/error)
   → postHandle
 ← 응답 반환
```

그래서 필터와 인터셉터가 **다시 실행되는 것**이다.

---

# 3. 왜 “새 요청”처럼 취급될까?

서블릿 컨테이너 톰캣은 오류 처리 흐름을 이렇게 규정하고 있다:

* 컨트롤러에서 예외가 발생하면
* **예외를 catch 하지 않은 채로** 톰캣 최상위 루프까지 전달된다
* 톰캣은 해당 상태코드를 보고 오류 페이지 매핑을 찾는다
* **오류 페이지로 내부 디스패치(FORWARD) 또는 ERROR 디스패치**를 수행한다

**이 ERROR 디스패치는 새로운 요청과 거의 동일한 단계들을 밟기 때문에**
필터·인터셉터가 다시 실행된다.

---

# 4. 왜 ExceptionResolver는 1회만 실행되는가?

ExceptionResolver는 “컨트롤러 내부에서 예외가 발생했을 때만” 실행된다.
오류 페이지 디스패치 때는:

* 이미 예외는 처리된 상태로 간주됨
* `/error` 요청은 정상 컨트롤러 호출

그래서 Resolver는 동작하지 않고,
인터셉터와 필터만 동작한다.

---

# 5. 정말 NEW 요청일까?

아니야.
“완전히 새로운 HTTP 요청”은 아니고,

**기존 요청을 ERROR 경로로 내부에서 FORWARD하는 것**이다.

그러나 Spring MVC 입장에서는
“새로운 요청처럼 보인다”.
왜냐면:

* DispatcherType이 ERROR로 변경
* HandlerMapping이 다시 동작
* HandlerAdapter가 다시 동작
* 인터셉터 체인이 새로 구성

즉 구조상 거의 새 요청을 처리하는 것과 동일하다.

---

# 6. 결론

throw 한 다음에도 필터·인터셉터가 실행되는 이유는:

### **throw → 톰캣 오류 처리 기동 → /error로 재요청(ERROR 디스패치)**

이 재요청이 또다시 Spring MVC의 정상 요청 흐름을 타기 때문이다.

그래서:

* 필터: DispatcherType ERROR 포함 시 다시 실행
* 인터셉터: 항상 다시 실행
* 컨트롤러: BasicErrorController(/error) 실행
* Resolver: 예외가 없어 다시 실행되지 않음

---

원하면,

* DispatcherServlet#doDispatch에서 예외가 터졌을 때 어떤 코드 경로로 빠지는지
* 톰캣의 `StandardHostValve`가 어떻게 오류 페이지로 디스패치하는지
  내부 코드를 기반으로 더 깊게 설명해줄게.

질문을 두 개로 나눠서 아주 명확하게 답해줄게.

---

# 1. **forward를 하면 처음부터 실행되는가? 아니면 컨트롤러부터 실행되는가?**

정답은:

## **forward는 필터부터 다시 실행된다.

컨트롤러부터 시작하는 것이 아니다.**

이게 핵심이다.

다만 forward는 본질적으로 **서블릿 내부에서 일어나는 “내부 이동”**이기 때문에
HTTP 요청을 다시 받는 것은 아니다.
하지만 서블릿 엔진은 forward를 할 때 **다시 디스패치 과정**을 밟는다.

---

## forward 시 실제 실행 흐름

forward 발생 시 DispatcherType.FORWARD로 흐른다:

```
필터(FORWARD 타입이 활성화된 경우 실행)
 → 인터셉터(preHandle)
 → 컨트롤러 호출
 → postHandle
 → view rendering
```

즉, **forward도 Spring MVC의 전체 요청 처리 흐름이 다시 실행된다.**

주의할 점:

* forward 시에도 HandlerMapping → HandlerAdapter → 컨트롤러까지 똑같이 돈다
* 인터셉터도 다시 실행
* 필터는 DispatcherType.FORWARD를 포함할 때 실행

forward는 단순히 "메서드를 다시 호출"하는 것이 아니라
**컨트롤러 매핑 단계부터 전체를 다시 처리**하는 것이다.

---

# 2. `ErrorPage` 등록은 forward인가?

정확히 말하면:

## ✔ **Yes, ErrorPage 동작은 forward(정확히는 ERROR 디스패치)를 사용한다.**

조금 더 정교하게 표현하면:

* 404, 500, 예외를 감지하면
* 톰캣은 등록된 ErrorPage로 **ERROR 디스패치**를 수행한다
* ERROR 디스패치는 forward의 특수한 형태다

즉,

### **ErrorPage → 톰캣이 오류 경로로 “내부 forward”를 하는 것과 같다.**

---

# 3. ERROR 디스패치 vs FORWARD 디스패치

둘 다 내부 디스패치이지만 의미가 다르다.

| 종류      | 언제 발생?                                | DispatcherType | 필터/인터셉터 동작               |
| ------- | ------------------------------------- | -------------- | ------------------------ |
| FORWARD | 개발자가 `RequestDispatcher.forward()` 호출 | FORWARD        | 조건에 따라 필터 실행, 인터셉터 항상 실행 |
| ERROR   | 예외 발생 or sendError                    | ERROR          | 조건에 따라 필터 실행, 인터셉터 항상 실행 |

즉 ErrorPage는 **DispatcherType.ERROR**로 내부 forward되는 구조다.

---

# 4. ErrorPage의 실제 동작

```java
ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
ErrorPage errorPage500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error-page/500");
ErrorPage errorPageEx = new ErrorPage(RuntimeException.class, "/error-page/500");

factory.addErrorPages(errorPage404, errorPage500, errorPageEx);
```

이 설정을 기반으로 톰캣 동작은 다음과 같다:

1. 컨트롤러에서 예외 발생
2. DispatcherServlet에서 처리 못 함
3. 예외가 톰캣까지 올라감
4. 톰캣은 등록된 ErrorPage를 확인
5. 매핑되는 ErrorPage를 찾음
6. 해당 경로(`/error-page/500` 등)로 **ERROR 디스패치 수행**
   (내부 forward)

이때 Spring MVC의 HandlerMapping이 `/error-page/500` 컨트롤러를 찾는다.
그리고 다시 인터셉터·컨트롤러가 실행된다.

---

# 즉, 결론

## 1) forward를 하면 컨트롤러부터가 아니라 필터(조건부) → 인터셉터 → 컨트롤러 전체 흐름을 다시 돈다.

## 2) ErrorPage(`/error-page/...`)도 결국 톰캣이 내부적 forward(정확히는 ERROR 디스패치)를 수행하는 것이다.

---

원하면:

* 톰캣의 `StandardHostValve` 코드가 어떻게 ERROR 디스패치를 하는지
* DispatcherServlet이 ERROR 디스패치를 감지하면 어떤 단계로 동작하는지
* 필터가 어떤 DispatcherType에서 실행되는지 구체적 코드로
  설명해줄 수 있어.

DispatcherType은 **서블릿 컨테이너가 현재 요청을 어떤 “목적”으로 처리하고 있는지 나타내는 값**이다.
즉, “이 요청이 왜 여기로 왔는가?”를 식별하기 위해 서블릿 스펙이 정의한 타입이다.

필터가 언제 실행될지, 어떤 흐름인지 판단할 때 반드시 필요하다.

아주 중요하고, 스프링 MVC 동작을 이해하는 핵심이다.

---

# DispatcherType 종류 (총 5가지)

서블릿 스펙에서 정의한 DispatcherType은 아래 5개다:

| DispatcherType | 의미                                                           |
| -------------- | ------------------------------------------------------------ |
| REQUEST        | 브라우저에서 들어온 최초 HTTP 요청                                        |
| FORWARD        | `RequestDispatcher.forward()`로 내부 전달된 요청                     |
| INCLUDE        | JSP에서 `<jsp:include>` 또는 RequestDispatcher.include()로 포함된 요청 |
| ERROR          | 예외 또는 `sendError()` 발생으로 서블릿 컨테이너가 오류 페이지로 디스패치하는 요청         |
| ASYNC          | AsyncContext.start() 등 비동기 서블릿 작업 중 발생한 디스패치                 |

이 5개가 요청 처리 과정에서 “현재 요청이 어떤 상황에서 왔는가”를 설명해주는 플래그다.

---

# 1. REQUEST

가장 일반적인 경우.

브라우저가 요청하면 톰캣이 이 요청을 **REQUEST 타입**으로 처리한다.

```
브라우저 요청
→ 필터(REQUEST) 실행
→ 인터셉터(preHandle)
→ 컨트롤러
```

---

# 2. FORWARD

서블릿 내부에서 다른 경로로 요청을 **내부 전달**했을 때 발생한다.

예:

```java
request.getRequestDispatcher("/new-path").forward(request, response);
```

forward는 **HTTP 요청을 다시 보내는 것이 아니라** 컨테이너 내부에서 리다이렉트하는 것이다.

forward가 일어나면 DispatcherType이 FORWARD로 바뀐다.

필터는 FORWARD 타입 적용 여부에 따라 실행되거나 안 된다.

---

# 3. INCLUDE

JSP에서 다른 JSP를 포함할 때 사용된다.

예:

```jsp
<jsp:include page="/header.jsp" />
```

또는

```java
request.getRequestDispatcher("/header.jsp").include(request, response);
```

이때 include 내부 요청은 DispatcherType.INCLUDE로 처리된다.

---

# 4. ERROR

컨트롤러에서 예외가 발생하거나 `response.sendError()`가 실행될 때
톰캣이 등록된 오류 페이지로 요청을 내부적으로 다시 보낸다.
이 “오류 처리 요청”이 바로 ERROR 타입이다.

즉 다음과 같은 순간에 ERROR로 바뀜:

* 컨트롤러에서 예외 throw → Resolver가 처리 못 함
* sendError 호출됨
* 톰캣이 ErrorPage 매핑 확인
* 내부 디스패치 수행
  → 이 디스패치가 ERROR 타입

이 ERROR 흐름에서 필터·인터셉터가 다시 실행될 수 있다.

---

# 5. ASYNC

비동기 서블릿(Async Servlet)에서 사용된다.

```java
request.startAsync();
asyncContext.dispatch("/next");
```

이때 next 요청은 ASYNC 타입이다.

스프링 MVC에서는 비동기 요청 처리(WebFlux 제외)에서 사용될 여지가 매우 적다.

---

# DispatcherType이 중요한 이유

## 1) 필터가 어떤 상황에서 실행될지 결정한다

필터는 이렇게 등록할 수 있다:

```java
FilterRegistrationBean filter = new FilterRegistrationBean();
filter.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
```

이렇게 설정하면:

* 최초 요청(REQUEST) 때 실행
* 오류 페이지 요청(ERROR) 때 실행
* forward나 include 때는 실행 안 됨

### 기본값은 무엇인가?

서블릿 컨테이너 기본값은 다음과 같다:

**REQUEST, FORWARD, INCLUDE, ASYNC**
(ERROR는 기본값에 포함되지 않을 수도 있다. 서버마다 다르다.)

그래서 ERROR 요청에서 필터가 실행될지 여부는 설정 방식에 따라 달라진다.

---

## 2) 스프링 인터셉터는 DispatcherType을 구분하지 않는다

즉 /error로 ERROR 디스패치가 되면 인터셉터는 항상 다시 실행된다.

필터와 다른 점이다.

---

## 3) 오류 처리 흐름 이해의 핵심 요소다

DispatcherType.ERROR를 이해하면 왜 오류 페이지에서
필터나 인터셉터가 두 번 실행되는지 정확히 설명할 수 있다.

---

# 흐름 요약

### 최초 요청

```
DispatcherType.REQUEST
→ 필터(REQUEST)
→ 인터셉터
→ 컨트롤러
```

### 예외 발생 후 오류 페이지 이동

```
DispatcherType.ERROR
→ 필터(ERROR 타입 포함 시 실행)
→ 인터셉터
→ 오류 컨트롤러(/error 또는 /error-page/500)
```

### forward 흐름

```
DispatcherType.FORWARD
→ 필터(FORWARD 포함 시 실행)
→ 인터셉터
→ 컨트롤러
```

---

필요하면:

* DispatcherServlet#doDispatch 내부에서 dispatcherType이 어떻게 적용되는지
* 톰캣의 StandardWrapperValve, StandardHostValve에서 ERROR 디스패치가 어떻게 실행되는지
* 필터 체인이 dispatcherType으로 어떻게 분기되는지

까지 소스 코드 기반으로 더 깊게 설명해줄 수 있어.

아주 좋은 질문이다.
**“오류 페이지 요청(ERROR 디스패치)에서 필터/인터셉터가 실행되지 않는다”**라고 되어 있는 그림은 **특정 설정**을 기준으로 한 단순화된 예시다.
실제 WAS(서블릿 컨테이너) 환경에서는 **항상 그런 것이 아니다.**

결론부터 말하면:

# ✔ 필터는 설정에 따라 실행되기도 하고 실행되지 않기도 한다

# ✔ 인터셉터는 항상 실행된다

아래에서 정확한 기준을 설명할게.

---

# 1. 필터 실행 여부: DispatcherType에 따라 달라진다

필터는 등록할 때 **어떤 DispatcherType에 적용할지 명시할 수 있다.**

예시:

```java
FilterRegistrationBean bean = new FilterRegistrationBean();
bean.setFilter(new LogFilter());
bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
```

### 필터는 다음 경우에 실행된다

* DispatcherType.REQUEST 포함 → 최초 요청에서 실행
* DispatcherType.ERROR 포함 → 오류 페이지 요청에서 실행
* DispatcherType.FORWARD 포함 → forward 시 실행

### 즉, 필터는 “ERROR 타입을 포함하도록 설정되어 있다면” 오류 페이지 요청에서도 실행된다.

### 기본값은 WAS마다 조금 다르지만

대개
**REQUEST, FORWARD, INCLUDE, ASYNC**만 포함하고
**ERROR는 포함하지 않는 경우가 흔하다.**

그래서 오류 페이지에서는 필터가 실행되지 않은 것으로 보이기도 한다.

---

# 2. 인터셉터는 항상 실행된다 (매우 중요)

Spring MVC의 HandlerInterceptor는
**DispatcherType을 구분하지 않는다.**

DispatcherServlet이 실행되면 무조건

* HandlerMapping
* HandlerAdapter
* HandlerInterceptor

이 순서로 항상 실행된다.

즉:

* 최초 요청(REQUEST) → 인터셉터 실행
* forward(FORWARD) → 인터셉터 실행
* include(INCLUDE) → 스프링 MVC는 include를 잘 안 쓰지만 실행될 수 있음
* 오류 페이지(ERROR) → 인터셉터 다시 실행

### 결국 ERROR 디스패치에서도

**인터셉터는 무조건 실행된다.**

---

# 3. 그럼 "인터셉터 x"라는 그림은 틀린 것인가?

정확히 말하면:

* "필터 x" → 필터가 ERROR를 포함하지 않은 설정에서는 맞는 설명
* "인터셉터 x" → 정확히는 **틀린 설명**이다
  (Spring MVC에서는 인터셉터가 항상 실행된다)

아마 해당 그림은 **서블릿/JSP 시절**의 단순한 흐름을 설명할 때 쓰는 구조라서
스프링 MVC 인터셉터까지 고려하지 않은 것이다.

---

# 4. 정리: 오류 페이지 요청에서 필터/인터셉터 동작 여부

| 구성요소 | 오류 요청(ERROR)에서 실행되는가? | 기준                                |
| ---- | --------------------- | --------------------------------- |
| 필터   | 경우에 따라 YES/NO         | `DispatcherTypes` 설정에 ERROR 포함 여부 |
| 인터셉터 | 항상 YES                | DispatcherServlet이 실행되기 때문        |
| 컨트롤러 | YES                   | /error 또는 등록된 error-page 컨트롤러 호출  |

---

# 5. 당신이 본 그림과 실제를 비교하기

그림 (예시):

```
4. WAS(/error-page/500, dispatchType=ERROR)
    -> 필터(x)
    -> 서블릿
    -> 인터셉터(x)
    -> 컨트롤러(/error-page/500)
    -> View
```

### 정확한 버전은 다음과 같다

```
4. WAS(/error-page/500, dispatchType=ERROR)
    -> 필터 (ERROR 타입 포함 시 실행, 아니면 실행 안 됨)
    -> 서블릿 (DispatcherServlet)
    -> 인터셉터 (무조건 실행)
    -> 컨트롤러(/error-page/500)
    -> View
```

---

필요하다면 다음도 설명해줄 수 있어:

* 왜 인터셉터는 DispatcherType과 상관없이 항상 실행되는가
  (DispatcherServlet에서 직접 호출하기 때문)
* 필터는 왜 ERROR 디스패치에서 실행되지 않을 수 있는가
  (FilterChain 구현 원리)
* Tomcat의 StandardHostValve가 ERROR 디스패치 시 어떤 플래그를 설정하는가

계속 궁금한 것 있으면 이어서 질문해.
