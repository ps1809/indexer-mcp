package com.projectiq.mcp.readiness.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ReadinessLevel enum.
 */
class ReadinessLevelTest {

    @Test
    void testEnumValues() {
        ReadinessLevel[] values = ReadinessLevel.values();
        assertEquals(4, values.length);
        assertEquals(ReadinessLevel.READY, values[0]);
        assertEquals(ReadinessLevel.READY_WITH_WARNINGS, values[1]);
        assertEquals(ReadinessLevel.REQUIRES_REVIEW, values[2]);
        assertEquals(ReadinessLevel.NOT_READY, values[3]);
    }

    @Test
    void testOrdering() {
        // Verify ordering from most ready to least ready
        assertTrue(ReadinessLevel.READY.ordinal() < ReadinessLevel.READY_WITH_WARNINGS.ordinal());
        assertTrue(ReadinessLevel.READY_WITH_WARNINGS.ordinal() < ReadinessLevel.REQUIRES_REVIEW.ordinal());
        assertTrue(ReadinessLevel.REQUIRES_REVIEW.ordinal() < ReadinessLevel.NOT_READY.ordinal());
    }

    @Test
    void testValueOf() {
        assertEquals(ReadinessLevel.READY, ReadinessLevel.valueOf("READY"));
        assertEquals(ReadinessLevel.READY_WITH_WARNINGS, ReadinessLevel.valueOf("READY_WITH_WARNINGS"));
        assertEquals(ReadinessLevel.REQUIRES_REVIEW, ReadinessLevel.valueOf("REQUIRES_REVIEW"));
        assertEquals(ReadinessLevel.NOT_READY, ReadinessLevel.valueOf("NOT_READY"));
    }
}