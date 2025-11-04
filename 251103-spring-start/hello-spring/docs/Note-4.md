자바 스프링(Spring)에서 컴포넌트(Component)와 빈(Bean)은 핵심적인 개념이에요.
이 둘은 스프링이 객체를 관리하는 방식(IoC, Inversion of Control)과 관련되어 있습니다.
## 1. 스프링 빈(Spring Bean)
정의:
스프링 컨테이너(ApplicationContext)가 관리하는 객체를 말합니다.
즉, 스프링이 생성하고, 의존성을 주입하고, 생명주기를 관리하는 모든 객체는 빈이에요.
예시:
```java
@Service
public class OrderService {
    // 이 클래스의 인스턴스는 스프링이 관리하는 Bean이 됨
}
```
이렇게 등록된 빈은 컨테이너에서 꺼내서 쓸 수 있습니다.
```java
@Autowired
private OrderService orderService;
```
정리하자면:

| 구분         | 의미                                                  |
| ---------- | --------------------------------------------------- |
| Bean       | 스프링이 관리하는 모든 객체                                     |
| Bean 등록 방식 | ① 어노테이션(Component 계열) ② Java Config(@Bean) ③ XML 설정 |
| 관리 주체      | Spring IoC Container                                |

## 2. 컴포넌트(Component)
정의:
`@Component`는 빈으로 등록하겠다는 표시(메타 어노테이션)입니다.
즉, `@Component`를 붙이면
"이 클래스를 스프링 컨테이너가 관리해야 합니다!" 라고 스프링에 알려주는 거예요.
```java
@Component
public class NotificationService {
    public void send() {
        System.out.println("알림 전송!");
    }
}
```
스프링이 `@ComponentScan`을 통해 자동으로 찾아서 빈으로 등록합니다.
## 3. `@Component`의 파생 어노테이션들
스프링은 역할에 따라 가독성을 높이기 위해 여러 파생 어노테이션을 제공합니다:

| 어노테이션             | 역할               | 기본 기능                       |
| ----------------- | ---------------- | --------------------------- |
| `@Component`      | 일반 컴포넌트          | 가장 기본적인 빈 등록용               |
| `@Service`        | 비즈니스 로직 계층       | 내부적으로 @Component 포함         |
| `@Repository`     | 데이터 접근 계층 (DAO)  | 예외 변환 기능 포함                 |
| `@Controller`     | 웹 계층 (MVC 컨트롤러)  | 요청/응답 처리                    |
| `@RestController` | REST API 전용 컨트롤러 | @Controller + @ResponseBody |

→ 즉, 이 어노테이션들도 결국 빈을 등록하는 역할을 합니다.
다만, 의미적 구분을 위해 이름만 다르게 쓰는 거예요.
## 4. `@Bean`과 `@Component`의 차이
둘 다 스프링 빈을 등록하지만 방식이 다릅니다.

| 비교 항목 | `@Component` | `@Bean` |
| -- | ----- | ----- |
| 위치 | 클래스 위 | 메서드 위 |
| 자동/수동 등록 | 자동 등록 (스캔 대상) | 수동 등록 (직접 등록) |
| 사용 예시 | 일반적인 서비스, DAO 등 | 외부 라이브러리, 설정용 객체 |
| 예시 코드 | `@Component public class MyService {}` | `@Bean public DataSource dataSource() { ... }` |

예를 들어 외부 라이브러리 클래스는 우리가 직접 수정할 수 없으므로
그런 객체를 빈으로 등록하려면 `@Bean`을 사용해야 합니다.
```java
@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
```
## 5. 요약
```
Spring IoC Container
 ├── @Component → 자동 등록된 Bean
 │     ├── @Controller
 │     ├── @Service
 │     └── @Repository
 └── @Bean → 수동 등록된 Bean
```

| 개념       | 설명        |
| -------- | --------- |
| Bean | 스프링 컨테이너가 관리하는 객체 |
| Component | Bean 등록을 위한 어노테이션 |
| @Component 계열 | 역할별 명시적 구분 (Controller, Service, Repository 등) |
| @Bean | 외부 객체나 커스텀 설정용 수동 Bean 등록 |
| 결국 둘 다 Bean 생성용 | 등록 방식만 다름 |

`@ComponentScan`은 스프링이 “자동으로 빈을 찾아 등록하는 핵심 메커니즘”이에요.
즉, 우리가 `@Component`, `@Service`, `@Repository`, `@Controller` 등을 붙이기만 하면
스프링이 알아서 찾아서 등록해 주는 이유가 바로 이 녀석입니다.
## 1. ComponentScan의 기본 개념
`@ComponentScan`은 다음 일을 합니다
> “지정된 패키지를 탐색하면서,
> `@Component` (또는 그 하위 어노테이션)가 붙은 클래스를 찾아
> 스프링 컨테이너에 빈으로 등록한다.”
## 2. 기본 동작 원리
1. 스프링이 시작될 때 (`@SpringBootApplication` 또는 `@Configuration` 클래스 실행 시)
2. `@ComponentScan`이 활성화됩니다.
3. 설정된 패키지(기본은 설정 클래스의 패키지)부터 재귀적으로 하위 패키지를 탐색
4. 각 클래스의 메타데이터를 확인해서 `@Component`, `@Service`, `@Repository`, `@Controller` 등이 붙어 있으면
5. 해당 클래스를 Bean으로 등록
## 3. 기본 예시
```java
@Configuration
@ComponentScan(basePackages = "com.example.myapp")
public class AppConfig {
}
```
위 설정은
`com.example.myapp` 패키지와 그 하위 패키지에서
`@Component` 계열이 붙은 모든 클래스를 찾아 빈으로 등록합니다.
```java
package com.example.myapp.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    // 자동으로 Bean 등록됨
}
```
이제 `@Autowired`로 주입할 수 있습니다.
```java
@Autowired
private UserService userService;
```
## 4. 기본 스캔 위치 (Spring Boot 기준)
스프링 부트에서는 보통 이렇게 시작하죠
```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```
여기서 `@SpringBootApplication` 안에는 이미
`@ComponentScan`이 포함되어 있습니다.
즉, 기본적으로 현재 클래스(MyApplication)의 패키지와 그 하위 패키지를 모두 스캔합니다.
> 💡 그래서 보통 `@SpringBootApplication`을 최상위 패키지에 두는 이유가 이것이에요.
> (하위의 모든 서브패키지를 자동으로 포함시키기 위해)
## 5. `@ComponentScan` 주요 속성 정리

| 속성         | 설명         | 예시                                |
| ---------- | ---------- | --------------------------------- |
| `basePackages` | 탐색할 패키지 지정 | `@ComponentScan(basePackages = {"com.example.service", "com.example.repo"})` |
| `basePackageClasses` | 클래스 기준으로 패키지 탐색 | `@ComponentScan(basePackageClasses = MyClass.class)` |
| `includeFilters` | 스캔에 포함할 클래스 조건 지정 | `@ComponentScan(includeFilters = @Filter(MyCustomAnnotation.class))` |
| `excludeFilters` | 스캔에서 제외할 클래스 조건 지정 | `@ComponentScan(excludeFilters = @Filter(Controller.class))` |
| `lazyInit` | Bean을 지연 초기화할지 여부 | `@ComponentScan(lazyInit = true)` |

