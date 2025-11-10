- 스프링 컨테이너는 크게 두 가지로 나뉘며, 각각 BeanFactory와 ApplicationContext
- 이들은 빈(Bean)의 생성과 관리 방식에서 차이
## 스프링 컨테이너란?
- 스프링 컨테이너는 스프링 프레임워크의 핵심 구성 요소
- 애플리케이션에서 사용하는 객체(Bean)의 생성, 초기화, 의존성 주입, 생명주기 관리를 담당
- 개발자는 객체 생성과 관리에 신경 쓰지 않고 비즈니스 로직에 집중
## 스프링 컨테이너의 주요 종류

| 종류 | 설명 | 로딩 방식 | 주요 기능 |
|------|------|------------|-----------|
| BeanFactory | 가장 기본적인 컨테이너. 빈을 필요할 때 생성함 | 지연 로딩 (Lazy Loading) | DI, 빈 생성 및 관리 |
| ApplicationContext | BeanFactory를 확장한 고급 컨테이너 | 즉시 로딩 (Pre-loading) | 메시지 처리, 이벤트 발행, AOP, 국제화 등 |

## BeanFactory 특징
- `getBean()` 호출 시점에 빈을 생성
- 메모리 효율이 좋음
- 기능이 제한적이라 최근에는 잘 사용되지 않음
## ApplicationContext 특징
- 컨테이너 시작 시점에 모든 빈을 미리 생성
- 다양한 기능 제공: 이벤트 처리, 메시지 리소스, AOP 등
- 대부분의 스프링 프로젝트에서 사용됨
## 용어 정리
- 빈(Bean): 스프링이 관리하는 자바 객체
- DI(Dependency Injection): 의존성 주입. 객체 간의 관계를 설정해주는 방식
- 컨텍스트(Context): 컨테이너와 같은 의미로 사용되며, 빈을 관리하는 환경
## ApplicationContext는 BeanFactory를 확장한 인터페이스
- BeanFactory의 기능을 모두 포함하면서 더 많은 기능을 제공

| 항목 | BeanFactory | ApplicationContext |
|------|-------|--------------|
| 상속 관계 | 최상위 컨테이너 | BeanFactory를 상속 |
| 빈 관리 | 지원 | 지원 |
| 의존성 주입(DI) | 지원 | 지원 |
| 국제화(i18n) | 미지원 | 지원 |
| 이벤트 처리 | 미지원 | 지원 |
| AOP, 메시지 리소스 등 | 미지원 | 지원 |
| 사용 시점 | 메모리 절약이 중요한 경우 | 대부분의 스프링 애플리케이션 |

