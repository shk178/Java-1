아래 코드가 “안 된다”는 것은 보통 자바스크립트 오류, 렌더링 결과가 이상함, Thymeleaf 치환이 안 됨 중 하나일 텐데, 이 경우 거의 항상 자바스크립트 인라인 처리(th:inline="javascript")의 동작 방식 때문이다.

아래에서 정확히 어떤 부분이 문제인지 차근차근 설명해보겠다.

---

# 1. 현재 코드의 핵심 문제

## 문제 1) `var username = [[${user.username}]];`

th:inline="javascript" 없으면 숫자·불린이 아닌 값은 그대로 문자 그대로 출력된다.

즉, `th:inline="javascript"`가 없는 스크립트에서는 다음처럼 나온다.

```javascript
var username = A;
```

여기서 따옴표가 없음 → 자바스크립트 SyntaxError 발생

---

## 문제 2) `var user = [[${user}]];`

Java 객체를 그대로 출력하려 하면 다음처럼 나오기 쉽다.

```javascript
var user = com.example.demo.User@2fc4c3;
```

이것도 JavaScript 입장에서 유효한 값이 아니다 → SyntaxError

---

## 문제 3) `/*[[${user.username}]]*/"test"`

`/*[[...]]*/` 구문은 인라인 JavaScript 모드에서만 동작한다.
하지만 첫 번째 `<script>`에는 `th:inline="javascript"`가 없음.

그래서 첫 번째 스크립트에서 Thymeleaf는 이 부분을 그대로 문자열로 남겨둠.

---

# 2. 올바른 동작을 위해 필요한 것

Thymeleaf는 자바스크립트 인라인 모드를 만나야 문자열에 자동으로 따옴표를 붙여준다.

예시:

| 표현                     | th:inline="javascript" 상태 | 출력 결과                   |
| ---------------------- | ------------------------- | ----------------------- |
| `[[${user.username}]]` | 있음                        | `"A"`                   |
| `[[${user.username}]]` | 없음                        | `A`                     |
| `[[${user}]]`          | 있음                        | JSON처럼 만들어줌             |
| `[[${user}]]`          | 없음                        | `com.example.User@xxxx` |

---

# 3. 그래서 아래는 반드시 필요하다

```html
<script th:inline="javascript">
```

이 한 줄이 있어야 Thymeleaf가 스크립트 안의 표현식을 안전하게 JS 값으로 변환한다.

---

# 4. 사용자 객체 JSON 문제

`var user = [[${user}]]`
이 코드는 자바 객체를 JSON 형태로 자동 변환해준다.

예를 들면 다음처럼 됨:

```javascript
var user = {"username":"A"};
```

하지만 th:inline="javascript"가 없으면 절대 변환되지 않음.

---

# 5. 결론: 왜 안 되나?

### 이유 1

첫 번째 `<script>`는 인라인이 꺼져 있어서 JS 안에
`A`, `com.example.User@xxxx` 같은 오류가 나는 값이 들어감.

### 이유 2

두 번째 `<script>`는 정상 작동하지만,
첫 번째에서 이미 자바스크립트 오류가 난 상태라
브라우저는 전체 스크립트 실행을 멈추게 됨.

---

# 6. 해결 방법

## 방법 1: 인라인 없는 스크립트에서 Thymeleaf 표현식 제거

```html
<script th:inline="javascript">
    var username = [[${user.username}]];
    var username2 = /*[[${user.username}]]*/"test";
    var user = [[${user}]];
</script>
```

## 방법 2: 첫 번째 스크립트 자체를 제거하거나, 예시라면 HTML 그대로 남기기

---

원하는 스타일로 다시 정리해줄 수도 있으니
어떤 오류 메시지를 봤는지 알려주면 더 정확히 짚어줄게.

