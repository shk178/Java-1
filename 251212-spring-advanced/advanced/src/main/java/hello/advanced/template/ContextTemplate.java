package hello.advanced.template;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContextTemplate<T> {
    private final StrategyAlgorithm<T> strategyAlgorithm;
    private final LogTrace logTrace;
    public T execute(String message) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.start(message);
            T result = strategyAlgorithm.call();
            logTrace.complete(traceStatus);
            return result;
        } catch (Exception e) {
            logTrace.except(traceStatus, e);
            throw e;
        }
    }
}
    // 변하지 않는 부분을 Context에 둔다.
    // Strategy는 변하는 알고리즘 역할 한다.
