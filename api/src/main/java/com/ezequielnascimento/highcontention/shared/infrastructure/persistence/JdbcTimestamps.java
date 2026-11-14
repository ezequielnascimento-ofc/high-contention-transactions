package com.ezequielnascimento.highcontention.shared.infrastructure.persistence;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Binds {@link Instant} values to {@code TIMESTAMP WITH TIME ZONE} columns.
 * <p>
 * The PostgreSQL JDBC driver cannot infer a SQL type for {@code Instant}
 * ({@code setObject} fails), but it maps {@link OffsetDateTime} natively.
 */
public final class JdbcTimestamps {

    private JdbcTimestamps() {
    }

    public static OffsetDateTime utc(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
