package hello.proxy;

import hello.proxy.beanprocess.BPConfig;
import hello.proxy.beanprocess.One;
import hello.proxy.beanprocess.Two;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BPTest {
    @Test
    void test() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(BPConfig.class);
        Two two = ac.getBean("one", Two.class);
        two.run();
        Assertions.assertThrows(NoSuchBeanDefinitionException.class, () -> ac.getBean(One.class));
    }
}
/*
BP: bean = hello.proxy.beanprocess.One@4e517165
BP: beanName = one
Two.run
 */