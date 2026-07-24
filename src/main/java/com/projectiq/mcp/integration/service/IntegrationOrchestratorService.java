package com.projectiq.mcp.integration.service;

import com.projectiq.mcp.analysis.service.TaskAnalysisService;
import com.projectiq.mcp.handoff.service.AgentHandoffService;
import com.projectiq.mcp.integration.dto.EndToEndWorkflowResponse;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import com.projectiq.mcp.pipeline.service.IntelligentContextPipelineService;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanDependency;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import com.projectiq.mcp.readiness.service.ExecutionReadinessService;
import com.projectiq.mcp.recommendation.service.RecommendationEngineService;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import com.projectiq.mcp.validation.service.WorkflowValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service that coordinates the complete end-to-end development workflow by
 * integrating all Phase 3 Intelligent AI Agent Orchestration capabilities.
 *
 * <p>The integration pipeline executes the following stages in order:
 * <ol>
 *   <li>Task Analysis - Analyze the developer request</li>
 *   <li>Workflow Orchestration - Generate and execute the workflow</li>
 *   <li>Context Assembly - Build intelligent context pipeline</li>
 *   <li>Execution Planning - Generate execution roadmap</li>
 *   <li>Workflow Validation - Validate workflow readiness</li>
 *   <li>Recommendation Generation - Generate prioritized recommendations</li>
 *   <li>Readiness Assessment - Assess execution readiness</li>
 *   <li>Development Session - Create and manage development session</li>
 *   <li>Agent Handoff - Export handoff package for AI agent</li>
 * </ol>
 *
 * <p>This service handles errors gracefully: non-critical stage failures are
 * recorded as warnings and execution continues. Critical failures are recorded
 * as errors and the overall status reflects the failure.</p>
 */
