아래 코드는 **Spring Framework**에서 국제화(i18n)나 메시지 관리용으로 사용하는 **MessageSource 빈(bean)**을 등록하는 설정이야.
어떤 역할을 하는지, 어디에 작성하는 코드인지 차근차근 설명해줄게.

---

# 1. 이 코드가 하는 일

### **MessageSource 빈 등록**

```java
@Bean
public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasenames("messages", "errors");
    messageSource.setDefaultEncoding("utf-8");
    return messageSource;
}
```

이 코드의 의미:

### 1) `ResourceBundleMessageSource`

* `messages.properties`, `errors.properties` 같은 **메시지 파일을 읽는 도구**야.
* `setBasenames("messages", "errors")`
  → `classpath`에서

    * `messages.properties`
    * `errors.properties`
      를 읽어오겠다는 의미.
* 다국어 지원을 위해

    * `messages_ko.properties`
    * `messages_en.properties`
      같은 것도 자동으로 인식해.

### 2) `setDefaultEncoding("utf-8")`

* 메시지 파일에서 한글 깨지는 것 방지.

### 3) `@Bean`

* 스프링 컨테이너에 **MessageSource 객체를 빈으로 등록**한다는 뜻.

---

# 2. **이 코드를 언제 필요로 해?**

Spring Boot는 기본적으로 `messages.properties`를 자동 로딩해주지만, 아래 상황에서 직접 설정해야 해.

### ▷ 필요한 경우

* 메시지 파일 이름을 `messages` 외의 것으로 추가하고 싶은 경우
  (여기선 `errors.properties` 추가함)
* Spring Boot가 아닌 **Spring MVC** 기반 프로젝트일 때
* 메시지 인코딩을 명시적으로 지정하려고 할 때
* 메시지를 여러 개 그룹으로 나누고 싶을 때

---

# 3. **이 코드를 어디에 적어?**

### 보통 위치:

```
src/
 └─ main/
     └─ java/
         └─ com.example.config/
              └─ WebConfig.java
```

### 예시

```java
@Configuration
public class WebConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "errors");
        messageSource.setDefaultEncoding("utf-8");
        return messageSource;
    }
}
```

즉, **스프링 설정 파일(@Configuration 클래스)에 넣는 코드**야.

---

# 4. 사용 예

컨트롤러에서:

```java
@Autowired
private MessageSource messageSource;

public void test() {
    String msg = messageSource.getMessage("user.notfound", null, Locale.KOREA);
    System.out.println(msg);
}
```

`messages_ko.properties` 안에:

```
user.notfound=사용자를 찾을 수 없습니다
```

이렇게 하면 해당 문자열을 가져올 수 있어.

---

# 5. 정리

| 내용    | 설명                             |
| ----- | ------------------------------ |
| 코드 역할 | 메시지 파일 로딩용 MessageSource 빈 등록  |
| 언제 사용 | 메시지 관리, i18n(국제화), 에러 메시지 처리 등 |
| 어디 작성 | @Configuration이 붙은 설정 클래스 내부   |
| 왜 필요  | Boot가 제공하는 기본값을 커스터마이즈하고 싶을 때  |

---

원하면

* 메시지 파일 작성 방법
* 국제화(i18n) 적용하는 방법
* Spring Boot에서 기본 설정과의 차이
  등도 더 이어서 설명해줄게.

좋아. 두 줄이 **정확히 어떤 의미인지**, 내부적으로 어떻게 동작하는지까지 더 깊게 설명해줄게.

---

# 1. `messageSource.setBasenames("messages", "errors");`

이 코드는 **스프링이 읽게 될 메시지 파일의 “이름”을 등록하는 작업**이야.

### 1) Basename이란?

`messages`라고 적으면
스프링은 클래스패스(classpath)에서 다음 형태의 파일들을 **모두 찾는다**.

