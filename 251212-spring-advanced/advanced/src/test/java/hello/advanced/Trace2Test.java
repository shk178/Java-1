package hello.advanced;

import hello.advanced.trace.Trace2;
import hello.advanced.trace.TraceStatus;
import org.junit.jupiter.api.Test;

public class Trace2Test {
    @Test
    void ne2() {
        System.out.println("--ne2()--");
        Trace2 trace2 = new Trace2();
        TraceStatus traceStatus1 = trace2.start("ne2시작-traceStatus1");
        TraceStatus traceStatus2 = trace2.startSync(traceStatus1.getTraceId(), "ne2시작-traceStatus2");
        trace2.end(traceStatus2, null);
        trace2.end(traceStatus1, null);
        System.out.println("--ne2()--");
    }
    @Test
    void ce2() {
        System.out.println("--ce2()--");
        Trace2 trace2 = new Trace2();
        TraceStatus traceStatus1 = trace2.start("ce2시작-traceStatus1");
        TraceStatus traceStatus2 = trace2.startSync(traceStatus1.getTraceId(), "ce2시작-traceStatus2");
        trace2.end(traceStatus2, new Exception("ce2발생-traceStatus2"));
        trace2.end(traceStatus1, new Exception("ce2발생-traceStatus1"));
        System.out.println("--ce2()--");
    }
    @Test
    void re2() {
        System.out.println("--re2()--");
        Trace2 trace2 = new Trace2();
        TraceStatus traceStatus1 = trace2.start("re2시작-traceStatus1");
        TraceStatus traceStatus2 = trace2.startSync(traceStatus1.getTraceId(), "re2시작-traceStatus2");
        trace2.end(traceStatus2, new RuntimeException("re2발생-traceStatus2"));
        trace2.end(traceStatus1, new RuntimeException("re2발생-traceStatus1"));
        System.out.println("--re2()--");
    }
}
/*
--ce2()--
19:21:11.237 [Test worker] INFO hello.advanced.trace.Trace2 -- [1acec69f] ce2시작-traceStatus1
19:21:11.241 [Test worker] INFO hello.advanced.trace.Trace2 -- [1acec69f] |-->ce2시작-traceStatus2
19:21:11.242 [Test worker] INFO hello.advanced.trace.Trace2 -- [1acec69f] |<X-ce2시작-traceStatus2 time=1ms e=ce2발생-traceStatus2
19:21:11.242 [Test worker] INFO hello.advanced.trace.Trace2 -- [1acec69f] ce2시작-traceStatus1 time=6ms e=ce2발생-traceStatus1
--ce2()--
--ne2()--
19:21:11.256 [Test worker] INFO hello.advanced.trace.Trace2 -- [65ed6592] ne2시작-traceStatus1
19:21:11.257 [Test worker] INFO hello.advanced.trace.Trace2 -- [65ed6592] |-->ne2시작-traceStatus2
19:21:11.257 [Test worker] INFO hello.advanced.trace.Trace2 -- [65ed6592] |<--ne2시작-traceStatus2 time=0ms
19:21:11.257 [Test worker] INFO hello.advanced.trace.Trace2 -- [65ed6592] ne2시작-traceStatus1 time=1ms
--ne2()--
--re2()--
19:21:11.258 [Test worker] INFO hello.advanced.trace.Trace2 -- [d2a15258] re2시작-traceStatus1
19:21:11.259 [Test worker] INFO hello.advanced.trace.Trace2 -- [d2a15258] |-->re2시작-traceStatus2
19:21:11.259 [Test worker] INFO hello.advanced.trace.Trace2 -- [d2a15258] |<X-re2시작-traceStatus2 time=0ms e=re2발생-traceStatus2
19:21:11.259 [Test worker] INFO hello.advanced.trace.Trace2 -- [d2a15258] re2시작-traceStatus1 time=1ms e=re2발생-traceStatus1
--re2()--
 */