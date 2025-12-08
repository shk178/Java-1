`SELECT ... FOR UPDATE`는 **트랜잭션 내부에서 특정 행(row)을 배타적으로 잠그기 위해 사용하는 SQL 문법**이다. 주로 동시성 제어, 즉 여러 트랜잭션이 동일한 데이터를 동시에 수정하려 할 때 **경쟁 상태(race condition)**를 방지하는 데 쓰인다.

아래에서 개념, 동작 방식, 예제, 주의사항을 순서대로 설명해줄게.

---

## 1. 기본 개념

`SELECT ... FOR UPDATE`는 다음을 의미한다.

* **조회된 행에 대해 배타적 잠금(exclusive lock)을 건다.**
* 잠금은 **트랜잭션이 종료될 때까지 유지**된다. (`COMMIT` 또는 `ROLLBACK`)
* 다른 트랜잭션은 해당 행을 **수정하거나 DELETE, FOR UPDATE 조회** 등을 할 수 없고, 잠금이 풀릴 때까지 **대기(block)** 하게 된다.

즉,

> “내가 이 데이터를 수정할 예정이니, 다른 트랜잭션은 이 행을 건드리지 마.”

라는 의도를 명시한다.

---

## 2. 동작 방식

예를 들어 두 개의 트랜잭션 T1, T2가 있다고 하자.

### 상황 1. T1이 먼저 SELECT FOR UPDATE 실행

1. T1:

   ```sql
   BEGIN;
   SELECT * FROM accounts WHERE id = 1 FOR UPDATE;
   ```

   → id=1 행이 T1에 의해 잠김.

2. T2:

   ```sql
   SELECT * FROM accounts WHERE id = 1 FOR UPDATE;
   ```

   → T1이 잠금을 해제할 때까지 **대기**.

3. T1이 `COMMIT` 또는 `ROLLBACK`을 하면 잠금이 풀리며, T2가 그 후에 잠금을 획득해 쿼리를 진행한다.

---

## 3. 주로 쓰는 이유

### 1) 동시 업데이트 방지

은행 잔고 업데이트, 재고 감소 등 **정확한 상태 관리가 필요한 경우**.

예: 재고가 1개인데 동시에 두 요청이 들어오는 상황을 막기.

### 2) 비관적 락(Pessimistic Lock) 구현

JPA에서도 `LockModeType.PESSIMISTIC_WRITE`가 결국 DB의 `SELECT FOR UPDATE`와 연결된다.

---

## 4. 사용 예제

### 예제 1: 단순 락

```sql
BEGIN;

SELECT balance
FROM accounts
WHERE id = 10
FOR UPDATE;

UPDATE accounts
SET balance = balance - 100
WHERE id = 10;

COMMIT;
```

### 예제 2: 여러 행 잠금

```sql
SELECT *
FROM orders
WHERE status = 'READY'
FOR UPDATE;
```

---

## 5. DBMS별 차이(중요)

### PostgreSQL

* 실제로 조회된 행만 잠금.
* 인덱스 조건을 통한 row-level locking.

### MySQL(InnoDB)

* 조건에 따라 **갭 락(gap lock)** 또는 **넥스트키 락(next-key lock)** 가능.
  즉, 행뿐 아니라 인접한 인덱스 범위도 잠글 수 있어 **동시성 영향이 더 클 수 있음**.

---

## 6. 주의사항

1. **트랜잭션 길이가 길어지면 락 대기 시간이 증가** → 성능 저하 원인.
2. **Deadlock** 위험.

    * 두 트랜잭션이 서로가 잠근 행을 기다리면 DB가 한쪽을 강제로 롤백시킨다.
3. 잠금을 사용하고 싶지 않다면 **SELECT 자체만으로는 비관적 잠금을 사용하지 않는 것이 좋다.**
4. 애플리케이션 단에서 관리되지 않으면 **병목 구간이 쉽게 만들어짐**.

---

## 7. 언제 사용해야 할까?

* “이 행을 읽자마자 바로 수정할 것이다”
* “동일 데이터를 두 요청이 동시에 수정하면 안 된다”
* “재고/카운트/수량 같은 값이 정확해야 한다”

이런 상황에서 `SELECT FOR UPDATE`는 매우 유용하다.

---

원하면 다음 내용도 더 설명해줄 수 있어

* MySQL과 PostgreSQL의 차이를 그림으로 비교
* `FOR UPDATE`, `FOR SHARE`, `FOR NO KEY UPDATE` 차이
* JPA에서 비관적 락과의 관계
* 갭락(next-key lock) 때문에 발생하는 문제 사례

어떤 부분이 가장 궁금해?

결론부터 말하면,
**트랜잭션 안에서 단순히 `SELECT`만 사용하는 것은 “안전하지 않을 수 있다.”**

왜냐하면 **트랜잭션은 조회한 데이터를 보호하지 않기 때문**이며,
조회한 순간에는 맞았던 값이 **동시에 다른 트랜잭션에 의해 변경될 수 있기 때문**이다.

즉, 트랜잭션은 **일관적 변경(원자성)을 보장하지만, 조회값 자체가 잠기는 것은 아니다.**

아래에서 이유를 단계별로 설명할게.

---

# 1. 트랜잭션은 “내 작업의 원자성”을 보장할 뿐, “조회한 데이터의 안전”을 보장하지 않는다

트랜잭션을 열고 다음을 한다고 하자.

```sql
BEGIN;

SELECT stock FROM item WHERE id = 1;

-- 여기서 stock = 10이라고 읽음
```

그런데 이 시점 이후에 **다른 트랜잭션이 동일한 행을 수정할 수 있다.**

예:

```
T2: UPDATE item SET stock = stock - 5 WHERE id = 1;
```

그러면 T1이 읽은 값은 이미 **무효한(old)** 값이 된다.

→ 즉, T1은 “트랜잭션 안에서 SELECT를 했지만”,
**조회한 값은 다른 트랜잭션에 의해 변경될 수 있다.**

그래서 “안전”하다고 할 수 없다.

---

# 2. SELECT는 기본적으로 “락을 걸지 않는다”

SELECT는 일반적으로 다음을 의미한다.

> “그냥 데이터를 읽는 것이지, 이 데이터를 내가 수정하려는 건 아니다.”

그래서 DB는 해당 행에 **락을 걸지 않는다.**

락이 걸리지 않는다는 뜻은:

* 다른 트랜잭션이 자유롭게 UPDATE / DELETE 가능
* 그 결과로 내가 읽은 값이 더 이상 유효하지 않을 수 있음

