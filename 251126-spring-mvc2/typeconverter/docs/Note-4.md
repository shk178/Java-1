아래 코드는 **Spring Framework의 `Formatter<Number>` 구현체**이며, 문자열과 숫자 타입 사이를 **양방향으로 변환**하기 위한 포맷터다. 기능 자체는 Converter와 겹치는 부분이 있지만, 동작 목적과 사용되는 위치가 다르다.

---

# 1. 이 코드가 하는 일

```java
public class MyNumberFormatter implements Formatter<Number> {
    @Override
    public Number parse(String text, Locale locale) throws ParseException {
        NumberFormat format = NumberFormat.getInstance(locale);
        return format.parse(text);
    }

    @Override
    public String print(Number object, Locale locale) {
        return NumberFormat.getInstance(locale).format(object);
    }
}
```

## parse()

* **문자열 → 숫자(Number)** 변환
* 로케일에 따라 다른 숫자 형식을 지원

    * 예:

        * 한국(KO): `"1,234.5"`
        * 독일(DE): `"1.234,5"`
* NumberFormat을 이용하므로 자연스럽게 국제화(i18n) 지원.

## print()

* **숫자(Number) → 문자열** 변환
* 역시 로케일에 맞춰 문자열을 생성

    * 한국: 12345 → `"12,345"`
    * 독일: 12345 → `"12.345"`

즉, **로케일에 따라 숫자를 읽고, 다시 로케일에 맞게 출력하는 ‘숫자 전용’ 포맷터**다.

---

# 2. Converter와 뭐가 다른가?

Converter와 Formatter의 차이를 핵심만 말하면 아래처럼 정리할 수 있어.

| 구분     | Converter                   | Formatter                                        |
| ------ | --------------------------- | ------------------------------------------------ |
| 목적     | 타입 A → 타입 B 단방향 변환          | 문자 ↔ 객체 양방향 변환                                   |
| 대상     | 모든 타입                       | 주로 **문자열과 객체**                                   |
| 국제화 지원 | 없음                          | 있음 (Locale 파라미터 제공)                              |
| 사용 예   | `String → Integer`, `A → B` | 날짜 포맷, 숫자 포맷, 통화 포맷                              |
| 메서드    | `convert(A source)`         | `parse(String, Locale)`, `print(Object, Locale)` |

---

# 3. 왜 Formatter가 필요한가?

폼 입력, URL 파라미터 바인딩, 뷰 렌더링에서는 대부분 **문자열**과 **객체** 사이의 변환이 반복된다. 그리고 이 과정은 로케일에 따라 달라질 수 있다.

그래서 Spring은 텍스트 기반 포맷을 담당하는 **Formatter API**를 별도로 제공한다.

예:

* 2024-12-01 → Date
* “₩12,345” → Number
* 1.234,56 (독일 형식) → Number

이런 걸 Converter로 처리하면 Locale 정보를 못 받기 때문에 Formatter가 적합하다.

---

# 4. 결론

이 클래스는 **국제화를 고려한 숫자 변환기**이다.
Converter와 역할이 겹치는 듯 보이지만 차이는 명확하다.

* Converter는 단방향, 일반 타입 변환
* Formatter는 **문자열 ↔ 객체** 양방향 + Locale 지원

즉, **웹에서 입력받은 숫자 문자열을 올바른 숫자 객체로 바꾸고, 다시 뷰에 표시할 때 로케일 맞춰 출력**할 때 쓰는 도구다.

원하면 Converter와 Formatter가 동시에 등록될 때 우선순위나 동작 흐름도 설명해줄 수 있다.

결론부터 말하면 **ResultHandler 단계에서는 Formatter가 적용되지 않는다.**
Formatter는 **데이터 바인딩(입력 처리)과 뷰 렌더링(출력 포맷팅)** 과정에서만 사용되고, ResultHandler는 그 이후 단계다.

아래에서 전체 흐름 속에서 Formatter가 어디에서 적용되는지, ResultHandler는 어떤 역할인지 구분해줄게.

---