```
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8">
    <title>Javascript</title>
</head>
<body>
<!-- 자바스크립트 인라인x -->
<script>
    var username = A;
    var username2 = /*A*/"test";
</script>
<!-- 자바스크립트 인라인o -->
<script>
    var username = "A";
    var username2 = "A";
</script>
</body>
</html>
```

좋은 질문입니다! Thymeleaf의 자바스크립트 인라인 기능을 이해하는 핵심 포인트네요.

## 각 부분의 역할 설명

### 1. `var username = [[${user.username}]];`
- 목적: 서버 데이터를 자바스크립트 변수에 할당
- 인라인 없으면: `var username = A;` (문법 오류!)
- 인라인 있으면: `var username = "A";` (정상 작동)

### 2. `var username2 = /*[[${user.username}]]*/"test";`
- "test"의 역할: 프로토타입(개발 시 기본값)
- 이것은 Thymeleaf의 자연스러운 템플릿 개념입니다

## "test"는 언제 사용되나?

```javascript
var username2 = /*[[${user.username}]]*/"test";
```

이 코드는 두 가지 상황에서 다르게 동작합니다:

### 1) HTML 파일을 브라우저에서 직접 열 때 (Thymeleaf 없이)
```javascript
var username2 = "test";  // 주석 안의 내용은 무시되고 "test" 사용
```
- 디자이너나 프론트엔드 개발자가 서버 없이 HTML을 바로 열어도
- 자바스크립트가 정상 작동 (기본값으로)

### 2) Thymeleaf가 처리할 때 (서버에서)
```javascript
var username2 = "A";  // Thymeleaf가 주석을 실제 값으로 치환
```
- 서버에서 렌더링되면 주석 부분이 실제 데이터로 교체됨
- "test"는 사라지고 서버 데이터만 남음

## 실무 활용 예시

```html
<script th:inline="javascript">
    // 서버 데이터를 JS 변수로 받기
    var userId = [[${user.id}]];
    var userName = [[${user.username}]];
    var userAge = [[${user.age}]];
    
    // 개발 중에는 "홍길동"으로 테스트, 실제로는 서버 데이터 사용
    var displayName = /*[[${user.displayName}]]*/"홍길동";
    
    // 이제 이 변수들을 JS에서 자유롭게 사용
    console.log("사용자: " + userName);
    if (userAge >= 10) {
    }
</script>
```

## 결론

- `var username`: 서버의 Java 객체 데이터를 JavaScript로 가져오기 위해 씁니다
- `"test"`: HTML을 서버 없이 직접 열어볼 때 사용되는 개발용 기본값입니다
- `th:inline="javascript"`: 문자열을 자동으로 따옴표로 감싸주고, 특수문자 이스케이프 등 안전한 변환을 보장합니다

인라인 없이 쓰면 `var username = A;` 같은 문법 오류가 나지만, 인라인을 켜면 `var username = "A";`로 올바르게 변환되는 것이 핵심입니다!

아, 맞습니다! 좋은 관찰이네요.

## 왜 오류가 나지 않을까?

```javascript
var username = A;
```

이 코드가 실행될 때:

### 1. `A`가 정의되지 않은 변수라면
```javascript
var username = A;  // ReferenceError: A is not defined
```
- 브라우저 콘솔에는 오류가 찍히지만
- 페이지는 그냥 렌더링됩니다
- 자바스크립트 오류가 나도 HTML은 표시되니까 "아무 일도 없는 것처럼" 보임

### 2. `A`가 우연히 전역 변수로 존재한다면
```javascript
var A = "뭔가";
var username = A;  // 정상 작동
```
- 오류 없이 실행됨

## 실제 확인 방법

브라우저 개발자 도구(F12)를 열고 Console 탭을 보면:

```
Uncaught ReferenceError: A is not defined
    at (anonymous) (your-page.html:10:18)
```

이런 식으로 빨간 오류가 찍혀 있을 겁니다.

## 실험 예시

