HTTP에서 “메서드가 캐시 가능하다”는 말은, 그 메서드를 사용해 요청한 결과를 중간 캐시(브라우저 캐시, 프록시 캐시, CDN 등)가 저장해 두었다가 나중에 동일한 요청이 오면 서버까지 가지 않고 캐시된 응답을 재사용할 수 있다는 뜻이다.

좀 더 핵심적으로 정리하면 다음과 같다.

---

# 1. “메서드가 캐시 가능하다” = “해당 메서드의 응답을 캐시할 수 있다”

HTTP 스펙은 각 메서드가 기본적으로 캐시 대상이 될 수 있는지를 규정한다.

* GET → 캐시 가능
* HEAD → 캐시 가능
* POST, PUT, DELETE, PATCH → 원칙적으로 캐시 불가
  (하지만 명시적으로 Cache-Control 등을 사용하면 제한적으로 가능)

즉, 캐시 가능 메서드는 응답을 저장해도 안전하다고 기본적으로 인정되는 메서드다.

---

# 2. 왜 GET과 HEAD만 기본적으로 캐시 가능할까?

GET과 HEAD는 안전한(safe) 메서드다.
즉, 서버 리소스를 변경하지 않는다고 간주된다.

반대로 POST/PUT/DELETE는 서버 상태를 바꾸는 메서드라서 응답을 캐싱해버리면 다음 문제가 생긴다:

* POST 응답을 캐싱해버리면 이후 POST 요청이 서버에 도착하지 않아 실제 데이터가 저장되지 않음
* PUT/DELETE 응답을 캐싱하면 상태 변화가 무시됨

그래서 기본적으로 캐싱 불가이다.

---

# 3. 캐시 가능 = 무조건 캐시된다는 뜻은 아니다

GET이라도 다음을 포함하면 캐시는 저장하지 않거나 재사용하지 않는다:

* `Cache-Control: no-store`
* `Cache-Control: no-cache`
* `Pragma: no-cache`
* 쿠키 포함 요청 (상황에 따라 캐시 회피)
* 인증 헤더 포함 요청(기본적으로 공유 캐시에서 사용 안 함)

즉,
캐시 가능 메서드 → 캐싱할 수 있는 후보일 뿐, 캐시 여부는 Cache-Control, ETag 등 정책에 따름

---

# 4. 같은 GET이라도 캐시 동작은 정책에 따라 달라짐

예시로 보자.

## (1) 캐시될 수 있는 GET

```
GET /images/logo.png
Cache-Control: max-age=3600
```

→ CDN, 브라우저 모두 응답 저장 후 1시간 재사용 가능

## (2) 캐시하면 안 되는 GET

```
GET /user/profile 
Cache-Control: no-store
Authorization: Bearer xxx
```

→ 캐시 불가능

---

# 5. (심화) POST도 캐시 가능할 수는 있다

스펙상 POST는 기본적으로 캐시 대상이 아니지만, 아래와 같이 서버가 명시적으로 지시하면 캐시 가능하다:

```
Cache-Control: max-age=600
```

그러면 캐시는 POST 응답도 저장할 수 있다.
하지만 실무에서 POST 캐싱은 거의 쓰이지 않는다.

---

# 핵심 정리

* “HTTP 메서드가 캐시 가능하다” = 해당 메서드로 받은 응답이 기본적으로 캐시될 수 있다.
* GET, HEAD만 기본적으로 캐시 가능.
* POST/PUT/DELETE는 기본적으로 캐시 불가.
* 그러나 실제 캐싱 여부는 Cache-Control/ETag 등 정책에 따라 결정된다.

원하면

* 메서드별 캐시 가능성
* 캐시 무효화 조건
* CDN/브라우저 캐시 흐름
  같은 더 깊은 내용도 설명해줄게.

정확하게는 “같은 메서드를 쓴다”가 기준이 아니라, “같은 요청(URI + 헤더 조건)”일 때 서버에 보내지 않고 캐시된 응답을 재사용할 수 있다는 의미다.

즉, 메서드는 캐시 가능 여부를 판정하는 “전제 조건”일 뿐이고,
실제로 캐시 사용 여부는 요청의 전체 조건을 보고 판단한다.

아래에서 정확히 정리해줄게.

---

# 1. 캐싱의 본질: “같은 요청이면 서버로 안 가고 응답을 재사용”

예를 들어 GET 요청이 있다고 하자.

```
GET /products/1
```

이 요청을 브라우저나 CDN이 캐시하고 있다면:

* 동일한 URL
* 동일한 QueryString
* 동일한 Host 헤더
* 동일한 Vary에 의해 정의된 헤더 값들(예: Accept-Language)

