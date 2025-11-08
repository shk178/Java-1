말씀하신 라이브러리들은 Spring Framework의 AOP(Aspect-Oriented Programming) 기능을 구현하는 데 사용되었던 핵심 종속성들입니다. 현재 Spring과의 연관성을 설명해드리겠습니다:

## 1. AspectJ
- 과거: Spring AOP의 어노테이션 스타일(@Aspect, @Before, @After 등)과 포인트컷 표현식 문법을 제공
- 현재: 여전히 사용 중
    - Spring Boot는 `spring-boot-starter-aop`를 통해 AspectJ Weaver를 포함
    - `@EnableAspectJAutoProxy`로 AspectJ 스타일 AOP 활성화
    - 컴파일 타임/로드 타임 위빙(weaving)이 필요한 경우 직접 AspectJ 사용

## 2. AOP Alliance
- 과거: AOP 프레임워크 간 상호운용성을 위한 표준 인터페이스 제공
- 현재: 여전히 사용 중
    - Spring AOP의 기본 인터페이스(MethodInterceptor 등)가 AOP Alliance 기반
    - Spring 6.x에서도 `aopalliance:aopalliance:1.0` 의존성 포함

## 3. CGLIB
- 과거: 클래스 기반 프록시 생성에 사용 (인터페이스가 없는 클래스의 프록시)
- 현재: Spring Core에 내장됨
    - Spring 3.2부터 CGLIB을 리패키징하여 내부에 포함
    - 별도 의존성 추가 불필요
    - 여전히 클래스 프록시 생성에 사용

## 4. ASM
- 과거: CGLIB이 바이트코드 조작에 사용하던 저수준 라이브러리
- 현재: Spring Core에 내장됨
    - Spring 4.0부터 ASM을 리패키징하여 내부에 포함
    - 애노테이션 메타데이터 처리, CGLIB 지원 등에 사용
    - `spring-core` JAR 내부에 `org.springframework.asm` 패키지로 존재

## 현재 Spring Boot 프로젝트에서는?

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

이것만 추가하면 AspectJ와 AOP Alliance가 자동으로 포함되고, CGLIB/ASM은 이미 Spring Core에 내장되어 있어 별도 설정이 필요 없습니다.

요약: 예전에는 직접 관리해야 했던 종속성들이지만, 현재는 대부분 Spring에 통합되었거나 starter를 통해 자동 관리되어 개발자가 신경 쓸 일이 거의 없습니다.

AOP를 사용하려면 spring-boot-starter-aop를 추가해야 합니다:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-aop'  // 추가
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

## 이것만 추가하면 자동으로 포함되는 것들:

1. AspectJ Weaver - AspectJ 스타일 AOP 지원
2. AOP Alliance - AOP 표준 인터페이스
3. Spring AOP - Spring의 프록시 기반 AOP 구현
4. CGLIB, ASM - 이미 `spring-core`에 내장되어 있음

## 추가 후 사용 예시:

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Before("execution(* aop..*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("메서드 실행 전: " + joinPoint.getSignature());
    }
}
```

```java
@SpringBootApplication
@EnableAspectJAutoProxy  // AOP 활성화 (Spring Boot는 자동 설정되지만 명시적으로 추가 가능)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

참고: Spring Boot는 `spring-boot-starter-aop`가 클래스패스에 있으면 자동으로 AOP를 활성화하므로 `@EnableAspectJAutoProxy`는 생략 가능합니다.

AOP가 필요한 이유를 실제 코드 예시로 설명해드리겠습니다.

## 문제 상황: 중복 코드

AOP 없이 로깅, 트랜잭션, 보안을 구현하면 이렇게 됩니다:

```java
public class UserService {
    
    public void createUser(User user) {
        // 로깅
        System.out.println("createUser 시작: " + LocalDateTime.now());
        
        // 권한 체크
        if (SecurityContext.hasRole("ADMIN")) {
            throw new SecurityException("권한 없음");
        }
        
        // 트랜잭션 시작
        TransactionStatus tx = transactionManager.getTransaction();
        
        try {
            // 실제 비즈니스 로직 (이것만 하고 싶었음)
            userRepository.save(user);
            
            // 트랜잭션 커밋
            transactionManager.commit(tx);
            
            // 로깅
            System.out.println("createUser 성공: " + LocalDateTime.now());
            
        } catch (Exception e) {
            // 트랜잭션 롤백
            transactionManager.rollback(tx);
            
            // 에러 로깅
            System.out.println("createUser 실패: " + e.getMessage());
            throw e;
        }
    }
    
    public void updateUser(User user) {
        // 또 똑같은 코드 반복...
        System.out.println("updateUser 시작: " + LocalDateTime.now());
        
        if (SecurityContext.hasRole("ADMIN")) {
            throw new SecurityException("권한 없음");
        }
        
        TransactionStatus tx = transactionManager.getTransaction();
        // ... 반복 반복 반복
    }
    
    public void deleteUser(Long id) {
        // 또또또 반복...
    }
}
```

