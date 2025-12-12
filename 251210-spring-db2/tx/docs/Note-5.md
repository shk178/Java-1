1. 외부 트랜잭션 코드 -> 트랜잭션 매니저: 외부 트랜잭션 시작
2. 트랜잭션 매니저: 데이터 소스 -> 커넥션 생성
3. 트랜잭션 매니저: 커넥션.setAutoClose(false)
4. 트랜잭션 매니저 -> 트랜잭션 동기화 매니저: 커넥션 보관
5. 트랜잭션 매니저 -> 외부 트랜잭션 코드: 트랜잭션 상태 반환
6. 로직1 -> 트랜잭션 동기화 매니저: 커넥션 사용
7. 내부 트랜잭션 코드 -> 트랜잭션 매니저: 내부 트랜잭션 시작
8. 트랜잭션 매니저 -> 트랜잭션 동기화 매니저: 기존 트랜잭션 존재 확인
9. 트랜잭션 매니저 -> 내부 트랜잭션 코드: 트랜잭션 상태 반환
10. 로직2 -> 트랜잭션 동기화 매니저: 커넥션 사용

설명은 **Spring Framework의 트랜잭션 관리 구조**(특히 `DataSourceTransactionManager` + `TransactionSynchronizationManager`)를 기준으로, 질문에 등장한 각 요소가 **어떤 클래스/객체**이며 **어떤 식으로 동작하는지**를 단계별로 풀어서 정리한 것이다.

---

# 1. 외부 트랜잭션 코드

**어떤 클래스/객체인가?**

* 주로 애플리케이션 서비스 계층의 메서드
* `@Transactional`이 붙어 Spring AOP/Proxy를 통해 트랜잭션 경계가 만들어지는 코드
* 또는 명시적으로 `PlatformTransactionManager.getTransaction()`을 호출하는 코드

**어떻게 동작하는가?**

* 메서드 실행 직전에 트랜잭션 프록시가 개입해 `PlatformTransactionManager`에게 **트랜잭션 시작 요청**을 보냄

---

# 2. 트랜잭션 매니저

**어떤 클래스인가?**

* 대표적으로 `DataSourceTransactionManager`
* 트랜잭션 시작/커밋/롤백을 책임지는 컴포넌트
* 인터페이스: `PlatformTransactionManager`

**어떻게 동작하는가?**

* `getTransaction()` 호출 시 Datasource에서 커넥션을 얻고 트랜잭션 속성을 반영
* 트랜잭션이 이미 존재하는지 검사
* 트랜잭션 동기화 매니저에 커넥션을 저장

---

# 3. 데이터 소스 → 커넥션 생성

**어떤 클래스인가?**

* `javax.sql.DataSource` 구현체

    * HikariCP: `HikariDataSource`
    * DriverManagerDataSource 등

**동작 방식**

* 트랜잭션 매니저가 `dataSource.getConnection()` 호출
* 연결 풀에서 Connection을 획득

---

# 4. Connection.setAutoCommit(false)

**어떤 객체인가?**

* JDBC `java.sql.Connection`

**동작 방식**

* 스프링은 JDBC 트랜잭션을 제어하기 위해 autoCommit을 false로 설정
* 이후 commit/rollback은 모두 스프링이 직접 호출

---

# 5. 트랜잭션 동기화 매니저

**어떤 클래스인가?**

* `org.springframework.transaction.support.TransactionSynchronizationManager` (static thread-local 기반)

**하는 역할**

* 현재 스레드에서 사용 중인 **Connection**, **transaction status**, **synchronization 상태** 등을 저장
* 트랜잭션 범위에 있는 모든 DAO/JdbcTemplate 등이 이 저장된 리소스를 공유

**동작 방식**

* `bindResource(dataSource, connectionHolder)`
* ThreadLocal에 “이 스레드는 이 Connection을 사용 중”이라는 정보를 기록

---

# 6. 로직1 → 트랜잭션 동기화 매니저 사용

