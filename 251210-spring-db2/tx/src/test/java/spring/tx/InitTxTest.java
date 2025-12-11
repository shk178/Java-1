package spring.tx;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class InitTxTest {
    @Autowired
    Hello hello;
    @Test
    void go() {}
    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }

    static class Hello {
        @PostConstruct
        @Transactional
        public void init1() {
            System.out.println("Hello.init1");
            System.out.println("TransactionSynchronizationManager.isActualTransactionActive() = " + TransactionSynchronizationManager.isActualTransactionActive());
        }
        @EventListener(value = ApplicationReadyEvent.class)
        @Transactional
        public void init2() {
            System.out.println("Hello.init2");
            System.out.println("TransactionSynchronizationManager.isActualTransactionActive() = " + TransactionSynchronizationManager.isActualTransactionActive());
        }
    }
}
/*
2025-12-11T21:16:30.144+09:00  INFO 8184 --- [tx] [    Test worker] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2025-12-11T21:16:30.149+09:00  INFO 8184 --- [tx] [    Test worker] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
Hello.init1
TransactionSynchronizationManager.isActualTransactionActive() = false
2025-12-11T21:16:30.316+09:00  INFO 8184 --- [tx] [    Test worker] spring.tx.InitTxTest                     : Started InitTxTest in 2.048 seconds (process running for 3.115)
Hello.init2
TransactionSynchronizationManager.isActualTransactionActive() = true
 */