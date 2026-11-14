package com.ezequielnascimento.highcontention.inventory.infrastructure.config;

import com.ezequielnascimento.highcontention.inventory.domain.port.out.StockDecrementer;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.AtomicConditionalUpdateDecrementer;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.DecrementMetrics;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.OptimisticVersionDecrementer;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.PessimisticLockDecrementer;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.SerializableRetryDecrementer;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.StockDecrementers;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * Builds the four decrement strategies and exposes the configured one as the
 * {@link StockDecrementer} used by the application.
 */
@Configuration
@EnableConfigurationProperties(InventoryConcurrencyProperties.class)
public class InventoryConcurrencyConfig {

    private static final Logger log = LoggerFactory.getLogger(InventoryConcurrencyConfig.class);

    @Bean
    StockDecrementers stockDecrementers(
            JdbcClient jdbcClient,
            PlatformTransactionManager transactionManager,
            ObjectProvider<MeterRegistry> meterRegistry,
            InventoryConcurrencyProperties properties) {
        DecrementMetrics metrics = new DecrementMetrics(meterRegistry.getIfAvailable(SimpleMeterRegistry::new));

        TransactionTemplate readCommitted = new TransactionTemplate(transactionManager);
        readCommitted.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionTemplate serializable = new TransactionTemplate(transactionManager);
        serializable.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

        return new StockDecrementers(List.of(
                new AtomicConditionalUpdateDecrementer(jdbcClient, metrics),
                new PessimisticLockDecrementer(jdbcClient, metrics, readCommitted),
                new OptimisticVersionDecrementer(jdbcClient, metrics, properties.retryPolicy()),
                new SerializableRetryDecrementer(jdbcClient, metrics, serializable, properties.retryPolicy())));
    }

    @Bean
    StockDecrementer stockDecrementer(StockDecrementers decrementers, InventoryConcurrencyProperties properties) {
        log.info("Stock decrease strategy: {} (max attempts {}, backoff {}..{})",
                properties.strategy().tag(), properties.maxAttempts(),
                properties.baseBackoff(), properties.maxBackoff());
        return decrementers.get(properties.strategy());
    }
}
