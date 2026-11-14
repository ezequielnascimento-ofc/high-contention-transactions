package com.ezequielnascimento.highcontention.integration.inventory.concurrency;

import com.ezequielnascimento.highcontention.inventory.application.DecreaseStockService;
import com.ezequielnascimento.highcontention.inventory.application.IncreaseStockService;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.ConcurrencyConflictException;
import com.ezequielnascimento.highcontention.inventory.domain.exceptions.InsufficientStockException;
import com.ezequielnascimento.highcontention.inventory.domain.model.Inventory;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryRepository;
import com.ezequielnascimento.highcontention.inventory.domain.port.out.InventoryStockNotifier;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.ConcurrencyStrategy;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.StockDecrementers;
import com.ezequielnascimento.highcontention.product.domain.model.Product;
import com.ezequielnascimento.highcontention.product.domain.port.out.ProductRepository;
import com.ezequielnascimento.highcontention.support.IntegrationTest;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Concurrency invariants on a real PostgreSQL, for every strategy.
 * <p>
 * Safety must hold for all of them: the stock never goes negative and no
 * update is lost. Liveness differs: atomic and pessimistic never give up,
 * while optimistic and serializable may refuse a decrease with
 * {@link ConcurrencyConflictException} after their bounded retries.
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
    private static final long TIMEOUT_MINUTES = 3;

    @Autowired
    private StockDecrementers decrementers;
    @Autowired
    private InventoryStockNotifier inventoryStockNotifier;
    @Autowired
    private IncreaseStockService increaseStockService;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private MeterRegistry meterRegistry;

    private Inventory inventory;
    private Product product;

    static Stream<Arguments> everyStrategyWith(int... taskCounts) {
        return Arrays.stream(ConcurrencyStrategy.values())
                .flatMap(strategy -> IntStream.of(taskCounts).mapToObj(tasks -> Arguments.of(strategy, tasks)));
    }

    static Stream<Arguments> oversellRuns() {
        return everyStrategyWith(10, 100, 1000, 10000);
    }

    static Stream<Arguments> lostUpdateRuns() {
        return everyStrategyWith(100, 1000, 10000);
    }

    @AfterEach
    void cleanUp() {
        if (inventory != null) {
            inventoryRepository.findById(inventory.id()).ifPresent(inventoryRepository::delete);
        }
        if (product != null) {
            productRepository.findById(product.id()).ifPresent(productRepository::delete);
        }
    }

    @ParameterizedTest(name = "{0}: {1} decrement tasks competing for half as much stock")
    @MethodSource("oversellRuns")
    void shouldNeverOversell(ConcurrencyStrategy strategy, int tasks) throws Exception {
        int initialStock = tasks / 2;
        setUpInventoryWithStock(initialStock);
        DecreaseStockService decrease = serviceFor(strategy);
        AtomicInteger successful = new AtomicInteger();
        AtomicInteger insufficientStock = new AtomicInteger();
        AtomicInteger gaveUp = new AtomicInteger();
        double outcomesBefore = outcomes(strategy);
        double conflictsBefore = conflicts(strategy);

        int unexpected = runConcurrently(strategy, tasks, index -> {
            try {
                decrease.execute(inventory.id(), 1);
                successful.incrementAndGet();
            } catch (InsufficientStockException e) {
                insufficientStock.incrementAndGet();
            } catch (ConcurrencyConflictException e) {
                gaveUp.incrementAndGet();
            }
        });

        int finalStock = currentStock();
        log.info("{}: {} tasks -> {} sold, {} insufficient stock, {} gave up, {} conflicts, final stock {}",
                strategy.tag(), tasks, successful.get(), insufficientStock.get(), gaveUp.get(),
                (long) (conflicts(strategy) - conflictsBefore), finalStock);

        assertEquals(0, unexpected, "Unexpected exceptions");
        assertEquals(tasks, successful.get() + insufficientStock.get() + gaveUp.get(), "Every task has one outcome");
        assertTrue(finalStock >= 0, "Stock must never be negative");
        assertEquals(initialStock - successful.get(), finalStock, "Every sale is reflected once (no lost update)");
        assertTrue(finalStock == 0 || gaveUp.get() > 0, "Stock may only remain if some decreases gave up");
        if (!retries(strategy)) {
            assertEquals(0, gaveUp.get(), strategy.tag() + " never gives up");
            assertEquals(initialStock, successful.get(), strategy.tag() + " sells all the stock");
        }
        assertEquals(tasks, outcomes(strategy) - outcomesBefore, 0.0, "One outcome metric per decrease");
    }

    @ParameterizedTest(name = "{0}: {1} interleaved increase and decrease tasks")
    @MethodSource("lostUpdateRuns")
    void shouldLoseNoUpdateUnderConcurrentIncreasesAndDecreases(ConcurrencyStrategy strategy, int tasks) throws Exception {
        int initialStock = tasks / 4;
        int increaseBy = 1;
        int decreaseBy = 2;
        setUpInventoryWithStock(initialStock);
        DecreaseStockService decrease = serviceFor(strategy);
        AtomicInteger increases = new AtomicInteger();
        AtomicInteger successfulDecreases = new AtomicInteger();

        int unexpected = runConcurrently(strategy, tasks, index -> {
            if (index % 2 == 0) {
                increaseStockService.execute(inventory.id(), increaseBy);
                increases.incrementAndGet();
            } else {
                try {
                    decrease.execute(inventory.id(), decreaseBy);
                    successfulDecreases.incrementAndGet();
                } catch (InsufficientStockException | ConcurrencyConflictException e) {
                    // refused without changing the stock; the invariant below must still hold
                }
            }
        });

        int expected = initialStock + increases.get() * increaseBy - successfulDecreases.get() * decreaseBy;
        assertEquals(0, unexpected, "Unexpected exceptions");
        assertEquals(tasks / 2, increases.get(), "Every increase must succeed");
        assertEquals(expected, currentStock(),
                "Final stock must equal initial + increases - successful decreases (no lost update)");
    }

    private DecreaseStockService serviceFor(ConcurrencyStrategy strategy) {
        return new DecreaseStockService(decrementers.get(strategy), inventoryStockNotifier);
    }

    private static boolean retries(ConcurrencyStrategy strategy) {
        return strategy == ConcurrencyStrategy.OPTIMISTIC || strategy == ConcurrencyStrategy.SERIALIZABLE;
    }

    /**
     * Runs {@code tasks} invocations of {@code task}, released together by a
     * start latch, and returns how many threw an unexpected exception.
     */
    private int runConcurrently(ConcurrencyStrategy strategy, int tasks, IntConsumer task)
            throws InterruptedException, SQLException {
        int threads = Math.min(tasks, MAX_THREADS);
        log.info("{}: running {} tasks on {} threads against a connection pool of {}",
                strategy.tag(), tasks, threads, connectionPoolSize());

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

    private double outcomes(ConcurrencyStrategy strategy) {
        return meterRegistry.find("inventory.decrease.outcomes").tag("strategy", strategy.tag())
                .counters().stream().mapToDouble(Counter::count).sum();
    }

    private double conflicts(ConcurrencyStrategy strategy) {
        Counter counter = meterRegistry.find("inventory.decrease.conflicts").tag("strategy", strategy.tag()).counter();
        return counter == null ? 0 : counter.count();
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
