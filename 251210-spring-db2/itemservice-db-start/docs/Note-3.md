둘 다 **동일한 동작**을 하지만, 사용하는 방식과 내부 처리에서 약간의 차이가 있어.

---

## 1. `new BeanPropertyRowMapper<>(Item.class)`

이 방식은 **생성자를 직접 호출**해서 RowMapper 객체를 만드는 것.

```java
npjt.query(sql, param, new BeanPropertyRowMapper<>(Item.class));
```

특징

* 가장 직관적이고 흔히 사용됨
* 생성 시점에 `Item.class`를 바로 넘겨서 매핑 정보를 준비함
* Spring 3.x 시절부터 쭉 사용되는 전통적 방식

---

## 2. `BeanPropertyRowMapper.newInstance(Item.class)`

이것도 내부적으로 **동일한 객체를 생성**하지만, **정적 팩토리 메서드**를 사용하는 방식.

```java
npjt.query(sql, param, BeanPropertyRowMapper.newInstance(Item.class));
```

특징

* 팩토리 메서드를 통해 생성
* 가독성이 조금 더 명확하다고 느끼는 사람도 있음 (“새 인스턴스 생성” 의도가 드러남)
* Spring 내부에서 rowMapper 생성 시 이 방식을 추천하는 경우도 있음
* 제네릭 타입 추론이 좀 더 안정적인 경우가 있음 (경우에 따라 컴파일러가 잘 추론함)

---

## 3. 실제 동작 차이는?

**거의 없다.**
두 방식 모두 `BeanPropertyRowMapper<Item>` 객체를 만들고, ResultSet의 컬럼명을 Item 필드명에 매핑하는 동일한 과정을 수행함.

Spring 소스를 보면 `newInstance()`도 결국 `new BeanPropertyRowMapper<>(mappedClass)`를 호출한다.

---

## 4. 언제 무엇을 쓰면 좋나?

* **둘 중 아무거나 써도 무방함**
* 팀 스타일에 따라 정하는 정도
* 정적 팩토리 메서드를 선호하는 팀이라면 `newInstance()`
* 단순/전통 방식 선호하면 `new BeanPropertyRowMapper<>(Item.class)`

---

## 5. 추천?

개인적으로는

```
BeanPropertyRowMapper.newInstance(Item.class)
```

형식을 선호하는 사람들이 늘고 있음.
이유는 가독성 때문이 아니라, 팀에서 `RowMapper`를 정적 팩토리로 통일하는 경우가 많기 때문.

하지만 기능적 차이는 **없다**.

---

원하면 Spring 내부 코드까지 보여주면서 완전히 비교해줄 수도 있어.

아래에 네 가지가 **무엇을 하는 것인지**, 그리고 **왜 쓰는지**를 각각 분명하게 정리해줄게. 전부 JDBC를 더 쉽고 실수 없이 쓰기 위해 Spring이 제공하는 도구들이다.

---

# 1. NamedParameterJdbcTemplate

**이름 기반 파라미터로 SQL을 실행할 수 있게 하는 Spring JDBC 도우미 클래스**

### 핵심 역할

* SQL에서 `:name`, `:price` 같은 **이름 기반 파라미터**를 사용할 수 있게 해준다.
* PreparedStatement의 물음표(?) 순서를 신경 쓸 필요가 없어 코드 가독성이 좋아짐.
* 쿼리 실행 메서드 제공: `query()`, `update()`, `queryForObject()`, 등.

### 예시

```java
String sql = "select * from item where id = :id";
Map<String, Object> param = Map.of("id", 1L);
Item item = npjt.queryForObject(sql, param, new BeanPropertyRowMapper<>(Item.class));
```

---

# 2. SimpleJdbcInsert

**insert SQL을 작성하지 않고도 insert를 자동으로 처리해주는 클래스**

### 핵심 역할

* SQL을 직접 작성할 필요 없이 insert를 수행.
* 자동 증가 키(primary key)를 자동으로 받아올 수 있음.
* 테이블명, 컬럼명만 지정하면 됨.

### 사용 이유

직접 insert SQL을 이렇게 쓰지 않아도 되기 때문:

```java
String sql = "insert into item (item_name, price, quantity) values (?, ?, ?)";
```

### 대신 다음처럼 선언으로 해결

```java
this.sji = new SimpleJdbcInsert(dataSource)
        .withTableName("item")
        .usingGeneratedKeyColumns("id")
        .usingColumns("item_name", "price", "quantity");
```

---

# 3. SqlParameterSource

**SQL 파라미터를 담기 위한 Spring의 추상 인터페이스**

### 역할

NamedParameterJdbcTemplate이나 SimpleJdbcInsert에 값을 전달할 때,
Map 대신 **객체(Object)의 프로퍼티를 기반으로 파라미터를 자동으로 만들어주는 기능**을 제공.

여러 구현체가 있고, 대표적으로 BeanPropertySqlParameterSource를 자주 사용한다.

---

# 4. BeanPropertySqlParameterSource

**Java 객체의 getter 값을 읽어서 SQL 파라미터로 자동 매핑해주는 구현체**

### 특징

* `Item` 객체의 필드명을 SQL 파라미터명으로 자동 매핑한다.
* `getItemName()` → `itemName` → SQL 파라미터 `:itemName`

### 예시

