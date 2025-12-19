package hello.proxy.spring;

import hello.proxy.app.v1.*;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyFactoryConfig {
    @Bean
    public OrderController1 orderController1(LogTrace logTrace) {
        OrderController1 orderController1 = new OrderController1Impl(orderService1(logTrace));
        ProxyFactory proxyFactory = new ProxyFactory(orderController1);
        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedNames("*");
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
