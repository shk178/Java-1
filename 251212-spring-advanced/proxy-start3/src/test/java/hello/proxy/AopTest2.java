package hello.proxy;

import hello.proxy.aop.ExamService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AopTest2 {
    @Autowired
    ExamService examService;
    @Test
    void one() {
        examService.req("a1");
        examService.req("a2");
        examService.req("a3");
        examService.req("a4");
        examService.req("a5");
        examService.req("b1");
    }
}
/*
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)

re
java.lang.RuntimeException: re
	at hello.proxy.aop.ExamRepository.save(ExamRepository.java:12)
	at hello.proxy.aop.ExamRepository$$FastClassBySpringCGLIB$$10ac8b71.invoke(<generated>)
 */
/*
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
retryCount = 1
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
retryCount = 1
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
retryCount = 1
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
retryCount = 1
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
retryCount = 1
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
retryCount = 1
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
retryCount = 1
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
retryCount = 1
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
retryCount = 1
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
retryCount = 1
retryCount = 2
ExamAspect.beforeRun: void hello.proxy.aop.ExamService.req(String)
retryCount = 1
ExamAspect.beforeRun: String hello.proxy.aop.ExamRepository.save(String)
retryCount = 1
 */