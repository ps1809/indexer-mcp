package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.recommendation.dto.RecommendationCategory;
import com.projectiq.mcp.recommendation.dto.RecommendationPriority;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.recommendation.service.RecommendationEngineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateRecommendationsToolTest {

    @Mock
    private RecommendationEngineService recommendationEngineService;

    private GenerateRecommendationsTool tool;

    @BeforeEach
    void setUp() {
        tool = new GenerateRecommendationsTool(recommendationEngineService);
    }

    @Test
    void shouldGenerateRecommendationsSuccessfully() {
        RecommendationReport report = new RecommendationReport();
        report.setWorkflowName("Test Workflow");
        report.setWorkflowType("Feature Implementation");
        report.setExecutiveSummary("Executive summary");
        report.setConfidenceScore(85);
        report.setPrioritizedRecommendations(new ArrayList<>());
        report.setImplementationAdvice(new ArrayList<>());
        report.setTestingRecommendations(new ArrayList<>());
        report.setErrors(new ArrayList<>());

        when(recommendationEngineService.generateRecommendations(
                anyString(), any(), any(), anyString(), any()))
                .thenReturn(report);

        String result = tool.generateRecommendations(
                "Test Workflow", "Feature Implementation",
                "Implement authentication", "my-project", "main");

        assertNotNull(result);
        assertTrue(result.contains("Test Workflow"));
        assertTrue(result.contains("85"));
    }

    @Test
    void shouldReturnErrorForEmptyWorkflowName() {
        String result = tool.generateRecommendations(
                "", "Feature", "Desc", "my-project", "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Workflow name is required"));
    }

    @Test
    void shouldReturnErrorForNullWorkflowName() {
        String result = tool.generateRecommendations(
                null, "Feature", "Desc", "my-project", "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Workflow name is required"));
    }

    @Test
    void shouldReturnErrorForEmptyRepositoryName() {
        String result = tool.generateRecommendations(
                "Workflow", "Feature", "Desc", "", "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void shouldReturnErrorForNullRepositoryName() {
        String result = tool.generateRecommendations(
                "Workflow", "Feature", "Desc", null, "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void shouldHandleServiceException() {
        when(recommendationEngineService.generateRecommendations(
                anyString(), any(), any(), anyString(), any()))
                .thenThrow(new RuntimeException("Service error"));

        String result = tool.generateRecommendations(
                "Workflow", "Feature", "Desc", "my-project", "main");

        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
    }

    @Test
    void shouldHandleNullWorkflowTypeAndBranch() {
        RecommendationReport report = new RecommendationReport();
        report.setWorkflowName("Test Workflow");
        report.setPrioritizedRecommendations(new ArrayList<>());
        report.setErrors(new ArrayList<>());

        when(recommendationEngineService.generateRecommendations(
                anyString(), isNull(), isNull(), anyString(), isNull()))
                .thenReturn(report);

        String result = tool.generateRecommendations(
                "Test Workflow", null, null, "my-project", null);

        assertNotNull(result);
        assertTrue(result.contains("Test Workflow"));
    }

    @Test
    void shouldTrimInputParameters() {
        RecommendationReport report = new RecommendationReport();
        report.setWorkflowName("Test Workflow");
        report.setPrioritizedRecommendations(new ArrayList<>());
        report.setErrors(new ArrayList<>());

        when(recommendationEngineService.generateRecommendations(
                eq("Test Workflow"), eq("Feature"), eq("Desc"),
                eq("my-project"), eq("main")))
                .thenReturn(report);

        String result = tool.generateRecommendations(
                "  Test Workflow  ", "  Feature  ", "  Desc  ",
                "  my-project  ", "  main  ");

        assertNotNull(result);
        assertTrue(result.contains("Test Workflow"));
    }

    @Test
    void shouldSerializeReportWithAllFields() throws JsonProcessingException {
        RecommendationReport report = new RecommendationReport();
        report.setWorkflowName("Full Report");
        report.setWorkflowType("Feature");
        report.setExecutiveSummary("Summary");
        report.setConfidenceScore(90);

        com.projectiq.mcp.recommendation.dto.Recommendation rec =
                new com.projectiq.mcp.recommendation.dto.Recommendation(
                        "REC-0001",
                        RecommendationCategory.TESTING,
                        RecommendationPriority.HIGH,
                        "Add tests",
                        "Add unit tests for new code",
                        "Testing is important"
                );
        rec.getActionItems().add("Write tests");
        report.addRecommendation(rec);

        report.addImplementationAdvice("Implement step by step");
        report.addTestingRecommendation("Run tests");
        report.addArchitecturalGuidance("Follow layering");
        report.addRepositoryBestPractice("Use conventions");

        com.projectiq.mcp.recommendation.dto.RecommendationReport.ReportSummary summary =
                new com.projectiq.mcp.recommendation.dto.RecommendationReport.ReportSummary();
        summary.setTotalRecommendations(1);
        summary.setHighCount(1);
        report.setRecommendationSummary(summary);

        when(recommendationEngineService.generateRecommendations(
                anyString(), any(), any(), anyString(), any()))
                .thenReturn(report);

        String result = tool.generateRecommendations(
                "Full Report", "Feature", "Request", "my-project", "main");

        assertNotNull(result);
        assertTrue(result.contains("Full Report"));
        assertTrue(result.contains("REC-0001"));
        assertTrue(result.contains("Add tests"));
        assertTrue(result.contains("Summary"));
        assertTrue(result.contains("90"));
    }
}