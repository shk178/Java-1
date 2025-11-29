아주 좋은 질문입니다.
위처럼 **스프링의 4단계 메시지 구조(Level1~Level4)를 모두 준비한 상태**에서
“ValidationUtils를 활용하려면 어떻게 해야 하는가?”

결론부터 말하면:

---

# ✔ ValidationUtils는 **Level1~Level4 중 필요한 메시지 코드를 자동으로 생성해주는 구조**를 만들어줍니다.

즉, ValidationUtils를 쓰면:

* Level1 (`required.item.itemName`)
* Level2 (`required.itemName`)
* Level3 (`required.java.lang.String`)
* Level4 (`required`)

이 **4가지 후보 코드가 전부 자동으로 생성**되고
MessageSource에서 **존재하는 코드를 찾아 메시지를 선택**합니다.

ValidationUtils는 자체적으로 메시지를 지정하지 않고
**“code = required”, field 명을 제공하는 방식**으로만 작동하기 때문입니다.

결국 당신이 만든 Level1~Level4 메시지는 그대로 적용됩니다.

---

# ✔ ValidationUtils 사용법 (필드 공백 체크)

예:

```java
ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName", "required");
```

이렇게 하면 스프링이 자동으로 아래 코드들을 만들어냅니다:

```
required.item.itemName
required.itemName
required.java.lang.String
required
```

MessageSource는 위 순서대로 존재하는 메시지를 찾습니다.

따라서 당신의 메시지 파일:

```
required.item.itemName=상품 이름을 입력하세요.
```

이게 Level1에서 매칭되어 사용됩니다.

---

# ✔ 동작 방식 전체 흐름

1. ValidationUtils.rejectIfEmptyOrWhitespace(...) 호출
2. FieldError 생성 (code = "required", field = "itemName")
3. DefaultMessageCodesResolver가 메시지 코드 4개 생성
4. MessageSource가 메시지 파일에서 가장 구체적인 코드부터 탐색
5. 존재하는 메시지를 발견하는 순간 반환

당신이 메시지 파일에 넣어둔 Level1~Level4가 정확히 이 구조와 맞기 때문에
**ValidationUtils를 아주 자연스럽게 사용할 수 있습니다.**

---

# ✔ 실제 Validator 코드 예시

```java
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        // Level1~4 메시지가 자동 적용되는 예
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName", "required");

        // price 체크 (직접 추가)
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 2000) {
            errors.rejectValue("price", "range", new Object[]{1000, 2000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 10) {
            errors.rejectValue("quantity", "range", new Object[]{1, 10}, null);
        }
    }
}
```

→ 메시지 선택은 당신이 정의한 Level1 → Level2 → Level3 → Level4 순서대로 진행됩니다.

---

# ✔ ValidationUtils는 언제 쓰면 좋은가?

ValidationUtils는 **"required" 조건만 처리할 때 매우 적합**합니다.

예:

* 공백 체크
* null 체크
* 필수 값 입력

그 이외의 조건 (범위, 길이, 타입 등)은 `rejectValue()`를 사용해야 합니다.

---

# ✔ 만약 validationUtils로 range도 하고 싶다면?

ValidationUtils는 “required” 체크만 제공하는 유틸입니다.
range에는 직접 errors.rejectValue()만 가능:

```java
errors.rejectValue("price", "range", new Object[]{1000, 2000}, null);
```

---

# ✔ 결론

당신이 작성한 메시지 파일을 기준으로 ValidationUtils를 적용하면:

### itemName

```
ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName", "required");
```

→ Level1: required.item.itemName
→ Level2: required.itemName
→ Level3: required.java.lang.String
→ Level4: required

중 존재하는 메시지가 자동으로 사용됩니다.

따라서 ValidationUtils와 메시지 구조(Level1~4)는 완벽하게 호환됩니다.

---

원한다면
ItemValidator 전체를 **ValidationUtils 활용 + 메시지 코드 최적 구조**로
더 깔끔한 버전으로 완성해서 보여드릴게요.

지금 작성한 컨트롤러 코드는 **FieldError를 직접 생성해서 넣는 방식**인데,
여기에 ValidationUtils를 “어떻게 활용하느냐”는 질문은
결국 → **직접 FieldError를 만드는 코드를 줄이고, 메시지 코드(Level1~4)를 자동 생성되게 만드는 방식으로 바꿀 수 있느냐**
라는 질문입니다.

정답은 예입니다.
ValidationUtils + rejectValue 를 사용하면 훨씬 짧고 안정적으로 만들 수 있고,
당신이 만든 Level1~Level4 메시지 구조도 그대로 자동 적용됩니다.

아래에서 **지금 코드 → ValidationUtils 활용 코드**로 바꾸는 방법을 단계별로 설명합니다.

---

# 1. 지금 방식의 문제점

