아래는 당신이 실행한 테스트 코드와 출력 로그를 기반으로, 어떤 일이 일어났고 그 의미가 무엇인지 명확하게 정리한 해석이다.

---

## 1. 프록시 생성 확인 결과

출력

```
this.getClass() = class spring.tx.TxBasicTest
basicService.getClass() = class spring.tx.TxBasicTest$BasicService$$SpringCGLIB$$0
```

의미

* `basicService`의 실제 런타임 클래스가 `$$SpringCGLIB$$0` 형태로 나온다.
* 즉, 스프링이 `BasicService`에 대해 **CGLIB 기반 프록시**를 생성했다는 의미다.
* `@Transactional`이 있는 메서드를 감싸기 위해 프록시가 적용된 것이다.

검증

```
assertThat(AopUtils.isAopProxy(basicService)).isTrue();
```

→ 프록시가 맞으므로 테스트 성공.

결론

* 트랜잭션 AOP가 정상 적용되고 있다.

---

## 2. 트랜잭션 적용 테스트 결과

출력

```
BasicService.tx
true
BasicService.nonTx
false
```

의미

### tx()

* `@Transactional`이 붙어 있다.
* 호출 시 트랜잭션 프록시가 동작하여 실제 트랜잭션이 시작된다.
* `TransactionSynchronizationManager.isActualTransactionActive()` → `true`

### nonTx()

* `@Transactional`이 없다.
* 트랜잭션 프록시가 동작하지 않음.
* 트랜잭션 활성 여부 → `false`

결론

* `@Transactional` 메서드는 트랜잭션 안에서 실행된다.
* `@Transactional`이 없는 메서드는 트랜잭션 없이 실행된다.
* AOP 기반 트랜잭션 적용이 정상적으로 동작하고 있다.

---

## 3. 테스트 전체 흐름 정상

로그에서 볼 수 있는 내용

* H2 인메모리 DB 연결 성공
* EntityManagerFactory 정상 생성
* Spring Boot Test 환경 정상 로딩
* 테스트 두 개 모두 정상 수행
* 마지막에 Shutdown 정상 처리
* `BUILD SUCCESSFUL`

결론

* 설정된 Spring Boot 환경에서 트랜잭션 AOP, JPA 설정 모두 정상 동작
* 기대한 대로 테스트가 성공적으로 실행됨

---

## 4. Mockito 관련 경고 메시지의 의미

테스트 중간에 이런 경고가 뜸:

```
Mockito is currently self-attaching...
Dynamic loading of agents will be disallowed...
```

의미

* Mockito가 JVM의 에이전트 기능을 동적으로 attach하면서 뜨는 경고다.
* Java 21에서 강화된 보안 정책 때문에 메시지가 나오는 것.
* Mockito를 사용하는 다른 테스트가 있다면 관련 설정을 추가해야 할 수도 있다.
* 그러나 지금 테스트가 실패하거나 동작에 문제가 생긴 것은 아니다.

---

## 전체 요약

1. `BasicService`는 CGLIB 프록시로 만들어졌고 트랜잭션 AOP가 정상 적용됨.
2. `@Transactional`이 있는 메서드는 트랜잭션이 활성(true)됨.
3. 없는 메서드는 트랜잭션 없이 실행(false).
4. 테스트 전체는 정상적으로 완료됨.
5. Mockito 경고는 단순 JVM 에이전트 관련 경고이며 테스트 결과에는 영향 없음.

---

혹시 여기서 **“왜 같은 클래스 내의 메서드 호출에서는 트랜잭션이 적용되지 않나요?”** 같은 추가적인 트랜잭션 동작 원리를 더 알고 싶다면, 이어서 설명해 줄 수 있어.

아래 테스트 결과는 **클래스 레벨의 @Transactional(readOnly = true)** 와 **메서드 레벨의 @Transactional(readOnly = false)** 조합이 어떻게 동작하는지를 정확하게 보여주는 예시다. 출력 내용을 바탕으로 단계별로 해석해준다.

---

# 1. 핵심 요약

