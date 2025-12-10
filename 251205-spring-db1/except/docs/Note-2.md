일반적으로 **맞습니다.** 조금 더 구조를 분명하게 정리하면 다음과 같습니다.

---

## 1. DB 서버에서 하는 일

DB 서버(예: MySQL, PostgreSQL, Oracle 등)는 클라이언트(애플리케이션)의 요청을 처리하다가 문제가 생기면 다음을 수행합니다.

1. **오류 코드를 생성**
2. **오류 메시지를 포함한 응답 패킷을 클라이언트로 전달**

예:

* MySQL 에러 코드: `1062 Duplicate entry`
* PostgreSQL SQLSTATE: `23505 unique_violation`

즉, DB 서버는 **예외를 던지지 않고 단순히 오류 정보를 응답**합니다.

---

## 2. JDBC 드라이버가 하는 일

스프링 애플리케이션은 JDBC 드라이버(또는 R2DBC 등)를 통해 DB와 통신합니다.

DB 서버가 오류 응답을 보내면 JDBC 드라이버는 다음을 수행합니다:

1. 응답 패킷의 **오류 코드·SQLSTATE·메시지를 분석**
2. 이를 Java 예외로 변환해 **`SQLException` 계열 예외를 던짐**

예:

* Unique Key 위반 → `SQLIntegrityConstraintViolationException`
* Connection 문제 → `SQLTransientConnectionException`
* 문법 오류 → `SQLSyntaxErrorException`

즉, **예외를 던지는 것은 DB 서버가 아니라 JDBC 드라이버입니다.**

---

## 3. 스프링에서의 예외 처리

스프링 JDBC / JPA / MyBatis 등은 JDBC 예외들을 다시 **Spring DataAccessException 계열의 런타임 예외로 변환**합니다.

예:

* `DuplicateKeyException`
* `DataIntegrityViolationException`
* `BadSqlGrammarException`

따라서 스프링 애플리케이션은 체크 예외(`SQLException`)를 직접 다루지 않고 런타임 예외로 처리할 수 있습니다.

---

## 4. 요약

| 단계        | 동작                                 |
| --------- | ---------------------------------- |
| DB 서버     | 오류 코드 + 메시지를 응답으로 보냄               |
| JDBC 드라이버 | 응답을 보고 Java 예외(`SQLException`)를 던짐 |
| 스프링       | JDBC 예외를 Spring 예외로 변환하여 앱에 전달     |

그래서 질문처럼 **DB 서버는 오류 코드를 반환하고, 스프링에서 DB 드라이버가 예외를 던지는 구조가 맞습니다.**

---

원하면 다음도 설명해줄 수 있습니다:

* 각 DB의 대표적인 에러 코드 체계 (MySQL vs PostgreSQL vs Oracle)
* 스프링의 `SQLExceptionTranslator` 동작 방식
* 스프링 부트에서 예외를 어떻게 로깅하는지
* JPA 환경에서 SQL 예외가 어떻게 변환되는지

정확히 말하면 **JDBC 드라이버 매니저(DriverManager)는 예외 변환을 하지 않습니다.**
예외를 던지는 주체와 런타임 예외로 변환하는 주체는 서로 다릅니다. 순서를 단계별로 보면 더 명확해집니다.

---

# 1. 누가 어떤 예외를 던지는가?

## (1) JDBC 드라이버가 체크 예외를 던짐

DB 서버가 오류 응답을 보내면 **JDBC 드라이버(각 DB 벤더의 구현체)**가 이를 Java 예외로 변환해 던집니다.

예:

* `java.sql.SQLIntegrityConstraintViolationException`
* `java.sql.SQLSyntaxErrorException`
* 기본적으로 모두 **`SQLException`** 계열

여기까지는 **런타임 예외가 아님**.

---

## (2) DriverManager는 예외 전달만 하고 변환은 안 함

`DriverManager#getConnection()` 등이 내부적으로 드라이버를 호출하고
드라이버가 던진 `SQLException`을 **그대로 위로 전달**할 뿐입니다.

