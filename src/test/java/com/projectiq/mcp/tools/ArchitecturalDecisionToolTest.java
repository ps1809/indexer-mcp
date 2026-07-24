package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse;
import com.projectiq.mcp.analysis.service.ArchitecturalDecisionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchitecturalDecisionToolTest {

    @Mock
    private ArchitecturalDecisionService architecturalDecisionService;

    private ArchitecturalDecisionTool tool;

    @BeforeEach
    void setUp() {
        tool = new ArchitecturalDecisionTool(architecturalDecisionService);
    }

    @Test
    void testAdviseArchitecture_NullCategory() {
        String result = tool.adviseArchitecture(null, "Test description", "my-repo");
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testAdviseArchitecture_EmptyCategory() {
        String result = tool.adviseArchitecture("", "Test description", "my-repo");
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testAdviseArchitecture_NullDescription() {
        String result = tool.adviseArchitecture("Composition vs Inheritance", null, "my-repo");
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testAdviseArchitecture_NullRepository() {
        String result = tool.adviseArchitecture("Composition vs Inheritance", "Test", null);
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testAdviseArchitecture_Success() throws Exception {
        ArchitecturalDecisionResponse response = new ArchitecturalDecisionResponse();
        response.setDecisionId("test-1");
        response.setDecisionCategory("Composition vs Inheritance");
        response.setRecommendedApproach("Composition");
        response.setRequestDescription("Test");
        response.setRepositoryName("my-repo");

        when(architecturalDecisionService.adviseArchitecture(anyString(), anyString(), anyString()))
                .thenReturn(response);

        String result = tool.adviseArchitecture("Composition vs Inheritance", "Test", "my-repo");
        assertNotNull(result);
        assertTrue(result.contains("test-1"));
        assertTrue(result.contains("Composition"));
    }

    @Test
    void testAdviseArchitecture_ServiceThrowsException() {
        when(architecturalDecisionService.adviseArchitecture(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        String result = tool.adviseArchitecture("Composition vs Inheritance", "Test", "my-repo");
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
    }

    @Test
    void testAdviseArchitecture_ValidResponseFormat() throws Exception {
        ArchitecturalDecisionResponse response = new ArchitecturalDecisionResponse();
        response.setDecisionId("test-xyz");
        response.setDecisionCategory("Composition vs Inheritance");
        response.setRecommendedApproach("Composition");
        response.setRequestDescription("Test decision");
        response.setRepositoryName("my-repo");

        when(architecturalDecisionService.adviseArchitecture(anyString(), anyString(), anyString()))
                .thenReturn(response);

        String result = tool.adviseArchitecture("Composition vs Inheritance", "Test decision", "my-repo");

        ObjectMapper mapper = new ObjectMapper();
        var parsed = mapper.readTree(result);
        assertNotNull(parsed.get("decisionId"));
        assertEquals("test-xyz", parsed.get("decisionId").asText());
        assertEquals("Composition", parsed.get("recommendedApproach").asText());
    }
}