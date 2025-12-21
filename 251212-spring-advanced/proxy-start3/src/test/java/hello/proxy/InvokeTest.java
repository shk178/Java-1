package hello.proxy;

import hello.proxy.aop.invokeproxy.CallService;
import hello.proxy.aop.invokeproxy.CallService2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class InvokeTest {
    @Autowired
    CallService callService;
    @Autowired
    CallService2 callService2;
    @Test
    void one() {
        callService.external();
    }
    @Test
    void two() {
        callService2.external();
    }
}
/*
ExamAspect.beforeRun: void hello.proxy.aop.invokeproxy.CallService.external()
CallService.external
ExamAspect.beforeRun: void hello.proxy.aop.invokeproxy.CallService.internal(int)
CallService.internal: 1
CallService.internal: 2
CallService.internal: 3
 */
/*
ExamAspect.beforeRun: void hello.proxy.aop.invokeproxy.CallService2.external()
CallService2.external
ExamAspect.beforeRun: void hello.proxy.aop.invokeproxy.CallService2.internal(int)
CallService2.internal: 1
CallService2.internal: 2
CallService2.internal: 3
 */