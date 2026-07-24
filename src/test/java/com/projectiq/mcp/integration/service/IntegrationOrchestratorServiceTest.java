package com.projectiq.mcp.integration.service;

import com.projectiq.mcp.analysis.dto.*;
import com.projectiq.mcp.analysis.service.TaskAnalysisService;
import com.projectiq.mcp.handoff.service.AgentHandoffService;
import com.projectiq.mcp.integration.dto.EndToEndWorkflowResponse;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import com.projectiq.mcp.pipeline.dto.ContextPackage;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import com.projectiq.mcp.readiness.dto.ReadinessLevel;
import com.projectiq.mcp.readiness.dto.ReadinessReport;
import com.projectiq.mcp.readiness.service.ExecutionReadinessService;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.recommendation.service.RecommendationEngineService;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.service.WorkflowValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IntegrationOrchestratorServiceTest {

    @Mock
    private TaskAnalysisService taskAnalysisService;

    @Mock
    private WorkflowOrchestratorService workflowOrchestratorService;

    @Mock
    private IntelligentContextPipelineService contextPipelineService;

    @Mock
    private ExecutionPlanningService executionPlanningService;

    @Mock
    private WorkflowValidationService workflowValidationService;

    @Mock
    private RecommendationEngineService recommendationEngineService;

    @Mock
    private ExecutionReadinessService executionReadinessService;

    @Mock
    private DevelopmentSessionService developmentSessionService;

    @Mock
    private AgentHandoffService agentHandoffService;

    private IntegrationOrchestratorService integrationService;

    @BeforeEach
    void setUp() {
        integrationService = new IntegrationOrchestratorService(
                taskAnalysisService, workflowOrchestratorService,
                contextPipelineService, executionPlanningService,
                workflowValidationService, recommendationEngineService,
                executionReadinessService, developmentSessionService,
                agentHandoffService);
    }

    @Test
    void testExecuteEndToEndWorkflow_AllStagesSucceed() {
        String request = "Add a new REST endpoint for user management";
        String repositoryName = "my-project";
        String branch = "main";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.NEW_FEATURE);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);
        taskAnalysis.setEstimatedComplexity(ComplexityLevel.MEDIUM);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);

        WorkflowResult workflowResult = new WorkflowResult();
        workflowResult.setWorkflowType("Feature Implementation");
        workflowResult.setExecutionStatus("COMPLETED");
        when(workflowOrchestratorService.orchestrate(request, repositoryName, branch))
                .thenReturn(workflowResult);

        ContextPackage contextPackage = new ContextPackage();
        contextPackage.setTotalContextItems(15);
        when(contextPipelineService.buildContextPipeline(
                any(), any(), any(), any(), any()))
                .thenReturn(contextPackage);

        ExecutionPlanResponse executionPlan = new ExecutionPlanResponse();
        executionPlan.setPlanStatus("READY");
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(executionPlan);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setReadinessScore(85);
        when(workflowValidationService.validateWorkflow(
                any(), any(), any(), any(), any(),
                any(), any()))
                .thenReturn(validationReport);

        RecommendationReport recommendationReport = new RecommendationReport();
        when(recommendationEngineService.generateRecommendations(
                any(), any(), any(), any(), any()))
                .thenReturn(recommendationReport);

        ReadinessReport readinessReport = new ReadinessReport();
        readinessReport.setOverallReadinessLevel(ReadinessLevel.READY);
        readinessReport.setReadinessScore(90);
        when(executionReadinessService.assessReadiness(
                any(), any(), any(), any(), any()))
                .thenReturn(readinessReport);

        DevelopmentSession session = new DevelopmentSession();
        session.setSessionId("test-session-001");
        when(developmentSessionService.createSession(
                any(), any(), any(), any()))
                .thenReturn(session);

        String handoffJson = "{\"sessionId\":\"test-session-001\"}";
        when(agentHandoffService.exportHandoffPackage(any()))
                .thenReturn(handoffJson);

        EndToEndWorkflowResponse response = integrationService.executeEndToEndWorkflow(
                request, repositoryName, branch);

        assertNotNull(response);
        assertEquals("COMPLETED", response.getOverallStatus());
        assertNotNull(response.getWorkflowId());
        assertEquals(request, response.getOriginalRequest());
        assertEquals(repositoryName, response.getRepositoryName());
        assertTrue(response.getTotalDurationMillis() >= 0);
        assertNotNull(response.getTaskAnalysis());
        assertNotNull(response.getWorkflowResult());
        assertNotNull(response.getContextPackage());
        assertNotNull(response.getExecutionPlan());
        assertNotNull(response.getValidationReport());
        assertNotNull(response.getRecommendationReport());
        assertNotNull(response.getReadinessReport());
        assertNotNull(response.getDevelopmentSession());
        assertNotNull(response.getHandoffPackage());
        assertTrue(response.getErrors().isEmpty());
        assertTrue(response.getWarnings().isEmpty());

        verify(taskAnalysisService).analyze(request);
        verify(workflowOrchestratorService).orchestrate(request, repositoryName, branch);
        verify(contextPipelineService).buildContextPipeline(
                any(), any(), any(), any(), any());
        verify(executionPlanningService).generateExecutionPlan(any(ExecutionPlanRequest.class));
        verify(workflowValidationService).validateWorkflow(
                any(), any(), any(), any(), any(),
                any(), any());
        verify(recommendationEngineService).generateRecommendations(
                any(), any(), any(), any(), any());
        verify(executionReadinessService).assessReadiness(
                any(), any(), any(), any(), any());
        verify(developmentSessionService).createSession(
                any(), any(), any(), any());
        verify(agentHandoffService).exportHandoffPackage(any());
    }

    @Test
    void testExecuteEndToEndWorkflow_WithWarnings() {
        String request = "Fix bug in authentication";
        String repositoryName = "my-project";

        when(taskAnalysisService.analyze(request))
                .thenThrow(new RuntimeException("Indexer unavailable"));

        WorkflowResult workflowResult = new WorkflowResult();
        workflowResult.setWorkflowType("Bug Fix");
        workflowResult.setExecutionStatus("COMPLETED");
        when(workflowOrchestratorService.orchestrate(request, repositoryName, "main"))
                .thenReturn(workflowResult);

        ContextPackage contextPackage = new ContextPackage();
        contextPackage.setTotalContextItems(10);
        when(contextPipelineService.buildContextPipeline(
                any(), any(), any(), any(), any()))
                .thenReturn(contextPackage);

        ExecutionPlanResponse executionPlan = new ExecutionPlanResponse();
        executionPlan.setPlanStatus("READY");
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(executionPlan);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setReadinessScore(80);
        when(workflowValidationService.validateWorkflow(
                any(), any(), any(), any(), any(),
                any(), any()))
                .thenReturn(validationReport);

        RecommendationReport recommendationReport = new RecommendationReport();
        when(recommendationEngineService.generateRecommendations(
                any(), any(), any(), any(), any()))
                .thenReturn(recommendationReport);

        ReadinessReport readinessReport = new ReadinessReport();
        readinessReport.setOverallReadinessLevel(ReadinessLevel.READY_WITH_WARNINGS);
        readinessReport.setReadinessScore(75);
        when(executionReadinessService.assessReadiness(
                any(), any(), any(), any(), any()))
                .thenReturn(readinessReport);

        DevelopmentSession session = new DevelopmentSession();
        session.setSessionId("test-session-002");
        when(developmentSessionService.createSession(
                any(), any(), any(), any()))
                .thenReturn(session);

        when(agentHandoffService.exportHandoffPackage(any()))
                .thenReturn("{\"sessionId\":\"test-session-002\"}");

        EndToEndWorkflowResponse response = integrationService.executeEndToEndWorkflow(
                request, repositoryName, null);

        assertNotNull(response);
        assertEquals("COMPLETED_WITH_WARNINGS", response.getOverallStatus());
        assertTrue(response.hasWarnings());
        assertTrue(response.getWarnings().stream()
                .anyMatch(w -> w.contains("Task analysis failed")));
        assertNotNull(response.getWorkflowResult());
        assertNotNull(response.getContextPackage());
        assertNotNull(response.getDevelopmentSession());
    }

    @Test
    void testExecuteEndToEndWorkflow_NullRequest() {
        assertThrows(IllegalArgumentException.class, () ->
                integrationService.executeEndToEndWorkflow(null, "repo", "main"));
    }

    @Test
    void testExecuteEndToEndWorkflow_EmptyRequest() {
        assertThrows(IllegalArgumentException.class, () ->
                integrationService.executeEndToEndWorkflow("", "repo", "main"));
    }

    @Test
    void testExecuteEndToEndWorkflow_NullRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                integrationService.executeEndToEndWorkflow("request", null, "main"));
    }

    @Test
    void testExecuteEndToEndWorkflow_EmptyRepository() {
        assertThrows(IllegalArgumentException.class, () ->
                integrationService.executeEndToEndWorkflow("request", "", "main"));
    }

    @Test
    void testExecuteEndToEndWorkflow_SessionCreationFails() {
        String request = "Add logging";
        String repositoryName = "my-project";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.UNKNOWN);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.MEDIUM);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);

        WorkflowResult workflowResult = new WorkflowResult();
        workflowResult.setWorkflowType("Repository Analysis");
        workflowResult.setExecutionStatus("COMPLETED");
        when(workflowOrchestratorService.orchestrate(request, repositoryName, "main"))
                .thenReturn(workflowResult);

        ContextPackage contextPackage = new ContextPackage();
        contextPackage.setTotalContextItems(8);
        when(contextPipelineService.buildContextPipeline(
                any(), any(), any(), any(), any()))
                .thenReturn(contextPackage);

        ExecutionPlanResponse executionPlan = new ExecutionPlanResponse();
        executionPlan.setPlanStatus("READY");
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(executionPlan);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setReadinessScore(90);
        when(workflowValidationService.validateWorkflow(
                any(), any(), any(), any(), any(),
                any(), any()))
                .thenReturn(validationReport);

        RecommendationReport recommendationReport = new RecommendationReport();
        when(recommendationEngineService.generateRecommendations(
                any(), any(), any(), any(), any()))
                .thenReturn(recommendationReport);

        ReadinessReport readinessReport = new ReadinessReport();
        readinessReport.setOverallReadinessLevel(ReadinessLevel.READY);
        readinessReport.setReadinessScore(95);
        when(executionReadinessService.assessReadiness(
                any(), any(), any(), any(), any()))
                .thenReturn(readinessReport);

        when(developmentSessionService.createSession(
                any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Session limit reached"));

        EndToEndWorkflowResponse response = integrationService.executeEndToEndWorkflow(
                request, repositoryName, "main");

        assertNotNull(response);
        assertEquals("COMPLETED_WITH_WARNINGS", response.getOverallStatus());
        assertNull(response.getDevelopmentSession());
        assertNull(response.getHandoffPackage());
    }

    @Test
    void testExecuteEndToEndWorkflow_AllStagesFail() {
        String request = "Complex refactoring";
        String repositoryName = "my-project";

        when(taskAnalysisService.analyze(request))
                .thenThrow(new RuntimeException("Task analysis error"));
        when(workflowOrchestratorService.orchestrate(request, repositoryName, "main"))
                .thenThrow(new RuntimeException("Workflow error"));
        when(contextPipelineService.buildContextPipeline(
                any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Context error"));
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenThrow(new RuntimeException("Planning error"));
        when(workflowValidationService.validateWorkflow(
                any(), any(), any(), any(), any(),
                any(), any()))
                .thenThrow(new RuntimeException("Validation error"));
        when(recommendationEngineService.generateRecommendations(
                any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Recommendation error"));
        when(executionReadinessService.assessReadiness(
                any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Readiness error"));
        when(developmentSessionService.createSession(
                any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Session error"));

        EndToEndWorkflowResponse response = integrationService.executeEndToEndWorkflow(
                request, repositoryName, "main");

        assertNotNull(response);
        assertEquals("COMPLETED_WITH_WARNINGS", response.getOverallStatus());
        assertTrue(response.hasWarnings());
        assertTrue(response.getWarnings().size() >= 8);
        assertNull(response.getTaskAnalysis());
        assertNull(response.getWorkflowResult());
        assertNull(response.getContextPackage());
        assertNull(response.getExecutionPlan());
        assertNull(response.getValidationReport());
        assertNull(response.getRecommendationReport());
        assertNull(response.getReadinessReport());
        assertNull(response.getDevelopmentSession());
        assertNull(response.getHandoffPackage());
    }

    @Test
    void testExecuteEndToEndWorkflow_DeterministicExecution() {
        String request = "Add unit tests";
        String repositoryName = "my-project";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.UNIT_TEST);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);

        WorkflowResult workflowResult = new WorkflowResult();
        workflowResult.setWorkflowType("Test Improvement");
        workflowResult.setExecutionStatus("COMPLETED");
        when(workflowOrchestratorService.orchestrate(request, repositoryName, "main"))
                .thenReturn(workflowResult);

        ContextPackage contextPackage = new ContextPackage();
        contextPackage.setTotalContextItems(12);
        when(contextPipelineService.buildContextPipeline(
                any(), any(), any(), any(), any()))
                .thenReturn(contextPackage);

        ExecutionPlanResponse executionPlan = new ExecutionPlanResponse();
        executionPlan.setPlanStatus("READY");
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(executionPlan);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setReadinessScore(88);
        when(workflowValidationService.validateWorkflow(
                any(), any(), any(), any(), any(),
                any(), any()))
                .thenReturn(validationReport);

        RecommendationReport recommendationReport = new RecommendationReport();
        when(recommendationEngineService.generateRecommendations(
                any(), any(), any(), any(), any()))
                .thenReturn(recommendationReport);

        ReadinessReport readinessReport = new ReadinessReport();
        readinessReport.setOverallReadinessLevel(ReadinessLevel.READY);
        readinessReport.setReadinessScore(92);
        when(executionReadinessService.assessReadiness(
                any(), any(), any(), any(), any()))
                .thenReturn(readinessReport);

        DevelopmentSession session = new DevelopmentSession();
        session.setSessionId("test-session-004");
        when(developmentSessionService.createSession(
                any(), any(), any(), any()))
                .thenReturn(session);

        when(agentHandoffService.exportHandoffPackage(any()))
                .thenReturn("{\"sessionId\":\"test-session-004\"}");

        EndToEndWorkflowResponse response1 = integrationService.executeEndToEndWorkflow(
                request, repositoryName, "main");
        EndToEndWorkflowResponse response2 = integrationService.executeEndToEndWorkflow(
                request, repositoryName, "main");

        assertEquals(response1.getOverallStatus(), response2.getOverallStatus());
        assertEquals(response1.getOriginalRequest(), response2.getOriginalRequest());
        assertEquals(response1.getRepositoryName(), response2.getRepositoryName());
        assertNotNull(response1.getWorkflowId());
        assertNotNull(response2.getWorkflowId());
        assertNotEquals(response1.getWorkflowId(), response2.getWorkflowId());
    }

    @Test
    void testExecuteEndToEndWorkflow_WithBranch() {
        String request = "Update configuration";
        String repositoryName = "my-project";
        String branch = "develop";

        TaskAnalysisResponse taskAnalysis = new TaskAnalysisResponse();
        taskAnalysis.setTaskType(TaskType.CONFIGURATION_CHANGE);
        taskAnalysis.setConfidenceLevel(ConfidenceLevel.HIGH);
        when(taskAnalysisService.analyze(request)).thenReturn(taskAnalysis);

        WorkflowResult workflowResult = new WorkflowResult();
        workflowResult.setWorkflowType("Configuration Change");
        workflowResult.setExecutionStatus("COMPLETED");
        when(workflowOrchestratorService.orchestrate(request, repositoryName, branch))
                .thenReturn(workflowResult);

        ContextPackage contextPackage = new ContextPackage();
        contextPackage.setTotalContextItems(5);
        when(contextPipelineService.buildContextPipeline(
                any(), any(), any(), any(), any()))
                .thenReturn(contextPackage);

        ExecutionPlanResponse executionPlan = new ExecutionPlanResponse();
        executionPlan.setPlanStatus("READY");
        when(executionPlanningService.generateExecutionPlan(any(ExecutionPlanRequest.class)))
                .thenReturn(executionPlan);

        ValidationReport validationReport = new ValidationReport();
        validationReport.setOverallStatus("PASSED");
        validationReport.setReadinessScore(85);
        when(workflowValidationService.validateWorkflow(
                any(), any(), any(), any(), any(),
                any(), any()))
                .thenReturn(validationReport);

        RecommendationReport recommendationReport = new RecommendationReport();
        when(recommendationEngineService.generateRecommendations(
                any(), any(), any(), any(), any()))
                .thenReturn(recommendationReport);

        ReadinessReport readinessReport = new ReadinessReport();
        readinessReport.setOverallReadinessLevel(ReadinessLevel.READY);
        readinessReport.setReadinessScore(85);
        when(executionReadinessService.assessReadiness(
                any(), any(), any(), any(), any()))
                .thenReturn(readinessReport);

        DevelopmentSession session = new DevelopmentSession();
        session.setSessionId("test-session-005");
        when(developmentSessionService.createSession(
                any(), any(), any(), any()))
                .thenReturn(session);

        when(agentHandoffService.exportHandoffPackage(any()))
                .thenReturn("{\"sessionId\":\"test-session-005\"}");

        EndToEndWorkflowResponse response = integrationService.executeEndToEndWorkflow(
                request, repositoryName, branch);

        assertNotNull(response);
        assertEquals("COMPLETED", response.getOverallStatus());
        verify(workflowOrchestratorService).orchestrate(request, repositoryName, branch);
    }
}