**어떤 코드인가?**

* DAO 계층, Repository 계층 (JdbcTemplate, MyBatis 등)

**동작 방식**

* DAO 내부에서 DB 접근 시

    * `DataSourceUtils.getConnection(dataSource)` 호출
    * 트랜잭션 동기화 매니저(ThreadLocal)에 있는 Connection을 꺼내서 사용
* 따라서 외부 트랜잭션과 같은 Connection을 공유

---

# 7. 내부 트랜잭션 코드

**어떤 코드인가?**

* 외부 트랜잭션 안에서 실행되는 또 다른 `@Transactional` 메서드
* “propagation” 정책에 따라 동작 (기본값: `REQUIRED`)

**동작 방식**

* 내부 메서드를 호출하면 또 트랜잭션 프록시가 동작
* 트랜잭션 매니저에게 다시 트랜잭션 시작 요청을 보냄

---

# 8. 기존 트랜잭션 존재 확인

**뭐가 하는 일인가?**

* `DataSourceTransactionManager.doGetTransaction()`

    * ThreadLocal에 이미 있는 ConnectionHolder가 있는지 확인
* 있다면: 기존 트랜잭션이 존재한다고 판단

**Propagation REQUIRED 동작 방식**

* 새 트랜잭션을 만들지 않고
* **기존 트랜잭션을 그대로 이어서 사용**

---

# 9. 내부 트랜잭션 상태 반환

**내용**

* 내부 트랜잭션에 대해 새로운 TransactionStatus가 만들어지긴 하지만

    * 실제로는 "참여(participating)" 상태
    * 새로운 Connection, 새로운 트랜잭션 경계는 없음
* 내부 트랜잭션에서 commit 또는 rollback을 호출하더라도

    * 실제로는 외부 트랜잭션이 처리

---

# 10. 로직2 → Connection 사용

* 로직2(DAO)도 동일하게 ThreadLocal에 묶여 있는 동일 Connection 사용
* 외부/내부 모두 하나의 커넥션에서 수행
* commit/rollback은 외부 트랜잭션이 최종 책임

---

# 전체 구조 요약 표

| 요소           | 실제 클래스/객체                             | 동작                           |
| ------------ | ------------------------------------- | ---------------------------- |
| 외부 트랜잭션 코드   | 서비스 메서드 (Transactional Proxy)         | 트랜잭션 시작 요청                   |
| 트랜잭션 매니저     | `DataSourceTransactionManager`        | 커넥션 획득, 트랜잭션 제어              |
| 데이터소스        | `HikariDataSource`, `BasicDataSource` | 커넥션 풀에서 Connection 반환        |
| 커넥션          | JDBC `Connection`                     | autoCommit 제어, 실제 SQL 실행     |
| 트랜잭션 동기화 매니저 | `TransactionSynchronizationManager`   | ThreadLocal 기반 리소스 저장        |
| 로직1/로직2      | DAO/JdbcTemplate 등                    | ThreadLocal로부터 Connection 사용 |
| 내부 트랜잭션 코드   | 또 다른 @Transactional 메서드               | 기존 트랜잭션에 참여                  |

---

필요하다면

* 이 흐름을 실제 **클래스 간 호출 순서(스택 트레이스 스타일)**로 그려드릴 수 있고
* 트랜잭션 전파 옵션(Propagation REQUIRES_NEW 등)에 따른 차이도 설명할 수 있다.

어떤 부분을 더 알고 싶나?

아래 설명은 **Spring의 `TransactionStatus` 객체가 ‘새로 만들어진다’는 말의 정확한 의미**를 정리한 것이다. 핵심은 다음과 같다.

* **트랜잭션 매니저는 내부 트랜잭션 요청이 와도 항상 새로운 `TransactionStatus` 객체를 하나 만든다.**
* 하지만 **그 객체가 의미하는 트랜잭션의 실체는 외부 트랜잭션과 동일**할 수 있다(`REQUIRED`일 때).
* 즉 **상태 객체는 새로 생기지만, 실제 JDBC Connection·트랜잭션 경계는 새로 생기지 않는다.**

