CRUD 요청을 처리할 때 상태 코드를 어떤 순서로 “판단”해야 하는지는 단순한 규칙이 아니라 논리적 흐름(Flow) 문제야.
즉, 서버가 요청을 처리하면서 무엇을 먼저 검사하고,
그다음 무엇을 확인해야 하는지에 따라 상태 코드가 결정된다.

아래는 CRUD 공통으로 적용되는 상태 코드 판단 순서(가장 일반적인 흐름)를 쉽게 정리한 것이야.

---

# 전체 흐름(가장 기본적인 순서)

CRUD 어떤 요청이든 다음 순서가 가장 일반적이야.

1. 인증 확인
2. 권한 확인
3. 요청 검증(Validation)
4. 리소스 존재 여부 확인
5. 비즈니스 로직 제약 조건 확인
6. 정상 처리 후 적절한 상태 코드 반환

아래에서 각 단계에서 발생하는 상태 코드를 정리해줄게.

---

# 1. 인증(Authorization) 확인 → 401

### “사용자가 로그인 했는가?”

요청에 토큰/세션이 없는 경우:

* 401 Unauthorized

이 단계는 항상 최우선이야.
인증이 안 되어 있으면 그다음 로직으로 갈 수 없다.

---

# 2. 권한(Access Control) 확인 → 403

### “이 사용자가 이 리소스를 접근할 권한이 있는가?”

* 예: 일반 사용자가 관리자 페이지 접근
* 예: 다른 사용자의 정보 수정하려 할 때

→ 403 Forbidden

이 단계는 인증이 통과한 뒤에만 검사한다.

---

# 3. 요청 데이터 검증 → 400

### “사용자가 보낸 데이터 형식이 맞는가?”

* JSON 파싱 오류
* 필수 값 누락(name, email 등)
* 정규식 오류(email 형식 불일치)

→ 400 Bad Request

정상 요청이라고 보지 않으므로 처리 중단.

---

# 4. 리소스 존재 여부 → 404

### "요청한 데이터가 실제로 존재하는가?"

* GET /members/9999 → 없는 회원
* PUT/DELETE 요청인데 해당 리소스가 없음

→ 404 Not Found

이 단계는 조회/수정/삭제에서 항상 필요함.

Created(POST)에서는 존재 여부가 다르게 동작할 수도 있음.

---

# 5. 비즈니스 로직 제약 조건 → 409

### “서버 상태와 충돌하는가?”

* 이메일 중복 회원 생성
* 버전 충돌(동시 수정 문제)
* 삭제할 수 없는 상태(예: 외래키 제약)

→ 409 Conflict

요청 자체는 맞지만 현재 서버 상태와 충돌해서 실행 불가.

---

# 6. 성공 처리 → 2xx

이제 정상 처리되었을 때 알맞은 성공 코드 반환.

## Create(생성)

* 201 Created

## Read(조회)

* 200 OK

## Update(수정)

* 200 OK 또는 204 No Content

## Delete(삭제)

* 204 No Content

---

# 전체 순서를 그림처럼 요약하면

```
[1] 인증 실패? → 401
    ↓
[2] 권한 없음? → 403
    ↓
[3] 요청 데이터 오류? → 400
    ↓
[4] 리소스 없음? → 404
    ↓
[5] 상태 충돌? → 409
    ↓
[6] 처리 성공 → 200 / 201 / 204
```

이 순서가 가장 일반적이고 실무에서 거의 그대로 적용되는 흐름이야.

---

# 핵심 정리

* 상태 코드는 “CRUD의 결과”를 단계적으로 판단해 나가는 과정에서 결정된다.
* 어떤 CRUD든 위 순서대로 검사하는 것이 자연스러운 흐름이다.
* 모든 API가 이 순서를 강제하는 건 아니지만,
  정상적인 REST API라면 대부분 이 구조를 따른다.

---

원하면 다음도 설명해줄게:

* CRUD별로 실제 코드 예시(Spring MVC 기준)
* 잘못된 상태 코드 사용 예시
* REST API 에러 설계 Best Practice
* 프론트엔드가 상태 코드를 어떻게 해석해야 하는지

