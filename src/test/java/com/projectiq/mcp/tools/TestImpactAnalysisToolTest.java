package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.TestImpactAnalysisResponse;
import com.projectiq.mcp.analysis.service.TestImpactAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestImpactAnalysisToolTest {

    @Mock
    private TestImpactAnalysisService testImpactAnalysisService;

    private TestImpactAnalysisTool tool;

    @BeforeEach
    void setUp() {
        tool = new TestImpactAnalysisTool(testImpactAnalysisService);
    }

    @Test
    void analyzeTestImpact_withValidRequest_returnsJsonResponse() {
        // Arrange
        TestImpactAnalysisResponse response = createResponse("Add pagination to UserController");
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeTestImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Add pagination to UserController"));
        assertTrue(result.contains("affectedProductionClasses"));
        assertTrue(result.contains("relatedTestClasses"));
        assertTrue(result.contains("recommendedTestExecutionOrder"));
        assertTrue(result.contains("estimatedTestingEffort"));
        assertTrue(result.contains("confidenceLevel"));
        assertTrue(result.contains("testingRationale"));
    }

    @Test
    void analyzeTestImpact_withEmptyTask_returnsError() {
        // Act
        String result = tool.analyzeTestImpact("", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
    }

    @Test
    void analyzeTestImpact_withNullTask_returnsError() {
        // Act
        String result = tool.analyzeTestImpact(null, "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
    }

    @Test
    void analyzeTestImpact_withEmptyRepository_returnsError() {
        // Act
        String result = tool.analyzeTestImpact("Add pagination", "", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeTestImpact_withNullRepository_returnsError() {
        // Act
        String result = tool.analyzeTestImpact("Add pagination", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeTestImpact_withNullBranch_usesDefault() {
        // Arrange
        TestImpactAnalysisResponse response = createResponse("Add pagination");
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeTestImpact("Add pagination", "test-repo", null);

        // Assert
        assertNotNull(result);
        verify(testImpactAnalysisService).analyzeTestImpact("Add pagination", "test-repo", "main");
    }

    @Test
    void analyzeTestImpact_withServiceException_returnsError() {
        // Arrange
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        String result = tool.analyzeTestImpact("Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
    }

    @Test
    void analyzeTestImpact_withIllegalArgumentException_returnsError() {
        // Arrange
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        // Act
        String result = tool.analyzeTestImpact("Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void analyzeTestImpact_withFeatureRequest_returnsCompleteReport() {
        // Arrange
        TestImpactAnalysisResponse response = createResponse("Add pagination to UserController");
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeTestImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("originalTask"));
        assertTrue(result.contains("affectedProductionClasses"));
        assertTrue(result.contains("relatedTestClasses"));
        assertTrue(result.contains("missingTests"));
        assertTrue(result.contains("recommendedTestExecutionOrder"));
        assertTrue(result.contains("estimatedTestingEffort"));
        assertTrue(result.contains("confidenceLevel"));
        assertTrue(result.contains("testingRationale"));
    }

    @Test
    void analyzeTestImpact_withBugFix_returnsBugFixReport() {
        // Arrange
        TestImpactAnalysisResponse response = createResponse("Fix null pointer in UserService");
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeTestImpact(
                "Fix null pointer in UserService", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Fix null pointer in UserService"));
    }

    @Test
    void analyzeTestImpact_withRefactoring_returnsRefactoringReport() {
        // Arrange
        TestImpactAnalysisResponse response = createResponse("Refactor UserService");
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeTestImpact(
                "Refactor UserService", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Refactor UserService"));
    }

    @Test
    void analyzeTestImpact_withRestApiChange_returnsApiReport() {
        // Arrange
        TestImpactAnalysisResponse response = createResponse("Add new endpoint to UserController");
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeTestImpact(
                "Add new endpoint to UserController", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Add new endpoint to UserController"));
    }

    @Test
    void analyzeTestImpact_withRepositoryChange_returnsRepoReport() {
        // Arrange
        TestImpactAnalysisResponse response = createResponse("Add new field to UserEntity");
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeTestImpact(
                "Add new field to UserEntity", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Add new field to UserEntity"));
    }

    @Test
    void analyzeTestImpact_deterministicOutput_returnsSameResult() {
        // Arrange
        TestImpactAnalysisResponse response = createResponse("Add pagination to UserController");
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String first = tool.analyzeTestImpact("Add pagination to UserController", "test-repo", "main");
        String second = tool.analyzeTestImpact("Add pagination to UserController", "test-repo", "main");

        // Assert
        assertEquals(first, second);
    }

    // --- Helper methods ---

    private TestImpactAnalysisResponse createResponse(String task) {
        TestImpactAnalysisResponse response = new TestImpactAnalysisResponse();
        response.setOriginalTask(task);
        response.setAffectedProductionClasses(List.of("UserController (Controller)"));
        response.setRelatedTestClasses(List.of("UserControllerTest"));
        response.setMissingTests(List.of("Missing: UserControllerTest (may need to be created)"));
        response.setRecommendedTestExecutionOrder(List.of(
                "1. Unit tests for new feature components",
                "2. Integration tests for new feature integration",
                "Final verification: Run full test suite to check for regressions"
        ));
        response.setEstimatedTestingEffort("Medium");
        response.setConfidenceLevel("High");
        response.setTestingRationale("Test impact analysis for new feature task.");
        return response;
    }
}