이 모두 같으면
서버로 요청을 보내지 않고 캐시된 응답을 그대로 반환할 수 있다.

---

# 2. "메서드가 같으면 캐싱한다"는 오해

캐시 여부는 메서드가 아니라 캐시 정책(Cache-Control, ETag, Expires 등)에 따라 결정된다.

* GET은 기본적으로 캐시 "가능한" 메서드라서 캐시 후보가 된다
* 하지만 GET이라도 no-store/no-cache가 있으면 캐시를 쓰지 않는다
* POST라도 서버가 명시적으로 캐시 가능하게 만들면 캐시가 일어날 수도 있다

즉,
캐시 사용 여부는 HTTP 메서드가 아니라 요청·응답의 캐시 정책이 결정한다.

---

# 3. 실제 캐싱 동작 시나리오

## (1) 캐시 HIT — 서버로 가지 않음

```
# 캐시 안에 이미 저장된 응답이 있고, 아직 유효할 때
GET /products/1
→ 캐시된 응답 바로 반환 (서버로 X)
```

## (2) 조건부 요청 — 서버까지 가지만 전체 응답 받지 않음

캐시가 있지만 오래됨 → ETag를 사용해서 서버에 확인

```
GET /products/1
If-None-Match: "abc123"

서버: 변경 없음 → 304 Not Modified
```

→ 클라이언트는 캐시된 본문을 계속 사용
→ 이 경우도 실제 새 데이터는 서버에서 안 옴

이것도 넓은 의미에서는 캐시 사용이다.

---

# 4. 캐시의 판단 기준은 “요청의 동일성”

요청이 동일한지 판단할 때 고려하는 요소:

* URI
* QueryString
* Host
* HEAD/GET 등 메서드
* Vary 헤더에 명시한 요청 헤더 (예: Accept-Encoding)

이 조건이 모두 일치해야 캐시 HIT가 가능하다.

---

# 5. 결론

질문:
“캐싱한다는 게 같은 메서드를 쓰면 보내지 않고 캐시 응답을 쓰는 의미인가?”

답:
아니다.
메서드는 캐시 가능 여부를 결정하는 요소일 뿐이고,
실제 캐시 재사용 여부는 URI, 헤더, 캐시 정책이 모두 같을 때 캐시된 응답을 서버 요청 없이 사용한다는 의미다.

---

원하면

* 캐싱의 판단 기준(캐시 키)
* Expires / Cache-Control / ETag 차이
* 브라우저 캐시 vs CDN 캐시
* private / public 캐시 차이
  도 자세히 설명해줄게.

HTTP 캐싱은 정해진 ‘고정 용량’이나 ‘고정 시간’이 있는 기능이 아니다.
HTTP 표준은 “얼마나 오래 저장할 수 있는지”와 “얼마나 큰 데이터를 저장할 수 있는지”를 지정하지 않는다.
대신 다음 두 요소에 의해 결정된다.

* 캐시 정책(서버·클라이언트가 정함) → 시간
* 캐시 저장소의 역량(브라우저·CDN·프록시가 정함) → 용량

아래에서 범위와 한계를 명확히 설명하겠다.

---

# 1. 캐싱 가능 "시간" 범위

HTTP는 시간 범위를 Cache-Control 헤더로 모두 개발자가 지정하도록 하고 있다.

대표 옵션:

### 1) `max-age=초`

이 시간이 지나기 전까지는 서버에 가지 않고 캐시를 그대로 재사용한다.

예:

```
Cache-Control: max-age=31536000
```

→ 1년 동안 캐싱 가능

표준상으로 1년(31536000초) 이상도 가능하다.
HTTP 스펙은 상한선을 두지 않는다.

### 2) `Expires: 날짜`

이 날짜까지 유효.

### 3) 절대 상한 없음

즉,

* 10초도 가능
* 1시간도 가능
* 1년도 가능
* 그 이상도 가능
* 개발자가 정하지 않으면 기본 정책(브라우저 자동 판단)이 적용됨

단, 브라우저나 CDN이 너무 비현실적인 시간을 지정하면 내부적으로 조정할 수는 있다.

실무에서 CDN은 1년 캐싱이 흔하다(정적 리소스: 이미지, CSS, JS).

---

# 2. 캐싱 가능 "용량" 범위

HTTP 표준은 캐시 용량을 지정하지 않는다.
용량은 캐시 저장소 종류에 따라 완전히 달라진다.

### 1) 브라우저 캐시 용량

브라우저마다 다르다.

