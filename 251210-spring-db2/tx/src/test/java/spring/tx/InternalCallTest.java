package spring.tx;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class InternalCallTest {
    @Autowired
    CallService callService;
    @Test
    void printProxy() {
        System.out.println("callService.getClass() = " + callService.getClass());
    }
    @Test
    void internalCall() {
        System.out.println("internalCall");
        callService.internal();
    }
    @Test
    void externalCall() {
        System.out.println("externalCall");
        callService.external();
    }

    @TestConfiguration
    static class InternalCallConfig {
        @Bean
        CallService callService() {
            return new CallService();
        }
    }

    static class CallService {
        @Transactional
        public void internal() {
            System.out.println("CallService.internal");
            printTxInfo();
        }

        public void external() {
            System.out.println("CallService.external");
            printTxInfo();
            internal();
        }
        private void printTxInfo() {
            System.out.println("TransactionSynchronizationManager.isActualTransactionActive() = " + TransactionSynchronizationManager.isActualTransactionActive());
        }
    }
}
/*
externalCall
CallService.external
TransactionSynchronizationManager.isActualTransactionActive() = false
CallService.internal
TransactionSynchronizationManager.isActualTransactionActive() = false
internalCall
CallService.internal
TransactionSynchronizationManager.isActualTransactionActive() = true
callService.getClass() = class spring.tx.InternalCallTest$CallService$$SpringCGLIB$$0
 */