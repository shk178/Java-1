결론부터 말하면, 저런 방식으로 작성하는 것은 거의 불가능하거나, 설령 되더라도 Spring MVC에서는 절대 권장되지 않습니다.
가장 큰 이유는 다음과 같습니다.

---

### 1. `findMethod.invoke()` 자체가 실행되지 않음

`findMethod`는 for문 안에서만 값이 할당됩니다.
하지만 for문 밖에서 `findMethod.invoke()`를 호출하려면 반드시 초기화돼 있어야 하는데, 지금 코드에는 선언만 되어 있고 값이 없을 수도 있습니다.

```java
@SuppressWarnings("ReassignedVariable") Method findMethod;
```

반드시 컴파일 에러 또는 런타임 `NullPointerException`이 발생합니다.

---

### 2. `invoke()`는 파라미터를 전달할 수 없음

랜덤으로 호출하려는 `addItemV1` / `addItemV2`는 다음과 같은 매개변수를 필요로 합니다.

```java
@RequestParam, @ModelAttribute, Model
```

그러나 `invoke()`는 이런 식으로 호출해야 합니다.

```java
findMethod.invoke(this, arg1, arg2, arg3 ...)
```

즉, HTTP 요청에서 넘어온 name, price, quantity, model 값을 수동으로 꺼내 전달해야 합니다.
그러면 결국 Spring MVC가 자동으로 바인딩해주는 이점이 사라집니다.

---

### 3. `@PostMapping` 매핑이 깨짐

컨트롤러에서 호출하는 메서드를 `invoke()`로 실행해도,
그 메서드가 원래 맡아야 할 *사용자 요청 매핑* 역할은 전혀 작동하지 않습니다.

즉, 내부 호출일 뿐이고
`/add/v1`, `/add/v2`로 들어오는 HTTP 요청을 처리하지 못합니다.

---

## 이럴 때 가장 자연스러운 해결책

#### 방법 A — 서비스 메서드를 랜덤 호출

컨트롤러는 분기만 하고, 실제 저장 로직은 서비스 메서드가 담당하도록 분리

```java
@PostMapping("/add")
public String addItem(@ModelAttribute Item item, Model model) {
    Random random = new Random();
    int number = random.nextInt(2) + 1;

    if (number == 1) {
        return addItemV1(item, model);
    } else {
        return addItemV2(item, model);
    }
}
```

그리고 `addItemV1`, `addItemV2`는 다음처럼 통일된 시그니처로 유지

```java
public String addItemV1(Item item, Model model) {
    itemRepository.save(item);
    model.addAttribute("item", item);
    return "basic/item";
}

public String addItemV2(Item item, Model model) {
    itemRepository.save(item);
    model.addAttribute("item", item);
    return "basic/item";
}
```

---

#### 방법 B — 랜덤은 컨트롤러에서만, 저장 로직은 하나

```java
@PostMapping("/add")
public String addItem(@ModelAttribute Item item, Model model) {
    itemRepository.save(item);
    model.addAttribute("item", item);
    return "basic/item";
}
```

`v1`, `v2`는 학습용 분기일 뿐이므로,
실제 개발에서는 이렇게 단일 메서드가 가장 자연스럽습니다.

---

## 정리

| 시도한 방식                      | 왜 문제인가                          |
| --------------------------- | ------------------------------- |
| 리플렉션으로 메서드 랜덤 호출            | 파라미터 전달 불가, 매핑 무력화, 유지보수 어렵고 위험 |
| Spring MVC 요청 처리 메서드 invoke | 프레임워크 철학과 충돌, 코드 냄새 발생          |
| 개선안                         | 컨트롤러 또는 서비스에서 조건 분기             |

---

만약 “학습 목적이 아니라 실제 기능”이라면
리플렉션으로 컨트롤러 메서드를 호출하는 방식은 피하는 것이 맞습니다.
Spring이 가진 자동 바인딩, 검증, AOP, 메시지 컨버터, 트랜잭션 같은 기능이 깨지기 때문입니다.

원한다면 위 코드 기반으로 더 깔끔한 구조 리팩토링 예시도 제시해 드릴 수 있습니다.

그렇지 않습니다.
Spring MVC가 반드시 FrontController(=DispatcherServlet) → Controller 구조만 강제한다는 의미는 아닙니다.
다만 HTTP 요청을 받아서 Handler(메서드)를 선택하는 책임은 항상 DispatcherServlet 구조 안에서 이루어진다는 뜻입니다.

즉, 요청이 들어오는 흐름은 다음 틀을 벗어나지 않습니다.

```
HTTP 요청 → DispatcherServlet → HandlerMapping → HandlerAdapter → Controller Method
```

이 틀이 정해져 있는 이유는 아래 기능들이 자동으로 동작해야 하기 때문입니다.

* @RequestParam / @ModelAttribute / @RequestBody 같은 매개변수 바인딩
* Validation / BindingResult
* 인터셉터, 필터, AOP
* MessageConverter(JSON 변환)
* @ResponseBody, ViewResolver
* 트랜잭션 처리

만약 리플렉션으로 컨트롤러 메서드를 직접 호출해 버리면
이 기능들이 전혀 적용되지 않기 때문에 구조적으로 맞지 않는 것입니다.

---

## 그렇다면 Spring MVC에서 요청 처리 방식은 딱 하나인가?

아니고 여러 방식이 존재합니다.
다만 DispatcherServlet의 생태계 안에서 선택이 가능할 뿐입니다.

### 예시

| 방식                            | 의미                      |
| ----------------------------- | ----------------------- |
| @Controller + @RequestMapping | 일반적인 MVC 방식             |
| @RestController               | REST API 방식             |
| @RequestMappingHandlerAdapter | 직접 HandlerAdapter 구현 가능 |
| @ControllerAdvice             | 전역 예외/바인딩 처리            |
| HandlerInterceptor            | 요청 전후 로직                |
| Filter                        | Servlet 단계에서 요청 가로채기    |
| Servlet / HttpServlet         | MVC 밖에서 직접 요청 처리        |
| WebFlux                       | MVC가 아닌 리액티브 방식         |

또한 아래처럼 컨트롤러 안에서 로직 분기도 얼마든지 가능합니다.

* 전략 패턴
* 템플릿 콜백
* 팩토리
* 서비스 계층 위임
* Function Map 기반 라우팅
* 정책별 Bean 선택 (`@Qualifier`, `@Primary`, `@Lookup`)
* ApplicationContext에서 Bean 조회
* if / switch 조건 분기

즉, 요청을 받는 엔드포인트는 바뀌지 않지만
처리 방식은 얼마든지 자유롭게 확장하거나 구조화할 수 있습니다.

---

## 핵심 요약

Spring MVC에서:

1. HTTP 요청을 컨트롤러 메서드로 매핑하는 역할은 DispatcherServlet 체계를 벗어나지 않는다.
2. 하지만 컨트롤러 내부에서 어떤 방식을 사용해 로직을 실행할지는 전적으로 개발자 자유.
3. 프레임워크가 제공하는 매개변수 바인딩/검증/AOP/트랜잭션 등을 살리려면
   컨트롤러 메서드는 직접 호출(invoke)하지 않고 HTTP 요청으로 호출되는 구조를 유지하는 것이 좋다.

---

