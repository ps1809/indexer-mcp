package com.projectiq.mcp.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.readiness.dto.ReadinessLevel;
import com.projectiq.mcp.readiness.dto.ReadinessReport;
import com.projectiq.mcp.readiness.service.ExecutionReadinessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the AssessExecutionReadinessTool.
 */
@ExtendWith(MockitoExtension.class)
class AssessExecutionReadinessToolTest {

    @Mock
    private ExecutionReadinessService executionReadinessService;

    private AssessExecutionReadinessTool tool;

    @BeforeEach
    void setUp() {
        tool = new AssessExecutionReadinessTool(executionReadinessService);
    }

    @Test
    void testMissingWorkflowName() {
        String result = tool.assessExecutionReadiness(null, "Feature", "Request", "repo", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Workflow name is required"));
    }

    @Test
    void testEmptyWorkflowName() {
        String result = tool.assessExecutionReadiness("", "Feature", "Request", "repo", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Workflow name is required"));
    }

    @Test
    void testMissingRepositoryName() {
        String result = tool.assessExecutionReadiness("workflow", "Feature", "Request", null, "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testEmptyRepositoryName() {
        String result = tool.assessExecutionReadiness("workflow", "Feature", "Request", "", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testSuccessfulAssessment() {
        ReadinessReport report = new ReadinessReport();
        report.setWorkflowName("test-workflow");
        report.setWorkflowType("Feature Implementation");
        report.setOverallReadinessLevel(ReadinessLevel.READY);
        report.setReadinessScore(95);
        report.setFinalImplementationRecommendation("IMPLEMENTATION APPROVED");

        when(executionReadinessService.assessReadiness(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(report);

        String result = tool.assessExecutionReadiness(
                "test-workflow", "Feature Implementation", "Implement feature",
                "test-repo", "main");

        assertNotNull(result);
        assertTrue(result.contains("test-workflow"));
        assertTrue(result.contains("READY"));
        assertTrue(result.contains("95"));
    }

    @Test
    void testServiceThrowsException() {
        when(executionReadinessService.assessReadiness(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service failure"));

        String result = tool.assessExecutionReadiness(
                "test-workflow", "Feature Implementation", null,
                "test-repo", "main");

        assertTrue(result.contains("INTERNAL_ERROR"));
    }
}