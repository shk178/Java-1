스프링 부트에서 **필터(Filter)**, **쿠키(Cookie)**, **세션(Session)**은 로그인 인증·인가 흐름에서 각각 역할이 다른 서블릿 기반 객체들입니다. 로그인 기능을 구현할 때 이 세 가지를 함께 사용하면 전형적인 **세션 기반 인증 구조**를 만들 수 있습니다. 아래에서 각각의 개념과 실제 로그인 처리 흐름에서 어떻게 조합되는지 단계별로 설명하겠습니다.

---

# 1. 필터(Filter)

### 역할

* 서블릿 컨테이너 단계에서 요청이 컨트롤러에 도달하기 전 가로채는 객체
* 인증/인가 체크, 로깅, 공통 처리 등에 사용
* 모든 요청 또는 특정 URL 패턴에 대해 작동 가능
* 스프링 MVC의 인터셉터보다 더 앞단(서블릿 레벨)에 동작

### 로그인에서 필터를 사용하면?

* 인증이 필요한 URL 요청 시 세션에 로그인 정보가 존재하는지 확인
* 존재하지 않으면 로그인 페이지로 리다이렉트
* 존재하면 요청을 계속 흐름으로 넘김

---

# 2. 쿠키(Cookie)

### 역할

* 클라이언트 브라우저에 저장되는 작은 데이터
* HTTP는 Stateless이기 때문에, 서버가 클라이언트 식별하는 데 쿠키가 사용됨
* 보통 **세션 ID(JSESSIONID)**를 클라이언트가 저장하는 용도로 사용
* 직접 사용자 정보를 쿠키에 저장하는 것은 보안 문제 때문에 권장하지 않음

### 로그인에서 쿠키를 사용하면?

* 사용자가 로그인하면 서버가 세션을 만들고, 그 세션의 ID를 쿠키(JSESSIONID)로 브라우저에 보냄
* 브라우저는 이후 요청마다 쿠키를 자동 전송 → 서버는 이를 통해 같은 사용자인지 식별

---

# 3. 세션(Session)

### 역할

* 서버 측에 저장되는 사용자 데이터 보관소
* 로그인 정보(예: userId)를 세션에 저장하여 이후 요청에서 사용
* 세션 ID로 연결된 사용자만 해당 데이터를 사용 가능

### 로그인에서 세션을 사용하면?

* 사용자 인증에 성공하면 세션에 사용자 정보를 저장
* 이후 요청마다 세션을 조회해 로그인 여부를 판단
* 로그아웃 시 세션을 삭제하면 쿠키의 JSESSIONID는 더 이상 유효하지 않음

---

# 4. 세 객체를 모두 활용한 로그인 처리 흐름

아래는 가장 기본적이고 안전한 **세션 기반 로그인 구조**입니다.

## (1) 로그인 요청 → ID/PW 체크

컨트롤러에서 사용자 입력 검증 후 인증 성공

```java
@PostMapping("/login")
public String login(LoginForm form, HttpServletRequest request) {
    // 사용자 검증
    User user = authService.login(form.getUsername(), form.getPassword());
    if (user == null) {
        return "redirect:/login?error";
    }

    // 세션 생성
    HttpSession session = request.getSession();
    session.setAttribute("loginUser", user);
    return "redirect:/home";
}
```

## (2) 세션이 생성되면 서버는 브라우저에게 JSESSIONID 쿠키 전달

* 스프링/서블릿 컨테이너가 자동으로 JSESSIONID 쿠키 생성
* 개발자가 별도 쿠키 설정할 필요 없음(특수한 경우 제외)

## (3) 이후 요청마다 필터에서 세션이 있는지 확인

필터 구현 예:

```java
public class LoginCheckFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // 인증이 필요 없는 경로는 그냥 통과
        if (isWhiteList(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // 세션 확인
        HttpSession session = httpRequest.getSession(false); // false = 없으면 새로 만들지 않음
        if (session == null || session.getAttribute("loginUser") == null) {
            httpResponse.sendRedirect("/login?redirectURL=" + requestURI);
            return;
        }

        // 로그인 상태 → 다음 필터로 진행
        chain.doFilter(request, response);
    }
}
```