즉 **DriverManager는 예외 변환을 하지 않습니다.**

---

# 2. 그럼 런타임 예외로 변환하는 건 누가 하는가?

## 답: **스프링의 SQLExceptionTranslator**

스프링은 JDBC 예외를 그대로 사용하지 않고 이를
**런타임 예외인 DataAccessException 계열로 변환**하여 던집니다.

그 역할을 하는 것이 `SQLExceptionTranslator`.

스프링 JDBC의 핵심 컴포넌트:

* `SQLErrorCodeSQLExceptionTranslator`
* `SQLStateSQLExceptionTranslator`

JDBC 코드에서 드라이버가 던진 `SQLException`을 잡아서 아래처럼 변환합니다:

예:

* SQLSTATE 23505 → `DuplicateKeyException`
* SQLSTATE 22001 → `DataIntegrityViolationException`

스프링 내부 흐름 예:

```java
try {
    jdbcTemplate.query(...);
} catch (SQLException ex) {
    throw sqlExceptionTranslator.translate("query", sql, ex);
}
```

여기서 `translate`가 **런타임 예외**(`DataAccessException`)를 반환합니다.

---

# 3. 왜 런타임 예외로 변환하는가?

1. `SQLException`은 체크 예외라서 모든 호출부에서 처리해야 함
2. 스프링은 계층 구조에 얽매이지 않는(DB 벤더 독립적) 예외 체계를 제공하고 싶었음
3. 런타임 예외로 바꾸면 트랜잭션 AOP에서 자동 롤백 처리 가능

그래서 스프링 JDBC, MyBatis-Spring, JPA 등 모두
스프링의 "예외 변환"을 활용합니다.

---

# 4. 전체 흐름 요약

| 주체                             | 하는 일                                                  |
| ------------------------------ | ----------------------------------------------------- |
| **DB 서버**                      | 오류 코드와 메시지를 응답으로 전달                                   |
| **JDBC 드라이버**                  | 이를 `SQLException` 계열 **체크 예외**로 변환해 던짐                |
| **DriverManager**              | 예외를 그대로 전달 (변환 안함)                                    |
| **스프링 SQLExceptionTranslator** | `SQLException` → **DataAccessException (런타임 예외)**로 변환 |
| **애플리케이션 코드**                  | 런타임 예외를 원하는 대로 처리                                     |

---

원하면 다음도 설명해 줄 수 있습니다.

* 스프링이 DB 벤더별 오류 코드를 어떤 파일로 관리하는지 (`sql-error-codes.xml`)
* MyBatis, JPA에서 예외 변환이 적용되는 과정
* 트랜잭션 AOP와 런타임 예외의 상관관계

`SQLExceptionTranslator`는 **ExceptionHandler가 아닙니다.**
그리고 **서블릿 필터나 인터셉터의 예외 처리 흐름과도 전혀 무관한 위치에서 동작**합니다.

핵심은 다음 한 줄입니다.

**SQLExceptionTranslator는 "웹 계층"이 아니라 "데이터 접근 계층 내부"에서 동작하는 예외 변환기이다.**

아래에서 구조를 단계별로 확인하면 더 분명해집니다.

---

# 1. SQLExceptionTranslator는 어디에서 동작할까?

`SQLExceptionTranslator`는 스프링 JDBC 내부에서, 즉 **Repository, DAO 내부에서 호출**됩니다.
컨트롤러까지 예외가 올라오기 전에 이미 예외 번역이 끝납니다.

대표적인 예:

* `JdbcTemplate`
* `NamedParameterJdbcTemplate`
* `SimpleJdbcInsert`
  등이 내부에서 사용합니다.

즉, `SQLExceptionTranslator`는

* JDBC 드라이버가 던진 `SQLException`을 잡아서
* 즉시 `DataAccessException`(런타임 예외)로 변환하고
* 변환된 예외를 상위 계층으로 던지는 역할

