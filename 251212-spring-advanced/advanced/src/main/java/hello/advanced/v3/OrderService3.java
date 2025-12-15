package hello.advanced.v3;

import hello.advanced.trace.Trace2;
import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.FieldLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService3 {
    private final OrderRepository3 orderRepository3;
    private final FieldLogTrace fieldLogTrace;
    public void orderItem(TraceId traceId, String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = fieldLogTrace.start("service3-orderItem");
            orderRepository3.save(traceStatus.getTraceId(), itemId);
            fieldLogTrace.complete(traceStatus);
        } catch (Exception e) {
            fieldLogTrace.except(traceStatus, e);
            throw e;
        }
    }
}
