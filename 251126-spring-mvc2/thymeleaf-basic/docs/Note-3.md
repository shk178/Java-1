좋아요! 아주 기초부터 차근차근 설명해드릴게요.

## 1. HTML이 뭐야?

HTML은 웹페이지를 만드는 언어예요. 마치 레고 블록처럼 여러 요소들을 조합해서 화면을 만듭니다.

```html
<input type="text" />
```
이건 입력창 하나를 만드는 코드예요.

## 2. 속성(Attribute)이 뭐야?

속성은 HTML 요소에 추가 정보를 알려주는 거예요.

```html
<input type="text" name="username" />
```

- `type="text"` : 이 입력창은 텍스트를 입력하는 창이야
- `name="username"` : 이 입력창의 이름은 "username"이야

마치 사람의 특징처럼 생각하면 돼요:
- 이름: 홍길동
- 나이: 20살
- 키: 175cm

## 3. 주요 속성들

### type (타입)
입력창의 종류를 정해요.

```html
<input type="text" />      <!-- 글자 입력창 -->
<input type="checkbox" />  <!-- 체크박스 (V 표시) -->
<input type="password" />  <!-- 비밀번호 입력창 (●●●로 보임) -->
```

### name (이름)
입력창의 이름표예요. 나중에 이 데이터를 찾을 때 사용해요.

```html
<input type="text" name="email" />
```
이건 "이메일을 입력하는 창"이라는 의미예요.

### class (클래스)
CSS로 꾸미기 위한 분류예요.

```html
<input type="text" class="big" />
```
"big"이라는 클래스에 "글자 크게, 파란색"이라는 스타일을 적용할 수 있어요.

### checked (체크됨)
체크박스가 이미 선택된 상태인지 알려줘요.

```html
<input type="checkbox" checked />  <!-- V 표시되어 있음 -->
<input type="checkbox" />          <!-- 빈 체크박스 -->
```

## 4. Thymeleaf가 뭐야?

Thymeleaf는 HTML을 자동으로 바꿔주는 도구예요.

예를 들어:
- 로그인한 사용자 이름을 자동으로 넣어주기
- 조건에 따라 체크박스를 체크하거나 안 하거나
- 클래스 이름을 동적으로 추가하기

`th:`로 시작하는 속성들이 Thymeleaf 명령어예요.

## 5. 예제를 쉽게 설명

### 예제 1: 이름 바꾸기
```html
<input type="text" name="mock" th:name="A" />
```

처음: name이 "mock"이에요.
Thymeleaf 실행 후: name이 "A"로 바뀌어요.

마치 이름표를 "mock"에서 "A"로 교체한 것과 같아요.

### 예제 2: 클래스 추가하기 (뒤에)
```html
<input type="text" class="text" th:attrappend="class=' large'" />
```

처음: class가 "text"예요.
Thymeleaf 실행 후: class가 "text large"가 돼요.

마치 이름표에 별명을 추가한 것처럼: "철수" → "철수 반장"

### 예제 3: 클래스 추가하기 (앞에)
```html
<input type="text" class="text" th:attrprepend="class='large '" />
```

처음: class가 "text"예요.
Thymeleaf 실행 후: class가 "large text"가 돼요.

순서만 반대예요: "철수" → "반장 철수"

### 예제 4: 체크박스 체크하기
```html
<input type="checkbox" th:checked="true" />
```

결과: ☑ (체크된 상태)

```html
<input type="checkbox" th:checked="false" />
```

결과: ☐ (빈 상태)

### 예제 5: HTML의 함정!
```html
<input type="checkbox" checked="false" />
```

이건 체크된 상태로 나와요! ☑

왜냐하면 HTML은 `checked`라는 단어만 있으면 체크되었다고 생각하거든요.
`false`라는 값은 무시돼요.

## 정리

- 속성: HTML 요소의 특징을 설명하는 것
- type: 입력창 종류 (text, checkbox 등)
- name: 입력창의 이름
- class: 꾸미기 위한 분류
- checked: 체크박스가 선택되어 있는지
- th:로 시작하는 것들: Thymeleaf가 HTML을 자동으로 바꾸는 명령어

이해가 되셨나요? 더 궁금한 부분이 있으면 말씀해주세요! 😊