```java
Item item = new Item("pen", 1000, 10);
SqlParameterSource param = new BeanPropertySqlParameterSource(item);
```

이렇게 하면 param 내부 구조는 대략 이렇게 됨:

| 필드명      | 값     |
| -------- | ----- |
| itemName | "pen" |
| price    | 1000  |
| quantity | 10    |

SimpleJdbcInsert나 NamedParameterJdbcTemplate이 이를 읽어서 insert/update 시 자동 바인딩함.

---

# 정리

| 이름                                 | 역할                                |
| ---------------------------------- | --------------------------------- |
| **NamedParameterJdbcTemplate**     | 이름 기반 파라미터로 SQL 실행                |
| **SimpleJdbcInsert**               | insert SQL 없이 insert 처리 + 자동 키 반환 |
| **SqlParameterSource**             | SQL 파라미터를 담는 인터페이스                |
| **BeanPropertySqlParameterSource** | 객체의 필드 → SQL 파라미터 자동 매핑           |

---

원하면 위 코드(`save2`)가 내부적으로 어떻게 동작하는지 SQL 관점에서 한 줄씩 흐름도로 보여줄 수도 있어.

**SimpleJdbcCall**은
**데이터베이스의 Stored Procedure(저장 프로시저)나 Function(함수)을 호출하기 쉽게 만들어주는 Spring JDBC 도우미 클래스**다.

즉, JDBC에서 프로시저를 호출하려면 원래는 굉장히 복잡한 코드를 직접 짜야 하는데,
SimpleJdbcCall을 사용하면 선언적으로 깔끔하게 호출할 수 있다.

---

# 1. 왜 필요해?

JDBC에서 저장 프로시저를 호출하려면
`CallableStatement`를 직접 만들고, 파라미터 타입을 정확히 설정해야 하고,
OUT 파라미터를 읽을 때도 별 코드가 필요하다.

하지만 SimpleJdbcCall은 이런 절차를 추상화해서 자동화한다.

---

# 2. 어떤 상황에 쓰는가?

DB 서버에 아래 같은 프로시저가 있을 때:

```sql
CREATE PROCEDURE get_item_by_id(IN in_id INT, OUT out_name VARCHAR(50))
BEGIN
    SELECT item_name INTO out_name FROM item WHERE id = in_id;
END
```

이걸 Java에서 쉽게 호출하고 싶을 때 SimpleJdbcCall을 쓴다.

---

# 3. 사용 예시

```java
SimpleJdbcCall jdbcCall = new SimpleJdbcCall(dataSource)
        .withProcedureName("get_item_by_id");

Map<String, Object> inParams = Map.of("in_id", 1);

Map<String, Object> out = jdbcCall.execute(inParams);

String itemName = (String) out.get("out_name");
```

### 특징 요약

* 파라미터 이름 기반 매핑 지원
* OUT 파라미터 자동 처리
* Stored Procedure/Function 모두 지원
* 결과 매핑을 자동화할 수 있음 (resultSet → 객체 매핑 가능)

---

# 4. SimpleJdbcInsert / SimpleJdbcCall / NamedParameterJdbcTemplate 비교

| 기능                             | 설명                        |
| ------------------------------ | ------------------------- |
| **NamedParameterJdbcTemplate** | SQL 직접 작성해서 실행 (가장 기본)    |
| **SimpleJdbcInsert**           | insert SQL 자동 생성, 자동 키 반환 |
| **SimpleJdbcCall**             | 프로시저/함수를 간단히 호출하기 위한 도우미  |

---

# 5. 실제로 자주 쓰는가?

최근에는 대부분 JPA, MyBatis를 사용하는 프로젝트가 늘면서 자주 쓰이지는 않지만,
**DB에서 프로시저 중심으로 로직을 운영하는 환경(금융·관공서·Legacy 시스템)**에서는 지금도 널리 사용된다.

---

필요하면 SimpleJdbcCall의

* IN/OUT 매개변수 선언 방식
* Function 호출 예제
* ResultSet 반환 시 RowMapper 쓰는 방법

까지 더 자세히 설명해줄게.

문제의 핵심은 **`(JdbcTemplate) dataSource` 로 캐스팅하려 했기 때문**이다.
`DataSource`는 JDBC 커넥션을 제공하는 객체이고,
`JdbcTemplate`은 Spring이 만든 템플릿 객체라서 **완전히 다른 타입**이다.

그래서 **절대 캐스팅이 불가능**하고, ClassCastException이 발생한다.

---

# 1. 왜 ClassCastException이 발생하나?

`dataSource`는 보통 HikariDataSource 같은 **커넥션 풀 객체**다.

예:

```java
HikariDataSource dataSource
```

이걸 아래처럼 캐스팅하면:

```java
(JdbcTemplate) dataSource
```

타입 자체가 다르기 때문에 실행 시점에 다음 오류가 터진다:

```
ClassCastException: class com.zaxxer.hikari.HikariDataSource cannot be cast to class org.springframework.jdbc.core.JdbcTemplate
```

---

# 2. 올바른 방식은?

JdbcTemplate은 **DataSource를 받아서 생성하는 객체**다.

즉, 이렇게 만들어야 한다:

```java
return new JTItemRepository(new JdbcTemplate(dataSource));
```

---

# 3. 수정된 전체 코드

