package com.ezequielnascimento.highcontention.integration.inventory.application;

import com.ezequielnascimento.highcontention.support.IntegrationTest;

import com.ezequielnascimento.highcontention.inventory.application.CreateInventoryService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InventoryAlreadyExistsException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.UnknownProductException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.model.InventoryId;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.model.ProductId;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
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

    @Test
    void shouldRejectASecondInventoryForTheSameProduct() {
        Product product = createAndTrackProduct();
        createdInventoryIds.add(createInventoryService.execute(product.id(), 10).id());

        assertThrows(InventoryAlreadyExistsException.class,
                () -> createInventoryService.execute(product.id(), 5));
    }

    @Test
    void shouldRejectAnInventoryForAProductThatDoesNotExist() {
        assertThrows(UnknownProductException.class,
                () -> createInventoryService.execute(ProductId.generate(), 10));
    }

    @Test
    void shouldLetExactlyOneOfManyConcurrentCreationsWin() throws Exception {
        Product product = createAndTrackProduct();
        int attempts = 20;
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Inventory>> results = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            results.add(executor.submit(() -> {
                start.await();
                return createInventoryService.execute(product.id(), 10);
            }));
        }
        start.countDown();

        int created = 0;
        int rejected = 0;
        for (Future<Inventory> result : results) {
            try {
                createdInventoryIds.add(result.get(30, TimeUnit.SECONDS).id());
                created++;
            } catch (java.util.concurrent.ExecutionException e) {
                assertTrue(e.getCause() instanceof InventoryAlreadyExistsException,
                        "Losers must get InventoryAlreadyExistsException, got " + e.getCause());
                rejected++;
            }
        }
        executor.shutdownNow();

        assertEquals(1, created, "Exactly one creation must succeed");
        assertEquals(attempts - 1, rejected, "Every other creation must be rejected");
    }
}
