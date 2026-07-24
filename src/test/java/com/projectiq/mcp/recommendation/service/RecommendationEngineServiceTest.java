package com.projectiq.mcp.recommendation.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.dto.TestImpactAnalysisResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ImplementationPlanningService;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.analysis.service.TestImpactAnalysisService;
import com.projectiq.mcp.orchestration.service.WorkflowExecutionService;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import com.projectiq.mcp.recommendation.dto.Recommendation;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.service.WorkflowValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationEngineServiceTest {

    @Mock
    private WorkflowValidationService workflowValidationService;
    @Mock
    private ExecutionPlanningService executionPlanningService;
    @Mock
    private WorkflowExecutionService workflowExecutionService;
    @Mock
    private ImplementationPlanningService implementationPlanningService;
    @Mock
    private ArchitectureInsightsService architectureInsightsService;
    @Mock
    private RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    @Mock
    private RepositoryHealthService repositoryHealthService;
    @Mock
    private TestImpactAnalysisService testImpactAnalysisService;

    private RecommendationEngineService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationEngineService(
                workflowValidationService,
                executionPlanningService,
                workflowExecutionService,
                implementationPlanningService,
                architectureInsightsService,
                repositoryConventionAnalyzerService,
                repositoryHealthService,
                testImpactAnalysisService
        );
    }

    @Test
    void shouldGenerateRecommendationsForFeatureImplementation() {
        // Given
        String workflowName = "Add User Authentication";
        String workflowType = "Feature Implementation";
        String originalRequest = "Implement user authentication with JWT";
        String repositoryName = "my-project";
        String branch = "main";

        RepositoryHealthResponse healthResponse = new RepositoryHealthResponse();
        healthResponse.setHealthScore(75);
        healthResponse.setMaintainabilityRating("good");
        healthResponse.setDependencyHealth("healthy");
        healthResponse.setTestingMaturity("moderate");
        healthResponse.setArchitectureConsistency("consistent");
        when(repositoryHealthService.analyzeHealth(eq(repositoryName), anyString()))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("layered");
        archResponse.setDetectedLayers(List.of("controller", "service", "repository"));
        when(architectureInsightsService.analyzeArchitecture(eq(repositoryName), anyString()))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        RepositoryConventionResponse.NamingConventions namingConvs =
                new RepositoryConventionResponse.NamingConventions();
        namingConvs.setClassNamingConvention("PascalCase");
        convResponse.setNamingConventions(namingConvs);
        convResponse.setProjectSpecificObservations(new ArrayList<>());
        when(repositoryConventionAnalyzerService.analyzeConventions(eq(repositoryName), anyString()))
                .thenReturn(convResponse);

        TestImpactAnalysisResponse testImpactResponse = new TestImpactAnalysisResponse();
        testImpactResponse.setRelatedTestClasses(List.of("AuthControllerTest", "AuthServiceTest"));
        testImpactResponse.setMissingTests(new ArrayList<>());
        when(testImpactAnalysisService.analyzeTestImpact(anyString(), eq(repositoryName), anyString()))
                .thenReturn(testImpactResponse);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setRecommendedActions(new ArrayList<>());
        validationReport.setReadinessScore(80);
        when(workflowValidationService.validateWorkflow(
                anyString(), anyString(), any(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        ExecutionPlanResponse planResponse = new ExecutionPlanResponse();
        planResponse.setPlanStatus("VALID");
        planResponse.setExecutionPhases(List.of(
                new ExecutionPlanResponse.ExecutionPhase()));
        planResponse.setOrderedImplementationTasks(List.of(
                new ExecutionPlanResponse.ImplementationTask()));
        planResponse.setPotentialRisks(new ArrayList<>());
        planResponse.setRequiredPrerequisites(new ArrayList<>());
        planResponse.setPlanningSummary(new ExecutionPlanResponse.PlanningSummary());
        when(executionPlanningService.generateExecutionPlan(any()))
                .thenReturn(planResponse);

        // When
        RecommendationReport report = service.generateRecommendations(
                workflowName, workflowType, originalRequest, repositoryName, branch);

        // Then
        assertNotNull(report);
        assertEquals(workflowName, report.getWorkflowName());
        assertEquals(workflowType, report.getWorkflowType());
        assertNotNull(report.getExecutiveSummary());
        assertTrue(report.getConfidenceScore() >= 0);
        assertTrue(report.getConfidenceScore() <= 100);
        assertNotNull(report.getPrioritizedRecommendations());
        assertNotNull(report.getImplementationAdvice());
        assertNotNull(report.getTestingRecommendations());
        assertNotNull(report.getRecommendationSummary());
    }

    @Test
    void shouldHandleEmptyWorkflowName() {
        RecommendationReport report = service.generateRecommendations(
                null, null, null, "my-project", null);

        assertNotNull(report);
        assertTrue(report.getPrioritizedRecommendations().stream()
                .anyMatch(r -> r.getTitle().contains("Define workflow name")));
    }

    @Test
    void shouldHandleMissingRepository() {
        RepositoryHealthResponse healthResponse = new RepositoryHealthResponse();
        healthResponse.setHealthScore(25);
        healthResponse.setMaintainabilityRating("poor");
        healthResponse.setDependencyHealth("poor");
        healthResponse.setTestingMaturity("immature");
        healthResponse.setArchitectureConsistency("inconsistent");
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("layered");
        archResponse.setDetectedLayers(new ArrayList<>());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setProjectSpecificObservations(new ArrayList<>());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(convResponse);

        when(workflowValidationService.validateWorkflow(
                anyString(), anyString(), any(),
                anyList(), anyList(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Validation failed"));

        when(executionPlanningService.generateExecutionPlan(any()))
                .thenThrow(new RuntimeException("Planning failed"));

        RecommendationReport report = service.generateRecommendations(
                "Test Workflow", null, null, "unknown-repo", null);

        assertNotNull(report);
        assertNotNull(report.getErrors());
        assertTrue(report.getConfidenceScore() >= 0);
    }

    @Test
    void shouldHandleValidationBlockedStatus() {
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(new RepositoryHealthResponse());

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setDetectedLayers(new ArrayList<>());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setProjectSpecificObservations(new ArrayList<>());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(convResponse);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("BLOCKED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setRecommendedActions(new ArrayList<>());
        validationReport.setReadinessScore(20);
        when(workflowValidationService.validateWorkflow(
                anyString(), anyString(), any(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        RecommendationReport report = service.generateRecommendations(
                "Test", "Bug Fix", "Fix login issue", "my-project", "main");

        assertNotNull(report);
        assertTrue(report.getPrioritizedRecommendations().stream()
                .anyMatch(r -> r.getTitle().contains("BLOCKED")));
    }

    @Test
    void shouldGenerateBugFixRecommendations() {
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(new RepositoryHealthResponse());

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setDetectedLayers(new ArrayList<>());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setProjectSpecificObservations(new ArrayList<>());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(convResponse);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setRecommendedActions(new ArrayList<>());
        validationReport.setReadinessScore(80);
        when(workflowValidationService.validateWorkflow(
                anyString(), anyString(), any(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        RecommendationReport report = service.generateRecommendations(
                "Fix Login Bug", "Bug Fix", "Users cannot log in", "my-project", "main");

        assertNotNull(report);
        assertTrue(report.getTestingRecommendations().stream()
                .anyMatch(r -> r.toLowerCase().contains("regression")));
    }

    @Test
    void shouldNotContainDuplicateRecommendations() {
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(new RepositoryHealthResponse());

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setDetectedLayers(new ArrayList<>());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setProjectSpecificObservations(new ArrayList<>());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(convResponse);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setRecommendedActions(new ArrayList<>());
        validationReport.setReadinessScore(80);
        when(workflowValidationService.validateWorkflow(
                anyString(), anyString(), any(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        RecommendationReport report1 = service.generateRecommendations(
                "Test", "Feature", "Desc", "my-project", "main");

        RecommendationReport report2 = service.generateRecommendations(
                "Test", "Feature", "Desc", "my-project", "main");

        assertEquals(report1.getPrioritizedRecommendations().size(),
                report2.getPrioritizedRecommendations().size());
    }

    @Test
    void shouldOrderRecommendationsByPriority() {
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(new RepositoryHealthResponse());

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setDetectedLayers(new ArrayList<>());
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setProjectSpecificObservations(new ArrayList<>());
        when(repositoryConventionAnalyzerService.analyzeConventions(anyString(), anyString()))
                .thenReturn(convResponse);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setRecommendedActions(new ArrayList<>());
        validationReport.setReadinessScore(80);
        when(workflowValidationService.validateWorkflow(
                anyString(), anyString(), any(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        RecommendationReport report = service.generateRecommendations(
                "Test", "Feature", "Desc", "my-project", "main");

        List<Recommendation> recs = report.getPrioritizedRecommendations();
        if (recs.size() > 1) {
            for (int i = 0; i < recs.size() - 1; i++) {
                int currentPriority = recs.get(i).getPriority().ordinal();
                int nextPriority = recs.get(i + 1).getPriority().ordinal();
                assertTrue(currentPriority <= nextPriority,
                        "Recommendations should be sorted by priority");
            }
        }
    }
}