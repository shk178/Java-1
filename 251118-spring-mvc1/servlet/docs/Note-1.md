`logging.level.org.apache.coyote.http11=debug` 은 Spring Boot의 로깅 설정 중 하나로, 다음을 의미한다.

## 무엇을 의미하나

* `logging.level.` : 특정 패키지나 클래스에 대한 로그 레벨을 지정한다.
* `org.apache.coyote.http11` : Tomcat 내부에서 HTTP/1.1 커넥터 처리 로직을 담당하는 패키지다.

    * 요청/응답을 실제로 읽고 쓰는 커넥터(Connector) 계층이 여기 포함된다.
* `debug` : 로그 레벨을 DEBUG로 설정한다.

즉,
Tomcat의 HTTP/1.1 커넥터 레벨에서 발생하는 내부 동작을 DEBUG 로그로 출력하라 는 뜻이다.

## 어떤 로그가 나오나

`debug` 레벨을 켜면 다음과 같은 정보가 출력될 수 있다.

* 소켓 연결 수립/해제 정보
* 요청 바이트를 읽는 과정
* keep-alive 처리 흐름
* body 읽기, chunk 처리, 버퍼 읽기/쓰기 과정
* 응답이 만들어지고 flush되는 과정
* 커넥션 재사용 여부

개발 시 요청/응답이 어떻게 Tomcat 레벨에서 처리되는지 확인할 때 유용하다.

## 언제 쓰면 좋나

* 요청이 이상하게 끊기는 문제가 있을 때
* 특정 클라이언트와의 연결 문제 원인 분석할 때
* Chunked Encoding, Keep-Alive, Timeout 같은 이슈 디버깅할 때

## 주의

* DEBUG 출력량이 매우 많다.
* 운영 환경에서는 거의 사용하지 않는다.

필요하면 어떤 로그가 실제로 나오는지도 예시로 보여줄까?

Tomcat의 `org.apache.coyote.http11` 패키지를 `debug` 로 설정하면 실제로는 다음과 비슷한 로그들이 출력된다.
(아래는 실제 Tomcat DEBUG 로그 패턴을 기반으로 단순화한 예시)

---

# 1. 커넥션 생성/종료

클라이언트와 연결이 생기고 종료될 때 기록된다.

```
DEBUG org.apache.coyote.http11.Http11NioProtocol - Accepted connection [0:0:0:0:0:0:0:1:54321] to /127.0.0.1:8080
```

```
DEBUG org.apache.coyote.http11.Http11Processor - Connection [1], Request processing completed
```

---

# 2. 요청 헤더 파싱 과정

클라이언트가 보낸 요청의 헤더를 Tomcat이 읽으며 출력한다.

```
DEBUG org.apache.coyote.http11.Http11InputBuffer - Received [GET /hello HTTP/1.1]
DEBUG org.apache.coyote.http11.Http11InputBuffer - Header host = localhost:8080
DEBUG org.apache.coyote.http11.Http11InputBuffer - Header user-agent = curl/7.79.1
```

---

# 3. 요청 본문(body) 읽기

POST 요청에서 body가 어떻게 읽히는지 확인할 수 있다.

```
DEBUG org.apache.coyote.http11.Http11InputBuffer - Content-Length: 27
DEBUG org.apache.coyote.http11.Http11InputBuffer - Reading 27 bytes of request body
DEBUG org.apache.coyote.http11.Http11InputBuffer - Finished reading body
```

Chunked 요청인 경우에는 chunk 단위 로그가 찍히기도 한다.

---

# 4. 응답 생성 및 쓰기

컨트롤러가 반환한 응답을 네트워크로 흘려보내는 순간이 기록된다.

```
DEBUG org.apache.coyote.http11.Http11OutputBuffer - Sending headers
DEBUG org.apache.coyote.http11.Http11OutputBuffer - Writing body using chunked encoding
DEBUG org.apache.coyote.http11.Http11OutputBuffer - Flushing output buffer
```

