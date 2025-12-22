package hello.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class OrderService {
    private final MeterRegistry registry;
    private AtomicInteger stock = new AtomicInteger(100);
    public void order() {
        stock.decrementAndGet();
        System.out.println("주문, 재고=" + stock);
        Counter.builder("myorder")
                .tag("class", this.getClass().getName())
                .tag("method", "order")
                .register(registry).increment();
    }
    public void cancleOrder() {
        stock.incrementAndGet();
        System.out.println("주문 취소, 재고=" + stock);
        Counter.builder("myorder")
                .tag("class", this.getClass().getName())
                .tag("method", "cancelOrder")
                .register(registry).increment();
    }
    public AtomicInteger getStock() {
        return stock;
    }
}