HTTP의 콘텐츠 협상(Content Negotiation)은 클라이언트와 서버가 “어떤 형태로 응답을 주고받을지”를 헤더를 이용해 자동으로 조율하는 메커니즘이다. 브라우저나 API 클라이언트가 서버에게 원하는 형식·언어·인코딩 등을 알려주면, 서버는 그 정보를 고려해 가장 적절한 응답을 돌려준다.

아래에서 핵심만 정확히 정리해줄게.

---

# 1. 협상 대상이 되는 대표 헤더

HTTP 협상은 크게 세 가지 종류가 있다.

## 1) 콘텐츠 형식 협상 (Media Type Negotiation)

어떤 포맷으로 응답을 받고 싶은가.

### `Accept`

클라이언트가 원하는 콘텐츠 타입을 우선순위(q값)와 함께 보낸다.

예:

```
Accept: text/html, application/json;q=0.9, */*;q=0.8
```

## 2) 언어 협상 (Language Negotiation)

어떤 언어의 응답을 원하는가.

### `Accept-Language`

예:

```
Accept-Language: ko-KR, en-US;q=0.8, en;q=0.6
```

## 3) 인코딩 협상 (Encoding Negotiation)

어떤 압축 방식을 지원하는가.

### `Accept-Encoding`

예:

```
Accept-Encoding: gzip, deflate, br
```

## 4) 캐릭터셋 협상

대부분 UTF-8을 기본으로 하지만 명시할 수도 있다.

### `Accept-Charset`

예:

```
Accept-Charset: utf-8, iso-8859-1;q=0.5
```

---

# 2. 서버는 어떻게 협상할까?

### 클라이언트 → 요청

클라이언트는 원하는 것들을 헤더로 보냄.

### 서버 → 응답

서버는 클라이언트의 요청을 참고해 가능한 것들 중에서 최적 매칭을 선택한다.

그리고 실제로 무엇을 선택했는지 헤더로 알려준다.

## 선택 결과 헤더들

### `Content-Type`

서버가 선택한 미디어 타입
예: `Content-Type: application/json; charset=utf-8`

### `Content-Language`

언어 결정
예: `Content-Language: ko-KR`

### `Content-Encoding`

압축 방식 결정
예: `Content-Encoding: gzip`

---

# 3. q값(품질 지수) 우선순위

Accept 계열 헤더에서 자주 나오는 개념.

```
Accept: text/html;q=1.0, application/json;q=0.9, */*;q=0.1
```

* 1.0이 가장 선호됨
* 서버는 높은 q값을 우선 적용
* 특정 타입을 피하고 싶다면 q=0으로 지정 가능

    * 예: `image/*;q=0`

---

# 4. 자동 협상이 중요한 이유

* 브라우저가 HTML을 원하면 HTML 제공
* 앱 클라이언트가 JSON을 원하면 JSON 제공
* 여러 언어를 지원할 때 올바른 언어 선택
* gzip 같은 인코딩으로 트래픽 절감
* REST API 설계에서 매우 핵심적

특히 API 서버는 Accept: application/json 여부를 기준으로 JSON/HTML을 다르게 내려주는 경우가 많다.

---

# 5. 간단한 예시

클라이언트 요청:

```
GET /products/1
Accept: application/json
Accept-Language: ko-KR, en;q=0.8
Accept-Encoding: gzip, br
```

서버 응답:

```
HTTP/1.1 200 OK
Content-Type: application/json
Content-Language: ko-KR
Content-Encoding: br

{"id":1, "name":"상품명"}
```

---

필요하다면 다음도 이어서 설명해줄 수 있어

* 협상이 실패하는 경우 (`406 Not Acceptable`)
* Spring MVC에서 어떻게 처리되는지
* 미디어 타입 우선순위 계산 로직
* Accept 헤더가 없을 때의 기본 동작
* 브라우저가 HTML을 요청하는 이유

원하는 방향 알려줘.