스프링 부트 등록:

```java
@Bean
public FilterRegistrationBean<LoginCheckFilter> loginCheckFilter() {
    FilterRegistrationBean<LoginCheckFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new LoginCheckFilter());
    bean.setOrder(1);
    bean.addUrlPatterns("/*");
    return bean;
}
```

## (4) 인증된 사용자만 컨트롤러 접근 가능

필터를 통과한 요청만 컨트롤러로 전달됨.

## (5) 로그아웃

세션 무효화

```java
@PostMapping("/logout")
public String logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate();
    }
    return "redirect:/";
}
```

---

# 5. 총정리: 각 객체의 역할 구분

| 객체      | 위치          | 역할              | 로그인에서의 활용                  |
| ------- | ----------- | --------------- | -------------------------- |
| Filter  | 서블릿 컨테이너 앞단 | 공통 요청 처리, 인증 체크 | 세션 존재 여부로 인증 검증            |
| Cookie  | 클라이언트 브라우저  | 상태 유지용 작은 데이터   | JSESSIONID 쿠키로 세션 식별       |
| Session | 서버          | 사용자 정보 보관       | 로그인 정보 저장 및 요청 간 사용자 상태 유지 |

---

# 6. 실제로 3개를 다 사용한다면?

**가장 일반적이고 안전한 로그인 방식:**

1. 로그인 성공 → **세션 생성**
2. 서버가 **세션 ID를 쿠키(JSESSIONID)**로 브라우저에 전송
3. 이후 요청에서 **필터가 쿠키의 세션 ID를 기반으로 세션을 확인**
4. 로그인된 사용자인지 여부로 접근 제어
5. 로그아웃 시 세션 삭제

즉

* **쿠키는 세션 ID 전달용**
* **세션은 인증 정보 저장용**
* **필터는 인증 확인 용**

이 세 가지가 함께 로그인 처리를 완성합니다.

---

필요하시면

* 인터셉터 방식과의 비교
* JWT 기반(세션 사용 안 함) 로그인 방식
* 스프링 시큐리티 기반의 표준 인증 구조
  도 자세히 설명해 드릴 수 있어요.

# 스프링 부트의 필터, 쿠키, 세션

## 1. 필터 (Filter)

필터는 서블릿 실행 전후에 요청과 응답을 가로채서 처리하는 객체입니다.

**주요 특징:**
- 서블릿 컨테이너 레벨에서 동작
- 요청/응답의 전처리, 후처리 가능
- 체인 방식으로 여러 필터 연결 가능

**기본 구조:**
```java
@Component
public class LoginCheckFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 전처리 로직
        System.out.println("요청 URI: " + httpRequest.getRequestURI());
        
        chain.doFilter(request, response); // 다음 필터 또는 서블릿으로 전달
        
        // 후처리 로직
    }
}
```

## 2. 쿠키 (Cookie)

클라이언트(브라우저)에 저장되는 작은 데이터 조각입니다.

**주요 특징:**
- 클라이언트 측에 저장
- 만료 시간 설정 가능
- 용량 제한 (약 4KB)
- 보안에 취약할 수 있음

**사용 예시:**
```java
// 쿠키 생성
Cookie cookie = new Cookie("userId", "user123");
cookie.setMaxAge(60 * 60 * 24); // 1일
cookie.setPath("/");
response.addCookie(cookie);

// 쿠키 읽기
Cookie[] cookies = request.getCookies();
if (cookies != null) {
    for (Cookie cookie : cookies) {
        if ("userId".equals(cookie.getName())) {
            String userId = cookie.getValue();
        }
    }
}
```

## 3. 세션 (Session)

서버에 저장되는 사용자별 데이터 저장소입니다.

