package hello.proxy;

import hello.proxy.app.v1.AppConfig1;
import hello.proxy.app.v2.AppConfig2;
import hello.proxy.trace.logtrace.LogTrace;
import hello.proxy.trace.logtrace.ThreadLocalLogTrace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({AppConfig1.class, AppConfig2.class})
@SpringBootApplication(scanBasePackages = "hello.proxy.app") //스프링 버전에 따라 설정
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}
	@Bean
	public LogTrace logTrace() {
		return new ThreadLocalLogTrace();
	}

}
