package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Counters per strategy, exposed at {@code /actuator/metrics}:
 * <ul>
 *   <li>{@code inventory.decrease.attempts}: tries, including retries;</li>
 *   <li>{@code inventory.decrease.conflicts}: tries lost to a concurrent writer;</li>
 *   <li>{@code inventory.decrease.outcomes}: one per call, tagged
 *       {@code success}, {@code insufficient_stock}, {@code not_found} or {@code gave_up}.</li>
 * </ul>
 */
public class DecrementMetrics {

    public enum Outcome { SUCCESS, INSUFFICIENT_STOCK, NOT_FOUND, GAVE_UP }

    private final MeterRegistry registry;

    public DecrementMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    void attempt(ConcurrencyStrategy strategy) {
        counter("inventory.decrease.attempts", strategy).increment();
    }

    void conflict(ConcurrencyStrategy strategy) {
        counter("inventory.decrease.conflicts", strategy).increment();
    }

    void outcome(ConcurrencyStrategy strategy, Outcome outcome) {
        Counter.builder("inventory.decrease.outcomes")
                .tag("strategy", strategy.tag())
                .tag("outcome", outcome.name().toLowerCase())
                .register(registry)
                .increment();
    }

    private Counter counter(String name, ConcurrencyStrategy strategy) {
        return Counter.builder(name).tag("strategy", strategy.tag()).register(registry);
    }
}
