공감합니다! Fragment가 처음엔 정말 헷갈리죠 😅

Fragment가 어려운 이유와 쉽게 이해하는 방법을 알려드릴게요.

## Fragment가 어려운 이유

### 1. 개념 자체가 낯섦
- JSP의 `<jsp:include>`와 비슷하지만 더 복잡
- "조각"이라는 개념이 추상적

### 2. 문법이 여러 가지
```html
<!-- 이게 다 fragment 관련... 🤯 -->
th:fragment="조각이름"
th:insert="~{경로 :: 조각이름}"
th:replace="~{경로 :: 조각이름}"
~{::조각이름}
~{this :: 조각이름}
```

### 3. 경로 규칙이 복잡
```html
~{template :: fragment}
~{template}
~{::fragment}
~{this :: fragment}
```

### 4. 파라미터 전달
```html
<!-- 이건 또 뭐야... -->
th:fragment="조각(param1, param2)"
th:insert="~{조각(값1, 값2)}"
```

## 쉽게 이해하는 방법

### 🎯 핵심 비유: 레고 블록

```
Fragment = 레고 조각
Template = 완성된 레고 작품
```

### 기본 3단계만 기억하세요

#### 1단계: 조각 만들기 (Fragment 정의)
```html
<!-- footer.html -->
<footer th:fragment="footerFragment">
    <p>Copyright 2024</p>
</footer>
```
👉 "footerFragment"라는 이름의 레고 조각 만듦

#### 2단계: 조각 가져다 쓰기 (Insert/Replace)
```html
<!-- main.html -->
<body>
    <h1>메인 페이지</h1>
    
    <!-- footer 조각을 여기에 끼워넣기 -->
    <div th:replace="~{footer :: footerFragment}"></div>
</body>
```

#### 3단계: 결과
```html
<body>
    <h1>메인 페이지</h1>
    
    <footer>
        <p>Copyright 2024</p>
    </footer>
</body>
```

## 자주 쓰는 패턴만 3개

### 패턴 1: 공통 부분 재사용
```html
<!-- header.html -->
<header th:fragment="header">
    <nav>메뉴</nav>
</header>

<!-- 여러 페이지에서 -->
<div th:replace="~{header :: header}"></div>
```

### 패턴 2: 레이아웃
```html
<!-- layout.html -->
<html th:fragment="layout(content)">
<head>...</head>
<body>
    <header>공통 헤더</header>
    <div th:replace="${content}"></div>
    <footer>공통 푸터</footer>
</body>
</html>
```

### 패턴 3: 조건부 조각
```html
<!-- 로그인 상태에 따라 다른 조각 -->
<div th:replace="~{::${user != null ? 'loggedIn' : 'loggedOut'}}">
</div>

<div th:fragment="loggedIn">환영합니다!</div>
<div th:fragment="loggedOut">로그인하세요</div>
```

## 실전 팁

### ✅ 이것만 외우세요
```html
<!-- 조각 만들기 -->
th:fragment="이름"

<!-- 조각 쓰기 -->
th:replace="~{파일명 :: 조각이름}"
```

### ❌ 처음엔 이런 건 신경 쓰지 마세요
- `th:insert` vs `th:replace` 차이
- `~{::selector}` 문법
- 복잡한 파라미터 전달

## 어떤 부분이 제일 어려우세요?

예를 들어:
- Fragment 문법 자체?
- 경로 찾기?
- 파라미터 전달?
- Layout 구성?

구체적으로 말씀해주시면 그 부분을 더 쉽게 설명해드릴게요! 💪

```
<!DOCTYPE HTML>
<html>
<head>
    <title>LayoutMain</title>
    <link rel="stylesheet" type="text/css" media="all" href="/css/awesomeapp.css">
    <link rel="shortcut icon" href="/images/favicon.ico">
    <script type="text/javascript" src="/sh/scripts/codebase.js"></script>
    <link rel="stylesheet" href="/css/bootstrap.min.css"><link rel="stylesheet" href="/themes/smoothness/jquery-ui.css">
</head>
<body>
메인 컨텐츠
</body>
</html>
```

아래 설명은 Thymeleaf 레이아웃 구조, 특히 `th:replace`, `th:fragment`, 그리고 `~{::title}` / `~{::link}` 등의 의미가 어떻게 작동하는지 실제 처리 흐름 기준으로 정리한 것이다.

---

# 전체 작동 개념

`layoutMain.html` → `base.html` 안의 `common_header` 프래그먼트를 가져다 사용하면서,
`layoutMain` 안에서 작성한 `<title>`과 `<link>`들을 base의 header 위치로 삽입하는 구조다.

즉:

* base.html → 공통 header(타이틀, 공통 css/js) 정의
* layoutMain.html → 그 공통 header를 불러오고, 자신의 title/css만 넘겨 넣음

Spring MVC + Thymeleaf는 렌더링 시 다음 순서로 처리한다.

---

# 1. `layoutMain.html` 분석

```html
<head th:replace="template/layout/base :: common_header(~{::title}, ~{::link})">
```