이 HTML 코드는 Thymeleaf 템플릿 엔진을 사용한 예제입니다. 각 부분을 자세히 설명해드릴게요.

## 기본 HTML 속성 개념

먼저 기본적인 HTML 속성들을 설명하면:

- type: input 요소의 종류 (text=텍스트 입력, checkbox=체크박스)
- name: 폼 데이터를 서버로 전송할 때 사용하는 식별자
- class: CSS 스타일을 적용하기 위한 클래스명
- checked: 체크박스가 선택되어 있는지 여부

## 각 코드 설명

### 1. `th:name` - 속성 교체
```html
<input type="text" name="mock" th:name="A" />
```
- 원래 `name="mock"`이지만, Thymeleaf가 실행되면 `name="A"`로 완전히 교체됩니다
- 결과: `<input type="text" name="A" />`

### 2. `th:attrappend` - 속성 값 뒤에 추가
```html
<input type="text" class="text" th:attrappend="class=' large'" />
```
- 기존 class 값 뒤에 공백과 함께 추가됩니다
- 결과: `<input type="text" class="text large" />`

### 3. `th:attrprepend` - 속성 값 앞에 추가
```html
<input type="text" class="text" th:attrprepend="class='large '" />
```
- 기존 class 값 앞에 추가됩니다 (공백 직접 포함 필요)
- 결과: `<input type="text" class="large text" />`

### 4. `th:classappend` - 클래스 추가 (권장)
```html
<input type="text" class="text" th:classappend="large" />
```
- class 전용 추가 명령어로 자동으로 공백 처리됩니다
- 결과: `<input type="text" class="text large" />`
- `th:attrappend`보다 편리합니다

### 5. `th:checked="true"` - 체크됨
```html
<input type="checkbox" name="active" th:checked="true" />
```
- 조건이 true이므로 checked 속성이 추가됩니다
- 결과: `<input type="checkbox" name="active" checked="checked" />`

### 6. `th:checked="false"` - 체크 안 됨
```html
<input type="checkbox" name="active" th:checked="false" />
```
- 조건이 false이므로 checked 속성이 제거됩니다
- 결과: `<input type="checkbox" name="active" />`

### 7. `checked="false"` - 주의! 항상 체크됨
```html
<input type="checkbox" name="active" checked="false" />
```
- HTML에서는 checked 속성이 존재하기만 하면 체크됩니다
- `checked="false"`도 체크된 상태로 표시됨 (일반 HTML의 함정)
- 체크 해제하려면 checked 속성 자체를 제거해야 함

## 요약

- th:name, th:class 등: 속성을 완전히 교체
- th:attrappend/attrprepend: 기존 값에 추가
- th:classappend: 클래스 추가 전용 (가장 편리)
- th:checked: boolean 조건에 따라 checked 속성 추가/제거
- 일반 HTML의 checked="false"는 체크된 것으로 인식됨

도움이 되셨나요? 더 궁금한 부분이 있으면 말씀해주세요!

아래 코드는 타임리프 태그 속성을 사용해 HTML 입력 요소(input)의

* 타입(type)
* 이름(name)
* 클래스(class)
* 기타 속성(checked 등)

을 동적으로 변경하거나 추가하는 예시들이다.
각 줄이 의미하는 것과 최종적으로 어떤 HTML이 생성되는지 하나씩 정확히 설명해줄게.

---

# 1. `<input type="text" name="mock" th:name="A" />`

## 의미

* 원래 HTML: name="mock"
* 타임리프: `th:name="A"` → 렌더링 시 name 속성을 "A"로 교체

## 결과

```html
<input type="text" name="A" />
```

즉, th:name은 name 속성을 동적으로 바꾸는 역할이다.

---

# 2. `<input type="text" class="text" th:attrappend="class='large'" />`

## 의미

* th:attrappend는 기존 속성에 값을 뒤에 붙이는 방식
* 여기서는 class 속성에 `large`를 뒤에 추가한다.

## 결과

```html
<input type="text" class="textlarge" />
```

하지만 이 형태는 문제를 일으킬 수 있다.
class는 공백으로 구분해야 하기 때문에 원래는 이렇게 써야 한다:

```html
th:attrappend="class=' large'"
```

그래야 아래처럼 나온다:

```html
<input type="text" class="text large" />
```

---

# 3. `<input type="text" class="text" th:attrprepend="class='large'" />`

## 의미

* th:attrprepend는 기존 속성 값 앞에 붙임

## 잘못된 형식

지금처럼 공백이 없으면 결과는

```html
class="largetext"
```

이 된다.

## 올바른 사용

```html
th:attrprepend="class='large '"
```

→

```html
<input type="text" class="large text" />
```

---

# 4. `<input type="text" class="text" th:classappend="large" />`

## 의미

* `th:classappend`는 class 속성 전용의 append 기능
* 자동으로 공백 처리가 되기 때문에 가장 안전함

## 결과

```html
<input type="text" class="text large" />
```

---

# 5. `<input type="checkbox" name="active" th:checked="true" />`

## 의미

* `th:checked="true"` → 체크된 상태로 렌더링됨

## 결과

```html
<input type="checkbox" name="active" checked="checked" />
```

또는

```html
<input type="checkbox" name="active" checked />
```

(HTML5에서는 둘 다 동일)

---

# 6. `<input type="checkbox" name="active" th:checked="flase" />`

## 문제점

* `flase`는 오타
* 타임리프는 `"flase"`를 String으로 보고 true/false로 변환할 수 없다고 판단하여
  체크되지 않은 상태(None)로 처리하거나 에러를 낼 수 있다.

## 결과(정상적인 경우)

* checked 없음

```html
<input type="checkbox" name="active" />
```

---

# 7. `<input type="checkbox" name="active" checked="flase" />`

## 의미

* HTML에서는 checked 속성은 값을 보지 않는다

    * checked="anything"
    * checked=""
    * checked
      전부 동일하게 체크된다.

즉, `checked="flase"`여도
→ 체크됨

## 결과

```html
<input type="checkbox" name="active" checked="flase" />
```

렌더링 시에도 그대로 남고, 브라우저는 체크된 것으로 판단한다.

---

# 정리표

| 원래 코드                            | 속성      | 타임리프 동작             | 최종 결과              |
| -------------------------------- | ------- | ------------------- | ------------------ |
| `th:name="A"`                    | name    | name을 A로 교체         | name="A"           |
| `th:attrappend="class='large'"`  | class   | class 뒤에 literal 추가 | textlarge (공백 미포함) |
| `th:attrprepend="class='large'"` | class   | class 앞에 literal 추가 | largetext (공백 미포함) |
| `th:classappend="large"`         | class   | 자동 공백 추가 후 append   | text large         |
| `th:checked="true"`              | checked | 체크됨                 | checked            |
| `th:checked="flase"`             | checked | boolean 아님 → 체크 안됨  | 빈 checked          |
| `checked="flase"`                | checked | HTML 표준: 값 무시       | 체크됨                |

---

# 결론

* `th:*`는 타임리프가 렌더링 시 HTML 속성을 동적으로 조작하는 기능이다.
* type은 HTML input의 종류
* name은 서버 전송 시 변수 이름
* class는 CSS 적용용
* checked는 체크박스의 UI 상태
* 특히 class 조작은 `th:classappend`가 가장 안정적이며 권장된다.

필요하다면 렌더링 전후의 전체 HTML도 예시로 만들어줄게.

HTML에서 `<input>`은 본래 입력 박스, 체크박스, 라디오 버튼, hidden 필드 등을 만들기 위한 태그라서
지금 코드처럼 `<input>` 태그에 `th:name`, `th:classappend`, `th:checked` 등을 적용하는 것은 정상적이고 완전히 맞는 사용 방식이다.

다만, 당신이 느끼는 불편함의 핵심은 “이 코드가 무슨 UI를 만들기 위해 쓰였는가?”일 가능성이 높다.
그래서 아래에서 어떤 경우에 이런 `<input>` + `th:*` 조합이 의미가 있는지 설명해줄게.

---

# 1. 이 코드는 “입력 박스(텍스트 필드)” + “체크박스” UI를 만드는 것이 맞다

예를 들면:

### 텍스트 입력 박스

```html
<input type="text" name="A" />
```

사용자에게 글자를 입력받는 박스.

