`new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null)`
이 생성자에서 각 인자가 어떤 의미인지 정리해드리고,
메시지 코드(`required.item.itemName`)가 메시지 파일(errors.properties)에 정의되어 있다면
**정확히 이 형태로 인식된다**는 것도 설명해드리겠습니다.

---

# 1. FieldError 생성자 인자 의미

Spring의 FieldError 주요 생성자:

```java
public FieldError(
    String objectName,     // 1
    String field,          // 2
    Object rejectedValue,  // 3
    boolean bindingFailure,// 4
    String[] codes,        // 5
    Object[] arguments,    // 6
    String defaultMessage  // 7
)
```

아래는 제공하신 예시입니다.

```java
new FieldError(
    "item",                    // 1
    "itemName",                // 2
    item.getItemName(),        // 3
    false,                     // 4
    new String[]{"required.item.itemName"}, // 5
    null,                      // 6
    null                       // 7
)
```

각 항목을 자세히 설명합니다.

---

# 1) objectName

```java
"item"
```

* @ModelAttribute("item")의 이름
* 해당 오류가 어떤 객체(item)의 필드 오류인지 지정하는 역할

---

# 2) field

```java
"itemName"
```

* 오류가 발생한 객체의 필드 이름
* 여기서는 Item 클래스의 itemName

---

# 3) rejectedValue

```java
item.getItemName()
```

* 사용자가 입력했던 값 그대로 저장됨
* 화면에 값 유지하려면 반드시 필요 (입력값 사라지지 않게)

---

# 4) bindingFailure

```java
false
```

* true = 바인딩 자체 실패(타입 변환 실패 등)
* false = 검증 실패(로직상 오류)
* 여기서는 검증 실패이므로 false

---

# 5) codes

```java
new String[]{"required.item.itemName"}
```

이게 “메시지 코드 우선순위 배열”입니다.

즉, 스프링은 이 배열 안의 코드들을 순서대로 messageSource에서 찾습니다.

예:

```
required.item.itemName
required.itemName
required.item
required
```

이런 식으로 fallback 하도록 매핑하는 것이 일반적입니다.

### 메시지 파일에 정의했다면 그대로 작동합니다

예:

**errors.properties**

```
required.item.itemName=상품 이름을 입력하세요
```

Spring은 이 코드를 messageSource에서 찾아서 FieldError의 메시지를 결정합니다.

즉, **new String[]{"required.item.itemName"}** 라고만 넣어도 인식합니다.

---

# 6) arguments

```java
null
```

메시지에서 파라미터 치환할 값이 있을 때 사용합니다.

예:

```
range.item.price=가격은 {0}~{1}이어야 함
```

이런 경우:

```java
new Object[]{1000, 2000}
```

을 넣습니다.

지금은 필요 없으므로 null.

---

# 7) defaultMessage

```java
null
```

* 메시지 파일에 코드가 없을 때 사용하는 메시지
* 보통 별도 메시지가 없으면 null로 둠
  → 스프링이 코드 자체를 메시지로 쓰지 않고, 빈 메시지를 출력하려고 시도함

보통 rejectValue를 사용하면 defaultMessage에 값을 넣을 수 있습니다.

---

# 2. 메시지 코드 인식 방식

**Q: “required.item.itemName는 errors.properties에 정의됐는데 이렇게만 써도 인식돼?”**

→ **네. 정확하게 인식됩니다.**

메시지 파일이 등록돼 있으면 스프링은 다음 순서로 messageSource에서 값을 찾습니다.

### 코드 배열 → arguments → defaultMessage

예:

```java
codes = {"required.item.itemName"}
```

Spring은 messageSource에 아래 파일들에서 코드를 찾습니다:

* messages.properties
* errors.properties
* messages_ko.properties
* errors_ko.properties
* config.i18n.messages.properties
  (spring.messages.basename 에 등록된 모든 basename)

즉, basename에 포함된 파일이면 어떤 파일이든 OK.

---

# 3. but! 실무에서는 이 생성자 거의 사용하지 않음

왜냐면 너무 길고 불편하기 때문입니다.

스프링은 아래처럼 간단하게 쓰도록 메서드를 제공합니다.

```java
errors.rejectValue("itemName", "required.item.itemName");
```

이렇게 하면:

* 메시지 코드 적용
* rejectedValue 자동 적용
* defaultMessage 옵션 가능

전부 자동으로 처리됩니다.

그래서 FieldError 직접 생성은 특수한 경우 외에는 잘 안 씁니다.

---

# 최종 요약

### ✔ new FieldError(...) 생성자의 각 인자 의미

* objectName = "item"
* field = "itemName"
* rejectedValue = 사용자가 입력한 값
* bindingFailure = 타입 변환 실패 여부
* codes = 메시지 코드 배열
* arguments = 메시지 파라미터
* defaultMessage = 메시지 파일 못 찾았을 때 출력할 문자열

### ✔ required.item.itemName 은 messageSource에서 자동 인식됨

* basename에 포함된 message 파일에 있으면 그대로 적용됨

---

원하시면
메시지 코드 전략(4단계 fallback 구조)과 실제 프로젝트 메세지 프로파일 구성도 정리해드릴게요.

말씀하신 것처럼
**“그냥 String으로 저렇게 쓰고 싶었을 수도 있잖아”**
라는 의도가 무엇인지 정확히 이해했습니다.

즉,

```java
new String[]{"required.item.itemName"}
```

이게 “메시지 코드”가 아니라 **그냥 String 배열일 뿐이고**,
개발자는 그저 문자열을 하나 넣은 것뿐인데…

