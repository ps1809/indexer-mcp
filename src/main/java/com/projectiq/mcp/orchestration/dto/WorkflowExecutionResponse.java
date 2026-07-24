package com.projectiq.mcp.orchestration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Response from executing a workflow.
 * Contains the workflow identifier, status, executed/skipped/failed steps,
 * progress summary, execution timeline, and final execution report.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "workflowId",
        "workflowStatus",
        "executedSteps",
        "skippedSteps",
        "failedSteps",
        "progressSummary",
        "executionTimeline",
        "finalReport"
})
public class WorkflowExecutionResponse {

    private String workflowId;
    private String workflowStatus;
    private List<StepResultInfo> executedSteps;
    private List<StepResultInfo> skippedSteps;
    private List<StepResultInfo> failedSteps;
    private ProgressSummary progressSummary;
    private List<ExecutionTimelineEntry> executionTimeline;
    private FinalReport finalReport;

    public WorkflowExecutionResponse() {
        this.executedSteps = new ArrayList<>();
        this.skippedSteps = new ArrayList<>();
        this.failedSteps = new ArrayList<>();
        this.executionTimeline = new ArrayList<>();
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public List<StepResultInfo> getExecutedSteps() {
        return executedSteps;
    }

    public void setExecutedSteps(List<StepResultInfo> executedSteps) {
        this.executedSteps = executedSteps;
    }

    public void addExecutedStep(StepResultInfo step) {
        this.executedSteps.add(step);
    }

    public List<StepResultInfo> getSkippedSteps() {
        return skippedSteps;
    }

    public void setSkippedSteps(List<StepResultInfo> skippedSteps) {
        this.skippedSteps = skippedSteps;
    }

    public void addSkippedStep(StepResultInfo step) {
        this.skippedSteps.add(step);
    }

    public List<StepResultInfo> getFailedSteps() {
        return failedSteps;
    }

    public void setFailedSteps(List<StepResultInfo> failedSteps) {
        this.failedSteps = failedSteps;
    }

    public void addFailedStep(StepResultInfo step) {
        this.failedSteps.add(step);
    }

    public ProgressSummary getProgressSummary() {
        return progressSummary;
    }

    public void setProgressSummary(ProgressSummary progressSummary) {
        this.progressSummary = progressSummary;
    }

    public List<ExecutionTimelineEntry> getExecutionTimeline() {
        return executionTimeline;
    }

    public void setExecutionTimeline(List<ExecutionTimelineEntry> executionTimeline) {
        this.executionTimeline = executionTimeline;
    }

    public void addExecutionTimelineEntry(ExecutionTimelineEntry entry) {
        this.executionTimeline.add(entry);
    }

    public FinalReport getFinalReport() {
        return finalReport;
    }

    public void setFinalReport(FinalReport finalReport) {
        this.finalReport = finalReport;
    }

    /**
     * Information about a single step execution result.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StepResultInfo {
        private int order;
        private String name;
        private String description;
        private String status;
        private String result;
        private String error;
        private long durationMillis;

        public StepResultInfo() {
        }

        public StepResultInfo(int order, String name, String description, String status) {
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

    /**
     * Summary of workflow execution progress.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProgressSummary {
        private int totalSteps;
        private int completedSteps;
        private int skippedSteps;
        private int failedSteps;
        private int remainingSteps;
        private String currentStep;
        private double successRate;
        private long totalDurationMillis;

        public ProgressSummary() {
        }

        public int getTotalSteps() {
            return totalSteps;
        }

        public void setTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
        }

        public int getCompletedSteps() {
            return completedSteps;
        }

        public void setCompletedSteps(int completedSteps) {
            this.completedSteps = completedSteps;
        }

        public int getSkippedSteps() {
            return skippedSteps;
        }

        public void setSkippedSteps(int skippedSteps) {
            this.skippedSteps = skippedSteps;
        }

        public int getFailedSteps() {
            return failedSteps;
        }

        public void setFailedSteps(int failedSteps) {
            this.failedSteps = failedSteps;
        }

        public int getRemainingSteps() {
            return remainingSteps;
        }

        public void setRemainingSteps(int remainingSteps) {
            this.remainingSteps = remainingSteps;
        }

        public String getCurrentStep() {
            return currentStep;
        }

        public void setCurrentStep(String currentStep) {
            this.currentStep = currentStep;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }

        public long getTotalDurationMillis() {
            return totalDurationMillis;
        }

        public void setTotalDurationMillis(long totalDurationMillis) {
            this.totalDurationMillis = totalDurationMillis;
        }
    }

    /**
     * Final execution report summary.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FinalReport {
        private String workflowId;
        private String workflowType;
        private String originalRequest;
        private String finalStatus;
        private int totalSteps;
        private int completedCount;
        private int skippedCount;
        private int failedCount;
        private long totalDurationMillis;
        private String summary;

        public FinalReport() {
        }

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
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

        public String getFinalStatus() {
            return finalStatus;
        }

        public void setFinalStatus(String finalStatus) {
            this.finalStatus = finalStatus;
        }

        public int getTotalSteps() {
            return totalSteps;
        }

        public void setTotalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
        }

        public int getCompletedCount() {
            return completedCount;
        }

        public void setCompletedCount(int completedCount) {
            this.completedCount = completedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public void setSkippedCount(int skippedCount) {
            this.skippedCount = skippedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(int failedCount) {
            this.failedCount = failedCount;
        }

        public long getTotalDurationMillis() {
            return totalDurationMillis;
        }

        public void setTotalDurationMillis(long totalDurationMillis) {
            this.totalDurationMillis = totalDurationMillis;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }
}