---

### CSS class를 붙인 텍스트 입력 박스

```html
<input type="text" class="text large" />
```

`large`라는 CSS 클래스가 추가된 박스.

---

### 체크박스

```html
<input type="checkbox" name="active" checked />
```

체크 여부를 조절하는 UI.

---

즉,

* 입력 박스(텍스트)
* 체크박스

이 두 종류의 UI를 보여주는 HTML이 맞다.
이게 <input> 태그의 본래 역할이기 때문에 정상 동작이다.

---

# 2. 그럼 이 `<input>` 코드들의 목적은 무엇인가?

이 코드들은 Thymeleaf attribute modifier 동작을 연습하거나 테스트할 때 자주 사용하는 예시들이다.

즉, 이 코드는 UI를 완성하려는 목적보다는:

* th:name → name 속성 바뀨는지 확인
* th:attrappend → class 뒤에 값 붙는지 확인
* th:attrprepend → class 앞에 값 붙는지 확인
* th:classappend → class append 정상 동작 확인
* th:checked → boolean 조건에 따라 체크박스 on/off 되는지 연습

이런 “속성 조작 기능을 실습하는 예제”다.

그래서 input만 주로 쓰는 게 일반적이다.
input은 태그 구조가 단순하고 눈에 잘 보이는 변화만 있어서 타임리프 속성 실습용으로 최고의 대상이다.

---

# 3. 일반적인 실전에서는 input뿐 아니라 다른 태그에도 쓴다

예를 들어:

### div 태그 클래스 동적 변경

```html
<div class="menu" th:classappend="${active} ? ' on' : _"></div>
```

### a 태그 href 동적 변경

```html
<a th:href="@{/item/{id}(id=${item.id})}">보기</a>
```

### img 태그 src 동적 설정

```html
<img th:src="@{/images/${fileName}}" />
```

### span 텍스트 표현

```html
<span th:text="${user.name}"></span>
```

즉, `<input>`만 쓰는 것이 아니라 HTML 전체에서 원하는 태그에 `th:*`를 붙인다.

---

# 4. 결론

1. `<input>`만 나오더라도 코드는 100% 정상이다.
2. 지금 코드는 Thymeleaf 속성 변경 기능을 연습하는 예제일 가능성이 높다.
3. 실전에서는 input뿐 아니라 div, span, a, img 등 다양한 태그에서 사용한다.

원하면 각 속성을 다른 HTML 태그에서 사용한 예제도 만들어줄게.

타임리프에서 `th:name`, `th:classappend`, `th:checked` 같은 속성들은
HTML 태그를 출력하는 용도가 아니라, 태그의 속성을 "동적으로 조작"하는 기능이기 때문에
태그 자체를 출력할 필요는 없다.

즉, 다음 두 가지는 완전히 다른 개념이다.

---

# 1. 태그 자체를 출력한다

→ HTML 태그 문자열 자체를 화면에 보여주는 것
→ 예: `<input type="text">`를 화면에 텍스트로 보여주고 싶다

이런 경우는 `th:text`, `th:utext` 등을 사용한다.

```html
<span th:text="'<input type=\'text\''>"></span>   <!-- 태그 문자열을 화면에 표시 -->
```

---

# 2. 태그를 “그려서” 화면에 렌더링한다

→ HTML 태그가 실제 UI 요소로 브라우저에 생성되는 것
→ 예: 진짜 입력박스(텍스트 필드)가 보이고, 사용자가 글을 입력할 수 있음

```html
<input type="text" th:name="A">
```

이 경우 `<input>` 태그는 화면에 출력되지만
"태그 문자열" 형태가 아니라
실제 입력창 UI 형태로 나타난다.

---

# 결론: input 테스트 예제는 “태그 문자열을 보여주는” 게 아니라 “태그 UI를 보여주는” 예제다

즉,

```html
<input type="text" class="text" th:classappend="large">
```

이 코드는

* 태그 자체를 화면에 출력하려는 것이 아니라
* class 속성을 동적으로 바꾼 결과 UI 요소를 브라우저에 보여주기 위한 코드

따라서 실제로 브라우저에서는 “텍스트 입력 박스” 하나 보이는 것이 정상이다.