즉, **동시성 문제(race condition)**가 발생한다.

---

# 3. 해결 방법: SELECT FOR UPDATE (비관적 락)

만약 조회 → 수정 흐름을 안전하게 하고 싶다면:

```sql
SELECT * FROM item WHERE id = 1 FOR UPDATE;
```

이렇게 해야 한다.

이 경우:

* 그 행은 **내 트랜잭션이 끝날 때까지 다른 트랜잭션이 수정 불가**
* 내가 읽은 데이터는 변경되지 않음
* 즉, 읽기-수정 작업이 “안전”해짐

---

# 4. 단순 SELECT가 안전한 경우도 있다

단순 SELECT라도 아래와 같은 경우는 안전하다고 볼 수 있다.

### 1) 데이터 정합성보다 읽기 성능이 더 중요한 OLAP/조회 시스템

예: 게시판 글 조회수 조회, 상품 목록 조회 등
변경되는 동안 읽어도 큰 문제 없음.

### 2) Repeatable Read 같은 격리 수준에서 “읽은 값이 동일하게 보이기만 하면 되는 경우”

하지만 이 경우도 **실제 데이터는 변경될 수 있지만, 트랜잭션 내에서 보이는 값만 동일한 것**이므로
업데이트를 포함한 로직에서는 여전히 위험하다.

---

# 5. 핵심 정리

| 케이스                      | 안전한가?                    | 이유                     |
| ------------------------ | ------------------------ | ---------------------- |
| 트랜잭션 + 단순 SELECT         | 안전하지 않을 수 있음             | 다른 트랜잭션이 데이터를 수정할 수 있다 |
| 트랜잭션 + SELECT FOR UPDATE | 안전함                      | 해당 행이 잠겨 수정 불가         |
| 단순 SELECT만 진행            | 대체로 괜찮지만 동시성 문제는 생길 수 있음 | 읽기 위주 시스템에서는 크게 문제 없음  |
| 조회 후 업데이트하는 로직           | **반드시 락을 고려해야 함**        | 레이스 컨디션 발생             |

---

# 6. 왜 많은 개발자가 착각할까?

트랜잭션을 “내가 읽은 값은 보호되겠지?”
라고 생각하는 경우가 많기 때문이다.

하지만 트랜잭션은 **변경 작업의 원자성**만 보장할 뿐,
데이터 자체의 독점권을 보장하지는 않는다.

이를 보장하려면 **락을 명시적으로 걸어야 한다.**

---

원하면 다음도 설명해줄게.

* 격리 수준별 SELECT의 차이 (e.g. READ COMMITTED vs REPEATABLE READ vs SERIALIZABLE)
* “SELECT 후 UPDATE”가 왜 위험한지 실제 동시성 시나리오로 시각화
* MySQL InnoDB의 gap lock이나 next-key lock 영향
* JPA에서 비관적 락과 낙관적 락 선택하는 기준

어떤 부분이 더 궁금해?

좋은 질문이다.
여기서 핵심은 **MVCC는 “읽기 일관성”을 위한 기술이고, 락(lock)은 “쓰기(수정) 충돌을 방지하기 위한 기술”이라는 점**이다.
둘은 목적이 다르며, 서로를 대체하지 않는다.

아래에서 차이를 명확하게 정리해줄게.

---

# 1. MVCC는 읽기(SELECT)를 위한 일관성 기술

MVCC(Multiversion Concurrency Control)는 **SELECT가 락을 걸지 않고도 안정적으로 과거 버전을 읽을 수 있게 만드는 기술**이다.

### MVCC의 역할

* SELECT는 **행을 잠그지 않는다**
* 대신 **이전 버전(row undo log)** 을 읽어
  “트랜잭션 시작 시점 기준으로 일관적인 데이터”를 제공한다
* 즉, **읽기는 충돌 없이 동시에 많이 발생 가능**

예를 들어,
T1과 T2가 동시에 같은 행을 읽고 수정한다고 하자.

```
행의 실제값 = 100

T1(조회): 100 읽음
T2(조회): 100 읽음
```

이때 T1과 T2는 서로 방해하지 않는다.
MVCC는 **읽기 간 충돌을 제거**하는 기술이다.

---

# 2. 락(lock)은 쓰기(write) 충돌을 방지하는 기술

락의 목적은 완전히 다르다.

> “이 행을 내가 수정할 예정이니, 다른 트랜잭션은 접근하거나 변경하지 마라.”

예:
T1이 100을 90으로 줄이려고 하는데
T2도 동시에 100을 80으로 줄이려 한다면?

이건 **경쟁 상태(race condition)** 이 생긴다.

MVCC는 이 문제를 해결하지 못한다.
MVCC는 “읽기”를 잘 처리해줄 뿐,
쓰기는 여전히 **서로 충돌한다.**

그래서 쓰기 작업에는 **배타적 락(exclusive lock)** 이 필요하다.

---

# 3. MVCC와 락은 역할이 다르다 (한 줄 요약)

| 기술   | 목적             | SELECT 영향      | UPDATE 영향 |
| ---- | -------------- | -------------- | --------- |
| MVCC | 읽기 일관성, 높은 동시성 | 락 없음, 과거 버전 읽음 | 충돌 해결 X   |
| Lock | 쓰기 충돌 방지       | (일반적으로 영향 없음)  | 행을 독점해야 함 |

따라서
**SELECT는 MVCC 덕분에 다른 트랜잭션을 막지 않고 읽을 수 있지만,
UPDATE는 다른 쓰기 작업과 충돌하므로 락이 필요하다.**

---

# 4. 왜 SELECT FOR UPDATE는 락이 필요한가?

SELECT FOR UPDATE는 MVCC와 다르게 다음을 의미한다.

> “나는 이 행을 곧 수정할 것이다.
> 그러니 다른 트랜잭션이 수정하지 못하게 잠궈라.”

→ 즉, MVCC가 제공하지 않는 **쓰기 안정성(write safety)** 를 보장한다.

이때 사용하는 것이 **배타적 락(exclusive lock)** 이다.

### MVCC만 있을 때 문제점

MVCC는 읽기 시점의 버전을 안정적으로 제공하지만
동시에 두 트랜잭션이 다음과 같이 하면 문제가 생긴다.

```
T1: SELECT stock = 10
T2: SELECT stock = 10

T1: UPDATE stock = 9
T2: UPDATE stock = 8  ← 사실 원래는 9가 되었어야 하는데…
```

