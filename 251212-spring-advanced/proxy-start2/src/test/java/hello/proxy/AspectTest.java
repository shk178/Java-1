package hello.proxy;

import hello.proxy.aspectaop.AAConfig;
import hello.proxy.aspectaop.AAOne;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AspectTest {
    @Test
    void one() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(AAConfig.class);
        AAOne one = ac.getBean("aAOne", AAOne.class);
        System.out.println(one.getClass());
        one.run();
    }
}
