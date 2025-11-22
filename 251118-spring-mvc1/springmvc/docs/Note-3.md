Spring Boot 3.2부터는 “자바 컴파일러에 -parameters 옵션을 추가해야 한다”라는 말은
자바 파일을 컴파일할 때, 메서드의 파라미터 이름을 .class 파일에 그대로 보관해 달라는 요청이라는 뜻이다.

### 왜 필요한가

Spring MVC에서는 다음과 같은 코드에서

```java
public String hello(String name)
```

메서드의 파라미터 이름 `name`을 보고 요청 파라미터 `?name=값`을 바인딩한다.
즉 HTTP 요청의 name 값을 저 파라미터로 넣어주는 기능이다.

그런데 자바는 기본적으로 컴파일할 때 파라미터 이름을 제거하고 순서대로만 저장한다.
예를 들어 컴파일 후 내부적으로는 이렇게 보일 수 있다:

```
(arg0)
```

이렇게 되면 Spring 입장에서는 `arg0`이 실제로 `name`이라고 알 수 없다.

그래서 Spring Boot 3.2 이후부터는 다음을 요구한다:

```
javac -parameters
```

이 옵션을 켜면 `.class` 파일에 파라미터 이름을 유지해 준다.
그 결과 Spring이 HTTP 요청 파라미터와 메서드 파라미터를 정확히 매칭할 수 있다.

### 왜 지금 와서 요구하게 됨?

Spring Boot 3.2부터 리플렉션 기반 바인딩을 더 엄격하게 변경했다.
이전에는 파라미터 이름을 찾을 수 없을 경우에도 여러 우회 방법을 사용했지만,
앞으로는 파라미터 이름이 명확히 있어야 안정적이고 오류 없는 바인딩을 위해 명시적으로 요구하게 된 것이다.

### 적용 방법

#### Gradle

```gradle
tasks.withType(JavaCompile) {
    options.compilerArgs << "-parameters"
}
```

#### Maven

