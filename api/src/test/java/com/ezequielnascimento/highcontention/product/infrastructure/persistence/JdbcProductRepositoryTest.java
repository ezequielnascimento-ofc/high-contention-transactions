package com.ezequielnascimento.highcontention.product.infrastructure.persistence;

import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest
class JdbcProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void shouldSaveProduct() {
        Product product = Product.create(
                "Mechanical Keyboard",
                "High-performance mechanical keyboard",
                new BigDecimal("499.90")
        );

        Product saved = repository.save(product);
        assertEquals(product.id(), saved.id());
    }

    @Test
    void shouldFindProductById() {
        Product product = Product.create(
                "Mechanical Keyboard",
                "High-performance mechanical keyboard",
                new BigDecimal("499.90")
        );

        repository.save(product);

        Product found = repository.findById(product.id())
                .orElseThrow();

        assertEquals(product.id(), found.id());
        assertEquals(product.name(), found.name());
        assertEquals(product.description(), found.description());
        assertEquals(product.price(), found.price());
        assertEquals(product.status(), found.status());
    }
}
