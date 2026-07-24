package com.projectiq.mcp.tools;

import com.projectiq.mcp.integration.dto.EndToEndWorkflowResponse;
import com.projectiq.mcp.integration.service.IntegrationOrchestratorService;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.pipeline.dto.ContextPackage;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.readiness.dto.ReadinessLevel;
import com.projectiq.mcp.readiness.dto.ReadinessReport;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.validation.dto.ValidationReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecuteEndToEndWorkflowToolTest {

    @Mock
    private IntegrationOrchestratorService integrationOrchestratorService;

    private ExecuteEndToEndWorkflowTool tool;

    @BeforeEach
    void setUp() {
        tool = new ExecuteEndToEndWorkflowTool(integrationOrchestratorService);
    }

    @Test
    void testApply_Success() {
        // Arrange
        String request = "Add new feature";
        String repositoryName = "my-project";
        String branch = "main";

        EndToEndWorkflowResponse expectedResponse = new EndToEndWorkflowResponse()
                .withWorkflowId("wf-001")
                .withOriginalRequest(request)
                .withRepositoryName(repositoryName);
        expectedResponse.setOverallStatus("COMPLETED");

        when(integrationOrchestratorService.executeEndToEndWorkflow(request, repositoryName, branch))
                .thenReturn(expectedResponse);

        // Act
        EndToEndWorkflowResponse result = tool.apply(
                new ExecuteEndToEndWorkflowTool.Request(request, repositoryName, branch));

        // Assert
        assertNotNull(result);
        assertEquals("COMPLETED", result.getOverallStatus());
        assertEquals("wf-001", result.getWorkflowId());
    }

    @Test
    void testApply_DefaultBranch() {
        // Arrange
        String request = "Fix bug";
        String repositoryName = "my-project";

        EndToEndWorkflowResponse expectedResponse = new EndToEndWorkflowResponse()
                .withOriginalRequest(request)
                .withRepositoryName(repositoryName);
        expectedResponse.setOverallStatus("COMPLETED");

        when(integrationOrchestratorService.executeEndToEndWorkflow(request, repositoryName, null))
                .thenReturn(expectedResponse);

        // Act - using constructor without branch
        EndToEndWorkflowResponse result = tool.apply(
                new ExecuteEndToEndWorkflowTool.Request(request, repositoryName));

        // Assert
        assertNotNull(result);
        assertEquals("COMPLETED", result.getOverallStatus());
    }

    @Test
    void testApply_EmptyRequest() {
        EndToEndWorkflowResponse result = tool.apply(
                new ExecuteEndToEndWorkflowTool.Request("", "repo", "main"));
        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
    }

    @Test
    void testApply_EmptyRepository() {
        EndToEndWorkflowResponse result = tool.apply(
                new ExecuteEndToEndWorkflowTool.Request("request", "", "main"));
        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
    }

    @Test
    void testApply_NullBranch() {
        // Branch can be null - should use default
        EndToEndWorkflowResponse expectedResponse = new EndToEndWorkflowResponse()
                .withOriginalRequest("request")
                .withRepositoryName("repo");
        expectedResponse.setOverallStatus("COMPLETED");

        when(integrationOrchestratorService.executeEndToEndWorkflow("request", "repo", null))
                .thenReturn(expectedResponse);

        EndToEndWorkflowResponse result = tool.apply(
                new ExecuteEndToEndWorkflowTool.Request("request", "repo", null));
        assertNotNull(result);
        assertEquals("COMPLETED", result.getOverallStatus());
    }

    @Test
    void testApply_IntegrationServiceThrowsException() {
        // Arrange
        when(integrationOrchestratorService.executeEndToEndWorkflow(
                anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        EndToEndWorkflowResponse result = tool.apply(
                new ExecuteEndToEndWorkflowTool.Request("request", "repo", "main"));

        // Assert
        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("Unexpected error"));
    }

    @Test
    void testApply_IntegrationServiceThrowsIllegalArgument() {
        // Arrange
        when(integrationOrchestratorService.executeEndToEndWorkflow(
                anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid repository"));

        // Act
        EndToEndWorkflowResponse result = tool.apply(
                new ExecuteEndToEndWorkflowTool.Request("request", "repo", "main"));

        // Assert
        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("Invalid argument"));
    }

    @Test
    void testRequestRecord_ValidConstruction() {
        ExecuteEndToEndWorkflowTool.Request req =
                new ExecuteEndToEndWorkflowTool.Request("Test request", "test-repo", "develop");
        assertEquals("Test request", req.request());
        assertEquals("test-repo", req.repositoryName());
        assertEquals("develop", req.branch());
    }

    @Test
    void testRequestRecord_NullRequest() {
        assertThrows(IllegalArgumentException.class, () ->
                new ExecuteEndToEndWorkflowTool.Request(null, "repo", "main"));
    }

    @Test
    void testRequestRecord_NullRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                new ExecuteEndToEndWorkflowTool.Request("request", null, "main"));
    }

    @Test
    void testRequestRecord_DefaultBranch() {
        ExecuteEndToEndWorkflowTool.Request req =
                new ExecuteEndToEndWorkflowTool.Request("Test request", "test-repo");
        assertEquals("Test request", req.request());
        assertEquals("test-repo", req.repositoryName());
        assertNull(req.branch());
    }
}