```java
@Configuration
@RequiredArgsConstructor
public class JTConfig {

    private final DataSource dataSource;

    @Bean
    public ItemRepository itemRepository() {
        return new JTItemRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }
}
```

이렇게 하면 ClassCastException이 발생하지 않는다.

---

# 4. 추가 팁

스프링에서는 JdbcTemplate을 Bean으로 등록해서 주입받아도 된다.

```java
@Bean
public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource);
}

@Bean
public ItemRepository itemRepository(JdbcTemplate jt) {
    return new JTItemRepository(jt);
}
```

이렇게 해도 동일하게 동작한다.

---

필요하면 JTItemRepository 안쪽 코드도 함께 점검해줄게.

맞아.
`JTItemRepository`의 생성자는 **JdbcTemplate 타입**의 `jt`를 받도록 선언되어 있기 때문에,
`JTConfig`에서 `ItemRepository`를 만들 때도 **JdbcTemplate 객체를 넘겨야 한다.**

즉, 아래 두 가지 사실을 정확히 알고 있어야 한다.

---

# 1. `DataSource`와 `JdbcTemplate`은 전혀 다른 타입

* `DataSource` → 커넥션을 제공하는 객체
* `JdbcTemplate` → SQL 실행을 도와주는 Spring의 템플릿 객체

그래서 아래처럼 캐스팅하면 무조건 오류 발생:

```java
(JdbcTemplate) dataSource   // 절대 불가능
```

---

# 2. `JTItemRepository`는 생성자에서 `JdbcTemplate`을 요구함

```java
@Repository
@RequiredArgsConstructor
public class JTItemRepository implements ItemRepository {
    private final JdbcTemplate jt;
}
```

따라서 Config에서 생성할 때도 반드시 이렇게 넘겨야 한다:

```java
new JTItemRepository(new JdbcTemplate(dataSource));
```

---

# 3. 최종적으로 올바른 JTConfig

```java
@Configuration
@RequiredArgsConstructor
public class JTConfig {

    private final DataSource dataSource;

    @Bean
    public ItemRepository itemRepository() {
        return new JTItemRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }
}
```

이렇게 하면 ClassCastException 없이 정상 동작한다.

---

원하면 `NamedParameterJdbcTemplate`이 필요한 경우의 Config 방식도 보여줄게.

테스트 클래스에 **@Transactional**을 붙이면,
**해당 클래스의 모든 테스트 메서드가 각각 트랜잭션 안에서 실행되고, 테스트가 끝나면 자동으로 롤백된다.**

메서드에 붙였을 때와 가장 큰 차이는 **적용 범위**뿐이며, 동작 원리는 동일하다.

아래에서 정확하게 설명할게.

---

# 1. 클래스에 @Transactional을 붙였을 때

```java
@Transactional
@SpringBootTest
class ItemRepositoryTest2 {
```

이렇게 하면:

* **클래스 내부 모든 @Test 메서드는 자동으로 트랜잭션 시작**
* 테스트가 끝나면 **항상 롤백**
* 테스트 데이터가 DB에 남지 않음
* 각 테스트는 서로 독립적인 DB 상태에서 실행됨

즉, 아래 두 메서드는 둘 다 트랜잭션 안에서 실행되고, 끝나면 둘 다 롤백된다.

```java
@Test
void testA() { ... }   // 트랜잭션 시작 → 실행 → 롤백
@Test
void testB() { ... }   // 트랜잭션 시작 → 실행 → 롤백
```

---

# 2. 메서드에 @Transactional을 붙였을 때

```java
@Test
@Transactional
void testA() { ... }
```

이렇게 메서드 단위로 붙이면:

* 해당 메서드만 트랜잭션에서 실행됨
* 나머지 메서드는 트랜잭션 없음

즉, 테스트마다 선택적으로 트랜잭션을 사용할 수 있다.

---

# 3. 동작 차이: 클래스 vs 메서드

| 항목    | 클래스에 @Transactional | 메서드에 @Transactional         |
| ----- | ------------------- | --------------------------- |
| 적용 범위 | 클래스 전체 테스트 메서드      | 특정 메서드만                     |
| 기본 롤백 | 모든 테스트 자동 롤백        | 해당 메서드만 롤백                  |
| 독립성   | 모든 테스트 독립적인 트랜잭션    | 필요할 때만 트랜잭션 사용              |
| 실수 위험 | 적음 (일관된 동작)         | 메서드에 붙이는 걸 잊으면 테스트가 DB를 더럽힘 |

---

# 4. 실제로 가장 많이 쓰는 패턴

**테스트 클래스 전체에 @Transactional을 붙여서 테스트 데이터가 남지 않도록 하는 것**
→ 스프링 테스트의 기본적인 베스트 프랙티스.

Spring Boot 팀이 실제 예제에서도 이 방식을 사용함.

---

# 5. 주의해야 할 점

### 1) 테스트에서는 기본적으로 **commit되지 않고 무조건 롤백**

실제로 insert/update가 잘 되었는지 확인하려면 테스트 내에서만 조회해야 한다.
DB 밖에서 확인하려고 하면 아무 데이터도 없을 것이다.

### 2) @Commit을 붙이면 롤백하지 않고 commit하게 만들 수 있다.

```java
@Test
@Transactional
@Commit
void test() { ... }
```

---

# 결론

