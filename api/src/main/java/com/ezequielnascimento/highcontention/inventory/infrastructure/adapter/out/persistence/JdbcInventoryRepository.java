package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryAlreadyExistsException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.UnknownProductException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.shared.infrastructure.persistence.SqlStates;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

import static com.ezequielnascimento.highcontention.shared.infrastructure.persistence.JdbcTimestamps.utc;

/**
 * Inventory persistence. Every statement that writes a row increments its
 * {@code version}, so optimistic decrements never overwrite another write.
 * Decreases live in the {@code StockDecrementer} adapters.
 */
@Repository
public class JdbcInventoryRepository implements InventoryRepository {

    private final JdbcClient jdbcClient;

    public JdbcInventoryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Inventory save(Inventory inventory) {
        if (existsById(inventory.id())) {
            jdbcClient.sql("""
                UPDATE inventory
                SET
                    product_id = :productId,
                    quantity = :quantity,
                    version = version + 1,
                    updated_at = :updatedAt
                WHERE id = :id
                """)
                    .param("id", inventory.id().value())
                    .param("productId", inventory.productId().value())
                    .param("quantity", inventory.quantity())
                    .param("updatedAt", utc(inventory.updatedAt()))
                    .update();
            return inventory;
        }

        try {
            jdbcClient.sql("""
                INSERT INTO inventory (id, product_id, quantity, created_at, updated_at)
                VALUES (:id, :productId, :quantity, :createdAt, :updatedAt)
                """)
                    .param("id", inventory.id().value())
                    .param("productId", inventory.productId().value())
                    .param("quantity", inventory.quantity())
                    .param("createdAt", utc(inventory.createdAt()))
                    .param("updatedAt", utc(inventory.updatedAt()))
                    .update();
        } catch (DataIntegrityViolationException e) {
            throw translateInsertViolation(e, inventory);
        }
        return inventory;
    }

    @Override
    public Optional<Inventory> findById(InventoryId id) {
        return jdbcClient.sql("SELECT " + InventoryRowMapper.COLUMNS + " FROM inventory WHERE id = :id")
                .param("id", id.value())
                .query(InventoryRowMapper.INSTANCE)
                .optional();
    }

    @Override
    public Optional<Inventory> findByProductId(ProductId productId) {
        return jdbcClient.sql("SELECT " + InventoryRowMapper.COLUMNS + " FROM inventory WHERE product_id = :productId")
                .param("productId", productId.value())
                .query(InventoryRowMapper.INSTANCE)
                .optional();
    }

    @Override
    public boolean existsByProductId(ProductId productId) {
        return jdbcClient.sql("SELECT EXISTS (SELECT 1 FROM inventory WHERE product_id = :productId)")
                .param("productId", productId.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public boolean increaseQuantity(InventoryId id, int quantity) {
        int affectedRows = jdbcClient.sql("""
                UPDATE inventory
                SET
                    quantity = quantity + :quantity,
                    version = version + 1,
                    updated_at = :updatedAt
                WHERE id = :id
                """)
                .param("id", id.value())
                .param("quantity", quantity)
                .param("updatedAt", utc(Instant.now()))
                .update();
        return affectedRows > 0;
    }

    @Override
    public void delete(Inventory inventory) {
        jdbcClient.sql("DELETE FROM inventory WHERE id = :id")
                .param("id", inventory.id().value())
                .update();
    }

    private boolean existsById(InventoryId id) {
        return jdbcClient.sql("SELECT EXISTS (SELECT 1 FROM inventory WHERE id = :id)")
                .param("id", id.value())
                .query(Boolean.class)
                .single();
    }

    /**
     * Turns constraint violations on INSERT into domain exceptions, by standard
     * SQLSTATE rather than driver classes.
     */
    private static RuntimeException translateInsertViolation(DataIntegrityViolationException e, Inventory inventory) {
        String sqlState = SqlStates.of(e);
        if (SqlStates.UNIQUE_VIOLATION.equals(sqlState)) {
            return new InventoryAlreadyExistsException(inventory.productId());
        }
        if (SqlStates.FOREIGN_KEY_VIOLATION.equals(sqlState)) {
            return new UnknownProductException(inventory.productId());
        }
        return e;
    }
}
