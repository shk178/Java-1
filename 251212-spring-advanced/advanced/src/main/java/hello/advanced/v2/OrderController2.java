package hello.advanced.v2;

import hello.advanced.trace.Trace2;
import hello.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController2 {
    private final OrderService2 orderService2;
    private final Trace2 trace2;
    @GetMapping("/v2/req")
    public String req(@RequestParam String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = trace2.start("v2-req");
            orderService2.orderItem(traceStatus.getTraceId(), itemId);
            trace2.end(traceStatus, null);
            return "ok";
        } catch (Exception e) {
            trace2.end(traceStatus, e);
            throw e;
        }
    }
}
/*
2025-12-12T20:15:29.031+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-12-12T20:15:29.031+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-12-12T20:15:29.033+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
2025-12-12T20:15:29.078+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace2              : [77d43b35] v2-req
2025-12-12T20:15:29.079+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace2              : [77d43b35] |-->service2-orderItem
2025-12-12T20:15:29.079+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace2              : [77d43b35] |   |-->repo2-save
2025-12-12T20:15:30.084+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace2              : [77d43b35] |   |<--repo2-save time=1005ms
2025-12-12T20:15:30.084+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace2              : [77d43b35] |<--service2-orderItem time=1005ms
2025-12-12T20:15:30.084+09:00  INFO 5208 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace2              : [77d43b35] v2-req time=1006ms
 */