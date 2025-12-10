아래 코드는 **검색 조건(ItemSearchCond)에 따라 동적으로 SQL을 만들고**,
JdbcTemplate으로 DB에서 `Item` 목록을 조회하는 메서드다.

각 줄이 어떤 역할을 하는지 순서대로 정확히 분석해줄게.

---

# 1. 조건 읽기

```java
String itemName = cond.getItemName();
Integer maxPrice = cond.getMaxPrice();
```

검색 조건 객체(`ItemSearchCond`)에 들어 있는 두 가지 조건을 꺼낸다.

* `itemName`: 상품명 부분 검색
* `maxPrice`: 가격 상한

둘 다 선택적(optional)이므로 null일 수도 있음.

---

# 2. 기본 SQL 시작

```java
String sql = "select id, item_name, price, quantity from item";
```

조건이 없으면 전체 조회.
조건이 있으면 뒤에 `where ...`가 붙는다.

---

# 3. WHERE 절이 필요한지 판단

```java
if (StringUtils.hasText(itemName) || maxPrice != null) {
    sql += " where";
}
```

* 검색 조건 둘 중 하나라도 존재하면 `where`를 붙인다.
* 아직 실제 조건은 붙이지 않고 단어만 넣은 상태.

---

# 4. 파라미터 리스트 초기화

```java
boolean andFlag = false;
List<Object> param = new ArrayList<>();
```

* `andFlag`: 이미 조건이 하나 붙었는지 판단하는 플래그
  → 두 번째 조건부터는 `and`가 필요하다.
* `param`: SQL에 들어갈 `?` 값들을 저장할 리스트

---

# 5. itemName 조건이 있는 경우

```java
if (StringUtils.hasText(itemName)) {
    sql += " item_name like concat('%', ?, '%')";
    param.add(itemName);
    andFlag = true;
}
```

* SQL에 `item_name like '%?%'` 조건을 추가한다.
* 사용자 입력을 직접 문자열로 넣지 않고 `?` 바인딩 방식 사용
* 바인딩할 값은 `param` 리스트에 추가
* 첫 조건이므로 `andFlag = true`

---

# 6. maxPrice 조건이 있는 경우

```java
if (maxPrice != null) {
    if (andFlag) {
        sql += " and";
    }
    sql += " price <= ?";
    param.add(maxPrice);
}
```

* 두 번째 조건일 수 있으므로, 이미 조건이 하나 붙었으면 `and` 추가
* 가격은 `<= ?` 형태로 들어감
* 바인딩 값(`maxPrice`)을 리스트에 추가

---

# 7. 최종 DB 조회 수행

```java
return jt.query(
        sql,
        new BeanPropertyRowMapper<>(Item.class),
        param.toArray()
);
```

### 여기서 중요한 점

#### (1) `jt.query(...)`

JdbcTemplate의 조회 메서드
SQL + 행 매핑(RowMapper) + 바인딩 파라미터 배열을 전달한다.

#### (2) `new BeanPropertyRowMapper<>(Item.class)`

* DB 컬럼명 → Java 객체 필드명 자동 매핑
* 규칙: `item_name` → `itemName` (snake_case → camelCase)

#### (3) `param.toArray()`

앞에서 모은 `?` 바인딩 값을 순서대로 배열로 변환하여 넣는다.

---

# 8. 전체 동작 요약

| 단계 | 내용                              |
| -- | ------------------------------- |
| 1  | 검색 조건 읽기                        |
| 2  | 기본 SELECT 문 생성                  |
| 3  | 조건 존재 시 WHERE 추가                |
| 4  | 조건에 따라 SQL 덧붙임 (`and` 처리 포함)    |
| 5  | 바인딩 파라미터 리스트 구성                 |
| 6  | JdbcTemplate로 SQL 실행            |
| 7  | 조회된 row를 Item 객체로 자동 매핑해 리스트 반환 |

동작 예시는 다음과 같다.

### 예제 1: itemName만 있을 때

입력:
itemName = "A", maxPrice = null

만들어지는 SQL:

```sql
select id, item_name, price, quantity from item
 where item_name like concat('%', ?, '%')
```

파라미터:

```
[A]
```

---

### 예제 2: itemName + maxPrice 있을 때

