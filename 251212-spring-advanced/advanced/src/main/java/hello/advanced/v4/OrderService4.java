package hello.advanced.v4;

import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.ThreadLocalLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService4 {
    private final OrderRepository4 orderRepository4;
    private final ThreadLocalLogTrace threadLocalLogTrace;
    public void orderItem(TraceId traceId, String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = threadLocalLogTrace.start("service4-orderItem");
            orderRepository4.save(traceStatus.getTraceId(), itemId);
            threadLocalLogTrace.complete(traceStatus);
        } catch (Exception e) {
            threadLocalLogTrace.except(traceStatus, e);
            throw e;
        }
    }
}
