package com.ezequielnascimento.highcontention.integration.product.application;

import com.ezequielnascimento.highcontention.product.application.ActivateProductService;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductStatus;
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
class ActivateProductIntegrationTest {

    @Autowired
    private ActivateProductService activateProductService;

    @Autowired
    private ProductRepository productRepository;

    @Nested
    class SuccessfulActivation {

        @Test
        void shouldActivateInactiveProductAndPersistChange() {
            Product product = Product.create(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            product.deactivate();
            productRepository.save(product);

            activateProductService.execute(product.id());

            Product updated = productRepository.findById(product.id()).orElseThrow();
            assertEquals(ProductStatus.ACTIVE, updated.status());
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId nonExistentId = ProductId.generate();

            assertThrows(ProductNotFoundException.class,
                    () -> activateProductService.execute(nonExistentId));
        }
    }
}