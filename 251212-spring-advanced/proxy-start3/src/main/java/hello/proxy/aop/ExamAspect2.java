package hello.proxy.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ExamAspect2 {
    @Around("@annotation(examAnnotation2)")
    public Object run(ProceedingJoinPoint pjp, ExamAnnotation2 examAnnotation2) throws Throwable {
        int maxRetry = examAnnotation2.value();
        Exception eHolder = null;
        int retryCount;
        for (retryCount = 1; retryCount <= maxRetry; retryCount++) {
            try {
                System.out.println("retryCount = " + retryCount);
                return pjp.proceed();
            } catch (Exception e) {
                eHolder = e;
            }
        }
        //throw eHolder;
        return null;
    }
}