이게 바로 **Lost Update(갱신 손실)** 문제다.
MVCC는 이를 자동으로 해결해주지 못한다.

그래서 다음을 해야 한다.

### UPDATE 시 → 배타적 락 필요

### SELECT 후 UPDATE 흐름 → SELECT FOR UPDATE 필요

---

# 5. Undo Log는 “읽기용”, 락은 “쓰기용”

정확히 나누면 이렇게 된다.

### Undo Log

* MVCC 구현에 사용
* 과거 버전을 보관
* SELECT 시 “읽는 순간의 일관성”을 제공
* 읽기 작업을 위한 기술

### Lock

* 쓰기 충돌 방지
* 한 행을 하나의 트랜잭션이 독점하게 함
* UPDATE/DELETE가 정확하게 일어나도록 보장
* 쓰기 작업을 위한 기술

둘은 목적이 전혀 다르며 상호보완적이다.

---

# 6. 실제 요약으로 정리

MVCC가 있어도 락은 필요하다.

### MVCC가 해결하는 문제

* SELECT 간의 충돌 없음
* SELECT와 UPDATE 간의 충돌 최소화
  (조회는 락을 안 걸고 과거 버전을 보면 됨)

### MVCC가 해결 못하는 문제

* UPDATE vs UPDATE 충돌
* SELECT 후 UPDATE 시 Lost Update 문제
* 재고/카운트 등 "정확한 값"이 요구되는 비즈니스 로직

그래서 **“조회 후 수정” 작업은 항상 락을 써야 한다.**
즉, SELECT FOR UPDATE.

---

원하면 다음도 설명해줄게.

* MySQL InnoDB의 MVCC 구조 (undo log, Read View)
* Repeatable Read가 어떻게 구현되는지
* UPDATE 시 실제 락 흐름 정리
* MVCC + Lock이 동시에 발생하는 시나리오 그림으로 설명

어디까지 더 깊게 알고 싶어?

결론부터 말하면,
**SELECT FOR UPDATE가 걸린 행은 ‘쓰기(UPDATE·DELETE·FOR UPDATE 형태의 조회)’는 막지만, 일반 SELECT(읽기)는 대부분 막지 않는다.**
단, DBMS별로 차이가 조금 있다.

아래를 차근차근 설명해줄게.

---

# 1. 일반 SELECT는 막지 않는다 (MVCC 덕분)

SELECT FOR UPDATE는 **배타적 행 락(row-level exclusive lock)** 을 건다.
이 락은 다음을 의미한다.

> “이 행을 수정하는 권한은 내가 독점한다.”

하지만 과거 버전(undo)을 읽을 수 있게 하는 MVCC 때문에
다른 세션은 아래처럼 **단순 SELECT는 정상적으로 가능**하다.

```sql
SELECT * FROM table WHERE id = 1;
```

→ 이 SELECT는 잠긴 현재 버전이 아니라
**스냅샷(과거 버전)을 읽기 때문에 락 대기 없이 바로 결과를 받는다.**

즉,

* SELECT FOR UPDATE → 쓰기 충돌 방지
* MVCC → 읽기에는 영향 없음

이 두 기술이 함께 동작한다.

---

# 2. 어떤 쿼리가 막히는가?

SELECT FOR UPDATE가 잡힌 행에 대해 아래 쿼리들은 대기 또는 충돌을 일으킨다.

| 쿼리 유형                           | 막힘 여부        |
| ------------------------------- | ------------ |
| 단순 SELECT                       | **막히지 않음**   |
| SELECT … FOR UPDATE             | **막힘**       |
| SELECT … FOR SHARE (PostgreSQL) | **막힘**       |
| UPDATE                          | **막힘**       |
| DELETE                          | **막힘**       |
| INSERT (동일 키 충돌 시)              | **상황 따라 막힘** |

즉,

* **읽기는 허용**
* **쓰기는 차단**

이게 핵심이다.

---

# 3. DBMS별 차이

### PostgreSQL

* SELECT는 절대 막히지 않는다.
* SELECT FOR UPDATE나 UPDATE만 기다리게 된다.

### MySQL(InnoDB)

* 기본적으로 SELECT는 막히지 않는다.
* 하지만 **Gap Lock / Next-Key Lock** 상황에서는
  범위 조회가 걸릴 수도 있다. (주로 REPEATABLE READ에서 발생)

예: 다음처럼 인덱스 범위에 락이 걸려 있으면

```sql
SELECT * FROM table WHERE age BETWEEN 20 AND 30;
```

이걸 다른 세션에서 같은 범위로 SELECT FOR UPDATE 하면 막히거나,
상황에 따라 단순 SELECT도 영향을 받을 수 있다.
그러나 “행 단위”로는 SELECT는 대부분 정상 작동한다.

---

# 4. 왜 읽기는 허용되고 쓰기는 막히는가?

이유는 명확하다.

### MVCC가 읽는 값을 고정해주기 때문

다른 트랜잭션이 SELECT를 하더라도

* 현재 버전이 락으로 묶여 있어도
* 과거 스냅샷 버전을 읽으면 됨

즉, 읽기 작업은 충돌하지 않는다.

### 반대로, 쓰기는 충돌할 수 있기 때문

두 트랜잭션이 같은 행을 동시에 업데이트하면

* Lost Update
* Dirty Write

같은 심각한 정합성 문제가 발생한다.

그래서 **쓰기 작업은 반드시 직렬화되어야 한다.**

---

# 5. 정리

다시 핵심만 정리하면:

1. **SELECT FOR UPDATE는 쓰기 작업만 막는다.**
2. **단순 SELECT는 보통 전혀 막지 않는다 (MVCC 때문에).**
3. MySQL은 인덱스 구조에 따라 범위 잠금이 생겨 예외적인 경우가 있을 수 있다.
4. SELECT FOR UPDATE는 “조회 후 수정” 시 정확성을 확보하기 위한 락이다.

---

원하면 다음도 설명해줄게.

* MySQL에서 gap lock 때문에 SELECT가 막히는 실제 발생 조건
* PostgreSQL의 key-share / share / update 락 비교
* MVCC 스냅샷 읽기 동작을 그림으로 설명
* UPDATE 시 어떤 잠금이 걸리며 어떤 순서로 동작하는지 트랜잭션 타임라인으로 보여주기

어떤 부분을 더 깊게 알고 싶어?

