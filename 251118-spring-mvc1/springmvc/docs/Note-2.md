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

`@RequestMapping`에서 `produces`를 설정한다는 것은 컨트롤러 메서드가 클라이언트에게 어떤 형태(MIME 타입)의 데이터를 반환할 것인지 명시하는 것이다.

예시:

```java
@RequestMapping(value = "/user", produces = "application/json")
public UserDto getUser() {
    return new UserDto("kim", 20);
}
```

위 메서드는 반환 결과가 JSON임을 선언한다.
따라서 스프링 MVC는 내부적으로 다음과 같은 일을 한다.

---

### 1. 클라이언트의 Accept 헤더와 매칭

HTTP 요청에는 다음과 같은 헤더가 올 수 있다.

```
Accept: application/json
Accept: text/html
```

클라이언트는 서버에게 "나는 이 형식으로 응답을 받고 싶다"라고 표현한다.

`produces = "application/json"`으로 설정된 메서드는
Accept가 JSON일 때 매칭되고 호출된다.
Accept가 맞지 않으면 406 Not Acceptable을 반환할 수 있다.

---

### 2. 어떤 메시지 컨버터를 사용할지 선택하는 기준이 된다

스프링은 반환 타입을 변환할 때 `HttpMessageConverter`를 사용한다.

`produces = "application/json"`
→ JSON 변환용 컨버터(Jackson 등)를 사용

`produces = "text/plain"`
→ 문자열 전송 컨버터 사용

즉, produces는 응답 변환 전략을 결정하는 힌트가 된다.

---

### 3. 여러 메서드 오버로딩에도 활용할 수 있다

```java
@RequestMapping(value = "/data", produces = "application/json")
public Data jsonData() { ... }

@RequestMapping(value = "/data", produces = "text/html")
public String htmlData() { ... }
```

같은 URL이라도 클라이언트가 요청한 Accept에 따라
JSON 버전 또는 HTML 버전 중 하나가 호출될 수 있다.

---

### 정리

| 설정 위치      | 의미                                  |
| ---------- | ----------------------------------- |
| `consumes` | 클라이언트가 보내는 Content-Type 조건 (입력 데이터) |
| `produces` | 서버가 응답하는 MIME 타입 조건 (출력 데이터)        |

---

### 결론

`produces`는
"이 컨트롤러는 어떤 형식으로 응답한다"
그리고
"요청의 Accept 헤더가 그 형식과 맞지 않으면 매칭되지 않는다"
는 의미다.

필요할 때 명시적으로 선언하면 API 명확성, 메시지 변환 안정성, 엔드포인트 선택 기능을 얻을 수 있다.

원하면 `consumes`, `headers`, `params`와 함께 쓰이는 조합 방식도 이어서 설명해줄 수 있다.

`@RequestMapping`은 여러 조건을 동시에 걸어서 요청을 정교하게 분기할 수 있다.
`consumes`, `produces`, `params`, `headers`, `method`, `path` 등을 조합하면 된다.

핵심은
“요청이 모든 조건을 만족할 때만 해당 메서드가 매핑된다”
라는 점이다.

---

## 1. consumes + produces 조합

입력 형식과 출력 형식을 동시에 제한할 수 있다.

```java
@RequestMapping(
    value = "/user",
    method = RequestMethod.POST,
    consumes = "application/json",
    produces = "application/json"
)
public UserDto createUser(@RequestBody UserDto dto) {
    return dto;
}
```

조건:

* 요청의 Content-Type = application/json 이어야 함
* Accept = application/json 이어야 함

만족하지 않으면:

* Content-Type이 안 맞으면 415 Unsupported Media Type
* Accept가 안 맞으면 406 Not Acceptable

---

## 2. params 조합 (쿼리 파라미터 조건)

```java
@RequestMapping(value = "/search", params = "type=admin")
public String adminSearch() { return "admin"; }

@RequestMapping(value = "/search", params = "type=user")
public String userSearch() { return "user"; }
```

같은 URL이지만 `?type=` 값에 따라 실행 메서드가 달라진다.

파라미터 존재 여부만 조건으로도 가능하다:

