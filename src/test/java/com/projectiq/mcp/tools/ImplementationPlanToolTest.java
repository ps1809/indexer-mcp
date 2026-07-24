package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.service.ImplementationPlanningService;
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
class ImplementationPlanToolTest {

    @Mock
    private ImplementationPlanningService planningService;

    private ImplementationPlanTool planTool;

    @BeforeEach
    void setUp() {
        planTool = new ImplementationPlanTool(planningService);
    }

    @Test
    void generateImplementationPlan_withValidInput_returnsJsonResponse() {
        // Arrange
        ImplementationPlanningResponse response = createSampleResponse();
        when(planningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = planTool.generateImplementationPlan(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Add pagination to UserController"));
        assertTrue(result.contains("New Feature"));

        verify(planningService).generatePlan(
                "Add pagination to UserController", "test-repo", "main");
    }

    @Test
    void generateImplementationPlan_withNullTask_returnsErrorResponse() {
        // Act
        String result = planTool.generateImplementationPlan(null, "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));

        verifyNoInteractions(planningService);
    }

    @Test
    void generateImplementationPlan_withEmptyTask_returnsErrorResponse() {
        // Act
        String result = planTool.generateImplementationPlan("", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
    }

    @Test
    void generateImplementationPlan_withNullRepository_returnsErrorResponse() {
        // Act
        String result = planTool.generateImplementationPlan("Add pagination", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void generateImplementationPlan_withEmptyRepository_returnsErrorResponse() {
        // Act
        String result = planTool.generateImplementationPlan("Add pagination", "", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void generateImplementationPlan_withNullBranch_defaultsToMain() {
        // Arrange
        ImplementationPlanningResponse response = createSampleResponse();
        when(planningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = planTool.generateImplementationPlan(
                "Add pagination", "test-repo", null);

        // Assert
        assertNotNull(result);
        verify(planningService).generatePlan(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void generateImplementationPlan_withEmptyBranch_defaultsToMain() {
        // Arrange
        ImplementationPlanningResponse response = createSampleResponse();
        when(planningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = planTool.generateImplementationPlan(
                "Add pagination", "test-repo", "");

        // Assert
        assertNotNull(result);
        verify(planningService).generatePlan(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void generateImplementationPlan_withServiceException_returnsErrorResponse() {
        // Arrange
        when(planningService.generatePlan(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service failure"));

        // Act
        String result = planTool.generateImplementationPlan(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Service failure"));
    }

    @Test
    void generateImplementationPlan_withIllegalArgumentException_returnsInvalidArgumentError() {
        // Arrange
        when(planningService.generatePlan(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid task"));

        // Act
        String result = planTool.generateImplementationPlan(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Invalid task"));
    }

    @Test
    void generateImplementationPlan_response_isValidJson() throws Exception {
        // Arrange
        ImplementationPlanningResponse response = createSampleResponse();
        when(planningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = planTool.generateImplementationPlan(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - verify it's valid JSON and contains expected fields
        assertNotNull(result);
        ObjectMapper mapper = new ObjectMapper();
        assertDoesNotThrow(() -> mapper.readTree(result));

        assertTrue(result.contains("\"originalTask\""));
        assertTrue(result.contains("\"taskType\""));
        assertTrue(result.contains("\"estimatedComplexity\""));
        assertTrue(result.contains("\"recommendedImplementationOrder\""));
        assertTrue(result.contains("\"filesToModify\""));
        assertTrue(result.contains("\"filesToReview\""));
        assertTrue(result.contains("\"componentsAffected\""));
        assertTrue(result.contains("\"dependenciesInvolved\""));
        assertTrue(result.contains("\"suggestedValidationSteps\""));
        assertTrue(result.contains("\"suggestedTestingScope\""));
        assertTrue(result.contains("\"risks\""));
        assertTrue(result.contains("\"assumptions\""));
    }

    @Test
    void generateImplementationPlan_returnsStableOutput() {
        // Arrange
        ImplementationPlanningResponse response = createSampleResponse();
        when(planningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act - run twice
        String result1 = planTool.generateImplementationPlan(
                "Add pagination to UserController", "test-repo", "main");
        String result2 = planTool.generateImplementationPlan(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - both responses should be identical
        assertEquals(result1, result2);
    }

    @Test
    void generateImplementationPlan_withFullPlan_containsAllSections() {
        // Arrange - create a comprehensive response
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setOriginalTask("Implement user authentication");
        response.setTaskType("New Feature");
        response.setEstimatedComplexity("HIGH");

        response.addRecommendedStep("1. Analyze the repository structure");
        response.addRecommendedStep("2. Implement the feature");

        response.addFileToModify("UserController.java");
        response.addFileToModify("UserService.java");

        response.addFileToReview("UserRepository.java");
        response.addFileToReview("Review existing tests for all affected components");

        response.addComponentAffected("UserController (Class)");
        response.addComponentAffected("UserService (Class)");

        response.addDependencyInvolved("Spring Security");
        response.addDependencyInvolved("Internal module dependencies");

        response.addValidationStep("Verify the changes compile without errors");
        response.addValidationStep("Run unit tests for the affected components");

        response.setSuggestedTestingScope("Medium scope: Unit tests and integration tests");

        response.addRisk("Security-related changes have high impact [Mitigation: Perform security review]");
        response.addAssumption("Feature requirements are fully specified in the task description");

        when(planningService.generatePlan(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = planTool.generateImplementationPlan(
                "Implement user authentication", "test-repo", "main");

        // Assert - verify JSON contains all expected sections
        assertNotNull(result);
        assertTrue(result.contains("\"originalTask\""));
        assertTrue(result.contains("\"taskType\""));
        assertTrue(result.contains("\"estimatedComplexity\""));
        assertTrue(result.contains("\"recommendedImplementationOrder\""));
        assertTrue(result.contains("\"filesToModify\""));
        assertTrue(result.contains("\"filesToReview\""));
        assertTrue(result.contains("\"componentsAffected\""));
        assertTrue(result.contains("\"dependenciesInvolved\""));
        assertTrue(result.contains("\"suggestedValidationSteps\""));
        assertTrue(result.contains("\"suggestedTestingScope\""));
        assertTrue(result.contains("\"risks\""));
        assertTrue(result.contains("\"assumptions\""));

        // Verify expected values
        assertTrue(result.contains("Implement user authentication"));
        assertTrue(result.contains("New Feature"));
        assertTrue(result.contains("HIGH"));
        assertTrue(result.contains("UserController"));
        assertTrue(result.contains("Spring Security"));
    }

    /**
     * Creates a sample ImplementationPlanningResponse for testing.
     */
    private ImplementationPlanningResponse createSampleResponse() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setOriginalTask("Add pagination to UserController");
        response.setTaskType("New Feature");
        response.setEstimatedComplexity("MEDIUM");

        response.addRecommendedStep("1. Analyze the repository structure to understand existing patterns");
        response.addRecommendedStep("2. Search the codebase for similar feature implementations");

        response.addFileToModify("UserController class");
        response.addFileToModify("UserService class");

        response.addFileToReview("UserRepository class");
        response.addFileToReview("Review existing tests for all affected components");

        response.addComponentAffected("UserController (Class)");
        response.addComponentAffected("UserService (Class)");

        response.addDependencyInvolved("Internal module dependencies");

        response.addValidationStep("Verify the changes compile without errors");

        response.setSuggestedTestingScope("Medium scope: Unit tests and integration tests");

        response.addRisk("Test risk [Mitigation: Test mitigation]");

        response.addAssumption("Feature requirements are fully specified in the task description");

        return response;
    }
}