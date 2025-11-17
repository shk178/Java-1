`ObjectFactory`와 `ObjectProvider`는 스프링 빈을 “필요할 때마다 늦게 조회하는 기능(Lazy Lookup)” 을 제공하는 도구다.
특히 싱글톤 안에서 프로토타입 빈을 사용할 때 많이 등장한다.

전체 개념과 차이, 사용 이유를 단계적으로 설명해볼게.

---

# 1. 공통된 목적

두 인터페이스 모두 아래 문제를 해결하기 위해 만들어졌다.

> “싱글톤 빈이 프로토타입 빈을 생성 시점에 딱 한 번만 주입받으면, 이후 계속 같은 프로토타입 인스턴스를 쓰게 된다.
> 매번 새로운 프로토타입 객체를 얻고 싶다면 어떻게 해야 하지?”

이때 ApplicationContext를 직접 주입해서
`ac.getBean(Prototype.class)` 를 호출할 수도 있지만, 이것은 안티패턴이다.

그래서 만들어진 것이:

* ObjectFactory
* ObjectProvider

두 개다.

---

# 2. ObjectFactory

가장 기본적인 “지연 조회” 기능을 제공한다.

```java
public interface ObjectFactory<T> {
    T getObject();
}
```

특징:

1. `getObject()` 를 호출할 때마다 빈을 조회한다
2. 프로토타입 빈이라면 새로운 객체를 반환한다
3. 기능이 매우 최소한이라 쓰기 불편할 수 있다
4. 스프링 초기 버전부터 존재하는 가장 단순한 Provider

사용 예:

```java
@Autowired
private ObjectFactory<PrototypeBean> prototypeFactory;

public int logic() {
    PrototypeBean prototype = prototypeFactory.getObject(); 
    prototype.addCount();
    return prototype.getCount();
}
```

이렇게 하면 logic() 호출할 때마다 새로운 PrototypeBean이 얻어진다.

---

# 3. ObjectProvider (ObjectFactory 확장판)

ObjectProvider는 ObjectFactory의 하위 인터페이스이다.

```java
public interface ObjectProvider<T> extends ObjectFactory<T> { ... }
```

즉,

> ObjectProvider = ObjectFactory + 추가 기능

ObjectProvider만의 능력:

1. Optional 지원: `getIfAvailable()`
2. 스트림 처리: `stream()`
3. 빈이 없어도 에러 없이 처리 가능
4. 더 유연하고 실무에서 사용하기 훨씬 편함

사용 예:

```java
@Autowired
private ObjectProvider<PrototypeBean> prototypeProvider;

public int logic() {
    PrototypeBean prototype = prototypeProvider.getObject();
    prototype.addCount();
    return prototype.getCount();
}
```

동작은 같은데, ObjectProvider는 훨씬 더 유연하다.

---

# 4. 왜 ApplicationContext 대신 ObjectProvider를 쓰는가?

ApplicationContext로도 가능하다:

```java
@Autowired
private ApplicationContext ac;

PrototypeBean p = ac.getBean(PrototypeBean.class);
```

하지만 이 방식은 다음 문제 때문에 좋지 않다.

1. ApplicationContext는 너무 많은 기능을 제공한다
   → 단순히 “빈 하나 조회”를 위해서 너무 무거운 의존

2. SRP(단일 책임 원칙)를 지키지 못한다
   → 클라이언트가 컨테이너 자체를 의존하게 됨

3. 테스트하기 어렵다

4. 컨테이너를 들고 다니는 것은 DIP 관점에서도 좋지 않다

반면 ObjectProvider는:

* 의존성을 깔끔하게 유지하면서
* “필요할 때마다 빈을 새로 요청하는 기능만” 제공한다
* 라이프사이클 관리 문제도 일으키지 않는다

그래서 실무에서도 ApplicationContext보다는 ObjectProvider를 권장한다.

---

# 5. ObjectProvider와 프로토타입 스코프의 관계

싱글톤은 한 번만 생성되지만
ObjectProvider.getObject()는 스프링에게 “그때그때 빈을 줘”라고 요청한다.

프로토타입인 경우:

* getObject() 호출 때마다 새로운 객체 생성
* @PostConstruct도 매번 실행
* 반환 후에는 스프링 관리 종료

그래서 아래와 같은 패턴이 안전하고 권장된다.