```java
@RequestMapping(value = "/search", params = "keyword")
public String searchWithKeyword() { ... }
```

---

## 3. headers 조합 (요청 헤더 조건)

```java
@RequestMapping(value = "/data", headers = "X-API-VERSION=2")
public String version2() { return "v2"; }
```

특정 헤더가 있어야 실행된다.

여러 개도 가능:

```java
@RequestMapping(value = "/data", headers = {"X-API-VERSION=2", "X-REGION=KR"})
```

---

## 4. method + produces + params 같이 쓰기

컨트롤러가 복잡한 API 요청을 구분할 때 활용도 높다.

```java
@RequestMapping(
    value = "/report",
    method = RequestMethod.GET,
    params = "format=excel",
    produces = "application/vnd.ms-excel"
)
public byte[] excelReport() { ... }

@RequestMapping(
    value = "/report",
    method = RequestMethod.GET,
    params = "format=pdf",
    produces = "application/pdf"
)
public byte[] pdfReport() { ... }
```

같은 URL, GET 요청이지만
파라미터와 응답 타입에 따라 메서드가 결정된다.

---

## 5. URL + 조건 + 조건 + 조건 … 조합의 의미

스프링은 내부적으로 다음 순서로 판단한다.

1. HTTP Method 일치 여부
2. URL Path 일치 여부
3. params 조건 일치 여부
4. headers 조건 일치 여부
5. consumes 조건(Content-Type) 일치 여부
6. produces 조건(Accept) 일치 여부

모두 통과해야 매핑된다.

---

## 실전에서 주로 쓰는 패턴

현업에서 많이 쓰는 조합 예시만 정리하면 다음과 같다.

| 목적                   | 자주 쓰는 조합                                    |
| -------------------- | ------------------------------------------- |
| 요청 타입에 따라 분기         | `consumes`                                  |
| 응답 타입에 따라 분기         | `produces`                                  |
| 쿼리 파라미터에 따라 분기       | `params`                                    |
| 헤더 기반 API 버저닝        | `headers`                                   |
| 같은 URL로 여러 API 버전 운영 | `params + produces` 또는 `headers + produces` |

---

## 참고 포인트

* 조건이 많아지면 코드 가독성이 떨어질 수 있으므로, 너무 복잡해지면 URL을 나누거나 `@GetMapping`, `@PostMapping` 같은 특화 어노테이션을 사용하는 것이 더 좋다.
* 조건 충돌이 있으면 스프링이 어떤 메서드를 선택해야 할지 모르게 되어 `Ambiguous mapping` 예외가 발생할 수 있다.

---

원하면

* Spring MVC가 어떤 기준으로 “가장 정확히 맞는 메서드”를 선택하는 매핑 우선순위
* consumes/produces에서 여러 MIME 타입을 나열하는 방식
* `@GetMapping`, `@PostMapping`에 같은 옵션 넣는 방식

이 중 어떤 내용을 더 다뤄주면 도움이 될까?

스프링이 요청을 매핑할 때 단순히 “맞다/아니다”만 보는 것이 아니라
체크해야 할 규칙들을 단계별로 검사한 뒤, 어느 단계에서 실패했는지에 따라 다른 종류의 오류를 반환한다.

즉, 오류 메시지는 어떤 매핑 조건이 충족되지 않았는지 스프링이 판단해서 결정하는 결과다.
개발자가 명시적으로 “415로 반환해라” 같은 코드를 쓰지 않아도 스프링이 상황에 따라 맞는 상태 코드를 넣어준다.

---

## 매핑 실패 판단의 우선순위

요청이 들어오면 스프링은 아래 순서로 매핑을 시도한다.

1. URL Path 검사
2. HTTP Method 검사
3. params 검사
4. headers 검사
5. consumes 검사 (요청 Content-Type)
6. produces 검사 (요청 Accept)

이 순서 중 어디에서 실패했는가에 따라 HTTP 오류가 달라진다.

---

## 어떤 조건에서 실패하면 어떤 오류가 나는지

아래는 실전에서 많이 겪는 케이스를 상태코드 기준으로 정리한 표다.

