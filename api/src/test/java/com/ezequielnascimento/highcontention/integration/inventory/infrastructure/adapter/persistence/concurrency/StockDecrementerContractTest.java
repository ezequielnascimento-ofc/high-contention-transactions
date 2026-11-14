package com.ezequielnascimento.highcontention.integration.inventory.infrastructure.adapter.persistence.concurrency;

import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryNotFoundException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.StockDecrementer;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.ConcurrencyStrategy;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.StockDecrementers;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import com.ezequielnascimento.highcontention.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The same sequential behaviour for every strategy. Concurrency is covered by
 * DecreaseStockConcurrencyTest.
 */
@IntegrationTest
class StockDecrementerContractTest {

    @Autowired
    private StockDecrementers decrementers;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JdbcClient jdbcClient;

    private Inventory inventory;
    private Product product;

    @AfterEach
    void cleanUp() {
        if (inventory != null) {
            inventoryRepository.findById(inventory.id()).ifPresent(inventoryRepository::delete);
        }
        if (product != null) {
            productRepository.findById(product.id()).ifPresent(productRepository::delete);
        }
    }

    @ParameterizedTest
    @EnumSource(ConcurrencyStrategy.class)
    void decreasesAndReturnsTheCommittedRow(ConcurrencyStrategy strategy) {
        createInventoryWithStock(100);

        Inventory updated = decrementer(strategy).decrement(inventory.id(), 30);

        assertEquals(70, updated.quantity());
        assertEquals(70, storedQuantity());
    }

    @ParameterizedTest
    @EnumSource(ConcurrencyStrategy.class)
    void allowsADecreaseThatEmptiesTheStock(ConcurrencyStrategy strategy) {
        createInventoryWithStock(100);

        assertEquals(0, decrementer(strategy).decrement(inventory.id(), 100).quantity());
    }

    @ParameterizedTest
    @EnumSource(ConcurrencyStrategy.class)
    void refusesToOversellAndLeavesTheStockUnchanged(ConcurrencyStrategy strategy) {
        createInventoryWithStock(100);

        assertThrows(InsufficientStockException.class, () -> decrementer(strategy).decrement(inventory.id(), 150));

        assertEquals(100, storedQuantity());
        assertEquals(0, storedVersion(), "A refused decrease must not write the row");
    }

    @ParameterizedTest
    @EnumSource(ConcurrencyStrategy.class)
    void reportsAMissingInventory(ConcurrencyStrategy strategy) {
        assertThrows(InventoryNotFoundException.class,
                () -> decrementer(strategy).decrement(InventoryId.generate(), 1));
    }

    @ParameterizedTest
    @EnumSource(ConcurrencyStrategy.class)
    void incrementsTheRowVersionOnEveryWrite(ConcurrencyStrategy strategy) {
        createInventoryWithStock(100);

        decrementer(strategy).decrement(inventory.id(), 1);
        inventoryRepository.increaseQuantity(inventory.id(), 5);
        decrementer(strategy).decrement(inventory.id(), 1);

        assertEquals(3, storedVersion(), "Decreases and increases must all bump the version");
        assertEquals(103, storedQuantity());
    }

    private StockDecrementer decrementer(ConcurrencyStrategy strategy) {
        return decrementers.get(strategy);
    }

    private void createInventoryWithStock(int quantity) {
        product = Product.create("Mechanical Keyboard", "Contract test", new BigDecimal("499.90"));
        productRepository.save(product);
        inventory = Inventory.create(product.id(), quantity);
        inventoryRepository.save(inventory);
    }

    private int storedQuantity() {
        return inventoryRepository.findById(inventory.id()).orElseThrow().quantity();
    }

    private long storedVersion() {
        return jdbcClient.sql("SELECT version FROM inventory WHERE id = :id")
                .param("id", inventory.id().value())
                .query(Long.class)
                .single();
    }
}
