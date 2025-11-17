결론부터 말하면,

HTML FORM에서는 JSON을 직접 전송하지 않는다.
즉, “요즘 Content-Type은 JSON을 많이 쓴다”는 말은 HTML FORM과는 거의 무관하다.
이건 주로 AJAX, fetch, axios 같은 JS 기반 요청에서 사용하는 방식이다.

아래에서 HTML FORM과 JSON의 관계를 정확히 정리해줄게.

---

# 1. HTML FORM은 JSON을 보낼 수 없다

FORM은 `enctype`을 통해 전송 형식을 지정하는데, 스펙으로 지원하는 것은 다음뿐이다.

* application/x-www-form-urlencoded (기본값)
* multipart/form-data (파일 업로드)
* text/plain

여기에는 application/json이 포함돼 있지 않다.

즉,

```
<form method="POST" enctype="application/json">
```

이렇게 쓰더라도 브라우저는 JSON 형식으로 바디를 전송해주지 않는다.

---

# 2. JSON은 JS 기반 요청에서 많이 쓰는 형식

요즘 “Content-Type: application/json”을 많이 쓴다는 말은 다음 같은 요청에서 JSON을 쓴다는 뜻이다.

예:

```javascript
fetch("/members", {
    method: "POST",
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify({
        name: "kim",
        age: 25
    })
})
```

JSON은 REST API와 SPA 환경에서 매우 흔히 사용된다.
HTML FORM과는 별도 기술이다.

---

# 3. 왜 FORM은 JSON을 지원하지 않을까?

HTML FORM은 웹 초창기(1990년대) 설계된 방식이다.
그 당시 웹의 기본 동작은 다음과 같았다.

* GET으로 문서 조회
* POST로 단순한 키-값 제출
* 서버 렌더링 페이지 이동

AJAX나 SPA, JSON을 통한 API 통신은 그보다 훨씬 뒤에 등장한 개념이라 FORM 스펙에 반영되지 않았다.

그래서 FORM은 단순한 key-value 전송만 지원한다.

---

# 4. HTML FORM에서 JSON을 꼭 보내고 싶다면?

브라우저가 자동으로 JSON 바디를 만들어주지는 않지만,
JS를 사용해 JSON을 보내는 것은 가능하다.

예:

```javascript
const form = document.querySelector("form");

form.addEventListener("submit", (e) => {
    e.preventDefault();

    const data = {
        username: form.username.value,
        password: form.password.value
    };

    fetch("/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
});
```

이 경우에는 FORM 자체가 보내는 것이 아니라,
JS가 내용을 읽어서 JSON으로 바꿔서 전송하는 것이다.

---

# 5. 정리

### 1) HTML FORM

* JSON을 직접 전송할 수 있음? → 불가능
* 전송 방식 → form-urlencoded, multipart/form-data
* 전통적인 서버 렌더링 환경에서 사용

### 2) JSON 요청

* FORM과 무관
* 주로 fetch, axios, AJAX 요청에서 사용
* REST API와 SPA에서 기본 방식

### 3) 요즘 JSON을 많이 쓴다는 말의 의미

* 서버와 클라이언트가 JSON 기반 REST API로 통신하는 구조가 증가
* FORM을 통한 전송보다 JS 기반 요청이 더 일반화됨

---

필요하면 이어서:

* 왜 JSON이 form-urlencoded보다 REST에 적합한지
* JSON vs form-urlencoded의 구조적 차이
* multipart/form-data와 JSON을 동시에 쓰는 방법
  도 설명해줄게.

