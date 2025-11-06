# Spring 문제 해결 가이드

## 1. 순환 참조(Circular Dependency) 문제 해결

### 문제 상황
애플리케이션 시작 시 다음과 같은 오류가 발생:
```
APPLICATION FAILED TO START

Description:
The dependencies of some of the beans in the application context form a cycle:
   memberController → memberService → timeTraceAop → (순환)
```
## 문제 원인
### 순환 참조가 발생한 이유
1. AOP 포인트컷이 너무 광범위함
   - `TimeTraceAop`의 포인트컷: `execution(* hello.hello_spring..*(..))`
   - 이는 `hello.hello_spring` 패키지의 모든 메서드를 가로채려고 시도
2. 수동 Bean 등록 방식
   - `TimeTraceAop`가 `SpringConfig`에서 `@Bean`으로 수동 등록되고 있었음
3. 순환 참조 발생 과정
   ```
   MemberController 생성 
   → MemberService 주입 필요
   → MemberService는 @Bean으로 등록됨
   → TimeTraceAop가 MemberService를 가로채려고 함
   → TimeTraceAop 자체가 @Bean으로 등록되는 과정에서
   → AOP가 자기 자신(TimeTraceAop)을 가로채려고 함
   → 순환 참조 발생
   ```
## 해결 방법
### 1. AOP 클래스를 컴포넌트 스캔으로 자동 등록
변경 전:
```java
@Aspect
public class TimeTraceAop {
    // ...
}
```
SpringConfig.java:
```java
@Bean
public TimeTraceAop timeTraceAop() {
    return new TimeTraceAop();
}
```
변경 후:
```java
@Aspect
@Component  // 컴포넌트 스캔으로 자동 등록
public class TimeTraceAop {
    // ...
}
```
SpringConfig.java에서 `timeTraceAop()` Bean 등록 제거
### 2. 포인트컷에서 Config 클래스 제외
변경 전:
```java
@Around("execution(* hello.hello_spring..*(..))")
```
변경 후:
```java
@Around("execution(* hello.hello_spring..*(..)) && !execution(* hello.hello_spring.SpringConfig..*(..))")
```
Config 클래스의 메서드는 AOP가 적용되지 않도록 제외
### 최종 코드
### TimeTraceAop.java
```java
package hello.hello_spring.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimeTraceAop {
    @Around("execution(* hello.hello_spring..*(..)) && !execution(* hello.hello_spring.SpringConfig..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        System.out.println("START: " + joinPoint.toString());
        try {
            return joinPoint.proceed();
        } finally {
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            System.out.println("END: " + joinPoint.toString() + " " + timeMs + "ms");
        }
    }
}
```
### SpringConfig.java
```java
@Configuration
public class SpringConfig {
    private final MemberRepository memberRepository;
    
    public SpringConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    
    @Bean
    public MemberService memberService() {
        return new MemberService(memberRepository);
    }
    
    // TimeTraceAop는 @Component로 자동 등록되므로 Bean 등록 제거
}
```
### 핵심 포인트
1. AOP 클래스는 일반적으로 `@Component`로 자동 등록
   - Spring이 AOP 프록시를 생성할 때 자동으로 처리
   - 수동 Bean 등록은 불필요한 복잡성만 추가
2. Config 클래스는 포인트컷에서 제외
   - Config 클래스의 메서드는 AOP 대상이 아님
   - 순환 참조 방지 및 성능 최적화
3. 포인트컷은 최대한 구체적으로 작성
   - 불필요한 클래스/패키지는 제외
   - 예: `&& !execution(* hello.hello_spring.config..*(..))`
### 참고사항
- Spring Boot 2.6부터 순환 참조가 기본적으로 금지됨
- `spring.main.allow-circular-references=true`로 강제 허용 가능하지만 권장하지 않음
- 올바른 설계로 순환 참조를 근본적으로 해결하는 것이 중요