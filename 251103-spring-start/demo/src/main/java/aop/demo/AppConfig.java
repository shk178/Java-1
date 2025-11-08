package aop.demo;

import aop.demo.aspect.Advices;
import aop.demo.aspect.LoggingAspect;
import aop.demo.model.Circle;
import aop.demo.model.Triangle;
import aop.demo.service.ShapeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy //AOP 프록시 활성화
//@ComponentScan(basePackages = "aop.demo.aspect") //@Component 붙은 클래스 자동 등록
public class AppConfig {
    @Bean(name = "circleBean")
    public Circle createCircle() {
        Circle circle = new Circle();
        circle.setName("c1");
        return circle;
    }
    @Bean(name = "triangleBean")
    public Triangle createTriangle() {
        Triangle triangle = new Triangle();
        triangle.setName("t1");
        return triangle;
    }
    @Bean(name = "shapeServiceBean")
    public ShapeService createShapeService() {
        ShapeService shapeService = new ShapeService();
        shapeService.setCircle(createCircle());
        shapeService.setTriangle(createTriangle());
        return shapeService;
    }
    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }
    @Bean
    public Advices advices() {
        return new Advices();
    }
}
