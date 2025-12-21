package hello.proxy.aspectaop;

import hello.proxy.trace.logtrace.LogTrace;
import hello.proxy.trace.logtrace.ThreadLocalLogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AAConfig {
    @Bean
    public LogTrace logTrace() {
        return new ThreadLocalLogTrace();
    }
    @Bean
    public Aspect1 aspect1(LogTrace logTrace) {
        return new Aspect1(logTrace);
    }
    @Bean
    public AAOne aAOne() {
        return new AAOne();
    }
}