---

# 왜 `<input>`이 예제로 자주 쓰일까?

타임리프 속성 테스트에서는 `<input>`을 자주 사용하는데 이유는:

1. 태그 구조가 단순하고 짧음
2. 속성을 조작하면 UI 변화가 즉시 확인 가능
3. 텍스트 입력 박스·체크박스 등, 동적 속성 변화 테스트에 최적

그래서 강의나 책에서도 `<input>`으로 예제를 설명하는 경우가 많다.

---

# 만약 태그 자체를 “문자 그대로” 출력하고 싶다면?

다음처럼 해야 한다.

## 단순 텍스트 출력 (escape 처리됨)

```html
<span th:text="'<input type=\'text\' />'"></span>
```

화면 결과:

```
<input type='text' />
```

## 태그가 실제로 렌더링되도록 하고 싶다면 (escape 제거)

```html
<span th:utext="'<input type=\'text\' />'"></span>
```

브라우저 결과:
실제 input 박스가 하나 생김.

---

# 핵심 요약

* 당신이 작성한 코드는 태그를 “출력(문자열)”하려는 것이 아니라 “렌더링(UI)”하려는 코드임
* `<input>`이 화면에 보이는 건 정상
* 태그 자체를 텍스트로 보고 싶다면 `th:text` 또는 `th:utext`를 사용해야 한다

원하면 “태그 문자열 출력 vs 태그 렌더링”을 비교하는 작은 예제 파일도 작성해줄게.

정확히 말하면 large라는 CSS 클래스가 태그에 추가되었고, 브라우저는 그 추가된 class를 기준으로 스타일을 적용할 준비가 되었다는 뜻이다.
즉, “large가 적용됐다”는 말은 large라는 클래스가 HTML 요소에 붙었다는 의미지,
CSS 스타일이 실제로 화면에 변화가 생겼다는 의미는 아니다.

두 가지가 다르다는 점이 중요하다.

---

# 1. large가 “적용”되었다는 의미

타임리프에서 아래 코드:

```html
<input type="text" class="text" th:classappend="large">
```

렌더링 후 HTML:

```html
<input type="text" class="text large">
```

여기서 “large가 적용되었다”는 말은

* class 속성 값에 large가 포함되었다
* 브라우저는 이 요소가 large 클래스를 가진 것으로 인식한다
* CSS에서 `.large { ... }` 스타일을 정의해두었다면 그 스타일이 적용될 수 있다

라는 의미다.

---

# 2. 하지만 화면에 변화가 없을 수도 있다

large라는 CSS 클래스가 정의되어 있지 않으면, 화면에는 아무 변화가 없다.

예:

```css
.large {
    font-size: 30px;
}
```

이런 CSS가 없으면 아무 변화 없음.

---

# 3. 직접 확인해보는 방법

## HTML 출력에서 class를 확인

렌더링된 화면에서 우클릭 → “검사”

그러면 이런 식으로 보이면 정상:

```html
<input type="text" class="text large">
```

여기서 large가 붙어있으면 타임리프 동작은 성공이다.

---

## CSS가 없다면 화면은 변화 없음

large는 값을 붙였을 뿐, 실제 스타일은 CSS 코드가 있어야 나타난다.

---

# 4. 따라서 정리하면

large가 적용되었다 = class 속성에 large가 포함되었다는 뜻.

하지만

large라는 클래스에 대한 CSS가 없으면 UI 변화는 없다.

---

원하면
CSS에서 large 클래스를 실제로 눈에 보이게 적용하는 예제도 만들어줄게.

결론부터 말하면, attrappend와 classappend는 다르다.
특히 class 속성을 다룰 때는 거의 항상 classappend를 써야 하고,
attrappend로 class를 다루면 원하는 결과가 안 나올 수 있다.

아래에서 정확하게 차이를 정리해줄게.

---

# 1. attrappend

### “일반 HTML 속성에 문자열을 *그대로 뒤에 붙이는* 기능”

형식:

```html
th:attrappend="속성이름='값'"
```

예:

```html
<input class="text" th:attrappend="class='large'" />
```

이렇게 하면 렌더링 시:

```html
<input class="textlarge" />
```

이유는:

* attrappend는 문자열을 그대로 이어붙임
* class와 class 사이의 공백을 자동으로 처리하지 않음
* 그 결과 `"text" + "large" = "textlarge"`

즉, CSS 클래스가 두 개 생기는 것이 아니라
`textlarge`라는 하나의 클래스가 생겨버린다.

그래서 class 속성에는 사실상 사용하면 안 된다.

---

# 2. classappend

### “class 속성 전용의 append 기능이며 공백 처리 자동 제공”

형식:

```html
th:classappend="클래스명"
```

예:

```html
<input class="text" th:classappend="large" />
```

렌더링:

```html
<input class="text large" />
```

classappend는 class 속성 전용 기능이기 때문에

* 선행 공백/후행 공백을 알아서 처리
* class 여러 개를 안전하게 합쳐줌
* CSS class 규칙에 맞는 형태로 완성함

---

# 3. 왜 둘이 다르게 동작해야 하는가?

* HTML `class` 속성은 “공백으로 구분된 여러 개의 class”를 갖는 특별한 속성이다.
* 따라서 문자열을 그대로 붙여버리면 잘못된 class가 만들어진다.

타임리프는 이를 해결하기 위해 classappend라는 별도 확장 기능을 제공한다.

반면 attrappend는 모든 속성에 일괄적으로 동작하는 일반적인 기능이므로
class처럼 특별한 처리를 하지 않는다.

---

# 4. 실제 비교 요약

| 기능                     | attrappend         | classappend         |
| ---------------------- | ------------------ | ------------------- |
| 처리 대상                  | 모든 속성              | class 속성 전용         |
| 문자열 합치기                | 그대로 붙임             | 공백 자동 처리            |
| class="text"에 large 추가 | textlarge (잘못된 결과) | text large (올바른 결과) |
| 사용 목적                  | 일반 속성 값 덧붙이기       | CSS 클래스 추가          |
| 실전에서 class에 사용?        | 비권장                | 필수                  |

---

# 5. 결론

맞다. attrappend와 classappend는 완전히 다르다.
그리고 class를 다룰 때는 반드시 classappend를 사용해야 한다.

원하면 attrprepend와 classprepend의 차이까지 이어서 설명해줄게.

결론부터 말하면, attrappend와 classappend는 확실히 다르고 용도가 분명히 구분된다.

둘 다 “속성에 값을 덧붙인다”는 점은 같지만,
classappend는 class 속성 전용 기능이고,
attrappend는 모든 속성에 적용되지만 class에 쓰면 원치 않는 결과가 나오기 쉽다.

아래에서 차이를 정확하게 정리해줄게.

---

# 1. th:attrappend — “아무 속성에나 값을 문자 그대로 덧붙임”

문법:

```html
th:attrappend="속성이름='값'"
```

동작:

* 해당 속성의 문자열 뒤에 그대로 덧붙임
* 공백도 직접 넣어줘야 함
* class에 쓰면 실수가 자주 발생함

예:

```html
<input class="text" th:attrappend="class=' large'">
```

결과:

```html
<input class="text large">
```

공백을 넣지 않으면:

```html
th:attrappend="class='large'"
```

결과:

```html
<input class="textlarge">
```

→ 의도하지 않은 결과 발생

---

# 2. th:classappend — “class 속성 전용 append, 자동 공백 처리”

문법:

```html
th:classappend="값"
```

동작:

* class 속성에 값을 추가
* 자동으로 앞에 공백을 붙여줌
* class 전용이기 때문에 좀 더 안전하고 깔끔함

예:

```html
<input class="text" th:classappend="large">
```

결과:

```html
<input class="text large">
```

공백 자동 처리되는 차이가 핵심이다.

---

# 3. 두 기능의 핵심적인 차이

| 구분                | th:attrappend | th:classappend     |
| ----------------- | ------------- | ------------------ |
| 적용 범위             | 모든 속성         | class 속성 전용        |
| 공백 자동 처리          | 없음 → 직접 넣어야 함 | 있음                 |
| class에 사용 시 권장 여부 | 비권장 (실수하기 쉬움) | 강력 권장              |
| 문자열 처리            | 문자 그대로 붙임     | class 리스트에 안전하게 추가 |

---