현재 코드:

```java
bindingResult.addError(
    new FieldError("item", "itemName", item.getItemName(), false,
        new String[]{"required.item.itemName"}, null, null)
);
```

이 방식은:

* FieldError를 직접 만든다
* 메시지 코드 배열을 수동으로 넣어야 한다
* rejectedValue, arguments 등을 모두 직접 넣어야 한다
* 코드가 길고 유지보수 어렵다
* 스프링의 자동 메시지 코드 생성 기능을 못 씀

즉, **스프링의 메시지 자동 확장 기능(Level1~Level4)을 쓰지 않고 있음**.

---

# 2. ValidationUtils + rejectValue로 바꾸면?

아래 코드 한 줄:

```java
ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");
```

이렇게 쓰면:

스프링이 자동으로 다음 메시지 코드를 생성:

```
required.item.itemName
required.itemName
required.java.lang.String
required
```

당신이 errors.properties에 넣어둔 4단계 규칙이 그대로 작동합니다.

---

# 3. range 같은 Custom Logic은 rejectValue 로 대체

현재 코드는:

```java
bindingResult.addError(
        new FieldError("item", "price", item.getPrice(), false,
            new String[]{"range.item.price"}, new Object[]{1_000, 2_000}, null)
);
```

이걸 rejectValue로 바꾸면:

```java
bindingResult.rejectValue("price", "range", new Object[]{1000, 2000}, null);
```

여기서 code="range" 이므로 스프링이 자동으로 다음 코드들을 생성:

```
range.item.price
range.price
range.java.lang.Integer
range
```

당신이 messages.properties(errors.properties)에 넣어둔:

```
range.item.price=가격은 {0}~{1}을 입력하세요.
```

이 메시지가 가장 먼저 선택됩니다.

---

# 4. quantity도 동일

기존 코드:

```java
bindingResult.addError(
        new FieldError("item", "quantity", item.getQuantity(), false,
            new String[]{"range.item.quantity"}, new Object[]{1, 10}, null)
);
```

→ rejectValue로 대체:

```java
bindingResult.rejectValue("quantity", "range", new Object[]{1, 10}, null);
```

이 역시:

```
range.item.quantity
range.quantity
range.java.lang.Integer
range
```

이 순서대로 메시지 찾습니다.

---

# 5. ValidationUtils 적용한 최종 컨트롤러

```java
@PostMapping("/add")
public String addItem(
        @ModelAttribute Item item,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
) {

    // 1) required 체크 (ValidationUtils)
    ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");

    // 2) price 범위 체크
    if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 2000) {
        bindingResult.rejectValue("price", "range", new Object[]{1000, 2000}, null);
    }

    // 3) quantity 범위 체크
    if (item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 10) {
        bindingResult.rejectValue("quantity", "range", new Object[]{1, 10}, null);
    }

    // 실패 시 다시 폼으로
    if (bindingResult.hasErrors()) {
        return "validation/v1/addForm";
    }

    // 성공 처리
    Item savedItem = itemRepository.save(item);
    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v1/items/{itemId}";
}
```

이렇게 하면:

* FieldError 직접 생성 코드 완전히 제거
* 메시지 코드(Level1~4)가 자동으로 생성되어 fallback됨
* errors.properties의 내용이 그대로 반영됨
* rejectedValue도 자동으로 채워져 값 유지됨
* 코드 가독성이 80% 좋아짐
* 유지보수 훨씬 쉬움

---

# 6. ValidationUtils + rejectValue 조합은 스프링이 권장하는 공식 패턴

스프링 MVC/스프링부트 공식 문서에서도 다음 구조를 사용합니다.

* required: ValidationUtils.rejectIfEmptyOrWhitespace
* custom rule: rejectValue
* message code는 단일 코드만 넣는다(code="required"/"range")
* fallback 코드는 MessageCodesResolver가 자동 생성
* 메시지 파일은 level1~4 를 필요에 따라 정의하면 됨

---

# 최종 요약

당신이 작성한 errors.properties(Level1~4) 메시지 구조는
ValidationUtils + rejectValue 패턴과 **100% 호환**됩니다.

따라서 지금의 긴 FieldError 직접 생성 코드는
모두 다음 방식으로 바꿔야 합니다:

* required → ValidationUtils.rejectIfEmptyOrWhitespace
* range → bindingResult.rejectValue(…, "range", params, null)

이렇게 사용하면 메시지 코드 생성이 자동화되고 유지보수가 훨씬 좋아집니다.

---

원한다면
현재 프로젝트 기준으로 **ItemValidator 클래스**로 분리한 버전까지 만들어서
더 깔끔한 구조를 드릴 수 있습니다.

지금 작성한 구조는 **컨트롤러에서 itemValidator.validate(item, bindingResult)** 를 직접 호출하는 방식입니다.
그런데 스프링 MVC는 이걸 **자동으로 호출해주는 방식**도 제공합니다.