맞다.
**SELECT FOR UPDATE는 “지금 당장 UPDATE를 수행하는 것”이 아니라,
“곧 UPDATE를 수행할 것이니 이 행을 내가 독점한다”라는 선언(예고)에 가깝다.**

하지만 이 “예고”의 효과는 **굉장히 실제적**이고,
트랜잭션이 종료될 때까지 다른 세션의 쓰기 작업을 막는다.

아래에서 정확히 어떻게 동작하는지 설명할게.

---

# 1. SELECT FOR UPDATE는 “락만 거는 것”

예:

```sql
BEGIN;

SELECT * FROM item WHERE id = 1 FOR UPDATE;
```

이 쿼리는 다음 두 일을 한다.

1. **id = 1 행을 읽는다.**
2. **그 행에 배타적 잠금(Exclusive Lock)을 건다.**

그 외에는 어떤 변경도 일어나지 않는다.

즉, 아직 UPDATE는 없다.
데이터는 바뀌지 않는다.

**하지만 이 락 때문에 다른 트랜잭션이 아래 작업을 못한다.**

* UPDATE item …
* DELETE FROM item …
* SELECT … FOR UPDATE
* SELECT … FOR SHARE(PostgreSQL)

즉,

> “내가 이 행을 수정할 권리를 확보한 상태”

가 된다.

---

# 2. 그럼 왜 굳이 UPDATE 전에 SELECT FOR UPDATE를 하는가?

목적은 단 하나.

> **“지금부터 이 행은 내가 다룰 것이니, 다른 트랜잭션은 손대지 마라.”**

즉, 수정 권한을 선점해두는 것.

대표적인 패턴은 다음과 같다.

```sql
BEGIN;

SELECT stock FROM item WHERE id = 1 FOR UPDATE;

-- 비즈니스 로직 계산
-- ex) stock - 1

UPDATE item SET stock = stock - 1 WHERE id = 1;

COMMIT;
```

이렇게 하면 재고 감소 로직이 동시성 문제 없이 안전하게 수행된다.

---

# 3. SELECT FOR UPDATE 없이 UPDATE만 하면 안 되나?

가능은 하다.

```sql
UPDATE item SET stock = stock - 1 WHERE id = 1;
```

이 쿼리 자체가 행 락을 건다.

하지만 이런 경우는 **UPDATE를 바로 실행할 수 있을 때만** 잘 작동한다.

문제가 되는 경우는:

* 재고를 먼저 읽어서 검증한 뒤 계산해야 하거나
* 여러 행을 읽어 조건을 검사해야 하고
* 그 조건이 맞으면 나중에 UPDATE를 해야 할 때

즉, **조회 후 수정(read → compute → write)** 로직에서
락을 먼저 잡아 놓지 않으면 경쟁 상태가 생긴다.

그래서 SELECT FOR UPDATE가 필요하다.

---

# 4. SELECT FOR UPDATE는 왜 “예고”가 아니라 실제 효과가 있나?

“예고”처럼 보이지만 실제 동작은 매우 무겁다.
DB는 그 순간 다음과 같은 실제 처리를 한다.

1. 해당 행의 레코드 락을 만듦
2. “이 락은 현재 트랜잭션이 끝날 때까지 유지”라고 등록
3. 다른 트랜잭션이 쓰기 시도하면 **대기(block)** 시킴

즉,
데이터 변경은 아니지만 **락 자체는 실질적인 동작**이다.

---

# 5. 정리

### SELECT FOR UPDATE의 본질

* “내가 곧 이 행을 수정할 것이다”라는 선언
* 실제 UPDATE는 하지 않음
* 하지만 해당 행을 **독점적으로 잠가** 다른 트랜잭션의 수정 접근을 차단
* 트랜잭션이 끝나야만 락이 풀림
* read → compute → write 흐름에서 동시성 정합성을 위해 필수적

SELECT FOR UPDATE는 단순한 “예고”가 아니라
**강력한 동시성 제어 장치**라고 보는 것이 정확하다.

---

원하면 다음도 더 설명해줄게.

* SELECT FOR UPDATE 이후 실제 UPDATE 시 내부적으로 락이 어떻게 재사용되는지
* 락이 걸린 행을 다른 세션에서 SELECT하면 어떤 버전을 읽게 되는지(MVCC 관점)
* SELECT FOR UPDATE가 deadlock을 어떻게 유발할 수 있는지 시나리오
* MySQL vs PostgreSQL의 SELECT FOR UPDATE 차이

어떤 쪽이 더 궁금해?

`TransactionManager`, `DataSource`, `DriverManager`는 모두 “DB에 연결하고 트랜잭션을 다루는 과정”에서 등장하지만, **역할과 계층이 완전히 다르다.**
이 셋은 서로를 대체하는 개념이 아니고, **각자 다른 목적을 수행한다.**

아래에서 차이를 가장 명확하게 설명해줄게.

---

# 1. DriverManager — JDBC 드라이버와 연결(Connection)을 직접 생성

DriverManager는 **가장 낮은 레벨의 JDBC 도구**다.

### 역할

* JDBC 드라이버를 통해 **새로운 Connection을 직접 만들어 반환**
* 트랜잭션 관리 없음
* 커넥션 풀 없음

### 코드 예

```java
Connection conn =
    DriverManager.getConnection("jdbc:mysql://...", "user", "password");
```

### 특징

* 매번 DB 연결을 직접 생성 → 비용 큼
* 애플리케이션 서버 환경에서는 거의 사용하지 않음
* 커넥션 풀링이 없어 성능에 치명적

→ **초기 JDBC 시절 방식**이며, 지금은 실무 대부분에서는 쓰지 않는다.

---

# 2. DataSource — 커넥션을 “관리된 방식”으로 제공하는 표준 인터페이스

DataSource는 DriverManager와 달리 **Connection을 직접 만들지 않음**.
대신 **커넥션 풀을 통해 Connection을 재사용하거나, 관리된 Connection을 제공**하는 역할이다.

### 역할

* 커넥션 풀 유지
* 커넥션을 효율적으로 재사용(return)
* DriverManager보다 성능 우수
* JNDI 등 애플리케이션 서버에서 표준처럼 사용

### 코드 예

```java
DataSource ds = ...;  // HikariCP, DBCP 등
Connection conn = ds.getConnection();
```

### 특징

* 스프링, 톰캣, 애플리케이션 서버에서 표준
* HikariCP 같은 고성능 pool 구현체 사용
* 트랜잭션 관리를 내장하지는 않음 (트랜잭션은 Connection이 담당)

