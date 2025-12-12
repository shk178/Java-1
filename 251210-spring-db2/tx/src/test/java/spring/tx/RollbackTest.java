package spring.tx;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class RollbackTest {
    @Autowired
    RollbackService rollbackService;

    @Test
    void testRe() {
        assertThatThrownBy(
                () -> rollbackService.re()
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void testCe() {
        assertThatThrownBy(
                () -> rollbackService.ce()
        ).isInstanceOf(Exception.class);
    }

    @Test
    void testRf() {
        assertThatThrownBy(
                () -> rollbackService.rf()
        ).isInstanceOf(Exception.class);
    }

    @TestConfiguration
    static class RollbackTestConfig {
        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }
    }

    static class RollbackService {
        @Transactional
        public void re() {
            throw new RuntimeException();
            // 롤백
        }
        @Transactional
        public void ce() throws Exception {
            throw new Exception();
            // 커밋
        }
        @Transactional(rollbackFor = Exception.class)
        public void rf() throws Exception {
            throw new Exception();
            // 롤백
        }
    }
}
/*
2025-12-12T11:32:22.350+09:00  INFO 6912 --- [tx] [    Test worker] spring.tx.RollbackTest                   : Starting RollbackTest using Java 21.0.8 with PID 6912 (started by user in C:\Users ... \tx)
2025-12-12T11:32:22.351+09:00  INFO 6912 --- [tx] [    Test worker] spring.tx.RollbackTest                   : No active profile set, falling back to 1 default profile: "default"
2025-12-12T11:32:22.721+09:00  INFO 6912 --- [tx] [    Test worker] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2025-12-12T11:32:22.744+09:00  INFO 6912 --- [tx] [    Test worker] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 12 ms. Found 0 JPA repository interfaces.
2025-12-12T11:32:23.062+09:00  INFO 6912 --- [tx] [    Test worker] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2025-12-12T11:32:23.222+09:00  INFO 6912 --- [tx] [    Test worker] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection conn0: url=jdbc:h2:mem:600ab882-cf78-4bd3-a516-653a570420a0 user=SA
2025-12-12T11:32:23.224+09:00  INFO 6912 --- [tx] [    Test worker] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2025-12-12T11:32:23.270+09:00  INFO 6912 --- [tx] [    Test worker] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2025-12-12T11:32:23.319+09:00  INFO 6912 --- [tx] [    Test worker] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.6.36.Final
2025-12-12T11:32:23.353+09:00  INFO 6912 --- [tx] [    Test worker] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2025-12-12T11:32:23.623+09:00  INFO 6912 --- [tx] [    Test worker] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2025-12-12T11:32:23.712+09:00  INFO 6912 --- [tx] [    Test worker] org.hibernate.orm.connections.pooling    : HHH10001005: Database info:
	Database JDBC URL [Connecting through datasource 'HikariDataSource (HikariPool-1)']
	Database driver: undefined/unknown
	Database version: 2.3.232
	Autocommit mode: undefined/unknown
	Isolation level: undefined/unknown
	Minimum pool size: undefined/unknown
	Maximum pool size: undefined/unknown
2025-12-12T11:32:24.025+09:00  INFO 6912 --- [tx] [    Test worker] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2025-12-12T11:32:24.032+09:00  INFO 6912 --- [tx] [    Test worker] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2025-12-12T11:32:24.207+09:00  INFO 6912 --- [tx] [    Test worker] spring.tx.RollbackTest                   : Started RollbackTest in 2.104 seconds (process running for 3.327)
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build what is described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (C:\Users ... \byte-buddy-agent-1.15.11.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
2025-12-12T11:32:24.774+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.RollbackTest$RollbackService.ce]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T11:32:24.804+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(735738459<open>)] for JPA transaction
2025-12-12T11:32:24.808+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@2a341e3d]
2025-12-12T11:32:24.809+09:00 TRACE 6912 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.RollbackTest$RollbackService.ce]
2025-12-12T11:32:24.810+09:00 TRACE 6912 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.RollbackTest$RollbackService.ce] after exception: java.lang.Exception
2025-12-12T11:32:24.810+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T11:32:24.810+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(735738459<open>)]
2025-12-12T11:32:24.812+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(735738459<open>)] after transaction
2025-12-12T11:32:24.837+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.RollbackTest$RollbackService.re]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T11:32:24.838+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(553790651<open>)] for JPA transaction
2025-12-12T11:32:24.838+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@353422fd]
2025-12-12T11:32:24.838+09:00 TRACE 6912 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.RollbackTest$RollbackService.re]
2025-12-12T11:32:24.839+09:00 TRACE 6912 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.RollbackTest$RollbackService.re] after exception: java.lang.RuntimeException
2025-12-12T11:32:24.839+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback
2025-12-12T11:32:24.839+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Rolling back JPA transaction on EntityManager [SessionImpl(553790651<open>)]
2025-12-12T11:32:24.841+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(553790651<open>)] after transaction
2025-12-12T11:32:24.846+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.RollbackTest$RollbackService.rf]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,-java.lang.Exception
2025-12-12T11:32:24.847+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(1825991051<open>)] for JPA transaction
2025-12-12T11:32:24.847+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@5ac7550a]
2025-12-12T11:32:24.847+09:00 TRACE 6912 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.RollbackTest$RollbackService.rf]
2025-12-12T11:32:24.848+09:00 TRACE 6912 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.RollbackTest$RollbackService.rf] after exception: java.lang.Exception
2025-12-12T11:32:24.848+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback
2025-12-12T11:32:24.848+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Rolling back JPA transaction on EntityManager [SessionImpl(1825991051<open>)]
2025-12-12T11:32:24.848+09:00 DEBUG 6912 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(1825991051<open>)] after transaction
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2025-12-12T11:32:24.861+09:00  INFO 6912 --- [tx] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2025-12-12T11:32:24.863+09:00  INFO 6912 --- [tx] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2025-12-12T11:32:24.865+09:00  INFO 6912 --- [tx] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
 */