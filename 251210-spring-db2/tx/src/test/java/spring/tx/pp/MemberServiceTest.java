package spring.tx.pp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import spring.tx.LogRepository;
import spring.tx.MemberRepository;
import spring.tx.MemberService;

@SpringBootTest
public class MemberServiceTest {
    @Autowired
    MemberService memberService;
    @TestConfiguration
    static class Config {
        @PersistenceContext
        EntityManager em;
        @Bean
        MemberRepository memberRepository() {
            return new MemberRepository(em);
        }
        @Bean
        LogRepository logRepository() {
            return new LogRepository(em);
        }
        @Bean
        MemberService memberService(
                MemberRepository memberRepository,
                LogRepository logRepository
        ) {
            return new MemberService(memberRepository, logRepository);
        }
    }
    @Test
    public void save_one() {
        System.out.println("---save_one()---");
        memberService.join("ne");
        System.out.println("---save_one()---");
    }
    @Test
    public void save_two() {
        System.out.println("---save_two()---");
        memberService.join("re");
        System.out.println("---save_two()---");
    }
}
/*
---save_one()---
2025-12-12T17:28:31.129+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.MemberRepository.save]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T17:28:31.129+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(181814414<open>)] for JPA transaction
2025-12-12T17:28:31.135+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@423d662a]
2025-12-12T17:28:31.136+09:00 TRACE 14184 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.MemberRepository.save]
2025-12-12T17:28:31.141+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : select next value for member_seq
2025-12-12T17:28:31.175+09:00 TRACE 14184 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.MemberRepository.save]
2025-12-12T17:28:31.175+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T17:28:31.175+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(181814414<open>)]
2025-12-12T17:28:31.186+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : insert into member (username,id) values (?,?)
2025-12-12T17:28:31.192+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(181814414<open>)] after transaction
2025-12-12T17:28:31.192+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.LogRepository.save]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T17:28:31.192+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(214853420<open>)] for JPA transaction
2025-12-12T17:28:31.192+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@496004e3]
2025-12-12T17:28:31.192+09:00 TRACE 14184 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.LogRepository.save]
2025-12-12T17:28:31.192+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : select next value for log_seq
2025-12-12T17:28:31.193+09:00 TRACE 14184 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.LogRepository.save]
2025-12-12T17:28:31.193+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T17:28:31.193+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(214853420<open>)]
2025-12-12T17:28:31.194+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : insert into log (message,id) values (?,?)
2025-12-12T17:28:31.194+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(214853420<open>)] after transaction
2025-12-12T17:28:31.555+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : select m1_0.id,m1_0.username from member m1_0 where m1_0.username=?
Optional[spring.tx.Member@12dd3501]
2025-12-12T17:28:31.580+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : select l1_0.id,l1_0.message from log l1_0 where l1_0.message=?
Optional[spring.tx.Log@24673720]
---save_one()---
---save_two()---
2025-12-12T17:28:31.597+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.MemberRepository.save]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T17:28:31.597+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(298021609<open>)] for JPA transaction
2025-12-12T17:28:31.597+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@4b59a1c1]
2025-12-12T17:28:31.597+09:00 TRACE 14184 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.MemberRepository.save]
2025-12-12T17:28:31.597+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : select next value for member_seq
2025-12-12T17:28:31.597+09:00 TRACE 14184 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.MemberRepository.save]
2025-12-12T17:28:31.597+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T17:28:31.597+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(298021609<open>)]
2025-12-12T17:28:31.599+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : insert into member (username,id) values (?,?)
2025-12-12T17:28:31.599+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(298021609<open>)] after transaction
2025-12-12T17:28:31.600+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.LogRepository.save]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T17:28:31.600+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(736062588<open>)] for JPA transaction
2025-12-12T17:28:31.600+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@13ac1657]
2025-12-12T17:28:31.600+09:00 TRACE 14184 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.LogRepository.save]
2025-12-12T17:28:31.601+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : select next value for log_seq
2025-12-12T17:28:31.604+09:00 TRACE 14184 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.LogRepository.save] after exception: java.lang.RuntimeException: join: re
2025-12-12T17:28:31.604+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback
2025-12-12T17:28:31.606+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Rolling back JPA transaction on EntityManager [SessionImpl(736062588<open>)]
2025-12-12T17:28:31.607+09:00 DEBUG 14184 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(736062588<open>)] after transaction
java.lang.RuntimeException: join: re
2025-12-12T17:28:31.608+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : select m1_0.id,m1_0.username from member m1_0 where m1_0.username=?
Optional[spring.tx.Member@487f025]
2025-12-12T17:28:31.609+09:00 DEBUG 14184 --- [tx] [    Test worker] org.hibernate.SQL                        : select l1_0.id,l1_0.message from log l1_0 where l1_0.message=?
Optional.empty
---save_two()---
 */