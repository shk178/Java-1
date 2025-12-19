package hello.advanced.v5;

import hello.advanced.template.TraceTemplate;
import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.LogTrace;
import hello.advanced.trace2.ThreadLocalLogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository5 {
    private final LogTrace logTrace;
    public void save(String itemId) {
        TraceTemplate<Void> traceTemplate = new TraceTemplate<>(logTrace) {
            @Override
            protected Void call() {
                if (itemId.equals("ce")) {
                    //throw new Exception("ce");
                    System.out.println("throw new Exception(\"ce\")");
                }
                if (itemId.equals("re")) {
                    throw new RuntimeException("re");
                }
                sleep(1000);
                return null;
            }
        };
        traceTemplate.execute("repo5-save");
    }
    private void sleep(int millies) {
        try {
            Thread.sleep(millies);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