## 6. includeFilters / excludeFilters 예시
```java
@Configuration
@ComponentScan(
    basePackages = "com.example",
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = CustomComponent.class),
    excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class)
)
public class AppConfig {
}
```
* `includeFilters`: 특정 어노테이션(@CustomComponent)이 붙은 것만 포함
* `excludeFilters`: 특정 어노테이션(@Controller)이 붙은 것은 제외
## 7. 실제 동작 흐름 (내부 메커니즘)
1. 스프링 컨테이너 시작 시
2. `ClassPathBeanDefinitionScanner`가 등록되어 실행됨
3. 설정된 base package에서 `.class` 파일을 모두 스캔
4. ASM(바이트코드 리더)을 이용해 클래스 메타데이터를 확인
5. `@Component` 어노테이션이 존재하면 `BeanDefinition` 객체로 변환
6. 이 정의 정보를 기반으로 빈 생성 및 등록
> 즉, 실제로는 클래스를 로드하지 않고도(리플렉션 없이)
> 바이트코드 수준에서 빠르게 스캔하는 구조예요.
> 성능과 효율을 고려한 설계입니다.
## 8. ComponentScan이 없으면 어떻게 될까?
만약 `@ComponentScan`을 설정하지 않으면
`@Component`, `@Service` 등이 붙은 클래스는 스프링이 인식하지 못합니다.
그럼 이런 에러가 뜨죠
```
No qualifying bean of type 'com.example.service.UserService' available
```
## 정리

| 구분       | 내용                                                |
| -------- | ------------------------------------------------- |
| 목적       | `@Component` 계열 클래스를 자동으로 Bean 등록                 |
| 기본 스캔 경로 | 설정 클래스(또는 `@SpringBootApplication`)의 패키지 및 하위 패키지 |
| 동작 방식    | 바이트코드 기반 메타데이터 스캔 → BeanDefinition 등록             |
| 주요 속성    | basePackages, includeFilters, excludeFilters 등    |
| 내부 클래스   | `ClassPathBeanDefinitionScanner`                  |
| 핵심 포인트   | 스프링이 자동으로 객체를 관리할 수 있게 만드는 기반 메커니즘                |

# 1. BeanDefinition이란?
`BeanDefinition`은 스프링 컨테이너가 빈을 만들기 위한 메타데이터(Metadata)입니다.
즉, 클래스 자체가 아니라
> “이 빈을 어떻게 만들지에 대한 설명서”라고 보면 돼요.
## BeanDefinition이 담고 있는 정보 예시

| 항목         | 설명                                |
| ---------- | --------------------------------- |
| Bean 이름    | `userService`                     |
| Bean 클래스   | `com.example.service.UserService` |
| 스코프        | `singleton`, `prototype`, ...     |
| 생성자 정보     | 어떤 생성자를 써서 만들지                    |
| 의존성 정보     | 주입해야 할 다른 Bean 목록                 |
| 초기화/소멸 메서드 | `@PostConstruct`, `@PreDestroy` 등 |
| Lazy 여부    | `@Lazy` 지정 여부                     |
| Primary 여부 | `@Primary` 설정 여부                  |

즉, 스프링은 실제 객체를 만들기 전에
모든 Bean을 BeanDefinition 객체로 등록해두고,
그걸 기반으로 나중에 객체를 생성합니다.
# 2. 전체 흐름 개요
`@ComponentScan`으로 시작해서
실제 Bean 객체가 생성되기까지의 내부 단계를 요약하면 다음과 같습니다:
```
@ComponentScan
   ↓
ClassPathBeanDefinitionScanner
   ↓
BeanDefinition 생성
   ↓
BeanDefinitionRegistry에 등록
   ↓
BeanFactoryPostProcessor (예: @Configuration 처리)
   ↓
BeanFactory
   ↓
Bean 생성 (생성자 호출)
   ↓
의존성 주입 (@Autowired 등)
   ↓
초기화 메서드 호출 (@PostConstruct)
   ↓
ApplicationContext에 Bean 완성
```
# 3. 세부 단계별 설명
## (1) `@ComponentScan`이 스캐너를 실행
* `ClassPathBeanDefinitionScanner` 클래스가 동작함
* 지정된 `basePackages` 경로를 순회하면서 `.class` 파일을 모두 검색
* `.class` 파일을 실제로 로드하지 않고, ASM(바이트코드 리더) 로 메타데이터를 읽음
  → `@Component`, `@Service`, `@Repository`, `@Controller` 등 여부 확인
## (2) BeanDefinition 생성
* 스프링은 찾은 클래스에 대해 `ScannedGenericBeanDefinition` 객체를 만듭니다.
  이게 바로 “이 클래스를 어떻게 빈으로 만들지” 정의한 메타데이터예요.
```java
ScannedGenericBeanDefinition beanDefinition = new ScannedGenericBeanDefinition(metadata);
beanDefinition.setBeanClassName("com.example.service.UserService");
beanDefinition.setScope("singleton");
```
## (3) BeanDefinitionRegistry에 등록
* 생성된 BeanDefinition은 `BeanDefinitionRegistry`에 등록됩니다.
* 스프링의 기본 구현체는 `DefaultListableBeanFactory`.
```java
beanDefinitionRegistry.registerBeanDefinition("userService", beanDefinition);
```
즉, 이 시점에는 아직 Bean 객체는 생성되지 않았습니다.
단지 "등록"만 된 상태예요.
## (4) BeanFactoryPostProcessor 단계
* 이후 `BeanFactoryPostProcessor`나 `ConfigurationClassPostProcessor`가 개입해서
  `@Configuration`, `@Import`, `@Bean` 같은 수동 등록 Bean도 BeanDefinition 형태로 추가합니다.
즉, 이 단계에서
자동 등록(ComponentScan) + 수동 등록(@Bean)
이 모두 BeanDefinition 레벨에서 합쳐집니다.
## (5) BeanFactory가 BeanDefinition을 기반으로 Bean 생성
* 이제 `BeanFactory` (보통 `DefaultListableBeanFactory`)가 실제 Bean을 생성하기 시작합니다.
순서
1. BeanDefinition 조회
2. 스코프 확인 (singleton/prototype 등)
3. 객체 인스턴스 생성 (`Constructor`, `FactoryMethod` 등)
4. 의존성 주입 수행 (`@Autowired`, `@Value`, `@Qualifier` 등)
5. 초기화 콜백 실행 (`@PostConstruct`, `InitializingBean.afterPropertiesSet()`)
6. 완성된 Bean을 캐싱 (singleton이라면 컨테이너에 보관)
## (6) 의존성 주입 과정
* `AutowiredAnnotationBeanPostProcessor`가 동작하면서
  BeanDefinition 정보를 참고해 필드/생성자/Setter에 필요한 다른 Bean을 주입합니다.
즉, BeanDefinition 덕분에
“어떤 Bean이 어떤 Bean을 필요로 하는지” 관계를 모두 알 수 있습니다.
## (7) 완성된 Bean 저장
* 모든 초기화가 끝나면 ApplicationContext에 Bean이 들어갑니다.
* 이때부터 `@Autowired`로 바로 사용할 수 있는 상태가 됩니다.
# 4. 실제 객체 생성 시점

| Bean Scope  | 생성 시점           |
| ----------- | --------------- |
| `singleton` | 컨테이너 시작 시 즉시 생성 |
| `prototype` | 요청될 때마다 새로 생성   |
| `lazy`      | 실제로 필요할 때 처음 생성 |

# 5. 그림
```
[ComponentScan] 
      ↓
[ClassPathBeanDefinitionScanner]
      ↓
[ScannedGenericBeanDefinition 생성]
      ↓
[BeanDefinitionRegistry에 등록]
      ↓
[BeanFactoryPostProcessor / Configuration 처리]
      ↓
[BeanFactory에서 Bean 생성]
      ↓
[@Autowired 등 의존성 주입]
      ↓
[@PostConstruct 등 초기화]
      ↓
[완성된 Bean ApplicationContext에 저장]
```

