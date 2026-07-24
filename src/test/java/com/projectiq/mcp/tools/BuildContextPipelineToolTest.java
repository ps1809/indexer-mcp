package com.projectiq.mcp.tools;

import com.projectiq.mcp.pipeline.dto.ContextPackage;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link BuildContextPipelineTool}.
 */
@ExtendWith(MockitoExtension.class)
class BuildContextPipelineToolTest {

    @Mock
    private IntelligentContextPipelineService pipelineService;

    private BuildContextPipelineTool tool;

    @BeforeEach
    void setUp() {
        tool = new BuildContextPipelineTool(pipelineService);
    }

    @Test
    void testBuildContextPipelineSuccess() {
        // Arrange
        ContextPackage mockPackage = new ContextPackage();
        mockPackage.setWorkflowSummary("Test workflow");
        mockPackage.setRepositorySummary("Test repo");
        mockPackage.setTotalContextItems(5);
        mockPackage.setHighPriorityCount(3);
        mockPackage.setMediumPriorityCount(2);
        mockPackage.setProcessingTimeMillis(50L);
        mockPackage.setSuggestedImplementationFocus("Focus on X");

        when(pipelineService.buildContextPipeline(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockPackage);

        // Act
        String result = tool.buildContextPipeline(
                "Test workflow", "implementation", "test-repo", "main", "Add pagination");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Context Pipeline Package"));
        assertTrue(result.contains("Test workflow"));
        assertTrue(result.contains("Test repo"));
        assertTrue(result.contains("Total Context Items: 5"));
        assertTrue(result.contains("High Priority: 3"));
        assertTrue(result.contains("Medium Priority: 2"));
    }

    @Test
    void testBuildContextPipelineMissingWorkflowSummary() {
        // Act
        String result = tool.buildContextPipeline(null, "implementation", "test-repo", "main", "task");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Workflow summary is required"));
    }

    @Test
    void testBuildContextPipelineEmptyWorkflowType() {
        // Act
        String result = tool.buildContextPipeline("Workflow", "", "test-repo", "main", "task");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Workflow type is required"));
    }

    @Test
    void testBuildContextPipelineMissingRepositoryName() {
        // Act
        String result = tool.buildContextPipeline("Workflow", "analysis", null, "main", "task");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testBuildContextPipelineMissingTaskDescription() {
        // Act
        String result = tool.buildContextPipeline("Workflow", "analysis", "test-repo", "main", "");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
    }

    @Test
    void testBuildContextPipelineWithNullBranch() {
        // Arrange
        ContextPackage mockPackage = new ContextPackage();
        mockPackage.setWorkflowSummary("Test");
        mockPackage.setRepositorySummary("Repo");
        when(pipelineService.buildContextPipeline(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockPackage);

        // Act - null branch should default to "main"
        String result = tool.buildContextPipeline("Test", "analysis", "test-repo", null, "task");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Context Pipeline Package"));
    }

    @Test
    void testBuildContextPipelineHandlesException() {
        // Arrange
        when(pipelineService.buildContextPipeline(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        String result = tool.buildContextPipeline("Test", "analysis", "test-repo", "main", "task");

        // Assert
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Service error"));
    }
}