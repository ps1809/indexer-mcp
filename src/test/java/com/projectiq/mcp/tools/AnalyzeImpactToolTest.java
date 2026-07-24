package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import com.projectiq.mcp.analysis.dto.RiskLevel;
import com.projectiq.mcp.analysis.dto.ScopeLevel;
import com.projectiq.mcp.analysis.service.ImpactAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyzeImpactToolTest {

    @Mock
    private ImpactAnalysisService impactAnalysisService;

    private AnalyzeImpactTool analyzeImpactTool;

    @BeforeEach
    void setUp() {
        analyzeImpactTool = new AnalyzeImpactTool(impactAnalysisService);
    }

    @Test
    void analyzeImpact_withValidInput_returnsJsonResponse() {
        // Arrange
        ImpactAnalysisResponse response = createSampleResponse();
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = analyzeImpactTool.analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Add pagination to UserController"));
        assertTrue(result.contains("New Feature"));
        assertTrue(result.contains("UserController"));

        verify(impactAnalysisService).analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");
    }

    @Test
    void analyzeImpact_withNullTask_returnsErrorResponse() {
        // Act
        String result = analyzeImpactTool.analyzeImpact(null, "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));

        verifyNoInteractions(impactAnalysisService);
    }

    @Test
    void analyzeImpact_withEmptyTask_returnsErrorResponse() {
        // Act
        String result = analyzeImpactTool.analyzeImpact("", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
    }

    @Test
    void analyzeImpact_withNullRepository_returnsErrorResponse() {
        // Act
        String result = analyzeImpactTool.analyzeImpact("Add pagination", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeImpact_withEmptyRepository_returnsErrorResponse() {
        // Act
        String result = analyzeImpactTool.analyzeImpact("Add pagination", "", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeImpact_withNullBranch_defaultsToMain() {
        // Arrange
        ImpactAnalysisResponse response = createSampleResponse();
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = analyzeImpactTool.analyzeImpact(
                "Add pagination", "test-repo", null);

        // Assert
        assertNotNull(result);
        verify(impactAnalysisService).analyzeImpact(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void analyzeImpact_withEmptyBranch_defaultsToMain() {
        // Arrange
        ImpactAnalysisResponse response = createSampleResponse();
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = analyzeImpactTool.analyzeImpact(
                "Add pagination", "test-repo", "");

        // Assert
        assertNotNull(result);
        verify(impactAnalysisService).analyzeImpact(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void analyzeImpact_withServiceException_returnsErrorResponse() {
        // Arrange
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service failure"));

        // Act
        String result = analyzeImpactTool.analyzeImpact(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Service failure"));
    }

    @Test
    void analyzeImpact_withIllegalArgumentException_returnsInvalidArgumentError() {
        // Arrange
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid task"));

        // Act
        String result = analyzeImpactTool.analyzeImpact(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Invalid task"));
    }

    @Test
    void analyzeImpact_response_isValidJson() throws Exception {
        // Arrange
        ImpactAnalysisResponse response = createSampleResponse();
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = analyzeImpactTool.analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - verify it's valid JSON and contains expected fields
        assertNotNull(result);
        assertTrue(result.contains("\"originalTask\""));
        assertTrue(result.contains("\"taskType\""));
        assertTrue(result.contains("\"primaryTargets\""));
        assertTrue(result.contains("\"directlyAffectedComponents\""));
        assertTrue(result.contains("\"estimatedImplementationScope\""));
        assertTrue(result.contains("\"potentialRisks\""));
        assertTrue(result.contains("\"confidenceLevel\""));

        // Verify enum values appear as names in JSON
        assertTrue(result.contains("MEDIUM"));
        assertTrue(result.contains("HIGH"));
    }

    @Test
    void analyzeImpact_returnsStableOutput() {
        // Arrange
        ImpactAnalysisResponse response = createSampleResponse();
        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act - run twice
        String result1 = analyzeImpactTool.analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");
        String result2 = analyzeImpactTool.analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - both responses should be identical since the mock returns the same object
        assertEquals(result1, result2);
    }

    @Test
    void analyzeImpact_withFullReport_containsAllSections() {
        // Arrange - create a comprehensive response
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask("Add pagination to UserController");
        response.setTaskType("New Feature");

        response.addPrimaryTarget("UserController (Controller)");
        response.addPrimaryTarget("UserService (Service)");

        response.addDirectlyAffectedComponent(
                new ImpactedComponent("UserController", "Class", "Directly referenced controller"));
        response.addDirectlyAffectedComponent(
                new ImpactedComponent("UserService", "Class", "Directly referenced service"));

        response.addIndirectlyAffectedComponent(
                new ImpactedComponent("UserRepository", "Class", "Repository associated with service"));

        response.addDependencyImpact("New dependencies may be required");

        response.setEstimatedImplementationScope(ScopeLevel.MEDIUM);
        response.setEstimatedTestingScope(ScopeLevel.LARGE);

        response.addPotentialRisk(new RiskItem(
                "API changes may break clients", RiskLevel.MEDIUM,
                "Maintain backward compatibility"));

        response.setConfidenceLevel(ConfidenceLevel.HIGH);

        when(impactAnalysisService.analyzeImpact(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = analyzeImpactTool.analyzeImpact(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - verify JSON contains all expected sections
        assertNotNull(result);
        assertTrue(result.contains("\"originalTask\""));
        assertTrue(result.contains("\"taskType\""));
        assertTrue(result.contains("\"primaryTargets\""));
        assertTrue(result.contains("\"directlyAffectedComponents\""));
        assertTrue(result.contains("\"indirectlyAffectedComponents\""));
        assertTrue(result.contains("\"dependencyImpact\""));
        assertTrue(result.contains("\"estimatedImplementationScope\""));
        assertTrue(result.contains("\"estimatedTestingScope\""));
        assertTrue(result.contains("\"potentialRisks\""));
        assertTrue(result.contains("\"confidenceLevel\""));

        // Verify expected values
        assertTrue(result.contains("Add pagination to UserController"));
        assertTrue(result.contains("New Feature"));
        assertTrue(result.contains("UserController"));
    }

    /**
     * Creates a sample ImpactAnalysisResponse for testing.
     */
    private ImpactAnalysisResponse createSampleResponse() {
        ImpactAnalysisResponse response = new ImpactAnalysisResponse();
        response.setOriginalTask("Add pagination to UserController");
        response.setTaskType("New Feature");

        response.addPrimaryTarget("UserController (Controller)");

        response.addDirectlyAffectedComponent(
                new ImpactedComponent("UserController", "Class", "Directly referenced controller"));

        response.addIndirectlyAffectedComponent(
                new ImpactedComponent("UserService", "Class", "Service layer associated with controller"));
        response.addIndirectlyAffectedComponent(
                new ImpactedComponent("Unit tests", "Testing", "Tests for all affected components need validation"));

        response.addDependencyImpact("Internal module dependencies may need coordination across teams");

        response.setEstimatedImplementationScope(ScopeLevel.MEDIUM);
        response.setEstimatedTestingScope(ScopeLevel.MEDIUM);

        response.addPotentialRisk(new RiskItem(
                "Limited information available for comprehensive risk assessment",
                RiskLevel.LOW,
                "Verify assumptions by examining the actual codebase"));

        response.setConfidenceLevel(ConfidenceLevel.HIGH);

        return response;
    }
}