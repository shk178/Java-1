# Spring Boot 빈 충돌 에러 해결
## 에러 발생 상황
Spring Boot 애플리케이션 실행 시 다음과 같은 에러가 발생했습니다:
```
org.springframework.context.annotation.ConflictingBeanDefinitionException: 
Annotation-specified bean name 'homeController' for bean class [hello.hello_spring.HomeController] 
conflicts with existing, non-compatible bean definition of same name and class [hello.hello_spring.controller.HomeController]
```
## 에러 원인
Spring에서 `@Controller` 어노테이션을 사용하면 자동으로 해당 클래스의 이름을 기반으로 빈(Bean)이 등록됩니다. 이때 빈 이름은 클래스 이름의 첫 글자를 소문자로 변환한 형태가 됩니다.
- `HomeController` 클래스 → `homeController` 빈 이름
문제: 같은 이름의 `HomeController` 클래스가 두 곳에 존재했습니다:
1. `hello.hello_spring.HomeController` (루트 패키지)
2. `hello.hello_spring.controller.HomeController` (controller 패키지)
두 클래스 모두 `@Controller` 어노테이션을 가지고 있어서 같은 빈 이름(`homeController`)으로 등록을 시도했고, 이로 인해 충돌이 발생했습니다.
## 해결 방법
중복된 컨트롤러 중 하나를 제거하거나 이름을 변경해야 합니다.
해결책: 
- 루트 패키지의 `HomeController`를 `HomeControllerOld`로 이름 변경
- `controller` 패키지의 `HomeController`를 유지 (일관된 패키지 구조 유지)
## 참고사항
1. 빈 이름 규칙: Spring은 클래스 이름의 첫 글자를 소문자로 변환하여 빈 이름을 생성합니다.
   - `HomeController` → `homeController`
   - `MemberService` → `memberService`
2. 패키지 구조: 컨트롤러는 `controller` 패키지에 모아두는 것이 좋은 관행입니다.
3. 에러 유형: `ConflictingBeanDefinitionException`은 같은 이름의 빈이 중복 정의될 때 발생합니다.
## 추가 에러: URL 매핑 충돌
### 에러 발생 상황 (2차)
빈 이름 충돌을 해결한 후, 클래스 이름을 변경했지만 다음과 같은 새로운 에러가 발생했습니다:
```
java.lang.IllegalStateException: Ambiguous mapping. Cannot map 'homeControllerOld' method 
hello.hello_spring.HomeControllerOld#home()
to {GET [/]}: There is already 'homeController' bean method
hello.hello_spring.controller.HomeController#home() mapped.
```
### 에러 원인
빈 이름은 다르지만, 두 컨트롤러가 같은 URL 경로(`"/"`)를 매핑하고 있어서 발생한 문제입니다.
- `HomeControllerOld#home()` → `@GetMapping("/")`
- `HomeController#home()` → `@GetMapping("/")`
Spring은 같은 HTTP 메서드와 URL 경로에 대해 여러 핸들러를 매핑할 수 없습니다.
### 해결 방법
`HomeControllerOld`의 `@Controller` 어노테이션을 주석 처리하여 Spring이 이 클래스를 컨트롤러로 인식하지 않도록 했습니다.
```java
// @Controller  // 주석 처리: controller 패키지의 HomeController와 URL 매핑 충돌 방지
public class HomeControllerOld {
    @GetMapping("/")
    public String home() {
        return "redirect:/index.html";
    }
}
```
이제 `controller` 패키지의 `HomeController`만 활성화되어 `"/"` 경로를 처리합니다.
### 참고사항
1. URL 매핑 충돌: 같은 HTTP 메서드와 URL 경로를 가진 핸들러 메서드는 하나만 존재해야 합니다.
2. 빈 이름 vs URL 매핑: 빈 이름은 다르더라도 URL 매핑이 같으면 충돌이 발생합니다.
3. 에러 유형: `IllegalStateException: Ambiguous mapping`은 URL 매핑이 중복될 때 발생합니다.
## "/" 경로의 회원 가입 및 회원 목록 기능 동작 원리
### 전체 구조 개요
`"/"` 경로는 `HomeController`가 처리하며, `home.html` 템플릿을 렌더링합니다. 이 페이지에는 회원 가입과 회원 목록 링크가 있습니다.
### 1. 홈 페이지 ("/") 흐름
```java
@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "home";  // templates/home.html 렌더링
    }
}
```
동작 과정:
1. 사용자가 `http://localhost:8080/` 접속
2. `HomeController#home()` 메서드 실행
3. `templates/home.html` 템플릿 렌더링
4. 화면에 "회원 가입"과 "회원 목록" 링크 표시
```html
<a href="/members/new">회원 가입</a>
<a href="/members">회원 목록</a>
```
### 2. 회원 가입 기능 흐름
#### 2-1. 회원 가입 폼 표시 (GET /members/new)
```java
@GetMapping(value = "/members/new")
public String createForm() {
    return "members/createMemberForm";
}
```
동작 과정:
1. 사용자가 "회원 가입" 링크 클릭 → `/members/new` GET 요청
2. `MemberController#createForm()` 실행
3. `templates/members/createMemberForm.html` 렌더링
4. 회원 이름 입력 폼 표시
#### 2-2. 회원 가입 처리 (POST /members/new)
```java
@PostMapping(value = "/members/new")
public String create(MemberForm form) {
    Member member = new Member();
    member.setName(form.getName());
    memberService.join(member);
    return "redirect:/";  // 홈으로 리다이렉트
}
```
동작 과정:
1. 사용자가 이름 입력 후 "등록" 버튼 클릭
2. `createMemberForm.html`의 폼이 POST로 `/members/new` 전송
   ```html
   <form action="/members/new" method="post">
       <input type="text" id="name" name="name" placeholder="이름 입력">
       <button type="submit">등록</button>
   </form>
   ```
