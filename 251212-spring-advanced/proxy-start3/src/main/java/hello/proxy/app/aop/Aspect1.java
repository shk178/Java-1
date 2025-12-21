package hello.proxy.app.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class Aspect1 {
    @Around("execution(* hello.proxy.app.v3..*(..))")
    public Object run(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println(pjp.getSignature());
        return pjp.proceed();
    }
}
