package hello.proxy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class BeanPostProcessorTest {
    @Test
    void one() {
        ApplicationContext context = new AnnotationConfigApplicationContext(ConfigA.class);
        B b = context.getBean("beanA", B.class);
        b.helloB();
    }
    @Configuration
    static class ConfigA {
        @Bean(name = "beanA")
        public A a() {
            return new A();
        }
        @Bean
        public AToBPostProcessor atob() {
            return new AToBPostProcessor();
        }
    }
    static class A {
        public void helloA() {
            System.out.println("helloA");
        }
    }
    static class B {
        public void helloB() {
            System.out.println("helloB");
        }
    }
    static class AToBPostProcessor implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            System.out.println("beanName = " + beanName);
            System.out.println("bean = " + bean);
            if (bean instanceof A) {
                return new B();
            }
            return bean;
        }
    }
}
