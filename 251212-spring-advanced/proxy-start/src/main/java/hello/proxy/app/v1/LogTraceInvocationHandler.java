package hello.proxy.app.v1;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class LogTraceInvocationHandler implements InvocationHandler {
    private final Object target;
    private final LogTrace logTrace;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TraceStatus traceStatus = null;
        try {
            String msg = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
            traceStatus = logTrace.begin(msg);
            Object result = method.invoke(target, args);
            logTrace.end(traceStatus);
            return result;
        } catch (Exception e) {
            logTrace.exception(traceStatus, e);
            throw e;
        }
    }
}