```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

### 안 켜면 어떤 문제가 생김?

예를 들어 컨트롤러가

```java
@GetMapping("/hello")
public String hello(String keyword)
```

이렇게 되어 있으면 요청 처리 시 컴파일된 이름이 `arg0`로 남아 있어서
Spring은 keyword를 바인딩하지 못하고 다음과 같은 문제가 발생할 수 있다:

* 파라미터 바인딩 실패
* 400 Bad Request
* Required request parameter 누락 오류

### 정리

| 상태             | .class에 남는 파라미터 이름 | 스프링 요청 파라미터 바인딩 |
| -------------- | ------------------ | --------------- |
| -parameters 없음 | arg0, arg1         | 실패 가능           |
| -parameters 있음 | name, age 같은 실제 이름 | 정확히 매칭됨         |

즉 메서드 파라미터 이름 기반 바인딩을 하기 위해 .class 파일에 이름을 남겨두는 옵션을 꼭 켜야 한다는 뜻이다.

결론부터 말하면 둘 다 영향을 받는다. 하지만 영향의 정도는 다르다.

---

## 1. `@RequestParam`의 경우

```java
@GetMapping("/search")
public String search(@RequestParam String keyword)
```

`@RequestParam`은 기본적으로 메서드 파라미터 이름을 이용해 요청 파라미터 이름을 추론한다.
즉 위 코드는 `?keyword=hello` 라는 요청을 자동으로 매칭한다.

하지만 -parameters 옵션이 꺼져 있으면 컴파일 후 파라미터 이름이 arg0로 바뀌기 때문에
Spring은 keyword라는 이름을 알 수 없다.

그래서 두 가지 문제 중 하나가 발생한다.

| 상황                                     | 결과                  |
| -------------------------------------- | ------------------- |
| @RequestParam("keyword")처럼 이름을 명시해둠    | 문제 없음               |
| 이름을 생략함 (@RequestParam String keyword) | 바인딩 실패 또는 400 오류 가능 |

즉 `@RequestParam`을 이름 없이 쓰려면 -parameters가 필요하다.

---

## 2. `@PathVariable`의 경우

```java
@GetMapping("/users/{id}")
public String getUser(@PathVariable String id)
```

`@PathVariable`도 같은 추론 규칙을 가진다.
id라는 파라미터 이름을 그대로 사용해 경로 변수 {id}를 매핑한다.

하지만 -parameters 옵션 없이 컴파일되면 파라미터 이름이 arg0가 되어 버리므로
Spring 입장에서는 {id}라는 이름을 찾을 수 없다.

역시 명시 여부에 따라 결과가 달라진다.

| 상황                              | 결과                |
| ------------------------------- | ----------------- |
| @PathVariable("id") → 이름 지정     | 문제 없음             |
| @PathVariable String id → 이름 생략 | 바인딩 실패 및 예외 발생 가능 |

즉 `@PathVariable`을 이름 없이 쓰려면 -parameters가 필요하다.

---

## 3. 정리

| 어노테이션           | 이름을 생략하면?      | -parameters 영향 |
| --------------- | -------------- | -------------- |
| `@RequestParam` | 파라미터 이름을 보고 매핑 | 있음             |
| `@PathVariable` | 파라미터 이름을 보고 매핑 | 있음             |

따라서 이름을 생략하고 아래처럼 깔끔히 쓰고 싶다면

```java
@PathVariable String id
@RequestParam String keyword
```

Spring Boot 3.2+에서는 반드시 `-parameters` 옵션을 켜야 한다.

---

## 4. 언제 신경 안 써도 되나요?

아래처럼 항상 이름을 직접 지정하는 스타일이라면 굳이 없어도 된다.

```java
@PathVariable("id") String userId
@RequestParam("keyword") String q
```

하지만 실무에서는 생략하는 스타일이 압도적으로 편하고 보편적이기 때문에
대부분 프로젝트에서 `-parameters` 옵션을 기본으로 켜둔다.

---

결론
Spring Boot 3.2부터는
RequestParam · PathVariable의 파라미터 이름 추론 기능을 안정적으로 사용하려면
자바 컴파일 옵션 -parameters가 꼭 필요하다.

핵심을 정확하게 짚었다.
Spring Boot 3.2부터 `-parameters` 옵션을 요구하게 된 실제 이유가 바로 그것이다.

---

## 3.2 이전까지의 동작 방식

Spring MVC는 기본적으로 Reflection으로 파라미터 이름을 얻으려고 시도하지만
컴파일된 .class 안에 이름 정보가 없으면 다음과 같은 우회 방식들을 사용했다.

대표적인 우회 방식

1. 바이트코드 파싱
   ASM 같은 라이브러리로 .class 파일을 분석해 원래의 파라미터 이름을 추측
2. 디버그 정보 참조
   컴파일 시 `-g` 등이 켜져 있었다면 디버그 심볼에서 파라미터 이름을 찾음
3. 바인딩 시 예외 메시지 분석 및 fallback 전략 적용
4. 문제가 있어도 그냥 경고만 출력하고 넘어감

이 방식은 작동할 때도 있었지만
JDK 버전, 컴파일러 설정, 빌드 툴별 최적화, 난독화, 레코드/람다 등 모던 기능들과 충돌이 잦았다.

결국 예상치 못한 바인딩 오류의 원인이 되기도 했다.

---

## 3.2 이후의 변화

Spring 팀은 더 이상 “파라미터 이름을 어떻게든 추측하려고 하지 않음”을 원칙으로 삼았다.

기준이 바뀜

| 버전     | 파라미터 이름 추론 방식                 |
| ------ | ----------------------------- |
| 3.2 이전 | 추측을 여러 방식으로 시도, 실패하면 fallback |
| 3.2 이후 | 추측 시도 안 함. 이름을 얻을 수 없으면 즉시 실패 |

이로 인해 다음이 필수화됨

```
javac -parameters
```

이 옵션이 없으면 파라미터 이름이 `.class`에 보존되지 않기 때문에
Spring은 매개변수 이름을 알 수 없고 단호하게 바인딩 오류를 낸다.

---

## 왜 이런 변화가 이루어졌는가

Spring의 공식적인 이유는 다음과 같은 안정성 문제 때문이다.

* 바이트코드 분석 방식이 JDK 업그레이드 때마다 계속 깨짐
* 디버그 정보 기반 추론은 컴파일 최적화나 프로덕션 배포 환경과 충돌
* 추론 로직이 작동해도 예측하기 어려운 동작을 만들어 디버깅을 어렵게 함

따라서 “추론 기반 매직”을 제거하고 명시적이고 예측 가능한 바인딩으로 전환한 것이다.

---

## 정리

질문에서 말한 문장이 정확한 핵심이다.

```
이전 스프링: 바이트코드를 파싱해서라도 파라미터 이름을 추론하려고 했다  
→ 지금 스프링(3.2+): 추론을 시도하지 않는다  
→ 따라서 -parameters 옵션이 필수
```

즉,
더 안전하고 예측 가능한 웹 바인딩을 위해
“추측”을 없애고 “명시적 정보만 사용”하는 방향으로 바뀐 것이다.

질문한 내용은 HttpMessageConverter가 HTTP 요청과 응답에서 실제로 데이터를 읽고 쓰는 과정을 알고 싶다는 의미로 이해했다.
Spring MVC 내부에서 이것이 어떻게 동작하는지 흐름 중심으로 설명한다.

---

## 1. 요청(Request) 처리 흐름

클라이언트가 HTTP 요청을 보냄 (예: JSON)

```
POST /users
Content-Type: application/json

