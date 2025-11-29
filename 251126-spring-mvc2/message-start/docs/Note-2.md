아래 코드는 **스프링의 MessageSource 기능이 정상적으로 동작하는지 검증하는 테스트 클래스**입니다.
각 테스트가 무엇을 확인하는지 하나씩 설명해드리겠습니다.

---

# 전체 요약

`MessageSourceTest` 클래스는 다음 세 가지를 검증합니다.

1. 메시지 키 `"hello"` 를 읽어서 올바른 값 `"안녕"` 을 리턴하는지
2. 존재하지 않는 키 `"no_code"` 요청 시 예외가 발생하는지
3. 존재하지 않는 키를 요청하더라도 **기본 메시지(defaultMessage)** 를 제공하면 그 값을 반환하는지

즉, **스프링 메시지 국제화(i18n) 기능의 기본 동작을 테스트하는 코드**입니다.

---

# 테스트 코드 상세 설명

```java
@SpringBootTest
public class MessageSourceTest {

    @Autowired
    MessageSource ms;
```

* `@SpringBootTest` : 스프링 부트 컨테이너를 띄워서 실제 빈을 사용해 테스트를 실행합니다.
* `MessageSource` 빈을 주입받습니다.

---

## 1. 메시지 정상 조회 테스트

```java
@Test
void helloMessage() {
    String result = ms.getMessage("hello", null, null);
    assertThat(result).isEqualTo("안녕");
}
```

### 의미

* `"hello"` 라는 메시지 키를 조회
* 두 번째 인자 `null`: 메시지에서 `{0}`, `{1}` 같은 포맷 문자열 대체 값 없음
* 세 번째 인자 `null`: locale이 null이면 기본 locale 사용
* 기대값 `"안녕"`과 일치해야 테스트 성공

### 실제 필요한 메시지 파일 예

```
messages.properties
hello=안녕
```

---

## 2. 존재하지 않는 메시지 조회 시 예외 발생 확인

```java
@Test
void notFoundMessageCode() {
    assertThatThrownBy(() -> ms.getMessage("no_code", null, null))
        .isInstanceOf(NoSuchMessageException.class);
}
```

### 의미

* `"no_code"` 라는 메시지가 메시지 파일에 존재하지 않으면
  `NoSuchMessageException` 이 발생해야 정상

### 확인하는 것

* 메시지 key 없을 때 스프링의 기본 동작이 예외라는 점을 검증

---

## 3. 기본 메시지(defaultMessage) 제공 시 동작 확인

```java
@Test
void notFoundMessageCodeDefaultMessage() {
    String result = ms.getMessage("no_code", null, "기본 메시지", null);
    assertThat(result).isEqualTo("기본 메시지");
}
```

### 의미

* `"no_code"` 가 실제 메시지 파일에 없지만
* 기본 메시지 `"기본 메시지"` 를 지정했기 때문에 그 값을 반환해야 합니다.

### 스프링의 MessageSource 동작 규칙

`getMessage(String code, Object[] args, String defaultMessage, Locale locale)`

* code가 없으면 `defaultMessage` 를 반환
* defaultMessage도 없으면 예외 발생

---

# 정리

| 테스트 이름                                | 의미                            |
| ------------------------------------- | ----------------------------- |
| `helloMessage()`                      | 키 `"hello"` 정상 조회되는지          |
| `notFoundMessageCode()`               | 존재하지 않는 키 요청 → 예외 발생하는지       |
| `notFoundMessageCodeDefaultMessage()` | 키 없을 때 기본 메시지를 넘기면 그 값을 사용하는지 |

---

원하신다면 다음도 추가 설명해드릴 수 있습니다.

* `ms.getMessage()` 파라미터 4개의 의미
* 메시지에서 `{0}`, `{1}` 같은 포맷 사용 예제
* locale 지정해서 언어별 메시지 테스트하는 방법
* MessageSource AutoConfiguration 원리

필요한 부분 알려주세요.

아래 세 가지 내용을 순서대로 정확하게 정리해드립니다.

---

# 1. `ms.getMessage()` 파라미터 4개의 의미

스프링의 `MessageSource.getMessage()` 주요 시그니처:

```java
String getMessage(String code, Object[] args, String defaultMessage, Locale locale)
```

각 파라미터의 의미는 다음과 같습니다.

### 1) `code`

메시지 파일에서 찾을 **메시지 키**
예: `"hello"`, `"error.notfound"`

### 2) `args`

메시지 텍스트 안의 `{0}`, `{1}` 같은 자리 표시자에 넣을 **치환 인자 배열**

예:

```properties
hello.name=안녕하세요 {0}님. 오늘은 {1}입니다.
```

사용:

```java
ms.getMessage("hello.name", new Object[]{"홍길동", "수요일"}, null, null);
```

### 3) `defaultMessage`

* 메시지 파일에 `code`가 없을 때 사용될 **기본 메시지**
* null이면 기본 메시지가 없음을 의미하며 이 경우 예외(`NoSuchMessageException`) 발생

### 4) `locale`

해당 메시지를 가져올 때 사용할 **언어/국가 Locale 정보**
예:

* `Locale.KOREA`
* `Locale.US`
* `Locale.JAPAN`

`null`이면 **서버 기본 Locale**이 사용됩니다.

---

# 2. 메시지에서 `{0}`, `{1}` 같은 포맷 사용 예제

### 메시지 파일 (messages.properties)

```
greeting=안녕하세요 {0}님
order.info=주문 {0}건이 처리되었습니다. 총 금액은 {1}원입니다.
```

### 사용 예제

```java
@Autowired
MessageSource ms;

@Test
void messageFormatTest() {
    // 하나의 치환 값
    String result1 = ms.getMessage("greeting", new Object[]{"홍길동"}, null, Locale.KOREA);
    // 결과: "안녕하세요 홍길동님"

    // 두 개의 치환 값
    String result2 = ms.getMessage("order.info", new Object[]{3, 15000}, null, Locale.KOREA);
    // 결과: "주문 3건이 처리되었습니다. 총 금액은 15000원입니다."
}
```

여기서 `{0}` → 1번째 객체
`{1}` → 2번째 객체
순서대로 치환합니다.

---

# 3. Locale 지정해서 언어별 메시지 테스트하는 방법

예를 들어 두 개의 메시지 파일이 있다고 가정합니다.

### messages.properties (기본)

```
hello=안녕
```

### messages_en.properties

```
hello=hello
```

### messages_ja.properties

```
hello=こんにちは
```

아래처럼 Locale을 지정해 테스트할 수 있습니다.

```java
@Test
void localeTest() {
    // 한국어
    String ko = ms.getMessage("hello", null, Locale.KOREA);
    assertThat(ko).isEqualTo("안녕");

    // 영어
    String en = ms.getMessage("hello", null, Locale.ENGLISH);
    assertThat(en).isEqualTo("hello");

    // 일본어
    String ja = ms.getMessage("hello", null, Locale.JAPAN);
    assertThat(ja).isEqualTo("こんにちは");
}
```

### Locale의 동작 방식

* `Locale.KOREA` → `messages_ko.properties` 찾음
* `Locale.ENGLISH` → `messages_en.properties`
* 해당 Locale 파일이 없으면 **기본 messages.properties** 로 fallback

---

# 정리

### `getMessage()` 파라미터

* `code`: 메시지 키
* `args`: 포맷 치환 인자
* `defaultMessage`: 키 없을 때 기본 메시지
* `locale`: 언어 선택

### 메시지 포맷

* `{0}`, `{1}` → args 배열 순서대로 치환

### Locale 테스트

* `ms.getMessage("key", null, Locale.ENGLISH)` 형태로 지역화 메시지 검증 가능

---

원하시면 아래도 설명해드릴 수 있습니다.

* 메시지 파일 캐싱 끄고 실시간으로 수정 반영하는 방법
* 테스트에서 기본 Locale 변경하는 방법
* Thymeleaf에서 메시지 사용하는 예
* Validation 메시지와 MessageSource 연동하는 법

추가로 궁금한 것 있으신가요?

말씀하신 상황이라면 **메시지 파일의 위치는 맞지만, 스프링 부트가 그 메시지 파일을 읽도록 설정이 되어 있지 않기 때문에 실패한 것**입니다.

