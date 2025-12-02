`Converter<S, T>`는 **S 타입을 T 타입으로 변환한다**는 의미의 제네릭 인터페이스야.

그래서

```java
public class CustomConverter implements Converter<String, Integer>
```

라고 타입을 지정했다는 것은,

**“String → Integer 로 변환하는 Converter”** 라는 걸 명확하게 선언한 것.

---

## 왜 타입을 지정해야 할까?

### 1) 변환의 입력 타입과 출력 타입을 확실하게 알려주기 위해

스프링은 Converter를 스캔해 등록하면서
“이 Converter는 어떤 타입을 어떤 타입으로 변환하는가?”를 알아야 해.

* 입력 타입(S): `String`
* 출력 타입(T): `Integer`

이 정보가 있어야만,

* HTTP 요청 파라미터를 Integer로 바꾼다거나
* PathVariable string을 Integer로 바꾼다거나
* 바인딩 과정에서 타입 변환을 한다거나

이런 상황에서 이 Converter를 사용할 수 있어.

---

## 2) 제네릭으로 타입을 고정해야 타입 안전성 확보

만약 제네릭 타입을 안 쓰면 어떻게 될까?

```java
class CustomConverter implements Converter { … }
```

이렇게 되어버리면, 변환할 타입을 알 수 없으니 스프링은 Converter를 활용할 수 없고,
당연히 컴파일 시 타입 체크도 불가능해져.

제네릭을 지정함으로써 “이 메서드는 반드시 String을 받아 Integer를 반환한다”고 컴파일 단계에서 보장돼.

---

## 3) convert() 메서드의 파라미터 타입과 반환 타입을 자동으로 결정해줌

`Converter<S,T>` 구조 때문에:

```java
@Override
public Integer convert(String source) {
```

이렇게 자동으로 정확한 타입을 갖게 되는 것.
당신이 직접 String과 Integer라고 선언한 게 아니라,
제네릭 타입을 지정했기 때문에 컴파일러가 정확한 구조를 강제하는 거야.

---

## 4) 스프링이 내부에서 Converter를 사용할 때 필요한 메타정보

스프링의 ConversionService는 내부적으로 이런 맵을 관리해:

```
Map<Pair<SourceType, TargetType>, Converter>
```

여기에 등록하려면 "출발 타입"과 "도착 타입"이 필요하잖아.
그걸 제네릭 타입으로 얻어가는 거야.

---

## 결론

`Converter<String, Integer>`라고 타입을 명시하는 이유는:

* String → Integer로 변환하는 Converter임을 선언하기 위해
* 스프링이 ConversionService에 등록하고 사용할 수 있게 하기 위해
* 컴파일 시 타입 안정성을 확보하기 위해
* convert() 함수 시그니처를 자동으로 맞추기 위해

즉, **"무엇을 무엇으로 바꾸는 컨버터인지 알려주는 설계적 약속"** 이라고 보면 돼.

필요하다면 다른 타입도 만들 수 있어:

```java
Converter<String, LocalDate>
Converter<Integer, String>
Converter<CustomTypeA, CustomTypeB>
```

궁금한 점 더 있어?

결론부터 말하면 **맞아.**
`Converter<S, T>`의 제네릭 덕분에 **컴파일 시점에 `convert()` 메서드의 입력/출력 타입이 반드시 S → T 형태로 맞는지 체크된다.**

---

# 컴파일 시점에서 실제로 어떤 일이 일어날까?

## 1) 제네릭이 메서드 시그니처를 확정해준다

다음 선언을 보면:

```java
class CustomConverter implements Converter<String, Integer>
```

이 순간 컴파일러는 다음과 같이 인식한다:

* `convert()` 메서드의 파라미터 타입: `String`
* `convert()` 메서드의 반환 타입: `Integer`

그래서 당신이 이렇게 작성하면:

```java
@Override
public Integer convert(String source) { ... }
```

타입이 정확히 맞아야 하고,
다르게 쓰면 컴파일 오류가 발생한다.

예를 들어 잘못 쓰면:

### (1) 잘못된 파라미터 타입

```java
@Override
public Integer convert(Object source) { ... }
```

→ 컴파일 오류.
"부모 메서드의 시그니처와 맞지 않는다."

### (2) 반환 타입 잘못됨

```java
@Override
public String convert(String source) {
    return source;
}
```

→ 컴파일 오류.
"반환 타입이 Integer가 아니라고 경고하고 막는다."

결국 **제네릭이 적용된 인터페이스를 구현하는 순간,
메서드 시그니처는 컴파일러가 강제하는 계약(contract)이 되는 것.**

---

# 2) 스프링이 사용하기 전부터 안전하게 제약이 걸림

스프링이 런타임에서 ConversionService에 등록하기 전에도,
컴파일러가 먼저 검사해서 잘못된 타입 사용은 막아버린다.

