```
<<interface>> BeanFactory - 스프링 컨테이너 최상위 인터페이스, getBean 비롯한 대부분의 기능 제공
<<interface>> ApplicationContext - BeanFactory 기능 + 부가 기능(여러 인터페이스)
AnnotationConfigApplicationContext
```
- 틀린 표현: "ApplicationContext가 여러 인터페이스를 상속한다"
- 맞는 표현: "ApplicationContext가 여러 인터페이스를 확장(extends)한다"
## 인터페이스 관계 정리
```java
// ApplicationContext의 실제 정의
public interface ApplicationContext 
    extends EnvironmentCapable,        // 환경 변수 관련
            ListableBeanFactory,       // BeanFactory 확장
            HierarchicalBeanFactory,   // BeanFactory 확장
            MessageSource,             // 국제화 메시지
            ApplicationEventPublisher, // 이벤트 발행
            ResourcePatternResolver {  // 리소스 로딩 (ResourceLoader 확장)
    // ...
}
```
## 클래스 간 관계
- 클래스 → 클래스: 상속 (`extends`)
- 클래스 → 인터페이스: 구현 (`implements`)
## 인터페이스 간 관계
- 인터페이스 → 인터페이스: 확장 (`extends`) 여러 개 가능
## Spring 계층 구조
```
<<interface>> BeanFactory
    ↑ extends
<<interface>> ListableBeanFactory, HierarchicalBeanFactory
    ↑ extends
<<interface>> ApplicationContext
    (+ MessageSource, EnvironmentCapable, ApplicationEventPublisher, ResourceLoader도 extends)
    ↑ implements
AnnotationConfigApplicationContext (구체 클래스)
```
- 메세지 소스: 국제화 기능 (한국에서 들어오면 한국어로)
- 환경 변수: 로컬, 개발, 운영 등을 구분해서 처리
- 애플리케이션 이벤트: 이벤트를 발행하고 구독하는 모델 지원
- 리소스 로더: 파일, 클래스패스, 외부 등에서 리소스 조회
## 다양한 설정 형식 지원
```
<<interface>> ApplicationContext를 구현하는 구현체들
- AnnotationConfigApplicationContext: AppConfig.class // 애노테이션 기반 자바 코드 설정
- GenericXmlApplicationContext: appConfig.xml // XML 기반 설정 (컴파일 없이 빈 설정 정보 변경 가능)
- XxxApplicationContext: appConfig.xxx
```
- BeanDefinition 추상화로 다양한 설정 형식이 가능해진다.
- 역할과 구현을 나눈 것이다.
- `<<interface>>` BeanDefinition이 있고 여러 구현체가 있다.
- AnnotationConfigApplicationContext -> AnnotatedBeanDefinitionReader -> AppConfig.class -> BeanDefinition의 구현체
- GenericXmlApplicationContext -> XmlBeanDefinitionReader -> appConfig.xml -> BeanDefinition의 구현체
## BeanDefinition 확인
```java
public class CheckBeanDefinition {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
    @Test
    @DisplayName("빈 설정 메타정보 확인")
    void beanDefinitionMeta() {
        BeanDefinition beanDefinition = ac.getBeanDefinition("memberService");
        System.out.println(beanDefinition);
    }
    /*
> Task :testClasses
Root bean: class=null; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; 
autowireCandidate=true; primary=false; fallback=false; factoryBeanName=appConfig; factoryMethodName=memberService; 
initMethodNames=null; destroyMethodNames=[(inferred)]; defined in basic.lecture.AppConfig
> Task :test
     */
}
```
- 이건 스프링의 빈 메타정보(BeanDefinition) 를 직접 확인하는 대표적인 예시예요.
- 스프링이 내부적으로 `@Bean` 정의를 해석하면서 저장해둔 설정 정보를 보여주는 겁니다.
## 각 항목 해설