| 단계            | 설명                                 |
| ------------- | ---------------------------------- |
| 1. 스캔     | @ComponentScan이 클래스 탐색             |
| 2. 정의 생성  | BeanDefinition 생성                  |
| 3. 등록     | BeanDefinitionRegistry에 등록         |
| 4. 가공     | BeanFactoryPostProcessor 등이 설정 보강  |
| 5. 생성     | BeanFactory가 실제 Bean 객체 생성         |
| 6. 주입/초기화 | 의존성 주입 + 초기화 메서드 실행                |
| 7. 완성     | ApplicationContext에 등록된 Bean 사용 가능 |

# 왜 이렇게 복잡할까?
BeanDefinition 단계를 분리한 이유는 유연성 때문이에요.
* XML, Annotation, Java Config 등 여러 등록 방식을 통합 가능
* Bean을 미리 로드하지 않아도 관계를 파악하고 최적화 가능
* BeanFactoryPostProcessor 같은 확장 포인트 제공
즉, 이 구조 덕분에 스프링은
“런타임에 클래스 구조를 분석해 객체를 자동 생성하고 관리하는”
강력한 DI 컨테이너가 된 것입니다.
앞서 배운 BeanDefinition → Bean 생성 과정이
“스프링이 객체를 만들기 전 단계”였다면,
이제는 “스프링이 만든 객체를 다듬는 단계”입니다.
# 1. BeanPostProcessor란?
`BeanPostProcessor`는 스프링이 빈을 생성한 뒤, 초기화 전후에 개입할 수 있는 훅(hook) 입니다.
> 즉, “스프링이 만든 객체를 건드릴 수 있는 확장 포인트”입니다.
>
> 대표적인 기능 예시:
>
> * `@Autowired` 의존성 주입
> * `@Transactional` 프록시 생성
> * `@Async` 비동기 처리 프록시
> * `@PostConstruct` 실행
이 모든 것들이 사실 내부적으로 BeanPostProcessor로 구현되어 있어요.
# 2. Bean 생명주기에서의 위치
스프링 Bean의 라이프사이클을 전체적으로 보면 이렇게 됩니다
```
BeanDefinition 등록
   ↓
Bean 인스턴스 생성 (new)
   ↓
의존성 주입 수행
   ↓
▶ [BeanPostProcessor - beforeInitialization()]
   ↓
초기화 메서드 호출 (@PostConstruct, afterPropertiesSet)
   ↓
▶ [BeanPostProcessor - afterInitialization()]
   ↓
ApplicationContext에 Bean 완성
```
즉, BeanPostProcessor는
초기화 전(`beforeInitialization`)과 초기화 후(`afterInitialization`)
두 번 호출됩니다.
# 3. BeanPostProcessor 인터페이스 구조
```java
public interface BeanPostProcessor {
    Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;
    Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
}
```
* `postProcessBeforeInitialization`
  → `@PostConstruct`나 `afterPropertiesSet()` 실행 직전
* `postProcessAfterInitialization`
  → 초기화 이후, 완성된 Bean을 조작하거나 프록시로 감쌀 때
# 4. 대표적인 BeanPostProcessor 종류

| 클래스명 | 역할 |
| ---- | -- |
| `AutowiredAnnotationBeanPostProcessor` | `@Autowired`, `@Value` 처리 |
| `CommonAnnotationBeanPostProcessor` | `@PostConstruct`, `@PreDestroy` 처리 |
| `ConfigurationClassPostProcessor` | `@Configuration` 내부의 `@Bean` 등록 |
| `AnnotationAwareAspectJAutoProxyCreator` | `@Transactional`, AOP 프록시 생성 |
| `AsyncAnnotationBeanPostProcessor` | `@Async` 비동기 실행용 프록시 생성 |
| `PersistenceAnnotationBeanPostProcessor` | JPA의 `@PersistenceContext` 처리 |

이 중 AOP 프록시 관련 처리 (`@Transactional`, `@Async`)는
`postProcessAfterInitialization()` 시점에서 일어납니다.
→ 즉, 빈이 완성된 후 프록시 객체로 대체되는 것이죠.
# 5. 직접 만들어보기 (예시)
```java
@Component
public class MyCustomBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("[BeforeInit] " + beanName);
        return bean; // null 반환하면 Bean 등록이 중단됨!
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("[AfterInit] " + beanName);
        return bean;
    }
}
```
출력 예시:
```
[BeforeInit] userService
[AfterInit] userService
```
이 코드는 모든 Bean이 생성될 때마다 로그를 찍어줍니다.
(실제 Spring 내부도 이런 식으로 수많은 BeanPostProcessor들이 자동 등록되어 있어요.)
# 6. BeanPostProcessor 동작 순서 (정확히 보기)
스프링은 `ApplicationContext` 초기화 시 다음 단계를 거칩니다
1. `BeanDefinition` 로딩 완료
2. BeanFactoryPostProcessor 실행 (BeanDefinition 수정 가능)
3. BeanPostProcessor 등록
4. Bean 생성 시작
5. Bean 생성 후, 모든 BeanPostProcessor의
   `postProcessBeforeInitialization()` 호출
6. `@PostConstruct` / 초기화 메서드 실행
7. 모든 BeanPostProcessor의
   `postProcessAfterInitialization()` 호출
8. 최종 Bean 컨테이너에 저장
# 7. AOP / Transaction이 동작하는 이유
`@Transactional`이 동작하는 원리도 바로 여기서 나옵니다.
1. Bean 생성 완료 후
2. `AnnotationAwareAspectJAutoProxyCreator`라는 BeanPostProcessor가 개입
3. 해당 클래스가 `@Transactional`로 표시되어 있으면
   → 실제 객체를 프록시 객체로 감싸서 교체
   → 최종 컨테이너에는 프록시 Bean이 등록됨
4. 이후 트랜잭션 로직은 프록시가 대신 처리
즉, BeanPostProcessor는 단순히 객체를 “가공”하는 게 아니라
아예 다른 객체로 교체할 수도 있습니다.
# 8. BeanPostProcessor vs BeanFactoryPostProcessor

| 구분 | BeanFactoryPostProcessor | BeanPostProcessor |
| -- | ----------- | --------- |
| 실행 시점 | Bean 생성 이전 (정의 단계) | Bean 생성 이후 (객체 단계) |
| 입력 대상 | `BeanDefinition` | Bean 인스턴스 |
| 대표 예시 | `PropertySourcesPlaceholderConfigurer`, `ConfigurationClassPostProcessor` | `AutowiredAnnotationBeanPostProcessor`, `AOP 프록시 생성기` |
| 용도 | Bean 정의를 수정 | Bean 객체를 조작 또는 교체 |
즉:
* BeanFactoryPostProcessor: “설명서(정의)” 수정
* BeanPostProcessor: “완성품(객체)” 수정
# 요약

| 개념 | 설명 |
| -- | -- |
| BeanPostProcessor | Bean 생성 후 초기화 전/후에 개입하는 확장 포인트 |
| BeforeInitialization | `@PostConstruct` 이전 |
| AfterInitialization | `@PostConstruct` 이후, AOP 프록시 적용 시점 |
| 대표적 사용 예시 | `@Autowired`, `@Transactional`, `@Async`, `@PostConstruct` |
| Bean 교체 가능 | 프록시 객체로 감싸 실제 Bean 대체 가능 |
| 실제 효과 | 스프링의 자동 주입, AOP, 트랜잭션 기능의 기반 |

