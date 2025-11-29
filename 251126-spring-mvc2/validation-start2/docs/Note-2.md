**정확한 상황 정리**

* **등록(add)** → id는 자동 생성됨 → **id가 null이어도 됨**
* **수정(edit)** → id가 반드시 존재해야 함 → **id가 null이면 안 됨**

즉,

* 같은 Item 객체라도 **“등록할 때”와 “수정할 때” 검증 규칙이 달라야 한다**
* Bean Validation의 필드 어노테이션(@NotNull 등)을 그대로 쓰면
  “등록할 때도 id를 입력하라”고 강요하는 문제가 생김

이 문제는 **Bean Validation의 “그룹(Group Validation)” 기능**으로 완벽하게 해결됩니다.

아래에서 실제 코드 기반으로 가장 깔끔한 해결책을 설명할게요.

---

# 해결 방법: Bean Validation의 “검증 그룹(Validation Groups)” 사용

## 1) 검증 그룹 인터페이스 두 개 만들기

```java
public interface SaveCheck {}      // 등록용 검증 그룹
public interface UpdateCheck {}    // 수정용 검증 그룹
```

---

## 2) Item에 그룹별 제약 조건을 지정

```java
public class Item {

    // 등록 시에는 필요 없음
    // 수정 시에는 반드시 필요함
    @NotNull(groups = UpdateCheck.class)
    private Long id;

    @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
    private String itemName;

    @Min(value = 1000, groups = {SaveCheck.class, UpdateCheck.class})
    @Max(value = 2000, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer price;

    @Min(value = 1, groups = {SaveCheck.class, UpdateCheck.class})
    @Max(value = 10, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer quantity;
}
```

설명:

* id는 **UpdateCheck**에서만 @NotNull
* 나머지 필드는 **SaveCheck + UpdateCheck 둘 다 적용**

---

## 3) 컨트롤러에서 그룹을 선택하여 검증

### 등록(add) 시:

```java
@PostMapping("/add")
public String addItem(
        @Validated(SaveCheck.class) @ModelAttribute("item") Item item,
        BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "validation/v2/addForm";
    }
    ...
}
```

### 수정(edit) 시:

```java
@PostMapping("/edit")
public String editItem(
        @Validated(UpdateCheck.class) @ModelAttribute("item") Item item,
        BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "validation/v2/editForm";
    }
    ...
}
```

이렇게 하면:

### add(등록) 시

* id가 null이어도 에러 없음
* itemName, price, quantity 만 검증됨

### edit(수정) 시

* id가 null이라면 반드시 오류 발생
* 나머지 필드도 검증

즉, 딱 원하는 구조가 됨.

---

# 이렇게 하면 좋은 점

### 1) Bean Validation 어노테이션을 필드에 그대로 유지할 수 있고

### 2) add / edit 상황에 따라 검증 규칙을 자동으로 다르게 적용할 수 있으며

### 3) Validator를 따로 만들 필요 없이 Bean Validation만으로 해결됨

### 4) 스프링 컨트롤러는 @Validated(그룹) 만으로 제어 가능

즉, 매우 깔끔하고 유지보수성이 높습니다.

---

# Validator를 직접 사용하는 방식에서도 동일하게 가능하지만

Bean Validation의 "그룹 검증"이 표준적이고 더 깔끔합니다.

스프링 Validator로 하려면:

* add에서는 id 검증 안 하고
* edit에서는 id 검증하게 조건문을 넣어야 함

```java
if (mode == UPDATE && item.getId() == null) {
    errors.rejectValue("id", "required");
}
```

이렇게 “상황(mode)”을 전달받아야 하는데
코드가 복잡해지고 유지보수가 어려움.

그래서 **그룹 검증이 정석**입니다.

---

# 결론

### “등록 때는 id 없어도 되고, 수정 때는 id가 반드시 있어야 한다”

이런 요구사항은 **Bean Validation의 그룹 기능으로 해결하는 것이 가장 깔끔하다.**

필요한 구성은 딱 세 개:

1. SaveCheck / UpdateCheck 인터페이스
2. 각 필드에 groups 속성 설정
3. 컨트롤러에서 @Validated(SaveCheck.class) 또는 @Validated(UpdateCheck.class)

