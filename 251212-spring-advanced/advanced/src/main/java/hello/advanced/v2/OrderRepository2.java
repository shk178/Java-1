package hello.advanced.v2;

import hello.advanced.trace.Trace2;
import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository2 {
    private final Trace2 trace2;
    public void save(TraceId traceId, String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = trace2.startSync(traceId, "repo2-save");
            if (itemId.equals("ce")) {
                throw new Exception("ce");
            }
            if (itemId.equals("re")) {
                throw new RuntimeException("re");
            }
            sleep(1000);
            trace2.end(traceStatus, null);
        } catch (Exception e) {
            trace2.end(traceStatus, e);
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