→ DB 연결을 “효율적이고 표준적으로 관리하는” 계층

---

# 3. TransactionManager — 트랜잭션 흐름을 고수준에서 제어

TransactionManager는 JDBC의 저수준 객체가 아니다.
스프링에서 **트랜잭션을 추상화하여 관리**하기 위한 고수준 컴포넌트다.

### 역할

* 트랜잭션 시작(begin)
* Connection 가져오기
* commit/rollback
* 여러 자원(DataSource, JPA, JMS 등)을 동시에 포함한 트랜잭션 관리
* 스레드 로컬에 Connection을 바인딩해 트랜잭션을 일관되게 유지

예:

```java
PlatformTransactionManager tx = ...;

TransactionStatus status = tx.getTransaction(new DefaultTransactionDefinition());
try {
    // DB 작업
    tx.commit(status);
} catch(Exception e) {
    tx.rollback(status);
}
```

### 특징

* DataSource로부터 Connection을 획득해 트랜잭션 경계를 관리
* @Transactional을 통해 선언적 트랜잭션 관리 가능
* JDBC뿐 아니라 JPA, Hibernate, MyBatis 등 다양한 기술을 동일 방식으로 묶어줌

→ 스프링의 트랜잭션 라이프사이클 제어 담당자

---

# 4. 이 셋의 관계를 그림으로 정리

행동이 일어나는 순서를 계층 구조로 보면 이렇게 된다.

```
애플리케이션 코드
       ↓
 TransactionManager  ← 트랜잭션 관리
       ↓
  DataSource         ← 커넥션 풀, 커넥션 대여
       ↓
 JDBC Driver         ← DB 통신
       ↓
   Database
```

DriverManager는 여기서 빠진다.
DataSource가 등장하면서 사실상 하위호환처럼 되었기 때문.

---

# 5. 한 줄 요약

| 구성                 | 레벨       | 역할                    | 실무 사용 여부  |
| ------------------ | -------- | --------------------- | --------- |
| DriverManager      | 가장 낮은 레벨 | Connection을 직접 생성     | 거의 사용 안 함 |
| DataSource         | 중간 레벨    | 커넥션 풀 + Connection 제공 | 거의 항상 사용  |
| TransactionManager | 가장 높은 레벨 | 트랜잭션 시작/종료 제어         | 스프링에서 핵심  |

---

# 6. 궁금증 해결 Q&A

### Q1: DataSource는 트랜잭션을 관리하지 않는데, 어떻게 트랜잭션이 유지돼?

→ Connection 객체가 트랜잭션을 유지한다.
TransactionManager는 동일 Connection을 계속 사용하도록 제어한다.

### Q2: DriverManager로도 트랜잭션 만들 수 있지 않나?

→ 가능하다.
하지만 커넥션 풀도 없고 트랜잭션 경계를 코드로 직접 써야 해서 비효율적.

### Q3: 왜 애플리케이션에서 DriverManager를 거의 버렸나?

→ DB 연결은 매우 비싼 작업이기 때문에 **풀링(DataSource)**이 필수다.

### Q4: TransactionManager 없는 JDBC는 어떻게 동작해?

→ 직접 Connection.commit / rollback 호출해야 한다.
선언적 트랜잭션을 쓸 수 없다.

---

원하면 다음도 설명해줄게.

* @Transactional이 실제로 어떤 순서로 Connection을 가져오고 반납하는지
* DataSourceTransactionManager 내부 동작
* HikariCP 같은 커넥션 풀의 구조
* 캐시된 Connection이 트랜잭션 경계를 넘어가면 안 되는 이유(스레드 로컬 동작)

어떤 부분이 더 궁금해?

그렇다.
**스프링의 TransactionManager, @Transactional, DataSource, Connection 관리 메커니즘은 ThreadLocal과 매우 깊은 관련이 있다.**
트랜잭션이 “요청 단위로 일관되게 유지되는 것”도 ThreadLocal 덕분이라고 해도 과언이 아니다.

아래에서 왜 ThreadLocal이 필요한지, 어떤 역할을 하는지 단계별로 설명할게.

---

# 1. 왜 ThreadLocal이 필요한가?

트랜잭션은 다음과 같은 요구조건이 있다.

1. 트랜잭션이 시작되면 **같은 스레드 내에서 모든 DB 작업이 동일한 Connection을 사용**해야 한다.
2. 트랜잭션이 끝나면 **그 Connection을 커밋·롤백하고 반납**해야 한다.
3. 트랜잭션이 진행되는 동안에는 **스프링 내부 여러 계층(JPA, JDBC, MyBatis 등)이 일관된 Connection을 공유**해야 한다.

즉,

> “하나의 트랜잭션 범위에서 동일한 Connection이 유지되어야 한다.”

이를 구현하기 위해 스레드 단위로 Connection을 저장할 필요가 있다.
이때 사용하는 것이 ThreadLocal이다.

---

# 2. TransactionManager 내부에서 ThreadLocal은 어떻게 쓰일까?

## 흐름을 아주 단순화하면 이렇게 된다.

### 1) @Transactional 진입

스프링 AOP가 트랜잭션을 시작할 시점에 다음을 한다.

* DataSource에서 Connection을 하나 가져온다.
* 그 Connection을 ThreadLocal에 저장한다.

  ```java
  ThreadLocal<Connection> txConnection = ...;
  txConnection.set(conn);
  ```

### 2) 트랜잭션 범위 내의 모든 JDBC/JPA 호출

JDBC든 JPA든 내부적으로 Connection이 필요할 때
스프링은 ThreadLocal에서 같은 Connection을 꺼내 사용한다.

즉,

* 같은 스레드 → 같은 트랜잭션 → 같은 Connection

이 보장된다.

### 3) @Transactional 메서드 종료

스프링이 트랜잭션을 종료하며 다음 실행:

* Connection.commit() 또는 rollback()
* ThreadLocal에서 Connection 삭제 (`remove()`)
* 커넥션 풀로 반납

---

# 3. 왜 ThreadLocal이 아니면 안 될까?

예를 들어, 웹 요청 하나를 처리하는 동안 DAO, Repository, Service가 서로 여러 가지 경로(JDBC, JPA, 템플릿 등)로 DB에 접근한다고 해보자.

만약 ThreadLocal이 없다면?

* DAO에서 DataSource.getConnection() 호출 → connA
* Repository에서 다시 getConnection() → connB
* JPA가 내부적으로 다른 connC 사용

이런 식으로 서로 다른 Connection을 쓰게 된다.

