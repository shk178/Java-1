아니. 코드에서 `return 405;`처럼 직접 반환하는 게 아니다.

### 어떻게 405가 반환되는가

`@RequestMapping`은 스프링 MVC의 DispatcherServlet 내부에서
요청 URL과 HTTP 메서드(GET/POST/PUT…)를 매핑한다.

```java
@RequestMapping(value = "/basic/hello/", method = RequestMethod.GET)
```

이 설정은 URL은 맞지만 HTTP 메서드가 GET이 아닐 때 스프링이 자동으로 405(Method Not Allowed)를 보내도록 동작한다.

### 즉, 흐름은 이렇게 됨

1. 사용자가 `/basic/hello/`로 요청을 보냄
2. 스프링이 “이 URL은 있다”는 걸 먼저 확인함
3. 해당 요청을 처리하는 메서드의 허용 메서드(GET) 를 확인함
4. 요청한 HTTP 메서드가 GET이 아니면
   → 스프링 내부에서 HttpRequestMethodNotSupportedException을 발생시킴
5. 그 예외를 스프링이 처리하면서
   → HTTP Status 405를 응답 코드로 전송함
   (메시지 바디에 "405 Method Not Allowed"와 허용된 메서드 목록을 담아 보냄)

### 그래서 중요한 점

* 개발자가 return으로 405를 돌려주는 게 아니라
* 스프링이 자동으로 405 상태 코드를 클라이언트에 보냄
* 컨트롤러 메서드는 아예 호출되지 않는다.
  (메서드까지 오기 전에 가로채서 405를 응답함)

### 코드의 의미

```java
@RequestMapping(value = "/basic/hello/", method = RequestMethod.GET)
```

요청 메서드가 GET일 때만 이 메서드를 실행한다는 뜻이며,
다른 메서드(POST/PUT 등)라면 스프링이 405를 리턴한다는 의미다.

궁금한 내용 더 이어서 설명해줄까?

결론부터 말하면 저 코드는 동작하지 않는다. 스프링 MVC에서 저 형태는 올바른 문법이 아니다.

## 왜 안 되는가

### 1. `@RequestMapping`의 경로 변수 문법이 잘못됨

스프링 MVC에서는 하나의 path variable은 `{variableName}` 형태여야 한다.

즉:

* `{var1, var2}`처럼 콤마로 여러 개를 묶는 문법은 없다.
* 여러 변수를 받으려면 경로 자체에서 분리된 형태로 적어야 한다.

예)

```
/basic/hello/{var1}/{var2}
```

### 2. `@PathVariable({"var1", "var2"})` 도 불가능

`@PathVariable`은

* 단일 변수 이름: `@PathVariable("var1") String v1`
* 이름 생략: `@PathVariable String var1` (변수명 기준)

만 지원한다.

`@PathVariable({"var1", "var2"})`처럼 배열을 넣을 수 있는 기능은 없다.

---

## 올바르게 작성하려면?

### 방법 1) 각각 받기

```java
@RequestMapping("/basic/hello/{var1}/{var2}")
public String varHello(@PathVariable String var1,
                       @PathVariable String var2) {
    return var1 + var2;
}
```

### 방법 2) 배열로 받고 싶다면 (var1, var2 순서 보장 불가)

스프링은 배열 바인딩을 기본 지원하지 않지만, 다음처럼 Map으로 받고 추출할 수는 있다:

```java
@RequestMapping("/basic/hello/{var1}/{var2}")
public String varHello(@PathVariable Map<String, String> pathVars) {
    return pathVars.get("var1") + pathVars.get("var2");
}
```

---

## 정리

* `{var1, var2}` 같이 path variable을 묶는 문법은 없다.
* `@PathVariable`에 배열을 넣는 문법도 없다.
* 경로에 변수를 여러 개 두려면 `{var1}/{var2}`처럼 각각 작성해야 한다.

원래 의도(여러 변수를 한 번에 받기)와 비슷하게 처리하고 싶다면 어떻게 구조를 잡고 싶은지 말해줘, 더 자연스러운 방식으로 구성해줄게.

아래는 `@RequestMapping`에서 `params`, `headers`, `consumes`를 각각 언제 사용하는지, 무엇을 필터링하는지, 실제 예시 상황까지 구분해서 설명한 것이야.

---

# 1. `params`

### 언제 쓰는가

URL 쿼리 파라미터가 특정 조건을 만족해야만 해당 핸들러가 실행되도록 하고 싶을 때 사용해.
즉, `?mode=debug` 같은 요청 파라미터 기반으로 분기할 때.

### 주로 사용하는 상황

* 같은 URL을 쓰지만 특정 파라미터가 있을 때만 다른 로직 실행
* 특정 파라미터 값이 일치해야 할 때
* 특정 파라미터가 반드시 없어야 할 때

