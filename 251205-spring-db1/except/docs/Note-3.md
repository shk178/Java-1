아래 catch 블록에서 하고 있는 핵심 기능은 **JDBC SQLException → 스프링 DataAccessException(런타임 예외)로 변환하는 것**입니다.
즉 **스프링의 예외 추상화(Exception Translation)**를 수동으로 직접 적용한 코드입니다.

코드를 한 줄씩 분석해 보겠습니다.

---

# 1. 전체 구조의 의미

```java
catch (SQLException e) {
    SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    DataAccessException dae = set.translate("select", sql, e);
    throw dae;
}
```

이 블록이 하는 일은 다음과 같습니다.

### • JDBC 드라이버가 던진 SQLException을 잡음

### • 스프링의 SQLExceptionTranslator를 직접 생성

### • 해당 translator를 사용해 표준화된 런타임 예외(DataAccessException)로 변환

### • 변환된 런타임 예외를 다시 throw

→ 상위 계층 서비스, 컨트롤러는 표준화된 예외만 받게 됨

스프링 JDBC를 직접 사용하면 내부에서 자동으로 처리되는 기능인데
이 코드에서는 이를 **수동으로 구현**하고 있습니다.

---

# 2. SQLErrorCodeSQLExceptionTranslator(dataSource)가 하는 일

```java
SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(dataSource);
```

여기서 translator는 다음 정보를 가져옵니다.

* DB 메타데이터 (벤더 이름, productName, productVersion 등)
* 벤더별 SQL 오류 코드 매핑 정보(sql-error-codes.xml)

즉, 어떤 DB인지(H2, MySQL, Oracle)를 파악하여
적절한 예외 매핑 테이블을 선택합니다.

---

# 3. translate("select", sql, e)가 하는 일

```java
DataAccessException dae = set.translate("select", sql, e);
```

이 부분이 핵심으로, 다음과 같은 일을 수행합니다.

1. SQLException의

    * SQLState
    * ErrorCode
    * 메시지 패턴
      을 기반으로 오류 유형을 분석하고

2. 가장 적합한 스프링 런타임 예외로 매핑합니다.

예를 들면:

| SQLException 정보      | 변환되는 Spring 예외                   |
| -------------------- | -------------------------------- |
| SQLState 23505 (중복키) | DuplicateKeyException            |
| 외래 키 위반              | DataIntegrityViolationException  |
| SQL 문법 오류            | BadSqlGrammarException           |
| 커넥션 오류               | CannotGetJdbcConnectionException |
| 그 외                  | UncategorizedSQLException        |

즉, DB마다 다른 SQLException을 **Spring 공통 예외 계층으로 표준화**하는 동작입니다.

---

# 4. throw dae;

마지막으로 변환된 예외를 다시 던집니다.

중요한 점:

* **DataAccessException은 런타임 예외**
* 상위 계층(Service, Controller)은 SQLException을 직접 다룰 필요가 없음
* DB 벤더가 바뀌어도 코드는 수정할 필요 없음

이 덕분에 스프링이 제공하는 데이터 접근 계층은 **DB 독립적인 예외 처리 체계**를 갖게 됩니다.

---

# 5. 왜 pstmt.executeUpdate()를 두 번 호출했을까?

```java
pstmt.executeUpdate();
pstmt.executeUpdate();  // 두 번째 호출에서 중복키 예외 발생
```

첫 번째 INSERT는 성공.
두 번째 INSERT는 **PRIMARY KEY(=member_id) 중복**으로 인해 SQLException이 발생.

이 SQLException이 catch로 들어오고,
catch 문에서 스프링 예외로 번역되어 DuplicateKeyException 등이 던져지게 됩니다.

---

# 6. 전체 흐름 요약

```
executeUpdate() → SQLException 발생
      ↓
catch (SQLException e)
      ↓
translator 생성(SQLErrorCodeSQLExceptionTranslator)
      ↓
translator.translate()에서 분석 후 DataAccessException 생성
      ↓
throw (런타임 예외로 서비스 계층으로 전달)
```

이 구조 그대로가 **스프링 JDBC 내부(JdbcTemplate)가 수행하는 예외 처리 방식**입니다.

---

# 7. 실무에서는 이 코드를 직접 작성할까?

일반적으로 아래의 이유로 직접 작성하지 않습니다.

* `JdbcTemplate`이 이미 이 기능을 자동으로 수행
* `NamedParameterJdbcTemplate`, MyBatis-Spring, JPA 등도 동일

