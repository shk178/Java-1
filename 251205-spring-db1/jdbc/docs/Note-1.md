아래 코드는 **순수 JDBC(Java DataBase Connectivity)** 를 사용해서 **Member** 객체를 DB에 저장하는 기능을 가진 **MemberRepository** 클래스입니다.
H2를 쓰고 있다는 점 외에, 코드가 실제로 어떤 일을 하는지 흐름 중심으로 설명해드릴게요.

---

## 전체 목적

`save(Member member)` 메서드는 **member 테이블에 새로운 레코드(회원 데이터)를 INSERT** 합니다.
즉, Member 객체 → DB row 로 변환하는 작업을 수행하는 저장소(repository) 역할입니다.

---

## 코드가 하는 일 단계별 정리

### 1. SQL 준비

```java
String sql = "insert into member (member_id, money) values (?, ?)";
```

DB에 넣을 SQL을 문자열로 작성합니다. `?`는 PreparedStatement가 나중에 값을 채우도록 하는 자리입니다.

---

### 2. DB 연결 생성

```java
con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
```

* DriverManager를 통해 DB(H2)에 연결(Connection) 객체를 얻습니다.
* Connection은 “DB랑 통신하는 통로”라고 보면 됩니다.

---

### 3. PreparedStatement 생성

```java
pstmt = con.prepareStatement(sql);
```

* SQL을 DB에 안전하게 전달하기 위한 객체입니다.
* SQL Injection 방지 효과가 있습니다.

---

### 4. SQL에 값 바인딩

```java
pstmt.setString(1, member.getMemberId());
pstmt.setInt(2, member.getMoney());
```

* 첫 번째 ?에는 memberId 값 넣기
* 두 번째 ?에는 money 값 넣기

---

### 5. SQL 실행

```java
pstmt.executeUpdate();
```

* INSERT, UPDATE, DELETE 같은 “데이터 변경 쿼리” 실행
* 실제로 DB에 한 줄이 저장됩니다.

---

### 6. Member 객체 반환

```java
return member;
```

DB 저장 후, 저장된 Member 객체를 그대로 돌려줍니다.

---

## 예외 처리

```java
} catch (SQLException e) {
    throw e; 
}
```

* SQLException이 발생하면 다시 던집니다.
* 즉, 여기서는 예외를 직접 처리하지 않고 상위 서비스 계층이 책임지게 합니다.

---

## 리소스 정리 (finally)

```java
close(con, pstmt, null);
```

`close()` 메서드는 Connection, Statement, ResultSet을 안전하게 닫는 역할을 합니다.

* DB 연결은 사용 후 반드시 닫아야 리소스 누수가 생기지 않습니다.

---

## 정리하자면

이 코드는 **JDBC 기반의 아주 기본적인 Repository 패턴 구현**입니다.

* DB 연결하기
* SQL 실행하기
* 자원(Connection/Statement/ResultSet) 닫기

이런 흐름을 가장 기초 수준에서 직접 다루는 형태이죠.

---

## 추가로 설명해줄 수 있는 내용

원하시면 다음도 설명 가능합니다.

* 이 코드의 문제점(예: 트랜잭션 처리 없음, 반복되는 코드 등)
* 더 좋은 구조(JdbcTemplate, JPA 등)로 개선하는 방법
* 테스트하는 방법
* H2 DB 설정 방식

궁금한 부분이 있다면 이어서 물어보세요.

아래에서 각 개념을 “실제로 내부에서 무슨 일이 일어나는지”에 가까운 수준으로 더 깊게 설명해 드릴게요.
(이모지를 쓰지 않는다는 점 기억했습니다.)

---

# 1. DriverManager를 통해 DB(H2)에 연결(Connection 객체 생성)

## DriverManager란 무엇인가

