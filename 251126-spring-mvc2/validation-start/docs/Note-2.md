두 문장은 스프링 MVC의 **BindingResult 규칙**을 설명하는 핵심 개념입니다.
아래에 각각을 **왜 필요한지**, **어떤 일이 벌어지는지**, **예제와 함께** 알기 쉽게 설명해드립니다.

---

# 1. “BindingResult는 검증할 대상 바로 다음에 와야 한다”

이 말은 컨트롤러 메서드 파라미터에서 아래 순서를 꼭 지켜야 한다는 뜻입니다.

```java
public String save(@ModelAttribute Item item,
                   BindingResult bindingResult)
```

즉,

* **검증할 객체** (`item`)
* **해당 객체의 검증 결과** (`bindingResult`)

이 둘이 반드시 **연속해서** 와야 한다는 규칙입니다.
중간에 다른 파라미터가 끼면 안 됩니다.

### 왜 이런 규칙이 필요할까?

스프링은 내부적으로 다음과 같은 방식으로 동작합니다:

1. @ModelAttribute 로 바운딩한 객체를 찾음 → `item`
2. 그 객체에 대한 BindingResult를 자동 생성 → `BindingResult itemBindingResult`
3. 스프링이 생성한 BindingResult를 **바로 그 뒤에 연결**해서 파라미터에 주입

예시 (스프링 내부 매핑):

```
item -> new Item()
bindingResult -> new BindingResult(item)
```

만약 순서를 어기면 스프링은
“어떤 객체의 검증 결과를 이 bindingResult에 넣어야 하지?”
하고 매핑을 못하게 됩니다.

→ 검증 오류가 발생해도 BindingResult에 들어가지 않고
→ 400 에러가 나거나, 오류 처리가 안 됩니다.

### 틀린 예 (동작 안 됨):

```java
public String save(@ModelAttribute Item item,
                   RedirectAttributes redirectAttributes,
                   BindingResult bindingResult) // 잘못된 위치
```

BindingResult가 item 뒤에 바로 있지 않으니 스프링이 매핑을 못함.

---

# 2. “BindingResult는 model에 자동으로 포함된다”

이 말은 다음과 같습니다.

컨트롤러에서 BindingResult를 받기만 하면
스프링이 이를 뷰로 자동으로 보내준다는 뜻입니다.

즉, 별도로 넣지 않아도 됩니다.

```java
model.addAttribute("bindingResult", bindingResult); // 이런 거 필요 없음
```

왜냐하면 스프링이 아래와 같은 구조를 자동으로 model에 추가하기 때문입니다.

```
model
 ├─ item
 ├─ BindingResult.item
```

즉, `BindingResult.item` 이라는 이름으로 자동으로 들어갑니다.

그래서 Thymeleaf에서 이런 코드를 사용할 수 있는 것입니다:

* `${#fields.hasErrors('itemName')}`
* `th:errors="*{itemName}"`

이런 기능은 전부 Model 안에
`BindingResult.item` 이 들어있기 때문에 영향을 받을 수 있습니다.

---

# 3. 두 문장은 서로 연결됨

자동으로 model에 포함되기 위해서는
컨트롤러 파라미터에서 “검증대상 바로 뒤에” 있어야 합니다.

스프링이 이름을 연결해야 하니까요.

```
Item item  → BindingResult itemBindingResult
```

이 연결을 알기 위한 규칙입니다.

---

# 4. 전체 예시로 보면 더 잘 이해됨

```java
@PostMapping("/add")
public String addItem(@ModelAttribute("item") Item item,
                      BindingResult bindingResult,
                      Model model) {

    // 에러 추가
    if (item.getPrice() < 1000) {
        bindingResult.rejectValue("price", "range", "가격은 1000 이상");
    }

    if (bindingResult.hasErrors()) {
        // BindingResult는 자동으로 model에 포함되므로
        // 따로 model.addAttribute() 할 필요 없음
        return "addForm";
    }

    return "success";
}
```

뷰에서는 이렇게 동작함:

```html
<div th:errors="*{price}"></div>
```

이게 가능한 이유:

* BindingResult가 model에 자동 추가됨
* BindingResult는 item 바로 뒤에서 받아서 item과 매핑되어 있음

---

# 정리

### 1) “검증대상 바로 뒤에 와야 한다”

스프링이 BindingResult를 해당 객체의 검증 결과라고 인식하려면
두 파라미터가 **연속**되어 있어야 한다.

### 2) “BindingResult는 model에 자동으로 포함된다”

스프링이 자동으로 뷰에 보내주므로
model.addAttribute() 하지 않아도 된다.

---

