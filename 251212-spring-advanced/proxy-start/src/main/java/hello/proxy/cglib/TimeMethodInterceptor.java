package hello.proxy.cglib;

import lombok.RequiredArgsConstructor;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class TimeMethodInterceptor implements MethodInterceptor {
    private final Object target;

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        System.out.println("TimeMethodInterceptor.intercept");
        long sTime = System.currentTimeMillis();
        Object result = methodProxy.invoke(target, args);
        long eTime = System.currentTimeMillis();
        System.out.println("duration=" + (eTime - sTime));
        return result;
    }
}
