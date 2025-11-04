# main 메서드 없이 테스트 코드가 실행되는 원리

## 1. 개요

일반적인 자바 애플리케이션은 `main()` 메서드가 프로그램의 진입점(Entry Point)입니다. 하지만 JUnit 테스트 코드에는 `main()` 메서드가 없는데도 테스트가 실행됩니다. 이는 JUnit 프레임워크가 테스트 러너(Test Runner)를 통해 테스트를 발견하고 실행하기 때문입니다.

---

## 2. JUnit의 테스트 실행 메커니즘

### 2.1 테스트 러너(Test Runner)의 역할

JUnit은 내부적으로 테스트 러너라는 컴포넌트가 있어서:
1. 테스트 클래스를 찾아서 로드
2. `@Test` 어노테이션이 붙은 메서드를 발견
3. 각 테스트 메서드를 실행
4. 결과를 수집하고 보고

실제로는 JUnit이 자체적으로 main 메서드를 가지고 있고, 이것이 테스트를 실행하는 진입점입니다.

### 2.2 리플렉션(Reflection)을 통한 테스트 발견

JUnit은 리플렉션(Reflection)을 사용하여 테스트 클래스와 메서드를 동적으로 발견합니다:

```java
// JUnit 내부 동작 방식 (의사 코드)
public class JUnitTestRunner {
    public static void main(String[] args) {
        // 1. 클래스패스에서 @Test 어노테이션이 있는 메서드 찾기
        Class<?> testClass = Class.forName("MemberServiceTest");
        
        // 2. 리플렉션으로 모든 메서드 조회
        Method[] methods = testClass.getDeclaredMethods();
        
        // 3. @Test 어노테이션이 붙은 메서드만 필터링
        for (Method method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                // 4. 테스트 메서드 실행
                Object instance = testClass.newInstance();
                
                // @BeforeEach 실행
                runBeforeEach(instance);
                
                // 실제 테스트 메서드 실행
                method.invoke(instance);
                
                // @AfterEach 실행
                runAfterEach(instance);
            }
        }
    }
}
```

### 2.3 어노테이션 기반 실행 흐름

JUnit은 어노테이션을 사용하여 테스트 라이프사이클을 관리합니다:

```
테스트 실행 흐름:
1. @BeforeAll (클래스 로드 시 한 번)
2. @BeforeEach (각 테스트 전)
3. @Test (실제 테스트 메서드)
4. @AfterEach (각 테스트 후)
5. @AfterAll (모든 테스트 완료 후 한 번)
```

---

## 3. IDE에서의 테스트 실행

### 3.1 IntelliJ IDEA / Eclipse / VS Code

IDE는 JUnit을 통합하여 테스트를 실행합니다:

1. IDE가 JUnit 라이브러리를 클래스패스에 추가
2. IDE가 테스트 클래스를 감지 (파일명이 `*Test.java` 또는 `@Test` 어노테이션 존재)
3. IDE가 내부적으로 JUnit 러너를 호출
4. JUnit 러너가 리플렉션으로 테스트 메서드 발견 및 실행

### 3.2 IDE의 "Run Test" 버튼 클릭 시

```
사용자가 "Run Test" 클릭
    ↓
IDE가 JUnit 러너 실행
    ↓
JUnit 러너가 리플렉션으로 테스트 클래스/메서드 발견
    ↓
테스트 메서드 실행
    ↓
결과를 IDE에 표시
```

---

## 4. Gradle 빌드 도구에서의 테스트 실행

### 4.1 Gradle의 테스트 실행 메커니즘

프로젝트의 `build.gradle` 파일을 확인하면:

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '...'
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter'
    // ...
}
```

Gradle이 `./gradlew test` 명령을 실행할 때:

1. Gradle이 JUnit 플러그인을 활성화
2. 테스트 태스크가 JUnit 플랫폼 러너를 호출
3. JUnit이 `src/test/java` 디렉토리의 테스트 클래스를 스캔
4. 리플렉션으로 테스트 메서드 발견 및 실행
5. 결과를 HTML/XML 리포트로 생성

### 4.2 Gradle의 테스트 실행 흐름

```bash
$ ./gradlew test
```

실행 시 내부 동작:

```
Gradle Test Task 실행
    ↓
