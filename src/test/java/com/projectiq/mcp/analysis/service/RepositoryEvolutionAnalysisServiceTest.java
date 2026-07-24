package com.projectiq.mcp.analysis.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryEvolutionAnalysisServiceTest {

    @Mock
    private CodeChangeAnalysisService codeChangeAnalysisService;
    @Mock
    private DependencyChangePredictionService dependencyChangePredictionService;
    @Mock
    private RefactoringImpactSimulationService refactoringImpactSimulationService;
    @Mock
    private ArchitectureInsightsService architectureInsightsService;
    @Mock
    private RepositoryHealthService repositoryHealthService;
    @Mock
    private RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    @Mock
    private IntelligentContextPipelineService intelligentContextPipelineService;
    @Mock
    private IndexerRestClient indexerRestClient;

    private RepositoryEvolutionAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new RepositoryEvolutionAnalysisService(
                codeChangeAnalysisService,
                dependencyChangePredictionService,
                refactoringImpactSimulationService,
                architectureInsightsService,
                repositoryHealthService,
                repositoryConventionAnalyzerService,
                intelligentContextPipelineService,
                indexerRestClient
        );
    }

    @Test
    void testAnalyzeEvolutionWithNullSummary() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(null);

        RepositoryEvolutionAnalysisResponse response = service.analyzeEvolution(
                "test-repo", "main", "Add new feature");

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertEquals("Add new feature", response.getProposedChange());
        assertEquals("Repository data not available. Unable to analyze evolution.",
                response.getProposedChangeSummary());
        assertEquals(Integer.valueOf(0), response.getRepositoryEvolutionScore());
    }

    @Test
    void testAnalyzeEvolutionWithEmptyBranch() {
        RepositorySummaryResponse summary = createSampleSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        when(architectureInsightsService.analyzeArchitecture("test-repo", "main"))
                .thenReturn(createArchitectureInsights());
        when(repositoryHealthService.analyzeHealth("test-repo", "main"))
                .thenReturn(createHealthResponse());
        when(repositoryConventionAnalyzerService.analyzeConventions("test-repo", "main"))
                .thenReturn(createConventionResponse());

        RepositoryEvolutionAnalysisResponse response = service.analyzeEvolution(
                "test-repo", null, "Add new feature");

        assertEquals("main", response.getBranch());
    }

    @Test
    void testAnalyzeEvolutionWithArchitectureChange() {
        RepositorySummaryResponse summary = createSampleSummary();
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(summary);

        when(architectureInsightsService.analyzeArchitecture("test-repo", "main"))
                .thenReturn(createArchitectureInsights());
        when(repositoryHealthService.analyzeHealth("test-repo", "main"))
                .thenReturn(createHealthResponse());
        when(repositoryConventionAnalyzerService.analyzeConventions("test-repo", "main"))
                .thenReturn(createConventionResponse());

        RepositoryEvolutionAnalysisResponse response = service.analyzeEvolution(
                "test-repo", "main", "Add new controller and repository layer");

        assertNotNull(response);
        assertEquals("test-repo", response.getRepositoryName());
        assertNotNull(response.getArchitectureEvolution());
        assertNotNull(response.getPackageGrowth());
        assertNotNull(response.getModuleExpansion());
        assertNotNull(response.getDependencyEvolution());
        assertNotNull(response.getConventionConsistency());
        assertNotNull(response.getMaintainability());
        assertNotNull(response.getTechnicalDebtAnalysis());
        assertNotNull(response.getScalabilityReadiness());
        assertNotNull(response.getLongTermRisks());
        assertNotNull(response.getRecommendedRepositoryPractices());
        assertNotNull(response.getRepositoryEvolutionScore());
    }

    @Test
    void testBuildChangeSummary() {
        RepositorySummaryResponse summary = createSampleSummary();
        String result = service.buildChangeSummary("Add new service", summary);
        assertTrue(result.contains("Add new service"));
        assertTrue(result.contains("test-repo"));
    }

    @Test
    void testBuildChangeSummaryWithNullSummary() {
        String result = service.buildChangeSummary("Add new service", null);
        assertTrue(result.contains("Add new service"));
    }

    @Test
    void testAnalyzePackageGrowthWithNoPackageKeywords() {
        RepositorySummaryResponse summary = createSampleSummary();
        var result = service.analyzePackageGrowth("Add new file", summary);
        assertEquals(Integer.valueOf(0), result.getEstimatedNewPackages());
    }

    @Test
    void testAnalyzePackageGrowthWithPackageKeywords() {
        RepositorySummaryResponse summary = createSampleSummary();
        var result = service.analyzePackageGrowth("Add new package for module", summary);
        assertEquals(Integer.valueOf(1), result.getEstimatedNewPackages());
    }

    @Test
    void testAnalyzeModuleExpansion() {
        RepositorySummaryResponse summary = createSampleSummary();
        var result = service.analyzeModuleExpansion("Add new service", summary);
        assertEquals(Integer.valueOf(1), result.getEstimatedNewClasses());
    }

    @Test
    void testAnalyzeDependencyEvolution() {
        RepositorySummaryResponse summary = createSampleSummary();
        var result = service.analyzeDependencyEvolution(
                "test-repo", "main", "Add new library dependency", summary);
        assertEquals(Integer.valueOf(1), result.getEstimatedNewDependencies());
        assertFalse(result.getCircularDependencyRisk());
    }

    @Test
    void testAnalyzeDependencyEvolutionWithCircularRisk() {
        RepositorySummaryResponse summary = createSampleSummary();
        var result = service.analyzeDependencyEvolution(
                "test-repo", "main", "Add controller directly accessing repository", summary);
        assertTrue(result.getCircularDependencyRisk());
    }

    @Test
    void testAnalyzeConventionConsistency() {
        when(repositoryConventionAnalyzerService.analyzeConventions("test-repo", "main"))
                .thenReturn(createConventionResponse());

        var result = service.analyzeConventionConsistency("test-repo", "main");
        assertNotNull(result);
        assertNotNull(result.getConventionScore());
    }

    @Test
    void testAnalyzeConventionConsistencyWithError() {
        when(repositoryConventionAnalyzerService.analyzeConventions("test-repo", "main"))
                .thenThrow(new RuntimeException("Connection error"));

        var result = service.analyzeConventionConsistency("test-repo", "main");
        assertNotNull(result);
        assertTrue(result.getDeviations().size() > 0);
    }

    @Test
    void testAnalyzeTechnicalDebtIndicators() {
        RepositorySummaryResponse summary = createSampleSummary();
        when(repositoryHealthService.analyzeHealth("test-repo", "main"))
                .thenReturn(createHealthResponse());

        var result = service.analyzeTechnicalDebtIndicators(
                "test-repo", "main", "Add new feature", summary);
        assertNotNull(result);
        assertNotNull(result.getTechnicalDebtScore());
    }

    @Test
    void testAnalyzeTechnicalDebtIndicatorsWithWorkaround() {
        RepositorySummaryResponse summary = createSampleSummary();
        when(repositoryHealthService.analyzeHealth("test-repo", "main"))
                .thenReturn(createHealthResponse());

        var result = service.analyzeTechnicalDebtIndicators(
                "test-repo", "main", "Quick fix workaround for bug", summary);
        assertNotNull(result);
        assertTrue(result.getDebtIndicators().size() > 0);
    }

    @Test
    void testAnalyzeScalabilityReadiness() {
        RepositorySummaryResponse summary = createSampleSummary();
        var result = service.analyzeScalabilityReadiness("Add new feature", summary);
        assertNotNull(result);
        assertNotNull(result.getScalabilityScore());
    }

    @Test
    void testAnalyzeScalabilityReadinessWithScalabilityKeywords() {
        RepositorySummaryResponse summary = createSampleSummary();
        var result = service.analyzeScalabilityReadiness(
                "Add new cache layer for performance", summary);
        assertTrue(result.getScalabilityScore() >= 50);
    }

    @Test
    void testIdentifyLongTermRisks() {
        var archEvolution = new RepositoryEvolutionAnalysisResponse.ArchitectureEvolutionAnalysis();
        archEvolution.setArchitectureScore(30);
        archEvolution.setArchitecturalDrifts(java.util.List.of("Drift"));

        var depEvolution = new RepositoryEvolutionAnalysisResponse.DependencyEvolutionAnalysis();
        depEvolution.setCircularDependencyRisk(true);
        depEvolution.setEstimatedNewDependencies(3);

        var debtAnalysis = new RepositoryEvolutionAnalysisResponse.TechnicalDebtIndicatorsAnalysis();
        debtAnalysis.setTechnicalDebtScore(40);

        var scalabilityAnalysis = new RepositoryEvolutionAnalysisResponse.ScalabilityReadinessAnalysis();
        scalabilityAnalysis.setScalabilityScore(30);

        var risks = service.identifyLongTermRisks(archEvolution, depEvolution, debtAnalysis, scalabilityAnalysis);
        assertFalse(risks.isEmpty());
        assertTrue(risks.stream().anyMatch(r -> r.contains("Architecture")));
        assertTrue(risks.stream().anyMatch(r -> r.contains("Circular")));
        assertTrue(risks.stream().anyMatch(r -> r.contains("debt")));
        assertTrue(risks.stream().anyMatch(r -> r.contains("scalability")));
    }

    @Test
    void testGenerateRecommendedPractices() {
        var archEvolution = new RepositoryEvolutionAnalysisResponse.ArchitectureEvolutionAnalysis();
        archEvolution.setArchitectureScore(40);

        var conventionAnalysis = new RepositoryEvolutionAnalysisResponse.ConventionConsistencyAnalysis();
        conventionAnalysis.setConventionScore(50);

        var maintAnalysis = new RepositoryEvolutionAnalysisResponse.MaintainabilityAnalysis();
        maintAnalysis.setMaintainabilityScore(40);
        maintAnalysis.setComplexityConcerns(java.util.List.of("Complex"));

        var debtAnalysis = new RepositoryEvolutionAnalysisResponse.TechnicalDebtIndicatorsAnalysis();
        debtAnalysis.setTechnicalDebtScore(50);

        var scalabilityAnalysis = new RepositoryEvolutionAnalysisResponse.ScalabilityReadinessAnalysis();
        scalabilityAnalysis.setScalabilityScore(40);

        var recommendations = service.generateRecommendedPractices(
                archEvolution, conventionAnalysis, maintAnalysis, debtAnalysis, scalabilityAnalysis);
        assertFalse(recommendations.isEmpty());
    }

    @Test
    void testCalculateEvolutionScore() {
        var archEvolution = new RepositoryEvolutionAnalysisResponse.ArchitectureEvolutionAnalysis();
        archEvolution.setArchitectureScore(80);

        var pkgGrowth = new RepositoryEvolutionAnalysisResponse.PackageGrowthAnalysis();
        var moduleExp = new RepositoryEvolutionAnalysisResponse.ModuleExpansionAnalysis();
        moduleExp.setModuleCohesionScore(70);
        var depEvolution = new RepositoryEvolutionAnalysisResponse.DependencyEvolutionAnalysis();

        var conventionAnalysis = new RepositoryEvolutionAnalysisResponse.ConventionConsistencyAnalysis();
        conventionAnalysis.setConventionScore(85);

        var maintAnalysis = new RepositoryEvolutionAnalysisResponse.MaintainabilityAnalysis();
        maintAnalysis.setMaintainabilityScore(75);

        var debtAnalysis = new RepositoryEvolutionAnalysisResponse.TechnicalDebtIndicatorsAnalysis();
        debtAnalysis.setTechnicalDebtScore(90);

        var scalabilityAnalysis = new RepositoryEvolutionAnalysisResponse.ScalabilityReadinessAnalysis();
        scalabilityAnalysis.setScalabilityScore(80);

        int score = service.calculateEvolutionScore(
                archEvolution, pkgGrowth, moduleExp, depEvolution,
                conventionAnalysis, maintAnalysis, debtAnalysis, scalabilityAnalysis);
        assertTrue(score > 0);
        assertTrue(score <= 100);
    }

    private RepositorySummaryResponse createSampleSummary() {
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test-repo");
        summary.setBranch("main");
        summary.setPackageCount(10);
        summary.setClassCount(100);
        summary.setMethodCount(500);
        summary.setFileCount(50);
        return summary;
    }

    private ArchitectureInsightsResponse createArchitectureInsights() {
        ArchitectureInsightsResponse response = new ArchitectureInsightsResponse();
        response.setArchitecturalStyle("Layered Architecture");
        response.setDetectedLayers(java.util.List.of(
                "Controller (Presentation)", "Service (Business Logic)", "Repository (Data Access)"));
        response.setPotentialConcerns(new java.util.ArrayList<>());
        return response;
    }

    private RepositoryHealthResponse createHealthResponse() {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setHealthScore(70);
        response.setPotentialRisks(new java.util.ArrayList<>());
        return response;
    }

    private RepositoryConventionResponse createConventionResponse() {
        RepositoryConventionResponse response = new RepositoryConventionResponse();
        response.setProjectSpecificObservations(new java.util.ArrayList<>());
        response.setConfidenceLevel("HIGH");
        return response;
    }
}