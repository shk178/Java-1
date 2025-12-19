package hello.advanced.template;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.LogTrace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class TraceTemplate<T> {
    private final LogTrace logTrace;
    public T execute(String message) {
        TraceStatus traceStatus = null;
        try {
            traceStatus = logTrace.start(message);
            T result = call();
            logTrace.complete(traceStatus);
            return result;
        } catch (Exception e) {
            logTrace.except(traceStatus, e);
            throw e;
        }
    }
    protected abstract T call();
}