이것을 외부/내부 상태로 나누어 정확히 설명해보겠다.

---

# 1. 외부/내부 모두 TransactionStatus 객체가 생긴다

```java
TransactionStatus status1 = txManager.getTransaction(...); // 외부
TransactionStatus status2 = txManager.getTransaction(...); // 내부
```

이렇게 **status1, status2 두 개의 객체가 존재하지만**, 둘이 같은 물리적 트랜잭션에 참여할 수 있다.

왜냐하면 스프링은 내부 트랜잭션 요청을 처리할 때 다음을 확인하기 때문이다.

* ThreadLocal(TransactionSynchronizationManager)에 기존 트랜잭션 리소스가 있는가?
* Propagation 정책이 REQUIRED인가?

→ 둘 다 Yes이면 **새로운 트랜잭션을 만들지 않고 기존 트랜잭션에 참여**하게 된다.

그래도 **TransactionStatus 객체는 항상 새로 생성**된다.

---

# 2. 그럼 새로 만들어진 상태(Status)는 무엇이 다른가?

## 외부 트랜잭션 상태 (status1)

* 실제 JDBC Connection을 열고 autoCommit false로 설정
* “트랜잭션을 시작한 주체”
* commit/rollback에 대해 **실행 책임을 실제로 갖고 있음**
* rollback-only 플래그를 끝까지 관리

## 내부 트랜잭션 상태 (status2)

* 실제 신규 트랜잭션은 아님
* 기존 트랜잭션에 “참여 중”이라고 표시
* 다음과 같은 특징을 가진다:

### (1) **newTransaction = false**

* 즉 “진짜 트랜잭션을 시작한 주체가 아니다”.

### (2) **commit()을 호출해도 실제 commit이 일어나지 않음**

* 내부 트랜잭션의 commit은 의미상 완료 표시일 뿐
* 진짜 commit은 외부 트랜잭션이 종료될 때만 가능

### (3) **rollback()을 호출하면 rollback-only 플래그만 설정**

* 내부 트랜잭션이 rollback을 요청하면

    * 실제 JDBC rollback을 즉시 수행하는 것이 아니라
    * 외부 트랜잭션이 나중에 commit 시도할 때
      “rollback-only라서 commit 못함 → 전체 롤백”
    * 이런 방식으로 전체 트랜잭션이 롤백됨

### (4) **리소스를 직접 관리하지 않음**

* Connection을 열거나 닫지 않음
* 트랜잭션 동기화 매니저에 리소스를 등록·해제하지 않음

즉, 내부 트랜잭션 상태는 **논리적 트랜잭션 경계를 표시하는 역할만 수행하는 얇은 객체**이다.

---

# 3. 상태 객체가 두 개 필요한 이유

두 이유가 있다.

## 첫째, 트랜잭션 전파(Propagation) 정책을 유연하게 처리하기 위함

* 내부 트랜잭션이 `REQUIRES_NEW`라면
  → 외부 트랜잭션을 잠시 보류하고 진짜 새로운 트랜잭션을 생성해야 한다
* 내부 트랜잭션이 `REQUIRED`라면
  → 기존 트랜잭션에 참여
* 이 판단을 개별 `TransactionStatus`에 기록해야 한다

즉, **논리적 트랜잭션의 경계를 구조적으로 표현하기 위해서 ‘상태 객체’를 별도로 둔다.**

## 둘째, AOP를 통해 메서드 단위의 트랜잭션 경계를 표현해야 하기 때문

각 @Transactional 메서드는 자체적인 “트랜잭션 시작/종료”를 가진다고 간주된다.
물리적으로 같은 트랜잭션이라 해도 **논리적 경계가 있으므로 상태 객체가 별도로 필요**하다.

---

