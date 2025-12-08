결론부터 말하면 **대부분의 경우, 두 메서드는 서로 다른 Connection을 사용한다.**
하지만 그 이유는 *어떻게 트랜잭션이 시작되느냐*와 *트랜잭션 경계가 어디에 있느냐*에 따라 달라진다. 아래에서 정확하게 정리해줄게.

---

# 1. `bizLogic()`

```java
transactionTemplate.executeWithoutResult(...)
```

여기서는 `TransactionTemplate`이 내부에서 다음을 수행한다:

1. 트랜잭션 시작
2. Connection 생성 또는 가져오기
3. 콜백 실행
4. commit / rollback
5. 트랜잭션 종료 → Connection 반환

즉, **`bizLogic()`은 매 호출마다 새로운 트랜잭션을 직접 시작하고 종료한다.
따라서 Connection도 트랜잭션 생명주기 동안 하나를 빌려왔다가 반환한다.**

---

# 2. `bizLogic2()`

```java
@Transactional
public void bizLogic2() { ... }
```

`@Transactional`은 스프링 AOP 프록시가 동작한다. 흐름은 다음과 같다:

1. 프록시가 메서드 호출을 가로챔
2. 트랜잭션 시작
3. 메서드 실행
4. commit / rollback
5. 트랜잭션 종료 → Connection 반환

즉, **`bizLogic2()`도 호출될 때마다 독립된 트랜잭션을 만든다.**
따라서 **Connection도 별도 트랜잭션에서 빌려오게 된다.**

---

# 결론: 둘은 서로 다른 트랜잭션 → 서로 다른 Connection을 사용

두 방식 모두 “트랜잭션 경계가 메서드 단위로 명확하게 설정”되어 있기 때문에:

* 두 메서드는 서로 다른 트랜잭션 범위에서 동작
* 각 트랜잭션은 독립적으로 Connection을 가져옴

따라서 이 둘이 같은 요청에서 호출되더라도 **동일한 Connection을 공유하지 않는다.**

즉,

> bizLogic()이 사용하는 Connection과
> bizLogic2()가 사용하는 Connection은 다르다.

---

# 예외: 동일 Connection을 쓰는 경우는 언제인가?

두 가지 특별한 상황에서만 동일 Connection을 공유할 수 있다.

### 1) 하나의 트랜잭션 안에서 두 메서드를 순서대로 호출할 때

예:

```java
@Transactional
public void outer() {
    bizLogic();   // 내부에서 트랜잭션 새로 시작 → 기본은 PROPAGATION_REQUIRED?
    bizLogic2();  // 외부 트랜잭션 사용
}
```

하지만 `bizLogic()`은 직접 TransactionTemplate을 사용해 **항상 새로운 트랜잭션을 시작**한다.

그래서 이 경우에도 **Connection이 절대로 같아지지 않는다.**
TransactionTemplate은 현재 트랜잭션을 무시하고 별도의 트랜잭션을 만들기 때문이다.

### 2) 둘 다 같은 트랜잭션을 공유하도록 강제로 설정했을 때

예: TransactionTemplate의 전파 레벨을 REQUIRED로 두고, 외부에서 이미 트랜잭션이 존재하는 경우 → 내부 트랜잭션 템플릿이 기존 트랜잭션을 재사용할 수 있다.

하지만 일반적으로 `TransactionTemplate`은 전파 옵션 기본값이 `PROPAGATION_REQUIRED`라서 기존 트랜잭션이 있으면 그걸 쓴다.
그러나 스프링이 지원하는 AOP 트랜잭션과 템플릿 내부 트랜잭션이 충돌하면 상황이 꼬일 수 있다.
실무에서는 템플릿을 중첩 사용하지 않는 것이 일반적이다.

---

# 현재 코드 상황 요약

| 메서드           | 트랜잭션 방식                      | Connection          |
| ------------- | ---------------------------- | ------------------- |
| `bizLogic()`  | TransactionTemplate 독자적 트랜잭션 | 독립적인 Connection     |
| `bizLogic2()` | @Transactional AOP 트랜잭션      | 또 다른 독립적 Connection |

