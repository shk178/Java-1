package hello.proxy.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class Advice2 implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Advice2.invoke---");
        System.out.println(invocation.getMethod().getName());
        Object result = invocation.proceed();
        System.out.println("---Advice2.invoke");
        return result;
    }
}