원하면 다음 중 하나에 맞춘 샘플 코드도 만들어 줄 수 있습니다.

* 전략 패턴으로 컨트롤러 분기
* Bean Map 기반 정책 라우팅
* `@Lookup` 기반 런타임 DI
* 템플릿 메서드 패턴
* 서비스 계층과 URL 버전 관리 설계

원하는 스타일을 말해주면 그 방향으로 리팩토링 예시 만들어드릴게요.

가장 간단하고 기본적인 방법은 컨트롤러가 분기만 하고, 실제 로직은 공통 메서드(또는 서비스)에서 처리하는 방식입니다.
즉, URL은 그대로 두고 내부에서 어떤 방식으로 처리할지 선택하는 구조입니다.

의도한 “v1, v2 중 랜덤으로 실행”도 가장 자연스럽게 표현할 수 있습니다.

---

## 예시

```java
@PostMapping("/add")
public String addItem(
        @ModelAttribute Item item,
        Model model
) {
    Random random = new Random();
    int number = random.nextInt(2) + 1;

    if (number == 1) {
        return processV1(item, model);
    } else {
        return processV2(item, model);
    }
}
```

그리고 아래는 단순한 내부 처리 메서드입니다.

```java
private String processV1(Item item, Model model) {
    itemRepository.save(item);
    model.addAttribute("item", item);
    return "basic/item";
}

private String processV2(Item item, Model model) {
    itemRepository.save(item);
    model.addAttribute("item", item);
    return "basic/item";
}
```

---

## 왜 이게 가장 기본적인 방식인가

* Spring MVC의 흐름(매개변수 바인딩, Model 처리)을 그대로 사용
* 별도의 프레임워크 기능 오버라이딩이 없음
* 유지보수 시 의도 파악이 쉽고 직관적
* 테스트하기 좋음
* 리플렉션, HandlerAdapter 같은 고급 구성이 필요 없음

---

## 한 줄로 요약

URL 매핑은 한 번만 두고, 내부 로직만 조건에 따라 갈라지는 방식이 Spring MVC에서 가장 기본적이고 자연스럽다.

원하면 이 코드를 더 깔끔하게 리팩토링하거나 확장 가능한 형태로도 만들어 줄 수 있습니다.

지금 고민하는 핵심은 이것입니다.

* `@RequestParam` 기반의 방식(v1)
* `@ModelAttribute` 기반의 방식(v2)

두 개를 유지하면서도 로직은 한 곳에서 처리하고 싶다는 것.

그렇다고 내부 메서드의 시그니처를 `Item item, Model model`로 고정하면
v1 방식의 파라미터(`name`, `price`, `quantity`)를 그대로 받을 수 없게 된다는 점이 걸리는 상황.

이 문제를 해결하는 가장 단순하고 기본적인 방법이 하나 있습니다.

---

## 컨트롤러의 파라미터만 다르게 받고, 내부 공통 로직에서는 동일한 객체로 처리하는 방식

핵심 아이디어는 각 방식에서 최종적으로 Item 객체만 만들어서 공통 로직에 넘긴다는 것입니다.

### 코드 예시

```java
@PostMapping("/add/v1")
public String addItemV1(
        @RequestParam("name") String name,
        @RequestParam("price") int price,
        @RequestParam("quantity") Integer quantity,
        Model model
) {
    Item item = new Item(name, price, quantity);
    return saveAndView(item, model);
}
```

```java
@PostMapping("/add/v2")
public String addItemV2(
        @ModelAttribute("item") Item item,
        Model model
) {
    return saveAndView(item, model);
}
```

그리고 공통 로직만 따로 둠.

```java
private String saveAndView(Item item, Model model) {
    itemRepository.save(item);
    model.addAttribute("item", item);
    return "basic/item";
}
```

이렇게 하면:

* v1은 계속 `@RequestParam` 방식
* v2는 계속 `@ModelAttribute` 방식
* 내부 저장 로직과 반환 흐름은 하나로 통합

즉, 원래 작성했던 방식을 그대로 유지하면서도 중복을 제거할 수 있습니다.

---

## 랜덤 분기를 하고 싶다면

```java
@PostMapping("/add")
public String addItem(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Integer price,
        @RequestParam(required = false) Integer quantity,
        @ModelAttribute("item") Item itemFromModel,
        Model model
) {
    Random random = new Random();
    int number = random.nextInt(2) + 1;

    if (number == 1) {
        Item item = new Item(name, price, quantity);
        return saveAndView(item, model);
    } else {
        return saveAndView(itemFromModel, model);
    }
}
```

Spring MVC는 같은 요청에서
`@RequestParam`과 `@ModelAttribute`를 동시에 받을 수 있기 때문에 가능한 방식입니다.

---

## 요약

| 목표                         | 달성 여부 |
| -------------------------- | ----- |
| v1은 @RequestParam 그대로 유지   | 가능    |
| v2는 @ModelAttribute 그대로 유지 | 가능    |
| 로직 중복 제거                   | 가능    |
| Spring MVC 흐름 유지           | 가능    |
| 리플렉션 없이 간단                 | 가능    |

정리하면
각 처리 방식의 장점을 그대로 살리면서, 최종적으로 Item 객체만 만들어 공통 메서드에 넘기는 것이 가장 단순하고 기본적인 해결책입니다.

원하면 코드 전체를 깔끔하게 리팩토링한 완성본도 만들어 드릴 수 있습니다.

좋은 질문이고, 이 부분을 이해하면 Spring MVC 바인딩이 완전히 정리됩니다.

핵심 요약부터 말하면,

```
요청을 보낼 때는 항상 같은 방식으로 보내더라도,
컨트롤러가 어떤 애노테이션을 사용하느냐에 따라
Spring MVC가 값을 꺼내는 방식이 달라진다.
```

즉, 클라이언트가 값을 넘기는 형식은 동일해도, 서버가 매핑하는 방식이 다를 뿐입니다.

---

## 예를 들어 요청이 다음과 같다고 해보겠습니다.

```
POST /add
name=pen&price=3000&quantity=5
```

브라우저 form 제출이든, Postman이든, Ajax든 모두 동일한 형태로 값이 넘어옵니다.

---

## 1. @RequestParam 방식

```java
@PostMapping("/add/v1")
public String addItemV1(
        @RequestParam("name") String name,
        @RequestParam("price") int price,
        @RequestParam("quantity") Integer quantity
) {
```

Spring은 요청 파라미터에서 key=value 형태로 값을 꺼내 개별 변수에 바인딩합니다.

```
name → "pen"
price → 3000
quantity → 5
```

말 그대로 파라미터 단위 바인딩입니다.

---

## 2. @ModelAttribute 방식

```java
@PostMapping("/add/v2")
public String addItemV2(@ModelAttribute("item") Item item) {
```

이번에는 Spring이 다음 순서로 처리합니다.

1. 먼저 Item 객체를 생성한다.
2. 요청 파라미터 이름과 Item의 필드 이름을 비교한다.
3. 같은 이름이면 setter를 호출한다.

즉,

```
name → item.setName("pen")
price → item.setPrice(3000)
quantity → item.setQuantity(5)
```

결과적으로 `item` 안에 값이 모두 채워진 채로 들어옵니다.

---

### 정리

