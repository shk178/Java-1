- Spring Boot에서는 static/index.html을 렌더링할 때와 templates/index.html을 렌더링할 때 동작 방식이 다릅니다.

| 경로 | 렌더링 방식 | 컨트롤러 필요 여부 |
|------|-------------|-------------------|
| src/main/resources/templates/index.html | Thymeleaf 등 템플릿 엔진으로 렌더링 | 필요함 (@Controller + return "index") |
| src/main/resources/static/index.html | 정적 파일로 서빙됨 | 필요 없음 (http://localhost:8080/index.html로 직접 접근) |

- static/index.html을 보여주려면?
- 방법 1: 컨트롤러 없이 직접 접근
- src/main/resources/static/index.html에 HTML 파일을 만들면
- 브라우저에서 http://localhost:8080/index.html로 접속하면 바로 보여져요.
- 이건 정적 파일 처리 방식이라 템플릿 엔진을 거치지 않아요.
- 방법 2: 컨트롤러에서 리다이렉트
정적 파일을 / 경로에서 보여주고 싶다면 컨트롤러에서 리다이렉트를 사용해요:
```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToStatic() {
        return "redirect:/index.html"; // static/index.html로 리다이렉트
    }
}
```
- 이렇게 하면 http://localhost:8080/으로 접속했을 때 static/index.html로 자동 이동돼요.
- static 폴더는 CSS, JS, 이미지, HTML 같은 정적 리소스를 위한 공간이에요.
- templates 폴더는 Thymeleaf 같은 템플릿 엔진을 사용하는 HTML을 위한 공간이에요.
- index.html 1. 템플릿 페이지로 만들기 (Thymeleaf 사용)
- 템플릿은 동적으로 데이터를 넣거나 조건문, 반복문 등을 사용할 수 있어요.
- 예를 들어 사용자 이름을 보여주거나 리스트를 출력할 수 있죠.
- 파일 위치: src/main/resources/templates/index.html
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>템플릿 페이지</title>
</head>
<body>
    <h1 th:text="'안녕하세요, ' + ${name} + '님!'">안녕하세요!</h1>
    <ul>
        <li th:each="item : ${items}" th:text="${item}">아이템</li>
    </ul>
</body>
</html>
```
- 컨트롤러에서 데이터 전달
```java
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("name", "shk178");
        model.addAttribute("items", List.of("Spring", "Java", "Gradle"));
        return "index";
    }
}
```
- 2. 정적 페이지로 꾸미기 (HTML만 사용)
- 정적 페이지는 단순한 HTML로 구성돼서 서버에서 바로 서빙돼요.
- 동적 데이터는 없고, 순수한 디자인과 정보만 담겨요.
- 파일 위치: src/main/resources/static/index.html
```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>정적 페이지</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>
    <h1>Welcome to My Spring Site</h1>
    <p>이 페이지는 정적 HTML로 구성되어 있습니다.</p>
    <img src="/images/spring-logo.png" alt="Spring Logo">
