- ProxyFactory + Advisor(Pointcut, Advice)
- pointcut는 classfilter(advisor 포함 여부), methodmatcher(메서드 적용 여부)를 설정한다.
- advice는 MethodBeforeAdvice(메서드 실행 전 로직), AfterReturningAdvice(메서드 실행 후 로직), ThrowsAdvice(예외 공통 처리), MethodInterceptor(메서드 가로채서 실행을 직접 설정)를 구현해서 만들 수 있다.
- proxyfactory는 methodinterceptor(다른 advice도 어댑터가 변환)로 타겟=구현클래스에는 jdk 프록시(같은 인터페이스를 구현한 클래스)를, 타겟=구체클래스 또는 factory.setProxyTargetClass(true);를 설정한 경우에는 cglib 프록시(해당 클래스를 상속한 클래스)를 만든다.
- 어떤 프록시든 메서드 내부에서 호출하는 메서드는 프록시 적용되지 않는다.
- jdk 프록시의 경우 타겟클래스로 캐스팅하면 안 되고 인터페이스로 캐스팅해야 한다. 타겟 객체의 메서드는 invocationHandler.invoke하면 호출된다.
- cglib 프록시의 경우 final 메서드는 오버라이드 안 된다. final 클래스의 경우 상속이 안 돼서 프록시도 안 만들어진다.

- proxyfactory를 스프링 빈마다 정의하면 설정의 양이 늘어난다.
- 컴포넌트스캔의 경우 스프링 빈이 자동 등록돼서 proxyfactory를 정의할 수 없다.

- 빈 후처리기 BeanPostProcessor 
```
1. 객체 생성 (new)
2. 의존성 주입
3. @PostConstruct
4. BeanPostProcessor.beforeInitialization
5. 초기화 메서드
6. BeanPostProcessor.afterInitialization // 여기서 프록시 반환
7. 컨테이너 등록
```
- 스프링 AOP: AbstractAutoProxyCreator, AnnotationAwareAspectJAutoProxyCreator가 Advisor가 있으면 프록시를 생성해서 반환하고 아니면 원본 빈을 반환한다. (포인트컷에 클래스나 메서드가 맞으면 프록시 적용)
- 스프링 AOP로만 프록시를 만들면 프록시는 1개이고 어드바이스->인터셉터 여러 개가 메서드 체인으로 실행된다.
- @Aspect는 컨테이너 초기화 중 AnnotationAwareAspectJAutoProxyCreator라는 빈 후처리기에 의해 Advisor로 변환되고, 각 빈이 생성될 때 해당 Advisor가 적용 가능하면 프록시로 만들어진다.
- @Aspect도 일반 빈이라서 생성 순서가 달라질 수 있지만, 빈 후처리기에서 Advisor의 메타데이터를 수집하는 것은 항상 먼저 된다.
- @Aspect 하나에 여러 Advice 메서드가 선언될 수 있다. 각 Advice 메서드에 대해 Advisor가 1개 만들어진다.
- Advisor는 빈이 아니라 AOP 인프라 객체로서 빈 후처리기 내부에서 메타데이터를 기반으로 필요 시 생성·캐싱되어 프록시가 공유한다.