**Spring이 messageSource에서 코드를 찾아서 메시지로 사용한다는 것이 자연스러운가?**
**그냥 String인데 어떻게 메시지 코드로 동작하나?**

이런 의문을 말하는 거죠.

정확하게 설명해드릴게요.

---

# 핵심 답변

### **FieldError 생성자의 `codes` 인자는 ‘문자열이지만 메시지 코드로 취급되는 특별한 필드’다.**

Spring은 `FieldError`의 `codes` 배열을 **그냥 문자열로 보지 않고**
**메시지 코드를 찾기 위한 key 목록**으로 해석하도록 설계되어 있습니다.

즉, 단순한 String 배열처럼 보이지만
Spring은 FieldError 내부의 이 값들을
**메시지 코드 후보군**으로 간주합니다.

그래서 아래처럼 작성하면:

```java
new String[]{"required.item.itemName"}
```

이건 일반 문자열이 아니라
**“메시지 코드 required.item.itemName 을 사용해라”**
라는 의미가 됩니다.

---

# 왜 그냥 String 이 메시지 코드로 인식될까?

Spring MessageSource는 다음 두 곳에서 메시지를 찾습니다.

1. `FieldError.getCodes()`
2. `ObjectError.getCodes()`

그리고 `codes`를 “문자열 배열”로 설계한 이유는
필드에 대한 메시지 우선순위를 표현하기 위해서입니다.

예시:

```
required.item.itemName
required.itemName
required.item
required
```

이런 식의 fallback 구조를 위해 배열이 필요합니다.

---

# “그냥 String이라면 defaultMessage처럼 쓸 수는 없어?”

이 질문의 의미는 다음과 같습니다:

**“String이 메시지 코드가 아닌 그냥 출력 문자열이면 안 되나?”**

가능합니다.
그 경우는 `defaultMessage`를 사용합니다.

FieldError 생성자 마지막 인자:

```java
defaultMessage
```

여기에 문자열을 넣으면 messageSource를 보지 않고
그 문자열 그대로 출력합니다.

예:

```java
new FieldError("item", "itemName", item.getItemName(),
               false, null, null,
               "그냥 이렇게 출력하고 싶다")
```

이러면 메시지 파일을 무시하고
"그냥 이렇게 출력하고 싶다"
가 바로 에러 메시지로 사용됩니다.

---

# 실무에서 세 가지 방식

### 1) 메시지 코드 사용하고 싶을 때

```java
new String[]{"required.item.itemName"}
```

### 2) 그냥 문자열 출력하고 싶을 때

```java
defaultMessage = "그냥 이 문자열을 쓰겠다"
```

### 3) 메시지 코드 + 치환 파라미터가 필요할 때

```java
new FieldError("item", "price", item.getPrice(),
               false, new String[]{"range"}, new Object[]{1000,2000}, null)
```

---

# 정리

* `codes`는 “그냥 String 배열처럼 보이지만”
  스프링에서는 **메시지 코드 목록을 담는 용도**로 정해진 필드다.

* 파일에 코드가 정의되어 있다면 자동으로 메시지로 치환된다.

* 문자열을 코드가 아니라 **그냥 직접 출력하고 싶으면**
  `defaultMessage` 인자를 사용해야 한다.

---

필요하시면
FieldError / ObjectError / 메시지 코드 우선순위의 전체 흐름도도 정리해드릴게요.

지금 상황은 **BindingResult에 넣은 메시지 코드가 의도한 필드만이 아니라, 모든 필드에 적용된 것처럼 보이는 문제**인데, 이 현상은 다음 두 가지 중 하나가 원인입니다.

---

# ✔ 원인 1: 컨트롤러에서 BindingResult 순서가 잘못됨

현재 코드를 보면 BindingResult가 **@ModelAttribute 바로 뒤에 있지 않고**,
`RedirectAttributes` 뒤에 있습니다.

```java
public String addItem(
        @ModelAttribute Item item,
        RedirectAttributes redirectAttributes,
        BindingResult bindingResult   // ← 여기가 문제
)
```

**BindingResult는 검증 대상 바로 다음에 와야 합니다.**

즉, 이렇게 되어야 합니다:

```java
public String addItem(
        @ModelAttribute Item item,
        BindingResult bindingResult,   // ← 바로 뒤에 와야 함
        RedirectAttributes redirectAttributes
)
```

이 규칙을 어기면 어떤 일이 발생하냐?

## ✔ 스프링은 BindingResult를 item과 연결시키지 못함

→ BindingResult가 "item"의 오류를 위한 객체인지 몰라서
→ BindingResult 자체가 Model에 `BindingResult.item` 이라는 이름으로 들어가지 않음
→ Thymeleaf는 해당 BindingResult를 “폼 전체의 글로벌 오류”처럼 처리함
→ 그래서 **필드에 상관없이 오류 메시지가 모든 필드에 표시됨**

이는 스프링의 공식 동작입니다.

### 즉: BindingResult 매핑이 실패하면 오류가 각 필드에 바르게 매칭되지 않아서

**모든 th:errors="*{필드}" 가 전부 동일한 오류 메시지를 가져오게 됩니다.**

---

# ✔ 원인 2: 메시지 코드의 fallback 우선순위 때문에 모든 필드가 “???” 로 표시됨

스프링이 메시지를 찾을 때 우선순위는 다음과 같습니다:

```
1) required.item.itemName
2) required.itemName
3) required
4) 코드 자체 출력 (ex: ???required.item.itemName???)
```

만약 messages.properties / errors.properties 중 어디에서도
**required.itemName** 또는 **required** 도 정의되어 있지 않다면,

