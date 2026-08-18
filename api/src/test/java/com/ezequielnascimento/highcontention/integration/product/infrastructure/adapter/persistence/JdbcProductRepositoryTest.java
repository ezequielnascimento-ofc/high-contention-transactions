package com.ezequielnascimento.highcontention.integration.product.infrastructure.adapter.persistence;

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
class JdbcProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    private final List<ProductId> createdProductIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (ProductId id : createdProductIds) {
            repository.findById(id).ifPresent(repository::delete);
        }
        createdProductIds.clear();
    }

    private Product createAndTrack(String name, String description, BigDecimal price) {
        Product product = Product.create(name, description, price);
        createdProductIds.add(product.id());
        return product;
    }

    @Nested
    class Save {

        @Test
        void shouldInsertNewProductAndAssignPersistedFields() {
            Product product = createAndTrack(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));

            Product saved = repository.save(product);

            Product found = repository.findById(saved.id()).orElseThrow();
            assertEquals(saved.id(), found.id());
            assertEquals(saved.name(), found.name());
            assertEquals(saved.description(), found.description());
            assertEquals(saved.price(), found.price());
            assertEquals(saved.status(), found.status());
        }

        @Test
        void shouldUpdateExistingProductWhenSavedAgain() {
            Product product = createAndTrack(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            repository.save(product);

            product.rename("Gaming Keyboard");
            product.changePrice(new BigDecimal("599.90"));
            repository.save(product);

            Product found = repository.findById(product.id()).orElseThrow();
            assertEquals("Gaming Keyboard", found.name());
            assertEquals(new BigDecimal("599.90"), found.price());
        }

        @Test
        void shouldNotDuplicateProductWhenSavedTwiceWithSameId() {
            Product product = createAndTrack(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));

            repository.save(product);
            repository.save(product);

            assertTrue(repository.existsById(product.id()));
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnProductWhenItExists() {
            Product product = createAndTrack(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            repository.save(product);

            Product found = repository.findById(product.id()).orElseThrow();

            assertEquals(product.id(), found.id());
            assertEquals(product.name(), found.name());
            assertEquals(product.description(), found.description());
            assertEquals(product.price(), found.price());
            assertEquals(product.status(), found.status());
        }

        @Test
        void shouldReturnEmptyWhenProductDoesNotExist() {
            ProductId nonExistentId = ProductId.generate();

            assertTrue(repository.findById(nonExistentId).isEmpty());
        }
    }

    @Nested
    class ExistsById {

        @Test
        void shouldReturnTrueWhenProductExists() {
            Product product = createAndTrack(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            repository.save(product);

            assertTrue(repository.existsById(product.id()));
        }

        @Test
        void shouldReturnFalseWhenProductDoesNotExist() {
            ProductId nonExistentId = ProductId.generate();

            assertFalse(repository.existsById(nonExistentId));
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldRemoveProductFromDatabase() {
            Product product = createAndTrack(
                    "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
            repository.save(product);
            assertTrue(repository.existsById(product.id()));

            repository.delete(product);

            assertFalse(repository.existsById(product.id()));
        }
    }
}