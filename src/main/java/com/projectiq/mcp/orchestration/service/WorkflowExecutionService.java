package com.projectiq.mcp.orchestration.service;

import com.projectiq.mcp.orchestration.dto.DependencyValidationResult;
import com.projectiq.mcp.orchestration.dto.ExecutionTimelineEntry;
import com.projectiq.mcp.orchestration.dto.ProgressTracker;
import com.projectiq.mcp.orchestration.dto.StepDependency;
import com.projectiq.mcp.orchestration.dto.WorkflowDefinition;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse.FinalReport;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse.ProgressSummary;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse.StepResultInfo;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionState;
import com.projectiq.mcp.orchestration.dto.WorkflowStep;
import com.projectiq.mcp.orchestration.dto.WorkflowStep.StepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service that executes multi-step developer workflows in a deterministic manner.
 * Manages workflow execution state, step dependencies, progress tracking, and recovery.
 *
 * <p>This service:
 * <ul>
 *   <li>Executes workflow steps sequentially</li>
 *   <li>Tracks execution state (Pending, Running, Completed, Skipped, Failed, Cancelled)</li>
 *   <li>Respects step dependencies (prerequisite steps execute first)</li>
 *   <li>Skips already completed steps</li>
 *   <li>Continues after recoverable failures</li>
 *   <li>Detects circular dependencies</li>
 *   <li>Produces deterministic execution results</li>
 * </ul>
 */
