package com.projectiq.mcp.monitoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PerformanceTimer}.
 * Verifies timing functionality and duration recording.
 */
class PerformanceTimerTest {

    @Test
    void shouldRecordDuration() {
        PerformanceTimer timer = PerformanceTimer.start("test-operation");

        // Simulate some work
        long duration = timer.stop();

        assertTrue(duration >= 0, "Duration should be non-negative");
    }

    @Test
    void shouldRecordPositiveDurationForWork() throws InterruptedException {
        PerformanceTimer timer = PerformanceTimer.start("test-sleep");

        Thread.sleep(10); // 10ms sleep

        long duration = timer.stop();

        assertTrue(duration >= 5, "Duration should be at least 5ms for 10ms sleep");
    }

    @Test
    void shouldReturnElapsedMsWithoutStopping() throws InterruptedException {
        PerformanceTimer timer = PerformanceTimer.start("test-elapsed");

        Thread.sleep(5);

        long elapsed = timer.elapsedMs();
        assertTrue(elapsed >= 2, "Elapsed time should be at least 2ms");

        // Timer should still be running
        long duration = timer.stop();
        assertTrue(duration >= elapsed, "Final duration should be >= elapsed time");
    }

    @Test
    void shouldRecordDurationWithContext() {
        PerformanceTimer timer = PerformanceTimer.start("test-context");

        long duration = timer.stopWithContext("repository: test-repo");

        assertTrue(duration >= 0, "Duration should be non-negative");
    }

    @Test
    void shouldHandleMultipleTimers() {
        PerformanceTimer timer1 = PerformanceTimer.start("operation-1");
        PerformanceTimer timer2 = PerformanceTimer.start("operation-2");

        long duration1 = timer1.stop();
        long duration2 = timer2.stop();

        assertTrue(duration1 >= 0);
        assertTrue(duration2 >= 0);
    }

    @Test
    void shouldHandleRapidStartStop() {
        for (int i = 0; i < 100; i++) {
            PerformanceTimer timer = PerformanceTimer.start("rapid-" + i);
            long duration = timer.stop();
            assertTrue(duration >= 0, "Duration should be non-negative for rapid operation " + i);
        }
    }
}