---

# 5. Keep-Alive 처리

이 커넥션을 재사용할 수 있는지 여부도 기록된다.

```
DEBUG org.apache.coyote.http11.Http11Processor - Keep-Alive: timeout=5000, max=100
DEBUG org.apache.coyote.http11.Http11Processor - Connection will be kept alive
```

---

# 6. 커넥션 종료 시점

타임아웃이나 종료 조건을 만나면 다음과 같은 로그가 남는다.

```
DEBUG org.apache.coyote.http11.Http11NioProtocol - Closing socket due to timeout
```

---

# 요약하자면

`org.apache.coyote.http11` 의 DEBUG 로그는 Tomcat이 실제로 요청을 읽고 응답을 쓰는 가장 낮은 수준의 흐름을 그대로 보여준다.

* 요청/응답 라인
* 헤더 파싱
* body 읽기
* chunk 처리
* keep-alive/재사용 처리
* 소켓 열고 닫기

등을 모두 확인할 수 있기 때문에, 네트워크 레벨 원인 분석에 매우 유용하다.

원한다면

* 특정 케이스(GET, POST, chunked 등)별로 더 상세한 실제 로그 예시
* 로그를 줄이거나 필터링하는 방법
  도 설명해줄 수 있다.

아래는 출력된 로그를 의미 단위로 끊어서 쉽게 해석한 내용이다. 코드가 무엇을 찍는지, 그리고 각 값이 왜 그렇게 나오는지를 함께 설명한다.

---

# 1. 요청 시작 줄 (Start Line)

```
req.getMethod() = GET
```

HTTP 요청 방식. 브라우저가 `/req-head` 주소를 GET 요청으로 호출했기 때문에 GET.

```
req.getProtocol() = HTTP/1.1
```

HTTP 프로토콜 버전. 일반적으로 브라우저는 HTTP/1.1 또는 HTTP/2를 사용한다.

```
req.getScheme() = http
```

요청이 들어온 스킴. HTTPS 가 아닌 HTTP 요청임을 의미.

```
req.getRequestURL() = http://localhost:8080/req-head
```

전체 요청 URL(쿼리 파라미터 제외).

```
req.getRequestURI() = /req-head
```

도메인 뒤의 경로만 출력. `/req-head`.

```
req.getQueryString() = null
```

GET 요청에 쿼리 파라미터가 없었기 때문에 null.

```
req.isSecure() = false
```

HTTPS 요청이 아니므로 false.

---

# 2. 요청 헤더 출력

브라우저가 서버에 보낸 HTTP 헤더 목록.

```
host: localhost:8080
```

Host 헤더. 현재 요청이 어디로 들어왔는지 나타낸다.

```
connection: keep-alive
```

TCP 연결을 유지하려는 의도. 일반적인 브라우저 기본값.

```
sec-ch-ua ... sec 관련 헤더들
```

브라우저 정보(브랜드, 플랫폼, 보안 관련 헤더들).
Chrome/Brave가 보내는 일반적인 Client Hint 헤더들.

```
upgrade-insecure-requests: 1
```

브라우저가 가능한 경우 HTTPS를 선호한다는 의미.

```
user-agent: Mozilla/5.0 ...
```

요청을 보낸 클라이언트 프로그램 정보 (브라우저 정보).

```
accept: text/html,application/xhtml+xml ...
```

브라우저가 어떤 데이터를 받을 수 있는지 정의.

```
sec-gpc: 1
```

GPC(Global Privacy Control) — 사용자의 추적 비동의 의사.

```
accept-language: ko-KR,ko;q=0.8
```

우선 언어 설정: 한국어(대한민국), 한국어.

```
sec-fetch-site: same-origin
sec-fetch-mode: navigate
sec-fetch-user: ?1
sec-fetch-dest: document
```