클래스 레벨에 @Transactional을 붙이면:

* 모든 테스트가 트랜잭션 안에서 실행되어
* 끝날 때 **자동 롤백**되고
* DB를 더럽히지 않으며
* 테스트 간에 데이터가 섞이지 않는다.

즉, **테스트 안정성과 반복 실행 가능성을 보장하는 설정**이다.

필요하면 실제 실행 순서나 트랜잭션 경계 어떻게 잡히는지 그림으로 설명해줄게.

질문한 설정은 **테스트 환경에서 사용할 전용 DataSource를 등록하는 코드**다.
즉, `@Profile("test")`가 붙어 있기 때문에 **테스트 실행 시에만 이 DataSource가 적용**되고,
운영/개발 환경에서는 전혀 사용되지 않는다.

아래에서 무엇이 달라지는지, 그리고 H2를 쓸 때 각각에 무엇을 적어야 하는지 정확하게 설명할게.

---

# 1. 이 설정을 쓰면 무엇이 달라지나?

### 1) 애플리케이션에서 사용하는 DB가 테스트용으로 바뀐다

`@Profile("test")`가 활성화되면 스프링은 다음 Bean을 로딩한다.

```java
@Bean
@Profile("test")
public DataSource dataSource() { ... }
```

즉,

* 로컬 개발에서는 MySQL 등 실제 DB를 쓰고
* 테스트에서는 **H2 메모리 DB**로 자동 전환

이 방식으로 테스트가 운영 DB를 건드리는 사고를 방지할 수 있다.

---

### 2) 테스트 실행 시 DB는 매번 새로 시작된다

H2의 in-memory 모드(`jdbc:h2:mem:...`)를 사용하면:

* 테스트 시작 시 DB 생성
* 테스트 종료 시 DB 사라짐

테스트를 반복해도 같은 데이터가 남지 않아 **항상 깨끗한 환경에서 테스트**할 수 있다.

---

### 3) 운영/개발 설정과 테스트 설정을 완전히 분리

테스트 DB와 운영 DB를 같은 코드에서 혼용하면 문제가 생길 수 있는데,
`@Profile` 덕분에 아예 다른 환경 설정을 사용하게 된다.

---

# 2. 각 설정에 무엇을 써야 하는가? (H2 기준)

일반적인 설정은 아래처럼 적는다.

```java
@Bean
@Profile("test")
public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.h2.Driver");
    dataSource.setUrl("jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
}
```

---

# 3. 설정값 설명

## 1) driverClassName

```java
org.h2.Driver
```

H2 DB를 사용한다는 의미다.

---

## 2) url

테스트 시 가장 많이 사용되는 H2 메모리 DB URL:

```java
jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1
```

### 주요 옵션 설명

* `mem:testdb` : 메모리에 testdb라는 이름의 데이터베이스를 생성
* `MODE=MYSQL` : MySQL 문법과 타입을 최대한 따라가도록 설정
  (실제 운영이 MySQL이면 이 옵션 매우 중요함)
* `DB_CLOSE_DELAY=-1` : 커넥션이 닫혀도 DB가 사라지지 않도록 유지

이렇게 쓰면 스프링 테스트에서 안정적으로 동작한다.

---

## 3) username / password

H2의 기본값:

```java
username: sa  
password: "" (빈 문자열)
```

---

# 4. 테스트 프로파일 활성화 방법

### application-test.properties

```properties
spring.profiles.active=test
```

또는 테스트 클래스에:

```java
@ActiveProfiles("test")
@SpringBootTest
class ItemRepositoryTest2 {
}
```

이렇게 하면 방금 만든 test용 DataSource가 사용된다.

---

# 요약

| 항목                 | 의미                                                |
| ------------------ | ------------------------------------------------- |
| `@Profile("test")` | 테스트에서만 이 DataSource를 사용                           |
| driver             | `org.h2.Driver`                                   |
| url                | `jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1` |
| user               | sa                                                |
| password           | 빈 문자열                                             |

테스트 시에는 실제 DB를 전혀 쓰지 않게 되고,
테스트마다 새로운 H2 인메모리 DB가 생성되어 **빠르고 안전한 테스트 환경**을 제공한다.

---

원하면 H2를 파일 모드로 사용하는 방법(`jdbc:h2:file:...`)이나,
schema.sql / data.sql 자동 실행 방식도 설명해줄게.

결론부터 말하면:

**H2 인메모리 DB는 테이블을 미리 만들어두지 않으면 바로 사용할 수 없다.**
하지만 Spring Boot가 **자동으로 테이블을 생성하게 만들 수는 있다.**

즉,

* **직접 테이블을 만들 필요는 없게 만들 수 있다.**
* 하지만 **DB가 아무것도 없이 자동으로 알아서 테이블을 만들어주는 것은 아니다.**
  (JPA/Hibernate를 쓰는 경우는 자동 생성 가능)

JDBC를 직접 쓰고 있다면 테이블 생성 SQL을 반드시 제공해야 한다.

아래에서 경우별로 정리해줄게.

---

# 1. H2는 아무 스키마 없이 그냥 실행되면 테이블이 없다

H2 인메모리 DB는 아래 URL처럼 메모리에서 실행된다:

```
jdbc:h2:mem:testdb
```

이 DB는 **빈 상태에서 시작**한다.
따라서 아래 같은 SQL을 실행하려고 하면:

```sql
select * from item;
```

H2는 다음 오류를 낸다:

```
Table "ITEM" not found
```

즉, **테이블은 반드시 만들어져 있어야 한다.**

---

# 2. 그럼 스프링이 자동으로 테이블을 만들어줄 수 있는가?

## (1) JPA/Hibernate 사용 시 → 자동 생성됨

예:
`spring.jpa.hibernate.ddl-auto=create`
또는 `update`, `create-drop` 등을 쓰면
**엔티티 클래스 기반으로 테이블이 자동 생성된다.**

하지만 지금은 **JDBC Template 기반 프로젝트**이므로 해당 옵션은 적용되지 않는다.

---

## (2) JDBC 사용 시 → schema.sql로 자동 생성 가능

Spring Boot는 다음 파일을 자동 실행한다.

* `schema.sql`
* `data.sql`

즉, `src/main/resources/schema.sql`에 테이블 생성 SQL을 넣으면
Spring Boot가 **애플리케이션 시작 시 자동 실행**해서 테이블을 만들어준다.

예:
`schema.sql`

```sql
create table item (
    id bigint generated by default as identity primary key,
    item_name varchar(255),
    price int,
    quantity int
);
```

이렇게 하면 별도로 만들 필요 없이,
테스트 시작할 때 Spring Boot가 자동 생성한다.

---

# 3. 그래서 결론은?

### 바로 쓸 수 있는 것 = X

테이블은 반드시 필요함.

하지만 Spring Boot에게 다음 중 하나를 맡기면 **직접 실행하지 않아도 됨**:

1. `schema.sql`을 이용한 자동 초기화
2. JPA 사용 시 ddl-auto 옵션
3. Flyway / Liquibase 사용

JDBC만 쓰는 경우라면 **schema.sql 방식이 가장 일반적**이다.

---

# 4. 추천 방식 (JDBC + H2 테스트 환경)

`src/test/resources/schema.sql` 추가:

```sql
create table item (
    id bigint auto_increment primary key,
    item_name varchar(255),
    price int,
    quantity int
);
```

그리고 테스트 프로파일에서 이것이 자동 실행되도록 한다.

이렇게 하면 **H2에 테이블을 만들지 않아도 테스트 시작할 때 자동으로 생성**된다.

---

원하면 schema.sql / data.sql의 실행 순서나,
테스트 환경에서 프로파일별 실행 우선순위도 자세히 설명해줄게.

핵심은 **당신이 “이미 만들어놓은 H2 데이터베이스”가 어떤 모드로 만들어졌느냐**에 따라 달라진다.
즉, *메모리 모드인지*, *파일 모드인지*, *서버 모드인지*에 따라 테스트에서 “그 DB를 그대로 사용해도 되는지”가 결정된다.

아래에서 명확하게 구분해줄게.

---

# 1. H2는 크게 세 가지 모드가 있다

## 1) In-Memory 모드 (메모리)

```
jdbc:h2:mem:testdb
```

특징

* DB가 메모리에만 존재
* JVM 종료 시 DB 사라짐
* 테스트에서 가장 일반적으로 사용
* 기존 DB 파일이든 뭐든 **재사용 불가**
* 항상 fresh한 DB

---

## 2) Embedded(파일) 모드 (로컬 파일DB)

```
jdbc:h2:file:~/testdb
jdbc:h2:file:./data/testdb
```

특징

* 파일로 DB가 디스크에 저장됨
* 테이블을 만들어 놓으면 계속 재사용 가능
* 테스트에서 사용 가능하지만 주의 필요
  (테스트가 DB 내용을 망칠 수 있음)

당신이 말하는 “이미 만들어놓은 H2 DB”가 **대부분 이 파일 모드일 가능성 높음**.

---

## 3) Server 모드 (TCP 접속)

```
jdbc:h2:tcp://localhost/~/testdb
```

특징

* 별도의 H2 서버(프로세스)가 필요
* 파일 모드보다 조금 더 외부 접속이 자유로움
* 테스트에서도 사용할 수는 있지만 매우 드묾

---

# 2. “이미 만들어놓은 H2 DB”를 테스트에서 써도 괜찮은가?

## 결론: **되긴 한다. 하지만 테스트에는 매우 비추천이다.**

### 이유:

1. 테스트 실행 시 DB의 기존 데이터가 테스트를 방해할 수 있다
2. 테스트 메서드가 데이터를 변경하면 실제 파일 DB가 오염된다
3. 테스트가 독립적이지 않게 된다
4. 스프링 테스트는 기본적으로 트랜잭션 롤백을 기대하는데, 파일 모드에서는

    * 트랜잭션을 테스트마다 관리하기 어렵다
5. 테스트를 여러 번 반복하면 DB 상태가 계속 누적됨 → 테스트 불안정

---

# 3. 그러면 어떤 경우에는 재사용이 괜찮은가?

### "테스트용 H2 파일 DB를 초기 데이터와 함께 구축해두고, 테스트에서 읽기 전용으로만 사용하는 경우"

예)

* 테스트 DB에 기초 데이터가 이미 있고
* 테스트는 조회만 하고
* insert/update/delete를 하지 않음

**하지만 대부분의 프로젝트 테스트에서는 권장되지 않는다.**

---

# 4. 가장 권장되는 방식 (강력 추천)

