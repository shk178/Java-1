package hello.proxy;

import hello.proxy.dynamic.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

public class DynamicProxyTest {
    @Test
    void dynamicA() {
        AInterface target = new AImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target);
        AInterface proxy = (AInterface) Proxy.newProxyInstance(AInterface.class.getClassLoader(), new Class[]{AInterface.class}, handler);
        proxy.call();
        System.out.println("target.getClass() = " + target.getClass());
        System.out.println("proxy.getClass() = " + proxy.getClass());
    }
    @Test
    void dynamicB() {
        BInterface target = new BImpl();
        TimeInvocationHandler handler = new TimeInvocationHandler(target);
        BInterface proxy = (BInterface) Proxy.newProxyInstance(BInterface.class.getClassLoader(), new Class[]{BInterface.class}, handler);
        proxy.call();
        System.out.println("target.getClass() = " + target.getClass());
        System.out.println("proxy.getClass() = " + proxy.getClass());
    }
}
/*
TimeInvocationHandler.invoke
AImpl.call
duration=0
target.getClass() = class hello.proxy.dynamic.AImpl
proxy.getClass() = class com.sun.proxy.$Proxy12
TimeInvocationHandler.invoke
BImpl.call
duration=0
target.getClass() = class hello.proxy.dynamic.BImpl
proxy.getClass() = class com.sun.proxy.$Proxy13
 */