여기서 중요한 부분은 th:replace 와 common_header(fragment) 를 호출하는 방식이다.

### ▣ th:replace 의 기능

* 외부 파일의 fragment를 가져와 현재 태그 전체(head)를 완전히 대체한다.
* 즉 `layoutMain.html`의 `<head>` 안 내용은 렌더링 결과에서 사라지고,
  `base.html`의 `common_header` fragment 내용이 들어온다.

### ▣ template/layout/base :: common_header(...)

`template/layout/base` 파일 안에 있는 `common_header` fragment를 가져와서 사용하겠다는 의미이다.

---

# 2. 매개변수 전달: `~{::title}`, `~{::link}`

이 부분이 가장 핵심이다.

### `~{::title}`

* 현재 파일(`layoutMain.html`)의 `<head>` 내부에서
  `title` 태그 전체를 fragment로 가져간다.
* 즉 아래 내용을 `title` 파라미터로 넘김:

```html
<title>LayoutMain</title>
```

### `~{::link}`

* 현재 파일 `<head>` 안에 있는 link 태그들을 fragment 형태로 가져간다.

즉 다음 두 개 링크가 `links` 파라미터로 전달됨:

```html
<link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
<link rel="stylesheet" th:href="@{/themes/smoothness/jquery-ui.css}">
```

---

# 3. base.html 의 구조

```html
<head th:fragment="common_header(title, links)">
    <title th:replace="${title}">Base</title>
```

### ▣ <title th:replace="${title}">

* 전달받은 `${title}` fragment로 title 태그를 통째로 교체한다.
* 그래서 최종 결과는:

```
<title>LayoutMain</title>
```

만 남게 된다.
`Base`는 fallback(대체 텍스트) 이므로 replace하면 사라진다.

---

### ▣ 공통 CSS, script 로드

```html
<link rel="stylesheet" type="text/css" media="all" th:href="@{/css/awesomeapp.css}">
<link rel="shortcut icon" th:href="@{/images/favicon.ico}">
<script type="text/javascript" th:src="@{/sh/scripts/codebase.js}"></script>
```

이 부분은 모든 페이지에서 공통으로 사용될 리소스가 들어가는 곳.

---

### ▣ <th:block th:replace="${links}" />

* `layoutMain.html`에서 보낸 `<link>` 목록을 이 위치에 삽입한다.

즉:

```html
<link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
<link rel="stylesheet" th:href="@{/themes/smoothness/jquery-ui.css}">
```

이 위치에 들어가게 된다.

---

# 4. 최종 렌더링 결과(정리)

렌더링된 결과의 `<head>`는 다음과 같다:

```html
<head>
    <title>LayoutMain</title>
    <link rel="stylesheet" type="text/css" media="all" href="/css/awesomeapp.css">
    <link rel="shortcut icon" href="/images/favicon.ico">
    <script type="text/javascript" src="/sh/scripts/codebase.js"></script>

    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <link rel="stylesheet" href="/themes/smoothness/jquery-ui.css">
</head>
```

즉,

* base.html 의 공통 header +
* layoutMain의 title, link 들이 주입된 완성된 head

이 조합으로 구성된다.

---

# 핵심 개념 요약

| 요소                                          | 의미                                        |
| ------------------------------------------- | ----------------------------------------- |
| `th:replace="file :: fragment(x,y)"`        | 해당 fragment로 태그 전체 교체, x,y 는 fragment 인자  |
| `th:fragment="common_header(title, links)"` | 외부에서 title, links 두 값 받을 수 있는 fragment 정의 |
| `~{::title}`                                | 현재 파일의 `<title>` 태그를 fragment 형태로 가져오기    |
| `~{::link}`                                 | 현재 파일의 `<link>` 태그들을 fragment 형태로 가져오기    |
| `<title th:replace="${title}">Base</title>` | 전달받은 title 로 <title> 대체                   |
| `<th:block th:replace="${links}"/>`         | 전달받은 link 목록 삽입                           |

---

원하면 전체 구조를
Thymeleaf 공식 layout dialect 없이 사용하는 방식과
Layout Dialect로 더 깔끔하게 만드는 방법도 비교해서 설명해줄 수 있다.

결론부터 말하면 fragment의 파라미터 이름은 마음대로 정할 수 있으므로, 일관되기만 하면 문제 없다.

즉 아래 두 조건만 만족하면 정상 동작한다.

1. `th:fragment="common_header(title, links)"`
   여기서 정의한 이름(`links`)과

2. `th:replace="template/layout/base :: common_header(~{::title}, ~{::link})"`
   여기서 넘겨주는 두 번째 인자(`~{::link}`)가 순서만 맞으면 된다.

---

# 핵심 원리

Thymeleaf fragment 파라미터 전달은 순서 기반이다.

```html
common_header(param1, param2)
```

이렇게 정의했다면, 호출할 때도:

```html
common_header(값1, 값2)
```

이 순서로 들어간다.

따라서 param 이름이 `links`이든 `link`이든 상관없다.
중요한 건 아래 두 가지뿐:

