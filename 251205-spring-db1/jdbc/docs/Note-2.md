아래는 세 가지의 차이를 핵심부터 정리한 뒤, 왜 출력 결과가 모두 H2의 `JdbcConnection`으로 나오는지도 설명한다.

---

# 1. DriverManager

**JDBC 표준이 제공하는 가장 기본적인 커넥션 획득 방식**

* `DriverManager.getConnection(url, user, pw)`
* 호출할 때마다 **새로운 실제 DB 커넥션**을 만든다.
* 스프링과는 무관한 **순수 JDBC 방식**
* 커넥션 풀 없음
* 트랜잭션 관리, 자원 자동 정리 등은 모두 직접 처리해야 함

즉,
`Connection con1 = DriverManager.getConnection(...);`
`Connection con2 = DriverManager.getConnection(...);`
→ 둘은 **완전히 별개의 실제 DB 커넥션**

---

# 2. DataSource (스프링이 사용하는 커넥션 획득 추상화)

* 스프링과 JDBC에서 공통적으로 사용하는 **커넥션 제공 인터페이스**
* 단순히 “커넥션을 만들거나 반환하는 방법을 추상화”한 것
* 자체 기능은 없음. 구현체에 따라 동작이 달라짐
* 예:

    * `DriverManagerDataSource` (매번 새 커넥션 생성)
    * `HikariDataSource` (커넥션 풀 제공)

즉, DataSource는 **역할(인터페이스)**만 정의하며 실제 동작은 구현 클래스가 결정.

---

# 3. DriverManagerDataSource (DataSource의 구현체 중 하나)

* 스프링에서 제공하는 매우 단순한 DataSource 구현체
* **매번 DriverManager를 이용해 새로운 커넥션을 만든다**
* 즉,
  `con3 = dataSource.getConnection()`
  `con4 = dataSource.getConnection()`
  → con3, con4도 각각 새로운 실제 DB 커넥션

✔ 즉, **DriverManager 방식과 본질적으로 동일**
✔ 단지 스프링에서 DataSource 인터페이스를 사용하기 위한 “연결어댑터” 역할

---

# 왜 con1~con4 모두 `JdbcConnection` 클래스인가?

* 어떤 방식을 쓰든 “H2가 제공하는 실제 JDBC 커넥션 타입”은 동일하기 때문
* DriverManager를 쓰든 DriverManagerDataSource를 쓰든 결국 **H2 드라이버가 Connection 객체를 만들어 반환**
* 구현체는 항상 `org.h2.jdbc.JdbcConnection`

---

# 요약 비교

| 방식                      | 커넥션 생성 방식                            | 커넥션 풀 여부   | 보통 사용 용도                |
| ----------------------- | ------------------------------------ | ---------- | ----------------------- |
| DriverManager           | 매번 새로운 커넥션 생성                        | 없음         | 가장 기본적인 JDBC 사용         |
| DataSource(인터페이스)       | 구현체에 따라 다름                           | 구현체에 따라 다름 | 스프링/프레임워크에서 표준화된 커넥션 방식 |
| DriverManagerDataSource | DriverManager를 내부에서 호출하여 매번 새 커넥션 생성 | 없음         | 테스트나 간단한 예제용 (실서비스 비권장) |

---

# 결론

* **DriverManager = 직접 JDBC 연결 요청**
* **DriverManagerDataSource = DriverManager를 감싼 스프링 DataSource 구현체 (매번 새로운 커넥션 생성)**
* **DataSource = 커넥션 추상화 인터페이스**

실무에서는
→ **DriverManagerDataSource 절대 사용하지 않음**
→ **커넥션 풀(DataSource: HikariCP 등)** 을 사용한다.

원하면 `HikariDataSource`를 이용한 커넥션 풀 동작 예제도 보여줄 수 있다.

