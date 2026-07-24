package com.projectiq.mcp.tools;

import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryHealthToolTest {

    @Mock
    private RepositoryHealthService repositoryHealthService;

    private RepositoryHealthTool tool;

    @BeforeEach
    void setUp() {
        tool = new RepositoryHealthTool(repositoryHealthService);
    }

    @Test
    void analyzeHealth_withValidRepository_returnsHealthReport() {
        // Arrange
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryName("my-app");
        response.setBranch("main");
        response.setHealthScore(75);
        response.setMaintainabilityRating("Good");
        response.setComplexityRating("Moderate");
        response.setArchitectureConsistency("Consistent");
        response.setDependencyHealth("Healthy");
        response.setTestingMaturity("Developing");
        response.setDocumentationMaturity("Adequate");
        response.setConfidenceLevel("HIGH");

        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeHealth("my-app", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("my-app"));
        assertTrue(result.contains("75"));
        assertTrue(result.contains("Good"));
        assertTrue(result.contains("HIGH"));
    }

    @Test
    void analyzeHealth_withNullRepositoryName_returnsError() {
        // Act
        String result = tool.analyzeHealth(null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeHealth_withEmptyRepositoryName_returnsError() {
        // Act
        String result = tool.analyzeHealth("", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeHealth_withNullBranch_defaultsToMain() {
        // Arrange
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryName("my-app");
        response.setBranch("main");
        response.setHealthScore(50);
        response.setConfidenceLevel("MEDIUM");

        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeHealth("my-app", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("my-app"));
        assertTrue(result.contains("main"));
    }

    @Test
    void analyzeHealth_whenServiceThrowsException_returnsError() {
        // Arrange
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String result = tool.analyzeHealth("my-app", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
    }

    @Test
    void analyzeHealth_whenServiceThrowsIllegalArgument_returnsError() {
        // Arrange
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid repository"));

        // Act
        String result = tool.analyzeHealth("my-app", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void analyzeHealth_withEmptyBranch_defaultsToMain() {
        // Arrange
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryName("my-app");
        response.setBranch("main");
        response.setHealthScore(50);
        response.setConfidenceLevel("MEDIUM");

        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeHealth("my-app", "");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("my-app"));
        assertTrue(result.contains("main"));
    }

    @Test
    void analyzeHealth_withValidRepository_returnsValidJson() {
        // Arrange
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setHealthScore(85);
        response.setMaintainabilityRating("Excellent");
        response.setConfidenceLevel("HIGH");

        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeHealth("test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
        assertTrue(result.contains("\"repositoryName\""));
        assertTrue(result.contains("\"healthScore\""));
        assertTrue(result.contains("\"maintainabilityRating\""));
        assertTrue(result.contains("\"confidenceLevel\""));
    }
}