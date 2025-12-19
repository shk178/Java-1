package hello.proxy;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class ReflectionTest {
    static class Hello {
        public String callA() {
            System.out.println("Hello.callA");
            return "A";
        }
        public String callB() {
            System.out.println("Hello.callB");
            return "B";
        }
    }
    @Test
    void one() {
        Hello target = new Hello();
        System.out.println("start");
        String result1 = target.callA();
        System.out.println("result=" + result1);
        System.out.println("start");
        String result2 = target.callB();
        System.out.println("result=" + result2);
    }
    @Test
    void two() throws Exception {
        Class classHello = Class.forName("hello.proxy.ReflectionTest$Hello");
        Hello target = new Hello();
        Method methodCallA = classHello.getMethod("callA");
        Object result1 = methodCallA.invoke(target);
        System.out.println("result=" + result1);
        Method methodCallB = classHello.getMethod("callB");
        Object result2 = methodCallB.invoke(target);
        System.out.println("result=" + result2);
    }
    @Test
    void logic() throws Exception {
        Class classHello = Class.forName("hello.proxy.ReflectionTest$Hello");
        Hello target = new Hello();
        Method methodCallA = classHello.getMethod("callA");
        Method methodCallB = classHello.getMethod("callB");
        dynamicCall(methodCallA, target);
        dynamicCall(methodCallB, target);
    }
    private void dynamicCall(Method method, Object target) throws Exception {
        System.out.println("start");
        Object result = method.invoke(target);
        System.out.println("result=" + result);
    }
}
