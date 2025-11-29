`th:classappend` 표현식이 **실행되지 않는 이유**는 `errors?.containsKey(...)` 구문 때문입니다.
Thymeleaf에서는 **`?.`(null-safe operator)** 를 지원하지 않습니다.
그래서 이 전체 조건식이 **파싱조차 되지 않아 동작하지 않는 상태**입니다.

아래처럼 조건을 분리하거나, `errors != null` 로 명시해야 합니다.

---

# 1. 가장 간단한 해결 방법

```html
th:classappend="${errors != null} ? 'field-error' : _"
```

검증 실패하면 `errors`가 null이 아니므로 클래스가 적용됩니다.

---

# 2. 특정 필드 오류 여부까지 체크하고 싶다면

Thymeleaf는 `Map`에 대해 `${errors['itemName']}` 형태로 접근할 수 있습니다.
value가 존재하면 true로 평가됩니다.

```html
th:classappend="${errors != null and (errors['itemName'] != null
    or errors['price'] != null
    or errors['quantity'] != null)} ? 'field-error' : _"
```

---

# 3. 더 깔끔하게: 각 필드별 class 적용

이런 경우가 더 일반적입니다.

```html
<input type="text" th:field="*{itemName}"
       th:classappend="${errors != null and errors['itemName'] != null} ? 'field-error' : _">
```

price에도:

```html
<input type="number" th:field="*{price}"
       th:classappend="${errors != null and errors['price'] != null} ? 'field-error' : _">
```

---

# 4. `map.isEmpty()` 직접 체크도 가능

```html
th:classappend="${errors != null and !#maps.isEmpty(errors)} ? 'field-error' : _"
```

---

# 5. 왜 `errors?.containsKey()`가 안 되는가?

Thymeleaf는 Java의 null-safe operator(`?.`)를 지원하지 않으며,
삼항 연산자에서도 **논리식 안에 null-safe 접근이 있으면 전체 표현식이 무효 처리**됩니다.

그래서 `containsKey()` 자체가 실행되지 않았던 것입니다.

---

원하시면 전체 템플릿 코드를 보내주시면 class 처리와 오류 메시지 바인딩 구조를 함께 최적화해드릴게요.

`th:if="${errors?.containsKey('itemName')}"` 가 **동작하는 것처럼 보이지만**, 사실 이것은 `?.` 때문에 제대로 평가되는 것이 아니라 **Thymeleaf가 이 표현식을 “단순 변수 이름”처럼 처리해서 항상 null → false 평가**되는 방식 때문에 생긴 착시입니다.

즉,

* `th:if`에서는 **조건식이 null이면 false로 간주되어** 그냥 안 보일 뿐이고
* **파싱 에러가 발생하지 않고 무시**되기 때문에 “되는 것처럼” 보이는 것뿐입니다.

반면 `th:classappend`는 엄격한 파싱을 하므로 `?.`가 나타나는 순간 전체 표현식을 무효 처리해버려서 동작이 안 됩니다.

---

# 정리: 왜 `th:if`에서는 “되는 것처럼” 보이고, `th:classappend`에서는 안 되는가?

### 1. `th:if` 의 특성

`th:if`는 내부 표현식이 **null이거나 false이면 요소를 렌더링하지 않음**
그러니 `errors?.containsKey('itemName')`이 파싱 불가 → null → false 로 처리

→ 화면에서 오류가 없을 때는 자연스럽게 사라지므로 "잘 되는 것처럼" 보임

### 2. `th:classappend` 는 동작 방식이 다름

classappend는:

* 문자열 결합
* 기존 클래스 읽기
* 새 클래스만 추가
* `_` 처리

등이 포함돼서 **표현식 파싱 실패 시 HTML 구조 자체가 깨지거나 속성이 통째로 무효화**됩니다.

그래서 “버튼이 안 눌리는” 등의 부작용이 생길 수 있습니다.

---

# 정확하게 null-safe 체크하는 방법

Thymeleaf에서는 `?.` 없으므로 반드시 아래처럼 써야 합니다.

## 1) `th:if`

```html
<div th:if="${errors != null and errors.containsKey('itemName')}">
    <p class="field-error" th:text="${errors['itemName']}"></p>
</div>
```

## 2) `th:classappend`

```html
th:classappend="${errors != null and errors.containsKey('itemName')} ? 'field-error' : _"
```

또는 Map 접근식을 선호하면:

```html
th:classappend="${errors != null and errors['itemName'] != null} ? 'field-error' : _"
```

---

# 결론

* **`?.` 는 Thymeleaf에서 지원되지 않음**
* `th:if`는 null이면 false라서 동작해 보이는 것
* `th:classappend`는 파싱 실패하면 전체 속성이 무효화되어 버튼 등이 동작하지 않게 됨
* 반드시 `errors != null and errors.containsKey(...)` 형태로 써야 함

---

원하시면 전체 폼 템플릿을 보여주시면 에러 메시지 처리 구조를 가장 안전한 형태로 다시 잡아드릴 수 있습니다.
