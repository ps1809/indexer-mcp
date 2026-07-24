package com.projectiq.mcp.pipeline.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ContextPriority}.
 */
class ContextPriorityTest {

    @Test
    void testEnumValues() {
        ContextPriority[] values = ContextPriority.values();
        assertEquals(3, values.length);
        assertEquals(ContextPriority.HIGH, values[0]);
        assertEquals(ContextPriority.MEDIUM, values[1]);
        assertEquals(ContextPriority.LOW, values[2]);
    }

    @Test
    void testOrdinalOrder() {
        assertTrue(ContextPriority.HIGH.ordinal() < ContextPriority.MEDIUM.ordinal());
        assertTrue(ContextPriority.MEDIUM.ordinal() < ContextPriority.LOW.ordinal());
    }
}