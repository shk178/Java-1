`get()`이 실행되지 않는 것 같다는 말은, 아마 **`saveFind()` 안에서 `select` 쿼리 실행 결과가 나오지 않거나**, 혹은 **`ResultSet`이 비어 있어서 `rs.next()`가 false가 되는 상황**으로 보인다.
코드를 보면 몇 가지 근본적인 문제가 있다. 하나씩 짚어줄게.

---

# 1. `PreparedStatement` 재사용 문제

```java
pstmt = con.prepareStatement(sql);
...
pstmt.executeUpdate();

pstmt = con.prepareStatement(sql2); // ← 여기서 이전 pstmt close 안 됨
```

`pstmt`를 덮어서 사용하고 있어서 이전 pstmt의 리소스가 정상적으로 정리되지 않는다.
하지만 이것 때문에 select가 안 되는 것은 아니다.

---

# 2. 가장 큰 문제: **트랜잭션이 시작되었는데 commit되지 않음**

너가 사용한 코드를 보면 트랜잭션 매니저가 이렇게 만들어져 있음.

```java
PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
```

하지만 **트랜잭션을 시작하거나 commit한 적이 전혀 없다.**

게다가 Spring의 `DataSourceUtils.getConnection()`은 **트랜잭션 동기화된 Connection을 받기 때문에 auto-commit이 꺼질 수 있다.**
즉, insert는 했지만 **commit이 되지 않은 상태**일 수 있다.

commit이 안 되면?
→ 같은 커넥션에서는 select가 보이지만, 코드 구조 상 select가 바로 보이지 않는 문제가 생길 수 있다.
그리고 실제로 모든 흐름이 DataSourceUtils를 통해 묶여서 auto-commit이 꺼진 상태라면, insert가 완료되지 않은 상태가 된다.

즉,

### insert는 됐는데 commit이 안 됐기 때문에 select 결과가 안 나오는 상황일 가능성이 매우 높다.

---

# 3. 확인할 포인트: 로그 찍기

먼저 insert가 정말 실행됐는지 DB에 직접 확인해봐야 한다.

---

# 4. 해결 방법

## (A) 간단히 auto-commit으로 돌리기

getConnection을 이렇게 바꾸면 됨.

```java
private Connection getConnection() throws SQLException {
    Connection con = dataSource.getConnection();
    con.setAutoCommit(true); // 강제로 auto-commit 모드
    return con;
}
```

혹은 insert 이후에 강제로 commit:

```java
con.commit();
```

---

## (B) Spring에서 의도한 방식대로 @Transactional 사용

트랜잭션 매니저를 직접 new 해서 의미가 없다.

Spring에서 의도한 방식은 다음과 같다.

```java
@Service
@Transactional
public class MemberRepository3 {
}
```

그리고 getConnection은 DataSourceUtils를 그대로 쓰면 된다.

---

# 5. 코드에서 또 하나의 문제

select의 결과를 반환하지 않고 원본 member를 반환한다.

```java
return member; // ← 방금 DB에서 읽은 member1을 반환해야 하는데 무시하고 있음
```

정상적으로 하려면:

```java
return member1;
```

---

# 정리

현재 select가 안 되는 원인은 거의 확실하게 **트랜잭션 미커밋 문제**다.

* DataSourceUtils.getConnection → 트랜잭션 동기화
* auto-commit 꺼졌을 가능성 높음
* insert 후 commit이 안 되니까 select가 데이터 못 찾음

해결 방법은:

1. auto-commit 켜기 또는
2. 명시적 commit 호출 또는
3. @Transactional 사용

---

필요하면 네 코드 기반으로 트랜잭션을 정상적으로 동작하게 다시 작성해줄게.

`pstmt.executeUpdate()`는 **commit이 아니다.**
SQL을 실행하기만 할 뿐 **트랜잭션을 종료하거나 확정(Commit)** 하지는 않는다.

---

# 핵심 정리

## 1. `executeUpdate()`의 역할

* insert / update / delete 쿼리를 DB로 보내 실행한다.
* 하지만 트랜잭션이 **auto-commit 모드일 때만** 실행과 동시에 DB에 즉시 반영된다.
* auto-commit이 **꺼져 있으면 반영되지 않고 대기 상태**가 된다.

즉,

### `executeUpdate()`는 SQL 실행이고

