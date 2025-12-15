package hello.advanced.trace2;

import hello.advanced.trace.TraceId;
import hello.advanced.trace.TraceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class FieldLogTrace implements LogTrace {
    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EXCEPT_PREFIX = "<X-";
    private TraceId traceIdHolder;

    private void syncTraceId() {
        if (traceIdHolder == null) {
            traceIdHolder = new TraceId();
        } else {
            traceIdHolder = traceIdHolder.createNextId();
        }
    }

    private void releaseTraceId() {
        if (traceIdHolder.isFirstLevel()) {
            traceIdHolder = null;
        } else {
            traceIdHolder = traceIdHolder.createPreviousId();
        }
    }

    private static String addSpace(String prefix, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append(
                    (i == level - 1) ?
                            "|" + prefix :
                            "|   "
            );
        }
        return sb.toString();
    }

    @Override
    public TraceStatus start(String message) {
        syncTraceId();
        TraceId traceId = traceIdHolder;
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}",
                traceId.getId(),
                addSpace(START_PREFIX, traceId.getLevel()),
                message);
        return new TraceStatus(traceId, startTimeMs, message);
    }

    @Override
    public void complete(TraceStatus status) {
        TraceId traceId = status.getTraceId();
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();
        log.info("[{}] {}{} time={}ms",
                traceId.getId(),
                addSpace(COMPLETE_PREFIX, traceId.getLevel()),
                status.getMessage(),
                resultTimeMs);
        releaseTraceId();
    }

    @Override
    public void except(TraceStatus status, Exception e) {
        TraceId traceId = status.getTraceId();
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - status.getStartTimeMs();
        log.info("[{}] {}{} time={}ms e={}",
                traceId.getId(),
                addSpace(EXCEPT_PREFIX, traceId.getLevel()),
                status.getMessage(),
                resultTimeMs,
                e.getMessage());
        releaseTraceId();
    }
}
