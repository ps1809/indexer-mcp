package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestrateWorkflowToolTest {

    @Mock
    private WorkflowOrchestratorService workflowOrchestratorService;

    private OrchestrateWorkflowTool tool;

    @BeforeEach
    void setUp() {
        tool = new OrchestrateWorkflowTool(workflowOrchestratorService);
    }

    @Test
    void testOrchestrateWorkflowSuccess() {
        // Arrange
        String request = "Add pagination to UserController";
        String repositoryName = "test-repo";
        WorkflowResult mockResult = new WorkflowResult();
        mockResult.setOriginalRequest(request);
        mockResult.setWorkflowType("Feature Implementation");
        mockResult.setExecutionStatus("COMPLETED");
        mockResult.setTotalDurationMillis(500);

        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), anyString()))
                .thenReturn(mockResult);

        // Act
        String response = tool.orchestrateWorkflow(request, repositoryName, "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Feature Implementation"));
        assertTrue(response.contains(request));
        assertTrue(response.contains("COMPLETED"));
    }

    @Test
    void testOrchestrateWorkflowWithDefaultBranch() {
        // Arrange
        String request = "Add pagination to UserController";
        String repositoryName = "test-repo";
        WorkflowResult mockResult = new WorkflowResult();
        mockResult.setOriginalRequest(request);
        mockResult.setWorkflowType("Feature Implementation");

        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), eq("main")))
                .thenReturn(mockResult);

        // Act
        String response = tool.orchestrateWorkflow(request, repositoryName, null);

        // Assert
        assertNotNull(response);
        verify(workflowOrchestratorService).orchestrate(request.trim(), repositoryName.trim(), "main");
    }

    @Test
    void testOrchestrateWorkflowWithNullRequest() {
        // Act
        String response = tool.orchestrateWorkflow(null, "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("INVALID_ARGUMENT"));
        assertTrue(response.contains("Developer request is required"));
        verifyNoInteractions(workflowOrchestratorService);
    }

    @Test
    void testOrchestrateWorkflowWithEmptyRequest() {
        // Act
        String response = tool.orchestrateWorkflow("", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testOrchestrateWorkflowWithNullRepositoryName() {
        // Act
        String response = tool.orchestrateWorkflow("Add pagination", null, "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("INVALID_ARGUMENT"));
        assertTrue(response.contains("Repository name is required"));
    }

    @Test
    void testOrchestrateWorkflowWithEmptyRepositoryName() {
        // Act
        String response = tool.orchestrateWorkflow("Add pagination", "", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testOrchestrateWorkflowHandlesServiceException() {
        // Arrange
        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String response = tool.orchestrateWorkflow("Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("INTERNAL_ERROR"));
    }

    @Test
    void testOrchestrateWorkflowHandlesIllegalArgumentException() {
        // Arrange
        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid request"));

        // Act
        String response = tool.orchestrateWorkflow("Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testOrchestrateWorkflowResultIsValidJson() {
        // Arrange
        String request = "Add pagination to UserController";
        String repositoryName = "test-repo";
        WorkflowResult mockResult = new WorkflowResult();
        mockResult.setOriginalRequest(request);
        mockResult.setWorkflowType("Bug Fix");
        mockResult.setExecutionStatus("COMPLETED");
        mockResult.addCompletedStep(new WorkflowResult.StepResult(1, "analyze_task", "Analyze task", "COMPLETED"));
        mockResult.addRepositoryInsight("Architecture style: Layered Architecture");
        mockResult.addRisk("Test risk [Level: LOW]");

        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), anyString()))
                .thenReturn(mockResult);

        // Act
        String response = tool.orchestrateWorkflow(request, repositoryName, "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.startsWith("{"));
        assertTrue(response.endsWith("}") || response.endsWith("}\n"));
    }
}