package com.ezequielnascimento.highcontention.integration.product.application;

import com.ezequielnascimento.highcontention.product.application.GetProductService;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GetProductIntegrationTest {

    @Autowired
    private GetProductService getProductService;

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
    class WhenProductExists {

        @Test
        void shouldReturnPersistedProductWithAllFields() {
            Product product = createAndTrack("Keyboard", "Mechanical keyboard", new BigDecimal("499.90"));
            productRepository.save(product);

            Product result = getProductService.execute(product.id());

            assertNotNull(result);
            assertEquals(product.id(), result.id());
            assertEquals(product.name(), result.name());
            assertEquals(product.description(), result.description());
            assertEquals(product.price(), result.price());
            assertEquals(product.status(), result.status());
        }
    }

    @Nested
    class WhenProductDoesNotExist {

        @Test
        void shouldThrowProductNotFoundException() {
            ProductId nonExistentId = ProductId.generate();

            assertThrows(ProductNotFoundException.class, () -> getProductService.execute(nonExistentId));
        }
    }
}