`MemberRepository2`는 생성자에서 `DataSource`를 받고 있으므로 의존관계 주입(DI)을 하려면 **외부에서 DataSource 객체를 만들어서 넣어주면 된다.**
스프링 없이 순수 자바로도 가능하고, 스프링 컨테이너를 사용하여 자동으로 주입받을 수도 있다.

아래에서 두 방식 모두 보여준다.

---

# 1. 스프링 없이 순수 자바로 주입하는 방법

예를 들어 HikariCP를 쓴다고 하면:

```java
public class MemberRepositoryTest {
    public static void main(String[] args) throws SQLException {
        // DataSource 생성
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // Repository에 주입
        MemberRepository2 memberRepository = new MemberRepository2(dataSource);

        // 사용
        Member member = new Member("idA", 1000);
        memberRepository.save(member);
    }
}
```

이렇게 **직접 DataSource를 만들고 생성자를 통해 넣어주면** 의존관계 주입이 완료된다.

DriverManagerDataSource를 쓰는 경우:

```java
DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
MemberRepository2 repository = new MemberRepository2(dataSource);
```

---

# 2. 스프링 컨테이너 사용 시 (일반적인 실제 개발 방식)

스프링에서는 DataSource와 Repository를 스프링 빈으로 등록하고 **스프링이 자동으로 주입**하게 만든다.

예를 들어 `@Configuration`을 사용하면:

```java
@Configuration
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        return dataSource;
    }

    @Bean
    public MemberRepository2 memberRepository2() {
        return new MemberRepository2(dataSource());
    }
}
```

스프링이 실행되면:

1. DataSource 빈 생성
2. MemberRepository2 빈 생성 시 DataSource를 생성자에 넣어줌

즉, 자동으로 DI가 일어난다.

---

# 3. 스프링에서 Component Scan 사용하는 경우

`MemberRepository2`에 `@Repository`를 붙이고 스프링이 자동으로 스캔하도록 할 수도 있다:

```java
@Repository
public class MemberRepository2 {
    private final DataSource dataSource;

    public MemberRepository2(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```

그리고 `DataSource`만 Bean으로 등록해두면 된다:

```java
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();  // URL, USERNAME 설정 생략
    }
}
```

스프링은 `DataSource` 빈을 자동으로 찾아서 `MemberRepository2` 생성자에 넣어준다.

---

# 핵심 요약

`MemberRepository2`는 생성자에서 `DataSource`를 받으므로:

1. **순수 자바**
   → `new MemberRepository2(new DriverManagerDataSource(...))`

2. **스프링 DI**
   → DataSource를 Bean으로 등록
   → 스프링이 생성자에 자동으로 주입

결국 의존관계 주입은 **DataSource 객체를 만들어서 생성자에 넣어주는 것**이다.

원하면 Service 계층까지 DI 적용해서 전체 구조를 예제로 보여줄게.

정확하게 보고 있다.
`DataSource`를 스프링 빈으로 등록하는 메서드에서는 **예외 선언이 필요 없다.**
반면 실제로 **커넥션을 가져오는 순간(getConnection)** 에만 `SQLException`이 발생할 수 있기 때문에 예외가 붙는다.

아래에서 이유를 정확히 설명한다.

---

# 1. DataSource 설정 메서드(@Bean)에서는 왜 SQLException이 없는가?

`@Bean` 메서드에서 하는 일은 단순히 **DataSource 객체를 생성하고 설정값을 넣는 것**이다.

예를 들어:

```java
HikariDataSource dataSource = new HikariDataSource();
dataSource.setJdbcUrl(URL);
dataSource.setUsername(USERNAME);
dataSource.setPassword(PASSWORD);
```

여기에서는 실제 DB 접속을 시도하지 않는다.
즉:

* DB와 네트워크 연결 안 함
* JDBC 드라이버 호출 안 함
* 커넥션 풀에서 커넥션을 미리 만드는 것도 아님(기본 설정 기준)

따라서 **체크 예외(SQLException)를 던질 이유가 없다.**

HikariCP도 설정 세팅 단계에서는 어떤 JDBC 예외도 발생시키지 않는다.