> BeanPostProcessor는
> “스프링이 만든 객체를 가공하거나 프록시로 교체할 수 있게 해주는 후처리기”입니다.
>
> 스프링의 의존성 주입, 트랜잭션, AOP 등은
> 전부 이 메커니즘 위에서 작동합니다.
# Spring Bean 라이프사이클 개요
스프링은 단순히 객체를 `new`로 생성하는 게 아니라,
“생성부터 의존성 주입, 초기화, 소멸”까지 모든 단계를 직접 관리합니다.
전체 흐름을 한 줄로 요약하면
```
BeanDefinition 등록
   ↓
Bean 인스턴스 생성 (new)
   ↓
의존성 주입 (Dependency Injection)
   ↓
Bean 초기화 (init method, @PostConstruct)
   ↓
사용 단계
   ↓
Bean 소멸 (destroy method, @PreDestroy)
```
# 단계별 상세 설명
## 1. BeanDefinition 등록
무엇:
`@ComponentScan`, `@Bean`, XML 등으로 정의된 Bean 정보가
`BeanDefinition` 형태로 컨테이너에 등록됩니다.
아직 객체는 생성되지 않은 상태예요.
핵심 클래스:
`DefaultListableBeanFactory`, `BeanDefinitionRegistry`
## 2. Bean 인스턴스 생성
무엇:
`BeanDefinition` 정보를 기반으로 실제 Java 객체(`new`)가 생성됩니다.
어떻게:
* 리플렉션(`Constructor`, `FactoryMethod`)을 이용해 객체 생성
* 생성자 주입(@Autowired Constructor)도 이 시점에 처리됩니다
```java
public class OrderService {
    private final UserRepository userRepository;

    @Autowired
    public OrderService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```
## 3. 의존성 주입 (Dependency Injection)
무엇:
Bean이 생성된 후, 내부 필드나 setter에 다른 Bean을 주입하는 단계입니다.
어떻게:
* `AutowiredAnnotationBeanPostProcessor`가 동작하여
  `@Autowired`, `@Value`, `@Qualifier` 등을 처리
* 생성자 외의 필드/세터 주입이 여기서 이뤄집니다
```java
@Component
public class PaymentService {
    @Autowired
    private OrderService orderService; // 이 시점에서 주입됨
}
```
## 4. Bean 초기화 (Initialization)
무엇:
모든 의존성 주입이 완료된 후,
Bean이 실제로 “사용 가능” 상태가 되기 직전에
초기화 작업을 수행합니다.
실행 순서 (중요)
1. `BeanNameAware`, `BeanFactoryAware` 등 Aware 인터페이스 호출
2. `BeanPostProcessor#postProcessBeforeInitialization()` 실행
3. `@PostConstruct` 메서드 실행
4. `InitializingBean#afterPropertiesSet()` 실행
5. `@Bean(initMethod = "init")`의 `init()` 메서드 실행
6. `BeanPostProcessor#postProcessAfterInitialization()` 실행
예시:
```java
@Component
public class ExampleBean implements InitializingBean {

    @PostConstruct
    public void postConstruct() {
        System.out.println("1️⃣ @PostConstruct 실행");
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("2️⃣ afterPropertiesSet 실행");
    }

    public void init() {
        System.out.println("3️⃣ @Bean(initMethod) 실행");
    }
}
```
출력 순서:
```
1️⃣ @PostConstruct 실행
2️⃣ afterPropertiesSet 실행
3️⃣ @Bean(initMethod) 실행
```
## 5. Bean 사용 (ApplicationContext에 등록 완료)
무엇:
초기화가 끝난 Bean은 이제 컨테이너에 저장되어
`@Autowired`나 `ApplicationContext.getBean()`을 통해 사용됩니다.
예시:
```java
@Autowired
private ExampleBean exampleBean; // 이미 완성된 Bean 주입
```
## 6. Bean 소멸 (Destruction)
무엇:
스프링 컨테이너가 종료될 때(`context.close()`),
Bean의 종료 콜백이 호출되어 정리 작업(리소스 해제 등)을 수행합니다.
실행 순서:
1. `@PreDestroy`
2. `DisposableBean#destroy()`
3. `@Bean(destroyMethod = "cleanup")` 메서드 실행
예시:
```java
@Component
public class ExampleBean implements DisposableBean {

    @PreDestroy
    public void preDestroy() {
        System.out.println("1️⃣ @PreDestroy 실행");
    }

    @Override
    public void destroy() {
        System.out.println("2️⃣ destroy() 실행");
    }

    public void cleanup() {
        System.out.println("3️⃣ @Bean(destroyMethod) 실행");
    }
}
```
# 전체 순서 요약 (Spring Lifecycle Full Flow)

| 단계 | 설명 | 관련 어노테이션 / 인터페이스 |
| -- | -- | ---------------- |
| 1️⃣ 정의 등록 | BeanDefinition 스캔 및 등록 | `@Component`, `@Bean`, XML |
| 2️⃣ 인스턴스 생성 | 객체 생성 (new / 리플렉션) | 생성자 주입           |
| 3️⃣ 의존성 주입 | 다른 Bean 주입 | `@Autowired`, `@Value` |
| 4️⃣ 초기화 전 처리 | BeanPostProcessor 전처리 | `postProcessBeforeInitialization()` |
| 5️⃣ 초기화 실행 | Bean 자체 초기화 | `@PostConstruct`, `InitializingBean`, `initMethod` |
| 6️⃣ 초기화 후 처리 | BeanPostProcessor 후처리 | `postProcessAfterInitialization()` |
| 7️⃣ 사용 | ApplicationContext에서 사용 가능 | —                |
| 8️⃣ 소멸 | 컨테이너 종료 시 정리 | `@PreDestroy`, `DisposableBean`, `destroyMethod` |

```
┌──────────────────────────────────────────────┐
│ BeanDefinition 등록 (@ComponentScan, @Bean) │
└──────────────────────────────────────────────┘
                     ↓
┌────────────────────┐
│ Bean 인스턴스 생성 │  ← new UserService()
└────────────────────┘
                     ↓
┌───────────────────────┐
│ 의존성 주입 (@Autowired) │
└───────────────────────┘
                     ↓
┌────────────────────────────────────────────┐
│ 초기화 단계                                │
│ 1. Aware 인터페이스 호출                   │
│ 2. BeanPostProcessor(before)               │
│ 3. @PostConstruct                          │
│ 4. afterPropertiesSet()                    │
│ 5. initMethod                              │
│ 6. BeanPostProcessor(after)                │
└────────────────────────────────────────────┘
                     ↓
┌────────────────────┐
│ 사용 (정상 동작)   │
└────────────────────┘
                     ↓
┌────────────────────┐
│ 소멸 단계          │
│ @PreDestroy        │
│ destroy()          │
│ destroyMethod()    │
└────────────────────┘
```
# Bean 스코프별 차이

| 스코프                   | 라이프사이클 특징                    |
| --------------------- | ---------------------------- |
| `singleton`           | 애플리케이션 시작 시 생성 → 종료 시 소멸     |
| `prototype`           | 요청 시마다 새로 생성 → 컨테이너는 소멸하지 않음 |
| `request` / `session` | 웹 요청/세션 단위로 생성 및 소멸          |

# 요약