| 실패 원인                             | 반환되는 오류 코드                 | 예                               |
| --------------------------------- | -------------------------- | ------------------------------- |
| URL에 맞는 매핑 자체가 없음                 | 404 Not Found              | `/unknown` 요청                   |
| URL은 맞는데 HTTP 메서드가 다름             | 405 Method Not Allowed     | POST로 호출했는데 GET만 허용             |
| Content-Type이 요구 조건(consumes)과 다름 | 415 Unsupported Media Type | 컨트롤러는 JSON만 받는데 form-data로 요청   |
| Accept가 produces와 맞지 않음           | 406 Not Acceptable         | 컨트롤러는 JSON만 주는데 클라이언트가 HTML만 원함 |
| 매핑은 여러 개인데 어떤 것을 선택해야 할지 모름       | 500 Ambiguous mapping      | 조건이 중복되어 매핑 충돌                  |

즉, 스프링은
“어떤 단계에서 실패했는가”를 근거로 오류를 선택한다.

---

## 스프링이 오류를 선택하는 방식은 ‘추론’이다

예를 들어 다음 상황을 생각해보자.

* `/user` 경로를 가진 컨트롤러가 존재
* GET만 허용
* produces = application/json

그런데 클라이언트가 다음 요청을 보냄:

```
POST /user
Accept: text/html
```

이 경우 스프링의 내부 판단 흐름은 다음과 같다.

1. URL 일치 → 통과
2. Method 불일치 → 여기서 실패 → 따라서 405 반환
   (스프링은 그 다음 조건(params, headers 등)을 더 보지 않음)

즉, 가장 먼저 충돌한 조건이 오류 원인이 된다.

---

## 또 다른 예시 1

```
GET /user
Content-Type: application/xml
```

컨트롤러

```
consumes = application/json
```

판단 흐름

1. URL 일치
2. Method 일치
3. params 조건 없음
4. headers 조건 없음
5. consumes 충돌 → 415 반환

---

## 또 다른 예시 2

```
GET /user
Accept: text/html
```

컨트롤러

```
produces = application/json
```

판단 흐름

1. URL 일치
2. Method 일치
3. params 조건 없음
4. headers 조건 없음
5. consumes 조건 없음
6. produces 충돌 → 406 반환

---

## 결론

* 스프링 MVC는 매핑 조건을 일련의 규칙 순서대로 검사한다.
* 어느 조건에서 실패했는지에 따라 상태코드가 자동으로 결정된다.
* 개발자가 `return 406;` 같은 코드를 쓰는 것이 아니라, 스프링의 매핑 로직이 실패 지점을 추론하여 가장 적절한 오류를 응답한다.

필요하다면
각 오류 상황을 직접 디버그하거나 로그로 볼 수 있는 방법 (`HandlerMapping`, `HandlerMethod`, `Trace`, `debug log`)을 추가로 알려줄 수도 있다.
그 부분도 궁금한가?

두 어노테이션은 결국 응답 데이터를 HTTP 바디에 그대로 담아서 전송한다는 공통 목적을 가진다.
하지만 적용 범위와 동작 방식이 다르다.

---

## @ResponseBody

* 메서드 단위에 적용
* 반환값을 뷰 이름으로 해석하지 않고 그대로 HTTP 응답 본문으로 전송
* 문자열이면 문자열 그대로, 객체면 JSON 변환 후 바디에 담긴다.

```java
@ResponseBody
@GetMapping("/hello")
public String hello() {
    return "hello";
}
```

이 경우 `"hello"`라는 문자열이 그대로 본문에 실려 전송된다.

만약 @ResponseBody가 없었다면 같은 코드는 `"hello"`라는 뷰 이름을 가진 템플릿을 찾으려는 시도를 하게 된다.

---

## @RestController

* 클래스 단위에 적용
* 내부의 모든 핸들러 메서드에 자동으로 `@ResponseBody` 기능 부여
* 즉, 클래스에 선언하면 해당 컨트롤러의 모든 메서드가 기본적으로 JSON/문자열 등 명시적 응답 바디를 반환하게 된다.

```java
@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
```

