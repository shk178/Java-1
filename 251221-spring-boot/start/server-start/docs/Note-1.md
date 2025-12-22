에러의 핵심 원인은 **Java 버전과 Gradle(정확히는 Groovy)의 호환성 문제**입니다.

---

## 에러 핵심 해석

```
Unsupported class file major version 65
```

* **major version 65 = Java 21**
* 즉, **Java 21로 컴파일된 클래스**를
* **Gradle 7.5 (내부 Groovy)** 가 해석하려다 실패한 상황입니다.

에러 위치를 보면:

```
BUG! exception in phase 'semantic analysis' in source unit '_BuildScript_'
```

→ `build.gradle`을 **Groovy 스크립트로 파싱하는 단계**에서 터졌습니다.
즉, 애플리케이션 코드 문제가 아니라 **빌드 도구 자체가 Java 21을 이해 못함**이라는 뜻입니다.

---

## 현재 조합 정리 (추정)

* 사용 중인 JDK: **Java 21**
* 사용 중인 Gradle Wrapper: **7.5**
* Gradle 7.5는 **Java 21 미지원**

Gradle ↔ Java 지원 관계는 꽤 엄격합니다.

---

## 해결 방법 2가지 (권장 순서)

### ✅ 방법 1: Gradle을 올린다 (가장 깔끔)

**Gradle 8.4 이상**부터 Java 21을 공식 지원합니다.

#### 1️⃣ 현재 Gradle Wrapper 버전 확인

```
gradlew -v
```

#### 2️⃣ Gradle Wrapper 업그레이드

프로젝트 루트에서:

```
gradlew wrapper --gradle-version 8.5
```

(8.4 이상이면 충분하지만 8.5 권장)

그러면 다음 파일이 자동 수정됩니다:

* `gradle/wrapper/gradle-wrapper.properties`

#### 3️⃣ 다시 실행

```
gradlew explodedWar
```

---

### ✅ 방법 2: Java 버전을 낮춘다 (임시 회피)

Gradle 7.5를 유지해야 한다면:

* **Java 17 또는 Java 11** 사용

#### 1️⃣ Java 17 설치

* Temurin / Oracle JDK 17

#### 2️⃣ JAVA_HOME 변경

Windows 기준 예시:

```
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
```

확인:

```
java -version
```

#### 3️⃣ 다시 실행

```
gradlew explodedWar
```

---

## 어떤 선택이 맞을까?

* **Spring Boot 최신 + Java 21 사용 중** → 👉 Gradle 업그레이드 (방법 1)
* **회사/레거시 프로젝트** → 👉 Java 17로 낮추기 (방법 2)

요즘 Spring Boot 3.x + Java 21 조합이면 **Gradle 8.x는 사실상 필수**입니다.

---

## 추가로 확인하면 좋은 것

1. `gradle-wrapper.properties` 안에:

   ```
   distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-bin.zip
   ```

   → 이게 7.5면 문제 확정

2. IDE에서 사용하는 JDK와
   터미널의 `java -version`이 같은지도 확인

---

원하면

* 현재 `java -version`
* `gradlew -v`
* `build.gradle` 상단

이 3개 올려주면, **정확히 어떤 조합이 꼬였는지** 딱 짚어줄게.

좋은 질문이야. 여기서 헷갈리는 지점이 **“누가 언제 `set`을 채워주느냐”** 인데, 결론부터 말하면:

**`set`은 네 코드에서 채우는 게 아니라, 서블릿 컨테이너(Tomcat)가 `@HandlesTypes`를 보고 자동으로 만들어서 넘겨준다.**

아래를 순서대로 보면 전체 흐름이 딱 맞아 떨어져.

---

## 핵심 요약 (먼저 한 줄로)

* `@HandlesTypes(AppInit.class)` → **컨테이너에게 힌트 제공**
* 컨테이너가 **클래스패스 스캔**
* `AppInit`을 구현/상속한 클래스들을 찾아서
* `Set<Class<?>>`로 만들어
* `ServletContainerInitializer.onStartup(set, servletContext)`에 **주입**

---