| 구분                       | 설명                                                        |
| ------------------------ | --------------------------------------------------------- |
| BeanDefinition       | Bean의 생성 정보 메타데이터 등록                                      |
| Instantiation        | 객체 생성 (`new`)                                             |
| Dependency Injection | 다른 Bean 주입 (`@Autowired`)                                 |
| Initialization       | 초기화 (`@PostConstruct`, `initMethod`)                      |
| Destruction          | 소멸 (`@PreDestroy`, `destroyMethod`)                       |
| 핵심 인터페이스             | `InitializingBean`, `DisposableBean`, `BeanPostProcessor` |

> 스프링 Bean의 생명주기는
> 정의 → 생성 → 주입 → 초기화 → 사용 → 소멸의 순환 구조이며,
> 스프링은 각 단계마다 확장 포인트(후처리기, 어노테이션, 인터페이스)를 제공해
> 개발자가 필요한 동작을 자연스럽게 삽입할 수 있도록 설계되어 있다.

스프링(Spring)은 자바(Java)로 만들어졌지만, 자바 그 자체와는 “패러다임”이 완전히 다르게 느껴집니다.
이건 단순한 착각이 아니라,
스프링이 “자바 언어 위에 새로운 프로그래밍 세계를 구축했기 때문”이에요.
## 기본 전제: 스프링은 100% 자바 기반 프레임워크
* 스프링 자체 코드도 전부 자바로 작성되어 있고,
* 우리가 작성하는 Bean, Controller, Service 등도 전부 자바 클래스입니다.
* 즉, “스프링은 자바의 위에서 돌아가는 프레임워크(Framework)”예요.
> 프레임워크란
> → 개발자가 작성한 코드를 “불러다 쓰는” 제어 구조를 가진 프로그램
> → 즉, “내가 코드를 호출하는 게 아니라, 프레임워크가 내 코드를 호출하는 구조 (IoC)”
이게 바로 스프링을 “자바 같지 않게” 느끼게 만드는 첫 번째 이유예요.
## 그런데 왜 자바랑 다르게 느껴질까?
그 이유는 크게 3가지 패러다임 변화 때문이에요.
### (1) 제어의 역전 (IoC, Inversion of Control)
일반 자바에서는 이렇게 하죠
```java
UserRepository repo = new UserRepository();
UserService service = new UserService(repo);
```
내가 객체를 만들고, 내가 연결합니다.
즉, 개발자가 제어(Control)를 가지고 있죠.
하지만 스프링에서는 이렇게만 씁니다
```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```
이제 객체 생성(`new`)이나 연결(`set`)을 직접 하지 않습니다.
스프링이 대신 해주죠.
> 즉, “객체의 제어권”이 개발자 → 스프링 컨테이너로 넘어간 겁니다.
>
> 그래서 IoC, Inversion of Control (제어의 역전)이라고 불러요.
이 순간부터,
개발자는 객체를 “만드는 사람”이 아니라 “설명하는 사람”이 됩니다.
### (2) 의존성 주입 (DI, Dependency Injection)
DI는 IoC의 구체적인 구현 방식이에요.
* 개발자가 “이 클래스는 이런 객체가 필요해요”라고 명시하면,
* 스프링이 알아서 주입(Injection)해 줍니다.
```java
@Component
public class OrderService {
    private final PaymentGateway paymentGateway;

    @Autowired
    public OrderService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }
}
```
자바에서는 new PaymentGateway() 해야 하지만,
스프링에서는 그냥 선언만 하면 됩니다.
> “객체를 직접 만드는 게 아니라, 스프링에게 부탁하는 것”
> — 이게 스프링 철학의 핵심이에요.
### 🧩 (3) 관점 지향 프로그래밍 (AOP, Aspect-Oriented Programming)
자바에서는 공통 로직(예: 로그, 트랜잭션, 보안)을 각 메서드에 일일이 써야 했어요.
```java
public void placeOrder() {
    System.out.println("트랜잭션 시작");
    try {
        // 주문 처리
    } finally {
        System.out.println("트랜잭션 종료");
    }
}
```
스프링에서는?
```java
@Transactional
public void placeOrder() {
    // 주문 처리
}
```
이 한 줄로 끝납니다.
> 왜냐면 스프링이 프록시 객체를 만들어서
> 메서드 호출 전후로 자동으로 트랜잭션 코드를 삽입해 주기 때문이에요.
> (BeanPostProcessor + AOP 기술 활용)
## 즉, 스프링은 “자바 객체를 조립하고 제어하는 거대한 컨테이너”
스프링이 하는 일은 이렇게 요약할 수 있습니다

| 역할          | 스프링이 하는 일                         |
| ----------- | --------------------------------- |
| Bean 관리     | 객체를 만들고, 연결하고, 소멸까지 관리            |
| DI (의존성 주입) | 필요한 객체를 자동으로 주입                   |
| AOP         | 공통 로직을 자동으로 삽입                    |
| 트랜잭션        | DB 트랜잭션을 자동 관리                    |
| 웹 요청 처리     | MVC 패턴에 따라 Controller-View 연결     |
| 설정 관리       | XML, Java Config, Annotation으로 구성 |

즉, 개발자는 “무엇을 하고 싶은지”만 정의하면 되고,
“어떻게 연결할지 / 언제 실행할지”는 스프링이 결정합니다.
> 그래서 스프링 코드를 보면,
> 자바 문법은 그대로인데 흐름은 완전히 달라진 느낌을 받는 거예요.
## 실제 자바와 스프링 비교

| 비교 항목  | 순수 자바        | 스프링                   |
| ------ | ------------ | --------------------- |
| 객체 생성  | `new` 키워드 직접 | 스프링 컨테이너가 생성          |
| 의존성 관리 | 개발자가 직접 연결   | `@Autowired`, `@Bean` |
| 실행 순서  | 내가 호출        | 프레임워크가 호출             |
| 공통 기능  | 직접 코드 삽입     | AOP로 자동 삽입            |
| 구조적 단위 | 클래스 중심       | Bean / Context 중심     |

## 결론 — “스프링은 자바의 위에 만들어진 새로운 ‘세계’”
* 스프링은 자바 언어의 문법과 런타임을 그대로 사용하면서,
* 그 위에 IoC 컨테이너, AOP, DI, MVC, 트랜잭션 관리 같은
  새로운 “프레임워크 레이어”를 얹은 것입니다.
> 자바는 "내가 제어하는 코드"
> 스프링은 "컨테이너가 제어하는 코드"
“스프링 부트 애플리케이션이 실행될 때, 내부에서 어떤 일이 일어나는가?”
즉,
`SpringApplication.run()` → `ApplicationContext` → `BeanFactory`
로 이어지는 실행 구조의 실제 흐름을 정리해볼게요.
## 시작점: `SpringApplication.run()`
모든 스프링 부트 앱은 여기서 시작하죠
```java
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}
```
이 한 줄이 스프링 세계를 여는 문이에요.
## `SpringApplication.run()` 내부 단계
실제로는 내부적으로 이렇게 진행됩니다
```java
public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
    SpringApplication app = new SpringApplication(primarySource);
    return app.run(args);
}
```
이때 `SpringApplication`은 단순히 “부트스트랩(bootstrap)” 도우미 객체예요.
즉, 스프링 컨테이너를 만들고 실행하는 역할만 합니다.
## `app.run()` 내부의 전체 플로우
아래는 실제 순서예요 (핵심 단계 중심으로 정리):
### (1) 환경(Environment) 준비
* OS, JVM, application.yml, 시스템 프로퍼티 등 읽기
* `ApplicationEnvironmentPreparedEvent` 발생
  → 프로파일, 설정 파일 처리