</body>
</html>
```
- /css/style.css나 /images/spring-logo.png 같은 리소스는 static 폴더 안에 있어야 해요:
- src/main/resources/static/css/style.css
- src/main/resources/static/images/spring-logo.png

| 목적 | 추천 방식 |
|------|-----------|
| 동적 데이터 표시 (예: 사용자 이름, 리스트) | 템플릿 (Thymeleaf) |
| 단순한 소개 페이지, 이미지, 스타일만 있는 경우 | 정적 페이지 |

- Gradle 빌드를 왜 할까?
- Gradle 빌드는 단순히 "코드를 컴파일한다"는 걸 넘어서 프로젝트 전체를 실행 가능한 형태로 준비하는 과정이에요.
- 1. 의존성 다운로드
- build.gradle에 선언한 라이브러리들(예: Spring Web, Thymeleaf 등)을 자동으로 다운로드하고 연결해줘요.
- 2. 코드 컴파일
- Java 코드를 .class 파일로 컴파일해서 실행 가능한 상태로 만들어요.
- 3. 리소스 처리
- resources 폴더의 HTML, CSS, 이미지 등을 포함해서 패키징해요.
- 4. 실행 파일(JAR) 생성
- ./gradlew build를 하면 build/libs/에 .jar 파일이 생겨요. 이걸로 서버를 실행할 수 있어요.
- 5. 테스트 실행
- ./gradlew test를 하면 테스트 코드를 실행해서 문제가 없는지 확인해줘요.
- Gradle 빌드 방법
- Gradle Wrapper 사용 (추천)
- 프로젝트에 이미 gradlew와 gradlew.bat 파일이 있다면, 아래 명령어로 빌드해요:
```bash
./gradlew build
```
- Windows에서는:
```bash
gradlew build
```
- 빌드 결과 확인
- 빌드가 성공하면 build/libs/ 폴더에 .jar 파일이 생겨요.
- 예: hello-0.0.1-SNAPSHOT.jar
- 실행 방법
- 빌드된 .jar 파일을 직접 실행할 수도 있어요:
```bash
java -jar build/libs/hello-0.0.1-SNAPSHOT.jar
```

| 명령어 | 설명 |
|--------|------|
| ./gradlew clean | 이전 빌드 결과를 삭제 |
| ./gradlew bootRun | 스프링 부트 애플리케이션을 바로 실행 |
| ./gradlew test | 테스트 코드 실행 |
| ./gradlew build --info | 빌드 상세 로그 출력 |

- JAVA_HOME 설정하기
- Java 설치 경로 확인
- IntelliJ에서 설치한 Java 경로를 확인하거나, 직접 설치한 Java 경로를 찾아야 해요.
- 예를 들어: C:\Program Files\Java\jdk-21 이런 식으로 jdk 폴더가 있어야 해요.
- IntelliJ에서 설치한 경우엔 다음 경로일 수도 있어요:
- C:\Users\user\.jdks\openjdk-21 (C:\Users\user\.jdks\ms-21.0.8)
- JAVA_HOME 환경변수 설정 (Windows 기준)
- 방법 A: 명령어로 일시 설정 (현재 터미널에서만 적용됨)
```cmd
set JAVA_HOME=C:\Program Files\Java\jdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
```
- 방법 B: 시스템 환경변수로 영구 설정
```
1. Win + S → "환경 변수" 검색 → "시스템 환경 변수 편집" 클릭
2. 아래쪽 "환경 변수(N)..." 버튼 클릭
3. 시스템 변수에서 JAVA_HOME 새로 만들기
   - 변수 이름: JAVA_HOME
   - 변수 값: C:\Program Files\Java\jdk-21 (자신의 JDK 경로로 설정) (C:\Users\user\.jdks\ms-21.0.8)