@Service
public class WorkflowExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecutionService.class);

    /**
     * Executes a workflow definition by running all steps sequentially,
     * respecting dependencies, tracking progress, and handling failures.
     *
     * @param definition the workflow definition to execute
     * @param dependencies optional list of step dependencies
     * @return a complete WorkflowExecutionResponse with execution results
     */
    public WorkflowExecutionResponse execute(WorkflowDefinition definition, List<StepDependency> dependencies) {
        if (definition == null) {
            throw new IllegalArgumentException("Workflow definition must not be null");
        }

        List<WorkflowStep> steps = definition.getSteps();
        if (steps == null || steps.isEmpty()) {
            return createEmptyWorkflowResponse(definition);
        }

        // Validate dependencies
        DependencyValidationResult validationResult = validateDependencies(steps, dependencies);
        if (!validationResult.isValid()) {
            String errors = String.join("; ", validationResult.getErrors());
            throw new IllegalArgumentException("Workflow dependency validation failed: " + errors);
        }

        String workflowId = generateWorkflowId();
        long startTime = System.currentTimeMillis();

        WorkflowExecutionResponse response = new WorkflowExecutionResponse();
        response.setWorkflowId(workflowId);
        response.setWorkflowStatus(WorkflowExecutionState.RUNNING.getDisplayName());

        // Build dependency map for quick lookup
        Map<String, List<String>> dependencyMap = buildDependencyMap(dependencies);

        // Track completed step names for dependency resolution
        Set<String> completedStepNames = new HashSet<>();
        List<ExecutionTimelineEntry> timeline = new ArrayList<>();

        // Execute steps in order
        for (WorkflowStep step : steps) {
            long stepStartTime = System.currentTimeMillis();

            // Check if step is already completed (by name)
            if (completedStepNames.contains(step.getName())) {
                step.setStatus(StepStatus.SKIPPED);
                step.setResult("Already completed");
                long stepDuration = System.currentTimeMillis() - stepStartTime;
                step.setDurationMillis(stepDuration);

                response.addSkippedStep(createStepResultInfo(step));
                timeline.add(new ExecutionTimelineEntry(
                        step.getOrder(), step.getName(), step.getDescription(),
                        StepStatus.SKIPPED.name(), stepStartTime, stepDuration, "Already completed"));
                continue;
            }

            // Check dependencies
            List<String> stepDeps = dependencyMap.getOrDefault(step.getName(), List.of());
            boolean allDepsCompleted = stepDeps.stream().allMatch(completedStepNames::contains);

            if (!allDepsCompleted) {
                // Find missing dependencies
                List<String> missingDeps = stepDeps.stream()
                        .filter(dep -> !completedStepNames.contains(dep))
                        .collect(Collectors.toList());

                step.setStatus(StepStatus.SKIPPED);
                step.setError("Missing dependencies: " + String.join(", ", missingDeps));
                long stepDuration = System.currentTimeMillis() - stepStartTime;
                step.setDurationMillis(stepDuration);

                response.addSkippedStep(createStepResultInfo(step));
                timeline.add(new ExecutionTimelineEntry(
                        step.getOrder(), step.getName(), step.getDescription(),
                        StepStatus.SKIPPED.name(), stepStartTime, stepDuration,
                        "Skipped - missing dependencies: " + missingDeps));
                continue;
            }

            // Execute the step
            step.setStatus(StepStatus.RUNNING);
            try {
                // Simulate step execution - in a real scenario this would invoke
                // the appropriate service based on step type/name
                String result = executeStep(step);
                step.setStatus(StepStatus.COMPLETED);
                step.setResult(result);
                completedStepNames.add(step.getName());

                long stepDuration = System.currentTimeMillis() - stepStartTime;
                step.setDurationMillis(stepDuration);

                response.addExecutedStep(createStepResultInfo(step));
                timeline.add(new ExecutionTimelineEntry(
                        step.getOrder(), step.getName(), step.getDescription(),
                        StepStatus.COMPLETED.name(), stepStartTime, stepDuration, result));

            } catch (Exception e) {
                logger.warn("Step '{}' failed: {}", step.getName(), e.getMessage());
                step.setStatus(StepStatus.FAILED);
                step.setError(e.getMessage());
                long stepDuration = System.currentTimeMillis() - stepStartTime;
                step.setDurationMillis(stepDuration);

                response.addFailedStep(createStepResultInfo(step));
                timeline.add(new ExecutionTimelineEntry(
                        step.getOrder(), step.getName(), step.getDescription(),
                        StepStatus.FAILED.name(), stepStartTime, stepDuration,
                        "Failed: " + e.getMessage()));

                // Continue execution after recoverable failures
                logger.info("Continuing execution after recoverable failure in step '{}'", step.getName());
            }
        }

        // Build progress summary
        long totalDuration = System.currentTimeMillis() - startTime;
        ProgressSummary progressSummary = buildProgressSummary(steps, totalDuration);
        response.setProgressSummary(progressSummary);

        // Set execution timeline
        response.setExecutionTimeline(timeline);

        // Determine final status
        String finalStatus = determineFinalStatus(steps);
        response.setWorkflowStatus(finalStatus);

        // Build final report
        FinalReport finalReport = buildFinalReport(workflowId, definition, steps, finalStatus, totalDuration);
        response.setFinalReport(finalReport);

        logger.info("Workflow '{}' execution completed with status: {}", workflowId, finalStatus);
        return response;
    }

    /**
     * Executes a workflow definition without explicit dependencies.
     * Steps are executed in the order they appear in the definition.
     *
     * @param definition the workflow definition to execute
     * @return a complete WorkflowExecutionResponse with execution results
     */
    public WorkflowExecutionResponse execute(WorkflowDefinition definition) {
        return execute(definition, List.of());
    }

    /**
     * Validates workflow step dependencies.
     * Checks for circular dependencies and missing step references.
     *
     * @param steps the workflow steps
     * @param dependencies the dependency definitions
     * @return validation result indicating if dependencies are valid
     */
    public DependencyValidationResult validateDependencies(List<WorkflowStep> steps, List<StepDependency> dependencies) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (dependencies == null || dependencies.isEmpty()) {
            return DependencyValidationResult.valid();
        }

        // Build set of step names for quick lookup
        Set<String> stepNames = steps.stream()
                .map(WorkflowStep::getName)
                .collect(Collectors.toSet());

        // Check for missing step references in dependencies
        for (StepDependency dep : dependencies) {
            if (!stepNames.contains(dep.getStepName())) {
                errors.add("Dependency references unknown step: '" + dep.getStepName() + "'");
            }
            for (String dependsOn : dep.getDependsOn()) {
                if (!stepNames.contains(dependsOn)) {
                    errors.add("Step '" + dep.getStepName() + "' depends on unknown step: '" + dependsOn + "'");
                }
            }
        }

        // Check for circular dependencies using DFS
        Map<String, List<String>> depMap = buildDependencyMap(dependencies);
        Set<String> circularDeps = detectCircularDependencies(depMap);
        for (String circular : circularDeps) {
            errors.add("Circular dependency detected involving step: '" + circular + "'");
        }

        if (!errors.isEmpty()) {
            return DependencyValidationResult.invalid(errors);
        }

        return DependencyValidationResult.valid();
    }

    /**
     * Detects circular dependencies in the dependency graph using DFS.
     *
     * @param dependencyMap map of step name to its dependencies
     * @return set of step names involved in circular dependencies
     */
    public Set<String> detectCircularDependencies(Map<String, List<String>> dependencyMap) {
        Set<String> circular = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : dependencyMap.keySet()) {
            if (!visited.contains(node)) {
                dfsDetectCycle(node, dependencyMap, visited, recursionStack, circular);
            }
        }

        return circular;
    }

    private void dfsDetectCycle(String node, Map<String, List<String>> graph,
                                 Set<String> visited, Set<String> recursionStack,
                                 Set<String> circular) {
        visited.add(node);
        recursionStack.add(node);

        List<String> neighbors = graph.getOrDefault(node, List.of());
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                dfsDetectCycle(neighbor, graph, visited, recursionStack, circular);
            } else if (recursionStack.contains(neighbor)) {
                circular.add(node);
                circular.add(neighbor);
            }
        }

        recursionStack.remove(node);
    }

    /**
     * Executes a single workflow step.
     * This is a deterministic step executor that simulates step execution.
     * In a production system, this would delegate to specific service implementations.
     *
     * @param step the workflow step to execute
     * @return the result of step execution
     * @throws Exception if step execution fails
     */
    protected String executeStep(WorkflowStep step) throws Exception {
        logger.debug("Executing step: {} ({})", step.getName(), step.getDescription());

        // Simulate step execution based on step name patterns
        // This is a deterministic implementation - no LLM or AI involved
        String stepName = step.getName().toLowerCase();

        if (stepName.contains("analyze") || stepName.contains("analysis")) {
            return "Analysis completed for step: " + step.getName();
        } else if (stepName.contains("validate") || stepName.contains("validation")) {
            return "Validation passed for step: " + step.getName();
        } else if (stepName.contains("context") || stepName.contains("assembly")) {
            return "Context assembled for step: " + step.getName();
        } else if (stepName.contains("impact")) {
            return "Impact analysis completed for step: " + step.getName();
        } else if (stepName.contains("plan") || stepName.contains("implementation")) {
            return "Implementation plan generated for step: " + step.getName();
        } else if (stepName.contains("test")) {
            return "Test analysis completed for step: " + step.getName();
        } else if (stepName.contains("refactor")) {
            return "Refactoring analysis completed for step: " + step.getName();
        } else if (stepName.contains("architecture")) {
            return "Architecture insights gathered for step: " + step.getName();
        } else if (stepName.contains("convention")) {
            return "Repository conventions analyzed for step: " + step.getName();
        } else if (stepName.contains("health")) {
            return "Repository health check completed for step: " + step.getName();
        } else if (stepName.contains("summary") || stepName.contains("statistics")) {
            return "Repository summary generated for step: " + step.getName();
        } else if (stepName.contains("search") || stepName.contains("find")) {
            return "Search completed for step: " + step.getName();
        } else if (stepName.contains("build") || stepName.contains("context")) {
            return "Context built for step: " + step.getName();
        } else {
            return "Step executed successfully: " + step.getName();
        }
    }

    private WorkflowExecutionResponse createEmptyWorkflowResponse(WorkflowDefinition definition) {
        String workflowId = generateWorkflowId();
        WorkflowExecutionResponse response = new WorkflowExecutionResponse();
        response.setWorkflowId(workflowId);
        response.setWorkflowStatus(WorkflowExecutionState.COMPLETED.getDisplayName());

        ProgressSummary progressSummary = new ProgressSummary();
        progressSummary.setTotalSteps(0);
        progressSummary.setCompletedSteps(0);
        progressSummary.setSkippedSteps(0);
        progressSummary.setFailedSteps(0);
        progressSummary.setRemainingSteps(0);
        progressSummary.setSuccessRate(100.0);
        progressSummary.setTotalDurationMillis(0);
        response.setProgressSummary(progressSummary);

        FinalReport finalReport = new FinalReport();
        finalReport.setWorkflowId(workflowId);
        finalReport.setWorkflowType(definition.getWorkflowType() != null
                ? definition.getWorkflowType().getDisplayName() : "Unknown");
        finalReport.setOriginalRequest(definition.getOriginalRequest());
        finalReport.setFinalStatus(WorkflowExecutionState.COMPLETED.getDisplayName());
        finalReport.setTotalSteps(0);
        finalReport.setCompletedCount(0);
        finalReport.setSkippedCount(0);
        finalReport.setFailedCount(0);
        finalReport.setTotalDurationMillis(0);
        finalReport.setSummary("Workflow has no steps to execute");
        response.setFinalReport(finalReport);

        return response;
    }

    private ProgressSummary buildProgressSummary(List<WorkflowStep> steps, long totalDuration) {
        int total = steps.size();
        int completed = 0;
        int skipped = 0;
        int failed = 0;
        String currentStep = null;

        for (WorkflowStep step : steps) {
            switch (step.getStatus()) {
                case COMPLETED:
                    completed++;
                    break;
                case SKIPPED:
                    skipped++;
                    break;
                case FAILED:
                    failed++;
                    break;
                case RUNNING:
                    currentStep = step.getName();
                    break;
                default:
                    break;
            }
        }

        int remaining = total - completed - skipped - failed;
        double successRate = total > 0 ? ((double) (completed) / total) * 100.0 : 100.0;

        ProgressSummary summary = new ProgressSummary();
        summary.setTotalSteps(total);
        summary.setCompletedSteps(completed);
        summary.setSkippedSteps(skipped);
        summary.setFailedSteps(failed);
        summary.setRemainingSteps(remaining);
        summary.setCurrentStep(currentStep);
        summary.setSuccessRate(successRate);
        summary.setTotalDurationMillis(totalDuration);

        return summary;
    }

    private String determineFinalStatus(List<WorkflowStep> steps) {
        boolean hasFailed = false;
        boolean hasSkipped = false;
        boolean allCompleted = true;

        for (WorkflowStep step : steps) {
            switch (step.getStatus()) {
                case FAILED:
                    hasFailed = true;
                    allCompleted = false;
                    break;
                case SKIPPED:
                    hasSkipped = true;
                    break;
                case PENDING:
                case RUNNING:
                    allCompleted = false;
                    break;
                default:
                    break;
            }
        }

        if (allCompleted && !hasFailed) {
            return WorkflowExecutionState.COMPLETED.getDisplayName();
        } else if (hasFailed && hasSkipped) {
            return WorkflowExecutionState.FAILED.getDisplayName();
        } else if (hasFailed) {
            return WorkflowExecutionState.FAILED.getDisplayName();
        } else if (hasSkipped) {
            return WorkflowExecutionState.SKIPPED.getDisplayName();
        } else {
            return WorkflowExecutionState.COMPLETED.getDisplayName();
        }
    }

    private FinalReport buildFinalReport(String workflowId, WorkflowDefinition definition,
                                          List<WorkflowStep> steps, String finalStatus, long totalDuration) {
        int completedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (WorkflowStep step : steps) {
            switch (step.getStatus()) {
                case COMPLETED:
                    completedCount++;
                    break;
                case SKIPPED:
                    skippedCount++;
                    break;
                case FAILED:
                    failedCount++;
                    break;
                default:
                    break;
            }
        }

        StringBuilder summaryBuilder = new StringBuilder();
        summaryBuilder.append("Workflow execution completed. ");
        summaryBuilder.append(completedCount).append(" steps completed, ");
        summaryBuilder.append(skippedCount).append(" skipped, ");
        summaryBuilder.append(failedCount).append(" failed out of ");
        summaryBuilder.append(steps.size()).append(" total steps.");

        if (failedCount > 0) {
            summaryBuilder.append(" Some steps encountered errors but execution continued.");
        }

        FinalReport report = new FinalReport();
        report.setWorkflowId(workflowId);
        report.setWorkflowType(definition.getWorkflowType() != null
                ? definition.getWorkflowType().getDisplayName() : "Unknown");
        report.setOriginalRequest(definition.getOriginalRequest());
        report.setFinalStatus(finalStatus);
        report.setTotalSteps(steps.size());
        report.setCompletedCount(completedCount);
        report.setSkippedCount(skippedCount);
        report.setFailedCount(failedCount);
        report.setTotalDurationMillis(totalDuration);
        report.setSummary(summaryBuilder.toString());

        return report;
    }

    private StepResultInfo createStepResultInfo(WorkflowStep step) {
        StepResultInfo info = new StepResultInfo(
                step.getOrder(), step.getName(), step.getDescription(),
                step.getStatus().name());
        info.setResult(step.getResult());
        info.setError(step.getError());
        info.setDurationMillis(step.getDurationMillis());
        return info;
    }

    private Map<String, List<String>> buildDependencyMap(List<StepDependency> dependencies) {
        Map<String, List<String>> map = new HashMap<>();
        if (dependencies != null) {
            for (StepDependency dep : dependencies) {
                map.put(dep.getStepName(), new ArrayList<>(dep.getDependsOn()));
            }
        }
        return map;
    }

    private String generateWorkflowId() {
        return "wf-" + UUID.randomUUID().toString().substring(0, 8);
    }
}