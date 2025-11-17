package basic.lecture3;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class NetworkClientTest {
    @Test
    public void lifeCycleTest() {
        ConfigurableApplicationContext ac = new AnnotationConfigApplicationContext(LifeCycleConfig.class);
        ac.close();
    }
    @Configuration
    static class LifeCycleConfig {
        @Bean
        public NetworkClient networkClient() {
            NetworkClient networkClient = new NetworkClient();
            networkClient.setUrl("http://hello-spring.dev");
            return networkClient;
        }
        /*
        생성자 호출, url = null
        connect: http://hello-spring.dev
        call: http://hello-spring.dev, message = 초기화 연결 메시지
        disconnect: http://hello-spring.dev
         */
        //@Bean /* initMethod는 지정해야 되고 destroyMethod는 추론 지원한다. */
        @Bean(initMethod = "init", destroyMethod = "close")
        public NetworkClient2 networkClient2() {
            NetworkClient2 networkClient2 = new NetworkClient2();
            networkClient2.setUrl("url주소");
            return networkClient2;
        }
        /*
        생성자 호출, url = null
        connect: http://hello-spring.dev
        call: http://hello-spring.dev, message = 초기화 연결 메시지
        생성자 호출, url = null
        connect: url주소
        call: url주소, message = 초기화 연결 메시지
        disconnect: url주소
        disconnect: http://hello-spring.dev
         */
        @Bean
        public NetworkClient3 networkClient3() {
            NetworkClient3 networkClient3 = new NetworkClient3();
            networkClient3.setUrl("urlAddress");
            return networkClient3;
        }
        /*
        생성자 호출, url = null
        connect: http://hello-spring.dev
        call: http://hello-spring.dev, message = 초기화 연결 메시지
        생성자 호출, url = null
        connect: url주소
        call: url주소, message = 초기화 연결 메시지
        생성자 호출, url = null
        connect: urlAddress
        call: urlAddress, message = 초기화 연결 메시지
        disconnect: urlAddress
        disconnect: url주소
        disconnect: http://hello-spring.dev
         */
    }
}