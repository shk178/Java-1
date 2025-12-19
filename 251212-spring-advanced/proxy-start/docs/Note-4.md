1. 스프링의 프록시 팩토리를 사용하면 어떤 문제를 해결하며 프록시를 편리하게 생성할 수 있을까요?

핵심 비즈니스 로직 변경 없이 부가 기능 추가

B
객체 생성 시 순환 참조 해결

C
동시성 문제 해결

D
데이터베이스 연결 관리



해설
프록시 팩토리는 핵심 코드 수정 없이 프록시를 통해 부가 기능을 넣는 문제를 추상화하여 해결해 줍니다. 핵심은 원본 코드 변경 없이 기능 확장이죠.
2. 스프링 AOP에서 Advice의 주된 역할은 무엇일까요?

A
부가 기능 적용 대상 필터링

부가 기능 로직 구현

C
프록시 적용 순서 결정

D
원본 객체 직접 호출



해설
Advice는 프록시가 호출할 부가 기능 자체의 로직을 담고 있습니다. 즉, 실제 어떤 일을 할지를 정의하는 부분이죠. 필터링은 Pointcut의 역할입니다.
3. 스프링 AOP에서 Pointcut의 주된 역할은 무엇일까요?

A
부가 기능 로직 구현

B
프록시 객체 생성

부가 기능 적용 대상 필터링

D
Advice와 Target 연결



해설
Pointcut은 이름 그대로 '어느 시점'에 부가 기능을 적용할지 결정하는 필터링 역할을 합니다. 주로 클래스나 메소드 이름, 애노테이션 등으로 대상을 지정하죠.
4. 스프링 AOP에서 Advisor는 무엇으로 구성될까요?

A
하나의 Pointcut과 여러 개의 Advice

B
여러 개의 Pointcut과 하나의 Advice

하나의 Pointcut과 하나의 Advice

D
여러 개의 Pointcut과 여러 개의 Advice



해설
Advisor는 부가 기능(Advice)을 '어디(Pointcut)'에 적용할지에 대한 정보를 한데 묶어 놓은 것입니다. 그래서 Pointcut 하나와 Advice 하나로 구성됩니다.
5. 클라이언트가 프록시를 호출했을 때, Advice 로직을 적용하기 전에 Pointcut이 어떤 역할을 할까요?

A
Advice 로직을 즉시 실행

B
원본 객체 메소드를 직접 호출

해당 메소드에 Advice 적용 가능 여부 판단

D
프록시 객체 재생성



해설
클라이언트가 프록시를 호출하면, 프록시는 Advisor에게 해당 메소드에 Advice를 적용할지 먼저 물어봅니다. 이때 Advisor의 Pointcut이 그 메소드가 적용 대상인지 판단하죠.
6. ProxyFactory가 대상 객체에 인터페이스가 있는 경우 기본적으로 어떤 프록시 기술을 사용하며 프록시를 생성할까요?

A
CGLIB

B
AspectJ

JDK Dynamic Proxy

D
Static Proxy



해설
ProxyFactory는 대상 객체의 정보를 보고 프록시 기술을 자동으로 선택합니다. 인터페이스가 있다면 JDK 동적 프록시를 사용합니다.
7. ProxyFactory에 `setProxyTargetClass(true)` 옵션을 설정하면 어떤 효과가 있을까요?

인터페이스 유무와 관계없이 CGLIB 사용

B
항상 JDK Dynamic Proxy 사용

C
프록시 생성 과정 로깅

D
Pointcut 필터링 무시



해설
`proxy-target-class` 옵션을 true로 설정하면, ProxyFactory는 인터페이스 존재 여부와 상관없이 대상 클래스를 기반으로 CGLIB 프록시를 생성하도록 강제합니다.
8. `org.aopalliance.intercept.MethodInterceptor`를 구현한 Advice에서 다음 Advice 또는 실제 대상 객체의 메소드를 호출하여 실행 흐름을 이어가는 코드는 무엇일까요?

A
invocation.getMethod()

B
invocation.getArgs()

C
invocation.getThis()

invocation.proceed()



해설
Advice의 `methodInterceptor` 메소드 내에서 `invocation.proceed()`를 호출해야만 다음 단계의 로직(다른 Advice 또는 최종 Target 메소드)이 실행됩니다. 부가 기능 로직 전후로 호출할 수 있죠.
9. 하나의 대상 객체에 여러 개의 Advisor를 적용해야 할 때, 스프링 AOP는 성능 최적화를 위해 일반적으로 몇 개의 프록시를 생성할까요?

