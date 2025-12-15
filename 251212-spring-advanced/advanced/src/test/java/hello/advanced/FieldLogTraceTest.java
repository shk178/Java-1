package hello.advanced;

import hello.advanced.trace.TraceStatus;
import hello.advanced.trace2.FieldLogTrace;
import org.junit.jupiter.api.Test;

public class FieldLogTraceTest {
    @Test
    void ne3() {
        System.out.println("--ne3()--");
        FieldLogTrace fieldLogTrace = new FieldLogTrace();
        TraceStatus traceStatus1 = fieldLogTrace.start("ne3시작-traceStatus1");
        TraceStatus traceStatus2 = fieldLogTrace.start("ne3시작-traceStatus2");
        fieldLogTrace.complete(traceStatus2);
        fieldLogTrace.complete(traceStatus1);
        System.out.println("--ne3()--");
    }
    @Test
    void ce3() {
        System.out.println("--ce3()--");
        FieldLogTrace fieldLogTrace = new FieldLogTrace();
        TraceStatus traceStatus1 = fieldLogTrace.start("ce3시작-traceStatus1");
        TraceStatus traceStatus2 = fieldLogTrace.start("ce3시작-traceStatus2");
        fieldLogTrace.except(traceStatus2, new Exception("ce3발생-traceStatus2"));
        fieldLogTrace.except(traceStatus1, new Exception("ce3발생-traceStatus1"));
        System.out.println("--ce3()--");
    }
    @Test
    void re3() {
        System.out.println("--re3()--");
        FieldLogTrace fieldLogTrace = new FieldLogTrace();
        TraceStatus traceStatus1 = fieldLogTrace.start("re3시작-traceStatus1");
        TraceStatus traceStatus2 = fieldLogTrace.start("re3시작-traceStatus2");
        fieldLogTrace.except(traceStatus2, new RuntimeException("re3발생-traceStatus2"));
        fieldLogTrace.except(traceStatus1, new RuntimeException("re3발생-traceStatus1"));
        System.out.println("--re3()--");
    }
}
/*
--ce3()--
19:29:36.077 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [90f73279] ce3시작-traceStatus1
19:29:36.083 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [90f73279] |-->ce3시작-traceStatus2
19:29:36.084 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [90f73279] |<X-ce3시작-traceStatus2 time=1ms e=ce3발생-traceStatus2
19:29:36.084 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [90f73279] ce3시작-traceStatus1 time=9ms e=ce3발생-traceStatus1
--ce3()--
--ne3()--
19:29:36.100 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [e8bdb29a] ne3시작-traceStatus1
19:29:36.100 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [e8bdb29a] |-->ne3시작-traceStatus2
19:29:36.100 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [e8bdb29a] |<--ne3시작-traceStatus2 time=0ms
19:29:36.100 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [e8bdb29a] ne3시작-traceStatus1 time=0ms
--ne3()--
--re3()--
19:29:36.102 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [804d69c7] re3시작-traceStatus1
19:29:36.102 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [804d69c7] |-->re3시작-traceStatus2
19:29:36.102 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [804d69c7] |<X-re3시작-traceStatus2 time=0ms e=re3발생-traceStatus2
19:29:36.102 [Test worker] INFO hello.advanced.trace2.FieldLogTrace -- [804d69c7] re3시작-traceStatus1 time=0ms e=re3발생-traceStatus1
--re3()--
 */