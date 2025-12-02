핵심부터 말하면:

**세 메서드 모두 String → One → Two “체인 변환”이 동작한다.**
다만 “값을 어디서 읽느냐(어떤 ArgumentResolver가 동작하느냐)”가 다를 뿐이다.

아래에서 각 방식의 동작 차이를 정확하게 설명할게.

---

# 전체 요약

| 메서드                                  | 파라미터 값의 출처                          | 어떤 ArgumentResolver?                 | Converter 체인 작동 여부     |
| ------------------------------------ | ----------------------------------- | ------------------------------------ | ---------------------- |
| chainPath1 (@RequestParam Two two)   | 쿼리 파라미터 (?two=...)                  | RequestParamMethodArgumentResolver   | **작동함**                |
| chainPath2 (@PathVariable Two two)   | PathVariable (/{two})               | PathVariableMethodArgumentResolver   | **작동함**                |
| chainPath3 (@ModelAttribute Two two) | 쿼리 파라미터(필드 단위), PathVariable(필드 단위) | ModelAttributeMethodArgumentResolver | **기본적으로 작동하지만 방식이 다름** |

주의:
chainPath3는 **기본적으로 ConversionService 체인을 직접 사용하지 않는다.**
대신 WebDataBinder가 필드 단위 변환을 수행한다.
필요하면 String → Two 전체 변환도 가능하지만 기본 방식은 다르다.

---

# 1) chainPath1: @RequestParam Two two

```java
@GetMapping("/chain-1/{two}")
public Two chainPath1(@RequestParam Two two) {
    return two;
}
```

HTTP 요청:

```
GET /chain-1/aaa?two=oneInt:1,oneInteger:10,oneString:ABC
```

동작:

* RequestParamMethodArgumentResolver 동작
* “two” 쿼리 파라미터 값(문자열)을 ConversionService에 전달
* ConversionService 는 String → One → Two 변환 체인을 자동 수행

즉,
**String → One → Two 체인이 100% 명확히 사용되는 타입 변환 방식**이다.

---

# 2) chainPath2: @PathVariable Two two

```java
@GetMapping("/chain-2/{two}")
public Two chainPath2(@PathVariable Two two) {
    return two;
}
```

HTTP 요청:

```
GET /chain-2/oneInt:1,oneInteger:10,oneString:ABC
```

동작:

* PathVariableMethodArgumentResolver 동작
* PathVariable 문자열 값을 ConversionService로 전달
* ConversionService 가 String → One → Two 변환 체인을 수행

즉,

**PathVariable 에서도 String → One → Two 체인이 그대로 동작한다.**

→ chain1과 동일하게 ConversionService 기반 변환.

---

# 3) chainPath3: @ModelAttribute Two two

```java
@GetMapping("/chain-3/{two}")
public Two chainPath3(@ModelAttribute Two two) {
    return two;
}
```

기본 요청 방식:

```
GET /chain-3/xxx?twoInt=1&twoInteger=10&twoString=ABC
```

기본 바인딩 동작:

* ModelAttributeMethodArgumentResolver 동작
* WebDataBinder 가 객체를 생성하고 필드 단위로 바인딩

    * String → int
    * String → Integer
    * String → String
* ConversionService는 각 “필드 타입 변환”에서만 사용됨
  (String → int 같은 기본 변환)

여기서 중요한 점:

**ModelAttribute는 문자열 하나 전체를 Two로 변환하는 Converter<String, Two>를 기본적으로 사용하지 않는다.**

즉,

* chainPath1, chainPath2: “전체 문자열 변환”
* chainPath3: “필드 단위 바인딩”

→ 두 방식은 전혀 다르다.

---

## 그럼 chainPath3 도 체인이 사용되도록 만들 수 있을까?

가능하지만 다음 조건이 필요하다:

* HTTP 요청이 `@ModelAttribute Two two` 를 “단일 값”으로 받도록
* 즉,

  ```
  /chain-3/{two}
  ```

  에서 {two} 에 해당하는 문자열이 두 전체를 의미해야 한다.

