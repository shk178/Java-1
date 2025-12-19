package hello.proxy.app.v1;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderService1Proxy implements OrderService1 {
    private final OrderService1 target;
    private final LogTrace logTrace;
    @Override
    public void orderItem(String itemId) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.begin("os1p-orderItem");
            target.orderItem(itemId);
            logTrace.end(traceStatus);
        } catch (Exception e) {
            logTrace.exception(traceStatus, e);
            throw e;
        }
    }
}
