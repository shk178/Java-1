package hello.proxy.app.v2;

import hello.proxy.app.v1.OrderService1;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

public class OrderService2Proxy extends OrderService2 {
    private final OrderService2 target;
    private final LogTrace logTrace;

    public OrderService2Proxy(OrderService2 target, LogTrace logTrace) {
        super(null);
        this.target = target;
        this.logTrace = logTrace;
    }

    @Override
    public void orderItem(String itemId) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.begin("os2p-orderItem");
            target.orderItem(itemId);
            logTrace.end(traceStatus);
        } catch (Exception e) {
            logTrace.exception(traceStatus, e);
            throw e;
        }
    }
}
