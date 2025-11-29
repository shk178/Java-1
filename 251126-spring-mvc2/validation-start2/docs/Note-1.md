Bean Validation(빈 검증)은 **Java 표준(Jakarta Bean Validation)** 기반의 검증 프레임워크입니다.
스프링이 자체적으로 만든 Validator와 달리 **표준 어노테이션(@NotNull 등)** 으로
검증 규칙을 간단하게 선언할 수 있고,
자동으로 BindingResult와 연동되며 국제화(i18n) 메시지도 자동 매칭됩니다.

아래에서 **무엇인지 → 왜 필요한지 → 어떻게 사용하는지 → 스프링과의 연결 구조**까지
전체적으로 정리해드릴게요.

---

# 1. Bean Validation이란?

정식 명칭은:

* Jakarta Bean Validation (과거: Javax Bean Validation)
* 표준 스펙(JSR 303, 349, 380)

핵심 개념:

### “어노테이션으로 도메인 객체(모델)에 검증 규칙을 붙이는 표준 기술”

예:

```java
public class Item {

    @NotBlank(message = "이름은 필수입니다.")
    private String itemName;

    @Min(value = 1000, message = "가격은 1000원 이상이어야 합니다.")
    @Max(value = 2000, message = "가격은 2000원 이하이어야 합니다.")
    private Integer price;
}
```

객체 자체에 검증 규칙을 선언해두면
스프링이 DTO나 ModelAttribute를 바인딩할 때 자동으로 검증을 수행합니다.

---

# 2. 왜 Bean Validation이 필요한가?

스프링 Validator로만 검증하면:

* 코드가 늘어남
* 필드와 검증 로직이 분리됨
* 필드 변경 시 Validator도 수정해야 함
* 큰 프로젝트에서 유지보수 어려움

Bean Validation은 다음을 해결합니다.

### 검증 규칙이 객체와 함께 존재 → 유지보수 쉬움

### 어노테이션 문법으로 간결

### 표준 스펙이라 여러 프레임워크와 호환됨

### 스프링이 자동으로 검사해줌 (@Valid / @Validated 필요)

---

# 3. 기본 제공 어노테이션들

아주 많이 쓰는 검증 어노테이션 BEST 10

## 문자열

| 어노테이션           | 설명                             |
| --------------- | ------------------------------ |
| @NotBlank       | null, "", " " 모두 허용 X (문자열 전용) |
| @NotEmpty       | null, ""만 허용 X                 |
| @NotNull        | null만 허용 X                     |
| @Size(min, max) | 문자열 길이 제한                      |

## 숫자

| 어노테이션           | 설명        |
| --------------- | --------- |
| @Min(값)         | 최소값       |
| @Max(값)         | 최대값       |
| @Positive       | 0보다 커야 함  |
| @PositiveOrZero | 0 이상      |
| @Negative       | 0보다 작아야 함 |

## 객체, 콜렉션

| 어노테이션     | 설명               |
| --------- | ---------------- |
| @Valid    | 내부 객체까지 검증하도록 설정 |
| @NotNull  | null이 아니어야 함     |
| @NotEmpty | size > 0         |

---

# 4. Bean Validation 적용 방법

## 1) 의존성 추가

Spring Boot는 기본적으로 Hibernate Validator가 내장되어 있습니다.
추가 설정할 필요 없는 경우가 대부분입니다.

## 2) DTO 또는 Model 객체에 어노테이션 추가

```java
public class Item {

    @NotBlank
    private String itemName;

    @Min(1000)
    @Max(2000)
    private Integer price;

    @Min(1)
    @Max(10)
    private Integer quantity;
}
```

## 3) 컨트롤러에 @Valid 또는 @Validated 사용

```java
@PostMapping("/add")
public String addItem(
        @Valid @ModelAttribute("item") Item item,
        BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "addForm";
    }
    ...
}
```

### @Valid = JSR 표준

### @Validated = 스프링 기능 + 그룹 검증 가능

둘의 차이점도 아래에서 설명합니다.

---

# 5. 메시지 사용 (메시지 프로퍼티 파일)

메시지는 자동으로 messageSource에서 찾으며, 예:

```
javax.validation.constraints.NotBlank.message=공백일 수 없습니다
javax.validation.constraints.Min.message={value} 이상만 가능합니다
javax.validation.constraints.Max.message={value} 이하만 가능합니다
```

혹은 커스텀 메시지:

```java
@NotBlank(message = "이름은 필수 입력입니다.")
private String itemName;
```

