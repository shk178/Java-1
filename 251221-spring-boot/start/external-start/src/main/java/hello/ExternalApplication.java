package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExternalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalApplication.class, args);
    }

}
/*
2025-12-22T16:46:16.620+09:00  INFO 19068 --- [           main] hello.AppProperties                      : env url=dev1
2025-12-22T16:46:16.621+09:00  INFO 19068 --- [           main] hello.AppProperties                      : env username=user
2025-12-22T16:46:16.621+09:00  INFO 19068 --- [           main] hello.AppProperties                      : env password=dev1
 */