원하시면
직접 작성한 컨트롤러 코드를 보고
BindingResult 위치나 구조가 맞는지 검토해드릴게요.

아래에서 **BindingResult에 검증 오류를 적용하는 3가지 방법**을
**스프링이 FieldError 자동 생성**, **개발자가 직접 FieldError 생성**,
**Validator 사용** 방식으로 정확하게 설명해드리겠습니다.

이 세 가지는 스프링 검증의 핵심 구조입니다.

---

# 1. 스프링이 FieldError를 자동 생성하는 방식 (바인딩 시 자동 오류)

스프링 MVC는 바인딩 과정에서 타입 변환 실패·필수값 누락 같은 오류가 발생하면
BindingResult에 **FieldError를 자동으로 넣어줍니다**.

예를 들어:

```java
<input type="number" name="price">
```

사용자가 price에 “열만원” 같은 문자열을 입력하면:

* Integer 변환 실패
* 스프링이 자동으로 FieldError 생성
* BindingResult 에 addError 자동 수행

컨트롤러에서는 이렇게 보임:

```java
@PostMapping("/add")
public String addItem(@ModelAttribute("item") Item item,
                      BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
        return "addForm";
    }

    return "success";
}
```

bindingResult에 자동으로 이런 오류가 들어있음:

```
Field error in object 'item' on field 'price':
rejected value [열만원]; default message [숫자를 입력하세요]
```

### 요약

사용자가 잘못된 타입 값을 넣으면
**스프링이 자동으로 FieldError 생성 → BindingResult에 저장**

---

# 2. 개발자가 직접 FieldError 만들기 (수동 검증)

직접 검증 로직을 만들고 오류를 추가할 때 사용합니다.

```java
if (item.getPrice() < 1000 || item.getPrice() > 2000) {
    bindingResult.addError(
        new FieldError("item", "price", item.getPrice(),
                       false, null, null,
                       "가격은 1000~2000이어야 합니다.")
    );
}
```

### FieldError 주요 생성자

```java
new FieldError(
    "타겟 객체 이름",      // "item"
    "필드명",              // "price"
    거부된 값,              // item.getPrice()
    bindingFailure 여부,    // false
    오류 코드 배열,         // {"range.item.price"}
    메시지 치환 파라미터,   // null
    기본 메시지            // "가격은 1000~2000 이어야 합니다"
)
```

하지만 보통 이렇게 길게 만들지 않고, 더 간단한 reject 계열을 씁니다.

---

# 3. rejectValue( ) / reject( ) 사용하기 (추천)

FieldError를 직접 만들지 않고
스프링의 메시지 코드를 기반으로 **간접적으로 FieldError 생성**하는 방식입니다.

예시:

```java
bindingResult.rejectValue("price", "range", "가격 범위를 벗어났습니다");
```

실제로는 스프링이 내부에서 FieldError를 만들어 BindingResult에 넣습니다.

### rejectValue()

특정 필드 오류일 때 사용.

```java
bindingResult.rejectValue(
    "price",        // 필드명
    "range",        // 메시지 코드
    "가격 범위를 벗어났습니다"   // 기본 메시지
);
```

### reject()

글로벌 오류일 때 사용.

```java
bindingResult.reject("totalPriceMin");
```

---

# 4. Validator 사용 (전문 검증 클래스 사용)

스프링에서 권장하는 구조화된 검증 방식입니다.

## 4-1. Validator 구현

```java
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Item item = (Item) target;

        if (!StringUtils.hasText(item.getItemName())) {
            errors.rejectValue("itemName", "required");
        }

        if (item.getPrice() < 1000 || item.getPrice() > 2000) {
            errors.rejectValue("price", "range", new Object[]{1000, 2000}, null);
        }
    }
}
```

## 4-2. 컨트롤러에서 사용

```java
ItemValidator validator = new ItemValidator();

@PostMapping("/add")
public String addItem(@ModelAttribute("item") Item item,
                      BindingResult bindingResult) {

    validator.validate(item, bindingResult);

    if (bindingResult.hasErrors()) {
        return "addForm";
    }

    return "success";
}
```

### 장점

* 검증 로직이 분리됨
* 재사용 가능
* 테스트가 쉬움
* Spring Boot에서는 @InitBinder로 자동 적용도 가능

---

# 5. 세 방식 비교

| 방식                          | 특징                         |
| --------------------------- | -------------------------- |
| **스프링 자동 FieldError 생성**    | 타입 변환 오류, 필수값 오류 등 자동 처리   |
| **개발자가 FieldError 직접 생성**   | 상세 제어 필요할 때 사용하지만 코드가 지저분함 |
| **rejectValue / reject 사용** | 가장 실용적이며 메시지 코드 기반으로 깔끔    |
| **Validator 사용**            | 검증 로직 분리, 재사용성 높음, 구조화된 방식 |