HTTP 협상이 실패하는 경우는 클라이언트가 요청한 조건을 서버가 만족시킬 수 없을 때 발생한다.
이때 서버는 406 Not Acceptable 응답을 보낼 수 있다.

아래에서 협상 실패가 발생하는 대표 상황을 정리해줄게.

---

# 1. 클라이언트가 요구한 미디어 타입을 서버가 전혀 지원하지 않을 때

요청:

```
Accept: application/xml
```

그런데 서버가 JSON, HTML만 생성할 수 있다면?

→ 서버는 클라이언트 선호 조건을 만족할 수 없어 406 Not Acceptable

---

# 2. q값 설정으로 인해 가능한 선택지가 모두 제거된 경우

예:

```
Accept: text/html;q=0, application/xml;q=0
```

즉, HTML도 XML도 받기 싫다는 의미.

그리고 서버는 HTML과 XML만 만들 수 있음.

→ 선택할 것이 없으므로 협상 실패 → 406

---

# 3. 언어 협상 실패 (Accept-Language)

요청:

```
Accept-Language: fr
```

서버는 영어(en), 한국어(ko)만 준비되어 있음.

→ 406이 될 수 있다.

다만 대부분의 서버/프레임워크는 언어 지원이 없으면 기본 언어로 fallback해주지, 실제로 406을 보내는 경우는 드물다.

---

# 4. 문자셋(Encoding / Charset) 협상 실패

예:

```
Accept-Charset: iso-8859-1
```

서버는 UTF-8만 제공 가능.

→ 조건 충족 불가 → 406

또는:

```
Accept-Encoding: gzip;q=0, br;q=0
```

압축은 반드시 요구하지만 둘 다 q=0이면 서버는 어떤 것도 사용 불가.

---

# 5. 클라이언트가 너무 좁은 조건만 허용하는 경우

예:

```
Accept: image/png
```

서버가 텍스트 기반 API만 제공한다면 이미지 생성은 불가능하므로 협상 실패.

---

# 6. 서버 정책에 따라 “생성 불가”라고 판단한 경우

REST API에서 심플한 예:

클라이언트:

```
Accept: text/html
```

서버는 API이며 JSON만 반환하도록 정책이 정해져 있음.

→ 협상 규칙상 HTML을 못 만든다고 판단하면 406을 줄 수 있다.

다만, Spring MVC 같은 프레임워크는 보통 HTML 뷰 리졸버가 없으면 자동으로 406을 반환한다.

---

# 협상 실패 시 실제 서버들은 어떻게 하는가?

엄밀하게는 406을 돌려야 하지만, 실제 세상에서는 다음과 같이 다르게 처리하는 경우가 많다.

* 브라우저는 잘못된 Accept를 보내도 서버는 HTML을 보내는 경우가 많음
* 언어 협상 실패 시, 대부분 기본 언어로 fallback
* 압축 실패 시, 압축 없이 평문 전달

즉, 실제로 406이 빈번하지는 않다.

프레임워크는 다음과 같은 상황에서 406을 많이 발생시킨다.

* Spring MVC: 미디어 타입에 맞는 HttpMessageConverter가 전혀 없을 때
* Express.js: `res.format()`이 선택할 포맷을 못 찾을 때

---

필요하면 다음도 추가로 설명해줄게.

* Spring MVC에서 406이 어떻게 발생하는 구조인지
* “Accept 헤더가 너무 광범위할 때”의 동작
* q값이 여러 포맷에 걸려 있을 때 충돌 처리
* 협상 실패를 피하는 API 설계 방법

Spring MVC에서 `HttpMessageConverter`는 기본적으로 프레임워크가 여러 타입을 자동 지원해 주지만,
추가 타입이 필요하면 개발자가 직접 등록할 수도 있다.
즉, 둘 다 가능하며 역할이 분리되어 있다.

아래에서 경계를 명확하게 설명해줄게.

---

# 1. 프레임워크가 기본으로 지원하는 것들

Spring MVC는 대부분의 웹 애플리케이션에서 필요한 미디어 타입을 자동으로 지원한다. 기본 내장 HttpMessageConverter 목록은 대략 다음과 같다:

## 문자열