| 속성명 | 의미 | 현재 값 |
| --- | ------------------------------------- | ---- |
| Root bean: class=null | 실제 생성될 구체 클래스 타입을 나타냅니다. <br>보통 XML에서 `<bean class="com.example.Foo">`로 등록하면 `class=com.example.Foo`가 들어갑니다. | 지금은 `@Bean` 메서드(`AppConfig.memberService()`)로 등록된 빈이므로, 스프링이 “직접 클래스 이름을 지정받은 게 아니라, 팩토리 메서드로 만들어질 객체”라고 인식합니다. 그래서 `class=null`이에요. 대신 아래의 `factoryBeanName`과 `factoryMethodName`이 사용됩니다. |
| scope= | 빈의 스코프(scope) — 기본은 `"singleton"`. <br>여기서 비어있는 건 `"singleton"`의 기본값이기 때문이에요. | `@Bean` 정의는 기본적으로 싱글톤이므로 별도 표시가 생략됩니다. 실제 내부에서는 `"singleton"`으로 인식됩니다. |
| abstract=false | 이 빈 정의가 추상 빈인지 여부. 추상 빈은 보통 상속용으로만 쓰입니다. | `@Bean` 정의는 실제 인스턴스가 생성되므로 `false`. |
| lazyInit=null | 지연 초기화 여부. true면 처음 참조될 때 생성, false면 컨텍스트 초기화 시점에 생성. | 설정하지 않았으므로 기본값(null) → 즉, 즉시 초기화(eager init). |
| autowireMode=3 | 자동 주입 방식. 내부적으로 상수를 사용합니다. <br>3은 `AUTOWIRE_CONSTRUCTOR` 또는 `AUTOWIRE_BY_TYPE`으로 매핑됩니다. | `@Configuration` 클래스 내부에서는 메서드 간 호출 시 자동으로 주입을 처리하므로, `byType` 방식이 기본으로 설정됩니다. (숫자 3은 `AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR`) |
| dependencyCheck=0 | 의존성 검사 모드 (deprecated). 보통 0은 사용 안 함. | 자동 주입이 없으므로 기본값 0. |
| autowireCandidate=true | 다른 빈에서 이 빈을 자동 주입 대상으로 사용할 수 있는지 여부.  | `@Bean` 정의는 기본적으로 true입니다. |
| primary=false | `@Primary`가 붙은 빈은 자동 주입 시 우선순위를 갖습니다. | `@Primary`가 없으므로 false. |
| fallback=false | fallback 빈 여부. 스프링 내부용 (일반적으로 false). | `@Configuration` 기반 빈은 fallback 아님. |
| factoryBeanName=appConfig | 이 빈을 생성할 팩토리 빈의 이름입니다.                | `AppConfig` 클래스가 스프링 컨테이너에 빈으로 등록되었고, 그 안의 `memberService()` 메서드가 `@Bean`으로 정의되어 있습니다. 따라서 `factoryBeanName=appConfig`가 됩니다. |
| factoryMethodName=memberService | 위 팩토리 빈이 호출할 메서드 이름.                  | `AppConfig.memberService()` 메서드로 빈을 만들었기 때문이에요. |
| initMethodNames=null | 빈 초기화 시 호출할 메서드 이름. `@Bean(initMethod="...")`로 지정할 수 있습니다. | 지정하지 않았으므로 null. |
| destroyMethodNames=[(inferred)] | 소멸 시 호출할 메서드 이름. 스프링이 자동으로 추론할 수 있습니다. 예: `close()`나 `shutdown()`. | `DisposableBean` 구현체나 `close()` 메서드가 있으면 자동 추론되어 `(inferred)`로 표시됩니다. |
| defined in basic.lecture.AppConfig | 이 빈이 정의된 소스 위치.                       | `AppConfig.class`의 `@Bean` 메서드에서 정의되었으므로 이렇게 표시됩니다. |

## 요약
- BeanDefinition은 스프링이 빈 생성 설계도로 사용하는 내부 메타데이터입니다.
- XML, 자바 설정(@Configuration), 컴포넌트 스캔 모두 `BeanDefinition`으로 변환되어 관리됩니다.
- 여기서 `factoryBeanName`과 `factoryMethodName`은 “팩토리 메서드 방식”으로 빈이 생성됨을 의미합니다.
- `class=null`은 실제 클래스가 직접 지정된 게 아니라, `@Bean` 팩토리 메서드로 주입된다는 뜻입니다.
- 지금까지는 `@Configuration` 클래스(`AppConfig`) 안의 `@Bean` 메서드로 등록된 빈의 `BeanDefinition` 메타정보를 봤죠.
- 이제 그와 `@Component`로 등록된 빈의 메타정보를 비교해보면, 스프링이 내부적으로 두 가지 등록 방식을 어떻게 다르게 인식하는지 명확히 보입니다.
## `@Bean` 방식 (AppConfig 기반)
```java
@Configuration
public class AppConfig {
    @Bean
    public MemberService memberService() {
        return new MemberServiceImpl();
    }
}
```
## `@Component` 방식 (컴포넌트 스캔 기반)
```java
@Component
public class MemberServiceImpl implements MemberService {
}
```
## BeanDefinition 비교

| 속성 | `@Bean` 방식 (`AppConfig.memberService`) | `@Component` 방식 (`MemberServiceImpl`) |
| -- | ------ | ------ |
| beanClassName | `null` | `basic.member.MemberServiceImpl` (실제 클래스명) |
| factoryBeanName | `appConfig` | `null` |
| factoryMethodName | `memberService` | `null` |
| defined in | `basic.lecture.AppConfig` | `basic.member.MemberServiceImpl` |
| scope | `singleton` (기본값) | `singleton` (기본값) |
| autowireCandidate | `true` | `true` |
| primary | `false` | `false` |
| abstract | `false` | `false` |
| lazyInit | `null` (즉시 초기화) | `null` (즉시 초기화) |
| destroyMethodNames | `[inferred]` (자동 추론 가능 시) | `[inferred]` 또는 `null` |
| origin / role | ROLE_APPLICATION (사용자 정의 빈) | ROLE_APPLICATION (사용자 정의 빈) |

## 요약

