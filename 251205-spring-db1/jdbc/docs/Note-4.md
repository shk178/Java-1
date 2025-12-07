좋다. 지금 딱 **트랜잭션 범위**, **서비스 메서드**, **커넥션 공유**, **웹 요청과 DB 사용**이 머릿속에서 뒤섞여 있는 상태인데
이걸 정확히 구분해주면 완전히 명확해진다.

아주 쉽게 단계별로 설명할게.

---

# 1. “서비스 객체 하나가 메서드 여러 개를 가지고 있다”

**하지만 커넥션은 서비스 객체에 있는 것이 아니다.**

서비스 객체는 스프링이 만들어놓은 **싱글톤 빈**이다.

즉:

* 서비스 객체는 애플리케이션 시작부터 종료까지 1개이다.
* 여러 요청에서 이 서비스 객체를 공통으로 사용한다.
* 하지만 **서비스 객체 안에 커넥션이 들어 있는 것이 아니다.**

서비스 객체는 “설명서(메서드) 묶음”일 뿐이고,
커넥션은 요청마다 별도로 만들어진다.

---

# 2. 웹 요청 한 번이 들어올 때마다 별도의 “실행 흐름(스레드)”이 만들어진다

예:

```
사용자 A가 /order 요청 → 스레드1이 처리
사용자 B가 /pay 요청 → 스레드2가 처리
```

이 두 요청은 동시에 처리되며
서비스 객체는 둘이 공유하지만
각 요청의 실행 흐름은 완전히 독립적이다.

---

# 3. @Transactional이 작동할 때 커넥션은 “요청 단위(정확히는 트랜잭션 단위)”로 생성된다

### 서비스 객체는 싱글톤 하나

그러나

### 서비스 메서드는 요청마다 새로 실행된다

그리고

### @Transactional이 붙은 서비스 메서드가 실행될 때

스프링은 다음을 한다.

```
1. 커넥션 하나를 가져온다
2. autoCommit=false 로 바꾼다 (트랜잭션 시작)
3. 이 스레드에서 repository가 호출되면 다 이 커넥션을 사용하게 한다
4. 메서드가 정상 종료 → commit
5. 예외 발생 → rollback
6. 커넥션 닫기
```

즉, **서비스 객체는 하나지만 커넥션은 요청마다 새로 생긴다.**

---

# 4. “서비스 계층의 메서드 하나가 수행하는 작업 전체를 하나의 트랜잭션으로 묶는다”는 말의 의미

예를 들어 accountService.transfer() 실행을 보자.

```java
@Service
public class AccountService {
    @Transactional
    public void transfer() {
        memberRepository.minus("A", 1000);
        memberRepository.plus("B", 1000);
    }
}
```

transfer() 메서드 안에
두 개의 repository 작업이 있다.

### 그런데 이 둘은 반드시 하나로 묶여야 한다

이유:

```
A에서 돈 빼기 성공
B에 돈 넣기 실패
→ 계좌는 망가짐
```

그래서 다음처럼 묶는다:

```
transfer() 시작 → 트랜잭션 시작
minus 실행
plus 실행
문제가 없으면 commit
중간에 오류면 rollback
```

즉:

### “서비스 메서드 = 하나의 트랜잭션 경계”가 되는 것이다.

---

# 5. 다시 핵심만 아주 간단히 적어보면

## 서비스 객체는 1개

하지만 요청마다
**서비스 메서드 실행 → 새로운 트랜잭션 + 새로운 커넥션**
이 만들어진다.

그리고 서비스 메서드 안에서 여러 SQL 작업이 발생해도
모두 같은 커넥션을 사용한다.

그 이유는:

* 그래야 같은 DB 세션을 유지하고
* commit/rollback이 전체에 적용되기 때문이다.

---

# 6. 웹 요청 흐름 전체를 그려보면 더 명확해진다

