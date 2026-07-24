package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse;
import com.projectiq.mcp.analysis.service.CrossRepositoryAnalysisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Tests for CrossRepositoryAnalysisTool.
 */
@ExtendWith(MockitoExtension.class)
class CrossRepositoryAnalysisToolTest {

    @Mock
    private CrossRepositoryAnalysisService crossRepositoryAnalysisService;

    private CrossRepositoryAnalysisTool tool;

    @BeforeEach
    void setUp() {
        tool = new CrossRepositoryAnalysisTool(crossRepositoryAnalysisService);
    }

    @Test
    void testAnalyzeCrossRepository_NullInput() {
        String result = tool.analyzeCrossRepository(null);
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testAnalyzeCrossRepository_EmptyInput() {
        String result = tool.analyzeCrossRepository("");
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testAnalyzeCrossRepository_WhitespaceInput() {
        String result = tool.analyzeCrossRepository("   ");
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testAnalyzeCrossRepository_SingleRepository() throws Exception {
        CrossRepositoryAnalysisResponse response = new CrossRepositoryAnalysisResponse();
        response.setAnalysisId("test-123");
        CrossRepositoryAnalysisResponse.RepositorySummary repo = 
                new CrossRepositoryAnalysisResponse.RepositorySummary();
        repo.setRepositoryName("repo1");
        response.setRepositories(List.of(repo));

        when(crossRepositoryAnalysisService.analyzeCrossRepository(anyList()))
                .thenReturn(response);

        String result = tool.analyzeCrossRepository("repo1");
        assertNotNull(result);
        assertTrue(result.contains("test-123"));
        assertTrue(result.contains("repo1"));
    }

    @Test
    void testAnalyzeCrossRepository_MultipleRepositories() throws Exception {
        CrossRepositoryAnalysisResponse response = new CrossRepositoryAnalysisResponse();
        response.setAnalysisId("test-456");
        CrossRepositoryAnalysisResponse.RepositorySummary repo1 = 
                new CrossRepositoryAnalysisResponse.RepositorySummary();
        repo1.setRepositoryName("repo1");
        CrossRepositoryAnalysisResponse.RepositorySummary repo2 = 
                new CrossRepositoryAnalysisResponse.RepositorySummary();
        repo2.setRepositoryName("repo2");
        response.setRepositories(List.of(repo1, repo2));

        when(crossRepositoryAnalysisService.analyzeCrossRepository(anyList()))
                .thenReturn(response);

        String result = tool.analyzeCrossRepository("repo1, repo2");
        assertNotNull(result);
        assertTrue(result.contains("test-456"));
        assertTrue(result.contains("repo1"));
        assertTrue(result.contains("repo2"));
    }

    @Test
    void testAnalyzeCrossRepository_ServiceThrowsException() {
        when(crossRepositoryAnalysisService.analyzeCrossRepository(anyList()))
                .thenThrow(new RuntimeException("Service error"));

        String result = tool.analyzeCrossRepository("repo1");
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
    }

    @Test
    void testAnalyzeCrossRepository_ValidResponseFormat() throws Exception {
        CrossRepositoryAnalysisResponse response = new CrossRepositoryAnalysisResponse();
        response.setAnalysisId("test-789");

        when(crossRepositoryAnalysisService.analyzeCrossRepository(anyList()))
                .thenReturn(response);

        String result = tool.analyzeCrossRepository("repo1,repo2,repo3");

        ObjectMapper mapper = new ObjectMapper();
        var parsed = mapper.readTree(result);
        assertNotNull(parsed.get("analysisId"));
        assertEquals("test-789", parsed.get("analysisId").asText());
    }
}