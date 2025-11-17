package basic.web;

import basic.lecture.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class LectureApplication {

	public static void main(String[] args) {
		SpringApplication.run(LectureApplication.class, args);
		/*
		Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'logService' defined in file [C:\Users...LogService.class]: Unsatisfied dependency expressed through constructor parameter 0: Error creating bean with name 'myLogger': Scope 'request' is not active for the current thread; consider defining a scoped proxy for this bean if you intend to refer to it from a singleton
		 */
		// http://localhost:9090/log-demo -> OK
	}

}