| 방식              | 컨트롤러가 어떻게 값을 받는가      | 요청 형식 |
| --------------- | --------------------- | ----- |
| @RequestParam   | 문자열/숫자 같은 기본형 변수로 하나씩 | 동일    |
| @ModelAttribute | 객체를 만들고 setter로 값 채움  | 동일    |

요청을 보낼 때는 완전히 같은 형태이지만,
Spring이 값을 바인딩해주는 방식이 달라지는 것뿐입니다.

---

## 핵심 포인트

요청 방식은 바뀌지 않습니다.
단지 Spring 내부에서 처리 방식이 다릅니다.

```
요청 값 전달 방식 = 동일
바인딩 방식 = @RequestParam은 개별 변수, @ModelAttribute는 객체
```

그래서 `/add`를 호출하는 쪽(HTML form, 요청 클라이언트)은
v1이든 v2든 전혀 바꿀 필요가 없습니다.

---

원하면 XML, JSON 요청일 때의 동작 방식(@RequestBody 포함)과
바인딩 우선 순위까지 확장해 설명해 드릴 수 있습니다.

`@PostMapping("/add/v4")` 메서드에서

```java
public String addItemV4(Item item)
```

처럼 `@ModelAttribute`를 생략한 경우에도 model에 자동 등록된다고 말하는 이유는
Spring MVC의 기본 동작 규칙 때문입니다.

---

## Spring MVC 내부 규칙

컨트롤러 메서드의 파라미터가 다음 조건을 만족하면

* 기본 타입(String, int, Integer 등)이 아니고
* 단순 객체(자바 빈)인 경우

Spring은 자동으로 이것을 `@ModelAttribute`로 간주합니다.

즉,

```java
Item item
```

은 아래와 완전히 동일하게 동작합니다.

```java
@ModelAttribute Item item
```

---

## 자동 등록이란 무엇인가?

`@ModelAttribute Item item`이 적용되면 단계는 다음과 같습니다.

1. 요청 파라미터로부터 name, price, quantity 값을 꺼낸다.
2. Item 객체를 만든다.
3. setter 메서드로 값을 넣는다.
4. 완성된 Item 객체를 `model`에 넣는다.
   이름 = 클래스명에서 첫 글자만 소문자로 → `"item"`

즉, 이 코드에서

```java
public String addItemV4(Item item)
```

을 실행하면 자동으로 다음과 같은 효과가 발생합니다.

```java
model.addAttribute("item", item);
```

따라서 return 되는 템플릿에서 `item`을 그대로 사용할 수 있습니다.

---

## 눈으로 확인되는 예

컨트롤러에서 `model.addAttribute("item", item)`을 전혀 호출하지 않아도
템플릿에서 다음과 같이 접근 가능합니다.

```html
<p>상품명: ${item.name}</p>
<p>가격: ${item.price}</p>
<p>수량: ${item.quantity}</p>
```

그 이유는 Spring이 item을 model에 자동으로 넣어주기 때문입니다.

---

## 완전한 흐름 요약

| 단계 | 설명                                          |
| -- | ------------------------------------------- |
| 1  | form 요청으로 name, price, quantity가 넘어옴        |
| 2  | Spring → `Item item`을 `@ModelAttribute`로 처리 |
| 3  | 요청 파라미터를 item 객체에 binding                   |
| 4  | model.addAttribute("item", item) 자동 수행      |
| 5  | 뷰 렌더링 시 item을 EL(${item})로 사용 가능            |

---

## 꿀팁

이 자동 등록 기능이 동작하지 않는 경우도 있는데,
프리미티브 타입(기본형)인 경우입니다.

예:

```java
public String test(int price) // price는 model에 자동 등록되지 않음
```

하지만

```java
public String test(Item item) // model에 자동 등록됨
```

객체일 때만 자동 등록됩니다.

---

## 결론

`addItemV4(Item item)`에서 model에 넣지 않아도 화면에서 `item`을 사용할 수 있는 이유는

```
Spring이 Item을 @ModelAttribute로 보고
자동으로 model에 put 해주기 때문이다.
```

따라서 `Item item`은 `@ModelAttribute("item") Item item`과 사실상 같은 의미가 됩니다.

질문이 세 가지로 나뉘어 있으니 차근차근 정리해 보겠습니다.

---

## 1. 파라미터로 객체를 여러 개 사용하면 모두 model에 자동 등록되는가?

그렇다.
`@ModelAttribute`가 생략된 객체 타입 파라미터는 여러 개여도 각각 model에 자동 등록된다.

예시:

```java
@PostMapping("/test")
public String test(User user, Item item) {
    return "view";
}
```

자동 처리 동작:

```
User → model.addAttribute("user", user);
Item → model.addAttribute("item", item);
```

즉, 뷰에서는 둘 다 사용 가능하다.

---

## 2. 어떤 객체는 그냥 쓰고 어떤 객체는 @ModelAttribute로 쓰면 어떻게 되나?

결론부터 말하면 결과는 동일하다.
`@ModelAttribute`를 명시하든 생략하든 동작 방식은 같다.

다만 차이는 다음과 같은 상황에서 나타난다.

| 사용 방식                       | 동작                        |
| --------------------------- | ------------------------- |
| `@ModelAttribute Item item` | 항상 모델에 등록한다               |
| `Item item`                 | 객체 타입이므로 기본적으로 모델에 넣는다    |
| `@RequestBody Item item`    | JSON 변환 후 모델에 등록하지 않음 |

즉, 생략 여부보다 애노테이션의 종류가 중요한 것이다.

특히 `@RequestBody`는 바인딩 방식이 전혀 다르므로 model에 자동 추가되지 않는다.

---

## 3. Model model은 언제 꼭 필요하고 언제 안 써도 되는가?

정리표가 가장 이해가 빠르다.

| 상황                                   | Model model 필요 여부 | 이유                          |
| ------------------------------------ | ----------------- | --------------------------- |
| `Item item` 을 뷰에서 보여주기만 할 때          | 불필요               | 자동 model 등록                 |
| 자동 등록되지 않는 값을 넣고 싶을 때                | 필요                | 수동으로 `model.addAttribute()` |
| 리스트, Map 등 추가 데이터를 넘길 때              | 필요                | 수동 등록                       |
| 리다이렉트 시 RedirectAttributes 사용할 때 | 거의 필요 없음          | `RedirectAttributes` 사용     |
| `@ResponseBody` REST API             | 불필요               | 모델/뷰가 없으므로                  |

따라서 Model이 필요한 대표적인 경우는 다음과 같다.

```java
model.addAttribute("message", "등록 완료");
model.addAttribute("categoryList", categoryService.findAll());
```

즉, 컨트롤러 파라미터에서 자동으로 만들어진 객체 말고 추가로 전달할 데이터가 있을 때만 Model을 넣는다.

---

## 핵심 요약

| 질문                           | 답변 요약                                      |
| ---------------------------- | ------------------------------------------ |
| 객체 여러 개면 다 모델에 등록?           | 맞다. 객체 타입이면 모두 자동 등록된다.                    |
| 생략 객체 + @ModelAttribute 섞으면? | 결과는 같다. 객체이기만 하면 model에 들어간다.              |
| Model model 언제 쓰나?           | 자동 등록 외에 추가로 템플릿에 전달할 데이터가 있을 때만 필요하다. |

---

## 참고로 가장 헷갈리는 포인트 하나