---

# 6. @Valid 와 @Validated 차이

### 공통점

둘 다 Bean Validation을 실행시킨다.

### 차이점

| 어노테이션      | 특징                            |
| ---------- | ----------------------------- |
| @Valid     | 표준 / 그룹 검증 기능 없음              |
| @Validated | 스프링 제공 / 그룹 검증 가능(주로 실무에서 사용) |

그룹 검증 예:

```java
public interface SaveCheck {}
public interface UpdateCheck {}
```

필드에 그룹 지정:

```java
@NotBlank(groups = SaveCheck.class)
@Size(min = 3, groups = UpdateCheck.class)
private String itemName;
```

컨트롤러:

```java
@PostMapping("/add")
public String add(@Validated(SaveCheck.class) Item item) {}

@PostMapping("/edit")
public String edit(@Validated(UpdateCheck.class) Item item) {}
```

---

# 7. WebDataBinder와 자동 연결

스프링 MVC는 내부적으로 WebDataBinder를 통해 Bean Validation을 자동 적용합니다.

* @Valid 또는 @Validated 발견
* WebDataBinder가 기본 글로벌 Validator(LocalValidatorFactoryBean)를 실행
* BindingResult에 오류 추가
* 뷰에서 th:errors 로 오류 표시

Bean Validation을 글로벌하게 쓰고 싶으면:

```java
@Override
public Validator getValidator() {
    return new LocalValidatorFactoryBean();
}
```

---

# 8. Bean Validation vs 스프링 Validator 차이

| 구분     | Bean Validation        | 스프링 Validator        |
| ------ | ---------------------- | -------------------- |
| 방식     | 어노테이션 기반               | 자바 코드 기반             |
| 위치     | 도메인 또는 DTO             | Validator 클래스        |
| 자동 호출  | @Valid / @Validated 필요 | @InitBinder 또는 직접 호출 |
| 유연성    | 보통 수준                  | 고급 수준(복잡한 검증 가능)     |
| 확장성    | 표준 기반                  | 스프링 전용               |
| 적합한 경우 | 단순 필드 검증               | 복잡한 비즈니스 검증          |

실무에서는 **둘을 함께 사용**합니다.

* 단순 필드 규칙 → Bean Validation
* 두 필드 조합 검증, 복잡한 조건 → 스프링 Validator

---

# 9. 왜 스프링이 Bean Validation을 적극적으로 지원할까?

* 검증 로직을 모델 옆에 둘 수 있어 유지보수 용이
* 너무 단순한 값 검증을 Validator로 매번 만들면 번거로움
* 자바 표준 스펙(Jakarta Validation)이라 다른 프레임워크와 호환
* 메시지 국제화까지 자동 지원

그래서 Spring Boot는 Hibernate Validator를 자동으로 포함하며
“제일 먼저 고려해야 하는 검증 방식”이 됩니다.

---

# 10. 직접 예제 만들어보기

원하면 다음을 함께 만들어드릴 수 있어요:

* Bean Validation 버전 Item 클래스
* 검증 후 화면 표시 예시(Thymeleaf)
* range 검증을 Bean Validation으로 대체하는 방법
* 복잡한 검증을 Bean Validation + Spring Validator 혼합 방식으로 구성한 예시

---

# 결론

**Bean Validation은 어노테이션으로 모델에 검증 규칙을 붙여
검증을 자동으로 수행하는 Java 표준 기술**이며,
스프링 MVC에서 가장 강력하고 편리한 검증 방식입니다.

원하시면 지금 작성하고 있는 ItemValidator를 Bean Validation으로 일부 또는 전체 대체하는 실전 코드도 만들어드릴게요.

Bean Validation에서는 **FieldError와 ObjectError를 직접 만들지 않습니다.**
대신 **어노테이션으로 선언하면 스프링이 자동으로 FieldError 또는 ObjectError로 변환해줍니다.**

아래에서 *필드 오류(FieldError)* 와 *글로벌 오류(ObjectError)* 를
Bean Validation에서 어떻게 처리하는지 정확하게 설명드릴게요.

---

# 1. Bean Validation에서 FieldError는 이렇게 만든다

결론:

### 필드에 @NotNull, @NotBlank, @Min, @Max 같은 어노테이션을 붙이면

### 스프링이 해당 어노테이션 검증 실패 시 FieldError를 자동 생성한다.

예:

```java
public class Item {

    @NotBlank(message = "상품 이름은 필수입니다.")
    private String itemName;

    @Min(value = 1000, message = "가격은 1000 이상이어야 합니다.")
    @Max(value = 2000, message = "가격은 2000 이하이어야 합니다.")
    private Integer price;

    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    @Max(value = 10, message = "수량은 10 이하이어야 합니다.")
    private Integer quantity;
}
```

컨트롤러:

```java
@PostMapping("/add")
public String addItem(
        @Valid @ModelAttribute Item item,
        BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "addForm";
    }
    ...
}
```

이렇게만 하면:

* itemName → FieldError 자동 생성
* price → FieldError 자동 생성
* quantity → FieldError 자동 생성

스프링이 자동으로 다음을 호출하는 것과 동일합니다:

```
bindingResult.addError(
    new FieldError("item", "itemName", rejectedValue, false, codes, args, message)
)
```

즉, Bean Validation은 개발자가 FieldError를 만들지 않아도
**자동으로 FieldError를 만들어서 bindingResult에 넣어줍니다.**

---

# 2. Bean Validation에서 ObjectError는 이렇게 만든다

필드 단위가 아닌 “객체 전체”에 대한 검증(글로벌 오류)을 만들고 싶다면
**클래스 레벨에 Bean Validation 어노테이션을 붙입니다.**

예: price * quantity ≥ 10000 이어야 한다

## 1) 클래스 레벨 제약 정의

```java
@CheckTotalPrice
public class Item {
    ...
}
```