* Chrome: 대략 디스크 총 용량의 1~10%
* Firefox/Safari: 유사한 비율
* PC에서는 수백 MB~수 GB까지도 갈 수 있음
* 모바일은 보통 적게(수십~수백 MB)

정해진 숫자가 아니라 디바이스 환경에 따라 동적으로 결정된다.

### 2) 메모리 캐시(브라우저)

* 수십 MB 수준
* 브라우저가 알아서 관리하며 오래된 항목은 금방 지움

### 3) CDN 캐시

용량 제한은 사실상 없음.
Cloudflare, Akamai, Fastly 같은 CDN은:

* 수 TB~수 PB 단위로 저장 가능
* 특정 파일 크기에도 제한 없음(GB 파일도 캐싱 가능)

### 4) Reverse Proxy (nginx, varnish)

* 운영자가 직접 설정한 디스크 용량에 따라 결정
* 1GB, 10GB, 1TB 등 자유롭게 설정 가능

---

# 3. 캐시가 실제로 유지되는 조건

캐시가 설정돼 있어도 다음 상황이면 유지되지 않을 수 있다.

### 1) 저장소 용량 부족

오래된 캐시가 자동 삭제됨.

### 2) 재부팅 또는 브라우저 강제 초기화

캐시가 모두 지워짐.

### 3) 동일 자원인데 URL 변경

예: `/app.js?v=2` → 캐시 무효화

### 4) 정책에 따라 캐시 무효화

서버에서 Cache-Control을 바꾸면 즉시 새 정책 적용.

---

# 4. 정리

### 시간

* HTTP 스펙은 상한선을 두지 않는다.
* 실무에서 대부분의 정적 리소스는 최대 1년 캐싱.
* 필요하면 더 오래도 가능.

### 용량

* 스펙이 아니라 저장소(브라우저/프록시/CDN)의 능력에 따라 달라진다.
* 브라우저는 수백 MB~수 GB
* 서버 측 CDN/프록시는 사실상 제한 없음

---

필요하면 다음도 자세히 설명할 수 있다.

* 브라우저 캐시와 CDN 캐시의 차이
* 강력 캐시 vs 조건부 캐시
* 이미지/JS/CSS 캐시 전략
* SPA 프레임워크(React/Vue)의 캐싱 전략
  말해줘.

아니다.
브라우저에서 로그인 정보가 “저장되는 것”은 HTTP 캐싱 때문이 아니다.
로그인 상태가 유지되는 이유는 쿠키 혹은 Web Storage(LocalStorage/SessionStorage) 때문이다.

HTTP 캐시는 “응답 데이터(HTML, CSS, 이미지 등)를 저장해 두는 기능”이고,
로그인 상태는 사용자 인증 정보를 저장해야 하므로 전혀 다른 메커니즘이다.

아래에서 명확히 구분해줄게.

---

# 1. 로그인 정보가 저장되는 이유

로그인 상태 유지 기술은 크게 두 가지다.

---

## 1) 쿠키(cookie)

가장 전통적이고 일반적인 방식.

* 로그인 성공 → 서버가 `Set-Cookie` 로 세션ID 또는 access token을 브라우저에 저장
* 이후 모든 요청에 쿠키 자동 포함 → 로그인 상태 유지
* 만료 시간이 지나면 자동 로그아웃

예:

```
Set-Cookie: SESSION=abc123; HttpOnly; Secure; Max-Age=3600
```

로그인 유지 = 쿠키 덕분

---

## 2) Web Storage (LocalStorage / SessionStorage)

SPA(React/Vue 등)에서 많이 사용.

* 브라우저의 키-값 저장소에 토큰을 직접 저장
* 개발자가 자바스크립트로 관리
* HTTP 요청에 자동 전송되지 않음(직접 헤더에 넣어야 함)

예:

```javascript
localStorage.setItem("access_token", "abc123");
```

이것도 캐시와는 무관

---

# 2. HTTP 캐시는 로그인 상태를 저장하지 않는다

HTTP 캐시는 다음 종류의 데이터를 저장한다.

* 이미지
* CSS
* JS
* HTML
* 글 목록 API 응답
* 정적 파일

즉, “리소스 응답”을 저장하는 기능일 뿐,
세션ID나 토큰 등 인증 정보를 저장하지 않는다.

오히려 보안 때문에 로그인 관련 응답은 캐시가 안 되도록 설정하는 게 기본이다.

예:

```
Cache-Control: no-store
```

로그인 페이지, 사용자 정보 페이지는 대부분 이렇게 되어 있다.

---

# 3. 혼동이 발생하는 이유

브라우저가 여러 데이터를 저장하니까 햇갈릴 수 있다.

