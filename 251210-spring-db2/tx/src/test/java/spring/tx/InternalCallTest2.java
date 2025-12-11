package spring.tx;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class InternalCallTest2 {
    @Autowired
    CallService callService;
    @Test
    void printProxy() {
        System.out.println("callService.getClass() = " + callService.getClass());
    }
    @Test
    void externalCall() {
        System.out.println("externalCall");
        callService.external();
    }

    @TestConfiguration
    static class InternalCallConfig2 {
        @Bean
        CallService callService() {
            return new CallService(callService2());
        }
        @Bean
        CallService2 callService2() {
            return new CallService2();
        }
    }

    @RequiredArgsConstructor
    static class CallService {
        private final CallService2 callService2;

        public void external() {
            System.out.println("CallService.external");
            printTxInfo();
            callService2.internal2();
        }
        private void printTxInfo() {
            System.out.println("TransactionSynchronizationManager.isActualTransactionActive() = " + TransactionSynchronizationManager.isActualTransactionActive());
        }
    }

    static class CallService2 {
        @Transactional
        public void internal2() {
            System.out.println("CallService2.internal2");
            printTxInfo2();
        }

        private void printTxInfo2() {
            System.out.println("TransactionSynchronizationManager.isActualTransactionActive() = " + TransactionSynchronizationManager.isActualTransactionActive());
        }
    }
}
/*
externalCall
CallService.external
TransactionSynchronizationManager.isActualTransactionActive() = false
CallService2.internal2
TransactionSynchronizationManager.isActualTransactionActive() = true
callService.getClass() = class spring.tx.InternalCallTest2$CallService
 */