3. Spring이 폼 데이터를 `MemberForm` 객체로 자동 바인딩
   - `name="name"` → `MemberForm.name` 필드에 자동 설정
   - 자세한 바인딩 원리는 아래 "Spring 데이터 바인딩과 접근 제어자" 섹션 참조
4. `MemberController#create()` 실행
5. `Member` 객체 생성 및 이름 설정
6. `MemberService#join()` 호출하여 회원 가입 처리
7. 가입 완료 후 `redirect:/`로 홈 페이지로 이동
#### 2-3. 회원 가입 서비스 로직
```java
public Long join(Member member) {
    validateDuplicateMember(member); // 중복 검증
    memberRepository.save(member);
    return member.getId();
}

private void validateDuplicateMember(Member member) {
    memberRepository.findByName(member.getName())
            .ifPresent(m -> {
                throw new IllegalStateException("이미 존재하는 회원");
            });
}
```
동작 과정:
1. 중복 회원 검증: 같은 이름의 회원이 이미 존재하는지 확인
2. 중복이 없으면 `MemberRepository#save()` 호출
3. `MemoryMemberRepository`에서 ID 자동 생성 및 저장
   ```java
   member.setId(++sequence);  // ID 자동 증가
   store.put(member.getId(), member);  // HashMap에 저장
   ```
4. 생성된 회원 ID 반환
### 3. 회원 목록 기능 흐름
#### 3-1. 회원 목록 조회 (GET /members)
```java
@GetMapping(value = "/members")
public String list(Model model) {
    List<Member> members = memberService.findMembers();
    model.addAttribute("members", members);
    return "members/memberList";
}
```
동작 과정:
1. 사용자가 "회원 목록" 링크 클릭 → `/members` GET 요청
2. `MemberController#list()` 실행
3. `MemberService#findMembers()` 호출하여 모든 회원 조회
4. 조회된 회원 리스트를 `Model`에 추가
   - `model.addAttribute("members", members)` → 템플릿에서 `${members}`로 접근 가능