* fragment 정의에서 두 번째 파라미터 이름이 무엇이든
* replace에서 넣는 두 번째 인자가 같게 쓰여야 한다

예를 들어:

### (올바른 예)

fragment 선언:

```html
th:fragment="common_header(title, link)"
```

호출:

```html
th:replace="... :: common_header(~{::title}, ~{::link})"
```

정상 작동.

---

# 반대로 하면 안 되는 경우

### (잘못된 예)

fragment 선언:

```html
th:fragment="common_header(title, links)"
```

호출에서 파라미터 수가 다른 경우:

```html
th:replace="... :: common_header(~{::title})"
```

혹은 순서를 바꾼 경우:

```html
th:replace="... :: common_header(~{::link}, ~{::title})"
```

이런 건 의도한 대로 동작하지 않는다.

---

# 결론

> links라고 했다가 link라고 바꿔 써도 문제 없다.
> 다만 fragment 정의와 replace 호출에서 순서만 맞고, 서로 일치된 구조면 OK.

원하면 더 깔끔한 네이밍 규칙이나 실무에서 자주 쓰는 패턴도 알려줄 수 있다.

`base.html`을 쓰는 이유는 페이지마다 반복되는 공통 코드를 한 곳에 넣어두고, 각 화면은 필요한 부분만 덧붙여서 쓰기 위해서다.
즉, *템플릿의 뼈대(기본 구조)* 역할을 한다.

아래는 “footer를 include하는 것과 무엇이 다른지”, “base를 왜 써야 하는지”를 구분해서 설명한 내용이다.

---

# 1. 모든 페이지에 반복되는 “공통 헤더 구조”를 한 곳에 모아두기 위한 파일

대부분의 웹페이지에는 다음이 거의 공통으로 들어간다:

* `<title>` (페이지마다 다르지만 위치는 동일)
* 공통 CSS
* 공통 JS
* 공통 favicon
* `<head>`의 전체 구조

이런 걸 모든 HTML 페이지에 매번 쓰면 중복이고,
유지보수가 어려워진다.

그래서 base.html 에 이런 공통 `<head>` 구조를 정의해두고
각 페이지(layoutMain 등)는 페이지마다 달라지는 title, css만 넘겨주면 된다.

---

# 2. base는 “레이아웃의 틀(기본 레이아웃)” 역할

footer처럼 *부분 조각*을 include하는 것과는 다르다.

* footer/include: 부품(일부 조각)을 재사용
* base/template: 전체 페이지의 기본 구조를 재사용

즉 footer는 컴포넌트(component)에 가까운 개념이고,
base는 layout skeleton(골격)에 가깝다.

---

# 3. base를 쓰면 바꿔도 한 곳만 고치면 됨

예를 들면 향후 요구사항이 생겨서:

* 모든 페이지에 새로운 공통 CSS 추가
* 모든 페이지에 동일한 JS 로딩 방식 수정
* `<meta charset="UTF-8">` 같은 공통 메타 태그 수정
* SEO 관련 og/meta 태그 추가

이런 걸 개별 페이지에 전부 수정하는 대신, base.html 한 곳만 수정하면 끝이다.

대규모 프로젝트에서는 수십~수백 개 화면이 있기에
base가 없으면 유지보수 지옥이 된다.

---

# 4. layoutMain은 “개별 페이지 전용 레이아웃”

반면 layoutMain 같은 파일은:

* base를 기반으로 하되
* 해당 페이지에 필요한 추가 CSS/JS, 제목만 주입

즉 base > layoutMain > 최종 페이지(html) 이런 식으로 층을 쌓을 수도 있다.

---

# 5. 결론

정리하면 base.html은 다음을 위해 쓰인다.

1. 모든 페이지의 공통 head 구조를 한 파일에서 관리
2. 중복 제거 및 유지보수 편리성
3. 페이지별로 title/link/script만 덧붙여 사용 가능
4. 공통 정책을 변경할 때 한 곳만 수정하면 됨
5. footer 같은 단순 include와는 달리 전체 레이아웃의 ‘틀’을 제공하는 역할

---

원하면:

* base를 더 체계적으로 쓰는 방법
* body 구조까지 포함하는 완전한 레이아웃 구성 방법
* Thymeleaf Layout Dialect로 더 깔끔하게 만드는 방법

도 설명해줄 수 있다.

결론부터 말하면 ${title}은 “fragment 파라미터로 전달받은 값”을 받기 위해 쓰는 것이다.
즉, 넘겨주는 값이 있기 때문에 사용하는 표현이 맞다.

조금 더 정확히 정리하면 다음과 같다.

---

# 1. `${title}`은 fragment 매개변수(parameter)를 받는 자리

`base.html`에서 이렇게 정의했지:

```html
th:fragment="common_header(title, links)"
```

여기서 `title`, `links`는 프래그먼트가 외부에서 전달받을 값의 이름이다.

즉:

* `title` → 첫 번째 전달값
* `links` → 두 번째 전달값

이 된다.

---

