package spring.tx.pp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@SpringBootTest
public class RequiresNewTest {
    @Autowired
    PlatformTransactionManager ptm;
    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void inner_rollback_requires_new() {
        System.out.println("---inner_rollback_requires_new()---");
        DefaultTransactionAttribute def = new DefaultTransactionAttribute();
        TransactionStatus outer = ptm.getTransaction(def);
        System.out.println("outer.isNewTransaction() = " + outer.isNewTransaction());
        DefaultTransactionAttribute def2 = new DefaultTransactionAttribute();
        def2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = ptm.getTransaction(def2);
        System.out.println("inner.isNewTransaction() = " + inner.isNewTransaction());
        ptm.rollback(inner);
        ptm.commit(outer);
        System.out.println("---inner_rollback_requires_new()---");
    }
}
/*
---inner_rollback_requires_new()---
2025-12-12T16:47:46.162+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T16:47:46.165+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@389407172 wrapping conn0: url=jdbc:h2:mem:5c7e5319-8100-432f-8ed2-5f940bed9d81 user=SA] for JDBC transaction
2025-12-12T16:47:46.167+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@389407172 wrapping conn0: url=jdbc:h2:mem:5c7e5319-8100-432f-8ed2-5f940bed9d81 user=SA] to manual commit
outer.isNewTransaction() = true
2025-12-12T16:47:46.168+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Suspending current transaction, creating new transaction with name [null]
2025-12-12T16:47:46.168+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@114559531 wrapping conn1: url=jdbc:h2:mem:5c7e5319-8100-432f-8ed2-5f940bed9d81 user=SA] for JDBC transaction
2025-12-12T16:47:46.169+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@114559531 wrapping conn1: url=jdbc:h2:mem:5c7e5319-8100-432f-8ed2-5f940bed9d81 user=SA] to manual commit
inner.isNewTransaction() = true
2025-12-12T16:47:46.169+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
2025-12-12T16:47:46.169+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@114559531 wrapping conn1: url=jdbc:h2:mem:5c7e5319-8100-432f-8ed2-5f940bed9d81 user=SA]
2025-12-12T16:47:46.174+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@114559531 wrapping conn1: url=jdbc:h2:mem:5c7e5319-8100-432f-8ed2-5f940bed9d81 user=SA] after transaction
2025-12-12T16:47:46.174+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Resuming suspended transaction after completion of inner transaction
2025-12-12T16:47:46.174+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
2025-12-12T16:47:46.175+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@389407172 wrapping conn0: url=jdbc:h2:mem:5c7e5319-8100-432f-8ed2-5f940bed9d81 user=SA]
2025-12-12T16:47:46.175+09:00 DEBUG 5780 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@389407172 wrapping conn0: url=jdbc:h2:mem:5c7e5319-8100-432f-8ed2-5f940bed9d81 user=SA] after transaction
---inner_rollback_requires_new()---
 */