```
messages.properties
messages_ko.properties
messages_en.properties
messages_ja.properties
...
```

즉, **확장된 언어 코드가 붙은 모든 파일을 하나의 메시지 그룹으로 간주**한다.

### 2) 여러 개 등록하면 어떻게 되나?

```java
setBasenames("messages", "errors")
```

이 말은 스프링에게 다음 두 그룹을 모두 로딩하라는 의미야.

* `messages*.properties`
* `errors*.properties`

예를 들어, 프로젝트 안에 이런 파일이 있으면 모두 읽힌다:

```
src/main/resources/messages.properties
src/main/resources/messages_ko.properties
src/main/resources/errors.properties
src/main/resources/errors_en.properties
```

### 3) 메시지 충돌 시 우선순위

Spring은 basenames 순서대로 읽기 때문에:

```java
("messages", "errors")
```

이면

1. `messages` 파일에서 키 찾기
2. 없으면 `errors` 파일에서 찾기

이런 순서로 조회한다.

---

# 2. `messageSource.setDefaultEncoding("utf-8");`

이 코드는 **메시지 파일을 읽을 때 사용할 문자 인코딩을 지정**하는 작업이야.

### 왜 필요할까?

기본적으로 자바의 `ResourceBundle`은 ISO-8859-1 인코딩을 사용한다.
이 상태에서 한국어가 들어 있는 `.properties` 파일을 읽으면 한글이 깨진다.

예를 들어:

```
hello=안녕하세요
```

→ 인코딩 지정이 없으면 문자열이 깨져서 읽힘.

그래서 UTF-8로 명시해주는 것.

---

# 3. 내부 동작 흐름(조금 더 깊게)

### `setBasenames()`

* 스프링이 `ResourceBundleMessageSource`로 메시지를 로딩할 때
  Basename들을 기준으로 ResourceBundle을 생성한다.
* 각 Basename마다 `Locale`에 따라 파일이 결정된다.
* 요청 시 `getMessage(key, args, locale)`로 메시지 추출한다.

### `setDefaultEncoding()`

* `Locale`에 맞는 `.properties` 파일을 찾은 후
  **해당 파일을 UTF-8로 읽어들이는 InputStreamReader를 사용하도록 설정**한다.

즉, 단순한 옵션처럼 보이지만, 실제로는 파일 읽기 방식 전체를 바꾸는 설정이다.

---

# 4. 정리

| 코드                                   | 의미                                                                 |
| ------------------------------------ | ------------------------------------------------------------------ |
| `setBasenames("messages", "errors")` | `messages*.properties`, `errors*.properties` 파일들을 메시지로 읽도록 스프링에 등록 |
| Basename 순서                          | 앞에 있는 파일에서 먼저 키 조회, 없으면 뒤에서 조회                                     |
| `setDefaultEncoding("utf-8")`        | 메시지 파일의 인코딩을 UTF-8로 읽게 설정 → 한글 깨짐 방지                               |

---

원하면

* messages.properties 파일 관리 잘하는 방법
* 국제화(i18n) 실제 적용 예시(Controller/HTML)
* Spring Boot에서는 왜 기본 메시지가 자동 설정되는지

이런 것도 더 설명해줄게.

정확히 짚어줄게.

# 결론부터 말하면

**파일은 자동으로 생성되지 않는다.
직접 만들어야 한다.
코드를 바꿔도 컴파일 후 자동 생성되는 과정은 없다.**

이 두 줄은 **"이런 이름의 파일을 읽어라"**라고 스프링에 알려주는 것뿐이야.

```
messageSource.setBasenames("messages", "errors");
```

그래서 스프링은 다음 파일들을 찾는다.

* messages.properties
* messages_ko.properties
* messages_en.properties
* errors.properties
* errors_ko.properties
* errors_en.properties
  ...

**하지만 이 파일들을 직접 만들어두지 않으면 에러가 나거나 값이 비어 있게 돼.**