따라서 **둘은 서로 다른 Connection을 사용**한다.

---

더 궁금한 점이 있으면 이어서 설명해줄게.

`@Transactional` 자체는 **인터페이스 수준의 선언**일 뿐이고, 실제로 트랜잭션을 처리하는 구현은 **스프링이 만든 AOP 프록시와 `PlatformTransactionManager` 구현체**가 담당한다.

정확히 어떤 구현체가 사용되는지 단계별로 정리해볼게.

---

# 1. 핵심: 트랜잭션을 실제로 수행하는 것은 `PlatformTransactionManager`

`@Transactional`은 아래 인터페이스의 구현체를 사용한다.

```java
org.springframework.transaction.PlatformTransactionManager
```

구현체는 환경에 따라 달라진다.

대표적인 구현체:

1. **`DataSourceTransactionManager`**

    * JDBC, MyBatis 사용 시
    * DataSource에서 Connection을 가져와 commit/rollback 처리
    * 가장 흔하게 사용됨

2. **`JpaTransactionManager`**

    * JPA(EntityManager) 사용 시
    * JPA의 EntityTransaction과 JDBC Connection을 함께 관리
    * 스프링 부트에서 JPA 의존성만 넣으면 자동 설정되는 기본값

3. **HibernateTransactionManager** (예전)

    * Hibernate Session 직접 관리 시 (지금은 JpaTransactionManager가 많음)

4. **JtaTransactionManager**

    * 여러 리소스(DB, MQ 등)를 하나의 글로벌 트랜잭션으로 묶을 때
    * 특별한 환경에서만 사용됨

즉,
프로젝트가 **JDBC 기반이냐, JPA 기반이냐**에 따라 트랜잭션 매니저 구현체가 달라진다.

---

# 2. `@Transactional`은 누구에게 요청하는가? → 프록시

`@Transactional`이 붙은 메서드는 직접 실행되지 않는다.

스프링이 **프록시 객체**를 만들어서 다음 순서로 실행한다:

1. 프록시가 메서드 호출을 가로챔
2. `TransactionInterceptor`가 동작
3. 등록된 `PlatformTransactionManager`를 통해 트랜잭션 시작
4. 실제 메서드 실행
5. 트랜잭션 commit/rollback

구조적으로 보면 이렇게 되어 있다:

```
@Transactional
   ↓
AOP Proxy
   ↓
TransactionInterceptor
   ↓
PlatformTransactionManager (구현체)
```

---

# 3. 자동으로 선택되는 구현체 예시

### Spring Boot + Spring Data JPA 환경

자동으로 다음이 bean 등록됨:

```java
JpaTransactionManager
```

### Spring Boot + MyBatis 또는 순수 JDBC 환경

자동으로:

```java
DataSourceTransactionManager
```

만약 둘 이상이 존재하면 스프링이 우선순위를 결정하거나, 직접 명시해야 한다.

---

# 4. 실제로 어떤 구현체가 사용되는지 확인하는 방법

애플리케이션 실행 시 로그에서 아래와 같은 문구가 출력된다.

예) JPA 환경:

```
Creating bean with name 'transactionManager' of type JpaTransactionManager
```

예) JDBC 환경:

```
Creating bean with name 'transactionManager' of type DataSourceTransactionManager
```

또는 직접 주입받아서 확인도 가능하다:

```java
@Autowired
PlatformTransactionManager txManager;

@PostConstruct
public void check() {
    System.out.println(txManager.getClass());
}
```

---

# 요약

`@Transactional`이 사용하는 트랜잭션 구현체는 다음 중 하나이며, 환경에 따라 결정된다.

* JDBC: `DataSourceTransactionManager`
* JPA: `JpaTransactionManager`
* JTA: `JtaTransactionManager`