* 클래스에 있는 `@Transactional(readOnly = true)`가 기본 설정이 된다.
* 메서드에 `@Transactional(readOnly = false)`를 적용하면 **해당 메서드는 기본 옵션을 덮어쓴다**.
* 트랜잭션은 AOP 프록시로 생성되므로, `write()`와 `read()` 모두 트랜잭션 안에서 실행된다.
* 읽기 전용(read-only) 여부만 다르게 적용된다.

---

# 2. write() 호출 결과 해석

출력

```
LevelService.write
TransactionSynchronizationManager.isActualTransactionActive() = true
TransactionSynchronizationManager.isCurrentTransactionReadOnly() = false
```

설명

1. 클래스 레벨: `@Transactional(readOnly = true)`
2. 메서드 레벨: `@Transactional(readOnly = false)`

메서드 레벨 설정이 우선이므로

* 트랜잭션이 시작되고
* 읽기 전용이 아닌(readOnly = false) **쓰기 가능한 트랜잭션**이 된다.

결론

* `write()`는 정상적인 **쓰기 트랜잭션**(readOnly = false)으로 동작한다.

---

# 3. read() 호출 결과 해석

출력

```
LevelService.read
TransactionSynchronizationManager.isActualTransactionActive() = true
TransactionSynchronizationManager.isCurrentTransactionReadOnly() = true
```

설명

* `read()` 메서드에는 `@Transactional`이 붙어 있지 않다.
* 따라서 클래스 레벨의 `@Transactional(readOnly = true)`가 적용된다.

결론

* `read()`는 **읽기 전용 트랜잭션**에서 실행된다.
* 읽기 전용 기능: 플러시 억제, 일부 최적화 가능
  (JPA/Hibernate가 내부적으로 쓰기 작업을 막을 수도 있음)

---

# 4. 왜 write()도 read()도 둘 다 트랜잭션이 true일까?

`LevelService` 클래스 자체가

```
@Transactional(readOnly = true)
```

로 덮여 있기 때문에, 스프링 AOP는 **클래스의 모든 public 메서드를 트랜잭션 경계로 감싼다**.

그래서 메서드별 차이는 다음처럼 나타난다.

| 메서드     | 트랜잭션 활성 | read-only 여부 | 적용 규칙           |
| ------- | ------- | ------------ | --------------- |
| write() | true    | false        | 메서드 레벨 설정 우선    |
| read()  | true    | true         | 클래스 레벨 기본 설정 사용 |

둘 다 트랜잭션 안이지만 readOnly 설정만 다르다.

---

# 5. 테스트 전체 결론

이 테스트는 다음 사실을 검증하고 있다.

1. **클래스에 @Transactional을 붙이면 모든 public 메서드에 기본 트랜잭션이 적용된다.**
2. **메서드에 @Transactional이 있으면 해당 설정이 클래스의 설정을 덮어쓴다.**
3. 따라서

    * write() → readOnly = false
    * read() → readOnly = true
4. 트랜잭션 활성 상태는 둘 다 true로 나온다.

즉, 트랜잭션 전파/상속 규칙이 정상적으로 동작하고 있다.

---

추가로
메서드 내부에서 서로를 호출할 때(예: write → read)
트랜잭션이 어떻게 동작하는지에 대한 의문이 생길 수도 있는데,
관심 있다면 그 부분도 자세하게 설명해줄게.

아래는 스프링 트랜잭션에서 자주 등장하는 두 개념을 이해하기 쉽게 정리한 설명이다.

---

# 1. readOnly란 무엇인가?

`@Transactional(readOnly = true)`처럼 설정할 수 있는 **트랜잭션의 속성 중 하나**다.
읽기 작업에 최적화된 트랜잭션이라는 뜻이다.

## 핵심 의미

* **데이터를 읽기만 하는 트랜잭션**이라는 의도를 스프링에게 알려주는 것.
* 스프링은 이를 기반으로 내부 동작을 최적화한다.

## 실제로 하는 일

### 1) JPA/Hibernate에서 플러시(쓰기 작업 반영) 억제

readOnly = true이면 Hibernate는 가능한 한 플러시를 하지 않는다.
즉, 엔티티의 변경 내용을 DB에 반영하지 않으려 한다.

### 2) 일부 DB 또는 드라이버에서 read-only 세션으로 동작

DB 수준에서 읽기 전용 모드를 제공한다면 활용할 수 있다.
하지만 DB마다 지원 여부는 다르다.