---

# 1. 파일을 직접 만들어야 한다

보통 다음 위치에 만든다:

```
src/main/resources/messages.properties
src/main/resources/errors.properties
```

예시:

### messages.properties

```
hello=Hello
welcome=Welcome
```

### errors.properties

```
error.notfound=Data not found
error.server=Server error
```

이렇게 직접 만들어서 key=value 형태로 채워줘야 스프링이 읽는다.

---

# 2. 스프링이 자동으로 메시지 파일을 생성해주지 않는다

Spring Boot도 마찬가지야.

* `messages.properties`
* `errors.properties`

같은 파일을 자동으로 만들어주지 않고
**네가 만들 파일을 읽기만 하는 구조**야.

---

# 3. 컴파일한 후에 다시 코드 작성 필요 없음

메시지 파일은 애플리케이션 실행 중에도 읽힌다.
다만:

* 배포한 jar 안에 포함돼야 하므로
* 실제 서버에 올라가려면 파일이 존재해야 함

즉, **코드 작성 → 메시지 파일 생성 → 빌드/배포** 이 순서면 충분해.

컴파일 후에 코드를 다시 작성해야 하는 과정은 전혀 없고,
메시지 파일만 잘 만들어 두면 정상적으로 동작해.

---

# 4. 파일이 없으면 어떻게 되나?

### 상황 A — 메시지 파일이 아예 없음

키를 찾으면 예외 발생:

```
NoSuchMessageException
```

### 상황 B — 일부 키만 없음

메시지 파일 있음, 키는 없음
→ 기본적으로 예외 발생

(옵션에 따라 메시지 키 자체를 출력하기도 함)

---

# 5. 정리

| 질문                | 답                         |
| ----------------- | ------------------------- |
| 파일 자동 생성되나?       | 자동 생성되지 않음                |
| 직접 작성해야 하나?       | 예, 반드시 직접 만들어야 함          |
| 컴파일 후 다시 작성해야 하나? | 불필요함                      |
| 코드가 하는 일          | 특정 이름의 메시지 파일을 읽도록 등록하는 것 |

---

원하면

* message 파일 구조 예시
* 다국어(i18n) 구조 예시
* Spring Boot에서 메시지 관리 팁

같은 것도 보여줄게.

스프링 부트에서 **메시지 소스(MessageSource)** 를 설정하는 방법은 크게 두 가지입니다.

1. **기본 설정 사용 (application.properties/yml 활용)**
2. **Java Config로 직접 Bean 등록**

아래에 두 방식 모두 정리해드립니다.

---

## 1. 기본 설정 사용 (application.yml / application.properties)

스프링 부트는 기본적으로 `messages.properties` 파일을 메시지 소스로 사용합니다.
파일 위치는 `src/main/resources/messages.properties`.

### application.yml 예시

```yaml
spring:
  messages:
    basename: messages, errors   # 여러 파일 지정 가능 (messages.properties, errors.properties)
    encoding: UTF-8
    fallback-to-system-locale: false
```

### application.properties 예시

```properties
spring.messages.basename=messages,errors
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
```

**파일 구조 예시**

```
src/main/resources/
 ├─ messages.properties
 ├─ messages_ko.properties
 └─ errors.properties
```

---

## 2. Java Config로 직접 MessageSource Bean 등록

스프링 부트의 자동 설정을 커스터마이징하고 싶다면 `@Configuration` 클래스를 만들어 직접 설정할 수 있습니다.

```java
@Configuration
public class MessageSourceConfig {

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages", "errors");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}
```

스프링 부트는 `MessageSource` 빈이 등록되면 자동 설정을 무시하고 **직접 등록한 설정을 사용**합니다.

---

## 3. 설정 후 메시지 사용 예시

### 컨트롤러 혹은 서비스에서

```java
@Autowired
private MessageSource messageSource;

public void test() {
    String msg = messageSource.getMessage("hello", null, Locale.KOREA);
    System.out.println(msg);
}
```

