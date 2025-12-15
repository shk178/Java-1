package hello.advanced.v4;

import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.ThreadLocalLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository4 {
    private final ThreadLocalLogTrace threadLocalLogTrace;
    public void save(TraceId traceId, String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = threadLocalLogTrace.start("repo4-save");
            if (itemId.equals("ce")) {
                throw new Exception("ce");
            }
            if (itemId.equals("re")) {
                throw new RuntimeException("re");
            }
            sleep(1000);
            threadLocalLogTrace.complete(traceStatus);
        } catch (Exception e) {
            threadLocalLogTrace.except(traceStatus, e);
            throw e;
        }
    }
    private void sleep(int millies) {
        try {
            Thread.sleep(millies);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