* JDBC는 여러 종류의 DB를 하나의 방식으로 다룰 수 있게 하는 표준입니다.
* DB 벤더(H2, MySQL, Oracle 등)는 JDBC 규격에 맞게 **Driver**를 제공합니다.
* `DriverManager`는 이러한 드라이버들을 관리하고, 개발자가 요청한 DB URL에 맞는 드라이버를 찾아 연결(Connection)을 만들어주는 역할을 합니다.

## 내부 흐름

1. 프로젝트에 H2 JDBC 드라이버가 포함되어 있음
2. `DriverManager.getConnection(URL, USERNAME, PASSWORD)` 호출
3. DriverManager는 URL을 보고 적절한 드라이버(H2 Driver)를 선택
4. H2 드라이버가 DB와 실제 연결을 수행
5. 연결이 성립하면 Connection 객체 반환

즉, **DriverManager는 DB 연결 요청을 드라이버에게 전달하고 결과를 돌려받는 중개자**입니다.

---

# 2. Connection 객체는 무엇인가 (DB와의 통신 통로)

Connection을 간단히 “통신 통로”라고 보면 되지만 더 정확히는 다음과 같습니다.

## Connection의 역할

1. DB 서버와 TCP/IP 소켓 연결 유지
2. SQL 실행을 위한 Statement/PreparedStatement 생성
3. 트랜잭션 관리 (commit, rollback)
4. 세션(Session) 유지 – DB와의 대화 상태를 지속

## 왜 Connection이 중요할까

* Connection이 없으면 DB와 어떠한 상호작용도 할 수 없다.
* Connection을 통해 실제 SQL이 DB에 전송되고 결과가 다시 프로그램으로 들어온다.

Connection을 열면 DB에서 자원(Session)을 하나 점유하게 됩니다.
따라서 반드시 닫아야 합니다.

---

# 3. PreparedStatement – SQL을 DB에 안전하게 전달하는 객체

Statement와 다르게 PreparedStatement는 다음 특징을 가집니다.

## (1) SQL을 미리 컴파일해둔다

DB는 SQL 문장을 받아 실행 계획을 세웁니다.
PreparedStatement는 다음 두 단계를 분리합니다.

* SQL 문장 구조는 고정
* 값만 나중에 바인딩

그래서 DB는 같은 구조의 SQL을 재사용할 수 있습니다.

그러나 H2나 MySQL 같은 DB에서는 이 성능 이점이 상황에 따라 미미할 수도 있습니다.

## (2) SQL Injection 방지

SQL에서 위험한 부분은 문자열을 직접 이어붙여 사용할 때입니다.

예시:
`"select * from member where id = '" + userInput + "'"`

악의적인 사용자가 다음을 넣으면
`a' or '1'='1`
전체 쿼리가 변형됩니다.

PreparedStatement는 이런 문제를 막습니다.

PreparedStatement는 값을 그냥 문자열로 넣지 않습니다.
바인딩된 값을 **문자열이 아닌 데이터로** 전달합니다.

즉, DB는 값 자체를 그대로 인식하며 SQL 문장의 일부로 오해하지 않습니다.

이 방식 때문에 SQL Injection이 원천 방지됩니다.

---

# 4. close() 메서드 – Connection, Statement, ResultSet을 닫아야 하는 이유

JDBC 자원은 OS 레벨 또는 DB 서버 레벨 자원을 점유합니다.

닫지 않으면 다음 문제가 발생합니다.

## (1) Connection 미닫기 → DB 커넥션 부족

DB는 동시 연결 수가 제한되어 있습니다.
보통 10개, 100개 등으로 제한됩니다.

닫지 않으면 연결이 계속 남아 결국 DB가 “더 이상 연결 불가” 상태가 됩니다.

## (2) PreparedStatement 미닫기 → 메모리 누수

PreparedStatement는 서버에서 캐싱 또는 메모리 자원을 사용합니다.
특히 대량 요청 시 누수가 가시화됩니다.

## (3) ResultSet 미닫기 → 커서(cursor) 누수