즉,
`@Transactional` 자체는 구현체가 없고, **프록시 + TransactionInterceptor + PlatformTransactionManager 구현체** 조합으로 동작한다.

---

원하면 지금 프로젝트 설정을 기준으로 실제로 어떤 트랜잭션 매니저가 사용되는지 함께 추적해줄게.

정확하게 이해하려면 **AOP 프록시가 어디에 붙는지**, 그리고 **그걸 어떻게 확인하는지**를 분리해서 설명하는 것이 좋다.

---

# 1. AOP 프록시는 “클래스 단위로” 생성된다

`@Transactional`이 클래스에 붙었든, 메서드에 붙었든 **스프링은 해당 Bean 전체를 프록시로 감싼다.**

즉,

```java
@Service
@RequiredArgsConstructor
public class MemberService3 { ... }
```

이 클래스 안에 **하나라도** `@Transactional`이 있는 메서드가 있으면,
스프링은 이 클래스를 빈으로 만들 때 **Proxy 객체를 등록한다.**

* "메서드 단위 AOP"처럼 보이지만 사실 **프록시는 클래스 단위로 생성**된다.
* 프록시가 생성되면 그 객체는 **클래스의 모든 public 메서드 호출을 가로챌 수 있다.**

따라서 지금 코드에서도 `MemberService3` 자체가 프록시 객체로 등록된다.

---

# 2. AOP 프록시 적용 확인 방법 (실전)

## 방법 1: `getClass()` 출력

가장 단순하고 정확한 방법.

```java
@Service
@RequiredArgsConstructor
public class MemberService3 {
    @PostConstruct
    public void checkProxy() {
        System.out.println("MemberService3 class = " + this.getClass());
    }
}
```

출력 예시:

* JDK 동적 프록시

  ```
  class com.sun.proxy.$Proxy78
  ```
* CGLIB 프록시

  ```
  class com.example.MemberService3$$EnhancerBySpringCGLIB$$a1b2c3
  ```

스프링 부트를 기본 설정으로 쓰면 일반적으로 **CGLIB 프록시**가 나온다.

여기에서 원형 클래스가 아닌 `$$EnhancerBySpringCGLIB` 같은 형태라면 **프록시 적용됨**이다.

---

## 방법 2: ApplicationContext 에서 조회해보기

```java
@Autowired
ApplicationContext context;

@PostConstruct
public void checkProxy() {
    Object bean = context.getBean(MemberService3.class);
    System.out.println(bean.getClass());
}
```

이것도 같은 결과를 준다.

---

## 방법 3: AOP 로깅 옵션 활성화

`application.properties`에 추가

```
logging.level.org.springframework.aop=DEBUG
logging.level.org.springframework.transaction=TRACE
```

실행하면 이런 로그가 보인다.

```
Creating transactional proxy for [MemberService3]
```

이 문구가 나오면 트랜잭션 AOP 프록시가 적용된 것이다.

---

# 3. 메서드에 붙든 클래스에 붙든 프록시는 동일하게 동작하나, 트랜잭션 전파 범위만 다르다

### 클래스에 @Transactional

전체 public 메서드가 트랜잭션 적용 대상.

```java
@Transactional
public class MemberService3 {
    public void a() {} // 트랜잭션 O
    public void b() {} // 트랜잭션 O
}
```

### 메서드에 @Transactional

그 메서드만 트랜잭션 적용 대상.

즉, **프록시는 클래스 단위**,
**트랜잭션 적용 여부는 메서드 단위**이다.

---

# 4. 내부 호출(self-invocation)에서는 트랜잭션이 안 걸리는 이유도 여기 때문

프록시는 “외부에서 호출될 때”만 동작한다.

즉:

```java
public void outer() {
    inner(); // 프록시를 타지 않음 → 트랜잭션 적용 안 됨
}
```

이런 유명한 이슈도 **프록시 구조 때문에 발생**한다.

---

# 요약

