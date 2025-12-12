아래는 **코드 실행 흐름 + 로그를 한 줄씩 정확히 대응해서 해석**한 설명이야.
핵심은 *REQUIRED 전파에서 내부 트랜잭션은 “논리 트랜잭션”일 뿐이고, 실제 물리 트랜잭션은 외부 하나뿐*이라는 점이야.

---

## 테스트 코드 흐름 요약

```java
TransactionStatus outer = ptm.getTransaction(...)
TransactionStatus inner = ptm.getTransaction(...)
ptm.commit(inner);
ptm.rollback(outer);
```

* 전파 옵션: 기본값 `PROPAGATION_REQUIRED`
* 따라서 **inner는 outer에 참여**
* 실제 DB 커넥션/트랜잭션은 하나만 생성됨

---

## 로그 한 줄씩 해석

### 1️⃣ 테스트 시작 로그

```
---outer_rollback()---
```

* 테스트 메서드 시작
* 아직 트랜잭션 없음

---

### 2️⃣ 외부 트랜잭션 생성

```
Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
```

* 현재 스레드에 트랜잭션이 없음
* `PROPAGATION_REQUIRED` → **새 물리 트랜잭션 생성**
* 이 시점에 **outer 트랜잭션이 진짜 주인**

---

### 3️⃣ DB 커넥션 획득

```
Acquired Connection [HikariProxyConnection@...] for JDBC transaction
```

* 커넥션 풀에서 JDBC 커넥션 하나 가져옴
* 이 커넥션이 **물리 트랜잭션의 실체**

---

### 4️⃣ auto-commit 끔

```
Switching JDBC Connection [...] to manual commit
```

* `setAutoCommit(false)`
* 여기서부터가 **진짜 DB 트랜잭션 시작 시점**

---

### 5️⃣ outer.isNewTransaction() 출력

```
outer.isNewTransaction() = true
```

* outer는 **새 물리 트랜잭션**
* 커밋/롤백 권한을 가짐

---

### 6️⃣ 내부 트랜잭션 요청

```
Participating in existing transaction
```

* 이미 외부 트랜잭션이 존재
* `PROPAGATION_REQUIRED` → **새 트랜잭션 안 만듦**
* 같은 커넥션, 같은 물리 트랜잭션에 참여

---

### 7️⃣ inner.isNewTransaction() 출력

```
inner.isNewTransaction() = false
```

* inner는 **논리 트랜잭션**
* 커밋 권한 없음
* 물리 트랜잭션 제어 불가

---

### 8️⃣ ptm.commit(inner) 호출 (중요)

```
(no commit log)
```

* inner는 새 트랜잭션이 아님
* 따라서:

    * DB commit 실행 안 함
    * 커넥션 유지
    * 아무 로그도 안 찍힘
* 의미:
  **“내부 트랜잭션은 문제없이 끝났다”는 표시만 한 것**

---

### 9️⃣ ptm.rollback(outer) 호출

```
Initiating transaction rollback
```

* outer는 **물리 트랜잭션의 주인**
* 이제 진짜 롤백 시작

---

### 🔟 JDBC 롤백 실행

```
Rolling back JDBC transaction on Connection [...]
```

* 실제 DB에 `rollback()` 호출
* outer + inner에서 했던 모든 작업이 전부 롤백됨

---

### 1️⃣1️⃣ 커넥션 반환

```
Releasing JDBC Connection [...] after transaction
```

* 트랜잭션 종료
* 커넥션 풀로 반환
* 스레드의 트랜잭션 컨텍스트 정리

---

### 1️⃣2️⃣ 테스트 종료 로그

```
---outer_rollback()---
```

* 테스트 정상 종료

---

## 핵심 정리 (아주 중요)

### 이 테스트에서의 관계

| 구분                    | outer | inner |
| --------------------- | ----- | ----- |
| isNewTransaction      | true  | false |
| 물리 트랜잭션 생성            | O     | X     |
| 실제 commit/rollback 가능 | O     | X     |
| DB 커넥션                | 동일    | 동일    |

### 왜 inner.commit()은 아무 일도 안 일어났나?