### 3) 성능 최적화 효과

* 엔티티 변경 검사 최소화
* 플러시 방지로 성능 향상 가능

### 4) 의도 표현

"이 메서드는 읽기 작업만 해야 한다"는 신호이기 때문에
개발자 스스로에게도 제약을 주는 효과가 있다.

---

## 쉽게 요약하면

* **readOnly = true**
  → 트랜잭션은 열리지만 write 쿼리 실행을 지향하지 않고 성능을 최적화함.

* **readOnly = false** (기본값)
  → 읽기와 쓰기가 모두 가능한 일반 트랜잭션.

---

# 2. TransactionSynchronizationManager란 무엇인가?

스프링 프레임워크 내부에서 사용되는 **트랜잭션 상태 관리 도구**다.

트랜잭션이 현재 활성인지, read-only인지, 어떤 리소스(DB 커넥션)가 트랜잭션에 묶여 있는지 등을
스레드 로컬(ThreadLocal)에 저장하고 조회한다.

즉, 스프링 트랜잭션 AOP가 트랜잭션을 제어할 때 내부적으로 정보를 저장해두는 곳이다.

대표적인 메서드들

### `isActualTransactionActive()`

현재 실행 중인 스레드에 **실제 DB 트랜잭션이 존재하는지** 알려준다.

### `isCurrentTransactionReadOnly()`

현재 트랜잭션이 **readOnly인가?** 여부를 반환한다.

### 왜 ThreadLocal을 쓰는가?

하나의 요청은 하나의 스레드에서 실행되므로
트랜잭션 정보를 그 스레드에 안전하게 저장해둘 수 있기 때문이다.

이 덕분에 트랜잭션 AOP는 다음과 같은 작업을 한다.

* 메서드 실행 전: 트랜잭션 시작 → ThreadLocal에 정보 저장
* 메서드 실행 중: 필요한 곳에서 현재 트랜잭션 상태 조회
* 메서드 종료 후: 트랜잭션 커밋 또는 롤백 → ThreadLocal 정보 제거

---

# 쉽게 말하면

## readOnly

"이 트랜잭션은 읽기 전용이야. 쓰기 작업은 하지 않을 것이고, 성능을 최적화하도록 동작해줘."

## TransactionSynchronizationManager

"지금 트랜잭션이 켜져 있는지, 그리고 그 트랜잭션이 readOnly인지 등을 조회하는 스프링의 내부 상태 관리자."

---

read-only 동작이 실제 DB 쿼리와 JPA의 flush에 어떤 영향을 미치는지까지 더 알고 싶다면 그 부분도 자세히 설명할 수 있어.

아래 설명은 스프링에서 **트랜잭션이 어떻게 적용되는지**,
그리고 **왜 같은 클래스 내부에서 메서드를 호출하면 트랜잭션이 적용되지 않는 문제가 생기는지**를 풀어쓴 것이다.
당신이 적어준 글을 *정확하고 이해되게* 정리해준다.

---

# 1. @Transactional은 어떻게 동작하는가?

`@Transactional`이 붙으면 스프링은 그 메서드를 바로 실행하지 않는다.
대신 **프록시(proxy)** 라는 가짜 객체가 중간에 끼어든다.

동작 순서

1. 누군가 서비스 메서드를 호출한다.
2. 호출은 실제 서비스 객체가 아니라 **프록시 객체**에게 전달된다.
3. 프록시가 트랜잭션을 시작한다.
4. 그리고 나서 진짜 서비스 객체(target)를 호출한다.
5. 메서드가 끝나면 프록시는 트랜잭션을 커밋하거나 롤백한다.

따라서 핵심

* **프록시를 거쳐야 트랜잭션이 동작한다.**
* **프록시를 거치지 않고 진짜 객체를 직접 호출하면 @Transactional이 적용되지 않는다.**

---

# 2. 스프링은 왜 프록시를 bean으로 등록하는가?

스프링 컨테이너는

* 개발자가 만든 원래 객체를 빈으로 등록하는 것이 아니라,
* 그 객체를 상속/위임한 **프록시 객체를 빈으로 등록**한다.

그리고 의존성 주입할 때도 항상 **프록시**를 주입한다.

그래서 일반 상황에서는

```
controller → service 호출
```