즉, 스프링은 "잘못된 타입의 Converter"가 애초에 컴파일되지 않기 때문에
안전하게 타입 변환 체계를 구성할 수 있어.

---

# 3) 제네릭을 안 쓰면 어떻게 되는지 비교

만약:

```java
class BadConverter implements Converter
```

이렇게 제네릭을 제거하면:

* `convert(Object source)` 형태로 실행됨
* 반환 타입도 Object
* 스프링이 **어떤 타입 → 어떤 타입 변환인지 파악을 못함**
* ConversionService 등록도 제대로 되지 않음
* 타입 안정성 0

그리고 컴파일러도 타입 체크를 하지 못한다.

---

# 결론

### 네, **컴파일 시점에 `convert()` 메서드의 타입 일치 여부를 체크한다.**

이 제네릭 규칙 덕분에:

* 입력/출력 타입이 강제되고
* 반환 타입 오류를 컴파일 단계에서 걸러내며
* 스프링이 안전하게 Converter를 등록할 수 있다

즉, 제네릭이 Converter 시스템의 기초 안전장치라고 보면 된다.

필요하다면 `ConverterFactory`, `GenericConverter`의 타입 검사 방식도 비교해서 설명해줄 수 있어.

지금 코드가 동작하지 않는 이유는 **스프링 MVC에서 @RequestParam 으로 DTO(객체) 타입을 바로 바인딩하려면 ‘한 개의 String → DTO’ 변환기가 필요하기 때문**이야.

당신이 만든 건:

```java
Converter<One, Two>
```

즉,
“One 객체 → Two 객체” 변환기.

하지만 실제 상황에서 스프링이 해야 하는 일은:

```
쿼리스트링 → Two 객체
"oneInt=2&oneInteger=20&oneString=200" 같은 문자열 → Two
```

즉, **String → Two 변환**이 필요해.

---

# 왜 현재 Converter<One, Two> 는 전혀 사용되지 않을까?

스프링이 @RequestParam Two 를 처리할 때 내부 동작은 다음과 같아:

1. 쿼리 파라미터를 MultiValueMap 형태로 읽음
2. @RequestParam Two two 를 보고 TypeConverter 또는 Binder 를 통해 변환 시도
3. “String → Two” 변환기를 찾음
4. 없음 → 바인딩 실패 → 에러 발생

당연히 One → Two 변환기는 아무 의미가 없지.

스프링이 One 타입을 만들 이유가 없거든.

---

# 결론: @RequestParam 에 객체를 직접 바인딩하려면 Converter<String, Two> 가 필요함

예)

```java
public class CustomConverter implements Converter<String, Two> {
    @Override
    public Two convert(String source) {
        // source 는 예: "oneInt=2&oneInteger=20&oneString=200"
        Two two = new Two();

        String[] parts = source.split("&");
        for (String part : parts) {
            String[] kv = part.split("=");
            String key = kv[0];
            String val = kv[1];

            switch (key) {
                case "oneInt" -> two.setTwoInt(Integer.parseInt(val));
                case "oneInteger" -> two.setTwoInteger(Integer.parseInt(val));
                case "oneString" -> two.setTwoString(val);
            }
        }
        return two;
    }
}
```

그리고 등록:

```java
registry.addConverter(new CustomConverter());
```

이렇게 해야 @RequestParam Two 가 정상 동작한다.

---

# 하지만 일반적으로는 이렇게 하지 않는다

보통 DTO 바인딩은 이렇게 작성한다:

```java
@GetMapping("/convert")
public Two convert(One one) {
    // 스프링이 자동으로 쿼리 → One 바인딩
    // 그 다음 직접 변환
    return customConverter.convert(one);
}
```

또는

```java
@GetMapping("/convert")
public Two convert(@ModelAttribute One one) {
    return customConverter.convert(one);
}
```

스프링 MVC의 기본 바인딩 규칙은:

* 복합 객체는 @ModelAttribute 로 바인딩
* @RequestParam 은 단순 타입만 바인딩(Integer, Long, String 등)

그래서 @RequestParam 두기 자체가 잘못된 설계야.

---

# 최종 정리

### 왜 안 됨?

* @RequestParam Two 가 동작하려면 “String → Two” 변환기가 필요함
* 당신은 “One → Two” 변환기를 만들었음 → 전혀 사용되지 않음

### 해결 방법 1: String → Two 변환기 만들기

(실전에서는 비추천)

### 해결 방법 2: 기본 규칙을 따르기 (추천)

```java
@GetMapping("/convert")
public Two convert(One one) {
    return customConverter.convert(one);
}
```

스프링은 원래 이 방식으로 DTO를 바인딩하도록 설계되어 있다.

---

필요하면

* String → One 변환기
* One → Two 변환기
  두 단계 변환 구조도 만들어줄 수 있어.

가능한데, **당신이 지금 하고 있는 방식(@RequestParam DTO + Converter<One, Two>) 조합에서는 절대 동작하지 않는다.**
즉, _등록 방식_이 잘못된 게 아니라 **Converter의 타입 구조가 현재 MVC 요청 바인딩 방식과 맞지 않아서 동작하지 않는 것**이다.