ResultSet은 DB에서 데이터를 스트리밍 형태로 가져오기도 합니다.
닫지 않으면 DB 서버에 커서가 남습니다.

## (4) finally에서 닫아야 하는 이유

예외가 발생하더라도 반드시 실행되기 때문입니다.

```java
finally {
    close(con, pstmt, null);
}
```

이 구조가 아니면 예외 상황에서 자원이 영구 누수될 위험이 있음.

---

# 5. close() 구현 방식 설명

코드:

```java
if (stmt != null) {
    try {
        stmt.close();
    } catch (SQLException e) {
        System.out.println(e);
    }
}
```

이 패턴은 매우 전형적이며 다음 원리를 따른다.

* 닫을 수 있는 객체인지 확인 (`stmt != null`)
* 닫을 때도 SQLException이 날 수 있기 때문에 개별 try-catch
* 하나가 실패해도 나머지는 계속 닫아야 함

만약 모든 close를 한 블록에서 처리하면 앞의 자원 닫는 데서 예외가 나면 뒤의 자원을 닫지 못하게 됩니다.
그래서 각각 독립적으로 닫도록 작성합니다.

---

# 정리

| 항목                | 설명                                    |
| ----------------- | ------------------------------------- |
| DriverManager     | 적절한 JDBC 드라이버를 선택해 DB 연결을 만들어주는 관리자   |
| Connection        | DB와 연결된 세션, SQL 실행의 기반, 트랜잭션 관리 담당    |
| PreparedStatement | SQL 구조 고정, 값 안전 바인딩, SQL Injection 방지 |
| close()           | 연결과 자원을 안정적으로 해제해 누수를 방지하는 필수 절차      |

---

원하신다면 다음도 설명 가능합니다.

* Connection Pool(HikariCP) 사용 시 이것들이 어떻게 달라지는지
* 트랜잭션이 Connection 단위로 어떻게 작동하는지
* JPA/Hibernate가 이 과정을 어떻게 자동화하는지
* 스프링 JdbcTemplate이 왜 더 편한지

궁금한 것을 자유롭게 물어보세요.

네, **현재 코드에서는 ResultSet을 전혀 사용하지 않습니다.**

이 코드는 **INSERT 쿼리만 실행하는 메서드**이기 때문에 결과 집합(ResultSet)이 존재하지 않습니다.

---

# 1. 왜 ResultSet이 없는가

`INSERT`, `UPDATE`, `DELETE` 같은 **데이터 변경 쿼리**는

* 몇 개의 행이 영향을 받았는지 정도만 알려주고
* 실제 데이터 행을 반환하지 않습니다.

그래서 다음 메서드를 사용합니다.

```java
pstmt.executeUpdate();
```

이 메서드는 **정수값(int)** 을 반환합니다.
(몇 개의 row가 영향을 받았는지)

반면, 결과가 있는 쿼리는 `SELECT`입니다.

예:

```java
ResultSet rs = pstmt.executeQuery();
```

---

# 2. 그래서 close(con, pstmt, null) 이라고 적혀 있는 이유

`save()` 메서드의 finally 블록에서

```java
close(con, pstmt, null);
```

이렇게 null을 넣은 것은 **ResultSet이 사용되지 않으므로 닫을 것도 없다는 의미**입니다.

close() 메서드는 내부에서 다음을 처리합니다.

* rs가 null이면 닫지 않음
* stmt가 null이 아니면 닫음
* con이 null이 아니면 닫음

즉,
**“이번 쿼리에서는 ResultSet 없어요.”**
라고 명시적으로 알려주는 형태입니다.

---

# 3. ResultSet은 언제 쓰게 되는가

SELECT 쿼리에서만 사용합니다.

예를 들어 findById 같은 메서드를 만들면 다음과 같이 됩니다.