```html
<script>
    console.log("스크립트 시작");
    var username = A;  // 여기서 오류
    console.log("스크립트 끝");  // 이건 실행 안됨!
</script>

<h1>하지만 HTML은 정상 표시됨</h1>
```

결과:
- "스크립트 시작"만 콘솔에 찍힘
- "스크립트 끝"은 실행 안됨 (오류 이후 코드는 중단)
- 하지만 HTML은 화면에 보임

## 결론

"아무 일도 없다"는 건 사실 오류가 조용히 발생한 것이고, 단지:
- 페이지는 계속 보이고
- 콘솔을 열어보지 않으면 모르는 것뿐입니다

실무에서는 이런 오류가 쌓이면 나중에 디버깅이 힘들어지므로, `th:inline="javascript"`를 꼭 사용하는 게 권장됩니다!

정확합니다! 그게 바로 문법 오류(정확히는 참조 오류)가 발생한 증거입니다.

## 오류 메시지 해석

```
Uncaught ReferenceError: A is not defined
    at javascript:10:20
```

- Uncaught: 이 오류를 처리(catch)하지 않았다
- ReferenceError: 정의되지 않은 변수 `A`를 참조하려 했다
- at javascript:10:20: 10번째 줄, 20번째 문자 위치에서 발생

## "아무 일도 없다"고 느낀 이유

```html
<body>
    <h1>페이지 제목</h1>
    
    <script>
        var username = A;  // 오류 발생!
    </script>
    
    <p>본문 내용</p>
</body>
```

이 경우:
- ✅ HTML은 정상 렌더링 (화면에 다 보임)
- ❌ 자바스크립트만 중단됨
- ⚠️ 콘솔 열어보기 전까지 모름

## 문제가 되는 경우

만약 그 아래에 중요한 코드가 있다면:

```javascript
var username = A;  // 여기서 멈춤!
document.getElementById('btn').addEventListener('click', function() {
    alert('클릭!');  // 이 코드는 실행 안됨!
});
```

- 버튼 클릭이 작동하지 않음
- 사용자는 "왜 안되지?" 하면서 답답해함
- 개발자는 콘솔을 열어봐야 원인을 알 수 있음

## 그래서 `th:inline="javascript"`가 중요한 이유

```html
<script th:inline="javascript">
    var username = [[${user.username}]];  // "A"로 안전하게 변환
</script>
```

이렇게 하면 오류 없이 깔끔하게 작동합니다!

요약: 오류는 발생했지만, HTML 렌더링은 멈추지 않아서 "아무 일 없는 것처럼" 보였던 거예요. 콘솔에 빨간 줄이 바로 그 증거입니다! 🎯

아! 정말 좋은 질문입니다! 혼란스러울 수 있는 부분이네요.

## 핵심 포인트

```html
<!-- 자바스크립트 인라인x -->
<script>
    var username = [[${user.username}]];
</script>
```

여기서 "인라인 x"의 의미는:
- ❌ "자바스크립트가 아니다"가 아니라
- ✅ "Thymeleaf 인라인 기능을 안 쓴다"는 뜻입니다!

## 이게 왜 자바스크립트 오류인가?

```html
<script>  ← 이 태그 자체가 자바스크립트!
    var username = [[${user.username}]];
</script>
```

1. `<script>` 태그: 브라우저가 이 안의 내용을 자바스크립트로 실행
2. Thymeleaf가 처리: `[[${user.username}]]` → `A`로 치환
3. 결과물:
```html
<script>
    var username = A;  ← 자바스크립트 코드인데 A가 정의 안됨
</script>
```
4. 브라우저: "이건 자바스크립트인데 A가 뭔데?" → ReferenceError

## 비교

### 인라인 없을 때 (Thymeleaf 처리만)
```html
<script>
    var username = [[${user.username}]];
    ↓ Thymeleaf가 단순 텍스트 치환
    var username = A;  ← 따옴표 없음! (오류 발생)
</script>
```