fallback으로 **???required.itemName???** 또는 **???required???** 같은 문자열이
**폼의 모든 th:errors 영역에 노출됩니다.**

이 때문에 “왜 모든 필드가 ?? 메시지를 출력하지?” 하고 보이게 된 겁니다.

---

# ✔ 실제 근본 문제: BindingResult가 제대로 매핑되지 않음

이 증상은 거의 100% 다음 한 가지 케이스에서 발생합니다.

## ⚠ BindingResult가 @ModelAttribute 뒤에 위치하지 않는다

그래서 bindingResult가 item에 매핑되지 않고
**리스트(글로벌 오류)**처럼 처리됨.

---

# ✔ 해결 방법: 파라미터 순서 수정하면 모든 문제 해결

반드시 이렇게 되어야 합니다.

```java
@PostMapping("/add")
public String addItem(
        @ModelAttribute("item") Item item,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
) {
    …
}
```

순서만 바꿨는데도 갑자기:

* 오류 메시지가 특정 필드에만 정확히 표시되고
* 다른 필드에는 전혀 영향 없고
* “???” 메시지가 사라지고
* th:errors 도 정상 동작하고
* rejectedValue(입력값 유지)도 정상

이런 정상 동작으로 돌아옵니다.

---

# ✔ 참고: 메시지 파일에서 반드시 fallback 메시지도 정의해야 함

예:

```
required=값을 입력하세요
range=범위를 벗어났습니다
```

fallback이 없으면 스프링이 ???

---

# ✔ 요약

### 문제 원인

* BindingResult 위치가 잘못됨
* 메시지 코드 fallback이 없어 “???” 노출

### 해결

1. 컨트롤러 파라미터 순서 수정
2. 메시지 파일 fallback 메시지 추가(optional)

---

원하시면 메시지 파일 구성(errors.properties / messages.properties)을
지금 사용 중인 코드에 맞게 최적화해서 만들어드릴게요.

`??label.item.id_ko_KR??` 이런 형태가 나온다는 것은
**BindingResult 문제는 이미 해결되었고**,
이제 남은 문제는 **메시지 코드를 메시지 파일에서 찾지 못한다는 의미**입니다.

즉, 스프링이 메시지를 찾으려고 다음 코드를 사용했는데,

```
label.item.id_ko_KR
```

이게 **messageSource 안 어떤 파일에서도 정의돼 있지 않아서**
스프링이 “??코드??” 형태로 그대로 출력한 겁니다.

---

# ✔ 왜 label.item.id 라는 메시지 코드를 찾으려고 하는가?

스프링 MVC에서 `<label th:for="*{id}">` 또는 `<th:field>` 를 사용할 때
form 태그를 위한 "라벨 자동 메시지" 기능이 있습니다.

Thymeleaf는 다음의 메시지 코드를 순서대로 찾습니다:

```
label.<objectName>.<fieldName>_<locale>
label.<objectName>.<fieldName>
label.<fieldName>
```

즉, item 객체의 id 필드를 렌더링할 때
아래 코드들을 찾습니다:

```
label.item.id_ko_KR
label.item.id
label.id
```

그런데 이 중 **아무 것도 messageSource에 없으니까**
스프링은 `??label.item.id_ko_KR??` 로 출력합니다.

---

# ✔ 즉, 지금 출력되는 오류 메시지는 “검증 메시지”가 아니라 “라벨 메시지”임

---

# ✔ 해결 방법 3가지 중 하나를 선택하면 됩니다

---

# 1) 메시지 파일에 해당 라벨 코드를 추가

`messages.properties` 혹은 `messages_ko.properties` 또는
당신이 설정한 basename 파일(예: errors.properties)에 아래 추가:

```
label.item.id=ID
label.item.itemName=상품명
label.item.price=가격
label.item.quantity=수량
```

한국어/영어 둘 다 쓰면:

**messages_ko.properties**

```
label.item.id=상품 ID
label.item.itemName=상품명
label.item.price=가격
label.item.quantity=수량
```

**messages.properties**

```
label.item.id=Item ID
label.item.itemName=Item Name
label.item.price=Price
label.item.quantity=Quantity
```

---

# 2) 라벨 자동 메시지 기능을 사용하지 않는 방법

템플릿에서:

```html
<label th:text="#{label.item.id}">ID</label>
```

또는 그냥 하드코딩:

```html
<label>ID</label>
```

이렇게 하면 메시지 코드를 찾기 위해 자동으로 label.* 를 만들지 않습니다.

---

# 3) 자동 라벨 메시지를 막기 (추천 X)

모든 input 태그에 th:field 대신 name/id를 수동 지정:

```html
<input type="text" name="itemName" th:value="*{itemName}">
```

하지만 이러면 스프링 검증/값 유지 기능이 깨지므로 추천하지 않습니다.

---

# ✔ 결론

`??label.item.id_ko_KR??` 는 스프링이 “검증 메시지”를 못 찾은 게 아니라
**폼 필드 라벨 메시지를 찾지 못해서 발생한 것**입니다.

그리고 이 문제는 메시지 파일에 해당 코드를 추가하면 즉시 해결됩니다.

---

원하시면
현재 템플릿 파일을 보여주시면,
사용 중인 모든 input/label에 필요한 메시지 코드를 자동으로 정리해서
messages.properties / messages_ko.properties 두 파일을 완전하게 만들어드릴게요.

네, **맞습니다. 정확히 그 설정 때문에 지금 문제가 생긴 것입니다.**

```
spring.messages.basename=errors
```