```java
@Autowired
private ObjectProvider<PrototypeBean> prototypeProvider;

public int logic() {
    PrototypeBean bean = prototypeProvider.getObject();
    return bean.getCount();
}
```

---

# 6. 두 인터페이스의 차이 정리

| 항목          | ObjectFactory     | ObjectProvider       |
| ----------- | ----------------- | -------------------- |
| 기본 기능       | 빈을 지연 조회          | 빈 지연 조회 + 더 많은 부가 기능 |
| Optional 처리 | 불가                | 가능                   |
| Stream 지원   | 없음                | 있음                   |
| 예외 처리       | 제한적               | 유연함                  |
| 실무 사용       | 거의 없음             | 많이 사용됨               |
| 역할          | Provider 기본 인터페이스 | 고급 Provider          |

요약하면:

> ObjectFactory는 최소 기능
> ObjectProvider는 실용적이고 강력한 Lazy Lookup 도구

---

# 7. 결론 정리

1. 두 도구 모두 “필요한 순간에 새 빈을 조회하는 기능”을 제공한다.
2. 싱글톤 안에서 프로토타입 빈을 안전하게 쓰는 핵심 도구다.
3. ApplicationContext에 의존하는 것보다 훨씬 좋은 설계다.
4. 실무에서는 대부분 ObjectProvider를 사용한다.
5. ObjectProvider.getObject()는 프로토타입 빈이면 매번 새로 생성해준다.

---

원한다면 이어서:

* Provider<PrototypeBean> (JSR-330 표준 Provider) 와의 비교
* ObjectProvider 내부 구현(BeanFactoryUtils 사용 방식)
* 실무에서 Provider 패턴을 사용하는 예시

까지도 설명해줄게.

표준 Provider는 JSR-330(javax.inject.Provider) 에 정의된,
스프링이 아닌 자바 공식 DI 표준에서 제공하는 “지연조회(Lazy Lookup) 인터페이스”다.

스프링이 만든 `ObjectProvider`와 달리
순수 자바 표준이므로 스프링 밖의 다른 DI 프레임워크(Guice 등)와도 호환된다.

아주 단순하지만, 그 단순함 덕분에 “표준 Provider”라고 부른다.

---

# 1. 표준 Provider의 형태

소스는 매우 간단하다.

```java
public interface Provider<T> {
    T get();
}
```

기능은 단 한 가지:

> get() 호출 시 새로운(또는 현재 스코프의) 빈을 반환한다.

끝이다. 추가 기능도 없다.

---

# 2. 사용 방법

스프링에서도 바로 사용할 수 있다.

예)

```java
import javax.inject.Provider;

@Autowired
private Provider<PrototypeBean> provider;

public int logic() {
    PrototypeBean bean = provider.get(); // 새 프로토타입 빈 생성
    bean.addCount();
    return bean.getCount();
}
```

이렇게 하면:

* provider.get()을 호출할 때마다
* 스프링 컨테이너가 PrototypeBean을 새로 생성해준다

즉, 스프링 ObjectProvider.getObject()와 동일한 목적을 가진다.

---

# 3. ObjectProvider와의 비교

## 공통점

* 둘 다 “필요할 때마다 빈을 요청하는 기능” 제공
* 싱글톤 내부에서 프로토타입 빈을 안전하게 사용 가능
* lazy lookup 가능

## 차이점

| 비교 항목 | ObjectProvider                                     | Provider(JSR-330 표준)    |
| ----- | -------------------------------------------------- | ----------------------- |
| 표준 여부 | 스프링 전용                                             | 자바 DI 표준                |
| 기능    | getObject(), getIfAvailable(), stream() 등 추가 기능 많음 | get() 하나뿐               |
| 의존성   | 스프링에 종속                                            | 스프링 외 DI 프레임워크에서도 사용 가능 |
| 유연성   | 훨씬 편함                                              | 최소한의 기능만 제공             |
| 실무 사용 | 스프링에서는 더 자주 사용됨                                    | 프레임워크 독립 설계를 원할 때 사용    |

결론적으로:

> 스프링에 묶이지 않은 순수한 Provider 패턴을 쓰고 싶으면 JSR-330 Provider
> 스프링 안에서 편하게 쓰고 싶으면 ObjectProvider

이렇게 선택하면 된다.

---

# 4. 왜 표준 Provider가 필요한가?