브라우저 저장 영역은 크게 4종류인데, 용도는 모두 다르다:

| 저장 기술              | 용도       | 로그인과 관련?       |
| ------------------ | -------- | -------------- |
| HTTP Cache     | 파일/응답 저장 | 관련 없음 (오히려 꺼둠) |
| Cookie         | 세션, 인증   | 핵심             |
| LocalStorage   | 토큰 저장    | 간혹 사용          |
| SessionStorage | 탭 단위 상태  | 일부 사용          |

---

# 4. 결론

브라우저에서 로그인 상태가 유지되는 것은 캐싱 때문이 아니라 쿠키 또는 Web Storage 때문이다.
HTTP 캐시는 로그인 상태 유지에 사용되지 않는다.

원하면

* 세션 기반 로그인
* JWT 기반 로그인
* Cookie vs LocalStorage 보안 차이
* 쿠키의 HttpOnly, Secure 의미
  같은 것도 자세히 설명해줄게.

HTTP에서 클라이언트가 서버로 데이터를 보내는 방식은 크게 두 가지로 나눌 수 있다.

1. 쿼리 파라미터(Query Parameter)를 통한 전송
2. 메시지 바디(Message Body)를 통한 전송

이 두 방식은 용도와 제약이 다르다. 아래에서 정확하게 정리해줄게.

---

# 1. 쿼리 파라미터(Query Parameter)를 통한 데이터 전송

URL 뒤에 `?key=value` 형태로 붙여서 보내는 방식이다.

예:

```
GET /search?query=spring&page=2
```

## 특징

### 1) 주소(URL)에 직접 포함됨

* 그로 인해 북마크 가능
* URL 공유 가능

### 2) 주로 GET 요청에서 사용

* 서버 데이터를 변경하지 않는 요청에 적합
* 검색, 필터링, 페이징 등 리소스 조회 시 많이 사용함

### 3) 용량 제한 존재

브라우저·서버마다 다르지만 일반적으로

* 수 KB~수 MB 사이 제한이 있음
* 실무에서는 URL 자체가 짧아야 함
  대용량 데이터 전송에는 부적합

### 4) 보안 이슈

* URL 로그, 브라우저 히스토리, 프록시 캐시에 남을 수 있음
* 민감한 정보 전송에 부적절

### 5) 포맷이 제한적

키-값 형태의 간단한 데이터 구조에 적합
복잡한 JSON 구조 보내기 어려움

---

# 2. 메시지 바디(Message Body)를 통한 데이터 전송

HTTP 메시지 바디에 데이터를 넣어 보내는 방식이다.

예:

```
POST /login
Content-Type: application/json

{
  "username": "test",
  "password": "1234"
}
```

## 특징

### 1) POST/PUT/PATCH 등에서 사용

리소스 생성·수정·등록 같은 작업에 사용된다.

### 2) 크기 제한이 사실상 없음

* 서버 설정에 따라 제한될 뿐
* 실무적으로 이미지 등 큰 파일도 전송 가능

### 3) 데이터 구조 자유로움

* JSON
* XML
* Form data
* multipart/form-data (파일 업로드)

복잡한 구조 전달 가능.

### 4) URL에 기록되지 않음

* 히스토리, 서버 로그에 남지 않음 (일반적으로)
* 민감한 데이터는 바디로 보내는 것이 바람직

### 5) 캐싱 대상 아님

* 바디가 있는 요청(POST 등)은 기본적으로 캐시 불가

---

# 3. 언제 어떤 방식이 적합할까?

| 구분      | 쿼리 파라미터       | 메시지 바디            |
| ------- | ------------- | ----------------- |
| 요청 메서드  | 주로 GET        | POST/PUT/PATCH 등  |
| 목적      | 조회            | 생성/수정/로그인 등       |
| 데이터 크기  | 작아야 함         | 제한 없음             |
| 데이터 구조  | 단순한 key-value | 복잡한 구조(JSON 등) 가능 |
| 민감한 데이터 | 부적합           | 적합                |
| 북마크/공유  | 가능            | 불가능               |

---

# 4. 예시로 더 확실하게 이해하기

## 1) 검색 기능 (쿼리 파라미터)

```
GET /products?category=book&page=3
```

## 2) 회원 가입 요청 (메시지 바디)

```
POST /members
Content-Type: application/json

{
  "name": "홍길동",
  "email": "test@example.com",
  "password": "1234"
}
```

## 3) 로그인 (메시지 바디)

```
POST /login
username=test&password=1234
```

---

# 결론