**주요 특징:**
- 서버 측에 저장
- 세션 ID를 통해 관리 (JSESSIONID 쿠키)
- 보안성이 쿠키보다 높음
- 서버 메모리 사용

**사용 예시:**
```java
// 세션 생성 및 데이터 저장
HttpSession session = request.getSession();
session.setAttribute("loginUser", userObject);

// 세션 데이터 읽기
User loginUser = (User) session.getAttribute("loginUser");

// 세션 무효화 (로그아웃)
session.invalidate();
```

## 로그인 처리 통합 예시

필터, 쿠키, 세션을 모두 활용한 로그인 시스템을 만들어드리겠습니다:## 동작 흐름 정리

**로그인 시:**
1. 사용자가 ID/PW 입력
2. 컨트롤러에서 인증 확인
3. **세션**에 사용자 정보 저장
4. "자동 로그인" 체크 시 **쿠키**에 토큰 저장

**페이지 접근 시 (필터 동작):**
1. **필터**가 모든 요청을 가로챔
2. 화이트리스트 체크 (로그인 페이지 등은 통과)
3. **세션**에서 로그인 사용자 확인
4. 세션 없으면 **쿠키**에서 자동 로그인 토큰 확인
5. 토큰 유효하면 세션 재생성
6. 둘 다 없으면 로그인 페이지로 리다이렉트

**보안 권장사항:**
- 비밀번호는 반드시 BCrypt 등으로 암호화
- 쿠키에 `HttpOnly`, `Secure` 플래그 설정
- CSRF 토큰 사용
- 세션 타임아웃 설정
- Remember-me 토큰은 일회용 또는 주기적 갱신

필요한 부분을 더 자세히 설명해드릴까요?

```java

// 1. User 엔티티
@Entity
public class User {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
private String username;
private String password;
private String email;

    // getters, setters
}

// 2. 로그인 컨트롤러
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }
    
    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam(required = false) boolean rememberMe,
                       HttpServletRequest request,
                       HttpServletResponse response) {
        
        User user = userService.authenticate(username, password);
        
        if (user == null) {
            return "redirect:/auth/login?error";
        }
        
        // 세션에 사용자 정보 저장
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", user);
        session.setMaxInactiveInterval(30 * 60); // 30분
        
        // "자동 로그인" 체크된 경우 쿠키 생성
        if (rememberMe) {
            String rememberToken = UUID.randomUUID().toString();
            
            // DB에 토큰 저장 (실제로는 UserService에서 처리)
            userService.saveRememberToken(user.getId(), rememberToken);
            
            // 쿠키에 토큰 저장 (7일)
            Cookie rememberCookie = new Cookie("rememberMe", rememberToken);
            rememberCookie.setMaxAge(7 * 24 * 60 * 60);
            rememberCookie.setPath("/");
            rememberCookie.setHttpOnly(true); // XSS 방지
            response.addCookie(rememberCookie);
        }
        
        return "redirect:/";
    }
    
    // 로그아웃
    @PostMapping("/logout")
    public String logout(HttpServletRequest request, 
                        HttpServletResponse response) {
        
        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // 자동 로그인 쿠키 삭제
        Cookie rememberCookie = new Cookie("rememberMe", null);
        rememberCookie.setMaxAge(0);
        rememberCookie.setPath("/");
        response.addCookie(rememberCookie);
        
        return "redirect:/auth/login";
    }
}

// 3. 로그인 체크 필터
@Component
@Order(1)
public class LoginCheckFilter implements Filter {

    @Autowired
    private UserService userService;
    
    // 로그인 없이 접근 가능한 경로
    private static final String[] WHITE_LIST = {
        "/auth/login", "/auth/logout", "/css/**", "/js/**", "/images/**"
    };
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();
        
        try {
            // 화이트리스트 체크
            if (!isLoginCheckPath(requestURI)) {
                chain.doFilter(request, response);
                return;
            }
            
            // 세션에서 로그인 사용자 확인
            HttpSession session = httpRequest.getSession(false);
            User loginUser = null;
            
            if (session != null) {
                loginUser = (User) session.getAttribute("loginUser");
            }
            
            // 세션에 없으면 쿠키 확인 (자동 로그인)
            if (loginUser == null) {
                Cookie[] cookies = httpRequest.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("rememberMe".equals(cookie.getName())) {
                            String token = cookie.getValue();
                            loginUser = userService.findByRememberToken(token);
                            
                            if (loginUser != null) {
                                // 세션 재생성
                                HttpSession newSession = httpRequest.getSession(true);
                                newSession.setAttribute("loginUser", loginUser);
                            }
                            break;
                        }
                    }
                }
            }
            
            // 로그인되지 않은 경우
            if (loginUser == null) {
                httpResponse.sendRedirect("/auth/login?redirectURL=" + requestURI);
                return;
            }
            
            // 로그인된 경우 다음 필터로
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    private boolean isLoginCheckPath(String requestURI) {
        for (String pattern : WHITE_LIST) {
            if (requestURI.startsWith(pattern.replace("/**", ""))) {
                return false;
            }
        }
        return true;
    }
}

// 4. UserService
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user != null && checkPassword(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    
    public void saveRememberToken(Long userId, String token) {
        // DB에 토큰 저장 (실제로는 별도 테이블 또는 User 엔티티에 저장)
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // user.setRememberToken(token);
            // userRepository.save(user);
        }
    }
    
    public User findByRememberToken(String token) {
        // DB에서 토큰으로 사용자 조회
        return userRepository.findByRememberToken(token);
    }
    
    private boolean checkPassword(String rawPassword, String encodedPassword) {
        // 실제로는 BCryptPasswordEncoder 사용
        return rawPassword.equals(encodedPassword);
    }
}

// 5. 필터 설정 (선택사항 - @Component로 자동 등록되지 않는 경우)
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<LoginCheckFilter> loginCheckFilter(
            LoginCheckFilter filter) {
        FilterRegistrationBean<LoginCheckFilter> registration = 
            new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(1);
        registration.addUrlPatterns("/*");
        return registration;
    }
}

```