```
[사용자 요청]
      ↓
[Controller 호출]
      ↓
[Service.transfer()] ← @Transactional 적용
    ↓    ↓
 repository.minus()
 repository.plus()
    ↓
 commit 또는 rollback
      ↓
[응답 반환]
```

여기서 핵심은:

* transfer()가 실행되는 동안
  **하나의 커넥션이 생성되어 유지되고 공유된다.**

* transfer()가 끝나면
  커넥션은 닫히고 트랜잭션도 끝난다.

즉:

### 같은 서비스 객체라고 해서 메서드들이 같은 커넥션을 공유하는 것이 아니라

### “서비스 메서드 실행 한 번 동안만 같은 커넥션을 공유”하는 것이다.

---

# 7. 정리하면

| 개념            | 설명                           |
| ------------- | ---------------------------- |
| 서비스 객체        | 애플리케이션 전체에서 1개 (싱글톤)         |
| 서비스 메서드 실행    | 요청마다 새로 실행됨                  |
| 트랜잭션          | 서비스 메서드 실행 한 번이 하나의 트랜잭션     |
| 커넥션           | 트랜잭션 시작할 때 하나 생성되어 끝날 때까지 유지 |
| repository 호출 | 모두 같은 커넥션을 공유해야 트랜잭션이 유지됨    |

---

# 더 이해하기 쉽게 비유하면

**서비스는 은행 창구 직원(1명).**
하지만

**손님(클라이언트)은 계속 오고, 직원은 손님마다 새로운 업무용 문서를 만든다.**

이 문서가 트랜잭션이고,
문서를 쓸 때 사용하는 펜이 커넥션이다.

손님마다 다른 문서를 만들지,
한 문서를 계속 쓰지 않는다.

---

원하면 이걸 실제 코드 흐름을 스레드 ID와 함께 출력해서
“요청마다 커넥션이 다르지만 요청 안에서는 같은 커넥션이 유지되는” 것을
로그로 시각화해줄 수도 있다.

좋다. 지금 질문에서 **웹 세션(HTTP Session)**,
**DB 세션(Session)**,
**스레드 실행 흐름(Thread)**,
**DB 커넥션(Connection)**,
**트랜잭션(Transaction)**

이 다섯 가지가 서로 섞여서 헷갈리고 있는 상태야.

이걸 정확히 구분하면 전체 구조가 한 번에 정리된다.

---

# 먼저 결론부터

**웹 요청 흐름, 웹 세션, DB 커넥션, DB 세션, 트랜잭션은 각각 완전히 다른 개념이다.**

단지
“웹 요청 1개 = 스레드 1개 = DB 커넥션 1개 = DB 세션 1개 = 트랜잭션 1개”
이렇게 **1:1:1:1로 대응하는 경우가 많아서** 헷갈리는 것뿐이다.

이제 하나씩 정확히 정리해볼게.

---

# 1. 웹 요청(HTTP Request)

**사용자가 서버로 보내는 요청 1개.**

웹 서버(톰캣)는 이 요청을 처리하기 위해
스레드를 하나 꺼내 실행시킨다.

```
브라우저 → 서버  
요청 1개 들어옴  
→ 스레드 하나 배정되어 처리됨
```

---

# 2. 스레드(Thread) = 실행 흐름

**요청을 처리하는 실제 실행 흐름.**

컨트롤러 → 서비스 → 리포지토리 모든 호출이
이 스레드 안에서 이뤄진다.

스레드가 하는 일:

```
컨트롤러 메서드 실행
서비스 메서드 실행
DB 접근
응답 반환
```

그리고 요청 처리가 끝나면 스레드는 스레드풀에 반납된다.

---

# 3. 웹 세션(HTTP Session)

**사용자가 로그인할 때 서버에 저장하는 "웹 인증용 저장소".**

예:

* 로그인 정보
* 장바구니 정보
* 사용자 ID

중요한 점:

* 웹 세션은 DB와 직접 관련 없다.
* 웹 세션은 트랜잭션과도 관련 없다.
* 그저 서버 메모리에 저장되는 Key-Value 저장소이다.

