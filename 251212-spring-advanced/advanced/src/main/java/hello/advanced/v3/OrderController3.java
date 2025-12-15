package hello.advanced.v3;

import hello.advanced.trace.Trace2;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.FieldLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController3 {
    private final OrderService3 orderService3;
    private final FieldLogTrace fieldLogTrace;
    @GetMapping("/v3/req")
    public String req(@RequestParam String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = fieldLogTrace.start("v3-req");
            orderService3.orderItem(traceStatus.getTraceId(), itemId);
            fieldLogTrace.complete(traceStatus);
            return "ok";
        } catch (Exception e) {
            fieldLogTrace.except(traceStatus, e);
            throw e;
        }
    }
}
/*
2025-12-15T19:47:19.332+09:00  INFO 8864 --- [advanced] [nio-8080-exec-1] hello.advanced.trace2.FieldLogTrace      : [1a60c34e] v3-req
2025-12-15T19:47:19.332+09:00  INFO 8864 --- [advanced] [nio-8080-exec-1] hello.advanced.trace2.FieldLogTrace      : [1a60c34e] |-->service3-orderItem
2025-12-15T19:47:19.332+09:00  INFO 8864 --- [advanced] [nio-8080-exec-1] hello.advanced.trace2.FieldLogTrace      : [1a60c34e] |   |-->repo3-save
2025-12-15T19:47:20.338+09:00  INFO 8864 --- [advanced] [nio-8080-exec-1] hello.advanced.trace2.FieldLogTrace      : [1a60c34e] |   |<--repo3-save time=1006ms
2025-12-15T19:47:20.338+09:00  INFO 8864 --- [advanced] [nio-8080-exec-1] hello.advanced.trace2.FieldLogTrace      : [1a60c34e] |<--service3-orderItem time=1006ms
2025-12-15T19:47:20.338+09:00  INFO 8864 --- [advanced] [nio-8080-exec-1] hello.advanced.trace2.FieldLogTrace      : [1a60c34e] v3-req time=1006ms
 */