package hello.advanced.v6;

import hello.advanced.template.ContextTemplate;
import hello.advanced.template.StrategyAlgorithm;
import hello.advanced.template.TraceTemplate;
import hello.advanced.trace2.LogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService6 {
    private final OrderRepository6 orderRepository6;
    private final LogTrace logTrace;
    public void orderItem(String itemId) {
        StrategyAlgorithm<Void> strategy = new StrategyAlgorithm<>() {
            @Override
            public Void call() {
                orderRepository6.save(itemId);
                return null;
            }
        };
        ContextTemplate<Void> context = new ContextTemplate<>(strategy, logTrace);
        context.execute("service6-orderItem");
    }
}
