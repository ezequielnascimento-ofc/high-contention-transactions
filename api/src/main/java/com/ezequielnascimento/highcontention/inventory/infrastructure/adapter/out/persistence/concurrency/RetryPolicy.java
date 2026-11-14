package com.ezequielnascimento.highcontention.inventory.infrastructure.adapter.out.persistence.concurrency;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Bounded retries with exponential backoff and full jitter: before attempt
 * {@code n + 1} the caller sleeps a random time in
 * {@code [0, min(maxBackoff, baseBackoff * 2^(n-1))]}. Jitter spreads the
 * retries of requests that conflicted at the same moment.
 */
public record RetryPolicy(int maxAttempts, Duration baseBackoff, Duration maxBackoff) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (baseBackoff.isNegative() || maxBackoff.compareTo(baseBackoff) < 0) {
            throw new IllegalArgumentException("Backoff must satisfy 0 <= base <= max");
        }
    }

    /** Upper bound of the pause after the given failed attempt (1-based). */
    public Duration backoffCeiling(int failedAttempt) {
        int exponent = Math.min(failedAttempt - 1, 30);
        long nanos = baseBackoff.toNanos() << exponent;
        if (nanos < 0 || nanos > maxBackoff.toNanos()) {
            nanos = maxBackoff.toNanos();
        }
        return Duration.ofNanos(nanos);
    }

    /** Sleeps a random time up to {@link #backoffCeiling(int)}. */
    public void pauseAfter(int failedAttempt) {
        long ceiling = backoffCeiling(failedAttempt).toNanos();
        if (ceiling == 0) {
            return;
        }
        long nanos = ThreadLocalRandom.current().nextLong(ceiling + 1);
        try {
            Thread.sleep(Duration.ofNanos(nanos));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while backing off", e);
        }
    }
}
