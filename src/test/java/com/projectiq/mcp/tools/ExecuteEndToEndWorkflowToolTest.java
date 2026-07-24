package com.projectiq.mcp.tools;

import com.projectiq.mcp.integration.dto.EndToEndWorkflowResponse;
import com.projectiq.mcp.integration.service.IntegrationOrchestratorService;
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
    void testExecuteEndToEndWorkflow_Success() {
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

        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow(request, repositoryName, branch);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getOverallStatus());
        assertEquals("wf-001", result.getWorkflowId());
    }

    @Test
    void testExecuteEndToEndWorkflow_DefaultBranch() {
        String request = "Fix bug";
        String repositoryName = "my-project";

        EndToEndWorkflowResponse expectedResponse = new EndToEndWorkflowResponse()
                .withOriginalRequest(request)
                .withRepositoryName(repositoryName);
        expectedResponse.setOverallStatus("COMPLETED");

        when(integrationOrchestratorService.executeEndToEndWorkflow(request, repositoryName, null))
                .thenReturn(expectedResponse);

        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow(request, repositoryName, null);

        assertNotNull(result);
        assertEquals("COMPLETED", result.getOverallStatus());
    }

    @Test
    void testExecuteEndToEndWorkflow_EmptyRequest() {
        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow("", "repo", "main");
        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
    }

    @Test
    void testExecuteEndToEndWorkflow_EmptyRepository() {
        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow("request", "", "main");
        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
    }

    @Test
    void testExecuteEndToEndWorkflow_NullBranch() {
        EndToEndWorkflowResponse expectedResponse = new EndToEndWorkflowResponse()
                .withOriginalRequest("request")
                .withRepositoryName("repo");
        expectedResponse.setOverallStatus("COMPLETED");

        when(integrationOrchestratorService.executeEndToEndWorkflow("request", "repo", null))
                .thenReturn(expectedResponse);

        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow("request", "repo", null);
        assertNotNull(result);
        assertEquals("COMPLETED", result.getOverallStatus());
    }

    @Test
    void testExecuteEndToEndWorkflow_IntegrationServiceThrowsException() {
        when(integrationOrchestratorService.executeEndToEndWorkflow(
                anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow("request", "repo", "main");

        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("Unexpected error"));
    }

    @Test
    void testExecuteEndToEndWorkflow_IntegrationServiceThrowsIllegalArgument() {
        when(integrationOrchestratorService.executeEndToEndWorkflow(
                anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid repository"));

        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow("request", "repo", "main");

        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("Invalid argument"));
    }

    @Test
    void testExecuteEndToEndWorkflow_NullRequest() {
        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow(null, "repo", "main");
        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
    }

    @Test
    void testExecuteEndToEndWorkflow_NullRepository() {
        EndToEndWorkflowResponse result = tool.executeEndToEndWorkflow("request", null, "main");
        assertNotNull(result);
        assertEquals("FAILED", result.getOverallStatus());
        assertTrue(result.hasErrors());
    }
}