## HTTP API 설계 예시
- HTTP API - 컬렉션: POST 기반 등록 예) 회원 관리 API 제공
```
URI는 리소스를 식별해야 함
리소스에 대한 행동 = HTTP 메서드
- 회원 목록 /members -> GET
- 회원 등록 /members -> POST
- 회원 조회 /members/{id} -> GET
- 회원 수정 /members/{id} -> PATCH, PUT, POST
- 회원 삭제 /members/{id} -> DELETE
```
```
POST - 신규 자원 등록 특징
- 클라이언트는 등록될 리소스의 URI를 모른다.
    - 회원 등록 /members -> POST
    - POST /members
- 서버는 새로 등록된 리소스 URI를 생성해준다.
    - HTTP/1.1 201 Created
      Location: /members/100
- 컬렉션
    - 서버가 관리하는 리소스 디렉토리
    - 서버가 리소스의 URI를 생성하고 관리
    - 여기서 컬렉션 = /members
```
- HTTP API - 스토어: PUT 기반 등록 예) 정적 컨텐츠 관리, 원격 파일 관리
```
파일 관리 시스템 (files는 파일 폴더)
- 파일 목록 /files -> GET
- 파일 조회 /files/{filename} -> GET
- 파일 등록 /files/{filename} -> PUT
    - 기존 파일이 있으면 덮어써서 PUT
- 파일 삭제 /files/{filename} -> DELETE
- 파일 대량 등록 /files -> POST
```
```
PUT - 신규 자원 등록 특징
- 클라이언트가 리소스 URI 알고 있어야 한다.
    - 파일 등록 /files/{filename} -> PUT
    - PUT /files/star.jpg
- 클라이언트가 직접 리소스의 URI를 지정한다.
- 스토어
    - 클라이언트가 관리하는 리소스 저장소
    - 클라이언트가 리소스의 URI를 알고 관리
    - 여기서 스토어는 /files
```
- HTML FORM 사용: GET, POST만 지원 예) 웹 페이지 회원 관리
```
- 회원 목록 /members -> GET
- 회원 등록 (폼) /members/new -> GET
- 회원 등록 /members/new, /members -> POST
- 회원 조회 /members/{id} -> GET
- 회원 수정 (폼) /members/{id}/edit -> GET
- 회원 수정 /members/{id}/edit, /members/{id} -> POST
- 회원 삭제 /members/{id}/delete -> POST
```

여기서 폼을 보여주는 페이지(GET)와 폼을 제출하는 처리 요청(POST)이 URL 구조상 짝을 이루고 있다는 의미다.
이건 전통적인 서버 렌더링 MVC 패턴에서 매우 일반적인 방식이다.

왜 이렇게 구성되는지 차근차근 설명해줄게.

---

# 1. “폼을 보여주는 GET”과 “폼을 제출하는 POST”는 역할이 다르다

예를 들어 회원 등록을 보면:

### 1) 등록 폼 요청 (입력 화면)

```
GET /members/new
```

* 빈 HTML form 화면을 보여주는 요청
* 서버는 뷰 템플릿(Thymeleaf 등)을 렌더링해서 클라이언트에게 제공

### 2) 등록 요청 (실제 데이터를 처리)

```
POST /members
```

* 사용자가 작성한 form 데이터를 서버에서 처리
* DB에 저장하고 리다이렉트

즉,

* GET /members/new → 화면 보여주기
* POST /members → 실제 등록 처리

이 둘은 같은 일을 하는 것이 아니라,
“같은 기능(등록)”의 서로 다른 단계를 분리한 것.

---

# 2. 왜 GET + POST로 나누는가?

## 1) GET은 화면 표시용

GET은

* 서버 상태 변화 없음
* 브라우저 URL로 접근 가능
* 새로고침해도 안전
* 캐싱 가능

따라서 입력 화면을 띄우는 데 적합하다.

## 2) POST는 데이터 제출용

POST는

* 서버에 데이터를 변경
* 바디로 form 데이터를 전송
* 캐싱 불가

따라서 등록/수정 같은 작업 처리를 담당한다.

---

# 3. 왜 “폼의 URL”과 “처리용 POST URL”이 비슷한가?

정리한 예시를 보면 아래와 같은 규칙이 있다:

| 기능 | 폼 화면(GET)            | 처리(POST)                                     |
| -- | -------------------- | -------------------------------------------- |
| 등록 | /members/new         | /members                                     |
| 수정 | /members/{id}/edit   | /members/{id}                                |
| 삭제 | /members/{id}/delete | (POST) /members/{id}/delete 혹은 /members/{id} |

### 이는 다음 이유 때문이다.

1. 사용자에게 보여주는 페이지(GET)와
2. 서버에서 처리하는 요청(POST)

이 같은 기능을 공유하고 있기 때문에 URL을 연결성 있게 구조화한 것이다.

즉, REST 스타일을 흉내 내는 전통 MVC 방식이다.

---

# 4. REST 관점에서 보면 사실 POST URL은 더 명확한 의미가 있다

### 등록:

```
POST /members
```

→ 회원 리소스를 생성하는 의미

