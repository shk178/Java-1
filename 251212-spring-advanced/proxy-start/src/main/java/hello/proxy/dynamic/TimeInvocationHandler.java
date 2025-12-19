package hello.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TimeInvocationHandler implements InvocationHandler {
    private final Object target;
    public TimeInvocationHandler(Object target) {
        this.target = target;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("TimeInvocationHandler.invoke");
        long sTime = System.currentTimeMillis();
        Object result = method.invoke(target, args);
        long eTime = System.currentTimeMillis();
        System.out.println("duration=" + (eTime - sTime));
        return result;
    }
}