A
적용된 Advisor 수만큼

B
항상 0개 (프록시 없음)

대상 객체당 1개

D
Pointcut 종류 수만큼



해설
스프링 AOP는 하나의 대상 객체에 여러 Advisor가 있더라도 프록시를 여러 개 만들지 않고, 단 하나의 프록시가 여러 Advisor를 관리하도록 최적화합니다. 프록시는 대상 객체당 한 개만 생성됩니다.
10. ProxyFactory를 이용한 수동 프록시 설정 방식이 컴포넌트 스캔 환경에서 바로 적용하기 어려운 주된 이유는 무엇일까요?

A
컴포넌트 스캔이 인터페이스만 검색해서

스프링 컨테이너에 프록시가 아닌 실제 객체가 빈으로 등록되어서

C
ProxyFactory가 컴포넌트 스캔보다 먼저 실행되어서

D
Advice와 Pointcut 설정이 불가능해서



해설
컴포넌트 스캔으로 대상 객체가 빈으로 등록될 때, 스프링 컨테이너에는 프록시가 아닌 원본 객체 인스턴스가 직접 등록됩니다. 따라서 프록시 객체를 대신 등록하는 별도의 과정이 필요하죠.

이 코드는 **Spring의 BeanPostProcessor를 이용해서 특정 패키지에 속한 빈들을 자동으로 프록시로 감싸는 역할**을 합니다. 주로 **AOP(공통 기능: 로그, 트랜잭션, 성능 측정 등)** 를 적용하기 위해 쓰입니다.

아래에서 흐름대로 설명할게요.

---

## 전체 한 줄 요약

> 스프링 컨테이너에 등록되는 빈 중에서 `basePackage`로 시작하는 클래스만 골라서, `advisor`를 적용한 프록시 객체로 바꿔치기하는 코드입니다.

---

## 클래스 구조 설명

```java
@RequiredArgsConstructor
public class LogTraceProxyPostProcessor implements BeanPostProcessor {
```

* `BeanPostProcessor`

    * **스프링 빈이 생성된 직후 / 초기화 직후에 가로채서 가공할 수 있는 확장 포인트**
* `@RequiredArgsConstructor`

    * `final` 필드인 `basePackage`, `advisor`를 생성자로 주입받음

---

## 필드 설명

```java
private final String basePackage;
private final Advisor advisor;
```

* `basePackage`

    * 프록시 적용 대상이 될 패키지 기준
    * 예: `"hello.proxy.app"`
* `advisor`

    * AOP의 핵심 구성 요소
    * **Pointcut + Advice**
    * 어떤 메서드에 어떤 부가 기능을 적용할지 정의

---

## 핵심 메서드: postProcessAfterInitialization

```java
@Override
public Object postProcessAfterInitialization(Object bean, String beanName)
```

* 빈이 **초기화 완료된 직후** 호출됨
* 반환값이 실제로 컨테이너에 등록될 객체

    * 원본 빈을 그대로 반환할 수도 있고
    * **프록시로 교체할 수도 있음**

---

## 동작 단계별 설명

### 1. 현재 생성 중인 빈 정보 출력

```java
System.out.println("beanName = " + beanName);
System.out.println("bean.getClass() = " + bean.getClass());
```

* 디버깅용 로그
* 어떤 빈이 후처리 대상인지 확인

---

### 2. 패키지 기준으로 필터링

```java
String packageName = bean.getClass().getPackageName();
if (!packageName.startsWith(basePackage)) {
    return bean;
}
```

* 빈 클래스의 패키지가 `basePackage`로 시작하지 않으면

    * **아무 처리 없이 원본 빈 반환**
* 즉, 모든 빈에 프록시를 씌우지 않고 **선별 적용**

---

### 3. 프록시 생성

```java
ProxyFactory proxyFactory = new ProxyFactory(bean);
proxyFactory.addAdvice(advisor);
```

* `ProxyFactory`

    * Spring AOP의 프록시 생성 도구
* 생성 방식

    * 인터페이스 있으면 JDK 동적 프록시
    * 없으면 CGLIB 프록시
* `advisor`를 추가

    * 이 advisor 안에 로그 트레이스 같은 부가기능이 들어 있음

---

### 4. 프록시 객체 생성 및 반환

```java
Object proxy = proxyFactory.getProxy();
return proxy;
```