```java
String sql = "select * from member where member_id = ?";
pstmt = con.prepareStatement(sql);
pstmt.setString(1, memberId);

ResultSet rs = pstmt.executeQuery();

if (rs.next()) {
    Member member = new Member(rs.getString("member_id"), rs.getInt("money"));
    return member;
} else {
    return null;
}
```

이런 경우 close는 다음과 같아야 합니다.

```java
close(con, pstmt, rs);
```

---

# 정리

* `save()` 메서드는 **INSERT**라서 ResultSet이 필요 없음
* ResultSet은 **SELECT 쿼리에서만** 사용
* 현재 close()에 null을 넣는 이유는 “없으니 닫을 필요 없음”이라는 의미

---

원하신다면

* findById, findAll 같은 SELECT 예제
* ResultSet cursors(커서) 구조 설명
* ResultSet을 잘못 닫았을 때 생기는 문제
  도 이어서 설명해드릴 수 있습니다.

아주 좋은 질문입니다.
지금 보고 있는 코드는 **스프링도 아니고 톰캣도 아니며**, 단순히 **자바(JDK)와 JDBC 표준**만을 사용합니다.
그래서 이 코드 안에 등장하는 객체들이 어떤 범주(인터페이스/구현체/IO/함수형 등)에 속하는지 자바 관점에서 구조적으로 설명드릴게요.

---

# 1. 이 코드들은 스프링과 관련이 있는가?

전혀 없습니다.

* 스프링이 제공하는 기능 없음
* 스프링 컨테이너 없음
* 트랜잭션 관리 없음
* 빈(bean)도 아님

그냥 자바만으로 DB에 접속하는 “순수 JDBC” 코드입니다.

스프링이 한다면 이런 반복 작업들을 대신 해주지만, **이 코드는 개발자가 JDBC API를 직접 쓰는 원시 스타일**입니다.

---

# 2. 톰캣과 관련 있는가?

이 코드도 톰캣과 무관합니다.

톰캣은 웹 서버/서블릿 컨테이너일 뿐 DB 접속과는 관계가 없습니다.
JDBC를 사용하는 코드는 **어디서든 실행 가능**합니다.

* 콘솔 애플리케이션
* 데스크탑 프로그램
* 톰캣 위에서 돌아가는 웹 애플리케이션
* 스프링부트 앱

이 코드 자체는 **DB에 접속하는 순수 자바 코드**일 뿐입니다.

---

# 3. 어떤 자바 객체들이 등장하고, 각각 어떤 성격을 가진 것인가?

아래는 JDBC 코드에서 등장하는 주요 객체들이 자바 관점에서 어디에 속하는지와 특징입니다.

---

## 3-1. DriverManager (클래스)

* `java.sql.DriverManager`
* 정적 메서드 제공 (`getConnection`)
* JDBC 드라이버들을 관리하는 유틸리티 역할
* 인터페이스 아님
* IoC, DI 없음
* 스레드로컬도 아님
* JDK에 포함

즉, 단순한 **정적 팩토리 클래스**라고 보면 됩니다.

---

## 3-2. Connection (인터페이스)

* **인터페이스**입니다.
* 구현체는 DB 벤더(H2, MySQL 등)가 제공
* 실제 동작은 H2 JDBC 드라이버 내부 구현체가 담당
* 자바 애플리케이션은 Connection을 “추상화된 채로” 사용

예:

```java
Connection con = DriverManager.getConnection(...);
```

리턴 타입은 인터페이스이지만
실제 생성되는 객체는 H2에서 제공하는 구현체입니다.

즉,
**자바는 Connection이 어떤 객체인지 몰라도 되며
다형성(polymorphism)으로 사용합니다.**

---

## 3-3. PreparedStatement (인터페이스)

* 이것도 인터페이스입니다.
* 구현체는 DB 드라이버가 제공
* SQL을 안전하게 실행하기 위한 추상화 역할

---

## 3-4. ResultSet (인터페이스)

* 역시 인터페이스
* DB의 결과를 읽기 위한 추상화

