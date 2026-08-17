package com.ezequielnascimento.highcontention.integration.product.application;

import com.ezequielnascimento.highcontention.product.application.ChangeProductDescriptionService;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChangeProductDescriptionIntegrationTest {

    @Autowired
    private ChangeProductDescriptionService changeProductDescriptionService;

    @Autowired
    private ProductRepository productRepository;

    @Nested
    class SuccessfulDescriptionChange {

        @Test
        void shouldChangeProductDescriptionAndPersistChange() {
            Product product = Product.create(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            productRepository.save(product);

            changeProductDescriptionService.execute(product.id(), "Gaming mechanical keyboard");

            Product updated = productRepository.findById(product.id()).orElseThrow();
            assertEquals("Gaming mechanical keyboard", updated.description());
        }

        @Test
        void shouldAllowChangingDescriptionToNull() {
            Product product = Product.create(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            productRepository.save(product);

            changeProductDescriptionService.execute(product.id(), null);

            Product updated = productRepository.findById(product.id()).orElseThrow();
            assertNull(updated.description());
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId nonExistentId = ProductId.generate();

            assertThrows(ProductNotFoundException.class,
                    () -> changeProductDescriptionService.execute(nonExistentId, "Gaming mechanical keyboard"));
        }
    }
}