package com.ezequielnascimento.highcontention.integration.product.application;

import com.ezequielnascimento.highcontention.product.application.ChangeProductPriceService;
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
class ChangeProductPriceIntegrationTest {

    @Autowired
    private ChangeProductPriceService changeProductPriceService;

    @Autowired
    private ProductRepository productRepository;

    @Nested
    class SuccessfulPriceChange {

        @Test
        void shouldChangeProductPriceAndPersistChange() {
            Product product = Product.create(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            productRepository.save(product);

            changeProductPriceService.execute(product.id(), new BigDecimal("599.90"));

            Product updated = productRepository.findById(product.id()).orElseThrow();
            assertEquals(new BigDecimal("599.90"), updated.price());
        }
    }

    @Nested
    class PriceValidation {

        @Test
        void shouldRejectNegativePrice() {
            Product product = Product.create(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            productRepository.save(product);

            assertThrows(InvalidProductException.class,
                    () -> changeProductPriceService.execute(product.id(), new BigDecimal("-50.00")));
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId nonExistentId = ProductId.generate();

            assertThrows(ProductNotFoundException.class,
                    () -> changeProductPriceService.execute(nonExistentId, new BigDecimal("599.90")));
        }
    }
}