* REQUIRED 전파
* inner는 **물리 트랜잭션이 아님**
* 그래서 commit은 “지연”되고,
* 최종 결과는 **outer의 rollback에 의해 결정**

---

## 한 문장 요약

> REQUIRED 전파에서는 내부 트랜잭션의 commit은 의미 없고,
> **외부 트랜잭션의 rollback 한 번으로 모든 것이 되돌려진다.**

다음으로

* `inner.rollback()` 하면 어떻게 되는지
* `REQUIRES_NEW`로 바꾸면 로그가 어떻게 달라지는지
  같이 보면 이해가 완전히 끝나.

아주 좋은 질문이야.
결론부터 말하면 **“정말로 DB에는 아무 일도 안 일어났다”가 맞지만, 트랜잭션 매니저 내부적으로는 중요한 처리는 일어났다**고 이해해야 정확해.

아래를 단계적으로 보자.

---

## 1. 왜 `inner.commit()`은 DB 로그가 하나도 없을까?

### 전제 상황

* 전파: `PROPAGATION_REQUIRED`
* outer → **새 물리 트랜잭션**
* inner → **기존 트랜잭션에 참여한 논리 트랜잭션**

즉,

* DB 커넥션: 1개
* 물리 트랜잭션: 1개
* 논리 트랜잭션: 2개 (outer, inner)

---

## 2. `inner.commit()`이 했던 일 (진짜로 아무것도 안 했나?)

### DB 관점

* `Connection.commit()` 호출 ❌
* SQL flush ❌
* auto-commit 변경 ❌

👉 **DB에는 진짜로 아무 일도 안 일어남**

---

### 트랜잭션 매니저 관점 (중요)

`inner.commit()`이 호출되면 스프링은 이렇게 판단한다:

```text
이 트랜잭션이 new 트랜잭션인가?
→ 아니다 (isNewTransaction = false)
```

그래서 스프링은:

1. 실제 커밋을 **보류**
2. “이 논리 트랜잭션은 정상 종료됨”이라고 내부적으로만 표시
3. 최종 결정권을 **외부 트랜잭션에게 위임**

즉,

> inner.commit()은
> “나는 문제없이 끝났으니, 최종 결정은 outer에게 맡길게”라는 신호

---

## 3. 그래서 outer.rollback()이 모든 걸 덮어쓴다

outer는:

* isNewTransaction = true
* 물리 트랜잭션의 주인

따라서

```java
ptm.rollback(outer);
```

→ 실제 `Connection.rollback()` 실행
→ inner에서 한 작업까지 전부 롤백

---

## 4. 그럼 `inner.rollback()`을 하면 어떻게 될까?

이게 진짜 핵심 포인트야.

### 코드가 이렇게 바뀌었다고 가정

```java
TransactionStatus outer = ptm.getTransaction(...)
TransactionStatus inner = ptm.getTransaction(...)
ptm.rollback(inner);
ptm.commit(outer);
```

---

### 로그와 실제 동작

#### 1️⃣ inner.rollback() 호출 시

스프링의 판단:

```text
inner는 new 트랜잭션인가?
→ 아니다
```

그래서 스프링은 **즉시 DB rollback을 하지 않는다.**

대신에 내부적으로:

* **rollback-only 플래그를 설정**
* 의미:

  > “이 물리 트랜잭션은 반드시 롤백되어야 한다”

이 시점에서도:

* DB rollback ❌
* 커넥션 유지됨

---

#### 2️⃣ outer.commit() 호출 시

이때 스프링이 체크한다:

```text
rollback-only 인가?
→ 예
```

그래서 결과는:

* commit 시도 ❌
* 강제 rollback 실행 ⭕

로그에는 보통 이런 흐름이 나온다:

```
Transaction marked as rollback-only
Initiating transaction rollback
Rolling back JDBC transaction
```

---

## 5. inner.rollback()의 진짜 의미

정리하면:

| 호출               | 실제 DB 동작          | 의미               |
| ---------------- | ----------------- | ---------------- |
| inner.commit()   | 아무것도 안 함          | “난 성공”           |
| inner.rollback() | rollback-only 설정  | “전체를 반드시 롤백해야 함” |
| outer.commit()   | rollback-only면 롤백 | 최종 판결            |
| outer.rollback() | 즉시 롤백             | 최종 판결            |