가장 큰 이유 두 가지:

### 이유 1) 스프링 종속성을 제거할 수 있음

다른 DI 프레임워크에서도 똑같이 동작하는 Provider 코드를 만들 수 있다.

예: 스프링을 Guice로 교체해도 Provider<T>는 동일하게 동작한다.

### 이유 2) 테스트와 모듈화가 쉬워짐

Provider는 아주 단순한 인터페이스라
모킹/mock 테스트하기 쉽다.

```java
Provider<PrototypeBean> mockProvider = () -> new MockPrototypeBean();
```

이런 식으로 단순하게 테스트할 수 있다.

---

# 5. 정리

Provider(JSR-330 표준) 는:

1. 자바 DI 표준에서 정의된 가장 간단한 Lazy Lookup 도구
2. 메서드는 단 하나: `get()`
3. 스프링뿐 아니라 Guice 같은 다른 DI 프레임워크에서도 동일하게 사용 가능
4. 스프링의 ObjectProvider보다 기능은 적지만 프레임워크 독립성이 장점

---

원한다면 이어서:

* Provider를 스코프별로 어떻게 동작하게 만드는지
* 프로토타입 외에도 Provider가 쓰이는 상황
* ObjectProvider 내부 구조
* Provider와 FactoryBean 차이

까지도 자세히 풀어줄게.

“gradle에 라이브러리를 추가해야 한다”는 말은 현재 프로젝트가 어떤 외부 기능(라이브러리)을 필요로 하기 때문에,
그 기능을 사용하려면 Gradle의 build.gradle 파일에 해당 라이브러리를 의존성으로 등록해야 한다는 뜻이다.

즉,

> 프로젝트에서 어떤 클래스를 쓰려면, 그 클래스가 들어 있는 라이브러리를 먼저 가져와야 한다.
> 그걸 가져오는 작업이 ‘Gradle에 추가하는 것’이다.

예제를 기준으로 설명해볼게.

---

# 1. 왜 Provider를 쓰려면 Gradle에 추가해야 하는가?

JSR-330의 `Provider` 인터페이스는
`javax.inject` 패키지에 들어 있다.

즉:

```java
import javax.inject.Provider;
```

이 코드를 쓰려면
javax.inject 관련 JAR이 프로젝트 클래스패스에 있어야 한다.

하지만 스프링이 기본으로 제공하는 라이브러리에는
javax.inject가 포함되어 있지 않다.

그래서 Gradle에 아래 라이브러리를 추가해줘야 한다는 말이 나온 거다.

이 라이브러리는 공식 JSR-330 API만 담고 있는 very small JAR이다.

---

# 2. 실제로 Gradle에 추가하는 코드

build.gradle (dependencies 블록)에 다음을 추가한다.

```gradle
implementation 'javax.inject:javax.inject:1'
```

이걸 추가하면:

* Provider 인터페이스가 프로젝트 클래스패스에 포함되고
* import javax.inject.Provider; 가 정상적으로 동작한다

---

# 3. 왜 이런 작업이 필요한 걸까?

자바 프로젝트는 다음 규칙을 가진다.

> 프로젝트에서 사용할 모든 클래스는 프로젝트 클래스패스(classpath)에 있어야 한다.

classpath는 다음을 포함한다:

* JDK 기본 클래스(java.util, java.lang 등)
* 프로젝트 내부의 .java 파일
* 외부 라이브러리(JAR 파일)

Provider는 JDK에 포함되어 있지 않은 외부 라이브러리이기 때문에
스스로 classpath에 추가해야 한다.

그 작업을 Gradle이 자동으로 해주는 방식이 바로:

```gradle
implementation 'javax.inject:javax.inject:1'
```

같은 dependency 선언이다.

---

# 4. 정리

### “gradle에 라이브러리를 추가해야 한다”는 말은?

> 그 기능이 들어있는 외부 라이브러리가 프로젝트에 없으니,
> build.gradle에 dependency를 추가해서 라이브러리를 가져와야 한다는 뜻.

### Provider를 쓰려면 왜 필요?

* Provider 인터페이스는 JDK 기본 제공이 아니다
* 별도의 라이브러리(JAR)에 들어있다
* 그 JAR을 Gradle로 가져와야 한다

### 어떻게 추가?

```gradle
implementation 'javax.inject:javax.inject:1'
```

끝.

---

원한다면:

* Gradle이 dependency를 다운로드하고 classpath에 넣는 과정
* implementation / compileOnly / runtimeOnly의 차이
* Maven에서 어떻게 추가하는지

이런 것도 이어 설명할게.

스프링 웹 스코프(Web Scope)는 웹 요청이 처리되는 동안만 존재하는 특별한 스코프들이다. 웹 애플리케이션 환경에서만 동작하며, 기본 싱글톤과 달리 HTTP 요청·세션·웹 애플리케이션의 생명주기와 밀접하게 연결된다.

아래는 핵심 스코프들과 동작 방식이다.

---

# 1. 웹 스코프가 필요한 이유

싱글톤 빈은 애플리케이션 전체에서 하나만 존재하기 때문에, 각 요청마다 다른 상태를 유지해야 하는 빈이 필요할 때 문제가 된다.
예를 들어,

* 사용자별 세션 정보
* 각 요청별 로깅 정보
* 요청별 계산 결과 저장

이럴 때 웹 스코프 빈을 사용한다.

---

# 2. 주요 웹 스코프 종류

## 2.1 request 스코프

* HTTP 요청 하나가 들어오고 나갈 때까지 유지
* 요청마다 새로운 인스턴스 생성
* 컨트롤러나 서비스에서 요청별로 독립적인 정보가 필요할 때 사용

```java
@Scope("request")
@Component
public class MyRequestBean {
}
```

생성/소멸 시점

* 생성: 요청 시작
* 소멸: 요청 종료

---

## 2.2 session 스코프

* HTTP 세션과 생명주기를 같이함
* 로그인 사용자 정보 저장에 자주 사용

```java
@Scope("session")
@Component
public class MySessionBean {
}
```

생성/소멸 시점

* 생성: 세션 생성
* 소멸: 세션 만료 혹은 로그아웃 시 제거

---

## 2.3 application 스코프

* 서블릿 컨텍스트와 생명주기를 같이함
* 애플리케이션 전체에서 1개만 존재하지만, 싱글톤과 달리 서블릿 컨테이너의 생명주기에 종속됨

```java
@Scope("application")
@Component
public class MyAppBean {
}
```

생성/소멸 시점

* 생성: 최초 서블릿 컨텍스트 시작
* 소멸: WAS 종료

---

## 2.4 websocket 스코프

* 웹소켓 연결 단위로 존재
* 실시간 통신이 많은 환경에서 사용 가능

```java
@Scope("websocket")
@Component
public class MyWsBean {
}
```

---

# 3. 웹 스코프 사용 시 주의할 점

## 3.1 웹 스코프 빈은 기본적으로 프록시를 사용한다

request/session 스코프 빈은 싱글톤 빈에 주입할 수 없다. 왜냐하면 요청마다 달라지기 때문이다.

예시:

```java
@Component
public class LogService {

    private final MyRequestBean requestBean; // request 스코프

    public LogService(MyRequestBean requestBean) {
        this.requestBean = requestBean; // 주입 시점엔 아직 요청이 없다
    }
}
```

이 경우 스프링은 프록시 객체를 대신 주입하여, 실제 요청이 들어왔을 때 진짜 request bean을 찾는다.

사용 예:

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Component
public class MyRequestBean { }
```

---

## 3.2 웹 스코프는 테스트 환경에서 바로 동작하지 않는다

`@WebMvcTest`나 `MockMvc`, `RequestContextHolder` 등을 사용해야 한다.
단순한 JUnit 실행에서는 request/session 스코프 빈을 만들 수 없다.

---

# 4. 언제 사용하면 좋은가?

| 스코프         | 사용 사례                                  |
| ----------- | -------------------------------------- |
| request     | 요청마다 새로운 ID 생성, 요청 로그 저장, 요청 단위 임시 데이터 |
| session     | 로그인 정보, 장바구니 정보                        |
| application | 애플리케이션 단위 공용 캐시, 설정 값 공유               |
| websocket   | 실시간 통신 클라이언트 상태                        |

---

# 5. request 스코프 예제 (가장 자주 쓰는 패턴)

```java
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LogTrace {

    private String traceId;

    @PostConstruct
    public void init() {
        this.traceId = UUID.randomUUID().toString();
        System.out.println("Request Start: " + traceId);
    }

    @PreDestroy
    public void close() {
        System.out.println("Request End: " + traceId);
    }
}
```

컨트롤러에서 주입:

```java
@RestController
public class LogController {