{"name": "kim", "age": 20}
```

컨트롤러가 다음과 같이 생겼다고 하자.

```java
@PostMapping("/users")
public User createUser(@RequestBody User user) { ... }
```

Spring MVC 내부 동작

1. 요청 Content-Type 확인
   → `application/json`

2. 등록된 HttpMessageConverter 목록을 순서대로 iterate
   각 컨버터에 대해 다음 메서드를 호출

   ```java
   converter.canRead(User.class, MediaType.APPLICATION_JSON)
   ```

   `true`를 반환하는 컨버터를 찾는다.

3. 예를 들어 `MappingJackson2HttpMessageConverter`가 선택됨
   → JSON을 객체로 변환할 수 있고 미디어 타입도 JSON이기 때문

4. 선택된 컨버터가 `read()` 호출됨

   ```java
   user = converter.read(User.class, inputMessage)
   ```

   실제 JSON → User 객체로 역직렬화 수행

5. 변환된 객체가 컨트롤러 메서드의 파라미터로 전달됨

   ```java
   createUser(User user)
   ```

정리하면
HTTP Body(JSON) → HttpMessageConverter → User 객체

---

## 2. 응답(Response) 처리 흐름

컨트롤러 반환

```java
return new User("kim", 20);
```

Spring MVC 내부 동작

1. 반환 객체의 타입 확인
   `User.class`

2. 클라이언트가 원하는 Accept 헤더 확인
   예:

   ```
   Accept: application/json
   ```

3. 등록된 HttpMessageConverter 목록을 순서대로 iterate

   ```java
   converter.canWrite(User.class, MediaType.APPLICATION_JSON)
   ```

   `true`를 반환하는 컨버터를 찾는다.

4. `MappingJackson2HttpMessageConverter`가 선택됨

5. 선택된 컨버터가 `write()` 호출

   ```java
   converter.write(user, MediaType.APPLICATION_JSON, outputMessage)
   ```

   User 객체 → JSON 직렬화하여 HTTP 응답 Body에 기록

클라이언트는 다음 응답을 받게 됨

```
HTTP/1.1 200 OK
Content-Type: application/json