그러면 문제가 생긴다.

* 트랜잭션 경계가 깨짐
* connA는 commit, connB는 rollback 등 일관성이 없음
* 하나의 논리적 트랜잭션이 여러 물리 트랜잭션으로 나뉘게 됨

즉, 트랜잭션이 완전히 무너진다.

그래서 스프링은 다음 원칙을 강하게 보장한다.

> **트랜잭션 범위 내에서 동일 스레드가 동일 Connection을 사용하도록 강제한다.**

그리고 그 강제 수단이 바로 ThreadLocal이다.

---

# 4. ThreadLocal을 쓰지 않는 다른 방법은 없나?

이론적으로는 “컨텍스트 객체를 모든 메서드에 전달”하는 방식도 가능하지만,

* 메서드에 매번 전달해야 하므로 코드 복잡도 증가
* 스프링 같은 AOP 기반 선언적 트랜잭션 관리가 불가능

따라서 실무에서는 ThreadLocal이 거의 유일한 방식이다.

---

# 5. JPA에서도 ThreadLocal이 중요할까?

그렇다.

엔티티 매니저(EntityManager)도 스레드 바운드(Thread-bound)다.

* 스프링은 @PersistenceContext로 주입된 EntityManager를 ThreadLocal 기반 프록시로 감쌌다.
* 트랜잭션마다 동일한 EntityManager가 유지된다.
* 내부적으로 동일 Connection을 공유한다.

결국 **JPA도 ThreadLocal 없이 정상적인 트랜잭션을 유지할 수 없다.**

---

# 6. 결국 세 구성 요소의 관계는 이렇게 된다

```
@Transactional
     ↓  (트랜잭션 시작)
TransactionManager
     ↓  (Connection 획득)
DataSource
     ↓  (Connection 반환)
ThreadLocal에 저장
     ↓
DAO / Repository / JPA Configuration / Template
(필요할 때 ThreadLocal의 Connection 사용)
     ↓
메서드 종료 → commit/rollback → ThreadLocal 삭제
```

즉,

### **트랜잭션을 구성하는 실제 핵심 도구는 ThreadLocal이다.**

---

# 7. 핵심 요약

* TransactionManager는 트랜잭션 경계를 관리한다.
* DataSource는 Connection을 제공한다.
* ThreadLocal은 “현재 스레드의 트랜잭션에 해당하는 Connection”을 저장한다.
* @Transactional이 가능한 이유도 ThreadLocal 때문이다.
* 트랜잭션 동안 여러 계층이 동일한 Connection을 사용하는 것도 ThreadLocal 덕분이다.

---

원하면 다음도 설명해줄게.

* 스프링에서 ThreadLocal을 사용하는 실제 클래스 이름
* JPA EntityManager와 ThreadLocal의 정확한 관계
* 멀티스레드 환경에서 ThreadLocal 누수(memory leak)가 왜 위험한지
* 트랜잭션 전파(propagation)와 ThreadLocal의 관계

어떤 부분이 더 알고 싶어?

좋은 지적이다.
**TransactionManager.getTransaction()는 분명 트랜잭션을 시작하면서 DataSource에서 Connection을 가져오는데,
정작 그 반환값은 Connection이 아니라 TransactionStatus다.**
왜 이런 구조가 되었는지 정확히 설명해줄게.

---

# 1. TransactionManager는 “Connection 제공자”가 아니다

TransactionManager의 역할은 다음 두 가지다.

1. **트랜잭션 경계를 관리**한다 (시작/커밋/롤백).
2. **스레드에 트랜잭션 컨텍스트를 바인딩**한다 (→ ThreadLocal 활용).

즉, TransactionManager는 **Connection을 필요로 하지만 Connection을 직접 외부로 제공하는 API가 아니다.**

왜냐하면:

* Connection을 외부로 직접 전달하면 트랜잭션의 일관성이 깨질 수 있다.
* 여러 계층(Repository, DAO, JPA, MyBatis 등)이 같은 트랜잭션에서 같은 Connection을 공유해야 하는데,
  외부에 Connection을 넘겨주면 모두가 이를 일관되게 처리하기 어렵다.

그래서 Connection은 **트랜잭션 스코프 내부에서만 관리되는 내부 자원**이다.

---

# 2. getTransaction()이 반환하는 것은 “트랜잭션 상태 정보”

`getTransaction()`의 반환 타입은 **TransactionStatus**이다.

예:

```java
TransactionStatus status =
    txManager.getTransaction(new DefaultTransactionDefinition());
```

TransactionStatus는 다음과 같은 “트랜잭션의 상태 메타데이터”만 제공한다.

* 새 트랜잭션인가?
* 기존 트랜잭션에 참여했는가?
* 롤백 전용으로 설정되었는가?
* 트랜잭션이 이미 완료되었는가?

즉, 트랜잭션을 조작하는 데 필요한 “컨트롤 정보”일 뿐이다.
여기에는 **Connection 자체가 포함되지 않는다.**

Connection은 ThreadLocal에 숨겨져 있기 때문이다.

---

# 3. 그럼 진짜 Connection은 어디 있나?

여기서 ThreadLocal이 등장한다.

트랜잭션이 시작될 때:

1. TransactionManager는 DataSource에서 Connection을 뽑아온다.
2. 이 Connection을 TransactionSynchronizationManager 내부의 ThreadLocal에 바인딩한다.

예:

```java
TransactionSynchronizationManager.bindResource(dataSource, connectionHolder);
```

이렇게 되면:

* 현재 스레드에서 실행되는 스프링 기반 JDBC/JPA/MyBatis는
  **DataSourceUtils.getConnection()**을 호출해
  ThreadLocal에서 같은 Connection을 가져온다.

**따라서 트랜잭션 안에서 모든 DB 접근은 같은 Connection을 자연스럽게 공유하게 된다.**

---

# 4. 왜 Connection을 반환하지 않는 것이 더 좋은가?

## 4.1 Connection을 외부로 넘기면 트랜잭션 일관성이 깨진다

예를 들어 getTransaction()이 Connection을 돌려준다고 상상해보자.

```java
Connection conn = txManager.getTransaction(...);
```

그럼 개발자가 실수로 다음과 같은 일을 할 수 있다.

* 중간에 conn.close() 호출 → 트랜잭션 붕괴
* 여러 스레드에서 conn을 공유 → 트랜잭션 혼선
* JPA와 JDBC가 서로 다른 Connection을 사용 → 데이터 불일치

