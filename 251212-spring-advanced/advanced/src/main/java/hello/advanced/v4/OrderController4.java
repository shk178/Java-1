package hello.advanced.v4;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.ThreadLocalLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController4 {
    private final OrderService4 orderService4;
    private final ThreadLocalLogTrace threadLocalLogTrace;
    @GetMapping("/v4/req")
    public String req(@RequestParam String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = threadLocalLogTrace.start("v4-req");
            orderService4.orderItem(traceStatus.getTraceId(), itemId);
            threadLocalLogTrace.complete(traceStatus);
            return "ok";
        } catch (Exception e) {
            threadLocalLogTrace.except(traceStatus, e);
            throw e;
        }
    }
}
/*
2025-12-15T20:46:34.705+09:00  INFO 19332 --- [advanced] [nio-8080-exec-2] h.advanced.trace2.ThreadLocalLogTrace    : [65e86fdf] v4-req
2025-12-15T20:46:34.706+09:00  INFO 19332 --- [advanced] [nio-8080-exec-2] h.advanced.trace2.ThreadLocalLogTrace    : [65e86fdf] |-->service4-orderItem
2025-12-15T20:46:34.706+09:00  INFO 19332 --- [advanced] [nio-8080-exec-2] h.advanced.trace2.ThreadLocalLogTrace    : [65e86fdf] |   |-->repo4-save
2025-12-15T20:46:35.715+09:00  INFO 19332 --- [advanced] [nio-8080-exec-2] h.advanced.trace2.ThreadLocalLogTrace    : [65e86fdf] |   |<--repo4-save time=1009ms
2025-12-15T20:46:35.715+09:00  INFO 19332 --- [advanced] [nio-8080-exec-2] h.advanced.trace2.ThreadLocalLogTrace    : [65e86fdf] |<--service4-orderItem time=1009ms
2025-12-15T20:46:35.715+09:00  INFO 19332 --- [advanced] [nio-8080-exec-2] h.advanced.trace2.ThreadLocalLogTrace    : [65e86fdf] v4-req time=1010ms
 */