{"name": "kim", "age": 20}
```

정리하면
User 객체 → HttpMessageConverter → HTTP Body(JSON)

---

## 3. 컨버터 선택 기준 정리

| 단계    | 판단 요소                        | 사용 메서드       |
| ----- | ---------------------------- | ------------ |
| 요청 처리 | 파라미터 타입 + Content-Type       | `canRead()`  |
| 응답 처리 | 반환 타입 + Accept 헤더 혹은 컨트롤러 설정 | `canWrite()` |

즉,
무슨 타입을 변환해야 하는지 + 미디어 타입이 무엇인지
이 두 조건이 맞는 컨버터가 선택된다.

---

## 4. 기본 컨버터와 적용 예시

| 컨버터                                 | 읽기/쓰기 타입 | 미디어 타입 예시        |
| ----------------------------------- | -------- | ---------------- |
| ByteArrayHttpMessageConverter       | byte[]   | 전체               |
| StringHttpMessageConverter          | String   | 전체               |
| MappingJackson2HttpMessageConverter | 객체/Map   | application/json |

예:

```java
@PostMapping("/text")
public String hello(@RequestBody String body) { ... }
```

→ StringHttpMessageConverter 사용

```java
@GetMapping("/raw")
public byte[] raw() { ... }
```

→ ByteArrayHttpMessageConverter 사용

```java
@PostMapping("/users")
public User save(@RequestBody User user) { ... }
```

→ MappingJackson2HttpMessageConverter 사용

---

## 핵심 요약

* `canRead()` → 요청 본문을 어떤 타입으로 변환할 수 있는지 판단
* `read()` → HTTP Body → 자바 객체 변환
* `canWrite()` → 자바 객체를 어떤 미디어타입으로 변환할 수 있는지 판단
* `write()` → 자바 객체 → HTTP Body 변환

즉 HttpMessageConverter는
HTTP 메시지의 Body를 자바 객체로 바꾸고,
자바 객체를 HTTP 메시지 Body로 바꾸는 변환기이다.

웹 MVC에서 @RequestBody와 @ResponseBody가 바로 이 컨버터를 사용하도록 트리거 역할을 한다.

반드시 사용되는 것은 아니다.
HttpEntity는 메시지 컨버터가 동작하도록 “직접 요청하거나 직접 결과를 감싸는” 명시적 수단일 뿐이고,
스프링 MVC에서 메시지 컨버터가 동작하기 위해 꼭 필요한 필수 요소는 아니다.

---

## 메시지 컨버터가 동작하는 두 가지 경로

### 1) 컨트롤러가 일반적인 방식으로 선언된 경우 (가장 흔함)

```java
@PostMapping("/users")
public User create(@RequestBody User user) { ... }
```

이 경우 개발자는 HttpEntity를 사용하지 않지만
내부적으로는 DispatcherServlet → HandlerAdapter → ArgumentResolver → MessageConverter 체인을 따라 자동으로 JSON ↔ 객체 변환이 수행된다.

즉, @RequestBody / @ResponseBody가 메시지 컨버터를 트리거한다.

이 흐름에는 개발자가 HttpEntity를 명시하지 않는다.

---

### 2) HttpEntity를 사용하는 경우 (명시적 방식)

```java
@PostMapping("/users")
public ResponseEntity<User> create(@RequestBody User user) { ... }
```

혹은

```java
@PostMapping("/users")
public HttpEntity<User> create(HttpEntity<User> httpEntity) {
    User user = httpEntity.getBody();
    ...
    return new HttpEntity<>(user);
}
```

이 경우에는 `HttpEntity`의 `getBody()`, `getHeaders()` 등을 통해
요청/응답의 본문과 헤더를 명시적으로 다룬다.

이때도 JSON ↔ 객체 변환은 결국 HttpMessageConverter가 수행한다.

즉,
HttpEntity는 컨버터를 사용한다 → 하지만 컨버터가 동작하기 위해 HttpEntity가 필수는 아니다.

---

## 관계를 간단히 구조적으로 표현

| 요소                           | 역할                      | 필수 여부             |
| ---------------------------- | ----------------------- | ----------------- |
| HttpMessageConverter         | HTTP 메시지 Body ↔ 객체 변환   | 필수                |
| @RequestBody / @ResponseBody | 컨버터 동작을 자동 트리거          | 필수 아님 (다른 방식도 있음) |
| HttpEntity / ResponseEntity  | Body와 헤더를 명시적으로 다룰 때 사용 | 필수 아님             |

컨트롤러가 단순히 객체를 리턴해도 컨버터는 동작한다.

```java
@GetMapping("/users/1")
public User getUser() { ... }  // ResponseEntity 없이도 JSON 응답
```

---

## 결론

* HttpMessageConverter → 항상 사용됨 (바디를 읽거나 쓸 때)
* HttpEntity → 선택 사항

   * 헤더·바디까지 명시적으로 다루고 싶을 때 사용
   * 메시지 컨버터의 동작을 위해 반드시 필요한 건 아니다

즉, 메시지 컨버터는 MVC 내부의 핵심 메커니즘이고,
HttpEntity는 “원하면 바디와 헤더 전체를 객체로 취급할 수 있게 해 주는 API”이다.

질문을 “HTTP 요청이 들어와서 컨트롤러 메서드의 파라미터로 객체가 전달될 때, 내부적으로 어떤 객체들이 오가며 어떤 단계에서 객체가 생성·전달되는가”로 이해하고, 스프링 MVC 내부 호출 흐름 기준으로 설명하겠다.

---

## 전체 흐름 개요

HTTP 요청 → `DispatcherServlet` → `HandlerMapping` → `HandlerAdapter`
→ `ArgumentResolver` → `HttpMessageConverter` → 컨트롤러 메서드 호출

여기서 객체가 실제로 생성되는 지점과 이동 경로가 핵심이다.

---

## 입력(Request) 기준 데이터 전달 흐름

### 1) HTTP 요청이 들어옴

서블릿 컨테이너가 요청을 받아 `DispatcherServlet`에게 넘긴다.
메시지 단위는 `HttpServletRequest` 형태.

### 2) 적절한 핸들러(컨트롤러 + 메서드) 탐색

`HandlerMapping`이 URL/HTTP Method 기반으로 실행할 메서드 정보를 찾는다.
그 결과는 `HandlerMethod`라는 객체로 표현된다.

### 3) HandlerAdapter가 호출 준비

`HandlerAdapter`는 `HandlerMethod`를 실행하기 전에
파라미터를 어떻게 채울지 결정한다.

그러면 HandlerMethodArgumentResolver 체인이 동작한다.

### 4) ArgumentResolver가 파라미터 별로 처리

예를 들어 메서드가 다음과 같다고 하자.

```java
@PostMapping("/users")
public User create(@RequestBody User user)
```

ArgumentResolver는 각 파라미터를 순서대로 검사하며 처리 가능한 resolver를 선택한다.

`@RequestBody User user` → `RequestResponseBodyMethodArgumentResolver`가 담당

### 5) ArgumentResolver가 HttpMessageConverter 호출

ArgumentResolver는 내부적으로 다음을 실행한다.

1. 요청의 `Content-Type` 확인
2. 등록된 컨버터 목록을 순회하며 `canRead(User.class, application/json)` 검사
3. 맞는 컨버터 선택 → `MappingJackson2HttpMessageConverter`

그리고 컨버터에게 Body를 객체로 만드는 작업을 위임한다.

```java
User obj = converter.read(User.class, inputMessage);
```

### 6) 객체가 컨트롤러 메서드로 전달

모든 ArgumentResolver가 각 파라미터 값을 완성하면

```java
create(User user)
```

형태로 실제 메서드 호출이 이루어진다.

여기서 중요한 점:

* 스프링이 직접 메서드를 호출하는 것이 아니라
* Java Reflection으로 메서드를 호출하며, 파라미터 자리에 방금 생성된 객체를 주입한다.

요약

```
HTTP Body(JSON) 
 → HttpMessageConverter.read() 
 → User 객체 생성 
 → ArgumentResolver가 반환 
 → Reflection을 통해 메서드 호출 파라미터에 주입
