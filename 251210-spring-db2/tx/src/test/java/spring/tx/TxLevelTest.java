package spring.tx;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class TxLevelTest {
    @Autowired
    LevelService levelService;
    @Test
    void orderTest() {
        levelService.write();
        levelService.read();
    }

    @TestConfiguration
    static class TxApplyLevelConfig {
        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }

    @Transactional(readOnly = true)
    static class LevelService {
        @Transactional(readOnly = false)
        public void write() {
            System.out.println("LevelService.write");
            printTxInfo();
        }

        public void read() {
            System.out.println("LevelService.read");
            printTxInfo();
        }
        private void printTxInfo() {
            System.out.println("TransactionSynchronizationManager.isActualTransactionActive() = " + TransactionSynchronizationManager.isActualTransactionActive());
            System.out.println("TransactionSynchronizationManager.isCurrentTransactionReadOnly() = " + TransactionSynchronizationManager.isCurrentTransactionReadOnly());
        }
    }
}
/*
LevelService.write
TransactionSynchronizationManager.isActualTransactionActive() = true
TransactionSynchronizationManager.isCurrentTransactionReadOnly() = false
LevelService.read
TransactionSynchronizationManager.isActualTransactionActive() = true
TransactionSynchronizationManager.isCurrentTransactionReadOnly() = true
 */