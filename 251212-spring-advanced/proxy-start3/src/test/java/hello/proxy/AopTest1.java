package hello.proxy;

import hello.proxy.app.v3.OrderController3;
import hello.proxy.app.v3.OrderRepository3;
import hello.proxy.app.v3.OrderService3;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AopTest1 {
    @Autowired
    OrderController3 orderController3;
    @Autowired
    OrderService3 orderService3;
    @Autowired
    OrderRepository3 orderRepository3;
    @Test
    void one() {
        System.out.println("AopUtils.isAopProxy(orderController3) = " + AopUtils.isAopProxy(orderController3));
        System.out.println("AopUtils.isAopProxy(orderService3) = " + AopUtils.isAopProxy(orderService3));
        System.out.println("AopUtils.isAopProxy(orderRepository3) = " + AopUtils.isAopProxy(orderRepository3));
        /*
AopUtils.isAopProxy(orderController3) = true
AopUtils.isAopProxy(orderService3) = true
AopUtils.isAopProxy(orderRepository3) = true
         */
    }
    @Test
    void two() {
        orderController3.req("id1");
        /*
String hello.proxy.app.v3.OrderController3.req(String)
2025-12-21 17:15:48.579  INFO 4276 --- [    Test worker] hello.proxy.app.v3.OrderController3      : OrderController3.req: itemId=id1
void hello.proxy.app.v3.OrderService3.orderItem(String)
2025-12-21 17:15:48.584  INFO 4276 --- [    Test worker] hello.proxy.app.v3.OrderService3         : OrderService3.orderItem: itemId=id1
void hello.proxy.app.v3.OrderRepository3.save(String)
2025-12-21 17:15:48.590  INFO 4276 --- [    Test worker] hello.proxy.app.v3.OrderRepository3      : OrderRepository3.save: itemId=id1
         */
    }
    @Test
    void re() {
        try {
            orderController3.req("re");
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
String hello.proxy.app.v3.OrderController3.req(String)
2025-12-21 17:16:06.619  INFO 22668 --- [    Test worker] hello.proxy.app.v3.OrderController3      : OrderController3.req: itemId=re
void hello.proxy.app.v3.OrderService3.orderItem(String)
2025-12-21 17:16:06.620  INFO 22668 --- [    Test worker] hello.proxy.app.v3.OrderService3         : OrderService3.orderItem: itemId=re
void hello.proxy.app.v3.OrderRepository3.save(String)
2025-12-21 17:16:06.628  INFO 22668 --- [    Test worker] hello.proxy.app.v3.OrderRepository3      : OrderRepository3.save: itemId=re
java.lang.RuntimeException: re
	at hello.proxy.app.v3.OrderRepository3.save(OrderRepository3.java:12)
	at hello.proxy.app.v3.OrderRepository3$$FastClassBySpringCGLIB$$d2a75a83.invoke(<generated>)
	at org.springframework.cglib.proxy.MethodProxy.invoke(MethodProxy.java:218)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.invokeJoinpoint(CglibAopProxy.java:779)
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:163)
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:750)
	at org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed(MethodInvocationProceedingJoinPoint.java:89)
	at hello.proxy.app.aop.Aspect1.run(Aspect1.java:14)
         */
    }
}