스프링은 이것을 막기 위해 Connection을 **절대 외부에 노출하지 않는다.**

---

## 4.2 트랜잭션 전파(propagation)를 처리할 수 없다

@Transactional이 중첩 호출될 때 다음 로직이 필요하다.

* 이미 트랜잭션이 있다면 새로운 Connection을 만들지 않고 기존 Connection을 공유해야 한다.
* REQUIRED, REQUIRES_NEW, MANDATORY 등 전파 규칙이 적용되어야 한다.

이런 로직은 **Connection 객체만으로는 표현 불가능**하다.

따라서 TransactionManager는 Connection이 아닌
"트랜잭션 상태 + 정책 적용 결과"를 나타내는 **TransactionStatus**를 반환해야 한다.

---

## 4.3 복수 자원 트랜잭션(JTA 등)을 지원하기 위해서

스프링 트랜잭션 API는 JDBC 하나만 관리할 수 있는 구조가 아니다.

* 여러 DataSource
* JMS
* JPA + JDBC 복합 트랜잭션
* 글로벌 트랜잭션 (XA)

이런 “여러 자원”의 트랜잭션 상태를 모두 관리하려면
반환값이 Connection일 수 없다.

TransactionStatus는 **트랜잭션 추상화 계층**을 유지하기 위한 메타 객체이다.

---

# 5. 요약 정리

| 요소                                     | 역할                                 | 외부 노출 여부              |
| -------------------------------------- | ---------------------------------- | --------------------- |
| **Connection (JDBC 레벨)**               | 실제 DB 통신, 트랜잭션의 물리적 단위             | 외부에 직접 노출 X           |
| **ThreadLocal에 바인딩된 ConnectionHolder** | 동일 스레드에서 Connection 공유             | 스프링 내부 전용             |
| **TransactionStatus**                  | 트랜잭션의 논리적 상태, 제어용                  | getTransaction()의 반환값 |
| **TransactionManager**                 | 트랜잭션 경계 제어 (begin/commit/rollback) | Connection을 숨김        |

---

# 6. 한 줄로 완성

> **TransactionManager.getTransaction()은 Connection이 아니라 트랜잭션 상태(TransactionStatus)를 반환한다.
> Connection은 ThreadLocal에 숨겨져 관리되며, JDBC/JPA 계층이 자동으로 그 Connection을 찾기 때문.**

---

원한다면 다음도 더 풀어서 설명해줄 수 있다.

* `TransactionSynchronizationManager` 내부 구조
* DataSourceUtils.getConnection()이 ThreadLocal에서 Connection을 찾는 과정
* JDBC, MyBatis, JPA가 동일 Connection을 공유하는 실제 호출 흐름
* 트랜잭션 전파 규칙별로 ThreadLocal이 어떻게 움직이는지

어느 정도 더 깊게 들어가고 싶어?

좋은 질문이다.
핵심은 다음 한 줄로 요약할 수 있다.

TransactionManager는 “트랜잭션의 상태”를 반환하지, 실제 Connection을 반환하지 않는다.
실제 Connection은 ThreadLocal에 숨겨 두고 스프링 내부에서 관리한다.

이제 왜 이런 구조가 되었는지 단계별로 풀어서 설명해줄게.

1. TransactionManager.getTransaction()은 왜 Connection을 반환하지 않을까?

스프링의 트랜잭션 추상화 철학 때문인데, 핵심 목표는 다음 두 가지다.

여러 기술(JDBC, JPA, Hibernate, JMS 등)을 “하나의 공통 인터페이스”로 트랜잭션 처리하기

애플리케이션 코드가 커넥션(Connection)을 직접 다루지 않도록 하기 (리소스 관리 분리)

Connection은 오직 JDBC에만 있는 개념이다.
하지만 스프링 트랜잭션은 JDBC만 지원하는 것이 아니다.

그래서 트랜잭션을 표현하는 객체는 Connection이 아닌 TransactionStatus다.

2. 그럼 실제 Connection은 어디에 있는가?

TransactionManager는 getTransaction() 호출 시 다음을 수행한다.

DataSource.getConnection()을 호출해 Connection을 획득

그 Connection을 ThreadLocal에 저장

트랜잭션을 시작(필요하면 setAutoCommit(false))

개발자에게는 TransactionStatus만 돌려줌

즉, 구조는 이렇게 되어 있다.

개발자 코드 ←– TransactionStatus
↑
TransactionManager
↓
DataSource → Connection → ThreadLocal에 저장


그래서 개발자는 Connection을 직접 볼 수 없지만,
스프링 내부의 모든 DB 작업은 동일 ThreadLocal의 Connection을 사용한다.

3. 왜 개발자에게 Connection을 주지 않나?

만약 트랜잭션 시작 시 개발자에게 Connection을 직접 반환해버리면 다음 문제가 생긴다.

문제 1) 개발자가 직접 close() 해버릴 수 있음

트랜잭션 관리가 깨진다.

문제 2) JPA는 EntityManager로 트랜잭션을 관리하는데 Connection은 필요 없음

기술별로 인터페이스가 달라진다.
스프링의 “기술 독립적” 추상화가 불가능해진다.

문제 3) 여러 자원을 하나의 트랜잭션으로 묶는 JTA가 불가능해짐

JDBC Connection만 기준으로 트랜잭션을 표현할 수 없다.

그래서 스프링은 Transaction = 상태 객체(TransactionStatus) 라는 고수준 개념으로 추상화했다.

4. TransactionStatus는 무엇을 포함하나?

TransactionStatus는 다음 메타 정보를 담고 있다.

지금 트랜잭션이 새로 시작된 것인지

이미 존재하는 트랜잭션에 참여 중인지

롤백 전용인지 (setRollbackOnly)

완료되었는지 여부

즉, 트랜잭션 경계(boundary)에 대한 상태만 제공한다.

Connection 같은 저수준 자원은 절대 개발자에게 노출하지 않는다.

5. 실제 Connection을 사용하는 주체는 누구인가?

다음의 컴포넌트들이 Connection을 사용한다.

JdbcTemplate

MyBatis

Hibernate / JPA (EntityManager가 내부적으로 JDBC Connection을 사용함)

이들은 모두 이런 흐름으로 Connection을 얻는다.

아래 내용은 스프링 기반 애플리케이션에서
JdbcTemplate, MyBatis, JPA(Hibernate)가 **실제 JDBC Connection을 어떻게 획득하고 공유하는지**를 가장 정확하게 설명하는 핵심 흐름이다.

