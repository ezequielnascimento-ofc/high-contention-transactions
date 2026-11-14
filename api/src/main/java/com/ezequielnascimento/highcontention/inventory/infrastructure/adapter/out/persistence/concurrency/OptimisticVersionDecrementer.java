package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.ConcurrencyConflictException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;

/**
 * Reads quantity and version without locking, then writes only if the version
 * is still the same ({@code UPDATE ... WHERE version = :read}). A concurrent
 * write makes the update touch no row; the attempt is retried after a jittered
 * backoff, up to {@link RetryPolicy#maxAttempts()}.
 */
public class OptimisticVersionDecrementer extends AbstractStockDecrementer {

    private final RetryPolicy retryPolicy;

    public OptimisticVersionDecrementer(JdbcClient jdbcClient, DecrementMetrics metrics, RetryPolicy retryPolicy) {
        super(jdbcClient, metrics, ConcurrencyStrategy.OPTIMISTIC);
        this.retryPolicy = retryPolicy;
    }

    @Override
    protected Inventory doDecrement(InventoryId inventoryId, int quantity) {
        for (int attempt = 1; attempt <= retryPolicy.maxAttempts(); attempt++) {
            countAttempt();
            Snapshot current = requireEnough(inventoryId, read(inventoryId, false), quantity);
            Optional<Inventory> written = writeQuantity(inventoryId, current.quantity() - quantity, current.version());
            if (written.isPresent()) {
                return written.get();
            }
            countConflict();
            if (attempt < retryPolicy.maxAttempts()) {
                retryPolicy.pauseAfter(attempt);
            }
        }
        throw new ConcurrencyConflictException(inventoryId, retryPolicy.maxAttempts());
    }
}
