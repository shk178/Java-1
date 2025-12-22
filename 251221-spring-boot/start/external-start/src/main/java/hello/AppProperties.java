package hello;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppProperties {
    private final Environment env;
    @PostConstruct
    public void get() {
        String url = env.getProperty("url");
        String username = env.getProperty("username");
        String password = env.getProperty("password");
        log.info("env url={}", url);
        log.info("env username={}", username);
        log.info("env password={}", password);
    }
}
/*
2025-12-22T16:43:48.482+09:00  INFO 2552 --- [           main] hello.AppProperties                      : env url=devdb1
2025-12-22T16:43:48.483+09:00  INFO 2552 --- [           main] hello.AppProperties                      : env username=user
2025-12-22T16:43:48.483+09:00  INFO 2552 --- [           main] hello.AppProperties                      : env password=dev_pw1
List.of(appArgs.getSourceArgs()) = []
 */