현재 코드 예시는 **스프링 예외 변환의 원리를 학습하기 위한 데모 코드**라고 보면 됩니다.

---

필요하면:

* SQLErrorCodeSQLExceptionTranslator 내부 소스 단계별 해석
* 왜 SQLSTATE와 errorCode가 다른지
* 스프링이 사용하는 sql-error-codes.xml 파일 구조
* SQLExceptionTranslator 확장/커스터마이징 방법

도 설명해 드릴 수 있습니다.

`set.translate("select", sql, e)` 형태의 호출에서
`"select"`, `sql`, `e` 를 전달하는 이유는 **단순히 예외를 변환하는 것 이상으로, 스프링이 오류 메시지를 “맥락 있는 형태”로 구성하기 위해서**입니다.

즉, `translate()`는 다음 **3가지 정보를 기반으로** 어떤 예외를 던질지 결정합니다.

---

# 1. translate(task, sql, e) — 각각의 의미

메서드 시그니처는 다음과 같습니다.

```java
DataAccessException translate(String task, String sql, SQLException ex)
```

각 인자는 다음 역할을 합니다.

## (1) task: 어떤 작업을 하다가 오류가 났는지 설명하는 텍스트

예: `"select"`, `"insert"`, `"delete member"`, `"getMemberById"`

이 값은 **오류 메시지에 포함**됩니다.

예:

```
PreparedStatementCallback; SQL [insert into member...]; 
insert member; nested exception is ...
```

즉, task는 순수하게 **사람이 읽는 용도**입니다.

---

## (2) sql: 실행하려던 SQL 문자열

스프링의 예외 메시지에 **SQL 구문을 함께 포함**하여 추적이 쉽도록 합니다.

예외 메시지 예:

```
BadSqlGrammarException: PreparedStatementCallback; bad SQL grammar 
[insert into member (member_id, money) values (?, ?)]; nested exception is ...
```

이처럼 translate에 sql을 주면:

* 어떤 SQL에서
* 어떤 오류가 났는지

명확히 표현됩니다.

만약 `sql`을 null로 보내면 메시지에는 SQL 구문이 포함되지 않습니다.

---

## (3) SQLException ex: 실제 발생한 원인 예외

이 값은 스프링이 다음을 분석하는 데 사용합니다.

* errorCode
* SQLState
* vendor code
* H2/MySQL/Oracle 각 벤더의 오류 분류 매핑

특히 중복키 오류(23505) 등은 SQLSTATE로 식별합니다.

translate()는 이 정보로 적절한 스프링 예외로 변환합니다:

* DuplicateKeyException
* DataIntegrityViolationException
* BadSqlGrammarException
* UncategorizedSQLException
  등등

---

# 2. 왜 이 세 인자가 모두 필요한가?

스프링 예외 메시지는 단순히 SQLException 메시지를 보여주는 것이 아니라
**더 풍부한 정보를 제공하기 위한 확장된 형태이기 때문입니다.**

예를 들어, DuplicateKeyException을 만들 때 메시지는 다음 구성 요소를 포함합니다.

* 수행 중이던 작업(task)
* 실행 SQL(sql)
* 원인 exception의 코드와 메시지(ex)

즉, 개발자가 로그를 확인할 때 **오류 원인을 즉시 파악할 수 있도록** 해줍니다.

---

# 3. 실제 생성되는 메시지 예

전달한 값이:

```java
task = "select"
sql  = "insert into member ..."
ex   = SQLException(23505)
```

일 때 translate가 생성할 예외 메시지는 이런 식입니다.

```
DuplicateKeyException: PreparedStatementCallback; 
SQL [insert into member ...]; select; nested exception is 
org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation...
```

여기서 task `"select"`가 메시지 중간에 포함되는 이유는 바로 인자로 전달했기 때문입니다.

---

# 4. 결론

`translate("select", sql, e)` 에서 인자를 넘기는 이유는 다음과 같습니다.

| 인자         | 목적                                  |
| ---------- | ----------------------------------- |
| `"select"` | 사람이 읽을 수 있는 작업 설명 (오류 메시지 가독성 향상)   |
| `sql`      | 어떤 SQL에서 오류 났는지 전달 (디버깅용)           |
| `e`        | 실제 SQLException → 스프링 런타임 예외 변환에 사용 |

