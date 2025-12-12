결론부터 말하면 **Config를 바꿔야 합니다.**
그리고 이유는 “repository에 `@Transactional`이 붙어 있는데도 왜 안 되느냐”를 정확히 이해하면 아주 명확해집니다.

---

## 1. 핵심 원인 한 줄 요약

> **`@Transactional`이 붙은 Repository가
> Spring이 만든 프록시 빈이 아니기 때문에
> 트랜잭션이 시작되지 않는다**

지금 구조에서는 이 한 문장으로 끝입니다.

---

## 2. 지금 구조를 정확히 보면

### TestConfiguration

```java
@Bean
MemberService memberService() {
    return new MemberService(
        new MemberRepository(em),
        new LogRepository(em)
    );
}
```

### Repository

```java
public class MemberRepository {

    @Transactional
    public void save(Member member) {
        em.persist(member);
    }
}
```

여기서 **치명적인 부분**은:

```java
new MemberRepository(em)
```

입니다.

---

## 3. 왜 Repository의 `@Transactional`이 무시되는가

Spring 트랜잭션은:

* 어노테이션을 읽어서 동작 ❌
* **프록시를 통해서만 동작 ⭕**

즉, 반드시 다음 조건이 필요합니다.

1. Repository가 Spring Bean이어야 한다
2. Spring이 프록시로 감싸야 한다
3. 그 프록시를 통해 호출돼야 한다

하지만 지금은:

* Repository가 `@Repository`도 아님 (있어도 의미 없음)
* `@Bean`으로도 등록 안 됨
* 그냥 `new` 로 생성한 **일반 객체**

그래서:

* `@Transactional`이 있어도
* Spring은 전혀 관여하지 않음
* 트랜잭션 시작 안 됨
* EntityManager는 트랜잭션 없음 → 예외

---

## 4. 실험 목적에 따른 선택지

### 선택 1. “Repository 트랜잭션” 구조를 유지하고 싶다면 (Config 수정)

**Repository를 Spring Bean으로 등록해야 합니다.**

```java
@TestConfiguration
static class Config {

    @PersistenceContext
    EntityManager em;

    @Bean
    MemberRepository memberRepository() {
        return new MemberRepository(em);
    }

    @Bean
    LogRepository logRepository() {
        return new LogRepository(em);
    }

    @Bean
    MemberService memberService(
            MemberRepository memberRepository,
            LogRepository logRepository
    ) {
        return new MemberService(memberRepository, logRepository);
    }
}
```

이제:

* Repository는 Spring Bean
* `@Transactional` 적용됨
* 트랜잭션 정상 시작

이 상태에서 **Repository 레벨 트랜잭션 실험**이 가능합니다.

---

### 선택 2. 가장 정석적인 구조 (권장)

```java
@Repository
public class MemberRepository {
    ...
}
```

```java
@Service
@Transactional
public class MemberService {
    ...
}
```

그리고 Config에서는 **아무 것도 만들지 않음**.

이게 실무 표준입니다.

---

## 5. Repository에 @Transactional만 두는 구조의 한계

실험해 보면 바로 느껴질 겁니다.

* 여러 Repository 호출 시 트랜잭션 경계가 쪼개짐
* 전파 옵션 의미 약해짐
* 도메인 로직 파편화

그래서 실무에서는:

> Repository에는 보통 `@Transactional(readOnly = true)` 정도만 두고
> **쓰기 트랜잭션은 Service에서 시작**

합니다.

---

## 6. 중요한 추가 포인트 (매우 중요)

### 내부 호출 문제

만약 Repository 안에서:

```java
@Transactional
public void save() {
    ...
    saveLog(); // 같은 클래스 내부 호출
}

@Transactional(propagation = REQUIRES_NEW)
public void saveLog() {
}
```

이건 **절대 동작하지 않습니다**.
이유는 프록시를 거치지 않기 때문입니다.

REQUIRES_NEW 실험을 하려면:

* **다른 Bean**
* **다른 프록시**

여야 합니다.

---

## 7. 정리

* 지금 구조에서는 `@Transactional`이 붙어 있어도 무효
* 이유는 Repository가 Spring Bean이 아니기 때문
* 실험하려면 Config에서 Repository를 Bean으로 등록해야 함
* 트랜잭션 전파 실험은 **반드시 프록시 경계를 넘겨야 함**

---

지금까지 질문 흐름을 보면
다음으로 딱 맞는 실험 주제는 이겁니다.

> “Repository에 붙은 REQUIRES_NEW는
> Service 트랜잭션과 어떻게 상호작용하는가”

그걸 로그로 풀어보면
Spring 트랜잭션 구조가 거의 완전히 보입니다.

