package hello.advanced.v6;

import hello.advanced.template.ContextTemplate;
import hello.advanced.template.StrategyAlgorithm;
import hello.advanced.template.TraceTemplate;
import hello.advanced.trace2.LogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Context;

@RestController
@RequiredArgsConstructor
public class OrderController6 {
    private final OrderService6 orderService6;
    private final LogTrace logTrace;
    @GetMapping("/v6/req")
    public String req(@RequestParam String itemId) {
        StrategyAlgorithm<String> strategy = new StrategyAlgorithm<>() {
            @Override
            public String call() {
                orderService6.orderItem(itemId);
                return "ok";
            }
        };
        ContextTemplate<String> context = new ContextTemplate<>(strategy, logTrace);
        return context.execute("v6-req");
    }
}
/*
2025-12-19T11:13:02.555+09:00  INFO 12572 --- [advanced] [nio-8080-exec-3] h.advanced.trace2.ThreadLocalLogTrace    : [532f4fcc] v6-req
2025-12-19T11:13:02.557+09:00  INFO 12572 --- [advanced] [nio-8080-exec-3] h.advanced.trace2.ThreadLocalLogTrace    : [532f4fcc] |-->service6-orderItem
2025-12-19T11:13:02.557+09:00  INFO 12572 --- [advanced] [nio-8080-exec-3] h.advanced.trace2.ThreadLocalLogTrace    : [532f4fcc] |   |-->repo6-save
2025-12-19T11:13:03.571+09:00  INFO 12572 --- [advanced] [nio-8080-exec-3] h.advanced.trace2.ThreadLocalLogTrace    : [532f4fcc] |   |<--repo6-save time=1014ms
2025-12-19T11:13:03.571+09:00  INFO 12572 --- [advanced] [nio-8080-exec-3] h.advanced.trace2.ThreadLocalLogTrace    : [532f4fcc] |<--service6-orderItem time=1014ms
2025-12-19T11:13:03.571+09:00  INFO 12572 --- [advanced] [nio-8080-exec-3] h.advanced.trace2.ThreadLocalLogTrace    : [532f4fcc] v6-req time=1016ms
 */