itemName = "A", maxPrice = 20000

SQL:

```sql
select id, item_name, price, quantity from item
 where item_name like concat('%', ?, '%') and price <= ?
```

파라미터:

```
[A, 20000]
```

---

### 예제 3: 조건이 아무것도 없을 때

SQL:

```sql
select id, item_name, price, quantity from item
```

파라미터 없음

---

필요하면 다음도 설명해줄 수 있어요:

* BeanPropertyRowMapper의 내부 동작 방식
* 동적 SQL을 직접 문자열로 조립할 때 문제점
* MyBatis나 JPA Criteria API와 비교
* SQL 인젝션 방지 측면에서 이 코드가 안전한 이유

`concat('%', ?, '%')` 를 사용하는 이유는 **파라미터 바인딩 방식으로 안전하게 부분 검색을 처리하기 위해서**이다.

조금 더 구체적으로 설명하면 다음과 같다.

---

# 1. 문자열을 직접 이어붙이면 안 되는 이유

부분 검색을 위해 보통 이렇게 하고 싶을 수 있다:

```sql
item_name like '%' + ? + '%'
```

하지만 **SQL에서는 문자열 덧셈(+)이 안 되고**,
DB마다 문자열 연결 방식이 다르다.

또 아래 방식처럼 파라미터에 직접 `%`를 넣을 수도 있다.

```java
param.add("%" + itemName + "%");
```

이 방식은 **가능하긴 하지만 비추천**이다. (아래에서 이유 설명)

---

# 2. concat을 쓰면 DB가 문자열 결합을 처리해준다

MySQL, MariaDB 등에서 문자열 결합은 `concat()`을 사용한다.

```sql
concat('%', ?, '%')
```

이 코드는 다음을 의미한다.

1. 앞에 `%` 문자열
2. 뒤에 `%` 문자열
3. 그 사이에 바인딩된 검색어(?)

결과적으로 SQL 실행할 때는 이런 문자열이 된다:

```
%검색어%
```

그리고 LIKE 검색이 수행된다.

---

# 3. concat을 쓰는 이유 (중요한 3가지)

## 1) **SQL 인젝션 방지**

검색어를 직접 문자열로 붙이지 않고
`?` 바인딩을 사용하기 때문에 안전하다.

직접 이어 붙이면 이런 위험이 있다:

```sql
where item_name like '%" + userInput + "%'
```

→ 사용자 입력 안에 `'`, `or 1=1` 같은 패턴이 들어가면 위험.

하지만 concat('%', ?, '%')는 내부적으로 **PreparedStatement** 바인딩을 사용하므로 인젝션 위험이 없다.

---

## 2) **JDBC와 쿼리 바인딩 메커니즘을 자연스럽게 활용**

JDBC에서 LIKE 검색 시 `?` 위치에 **온전한 값 하나**만 전달할 수 있다.

* 잘못된 방식:

  ```sql
  like '%?%'
  ```

  이렇게 하면 %가 파라미터 안에 포함되지 않고 SQL 문법 오류가 난다.

* 올바른 방식:

  ```sql
  like concat('%', ?, '%')
  ```

DB가 문자열 결합을 처리하므로, 파라미터는 순수한 값만 들어간다.

---

## 3) **DB 독립성은 낮지만, 명확하고 안정적인 방식**

MySQL/MariaDB에서는 concat이 잘 동작한다.
H2에서도 `||` 대신 concat을 지원하도록 설정할 수 있다.

---

# 4. 대안도 존재한다

실제로 많은 실무 코드에서는 이렇게 처리한다.

### 대안 1) 파라미터에 '%'를 직접 붙이는 방식

```java
param.add("%" + itemName + "%");
sql += " item_name like ?";
```

이 방식도 안전한데, 단점은 “검색 값 가공 책임이 SQL 생성부에 있음”.

### 대안 2) DB마다 문자열 연결 방식이 다를 수 있음

* MySQL: `concat`
* Oracle: `||`
* PostgreSQL: `||`

그래서 MyBatis나 JPA에서는 문자열 처리 로직을 프레임워크가 해주기도 한다.

---

# 정리

`concat('%', ?, '%')`를 쓰는 이유는 다음 때문이다.