## 구조적으로 보면
- `ApplicationContext`는 `BeanFactory`를 상속한 하위 인터페이스
- `ApplicationContext`는 `BeanFactory`의 모든 기능을 사용할 수 있음
- 여기에 추가 기능(국제화, 이벤트, AOP 등)을 제공
- 스프링 내부적으로도 `ApplicationContext`를 사용할 때 내부적으로는 `BeanFactory`를 기반으로 동작
## 예시 코드
```java
// BeanFactory 사용
Resource resource = new ClassPathResource("beans.xml");
BeanFactory factory = new XmlBeanFactory(resource);
MyBean bean = (MyBean) factory.getBean("myBean");
// ApplicationContext 사용
ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");
MyBean bean = (MyBean) context.getBean("myBean");
```
## 스프링 컨테이너 생성 시 구성 정보를 인자로 넘긴다.
- AppConfig.class
```java
@Bean
public MemberService memberService() {
    return new MemberServiceImpl(memberRepository());
}
@Bean
public OrderService orderService() {
    return new OrderServiceImpl(
            memberRepository(),
            discountPolicy()
    );
}
@Bean
public MemberRepository memberRepository() {
    return new MemoryMemberRepository();
}
@Bean
public DiscountPolicy discountPolicy() {
    return new RateDiscountPolicy();
}
@Bean
public DiscountPolicy discountPolicy2() {
    return new RateDiscountPolicy();
}
```
- 스프링 컨테이너가 스프링 빈 저장소에 다음과 같이 저장한다.
```
빈 이름: memberService, 빈 객체: MemberServiceImpl@x01
빈 이름: orderService, 빈 객체: OrderServiceImpl@x02
빈 이름: memberRepository, 빈 객체: MemoryMemberRepository@x03
빈 이름: discountPolicy, 빈 객체: RateDiscountPolicy@x04
```
- 빈 이름은 항상 다른 이름을 부여해야 한다.
- 같은 이름 부여 시 덮어쓰기 하거나 오류 발생한다.
## 스프링 빈 의존관계 설정
- memberService -> memberResporitoy
- orderService -> memberResporitory
- orderService -> discountPolicy
- 스프링 컨테이너는 설정 정보를 참고해서 의존관계 주입한다.
- 단순 자바 코드 호출과는 다르다. (싱글톤 컨테이너)
## 컨테이너에 등록된 모든 빈 조회
```java
AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
@Test
@DisplayName("모든 빈 출력 테스트")
void findAllBean() {
    String[] beanDefinitionNames = ac.getBeanDefinitionNames();
    for (String beanDefinitionName : beanDefinitionNames) {
        Object bean = ac.getBean(beanDefinitionName);
        System.out.println("ac.getBean(" + beanDefinitionName + ") = " + bean);
    }
}
/*
ac.getBean(org.springframework.context.annotation.internalConfigurationAnnotationProcessor) = org.springframework.context.annotation.ConfigurationClassPostProcessor@71e9a896
ac.getBean(org.springframework.context.annotation.internalAutowiredAnnotationProcessor) = org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor@51650883
ac.getBean(org.springframework.context.annotation.internalCommonAnnotationProcessor) = org.springframework.context.annotation.CommonAnnotationBeanPostProcessor@6c4f9535
ac.getBean(org.springframework.context.event.internalEventListenerProcessor) = org.springframework.context.event.EventListenerMethodProcessor@5bd1ceca
ac.getBean(org.springframework.context.event.internalEventListenerFactory) = org.springframework.context.event.DefaultEventListenerFactory@30c31dd7
ac.getBean(appConfig) = basic.lecture.AppConfig$$SpringCGLIB$$0@499b2a5c
ac.getBean(memberService) = basic.lecture.MemberServiceImpl@596df867
ac.getBean(orderService) = basic.lecture.OrderServiceImpl@c1fca1e
ac.getBean(memberRepository) = basic.lecture.MemoryMemberRepository@241a53ef
ac.getBean(discountPolicy) = basic.lecture.RateDiscountPolicy@344344fa
 */
```
## 애플리케이션 빈 조회
```java
    @Test
@DisplayName("App 빈 출력 테스트")
void findAppBean() {
    String[] beanDefinitionNames = ac.getBeanDefinitionNames();
    for (String beanDefinitionName : beanDefinitionNames) {
        BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);
        if (beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
            Object bean = ac.getBean(beanDefinitionName);
            System.out.println("ac.getBean(" + beanDefinitionName + ") = " + bean);
        }
    }
}
/*
ac.getBean(appConfig) = basic.lecture.AppConfig$$SpringCGLIB$$0@30c31dd7
ac.getBean(memberService) = basic.lecture.MemberServiceImpl@4362d7df
ac.getBean(orderService) = basic.lecture.OrderServiceImpl@66238be2
ac.getBean(memberRepository) = basic.lecture.MemoryMemberRepository@1c25b8a7
ac.getBean(discountPolicy) = basic.lecture.RateDiscountPolicy@200606de
 */
```
## 애플리케이션 제외 빈 조회
```java
@Test
@DisplayName("App 제외 빈 출력 테스트")
void findEtcBean() {
    String[] beanDefinitionNames = ac.getBeanDefinitionNames();
    for (String beanDefinitionName : beanDefinitionNames) {
        BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);
        if (beanDefinition.getRole() == BeanDefinition.ROLE_SUPPORT) {
            Object bean = ac.getBean(beanDefinitionName);
            System.out.println("[ROLE_SUPPORT] ac.getBean(" + beanDefinitionName + ") = " + bean);
        } else if (beanDefinition.getRole() == BeanDefinition.ROLE_INFRASTRUCTURE) {
            Object bean = ac.getBean(beanDefinitionName);
            System.out.println("[ROLE_INFRASTRUCTURE] ac.getBean(" + beanDefinitionName + ") = " + bean);
        }
    }
}
/*
[ROLE_INFRASTRUCTURE] ac.getBean(org.springframework.context.annotation.internalConfigurationAnnotationProcessor) = org.springframework.context.annotation.ConfigurationClassPostProcessor@499b2a5c
[ROLE_INFRASTRUCTURE] ac.getBean(org.springframework.context.annotation.internalAutowiredAnnotationProcessor) = org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor@66238be2
[ROLE_INFRASTRUCTURE] ac.getBean(org.springframework.context.annotation.internalCommonAnnotationProcessor) = org.springframework.context.annotation.CommonAnnotationBeanPostProcessor@1c25b8a7
[ROLE_INFRASTRUCTURE] ac.getBean(org.springframework.context.event.internalEventListenerProcessor) = org.springframework.context.event.EventListenerMethodProcessor@200606de
[ROLE_INFRASTRUCTURE] ac.getBean(org.springframework.context.event.internalEventListenerFactory) = org.springframework.context.event.DefaultEventListenerFactory@750fe12e
 */
```
## 스프링 빈(Spring Bean)이란?
> 스프링 빈이란 스프링 IoC 컨테이너가 관리(생성, 의존 주입, 생명주기 관리)하는 객체를 말합니다.
> 일반 자바 객체(POJO)이지만, 스프링이 관리한다는 점이 다릅니다.
즉, `new`로 직접 생성하는 게 아니라 컨테이너가 대신 만들어서 주입해주는 객체예요.
## 스프링 빈의 종류 (분류 기준에 따라 다름)
스프링 빈은 “무엇을 기준으로 분류하느냐”에 따라 여러 가지로 나눌 수 있습니다.
## (1) 기능에 따른 분류

