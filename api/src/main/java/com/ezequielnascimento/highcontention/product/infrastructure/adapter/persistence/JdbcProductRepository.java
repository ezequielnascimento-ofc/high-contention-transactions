package com.ezequielnascimento.highcontention.product.infrastructure.adapter.persistence;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductStatus;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcProductRepository implements ProductRepository {

    private final JdbcClient jdbcClient;

    public JdbcProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Product save(Product product) {
        if (existsById(product.id())) {
            jdbcClient.sql("""
                UPDATE products
                SET
                    name = :name,
                    description = :description,
                    price = :price,
                    status = :status,
                    updated_at = :updatedAt
                WHERE id = :id
                """)
                    .param("id", product.id().value())
                    .param("name", product.name())
                    .param("description", product.description())
                    .param("price", product.price())
                    .param("status", product.status().name())
                    .param("updatedAt", product.updatedAt())
                    .update();

            return product;
        }

        jdbcClient.sql("""
            INSERT INTO products (
                id,
                name,
                description,
                price,
                status,
                created_at,
                updated_at
            )
            VALUES (
                :id,
                :name,
                :description,
                :price,
                :status,
                :createdAt,
                :updatedAt
            )
            """)
                .param("id", product.id().value())
                .param("name", product.name())
                .param("description", product.description())
                .param("price", product.price())
                .param("status", product.status().name())
                .param("createdAt", product.createdAt())
                .param("updatedAt", product.updatedAt())
                .update();

        return product;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return jdbcClient.sql("""
                SELECT
                    id,
                    name,
                    description,
                    price,
                    status,
                    created_at,
                    updated_at
                FROM products
                WHERE id = :id
                """)
                .param("id", id.value())
                .query((rs, rowNum) -> Product.reconstitute(
                        new ProductId(UUID.fromString(rs.getString("id"))),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getString("status") != null
                                ? ProductStatus.valueOf(rs.getString("status"))
                                : null,
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant()
                ))
                .optional();
    }

    @Override
    public boolean existsById(ProductId id) {
        return jdbcClient.sql("""
                SELECT EXISTS (
                    SELECT 1
                    FROM products
                    WHERE id = :id
                )
                """)
                .param("id", id.value())
                .query(Boolean.class)
                .single();
    }

    @Override
    public void delete(Product product) {
        jdbcClient.sql("""
                DELETE FROM products
                WHERE id = :id
                """)
                .param("id", product.id().value())
                .update();
    }

    @Override
    public List<Product> findAll() {
        return jdbcClient.sql("""
            SELECT
                id,
                name,
                description,
                price,
                status,
                created_at,
                updated_at
            FROM products
            ORDER BY created_at DESC
            """)
                .query((rs, rowNum) -> Product.reconstitute(
                        new ProductId(UUID.fromString(rs.getString("id"))),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getString("status") != null
                                ? ProductStatus.valueOf(rs.getString("status"))
                                : null,
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getTimestamp("updated_at").toInstant()
                ))
                .list();
    }
}