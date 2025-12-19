package hello.advanced.v6;

import hello.advanced.template.ContextTemplate;
import hello.advanced.template.StrategyAlgorithm;
import hello.advanced.template.TraceTemplate;
import hello.advanced.trace2.LogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepository6 {
    private final LogTrace logTrace;
    public void save(String itemId) {
        StrategyAlgorithm<Void> strategy = new StrategyAlgorithm<>() {
            @Override
            public Void call() {
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
        ContextTemplate<Void> context = new ContextTemplate<>(strategy, logTrace);
        context.execute("repo6-save");
    }
    private void sleep(int millies) {
        try {
            Thread.sleep(millies);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
