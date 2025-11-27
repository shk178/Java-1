타임리프(Thymeleaf)에서 리터럴(literal)은 *문자 그대로의 값*을 템플릿에서 표현식으로 사용할 때 쓰는 개념이다.
즉, 변수가 아니라 고정된 값(문자, 문자열, 숫자, 불린 등)을 의미하며, 스프링 EL(SpEL) 문법과는 조금 다르게 처리되는 부분이 있다.

아래에서 핵심 개념을 깔끔히 정리해줄게.

---

# 1. 리터럴의 종류

타임리프는 크게 다음과 같은 리터럴을 지원한다.

## 1) 텍스트 리터럴 (문자열)

문자열은 작은따옴표(' ')로 감싸야 한다.

```html
<span th:text="'hello'"></span>
```

만약 문자열 안에 작은따옴표가 필요하면 두 개 연속으로 사용한다.

```html
<span th:text="'It''s good'"></span>
```

※ 큰따옴표(" ")로 문자열을 감쌀 수는 없다.

---

## 2) 숫자 리터럴

그냥 숫자만 쓰면 된다.

```html
<span th:text="10"></span>
<span th:text="3.14"></span>
```

---

## 3) 불린 리터럴

```html
<span th:text="true"></span>
<span th:text="false"></span>
```

---

## 4) Null 리터럴

```html
<span th:text="null"></span>
```

---

# 2. 텍스트 리터럴 — 작은따옴표가 필요한 이유

타임리프 표현식에서 다음과 같이 쓰면 오류가 난다.

```html
<span th:text="hello"></span>
```

왜냐하면 타임리프는 작은따옴표 없는 단어를 변수 이름으로 해석하려고 시도하기 때문이다.
따라서 없는 변수라면 오류가 발생하거나 빈 값이 된다.

문자 그대로 출력하고 싶다면 반드시 작은따옴표:

```html
<span th:text="'hello'"></span>
```

---

# 3. 문자열 리터럴과 변수의 결합

## 1) 기본 문자열 + 변수 연결

Thymeleaf는 자동 연결 기능(`| ... |`)을 제공한다.

```html
<span th:text="|Hello ${name}|"></span>
```

이 방식은 문자열 리터럴 규칙을 신경 쓸 필요 없이 가장 편하다.

## 2) 수동 연결 (+ 사용)

```html
<span th:text="'Hello ' + ${name}"></span>
```

---

# 4. 리터럴 처리 시 주의할 점

### 1) 문자열은 반드시 작은따옴표로

```html
th:text="'abc'"   ←  가능  
th:text="abc"     ←  abc라는 변수 찾게 되어 오류
```

### 2) URL 리터럴에서도 문자열 규칙 적용됨

```html
<a th:href="@{'/hello/' + ${id}}"></a>
```

### 3) 리터럴이 아닌 변수 이름과 헷갈리지 않도록

특히 `$data` 처럼 ‘식별자 규칙’을 어기는 경우 오류가 발생한다.

---

# 5. 가장 실용적인 패턴

실무에서 잘 쓰는 안정적인 방식은 아래 두 가지다.

### 텍스트는 `| ... |`

```html
<span th:text="|Hello ${username}|"></span>
```

### URL은 `@{}` + 리터럴/변수 조합

```html
<a th:href="@{/hello/{id}(id=${user.id})}">go</a>
```

---

필요하다면 리터럴 처리에서 자주 나오는 실수 사례도 더 정리해줄게.

가능하다. 숫자, 불린, null 리터럴은 모두 타임리프 표현식(OGNL/SpEL) 안에서 연산될 수 있다.
아래에서 각각 어떻게 동작하는지 정리해줄게.

---

# 1. 숫자 리터럴은 완전한 산술 연산 가능

타임리프는 숫자 리터럴을 정수/실수로 인식하므로 기본적인 산술은 모두 된다.

예시:

```html
<span th:text="1 + 2"></span>              <!-- 3 -->
<span th:text="10 - 3"></span>             <!-- 7 -->
<span th:text="2 * 3"></span>              <!-- 6 -->
<span th:text="10 / 4"></span>             <!-- 2.5 -->
<span th:text="(5 + 5) * 2"></span>        <!-- 20 -->
```

또한 숫자 리터럴과 변수도 함께 연산 가능:

```html
<span th:text="${price} * 1.1"></span>
```

---

# 2. 불린(boolean) 리터럴은 논리 연산 가능

불린 리터럴도 SpEL의 불린 연산 규칙을 그대로 따른다.

```html
<span th:text="true and true"></span>       <!-- true -->
<span th:text="true or false"></span>       <!-- true -->
<span th:text="not false"></span>           <!-- true -->
```

조건식으로도 사용 가능:

```html
<span th:text="${age} > 18"></span>
```

---

# 3. null 리터럴은 "비교"와 "널 체크" 연산 가능

null은 산술 연산에는 사용할 수 없지만, 비교나 null 체크는 가능하다.

예시:

## 1) null 비교

```html
<span th:text="${user} == null"></span>
<span th:text="${user} != null"></span>
```

## 2) 삼항 연산자와 함께 사용

```html
<span th:text="${user} == null ? 'no user' : user.username"></span>
```

## 3) 문자열/숫자와의 직접 연산은 불가

아래와 같은 연산은 오류:

```html
<!-- 오류: null + 1 -->
<span th:text="null + 1"></span>
```

스프링 EL에서 null은 산술 연산이 불가능한 값이기 때문.

---

# 4. 함께 조합된 예시

### 숫자 + 불린(불린의 산술 변환은 지원되지 않음)

자바처럼 true=1, false=0으로 취급하지 않는다.
따라서 다음은 불가:

```html
<!-- 오류 -->
<span th:text="true + 1"></span>
```

### null과 불린 연산

불가능. 타입이 다르기 때문.

---

# 5. 정리

| 리터럴 종류 | 연산 가능 여부 | 설명                   |
| ------ | -------- | -------------------- |
| 숫자     | 가능       | 산술 연산 모두 허용          |
| 불린     | 가능       | and/or/not로 논리 연산 가능 |
| null   | 부분 가능    | 비교/조건식은 가능, 산술은 불가   |

---

원하면 숫자, 불린, null이 사용된 실제 Thymeleaf 템플릿 예제도 만들어줄게.

컨트롤러에서 모델에 값을 넣을 때, 문자열이 아니라 실제 숫자/불린/null 타입 그대로 넣어야 템플릿에서 리터럴처럼 정상적으로 동작한다.
지금 코드에서는 `"1"`, `"2"`처럼 문자열이 들어가 있어서 산술 연산이 불가능하다.

아래에서 상황별로 정확하게 정리해줄게.

---

# 1. 숫자를 넘기려면

문자열 `"1"`이 아니라 정수(Integer) 또는 실수(Double) 형태로 넣어야 한다.

```java
@GetMapping("/operation")
public String operation(Model model) {
    model.addAttribute("value1", 1);       // Integer
    model.addAttribute("value2", 2);       // Integer
    model.addAttribute("value3", 3.14);    // Double (원하면)

    return "basic/operation";
}
```

그럼 템플릿에서는 이렇게 연산 가능해진다.

```html
<span th:text="${value1} + ${value2}"></span>   <!-- 3 -->
<span th:text="${value3} * 2"></span>           <!-- 6.28 -->
```

---

# 2. 불린(boolean)을 넘기려면

문자열 `"true"`가 아니라 boolean 또는 Boolean 타입을 넣어야 한다.

```java
model.addAttribute("flag1", true);
model.addAttribute("flag2", false);
```

템플릿:

```html
<span th:text="${flag1} and ${flag2}"></span>    <!-- false -->
<span th:text="not ${flag2}"></span>            <!-- true -->
```

---

# 3. null을 넘기려면

null 자체를 넣으면 된다.

```java
model.addAttribute("data1", null);
```

템플릿에서는 null 체크 가능:

```html
<span th:text="${data1} == null"></span>         <!-- true -->
<span th:text="${data1} != null"></span>         <!-- false -->
```

---

# 4. 전체 예시 (정리된 컨트롤러 코드)

```java
@GetMapping("/operation")
public String operation(Model model) {

    // 숫자
    model.addAttribute("value1", 1);
    model.addAttribute("value2", 2);

    // 불린
    model.addAttribute("flagTrue", true);
    model.addAttribute("flagFalse", false);

    // null
    model.addAttribute("nullable", null);

    return "basic/operation";
}
```

---

# 5. 참고: 문자열로 넘어가면 어떻게 되느냐?

예를 들어

```java
model.addAttribute("value1", "1");
```

이렇게 문자열로 넘기면 타임리프는 변수 `${value1}`을 문자열로 인식하기 때문에

```html
<span th:text="${value1} + 2"></span>
```

는 연산이 아니라 문자열 덧붙이기로 처리할 가능성이 있으며, 자주 타입 오류나 변환 불가 오류가 발생한다.
따라서 숫자/불린/null은 모델에 원 타입 그대로 넣는 것이 정석이다.

---

필요하면 `operation.html` 템플릿도 예시로 만들어줄까?

두 페이지가 템플릿 오류로 인해 파싱 단계에서 막혀서 열리지 않는 상태다.
적어주신 코드 전체를 보면 각각 치명적인 문법 오류가 포함되어 있어서 타임리프가 HTML 템플릿 자체를 로딩하지 못한다.

아래에서 어떤 부분이 문제인지 정확하게 짚어줄게.

---

# 1. Literal.html 문제점

### 문제 1)

```html
<li><span th:text="hello"></span></li>
```

`hello`는 리터럴이 아니라 변수 이름으로 처리된다.
템플릿을 여는 것 자체는 가능하지만, hello 변수가 존재하지 않으면 오류가 발생한다.

문자 리터럴로 출력하려면 반드시 작은따옴표:

```html
<span th:text="'hello'"></span>
```

---

# 2. Operation.html 문제점

이 페이지에는 파싱을 막는 Syntax Error가 두 군데 있다.

---

## 문제 A) 불린 연산 기호가 잘못됨

```html
<li><span th:text="${bool1} & ${bool2}"></span></li>
<li><span th:text="${bool1} | ${bool2}"></span></li>
```

`&`, `|` 는 타임리프에서 사용 불가
타임리프/SpEL의 불린 연산은 다음 3개뿐이다:

* `and`
* `or`
* `not`

따라서 아래처럼 해야 파싱된다.

```html
<span th:text="${bool1} and ${bool2}"></span>
<span th:text="${bool1} or ${bool2}"></span>
```

---

## 문제 B) 가장 치명적 — 태그 문법 오류

작성된 코드:

```html
<li><span th:text="${null0}?'데이터 없음':'데이터 있음'"</span></li>
```

문제가 되는 부분:

1. `th:text="...")` 안의 큰따옴표가 닫히지 않았음
2. `</span>` 앞에 여는 큰따옴표가 들어가 HTML 구조가 깨짐

HTML 파싱 자체가 실패해서 템플릿이 열리지 않는 가장 큰 원인이다.

올바른 형태:

```html
<li><span th:text="${null0} ? '데이터 없음' : '데이터 있음'"></span></li>
```

---

# 3. 전체 수정된 템플릿

## Literal.html (정상 동작 버전)

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Literal</title>
</head>
<body>
<ul>
    <li><span th:text="'hello'"></span></li>
    <li><span th:text="'hello spring'"></span></li>
    <li><span th:text="'It''s good'"></span></li>
    <li><span th:text="10"></span></li>
    <li><span th:text="3.14"></span></li>
    <li><span th:text="true"></span></li>
    <li><span th:text="false"></span></li>
    <li><span th:text="null"></span></li>
    <li><span th:text="|Hello ${data}|"></span></li>
    <li><span th:text="'Hello ' + ${data}"></span></li>