이 자체가 IO(strean)처럼 동작합니다.
DB에서 한 row씩 forward-only 커서로 읽습니다.
그래서 사실상 “DB 결과 스트림”이라고 볼 수 있습니다.

---

# 4. JDBC는 함수형, 스트림, 람다와 관련이 있는가?

거의 없습니다.

* JDBC API는 **1997년에 만들어진 오래된 API**
* 함수형 패러다임이나 스트림 개념이 등장하기 훨씬 이전
* ResultSet을 Stream으로 변환하는 기능은 JDK에 없음
* try-with-resources조차 JDBC 초기엔 없었음(JDK 7 추가)

즉,
JDBC는 **매우 오래된 명령형 API이며 객체지향 기반 추상화만 제공**합니다.

---

# 5. IO와 관련 있는가?

직접적으로는 IO 클래스(java.io.* , java.nio.*)를 사용하지 않지만,
**내부적으로는 네트워크 IO 기반으로 동작**합니다.

Connection = DB 서버와 TCP 소켓 연결
PreparedStatement = SQL을 네트워크로 전송
ResultSet = DB 서버가 보내주는 바이너리 결과를 읽어옴

그래서 JDBC는 네트워크 IO 기반 API라고 할 수 있으나,
IO를 개발자가 직접 다루지 않습니다.
모두 JDBC 드라이버가 숨겨줍니다.

---

# 6. “H2 콘솔에서 run하는 SQL을 자바 객체로 구현한 것인가?”

부분적으로는 맞고, 부분적으로는 아닙니다.

## 맞는 부분

H2 콘솔에서 실행하는:

```sql
insert into member (member_id, money) values ('A', 1000);
```

이 SQL을
자바에서 PreparedStatement로 실행할 수 있으므로

자바 코드가 H2 콘솔에서 실행하는 SQL의 “프로그래밍 버전”이라고 볼 수 있습니다.

## 아닌 부분

자바가 SQL을 객체로 변환한 것은 아닙니다.

* SQL은 문자열(String)일 뿐
* 객체지향적으로 SQL이 변환된 것은 아님
* 단순히 문자열을 DB로 보내고 결과를 받는 방식

JDBC는 다음과 같은 “미니 언어를 표현하는 객체 모델”이 아닙니다.

* AST(Abstract Syntax Tree) 없음
* SQL 파싱도 하지 않음
* SQL 의미 이해도 없음

그저 SQL 문자열을 DB 서버에 전달하는 통로이며,
실제 SQL 처리(파싱, 최적화, 실행)는 DB 서버(H2)가 담당합니다.

---

# 7. 전체 구조 요약

| 요소                | 성격     | 제공 주체                      |
| ----------------- | ------ | -------------------------- |
| DriverManager     | 클래스    | JDK                        |
| Connection        | 인터페이스  | JDBC 표준(JDK), 구현은 드라이버(H2) |
| PreparedStatement | 인터페이스  | JDBC 표준, 구현은 드라이버(H2)      |
| ResultSet         | 인터페이스  | JDBC 표준, 구현은 드라이버(H2)      |
| SQLException      | 예외 클래스 | JDK                        |
| close             | 개발자 작성 | 자원 해제 유틸리티                 |

이 모든 것은 **純 자바 + JDBC API + H2 드라이버의 구현체**로 구성됩니다.

스프링, 톰캣, 자바 스트림, 함수형 프로그래밍과는 무관합니다.

---

# 결론

당신이 묻고 싶어했던 핵심을 다시 한 줄로 정리하면:

> 이 코드의 객체들은 모두 자바(JDK)에서 제공한 JDBC 인터페이스 또는 H2 드라이버의 구현체이며, 스프링이나 톰캣, 함수형, 스트림, IO 라이브러리와 직접적인 관련은 없다.

---

원하시면 다음도 설명 드릴 수 있습니다.