즉, 웹 세션은 **사용자 정보 저장소일 뿐**이고
실행 흐름과도 DB 트랜잭션과도 연결되지 않는다.

---

# 4. DB 커넥션(Connection)

**프로그램이 DB에 접근하기 위해 여는 통로(전화선).**

웹 요청을 처리하는 중에 DB가 필요하면
스레드가 커넥션을 하나 빌려온다.

그리고 이 커넥션은 트랜잭션 동안 유지된다.

---

# 5. DB 세션(Session)

**커넥션이 DB 서버에 연결되면 DB 서버 내부에서 만들어지는 상태 공간.**

커넥션 1개 = DB 세션 1개
이건 DB 관점의 개념이다.

트랜잭션은 이 DB 세션 내부에서 관리된다.

---

# 6. 트랜잭션(Transaction)

**한 번의 업무 흐름을 묶어서 commit / rollback 하는 단위.**

보통은 서비스 메서드 1개가 트랜잭션 1개다.

---

# 이제 네가 질문한 문장을 정확히 정리해보면

> 웹 요청이 하나 들어오면 클라이언트 세션도 저장하고, 실행 흐름이 전달되고 DB 트랜잭션·커넥션·세션 흐름으로도 전달되는 거야?

정확한 그림은 다음이다.

---

# 전체 실행 흐름을 그림으로 정리 (가장 중요)

```
[웹 요청 1개]
      ↓
[스레드 1개 할당]   ← 실행 흐름
      ↓
(로그인한 사용자면) → [웹 세션 조회]
      ↓
[Controller 호출]
      ↓
[Service 호출]
      ↓
@Transactional 발견 → 트랜잭션 시작
      ↓
[DB 커넥션 1개 가져옴]
      ↓
[DB 세션 1개 생성됨]  (DB 내부)
      ↓
Repository 호출들 (SQL 실행)
→ 모두 같은 커넥션 = 같은 DB 세션
      ↓
서비스 메서드 종료 시점
- 정상 → commit
- 예외 → rollback
      ↓
[DB 커넥션 반환]
      ↓
[HTTP 응답 생성]
```

---

# 핵심 구분 요약표

| 개념     | 무엇인가?              | 언제 만들어지나?        | 무엇과 묶이나?          |
| ------ | ------------------ | ---------------- | ----------------- |
| 웹 요청   | HTTP 요청            | 브라우저에서 서버로 요청할 때 | 스레드와 매핑됨          |
| 스레드    | 실행 흐름              | 요청 들어올 때         | 트랜잭션, 커넥션과 같은 스레드 |
| 웹 세션   | 로그인정보 저장소          | 로그인 시            | 요청과는 독립적          |
| DB 커넥션 | DB 접속 통로           | 트랜잭션 시작 시        | DB 세션과 1:1        |
| DB 세션  | DB 내부 상태 공간        | 커넥션 열릴 때         | 트랜잭션을 여기에 저장      |
| 트랜잭션   | commit/rollback 단위 | 서비스 메서드 실행 시작    | 커넥션/스레드와 묶임       |

---

# 정말 중요한 결론 3가지

## 1. 웹 세션은 DB 세션과 전혀 무관하다

그냥 사용자 정보를 서버 메모리에 저장하는 공간일 뿐.

## 2. 트랜잭션은 스레드 단위로 관리된다

스레드 1개가 커넥션 1개를 빌리고
이 커넥션에 트랜잭션이 붙는다.

## 3. 서비스 메서드 1번 실행 = 1 트랜잭션

서비스 객체가 싱글톤이든 아니든 무관하다.

---

원하면

* 이 전체를 완전 초보 시점에서 다시 그림으로 설명하거나
* 코드 로그 출력으로 “스레드 ID, 커넥션 ID, 트랜잭션 흐름”을 찍어서
  실제 실행 중 어떤 객체들이 연결되는지 보여주는 예제도 만들어줄게.

