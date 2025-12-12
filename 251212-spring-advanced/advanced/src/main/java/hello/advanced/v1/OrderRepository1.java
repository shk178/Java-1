package hello.advanced.v1;

import hello.advanced.trace.Trace1;
import hello.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository1 {
    private final Trace1 trace1;
    public void save(String itemId) throws Exception {
        TraceStatus traceStatus = null;
        try {
            traceStatus = trace1.start("repo1-save");
            if (itemId.equals("ce")) {
                throw new Exception("ce");
            }
            if (itemId.equals("re")) {
                throw new RuntimeException("re");
            }
            sleep(1000);
            trace1.end(traceStatus, null);
        } catch (Exception e) {
            trace1.end(traceStatus, e);
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
