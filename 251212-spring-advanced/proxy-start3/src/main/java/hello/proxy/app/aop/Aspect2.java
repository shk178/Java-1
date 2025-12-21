package hello.proxy.app.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

public class Aspect2 {
    @Aspect
    @Component
    @Order(1)
    public static class aOne {
        @Around("execution(* hello.proxy.app.v3.OrderService3.orderItem(..))")
        public Object run(ProceedingJoinPoint pjp) throws Throwable {
            System.out.println("[Service1]");
            return pjp.proceed();
        }
    }
    @Aspect
    @Component
    @Order(2)
    public static class aTwo {
        @Around("execution(* hello.proxy.app.v3.OrderService3.orderItem(..))")
        public Object run(ProceedingJoinPoint pjp) throws Throwable {
            System.out.println("[Service2]");
            return pjp.proceed();
        }
    }
}
/*
String hello.proxy.app.v3.OrderController3.req(String)
2025-12-21 17:25:05.561  INFO 5580 --- [    Test worker] hello.proxy.app.v3.OrderController3      : OrderController3.req: itemId=id1
[Service1]
[Service2]
void hello.proxy.app.v3.OrderService3.orderItem(String)
2025-12-21 17:25:05.566  INFO 5580 --- [    Test worker] hello.proxy.app.v3.OrderService3         : OrderService3.orderItem: itemId=id1
void hello.proxy.app.v3.OrderRepository3.save(String)
2025-12-21 17:25:05.570  INFO 5580 --- [    Test worker] hello.proxy.app.v3.OrderRepository3      : OrderRepository3.save: itemId=id1
 */