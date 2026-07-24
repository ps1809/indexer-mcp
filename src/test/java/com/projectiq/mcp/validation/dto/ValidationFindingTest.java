package com.projectiq.mcp.validation.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ValidationFinding DTO.
 */
class ValidationFindingTest {

    @Test
    void shouldCreateFindingWithAllFields() {
        ValidationFinding finding = new ValidationFinding(
                ValidationCategory.WORKFLOW_VALIDATION,
                ValidationSeverity.CRITICAL,
                "Workflow name is missing",
                "A workflow must have a non-empty name",
                true);

        assertEquals(ValidationCategory.WORKFLOW_VALIDATION, finding.getCategory());
        assertEquals(ValidationSeverity.CRITICAL, finding.getSeverity());
        assertEquals("Workflow name is missing", finding.getMessage());
        assertEquals("A workflow must have a non-empty name", finding.getDetails());
        assertTrue(finding.isBlocking());
    }

    @Test
    void shouldCreateNonBlockingFinding() {
        ValidationFinding finding = new ValidationFinding(
                ValidationCategory.REPOSITORY_VALIDATION,
                ValidationSeverity.INFORMATIONAL,
                "Health score is good",
                "Repository health is adequate",
                false);

        assertFalse(finding.isBlocking());
        assertEquals(ValidationSeverity.INFORMATIONAL, finding.getSeverity());
    }

    @Test
    void shouldSupportDefaultConstructor() {
        ValidationFinding finding = new ValidationFinding();
        assertNull(finding.getCategory());
        assertNull(finding.getSeverity());
        assertNull(finding.getMessage());
        assertNull(finding.getDetails());
        assertFalse(finding.isBlocking());
    }

    @Test
    void shouldAllowSetters() {
        ValidationFinding finding = new ValidationFinding();
        finding.setCategory(ValidationCategory.DEPENDENCY_VALIDATION);
        finding.setSeverity(ValidationSeverity.HIGH);
        finding.setMessage("Circular dependency detected");
        finding.setDetails("Cycle found in dependency graph");
        finding.setBlocking(true);

        assertEquals(ValidationCategory.DEPENDENCY_VALIDATION, finding.getCategory());
        assertEquals(ValidationSeverity.HIGH, finding.getSeverity());
        assertEquals("Circular dependency detected", finding.getMessage());
        assertTrue(finding.isBlocking());
    }
}