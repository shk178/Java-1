아래 코드를 기준으로 하나씩 풀어서 설명해줄게.

```java
@GetMapping("/text")
public String text(Model model) {
    model.addAttribute("text", "Hello <b>Spring</b>");
    model.addAttribute("utext", "Hello <b>Spring</b>");
    // text: 이스케이프o (<, /, > 그대로 나옴)
    // utext: 이스케이프x (Spring 진하게 나옴)
    return "basic/text";
}
```

---

# 1. 이 메서드의 역할

`/text` URL로 GET 요청이 들어오면, `basic/text.html` 템플릿을 렌더링해서 반환하는 컨트롤러 메서드다.

즉:
클라이언트 → /text 요청 → 템플릿 렌더링 → HTML 응답

---

# 2. Model에 값 넣기

```java
model.addAttribute("text", "Hello <b>Spring</b>");
model.addAttribute("utext", "Hello <b>Spring</b>");
```

Model은 템플릿(Thymeleaf)에 데이터를 넘겨주는 역할을 한다.

* `text` 이름으로 `"Hello <b>Spring</b>"` 저장
* `utext` 이름으로 `"Hello <b>Spring</b>"` 저장

두 값은 문자열은 동일하다.
단지 템플릿에서 어떻게 출력하느냐에 따라 결과가 달라짐.

---

# 3. Thymeleaf에서의 차이: `th:text` vs `th:utext`

Thymeleaf는 기본적으로 HTML을 안전하게 렌더링하기 위해 escape(이스케이프) 처리를 한다.

## • `th:text="text"`

* HTML 이스케이프 O
* 문자열 안의 `<`, `>`, `/`, `"` 같은 HTML 태그 문자를 그대로 글자로 처리
* 그래서 페이지에 *Spring*이 굵게 보이지 않고, `<b>Spring</b>`이 그대로 출력됨.

예:

```
Hello <b>Spring</b>
```

(태그가 작동하지 않고 그냥 텍스트)

---

## • `th:utext="utext"`

* HTML 이스케이프 X
* 문자열을 HTML 로 해석함
* `<b></b>` 태그가 실제로 적용되어 “Spring”이 굵게 표시됨.

예:
Hello Spring

---

# 4. 정리

| 속성         | HTML 태그 해석 여부     | 출력 결과                  |
| ---------- | ----------------- | ---------------------- |
| `th:text`  | 해석하지 않음 (이스케이프 O) | `<b>Spring</b>` 그대로 출력 |
| `th:utext` | 해석함 (이스케이프 X)     | Spring이 굵게 보임          |

---

# 5. 왜 이 두 가지가 필요한가?

* `th:text`는 XSS(스크립트 공격) 방지용 기본 출력 방식
* `th:utext`는 HTML 태그가 포함된 안전한 내용을 출력해야 할 경우 사용

예를 들어, 사용자 입력을 그대로 `utext`로 출력하면 보안 문제가 생김.

---

필요하다면 `basic/text.html`에서 어떤 식으로 사용해야 하는지 예시도 만들어줄게.

아래에서 Spring EL(SpEL)이 무엇인지, 그리고 타임리프가 그것을 어떻게 활용하는지를 구조적으로 설명해줄게.

---

# 1. Spring EL(Expression Language)란?

Spring 프레임워크에서 사용하는 표현식 언어다.
쉽게 말하면, *문자열 안에서 특정 문법을 사용해 객체의 속성, 메서드 호출, 연산 등을 수행할 수 있게 해주는 문법*.

예를 들어:

* 객체의 필드를 읽기: `person.name`
* 메서드 호출: `person.getAge()`
* 조건문: `age > 18`
* 컬렉션 접근: `list[0]`
* 빈(bean) 접근: `@myService.method()`

Spring이 관리하는 객체(Model, Bean 등)를 템플릿이나 설정 파일에서 편하게 접근하도록 만들어주는 언어라서 SpEL이라고 부른다.

---

# 2. SpEL의 구문 형태

SpEL은 일반적으로 #{...} 문법으로 사용된다.

예:

```java
@Value("#{1 + 2}")  // 3
@Value("#{myBean.name}") 
```

그러나 타임리프에서는 ${...} 형태로 SpEL을 기반한 표현식을 사용한다.
즉, 타임리프는 SpEL을 그대로 가져다 쓰는 것이 아니라 SpEL 스타일의 표현식을 자신의 문법 속에 통합한 방식이다.

---

# 3. 타임리프의 표현식은 SpEL을 기반으로 한다

타임리프에서 우리가 사용하는 다음 문법들:

```html
th:text="${user.name}"
th:if="${age > 20}"
th:each="item : ${list}"
```

여기 있는 `${...}` 내부의 표현식 엔진이 바로 Spring EL을 기반으로 한다.

즉:

* `${user.name}` → SpEL이 user 객체에서 name 값을 꺼냄
* `${age > 20}` → SpEL이 조건 연산을 수행
* `${list[0]}` → SpEL이 컬렉션 접근 수행
* `${#strings.toUpperCase(text)}` → SpEL의 유틸리티 객체 호출

타임리프는 자체 파서를 가지지만, 내부적으로 SpEL 규칙을 따라 평가를 수행한다.

---

# 4. 타임리프가 SpEL을 사용하는 방식

## (1) `${...}` 표현식

가장 기본적인 EL.
Model에 넣어둔 데이터를 가져오거나, 단순 연산, 메서드 호출 등을 수행한다.

예:

```html
<span th:text="${dto.title}"></span>
<span th:text="${1 + 2}"></span>
```

타임리프는 이 EL을 처리할 때 SpEL 엔진을 이용하여 값을 평가한 뒤, HTML에 텍스트로 삽입한다.

---

## (2) 유틸리티 객체(Static Utility Objects)

타임리프는 SpEL을 확장하여 `#strings`, `#dates`, `#numbers` 등 유틸리티 객체를 제공한다.

예:

```html
<span th:text="${#strings.toUpperCase(name)}"></span>
```

이것은 사실상 SpEL의 메서드 호출 방식으로 동작한다.

---

## (3) 변수 접근

SpEL 규칙에 따라, 아래와 같은 접근이 모두 가능하다:

```html
${user.name}
${user.getName()}
${map['key']}
${list[0]}
```

이것은 SpEL의 프로퍼티 접근자 규칙을 그대로 따르는 것이다.

---

## (4) 조건문 / 논리연산

모두 SpEL이 지원하는 연산:

```html
th:if="${age > 18}"
th:unless="${list.size() == 0}"
```

---

# 5. 정리

| 역할                | 설명                                        |
| ----------------- | ----------------------------------------- |
| SpEL          | Spring이 제공하는 표현식 언어. 객체 접근, 연산, 메서드 호출 가능 |
| Thymeleaf의 EL | `${...}` 형태. SpEL 문법과 평가 엔진을 기반으로 동작      |
| 결과            | 타임리프 템플릿에서 Model, Bean, 컬렉션을 자유롭게 다룰 수 있음 |

따라서 타임리프는 SpEL을 HTML 템플릿에서 쉽게 쓰도록 포장한 표현식 시스템을 갖고 있다고 이해하면 된다.

---

원한다면,

* SpEL의 문법
* 타임리프 표현식 전체 정리
* `${}`, `*{}`, `#{}` 차이
  도 이어서 설명해줄게.

아래에 문제점 정리와 주의할 점만 다시 간결하게 정돈해 드립니다.

---

# 문제점 정리

1. `${...}`를 URL에 사용함 → 문법 오류

  * `${...}`는 변수 참조용이며 URL 생성은 불가.
  * URL은 반드시 `@{...}`를 사용해야 함.

2. `${...}` 안에 또 `${...}` 포함 → 중첩 불가

  * EL 내부에 EL 표현식을 다시 넣을 수 없음.

3. `${/hello/${param}}` 같은 문자열 결합 → 불가능

  * EL 내부에서 문자열 + 변수 형태로 URL을 조합할 수 없음.

4. PathVariable 문법 오류

  * `/hello/{param1}` 형태는 `@{...}` 안에서만 의미가 있음.
  * `${...}`에서 `{param1}`은 무효.

5. URL 파라미터 전달 방식 오류

  * 쿼리 파라미터를 `/hello(param1=data)`처럼 EL에서 직접 쓸 수 없음.
  * 올바른 방식은 `@{/hello(param1=${value})}`.

---

# 주의할 점

1. URL은 항상 `@{...}` 로 감싸기

  * `th:href="@{/path}"` 형태로 작성해야 함.

2. PathVariable은 `{name}`로 표시하고 `(name=${value})`로 바인딩

  * 예: `@{/hello/{id}(id=${userId})}`

3. 쿼리 파라미터도 `(key=${value})` 형식으로 작성

  * 예: `@{/hello(param1=${p1}, param2=${p2})}`

4. `@{...}` 안에서는 스프링 EL의 `${...}`만 값 부분에 사용

  * 경로나 변수 이름 부분에는 EL 사용 불가.

5. EL 내부에서는 문자열 연결로 URL을 만들 수 없음

  * `/hello/${param}` 같은 구조는 작동하지 않음.
  * URL 조합은 반드시 `@{...}` 내부 규칙 사용.

---

필요하시면 잘못된 코드 → 올바른 코드 비교 표로 정리해서 드릴 수도 있습니다.

아래는 질문에서 작성하신 링크들을 Thymeleaf 규칙에 맞게 모두 수정한 완성된 올바른 코드입니다.
이대로 붙여넣으면 정상 동작합니다.