이 설정을 하면 스프링은 **오직 errors.properties / errors_ko.properties**
**딱 이 두 개의 파일만** 메시지 소스로 사용합니다.

그런데 화면에는 다음 메시지를 사용하려고 합니다:

```
label.item.id_ko_KR
label.item.itemName
label.item.price
label.item.quantity
```

이런 라벨 메시지들은 일반적으로
`messages.properties` / `messages_ko.properties` 에 들어가는 항목들입니다.

하지만 지금 Spring Boot는 메시지를 오직 **errors.properties에서만 찾도록 설정**되어 있기 때문에:

```
??label.item.id_ko_KR??
```

이런 “메시지 못 찾았다” 형태가 화면에 나오는 것입니다.

즉,

### ✔ 메시지 코드가 없어서가 아니라

### ✔ 메시지 파일 자체에 접근하지 못하고 있어서 발생한 문제입니다.

---

# 해결 방법 2가지

---

# 1) 권장: basename을 여러 개 등록하기

스프링은 쉼표(,)로 여러 개 등록 가능합니다.

```properties
spring.messages.basename=messages,errors
```

또는

```properties
spring.messages.basename=messages,errors,config.i18n.messages
```

이렇게 하면:

* 검증 메시지 → errors.properties
* 화면 라벨, UI 메시지 → messages.properties
* 커스텀 위치 메시지 → config.i18n.messages.properties

처럼 자연스럽게 분리됩니다.

이렇게 설정하면
`label.item.id_ko_KR` 같은 것들은 messages.properties에서 찾습니다.

---

# 2) 모든 메시지를 errors.properties 하나에 몰아넣기

지금 설정은 “errors만 쓴다”는 의미이므로
라벨 메시지까지 errors.properties에 직접 넣어도 됩니다.

예:

```
label.item.id=상품 ID
label.item.itemName=상품명
label.item.price=가격
label.item.quantity=수량
```

하지만 검증 메시지와 UI 메시지를 한 파일에 다 넣으면
실무에서는 관리가 힘들기 때문에 권하지 않습니다.

---

# 결론

### **지금 나타나는 ??label.item.id_ko_KR?? 문제는 정확하게

`spring.messages.basename=errors` 때문입니다.**

### 해결하려면

`messages` 파일을 포함시키거나
errors 파일에 라벨 메시지를 모두 넣어야 합니다.

---

원하시면
현재 프로젝트에 맞는 **messages.properties / errors.properties 구조**를 함께 구성해드릴 수 있습니다.

좋은 관찰력이고, “message.properties는 없는데도 왜 잘 작동하지?”라는 질문은
스프링 메시지 처리의 **fallback 전략**을 정확히 이해하면 바로 해석됩니다.

정답은 간단합니다.

### ✔ Spring Boot는 basename에 지정한 파일이 “존재하지 않아도 오류를 내지 않으며”,

### ✔ 존재하는 파일만 로딩해서 MessageSource를 구성합니다.

즉,
`spring.messages.basename=messages,errors`
이라고 했을 때

* `messages.properties` → 없음
* `errors.properties` → 있음

이라면 스프링은 다음처럼 처리합니다:

```
MessageSource:
  - messages (파일 없으므로 무시)
  - errors (파일 있으므로 로딩됨)
```

즉, **존재하는 파일만 유효하게 작동**합니다.
존재하지 않는 basename은 단순히 스킵됩니다.
오류가 발생하지 않기 때문에 문제 없이 동작하는 것처럼 보이는 것입니다.

---

# 그렇다면 “왜 label.item.* 메시지가 잘 나왔을까?”

결론:

### ✔ label.item.* 메시지를 errors.properties 안에 넣었기 때문입니다.

또는

### ✔ 기본 메시지 코드 자체를 default message로 쓰지 않는 구조였기 때문입니다.

예를 들어 errors.properties에 다음을 넣었다고 가정하면:

```
label.item.itemName=상품명
label.item.price=가격
label.item.quantity=수량
```

그렇다면 messages.properties 없이도 전혀 문제 없습니다.
Spring Boot는 "messages 파일이 반드시 있어야 한다"는 전제가 없습니다.

---

# 핵심 정리

### ✔ basename 목록에 있는 파일이 **존재하지 않아도 문제 없음**

스프링은 단순히 있는 파일만 로딩합니다.

### ✔ 메시지는 messageSource에서만 찾음

errors.properties 하나만 있어도 충분하면 messages.properties 없어도 됨.

### ✔ label.* 메시지가 잘 나오는 이유

그 메시지 코드가 errors.properties 안에 존재하기 때문.

### ✔ messages.properties는 “관습적” 파일명일 뿐

꼭 필요한 파일은 아님.
오직 basename에 지정된 파일만 의미가 있습니다.

---

# 결론

* 메시지 두 파일로 분리하고 싶으면 messages.properties 만들어도 되고
* 하나로 관리하고 싶으면 errors.properties만 써도 됨
* basename에는 여러 파일을 지정해도 없음 파일은 단순 무시됨

---

원하시면 errors.properties 파일 내용을 보여주시면
필요한 메시지 코드가 잘 정리되어 있는지도 검토해 드릴게요.

`DefaultMessageCodesResolver` 는 **스프링이 검증 오류 메시지 코드를 자동으로 만들어주는 규칙 클래스**입니다.
FieldError나 ObjectError가 생성될 때 이 Resolver가 메시지 코드 배열을 만들어 줍니다.

당신이 테스트한 코드는 **스프링이 메시지 코드를 어떻게 조합하는지 확인하는 것**인데,
출력되는 배열 형태는 **스프링의 공식 규칙에 100% 정확하게 맞는 정상 동작**입니다.