4. Path 변수 편집 → 새 항목 추가: %JAVA_HOME%\bin (C:\Users\user\.jdks\ms-21.0.8\bin)
5. 확인 → 확인 → 확인
```
- 터미널에서 아래 명령어로 확인해요:
```cmd
java -version
```
### http://localhost:8080/hello-mvc Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.

Mon Nov 03 13:04:40 KST 2025
There was an unexpected error (type=Bad Request, status=400).
### http://localhost:8080/hello-mvc?name=
hello
### http://localhost:8080/hello-mvc?name=1
hello 1
- @RequestParam("name")을 필수 파라미터로 설정해놓으면
- name이 없는 요청에서는 400 Bad Request 에러가 발생해서 템플릿 자체가 렌더링되지 않아요.
- 그래서 템플릿 안에 있는 기본 텍스트도 뜨지 않게 돼요.
- 해결 방법: 파라미터를 선택적으로 만들기
```java
@GetMapping("hello-mvc")
public String helloMvc(@RequestParam(value = "name", required = false) String name, Model model) {
    model.addAttribute("name", name != null ? name : "방문자");
    return "hello-template";
}
```
- required = false로 설정하면 name이 없어도 에러가 나지 않아요.
- name이 없을 경우 "방문자"라는 기본값을 넣어줘요.
### http://localhost:8080/hello-mvc2
hello null (기본 메시지가 안 뜬다.)
### http://localhost:8080/hello-mvc2?name
hello (빈 문자열이다.)
- hello-template.html에서 파라미터가 없을 때도 "안녕하세요!"라는 기본 텍스트가 뜨게 하는 것
- Thymeleaf의 조건 처리 기능을 활용하면 아주 자연스럽게 해결할 수 있어요.
- 방법: 템플릿에서 조건 처리하기
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Hello</title>
</head>
<body>
    <h1 th:if="${name}" th:text="'안녕하세요, ' + ${name} + '님!'">안녕하세요!</h1>
    <h1 th:unless="${name}">안녕하세요!</h1>
</body>
</html>
```
- th:if="${name}": name이 있을 때만 "안녕하세요, 성희님!"처럼 출력
- th:unless="${name}": name이 없을 때 "안녕하세요!" 출력
- 컨트롤러 코드 예시
```java
@GetMapping("hello-mvc2")
public String helloMvc2(@RequestParam(value = "name", required = false) String name, Model model) {
    model.addAttribute("name", name); // null일 수도 있음
    return "hello-template";
}
```
- name이 없어도 에러 없이 실행되고 템플릿에서 조건에 따라 출력이 달라져요
### http://localhost:8080/hello-mvc3
안녕하세요!
### http://localhost:8080/hello-mvc3?name
안녕하세요, 님!
### http://localhost:8080/hello-mvc3?name=
안녕하세요, 님!
- null이라는 글자가 보이지 않고 "안녕하세요, 님!"처럼 출력된 이유는
- Thymeleaf가 null 값을 빈 문자열로 처리했기 때문이에요.
- 1. /hello-mvc3
- name 파라미터가 아예 없음
- @RequestParam(value = "name", required = false) 덕분에 name은 null
- 템플릿에서 ${name}은 null → 조건문으로 처리하지 않으면 "null"로 보일 수도 있음
- 하지만 템플릿에서 th:text="'안녕하세요, ' + ${name} + '님!'"처럼 쓰면:
- null + "님!" → "null님!"이 아니라 빈 문자열로 처리돼서 "안녕하세요, 님!"이 됨
- 2. /hello-mvc3?name, /hello-mvc3?name=
- name 파라미터는 있지만 값이 없음
- 즉, name="" (빈 문자열)
- 이 경우에도 ${name}은 빈 문자열 → "안녕하세요, 님!"
- 조건문으로 처리
```html
<h1 th:if="${name}">안녕하세요, <span th:text="${name}"></span>님!</h1>
<h1 th:unless="${name}">안녕하세요!</h1>
```
- name이 없거나 빈 문자열이면 "안녕하세요!" 출력
- name이 있으면 "안녕하세요, 성희님!" 출력
- Java 코드에서 기본값 처리
```java
@RequestParam(value = "name", defaultValue = "") String name
//...
model.addAttribute("name", name.isBlank() ? null : name);
```
### @RequestParam 동작 정리
```java
@RequestParam("name") String name
```
- name이 필수 파라미터로 설정됨
- /hello-mvc2 → 400 Bad Request (파라미터 없음)
- /hello-mvc2?name= → "name"은 빈 문자열 ("")
### 선택적 파라미터
```java
@RequestParam(value = "name", required = false) String name
```
- name이 선택적 파라미터로 설정됨
- /hello-mvc2 → name = null
- /hello-mvc2?name 또는 /hello-mvc2?name= → name = "" (빈 문자열)
- 즉, null은 파라미터 자체가 없을 때만 발생
- 빈 문자열은 파라미터는 있지만 값이 없을 때 발생
- 이걸 활용해서 템플릿에서 조건 분기를 하거나, 기본값을 설정할 수 있어요:
```java
model.addAttribute("name", (name == null || name.isBlank()) ? "방문자" : name);
```
- 또는 템플릿에서:
```html
<h1 th:if="${name}">안녕하세요, <span th:text="${name}"></span>님!</h1>
<h1 th:unless="${name}">안녕하세요!</h1>
```
### http://localhost:8080/not-use-view-resolver?name=1
hello 1
- @ResponseBody를 붙이면 뷰 리졸버(View Resolver)를 사용하지 않는다
- Spring MVC에서 컨트롤러가 문자열을 반환할 때
- 그 문자열이 "뷰 이름"인지, 그냥 "문자열 데이터"인지를 판단해서 처리해주는 게
- 뷰 리졸버(View Resolver)예요.
```java
@GetMapping("/")
public String home() {
    return "index"; // → templates/index.html을 찾아서 렌더링
}
```
- 여기서 "index"는 뷰 이름이에요.
- Spring은 ViewResolver를 통해 src/main/resources/templates/index.html을 찾아서 렌더링해요.
```java
@GetMapping("not-use-view-resolver")
@ResponseBody
public String helloString(@RequestParam("name") String name) {
    return "<html><h1>hello " + name + "</h1></html>";
}
```
- 이 경우 `"hello <name>"`은 뷰 이름이 아니라 그냥 문자열 데이터예요.
- Spring은 뷰를 찾지 않고, 그 문자열을 HTTP 응답 본문(body)에 그대로 실어서 클라이언트에게 보내요.
- 즉, 뷰 리졸버를 건너뛰고 직접 응답을 반환하는 방식이에요.
- @ResponseBody는 API 응답, 간단한 텍스트 응답, JSON 반환 등에 자주 사용돼요.
- 예: @RestController는 모든 메서드에 자동으로 @ResponseBody가 붙은 것처럼 동작해요.

