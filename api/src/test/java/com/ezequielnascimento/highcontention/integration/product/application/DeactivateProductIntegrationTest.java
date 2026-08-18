package com.ezequielnascimento.highcontention.integration.product.application;

import com.ezequielnascimento.highcontention.product.application.DeactivateProductService;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.model.ProductStatus;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class DeactivateProductIntegrationTest {

    @Autowired
    private DeactivateProductService deactivateProductService;

    @Autowired
    private ProductRepository productRepository;

    private final List<ProductId> createdProductIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (ProductId id : createdProductIds) {
            productRepository.findById(id).ifPresent(productRepository::delete);
        }
        createdProductIds.clear();
    }

    private Product createAndTrack(String name, String description, BigDecimal price) {
        Product product = Product.create(name, description, price);
        createdProductIds.add(product.id());
        return product;
    }

    @Nested
    class SuccessfulDeactivation {

        @Test
        void shouldDeactivateActiveProductAndPersistChange() {
            Product product = createAndTrack(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            productRepository.save(product);

            deactivateProductService.execute(product.id());

            Product updated = productRepository.findById(product.id()).orElseThrow();
            assertEquals(ProductStatus.INACTIVE, updated.status());
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId nonExistentId = ProductId.generate();

            assertThrows(ProductNotFoundException.class,
                    () -> deactivateProductService.execute(nonExistentId));
        }
    }
}