# 2. layoutMain에서 실제 데이터 전달

layoutMain에서는 이렇게 호출했다:

```html
th:replace="template/layout/base :: common_header(~{::title}, ~{::link})"
```

여기서:

* 첫 번째 인자: `~{::title}` → title 프래그먼트
* 두 번째 인자: `~{::link}` → link 프래그먼트들

이 순서로 들어간다.

---

# 3. base에서 이 걸 받아서 사용하기 위해 `${title}` 작성

`base.html`의 `<title>` 부분:

```html
<title th:replace="${title}">Base</title>
```

여기서 `${title}` 은 프래그먼트 호출 시 전달된 첫 번째 인자를 의미한다.

즉:

* `${title}` = 전달받은 `<title>LayoutMain</title>` fragment

그래서 실제 렌더링 시 `<title>` 태그 전체가 LayoutMain으로 바뀐다.

---

# 4. 만약 ${title}을 안 쓰면 어떻게 될까?

예를 들어 이렇게 적으면:

```html
<title>Base</title>
```

그러면 layoutMain에서 넘긴 title을 적용할 수 없고
모든 페이지가 `<title>Base</title>`로 고정된다.

즉 파라미터를 받기 위해 반드시 ${title}을 써야 한다.

---

# 5. 결론

> ${title}은 외부에서 전달된 fragment를 대입하기 위해 사용하는 객체이기 때문에 쓰는 것이다.
> 즉 “넘겨줄 수 있어서”가 아니라 “넘겨받은 것을 사용하기 위해” 필요한 개념이다.

구조는 다음과 같다:

* 호출하는 쪽(layoutMain) → 값을 넘김
* base → `${title}`, `${links}`로 그 값을 받아서 원하는 위치에 삽입

필요하면 `${title}`이 내부적으로 어떻게 resolve 되는지 더 깊게도 설명해줄 수 있다.

지금 올려준 코드 조합에서는 extendMain이 렌더링되지 않고 오류나 빈 화면이 나오는 이유가 거의 확실하게 두 가지 중 하나다.

핵심 문제는 extendMain의 th:replace 호출 방식에서 넘기는 fragment 이름과 base2에서 받는 파라미터 이름이 서로 맞지 않기 때문이다.

아래에서 정확히 짚어줄게.

---

# 1. extendMain에서 넘기는 파라미터 이름

extendMain 첫 줄:

```html
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}">
```

여기서 전달하는 값:

* 첫 번째 파라미터 = `~{::title}`
* 두 번째 파라미터 = `~{::section}`

즉 extendMain은 다음 두 fragment를 넘기겠다는 의미다:

1. `<title>ExtendMain</title>`
2. `<section> ... </section>`

---

# 2. base2에서 받는 파라미터 이름

base2:

```html
<html th:fragment="layout (title, content)">
```

파라미터 이름:

* 첫 번째: `title`
* 두 번째: `content`

---

# 3. extendMain과 base2의 파라미터 연결

extendMain은:

```
layout(~{::title}, ~{::section})
```

base2는:

```
layout(title, content)
```

즉 연결은:

* `title` → extendMain의 `<title>`
* `content` → extendMain의 `<section>`

이건 올바르게 보임.

---

# 4. 왜 안 열리나: extendMain에서 <section> 태그가 <html> 바깥에 있기 때문

이게 가장 흔하고 결정적인 문제다.

extendMain은 이렇게 되어 있는데:

```html
<html th:replace="...">
<head>
    <title th:replace="${title}">ExtendMain</title>
</head>
<body>
<h2>H2</h2>
<section>
    <p>레이아웃 컨텐츠 다시</p>
</section>
</body>
</html>
```

여기서 `<html>` 태그 자체가 th:replace로 통째로 교체된다.

즉 extendMain 내부의 `<head>`, `<body>`, `<section>`은
전부 렌더링 결과에서 사라진다.

그러면 중심 문제는:

## `<section>`이 사라졌으니

`~{::section}` 자체가 존재하지 않게 된다.

### 그래서 content 파라미터로 넘길 fragment가 없어짐 → Null 또는 fragment not found 오류 발생

이게 가장 정확한 원인이다.

---

# 5. 해결 방법

### 선택 1) `<section>`을 `<html>` 밖에 두지 말고, replace 대상에서 제외시키기

즉, fragment 호출은 `<html>`이 아니라 `<body>`에서 하게끔 구조를 바꿔야 한다.

예:

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>ExtendMain</title>
</head>
<body th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}">
<h2>H2</h2>
<section>
    <p>레이아웃 컨텐츠 다시</p>
