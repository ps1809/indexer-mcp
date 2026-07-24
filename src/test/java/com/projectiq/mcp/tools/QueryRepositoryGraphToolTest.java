package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraphReport;
import com.projectiq.mcp.knowledgegraph.service.RepositoryKnowledgeGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryRepositoryGraphToolTest {

    @Mock
    private RepositoryKnowledgeGraphService knowledgeGraphService;

    private QueryRepositoryGraphTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tool = new QueryRepositoryGraphTool(knowledgeGraphService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void queryRepositoryGraph_withValidInput_returnsGraphReport() throws Exception {
        // Arrange
        KnowledgeGraphReport report = createSampleReport();
        when(knowledgeGraphService.generateKnowledgeGraphReport("test-repo", "main"))
                .thenReturn(report);

        // Act
        String result = tool.queryRepositoryGraph("test-repo", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("connectedEntities"));
        assertTrue(result.contains("relationshipGraph"));
        assertTrue(result.contains("graphStatistics"));
    }

    @Test
    void queryRepositoryGraph_withNullRepositoryName_returnsError() throws Exception {
        // Act
        String result = tool.queryRepositoryGraph(null, null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("repositoryName is required"));
    }

    @Test
    void queryRepositoryGraph_withEmptyRepositoryName_returnsError() throws Exception {
        // Act
        String result = tool.queryRepositoryGraph("", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("repositoryName is required"));
    }

    @Test
    void queryRepositoryGraph_withEntityName_callsTraverseFromEntity() throws Exception {
        // Arrange
        KnowledgeGraphReport report = createSampleReport();
        when(knowledgeGraphService.traverseFromEntity("test-repo", "UserController", "main"))
                .thenReturn(report);

        // Act
        String result = tool.queryRepositoryGraph("test-repo", "UserController", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("test-repo"));
    }

    @Test
    void queryRepositoryGraph_whenServiceThrowsException_returnsError() throws Exception {
        // Arrange
        when(knowledgeGraphService.generateKnowledgeGraphReport(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        String result = tool.queryRepositoryGraph("test-repo", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
    }

    @Test
    void queryRepositoryGraph_returnsDeterministicOutput() throws Exception {
        // Arrange
        KnowledgeGraphReport report = createSampleReport();
        when(knowledgeGraphService.generateKnowledgeGraphReport("test-repo", "main"))
                .thenReturn(report);

        // Act
        String result1 = tool.queryRepositoryGraph("test-repo", null, "main");
        String result2 = tool.queryRepositoryGraph("test-repo", null, "main");

        // Assert
        assertEquals(result1, result2);
    }

    @Test
    void queryRepositoryGraph_responseContainsAllRequiredFields() throws Exception {
        // Arrange
        KnowledgeGraphReport report = createSampleReport();
        when(knowledgeGraphService.generateKnowledgeGraphReport("test-repo", "main"))
                .thenReturn(report);

        // Act
        String result = tool.queryRepositoryGraph("test-repo", null, "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("repositoryName"));
        assertTrue(result.contains("branch"));
        assertTrue(result.contains("connectedEntities"));
        assertTrue(result.contains("relationshipGraph"));
        assertTrue(result.contains("dependencyPaths"));
        assertTrue(result.contains("architecturalRelationships"));
        assertTrue(result.contains("indirectDependencies"));
        assertTrue(result.contains("criticalNodes"));
        assertTrue(result.contains("graphStatistics"));
        assertTrue(result.contains("traversalSummary"));
    }

    // --- Helper methods ---

    private KnowledgeGraphReport createSampleReport() {
        KnowledgeGraphReport report = new KnowledgeGraphReport();
        report.setRepositoryName("test-repo");
        report.setBranch("main");

        report.setConnectedEntities(new ArrayList<>());
        report.setRelationshipGraph(new ArrayList<>());
        report.setDependencyPaths(new ArrayList<>());
        report.setArchitecturalRelationships(new ArrayList<>());
        report.setIndirectDependencies(new ArrayList<>());
        report.setCriticalNodes(new ArrayList<>());

        KnowledgeGraphReport.GraphStatistics stats = new KnowledgeGraphReport.GraphStatistics();
        stats.setTotalNodes(10);
        stats.setTotalEdges(15);
        stats.setEntityTypeCount(5);
        stats.setRelationshipTypeCount(4);
        stats.setCriticalNodeCount(2);
        stats.setIndirectDependencyCount(3);
        stats.setAverageConnectionsPerNode(1.5);
        report.setGraphStatistics(stats);

        report.setTraversalSummary("Knowledge Graph for repository 'test-repo': 10 nodes, 15 edges.");
        return report;
    }
}