## 2) 커스텀 Validator 구현

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckTotalPriceValidator.class)
public @interface CheckTotalPrice {
    String message() default "총 금액이 10000 이상이어야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## 3) 어노테이션 검증 로직

```java
public class CheckTotalPriceValidator implements ConstraintValidator<CheckTotalPrice, Item> {
    @Override
    public boolean isValid(Item item, ConstraintValidatorContext context) {
        if (item.getPrice() != null && item.getQuantity() != null) {
            return item.getPrice() * item.getQuantity() >= 10000;
        }
        return true;
    }
}
```

Bean Validation은 이 어노테이션이 실패할 경우:

### → 자동으로 ObjectError를 생성해서 bindingResult에 넣습니다.

즉, 다음과 같은 효과:

```
bindingResult.addError(
    new ObjectError("item", codes, args, message)
)
```

여기에서도 개발자가 직접 ObjectError를 만들 필요가 없습니다.

---

# 3. 정리: Bean Validation에서 FieldError/ObjectError는 이렇게 처리됨

| 기능          | 스프링 Validator (직접)                | Bean Validation (자동)           |
| ----------- | --------------------------------- | ------------------------------ |
| FieldError  | rejectValue() 또는 new FieldError() | 필드에 제약 어노테이션(@NotNull, @Min 등) |
| ObjectError | reject() 또는 new ObjectError()     | 클래스 레벨 제약(커스텀 어노테이션)           |

---

# 4. Bean Validation에서 FieldError 메시지 커스터마이징

### 필드 제약에 메시지를 지정하면 자동으로 매칭됨

```java
@NotBlank(message = "상품 이름은 필수입니다.")
```

또는 메시지 코드 방식:

```java
@NotBlank(message = "{required.item.itemName}")
```

messages.properties:

```
required.item.itemName=상품 이름은 필수입니다.
```

---

# 5. Bean Validation에서 ObjectError 메시지 커스터마이징

클래스 레벨 제약 어노테이션에서 message 속성에 코드 사용:

```java
@CheckTotalPrice(message = "{totalPriceMin}")
```

messages.properties:

```
totalPriceMin=총 금액이 10000 이상이어야 합니다.
```

---

# 6. Bean Validation + Spring Validator 혼합도 가능

필드 검증은 Bean Validation으로 처리하고:

* 두 필드 조합 검증
* 비즈니스 규칙 검증
* 데이터베이스 조회 필요 검증

같은 복잡한 검증은 스프링 Validator를 그대로 사용하면 됩니다.

---

# 최종 요약

### Bean Validation의 핵심

1. **필드 오류(FieldError)**
   → 필드에 제약 어노테이션 붙이면 스프링이 자동 생성

2. **글로벌 오류(ObjectError)**
   → 클래스에 제약 어노테이션(커스텀 constraint) 붙이면 생성

3. **BindingResult에 자동으로 추가됨**
   → 개발자가 new FieldError/new ObjectError 할 필요 없음

4. **필드-level과 object-level 검증을 모두 할 수 있다**

---

원하면
당신의 ItemValidator를 Bean Validation 방식으로 완전히 재구성해드릴 수도 있고,
필드 검증은 Bean Validation / 글로벌 검증은 Spring Validator 혼합 방식으로 설계해드릴 수도 있어요.

좋아요.
ObjectError(글로벌 오류)를 **Bean Validation으로 가장 단순하게 구현하는 방식**을
“필드 여러 개가 서로 관련이 있을 때”의 예로 아주 쉽게 보여드릴게요.

아래 예시는 **두 숫자의 합이 10 이상이어야 한다** 같은 간단한 규칙입니다.
(실무에서는 price * quantity 검증 같은 곳에 사용)

---

# 가장 단순한 Bean Validation 기반 ObjectError 예시

## 1) 검증할 클래스

```java
public class Item {

    private Integer price;
    private Integer quantity;

    // getters, setters
}
```

## 2) 클래스 자체에 “글로벌 검증”용 어노테이션 하나 붙이기

```java
@TotalMin
public class Item {
    ...
}
```

## 3) TotalMin 어노테이션 만들기

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TotalMinValidator.class)
public @interface TotalMin {
    String message() default "총합이 최소 10 이상이어야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

* TYPE 레벨 → 클래스 전체를 검사함
* message는 ObjectError 메시지가 됨

## 4) TotalMinValidator 구현

```java
public class TotalMinValidator implements ConstraintValidator<TotalMin, Item> {

    @Override
    public boolean isValid(Item item, ConstraintValidatorContext context) {
        if (item.getPrice() == null || item.getQuantity() == null) {
            return true;  // 필드 자체 오류는 FieldError가 처리
        }

        int total = item.getPrice() + item.getQuantity();
        return total >= 10;
    }
}
```

여기서 `false`가 반환되면:

* Bean Validation이 ObjectError를 생성
* BindingResult에 자동으로 등록됨

→ 개발자가 `new ObjectError("item", ...)` 같은 코드를 만들 필요 없음.

---

# 5) 컨트롤러

```java
@PostMapping("/add")
public String addItem(
        @Validated @ModelAttribute("item") Item item,
        BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "addForm";
    }
    return "success";
}
```

여기서 validate()를 직접 호출할 필요 없음.

---

# 6) Thymeleaf에서 ObjectError 표시

전체 에러 메시지를 한 번에 표시하고 싶을 때:

```html
<div th:if="${#fields.hasGlobalErrors()}">
    <p th:each="err : ${#fields.globalErrors()}" 
       th:text="${err.defaultMessage}" class="field-error">
    </p>
</div>
```

Bean Validation이 만든 ObjectError 메시지가 여기에 나타납니다.

---

# 결과

price=3, quantity=4일 때:
3 + 4 = 7 < 10

→ 글로벌 오류 발생
→ ObjectError 자동 생성
→ “총합이 최소 10 이상이어야 합니다.” 라는 메시지가 화면에 표시됨

---

# 전체 예시 요약

### Bean Validation ObjectError 구성 요소

1. **클래스 레벨 어노테이션**
2. **ConstraintValidator 구현**
3. **false 반환 시 ObjectError 생성**
4. **BindingResult에 자동 등록**
5. **Thymeleaf에서 #fields.globalErrors()로 표시**

---

# 왜 이 방식이 좋은가?

* FieldError는 필드에 어노테이션 붙이면 자동
* ObjectError는 클래스에 어노테이션 붙이면 자동
* 개발자가 FieldError, ObjectError를 직접 만들 필요 없음
* 컨트롤러에서 validate 호출할 필요 없음
* 복잡한 비즈니스 검증도 깔끔하게 재사용 가능

---

원하면
당신이 사용 중인 Item(price, quantity, itemName)을 기반으로
실제 어노테이션(@TotalPriceMin) 예시와 템플릿까지 완전히 맞춘 버전을 만들어드릴게요.

정확하게 이해하고 계십니다.
정리하면 **ObjectError는 “객체 전체를 대상으로 하는 오류”**이고
**FieldError는 “필드 하나에 대한 오류”**입니다.

그래서 질문하신 두 가지에 대해 분명하게 답해드릴게요.

---

# 1. “직접 Validator를 구현하는 게 중요한 거지?”

Bean Validation(@NotNull, @Min…)은 **단순 필드 검증**에는 매우 좋습니다.
하지만 다음 같은 검증은 Bean Validation만으로는 어렵습니다.

* 두 필드의 관계가 있는 검증
* price * quantity ≥ 10000
* 시작 날짜 < 종료 날짜
* 로그인 이메일과 비밀번호 매칭
* DB 조회가 필요한 검증
* 서비스 계층까지 로직이 필요한 검증

이런 검증은 **Field 단위가 아니라 객체 단위**이기 때문에
다음 중 하나로 처리해야 합니다.

1. **클래스 레벨 Bean Validation(= 객체 전체를 검사)**
2. **Spring Validator 직접 구현**

Bean Validation의 클래스 레벨(@TotalPriceMin)도 결국 내부에서
**직접 Validator를 구현**하게 되어 있습니다.

따라서 결론:

### 복잡한 검증일수록 “직접 Validator를 구현하는 방식”이 필요하다.

### Bean Validation과 Spring Validator는 서로 대체가 아니라 보완 관계이다.

---

# 2. “ObjectError를 필드 하나에 사용하기도 해?”

결론: **아니요. 하지 않습니다.**

ObjectError는 **필드가 아닌 “객체 전체”를 위한 오류**입니다.

### FieldError → 특정 필드에 연결되는 오류

(힌트: fieldName 존재)

### ObjectError → 특정 필드와 연결되지 않는 오류

(힌트: fieldName 없음)

그래서 ObjectError를 필드 하나에 쓰는 것은 **의도와 맞지 않습니다.**

---

# 왜 ObjectError를 필드에 쓰지 않는가?

예를 들어 다음 메시지를 생각해봅시다.

* “상품 이름은 필수입니다.”

이건 itemName 필드에 대한 오류이므로 FieldError가 맞습니다.

ObjectError로 만들면:

* 어떤 필드에 대한 오류인지 템플릿에서 찾을 수 없음
* th:errors="*{itemName}"에서 표시되지 않음
* #fields.hasErrors("itemName")도 false
* 결국 화면에서 필드에 표시하기 어려움

즉, FieldError 대신 ObjectError를 쓰면 **UI단에서 표현이 어려움**.

그래서 ObjectError는 다음처럼 “필드 여러 개를 함께 봐야 하는 경우”에만 사용합니다.

* 가격과 수량을 곱한 값이 10000 이상이어야 한다
* 시작일 < 종료일
* 이메일/비밀번호 조합이 맞지 않는다
* 전체 합계가 특정 값을 넘지 않아야 한다

이런 그래야 하는 이유는:

### ObjectError는 필드명을 갖지 않기 때문이다.

---

# 3. 그럼 “필드 하나만 있는 검증”은 무조건 FieldError를 써야 하나?

네. Bean Validation도 Bean Validator도 모두 같은 원칙을 따릅니다.

필드 단일 검증이면:

### - Bean Validation → @NotBlank, @Min, @Max 등 필드에 직접 붙임

### - Spring Validator → errors.rejectValue("fieldName", ...) 사용

ObjectError를 필드 하나에 쓰는 구조는 스프링 MVC의 설계 목적에도 맞지 않습니다.

---

# 4. 아주 간단한 요약

| 구분          | 언제 사용?             | 예시                  |
| ----------- | ------------------ | ------------------- |
| FieldError  | 필드 하나에 대한 검증       | itemName이 비었을 때     |
| ObjectError | 여러 필드 조합, 객체 전체 검증 | price * quantity 검증 |

---

# 5. 결론

### Bean Validation은 단순 필드 검증용

### 객체 전체 검증(ObjectError)은 직접 Validator 구현 또는 클래스 레벨 제약 필요

### ObjectError를 필드 하나에 쓰는 것은 잘못된 사용

필요하면 지금 Item에 대해

* FieldError는 Bean Validation으로
* ObjectError는 커스텀 어노테이션으로
  완전히 구현한 “정석 구조”를 만들어 드릴게요.

ScriptAssert는 **Bean Validation(JSR-303/380)의 “클래스 레벨 제약(Class-level constraint)” 중 하나**로,
객체 전체를 대상으로 **간단한 논리를 스크립트(JavaScript/Python 등)로 평가하는 방식**입니다.

즉,

* 두 필드의 값을 비교하거나
* price * quantity 같은 계산을 하거나

이런 “객체 전체 검증(ObjectError)”을 **스크립트로 짧게 작성해서 해결**할 수 있게 만든 기능입니다.

하지만 결론부터 말하면:

### ScriptAssert는 “가능은 하지만 실무에서 거의 사용하지 않는 방식”이다.

### 이유는: 불편하고, IDE 지원이 약하며, 오류가 코딩 중 검출되지 않고 런타임 평가라 위험하기 때문.

그래도 어떻게 쓰는지 이해해보면 Bean Validation의 구조가 완성되니 간단 예시도 보여드릴게요.

---

# 1. ScriptAssert 기본 개념

어노테이션 형태:

```java
@ScriptAssert(
    lang = "javascript",
    script = "_this.price + _this.quantity >= 10",
    message = "총합이 10 이상이어야 합니다."
)
public class Item {
    private Integer price;
    private Integer quantity;
}
```

여기서 `_this` 는 현재 객체(Item)를 가리킵니다.

즉,

* “이 객체의 price + quantity가 10 이상인지 검증해라”
* 실패하면 “총합이 10 이상이어야 합니다.” 메시지가 글로벌 오류(ObjectError)로 등록됩니다.

---

# 2. ScriptAssert 동작 방식

Bean Validation은 다음 단계를 밟습니다.

1. Item 클래스에 @ScriptAssert가 붙어 있음 확인
2. 객체 바인딩 후
3. script를 평가 (JS 엔진 사용)
4. true → 통과
5. false → ObjectError 자동 생성

→ 스프링 컨트롤러의 bindingResult에서 글로벌 오류로 조회됨

---

# 3. 간단한 예시

## Item 클래스

```java
@ScriptAssert(
        lang = "javascript",
        script = "_this.price * _this.quantity >= 10000",
        message = "총 금액은 10000 이상이어야 합니다."
)
public class Item {

    @NotNull
    private Integer price;

    @NotNull
    private Integer quantity;

    // getter/setter
}
```

## 컨트롤러

```java
@PostMapping("/add")
public String addItem(
        @Valid @ModelAttribute("item") Item item,
        BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "addForm";
    }
    return "success";
}
```

## 뷰(Thymeleaf)

```html
<div th:if="${#fields.hasGlobalErrors()}">
    <p th:each="err : ${#fields.globalErrors()}"
       th:text="${err.defaultMessage}"></p>
</div>
```

---

# 4. ScriptAssert의 큰 단점 (그래서 잘 안 씀)

### 1) 스크립트가 문자열이기 때문에 오타를 IDE가 잡아주지 않음

* `_this.prce` 라고 잘못 쓰면? → 컴파일 에러 없음
* 런타임에 JavaScript 엔진에서 오류 발생

### 2) 필드 이름 refactoring 시 자동 변경되지 않음

Java 코드 기반 검증은 필드명이 바뀌면 IDE가 자동으로 수정해주지만
ScriptAssert는 문자열이라 IDE가 모름.

### 3) 복잡한 식은 가독성이 떨어짐

`"_this.startDate <= _this.endDate"` 정도는 괜찮지만
이 이상이면 커스텀 Validator로 만드는 게 훨씬 안전함.

### 4) 성능 문제

런타임에 스크립트 엔진을 호출해야 함.

### 5) Spring 공식 문서에서도 ScriptAssert를 실무용으로 권장하지 않음

→ “가능하지만 추천하지 않는 방법”

---

# 5. ScriptAssert 대신 권장하는 방식 = 커스텀 클래스 레벨 Constraint

예를 들어:

```java
@PriceQuantityValid
public class Item { ... }
```

그리고 Validator:

```java
public class PriceQuantityValidator
        implements ConstraintValidator<PriceQuantityValid, Item> {

    @Override
    public boolean isValid(Item item, ConstraintValidatorContext context) {
        if (item.getPrice() == null || item.getQuantity() == null) {
            return true;
        }
        return item.getPrice() * item.getQuantity() >= 10000;
    }
}
```

이 방식은:

* IDE 리팩토링과 함께 자동으로 필드 변경 추적
* 타입 안정성
* 디버깅 쉬움
* 유지보수 훨씬 편함

그래서 실무에서는 거의 **무조건 이 방식**을 사용합니다.

---

# 6. 결론

ScriptAssert는:

* 객체 전체 오류(ObjectError)를 만들 수 있는 **간단한 기능**이지만
* 유지보수·IDE 지원·안전성 문제로 인해
* **실무에선 사용 권장되지 않음**
* 대신 **커스텀 클래스 레벨 Validator**가 정석

그래도 ScriptAssert를 알아두면
Bean Validation 내의 ObjectError 구조를 이해하는 데 도움이 됩니다.

---

원하면

* ScriptAssert를 적용한 완전한 Item 코드
* ScriptAssert 대신 커스텀 Validator를 적용한 정석 버전
  둘 다 비교해서 더 이해하기 쉽게 정리해드릴까요?