# 1. Formatter가 적용되는 위치

Spring MVC 요청 처리 흐름을 기준으로 보면 Formatter는 다음 두 지점에서 사용된다.

## 1) **입력 바인딩 시 적용 (String → 객체)**

* 폼 입력 값, 쿼리 파라미터, 경로 변수 등을
  **문자열에서 객체로 바꿀 때 Formatter.parse() 호출**

예:

```http
GET /order?price=1,234
```

→ Controller 메서드의 파라미터 `int price`로 변환하는 과정에서 Formatter 적용.

## 2) **뷰 렌더링 시 적용 (객체 → String)**

* 모델 객체를 화면에 출력할 때
  **Formatter.print() 호출**

예: 모델의 Number 필드를 `"1,234"` 형식으로 출력.

---

# 2. ResultHandler는 언제 실행되는가?

Spring MVC에서 `HandlerAdapter`가 컨트롤러를 실행한 뒤,
**컨트롤러의 ‘결과’를 처리하는 단계**가 바로 ResultHandler다.

### ResultHandler는 다음 로직을 수행한다.

* 컨트롤러 반환값을 `ModelAndView`로 변환
* 뷰를 선택
* 뷰 렌더링 과정 호출

즉, ResultHandler는 **컨트롤러 실행 후, 뷰 렌더링 전에 실행**되는 큰 분기 처리 단계다.

---

# 3. Formatter는 ResultHandler 안에서 실행되는가?

아니야.

Formatter는 다음 순간에만 동작한다:

| 시점    | 내용                                     |
| ----- | -------------------------------------- |
| 입력 처리 | HandlerAdapter가 컨트롤러를 호출하기 **직전**      |
| 출력 처리 | ResultHandler가 뷰 렌더링을 호출한 **뒤(뷰 안에서)** |

즉, Formatter 동작은 두 번 끼어들지만, **ResultHandler 자체가 Formatter를 직접 실행하는 것은 아니다.**

---

# 4. 흐름을 그림으로 보면

```
[DispatcherServlet]
   ↓
[HandlerMapping]
   ↓
[HandlerAdapter]
   ↓
[DataBinder] ——— Formatter.parse() 호출 (문자열 → 객체 변환)
   ↓
[Controller]
   ↓
[ResultHandler]  ← 여기서는 Formatter 동작 안 함
   ↓
[View]
   ↓         Formatter.print() 호출 (객체 → 문자열)
[HTML 렌더링]
```

---

# 최종 정리

* Formatter는 **요청 파라미터 바인딩 과정**과
  **뷰 렌더링 과정의 출력 포맷팅**에서만 동작한다.
* ResultHandler는 컨트롤러 결과 전체 흐름을 핸들링하는 단계이며,
  Formatter는 그 내부에서 직접 호출되지 않는다.

원한다면 HandlerAdapter, DataBinder, FormatterRegistry가 서로 어떻게 연결되는지도 더 상세하게 설명해줄게.

가능한 경우와 불가능한 경우가 나뉜다. 결론부터 말하면 아래와 같다.

1. **지금 작성한 Converter 전체를 그대로 Formatter로 바꾸는 것은 가능하다.**
   단, 문자열 전체를 파싱해서 One 객체로 만드는 구조라면 *Formatter 하나가 전체를 담당*하게 된다.

2. **각 필드(oneInt, oneInteger, oneString)에 따로 Formatter가 적용되는 것은 아니다.**
   여러 Formatter가 각 필드에 자동으로 분배되어 적용되지는 않는다.
   결국 **문자열 → One 객체 변환은 하나의 Converter 또는 하나의 Formatter가 담당한다.**

아래에서 자세히 설명할게.

---

# 1. 현재 Converter의 역할

지금 `StringToOneConverter`는 이런 문자열을

```
"oneInt:10, oneInteger:20, oneString:hello"
```

직접 파싱해서 **One 객체를 조립**하고 있다.

즉,

* 문자열 전체를 하나의 객체로 매핑
* 필드를 직접 꺼내서 세팅