---

# 2. 언제 SQLException이 발생하는가?

`DataSource`는 단순히 “연결 팩토리” 역할이며
**실제 DB 연결은 getConnection() 호출 시점에 발생한다.**

예:

```java
Connection con = dataSource.getConnection();  // 여기서 SQLException 가능
```

이 순간에:

* DB와 커넥션을 맺고
* 네트워크 작업이 수행되며
* 드라이버가 실제 JDBC Connection을 반환

이 과정에서 문제가 생기면 **SQLException**이 발생한다.

그래서 JDBC 표준 인터페이스 `DataSource#getConnection()`은 반드시 `throws SQLException`을 갖는다.

```java
Connection getConnection() throws SQLException;
```

---

# 3. HikariCP는 언제 실제 커넥션을 만드는가?

기본적으로 HikariCP는 **lazy initialization**이다.

즉, DataSource를 만들 때 커넥션을 만들지 않고
`getConnection()` 호출이 들어오면 그때 만들어서 풀에 넣는다.

그래서 빈 등록 순간에는 예외가 발생하지 않는다.

---

# 4. 예외가 필요 없는 이유 정리

| 시점                           | 하는 일              | SQLException 가능 여부 |
| ---------------------------- | ----------------- | ------------------ |
| `@Bean` DataSource 생성        | 단순히 객체 생성 및 속성 설정 | 없음                 |
| `dataSource.getConnection()` | 실제 DB 연결 작업 발생    | 있음                 |

즉,
**“설정 단계에서는 예외 없음, 연결 단계에서만 예외 발생”**
이게 JDBC + HikariCP의 구조이다.

---

필요하면 `@PostConstruct`에서 미리 커넥션 풀을 초기화하여 예외를 빨리 잡는 방법도 알려줄 수 있다.

지금 상황의 핵심은 **스프링이 관리하는 빈의 필드(의존성)는 인스턴스 레벨**이고,
`one()` / `main()`은 **static 메서드이기 때문에 인스턴스 필드에 접근할 수 없다**는 점이다.

그리고 **스프링 DI는 절대로 static 필드에 자동 주입되지 않는다.**
스프링은 빈의 인스턴스를 만들고 그 인스턴스에 의존성을 넣는 구조라, static은 DI의 대상이 아니다.
따라서 “static으로 만들고 자동 DI”는 설계적으로 불가능하다.

---

# 해결 가능한 방법들

## 1) static 메서드를 없애고 인스턴스 메서드로 전환 (정석)

스프링 컨테이너가 RunService를 빈으로 생성하므로 인스턴스 메서드를 호출하도록 바꾼다.

```java
@Component
@RequiredArgsConstructor
public class RunService {
    private final MemberRepository2 memberRepository2;
    private final MemberService memberService;

    public void one() throws SQLException {
        Member m1 = new Member("m1", 10_000);
        Member m2 = new Member("m2", 10_000);
        memberRepository2.save(m1);
        memberRepository2.save(m2);

        memberService.accountTransfer(m1.getMemberId(), m2.getMemberId(), 1_000);

        Member afterM1 = memberRepository2.findById(m1.getMemberId());
        Member afterM2 = memberRepository2.findById(m2.getMemberId());
        System.out.println(afterM1.getMoney());
        System.out.println(afterM2.getMoney());
    }
}
```

그리고 main에서는 스프링 컨테이너를 띄운 뒤 빈을 얻어서 호출한다.

```java
public static void main(String[] args) throws SQLException {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
    RunService runService = ac.getBean(RunService.class);
    runService.one();
}
```

가장 권장되는 방식이다.

---

## 2) CommandLineRunner or ApplicationRunner 사용

스프링 부트라면 main 메서드 없이도 앱 실행 시 자동으로 실행되는 코드를 만들 수 있다.