이 과정에서 프록시가 항상 개입한다.
트랜잭션도 자연스럽게 적용된다.

---

# 3. 그런데 내부 메서드 호출에서는 문제가 생긴다

문제의 핵심은 이것 하나다.

### "같은 클래스 내부에서 메서드를 호출하면 프록시를 거치지 않는다."

예시

```java
public class MemberService {

    @Transactional
    public void saveMember() {
        validateMember(); // 내부 메서드 호출
        // DB 저장
    }

    public void validateMember() {
        // @Transactional이 붙어 있어도 프록시를 거치지 않는다
    }
}
```

왜 안 될까?

* 프록시는 **외부에서 호출될 때만** 동작한다.
* 내부에서 `this.validateMember()` 처럼 자기 자신의 메서드를 호출하면
  호출 흐름이 **프록시를 통과하지 않고** 바로 실제 객체 메서드를 실행한다.

결과

* `validateMember()`에 @Transactional이 있어도 적용되지 않는다.
* 트랜잭션이 필요해도 시작되지 않고 읽기 전용이나 전파 옵션도 먹히지 않는다.

이 문제는 **실무에서 정말 많이 발생**한다.

---

# 4. 요약

| 상황                           | 프록시 거침?   | @Transactional 적용? |
| ---------------------------- | --------- | ------------------ |
| 컨트롤러 → 서비스 호출                | 예         | 적용됨                |
| 서비스 외부에서 public 메서드 호출       | 예         | 적용됨                |
| 서비스 내부에서 자기 자신의 메서드 호출       | 아니오       | 적용 안됨              |
| private 메서드, protected 메서드는? | 프록시 대상 아님 | 적용 안됨              |

---

# 5. 왜 실무에서 자주 사고가 나는가?

예를 들어

```java
public void serviceA() {
    doSomething(); // 내부 호출
}

@Transactional
public void doSomething() {
    // 트랜잭션이 필요함
}
```

개발자는 트랜잭션이 적용될 것이라고 생각한다.
하지만 실제로는

* 프록시를 거치지 않았기 때문에
* 트랜잭션이 열리지 않는다.

그리고 이것 때문에

* DB가 롤백되지 않음
* 읽기 전용으로 설정했는데 쓰기 반영됨
* LazyInitializationException
* 트랜잭션 전파 규칙 무시
  등의 문제로 곤란해진다.

---

# 6. 그래서 어떻게 해결하나?

대표적인 해결 방법 3가지

1. 내부 호출을 외부 구조로 변경

    * 메서드를 다른 서비스로 분리
    * 의존성 주입을 통해 호출하면 프록시를 거친다.

2. 자기 자신도 프록시를 통해 호출하도록 별도 메커니즘 사용

    * 예: AopContext.currentProxy()
    * 단, 설정 필요.

3. 애초에 같은 클래스에 서로 다른 트랜잭션 메서드를 섞지 않기

    * 책임을 분리하는 것이 추천 방식.

---

# 결론

당신이 적은 글은 다음 메시지를 전달하고 있다.

### "@Transactional은 프록시 기반이므로, 프록시를 통해 호출될 때만 트랜잭션이 적용된다.

같은 클래스 내부에서 자기 메서드를 호출하면 프록시가 개입하지 않으므로 트랜잭션이 적용되지 않는다.
이 문제는 실무에서 자주 터지니 꼭 이해해야 한다."

---

원하면

* 내부 호출 문제를 그림으로 설명하거나
* 실제 코드로 재현하는 예제를 만들어 줄 수도 있어.

내부 메서드를 호출하지 말라는 뜻은 아니다.
**“내부 호출 방식으로는 @Transactional이 적용되지 않는다”**는 사실을 이해하고,
필요한 경우 **프록시를 반드시 거치도록 구조를 바꿔야 한다**는 의미이다.

즉, 해결 방법은 여러 가지가 있고, 내부 호출을 아예 금지하는 것은 아니다.
아래에서 정리해준다.

---

# 1. 내부 메서드 호출 자체는 문제가 없다

단지 **내부 호출 방식에서는 트랜잭션 AOP가 동작하지 않는다**는 점만 조심하면 된다.

예를 들어