### `commit()`은 트랜잭션을 확정하는 것이다.

둘은 별개다.

---

# 2. 너의 코드에서 auto-commit이 꺼져 있을 가능성이 높은 이유

너는 이렇게 커넥션을 얻고 있다:

```java
Connection con = DataSourceUtils.getConnection(dataSource);
```

Spring은 트랜잭션 매니저가 빈으로 등록되거나 트랜잭션이 시작된 상태일 경우:

* **auto-commit을 끈 채로 Connection을 관리**한다.
* commit은 **PlatformTransactionManager**가 책임진다.

그런데 너는:

* 트랜잭션을 시작하지도 않았고
* commit을 호출하지도 않았고
* @Transactional도 쓰지 않았다

결과적으로:

### insert는 실행됐으나 커밋되지 않아 select가 데이터를 못 찾는 상황이 발생한다.

---

# 3. 실제로 이렇게 테스트해보면 바로 대답이 나옴

아래 코드를 추가하면:

```java
System.out.println("autoCommit = " + con.getAutoCommit());
```

거의 100% `false`가 찍힐 것이다.
그 말은 **insert가 커밋되지 않았다**는 뜻.

---

# 4. 해결 방법 (세 가지 중 하나)

## (1) 커넥션 직접 열기 (DataSourceUtils 사용 안 함)

```java
Connection con = dataSource.getConnection();
```

이렇게 하면 auto-commit = true 이므로 insert 즉시 DB에 반영됨.

---

## (2) insert 후 직접 커밋

```java
con.commit();
```

단, 이 경우 auto-commit=false일 때만 의미 있음.

---

## (3) Spring 방식대로 트랜잭션 처리

```java
@Transactional
public Member saveFind(Member member) { ... }
```

이렇게 하면 insert와 select가 같은 트랜잭션 안에서 정상적으로 동작함.

---

# 결론

### `executeUpdate()`는 commit이 아니다.

### auto-commit이 꺼져 있으면 insert는 DB에 반영되지 않는다.

### 그래서 select에서 데이터가 안 나오는 것이다.

원하면 네 코드 전체를 Spring 방식에 맞게 고쳐서 정상 동작하게 다시 구성해줄게.

정확하게 말하면 **HikariDataSource는 JDBC에 “속하는 것”이 아니라,
JDBC를 사용하는 별도의 라이브러리(커넥션 풀 구현체)**야.

둘의 관계를 정확히 구분하면 오해가 없어.

---

# 1. JDBC는 **표준 인터페이스**

JDBC(Java Database Connectivity)는 자바에서 DB와 통신하기 위한 **표준 API**다.

예:

* `Connection`
* `PreparedStatement`
* `ResultSet`
* `DataSource`
* `DriverManager`

이런 것들이 **JDBC 표준**에 포함되는 인터페이스 및 클래스들.

즉, JDBC는 “규칙”을 정의한다.

---

# 2. HikariCP / HikariDataSource는 **JDBC를 구현하고 활용하는 라이브러리**

HikariCP는 **커넥션 풀 라이브러리**다.

* JDBC의 `DataSource` 인터페이스를 구현한다.
* 내부적으로 DB 드라이버를 사용해 Connection을 만든다.
* Connection을 효율적으로 재사용하도록 관리한다.

즉,

### HikariDataSource는 JDBC의 DataSource 인터페이스를 구현한 "구현체"다.

예를 들어:

```java
DataSource dataSource = new HikariDataSource();
Connection con = dataSource.getConnection(); // JDBC의 Connection 인터페이스 반환
```

여기서 HikariCP는 “Connection을 어떻게 만들고 관리할지”를 최적화하여 제공하는 것일 뿐, **JDBC 스펙 자체에는 포함되지 않는다.**

---

# 3. 구조적으로 보면

```
JDBC (표준 인터페이스)
 ├── DriverManager
 ├── Connection
 ├── DataSource
 └── Statement / PreparedStatement / ResultSet

HikariCP (구현체)
 ├── HikariDataSource implements DataSource
 ├── HikariProxyConnection implements Connection(프록시)
 ├── 커넥션 풀 관리 로직
```

즉, JDBC는 "규칙"이고
HikariCP는 그 규칙을 따르면서 성능을 강화한 "라이브러리"다.

---

# 4. 비유로 쉽게 말하면