여기서는 `@ResponseBody`를 명시하지 않아도 같은 동작을 한다.

---

## 차이 요약

| 항목       | @RestController                 | @ResponseBody             |
| -------- | ------------------------------- | ------------------------- |
| 적용 위치    | 클래스                             | 메서드                       |
| 효과       | 클래스 내부 모든 메서드에 @ResponseBody 적용 | 해당 메서드에만 적용               |
| 용도       | API 개발 전체 컨트롤러                  | 특정 메서드만 JSON/문자열 반환하게 할 때 |
| 기본 반환 처리 | 응답 바디                           | 응답 바디                     |

---

## @RestController = @Controller + @ResponseBody 합성

스프링 소스 관점에서 보면
`@RestController`는 다음 두 가지 어노테이션의 기능을 합친 것이다.

```
@Controller
@ResponseBody
```

따라서 `@RestController`는 템플릿 렌더링 방식의 웹 페이지를 만드는 것이 아니라
API 서버(REST API, JSON API)를 만들 때 사용한다.

---

## 어떤 상황에서 무엇을 쓰는가

* JSON API 서버 / REST API 개발 → `@RestController`
* 웹페이지(타임리프 등 템플릿) + 특정 일부 API만 JSON 응답 → `@Controller + @ResponseBody`

예를 들어 웹 페이지를 렌더링하는 컨트롤러인데
특정 메서드만 AJAX 응답을 반환할 때:

```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @ResponseBody
    @GetMapping("/api/user")
    public User getUser() {
        return new User("kim", 20);
    }
}
```

---

## 정리

* @ResponseBody는 메서드의 반환값을 그대로 응답 바디로 보낸다.
* @RestController는 컨트롤러 전체 메서드에 @ResponseBody 효과를 자동 적용한다.

추가로 궁금하면 이어서 설명할 수 있는 내용:

* @RestController에서 문자열 반환 시 text/plain인지 JSON인지 결정되는 기준
* 객체를 반환할 때 어떤 MessageConverter가 동작하는지
* @Controller와 ModelAndView 흐름 비교

어떤 부분 더 알고 싶어?

1. 실제 서비스 개발에서 System.out.println 대신 로깅 라이브러리를 사용하는 주된 이유가 무엇일까요?
- A 코드가 더 짧아져요.
- B 성능이 훨씬 빨라요.
- (답)로그 레벨 설정으로 출력 레벨을 조절할 수 있어요.
- D 다른 개발자가 보기에 더 멋있어요.
  해설
  로깅 라이브러리는 레벨 설정으로 필요한 로그만 출력하고, 성능 최적화가 되어있어요. 실제 운영 환경에서 시스템 자원을 효율적으로 사용하며 문제를 추적할 수 있게 도와주죠.
2. 애플리케이션의 로깅 레벨을 INFO로 설정했다면, 어떤 레벨의 로그 메시지가 출력될까요?
- A TRACE, DEBUG, INFO, WARN, ERROR 모두
- B DEBUG, INFO, WARN, ERROR
- (답)INFO, WARN, ERROR
- D WARN, ERROR
  해설
  로깅 레벨은 설정된 레벨 이상(더 심각한)의 로그만 출력하도록 동작해요. 따라서 INFO 레벨로 설정하면 INFO, WARN, ERROR 로그가 보이게 된답니다.
3. Spring MVC에서 @RequestMapping 어노테이션은 기본적으로 URL 경로 외에 어떤 정보를 함께 조합해서 요청을 특정 컨트롤러 메서드에 매핑할 수 있을까요?
- A 요청 시간
- B 서버 자원 사용량
- (답)HTTP 메서드 (GET, POST 등)
- D 클라이언트 IP 주소
  해설
  @RequestMapping은 URL 뿐만 아니라 HTTP 메서드(GET, POST 등), 파라미터 조건, 헤더 조건, 미디어 타입 등을 조합하여 요청을 매핑할 수 있어요. 이를 통해 더 유연하고 구체적인 매핑이 가능해지죠.