예:

```
/chain-3/oneInt:1,oneInteger:10,oneString:ABC
```

이 경우:

* {two} 는 전체 문자열
* ModelAttributeResolver 는 “pathvariable 두 값을 Two 객체로 변환할 수 있는지 체크”
* ConversionService 가 호출됨 (String → Two)

즉,

**chainPath3도 특정 형태에서는 체인이 작동한다.**
(PathVariable 값으로 바인딩되는 경우)

그러나
일반적인 사용 방식(각 필드를 쿼리 파라미터로 받는 경우)에서는 체인이 사용되지 않는다.

---

# 최종 정리

| 메서드                                  | 변환 과정                    | ConversionService 체인 사용 여부 |
| ------------------------------------ | ------------------------ | -------------------------- |
| chainPath1 (@RequestParam Two two)   | 문자열 전체를 Two로 변환          | **항상 사용**                  |
| chainPath2 (@PathVariable Two two)   | PathVariable 전체를 Two로 변환 | **항상 사용**                  |
| chainPath3 (@ModelAttribute Two two) | 필드 단위 바인딩                | 기본적으로 **사용 안 함**           |
| chainPath3 (PathVariable 기반)         | 전체 문자열을 Two 로 변환         | **사용됨**                    |

---

# 결론

**String → One → Two 체인은 다음 두 형태에서 확실히 작동한다.**

* @RequestParam Two two
* @PathVariable Two two

**@ModelAttribute Two two 는 “필드 기반 바인딩”이 기본이라 체인을 사용하지 않는다.**
하지만 PathVariable 기반으로 전달하면 ConversionService를 사용할 수도 있다.

---

원하면
세 방식이 실제로 어떻게 내부에서 작동하는지
ArgumentResolver 단계-by-단계 흐름도까지 그려서 설명해줄게.

지금 보내준 요청 URL:

```
http://localhost:8080/chain-1/aaa?two=oneInt:1,oneInteger:10,oneString:ABC
```

이 URL 자체는 **문법적으로 문제없다.**
그런데 이 요청에서 에러가 나는 건 **코드 구조가 서로 맞지 않기 때문**이다.

당신의 코드:

```java
@GetMapping("/chain-1/{two}")
public Two chainPath1(@RequestParam Two two) {
    return two;
}
```

여기서 중요한 충돌이 있다.

---

# 1) @PathVariable {two} 를 선언해놓고 파라미터로는 @RequestParam Two two 사용 중

다음 부분이 문제다:

```java
@GetMapping("/chain-1/{two}")
                ↑ PathVariable two 선언됨
```

그러면 스프링은 이렇게 기대한다:

```
/chain-1/{two}
```

즉:

```
/chain-1/oneInt:1,oneInteger:10,oneString:ABC
```

그런데 실제 요청은 이렇게 보냈다:

```
/chain-1/aaa?two=...
```

여기서 문제:

* 경로 변수 `{two}` 는 `aaa` 로 매핑됨
* 메서드 파라미터 `@RequestParam Two two` 는 쿼리 파라미터로 매핑됨
* 결국 메서드 시그니처에서는 “경로 변수 Two”는 바인딩할 곳이 없음
  → 스프링은 이를 **핸들링 불가한 요청 구조**라고 판단하고 에러를 띄운다

즉,

**URL은 PathVariable + RequestParam 두 개를 모두 요구하는데,
메서드는 RequestParam 하나만 요구하고 있어서 충돌 발생.**

---

# 2) 해결 방법 ①: PathVariable 을 제거

chain-1 은 RequestParam 전용이었으니까 이렇게 고치면 된다:

```java
@GetMapping("/chain-1")
public Two chainPath1(@RequestParam Two two) {
    return two;
}
```

이제 URL도 이렇게 쓰면 된다:

```
http://localhost:8080/chain-1?two=oneInt:1,oneInteger:10,oneString:ABC
```