```

---

## 출력(Response) 기준 객체 전달 흐름

컨트롤러가 객체를 반환한다고 가정한다.

```java
return new User("kim", 20);
```

동작 순서:

1. 컨트롤러 메서드가 객체(User)를 리턴
2. `HandlerAdapter`가 반환값 처리
3. 반환값이 `@ResponseBody` 또는 `ResponseEntity` 또는 `RestController`이면
   → `HandlerMethodReturnValueHandler`가 `HttpMessageConverter`를 호출해야 한다고 판단
4. 메시지 컨버터 목록에서 `canWrite(User.class, application/json)` 검색
5. 매칭되는 컨버터 선택 후 `write()` 호출

   ```
   converter.write(user, application/json, outputMessage)
   ```
6. JSON으로 변환되어 HTTP Body에 기록됨

요약

```
User 객체 
 → HttpMessageConverter.write()
 → JSON 문자열 생성
 → HTTP Body로 기록
```

---

## 핵심만 압축

| 구간      | 객체 전달 주체                                          | 전달되는 데이터           |
| ------- | ------------------------------------------------- | ------------------ |
| 요청 수신   | Servlet → DispatcherServlet                       | HttpServletRequest |
| 파라미터 분석 | HandlerAdapter → ArgumentResolver                 | 변환 전 HTTP Body     |
| 객체 생성   | HttpMessageConverter.read()                       | HTTP Body → 자바 객체  |
| 컨트롤러 호출 | Reflection                                        | 파라미터에 객체 주입        |
| 응답 변환   | ReturnValueHandler → HttpMessageConverter.write() | 자바 객체 → HTTP Body  |

---

## 한 문장으로 정리

컨트롤러 파라미터로 객체가 전달될 때는
ArgumentResolver가 MessageConverter를 이용해 HTTP Body를 역직렬화하여 객체를 만들고,
그 객체를 Reflection 호출에 파라미터로 전달한다.

컨버터는 변환을 담당하고,
ArgumentResolver/ReturnValueHandler는 변환된 객체를 컨트롤러 호출과 응답 처리에 연결해 주는 접착제 역할을 한다.

`v3()`가 실행은 되지만, 화면에 "ok"가 출력되지 않는 것처럼 보이는 이유는
`v3()`의 반환 타입이 `String`인데 @ResponseBody가 없기 때문이다.

즉, `"ok"`라는 문자열은 HTTP 응답 Body로 보내지는 값이 아니라, 뷰 이름(view name) 으로 처리된다.

---

## 왜 이런 일이 발생하는가

`v3()`의 선언을 다시 보면

```java
@GetMapping("/v3")
public String v3() {
    return "ok";
}
```

Spring MVC 규칙:

| 반환 타입    | 어노테이션         | 동작                                    |
| -------- | ------------- | ------------------------------------- |
| `String` | 없음            | 반환값을 ViewResolver에게 넘겨서 뷰 이름으로 처리 |
| `String` | @ResponseBody | 반환값을 HTTP Body에 그대로 출력            |

따라서 `"ok"`는 문자열 데이터가 아니라
뷰 이름 "ok"를 가진 JSP/HTML 템플릿을 찾으려고 시도하게 된다.

예를 들어 다음 위치들 중에서 view 파일을 찾으려 한다.

```
/templates/ok.html (Thymeleaf)
/WEB-INF/views/ok.jsp
/resources/templates/ok.mustache
```

그런 템플릿이 없기 때문에
`whitelabel error page` 또는 `view cannot be resolved` 같은 결과가 나타난다.
그래서 마치 실행되지 않는 것처럼 보이는 것이다.

---

## 해결 방법

### Body에 "ok"를 보내고 싶은 경우

```java
@ResponseBody
@GetMapping("/v3")
public String v3() {
    return "ok";
}
```

혹은 클래스 레벨에 `@RestController`를 붙이면 `@ResponseBody`가 생략 가능하다.

---

### View를 렌더링하려고 했던 경우

정말 템플릿을 반환하고 싶다면 `/templates/ok.html` 같은 뷰 파일을 만들어야 한다.

---

## 내부 동작 비교

| 메서드 | 동작 방식                                                    |
| --- | -------------------------------------------------------- |
| v1  | HttpServletResponse에 직접 써서 Body 출력                       |
| v2  | `ResponseEntity<String>` 사용 → Body "ok" 전송               |
| v3  | `"ok"`을 뷰 이름으로 해석 → 템플릿 파일을 찾음                           |
| v4  | `ResponseEntity<HelloData>` → Body에 JSON 작성              |
| v5  | @ResponseBody + 객체 → Body에 JSON 작성 (MessageConverter 동작) |

이 중에서 v3만 뷰 리졸버(ViewResolver) 경로로 빠진다.

---

## 한 문장으로 정리

`v3()`가 제대로 응답 Body를 채우지 않는 이유는
`@ResponseBody`가 없어서 `"ok"`이 뷰 이름으로 해석되기 때문이다.
→ Body에 문자열을 보내고 싶다면 `@ResponseBody`를 추가하거나 `@RestController`를 사용해야 한다.

`v3()`가 “실행되지 않는 것처럼 보이는 이유”는
`void` 반환 메서드의 동작 방식 때문이다.

메서드는 실행되고 모델에도 값이 담기지만,
화면 렌더링에 사용할 뷰 이름이 제대로 결정되지 않아 뷰를 못 찾아서 결과가 보이지 않는 것이다.

---

## 왜 이런 현상이 발생하는가

`v3()`을 다시 보자.

```java
@RequestMapping("/hello-leaf")
public void v3(Model model) {
    model.addAttribute("data", "hello!");
}
```

반환 타입이 `void`인 경우 Spring MVC 규칙:

| 반환 타입          | 뷰 이름 결정 방식                  |
| -------------- | --------------------------- |
| `String`       | 반환값이 뷰 이름                   |
| `ModelAndView` | 객체 내부에 뷰 이름 포함              |
| `void`         | 요청 URL 경로를 기반으로 뷰 이름을 자동 추론 |

즉 `void`면 뷰 이름을 명시하지 않고, URL 경로로부터 자동 추론한다.

지금 요청 URL은

```
/users/view/hello-leaf
```

따라서 추론되는 뷰 이름은

```
"users/view/hello-leaf"
```

이 이름을 가진 템플릿 파일을 찾으려 한다.

예를 들면 다음 경로들:

```
/templates/users/view/hello-leaf.html
/WEB-INF/views/users/view/hello-leaf.jsp
```

하지만 프로젝트에는 아마 다음처럼 돼 있을 것이다:

```
/templates/hello-leaf.html
```

경로가 다르므로 뷰를 찾지 못해 화면에 아무것도 보이지 않는 결과가 나온다.
그래서 “메서드가 실행되지 않는 것처럼” 느껴진다.

---

## 해결 방법

### 선택 1) String 반환으로 바꾸기

```java
@RequestMapping("/hello-leaf")
public String v3(Model model) {
    model.addAttribute("data", "hello!");
    return "hello-leaf";
}
```

### 선택 2) View 이름을 정확히 맞춰주는 ModelAndView 방식

```java
@RequestMapping("/hello-leaf")
public ModelAndView v3() {
    return new ModelAndView("hello-leaf").addObject("data", "hello!");
}
```

### 선택 3) void를 유지하고 URL을 템플릿 경로와 맞추기

예:

```
/templates/users/view/hello-leaf.html
```

파일을 이렇게 배치하면 Spring이 찾게 된다.

---

## 비교 정리

| 컨트롤러 반환      | 뷰 이름 결정           |
| ------------ | ----------------- |
| ModelAndView | 직접 명시             |
| String       | 직접 명시             |
| void         | URL 경로 기반으로 자동 추측 |

`v3`은 자동 추론된 뷰 이름이 템플릿 파일과 일치하지 않아 렌더링 실패가 발생한 것.

---

## 결론

`v3()`는 내부적으로 정상 실행되지만
`void` 반환 시 URL을 기반으로 한 뷰 자동 매핑 규칙과 템플릿 파일 위치가 일치하지 않아
화면이 출력되지 않는 것이다.
→ 뷰 이름을 String으로 직접 반환하거나 템플릿 경로를 URL과 일치시켜야 한다.

Argument Resolver는 컨트롤러 메서드의 파라미터에 값(객체)을 넣어주는 책임을 가진 확장 기능이다.
Spring MVC의 요청 처리 과정에서, 컨트롤러 메서드를 호출하기 직전에 각 파라미터를 해석하고 채우는 역할을 한다.

---

## 왜 필요한가

아래와 같은 컨트롤러가 있다고 하자.

```java
@GetMapping("/users/{id}")
public String user(
        @PathVariable Long id,
        @RequestParam String name,
        @RequestHeader("User-Agent") String agent,
        HttpServletRequest request,
        HelloData data) {
    ...
}
```

HTTP 요청에는 단지 URL, 헤더, 바디 등이 존재할 뿐인데
위의 파라미터들이 어떻게 각각 들어올까?

* id는 경로 변수
* name은 요청 파라미터
* agent는 헤더
* request는 서블릿 객체
* data는 JSON Body
  → 각각 해석 방식이 다르다

Spring MVC는 컨트롤러를 호출하기 전에
각 파라미터의 타입, 어노테이션 등을 보고
해석 가능한 Argument Resolver에게 처리 권한을 위임한다.

---

## 동작 흐름

요청이 컨트롤러 메서드에 도달하면

1. 스프링은 파라미터를 하나씩 순서대로 확인한다.
2. 등록된 ArgumentResolver 목록을 순회한다.
3. 각 resolver에 대해 다음 메서드를 호출한다.

   ```java
   resolver.supportsParameter(parameter)
   ```

   → 이 resolver가 해당 파라미터를 처리할 수 있는지 판별
4. 처리 가능한 resolver가 선택되면

   ```java
   resolver.resolveArgument(parameter, ...)
   ```

   → 여기서 실제 값을 만들고 반환
5. 반환된 값이 컨트롤러 파라미터 자리에 채워짐
6. 모든 파라미터가 준비되면 컨트롤러 메서드가 호출됨

---

## 대표 Argument Resolver 예시

| 처리 대상                                       | Resolver                                  |
| ------------------------------------------- | ----------------------------------------- |
| `@RequestParam`                             | RequestParamMethodArgumentResolver        |
| `@PathVariable`                             | PathVariableMethodArgumentResolver        |
| `@ModelAttribute`                           | ModelAttributeMethodArgumentResolver      |
| `@RequestBody`                              | RequestResponseBodyMethodArgumentResolver |
| `@RequestHeader`                            | RequestHeaderMethodArgumentResolver       |
| `HttpServletRequest`, `HttpServletResponse` | ServletRequestMethodArgumentResolver      |
| `Principal`                                 | PrincipalMethodArgumentResolver           |
| `Locale`                                    | LocaleMethodArgumentResolver              |
| `@CookieValue`                              | CookieValueMethodArgumentResolver         |

각 항목마다 Request, Path, Header, Body 등 해석 방식이 모두 다르기 때문에
Resolver 들이 역할을 분담하고 있다.

---

## MessageConverter와의 관계

ArgumentResolver는 요청 Body를 처리할 때 MessageConverter를 내부적으로 호출한다.

예:

```java
@PostMapping("/users")
public HelloData save(@RequestBody HelloData data)
```

여기서는
`RequestResponseBodyMethodArgumentResolver`가 파라미터를 담당하고
이 resolver 내부에서 `HttpMessageConverter.read()`를 실행해 JSON → 객체 변환을 수행한다.

즉

| 역할                    | 담당               |
| --------------------- | ---------------- |
| Body를 객체로 변환          | MessageConverter |
| 변환된 객체를 컨트롤러 파라미터에 공급 | ArgumentResolver |

---

## 한 문장 요약

Argument Resolver는
컨트롤러 파라미터를 해석해서 적절한 값(객체)을 만들어 주입해 주는 모듈이며,
Spring이 컨트롤러를 호출할 수 있게 만들어 주는 핵심 연결고리다.

`ReturnValueHandler`는 컨트롤러 메서드의 반환값을 해석해서 적절한 방식으로 응답 처리하도록 만드는 스프링 MVC 구성 요소이다.
즉, argument resolver가 “메서드 파라미터를 채워주는 역할”이라면,
return value handler는 “메서드 반환값을 HTTP 응답으로 바꿔주는 역할”이다.

---

## 왜 필요한가

컨트롤러 메서드는 다양한 형태로 값을 반환한다.

```java
return "hello";                    // 뷰 이름
return user;                       // 객체 → JSON
return new ModelAndView(...);      // 모델 + 뷰
return ResponseEntity.ok(user);    // 상태 코드 + 헤더 + 바디
return void;                       // 뷰 자동 추론
```

Spring은 반환값의 종류가 이렇게 다양하더라도
하나의 규칙으로 단순히 처리하지 않는다.
각기 다른 반환 타입을 해석할 수 있는 ReturnValueHandler 목록을 등록해 두고,
컨트롤러 반환값을 적절한 방식으로 응답 처리한다.

---

## 동작 흐름

컨트롤러가 실행된 후 반환값이 나오면

1. 스프링 MVC는 등록된 ReturnValueHandler 리스트를 순회한다.
2. 다음 메서드로 해당 handler가 처리 가능한지 확인한다.

   ```java
   handler.supportsReturnType(returnType)
   ```
3. 처리 가능한 핸들러가 선택되면

   ```java
   handler.handleReturnValue(returnVal, ...)
   ```

   → 실제 응답 처리 작업을 수행한다.

이때 내부적으로 MessageConverter가 필요하면 호출할 수도 있다.
즉,ReturnValueHandler는 반환값을 어떻게 응답으로 바꿀지 결정하고 조정한다.

---

## 대표 ReturnValueHandler 예시

| 반환 형태            | 사용되는 Handler                                | 처리 방식                             |
| ---------------- | ------------------------------------------- | --------------------------------- |
| `String`(뷰 이름)   | ViewNameMethodReturnValueHandler            | ViewResolver로 뷰 렌더링               |
| `ModelAndView`   | ModelAndViewMethodReturnValueHandler        | 모델 + 뷰 그대로 처리                     |
| `@ResponseBody`  | RequestResponseBodyMethodReturnValueHandler | HttpMessageConverter로 body에 직접 작성 |
| `ResponseEntity` | ResponseEntityMethodReturnValueHandler      | 상태 코드 + 헤더 + Body 처리              |
| `void`           | ServletModelAttributeMethodProcessor 등      | URL 기반 뷰 자동 추론 또는 직접 응답 컨트롤       |

이 덕분에 컨트롤러는 어떤 형식으로 반환하든 동작한다.

---

## MessageConverter와의 관계

반환값이 HTTP Body에 직접 들어가는 경우:

```java
@GetMapping("/users")
@ResponseBody
public HelloData user() {
    return new HelloData("kim", 10);
}
```

여기서는

* ReturnValueHandler: `RequestResponseBodyMethodReturnValueHandler`
* 하지만 JSON 변환 자체는 MessageConverter가 담당

  ```
  converter.write(HelloData → JSON → HTTP Body)
  ```

즉 역할 분담은 다음과 같다.

| 역할                 | 담당                   |
| ------------------ | -------------------- |
| 반환값이 어떤 처리 방식인지 판정 | ReturnValueHandler   |
| 실제 객체 ↔ 메시지 변환     | HttpMessageConverter |

---

## ArgumentResolver와 ReturnValueHandler 비교

| 시점        | 역할      | 예                                                      |
| --------- | ------- | ------------------------------------------------------ |
| 컨트롤러 호출 전 | 파라미터 채움 | `@RequestBody`, `@PathVariable`, `@RequestParam` 등을 처리 |
| 컨트롤러 호출 후 | 반환값 처리  | 뷰 렌더링, JSON 응답, ResponseEntity 등                       |

둘은 서로 반대 방향의 역할을 한다.

---

## 한 문장 요약

ReturnValueHandler는
컨트롤러가 반환한 값을 분석하여 뷰 렌더링 또는 HTTP Body 작성 등 적절한 방식으로 응답을 만드는 “반환 처리 전략” 컴포넌트이다.

`WebMvcConfigurer`는 Spring MVC의 동작을 개발자가 확장·커스터마이징할 수 있도록 제공되는 콜백 인터페이스이다.
즉, Spring MVC의 자동 설정을 유지하면서 원하는 부분만 덧붙여 설정할 수 있는 확장 지점이다.

Spring Boot는 대부분의 MVC 설정을 자동으로 해 준다.
하지만 가끔 다음이 필요할 수 있다.

* 인터셉터 추가
* CORS 설정
* 정적 리소스 핸들링 방법 변경
* 메시지 컨버터 추가 또는 우선순위 변경
* 뷰 리졸버 추가
* API 응답 포맷 조정

이런 기능을 위해 전체 MVC 설정을 덮어쓰기(= `@EnableWebMvc`) 하면
Boot의 자동 설정이 전부 꺼지고 비효율적이다.

그래서 자동 설정을 유지하면서 필요한 기능만 선택해서 확장하기 위해
`WebMvcConfigurer`가 제공된다.

---

## 어디에 사용하나 (예시)

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/")
                .excludePathPatterns("/css/", "/error");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/")
                .allowedOrigins("https://example.com");
    }
}
```

