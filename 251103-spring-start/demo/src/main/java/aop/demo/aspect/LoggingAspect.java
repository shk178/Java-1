package aop.demo.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
//@Component //해당 클래스를 빈으로 등록
public class LoggingAspect {
    //@After("execution(public String getName())") /* getName() */
    //@After("execution(public String aop.demo.model.*.getName())")
    //@After("execution(public String aop.demo.model.*.get*())")
    //@After("execution(public * get*())") /* getCircle(), getTriangle(), getName() */
    @After("allGetters()")
    public void LoggingAdvice() {
        System.out.println("LoggingAspect.LoggingAdvice run");
    }
    @After("allGetters()")
    public void SecondAdvice() {
        System.out.println("LoggingAspect.SecondAdvice run");
    }
    @After("allCircleMethods()")
    public void ThirdAdvice() {
        System.out.println("LoggingAspect.ThirdAdvice run");
    }
    @After("allModelMethods()")
    public void FourthAdvice() {
        System.out.println("LoggingAspect.FourthAdvice run");
    }
    @After("allCircleMethods() || stringArgsMethods()") //or
    //@After("allCircleMethods() && stringArgsMethods()") //and
    public void FifthAdvice() {
        System.out.println("LoggingAspect.FifthAdvice run");
    }
    @Pointcut("execution(* get*())")
    public void allGetters() {}
    @Pointcut("execution(* aop.demo.model.Circle.*(..))")
    public void allCircleMethods() {}
    @Pointcut("within(aop..model.*)")
    public void allModelMethods() {}
    @Pointcut("args(java.lang.String)")
    public void stringArgsMethods() {}
}
