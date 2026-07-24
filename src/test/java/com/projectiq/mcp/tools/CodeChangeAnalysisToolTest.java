package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.CodeChangeAnalysisResponse;
import com.projectiq.mcp.analysis.service.CodeChangeAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CodeChangeAnalysisTool}.
 * Verifies tool execution, error handling, and JSON serialization.
 */
@ExtendWith(MockitoExtension.class)
class CodeChangeAnalysisToolTest {

    @Mock
    private CodeChangeAnalysisService codeChangeAnalysisService;

    private CodeChangeAnalysisTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tool = new CodeChangeAnalysisTool(codeChangeAnalysisService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testAnalyzeCodeChange_Success() throws JsonProcessingException {
        String change = "Add pagination to UserController";
        String repository = "my-repo";

        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();
        response.setProposedChangeSummary("Add pagination to UserController. Change Type: New Feature.");
        response.setImpactedFiles(Arrays.asList("UserController.java", "UserService.java"));
        response.setImpactedClasses(Arrays.asList("UserController (Controller)", "UserService (Service)"));
        response.setImpactedMethods(Arrays.asList("findAll()", "findById()"));
        response.setImpactedRestApis(Arrays.asList("GET /api/users"));
        response.setDependencyChanges(Arrays.asList("New dependencies may be required"));
        response.setTestingRecommendations(Arrays.asList("Write unit tests for all new classes"));
        response.setRiskAssessment(Arrays.asList("New feature may introduce integration issues"));
        response.setSuggestedImplementationOrder(Arrays.asList("1. Define data models", "2. Implement repository layer"));

        when(codeChangeAnalysisService.analyzeCodeChange(anyString(), anyString(), anyString()))
                .thenReturn(response);

        String result = tool.analyzeCodeChange(change, repository, "main");

        assertNotNull(result);
        assertTrue(result.contains("Add pagination to UserController"));
        assertTrue(result.contains("UserController.java"));
        assertTrue(result.contains("UserService (Service)"));
        assertTrue(result.contains("findAll()"));
        assertTrue(result.contains("GET /api/users"));

        // Verify it's valid JSON
        CodeChangeAnalysisResponse parsed = objectMapper.readValue(result, CodeChangeAnalysisResponse.class);
        assertNotNull(parsed);
        assertEquals("Add pagination to UserController. Change Type: New Feature.", parsed.getProposedChangeSummary());
    }

    @Test
    void testAnalyzeCodeChange_EmptyChange_ReturnsError() throws JsonProcessingException {
        String result = tool.analyzeCodeChange("", "my-repo", "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Proposed change description is required"));
    }