| 반환 방식 | 뷰 리졸버 사용 여부 | 결과 |
|-----------|----------------|------|
| `return "index"` | 사용함 | index.html 템플릿 렌더링 |
| `@ResponseBody return "<html>..."` | 사용 안 함 | HTML 문자열 그대로 응답 |

- "템플릿 렌더링"과 "HTML 문자열 그대로 응답"은 Spring에서 HTML을 처리하는 두 가지 방식
### 템플릿 렌더링이란?
- 템플릿 렌더링은 HTML 파일 안에 있는 동적 표현식(예: ${name})을
- 서버에서 처리해서 완성된 HTML을 만들어 클라이언트에게 보내는 것이에요.
- templates/index.html
```html
<h1 th:text="'안녕하세요, ' + ${name} + '님!'">안녕하세요!</h1>
```
- 컨트롤러
```java
@GetMapping("/")
public String home(Model model) {
    model.addAttribute("name", "shk178");
    return "index"; // templates/index.html 렌더링
}
```
- 결과
- 서버가 ${name}을 "shk178"로 바꿔서 클라이언트에게 아래처럼 완성된 HTML을 보냄:
```html
<h1>안녕하세요, shk178님!</h1>
```
- 이게 바로 템플릿 렌더링이에요. 템플릿 엔진(예: Thymeleaf)이 동작해서 HTML을 "렌더링"해주는 거죠.
### HTML 문자열 그대로 응답이란?
- 이건 서버가 HTML을 문자열로 직접 반환하는 방식이에요.
- 즉, 서버는 아무런 처리 없이 문자열을 그대로 클라이언트에게 보내요.
```java
@GetMapping("/raw-html")
@ResponseBody
public String rawHtml(@RequestParam("name") String name) {
    return "<h1>안녕하세요, " + name + "님!</h1>";
}
```
- 결과
- 서버는 `<h1>안녕하세요, shk178님!</h1>`라는 문자열을 그대로 응답 본문에 넣어서 보냄.
- 템플릿 엔진은 전혀 관여하지 않음.

| 방식 | 템플릿 렌더링 | HTML 문자열 응답 |
|------|----------------|------------------|
| 처리 주체 | 서버가 템플릿 엔진으로 렌더링 | 서버가 문자열 그대로 반환 |
| 표현식 사용 | 가능 (${name}, th:if) | 불가능 |
| 파일 위치 | src/main/resources/templates | 없음 (코드 안에 직접 작성) |
| 용도 | 동적 페이지, MVC | 간단한 응답, API, 테스트 |

