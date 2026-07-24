package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.ContextAssemblyResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
import com.projectiq.mcp.analysis.dto.TaskType;
import com.projectiq.mcp.analysis.service.ContextAssemblyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssembleContextToolTest {

    @Mock
    private ContextAssemblyService contextAssemblyService;

    private AssembleContextTool assembleContextTool;

    @BeforeEach
    void setUp() {
        assembleContextTool = new AssembleContextTool(contextAssemblyService);
    }

    @Test
    void assembleContext_withValidInput_returnsJsonResponse() {
        // Arrange
        ContextAssemblyResponse response = createSampleResponse();
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = assembleContextTool.assembleContext(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Add pagination to UserController"));
        assertTrue(result.contains("originalTask"));
        assertTrue(result.contains("executedTools"));
        assertTrue(result.contains("executionPlan"));

        verify(contextAssemblyService).assembleContext(
                "Add pagination to UserController", "test-repo", "main");
    }

    @Test
    void assembleContext_withNullTask_returnsErrorResponse() {
        // Act
        String result = assembleContextTool.assembleContext(null, "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(contextAssemblyService);
    }

    @Test
    void assembleContext_withEmptyTask_returnsErrorResponse() {
        // Act
        String result = assembleContextTool.assembleContext("", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(contextAssemblyService);
    }

    @Test
    void assembleContext_withBlankTask_returnsErrorResponse() {
        // Act
        String result = assembleContextTool.assembleContext("   ", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        verifyNoInteractions(contextAssemblyService);
    }

    @Test
    void assembleContext_withNullRepositoryName_returnsErrorResponse() {
        // Act
        String result = assembleContextTool.assembleContext("Add pagination", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
        verifyNoInteractions(contextAssemblyService);
    }

    @Test
    void assembleContext_withEmptyRepositoryName_returnsErrorResponse() {
        // Act
        String result = assembleContextTool.assembleContext("Add pagination", "", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
        verifyNoInteractions(contextAssemblyService);
    }

    @Test
    void assembleContext_withBlankRepositoryName_returnsErrorResponse() {
        // Act
        String result = assembleContextTool.assembleContext("Add pagination", "   ", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        verifyNoInteractions(contextAssemblyService);
    }

    @Test
    void assembleContext_withNullBranch_defaultsToMain() {
        // Arrange
        ContextAssemblyResponse response = createSampleResponse();
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = assembleContextTool.assembleContext(
                "Add pagination to UserController", "test-repo", null);

        // Assert
        assertNotNull(result);
        verify(contextAssemblyService).assembleContext(
                "Add pagination to UserController", "test-repo", "main");
    }

    @Test
    void assembleContext_withEmptyBranch_defaultsToMain() {
        // Arrange
        ContextAssemblyResponse response = createSampleResponse();
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = assembleContextTool.assembleContext(
                "Add pagination to UserController", "test-repo", "");

        // Assert
        assertNotNull(result);
        verify(contextAssemblyService).assembleContext(
                "Add pagination to UserController", "test-repo", "main");
    }

    @Test
    void assembleContext_withServiceException_returnsErrorResponse() {
        // Arrange
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Internal error occurred"));

        // Act
        String result = assembleContextTool.assembleContext(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Internal error occurred"));
    }

    @Test
    void assembleContext_withIllegalArgumentException_returnsErrorResponse() {
        // Arrange
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        // Act
        String result = assembleContextTool.assembleContext(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Invalid argument"));
    }

    @Test
    void assembleContext_responseIsValidJson() {
        // Arrange
        ContextAssemblyResponse response = createSampleResponse();
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = assembleContextTool.assembleContext(
                "Add pagination to UserController", "test-repo", "main");

        // Assert - verify it's valid JSON
        assertDoesNotThrow(() -> new ObjectMapper().readTree(result));
    }

    @Test
    void assembleContext_taskIsTrimmedBeforeProcessing() {
        // Arrange
        ContextAssemblyResponse response = createSampleResponse();
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = assembleContextTool.assembleContext(
                "  Add pagination  ", "test-repo", "main");

        // Assert
        assertNotNull(result);
        verify(contextAssemblyService).assembleContext(
                "Add pagination", "test-repo", "main");
    }

    @Test
    void assembleContext_repositoryNameIsTrimmedBeforeProcessing() {
        // Arrange
        ContextAssemblyResponse response = createSampleResponse();
        when(contextAssemblyService.assembleContext(anyString(), anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = assembleContextTool.assembleContext(
                "Add pagination", "  test-repo  ", "main");

        // Assert
        assertNotNull(result);
        verify(contextAssemblyService).assembleContext(
                "Add pagination", "test-repo", "main");
    }

    /**
     * Creates a sample ContextAssemblyResponse for testing.
     */
    private ContextAssemblyResponse createSampleResponse() {
        ContextAssemblyResponse response = new ContextAssemblyResponse();
        response.setOriginalTask("Add pagination to UserController");
        response.setTotalExecutionTimeMillis(1234L);

        TaskAnalysisResponse analysis = new TaskAnalysisResponse();
        analysis.setOriginalTask("Add pagination to UserController");
        analysis.setTaskType(TaskType.NEW_FEATURE);
        response.setTaskAnalysis(analysis);

        List<ExecutionStep> plan = new ArrayList<>();
        plan.add(new ExecutionStep(1, "repository_summary", "Get repository summary"));
        response.setExecutionPlan(plan);

        response.addExecutedTool("repository_summary");
        response.addExecutedTool("search_code");
        response.addExecutedTool("prompt_context");

        response.setExecutionSummary("3 tools executed in 1234ms");

        return response;
    }
}