4. /users/{userId}와 같은 형식으로 URL 경로 자체에 값을 포함시켜 요청을 처리하는 방식을 무엇이라고 부르나요?
- A 쿼리 파라미터
- B 폼 데이터
- C 메시지 바디
- (답)경로 변수 (Path Variable)
  해설
  URL 경로 일부에 변수를 넣는 방식을 경로 변수(Path Variable)라고 해요. REST API 설계에서 특정 리소스를 식별할 때 자주 사용되는 형태랍니다.
5. HTTP 요청 시 데이터를 전달하는 방식 중, request.getParameter()로 편리하게 조회할 수 있는 방식은 무엇인가요?
- (답)URL 쿼리 파라미터와 HTML Form 데이터
- B HTTP 메시지 바디에 담긴 데이터
- C HTTP 헤더에 담긴 데이터
- D 쿠키에 담긴 데이터
  해설
  URL에 붙는 쿼리 파라미터와 HTML <form> 태그로 전송되는 데이터는 request.getParameter()로 동일하게 조회할 수 있어요. 둘 다 요청 파라미터 형식이기 때문이죠.
6. Spring MVC의 @ModelAttribute는 주로 어떤 목적으로 사용될까요?
- A HTTP 메시지 바디의 데이터를 자바 객체로 변환할 때
- B 컨트롤러 메서드의 반환 값을 HTTP 응답 본문에 넣을 때
- (답)HTTP 요청 파라미터들을 모아서 자바 객체에 바인딩할 때
- D HTTP 헤더 정보를 조회할 때
  해설
  @ModelAttribute는 요청 파라미터(쿼리 스트링, 폼 데이터)의 이름과 객체의 필드 이름이 같을 때, 해당 객체의 세터(setter)를 호출하여 자동으로 값을 바인딩해주는 역할을 해요.
7. Spring MVC에서 @RequestBody나 @ResponseBody 어노테이션이 제대로 동작하기 위해 핵심적으로 필요한 내부 컴포넌트는 무엇일까요?
- A ViewResolver
- B Template Engine
- (답)HTTP Message Converter
- D DispatcherServlet
  해설
  HTTP 메시지 컨버터는 HTTP 요청 본문의 데이터를 원하는 자바 객체로 변환하거나, 자바 객체를 HTTP 응답 본문으로 변환하는 역할을 해요. @RequestBody와 @ResponseBody는 이 컨버터를 사용한답니다.
8. 클라이언트에서 HTTP 요청 본문에 JSON 데이터를 담아 보낼 때, Spring이 이를 적절한 자바 객체로 자동 변환하기 위해 주로 확인하는 HTTP 헤더는 무엇일까요?
- A Accept
- B Authorization
- (답)Content-Type
- D User-Agent
  해설
  클라이언트가 보내는 Content-Type 헤더는 요청 본문에 담긴 데이터의 타입을 알려줘요. Spring은 이 정보를 보고 어떤 HTTP 메시지 컨버터(예: JSON용 Jackson)를 사용할지 결정한답니다.
9. HTML 템플릿을 사용해 동적인 웹 페이지를 응답하는 @Controller와 달리, API 서버처럼 데이터를 HTTP 응답 본문에 직접 담아 보내기 편리하도록 @ResponseBody 기능이 추가된 어노테이션은 무엇일까요?
- A @Service
- B @Repository
- C @Component
- (답)@RestController
  해설
  @RestController는 @Controller와 @ResponseBody의 기능을 합친 어노테이션이에요. 뷰 템플릿 대신 데이터(주로 JSON)를 응답 본문에 직접 담아 보내는 API 컨트롤러에 적합하죠.
10. 컨트롤러에서 HTTP 응답 본문 데이터와 함께 HTTP 상태 코드를 동적으로 변경하거나, 헤더 정보까지 세밀하게 제어하고 싶을 때 주로 사용하는 Spring의 반환 타입은 무엇일까요?
- A String
- B void
- C ModelAndView
- (답)ResponseEntity
  해설
  ResponseEntity는 HTTP 응답 전체를 객체로 표현해요. 응답 본문 데이터 외에 HTTP 상태 코드, 헤더 정보 등을 프로그래밍적으로 설정하여 동적으로 제어할 수 있는 강력한 반환 타입이랍니다.