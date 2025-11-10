package basic.lecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeanFind {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
    @Test
    @DisplayName("모든 빈 출력 테스트")
    void findAllBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = ac.getBean(beanDefinitionName);
            System.out.println("ac.getBean(" + beanDefinitionName + ") = " + bean);
        }
        System.out.println();
        System.out.println();
    }
    @Test
    @DisplayName("App 빈 출력 테스트")
    void findAppBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);
            if (beanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION) {
                Object bean = ac.getBean(beanDefinitionName);
                System.out.println("ac.getBean(" + beanDefinitionName + ") = " + bean);
            }
        }
        System.out.println();
        System.out.println();
    }
    @Test
    @DisplayName("App 제외 빈 출력 테스트")
    void findEtcBean() {
        String[] beanDefinitionNames = ac.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);
            if (beanDefinition.getRole() == BeanDefinition.ROLE_SUPPORT) {
                Object bean = ac.getBean(beanDefinitionName);
                System.out.println("[ROLE_SUPPORT] ac.getBean(" + beanDefinitionName + ") = " + bean);
            } else if (beanDefinition.getRole() == BeanDefinition.ROLE_INFRASTRUCTURE) {
                Object bean = ac.getBean(beanDefinitionName);
                System.out.println("[ROLE_INFRASTRUCTURE] ac.getBean(" + beanDefinitionName + ") = " + bean);
            }
        }
        System.out.println();
        System.out.println();
    }
}