```java
ConfigurableEnvironment environment = prepareEnvironment();
```
### (2) ApplicationContext 생성
* 어떤 “컨테이너”를 쓸지 결정합니다.
    * 일반 애플리케이션: `AnnotationConfigApplicationContext`
    * 웹 서블릿: `AnnotationConfigServletWebServerApplicationContext`
    * 리액티브 웹: `AnnotationConfigReactiveWebServerApplicationContext`
```java
ConfigurableApplicationContext context = createApplicationContext();
```
즉, 이 단계에서 “스프링 컨테이너”가 메모리에 생성돼요.
아직 비어 있고, Bean도 없습니다.
### (3) Bean 등록: `BeanDefinition` 스캔 및 로드
이제 본격적으로 Bean 정보를 등록하기 시작합니다.
* `@SpringBootApplication` 내부의
  `@ComponentScan`이 동작하면서
  classpath 하위 패키지에서 `@Component`, `@Service`, `@Repository`, `@Controller`를 찾습니다.
* `@Configuration` 클래스의 `@Bean` 메서드들도 같이 등록됩니다.
모든 Bean은 다음 단계로 들어갑니다
→ BeanDefinition 등록 단계
```java
context.refresh(); // 여기서 BeanFactory 초기화와 등록이 일어남
```
## `ApplicationContext.refresh()` 내부 흐름
이 메서드가 진짜 핵심입니다.
스프링 컨테이너가 완성되는 대부분의 과정이 여기서 일어납니다.
아래는 실제 실행 순서예요
### (1) BeanFactory 생성
```java
ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
```
* BeanDefinitionRegistry로부터 BeanDefinition 로드 완료
* `DefaultListableBeanFactory` 생성
### (2) BeanFactoryPostProcessor 실행
```java
invokeBeanFactoryPostProcessors(beanFactory);
```
* `@Configuration` 클래스 처리 (`@Bean` 등록)
* `PropertySourcesPlaceholderConfigurer` 적용 (`${}` 값 치환)
> 즉, “빈 정의(설명서)”를 수정하는 단계예요.
### (3) BeanPostProcessor 등록
```java
registerBeanPostProcessors(beanFactory);
```
* `@Autowired`, `@Transactional`, `@Async` 등 후처리기 등록
> 즉, “빈을 만들면 어떻게 가공할지”를 미리 등록하는 단계입니다.
### (4) Bean 생성 (Instantiation)
```java
finishBeanFactoryInitialization(beanFactory);
```
이제 실제 Bean들이 생성됩니다.
1. `BeanDefinition` 조회
2. 생성자 호출 → 인스턴스 생성
3. 의존성 주입(DI) 수행
4. `@PostConstruct`, `InitializingBean.afterPropertiesSet()` 실행
5. BeanPostProcessor 호출 (`beforeInit`, `afterInit`)
6. 프록시(AOP) 적용 후 최종 Bean 등록
### (5) ApplicationContext 완성
모든 Bean이 생성되고,
스프링 컨테이너가 정상 가동 상태에 들어갑니다.
`ApplicationRunner`, `CommandLineRunner` 등이 실행되고,
웹 앱이라면 내장 톰캣(WebServer)이 이 시점에 올라옵니다.
## 전체 흐름 요약 다이어그램
```
main()
 ↓
SpringApplication.run()
 ↓
환경(Environment) 설정
 ↓
ApplicationContext 생성
 ↓
BeanDefinition 로딩
 ↓
BeanFactoryPostProcessor 실행
 ↓
BeanPostProcessor 등록
 ↓
Bean 인스턴스 생성 + 의존성 주입 + 초기화
 ↓
AOP 프록시 적용
 ↓
ApplicationContext refresh 완료
 ↓
CommandLineRunner 실행
 ↓
스프링 애플리케이션 정상 실행
```
## 결론 — "스프링이 자바보다 거대한 이유"
스프링은 단순히 “자바 클래스 실행기”가 아닙니다.
> 스프링은 자바 클래스를 읽고,
> 관계를 파악하고,
> 객체 생명주기를 통제하며,
> 프로그램 전체의 제어권을 가져가는 플랫폼이에요.
즉, 자바는 언어고,
스프링은 그 언어 위에 만들어진 “운영 체제” 같은 존재입니다
## 스프링의 “하나뿐인 중심”은 `ApplicationContext`
스프링에서 딱 하나인 가장 근본적인 객체는
`ApplicationContext` (스프링 컨테이너) 입니다.
### 역할:
`ApplicationContext`는
스프링의 모든 Bean을 담고 관리하는 “객체 저장소이자 제어센터”예요.
* BeanDefinition(설명서) 등록
* Bean 생성/초기화/소멸 관리
* 의존성 주입(DI)
* 이벤트 발행
* 리소스 로딩
* AOP, 트랜잭션 등 전역 기능 관리
### 왜 딱 하나만 있나?
> 스프링은 “하나의 컨테이너가 하나의 애플리케이션 컨텍스트”라는 철학을 가집니다.
이유는 명확합니다

| 이유                 | 설명                                |
| ------------------ | --------------------------------- |
| 일관된 Bean 관리    | Bean이 여러 컨테이너에 나뉘면 DI, AOP 적용이 꼬임 |
| 객체 공유 보장       | 싱글톤 Bean을 한 컨테이너 내에서만 유지해야 의미 있음  |
| 트랜잭션/캐시/보안 일관성 | 한 컨테이너가 전체 객체 생명주기를 조정해야 안전       |
| 성능 최적화         | Bean을 매번 만들지 않고, 한 번 만들어 재사용      |

즉, ApplicationContext는 전역 스코프의 “객체 관리자”이기 때문에
하나 이상 생기면 애플리케이션이 분열됩니다
## ApplicationContext 안에는 수많은 “싱글톤 Bean”이 있다
스프링의 기본 Bean Scope은 `singleton`입니다.
즉, 하나의 컨테이너 내에서 Bean은 1개 인스턴스만 존재합니다.
```java
@Component
public class UserService {}
```
위 Bean은 ApplicationContext 안에서
한 번만 생성되고, 모든 의존성에 주입될 때 재사용됩니다.
### 이유는?

| 이유            | 설명                                       |
| ------------- | ---------------------------------------- |
| 메모리 절약    | 같은 Bean을 계속 만들면 낭비                       |
| 상태 공유 일관성 | Service, Repository는 stateless하게 유지되어야 함 |
| 성능 최적화    | DI 시점마다 객체를 새로 만드는 오버헤드 방지               |
| AOP 안정성   | 프록시 적용 후 동일 객체를 재사용해야 함                  |

> 즉, “Bean은 하나의 컨테이너 안에서 싱글톤,
> 컨테이너 자체는 애플리케이션 전체에서 단 하나”인 구조입니다.
## 그래도 “하나만” 아닌 것도 있다
스프링이 모든 걸 하나로 만들진 않아요.
스코프(Scope)에 따라 달라집니다

| 스코프             | 생성 주기              | 사용 예시                             |
| --------------- | ------------------ | --------------------------------- |
| singleton   | 애플리케이션당 1개         | 대부분의 Bean (Service, Repository 등) |
| prototype   | 요청 시마다 새로 생성       | 특별한 상태 관리 객체                      |
| request     | HTTP 요청당 1개        | 웹 요청 스코프 (RequestContext)         |
| session     | 세션당 1개             | 로그인 정보 Bean                       |
| application | ServletContext당 1개 | 전역 리소스                            |