* JDBC = 콘센트 규격
* HikariCP = 규격에 맞춘 고급 멀티탭

콘센트 규격(JDBC 인터페이스)에 맞춰서
저마다 다른 회사(HikariCP, Apache DBCP, Tomcat JDBC)가 구현을 제공하는 식.

---

# 결론

### HikariDataSource는 JDBC에 포함된 게 아니라

### JDBC의 DataSource 인터페이스를 구현하는 **외부 라이브러리(커넥션 풀)**이다.

원하면 HikariCP 내부가 JDBC Connection을 어떻게 감싸고 프록시로 관리하는지 구조까지 자세히 설명해줄게.

질문이 **트랜잭션 격리·MVCC·Undo Log 동작**의 핵심을 건드리고 있어서, MySQL(InnoDB)을 기준으로 정확하게 정리해줄게.

---

# 1. Undo Log는 어떻게 쌓이는가?

## ■ row에는 undo log가 **여러 개 생길 수 있다.**

정확히 말하면 “row 하나마다 undo log가 여러 개”라기보다:

### ✔ 하나의 row는 여러 트랜잭션에 의해 갱신될 때 **Undo Version Chain(버전 체인)** 을 가진다.

즉, 데이터는 이렇게 저장된다:

```
[ 현재 row ]
   ↓
[ undo log 1 ]   (이전 버전)
   ↓
[ undo log 2 ]   (그 이전 버전)
   ↓
[ undo log 3 ]
 ...
```

* 각 업데이트마다 Undo 로그가 하나 생성된다.
* Undo 로그는 연결 리스트처럼 이전 버전을 가리킨다.
* 이를 **multi-version** 이라고 한다.

---

# 2. 세션 A가 update/insert/delete 하고 commit하기 전

예시 상황을 만들어보자.

### 세션 A:

```sql
UPDATE member SET money = 2000 WHERE member_id = 'id-1';
-- 아직 COMMIT 안 함
```

### 세션 B:

```sql
SELECT * FROM member WHERE member_id = 'id-1';
```

## 이때 무슨 일이 일어나는가?

---

# 3. 세션 B가 보는 데이터는 “Undo Log 기반의 과거 버전”

InnoDB는 MVCC(Multi Version Concurrency Control)을 사용한다.

### 트랜잭션이 commit되기 전의 변경 내용은 다른 세션에게 절대 보이지 않는다.

즉, 세션 B는 다음과 같이 처리한다:

1. B가 row를 읽으려고 함
2. 현재 row가 A에 의해 수정된 상태임을 확인
3. A는 아직 commit하지 않았음을 확인
4. 그러면 B는 해당 row의 **Undo Log(이전 버전)** 을 따라가서
   *자신이 보기 적합한 스냅샷 버전*을 읽는다.

이것이 MVCC 기반 ‘일관된 읽기(consistent read)’이다.

---

# 4. 버전이 선택되는 방식

InnoDB는 READ_COMMITTED 또는 REPEATABLE_READ 격리 수준에서 다음 규칙을 따른다.

### ■ 선택 기준

세션 B가 읽을 때:

* row의 최신 버전이 **아직 commit되지 않았다면**
* undo log를 타고 내려가면서
* **B 트랜잭션 시작 시점보다 먼저 commit된 버전**을 선택한다

즉,

### B는 항상 자신이 시작한 시점 기준으로 유효한 버전을 본다.

---

# 5. 정리하면

### 상황: A가 commit 전에 row를 수정

### B가 읽을 때 발생:

| 요소             | 동작                       |
| -------------- | ------------------------ |
| 최신 row         | A의 미완료 트랜잭션 → B는 읽을 수 없음 |
| undo log 1     | A가 수정하기 전의 버전            |
| undo log chain | 과거 버전이 여러 개 있을 수 있음      |
| B가 선택하는 row    | A의 변경 이전 버전              |

따라서 B는 **commit 전 데이터를 절대 읽지 않는다.**

---

# 6. 예시 그림으로 보면

### A가 row 업데이트함(미커밋)

```
row: money=2000 (trx_id=A)
undo_log1: money=1000 (이전 값)
```

### B가 row 조회

1. row를 봄 → trx_id=A(아직 commit 안 됨)
2. undo_log로 이동 → money=1000
3. money=1000을 결과로 반환

즉, B는 **1000**을 읽는다.