    private final LogTrace trace;

    public LogController(LogTrace trace) {
        this.trace = trace;
    }

    @GetMapping("/test")
    public String test() {
        trace.doSomething();
        return "ok";
    }
}
```

요청할 때마다 traceId가 새로 생성된다.

---

필요하시면

* request 스코프 적용 시 프록시가 어떻게 동작하는지
* 실제 WAS 환경 흐름
* request 스코프 빈이 컨트롤러에서 어떻게 공유되는지
* 세션 스코프 사용 시 주의점
  등을 더 깊게 설명해드릴 수 있다.

업로드한 네 개의 파일은 모두 정상적으로 읽혔고, 구조 자체에는 문제 없어 보인다.
오류가 난다고 했는데, web 패키지의 request 스코프 빈(MyLogger) 때문에 발생하는 전형적인 문제일 가능성이 높다.

아래에서 네 파일을 기반으로 어떤 오류가 날 수 있는지 설명할게.

---

# 1. 전체 파일 구조

### AutoAppConfig.java

`@ComponentScan`만 설정한 기본 설정 파일

### LogController.java

`MyLogger`와 `LogService`를 주입받는 컨트롤러

### LogService.java

`MyLogger`를 주입받아 로직 실행

### MyLogger.java

request 스코프 빈

---

# 2. 오류가 발생하는 가장 흔한 이유

현재 코드에서 가장 문제가 될 부분은 다음 한 줄이다.

```
@Scope(value = "request")
private final MyLogger myLogger;  (컨트롤러와 서비스에서 사용)
```

`LogService`는 싱글톤 서비스 빈이고, 여기에 request 스코프 빈을 직접 주입하면 스프링이 실행 시점에 주입할 수 없어 예외가 발생한다.

일반적으로 Spring Boot에서 이런 구조를 만들면 다음 예외가 난다:

```
Scope 'request' is not active for the current thread
```

또는

```
Error creating bean with name 'logService': 
 Scope 'request' is not active ...
```

즉, request 스코프 빈은 웹 요청(ThreadLocal 기반) 동안만 살아있기 때문에
싱글톤 빈이 생성되는 시점에 주입할 수 없다.

---

# 3. 해결 방법 (Spring 공식 방식)

### 방법 1: ObjectProvider 사용

`LogService`에 직접 주입하지 말고, 필요할 때 요청한다.

```java
@Service
@RequiredArgsConstructor
public class LogService {

    private final ObjectProvider<MyLogger> myLoggerProvider;