JUnit Platform Launcher 호출
    ↓
클래스패스에서 테스트 클래스 스캔
    ↓
리플렉션으로 @Test 메서드 발견
    ↓
테스트 실행 (JUnit 러너가 main 역할)
    ↓
테스트 결과 수집 및 리포트 생성
```

---

## 5. JUnit의 실제 진입점

### 5.1 JUnit Platform Launcher

JUnit 5는 JUnit Platform이라는 새로운 아키텍처를 사용합니다:

- JUnit Platform Launcher: 테스트 실행을 위한 공통 API
- JUnit Jupiter: JUnit 5의 테스트 엔진
- JUnit Vintage: JUnit 4 호환성 제공

### 5.2 JUnit의 내부 main 메서드

JUnit 라이브러리 내부에는 실제로 main 메서드가 있습니다:

```java
// JUnit Platform 내부 (간소화된 설명)
public class JUnitPlatform {
    public static void main(String[] args) {
        // 테스트 클래스들을 찾아서 실행
        TestEngine engine = new JupiterTestEngine();
        engine.discover(...);
        engine.execute(...);
    }
}
```

이 main 메서드는:
- JUnit 라이브러리 내부에 존재
- IDE나 빌드 도구가 이를 호출
- 우리가 작성한 테스트 코드에는 필요 없음

---

## 6. 리플렉션을 통한 테스트 메서드 발견 과정

### 6.1 단계별 동작

테스트 클래스 예시:

```java
class MemberServiceTest {
    @BeforeEach
    public void beforeEach() { ... }
    
    @Test
    public void 회원가입() { ... }
    
    @Test
    public void 중복회원_예외() { ... }
    
    @AfterEach
    public void afterEach() { ... }
}
```

JUnit이 이 클래스를 처리하는 과정:

1. 클래스 로딩
   ```java
   Class<?> testClass = Class.forName("hello.hello_spring.service.MemberServiceTest");
   ```

2. 메서드 스캔
   ```java
   Method[] methods = testClass.getDeclaredMethods();
   // 모든 메서드 조회: beforeEach, 회원가입, 중복회원_예외, afterEach
   ```

3. 어노테이션 필터링
   ```java
   for (Method method : methods) {
       if (method.isAnnotationPresent(Test.class)) {
           // @Test가 붙은 메서드만 실행 목록에 추가
           testMethods.add(method);
       }
       if (method.isAnnotationPresent(BeforeEach.class)) {
           beforeEachMethods.add(method);
       }
       // ...
   }
   ```

4. 인스턴스 생성 및 실행
   ```java
   Object instance = testClass.newInstance();
   
   // @BeforeEach 실행
   for (Method beforeEach : beforeEachMethods) {
       beforeEach.invoke(instance);
   }
   
   // @Test 실행
   for (Method testMethod : testMethods) {
       testMethod.invoke(instance);
   }
   
   // @AfterEach 실행
   for (Method afterEach : afterEachMethods) {
       afterEach.invoke(instance);
   }
   ```

### 6.2 리플렉션이 필요한 이유

- 동적 발견: 컴파일 시점에 모든 테스트를 알 수 없음
- 유연성: 새로운 테스트 메서드를 추가해도 자동으로 발견
- 확장성: 커스텀 어노테이션으로 테스트 전략 확장 가능

---

## 7. 실제 테스트 실행 예시

### 7.1 테스트 클래스 실행 시나리오

```java
class MemoryMemberRepositoryTest {
    MemoryMemberRepository repository = new MemoryMemberRepository();
    
    @AfterEach
    public void afterEach() {
        repository.clearStore();
    }
    
    @Test
    public void save() { ... }
    
    @Test
    public void findByName() { ... }
}
```

실행 순서:

```
1. JUnit 러너가 MemoryMemberRepositoryTest 클래스 로드
2. 인스턴스 생성 (repository 필드 초기화)
3. save() 테스트:
   - save() 실행
   - afterEach() 실행 (데이터 초기화)
4. findByName() 테스트:
   - findByName() 실행
   - afterEach() 실행 (데이터 초기화)