즉, 단순한 변환기라기보다
**정확한 예외 분류 + 풍부한 오류 메시지**를 위해 필요한 정보 세트입니다.

---

원하면:

* translate 내부의 실제 코드 흐름
* SQLErrorCodeSQLExceptionTranslator가 sql-error-codes.xml을 로딩하는 방식
* task, sql을 null로 넣었을 때의 차이

같은 것도 더 자세히 설명해드릴 수 있습니다.

`memberRowMapper()`는 **JDBC ResultSet을 Member 객체로 변환하는 역할을 하는 함수(매퍼)**입니다.
JdbcTemplate을 사용할 때 **DB에서 조회한 한 행(ResultSet의 row) → 도메인 객체(Member)**로 바꾸는 책임을 가집니다.

---

# 1. RowMapper의 역할

`RowMapper<T>`는 스프링 JDBC가 제공하는 함수형 인터페이스로,
조회된 결과의 **각 행을 T 타입의 객체로 매핑**합니다.

대표 시그니처는 다음과 같습니다.

```java
T mapRow(ResultSet rs, int rowNum) throws SQLException;
```

즉,

* `rs` : 현재 행(row)의 데이터
* `rowNum` : 현재 행 번호(0 or 1부터 시작)

이 두 값을 받아서 원하는 객체를 생성해 반환하면 됩니다.

---

# 2. 질문 코드에서 memberRowMapper()가 하는 일

질문에 나온 코드:

```java
private RowMapper<Member> memberRowMapper() {
    return (rs, rowNum) -> {
        Member member = new Member();
        member.setMemberId(rs.getString("member_id"));
        member.setMoney(rs.getInt("money"));
        return member;
    };
}
```

이 람다식은 다음과 동일합니다.

```java
public Member mapRow(ResultSet rs, int rowNum) {
    Member member = new Member();
    member.setMemberId(rs.getString("member_id"));
    member.setMoney(rs.getInt("money"));
    return member;
}
```

즉, DB의 한 행에서 `member_id`와 `money` 값을 꺼내서
**Member 객체에 담아 반환**하는 역할입니다.

---

# 3. 어떻게 사용되는가?

아래 코드에서 RowMapper가 사용됩니다.

```java
return jdbcTemplate.queryForObject(sql, memberRowMapper(), memberId);
```

JdbcTemplate 내부 흐름은 다음처럼 이루어집니다:

1. SQL 실행
2. ResultSet을 얻음
3. ResultSet의 row를 하나씩 순회
4. 각 row를 RowMapper에 넘겨 Member 객체 생성
5. 최종적으로 생성된 Member를 반환

즉, 조회 결과 한 건을 Member 객체로 바꿔주는 매퍼입니다.

---

# 4. RowMapper를 쓰는 이유

이 매퍼가 없으면 직접 ResultSet을 읽어야 합니다.

예:

```java
PreparedStatement pstmt = ...
ResultSet rs = pstmt.executeQuery();
if (rs.next()) {
    Member member = new Member();
    member.setMemberId(rs.getString("member_id"));
    member.setMoney(rs.getInt("money"));
    return member;
}
```

이런 코드를 JdbcTemplate이 자동으로 처리할 수 있도록
데이터 매핑 부분만 RowMapper로 분리한 것입니다.

즉, 중복 코드를 줄이고, 더 깔끔한 DAO 구조를 만드는 데 필수입니다.

---

# 5. 결론 요약

| 항목                | 설명                                                      |
| ----------------- | ------------------------------------------------------- |
| RowMapper         | ResultSet → Member 객체 변환기                               |
| 역할                | DB 조회 결과 각 행을 도메인 객체로 만드는 책임                            |
| memberRowMapper() | Member 객체를 생성하는 람다식 매퍼                                  |
| 사용 위치             | `jdbcTemplate.queryForObject()`, `jdbcTemplate.query()` |

즉 memberRowMapper는 **조회 결과를 Member로 매핑하는 로직을 담고 있는 함수**입니다.

원하면 다음도 이어서 설명할 수 있습니다.

* query(), queryForObject() 두 메서드의 차이
* RowMapper를 별도 클래스로 분리하는 장단점
* BeanPropertyRowMapper 등 스프링이 제공하는 자동 매퍼
* RowMapper vs ResultSetExtractor 사용 시점

두 가지 질문을 분리해서 명확하게 설명해드리겠습니다.

---

# 1. `query()`와 `queryForObject()`의 차이