---

원하면
현재 Item 클래스를 기반으로

* 완전한 Bean Validation 그룹 코드
* addForm / editForm 템플릿 예시
* 컨트롤러 코드
  전체를 하나로 묶어 완성된 구조를 만들어드릴게요.

폼 전송 객체(Form DTO)를 다르게 쓰는 방법은 **등록(add)** 과 **수정(edit)** 이 검증 규칙뿐 아니라
입력 UI, 요구 정보, 비즈니스 로직이 달라질 때 흔히 사용하는 정석적인 방법입니다.

이 방식은 다음과 같은 상황에서 매우 유리합니다.

* 등록 시 입력해야 하는 필드와 수정 시 필요한 필드가 다르다
* 등록 시 id는 필요 없고 수정 시 id는 반드시 필요
* 등록 시에는 price가 필수지만 수정에서는 선택
* UI도 add/edit에서 다르게 구성됨
* Bean Validation 그룹이 복잡해질 때 회피하기 좋음

아래에서 **정석적인 DTO 분리 방식**을 실제 코드로 보여드릴게요.

---

# 1. 핵심 개념

### 도메인 객체(Item)를 그대로 폼에 사용하지 않고

### 등록용 DTO(AddItemForm)

### 수정용 DTO(UpdateItemForm)

을 완전히 별개로 만든다.

즉,

* **폼 입력 → DTO**
* **DTO → 도메인(Entity) 변환 → 저장**

이 패턴을 사용하면 검증이 매우 깔끔합니다.

---

# 2. 등록 DTO 예시 (AddItemForm)

```java
public class AddItemForm {

    @NotBlank
    private String itemName;

    @Min(1000)
    @Max(2000)
    private Integer price;

    @Min(1)
    @Max(10)
    private Integer quantity;

    // 등록 시 id는 필요 없음
}
```

### 등록은 id가 필요 없으므로 id 필드를 아예 제거해도 됨.

---

# 3. 수정 DTO 예시 (UpdateItemForm)