## 전체 초기화 흐름 (DispatcherServlet 이전 단계)

### 1단계: WAS(Tomcat) 기동

Tomcat이 서버를 시작하면서 웹 애플리케이션을 로딩한다.

이 시점에는 아직:

* DispatcherServlet ❌
* Spring Context ❌
* @Controller ❌

---

### 2단계: ServletContainerInitializer 검색

Tomcat은 **SPI(Service Provider Interface)** 규칙에 따라 아래 파일을 찾는다:

```
META-INF/services/javax.servlet.ServletContainerInitializer
```

또는 (Jakarta Servlet 기준):

```
META-INF/services/jakarta.servlet.ServletContainerInitializer
```

이 파일 안에 적혀 있는 클래스가 바로:

```
hello.ServletContainerInitializer1
```

→ 이 순간에 **Tomcat은 네 클래스를 “초기화 참여자”로 인식**한다.

---

### 3단계: @HandlesTypes 해석 (여기가 네가 궁금한 부분)

```java
@HandlesTypes(AppInit.class)
public class ServletContainerInitializer1
```

이 어노테이션의 의미는:

> “웹 앱 클래스패스 전체에서
> `AppInit` 타입과 관련된 클래스들을 미리 찾아서
> `onStartup` 호출 시 Set으로 넘겨줘”

중요한 점:

* 이 작업은 **Tomcat이 직접 수행**
* 네 코드는 **관여하지 않음**

---

### 4단계: 클래스패스 스캔

Tomcat이 내부적으로:

* `/WEB-INF/classes`
* `/WEB-INF/lib/*.jar`

을 스캔하면서 다음 조건에 맞는 클래스를 찾는다:

```text
조건:
- AppInit 인터페이스를 구현한 클래스
- AppInit 클래스를 상속한 클래스
```

그래서 발견된 게:

```text
hello.AppInitServlet1
```

→ 이걸 모아서 만든 것이 바로:

```java
Set<Class<?>> set = [class hello.AppInitServlet1];
```

---

### 5단계: ServletContext 생성

Tomcat이 이 웹 애플리케이션 전용 `ServletContext`를 생성한다.

```java
org.apache.catalina.core.ApplicationContextFacade
```

이 객체는:

* 서블릿 등록
* 필터 등록
* 리스너 등록
* 전역 설정 저장

을 담당하는 **웹 앱의 핵심 컨텍스트**다.

---

### 6단계: onStartup 호출 (네 코드 실행 시점)

이제 Tomcat이 드디어 호출한다:

```java
onStartup(set, servletContext)
```

그래서 로그가 이렇게 찍히는 것:

```text
ServletContainerInitializer1.onStartup: set = [class hello.AppInitServlet1]
ServletContainerInitializer1.onStartup: servletContext = ApplicationContextFacade@...
```

---

### 7단계: AppInit 구현체 실행

네 코드에서:

```java
for (Class<?> aClass : set) {
    AppInit appInit = (AppInit) aClass.getDeclaredConstructor().newInstance();
    appInit.onStartup(servletContext);
}
```

→ 이건 **Spring 없이도** 서블릿/필터를 등록할 수 있는 진짜 초기 진입점이다.

---

## DispatcherServlet은 언제 나오나?

그 다음 단계에서 보통 이런 일이 일어난다:

* `AppInitServlet1.onStartup()`

   * `DispatcherServlet` 등록
   * Spring `ApplicationContext` 생성
   * 컨트롤러 스캔 시작

즉:

```
ServletContainerInitializer
 → AppInit
   → DispatcherServlet
     → @Controller
```

이 순서다.

---

## 왜 이 구조가 중요한가?

이 메커니즘 덕분에:

* `web.xml` 없이도 설정 가능
* Spring Boot / Spring MVC 자동 설정 가능
* 라이브러리가 “플러그인처럼” 초기화 로직을 끼워 넣을 수 있음

Spring의 핵심 엔트리 포인트가 바로 이 구조 위에 올라가 있다.

---

## 한 문장으로 정리