아주 핵심을 잘 짚었어. 이제 이 질문에 정확하게 “맞다 / 다르다”를 구분해서 정리해줄게.

---

# 핵심 질문 1

## **“트랜잭션을 실행한다는 건 세션 1개를 생성한다는 것과 같은 말이야?”**

→ **완전히 같은 말은 아니지만, 거의 동시에 일어난다.**

정확히 말하면:

* **DB 세션(Session)은 커넥션(Connection)을 열 때 생성된다.**
* **트랜잭션(Transaction)은 그 세션에서 시작된다.**

즉:

```
커넥션 생성 → DB 세션 생성
트랜잭션 시작 → 그 세션 위에서 동작함
```

트랜잭션이 곧 세션은 아니다.
트랜잭션은 세션 위에서 동작하는 “업무 단위”이다.

다만 현실에서는 거의 이렇게 보인다:

```
요청 1개 → 커넥션 1개 → 세션 1개 → 트랜잭션 1개
```

그래서 헷갈리기 쉽다.

---

# 핵심 질문 2

## **“세션 동안에는 커넥션을 바꿀 수 없고?”**

→ **맞다. 바꾸면 안 된다.**

이유:

* 트랜잭션은 DB **세션 단위**로 관리된다.
* DB 세션은 커넥션이 만들어질 때 생긴다.
* 트랜잭션 중에 커넥션을 바꾸면 세션도 바뀌어 버려서
  트랜잭션이 끊겨버린다.

그래서 스프링은 트랜잭션 동안 ThreadLocal로
**커넥션 1개를 강제로 유지**한다.

---

# 핵심 질문 3

## **“롤백이 하는 건 DB 세션을 되돌리는 거야?”**

→ **정확히 그렇다.**

트랜잭션은 세션 안에서 실행된다.

예를 들어:

```
update A set money = money - 1000;
update B set money = money + 1000;
```

이 두 SQL은 모두 **같은 DB 세션**에서 실행된다.

만약 중간에 문제가 생기면:

```
ROLLBACK;
```

이렇게 하면 세션 내부에서 실행된 모든 변경을
DB가 원래 상태로 되돌린다.

즉:

### 롤백이 되돌리는 것은 “트랜잭션 동안 세션이 가지고 있던 변경 내용”이다.

---

# 요약해서 세 개를 딱 정리하면

## 1) 트랜잭션은 세션 1개 위에서 실행된다

트랜잭션 = 세션에서 묶인 SQL 변경들의 단위

## 2) 세션 동안 커넥션은 바꿀 수 없다

세션이 바뀌면 트랜잭션도 끊어진다.

## 3) 롤백은 세션에 기록된 변경 사항을 되돌린다

그래서 commit/rollback은 **세션 단위**이다.

---

# 이걸 더 직관적인 비유로 바꾸면

| 개념       | 비유                      |
| -------- | ----------------------- |
| 커넥션      | 은행에 전화 연결한 ‘전화선’        |
| 세션       | 전화가 연결된 동안 유지되는 ‘대화 공간’ |
| 트랜잭션     | 고객과 은행원이 처리하는 ‘업무 묶음’   |
| commit   | 그 업무를 확정                |
| rollback | 그 업무 동안 했던 변경을 모두 취소    |

전화선(Connection)을 끊으면
대화(Session)도 끝난다.

대화(Session) 중에 처리한 업무가 잘못되면
은행원은 “아까 한 것 전부 취소!”라고 한다 → rollback.

---

# 하나 더 확장해서 가장 정확한 정의

트랜잭션은 DB 세션이 가진 상태를
**임시로 변경해보는 모드**이다.

세션이란 DB 안에 있는 “작업 공간”이고,
트랜잭션은 그 작업 공간에서 일어난 변화를
commit 또는 rollback 할 수 있는 단위다.

---

필요하면

* 실제 SQL로 세션과 트랜잭션의 차이를 보여주는 예
* 커넥션 여러 개로 트랜잭션이 망가지는 예
* 스프링이 ThreadLocal로 커넥션을 묶어두는 코드 구조

