package com.ezequielnascimento.highcontention.integration.product.application;

import com.ezequielnascimento.highcontention.product.application.ChangeProductPriceService;
import com.ezequielnascimento.highcontention.product.domain.exceptions.InvalidProductException;
import com.ezequielnascimento.highcontention.product.domain.exceptions.ProductNotFoundException;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
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
class ChangeProductPriceIntegrationTest {

    @Autowired
    private ChangeProductPriceService changeProductPriceService;

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
    class SuccessfulPriceChange {

        @Test
        void shouldChangeProductPriceAndPersistChange() {
            Product product = createAndTrack(
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
            Product product = createAndTrack(
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