아래에서 왜 그런지 정확히 설명해드릴게요.

---

# 1. Object Error일 때 (필드가 없는 경우)

```java
String[] messageCodes = mcr.resolveMessageCodes("required", "item");
System.out.println(Arrays.toString(messageCodes));
```

출력:

```
[required.item, required]
```

## ▷ 왜 이런 코드가 나오나?

object-level error일 때 스프링은 **두 단계 메시지 코드**를 생성합니다.

### 1) 가장 구체적인 코드: `"code.objectName"`

```
required.item
```

### 2) fallback 코드: `"code"`

```
required
```

즉 순서:

```
required.item     → 가장 구체적  
required          → fallback
```

정확히 두 단계가 맞습니다.

---

# 2. Field Error일 때

```java
String[] messageCodes = mcr.resolveMessageCodes(
        "required", "item", "itemName", String.class);
```

출력:

```
[required.item.itemName,
 required.itemName,
 required.java.lang.String,
 required]
```

## ▷ 왜 이렇게 4개나 나올까?

이건 스프링의 **FieldError 우선순위 규칙** 때문입니다.

### 규칙: 가장 구체적인 → 덜 구체적인 → 타입 기반 → 최종 fallback

그래서 생성 순서는 아래와 같습니다.

---

## 1) 가장 구체적: code + objectName + field

```
required.item.itemName
```

---

## 2) 중간: code + field

```
required.itemName
```

---

## 3) 타입 기반: code + fieldType

fieldType이 String.class 인 경우:

```
required.java.lang.String
```

---

## 4) 최종 fallback: code

```
required
```

---

# 3. 왜 이렇게 계층적으로 만들까?

스프링 검증 메시지 국제화에서 가장 정교한 메시지 우선순위를 제공하기 위해서입니다.

예를 들어 `required.item.itemName` 이 정의되어 있으면 **이 메시지가 사용**되고,
없으면 `required.itemName`
그래도 없으면 `required.java.lang.String`
그것도 없으면 최종적으로 `required` 를 사용합니다.

이렇게 하면 아주 세밀한 메시지를 줄 수도 있고,
대략적인 메시지를 fallback으로 둘 수도 있습니다.

---

# 4. 직접 만든 메시지 코드도 이 규칙에 따라 매칭된다

당신이 작성한 메시지:

```
required.item.itemName=상품 이름을 입력하세요.
range.item.price=가격은 {0}~{1}을 입력하세요.
range.item.quantity=수량은 {0}~{1}을 입력하세요.
```

이 중

* `required.item.itemName` → 가장 우선순위 높음
* `range.item.price` → price 필드용
* `range.item.quantity` → quantity 필드용

이 메시지만 있어도 충분한 이유는:

**스프링이 자동으로 fallback 코드까지 준비해주기 때문**입니다.

예:
`required.item.itemName`만 넣으면 나머지 코드는 없어도 문제 없음
스프링이 없으면 fallback을 자동으로 찾으니까.

---

# 5. FieldError 생성 시 codes 배열을 넣지 않아도 되는 이유

`rejectValue()` 를 쓰면:

```java
bindingResult.rejectValue("itemName", "required");
```

코드 "required"만 넣었는데도

```java
[required.item.itemName,
 required.itemName,
 required.java.lang.String,
 required]
```

이 4개가 자동으로 만들어짐.

---

# 6. 결론

당신이 본 메시지 code 배열:

```
[required.item.itemName, required.itemName, required.java.lang.String, required]
```

은 **스프링의 FieldError 표준 메시지 우선순위 규칙** 때문이며 다음 목적을 가짐:

* 가장 구체적인 메시지부터 사용
* 없으면 덜 구체적인 것을 사용
* 그래도 없으면 타입 기반 메시지 사용
* 마지막으로 기본 코드 사용

그리고 object-level error는 필드 정보가 없으므로:

```
[required.item, required]
```

를 생성함.

---

원하면 직접 작성한 오류 메시지 파일(errors.properties)을 토대로
메시지 우선순위가 올바르게 적용되는지 검토해드릴게요.

`required.itemName`, `required.java.lang.String`, `required`
이 **세 가지 메시지 코드는 properties 파일에 정의해두지 않았는데도**
스프링이 출력 목록에 포함시키는 이유는,

**DefaultMessageCodesResolver가 “존재 여부와 상관없이”
항상 생성해주는 *표준 메시지 코드 우선순위 목록*이기 때문입니다.**

즉,

* **메시지 파일에 실제로 있든 없든**
* **스프링은 규칙대로 모든 후보 코드를 생성한 뒤**
* **그 중에 “존재하는 코드만 실제 메시지로 사용”**

하는 방식입니다.

따라서 배열에 "없는 코드"가 포함되는 것은 **정상 동작**입니다.

---

# 1. 메시지 파일에 존재하는지와 관계 없이 무조건 생성된다

FieldError 메시지 코드 생성은 다음과 같은 규칙을 가집니다.

필드 오류의 경우:

1. `required.item.itemName`
2. `required.itemName`
3. `required.java.lang.String`
4. `required`

즉, **항상 4단계를 모두 만들어냅니다.**

메시지 파일에 없더라도 무조건 배열에 들어갑니다.

당신의 properties 파일에는 이 중 1번만(예: `required.item.itemName`) 존재할 뿐입니다.

나머지 코드들은:

* 존재하지 않아도 되고
* 메시지 파일에 없어도 되고
* 스프링은 단지 “사용 가능한 후보 코드 목록”으로 만들어둘 뿐입니다.

---

