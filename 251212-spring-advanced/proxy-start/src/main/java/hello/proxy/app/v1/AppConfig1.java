package hello.proxy.app.v1;

import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;

@Configuration
public class AppConfig1 {
    @Bean
    public OrderController1 orderController1(LogTrace logTrace) {
        OrderController1Impl impl = new OrderController1Impl(orderService1(logTrace));
        OrderController1 proxy = (OrderController1) Proxy.newProxyInstance(
                OrderController1.class.getClassLoader(),
                new Class[]{OrderController1.class},
                new LogTraceInvocationHandler(impl, logTrace)
        );
        return proxy;
    }
    @Bean
    public OrderService1 orderService1(LogTrace logTrace) {
        OrderService1Impl impl = new OrderService1Impl(orderRepository1(logTrace));
        OrderService1 proxy = (OrderService1) Proxy.newProxyInstance(
                OrderService1.class.getClassLoader(),
                new Class[]{OrderService1.class},
                new LogFilterInvocationHandler(impl, logTrace, new String[]{"order*"})
        );
        return proxy;
    }
    @Bean
    public OrderRepository1 orderRepository1(LogTrace logTrace) {
        OrderRepository1Impl impl = new OrderRepository1Impl();
        return new OrderRepository1Proxy(impl, logTrace);
    }
}
/*
2025-12-19 16:42:58.702  INFO 15560 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [d3d9aa62] OrderController1.req()
2025-12-19 16:42:58.707  INFO 15560 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [d3d9aa62] |-->OrderService1.orderItem()
2025-12-19 16:42:58.708  INFO 15560 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [d3d9aa62] |   |-->or1p-save
2025-12-19 16:42:59.720  INFO 15560 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [d3d9aa62] |   |<--or1p-save time=1012ms
2025-12-19 16:42:59.720  INFO 15560 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [d3d9aa62] |<--OrderService1.orderItem() time=1014ms
2025-12-19 16:42:59.720  INFO 15560 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [d3d9aa62] OrderController1.req() time=1018ms
 */