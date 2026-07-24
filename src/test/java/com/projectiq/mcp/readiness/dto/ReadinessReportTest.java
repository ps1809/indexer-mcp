package com.projectiq.mcp.readiness.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the ReadinessReport DTO.
 */
class ReadinessReportTest {

    @Test
    void testDefaultConstructor() {
        ReadinessReport report = new ReadinessReport();
        assertNotNull(report.getBlockingIssues());
        assertNotNull(report.getWarnings());
        assertNotNull(report.getPassedChecks());
        assertNotNull(report.getCategoryAssessments());
        assertNotNull(report.getNextActions());
        assertNotNull(report.getErrors());
        assertTrue(report.getBlockingIssues().isEmpty());
        assertTrue(report.getErrors().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        ReadinessReport report = new ReadinessReport();
        report.setWorkflowName("test-workflow");
        report.setWorkflowType("Feature Implementation");
        report.setOverallReadinessLevel(ReadinessLevel.READY);
        report.setReadinessScore(95);
        report.setFinalImplementationRecommendation("Approved");

        assertEquals("test-workflow", report.getWorkflowName());
        assertEquals("Feature Implementation", report.getWorkflowType());
        assertEquals(ReadinessLevel.READY, report.getOverallReadinessLevel());
        assertEquals(95, report.getReadinessScore());
        assertEquals("Approved", report.getFinalImplementationRecommendation());
    }

    @Test
    void testAddBlockingIssue() {
        ReadinessReport report = new ReadinessReport();
        report.addBlockingIssue("Issue 1");
        report.addBlockingIssue("Issue 2");
        assertEquals(2, report.getBlockingIssues().size());
        assertTrue(report.getBlockingIssues().contains("Issue 1"));
    }

    @Test
    void testAddWarning() {
        ReadinessReport report = new ReadinessReport();
        report.addWarning("Warning 1");
        assertEquals(1, report.getWarnings().size());
        assertTrue(report.getWarnings().contains("Warning 1"));
    }

    @Test
    void testAddPassedCheck() {
        ReadinessReport report = new ReadinessReport();
        report.addPassedCheck("Check 1");
        assertEquals(1, report.getPassedChecks().size());
        assertTrue(report.getPassedChecks().contains("Check 1"));
    }

    @Test
    void testCategoryAssessment() {
        ReadinessReport.CategoryAssessment assessment = new ReadinessReport.CategoryAssessment();
        assessment.setCategory(AssessmentCategory.WORKFLOW);
        assessment.setStatus("PASSED");
        assessment.setScore(100);
        assessment.addFinding("Workflow complete");
        assessment.addRecommendation("Proceed");

        assertEquals(AssessmentCategory.WORKFLOW, assessment.getCategory());
        assertEquals("PASSED", assessment.getStatus());
        assertEquals(100, assessment.getScore());
        assertEquals(1, assessment.getFindings().size());
        assertEquals(1, assessment.getRecommendations().size());
    }

    @Test
    void testRepositorySummary() {
        ReadinessReport.RepositorySummary summary = new ReadinessReport.RepositorySummary();
        summary.setRepositoryName("test-repo");
        summary.setHealthScore(80);
        summary.setMaintainabilityRating("Good");
        summary.setTestingMaturity("Mature");

        assertEquals("test-repo", summary.getRepositoryName());
        assertEquals(80, summary.getHealthScore());
        assertEquals("Good", summary.getMaintainabilityRating());
        assertEquals("Mature", summary.getTestingMaturity());
    }

    @Test
    void testRiskOverview() {
        ReadinessReport.RiskOverview riskOverview = new ReadinessReport.RiskOverview();
        riskOverview.setTotalRisks(3);
        riskOverview.setCriticalRisks(1);
        riskOverview.setHighRisks(1);
        riskOverview.setMediumRisks(1);
        riskOverview.addTopRisk("Critical risk");

        assertEquals(3, riskOverview.getTotalRisks());
        assertEquals(1, riskOverview.getCriticalRisks());
        assertEquals(1, riskOverview.getHighRisks());
        assertEquals(1, riskOverview.getMediumRisks());
        assertEquals(1, riskOverview.getTopRisks().size());
    }

    @Test
    void testAssessmentSummary() {
        ReadinessReport.AssessmentSummary summary = new ReadinessReport.AssessmentSummary();
        summary.setTotalCategories(8);
        summary.setPassedCategories(6);
        summary.setWarningCategories(1);
        summary.setFailedCategories(1);
        summary.setTotalBlockingIssues(0);
        summary.setTotalWarnings(2);
        summary.setTotalPassedChecks(10);
        summary.setDecision("READY_WITH_WARNINGS");

        assertEquals(8, summary.getTotalCategories());
        assertEquals(6, summary.getPassedCategories());
        assertEquals(1, summary.getWarningCategories());
        assertEquals(1, summary.getFailedCategories());
        assertEquals(0, summary.getTotalBlockingIssues());
        assertEquals(2, summary.getTotalWarnings());
        assertEquals(10, summary.getTotalPassedChecks());
        assertEquals("READY_WITH_WARNINGS", summary.getDecision());
    }

    @Test
    void testAddError() {
        ReadinessReport report = new ReadinessReport();
        report.addError("Error occurred");
        assertEquals(1, report.getErrors().size());
        assertTrue(report.getErrors().contains("Error occurred"));
    }

    @Test
    void testNullSetters() {
        ReadinessReport report = new ReadinessReport();
        report.setBlockingIssues(null);
        report.setWarnings(null);
        report.setPassedChecks(null);
        report.setCategoryAssessments(null);
        report.setNextActions(null);
        report.setErrors(null);

        assertNotNull(report.getBlockingIssues());
        assertTrue(report.getBlockingIssues().isEmpty());
        assertNotNull(report.getErrors());
        assertTrue(report.getErrors().isEmpty());
    }
}