가능한 경우와 불가능한 경우가 명확히 갈린다.
아래를 보면 전체 흐름이 정리된다.

---

# 1. 결론 먼저

* **Thymeleaf, JSP 등 뷰 템플릿이 `Model`의 값을 출력할 때는 Converter/Formatter가 자동 적용된다.**
  → 즉, `Number → String`, `Date → String` 포매팅에서 **Formatter.print()**가 사용될 수 있다.

* **JSON 응답(@ResponseBody)에서는 Converter/Formatter가 적용되지 않는다.**
  → Jackson이 처리함.

* **템플릿에서 문자열 → 타입 변환(String → Number) 같은 역변환은 일반적으로 일어나지 않는다.**
  → 뷰는 데이터 바인딩이 아니라 단순 출력이기 때문.

---

# 2. 구체적으로 어떤 상황에서 적용되는가?

## 상황 1: 뷰 템플릿이 모델 값을 **출력할 때**

예: Thymeleaf

```html
<span th:text="${price}"></span>
```

여기서 `price`가 `Number`라면:

* Spring은 `Formatter.print()`를 사용해 문자열로 변환한다.
* 즉, 너의 `MyNumberFormatter.print()`가 적용될 수 있다.

즉, 뷰 템플릿은 단순히 `${price}`로 접근하지만
실제로는 내부적으로 `Formatter.print()`가 호출된다.

---

## 상황 2: `<form:input>` 같은 Spring Form 태그 사용 시

JSP의 Spring form 태그:

```jsp
<form:input path="price"/>
```

이 때도 내부적으로 출력할 때:

* 모델의 Number 값을 문자열로 변환해야 한다.
* 여기서도 Formatter.print()가 사용된다.

---

# 3. 템플릿에서 역변환이 필요한 경우는?

## 템플릿 자체는 역변환(String → Number)을 하지 않는다.

왜냐하면:

* 화면은 그냥 문자열을 보여줄 뿐이고
* 역변환은 요청이 다시 서버로 들어올 때(Spring MVC 데이터 바인딩 시점)에 일어난다.

따라서 **뷰는 "출력 포매팅"만 관여한다.**

---

# 4. Thymeleaf의 특별한 기능

Thymeleaf는 많은 경우 Spring의 ConversionService를 그대로 사용한다.

예:

```html
<span th:text="${#numbers.formatInteger(price)}"></span>
```

이런 메서드를 쓰면 Formatter는 안 쓰고 Thymeleaf 자체 포맷터가 쓰인다.

그러나 단순 `${price}`는 Spring MVC의 ConversionService → Formatter.print()가 사용된다.

---

# 5. 결론 정리

### Yes – 적용되는 경우

* 뷰 템플릿이 Model 값을 출력할 때
* Spring Form 태그(`<form:input>`, `<form:select>`) 등에서 표시할 때
* `Number → String`, `Date → String` 등 **출력 포매팅 과정**

### No – 적용되지 않는 경우

* JSON 변환
* 템플릿 내부에서 문자열을 다른 타입으로 바꾸는 "역변환"
* 템플릿 자체의 포매팅 함수 호출(예: Thymeleaf의 `#numbers.format...`) → Spring Formatter가 아니라 템플릿 엔진 자체 기능이 사용됨

---

# 6. 단순 요약

뷰 템플릿에서 표시하는 값은 Spring이 ConversionService를 통해 **Formatter.print()를 호출**해 문자열을 만들어 넣는다.
즉, “템플릿에서 출력되는 Number 값에 포매팅이 걸리는 것”은 가능하다.

원하면 실제 적용 예제(컨트롤러 → 모델 → 템플릿 출력 과정)도 코드로 보여줄 수 있다.

아래는 **Formatter가 뷰 템플릿에서 실제로 어떻게 적용되는지**를
컨트롤러 → 모델 → 템플릿까지 흐름 전체를 코드로 보여주는 예시다.

---

# 1. Formatter 등록

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new MyNumberFormatter());
    }
}
```

---

# 2. Formatter 구현

```java
public class MyNumberFormatter implements Formatter<Number> {

    @Override
    public Number parse(String text, Locale locale) throws ParseException {
        return NumberFormat.getInstance(locale).parse(text);
    }

    @Override
    public String print(Number object, Locale locale) {
        return NumberFormat.getInstance(locale).format(object);
    }
}
```

---

# 3. 컨트롤러에서 모델에 값 넣기

```java
@Controller
public class SampleController {

    @GetMapping("/view-test")
    public String viewTest(Model model) {
        model.addAttribute("price", 1234567);
        return "sample";
    }
}
```

모델에 있는 `price`는 **Number 타입**이다.
템플릿에서 출력할 때 `print()`가 자동 호출될 수 있다.

---

# 4. 뷰 템플릿 (Thymeleaf)

`sample.html` 예시

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>

<p>가격: <span th:text="${price}"></span></p>

</body>
</html>
```

여기서 핵심:

`th:text="${price}"`

* 내부적으로 Spring의 `ConversionService`가 모델 값을 변환한다.
* `price`가 Number이므로 MyNumberFormatter의 `print()`가 호출된다.

예를 들어, 한국 locale 기준 출력:

```
가격: 1,234,567
```

---

# 5. 실제 호출 흐름

요청 → 컨트롤러 → 모델에 `Number` 저장
→ 템플릿 렌더링 시점에 `${price}` 렌더링
→ SpringConversionService가 Number → String 변환 요청
→ Formatter.print() 호출
→ 템플릿에 포맷된 문자열 삽입

