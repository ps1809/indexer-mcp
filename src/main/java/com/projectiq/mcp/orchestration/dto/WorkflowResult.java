package com.projectiq.mcp.orchestration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * The final workflow report produced by the WorkflowOrchestratorService.
 * Contains all information about the workflow execution, including the
 * original request, workflow type, execution plan, completed/skipped/failed
 * steps, repository insights, risks, suggested next actions, and duration.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "originalRequest",
        "workflowType",
        "executionPlan",
        "completedSteps",
        "skippedSteps",
        "failedSteps",
        "repositoryInsights",
        "risksIdentified",
        "suggestedNextActions",
        "totalDurationMillis",
        "executionStatus",
        "summary"
})
public class WorkflowResult {

    private String originalRequest;
    private String workflowType;
    private List<String> executionPlan;
    private List<StepResult> completedSteps;
    private List<StepResult> skippedSteps;
    private List<StepResult> failedSteps;
    private List<String> repositoryInsights;
    private List<String> risksIdentified;
    private List<String> suggestedNextActions;
    private long totalDurationMillis;
    private String executionStatus;
    private String summary;

    public WorkflowResult() {
        this.completedSteps = new ArrayList<>();
        this.skippedSteps = new ArrayList<>();
        this.failedSteps = new ArrayList<>();
        this.executionPlan = new ArrayList<>();
        this.repositoryInsights = new ArrayList<>();
        this.risksIdentified = new ArrayList<>();
        this.suggestedNextActions = new ArrayList<>();
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public List<String> getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(List<String> executionPlan) {
        this.executionPlan = executionPlan;
    }

    public void addExecutionPlanItem(String item) {
        this.executionPlan.add(item);
    }

    public List<StepResult> getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(List<StepResult> completedSteps) {
        this.completedSteps = completedSteps;
    }

    public void addCompletedStep(StepResult step) {
        this.completedSteps.add(step);
    }

    public List<StepResult> getSkippedSteps() {
        return skippedSteps;
    }

    public void setSkippedSteps(List<StepResult> skippedSteps) {
        this.skippedSteps = skippedSteps;
    }

    public void addSkippedStep(StepResult step) {
        this.skippedSteps.add(step);
    }

    public List<StepResult> getFailedSteps() {
        return failedSteps;
    }

    public void setFailedSteps(List<StepResult> failedSteps) {
        this.failedSteps = failedSteps;
    }

    public void addFailedStep(StepResult step) {
        this.failedSteps.add(step);
    }

    public List<String> getRepositoryInsights() {
        return repositoryInsights;
    }

    public void setRepositoryInsights(List<String> repositoryInsights) {
        this.repositoryInsights = repositoryInsights;
    }

    public void addRepositoryInsight(String insight) {
        this.repositoryInsights.add(insight);
    }

    public List<String> getRisksIdentified() {
        return risksIdentified;
    }

    public void setRisksIdentified(List<String> risksIdentified) {
        this.risksIdentified = risksIdentified;
    }

    public void addRisk(String risk) {
        this.risksIdentified.add(risk);
    }

    public List<String> getSuggestedNextActions() {
        return suggestedNextActions;
    }

    public void setSuggestedNextActions(List<String> suggestedNextActions) {
        this.suggestedNextActions = suggestedNextActions;
    }

    public void addSuggestedNextAction(String action) {
        this.suggestedNextActions.add(action);
    }

    public long getTotalDurationMillis() {
        return totalDurationMillis;
    }

    public void setTotalDurationMillis(long totalDurationMillis) {
        this.totalDurationMillis = totalDurationMillis;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Represents the result of a single workflow step.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StepResult {
        private int order;
        private String name;
        private String description;
        private String status;
        private String result;
        private String error;
        private long durationMillis;

        public StepResult() {
        }

        public StepResult(int order, String name, String description, String status) {
            this.order = order;
            this.name = name;
            this.description = description;
            this.status = status;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public long getDurationMillis() {
            return durationMillis;
        }

        public void setDurationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
        }
    }
}