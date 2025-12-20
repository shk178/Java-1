package hello.proxy.spring;

import hello.proxy.app.v2.*;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyFactoryConfig2 {
    @Bean
    public OrderController2 orderController2(LogTrace logTrace) {
        OrderController2 impl = new OrderController2(orderService2(logTrace));
        ProxyFactory proxyFactory = new ProxyFactory(impl);
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("req");
        LogTraceAdvice advice = new LogTraceAdvice(logTrace);
        proxyFactory.addAdvisor(new DefaultPointcutAdvisor(pointcut, advice));
        OrderController2 proxy = (OrderController2) proxyFactory.getProxy();
        return proxy;
    }
    @Bean
    public OrderService2 orderService2(LogTrace logTrace) {
        OrderService2 impl = new OrderService2(orderRepository2(logTrace));
        return new OrderService2Proxy(impl, logTrace);
    }
    @Bean
    public OrderRepository2 orderRepository2(LogTrace logTrace) {
        OrderRepository2 impl = new OrderRepository2();
        return new OrderRepository2Proxy(impl, logTrace);
    }
}
/*
2025-12-20 13:44:52.952  INFO 15976 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [90165a3e] OrderController2.req() // LogTraceAdvice.invoke
2025-12-20 13:44:52.960  INFO 15976 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [90165a3e] |-->os2p-orderItem
2025-12-20 13:44:52.960  INFO 15976 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [90165a3e] |   |-->or2p-save
2025-12-20 13:44:53.973  INFO 15976 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [90165a3e] |   |<--or2p-save time=1013ms
2025-12-20 13:44:53.973  INFO 15976 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [90165a3e] |<--os2p-orderItem time=1014ms
2025-12-20 13:44:53.973  INFO 15976 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [90165a3e] OrderController2.req() // LogTraceAdvice.invoke time=1021ms
 */