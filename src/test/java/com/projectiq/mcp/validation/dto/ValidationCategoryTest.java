package com.projectiq.mcp.validation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ValidationCategory enum.
 */
class ValidationCategoryTest {

    @Test
    void shouldHaveAllCategories() {
        assertEquals(8, ValidationCategory.values().length);
        assertNotNull(ValidationCategory.valueOf("WORKFLOW_VALIDATION"));
        assertNotNull(ValidationCategory.valueOf("REPOSITORY_VALIDATION"));
        assertNotNull(ValidationCategory.valueOf("DEPENDENCY_VALIDATION"));
        assertNotNull(ValidationCategory.valueOf("ARCHITECTURE_VALIDATION"));
        assertNotNull(ValidationCategory.valueOf("CONVENTION_VALIDATION"));
        assertNotNull(ValidationCategory.valueOf("TEST_COVERAGE_VALIDATION"));
        assertNotNull(ValidationCategory.valueOf("RISK_VALIDATION"));
        assertNotNull(ValidationCategory.valueOf("EXECUTION_READINESS"));
    }

    @Test
    void shouldMaintainStableOrdering() {
        ValidationCategory[] values = ValidationCategory.values();
        assertEquals(ValidationCategory.WORKFLOW_VALIDATION, values[0]);
        assertEquals(ValidationCategory.REPOSITORY_VALIDATION, values[1]);
        assertEquals(ValidationCategory.DEPENDENCY_VALIDATION, values[2]);
        assertEquals(ValidationCategory.ARCHITECTURE_VALIDATION, values[3]);
        assertEquals(ValidationCategory.CONVENTION_VALIDATION, values[4]);
        assertEquals(ValidationCategory.TEST_COVERAGE_VALIDATION, values[5]);
        assertEquals(ValidationCategory.RISK_VALIDATION, values[6]);
        assertEquals(ValidationCategory.EXECUTION_READINESS, values[7]);
    }
}