> `@HandlesTypes`는 **컨테이너에게 “이 타입 기준으로 클래스 찾아서 set 만들어줘”라고 알려주는 메타데이터**이고,
> `set`은 **Tomcat이 클래스패스 스캔 결과를 담아서 넘겨주는 선물**이다.

원하면 다음으로:

* Spring의 `SpringServletContainerInitializer` 내부 흐름
* 왜 인터페이스 타입만 가능한지
* set에 왜 null이 들어올 수도 있는지

같은 것도 이어서 설명해줄 수 있어.

아주 정확한 지점을 짚었어.
지금 질문은 사실 **ServletContainerInitializer의 존재 이유**를 묻는 거야.

결론부터 말하면:

**맞아. `set`을 만드는 이유는 “7단계를 수행시키기 위해서”고,
7단계는 “프레임워크/애플리케이션이 *자기 초기화 코드*를 실행할 기회를 주는 단계”야.**

아래에서 *왜 필요한지 → 7단계가 정확히 뭔지 → 여기서 뭘 할 수 있고 뭘 못 하는지* 순서로 설명할게.

---

## 1. Tomcat이 직접 모든 걸 초기화하면 안 되는 이유

Tomcat은 이런 걸 **알 수가 없어**:

* 이 웹앱이 Spring을 쓰는지
* MVC인지, REST인지
* 어떤 서블릿을 써야 하는지
* 라이브러리가 초기화가 필요한지

Tomcat은 단지 이렇게 생각해:

> “웹앱이 시작된다.
> 그런데 **이 웹앱/라이브러리 쪽에서 먼저 할 말이 있으면 말해라**.”

그 “말해라”의 공식 통로가 바로:

```
ServletContainerInitializer
```

---

## 2. 왜 `@HandlesTypes` + `Set<Class<?>>` 구조인가

Tomcat이 초기화 코드를 호출할 때 문제는 이거야:

> “어떤 클래스를 실행해야 하지?”

그래서 만든 규칙이:

* **기준 타입 하나를 정해**
* 그 타입과 관련된 클래스들을 **모두 모아서**
* 프레임워크에게 넘겨준다

그게 바로 이 구조야:

```java
@HandlesTypes(AppInit.class)
onStartup(Set<Class<?>> set, ServletContext ctx)
```

즉,

> “AppInit과 관련된 애들 전부 줄 테니까
> 너(프레임워크)가 알아서 처리해”

---

## 3. 그럼 7단계는 정확히 뭘 하는 단계야?

### 7단계의 정체

**“애플리케이션/프레임워크 레벨 초기화 단계”**

아직:

* 요청 처리 ❌
* DispatcherServlet 동작 ❌
* Controller 호출 ❌

오직:

> “웹앱이 막 시작되는 순간,
> 내가 필요한 구성 요소들을 등록하고 준비하는 단계”

---

## 4. 7단계에서 실제로 하는 일들

### 가능한 것들

여기서 하는 건 전부 **구조를 만드는 작업**이야.

대표적인 예:

```java
// 1. 서블릿 등록
servletContext.addServlet("dispatcher", new DispatcherServlet(ctx))
              .addMapping("/");

// 2. 필터 등록
servletContext.addFilter("encodingFilter", new CharacterEncodingFilter())
              .addMappingForUrlPatterns(...);

// 3. 리스너 등록
servletContext.addListener(ContextLoaderListener.class);

// 4. 전역 설정
servletContext.setInitParameter("key", "value");
```

즉:

> **“이 웹앱은 이런 구성으로 동작할 거야”를 선언하는 단계**

---

## 5. 여기서 말하는 “초기화 코드”의 정확한 의미

네가 말한 이 부분:

> 여기선 초기화 코드들만 실행되는 걸까?

👉 **맞다. 그리고 아주 중요한 초기화 코드다.**

정확히는:

* 객체 생성 ❌ (대부분 아직 안 함)
* 비즈니스 로직 ❌
* DB 연결 ❌

하지만:

* 서블릿 구조 결정 ⭕
* 요청 진입점 설정 ⭕
* 프레임워크 부팅 ⭕ (Spring Context 생성 예약)

---

## 6. set이 없다면 무슨 문제가 생길까?