# 4. 실전에서는 classappend를 사용하는 것이 정석

실제 개발에서는 다음이 거의 표준이다:

```html
<div class="menu" th:classappend="${active} ? ' on' : _"></div>
```

왜냐하면:

* class는 공백 기반(여러 개의 class 지원)
* classappend는 공백 자동 처리
* class 전용 로직이라 버그 발생 가능성 낮음

---

# 5. 결론

attrappend와 classappend는 다르며, class 속성에는 classappend를 사용해야 한다.

* attrappend → `"문자열을 그냥 붙이는 것"`
* classappend → `"class 속성을 안전하게 확장하는 것"`

따라서 class에 값을 추가하려면 classappend가 훨씬 적절하고 안전하다.

---

원하면 attrprepend와 classprepend 비교도 정리해줄게.

여기서 말하는 공백을 추가한다는 것은
“속성이 여러 개”라는 뜻이 아니라,
class 속성의 값 안에 여러 개의 클래스 이름을 구분하기 위해 공백을 넣는다는 의미다.

즉, 속성이 여러 개가 아니라
class라는 하나의 속성 안에 여러 개의 class 이름이 들어간다는 개념이다.

아래에서 정확히 구분해줄게.

---

# 1. HTML 속성(attribute)은 그대로 하나다

예:

```html
<input class="text large" />
```

여기서 속성은 하나다:

* 속성 이름: class
* 속성 값: "text large"

따라서 class라는 속성은 1개이다.

---

# 2. 공백은 “클래스 이름이 여러 개인 것”을 의미한다

class 속성의 값은 다음 규칙을 갖는다.

* CSS 클래스 이름은 공백으로 구분된다.
* 즉 공백이 들어가면 “클래스가 여러 개”라는 뜻이다.

예:

```html
class="text large"
```

이 의미는:

* text 라는 class 하나
* large 라는 class 하나

총 두 개의 class가 이 요소에 적용됨.

하지만
속성 자체는 class 하나뿐이다.

---

# 3. attrappend가 문제를 만들었던 이유도 여기 있다

예:

```html
<input class="text" th:attrappend="class='large'" />
```

렌더링:

```html
<input class="textlarge" />
```

여기에는 공백이 없다.

브라우저 해석:

* 클래스 이름: textlarge (1개)
* text라는 클래스도 없고, large라는 클래스도 없다

따라서 완전히 잘못된 결과다.

---

# 4. classappend는 공백을 자동으로 넣어준다

왜냐하면 타임리프는 class 속성이
“공백으로 구분된 여러 개의 class 이름을 담는 특별한 속성”
이라는 걸 알고 있기 때문이다.

```html
<input class="text" th:classappend="large" />
```

렌더링:

```html
<input class="text large" />
```

여기서 공백을 넣어주는 이유는:

* text 라는 class
* large 라는 class

이렇게 두 개의 class를 적용하기 위한 구분자가 필요하기 때문이다.

---

# 5. 정리

| 표현                   | 의미                  | 속성 개수    | 클래스 개수   |
| -------------------- | ------------------- | -------- | -------- |
| `class="text"`       | 클래스 하나              | 1        | 1        |
| `class="text large"` | 클래스 두 개             | 1        | 2        |
| `class="textlarge"`  | 클래스 한 개 (textlarge) | 1        | 1        |
| 공백 추가                | 클래스 간 구분자           | 속성 수와 무관 | 클래스 수 증가 |

---

# 결론

* 공백이 생긴다고 해서 HTML 속성이 여러 개가 되는 것이 아니다.
* 공백은 하나의 class 속성 안에서 여러 개의 CSS 클래스 이름을 구분하는 역할을 한다.
* 따라서 class 속성은 한 개지만, classappend는 class 이름을 여러 개 추가하기 위해 공백을 자동으로 붙여주는 것이다.

원하면 클래스가 여러 개일 때 CSS가 어떻게 적용되는지도 예제로 설명할게.

그렇다. 타임리프에서 class 속성은 HTML의 CSS 클래스(class)를 의미한다.
그리고 HTML 태그에는 class 외에도 매우 다양한 다른 속성(attribute)들이 있다.

아래에서 정리해줄게.

---

# 1. class 속성 = CSS 클래스들

