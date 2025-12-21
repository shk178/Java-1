package hello.proxy;

import hello.proxy.spring.*;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

public class Advice1Test {
    @Test
    void one() {
        ImplementInterface implementInterface = new ImplementClass();
        ConcreteClass concreteClass = new ConcreteClass();
        ProxyFactory proxyFactory1 = new ProxyFactory(implementInterface);
        proxyFactory1.addAdvice(new Advice1());
        DefaultPointcutAdvisor advisor1 = new DefaultPointcutAdvisor(Pointcut.TRUE, new Advice2());
        proxyFactory1.addAdvisor(advisor1);
        ImplementInterface proxy1 = (ImplementInterface) proxyFactory1.getProxy();
        ProxyFactory proxyFactory2 = new ProxyFactory(concreteClass);
        proxyFactory2.addAdvice(new Advice1());
        DefaultPointcutAdvisor advisor2 = new DefaultPointcutAdvisor(new Pointcut1(), new Advice2());
        proxyFactory2.addAdvisor(advisor2);
        ConcreteClass proxy2 = (ConcreteClass) proxyFactory2.getProxy();
        proxy1.run();
        System.out.println();
        proxy2.run();
    }
}
/*
Advice1.invoke---
Advice2.invoke---
ImplementClass.run
---Advice2.invoke
---Advice1.invoke

Advice1.invoke---
Advice2.invoke---
ConcreteClass.run
---Advice2.invoke
---Advice1.invoke
 */
// proxyFactory.setProxyTargetClass(true);를 하면
// 구현클래스여도 CGLIB로 프록시 만든다.
// 포인트컷: 필터링 로직으로 클래스/메서드 이름으로 필터링한다.
// 어드바이스: 프록시가 호출하는 부가 기능 (프록시 로직)
// 어드바이저: 포인트컷 1개 + 어드바이스 1개
// 포인트컷.TRUE: 항상 true 반환