Fetch Metadata Request Headers — CSRF 보호나 Context 파악을 위한 헤더.

```
referer: http://localhost:8080/
```

이전 페이지 주소. `/` 페이지에서 `/req-head`로 이동함.

```
accept-encoding: gzip, deflate, br, zstd
```

서버가 데이터를 압축해서 보내도 브라우저가 처리 가능하다는 의미.

---

# 3. 헤더 유틸 출력 (`printHeaderUtil`)

```
req.getServerName() = localhost
```

서버가 구동 중인 이름 (Host header 또는 서버 설정 기반).

```
req.getServerPort() = 8080
```

서버가 바인딩된 포트.

```
req.getLocales() = java.util.Collections$3@63edc606
```

클라이언트가 지원 언어 목록의 Enumeration 객체 자체.
실제 내용은 아래에서 iterator 로 출력됨.

```
locale = ko_KR
locale = ko
```

브라우저의 언어 우선순위 목록. `ko-KR` → `ko`.

```
req.getLocale() = ko_KR
```

가장 우선순위 높은 언어.

```
req.getContentType() = null
```

GET 요청이라서 본문(body)이 없고, Content-Type 헤더도 없음.

```
req.getContentLength() = -1
```

GET 요청이므로 body 길이 없음. 없는 경우 -1.

```
req.getCharacterEncoding() = UTF-8
```

서블릿에서 기본으로 설정된 요청 인코딩.

---

# 4. 기타(request 관련 서버/클라이언트 정보)

```
req.getRemoteHost() = 0:0:0:0:0:0:0:1
req.getRemoteAddr() = 0:0:0:0:0:0:0:1
```

요청을 보낸 클라이언트 주소. IPv6 로 표시된 localhost.
원래 IPv4 localhost는 127.0.0.1.

```
req.getRemotePort() = 53836
```

클라이언트(브라우저)가 서버로 요청을 보낼 때 사용한 임시 포트(ephemeral port).

```
req.getLocalName() = 0:0:0:0:0:0:0:1
req.getLocalAddr() = 0:0:0:0:0:0:0:1
```

서버 자신의 주소. 로컬 환경이라 IPv6 localhost.

```
req.getLocalPort() = 8080
```

서버가 리스닝하고 있는 포트.

---

# 전체 요약

이 출력은 브라우저가 `/req-head`로 GET 요청을 보냈을 때 서버가 요청 정보를 그대로 찍은 결과다.
헤더, 클라이언트 정보, 서버 정보, 요청 메타데이터 등이 모두 정상적으로 나타난 상태이며, 로컬 개발환경(localhost) 특성 때문에 IPv6 주소가 많이 보이고 GET 요청 특성상 Content-Type/Content-Length는 비어 있다.

필요하면 각 메서드가 언제, 왜, 어떤 상황에서 쓰이는지도 더 자세히 설명해줄게.

`req.getParameter(paramName)`이 여러 값을 가진 파라미터를 하나만 반환하기 때문이다.
즉, `nameA`가 `valueA`, `valueC` 두 개를 갖고 있어도 getParameter는 첫 번째 값만 반환한다.

---

# 1. 왜 valueC가 출력되지 않는가

요청 주소:

```
/req-param?nameA=valueA&nameB=valueB&nameA=valueC
```

여기서 `nameA` 파라미터는 두 번 등장했다.

```
nameA = valueA
nameA = valueC
```

Servlet 스펙에서 `req.getParameter(name)`은 다음과 같다:

* 여러 값이 있을 경우 → 첫 번째 값만 반환한다
* 나머지는 `getParameterValues(name)`로만 조회 가능하다

그래서 출력은 아래처럼 된다:

```
paramName = nameA, req.getParameter(nameA) = valueA
paramName = nameB, req.getParameter(nameB) = valueB
```

`valueC`는 무시된 게 아니라, `getParameter`가 의도적으로 단일 값만 제공하는 API이기 때문에 나오지 않은 것이다.