* JDBC 인터페이스들(Connection/Statement/ResultSet)이 어떻게 다형성을 활용하는지
* H2 드라이버가 실제로 Connection 구현체를 어떻게 만들고 네트워크 통신하는지
* JDBC가 오래된 API라서 불편한 이유
* 스프링이 왜 JdbcTemplate을 만들었는지
* JPA가 이 추상화를 어떻게 더 극단적으로 발전시켰는지

어떤 방향으로 더 깊게 파고들고 싶나요?

정확합니다. 기본적으로 **DB 서버가 동시성(concurrency), 저장(persistence), 조회(read), 트랜잭션(transaction)** 같은 **데이터 관련 핵심 기능을 대부분 담당합니다.**
자바/JDBC 코드는 단순히 **“명령을 보내고 결과를 받을 뿐”**입니다.

아래에서 좀 더 체계적으로 설명해 드리겠습니다.

---

# 1. 동시성(concurrency)은 누가 처리하나?

**DB 서버가 처리합니다.**

여러 클라이언트(자바 애플리케이션, 다른 서버 등)가 동시에 접근하더라도:

* 동일한 레코드를 동시에 업데이트하면?
  → **DB가 Lock, MVCC 등으로 조절**
* 동시에 INSERT/UPDATE가 발생하면?
  → **DB가 순서 보장**
* Dirty Read / Phantom Read 같은 문제를 어떻게 해결하나?
  → **DB의 Isolation Level이 관리**

즉,
“두 명이 같은 데이터를 동시에 수정할 수 있나?” 같은 고민은
자바 코드가 아니라 **DB 엔진이 해결하는 문제**입니다.

---

# 2. 데이터 저장(persistence)은 누가 처리하나?

역시 **DB 서버가 담당합니다.**

DB 서버는 다음을 관리합니다.

* 디스크에 안전하게 저장
* WAL(Write-Ahead Logging)
* 체크포인트
* 인덱스 유지
* 장애 복구(recovery)
* 파일 시스템과의 인터랙션

자바 코드에서는 단지 다음만 함:

```java
pstmt.executeUpdate();
```

이 한 줄을 보내면 DB가 모든 로우 레벨 작업을 수행합니다.

---

# 3. 데이터 조회(read)는 누가 처리하나?

**DB 서버가 처리합니다.**

예를 들어 자바에서 다음을 실행했다고 하면:

```java
select * from member where member_id = ?;
```

DB는 다음을 내부에서 수행합니다.

* 쿼리 파싱
* 실행 계획 수립(optimizer)
* 인덱스를 사용할지 여부 결정
* 디스크/메모리에서 데이터 읽기
* 정렬, 그룹핑 등 결과 조작
* 네트워크를 통해 자바에게 결과 전송

**ResultSet은 DB가 보내주는 데이터를 한 줄씩 읽는 API**일 뿐입니다.

---

# 4. 트랜잭션(transaction)은 누가 처리하나?

트랜잭션의 **진짜 실행**은 DB가 합니다.

자바에서는 단지 이렇게 요청할 뿐입니다.

```java
con.setAutoCommit(false);
con.commit();  
con.rollback();
```

그러면 DB는 다음을 수행합니다.

* 여러 SQL을 하나의 원자적 작업으로 묶기
* 에러 발생 시 롤백
* 동시성 제어
* 락 처리
* 격리 수준 준수

즉, 트랜잭션의 본질적인 처리는 DB가 담당하고
자바는 **DB에게 명령을 전달하는 일**만 합니다.

---

# 5. 자바(JDBC)의 역할은 어디까지인가?

JDBC 코드의 역할은 아래 네 가지로 끝입니다.

1. **DB 연결하기** (DriverManager → Connection)
2. **SQL 전달하기** (PreparedStatement)
3. **DB가 준 결과 읽기** (ResultSet)
4. **자원 정리하기** (close)

