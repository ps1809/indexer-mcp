package com.projectiq.mcp.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight, log-based performance timer for recording operation durations.
 * Uses System.nanoTime() for high-precision timing and logs results at INFO level.
 *
 * <p>This is a log-based monitoring approach only - no external monitoring
 * platforms or distributed tracing are used.
 */
public final class PerformanceTimer {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTimer.class);
    private static final long SLOW_THRESHOLD_MS = 5000;

    private final String operationName;
    private final long startTimeNanos;

    private PerformanceTimer(String operationName) {
        this.operationName = operationName;
        this.startTimeNanos = System.nanoTime();
    }

    /**
     * Creates and starts a new performance timer for the given operation.
     *
     * @param operationName a descriptive name for the operation being timed
     * @return a new started PerformanceTimer
     */
    public static PerformanceTimer start(String operationName) {
        return new PerformanceTimer(operationName);
    }

    /**
     * Stops the timer and logs the duration at INFO level.
     * If the operation exceeded the slow threshold (5s), logs at WARN level.
     *
     * @return the duration in milliseconds
     */
    public long stop() {
        long durationNanos = System.nanoTime() - startTimeNanos;
        long durationMs = durationNanos / 1_000_000L;

        if (durationMs >= SLOW_THRESHOLD_MS) {
            logger.warn("[PERF] {} completed in {} ms (SLOW)", operationName, durationMs);
        } else {
            logger.info("[PERF] {} completed in {} ms", operationName, durationMs);
        }

        return durationMs;
    }

    /**
     * Stops the timer and logs the duration with additional context.
     *
     * @param context additional context to include in the log (e.g., repository name)
     * @return the duration in milliseconds
     */
    public long stopWithContext(String context) {
        long durationNanos = System.nanoTime() - startTimeNanos;
        long durationMs = durationNanos / 1_000_000L;

        if (durationMs >= SLOW_THRESHOLD_MS) {
            logger.warn("[PERF] {} completed in {} ms (SLOW) [{}]", operationName, durationMs, context);
        } else {
            logger.info("[PERF] {} completed in {} ms [{}]", operationName, durationMs, context);
        }

        return durationMs;
    }

    /**
     * Returns the elapsed time in milliseconds without stopping the timer.
     *
     * @return elapsed time in milliseconds since this timer was started
     */
    public long elapsedMs() {
        return (System.nanoTime() - startTimeNanos) / 1_000_000L;
    }
}