* `StringHttpMessageConverter`
  → `text/plain`, `text/*`, `*/*`일 때 문자열 처리

## JSON

* `MappingJackson2HttpMessageConverter`
  → `application/json` 처리
  (Jackson 라이브러리가 classpath에 있으면 자동 등록)

## Form 데이터

* `FormHttpMessageConverter`
  → `application/x-www-form-urlencoded` 처리

## Multipart(파일 업로드)

* `MultipartHttpMessageConverter`
  → `multipart/form-data` 처리

## Byte Array

* `ByteArrayHttpMessageConverter`
  → 파일 다운로드 등에 사용

## Resource

* `ResourceHttpMessageConverter`

즉, 일반적인 JSON API, HTML 텍스트 반환, 파일 응답, 폼 전송 등은 개발자가 아무런 설정을 하지 않아도 자동으로 처리된다.

---

# 2. 개발자가 하는 역할

프레임워크 기본 지원으로 해결되지 않는 경우에 개발자가 추가한다.

## 1) 새로운 미디어 타입을 지원하고 싶을 때

예:

* CSV 응답: `text/csv`
* 이미지 생성: `image/png`
* YAML 응답: `application/x-yaml`
* 특정 바이트 프로토콜 처리

→ 개발자가 직접 커스텀 Converter 작성

```java
public class CsvMessageConverter extends AbstractHttpMessageConverter<MyData> {
    public CsvMessageConverter() {
        super(new MediaType("text", "csv"));
    }

    @Override
    protected MyData readInternal(...) { ... }

    @Override
    protected void writeInternal(MyData data, ...) { ... }
}
```

그리고 등록:

```java
@Override
public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(new CsvMessageConverter());
}
```

## 2) 기존 Converter의 우선순위를 바꾸고 싶을 때

예: JSON보다 XML을 더 우선 적용하고 싶음.

→ 커스터마이징으로 순서 조정

## 3) 메시지 변환 동작 방식을 변경하고 싶을 때

예:

* JSON 직렬화 규칙 변경
* SnakeCase 적용
* Null 필드 제거
  → Jackson 설정 변경 → Spring 자동 반영됨

---

# 3. 협상 과정에서 프레임워크가 하는 역할

프레임워크(Spring)는 다음을 자동으로 해준다.

1. 클라이언트의 `Accept` 헤더 읽기
2. 서버 측 리소스 반환 타입 확인 (`@ResponseBody` 또는 `ResponseEntity` 등)
3. 등록된 모든 HttpMessageConverter 목록을 순서대로 탐색
4. “지원 가능한 미디어 타입”을 가진 Converter 선택
5. 선택된 Converter로 객체를 직렬화해 응답 전달

즉, 협상 로직 자체는 전부 Spring이 수행한다.

---

# 4. 경계 정리

## 프레임워크(Spring)가 하는 역할

* 기본 미디어 타입 자동 지원
* 기본 HttpMessageConverter 자동 등록
* Accept 헤더 기반 미디어 타입 협상
* Converter 탐색 및 적절한 선택
* JSON/XML 직렬화(Spring Boot는 기본 최적 설정 제공)

## 개발자가 하는 역할

* 기본 미디어 타입 외의 새로운 타입 지원
* 기존 Converter의 동작 변경 또는 우선순위 조절
* 커스텀 직렬화/역직렬화 로직 작성
* 잘못된 Accept 요청을 어떻게 처리할지 정책 결정(예: 406)

---

# 5. 결론

Spring MVC의 미디어 타입 처리 관련 책임은 다음처럼 나뉜다.

* 일반적인 웹·API 개발 → 거의 전부 자동처리 (개발자 설정 필요 없음)
* 특수 포맷 응답/요청, 커스텀 처리 → 개발자가 Converter 작성

즉,
기본적인 JSON API는 개발자가 HttpMessageConverter를 건드릴 일이 없다.
필요할 때만 추가하거나 덮어쓰는 방식이다.

---

원한다면 다음도 더 자세히 설명해줄 수 있어:

* Spring이 Converter를 선택하는 구체적인 알고리즘
* Accept 헤더와 @RequestMapping의 produces 관계
* Converter가 여러 개 있을 때 충돌 해결 방식
* ‘406 Not Acceptable’이 발생하는 정확한 조건

어떤 방향으로 이어 설명할까?

결론부터 말하면,

일반적인 웹/REST API 개발에서는 HttpMessageConverter에 넣을 “입력/출력 포맷”을 개발자가 직접 작성하지 않는다.
즉, 개발자가 직접 Converter를 만드는 경우는 특수한 상황뿐이며, 대부분의 경우 프레임워크가 제공하는 Converter를 그대로 사용한다.

아래에서 왜 그런지 구체적으로 설명해줄게.

---

# 1. 보통은 개발자가 HttpMessageConverter를 직접 작성하지 않는다

Spring MVC 기본 개발 패턴에서,

* JSON 요청 → 자바 객체
* 자바 객체 → JSON 응답
* 문자열 응답
* 폼 데이터 처리
* 파일 업로드/다운로드

이 모든 것은 기본 제공 Converter로 자동 처리된다.

개발자가 신경 쓸 부분은

```java
@RequestBody MyDto dto
@ResponseBody MyResponse res
```

정도이며, Converter를 작성하지 않는다.

즉, 대부분의 개발자는 Converter라는 개념을 몰라도 개발이 잘 된다.

---

# 2. 개발자가 Converter를 작성하는 경우는 언제인가?

아래 같은 특수한 요구사항이 생길 때만 개발자가 HttpMessageConverter를 직접 만든다.

## 1) JSON 말고 다른 포맷을 지원해야 할 때

* CSV
* YAML
* 특정 이미지 포맷
* 고유한 바이너리 프로토콜
* IoT 디바이스와 통신하는 특수 구조

이런 경우에는 Spring 기본 Converter가 없으므로 직접 Converter를 작성해야 한다.

## 2) JSON 직렬화 규칙을 아주 특이하게 바꿔야 할 때

예:

* JSON을 Base64로 인코딩해서 보내야 한다
* 필드 암호화가 필요하다
* 특정 조건에 따라 JSON 출력 필드를 동적으로 생성해야 한다

이럴 때는 `MappingJackson2HttpMessageConverter` 설정을 커스터마이즈하거나 별도 Converter를 작성하기도 한다.

## 3) 콘텐츠 타입이 독자적인 경우

예:

```
Content-Type: application/x.my-company.custom-protocol
```

→ 개발자가 이 타입을 해석하는 Converter를 만들어야 한다.

---

# 3. 개발자 입장에서 일반적으로 작성하는가?

정리하면:

### 일반적인 Spring Boot REST API

* Converter 작성 → 안 함
* 필요 없음
* 프레임워크가 모두 처리

### 특수한 요구사항이 많은 프로젝트

* Converter 작성 → 가끔 필요
* 데이터 포맷이 표준이 아닐 때
* 또는 회사/제품에서 독자 포맷을 사용할 때

즉, 전체 개발자 중 대부분은 작성할 일이 없다,
하지만 기업용 시스템이나 특화 시스템에서는 꽤 작성될 수 있다 정도로 보면 된다.

---

# 4. 실제 산업에서의 비율 감각

현장에서 Converter를 작성하는 빈도는 대략 다음처럼 볼 수 있다:

* 일반적인 웹 서비스/REST 서비스: 0%
* JSON 기반 API 중심 서비스: 0~5% (거의 없음)
* 특수 포맷/금융/IoT/레거시 시스템 연동: 10~20%
* 산업용 장비/프로토콜 시스템: 50% 이상

일반적인 웹 개발에서는 거의 작성하지 않는다는 의미다.

---

필요하다면 다음도 이어 설명할 수 있어:

* Converter가 실제로 동작하는 내부 과정
* Spring이 어떤 기준으로 어떤 Converter를 선택하는지
* `produces`, `consumes`와 MediaType 협상 관계
* DTO 바인딩과 Converter의 차이

원하는 주제를 이어서 말해줘.