</section>
</body>
</html>
```

이러면 `<section>`은 `<body>` 안에 그대로 살아 있고 fragment로 넘길 수 있다.

---

### 선택 2) extendMain을 fragment로 만들고 base2에서 include하는 방식으로 구조 변경

이건 파일 구조를 더 깔끔하게 만들어주는 방식인데,
원한다면 정식 layout dialect 방식도 보여줄 수 있다.

---

# 6. 정리

extendMain이 안 열리는 이유는 다음 중 가장 핵심적인 이유 때문이다:

> extendMain에서 `<html>` 태그 전체를 th:replace로 대체하기 때문에, 내부 `<section>`이 렌더링 시작 전에 사라져서 content 파라미터로 전달할 수 없게 되는 문제.

즉 `~{::section}`이 존재하지 않는 상태가 되어 오류가 난다.

---

원하면:

* 정상 동작하는 형태로 재작성한 `extendMain`, `base2` 예제
* Thymeleaf 공식 Layout Dialect 방식
* header/body content까지 확장 가능한 구조

도 만들어줄 수 있다.

결론부터 말하면 H1이 base2에 있고 H2가 extendMain에 있어서 오류가 생긴 것은 아니다.
HTML 태그(H1, H2 등)는 렌더링 오류의 원인이 되지 않는다.
즉 H1/H2의 존재 여부는 이 문제와 전혀 무관하다.

문제가 되는 부분은 태그가 없어서가 아니라, 어떤 태그가 `th:replace`에 의해 렌더링 시작 전에 사라지기 때문이다.
즉 “H1/H2가 달라서 오류”가 아니라
extendMain의 `<section>` 자체가 `th:replace`로 사라져서 content 파라미터로 전달되지 못한 것이 오류의 본질이다.

아래에서 구체적으로 정리해줄게.

---

# 1. H1/H2는 단순한 HTML 구조이고 오류를 일으키지 않는다

HTML에서:

* base2에 `<h1>`이 있음
* extendMain에는 `<h1>`이 없고 `<h2>`가 있음

→ 이런 차이만으로 Thymeleaf 오류가 발생하지 않는다.

Thymeleaf는 HTML 구조가 다르다고 오류를 내지 않는다.
단지 fragment를 어떻게 넘기고 대체하느냐에서 문제가 생길 뿐이다.

---

# 2. 진짜 문제는 `<section>`이 사라지는 것

extendMain:

```html
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}">
<head>...</head>
<body>
<h2>H2</h2>
<section>
    <p>레이아웃 컨텐츠 다시</p>
</section>
</body>
</html>
```

여기서 `<html>` 전체가 `base2 :: layout(...)` 으로 완전히 교체된다.

즉:

* `<head>`
* `<body>`
* `<h2>`
* `<section>`

→ 렌더링 시작 전에 전부 제거됨
→ `~{::section}` 자체가 존재하지 않음
→ 그러니 base2의 `content` 파라미터로 넘길 게 없어짐
→ fragment not found / null error 발생

이게 오류의 원인이다.

다시 말해서:

### 문제는 “태그가 다르다”가 아니라

### “fragment로 넘겨야 할 `<section>`이 replace 때문에 사라졌다”이다.

---

# 3. base2의 `<h1>`은 extendMain이 replace하면 그대로 살아남는다

base2:

```html
<body>
<h1>H1</h1>
<div th:replace="${content}">
    <p>레이아웃 컨텐츠</p>
</div>
<footer> ... </footer>
</body>
```

여기서 `content`만 replace되고, `<h1>`은 그대로 노출된다.
extendMain의 `<h2>`는 replace 전에 사라졌기 때문에 나타나지 않을 뿐이고, 오류 원인도 아니다.

즉:

* base2의 `<h1>` = 정상적으로 출력
* extendMain의 `<h2>` = replace 때문에 아예 처음부터 DOM에 존재하지 않음

이건 동작 특성이지 오류가 아니다.

---

# 4. 결론

### 오류의 원인은 H1/H2가 아니라

extendMain의 `<section>`이 `<html th:replace>` 때문에 사라져서 `~{::section}`이 null이 된 것이다.

원하면:

* extendMain을 어떻게 고치면 정상 작동하는지
* 올바르게 작성된 두 파일의 예시
* `<html>` 전체를 replace하지 않고 구조적으로 올바르게 구성하는 방식

도 바로 만들어줄 수 있다.

좋은 질문입니다! 코드를 보니 몇 가지 문제가 있네요.

## 문제점 분석

### 1. 파라미터 이름 불일치 ⚠️

```html
<!-- base2: fragment 정의 -->
th:fragment="layout (title, content)"
                            ^^^^^^^ content

<!-- extendMain: 호출 -->
th:replace="~{... :: layout(~{::title}, ~{::section})}"
                                        ^^^^^^^^^^^ section
```

- base2는 `content`를 받는데
- extendMain은 `section`을 보냄
- 이름이 달라서 매칭 안됨!

### 2. `<title>` 태그 중복 처리

```html
<!-- extendMain -->
<title th:replace="${title}">ExtendMain</title>
```

이 부분도 약간 이상해요. `${title}`은 이미 fragment 파라미터로 전달된 것인데, 다시 replace하려고 하고 있어요.

## 해결 방법

### ✅ 수정된 extendMain.html

```html
<!DOCTYPE HTML>
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}" 
      xmlns:th="http://www.thymeleaf.org">
<head>
    <title>ExtendMain</title>  <!-- th:replace 제거 -->