Spring Boot는 기존 MVC 설정을 그대로 유지하고,
해당 설정만 추가로 적용한다.

---

## 대표 확장 포인트들

| 기능          | WebMvcConfigurer 메서드                                     |
| ----------- | -------------------------------------------------------- |
| 인터셉터 등록     | `addInterceptors`                                        |
| 리소스 핸들링     | `addResourceHandlers`                                    |
| CORS        | `addCorsMappings`                                        |
| 뷰 컨트롤러      | `addViewControllers`                                     |
| 메시지 컨버터 조정  | `extendMessageConverters` / `configureMessageConverters` |
| 포맷터/컨버터 등록  | `addFormatters`                                          |
| URL 매핑 설정   | `configurePathMatch`                                     |
| 요청 파라미터 리졸버 | `addArgumentResolvers`                                   |
| 반환값 핸들러     | `addReturnValueHandlers`                                 |

이런 지점을 통해 Spring MVC의 내부 동작에 들어가는 컴포넌트들을 수정하거나 추가할 수 있다.

---

## `configureMessageConverters` vs `extendMessageConverters`

많이 헷갈리는 부분을 정리해 보면

| 메서드                          | 목적                           |
| ---------------------------- | ---------------------------- |
| `extendMessageConverters`    | 기본 등록된 컨버터 유지 + 뒤에 추가        |
| `configureMessageConverters` | 기본 컨버터 제거 + 개발자가 등록한 컨버터만 사용 |

대부분은 `extendMessageConverters`를 사용해야 한다.

---

## `WebMvcConfigurer` vs `@EnableWebMvc`

| 항목                 | 동작                                   |
| ------------------ | ------------------------------------ |
| `WebMvcConfigurer` | Boot 자동 설정 유지 + 개발자 설정 추가            |
| `@EnableWebMvc`    | Boot 자동 설정 비활성화 + 개발자가 모든 MVC 설정을 책임 |

일반적으로 Boot 환경에서는 `@EnableWebMvc`를 사용하지 않는다.

---

## 한 문장 요약

WebMvcConfigurer는
스프링 MVC의 기본 자동 설정을 유지하면서, 필요한 설정만 선택적으로 확장할 수 있게 해주는 구성 인터페이스이다.