    @Test
    void testAnalyzeCodeChange_NullChange_ReturnsError() throws JsonProcessingException {
        String result = tool.analyzeCodeChange(null, "my-repo", "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Proposed change description is required"));
    }

    @Test
    void testAnalyzeCodeChange_EmptyRepository_ReturnsError() throws JsonProcessingException {
        String result = tool.analyzeCodeChange("Add feature", "", "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testAnalyzeCodeChange_NullRepository_ReturnsError() throws JsonProcessingException {
        String result = tool.analyzeCodeChange("Add feature", null, "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testAnalyzeCodeChange_WhitespaceChange_ReturnsError() throws JsonProcessingException {
        String result = tool.analyzeCodeChange("   ", "my-repo", "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testAnalyzeCodeChange_ServiceThrowsIllegalArgument() throws JsonProcessingException {
        when(codeChangeAnalysisService.analyzeCodeChange(anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid repository name"));

        String result = tool.analyzeCodeChange("Add feature", "invalid-repo", "main");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Invalid repository name"));
    }

    @Test
    void testAnalyzeCodeChange_ServiceThrowsUnexpectedException() throws JsonProcessingException {
        when(codeChangeAnalysisService.analyzeCodeChange(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected database error"));

        String result = tool.analyzeCodeChange("Add feature", "my-repo", "main");

        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Unexpected database error"));
    }

    @Test
    void testAnalyzeCodeChange_WithNullBranch_DefaultsToMain() {
        String change = "Add logging";
        String repository = "my-repo";

        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();
        response.setProposedChangeSummary("Add logging. Change Type: New Feature.");
        when(codeChangeAnalysisService.analyzeCodeChange(eq(change), eq(repository), eq("main")))
                .thenReturn(response);

        String result = tool.analyzeCodeChange(change, repository, null);

        assertNotNull(result);
        assertTrue(result.contains("Add logging"));
        verify(codeChangeAnalysisService).analyzeCodeChange(eq(change), eq(repository), eq("main"));
    }

    @Test
    void testAnalyzeCodeChange_WithEmptyBranch_DefaultsToMain() {
        String change = "Add logging";
        String repository = "my-repo";

        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();
        response.setProposedChangeSummary("Add logging. Change Type: New Feature.");
        when(codeChangeAnalysisService.analyzeCodeChange(eq(change), eq(repository), eq("main")))
                .thenReturn(response);

        String result = tool.analyzeCodeChange(change, repository, "");

        assertNotNull(result);
        assertTrue(result.contains("Add logging"));
        verify(codeChangeAnalysisService).analyzeCodeChange(eq(change), eq(repository), eq("main"));
    }

    @Test
    void testAnalyzeCodeChange_WithCustomBranch() {
        String change = "Add logging";
        String repository = "my-repo";

        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();
        response.setProposedChangeSummary("Add logging. Change Type: New Feature.");
        when(codeChangeAnalysisService.analyzeCodeChange(eq(change), eq(repository), eq("develop")))
                .thenReturn(response);

        String result = tool.analyzeCodeChange(change, repository, "develop");

        assertNotNull(result);
        assertTrue(result.contains("Add logging"));
        verify(codeChangeAnalysisService).analyzeCodeChange(eq(change), eq(repository), eq("develop"));
    }

    @Test
    void testAnalyzeCodeChange_ResponseContainsAllFields() throws JsonProcessingException {
        String change = "Add user registration";
        String repository = "my-repo";

        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();
        response.setProposedChangeSummary("Add user registration. Change Type: New Feature.");
        response.setImpactedFiles(Arrays.asList("UserController.java"));
        response.setImpactedClasses(Arrays.asList("UserController (Controller)"));
        response.setImpactedMethods(Arrays.asList("register()"));
        response.setImpactedRestApis(Arrays.asList("POST /api/users"));
        response.setDependencyChanges(Arrays.asList("New dependency required"));
        response.setTestingRecommendations(Arrays.asList("Write unit tests"));
        response.setRiskAssessment(Arrays.asList("Integration risk"));
        response.setSuggestedImplementationOrder(Arrays.asList("1. Implement changes"));

        when(codeChangeAnalysisService.analyzeCodeChange(anyString(), anyString(), anyString()))
                .thenReturn(response);

        String result = tool.analyzeCodeChange(change, repository, "main");

        CodeChangeAnalysisResponse parsed = objectMapper.readValue(result, CodeChangeAnalysisResponse.class);
        assertEquals("Add user registration. Change Type: New Feature.", parsed.getProposedChangeSummary());
        assertEquals(1, parsed.getImpactedFiles().size());
        assertEquals(1, parsed.getImpactedClasses().size());
        assertEquals(1, parsed.getImpactedMethods().size());
        assertEquals(1, parsed.getImpactedRestApis().size());
        assertEquals(1, parsed.getDependencyChanges().size());
        assertEquals(1, parsed.getTestingRecommendations().size());
        assertEquals(1, parsed.getRiskAssessment().size());
        assertEquals(1, parsed.getSuggestedImplementationOrder().size());
    }

    @Test
    void testAnalyzeCodeChange_DeterministicOutput() {
        String change = "Add pagination support";
        String repository = "test-repo";

        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();
        response.setProposedChangeSummary("Add pagination support. Change Type: New Feature.");
        response.setImpactedFiles(Arrays.asList("PaginationService.java"));
        response.setImpactedClasses(Arrays.asList("PaginationService (Service)"));
        response.setTestingRecommendations(Arrays.asList("Write unit tests"));
        response.setRiskAssessment(Arrays.asList("Limited information available"));
        response.setSuggestedImplementationOrder(Arrays.asList("1. Implement changes"));

        when(codeChangeAnalysisService.analyzeCodeChange(anyString(), anyString(), anyString()))
                .thenReturn(response);

        String result1 = tool.analyzeCodeChange(change, repository, "main");
        String result2 = tool.analyzeCodeChange(change, repository, "main");

        assertEquals(result1, result2);
    }
}