package spring.tx.pp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@SpringBootTest
public class BasicTest {
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
    void commit() {
        TransactionStatus ts = ptm.getTransaction(new DefaultTransactionAttribute());
        ptm.commit(ts);
    }
    @Test
    void rollback() {
        TransactionStatus ts = ptm.getTransaction(new DefaultTransactionAttribute());
        ptm.rollback(ts);
    }
    @Test
    void double_commit() {
        TransactionStatus ts1 = ptm.getTransaction(new DefaultTransactionAttribute());
        ptm.commit(ts1);
        TransactionStatus ts2 = ptm.getTransaction(new DefaultTransactionAttribute());
        ptm.commit(ts2);
    }
    @Test
    void inner_commit() {
        System.out.println("---inner_commit()---");
        TransactionStatus outer = ptm.getTransaction(new DefaultTransactionAttribute());
        System.out.println("outer.isNewTransaction() = " + outer.isNewTransaction());
        TransactionStatus inner = ptm.getTransaction(new DefaultTransactionAttribute());
        System.out.println("inner.isNewTransaction() = " + inner.isNewTransaction());
        ptm.commit(inner);
        ptm.commit(outer);
        System.out.println("---inner_commit()---");
    }
}
/*
---inner_commit()---
2025-12-12T13:33:04.146+09:00 DEBUG 21260 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T13:33:04.152+09:00 DEBUG 21260 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@2091447767 wrapping conn0: url=jdbc:h2:mem:9e61e410-2bd4-4c0f-a737-94fc080dd3d1 user=SA] for JDBC transaction
2025-12-12T13:33:04.154+09:00 DEBUG 21260 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@2091447767 wrapping conn0: url=jdbc:h2:mem:9e61e410-2bd4-4c0f-a737-94fc080dd3d1 user=SA] to manual commit
outer.isNewTransaction() = true
2025-12-12T13:33:04.155+09:00 DEBUG 21260 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Participating in existing transaction
inner.isNewTransaction() = false
2025-12-12T13:33:04.156+09:00 DEBUG 21260 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction commit
2025-12-12T13:33:04.156+09:00 DEBUG 21260 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Committing JDBC transaction on Connection [HikariProxyConnection@2091447767 wrapping conn0: url=jdbc:h2:mem:9e61e410-2bd4-4c0f-a737-94fc080dd3d1 user=SA]
2025-12-12T13:33:04.158+09:00 DEBUG 21260 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@2091447767 wrapping conn0: url=jdbc:h2:mem:9e61e410-2bd4-4c0f-a737-94fc080dd3d1 user=SA] after transaction
---inner_commit()---
 */