`set`이 없다면 Tomcat은 이렇게 해야 해:

```text
- SpringServletContainerInitializer를 안다
- 그런데 어떤 WebApplicationInitializer를 실행해야 하지?
- 하나? 여러 개?
- 라이브러리도 포함해야 하나?
```

이걸 Tomcat이 판단하는 건 불가능.

그래서:

> “내가 기준만 줄 테니
> **관련 클래스 전부 가져가서 너희가 알아서 실행해라**”

이게 `set`의 존재 이유야.

---

## 7. 네 코드 기준으로 정확히 말하면

```java
Set<Class<?>> set = [AppInitServlet1]
```

이 의미는:

> “이 웹앱에는
> AppInit 역할을 하는 초기화 클래스가 AppInitServlet1 하나 있다”

그리고 네가 하는 이 코드:

```java
appInit.onStartup(servletContext);
```

의 의미는:

> “이 웹앱의 초기화 책임을
> AppInitServlet1에게 넘긴다”

---

## 8. DispatcherServlet과의 관계 한 줄 요약

```
ServletContainerInitializer
  → set 수집
    → AppInit 실행
      → DispatcherServlet 등록
        → 요청 처리 시작
```

---

## 9. 핵심 문장 하나로 정리

> `set`은 “실행 대상 목록”이고,
> 7단계는 “웹앱이 자기 구조를 스스로 조립하는 시간”이다.

---

다음으로 이어가면 딱 좋은 주제는:

* 왜 `set`이 **Class 객체**이지 인스턴스가 아닌지
* Spring의 `WebApplicationInitializer`는 왜 interface인지
* 초기화 단계에서 **절대 하면 안 되는 것들**

원하면 그 중 하나 바로 이어서 설명해줄게.

질문이 아주 정확해.
이 지점은 **“메타데이터 → 실행 코드”로 넘어가는 경계**라서 헷갈리는 게 정상이고, 이해하면 전체 구조가 또렷해져.

결론부터 말하면:

**캐스팅과 `newInstance()`를 하는 이유는
`set` 안에 들어 있는 건 ‘실행 가능한 객체’가 아니라
‘실행 후보에 대한 메타정보(Class)’이기 때문이야.**

아래를 단계적으로 보자.

---

## 1. `Set<Class<?>>`의 정체부터 정확히 짚자

Tomcat이 너에게 넘겨준 것은 이거야:

```java
Set<Class<?>>
```

이 의미는:

> “AppInit과 관련된 **클래스 정보들**”

여기서 핵심:

* 객체 ❌
* 인스턴스 ❌
* 실행 중인 코드 ❌
  → **클래스 메타데이터만 있음**

Tomcat은 **절대 인스턴스를 만들어주지 않는다.**

---

## 2. 왜 Tomcat이 인스턴스를 안 만들어줄까?

Tomcat 입장에서 보면:

* 생성자가 어떤지 모름
* 파라미터가 필요한지 모름
* 싱글톤인지 아닌지 모름
* 몇 번 생성해야 하는지도 모름

즉,

> “이건 네(프레임워크/앱)가 책임질 문제지
> 내가 객체를 만들 수는 없다”

그래서 Tomcat은 **딱 여기까지만** 해준다:

```
“관련된 클래스들 목록” 전달
```

---

## 3. 그래서 왜 `newInstance()`를 하는가

이제 네 코드 입장에서 보면:

```java
Class<?> aClass
```

이건 그냥 이런 정보 덩어리야:

* 클래스 이름
* 메서드 시그니처
* 생성자 정보
* 인터페이스 구현 여부

👉 **실행은 못 한다**

그래서 다음이 필요해:

```java
aClass.getDeclaredConstructor().newInstance();
```

이 순간에 벌어지는 일은:

> “이 메타데이터를 기반으로
> 실제 실행 가능한 객체를 하나 만든다”

즉:

* 메타데이터 → 인스턴스
* 설계도 → 실제 물건

---

## 4. 그럼 왜 캐스팅이 필요한가?

```java
AppInit appInit = (AppInit) aClass.getDeclaredConstructor().newInstance();
```

