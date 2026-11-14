package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.ConcurrencyConflictException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.shared.infrastructure.persistence.SqlStates;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Plain read-check-write inside a {@code SERIALIZABLE} transaction: PostgreSQL
 * aborts one of two conflicting transactions with SQLSTATE 40001 (or 40P01 on
 * a deadlock) instead of letting both commit. The aborted attempt is retried
 * in a new transaction after a jittered backoff.
 */
public class SerializableRetryDecrementer extends AbstractStockDecrementer {

    private final TransactionTemplate serializableTransaction;
    private final RetryPolicy retryPolicy;

    public SerializableRetryDecrementer(
            JdbcClient jdbcClient, DecrementMetrics metrics,
            TransactionTemplate serializableTransaction, RetryPolicy retryPolicy) {
        super(jdbcClient, metrics, ConcurrencyStrategy.SERIALIZABLE);
        this.serializableTransaction = serializableTransaction;
        this.retryPolicy = retryPolicy;
    }

    @Override
    protected Inventory doDecrement(InventoryId inventoryId, int quantity) {
        for (int attempt = 1; attempt <= retryPolicy.maxAttempts(); attempt++) {
            countAttempt();
            try {
                return serializableTransaction.execute(status -> {
                    Snapshot current = requireEnough(inventoryId, read(inventoryId, false), quantity);
                    return writeQuantity(inventoryId, current.quantity() - quantity, null).orElseThrow();
                });
            } catch (DataAccessException | TransactionException e) {
                // 40001 can surface on a statement or on COMMIT.
                if (!isSerializationConflict(e)) {
                    throw e;
                }
                countConflict();
                if (attempt < retryPolicy.maxAttempts()) {
                    retryPolicy.pauseAfter(attempt);
                }
            }
        }
        throw new ConcurrencyConflictException(inventoryId, retryPolicy.maxAttempts());
    }

    private static boolean isSerializationConflict(RuntimeException e) {
        String sqlState = SqlStates.of(e);
        return SqlStates.SERIALIZATION_FAILURE.equals(sqlState) || SqlStates.DEADLOCK_DETECTED.equals(sqlState);
    }
}