---

## 6. 한 줄로 정확히 정리

* `inner.commit()`
  → **DB에는 아무 일도 없고**, 내부적으로 “정상 종료” 표시만 한다
* `inner.rollback()`
  → **즉시 롤백은 안 하지만**, 전체 트랜잭션을 **되돌릴 수밖에 없게 만든다**

---

## 7. 그래서 실무에서 나오는 유명한 말

> REQUIRED 전파에서
> **내부 트랜잭션은 커밋할 수 없고, 롤백만 의미가 있다**

다음 단계로는

* `REQUIRES_NEW`에서 inner.commit / inner.rollback이 어떻게 **즉시 DB에 반영되는지**
* 또는 `UnexpectedRollbackException`이 언제 터지는지
  이걸 보면 트랜잭션 전파가 완전히 정리돼.

네. **그렇다.**
`inner.rollback()`으로 **rollback-only 플래그가 설정되면**, 이후에 `outer.commit()`을 호출해도 **전부 롤백된다**.
이건 예외가 아니라 **스프링 트랜잭션의 핵심 규칙**이야.

아래에서 왜 그런지, 그리고 실제로 어떤 일이 벌어지는지 정확히 설명할게.

---

## 1. rollback-only가 의미하는 정확한 상태

`PROPAGATION_REQUIRED`에서 `inner.rollback()`이 호출되면 스프링은 이렇게 처리한다.

* inner는 **새 트랜잭션이 아님**
* 즉시 DB rollback ❌
* 대신 **현재 물리 트랜잭션에 rollback-only 마킹**

의미는 딱 하나다.

> “이 물리 트랜잭션은 **절대 커밋되면 안 된다**”

이건 **취소 불가능한 판결**이다.

---

## 2. 그 상태에서 `outer.commit()`을 호출하면?

스프링의 커밋 로직은 항상 **이 체크부터** 한다.

```text
이 트랜잭션이 rollback-only 인가?
```

### 결과 분기

* ❌ 아니면 → 정상 커밋
* ✅ 맞으면 → **강제 롤백**

즉,

```java
ptm.rollback(inner); // rollback-only 설정
ptm.commit(outer);   // 커밋 시도
```

실제 동작은:

1. 커밋 시도
2. rollback-only 감지
3. commit 취소
4. **DB rollback 실행**

---

## 3. 로그 관점에서 벌어지는 일

보통 이런 흐름이 나온다.

```
Participating in existing transaction
Transaction marked as rollback-only
Initiating transaction rollback
Rolling back JDBC transaction
```

여기서 중요한 점:

* `outer.commit()`을 호출했는데
* 실제로는 **commit 로그가 없다**
* 대신 rollback 로그만 나온다

---

## 4. 그럼 outer 입장에서는 성공일까? 실패일까?

이게 매우 중요한 포인트다.

### 1️⃣ 트랜잭션 자체

* DB 결과: ❌ 전부 롤백
* commit 성공 ❌

### 2️⃣ 예외 발생 여부

상황에 따라 다르다.

#### 현재 네 코드처럼 직접 `ptm.commit()`을 호출한 경우

* **예외 안 터질 수도 있음**
* 그냥 롤백되고 종료될 수 있음

#### `@Transactional` 환경이라면

* `UnexpectedRollbackException` 발생
* 이유:

  > “나는 커밋하라고 했는데, 내부에서 이미 롤백이 결정돼 있었음”

---

## 5. 왜 이렇게 강하게 막을까?

이 규칙이 없으면 이런 일이 가능해진다.

* inner: “이건 심각한 오류라 롤백해야 해”
* outer: “괜찮아 보여, 커밋하자”

이러면 **데이터 무결성이 깨진다.**

그래서 스프링은:

> 한 번이라도 rollback 의사가 나오면
> **그 물리 트랜잭션은 무조건 롤백**

이라는 규칙을 강제한다.

---

## 6. 한 문장으로 정리

> `PROPAGATION_REQUIRED`에서
> **내부 트랜잭션이 rollback-only를 설정하면,
> 외부 트랜잭션의 commit 호출은 의미가 없고 결과는 항상 전체 롤백이다.**

