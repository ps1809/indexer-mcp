package com.projectiq.mcp.planning.service;

import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanDependency;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.DependencyValidationInfo;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.EffortEstimate;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.ExecutionPhase;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.ImplementationTask;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.PlanningSummary;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.RiskAssessment;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.TestingPoint;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse.ValidationCheckpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service that generates deterministic execution roadmaps from workflow analysis.
 * Converts workflow definitions into optimized execution strategies by validating
 * dependencies, determining optimal execution order, detecting blockers,
 * estimating complexity, assessing risks, and producing a structured roadmap.
 *
 * <p>This service is purely deterministic with no AI/LLM involvement.
 * All analyses are rule-based and produce stable, repeatable results.</p>
 */
@Service
public class ExecutionPlanningService {

    private static final Logger logger = LoggerFactory.getLogger(ExecutionPlanningService.class);

    /**
     * Generates a complete deterministic execution roadmap from the provided
     * workflow request information.
     *
     * @param request the execution plan request containing workflow steps and dependencies
     * @return a structured ExecutionPlanResponse with phases, tasks, risks, and summary
     */
    public ExecutionPlanResponse generateExecutionPlan(ExecutionPlanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Execution plan request must not be null");
        }

        logger.info("Generating execution plan for workflow: {} (type: {})",
                request.getWorkflowName(), request.getWorkflowType());

        ExecutionPlanResponse response = new ExecutionPlanResponse();
        response.setWorkflowName(request.getWorkflowName());
        response.setWorkflowType(request.getWorkflowType());
        response.setOriginalRequest(request.getOriginalRequest());

        List<PlanStep> steps = request.getSteps();
        List<PlanDependency> dependencies = request.getDependencies();

        // Validate inputs
        List<String> validationErrors = validateRequest(request);
        if (!validationErrors.isEmpty()) {
            response.setPlanStatus("INVALID");
            response.setErrors(validationErrors);
            response.setPlanningSummary(buildErrorSummary(validationErrors));
            logger.warn("Execution plan request validation failed: {}", validationErrors);
            return response;
        }

        // 1. Validate dependencies
        DependencyValidationInfo depValidation = validateDependencies(steps, dependencies);
        response.setDependencyValidation(depValidation);

        if (!depValidation.isValid()) {
            response.setPlanStatus("BLOCKED");
            response.setErrors(depValidation.getErrors());
            response.setPlanningSummary(buildBlockedSummary(depValidation));
            logger.warn("Dependency validation failed for workflow: {}", request.getWorkflowName());
            return response;
        }

        // 2. Determine optimal execution order (dependency-first topological sort)
        List<PlanStep> orderedSteps = determineExecutionOrder(steps, dependencies);
        response.setPlanStatus("READY");

        // 3. Build execution phases
        List<ExecutionPhase> phases = buildExecutionPhases(steps, dependencies);
        response.setExecutionPhases(phases);

        // 4. Build ordered implementation tasks
        List<ImplementationTask> tasks = buildImplementationTasks(orderedSteps, dependencies, phases);
        response.setOrderedImplementationTasks(tasks);

        // 5. Determine required prerequisites
        List<String> prerequisites = determinePrerequisites(steps, dependencies);
        response.setRequiredPrerequisites(prerequisites);

        // 6. Build validation checkpoints
        List<ValidationCheckpoint> checkpoints = buildValidationCheckpoints(tasks);
        response.setValidationCheckpoints(checkpoints);

        // 7. Build recommended testing points
        List<TestingPoint> testingPoints = buildTestingPoints(tasks);
        response.setRecommendedTestingPoints(testingPoints);

        // 8. Assess risks
        List<RiskAssessment> risks = assessRisks(steps, dependencies);
        response.setPotentialRisks(risks);

        // 9. Estimate effort
        EffortEstimate effort = estimateEffort(steps, dependencies, risks);
        response.setEstimatedImplementationEffort(effort);

        // 10. Detect critical path
        List<String> criticalPath = detectCriticalPath(tasks);
        response.setCriticalPath(criticalPath);

        // 11. Build planning summary
        PlanningSummary summary = buildSummary(phases, tasks, dependencies, risks, criticalPath);
        response.setPlanningSummary(summary);

        logger.info("Execution plan generated successfully for workflow: {} ({} phases, {} tasks, {} risks)",
                request.getWorkflowName(), phases.size(), tasks.size(), risks.size());