# 필터, 쿠키, 세션 심층 개념

## 1. 필터 (Filter)

### 필터란?
필터는 **서블릿 컨테이너**와 **서블릿** 사이에서 요청과 응답을 가로채는 중간 계층입니다. 클라이언트의 요청이 서블릿에 도달하기 전, 그리고 서블릿의 응답이 클라이언트에게 전달되기 전에 특정 작업을 수행할 수 있습니다.

### 동작 원리

```
클라이언트 → [Filter1] → [Filter2] → [Filter3] → 서블릿 → 컨트롤러
          ↑                                              ↓
          ←─────────────── 응답 ──────────────────────────
```

### 필터 체인 (Filter Chain)
- 여러 필터가 체인 형태로 연결되어 순차적으로 실행
- `chain.doFilter()`를 호출해야 다음 필터로 넘어감
- 호출하지 않으면 요청이 서블릿까지 도달하지 못함

### 필터의 주요 용도
- **인증/인가**: 로그인 체크, 권한 확인
- **로깅**: 요청/응답 정보 기록
- **인코딩 설정**: 문자 인코딩 통일
- **압축**: 응답 데이터 압축
- **XSS 방지**: 악성 스크립트 필터링
- **CORS 처리**: Cross-Origin 요청 처리

### 필터 라이프사이클

```java
public interface Filter {
    // 필터 초기화 (서버 시작 시 1번만 실행)
    void init(FilterConfig filterConfig) throws ServletException;
    
    // 요청마다 실행
    void doFilter(ServletRequest request, 
                  ServletResponse response, 
                  FilterChain chain) throws IOException, ServletException;
    
    // 필터 종료 (서버 종료 시 1번만 실행)
    void destroy();
}
```

### 필터 vs 인터셉터

