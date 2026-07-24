package com.projectiq.mcp.validation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ValidationSeverity enum.
 */
class ValidationSeverityTest {

    @Test
    void shouldHaveAllSeverityLevels() {
        assertEquals(5, ValidationSeverity.values().length);
        assertNotNull(ValidationSeverity.valueOf("CRITICAL"));
        assertNotNull(ValidationSeverity.valueOf("HIGH"));
        assertNotNull(ValidationSeverity.valueOf("MEDIUM"));
        assertNotNull(ValidationSeverity.valueOf("LOW"));
        assertNotNull(ValidationSeverity.valueOf("INFORMATIONAL"));
    }

    @Test
    void shouldMaintainStableOrdering() {
        ValidationSeverity[] values = ValidationSeverity.values();
        assertEquals(ValidationSeverity.CRITICAL, values[0]);
        assertEquals(ValidationSeverity.HIGH, values[1]);
        assertEquals(ValidationSeverity.MEDIUM, values[2]);
        assertEquals(ValidationSeverity.LOW, values[3]);
        assertEquals(ValidationSeverity.INFORMATIONAL, values[4]);
    }
}