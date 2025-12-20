package hello.proxy.beanprocess;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class BP implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof One) {
            System.out.println("BP: bean = " + bean);
            System.out.println("BP: beanName = " + beanName);
            return new Two();
        }
        return bean;
    }
}