# 4. 외부/내부 상태의 차이 요약

| 항목                   | 외부 TransactionStatus | 내부 TransactionStatus (REQUIRED) |
| -------------------- | -------------------- | ------------------------------- |
| 객체 생성                | O                    | O                               |
| 실제 트랜잭션 생성           | O                    | X                               |
| newTransaction 플래그   | true                 | false                           |
| Connection 생성·관리     | O                    | X                               |
| commit() 실제 수행       | O                    | X                               |
| rollback() 실제 수행     | O                    | X (rollback-only 플래그 설정만)       |
| rollback-only 플래그 확인 | 최종 commit 전에 확인      | 외부에 true로 표시                    |

→ 실제 JDBC 트랜잭션을 움직이는 주체는 **오직 외부 TransactionStatus**뿐이다.
→ 내부 TransactionStatus는 **논리적 경계 및 rollback-only 전달자 역할**만 한다.

---

# 정리

“새로운 상태가 만들어진다”는 말은

* **스프링이 트랜잭션 상태를 표현하는 객체(TransactionStatus)를 매번 새로 만든다는 의미**
* **실제 트랜잭션(Connection 기반)이 새로 생긴다는 의미가 아니다 (REQUIRED일 경우)**

즉,
**상태 객체는 논리적 트랜잭션을 나타내는 ‘껍데기’이고, 외부 상태만이 실제 물리 트랜잭션을 갖는다.**

---

원한다면

* 실제 소스 코드(`DataSourceTransactionManager.doBegin()`, `TransactionStatus` 구조)로 더 상세히 설명하거나
* Propagation REQUIRES_NEW, NESTED 비교도 깊게 다뤄줄 수 있다.

계속 설명할까?

11. 내부 트랜잭션 코드 -> 트랜잭션 매니저: 내부 트랜잭션 커밋
12. 트랜잭션 매니저: New 트랜잭션 확인 -> 아니면 커밋 호출x
13. 외부 트랜잭션 코드 -> 트랜잭션 매니저: 외부 트랜잭션 커밋
14. 트랜잭션 매니저: New 트랜잭션 확인 -> 맞으면 커밋 호출o
15. 트랜잭션 매니저 -> 트랜잭션 동기화 매니저: 물리 커밋

아래 설명은 질문 11~15 단계가 **Spring의 트랜잭션 종료 과정에서 실제로 어떤 클래스가 어떤 로직을 실행하는지**, 그리고 **왜 내부 트랜잭션은 물리 커밋을 하지 않고 외부 트랜잭션만 물리 커밋을 하는지**를 정확한 실행 구조로 정리한 것이다.

---

# 전체 흐름 요약

* 내부 트랜잭션(@Transactional 내부 호출)은 **TransactionStatus 객체는 생기지만 실제로는 기존 트랜잭션에 “참여”만 한다.**
* 참여 트랜잭션은 커밋 요청이 와도 **물리적 commit()을 실행할 권한이 없다.**
* 외부 트랜잭션만이 Connection을 실제로 연 주체이므로 **Connection.commit()을 호출할 수 있는 유일한 주체**다.

이제 단계별로 분석해보자.

---

# 11. 내부 트랜잭션 코드 → 트랜잭션 매니저: 내부 트랜잭션 커밋 요청

**어떤 코드에서 실행되나?**

* 내부 @Transactional 메서드가 정상 종료된 직후
* AOP Proxy가 `txManager.commit(innerStatus)`를 호출

**내부 TransactionStatus의 특징**

* `newTransaction = false`
* 즉 “내가 실제 트랜잭션을 연 주체가 아니다”라는 의미

이 상태로 트랜잭션 매니저에 commit을 요청한다.

---

# 12. 트랜잭션 매니저: New 트랜잭션 여부 확인 → 아니면 커밋 호출 X

스프링 내부 흐름