그때 사용하는 것이 바로:

1. **WebDataBinder에 Validator 등록 (컨트롤러에서)**
2. **@Validated 또는 @Valid 사용 (파라미터에서)**

이 두 가지입니다.

아래에서 흐름과 원리를 하나씩 설명해드릴게요.

---

# 1. WebDataBinder에 ItemValidator를 등록한다는 의미

컨트롤러에 다음처럼 작성합니다:

```java
@InitBinder
public void init(WebDataBinder binder) {
    binder.addValidators(itemValidator);
}
```

이 코드는 **이 컨트롤러가 처리하는 요청에서 사용되는 WebDataBinder에
itemValidator를 자동으로 연결하겠다**는 뜻입니다.

즉,

* 스프링이 바인딩 작업을 수행할 때
* Item 객체를 처리하는 상황이 오면
* WebDataBinder가 itemValidator를 호출합니다

이제 컨트롤러에서 직접 `itemValidator.validate(...)` 를 호출할 필요가 없어집니다.

---

# 2. @Validated 또는 @Valid 를 붙이면 자동 검증이 작동한다

컨트롤러 파라미터 앞에:

```java
public String addItem(
        @Validated @ModelAttribute Item item,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
) {
```

또는

```java
public String addItem(@Valid Item item, BindingResult bindingResult)
```

이렇게 하면 스프링은 다음 순서로 동작합니다.

### ✔ 흐름

1. Item 객체 바인딩
2. WebDataBinder가 등록된 Validator 목록 확인
3. ItemValidator.supports(Item.class) → true
4. ItemValidator.validate(item, bindingResult) 자동 호출
5. bindingResult에 오류 넣기
6. 컨트롤러에서 hasErrors() 체크 가능

즉, 개발자는 validate()를 직접 호출할 필요 없음.

---

# 3. 자동 검증 흐름 그림으로 보면 간단함

```
@Validated 추가
      ↓
WebDataBinder 가 Validator 목록 확인
      ↓
ItemValidator.supports(Item.class)?
      ↓    yes
ItemValidator.validate() 자동 호출
      ↓
BindingResult에 오류 추가
      ↓
컨트롤러에서 hasErrors() 호출
```

컨트롤러가 검증을 직접 호출하지 않아도 됩니다.

---

# 4. 지금 코드와 비교해보면

현재:

```java
itemValidator.validate(item, bindingResult);
```

자동 검증 버전:

```java
@Validated @ModelAttribute Item item
```

* WebDataBinder:

```java
@InitBinder
public void init(WebDataBinder binder) {
    binder.addValidators(itemValidator);
}
```

이렇게 구성하면 아래만 남고, 검증 호출은 제거됩니다.

```java
if (bindingResult.hasErrors()) {
    return "validation/v1/addForm";
}
```

---

# 5. 전체 코드 예시

## 컨트롤러

```java
@Controller
@RequiredArgsConstructor
public class ItemControllerV2 {

    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder binder) {
        binder.addValidators(itemValidator);
    }

    @PostMapping("/add")
    public String addItem(
            @Validated @ModelAttribute("item") Item item,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "validation/v1/addForm";
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);

        return "redirect:/validation/v1/items/{itemId}";
    }
}
```

## Validator

```java
@Component
public class ItemValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "itemName", "required");

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 2000) {
            errors.rejectValue("price", "range", new Object[]{1000, 2000}, null);
        }

        if (item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 10) {
            errors.rejectValue("quantity", "range", new Object[]{1, 10}, null);
        }
    }
}
```

---

# 6. 언제 이 패턴이 유리할까?

## 장점

* 컨트롤러가 검증 로직을 직접 호출하지 않아 깔끔해진다
* 여러 메서드에서 같은 검증기를 재사용 가능
* Bean Validation(옵션)과도 자연스럽게 연결됨(@NotNull 등)

## 단점

* 처음 보는 사람은 “왜 검증기가 자동으로 실행되지?” 하고 헷갈릴 수 있음
* 컨트롤러의 작동 방식을 추적하기 어려워질 수 있음

그래서 실무에서는:

* 간단한 프로젝트 → 직접 validate 호출
* 큰 프로젝트 → WebDataBinder + @Validated 사용

이런 식으로 선택합니다.

---

# 결론

### “WebDataBinder에 검증기를 추가하고, @Validated를 쓰면

### 컨트롤러가 validate()를 직접 호출할 필요가 없이

### 스프링이 자동으로 검증기를 실행해준다.”

지금 코드에 적용하고 싶다면
자동 검증 버전으로 리팩터링한 완성 코드를 만들어드릴까요?