### 인라인 있을 때 (Thymeleaf + 자바스크립트 인텔리전스)
```html
<script th:inline="javascript">
    var username = [[${user.username}]];
    ↓ Thymeleaf가 "이게 자바스크립트구나!" 인식
    var username = "A";  ← 따옴표 추가! (정상)
</script>
```

## 정리

- "자바스크립트 인라인 x" = Thymeleaf가 자바스크립트 문법을 몰라서 그냥 텍스트만 치환
- "자바스크립트 인라인 o" = Thymeleaf가 자바스크립트 문법을 알고 똑똑하게 변환

두 경우 모두 `<script>` 안이므로 자바스크립트이고, 차이는 Thymeleaf가 얼마나 똑똑하게 치환해주냐입니다!

그래서 인라인 안 한 쪽에서 자바스크립트 오류가 난 거예요. 🎯

완벽한 질문입니다! 이게 웹 개발의 핵심 개념이에요.

## `<script>` 태그는 누가 처리하나?

### 브라우저가 처리합니다! 🌐

```html
<!DOCTYPE html>
<html>
<head>
    <title>테스트</title>
</head>
<body>
    <h1>안녕하세요</h1>
    
    <script>
        console.log("이건 브라우저에서 실행!");
        alert("팝업!");
    </script>
</body>
</html>
```

## 흐름 설명

### 1️⃣ 서버에서 (Thymeleaf 처리)
```html
<script th:inline="javascript">
    var username = [[${user.username}]];
</script>
```
⬇️ Thymeleaf가 치환
```html
<script>
    var username = "A";
</script>
```
- 서버는 그냥 HTML 텍스트를 만듦
- `<script>` 태그도 서버 입장에선 그냥 문자열

### 2️⃣ 브라우저로 전송
```
서버 → (HTML 텍스트) → 브라우저
```

### 3️⃣ 브라우저에서 (자바스크립트 실행)
```html
<script>
    var username = "A";  ← 브라우저가 "아, 이건 자바스크립트구나!" 인식
</script>
```
- 브라우저가 `<script>` 태그를 만나면
- 그 안의 내용을 자바스크립트 엔진으로 실행
- 이게 바로 "자바스크립트가 된다"는 의미!

## 비유로 이해하기

```
서버 = 요리사 (재료 준비)
- Thymeleaf로 HTML 문서를 "요리"
- <script>도 그냥 재료 중 하나

브라우저 = 손님 (요리 먹기)
- HTML 받아서 화면에 표시
- <script> 발견하면 자바스크립트 엔진 실행
- <style> 발견하면 CSS 적용
```

## 실제 예시

```html
<!-- 서버에서 만든 최종 HTML -->
<!DOCTYPE html>
<html>
<body>
    <h1>제목</h1>
    
    <script>
        alert("안녕!");  ← 브라우저가 실행
    </script>
    
    <style>
        h1 { color: red; }  ← 브라우저가 적용
    </style>
</body>
</html>
```

브라우저가 이 HTML을 받으면:
1. `<h1>` 보고 → 화면에 제목 그림
2. `<script>` 보고 → 자바스크립트 실행 (alert 팝업!)
3. `<style>` 보고 → CSS 적용 (제목을 빨간색으로)

## 정리

| 단계 | 누가 | 하는 일 |
|------|------|---------|
| 1. 서버 | Thymeleaf | `[[${...}]]` → 실제 값으로 치환 (텍스트 작업) |
| 2. 전송 | 네트워크 | HTML 문자열을 브라우저로 보냄 |
| 3. 브라우저 | 자바스크립트 엔진 | `<script>` 안의 코드를 실행 |

결론: `<script>`는 서버에서 만들지만, 실제 자바스크립트로 실행되는 건 브라우저입니다! 🎯

좋은 질문입니다! `user` 객체를 그대로 넘기려고 할 때 문제가 생기는 이유를 설명할게요.

