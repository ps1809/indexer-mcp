package com.projectiq.mcp.tools;

import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.NamingConventions;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryConventionToolTest {

    @Mock
    private RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;

    private RepositoryConventionTool tool;

    @BeforeEach
    void setUp() {
        tool = new RepositoryConventionTool(repositoryConventionAnalyzerService);
    }

    @Test
    void analyzeConventions_withValidRepository_returnsJsonResponse() {
        // Arrange
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setRepositoryOverview("Test overview");
        response.setConfidenceLevel("HIGH");
        response.setNamingConventions(new NamingConventions());
        response.getNamingConventions().setClassNamingConvention("PascalCase (consistent)");

        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeConventions("test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("main"));
        assertTrue(result.contains("HIGH"));
        assertTrue(result.contains("PascalCase (consistent)"));
    }

    @Test
    void analyzeConventions_withEmptyRepositoryName_returnsError() {
        // Act
        String result = tool.analyzeConventions("", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeConventions_withNullRepositoryName_returnsError() {
        // Act
        String result = tool.analyzeConventions(null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeConventions_withNullBranch_defaultsToMain() {
        // Arrange
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setConfidenceLevel("MEDIUM");
        response.setNamingConventions(new NamingConventions());

        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeConventions("test-repo", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("main"));
    }

    @Test
    void analyzeConventions_withEmptyBranch_defaultsToMain() {
        // Arrange
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setConfidenceLevel("MEDIUM");
        response.setNamingConventions(new NamingConventions());

        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeConventions("test-repo", "");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("main"));
    }

    @Test
    void analyzeConventions_whenServiceThrowsException_returnsError() {
        // Arrange
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service failure"));

        // Act
        String result = tool.analyzeConventions("test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Service failure"));
    }

    @Test
    void analyzeConventions_whenServiceThrowsIllegalArgument_returnsError() {
        // Arrange
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid argument"));

        // Act
        String result = tool.analyzeConventions("test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Invalid argument"));
    }

    @Test
    void analyzeConventions_withFullResponse_containsAllSections() {
        // Arrange
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setRepositoryName("full-repo");
        response.setBranch("main");
        response.setRepositoryOverview("Full repository analysis");
        response.setConfidenceLevel("HIGH");

        NamingConventions naming = new NamingConventions();
        naming.setClassNamingConvention("PascalCase (consistent)");
        naming.setServiceNamingPattern("{Name}Service");
        naming.setRepositoryNamingPattern("{Name}Repository");
        response.setNamingConventions(naming);

        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(response);

        // Act
        String result = tool.analyzeConventions("full-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("full-repo"));
        assertTrue(result.contains("PascalCase"));
        assertTrue(result.contains("{Name}Service"));
        assertTrue(result.contains("{Name}Repository"));
        assertTrue(result.contains("HIGH"));
        assertTrue(result.contains("Full repository analysis"));
    }
}