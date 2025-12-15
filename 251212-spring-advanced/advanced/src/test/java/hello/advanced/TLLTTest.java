package hello.advanced;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.ThreadLocalLogTrace;
import org.junit.jupiter.api.Test;

public class TLLTTest {
    ThreadLocalLogTrace tllt = new ThreadLocalLogTrace();
    @Test
    void ne4() {
        System.out.println("--ne4()--");
        TraceStatus traceStatus1 = tllt.start("ne4시작-traceStatus1");
        TraceStatus traceStatus2 = tllt.start("ne4시작-traceStatus2");
        tllt.complete(traceStatus2);
        tllt.complete(traceStatus1);
        System.out.println("--ne4()--");
    }
    @Test
    void ce4() {
        System.out.println("--ce4()--");
        TraceStatus traceStatus1 = tllt.start("ce4시작-traceStatus1");
        TraceStatus traceStatus2 = tllt.start("ce4시작-traceStatus2");
        tllt.except(traceStatus2, new Exception("ce4발생-traceStatus2"));
        tllt.except(traceStatus1, new Exception("ce4발생-traceStatus1"));
        System.out.println("--ce4()--");
    }
    @Test
    void re4() {
        System.out.println("--re4()--");
        TraceStatus traceStatus1 = tllt.start("re4시작-traceStatus1");
        TraceStatus traceStatus2 = tllt.start("re4시작-traceStatus2");
        tllt.except(traceStatus2, new RuntimeException("re4발생-traceStatus2"));
        tllt.except(traceStatus1, new RuntimeException("re4발생-traceStatus1"));
        System.out.println("--re4()--");
    }
}
/*
--ce4()--
20:38:59.400 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [109e44d0] ce4시작-traceStatus1
20:38:59.404 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [109e44d0] |-->ce4시작-traceStatus2
20:38:59.404 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [109e44d0] |<X-ce4시작-traceStatus2 time=0ms e=ce4발생-traceStatus2
20:38:59.404 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [109e44d0] ce4시작-traceStatus1 time=5ms e=ce4발생-traceStatus1
--ce4()--
--ne4()--
20:38:59.412 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [da1739ac] ne4시작-traceStatus1
20:38:59.412 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [da1739ac] |-->ne4시작-traceStatus2
20:38:59.418 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [da1739ac] |<--ne4시작-traceStatus2 time=6ms
20:38:59.418 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [da1739ac] ne4시작-traceStatus1 time=6ms
--ne4()--
--re4()--
20:38:59.419 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [56d87ebd] re4시작-traceStatus1
20:38:59.420 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [56d87ebd] |-->re4시작-traceStatus2
20:38:59.420 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [56d87ebd] |<X-re4시작-traceStatus2 time=0ms e=re4발생-traceStatus2
20:38:59.421 [Test worker] INFO hello.advanced.trace2.ThreadLocalLogTrace -- [56d87ebd] re4시작-traceStatus1 time=2ms e=re4발생-traceStatus1
--re4()--
 */