| 구분 | 설명 |
| -- | -- |
| 1. 생성 방식 차이 | `@Bean` 방식은 팩토리 메서드 방식 (스프링이 `AppConfig.memberService()`를 호출해서 빈을 만듦).<br>`@Component` 방식은 직접 인스턴스화 방식 (스프링이 리플렉션으로 `new MemberServiceImpl()` 호출). |
| 2. BeanDefinition의 class 속성 | `@Component`는 실제 클래스를 직접 명시하므로 `class=com.example.MemberServiceImpl`로 표시됨.<br>`@Bean`은 메서드가 객체를 반환하므로 `class=null`, 대신 `factoryBeanName`과 `factoryMethodName`이 존재. |
| 3. 소스 위치 | `@Bean`: 정의된 위치가 `AppConfig.class`의 메서드.<br>`@Component`: 정의된 위치가 실제 클래스(`MemberServiceImpl.class`). |
| 4. 스프링이 관리하는 시점 | `@Bean`: `AppConfig`가 먼저 스프링에 등록된 후, 그 안의 메서드들이 실행되어 다른 빈 생성.<br>`@Component`: 컴포넌트 스캔 시점에 자동으로 등록됨. |
| 5. 리플렉션 사용 여부 | `@Bean`: 일반 메서드 호출 → 리플렉션 아님.<br>`@Component`: 리플렉션(newInstance) 으로 인스턴스 생성. |
| 6. 프록시 처리 여부 | `@Configuration` 클래스는 CGLIB 프록시로 감싸지므로, 내부 `@Bean` 호출 시 항상 싱글톤 보장됨.<br>`@Component`는 기본적으로 프록시 처리되지 않음. |

## 예시
```java
AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
BeanDefinition bd1 = ac.getBeanDefinition("memberService");
System.out.println(bd1);
```
→ 출력:
```
Root bean: class=null; factoryBeanName=appConfig; factoryMethodName=memberService; ...
```
```java
AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AutoAppConfig.class);
BeanDefinition bd2 = ac.getBeanDefinition("memberServiceImpl");
System.out.println(bd2);
```
→ 출력:
```
Root bean: class=basic.member.MemberServiceImpl; factoryBeanName=null; factoryMethodName=null; ...
```
> `@Bean` = “팩토리 메서드로 만들어진 빈” → `class=null`, `factoryBeanName`/`factoryMethodName` 존재
> `@Component` = “직접 인스턴스화된 빈” → `class=구체클래스명`, 팩토리 관련 속성 없음
- 이제 BeanDefinition의 Role(역할 구분)을 알아보죠.
- 이건 스프링이 내부적으로 등록된 모든 빈을 “누가, 어떤 목적으로 만들었는가”를 구분하기 위해 사용합니다.
## BeanDefinition의 `role` 속성
- 스프링은 모든 빈을 `BeanDefinition` 단위로 관리하면서, 각 빈이 어떤 “목적”으로 등록되었는지를 세 가지 상수로 구분합니다.

| 구분 | 상수명 | 역할 | 예시 |
| -- | --- | -- | -- |
| ROLE_APPLICATION | `0` | 사용자 애플리케이션 코드에서 직접 등록한 빈 | `@Bean`, `@Component`, `@Service`, `@Repository` 등 |
| ROLE_SUPPORT | `1` | 스프링이 자동 설정 시 내부적으로 지원하기 위한 보조 빈 | AOP 관련 어드바이저, 자동 프록시 크리에이터 등 |
| ROLE_INFRASTRUCTURE | `2` | 스프링 프레임워크 자체 동작을 위한 핵심 인프라 빈 | `ConfigurationClassPostProcessor`, `AutowiredAnnotationBeanPostProcessor` 등 |

| 역할 | 설명 | 우리가 수정할 가능성 |
| -- | -- |------|
| ROLE_APPLICATION | 우리가 직접 만든 애플리케이션 빈 | 자주 봄 |
| ROLE_SUPPORT | 스프링이 내부 기능을 지원하기 위해 추가로 등록하는 보조 빈 | 거의 손대지 않음 |
| ROLE_INFRASTRUCTURE | 스프링 컨테이너 자체가 돌아가려면 반드시 필요한 핵심 컴포넌트 | 절대 건드리지 않음 |

| Bean 이름 | 역할 | 이유 |
| ------- | -- | -- |
| `appConfig`, `memberService` 등 | ROLE_APPLICATION | 사용자가 직접 등록 (`@Configuration`, `@Bean`) |
| `internalConfigurationAnnotationProcessor` | ROLE_INFRASTRUCTURE | `@Configuration` 분석용 Processor (스프링 핵심) |
| `internalAutowiredAnnotationProcessor` | ROLE_INFRASTRUCTURE | `@Autowired` 처리용 PostProcessor |
| (기타) `support` 빈들 | ROLE_SUPPORT | 내부 기능 보조용 (예: 자동 프록시, 이벤트 리스너 등) |

> `role`은 “이 빈이 누가 왜 만들었는가”를 구분하는 스프링 내부용 메타정보이며,
> 일반 개발자는 ROLE_APPLICATION 빈만 직접 관리하면 됩니다.