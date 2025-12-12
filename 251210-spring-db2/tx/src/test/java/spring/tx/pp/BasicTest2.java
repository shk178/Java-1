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
public class BasicTest2 {
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
    void outer_rollback() {
        System.out.println("---outer_rollback()---");
        TransactionStatus outer = ptm.getTransaction(new DefaultTransactionAttribute());
        System.out.println("outer.isNewTransaction() = " + outer.isNewTransaction());
        TransactionStatus inner = ptm.getTransaction(new DefaultTransactionAttribute());
        System.out.println("inner.isNewTransaction() = " + inner.isNewTransaction());
        ptm.commit(inner);
        ptm.rollback(outer);
        System.out.println("---outer_rollback()---");
    }
}
/*
---outer_rollback()---
2025-12-12T16:19:03.373+09:00 DEBUG 17856 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T16:19:03.375+09:00 DEBUG 17856 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Acquired Connection [HikariProxyConnection@581100929 wrapping conn0: url=jdbc:h2:mem:ebf952a7-02ee-407f-869a-c4482699c80b user=SA] for JDBC transaction
2025-12-12T16:19:03.382+09:00 DEBUG 17856 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Switching JDBC Connection [HikariProxyConnection@581100929 wrapping conn0: url=jdbc:h2:mem:ebf952a7-02ee-407f-869a-c4482699c80b user=SA] to manual commit
outer.isNewTransaction() = true
2025-12-12T16:19:03.383+09:00 DEBUG 17856 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Participating in existing transaction
inner.isNewTransaction() = false
2025-12-12T16:19:03.383+09:00 DEBUG 17856 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Initiating transaction rollback
2025-12-12T16:19:03.384+09:00 DEBUG 17856 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Rolling back JDBC transaction on Connection [HikariProxyConnection@581100929 wrapping conn0: url=jdbc:h2:mem:ebf952a7-02ee-407f-869a-c4482699c80b user=SA]
2025-12-12T16:19:03.385+09:00 DEBUG 17856 --- [tx] [    Test worker] o.s.j.d.DataSourceTransactionManager     : Releasing JDBC Connection [HikariProxyConnection@581100929 wrapping conn0: url=jdbc:h2:mem:ebf952a7-02ee-407f-869a-c4482699c80b user=SA] after transaction
---outer_rollback()---
 */