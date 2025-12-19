package hello.proxy.app.v1;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrderRepository1Proxy implements OrderRepository1 {
    private final OrderRepository1 target;
    private final LogTrace logTrace;
    @Override
    public void save(String itemId) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.begin("or1p-save");
            target.save(itemId);
            logTrace.end(traceStatus);
        } catch (Exception e) {
            logTrace.exception(traceStatus, e);
            throw e;
        }
    }
}