### 수정:

```
POST /members/{id}
```

→ 실제 REST에서는 PUT/PATCH를 쓰지만
HTML FORM 제약 때문에 POST를 우회적으로 사용하는 것

### 삭제:

```
POST /members/{id}/delete
```

→ REST라면 DELETE /members/{id} 이지만
FORM은 DELETE를 지원하지 않음 → POST로 대체

즉,

* 폼 화면(GET): REST가 아니라 그냥 “화면”을 띄우는 URL
* 처리 요청(POST): REST에 최대한 가깝게 설계된 API 역할

두 개가 짝을 이루는 구조로 본 것이다.

---

# 5. “같은 이름을 쓴다”는 게 무슨 의미인가?

예를 들어:

```
GET /members/{id}/edit
POST /members/{id}
```

여기서 “같은 이름”처럼 보이는 이유는:

* GET은 수정 화면을 제공
* POST는 수정 처리를 수행

서로 같은 기능을 위한 두 단계이므로 URL 패턴을 맞춰 놓은 것이다.

---

# 6. 폼과 POST는 어떤 관계인가?

FORM은 GET/POST만 지원한다.

* 수정은 원래 PUT/PATCH
* 삭제는 원래 DELETE

하지만 HTML FORM으로는 못 한다.
그래서 POST로 대체하는 관행이 생긴 것이다.

따라서 구조는 자연스럽게 이렇게 된다:

* 폼(GET): 화면 출력
* POST: 처리
* URL은 기능별로 묶어서 비슷하게 구성

---

# 7. 전체 구조를 다시 깔끔하게 이해하기

### 등록

```
GET  /members/new   → 등록 화면
POST /members       → 등록 처리
```

### 수정

```
GET  /members/{id}/edit  → 수정 화면
POST /members/{id}       → 수정 처리 (PUT을 대신함)
```

### 삭제

```
POST /members/{id}/delete → 삭제 처리 (DELETE 대신)
```

여기서 폼 화면 URL은 REST와 관계 없는 UI URL,
POST URL은 REST 행동을 모방한 처리 URL이다.

---

# 결론

* 폼을 보여주는 GET과
* 폼을 제출하는 POST는

같은 기능의 “화면 단계”와 “처리 단계”를 나누어 구성한 것이다.

폼은 GET/POST만 지원하므로
수정/삭제도 POST로 처리하게 되고
기능별로 URL을 일관성 있게 맞춘 것이다.

원하면

* RESTful MVC URL 구조 best practice
* PUT/PATCH/DELETE가 FORM과 충돌하는 이유
* HiddenHttpMethodFilter의 정확한 동작 원리
  도 이어서 설명해줄게.

아래는 회원 등록 / 회원 수정 / 회원 삭제를 HTML FORM으로 구현할 때의 실제 코드 예시다.
지금까지 설명했던 URL 규칙과 동작 방식을 그대로 반영한 형태다.

---

# 1. 회원 등록

## 1) 등록 폼 화면: GET /members/new

서버가 이 HTML을 렌더링해서 브라우저에 보여준다.

```html
<form action="/members" method="post">
    <label>이름</label>
    <input type="text" name="name">

    <label>나이</label>
    <input type="number" name="age">

    <button type="submit">등록</button>
</form>
```

### 설명

* 폼 화면을 보여주는 URL은 `/members/new` (GET)
* 실제 제출은 `/members` (POST)
* form의 `action="/members"`가 핵심

---

# 2. 회원 수정

## 1) 수정 폼 화면: GET /members/{id}/edit

서버가 수정할 데이터(id=1 등)를 불러와 아래 HTML을 렌더링해서 제공한다.

```html
<form action="/members/1" method="post">
    <label>이름</label>
    <input type="text" name="name" value="홍길동">

    <label>나이</label>
    <input type="number" name="age" value="20">

    <button type="submit">수정</button>
</form>
```

여기서 가장 중요한 점:

* FORM은 method="put" 을 지원하지 않음
* 그래서 수정 처리도 POST로 우회
* action="/members/1" → 수정 처리 URL에 대응

Spring MVC에서는 이를 PUT/PATCH로 처리하고 싶으면 HiddenHttpMethodFilter를 사용한다.

### HiddenHttpMethodFilter 버전 (PUT 사용)

