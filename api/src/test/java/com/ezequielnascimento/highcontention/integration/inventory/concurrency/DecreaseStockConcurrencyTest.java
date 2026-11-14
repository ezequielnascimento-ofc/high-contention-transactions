package com.ezequielnascimento.highcontention.integration.inventory.concurrency;

import com.ezequielnascimento.highcontention.inventory.application.DecreaseStockService;
import com.ezequielnascimento.highcontention.inventory.application.IncreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import com.ezequielnascimento.highcontention.support.IntegrationTest;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrency invariants on a real PostgreSQL.
 * <p>
 * Every task is submitted at once behind a start latch, but the real
 * parallelism is bounded twice: by the thread pool ({@value #MAX_THREADS}
 * threads at most) and, at the database, by the connection pool size, which
 * is logged on every run.
 */
@IntegrationTest
class DecreaseStockConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(DecreaseStockConcurrencyTest.class);

    static final int MAX_THREADS = 500;
    private static final long TIMEOUT_MINUTES = 2;

    @Autowired
    private DecreaseStockService decreaseStockService;
    @Autowired
    private IncreaseStockService increaseStockService;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DataSource dataSource;

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

    @ParameterizedTest(name = "{0} decrement tasks competing for half as much stock")
    @ValueSource(ints = {10, 100, 1000, 10000})
    void shouldNeverOversellUnderConcurrentDecreases(int tasks) throws Exception {
        int initialStock = tasks / 2;
        setUpInventoryWithStock(initialStock);
        AtomicInteger successful = new AtomicInteger();
        AtomicInteger insufficientStock = new AtomicInteger();

        int unexpected = runConcurrently(tasks, index -> {
            try {
                decreaseStockService.execute(inventory.id(), 1);
                successful.incrementAndGet();
            } catch (InsufficientStockException e) {
                insufficientStock.incrementAndGet();
            }
        });

        assertEquals(0, unexpected, "Unexpected exceptions (other than InsufficientStockException)");
        assertEquals(initialStock, successful.get(), "Successful decrements must equal the initial stock");
        assertEquals(tasks - initialStock, insufficientStock.get(),
                "Insufficient-stock failures must account for every remaining task");
        assertEquals(0, currentStock(), "Final stock must be exactly zero, never negative (no overselling)");
    }

    @ParameterizedTest(name = "{0} interleaved increase and decrease tasks")
    @ValueSource(ints = {100, 1000, 10000})
    void shouldLoseNoUpdateUnderConcurrentIncreasesAndDecreases(int tasks) throws Exception {
        int initialStock = tasks / 4;
        int increaseBy = 1;
        int decreaseBy = 2;
        setUpInventoryWithStock(initialStock);
        AtomicInteger increases = new AtomicInteger();
        AtomicInteger successfulDecreases = new AtomicInteger();

        int unexpected = runConcurrently(tasks, index -> {
            if (index % 2 == 0) {
                increaseStockService.execute(inventory.id(), increaseBy);
                increases.incrementAndGet();
            } else {
                try {
                    decreaseStockService.execute(inventory.id(), decreaseBy);
                    successfulDecreases.incrementAndGet();
                } catch (InsufficientStockException e) {
                    // expected when the stock runs out; the invariant below must still hold
                }
            }
        });

        int expected = initialStock + increases.get() * increaseBy - successfulDecreases.get() * decreaseBy;
        assertEquals(0, unexpected, "Unexpected exceptions (other than InsufficientStockException)");
        assertEquals(tasks / 2, increases.get(), "Every increase must succeed");
        assertEquals(expected, currentStock(),
                "Final stock must equal initial + increases - successful decreases (no lost update)");
    }

    /**
     * Runs {@code tasks} invocations of {@code task}, released together by a
     * start latch, and returns how many threw an unexpected exception.
     */
    private int runConcurrently(int tasks, IntConsumer task) throws InterruptedException, SQLException {
        int threads = Math.min(tasks, MAX_THREADS);
        log.info("Running {} tasks on {} threads against a connection pool of {}",
                tasks, threads, connectionPoolSize());

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(tasks);
        AtomicInteger unexpected = new AtomicInteger();

        for (int i = 0; i < tasks; i++) {
            int index = i;
            executor.submit(() -> {
                try {
                    start.await();
                    task.accept(index);
                } catch (Exception e) {
                    unexpected.incrementAndGet();
                    log.warn("Unexpected exception in task {}", index, e);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        boolean finished = done.await(TIMEOUT_MINUTES, TimeUnit.MINUTES);
        executor.shutdownNow();
        assertTrue(finished, "Not all tasks finished within " + TIMEOUT_MINUTES + " minutes");
        return unexpected.get();
    }

    private int connectionPoolSize() throws SQLException {
        return dataSource.unwrap(HikariDataSource.class).getMaximumPoolSize();
    }

    private int currentStock() {
        return inventoryRepository.findById(inventory.id()).orElseThrow().quantity();
    }

    private void setUpInventoryWithStock(int quantity) {
        product = Product.create(
                "Mechanical Keyboard", "High-performance mechanical keyboard", new BigDecimal("499.90"));
        productRepository.save(product);
        inventory = Inventory.create(product.id(), quantity);
        inventoryRepository.save(inventory);
    }
}
