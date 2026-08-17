package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.persistence;


import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
                        new InventoryId(
                                UUID.fromString(rs.getString("id"))
                        ),
                        new ProductId(
                                UUID.fromString(rs.getString("product_id"))
                        ),
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
    public void delete(Inventory inventory) {
        jdbcClient.sql("""
            DELETE FROM inventory
            WHERE id = :id
            """)
                .param("id", inventory.id().value())
                .update();
    }
}