---

# 2. valueC도 출력하려면?

모든 값을 출력하려면 `req.getParameterValues(paramName)`을 사용해야 한다.

예시:

```java
req.getParameterNames().asIterator()
        .forEachRemaining(paramName -> {
            String[] values = req.getParameterValues(paramName);
            System.out.println("paramName = " + paramName);
            for (String value : values) {
                System.out.println("value = " + value);
            }
        });
```

출력:

```
paramName = nameA
value = valueA
value = valueC

paramName = nameB
value = valueB
```

---

# 3. 정리

| 메서드                            | 설명                       |
| ------------------------------ | ------------------------ |
| `req.getParameter(name)`       | 여러 값이 있어도 첫 번째 값만 반환 |
| `req.getParameterValues(name)` | 모든 값을 배열로 반환             |
| `req.getParameterNames()`      | 모든 파라미터 이름을 가져옴          |

---

필요하면 `getParameterMap()`을 써서 키-값 목록 전체를 출력하는 방법도 보여줄게.

`getParameterMap()`을 사용하면, 모든 파라미터 이름과 그 파라미터에 대응하는 모든 값들을 한 번에 확인할 수 있다.
키는 파라미터 이름(String), 값은 문자열 배열(String[])이다.

아래는 전체 파라미터를 출력하는 예시 코드다.

---

# 1. getParameterMap() 사용 예시

```java
@Override
protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    Map<String, String[]> paramMap = req.getParameterMap();

    for (String key : paramMap.keySet()) {
        System.out.println("paramName = " + key);

        String[] values = paramMap.get(key);
        for (String value : values) {
            System.out.println("value = " + value);
        }
        System.out.println();
    }
}
```

---

# 2. 출력 예시 (`/req-param?nameA=valueA&nameB=valueB&nameA=valueC`)

```
paramName = nameA
value = valueA
value = valueC

paramName = nameB
value = valueB
```

---

# 3. 정리

* `getParameter(name)` → 단일 값 조회
* `getParameterValues(name)` → 여러 값 배열로 조회
* `getParameterMap()` → 모든 파라미터를 한 번에 조회 (Map<String, String[]>)

`getParameterMap()`을 사용할 때 좋은 점은, 파라미터들의 구조를 전체적으로 보고 싶을 때 직관적이라는 것이다.

다른 방식(람다 스타일)으로도 원하면 코드 예제를 더 알려줄게.

아래 코드가 처리하는 것은 요청 바디(raw body)이며, Content-Type에 따라 처리 방식이 달라집니다. 따라서 요청을 보낼 때 두 가지 방식(text/plain / application/json) 중 하나로 요청을 만들면 됩니다.

아래에 각각 어떻게 요청을 보내야 하는지 정리해줄게.

---

# 1. text/plain 요청 보내기

## 브라우저 주소창으로는 불가능

브라우저 주소창 GET 요청은 바디를 보낼 수 없기 때문에 Postman, curl 또는 HTML form 등을 사용해야 한다.

## Postman 사용 예

