package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

import com.ezequielnascimento.highcontention.inventory.domain.port.out.StockDecrementer;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** All four strategies, so tests and benchmarks can run each one side by side. */
public final class StockDecrementers {

    private final Map<ConcurrencyStrategy, StockDecrementer> byStrategy = new EnumMap<>(ConcurrencyStrategy.class);

    public StockDecrementers(List<AbstractStockDecrementer> decrementers) {
        decrementers.forEach(d -> byStrategy.put(d.strategy(), d));
        if (byStrategy.size() != ConcurrencyStrategy.values().length) {
            throw new IllegalStateException("Expected one decrementer per strategy, got " + byStrategy.keySet());
        }
    }

    public StockDecrementer get(ConcurrencyStrategy strategy) {
        return byStrategy.get(strategy);
    }

    public Map<ConcurrencyStrategy, StockDecrementer> all() {
        return Collections.unmodifiableMap(byStrategy);
    }
}
