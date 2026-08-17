package com.ezequielnascimento.highcontention.integration.product.application;

import com.ezequielnascimento.highcontention.product.application.RenameProductService;
import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RenameProductIntegrationTest {

    @Autowired
    private RenameProductService renameProductService;

    @Autowired
    private ProductRepository productRepository;

    @Nested
    class SuccessfulRename {

        @Test
        void shouldRenameProductAndPersistChange() {
            Product product = Product.create(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            productRepository.save(product);

            renameProductService.execute(product.id(), "Gaming Keyboard");

            Product updated = productRepository.findById(product.id()).orElseThrow();
            assertEquals("Gaming Keyboard", updated.name());
        }
    }

    @Nested
    class RenameValidation {

        @Test
        void shouldRejectBlankName() {
            Product product = Product.create(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            productRepository.save(product);

            assertThrows(InvalidProductException.class,
                    () -> renameProductService.execute(product.id(), " "));
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId nonExistentId = ProductId.generate();

            assertThrows(ProductNotFoundException.class,
                    () -> renameProductService.execute(nonExistentId, "Gaming Keyboard"));
        }
    }
}