**결론을 먼저 말하면:
이들 프레임워크는 모두 “직접 DataSource에서 새 Connection을 뽑지 않는다”.
항상 스프링이 ThreadLocal에 바인딩해둔 Connection을 가져와 사용한다.**

이 과정을 단계별로 설명할게.

---

# 1. 핵심: 모든 컴포넌트는 DataSourceUtils.getConnection()을 사용한다

스프링은 “트랜잭션 범위 내에서 동일 Connection을 사용해야 한다”는 목표를 위해
**DataSourceUtils.getConnection()** 이라는 유틸리티를 제공한다.

이 메서드는 다음 두 동작을 한다.

### 1) ThreadLocal(TransactionSynchronizationManager)에 이미 Connection이 있으면 그걸 반환

→ 트랜잭션이 이미 시작된 경우
→ 새로운 커넥션을 만들면 안 됨

### 2) 없으면 DataSource에서 새 Connection을 얻고 ThreadLocal에 바인딩

→ 트랜잭션이 없고 그냥 단순 DB 호출일 때

즉,
**Connection 접근은 항상 이 통로를 거치기 때문에,
스프링이 트랜잭션을 제어할 수 있게 되는 것이다.**

---

# 2. JdbcTemplate이 Connection을 얻는 과정

JdbcTemplate 내부 구조를 따라가보면:

```java
Connection con = DataSourceUtils.getConnection(dataSource);
```

이렇게 되어 있다.

JdbcTemplate 자체가 DataSource에서 직접 `getConnection()`을 호출하지 않는다.

JdbcTemplate은 항상 DataSourceUtils를 통해 호출하기 때문에:

* 트랜잭션 중이면 같은 Connection을 재사용
* 트랜잭션 밖이면 새로운 Connection을 생성 후 자동으로 닫음

→ 그래서 JdbcTemplate, Repository, DAO 등이 트랜잭션에서 일관된 Connection을 공유한다.

---

# 3. MyBatis가 Connection을 얻는 과정

MyBatis-Spring 연동 시, MyBatis는 다음 과정으로 Connection을 얻는다.

## ① SqlSessionTemplate 사용

스프링은 MyBatis의 SqlSession을 래핑한 `SqlSessionTemplate`을 제공한다.

SqlSessionTemplate 내부에서 SQL 실행 시:

```java
Connection con = DataSourceUtils.getConnection(dataSource);
```

즉, MyBatis도 DataSourceUtils를 통해 ThreadLocal에 저장된 Connection을 가져온다.

## 왜 중요한가?

MyBatis도 JDBC 기반이므로,

* UPDATE/SELECT/INSERT 같은 명령을 실행할 때
* JPA나 JdbcTemplate과 동일한 Connection을 사용하게 된다.

그래서 같은 @Transactional 안에서 MyBatis와 JPA를 섞어도
논리적으로 하나의 트랜잭션으로 묶인다.

---

# 4. JPA/Hibernate가 Connection을 얻는 과정

JPA는 JDBC를 직접 사용하지 않지만, 내부적으로 결국 JDBC Connection이 필요하다.

흐름은 이렇게 된다:

## ① EntityManager는 트랜잭션 참여 시 Connection을 필요로 함

스프링은 EntityManager를 프록시로 감싸고 있다.
트랜잭션 시작 시 이 프록시는 다음을 실행한다.

```java
Connection con = DataSourceUtils.getConnection(dataSource);
```

이 Connection은 Hibernate Session에게 전달되고
Hibernate는 이를 통해 PreparedStatement를 생성한다.

## ② EntityManager는 동일 스레드에서 항상 같은 Connection 사용

스프링이 TransactionSynchronizationManager에 ConnectionHolder를 저장하기 때문에:

* EntityManager
* JPQL
* Native Query
* Hibernate 내부 flush
* 배치 업데이트

이 모든 것이 **트랜잭션 동안 같은 Connection을 사용**한다.

그래서 JPA에서도 JDBC와 동일한 트랜잭션 정합성이 유지된다.

---

# 5. 전체 흐름을 그림으로 정리

트랜잭션이 시작되면:

```
@Transactional
   ↓
TransactionManager
   ↓
DataSource에서 Connection 획득
   ↓
ConnectionHolder를 ThreadLocal에 저장
   ↓
JPA / JdbcTemplate / MyBatis
   ↓
DataSourceUtils.getConnection()
   ↓
ThreadLocal에서 같은 Connection을 가져옴
```

트랜잭션이 끝나면:

```
commit or rollback
   ↓
ConnectionHolder 제거(ThreadLocal remove)
   ↓
Connection을 커넥션 풀로 반환
```

---

# 6. 왜 이런 구조가 필요한가?

이 구조를 쓰지 않으면 다음 문제가 발생한다.

### 1) 같은 트랜잭션 안에서 여러 Connection 사용 → 일관성 붕괴

### 2) JPA, MyBatis, JDBC가 서로 다른 Connection 사용 → 하나의 트랜잭션이 여러 개로 쪼개짐

### 3) 커밋 타이밍이 서로 달라짐 → 데이터 불일치

### 4) 선언적 트랜잭션(@Transactional)이 불가능해짐

→ 즉, 스프링의 트랜잭션 관리 자체가 근본적으로 불가능해진다.

---

# 7. 핵심 요약

| 컴포넌트          | Connection 획득 방법                                     | 트랜잭션 내 연결 공유? |
| ------------- | ---------------------------------------------------- | ------------- |
| JdbcTemplate  | DataSourceUtils.getConnection()                      | 예             |
| MyBatis       | SqlSessionTemplate → DataSourceUtils.getConnection() | 예             |
| JPA/Hibernate | EntityManager 프록시 → DataSourceUtils.getConnection()  | 예             |

그리고 이 세 가지가 모두 같은 Connection을 공유하게 되는 이유는:

1. TransactionManager가 DataSource에서 Connection을 가져오고
2. TransactionSynchronizationManager(ThreadLocal)에 저장하고
3. DataSourceUtils.getConnection()이 이를 꺼내쓰기 때문이다.

---

필요하다면 다음도 설명해줄 수 있어.

* DataSourceUtils 내부 코드 흐름
* TransactionSynchronizationManager의 자원 바인딩 구조
* JPA의 EntityManager가 Connection을 요청하는 시점
* Spring이 JPA EntityManager를 프록시로 감싸는 이유
* Connection을 직접 사용하면 트랜잭션이 깨지는 이유

어떤 걸 더 깊게 알고 싶어?