따라서 요청·응답 레벨(web layer)에는 등장하지 않습니다.

---

# 2. ExceptionHandler(@ControllerAdvice)와의 차이

`@ExceptionHandler`는 **컨트롤러에서 던져진 예외가 DispatcherServlet까지 도달했을 때**
예외 응답을 만들어주는 "웹 영역 처리기"입니다.

반면:

* `SQLExceptionTranslator`는 **DAO/JDBC 수준에서 예외를 변환**
* `@ExceptionHandler`는 **웹 요청 처리 중 발생 예외를 응답으로 변환**

서로 계층이 다릅니다.

---

# 3. 필터, 인터셉터는 흐름이 더 먼 곳에서 동작한다

### 필터(Filter)

* 서블릿 컨테이너 레벨
* DispatcherServlet 실행 전/후 동작

### 스프링 인터셉터(HandlerInterceptor)

* 컨트롤러 실행 전/후
* 뷰 렌더링 전/후

### 둘 다 DAO/JDBC 오류 변환과는 완전히 별개의 영역

**중요한 점**
필터와 인터셉터는 **이미 컨트롤러가 실행된 이후**에야 예외를 잡을 수 있습니다.

하지만 `SQLExceptionTranslator`는 **컨트롤러까지 예외가 도달하기 전에 이미 처리됨**.

---

# 4. 전체 호출 흐름 비교

## JDBC 호출 시 흐름

```
Repository (DAO)
   ↓
JdbcTemplate.execute(...)
   ↓
JDBC 드라이버가 SQLException 던짐
   ↓
SQLExceptionTranslator가 잡아서 DataAccessException으로 변환
   ↓
서비스 계층으로 런타임 예외 전파
   ↓
컨트롤러 → DispatcherServlet로 전파
   ↓
스프링 @ExceptionHandler 또는 Filter/Interceptor가 마지막으로 개입 가능
```

여기서 중요한 포인트는:

* `SQLExceptionTranslator`는 **DAO 내부**에서 호출됨
* `Filter`, `Interceptor`, `@ExceptionHandler`는 **웹 요청 처리 과정**에서만 개입
* 서로 레이어가 다르므로 직접적인 관계는 없다

---

# 5. 요약

| 요소                         | 위치                     | 역할                                        |
| -------------------------- | ---------------------- | ----------------------------------------- |
| **SQLExceptionTranslator** | 데이터 접근 계층(DAO/JDBC 내부) | `SQLException` → `DataAccessException` 변환 |
| **@ExceptionHandler**      | 웹 컨트롤러 계층              | 응답 변환                                     |
| **인터셉터**                   | 컨트롤러 전/후               | 요청/응답 가공                                  |
| **필터**                     | 서블릿 컨테이너 레벨            | 요청/응답 가공                                  |

즉, `SQLExceptionTranslator`는 **ExceptionHandler와 역할이 완전히 다르고**,
**웹 계층과 무관하게 JDBC 계층에서 독립적으로 동작**합니다.

---

필요하면 이렇게도 설명해 드릴 수 있습니다.

* JdbcTemplate 내부에서 예외 번역이 적용되는 실제 소스 코드
* SQLExceptionTranslator가 SQLSTATE와 에러코드를 기준으로 번역하는 방식
* JPA/Hibernate가 예외를 어떻게 변환하는지 비교 설명

아래는 **스프링이 어떻게 `SQLExceptionTranslator`를 DAO 내부에서 호출하는지**
실제 `JdbcTemplate` 핵심 코드를 기준으로 단계별로 설명합니다.

스프링의 `JdbcTemplate`은 다음 흐름으로 동작합니다.

1. JDBC 드라이버가 `SQLException`을 던짐
2. `JdbcTemplate`이 catch 블록에서 이 예외를 잡음
3. 내부에 주입된 `SQLExceptionTranslator`가 이를 런타임 예외로 변환
4. 변환된 `DataAccessException`을 다시 throw하여 DAO 밖으로 전달