문제점:
- 핵심 비즈니스 로직(`userRepository.save()`)보다 부가 기능 코드가 더 많음
- 모든 메서드에 동일한 코드 반복
- 로깅 방식을 바꾸려면 100개 메서드를 다 수정해야 함
- 코드 가독성 낮음

## AOP로 해결

```java
// 핵심 비즈니스 로직만 남음
@Service
public class UserService {
    
    @Transactional
    @Secured("ADMIN")
    @Loggable
    public void createUser(User user) {
        userRepository.save(user);  // 이것만 하면 됨
    }
    
    @Transactional
    @Secured("ADMIN")
    @Loggable
    public void updateUser(User user) {
        userRepository.update(user);
    }
}

// 부가 기능은 한 곳에서 관리
@Aspect
@Component
public class LoggingAspect {
    
    @Around("@annotation(Loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println(joinPoint.getSignature() + " 시작: " + LocalDateTime.now());
        
        try {
            Object result = joinPoint.proceed();
            System.out.println(joinPoint.getSignature() + " 성공");
            return result;
        } catch (Exception e) {
            System.out.println(joinPoint.getSignature() + " 실패: " + e.getMessage());
            throw e;
        }
    }
}
```

## AOP가 필요한 실제 사례들

### 1. 성능 측정
```java
@Aspect
@Component
public class PerformanceAspect {
    
    @Around("execution(* aop.service.*.*(..))")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        
        System.out.println(joinPoint.getSignature() + " 실행 시간: " + (end - start) + "ms");
        return result;
    }
}
```
→ 모든 서비스 메서드의 실행 시간을 자동으로 측정

### 2. API 호출 로깅
```java
@Around("@annotation(org.springframework.web.bind.annotation.GetMapping)")
public Object logApiCalls(ProceedingJoinPoint joinPoint) throws Throwable {
    HttpServletRequest request = // ... 현재 요청 정보
    System.out.println("API 호출: " + request.getRequestURI());
    return joinPoint.proceed();
}
```
→ 모든 API 엔드포인트 호출을 자동 로깅

### 3. 캐시 처리
```java
@Around("@annotation(Cacheable)")
public Object cacheResult(ProceedingJoinPoint joinPoint) throws Throwable {
    String key = generateKey(joinPoint);
    
    if (cache.contains(key)) {
        return cache.get(key);
    }
    
    Object result = joinPoint.proceed();
    cache.put(key, result);
    return result;
}
```
→ 메서드 결과를 자동으로 캐싱

### 4. 재시도 로직
```java
@Around("@annotation(Retry)")
public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
    int attempts = 0;
    while (attempts < 3) {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            attempts++;
            if (attempts >= 3) throw e;
            Thread.sleep(1000);
        }
    }
}
```
→ 실패 시 자동으로 재시도

## 핵심 장점

1. 관심사의 분리 (Separation of Concerns)
  - 비즈니스 로직과 부가 기능을 완전히 분리
  - 각자의 역할에만 집중 가능

2. 중복 코드 제거
  - 공통 기능을 한 곳에서 관리
  - 수정이 필요하면 Aspect만 수정

3. 유지보수성 향상
  - 로깅 방식 변경? → Aspect 하나만 수정
  - 새로운 메서드 추가? → 자동으로 AOP 적용

4. 코드 가독성
  - 핵심 로직만 남아서 코드가 깔끔함

결론: AOP는 "공통으로 사용되는 부가 기능을 핵심 비즈니스 로직에서 분리"하여 코드 중복을 없애고 유지보수를 쉽게 만드는 프로그래밍 패러다임입니다.

좋은 질문입니다 스프링의 핵심 철학과 AOP의 관계를 설명해드리겠습니다.

## 스프링의 핵심 철학

스프링은 "좋은 객체지향 설계"를 돕기 위한 프레임워크입니다. 그 중심에는:

```
1. IoC/DI (제어의 역전 / 의존성 주입)
2. AOP (관점 지향 프로그래밍)
3. PSA (이식 가능한 서비스 추상화)
```