| 종류 | 예시 어노테이션 | 역할 |
| -- | -------- | -- |
| Controller 빈 | `@Controller`, `@RestController` | 웹 요청을 받고 응답을 처리하는 프레젠테이션 계층의 빈 |
| Service 빈 | `@Service` | 비즈니스 로직을 수행하는 서비스 계층의 빈 |
| Repository 빈 | `@Repository` | 데이터 접근, DAO(Data Access Object) 기능 담당 |
| Component 빈 | `@Component` | 위 3가지 외의 일반적인 목적의 빈 (공통 유틸, 헬퍼 등) |
| Configuration 빈 | `@Configuration` | 스프링 설정 정보를 제공하는 빈 |
| Bean 메서드로 등록된 빈 | `@Bean`  | 수동으로 직접 정의한 객체를 등록할 때 사용 |

- 위 4개(`@Controller`, `@Service`, `@Repository`, `@Component`)는 모두 @Component 기반 자동 스캔 빈입니다.
- `@Configuration` + `@Bean`은 수동 등록 빈입니다.
## (2) 등록 방식에 따른 분류

| 등록 방식 | 설명 | 예시 |
| ----- | -- | -- |
| 자동 등록 빈 | 컴포넌트 스캔(`@ComponentScan`)을 통해 자동으로 등록 | 클래스에 `@Component`, `@Service`, `@Controller` 등 사용 |
| 수동 등록 빈 | 개발자가 직접 자바 설정파일(`@Configuration`)에서 등록 | `@Bean` 메서드로 명시적 등록 |

- 자동 등록은 스프링이 탐색해서 등록,
- 수동 등록은 개발자가 코드로 명시해서 등록.
## (3) 스코프(scope)에 따른 분류

| 스코프 | 설명 |
| --- | -- |
| singleton (기본값) | 컨테이너당 1개 인스턴스만 생성 (공유) |
| prototype | 요청마다 새로운 인스턴스 생성 |
| request | HTTP 요청마다 1개 빈 생성 (웹 전용) |
| session | HTTP 세션마다 1개 빈 생성 (웹 전용) |
| application | ServletContext당 1개 빈 생성 |

## 스프링 빈의 역할

| 역할 | 설명 |
| -- | -- |
| 객체 생성 관리 | 객체를 개발자가 직접 `new`하지 않고, 컨테이너가 생성 및 초기화 |
| 의존성 주입(DI) | 필요한 객체를 알아서 주입 (생성자, 세터, 필드 등) |
| 라이프사이클 관리 | 객체 생성 → 의존 주입 → 초기화 → 소멸까지 컨테이너가 관리 |
| AOP 적용 대상 | 트랜잭션, 로깅 같은 횡단 관심사를 빈 단위로 쉽게 적용 |
| 환경 독립성 보장 | 환경 설정(예: DB, API 키 등)을 외부 설정으로 주입 가능 |