1. **LIKE 검색어에 %를 동적으로 안전하게 붙이기 위해**
2. **SQL 인젝션을 피하면서 PreparedStatement 바인딩을 유지하기 위해**
3. **LIKE '%검색어%' 형태를 SQL 표준 문법에 맞게 구성하기 위해**

---

원하면 “LIKE 검색 시 parameter binding이 어떻게 작동하는지”
혹은 “concat 없이 LIKE 검색을 안전하게 처리하는 여러 방식”도 자세히 설명해줄게.

SQL에서 `%`는 **LIKE 연산자에서 사용하는 와일드카드(wildcard)**로,
**0개 이상의 임의의 문자열을 의미한다.**

즉, `%`는 “아무 문자(문자열)든 몇 개든 와도 된다”는 뜻이다.

아주 핵심만 예제로 설명해볼게.

---

# 1. `%`의 기본 의미

| 패턴    | 매칭되는 문자열       |
| ----- | -------------- |
| `%A`  | A로 끝나는 모든 문자열  |
| `A%`  | A로 시작하는 모든 문자열 |
| `%A%` | A가 포함된 모든 문자열  |
| `%`   | 모든 문자열 (전체 매칭) |

---

# 2. LIKE에서 `%`가 사용되는 이유

예를 들어 검색어가 `"car"`라면:

### 1) `%car`

끝이 `"car"`로 끝나는 모든 문자열
예: `mycar`, `supercar`

### 2) `car%`

앞이 `"car"`로 시작
예: `carpet`, `carbon`, `carshop`

### 3) `%car%`

중간에 `"car"`가 들어있는 모든 문자열 → 가장 흔한 부분 검색
예:

* `carrot`
* `escarp`
* `mycarshop`
* `scary`

---

# 3. 이 코드에서는 왜 `%`를 양쪽에 붙였나?

검색 문자열을 “포함 검색”으로 하고 싶기 때문이다.

코드:

```sql
item_name like concat('%', ?, '%')
```

→ `'%검색어%'` 형태가 되고
→ 검색어가 중간에 포함된 모든 item을 찾는다.

---

# 4. `%`와 `_`의 차이

LIKE에서 사용하는 와일드카드는 두 가지다:

| 와일드카드 | 의미            |
| ----- | ------------- |
| `%`   | 임의의 문자열 0개 이상 |
| `_`   | 임의의 문자 정확히 1개 |

예:

* `A_B` → A로 시작하고, 그 다음 아무 문자 1개, 그 다음 B로 끝나는 문자열
  (예: `ACB`, `A1B`)

---

원하면 LIKE의 전체 패턴 매칭 규칙도 더 자세히 설명해줄 수 있어.

이 코드는 **DB에 새로운 Item을 INSERT하면서, DB가 자동 생성한 id(자동 증가 PK)를 받아오는 과정**을 구현한 것이다.
JdbcTemplate로 **auto-generated key(자동 생성 키)**를 읽어오는 정석적인 작성 방식이다.

아래에서 한 줄씩 완전하게 설명해줄게.

---

# 1. SQL 준비

```java
String sql = "insert into item (item_name, price, quantity) values (?, ?, ?)";
```

DB에 새로운 row를 삽입하기 위한 INSERT 구문이다.
대부분의 RDBMS(H2, MySQL)는 id 컬럼을 auto increment 로 설정해둔다.

---

# 2. KeyHolder 생성

```java
KeyHolder keyHolder = new GeneratedKeyHolder();
```

INSERT 후 DB가 생성한 **자동 증가 id 값을 담아둘 객체**다.
JdbcTemplate이 내부에서 이 객체에 값을 넣어준다.

---

# 3. JdbcTemplate update 호출

```java
jt.update(con -> {
    PreparedStatement pstmt = con.prepareStatement(sql, new String[]{"id"});
    pstmt.setString(1, item.getItemName());
    pstmt.setInt(2, item.getPrice());
    pstmt.setInt(3, item.getQuantity());
    return pstmt;
}, keyHolder);
```

이 부분이 핵심이다.

## (1) 람다에서 PreparedStatement 생성

`con.prepareStatement(sql, new String[]{"id"})`