이 세 가지가 서로 협력하여 SOLID 원칙을 쉽게 지킬 수 있게 해줍니다.

## 객체지향의 한계와 AOP의 등장

### 문제: 객체지향만으로는 해결 안 되는 것들

```java
// 객체지향 설계로 만든 깔끔한 서비스
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    
    public void createOrder(Order order) {
        // 핵심 비즈니스 로직
        validateOrder(order);
        orderRepository.save(order);
        paymentService.process(order);
    }
}

public class UserService {
    private final UserRepository userRepository;
    
    public void createUser(User user) {
        // 핵심 비즈니스 로직
        validateUser(user);
        userRepository.save(user);
    }
}

public class ProductService {
    private final ProductRepository productRepository;
    
    public void createProduct(Product product) {
        // 핵심 비즈니스 로직
        validateProduct(product);
        productRepository.save(product);
    }
}
```

여기에 "모든 메서드 실행 시간을 로깅하라"는 요구사항이 생기면?

### 안 좋은 해결책 1: 각 메서드에 직접 추가

```java
public class OrderService {
    public void createOrder(Order order) {
        long start = System.currentTimeMillis();  // 중복
        
        validateOrder(order);
        orderRepository.save(order);
        paymentService.process(order);
        
        long end = System.currentTimeMillis();  // 중복
        log.info("실행시간: " + (end - start));  // 중복
    }
}

public class UserService {
    public void createUser(User user) {
        long start = System.currentTimeMillis();  // 또 중복
        // ... 반복
    }
}
```

문제점:
- SRP 위반: 비즈니스 로직 + 로깅 책임이 섞임
- OCP 위반: 새로운 기능 추가 시 모든 클래스 수정 필요
- 코드 중복의 향연

### 안 좋은 해결책 2: 상속 사용

```java
public abstract class LoggableService {
    protected void logExecutionTime(Runnable task) {
        long start = System.currentTimeMillis();
        task.run();
        long end = System.currentTimeMillis();
        log.info("실행시간: " + (end - start));
    }
}

public class OrderService extends LoggableService {
    public void createOrder(Order order) {
        logExecutionTime(() -> {
            validateOrder(order);
            orderRepository.save(order);
            paymentService.process(order);
        });
    }
}
```

문제점:
- 자바는 단일 상속만 가능 (다른 클래스 상속 불가)
- 상속은 강한 결합을 만듦
- 여전히 모든 메서드에 `logExecutionTime()` 호출 필요

### 안 좋은 해결책 3: 데코레이터 패턴

```java
public class LoggingOrderService implements OrderService {
    private final OrderService delegate;
    
    public void createOrder(Order order) {
        long start = System.currentTimeMillis();
        delegate.createOrder(order);
        long end = System.currentTimeMillis();
        log.info("실행시간: " + (end - start));
    }
}

// 사용
OrderService service = new LoggingOrderService(
    new TransactionalOrderService(
        new SecurityOrderService(
            new OrderServiceImpl()
        )
    )
);
```

문제점:
- 데코레이터 클래스를 일일이 만들어야 함
- 설정이 복잡해짐
- 여전히 보일러플레이트 코드 다량

## AOP의 해결책: 횡단 관심사의 분리

```java
// 비즈니스 로직은 깔끔하게 유지
@Service
public class OrderService {
    public void createOrder(Order order) {
        validateOrder(order);
        orderRepository.save(order);
        paymentService.process(order);
    }
}

// 로깅은 완전히 분리된 곳에서 관리
@Aspect
@Component
public class LoggingAspect {
    
    @Around("execution(* aop.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        
        log.info(joinPoint.getSignature() + " 실행시간: " + (end - start));
        return result;
    }
}
```

## 스프링 핵심 기능들과의 관계

### 1. DI와 AOP의 협력

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;  // DI로 주입
    private final PaymentService paymentService;    // DI로 주입
    
    // AOP가 이 메서드를 감싸는 프록시 생성
    @Transactional
    public void createOrder(Order order) {
        orderRepository.save(order);
        paymentService.process(order);
    }
}
```

DI 컨테이너가:
1. `OrderService` 빈 생성
2. AOP 프록시로 감싸기
3. 프록시를 다른 빈에 주입

```
Controller → [AOP Proxy] → OrderService (실제 객체)
                ↓
          트랜잭션, 로깅 처리
```

### 2. SOLID 원칙과 AOP

#### SRP (단일 책임 원칙)
```java
// AOP 없이: 2개 책임 (비즈니스 로직 + 로깅)
public void createOrder(Order order) {
    log.info("시작");
    // 비즈니스 로직
    log.info("종료");
}