핵심은 다음 두 가지 중 하나입니다.

1. `spring.messages.basename` 에 **config/i18n/messages** 가 등록되어 있지 않다
2. 등록은 했지만 **경로 표기 방식이 잘못되었다**

아래에서 정확한 원인과 해결 방법을 정리해드립니다.

---

# 1. `spring.messages.basename` 설정 확인이 필요합니다

지금 메시지 파일 위치는 다음입니다.

```
src/main/resources/config/i18n/messages.properties
```

이 파일을 스프링 부트가 읽으려면 반드시 다음 설정이 필요합니다.

### application.properties

```
spring.messages.basename=config.i18n.messages
```

### application.yml

```yaml
spring:
  messages:
    basename: config.i18n.messages
```

즉, **config.i18n.messages** 라고 정확히 써야 합니다.

---

# 2. 설정이 없으면 스프링은 어디만 읽는가?

스프링 부트의 기본 동작:

* 자동으로 읽는 경로:

  ```
  classpath:/messages.properties
  ```
* 즉, 기본 basename = `messages`

따라서 당신이 작성한 파일:

```
config/i18n/messages.properties
```

은 **기본 규칙에서는 절대 읽히지 않습니다.**

따라서 테스트:

```java
ms.getMessage("hello", null, null)
```

는 `"hello"` 키를 찾다가 실패합니다.

---

# 3. 설정이 올바른지 체크 리스트

아래 항목 중 하나라도 빠지면 메시지가 읽히지 않습니다.

### (1) 파일 위치가 정확한지

```
src/main/resources/config/i18n/messages.properties
```

정확한 경로여야 합니다.

### (2) basename 설정이 정확한지

```
spring.messages.basename=config.i18n.messages
```

여기서 `.` 은 디렉토리 구분자로 해석됩니다.

따라서 스프링은 다음 파일을 찾습니다.

```
classpath:/config/i18n/messages.properties
classpath:/config/i18n/messages_ko.properties
classpath:/config/i18n/messages_en.properties
```

### (3) 파일 인코딩이 UTF-8인지

`안녕` 같은 한글이 들어간 경우 반드시 설정해야 합니다.

```
spring.messages.encoding=UTF-8
```

---

# 4. 설정 예시 (정상적으로 동작하는 형태)

### application.properties

```
spring.messages.basename=config.i18n.messages
spring.messages.encoding=UTF-8
```

메시지 파일:

```
src/main/resources/config/i18n/messages.properties
hello=안녕
```

테스트:

```java
ms.getMessage("hello", null, null)  // "안녕"
```

---

# 5. 그래도 안 된다면 확인해야 할 추가 사항

1. 파일 이름 오타:
   `messages.properties`인지 (`message.properties` 아님)
2. 폴더 이름 오타:
   `config/i18n`인지 (`config/in18` 등 아님)
3. 파일 인코딩이 UTF-8이 맞는지
4. IntelliJ가 리소스를 빌드에 반영했는지

    * Rebuild / Invalidate Caches 필요할 수 있음
5. test 환경에서 `application-test.properties` 로 인해 덮어쓰기 발생 여부

원하시면 위 항목을 함께 점검해드릴 수 있습니다.
지금 사용 중인 `application.properties` 내용을 보여주시면 정확한 원인을 바로 잡아드릴 수 있습니다.

오류 메시지의 핵심은 여기입니다.

```
Expected :"안녕"
Actual   :"??"
```

**"??"** 가 출력되었다는 것은 메시지를 못 찾은 것이 아니라,
**찾았지만 한글이 깨져서 표시되었다**는 뜻입니다.

즉, 메시지 파일은 정상적으로 읽혔습니다.
문제는 **인코딩(UTF-8)** 이 맞지 않아 `"안녕"`이 `"??"` 로 바뀐 것입니다.

이 문제는 100% **메시지 파일 인코딩 설정** 때문입니다.

---

# 원인

스프링 부트는 기본적으로 메시지 파일을 ISO-8859-1(라틴1) 로 해석하려고 합니다.
여기에 한글이 들어가면 전부 `"??"` 로 깨집니다.

따라서 반드시 UTF-8 인코딩을 설정해야 합니다.

