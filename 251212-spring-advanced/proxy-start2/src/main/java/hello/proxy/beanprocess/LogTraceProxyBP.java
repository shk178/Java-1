package hello.proxy.beanprocess;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

@RequiredArgsConstructor
public class LogTraceProxyBP implements BeanPostProcessor {
    private final String className;
    private final Advisor advisor;
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!bean.getClass().getSimpleName().contains(className)) {
            return bean;
        }
        System.out.println("LogTraceProxyBP bean = " + bean);
        System.out.println("LogTraceProxyBP beanName = " + beanName);
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.addAdvisor(advisor);
        Object proxy = proxyFactory.getProxy();
        return proxy;
    }
}
/*
2025-12-20 18:52:11.436  INFO 1932 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [e98bd10d] OrderController1.req() // LogTraceAdvice.invoke
2025-12-20 18:52:11.437  INFO 1932 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [e98bd10d] |-->os1p-orderItem
2025-12-20 18:52:11.437  INFO 1932 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [e98bd10d] |   |-->OrderRepository1.save() // LogTraceAdvice.invoke
2025-12-20 18:52:11.437  INFO 1932 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [e98bd10d] |   |   |-->or1p-save
2025-12-20 18:52:12.443  INFO 1932 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [e98bd10d] |   |   |<--or1p-save time=1006ms
2025-12-20 18:52:12.444  INFO 1932 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [e98bd10d] |   |<--OrderRepository1.save() // LogTraceAdvice.invoke time=1007ms
2025-12-20 18:52:12.444  INFO 1932 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [e98bd10d] |<--os1p-orderItem time=1008ms
2025-12-20 18:52:12.444  INFO 1932 --- [nio-8080-exec-1] h.p.trace.logtrace.ThreadLocalLogTrace   : [e98bd10d] OrderController1.req() // LogTraceAdvice.invoke time=1009ms
 */