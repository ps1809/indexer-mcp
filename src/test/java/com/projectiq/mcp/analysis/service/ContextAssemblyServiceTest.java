package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ContextAssemblyResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
import com.projectiq.mcp.analysis.dto.TaskType;
import com.projectiq.mcp.client.dto.BuildContextRequest;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.PromptContext;
import com.projectiq.mcp.client.dto.RepositoryContext;
import com.projectiq.mcp.client.service.DevelopmentContextService;
import com.projectiq.mcp.client.service.PromptContextService;
import com.projectiq.mcp.client.service.RepositoryContextBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextAssemblyServiceTest {

    @Mock
    private TaskAnalysisService taskAnalysisService;

    @Mock
    private RepositoryContextBuilderService contextBuilderService;

    @Mock
    private DevelopmentContextService developmentContextService;

    @Mock
    private PromptContextService promptContextService;

    private ContextAssemblyService contextAssemblyService;

    @BeforeEach
    void setUp() {
        contextAssemblyService = new ContextAssemblyService(
                taskAnalysisService,
                contextBuilderService,
                developmentContextService,
                promptContextService
        );
    }

    @Test
    void assembleContext_withValidInput_returnsCompleteResponse() {
        // Arrange
        TaskAnalysisResponse analysis = createSampleAnalysis();
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setRepositoryName("test-repo");
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setRepositoryName("test-repo");
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(devContext);

        PromptContext promptContext = new PromptContext();
        promptContext.setTask("Add pagination");
        when(promptContextService.createPromptContext(any())).thenReturn(promptContext);

        // Act
        ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                "Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Add pagination to UserController", response.getOriginalTask());
        assertNotNull(response.getTaskAnalysis());
        assertNotNull(response.getExecutionPlan());
        assertFalse(response.getExecutionPlan().isEmpty());
        assertNotNull(response.getExecutedTools());
        assertFalse(response.getExecutedTools().isEmpty());
        assertNotNull(response.getRepositoryContext());
        assertNotNull(response.getDevelopmentContext());
        assertNotNull(response.getExecutionSummary());
        assertTrue(response.getTotalExecutionTimeMillis() >= 0);

        verify(taskAnalysisService).analyze("Add pagination to UserController");
        verify(contextBuilderService).buildContext(any());
        verify(developmentContextService).createDevelopmentContext(any());
        verify(promptContextService).createPromptContext(any());
    }

    @Test
    void assembleContext_withEmptyExecutionPlan_returnsEarlyResponse() {
        // Arrange
        TaskAnalysisResponse analysis = new TaskAnalysisResponse();
        analysis.setOriginalTask("Simple task");
        analysis.setExecutionPlan(new ArrayList<>());
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                "Simple task", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertEquals("Simple task", response.getOriginalTask());
        assertNotNull(response.getTaskAnalysis());
        assertTrue(response.getExecutionPlan().isEmpty());
        assertTrue(response.getExecutedTools().isEmpty());
        assertNull(response.getRepositoryContext());
        assertNull(response.getDevelopmentContext());
        assertNotNull(response.getExecutionSummary());
        assertTrue(response.getTotalExecutionTimeMillis() >= 0);

        verify(taskAnalysisService).analyze("Simple task");
        verifyNoInteractions(contextBuilderService);
        verifyNoInteractions(developmentContextService);
        verifyNoInteractions(promptContextService);
    }

    @Test
    void assembleContext_withNullExecutionPlan_returnsEarlyResponse() {
        // Arrange
        TaskAnalysisResponse analysis = new TaskAnalysisResponse();
        analysis.setOriginalTask("Simple task");
        analysis.setExecutionPlan(null);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        // Act
        ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                "Simple task", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertTrue(response.getExecutionPlan().isEmpty());
        assertTrue(response.getExecutedTools().isEmpty());
        assertNull(response.getRepositoryContext());
    }

    @Test
    void assembleContext_withIndexerFailure_recordsFailedTools() {
        // Arrange
        TaskAnalysisResponse analysis = createSampleAnalysis();
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        when(contextBuilderService.buildContext(any()))
                .thenThrow(new RuntimeException("Indexer unreachable"));

        // Act
        ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertNotNull(response.getTaskAnalysis());
        assertFalse(response.getFailedTools().isEmpty());
        assertTrue(response.getFailedTools().stream()
                .anyMatch(f -> f.contains("Indexer unreachable")));
        assertNull(response.getRepositoryContext());
        assertNull(response.getDevelopmentContext());
        assertNotNull(response.getExecutionSummary());
    }

    @Test
    void assembleContext_withDevelopmentContextFailure_recordsFailedTool() {
        // Arrange
        TaskAnalysisResponse analysis = createSampleAnalysis();
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setRepositoryName("test-repo");
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        when(developmentContextService.createDevelopmentContext(any()))
                .thenThrow(new RuntimeException("Development context error"));

        // Act
        ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(response);
        assertNotNull(response.getRepositoryContext());
        assertNull(response.getDevelopmentContext());
        assertTrue(response.getFailedTools().stream()
                .anyMatch(f -> f.contains("development_context")));
    }

    @Test
    void assembleContext_eliminatesDuplicateTools() {
        // Arrange
        TaskAnalysisResponse analysis = new TaskAnalysisResponse();
        analysis.setOriginalTask("Add pagination");
        // Create a plan with duplicate tool names
        List<ExecutionStep> plan = new ArrayList<>();
        plan.add(new ExecutionStep(1, "repository_summary", "Get summary"));
        plan.add(new ExecutionStep(2, "repository_summary", "Get summary again (duplicate)"));
        plan.add(new ExecutionStep(3, "search_code", "Search code"));
        analysis.setExecutionPlan(plan);
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setRepositoryName("test-repo");
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        // Act
        ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                "Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(response);
        // repository_summary should only appear once in executed tools
        long summaryCount = response.getExecutedTools().stream()
                .filter(t -> t.equals("repository_summary"))
                .count();
        assertEquals(1, summaryCount);
    }

    @Test
    void assembleContext_withNullBranch_defaultsToMain() {
        // Arrange
        TaskAnalysisResponse analysis = createSampleAnalysis();
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setRepositoryName("test-repo");
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setRepositoryName("test-repo");
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(devContext);

        PromptContext promptContext = new PromptContext();
        promptContext.setTask("Add pagination");
        when(promptContextService.createPromptContext(any())).thenReturn(promptContext);

        // Act
        ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                "Add pagination", "test-repo", null);

        // Assert
        assertNotNull(response);
        verify(contextBuilderService).buildContext(any());
    }

    @Test
    void assembleContext_withEmptyBranch_defaultsToMain() {
        // Arrange
        TaskAnalysisResponse analysis = createSampleAnalysis();
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setRepositoryName("test-repo");
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setRepositoryName("test-repo");
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(devContext);

        PromptContext promptContext = new PromptContext();
        promptContext.setTask("Add pagination");
        when(promptContextService.createPromptContext(any())).thenReturn(promptContext);

        // Act
        ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                "Add pagination", "test-repo", "");

        // Assert
        assertNotNull(response);
        verify(contextBuilderService).buildContext(any());
    }

    @Test
    void assembleContext_returnsDeterministicOrdering() {
        // Arrange
        TaskAnalysisResponse analysis = createSampleAnalysis();
        when(taskAnalysisService.analyze(anyString())).thenReturn(analysis);

        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setRepositoryName("test-repo");
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setRepositoryName("test-repo");
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(devContext);

        PromptContext promptContext = new PromptContext();
        promptContext.setTask("Add pagination");
        when(promptContextService.createPromptContext(any())).thenReturn(promptContext);

        // Act - run twice
        ContextAssemblyResponse response1 = contextAssemblyService.assembleContext(
                "Add pagination", "test-repo", "main");
        ContextAssemblyResponse response2 = contextAssemblyService.assembleContext(
                "Add pagination", "test-repo", "main");

        // Assert - both responses should have the same structure
        assertEquals(response1.getExecutedTools().size(), response2.getExecutedTools().size());
        assertEquals(response1.getExecutionPlan().size(), response2.getExecutionPlan().size());
    }

    @Test
    void assembleContext_withTaskAnalysisFailure_propagatesException() {
        // Arrange
        when(taskAnalysisService.analyze(anyString()))
                .thenThrow(new IllegalArgumentException("Task cannot be empty"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                contextAssemblyService.assembleContext("", "test-repo", "main"));
    }

    /**
     * Creates a sample TaskAnalysisResponse with a realistic execution plan.
     */
    private TaskAnalysisResponse createSampleAnalysis() {
        TaskAnalysisResponse analysis = new TaskAnalysisResponse();
        analysis.setOriginalTask("Add pagination to UserController");
        analysis.setTaskType(TaskType.NEW_FEATURE);

        List<ExecutionStep> plan = new ArrayList<>();
        plan.add(new ExecutionStep(1, "repository_summary",
                "Obtain a high-level overview of the repository structure"));
        plan.add(new ExecutionStep(2, "search_code",
                "Search the codebase for files related to the task"));
        plan.add(new ExecutionStep(3, "find_spring_component",
                "Identify Spring-managed components"));
        plan.add(new ExecutionStep(4, "find_class",
                "Locate specific classes and their metadata"));
        plan.add(new ExecutionStep(5, "find_method",
                "Find method signatures and implementations"));
        plan.add(new ExecutionStep(6, "find_rest_api",
                "Discover REST API endpoints"));
        plan.add(new ExecutionStep(7, "list_related_files",
                "List files related to the detected entities"));
        plan.add(new ExecutionStep(8, "prompt_context",
                "Generate a consolidated AI-ready prompt context"));
        analysis.setExecutionPlan(plan);

        return analysis;
    }
}