* 실제 빈 대신 **프록시 객체를 스프링 컨테이너에 등록**
* 이후 의존성 주입 시 이 프록시가 주입됨
* 메서드 호출 → advisor → 실제 객체 순으로 실행됨

---

## 결과적으로 발생하는 일

* 스프링이 빈을 생성
* 이 BeanPostProcessor가 가로챔
* 지정한 패키지의 빈이면

    * 원본 빈 대신 **AOP가 적용된 프록시 빈으로 교체**
* 개발자는 AOP 설정을 직접 각 빈에 안 해도 됨

---

## 이 코드가 필요한 이유

* `@Aspect` 기반 AOP를 쓰지 않고
* **프로그래밍 방식으로 AOP를 적용**하고 싶을 때
* 또는

    * 프록시 생성 시점을 명확히 제어하고 싶을 때
    * 특정 조건(패키지, 클래스)에만 적용하고 싶을 때

---

## 대표적인 사용 예

* 로그 추적 (LogTrace)
* 트랜잭션
* 성능 측정
* 인증/인가

---

원하면

* 이 코드가 `@Aspect` 방식과 어떻게 다른지
* `Advisor`, `Advice`, `Pointcut` 관계
* 실행 흐름을 그림처럼 단계별로 설명

이 중에서 더 자세히 설명해줄 수도 있어.

1. 빈 후처리기는 스프링 빈 생명주기 중 주로 언제 동작하며, 어떤 처리를 할 수 있나요?

A
객체 생성 전 - 속성 값 주입

객체 생성 후, 컨테이너 등록 전 - 객체 조작/교체

C
객체 등록 후, 사용 준비 전 - 의존 관계 주입

D
객체 사용 완료 후 - 자원 해제



해설
빈 후처리기는 스프링이 객체를 생성한 후, 이를 빈 컨테이너에 등록하기 직전에 동작해요. 이때 객체 내용을 변경하거나 다른 객체로 완전히 바꿀 수 있답니다.
2. 빈 후처리기가 스프링이 생성한 원본 빈 객체에 대해 할 수 있는 강력한 능력은 무엇일까요?

A
객체의 특정 메소드를 호출하는 것

B
객체의 특정 속성 값을 설정하는 것

원본 객체를 완전히 다른 객체로 교체하는 것

D
객체의 초기화 로직을 건너뛰는 것



해설
빈 후처리기는 단순히 객체의 속성을 바꾸거나 메소드를 호출하는 것을 넘어, 스프링이 컨테이너에 등록하려는 객체 자체를 다른 객체로 바꿔치기 할 수 있어요.
3. AOP 등에서 프록시 적용 시, 빈 후처리기를 활용하는 방식의 가장 큰 장점은 무엇인가요?

A
프록시 객체 생성 속도가 더 빠릅니다.

B
컴포넌트 스캔 대상 빈에는 적용할 수 없습니다.

프록시 생성 로직을 자동화하고 중앙 집중화할 수 있습니다.

D
오직 특정 타입의 빈에만 적용 가능합니다.



해설
개별 빈마다 프록시 설정을 하는 대신, 빈 후처리기를 통해 프록시 적용 대상을 판단하고 생성을 자동화해서 설정 코드를 줄일 수 있어요.
4. 스프링의 자동 프록시 생성기(빈 후처리기)가 특정 빈에 대해 프록시를 만들지 말지 판단하는 주요 기준은 무엇일까요?

A
해당 빈이 인터페이스를 구현했는지 확인합니다.

B
빈 이름에 특정 키워드가 포함되었는지 확인합니다.

등록된 Advisor들의 Pointcut 조건에 해당 빈이 매칭되는지 확인합니다.

D
빈 클래스에 @Proxy 어노테이션이 붙어있는지 확인합니다.



해설
자동 프록시 생성기는 등록된 모든 Advisor를 찾고, 각 Advisor 안의 Pointcut 조건을 이용해 해당 빈이 프록시 적용 대상인지 결정해요.
5. 하나의 스프링 빈이 여러 개의 등록된 Advisor의 Pointcut 조건에 모두 매칭될 경우, 스프링은 해당 빈에 대해 몇 개의 프록시를 생성할까요?

A
0개

1개

C
매칭된 Advisor 개수만큼

D
빈의 타입에 따라 다릅니다



해설
여러 Advisor의 Pointcut에 매칭되어도 스프링은 하나의 프록시만 만들고, 그 안에 매칭된 모든 Advisor를 포함시켜 효율성을 높여요.