```java
if (status.isNewTransaction()) {
    // 실제 트랜잭션 시작한 경우
    doCommit(status);
} else {
    // 참여 트랜잭션인 경우
    // 물리적 commit 절대 하지 않음
}
```

즉 내부 트랜잭션에 대한 commit은 **형식적(commit 표시)**일 뿐, JDBC commit을 수행하지 않는다.

왜냐하면:

* 내부 트랜잭션은 Connection을 생성하지 않았고
* autoCommit 설정도 하지 않았고
* 실제 트랜잭션 경계를 열거나 닫는 권한이 없다

→ 따라서 **절대 물리 커밋을 하면 안 됨**
→ 외부 트랜잭션이 전체 범위를 책임지기 때문

---

# 13. 외부 트랜잭션 코드 → 트랜잭션 매니저: 외부 트랜잭션 커밋 요청

외부 @Transactional 메서드가 정상적으로 끝나면
AOP Proxy는 `txManager.commit(outerStatus)` 호출

외부 TransactionStatus는 다음과 같다:

* `newTransaction = true`
* Connection을 실제로 열고 autoCommit(false) 설정한 당사자
* 트랜잭션 동기화 매니저에 리소스를 등록한 주체

따라서 외부 트랜잭션만이 **물리 commit()을 실행할 주체**다.

---

# 14. 트랜잭션 매니저: New 트랜잭션 확인 → 맞으면 커밋 호출 O

트랜잭션 매니저 내부 로직은 다음처럼 동작한다:

```java
if (status.isNewTransaction()) {
    doCommit(status);  // <-- 실제 JDBC 커밋 호출
}
```

여기서 실행되는 것이 `DataSourceTransactionManager.doCommit()`:

```java
connectionHolder.getConnection().commit();
```

이 시점에서 비로소 전체 트랜잭션이 DB에 반영된다.

---

# 15. 트랜잭션 매니저 → 트랜잭션 동기화 매니저: 물리 커밋 이후 정리

커밋 후에 스프링은 다음을 수행한다:

1. ThreadLocal에서 ConnectionHolder 제거
   `TransactionSynchronizationManager.unbindResource(dataSource)`
2. Connection을 닫거나 풀에 반환
3. Synchronization 콜백(beforeCommit, afterCommit) 실행
4. 트랜잭션 완료 표시

이 과정을 통해 **하나의 트랜잭션(외부 트랜잭션) 전체가 종료**된다.

---

# 전체 구조를 쉽게 정리하면:

## 내부 트랜잭션(@Transactional 메서드)

* TransactionStatus 객체는 새로 생김
* 하지만 newTransaction = false
* commit() 요청 → 실제 커밋 없음
* rollback() 요청 → rollback-only 플래그만 설정
* 물리적 commit/rollback 불가 (권한 없음)

## 외부 트랜잭션

* newTransaction = true
* Connection을 열고 관리
* commit() 요청 → 실제 JDBC commit 실행
* 최종 트랜잭션 경계 담당

---

# 한 줄로 요약

**내부 트랜잭션은 ‘논리적 경계’만 제공하며 물리 트랜잭션 제어 권한이 없고, 외부 트랜잭션만이 실제 커밋과 롤백의 권한을 가진다.**

---

원하면

* 실제 Spring 내부 코드 경로 (`AbstractPlatformTransactionManager`, `DataSourceTransactionManager`)를 스트립트처럼 단계별로 보여줄 수 있고
* REQUIRES_NEW / NESTED 상황에서 11~15번이 어떻게 달라지는지도 정리할 수 있다.

아래 설명은 **외부 트랜잭션 / 내부 트랜잭션 / 물리 트랜잭션**이 Spring에서 커밋·롤백을 어떻게 주고받는지 “정확한 규칙”으로 정리한 것이다.
(전파 옵션이 기본값인 **REQUIRED**일 때를 기준으로 한다.)

---

# 1. 세 가지 트랜잭션의 역할 구분

