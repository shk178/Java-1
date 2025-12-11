package spring.tx;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TxBasicTest {
    @Autowired
    BasicService basicService;
    @Test
    void proxyCheck() {
        System.out.println("this.getClass() = " + this.getClass());
        System.out.println("basicService.getClass() = " + basicService.getClass());
        assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }
    @Test
    void txTest() {
        basicService.tx();
        basicService.nonTx();
    }

    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        BasicService basicService() {
            return new BasicService();
        }
    }

    static class BasicService {
        @Transactional
        public void tx() {
            System.out.println("BasicService.tx");
            System.out.println(TransactionSynchronizationManager.isActualTransactionActive());
        }

        public void nonTx() {
            System.out.println("BasicService.nonTx");
            System.out.println(TransactionSynchronizationManager.isActualTransactionActive());
        }
    }
}
/*
this.getClass() = class spring.tx.TxBasicTest
basicService.getClass() = class spring.tx.TxBasicTest$BasicService$$SpringCGLIB$$0
BasicService.tx
true
BasicService.nonTx
false
 */