**테스트에서는 무조건 In-Memory H2 DB를 새로 만들어 사용하는 것**

테스트 시작 시:

* DB 생성
* schema.sql 자동 실행
* data.sql 자동 실행(필요하면)
* 테스트 끝나면 DB 삭제

이 방식의 장점:

* 테스트 간 독립성 보장
* DB 오염 없음
* 반복 실행 항상 동일 결과
* 빠름

---

# 5. 그래도 “이미 만들어둔 H2 파일 DB”를 테스트에서 쓰고 싶다면?

가능은 함.
방법:

```java
dataSource.setUrl("jdbc:h2:file:./mydb/testdb;MODE=MYSQL");
```

다만 다음 문제에 대비해야 한다.

* 데이터 reset 필요
* 테스트끼리 간섭 발생
* rollback이 의미 없음
* 테스트 코드가 DB 상태에 따라 실패할 수 있음
* CI 서버에서는 동일한 DB 파일이 보장되지 않음

즉, **테스트 신뢰성이 크게 떨어진다.**

---

# 최종 결론

**이미 만들어둔 H2 DB 파일을 테스트에서 “쓸 수는 있지만 추천되지 않는다.”**
테스트는 인메모리 DB로 깨끗하게 시작하는 것이 훨씬 낫다.

* H2 메모리 모드 → 테스트 전용, 빠르고 깨끗함
* 기존 H2 파일 DB → 테스트 오염, 불안정, 반복 실행 어려움

그래서 테스트 환경에서는 보통:

```
jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1
```

이렇게 새로 생성하는 것이 정상적이고 안정적이다.

---

원하면

* 현재 가지고 있는 H2 파일 DB가 어떤 모드인지 파악하는 방법
* 테스트 프로파일 구성 예시
* schema.sql 자동 생성 설정

같은 것도 정리해줄게.

지금 상황을 정리하면 다음과 같다.

* 당신은 **인메모리 H2를 명시적으로 설정하지 않았는데도**
* `src/test/resources/schema.sql`에 있는 스키마가 **자동으로 실행되었고**
* 테스트가 정상 동작했다.

이 현상은 전혀 이상한 것이 아니다.
Spring Boot가 **테스트 시 자동으로 H2 인메모리 DB를 사용했기 때문**이다.

즉, 당신이 “인 메모리 DB를 만들라고 따로 설정하지 않아도”
Spring Boot가 **테스트 환경에서는 기본적으로 인메모리 H2를 사용한다.**

이제 이것을 정확하게 설명할게.

---

# 1. Spring Boot는 테스트 시 기본 DB 전략을 자동 적용한다

Spring Boot는 H2 의존성이 추가되어 있고
`application-test.properties` 같은 곳에서 특별한 설정이 없을 경우,

**테스트 환경에서는 자동으로 H2 인메모리(DB) DataSource를 만든다.**

즉, 아래 URL 형태의 인메모리 DB가 자동 생성된다.

```
jdbc:h2:mem:testdb
```

이것은 Boot의 **AutoConfiguration** 때문이며,
다음 조건을 만족하면 자동 설정이 활성화된다.

조건

1. DataSource 빈을 직접 만들지 않았다
2. H2 라이브러리가 있다
3. Spring Boot Test가 실행 중이다

그러면 Boot는 다음 빈을 자동 생성한다.

* DataSource(H2 in-memory)
* JdbcTemplate
* NamedParameterJdbcTemplate

그래서 따로 설정하지 않았어도 자동으로 H2(mem) DB가 쓰인다.

---

# 2. schema.sql은 자동 실행된다

Spring Boot는 다음 파일을 자동 실행한다.

* `schema.sql`
* `data.sql`

그리고 그 우선순위는 다음과 같다.

* src/test/resources/schema.sql
  → 테스트 환경에서 가장 먼저 사용됨
* src/main/resources/schema.sql
  → test에 없으면 사용

따라서 테스트를 실행하면:

1. Spring Boot가 인메모리 H2 DB를 생성
2. `src/test/resources/schema.sql` 자동 실행
3. 테이블 생성
4. 테스트 코드 실행
5. 테스트 종료 → 트랜잭션 롤백
6. JVM 종료 → 인메모리 DB 삭제

이렇게 전체 흐름이 자동으로 진행된다.

---

# 3. 당신이 명시적으로 DataSource 설정을 하지 않았기 때문에 자동 적용됨

즉, 다음과 같은 설정이 테스트에 존재하지 않았기 때문에:

```java
@Bean
public DataSource dataSource() {
    ...
}
```

Spring Boot는 “DataSource가 없으니 자동 구성해야겠다” 하고 판단한다.

그리고 테스트 환경에서 H2가 있다면 인메모리 DB를 기본으로 선택한다.

---

# 4. 그래서 결론

### 1) 당신이 인메모리 DB를 "직접" 만든 게 아니다

Spring Boot가 자동으로 만든 것이다.

### 2) schema.sql이 실행된 것도 "직접 실행한 게 아니다"

Spring Boot가 자동으로 실행한 것이다.

### 3) 테스트가 성공한 것은 "자동 구성된 H2 인메모리 DB 덕분"이다

그래서 파일 DB든, 외부 DB든 필요 없다.

---

# 5. 정리

