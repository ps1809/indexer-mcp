package com.projectiq.mcp.planning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Output response containing the deterministic execution roadmap.
 * Includes execution phases, ordered tasks, prerequisites, validation checkpoints,
 * testing points, risks, and estimated effort.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "workflowName",
        "workflowType",
        "originalRequest",
        "planStatus",
        "dependencyValidation",
        "executionPhases",
        "orderedImplementationTasks",
        "requiredPrerequisites",
        "validationCheckpoints",
        "recommendedTestingPoints",
        "potentialRisks",
        "estimatedImplementationEffort",
        "criticalPath",
        "planningSummary",
        "errors"
})
public class ExecutionPlanResponse {

    private String workflowName;
    private String workflowType;
    private String originalRequest;
    private String planStatus;
    private DependencyValidationInfo dependencyValidation;
    private List<ExecutionPhase> executionPhases;
    private List<ImplementationTask> orderedImplementationTasks;
    private List<String> requiredPrerequisites;
    private List<ValidationCheckpoint> validationCheckpoints;
    private List<TestingPoint> recommendedTestingPoints;
    private List<RiskAssessment> potentialRisks;
    private EffortEstimate estimatedImplementationEffort;
    private List<String> criticalPath;
    private PlanningSummary planningSummary;
    private List<String> errors;