```java
@Component
@RequiredArgsConstructor
public class RunService implements CommandLineRunner {

    private final MemberRepository2 memberRepository2;
    private final MemberService memberService;

    @Override
    public void run(String... args) throws Exception {
        one();
    }

    private void one() throws SQLException {
        // 동일한 로직
    }
}
```

---

## 3) static을 유지하고 싶다면: 스프링 DI 사용 불가 → 수동 주입 필요

스프링을 통한 자동 DI는 불가능하고, 무조건 인스턴스를 생성해서 static에 직접 할당해야 한다.
하지만 이건 스프링 아키텍처에서는 강하게 비추천된다.

---

# 결론

* **스프링 DI는 절대로 static 필드에 자동 주입되지 않는다.**
* 해결하려면

    * static 메서드를 인스턴스 메서드로 바꾸고 스프링 빈을 통해 호출하거나
    * CommandLineRunner를 사용해 실행 시점에 코드 실행하는 방식이 필요하다.

원한다면 지금 구조에서 가장 깔끔한 구조로 전체 리팩토링 예시를 만들어줄 수도 있다.

지금 오류 메시지는 단순히 **프로세스가 비정상 종료(exit code 1)** 됐다는 뜻이고,
원인은 **CommandLineRunner 내부에서 예외가 발생했기 때문**이다.

즉, CommandLineRunner 자체가 문제가 아니라 **네가 실행한 비즈니스 로직(one 메서드) 안에서 SQLException 등 예외가 터져서 프로세스가 종료된 것**이다.

이 문제는 CommandLineRunner 구조 때문이 아니라, 내부 코드에서 예외가 propagate 되었기 때문에 스프링 부트가 애플리케이션을 실패로 판단하는 것이다.

---

# 왜 이런 일이 발생하나

CommandLineRunner는 아래처럼 예외를 던질 수 있다.

```java
@Override
public void run(String... args) throws Exception {
    one(); // 여기서 예외 발생 → 런타임에서 프로세스 종료
}
```

one() 내부에 예외가 발생하면 스프링 부트는 이를 잡지 않기 때문에
프로그램은 exit code 1로 종료된다.

---

# 해결 방법

## 1) run() 내부에서 예외를 try-catch로 처리

테스트 실행을 안정적으로 하고 싶다면 예외를 잡아야 한다.

```java
@Override
public void run(String... args) {
    try {
        one();
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

이렇게 하면 프로세스가 강제 종료되지 않는다.

---

## 2) 실제 원인을 찾으려면 스택트레이스를 출력해봐야 한다

현재 메시지는 Gradle이 보여주는 표면적인 정보라 원인을 알 수 없다.
실제 오류는 네 코드 내부 the real exception이다.

실행 로그 전체 또는 stacktrace를 보면 정확한 원인을 알 수 있다.

---

# CommandLineRunner는 제대로 사용한 것이 맞다

프로세스 exit code 1은 **CommandLineRunner를 사용했다는 이유로 발생하는 오류가 아니다.**
반드시 내부 비즈니스 로직 중 하나가 실패했다.

예:

* DB 커넥션 실패
* 트랜잭션에서 예외 발생
* accountTransfer 내부 오류
* save 중 SQL 예외 발생
* Member 테이블이 없음

등등.

---

# 정확한 원인 파악 도와줄게

스프링 실행 로그 중 아래 내용만 복사해서 보내줘:

* 에러가 발생한 첫 번째 Exception 메시지
* stacktrace (caused by 포함)

예:

```
org.springframework.jdbc.BadSqlGrammarException: ...
Caused by: java.sql.SQLException: ...
    at ...
```

이걸 보면 정확히 어떤 SQL/트랜잭션 문제인지 바로 진단해줄 수 있다.

필요하면 지금 RunService 전체 구조와 MemberRepository2, MemberService도 함께 보면 바로 잡아줄 수 있어.

지금 코드를 보면 **논리적으로 예외가 반드시 발생하는 지점이 하나 있다.**
따라서 CommandLineRunner 실행 시 exit code 1로 종료되는 것은 정상적인 결과다.

아래를 보자.

# 문제의 핵심

MemberService.accountTransfer 내부

```java
validation(toMember, toId);