이 과정은 **웹 계층에 도달하기 전에 이미 변환이 완료**되는 구조입니다.

---

# 1. JdbcTemplate 내부 동작 코드

스프링 프레임워크 실제 소스를 기반으로 핵심 부분만 축약합니다.
(원본은 너무 길기 때문에 핵심 흐름만 보여드립니다.)

## JdbcTemplate의 핵심 구조 (스프링 실제 코드 기반)

### query 실행 단계

```java
@Override
public <T> T query(String sql, ResultSetExtractor<T> rse) throws DataAccessException {
    try {
        return execute(new QueryStatementCallback<>(sql, rse));
    }
    catch (SQLException ex) {
        throw translateException("Query", sql, ex);
    }
}
```

여기서 중요한 부분은 **SQLException을 잡아서 translateException()으로 넘기는 부분**입니다.

---

# 2. translateException() 내부

스프링 JdbcTemplate 내부에는 다음과 같이 되어 있습니다.

```java
protected DataAccessException translateException(String task, String sql, SQLException ex) {
    SQLExceptionTranslator translator = getExceptionTranslator();
    return translator.translate(task, sql, ex);
}
```

여기서 사용되는 Translator는 보통 **SQLErrorCodeSQLExceptionTranslator**입니다.

즉 이 시점에서 이미 **SQLException → DataAccessException** 변환이 일어납니다.

---

# 3. SQLErrorCodeSQLExceptionTranslator 내부 동작 예

`SQLExceptionTranslator`의 대표 구현체는 다음과 같습니다.

```java
@Override
public DataAccessException translate(String task, String sql, SQLException ex) {
    String errorCode = extractErrorCode(ex);

    if (isDuplicateKey(errorCode)) {
        return new DuplicateKeyException(buildMessage(task, sql, ex), ex);
    }

    if (isDataIntegrityViolation(errorCode)) {
        return new DataIntegrityViolationException(buildMessage(task, sql, ex), ex);
    }

    // 기타 조건
    return new UncategorizedSQLException(task, sql, ex);
}
```

즉, 이 변환기에서 벤더별 오류코드를 기준으로
MySQL·PostgreSQL·Oracle 등을 분석하여 적합한 스프링 런타임 예외로 변환합니다.

---

# 4. 전체 흐름 예시로 정리

예를 들어 DAO에서 아래와 같은 코드가 있다고 가정합니다.

```java
public User findUser(Long id) {
    return jdbcTemplate.queryForObject("SELECT * FROM user WHERE id = ?", userRowMapper, id);
}
```

이 쿼리가 실행되면 다음 흐름으로 진행됩니다.

```
JDBC 드라이버
  ↓ SQLException 발생
JdbcTemplate
  ↓ catch(SQLException)
  ↓ translateException()
SQLExceptionTranslator
  ↓ SQLSTATE / error code 분석
  ↓ 적절한 데이터 접근 예외 생성
DataAccessException (런타임 예외) 발생
  ↓ 서비스 계층으로 전파
```

즉 DAO 내부에서 이미 변환이 끝나기 때문에
컨트롤러나 필터/인터셉터에서는 **DataAccessException만 보게 됩니다.**

---

# 5. 실제 스프링 코드로 보는 더 자세한 흐름

JdbcTemplate의 모든 JDBC 실행은 `execute()` 내부에서 처리됩니다.

실제 스프링 코드는 다음과 같은 형태입니다.

```java
public <T> T execute(StatementCallback<T> action) {
    try {
        Connection con = getConnection();
        Statement stmt = con.createStatement();

        try {
            return action.doInStatement(stmt);
        }
        catch (SQLException ex) {
            throw translateException("StatementCallback", getSql(action), ex);
        }
        finally {
            closeStatement(stmt);
            releaseConnection(con);
        }
    }
    catch (SQLException ex) {
        throw translateException("getConnection", null, ex);
    }
}
```

여기서 핵심은:

### 모든 SQLException을 잡아서 translateException()으로 넘긴다.

즉 JDBC 호출이 어디서 발생하든 동일하게 번역 로직이 적용됩니다.

---

# 요약

| 레이어                    | 역할                                         |
| ---------------------- | ------------------------------------------ |
| JDBC 드라이버              | SQLException 던짐                            |
| JdbcTemplate           | SQLException catch → translateException 호출 |
| SQLExceptionTranslator | DB 벤더 오류코드 기반으로 DataAccessException 생성     |
| DAO 밖                  | 이미 런타임 예외 상태로 전달됨                          |

따라서 `SQLExceptionTranslator`는
**DAO 내부(JdbcTemplate 내부)에서 SQLException을 런타임 예외로 변환하는 전용 컴포넌트**이며,
웹 계층까지 올라갈 필요 없이 이미 변환이 완료된 상태로 상위 계층에 전달됩니다.

---

원하면 아래도 더 설명해 줄 수 있습니다.

* 스프링이 DB 벤더 오류코드를 `sql-error-codes.xml`로 관리하는 방식
* MyBatis-Spring, JPA에서 동일한 패턴으로 예외 변환이 일어나는 과정
* SQLExceptionTranslator를 직접 커스터마이징하는 방법

결론부터 말하면 **가능은 하지만 MyDBException 내부에서 `SQLException`을 보관하고 있어야만**
`getErrorCode()`를 호출할 수 있습니다.

즉, 단순히 `throw new MyDBException(e)`만 하고,
`MyDBException`이 내부에 `SQLException`을 저장하지 않으면 **바깥에서 오류 코드를 알 방법이 없습니다.**

---

# 1. 가능한 시나리오와 불가능한 시나리오

## 불가능한 경우

```java
catch (SQLException e) {
    throw new MyDBException(e);
}
```

그리고 MyDBException 내부가 이렇게 되어 있다면:

```java
public class MyDBException extends RuntimeException {
}
```

이 경우 **SQLException을 꺼낼 수 없기 때문에 errorCode 사용 불가**입니다.

---

# 2. 가능한 경우 (정석적인 방식)

`MyDBException`이 반드시 **원인 예외(cause)를 보관하는 생성자**를 사용해야 합니다.

예:

```java
public class MyDBException extends RuntimeException {
    public MyDBException(Throwable cause) {
        super(cause);
    }

    public int getErrorCode() {
        if (getCause() instanceof SQLException ex) {
            return ex.getErrorCode();
        }
        return -1;
    }
}
```

그리고 사용:

```java
try {
    ...
} catch (SQLException e) {
    throw new MyDBException(e);
}
```

바깥에서 처리할 때:

```java
try {
    service.doSomething();
} catch (MyDBException ex) {
    int code = ex.getErrorCode();
    System.out.println("DB Error Code = " + code);
}
```

이렇게 하면 **MyDBException에서 다시 SQLException의 상세 정보(오류 코드, SQLState 등)를 추출할 수 있습니다.**

---

# 3. 더 안전한 구현: 원인 예외를 명시적으로 보관

권장 형태:

```java
public class MyDBException extends RuntimeException {

    private final SQLException sqlException;

    public MyDBException(SQLException cause) {
        super(cause);
        this.sqlException = cause;
    }

    public int getErrorCode() {
        return sqlException.getErrorCode();
    }

    public String getSqlState() {
        return sqlException.getSQLState();
    }
}
```

이 방식은:

* 원인 예외를 명확하게 `SQLException`으로 고정
* 다운캐스팅이 필요 없음
* 의미 명확

이 때문에 실무에서는 **명시적 필드 보관 방식**이 더 많이 사용됩니다.

---

# 4. 정리

| 질문                                                          | 답변                                                 |
| ----------------------------------------------------------- | -------------------------------------------------- |
| MyDBException을 catch 해서 SQLException의 errorCode를 가져올 수 있는가? | MyDBException이 내부에서 SQLException을 저장하고 있다면 가능      |
| MyDBException이 cause를 저장하지 않으면?                             | 불가능                                                |
| 가장 좋은 구현 방식?                                                | MyDBException이 SQLException을 명시적으로 보관하고 getter로 제공 |