즉, 기본은 “하나”지만, 필요하면 컨테이너가 하나의 컨텍스트 안에서 여러 생명주기 Bean을 관리할 수 있습니다.
## “컨테이너 하나” → 보통은 Yes, 하지만 계층 구조도 있다
기본적으로는 `ApplicationContext`가 1개지만,
특정 구조에서는 여러 개의 컨테이너가 계층적으로 존재하기도 합니다.
예를 들어 Spring MVC 구조
```
Root ApplicationContext
  ├─ Service / Repository Bean (공통)
  └─ WebApplicationContext (DispatcherServlet)
        ├─ Controller / ViewResolver Bean
```
* RootContext : 전역 비즈니스 로직 (서비스, 리포지토리)
* WebContext : 웹 전용 빈 (컨트롤러, 뷰 리졸버)
> 하지만 여전히 “최상위 루트 컨텍스트”는 하나뿐이에요.
> 나머지는 하위 컨텍스트(자식)로 연결됩니다.
## 정리: 스프링에서 "딱 하나"인 것들

| 구분 | 이름 | 설명 |
| -- | -- | -- |
| 전역 컨테이너 | ApplicationContext | 스프링 애플리케이션의 뿌리, BeanFactory를 포함 |
| Bean 생성 단위 | BeanFactory (내부에 1개) | Bean 정의/생성/DI의 실제 엔진 |
| Bean 기본 스코프 | singleton Bean | 컨테이너당 Bean 1개 인스턴스 |
| 환경설정 | Environment | 설정값, 프로파일, 프로퍼티 관리 (전역 1개) |
| 이벤트 시스템 | ApplicationEventMulticaster | 스프링 이벤트 발행 시스템 (컨텍스트당 1개) |
| AOP 프록시 팩토리 | ProxyFactoryBean / AdvisorChainFactory | 프록시 생성을 담당하는 전역 엔진 |

> 스프링은 ApplicationContext(컨테이너) 하나를 중심으로,
> 그 안에 싱글톤 Bean들을 등록해 관리하는 구조입니다.
>
> 컨테이너가 여러 개면 세상(애플리케이션)이 분열되고,
> Bean이 여러 개면 정체성이 흐려지니까 —
>
> “컨테이너는 하나, Bean은 기본적으로 하나”가 스프링의 설계 철학이에요.
## 스프링이 싱글톤을 어떻게 보장하는가 = Bean이 어디에, 언제, 어떻게 저장되고 재사용되는가
* 스프링은 Bean을 “싱글톤 캐시(Map)”에 저장합니다.
* 이 캐시는 `DefaultSingletonBeanRegistry` 클래스 내부에 있습니다.
* Bean이 처음 생성될 때만 `new` 되고,
  다음부터는 이 캐시(Map)에서 재사용됩니다.
## Bean 생성의 실제 책임자: `DefaultSingletonBeanRegistry`
이 클래스는 `BeanFactory` 계층 구조의 가장 아래쪽에 위치한 핵심 클래스예요.
```java
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry
```
이름 그대로 “싱글톤을 등록하고 관리하는 저장소(Registry)”입니다.
## 내부 구조 (진짜 캐시 Map들)
`DefaultSingletonBeanRegistry` 내부를 보면 이렇게 생겼어요
```java
public class DefaultSingletonBeanRegistry {

    // 1단계: 완전히 생성된 싱글톤 Bean 저장소
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    // 2단계: 생성 중인 Bean 임시 저장소 (순환 참조 방지용)
    private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);

    // 3단계: ObjectFactory 저장소 (Bean 생성 전략 저장)
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
}
```
### 각 Map의 역할

| 캐시                      | 역할                             | 시점             |
| ----------------------- | ------------------------------ | -------------- |
| `singletonObjects`      | 완성된 Bean 저장소                   | Bean 생성 완료 후   |
| `earlySingletonObjects` | 순환 참조 방지용 임시 캐시                | Bean 생성 중간 단계  |
| `singletonFactories`    | Bean 생성 팩토리 (ObjectFactory) 저장 | Bean 인스턴스 생성 전 |

## Bean 생성 및 캐싱 흐름
스프링이 Bean을 생성할 때의 핵심 메서드는
`AbstractBeanFactory#getBean()` → `doGetBean()`
이 내부에서 다음 순서로 작동합니다
### 1단계: 캐시에서 Bean 검색
```java
Object sharedInstance = getSingleton(beanName);
if (sharedInstance != null) {
    return sharedInstance; // 이미 있으면 바로 반환
}
```
이미 `singletonObjects`에 Bean이 있으면,
다시 만들지 않고 바로 반환합니다.
### 2단계: 없으면 Bean 새로 생성
```java
sharedInstance = createBean(beanName, mbd, args);
```
이때 내부적으로는
`AbstractAutowireCapableBeanFactory#createBean()` → `doCreateBean()`으로 이동합니다.
### 3단계: Bean 생성 중간 캐시 등록 (순환참조 대비)
```java
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
```
즉, 아직 완성은 안 됐지만,
순환 참조가 발생할 경우 대비해서 `singletonFactories`에 ObjectFactory를 미리 넣습니다.
이걸 “early reference”라고 불러요.
### 4단계: Bean 인스턴스 생성, 의존성 주입, 초기화 수행
```java
Object beanInstance = doCreateBean(beanName, mbdToUse, args);
```
* 생성자 호출 (new)
* DI 수행 (`populateBean()`)
* 초기화 (`initializeBean()`)
* BeanPostProcessor 실행 (AOP, 프록시 등)
### 5단계: 생성 완료 후 캐시에 저장
```java
addSingleton(beanName, singletonObject);
```
`DefaultSingletonBeanRegistry`의 실제 구현
```java
protected void addSingleton(String beanName, Object singletonObject) {
    synchronized (this.singletonObjects) {
        this.singletonObjects.put(beanName, singletonObject);
        this.singletonFactories.remove(beanName);
        this.earlySingletonObjects.remove(beanName);
    }
}
```
> 이 시점에서 Bean은 `singletonObjects`에 등록되어
> 완성된 싱글톤 객체로 재사용 가능 상태가 됩니다.
### 6단계: 다음 요청 시 재사용
이후 `getBean(beanName)`이 다시 호출되면,
가장 먼저 `singletonObjects`에서 Bean을 꺼내 반환합니다.
→ 따라서 “한 컨테이너 안에서는 Bean이 한 번만 생성”.
## 싱글톤 보장 원리 (Thread-Safe)
* `singletonObjects`는 ConcurrentHashMap으로 선언되어 있어서
  멀티스레드 환경에서도 안전합니다.
* Bean 생성 중에는 synchronized 블록으로 잠금(lock)을 걸어
  “동시에 두 개 Bean이 생성되는 상황”을 방지합니다.
```java
synchronized (this.singletonObjects) {
    // 중복 생성 방지
}
```
## 순환 참조까지 처리 가능한 구조
이 3단계 캐시 구조 (`singletonFactories` → `earlySingletonObjects` → `singletonObjects`) 덕분에
스프링은 순환 참조도 안전하게 처리할 수 있습니다
예시:
```java
@Component
class A {
    @Autowired B b;
}

@Component
class B {
    @Autowired A a;
}
```
스프링은 아래처럼 동작합니다
1. A 생성 시작
   → 아직 완성 전이라 `singletonFactories`에 A 팩토리 등록
2. B 생성 시작
   → B가 A를 필요로 함
   → `getEarlyBeanReference(A)`를 통해 미완성 A 인스턴스 제공