1. `@Transactional`이 하나라도 있으면 **해당 클래스 전체가 AOP 프록시로 감싸진다**
2. 프록시 여부 확인 → `this.getClass()`를 출력하면 된다
3. 클래스에 붙든 메서드에 붙든 **프록시 생성 방식은 동일**,
   차이는 “트랜잭션을 어떤 메서드에 적용할 것인가”
4. 내부 호출에서는 트랜잭션이 적용되지 않는 이유도 프록시 구조 때문이다

---

필요하면 "왜 내부 호출 시 트랜잭션이 적용되지 않는지"를 그림으로 설명해줄게.

결론부터 말하면 **bizLogic()은 프록시를 타지만, 트랜잭션 AOP는 적용되지 않는다.**

이유를 단계별로 정확하게 설명해볼게.

---

# 1. MemberService3 전체가 프록시 Bean이다

`@Transactional`이 클래스의 어떤 메서드라도 존재하면,
스프링은 **MemberService3 전체를 프록시로 등록**한다.

따라서 아래 두 메서드는 모두 프록시 객체를 통해 호출된다.

```java
public void bizLogic()     // @Transactional 없음
public void bizLogic2()    // @Transactional 있음
```

즉, **메서드 자체가 프록시를 거치는지 여부만 보면 둘 다 YES**다.

---

# 2. 하지만 “프록시를 거친다 = 트랜잭션 AOP가 적용된다”는 뜻이 아니다

프록시가 하는 작업은 다음과 같다:

1. 메서드 호출 차단
2. 적용 가능한 Advisor(AOP Pointcut)를 확인
3. 해당 Advisor가 적용되면 Advice(TransactionInterceptor 등)를 실행

즉, 프록시는 항상 호출을 가로채지만, 그 안에서 트랜잭션을 걸지 여부는 **Advisor의 Pointcut 조건**에 따라 다르다.

---

# 3. bizLogic()은 @Transactional이 없어서 Advisor 매칭이 안 된다

Transactional AOP는 다음 조건을 만족하는 메서드에만 적용된다.

* @Transactional 이 클래스 또는 해당 메서드에 존재
* public 메서드

bizLogic()은 아래처럼 애노테이션이 없다.

```java
public void bizLogic() {
    ...
}
```

따라서 프록시는 이렇게 판단한다:

* “프록시로 가로채긴 했지만, 트랜잭션 Advisor 적용 대상은 아님 → 트랜잭션 열지 않음”

결과:

* **프록시는 거친다**
* **트랜잭션 Advice는 실행되지 않는다**

---

# 4. bizLogic2()는 Advisor 매칭 → 트랜잭션 열림

```java
@Transactional
public void bizLogic2()
```

이 메서드는 프록시가 트랜잭션 Advisor 매칭에 성공한다.

그래서:

* 프록시가 트랜잭션을 시작
* bizLogic2() 실행
* commit/rollback 처리

---

# 5. 정리 표

| 메서드         | 프록시를 통과? | 트랜잭션 AOP 동작? | Connection 생성 방식                   |
| ----------- | -------- | ------------ | ---------------------------------- |
| bizLogic()  | 예        | 아니오          | TransactionTemplate 내부 로직이 트랜잭션 시작 |
| bizLogic2() | 예        | 예            | @Transactional AOP가 트랜잭션 시작        |

---

# 6. 왜 bizLogic()도 프록시인가?

스프링은 클래스 단위로 프록시를 생성하기 때문이다.

프록시는 모든 public 메서드를 intercept하지만,
적용되는 AOP Advice(TransactionInterceptor)는 **조건(@Transactional)**에 따라 선택적으로 실행된다.

---

# 최종 결론

### bizLogic()은 프록시를 거친다.

하지만 **트랜잭션 AOP는 적용되지 않는다**
→ 즉, Connection을 열거나 commit/rollback 하지 않는다.
→ 대신 TransactionTemplate이 직접 트랜잭션을 시작한다.

---

원하면 직접 프록시 여부를 확인할 수 있는 코드도 작성해줄게.