스프링 `JdbcTemplate`에서 두 메서드는 **조회용 메서드**이지만, 반환 형태와 용도가 다릅니다.

---

## 1) query()

### 목적

**여러 행(row)을 조회**할 때 사용.

### 특징

* ResultSet의 여러 행을 RowMapper를 이용하여 **List<T>** 형태로 매핑.
* 0건, 1건, 여러 건 모두 허용.
* 결과가 0건이면 **빈 리스트 반환**.
* 결과가 1건이면 리스트 길이가 1.
* 결과가 N건이면 리스트 길이가 N.

### 예

```java
List<Member> members = jdbcTemplate.query(sql, memberRowMapper());
```

즉, 다건(여러 행) 조회에 적합한 메서드입니다.

---

## 2) queryForObject()

### 목적

**정확히 1건(row)만 조회**할 때 사용.

### 특징

* 결과가 정확히 1건이 아니면 예외 발생.

    * 0건 → `EmptyResultDataAccessException`
    * 2건 이상 → `IncorrectResultSizeDataAccessException`
* 반환 타입은 **T 단일 객체**.

### 예

```java
Member member = jdbcTemplate.queryForObject(sql, memberRowMapper(), memberId);
```

즉, "ID로 단일 객체 조회" 같은 경우에 적합합니다.

---

# 정리 표

| 메서드                  | 용도    | 반환        | 0건 결과 | 2건 이상              |
| -------------------- | ----- | --------- | ----- | ------------------ |
| **query()**          | 다건 조회 | `List<T>` | 빈 리스트 | 정상(2개 이상 들어있는 리스트) |
| **queryForObject()** | 단건 조회 | `T`       | 예외    | 예외                 |

---

# 2. update()를 사용하는 이유 (이 코드에서 query()가 아닌 이유)

질문에 나온 코드에서는 INSERT, UPDATE, DELETE를 다음처럼 수행합니다.

```java
jdbcTemplate.update(sql, member.getMemberId(), member.getMoney());
```

이는 **query()나 queryForObject()가 아니라 update()를 반드시 사용해야 하는 이유가 있습니다.**

---

## 1) update()의 목적

`update()`는 아래 SQL을 실행하는 용도로 설계되었습니다.

* INSERT
* UPDATE
* DELETE
* DDL(create, drop 등)

즉, **결과 집합(ResultSet)이 없는 SQL**을 실행할 때 사용하는 메서드입니다.

---

## 2) query() / queryForObject()는 SELECT에서만 사용 가능

query 계열 메서드는 **반드시 ResultSet을 반환하는 SQL(= SELECT)**에서만 사용할 수 있습니다.

DELETE나 UPDATE에 query를 사용하면 다음 같은 오류가 나게 됩니다.

```
Method is only allowed for a query. Use execute or executeUpdate instead of executeQuery;
```

바로 이 오류가 이전 메시지에서 보였던 **H2 90002 에러**입니다.

즉, SELECT가 아닌 SQL은 query 계열로 실행할 수 없습니다.

---

# 결론

### 1) query() / queryForObject() 차이

| 메서드              | 설명                |
| ---------------- | ----------------- |
| query()          | 여러 행 조회 (List 반환) |
| queryForObject() | 1행 조회 (단일 객체 반환)  |

### 2) 이 코드에서 update()를 쓰는 이유

* SQL이 INSERT / UPDATE / DELETE이기 때문에
* SELECT 쿼리가 아니므로
* ResultSet이 없어서 query()·queryForObject()를 사용할 수 없기 때문에
  **update()가 맞는 메서드**

---

# 더 알고 싶다면

원하시면 다음도 자세히 설명해드릴 수 있습니다.

* update(), execute(), batchUpdate() 차이
* query vs queryForObject 내부 처리 흐름 분석
* PreparedStatement에 따라 JdbcTemplate이 어떤 메서드를 호출하는지 내부 코드 분석
* JPA의 persist/update와 JdbcTemplate.update의 차이

`BeanPropertyRowMapper`는 **스프링 JDBC가 제공하는 자동 매핑 RowMapper**입니다.
이 매퍼는 ResultSet의 컬럼 이름을 기반으로 Java 객체의 필드(프로퍼티)에 자동으로 값을 넣어주는 기능을 합니다.

즉, **쿼리 결과를 도메인 객체로 만들 때 직접 RowMapper를 작성할 필요 없이 자동 매핑을 수행**하는 매퍼입니다.

---

