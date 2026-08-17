package com.ezequielnascimento.highcontention.integration.inventory.application;

import com.ezequielnascimento.highcontention.inventory.application.CreateInventoryService;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class CreateInventoryIntegrationTest {


    @Autowired
    private CreateInventoryService createInventory;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldCreateAndPersistInventory() {
        Product product = Product.create(
                "Mechanical Keyboard",
                "High-performance mechanical keyboard",
                new BigDecimal("499.90")
        );

        productRepository.save(product);

        Inventory result = createInventory.execute(
                product.id(),
                100
        );

        assertNotNull(result);
        assertEquals(product.id(), result.productId());
        assertEquals(100, result.quantity());

        Inventory persisted = inventoryRepository
                .findById(result.id())
                .orElseThrow();

        assertEquals(result.id(), persisted.id());
        assertEquals(product.id(), persisted.productId());
        assertEquals(100, persisted.quantity());
    }
}