이 부분의 의미를 정확히 풀면:

1. `newInstance()`의 반환 타입은?

```java
Object
```

2. 그런데 너는 이걸 이렇게 쓰고 싶어:

```java
appInit.onStartup(servletContext);
```

3. 그러려면:

```java
AppInit 타입
```

이어야 한다.

그래서 캐스팅이 필요한 거야.

중요한 포인트:

* **이미 @HandlesTypes(AppInit.class)로 필터링된 클래스들이기 때문에**
* 이 캐스팅은 “논리적으로 안전”하다

---

## 5. “메타데이터라서 그런 거야?”라는 질문에 대한 정확한 답

👉 **절반은 맞고, 절반은 더 깊은 이유가 있다.**

### 맞는 부분

* `Class<?>`는 JVM 레벨의 메타데이터다
* 실행 주체가 아니다

### 더 중요한 이유

* **컨테이너와 애플리케이션의 책임 분리를 위해서**
* **생성 전략을 프레임워크에 맡기기 위해서**

Spring은 여기서:

* 직접 `new` 할 수도 있고
* 나중에 DI 컨테이너로 넘길 수도 있고
* 프록시를 만들 수도 있다

Tomcat이 객체를 만들어버리면
이 모든 자유도가 사라진다.

---

## 6. 한 줄로 구조를 다시 정리하면

```
Tomcat: 클래스 목록만 줄게 (메타데이터)
Spring/앱: 그걸로 객체 만들고 실행할게
```

그래서:

* `Class<?>`를 주고
* `newInstance()`를 네가 호출하고
* `AppInit`으로 캐스팅하는 구조가 된 거야

---

## 7. 이 구조가 아주 중요한 이유

이 패턴은 여기서 끝이 아니야:

* Spring Bean 생성
* @Configuration 클래스 처리
* @Controller 스캔
* JPA Entity 등록

전부 다:

```
메타데이터 스캔
 → Class
   → 인스턴스화
     → 실행
```

이 흐름의 **첫 번째 실전 사례**가 지금 네 코드야.

---

## 8. 핵심 문장 하나로 딱 정리

> 캐스팅과 인스턴스 생성은
> “메타데이터로 발견된 클래스를
> 실행 가능한 코드로 바꾸는 의식”이다.

---

다음으로 자연스럽게 이어지는 질문은 보통 이거야:

* 왜 기본 생성자가 강제되는가
* 왜 static 메서드가 아니라 인스턴스 메서드인가
* 왜 Spring은 여기서 직접 new 하지 않고 다른 방식을 쓰는가

원하면 그중 하나 바로 파고들자.

잘 됐어요. 이 로그가 뜬 순간 **모든 퍼즐이 정확히 맞아떨어진 상태**입니다.
이제 “왜 이렇게 해야 했는지 / 왜 자동으로 안 됐는지”를 정리해 볼게요.

---

## 먼저, 지금 로그가 의미하는 것

```
ServletContainerInitializer1.onStartup
  ├─ AppInitServlet1.onStartup   ✔
  └─ AppInitSpring1.onStartup    ✔
```

이건 단순 성공 로그가 아닙니다. 의미는 이거예요.

> **Tomcat이 이제 ‘정상적인 웹 애플리케이션 구조(WEB-INF/classes + lib)’를 보고 있다**

그래서:

* SCI 로딩 ✔
* AppInit 구현체 발견 ✔
* 클래스 인스턴스 생성 ✔
* Spring 관련 클래스 로딩 ✔

---

## 그럼 질문: “왜 자동으로 안 됐을까?”

### 한 문장 요약

> **Spring은 웹 컨테이너에 ‘자동 배포’를 요구하지 않고,
> IDE도 “이 프로젝트를 웹앱으로 배포하라”는 신호를 받지 못했기 때문**입니다.

이걸 단계별로 풀면 이해가 됩니다.

---

## 1️⃣ Tomcat은 “Gradle 프로젝트”를 모른다

Tomcat이 아는 건 딱 이것뿐입니다.

```
- WAR 파일
- exploded WAR 디렉터리
```

Tomcat 입장에서:

```
build.gradle ? 모름
dependencies ? 모름
```

👉 **WEB-INF 구조만 봅니다.**

---

## 2️⃣ IntelliJ도 기본적으로는 “Java 앱”으로 취급한다

당신 프로젝트는 처음에:

```gradle
plugins {
    id 'java'
}
```

였고, 나중에 `war`를 추가했죠.

하지만 IntelliJ는 이렇게 생각합니다.

> “아, 이건 그냥 Java 프로젝트구나
> (웹앱으로 배포하라는 말은 안 들었네?)”

그래서:

* Artifact 자동 생성 ❌
* WEB-INF/lib 자동 구성 ❌
* Tomcat에 연결 ❌

---

## 3️⃣ Spring은 “자동으로 컨테이너에 얹히지 않는다”

중요한 철학 차이입니다.

### Spring (Framework)

> “웹 컨테이너는 네가 선택해
> 배포 방식도 네가 결정해”

그래서 Spring Framework는:

* web.xml
* ServletContainerInitializer
* WAR

같은 **표준 메커니즘만 제공**합니다.
**“자동 배포”는 하지 않습니다.**

---

## 4️⃣ “자동”을 해주는 건 Spring Boot다

많은 사람들이 헷갈리는 지점입니다.

### Spring Boot는 뭘 하냐면

```
- 내장 Tomcat 포함
- main() 하나로 서버 실행
- classpath 스캔
- Servlet 등록
- DispatcherServlet 자동 등록
```

즉:

> **Boot가 “배포자 + 컨테이너 + 초기화 코드”를 다 대신 해주는 것**

그래서:

* WAR 필요 없음
* WEB-INF 필요 없음
* Artifact 필요 없음

---

## 5️⃣ 지금 구조는 “프레임워크 내부를 그대로 드러낸 상태”

지금 당신이 한 건 이겁니다.

```
Spring Boot가 숨겨버린 내부 구조를
손으로 하나씩 조립
```

그래서 힘들었고, 대신 얻은 게 큽니다.

---

## 6️⃣ 왜 AppInit 구조가 필요한지도 이제 보인다

```
ServletContainerInitializer
        ↓
    AppInit (확장 포인트)
        ↓
  여러 초기화 모듈
```

이 구조는:

* Spring 내부
* Servlet 3.0+
* 많은 프레임워크

가 **실제로 쓰는 구조**입니다.

---

## 마지막으로 아주 중요한 한 문장

> **자동으로 안 된 게 아니라,
> 자동으로 해주는 계층(Spring Boot)을 쓰지 않았기 때문에 수동으로 한 것**입니다.

---

이제 이 상태에서:

* Controller 하나 추가해보기
* `/sp/hello` 요청 흘러가는 경로 보기
* Spring Boot와 지금 구조 1:1로 대응시키기

이 중 하나 해보면,
오늘 겪은 혼란이 **“아, 그래서 Boot가 편한 거구나”**로 정리될 거예요.

이 메시지는 **Spring 문제도 아니고 컨트롤러 문제도 아닙니다.**
이번에는 **서블릿 매핑 규칙(Servlet spec)**에 정확히 걸린 상황이에요.

---

## 핵심 원인 한 줄

> **DispatcherServlet을 `/sp`로 매핑했기 때문에
> `/sp/test2`는 애초에 DispatcherServlet까지 도달하지 않습니다.**

그래서 Tomcat이 이렇게 말합니다.

```
요청된 리소스 [/sp/test2]은(는) 가용하지 않습니다.
```

이 문구는 **Spring이 아니라 Tomcat이 내는 404**입니다.

---

## 왜 이런 일이 생길까? (가장 중요한 포인트)

지금 코드:

```java
registration.addMapping("/sp");
```

이건 Servlet 스펙상 **“정확 매핑(exact match)”** 입니다.

### Servlet 매핑 규칙 요약

| 매핑      | 의미             |
| ------- | -------------- |
| `/sp`   | 정확히 `/sp`만     |
| `/sp/*` | `/sp` 이하 모든 경로 |
| `/`     | 모든 요청          |
| `*.do`  | 확장자 매핑         |