이건 **일반적인 숫자/날짜 포맷팅과는 성격이 다르다.**

---

# 2. Formatter로 바꾸는 것은 가능하다

Formatter는 기본적으로 "String ↔ Object" 양방향 변환을 담당한다.

즉, **전체 문자열을 하나의 One 객체로 파싱하는 구조라면 Formatter로 구현할 수 있다.**

예:

```java
public class OneFormatter implements Formatter<One> {

    @Override
    public One parse(String text, Locale locale) throws ParseException {
        One one = new One();

        String[] parts = text.split(",");
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

    @Override
    public String print(One object, Locale locale) {
        return "oneInt:" + object.getOneInt()
                + ", oneInteger:" + object.getOneInteger()
                + ", oneString:" + object.getOneString();
    }
}
```

이렇게 하면 **폼 입력 처리와 뷰 렌더링 양쪽 모두 동일 형식으로 포맷팅**할 수 있다.

---

# 3. “필드마다 Formatter가 적용되나요?” → 아니야.

Spring은 **문자열 전체를 객체로 변환할 때** Converter 또는 Formatter를 하나만 사용한다.

예를 들어,

```
String → One
```

이 변환을 수행할 Candidate는 하나다.

Spring은 각 필드 값을 끊어서 필드 단위에 Formatter를 적용하지 않는다.

왜냐하면,

* 문자열 자체가 구조화된 데이터(JSON 비슷한 구조)를 담고 있고
* 그 문자열을 **파싱해서 One 객체로 만드는 로직은 개발자가 직접 만든 맞춤 규칙**이기 때문

즉,

* `"oneInt:10"`에서 `10`에 대해 Formatter 적용
* `"oneString:hello"`에서 `hello`에 대해 Formatter 적용

이런 구조는 Spring에서 자동으로 지원하지 않는다.

**전체 문자열을 해석하는 것은 Converter or Formatter의 책임이다.**

---

# 4. 언제 필드별 Formatter가 적용될까?

Spring은 이런 구조에서만 필드별 Formatter를 적용한다:

```
입력값이 "10"이고, 필드가 Integer 타입.
입력값이 "1,234"이고, 필드가 Number 타입.
입력값이 "2024-01-01"이고, 필드가 LocalDate 타입.
```

즉, **입력값이 개별 필드로 직접 들어갈 때**만 Formatter가 적용된다.

지금 경우처럼 One 같은 커스텀 객체를 문자열 하나로 표현하는 경우는
Spring이 분해해서 필드마다 포맷터를 적용해주는 방식이 아니라,

**문자열 → One** 변환 자체를 하나의 단위로 보고 하나의 Converter/Formatter를 사용한다.

---

# 결론

* “String → One” 같은 **커스텀 객체 변환은 Converter 또는 Formatter 한 개가 전체를 담당한다.**
* “필드별로 Formatter가 자동 적용”되지는 않는다.
* 현재 Converter를 Formatter로 바꾸는 것은 가능하며, 양방향 포맷이 필요하면 Formatter가 적합하다.

필요하면

* 실제 등록 방법
* WebMvcConfigurer에서의 FormatterRegistry 설정
* 커스텀 객체 변환 우선순위
  도 더 알려줄게.

아래는 **Spring MVC에서 Formatter를 등록하는 방법**과, **Converter/Formatter 간 우선순위가 어떻게 결정되는지**를 전체 흐름 기준으로 정리한 내용이다.

---

# 1. Formatter 등록 방법

Formatter는 기본적으로 두 가지 방식으로 등록할 수 있다.

---

## 방법 1) `WebMvcConfigurer` 사용 (일반적인 방식)