그 외 동시성, 저장, 트랜잭션 처리, 복구 등은
**자바가 아닌 DB 서버가 처리합니다.**

JDBC는 그저 **문자열 SQL을 DB로 보내고, 결과를 받아오는 통로**입니다.

---

# 6. 비유로 설명하면

자바(JDBC)는 다음 역할입니다.

* 웨이터: 주문을 받아 주방(DB)으로 전달
* 주문 내용을 조합해 요리할 생각은 없음
* 요리는 전부 주방(DB)이 처리
* 완성된 요리를 손님에게 가져다줌

DB 서버는:

* 요리 순서(실행 계획) 결정
* 재료(데이터)를 찾아 조합
* 여러 손님이 와도 동시성 관리
* 주문 충돌 시 조정
* 음식이 망하면 rollback

모든 중대한 책임은 DB 서버가 담당합니다.

---

# 결론

> DB 서버가 동시성 제어, 저장, 조회, 트랜잭션, 인덱스, 복구 등 **모든 핵심 데이터 작업을 처리한다.**
> 자바/JDBC는 **명령 전달자이자 결과 수신자**일 뿐, 내부 처리 로직은 없다.

---

원하신다면 DB 내부 구조도 설명할 수 있습니다.

* MVCC가 어떻게 동시성을 해결하는지
* Lock이 어떻게 걸리고 해제되는지
* 왜 commit이 빠르고 rollback이 빠른지
* H2가 메모리 DB일 때와 파일 DB일 때 차이
* MySQL, H2, PostgreSQL 동시성 전략 차이

궁금한 부분을 알려주세요.

DB 서버(Database Engine)는 제품마다 서로 다른 언어로 작성됩니다. 대표적인 DBMS들이 어떤 언어로 구현되어 있는지 정리해보면 다음과 같습니다.

---

# 1. H2 Database

* **사용 언어: Java**
* H2는 완전히 순수 자바로 작성된 임베디드형/서버형 DB입니다.
* 그래서 자바 애플리케이션에서 실행하기 편리하며, JDBC 드라이버와 엔진이 같은 언어 기반이라 통합이 쉽습니다.

즉, H2는 내부 엔진까지 모두 자바로 구현되어 있습니다.

---

# 2. MySQL / MariaDB

* **사용 언어: C, C++**
* 전통적으로 매우 빠른 네이티브 성능을 위해 C/C++로 구현됨.
* 스토리지 엔진(예: InnoDB)도 C/C++ 기반.

---

# 3. PostgreSQL

* **사용 언어: C**
* 안정성과 표준 준수에 초점을 둔 DBMS.
* 전체 엔진이 C로 작성되어 있고 확장 기능 일부는 다른 언어도 가능(PL/pgSQL, PL/Python 등).

---

# 4. Oracle Database

* **사용 언어: C, C++**
* 상용 DB 중 가장 복잡한 엔진을 가진 시스템.
* 핵심 엔진은 매우 최적화된 C/C++ 코드 기반.

---

# 5. SQL Server (Microsoft)

* **사용 언어: C, C++**
* 윈도우 기반으로 개발되었으며, 엔진 대부분이 C/C++로 작성됨.

---

# 6. SQLite

* **사용 언어: C**
* 매우 가벼운 임베디드 DB.
* 단일 C 파일로 빌드하여 사용 가능할 정도로 작은 코드베이스.

---

# 정리

| DBMS          | 주요 언어  |
| ------------- | ------ |
| H2            | Java   |
| MySQL/MariaDB | C, C++ |
| PostgreSQL    | C      |
| Oracle        | C, C++ |
| SQL Server    | C, C++ |
| SQLite        | C      |

---

# 왜 대부분 C/C++으로 구현할까?

DB 엔진은 다음과 같은 작업을 합니다.

* 디스크 I/O 성능 최적화
* 메모리 버퍼 관리
* Lock, 동시성, 트랜잭션 구현
* 파일 시스템과의 고성능 연동
* 네트워크 처리
* 매우 낮은 레벨의 데이터 포맷 관리