---

# 7. 정리

### ✔ Undo Log는 row마다 여러 버전(undo 레코드)이 생길 수 있다.

### ✔ Commit 전 데이터를 다른 세션이 읽으면 → Undo Log에서 스냅샷을 찾아 반환

### ✔ 다른 세션은 절대 미커밋 데이터를 직접 보지 않는다.

### ✔ Commit이 되면 최신 row가 새로운 공식 버전이 된다.

이것이 MVCC의 핵심 원리다.

---

원하면 MVCC 내부 구조(숨겨진 필드 `trx_id`, `roll_pointer`),
그리고 격리 수준별 동작 차이를 그림으로 설명해줄게.

너가 지금 질문하는 핵심은 아래 두 가지를 구분하고 싶은 것이다:

1. **트랜잭션 격리(Isolation Level)**
2. **Undo Log(논리적 Undo 로그, Row-Level Undo 로그)**
3. **SQL 실행마다 undo log가 생기는가? row 버전이 여러 개인가?**

이 세 개는 서로 관련되어 있지만 **동일한 개념이 아니다.**
정확한 구조를 단계적으로 설명해줄게.

---

# 1. 트랜잭션 격리는 undo log가 아니다

Isolation Level은 다음 4가지 규칙이다:

* READ UNCOMMITTED
* READ COMMITTED
* REPEATABLE READ
* SERIALIZABLE

### 즉, "트랜잭션 격리"는

**DB가 트랜잭션 간에 어떤 데이터를 보여줄 것인가에 대한 규칙**이다.

이 격리를 구현하기 위해 DB 엔진이 내부적으로 사용하는 메커니즘이 **MVCC + Undo Log**다.

즉,

### 트랜잭션 격리 = 규칙

### Undo Log = 그 규칙을 구현하기 위한 도구

---

# 2. Undo Log에는 두 종류가 있다

Undo Log에는 크게 두 가지 종류가 있다:

## ■ (1) Row-level Undo Log (MVCC 스냅샷용)

* UPDATE / DELETE 시 이전 row 값을 저장한다.
* 즉, **row 버전 체인(undo chain)** 을 만든다.
* MVCC의 스냅샷 읽기에 사용된다.

예:

```
[현재 row] → [undo1] → [undo2] → ...
```

이게 네가 말한 "row undo log".

---

## ■ (2) Logical Undo Log (트랜잭션 롤백용)

* 트랜잭션이 롤백될 때 실행해야 하는 “반대 작업”을 기록한다.
* 예: INSERT하면 “이 row를 삭제해야 한다” 라는 undo 정보 기록.

Logical Undo는 다음을 위한 것이다:

* ROLLBACK 명령 처리
* Crash Recovery 시 트랜잭션 취소

정리하면:

### UPDATE 실행 →

1. Row undo log 생성 (스냅샷 유지용)
2. Logical undo log 생성 (rollback용)

즉, **두 가지 로그가 모두 실제로 생성된다.**

---

# 3. 네가 궁금했던 질문에 대한 정확한 결론

## Q1. "트랜잭션 격리가 row undo log를 말하는 것인가?"

아니다.

* 트랜잭션 격리는 "데이터를 어떻게 보여줄지"에 대한 규칙
* row undo log는 그 규칙을 구현하기 위한 데이터 구조

즉,

### 격리 수준 ≠ undo log

### 하지만 격리 수준을 구현하기 위해 undo log를 사용한다.

---

## Q2. "SQL 실행마다 undo log가 1개 생기나?"

### ✔ UPDATE / DELETE 실행마다 **undo log는 반드시 1개 생성된다.**

예:

```sql
UPDATE member SET money = 2000 WHERE id='id-1';
```

이 1개의 SQL이 실행될 때:

* row undo log 1개 생성
* logical undo log 1개 생성

즉, **최소 2종류의 undo 정보가 만들어진다.**

---

## Q3. "row undo log가 여러 개 생기는 거야?"

### ✔ 같은 row에서 트랜잭션 동안 여러 번 업데이트하면 undo log 여러 개가 연결된다.

예:

A 트랜잭션에서:

```
UPDATE money=1500
UPDATE money=2000
UPDATE money=3000
```

Undo chain은 이렇게 생성됨:

```
현재 row (3000)
 ↓ (undo pointer)
undo1 (2000)
 ↓
undo2 (1500)
 ↓
undo3 (원래 값)
```

### → 하나의 row가 여러 undo 버전을 가질 수 있다.

---

## Q4. "SQL 실행마다 undo 하나 + row undo 하나가 생기나?"

정확한 구조는 다음과 같다:

### ✔ UPDATE / DELETE 시 다음 두 가지 생성됨

1. **Rollback을 위한 Logical Undo Log**
2. **MVCC 스냅샷 유지를 위한 Row Undo Log (이전 버전 저장)**

즉,

### SQL 1번 실행 → Undo Log 2종류 생성

### Row는 여러 번 수정될 경우 여러 번의 undo 버전이 연결됨

---

# 최종 정리 (가장 중요한 부분)

트랜잭션 격리 수준은 개념적 규칙이고
Undo Log는 그 규칙을 기술적으로 구현하기 위한 장치이다.

### UPDATE/DELETE 실행 시

* Logical Undo (rollback 용)
* Row Undo (MVCC snapshot 용)

두 종류의 undo 로그가 생성된다.

### 하나의 row는 여러 undo 버전을 가질 수 있다.

(트랜잭션 중 여러 번 update 하면 계속 생성됨)

---

