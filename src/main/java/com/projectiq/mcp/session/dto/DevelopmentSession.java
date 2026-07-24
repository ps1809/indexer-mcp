package com.projectiq.mcp.session.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.pipeline.dto.ContextPackage;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.validation.dto.ValidationReport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete development session that preserves workflow progress,
 * execution context, collected repository intelligence, validation results,
 * recommendations, and implementation history.
 *
 * <p>Sessions follow a deterministic lifecycle:
 * CREATED -> IN_PROGRESS -> COMPLETED / ARCHIVED.
 *
 * <p>Once a stage is completed, its history entry is immutable.
 * No duplicate workflow stages are permitted.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "sessionId",
        "repositoryId",
        "developerRequest",
        "workflowType",
        "currentStage",
        "status",
        "workflowProgress",
        "executionHistory",
        "contextPackage",
        "validationReport",
        "recommendationReport",
        "sessionSummary",
        "createdAt",
        "updatedAt",
        "completedAt",
        "archivedAt"
})
public class DevelopmentSession {

    private String sessionId;
    private String repositoryId;
    private String developerRequest;
    private WorkflowType workflowType;
    private String currentStage;
    private SessionStatus status;
    private double workflowProgress;
    private List<String> completedStages;
    private List<String> pendingStages;
    private List<ExecutionHistoryEntry> executionHistory;
    private ContextPackage contextPackage;
    private ValidationReport validationReport;
    private RecommendationReport recommendationReport;
    private SessionSummary sessionSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private LocalDateTime archivedAt;

    public DevelopmentSession() {
        this.completedStages = new ArrayList<>();
        this.pendingStages = new ArrayList<>();
        this.executionHistory = new ArrayList<>();
        this.status = SessionStatus.CREATED;
        this.workflowProgress = 0.0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getDeveloperRequest() {
        return developerRequest;
    }

    public void setDeveloperRequest(String developerRequest) {
        this.developerRequest = developerRequest;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(WorkflowType workflowType) {
        this.workflowType = workflowType;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public double getWorkflowProgress() {
        return workflowProgress;
    }

    public void setWorkflowProgress(double workflowProgress) {
        this.workflowProgress = workflowProgress;
    }

    public List<String> getCompletedStages() {
        return completedStages;
    }

    public void setCompletedStages(List<String> completedStages) {
        this.completedStages = completedStages != null ? new ArrayList<>(completedStages) : new ArrayList<>();
    }

    public void addCompletedStage(String stage) {
        if (!this.completedStages.contains(stage)) {
            this.completedStages.add(stage);
        }
    }

    public List<String> getPendingStages() {
        return pendingStages;
    }

    public void setPendingStages(List<String> pendingStages) {
        this.pendingStages = pendingStages != null ? new ArrayList<>(pendingStages) : new ArrayList<>();
    }

    public void addPendingStage(String stage) {
        if (!this.pendingStages.contains(stage) && !this.completedStages.contains(stage)) {
            this.pendingStages.add(stage);
        }
    }

    public List<ExecutionHistoryEntry> getExecutionHistory() {
        return executionHistory;
    }

    public void setExecutionHistory(List<ExecutionHistoryEntry> executionHistory) {
        this.executionHistory = executionHistory != null ? new ArrayList<>(executionHistory) : new ArrayList<>();
    }

    public void addExecutionHistoryEntry(ExecutionHistoryEntry entry) {
        this.executionHistory.add(entry);
    }

    public ContextPackage getContextPackage() {
        return contextPackage;
    }

    public void setContextPackage(ContextPackage contextPackage) {
        this.contextPackage = contextPackage;
    }

    public ValidationReport getValidationReport() {
        return validationReport;
    }

    public void setValidationReport(ValidationReport validationReport) {
        this.validationReport = validationReport;
    }

    public RecommendationReport getRecommendationReport() {
        return recommendationReport;
    }

    public void setRecommendationReport(RecommendationReport recommendationReport) {
        this.recommendationReport = recommendationReport;
    }

    public SessionSummary getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(SessionSummary sessionSummary) {
        this.sessionSummary = sessionSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    /**
     * Represents an immutable entry in the execution history.
     * Once added to the session, it is never modified.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExecutionHistoryEntry {
        private String stageName;
        private String action;
        private String status;
        private String details;
        private LocalDateTime timestamp;

        public ExecutionHistoryEntry() {
            this.timestamp = LocalDateTime.now();
        }

        public ExecutionHistoryEntry(String stageName, String action, String status, String details) {
            this.stageName = stageName;
            this.action = action;
            this.status = status;
            this.details = details;
            this.timestamp = LocalDateTime.now();
        }

        public String getStageName() {
            return stageName;
        }

        public void setStageName(String stageName) {
            this.stageName = stageName;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    /**
     * Summary information for a development session.
     * Includes current workflow, completed/pending stages,
     * repository intelligence summary, validation/recommendation summaries,
     * readiness status, and suggested next step.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SessionSummary {
        private String currentWorkflow;
        private List<String> completedStages;
        private List<String> pendingStages;
        private String repositoryIntelligenceSummary;
        private String validationSummary;
        private String recommendationSummary;
        private String readinessStatus;
        private String suggestedNextStep;

        public SessionSummary() {
            this.completedStages = new ArrayList<>();
            this.pendingStages = new ArrayList<>();
        }

        public String getCurrentWorkflow() {
            return currentWorkflow;
        }

        public void setCurrentWorkflow(String currentWorkflow) {
            this.currentWorkflow = currentWorkflow;
        }

        public List<String> getCompletedStages() {
            return completedStages;
        }

        public void setCompletedStages(List<String> completedStages) {
            this.completedStages = completedStages != null ? new ArrayList<>(completedStages) : new ArrayList<>();
        }

        public List<String> getPendingStages() {
            return pendingStages;
        }

        public void setPendingStages(List<String> pendingStages) {
            this.pendingStages = pendingStages != null ? new ArrayList<>(pendingStages) : new ArrayList<>();
        }

        public String getRepositoryIntelligenceSummary() {
            return repositoryIntelligenceSummary;
        }

        public void setRepositoryIntelligenceSummary(String repositoryIntelligenceSummary) {
            this.repositoryIntelligenceSummary = repositoryIntelligenceSummary;
        }

        public String getValidationSummary() {
            return validationSummary;
        }

        public void setValidationSummary(String validationSummary) {
            this.validationSummary = validationSummary;
        }

        public String getRecommendationSummary() {
            return recommendationSummary;
        }

        public void setRecommendationSummary(String recommendationSummary) {
            this.recommendationSummary = recommendationSummary;
        }

        public String getReadinessStatus() {
            return readinessStatus;
        }

        public void setReadinessStatus(String readinessStatus) {
            this.readinessStatus = readinessStatus;
        }

        public String getSuggestedNextStep() {
            return suggestedNextStep;
        }

        public void setSuggestedNextStep(String suggestedNextStep) {
            this.suggestedNextStep = suggestedNextStep;
        }
    }
}