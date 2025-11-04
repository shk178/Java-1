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

# Spring 빈 등록 방법: 컴포넌트 스캔 vs 자바 코드 직접 등록

## 1. 개요

Spring에서 빈(Bean)을 등록하는 방법은 크게 두 가지가 있습니다:
1. 컴포넌트 스캔: `@Component`, `@Service`, `@Repository` 어노테이션을 사용하여 자동 등록
2. 자바 코드 직접 등록: `@Configuration` 클래스에서 `@Bean` 메서드로 수동 등록

현재 프로젝트의 `MemberService`와 `MemoryMemberRepository`를 Spring 빈으로 등록하는 두 가지 방법을 설명합니다.

---

## 2. 현재 상태

### 2.1 현재 코드 상태

```1:38:src/main/java/hello/hello_spring/service/MemberService.java
package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import java.util.List;
import java.util.Optional;

public class MemberService {
    private final MemberRepository memberRepository;
    
    //회원 서비스 코드를 DI 가능하도록
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    //회원 가입
    public Long join(Member member) {
        validateDuplicateMember(member); //중복 검증
        memberRepository.save(member);
        return member.getId();
    }
    
    private void validateDuplicateMember(Member member) {
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원");
                });
    }
    
    //전체 회원 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
    
    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}
```

```1:43:src/main/java/hello/hello_spring/repository/MemoryMemberRepository.java
package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

//동시성 문제 해결 안 됨
//실무에서는 ConcurrentHashMap, AtomicLong 사용 고려
public class MemoryMemberRepository implements MemberRepository {
    private static Map<Long, Member> store = new HashMap<>();
    private static long sequence = 0L;
    
    @Override
    public Member save(Member member) {
        member.setId(++sequence);
        store.put(member.getId(), member);
        return member;
    }
    
    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }
    
    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }
    
    @Override
    public Optional<Member> findByName(String name) {
        return store.values().stream()
                .filter(member -> member.getName().equals(name))
                .findAny();
    }
    
    public void clearStore() {
        store.clear();
    }
}
```

현재 상태: 일반 자바 클래스로, Spring 빈으로 등록되지 않음

### 2.2 Spring Boot 애플리케이션 클래스

```1:13:src/main/java/hello/hello_spring/HelloSpringApplication.java
package hello.hello_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HelloSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloSpringApplication.class, args);
	}

}
```

`@SpringBootApplication`의 역할:
- `@ComponentScan`을 포함: `hello.hello_spring` 패키지와 하위 패키지를 자동 스캔
- `@Configuration` 포함: 설정 클래스로 인식
- `@EnableAutoConfiguration` 포함: 자동 설정 활성화

---

## 3. 방법 1: 컴포넌트 스캔을 통한 자동 빈 등록

### 3.1 작동 원리

컴포넌트 스캔은 Spring이 특정 어노테이션이 붙은 클래스를 자동으로 찾아서 빈으로 등록하는 기능입니다.

스캔 대상 어노테이션:
- `@Component`: 일반 컴포넌트
- `@Service`: 비즈니스 로직 서비스 계층
- `@Repository`: 데이터 접근 계층
- `@Controller`: 웹 MVC 컨트롤러 계층

### 3.2 구현 방법

#### Step 1: MemberService에 @Service 추가

```java
package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service  // ← 추가: Spring이 자동으로 빈으로 등록
public class MemberService {
    private final MemberRepository memberRepository;
    
    // 생성자 주입: Spring이 자동으로 MemberRepository 빈을 주입
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    // ... 나머지 코드 동일
}
```

#### Step 2: MemoryMemberRepository에 @Repository 추가

```java
package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository  // ← 추가: Spring이 자동으로 빈으로 등록
public class MemoryMemberRepository implements MemberRepository {
    // ... 나머지 코드 동일
}
```

### 3.3 의존성 주입 자동 처리

동작 과정:
1. Spring이 `@Service`가 붙은 `MemberService`를 발견
2. `MemberService`의 생성자를 확인
3. 생성자 파라미터 타입(`MemberRepository`)의 빈을 찾음
4. `@Repository`가 붙은 `MemoryMemberRepository`를 주입
5. `MemberService` 빈 생성 완료

의존성 주입 흐름:
```
@SpringBootApplication
    ↓
@ComponentScan (hello.hello_spring 패키지 스캔)
    ↓
@Repository 발견 → MemoryMemberRepository 빈 등록
    ↓
@Service 발견 → MemberService 빈 등록
    ↓
MemberService 생성자 확인 → MemberRepository 타입의 빈 필요
    ↓
MemoryMemberRepository 주입 (생성자 주입)
```

### 3.4 컴포넌트 스캔 범위

기본 스캔 위치: `@SpringBootApplication`이 있는 패키지와 하위 패키지