---

# 해결 방법

application.properties 또는 application.yml 에 다음을 추가하세요.

```
spring.messages.encoding=UTF-8
```

또는

```yaml
spring:
  messages:
    encoding: UTF-8
```

이 설정이 없으면 한글이 무조건 깨집니다.

---

# 추가적으로 확인해야 할 파일 인코딩 (중요)

IntelliJ 또는 VSCode에서 메시지 파일이 **정말 UTF-8로 저장**되어 있는지도 확인해야 합니다.

### IntelliJ에서 확인 방법

1. 메시지 파일 `messages.properties` 열기
2. 아래 상태바에서 Encoding 확인
3. UTF-8인지 확인
4. 아니라면 UTF-8로 변경 후 `Reload` 또는 `Convert` 클릭

---

# 체크리스트

아래 둘 다 충족되어야 합니다.

### 1) application.properties 설정됨

```
spring.messages.basename=messages,config.i18n.messages
spring.messages.encoding=UTF-8
```

### 2) messages.properties 파일이 UTF-8로 저장됨

(특히 Windows 환경에서는 기본 인코딩이 CP949인 경우가 많습니다.)

---

# 결론

**현재 메시지를 못 찾는 문제가 아니라, 메시지를 제대로 읽어왔지만 인코딩이 깨져서 "??" 로 출력된 것입니다.**

따라서 아래 한 줄을 추가하면 해결됩니다.

```
spring.messages.encoding=UTF-8
```

필요하시면 메시지 파일 인코딩 확인 방법 또는 프로젝트 설정 캡처 보내주시면 함께 점검해드릴 수 있습니다.

지금 문제의 원인이 정확히 드러났습니다.

`messages.properties` 파일의 인코딩이 **ISO-8859-1** 로 되어 있기 때문에
한글 `"안녕"` 이 `"??"` 로 깨진 것입니다.

**해결은 두 단계만 하면 바로 정상 동작합니다.**

---

# 1. 메시지 파일 인코딩을 UTF-8로 변경

IntelliJ 기준으로 설명합니다.

### IntelliJ에서 인코딩 변경 방법

1. `messages.properties` 파일 열기
2. 오른쪽 아래 상태바에서 `ISO-8859-1` 표시 클릭
3. **UTF-8** 선택
4. 팝업에서

    * “Reload” 또는 “Convert”
      둘 중 하나 묻는데, **Convert** 를 선택해야 합니다.
      (Convert = 실제 파일 내용을 UTF-8로 재저장)

※ “Reload” 를 누르면 파일은 여전히 ISO-8859-1로 저장됨 → 문제 해결 안 됨
※ 반드시 “Convert” 선택

---

# 2. application.properties 에 UTF-8 설정 추가

스프링 부트가 메시지 파일을 UTF-8로 읽도록 설정해야 합니다.

```
spring.messages.encoding=UTF-8
```

또는 yml:

```yaml
spring:
  messages:
    encoding: UTF-8
```

---

# 이 두 가지를 반영하면 결과는 이렇게 됩니다

테스트 코드:

```java
String result = ms.getMessage("hello", null, null);
assertThat(result).isEqualTo("안녕");
```

실제 출력:

```
Expected: 안녕
Actual: 안녕
```

---

# 추가 확인 (필요할 수 있는 부분)

* `config/i18n/messages.properties` 파일도 ISO-8859-1 인코딩이면 동일한 문제 발생
* Windows 환경에서 새 properties 파일은 기본 CP949 또는 ISO8859-1로 만들어지는 경우가 있음
* 모든 메시지 파일을 UTF-8로 변환해야 함

---

# 잘 바뀌었는지 확인하려면

파일에 다음처럼 한글을 넣고 저장한 뒤 테스트하세요.

```
hello=안녕하세요
```

테스트 실행 시 그대로 나와야 합니다.

---

필요하다면

* 메시지 파일 전체 인코딩 일괄 변환 방법
* IntelliJ 기본 인코딩 UTF-8로 고정하는 방법
* Gradle 빌드 시 UTF-8 강제 적용 방법
  도 안내해드릴 수 있습니다.

어떤 부분도 이어서 도와드릴까요?