---

# 6. 추천하는 사용 순서

실무에서는 **3 → 4 → 1 순으로 활용**합니다.

1. **rejectValue() / reject()**
2. 필요하면 **Validator 분리**
3. 타입 오류는 **스프링이 자동 처리하도록 맡김**

FieldError를 직접 생성하는 방식은 잘 안 씁니다.

---

원하시면 지금 작성 중인 검증 로직(Map 방식)을
BindingResult + rejectValue() 기반 구조로 완전히 리팩토링해드릴게요.

결론부터 말하면,

**네. BindingResult → FieldError → #fields 에 “거부된 값(rejectedValue)”이 보이게 하려면
FieldError 내부에 rejectedValue 가 들어 있어야 합니다.**

하지만
**반드시 직접 넣을 필요는 없습니다.**
스프링이 자동으로 넣어주는 경우도 있고,
개발자가 직접 FieldError 를 만들 때만 신경 쓰면 됩니다.

아래에서 상황별로 정확하게 정리해드릴게요.

---

# 1. 스프링이 자동으로 오류를 만들면 rejectedValue도 자동 저장됨

예: price=int 필드인데 “abc” 같은 문자열을 입력한 경우

컨트롤러:

```java
@PostMapping("/add")
public String addItem(@ModelAttribute("item") Item item,
                      BindingResult bindingResult) {
    …
}
```

폼에서 price = "abc" 를 보내면 스프링이 바인딩 오류를 만듭니다.

이때 FieldError 내부에는:

```
rejectedValue = "abc"
```

이걸 바탕으로 Thymeleaf에서 입력값이 유지되고
#fields.rejectedValue('price') 로도 접근이 가능합니다.

즉, **타입 변환 오류나 바인딩 오류는 스프링이 rejectedValue를 자동으로 채운다.**

따로 넣을 필요 없음.

---

# 2. rejectValue() 를 쓰면 rejectedValue 자동 저장

예:

```java
bindingResult.rejectValue("price", "range", "가격 오류");
```

여기서 rejectedValue 를 넘기지 않았는데도
BindingResult 내부에는 자동으로 다음 값이 들어갑니다:

```
rejectedValue = item.getPrice()
```

즉, 검증 대상 객체의 해당 필드 값이 자동으로 들어간다는 뜻입니다.

그래서 입력값이 화면에 그대로 유지됩니다.

---

# 3. 개발자가 FieldError 를 “직접 생성”할 때만 rejectedValue 를 넣어줘야 한다

예:

```java
bindingResult.addError(
    new FieldError("item", "price", item.getPrice(),
                   false, null, null,
                   "가격이 1000~2000 범위가 아닙니다.")
);
```

여기서 `item.getPrice()`가 바로 rejectedValue 입니다.

만약 이 값을 null 로 넣으면:

* 화면에 입력값이 유지되지 않음
* #fields.rejectedValue('price') 도 null

즉, **수동 생성 시에는 rejectedValue 를 꼭 넣어야 값이 유지됨.**

---

# 4. rejectedValue 는 어디에 쓰이나?

Thymeleaf에서 입력값을 유지하는 기능:

```html
<input th:field="*{price}">
```

이 입력 필드의 value는 다음 우선순위로 결정됩니다.

1. **오류 발생 시, rejectedValue**
2. 정상 바인딩된 item.price 값

즉, 사용자가 입력한 “잘못된 값”을 화면에 그대로 보여줄 때
반드시 rejectedValue 가 필요합니다.

---

# 5. 정리

## ✔ rejectedValue가 필요한 경우

개발자가 FieldError 를 직접 만드는 경우

```java
new FieldError("item", "price", item.getPrice(), …)
```

## ✔ rejectedValue를 직접 안 넣어도 되는 경우

* 스프링이 바인딩 오류 자동 생성
* rejectValue() 사용
* Validator 사용(errors.rejectValue())

이런 경우 모두 스프링이 rejectedValue를 알아서 넣어줌.

---

# 결론

### 질문: “오류 값을 fields에 저장하려면 rejectedValue 인자에 값을 써야 돼?”

* **직접 FieldError를 만들 때만 넣어야 함**
* rejectValue(), Validator, 바인딩 오류 등에서는 **자동으로 들어감**
* 그래서 일반적으로는 rejectedValue를 직접 넣을 일이 거의 없음

---

지금 작성 중인 오류 처리 방식(Map→BindingResult)으로 옮긴 코드를 보여주시면
어떤 방식으로 rejectedValue가 동작할지 최종 구조를 정확히 짚어드릴게요.