`@RequestBody MyDto dto` 와 `@ResponseBody MyResponse res`는
Spring MVC가 HttpMessageConverter를 사용하도록 트리거하는 핵심 지점이다.

즉, 이것들이 HTTP 메시지(바디) ↔ 자바 객체 변환을 자동으로 일으키는 장치다.

아주 구체적으로 설명해줄게.

---

# 1. `@RequestBody MyDto dto` 는 무엇인가?

## 역할

HTTP 요청 바디(Body)에 있는 데이터를 → 자바 객체(dto)로 변환한다.

즉,

* 클라이언트가 JSON을 보내면
* Spring은 `MappingJackson2HttpMessageConverter`를 사용해서
* JSON을 `MyDto` 객체로 역직렬화(deserialize)한다.

예:

```http
POST /members
Content-Type: application/json

{
  "name": "kim",
  "age": 20
}
```

컨트롤러:

```java
@PostMapping("/members")
public String create(@RequestBody MyDto dto) {
    // dto.name = "kim"
    // dto.age = 20
    return "ok";
}
```

여기서 HttpMessageConverter가 자동 실행되는 지점이 바로 `@RequestBody`.

---

# 2. `@ResponseBody MyResponse res` 는 무엇인가?

## 역할

자바 객체(res) → HTTP 응답 바디로 변환한다.

즉,

* 컨트롤러가 `MyResponse` 객체를 반환하면
* Spring이 HttpMessageConverter를 이용해서
* 그 객체를 JSON, XML, String 등으로 직렬화(serialize)해서 클라이언트에게 보낸다.

예:

```java
@GetMapping("/members/{id}")
@ResponseBody
public MyResponse detail(@PathVariable Long id) {
    return new MyResponse(id, "kim");
}
```

클라이언트가 받는 응답은 이런 JSON이 된다:

```json
{
  "id": 1,
  "name": "kim"
}
```

이때 JSON 변환을 하는 주체가 바로 `MappingJackson2HttpMessageConverter`.

---

# 3. 한마디로 요약하면

## @RequestBody

HTTP 요청바디 → 자바 객체 변환
(역직렬화)

## @ResponseBody

자바 객체 → HTTP 응답바디
(직렬화)

이 두 개는 모두 HttpMessageConverter를 자동 실행시킨다는 공통점이 있다.

---

# 4. 그럼 객체가 아닌 문자열을 반환하면?

예:

```java
@ResponseBody
public String hello() {
    return "hello";
}
```

이건 `StringHttpMessageConverter`가 처리해서 그대로 텍스트 응답이 된다.

---

# 5. 그럼 @ResponseBody 를 빼면?

Spring MVC는 두 가지 모드가 있다.

### 1) 뷰 렌더링 모드(@Controller)

* 문자열을 반환하면 템플릿 이름으로 인식 (예: Thymeleaf, JSP)
* 객체 반환하면 Model에 넣어서 View로 전달

### 2) HTTP 메시지 직접 반환 모드(@ResponseBody 또는 @RestController)

* 객체를 반환하면 JSON 등으로 변환

즉,
`@ResponseBody`가 붙으면 ViewResolver 대신 HttpMessageConverter가 동작한다.

---

# 6. 실무적 비유

* `@RequestBody`: “JSON 주세요” → 객체로 변환해서 메서드에 넣어줌
* `@ResponseBody`: “객체 받았습니다” → JSON 등으로 다시 변환해서 응답함

Spring이 자동으로 처리하므로 개발자는 데이터 전송 로직을 작성할 필요가 없다.

---

필요하면 다음도 계속 설명해줄게.

* @ModelAttribute와 @RequestBody 차이
* 파라미터 바인딩 vs 메시지 변환의 차이
* consumes/produces와의 관계
* RestController가 @ResponseBody를 내장하는 방식

어떤 쪽이 궁금해?

- 청크 전송은 메시지 바디를 여러 덩어리(chunk)로 나눠 보내며, 각 덩어리에 길이 정보가 포함되므로 전체 길이를 미리 알 필요가 없습니다. Content-Length와 함께 사용하면 오류가 발생합니다.