이 구조가 **정확히 ConversionService 체인이 작동하는 구조**다.

---

# 3) 해결 방법 ②: PathVariable 로 받기 (chain-2 스타일)

만약 PathVariable 을 사용하려면 다음처럼 해야 한다:

```java
@GetMapping("/chain-1/{two}")
public Two chainPath1(@PathVariable Two two) {
    return two;
}
```

그리고 URL도 이렇게 보내야 한다:

```
/chain-1/oneInt:1,oneInteger:10,oneString:ABC
```

그러면 체인이 동작한다.

---

# 4) 정리

현재 에러의 직접 원인:

**URL은 PathVariable + RequestParam 조합인데,
메서드는 PathVariable 을 처리하지 않아서 매핑이 깨짐.**

운영 가능한 조합은 두 가지뿐이다:

### ① RequestParam 방식

```java
@GetMapping("/chain-1")
public Two chainPath1(@RequestParam Two two)
```

URL:

```
/chain-1?two=...
```

### ② PathVariable 방식

```java
@GetMapping("/chain-1/{two}")
public Two chainPath1(@PathVariable Two two)
```

URL:

```
/chain-1/{two}
```

둘 중 하나를 선택해야 한다.

---

원하면

* 세 방식 전체를 하나의 컨트롤러 예제로 다시 정리해서
* 올바른 URL과 실제 변환 흐름
  을 깔끔하게 표로 정리해줄 수도 있다.

URL, 코드 모두 맞는데 **500 에러**가 난다는 건
이제 “요청은 매핑되지만, ‘타입 변환 과정에서 예외가 발생’했다”는 의미다.

즉,
컨트롤러는 정상 선택되지만
`@RequestParam Two two` → `ConversionService.convert()` 과정에서 실패한 것이다.

500이 나는 가장 흔한 원인은 아래 네 가지이며, 거의 100% 이 안에서 해결된다.

---

# 1) String → One 변환기에서 예외 발생

당신이 만든 String → One 변환기 (`StringToOneConverter`)에서
다음 부분에서 예외가 가장 흔히 난다.

```java
String[] kv = part.split(":");
String key = kv[0];
String value = kv[1];  // ← 여기가 NPE 또는 IndexOutOfBounds 예외 자주 발생
```

**개선 포인트: ‘key:value’ 구조가 아닐 때 Index 오류가 발생.**

예:

```
oneInt:
```

이런 값이 들어오면 value가 없음.

또는:

```
oneInt:1,oneInteger:10,oneString
```

이런 식이면 kv[1]이 없음 → 예외 발생 → 500

**실제 URL에서 콤마나 콜론 주변 공백이 있어도 똑같이 터짐.**

확실한 디버깅 방법:

StringToOneConverter 내부에 로그를 찍어보면 바로 원인이 나온다.

---

# 2) URL 인코딩 문제 (현재 가장 가능성 높음)

URL을 다시 보자:

```
http://localhost:8080/chain-1?two=oneInt:1,oneInteger:10,oneString:ABC
```

여기서 `:` 와 `,` 는
HTTP 스펙상 **특수 문자**로 인식될 수 있다.

브라우저에 따라서는
파라미터 값에서 `,` 또는 `:` 가 잘려서 들어오거나
의도하지 않은 인코딩으로 들어올 수 있다.

즉,

스프링은 다음 문자열이 들어왔다고 생각하지 않는다:

```
oneInt:1,oneInteger:10,oneString:ABC
```

대신, 어떤 부분이 누락되거나 깨진 값이 들어온다.

그러면 변환기 내부 파싱에서 예외 발생 → 500

### 해결 방법

정상 인코딩 버전으로 호출:

```
oneInt%3A1%2ConeInteger%3A10%2ConeString%3AABC
```

즉:

* `:` → `%3A`
* `,` → `%2C`

전체 URL:

```
http://localhost:8080/chain-1?two=oneInt%3A1%2ConeInteger%3A10%2ConeString%3AABC
```

→ 이러면 거의 100% 정상 동작한다.

실제로 많은 개발자가 이 문제로 500 발생을 겪는다.

---

# 3) 변환기 등록 순서 / 등록 누락

WebConfig 의 addFormatters 가 누락되거나
컴포넌트 스캔 경로 밖에 있으면
변환기가 등록되지 않는다.

확인:

* WebConfig 가 @Configuration 붙어 있는가?
* WebMvcConfigurer 를 제대로 구현했는가?
* 스프링이 WebConfig 를 읽고 있는가?

이게 빠지면 ConversionService 는 String → Two 시도하다가
“String→One 변환기를 찾지 못해서” TypeMismatchException 발생 → 500

---

# 4) One → Two 변환기 내부에서 NPE 발생

예:

```
two.setTwoString(one.getOneString() + one.getOneString());
```

만약 one.getOneString() 이 null 이면 NPE 발생 → 500

혹은
oneInt, oneInteger 가 파싱 오류 나면서 null 값이 들어올 때도 동일.

---

# 결론: 가장 가능성이 높은 1, 2 순서

## (최우선 문제)

브라우저가 `two=oneInt:1,oneInteger:10,oneString:ABC` 를
깨진 값으로 전달해 StringToOneConverter 내부 split 로직에서 예외가 발생 → 500

### 반드시 아래처럼 URL 인코딩 버전으로 호출해봐라:

```
http://localhost:8080/chain-1?two=oneInt%3A1%2ConeInteger%3A10%2ConeString%3AABC
```

→ 이거로 정상 동작하면 “원인 = URL 인코딩”이 확정이다.

---

원하면

* Converter 내부에 안전한 split 로직 구현
* JSON 기반으로 전체 구조 통일
* URL-safe 포맷 설계
  같은 방식까지 모두 만들어줄게.

좋은 지점에서 막혔어. 여기서 한 번 정리해서 갈 필요가 있다.

## 0. 먼저, 내 이전 설명 정정할 부분

내가 전에
“String → One, One → Two 두 개의 Converter만 등록해도 Spring이 자동으로 String → Two 체인 변환을 해준다”
라고 말했는데, **이 부분은 잘못된 설명**이었어.

`ConversionService`는 **중간 타입을 자동으로 찾아서 여러 단계를 거치는 변환을 하지 않는다.**
`String -> Two` 변환을 하려면 **직접 그 쌍에 대한 Converter가 있어야** 한다.

그래서 지금 상황이 이렇게 돼:

* 등록해 둔 것: `Converter<String, One>`, `Converter<One, Two>`
* 스프링 MVC가 필요한 것: `String → Two` (왜냐면 `@RequestParam Two two`)

→ 해당 쌍에 대한 Converter가 없어서 `ConverterNotFoundException` 터지고, 그게 500 에러로 보이는 것.

이제 본론으로.

---

## 1. 왜 이 코드에서 500이 나는가?

```java
@GetMapping("/chain-1")
public Two chainPath1(@RequestParam Two two) {
    return two;
}
```

요청:

```text
http://localhost:8080/chain-1?two=oneInt%3A1%2ConeInteger%3A10%2ConeString%3AABC
```

스프링의 내부 동작:

1. `two` 쿼리 파라미터 값을 `String`으로 읽음
2. 파라미터 타입이 `Two` 이므로,
   `ConversionService` 에게 **“String → Two로 바꿔줄 수 있냐?”** 묻는다
3. ConversionService는 등록된 Converter 목록을 보고

  * `String → One` 있음
  * `One → Two` 있음
  * **하지만 `String → Two` 는 없음**
4. 그래서 `No converter found capable of converting from type [java.lang.String] to type [패키지.Two]`
   같은 예외를 던짐 → 500 에러

**핵심:**
스프링은 `String → One → Two` 같은 여러 단계 변환을 자동으로 안 한다.
**항상 “한 번에 한 쌍”**만 본다.