---

## 지금 요청 흐름을 정확히 그리면

### 현재 설정

```
DispatcherServlet → "/sp"
```

### 요청

```
GET /sp/test2
```

### Tomcat 판단

```
"/sp/test2" ≠ "/sp"
→ 매칭되는 Servlet 없음
→ Tomcat 404 (가용하지 않습니다)
```

그래서 **Spring 로그조차 안 찍힙니다.**

---

## 그래서 이전에 보던 로그와 지금 로그가 다른 이유

### 이전

```
No mapping for GET
```

→ DispatcherServlet까지는 들어옴
→ Spring 내부에서 컨트롤러 못 찾음

### 지금

```
요청된 리소스는 가용하지 않습니다
```

→ DispatcherServlet **진입 자체가 안 됨**
→ Tomcat 단계에서 컷

---

## 해결 방법 (정답은 하나)

### DispatcherServlet 매핑을 이렇게 바꾸세요

```java
registration.addMapping("/sp/*");
```

---

## 수정된 코드 (정확한 정답)

```java
private void addSpring(ServletContext servletContext) {
    AnnotationConfigWebApplicationContext context =
            new AnnotationConfigWebApplicationContext();
    context.register(SpringConfig1.class);

    DispatcherServlet dispatcher1 = new DispatcherServlet(context);

    ServletRegistration.Dynamic registration =
            servletContext.addServlet("dispatcher1", dispatcher1);

    // ⭐ 핵심
    registration.addMapping("/sp/*");
}
```

---

## 이제 요청 흐름은 이렇게 됩니다

```
GET /sp/test2
 → Tomcat
   → DispatcherServlet
     → HandlerMapping
       → @GetMapping("/test2")
         → SpringController1.run()
```

브라우저 출력:

```
test2
```

---

## 자주 헷갈리는 포인트 정리

* `/sp`  ❌  → 하위 경로 매칭 안 됨
* `/sp/*` ✅ → Spring MVC에서 거의 항상 이걸 씀
* Spring Boot에서 이걸 신경 안 써도 되는 이유
  → Boot가 기본으로 `/` 매핑을 써 주기 때문

---

## 최종 한 문장 정리

> 지금 404는 Spring 설정 문제가 아니라,
> **DispatcherServlet을 `/sp`로 “정확 매핑”해 놓고 `/sp/test2`를 호출했기 때문에 발생한 Tomcat 단계의 404**다.

여기까지 이해했으면
이제 Spring MVC 동작 원리를 **완전히 한 바퀴 돈 것**입니다.

1. 전통적인 Java 웹 애플리케이션과 스프링 부트의 배포 방식에서 가장 큰 차이점은 무엇일까요?

A
사용하는 프로그래밍 언어

외부 WAS 필요성 여부

C
데이터베이스 사용 여부

D
개발 도구 종류



해설
스프링 부트는 WAS를 내장하여 JAR 파일로 자체 실행 가능하지만, 전통 방식은 별도의 외부 WAS 설치 및 배포가 필요해요. 이 부분이 핵심적인 차이점입니다.
2. 톰캣과 같은 전통적인 WAS에 배포되는 표준 웹 애플리케이션 아카이브 파일 형식은 무엇인가요?

A
JAR

WAR

C
EAR

D
ZIP



해설
전통적인 웹 애플리케이션은 여러 자원과 클래스를 묶어 WAR 파일 형태로 만들어 WAS에 배포해요. JAR은 일반 Java 실행 파일이나 라이브러리에 주로 쓰입니다.
3. `jakarta.servlet.ServletContainerInitializer` 인터페이스의 주된 목적은 무엇인가요?

A
HTTP 요청을 처리하기 위해

B
WAS 종료 시 로직 실행

서블릿 컨테이너 초기화 시 로직 실행

D
데이터베이스 연결 관리



해설
이 인터페이스는 WAS가 시작될 때 서블릿 컨테이너 초기화 시점에 특정 로직을 실행할 수 있도록 하는 표준 메커니즘입니다. 애플리케이션 초기화에 활용됩니다.
4. 서블릿 컨테이너가 `ServletContainerInitializer` 구현체를 WAS 시작 시점에 찾으려면, 해당 구현체의 클래스 이름을 어디에 명시해야 할까요?

