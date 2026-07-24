package com.projectiq.mcp.recommendation.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;

class RecommendationReportTest {

    @Test
    void shouldCreateEmptyReport() {
        RecommendationReport report = new RecommendationReport();
        assertNotNull(report.getPrioritizedRecommendations());
        assertNotNull(report.getImplementationAdvice());
        assertNotNull(report.getTestingRecommendations());
        assertNotNull(report.getArchitecturalGuidance());
        assertNotNull(report.getRepositoryBestPractices());
        assertNotNull(report.getRiskMitigationSuggestions());
        assertNotNull(report.getErrors());
        assertTrue(report.getPrioritizedRecommendations().isEmpty());
        assertTrue(report.getErrors().isEmpty());
    }

    @Test
    void shouldSetAndGetWorkflowInfo() {
        RecommendationReport report = new RecommendationReport();
        report.setWorkflowName("Test Workflow");
        report.setWorkflowType("Feature Implementation");
        assertEquals("Test Workflow", report.getWorkflowName());
        assertEquals("Feature Implementation", report.getWorkflowType());
    }

    @Test
    void shouldSetAndGetExecutiveSummary() {
        RecommendationReport report = new RecommendationReport();
        report.setExecutiveSummary("Test summary");
        assertEquals("Test summary", report.getExecutiveSummary());
    }

    @Test
    void shouldAddRecommendations() {
        RecommendationReport report = new RecommendationReport();
        Recommendation rec = new Recommendation("REC-0001", RecommendationCategory.IMPLEMENTATION,
                RecommendationPriority.HIGH, "Test", "Desc", "Rationale");
        report.addRecommendation(rec);
        assertEquals(1, report.getPrioritizedRecommendations().size());
        assertEquals("REC-0001", report.getPrioritizedRecommendations().get(0).getId());
    }

    @Test
    void shouldAddListItems() {
        RecommendationReport report = new RecommendationReport();
        report.addImplementationAdvice("Advice 1");
        report.addTestingRecommendation("Test 1");
        report.addArchitecturalGuidance("Arch 1");
        report.addRepositoryBestPractice("Practice 1");
        report.addRiskMitigationSuggestion("Risk 1");

        assertEquals(1, report.getImplementationAdvice().size());
        assertEquals(1, report.getTestingRecommendations().size());
        assertEquals(1, report.getArchitecturalGuidance().size());
        assertEquals(1, report.getRepositoryBestPractices().size());
        assertEquals(1, report.getRiskMitigationSuggestions().size());
    }

    @Test
    void shouldSetAndGetConfidenceScore() {
        RecommendationReport report = new RecommendationReport();
        report.setConfidenceScore(85);
        assertEquals(85, report.getConfidenceScore());
    }

    @Test
    void shouldSetAndGetReportSummary() {
        RecommendationReport report = new RecommendationReport();
        RecommendationReport.ReportSummary summary = new RecommendationReport.ReportSummary();
        summary.setTotalRecommendations(10);
        summary.setCriticalCount(2);
        summary.setHighCount(3);
        summary.setMediumCount(3);
        summary.setLowCount(2);
        summary.setImplementationCount(4);
        summary.setArchitectureCount(2);
        summary.setTestingCount(2);
        summary.setRiskCount(2);

        report.setRecommendationSummary(summary);
        assertEquals(10, report.getRecommendationSummary().getTotalRecommendations());
        assertEquals(2, report.getRecommendationSummary().getCriticalCount());
        assertEquals(3, report.getRecommendationSummary().getHighCount());
        assertEquals(4, report.getRecommendationSummary().getImplementationCount());
    }

    @Test
    void shouldAddError() {
        RecommendationReport report = new RecommendationReport();
        report.addError("Test error");
        assertEquals(1, report.getErrors().size());
        assertEquals("Test error", report.getErrors().get(0));
    }

    @Test
    void shouldHandleNullListsWhenSetting() {
        RecommendationReport report = new RecommendationReport();
        report.setPrioritizedRecommendations(null);
        report.setImplementationAdvice(null);
        report.setTestingRecommendations(null);
        report.setErrors(null);

        assertNotNull(report.getPrioritizedRecommendations());
        assertNotNull(report.getImplementationAdvice());
        assertNotNull(report.getTestingRecommendations());
        assertNotNull(report.getErrors());
        assertTrue(report.getPrioritizedRecommendations().isEmpty());
    }
}