```html
<form action="/members/1" method="post">
    <input type="hidden" name="_method" value="put">

    <label>이름</label>
    <input type="text" name="name" value="홍길동">

    <label>나이</label>
    <input type="number" name="age" value="20">

    <button type="submit">수정</button>
</form>
```

POST 요청이지만 `_method=put` 때문에 서버는 PUT으로 처리하게 된다.

---

# 3. 회원 삭제

HTML FORM은 DELETE 요청도 직접 지원하지 않는다.

따라서 다음처럼 작성한다.

## 방법 1) POST로 처리하는 경우 (단순)

```
POST /members/{id}/delete
```

HTML:

```html
<form action="/members/1/delete" method="post">
    <button type="submit">삭제</button>
</form>
```

## 방법 2) DELETE 메서드를 쓰고 싶은 경우

HiddenHttpMethodFilter 사용.

```html
<form action="/members/1" method="post">
    <input type="hidden" name="_method" value="delete">
    <button type="submit">삭제</button>
</form>
```

서버에서는 DELETE /members/{id} 로 동작하게 된다.

---

# 4. 전체적으로 기억할 핵심 규칙

1. 폼 화면 요청 = GET
   처리(action) = POST
   → FORM이 지원하는 메서드는 GET/POST 뿐이기 때문

2. REST의 PUT/DELETE를 FORM에서 구현하려면
   `_method` 숨김 필드 + POST 조합을 써야 한다.

3. GET 화면 URL과 POST 처리 URL은
   같은 기능을 공유하도록 구조를 비슷하게 잡는다.

---

# 정리된 예시 구조

| 기능 | 화면(GET)            | 처리(POST)                              | FORM 코드 핵심                                 |
| -- | ------------------ | ------------------------------------- | ------------------------------------------ |
| 등록 | /members/new       | /members                              | `<form action="/members" method="post">`   |
| 수정 | /members/{id}/edit | /members/{id}                         | `<form action="/members/1" method="post">` |
| 삭제 | (없거나 동일)           | /members/{id}/delete 또는 /members/{id} | `<form action="..." method="post">`        |

---

원하면

* HiddenHttpMethodFilter가 실제로 요청을 어떻게 바꾸는지
* Spring MVC에서 @RequestMapping 조합
* form-urlencoded로 넘어가는 실제 HTTP 메시지 예시
  도 이어서 알려줄게.

결론부터 말하면,

실제 등록 처리를 `/members/new` 로 POST하는 메서드는 만들지 않는다.
HTML 폼에서 화면을 보여주는 URL(GET)과 실제 데이터를 처리하는 URL(POST)을 분리하는 것이 일반적인 패턴이기 때문이다.

아래에서 이유를 아주 명확히 설명해줄게.

---

# 1. `/members/new`는 “화면을 보여주는 URL”이다

이 URL은 다음 역할만 한다:

```
GET /members/new
→ 회원 등록 폼 화면을 렌더링한다 (HTML만 보여줌)
```

* 이 URL은 리소스를 생성하는 작업이 아니라 화면 출력 역할
* 서버 상태를 변경하면 안 되는 “순수 조회” 요청
* 브라우저에서 새로고침해도 안전해야 함

그래서 GET 전용으로 사용하며
POST 처리를 여기에 붙이지 않는다.

---

# 2. 실제 등록 동작은 “리소스 컬렉션”인 `/members`에 POST한다

REST 관점에서 새 회원을 만들려면:

* 회원 전체 컬렉션(/members)에
* 새로운 리소스를 추가하는 동작 = POST

즉:

```
POST /members
```

이게 자연스러운 구조다.

### 왜 `/members/new`가 아니라 `/members`에 POST할까?

* `/members`는 "회원 목록(컬렉션)"의 URI
* POST는 “컬렉션에 새로운 객체를 추가”하는 의미
* `/members/new`는 "폼 화면"을 의미하는 UI URL일 뿐, REST 리소스가 아님

즉 /members/new 는 UI URL이고,
/members 는 실제 리소스 URL이다.

따라서 폼 action은 이렇게 된다:

```html
<form action="/members" method="post">
```

---

# 3. `/members/new`로 POST를 만들지 않는 이유 3가지

## 1) REST 관점에서 의미가 맞지 않음

`/members/new`는 리소스를 표현하는 URI가 아니다.
"화면"이라는 부가 기능에 대한 URI일 뿐이다.