정리하자면:

---

# 1) WebConfig 에서 registry.addConverter() 는 “정상적이고 권장되는 방식”

당신이 작성한 코드는 전혀 문제가 없다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new CustomConverter());
    }
}
```

스프링 MVC 환경에서 Converter 를 등록할 때 **가장 일반적으로 사용하는 방법**이다.

즉, “WebConfig에 등록하는 것은 틀려서 안 되는 것”이 아니다.

---

# 2) 문제는 “어떤 Converter 를 등록했는가”다

지금 등록한 Converter:

```java
public class CustomConverter implements Converter<One, Two>
```

즉, *입력값이 One인 경우에만* 스프링이 이 Converter 를 적용할 수 있다.

그러나 요청은:

```
/convert?oneInt=2&oneInteger=20&oneString=200
```

그리고 컨트롤러는:

```java
@GetMapping("/convert")
public Two convert(@RequestParam Two two)
```

이 조합에서 스프링이 실제로 하려는 일은 다음이다:

---

# 3) 스프링이 하는 실제 변환: “String → Two”

스프링은 **HTTP 요청 파라미터를 항상 문자열로 읽는다.**

`@RequestParam Two two`
를 만나면,

스프링:
“String → Two 변환기가 있나?”
“없네… 그럼 실패.”

그래서 실패하는 것이다.

당신이 등록한 **One → Two 변환기**는 전혀 사용되지 않는다.

---

# 4) 따라서 “WebConfig 방식은 가능하지만, 타입 조합이 맞아야만 가능”한 것

즉:

* WebConfig 등록 방식은 올바름
* 문제는 변환기 타입이 현재 MVC 요청 처리 흐름과 맞지 않음

정확히 말하자면:

### @RequestParam DTO

→ **String → DTO Converter 가 필요**

### @ModelAttribute DTO

→ **Spring 기본 바인딩으로 자동 처리 (String → 필드별 타입)**
→ Converter<One, Two> 같은 변환기는 사용하지 않음

### Converter<One, Two>

→ “One 객체가 이미 있을 때 Two 객체로 바꾸는 용도”
→ HTTP 요청 바인딩과는 직접 호환되지 않음

---

# 5) 해결 방법 (3가지 중 하나 선택)

## 방법 A) @ModelAttribute 사용 (제일 자연스럽고 정석)

```java
@GetMapping("/convert")
public Two convert(@ModelAttribute One one) {
    return customConverter.convert(one);
}
```

또는 @ModelAttribute 생략해도 동일함:

```java
public Two convert(One one)
```

웹 요청 파라미터 → One 자동 변환
→ CustomConverter(One → Two) 호출

이 조합이 가장 자연스레 맞아떨어진다.

---

## 방법 B) String → Two Converter 를 만들어서 @RequestParam Two 를 강제로 지원

비추지만 가능.

```java
class StringToTwoConverter implements Converter<String, Two>
```

이렇게 하면 @RequestParam Two 가 동작한다.

---

## 방법 C) QueryString → One 변환기를 먼저 만들고, One → Two 로 체인 구성

가능하지만 복잡해짐.

---

# 결론

> WebConfig 에 등록하는 방식은 문제없다.
> Converter<One, Two> 가 @RequestParam Two 를 처리할 수 없어서 발생한 문제다.

즉, **등록 방식은 맞지만 Converter 타입이 MVC 요청 처리 방식과 맞지 않아서 안 되는 것**이다.

원하는 방식이 무엇인지 알려주면, 그 방식에 가장 잘 맞는 Converter 구조를 만들어줄 수 있어.

정확하게 이해했다.
네가 말한 그 코드에서는 **WebConfig 에 registry.addConverter() 로 등록한 Converter<One, Two> 는 절대 사용되지 않는다.**

이유를 구조적으로 설명해줄게.

---

# 1) @ModelAttribute One one — 이 단계에서는 “One 객체 생성 및 필드 바인딩”

`@ModelAttribute` 또는 생략된 방식:

```java
public Two convert(One one)
```

이 경우 스프링 MVC는 다음 순서로 동작한다:

### ① HTTP 쿼리 파라미터를 읽는다 (String)

### ② 기본 WebDataBinder 를 사용해 각 필드를 매핑한다

* oneInt → int
* oneInteger → Integer
* oneString → String

여기에서 필요한 타입 변환은 다음뿐이다:

* String → int
* String → Integer
* String → String

이건 스프링이 **기본적으로 다 지원하는 변환**이며
너의 One→Two Converter 와는 전혀 무관하다.

즉, `registry.addConverter(new CustomConverter())` 는 여기에 관여하지 않는다.

---

# 2) 네가 converter.convert(one) 을 직접 호출했다

```java
return customConverter.convert(one);
```

이건 MVC 타입 변환과 전혀 관계 없는 "직접 호출"이다.

즉:

* 스프링의 ConversionService 미사용
* registry에 등록한 Converter 미사용
* 단지 너가 new CustomConverter() 해서 직접 메서드를 호출한 것과 동일

그래서 WebConfig에서 등록한 Converter 는 사용되지 않는다.

---

# 3) 결론

> 네, @ModelAttribute 방식에서는 registry 에 등록한 Converter<One, Two> 가 사용되지 않는다.

왜냐하면 One → Two 변환은 **스프링이 자동으로 발생시키는 작업이 아니고**,
너 스스로 직접 호출했기 때문이다.

---

# 4) 그렇다면 registry.addConverter(new CustomConverter()) 를 사용하는 상황은?

### ConversionService 가 자동으로 타입 변환을 시도할 때

스프링은 다음 경우에 ConversionService 를 활용한다:

### (1) @RequestParam 타입 변환

String → T 변환 필요할 때
(여기서 T 가 DTO면 String → DTO Converter 필요)

### (2) @PathVariable 타입 변환

`/users/{id}` → Integer, UUID 등

### (3) @Value("${...}")

환경 변수 문자열 → 타입 변환

### (4) @ConfigurationProperties

프로퍼티 → 객체

### (5) WebDataBinder 바인딩 중 필요할 때

기본 타입 외의 커스텀 타입 매핑 요구될 때

---

# 5) 그럼 One → Two Converter 를 자동 적용시키려면?

스프링이 아래와 같은 코드를 자동 생성해야 한다:

```
One 객체를 생성한 후 → 자동으로 Two 객체로 변환
```

그러나 스프링 MVC는
“DTO 완성 후 다시 다른 DTO로 변환하는 자동 과정”
을 제공하지 않는다.

즉,

* One → Two 변환은 **MVC 요청 처리 과정의 기본 단계가 아니다**
* 그래서 등록된 Converter<One, Two> 가 자동으로 쓰이지 않는다

---

# 6) Registry 의 Converter<One, Two> 를 쓰게 만들 수 있는 패턴

## 방법 A) 컨트롤러 파라미터를 Two 로 받고, One → Two 변환기가 필요하도록 만들기

```java
@GetMapping("/convert")
public Two convert(Two two) { ... }
```

그리고 String → One 과 One → Two 를 등록하면:

```
String → One → Two
```

이 체인 변환이 작동할 수도 있다. (다만 복잡함)

---

## 방법 B) String → Two 변환기를 만들어 @RequestParam Two 동작시키기

이 방식은 Converter 를 활용하게 된다.

---

## 방법 C) @InitBinder 로 수동 지정

가능하지만 일일이 작성해야 함.

---

# 최종 결론

* **네, @ModelAttribute 방식에서는 registry 에 등록한 Converter<One, Two> 는 전혀 사용되지 않는다.**
* 스프링 MVC 는 One → Two 자동 변환 과정을 갖고 있지 않기 때문이다.
* 지금 구조에서는 직접 호출한 것이므로 WebConfig 등록과 무관하다.

원하면
"스프링 MVC 요청 바인딩에서 등록된 Converter<One, Two> 를 실제로 사용하게 만드는 구조"
를 예제로 만들어줄 수 있어.

스프링이 **기본적으로 String → Two(사용자 정의 DTO)** 변환을 지원하지 않는 이유와,
**언제 변환기를 직접 만들어야 하는지**를 정리해서 설명해줄게.

---

# 1) 스프링은 “String → DTO” 변환을 지원하지 않는다

정확히 말하면, 스프링은 **문자열 전체를 DTO 하나로 직접 변환하는 기능을 제공하지 않는다.**

즉,

```http
/convert?oneInt=2&oneString=200
```

이런 요청에서:

```
"oneInt=2&oneString=200" → DTO(One)
```

이런 직접 변환은 기본적으로 없다.

---

# 2) 그럼 스프링은 어떻게 DTO를 바인딩할까? (@ModelAttribute 방식)

스프링 MVC가 DTO 바인딩을 처리하는 진짜 방식은:

### “필드 단위로 나눠서 각각 매핑하는 방식”

예:

```
oneInt=2        →  String → int 변환
oneString=200   →  String → String
```

이렇게 **각 필드별로 스프링이 기본 변환기를 사용해서 자동으로 매핑**한다.

즉, 스프링 MVC는 DTO 전체를 String → DTO 로 한 번에 바꾸지 않고
**필드를 하나씩 매핑하는 방식**을 내장 규칙으로 갖고 있다.

그래서 @ModelAttribute는 변환기 없이도 잘 동작한다.

---

# 3) 왜 “String → Two”는 자동으로 지원하지 않을까?

이건 스프링 철학 때문이다.

* @RequestParam 은 “단일 값”
* @ModelAttribute 는 “필드 기반 DTO”
* @RequestBody 는 “JSON → DTO”
* @PathVariable 은 “단일 값”

즉, 스프링은 **단일 값 타입 변환만 ConversionService 에 맡기고,
복합 객체 바인딩은 WebDataBinder 가 담당하게 설계**되어 있다.

따라서 스프링이 기본적으로 제공하는 단일 변환 형태는:

```
String → Integer
String → LocalDate
String → Enum
String → Boolean
...
```

이렇게 “문자열 하나 → 기본 타입”이 대부분이다.

사용자 정의 DTO 전체를 문자열 하나에 담아 변환하는 건
스프링 MVC 기본 설계 대상이 아니다.

---

# 4) 그럼 언제 Converter 를 만들어야 할까?

다음 세 가지 경우에 **Converter** 또는 **Formatter** 를 만들어 등록해야 한다.

---

## 4-1) @RequestParam 에 DTO 를 직접 받고 싶을 때

예:

```java
@GetMapping("/convert")
public Two convert(@RequestParam Two two)
```

이건 스프링이 기본적으로 지원하지 않음.
이 상황에서는 반드시 **String → Two Converter** 를 만들어야 한다.

예:

```
/convert?two=oneInt:2,oneString:aaa
```

이런 특별한 형태를 직접 파싱해야 한다.

---

## 4-2) @PathVariable 로 특수한 형식의 값을 받고 싶을 때

예:

```
/users/{userId}
```

userId 의 형식이 “100-ABC-2025” 같은 복합 문자열이라면
이를 UserId 타입으로 변환하려면 Converter 필요.

---

## 4-3) JSON 이 아닌 다른 형식의 문자열을 DTO 로 바인딩할 때

예를 들어 CSV, key:value 형식처럼 JSON 표준이 아닌 경우

```
/csv?item=10,20,30
```

이걸 `List<Integer>` 로 바꾸는 Converter/Formatter가 필요하다.

---

## 4-4) @Value, @ConfigurationProperties 에서 특수 타입으로 바인딩할 때

예:

```yaml
app.duration=10s
```

이걸 Duration 타입으로 바꾸려면 내부 Converter가 필요.

사용자 정의 타입도 만들 수 있음.

---

# 5) 반대로 Converter 가 필요 없는 경우

### @ModelAttribute DTO 바인딩

필드별로 기본 변환기를 사용 → Converter 필요 없음.

### @RequestBody JSON → DTO

ObjectMapper 사용 → Converter 필요 없음.

### 기본 타입 변환(String → int, LocalDate 등)

스프링 기본 Converter 로 충분.

---

# 결론

### (1) 스프링은 “String → DTO” 같은 **전체 문자열 → 객체** 변환은 기본 제공하지 않는다.

스프링 MVC는 DTO 바인딩을 “필드 단위 바인딩”으로 처리하기 때문이다.

### (2) Converter 가 필요한 상황은 다음과 같다:

* @RequestParam 으로 DTO 를 직접 받고 싶을 때
* @PathVariable 복합 문자열 → DTO 로 변환할 때
* JSON 이 아닌 문자열 형식으로 DTO 바인딩을 해야 할 때
* 프로퍼티(환경 변수) 값을 사용자 정의 타입으로 변환할 때

즉,

> **“단일 문자열을 사용자 정의 타입으로 바꿔야 할 때” Converter 가 필요하다.**

---

원하면
String → Two Converter 예제를 실제 URL 요청과 함께 만들어 줄게.

아래는 **스프링 MVC에서 `@RequestParam Two two` 를 정상 동작하게 만드는 완전한 예제**다.
즉, **String → Two 변환기(Converter<String, Two>)** 를 직접 구현하고, 요청 시 자동 변환되도록 구성하는 방식이다.

목표:

```
/convert?two=oneInt:2,oneInteger:20,oneString:200
```

처럼 단일 문자열을 DTO(Two)로 자동 바인딩되는 구조.

---

# 1) DTO 정의

```java
public class Two {
    private int twoInt;
    private Integer twoInteger;
    private String twoString;

