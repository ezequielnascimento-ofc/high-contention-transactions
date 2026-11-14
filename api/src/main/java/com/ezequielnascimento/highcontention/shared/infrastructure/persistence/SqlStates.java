package com.ezequielnascimento.highcontention.shared.infrastructure.persistence;

import java.sql.SQLException;

/**
 * Standard SQLSTATE codes, read from the cause chain of Spring's data access
 * exceptions so adapters do not depend on driver classes.
 */
public final class SqlStates {

    public static final String UNIQUE_VIOLATION = "23505";
    public static final String FOREIGN_KEY_VIOLATION = "23503";
    public static final String SERIALIZATION_FAILURE = "40001";
    public static final String DEADLOCK_DETECTED = "40P01";

    private SqlStates() {
    }

    public static String of(Throwable throwable) {
        for (Throwable t = throwable; t != null; t = t.getCause()) {
            if (t instanceof SQLException sqlException && sqlException.getSQLState() != null) {
                return sqlException.getSQLState();
            }
        }
        return null;
    }
}