### messages.properties

```
hello=안녕하세요
```

---

## 4. 국제화(i18n) 적용 시

웹에서 언어 변경을 허용하려면 `LocaleResolver` 와 `LocaleChangeInterceptor` 를 설정합니다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREA);
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // ?lang=en
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

브라우저 요청 예시

```
GET /home?lang=en
```

---

원하시면 아래도 추가 설명 드릴 수 있습니다.

* 메시지 파일 캐싱 설정 (개발 시 자동 리로드)
* Validation 메시지와 통합하는 방법
* Thymeleaf에서 #{key} 형태로 사용하기
* 국제화 구조 예시 프로젝트 템플릿

필요한 부분 말씀해주시면 이어서 자세히 설명드리겠습니다.

`spring.messages.basename=messages,config.i18n.messages`
이 설정은 **스프링 부트가 사용할 메시지 파일들의 기본 이름(basename)을 여러 개 지정한 것**입니다.

즉, 스프링이 메시지를 찾을 때 아래 두 경로에서 읽어오도록 설정한 것입니다.

1. `messages`
2. `config/i18n/messages`

정확히 어떻게 해석되는지 아래에서 설명드립니다.

---

# 1. basename의 의미

`basename` 은 메시지 파일의 **prefix(경로 포함)** 을 뜻합니다.
확장자(.properties)는 자동으로 붙습니다.

따라서 아래 설정

```
spring.messages.basename=messages,config.i18n.messages
```

은 다음 파일들을 메시지 소스로 사용하라는 의미입니다.

---

# 2. 실제로 로딩되는 파일들

스프링은 각 basename에 대해 **Locale별 변형 파일을 모두 자동으로 탐색합니다.**

## 첫 번째 basename: `messages`

찾는 파일 예:

```
classpath:/messages.properties
classpath:/messages_ko.properties
classpath:/messages_en.properties
...
```

## 두 번째 basename: `config.i18n.messages`

`.` 은 디렉토리 구분자로 해석되므로 실제 경로는:

```
config/i18n/messages.properties
config/i18n/messages_ko.properties
config/i18n/messages_en.properties
...
```

예시 경로 (Spring Boot 기본 resource root 기준):

```
src/main/resources/config/i18n/messages.properties
src/main/resources/config/i18n/messages_ko.properties
...
```

---

# 3. 여러 basename을 지정한 이유

보통 아래와 같은 경우에 여러 basename을 사용합니다.

### 1) 메시지를 역할/도메인별로 분리하고 싶을 때

* `messages.properties` → 기본 메시지
* `config/i18n/messages.properties` → 국제화 리소스를 구조적으로 정리

### 2) 라이브러리별 메시지와 프로젝트 메시지를 분리할 때

```
messages.properties           // 공통 메시지
config/i18n/messages.properties // 화면/UI 국제화 메시지
```

### 3) 여러 프로젝트 구조를 통합할 때

기존 메시지 파일을 그대로 두면서 새로운 경로에 추가 메시지 파일을 배치할 수 있습니다.

---

# 4. 우선순위

여러 basename을 지정했다면 **앞에 선언된 파일이 먼저 로딩되며**, 같은 키가 중복될 경우 **가장 마지막 basename이 우선합니다.**

즉, 아래의 경우:

```
spring.messages.basename=messages,config.i18n.messages
```

`config.i18n.messages` 가 `messages` 보다 높은 우선순위를 가집니다.

---

필요하시면 다음도 설명드릴 수 있습니다.

* 메시지 파일 병합 방식(override 규칙)
* 스프링 부트가 basename을 실제로 탐색하는 경로 구조
* yml로 동일하게 설정하는 방법
* 프로젝트에서 다국어 구조를 정리하는 베스트 프랙티스

어떤 부분이 더 궁금하신가요?
