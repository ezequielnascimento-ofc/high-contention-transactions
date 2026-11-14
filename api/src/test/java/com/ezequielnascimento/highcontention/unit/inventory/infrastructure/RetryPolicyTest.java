package com.ezequielnascimento.highcontention.unit.inventory.infrastructure;

import com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryPolicyTest {

    private final RetryPolicy policy = new RetryPolicy(10, Duration.ofMillis(2), Duration.ofMillis(50));

    @Test
    void ceilingDoublesAfterEachFailedAttempt() {
        assertEquals(Duration.ofMillis(2), policy.backoffCeiling(1));
        assertEquals(Duration.ofMillis(4), policy.backoffCeiling(2));
        assertEquals(Duration.ofMillis(8), policy.backoffCeiling(3));
        assertEquals(Duration.ofMillis(32), policy.backoffCeiling(5));
    }

    @Test
    void ceilingIsCappedAtTheMaximumBackoff() {
        assertEquals(Duration.ofMillis(50), policy.backoffCeiling(6));
        assertEquals(Duration.ofMillis(50), policy.backoffCeiling(1_000));
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new RetryPolicy(0, Duration.ZERO, Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> new RetryPolicy(3, Duration.ofMillis(10), Duration.ofMillis(5)));
    }
}