    // getters / setters
    public int getTwoInt() { return twoInt; }
    public void setTwoInt(int twoInt) { this.twoInt = twoInt; }

    public Integer getTwoInteger() { return twoInteger; }
    public void setTwoInteger(Integer twoInteger) { this.twoInteger = twoInteger; }

    public String getTwoString() { return twoString; }
    public void setTwoString(String twoString) { this.twoString = twoString; }
}
```

---

# 2) Converter<String, Two> 구현

```java
public class StringToTwoConverter implements Converter<String, Two> {

    @Override
    public Two convert(String source) {
        // 예: "oneInt:2,oneInteger:20,oneString:200"
        Two two = new Two();

        String[] parts = source.split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            String key = kv[0].trim();
            String val = kv[1].trim();

            switch (key) {
                case "oneInt" -> two.setTwoInt(Integer.parseInt(val));
                case "oneInteger" -> two.setTwoInteger(Integer.parseInt(val));
                case "oneString" -> two.setTwoString(val);
            }
        }

        return two;
    }
}
```

포맷 규칙을 자유롭게 바꿀 수 있다.
예: JSON처럼 하고 싶으면 JSON 파싱하면 된다.

---

# 3) WebConfig 에 Converter 등록

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToTwoConverter());
    }
}
```

이제 스프링 MVC ConversionService에 등록됨.