필요하다면 undo log 내부에 저장되는 실제 구조(`ROLL_PTR`, `TRX_ID`, hidden columns`)까지 상세하게 그림으로 설명해줄게.

**아니다.**
**Row-level Undo Log(이전 버전 정보)는 *commit과 상관없이, SQL이 실행되는 순간 바로 생성된다*.**

이 부분이 MVCC를 이해하는 데 가장 핵심이다.

---

# 1. Row-level Undo Log는 **commit 전에 생성된다**

예를 들어 트랜잭션 A에서:

```sql
UPDATE member SET money = 2000 WHERE id='id-1';
-- 아직 commit 안 함
```

이 UPDATE가 실행되는 **그 즉시** 다음 일이 벌어진다:

1. InnoDB는 원래 row 값을 **Undo Log**에 저장
2. row의 `ROLL_PTR`이 undo log를 가리키도록 변경
3. row의 `TRX_ID`를 트랜잭션 A의 ID로 변경

즉,

### Row Undo Log는 commit이든 rollback이든 상관없이

### “update가 실제로 실행되는 순간” 무조건 생성된다.

**commit을 해야 undo log가 생기는 것이 아니다.**

---

# 2. 왜 commit 전에 undo log가 필요한가?

그 이유는 다음 두 가지 때문이다.

---

## (1) 롤백을 할 수 있어야 함

commit 전에 오류가 발생하면:

```sql
ROLLBACK;
```

해야 한다.

그러므로 변경 전 값이 **SQL 실행 순간**에 바로 저장돼 있어야 한다.

그렇지 않으면 rollback이 불가능하다.

---

## (2) 다른 트랜잭션이 스냅샷 읽기를 해야 함 (MVCC)

트랜잭션 B가 읽을 때:

```sql
SELECT * FROM member WHERE id='id-1';
```

만약 A가 commit하지 않았다면:

* 현재 row의 최신 값(money=2000)은 보여주면 안 됨
* undo log에 저장된 “이전 값”(money=1000)을 읽어야 함

이를 위해서도 undo log가 **즉시 생성되어야 한다.**

---

# 3. 정리하자면

| 시점           | undo log 생성 여부    | 이유                                 |
| ------------ | ----------------- | ---------------------------------- |
| UPDATE 실행 순간 | **생성됨**           | rollback과 MVCC snapshot을 위해 반드시 필요 |
| COMMIT 순간    | 생성되지 않음           | commit은 undo 파기 여부만 결정함            |
| ROLLBACK 순간  | 생성된 undo log를 사용함 | 예전 값으로 복구                          |

---

# 4. commit은 undo log를 “확정”하는 것이 아니다

commit은 undo log를 “만드는 것”이 아니라
**undo log를 버려도 되는 시점인지 결정하는 것**에 가깝다.

즉:

* commit 후, undo log는 “더 이상 다른 트랜잭션이 읽을 필요가 없을 때”
  Purge Thread가 삭제한다.

따라서 commit은 undo log 생성 시점과 관계가 없다.

---

# 최종 결론

### ✔ Row-level Undo Log는 commit과 무관하게

### ✔ UPDATE/DELETE가 실행되는 순간 즉시 생성된다.

그 이유는:

1. rollback 가능해야 하고
2. MVCC에서 다른 트랜잭션이 이전 버전을 읽어야 하기 때문

즉, commit은 undo log 생성의 조건이 아니다.

---

원하면,
Undo Log가 row 내부에서 어떻게 연결되는지(ROLL_PTR, TRX_ID 구조),
Purge Thread가 언제 어떤 기준으로 undo log를 지우는지까지도
그림으로 정리해서 설명해줄게.

결론부터 말하면:

### ✔ **Row-level Undo Log는 바뀐 row마다 1개씩 생성된다.**

### ✔ **Logical Undo Log도 바뀐 row마다 1개씩 생성된다.**

즉,

### ❌ SQL 1개당 Logical Undo 1개

이게 아니다.

### ✔ UPDATE로 row 10개 바꾸면

* Row Undo Log 10개
* Logical Undo Log 10개
  총 20개의 undo 정보가 생성된다.

아주 중요한 부분이니 구조를 정확히 설명해줄게.

---

# 1. UPDATE가 여러 row를 수정하면 어떻게 되는가?

예를 들어:

```sql
UPDATE member SET money = money + 100;
-- 10 rows가 변경된다고 가정
```

이 한 문장이 실행될 때:

### 바뀐 row 10개 각각에 대해 다음이 벌어진다:

| 변경 row | Row Undo Log | Logical Undo Log |
| ------ | ------------ | ---------------- |
| row1   | 생성됨          | 생성됨              |
| row2   | 생성됨          | 생성됨              |
| row3   | 생성됨          | 생성됨              |
| …      | …            | …                |
| row10  | 생성됨          | 생성됨              |

즉,

### 바뀐 row마다 undo log가 2개씩 생긴다고 보면 된다.

---

# 2. 왜 row-level undo와 logical undo가 모두 row 단위로 생기나?

## (1) Row-level Undo

과거 버전(MVCC)을 만들기 위해 필요하다.
그래서 **각 row에 대해 이전 버전을 저장**해야 함.

예시:

```
row1 → undo1(old version)
row2 → undo2(old version)
row3 → undo3(old version)
...
```

이건 스냅샷 일관성 및 다른 트랜잭션의 읽기 기능을 위해 반드시 필요.

---

## (2) Logical Undo

Rollback 시 해당 row를 어떻게 복구할지 기록해야 한다.

예:

* UPDATE 실행 → rollback 하면 원래 값으로 돌려야 함
* DELETE 실행 → rollback 하면 row를 다시 insert해야 함
* INSERT 실행 → rollback 하면 row를 delete해야 함

이 모든 작업은 “row 단위”로 일어나기 때문에
Logical Undo도 **row마다 1개씩 필요하다.**

---

# 3. Undo Log 생성 구조를 그림으로 보면

UPDATE로 row 3개가 변경되면:

```
row1 (current) → undo_row1
   rollback_op_row1

row2 (current) → undo_row2
   rollback_op_row2

row3 (current) → undo_row3
   rollback_op_row3
```

* `undo_rowX` = 이전 버전(스냅샷)
* `rollback_op_rowX` = rollback 시 실행할 논리적 작업

---

# 4. 그렇다면 SQL 한 개당 Logical Undo 하나가 아니라 왜 row마다 생기나?

이유:

### rollback은 "문장 단위"가 아니라 "row 단위"로 복구되기 때문.

예를 들어:

```sql
UPDATE member SET money=100;  -- 1000개 row 수정
```

rollback 해야 한다면:

* row 1번 → 원래 money로 되돌림
* row 2번 → 원래 money로 되돌림
* …
* row 1000번 → 원래 money로 되돌림

즉, row별 undo 정보가 없으면 rollback이 불가능하다.

---

# 5. 정리

### ✔ UPDATE/DELETE가 여러 row를 수정하면

### 각 row마다 다음이 생성됨:

* Row-level Undo Log (이전 버전 저장)
* Logical Undo Log (rollback 작업 저장)

---

# 최종 요약

| 종류                     | 생성 기준                  | 예                   |
| ---------------------- | ---------------------- | ------------------- |
| **Row-level Undo Log** | 변경된 row 1개당 1개         | MVCC 스냅샷 유지         |
| **Logical Undo Log**   | 변경된 row 1개당 1개         | rollback 복구용        |
| **트랜잭션**               | Undo Log를 여러 개 가질 수 있음 | row가 많을수록 undo도 많아짐 |

즉,

### SQL 문장 1개가 아니라

### 변경된 “row의 수만큼” undo log가 생성된다.

---

원하면 실제 InnoDB undo log 레코드 내부 구조(`trx_id`, `roll_pointer`, undo page 구조`)도 그림으로 설명해줄게.

