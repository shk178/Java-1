package hello.jdbc.transaction2;

import org.springframework.lang.Nullable;
import org.springframework.transaction.*;

// PlatformTransactionManager
public interface TxManager extends TransactionManager {
    /**
     * @see TransactionDefinition#getPropagationBehavior
     * @see TransactionDefinition#getIsolationLevel
     * @see TransactionDefinition#getTimeout
     * @see TransactionDefinition#isReadOnly
     */
    TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException;

    /**
     * @see TransactionStatus#setRollbackOnly
     */
    void commit(TransactionStatus status) throws TransactionException;

    void rollback(TransactionStatus status) throws TransactionException;
}