package hello.advanced.v5;

import hello.advanced.template.TraceTemplate;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.LogTrace;
import hello.advanced.trace2.ThreadLocalLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController5 {
    private final OrderService5 orderService5;
    private final LogTrace logTrace;
    @GetMapping("/v5/req")
    public String req(@RequestParam String itemId) throws Exception {
        TraceTemplate<String> traceTemplate = new TraceTemplate<>(logTrace) {
            @Override
            protected String call() {
                orderService5.orderItem(itemId);
                return "ok";
            }
        };
        return traceTemplate.execute("v5-req");
    }
}
/*
2025-12-16T15:21:52.625+09:00  INFO 19032 --- [advanced] [nio-8080-exec-1] h.advanced.trace2.ThreadLocalLogTrace    : [ae434c1e] v5-req
2025-12-16T15:21:52.627+09:00  INFO 19032 --- [advanced] [nio-8080-exec-1] h.advanced.trace2.ThreadLocalLogTrace    : [ae434c1e] |-->service5-orderItem
2025-12-16T15:21:52.627+09:00  INFO 19032 --- [advanced] [nio-8080-exec-1] h.advanced.trace2.ThreadLocalLogTrace    : [ae434c1e] |   |-->repo5-save
2025-12-16T15:21:53.635+09:00  INFO 19032 --- [advanced] [nio-8080-exec-1] h.advanced.trace2.ThreadLocalLogTrace    : [ae434c1e] |   |<--repo5-save time=1008ms
2025-12-16T15:21:53.635+09:00  INFO 19032 --- [advanced] [nio-8080-exec-1] h.advanced.trace2.ThreadLocalLogTrace    : [ae434c1e] |<--service5-orderItem time=1008ms
2025-12-16T15:21:53.635+09:00  INFO 19032 --- [advanced] [nio-8080-exec-1] h.advanced.trace2.ThreadLocalLogTrace    : [ae434c1e] v5-req time=1010ms
 */