</ul>
</body>
</html>
```

---

## Operation.html (정상 동작 버전)

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Operation</title>
</head>
<body>
<ul>
    <li><span th:text="${num1} + ${num2}"></span></li>
    <li><span th:text="${num1} == 1"></span></li>
    <li><span th:text="${num1} > 2"></span></li>
    <li><span th:text="(${num2} % 2 == 0) ? '짝수' : '홀수'"></span></li>
    <li><span th:text="${bool1} and ${bool2}"></span></li>
    <li><span th:text="${bool1} or ${bool2}"></span></li>
    <li><span th:text="${null0} ? '데이터 없음' : '데이터 있음'"></span></li>
</ul>
</body>
</html>
```

---

# 결론

두 페이지가 안 열린 이유는 다음 두 가지:

1. Operation.html 마지막 줄에서 HTML 태그 문법이 깨져 파싱 실패
2. and/or 대신 & , | 를 사용해 SpEL 문법 오류 발생

이 문제들을 수정하면 정상적으로 페이지가 열릴 것이다.

원하면 컨트롤러 코드까지 포함해서 전체 샘플 프로젝트 형태로 정리해줄게.

로그를 보면 오류 원인이 명확하다.

```
Could not parse as expression: "'It''s good'"
(template: "basic/literal" - line 11, col 15)
```

타임리프 3.1에서는
문자열 리터럴의 작은따옴표 이스케이프 방식이 이전 버전과 다르고, `'It''s good'` 형식이 더 이상 허용되지 않는다.

즉, 문자열 내부에 작은따옴표 두 개(`''`)를 넣는 기존 방식이 3.1에서는 Expression 구문 오류가 된다.

그래서 literal.html을 아무리 고쳐도 이 줄 하나 때문에 전체 템플릿 파싱이 실패하고 페이지가 열리지 않는다.

---

# 해결 방법 (확실한 방법)

작은따옴표가 포함된 문자열을 표현할 때,
타임리프 3.1에서는 다음 방식 중 하나를 사용해야 한다.

## 1. 리터럴 대체 구문 `| |` 사용

가장 안정적이고 권장되는 방식이다.

```html
<span th:text="|It's good|"></span>
```

이 방식은 작은따옴표를 특별하게 처리하지 않으므로 그대로 출력된다.

---

## 2. origiinal 작은따옴표 방식을 쓰려면 \ escaping

하지만 타임리프 3.1에서는 두 개의 작은따옴표를 쓰는 방식이 Expression parsing에서 깨져 오류가 발생하므로
가능한 한 아래 방식만 사용을 권장한다.

```html
<span th:text="'It\'s good'"></span>
```

(타임리프 3.1에서는 이 방식은 일부 조건에서 동작하나, HTML 템플릿 구조에 따라 인식이 달라지므로 가장 권장되지 않는다.)

---

# 결론: 이 줄만 바꾸면 페이지가 열린다

현재 문제 줄:

```html
<li><span th:text="'It''s good'"></span></li>
```

정상 작동 버전(권장):

```html
<li><span th:text="|It's good|"></span></li>
```

---

# 왜 이런 일이 발생했나?

타임리프 3.1부터 Expression parser가 강화되면서
`'It''s good'` 같은 중첩 작은따옴표 escape 방식이 일부 상황에서 구문 분석 오류를 일으킨다.

따라서 문자열 안에 작은따옴표가 들어갈 때 가장 안전한 방식은 `| |` 문자열 리터럴이다.

---

# 페이지가 열리지 않은 이유 정리

1. literal.html 내부에
   `" 'It''s good' "` 구문 오류 발생
2. 타임리프는 해당 템플릿 파싱 자체를 중단함
3. DispatcherServlet 렌더링 실패 → 페이지 안 열림

---

이제 이 줄만 수정하면 literal 페이지는 바로 정상 출력된다.

필요하면 literal.html 전체를 최신 버전 기준으로 안전하게 수정해드릴게.