---

## 2. 해결 방법 1: `String → Two` Converter를 직접 만든다 (가장 직관적인 방법)

지금처럼 `@RequestParam Two two` 를 계속 쓰고 싶다면
**반드시 `Converter<String, Two>` 를 구현해야** 한다.

```java
public class StringToTwoConverter implements Converter<String, Two> {

    @Override
    public Two convert(String source) {
        // source 예: "oneInt:1,oneInteger:10,oneString:ABC"
        Two two = new Two();

        String[] parts = source.split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            if (kv.length != 2) {
                throw new IllegalArgumentException("잘못된 포맷: " + part);
            }
            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case "oneInt" -> two.setTwoInt(Integer.parseInt(value));
                case "oneInteger" -> two.setTwoInteger(Integer.parseInt(value));
                case "oneString" -> two.setTwoString(value);
                default -> throw new IllegalArgumentException("알 수 없는 키: " + key);
            }
        }
        return two;
    }
}
```

등록:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToTwoConverter());
        // StringToOne, OneToTwo는 이 케이스에선 필요 없음 (있어도 상관 X)
    }
}
```

이제야 비로소:

```java
@GetMapping("/chain-1")
public Two chainPath1(@RequestParam Two two) {
    return two;
}
```

가 제대로 동작한다.

---

## 3. 해결 방법 2: “String → One” + “One → Two” 두 단계는 **직접 호출**로 체인한다

자동 체인이 안 되니까,
**코드에서 직접 한 번 더 convert를 호출해서 체인**해줘야 한다.

예를 들어:

```java
@RestController
public class ConvertController {

    private final ConversionService conversionService;

    public ConvertController(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @GetMapping("/chain-1")
    public Two chainPath1(@RequestParam String two) {
        // 1단계: String → One
        One one = conversionService.convert(two, One.class);

        // 2단계: One → Two
        Two result = conversionService.convert(one, Two.class);

        return result;
    }
}
```

이 경우에는:

* `Converter<String, One>`
* `Converter<One, Two>`

두 개만 등록하면 되고,
**체인 자체는 내가 직접 호출해서 구성**하는 방식이야.

---

## 4. 그럼 `@PathVariable Two two`, `@ModelAttribute Two two` 는?

* `@RequestParam Two two`
* `@PathVariable Two two`

둘 다 결국 내부적으로 **“String → Two”** 를 원해.
그래서 두 경우 모두 **직접적인 `Converter<String, Two>` 가 필요**해.

`@ModelAttribute Two two` 는 기본적으로:

* `Two` 객체를 new 한 뒤
* 각 필드에 `String → 필드 타입`으로 바인딩하는 방식이라
  전체 String → Two Converter를 쓰지 않고,
  **필드 단위 기본 변환기**를 사용한다고 보면 된다.

---

## 5. 정리

1. 지금 500 에러 이유

  * `@RequestParam Two two` 때문에
    스프링이 **String → Two** 변환을 시도
  * 하지만 그 쌍에 대한 Converter가 없음
  * 그래서 500 (대부분 `ConverterNotFoundException` or `TypeMismatchException`)

2. **ConversionService는 중간 타입을 거치는 다단계 체인을 자동으로 하지 않는다.**

  * `String → One`, `One → Two` 둘 다 있어도
  * 자동 `String → Two` 변환 안 됨

3. 선택지

  * `Converter<String, Two>` 직접 구현해서 등록
    → `@RequestParam Two two`, `@PathVariable Two two` 자동 변환
  * 또는 `String → One`, `One → Two` 만 두고
    컨트롤러에서 `conversionService.convert()` 두 번 직접 호출해서 체인

---

지금 코드/설계 의도에 더 맞는 쪽이 뭔지 말해주면,
그 방향으로 전체 예제(컨트롤러 + Converter + URL 예시) 한 번에 깔끔하게 다시 묶어서 정리해줄게.
