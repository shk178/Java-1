package hello.proxy.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class Advice1 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Advice1.invoke---");
        Object result = invocation.proceed();
        System.out.println("---Advice1.invoke");
        return result;
    }
}