| 구분 | 필터 (Filter) | 인터셉터 (Interceptor) |
|------|---------------|------------------------|
| 위치 | 서블릿 컨테이너 레벨 | 스프링 컨텍스트 레벨 |
| 적용 범위 | 모든 요청 | DispatcherServlet 이후 |
| 스프링 빈 주입 | 가능하지만 제약적 | 자유롭게 가능 |
| 예외 처리 | @ControllerAdvice 처리 불가 | 가능 |

---

## 2. 쿠키 (Cookie)

### 쿠키란?
쿠키는 **클라이언트(브라우저)에 저장**되는 작은 텍스트 데이터입니다. 서버가 클라이언트에게 데이터를 저장하도록 요청하고, 이후 같은 서버로 요청할 때 자동으로 함께 전송됩니다.

### 왜 쿠키가 필요한가?
HTTP는 **무상태(Stateless) 프로토콜**입니다. 즉, 서버는 이전 요청을 기억하지 못합니다. 쿠키를 통해 상태를 유지할 수 있습니다.

```
[1번째 요청]
클라이언트 → 서버: "로그인: user123"
서버 → 클라이언트: "Set-Cookie: userId=user123"

[2번째 요청 - 자동으로 쿠키 전송]
클라이언트 → 서버: "쿠키: userId=user123"
서버: "아, 이 사람은 user123이구나!"
```

### 쿠키의 구성 요소

```java
Cookie cookie = new Cookie("name", "value");

// 주요 속성들
cookie.setMaxAge(3600);           // 만료 시간 (초 단위)
cookie.setPath("/");              // 쿠키가 전송될 경로
cookie.setDomain("example.com");  // 쿠키가 전송될 도메인
cookie.setSecure(true);           // HTTPS에서만 전송
cookie.setHttpOnly(true);         // JavaScript 접근 차단
cookie.setSameSite("Strict");     // CSRF 방지
```

### 쿠키 속성 상세

**MaxAge (만료 시간)**
- 양수: 지정한 초 후 만료 (영구 쿠키)
- 0: 즉시 삭제
- 음수 (기본값): 브라우저 종료 시 삭제 (세션 쿠키)

**Path (경로)**
- `/`: 모든 경로에서 전송
- `/admin`: /admin으로 시작하는 경로에만 전송

**Domain (도메인)**
- 설정 안 함: 쿠키를 생성한 도메인만
- `.example.com`: 모든 서브도메인 포함

**Secure (보안)**
- `true`: HTTPS 연결에서만 전송
- 민감한 정보는 반드시 설정

**HttpOnly**
- `true`: JavaScript로 접근 불가 (XSS 방지)
- `document.cookie`로 읽을 수 없음

**SameSite (CSRF 방지)**
- `Strict`: 같은 사이트에서만 전송
- `Lax`: 일부 크로스 사이트 허용
- `None`: 모든 경우 전송 (Secure 필수)

### 쿠키의 한계
- **용량 제한**: 도메인당 약 4KB
- **개수 제한**: 도메인당 약 20-50개
- **보안 취약**: 클라이언트에서 수정 가능
- **네트워크 비용**: 매 요청마다 전송

---

## 3. 세션 (Session)

### 세션이란?
세션은 **서버에 저장**되는 사용자별 데이터 저장소입니다. 각 사용자는 고유한 세션 ID를 받고, 이를 통해 서버의 세션 데이터에 접근합니다.

### 세션 동작 원리

```
[첫 방문]
클라이언트 → 서버: 요청
서버: 세션 생성 (ID: ABC123)
     메모리에 데이터 저장소 생성
서버 → 클라이언트: "Set-Cookie: JSESSIONID=ABC123"

[이후 방문]
클라이언트 → 서버: "Cookie: JSESSIONID=ABC123"
서버: "세션 ABC123 찾기 → 저장된 데이터 조회"
```

### 세션 ID의 전달 방식

1. **쿠키 (가장 일반적)**
    - `JSESSIONID` 쿠키로 자동 전송

2. **URL 파라미터**
    - `http://example.com/page;jsessionid=ABC123`
    - 쿠키 사용 불가능할 때