    public void logic(String id) {
        MyLogger myLogger = myLoggerProvider.getObject();
        myLogger.log("service id = " + id);
    }
}
```

### 방법 2: 프록시 사용 (가장 추천되는 방식)

`MyLogger`에 프록시 모드 추가:

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

이렇게 하면 스프링이 MyLogger를 가짜 프록시 객체로 감싸고,
실제 요청(request)이 들어올 때 진짜 객체를 내부적으로 바인딩한다.

컨트롤러·서비스 같은 싱글톤 빈에서도 문제 없이 주입된다.

---

# 4. 왜 웹에서 실행할 때만 오류가 나나?

로컬에서 단순 테스트 코드로 실행할 경우 request 스코프가 없기 때문에
MyLogger를 사용할 수 없다.

웹 요청을 받는 컨트롤러에서 테스트하면 request 스코프가 활성화되므로 동작한다.

하지만 서비스 빈은 애플리케이션 구동 시점에 생성되므로
request 스코프 빈을 바로 주입할 수 없어 오류가 나는 것이다.

---

# 5. 결론

정리하면:

* 파일 내용은 구조적으로 올바르다.
* 오류 발생 원인은 싱글톤 빈(LogService)이 request 스코프 빈(MyLogger)를 직접 주입받기 때문이다.
* 해결하려면

    * `@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)`
    * 또는 `ObjectProvider`, `ProxyFactoryBean` 등을 사용해야 한다.

---

필요하다면 수정된 전체 코드도 만들어줄게.
혹시 실행 시 나타나는 정확한 에러 메시지를 알려주면 더 정확하게 분석해줄 수 있다.

지금 나타난 에러 로그는 전형적인 AutoAppConfigTest에서 request 스코프 빈(MyLogger)을 로딩하려 시도했기 때문에 발생하는 오류다.

핵심 원인은 이미 로그에 정확히 적혀 있다.

```
Caused by: java.lang.IllegalStateException: No Scope registered for scope name 'request'
```

이 메시지가 말 그대로 문제의 정답이다.

---

# 1. 왜 테스트에서만 터질까?

`AutoAppConfigTest`에서는 다음 코드가 실행된다.

```java
new AnnotationConfigApplicationContext(AutoAppConfig.class);
```

여기엔 웹 환경이 없다.
ServletRequest, DispatcherServlet, RequestContextHolder 같은 것들이 없다.

따라서 스프링은 request 스코프를 등록하지 않는다.

그런데 AutoAppConfig는 컴포넌트 스캔을 통해 다음 빈을 찾는다.

```
MyLogger (request scope)
LogService (싱글톤)
LogController (싱글톤)
```

싱글톤 LogService는 MyLogger를 바로 주입받으려고 하고,
그 순간 테스트 환경에서는 request 스코프가 없기 때문에 터진다.

---

# 2. 실제 웹 실행에서는 왜 잘 되는데, 테스트에서는 안 되나?

웹 실행 시에는 다음이 자동등록된다.

* DispatcherServlet
* RequestContextListener 또는 RequestContextFilter
* RequestScope

그래서 request 스코프 빈이 정상적으로 동작한다.

하지만 순수 단위 테스트(AnnotationConfigApplicationContext)에서는
이것들이 자동으로 없으므로 request 스코프가 존재하지 않는다.

---

# 3. 해결 방법

## 방법 A — request 스코프 빈에 프록시 추가 (스프링 공식 권장)

`MyLogger`를 다음처럼 바꾼다.

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

이렇게 하면 request 스코프 빈을 직접 생성하는 것이 아니라
프록시(dummy) 객체를 먼저 주입해두고, 요청이 실제 들어올 때 진짜 객체를 연결한다.

이 경우 테스트에서도 애플리케이션 컨텍스트가 오류 없이 뜬다.
(테스트에서는 프록시는 살아있지만 내부의 실제 request 데이터는 null)

---

## 방법 B — 테스트에서 request 스코프 수동 등록

테스트 코드에 다음을 추가하는 방식도 가능하다.

```java
@Test
public void basicScan() {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
    ac.registerScope("request", new RequestScope());  // 강제로 스코프 등록
    ac.register(AutoAppConfig.class);
    ac.refresh();
}
```

하지만 이것은 일반적으로 추천되지 않는다.

---

## 방법 C — LogService에서 ObjectProvider 사용

```java
private final ObjectProvider<MyLogger> myLoggerProvider;
```

이 방식도 가능하지만, 테스트 시 request 스코프 자체가 없으면 여전히 터질 수 있다.

프록시 방식(A)이 가장 깔끔하고, 강의에서도 보통 이렇게 한다.

---

# 4. 정답 요약

AutoAppConfigTest에서 request 스코프가 없기 때문에 발생하는 정상적인 오류다.

해결법(가장 정석):

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

이 한 줄만 추가하면 테스트도 정상적으로 실행된다.

---

원한다면 MyLogger, LogService, LogController 전체 수정된 버전도 만들어줄게.

정확하게 이해했다.
request 스코프 빈을 테스트하려면 웹 환경(즉, localhost:포트로 실제 요청 들어오는 환경)이 필요하다.
`new AnnotationConfigApplicationContext()`로는 절대 안 된다.

이유를 단계별로 설명해줄게.

---

# 1. request 스코프는 "웹 요청"이 있어야만 존재한다

`@Scope("request")`는 다음 상황에서만 활성화된다.

* DispatcherServlet이 동작 중
* HTTP 요청이 들어와서 RequestContextHolder에 request 정보가 바인딩됨
* Spring Web MVC 환경이 초기화됨

즉, 다음 같은 환경에서만 request 스코프를 사용할 수 있다.

```
http://localhost:9090/log-demo
```

또는 실제 서버가 올라가 있는 상태의 모든 HTTP 요청

---

# 2. 그런데 AnnotationConfigApplicationContext는 웹이 아니다

다음 코드는 순수한 스프링 컨테이너를 만드는 코드다.

```java
new AnnotationConfigApplicationContext(AutoAppConfig.class);
```

여기에는 다음이 없다.

* 웹 서버
* DispatcherServlet
* request 스코프
* session 스코프
* RequestContextHolder

그러니 request 스코프 빈이 로딩되면 에러가 날 수밖에 없다.

로그에 나온 에러 그대로다.

```
No Scope registered for scope name 'request'
```

---

# 3. 결론: request 스코프 빈은 web server 환경에서만 정상 동작한다

따라서 두 문장을 정확히 구분해야 한다.

### ❌ 잘못된 방식

AnnotationConfigApplicationContext로 request 스코프 빈을 테스트하려 한다
→ 반드시 오류 난다.

### ✔ 올바른 방식

실제 서버를 띄워서 웹 요청을 보낸다
예:

```
localhost:9090/log-demo
```

이렇게 하면 MyLogger.init(), log(), close() 모두 정상 동작한다.

---

# 4. 그러면 테스트는 어떻게 해야 하는가?

만약 *테스트 코드*에서 request 스코프 빈을 다루고 싶다면 선택지는 두 개다.

### (1) SpringBootTest + WebEnvironment 사용

스프링이 톰캣까지 띄워주므로 request 스코프가 활성화됨.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MyTest {
}
```

