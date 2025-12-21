package hello.proxy.aspectaop;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@RequiredArgsConstructor
public class Aspect1 {
    private final LogTrace logTrace;
    @Around("execution(* hello.proxy.aspectaop.AAOne.*(..))")
    public Object execute(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println();
        System.out.println("pjp.getTarget() = " + pjp.getTarget());
        System.out.println("pjp.getArgs() = " + pjp.getArgs());
        System.out.println("pjp.getSignature() = " + pjp.getSignature());
        TraceStatus traceStatus = null;
        try {
            String msg = pjp.getSignature().toShortString();
            traceStatus = logTrace.begin(msg);
            Object result = pjp.proceed();
            logTrace.end(traceStatus);
            return result;
        } catch (Exception e) {
            logTrace.exception(traceStatus, e);
            throw e;
        }
    }
}
/*
class hello.proxy.aspectaop.AAOne$$EnhancerBySpringCGLIB$$a66395aa

pjp.getTarget() = hello.proxy.aspectaop.AAOne@18151a14
pjp.getArgs() = [Ljava.lang.Object;@894858
pjp.getSignature() = void hello.proxy.aspectaop.AAOne.run()
15:18:40.003 [Test worker] INFO hello.proxy.trace.logtrace.ThreadLocalLogTrace - [9ed65ff3] AAOne.run()
AAOne.run
15:18:40.010 [Test worker] INFO hello.proxy.trace.logtrace.ThreadLocalLogTrace - [9ed65ff3] AAOne.run() time=7ms
 */