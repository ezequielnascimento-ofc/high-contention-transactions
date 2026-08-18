package com.ezequielnascimento.highcontention.integration.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.GetInventoryService;
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
class GetInventoryIntegrationTest {

    @Autowired
    private GetInventoryService getInventoryService;

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
    class WhenInventoryExists {

        @Test
        void shouldReturnPersistedInventoryWithAllFields() {
            Inventory inventory = createAndTrackInventory(100);

            Inventory result = getInventoryService.execute(inventory.id());

            assertEquals(inventory.id(), result.id());
            assertEquals(inventory.productId(), result.productId());
            assertEquals(100, result.quantity());
        }
    }

    @Nested
    class WhenInventoryDoesNotExist {

        @Test
        void shouldThrowInventoryNotFoundException() {
            InventoryId nonExistentId = InventoryId.generate();

            assertThrows(InventoryNotFoundException.class,
                    () -> getInventoryService.execute(nonExistentId));
        }
    }
}