회원 생성은 /members에서 일어나야 한다.

---

## 2) GET/POST 역할 분리가 무너진다

하나의 URL에:

* GET: 화면 출력
* POST: 등록 처리

를 동시에 묶으면, 역할이 섞여서 URL 의미가 불명확해진다.

실무에서는 GET은 “폼 표시”, POST는 “데이터 처리”로 고정하는 패턴을 따른다.

---

## 3) 브라우저 입장에서 URL 충돌 가능성

예를 들어 `/members/new`에서 POST 성공 후 리다이렉트 할 때:

* `/members/new`를 새로고침하면 다시 화면이 나와야 하는데
* 만약 POST 처리도 `/members/new`에 있다면 의도치 않게 동작이 섞인다.

URL은 행동에 따라 분리되어야 관리가 편하다.

---

# 4. 실제 MVC 구조 요약

### 등록 화면

```
GET  /members/new
```

### 등록 처리

```
POST /members
```

### HTML FORM

```html
<form action="/members" method="post">
    <input name="name">
    <input name="age">
    <button type="submit">등록</button>
</form>
```

---

# 5. 예외적으로 `/members/new`에 POST를 만들 수는 있는가?

기술적으로는 만들 수 있다.

```java
@PostMapping("/members/new")
public String create() { ... }
```

하지만 REST 관점에서도,
전통 MVC 패턴 관점에서도,
이 URL을 POST로 사용하지 않는다.

이유:

* URL 의미가 흐려지고
* REST 리소스 모델이 깨지고
* 유지보수성이 떨어진다

실무 관례에도 맞지 않는다.

---

# 결론

/members/new는 폼 화면(GET) 전용 URL이며,
실제 생성 처리는 항상 /members에 POST한다.

그래서 /members/new로 POST 메서드는 만들지 않는다.

원하면

* 수정 URL 구조가 동일한 원리로 어떻게 구성되는지
* 화면 URL과 API URL을 분리하는 원칙
* Spring MVC의 패턴에서 왜 이 구조가 표준인지
  도 이어서 자세하게 알려줄게.

정확하게 이해하고 있어.
`/new`, `/edit`, `/delete` 같은 경로는 리소스를 표현하는 URI(Resource URI)가 아니라, 화면(UI)이나 동작을 위한 “컨트롤(Controller) URI”라고 부른다.

좀 더 명확하게 정리해줄게.

---

# 1. URI에는 두 종류가 있다

REST에서 URI는 크게 두 가지 역할을 한다.

## 1) 리소스 URI

* 만들고, 읽고, 수정하고, 삭제되는 “대상(자원)”을 표현하는 URI
* REST의 중심 개념
* 보통 명사 형태

예:

```
/members
/members/1
/orders/10
```

이 URI들은

* GET → 조회
* POST → 생성
* PUT/PATCH → 수정
* DELETE → 삭제
  같은 REST 메서드를 적용할 수 있다.

즉, 상태 변화의 대상이 되는 실체(자원)을 나타낸다.

---

## 2) 컨트롤(Controller) URI

* “리소스”를 표현하는 게 아니라
* 그 리소스를 다루기 위한 행위(동작)나 화면(UI)을 제공하는 URI
* REST 리소스 컬렉션의 일부가 아님
* 보통 동사적 의미 또는 UI 목적

예:

```
/members/new        ← 입력 화면
/members/1/edit     ← 수정 화면
/members/1/delete   ← 삭제 버튼용 경로(POST)
```

이들은 리소스가 아니다.
그 자체가 생성되거나 삭제되는 존재가 아니다.

---

# 2. 그럼 /new가 컨트롤 URI냐?

맞다.

`/members/new`는 다음을 의미한다:

* “회원이라는 리소스를 생성하기 위한 입력 화면”
* “UI를 제공하는 경로”
* “리소스가 아니라, 리소스 생성 프로세스를 위한 컨트롤러 경로”

즉, REST 리소스가 아니라, 리소스를 다루는 도구(Controller URL)이다.

이 URI는 리소스(회원 데이터)를 표현하지 않고
화면을 제공할 뿐이다.

---

# 3. 왜 컨트롤 URI가 필요한가?

HTML FORM은 GET/POST 화면 이동 기반이라 다음이 필요하다:

* 입력 폼 화면(HTML) → GET
* 수정 폼 화면(HTML) → GET
* 삭제 확인 화면(HTML) → GET

이 화면들은 데이터가 아니다.
리소스도 아니다.

단순히 UI를 제공하는 역할이기 때문에
REST 리소스 URI(`/members`, `/members/{id}`)와는 별도로 존재한다.

---

# 4. 컨트롤 URI는 “행위”이므로 REST 규칙과 직접 연결되지 않는다

REST는 리소스를 표현하는 URI만 관심 있다.
즉:

* 리소스를 나타내는 URI는 명사적이어야 하고
* HTTP 메서드로 행동을 표현한다

하지만 `/new`, `/edit`는 행위이기 때문에
REST 리소스 규칙과는 무관한 “컨트롤러 전용” URI이다.

그래서 화면 UI가 있는 전통적 MVC에서는 이런 URI가 자연스럽게 생긴다.

---

# 5. 정리

### ✔ `/members`, `/members/{id}`

→ 리소스 URI
→ 데이터의 실체를 나타냄
→ REST 메서드로 CRUD 수행

### ✔ `/members/new`, `/members/{id}/edit`

→ 컨트롤(Controller) URI
→ 리소스가 아니라 화면(UI)을 제공
→ GET 요청 전용

### ✔ “컨트롤”의 의미

* 리소스를 표현하지 않음
* 리소스를 처리하기 위한 작업 경로
* HTML 화면/프로세스를 위한 URL

---

# 6. REST API만 사용하면 컨트롤 URI는 사라진다

SPA나 모바일 앱처럼 서버가 화면을 제공하지 않는 구조에서는:

* `/new`, `/edit` 같은 UI URL이 필요 없다.
* 화면은 프론트에서 만들고
* 서버는 오직 리소스 URI만 제공한다.

그래서 REST API 서버만 따로 구성하면 이렇게 됨:

```
POST   /members      ← 생성
GET    /members/{id} ← 조회
PUT    /members/{id} ← 수정
DELETE /members/{id} ← 삭제
```

컨트롤 URI가 사라지는 이유는
서버가 화면을 제공하지 않기 때문이다.

---

원하면 이어서

* 왜 MVC 환경에서는 컨트롤 URI가 필수인지
* REST 리소스 URI만 사용하는 환경에서는 어떻게 동작하는지
* URI 설계 원칙(명사, 복수형, 계층 구조 등)
  도 상세히 정리해줄게.

MVC 환경과 REST API 서버 환경은 구조 자체가 다르다.
그래서 MVC에서는 컨트롤 URI가 필수이고,
REST API 서버에서는 컨트롤 URI가 불필요하다.

아래에서 개념과 이유를 가장 명확하게 정리해줄게.

---

# 1. MVC 환경이란 무엇인가?

여기서 말하는 MVC는 Spring MVC·JSP·Thymeleaf 같은 전통적인 서버 렌더링 Web MVC 모델이다.

특징:

* 브라우저가 HTML 페이지를 요청하면
* 서버가 직접 HTML을 만들어서 응답함(View 렌더링)
* 서버가 "화면"을 만든다

즉,

### 서버 = 화면(HTML) + 비즈니스 로직 + DB + 라우팅

이 전체를 책임지는 구조가 MVC 환경이다.

대표적 사례:

* Spring MVC + Thymeleaf
* Django 템플릿
* Ruby on Rails의 ERB
* PHP 템플릿

브라우저에게 서버가 “HTML 웹페이지 전체”를 직접 준다고 이해하면 된다.

---

# 2. 왜 MVC 환경에서는 컨트롤 URI가 필수인가?

## 이유 1) “화면을 보여주는 URL”이 필요하기 때문

MVC 환경에서는 서버가 HTML 화면(입력 폼, 수정 폼, 목록 페이지)을 렌더링해서 내려준다.

예:

```
GET /members/new        → 회원 등록 화면
GET /members/1/edit     → 회원 수정 화면
GET /members/1/delete   → 삭제 확인 화면
```

이 HTML 화면 자체는 리소스가 아니다.
데이터도 아니다.
단지 UI를 보여주는 페이지다.

REST 리소스 규칙과 연결되지 않는다.

그래서 "화면 전용 URL"이 필요하게 되고
이것이 바로 컨트롤 URI다.