// AOP 사용: 1개 책임 (비즈니스 로직만)
public void createOrder(Order order) {
    // 비즈니스 로직만
}
```

#### OCP (개방-폐쇄 원칙)
```java
// 새로운 기능(성능 모니터링) 추가 시
// 기존 코드 수정 없이 Aspect만 추가
@Aspect
@Component
public class PerformanceAspect {
    @Around("execution(* aop.service.*.*(..))")
    public Object monitor(ProceedingJoinPoint joinPoint) {
        // 성능 모니터링 로직
    }
}
```

#### DIP (의존성 역전 원칙)
```java
// 서비스는 구체적인 로깅 방식에 의존하지 않음
// Aspect가 변경되어도 서비스 코드는 그대로
@Service
public class OrderService {
    // 로깅 코드에 의존하지 않음
    public void createOrder(Order order) {
        // ...
    }
}
```

### 3. 스프링의 실제 AOP 활용

스프링이 내부적으로 AOP를 사용하는 예:

```java
// @Transactional - 트랜잭션 관리
@Transactional  // AOP로 구현됨
public void transferMoney(Account from, Account to, Money amount) {
    from.withdraw(amount);
    to.deposit(amount);
    // 자동으로 커밋/롤백 처리
}

// @Cacheable - 캐싱
@Cacheable("products")  // AOP로 구현됨
public Product findById(Long id) {
    return repository.findById(id);
}

// @Async - 비동기 처리
@Async  // AOP로 구현됨
public void sendEmail(String to) {
    // 비동기로 실행됨
}

// @PreAuthorize - 보안
@PreAuthorize("hasRole('ADMIN')")  // AOP로 구현됨
public void deleteUser(Long id) {
    userRepository.deleteById(id);
}
```

모두 프록시 패턴 + AOP로 구현되어 있습니다

## 관계도 정리

```
객체지향의 목표: 좋은 설계 (SOLID 원칙)
         ↓
문제: 횡단 관심사(로깅, 트랜잭션 등)는 객체지향으로 해결 어려움
         ↓
해결책: AOP
         ↓
스프링이 제공하는 것:
1. DI 컨테이너 - 객체 생성과 의존성 관리
2. AOP 프록시 - 횡단 관심사 분리
3. 이 둘의 통합 - 깔끔한 코드
```

## 결론

AOP는 객체지향의 한계를 보완하는 패러다임입니다:

- 객체지향: 수직적 관심사 분리 (도메인 로직)
- AOP: 수평적(횡단) 관심사 분리 (공통 기능)

스프링은 DI + AOP를 결합하여:
1. 객체지향 설계 원칙(SOLID)을 쉽게 지키고
2. 중복 코드를 제거하며
3. 관심사를 깔끔하게 분리할 수 있게 해줍니다

이것이 스프링이 "좋은 객체지향 프레임워크"라고 불리는 이유입니다

## AOP vs AOP 프록시

```
AOP (Aspect-Oriented Programming)
→ 프로그래밍 패러다임, 개념

AOP 프록시 (AOP Proxy)
→ AOP를 구현하는 기술적 방법 중 하나
```

## 1. AOP (개념)

"횡단 관심사를 모듈화하는 프로그래밍 방식"

```java
// 이것이 AOP의 개념
// "메서드 실행 전후에 로깅을 추가하겠다"
@Aspect
@Component
public class LoggingAspect {
    
    @Around("execution(* aop.service.*.*(..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("메서드 실행 전");
        Object result = joinPoint.proceed();
        System.out.println("메서드 실행 후");
        return result;
    }
}
```

## 2. AOP 프록시 (구현 방법)

"AOP를 실제로 구현하는 기술적 메커니즘"

Spring이 런타임에 생성하는 대리 객체입니다.

```java
@Service
public class UserService {
    public void createUser(User user) {
        System.out.println("실제 비즈니스 로직");
    }
}

// Spring이 실제로 생성하는 것:
// UserService 프록시 (가짜 객체)
//      ↓ 호출 위임
// UserService 실제 객체 (진짜 객체)
```

## AOP를 구현하는 3가지 방법

### 방법 1: 컴파일 타임 위빙 (AspectJ 컴파일러)

```java
// 원본 코드
public void createUser(User user) {
    userRepository.save(user);
}

