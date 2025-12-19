package hello.proxy;

import hello.proxy.cglib.ConcreteService;
import hello.proxy.cglib.ServiceImpl;
import hello.proxy.cglib.ServiceInterface;
import hello.proxy.spring.TimeAdvice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class ProxyFactoryTest {
    @Test
    @DisplayName("인터페이스가 있으면 JDK 동적 프록시")
    void one() {
        ServiceInterface target = new ServiceImpl();
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvice(new TimeAdvice());
        ServiceInterface proxy = (ServiceInterface) proxyFactory.getProxy();
        System.out.println("target.getClass() = " + target.getClass());
        System.out.println("proxy.getClass() = " + proxy.getClass());
        proxy.save();
        assertThat(AopUtils.isAopProxy(proxy)).isTrue();
        assertThat(AopUtils.isJdkDynamicProxy(proxy)).isTrue();
        assertThat(AopUtils.isCglibProxy(proxy)).isFalse();
    }
    @Test
    @DisplayName("구체클래스는 CGLIB 사용")
    void two() {
        ConcreteService target = new ConcreteService();
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvice(new TimeAdvice());
        ConcreteService proxy = (ConcreteService) proxyFactory.getProxy();
        System.out.println("target.getClass() = " + target.getClass());
        System.out.println("proxy.getClass() = " + proxy.getClass());
        proxy.call();
        assertThat(AopUtils.isAopProxy(proxy)).isTrue();
        assertThat(AopUtils.isJdkDynamicProxy(proxy)).isFalse();
        assertThat(AopUtils.isCglibProxy(proxy)).isTrue();
    }
}
/*
target.getClass() = class hello.proxy.cglib.ServiceImpl
proxy.getClass() = class com.sun.proxy.$Proxy13
TimeAdvice.invoke
ServiceImpl.save
duration=0
 */
/*
target.getClass() = class hello.proxy.cglib.ConcreteService
proxy.getClass() = class hello.proxy.cglib.ConcreteService$$EnhancerBySpringCGLIB$$4013c53e
TimeAdvice.invoke
ConcreteService.call
duration=10
 */