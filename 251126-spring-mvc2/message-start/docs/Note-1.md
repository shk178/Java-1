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