## 스프링 빈 정의(Bean Definition)의 역할
> BeanDefinition은 “스프링 컨테이너가 빈을 어떻게 만들지에 대한 설계도” 역할을 합니다.
- 쉽게 말해:
* 클래스 정보,
* 스코프(singleton/prototype),
* 의존 관계,
* 초기화/소멸 메서드,
* 프로퍼티 값
- 등을 담은 메타데이터 객체예요.
- 컨테이너는 내부적으로 BeanDefinition 정보를 읽고 다음과 같이 동작합니다:
```
BeanDefinition 읽기 → Bean 생성 → 의존성 주입 → 초기화 → 사용 → 소멸
```
- 따라서 `@Bean`, `@Component` 등의 선언은 결국 BeanDefinition을 등록하는 행위입니다.
## 요약

| 구분 | 설명 |
| -- | -- |
| 스프링 빈 | 스프링 컨테이너가 관리하는 객체 |
| 빈의 종류 | Controller, Service, Repository, Component 등 |
| 등록 방식 | 자동 등록(@ComponentScan), 수동 등록(@Bean) |
| 빈의 역할 | 객체 생성, 의존 주입, 라이프사이클, AOP 적용 |
| 빈 정의(BeanDefinition) | 빈을 어떻게 만들고 관리할지 나타내는 설계 정보 |