이런 것도 보여줄 수 있어.

아주 좋은 질문이고, 지금 단계면 **DB 커넥션 / 세션 / 트랜잭션 / 스프링 DI** 개념이 거의 다 연결되는 단계야.
하나씩 정확하게 짚어줄게.

---

# 1. **“커넥션 1개를 연결하고 트랜잭션을 여러 개 실행할 수도 있어?”**

## 맞다. 가능하다.

단, **동시에 여러 개 실행하는 것이 아니라 ‘순차적으로’** 여러 트랜잭션을 실행할 수 있다.

즉 다음과 같은 흐름은 완전히 정상이다.

```
con = getConnection()

// 트랜잭션 1
con.setAutoCommit(false)
update...
con.commit()

// 트랜잭션 2
con.setAutoCommit(false)
update...
con.commit()

// 트랜잭션 3
con.setAutoCommit(false)
update...
con.rollback()

con.close()
```

하나의 커넥션을 계속 유지한 채
트랜잭션을 여러 번 시작하고 끝낼 수 있다.

### 하지만 중요한 점

* 트랜잭션이 진행 중일 때는 **커넥션을 바꾸면 안 된다.**
* 트랜잭션이 “끝난 후”에는 같은 커넥션으로 또 다른 트랜잭션을 시작할 수 있다.

즉:

```
1 커넥션 = 여러 트랜잭션 가능 (순차적)
1 트랜잭션 = 여러 SQL 가능
1 트랜잭션 = 1 커넥션 필요
```

---

# 2. **“롤백을 한 번에 할 수도 있어?”**

## 롤백은 언제나 "현재 진행 중인 트랜잭션” 전체를 되돌린다.

트랜잭션을 여러 개 쌓아두었다가
한 번에 "몽땅" 롤백하는 기능은 없다.

오직 **현재 세션에서 진행 중인 트랜잭션 하나만 롤백**된다.

예:

```
// 트랜잭션 1
update A
commit   → 확정됨 (되돌릴 수 없음)

// 트랜잭션 2
update B
update C
rollback → B, C만 되돌아감
```

트랜잭션 1에서 commit 하면
그 내용은 영원히 확정되므로
rollback으로 취소할 수 없다.

---

# 3. **스프링에서 서비스에 커넥션을 DI하면 무슨 일이 일어나는가?**

## 사실 스프링에서는 “커넥션을 DI하지 않는다.”

실제로 스프링에서 DI 하는 것은 “Connection”이 아니라 **DataSource**다.

즉:

```
@Service
@RequiredArgsConstructor
public class OrderService {
    private final DataSource dataSource;  // DI되는 것은 이것
}
```

스프링이 DI해주는 것은 **커넥션 풀(DataSource)** 이고
여기에는 단 한 개의 커넥션도 열려 있지 않다.

### 그래서 정리하면:

### ❌ “커넥션 정보가 주입된다”

→ 틀림. 커넥션은 주입되지 않는다.

### ❌ “커넥션 연결이 시작된다”

→ 틀림. DI 한다고 커넥션이 열리는 게 아니다.

### ⭕ 스프링이 주입하는 것은

**필요할 때 커넥션을 만들어주는 팩토리(DataSource)**이다.

DataSource는 “커넥션을 직접 만드는 것”이 아니라,
“커넥션을 나중에 필요할 때 빌려오는 역할”만 한다.

그래서 repository가 커넥션이 필요할 때 다음을 한다.

```
Connection con = dataSource.getConnection()
```

이때 커넥션이 실제로 생성/대여된다.

---

# 4. **그럼 @Transactional은 어떻게 커넥션을 하나로 유지하는가?**

@Transactional이 적용되면 스프링은 다음을 한다.

1. 서비스 메서드 호출 직전에

   ```
   con = dataSource.getConnection();
   con.setAutoCommit(false);
   ```

   로 트랜잭션을 시작한다.