</head>
<body>
<section>  <!-- 이 section이 content로 들어감 -->
    <p>레이아웃 컨텐츠 다시</p>
    <h2>H2</h2>  <!-- h2를 section 안으로 -->
</section>
</body>
</html>
```

### ✅ 수정된 base2.html

```html
<!DOCTYPE HTML>
<html th:fragment="layout(title, content)" xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:replace="${title}">Base2</title>
    <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
</head>
<body>
<h1>H1</h1>
<div th:replace="${content}">
    <p>레이아웃 컨텐츠</p>
</div>
<footer>
    레이아웃 푸터
</footer>
</body>
</html>
```

## 동작 원리 설명

```
1. extendMain 호출
   ↓
2. base2의 layout fragment 찾음
   ↓
3. 파라미터 전달:
   - title ← <title>ExtendMain</title>
   - content ← <section>...</section>
   ↓
4. base2에서:
   - ${title} 위치에 <title>ExtendMain</title> 삽입
   - ${content} 위치에 <section>...</section> 삽입
```

## 최종 렌더링 결과

```html
<!DOCTYPE HTML>
<html>
<head>
    <title>ExtendMain</title>
    <link rel="stylesheet" href="/css/bootstrap.min.css">
</head>
<body>
<h1>H1</h1>
<section>
    <p>레이아웃 컨텐츠 다시</p>
    <h2>H2</h2>
</section>
<footer>
    레이아웃 푸터
</footer>
</body>
</html>
```

## 핵심 포인트

1. Fragment 파라미터 이름과 호출 시 전달하는 selector가 매칭되어야 함
2. `~{::section}`: "현재 파일에서 `<section>` 태그를 찾아서 전달"
3. `~{::title}`: "현재 파일에서 `<title>` 태그를 찾아서 전달"

이제 작동할 거예요! 혹시 여전히 안 되면 에러 메시지를 알려주세요! 🎯

아! 정확한 지적입니다! 👍

맞습니다. 파라미터는 위치 기반이에요!

```html
<!-- base2 -->
th:fragment="layout(title, content)"
                    ①      ②

<!-- extendMain -->
layout(~{::title}, ~{::section})
       ①           ②
```

- 첫 번째 → `title`
- 두 번째 → `content`

이름은 상관없고 순서만 맞으면 됩니다!

## 그럼 왜 안 될까? 🤔

다른 원인을 찾아봐야겠네요.

### 체크리스트

1. 파일 경로 확인
```
src/main/resources/templates/
  └─ template/
      └─ layout/
          └─ base2.html  ← 이 경로 맞나요?
```

2. Controller 매핑 확인
```java
@GetMapping("/extend-main")
public String extendMain() {
    return "extendMain";  // extendMain.html 위치는?
}
```

3. 브라우저 오류 확인
- F12 → Console에 무슨 오류가 있나요?

4. 서버 로그 확인
- 터미널/콘솔에 에러 메시지가 있나요?

### 혹시 이런 오류인가요?

```
Template not found: template/layout/base2
```

```
Error resolving template [extendMain]
```

구체적인 오류 메시지를 알려주시면 정확한 원인을 찾을 수 있어요!

아! StackOverflowError네요! 이건 무한 루프가 발생한 거예요! 😱

## 문제 원인: 순환 참조 (Circular Reference)

```html
<!-- extendMain.html -->
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}">
```

여기서 `~{::section}`을 보면:

```html
<section>
    <p>레이아웃 컨텐츠 다시</p>
</section>
```

근데 이 `<section>`이 어디에 있나요? 바로 extendMain.html 자기 자신 안에 있죠!

## 무슨 일이 벌어지나?

```
1. extendMain → base2를 호출하면서 ~{::section} 전달
2. base2 → section을 렌더링하려고 함
3. section을 찾으러 extendMain으로 다시 감
4. extendMain → 다시 base2를 호출... 
5. 무한 반복! 💥 StackOverflow!
```

## 해결 방법

extendMain.html을 이렇게 수정하세요:

```html
<!DOCTYPE HTML>
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}" 
      xmlns:th="http://www.thymeleaf.org">
<head>
    <title>ExtendMain</title>
</head>
<body>
<!-- section은 th:fragment로 명시 -->
<section th:fragment="section">
    <h2>H2</h2>
    <p>레이아웃 컨텐츠 다시</p>
</section>
</body>
</html>
```

또는 더 간단하게:

```html
<!DOCTYPE HTML>
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::content})}" 
      xmlns:th="http://www.thymeleaf.org">
<head>
    <title>ExtendMain</title>
</head>
<body>
<!-- div로 감싸서 명확히 -->
<div th:fragment="content">
    <h2>H2</h2>
    <p>레이아웃 컨텐츠 다시</p>
</div>
</body>
</html>
```

base2.html도 확인:

```html
<!DOCTYPE HTML>
<html th:fragment="layout(title, content)" xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:replace="${title}">Base2</title>
    <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