```
hello.hello_spring
├── HelloSpringApplication.java (@SpringBootApplication)
├── service
│   └── MemberService.java (@Service) ✓ 스캔됨
└── repository
    └── MemoryMemberRepository.java (@Repository) ✓ 스캔됨
```

스캔 범위 밖의 패키지는 스캔되지 않습니다:
```
com.other.package
└── SomeService.java (@Service) ✗ 스캔 안 됨
```

### 3.5 장점과 단점

장점:
- 간단하고 빠름: 어노테이션만 추가하면 됨
- 코드가 깔끔함: 설정 파일이 필요 없음
- 자동화: Spring이 의존성을 자동으로 찾아서 주입

단점:
- 제어가 제한적: 빈 생성 과정을 세밀하게 제어하기 어려움
- 테스트 시 교체 어려움: 구현체를 쉽게 바꾸기 어려움

---

## 4. 방법 2: 자바 코드로 직접 스프링 빈 등록

### 4.1 작동 원리

`@Configuration` 클래스에서 `@Bean` 메서드를 정의하여 수동으로 빈을 등록합니다. 이 방법은 빈 생성 과정을 완전히 제어할 수 있습니다.

### 4.2 구현 방법

#### Step 1: SpringConfig 클래스 생성

`src/main/java/hello/hello_spring/SpringConfig.java` 파일 생성:

```java
package hello.hello_spring;

import hello.hello_spring.repository.MemberRepository;
import hello.hello_spring.repository.MemoryMemberRepository;
import hello.hello_spring.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // Spring 설정 클래스임을 명시
public class SpringConfig {
    
    @Bean  // 이 메서드가 반환하는 객체를 Spring 빈으로 등록
    public MemberService memberService() {
        return new MemberService(memberRepository());  // 의존성 주입
    }
    
    @Bean  // 이 메서드가 반환하는 객체를 Spring 빈으로 등록
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
}
```

#### Step 2: MemberService와 MemoryMemberRepository는 일반 클래스로 유지

MemberService.java (어노테이션 없음):
```java
package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import java.util.List;
import java.util.Optional;

public class MemberService {  // @Service 어노테이션 없음
    private final MemberRepository memberRepository;
    
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    // ... 나머지 코드 동일
}
```

MemoryMemberRepository.java (어노테이션 없음):
```java
package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemoryMemberRepository implements MemberRepository {  // @Repository 어노테이션 없음
    // ... 나머지 코드 동일
}
```

### 4.3 빈 등록 과정

동작 과정:
1. Spring이 `@Configuration` 클래스를 발견
2. `@Bean` 메서드를 확인
3. `memberRepository()` 메서드 실행 → `MemoryMemberRepository` 인스턴스 생성
4. `memberService()` 메서드 실행 → `memberRepository()` 호출하여 의존성 주입
5. 각각을 Spring 빈으로 등록

의존성 주입 흐름:
```
@Configuration
    ↓
@Bean memberRepository() → MemoryMemberRepository 빈 등록
    ↓
@Bean memberService() → MemberService 빈 등록
    ↓
memberService() 내부에서 memberRepository() 호출
    ↓
의존성 주입 완료
```

### 4.4 빈 이름

`@Bean` 메서드 이름이 빈 이름이 됩니다:
- `memberService()` → 빈 이름: "memberService"
- `memberRepository()` → 빈 이름: "memberRepository"

빈 이름 변경:
```java
@Bean(name = "myMemberService")  // 빈 이름 지정
public MemberService memberService() {
    return new MemberService(memberRepository());
}
```

### 4.5 장점과 단점

장점:
- 완전한 제어: 빈 생성 과정을 세밀하게 제어 가능
- 구현체 교체 용이: 설정만 변경하면 다른 구현체로 교체 가능
- 테스트 용이: Mock 객체나 테스트용 구현체로 쉽게 교체
- 명시적: 어떤 빈이 등록되는지 명확히 알 수 있음

단점:
- 설정 파일 필요: 추가 설정 클래스가 필요
- 코드가 늘어남: 빈 개수가 많아지면 설정 코드가 많아짐

---

## 5. 두 방법 비교

### 5.1 비교표

| 항목 | 컴포넌트 스캔 | 자바 코드 직접 등록 |
|------|--------------|-------------------|
| 설정 방법 | 어노테이션만 추가 | @Configuration 클래스 필요 |
| 코드 양 | 적음 | 많음 |
| 제어 정도 | 낮음 | 높음 |
| 구현체 교체 | 어려움 | 쉬움 |
| 테스트 용이성 | 보통 | 높음 |
| 가독성 | 높음 | 중간 |
| 실무 활용 | 일반적 | 설정이 복잡할 때 |

### 5.2 언제 어떤 방법을 사용할까?

컴포넌트 스캔을 사용하는 경우:
- 간단한 프로젝트
- 구현체를 바꿀 일이 없는 경우
- 빠른 개발이 필요한 경우
- 표준적인 Spring 애플리케이션 구조

