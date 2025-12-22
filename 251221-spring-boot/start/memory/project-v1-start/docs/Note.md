1. Spring Boot 자동 구성이 해결하고자 하는 주요 문제점은 무엇일까요?

A
애플리케이션 빌드 시간 지연 문제 해결

개발자의 반복적인 빈(Bean) 설정 작업 최소화

C
데이터베이스 연결 오류 자동 수정

D
실시간 메모리 사용량 모니터링 기능 제공



해설
스프링 부트의 자동 구성은 DataSource, JdbcTemplate 등 자주 사용되는 객체들을 개발자가 직접 빈으로 등록하는 반복적인 과정을 자동화하여 편리함을 제공합니다.
2. 다음 중 스프링 부트가 관련 의존성이 있을 때 일반적으로 자동 구성하는 빈은 무엇일까요?

A
사용자가 정의한 회원 컨트롤러(MemberController)

DataSource, JdbcTemplate, PlatformTransactionManager

C
SpringApplication, ApplicationContext

D
Lombok 어노테이션 프로세서



해설
스프링 부트는 개발자들이 데이터베이스 연동 시 자주 사용하는 DataSource, JdbcTemplate, PlatformTransactionManager 등의 인프라 관련 빈들을 자동으로 등록해줍니다.
3. Spring Boot 자동 구성에서 `@Conditional` 어노테이션의 핵심적인 역할은 무엇인가요?

A
빈 간의 의존성 주입 순서 강제

특정 조건이 만족될 때만 해당 설정이나 빈 등록

C
애플리케이션 프로파일(profile) 선택

D
자동 구성 캐싱 기능 활성화



해설
`@Conditional` 어노테이션은 지정된 조건(예: 특정 클래스 존재 여부, 환경 속성 값)이 참일 경우에만 해당 설정 클래스나 빈 정의가 유효하게 만드는 역할을 합니다.
4. 스프링 Environment에 특정 속성이 존재하고 그 값이 특정 값과 일치할 때 활성화되는 `@Conditional` 구현체는 무엇일까요?

A
`@ConditionalOnClass`

B
`@ConditionalOnBean`

`@ConditionalOnProperty`

D
`@ConditionalOnResource`



해설
`@ConditionalOnProperty`는 애플리케이션의 환경 속성(application.properties 등)에 특정 키와 값이 일치하는지를 조건으로 사용하여 설정 적용 여부를 결정합니다.
5. 외부 라이브러리를 개발하여 제공할 때 Spring Boot 자동 구성을 포함시키는 주된 목적은 무엇일까요?

A
라이브러리 자체의 성능 최적화

라이브러리를 사용하는 개발자(클라이언트)의 설정 편의성 제공

C
라이브러리 내부 구현 숨기기

D
외부 종속성 제거



해설
라이브러리 제공자는 자동 구성을 통해 라이브러리 사용에 필요한 빈들을 자동으로 등록해주므로, 클라이언트 개발자는 최소한의 설정만으로 기능을 사용할 수 있게 됩니다.
6. Spring Boot는 외부 라이브러리에서 자동 구성 대상을 찾기 위해 주로 어떤 파일을 확인하나요?

A
`/src/main/resources/application.properties`

B
`/META-INF/spring.factories` (이전 방식)

`/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

D
`/META-INF/MANIFEST.MF`



해설
스프링 부트 2.7부터는 자동 구성 등록 방식이 변경되어, 라이브러리 내 `/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일에 자동 구성 클래스 목록을 명시합니다.
7. Spring Boot 애플리케이션의 메인 클래스에 주로 붙는 `@EnableAutoConfiguration` 어노테이션의 주요 기능은 무엇일까요?

A
Spring MVC 설정 활성화

클래스 경로 기반 자동 구성 프로세스 시작

C
내장 웹 서버 설정

D
Lombok 기능 활성화



해설
`@EnableAutoConfiguration`은 스프링 부트 애플리케이션 실행 시 클래스 경로에 있는 라이브러리들을 기반으로 자동 구성 후보들을 찾고 조건을 만족하는 설정을 적용하도록 지시합니다.
8. Spring Framework에서 `@Import` 어노테이션과 함께 사용하여, 프로그래밍 방식으로 동적으로 가져올(import) 설정 클래스를 결정하는 인터페이스는 무엇인가요?

A
BeanFactoryPostProcessor

B
BeanDefinitionRegistryPostProcessor

ImportSelector

D
ApplicationContextInitializer



해설
`ImportSelector` 인터페이스를 구현하면 `selectImports` 메소드의 반환 값(설정 클래스의 FQCN 배열)에 따라 `@Import` 할 대상을 동적으로 선택할 수 있습니다. 스프링 부트의 자동 구성도 이를 활용합니다.
9. `@AutoConfiguration`으로 어노테이션된 클래스가 Spring Boot의 기본 컴포넌트 스캔 대상에서 제외되는 주된 이유는 무엇일까요?

A
빈 등록 충돌 방지

자동 구성 전용 로직으로 관리되어야 함

C
메모리 효율성 증대

D
AOP 프록시 적용 제외



해설
자동 구성 클래스는 `@EnableAutoConfiguration`을 통한 특정 프로세스에 의해 관리 및 등록되어야 하며, 일반 컴포넌트 스캔에 포함될 경우 예상치 못한 문제가 발생하거나 의도가 깨질 수 있습니다.
10. 강의 예제에서 테스트 및 실습 편의를 위해 H2 데이터베이스를 설정할 때 주로 사용된 모드는 무엇이었나요?

A
파일 시스템 모드

B
TCP 서버 모드

인메모리 모드

D
웹 콘솔 모드



해설
JVM 내에서 실행되고 애플리케이션 종료 시 데이터가 휘발되는 인메모리 모드는 테스트 환경에서 빠르고 간편하게 데이터베이스를 사용할 때 유용합니다. 예제에서 이 모드가 사용되었습니다.