package com.projectiq.mcp.pipeline.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ContextSourceType}.
 */
class ContextSourceTypeTest {

    @Test
    void testEnumValues() {
        ContextSourceType[] values = ContextSourceType.values();
        assertEquals(16, values.length);
        assertEquals(ContextSourceType.REPOSITORY_SUMMARY, values[0]);
        assertEquals(ContextSourceType.IMPACT_ANALYSIS, values[15]);
    }
}