---

## 이유 2) “화면 표시(GET)”와 “데이터 처리(POST)”를 분리해야 하기 때문

HTML FORM은 두 단계로 동작한다:

1. GET → 폼을 보여주는 화면
2. POST → 폼을 제출하여 서버에 반영

MVC에서는 이 구조를 그대로 사용한다.

### 예: 회원 등록

#### 1) 폼 보기

```
GET /members/new
```

#### 2) 폼 제출

```
POST /members
```

여기서 GET 화면 `/members/new`는
REST API의 리소스가 아니다.
단순히 UI만 보여주는 경로라서 컨트롤 URI가 된다.

---

## 이유 3) 하나의 기능에 대해 "입력 단계"라는 개념이 존재

전통적인 웹 페이지는 다음처럼 여러 화면으로 나뉜다:

* 등록 입력 화면
* 수정 입력 화면
* 삭제 확인 화면

이 화면들은 리소스가 아니라 “흐름(flow)”과 “단계(step)”임.
그래서 이 단계를 나타내는 별도의 URI가 필요하다.

---

# 3. REST API 서버에서는 왜 컨트롤 URI가 필요 없을까?

REST API 서버는 단 하나만 한다.

데이터(JSON)만 주고받는다.
화면(HTML)은 전혀 만들지 않는다.

즉:

* 폼 화면 없음
* 수정 화면 없음
* 삭제 확인 화면 없음

클라이언트(프론트엔드, 모바일 앱)가 화면을 모두 만든다.

따라서 REST 서버는 다음만 제공하면 된다:

```
GET    /members
POST   /members
GET    /members/{id}
PUT    /members/{id}
DELETE /members/{id}
```

UI가 없어서
컨트롤 URI(/new, /edit)는 필요 없다.

---

# 4. 두 환경의 차이를 요약

| 구분                   | MVC 환경(서버 렌더링)     | REST API 서버 |
| -------------------- | ------------------ | ----------- |
| HTML 화면              | 서버가 직접 렌더링         | 클라이언트가 렌더링  |
| 폼(GET/POST)          | O                  | 없음          |
| REST 메서드(PUT/DELETE) | FORM 제약 때문에 사용 어려움 | 바로 사용 가능    |
| 컨트롤 URI(/new, /edit) | 필수                 | 불필요         |
| URL 설계               | 화면 단계 중심           | 리소스 중심      |

---

# 5. 핵심 결론

### ✔ MVC 환경(서버 렌더링)에서는

* 화면을 제공해야 하므로
* 입력 화면용 URL이 필요하고
* 그것이 바로 컨트롤 URI(/new, /edit 등)이다.

### ✔ REST API 서버에서는

* 화면을 제공하지 않으므로
* UI 흐름을 나타내는 /new, /edit 같은 URI는 필요 없다.
* 오직 리소스 URI만 제공한다.

---

원하면

* MVC와 RESTful API의 차이를 그림으로 설명하거나
* Spring Boot에서 MVC와 REST 컨트롤러 코딩 차이를 비교하거나
* SPA(React/Vue) 등장 후 MVC 환경이 어떻게 변화했는지
  도 이어서 설명해줄게.

## URI 설계 개념
1. 문서
- 단일 개념(파일 하나, 객체 인스턴스, 데이터베이스 row)
- 예) /members/100, /files/star.jpg
2. 컬렉션
- 서버가 관리하는 리소스 디렉터리
- 서버가 리소스의 URI를 생성하고 관리
- 예) /members
3. 스토어
- 클라이언트가 관리하는 자원 저장소
- 클라이언트가 리소스의 URI를 알고 관리
- 예) /files
4. 컨트롤러, 컨트롤 URI
- 추가 프로세스 실행
- 동사를 직접 사용
- 예) /members/{id}/new

HTTP 상태 코드는 클라이언트의 요청에 대해 서버가 어떤 결과를 반환했는지 알려주는 숫자 코드이다.
즉, 브라우저나 클라이언트가 요청을 보냈을 때:

* 성공했는지
* 리다이렉트해야 하는지
* 클라이언트 요청이 잘못됐는지
* 서버 내부에서 오류가 났는지

등을 알려주는 역할을 한다.

전체적으로 100~599까지 5가지 범위로 나뉘며, 각 범위는 명확한 의미가 있다.

---