---

# 올바른 전체 코드

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Link</title>
</head>
<body>
<ul>
    <!-- 단순 링크 -->
    <li><a th:href="@{/hello}">/hello</a></li>

    <!-- 쿼리 파라미터 1개 -->
    <li><a th:href="@{/hello(param1=${param1})}">/hello?param1=data1</a></li>

    <!-- 쿼리 파라미터 2개 -->
    <li><a th:href="@{/hello(param1=${param1}, param2=${param2})}">
        /hello?param1=data1&param2=data2
    </a></li>

    <!-- PathVariable 2개 -->
    <li><a th:href="@{/hello/{param1}/{param2}(param1=${param1}, param2=${param2})}">
        /hello/data1/data2
    </a></li>

    <!-- PathVariable 2개 (변수명 변경 가능: p1, p2) -->
    <li><a th:href="@{/hello/{p1}/{p2}(p1=${param1}, p2=${param2})}">
        /hello/data1/data2
    </a></li>

    <!-- PathVariable + 쿼리 파라미터 혼합 -->
    <li><a th:href="@{/hello/{param1}(param1=${param1}, param2=${param2})}">
        /hello/data1?param2=data2
    </a></li>
</ul>
</body>
</html>
```

---

# 각 링크가 실제로 만들어지는 형태 정리

| 코드                                      | 실제 URL 예시                          |
| --------------------------------------- | ---------------------------------- |
| `@{/hello}`                             | `/hello`                           |
| `@{/hello(param1=${param1})}`           | `/hello?param1=data1`              |
| `@{/hello(param1=${p1}, param2=${p2})}` | `/hello?param1=data1&param2=data2` |
| `@{/hello/{param1}/{param2}(...)}`      | `/hello/data1/data2`               |
| `@{/hello/{param1}(param2=${param2})}`  | `/hello/data1?param2=data2`        |

---

필요하면 각 항목을 왜 이렇게 작성해야 하는지도 구조별로 설명해 드릴게요.

`/hello?param1=data1&param2=data2` 부분에서 & 문자가 특수문자처럼 보이거나 `&para` 같은 기호로 출력되는 이유는 HTML에서 `&`가 특수문자 시작 기호이기 때문입니다.

---

# 이유

HTML에서 `&`는 HTML 엔티티 시작 문자입니다.

예를 들어:

* `&lt;` → `<`
* `&gt;` → `>`
* `&amp;` → `&`
* `&para;` → ¶ (문단 기호)

즉, HTML은 `&`가 나오면 “이건 엔티티인가?” 하고 해석하려고 합니다.
그래서 텍스트 안에 그냥 `&` 를 쓰면 브라우저가 엔티티로 오해하여
`&para` 같은 특수문자로 바꿔버릴 수 있습니다.

---

# 왜 `th:href`는 정상인데 텍스트 부분만 깨질까

링크는 두 부분으로 나뉩니다.

1. th:href 속성 내부

  * 여기서는 Thymeleaf가 알아서 URL 인코딩을 처리하기 때문에 문제 없음

2. 사용자가 적은 링크 설명(텍스트)
   즉, 이런 부분:

   ```html
   /hello?param1=data1&param2=data2
   ```

   이 부분은 HTML 내용으로 직접 출력되므로
   HTML 엔티티 규칙이 그대로 적용됩니다.
   그래서 `&param2`가 엔티티처럼 해석되고 깨집니다.

---

# 해결 방법

## 방법 1: `&` 대신 `&amp;` 사용

HTML에서 텍스트로 `&`를 표시하려면 반드시 `&amp;` 로 적어야 합니다.

```html
<li><a th:href="@{/hello(param1=${param1}, param2=${param2})}">
    /hello?param1=data1&amp;param2=data2
</a></li>
```

이렇게 하면 텍스트로는 올바르게 보입니다.

---

## 방법 2: 텍스트 자체를 Thymeleaf로 출력하도록 변경 (추천)

텍스트도 Thymeleaf가 처리하면 HTML 엔티티 충돌이 없습니다.

```html
<li>
    <a th:href="@{/hello(param1=${param1}, param2=${param2})}"
       th:text="'/hello?param1=' + ${param1} + '&param2=' + ${param2}">
    </a>
</li>
```

이렇게 하면 `&`도 자동으로 안전하게 출력됩니다.

---

# 정리

* HTML에서는 `&`가 특수문자 시작 문자인데
  텍스트 영역에서 그냥 `&`를 쓰면 오해해서 `&para` 같은 기호로 변환될 수 있음.
* 그래서 텍스트에서는 `&amp;`로 써야 함.
* 또는 텍스트도 Thymeleaf로 렌더링하도록 만들어도 해결됨.

---

필요하시면 링크 텍스트 자동 생성 방식을 더 깔끔하게 바꿔 드릴 수도 있습니다.