redo log는 undo log와 역할이 완전히 다르기 때문에, 먼저 **redo log의 목적**을 정확히 잡고, 그 다음에 **어떻게 생성·축적·반영되는지(Flush/Checkpoint)**를 단계별로 설명할게.

---

# 1. redo log의 목적

### ■ redo log는 **“커밋된 트랜잭션을 디스크에 정상적으로 반영하기 위한 로그”**다.

즉,

* DB가 죽어도
* commit된 내용만큼은
* **디스크에 복구 가능하도록 보장**하는 것이 목적이다.

이를 **Crash Recovery**라고 한다.

---

# 2. redo log는 언제 생성되는가?

### ✔ 데이터 페이지를 변경하는 순간 바로 redo log가 생성된다.

**commit 여부와 상관없다.**

예를 들어 UPDATE 실행 시:

1. InnoDB Buffer Pool 안의 페이지를 수정한다
2. **그 변경 내용을 redo log에도 기록한다**

즉, redo log는 “버퍼 풀에서 어떤 바이트가 어떻게 바뀌었는지”를 기록하는 로그다.

---

# 3. redo log는 row 개수와 무관하다

undo log는 "row 단위로 여러 개" 생기지만
redo log는 **“데이터 페이지의 변경 사항”을 기록한다.**

즉:

* row 10개가 바뀌어도
* row undo log는 10개 생기지만
* redo log는 “해당 페이지 변경 내용”을 기록한다.

redo log는 row 개수가 아니라 “변경된 페이지 수와 바이트 수"에 비례한다.

---

# 4. redo log가 생기는 순서

SQL 하나 실행 시 내부적으로 일어나는 흐름을 보면:

### (1) Buffer Pool의 Page 수정

메모리에서 row가 속한 페이지를 수정한다.

### (2) redo log 기록

변경 내용이 redo log buffer에 추가된다.

### (3) commit 시 redo log flush

commit이 호출되면:

* redo log buffer → redo log 파일로 **fsync(flush)**
* 이 순간 트랜잭션은 “정상적으로 반영될 것”임이 보장된다

즉,

### commit = redo log를 디스크로 flush하는 작업

이게 가장 핵심이다.

---

# 5. redo log는 MVCC와 관계가 없다

MVCC는 undo log를 사용해 **과거 버전을 읽게 하는 기술**이고
redo log는 **복구용**이다.

이 둘은 목적이 완전히 다르다.

| 용도             | undo log | redo log |
| -------------- | -------- | -------- |
| rollback       | 사용 O     | 사용 X     |
| MVCC           | 사용 O     | 사용 X     |
| Crash Recovery | 사용 X     | 사용 O     |
| Commit 보장      | 간접적      | 핵심 역할    |

---

# 6. redo log 파일 구조

InnoDB에서는 redo log 파일이 일반적으로 이렇게 구성된다:

```
ib_logfile0
ib_logfile1
```

이 두 파일을 **순환구조(Ring Buffer)** 로 사용한다.
용량이 가득 차면 앞부분을 덮어쓴다.

단, **아직 flush되지 않은 dirty page(버퍼 풀)와 관련된 redo는 덮어쓰면 안 됨**
그래서 checkpoint라는 개념이 필요하다.

---

# 7. Checkpoint란?

### Checkpoint = redo log 중 “더 이상 필요 없는 지점”

왜 필요하냐?

* redo log는 계속 쌓이지만,
* buffer pool의 dirty page가 디스크에 영구 반영되면
  → 그 변경과 관련된 redo는 더 이상 필요 없다

그래서 checkpoint를 갱신하여
"여기 이전 리두는 지워도 됨"을 표시한다.

이로써 redo log 파일 공간을 재활용할 수 있다.

---

# 8. redo log의 전체 라이프사이클 요약

