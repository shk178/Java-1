package hello.advanced.v2;

import hello.advanced.trace.Trace2;
import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService2 {
    private final OrderRepository2 orderRepository2;
    private final Trace2 trace2;
    public void orderItem(TraceId traceId, String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = trace2.startSync(traceId, "service2-orderItem");
            orderRepository2.save(traceStatus.getTraceId(), itemId);
            trace2.end(traceStatus, null);
        } catch (Exception e) {
            trace2.end(traceStatus, e);
            throw e;
        }
    }
}
