package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.dto.RepositoryContext;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import com.projectiq.mcp.client.service.RepositoryContextBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildContextToolTest {

    @Mock
    private RepositoryContextBuilderService contextBuilderService;

    private BuildContextTool tool;

    @BeforeEach
    void setUp() {
        tool = new BuildContextTool(contextBuilderService);
    }

    @Test
    void buildContext_withValidRequest() throws IndexerClientException {
        // Arrange
        RepositoryContext mockContext = new RepositoryContext();
        mockContext.setTask("Add pagination");
        mockContext.setRepositoryName("test-repo");
        mockContext.setBranch("main");
        mockContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        
        when(contextBuilderService.buildContext(any())).thenReturn(mockContext);

        // Act
        String result = toolBuildContext("Add pagination", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Repository Context"));
        assertTrue(result.contains("Add pagination"));
        assertTrue(result.contains("test-repo"));
        verify(contextBuilderService).buildContext(any());
    }

    @Test
    void buildContext_withNullTask() {
        // Act & Assert
        String result = toolBuildContext(null, "test-repo", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(contextBuilderService);
    }

    @Test
    void buildContext_withEmptyTask() {
        // Act & Assert
        String result = toolBuildContext("", "test-repo", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(contextBuilderService);
    }

    @Test
    void buildContext_withNullRepository() {
        // Act & Assert
        String result = toolBuildContext("Add pagination", null, "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
        verifyNoInteractions(contextBuilderService);
    }

    @Test
    void buildContext_withEmptyRepository() {
        // Act & Assert
        String result = toolBuildContext("Add pagination", "", "main");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
        verifyNoInteractions(contextBuilderService);
    }

    @Test
    void buildContext_withDefaultBranch() throws IndexerClientException {
        // Arrange
        RepositoryContext mockContext = new RepositoryContext();
        mockContext.setTask("Test");
        mockContext.setRepositoryName("test-repo");
        mockContext.setBranch("main");
        mockContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        
        when(contextBuilderService.buildContext(any())).thenReturn(mockContext);

        // Act
        String result = toolBuildContext("Test", "test-repo", null);

        // Assert
        assertNotNull(result);
        verify(contextBuilderService).buildContext(argThat(req -> "main".equals(req.getBranch())));
    }

    @Test
    void buildContext_withIndexerConnectionException() {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenThrow(new IndexerConnectionException("Connection refused"));

        // Act
        String result = toolBuildContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_UNREACHABLE"));
        verify(contextBuilderService).buildContext(any());
    }

    @Test
    void buildContext_withIndexerTimeoutException() {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenThrow(new com.projectiq.mcp.client.exception.IndexerTimeoutException("Timeout after 30s"));

        // Act
        String result = toolBuildContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_TIMEOUT"));
        verify(contextBuilderService).buildContext(any());
    }

    @Test
    void buildContext_withIndexerClientException() {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenThrow(new IndexerClientException("Bad request"));

        // Act
        String result = toolBuildContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_ERROR"));
        verify(contextBuilderService).buildContext(any());
    }

    @Test
    void buildContext_withGenericException() {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String result = toolBuildContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INTERNAL_ERROR"));
        verify(contextBuilderService).buildContext(any());
    }

    @Test
    void buildContext_withErrors() throws IndexerClientException {
        // Arrange
        RepositoryContext mockContext = new RepositoryContext();
        mockContext.setTask("Test");
        mockContext.setRepositoryName("test-repo");
        mockContext.setBranch("main");
        mockContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        mockContext.addError(new ContextBuildError("searchCode", "INDEXER_UNREACHABLE", "Cannot connect"));
        
        when(contextBuilderService.buildContext(any())).thenReturn(mockContext);

        // Act
        String result = toolBuildContext("Test", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Errors"));
        assertTrue(result.contains("INDEXER_UNREACHABLE"));
    }

    private String toolBuildContext(String task, String repositoryName, String branch) {
        return tool.buildContext(task, repositoryName, branch);
    }
}