### 예시

```java
@RequestMapping(value = "/search", params = "type=book")
public String searchBook() { ... }

@RequestMapping(value = "/search", params = "type=movie")
public String searchMovie() { ... }

@RequestMapping(value = "/search", params = "!type")
public String searchDefault() { ... }
```

---

# 2. `headers`

### 언제 쓰는가

HTTP 헤더 조건에 따라 다른 핸들러가 실행되도록 할 때 사용해.

즉, 요청의 `Header` 값에 따라 분기하고 싶을 때.

### 주로 사용하는 상황

* 특정 클라이언트가 보내는 커스텀 헤더를 기반으로 분기
* 특정 API 버전 사용 여부를 헤더로 구분
* 헤더가 있어야만 접근 가능한 엔드포인트
* 프록시나 게이트웨이에서 붙이는 헤더로 구분

### 예시

```java
@RequestMapping(value = "/data", headers = "X-API-VERSION=1")
public String v1() { ... }

@RequestMapping(value = "/data", headers = "X-API-VERSION=2")
public String v2() { ... }

@RequestMapping(value = "/data", headers = "!X-API-VERSION")
public String defaultVersion() { ... }
```

---

# 3. `consumes`

### 언제 쓰는가

클라이언트가 보내는 요청 바디의 Content-Type에 따라 처리할 핸들러를 선택할 때
즉, 요청의 Content-Type 필터링.

### 주로 사용하는 상황

* JSON 요청만 받을 때
* form-data 요청만 받을 때
* XML 요청만 받을 때
* 같은 URL을 쓰더라도 입력 형식(body 타입)에 따라 다른 메서드를 실행하고 싶을 때

### 예시

```java
@RequestMapping(value = "/user", consumes = "application/json")
public String createUserJson(@RequestBody User user) { ... }

@RequestMapping(value = "/user", consumes = "application/x-www-form-urlencoded")
public String createUserForm(User user) { ... }
```

---

# 언제 어떤 것을 선택해야 하나?

| 기능           | 목적                       | 조건 검사 대상        | 대표 사용 상황                |
| ------------ | ------------------------ | --------------- | ----------------------- |
| params   | 쿼리 파라미터 기준 라우팅           | URL 파라미터        | `?type=book` 같은 라우팅     |
| headers  | 헤더 기반 라우팅                | Header          | API 버전, 특정 클라이언트 분리     |
| consumes | 요청 body의 Content-Type 구분 | Content-Type 헤더 | JSON, XML, form-data 구분 |

---

# 어떤 것을 더 자주 쓰는가?

* `consumes` → REST API에서 가장 많이 사용
* `params` → 검색이나 모드 분리에 가끔 사용
* `headers` → API 버전을 헤더로 관리하거나 클라이언트별 라우팅할 때 사용

---

필요하면 `produces`와의 차이도 정리해서 이어서 설명해줄게.

`consumes`는 메서드 인자와는 아무 상관없어.
단지 요청의 Content-Type이 무엇인지를 기준으로 *이 핸들러가 이 요청을 받을 수 있는지 조건을 거는 기능*이야.

즉:

* `@RequestBody`를 받든
* `MultipartFile`을 받든
* 그냥 아무 인자도 받지 않든

consumes는 단지 “이 요청의 Content-Type이 특정 값일 때만 이 메서드를 실행해라”라는 의미만 갖는다.

---

# 예) 인자가 없는 경우에도 가능

```java
@RequestMapping(value="/test", consumes="application/json")
public String test() {
    return "ok";
}
```

이 메서드는 JSON 요청만 받을 수 있어.
인자는 없어도 상관없어.

---

# 예) 인자가 있어도 상관없음

```java
@RequestMapping(value="/user", consumes="application/json")
public String create(@RequestBody User user) {
    return "ok";
}
```

여기서 `@RequestBody User user`가 들어간 건
JSON을 바인딩하기 위함이지 consumes와는 별개야.

---

# 핵심 정리

* consumes는 인자와는 전혀 무관
* 클라이언트가 보내는 Content-Type 헤더가 조건과 맞아야만 해당 메서드가 선택됨
* 인자의 유무나 타입은 다른 문제

원하면 `consumes`, `produces`, `headers`가 어떻게 충돌할 때 스프링이 어떤 순서로 매핑하는지도 설명해줄게.

꼭 그렇다고만 볼 수는 없어.
Content-Type은 “이 요청의 본문(body)을 어떤 형식으로 해석해야 하는가”를 나타내는 헤더이지만, 반드시 body가 있어야만 쓰이는 것은 아니야.

아래처럼 정리할 수 있어.

---

# 1. 원칙: Content-Type은 “본문의 형식”을 나타내는 헤더

