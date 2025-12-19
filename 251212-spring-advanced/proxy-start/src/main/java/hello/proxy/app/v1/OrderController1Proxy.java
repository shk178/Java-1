package hello.proxy.app.v1;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderController1Proxy implements OrderController1 {
    private final OrderController1 target;
    private final LogTrace logTrace;

    @Override
    public String req(String itemId) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.begin("oc1p-req");
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
2025-12-19 15:03:41.727  INFO 12488 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [fe5b3518] oc1p-req
2025-12-19 15:03:41.732  INFO 12488 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [fe5b3518] |-->os1p-orderItem
2025-12-19 15:03:41.732  INFO 12488 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [fe5b3518] |   |-->or1p-save
2025-12-19 15:03:42.741  INFO 12488 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [fe5b3518] |   |<--or1p-save time=1009ms
2025-12-19 15:03:42.741  INFO 12488 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [fe5b3518] |<--os1p-orderItem time=1012ms
2025-12-19 15:03:42.741  INFO 12488 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [fe5b3518] oc1p-req time=1014ms
 */