필요하면

* 스프링의 `UncategorizedSQLException` 소스
* `DuplicateKeyException` 내부 구조
  등을 보여드릴 수 있습니다.

결론부터 말하면 **`getCause().getErrorCode()`는 바로 호출할 수 없습니다.**
이유는 **`getCause()`의 타입이 `Throwable`이기 때문입니다.**
`Throwable`에는 `getErrorCode()`라는 메서드가 없기 때문에 **컴파일 오류가 발생**합니다.

---

# 1. 왜 바로 호출이 안 되는가?

`MyDBException`이 `SQLException`을 cause로 갖고 있다고 해도

```java
Throwable cause = getCause();
cause.getErrorCode();     // 컴파일 에러 발생
```

이유:

* `getCause()`의 반환 타입은 `Throwable`
* `Throwable`에는 `getErrorCode()` 메서드가 없음
* 따라서 **다운캐스팅 없이 호출 불가**

---

# 2. 올바른 호출 방식: 다운캐스팅 필요

```java
if (getCause() instanceof SQLException ex) {
    int code = ex.getErrorCode();
}
```

또는 기존 문법:

```java
SQLException ex = (SQLException) getCause();
int code = ex.getErrorCode();
```

---

# 3. 가장 깔끔한 방식: MyDBException이 직접 제공하도록 만들기

예외를 감싸는 패턴에서는 이렇게 만드는 것이 가장 좋습니다.

```java
public class MyDBException extends RuntimeException {

    public MyDBException(SQLException cause) {
        super(cause);
    }

    public int getErrorCode() {
        return ((SQLException)getCause()).getErrorCode();
    }
}
```

사용:

```java
try {
    ...
} catch (MyDBException ex) {
    System.out.println(ex.getErrorCode());
}
```

이 방식의 장점:

* 바깥에서 매번 `instanceof`나 캐스팅을 하지 않아도 됨
* MyDBException이 SQL 오류 정보를 제대로 래핑하여 제공

---

# 4. 정리

| 표현                                          | 가능 여부    | 이유                       |
| ------------------------------------------- | -------- | ------------------------ |
| `getCause().getErrorCode()`                 | 불가능      | getCause() 타입은 Throwable |
| `(SQLException) getCause()).getErrorCode()` | 가능       | 다운캐스팅을 통해 호출             |
| `instanceof` 확인 후 호출                        | 가능       | 타입 안전성 확보                |
| MyDBException 내부에서 errorCode를 제공하도록 구현      | 가장 좋은 방식 | 외부가 DB 세부사항에 의존하지 않음     |

---

원하면:

* 스프링이 내부적으로 `Throwable#getCause()`를 어떻게 캐스팅하여 사용하는지 실제 소스 코드
* 스프링의 `UncategorizedSQLException` 구현
* 커스텀 예외 래핑 시 주의사항

도 상세히 설명해드릴 수 있습니다.

질문에 나온 내용을 정리해 보면 다음과 같습니다.

```
errCode = 90002
e = jdbc.except.MyDBException: 
    org.h2.jdbc.JdbcSQLNonTransientException: 
        Method is only allowed for a query. 
        Use execute or executeUpdate instead of executeQuery; 
        SQL statement:
        delete from member where member_id = ? [90002-240]
```

즉, **H2 데이터베이스에서 error code 90002가 발생했고**,
원인 예외는 `JdbcSQLNonTransientException`이며 메시지는 **"Method is only allowed for a query"**입니다.

이 에러는 **SQL 문법 오류**가 아니라 **JDBC API 사용 방식 오류**입니다.

---

# 핵심 원인

## 1. DELETE 문을 executeQuery()로 실행했기 때문

H2는 다음 규칙을 갖습니다.