### (2) 프록시 모드 사용 (강의에서 자주 쓰는 방식)

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

이렇게 하면 테스트 컨텍스트도 에러 없이 올라간다.
왜냐하면 request 스코프 빈이 아닌 프록시 객체가 대신 주입되기 때문이다.

---

# 5. 요약

당신이 말한 것, 완전히 맞다.

* request 스코프 빈은 localhost:9090 같은 실제 웹 요청이 있어야 한다.
* `new AnnotationConfigApplicationContext()` 같은 순수 컨테이너에서는 request 스코프가 없다.
* 그래서 테스트에서 빈 생성할 때 예외가 난다.
* 해결하려면 proxyMode를 켜거나 SpringBootTest로 실행해야 한다.

---

추가로,
지금 파일 구조 전체가 request 스코프 예제를 그대로 만든 것이므로
웹 환경에서 실행해야 제대로 되는 구조가 맞다.

필요하면 실행 가능한 전체 코드 정리해서 다시 만들어줄까?

결론부터 말하면 MyTest 클래스 하나만 만든다고 웹에서 실행되지는 않는다.
웹 환경에서 실행되려면 웹 서버(내장 톰캣 등)를 실제로 띄워야 한다.

즉, 다음 두 경우 중 하나여야 한다.

---

# 1. 실제로 웹 애플리케이션을 실행하는 경우 (main 실행)

보통 스프링 부트에서는 이렇게 한다.

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

이렇게 해야 내장 톰캣이 뜨고, localhost:8080 또는 설정한 포트로 접속할 수 있다.

당신 프로젝트가 순수 스프링(Core), Web MVC만 사용하는 경우라면 ServletContainer를 따로 띄워야 한다.
(클래식 스프링 MVC라면 web.xml 또는 WebInitializer 필요)

---

# 2. 테스트로 웹 환경을 띄우는 경우 (SpringBootTest)

만약 "테스트 코드만으로도 request 스코프를 테스트하고 싶다"면,
다음처럼 해야 한다.

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class MyTest {
}
```

또는 실제 서버까지 띄우려면:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyTest {
}
```

이 방식은 스프링 부트 기반일 때만 가능하다.

---

# 그런데 지금 당신 프로젝트는 어떻게 보이냐?

파일 경로가 아래처럼 되어 있다.

```
lecture/src/main/java/basic/web
```

그리고 실행할 때도 다음 테스트를 쓰고 있다.

```java
new AnnotationConfigApplicationContext(AutoAppConfig.class);
```

이걸 보면 스프링 부트 프로젝트가 아닌 순수 스프링(Core)로 보인다.
이 경우 `AnnotationConfigApplicationContext`로는 웹 서버를 띄울 수 없다.

따라서:

* MyTest 클래스 하나 만든다고 웹 실행이 되지 않는다.
* 웹 서버를 따로 띄우는 환경이 반드시 필요하다.

---

# 당신이 원하는 결과가 무엇인지에 따라 방법이 달라진다

### 1) “MyLogger request 스코프 예제를 실제 웹 요청으로 돌리고 싶다”

→ 웹 서버가 필요함

방법:

* 스프링 부트 프로젝트로 변경해 실행
  또는