* INSERT 수행 후 **어떤 컬럼의 자동 생성 키를 반환받을 것인지 지정**
* `"id"` 라고 적었으므로 DB가 생성한 id 값이 KeyHolder에 저장된다

즉, JDBC의 다음 문법을 그대로 사용한 것:

```java
prepareStatement(sql, new String[]{"id"})
```

### 왜 "id"를 적나?

DB에 정의된 PK 컬럼명이 `"id"`이기 때문에
해당 컬럼의 generate key를 반환해달라는 의미이다.

---

## (2) 바인딩 파라미터 적용

```java
pstmt.setString(1, item.getItemName());
pstmt.setInt(2, item.getPrice());
pstmt.setInt(3, item.getQuantity());
```

SQL의 `?` 순서대로 값이 들어간다.

---

## (3) PreparedStatement 반환

JdbcTemplate은 이 PreparedStatement를 실행하고
INSERT 결과로 생성된 키를 KeyHolder에 담아준다.

---

# 4. 생성된 key 꺼내기

```java
long key = keyHolder.getKey().longValue();
```

DB가 INSERT 시 자동으로 생성한 id 값을 가져온다.

예를 들어 DB id가 auto increment라면:

```
1 → 2 → 3 → ...
```

이런 식으로 증가된 id 중 하나가 여기 들어온다.

---

# 5. item 객체에 id 설정

```java
item.setId(key);
```

INSERT 후 실제 DB에 저장된 id 값을 item 객체에도 설정한다.
즉, save() 반환 시점에는 아래 값들이 모두 알 수 있게 된다.

* item.name
* item.price
* item.quantity
* **item.id (DB에서 생성된 값)**

---

# 6. 최종 반환

```java
return item;
```

DB 저장이 끝나고 id까지 채워진 완성된 Item 객체를 반환한다.

---

# 동작 요약

1. INSERT SQL 준비
2. PreparedStatement에 파라미터 바인딩
3. DB 실행
4. DB가 자동 생성한 id 값을 KeyHolder에 저장
5. keyHolder에서 id 받아서 item.setId(...)
6. 저장 완료된 item 객체 반환

---

# 실제 예시

item 객체가 다음 값이라고 해보자:

```
itemName = "Pen"
price = 500
quantity = 3
```

INSERT 실행 후:

DB 저장된 row:

| id | item_name | price | quantity |
| -- | --------- | ----- | -------- |
| 17 | Pen       | 500   | 3        |

KeyHolder.getKey() → `17`

return된 item 객체:

```
Item(id=17, name="Pen", price=500, quantity=3)
```

---

# 부가적으로 필요하면 설명해줄 수 있는 내용

* KeyHolder는 왜 PreparedStatementCreator 방식에서만 사용되는지
* JDBC에서 generated key를 반환받는 표준 방식
* JPA의 persist와 비교하면 어떤 차이가 있는지
* MySQL/H2에서 auto increment 동작 방식

원하면 이어서 설명해줄게.

아래에서 질문한 두 가지를 정확히 정리해 설명해줄게.

---

# 1. KeyHolder는 왜 *PreparedStatementCreator 방식*에서만 사용되는가?

JdbcTemplate에는 크게 두 가지 update 방식이 있다.

## A) 단순 update

```java
jt.update(sql, args...);
```

이 방식은:

* 단순히 SQL 실행만 한다
* PreparedStatement를 내부에서 자동 생성한다
* 당신이 생성 방식에 개입할 수 없다
* 따라서 **어떤 컬럼의 generated key를 받을지 스스로 지정할 수 없다**

즉, 이런 단순 update 방식은 "키를 반환하는 PreparedStatement"를 만들 수 없기 때문에 generated key를 받을 수 없다.

---

## B) PreparedStatementCreator를 사용하는 update

```java
jt.update(con -> {
    return con.prepareStatement(sql, new String[]{"id"});
}, keyHolder);
```

이 방식에서는:

* PreparedStatement를 개발자가 직접 생성한다
* 생성 시점에 `prepareStatement(sql, columnNames)` 를 지정할 수 있다
  → "insert 시 이 컬럼의 자동 생성 키를 반환해라"
* JdbcTemplate이 바로 이 PreparedStatement를 사용해서 실행한다
* 그리고 결과 키를 KeyHolder에 담아준다

