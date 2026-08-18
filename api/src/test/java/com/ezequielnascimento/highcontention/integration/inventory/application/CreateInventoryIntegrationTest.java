package com.ezequielnascimento.highcontention.integration.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.CreateInventoryService;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class CreateInventoryIntegrationTest {

    @Autowired
    private CreateInventoryService createInventoryService;

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

    private Product createAndTrackProduct() {
        Product product = Product.create(
                "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
        productRepository.save(product);
        createdProductIds.add(product.id());
        return product;
    }

    @Test
    void shouldCreateInventoryAndPersistItInDatabase() {
        Product product = createAndTrackProduct();

        Inventory result = createInventoryService.execute(product.id(), 100);
        createdInventoryIds.add(result.id());

        assertNotNull(result);
        assertEquals(product.id(), result.productId());
        assertEquals(100, result.quantity());

        Inventory persisted = inventoryRepository.findById(result.id()).orElseThrow();
        assertEquals(result.id(), persisted.id());
        assertEquals(product.id(), persisted.productId());
        assertEquals(100, persisted.quantity());
    }
}