이렇게 된다.

---

# 6. Spring Form 태그 사용 예도 가능

JSP + Spring Form 태그라면 다음과 같이 작동한다.

```jsp
<form:form modelAttribute="form">
    <form:input path="price" />
</form:form>
```

여기서도:

* `price` 값이 Number
* 입력창에 넣을 값은 문자열이어야 함
* ConversionService가 Number → String 변환
* Formatter.print()가 사용됨

즉, input 박스 안에서도 `1,234,567`처럼 표시될 수 있다.

---

# 7. Thymeleaf에서 *특별한 포맷 함수*를 쓰는 경우

```html
<span th:text="${#numbers.formatInteger(price)}"></span>
```

이 경우는 Thymeleaf 자체 포매터가 적용되고
Spring Formatter는 적용되지 않는다.

하지만 단순 `${price}`는 Spring Formatter가 적용된다.

---

# 8. 중요한 정리

### 출력할 때 적용됨

* `${price}` 출력
* `<form:input>` 값 바인딩
* `<form:select>` 선택값 표시

### 입력값 처리할 때는?

입력된 값이 컨트롤러로 다시 들어올 때는
`Formatter.parse()`가 적용된다.

---

# 9. 하나로 묶은 요약 예시

**요청**

```
/view-test
```

**템플릿 출력**

```
가격: 1,234,567
```

이 결과는 완전히 `MyNumberFormatter.print()` 때문에 나타난 것이다.

---

필요하면 `@ModelAttribute`에서 입력값을 포맷팅하는 전체 흐름도 같이 보여줄까?

전역(`WebMvcConfigurer.addFormatters`)으로 등록하지 않고 **특정 컨트롤러나 특정 요청에만 Formatter/Converter를 적용**하려면
Spring에서 제공하는 방식은 **오직 하나**, 즉 **`@InitBinder`**를 사용하는 것이다.

아래에 모든 방법을 정리한다.

---

# 1. 특정 컨트롤러/요청에서만 적용하고 싶다면 — `@InitBinder`

`@InitBinder` 메서드에 Formatter를 등록하면,
**그 컨트롤러 안에서만**, 혹은 **해당 메서드가 적용되는 요청에서만** Formatter가 작동한다.

예시:

```java
@Controller
public class SampleController {

    @InitBinder
    public void init(WebDataBinder binder) {
        binder.addCustomFormatter(new MyNumberFormatter());
    }

    @GetMapping("/convert-3")
    public String convert3(@ModelAttribute One one) {
        return "ok";
    }
}
```

이 경우 동작 범위:

* **해당 컨트롤러 전체에서 적용**
* 해당 컨트롤러 밖에서는 적용되지 않음

---

# 2. 특정 모델 객체에만 적용하고 싶을 때

`@InitBinder("one")`처럼 **특정 ModelAttribute 이름에만 적용**하는 것도 가능하다.

```java
@Controller
public class SampleController {

    @InitBinder("one")
    public void initForOne(WebDataBinder binder) {
        binder.addCustomFormatter(new MyNumberFormatter());
    }

    @GetMapping("/convert-3")
    public One convert3(@ModelAttribute("one") One one) {
        return one;
    }
}
```

이 경우:

* **One 객체 바인딩을 할 때만** Formatter가 적용된다.
* 같은 컨트롤러 안의 다른 타입의 ModelAttribute에는 적용되지 않는다.

---

# 3. 특정 필드에만 적용하는 방법은 있는가?

Spring Formatter는 **타입 단위**로 동작하며
"특정 필드만" 지정하는 기능은 없다.

하지만 다음 우회 방식은 가능하다.

### 3-1. 특정 필드에만 적용되도록 Converter를 따로 만들고

해당 필드는 별도 타입으로 정의하는 방법

```java
public class Money { 
    private int value; 
}
```

필드를 이렇게 변경하고

```java
private Money price;
```

그리고 Money만 변환하는 Converter/Formatter를 만든 다음

`@InitBinder`로 등록하는 방식.

→ 타입을 분리해야 하기 때문에 코드 구조를 바꿔야 한다는 단점이 있음.

---

# 4. 정리

전역 설정 없이 특정 상황에만 Formatter를 적용하려면 다음 중 하나를 사용한다.

| 적용 범위              | 방법                                                   |
| ------------------ | ---------------------------------------------------- |
| 특정 컨트롤러 전체         | `@InitBinder`                                        |
| 특정 ModelAttribute만 | `@InitBinder("attributeName")`                       |
| 특정 타입만             | 해당 타입만 변환하는 Formatter/Converter 작성 + @InitBinder로 등록 |
| 단순 출력 포맷 (뷰)에서만    | 템플릿 엔진 자체 포맷 기능 사용 (Formatter 사용 아님)                 |

---

# 5. 추천 방식

대부분의 경우:

* **컨트롤러 단위 적용 → `@InitBinder`**
* 입력값 파싱과 출력값 포맷이 필요한 경우 `Formatter` 사용
* 특정 타입만 처리할 경우 타입 분리 후 해당 타입 전용 Formatter 구현

이 구조가 가장 깔끔하다.

---

원하면 **Converter와 Formatter가 데이터 바인딩 흐름에서 어떻게 선택되는지**,
또는 **여러 Formatter/Converter를 동시에 등록했을 때 우선순위 흐름도**도 설명해줄 수 있다.
