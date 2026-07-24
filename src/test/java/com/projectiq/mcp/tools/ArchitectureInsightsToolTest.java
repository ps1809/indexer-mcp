package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchitectureInsightsToolTest {

    @Mock
    private ArchitectureInsightsService architectureInsightsService;

    private ArchitectureInsightsTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tool = new ArchitectureInsightsTool(architectureInsightsService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void analyzeArchitecture_withValidInput_returnsArchitectureInsights() throws Exception {
        // Arrange
        ArchitectureInsightsResponse response = createSampleResponse();
        when(architectureInsightsService.analyzeArchitecture("my-app", "main"))
                .thenReturn(response);

        // Act
        String result = tool.analyzeArchitecture("my-app", "main");

        // Assert
        assertNotNull(result);
        ArchitectureInsightsResponse parsed = objectMapper.readValue(result, ArchitectureInsightsResponse.class);
        assertEquals("my-app", parsed.getRepositoryName());
        assertEquals("main", parsed.getBranch());
        assertNotNull(parsed.getRepositoryOverview());
        assertNotNull(parsed.getArchitecturalStyle());
        assertNotNull(parsed.getDetectedLayers());
        assertNotNull(parsed.getConfidenceLevel());
    }

    @Test
    void analyzeArchitecture_withNullRepositoryName_returnsError() throws Exception {
        // Act
        String result = tool.analyzeArchitecture(null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeArchitecture_withEmptyRepositoryName_returnsError() throws Exception {
        // Act
        String result = tool.analyzeArchitecture("", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void analyzeArchitecture_withNullBranch_defaultsToMain() throws Exception {
        // Arrange
        ArchitectureInsightsResponse response = createSampleResponse();
        when(architectureInsightsService.analyzeArchitecture("my-app", "main"))
                .thenReturn(response);

        // Act
        String result = tool.analyzeArchitecture("my-app", null);

        // Assert
        assertNotNull(result);
        ArchitectureInsightsResponse parsed = objectMapper.readValue(result, ArchitectureInsightsResponse.class);
        assertEquals("main", parsed.getBranch());
    }

    @Test
    void analyzeArchitecture_withEmptyBranch_defaultsToMain() throws Exception {
        // Arrange
        ArchitectureInsightsResponse response = createSampleResponse();
        when(architectureInsightsService.analyzeArchitecture("my-app", "main"))
                .thenReturn(response);

        // Act
        String result = tool.analyzeArchitecture("my-app", "");

        // Assert
        assertNotNull(result);
        ArchitectureInsightsResponse parsed = objectMapper.readValue(result, ArchitectureInsightsResponse.class);
        assertEquals("main", parsed.getBranch());
    }

    @Test
    void analyzeArchitecture_withCustomBranch_usesCustomBranch() throws Exception {
        // Arrange
        ArchitectureInsightsResponse response = createSampleResponse();
        response.setBranch("develop");
        when(architectureInsightsService.analyzeArchitecture("my-app", "develop"))
                .thenReturn(response);

        // Act
        String result = tool.analyzeArchitecture("my-app", "develop");

        // Assert
        assertNotNull(result);
        ArchitectureInsightsResponse parsed = objectMapper.readValue(result, ArchitectureInsightsResponse.class);
        assertEquals("develop", parsed.getBranch());
    }

    @Test
    void analyzeArchitecture_whenServiceThrowsException_returnsError() throws Exception {
        // Arrange
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service failure"));

        // Act
        String result = tool.analyzeArchitecture("my-app", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Service failure"));
    }

    @Test
    void analyzeArchitecture_whenServiceThrowsIllegalArgument_returnsError() throws Exception {
        // Arrange
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid repository"));

        // Act
        String result = tool.analyzeArchitecture("my-app", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Invalid repository"));
    }

    @Test
    void analyzeArchitecture_returnsDeterministicOutput() throws Exception {
        // Arrange
        ArchitectureInsightsResponse response = createSampleResponse();
        when(architectureInsightsService.analyzeArchitecture("my-app", "main"))
                .thenReturn(response);

        // Act
        String result1 = tool.analyzeArchitecture("my-app", "main");
        String result2 = tool.analyzeArchitecture("my-app", "main");

        // Assert
        assertEquals(result1, result2);
    }

    @Test
    void analyzeArchitecture_responseContainsAllRequiredFields() throws Exception {
        // Arrange
        ArchitectureInsightsResponse response = createSampleResponse();
        when(architectureInsightsService.analyzeArchitecture("my-app", "main"))
                .thenReturn(response);

        // Act
        String result = tool.analyzeArchitecture("my-app", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("repositoryName"));
        assertTrue(result.contains("branch"));
        assertTrue(result.contains("repositoryOverview"));
        assertTrue(result.contains("architecturalStyle"));
        assertTrue(result.contains("detectedLayers"));
        assertTrue(result.contains("dependencyFlow"));
        assertTrue(result.contains("architecturalStrengths"));
        assertTrue(result.contains("potentialConcerns"));
        assertTrue(result.contains("confidenceLevel"));
    }

    // --- Helper methods ---

    private ArchitectureInsightsResponse createSampleResponse() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setRepositoryName("my-app");
        response.setBranch("main");
        response.setRepositoryOverview("Repository 'my-app' on branch 'main' is indexed. Contains 6 packages, 20 classes, 100 methods, and 30 files.");
        response.setArchitecturalStyle("Layered Architecture (Controller-Service-Repository)");

        List<String> layers = new ArrayList<>();
        layers.add("Controller (Presentation)");
        layers.add("Service (Business Logic)");
        layers.add("Repository (Data Access)");
        response.setDetectedLayers(layers);

        response.setDependencyFlow("Controller -> Service -> Repository (Downward dependency flow)");

        List<String> strengths = new ArrayList<>();
        strengths.add("Layered Architecture");
        strengths.add("Repository Pattern");
        response.setArchitecturalStrengths(strengths);

        List<String> concerns = new ArrayList<>();
        concerns.add("No architectural layers detected");
        response.setPotentialConcerns(concerns);

        response.setConfidenceLevel("HIGH");
        return response;
    }
}