# 2. 실제로 렌더링 시에는 “가장 먼저 찾은 메시지 코드만” 사용한다

예:

```
required.item.itemName=상품 이름을 입력하세요.
```

이 코드가 있으므로 스프링은 다음 순서로 MessageSource를 조회합니다.

1. required.item.itemName → 존재함 → 이 메시지 사용
2. required.itemName → 조회 안 함 (이미 첫 번째에서 성공)
3. required.java.lang.String → 조회 안 함
4. required → 조회 안 함

즉, 배열에 들어간다고 해서 실제로 그 메시지가 화면에 표시되는 게 아닙니다.

---

# 3. 그러면 왜 "없는 코드"를 굳이 배열에 추가할까?

이 이유는 **fallback 설계 때문**입니다.

예를 들어,

```
required.item.itemName
```

이 메시지는 있고,

```
required.itemName
```

이 메시지는 없다고 할 때:

→ 코드 1이 있다면 그것을 사용
→ 코드 1이 없으면 코드 2 사용
→ 코드 2도 없으면 코드 3 사용
→ 마지막에 code("required") 사용

즉, 메시지 세분화 상황을 완전히 제어할 수 있게 하기 위해서입니다.

---

# 4. 스프링은 코드가 “있는지 없는지”를 미리 검사하지 않음

왜냐하면 그건 메시지 파일을 매번 읽어봐야 하기 때문입니다.

그래서 단순히 **정해진 규칙대로 코드 배열을 만들어두고**,
나중에 MessageSource가 **해당 코드를 찾는 순간** 필요한 코드만 사용합니다.

---

# 5. 다시 정리

### 질문:

“required.itemName, required.java.lang.String, required는 없는 건데 왜 나오는 거야?”

### 답:

**DefaultMessageCodesResolver는 메시지가 존재하는지와 관계 없이
항상 정해진 우선순위 규칙에 따라 모든 후보 코드를 생성하기 때문이다.**

그리고 메시지 파일에 실제로 존재하는 코드는
이 후보들 중에서 스프링이 찾아 사용하는 것이다.

---

원하면

* ObjectError 코드 생성 규칙
* FieldError 코드 생성 규칙
* messageSource의 실제 fallback 동작 흐름
  을 다이어그램 형태로 정리해서 더 명확하게 보여드릴 수도 있어요.

핵심을 아주 정확히 짚으셨습니다.
바로 이 지점이 많은 분들이 헷갈려하는 부분입니다.

정리하면 **왜 “required → required.item.itemName / required.itemName …” 은 생성되는데**,
**왜 “itemName → itemName.item.itemName / itemName.itemName …” 같은 건 생성되지 않는가?**
이 질문이죠.

정답은 다음과 같습니다.

---

# ✔ 결론

**MessageCodesResolver는 “검증 코드(code)” 기준으로만 메시지 코드를 확장(fallback)하고,
필드명(field) 자체에서 새로운 메시지 코드를 “역으로” 생성하지 않는다.**

즉:

* `"required"` → 스프링이 규칙에 따라 더 구체적인 코드로 **확장**
* `"itemName"` → 스프링은 이것을 “검증 코드”라고 생각하지 않기 때문에 **확장 안 함**

왜냐하면, 스프링 MVC의 메시지 생성 규칙은
**“항상 검증 코드(code)를 기준으로 메세지 후보들을 생성하는 구조”** 이기 때문입니다.

---

# ✔ DefaultMessageCodesResolver가 메시지 코드를 생성하는 구조

FieldError일 때 다음 규칙을 사용합니다:

```
1) code.objectName.field
2) code.field
3) code.fieldType
4) code
```

여기서 code = “검증 코드(validation code)”.

예: required, range, typeMismatch…

### 즉, code 자리에 들어가는 것은 “검증 논리 이름”이지 “필드 이름”이 아닙니다.

---

# ✔ 왜 itemName으로 확장 코드를 만들지 않는가?

왜냐하면 "itemName" 자체는 **검증 코드가 아니기 때문**입니다.

`DefaultMessageCodesResolver`는 **항상 검증 코드(code)** 를 기준으로 확장 코드를 생성합니다.

예를 들어:

```java
bindingResult.rejectValue("itemName", "required");
```

여기서 "required"가 code이며, 이것을 기반으로:

* required.item.itemName
* required.itemName
* required.java.lang.String
* required

이렇게 만들어냅니다.

반면에:

```java
bindingResult.rejectValue("itemName", "itemName");
```

처럼 code에 필드 이름을 넣는 것은 스프링의 설계 취지에 맞지 않습니다.
필드명은 code 역할을 하지 않기 때문에:

* itemName.item.itemName
* itemName.itemName
* itemName.java.lang.String
* itemName

이런 확장 코드를 생성하지 않습니다.

---

# ✔ 이유: “code는 검증 규칙 이름”이고 “field는 검증 대상의 속성 이름”이기 때문

Spring은 두 개를 **완전히 다른 역할**로 처리합니다.

| 역할    | 의미                  | 예                                       |
| ----- | ------------------- | --------------------------------------- |
| code  | 검증 규칙 이름            | required, range, max, min, typeMismatch |
| field | 검증 대상으로 사용된 객체의 속성명 | itemName, price, quantity               |

스프링이 메시지 코드를 생성할 때 기준이 되는 것은 **code(검증 규칙)** 이며,
**field는 그 code에 추가되는 부가 정보**일 뿐입니다.

---

# ✔ 그렇다면 왜 “필드 기반 메시지 코드”는 지원하지 않을까?

그 이유는 다음과 같습니다.