* 단순 로직 분리
* private 메서드로 공통 코드 정리
  이런 내부 호출은 자유롭게 사용해도 된다.
  트랜잭션이 필요하지 않은 메서드라면 아무 문제가 없다.

---

# 2. 하지만 “트랜잭션이 필요한 메서드를 내부에서 호출하는 것”이 문제다

예를 들면

```java
public void serviceA() {
    inner(); // 내부 호출이면 트랜잭션이 적용되지 않음
}

@Transactional
public void inner() {
    ...
}
```

개발자는 트랜잭션이 적용된다고 착각하지만,
실제로는 **트랜잭션이 열리지 않는다**.

그래서 이것을 피해야 한다.

---

# 3. 해결 방법(정석)

스프링이 의도한 바는 단 하나다.

### “트랜잭션이 필요한 메서드는 반드시 ‘프록시’가 호출하도록 구조를 만들라.”

이를 지키기 위한 대표적인 해법 3개를 아래에 소개한다.

---

# 4. 해결책 1: 메서드를 외부 서비스로 분리

가장 많이 쓰고 가장 권장되는 방법이다.

```java
@Service
public class AService {
    private final BService bService;

    public void serviceA() {
        bService.inner();  // 프록시를 거침 → 트랜잭션 적용됨
    }
}

@Service
public class BService {
    @Transactional
    public void inner() { }
}
```

장점

* 구조가 명확하다
* 유지보수가 쉽다
* 스프링이 권장하는 방식

실무에서도 대부분 이런 식으로 서비스 책임을 분리한다.

---

# 5. 해결책 2: AopContext를 사용하여 자기 자신을 프록시 경유로 호출

코드를 바꾸지 않으면서 내부 호출 문제를 해결할 수 있다.

스프링 설정

```java
@EnableAspectJAutoProxy(exposeProxy = true)
```

호출 코드

```java
public void serviceA() {
    ((AService) AopContext.currentProxy()).inner();  
}
```

장점

* 코드만 약간 수정하면 됨
  단점
* 코드가 지저분해지고 가독성이 떨어짐
* 권장 방식은 아님

특별한 이유가 없다면 사용하지 않는 편이 좋다.

---

# 6. 해결책 3: 클래스 구조 재조정

예)

* public 메서드끼리 내부 호출하지 않도록 설계
* 하나의 서비스가 너무 많은 책임을 가지지 않도록 분리

이 방식은 근본적인 설계 개선을 통해 내부 호출 자체를 피한다.

---

# 7. 결론

### 내부 메서드를 호출하면 안 되는 것이 아니라,

### **트랜잭션이 필요한 메서드를 내부에서 호출하지 않아야 한다.**

해결 방법은 다음 중 하나이다.

1. 트랜잭션 필요한 메서드를 다른 서비스로 분리해서 프록시를 거치게 만들기(권장)
2. AopContext.currentProxy()로 자기 자신도 프록시로 호출하기(차선)
3. 설계를 개선해 내부 호출을 필요 없게 만들기

---

원한다면

* 내부 호출 문제가 실제로 어떻게 발생하는지 예제를 직접 만들어 보여줄 수도 있다.
* 또는 가장 좋은 서비스 설계 구조에 대해 안내해줄 수도 있다.

아주 좋은 질문이다.
당신이 헷갈린 지점을 **딱 정확하게** 짚어 설명해 줄게.
이 테스트는 “왜 어떤 호출에서는 트랜잭션이 적용되고, 어떤 호출에서는 적용되지 않는가”를 보여주는 전형적인 사례다.

아래 설명을 보면 완전히 명확해질 것이다.

---

# 1. 가장 중요한 사실

### **트랜잭션은 “프록시가 호출할 때만” 적용된다.**

### “같은 클래스 내부에서의 메서드 호출은 프록시를 거치지 않는다.”

이 두 줄이 전부다.

이 원리 때문에

* 외부에서 호출하면 @Transactional 적용됨
* 내부에서 호출하면 @Transactional 적용 안됨
  이런 현상이 발생한다.

---

# 2. 당신의 테스트에서 실제 호출 흐름

## 프록시 클래스 출력

```
callService.getClass() = class spring.tx.InternalCallTest$CallService$$SpringCGLIB$$0
```

즉,
스프링이 **CallService의 프록시 객체를 빈으로 등록했다**는 뜻이다.

