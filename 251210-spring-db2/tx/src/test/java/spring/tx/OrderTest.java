package spring.tx;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class OrderTest {
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Test
    void ne() throws BzException {
        Order order = new Order();
        order.setUsername("normal");
        orderService.order(order);
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }
    @Test
    void re() throws BzException {
        Order order = new Order();
        order.setUsername("sys");
        assertThatThrownBy(
                () -> orderService.order(order)
        ).isInstanceOf(RuntimeException.class);
        Optional<Order> findOrder = orderRepository.findById(order.getId());
        assertThat(findOrder.isEmpty()).isTrue();
    }
    @Test
    void be() throws BzException {
        Order order = new Order();
        order.setUsername("bz");
        assertThatThrownBy(
                () -> orderService.order(order)
        ).isInstanceOf(BzException.class);
        Optional<Order> findOrder = orderRepository.findById(order.getId());
        assertThat(findOrder.get().getPayStatus()).isEqualTo("대기");
    }
}
/*
2025-12-12T12:01:44.602+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.OrderService.order]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T12:01:44.603+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(588890592<open>)] for JPA transaction
2025-12-12T12:01:44.607+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@743f58c3]
2025-12-12T12:01:44.608+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.OrderService.order]
2025-12-12T12:01:44.612+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(588890592<open>)] for JPA transaction
2025-12-12T12:01:44.612+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction
2025-12-12T12:01:44.612+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-12-12T12:01:44.668+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-12-12T12:01:44.669+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.OrderService.order] after exception: spring.tx.BzException: 비즈니스 예외
2025-12-12T12:01:44.669+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T12:01:44.670+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(588890592<open>)]
2025-12-12T12:01:44.694+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(588890592<open>)] after transaction
2025-12-12T12:01:44.709+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly
2025-12-12T12:01:44.709+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(787889005<open>)] for JPA transaction
2025-12-12T12:01:44.710+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@377cc0f8]
2025-12-12T12:01:44.710+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
2025-12-12T12:01:44.751+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
2025-12-12T12:01:44.752+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T12:01:44.752+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(787889005<open>)]
2025-12-12T12:01:44.752+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(787889005<open>)] after transaction
2025-12-12T12:01:44.769+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.OrderService.order]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T12:01:44.770+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(1817055696<open>)] for JPA transaction
2025-12-12T12:01:44.771+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@29088d3d]
2025-12-12T12:01:44.771+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.OrderService.order]
2025-12-12T12:01:44.771+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(1817055696<open>)] for JPA transaction
2025-12-12T12:01:44.771+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction
2025-12-12T12:01:44.771+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-12-12T12:01:44.773+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-12-12T12:01:44.773+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.OrderService.order]
2025-12-12T12:01:44.773+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T12:01:44.773+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(1817055696<open>)]
2025-12-12T12:01:44.774+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(1817055696<open>)] after transaction
2025-12-12T12:01:44.774+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly
2025-12-12T12:01:44.775+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(1641289051<open>)] for JPA transaction
2025-12-12T12:01:44.775+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@48590849]
2025-12-12T12:01:44.775+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
2025-12-12T12:01:44.776+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
2025-12-12T12:01:44.776+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T12:01:44.776+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(1641289051<open>)]
2025-12-12T12:01:44.776+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(1641289051<open>)] after transaction
2025-12-12T12:01:44.781+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [spring.tx.OrderService.order]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
2025-12-12T12:01:44.781+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(571309751<open>)] for JPA transaction
2025-12-12T12:01:44.781+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@a165c11]
2025-12-12T12:01:44.781+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [spring.tx.OrderService.order]
2025-12-12T12:01:44.782+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Found thread-bound EntityManager [SessionImpl(571309751<open>)] for JPA transaction
2025-12-12T12:01:44.782+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Participating in existing transaction
2025-12-12T12:01:44.782+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-12-12T12:01:44.782+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.save]
2025-12-12T12:01:44.782+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [spring.tx.OrderService.order] after exception: java.lang.RuntimeException: 시스템 예외
2025-12-12T12:01:44.783+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction rollback
2025-12-12T12:01:44.783+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Rolling back JPA transaction on EntityManager [SessionImpl(571309751<open>)]
2025-12-12T12:01:44.785+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(571309751<open>)] after transaction
2025-12-12T12:01:44.785+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Creating new transaction with name [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT,readOnly
2025-12-12T12:01:44.785+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Opened new EntityManager [SessionImpl(531068784<open>)] for JPA transaction
2025-12-12T12:01:44.785+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@62f3ad90]
2025-12-12T12:01:44.786+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Getting transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
2025-12-12T12:01:44.786+09:00 TRACE 5908 --- [tx] [    Test worker] o.s.t.i.TransactionInterceptor           : Completing transaction for [org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById]
2025-12-12T12:01:44.786+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Initiating transaction commit
2025-12-12T12:01:44.786+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Committing JPA transaction on EntityManager [SessionImpl(531068784<open>)]
2025-12-12T12:01:44.788+09:00 DEBUG 5908 --- [tx] [    Test worker] o.s.orm.jpa.JpaTransactionManager        : Closing JPA EntityManager [SessionImpl(531068784<open>)] after transaction
 */