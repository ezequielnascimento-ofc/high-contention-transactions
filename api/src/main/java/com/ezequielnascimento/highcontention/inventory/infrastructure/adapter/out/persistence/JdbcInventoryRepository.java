package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.persistence;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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
                    updated_at = :updatedAt
                WHERE id = :id
                """)
                    .param("id", inventory.id().value())
                    .param("productId", inventory.productId().value())
                    .param("quantity", inventory.quantity())
                    .param("updatedAt", inventory.updatedAt())
                    .update();

            return inventory;
        }

        jdbcClient.sql("""
            INSERT INTO inventory (
                id,
                product_id,
                quantity,
                created_at,
                updated_at
            )
            VALUES (
                :id,
                :productId,
                :quantity,
                :createdAt,
                :updatedAt
            )
            """)
                .param("id", inventory.id().value())
                .param("productId", inventory.productId().value())
                .param("quantity", inventory.quantity())
                .param("createdAt", inventory.createdAt())
                .param("updatedAt", inventory.updatedAt())
                .update();

        return inventory;
    }

    @Override
    public Optional<Inventory> findById(InventoryId id) {
        return jdbcClient.sql("""
                SELECT
                    id,
                    product_id,
                    quantity,
                    created_at,
                    updated_at
                FROM inventory
                WHERE id = :id
                """)
                .param("id", id.value())
                .query((rs, rowNum) -> Inventory.reconstitute(
                        new InventoryId(UUID.fromString(rs.getString("id"))),
                        new ProductId(UUID.fromString(rs.getString("product_id"))),
                        rs.getInt("quantity"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant()
                ))
                .optional();
    }

    @Override
    public Optional<Inventory> findByProductId(ProductId productId) {
        return jdbcClient.sql("""
                SELECT
                    id,
                    product_id,
                    quantity,
                    created_at,
                    updated_at
                FROM inventory
                WHERE product_id = :productId
                """)
                .param("productId", productId.value())
                .query((rs, rowNum) -> Inventory.reconstitute(
                        new InventoryId(UUID.fromString(rs.getString("id"))),
                        new ProductId(UUID.fromString(rs.getString("product_id"))),
                        rs.getInt("quantity"),
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant()
                ))
                .optional();
    }

    @Override
    public boolean existsByProductId(ProductId productId) {
        return jdbcClient.sql("""
                SELECT EXISTS (
                    SELECT 1
                    FROM inventory
                    WHERE product_id = :productId
                )
                """)
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
                    updated_at = :updatedAt
                WHERE id = :id
                """)
                .param("id", id.value())
                .param("quantity", quantity)
                .param("updatedAt", Instant.now())
                .update();

        return affectedRows > 0;
    }

    @Override
    public boolean decreaseQuantity(InventoryId id, int quantity) {
        int affectedRows = jdbcClient.sql("""
                UPDATE inventory
                SET
                    quantity = quantity - :quantity,
                    updated_at = :updatedAt
                WHERE id = :id AND quantity >= :quantity
                """)
                .param("id", id.value())
                .param("quantity", quantity)
                .param("updatedAt", Instant.now())
                .update();

        return affectedRows > 0;
    }

    @Override
    public void delete(Inventory inventory) {
        jdbcClient.sql("""
                DELETE FROM inventory
                WHERE id = :id
                """)
                .param("id", inventory.id().value())
                .update();
    }

    private boolean existsById(InventoryId id) {
        return jdbcClient.sql("""
                SELECT EXISTS (
                    SELECT 1
                    FROM inventory
                    WHERE id = :id
                )
                """)
                .param("id", id.value())
                .query(Boolean.class)
                .single();
    }
}