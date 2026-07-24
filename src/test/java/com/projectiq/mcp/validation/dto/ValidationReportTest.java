package com.projectiq.mcp.validation.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the ValidationReport DTO.
 */
class ValidationReportTest {

    @Test
    void shouldCreateEmptyReport() {
        ValidationReport report = new ValidationReport();
        assertNull(report.getOverallStatus());
        assertEquals(0, report.getPassedValidations());
        assertEquals(0, report.getFailedValidations());
        assertEquals(0, report.getWarnings());
        assertEquals(0, report.getBlockingIssues());
        assertTrue(report.getFindings().isEmpty());
        assertTrue(report.getRecommendedActions().isEmpty());
        assertTrue(report.getErrors().isEmpty());
    }

    @Test
    void shouldAddFindings() {
        ValidationReport report = new ValidationReport();
        report.addFinding(new ValidationFinding(
                ValidationCategory.WORKFLOW_VALIDATION,
                ValidationSeverity.INFORMATIONAL,
                "Test finding",
                "Test details",
                false));

        assertEquals(1, report.getFindings().size());
        assertEquals("Test finding", report.getFindings().get(0).getMessage());
    }

    @Test
    void shouldBuildRepositoryHealthSummary() {
        ValidationReport.RepositoryHealthSummary healthSummary = new ValidationReport.RepositoryHealthSummary();
        healthSummary.setHealthScore(80);
        healthSummary.setMaintainabilityRating("good");
        healthSummary.setComplexityRating("moderate");
        healthSummary.setTestingMaturity("adequate");
        healthSummary.setDependencyHealth("healthy");
        healthSummary.setArchitectureConsistency("consistent");

        ValidationReport report = new ValidationReport();
        report.setRepositoryHealthSummary(healthSummary);

        assertNotNull(report.getRepositoryHealthSummary());
        assertEquals(80, report.getRepositoryHealthSummary().getHealthScore());
        assertEquals("good", report.getRepositoryHealthSummary().getMaintainabilityRating());
        assertEquals("consistent", report.getRepositoryHealthSummary().getArchitectureConsistency());
    }

    @Test
    void shouldBuildRiskSummary() {
        ValidationReport.RiskSummary riskSummary = new ValidationReport.RiskSummary();
        riskSummary.setTotalRisks(3);
        riskSummary.setCriticalRisks(1);
        riskSummary.setHighRisks(1);
        riskSummary.setMediumRisks(1);
        riskSummary.setTopRisks(List.of("Critical risk A", "High risk B"));

        ValidationReport report = new ValidationReport();
        report.setRiskSummary(riskSummary);

        assertNotNull(report.getRiskSummary());
        assertEquals(3, report.getRiskSummary().getTotalRisks());
        assertEquals(1, report.getRiskSummary().getCriticalRisks());
        assertEquals(2, report.getRiskSummary().getTopRisks().size());
    }

    @Test
    void shouldAddRecommendedActions() {
        ValidationReport report = new ValidationReport();
        report.addRecommendedAction("Action 1");
        report.addRecommendedAction("Action 2");

        assertEquals(2, report.getRecommendedActions().size());
        assertEquals("Action 1", report.getRecommendedActions().get(0));
    }

    @Test
    void shouldSetOverallStatus() {
        ValidationReport report = new ValidationReport();
        report.setOverallStatus("BLOCKED");
        report.setReadinessScore(35);
        report.setReadinessLabel("HIGH_RISK");

        assertEquals("BLOCKED", report.getOverallStatus());
        assertEquals(35, report.getReadinessScore());
        assertEquals("HIGH_RISK", report.getReadinessLabel());
    }

    @Test
    void shouldHandleErrors() {
        ValidationReport report = new ValidationReport();
        report.addError("Error occurred");

        assertEquals(1, report.getErrors().size());
        assertEquals("Error occurred", report.getErrors().get(0));
    }
}