## 문제 상황

```javascript
var user = [[${user}]];
```

### 인라인 없을 때
```javascript
var user = User(username=A, age=10);  // 문법 오류!
```
- Thymeleaf가 Java 객체의 `toString()` 결과를 그대로 넣음
- 이건 자바스크립트 문법이 아님!

### 인라인 있을 때
```javascript
var user = {"username":"A","age":10};  // 정상!
```
- Thymeleaf가 Java 객체를 JSON으로 변환해줌
- 브라우저가 이해할 수 있는 자바스크립트 객체

## 왜 `username`은 괜찮았나?

```javascript
// username은 String이라서 간단함
var username = [[${user.username}]];

// 인라인 없으면
var username = A;  // 따옴표만 없는 문제

// 인라인 있으면  
var username = "A";  // 문자열로 잘 변환
```

## 객체는 복잡함

```java
// 서버의 User 객체
class User {
    String username = "A";
    int age = 10;
}
```

```javascript
// 이걸 자바스크립트로 옮기려면?
var user = [[${user}]];

// 인라인 없으면: Java toString() 결과
var user = User(username=A, age=10);  // ❌ JS 문법 아님!

// 인라인 있으면: JSON 변환
var user = {"username":"A","age":10};  // ✅ JS 객체!
```

## 실제 오류 확인

브라우저 콘솔에 뜰 오류:
```
Uncaught SyntaxError: Unexpected identifier 'username'
```

`User(username=A, age=10)` 이런 문법은 자바스크립트에 없으니까요!

## 정리

| 타입 | 인라인 없을 때 | 인라인 있을 때 |
|------|---------------|---------------|
| String | `A` (따옴표 없음) ❌ | `"A"` ✅ |
| 숫자 | `10` ✅ | `10` ✅ |
| 객체 | `User(username=A)` ❌ | `{"username":"A"}` ✅ |
| 배열 | `[User@123, User@456]` ❌ | `[{...}, {...}]` ✅ |

결론: 객체나 배열을 자바스크립트로 넘길 때는 반드시 `th:inline="javascript"`를 써야 JSON으로 제대로 변환됩니다! 🎯

아! 정확한 지적입니다! 두 가지를 확인해볼게요.

## 1. 스크립트 중단 문제

맞습니다! 인라인 없는 쪽에서:

```javascript
<script>
    var username = A;  // 여기서 오류로 중단!
    var username2 = /*A*/"test";  // 이건 실행 안됨
    var user = [[${user}]];  // 이것도 실행 안됨
</script>
```

첫 줄에서 이미 오류가 나서 그 아래 코드는 실행조차 안 됩니다. 맞아요!

## 2. 인라인 있을 때도 오류가 난다고요?

```javascript
<script th:inline="javascript">
    var username = [[${user.username}]];
    var username2 = /*[[${user.username}]]*/"test";
    var user = [[${user}]];  // 여기서 오류?
</script>
```

혹시 오류 메시지가 뭔가요?

가능한 원인들:

### 원인 1: `user` 객체가 복잡한 구조
```java
// 만약 User 객체에 순환 참조나 특수한 필드가 있다면
class User {
    String username;
    Date createdAt;  // JSON 변환 문제 가능
    List<Something> items;  // 복잡한 중첩 구조
}
```

### 원인 2: Jackson/JSON 변환 실패
Thymeleaf가 객체를 JSON으로 변환할 때 문제가 생길 수 있어요.

## 확인해볼 방법

Controller에서 어떤 User 객체를 넘겼나요?

```java
@GetMapping("/javascript")
public String javascript(Model model) {
    User user = new User();
    user.setUsername("A");
    // 다른 필드들은?
    model.addAttribute("user", user);
    return "javascript";
}
```

브라우저에서 "페이지 소스 보기" (Ctrl+U)로 실제 렌더링된 HTML을 보면:

```javascript
<script>
    var user = ??? // 여기에 뭐가 나오나요?
</script>
```