        return response;
    }

    /**
     * Validates the execution plan request for required fields.
     */
    private List<String> validateRequest(ExecutionPlanRequest request) {
        List<String> errors = new ArrayList<>();

        if (request.getWorkflowName() == null || request.getWorkflowName().trim().isEmpty()) {
            errors.add("Workflow name is required");
        }

        if (request.getSteps() == null || request.getSteps().isEmpty()) {
            errors.add("At least one workflow step is required");
        }

        // Validate step names are unique
        if (request.getSteps() != null) {
            Set<String> stepNames = new HashSet<>();
            for (PlanStep step : request.getSteps()) {
                if (step.getName() == null || step.getName().trim().isEmpty()) {
                    errors.add("Step name must not be null or empty");
                } else if (!stepNames.add(step.getName())) {
                    errors.add("Duplicate step name: '" + step.getName() + "'");
                }
            }
        }

        return errors;
    }

    /**
     * Validates workflow dependencies: checks for missing step references
     * and circular dependencies.
     */
    private DependencyValidationInfo validateDependencies(List<PlanStep> steps, List<PlanDependency> dependencies) {
        DependencyValidationInfo result = new DependencyValidationInfo();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (dependencies == null || dependencies.isEmpty()) {
            result.setValid(true);
            result.setErrors(errors);
            result.setWarnings(warnings);
            return result;
        }

        // Build set of valid step names
        Set<String> stepNames = steps.stream()
                .map(PlanStep::getName)
                .collect(Collectors.toSet());

        // Check each dependency
        for (PlanDependency dep : dependencies) {
            String stepName = dep.getStepName();

            if (!stepNames.contains(stepName)) {
                errors.add("Dependency references unknown step: '" + stepName + "'");
            }

            for (String dependsOn : dep.getDependsOn()) {
                if (!stepNames.contains(dependsOn)) {
                    errors.add("Step '" + stepName + "' depends on unknown step: '" + dependsOn + "'");
                }
                if (stepName.equals(dependsOn)) {
                    errors.add("Step '" + stepName + "' cannot depend on itself");
                }
            }
        }

        // Check for circular dependencies using DFS
        Map<String, List<String>> depMap = buildDependencyMap(dependencies);
        Set<String> circularSteps = detectCircularDependencies(depMap);

        for (String circular : circularSteps) {
            errors.add("Circular dependency detected involving step: '" + circular + "'");
        }

        result.setValid(errors.isEmpty());
        result.setErrors(errors);
        result.setWarnings(warnings);
        return result;
    }

    /**
     * Detects circular dependencies in the dependency graph using DFS.
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
     * Determines the optimal execution order using topological sort.
     * Steps with no dependencies come first, then steps whose dependencies
     * have been satisfied, maintaining original order where possible.
     */
    private List<PlanStep> determineExecutionOrder(List<PlanStep> steps, List<PlanDependency> dependencies) {
        Map<String, PlanStep> stepMap = new LinkedHashMap<>();
        for (PlanStep step : steps) {
            stepMap.put(step.getName(), step);
        }

        Map<String, List<String>> depMap = buildDependencyMap(dependencies);
        Map<String, Integer> inDegree = new HashMap<>();

        // Initialize in-degree for all steps
        for (PlanStep step : steps) {
            inDegree.put(step.getName(), 0);
        }

        // Calculate in-degree from dependencies
        for (Map.Entry<String, List<String>> entry : depMap.entrySet()) {
            for (String dep : entry.getValue()) {
                inDegree.merge(entry.getKey(), 1, Integer::sum);
            }
        }

        // Kahn's algorithm for topological sort
        List<String> sortedNames = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        // Find all steps with no dependencies (in-degree = 0)
        List<String> noDeps = steps.stream()
                .map(PlanStep::getName)
                .filter(name -> inDegree.getOrDefault(name, 0) == 0)
                .collect(Collectors.toList());

        // Process in original order where possible
        while (visited.size() < steps.size()) {
            boolean progress = false;

            for (String name : noDeps) {
                if (!visited.contains(name)) {
                    visited.add(name);
                    sortedNames.add(name);
                    progress = true;

                    // Reduce in-degree of dependents
                    for (Map.Entry<String, List<String>> entry : depMap.entrySet()) {
                        if (entry.getValue().contains(name)) {
                            inDegree.merge(entry.getKey(), -1, Integer::sum);
                        }
                    }
                }
            }

            // Recalculate no-dependency steps
            noDeps = steps.stream()
                    .map(PlanStep::getName)
                    .filter(name -> !visited.contains(name))
                    .filter(name -> inDegree.getOrDefault(name, 0) == 0)
                    .collect(Collectors.toList());

            if (!progress && visited.size() < steps.size()) {
                // Add remaining steps (should not happen if validation passed)
                for (PlanStep step : steps) {
                    if (!visited.contains(step.getName())) {
                        sortedNames.add(step.getName());
                        visited.add(step.getName());
                    }
                }
                break;
            }
        }

        // Map back to PlanStep objects
        List<PlanStep> ordered = new ArrayList<>();
        for (String name : sortedNames) {
            PlanStep step = stepMap.get(name);
            if (step != null) {
                ordered.add(step);
            }
        }

        return ordered;
    }

    /**
     * Builds logical execution phases grouping related tasks by category.
     */
    private List<ExecutionPhase> buildExecutionPhases(List<PlanStep> steps, List<PlanDependency> dependencies) {
        List<ExecutionPhase> phases = new ArrayList<>();

        if (steps == null || steps.isEmpty()) {
            return phases;
        }

        // Group steps by category
        Map<String, List<PlanStep>> categorized = new LinkedHashMap<>();
        List<PlanStep> uncategorized = new ArrayList<>();

        for (PlanStep step : steps) {
            String category = step.getCategory();
            if (category != null && !category.trim().isEmpty()) {
                categorized.computeIfAbsent(category, k -> new ArrayList<>()).add(step);
            } else {
                uncategorized.add(step);
            }
        }

        int phaseOrder = 0;

        // Create phases for each category
        for (Map.Entry<String, List<PlanStep>> entry : categorized.entrySet()) {
            ExecutionPhase phase = new ExecutionPhase();
            phaseOrder++;
            phase.setName(entry.getKey());
            phase.setDescription("Execution phase: " + entry.getKey());
            phase.setOrder(phaseOrder);
            phase.setStatus("PENDING");
            for (PlanStep step : entry.getValue()) {
                phase.addTask(step.getName());
            }
            phase.setEstimatedEffort(estimatePhaseEffort(entry.getValue()));
            phases.add(phase);
        }

        // Create phase for uncategorized steps
        if (!uncategorized.isEmpty()) {
            ExecutionPhase phase = new ExecutionPhase();
            phaseOrder++;
            phase.setName("General");
            phase.setDescription("General execution phase");
            phase.setOrder(phaseOrder);
            phase.setStatus("PENDING");
            for (PlanStep step : uncategorized) {
                phase.addTask(step.getName());
            }
            phase.setEstimatedEffort(estimatePhaseEffort(uncategorized));
            phases.add(phase);
        }

        return phases;
    }

    /**
     * Estimates effort for a group of steps in a phase.
     */
    private String estimatePhaseEffort(List<PlanStep> steps) {
        int count = steps.size();
        if (count <= 2) {
            return "Low";
        } else if (count <= 5) {
            return "Medium";
        } else {
            return "High";
        }
    }

    /**
     * Builds ordered implementation tasks with dependency context.
     */
    private List<ImplementationTask> buildImplementationTasks(
            List<PlanStep> orderedSteps, List<PlanDependency> dependencies, List<ExecutionPhase> phases) {

        List<ImplementationTask> tasks = new ArrayList<>();
        Map<String, String> stepToPhase = new HashMap<>();

        // Build step-to-phase mapping
        for (ExecutionPhase phase : phases) {
            for (String taskName : phase.getTasks()) {
                stepToPhase.put(taskName, phase.getName());
            }
        }

        // Build dependency lookup
        Map<String, List<String>> depMap = buildDependencyMap(dependencies);

        int order = 0;
        for (PlanStep step : orderedSteps) {
            order++;
            ImplementationTask task = new ImplementationTask();
            task.setOrder(order);
            task.setName(step.getName());
            task.setDescription(step.getDescription());
            task.setPhase(stepToPhase.getOrDefault(step.getName(), "General"));
            task.setStatus("PENDING");

            // Set dependencies
            List<String> stepDeps = depMap.getOrDefault(step.getName(), List.of());
            task.setDependencies(new ArrayList<>(stepDeps));

            // Estimate complexity based on dependencies
            if (stepDeps.isEmpty()) {
                task.setEstimatedComplexity("Low");
            } else if (stepDeps.size() <= 2) {
                task.setEstimatedComplexity("Medium");
            } else {
                task.setEstimatedComplexity("High");
            }

            // Suggest required context
            if (stepDeps.isEmpty()) {
                task.addRequiredContext("No dependencies - can start immediately");
            } else {
                task.addRequiredContext("Requires completion of: " + String.join(", ", stepDeps));
            }

            tasks.add(task);
        }

        return tasks;
    }

    /**
     * Determines overall prerequisites based on workflow type and dependencies.
     */
    private List<String> determinePrerequisites(List<PlanStep> steps, List<PlanDependency> dependencies) {
        List<String> prerequisites = new ArrayList<>();

        // Always include repository context as a prerequisite
        prerequisites.add("Repository context must be available");

        // Steps with no dependencies are independent prerequisites
        if (dependencies != null) {
            Set<String> dependentSteps = dependencies.stream()
                    .flatMap(d -> d.getDependsOn().stream())
                    .collect(Collectors.toSet());

            for (PlanStep step : steps) {
                if (!dependentSteps.contains(step.getName())) {
                    prerequisites.add("Independent step: " + step.getName());
                }
            }
        }

        return prerequisites;
    }

    /**
     * Builds validation checkpoints at key points in the execution plan.
     */
    private List<ValidationCheckpoint> buildValidationCheckpoints(List<ImplementationTask> tasks) {
        List<ValidationCheckpoint> checkpoints = new ArrayList<>();
        int taskCount = tasks.size();

        if (taskCount == 0) {
            return checkpoints;
        }

        // Checkpoint after first task
        ValidationCheckpoint first = new ValidationCheckpoint();
        first.setName("Initial Validation");
        first.setDescription("Validate initial step execution and verify no regressions");
        first.setAfterTaskOrder(1);
        first.setValidationType("COMPILE");
        checkpoints.add(first);

        // Checkpoint at midpoint
        if (taskCount > 2) {
            int midPoint = taskCount / 2;
            ValidationCheckpoint mid = new ValidationCheckpoint();
            mid.setName("Mid-Plan Validation");
            mid.setDescription("Validate intermediate results and dependency chain");
            mid.setAfterTaskOrder(midPoint);
            mid.setValidationType("INTEGRATION");
            checkpoints.add(mid);
        }

        // Checkpoint after last task
        ValidationCheckpoint last = new ValidationCheckpoint();
        last.setName("Final Validation");
        last.setDescription("Complete validation of all implementation steps");
        last.setAfterTaskOrder(taskCount);
        last.setValidationType("FULL");
        checkpoints.add(last);

        return checkpoints;
    }

    /**
     * Builds recommended testing points at key stages.
     */
    private List<TestingPoint> buildTestingPoints(List<ImplementationTask> tasks) {
        List<TestingPoint> testingPoints = new ArrayList<>();
        int taskCount = tasks.size();

        if (taskCount == 0) {
            return testingPoints;
        }

        // Test after each phase transition
        Set<String> seenPhases = new HashSet<>();
        for (ImplementationTask task : tasks) {
            String phase = task.getPhase();
            if (!seenPhases.contains(phase)) {
                seenPhases.add(phase);
                if (seenPhases.size() > 1) {
                    TestingPoint tp = new TestingPoint();
                    tp.setName("Phase Transition Test: " + phase);
                    tp.setDescription("Test integration across phase boundary");
                    tp.setAfterTaskOrder(task.getOrder() - 1);
                    tp.setTestScope("Integration");
                    testingPoints.add(tp);
                }
            }
        }

        // Final test point
        TestingPoint finalTest = new TestingPoint();
        finalTest.setName("Complete Implementation Test");
        finalTest.setDescription("Full test suite execution");
        finalTest.setAfterTaskOrder(taskCount);
        finalTest.setTestScope("Full");
        testingPoints.add(finalTest);

        return testingPoints;
    }

    /**
     * Assesses risks based on workflow complexity, dependencies, and step categories.
     */
    private List<RiskAssessment> assessRisks(List<PlanStep> steps, List<PlanDependency> dependencies) {
        List<RiskAssessment> risks = new ArrayList<>();
        int totalSteps = steps != null ? steps.size() : 0;
        int totalDeps = dependencies != null ? dependencies.size() : 0;

        // Risk: Large workflow
        if (totalSteps > 10) {
            RiskAssessment risk = new RiskAssessment();
            risk.setDescription("Large workflow with " + totalSteps + " steps increases complexity");
            risk.setSeverity("MEDIUM");
            risk.setImpact("Higher chance of errors and longer execution time");
            risk.setMitigation("Break workflow into smaller, focused sub-workflows");
            risks.add(risk);
        }

        // Risk: High dependency count
        if (totalDeps > 5) {
            RiskAssessment risk = new RiskAssessment();
            risk.setDescription("High dependency count (" + totalDeps + " dependencies) may cause cascading failures");
            risk.setSeverity("HIGH");
            risk.setImpact("Failure in early steps may block dependent steps");
            risk.setMitigation("Validate each dependency chain before proceeding");
            risks.add(risk);
        }

        // Risk: Steps with many dependencies
        if (dependencies != null) {
            for (PlanDependency dep : dependencies) {
                if (dep.getDependsOn().size() > 3) {
                    RiskAssessment risk = new RiskAssessment();
                    risk.setDescription("Step '" + dep.getStepName() + "' has " + dep.getDependsOn().size() + " dependencies");
                    risk.setSeverity("MEDIUM");
                    risk.setImpact("Step is heavily coupled to other steps");
                    risk.setMitigation("Ensure all dependency steps are thoroughly tested first");
                    risks.add(risk);
                }
            }
        }

        // Risk: Steps with no category
        long uncategorized = steps.stream()
                .filter(s -> s.getCategory() == null || s.getCategory().trim().isEmpty())
                .count();
        if (uncategorized > 0) {
            RiskAssessment risk = new RiskAssessment();
            risk.setDescription(uncategorized + " steps are uncategorized");
            risk.setSeverity("LOW");
            risk.setImpact("Unclear purpose may lead to implementation gaps");
            risk.setMitigation("Review and categorize uncategorized steps");
            risks.add(risk);
        }

        return risks;
    }

    /**
     * Estimates overall implementation effort.
     */
    private EffortEstimate estimateEffort(List<PlanStep> steps, List<PlanDependency> dependencies, List<RiskAssessment> risks) {
        EffortEstimate effort = new EffortEstimate();
        int totalSteps = steps != null ? steps.size() : 0;

        effort.setTotalTasks(totalSteps);

        // Calculate estimated minutes based on step count and complexity
        int baseMinutes = totalSteps * 15; // 15 minutes base per step
        int dependencyOverhead = (dependencies != null ? dependencies.size() : 0) * 5; // 5 min per dependency
        int riskOverhead = risks.size() * 10; // 10 min per risk item
        int estimatedMinutes = baseMinutes + dependencyOverhead + riskOverhead;

        effort.setEstimatedMinutes(estimatedMinutes);

        // Determine overall complexity
        if (totalSteps <= 3) {
            effort.setOverallComplexity("LOW");
        } else if (totalSteps <= 8) {
            effort.setOverallComplexity("MEDIUM");
        } else {
            effort.setOverallComplexity("HIGH");
        }

        effort.setDescription(String.format(
                "Estimated effort: %d minutes (%d steps × %d min base + %d dependencies × %d min + %d risks × %d min)",
                estimatedMinutes, totalSteps, 15,
                dependencies != null ? dependencies.size() : 0, 5,
                risks.size(), 10));

        return effort;
    }

    /**
     * Detects the critical path through the execution plan.
     * The critical path consists of steps that have the longest chain of dependencies.
     */
    private List<String> detectCriticalPath(List<ImplementationTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return List.of();
        }

        // Build adjacency and dependency maps
        Map<String, List<String>> dependsOn = new HashMap<>();
        Map<String, Integer> depth = new HashMap<>();

        for (ImplementationTask task : tasks) {
            dependsOn.put(task.getName(), task.getDependencies());
        }

        // Calculate depth for each task (longest chain of dependencies)
        for (ImplementationTask task : tasks) {
            calculateDepth(task.getName(), dependsOn, depth, new HashSet<>());
        }

        // Find max depth
        int maxDepth = depth.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (maxDepth == 0) {
            // All tasks have no dependencies - critical path is first to last
            return tasks.stream().map(ImplementationTask::getName).collect(Collectors.toList());
        }

        // Collect steps on critical path (those with max depth)
        List<String> criticalPath = new ArrayList<>();
        for (ImplementationTask task : tasks) {
            if (depth.getOrDefault(task.getName(), 0) == maxDepth) {
                criticalPath.add(task.getName());
            }
        }

        return criticalPath;
    }

    private int calculateDepth(String stepName, Map<String, List<String>> dependsOn,
                                Map<String, Integer> depth, Set<String> visited) {
        if (depth.containsKey(stepName)) {
            return depth.get(stepName);
        }

        if (visited.contains(stepName)) {
            return 0; // Cycle detected, return 0
        }

        visited.add(stepName);

        List<String> deps = dependsOn.getOrDefault(stepName, List.of());
        int maxDepDepth = 0;
        for (String dep : deps) {
            int depDepth = calculateDepth(dep, dependsOn, depth, visited);
            maxDepDepth = Math.max(maxDepDepth, depDepth);
        }

        int stepDepth = maxDepDepth + 1;
        depth.put(stepName, stepDepth);
        return stepDepth;
    }

    /**
     * Builds a summary of the complete execution plan.
     */
    private PlanningSummary buildSummary(List<ExecutionPhase> phases, List<ImplementationTask> tasks,
                                         List<PlanDependency> dependencies, List<RiskAssessment> risks,
                                         List<String> criticalPath) {
        PlanningSummary summary = new PlanningSummary();
        summary.setTotalPhases(phases.size());
        summary.setTotalTasks(tasks.size());
        summary.setValidatedDependencies(dependencies != null ? dependencies.size() : 0);
        summary.setTotalRisks(risks.size());
        summary.setCriticalPathLength(criticalPath.size());

        // Generate recommendation
        StringBuilder recommendation = new StringBuilder();
        if (risks.isEmpty()) {
            recommendation.append("No risks identified. ");
        } else {
            long highRisks = risks.stream()
                    .filter(r -> "HIGH".equals(r.getSeverity()))
                    .count();
            if (highRisks > 0) {
                recommendation.append("Address ").append(highRisks)
                        .append(" high-severity risks before proceeding. ");
            }
        }

        int totalMinutes = tasks.stream()
                .mapToInt(t -> {
                    if ("LOW".equals(t.getEstimatedComplexity())) return 15;
                    if ("MEDIUM".equals(t.getEstimatedComplexity())) return 30;
                    return 60;
                })
                .sum();

        recommendation.append("Estimated total implementation time: ~")
                .append(totalMinutes).append(" minutes (")
                .append(phases.size()).append(" phases, ")
                .append(tasks.size()).append(" tasks). ");

        recommendation.append("Start with independent steps and follow the dependency chain.");

        summary.setRecommendation(recommendation.toString());
        return summary;
    }

    /**
     * Builds summary for invalid request.
     */
    private PlanningSummary buildErrorSummary(List<String> errors) {
        PlanningSummary summary = new PlanningSummary();
        summary.setTotalPhases(0);
        summary.setTotalTasks(0);
        summary.setValidatedDependencies(0);
        summary.setTotalRisks(0);
        summary.setCriticalPathLength(0);
        summary.setRecommendation("Fix " + errors.size() + " validation error(s) before planning: "
                + String.join("; ", errors));
        return summary;
    }

    /**
     * Builds summary for blocked plan (dependency validation failed).
     */
    private PlanningSummary buildBlockedSummary(DependencyValidationInfo depValidation) {
        PlanningSummary summary = new PlanningSummary();
        summary.setTotalPhases(0);
        summary.setTotalTasks(0);
        summary.setValidatedDependencies(0);
        summary.setTotalRisks(0);
        summary.setCriticalPathLength(0);
        summary.setRecommendation("Resolve " + depValidation.getErrors().size()
                + " dependency error(s) before planning: "
                + String.join("; ", depValidation.getErrors()));
        return summary;
    }

    /**
     * Builds a dependency map from the list of dependencies.
     */
    private Map<String, List<String>> buildDependencyMap(List<PlanDependency> dependencies) {
        Map<String, List<String>> map = new HashMap<>();
        if (dependencies != null) {
            for (PlanDependency dep : dependencies) {
                map.put(dep.getStepName(), new ArrayList<>(dep.getDependsOn()));
            }
        }
        return map;
    }
}