가장 직관적이고 많이 사용되는 방식이다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new OneFormatter());
    }
}
```

이렇게 하면 Spring 컨테이너 시작 시 FormatterRegistry에 등록된다.

Spring MVC의 변환 구조는 다음 순서로 이 Registry를 참고한다:

* Converter
* GenericConverter
* Formatter

이 세 가지가 모두 **ConversionService(Fallback Binding)**에 통합된다.

---

## 방법 2) `@Component` + `FormattingConversionServiceFactoryBean` 사용

정교한 구성 또는 XML 시절 구성 방식이다.
요즘은 거의 `WebMvcConfigurer`만 사용하지만, 이렇게도 된다.

```java
@Component
public class OneFormatter implements Formatter<One> {
    ...
}
```

이 경우 Spring은 `FormattingConversionService`를 빈으로 구성할 때
클래스패스 스캔된 Formatter/Converter를 자동으로 등록할 수 있다.

하지만 기본 설정만으로 자동 등록되지는 않아
대개 `WebMvcConfigurer` 방식이 훨씬 명확하고 안정적이다.

---

# 2. Formatter vs Converter 우선순위

Spring의 타입 변환은 **ConversionService**에서 이루어진다.

여기에는 다음 우선순위 규칙이 있다.

---

## 우선순위 규칙 (핵심)

### 1순위: **Converter**

(특히 정확한 소스 → 타겟 타입을 지정한 Converter)

### 2순위: **GenericConverter / ConditionalConverter**

(조건 기반, 다형성 기반 Converter)

### 3순위: **Formatter**

(문자열 ↔ 객체 양방향 변환 담당)

---

즉, 동일한 변환 목적이 있는 경우:

* `String → One` Converter가 존재함
* `Formatter<One>`이 존재함

둘 중 무엇이 사용될까?

→ **Converter가 먼저 선택된다.**

왜냐하면 Formatter도 실제로는 내부적으로 Converter로 래핑되지만,
ConversionService에서 **정확 매칭 Converter가 있으면 Formatter보다 먼저 적용하기 때문**이다.

---

# 3. 왜 Converter가 Formatter보다 먼저일까?

Spring의 ConversionService 구조는 다음과 같다.

```
ConversionService
 ├─ Converters (정확한 타입 변환 먼저 확인)
 ├─ GenericConverters (조건 기반)
 └─ Printer/Parser (Formatter)
```

Formatter는 Printer + Parser로 래핑되어
문자열 기반 변환이 필요할 때만 선택된다.

즉, 다음 조건에서 우선순위가 낮다:

* 동일한 타입 매칭의 Converter가 이미 존재한다.
* Converter는 타입 변환 전용이라 정확한 매칭이 가능하다.

---

# 4. Converter·Formatter 우선순위 충돌 예시

### 예시: String → One

* `StringToOneConverter` 등록됨
* `OneFormatter` 등록됨

이때
**Controller 파라미터 `One one`**
바인딩 요청이 들어오면:

1. 먼저 `String → One` Converter 탐색
2. 정확히 일치하는 Converter가 있으면 바로 사용
3. Formatter는 무시됨

이게 Spring 기본 정책이다.

---

# 5. 그럼 Formatter를 우선 적용하고 싶으면?

방법은 두 가지다.

### 방법 A) Converter를 등록하지 않는다

→ String↔One 변환은 Formatter가 수행

### 방법 B) Converter 이름을 제거하고 ConditionalConverter로 조건화

→ 타입 특정 상황에서만 동작하도록 제한
(대부분 필요 없음)

결국은 Converter가 더 명확하게 타입을 지정하기 때문에
Formatter보다 항상 앞선다고 보면 돼.

---

# 6. 결론 정리

### Formatter 등록

* 가장 흔한 방식: `WebMvcConfigurer.addFormatters()`
* 권장 코드:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new OneFormatter());
    }
}
```

### 우선순위

1. Converter (정확 매칭)
2. Generic / Conditional Converter
3. Formatter (문자열 변환)

### 충돌 시

* 동일 변환이 있다면 Converter가 항상 Formatter보다 먼저 동작한다.

---

필요하다면

* ConversionService 구조 내부 동작
* DataBinder에 ConversionService가 어떻게 연결되는지
* HandlerMethodArgumentResolver 단계에서 Formatter가 어느 타이밍에 호출되는지
  이런 내부 흐름도 더 깊이 설명해줄 수 있다.