이제 다음으로 보면 좋은 포인트는:

* 왜 `UnexpectedRollbackException`이 “외부”에서 터지는지
* `REQUIRES_NEW`에서는 왜 이 문제가 안 생기는지

이 두 개가 연결되면 트랜잭션 전파가 완전히 끝난다.

아주 핵심을 찌른 질문이야.
이걸 이해하면 **“논리 트랜잭션 / 물리 트랜잭션” 개념이 왜 필요한지**가 한 번에 정리돼.

결론부터 말하면,

> **내부 트랜잭션은 물리 트랜잭션의 주인이 아니기 때문에
> “지금 당장 롤백할 권한이 없어서” 표시만 해두는 것**이야.

아래를 차근차근 보자.

---

## 1. 권한 문제: 누가 물리 트랜잭션의 주인인가

현재 상황을 다시 쓰면:

* outer: `isNewTransaction = true`
* inner: `isNewTransaction = false`
* 물리 트랜잭션: **outer가 생성**
* DB 커넥션: **outer가 획득**

즉,

> **물리 트랜잭션의 생명주기를 시작한 주체만이
> 끝낼(커밋/롤백) 권한을 가진다**

inner는:

* 커넥션을 만들지도 않았고
* auto-commit을 끄지도 않았고
* 트랜잭션을 시작하지도 않았다

그래서 **즉시 rollback을 해버리면 안 된다.**

---

## 2. “바로 롤백하면 안 되는” 구체적인 이유

### 이유 1. 외부 트랜잭션의 제어 흐름을 깨뜨리면 안 된다

outer 코드 입장에서는 아직 이런 상태일 수 있다.

```java
try {
    serviceA(); // inner.rollback() 발생
    serviceB(); // 아직 실행 중
    serviceC();
    commit();
} catch (...) {
    rollback();
}
```

만약 inner가 **몰래 DB rollback을 해버리면**:

* outer는 아직 트랜잭션이 살아있다고 믿음
* 하지만 DB에서는 이미 트랜잭션이 종료됨
* 이후 SQL 실행 → 예측 불가능한 상태

그래서 스프링은:

> “지금은 표시만 하고,
> 정리는 반드시 트랜잭션 주인이 하게 한다”

---

### 이유 2. 트랜잭션 동기화 자원들이 아직 살아 있다

스프링 트랜잭션에는 DB 말고도 이런 것들이 엮인다.

* `TransactionSynchronization`
* JPA `EntityManager`
* Hibernate flush / clear
* 이벤트 리스너 (`afterCommit`, `afterCompletion`)

이 것들은 **외부 트랜잭션 종료 시점**에 한 번에 정리돼야 한다.

inner에서 바로 rollback하면:

* 동기화 콜백 순서 깨짐
* 리소스 정리 중복
* afterCommit/afterRollback 타이밍 붕괴

---

### 이유 3. “한 트랜잭션 = 한 종료 지점” 규칙

스프링은 의도적으로 이 규칙을 강제한다.

> 하나의 물리 트랜잭션은
> **정확히 한 번만 종료**되어야 한다

* 시작: outer
* 종료: outer

inner에서 종료시키면 **중복 종료**가 된다.

---

## 3. rollback-only는 “예약된 판결”이다

그래서 스프링은 이렇게 설계했다.

* inner.rollback():

  * 판결만 남김
  * “이 트랜잭션은 실패”
* outer.commit() or outer.rollback():

  * 판결 집행

이건 법원 비유로 보면 딱 맞다.

* inner: 하급심 → 유죄 판결
* outer: 대법원 → 집행

하급심이 바로 형을 집행하면 안 되는 것과 같은 이유다.

---

## 4. 만약 바로 롤백하도록 허용하면 생기는 문제

가정해보자.

> inner.rollback() → 즉시 DB rollback 허용

그러면 이런 일이 가능해진다.

* inner에서 rollback
* outer는 모르고 commit 시도
* 스프링은 이미 종료된 트랜잭션에 commit 시도
* DB / 커넥션 상태 불일치
* 심각한 버그 발생

