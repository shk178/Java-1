package basic.web2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LectureApplication {

	public static void main(String[] args) {
		SpringApplication.run(LectureApplication.class, args);
	}

}
/*
MyLogger.init(17a51773-d82d-4b52-9779-bfa815ec3fd3): basic.web2.MyLogger@61b2e2fb
log: [17a51773-d82d-4b52-9779-bfa815ec3fd3] [http://localhost:9090/log-demo] controller test
log: [17a51773-d82d-4b52-9779-bfa815ec3fd3] [http://localhost:9090/log-demo] service id = testId
MyLogger.close(17a51773-d82d-4b52-9779-bfa815ec3fd3)
 */