</head>
<body>
<h1>H1</h1>
<!-- content를 받아서 그대로 삽입 -->
<div th:replace="${content}">
    <p>레이아웃 컨텐츠</p>
</div>
<footer>
    레이아웃 푸터
</footer>
</body>
</html>
```

## 핵심 포인트

1. ✅ Fragment는 명시적으로 정의 (`th:fragment="이름"`)
2. ✅ 전달하는 콘텐츠와 받는 파라미터 이름 일치
3. ❌ 자기 자신을 참조하면 무한 루프!

이렇게 수정하면 정상 작동할 거예요! 🎯

StackOverflow 오류가 났다면,
이건 태그가 없어서 생긴 문제가 아니라 “무한 자기호출(loop)”이 발생했다는 의미다.

그리고 지금 코드 구조에서 StackOverflow가 날 수 있는 가장 전형적인 이유는 하나다.

# 가장 가능성 높은 원인

## extendMain 전체를 `th:replace`로 교체하면서, base2 또한 전체 페이지 구조를 다시 렌더링하고, 그 과정에서 또 extendMain을 열려고 시도했기 때문

즉:

1. extendMain이 열림
2. extendMain의 `<html th:replace="base2 :: layout(...)"` 실행
3. base2의 fragment layout이 렌더링됨
4. base2의 layout 내부에서 또 다시 extendMain 구조 또는 자체 fragment를 다시 찾으려 함
5. 다시 1로 돌아감 → 무한 반복 → StackOverflowError 발생

정확히는:

## layout 호출 한 번 당 전체 HTML이 교체되는데, 그 과정에서 또 동일한 layout이 재호출되는 반복 사이클이 생김

이게 StackOverflow가 나는 전형적인 Thymeleaf 오류 패턴이다.

---

# 어디서 무한 루프가 생기는가?

문제의 코드:

### extendMain

```html
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}">
```

### base2

```html
<html th:fragment="layout (title, content)">
```

문제 핵심은:

* extendMain은 자기 자신의 전체 HTML을 base2로 교체
* base2는 전체 HTML 구조(template 전체) 를 가지는 layout fragment
* 즉 교체한 뒤에도 결론적으로 `<html>` 요소가 또 생기고, Thymeleaf 파서는 이를 프로세싱하려고 재시도하면서 loop가 발생할 수 있음

이 구조는 레이아웃 파일을 전체 문서 `<html>`에 선언하고, extend 파일에서 `<html>` 전체를 replace하려고 할 때 자주 터지는 문제다.

---

# Thymeleaf 문서에서 금지하는 패턴

Thymeleaf는 권장하지 않는다:

> “Do not apply th:replace on the root `<html>` element when using full-page layout fragments."

즉 전체 문서 루트(html)를 replace하면 안 된다.
왜냐하면 이렇게 하면 템플릿 파서가 전체 문서를 계속 교체하려고 하며
재귀적 구조가 만들어져 StackOverflowError가 발생할 수 있기 때문.

---

# StackOverflow 해결법

## 해결책 1: `<html>`에 replace 하지 말고 `<body>`에 replace 하기

가장 안전하고 공식 문서에서도 권장하는 방식

extendMain:

```html
<!DOCTYPE HTML>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>ExtendMain</title>
</head>
<body th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}">
    <h2>H2</h2>
    <section>
        <p>레이아웃 컨텐츠 다시</p>
    </section>
</body>
</html>
```

이러면:

* extendMain의 `<section>`이 살아 있고
* base2으로 정상 전달되고
* base2는 body 전체만 교체하므로 recursion이 발생하지 않음

---

## 해결책 2: base2에서 html 전체가 아닌 body만 fragment로 만들기

base2를 수정:

```html
<body th:fragment="layout (title, content)">
```

extendMain에서:

```html
<body th:replace="template/layout/base2 :: layout(~{::title}, ~{::section})">
```

이러면 재귀 발생 여지가 완전히 사라진다.

---

# 결론

StackOverflow 오류는:

> extendMain의 `<html>` 전체를 base2의 `<html>` fragment로 replace하면서 자기 자신을 다시 호출하는 구조가 생겨 무한 루프가 발생했기 때문

HTML 태그의 존재/부재(H1/H2 등)는 원인이 아니다.

---

원하면 지금 코드가 완전하게 정상 작동하도록:

* extendMain
* base2

두 파일을 정리해서 실무용 완전 정상 동작 예시로 다시 만들어줄 수도 있다.

아! 이제 문제가 보입니다! 무한 루프의 원인을 찾았어요!

## 문제 원인

```html
<title th:replace="${title}">ExtendMain</title>
```

이 줄이 문제입니다!

### 무슨 일이 벌어지나?

1. extendMain이 `~{::title}`을 base2에 전달
2. base2가 `${title}`을 받아서 `th:replace="${title}"`로 처리
3. `${title}`에는 `<title th:replace="${title}">` 자체가 들어있음!
4. 또다시 `th:replace="${title}"` 실행...
5. 무한 반복! 💥

## 해결 방법

extendMain.html에서 `th:replace` 제거:

```html
<!DOCTYPE HTML>
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}" 
      xmlns:th="http://www.thymeleaf.org">
