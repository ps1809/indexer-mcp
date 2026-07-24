package com.projectiq.mcp.readiness.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the AssessmentCategory enum.
 */
class AssessmentCategoryTest {

    @Test
    void testEnumValues() {
        AssessmentCategory[] values = AssessmentCategory.values();
        assertEquals(8, values.length);
        assertEquals(AssessmentCategory.WORKFLOW, values[0]);
        assertEquals(AssessmentCategory.REPOSITORY, values[1]);
        assertEquals(AssessmentCategory.DEPENDENCIES, values[2]);
        assertEquals(AssessmentCategory.ARCHITECTURE, values[3]);
        assertEquals(AssessmentCategory.TESTING, values[4]);
        assertEquals(AssessmentCategory.CONFIGURATION, values[5]);
        assertEquals(AssessmentCategory.RISK, values[6]);
        assertEquals(AssessmentCategory.DOCUMENTATION, values[7]);
    }

    @Test
    void testValueOf() {
        assertEquals(AssessmentCategory.WORKFLOW, AssessmentCategory.valueOf("WORKFLOW"));
        assertEquals(AssessmentCategory.REPOSITORY, AssessmentCategory.valueOf("REPOSITORY"));
        assertEquals(AssessmentCategory.DEPENDENCIES, AssessmentCategory.valueOf("DEPENDENCIES"));
        assertEquals(AssessmentCategory.ARCHITECTURE, AssessmentCategory.valueOf("ARCHITECTURE"));
        assertEquals(AssessmentCategory.TESTING, AssessmentCategory.valueOf("TESTING"));
        assertEquals(AssessmentCategory.CONFIGURATION, AssessmentCategory.valueOf("CONFIGURATION"));
        assertEquals(AssessmentCategory.RISK, AssessmentCategory.valueOf("RISK"));
        assertEquals(AssessmentCategory.DOCUMENTATION, AssessmentCategory.valueOf("DOCUMENTATION"));
    }
}