# 1. 1xx: Informational(정보)

요청을 잘 받았고, 작업을 계속 진행 중임을 의미한다.
일반적인 웹 애플리케이션에서는 거의 사용되지 않는다.

예:

* 100 Continue
* 101 Switching Protocols
* 103 Early Hints

---

# 2. 2xx: Success(성공)

클라이언트 요청을 정상적으로 처리했다는 뜻이다.

가장 많이 사용되는 성공 코드가 포함된다.

## 대표 코드

### 200 OK

* 가장 기본적인 성공 응답
* GET /members → 목록 정상 반환

### 201 Created

* 새로운 리소스가 성공적으로 생성됨
* POST /members → 새 회원 생성 시 사용
* Location 헤더에 생성된 리소스 URI 담음

### 204 No Content

* 성공했지만 응답 본문이 없음
* DELETE /members/1 → 삭제 성공 시 자주 사용

---

# 3. 3xx: Redirection(리다이렉션)

클라이언트가 다른 URL로 이동해야 한다는 의미.

브라우저는 보통 자동으로 해당 URL로 이동한다.

## 대표 코드

### 301 Moved Permanently

* 영구 이동
* URL이 완전히 바뀌었을 때

### 302 Found

* 일시적 이동
* POST 처리 후 GET으로 redirect할 때 자주 사용(SPRING의 Redirect 방식)

### 303 See Other

* POST 처리 후 반드시 GET으로 바꿔서 요청하라는 의미
* PRG(Post-Redirect-Get) 패턴에서 많이 사용

### 304 Not Modified

* 캐시 사용
* 조건부 요청(If-None-Match 등)에서 변경 없음을 의미
* 브라우저는 로컬 캐시 사용

---

# 4. 4xx: Client Error(클라이언트 오류)

클라이언트가 잘못된 요청을 보냈다는 의미.

## 대표 코드

### 400 Bad Request

* 요청 형식이 잘못됨
* JSON 구조 오류, 쿼리 파라미터 이상 등

### 401 Unauthorized

* 인증 필요
* 로그인/토큰 문제가 있을 때

### 403 Forbidden

* 권한 없음
* 로그인은 했지만 접근 권한 없음

### 404 Not Found

* 요청한 리소스가 없음
* URL 잘못되었거나 id에 해당하는 데이터 없음

### 405 Method Not Allowed

* URL은 맞는데 HTTP 메서드가 잘못됨
  예: GET만 가능한 URL에 POST 요청

### 409 Conflict

* 현재 리소스 상태와 충돌
  예: 중복 회원, 버전 충돌

### 429 Too Many Requests

* 요청이 너무 많음 → rate limit

---

# 5. 5xx: Server Error(서버 오류)

서버 내부에서 문제가 발생한 경우.

## 대표 코드

### 500 Internal Server Error

* 서버 내부 예외
* 가장 일반적인 서버 오류

### 502 Bad Gateway

* 게이트웨이/프록시에서 잘못된 응답 받은 경우

### 503 Service Unavailable

* 서버 과부하, 점검 중

### 504 Gateway Timeout

* 서버가 특정 내부 서비스로부터 응답을 못 받음

---

# 6. 전체 요약 표

| 코드  | 의미       | 설명                  |
| --- | -------- | ------------------- |
| 1xx | 정보       | 거의 사용 X             |
| 2xx | 성공       | 200/201/204 자주 사용   |
| 3xx | 리다이렉션    | 302/303/304         |
| 4xx | 클라이언트 오류 | 400/401/403/404/409 |
| 5xx | 서버 오류    | 500/502/503/504     |

---

# 7. 실무에서 특히 중요하게 쓰는 상태 코드

### 성공

* 200 OK
* 201 Created
* 204 No Content

### 리다이렉션

* 302 Found
* 303 See Other
* 304 Not Modified

### 클라이언트 오류

* 400 Bad Request
* 401 Unauthorized
* 403 Forbidden
* 404 Not Found
* 409 Conflict

### 서버 오류

* 500 Internal Server Error

---

필요하다면

* Spring Boot에서 상태 코드 직접 설정하는 방법
* POST-Redirect-GET 패턴이 왜 303 또는 302를 쓰는지
* REST API 설계 시 어떤 상태코드를 어떤 상황에 써야 하는지
  등도 자세히 설명해줄게.