1. 스프링의 검증 메시지 구조는 **검증 규칙 중심(code 중심)** 으로 설계됨
2. 필드명은 검증 규칙이 아님
3. 필드명은 “어떤 값이 잘못되었는지” 표현하기 위한 데이터일 뿐
4. 필드명 자체에서 메시지 코드를 확장하면 혼란만 증가함

---

# ✔ 예시로 비교하면 더 명확

검증 코드가 `"required"` 일 때:

```
required.item.itemName
required.itemName
required.java.lang.String
required
```

이것은 **검증 규칙(required)** 을 기준으로 메시지를 확장한 형태.

반면 "itemName"은 필드명일 뿐, 검증 규칙이 아니므로:

```
itemName.item.itemName  (X)
itemName.itemName        (X)
itemName.java.lang.String (X)
itemName                  (X)
```

이런 메시지를 만드는 것은 스프링의 설계 의도와 충돌합니다.

---

# ✔ 한 문장으로 정리

### **code(required, range 등)만이 메시지 확장 규칙의 출발점이고,

### field(itemName)는 확장을 위한 기준이 아니기 때문이다.**

---

# ✔ 추가로 더 이해를 돕는 비유

* code = “틀린 이유”
* field = “틀린 값이 속한 위치”

메시지는 항상 “왜 틀렸는지”를 중심으로 결정되므로
fieldName 자체로 메시지를 만드는 개념이 없습니다.

---

필요하시면
FieldError / ObjectError 의 메시지 생성 우선순위를
표 형식 또는 그림 형태로 정리해서 간단히 암기할 수 있게 만들어 드릴까요?

정확히 짚으셨습니다.
바로 이 부분이 헷갈리기 쉬운 지점이므로 아주 명확하게 정리해드릴게요.

---

# ✔ 결론부터 말하면

```
mcr.resolveMessageCodes("required", "item");
```

여기서 **required만 “코드(code)”로 취급되고**,
**item은 “코드가 아니라 객체 이름(objectName)”이라서 메시지 생성의 기준이 되지 않습니다.**

즉,

* required → 메시지 코드 생성의 출발점
* item → 메시지 코드를 “확장하는 데 사용되는 부가 정보”

따라서 **두 개 모두를 코드로 사용하지 않습니다.**

---

# ✔ 왜 required는 확장되지만 item은 확장되지 않는가?

MessageCodesResolver 의 정의에 따르면:

`resolveMessageCodes(code, objectName)` 에서:

* **code = 검증 규칙 이름**
* **objectName = @ModelAttribute의 이름(객체 이름)**

즉, 여기서 “코드(code)”는 **반드시 검증 규칙(validation code)** 이어야만
메시지를 확장할 때 사용됩니다.

예:
required, range, max, min, typeMismatch …

반면,

* item은 필드가 아니라 "객체 이름(modelAttribute)"
* 검증 규칙이 아님

따라서 확장의 기준이 되지 않습니다.

---

# ✔ 실제 생성 규칙을 보면 더 명확

`resolveMessageCodes("required", "item")` 에 대한 스프링의 공식 확장 규칙:

### 1) 가장 구체적인 코드

```
required.item
```

* code + objectName
* 여기서 objectName(item)은 **확장의 대상이 아니라 붙는 정보**

### 2) fallback 코드

```
required
```

---

# ✔ item을 code 로 인식하지 않는 이유

스프링이 메시지 코드를 생성할 때
"code" 라고 인식하는 것은 항상 **검증 규칙(validation rule)** 이기 때문입니다.

즉, messageCodesResolver는 다음 2개를 다르게 취급합니다.

| 인자                  | 의미    | 확장에서 기준으로 사용되나          |
| ------------------- | ----- | ----------------------- |
| 첫 번째 인자(code)       | 검증 코드 | **O (확장 기준)**           |
| 두 번째 인자(objectName) | 객체 이름 | **X (확장 기준 아님, 단순 정보)** |

따라서:

* required → 확장 기준
* item → 메시지 생성의 기준이 아님

---

# ✔ 왜 두 번째 인자를 “코드”로 쓰지 않을까?

아주 중요한 이유가 있습니다.

**두 번째 인자는 “이 오류가 어떤 객체의 것인지” 나타내는 식별자이지,
검증 규칙이 아니기 때문입니다.**

객체 이름이 “코드”라고 오해하면
메시지 구조가 완전히 이상해집니다.

예를 들어 검증 규칙이 required인데,
만약 objectName(item)을 포함해 확장을 한다면:

```
item.item.itemName?
```

같은 말도 안 되는 코드가 만들어집니다.

그래서 objectName은 확장 기준에서 제외됩니다.

---

# ✔ 그러면 왜 출력 배열에는 두 개 다 들어가는 것처럼 보일까?

출력된 배열:

```
[required.item, required]
```

여기서 “required.item”은
“required가 확장될 때 objectName이 붙는 것”일 뿐,
item이 “코드”로서 확장을 만든 것이 아닙니다.

즉, item은 확장 대상이 아니라 **확장의 재료**입니다.

---

# ✔ 비유로 쉽게 이해하기

* **code(required)** = “틀린 이유”
* **objectName(item)** = “어디에서 틀렸는지”

스프링은 “왜 틀렸는가(required)”를 중심으로 메시지를 생성합니다.
필요하면 “어디에서(item)”를 붙여 구체화할 뿐입니다.

---

# ✔ 최종 정리 한 줄

**resolveMessageCodes("required", "item") 는 “required(검증 규칙)”을 기준으로 확장 코드를 만들고, "item"은 단지 구체화하기 위해 붙는 정보일 뿐 ‘코드의 출발점’이 아니다.**