| 항목                     | 설명                       |
| ---------------------- | ------------------------ |
| DataSource를 직접 정의하지 않음 | Boot가 자동으로 H2 인메모리 DB 생성 |
| schema.sql 배치          | Boot가 자동 실행              |
| drop/create SQL 정상 동작  | 인메모리 DB가 살아 있어서 실행 가능    |
| 테스트 종료 후 DB 삭제         | 인메모리 DB 특성               |

---

# 추가로 알려줄 내용

원하면 다음도 설명해줄 수 있어:

* 스프링 부트가 인메모리 DB를 자동으로 선택하는 조건
* schema.sql을 강제 비활성화하는 방법
* test/resources/data.sql을 추가하면 초기 데이터 넣는 방법
* 프로파일(test/dev/prod)마다 다른 DB 구성하는 구조

말하면 바로 정리해줄게.

지금 상황을 연결해서 보면 이렇게 된다.

* 당신은 **@Profile("test")** 이 붙은 DataSource 빈을 만들었다.
* 그리고 **테스트를 실행했다.**
* 따라서 스프링은 **test 프로파일이 활성화되었다고 판단**하고,
  그 안에 정의한 H2 인메모리 DataSource 빈을 사용했다.

그래서 “내가 인메모리 DB를 따로 설정하지 않았는데도 schema.sql이 실행되면서 정상 동작했다”는 현상이 자연스럽게 일어난 것이다.

이걸 단계별로 정확하게 설명할게.

---

# 1. 테스트 실행 시 Spring Boot는 기본적으로 "test" 프로파일을 활성화한다

JUnit + @SpringBootTest 환경에서:

* `@ActiveProfiles("test")`를 직접 붙였거나
* 테스트 설정(application-test.properties)이 있거나
* 또는 IDE에서 test 프로파일을 활성화했다면

**스프링은 test 프로파일을 활성화해서 실행한다.**

그러면 @Profile("test")가 붙은 Bean이 모두 로딩된다.

그중 하나가 바로:

```java
@Bean
@Profile("test")
public DataSource dataSource() { ... }
```

이 빈이다.

---

# 2. 따라서 테스트에서는 이 DataSource가 강제로 적용된다

Spring Boot 자동 구성(DataSourceAutoConfiguration)은 다음 규칙으로 동작한다.

* **DataSource Bean이 이미 등록돼 있으면 자동 구성은 동작하지 않는다.**
* 즉, test 프로파일이 활성화되면
  → 당신이 만든 `dataSource()` Bean이 등록된다.
  → Boot 자동 구성은 비활성화된다.

그 결과:

* 스프링은 **항상 이 DataSource만 사용**하게 된다.
* 이 DataSource는 **H2 인메모리 DB** (`mem:testdb`) 이다.

---

# 3. 그러면 schema.sql은 언제 실행되나?

테스트용 DataSource가 인메모리 DB이기 때문에:

1. 테스트 시작 시 DB가 새로 생성됨
2. Spring Boot가 **src/test/resources/schema.sql**을 자동 실행함
3. 테이블 생성됨
4. 테스트 코드에서 DB 사용
5. 테스트 종료 시 롤백
6. JVM 종료 시 DB 사라짐

이렇게 매번 깨끗한 DB가 만들어져서 테스트가 독립적으로 실행된다.

---

# 4. 결론: “테이블을 만들지 않았는데도 테스트가 돌아간 것처럼 보인 이유”

사실은 다음 두 가지가 자동으로 실행된 것:

1. @Profile("test") 덕분에
   → H2 인메모리 DataSource가 활성화됨
2. schema.sql 덕분에
   → 테스트 시작할 때 테이블 자동 생성됨

즉,

**명시적으로 "인메모리 DB를 생성하라" 설정을 하지 않아도,
이미 test 프로파일 DataSource를 인메모리 DB로 지정했기 때문에
항상 인메모리 DB가 생성되어 정상 동작한 것이다.**

---

# 5. 확인 체크리스트 (당신이 겪은 현상이 완전 정상이라는 증거)

* test 프로파일 활성화
* @Profile("test") DataSource = 인메모리 H2
* schema.sql 자동 실행
* 테스트는 항상 롤백
* 테스트 종료 후 DB 사라짐

→ 그래서 “DB를 안 만들었는데 돌아가네?”라고 느껴질 수밖에 없다.

---

필요하면:

* test 프로파일 비활성화 시 어떤 DB가 쓰이는지
* 여러 DataSource 중 어떤 것이 선택되는지
* schema.sql / data.sql 자동 실행 우선순위

도 추가로 정리해줄게.

아래 두 질문을 각각 명확히 정리해 설명하겠다.

---

## 1. “지금 정의한 DataSource는 임베디드 모드야?”

네.
작성한 설정은 **H2 메모리 DB**를 직접 생성하는 것이므로 **임베디드 모드(Embedded Mode)** 로 동작한다.

코드 분석:

```java
dataSource.setDriverClassName("org.h2.Driver");
dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
```

* `jdbc:h2:mem:...` → **메모리 DB** 를 의미
* 애플리케이션 내에서만 존재하며 외부 서버(H2 TCP 서버 등)에 연결하는 방식이 아니다
* 따라서 **임베디드 모드**가 맞다

즉, 이 Bean을 정의하면 스프링이 자동 설정하는 DataSource 대신 **직접 등록한 메모리 DB DataSource**를 사용하게 된다.