5. `templates/members/memberList.html` 렌더링
#### 3-2. 회원 목록 서비스 로직
```java
public List<Member> findMembers() {
    return memberRepository.findAll();
}
```
```java
@Override
public List<Member> findAll() {
    return new ArrayList<>(store.values());  // HashMap의 모든 값 반환
}
```
동작 과정:
1. `MemoryMemberRepository#findAll()` 호출
2. `HashMap`에 저장된 모든 `Member` 객체를 리스트로 변환하여 반환
#### 3-3. 회원 목록 화면 렌더링
```html
<table>
    <thead>
        <tr>
            <th>#</th>
            <th>이름</th>
        </tr>
    </thead>
    <tbody>
    <tr th:each="member : ${members}">
        <td th:text="${member.id}"></td>
        <td th:text="${member.name}"></td>
    </tr>
    </tbody>
</table>
```
동작 과정:
1. Thymeleaf 템플릿 엔진이 `members` 리스트를 순회 (`th:each="member : ${members}"`)
2. 각 회원의 ID와 이름을 테이블 행으로 표시
3. 사용자에게 회원 목록이 표 형식으로 표시됨
### 전체 데이터 흐름도
```
[사용자]
    ↓
[/ 홈 페이지] HomeController → home.html
    ↓
[회원 가입 링크 클릭]
    ↓
[GET /members/new] MemberController#createForm() → createMemberForm.html
    ↓
[폼 제출]
    ↓
[POST /members/new] MemberController#create(MemberForm)
    ↓
[MemberService#join()] → 중복 검증
    ↓
[MemoryMemberRepository#save()] → HashMap에 저장
    ↓
[redirect:/] → 홈으로 이동
    ↓
[회원 목록 링크 클릭]
    ↓
[GET /members] MemberController#list()
    ↓
[MemberService#findMembers()]
    ↓
[MemoryMemberRepository#findAll()] → HashMap에서 모든 값 조회
    ↓
[memberList.html] → 회원 목록 테이블 표시
```
### 핵심 컴포넌트 역할
1. Controller (`MemberController`, `HomeController`)
   - HTTP 요청 처리 및 뷰 선택
   - 서비스 계층 호출
2. Service (`MemberService`)
   - 비즈니스 로직 처리 (중복 검증 등)
   - 리포지토리 호출
3. Repository (`MemoryMemberRepository`)
   - 데이터 저장 및 조회 (현재는 메모리 HashMap 사용)
4. Domain (`Member`)
   - 회원 엔티티 (데이터 구조)
5. Form (`MemberForm`)
   - HTTP 요청 데이터를 받는 DTO (Data Transfer Object)
### 참고사항
1. 리다이렉트: 회원 가입 후 `redirect:/`로 홈으로 이동하여 새로고침 시 중복 제출 방지
2. Thymeleaf 템플릿: HTML에 동적 데이터를 삽입하는 템플릿 엔진 (`th:each`, `th:text` 등)
3. Model 객체: 컨트롤러에서 뷰로 데이터를 전달하는 Spring의 객체
4. 메모리 저장: 현재는 `HashMap`을 사용하여 애플리케이션 재시작 시 데이터가 사라짐 (실무에서는 DB 사용)
## Spring 데이터 바인딩과 접근 제어자
### 질문: Spring이 private 필드에 접근할 수 있는가?
Spring의 폼 데이터 바인딩은 접근 제어자(private, protected 등)와 무관하게 작동합니다. 하지만 실제로는 setter 메서드를 통해 접근합니다.
### 데이터 바인딩 방식
#### 1. Setter 메서드를 통한 접근 (일반적인 방법)
```java
public class MemberForm {
    private String name;  // private 필드
    
    // Spring이 이 setter 메서드를 호출합니다
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
```
동작 과정:
1. 폼에서 `name="name"` 값이 전송됨
2. Spring이 `MemberForm` 객체를 생성
3. Spring이 `setName()` 메서드를 찾아서 호출 (리플렉션 사용)
4. `setName()`이 private 필드 `name`에 값을 설정
핵심: Spring은 private 필드에 직접 접근하는 것이 아니라, public setter 메서드를 호출합니다.
#### 2. 리플렉션을 통한 직접 필드 접근 (가능하지만 비권장)
리플렉션(Reflection)을 사용하면 private 필드에도 직접 접근할 수 있습니다:
```java
// 이론적으로 가능하지만, Spring은 일반적으로 setter를 사용합니다
Field field = MemberForm.class.getDeclaredField("name");
field.setAccessible(true);  // 접근 제어자 무시
field.set(form, "값");
```
하지만 Spring의 `DataBinder`는 기본적으로 JavaBean 규칙을 따르므로 setter 메서드를 우선적으로 사용합니다.
### 접근 제어자와의 관계

