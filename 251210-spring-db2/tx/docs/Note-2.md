당신이 궁금한 부분은 **“프록시가 어떻게 @Transactional 유무를 판단하고, 트랜잭션을 적용하거나 생략하는지”**에 대한 **프록시 내부 동작 흐름**이다.

아래는 스프링 트랜잭션 프록시가 메서드 하나를 호출받았을 때 실제로 어떤 과정을 거치는지 자세히 설명한 것이다.

---

# 1. 프록시 구조를 먼저 이해하자

스프링에서 @Transactional을 적용하면
스프링은 원본 객체(target)를 감싸는 **트랜잭션 프록시 객체(proxy)**를 만든다.

이 프록시 객체는 내부적으로 다음 ‘체인’을 가진다.

```
[프록시] → [TransactionInterceptor] → [원본 객체]
```

즉, 프록시는 직접 트랜잭션을 제어하지 않는다.
트랜잭션 관련 로직은 `TransactionInterceptor`가 담당한다.

---

# 2. 메서드 호출 흐름 (중요)

외부 코드에서

```java
callService.external();
```

을 호출하면 실제로는 다음 흐름으로 진행된다:

1. **프록시가 메서드 호출을 가로챔**

```
proxy.invoke(method=external)
```

2. 프록시는 호출된 메서드에 적용될 Advice를 찾음
   (이때 @Transactional이 붙어 있으면 TransactionInterceptor가 매칭됨)

3. external()에는 @Transactional이 없음
   → 적용할 필요가 없다고 판단

4. 프록시는 트랜잭션 시작 로직을 실행하지 않음
   즉, TransactionInterceptor를 건너뜀

5. 프록시는 원본 객체의 메서드를 그대로 호출

```
target.external()
```

6. 메서드 수행 후에도 트랜잭션/커밋 로직 없음

결과적으로 트랜잭션 **활성 = false**

---

# 3. 그럼 internal()은 어떻게 동작하는가?

외부에서 이렇게 호출하면

```java
callService.internal();
```

1. 프록시가 가로챔
2. @Transactional 존재 → TransactionInterceptor 매칭
3. TransactionInterceptor가 트랜잭션 시작
4. 트랜잭션 안에서 target.internal() 실행
5. target.internal() 끝나면 commit 또는 rollback
6. 종료

결과: 트랜잭션 활성 = true

---

# 4. 내부 호출 시 어떻게 되는가?

external() 내부에서 이렇게 호출한다고 해보자:

```java
internal();
```

여기서 중요한 점:

### 내부 호출은 프록시를 거치지 않는다.

### 즉, 직접 target.internal() 호출과 동일하다.

따라서:

* 프록시는 실행되지 않음
* TransactionInterceptor도 실행되지 않음
* @Transactional도 무시됨

그 결과 internal()이라도 트랜잭션 없음.

---

# 5. 실제 코드 수준으로 보면?

스프링 프록시가 하는 핵심 로직(개념화된 형태):

```java
public Object invoke(Method method, Object[] args) {
    // 1. 이 메서드에 @Transactional이 있는가?
    TransactionAttribute txAttr = txAttributeSource.getTransactionAttribute(method, targetClass);

    if (txAttr != null) {
        // 2. 트랜잭션 시작
        TransactionStatus status = txManager.getTransaction(txAttr);

        try {
            Object result = method.invoke(target, args);
            txManager.commit(status);
            return result;
        } catch (Exception e) {
            txManager.rollback(status);
            throw e;
        }

    } else {
        // 3. 트랜잭션 필요 없음 → 바로 target 호출
        return method.invoke(target, args);
    }
}
```

여기서 중요한 코드:

```java
if (txAttr != null) {
    // 트랜잭션 로직 실행
} else {
    // 트랜잭션 안 함
}
```

이 로직이 바로
“프록시는 이 메서드에 트랜잭션을 적용할 필요가 없네?”
→ "바로 대상 메서드 실행"
이라는 동작이다.

external() 메서드를 호출하면

* txAttr = null
  그러므로 바로 원본 객체 메서드를 호출해서 트랜잭션 없이 실행되는 것이다.

---

# 6. 다시 핵심을 요약하면