그래서 **설계적으로 금지**되어 있다.

---

## 5. 그럼 언제 “바로 롤백”이 가능할까?

오직 한 경우뿐이다.

* `inner.isNewTransaction() == true`

즉:

* `PROPAGATION_REQUIRES_NEW`
* `PROPAGATION_NESTED` (savepoint 기반)

이때는 inner가 **자기 물리 트랜잭션을 가짐**

그래서:

* inner.rollback() → 즉시 DB rollback 가능
* outer에는 영향 없음 (REQUIRES_NEW)
* 또는 savepoint로 부분 롤백 (NESTED)

---

## 6. 한 문장으로 요약

> rollback-only는
> **“지금은 롤백할 권한이 없으니,
> 최종 종료 시점에 반드시 롤백하라”는 예약된 명령**이다.

이 개념이 잡히면
왜 REQUIRED가 위험할 수 있는지,
왜 실무에서 REQUIRES_NEW를 조심스럽게 쓰는지도 자연스럽게 이어진다.

짧게 답하면 **“보통은 안 나지만, 특정 조건에서는 난다”**야.
그리고 **언제, 어떤 종류의 에러가 나는지**를 구분해서 이해해야 헷갈리지 않는다.

아래를 단계별로 보자.

---

## 1. 일반적인 경우: 롤백 자체는 거의 실패하지 않는다

### 정상적인 JDBC 트랜잭션에서

```java
connection.rollback();
```

* 대부분의 DB에서 **롤백은 매우 안정적인 작업**
* 이미 변경된 내용을 되돌리는 동작이라
* commit보다 실패 확률이 훨씬 낮다

그래서 **대부분의 경우**:

* rollback 수행
* 예외 없음
* 로그만 남고 정상 종료

---

## 2. 그런데 “에러처럼 보이는 상황”들이 있다

여기서부터가 중요하다.
롤백 중에 *진짜 에러*가 나는 경우와,
*롤백 때문에 예외가 던져지는 경우*는 다르다.

---

## 3. 케이스 1: rollback-only + outer.commit()

이건 **롤백 에러가 아니다.**

### 상황

* inner.rollback() → rollback-only 설정
* outer.commit() 호출

### 결과

* 실제 동작: **rollback 실행**
* 이때 스프링이 던질 수 있는 예외:

```
UnexpectedRollbackException
```

### 중요한 점

* DB 롤백은 **정상적으로 성공**
* 예외의 의미는 이것이다:

> “너는 commit을 요청했는데,
> 이미 내부에서 rollback이 결정돼 있었어”

즉,

* **롤백 실패 ❌**
* **커밋 기대가 깨졌다는 신호 ⭕**

---

## 4. 케이스 2: 진짜로 롤백 중 DB 에러가 나는 경우

이건 드물지만 가능하다.

### 예시 상황

* DB 커넥션이 이미 끊김
* 네트워크 장애
* 커넥션 풀에서 비정상 상태
* DB 서버 다운

이 경우 로그는 보통 이렇게 된다.

```
Could not roll back JDBC transaction
```

그리고 내부적으로는:

* `SQLException`
* 또는 이를 감싼 `TransactionSystemException`

### 특징

* 이미 DB 상태는 **알 수 없음**
* 스프링도 더 이상 복구 불가
* 커넥션은 폐기(discard)됨

---

## 5. 케이스 3: 이중 롤백 / 이미 종료된 트랜잭션

예를 들면:

* inner에서 잘못된 방식으로 강제 종료
* 외부에서 다시 rollback 시도

이 경우:

* 대부분은 **무시하거나 로그만 남김**
* 심각한 예외는 잘 안 던진다

스프링은 “롤백은 최대한 안전하게” 설계되어 있다.

---

## 6. 정리 표

| 상황                     | DB 롤백 실패 | 예외 발생 | 의미       |
| ---------------------- | -------- | ----- | -------- |
| 정상 rollback            | ❌        | ❌     | 정상       |
| rollback-only + commit | ❌        | ⭕     | 커밋 불가 알림 |
| DB 장애                  | ⭕        | ⭕     | 시스템 장애   |
| 중복 rollback            | ❌        | △     | 대부분 무시   |