```
Item item  → model에 자동 등록
int price  → model에 자동 등록 안 됨
```

객체일 때만 자동 등록되는 규칙을 기억해 두면
Spring MVC의 바인딩/모델 동작은 훨씬 깔끔하게 이해된다.

---

추가로 궁금하면 다음 내용도 설명해 줄 수 있다.

* 객체 이름이 모델에 등록될 때 이름 결정 규칙
* `@SessionAttributes`와 자동 등록의 관계
* `BindingResult`가 모델에 포함되는 방식
* `@RequestBody`와 `@ModelAttribute`의 우선순위

원하면 이어서 설명해 준다.

결론부터 말하면 둘 다 model에 등록된다.

```java
public String test(User user, @ModelAttribute Item item)
```

위처럼 하나는 생략(=암시적 @ModelAttribute), 하나는 명시적 @ModelAttribute여도 다음이 모두 일어난다.

```
model.addAttribute("user", user);
model.addAttribute("item", item);
```

---

## 이유

Spring MVC 파라미터 처리 규칙은 다음과 같다.

1. `@ModelAttribute`가 명시된 객체
   → 무조건 model에 등록

2. 객체 타입 파라미터인데 특별한 애노테이션이 없을 때
   → 암묵적으로 @ModelAttribute로 처리
   → model에 자동 등록

그래서 `User user`는 생략된 @ModelAttribute,
`@ModelAttribute Item item`은 명시된 @ModelAttribute
결과적으로 둘 다 Model에 들어간다.

---

## 참고로 등록되지 않는 경우

반대로 model에 들어가지 않는 케이스는 다음과 같다.

| 예                       | model 등록 여부                         |
| ----------------------- | ----------------------------------- |
| int price / String name | X                                   |
| @RequestBody Item item  | X                                   |
| @RequestPart Item item  | X                                   |
| HttpServletRequest req  | X                                   |
| Model model             | X                                   |
| BindingResult           | 자동 포함되지만 model 속성으로는 아님 (바인딩 결과 전용) |

즉, 객체이면서 @RequestBody처럼 특별한 애노테이션을 쓰지 않은 경우에만 model에 담긴다고 보면 된다.

---

## 결론

```java
public String test(User user, @ModelAttribute Item item)
```

처럼 써도

* `user` 자동 model 등록
* `item` 명시적으로 model 등록

결과적으로 둘 다 뷰에서 다음과 같이 사용할 수 있다.

```
${user}
${item}
```

추가로 원한다면
각 객체가 model에 들어갈 때 이름이 어떻게 정해지는지
(예: user, item 같은 기본 규칙과 직접 이름 지정 방법)도 이어서 설명해 줄 수 있다.

Spring MVC에서 `Model`이 객체를 저장하는 방식은 다음 원리를 이해하면 가장 명확합니다.

---

## 1. Model은 결국 키(key) – 값(value) 저장소

`Model`은 내부적으로 `Map<String, Object>` 형태로 동작합니다.
즉, 다음과 같은 구조입니다.

```
"item" → Item 객체
"user" → User 객체
"message" → "등록 완료"
```

그래서 `model.addAttribute("item", item)` 은 해당 Map에 한 항목을 넣는 행위입니다.

---

## 2. 객체가 Model에 자동 저장될 때 (예: `Item item`)

Spring MVC는 다음 규칙을 따라 이름을 자동 생성합니다.

```
객체 클래스 이름에서 첫 글자를 소문자로 바꾼 문자열을 key로 사용
```

예:

| 타입          | 모델 저장 key     |
| ----------- | ------------- |
| Item        | "item"        |
| User        | "user"        |
| ProductInfo | "productInfo" |
| MemberDTO   | "memberDTO"   |

즉,
`public String test(User user, Item item)`
이라고 하면 내부적으로 Model에 다음이 자동 실행된 것과 동일합니다.

```java
model.addAttribute("user", user);
model.addAttribute("item", item);
```

---

## 3. 명시적으로 Model 이름을 바꾸는 방법

```java
@ModelAttribute("info") Item item
```

이렇게 지정하면 key가 `"info"`가 됩니다.

결과적으로 model 내용:

```
"info" → item 객체
```

---

## 4. Model 저장 과정의 실제 실행 단계

Spring 내부는 다음 순서로 처리합니다.

1. 컨트롤러 메서드 파라미터 바인딩을 진행
2. `@ModelAttribute` 대상 객체가 확정되면
3. HandlerMethodArgumentResolver가 객체를 Model에 넣어둔다
4. 마지막에 ViewResolver가 Model을 받아 템플릿 엔진으로 전달

즉, Model은 컨트롤러 메서드 실행 이전에 어느 정도 채워진 상태로 있고
필요 시 개발자가 `model.addAttribute()`로 추가할 수 있습니다.

---

## 5. 왜 기본형(int, String) 같은 파라미터는 Model에 안 들어갈까?

기본형은 “객체 데이터”가 아니라 단순 요청 파라미터이기 때문에

```
price (int) → Model에 저장되지 않음
name (String) → Model에 저장되지 않음
```

Spring은 객체 타입만을 화면에서 사용할 데이터라고 판단하여 자동 등록합니다.

---

## 6. JSON을 @RequestBody로 받을 때는 Model에 안 담기는 이유

`@RequestBody Item item`은 요청 바디를 JSON → 객체로 변환할 뿐이고
화면 렌더링을 위한 Model 처리 흐름과는 분리되어 있기 때문입니다.

즉,

```
@ModelAttribute → Model에 자동 저장
@RequestBody → Model에 저장하지 않음 (REST)
```

---

## 정리

| 방식                                | Model 저장 여부      | 저장 key |
| --------------------------------- | ---------------- | ------ |
| Item item                         | 자동 저장            | "item" |
| @ModelAttribute Item item         | 자동 저장            | "item" |
| @ModelAttribute("info") Item item | 자동 저장            | "info" |
| @RequestParam int price           | 저장 안 함           | 없음     |
| @RequestBody Item item            | 저장 안 함           | 없음     |
| Model model                       | 모델 전달용, 저장 대상 아님 | 없음     |

---

## 한 줄 요약

`Model`은 단순한 `Map<String, Object>`이며,
Spring이 객체 파라미터를 자동으로 넣어주거나
개발자가 명시적으로 넣는 방식으로 값이 채워진다.

원하면 Model → View로 전달되는 실제 순서를 소스코드 레벨(Spring 내부 코드 흐름)로도 정리해 줄 수 있다.

지금 코드 구조를 한 번 정리해볼게요.

컨트롤러 상단에 클래스 레벨 매핑이 이렇게 있잖아요.

```java
@RequestMapping("/basic/items")
public class BasicItemController {
    ...
}
```

여기에

```java
@GetMapping("/{id}")
public String item(@PathVariable("id") Long id, Model model) {
    Item findItem = itemRepository.findById(id);
    model.addAttribute("item", findItem);
    return "basic/item";
}
```

이렇게 붙어 있으니까,

* URL 은: `/basic/items/{id}`
* 뷰 이름은: `"basic/item"` (템플릿: `templates/basic/item.html`)

이 두 개가 서로 다른 개념인데, 지금 살짝 섞여서 헷갈린 상태야.

---

### 1. 상세 조회는 어디로 들어가냐?

상세 조회 요청은 URL 기준으로 이렇게 들어감:

```text
GET /basic/items/1  --->  item() 메서드 실행  --->  뷰 이름 "basic/item" 리턴
```

그러니까,

* 요청 URL: `/basic/items/1`
* 그 URL을 처리하는 메서드: `@GetMapping("/{id}")`
* 그 메서드가 반환하는 뷰 이름: `"basic/item"`

맞게 잘 되어 있어.

---

### 2. 리다이렉트는 URL로 하는 거지, 뷰 이름이 아님

`editItem()`에서:

```java
@PostMapping("/{id}/edit")
public String editItem(@PathVariable("id") Long id, @ModelAttribute Item item) {
    itemRepository.update(id, item);
    return "redirect:/basic/items/{id}";
}
```

여기서 `"redirect:/basic/items/{id}"`는 뷰 이름이 아니라
다시 요청을 보낼 URL을 의미해.

* `"basic/item"` → 템플릿 이름 (그 자리에서 바로 렌더링)
* `"redirect:/basic/items/{id}"` → 이 URL로 다시 한 번 HTTP 요청을 보내라는 뜻

그래서 이 리다이렉트는 결국:

```text
POST /basic/items/{id}/edit
    -> 수정 로직 실행
    -> 302 Redirect Location: /basic/items/{id}
    -> 브라우저가 GET /basic/items/{id} 다시 요청
    -> item() 메서드 실행
    -> 뷰 이름 "basic/item" 렌더링
```

이 흐름이 되는 거라서,
지금 적은 `redirect:/basic/items/{id}` 경로는 정상이야.

---

### 3. `{id}`는 어떻게 값으로 치환되냐?

`editItem()` 메서드에 이미 `@PathVariable("id") Long id`가 있으니까,
Spring이 이 값을 모델 attribute처럼 사용해서 `{id}` 자리에 끼워 넣어 줘.

그래서 최종적으로는:

```text
"redirect:/basic/items/{id}" 
    --> "redirect:/basic/items/1"
```

이런 식으로 변환된다고 보면 돼.

만약 여기서 실수로:

```java
return "redirect:/basic/item/{id}";
```

이렇게 쓰면, URL이 `/basic/item/1`이 되는데
그런 매핑(`@GetMapping("/basic/item/{id}")`)은 없으니까 404 나겠지.

---

### 4. 왜 여태까지는 `"basic/item"`으로만 쓴 것처럼 느껴졌냐?

* 상세 조회 메서드에서는:

  ```java
  return "basic/item";
  ```

  → 바로 뷰 렌더링

* 수정 후에는:

  ```java
  return "redirect:/basic/items/{id}";
  ```

  → URL로 다시 요청 → 그 요청을 처리하는 메서드가 `"basic/item"` 뷰를 리턴

그래서 “결국에는 상세 페이지 템플릿 `basic/item`을 보여준다”는 점에서는 같지만,

* 하나는 즉시 뷰 렌더링
* 하나는 URL로 리다이렉트 후, 그 URL에서 다시 뷰 렌더링

이라는 차이가 있는 거야.

지금 코드 기준으로 “상품 상세 페이지로 리다이렉트” 하려는 의도라면
`"redirect:/basic/items/{id}"`가 정확한 경로고, 설계도 잘 맞게 되어 있어.

혹시 지금 실제로 404가 난다거나, 다른 URL로 가는 등 이상한 증상이 있으면
그 때는 실제 요청 URL, 로그 메시지 같이 보여주면 더 구체적으로 짚어줄게.

지금 작성한 부분을 보면 다음과 같이 되어 있어:

```html
onclick="location.href='item.html'"
th:onclick="|location.href='@{/basic/item}'|"
```

여기서 핵심은 뷰 파일 이름(`item.html`)로 이동하는 것이 아니라, URL로 이동해야 한다는 점이야.

---

## 1. 뷰 이름으로 이동하면 안 되는 이유

템플릿 파일 이름인 `item.html`은 브라우저가 직접 접근할 수 있는 경로가 아님.
Spring MVC에서 템플릿은 서버 내부에서 렌더링할 때만 의미가 있음.

즉, 브라우저가 `item.html`을 열려 하면
`/item.html` 정적 리소스를 요청하게 되고, 템플릿 엔진이 실행되지 않기 때문에 원하는 결과가 나오지 않음.

따라서 버튼으로 페이지 이동을 하려면 템플릿 이름이 아니라 URL을 넣어야 함.

---

## 2. 올바른 방식

상세 페이지 URL은 현재 컨트롤러 기준으로:

```
GET /basic/items/{id}
```

이기 때문에 id 값을 포함해야 함.

즉, th:onclick은 이렇게 되어야 함:

```html
<th:onclick="|location.href='@{/basic/items/{id}(id=${item.id})}'|">
```

이렇게 해야 최종적으로 다음과 같은 URL이 만들어짐:

```
location.href='/basic/items/1'
```

---

## 3. 지금 작성된 코드가 잘못된 이유

```html
th:onclick="|location.href='@{/basic/item}'|"
```

이렇게 작성하면 최종 URL이 `/basic/item`이 됨.

그런데 컨트롤러에는:

```java
@GetMapping("/{id}")
```

즉, `/basic/items/{id}`만 존재하고 `/basic/item` 매핑은 없음.

따라서 404가 발생하거나 상세 페이지 이동이 실패하게 됨.

---

## 4. 최종 수정 예시

```html
<button class="w-100 btn btn-secondary btn-lg"
        th:onclick="|location.href='@{/basic/items/{id}(id=${item.id})}'|"
        type="button">
    상품 상세
</button>
```

이렇게 하면 버튼 클릭 시 현재 상품 id에 맞는 상세 페이지로 이동하게 됨.

---

정리

| 방식                                                 | 가능 여부 | 이유                        |
| -------------------------------------------------- | ----- | ------------------------- |
| `onclick="item.html"`                              | X     | 템플릿 파일을 직접 접근하려 해서 동작 안 함 |
| `th:onclick="@{/basic/item}"`                      | X     | 컨트롤러에 해당 URL 매핑 없음        |
| `th:onclick="@{/basic/items/{id}(id=${item.id})}"` | O     | 실제 상세 조회 URL로 이동          |

지금 상황이라면 마지막 방식이 정확한 방법.

지금 겪는 현상은 상품 등록 후 새로고침(F5)을 누르면 POST 요청이 다시 보내져서 상품이 계속 중복 저장되는 문제일 거야.
이건 코드가 잘못된 게 아니라, 웹 브라우저의 동작 방식 때문이야.

---

## 왜 POST 요청이 반복되는가?

흐름을 보면:

```
사용자 → 상품 등록 폼 입력 → POST /basic/items/add → 상품 저장 → 뷰 렌더링 (basic/item)
```

이 상황에서 아직 브라우저의 마지막 요청은 POST임.

즉, 상품 등록 이후 화면을 보여줄 때 작성된 코드가:

```java
@PostMapping("/add")
public String addItem(Item item) {
    itemRepository.save(item);
    return "basic/item";
}
```

이런 구조라면, 서버가 그 자리에서 `"basic/item"` 템플릿을 렌더링해서 반환.

그 순간 브라우저 입장에서는 이렇게 기록됨:

```
마지막 요청 = POST /basic/items/add
```