여기에 어떤 값이 나오는지 알려주시면 정확한 원인을 찾을 수 있을 것 같아요! 🔍

완벽하게 원인을 찾았습니다! 🎯

## 핵심 오류 메시지

```
java.lang.IllegalAccessException: class ... cannot access a member of class hello.thymeleaf.basic.BasicController4$User
```

주목! → `BasicController4$User`

`$` 기호는 내부 클래스(inner class)를 의미합니다!

## 문제 원인

User 클래스를 Controller 안에 내부 클래스로 만드셨죠?

```java
public class BasicController4 {
    
    @Data
    static class User {  // 또는 static 없이
        private String username;
    }
    
    @GetMapping("/javascript")
    public String javascript(Model model) {
        User user = new User();
        user.setUsername("A");
        model.addAttribute("user", user);
        return "javascript";
    }
}
```

문제:
- 내부 클래스는 Java 리플렉션 접근 제한이 있어요
- Thymeleaf가 JSON으로 변환할 때 접근 권한 문제 발생
- `@Data`로 getter가 있어도 소용없음!

## 해결 방법 (3가지)

### ✅ 방법 1: 별도 파일로 분리 (권장)

```java
// User.java (새 파일)
@Data
public class User {
    private String username;
}
```

```java
// BasicController4.java
public class BasicController4 {
    @GetMapping("/javascript")
    public String javascript(Model model) {
        User user = new User();
        user.setUsername("A");
        model.addAttribute("user", user);
        return "javascript";
    }
}
```

### ✅ 방법 2: static 내부 클래스로 (임시방편)

```java
public class BasicController4 {
    
    @Data
    public static class User {  // public static 필수!
        private String username;
    }
}
```

- `static` 붙이면 외부 클래스와 독립적
- 하지만 여전히 접근 제한 문제 가능성 있음

### ✅ 방법 3: 객체 대신 필드만 넘기기

```html
<script th:inline="javascript">
    var username = [[${user.username}]];
    
    // 수동으로 객체 구성
    var user = {
        username: [[${user.username}]]
    };
</script>
```

## 왜 이런 문제가?

```
접근 레벨:
Top-level class (별도 파일) ← Thymeleaf 접근 가능 ✅
  ↓
Static inner class ← 조건부 가능 ⚠️
  ↓  
Non-static inner class ← 접근 불가 ❌
```

## 추천 해결

User.java를 별도 파일로 만드세요!

```java
package hello.thymeleaf.basic;

import lombok.Data;

@Data
public class User {
    private String username;
}
```

이렇게 하면 100% 해결됩니다! 실무에서도 일반적으로 도메인 클래스는 별도 파일로 관리합니다. 🚀

```
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8">
    <title>Javascript</title>
</head>
<body>
<!-- 자바스크립트 인라인x -->
<script>
    var username = A;
    var username2 = /*A*/"test";
    var user= BasicController4.User(username=A);
</script>
<!-- 자바스크립트 인라인o -->
<script>
    var username = "A";
    var username2 = "A";
    var user= {"username":"A"};
</script>
</body>
</html>
```

완벽합니다! 🎉 이제 차이가 명확하게 보이네요!

## 결과 분석

### 인라인 없을 때 (❌ 오류 발생)
```javascript
<script>
    var username = A;  // 오류! A는 정의 안된 변수
    var username2 = /*A*/"test";  // 주석 처리되어 "test" 사용
    var user= BasicController4.User(username=A);  // Java toString() 결과 (오류!)
</script>
```

### 인라인 있을 때 (✅ 정상 작동)
```javascript
<script>
    var username = "A";  // 문자열로 변환 ✅
    var username2 = "A";  // 주석을 실제 값으로 치환 ✅
    var user= {"username":"A"};  // JSON 객체로 변환 ✅
</script>
```

## 핵심 정리