---

# 4) 컨트롤러에서 @RequestParam Two 받기

```java
@RestController
public class ConvertController {

    @GetMapping("/convert")
    public Two convert(@RequestParam Two two) {
        return two;
    }
}
```

---

# 5) 호출 예시

```
GET http://localhost:8080/convert?two=oneInt:2,oneInteger:20,oneString:200
```

결과:

```json
{
  "twoInt": 2,
  "twoInteger": 20,
  "twoString": "200"
}
```

---

# 6) 이 방식의 의미

스프링이 수행하는 변환 과정은 다음과 같다:

```
HTTP 요청 파라미터 "two" → 문자열
문자열 → Two 객체  ← 우리가 만든 Converter<String, Two> 사용
컨트롤러 파라미터 Two 인스턴스 주입
```

즉, Converter 가 **스프링 MVC 바인딩 파이프라인의 일부로 자동으로 사용**된다.

이제 registry.addConverter() 를 제대로 활용하게 된 것.

---

# 원하면 더 확장된 기능도 가능

* String → One, One → Two 두 단계 체인 변환
* JSON형 String → DTO 변환
* Two 리스트 변환(String → List<Two>)
* PathVariable 에도 사용 가능

이런 것도 만들어줄 수 있다.

아래에서 **두 가지 방식 모두 완전한 예제**로 설명해줄게.