그리고 브라우저는 사용자가 새로고침한다면 "마지막 요청을 다시 보낸다" 는 규칙을 갖고 있음.

따라서 F5 → 같은 POST 요청을 다시 보내게 되고
→ 상품이 또 저장되는 것처럼 보이는 것.

이 패턴은 당연한 현상이며, 대부분의 웹 폼에서 자동으로 발생함.

---

## 그래서 등장한 패턴: PRG (Post-Redirect-Get)

폼 제출 후에는 뷰를 바로 렌더링하는 것이 아니라, redirect로 GET 요청을 보내도록 해야 한다.

```java
@PostMapping("/add")
public String addItem(Item item) {
    itemRepository.save(item);
    return "redirect:/basic/items/" + item.getId();
}
```

변경 후 흐름:

```
POST /basic/items/add  →  상품 저장 →  redirect /basic/items/1
브라우저가 GET /basic/items/1 다시 요청 → 상세 페이지 렌더링
```

이제 브라우저의 "마지막 요청" 기록은 GET이 됨.

따라서:

```
사용자 F5 (새로고침)
→ GET /basic/items/1 반복
→ 데이터 중복 저장 없음
```

이 패턴이 웹 개발에서 사실상 표준이고, 많은 프레임워크들이 이에 맞게 구현하도록 권장하고 있음.

---

## 정리

| 흐름                                | 새로고침 결과           |
| --------------------------------- | ----------------- |
| POST → 뷰 렌더링                      | POST 재전송 → 데이터 중복 |
| POST → redirect → GET → 뷰 렌더링 | GET만 반복 → 안전      |

즉, redirect를 하지 않아서 발생한 문제이고,
상품 등록 후 상세 페이지로 redirect하도록 수정하면 해결됨.

---

원래 코드가 V4에서 이렇게 돼야 최종적으로 안전한 구조:

```java
@PostMapping("/add")
public String addItem(Item item) {
    itemRepository.save(item);
    return "redirect:/basic/items/" + item.getId();
}
```

이렇게 바꿔주면 새로고침해도 POST가 반복되지 않아.

결론부터 말하면 맞다. `return "redirect:/basic/items/{id}"` 안의 `{id}`는 `RedirectAttributes`에 넣은 id 값으로 치환된다.

설명 흐름:

---

### 1. RedirectAttributes 동작

```java
ras.addAttribute("id", savedItem.getId());
ras.addAttribute("status", true);
```

여기서 `ras`에 `id`와 `status`를 저장하면 스프링이 리다이렉트 경로에 값을 바인딩한다.

---

### 2. 경로 변수 치환

```java
return "redirect:/basic/items/{id}";
```

여기서 `{id}`라고 적혀 있으므로 `RedirectAttributes` 안에 있는 `id` 값을 사용해 치환된다.

예를 들어 `savedItem.getId()`가 10이라면:

```
/basic/items/10
```

이렇게 된다.

---

### 3. 나머지 파라미터 처리

`status`는 경로 변수에 자리가 없으므로 쿼리 파라미터로 붙는다.

최종 리다이렉트 URL 예시:

```
/basic/items/10?status=true
```

---

### 정리

| 코드 요소    | 사용 방식                               |
| -------- | ----------------------------------- |
| `{id}`   | RedirectAttributes의 `id`를 경로 변수로 치환 |
| `status` | 경로에서 자리를 못 찾으면 쿼리 파라미터로 추가          |

---

따라서 질문에 대한 답:

> 여기서 {id}는 ras의 id야?

그렇다. `ras.addAttribute("id", ...)` 로 추가한 id가 `{id}` 자리에 자동으로 들어간다.

문제의 원인은 `form` 태그의 `action` 때문이다.

현재 코드:

```html
<form action="item.html" method="post">
```

이렇게 되어 있으니 POST 요청을 `/basic/items/item.html` 로 보내게 된다.
왜냐하면 이 페이지 자체가 `/basic/items` 경로에서 렌더링되므로 상대경로 규칙이 적용된다:

```
/basic/items  +  item.html  →  /basic/items/item.html
```

하지만 스프링 컨트롤러에는 `/basic/items/item.html` 같은 요청을 처리하는 매핑이 없다.
그래서 404가 발생하는 것이다.

---

### 해결 방법

상품 등록 POST 요청을 스프링 컨트롤러가 받도록 `action`을 설정해야 한다.

예시:

```html
<form th:action="@{/basic/items/add}" method="post">
```

또는 손으로 쓰는 경우:

```html
<form action="/basic/items/add" method="post">
```

이렇게 하면 POST가 다음으로 정상적으로 보내진다:

```
POST /basic/items/add
```

그리고 컨트롤러의 `@PostMapping("/add")`가 실행된다.

---

### 정리

| 현재                                                               | 결과                                             |
| ---------------------------------------------------------------- | ---------------------------------------------- |
| `action="item.html"`                                             | 상대경로 처리되어 `/basic/items/item.html` 로 요청됨 → 404 |
| `action="/basic/items/add"` 또는 `th:action="@{/basic/items/add}"` | 컨트롤러의 `/basic/items/add` 로 요청됨 → 정상 동작         |

---

### 추가 팁

Thymeleaf를 사용할 때는 속성 하나만 변경해도 된다.

```html
<form th:action="@{/basic/items/add}" method="post">
```

`action`과 `th:action`을 동시에 쓰지 말고 Thymeleaf가 JSP-style 경로 생성을 대체하도록 `th:action`만 사용하는 것이 권장된다.

가능하다.
스프링 MVC에서는 GET과 POST를 같은 URI로 매핑해도 된다.
HTTP 메서드만 다르면 전혀 충돌하지 않는다.

예시:

```java
@GetMapping("/add")
public String addForm() {
    return "basic/addForm";
}

@PostMapping("/add")
public String addItem(Item item) {
    itemRepository.save(item);
    return "redirect:/basic/items";
}
```

이 경우 흐름은 다음과 같다:

1. `/basic/items/add` GET 요청 → 등록 폼 화면 표시
2. 버튼 눌러서 same URI `/basic/items/add` 로 POST 요청 → 저장 처리

즉 경로는 그대로 두고 HTTP 메서드만 바꿔서 폼 화면과 저장 로직을 같은 URL에서 처리하는 패턴이 가능하다.

---

### 하지만 폼에서 `method="post"`로 보내야 함

폼에서 action을 GET 요청 화면과 동일하게 맞추기만 하면 된다.

```html
<form th:action="@{/basic/items/add}" method="post">
```

GET 화면을 띄운 `/basic/items/add`에서 submit하면 같은 `/basic/items/add`로 POST 요청이 보내진다.

---

### 주의해야 할 점

가능하지만 한 가지 문제점이 있다.

POST 후 성공 화면 없이 새로고침하면 브라우저가 같은 POST를 다시 보낸다.
사용자는 상품이 중복 등록되는 문제가 발생할 수 있다.

그래서 보통 다음 패턴을 쓴다.

#### PRG 패턴(Post-Redirect-Get)

1. POST → 저장
2. redirect
3. GET으로 상세 페이지 또는 목록을 다시 요청

예시:

```java
@PostMapping("/add")
public String addItem(Item item, RedirectAttributes ras) {
    Item saved = itemRepository.save(item);
    ras.addAttribute("id", saved.getId());
    return "redirect:/basic/items/{id}";
}
```