* 쿼리 파라미터 → 주로 GET. 데이터 조회. URL에 직접 들어감. 크기 제한. 기록됨.
* 메시지 바디 → POST 등에서 사용. 생성/수정. 구조 자유롭고 대용량 가능. 기록되지 않음.

원하면

* GET vs POST 차이
* Form submit 방식(application/x-www-form-urlencoded vs multipart)
* RESTful API에서 어떤 데이터를 어디에 담는지
  도 상세히 설명해줄게.

“정적 데이터 조회”와 “동적 데이터 조회”는 서버가 응답을 만들어내는 방식의 차이를 기준으로 나눈 개념이다.
둘의 경계가 모호한 경우도 있지만, 일반적으로 다음처럼 구분한다.

---

# 1. 정적 데이터 조회

서버가 미리 준비해 둔 파일(콘텐츠)을 그대로 반환하는 조회 방식이다.

예:

* HTML, CSS, JS 파일
* 이미지(jpg, png, svg)
* 글 내용이 바뀌지 않는 문서(고정된 안내 페이지 등)
* CDN에서 제공하는 정적 자원

## 특징

### 1) 서버 로직 없이 바로 반환

서버는 단순히 파일을 찾아서 내려주기만 한다.
별도의 계산이나 DB 조회가 없다.

### 2) 캐싱하기 좋음

내용이 자주 바뀌지 않기 때문에:

* 브라우저 캐시
* 프록시 캐시
* CDN 캐시

모두 적극적으로 활용할 수 있다.

### 3) 요청마다 응답이 거의 동일

사용자가 누구인지, 어떤 조건인지와 무관하다.

---

# 2. 동적 데이터 조회

사용자나 상황에 따라 서버가 조회·계산·로직 처리 등을 거쳐 매번 새 응답을 생성하는 방식이다.

예:

* 로그인한 사용자 정보 조회
* 상품 검색 결과
* 주문 목록
* 게시글 목록(정렬, 필터 적용)
* 백엔드에서 DB를 읽고 JSON으로 응답하는 API

## 특징

### 1) 서버 로직을 통해 응답 생성

대표적으로:

* DB 조회
* 비즈니스 로직 처리
* 인증/권한 체크
* 매번 내용이 달라짐

### 2) 캐싱에 제한 있음

사용자에 따라 응답이 달라질 가능성이 있으므로
무조건 캐싱하기 어렵고,

* private 캐시(브라우저 캐시)
* 조건부 캐싱(ETag)

등을 통해 제한적으로 캐시한다.

### 3) 요청마다 결과가 달라짐

요청 시점, 사용자, 검색 조건 등에 따라 응답이 바뀜.

---

# 3. 비교 정리

| 구분       | 정적 데이터 조회             | 동적 데이터 조회             |
| -------- | --------------------- | --------------------- |
| 응답 생성 방식 | 이미 존재하는 파일 그대로 제공     | 서버가 매번 로직·DB로 생성      |
| 콘텐츠 변화   | 거의 없음                 | 자주 변함                 |
| 캐싱       | 매우 쉬움                 | 조건부 또는 제한적            |
| 예시       | 이미지, CSS, JS, 고정 HTML | 사용자 프로필, 상품 목록, 검색 결과 |
| 처리 비용    | 매우 낮음                 | 상대적으로 높음              |

---

# 4. 예시로 더 쉽게 이해하기

## 정적 데이터 예시

```
GET /images/logo.png
→ 서버가 디스크/CDN에서 그 파일을 그대로 반환
```

## 동적 데이터 예시

```
GET /users/123/orders
→ 서버가 DB에서 주문 목록을 조회해 JSON 생성 후 반환
```

---

# 결론

* 정적 데이터 조회: 고정된 파일을 그대로 반환, 캐싱에 최적, 빠르고 단순
* 동적 데이터 조회: DB/로직을 거쳐 생성되는 응답, 사용자·상황에 따라 달라짐, 캐싱이 제한적

원하면

* 왜 정적 파일을 CDN에서 제공하는지
* 동적 데이터 캐싱 전략(ETag, last-modified)
* REST API에서 정적/동적을 어떻게 구분하는지
  같은 것도 설명해줄게.

직접적으로 연결된 개념은 아니지만, “정적/동적 조회”와 “쿼리 파라미터(QUERY)”는 실무에서 자주 함께 등장하기 때문에 관련이 있다고 느끼기 쉽다.
정확히 짚어서 설명해줄게.

---

# 1. 정적 조회 ↔ 동적 조회는 “응답이 어떻게 만들어지는가”에 대한 분류

* 정적 조회 = 이미 존재하는 파일을 그대로 반환
* 동적 조회 = 서버 로직·DB 조회를 통해 매번 새로 응답 생성