- 템플릿 렌더링은 HTML 파일을 기반으로 동적 데이터를 넣어주는 방식
- HTML 문자열 응답은 그냥 문자열을 그대로 보내는 방식
- 둘 다 브라우저에서는 "렌더링"되지만, 서버에서 처리하는 방식이 다름
### 브라우저 렌더링이란?
- HTML 코드 → 실제 웹페이지 화면으로 변환하는 과정
- 브라우저는 우리가 작성한 HTML, CSS, JS 파일을 받아서 다음과 같은 과정을 거쳐 화면에 보여줘:
- 1. HTML 파싱
- HTML 문서를 위에서 아래로 읽으면서 DOM(Document Object Model)을 만든다.
- 2. CSS 파싱
- 스타일 정보를 읽어서 CSSOM(CSS Object Model)을 만든다.
- 3. 렌더 트리(Render Tree) 생성
- DOM + CSSOM을 합쳐서 실제로 화면에 표시할 요소들을 정리한 구조를 만든다.
- 4. 레이아웃 계산
- 각 요소의 위치와 크기를 계산한다.
- 5. 페인팅(Painting)
- 계산된 내용을 바탕으로 픽셀 단위로 화면에 그린다.
- 6. 컴포지팅(Compositing)
- 여러 레이어를 합쳐서 최종 화면을 만든다.
```html
<h1 style="color: blue;">안녕하세요!</h1>
```
- 브라우저 렌더링 결과 파란색 큰 글씨로 "안녕하세요!"가 화면에 표시됨
- 우리가 HTML을 작성하거나 서버에서 HTML을 응답할 때
- 브라우저가 어떻게 해석해서 보여줄지를 이해하면 더 좋은 웹페이지를 만들 수 있어.
- 성능 최적화, 반응형 디자인, 접근성 개선 등도 렌더링 흐름을 이해해야 잘 할 수 있어.
### 서버가 문자열을 그대로 반환할 때 브라우저가 어떻게 처리하는지
```java
@GetMapping("not-use-view-resolver2")
@ResponseBody
public String helloString2(@RequestParam("name") String name) {
    return "hello " + name;
}
```
- 브라우저는 문자열을 받으면 "렌더링"은 해.
- 하지만 그게 HTML이 아니면 그냥 텍스트로 보여줄 뿐이야.
- 이 코드는 "hello 이름" 같은 순수 문자열을 HTTP 응답 본문에 그대로 넣어서 반환해.
- 브라우저는 이 응답을 받아서 화면에 그대로 출력해.
- 하지만 HTML 태그가 없기 때문에 스타일이나 구조 없이 그냥 텍스트만 보여줘.
- 즉, 브라우저는 받은 내용을 해석해서 보여주긴 하지만
- 그 내용이 HTML이 아니면 "렌더링"이라기보다는 그냥 출력"에 가까워.

| 응답 내용 | 브라우저 렌더링 방식 |
|-----------|--------------|
| `"hello 이름"` | 그냥 텍스트로 `"hello 이름"` 표시됨 |
| `"<h1>hello 이름</h1>"` | `<h1>` 태그가 적용돼서 크게 표시됨 |
| `"{"name":"이름"}"` | JSON 문자열로 표시됨 (스타일 없음) |

### http://localhost:8080/hello-api?name=1
{"name":"1"}
// pretty print 적용 체크하면
{
"name": "1"
}
### @ResponseBody를 사용
- viewResolver 대신에 HttpMessageConverter가 동작
- 기본 문자 처리: StringHttpMessageConverter
- 기본 객체 처리: MappingJackson2HttpMessageConverter
- byte 등 여러 처리마다 기본 HttpMessageConverter가 있다.
- 클라이언트의 HTTP Accept 헤더 + 서버의 컨트롤러 반환 타입을 조합해서 HttpMessageConverter가 선택된다.
- Controller, Converter 모두 스프링 컨테이너 안에 있다.
- MappingJackson2HttpMessageConverter - JsonConverter다.
### 클라이언트의 HTTP Accept 헤더 + 서버의 컨트롤러 반환 타입을 조합
- 부분적으로 맞지만 정확하지 않습니다. 실제로는:
- 요청 처리 시 (요청 본문 → 객체): Content-Type 헤더 + 메서드 파라미터 타입
- 응답 처리 시 (객체 → 응답 본문): Accept 헤더 + 컨트롤러 반환 타입
- 그리고 더 정확하게는 HttpMessageConverter 선택 기준
- 1. canRead() / canWrite() 메서드로 지원 여부 확인
- 미디어 타입 (Content-Type, Accept)
- 클래스 타입 (반환 타입, 파라미터 타입)
- 2. 우선순위에 따라 첫 번째로 매칭되는 컨버터 선택
### "클라이언트의 HTTP Accept 헤더 타입을 설정하면 거기에 맞는 컨버터가 작동"
- 맞지만, 중요한 제약이 있습니다:
- Accept 헤더만으로 결정되는 것이 아님
- 컨트롤러의 반환 타입도 중요한 판단 기준
- 예: 반환 타입이 String인데 Accept가 application/json이어도 StringHttpMessageConverter가 동작할 수 있음
### @RequestBody도 HttpMessageConverter를 사용합니다:
```java
@PostMapping
public String create(@RequestBody Member member) {
    // Content-Type 헤더 + Member 타입
    // → MappingJackson2HttpMessageConverter 동작
}
```
### 보완된 정리
- 1. @ResponseBody (및 @RequestBody) 사용
- viewResolver 대신 HttpMessageConverter가 동작
- 기본 문자 처리: StringHttpMessageConverter
- 기본 객체 처리: MappingJackson2HttpMessageConverter
- byte 등 여러 타입마다 기본 HttpMessageConverter가 있음
- 2. Converter 선택 기준
- 응답 시 (@ResponseBody): 클라이언트의 Accept 헤더 + 컨트롤러 반환 타입
- 요청 시 (@RequestBody): 클라이언트의 Content-Type 헤더 + 메서드 파라미터 타입
- 3. 동작 방식
- 등록된 HttpMessageConverter 목록을 순회
- canRead() / canWrite()로 처리 가능 여부 확인
- 첫 번째로 매칭되는 컨버터 사용
- Controller, Converter 모두 스프링 컨테이너 내에서 관리
### @RequestParam과 Converter
- HttpMessageConverter는 사용 안 함
- @RequestParam은 HttpMessageConverter를 사용하지 않습니다.
- 대신 Converter/Formatter 사용
```java
@GetMapping("/user")
public String getUser(@RequestParam String name,
                      @RequestParam Integer age,
                      @RequestParam LocalDate birthDate) {
    // ...
}
```
- 동작 방식:
- URL 쿼리 스트링: ?name=kim&age=25&birthDate=2000-01-01
- 모두 문자열로 전달됨
- Spring이 자동으로 타입 변환:
- "25" → Integer (Converter 사용), "2000-01-01" → LocalDate (Formatter 사용)
### 타입 변환 담당자들
- 1. Converter - 단순 타입 변환
- String → Integer, String → Long, String → Boolean
- 2. Formatter - 문자열 ↔ 객체 (주로 날짜, 숫자 포맷)
- String → LocalDate, String → LocalDateTime
- 3. HttpMessageConverter - HTTP 요청/응답 본문 변환
- JSON → 객체 (@RequestBody), 객체 → JSON (@ResponseBody)

