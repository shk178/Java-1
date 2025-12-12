package hello.advanced.trace;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Trace2 {
    private static final String START_PREFIX = "-->";
    private static final String COMPLETE_PREFIX = "<--";
    private static final String EXCEPT_PREFIX = "<X-";
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
    public TraceStatus start(String message) {
        TraceId traceId = new TraceId();
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}",
                traceId.getId(),
                addSpace(START_PREFIX, traceId.getLevel()),
                message);
        return new TraceStatus(traceId, startTimeMs, message);
    }
    public TraceStatus startSync(TraceId beforeTraceId, String message) {
        TraceId traceId = beforeTraceId.createNextId();
        Long startTimeMs = System.currentTimeMillis();
        log.info("[{}] {}{}",
                traceId.getId(),
                addSpace(START_PREFIX, traceId.getLevel()),
                message);
        return new TraceStatus(traceId, startTimeMs, message);
    }
    public void end(TraceStatus traceStatus, Exception e) {
        TraceId traceId = traceStatus.getTraceId();
        Long stopTimeMs = System.currentTimeMillis();
        long resultTimeMs = stopTimeMs - traceStatus.getStartTimeMs();
        if (e == null) {
            log.info("[{}] {}{} time={}ms",
                    traceId.getId(),
                    addSpace(COMPLETE_PREFIX, traceId.getLevel()),
                    traceStatus.getMessage(),
                    resultTimeMs);
        } else {
            log.info("[{}] {}{} time={}ms e={}",
                    traceId.getId(),
                    addSpace(EXCEPT_PREFIX, traceId.getLevel()),
                    traceStatus.getMessage(),
                    resultTimeMs,
                    e.getMessage());
        }
    }
}