HTML의 `<input>`이나 `<div>` 같은 태그에서

```html
class="text large"
```

이렇게 되어 있으면 의미는:

* CSS에서 `.text { ... }` 규칙을 적용
* CSS에서 `.large { ... }` 규칙을 적용

즉, class 속성 안에서는 CSS class 이름들을 공백으로 구분하여 여러 개 나열할 수 있다.

타임리프는 class 속성을 특별취급하여:

* `th:classappend`
* `th:classprepend`
* `th:class`

등을 제공하고,
이 속성들은 CSS class를 다루기 위한 전용 도구다.

---

# 2. class 외에 HTML 태그가 가질 수 있는 속성

HTML 태그에서 class 외에도 다음과 같은 속성들이 존재한다:

## 1) id

요소를 식별하는 고유 값

```html
<div id="header"></div>
```

## 2) name

입력 요소의 이름(서버로 전달될 key)

```html
<input type="text" name="username">
```

## 3) type

input 태그의 타입 (text, checkbox 등)

```html
<input type="checkbox">
```

## 4) value

입력 값

```html
<input value="default">
```

## 5) href

링크 목적지

```html
<a href="/home">홈</a>
```

## 6) src

이미지나 스크립트의 경로

```html
<img src="/img/logo.png">
```

## 7) checked

checkbox/radio의 선택 여부

```html
<input type="checkbox" checked>
```

## 8) disabled / readonly

입력 불가능하게

```html
<input disabled>
```

## 9) style

직접 CSS 작성

```html
<div style="color: red"></div>
```

## 10) placeholder

입력창 기본 안내 문구

```html
<input placeholder="이름을 입력하세요">
```

## 11) data-*

사용자 정의 데이터

```html
<div data-id="3"></div>
```

등등, HTML에는 수십 가지 속성이 있다.

---

# 3. 타임리프에서 class 속성만 특별한 이유

타임리프는 대부분의 속성은 공통 규칙으로 처리한다.

예:

* `th:attr`
* `th:attrappend`
* `th:attrprepend`
* `th:attr="name=${val}"`

하지만 class 속성은 "여러 개의 CSS 클래스를 공백으로 나열"하는 특별한 방식 때문에
타임리프는 따로 전용 속성을 제공한다.

* `th:classappend` : CSS 클래스 뒤에 추가
* `th:classprepend` : CSS 클래스 앞에 추가
* 공백 자동 처리
* 다중 class 안전하게 추가

이 모든 것은 CSS class 특성 때문이지,
타임리프 자체가 class를 특별한 기능으로 쓰는 건 아니다.

---

# 4. 나머지 HTML 속성들은 어떻게 타임리프가 다뤄?

다른 속성들은 다음과 같이 다룬다.

### 속성 직접 설정

```html
<input th:name="'myName'">
```

### 동적 입력

```html
<input th:value="${username}">
```

### 속성 덧붙이기

```html
<a href="/base" th:attrappend="href='?page=1'"></a>
```

### 속성 제거(null로 설정)

```html
<div th:attr="id=${cond} ? 'main' : null"></div>
```

class처럼 특별한 처리(공백 붙이기 등)는 없다.

---

# 5. 요약

| HTML 속성 | 의미        | 타임리프 처리              |
| ------- | --------- | -------------------- |
| class   | CSS 클래스들  | 특별취급 (classappend 등) |
| id      | 요소 고유 이름  | 일반 속성 처리             |
| name    | 입력 이름     | 일반 속성 처리             |
| href    | 링크 주소     | 일반 속성                |
| src     | 이미지/파일 경로 | 일반 속성                |
| value   | 입력 값      | 일반 속성                |
| checked | 체크 여부     | 일반 속성                |
| type    | input 타입  | 일반 속성                |

---

# 결론

* 타임리프에서 class 속성은 CSS 클래스를 말한다.
* class 외에도 HTML 태그에는 다양한 속성이 있으며,
  타임리프는 대부분을 일반 속성으로 처리한다.
* class 속성만 특별히 다루는 이유는 CSS 클래스 특성 때문이다.

원하면 class, id, name, value 등 HTML 속성들의 쓰임새만 따로 깔끔히 정리해서 알려줄게.
