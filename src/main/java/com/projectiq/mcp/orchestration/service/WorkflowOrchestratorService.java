package com.projectiq.mcp.orchestration.service;

import com.projectiq.mcp.analysis.dto.ContextAssemblyResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.ImpactedComponent;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse.RiskItem;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.dto.RefactoringAssistantResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
import com.projectiq.mcp.analysis.dto.TaskType;
import com.projectiq.mcp.analysis.dto.TestImpactAnalysisResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import com.projectiq.mcp.analysis.service.ContextAssemblyService;
import com.projectiq.mcp.analysis.service.ImpactAnalysisService;
import com.projectiq.mcp.analysis.service.ImplementationPlanningService;
import com.projectiq.mcp.analysis.service.RefactoringAssistantService;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import com.projectiq.mcp.analysis.service.TaskAnalysisService;
import com.projectiq.mcp.analysis.service.TestImpactAnalysisService;
import com.projectiq.mcp.orchestration.dto.WorkflowDefinition;
import com.projectiq.mcp.orchestration.dto.WorkflowExecution;
import com.projectiq.mcp.orchestration.dto.WorkflowExecution.ExecutionStatus;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.orchestration.dto.WorkflowResult.StepResult;
import com.projectiq.mcp.orchestration.dto.WorkflowStep;
import com.projectiq.mcp.orchestration.dto.WorkflowStep.StepStatus;
import com.projectiq.mcp.orchestration.dto.WorkflowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service that orchestrates intelligent workflows by coordinating existing
 * repository intelligence services into a single deterministic execution pipeline.
 *
 * <p>This service accepts a developer request, invokes Task Analysis, builds an
 * execution workflow, coordinates existing services, aggregates intermediate
 * results, and produces a deterministic workflow result.</p>
 *
 * <p>Workflows are executed sequentially with stable execution order.
 * No duplicate workflow steps are executed. Execution continues after
 * recoverable failures. The service never generates or modifies code.</p>
 */
