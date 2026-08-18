package com.ezequielnascimento.highcontention.integration.inventory.concurrency;

import com.ezequielnascimento.highcontention.inventory.application.DecreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DecreaseStockConcurrencyTest {

    @Autowired
    private DecreaseStockService decreaseStockService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

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

    @ParameterizedTest(name = "com {0} threads concorrentes")
    @ValueSource(ints = {10, 100, 1000, 10000})
    void shouldNeverAllowStockToGoNegativeUnderConcurrentDecrease(int threadCount) throws InterruptedException {
        int initialStock = threadCount / 2;
        setUpInventoryWithStock(initialStock);

        int poolSize = Math.min(threadCount, 500);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientStockCount = new AtomicInteger(0);
        AtomicInteger unexpectedErrorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    decreaseStockService.execute(inventory.id(), 1);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException e) {
                    insufficientStockCount.incrementAndGet();
                } catch (Exception e) {
                    unexpectedErrorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completedInTime = doneLatch.await(2, TimeUnit.MINUTES);
        executor.shutdown();

        assertTrue(completedInTime, "Nem todas as threads terminaram dentro do tempo limite");
        assertEquals(0, unexpectedErrorCount.get(),
                "Houve exceções inesperadas (não InsufficientStockException) durante a execução concorrente");

        assertEquals(initialStock, successCount.get(),
                "Número de decrementos bem-sucedidos deveria ser igual ao estoque inicial");
        assertEquals(threadCount - initialStock, insufficientStockCount.get(),
                "Número de falhas por estoque insuficiente deveria completar o total de threads");

        Inventory finalInventory = inventoryRepository.findById(inventory.id()).orElseThrow();
        assertEquals(0, finalInventory.quantity(),
                "Estoque final deveria ser exatamente zero, nunca negativo (overselling)");
    }

    private void setUpInventoryWithStock(int quantity) {
        product = Product.create(
                "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
        productRepository.save(product);

        inventory = Inventory.create(product.id(), quantity);
        inventoryRepository.save(inventory);
    }
}