---

## 7. 그래서 실무에서 기억할 한 문장

> **“롤백은 거의 실패하지 않는다.
> 대신, 롤백 때문에 예외가 던져지는 경우가 있다.”**

그리고 그 예외의 대부분은:

* DB 문제가 아니라
* **트랜잭션 흐름 위반을 알려주는 신호**다.

---

다음으로 자연스럽게 이어지는 질문은 보통 이거다.

* 왜 `UnexpectedRollbackException`은 **외부에서만** 터질까
* 왜 내부에서는 조용히 rollback-only만 설정할까

이 둘은 지금 질문과 정확히 연결돼 있다.

결론부터 말하면, **저렇게 `new EntityManager()` 형태로 만드는 것은 불가능**하고, **권장되지도 않습니다**.
`EntityManager`는 애플리케이션 코드에서 직접 생성하는 객체가 아니라 **JPA와 Spring이 관리하는 컨텍스트 객체**입니다.

아래에서 왜 안 되는지, 그리고 올바른 방법을 단계별로 설명할게요.

---

## 1. 왜 `@Bean EntityManager()`로 만들 수 없나

```java
@Bean
EntityManager entityManager() {
    return new EntityManager(); // ❌ 불가능
}
```

이게 안 되는 이유는:

1. `EntityManager`는 **인터페이스**
2. 실제 구현체(`Hibernate EntityManager`)는

  * `EntityManagerFactory`
  * 트랜잭션
  * 영속성 컨텍스트
    와 강하게 결합되어 있음
3. Spring은 트랜잭션 범위에 맞춰

  * 프록시 EntityManager
  * Thread-bound EntityManager
    를 자동으로 주입함

즉, **Spring이 생명주기를 통제해야 하는 객체**라서 `new`로 만들 수 없음.

---

## 2. 정석적인 방법 (가장 권장)

### 2.1 Repository에서 직접 주입

```java
@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public MemberRepository(EntityManager em) {
        this.em = em;
    }
}
```

Spring Boot라면:

* `EntityManagerFactory`
* `EntityManager`
* `TransactionManager`

모두 자동 구성됨

---

### 2.2 Service도 마찬가지

```java
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    public MemberService(MemberRepository memberRepository,
                         LogRepository logRepository) {
        this.memberRepository = memberRepository;
        this.logRepository = logRepository;
    }
}
```

이렇게 두고 **Config에서 직접 new 하지 않는 게 핵심**입니다.

---

## 3. Config에서 굳이 명시하고 싶다면

정말 테스트나 학습 목적으로 Config를 쓰고 싶다면 이렇게 합니다.

```java
@Configuration
public class Config {

    @PersistenceContext
    private EntityManager em;

    @Bean
    MemberService memberService() {
        return new MemberService(
            new MemberRepository(em),
            new LogRepository(em)
        );
    }
}
```

이 경우에도 중요한 점은:

* `EntityManager`는 **Spring이 주입**
* Config에서는 **조립만 담당**

---

## 4. 절대 피해야 할 형태

```java
@Bean
MemberService memberService() {
    return new MemberService(
        new MemberRepository(entityManager()),
        new LogRepository(entityManager())
    );
}
```

이 방식의 문제점:

* 트랜잭션 동기화 깨짐
* 같은 트랜잭션인데 EntityManager가 다를 수 있음
* `@Transactional` 의미 없어짐
* 실제 서비스 코드에서는 거의 버그로 이어짐

---

## 5. 한 줄 요약

* `EntityManager`는 `@Bean`으로 직접 만들지 않는다
* `@PersistenceContext` 또는 생성자 주입으로 받는다
* Config는 객체 생성이 아니라 **의존성 연결용**

---

지금 코드를 보면 **트랜잭션 전파(REQUIRES_NEW) 실험 중**인 것 같은데,
그 목적이라면 현재 구조를 조금 더 트랜잭션 친화적으로 다듬어주는 게 좋습니다.
원하면 그 방향으로 예제도 같이 정리해 줄게요.

원인은 **JUnit 5의 테스트 클래스 생성 방식과 Lombok `@RequiredArgsConstructor`가 충돌**했기 때문입니다.
에러 메시지가 아주 정확하게 그걸 말해주고 있어요.