@Service
public class WorkflowOrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowOrchestratorService.class);

    private final TaskAnalysisService taskAnalysisService;
    private final ContextAssemblyService contextAssemblyService;
    private final ImpactAnalysisService impactAnalysisService;
    private final ImplementationPlanningService implementationPlanningService;
    private final TestImpactAnalysisService testImpactAnalysisService;
    private final RefactoringAssistantService refactoringAssistantService;
    private final ArchitectureInsightsService architectureInsightsService;
    private final RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    private final RepositoryHealthService repositoryHealthService;

    // --- Deterministic workflow type mapping ---

    private static final Map<TaskType, WorkflowType> WORKFLOW_TYPE_MAP = buildWorkflowTypeMap();

    // --- Deterministic workflow step templates by WorkflowType ---

    private static final Map<WorkflowType, List<WorkflowTemplateStep>> WORKFLOW_STEP_TEMPLATES = buildWorkflowStepTemplates();

    public WorkflowOrchestratorService(
            TaskAnalysisService taskAnalysisService,
            ContextAssemblyService contextAssemblyService,
            ImpactAnalysisService impactAnalysisService,
            ImplementationPlanningService implementationPlanningService,
            TestImpactAnalysisService testImpactAnalysisService,
            RefactoringAssistantService refactoringAssistantService,
            ArchitectureInsightsService architectureInsightsService,
            RepositoryConventionAnalyzerService repositoryConventionAnalyzerService,
            RepositoryHealthService repositoryHealthService) {
        this.taskAnalysisService = taskAnalysisService;
        this.contextAssemblyService = contextAssemblyService;
        this.impactAnalysisService = impactAnalysisService;
        this.implementationPlanningService = implementationPlanningService;
        this.testImpactAnalysisService = testImpactAnalysisService;
        this.refactoringAssistantService = refactoringAssistantService;
        this.architectureInsightsService = architectureInsightsService;
        this.repositoryConventionAnalyzerService = repositoryConventionAnalyzerService;
        this.repositoryHealthService = repositoryHealthService;
    }

    /**
     * Orchestrates a complete workflow for the given developer request.
     * Accepts a natural language developer task, builds an execution workflow,
     * executes it by coordinating existing services, and returns a complete
     * workflow report.
     *
     * @param request        the natural language developer request
     * @param repositoryName the repository name
     * @param branch         the git branch (optional, defaults to "main")
     * @return a complete workflow report with execution results
     * @throws IllegalArgumentException if the request is null or empty
     */
    public WorkflowResult orchestrate(String request, String repositoryName, String branch) {
        long startTime = System.currentTimeMillis();
        logger.info("Orchestrating workflow for request: {} in repository: {}", request, repositoryName);

        if (request == null || request.trim().isEmpty()) {
            throw new IllegalArgumentException("Developer request cannot be null or empty");
        }

        String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

        // Step 1: Analyze the task
        TaskAnalysisResponse taskAnalysis = taskAnalysisService.analyze(request);
        WorkflowType workflowType = mapToWorkflowType(taskAnalysis.getTaskType());

        // Step 2: Build workflow definition
        WorkflowDefinition definition = buildWorkflowDefinition(request, workflowType, taskAnalysis);

        // Step 3: Create execution
        WorkflowExecution execution = new WorkflowExecution(definition);
        execution.setStatus(ExecutionStatus.RUNNING);

        WorkflowResult result = new WorkflowResult();
        result.setOriginalRequest(request);
        result.setWorkflowType(workflowType.getDisplayName());

        // Record execution plan
        for (WorkflowStep step : definition.getSteps()) {
            result.addExecutionPlanItem(step.getOrder() + ". " + step.getName() + ": " + step.getDescription());
        }

        // Step 4: Execute workflow steps sequentially
        Set<String> processedStepNames = new LinkedHashSet<>();
        List<String> repositoryInsights = new ArrayList<>();
        List<String> risksIdentified = new ArrayList<>();

        for (WorkflowStep step : definition.getSteps()) {
            String stepName = step.getName();

            // Skip duplicate steps
            if (processedStepNames.contains(stepName)) {
                step.setStatus(StepStatus.SKIPPED);
                execution.addStep(step);
                result.addSkippedStep(toStepResult(step));
                logger.debug("Skipping duplicate step: {}", stepName);
                continue;
            }

            long stepStartTime = System.currentTimeMillis();
            step.setStatus(StepStatus.RUNNING);
            logger.info("Executing workflow step: {} ({})", stepName, step.getDescription());

            try {
                String stepResult = executeWorkflowStep(
                        stepName, request, repositoryName, effectiveBranch,
                        taskAnalysis, repositoryInsights, risksIdentified);
                step.setResult(stepResult);
                step.setStatus(StepStatus.COMPLETED);
                step.setDurationMillis(System.currentTimeMillis() - stepStartTime);
                processedStepNames.add(stepName);
                execution.addStep(step);
                result.addCompletedStep(toStepResult(step));
                logger.info("Step '{}' completed in {}ms", stepName, step.getDurationMillis());
            } catch (Exception e) {
                logger.warn("Step '{}' failed: {}", stepName, e.getMessage());
                step.setError(e.getMessage());
                step.setStatus(StepStatus.FAILED);
                step.setDurationMillis(System.currentTimeMillis() - stepStartTime);
                execution.addStep(step);
                result.addFailedStep(toStepResult(step));
                // Continue after recoverable failures
            }
        }

        // Step 5: Populate repository insights and risks
        result.setRepositoryInsights(repositoryInsights);
        result.setRisksIdentified(risksIdentified);

        // Step 6: Determine suggested next actions
        result.setSuggestedNextActions(determineSuggestedNextActions(workflowType, execution));

        // Step 7: Finalize
        long totalDuration = System.currentTimeMillis() - startTime;
        execution.setEndTimeMillis(startTime + totalDuration);
        result.setTotalDurationMillis(totalDuration);

        // Determine execution status
        ExecutionStatus execStatus = determineExecutionStatus(execution);
        execution.setStatus(execStatus);
        result.setExecutionStatus(execStatus.name());

        // Generate summary
        result.setSummary(generateSummary(result, execStatus));

        logger.info("Workflow orchestration complete: type={}, status={}, time={}ms",
                workflowType, execStatus, totalDuration);

        return result;
    }

    /**
     * Builds a workflow definition from the task analysis and workflow type.
     */
    WorkflowDefinition buildWorkflowDefinition(String request, WorkflowType workflowType,
                                                TaskAnalysisResponse taskAnalysis) {
        List<WorkflowStep> steps = new ArrayList<>();
        int order = 1;

        List<WorkflowTemplateStep> templateSteps = WORKFLOW_STEP_TEMPLATES
                .getOrDefault(workflowType, WORKFLOW_STEP_TEMPLATES.get(WorkflowType.REPOSITORY_ANALYSIS));

        // Add task analysis as the first step
        steps.add(new WorkflowStep(order++, "analyze_task",
                "Analyze the developer request to determine task type and required intelligence"));

        // Add template-based steps
        for (WorkflowTemplateStep template : templateSteps) {
            steps.add(new WorkflowStep(order++, template.name(), template.description()));
        }

        // Build reasoning
        String reasoning = buildReasoning(workflowType, taskAnalysis, steps);

        return new WorkflowDefinition(workflowType, request, steps, reasoning);
    }

    /**
     * Executes a single workflow step by delegating to the appropriate service.
     */
    private String executeWorkflowStep(
            String stepName, String request, String repositoryName, String branch,
            TaskAnalysisResponse taskAnalysis,
            List<String> repositoryInsights, List<String> risksIdentified) throws Exception {

        return switch (stepName) {
            case "analyze_task" -> executeAnalyzeTask(request, taskAnalysis);
            case "analyze_impact" -> executeAnalyzeImpact(request, repositoryName, branch, risksIdentified);
            case "assemble_context" -> executeAssembleContext(request, repositoryName, branch);
            case "implementation_plan" -> executeImplementationPlan(request, repositoryName, branch);
            case "test_impact_analysis" -> executeTestImpactAnalysis(request, repositoryName, branch);
            case "architecture_analysis" -> executeArchitectureAnalysis(repositoryName, branch, repositoryInsights);
            case "convention_analysis" -> executeConventionAnalysis(repositoryName, branch, repositoryInsights);
            case "health_analysis" -> executeHealthAnalysis(repositoryName, branch, repositoryInsights);
            case "refactoring_analysis" -> executeRefactoringAnalysis(request, repositoryName, branch);
            default -> {
                logger.warn("Unknown workflow step: {}", stepName);
                yield "Step '" + stepName + "' is not recognized";
            }
        };
    }

    // --- Step execution methods ---

    private String executeAnalyzeTask(String request, TaskAnalysisResponse taskAnalysis) {
        logger.debug("Executing analyze_task step");
        return "Task type: " + taskAnalysis.getTaskType().getDisplayName()
                + ", Confidence: " + taskAnalysis.getConfidenceLevel().name()
                + ", Complexity: " + taskAnalysis.getEstimatedComplexity().name()
                + ", Entities detected: " + (taskAnalysis.getDetectedEntities() != null
                ? taskAnalysis.getDetectedEntities().size() : 0);
    }

    private String executeAnalyzeImpact(String request, String repositoryName, String branch,
                                         List<String> risksIdentified) {
        logger.debug("Executing analyze_impact step");
        ImpactAnalysisResponse impact = impactAnalysisService.analyzeImpact(request, repositoryName, branch);

        if (impact.getPotentialRisks() != null) {
            for (RiskItem risk : impact.getPotentialRisks()) {
                risksIdentified.add(risk.getDescription() + " [Level: " + risk.getRiskLevel().name()
                        + ", Mitigation: " + risk.getMitigation() + "]");
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Scope: ").append(impact.getEstimatedImplementationScope().name());
        sb.append(", Directly affected: ").append(
                impact.getDirectlyAffectedComponents() != null ? impact.getDirectlyAffectedComponents().size() : 0);
        sb.append(", Indirectly affected: ").append(
                impact.getIndirectlyAffectedComponents() != null ? impact.getIndirectlyAffectedComponents().size() : 0);
        sb.append(", Risks: ").append(risksIdentified.size());
        return sb.toString();
    }

    private String executeAssembleContext(String request, String repositoryName, String branch) {
        logger.debug("Executing assemble_context step");
        ContextAssemblyResponse context = contextAssemblyService.assembleContext(
                request, repositoryName, branch);
        return "Context assembled: " + (context.getExecutedTools() != null
                ? context.getExecutedTools().size() : 0) + " tools executed"
                + ", Time: " + context.getTotalExecutionTimeMillis() + "ms";
    }

    private String executeImplementationPlan(String request, String repositoryName, String branch) {
        logger.debug("Executing implementation_plan step");
        ImplementationPlanningResponse plan = implementationPlanningService.generatePlan(
                request, repositoryName, branch);
        return "Plan generated: " + (plan.getRecommendedImplementationOrder() != null
                ? plan.getRecommendedImplementationOrder().size() : 0) + " steps"
                + ", Complexity: " + plan.getEstimatedComplexity();
    }

    private String executeTestImpactAnalysis(String request, String repositoryName, String branch) {
        logger.debug("Executing test_impact_analysis step");
        TestImpactAnalysisResponse testImpact = testImpactAnalysisService.analyzeTestImpact(
                request, repositoryName, branch);
        return "Test impact: " + (testImpact.getRelatedTestClasses() != null
                ? testImpact.getRelatedTestClasses().size() : 0) + " related tests"
                + ", Effort: " + testImpact.getEstimatedTestingEffort()
                + ", Confidence: " + testImpact.getConfidenceLevel();
    }

    private String executeArchitectureAnalysis(String repositoryName, String branch,
                                                List<String> repositoryInsights) {
        logger.debug("Executing architecture_analysis step");
        var archResponse = architectureInsightsService.analyzeArchitecture(repositoryName, branch);
        repositoryInsights.add("Architecture style: " + archResponse.getArchitecturalStyle());
        repositoryInsights.add("Layers detected: " + String.join(", ", archResponse.getDetectedLayers()));
        return "Architecture: " + archResponse.getArchitecturalStyle()
                + ", Layers: " + (archResponse.getDetectedLayers() != null
                ? archResponse.getDetectedLayers().size() : 0);
    }

    private String executeConventionAnalysis(String repositoryName, String branch,
                                              List<String> repositoryInsights) {
        logger.debug("Executing convention_analysis step");
        var convResponse = repositoryConventionAnalyzerService.analyzeConventions(repositoryName, branch);
        if (convResponse.getNamingConventions() != null) {
            repositoryInsights.add("Class naming: " + convResponse.getNamingConventions().getClassNamingConvention());
        }
        if (convResponse.getProjectSpecificObservations() != null) {
            repositoryInsights.addAll(convResponse.getProjectSpecificObservations());
        }
        return "Conventions analyzed, Observations: " + (convResponse.getProjectSpecificObservations() != null
                ? convResponse.getProjectSpecificObservations().size() : 0);
    }

    private String executeHealthAnalysis(String repositoryName, String branch,
                                          List<String> repositoryInsights) {
        logger.debug("Executing health_analysis step");
        var healthResponse = repositoryHealthService.analyzeHealth(repositoryName, branch);
        repositoryInsights.add("Health score: " + healthResponse.getHealthScore() + "/100");
        repositoryInsights.add("Maintainability: " + healthResponse.getMaintainabilityRating());
        if (healthResponse.getPotentialRisks() != null) {
            repositoryInsights.addAll(healthResponse.getPotentialRisks());
        }
        return "Health score: " + healthResponse.getHealthScore()
                + ", Maintainability: " + healthResponse.getMaintainabilityRating();
    }

    private String executeRefactoringAnalysis(String request, String repositoryName, String branch) {
        logger.debug("Executing refactoring_analysis step");
        RefactoringAssistantResponse refactorResponse = refactoringAssistantService.analyzeRefactoring(
                request, repositoryName, branch);
        return "Refactoring type: " + refactorResponse.getRefactoringType()
                + ", Affected classes: " + (refactorResponse.getAffectedClasses() != null
                ? refactorResponse.getAffectedClasses().size() : 0);
    }

    // --- Helper methods ---

    /**
     * Maps a TaskType to the corresponding WorkflowType.
     */
    private WorkflowType mapToWorkflowType(TaskType taskType) {
        return WORKFLOW_TYPE_MAP.getOrDefault(taskType, WorkflowType.UNKNOWN);
    }

    /**
     * Determines the overall execution status based on step results.
     */
    private ExecutionStatus determineExecutionStatus(WorkflowExecution execution) {
        List<WorkflowStep> steps = execution.getSteps();
        if (steps.isEmpty()) {
            return ExecutionStatus.FAILED;
        }

        boolean hasFailed = steps.stream().anyMatch(s -> s.getStatus() == StepStatus.FAILED);
        boolean hasSkipped = steps.stream().anyMatch(s -> s.getStatus() == StepStatus.SKIPPED);
        boolean allCompleted = steps.stream().allMatch(s ->
                s.getStatus() == StepStatus.COMPLETED || s.getStatus() == StepStatus.SKIPPED);

        if (allCompleted && !hasFailed) {
            if (hasSkipped) {
                return ExecutionStatus.COMPLETED_WITH_SKIPPED;
            }
            return ExecutionStatus.COMPLETED;
        }

        if (hasFailed && execution.getCompletedSteps().size() > 0) {
            return ExecutionStatus.COMPLETED_WITH_FAILURES;
        }

        return ExecutionStatus.FAILED;
    }

    /**
     * Determines suggested next actions based on workflow type and execution.
     */
    private List<String> determineSuggestedNextActions(WorkflowType workflowType,
                                                        WorkflowExecution execution) {
        List<String> actions = new ArrayList<>();
        boolean hasFailures = !execution.getFailedSteps().isEmpty();

        if (hasFailures) {
            actions.add("Review failed steps and resolve issues before proceeding");
        }

        switch (workflowType) {
            case FEATURE_IMPLEMENTATION:
                actions.add("Begin implementing the feature following the generated plan");
                actions.add("Create or modify source files as specified in the implementation plan");
                actions.add("Add tests for the new feature implementation");
                break;
            case BUG_FIX:
                actions.add("Examine the affected code paths identified in the impact analysis");
                actions.add("Apply the fix to the identified components");
                actions.add("Add regression tests to prevent future occurrences");
                break;
            case REFACTORING:
                actions.add("Follow the refactoring execution order to restructure the code");
                actions.add("Run the validation checklist after each refactoring step");
                actions.add("Verify no behavioral changes were introduced");
                break;
            case REST_API_ENHANCEMENT:
                actions.add("Update the controller and service layer as identified");
                actions.add("Update API documentation and specs");
                actions.add("Verify backward compatibility with existing clients");
                break;
            case CONFIGURATION_CHANGE:
                actions.add("Apply configuration changes across all environments");
                actions.add("Validate configuration changes do not break dependent components");
                break;
            case DOCUMENTATION_UPDATE:
                actions.add("Update documentation for the identified components");
                actions.add("Review documentation accuracy against actual implementation");
                break;
            case TEST_IMPROVEMENT:
                actions.add("Create or update test classes for the identified production classes");
                actions.add("Run the test suite to verify new tests pass");
                break;
            case REPOSITORY_ANALYSIS:
                actions.add("Review repository health findings and address identified concerns");
                actions.add("Address any architectural or convention inconsistencies");
                break;
            default:
                actions.add("Review the analysis results and determine next steps");
                break;
        }

        actions.add("Run the full test suite to verify no regressions");
        return actions;
    }

    /**
     * Generates a human-readable summary of the workflow execution.
     */
    private String generateSummary(WorkflowResult result, ExecutionStatus status) {
        StringBuilder sb = new StringBuilder();
        sb.append("Workflow '").append(result.getWorkflowType()).append("' ");
        sb.append(status.name().toLowerCase().replace("_", " "));
        sb.append(" in ").append(result.getTotalDurationMillis()).append("ms. ");
        sb.append(result.getCompletedSteps().size()).append(" steps completed");
        if (!result.getSkippedSteps().isEmpty()) {
            sb.append(", ").append(result.getSkippedSteps().size()).append(" skipped");
        }
        if (!result.getFailedSteps().isEmpty()) {
            sb.append(", ").append(result.getFailedSteps().size()).append(" failed");
        }
        sb.append(". ");
        sb.append(result.getRepositoryInsights().size()).append(" repository insights collected");
        sb.append(", ").append(result.getRisksIdentified().size()).append(" risks identified.");
        return sb.toString();
    }

    /**
     * Converts a WorkflowStep to a StepResult DTO.
     */
    private StepResult toStepResult(WorkflowStep step) {
        StepResult sr = new StepResult(
                step.getOrder(),
                step.getName(),
                step.getDescription(),
                step.getStatus().name()
        );
        sr.setResult(step.getResult());
        sr.setError(step.getError());
        sr.setDurationMillis(step.getDurationMillis());
        return sr;
    }

    /**
     * Builds reasoning text for the workflow definition.
     */
    private String buildReasoning(WorkflowType workflowType, TaskAnalysisResponse taskAnalysis,
                                   List<WorkflowStep> steps) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request classified as '").append(workflowType.getDisplayName()).append("' workflow");
        if (taskAnalysis.getDetectedEntities() != null && !taskAnalysis.getDetectedEntities().isEmpty()) {
            sb.append(" with ").append(taskAnalysis.getDetectedEntities().size())
                    .append(" detected entities");
        }
        sb.append(". ");
        sb.append("Workflow consists of ").append(steps.size())
                .append(" sequential steps: ");
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(steps.get(i).getName());
        }
        sb.append(". ");
        sb.append("Execution order is deterministic: start with task analysis, ");
        sb.append("then coordinate intelligence services based on workflow type, ");
        sb.append("and finally consolidate all results into a workflow report.");
        return sb.toString();
    }

    // --- Static initializers ---

    private static Map<TaskType, WorkflowType> buildWorkflowTypeMap() {
        Map<TaskType, WorkflowType> map = new LinkedHashMap<>();
        map.put(TaskType.NEW_FEATURE, WorkflowType.FEATURE_IMPLEMENTATION);
        map.put(TaskType.BUG_FIX, WorkflowType.BUG_FIX);
        map.put(TaskType.REFACTORING, WorkflowType.REFACTORING);
        map.put(TaskType.REST_API_CHANGE, WorkflowType.REST_API_ENHANCEMENT);
        map.put(TaskType.CONFIGURATION_CHANGE, WorkflowType.CONFIGURATION_CHANGE);
        map.put(TaskType.DOCUMENTATION, WorkflowType.DOCUMENTATION_UPDATE);
        map.put(TaskType.UNIT_TEST, WorkflowType.TEST_IMPROVEMENT);
        map.put(TaskType.DATABASE_CHANGE, WorkflowType.FEATURE_IMPLEMENTATION);
        map.put(TaskType.PERFORMANCE_IMPROVEMENT, WorkflowType.REFACTORING);
        map.put(TaskType.UNKNOWN, WorkflowType.REPOSITORY_ANALYSIS);
        return map;
    }

    private static Map<WorkflowType, List<WorkflowTemplateStep>> buildWorkflowStepTemplates() {
        Map<WorkflowType, List<WorkflowTemplateStep>> map = new LinkedHashMap<>();

        // Feature Implementation workflow
        map.put(WorkflowType.FEATURE_IMPLEMENTATION, List.of(
                new WorkflowTemplateStep("assemble_context", "Assemble repository context for the feature implementation"),
                new WorkflowTemplateStep("analyze_impact", "Analyze the impact of the feature on existing components"),
                new WorkflowTemplateStep("implementation_plan", "Generate an implementation plan with recommended order"),
                new WorkflowTemplateStep("test_impact_analysis", "Identify tests affected by the feature implementation"),
                new WorkflowTemplateStep("architecture_analysis", "Analyze repository architecture for integration points"),
                new WorkflowTemplateStep("convention_analysis", "Analyze repository conventions for consistent implementation")
        ));

        // Bug Fix workflow
        map.put(WorkflowType.BUG_FIX, List.of(
                new WorkflowTemplateStep("assemble_context", "Assemble repository context for the bug fix"),
                new WorkflowTemplateStep("analyze_impact", "Analyze the impact of the bug fix on affected components"),
                new WorkflowTemplateStep("implementation_plan", "Generate an implementation plan for the fix"),
                new WorkflowTemplateStep("test_impact_analysis", "Identify regression tests needed for the fix")
        ));

        // Refactoring workflow
        map.put(WorkflowType.REFACTORING, List.of(
                new WorkflowTemplateStep("assemble_context", "Assemble repository context for refactoring"),
                new WorkflowTemplateStep("analyze_impact", "Analyze the impact of refactoring on dependent components"),
                new WorkflowTemplateStep("refactoring_analysis", "Generate detailed refactoring execution plan"),
                new WorkflowTemplateStep("implementation_plan", "Generate an overall implementation plan"),
                new WorkflowTemplateStep("test_impact_analysis", "Identify tests impacted by the refactoring")
        ));

        // REST API Enhancement workflow
        map.put(WorkflowType.REST_API_ENHANCEMENT, List.of(
                new WorkflowTemplateStep("assemble_context", "Assemble repository context for the API change"),
                new WorkflowTemplateStep("analyze_impact", "Analyze the impact of the API change on consumers"),
                new WorkflowTemplateStep("implementation_plan", "Generate an implementation plan for the API change"),
                new WorkflowTemplateStep("test_impact_analysis", "Identify API tests affected by the change"),
                new WorkflowTemplateStep("convention_analysis", "Analyze repository API conventions")
        ));

        // Configuration Change workflow
        map.put(WorkflowType.CONFIGURATION_CHANGE, List.of(
                new WorkflowTemplateStep("assemble_context", "Assemble repository context for the configuration change"),
                new WorkflowTemplateStep("analyze_impact", "Analyze the impact of configuration changes"),
                new WorkflowTemplateStep("implementation_plan", "Generate an implementation plan for the config change"),
                new WorkflowTemplateStep("test_impact_analysis", "Identify tests for configuration validation")
        ));

        // Documentation Update workflow
        map.put(WorkflowType.DOCUMENTATION_UPDATE, List.of(
                new WorkflowTemplateStep("assemble_context", "Assemble repository context for documentation updates"),
                new WorkflowTemplateStep("implementation_plan", "Generate a plan for documentation updates"),
                new WorkflowTemplateStep("convention_analysis", "Analyze repository documentation conventions")
        ));

        // Test Improvement workflow
        map.put(WorkflowType.TEST_IMPROVEMENT, List.of(
                new WorkflowTemplateStep("assemble_context", "Assemble repository context for test improvements"),
                new WorkflowTemplateStep("test_impact_analysis", "Analyze test coverage and identify gaps"),
                new WorkflowTemplateStep("implementation_plan", "Generate a plan for test improvements"),
                new WorkflowTemplateStep("convention_analysis", "Analyze repository testing conventions")
        ));

        // Repository Analysis workflow (default when type is unknown)
        map.put(WorkflowType.REPOSITORY_ANALYSIS, List.of(
                new WorkflowTemplateStep("assemble_context", "Assemble full repository context"),
                new WorkflowTemplateStep("architecture_analysis", "Analyze repository architecture and module relationships"),
                new WorkflowTemplateStep("convention_analysis", "Analyze repository coding and naming conventions"),
                new WorkflowTemplateStep("health_analysis", "Analyze repository health and maintainability"),
                new WorkflowTemplateStep("analyze_impact", "Analyze impact based on available context")
        ));

        // Unknown workflow
        map.put(WorkflowType.UNKNOWN, map.get(WorkflowType.REPOSITORY_ANALYSIS));

        return map;
    }

    /**
     * A template for a workflow step with name and description.
     */
    private record WorkflowTemplateStep(String name, String description) {
    }
}