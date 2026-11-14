package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

/**
 * How a stock decrease stays correct when many requests hit the same row.
 * Selected with {@code inventory.concurrency.strategy}.
 */
public enum ConcurrencyStrategy {

    /** One conditional {@code UPDATE ... WHERE quantity >= :q}; the row lock lasts one statement. */
    ATOMIC,

    /** {@code SELECT ... FOR UPDATE}, check in Java, then update; writers queue on the row lock. */
    PESSIMISTIC,

    /** Read the version, write only if it is unchanged, retry with backoff otherwise. */
    OPTIMISTIC,

    /** Read-check-write in a {@code SERIALIZABLE} transaction, retry on serialization failure. */
    SERIALIZABLE;

    public String tag() {
        return name().toLowerCase();
    }
}