---

## 1. 에러의 핵심 의미

```
No ParameterResolver registered for parameter [MemberService memberService]
```

JUnit Jupiter는 **테스트 클래스의 생성자를 직접 호출**합니다.
그런데 지금 테스트 클래스는 이렇게 생겼죠.

```java
@SpringBootTest
@RequiredArgsConstructor
public class MemberServiceTest {

    private final MemberService memberService;
}
```

Lombok이 아래 생성자를 만들어버립니다.

```java
public MemberServiceTest(MemberService memberService) {
    this.memberService = memberService;
}
```

하지만 JUnit 5 입장에서는:

* “이 생성자의 파라미터를 누가 넣어주지?”
* Spring은 아직 관여하지 않음
* JUnit에는 `MemberService`를 해결해 줄 `ParameterResolver`가 없음

그래서 바로 예외가 발생합니다.

---

## 2. 중요한 개념 정리 (핵심)

* **Spring이 빈을 주입하는 대상**

  * `@Component`, `@Service`, `@Configuration`
* **JUnit이 직접 생성하는 대상**

  * 테스트 클래스

즉:

> 테스트 클래스 생성자 주입은
> **Spring이 아니라 JUnit이 처리**한다

그래서 일반적인 Spring 생성자 주입 방식이 **테스트 클래스에서는 기본적으로 안 됩니다**.

---

## 3. 가장 정석적인 해결 방법 (권장)

### 필드 주입으로 바꾸기

```java
@SpringBootTest
public class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @TestConfiguration
    static class Config {
        @PersistenceContext
        private EntityManager em;

        @Bean
        MemberService memberService() {
            return new MemberService(
                    new MemberRepository(em),
                    new LogRepository(em)
            );
        }
    }

    @Test
    void save_one() {
        memberService.join("ne");
    }
}
```

이 방식은:

* 테스트 클래스는 **기본 생성자**
* Spring TestContext가 테스트 인스턴스 생성 후
* `@Autowired` 필드를 주입

가장 안정적이고 흔한 패턴입니다.

---

## 4. 생성자 주입을 꼭 쓰고 싶다면 (JUnit 5 전용 설정)

JUnit 5에서는 **명시적으로 허용**해야 합니다.

```java
@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
public class MemberServiceTest {

    private final MemberService memberService;
}
```

이렇게 하면:

* JUnit이 생성자를 호출할 때
* Spring 컨텍스트에서 파라미터를 찾아서 주입

하지만 단점도 분명합니다.

* 테스트 클래스에서만 특수 설정
* 팀 단위에서는 헷갈리기 쉬움
* 실무에서는 거의 안 씀

그래서 보통은 **필드 주입을 선택**합니다.

---

## 5. 지금 구조에서 추가로 짚을 점

### TestConfiguration 위치는 매우 좋음

```java
@TestConfiguration
static class Config { ... }
```

* 테스트 전용 빈 정의
* 메인 컨텍스트 오염 없음
* 트랜잭션 실험용으로 아주 적절

이 부분은 **전혀 문제가 없습니다**.

---

## 6. 한 줄 요약

* `@RequiredArgsConstructor` 때문에 **JUnit이 생성자를 해석 못함**
* 테스트 클래스는 기본적으로 **필드 주입 사용**
* 생성자 주입을 쓰려면 `@TestConstructor`가 필요

---

지금 흐름을 보면
다음 단계로는 **`@Transactional` + `REQUIRES_NEW`가 실제로 어떤 EntityManager를 쓰는지 로그로 확인**하는 게 딱 좋습니다.
그쪽으로 이어서 정리해 줄까요?

이 에러는 **아주 전형적인 JPA + Spring 테스트에서의 트랜잭션 문제**입니다.
로그가 말해주는 원인은 하나로 정리됩니다.

---

## 1. 에러 메시지의 정확한 의미

```
No EntityManager with actual transaction available for current thread
TransactionRequiredException
```

이 뜻은:

* `EntityManager` 자체는 존재한다
* 하지만 **현재 스레드에 시작된 트랜잭션이 없다**
* 그런데 `persist()` 는 **반드시 트랜잭션 안에서만 가능**