3. B 완성
4. 다시 A 완성
5. 두 객체 모두 정상적으로 초기화 완료
## 요약
```
getBean("userService")
   ↓
getSingleton("userService") → 캐시에 없으면
   ↓
createBean()
   ↓
addSingletonFactory()  // 순환 참조 대비
   ↓
doCreateBean()
   ↓
addSingleton()         // 완성된 Bean 등록
   ↓
singletonObjects.put(beanName, bean)
   ↓
다음 getBean() 호출 시 캐시에서 즉시 반환
```
> 스프링의 싱글톤은 단순히 “객체를 한 번만 만든다”가 아니라,
> 컨테이너 내부 Map(`singletonObjects`)에 Bean을 캐싱해두고,
> 다음 호출 때 그 캐시를 재사용하는 구조입니다.
이 구조는
* Thread-safe
* 순환 참조 처리 가능
* AOP 프록시 호환
  하게 설계되어 있습니다.

| 개념                             | 설명                            |
| ------------------------------ | ----------------------------- |
| `ApplicationContext`           | 컨테이너 (BeanFactory를 포함한 큰 껍데기) |
| `BeanFactory`                  | Bean 생성·관리 핵심 엔진              |
| `DefaultSingletonBeanRegistry` | 실제 Bean 저장소 (싱글톤 캐시)          |
| `singletonObjects`             | 완성된 Bean 저장 Map               |
| `earlySingletonObjects`        | 순환참조 임시 저장 Map                |
| `singletonFactories`           | ObjectFactory 저장 Map          |

## 스프링이 Bean을 만들 때의 전체 흐름 다시 보기
Bean이 만들어질 때 스프링은 다음 순서로 진행합니다:
```
doCreateBean()
  ├─ new → 객체 생성
  ├─ 의존성 주입
  ├─ 초기화 전 콜백
  ├─ BeanPostProcessor.beforeInit()
  ├─ @PostConstruct
  ├─ BeanPostProcessor.afterInit() ← 프록시 생성 위치
  ├─ 최종 Bean 등록
```
여기서 핵심은 `afterInitialization()` 단계입니다.
바로 AOP 프록시가 생성되는 시점이에요.
## AOP 프록시 생성 시점
AOP 관련 BeanPostProcessor (예: `AnnotationAwareAspectJAutoProxyCreator`)가
`postProcessAfterInitialization()`을 오버라이드해서
Bean을 감싸 프록시 객체로 바꿉니다.
```java
@Override
public Object postProcessAfterInitialization(Object bean, String beanName) {
    if (needsProxy(bean)) {
        return createProxy(bean); // 프록시 생성
    }
    return bean; // 그대로 반환
}
```
이때 `createProxy(bean)`이 실제로는 CGLIB이나 JDK Proxy를 사용해서
원래 Bean을 감싸는 새 객체를 만듭니다.
## 중요한 포인트 — 프록시가 컨테이너에 “대체 등록”된다
이 프록시 객체는 단순히 리턴되는 게 아니라
컨테이너가 최종적으로 캐시에 넣는 객체가 이 프록시입니다.
즉, 이 시점부터는
“원래 Bean”이 아니라 “프록시 Bean”이
`singletonObjects` 캐시에 들어갑니다
```java
addSingleton(beanName, proxyInstance);
```
그래서 이후 `getBean("myService")`을 호출하면
스프링은 항상 이 “프록시”를 돌려줍니다.
## 이후에는 교체되지 않는다 (1회성 대체)
프록시는 한 번 만들어지면
Bean의 생명주기 전체 동안 그대로 유지됩니다.
* 프록시는 진짜 객체를 내부에 보관(delegate) 합니다.
* 프록시의 메서드를 호출하면 내부에서 진짜 객체 메서드가 호출됩니다.
* 트랜잭션이나 AOP 로직이 프록시의 `invoke()` 앞뒤에 추가되어 실행됩니다.
> 즉, “실제 객체는 프록시 안에 들어 있고,
> 밖에서 보이는 건 프록시뿐”이에요.
## 실제 구조 (예시)
예를 들어 이런 코드가 있다고 합시다
```java
@Service
@Transactional
public class OrderService {
    public void placeOrder() { ... }
}
```
스프링이 만드는 구조는 다음과 같아요:
```
orderService (beanName: orderService)
   └──> Proxy 객체 (CGLIB or JDK Dynamic Proxy)
            └── target: 실제 OrderService 인스턴스
```
컨테이너에 등록되는 것은 Proxy이고,
이 Proxy는 내부에 실제 객체(`target`)를 감싼 상태입니다.
## 이후 호출 동작
```java
OrderService service = context.getBean(OrderService.class);
service.placeOrder();
```
* 사실 `service` 변수에는 진짜 `OrderService`가 아니라 Proxy가 들어있습니다.
* `service.placeOrder()` 호출 시:
    1. Proxy가 메서드 호출을 가로챔 (`invoke()`)
    2. 트랜잭션 시작
    3. 내부 target.placeOrder() 호출
    4. 트랜잭션 커밋
    5. 반환
즉, Proxy는 계속 “입구 문” 역할을 합니다.
그 문 안에는 진짜 객체가 있고, 그 앞뒤에 공통 로직이 붙는 거죠.
## 프록시가 교체되지 않는 이유
한 번 프록시가 등록되면 바뀌지 않는 이유는 3가지예요

| 이유 | 설명 |
| -- | -- |
| 1. 싱글톤 캐시 구조 | `singletonObjects`에 프록시가 이미 들어가므로 재생성되지 않음 |
| 2. Bean 생명주기 단일성 | `postProcessAfterInitialization()`은 Bean 생성 시 단 1회 실행 |
| 3. AOP 안정성 | Bean이 바뀌면 프록시가 가리키는 target이 꼬일 수 있음 (불변 유지 필요) |

## “그럼 프록시를 거치지 않고 진짜 객체에 접근할 수 있나요?”
스프링 내부적으로는 가능하지만, 일반 개발자 코드에서는 불가능하거나 권장되지 않습니다.
프록시는 스프링이 컨테이너 레벨에서 완전히 교체해 버리기 때문에
`@Autowired`로 주입받는 모든 참조 역시 프록시 객체를 참조하게 됩니다.
> 즉, 우리가 보는 건 항상 프록시이고,
> 프록시 안에 진짜 객체가 있을 뿐이에요.
## 요약
```
1. new OrderService() → 실제 객체 생성
2. @Transactional 발견 → 프록시 필요 판단
3. postProcessAfterInitialization() 단계에서
   → Proxy(OrderServiceProxy) 생성
4. Proxy를 컨테이너에 최종 등록
5. getBean() 시 Proxy 반환
6. Proxy.invoke() → target(OrderService) 호출
```
> 한 번 프록시로 바뀌면,
> 그 프록시가 컨테이너에 등록되어 끝까지 유지됩니다.

| 질문 | 답변 |
| -- | -- |
| 프록시를 등록하면 쭉 쓰는 거야? | ✅ 네, 한 번 등록되면 컨테이너가 그걸 계속 사용합니다. |
| 객체가 나중에 바뀌거나 교체돼? | ❌ 아니요. 초기 Bean 생성 시 프록시로 대체되고, 이후에는 불변입니다. |
| 프록시는 내부에 진짜 객체를 품고 있지? | ✅ 네, `target` 필드로 실제 Bean을 감싸고 있어요. |
| DI 시 주입되는 것도 프록시야? | ✅ 맞습니다. 주입 대상들도 프록시 객체를 참조합니다. |