* `executeQuery()` → 반드시 **SELECT**처럼 결과 집합(ResultSet)이 있는 SQL만 호출 가능
* `executeUpdate()` → INSERT / UPDATE / DELETE / DDL 실행
* `execute()` → 모든 SQL 가능 (반환 타입에 따라 분기)

따라서 다음과 같이 잘못 호출하면 이 오류가 발생합니다.

```java
PreparedStatement ps = conn.prepareStatement("delete from member where member_id = ?");
ps.setLong(1, id);
ps.executeQuery();   // 잘못된 호출 → 바로 90002 발생
```

DELETE는 결과 집합이 없기 때문에
`executeQuery()`는 허용되지 않고 오류 코드를 발생시킵니다.

---

# 2. H2의 오류 코드 90002의 의미

H2 공식 메시지 설명:
`90002` = **METHOD_ONLY_ALLOWED_FOR_QUERY**

즉:

> “이 메서드는 SELECT 같은 쿼리에서만 사용할 수 있으며, DELETE에는 사용할 수 없다”

---

# 3. 왜 MyDBException으로 감싸져서 보였는가?

당신의 코드에서 아마 이런 형태로 래핑했기 때문입니다.

```java
catch (SQLException e) {
    throw new MyDBException(e);
}
```

그래서 스택트레이스 최상단은 MyDBException이지만,
실제 원인(cause)은 H2의 `JdbcSQLNonTransientException`입니다.

오류 메시지의 마지막 부분 `[90002-240]`은 H2가 제공한:

* 90002 = 오류 코드
* 240 = H2 버전을 의미합니다 (240 = H2 2.0.240)

---

# 4. 해결 방법

`executeQuery()` → `executeUpdate()`로 변경해야 합니다.

예:

```java
PreparedStatement ps = conn.prepareStatement(
    "delete from member where member_id = ?"
);
ps.setLong(1, id);
int result = ps.executeUpdate();   // 정답
```

스프링 JDBC에서는 자동으로 올바른 메서드를 호출하지만,
직접 JDBC를 사용하면 호출 메서드를 명확히 구분해야 합니다.

---

# 5. 정리

| 상황               | 의미                                   |
| ---------------- | ------------------------------------ |
| error code 90002 | SELECT가 아닌 SQL에서 executeQuery() 호출   |
| 예외 타입            | JdbcSQLNonTransientException         |
| 메시지              | “Method is only allowed for a query” |
| 원인               | DELETE 문을 executeQuery로 실행           |
| 해결               | executeUpdate() 또는 execute() 사용      |

---

필요하다면 아래도 설명해 드릴 수 있습니다.

* execute(), executeUpdate(), executeQuery()의 차이
* 스프링 JdbcTemplate이 내부적으로 어떤 JDBC API를 사용하는지
* H2 오류 코드 체계(90000번대 코드들)
* MyDBException 내부에서 original SQLException을 꺼내는 방법

원하시는 방향으로 더 자세히 알려드릴게요.

지금 출력 내용을 보면 다음과 같이 나옵니다.

```
jdbc.except.MyDBException: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException:
Unique index or primary key violation …
insert into member …
[23505-240]
```

메시지 마지막에 **[23505-240]**가 적혀 있으므로
**H2는 분명히 SQLSTATE = 23505(중복 키 오류)**를 내보내고 있습니다.

그런데 “왜 23505가 안 나온다고 느껴지는가?” 하는 문제는
**`SQLException.getErrorCode()`를 확인해서 생긴 착각일 가능성이 매우 높습니다.**

즉:

* SQLSTATE (문자열) → `"23505"`
* ERROR CODE (정수) → H2에서는 `"23505"`와 무관한 **별도 숫자**를 사용하기도 함

H2에서는 이 둘이 **다른 체계**입니다.

---

# 핵심 원인: H2에서는 SQLSTATE와 errorCode가 서로 다르다

## 1. SQLSTATE = 23505