---

# 1) JSON 문자열(String) → DTO 변환 (Converter<String, DTO>)

스프링 MVC에서 `@RequestParam Two two` 를 JSON 문자열로 받고 싶다면,
다음처럼 Converter<String, Two> 를 만들어 등록하면 된다.

---

## DTO

```java
public class Two {
    private int twoInt;
    private Integer twoInteger;
    private String twoString;

    // getters/setters
}
```

---

## JSON String → Two Converter

ObjectMapper 를 내부에서 직접 사용한다.

```java
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonToTwoConverter implements Converter<String, Two> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Two convert(String source) {
        try {
            return objectMapper.readValue(source, Two.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Two JSON: " + source, e);
        }
    }
}
```

---

## WebConfig에 등록

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new JsonToTwoConverter());
    }
}
```

---

## 컨트롤러

```java
@RestController
public class ConvertController {

    @GetMapping("/convertJson")
    public Two convertJson(@RequestParam Two two) {
        return two;
    }
}
```

---

## 호출 예시

```
GET /convertJson?two={"twoInt":1,"twoInteger":20,"twoString":"hello"}
```

Spring MVC가 내부적으로 다음 과정을 수행함:

```
String → Two(Converter) → Controller Two
```

---

# 2) @PathVariable → DTO 변환

예를 들어 URL에서 다음과 같은 값이 넘어온다고 하자.

```
/path/oneInt:1,oneInteger:10,oneString:abc
```

PathVariable 은 기본적으로 String 이므로,
이를 DTO로 바꾸기 위해서는 **String → Two Converter** 가 필요하다.

---

## PathVariable Converter 예제

```java
public class PathStringToTwoConverter implements Converter<String, Two> {

    @Override
    public Two convert(String source) {
        // 예: "oneInt:1,oneInteger:10,oneString:abc"
        Two two = new Two();

        String[] parts = source.split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case "oneInt" -> two.setTwoInt(Integer.parseInt(value));
                case "oneInteger" -> two.setTwoInteger(Integer.parseInt(value));
                case "oneString" -> two.setTwoString(value);
            }
        }
        return two;
    }
}
```

---

## WebConfig에 등록

```java
@Override
public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new PathStringToTwoConverter());
}
```

---

## 컨트롤러

```java
@RestController
public class ConvertController {