private void validation(Member toMember, String toId) {
    if (!toMember.getMemberId().equals(toId)) {
        throw new IllegalStateException(toMember.getMemberId());
    }
}
```

그런데 toMember는 다음에서 조회된다.

```java
Member toMember = memberRepository2.findById(toId);
```

여기서 toId는 문자열 `"m2"`이다.
그리고 save에서 넣은 값도 `"m2"`다.

즉, 정상이라면 항상 조건이 성립한다.

하지만 네 코드에서는 validation 조건이 **항상 false가 되는 상황**이 발생한다는 뜻이다.

왜 그럴까?

---

# 결론: 지금 구조에서는 validation은 무조건 통과해야 한다.

즉, 현재 코드에서 IllegalStateException이 발생했다면 그 원인은 다음 둘 중 하나다.

---

# 원인 1: `m2`가 이미 DB에 존재해서 save 시도 중 예외가 발생했고, 그 후 흐름이 꼬였을 가능성

`save(new Member("m2", 10000))` 코드가 실행되는데
이미 member 테이블에 `"m2"`가 있으면 SQLIntegrityConstraintViolationException이 터진다.

그러면 after 값 조회 시에도 문제가 생길 수 있다.

이 경우 로그는 대략 다음과 같다.

```
Caused by: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation
```

---

# 원인 2: member 테이블이 비어 있지 않은 상태에서 중복 ID 저장을 시도

지금 실행 순서는 다음과 같다.

```
save(m1) → save(m2) → find(m1) → find(m2) → update(m1) → validation(toMember, toId)
```

그런데 만약 이전 실행 흔적 때문에 DB에 `"m2"` 대신 다른 값이 있다면
findById("m2") 자체가 오류를 던진다:

```
throw new NoSuchElementException(memberId)
```

이 NoSuchElementException 역시 애플리케이션을 exit 1로 종료시킨다.

---

# 더 정확히: 지금 코드 구조는 트랜잭션이 없기 때문에 문제가 발생한다

MemberService.accountTransfer는 여러 DB 작업을 수행하지만 트랜잭션이 없다.

따라서 save(m1)과 save(m2) 중 하나만 성공하거나
update(m1) 후 validation에서 예외가 터지면 DB가 비정상 상태가 된다.

이런 상태에서 다시 실행하면 “이미 존재하는 member_id” 또는 “없는 member_id”가 발생한다.

---

# 해결 방법

## 1) 매번 실행하기 전에 member 테이블을 초기화

가장 단순한 확인 방법.

RunService.run() 초반에 아래 추가:

```java
memberRepository2.delete("m1");
memberRepository2.delete("m2");
```

또는 테이블 전체 truncate:

```sql
truncate table member;
```

---

## 2) validation 로직을 임시로 비활성화해서 예외 원인이 그 부분인지 확인

```java
private void validation(Member toMember, String toId) {
    // 비활성화
}
```

만약 이걸 주석 처리했더니 정상 실행되면
현재 에러 원인은 바로 이 validation이다.

---

## 3) 트랜잭션 적용 (정석)

MemberService에 @Transactional 추가.

```java
@Service
@RequiredArgsConstructor
public class MemberService {

