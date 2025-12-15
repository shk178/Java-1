package hello.advanced.trace2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogTraceConfig {
    @Bean
    public LogTrace logTrace() {
        return threadLocalLogTrace();
    }
    @Bean
    public FieldLogTrace fieldLogTrace() {
        return new FieldLogTrace();
    }
    @Bean
    public ThreadLocalLogTrace threadLocalLogTrace() {
        return new ThreadLocalLogTrace();
    }
}