즉, 서버 응답 생성 방식을 기준으로 나누는 개념이다.
이건 쿼리 파라미터가 있든 없든 전혀 상관없다.

---

# 2. 쿼리 파라미터는 “요청에 조건을 전달하는 방식”

쿼리 파라미터는 다음 같은 용도로 쓰인다:

```
/products?page=2&sort=price
```

* 필터링
* 정렬
* 페이징
* 검색어 전달

즉, 요청의 옵션/조건을 표현하는 기술이다.

---

# 3. 둘의 관계는 “동적 조회에서 쿼리 파라미터가 자주 사용될 뿐”

정적 조회에서는 쿼리 파라미터 대부분 필요 없다.

* 정적: `/image/logo.png`
  → 파라미터 없이 그대로 반환

동적 조회에서는 쿼리 파라미터가 거의 필수다.

* 동적: `/products?page=3&sort=price`
  → 동적 로직(DB 조회)을 위해 조건이 필요하므로 쿼리 파라미터가 많이 쓰임

따라서:

* 정적 조회는 파라미터가 거의 없음
* 동적 조회는 파라미터가 자주 있음

하지만 이것은 “관계가 있다”는 뜻이 아니라
실무 패턴상 함께 나타날 뿐, 개념적으로 직접 연결된 것은 아니다.

---

# 4. 결론: 둘은 원리적으로는 별개지만 실무에서는 자주 함께 등장

정리하면:

* 정적/동적 조회 → 응답이 만들어지는 방식
* 쿼리 파라미터 → 요청 조건을 전달하는 방법

개념적으로는 관련 없음.
하지만 동적 조회의 조건을 표현하기 위해 쿼리 파라미터를 자주 사용함.

---

원하면 더 이어서

* 쿼리 파라미터 vs 메시지 바디를 동적 조회에서 어떻게 선택하는지
* REST API에서 URL/쿼리/바디의 역할 구분
  도 정리해줄게.

HTML의 `<form>` 태그와 HTTP 메서드는 입력 데이터를 서버로 전송하는 방식과 직접적으로 연결된다.
하지만 HTML 폼이 지원하는 HTTP 메서드에는 제한이 있다.

전체 구조를 정리해줄게.

---

# 1. HTML `<form>`은 “어떤 HTTP 메서드로 요청을 보낼지”를 결정한다

폼은 다음과 같이 작성한다.

```html
<form action="/submit" method="POST">
    <input name="username">
    <button type="submit">Send</button>
</form>
```

`method` 속성에 따라 브라우저가 어떤 HTTP 요청을 보낼지 결정된다.

---

# 2. 폼이 기본적으로 지원하는 메서드는 단 2개

HTML 스펙상 `<form>`은 다음 두 가지 HTTP 메서드만 직접 지원한다.

1. GET
2. POST

다른 메서드(DELETE, PUT, PATCH)를 폼에서 직접 보낼 수 없다.

---

# 3. 폼에서 method="GET"일 때

```html
<form method="GET">
```

브라우저는 데이터를 쿼리 파라미터(URL)에 붙여서 보낸다.

예)

```
GET /search?q=apple&page=1
```

특징:

* 데이터가 URL에 노출됨
* 파일 전송 불가
* 주로 조회용

---

# 4. 폼에서 method="POST"일 때

```html
<form method="POST">
```

브라우저는 데이터를 메시지 바디에 넣어 전송한다.

예)

```
POST /login
Content-Type: application/x-www-form-urlencoded

username=test&password=1234
```

`enctype`에 따라 바디 포맷이 달라진다.

예:

* `application/x-www-form-urlencoded` (기본)
* `multipart/form-data` (파일 업로드)
* `text/plain` (거의 사용되지 않음)

---

# 5. 왜 FORM은 PUT, DELETE를 직접 못 쓸까?

HTML이 처음 만들어졌을 때
웹은 *“문서 전송(GET) / 폼 데이터 제출(POST)”*만 고려했다.
REST 개념과 PUT/DELETE 메서드는 훨씬 나중에 도입된 개념이다.

그래서 HTML FORM은 아직도 GET/POST만 공식 지원한다.

---

# 6. 실무에서는 어떻게 해결하나?

REST API는 PUT/DELETE를 필요로 하지만, `<form>`은 지원하지 않는다.

그래서 실무에서는 두 가지 방식 사용:

## 1) 폼은 POST로 보내고 서버가 “메서드 숨김 기법” 처리

예)

```html
<form method="POST" action="/members/1">
    <input type="hidden" name="_method" value="DELETE">
    <button type="submit">삭제</button>
</form>
```