@Service
public class IntegrationOrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(IntegrationOrchestratorService.class);

    private final TaskAnalysisService taskAnalysisService;
    private final WorkflowOrchestratorService workflowOrchestratorService;
    private final IntelligentContextPipelineService contextPipelineService;
    private final ExecutionPlanningService executionPlanningService;
    private final WorkflowValidationService workflowValidationService;
    private final RecommendationEngineService recommendationEngineService;
    private final ExecutionReadinessService executionReadinessService;
    private final DevelopmentSessionService developmentSessionService;
    private final AgentHandoffService agentHandoffService;

    public IntegrationOrchestratorService(
            TaskAnalysisService taskAnalysisService,
            WorkflowOrchestratorService workflowOrchestratorService,
            IntelligentContextPipelineService contextPipelineService,
            ExecutionPlanningService executionPlanningService,
            WorkflowValidationService workflowValidationService,
            RecommendationEngineService recommendationEngineService,
            ExecutionReadinessService executionReadinessService,
            DevelopmentSessionService developmentSessionService,
            AgentHandoffService agentHandoffService) {
        this.taskAnalysisService = taskAnalysisService;
        this.workflowOrchestratorService = workflowOrchestratorService;
        this.contextPipelineService = contextPipelineService;
        this.executionPlanningService = executionPlanningService;
        this.workflowValidationService = workflowValidationService;
        this.recommendationEngineService = recommendationEngineService;
        this.executionReadinessService = executionReadinessService;
        this.developmentSessionService = developmentSessionService;
        this.agentHandoffService = agentHandoffService;
    }

    /**
     * Executes the complete end-to-end development workflow for the given
     * developer request, coordinating all Phase 3 services.
     *
     * @param request        the developer request (required)
     * @param repositoryName the repository name (required)
     * @param branch         the git branch (optional, defaults to "main")
     * @return a complete EndToEndWorkflowResponse with all stage results
     * @throws IllegalArgumentException if request or repositoryName is null/empty
     */
    public EndToEndWorkflowResponse executeEndToEndWorkflow(
            String request, String repositoryName, String branch) {

        long startTime = System.currentTimeMillis();
        logger.info("=== Starting End-to-End Integration Workflow ===");
        logger.info("Request: {}, Repository: {}, Branch: {}",
                request, repositoryName, branch != null ? branch : "main");

        if (request == null || request.trim().isEmpty()) {
            throw new IllegalArgumentException("Developer request cannot be null or empty");
        }
        if (repositoryName == null || repositoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository name cannot be null or empty");
        }

        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
        String workflowId = UUID.randomUUID().toString();

        EndToEndWorkflowResponse response = new EndToEndWorkflowResponse()
                .withWorkflowId(workflowId)
                .withOriginalRequest(request.trim())
                .withRepositoryName(repositoryName.trim());

        String workflowType = "UNKNOWN";
        var stagesProgress = new ArrayList<String>();

        try {
            // ============================================================
            // Stage 1: Task Analysis
            // ============================================================
            stagesProgress.add("TASK_ANALYSIS");
            logger.info("Stage 1/9: Task Analysis");
            try {
                var taskAnalysis = taskAnalysisService.analyze(request);
                response.setTaskAnalysis(taskAnalysis);
                if (taskAnalysis != null && taskAnalysis.getTaskType() != null) {
                    workflowType = taskAnalysis.getTaskType().getDisplayName();
                }
                logger.info("Task analysis complete: type={}", workflowType);
            } catch (Exception e) {
                logger.warn("Task analysis failed: {}", e.getMessage());
                response.addWarning("Task analysis failed: " + e.getMessage());
            }

            // ============================================================
            // Stage 2: Workflow Orchestration
            // ============================================================
            stagesProgress.add("WORKFLOW_ORCHESTRATION");
            logger.info("Stage 2/9: Workflow Orchestration");
            try {
                WorkflowResult workflowResult = workflowOrchestratorService.orchestrate(
                        request, repositoryName, effectiveBranch);
                response.setWorkflowResult(workflowResult);
                logger.info("Workflow orchestration complete: type={}, status={}",
                        workflowResult != null ? workflowResult.getWorkflowType() : "N/A",
                        workflowResult != null ? workflowResult.getExecutionStatus() : "N/A");
            } catch (Exception e) {
                logger.warn("Workflow orchestration failed: {}", e.getMessage());
                response.addWarning("Workflow orchestration failed: " + e.getMessage());
            }

            // ============================================================
            // Stage 3: Context Assembly
            // ============================================================
            stagesProgress.add("CONTEXT_ASSEMBLY");
            logger.info("Stage 3/9: Context Assembly (Intelligent Context Pipeline)");
            try {
                var contextPackage = contextPipelineService.buildContextPipeline(
                        request, workflowType, repositoryName, effectiveBranch, request);
                response.setContextPackage(contextPackage);
                logger.info("Context assembly complete: {} items",
                        contextPackage != null ? contextPackage.getTotalContextItems() : 0);
            } catch (Exception e) {
                logger.warn("Context assembly failed: {}", e.getMessage());
                response.addWarning("Context assembly failed: " + e.getMessage());
            }

            // ============================================================
            // Stage 4: Execution Planning
            // ============================================================
            stagesProgress.add("EXECUTION_PLANNING");
            logger.info("Stage 4/9: Execution Planning");
            try {
                var planRequest = new ExecutionPlanRequest(
                        request, workflowType, request, new ArrayList<>(), new ArrayList<>());
                var executionPlan = executionPlanningService.generateExecutionPlan(planRequest);
                response.setExecutionPlan(executionPlan);
                logger.info("Execution planning complete: status={}, tasks={}",
                        executionPlan != null ? executionPlan.getPlanStatus() : "N/A",
                        executionPlan != null && executionPlan.getOrderedImplementationTasks() != null
                                ? executionPlan.getOrderedImplementationTasks().size() : 0);
            } catch (Exception e) {
                logger.warn("Execution planning failed: {}", e.getMessage());
                response.addWarning("Execution planning failed: " + e.getMessage());
            }

            // ============================================================
            // Stage 5: Workflow Validation
            // ============================================================
            stagesProgress.add("WORKFLOW_VALIDATION");
            logger.info("Stage 5/9: Workflow Validation");
            try {
                var validationReport = workflowValidationService.validateWorkflow(
                        request, workflowType, request,
                        new ArrayList<PlanStep>(),
                        new ArrayList<PlanDependency>(),
                        repositoryName, effectiveBranch);
                response.setValidationReport(validationReport);
                logger.info("Validation complete: status={}, score={}",
                        validationReport != null ? validationReport.getOverallStatus() : "N/A",
                        validationReport != null ? validationReport.getReadinessScore() : 0);
            } catch (Exception e) {
                logger.warn("Workflow validation failed: {}", e.getMessage());
                response.addWarning("Workflow validation failed: " + e.getMessage());
            }

            // ============================================================
            // Stage 6: Recommendation Generation
            // ============================================================
            stagesProgress.add("RECOMMENDATION_GENERATION");
            logger.info("Stage 6/9: Recommendation Generation");
            try {
                var recommendationReport = recommendationEngineService.generateRecommendations(
                        request, workflowType, request, repositoryName, effectiveBranch);
                response.setRecommendationReport(recommendationReport);
                logger.info("Recommendations complete: {} recommendations",
                        recommendationReport != null && recommendationReport.getPrioritizedRecommendations() != null
                                ? recommendationReport.getPrioritizedRecommendations().size() : 0);
            } catch (Exception e) {
                logger.warn("Recommendation generation failed: {}", e.getMessage());
                response.addWarning("Recommendation generation failed: " + e.getMessage());
            }

            // ============================================================
            // Stage 7: Readiness Assessment
            // ============================================================
            stagesProgress.add("READINESS_ASSESSMENT");
            logger.info("Stage 7/9: Readiness Assessment");
            try {
                var readinessReport = executionReadinessService.assessReadiness(
                        request, workflowType, request, repositoryName, effectiveBranch);
                response.setReadinessReport(readinessReport);
                logger.info("Readiness assessment complete: level={}, score={}",
                        readinessReport != null && readinessReport.getOverallReadinessLevel() != null
                                ? readinessReport.getOverallReadinessLevel().name() : "N/A",
                        readinessReport != null ? readinessReport.getReadinessScore() : 0);
            } catch (Exception e) {
                logger.warn("Readiness assessment failed: {}", e.getMessage());
                response.addWarning("Readiness assessment failed: " + e.getMessage());
            }

            // ============================================================
            // Stage 8: Development Session
            // ============================================================
            stagesProgress.add("DEVELOPMENT_SESSION");
            logger.info("Stage 8/9: Development Session Management");
            try {
                var session = developmentSessionService.createSession(
                        repositoryName, request,
                        com.projectiq.mcp.orchestration.dto.WorkflowType.UNKNOWN,
                        List.of("TASK_ANALYSIS", "WORKFLOW_EXECUTION", "CONTEXT_ASSEMBLY",
                                "EXECUTION_PLANNING", "VALIDATION", "RECOMMENDATIONS",
                                "READINESS_ASSESSMENT", "AGENT_HANDOFF"));
                response.setDevelopmentSession(session);
                logger.info("Development session created: {}", session.getSessionId());
            } catch (Exception e) {
                logger.warn("Development session creation failed: {}", e.getMessage());
                response.addWarning("Development session creation failed: " + e.getMessage());
            }

            // ============================================================
            // Stage 9: Agent Handoff
            // ============================================================
            stagesProgress.add("AGENT_HANDOFF");
            logger.info("Stage 9/9: Agent Handoff Export");
            try {
                if (response.getDevelopmentSession() != null) {
                    String sessionId = response.getDevelopmentSession().getSessionId();
                    var handoffPackage = agentHandoffService.exportHandoffPackage(sessionId);
                    response.setHandoffPackage(handoffPackage);
                    logger.info("Agent handoff package exported for session: {}", sessionId);
                } else {
                    response.addWarning("Cannot export handoff: no development session available");
                }
            } catch (Exception e) {
                logger.warn("Agent handoff export failed: {}", e.getMessage());
                response.addWarning("Agent handoff export failed: " + e.getMessage());
            }

            // ============================================================
            // Determine Overall Status
            // ============================================================
            long totalDuration = System.currentTimeMillis() - startTime;
            response.setTotalDurationMillis(totalDuration);

            if (response.hasErrors()) {
                response.setOverallStatus("COMPLETED_WITH_ERRORS");
            } else if (response.hasWarnings()) {
                response.setOverallStatus("COMPLETED_WITH_WARNINGS");
            } else {
                response.setOverallStatus("COMPLETED");
            }

            logger.info("=== End-to-End Workflow Complete ===");
            logger.info("Status: {}, Duration: {}ms, Warnings: {}, Errors: {}",
                    response.getOverallStatus(), totalDuration,
                    response.getWarnings().size(), response.getErrors().size());

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for end-to-end workflow: {}", e.getMessage());
            response.addError("Invalid argument: " + e.getMessage());
            response.setOverallStatus("FAILED");
        } catch (Exception e) {
            logger.error("Unexpected error during end-to-end workflow: {}", e.getMessage(), e);
            response.addError("Unexpected error: " + e.getMessage());
            response.setOverallStatus("FAILED");
        }

        return response;
    }
}