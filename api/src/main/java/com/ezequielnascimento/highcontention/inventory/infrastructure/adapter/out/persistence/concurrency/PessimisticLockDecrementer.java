package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Locks the row with {@code SELECT ... FOR UPDATE}, checks the stock in Java
 * and writes, all in one READ COMMITTED transaction. Other writers wait on the
 * lock for the whole read-check-write, so there are no conflicts to retry,
 * but the lock is held for two round trips instead of one.
 */
public class PessimisticLockDecrementer extends AbstractStockDecrementer {

    private final TransactionTemplate transaction;

    public PessimisticLockDecrementer(JdbcClient jdbcClient, DecrementMetrics metrics, TransactionTemplate transaction) {
        super(jdbcClient, metrics, ConcurrencyStrategy.PESSIMISTIC);
        this.transaction = transaction;
    }

    @Override
    protected Inventory doDecrement(InventoryId inventoryId, int quantity) {
        countAttempt();
        return transaction.execute(status -> {
            Snapshot current = requireEnough(inventoryId, read(inventoryId, true), quantity);
            return writeQuantity(inventoryId, current.quantity() - quantity, null).orElseThrow();
        });
    }
}