일반적으로는 요청 body가 있을 때 사용돼.

예를 들어:

* `application/json`
* `application/x-www-form-urlencoded`
* `multipart/form-data`
* `text/plain`

이런 것들은 모두 body 데이터를 보내기 위해 쓰는 타입들.

그래서 관례적으로
“Content-Type이 있다 → body가 있다”
이렇게 이해하는 경우가 많아.

---

# 2. 하지만 body가 없어도 Content-Type을 붙일 수 있음

HTTP 스펙상 Content-Type은 선택(headers)이고,
body가 빈 요청에서도 붙일 수는 있어.

예시:

```http
GET /hello
Content-Type: application/json
```

이런 요청은 body가 없어도 technically valid야.

단, 의미가 없을 뿐이지.

---

# 3. 실질적으로 Spring MVC에서는 이렇게 이해하는 것이 정확함

Content-Type은 body parser를 결정하는 정보고,
body가 있는 요청에서만 의미를 가진다.

그래서 실무에서는 이렇게 기억하는 게 정확해:

* body가 있는 요청이라면 Content-Type이 중요하다.
* body가 없는 요청에서는 Content-Type은 무시되거나 의미가 없다.

---

# 4. 그래서 `consumes`도 사실상 “body가 있는 요청에만 영향”

`consumes = "application/json"`
→ Content-Type이 application/json인 요청만 처리하겠다는 의미.

여기서 핵심은:

* Content-Type은 body를 전제로 설계된 헤더
* body 없는 요청에 Content-Type을 붙여도 consumes 매칭에는 일반적으로 의미 없음

---

원하면 `GET`, `POST`, `PUT`, `DELETE` 같은 HTTP 메서드별로
body가 있을 수 있는지 없을 수 있는지 정리해줄게.

`params = "mode=debug"` 같은 조건은 “같은 URL을 쓰면서, 특정 쿼리 파라미터가 있을 때만 다른 로직을 실행하고 싶을 때” 쓰는 방식이야.
스펙 분리를 위한 라우팅 용도지, 파라미터 값을 직접 인자로 받기 위해 쓰는 게 아니야.

아래에서 실제로 언제 등장하는지 상황을 설명해볼게.

---

# 1. 모드 분리(기능 스위치)

같은 엔드포인트에서 환경/모드에 따라 서로 다른 동작을 하게 하고 싶을 때.

예)

```
/report?mode=debug  
/report?mode=release
```

```java
@RequestMapping(value="/report", params="mode=debug")
public String debugReport() { ... }

@RequestMapping(value="/report", params="mode=release")
public String releaseReport() { ... }
```

이런 식으로 디버그 버전과 정식 버전을 URL로 분리하고 싶을 때.

---

# 2. 검색 조건에 따라 다른 핸들러 매칭

예를 들어 `/search`라는 API가 있는데,
`?type=user`와 `?type=order`를 완전히 다른 로직으로 처리하고 싶을 경우.

```java
@RequestMapping(value="/search", params="type=user")
public String searchUser() { ... }

@RequestMapping(value="/search", params="type=order")
public String searchOrder() { ... }
```

즉, 파라미터 기반 라우팅이 필요할 때.

---

# 3. 특정 파라미터가 “있을 때만” 접근하도록 제한

예: `/download?token=xxxx`
토큰이 없으면 아예 다른 메서드로 보내거나 400 처리.

```java
@RequestMapping(value="/download", params="token")
public String downloadWithToken() { ... }

@RequestMapping(value="/download", params="!token")
public String missingToken() {
    throw new IllegalArgumentException("token required");
}
```

---

# 4. 같은 URL을 쓰되 프런트·백오피스 구분

예) `/dashboard`

* 프런트: `/dashboard?client=web`
* 백오피스: `/dashboard?client=admin`

```java
@RequestMapping(value="/dashboard", params="client=admin")
public String adminDash() { ... }

@RequestMapping(value="/dashboard", params="client=web")
public String webDash() { ... }
```

---

# 5. 레거시 API 호환

기존 시스템에서 `?mode=legacy` 같은 파라미터를 던지는 경우
새 시스템과 구 시스템 로직을 동시에 유지해야 할 때 사용되기도 해.

---

# 핵심 요약

`params = "mode=debug"`는 쿼리 파라미터를 기준으로 “같은 URL의 여러 버전”을 나누는 용도야.

* 같은 URL인데 조건에 따라 분기해야 할 때
* 파라미터가 특정 값일 때만 선택되는 라우팅이 필요할 때
* 별도의 path를 만들기 싫거나 만들면 복잡해질 때

이럴 때 바로 이 방식을 사용해.

원하면 `params`로 체크할 수 있는 모든 형태(있음/없음/특정값/여러 조건)를 표로 정리해줄게.
