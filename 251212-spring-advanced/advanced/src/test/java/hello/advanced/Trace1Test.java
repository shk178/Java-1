package hello.advanced;

import hello.advanced.trace.Trace1;
import hello.advanced.trace.TraceStatus;
import org.junit.jupiter.api.Test;

public class Trace1Test {
    @Test
    void ne() {
        System.out.println("--ne()--");
        Trace1 trace1 = new Trace1();
        TraceStatus traceStatus = trace1.start("ne시작");
        trace1.end(traceStatus, null);
        System.out.println("--ne()--");
    }
    @Test
    void ce() {
        System.out.println("--ce()--");
        Trace1 trace1 = new Trace1();
        TraceStatus traceStatus = trace1.start("ce시작");
        trace1.end(traceStatus, new Exception("ce발생"));
        System.out.println("--ce()--");
    }
    @Test
    void re() {
        System.out.println("--re()--");
        Trace1 trace1 = new Trace1();
        TraceStatus traceStatus = trace1.start("re시작");
        trace1.end(traceStatus, new RuntimeException("re발생"));
        System.out.println("--re()--");
    }
}
/*
--ce()--
18:48:53.318 [Test worker] INFO hello.advanced.trace.Trace1 -- [0f0f7a2c] ce시작
18:48:53.322 [Test worker] INFO hello.advanced.trace.Trace1 -- [0f0f7a2c] ce시작 time=5ms e=ce발생
--ce()--
--ne()--
18:48:53.337 [Test worker] INFO hello.advanced.trace.Trace1 -- [0b68b76f] ne시작
18:48:53.338 [Test worker] INFO hello.advanced.trace.Trace1 -- [0b68b76f] ne시작 time=1ms
--ne()--
--re()--
18:48:53.340 [Test worker] INFO hello.advanced.trace.Trace1 -- [068af332] re시작
18:48:53.340 [Test worker] INFO hello.advanced.trace.Trace1 -- [068af332] re시작 time=0ms e=re발생
--re()--
 */