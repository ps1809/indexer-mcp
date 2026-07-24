package com.projectiq.mcp.tools;

import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse;
import com.projectiq.mcp.analysis.service.RepositoryEvolutionAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeRepositoryEvolutionToolTest {

    @Mock
    private RepositoryEvolutionAnalysisService repositoryEvolutionAnalysisService;

    private AnalyzeRepositoryEvolutionTool tool;

    @BeforeEach
    void setUp() {
        tool = new AnalyzeRepositoryEvolutionTool(repositoryEvolutionAnalysisService);
    }

    @Test
    void testAnalyzeRepositoryEvolutionSuccess() {
        RepositoryEvolutionAnalysisResponse response = new RepositoryEvolutionAnalysisResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setProposedChange("Add new feature");
        response.setProposedChangeSummary("Summary");
        response.setRepositoryEvolutionScore(75);

        when(repositoryEvolutionAnalysisService.analyzeEvolution(anyString(), anyString(), anyString()))
                .thenReturn(response);

        String result = tool.analyzeRepositoryEvolution("test-repo", "main", "Add new feature");

        assertNotNull(result);
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("75"));
    }

    @Test
    void testAnalyzeRepositoryEvolutionWithNullBranch() {
        RepositoryEvolutionAnalysisResponse response = new RepositoryEvolutionAnalysisResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setProposedChange("Add new feature");
        response.setProposedChangeSummary("Summary");
        response.setRepositoryEvolutionScore(75);

        when(repositoryEvolutionAnalysisService.analyzeEvolution(anyString(), anyString(), anyString()))
                .thenReturn(response);

        String result = tool.analyzeRepositoryEvolution("test-repo", null, "Add new feature");

        assertNotNull(result);
        assertTrue(result.contains("test-repo"));
    }

    @Test
    void testAnalyzeRepositoryEvolutionWithEmptyRepositoryName() {
        String result = tool.analyzeRepositoryEvolution("", "main", "Add new feature");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testAnalyzeRepositoryEvolutionWithNullRepositoryName() {
        String result = tool.analyzeRepositoryEvolution(null, "main", "Add new feature");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testAnalyzeRepositoryEvolutionWithEmptyProposedChange() {
        String result = tool.analyzeRepositoryEvolution("test-repo", "main", "");

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Proposed change description is required"));
    }

    @Test
    void testAnalyzeRepositoryEvolutionWithNullProposedChange() {
        String result = tool.analyzeRepositoryEvolution("test-repo", "main", null);

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Proposed change description is required"));
    }

    @Test
    void testAnalyzeRepositoryEvolutionWithServiceException() {
        when(repositoryEvolutionAnalysisService.analyzeEvolution(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        String result = tool.analyzeRepositoryEvolution("test-repo", "main", "Add new feature");

        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
    }
}