### 프록시는 항상 호출을 가로챈다.

하지만

* @Transactional이 붙은 메서드인지
* 클래스 레벨 설정이 있는지

등을 확인하고

* 필요하면 TransactionInterceptor를 실행 → 트랜잭션 시작
* 필요 없으면 바로 target 메서드 호출 → 트랜잭션 없음

이런 식이다.

그리고
**내부 호출은 프록시 자체를 건너뛰므로 아예 이 판단 절차 자체가 생략된다.**

---

# 7. 따라서 이런 현상이 생긴다

| 호출 위치                         | 프록시 가로챔? | @Transactional 검사?                     | 트랜잭션 실행? |
| ----------------------------- | -------- | -------------------------------------- | -------- |
| 외부에서 internal() 호출            | 예        | 예                                      | 예        |
| 외부에서 external() 호출            | 예        | external에 @Transactional 없음 → 트랜잭션 안 함 | 아니오      |
| external() 내부에서 internal() 호출 | 아니오      | 검사 X                                   | 아니오      |

---

원한다면

* 내부 호출 문제를 해결하는 전략(서비스 분리, 자기 프록시 호출 등)을 다시 정리하거나,
* 트랜잭션 프록시의 call stack을 그림으로 시각화해서 보여줄 수도 있다.

당신 질문의 핵심은 두 가지다.

1. **프록시 → 트랜잭션 인터셉터 → 원본 객체** 라는 호출 흐름이
   “메서드가 호출되지 않은 순간에도 실행되는 구조인가?”
2. **프록시 객체는 언제 만들어지는가?**

하나씩 정확하게 설명해줄게.

---

# 1. 프록시 → 인터셉터 → 원본 객체 흐름은 "메서드 호출 시점에만" 실행된다

스프링은 프록시 객체를 만들어서 **스프링 빈으로 등록해 둔다**.
하지만 프록시가 실제로 동작하는 순간은 “메서드를 호출할 때”다.

즉,

* 프록시가 만들어져 있다고 해서
  **항상 프록시 → 인터셉터 → 타깃**이 자동 실행되는 게 아니다.
* 이 체인은 **메서드가 호출될 때만** 발동한다.

예를 들어:

```java
callService.internal();
```

이 순간에만 다음 흐름이 실행된다.

```
프록시.invoke()  
→ TransactionInterceptor.invoke()  
→ target.internal()
```

아무 것도 호출하지 않으면

* 프록시는 그냥 객체처럼 존재할 뿐 동작하지 않는다.
* 인터셉터도 동작하지 않는다.
* 트랜잭션도 시작되지 않는다.

따라서

### “프록시의 동작은 오직 메서드 호출이 이루어질 때만 실행된다.”

---

# 2. 프록시 객체는 언제 만들어지는가?

프록시는 **스프링 컨테이너가 빈을 생성할 때** 만들어진다.

정확한 시점:

### 빈 생성 → 빈 후처리기(BeanPostProcessor) → AOP 프록시 적용

조금 더 단계별로 보면:

1. 개발자가 만든 원본 객체(CallService)가 먼저 생성된다.
2. 스프링은 BeanPostProcessor 중 AOP 관련 후처리기(특히 `AnnotationAwareAspectJAutoProxyCreator`)를 실행한다.
3. 이 후처리기가 해당 빈에 @Transactional 같은 AOP 적용 대상이 있는지 조사한다.
4. AOP 대상이라고 판단되면
   **원본 객체를 감싸는 프록시 객체를 만들고**
   그 프록시를 스프링 빈으로 등록한다.

따라서 컨테이너에는 이렇게 저장된다.

```
CallService$$SpringCGLIB$$0  ← 프록시
CallService                  ← 프록시가 감싼 진짜 객체(컨테이너에서 감춰짐)
```

controller나 다른 서비스가 CallService를 주입받으면
항상 **프록시 객체**가 주입된다.

---

# 3. 그래서 트랜잭션이 가능해지는 이유

스프링 DI 구조에서

```
controller → callService
```

이때 주입되는 callService는 항상 **프록시 객체**다.

프록시 덕분에