타임리프에서 Elvis 연산자와 No-Operation(연산하지 않기)는
템플릿 표현식을 더 유연하고 안전하게 작성하기 위해 자주 쓰인다.
둘은 비슷한 상황에서 사용되지만 역할은 다르다.

아래에서 개념·사용법·주의점까지 정리해줄게.

---

# 1. Elvis 연산자 (Elvis Operator)

## 개념

스프링 EL(SpEL)이 지원하는 연산자로,
값이 null 또는 비어 있는 경우 대체 값을 사용하도록 처리하는 연산자다.

형식:

```
A ?: B
```

의미:

* A가 null 또는 빈 값이면 B를 사용
* A가 값이 있으면 A 사용

---

## 사용 예시

### 기본

```html
<span th:text="${data} ?: '기본값'"></span>
```

* data가 null이면 → "기본값"
* data에 값이 있으면 → data 값

---

### 객체 접근에도 사용

```html
<span th:text="${user.name} ?: '이름 없음'"></span>
```

### chaining

```html
<span th:text="${name} ?: ${nickname} ?: '이름 미상'"></span>
```

---

## 주의할 점

* Elvis는 undefined / null / 빈 문자열("") / false 등을 "비어있다"로 평가한다.
* 삼항 연산자(`condition ? a : b`)와는 다르다.
  Elvis는 조건식이 아니라 값을 평가한다.

---

# 2. No-Operation (`_`)

## 개념

타임리프에서 제공하는 특별한 기호로
표현식이 아무 것도 하지 않도록 만드는 목적의 리터럴이다.

즉, "값을 바꾸지 않고 그대로 둬라"는 의미.

형식:

```
th:xxx="${condition} ? 'newValue' : _"
```

여기서 `_`은 “현재 값 유지”라는 뜻이다.

---

## 사용 목적

1. 속성 값을 조건부로 적용하고 싶을 때
2. 조건이 false일 때 기존 HTML 값 그대로 유지하고 싶을 때
3. 속성을 완전히 제거하지 않고 원본을 남기고 싶을 때

---

## 사용 예시

### 1) 클래스를 조건이 참일 때만 변경

```html
<div class="box" th:classappend="${active} ? ' on' : _"></div>
```

active가 false면 원래 class="box" 그대로 유지된다.

---

### 2) href를 조건부로 적용

```html
<a href="#" th:href="${link != null} ? ${link} : _">링크</a>
```

link가 null이면 href="#" 원본 유지.

---

### 3) input value의 조건 적용

```html
<input type="text" th:value="${name} != null ? ${name} : _">
```

name이 null이면 기존 value는 변경되지 않는다.

---

## No-Operation과 사용하면 안 되는 상황

* 속성 자체를 제거하려면 `_`이 아니라 `null`을 사용해야 한다.
* 비어 있는 값을 넣고 싶다면 빈 문자열 `''`을 사용.

---

# 3. Elvis vs No-Operation 비교

| 기능    | Elvis (A ?: B)    | No-Operation (`_`)         |
| ----- | ----------------- | -------------------------- |
| 목적    | 값이 없으면 대체 값 사용    | 기존 HTML 속성 값을 그대로 유지       |
| 적용 위치 | 값 표현식 내부          | th:xxx 속성의 삼항 연산자에서만 의미 있음 |
| 사용 예  | `${name} ?: '무명'` | `${cond} ? 'on' : _`       |
| 결과    | B가 값으로 렌더됨        | 아무 것도 하지 않고 원본 유지          |

---

# 4. 실제 HTML 예제

```html
<div>
    <!-- Elvis -->
    <span th:text="${user.name} ?: '이름 없음'"></span>

    <!-- No-Operation -->
    <p class="base" th:classappend="${highlight} ? ' active' : _">내용</p>
</div>
```

highlight가 false면 class="base" 그대로 유지된다.

---

필요하면 Elvis + No-Operation을 함께 쓰는 복합 예제나
Elvis와 삼항 연산자의 차이도 정리해줄게.
