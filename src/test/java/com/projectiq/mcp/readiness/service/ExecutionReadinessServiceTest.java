package com.projectiq.mcp.readiness.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.orchestration.service.WorkflowExecutionService;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import com.projectiq.mcp.readiness.dto.AssessmentCategory;
import com.projectiq.mcp.readiness.dto.ReadinessLevel;
import com.projectiq.mcp.readiness.dto.ReadinessReport;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.recommendation.service.RecommendationEngineService;
import com.projectiq.mcp.validation.dto.ValidationFinding;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.service.WorkflowValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the ExecutionReadinessService.
 */
@ExtendWith(MockitoExtension.class)
class ExecutionReadinessServiceTest {

    @Mock
    private WorkflowOrchestratorService workflowOrchestratorService;
    @Mock
    private WorkflowExecutionService workflowExecutionService;
    @Mock
    private IntelligentContextPipelineService contextPipelineService;
    @Mock
    private ExecutionPlanningService executionPlanningService;
    @Mock
    private WorkflowValidationService workflowValidationService;
    @Mock
    private RecommendationEngineService recommendationEngineService;
    @Mock
    private RepositoryHealthService repositoryHealthService;
    @Mock
    private ArchitectureInsightsService architectureInsightsService;

    private ExecutionReadinessService service;

    @BeforeEach
    void setUp() {
        service = new ExecutionReadinessService(
                workflowOrchestratorService,
                workflowExecutionService,
                contextPipelineService,
                executionPlanningService,
                workflowValidationService,
                recommendationEngineService,
                repositoryHealthService,
                architectureInsightsService);
    }