// 컴파일 시 AspectJ 컴파일러가 바이트코드에 직접 삽입
public void createUser(User user) {
    System.out.println("로깅 시작");  // ← 컴파일 시 추가됨
    userRepository.save(user);
    System.out.println("로깅 종료");  // ← 컴파일 시 추가됨
}
```

프록시 없음 바이트코드 자체가 변경됩니다.

### 방법 2: 로드 타임 위빙 (클래스 로딩 시)

```java
// .class 파일 로딩 시 JVM 에이전트가 바이트코드 수정
java -javaagent:aspectjweaver.jar MyApp
```

프록시 없음 클래스 로더가 바이트코드를 수정합니다.

### 방법 3: 런타임 프록시 (Spring AOP)

```java
// Spring이 런타임에 프록시 객체 생성
UserService proxy = createProxy(realUserService);

// 내부 동작
public class UserServiceProxy extends UserService {
    private UserService target;
    
    public void createUser(User user) {
        System.out.println("로깅 시작");  // 프록시가 추가
        target.createUser(user);          // 실제 객체 호출
        System.out.println("로깅 종료");  // 프록시가 추가
    }
}
```

프록시 사용 Spring이 기본적으로 사용하는 방식입니다.

## Spring AOP 프록시의 동작 방식

### JDK 동적 프록시 (인터페이스 기반)

```java
// 인터페이스가 있는 경우
public interface UserService {
    void createUser(User user);
}

@Service
public class UserServiceImpl implements UserService {
    public void createUser(User user) {
        System.out.println("실제 로직");
    }
}

// Spring이 생성하는 프록시
UserService proxy = (UserService) Proxy.newProxyInstance(
    classLoader,
    new Class[]{UserService.class},
    new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) {
            // AOP 로직 실행
            System.out.println("프록시: 메서드 실행 전");
            Object result = method.invoke(target, args);  // 실제 객체 호출
            System.out.println("프록시: 메서드 실행 후");
            return result;
        }
    }
);
```

### CGLIB 프록시 (클래스 기반)

```java
// 인터페이스가 없는 경우
@Service
public class UserService {  // 인터페이스 없음
    public void createUser(User user) {
        System.out.println("실제 로직");
    }
}

// Spring이 생성하는 프록시 (상속 방식)
public class UserService$$EnhancerBySpringCGLIB extends UserService {
    private UserService target;
    
    @Override
    public void createUser(User user) {
        // AOP 로직
        System.out.println("프록시: 메서드 실행 전");
        super.createUser(user);  // 부모 클래스 호출
        System.out.println("프록시: 메서드 실행 후");
    }
}
```

## 실제 동작 확인하기

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = 
            SpringApplication.run(Application.class, args);
        
        UserService service = context.getBean(UserService.class);
        
        // 프록시 객체임을 확인
        System.out.println(service.getClass().getName());
        // 출력: aop.service.UserService$$EnhancerBySpringCGLIB$$12345678
        //                                  ↑ 프록시 클래스
    }
}
```

## 프록시의 한계

### 1. 내부 호출 문제

```java
@Service
public class UserService {
    
    @Transactional
    public void createUser(User user) {
        // 트랜잭션 적용됨 (외부에서 호출)
        userRepository.save(user);
    }
    
    public void registerUser(User user) {
        validateUser(user);
        this.createUser(user);  // 프록시를 거치지 않음
    }
}

// 실제 동작
[Proxy] → registerUser() → this.createUser()
                              ↑
                     프록시를 우회해서 직접 호출
                     트랜잭션 적용 안 됨
```

이유: `this`는 프록시가 아닌 실제 객체를 가리킵니다.

### 2. private/final 메서드는 AOP 적용 안 됨

```java
@Service
public class UserService {
    
    @Transactional
    private void createUser(User user) {  // private
        userRepository.save(user);
    }
    
    @Transactional
    public final void updateUser(User user) {  // final
        userRepository.update(user);
    }
}
```

이유:
- JDK 프록시는 인터페이스 메서드만 가능
- CGLIB은 상속 기반이라 `private`, `final` 오버라이드 불가

## 정리

| 구분 | AOP | AOP 프록시 |
|------|-----|-----------|
| 정의 | 프로그래밍 패러다임 | AOP 구현 기술 |
| 레벨 | 개념적 | 구현적 |
| 예시 | "횡단 관심사 분리" | "동적으로 생성된 대리 객체" |
| 언제 | 설계/코드 작성 시 | 런타임 시 |

```
AOP = "무엇을" 할 것인가 (What)
AOP 프록시 = "어떻게" 구현할 것인가 (How)
```

Spring AOP = AOP(개념)를 프록시(기술)로 구현한 것 입니다