    @Transactional
    public void accountTransfer(...) { ... }
}
```

스프링 트랜잭션을 적용하면 update 도중 예외가 발생해도
DB 일관성이 보장되므로 다음 실행에서 문제가 덜 발생한다.

---

# 정리

전달한 로그에는 **실제 예외 메시지의 본체가 없다**.
하지만 코드 구조만 보면 exit code 1이 나올 수 있는 지점은 명확하다.

가능성이 높은 순서대로 원인은 다음과 같다:

1. 이전 실행에서 남은 데이터 때문에 save("m2")가 실패
2. validation이 예외를 던짐
3. findById("m2")에서 NoSuchElementException 발생
4. DB에 예상치 못한 값이 들어 있음
5. 트랜잭션이 없어 이전 실패 작업이 누적됨

---

원한다면:

1. 실제 DB(member 테이블)의 현재 상태
2. 실행 콘솔 전체 로그
3. Member 클래스

이 세 가지를 보여주면 정확한 원인을 pinpoint해서 해결해줄 수 있다.

지금 오류는 **코드 문제도 아니고 DB 문제도 아니다.**
원인은 **Gradle이 실행하려는 “RunService.main()”이라는 태스크 설정 자체가 잘못돼서 main 메서드를 찾지 못하는 것**이다.

즉, **CommandLineRunner로 실행할 때는 main()을 직접 만들지 않는데, Gradle이 계속 “RunService.main()을 실행하라”는 태스크를 기억하고 있어서 그걸 실행하려다가 실패한 것**이다.

이 오류 메시지가 이를 그대로 말해준다:

```
hello.jdbc.transaction.RunService 클래스에서 기본 메서드를 찾을 수 없습니다.
public static void main(String[] args)
```

즉 RunService에는 main()이 없으므로 실행 불가.

따라서 Gradle은 exit code 1을 낸다.

---

# 상황 정리

## 현재 구조

* RunService는 @Component + CommandLineRunner → 스프링 컨테이너가 실행 시 자동 실행
* RunService에는 main() 없음
* 그런데 Gradle은 여전히 다음 태스크를 실행하려고 한다:

```
:hello.jdbc.transaction.RunService.main()
```

이 태스크는 build.gradle의 application plugin 또는 IntelliJ Run Configuration에서 생성된 실행 구성이며,
여전히 RunService.main()을 찾으려고 한다.

---

# 해결 방법

## 방법 1: Gradle task **RunService.main()** 삭제 또는 수정

IDE(인텔리J)의 Run/Debug Configurations에서
`RunService.main()`을 실행하는 설정을 삭제해야 한다.

스프링 부트 프로젝트라면 **Application 클래스의 main()을 실행해야 한다.**

보통 아래처럼 생긴 클래스가 있다:

```java
@SpringBootApplication
public class HelloJdbcApplication {
    public static void main(String[] args) {
        SpringApplication.run(HelloJdbcApplication.class, args);
    }
}
```

이 클래스를 실행해야 한다.

---

## 방법 2: build.gradle에서 application.mainClass를 잘못 지정했을 가능성

build.gradle 확인:

```
application {
    mainClass = 'hello.jdbc.transaction.RunService'
}
```

이렇게 되어 있으면 실행시 무조건 main()을 찾으려 한다.

→ 이 값을 스프링 부트 Application 클래스로 수정해야 한다.

예시:

```
application {
    mainClass = 'hello.jdbc.HelloJdbcApplication'
}
```

---

## 방법 3: 터미널에서 실행할 때도 mainClass를 제대로 호출해야 한다

예:

```
./gradlew bootRun
```

이 경우 Spring Boot의 main 클래스가 자동 지정되므로 문제 없음.

반면 다음은 하면 안 된다:

```
./gradlew RunService.main
```

---

# 결론

오류 원인은 다음과 같다.

* RunService는 이제 main() 메서드가 없음
* 그런데 Gradle 실행 구성은 여전히 RunService.main()을 실행하려고 함
* 따라서 “기본 메서드를 찾을 수 없습니다” 오류가 발생
* CommandLineRunner와는 무관한 문제

---

# 다음 단계

1. 인텔리J에서 Run Configuration을 열어서
   **RunService.main() 실행 설정을 삭제**

2. 스프링 부트 애플리케이션의 main() 클래스(예: HelloJdbcApplication)를 실행

3. 또는 터미널에서:

```
./gradlew bootRun
```

이걸 실행하면 RunService.run()이 자동 실행된다.

---

원하면 build.gradle과 프로젝트 구조를 보여주면
정확히 어떤 부분을 수정해야 하는지 바로 짚어 줄 수 있다.