이는 ANSI/ISO SQL 표준에서 **unique_violation**을 의미하는 국제 표준 코드입니다.
스프링은 보통 이 값을 사용합니다.

## 2. H2 errorCode = 23505가 아닐 수 있다

H2는 SQLSTATE는 표준을 따르지만, errorCode 값은 **DB마다, 기능마다 다르게 매핑**됩니다.
같은 오류라도 errorCode는 SQLSTATE와 동일하지 않습니다.

즉 다음이 모두 동시에 참일 수 있습니다.

```
SQLSTATE = "23505"  // unique constraint violation
errorCode = 90020   // 예를 들면 H2 내부의 고유 정수 코드
message = "... [23505-240]" // SQLSTATE 출력
```

그래서 당신이 `e.getErrorCode()`로 체크하면
**23505가 아니라 다른 값이 나오는 것**이 정상입니다.

---

# 실제 H2의 예외 구조

H2의 SQLException은 다음 정보를 제공합니다.

| 항목                         | 값               |
| -------------------------- | --------------- |
| **SQLState**               | "23505"         |
| **Error Code (int)**       | H2 내부 에러 코드     |
| **메시지 맨 끝의 `[23505-240]`** | SQLSTATE - H2버전 |

따라서 **23505는 errorCode가 아니라 SQLSTATE**입니다.

JDBC `SQLException`의 중요한 두 값:

```java
ex.getSQLState();   // "23505"  ← 이게 표준 중복키 코드
ex.getErrorCode();  // H2 내부 에러 번호 (표준과 다름)
```

당신이 아마 이렇게 코드를 작성했을 가능성이 큽니다.

```java
int code = ex.getErrorCode();   // 23505가 아니라 다른 값이 옴
```

그래서 “23505가 왜 안 나와?”라고 느낀 것입니다.

---

# 2. 스프링은 왜 23505를 잘 감지할까?

`SQLExceptionTranslator`는 DB 벤더별 SQLSTATE / 오류코드 매핑을 보고
다음 중 하나라도 일치하면 중복키 예외로 번역합니다:

* SQLSTATE = "23505"
* vendor-specific error code (H2 → 23505 아님)
* 메시지 패턴

따라서 스프링은 SQLSTATE만 보고도 `DuplicateKeyException`을 잘 던집니다.

**즉, 스프링은 errorCode 대신 SQLSTATE를 우선적으로 사용합니다.**

---

# 3. 어떻게 해야 “23505”를 다시 꺼낼 수 있는가?

정답:

```java
SQLException cause = (SQLException) myEx.getCause();
String sqlState = cause.getSQLState();       // "23505"
int vendorCode = cause.getErrorCode();       // H2 내부 error code (23505 아님)
```

그러면 제대로 구분할 수 있습니다.

---

# 4. 그래서 요약하면

| 항목                  | 값                     | 설명                      |
| ------------------- | --------------------- | ----------------------- |
| 메시지 끝 `[23505-240]` | SQLSTATE = 23505      | unique violation        |
| `ex.getSQLState()`  | `"23505"`             | 중복키 표준 코드               |
| `ex.getErrorCode()` | 표준과 무관                | DB마다 다름 (H2는 23505 아님)  |
| 왜 23505가 안 보이는가     | getErrorCode()를 썼기 때문 | SQLSTATE와 errorCode를 혼동 |

---

# 결론

**23505는 SQLSTATE이고, errorCode는 아예 다른 값이기 때문에 `getErrorCode()`에서는 23505가 나오지 않습니다.**
메시지에 `[23505-240]`처럼 표시되는 것은 SQLSTATE이며, H2에서 중복키 예외가 발생했음을 정상적으로 나타낸 것입니다.

원하면:

* H2 내부 errorCode 목록
* SQLSTATE와 errorCode의 차이
* 스프링이 H2 오류를 어떻게 매핑하는지 설명 (`SQLErrorCodeSQLExceptionTranslator`)

또 설명해 드릴 수 있습니다.
