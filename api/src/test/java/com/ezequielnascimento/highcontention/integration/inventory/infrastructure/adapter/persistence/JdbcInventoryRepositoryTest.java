package com.ezequielnascimento.highcontention.integration.inventory.infrastructure.adapter.persistence;

import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JdbcInventoryRepositoryTest {

    @Autowired
    private InventoryRepository repository;

    @Autowired
    private ProductRepository productRepository;

    private final List<InventoryId> createdInventoryIds = new ArrayList<>();
    private final List<ProductId> createdProductIds = new ArrayList<>();

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.create(
                "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
        productRepository.save(product);
        createdProductIds.add(product.id());
    }

    @AfterEach
    void cleanUp() {
        for (InventoryId id : createdInventoryIds) {
            repository.findById(id).ifPresent(repository::delete);
        }
        createdInventoryIds.clear();

        for (ProductId id : createdProductIds) {
            productRepository.findById(id).ifPresent(productRepository::delete);
        }
        createdProductIds.clear();
    }

    private Inventory createAndTrack(int quantity) {
        Inventory inventory = Inventory.create(product.id(), quantity);
        createdInventoryIds.add(inventory.id());
        return inventory;
    }

    @Nested
    class Save {

        @Test
        void shouldInsertNewInventoryAndAssignPersistedFields() {
            Inventory inventory = createAndTrack(100);

            Inventory saved = repository.save(inventory);

            Inventory found = repository.findById(saved.id()).orElseThrow();
            assertEquals(saved.id(), found.id());
            assertEquals(saved.productId(), found.productId());
            assertEquals(saved.quantity(), found.quantity());
            assertEquals(saved.createdAt(), found.createdAt());
            assertEquals(saved.updatedAt(), found.updatedAt());
        }

        @Test
        void shouldUpdateExistingInventoryWhenSavedAgain() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            inventory.increase(50);
            repository.save(inventory);

            Inventory found = repository.findById(inventory.id()).orElseThrow();
            assertEquals(150, found.quantity());
        }

        @Test
        void shouldNotDuplicateInventoryWhenSavedTwiceWithSameId() {
            Inventory inventory = createAndTrack(100);

            repository.save(inventory);
            repository.save(inventory);

            assertTrue(repository.findById(inventory.id()).isPresent());
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnInventoryWhenItExists() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            Optional<Inventory> result = repository.findById(inventory.id());

            assertTrue(result.isPresent());
            Inventory found = result.get();
            assertEquals(inventory.id(), found.id());
            assertEquals(inventory.productId(), found.productId());
            assertEquals(inventory.quantity(), found.quantity());
            assertEquals(inventory.createdAt(), found.createdAt());
            assertEquals(inventory.updatedAt(), found.updatedAt());
        }

        @Test
        void shouldReturnEmptyWhenInventoryDoesNotExist() {
            InventoryId nonExistentId = InventoryId.generate();

            assertTrue(repository.findById(nonExistentId).isEmpty());
        }
    }

    @Nested
    class FindByProductId {

        @Test
        void shouldReturnInventoryWhenItExistsForProduct() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            Optional<Inventory> result = repository.findByProductId(product.id());

            assertTrue(result.isPresent());
            Inventory found = result.get();
            assertEquals(inventory.id(), found.id());
            assertEquals(inventory.productId(), found.productId());
            assertEquals(inventory.quantity(), found.quantity());
        }

        @Test
        void shouldReturnEmptyWhenInventoryDoesNotExistForProduct() {
            ProductId nonExistentProductId = ProductId.generate();

            assertTrue(repository.findByProductId(nonExistentProductId).isEmpty());
        }
    }

    @Nested
    class ExistsByProductId {

        @Test
        void shouldReturnTrueWhenInventoryExistsForProduct() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            assertTrue(repository.existsByProductId(product.id()));
        }

        @Test
        void shouldReturnFalseWhenInventoryDoesNotExistForProduct() {
            ProductId nonExistentProductId = ProductId.generate();

            assertFalse(repository.existsByProductId(nonExistentProductId));
        }
    }

    @Nested
    class IncreaseQuantity {

        @Test
        void shouldIncreaseQuantityAndReturnTrueWhenInventoryExists() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            boolean increased = repository.increaseQuantity(inventory.id(), 50);

            assertTrue(increased);
            Inventory found = repository.findById(inventory.id()).orElseThrow();
            assertEquals(150, found.quantity());
        }

        @Test
        void shouldReturnFalseWhenInventoryDoesNotExist() {
            InventoryId nonExistentId = InventoryId.generate();

            boolean increased = repository.increaseQuantity(nonExistentId, 50);

            assertFalse(increased);
        }

        @Test
        void shouldAccumulateMultipleSequentialIncreasesCorrectly() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            repository.increaseQuantity(inventory.id(), 10);
            repository.increaseQuantity(inventory.id(), 20);
            repository.increaseQuantity(inventory.id(), 30);

            Inventory found = repository.findById(inventory.id()).orElseThrow();
            assertEquals(160, found.quantity());
        }
    }

    @Nested
    class DecreaseQuantity {

        @Test
        void shouldDecreaseQuantityAndReturnTrueWhenStockIsSufficient() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            boolean decreased = repository.decreaseQuantity(inventory.id(), 30);

            assertTrue(decreased);
            Inventory found = repository.findById(inventory.id()).orElseThrow();
            assertEquals(70, found.quantity());
        }

        @Test
        void shouldReturnFalseAndKeepQuantityUnchangedWhenStockIsInsufficient() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            boolean decreased = repository.decreaseQuantity(inventory.id(), 150);

            assertFalse(decreased);
            Inventory found = repository.findById(inventory.id()).orElseThrow();
            assertEquals(100, found.quantity());
        }

        @Test
        void shouldReturnFalseWhenInventoryDoesNotExist() {
            InventoryId nonExistentId = InventoryId.generate();

            boolean decreased = repository.decreaseQuantity(nonExistentId, 10);

            assertFalse(decreased);
        }

        @Test
        void shouldAllowDecreaseThatExactlyZeroesQuantity() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);

            boolean decreased = repository.decreaseQuantity(inventory.id(), 100);

            assertTrue(decreased);
            Inventory found = repository.findById(inventory.id()).orElseThrow();
            assertEquals(0, found.quantity());
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldRemoveInventoryFromDatabase() {
            Inventory inventory = createAndTrack(100);
            repository.save(inventory);
            assertTrue(repository.findById(inventory.id()).isPresent());

            repository.delete(inventory);

            assertTrue(repository.findById(inventory.id()).isEmpty());
        }
    }
}