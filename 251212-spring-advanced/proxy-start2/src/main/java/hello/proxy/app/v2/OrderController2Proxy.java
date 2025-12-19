package hello.proxy.app.v2;

import hello.proxy.app.v1.OrderController1;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

public class OrderController2Proxy extends OrderController2 {
    private final OrderController2 target;
    private final LogTrace logTrace;

    public OrderController2Proxy(OrderController2 target, LogTrace logTrace) {
        super(null);
        this.target = target;
        this.logTrace = logTrace;
    }

    @Override
    public String req(String itemId) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.begin("oc2p-req");
            String result = target.req(itemId);
            logTrace.end(traceStatus);
            return result;
        } catch (Exception e) {
            logTrace.exception(traceStatus, e);
            throw e;
        }
    }

    @Override
    public String req2() {
        return target.req2();
    }
}
/*
2025-12-19 15:24:53.837  INFO 14148 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [11bc4e09] oc2p-req
2025-12-19 15:24:53.840  INFO 14148 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [11bc4e09] |-->os2p-orderItem
2025-12-19 15:24:53.840  INFO 14148 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [11bc4e09] |   |-->or2p-save
2025-12-19 15:24:54.856  INFO 14148 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [11bc4e09] |   |<--or2p-save time=1016ms
2025-12-19 15:24:54.856  INFO 14148 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [11bc4e09] |<--os2p-orderItem time=1017ms
2025-12-19 15:24:54.856  INFO 14148 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [11bc4e09] oc2p-req time=1019ms
 */