이 방식이면 POST 후 새로고침해도 중복 저장되지 않는다.

---

### 정리

| 방식                       | 가능 여부 | 장점                 | 단점                 |
| ------------------------ | ----- | ------------------ | ------------------ |
| GET/POST 동일 URI          | 가능    | 깔끔한 주소             | 새로고침 시 POST 재실행 위험 |
| PRG 패턴 (POST 후 redirect) | 권장    | 새로고침으로 인한 중복 요청 방지 | 주소가 한 번 더 이동함      |

결론:

> 같은 경로로 GET과 POST를 매핑해서 폼 표시와 저장을 처리할 수 있다.
> 하지만 새로고침 문제 때문에 보통 저장 후에는 redirect를 사용한다.

`form action`을 비워 두면 기술적으로는 현재 페이지의 경로로 그대로 POST 요청이 간다.
즉, 이런 코드는:

```html
<form method="post">
```

지금 화면을 GET으로 띄운 경로가 `/basic/items/add`라면, submit 시 그대로

```
POST /basic/items/add
```

이 된다.
따라서 GET과 POST를 같은 경로로 매핑한 경우 동작은 잘 된다.

---

### 그런데 권장되지 않는 이유

1. 의도가 코드에서 보이지 않는다
   action이 비어 있으면, GET과 POST의 URL 연결을 코드를 읽는 사람은 추론해야 한다.
   명시적으로 `th:action="@{/basic/items/add}"`라고 쓰는 것이 폼의 목적을 명확하게 한다.

2. 앞으로 경로가 바뀌면 폼이 깨질 수 있다
   나중에 템플릿 파일이 다른 경로에서 재사용되거나 view 경로만 바뀌는 상황이 생기면
   action이 비어 있는 폼은 엉뚱한 URL로 보내질 수 있다.

3. HTML 명세 문제
   action을 비워 두면 브라우저마다 동작이 미묘하게 달랐던 이슈가 과거에 존재했다.
   지금은 대부분 현재 URL로 보내지만, 스프링 생태계에서는 명시적 action을 사용하는 것이 베스트 프랙티스로 자리잡았다.

---

### 언제 괜찮은가

학습용 또는 내부 관리 페이지처럼 URI 변화 가능성이 거의 없고,
설계를 단순히 유지하고 싶을 때는 action을 비워도 문제 없이 작동한다.

즉:

* 실무 → 명시적 action 추천
* 학습/테스트 → action 비워도 동작

---

### 한 줄 결론

> action을 비워 두면 현재 경로로 POST되기 때문에 GET/POST 같은 경로 매핑에 기술적으로 문제는 없지만, 유지보수성과 명확성을 위해 보통은 `th:action="@{/...}"` 처럼 명시하는 것이 더 좋은 선택이다.

현재 화면에서 상품 목록 버튼이 안 눌리는 이유는 자바스크립트 onclick 속성이 충돌하거나 무효화되었기 때문이다.

지금 코드:

```html
<button class="w-100 btn btn-secondary btn-lg" onclick="location.href='items.html'"
        th:onclick="|location.href='@{/basic/items}'"
        type="button">상품 목록</button>
```

여기서 `onclick`과 `th:onclick`을 동시에 사용하고 있다.
Thymeleaf가 렌더링할 때 `th:onclick`이 `onclick`을 대체하지만, 브라우저에서 해석될 때 앞뒤 공백·따옴표 문제 등으로 이벤트가 무시될 수 있다.

즉 둘 다 적으면 충돌 가능성이 높다.

---

### 해결 방법

`onclick`을 하나만 남기면 정상 동작한다.

Thymeleaf를 사용할 것이므로 다음처럼 고치는 것이 좋다:

```html
<button class="w-100 btn btn-secondary btn-lg"
        th:onclick="|location.href='@{/basic/items}'|"
        type="button">상품 목록</button>
```

또는 자바스크립트 없이 href 방식으로도 표현 가능하다:

```html
<a class="w-100 btn btn-secondary btn-lg"
   th:href="@{/basic/items}">상품 목록</a>
```

이 방법이 Bootstrap과 함께 가장 많이 사용된다.

---

### 정리

| 버튼 방식                       | 동작 여부              |
| --------------------------- | ------------------ |
| onclick + th:onclick 동시에 사용 | 충돌로 인해 동작 안 될 수 있음 |
| th:onclick 하나만 사용           | 정상                 |
| 버튼 대신 a 태그 + href 사용        | 가장 깔끔하고 권장         |

---

따라서 버튼이 안 눌리는 문제는 onclick 속성 중복 때문이다.
하나만 남기면 정상 동작한다.

지금 상황은 상품 수정 버튼을 눌렀을 때 요청은 보내지지만 스프링 컨트롤러가 받지 못해서 동작이 안 되는 상태다.

원인은 form 태그의 action 설정 때문이다.

현재 코드:

```html
<form action="item.html" method="post">
```

이렇게 돼 있으니 폼을 제출하면 브라우저는 다음으로 POST 요청을 보낸다.

```
/basic/items/item.html
```

그러나 컨트롤러에 `/basic/items/item.html`을 처리하는 `@PostMapping`은 없으므로 저장/수정이 되지 않는다.

---

### 해결 방법

상품 수정용 컨트롤러 매핑 경로에 맞춰서 action을 바꿔야 한다.

일반적으로 다음 두 방식 중 하나를 사용한다.

#### 1) 수정할 상품 ID를 포함하는 방식

```html
<form th:action="@{/basic/items/{id}(id=${item.id})}" method="post">
```

그리고 컨트롤러는 예를 들어 이런 식으로 있어야 한다:

```java
@PostMapping("/{id}/edit")
public String edit(@PathVariable Long id, Item item) {
    itemRepository.update(id, item);
    return "redirect:/basic/items/{id}";
}
```

#### 2) 별도 edit 경로로 보낼 때

```html
<form th:action="@{/basic/items/edit}" method="post">
```

컨트롤러:

```java
@PostMapping("/edit")
public String edit(Item item) { ... }
```

---

### 추가 확인 포인트

폼 value 설정은 문제 아니다.

오히려 다음 부분이 문제를 더 확실하게 드러낸다.

```
상품 수정 버튼을 눌렀더니 아무 반응 없는 것처럼 보임
```

그 이유는:

* 요청은 보내졌지만 매핑된 컨트롤러가 없어서 템플릿을 찾지 못하고 404 또는 같은 화면을 다시 보여주기 때문
* 그래서 버튼이 안 눌린 것처럼 느껴지는 것

---

### 정리

| 현재                                                | 문제                                             |
| ------------------------------------------------- | ---------------------------------------------- |
| `action="item.html"`                              | POST가 `/basic/items/item.html`로 가서 컨트롤러가 받지 못함 |
| `th:action="@{/basic/items/{id}(id=${item.id})}"` | 컨트롤러가 정상적으로 수정 처리                              |

---

한 줄 결론

> 수정 버튼이 안 눌리는 게 아니라 `action`이 잘못되어 POST 요청이 컨트롤러로 가지 않는 상황이다. 폼 action을 수정 매핑 경로에 맞춰서 변경하면 정상 동작한다.

이 버튼은 폼 submit이 아니라 단순 페이지 이동이기 때문이다.

코드:

```html
<button class="btn btn-primary float-end"
        onclick="location.href='addForm.html'"
        th:onclick="|location.href='@{/basic/items/add}'|"
        type="button">상품 등록</button>
```

동작 방식 분석:

1. `type="button"`
   submit이 아니라 단순 클릭용 버튼임
   즉 form 제출이 아니라 단순 GET 요청으로 페이지 이동

2. onclick 실행
   실제 렌더링 시 `th:onclick`이 `onclick`을 대체하여 다음처럼 바뀜:

   ```
   onclick="location.href='/basic/items/add'"
   ```

   즉 클릭하면 브라우저가 /basic/items/add로 이동

3. 컨트롤러에는 보통 다음 같은 매핑이 존재

   ```java
   @GetMapping("/add")
   public String addForm() {
       return "basic/addForm";
   }
   ```

   GET 요청이 정상적으로 처리되므로 화면이 잘 뜸

---

### 이 버튼이 잘 동작하는 핵심 이유

| 요소             | 결과             |
| -------------- | -------------- |
| GET 요청         | 페이지 이동에 적합     |
| onclick        | 단순 URL 이동      |
| type="button"  | form submit 아님 |
| 컨트롤러 GET 매핑 존재 | 경로 일치          |

그래서 아무 문제 없이 작동한 것.

---

### 반대로 수정 버튼이 안 된 이유와 대조

| 버튼 유형    | 동작                  | action 영향               |
| -------- | ------------------- | ----------------------- |
| 상품 등록 버튼 | onclick을 이용한 GET 이동 | form action과 무관         |
| 상품 수정 버튼 | form submit → POST  | action이 잘못돼 있으면 요청이 안 감 |

---

한 줄로 정리

> 상품 등록 버튼은 단순 GET 이동이라 URL만 맞으면 문제 없이 동작하지만, 상품 수정 버튼은 form submit 방식이어서 action 경로가 컨트롤러 매핑과 맞지 않으면 동작하지 않는다.

지금 화면에서 두 버튼이 모두 안 눌리는 이유는 Thymeleaf 구문 오류 때문에 브라우저가 onclick과 action을 제대로 렌더링하지 못하고 있기 때문이다.
템플릿 자체는 보이지만, 렌더링된 HTML 내부에 문법이 깨진 JS 코드가 포함되면서 버튼이 동작하지 않게 된다.

핵심 원인은 여기:

```html
th:onclick="|location.href='@{/basic/items/{id}(id=${item.id})'|"
```

`|` 파이프 문법이 닫히지 않아 구문 오류가 발생한다.
문법적으로는 다음 형태가 맞다:

```
| 문자열 |   ← 양쪽에 파이프가 있어야 한다
```

그런데 현재는 `| … |"`가 아니라 `| … '` 로 닫혀 있지 않아서 전체 HTML 실행이 깨진 것이다.

---

### 수정된 올바른 버전

```html
<button class="w-100 btn btn-secondary btn-lg"
        th:onclick="|location.href='@{/basic/items/{id}(id=${item.id})}'|"
        type="button">상품 상세</button>
```

파이프를 앞뒤로 정확히 닫아주면 정상적으로 동작한다.

---

### 상품 수정 버튼이 안 눌린 이유도 함께 정리

상품 수정 버튼은 `onclick`이 아니라 `form` 제출이므로 원인은 다른 부분에 있다.

현재 form action:

```html
<form th:action="@{/basic/items/{id}(id=${item.id})}" method="post">
```

이 경로로 요청을 처리할 컨트롤러가 아래 방식으로 존재해야 한다:

```java
@PostMapping("/{id}")
public String edit(@PathVariable Long id, Item item) {
    itemRepository.update(id, item);
    return "redirect:/basic/items/{id}";
}
```

컨트롤러가 다음처럼 되어 있다면 동작 하지 않는다:

```java
@PostMapping("/{id}/edit")
```

그 경우 action을 이렇게 바꿔야 한다:

```html
<form th:action="@{/basic/items/{id}/edit(id=${item.id})}" method="post">
```

즉 수정 버튼 문제는 폼 action과 컨트롤러 POST 매핑 경로 불일치가 원인이다.

---

### 정리 표

| 버튼    | 동작 실패 이유                                           | 해결                   |     |             |
| ----- | -------------------------------------------------- | -------------------- | --- | ----------- |
| 상품 수정 | `form th:action` 경로가 컨트롤러의 `@PostMapping`과 일치하지 않음 | action 경로와 매핑 경로 맞추기 |     |             |
| 상품 상세 | `th:onclick` 파이프 문법 오류                             | `                    | ... | ` 로 정확히 감싸기 |

---

### 한 줄로 결론

> 상품 수정은 action 경로 불일치 때문에, 상품 상세는 th:onclick 문법 오류 때문에 동작하지 않았다.
> action 매핑을 컨트롤러와 맞추고 onclick 파이프 문법을 정확히 닫으면 정상 작동한다.

위 코드에서 파이프(|) 문법 사용은 적절하다.
특히 아래 형태처럼 URL 문자열 안에 변수를 직접 끼워 넣어야 할 때 파이프 문법이 적합하다.

```html
th:href="@{|/basic/items/${item.id}|}"
```

---

### 이유

Thymeleaf URL 표현 방식에는 두 가지 방법이 있다.

#### 방식 1: 매개변수 바인딩 방식

```html
th:href="@{/basic/items/{id}(id=${item.id})}"
```

장점: 매개변수 이름과 값을 명시적으로 전달
특징: URL 템플릿에서 `{id}`가 value로 치환됨

#### 방식 2: 파이프(|) 문자열 템플릿 방식

```html
th:href="@{|/basic/items/${item.id}|}"
```

장점: 문자열 안에서 변수 삽입이 직관적
특징: URL 그 자체를 문자열로 구성, 따옴표/슬래시 등 그대로 유지

---

### 두 방식의 차이

| 항목 | 매개변수 바인딩 방식 | 파이프(|) 템플릿 방식 |
|------|---------------------|-----------------------|
| 형식 | `{id}(id=...)` | `/path/${var}` |
| 가독성 | 값이 많아도 명확 | 값이 많으면 보기 불편 |
| 변수 개수 | 여러 변수에 적합 | 1~2개 정도일 때 편리 |
| URL 변경 시 안정성 | 높음 | 상대적으로 낮음 |

---

### 지금 코드에서 파이프 문법이 적절한 이유

여기서는 `/basic/items/${item.id}` 라는 단순한 URL을 만드는 상황이다.
따라서 파이프 문법이 더 짧고 자연스럽다, 문제 없다.

반대로 아래처럼 쿼리 파라미터가 여러 개라면 첫 번째 방식이 더 적합하다:

```html
th:href="@{/basic/items/{id}(id=${item.id}, status=true, sort='latest')}"
```

---

### 요약

| Task               | 적절한 방식                |          |
| ------------------ | --------------------- | -------- |
| 단일 값으로 URL 치환      | 파이프(                  | ) 방식이 적절 |
| 여러 파라미터 또는 복잡한 URL | `{id}(id=...)` 방식이 적절 |          |

---

따라서 질문에 대한 대답

> 여기선 파이프 문법이 적절해?

네, `/basic/items/${item.id}` 형태를 만들고 있으므로 파이프 문법 사용이 적절한 상황이다.