| 구분          | 실제 존재 여부          | 역할                       | 커밋·롤백 권한                           |
| ----------- | ----------------- | ------------------------ | ---------------------------------- |
| **외부 트랜잭션** | O                 | 진짜 JDBC 트랜잭션을 시작한 논리적 경계 | 실제 커밋/롤백 가능                        |
| **내부 트랜잭션** | O (논리적 경계)        | 기존 트랜잭션에 “참여”            | 실제 커밋/롤백 불가 (rollback-only 전달만 가능) |
| **물리 트랜잭션** | O (Connection 기반) | DB와 연결된 실제 트랜잭션          | 외부 트랜잭션에 의해 제어됨                    |

즉

* **외부 트랜잭션 = 물리 트랜잭션을 제어하는 주체**
* **내부 트랜잭션 = 물리 트랜잭션을 제어하지 못하고, 단지 논리 경계만 가짐**

---

# 2. 내부 트랜잭션이 commit()을 호출하면 결과는?

**항상 “형식적 커밋”이다. 즉, 물리적 commit은 절대 일어나지 않는다.**

흐름:

1. 내부 트랜잭션 종료
2. 트랜잭션 매니저가 `status.isNewTransaction()` 검사
3. false → 실제 JDBC commit() 호출 안 함
4. 아무 일도 일어나지 않고 다음 단계 진행

따라서 **내부 트랜잭션의 commit은 물리 트랜잭션에 영향을 주지 않는다.**

---

# 3. 내부 트랜잭션이 rollback()을 호출하면?

이 경우 Spring은 다음을 수행한다:

* 실제 JDBC rollback()을 호출하지 않음
* 대신 **외부 트랜잭션에 “rollback-only” 플래그만 설정**

즉,

1. 내부에서 오류 발생 → rollback 요청
2. 내부 TransactionStatus가 rollback-only 표시
3. 외부 트랜잭션이 commit하려 할 때 플래그를 확인
4. commit 불가 → 전체 물리 트랜잭션 rollback 수행

결과적으로:

**내부 트랜잭션의 rollback 요청은 외부 트랜잭션을 강제로 전체 롤백시킨다.**

---

# 4. 외부 트랜잭션의 commit() 호출 시

외부 트랜잭션은 물리 트랜잭션의 주체이므로 commit 발생 여부는 다음으로 결정된다:

## 케이스 A: 내부에서 rollback-only가 설정되지 않음

→ **정상 commit**

```java
connection.commit();
```

## 케이스 B: 내부 트랜잭션 중 rollback-only 설정됨

→ **commit 대신 물리 rollback 발생**

```java
connection.rollback();  // 전체 트랜잭션 취소
```

즉, 외부 트랜잭션은 내부의 rollback-only 표시를 최종적으로 확인하는 책임자다.

---

# 5. 외부 트랜잭션이 rollback()을 호출하면?

* 물리 트랜잭션을 rollback()
* 내부/외부 경계는 모두 롤백됨(하나의 물리 트랜잭션이기 때문)

내부 트랜잭션은 물리 경계를 갖고 있지 않으므로 **외부가 롤백하면 내부도 자동으로 롤백되는 것처럼 보인다.**

---

# 6. 최종적으로 세 주체의 커밋·롤백 관계 요약

아래는 Spring REQUIRED 전파에서 발생하는 모든 규칙을 통합한 것이다.

## 내부 트랜잭션 commit

* 물리 commit 없음
* 단순히 자기 논리 경계만 종료
* 외부 트랜잭션에게 영향 없음

## 내부 트랜잭션 rollback

* 물리 rollback 없음
* rollback-only 플래그만 설정
* 결국 **외부 트랜잭션 commit 순간에 전체 rollback 발생**

## 외부 트랜잭션 commit

* rollback-only 플래그가 없으면 물리 commit
* rollback-only 플래그가 있으면 물리 rollback

## 외부 트랜잭션 rollback

* 즉시 물리 rollback
* 내부/외부 모든 논리 경계가 다 롤백된 것과 동일한 효과