    public ExecutionPlanResponse() {
        this.executionPhases = new ArrayList<>();
        this.orderedImplementationTasks = new ArrayList<>();
        this.requiredPrerequisites = new ArrayList<>();
        this.validationCheckpoints = new ArrayList<>();
        this.recommendedTestingPoints = new ArrayList<>();
        this.potentialRisks = new ArrayList<>();
        this.criticalPath = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public String getPlanStatus() {
        return planStatus;
    }

    public void setPlanStatus(String planStatus) {
        this.planStatus = planStatus;
    }

    public DependencyValidationInfo getDependencyValidation() {
        return dependencyValidation;
    }

    public void setDependencyValidation(DependencyValidationInfo dependencyValidation) {
        this.dependencyValidation = dependencyValidation;
    }

    public List<ExecutionPhase> getExecutionPhases() {
        return executionPhases;
    }

    public void setExecutionPhases(List<ExecutionPhase> executionPhases) {
        this.executionPhases = executionPhases;
    }

    public void addExecutionPhase(ExecutionPhase phase) {
        this.executionPhases.add(phase);
    }

    public List<ImplementationTask> getOrderedImplementationTasks() {
        return orderedImplementationTasks;
    }

    public void setOrderedImplementationTasks(List<ImplementationTask> orderedImplementationTasks) {
        this.orderedImplementationTasks = orderedImplementationTasks;
    }

    public void addImplementationTask(ImplementationTask task) {
        this.orderedImplementationTasks.add(task);
    }

    public List<String> getRequiredPrerequisites() {
        return requiredPrerequisites;
    }

    public void setRequiredPrerequisites(List<String> requiredPrerequisites) {
        this.requiredPrerequisites = requiredPrerequisites;
    }

    public void addRequiredPrerequisite(String prerequisite) {
        this.requiredPrerequisites.add(prerequisite);
    }

    public List<ValidationCheckpoint> getValidationCheckpoints() {
        return validationCheckpoints;
    }

    public void setValidationCheckpoints(List<ValidationCheckpoint> validationCheckpoints) {
        this.validationCheckpoints = validationCheckpoints;
    }

    public void addValidationCheckpoint(ValidationCheckpoint checkpoint) {
        this.validationCheckpoints.add(checkpoint);
    }

    public List<TestingPoint> getRecommendedTestingPoints() {
        return recommendedTestingPoints;
    }

    public void setRecommendedTestingPoints(List<TestingPoint> recommendedTestingPoints) {
        this.recommendedTestingPoints = recommendedTestingPoints;
    }

    public void addTestingPoint(TestingPoint testingPoint) {
        this.recommendedTestingPoints.add(testingPoint);
    }

    public List<RiskAssessment> getPotentialRisks() {
        return potentialRisks;
    }

    public void setPotentialRisks(List<RiskAssessment> potentialRisks) {
        this.potentialRisks = potentialRisks;
    }

    public void addRisk(RiskAssessment risk) {
        this.potentialRisks.add(risk);
    }

    public EffortEstimate getEstimatedImplementationEffort() {
        return estimatedImplementationEffort;
    }

    public void setEstimatedImplementationEffort(EffortEstimate estimatedImplementationEffort) {
        this.estimatedImplementationEffort = estimatedImplementationEffort;
    }

    public List<String> getCriticalPath() {
        return criticalPath;
    }

    public void setCriticalPath(List<String> criticalPath) {
        this.criticalPath = criticalPath;
    }

    public void addCriticalPathStep(String step) {
        this.criticalPath.add(step);
    }

    public PlanningSummary getPlanningSummary() {
        return planningSummary;
    }

    public void setPlanningSummary(PlanningSummary planningSummary) {
        this.planningSummary = planningSummary;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    // ========== Nested DTOs ==========

    /**
     * Result of dependency validation.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DependencyValidationInfo {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;

        public DependencyValidationInfo() {
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
    }

    /**
     * A logical execution phase grouping related tasks.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExecutionPhase {
        private String name;
        private String description;
        private int order;
        private String status;
        private List<String> tasks;
        private String estimatedEffort;

        public ExecutionPhase() {
            this.tasks = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<String> getTasks() {
            return tasks;
        }

        public void setTasks(List<String> tasks) {
            this.tasks = tasks;
        }

        public void addTask(String task) {
            this.tasks.add(task);
        }

        public String getEstimatedEffort() {
            return estimatedEffort;
        }

        public void setEstimatedEffort(String estimatedEffort) {
            this.estimatedEffort = estimatedEffort;
        }
    }

    /**
     * A single ordered implementation task.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImplementationTask {
        private int order;
        private String name;
        private String description;
        private String phase;
        private String status;
        private List<String> dependencies;
        private String estimatedComplexity;
        private List<String> requiredContext;

        public ImplementationTask() {
            this.dependencies = new ArrayList<>();
            this.requiredContext = new ArrayList<>();
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPhase() {
            return phase;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
        }

        public void addDependency(String dependency) {
            this.dependencies.add(dependency);
        }

        public String getEstimatedComplexity() {
            return estimatedComplexity;
        }

        public void setEstimatedComplexity(String estimatedComplexity) {
            this.estimatedComplexity = estimatedComplexity;
        }

        public List<String> getRequiredContext() {
            return requiredContext;
        }

        public void setRequiredContext(List<String> requiredContext) {
            this.requiredContext = requiredContext;
        }

        public void addRequiredContext(String context) {
            this.requiredContext.add(context);
        }
    }

    /**
     * A validation checkpoint to verify correctness at a specific point.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationCheckpoint {
        private String name;
        private String description;
        private int afterTaskOrder;
        private String validationType;

        public ValidationCheckpoint() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getAfterTaskOrder() {
            return afterTaskOrder;
        }

        public void setAfterTaskOrder(int afterTaskOrder) {
            this.afterTaskOrder = afterTaskOrder;
        }

        public String getValidationType() {
            return validationType;
        }

        public void setValidationType(String validationType) {
            this.validationType = validationType;
        }
    }

    /**
     * A recommended point for testing during implementation.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestingPoint {
        private String name;
        private String description;
        private int afterTaskOrder;
        private String testScope;

        public TestingPoint() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getAfterTaskOrder() {
            return afterTaskOrder;
        }

        public void setAfterTaskOrder(int afterTaskOrder) {
            this.afterTaskOrder = afterTaskOrder;
        }

        public String getTestScope() {
            return testScope;
        }

        public void setTestScope(String testScope) {
            this.testScope = testScope;
        }
    }

    /**
     * An identified risk with severity and mitigation.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskAssessment {
        private String description;
        private String severity;
        private String impact;
        private String mitigation;

        public RiskAssessment() {
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getImpact() {
            return impact;
        }

        public void setImpact(String impact) {
            this.impact = impact;
        }

        public String getMitigation() {
            return mitigation;
        }

        public void setMitigation(String mitigation) {
            this.mitigation = mitigation;
        }
    }

    /**
     * Estimated effort for implementation.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EffortEstimate {
        private String overallComplexity;
        private int totalTasks;
        private int estimatedMinutes;
        private String description;

        public EffortEstimate() {
        }

        public String getOverallComplexity() {
            return overallComplexity;
        }

        public void setOverallComplexity(String overallComplexity) {
            this.overallComplexity = overallComplexity;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(int totalTasks) {
            this.totalTasks = totalTasks;
        }

        public int getEstimatedMinutes() {
            return estimatedMinutes;
        }

        public void setEstimatedMinutes(int estimatedMinutes) {
            this.estimatedMinutes = estimatedMinutes;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Summary of the entire execution plan.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PlanningSummary {
        private int totalPhases;
        private int totalTasks;
        private int validatedDependencies;
        private int totalRisks;
        private int criticalPathLength;
        private String recommendation;

        public PlanningSummary() {
        }

        public int getTotalPhases() {
            return totalPhases;
        }

        public void setTotalPhases(int totalPhases) {
            this.totalPhases = totalPhases;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(int totalTasks) {
            this.totalTasks = totalTasks;
        }

        public int getValidatedDependencies() {
            return validatedDependencies;
        }

        public void setValidatedDependencies(int validatedDependencies) {
            this.validatedDependencies = validatedDependencies;
        }

        public int getTotalRisks() {
            return totalRisks;
        }

        public void setTotalRisks(int totalRisks) {
            this.totalRisks = totalRisks;
        }

        public int getCriticalPathLength() {
            return criticalPathLength;
        }

        public void setCriticalPathLength(int criticalPathLength) {
            this.criticalPathLength = criticalPathLength;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }
    }
}