package aop.demo.aspect;

import aop.demo.model.Circle;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import java.lang.reflect.Field;

@Aspect
public class Advices {
    @Before("allCircleMethods()")
    public void one(JoinPoint joinPoint) {
        System.out.println(joinPoint.toString()); //execution(String aop.demo.model.Circle.getName())
        System.out.println(joinPoint.getTarget()); //aop.demo.model.Circle@479460a6
        Circle circle = (Circle) joinPoint.getTarget();
        circle.setName("c2");
    }
    @Pointcut("execution(* aop.demo.model.Circle.*(..))")
    public void allCircleMethods() {}
    @Before("args(name)")
    public void two(JoinPoint joinPoint, String name) throws Exception {
        Object target = joinPoint.getTarget();
        Field beforeName = target.getClass().getDeclaredField("name");
        beforeName.setAccessible(true);
        Object fieldValue = beforeName.get(target);
        System.out.println("Advices.two run: " + name + " (beforeName=" + fieldValue + ")");
    }
    @AfterReturning(pointcut="args(name)", returning="returnStr")
    public void three(String name, String returnStr) {
        System.out.println("Advices.three run: name2=" + name + ", returnString=" + returnStr);
    }
    @Around("allCircleMethods()")
    public Object AroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object obj = pjp.proceed();
        System.out.println("pjp.proceed() returns=" + obj);
        return obj;
    }
    @Around("@annotation(aop.demo.aspect.Loggable)")
    public Object AroundTwo(ProceedingJoinPoint pjp) throws Throwable {
        Object obj = pjp.proceed();
        System.out.println("pjp.proceed() returns=" + obj);
        return obj;
    }
}