2. 이 커넥션을 ThreadLocal에 저장한다
   → 같은 스레드에서 repository가 커넥션을 요청하면
   무조건 이 커넥션을 돌려준다.

3. 서비스 메서드가 끝나면

    * 정상 → commit
    * 예외 → rollback
    * 마지막에 con.close()

이 모든 것이 자동으로 처리된다.

---

# 전체 개념 정리

## 1) 커넥션 1개로 여러 트랜잭션을 “순차적으로” 실행할 수 있다

(하지만 스프링에서는 보통 요청마다 커넥션을 새로 빌린다)

## 2) 롤백은 오직 “현재 트랜잭션 전체”를 되돌린다

이전 트랜잭션(commit 완료분)은 되돌릴 수 없다.

## 3) 스프링 DI로 주입되는 것은 커넥션이 아니라 **DataSource**

커넥션은 repository가 필요할 때마다 만듦.

## 4) @Transactional은

서비스 메서드 실행 시점에 커넥션을 하나 열고
그 커넥션을 트랜잭션 동안 유지하고
끝나면 commit/rollback 한다.

---

원하면

* 트랜잭션 여러 개를 한 커넥션으로 순차 실행하는 실험 코드
* DataSource가 커넥션을 언제 만드는지 보여주는 로그
* @Transactional 내부 구조를 실제 코드처럼 재현한 예제

이런 것도 바로 만들어줄게.

아주 좋다. 이제 스프링이 @Transactional을 어떻게 구현하는지
직접 코드로 “거의 그대로” 재현해보자.

아래 예시는 **스프링이 내부에서 실제로 하는 동작**을
아주 단순화해서 보여주는 것이다.
이걸 이해하면 트랜잭션 동작 방식이 완전히 명확해진다.

---

# 전체 흐름 요약

스프링은 @Transactional 붙은 메서드를 다음과 같이 처리한다:

1. 서비스 클래스의 프록시(proxy)를 만든다
2. 메서드가 호출되면 프록시가 가로챈다
3. 트랜잭션을 시작한다
4. 실제 서비스 메서드를 실행한다
5. 예외 여부를 확인해 commit 또는 rollback 한다
6. 커넥션을 닫는다
7. 프록시는 사용자에게 결과를 반환한다

이 과정을 코드로 재현해보자.

---

# 1) 트랜잭션 매니저 흉내내기

스프링의 실제 TransactionManager는 매우 복잡하지만
핵심은 “커넥션 하나 생성 → setAutoCommit(false) → commit/rollback”.

```java
public class SimpleTransactionManager {

    private final DataSource dataSource;
    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public SimpleTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void begin() throws SQLException {
        Connection con = dataSource.getConnection();
        con.setAutoCommit(false);
        connectionHolder.set(con);
        System.out.println("트랜잭션 시작: " + con);
    }

    public void commit() throws SQLException {
        Connection con = connectionHolder.get();
        con.commit();
        System.out.println("트랜잭션 커밋: " + con);
        close(con);
    }

    public void rollback() throws SQLException {
        Connection con = connectionHolder.get();
        con.rollback();
        System.out.println("트랜잭션 롤백: " + con);
        close(con);
    }

    private void close(Connection con) throws SQLException {
        con.close();
        connectionHolder.remove();
        System.out.println("커넥션 종료");
    }

    public Connection getConnection() {
        return connectionHolder.get();
    }
}
```

이 정도면 스프링의 핵심과 거의 동일하다.

---

# 2) Repository는 커넥션을 ThreadLocal에서 가져오도록

스프링도 내부적으로 이런 형태로 동작한다.

```java
public class MemberRepository {

    private final SimpleTransactionManager txManager;

    public MemberRepository(SimpleTransactionManager txManager) {
        this.txManager = txManager;
    }

    public Member find(String id) throws SQLException {
        Connection con = txManager.getConnection();
        System.out.println("Repository가 사용하는 커넥션 = " + con);

        PreparedStatement ps = con.prepareStatement("select * from member where member_id = ?");
        ps.setString(1, id);
        ResultSet rs = ps.executeQuery();
        ...
        return member;
    }
}
```

