package hello.advanced.v5;

import hello.advanced.template.TraceTemplate;
import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.LogTrace;
import hello.advanced.trace2.ThreadLocalLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService5 {
    private final OrderRepository5 orderRepository5;
    private final LogTrace logTrace;
    public void orderItem(String itemId) {
        TraceTemplate<Void> traceTemplate = new TraceTemplate<>(logTrace) {
            @Override
            protected Void call() {
                orderRepository5.save(itemId);
                return null;
            }
        };
        traceTemplate.execute("service5-orderItem");
    }
}