즉, KeyHolder를 쓰려면 **반드시 직접 만든 PreparedStatement에 ‘키를 반환해라’라고 명령해야 한다.**

이 구조 때문에 반드시 PreparedStatementCreator 방식이어야 한다.

---

## 핵심 정리

* KeyHolder는 “어떤 컬럼의 키를 리턴받을지 설정된 PreparedStatement”가 필요하다.
* 단순 update()는 이 설정을 할 수 없기 때문에 KeyHolder 사용 불가능.
* PreparedStatementCreator(update(con -> { ... }))에서는 직접 `prepareStatement(sql, columnNames)`를 호출할 수 있기 때문에 KeyHolder 사용 가능.

---

# 2. JDBC에서 generated key를 반환받는 표준 방식 (핵심 규약)

JDBC 표준에서 auto-generated key를 받는 절차는 다음 두 가지 중 하나를 사용한다.

---

## 방식 1) 컬럼명을 지정하는 방식

```java
PreparedStatement pstmt =
    con.prepareStatement(sql, new String[]{"id"}); // PK 컬럼명 지정
```

DB가 생성한 "id" 컬럼 값을 generated key로 반환해 준다.
JdbcTemplate 예제에서 사용한 방식이 바로 이것이다.

---

## 방식 2) `Statement.RETURN_GENERATED_KEYS` 사용 (JDBC 표준)

```java
PreparedStatement pstmt =
    con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
```

이 방식은 DB가 기본적으로 자동 생성하는 모든 컬럼의 키를 반환하게 한다.
MySQL, H2, PostgreSQL 등 대부분 지원한다.

---

## 실행 후 키 읽기

두 방식 모두 INSERT 실행 후 다음을 호출하면 키를 얻을 수 있다.

```java
ResultSet rs = pstmt.getGeneratedKeys();
if (rs.next()) {
    long id = rs.getLong(1);
}
```

JdbcTemplate의 KeyHolder가 내부적으로 이 과정을 대신 해주는 것이다.

---

# JDBC 키 반환 절차 전체 흐름

표준 JDBC 동작은 다음 순서로 정해져 있다.

1. PreparedStatement 생성 시 “키를 반환하겠다” 옵션을 설정한다
2. INSERT 실행
3. JDBC 드라이버가 자동 생성 키를 ResultSet 형태로 제공한다
4. `getGeneratedKeys()` 로 꺼내서 Java로 가져온다
5. 개발자 또는 스프링이 그 값을 객체에 채워 넣는다

Spring의 `GeneratedKeyHolder`는 **4단계 작업을 자동화**한 것이다.

---

# 3. Spring에서 표준 JDBC 방식과 어떻게 연결되는지

Spring JdbcTemplate은 다음 구조로 동작한다.

PreparedStatementCreator
→ `prepareStatement(sql, new String[]{"id"})`
→ DB 실행
→ DB가 키 생성
→ JdbcTemplate 내부에서 `getGeneratedKeys()` 호출
→ KeyHolder에 저장
→ 개발자가 `keyHolder.getKey()`로 꺼냄

즉, Spring은 **JDBC 표준 방식 위에 편의 기능을 얹은 것**뿐이다.

---

# 최종 요약

### KeyHolder를 PreparedStatementCreator 방식에서만 사용할 수 있는 이유

1. 단순 update()는 PreparedStatement 생성 과정에 관여할 수 없음
2. generated key를 받기 위해서는 반드시 `prepareStatement(sql, columnNames)` 같은 설정이 필요
3. 그 설정은 PreparedStatementCreator(Psc) 방식에서만 가능
4. 그래서 KeyHolder는 반드시 Psc 방식과 함께 사용해야 한다

### JDBC에서 generated key를 반환하는 표준 방식

1. `prepareStatement(sql, new String[]{"id"})`
2. `prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)`
   두 방식 중 하나를 사용하고
   `getGeneratedKeys()`로 읽어오는 것이 표준.

---

원하면 다음도 추가로 설명해줄 수 있어요:

* generated key 반환이 DB별로 어떻게 다르게 구현되는지
* auto increment vs sequence 기반 DB는 어떻게 처리되는지
* JPA에서 @GeneratedValue가 내부적으로 어떤 SQL을 실행하는지
