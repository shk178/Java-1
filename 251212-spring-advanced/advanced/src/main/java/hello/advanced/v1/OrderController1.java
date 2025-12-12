package hello.advanced.v1;

import hello.advanced.trace.Trace1;
import hello.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController1 {
    private final OrderService1 orderService1;
    private final Trace1 trace1;
    @GetMapping("/v1/req")
    public String req(@RequestParam String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = trace1.start("v1-req");
            orderService1.orderItem(itemId);
            trace1.end(traceStatus, null);
            return "ok";
        } catch (Exception e) {
            trace1.end(traceStatus, e);
            throw e;
        }
    }
}
/*
2025-12-12T19:09:50.676+09:00  INFO 15544 --- [advanced] [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2025-12-12T19:09:50.676+09:00  INFO 15544 --- [advanced] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2025-12-12T19:09:50.676+09:00  INFO 15544 --- [advanced] [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
2025-12-12T19:09:50.718+09:00  INFO 15544 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace1              : [0d79e21c] v1-req
2025-12-12T19:09:50.720+09:00  INFO 15544 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace1              : [2e48f2d9] repo1-save
2025-12-12T19:09:51.732+09:00  INFO 15544 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace1              : [2e48f2d9] repo1-save time=1012ms
2025-12-12T19:09:51.732+09:00  INFO 15544 --- [advanced] [nio-8080-exec-1] hello.advanced.trace.Trace1              : [0d79e21c] v1-req time=1014ms
 */