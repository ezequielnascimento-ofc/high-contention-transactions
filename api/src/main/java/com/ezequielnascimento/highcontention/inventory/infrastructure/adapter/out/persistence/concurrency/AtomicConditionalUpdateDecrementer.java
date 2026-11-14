package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.InventoryRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.util.Optional;

import static com.ezequielnascimento.highcontention.shared.infrastructure.persistence.JdbcTimestamps.utc;

/**
 * The check and the write are one statement, so PostgreSQL evaluates the
 * condition against the latest committed row while holding its lock:
 * concurrent decreases serialise on the row for one statement each and can
 * never oversell. There are no conflicts to retry and no transaction beyond
 * the statement.
 */
public class AtomicConditionalUpdateDecrementer extends AbstractStockDecrementer {

    public AtomicConditionalUpdateDecrementer(JdbcClient jdbcClient, DecrementMetrics metrics) {
        super(jdbcClient, metrics, ConcurrencyStrategy.ATOMIC);
    }

    @Override
    protected Inventory doDecrement(InventoryId inventoryId, int quantity) {
        while (true) {
            countAttempt();
            Optional<Inventory> updated = jdbcClient.sql("""
                    UPDATE inventory
                    SET quantity = quantity - :quantity, version = version + 1, updated_at = :updatedAt
                    WHERE id = :id AND quantity >= :quantity
                    RETURNING""" + " " + InventoryRowMapper.COLUMNS)
                    .param("id", inventoryId.value())
                    .param("quantity", quantity)
                    .param("updatedAt", utc(Instant.now()))
                    .query(InventoryRowMapper.INSTANCE)
                    .optional();
            if (updated.isPresent()) {
                return updated.get();
            }
            // No row changed: missing or short of stock. If the read shows enough stock,
            // it was restocked between the two statements, so the update is tried again.
            requireEnough(inventoryId, read(inventoryId, false), quantity);
        }
    }
}