```

### 7.2 테스트 격리 메커니즘

각 테스트는 독립적인 인스턴스에서 실행됩니다:

```
테스트 1 실행:
  - 새로운 MemoryMemberRepositoryTest 인스턴스 생성
  - repository 필드 초기화
  - save() 실행
  - afterEach() 실행
  - 인스턴스 폐기

테스트 2 실행:
  - 새로운 MemoryMemberRepositoryTest 인스턴스 생성 (깨끗한 상태)
  - repository 필드 초기화
  - findByName() 실행
  - afterEach() 실행
  - 인스턴스 폐기
```

이렇게 각 테스트가 독립적으로 실행되므로 테스트 간 간섭이 없습니다.

---

## 8. JUnit vs 일반 Java 프로그램 비교

### 8.1 일반 Java 프로그램

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
```

실행 방식:
```bash
java HelloWorld
# → JVM이 main() 메서드를 찾아서 실행
```

### 8.2 JUnit 테스트 프로그램

```java
class HelloWorldTest {
    @Test
    public void test() {
        System.out.println("Hello World");
    }
}
```

실행 방식:
```bash
# IDE나 Gradle이 JUnit 러너를 호출
# → JUnit 러너가 리플렉션으로 @Test 메서드를 찾아서 실행
```

---

## 9. 핵심 개념 정리

### 9.1 왜 main 메서드가 필요 없나?

1. JUnit 라이브러리가 자체적으로 main 메서드를 가지고 있음
2. IDE나 빌드 도구가 JUnit 러너를 호출
3. 리플렉션을 통해 테스트 메서드를 동적으로 발견
4. 어노테이션 기반으로 테스트 라이프사이클 관리

### 9.2 테스트 실행의 진입점

| 실행 환경 | 진입점 | 설명 |
|---------|--------|------|
| 일반 Java 프로그램 | `main()` 메서드 | 개발자가 직접 작성 |
| JUnit 테스트 | JUnit 러너 | JUnit 라이브러리 내부에 존재 |
| IDE 실행 | IDE → JUnit 러너 | IDE가 러너를 호출 |
| Gradle 실행 | Gradle Test Task → JUnit 러너 | Gradle이 러너를 호출 |

### 9.3 리플렉션의 역할

- 클래스 로딩: 테스트 클래스를 동적으로 로드
- 메서드 발견: `@Test` 어노테이션이 붙은 메서드 찾기
- 메서드 실행: `method.invoke()`로 테스트 메서드 실행
- 어노테이션 처리: `@BeforeEach`, `@AfterEach` 등 라이프사이클 관리

---

## 10. 실제 확인 방법

### 10.1 Gradle로 테스트 실행 시

```bash
./gradlew test --info
```

출력 예시:
```
> Task :test
Executing test 'MemoryMemberRepositoryTest.save()'
Executing test 'MemoryMemberRepositoryTest.findByName()'
...
```

### 10.2 IDE에서 테스트 실행 시

- IntelliJ IDEA: 오른쪽 클릭 → "Run 'MemoryMemberRepositoryTest'"
- 실행 창에 "JUnit" 러너가 실행되는 것을 확인 가능

### 10.3 JUnit 내부 구조 확인

Maven/Gradle 의존성에서 확인:
```gradle
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-engine:5.9.2'
    // 이 라이브러리 안에 JUnit 러너가 포함되어 있음
}
```

---

## 11. 요약

### main 메서드 없이 테스트가 실행되는 이유

1. JUnit 프레임워크가 자체적으로 테스트 러너를 제공
2. 리플렉션을 통해 테스트 클래스와 메서드를 동적으로 발견
3. 어노테이션 기반으로 테스트 라이프사이클을 관리
4. IDE나 빌드 도구가 JUnit 러너를 호출하여 테스트 실행

### 핵심 개념

- 테스트 러너: JUnit이 제공하는 테스트 실행 엔진
- 리플렉션: 런타임에 클래스 정보를 동적으로 조회하고 실행
- 어노테이션: 테스트 메서드와 라이프사이클을 표시하는 메타데이터
- 진입점 분리: 일반 프로그램의 `main()` 대신 JUnit 러너가 진입점 역할

이러한 메커니즘 덕분에 개발자는 `main()` 메서드를 작성할 필요 없이 `@Test` 어노테이션만 붙이면 테스트가 자동으로 실행됩니다.