    @GetMapping("/path/{two}")
    public Two getByPath(@PathVariable Two two) {
        return two;
    }
}
```

---

## 호출 예

```
GET /path/oneInt:1,oneInteger:10,oneString:abc
```

결과(JSON):

```json
{
  "twoInt": 1,
  "twoInteger": 10,
  "twoString": "abc"
}
```

---

# 3) 한 번에 JSON + PathVariable 지원도 가능

원하면 **하나의 Converter** 안에서 JSON과 일반 포맷 모두 인식하는 방식도 구현 가능하다.

예:

```
/path/{"twoInt":1,"twoInteger":10,"twoString":"abc"}
```

이런 것도 자동 변환 가능하다.

---

# 4) 추가: JSON을 GET PathVariable 이나 RequestParam 으로 받는 건 권장되지 않는다

일반적으로 JSON은 **POST/PUT + @RequestBody**로 사용하는 것이 표준.

다만 스프링 MVC의 conversion 기능을 이해하고 학습하기 위한 예시로는 충분히 의미 있다.

---

# 정리

### JSON String → DTO

* Converter<String, Two> 구현 (ObjectMapper 사용)

### PathVariable → DTO

* Converter<String, Two> 구현
* 등록 후 @PathVariable Two 매핑

스프링 MVC는 단일 문자열 값을 DTO로 바꾸는 기본 기능이 없어서,
이런 경우 반드시 직접 Converter 를 등록해야 한다.

---

원하면

* JSON + PathVariable 혼합 지원
* List<Two> 처럼 리스트 변환
* Map 형태 변환
  같은 고급 예제도 만들어줄게.

아래는 **String → One → Two** 두 단계 체인 변환이 실제로 작동하는 완전한 구조야.
이 예제는 스프링의 ConversionService가 **중간 타입 One을 거쳐 자동으로 Two까지 변환하는 과정**을 실제로 수행하는 것을 보여준다.

핵심 개념:

```
String  --(Converter<String, One>)-->  One  
One     --(Converter<One, Two>)----->  Two
```

두 개의 Converter 를 모두 등록해두면
**스프링은 String → Two 변환을 요청받았을 때 자동으로 중간 단계 One → Two 변환까지 수행**한다.

---

# 1) DTO 클래스

## One

```java
public class One {
    private int oneInt;
    private Integer oneInteger;
    private String oneString;

    // getters/setters
}
```

## Two

```java
public class Two {
    private int twoInt;
    private Integer twoInteger;
    private String twoString;

    // getters/setters
}
```

---

# 2) String → One 변환기

HTTP 요청 값은 항상 문자열이므로
String → One 변환기가 필요하다.

예: `"oneInt:1,oneInteger:10,oneString:abc"`

```java
public class StringToOneConverter implements Converter<String, One> {

    @Override
    public One convert(String source) {
        One one = new One();

        String[] parts = source.split(",");
        for (String part : parts) {
            String[] kv = part.split(":");
            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case "oneInt" -> one.setOneInt(Integer.parseInt(value));
                case "oneInteger" -> one.setOneInteger(Integer.parseInt(value));
                case "oneString" -> one.setOneString(value);
            }
        }
        return one;
    }
}
```

---

# 3) One → Two 변환기

```java
public class OneToTwoConverter implements Converter<One, Two> {

    @Override
    public Two convert(One one) {
        Two two = new Two();
        two.setTwoInt(one.getOneInt() * 2);
        two.setTwoInteger(one.getOneInteger() * 2);
        two.setTwoString(one.getOneString() + one.getOneString());
        return two;
    }
}
```

---

# 4) WebConfig에 두 변환기 등록

여기서 ConversionService 는 두 변환기를 **체인으로 자동 구성한다.**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToOneConverter());
        registry.addConverter(new OneToTwoConverter());
    }
}
```

이렇게 하면:

```
String → One (가능)
One → Two (가능)
⇒ 따라서 String → Two (자동으로 가능)
```

스프링 ConversionService가 자동으로 경로를 연결한다.

---

# 5) 컨트롤러: @RequestParam Two 를 받는다

```java
@RestController
public class ConvertController {

    @GetMapping("/chain")
    public Two chainConvert(@RequestParam Two two) {
        return two;
    }
}
```

여기서 중요한 점:

* 사용자는 “Two” 문자열을 보내지만
* 스프링은 먼저 **String → One**,
* 그 결과를 다시 **One → Two**,
* 마지막에 컨트롤러에 **Two 타입으로 전달**

이 과정을 자동으로 수행한다.

---

# 6) 호출 예시

```
GET /chain?two=oneInt:1,oneInteger:10,oneString:ABC
```

ConversionService 작동:

```
"oneInt:1,oneInteger:10,oneString:ABC"
      ↓ (StringToOneConverter)
One{ oneInt=1, oneInteger=10, oneString="ABC" }
      ↓ (OneToTwoConverter)
Two{ twoInt=2, twoInteger=20, twoString="ABCABC" }
```

컨트롤러에서 반환되는 JSON 예시는:

```json
{
  "twoInt": 2,
  "twoInteger": 20,
  "twoString": "ABCABC"
}
```

---

# 7) 이 구조가 중요한 이유

### (1) 스프링 ConversionService가 중간 타입을 자동으로 찾고 연결할 수 있다

이는 스프링 타입 변환 시스템의 핵심 능력 중 하나다.

### (2) PathVariable, RequestParam, @Value, @ConfigurationProperties 등 모든 곳에서 체인 변환 자동 적용 가능

### (3) 변환기를 여러 개 만들고 체인 구성하면 복잡한 변환 과정도 자동으로 처리된다

---

# 8) PathVariable 도 자동으로 된다

컨트롤러 아래처럼 해도 완전히 동일하게 작동한다:

```java
@GetMapping("/chain/{two}")
public Two chainPath(@PathVariable Two two) {
    return two;
}
```

URI:

```
/chain/oneInt:3,oneInteger:30,oneString:XYZ
```

변환 과정:

```
String → One → Two
```

자동 처리.

---

# 원하면

* 체인 변환을 JSON 기반으로 수정
* String → List<One> → List<Two> 확장
* Map 변환
* 복잡한 도메인 타입 변환
  전부 만들어줄 수 있어.

정확히 핵심을 짚었고, 결론은 다음 두 줄로 요약된다.

**① @RequestParam Two two 처럼 “컨트롤러 인자(argument)”에 타입을 선언해야 ArgumentResolver가 ConversionService를 사용한다.
② reverse 방향(= 리턴값 String → Two 변환)은 ResultHandler가 ConversionService를 사용하지 않아서 자동 변환되지 않는다.**

아래에서 작동 원리를 단계별로 정확하게 설명해줄게.

---

# 1) ArgumentResolver 는 “컨트롤러 인자”를 만들 때 ConversionService 를 사용한다

스프링 MVC 요청 처리 단계에서:

```
HandlerMethodArgumentResolver → 컨트롤러 파라미터 생성
```

이 과정에서 **ConversionService 를 사용하여 타입 변환을 시도한다.**

즉,
컨트롤러 메서드 인자에 다음처럼 선언하면:

```java
public Two chainConvert(@RequestParam Two two)
```

스프링은 다음과 같이 동작한다:

1. HTTP 요청 파라미터를 String 으로 읽음
2. 파라미터 타입(Two)을 보고
3. ConversionService 에 “String → Two 변환기”가 등록되어 있는지 확인
4. 있으면 Converter 로 변환
5. 컨트롤러 인자로 주입

즉, **ArgumentResolver는 컨버터를 자동 사용한다.**

이게 `@RequestParam Two two` 가 되는 이유다.

---

# 2) “리턴값 변환”은 ResultHandler가 처리하는데 ConversionService를 사용하지 않는다

컨트롤러의 반환 값 처리 체인은 다음이다:

```
HandlerMethodReturnValueHandler → HttpMessageConverter → 응답
```

여기엔 두 가지 중요한 특징이 있다.

---

## (1) 리턴 값이 객체면 JSON 직렬화(ObjectMapper)만 실행된다

```java
public Two chainConvert(...) {
    return two;
}
```

→ Jackson(ObjectMapper)이 실행되어 JSON 변환된다.
**ConversionService는 관여하지 않는다.**

---

## (2) 리턴 타입이 String이면 “View 이름”으로 처리된다

```java
return "hello";   // View 이름
```

String 반환은 “뷰 이름” 처리 규칙이 있다.

즉:

* String → DTO 변환 없음
* ConversionService 사용 없음

따라서 “리턴값에 Converter를 적용해서 변환”하는 것은 **MVC 표준 구조상 불가능**하다.

---

# 3) 왜 ResultHandler는 ConversionService를 사용하지 않을까?

컨트롤러 반환 처리 영역은 **타입 변환(layer)**이 아니라 **응답 생성(response generation) layer**이기 때문이다.

정리하면:

| 단계            | 변환기 사용?                     | 설명                              |
| ------------- | --------------------------- | ------------------------------- |
| 요청 → 컨트롤러 인자  | **사용함 (ConversionService)** | ArgumentResolver 단계             |
| 컨트롤러 인자 → 리턴값 | 사용 안함                       | 그냥 자바 코드                        |
| 리턴값 → HTTP 응답 | **사용 안함**                   | HttpMessageConverter (JSON/XML) |

즉,
**ConversionService는 “입력 변환기”이지 “출력 변환기”가 아니다.**

---

# 4) 리턴값 변환을 하고 싶으면?

방법은 기본적으로 **직접 호출**뿐이다.

```java
@GetMapping("/xx")
public Two convertResult() {
    One one = ...
    return conversionService.convert(one, Two.class);
}
```

리턴 단계에서 자동 변환은 지원되지 않는다.

---

# 5) 결론

### 질문 1

“@RequestParam Two two 인자에 이렇게 써야 argumentresolver가 converter를 작동시켜?”

→ **정확하다. 인자 타입에 선언해야 ArgumentResolver가 ConversionService로 변환한다.**

### 질문 2

“return에 string을 써서 resulthandler에서 변환하는 거는 자동으로 안 해줘?”

→ **절대 안 된다. ResultHandler/HttpMessageConverter는 ConversionService를 사용하지 않는다.**

즉, 자동 변환은 오직 **컨트롤러 인자(argument)** 단계에서만 작동한다.

---

원하면

* ResponseBodyAdvice 를 사용하여 리턴값 가로채 변환하기
* ControllerAdvice 로 리턴 포맷 바꾸기
* 출력 변환을 자동화하는 고급 패턴
  도 알려줄 수 있어.