| 접근 제어자 | Spring 바인딩 가능 여부 | 방법 |
|------------|----|------|
| private 필드 + public setter | 가능 | setter 메서드 호출 |
| private 필드만 (setter 없음) | 가능하지만 비권장 | 리플렉션 직접 접근 |
| public 필드 | 가능 | 직접 접근 또는 setter |

### JavaBean 규칙 준수
```java
public class MemberForm {
    private String name;  // private 필드
    
    // public setter - Spring이 이걸 사용합니다
    public void setName(String name) {
        this.name = name;
    }
    
    // public getter
    public String getName() {
        return name;
    }
}
```
이유:
- 캡슐화 원칙 준수
- 유효성 검증을 setter에서 수행 가능
- 코드 가독성 향상
#### 비권장: public 필드 직접 사용
```java
public class MemberForm {
    public String name;  // public 필드 - 비권장
}
```
이유:
- 캡슐화 원칙 위반
- 유효성 검증 어려움
- 향후 변경 시 영향 범위가 큼
### Spring의 바인딩 프로세스
```
[HTTP 요청]
    ↓
[폼 데이터: name=kim]
    ↓
[Spring DataBinder]
    ↓
1. MemberForm 객체 생성 (new MemberForm())
    ↓
2. 필드 이름 매칭: "name" 찾기
    ↓
3. setter 메서드 탐색: setName() 찾기
    ↓
4. 리플렉션으로 setName() 호출
    ↓
5. setter 내부에서 private 필드에 값 설정
    ↓
[바인딩 완료: MemberForm.name = "kim"]
```
### 정리
1. 접근 제어자와 무관: Spring은 리플렉션을 사용하므로 private, protected, public 모두 접근 가능합니다.
2. 하지만 setter 사용: Spring은 일반적으로 public setter 메서드를 통해 데이터를 바인딩합니다. 이는 JavaBean 규칙을 따르는 것입니다.
3. 모든 데이터 접근 가능: 리플렉션을 사용하므로 접근 제어자 제한을 우회할 수 있습니다. 하지만 보안 모듈(Java Security Manager)이 설정되어 있으면 제한될 수 있습니다.
4. 실무 권장: private 필드 + public setter/getter 패턴을 사용하는 것이 좋습니다.
## final과 static 필드의 바인딩 동작
### final 필드의 경우
#### 1. final 필드의 특성
```java
public class MemberForm {
    private final String name;  // final 필드 - 한 번만 할당 가능
    
    // 컴파일 에러! final 필드는 setter로 변경 불가능
    // public void setName(String name) {
    //     this.name = name;  // 에러: cannot assign a value to final variable
    // }
}
```
특징:
- final 필드는 선언 시 또는 생성자에서만 초기화 가능
- 한 번 할당된 후 변경 불가능 (불변성 보장)
- setter 메서드로 값을 변경할 수 없음 (컴파일 에러)
#### 2. final 필드 바인딩 방법: 생성자 사용
Spring은 final 필드를 바인딩할 때 생성자 파라미터를 사용합니다:
```java
public class MemberForm {
    private final String name;  // final 필드
    
    // 생성자로 초기화
    public MemberForm(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
}
```
동작 과정:
1. Spring이 폼 데이터를 분석하여 `name` 값을 찾음
2. `MemberForm`의 생성자를 찾아서 `new MemberForm("kim")` 형태로 객체 생성
3. 생성자 파라미터에 폼 데이터를 바인딩
4. final 필드가 생성자에서 초기화됨
컨트롤러에서의 사용:
```java
@PostMapping("/members/new")
public String create(MemberForm form) {
    // Spring이 자동으로 MemberForm(String name) 생성자를 호출
    // form.name은 이미 생성자에서 초기화됨
    return "redirect:/";
}
```
#### 3. final 필드 바인딩 제한사항
```java
// 불가능: setter 방식
public class MemberForm {
    private final String name;
    
    public MemberForm() {}  // 기본 생성자
    
    // 컴파일 에러! final 필드는 setter로 변경 불가능
    public void setName(String name) {
        this.name = name;  // 에러!
    }
}
```
문제점:
- 기본 생성자만 있고 setter가 없으면 final 필드를 초기화할 수 없음
- Spring이 바인딩할 방법이 없음
해결책: 생성자 파라미터를 사용하거나 final을 제거해야 합니다.
### static 필드의 경우
#### 1. static 필드의 특성
```java
public class MemberForm {
    private String name;  // 인스턴스 필드
    private static String staticName;  // static 필드 (클래스 레벨)
    
    public void setName(String name) {
        this.name = name;
    }
    
    public static void setStaticName(String staticName) {
        MemberForm.staticName = staticName;
    }
}
```
특징:
- static 필드는 클래스에 속한 변수 (인스턴스가 아닌 클래스 레벨)
- 모든 인스턴스가 공유하는 하나의 변수
- 인스턴스 생성 없이도 접근 가능
#### 2. Spring의 static 필드 바인딩: 불가능
Spring은 static 필드를 바인딩하지 않습니다.
```java
public class MemberForm {
    private String name;  // 바인딩됨
    private static String staticName;  // 바인딩 안 됨
    
    public void setName(String name) {
        this.name = name;
    }
    
    public static void setStaticName(String staticName) {
        MemberForm.staticName = staticName;
    }
}
```
이유:
1. Spring은 인스턴스 필드에만 바인딩합니다
2. static 필드는 클래스 레벨 변수이므로 인스턴스와 무관합니다
3. 폼 데이터는 특정 인스턴스에 바인딩되어야 하는데, static은 인스턴스와 무관합니다
실제 동작:
```
[폼 데이터: name=kim, staticName=lee]
    ↓
[Spring DataBinder]
    ↓
name → MemberForm 인스턴스의 name 필드에 바인딩
staticName → 무시됨 (바인딩 안 됨)
```
#### 3. static 필드 사용 시 주의사항
```java
// 비권장: static 필드를 폼 데이터로 사용
public class MemberForm {
    private static String name;  // 바인딩 안 됨!
    
    public static void setName(String name) {
        MemberForm.name = name;
    }
}
```
문제점:
- Spring이 바인딩하지 않음
- 모든 요청이 같은 static 변수를 공유 (스레드 안전성 문제)
- 동시 요청 시 데이터 충돌 가능
### 정리: final과 static 필드 바인딩