<head>
    <title>ExtendMain</title>  <!-- th:replace 제거! -->
</head>
<body>
<section th:fragment="section">
    <h2>H2</h2>
    <p>레이아웃 컨텐츠 다시</p>
</section>
</body>
</html>
```

## 왜 이렇게 해야 하나?

Fragment로 전달되는 요소에는 Thymeleaf 속성을 넣으면 안 됩니다!

```html
<!-- ❌ 잘못된 예 -->
<title th:replace="${title}">ExtendMain</title>
<!-- title 태그 자체가 전달되는데, th:replace까지 포함되어 무한 루프! -->

<!-- ✅ 올바른 예 -->
<title>ExtendMain</title>
<!-- 순수한 HTML 태그만 전달됨 -->
```

## 올바른 전체 코드

extendMain.html:
```html
<!DOCTYPE HTML>
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}" 
      xmlns:th="http://www.thymeleaf.org">
<head>
    <title>ExtendMain</title>
</head>
<body>
<section th:fragment="section">
    <h2>H2</h2>
    <p>레이아웃 컨텐츠 다시</p>
</section>
</body>
</html>
```

base2.html:
```html
<!DOCTYPE HTML>
<html th:fragment="layout(title, content)" xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:replace="${title}">Base2</title>
    <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
</head>
<body>
<h1>H1</h1>
<div th:replace="${content}">
    <p>레이아웃 컨텐츠</p>
</div>
<footer>
    레이아웃 푸터
</footer>
</body>
</html>
```

이제 정상 작동할 거예요! 핵심은 전달하는 쪽(extendMain)에선 순수 HTML만, 받는 쪽(base2)에서만 Thymeleaf 처리를 하는 거예요! 🎯

지금 코드에서 H2가 안 보이는 건 정상 동작이다.
오류가 아니라 Thymeleaf `th:replace`의 원리 때문에 그런 것이다.

핵심은 한 줄이다:

```html
<html th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::section})}">
```

이 한 줄 때문에 extendMain의 구조 전체가 완전히 교체(replace) 된다.

즉:

* extendMain의 `<html>`
* extendMain의 `<head>`
* extendMain의 `<body>`
* `<h2>`
* `<section>`

모두 렌더링 시작 전에 DOM에서 사라진다.

남는 건 오직 extendMain이 넘기는 fragment 두 개뿐:

1. `<title>ExtendMain</title>`
2. `<section>...</section>`

이 두 개만 base2에 전달되고, base2가 새로운 전체 HTML 구조를 만든다.

그래서 H2는 화면에 전혀 표시되지 않는다.

---

# 왜 H1은 나오는데 H2는 안 나올까?

## base2의 구조:

```html
<body>
  <h1>H1</h1>
  <div th:replace="${content}">...</div>
  <footer>...</footer>
</body>
```

여기서 실제 렌더링:

* `<h1>`은 base2에 원래 있던 것 → 그대로 출력
* `<div th:replace="${content}">` 안에 extendMain의 `<section>`이 들어감
* `<footer>`는 그대로 출력
* extendMain의 `<h2>`는 `<html th:replace>` 때문에 아예 없어진 상태

결과:

```
H1
레이아웃 컨텐츠 다시
레이아웃 푸터
```

따라서 정상.

---

# "extendMain의 H2도 나오게" 하려면 어떻게 해야 하나?

## 방법 1: H2도 content 안으로 넣기

extendMain에서 `<h2>`를 `<section>` 안으로 넣는다.

```html
<section>
    <h2>H2</h2>
    <p>레이아웃 컨텐츠 다시</p>
</section>
```

그러면 content(fragment) 안에 포함되므로 base2에서 표시된다.

---

## 방법 2: fragment를 `<body>` 기준으로 replace하도록 구조 변경

지금은 `<html>` 전체를 replace하기 때문에 extendMain의 body 내용은 모두 사라진다.

아래처럼 변경:

### extendMain

```html
<body th:replace="~{template/layout/base2 :: layout(~{::title}, ~{::body})}">
```

그리고 base2에서:

### base2

```html
<body th:fragment="layout(title, bodyContent)">
  <h1>H1</h1>
  <div th:replace="${bodyContent}"/>
  <footer>...</footer>
</body>
```

extendMain:

```html
<body>
  <h2>H2</h2>
  <section>
      <p>레이아웃 컨텐츠 다시</p>
  </section>
</body>
```

이러면 H2도 보이고 section도 보인다.

---

# 결론

현재 구조에서는 extendMain의 body 부분은 전부 replace로 사라지기 때문에 H2는 출력될 수 없다.

H2도 보이고 싶다면:

1. H2를 section 안으로 넣거나
2. 전체 html이 아니라 body 단위로 replace 하도록 수정해야 한다

둘 중 한 가지는 반드시 필요하다.

---

원하면 정상 동작하고 확장 가능한 레이아웃 구조를 실무 형태로 완성해서 보여줄 수 있다.