서버는 `_method=DELETE`를 보고 DELETE 요청처럼 처리한다.

Spring MVC에서는 `HiddenHttpMethodFilter`가 이 역할을 한다.

## 2) 자바스크립트 fetch를 사용해 PUT/DELETE 보내기

SPA에서는 아예 폼을 안 쓰고 JS로 요청을 보낸다.

```javascript
fetch('/members/1', {
  method: 'DELETE'
})
```

---

# 7. 정리

HTML FORM과 HTTP 메서드 관계를 요약하면:

* `<form>`은 HTTP 요청을 보낼 수 있는 HTML 요소
* `method` 속성으로 HTTP 메서드를 지정
* 폼은 GET, POST만 공식 지원
* GET → 데이터가 URL로 전달
* POST → 데이터가 메시지 바디로 전달
* PUT/DELETE는 폼에서 직접 불가 → 숨김 필드나 JS로 해결

---

원하면 이어서

* GET/POST의 브라우저 자동 동작 차이
* enctype별 바디 포맷
* FormData와 multipart 업로드 원리
  도 더 상세히 설명해줄게.

REST API가 PUT/DELETE를 “필요로 한다”는 말은, 리소스를 다루는 행위를 HTTP 메서드에 일관되게 매핑하는 REST의 원리 때문이다.
HTML FORM을 꼭 써야 하는 것은 아니고, 오히려 대부분의 REST API 환경에서는 폼을 거의 사용하지 않는다.
하나씩 정리해줄게.

---

# 1. 왜 REST API는 PUT/DELETE를 “필요로” 하는가?

REST는 리소스를 다음과 같은 규칙으로 조작하는 것을 권장한다.

| 리소스 조작 | REST 권장 메서드 |
| ------ | ----------- |
| 조회     | GET         |
| 생성     | POST        |
| 전체 수정  | PUT         |
| 부분 수정  | PATCH       |
| 삭제     | DELETE      |

즉, REST는 행동을 HTTP 메서드로 명확하게 표현하는 방식이다.

예를 들어 사용자 리소스(member)를 다룬다고 해보자.

### 1) 생성(create)

```
POST /members
```

### 2) 수정(update)

```
PUT /members/10
```

### 3) 삭제(delete)

```
DELETE /members/10
```

이처럼 리소스를 “CRUD 형태로 다룰 때” REST는 PUT/DELETE가 매우 자연스럽게 필요해진다.

특히:

* PUT: 리소스 전체 교체
* DELETE: 리소스 삭제

따라서 REST 스타일로 API를 설계하면 PUT과 DELETE 메서드는 자연스럽게 필요해진다.

---

# 2. HTML FORM에서는 PUT/DELETE를 직접 못 쓰는 이유

HTML FORM의 `method`는 GET과 POST만 지원한다.

```html
<form method="GET">
<form method="POST">
```

PUT, DELETE가 스펙에 없다.
HTML이 만들어질 당시에는 REST 개념이 없었기 때문이다.

---

# 3. REST API를 사용할 때 form을 꼭 써야 하는가?

결론: 아니다. 대부분 form을 아예 쓰지 않는다.

왜냐하면 REST API는 보통 다음 환경에서 쓰이기 때문이다:

* 모바일 앱(Android/iOS)
* SPA(React, Vue, Angular)
* 백엔드 서버 간 API
* 프론트엔드 → 백엔드 통신(ajax 요청)
* jQuery fetch/ajax
* JavaScript fetch API

이런 경우에는 전부:

```javascript
fetch("/members/10", { method: "PUT" })
fetch("/members/10", { method: "DELETE" })
```

처럼 브라우저에서 자바스크립트로 직접 PUT/DELETE 요청을 보낸다.

즉,

* 웹 페이지 + 서버 렌더링 환경 ⇢ FORM을 사용할 수 있음(과거)
* SPA/모바일/프론트-백엔드 분리 환경 ⇢ JS 기반 REST 통신(현재 대부분)

REST API 개발에서는 form을 반드시 쓸 필요가 없다.

---

# 4. 그래도 폼 제출로 PUT/DELETE가 필요하다면?

서버 사이드 렌더링(SSR)의 MVC 환경(전통적인 Spring MVC / Thymeleaf)에서는 여전히 HTML FORM을 쓴다.
이때 PUT/DELETE를 직접 보낼 수 없어서 “숨김 메서드 기법”을 사용한다.

예:

```html
<form method="POST" action="/members/10">
    <input type="hidden" name="_method" value="DELETE">
    <button type="submit">삭제</button>
</form>
```

