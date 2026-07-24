package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.validation.dto.ValidationCategory;
import com.projectiq.mcp.validation.dto.ValidationFinding;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.dto.ValidationSeverity;
import com.projectiq.mcp.validation.service.WorkflowValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests for the ValidateWorkflowTool.
 */
@ExtendWith(MockitoExtension.class)
class ValidateWorkflowToolTest {

    @Mock
    private WorkflowValidationService validationService;

    private ValidateWorkflowTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tool = new ValidateWorkflowTool(validationService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldReturnValidationReportForValidWorkflow() throws JsonProcessingException {
        // Arrange
        String stepsJson = "[{\"name\":\"step1\",\"description\":\"First step\",\"category\":\"implementation\"}]";

        ValidationReport mockReport = new ValidationReport();
        mockReport.setOverallStatus("PASSED");
        mockReport.setReadinessScore(95);
        mockReport.setReadinessLabel("READY");
        mockReport.addFinding(new ValidationFinding(
                ValidationCategory.WORKFLOW_VALIDATION,
                ValidationSeverity.INFORMATIONAL,
                "Workflow is valid",
                "All checks passed",
                false));

        when(validationService.validateWorkflow(
                anyString(), any(), any(), anyList(), anyList(), anyString(), any()))
                .thenReturn(mockReport);

        // Act
        String result = tool.validateWorkflow(
                "test-workflow",
                "Feature Implementation",
                "Implement feature",
                stepsJson,
                null,
                "test-repo",
                "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("PASSED"));
        assertTrue(result.contains("READY"));
        assertTrue(result.contains("95"));
    }

    @Test
    void shouldReturnErrorForMissingWorkflowName() {
        // Act
        String result = tool.validateWorkflow(
                "",
                "Feature",
                "Request",
                "[{\"name\":\"step1\",\"description\":\"desc\",\"category\":\"cat\"}]",
                null,
                "test-repo",
                "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Workflow name is required"));
    }

    @Test
    void shouldReturnErrorForMissingRepositoryName() {
        // Act
        String result = tool.validateWorkflow(
                "test-workflow",
                "Feature",
                "Request",
                "[{\"name\":\"step1\",\"description\":\"desc\",\"category\":\"cat\"}]",
                null,
                "",
                "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void shouldReturnErrorForMissingSteps() {
        // Act
        String result = tool.validateWorkflow(
                "test-workflow",
                "Feature",
                "Request",
                null,
                null,
                "test-repo",
                "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Steps JSON is required"));
    }

    @Test
    void shouldReturnErrorForInvalidStepsJson() {
        // Act
        String result = tool.validateWorkflow(
                "test-workflow",
                "Feature",
                "Request",
                "invalid json",
                null,
                "test-repo",
                "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Failed to parse steps JSON"));
    }

    @Test
    void shouldHandleInternalErrors() {
        // Arrange
        String stepsJson = "[{\"name\":\"step1\",\"description\":\"desc\",\"category\":\"cat\"}]";

        when(validationService.validateWorkflow(
                anyString(), any(), any(), anyList(), anyList(), anyString(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String result = tool.validateWorkflow(
                "test-workflow",
                "Feature",
                "Request",
                stepsJson,
                null,
                "test-repo",
                "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
    }

    @Test
    void shouldHandleInvalidDependenciesJson() {
        // Arrange
        String stepsJson = "[{\"name\":\"step1\",\"description\":\"desc\",\"category\":\"cat\"}]";

        // Act
        String result = tool.validateWorkflow(
                "test-workflow",
                "Feature",
                "Request",
                stepsJson,
                "invalid json",
                "test-repo",
                "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Failed to parse dependencies JSON"));
    }
}