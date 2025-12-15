package hello.advanced.v3;

import hello.advanced.trace.Trace2;
import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.FieldLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository3 {
    private final FieldLogTrace fieldLogTrace;
    public void save(TraceId traceId, String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = fieldLogTrace.start("repo3-save");
            if (itemId.equals("ce")) {
                throw new Exception("ce");
            }
            if (itemId.equals("re")) {
                throw new RuntimeException("re");
            }
            sleep(1000);
            fieldLogTrace.complete(traceStatus);
        } catch (Exception e) {
            fieldLogTrace.except(traceStatus, e);
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
