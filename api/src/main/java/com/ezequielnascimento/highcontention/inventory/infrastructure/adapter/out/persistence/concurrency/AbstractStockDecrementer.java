package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.ConcurrencyConflictException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.StockDecrementer;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.InventoryRowMapper;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.DecrementMetrics.Outcome;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Instant;
import java.util.Optional;

import static com.ezequielnascimento.highcontention.shared.infrastructure.persistence.JdbcTimestamps.utc;

/** Shared SQL, metrics and outcome bookkeeping for the four strategies. */
public abstract class AbstractStockDecrementer implements StockDecrementer {

    /** A read of the current row: quantity and version. */
    record Snapshot(int quantity, long version) {
    }

    protected final JdbcClient jdbcClient;
    private final DecrementMetrics metrics;
    private final ConcurrencyStrategy strategy;

    AbstractStockDecrementer(JdbcClient jdbcClient, DecrementMetrics metrics, ConcurrencyStrategy strategy) {
        this.jdbcClient = jdbcClient;
        this.metrics = metrics;
        this.strategy = strategy;
    }

    public ConcurrencyStrategy strategy() {
        return strategy;
    }

    @Override
    public final Inventory decrement(InventoryId inventoryId, int quantity) {
        try {
            Inventory updated = doDecrement(inventoryId, quantity);
            metrics.outcome(strategy, Outcome.SUCCESS);
            return updated;
        } catch (InsufficientStockException e) {
            metrics.outcome(strategy, Outcome.INSUFFICIENT_STOCK);
            throw e;
        } catch (InventoryNotFoundException e) {
            metrics.outcome(strategy, Outcome.NOT_FOUND);
            throw e;
        } catch (ConcurrencyConflictException e) {
            metrics.outcome(strategy, Outcome.GAVE_UP);
            throw e;
        }
    }

    protected abstract Inventory doDecrement(InventoryId inventoryId, int quantity);

    protected void countAttempt() {
        metrics.attempt(strategy);
    }

    protected void countConflict() {
        metrics.conflict(strategy);
    }

    protected Optional<Snapshot> read(InventoryId id, boolean forUpdate) {
        return jdbcClient.sql("SELECT quantity, version FROM inventory WHERE id = :id" + (forUpdate ? " FOR UPDATE" : ""))
                .param("id", id.value())
                .query((rs, rowNum) -> new Snapshot(rs.getInt("quantity"), rs.getLong("version")))
                .optional();
    }

    /** Fails with the right exception when a snapshot cannot be decreased by {@code quantity}. */
    protected static Snapshot requireEnough(InventoryId id, Optional<Snapshot> snapshot, int quantity) {
        Snapshot current = snapshot.orElseThrow(() -> new InventoryNotFoundException(id));
        if (current.quantity() < quantity) {
            throw new InsufficientStockException(id, current.quantity(), quantity);
        }
        return current;
    }

    /** Writes an absolute quantity, optionally only if the version is unchanged. */
    protected Optional<Inventory> writeQuantity(InventoryId id, int newQuantity, Long expectedVersion) {
        String guard = expectedVersion == null ? "" : " AND version = :version";
        var statement = jdbcClient.sql("""
                UPDATE inventory
                SET quantity = :quantity, version = version + 1, updated_at = :updatedAt
                WHERE id = :id""" + guard + "\nRETURNING " + InventoryRowMapper.COLUMNS)
                .param("id", id.value())
                .param("quantity", newQuantity)
                .param("updatedAt", utc(Instant.now()));
        if (expectedVersion != null) {
            statement = statement.param("version", expectedVersion);
        }
        return statement.query(InventoryRowMapper.INSTANCE).optional();
    }
}