스프링 MVC에서 **Validator를 글로벌로 설정(global validator 등록)** 하면
모든 컨트롤러에서 자동으로 검증이 적용됩니다.
즉, 개별 컨트롤러마다 `@InitBinder` 를 넣지 않아도 되고,
전역(default) validator처럼 동작합니다.

아래에서 글로벌 설정을 적용하는 3가지 정식 방법을 모두 정리해드릴게요.

---

# ✔ 글로벌 Validator 설정 방법 3가지

---

# 1) WebMvcConfigurer에서 글로벌 Validator 등록 (실무 가장 많이 사용)

스프링 MVC 설정 클래스에 다음처럼 등록합니다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ItemValidator itemValidator;

    public WebConfig(ItemValidator itemValidator) {
        this.itemValidator = itemValidator;
    }

    @Override
    public Validator getValidator() {
        return itemValidator;  // 글로벌 Validator 설정
    }
}
```

이렇게 하면:

* 모든 컨트롤러의 `@Validated` 가 이 Validator를 사용함
* WebDataBinder의 기본 Validator가 itemValidator로 대체됨
* Bean Validation(예: Hibernate Validator)이 있다면 이것을 덮어쓰기 때문에 조심해야 함

즉, 프로젝트 전체에 ItemValidator가 기본 검증기로 쓰입니다.

---

# 2) WebDataBinder에 Validator를 글로벌로 추가하기 (addValidators)

이 방법은 글로벌이기는 한데 "추가" 방식입니다.
즉, Bean Validation을 제거하지 않고 덧붙입니다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ItemValidator itemValidator;

    public WebConfig(ItemValidator itemValidator) {
        this.itemValidator = itemValidator;
    }

    @Override
    public void addValidators(Validator... validators) {
        // 스프링 6에서는 addValidators가 제거됨
        // WebMvcConfigurer에 없으면 아래 방식 참조
    }
}
```

Spring 6 이후로는 이 방식은 사용하지 않습니다.

---

# 3) LocalValidatorFactoryBean을 글로벌 validator로 등록

Bean Validation(JSR-380, @NotNull 등)도 함께 쓰고 싶은 경우
Global Validator를 등록하는 가장 깔끔한 방식입니다.

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ItemValidator itemValidator;

    public WebConfig(ItemValidator itemValidator) {
        this.itemValidator = itemValidator;
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Override
    public Validator getValidator() {
        return validator();   // Bean Validation을 글로벌 기본값으로 사용
    }
}
```

이렇게 하면:

* @NotNull 등 Bean Validation이 전역 적용
* ItemValidator는 수동으로만 사용 가능

그리고 ItemValidator도 글로벌로 함께 쓰고 싶다면
2가지를 묶어 CompositeValidator를 만들어야 합니다.

---

# ✔ 글로벌로 ItemValidator 적용하고 싶다면 가장 좋은 패턴

## 1) WebConfig에 등록

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ItemValidator itemValidator;

    public WebConfig(ItemValidator itemValidator) {
        this.itemValidator = itemValidator;
    }

    @Override
    public Validator getValidator() {
        return itemValidator;     // 글로벌 Validator 지정
    }
}
```

## 2) 컨트롤러에서는 @Validated만 붙이면 됨

```java
@PostMapping("/add")
public String addItem(
        @Validated @ModelAttribute("item") Item item,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
) {
    if (bindingResult.hasErrors()) {
        return "validation/v1/addForm";
    }

    ...
}
```

이렇게 하면:

* 모든 컨트롤러에서 Validator가 자동으로 동작
* validate()를 호출할 필요 없음
* @InitBinder도 필요 없음
* 코드가 매우 간결해짐

---

# ✔ 글로벌 Validator의 장점

* 컨트롤러가 깨끗해짐
* 검증 호출을 깜빡하는 오류가 사라짐
* Validator가 여러 컨트롤러, 여러 객체에서 재사용 가능
* 프로젝트 규모가 커질수록 더 유효함

---

# ✔ 단점도 있음

* 특정 컨트롤러나 특정 요청에만 다른 검증을 적용하고 싶을 때 제한됨
* Bean Validation(예: @Valid @NotNull) 같은 전역 Validator와 충돌 가능
* 많은 Validator가 있으면 어떤 Validator가 실행되는지 파악 어려움

그래서 대부분의 실무 프로젝트는:

### - “정말 전역으로 적용할 대상”만 WebMvcConfigurer에 등록

### - 나머지는 @InitBinder 또는 수동 호출로 개별 적용

이 패턴을 사용합니다.

---

# ✔ 결론

### “글로벌 설정 = WebMvcConfigurer의 getValidator()에서 Validator를 등록하는 것”

이렇게 하면 @Validated만 있어도 자동 검증이 실행됩니다.

원하면 당신 프로젝트를 기준으로
지금 ItemValidator를 글로벌로 적용하는 최적 예시 코드를 만들어드릴게요.
