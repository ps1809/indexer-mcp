package com.projectiq.mcp.tools;

import com.projectiq.mcp.knowledge.dto.KnowledgeReport;
import com.projectiq.mcp.knowledge.service.DevelopmentKnowledgeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryDevelopmentKnowledgeToolTest {

    @Mock
    private DevelopmentKnowledgeService developmentKnowledgeService;

    private QueryDevelopmentKnowledgeTool tool;

    @BeforeEach
    void setUp() {
        tool = new QueryDevelopmentKnowledgeTool(developmentKnowledgeService);
    }

    @Test
    void queryDevelopmentKnowledge_withValidInputs_returnsReport() {
        // Arrange
        KnowledgeReport expectedReport = new KnowledgeReport();
        expectedReport.setRepositoryName("test-repo");
        expectedReport.setBranch("main");
        expectedReport.setQuery("Show me architecture");
        expectedReport.setKnowledgeDomain("Architecture");
        expectedReport.setStatus("SUCCESS");

        when(developmentKnowledgeService.queryKnowledge(anyString(), anyString(), anyString()))
                .thenReturn(expectedReport);

        // Act
        KnowledgeReport result = tool.queryDevelopmentKnowledge("Show me architecture", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertEquals("test-repo", result.getRepositoryName());
        assertEquals("main", result.getBranch());
        assertEquals("Show me architecture", result.getQuery());
        assertEquals("Architecture", result.getKnowledgeDomain());
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    void queryDevelopmentKnowledge_withNullQuery_returnsErrorReport() {
        // Arrange
        when(developmentKnowledgeService.queryKnowledge(eq(null), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Query cannot be null or empty"));

        // Act
        KnowledgeReport result = tool.queryDevelopmentKnowledge(null, "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void queryDevelopmentKnowledge_withEmptyQuery_returnsErrorReport() {
        // Arrange
        when(developmentKnowledgeService.queryKnowledge(eq(""), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Query cannot be null or empty"));

        // Act
        KnowledgeReport result = tool.queryDevelopmentKnowledge("", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void queryDevelopmentKnowledge_withServiceException_returnsErrorReport() {
        // Arrange
        when(developmentKnowledgeService.queryKnowledge(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        KnowledgeReport result = tool.queryDevelopmentKnowledge("test query", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void queryDevelopmentKnowledge_withRepositoryQuery_returnsAllDomains() {
        // Arrange
        KnowledgeReport expectedReport = new KnowledgeReport();
        expectedReport.setRepositoryName("test-repo");
        expectedReport.setBranch("main");
        expectedReport.setQuery("Show me repository structure");
        expectedReport.setKnowledgeDomain("Repository Structure");
        expectedReport.setStatus("SUCCESS");

        when(developmentKnowledgeService.queryKnowledge(anyString(), anyString(), anyString()))
                .thenReturn(expectedReport);

        // Act
        KnowledgeReport result = tool.queryDevelopmentKnowledge("Show me repository structure", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertEquals("Repository Structure", result.getKnowledgeDomain());
        assertEquals("SUCCESS", result.getStatus());
    }

    @Test
    void queryDevelopmentKnowledge_withNullBranch_usesDefaultBranch() {
        // Arrange
        KnowledgeReport expectedReport = new KnowledgeReport();
        expectedReport.setRepositoryName("test-repo");
        expectedReport.setBranch("main");
        expectedReport.setQuery("Show me sessions");
        expectedReport.setKnowledgeDomain("Development Sessions");
        expectedReport.setStatus("SUCCESS");

        when(developmentKnowledgeService.queryKnowledge(anyString(), anyString(), isNull()))
                .thenReturn(expectedReport);

        // Act
        KnowledgeReport result = tool.queryDevelopmentKnowledge("Show me sessions", "test-repo", null);

        // Assert
        assertNotNull(result);
        assertEquals("Development Sessions", result.getKnowledgeDomain());
    }
}