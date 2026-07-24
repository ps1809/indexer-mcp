package com.projectiq.mcp.tools;

import com.projectiq.mcp.analysis.dto.ComplexityLevel;
import com.projectiq.mcp.analysis.dto.ConfidenceLevel;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskType;
import com.projectiq.mcp.analysis.service.TaskAnalysisService;
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
class AnalyzeTaskToolTest {

    @Mock
    private TaskAnalysisService taskAnalysisService;

    private AnalyzeTaskTool tool;

    @BeforeEach
    void setUp() {
        tool = new AnalyzeTaskTool(taskAnalysisService);
    }

    @Test
    void analyzeTask_withValidRequest_returnsJsonResponse() {
        // Arrange
        TaskAnalysisResponse mockResponse = createSampleResponse();
        when(taskAnalysisService.analyze(anyString())).thenReturn(mockResponse);

        // Act
        String result = tool.analyzeTask("Add pagination to UserController");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Add pagination to UserController"));
        assertTrue(result.contains("NEW_FEATURE"));
        assertTrue(result.contains("repository_summary"));
        assertTrue(result.contains("executionPlan"));
        verify(taskAnalysisService).analyze("Add pagination to UserController");
    }

    @Test
    void analyzeTask_withNullTask_returnsError() {
        // Act
        String result = tool.analyzeTask(null);

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(taskAnalysisService);
    }

    @Test
    void analyzeTask_withEmptyTask_returnsError() {
        // Act
        String result = tool.analyzeTask("");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(taskAnalysisService);
    }

    @Test
    void analyzeTask_withWhitespaceTask_returnsError() {
        // Act
        String result = tool.analyzeTask("   ");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(taskAnalysisService);
    }

    @Test
    void analyzeTask_withServiceException_returnsError() {
        // Arrange
        when(taskAnalysisService.analyze(anyString())).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String result = tool.analyzeTask("Add pagination");

        // Assert
        assertTrue(result.contains("INTERNAL_ERROR"));
        verify(taskAnalysisService).analyze(anyString());
    }

    @Test
    void analyzeTask_withIllegalArgumentException_returnsError() {
        // Arrange
        when(taskAnalysisService.analyze(anyString())).thenThrow(new IllegalArgumentException("Invalid task"));

        // Act
        String result = tool.analyzeTask("Add pagination");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void analyzeTask_responseContainsAllRequiredFields() {
        // Arrange
        TaskAnalysisResponse mockResponse = createSampleResponse();
        when(taskAnalysisService.analyze(anyString())).thenReturn(mockResponse);

        // Act
        String result = tool.analyzeTask("Add pagination to UserController");

        // Assert
        assertTrue(result.contains("originalTask"));
        assertTrue(result.contains("taskType"));
        assertTrue(result.contains("confidenceLevel"));
        assertTrue(result.contains("detectedEntities"));
        assertTrue(result.contains("suggestedTools"));
        assertTrue(result.contains("executionPlan"));
        assertTrue(result.contains("reasoningSummary"));
        assertTrue(result.contains("estimatedComplexity"));
    }

    @Test
    void analyzeTask_responseContainsExecutionSteps() {
        // Arrange
        TaskAnalysisResponse mockResponse = createSampleResponse();
        when(taskAnalysisService.analyze(anyString())).thenReturn(mockResponse);

        // Act
        String result = tool.analyzeTask("Add pagination to UserController");

        // Assert
        assertTrue(result.contains("stepNumber"));
        assertTrue(result.contains("toolName"));
        assertTrue(result.contains("description"));
    }

    @Test
    void analyzeTask_stableOutput_forRepeatedRequests() {
        // Arrange
        TaskAnalysisResponse mockResponse = createSampleResponse();
        when(taskAnalysisService.analyze(anyString())).thenReturn(mockResponse);

        // Act
        String first = tool.analyzeTask("Add pagination to UserController");
        String second = tool.analyzeTask("Add pagination to UserController");

        // Assert
        assertEquals(first, second);
    }

    /**
     * Creates a sample TaskAnalysisResponse for testing.
     */
    private TaskAnalysisResponse createSampleResponse() {
        TaskAnalysisResponse response = new TaskAnalysisResponse();
        response.setOriginalTask("Add pagination to UserController");
        response.setTaskType(TaskType.NEW_FEATURE);
        response.setConfidenceLevel(ConfidenceLevel.HIGH);
        response.setDetectedEntities(List.of("Class: UserController (Controller)"));
        response.setSuggestedTools(List.of("repository_summary", "search_code", "find_class",
                "find_method", "find_rest_api", "list_related_files", "prompt_context"));
        response.setEstimatedComplexity(ComplexityLevel.MEDIUM);
        response.setReasoningSummary("Task classified as 'New Feature' with 1 entity references: Class: UserController (Controller). " +
                "7 MCP tools required for execution: repository_summary, search_code, find_class, find_method, " +
                "find_rest_api, list_related_files, prompt_context. " +
                "The execution plan follows a logical progression: start with repository overview, " +
                "search for relevant code, identify specific components and classes, examine implementations, " +
                "and finally consolidate into a development context.");

        response.addExecutionStep(new TaskAnalysisResponse.ExecutionStep(1, "repository_summary",
                "Obtain a high-level overview of the repository structure, packages, and key statistics"));
        response.addExecutionStep(new TaskAnalysisResponse.ExecutionStep(2, "search_code",
                "Search the codebase for files and code related to the task description"));
        response.addExecutionStep(new TaskAnalysisResponse.ExecutionStep(3, "find_class",
                "Locate specific classes and their metadata based on detected entities"));
        response.addExecutionStep(new TaskAnalysisResponse.ExecutionStep(4, "find_method",
                "Find method signatures and implementations within relevant classes"));
        response.addExecutionStep(new TaskAnalysisResponse.ExecutionStep(5, "find_rest_api",
                "Discover REST API endpoints, HTTP methods, and request/response types"));
        response.addExecutionStep(new TaskAnalysisResponse.ExecutionStep(6, "list_related_files",
                "List files related to the detected entities and task scope"));
        response.addExecutionStep(new TaskAnalysisResponse.ExecutionStep(7, "prompt_context",
                "Generate a consolidated AI-ready prompt context with all gathered information"));

        return response;
    }
}