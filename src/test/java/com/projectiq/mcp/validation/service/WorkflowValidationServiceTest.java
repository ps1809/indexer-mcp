package com.projectiq.mcp.validation.service;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse.NamingConventions;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.dto.RiskLevel;
import com.projectiq.mcp.analysis.dto.ScopeLevel;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ImpactAnalysisService;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.analysis.service.TestImpactAnalysisService;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanDependency;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import com.projectiq.mcp.validation.dto.ValidationCategory;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.dto.ValidationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for the WorkflowValidationService.
 */
@ExtendWith(MockitoExtension.class)
class WorkflowValidationServiceTest {

    @Mock
    private WorkflowOrchestratorService workflowOrchestratorService;
    @Mock
    private ExecutionPlanningService executionPlanningService;
    @Mock
    private ImpactAnalysisService impactAnalysisService;
    @Mock
    private ArchitectureInsightsService architectureInsightsService;
    @Mock
    private RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    @Mock
    private RepositoryHealthService repositoryHealthService;
    @Mock
    private TestImpactAnalysisService testImpactAnalysisService;

    private WorkflowValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new WorkflowValidationService(
                workflowOrchestratorService,
                executionPlanningService,
                impactAnalysisService,
                architectureInsightsService,
                repositoryConventionAnalyzerService,
                repositoryHealthService,
                testImpactAnalysisService);
    }

    @Test
    void shouldValidateValidWorkflow() {
        // Arrange
        List<PlanStep> steps = List.of(
                new PlanStep("step1", "First step", "implementation"),
                new PlanStep("step2", "Second step", "testing")
        );

        RepositoryHealthResponse healthResponse = createHealthResponse(85, "good", "good");
        when(repositoryHealthService.analyzeHealth(eq("test-repo"), eq("main")))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered");
        archResponse.setDetectedLayers(List.of("controller", "service", "repository"));
        when(architectureInsightsService.analyzeArchitecture(eq("test-repo"), eq("main")))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setNamingConventions(new NamingConventions());
        convResponse.setProjectSpecificObservations(List.of());
        when(repositoryConventionAnalyzerService.analyzeConventions(eq("test-repo"), eq("main")))
                .thenReturn(convResponse);

        ImpactAnalysisResponse impactResponse = new ImpactAnalysisResponse();
        impactResponse.setEstimatedImplementationScope(ScopeLevel.MEDIUM);
        impactResponse.setPotentialRisks(List.of());
        when(impactAnalysisService.analyzeImpact(anyString(), eq("test-repo"), eq("main")))
                .thenReturn(impactResponse);

        // Act
        ValidationReport report = validationService.validateWorkflow(
                "test-workflow",
                "Feature Implementation",
                "Implement a new feature",
                steps,
                List.of(),
                "test-repo",
                "main");

        // Assert
        assertNotNull(report);
        assertEquals("PASSED", report.getOverallStatus());
        assertTrue(report.getReadinessScore() >= 80);
        assertTrue(report.getFindings().stream()
                .anyMatch(f -> f.getCategory() == ValidationCategory.WORKFLOW_VALIDATION));
        assertTrue(report.getFindings().stream()
                .anyMatch(f -> f.getCategory() == ValidationCategory.REPOSITORY_VALIDATION));
        assertTrue(report.getFindings().stream()
                .anyMatch(f -> f.getCategory() == ValidationCategory.ARCHITECTURE_VALIDATION));
    }

    @Test
    void shouldDetectInvalidWorkflow() {
        // Act
        ValidationReport report = validationService.validateWorkflow(
                "",
                null,
                null,
                List.of(),
                List.of(),
                "test-repo",
                null);

        // Assert
        assertNotNull(report);
        assertEquals("BLOCKED", report.getOverallStatus());
        assertTrue(report.getBlockingIssues() > 0);
        assertTrue(report.getFindings().stream()
                .anyMatch(f -> f.getSeverity() == ValidationSeverity.CRITICAL));
    }

    @Test
    void shouldDetectCircularDependencies() {
        // Arrange
        List<PlanStep> steps = List.of(
                new PlanStep("stepA", "Step A", "implementation"),
                new PlanStep("stepB", "Step B", "implementation")
        );

        List<PlanDependency> dependencies = List.of(
                new PlanDependency("stepA", List.of("stepB"), "A depends on B"),
                new PlanDependency("stepB", List.of("stepA"), "B depends on A")
        );

        RepositoryHealthResponse healthResponse = createHealthResponse(90, "good", "good");
        when(repositoryHealthService.analyzeHealth(eq("test-repo"), eq("main")))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Microservices");
        archResponse.setDetectedLayers(List.of("api", "core"));
        when(architectureInsightsService.analyzeArchitecture(eq("test-repo"), eq("main")))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setNamingConventions(new NamingConventions());
        when(repositoryConventionAnalyzerService.analyzeConventions(eq("test-repo"), eq("main")))
                .thenReturn(convResponse);

        ImpactAnalysisResponse impactResponse = new ImpactAnalysisResponse();
        impactResponse.setEstimatedImplementationScope(ScopeLevel.SMALL);
        impactResponse.setPotentialRisks(List.of());
        when(impactAnalysisService.analyzeImpact(anyString(), eq("test-repo"), eq("main")))
                .thenReturn(impactResponse);

        // Act
        ValidationReport report = validationService.validateWorkflow(
                "test-workflow",
                "Bug Fix",
                "Fix a bug",
                steps,
                dependencies,
                "test-repo",
                "main");

        // Assert
        assertNotNull(report);
        assertTrue(report.getFindings().stream()
                .anyMatch(f -> f.getCategory() == ValidationCategory.DEPENDENCY_VALIDATION
                        && f.getSeverity() == ValidationSeverity.CRITICAL
                        && f.getMessage().contains("Circular dependency")));
    }

    @Test
    void shouldDetectMissingDependencies() {
        // Arrange
        List<PlanStep> steps = List.of(
                new PlanStep("stepA", "Step A", "implementation")
        );

        List<PlanDependency> dependencies = List.of(
                new PlanDependency("nonExistentStep", List.of("stepA"), "Refers to non-existent step")
        );

        RepositoryHealthResponse healthResponse = createHealthResponse(90, "good", "good");
        when(repositoryHealthService.analyzeHealth(eq("test-repo"), eq("main")))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered");
        archResponse.setDetectedLayers(List.of("controller", "service"));
        when(architectureInsightsService.analyzeArchitecture(eq("test-repo"), eq("main")))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setNamingConventions(new NamingConventions());
        when(repositoryConventionAnalyzerService.analyzeConventions(eq("test-repo"), eq("main")))
                .thenReturn(convResponse);

        ImpactAnalysisResponse impactResponse = new ImpactAnalysisResponse();
        impactResponse.setEstimatedImplementationScope(ScopeLevel.SMALL);
        impactResponse.setPotentialRisks(List.of());
        when(impactAnalysisService.analyzeImpact(anyString(), eq("test-repo"), eq("main")))
                .thenReturn(impactResponse);

        // Act
        ValidationReport report = validationService.validateWorkflow(
                "test-workflow",
                "Feature",
                "Request",
                steps,
                dependencies,
                "test-repo",
                "main");

        // Assert
        assertNotNull(report);
        assertTrue(report.getFindings().stream()
                .anyMatch(f -> f.getCategory() == ValidationCategory.DEPENDENCY_VALIDATION
                        && f.getMessage().contains("non-existent")));
    }

    @Test
    void shouldHandleRepositoryHealthFailures() {
        // Arrange
        List<PlanStep> steps = List.of(
                new PlanStep("step1", "First step", "implementation")
        );

        // Very low health score
        RepositoryHealthResponse healthResponse = createHealthResponse(25, "poor", "poor");
        when(repositoryHealthService.analyzeHealth(eq("test-repo"), eq("main")))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered");
        archResponse.setDetectedLayers(List.of("controller"));
        when(architectureInsightsService.analyzeArchitecture(eq("test-repo"), eq("main")))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setNamingConventions(new NamingConventions());
        when(repositoryConventionAnalyzerService.analyzeConventions(eq("test-repo"), eq("main")))
                .thenReturn(convResponse);

        ImpactAnalysisResponse impactResponse = new ImpactAnalysisResponse();
        impactResponse.setEstimatedImplementationScope(ScopeLevel.SMALL);
        impactResponse.setPotentialRisks(List.of());
        when(impactAnalysisService.analyzeImpact(anyString(), eq("test-repo"), eq("main")))
                .thenReturn(impactResponse);

        // Act
        ValidationReport report = validationService.validateWorkflow(
                "test-workflow",
                "Feature",
                "Request",
                steps,
                List.of(),
                "test-repo",
                "main");

        // Assert
        assertNotNull(report);
        assertTrue(report.getFindings().stream()
                .anyMatch(f -> f.getCategory() == ValidationCategory.REPOSITORY_VALIDATION
                        && f.getSeverity() == ValidationSeverity.CRITICAL));
        assertTrue(report.getReadinessScore() < 50);
    }

    @Test
    void shouldEvaluateTestReadiness() {
        // Arrange
        List<PlanStep> steps = List.of(
                new PlanStep("step1", "First step", "implementation")
        );

        RepositoryHealthResponse healthResponse = createHealthResponse(80, "good", "good");
        when(repositoryHealthService.analyzeHealth(eq("test-repo"), eq("main")))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered");
        archResponse.setDetectedLayers(List.of("controller"));
        when(architectureInsightsService.analyzeArchitecture(eq("test-repo"), eq("main")))
                .thenReturn(archResponse);

        RepositoryConventionResponse convResponse = new RepositoryConventionResponse();
        convResponse.setNamingConventions(new NamingConventions());
        when(repositoryConventionAnalyzerService.analyzeConventions(eq("test-repo"), eq("main")))
                .thenReturn(convResponse);

        ImpactAnalysisResponse impactResponse = new ImpactAnalysisResponse();
        impactResponse.setEstimatedImplementationScope(ScopeLevel.SMALL);
        impactResponse.setPotentialRisks(List.of());
        when(impactAnalysisService.analyzeImpact(anyString(), eq("test-repo"), eq("main")))
                .thenReturn(impactResponse);

        // Act
        ValidationReport report = validationService.validateWorkflow(
                "test-workflow",
                "Feature",
                "Request",
                steps,
                List.of(),
                "test-repo",
                "main");

        // Assert
        assertNotNull(report);
        assertTrue(report.getFindings().stream()
                .anyMatch(f -> f.getCategory() == ValidationCategory.TEST_COVERAGE_VALIDATION));
    }

    @Test
    void shouldHandleEmptyWorkflow() {
        // Act
        ValidationReport report = validationService.validateWorkflow(
                null,
                null,
                null,
                null,
                null,
                "test-repo",
                null);

        // Assert
        assertNotNull(report);
        assertNotNull(report.getOverallStatus());
        // Should not throw exceptions
        assertTrue(report.getErrors().isEmpty() || report.getErrors() != null);
    }

    private RepositoryHealthResponse createHealthResponse(int score, String maintainability, String depHealth) {
        RepositoryHealthResponse response = new RepositoryHealthResponse();
        response.setHealthScore(score);
        response.setMaintainabilityRating(maintainability);
        response.setComplexityRating("moderate");
        response.setTestingMaturity("adequate");
        response.setDependencyHealth(depHealth);
        response.setArchitectureConsistency("consistent");
        response.setRepositoryName("test-repo");
        return response;
    }
}