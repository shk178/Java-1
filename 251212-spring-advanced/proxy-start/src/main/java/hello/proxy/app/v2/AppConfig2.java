package hello.proxy.app.v2;

import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig2 {
    @Bean
    public OrderController2 orderController2(LogTrace logTrace) {
        OrderController2 impl = new OrderController2(orderService2(logTrace));
        return new OrderController2Proxy(impl, logTrace);
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