* Method: POST
* URL: [http://localhost:8080/req-body](http://localhost:8080/req-body)
* Headers:

    * Content-Type: text/plain
* Body (raw):

```
hello world
```

## curl 예

```
curl -X POST http://localhost:8080/req-body \
  -H "Content-Type: text/plain" \
  -d "hello world"
```

서버 출력:

```
req.getContentType() = text/plain
msgBody = hello world
```

---

# 2. application/json 요청 보내기

## Postman 사용 예

* Method: POST
* URL: [http://localhost:8080/req-body](http://localhost:8080/req-body)
* Headers:

    * Content-Type: application/json
* Body (raw):

```
{
  "username": "spring",
  "age": 20
}
```

서버 출력:

```
req.getContentType() = application/json
msgBody = {"username":"spring","age":20}
helloData = HelloData{username='spring', age=20}
```

---

# 3. curl 예

```
curl -X POST http://localhost:8080/req-body \
  -H "Content-Type: application/json" \
  -d '{"username": "spring", "age": 20}'
```

---

# 4. HTML form으로는 JSON이나 text/plain 직접 생성 불가

form은 기본적으로 `application/x-www-form-urlencoded`로 전송됨.
따라서 위 코드와 맞지 않음.

---

# 정리

| Content-Type     | 바디 예시                          | curl/Postman에서 보내는 방식 |
| ---------------- | ------------------------------ | --------------------- |
| text/plain       | hello world                    | raw text              |
| application/json | {"username":"spring","age":20} | raw JSON              |

원하는 방식으로 Postman이나 curl로 요청 보내면 코드가 정상적으로 동작해.

웹 브라우저에서 “클릭해서 보내는 요청”은 기본적으로 URL에 파라미터를 붙이거나, HTML form을 제출하거나, 자바스크립트로 AJAX 요청을 보내도록 만들어져 있다.
하지만 여기서 중요한 점은:

브라우저는 기본적으로 text/plain이나 application/json 바디를 자동으로 보내지 않는다.
(특히 POST JSON은 자바스크립트를 써야만 가능하다.)

아래에서 웹에서 흔히 사용하는 각 방식이 어떻게 요청을 만드는지 설명해줄게.

---

# 1. 링크 클릭 (`<a href="...">`)

링크를 클릭하면 GET 요청이 전송되고, 바디를 담을 수 없다.

예

```
<a href="/hello?username=kim&age=20">클릭</a>
```

→ 실제 요청

```
GET /hello?username=kim&age=20
Content-Type 없음
Body 없음
```

---

# 2. HTML form 제출

form은 기본적으로 다음 두 가지 방식만 자동 지원한다.

### (1) GET 방식

```
<form action="/req-body" method="get">
   <input name="username" value="kim">
   <input name="age" value="20">
   <button>전송</button>
</form>
```

→ 요청

```
GET /req-body?username=kim&age=20
```

### (2) POST + application/x-www-form-urlencoded

```
<form action="/req-body" method="post">
   <input name="username" value="kim">
   <input name="age" value="20">
   <button>전송</button>
</form>
```

→ 요청

```
POST /req-body
Content-Type: application/x-www-form-urlencoded
Body = username=kim&age=20
```

### (3) POST + multipart/form-data (파일 업로드)

form에서 파일을 업로드하면 multipart로 전송된다.

---

# 3. 웹 브라우저에서 JSON 전송은 기본적으로 불가능

브라우저 기본 기능만으로는 아래 같은 JSON POST 요청을 만들 수 없다.

```
POST /req-body
Content-Type: application/json
Body: {"username": "kim", "age": 20}
```

이걸 하려면 자바스크립트(AJAX, fetch API)가 반드시 필요하다.

---

# 4. 브라우저에서 JSON 보내려면 어떻게 해야 하나?

### (1) HTML + JavaScript(fetch) 사용

아래처럼 코드 작성해야 한다.

```
<button id="send">전송</button>

<script>
document.getElementById("send").addEventListener("click", () => {
    fetch('/req-body', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: 'kim',
            age: 20
        })
    });
});
</script>
```

→ 브라우저 버튼을 클릭하면 JSON POST 요청이 전송됨.

---

# 5. 그렇다면 일반 웹 사이트는 어떻게 JSON을 보내는가?

현대 웹은 대부분 백엔드 API 서버에 JSON을 보내기 위해 JavaScript(AJAX)를 사용한다.

예:

* 로그인 버튼 클릭 → JS에서 `/login`으로 JSON POST
* 댓글 작성 버튼 클릭 → JS에서 `/comment`로 JSON POST
* React/Vue 앱 → 거의 모든 요청을 JSON으로 보냄

form만으로는 JSON을 보낼 수 없기 때문에 실제 서비스들은 모두 JS로 요청을 만든다.

---

# 핵심 요약

| 방식                      | JSON/텍스트 바디 전송 가능 여부          |
| ----------------------- | ----------------------------- |
| 링크 클릭                   | 불가능                           |
| HTML form               | JSON 불가능, 특정 Content-Type만 가능 |
| JavaScript(fetch, AJAX) | JSON 가능                       |
| Postman/curl            | 당연히 가능                        |

---

원하면 여기 코드를 직접 테스트할 수 있는 HTML + JS 페이지 예제도 만들어줄까?

가능하다. 하지만 서버사이드 렌더링(SSR)이 가능하다는 말과
브라우저가 서버로 JSON 같은 복잡한 바디 요청을 보낼 수 있다는 말은 완전히 다른 개념이다.

아래에서 둘의 차이를 명확히 설명해줄게.

---

# 1. SSR은 “HTML을 서버에서 만들어 보내는 방식”이다

SSR(Server-Side Rendering)은 다음 과정을 의미한다.

1. 브라우저가 GET 요청을 보냄
   (대부분 `GET /page?user=kim` 형태)
2. 서버가 HTML을 생성해서 응답으로 돌려줌
3. 브라우저가 HTML을 바로 렌더링함

여기서 중요한 점은:

* SSR은 요청을 어떻게 보내는지와는 상관이 없다
* SSR은 서버가 HTML을 만들어 보내는 방식 이야기일 뿐이다
* 요청은 그냥 기본 GET 요청으로 충분하다

즉, SSR은 단순히 “응답을 HTML로 받는다”는 개념이지
“브라우저가 어떤 요청을 만들 수 있느냐”와는 별개이다.

---

# 2. 브라우저가 JS 없이 보낼 수 있는 요청은 매우 제한적

브라우저 기본 기능만 사용하면 다음 세 가지뿐이다.

## (1) GET 요청 (주소창/링크/form GET)

* 바디 없음
* URL?파라미터=값 형태만 가능

## (2) POST + application/x-www-form-urlencoded (form)

* HTML form 기본 방식

## (3) POST + multipart/form-data (파일 업로드)

* 파일 첨부 form에서 자동 생성됨

이 세 가지는 브라우저가 HTML만으로 만들 수 있는 모든 요청 방식이다.
이 안에는 `application/json`이나 `text/plain` 같은 바디 전송이 없다.

---

# 3. SSR을 사용해도 요청은 form이나 GET만 사용한다

SSR 프로젝트들(스프링 MVC, JSP, 타임리프, 레일즈, 장고 템플릿 등)은 기본적으로 이렇게 구조화되어 있다.

* 페이지 요청 → GET
* 회원가입/로그인 같은 작업 → form POST (urlencoded)
* 파일 업로드 → form POST (multipart)

SSR은 응답을 서버에서 렌더링할 뿐,
요청 방식은 기본 form 방식만 사용한다.

다시 말해:

SSR ∼= “HTML을 서버에서 만든다”
요청 형식 ∼= “브라우저가 form/GET으로 요청 보낸다”

두 개는 별개의 차원이다.

---

# 4. JSON 요청은 JS를 사용할 때에만 가능

브라우저는 기본적으로 JSON POST 같은 요청 형식을 만들 수 없기 때문에, JSON API를 호출하려면 반드시 JS(fetch/AJAX)가 필요하다.

즉:

* SSR → HTML 렌더링 방법에 대한 이야기
* JSON POST 요청 → 브라우저 요청 능력에 대한 이야기

이 둘은 서로 연관이 없다.

---

# 요약

SSR이 가능하다는 말은 서버가 HTML을 만들어줄 수 있다는 뜻이지,
브라우저가 JSON/APIs 요청을 JS 없이 보낼 수 있다는 뜻이 아니다.

브라우저가 JS 없이 자동으로 만드는 요청은 다음뿐이다.

* GET
* POST(application/x-www-form-urlencoded)
* POST(multipart/form-data)

JSON, text/plain POST 요청은 JS가 있어야만 가능하다.

---

필요하다면 SSR 기반 웹사이트에서 JSON API를 호출하는 구조를 그림으로 정리해서 보여줄까?

아래 코드가 왜 복잡하게 느껴지는지, HTTP 응답 메시지 구조와 서블릿 API가 어떤 순서로 응답을 구성하는지를 먼저 이해하면 훨씬 깔끔해진다.
지금은 내용이 섞여 있어서 헷갈리는 게 자연스럽다.

---

# 1. HTTP 응답 메시지의 구성

HTTP 응답은 항상 다음 3단계 구조를 가진다:

1. Status Line
2. Headers
3. Body

예시:

```
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8
Set-Cookie: myCookie=good; Max-Age=600

hello kim
```

서블릿에서 당신의 코드가 하는 일도 결국 이 구조를 채우는 것뿐이다.

---

# 2. 당신의 코드가 실제로 하는 작업을 구조적으로 재정리하면

### ① Status Line 설정

```java
resp.setStatus(HttpServletResponse.SC_OK);
```

### ② Header 설정

```java
resp.setHeader("Content-Type", "text/plain;charset=utf-8");
resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
resp.setHeader("Pragma", "no-cache");
resp.setHeader("my-header", "hello");
```

### ③ Header 유틸 메서드 (header를 좀 더 편하게 설정하는 API)

```java
content(resp); // Content-Type, CharacterEncoding 설정
cookie(resp);  // Set-Cookie 헤더 설정
redirect(resp); // Location 헤더 설정 + 302 상태 코드 변경
```

### ④ Body 출력

```java
PrintWriter writer = resp.getWriter();
writer.println("hello kim");
```

---

# 3. 혼란이 생기는 이유

가장 큰 문제는 아래 두 가지다.

## 문제 1) "헤더"와 "유틸로 설정하는 헤더"가 섞여 있다

* 직접 헤더를 넣는 부분이 있고
* `content()`, `cookie()`, `redirect()` 같은 유틸 함수에서도 헤더를 넣는다

즉, 헤더가 여기저기 분산되어 있어 전체 응답 구조가 한눈에 안 보이는 것이 헷갈리는 이유다.

## 문제 2) `redirect()`가 응답의 흐름을 바꾼다

`sendRedirect()`는 내부적으로 이렇게 한다:

```
Status: 302
Location: /basic/hello-form.html
```

그리고 정상적으로 브라우저는 이 응답을 받으면 바디를 무시한다
그런데 현재 코드에서는 redirect 이후에 body도 쓰기 때문에 더 혼란스럽다.

---

# 4. 정리된 형태 (이해하기 쉬운 구조)

서블릿 응답을 처리할 때는 이 순서대로 작성하는 것이 가장 깔끔하다

### 1) Status

### 2) Headers

### 3) Cookies

### 4) Body OR Redirect (둘 중 하나만)

이 구조대로 재구성하면 다음처럼 정돈된다.

---

### 예: Redirect 아닌 일반 응답

```java
resp.setStatus(HttpServletResponse.SC_OK);

// Header
resp.setContentType("text/plain");
resp.setCharacterEncoding("utf-8");
resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
resp.setHeader("Pragma", "no-cache");

// Cookie
Cookie cookie = new Cookie("myCookie", "good");
cookie.setMaxAge(600);
resp.addCookie(cookie);

// Body
PrintWriter writer = resp.getWriter();
writer.println("hello kim");
```

---

### 예: Redirect 응답만 보낼 경우

```java
resp.sendRedirect("/basic/hello-form.html");
```

리다이렉트 응답에서는 아래를 절대 섞지 않는다:

* Content-Type
* Body
* 캐시 관련 헤더
* 기타 커스텀 헤더

---

# 5. 핵심 요약

* HTTP 응답은 Status → Header → Body 순서이다
* Redirect는 Body를 사용하지 않는다
* 내용이 흩어져 있으면 이해가 어렵기 때문에 하나의 위치에서 한 번에 작성하는 게 좋다
* redirect와 일반 응답은 목적이 달라 같은 메서드에서 동시에 다루지 않는 것이 바람직하다

---

필요하면 내가 위 코드를 더 읽기 좋게 리팩터링해줄 수도 있어.
혹은 Spring MVC 기준으로 응답을 어떻게 보내는지와 비교해줄 수도 있어.

아래 내용을 차근차근 뜯어서 설명하면 훨씬 명확해진다.
핵심은 `application/json`은 스펙상 기본 인코딩이 UTF-8이라 `charset=utf-8`을 붙이면 안 된다, 그리고 서블릿의 Writer는 자동으로 charset 파라미터를 붙여버린다는 점이다.

---

# 1. `application/json` 은 스펙상 charset이 UTF-8 고정

RFC 8259(이전: RFC 7159)에서 JSON은 이렇게 정의되어 있다:

* JSON 텍스트는 UTF-8로 인코딩해야 한다
* 따라서 MIME 타입 `application/json`에는
  `charset=utf-8` 같은 파라미터를 붙이지 않는다

예:

```
Content-Type: application/json
```

`application/json;charset=utf-8`
→ 표준(MIME 타입 스펙)에는 없는 의미 없는 추가 파라미터가 된다.

---

# 2. 서블릿에서 Writer를 쓰면 charset이 자동으로 붙음

서블릿의 `response.getWriter()`는 내부에서 이런 일을 자동으로 한다:

1. 응답 charset 을 결정
2. Content-Type 헤더에 charset 파라미터를 붙여버림
   예:

```
Content-Type: application/json;charset=UTF-8
```

즉, JSON 스펙과 맞지 않게 헤더가 만들어질 수 있다.

---

# 3. 왜 이런 일이 벌어지나?

서블릿 API는 원래 HTML 텍스트 출력 중심이었고, 텍스트 데이터는 대부분 charset이 필요하다.

그래서 Writer를 쓰면 다음이 자동 적용된다:

* `response.setCharacterEncoding("UTF-8")`
* Content-Type 에 `charset=UTF-8` 추가

하지만 JSON은 예외다.
JSON은 스펙적으로 항상 UTF-8이다.
즉, 구체적인 charset 파라미터가 필요 없다.

---

# 4. 그래서 JSON 출력할 때 OutputStream을 쓰라는 의미

OutputStream은 바이너리 스트림이기 때문에
서블릿이 Content-Type에 charset을 자동으로 붙이지 않는다.

예:

```java
resp.setContentType("application/json");
ServletOutputStream out = resp.getOutputStream();
out.write(jsonString.getBytes(StandardCharsets.UTF_8));
```

이렇게 하면 헤더가 다음처럼 유지된다:

```
Content-Type: application/json
```

즉, 스펙에 맞다.

---

# 5. 정리

1. JSON의 Content-Type은

   ```
   application/json
   ```

   이게 표준이며 charset 파라미터를 붙이지 않는다.

2. `response.getWriter()`
   → Content-Type에 `charset=UTF-8`을 자동 추가해버림
   → JSON 스펙과 맞지 않는 형태 발생

3. `response.getOutputStream()`
   → charset 자동 추가 없음
   → JSON 표준을 지키고 싶은 경우 이쪽 사용이 권장됨

---

더 궁금하면

* JSON 스펙(RFC 8259)의 인코딩 규칙
* 서블릿 API 내부 동작(`getWriter()` vs `getOutputStream()`)
* Spring MVC에서 JSON이 어떻게 처리되는지
  까지도 이어서 설명해줄게.
