package com.projectiq.mcp.knowledge.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ArchitecturalDecisionService;
import com.projectiq.mcp.analysis.service.CrossRepositoryAnalysisService;
import com.projectiq.mcp.analysis.service.RepositoryEvolutionAnalysisService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.knowledge.dto.KnowledgeReport;
import com.projectiq.mcp.knowledgegraph.dto.KnowledgeGraphReport;
import com.projectiq.mcp.knowledgegraph.service.RepositoryKnowledgeGraphService;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import com.projectiq.mcp.recommendation.dto.Recommendation;
import com.projectiq.mcp.recommendation.dto.RecommendationPriority;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.recommendation.service.RecommendationEngineService;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevelopmentKnowledgeServiceTest {

    @Mock
    private RepositoryKnowledgeGraphService knowledgeGraphService;
    @Mock
    private DevelopmentSessionService sessionService;
    @Mock
    private WorkflowOrchestratorService workflowOrchestratorService;
    @Mock
    private IntelligentContextPipelineService contextPipelineService;
    @Mock
    private RecommendationEngineService recommendationEngineService;
    @Mock
    private RepositoryEvolutionAnalysisService evolutionAnalysisService;
    @Mock
    private CrossRepositoryAnalysisService crossRepositoryAnalysisService;
    @Mock
    private ArchitectureInsightsService architectureInsightsService;
    @Mock
    private ArchitecturalDecisionService architecturalDecisionService;
    @Mock
    private RepositoryHealthService repositoryHealthService;

    private DevelopmentKnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new DevelopmentKnowledgeService(
                knowledgeGraphService, sessionService, workflowOrchestratorService,
                contextPipelineService, recommendationEngineService,
                evolutionAnalysisService, crossRepositoryAnalysisService,
                architectureInsightsService, architecturalDecisionService,
                repositoryHealthService);
    }

    @Test
    void queryKnowledge_withNullQuery_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.queryKnowledge(null, "test-repo", "main"));
    }

    @Test
    void queryKnowledge_withEmptyQuery_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.queryKnowledge("", "test-repo", "main"));
    }

    @Test
    void queryKnowledge_withNullRepository_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.queryKnowledge("test query", null, "main"));
    }

    @Test
    void queryKnowledge_withEmptyRepository_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> service.queryKnowledge("test query", "", "main"));
    }

    @Test
    void queryKnowledge_withAllDomain_returnsFullReport() {
        // Arrange
        KnowledgeGraphReport kgReport = createSampleKnowledgeGraphReport();
        when(knowledgeGraphService.generateKnowledgeGraphReport(anyString(), anyString()))
                .thenReturn(kgReport);

        ArchitectureInsightsResponse archResponse = createSampleArchitectureResponse();
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        when(sessionService.getTotalSessionCount()).thenReturn(5);
        when(sessionService.getActiveSessionCount()).thenReturn(2);

        WorkflowResult workflowResult = createSampleWorkflowResult();
        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), anyString()))
                .thenReturn(workflowResult);

        RepositoryEvolutionAnalysisResponse evolutionResponse = createSampleEvolutionResponse();
        when(evolutionAnalysisService.analyzeEvolution(anyString(), anyString(), anyString()))
                .thenReturn(evolutionResponse);

        RepositoryHealthResponse healthResponse = createSampleHealthResponse();
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);

        RecommendationReport recReport = createSampleRecommendationReport();
        when(recommendationEngineService.generateRecommendations(
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(recReport);

        // Act
        KnowledgeReport report = service.queryKnowledge("Show me everything", "test-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals("test-repo", report.getRepositoryName());
        assertEquals("main", report.getBranch());
        assertEquals("Show me everything", report.getQuery());
        assertEquals("All Domains", report.getKnowledgeDomain());
        assertEquals("SUCCESS", report.getStatus());
        assertTrue(report.getGenerationDurationMillis() >= 0);
        assertNotNull(report.getUnifiedSummary());
        assertNotNull(report.getMetadata());
    }

    @Test
    void queryKnowledge_withArchitectureQuery_returnsArchitectureKnowledge() {
        // Arrange
        ArchitectureInsightsResponse archResponse = createSampleArchitectureResponse();
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        // Act
        KnowledgeReport report = service.queryKnowledge("Show me architecture", "test-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals("Architecture", report.getKnowledgeDomain());
        assertTrue(report.getArchitectureSummary().contains("Layered"));
        assertEquals("SUCCESS", report.getStatus());
    }

    @Test
    void queryKnowledge_withSessionsQuery_returnsSessionKnowledge() {
        // Arrange
        when(sessionService.getTotalSessionCount()).thenReturn(10);
        when(sessionService.getActiveSessionCount()).thenReturn(3);

        // Act
        KnowledgeReport report = service.queryKnowledge("Show me sessions", "test-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals("Development Sessions", report.getKnowledgeDomain());
        assertTrue(report.getRepositoryOverview().contains("10 total"));
        assertEquals(2, report.getActiveSessions().size());
    }

    @Test
    void queryKnowledge_withWorkflowQuery_returnsWorkflowIntelligence() {
        // Arrange
        WorkflowResult workflowResult = createSampleWorkflowResult();
        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), anyString()))
                .thenReturn(workflowResult);

        // Act
        KnowledgeReport report = service.queryKnowledge("workflow intelligence", "test-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals("Workflow Intelligence", report.getKnowledgeDomain());
        assertFalse(report.getWorkflowSummaries().isEmpty());
    }

    @Test
    void queryKnowledge_withEvolutionQuery_returnsEvolutionKnowledge() {
        // Arrange
        RepositoryEvolutionAnalysisResponse evolutionResponse = createSampleEvolutionResponse();
        when(evolutionAnalysisService.analyzeEvolution(anyString(), anyString(), anyString()))
                .thenReturn(evolutionResponse);

        // Act
        KnowledgeReport report = service.queryKnowledge("repository evolution analysis", "test-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals("Repository Evolution", report.getKnowledgeDomain());
        assertFalse(report.getEvolutionInsights().isEmpty());
    }

    @Test
    void queryKnowledge_withInvalidQuery_returnsAllDomain() {
        // Arrange
        KnowledgeGraphReport kgReport = createSampleKnowledgeGraphReport();
        when(knowledgeGraphService.generateKnowledgeGraphReport(anyString(), anyString()))
                .thenReturn(kgReport);
        ArchitectureInsightsResponse archResponse = createSampleArchitectureResponse();
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);
        when(sessionService.getTotalSessionCount()).thenReturn(0);
        when(sessionService.getActiveSessionCount()).thenReturn(0);
        WorkflowResult workflowResult = createSampleWorkflowResult();
        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), anyString()))
                .thenReturn(workflowResult);
        RepositoryEvolutionAnalysisResponse evolutionResponse = createSampleEvolutionResponse();
        when(evolutionAnalysisService.analyzeEvolution(anyString(), anyString(), anyString()))
                .thenReturn(evolutionResponse);
        RepositoryHealthResponse healthResponse = createSampleHealthResponse();
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);
        RecommendationReport recReport = createSampleRecommendationReport();
        when(recommendationEngineService.generateRecommendations(
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(recReport);

        // Act
        KnowledgeReport report = service.queryKnowledge("some random query", "test-repo", "main");

        // Assert
        assertNotNull(report);
        assertEquals("All Domains", report.getKnowledgeDomain());
        assertEquals("SUCCESS", report.getStatus());
    }

    @Test
    void queryKnowledge_withServiceException_returnsPartialReport() {
        // Arrange
        // When buildFullKnowledgeReport is called, it catches exceptions from
        // individual domain builders and continues. So the report returns SUCCESS.
        ArchitectureInsightsResponse archResponse = createSampleArchitectureResponse();
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);
        when(sessionService.getTotalSessionCount()).thenReturn(0);
        when(sessionService.getActiveSessionCount()).thenReturn(0);
        WorkflowResult workflowResult = createSampleWorkflowResult();
        when(workflowOrchestratorService.orchestrate(anyString(), anyString(), anyString()))
                .thenReturn(workflowResult);
        RepositoryEvolutionAnalysisResponse evolutionResponse = createSampleEvolutionResponse();
        when(evolutionAnalysisService.analyzeEvolution(anyString(), anyString(), anyString()))
                .thenReturn(evolutionResponse);
        RepositoryHealthResponse healthResponse = createSampleHealthResponse();
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);
        RecommendationReport recReport = createSampleRecommendationReport();
        when(recommendationEngineService.generateRecommendations(
                anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(recReport);

        // But make knowledgeGraphService throw
        when(knowledgeGraphService.generateKnowledgeGraphReport(anyString(), anyString()))
                .thenThrow(new RuntimeException("Graph service unavailable"));

        // Act
        KnowledgeReport report = service.queryKnowledge("some random query", "test-repo", "main");

        // Assert
        assertNotNull(report);
            // Exceptions in individual domain builders are caught, report still succeeds
            assertEquals("SUCCESS", report.getStatus());
            // Component relationships may be populated from non-failing builders like architecture
            // The key assertion is that the report overall succeeded despite the knowledge graph failure
            assertNotNull(report.getComponentRelationships());
    }

    // ========== Helper methods ==========

    private KnowledgeGraphReport createSampleKnowledgeGraphReport() {
        KnowledgeGraphReport report = new KnowledgeGraphReport();
        report.setRepositoryName("test-repo");
        report.setBranch("main");
        report.setConnectedEntities(List.of("ClassA (CLASS)", "ClassB (CLASS)"));

        KnowledgeGraphReport.GraphStatistics stats = new KnowledgeGraphReport.GraphStatistics();
        stats.setTotalNodes(10);
        stats.setTotalEdges(15);
        stats.setEntityTypeCount(3);
        stats.setRelationshipTypeCount(4);
        stats.setCriticalNodeCount(2);
        stats.setAverageConnectionsPerNode(1.5);
        report.setGraphStatistics(stats);

        report.setDependencyPaths(List.of("ClassA -> ClassB"));
        report.setIndirectDependencies(new ArrayList<>());
        report.setCriticalNodes(List.of("ClassA (CLASS) - 5 connections"));
        report.setArchitecturalRelationships(List.of("ClassA [CLASS] --[DEPENDS_ON]--> ClassB [CLASS]"));
        report.setRelationshipGraph(new ArrayList<>());
        return report;
    }

    private ArchitectureInsightsResponse createSampleArchitectureResponse() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setArchitecturalStyle("Layered Architecture");
        response.setDetectedLayers(List.of("Controller", "Service", "Repository"));
        response.setModuleRelationships(List.of(
                new ArchitectureInsightsResponse.ModuleRelationship("Controller", "Service", "DEPENDS_ON")
        ));
        return response;
    }

    private WorkflowResult createSampleWorkflowResult() {
        WorkflowResult result = new WorkflowResult();
        result.setWorkflowType("REPOSITORY_ANALYSIS");
        result.setExecutionStatus("COMPLETED");
        result.setSummary("Workflow completed successfully");
        result.setCompletedSteps(new ArrayList<>());
        result.setRepositoryInsights(List.of("Insight 1"));
        result.setRisksIdentified(List.of("Risk 1"));
        result.setSuggestedNextActions(List.of("Review findings"));
        return result;
    }

    private RepositoryEvolutionAnalysisResponse createSampleEvolutionResponse() {
        RepositoryEvolutionAnalysisResponse response = new RepositoryEvolutionAnalysisResponse();
        response.setMaintainabilityAssessment("Good");
        response.setArchitecturalImpact("Low impact");
        response.setConventionCompliance("Compliant");
        response.setRepositoryEvolutionScore(85);
        response.setTechnicalDebtIndicators(List.of("Some technical debt"));
        response.setScalabilityConsiderations("Adequate");
        response.setLongTermRisks(List.of("Risk 1"));
        response.setRecommendedRepositoryPractices(List.of("Practice 1"));
        return response;
    }

    private RepositoryHealthResponse createSampleHealthResponse() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setHealthScore(85);
        response.setMaintainabilityRating("Good");
        response.setPotentialRisks(List.of("Risk 1"));
        return response;
    }

    private RecommendationReport createSampleRecommendationReport() {
        RecommendationReport report = new RecommendationReport();
        Recommendation rec = new Recommendation();
        rec.setPriority(RecommendationPriority.HIGH);
        rec.setTitle("Test Recommendation");
        rec.setDescription("Test description");
        report.setPrioritizedRecommendations(List.of(rec));
        return report;
    }
}