따라서
테스트 코드에서 `callService.internal()` 또는 `callService.external()` 을 호출하면
항상 프록시가 먼저 받는다.

---

# 3. internalCall() 테스트 설명

출력

```
internalCall
CallService.internal
TransactionSynchronizationManager.isActualTransactionActive() = true
```

이 흐름은 다음과 같다.

1. 테스트 코드에서 `callService.internal()` 호출
2. callService는 프록시
3. 프록시가 먼저 트랜잭션을 시작
4. 실제 internal() 실행
5. 따라서 트랜잭션 활성 = true

결론

### 외부에서 프록시를 통해 internal()이 호출되므로 @Transactional 정상 적용된다.

---

# 4. externalCall() 테스트 설명

출력

```
externalCall
CallService.external
TransactionSynchronizationManager.isActualTransactionActive() = false
CallService.internal
TransactionSynchronizationManager.isActualTransactionActive() = false
```

여기에 핵심이 숨어 있다.

## 4-1. 첫 번째 부분: external() 호출

외부에서 호출되었지만 external()은 @Transactional이 아니다.
그래서 프록시는 다음과 같이 동작한다:

* external()에 @Transactional 없음
* 프록시는 “이 메서드에 트랜잭션을 적용할 필요가 없네?”
* → 바로 실제 메서드 실행

그래서 결과

```
TransactionSynchronizationManager.isActualTransactionActive() = false
```

즉, **external()은 트랜잭션 없이 실행됨**

---

## 4-2. 두 번째 부분: external() 내부에서 internal() 호출

여기서 많은 개발자가 헷갈린다.

external() 내부에서

```
internal();
```

를 호출하면 다음이 발생한다.

* 이 호출은 **프록시를 거치지 않는다.**
* 단지 **this.internal()** 과 동일한 내부 호출이다.
* 따라서 @Transactional이 있어도 프록시는 개입하지 못한다.

결과

```
TransactionSynchronizationManager.isActualTransactionActive() = false
```

internal() 메서드에 @Transactional이 붙어 있어도
**내부 호출이라서 트랜잭션이 적용되지 않은 것**이다.

---

# 5. 정리해서 하나의 표로 보면

| 호출 방식                           | 프록시 경유? | @Transactional 적용? | 결과      |
| ------------------------------- | ------- | ------------------ | ------- |
| 테스트 코드 → callService.internal() | 예       | 예                  | 트랜잭션 있음 |
| 테스트 코드 → callService.external() | 예       | 없음                 | 트랜잭션 없음 |
| external() 내부 → internal()      | 아니오     | 있음 (하지만 무시됨)       | 트랜잭션 없음 |

---

# 6. 질문에 대한 답변 정리

### 질문 1

트랜잭션이 적용되고 안 되고 기준이 뭐야?

→ **프록시를 거쳤는가?**
그게 유일한 기준이다.

---

### 질문 2

프록시는 클래스인데 transaction이 안 붙은 메서드는 AOP가 실행이 안 되는 걸까?

맞다.

프록시는 다음처럼 판단한다.

* 호출한 메서드에 @Transactional이 있으면
  → 트랜잭션 시작 후 target 호출

* @Transactional이 없으면
  → 트랜잭션 없이 바로 target 호출

즉, 프록시가 “이 메서드는 트랜잭션이 필요 없다”고 판단하면 AOP가 동작하지 않는다.

---

### 질문 3

그러면 internal()에 @Transactional 있어도 external()에서 호출하면 왜 적용되지 않나?

→ **외부에서 호출된 게 아니라 내부(this)에서 호출되었기 때문이다.**
→ 내부 호출은 프록시를 거치지 않기 때문이다.

---

# 7. 핵심 결론 한 줄로 요약

### “@Transactional은 프록시가 호출할 때만 적용된다.

같은 클래스 내부에서 자기 자신을 호출하면 프록시를 거치지 않아 트랜잭션이 적용되지 않는다.”

---

더 알고 싶다면

* 내부 호출 문제를 해결하는 방법
* 트랜잭션 프록시가 동작하는 실제 call 흐름 그림
* JDK 동적 프록시 vs CGLIB 차이

이런 부분도 이어서 설명해 줄 수 있어.