그래서 JPA가 예외를 던진 겁니다.

---

## 2. 왜 트랜잭션이 없는가

지금 테스트 흐름을 보면:

```java
@Test
void save_one() {
    memberService.join("ne");
}
```

그리고 서비스 코드(추정):

```java
public class MemberService {
    public void join(String name) {
        memberRepository.save(...); // persist
    }
}
```

**어디에도 `@Transactional` 이 없습니다.**

Spring의 트랜잭션은:

* 자동으로 시작되지 않는다
* `@Transactional` 이 붙은 메서드가 호출될 때만 시작된다

즉 현재 흐름은:

1. 테스트 메서드 실행
2. 서비스 메서드 호출
3. EntityManager는 있음 (프록시)
4. 트랜잭션은 없음
5. `persist()` → 즉시 예외

---

## 3. 가장 간단한 해결 방법 (테스트 기준)

### 테스트 클래스에 트랜잭션 적용

```java
@SpringBootTest
@Transactional
class MemberServiceTest {
    ...
}
```

이렇게 하면:

* 각 테스트 메서드 실행 전에 트랜잭션 시작
* 테스트 종료 후 자동 롤백
* `persist()` 정상 동작

테스트에서는 **이게 가장 흔한 패턴**입니다.

---

## 4. 더 정석적인 구조 (실무 기준)

### 서비스 계층에 트랜잭션 부여

```java
@Service
@Transactional
public class MemberService {

    public void join(String name) {
        memberRepository.save(...);
    }
}
```

그리고 테스트는 단순 호출만 합니다.

```java
@SpringBootTest
class MemberServiceTest {
    @Autowired MemberService memberService;
}
```

이 구조의 장점:

* 트랜잭션 책임이 서비스 계층에 명확
* 테스트 / 실제 실행 환경 동일
* 전파 옵션(REQUIRED, REQUIRES_NEW) 실험 가능

---

## 5. 지금 구조에서 특히 중요한 포인트

지금 Config에서 만든 `MemberService` 는:

```java
@Bean
MemberService memberService() {
    return new MemberService(
        new MemberRepository(em),
        new LogRepository(em)
    );
}
```

여기서 주의할 점:

* 이 `MemberService` 는 **프록시가 아님**
* 따라서 클래스에 `@Transactional` 을 붙여도
  → **적용되지 않습니다**

이건 매우 중요합니다.

### 이유

Spring의 `@Transactional` 은 **프록시 기반 AOP**인데,
지금은:

* `@Service` 아님
* `@Configuration` 에서 직접 `new` 로 생성

즉, 트랜잭션 AOP가 끼어들 수 없습니다.

---

## 6. 그래서 지금 실험 구조에서의 정답

### 선택지 1 (테스트용으로 가장 명확)

```java
@SpringBootTest
@Transactional
class MemberServiceTest { ... }
```

트랜잭션은 테스트가 책임진다.

---

### 선택지 2 (트랜잭션 실험 목적이라면)

```java
@Bean
@Transactional
MemberService memberService() { ... }
```

이건 **동작하지 않습니다**.
Bean 메서드에 붙여도 소용없습니다.

---

### 선택지 3 (권장되는 실험 구조)

* `MemberService` 를 `@Service` 로 등록
* 트랜잭션은 서비스에 선언
* TestConfiguration 에서는 Repository 만 조립

이렇게 해야 `REQUIRES_NEW` 같은 전파 옵션이 **의미를 가집니다**.

---

## 7. 한 줄 요약

* `persist()` 는 반드시 트랜잭션 안에서 실행돼야 한다
* 지금은 **트랜잭션을 시작하는 주체가 없다**
* 테스트에 `@Transactional` 을 붙이거나
* 서비스가 **프록시 빈**이 되도록 구조를 바꿔야 한다

---

지금까지 흐름을 보면
다음으로 자연스럽게 이어지는 질문은 이겁니다:

> “TestConfiguration에서 만든 Service에
> @Transactional이 왜 안 먹히는가”

그걸 정확히 이해하면
Spring 트랜잭션 AOP 구조가 한 번에 정리됩니다.
