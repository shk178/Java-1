package hello.proxy.app.v2;

import hello.proxy.app.v1.OrderRepository1;
import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

public class OrderRepository2Proxy extends OrderRepository2 {
    private final OrderRepository2 target;
    private final LogTrace logTrace;

    public OrderRepository2Proxy(OrderRepository2 target, LogTrace logTrace) {
        this.target = target;
        this.logTrace = logTrace;
    }

    @Override
    public void save(String itemId) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.begin("or2p-save");
            target.save(itemId);
            logTrace.end(traceStatus);
        } catch (Exception e) {
            logTrace.exception(traceStatus, e);
            throw e;
        }
    }
}