| 필드 타입 | 바인딩 가능 여부 | 방법 | 주의사항 |
|----------|--------|------|---------|
| 일반 필드 | 가능 | setter 메서드 | 가장 일반적 |
| final 필드 | 가능 | 생성자 파라미터 | setter 불가능, 생성자 필수 |
| static 필드 | 불가능 | - | Spring이 바인딩하지 않음 |

### 일반 필드 (바인딩 가능)
```java
public class MemberForm {
    private String name;
    
    public void setName(String name) {
        this.name = name;  // Spring이 호출
    }
}
```
### final 필드 (생성자로 바인딩)
```java
public class MemberForm {
    private final String name;
    
    public MemberForm(String name) {
        this.name = name;  // Spring이 생성자 호출 시 바인딩
    }
}
```
#### static 필드 (바인딩 안 됨)
```java
public class MemberForm {
    private static String name;  // Spring이 무시
    
    public static void setName(String name) {
        MemberForm.name = name;  // Spring이 호출하지 않음
    }
}
```
### 실무 권장사항
1. 폼 데이터 바인딩: 일반 필드 + setter 사용
2. 불변성 필요 시: final 필드 + 생성자 사용
3. static 필드: 폼 데이터 바인딩에 사용하지 말 것 (클래스 레벨 상수 등에만 사용)
- HTML 폼 입력 태그의 `name` 속성이 중요한 역할은 무엇일까요?
디자인 용도
[`서버 전달 데이터 키`]
자바스크립트 전용
전송 방식 결정
- 해설
HTML 폼 입력의 `name` 속성은 서버로 전송될 때 해당 입력 값과 매칭되는 키 이름으로 사용되어 데이터 바인딩을 돕습니다.
- 컨트롤러에서 비즈니스 로직 처리 후 뷰 템플릿으로 데이터를 넘겨줄 때 사용하는 주요 객체는 무엇인가요?
Request
Session
[`Model`]
Redirect
- 해설
컨트롤러는 Model 객체에 `addAttribute` 메소드를 사용하여 뷰 템플릿에 전달할 데이터를 담습니다. 뷰 리졸버가 템플릿을 찾고 Thymeleaf가 이 데이터를 렌더링하죠.