| 요소 | 인라인 없음 | 인라인 있음 |
|------|------------|------------|
| String | `A` (오류) | `"A"` (정상) |
| 자연스러운 템플릿 | `/*A*/"test"` → `"test"` | `/*A*/"test"` → `"A"` |
| 객체 | `User(username=A)` (오류) | `{"username":"A"}` (JSON) |

## 실제 사용 예시

이제 자바스크립트에서 이렇게 쓸 수 있어요:

```javascript
<script>
    var username = "A";
    var username2 = "A";
    var user = {"username":"A"};
    
    // 정상 작동!
    console.log(username);  // "A"
    console.log(user.username);  // "A"
    alert("안녕하세요, " + user.username + "님!");
</script>
```

## 결론

`th:inline="javascript"`의 역할:
1. 문자열에 자동으로 따옴표 추가
2. 객체를 JSON으로 변환
3. 특수문자 이스케이프 처리
4. 자바스크립트 문법에 맞게 안전하게 변환

실무에서는 반드시 인라인을 사용해야 안전하고 정상적으로 작동합니다! 💯

```
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8">
    <title>Javascript2</title>
</head>
<body>
<script>
    
    var user1 = {"username":"A"};
    var user2 = {"username":"B"};
    
</script>
</body>
</html>
```

아래 코드는 Thymeleaf의 JavaScript 인라인 모드(th:inline="javascript")를 사용해, 서버에서 전달된 `userList`를 자바스크립트 변수로 렌더링하는 예제다.

---

# 1. 코드 설명

```html
<script th:inline="javascript">
    [# th:each="user, stat: ${userList}"]
    var user[[${stat.count}]] = [[${user}]];
    [/]
</script>
```

### ■ `th:inline="javascript"`

* Thymeleaf이 스크립트 안의 `[[...]]` 표현식을 정상적인 자바스크립트 값으로 변환한다.
* 문자열은 자동으로 `"문자열"`로 감싸고, 객체나 숫자도 JS 값에 맞게 변환해 준다.

### ■ `[#[/]]` 형태의 블록

* `<th:block>` 또는 `<span>` 대신 사용할 수 있는 프로토타입(Typed) 블록 문법.
* HTML에 실제로 출력되지 않고, 렌더링 시 템플릿 로직만 수행된다.

    * `[ # ... ]` 시작
    * `[/ ]` 종료

### ■ `th:each="user, stat: ${userList}"`

* 서버에서 전달된 `userList`(예: List<User>)를 반복하면서

    * `user` → 현재 요소
    * `stat` → 카운터 객체(stat.count 등)

### ■ 실제 렌더링 후 예시

서버에서 예를 들어 다음과 같은 값이 올 때:

```java
userList = ["test1", "test2", "test3"];
```

브라우저에서 실제 출력되는 JS:

```javascript
var user1 = "test1";
var user2 = "test2";
var user3 = "test3";
```

---

# 2. 브라우저 화면에 user 값을 표시하려면?

스크립트 내부 변수는 HTML 요소에 출력해야 브라우저 화면에 보인다.

## 방법 A: HTML 요소에 직접 출력

HTML에 요소 하나 만들고:

```html
<div id="result"></div>
```

JS에서 값 넣기:

```html
<script th:inline="javascript">
    [# th:each="user, stat: ${userList}"]
    var user[[${stat.count}]] = [[${user}]];
    [/]

    document.getElementById("result").textContent = user1;   // 첫 번째 값 출력
</script>
```

---

## 방법 B: 모든 user를 리스트로 출력하고 싶다면

### HTML:

```html
<ul id="userList"></ul>
```

### JS:

```html
<script th:inline="javascript">
    var users = [];

    [# th:each="user, stat: ${userList}"]
    users.push([[${user}]]);
    [/]

    const ul = document.getElementById("userList");
    users.forEach(function(u) {
        const li = document.createElement("li");
        li.textContent = u;
        ul.appendChild(li);
    });
</script>
```