자바 코드 직접 등록을 사용하는 경우:
- 구현체를 바꿔야 할 가능성이 있는 경우 (예: 개발/운영 환경별 다른 구현체)
- 테스트 코드에서 Mock 객체를 주입해야 하는 경우
- 빈 생성 과정에 특별한 로직이 필요한 경우
- 기존 라이브러리 클래스를 빈으로 등록해야 하는 경우

---

## 6. 실제 적용 예시

### 6.1 컴포넌트 스캔 방식 적용

MemberService.java:
```java
package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service  // ← 추가
public class MemberService {
    private final MemberRepository memberRepository;
    
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    // ... 나머지 코드
}
```

MemoryMemberRepository.java:
```java
package hello.hello_spring.repository;

import hello.hello_spring.domain.Member;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository  // ← 추가
public class MemoryMemberRepository implements MemberRepository {
    // ... 나머지 코드
}
```

컨트롤러에서 사용:
```java
@Controller
public class MemberController {
    private final MemberService memberService;
    
    // 생성자 주입: Spring이 자동으로 MemberService 빈을 주입
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
}
```

### 6.2 자바 코드 직접 등록 방식 적용

SpringConfig.java 생성:
```java
package hello.hello_spring;

import hello.hello_spring.repository.MemberRepository;
import hello.hello_spring.repository.MemoryMemberRepository;
import hello.hello_spring.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {
    
    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }
    
    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();
    }
}
```

컨트롤러에서 사용 (동일):
```java
@Controller
public class MemberController {
    private final MemberService memberService;
    
    // 생성자 주입: Spring이 자동으로 MemberService 빈을 주입
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
}
```

---

## 7. 구현체 교체 예시 (자바 코드 직접 등록의 장점)

### 7.1 개발 환경: MemoryMemberRepository

```java
@Configuration
public class SpringConfig {
    
    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }
    
    @Bean
    public MemberRepository memberRepository() {
        return new MemoryMemberRepository();  // 메모리 기반
    }
}
```

### 7.2 운영 환경: JdbcMemberRepository로 변경

```java
@Configuration
public class SpringConfig {
    
    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository());
    }
    
    @Bean
    public MemberRepository memberRepository() {
        // return new MemoryMemberRepository();  // 주석 처리
        return new JdbcMemberRepository(dataSource);  // DB 기반으로 변경
    }
}
```

장점: `MemberService` 코드는 전혀 변경하지 않고, 설정만 변경하면 됩니다!

---

## 8. 의존성 주입 방식

두 방법 모두 Spring의 의존성 주입을 사용합니다. 주입 방식은 세 가지가 있습니다:

### 8.1 생성자 주입 (권장)

```java
@Controller
public class MemberController {
    private final MemberService memberService;
    
    // 생성자 주입: Spring이 자동으로 주입
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
}
```

장점:
- 필수 의존성을 명확히 표현
- final로 불변성 보장
- 테스트 시 Mock 객체 주입 용이

### 8.2 필드 주입 (비권장)

```java
@Controller
public class MemberController {
    @Autowired
    private MemberService memberService;  // 비권장
}
```

단점:
- 테스트 시 주입이 어려움
- final 불가능 (불변성 보장 불가)

### 8.3 Setter 주입

```java
@Controller
public class MemberController {
    private MemberService memberService;
    
    @Autowired
    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }
}
```

단점:
- 선택적 의존성에만 사용 권장
- final 불가능

---

## 9. 빈 등록 확인 방법

### 9.1 애플리케이션 실행 시 로그 확인

애플리케이션을 실행하면 다음과 같은 로그가 출력됩니다:

```
Creating shared instance of singleton bean 'memberService'
Creating shared instance of singleton bean 'memberRepository'
```

### 9.2 컨트롤러에서 확인

```java
@Controller
public class MemberController {
    private final MemberService memberService;
    
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
        System.out.println("memberService = " + memberService.getClass());
        // 출력: memberService = class hello.hello_spring.service.MemberService$$SpringCGLIB$$0
    }
}
```

`$$SpringCGLIB$$0`이 붙은 것은 Spring이 생성한 프록시 객체입니다.

---

## 10. 요약

### 컴포넌트 스캔 방식
- `@Service`, `@Repository` 어노테이션 추가
- Spring이 자동으로 빈 등록 및 의존성 주입
- 간단하고 빠름

### 자바 코드 직접 등록 방식
- `@Configuration` 클래스에서 `@Bean` 메서드로 등록
- 빈 생성 과정을 완전히 제어 가능
- 구현체 교체가 용이

### 선택 기준
- 컴포넌트 스캔: 일반적인 개발, 빠른 프로토타이핑
- 자바 코드 직접 등록: 설정이 복잡하거나 구현체 교체가 필요한 경우

두 방법 모두 Spring의 의존성 주입을 활용하며, 생성자 주입 방식을 권장합니다.