* 메서드 호출을 “가로챌 수 있고”
* 트랜잭션 적용 여부를 검토할 수 있고
* 필요한 경우 TransactionInterceptor를 실행할 수 있다.

---

# 4. 내부 호출에서 트랜잭션이 안 되는 이유를 다시 보면

외부에서 호출할 때는:

```
controller → [프록시] → [Interceptor] → target.method()
```

그러나 내부 메서드 호출은:

```
target.external() 내부에서 → target.internal()
```

여기서는 프록시가 개입할 기회가 없다.

프록시는 메서드를 **“가로채야”** 하는데,
내부 호출은 가로챌 수 있는 순간이 없다.

그래서 internal()에 @Transactional이 있어도
트랜잭션이 발동하지 않는 것이다.

---

# 5. 전체 요약

### 1) 프록시는 메서드 호출 시에만 동작한다

프록시 → 인터셉터 → 타깃 흐름은 호출할 때만 실행된다.

### 2) 프록시는 스프링 컨테이너가 빈을 만들 때 생성된다

BeanPostProcessor(AOP 후처리기)가 생성하고 등록한다.

### 3) 그래서 외부 호출은 트랜잭션이 되고, 내부 호출은 안 된다

외부 호출은 프록시 개입
내부 호출은 프록시 건너뜀

---

원한다면

* 프록시 생성 시점을 디버그로 직접 확인하는 방법
* CGLIB 프록시가 실제로 어떤 클래스를 만드는지
* AOP 후처리기 흐름(call stack)

이런 것도 단계별로 더 자세히 설명해줄 수 있어.

아래 상황을 정확히 구분하면 왜 `false`, `true`가 나오는지 깔끔하게 이해할 수 있다.

---

## 1. `@PostConstruct` + `@Transactional` → 트랜잭션 적용 안 됨 (`false`가 나오는 이유)

`@PostConstruct`가 호출되는 시점은 **빈 객체가 생성되고 의존성 주입이 끝난 직후**다.
하지만 **트랜잭션 프록시가 적용되는 시점은 그보다 뒤**다.

### 실행 순서 흐름

1. 스프링이 `Hello` 객체를 생성한다. (아직 프록시 X)
2. `@PostConstruct` 메서드 `init1()`을 **직접 호출**한다.

    * 이때는 트랜잭션 프록시가 생성되기 전이므로 AOP 적용도 불가능
3. 따라서 `@Transactional`은 완전히 무효
4. 결과적으로 `isActualTransactionActive()` → `false`

즉, **프록시가 생성되기 전에 호출된 메서드이기 때문에 트랜잭션 적용 불가**다.

---

## 2. `@EventListener(ApplicationReadyEvent)` + `@Transactional` → 트랜잭션 적용됨 (`true`가 나오는 이유)

`ApplicationReadyEvent`는 **스프링 컨테이너가 완전히 초기화되고, 모든 빈 생성 & 프록시 적용이 끝난 뒤**에 발생한다.

### 실행 순서 흐름

1. 모든 빈 생성 → 트랜잭션 프록시 생성 완료
2. 스프링 애플리케이션 기동 완료
3. `ApplicationReadyEvent` 발생
4. 이벤트를 듣는 `init2()`는 **프록시를 통해 호출됨**
5. 프록시는 `@Transactional`을 감싸므로 트랜잭션이 시작된다
6. 따라서 `isActualTransactionActive()` → `true`

즉, **프록시가 완전히 준비된 이후 호출되기 때문에 트랜잭션이 정상 동작**한다.

---

## 핵심 요약

| 메서드                                            | 언제 호출됨          | 호출 주체                 | 프록시 적용 여부 | 트랜잭션 활성화 |
| ---------------------------------------------- | --------------- | --------------------- | --------- | -------- |
| `init1(@PostConstruct)`                        | 빈 생성 직후         | 컨테이너가 직접 호출           | 적용 불가     | false    |
| `init2(@EventListener(ApplicationReadyEvent))` | 애플리케이션 완전 초기화 후 | 이벤트 디스패처 → 프록시를 통해 호출 | 적용됨       | true     |

---

원하면 이 두 상황이 AOP 관점에서 어떤 호출 스택을 타는지도 더 깊게 설명해 줄 수 있다.
