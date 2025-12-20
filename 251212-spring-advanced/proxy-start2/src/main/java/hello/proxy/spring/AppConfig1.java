package hello.proxy.spring;

import hello.proxy.app.v1.*;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig1 {
    @Bean
    public OrderController1 orderController1(LogTrace logTrace) {
        OrderController1 impl = new OrderController1Impl(orderService1(logTrace));
        return new OrderController1Proxy(impl, logTrace);
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