이런 것들은 “가비지 컬렉션이 없는 언어(C/C++)”가 더 예측 가능하고 빠르게 만들 수 있기 때문입니다.

단,
H2처럼 Java로 만든 DB도 존재합니다.

H2는:

* 가벼움
* 자바 개발자 친화적
* JVM 위에서 돌아가는 환경에 적합

이라는 강점 때문에 Java 기반으로 만들어진 특별한 경우입니다.

---

# 결론

> 대부분의 DB 서버는 C/C++로 작성되지만, H2는 예외적으로 **순수 Java로 구현된 DB 엔진**이다.

---

원하시면 다음도 이어서 설명 가능합니다.

* H2가 자바로 구현되었을 때 얻는 장점과 한계
* DB 엔진이 내부적으로 어떻게 작동하는지(파서, 옵티마이저, 실행기 구조)
* 자바로 DB를 만들면 왜 느릴 수 있는지 또는 실제로 얼마나 느린지
* JVM 기반 DB가 가진 특징

어떤 쪽이 더 궁금하신가요?

1. JDBC는 데이터베이스 연동에서 어떤 문제를 해결하기 위해 만들어졌나요?

A
데이터베이스 쿼리 속도 저하

B
다양한 데이터베이스 미지원

데이터베이스 변경 시 코드 전면 수정 필요

D
복잡한 SQL 쿼리 작성의 어려움



해설
이전에는 DB마다 연결/쿼리 방식이 달라 DB 교체 시 코드를 다 바꿔야 했죠. JDBC는 표준 인터페이스로 이 문제를 해결했어요.
2. JDBC 드라이버의 역할은 무엇인가요?

A
여러 사용자의 DB 연결 관리

특정 DB에 대한 JDBC 표준 인터페이스 구현

C
SQL 쿼리 실행 계획 최적화

D
DB 관리를 위한 웹 콘솔 제공



해설
JDBC는 표준만 정의하며, 각 DB 제조사가 JDBC 드라이버를 통해 이 표준을 구현하여 DB 연동 기능을 제공합니다.
3. SQL 매퍼(MyBatis 등)와 ORM(JPA 등) 기술의 주요 차이점은 무엇일까요?

A
SQL 매퍼는 SQL을 자동 생성, ORM은 수동 SQL

B
SQL 매퍼는 ORM 위에 구축, ORM은 JDBC 직접 사용

SQL 매퍼는 SQL 작성 필요, ORM은 객체 매핑으로 SQL 생성

D
SQL 매퍼는 NoSQL, ORM은 관계형 DB용



해설
SQL 매퍼는 SQL을 직접 작성하지만 ORM은 객체와 DB 테이블을 매핑하여 필요한 SQL을 자동 생성하는 방식입니다.
4. JDBC에서 단순 Statement 문자열 연결 대신 PreparedStatement를 사용하는 주된 이점은 무엇인가요?

A
쿼리 실행 속도 향상

B
리소스 자동 반환

SQL Injection 공격 방지

D
복잡한 데이터 타입 지원



해설
PreparedStatement는 파라미터 바인딩을 제공하여 입력값을 데이터로 처리하므로, 악의적인 문자열이 SQL 구문으로 해석되는 것을 막아줍니다.
5. JPA나 Spring Data JPA 같은 최신 Java 데이터 접근 기술은 JDBC와 어떤 관계일까요?

A
JDBC를 완전히 대체합니다.

내부적으로 JDBC를 하위 기술로 사용합니다.

C
개발자가 JDBC 코드를 더 많이 작성해야 합니다.

D
드라이버 없이 데이터베이스에 연결합니다.



해설
JPA, ORM 등 최신 기술들도 결국 DB와 통신할 때는 JDBC 드라이버를 통해 JDBC 표준 인터페이스를 사용합니다.