## 빈 이름/타입으로 조회
```java
AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
@Test
@DisplayName("빈 이름으로 조회 테스트")
void findBeanByName() {
    MemberService memberService = ac.getBean("memberService", MemberService.class);
    Assertions.assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
}
@Test
@DisplayName("빈 타입만으로 조회 테스트")
void findBeanByType() {
    MemberService memberService = ac.getBean(MemberService.class);
    Assertions.assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
}
```
- MemberService.class 대신 MemberServiceImpl.class으로 getBean해도 된다.
- @Bean 메서드가 new MemberServiceImpl로 return을 하니까 된다.
## 조회 불가 테스트
```java
@Test
@DisplayName("조회 불가 테스트")
void noSuchBeanDefined() {
    assertThrows(NoSuchBeanDefinitionException.class, () -> ac.getBean("xxx", MemberService.class));
}
```
## 타입으로 조회 시 같은 타입이 둘 이상이면 중복 오류 난다.
```java
@Test
@DisplayName("타입 조회 중복 오류")
void noUniqueBeanDefined() {
    assertThrows(NoUniqueBeanDefinitionException.class,
            () -> ac.getBean(DiscountPolicy.class));
}
```
## 타입으로 둘 이상 빈 조회하려면
```java
@Test
@DisplayName("타입으로 여러 빈 조회")
void findBeansByType() {
    Map<String, DiscountPolicy> discountPolicies = ac.getBeansOfType(DiscountPolicy.class);
    for (Map.Entry<String, DiscountPolicy> entry : discountPolicies.entrySet()) {
        System.out.println(entry);
    }
}
/*
discountPolicy=basic.lecture.RateDiscountPolicy@5305c37d
discountPolicy2=basic.lecture.RateDiscountPolicy@51a06cbe
 */
```
## 스프링 빈 조회 - 상속관계
```java
@Test
@DisplayName("상속관계")
void findBeansByType2() {
    Map<String, Object> objects = ac.getBeansOfType(Object.class);
    for (Map.Entry<String, Object> entry : objects.entrySet()) {
        System.out.println(entry);
    }
}
```
- 이 코드는 Spring 컨테이너에 등록된 모든 빈(Bean)을 조회하는 테스트입니다.
## 결과 분석
## Spring 내부 인프라 빈들 (자동 등록)
```
internalConfigurationAnnotationProcessor (ConfigurationClassPostProcessor)
internalAutowiredAnnotationProcessor (AutowiredAnnotationBeanPostProcessor)
internalCommonAnnotationProcessor (CommonAnnotationBeanPostProcessor)
internalEventListenerProcessor (EventListenerMethodProcessor)
internalEventListenerFactory (DefaultEventListenerFactory)
```
- Spring이 자동으로 등록하는 시스템 빈들
- 설정 클래스 처리, `@Autowired` 처리, 이벤트 리스너 등을 담당
## 사용자 정의 빈들 (AppConfig에서 등록)
```
appConfig (CGLIB 프록시)
memberService (MemberServiceImpl)
orderService (OrderServiceImpl)
memberRepository (MemoryMemberRepository)
discountPolicy (RateDiscountPolicy) - 첫 번째 인스턴스
discountPolicy2 (RateDiscountPolicy) - 두 번째 인스턴스
```
- `AppConfig`에서 `@Bean`으로 등록한 빈들
- 주목할 점: `RateDiscountPolicy`가 2개나 있네요
## Environment 관련 빈들
```
environment (StandardEnvironment)
systemProperties (시스템 속성들)
systemEnvironment (환경 변수들)
```
- Spring의 환경 설정 정보
- JVM 속성, OS 환경변수 등을 포함
## 기타 Spring 시스템 빈들
```
applicationStartup
messageSource
applicationEventMulticaster
lifecycleProcessor
```
`Object.class`로 조회했기 때문에:
- Java의 모든 클래스는 `Object`를 상속받으므로
- Spring 컨테이너의 모든 빈이 조회됨
- 내부 시스템 빈까지 포함
실무에서는:
```java
// 특정 타입만 조회하는 게 일반적
ac.getBeansOfType(MemberRepository.class);
ac.getBeansOfType(DiscountPolicy.class);
```
이런 식으로 특정 인터페이스나 클래스 타입으로 필터링해서 사용합니다.
## BeanDefinition의 ROLE 구분
## ROLE_INFRASTRUCTURE (역할 = 0)
Spring 내부에서 사용하는 인프라 빈들입니다.
```
internalConfigurationAnnotationProcessor
internalAutowiredAnnotationProcessor
internalCommonAnnotationProcessor
internalEventListenerProcessor
internalEventListenerFactory
applicationStartup
messageSource
applicationEventMulticaster
lifecycleProcessor
```
## ROLE_SUPPORT (역할 = 1)
설정 클래스 같은 보조적인 빈들입니다.
```
appConfig (CGLIB 프록시)
```
## ROLE_APPLICATION (역할 = 2)
사용자가 직접 정의한 애플리케이션 빈들입니다.
```
memberService
orderService
memberRepository
discountPolicy
discountPolicy2
```
출력 결과를 다시 보면 BeanDefinition이 없는 객체들도 출력됐어요:
## BeanDefinition이 없는 객체들
```
environment (StandardEnvironment)
systemProperties (Map)
systemEnvironment (Map)
org.springframework.context.annotation.ConfigurationClassPostProcessor.importRegistry
```
## 왜 그럴까?
이 객체들은 BeanDefinition을 가지지 않는 특수한 싱글톤 객체들입니다
```java
// BeanDefinition이 있는지 확인하는 코드
@Test
void checkBeanDefinition() {
    Map<String, Object> beans = ac.getBeansOfType(Object.class);
    
    for (String beanName : beans.keySet()) {
        try {
            BeanDefinition bd = ac.getBeanDefinition(beanName);
            System.out.println(beanName + " -> ROLE: " + bd.getRole());
        } catch (NoSuchBeanDefinitionException e) {
            System.out.println(beanName + " -> BeanDefinition 없음!");
        }
    }
}
```
## 예상 결과
```
memberService -> ROLE: 2 (ROLE_APPLICATION)
appConfig -> ROLE: 1 (ROLE_SUPPORT)
internalConfigurationAnnotationProcessor -> ROLE: 0 (ROLE_INFRASTRUCTURE)
environment -> BeanDefinition 없음
systemProperties -> BeanDefinition 없음
systemEnvironment -> BeanDefinition 없음
```
`getBeansOfType(Object.class)`는:
- BeanDefinition이 있는 일반 빈들
- BeanDefinition 없이 Spring 컨테이너에 직접 등록된 싱글톤들
모두 다 반환합니다. 그래서 정확히 말하면:
- ROLE_APPLICATION / SUPPORT / INFRASTRUCTURE: BeanDefinition이 있는 빈들
- 그 외: BeanDefinition 없이 등록된 특수 싱글톤 객체들