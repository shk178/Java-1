package basic.lecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CheckBeanDefinition {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);
    @Test
    @DisplayName("빈 설정 메타정보 확인")
    void beanDefinitionMeta() {
        BeanDefinition beanDefinition = ac.getBeanDefinition("memberService");
        System.out.println(beanDefinition);
    }
    /*
> Task :testClasses
Root bean: class=null; scope=; abstract=false; lazyInit=null; autowireMode=3; dependencyCheck=0; autowireCandidate=true; primary=false; fallback=false; factoryBeanName=appConfig; factoryMethodName=memberService; initMethodNames=null; destroyMethodNames=[(inferred)]; defined in basic.lecture.AppConfig
> Task :test
     */
}
