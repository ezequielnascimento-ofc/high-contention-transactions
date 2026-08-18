package com.ezequielnascimento.highcontention.integration.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.DecreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InvalidInventoryException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
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
class DecreaseStockIntegrationTest {

    @Autowired
    private DecreaseStockService decreaseStockService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private final List<InventoryId> createdInventoryIds = new ArrayList<>();
    private final List<ProductId> createdProductIds = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (InventoryId id : createdInventoryIds) {
            inventoryRepository.findById(id).ifPresent(inventoryRepository::delete);
        }
        createdInventoryIds.clear();

        for (ProductId id : createdProductIds) {
            productRepository.findById(id).ifPresent(productRepository::delete);
        }
        createdProductIds.clear();
    }

    private Inventory createAndTrackInventory(int quantity) {
        Product product = Product.create(
                "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
        productRepository.save(product);
        createdProductIds.add(product.id());

        Inventory inventory = Inventory.create(product.id(), quantity);
        inventoryRepository.save(inventory);
        createdInventoryIds.add(inventory.id());

        return inventory;
    }

    @Nested
    class SuccessfulDecrease {

        @Test
        void shouldDecreaseStockAndPersistChange() {
            Inventory inventory = createAndTrackInventory(100);

            decreaseStockService.execute(inventory.id(), 30);

            Inventory updated = inventoryRepository.findById(inventory.id()).orElseThrow();
            assertEquals(70, updated.quantity());
        }

        @Test
        void shouldAllowDecreaseThatExactlyZeroesQuantity() {
            Inventory inventory = createAndTrackInventory(100);

            decreaseStockService.execute(inventory.id(), 100);

            Inventory updated = inventoryRepository.findById(inventory.id()).orElseThrow();
            assertEquals(0, updated.quantity());
        }
    }

    @Nested
    class QuantityValidation {

        @Test
        void shouldRejectZeroAsDecreaseAmount() {
            Inventory inventory = createAndTrackInventory(100);

            assertThrows(InvalidInventoryException.class,
                    () -> decreaseStockService.execute(inventory.id(), 0));
        }

        @Test
        void shouldRejectNegativeDecreaseAmount() {
            Inventory inventory = createAndTrackInventory(100);

            assertThrows(InvalidInventoryException.class,
                    () -> decreaseStockService.execute(inventory.id(), -1));
        }
    }

    @Nested
    class WhenStockIsInsufficient {

        @Test
        void shouldThrowInsufficientStockExceptionAndKeepQuantityUnchanged() {
            Inventory inventory = createAndTrackInventory(100);

            assertThrows(InsufficientStockException.class,
                    () -> decreaseStockService.execute(inventory.id(), 150));

            Inventory unchanged = inventoryRepository.findById(inventory.id()).orElseThrow();
            assertEquals(100, unchanged.quantity());
        }
    }

    @Nested
    class WhenInventoryDoesNotExist {

        @Test
        void shouldThrowInventoryNotFoundException() {
            InventoryId nonExistentId = InventoryId.generate();

            assertThrows(InventoryNotFoundException.class,
                    () -> decreaseStockService.execute(nonExistentId, 30));
        }
    }
}