    @Test
    void testReadyWorkflow() {
        // Mock execution plan
        ExecutionPlanResponse planResponse = new ExecutionPlanResponse();
        planResponse.setPlanStatus("READY");
        planResponse.setOrderedImplementationTasks(new ArrayList<>());
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(planResponse);

        // Mock validation report
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setBlockingIssues(0);
        validationReport.setWarnings(0);
        when(workflowValidationService.validateWorkflow(anyString(), anyString(), anyString(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        // Mock recommendation report
        RecommendationReport recReport = new RecommendationReport();
        RecommendationReport.ReportSummary summary = new RecommendationReport.ReportSummary();
        summary.setCriticalCount(0);
        summary.setHighCount(0);
        recReport.setRecommendationSummary(summary);
        when(recommendationEngineService.generateRecommendations(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn(recReport);

        // Mock repository health
        RepositoryHealthResponse healthResponse = new RepositoryHealthResponse();
        healthResponse.setHealthScore(85);
        healthResponse.setMaintainabilityRating("Good");
        healthResponse.setTestingMaturity("Mature");
        healthResponse.setArchitectureConsistency("Consistent");
        healthResponse.setDependencyHealth("Healthy");
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);

        // Mock architecture insights
        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered Architecture");
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        ReadinessReport report = service.assessReadiness(
                "test-workflow", "Feature Implementation", "Implement feature X",
                "test-repo", "main");

        assertNotNull(report);
        assertEquals("test-workflow", report.getWorkflowName());
        assertNotNull(report.getOverallReadinessLevel());
        assertTrue(report.getReadinessScore() >= 0);
        assertNotNull(report.getFinalImplementationRecommendation());
        assertNotNull(report.getAssessmentSummary());
    }

    @Test
    void testWorkflowWithWarnings() {
        ExecutionPlanResponse planResponse = new ExecutionPlanResponse();
        planResponse.setPlanStatus("READY");
        List<ExecutionPlanResponse.ImplementationTask> tasks = new ArrayList<>();
        planResponse.setOrderedImplementationTasks(tasks);
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(planResponse);

        // Validation with warnings
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED_WITH_WARNINGS");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setBlockingIssues(0);
        validationReport.setWarnings(2);
        when(workflowValidationService.validateWorkflow(any(), any(), any(),
                anyList(), anyList(), any(), any()))
                .thenReturn(validationReport);

        RecommendationReport recReport = new RecommendationReport();
        RecommendationReport.ReportSummary summary = new RecommendationReport.ReportSummary();
        summary.setCriticalCount(0);
        summary.setHighCount(1);
        recReport.setRecommendationSummary(summary);
        when(recommendationEngineService.generateRecommendations(any(), any(),
                any(), any(), any()))
                .thenReturn(recReport);

        RepositoryHealthResponse healthResponse = new RepositoryHealthResponse();
        healthResponse.setHealthScore(70);
        healthResponse.setMaintainabilityRating("Good");
        healthResponse.setTestingMaturity("Developing");
        healthResponse.setArchitectureConsistency("Mostly Consistent");
        healthResponse.setDependencyHealth("Moderate");
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered Architecture");
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        ReadinessReport report = service.assessReadiness(
                "test-workflow", "Feature Implementation", null,
                "test-repo", "main");

        assertNotNull(report);
        // With warning mocks, we expect the assessment to complete and produce a level/score
        assertNotNull(report.getOverallReadinessLevel());
        assertTrue(report.getReadinessScore() >= 0);
        // The final recommendation is always produced regardless of outcome
        assertNotNull(report.getFinalImplementationRecommendation());
    }

    @Test
    void testWorkflowWithBlockingIssues() {
        ExecutionPlanResponse planResponse = new ExecutionPlanResponse();
        planResponse.setPlanStatus("READY");
        planResponse.setOrderedImplementationTasks(new ArrayList<>());
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(planResponse);

        // Validation with blocking issues
        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("BLOCKED");
        List<ValidationFinding> findings = new ArrayList<>();
        findings.add(new ValidationFinding(
                com.projectiq.mcp.validation.dto.ValidationCategory.WORKFLOW_VALIDATION,
                com.projectiq.mcp.validation.dto.ValidationSeverity.CRITICAL,
                "Workflow name is missing", "Details", true));
        validationReport.setFindings(findings);
        validationReport.setBlockingIssues(1);
        validationReport.setWarnings(0);
        when(workflowValidationService.validateWorkflow(anyString(), anyString(), anyString(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        RecommendationReport recReport = new RecommendationReport();
        RecommendationReport.ReportSummary summary = new RecommendationReport.ReportSummary();
        summary.setCriticalCount(1);
        summary.setHighCount(0);
        recReport.setRecommendationSummary(summary);
        when(recommendationEngineService.generateRecommendations(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn(recReport);

        RepositoryHealthResponse healthResponse = new RepositoryHealthResponse();
        healthResponse.setHealthScore(40);
        healthResponse.setMaintainabilityRating("Poor");
        healthResponse.setTestingMaturity("Minimal");
        healthResponse.setArchitectureConsistency("Inconsistent");
        healthResponse.setDependencyHealth("Concerning");
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered Architecture");
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        ReadinessReport report = service.assessReadiness(
                "test-workflow", "Bug Fix", "Fix critical bug",
                "test-repo", "main");

        assertNotNull(report);
        assertTrue(report.getBlockingIssues().size() > 0
                || report.getOverallReadinessLevel() == ReadinessLevel.NOT_READY);
    }

    @Test
    void testMissingRepositoryInformation() {
        ExecutionPlanResponse planResponse = new ExecutionPlanResponse();
        planResponse.setPlanStatus("READY");
        planResponse.setOrderedImplementationTasks(new ArrayList<>());
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(planResponse);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setBlockingIssues(0);
        validationReport.setWarnings(0);
        when(workflowValidationService.validateWorkflow(anyString(), anyString(), anyString(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        RecommendationReport recReport = new RecommendationReport();
        RecommendationReport.ReportSummary summary = new RecommendationReport.ReportSummary();
        summary.setCriticalCount(0);
        summary.setHighCount(0);
        recReport.setRecommendationSummary(summary);
        when(recommendationEngineService.generateRecommendations(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn(recReport);

        // Repository health returns null (unavailable)
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(null);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered Architecture");
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        ReadinessReport report = service.assessReadiness(
                "test-workflow", "Feature Implementation", "Implement feature",
                "unknown-repo", "main");

        assertNotNull(report);
        assertNull(report.getRepositorySummary());
    }

    @Test
    void testInvalidWorkflow() {
        ReadinessReport report = service.assessReadiness(
                null, null, null, "test-repo", "main");

        assertNotNull(report);
        assertTrue(report.getBlockingIssues().size() > 0
                || report.getErrors().size() > 0);
    }

    @Test
    void testEmptyWorkflow() {
        ReadinessReport report = service.assessReadiness(
                "", "", "", "test-repo", "main");

        assertNotNull(report);
        assertTrue(report.getBlockingIssues().size() > 0
                || report.getErrors().size() > 0);
    }

    @Test
    void testDeterministicOutput() {
        ExecutionPlanResponse planResponse = new ExecutionPlanResponse();
        planResponse.setPlanStatus("READY");
        planResponse.setOrderedImplementationTasks(new ArrayList<>());
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(planResponse);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setBlockingIssues(0);
        validationReport.setWarnings(0);
        when(workflowValidationService.validateWorkflow(anyString(), anyString(), anyString(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        RecommendationReport recReport = new RecommendationReport();
        RecommendationReport.ReportSummary summary = new RecommendationReport.ReportSummary();
        summary.setCriticalCount(0);
        summary.setHighCount(0);
        recReport.setRecommendationSummary(summary);
        when(recommendationEngineService.generateRecommendations(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn(recReport);

        RepositoryHealthResponse healthResponse = new RepositoryHealthResponse();
        healthResponse.setHealthScore(90);
        healthResponse.setMaintainabilityRating("Excellent");
        healthResponse.setTestingMaturity("Mature");
        healthResponse.setArchitectureConsistency("Consistent");
        healthResponse.setDependencyHealth("Healthy");
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered Architecture");
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        // Run twice to verify deterministic output
        ReadinessReport report1 = service.assessReadiness(
                "test-workflow", "Feature Implementation", "Implement feature",
                "test-repo", "main");

        ReadinessReport report2 = service.assessReadiness(
                "test-workflow", "Feature Implementation", "Implement feature",
                "test-repo", "main");

        assertNotNull(report1);
        assertNotNull(report2);
        assertEquals(report1.getOverallReadinessLevel(), report2.getOverallReadinessLevel());
        assertEquals(report1.getReadinessScore(), report2.getReadinessScore());
        assertEquals(report1.getBlockingIssues().size(), report2.getBlockingIssues().size());
        assertEquals(report1.getWarnings().size(), report2.getWarnings().size());
    }

    @Test
    void testCategoryAssessmentsPresent() {
        // Fix: Import Mockito.any() already auto-imported via static import
        ExecutionPlanResponse planResponse = new ExecutionPlanResponse();
        planResponse.setPlanStatus("READY");
        List<ExecutionPlanResponse.ImplementationTask> tasks = new ArrayList<>();
        planResponse.setOrderedImplementationTasks(tasks);
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(planResponse);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setFindings(new ArrayList<>());
        validationReport.setBlockingIssues(0);
        validationReport.setWarnings(0);
        when(workflowValidationService.validateWorkflow(anyString(), anyString(), anyString(),
                anyList(), anyList(), anyString(), anyString()))
                .thenReturn(validationReport);

        RecommendationReport recReport = new RecommendationReport();
        RecommendationReport.ReportSummary summary = new RecommendationReport.ReportSummary();
        summary.setCriticalCount(0);
        summary.setHighCount(0);
        recReport.setRecommendationSummary(summary);
        when(recommendationEngineService.generateRecommendations(anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn(recReport);

        RepositoryHealthResponse healthResponse = new RepositoryHealthResponse();
        healthResponse.setHealthScore(85);
        healthResponse.setMaintainabilityRating("Good");
        healthResponse.setTestingMaturity("Mature");
        healthResponse.setArchitectureConsistency("Consistent");
        healthResponse.setDependencyHealth("Healthy");
        when(repositoryHealthService.analyzeHealth(anyString(), anyString()))
                .thenReturn(healthResponse);

        ArchitectureInsightsResponse archResponse = new ArchitectureInsightsResponse();
        archResponse.setArchitecturalStyle("Layered Architecture");
        when(architectureInsightsService.analyzeArchitecture(anyString(), anyString()))
                .thenReturn(archResponse);

        ReadinessReport report = service.assessReadiness(
                "test-workflow", "Feature Implementation", "Implement feature",
                "test-repo", "main");

        assertNotNull(report.getCategoryAssessments());
        assertFalse(report.getCategoryAssessments().isEmpty());

        // Verify that at least some categories are present (the exact count depends on mock results)
        List<AssessmentCategory> categories = report.getCategoryAssessments().stream()
                .map(ReadinessReport.CategoryAssessment::getCategory)
                .toList();
        assertFalse(categories.isEmpty());
        // WORKFLOW should always be present (evaluated first before any mocks)
        assertTrue(categories.contains(AssessmentCategory.WORKFLOW));
    }
}