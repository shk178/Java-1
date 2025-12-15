package hello.advanced.trace2;

import hello.advanced.trace.TraceStatus;

public interface LogTrace {
    TraceStatus start(String message);
    void complete(TraceStatus status);
    void except(TraceStatus status, Exception e);
}
