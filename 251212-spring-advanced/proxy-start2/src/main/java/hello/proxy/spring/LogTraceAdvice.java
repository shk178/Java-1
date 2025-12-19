package hello.proxy.spring;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class LogTraceAdvice implements MethodInterceptor {
    private final LogTrace logTrace;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        TraceStatus traceStatus = null;
        try {
            Method method = invocation.getMethod();
            String msg = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "() // LogTraceAdvice.invoke";
            traceStatus = logTrace.begin(msg);
            Object result = invocation.proceed();
            logTrace.end(traceStatus);
            return result;
        } catch (Exception e) {
            logTrace.exception(traceStatus, e);
            throw e;
        }
    }
}
