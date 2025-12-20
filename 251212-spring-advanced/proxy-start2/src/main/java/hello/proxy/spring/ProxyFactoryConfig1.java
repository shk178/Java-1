package hello.proxy.spring;

import hello.proxy.app.v1.*;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyFactoryConfig1 {
    @Bean
    public OrderController1 orderController1(LogTrace logTrace) {
        OrderController1 orderController1 = new OrderController1Impl(orderService1(logTrace));
        ProxyFactory proxyFactory = new ProxyFactory(orderController1);
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("req");
        LogTraceAdvice advice = new LogTraceAdvice(logTrace);
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(pointcut, advice));
        OrderController1 proxy = (OrderController1) proxyFactory.getProxy();
        return proxy;
    }
    @Bean
    public OrderService1 orderService1(LogTrace logTrace) {
        OrderService1 impl = new OrderService1Impl(orderRepository1(logTrace));
        return new OrderService1Proxy(impl, logTrace);
    }
    @Bean
    public OrderRepository1 orderRepository1(LogTrace logTrace) {
        OrderRepository1 impl = new OrderRepository1Impl();
        return new OrderRepository1Proxy(impl, logTrace);
    }
}
/*
2025-12-20 13:34:33.039  INFO 6492 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [2d61ba39] OrderController1.req() // LogTraceAdvice.invoke
2025-12-20 13:34:33.044  INFO 6492 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [2d61ba39] |-->os1p-orderItem
2025-12-20 13:34:33.044  INFO 6492 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [2d61ba39] |   |-->or1p-save
2025-12-20 13:34:34.054  INFO 6492 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [2d61ba39] |   |<--or1p-save time=1010ms
2025-12-20 13:34:34.054  INFO 6492 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [2d61ba39] |<--os1p-orderItem time=1012ms
2025-12-20 13:34:34.054  INFO 6492 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [2d61ba39] OrderController1.req() // LogTraceAdvice.invoke time=1015ms
 */