# 1. BeanPropertyRowMapper란?

스프링의 `org.springframework.jdbc.core.BeanPropertyRowMapper<T>`는
다음 역할을 수행합니다.

* ResultSet의 컬럼 이름을 가져와서
* Java Bean 규칙(getter/setter) 기반으로
* 동일한 이름의 프로퍼티에 값을 자동으로 설정

예:

| DB 컬럼     | Member 객체 필드 |
| --------- | ------------ |
| member_id | memberId     |
| money     | money        |

스프링은 자동으로 매칭하여 one-to-one으로 설정합니다.

따라서 다음처럼 사용할 수 있습니다.

```java
Member member = jdbcTemplate.queryForObject(
    "select * from member where member_id = ?",
    new BeanPropertyRowMapper<>(Member.class),
    memberId
);
```

---

# 2. 자동 매핑 규칙

BeanPropertyRowMapper는 다음 규칙에 따라 DB 컬럼 → 필드로 매핑합니다.

## 컬럼 이름 → 카멜케이스 변환

예:

* `member_id` → `memberId`
* `order_date` → `orderDate`
* `ACCOUNT_BALANCE` → `accountBalance`

대문자, 언더스코어 모두 인식하여 변환합니다.

## setter 메서드 필수

예:

```java
public void setMemberId(String memberId) { … }
```

setter가 없으면 매핑 불가.

## 데이터 타입 자동 변환

JDBC 타입과 Java 타입 간 변환도 처리합니다.

예:

* VARCHAR → String
* INT → int / Integer
* TIMESTAMP → LocalDateTime
* DATE → LocalDate

---

# 3. RowMapper를 직접 만들 필요 없는 예

아래와 같은 RowMapper는 BeanPropertyRowMapper로 대체됩니다.

```java
private RowMapper<Member> memberRowMapper() {
    return (rs, rowNum) -> {
        Member member = new Member();
        member.setMemberId(rs.getString("member_id"));
        member.setMoney(rs.getInt("money"));
        return member;
    };
}
```

이를 아래처럼 간단히 바꿀 수 있습니다.

```java
new BeanPropertyRowMapper<>(Member.class)
```

---

# 4. BeanPropertyRowMapper의 장점

* RowMapper를 직접 작성할 필요 없이 **자동 매핑**
* 코드가 간결해짐
* 다양한 컬럼 네이밍 규칙을 지원
* 스프링이 내부적으로 Reflection 사용하여 프로퍼티 자동 설정

---

# 5. 단점 및 주의사항

## 1) 컬럼 이름과 필드 이름이 반드시 규칙에 맞아야 한다

예를 들어, DB 컬럼이 `memberid`라면
Java 필드가 `memberId`일 경우 매칭이 안 될 수 있습니다.

## 2) 매핑 오류는 런타임에만 발견됨

Reflection 기반이라 컴파일 타임 검증이 없음.

## 3) 성능은 RowMapper 직접 구현보다 약간 느림

Reflection을 사용하기 때문이지만, 일반적인 규모에서는 무시해도 될 정도입니다.

## 4) DTO 필드가 DB 컬럼에 종속될 수 있음

컬럼 이름 변경 -> DTO 필드도 변경 필요
(따라서 query 전용 DTO에서 사용되는 경우가 많음)

---

# 6. 실무에서의 사용 패턴

### 도메인 엔티티에 직접 사용 → 지양

도메인 필드 구조가 DB 컬럼 구조와 항상 1:1로 일치하지 않기 때문.

### 조회용 DTO에 사용 → 권장

예:

```java
public class MemberDto {
    private String memberId;
    private int money;
}
```

그리고 사용:

```java
jdbcTemplate.query(
    "select member_id, money from member",
    new BeanPropertyRowMapper<>(MemberDto.class)
);
```

---

# 7. 간단 요약

| 특징    | 설명                          |
| ----- | --------------------------- |
| 역할    | ResultSet → Java 객체 자동 매핑   |
| 원리    | 컬럼명 → 카멜케이스 → setter 호출     |
| 장점    | 코드 간결, 자동화                  |
| 단점    | 컬럼/필드 이름 의존성, Reflection 비용 |
| 사용 추천 | 조회용 DTO 매핑                  |

---

BeanPropertyRowMapper의 내부 구현 흐름(Reflection 기반),
성능 이슈, 커스터마이징 방법 등도 필요한 경우 추가로 설명해드릴 수 있습니다.