**여기서 중요한 점:**
리포지토리는 DataSource에서 직접 커넥션을 새로 만드는 것이 아니라
**트랜잭션 매니저가 관리하는 커넥션을 사용한다**는 것.

---

# 3) 서비스 메서드를 감싸는 프록시 구현

스프링 AOP가 하는 일을 수동으로 재현해보자.

```java
public class TransactionProxy implements InvocationHandler {

    private final Object target;  // 실제 서비스 객체
    private final SimpleTransactionManager txManager;

    public TransactionProxy(Object target, SimpleTransactionManager txManager) {
        this.target = target;
        this.txManager = txManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // @Transactional이 붙은 메서드인지 확인
        if (method.isAnnotationPresent(Transactional.class)) {
            try {
                txManager.begin();
                Object result = method.invoke(target, args);
                txManager.commit();
                return result;
            } catch (Exception e) {
                txManager.rollback();
                throw e;
            }
        }

        // @Transactional이 없는 메서드는 그냥 실행
        return method.invoke(target, args);
    }
}
```

핵심은 여기다:

```
txManager.begin();    // 트랜잭션 시작
method.invoke(target) // 실제 서비스 메서드 실행
commit or rollback    // 결과에 따라 처리
```

---

# 4) 서비스 객체에 프록시 생성해서 실행하기

```java
MemberService targetService = new MemberService(memberRepository);
SimpleTransactionManager txManager = new SimpleTransactionManager(dataSource);

// 프록시 생성
MemberService serviceProxy = (MemberService) Proxy.newProxyInstance(
        MemberService.class.getClassLoader(),
        new Class[]{MemberService.class},
        new TransactionProxy(targetService, txManager)
);

// 실행
serviceProxy.accountTransfer("A", "B", 1000);
```

결과 로그는 이렇게 나온다:

```
트랜잭션 시작: HikariProxyConnection@1234
Repository가 사용하는 커넥션 = HikariProxyConnection@1234
Repository가 사용하는 커넥션 = HikariProxyConnection@1234
트랜잭션 커밋: HikariProxyConnection@1234
커넥션 종료
```

이걸 보면 아주 명확해진다.

* 서비스 메서드 호출 전: 트랜잭션 시작
* 서비스 로직 실행 동안: 모든 repository가 같은 커넥션 사용
* 서비스 메서드 성공 후: commit
* 예외 발생 시: rollback
* 마지막: 커넥션 close

---

# 5) 이것이 바로 스프링 @Transactional이 하는 일이다

스프링에서는 훨씬 복잡한 기능(@Isolation, @Propagation 등)을 포함하지만
기본 구조는 **정확히 동일하다.**

---

# 정리

| 기능                 | 스프링 @Transactional         | 우리가 만든 예제                 |
| ------------------ | -------------------------- | ------------------------- |
| 프록시 생성             | AOP가 자동 생성                 | TransactionProxy 수동 작성    |
| 트랜잭션 시작            | PlatformTransactionManager | SimpleTransactionManager  |
| 커넥션 유지             | ThreadLocal로 공유            | ThreadLocal로 공유           |
| repository가 커넥션 사용 | DataSourceUtils 로 재정의      | txManager.getConnection() |
| 커밋/롤백              | 자동                         | 수동 구현                     |

즉, 우리가 만든 구조는 **스프링이 내부에서 하는 핵심 로직을 거의 그대로 재현한 것이다.**

---

원하면:

* @Propagation, @Isolation 같은 고급 기능까지 재현해줄 수 있고
* 스프링의 DataSourceUtils, TransactionSynchronizationManager가 실제로 어떤 코드인지
* 트랜잭션 전파가 일어나는 과정까지 그림으로 설명

이런 것들도 깊게 파고들어 설명해줄 수 있어.