---

# 7. 더 쉬운 비유로 정리

외부 트랜잭션이 **“운전대”**라면,
내부 트랜잭션은 **“동승자”**다.

* 내부 트랜잭션(commit/rollback)은 운전대를 움직일 수 없다.
* 단지 “이제 가면 안 된다”라는 신호(rollback-only)를 줄 수 있을 뿐이다.
* 최종적으로 브레이크/엑셀(커밋/롤백)을 밟는 사람은 외부 트랜잭션뿐이다.

---

# 마지막 핵심 정리 한 줄

**물리적 트랜잭션은 외부 트랜잭션만 제어하며, 내부 트랜잭션은 commit을 해도 영향이 없고 rollback을 하면 외부 트랜잭션을 롤백-only 상태로 만들어 전체 롤백을 유발한다.**

---

원하면

* REQUIRES_NEW 전파일 때는 어떻게 관계가 달라지는지
* NESTED(저장점 기반)일 때 내부·외부·물리 트랜잭션 관계가 어떻게 달라지는지도
  그림과 함께 정리해줄 수 있다.

아래는 Spring 트랜잭션 **전파(Propagation)** 옵션별로
**외부 트랜잭션 / 내부 트랜잭션 / 물리 트랜잭션이 커밋·롤백에서 어떻게 상호작용하는지**를
가장 정확하고 실전적으로 정리한 내용이다.

특히 질문의 맥락이 “외부/내부/물리 트랜잭션의 커밋·롤백 관계”였기 때문에
각 전파 옵션이 **물리 트랜잭션을 공유하는지**, **새로 만드는지**, **커밋·롤백 권한이 어떻게 달라지는지** 중심으로 설명한다.

---

# 1. REQUIRED (기본값)

설명: 이미 트랜잭션이 있으면 참여, 없으면 새로 시작

* 내부는 **논리 트랜잭션**만 생성 (실제 트랜잭션 X)
* 물리 트랜잭션은 **외부 트랜잭션이 단 하나**
* 내부 commit → 물리 commit 없음
* 내부 rollback → rollback-only 설정(외부가 전체 롤백)
* 외부만 물리 commit/rollback 가능

✔ 많이 사용하는 방식
✔ 내부 rollback → 외부까지 영향

---

# 2. REQUIRES_NEW

설명: 항상 새로운 트랜잭션을 시작하며, 기존 트랜잭션은 **일시 정지**됨

### 핵심

* 내부 트랜잭션이 **완전히 별도 물리 트랜잭션**을 가짐
* 외부 트랜잭션은 ThreadLocal에서 잠시 제거되어 “보류됨"

### 커밋·롤백 관계

| 주체                   | 물리 트랜잭션 영향        |
| -------------------- | ----------------- |
| 내부 commit            | 내부 물리 commit 발생   |
| 내부 rollback          | 내부 물리 rollback 발생 |
| 외부 commit            | 외부 물리 commit 발생   |
| 내부 rollback이 외부에 영향? | 없음                |

✔ 내부 실패는 외부에 영향 주지 않음
✔ 트랜잭션을 완전히 분리하고 싶을 때 사용

---

# 3. NESTED

설명: 외부 트랜잭션 내에서 **SAVEPOINT(저장점)**을 통해 부분 트랜잭션(부분 롤백)을 제공

### 조건

* JDBC가 savepoint를 지원해야 함
* `DataSourceTransactionManager`는 지원
* JPA 트랜잭션은 거의 지원 안 함

### 커밋·롤백 관계

| 동작          | 결과                            |
| ----------- | ----------------------------- |
| 내부 commit   | savepoint 유지, 실제 물리 commit 없음 |
| 내부 rollback | savepoint 롤백만 수행(외부는 유지)      |
| 외부 rollback | 물리 rollback                   |

✔ 내부 rollback이 외부까지 파괴하지 않음
✔ 하지만 내부 commit이 외부 commit보다 먼저 물리 commit 되지 않음