---

필요하시면:

* field 오류일 때 어떻게 4단계 코드가 만들어지는지
* object 오류일 때 어떻게 2단계 코드가 만들어지는지
* 메시지 fallback 흐름도

한 번에 보는 설명도 만들어드릴게요.

아주 좋은 질문입니다.
스프링이 **“단계별 메시지 코드(fallback code)”를 자동으로 여러 개 생성하는 이유**를 정확히 이해하면,
왜 이렇게 복잡해 보이는 구조를 가져가는지 분명하게 보입니다.

아래 설명은 **스프링 MVC 검증 설계 의도**를 기반으로 한 내용입니다.

---

# ✔ 답변 요약

* 필드마다, 검증 규칙마다, 타입마다 “정도에 따라 다르게 메시지를 세밀하게 정의”할 수 있도록 하기 위한 구조
* 즉, **세밀하게 정의하고 싶으면 정의하고, 아니면 안 해도 되는 선택권**을 주기 위함
* 실무에서 **모든 단계 메시지를 다 쓰는 경우는 드물다**
* 큰 프로젝트에서는 이 구조가 “복잡해 보이지만 오히려 유지보수에 도움”이 된다
* 작은 프로젝트에서는 사실 한 두 개만 정의해도 충분하다

---

# ✔ 단계 코드가 왜 필요할까? (스프링의 설계 철학)

스프링은 다음 문제를 해결하려고 했습니다.

### “검증 오류 메시지를 얼마나 세밀하게 제어할 수 있어야 하는가?”

필드, 객체, 검증 규칙은 다양합니다.

예:

* itemName은 공백일 때 메시지가 다르게 필요할 수도 있고
* price는 범위 오류, 타입 오류, required 오류가 각각 다르고
* quantity는 숫자 범위 오류일 때 다르게 보여줘야 하고…

이때 **세밀한 메시지 제어**가 필요하면:

```
required.item.itemName
```

이렇게 구체적인 메시지를 설정할 수 있어야 하고,

반대로 **전 필드 공통으로 같은 메시지를 쓰고 싶으면**

```
required
```

이 코드만 정의해도 됩니다.

즉,

✔ 구체화 → 선택
✔ 단순화 → 선택
✔ 띄엄띄엄만 정의해도 자동 fallback
✔ 모두 생략해도 defaultMessage로 처리됨

이런 유연성이 바로 단계 코드의 목적입니다.

---

# ✔ 단계별 메시지는 실제로 자주 쓰일까?

## 1단계: code.objectName.field

```
required.item.itemName
```

→ 특정 필드에 특정 규칙 메시지를 주고 싶을 때
→ **자주 사용**됨 (한국 프로젝트에서도 필수)

## 2단계: code.field

```
required.itemName
```

→ 특정 필드에 모든 required 규칙에 공통 메시지를 줄 때
→ **경우에 따라 사용**

## 3단계: code.fieldType

```
required.java.lang.String
```

→ 특정 타입(String)에 대해 required 규칙 공통 메시지를 주고 싶을 때
→ **거의 사용 안 함**

## 4단계: code

```
required
```

→ 모든 required 규칙에 대해 공통 메시지를 쓰고 싶을 때
→ **자주 사용**됨

즉,

* 1단계는 자주 사용
* 2단계는 가끔
* 3단계는 거의 없음
* 4단계는 매우 자주 사용

---

# ✔ 단계 코드를 사용하는 이점

### 1) 작은 프로젝트

그냥 가장 단순한 코드만 쓰면 됨:

```
required=값을 입력하세요
range=범위를 벗어났습니다
typeMismatch=잘못된 형식입니다
```

이러면 스프링이 자동으로 fallback을 해서
모든 필드에 적당히 잘 나옵니다.

### 2) 중간 규모 프로젝트

필드마다 다른 메시지가 필요할 때:

```
required.item.itemName=상품명을 입력해주세요
required.item.price=가격을 입력해주세요
```

### 3) 대규모 프로젝트

타입별, 폼별, 필드별로 정교하게 분리하고 싶을 때 이 구조가 매우 유용합니다.

예:
다른 화면에서 같은 Item 객체를 쓸 때
서로 다른 메시지를 적용할 수 있음.

---

# ✔ “결국은 복잡해지는 것 같은데…”

이 느낌이 드는 이유는

* 스프링의 메시지 구조가 “확장 가능한 규칙”을 갖고 있어서
* 작은 프로젝트에서 보면 다소 과하게 느껴지기 때문입니다.

하지만 실제로 **모든 단계 코드를 다 작성할 일은 거의 없습니다.**

당신은 필요한 것만 쓰면 됩니다.

보통 다음만 정의해도 충분합니다:

### ✔ 필드별 규칙 메시지

```
required.item.itemName
range.item.price
range.item.quantity
```

### ✔ 공통 규칙 메시지

```
required
range
typeMismatch
```

---

# ✔ 최종 정리

### 단계 코드는 “대규모 확장성과 유연성을 위해” 만들어졌다.

### 하지만 실제로는 1~2단계만 주로 쓰고, 3단계는 거의 안 쓴다.

### 스프링은 복잡한 상황을 다룰 수 있게 설계된 것이지,

### 개발자가 모두 써야 한다고 강요하는 구조가 아니다.

그래서 **필요한 만큼만 쓰면 된다**는 것이 핵심입니다.

---

원한다면
당신의 프로젝트를 기준으로 “최소 메시지 코드셋”과
“확장 가능한 메시지 구조”를 최적의 형태로 설계해드릴게요.