Spring의 `HiddenHttpMethodFilter`가 `_method` 값을 읽어 DELETE 요청으로 변환해 준다.

이건 HTML FORM의 한계를 보완하기 위한 꼼수일 뿐이다.

---

# 5. 정리

### 1) REST API는 리소스 조작을 명확하게 표현하기 위해 PUT/DELETE가 자연스럽게 필요하다.

### 2) HTML FORM은 GET과 POST만 지원하므로 REST 메서드를 직접 사용하기 어렵다.

### 3) 현대적인 REST API 사용 방식에서는 대부분 form을 쓰지 않는다.

* JS fetch
* axios
* 모바일 앱
* SPA 프론트엔드
  등에서 PUT/DELETE를 바로 쓴다.

### 4) 서버 렌더링 환경에서 폼으로 PUT/DELETE를 써야 할 때만 숨김 필드 방식을 써서 우회한다.

---

원하면 다음도 설명해줄 수 있다:

* PUT과 POST의 차이를 예제 코드로 비교
* PATCH가 필요한 이유
* RESTful URL 설계 규칙
* HiddenHttpMethodFilter가 동작하는 원리
  말해줘.

HTML FORM에서 전송되는 Content-Type은 `method`가 아니라 `enctype` 속성에 의해 결정된다.
FORM을 사용할 때 브라우저가 메시지 바디에 어떤 형식으로 데이터를 넣어서 보낼지를 결정하는 역할을 한다.

HTML FORM의 Content-Type은 크게 세 가지가 있다.

---

# 1. 기본값: `application/x-www-form-urlencoded`

FORM에서 `enctype`을 지정하지 않으면 자동으로 이 타입이 된다.

```html
<form method="POST">
```

브라우저가 보낼 때:

```
POST /login
Content-Type: application/x-www-form-urlencoded

username=hello&password=1234
```

## 특징

* 공백은 `+`로 인코딩됨
* 문자는 URL 인코딩됨
* key=value&key2=value2 형태
* 서버(특히 Java/Spring)의 기본 파싱 방식과 자연스럽게 맞아떨어짐
* 파일 업로드 불가능

웹에서 가장 많이 사용되는 FORM Content-Type이다.

---

# 2. 파일 업로드용: `multipart/form-data`

파일 업로드(input type="file")가 포함되면 이 타입을 사용해야 한다.

```html
<form method="POST" enctype="multipart/form-data">
```

전송 예시는 구조가 복잡하다.

```
POST /upload
Content-Type: multipart/form-data; boundary=----abc123

------abc123
Content-Disposition: form-data; name="title"

hello
------abc123
Content-Disposition: form-data; name="file"; filename="a.png"
Content-Type: image/png

(binary data)
------abc123--
```

## 특징

* 파일 전송 가능
* binary 데이터 그대로 전송
* boundary 구분자로 파트가 나뉨
* 서버는 multipart 파서로 처리해야 함
  (Spring에서는 MultipartResolver와 MultipartFile 사용)

---

# 3. 거의 쓰이지 않는 타입: `text/plain`

명시적으로 설정할 수 있지만 특별한 이유가 없으면 사용하지 않는다.

```html
<form method="POST" enctype="text/plain">
```

전송 예:

```
POST /submit
Content-Type: text/plain

username=hello
password=1234
```

## 특징

* URL 인코딩이 없음
* 줄바꿈과 띄어쓰기가 그대로 들어가므로 파싱이 까다롭다
* 실무에서는 사실상 사용하지 않음

---

# 4. GET일 때는 Content-Type 없다

FORM에서 `method="GET"`이면 메시지 바디가 없기 때문에 Content-Type도 없다.

예:

```html
<form method="GET">
```

브라우저가 보낼 요청:

```
GET /search?keyword=hello&page=1
```

## 특징

* 모든 데이터가 쿼리 파라미터로 이동
* Body 없음 → Content-Type 헤더 없음

---

# 5. 정리 표

| enctype             | Content-Type                      | 파일 업로드 | 비고         |
| ------------------- | --------------------------------- | ------ | ---------- |
| (기본값)               | application/x-www-form-urlencoded | X      | 가장 많이 사용   |
| multipart/form-data | multipart/form-data               | O      | 파일 업로드용    |
| text/plain          | text/plain                        | X      | 특별한 경우 외 X |
| GET일 때              | 없음                                | X      | Body 없음    |

---

필요하면 이어서:

* Spring에서 multipart/form-data를 처리하는 원리
* FormData 객체와 multipart의 관계
* fetch/axios에서 multipart를 어떻게 전송하는지
  도 설명해줄게.
