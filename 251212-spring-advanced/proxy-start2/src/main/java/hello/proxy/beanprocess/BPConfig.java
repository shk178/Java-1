package hello.proxy.beanprocess;

import hello.proxy.spring.Advice2;
import hello.proxy.spring.LogTraceAdvice;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BPConfig {
    @Bean
    public One one() {
        return new One();
    }
    @Bean
    public BP bP() {
        return new BP();
    }
    @Bean
    public LogTraceProxyBP logTraceProxyBP(LogTrace logTrace) {
        return new LogTraceProxyBP("Repository", advisor(logTrace));
    }
    private Advisor advisor(LogTrace logTrace) {
        LogTraceAdvice advice = new LogTraceAdvice(logTrace);
        return new DefaultPointcutAdvisor(Pointcut.TRUE, advice);
    }
    @Bean
    public Advisor advisor2() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* hello.proxy.beanprocess..*(..))");
        Advice2 advice = new Advice2();
        return new DefaultPointcutAdvisor(pointcut, advice);
    }
}
/*
LogTraceProxyBP bean = hello.proxy.app.v1.OrderRepository1Proxy@62628e78
LogTraceProxyBP beanName = orderRepository1
LogTraceProxyBP bean = hello.proxy.app.v2.OrderRepository2Proxy@2b98b3bb
LogTraceProxyBP beanName = orderRepository2
BP: bean = hello.proxy.beanprocess.One@409986fe
BP: beanName = one
 */