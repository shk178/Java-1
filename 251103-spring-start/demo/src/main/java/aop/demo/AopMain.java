package aop.demo;

import aop.demo.service.ShapeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class AopMain {
    public static void main(String[] args) {
        //SpringApplication.run(AopMain.class, args);
        ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        ShapeService shapeService = ctx.getBean("shapeServiceBean", ShapeService.class);
        System.out.println();
        System.out.println(shapeService.getCircle().getName());
        System.out.println();
        System.out.println(shapeService.getTriangle().getName());
        System.out.println();
        shapeService.getTriangle().setName("t2");
        System.out.println();
        shapeService.getTriangle().setNameandReturn("t3");
        System.out.println();
        shapeService.getTriangle().printName();
    }
}
/*

LoggingAspect.SecondAdvice run
LoggingAspect.LoggingAdvice run
execution(String aop.demo.model.Circle.getName())
aop.demo.model.Circle@335b5620
pjp.proceed() returns=c2
LoggingAspect.ThirdAdvice run
LoggingAspect.SecondAdvice run
LoggingAspect.LoggingAdvice run
LoggingAspect.FourthAdvice run
LoggingAspect.FifthAdvice run
c2

LoggingAspect.SecondAdvice run
LoggingAspect.LoggingAdvice run
LoggingAspect.SecondAdvice run
LoggingAspect.LoggingAdvice run
LoggingAspect.FourthAdvice run
t1

LoggingAspect.SecondAdvice run
LoggingAspect.LoggingAdvice run
Advices.two run: t2 (beforeName=t1)
LoggingAspect.FourthAdvice run
LoggingAspect.FifthAdvice run

LoggingAspect.SecondAdvice run
LoggingAspect.LoggingAdvice run
Advices.two run: t3 (beforeName=t2)
Advices.three run: name2=t3, returnString=t3
LoggingAspect.FourthAdvice run
LoggingAspect.FifthAdvice run

LoggingAspect.SecondAdvice run
LoggingAspect.LoggingAdvice run
printName: t3
pjp.proceed() returns=null
LoggingAspect.FourthAdvice run
 */