| 애노테이션 | 데이터 위치 | 사용하는 변환기 |
|-----------|------------|----------------|
| @RequestParam | URL 쿼리 스트링 | Converter/Formatter |
| @PathVariable | URL 경로 | Converter/Formatter |
| @RequestBody | HTTP Body | HttpMessageConverter |
| @ResponseBody | HTTP Body | HttpMessageConverter |

### 스프링 컨테이너란?
- 객체(Bean)를 생성하고 관리하는 스프링의 핵심 시스템
- 일반 자바 프로그래밍:
- `MyService service = new MyService();  // 내가 직접 생성`
- `MyController controller = new MyController(service);  // 내가 직접 주입`
- 스프링 컨테이너 사용:
```java
// 스프링이 알아서 생성하고 주입
@Controller
class MyController {
    @Autowired
    MyService service;  // 스프링이 자동으로 넣어줌
}
```
- 주요 역할 1. Bean 생성 및 관리
```java
@Service
public class MemberService { }  // 스프링 컨테이너가 생성해서 보관

@Controller
public class MemberController { }  // 스프링 컨테이너가 생성해서 보관
```
- 2. 의존성 주입 (DI)
```java
@Controller
public class MemberController {
    private final MemberService memberService;
    
    // 스프링 컨테이너가 MemberService를 찾아서 자동 주입
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
}
```
- 3. Bean 생명주기 관리
```java
// 객체 생성 → 의존성 주입 → 초기화 → 사용 → 소멸
// 모든 과정을 스프링 컨테이너가 관리
```
### 스프링 컨테이너의 종류
- 1. BeanFactory - 기본 컨테이너
- 2. ApplicationContext - 실무에서 사용 (BeanFactory + 추가 기능)
```java
// 스프링 부트 실행 시
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // 이 순간 ApplicationContext(스프링 컨테이너) 생성
        SpringApplication.run(Application.class, args);
    }
}
```
### 컨테이너에 등록되는 것들
```java
// 1. 직접 등록
@Component, @Service, @Repository, @Controller

// 2. 설정으로 등록
@Configuration
public class AppConfig {
    @Bean
    public MemberService memberService() {
        return new MemberService();
    }
}

/*
3. 스프링이 자동 등록
- HttpMessageConverter
- ViewResolver
- HandlerMapping
- 기타 스프링 인프라 빈들
 */
```
- 스프링 컨테이너 = 객체 공장 + 관리자
- 내가 new로 생성 X
- 스프링이 대신 생성하고 관리
- 필요한 곳에 자동으로 주입
- 싱글톤으로 관리 (기본)