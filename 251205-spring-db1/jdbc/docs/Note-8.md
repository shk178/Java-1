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