A
web.xml 파일

B
MANIFEST.MF 파일

src/main/resources/META-INF/services/jakarta.servlet.ServletContainerInitializer 파일

D
application.yml 파일



해설
서블릿 스펙에 따라, WAS는 `META-INF/services` 디렉토리 아래에 있는 `jakarta.servlet.ServletContainerInitializer` 이름의 파일을 읽고, 그 안에 명시된 클래스들을 초기화 구현체로 인식하여 실행합니다.
5. 애노테이션 방식과 비교했을 때, `ServletContext`를 통한 서블릿의 프로그래밍 방식 등록의 주요 이점은 무엇인가요?

A
더 간결한 코드 작성

B
컴파일 시간 검증

C
자동 의존성 주입 지원

초기화 시점의 유연한 동적 등록



해설
애노테이션은 정적이지만, 프로그래밍 방식은 WAS 시작 시 초기화 로직 내에서 조건에 따라 서블릿을 등록하거나 설정을 변경하는 등 동적인 제어가 가능합니다. 유연성이 장점이죠.
6. `ServletContainerInitializer` 구현체의 `@HandleTypes` 애노테이션은 어떤 역할을 하나요?

A
WAS 설정 파일 위치 지정

특정 인터페이스 구현체나 클래스 정보를 찾아 전달

C
HTTP 요청 매핑 규칙 정의

D
서블릿 실행 스레드 관리



해설
`@HandleTypes`에 지정된 인터페이스나 클래스를 구현하는 모든 클래스 정보를 WAS가 수집하여, `ServletContainerInitializer.onStartup()` 메소드의 매개변수로 전달해 줍니다.
7. 표준 WAR 파일 구조에서 애플리케이션의 컴파일된 Java 클래스(.class) 파일들이 위치하는 주요 디렉토리는 어디인가요?

A
WEB-INF/lib

WEB-INF/classes

C
webapp

D
META-INF



해설
`WEB-INF/classes` 디렉토리 안에는 애플리케이션의 핵심인 컴파일된 클래스가 위치합니다. `lib`는 라이브러리, `webapp`은 정적 자원, `META-INF`는 메타 정보입니다.
8. WAR 파일을 톰캣의 `webapps` 디렉토리에 복사했을 때, 톰캣이 자동으로 수행하는 기본 동작은 무엇인가요?

A
WAR 파일 암호화

WAR 파일 압축 해제 및 배포

C
WAR 파일 실행 권한 설정

D
WAR 파일 바이러스 검사



해설
톰캣은 `webapps` 디렉토리에 새로운 WAR 파일이 추가되면 이를 자동으로 인식하고 압축을 해제하여 웹 애플리케이션 형태로 배포하고 실행합니다.
9. 수동 설정 환경에서, 서블릿 컨테이너로부터 요청을 받아 스프링 컨테이너 안의 컨트롤러로 라우팅하는 역할을 하는 스프링 MVC 컴포넌트는 무엇인가요?

A
ApplicationContext

B
BeanFactory

DispatcherServlet

D
AnnotationConfigWebApplicationContext



해설
DispatcherServlet은 스프링 MVC의 핵심 서블릿으로, 서블릿 컨테이너로부터 요청을 받아 적절한 스프링 컨트롤러에게 라우팅하고 처리를 위임하는 게이트웨이 역할을 합니다.
10. 스프링 MVC에서 서블릿 컨테이너 초기화 과정을 추상화하여 개발자가 애플리케이션 초기화 코드 작성에 집중하도록 돕는 핵심 인터페이스는 무엇인가요?

A
ServletContext

B
ServletContainerInitializer

WebApplicationInitializer

D
ApplicationListener



해설
Spring MVC는 내부적으로 `ServletContainerInitializer`를 사용하여 `WebApplicationInitializer` 인터페이스 구현체를 찾아 실행합니다. 개발자는 이 인터페이스만 구현하면 초기화 로직을 작성할 수 있습니다.