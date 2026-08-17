package com.ezequielnascimento.highcontention.product.infrastructure;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbcProductRepository implements ProductRepository {

    private final JdbcClient jdbcClient;

    public JdbcProductRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Product save(Product product) {
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean existsById(ProductId id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(Product product) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
