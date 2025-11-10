package basic.lecture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class LectureApplication {

	public static void main(String[] args) {
		//SpringApplication.run(LectureApplication.class, args);
		ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
	}

}