```java
public class UpdateItemForm {

    @NotNull   // 수정 시 id는 반드시 있어야 함
    private Long id;

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

### 수정 시에는 id가 반드시 필요하므로 @NotNull 부여.

---

# 4. 컨트롤러에서 DTO를 사용

## 4-1. 등록(add)

```java
@PostMapping("/add")
public String addItem(
        @Validated @ModelAttribute("item") AddItemForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
) {
    if (bindingResult.hasErrors()) {
        return "validation/v3/addForm";
    }

    // DTO → Entity 변환
    Item item = new Item();
    item.setItemName(form.getItemName());
    item.setPrice(form.getPrice());
    item.setQuantity(form.getQuantity());

    Item savedItem = itemRepository.save(item);

    redirectAttributes.addAttribute("itemId", savedItem.getId());
    redirectAttributes.addAttribute("status", true);
    return "redirect:/validation/v3/items/{itemId}";
}
```

---

## 4-2. 수정(edit)

```java
@PostMapping("/edit")
public String editItem(
        @Validated @ModelAttribute("item") UpdateItemForm form,
        BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "validation/v3/editForm";
    }

    Item item = itemRepository.findById(form.getId());
    item.setItemName(form.getItemName());
    item.setPrice(form.getPrice());
    item.setQuantity(form.getQuantity());

    return "redirect:/validation/v3/items/" + form.getId();
}
```

---

# 5. DTO를 분리하는 장점

### 1) 검증 로직이 매우 깔끔함

* id 필드를 groups로 관리할 필요 없음
* 등록/수정의 validation이 충돌하지 않음
* 어떤 필드가 등록용인지 수정용인지 한눈에 보임

### 2) UI도 자연스럽게 분리됨

등록 페이지는 AddItemForm 기반
수정 페이지는 UpdateItemForm 기반

### 3) 유지보수가 쉬움

등록과 수정의 요구사항이 달라질 때 검증 충돌이 없음.

### 4) 도메인 객체(Entity)를 외부 요청으로부터 보호

폼 전송 객체를 따로 쓰면

* JSON 변조 공격
* 도메인 필드 노출
  이런 위험도 차단됨.

### 5) Bean Validation 그룹 없이도 해결 가능

그룹을 쓰면 구조가 복잡해지지만
Form DTO 방식을 쓰면 간단하게 분리됨.

---

# 6. 그럼 “Form 객체 분리”와 “그룹 검증”은 어떤 기준으로 선택해야 하나?

| 방식         | 장점                     | 단점          | 적합한 경우                 |
| ---------- | ---------------------- | ----------- | ---------------------- |
| 그룹 검증      | 하나의 객체로 add/edit 모두 처리 | 그룹이 많아지면 복잡 | 같은 속성을 add/edit 둘 다 사용 |
| Form 객체 분리 | add/edit 완전 분리, 깔끔     | 변환 코드 필요    | add/edit 요구사항이 달라지는 경우 |

**당신의 상황(id는 add에서 필요 없음 / edit에서는 반드시 필요함)** 은
필드 요구사항이 완전히 달라지는 케이스이므로

### Form 객체 분리가 더 자연스럽고 실무에서 권장되는 방식입니다.

---

# 결론

### “등록과 수정의 검증 규칙이 다르다면 Form 객체를 분리하는 것이 가장 깔끔한 해결법이다.”

필요하면:

* AddItemForm / UpdateItemForm의 전체 코드
* addForm.html / editForm.html 템플릿 구조
* 컨트롤러 전체 코드
* DTO → Entity 변환 로직까지
  모두 하나의 예제로 묶어서 드릴 수도 있습니다.

아주 중요한 내용을 잘 가져오셨습니다.
이 내용은 **스프링 MVC에서 @ModelAttribute와 @RequestBody가 “바인딩(데이터 변환)”을 어떻게 처리하는가**에 대한 차이입니다.

아래 설명에서는 **단계별 흐름**으로 완전하게 이해할 수 있게 정리합니다.

---

# 핵심 요약 (먼저 직관적으로)

| 구분            | @ModelAttribute            | @RequestBody                 |
| ------------- | -------------------------- | ---------------------------- |
| 데이터 소스        | 요청 파라미터(form-data, query)  | JSON 요청 본문                   |
| 바인딩 방식        | 필드 단위로 바인딩                 | 객체 전체 단위로 바인딩                |
| 부분 실패         | 허용됨 (필드 하나 변환 실패해도 나머지 성공) | 허용 안 됨 (필드 하나 변환 실패하면 전체 실패) |
| 검증(Validator) | 바인딩 후 항상 가능                | JSON 변환 성공해야 가능              |
| 실패 시 컨트롤러 실행  | 실행됨                        | 실행되지 않고 예외 발생                |

즉,

### @ModelAttribute = 필드 단위로 유연한 바인딩 → 부분 실패 OK → Validator도 적용 가능

### @RequestBody = JSON 전체를 객체로 변환해야 함 → 부분 실패 불가 → Validator 적용 전에 변환 실패하면 예외

아래에 더 자세히 설명해드릴게요.

---

# 1. @ModelAttribute는 “필드 단위(프로퍼티 단위)”로 바인딩한다

예:

```
POST /item/add?itemName=hello&price=aaa&quantity=10
```

price=aaa 는 숫자가 아니므로 Integer로 변환 실패합니다.

### 그런데 @ModelAttribute는?

```
item.itemName = "hello"   OK
item.price = 변환 실패    (FieldError 발생)
item.quantity = 10        OK
```

즉,

* 필드 하나 실패해도 객체(item)는 정상 생성됨
* 실패한 필드는 BindingResult에 오류로 들어감
* 그래도 컨트롤러는 정상 호출됨
* Validator도 추가로 적용 가능

### 이게 스프링 MVC에서 “세밀한 필드 단위 바인딩”이라고 부르는 개념입니다.

---

# 2. @RequestBody는 “객체 전체 단위(JSON 전체)”로 바인딩한다

예: JSON 요청

```json
{
  "itemName": "hello",
  "price": "aaa",
  "quantity": 10
}
```

price가 숫자가 아니므로 JSON 파싱 실패.

### @RequestBody의 동작은?

스프링은 먼저 HttpMessageConverter를 통해 JSON → 객체로 변환해야 합니다.

하지만 price 변환 실패 = JSON 전체가 객체로 변환되지 못함

### 그래서 컨트롤러 호출 자체가 안 됨.

다음과 같은 예외 발생:

```
HttpMessageNotReadableException
HttpMessageConversionException
```

→ BindingResult도 사용할 수 없음
→ Validator도 호출되지 않음
→ 컨트롤러가 실행되지 않음

### 즉, @RequestBody는 “부분 바인딩 실패”가 불가능하다.

---

# 3. 왜 이런 차이가 나는가?

### @ModelAttribute 동작 흐름

1. 스프링이 빈 객체 생성 (new Item())
2. HTTP 파라미터를 필드 단위로 바인딩
3. 실패한 필드는 FieldError 추가
4. BindingResult에 담음
5. 컨트롤러 실행
6. @Valid/@Validated 적용 가능

→ 매우 유연함
→ Form 기반 요청에 적합

---

### @RequestBody 동작 흐름

1. HttpMessageConverter가 JSON 전체를 Item 객체로 변환
2. JSON → 객체 변환이 모두 끝나야 컨트롤러 호출
3. 변환 실패하면 예외 던지고 끝
4. 컨트롤러 호출 X
5. BindingResult 적용 X
6. @Valid/@Validated도 적용 X

→ JSON 파싱 실패 = 요청 전체 실패
→ API 기반 요청(@RestController)에 적합

---

# 4. 예제 비교하면 더 명확

### @ModelAttribute 예

```java
@PostMapping("/add")
public String addItem(
    @ModelAttribute Item item,
    BindingResult bindingResult
) {
    ...
}
```

요청:

```
itemName=hello
price=aaa
quantity=10
```

결과:

* itemName 정상
* price 변환 실패 → FieldError
* quantity 정상
* 컨트롤러 정상 진입
* bindingResult.hasErrors() = true
* 폼 화면 다시 보여줌 가능

---

### @RequestBody 예

```java
@PostMapping("/add")
public String addItem(
    @RequestBody Item item
) {
    ...
}
```

요청(JSON):

```json
{
  "itemName": "hello",
  "price": "aaa",
  "quantity": 10
}
```

결과:

* price 변환 실패 → JSON 파싱 자체 실패
* 컨트롤러 호출 안 됨
* Validator 적용 안 됨
* 예외 발생 → 예외 처리 핸들러로 이동

즉,

### @ModelAttribute는 “필드 단위”

### @RequestBody는 “객체 전체 단위”

---

# 5. 그래서 스프링 문서에서 말한 문장의 의미

당신이 적은 문장을 다시 해석하면:

---

### ✓ @ModelAttribute

* HTTP 파라미터를 “각 필드 단위”로 바인딩하므로
* price만 타입 오류가 나도 itemName, quantity는 정상 처리됨
* BindingResult 존재
* Validator도 정상 작동

---

### ✓ @RequestBody

* HttpMessageConverter가 JSON 전체를 객체로 만들어야 한다
* price 변환 실패 → 객체 전체 생성 실패
* 컨트롤러 호출 X
* Validator 적용 X

---

# 6. 실제 실무 기준 정리

### @ModelAttribute

* form-data(form submit)
* query 파라미터
* 세밀한 필드 바인딩 필요
* bindingResult로 오류 상세 처리 가능
* HTML Form을 사용하는 MVC에 가장 적합

### @RequestBody

* JSON API
* 전송 객체 무조건 전체 변환 필요
* 실패 시 전체 요청 실패
* 폼 검증 방식과 다름
* API 작성 시에 가장 적합

---

# 최종 정리

### @ModelAttribute

* 필드 단위 바인딩
* 부분 실패 가능
* BindingResult로 오류 담김
* Validator 적용 가능
* Form 방식에 최적화

### @RequestBody

* JSON 전체 단위 변환
* 부분 실패 불가 (하나라도 실패하면 전체 실패)
* 컨트롤러 호출 자체가 안 됨
* Validator 적용도 안 됨
* API 방식에 최적화

---

원하면
두 방식의 동작 흐름을 그림으로 정리해드릴 수도 있습니다.