브라우저에서는 이렇게 보인다:

* test1
* test2
* test3

---

# 3. 추천 패턴

현재 코드처럼
`var user1 = "A"; var user2 = "B"; ...`
이런 식으로 변수 여러 개를 만드는 것보다,

### 하나의 배열로 만드는 것이 더 좋다:

```html
<script th:inline="javascript">
    var users = [[${userList}]];
</script>
```

렌더링 결과:

```javascript
var users = ["test1", "test2", "test3"];
```

---

필요하다면 이 코드가 실제로 렌더링될 때 어떻게 보이는지(HTML/JS), 또는 `user`가 객체일 때 어떻게 출력되는지 예시도 자세히 보여줄게.

```
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8">
    <title>Javascript2</title>
</head>
<body>
<script>
    
    var user1 = {"username":"A"};
    var user2 = {"username":"B"};
    
</script>
<script>
    var users = [];

    
    users.push({"username":"A"});
    users.push({"username":"B"});
    

    const ul = document.getElementById("userList");
    users.forEach(function(u) {
        const li = document.createElement("li");
        li.textContent = u;
        ul.appendChild(li);
    });
</script>
</body>
</html>
```

지금 코드에서 브라우저에 아무것도 안 보이는 이유는 단 하나다.

`<ul id="userList"></ul>`
이 HTML 요소가 존재하지 않기 때문이다.

JS에서

```javascript
const ul = document.getElementById("userList");
```

를 찾는데, 문서 안에 해당 요소가 없으니 `ul`은 `null`이 되고,
`ul.appendChild(li)`가 실행되지 않는다.
(콘솔에서 에러가 났을 가능성도 높다)

---

# 1. 화면에 출력되도록 수정한 전체 코드

아래처럼 HTML에 `<ul id="userList"></ul>` 를 추가하면 정상적으로 출력된다.

```html
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8">
    <title>Javascript2</title>
</head>
<body>

<ul id="userList"></ul>

<script>
    var user1 = {"username":"A"};
    var user2 = {"username":"B"};
</script>

<script>
    var users = [];

    users.push({"username":"A"});
    users.push({"username":"B"});

    const ul = document.getElementById("userList");

    users.forEach(function(u) {
        const li = document.createElement("li");
        li.textContent = u.username;   // 객체 그대로 출력하면 [object Object]가 보이므로 username을 꺼내야 함
        ul.appendChild(li);
    });
</script>

</body>
</html>
```

---

# 2. 브라우저에서 실제로 보이는 화면

```
A
B
```

---

# 3. 중요한 포인트

### 1) HTML 요소가 없으면 출력할 수 없다

JS는 DOM 요소에 데이터를 넣어야 화면에 그려진다.

### 2) 객체를 그대로 출력하면 `[object Object]`만 보인다

`li.textContent = u;`
이렇게 쓰면 문자열 변환되면서 `[object Object]`가 된다.

필요한 속성을 꺼내야 한다.

```
li.textContent = u.username;
```

---

원한다면 이 전체 흐름을 Thymeleaf 템플릿 형태로 변환해서 다시 보여줄 수도 있고,
객체 구조가 더 복잡해질 때 어떻게 JSON으로 변환하는지까지 설명해줄 수 있어.

```
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8">
    <title>Javascript2</title>
</head>
<body>
<script>
    
    var user1 = {"username":"A"};
    var user2 = {"username":"B"};
    
</script>
<ul id="userList"></ul>
<script>
    var users = [];

    
    users.push({"username":"A"});
    users.push({"username":"B"});
    

    const ul = document.getElementById("userList");

    users.forEach(function(u) {
        const li = document.createElement("li");
        li.textContent = u.username;   // 객체 그대로 출력하면 [object Object]가 보이므로 username을 꺼내야 함
        ul.appendChild(li);
    });
</script>
</body>
</html>
```
A
B