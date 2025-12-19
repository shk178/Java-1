package hello.proxy;

import hello.proxy.cglib.ConcreteService;
import hello.proxy.cglib.TimeMethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Enhancer;

// JDK 동적 프록시는 인터페이스를 구현해서 프록시를 만든다.
// GCLIB는 구체 클래스를 상속해서 프록시를 만든다.
public class CGLIBTest {
    @Test
    void one() {
        ConcreteService target = new ConcreteService();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ConcreteService.class); // 프록시는 구체 클래스를 상속받는다.
        enhancer.setCallback(new TimeMethodInterceptor(target)); // 프록시는 실행 로직을 할당받는다.
        ConcreteService proxy = (ConcreteService) enhancer.create(); // 프록시가 만들어진다.
        System.out.println("target.getClass() = " + target.getClass());
        System.out.println("proxy.getClass() = " + proxy.getClass());
        proxy.call();
    }
}
/*
target.getClass() = class hello.proxy.cglib.ConcreteService
proxy.getClass() = class hello.proxy.cglib.ConcreteService$$EnhancerByCGLIB$$627a4e48
TimeMethodInterceptor.intercept
ConcreteService.call
duration=9
 */