3. **숨겨진 폼 필드**
    - `<input type="hidden" name="jsessionid" value="ABC123">`

### 세션 저장소

**1. 메모리 (기본)**
```java
HttpSession session = request.getSession();
session.setAttribute("user", userObject);
```
- 장점: 빠름
- 단점: 서버 재시작 시 소멸, 분산 환경에서 공유 불가

**2. 데이터베이스**
- 영구 저장 가능
- 느림

**3. Redis (권장)**
```java
// Spring Session + Redis
@EnableRedisHttpSession
public class SessionConfig {
    // 설정
}
```
- 빠르고 확장 가능
- 분산 환경에서 세션 공유

### 세션 설정

```java
HttpSession session = request.getSession();

// 세션 타임아웃 설정 (초 단위)
session.setMaxInactiveInterval(1800); // 30분

// 세션 무효화
session.invalidate();

// 세션 ID 변경 (보안 강화)
request.changeSessionId();
```

**application.properties**
```properties
# 세션 타임아웃 (분 단위)
server.servlet.session.timeout=30m

# 세션 저장소 타입
spring.session.store-type=redis
```

### 세션 보안 이슈

**1. 세션 고정 공격 (Session Fixation)**
```java
// 로그인 성공 후 세션 ID 변경
String oldSessionId = request.getSession().getId();
request.changeSessionId();
```

**2. 세션 하이재킹 (Session Hijacking)**
- HTTPS 사용
- HttpOnly 쿠키 설정
- IP 체크, User-Agent 체크

**3. 메모리 부족**
- 세션 타임아웃 적절히 설정
- 불필요한 데이터 저장 지양

---

## 쿠키 vs 세션 비교

| 구분 | 쿠키 | 세션 |
|------|------|------|
| **저장 위치** | 클라이언트 (브라우저) | 서버 (메모리/DB/Redis) |
| **보안** | 낮음 (클라이언트에서 수정 가능) | 높음 (서버에서만 수정) |
| **용량** | 약 4KB | 제한 없음 (서버 용량) |
| **속도** | 빠름 (네트워크만) | 느림 (서버 조회 필요) |
| **생명주기** | 만료 시간 설정 가능 | 타임아웃 또는 명시적 삭제 |
| **네트워크 부담** | 매 요청마다 전송 | 세션 ID만 전송 |

---

## 실전 활용 패턴

### 1. JWT vs 세션

**세션 기반 인증**
```
장점: 서버에서 완전 제어, 즉시 무효화 가능
단점: 서버 부담, 분산 환경 복잡
```

**JWT 기반 인증**
```
장점: 무상태, 확장성 좋음
단점: 즉시 무효화 어려움, 토큰 크기
```

### 2. Remember Me 구현

**방법 1: 세션 + 쿠키**
```java
// 쿠키에 토큰 저장
Cookie cookie = new Cookie("rememberMe", token);
cookie.setMaxAge(7 * 24 * 60 * 60); // 7일

// 필터에서 토큰 확인 → 세션 재생성
```

**방법 2: 영구 세션**
```java
session.setMaxInactiveInterval(-1); // 만료 없음
```

### 3. 멀티 디바이스 로그인

```java
// 세션 ID를 DB에 저장
User: { id: 1, username: "user", 
        sessions: ["ABC123", "DEF456"] }

// 다른 기기에서 로그인 시 기존 세션 무효화 가능
```

---

## 추가 개념: 토큰 기반 인증

### JWT (JSON Web Token)
쿠키/세션의 대안으로, 토큰 자체에 정보를 담아 전달합니다.

```
클라이언트 → 서버: 로그인
서버 → 클라이언트: JWT 토큰
클라이언트 → 서버: Authorization: Bearer {token}
서버: 토큰 검증 (DB 조회 불필요)
```

**장점**: 무상태, 확장성  
**단점**: 토큰 크기, 즉시 무효화 어려움

더 궁금한 부분이 있으면 말씀해주세요!