### REQUIRED와의 차이

* REQUIRED rollback: 외부까지 전체 롤백
* NESTED rollback: savepoint까지만 롤백 (외부는 계속 진행)

---

# 4. SUPPORTS

설명: 트랜잭션이 있으면 참여하고, 없으면 트랜잭션 없이 실행

### 커밋·롤백 관계

* 외부 트랜잭션 존재하면 REQUIRED와 동일하게 움직임
* 외부 트랜잭션 없으면

  * 내부는 트랜잭션 없이 실행
  * commit/rollback 개념이 없음
  * 모두 autoCommit=true 상태에서 쿼리 수행

✔ “트랜잭션 여부에 상관없이 동작하라”는 의미
✔ DB 변경이 있다면 보통 위험하므로 조회 메서드에 주로 사용됨

---

# 5. MANDATORY

설명: 반드시 트랜잭션이 필요. 없으면 예외 발생

* 내부 트랜잭션은 항상 REQUIRED 참여 트랜잭션처럼 동작
* 커밋·롤백 관계도 REQUIRED와 동일
* 단지 트랜잭션이 없다면 IllegalTransactionStateException 발생

✔ 내부 트랜잭션 → 물리 commit 없음
✔ rollback-only 전달 방식 동일

---

# 6. NOT_SUPPORTED

설명: 트랜잭션이 있다면 **해제(중단)**하고, 트랜잭션 없이 실행

### 커밋·롤백 관계

* 내부는 트랜잭션 없이 autoCommit=true로 실행
* 내부 commit/rollback 개념 없음
* 내부 오류가 외부 트랜잭션을 rollback-only로 만들지 않음
* 외부 트랜잭션은 내부 호출 이후 다시 재개됨

✔ 조회/포맷팅 등 안정적 작업에 사용
✔ 트랜잭션을 피하고 싶을 때 사용

---

# 7. NEVER

설명: 트랜잭션이 있으면 안 됨. 존재하면 예외 발생

* 트랜잭션이 없을 때만 실행됨
* autoCommit=true로 진행
* commit/rollback 없음
* 외부 트랜잭션이 있으면 예외로 차단

✔ 트랜잭션이 있으면 안 되는 작업(예: 특정 로그 저장)에 사용

---

# 전파 옵션별 커밋·롤백 관계 총정리 표

| 전파            | 물리 트랜잭션 공유 여부  | 내부 commit           | 내부 rollback        | 외부 영향 여부         |
| ------------- | -------------- | ------------------- | ------------------ | ---------------- |
| REQUIRED      | 공유             | 물리 commit 없음        | rollback-only 설정   | O (외부 commit 실패) |
| REQUIRES_NEW  | 분리             | 내부 물리 commit        | 내부 물리 rollback     | X                |
| NESTED        | 공유 (savepoint) | 물리 commit 없음        | savepoint rollback | X (savepoint까지만) |
| SUPPORTS      | 상황 따라 다름       | 외부 있으면 REQUIRED와 동일 | 동일                 | 동일               |
| MANDATORY     | 공유             | REQUIRED와 동일        | REQUIRED와 동일       | O                |
| NOT_SUPPORTED | 없음             | 의미 없음               | 의미 없음              | X                |
| NEVER         | 없음             | 의미 없음               | 의미 없음              | X                |

---

# 핵심만 한 줄로 정리

* **REQUIRED**: 내부 rollback → 외부 rollback
* **REQUIRES_NEW**: 내부/외부 완전 독립
* **NESTED**: 내부 rollback은 savepoint까지만, 외부에는 영향 없음

---

원하면 이 전파 속성별 동작을

* JDBC Connection 수준에서 그림으로 그려드리거나
* Spring 내부 코드 구조(`AbstractPlatformTransactionManager`, `DataSourceTransactionManager`) 기반으로 더 깊게 분석해줄 수 있다.
