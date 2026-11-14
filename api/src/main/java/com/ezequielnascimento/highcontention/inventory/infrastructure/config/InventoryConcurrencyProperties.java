package com.ezequielnascimento.highcontention.inventory.infrastructure.config;

import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.ConcurrencyStrategy;
import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.RetryPolicy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * {@code inventory.concurrency.*}: which strategy serves the decrease endpoint
 * and how the retrying strategies (optimistic, serializable) back off.
 */
@ConfigurationProperties("inventory.concurrency")
public record InventoryConcurrencyProperties(
        @DefaultValue("atomic") ConcurrencyStrategy strategy,
        @DefaultValue("10") int maxAttempts,
        @DefaultValue("2ms") Duration baseBackoff,
        @DefaultValue("50ms") Duration maxBackoff
) {

    public RetryPolicy retryPolicy() {
        return new RetryPolicy(maxAttempts, baseBackoff, maxBackoff);
    }
}