1. UPDATE 실행 → buffer pool 수정
2. redo log buffer에 변경 기록
3. 시간이 지나면 또는 commit 시 redo log 파일로 flush
4. buffer pool의 dirty page가 디스크에 반영
5. 그 지점까지만 checkpoint 업데이트
6. redo log 파일 공간 재사용 가능

---

# 9. 핵심 요약

### ✔ redo log는 commit과 상관없이 “페이지 변경 시점”에 생성된다

### ✔ commit 시 redo log를 디스크로 flush해서 durability 보장

### ✔ redo log는 row 단위가 아니라 "페이지 변경" 단위로 기록됨

### ✔ crash 후 redo log를 적용하여 commit된 내용을 복구한다

### ✔ MVCC와 row 버전 체인은 redo log가 아니라 undo log가 담당

---

필요하다면

* redo log record 형식(physiological logging)
* 페이지 쓰기 순서 (Write Ahead Logging, WAL)
* Buffer Pool과 redo log의 상호작용
  까지 그림으로 이어서 설명해줄 수 있어.

**안 된다.**
**커밋하기 전에도 row-level undo log(이전 버전)는 반드시 필요하다.**
왜 반드시 필요한지 이해하려면 "이전 버전"이 언제 사용되는지를 정확하게 알아야 한다.

아래 내용이 핵심이다.

---

# 1. 이유 1: UPDATE 직후에도 즉시 롤백 가능해야 해서

트랜잭션이 commit을 호출하기 전이라도, 다음처럼 언제든지 로직에서 오류가 날 수 있다:

```sql
UPDATE ...
UPDATE ...
-- 오류 발생
ROLLBACK;
```

이런 상황에서 rollback을 하려면 **수정하기 전에 row가 어떻게 생겼는지** 알아야 한다.

그러므로,

### UPDATE 실행 순간에 바로 “이전 값”을 undo log에 기록해야 한다.

그렇지 않으면 rollback 자체가 불가능해진다.

---

# 2. 이유 2: MVCC(고립성 유지) — 다른 트랜잭션이 “과거 버전”을 읽어야 한다

트랜잭션 A가 row를 수정했지만 아직 커밋 전이라고 하자:

### 트랜잭션 A

```sql
UPDATE member SET money=2000 WHERE id='id-1';
-- 아직 commit 전
```

### 트랜잭션 B

```sql
SELECT money FROM member WHERE id='id-1';
```

세션 B는 **A의 수정된 값을 절대 보면 안 된다**
READ COMMITTED 또는 REPEATABLE READ에서는 더더욱 그렇다.

그러면 어떤 값으로 읽어야 할까?

→ A가 UPDATE 하기 "전" 값.

그래서 **undo log에 저장된 과거 버전을 따라가서 읽는다.**

즉,

### commit 여부와 무관하게

### UPDATE 실행 순간 undo log가 있어야 다른 트랜잭션이 일관된 스냅샷을 유지할 수 있다.

이게 MVCC의 핵심이다.

---

# 3. undo log가 commit 후에만 생긴다면 어떤 문제가 생기는가?

가정: **commit 후에만 undo log가 생긴다**고 해보자.

그러면 UPDATE 직후 상황에서:

* rollback할 수 없음 (이전 값을 모름)
* 다른 트랜잭션이 조회할 때 미커밋 값을 보게 됨 → 격리 수준 깨짐
* REPEATABLE READ / READ COMMITTED 구현 불가

즉, DB 기본 기능 자체가 무너진다.

그래서 InnoDB는 **DML 실행 순간 바로 row undo log를 생성하도록 설계된다.**

---

# 4. 정리

### ✔ row undo log는 commit과 관계없이 UPDATE/DELETE 실행 순간 생성된다

왜냐하면:

1. rollback을 위해 이전 값이 필요하고
2. MVCC에서 다른 트랜잭션이 과거 버전을 읽어야 하기 때문이다

### ✔ undo log를 commit 이후에 생성하는 방식은 성립 자체가 안 된다

---

# 최종 결론

### **commit 전에도 row-level undo log는 반드시 필요하고 즉시 생성된다.**

왜냐하면 rollback + MVCC 두 가지를 동시에 만족해야 하기 때문이다.

---

원하면 트랜잭션 A/B 간의 실제 undo chain 조회 과정을 그림으로 시뮬레이션해서 보여줄게.