---

## 2. “DataSource 정의 안 하면 스프링부트가 임베디드 모드로 접근하는 DataSource 만들어 등록한다. 이거는 properties 파일 정보대로 연결하는 걸 만든다는 거야?”

정확히 말하면 **두 상황으로 나뉜다.**

### 상황 A. properties에 DB 설정(`spring.datasource.*`)이 있을 때

예:

```
spring.datasource.url=jdbc:mysql://localhost/test
spring.datasource.username=root
spring.datasource.password=1234
```

이 경우 스프링부트는 **임베디드 DB를 사용하지 않고**,
**properties 값으로 External DB(MySQL 등)에 연결하는 DataSource를 자동 생성한다.**

즉,
설정이 존재하면 → 외부 DB용 DataSource 자동 생성
임베디드 모드 아님

---

### 상황 B. properties에 어떤 DataSource 설정도 없을 때

이 경우 스프링부트는 다음 규칙에 따라 움직인다:

1. H2, HSQLDB, Derby 같은 **임베디드 DB 라이브러리가 classpath에 있으면**
   → 자동으로 해당 DB를 **임베디드 모드로 접근하는 DataSource**를 생성한다.
2. 임베디드 DB 라이브러리가 없으면
   → DataSource를 구성할 수 없어서 에러 발생

즉,

* **properties가 없고**
* **H2 의존성이 있다**

→ 자동으로 *"jdbc:h2:mem:testdb"* 같은 **임베디드 H2 메모리 DB** DataSource가 만들어진다는 의미다.

---

## 요약

| 조건                              | 어떤 DataSource가 생성되는가?                         |
| ------------------------------- | --------------------------------------------- |
| **네가 직접 Bean으로 DataSource 등록**  | 등록한 내용이 우선 → 지금처럼 임베디드 H2 메모리 DB              |
| **properties에 DB 설정 있음**        | 그 DB(MySQL, PostgreSQL 등)에 연결하는 일반 DataSource |
| **properties 없음 + H2 라이브러리 존재** | 스프링부트가 자동으로 **임베디드 H2 메모리 DB** 생성             |
| **properties 없음 + 임베디드 DB 없음**  | DataSource 구성 불가 → 에러                         |

---

필요하다면 `spring.datasource.*` 설정의 우선순위나 테스트 환경 분리 방식(프로파일, @TestConfiguration 등)까지 더 상세히 설명할 수도 있다.

결론부터 말하면 **“임베디드(embedded)”와 “인-메모리(in-memory)”는 다른 개념**이지만, **H2 같은 DB에서는 보통 둘이 함께 쓰이는 경우가 많아 헷갈리는 것**이다.

아래에서 명확히 구분해 보겠다.

---

# 1. 임베디드(Embedded) = 프로세스 내부에서 DB가 동작하는 방식

* 애플리케이션 **JVM 내부에서 DB가 함께 실행**됨
* **외부 서버가 필요 없음**
* 파일로 저장하는 embedded도 있고, 메모리에 저장하는 embedded도 있다.

즉, 임베디드 모드는 DB 서버를 별도로 실행하지 않고,
**내 애플리케이션이 DB 엔진을 포함해서 돌리는 방식**이라고 이해하면 된다.

예:

* `jdbc:h2:~/test` → 파일 기반이지만 **임베디드 모드**
* `jdbc:h2:mem:testdb` → 메모리 기반이지만 **임베디드 모드**

---

# 2. 인-메모리(In-Memory) = 저장 위치가 메모리인지 여부

* 데이터가 **파일이 아니라 메모리에 저장**됨
* 애플리케이션 종료 시 데이터 사라짐
* 메모리에 저장될 뿐, DB 서버는 외부일 수도 있고 내부일 수도 있다
  (대부분 embedded지만 개념적으로는 별개)

예:

* `jdbc:h2:mem:testdb` → 메모리 기반(in-memory)
* `jdbc:h2:file:~/test` → 파일 기반(on-disk)

---

# 3. 둘의 관계 정리

| 개념                   | 의미                  | H2 예시                                        |
| -------------------- | ------------------- | -------------------------------------------- |
| **임베디드(embedded)**   | 애플리케이션 안에서 DB 엔진 실행 | `jdbc:h2:mem:testdb` , `jdbc:h2:file:~/test` |
| **인-메모리(in-memory)** | 데이터 저장이 메모리에서만 이루어짐 | `jdbc:h2:mem:testdb`                         |

즉,

* **임베디드 ≠ 인-메모리**
* **인-메모리는 저장 방식**, **임베디드는 실행 방식**

하지만 H2에서는
`jdbc:h2:mem:...`
처럼 **메모리 DB가 곧 임베디드 모드로 동작하는 경우가 많아서 둘이 같이 쓰여 헷갈리는 것**이다.

---

# 4. 네 코드의 설정은 어떤 모드?

```java
dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
```

→ **임베디드 모드 + 인-메모리 모드 둘 다 해당된다.**

* 임베디드: 애플리케이션 내부에서 H2 DB 엔진이 실행됨
* 인-메모리: 메모리에 DB가 생성되고 종료 시 사라짐

---

필요하면 H2의 Server 모드(TCP 모드)와 임베디드 모드 차이도 정리해 줄 수 있다.