* Tomcat/WAS 설정을 직접 구성해 web.xml 기반으로 실행

### 2) “테스트에서도 request 스코프 초기화 에러 없이 빈을 불러오고 싶다”

→ `proxyMode`만 지정해도 해결

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

이렇게 하면 테스트에서도 에러가 안 나고, 웹에서는 정상 동작한다.

하지만 HTTP 요청을 보내지는 않으므로 로그 출력이 완전히 뜨는 것은 아니다.

---

# 정리

* MyTest 클래스만으로는 웹이 실행되지 않는다.
* 웹 환경이 필요하면 서버를 실제로 띄우는 코드(Main)가 있어야 한다.
* 테스트에서 request 스코프 오류를 피하고 싶다면 proxyMode만 추가하면 된다.

---

당신 코드 목적에 따라
“웹 서버까지 포함한 실행 코드”나
“SpringBoot 버전으로 전환한 구조”도 만들어줄 수 있어.

원하는 방향이 어떤 건지 말해줄래?

* 웹 서버를 실제로 띄워서 실행하고 싶은지
* 테스트에서 오류 없이 컨텍스트만 띄우고 싶은지
* 둘 다 필요한지

지금 나온 오류는 정확히 이렇게 말하고 있다.

```
Scope 'request' is not active for the current thread;
consider defining a scoped proxy for this bean if you intend to refer to it from a singleton
```

이 메시지는 스프링 Boot 애플리케이션을 실행할 때조차 request 스코프 빈(MyLogger)을 싱글톤 빈(LogService)에 직접 주입했기 때문에 생기는 정상적인 오류다.

즉, 애플리케이션을 실행했더라도 아직 HTTP 요청이 들어온 것이 아니므로 request 스코프가 활성화되지 않은 상태다.

그런데 스프링은 서버 시작과 동시에 모든 싱글톤 빈을 먼저 생성하려고 한다.

이 시점에서 싱글톤 LogService는 다음 의존성을 주입받아야 한다.

```
private final MyLogger myLogger;   // request 스코프
```

request 스코프는 HTTP 요청이 있을 때만 생성할 수 있으므로
스프링이 빈을 만들 수 없고 당연히 실패한다.

---

# 핵심: request 스코프 빈은 싱글톤 빈에 직접 주입될 수 없다

이 문제는 웹 환경을 띄웠는지 여부와 상관이 없다.

* 서버를 띄웠든
* 테스트로 실행했든
* ApplicationContext를 만들었든

싱글톤 빈 생성 시점에 request 스코프가 없다는 사실은 동일하다.

따라서 해결책은 단 하나다.

---

# 해결책 (스프링 공식)

### 반드시 다음을 MyLogger에 추가해야 한다.

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

즉, 전체 수정본은 다음처럼 되어야 한다.

```java
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MyLogger {
    ...
}
```

이것을 넣으면:

* LogService에 주입되는 것은 MyLogger 자체가 아니라 가짜 프록시 객체
* HTTP 요청이 들어오면 프록시가 실제 request 스코프 MyLogger를 찾아 바인딩
* HTTP 요청이 없으면 프록시는 내부에 아무것도 연결하지 않은 상태로 남아있음
* 스프링 컨테이너 시작할 때도 에러가 없음

이 방식이 바로 스프링 MVC에서 request, session 스코프 빈을 싱글톤 빈에 연결할 때 사용하는 정석 구조다.

---

# 왜 꼭 프록시가 필요한가?

싱글톤(LogService)은 애플리케이션 시작 시 즉시 생성된다.
request 스코프(MyLogger)는 HTTP 요청이 있을 때만 생성된다.

시점이 다르다.

직접 주입 → 시점이 다르므로 에러
프록시 주입 → 시점을 일치시키지 않고도 지연 연결 가능

스프링이 이 케이스를 위해 proxyMode를 제공한다.

---

# 정리

지금 오류는 정상적이고, 정확히 예상되는 동작이다.
스프링 부트를 실행했더라도 request 스코프 빈 생성 시점은 HTTP 요청 이후이기 때문이다.

따라서 해결하려면:

```
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

이것을 MyLogger에 반드시 넣어야 한다.

---

원하면 proxyMode 적용한 전체 코드(LectureApplication + MyLogger + LogService + Controller)도 정리해서 다시 보여줄게.
