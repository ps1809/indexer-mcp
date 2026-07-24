package com.projectiq.mcp.handoff.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.session.dto.DevelopmentSession.ExecutionHistoryEntry;
import com.projectiq.mcp.session.dto.DevelopmentSession.SessionSummary;
import com.projectiq.mcp.pipeline.dto.ContextPackage;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.readiness.dto.ReadinessReport;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A deterministic, self-contained handoff package that captures the complete
 * state of an AI-assisted development session. This package enables any
 * compatible AI coding agent to immediately continue work without re-analyzing
 * the repository or rebuilding execution context.
 *
 * <p>All collections maintain stable ordering. No duplicate information.
 * Once recorded, execution history is immutable. The package is fully
 * deterministic and serializable.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "packageVersion",
        "exportedAt",
        "sessionId",
        "repositoryId",
        "repositoryName",
        "developerRequest",
        "workflowType",
        "currentStage",
        "completedStages",
        "pendingStages",
        "workflowProgress",
        "sessionStatus",
        "executionHistory",
        "contextPackage",
        "validationReport",
        "recommendationReport",
        "readinessReport",
        "readinessAssessment",
        "validationSummary",
        "recommendationSummary",
        "sessionSummary",
        "outstandingRisks",
        "suggestedNextActions",
        "integrityHash"
})
public class AgentHandoffPackage {

    private String packageVersion;
    private LocalDateTime exportedAt;
    private String sessionId;
    private String repositoryId;
    private String repositoryName;
    private String developerRequest;
    private WorkflowType workflowType;
    private String currentStage;
    private List<String> completedStages;
    private List<String> pendingStages;
    private double workflowProgress;
    private String sessionStatus;
    private List<ExecutionHistoryEntry> executionHistory;
    private ContextPackage contextPackage;
    private ValidationReport validationReport;
    private RecommendationReport recommendationReport;
    private ReadinessReport readinessReport;
    private String readinessAssessment;
    private String validationSummary;
    private String recommendationSummary;
    private SessionSummary sessionSummary;
    private List<String> outstandingRisks;
    private List<String> suggestedNextActions;
    private String integrityHash;

    public AgentHandoffPackage() {
        this.completedStages = new ArrayList<>();
        this.pendingStages = new ArrayList<>();
        this.executionHistory = new ArrayList<>();
        this.outstandingRisks = new ArrayList<>();
        this.suggestedNextActions = new ArrayList<>();
        this.exportedAt = LocalDateTime.now();
        this.packageVersion = "1.0";
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public LocalDateTime getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(LocalDateTime exportedAt) {
        this.exportedAt = exportedAt;
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

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
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

    public double getWorkflowProgress() {
        return workflowProgress;
    }

    public void setWorkflowProgress(double workflowProgress) {
        this.workflowProgress = workflowProgress;
    }

    public String getSessionStatus() {
        return sessionStatus;
    }

    public void setSessionStatus(String sessionStatus) {
        this.sessionStatus = sessionStatus;
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

    public ReadinessReport getReadinessReport() {
        return readinessReport;
    }

    public void setReadinessReport(ReadinessReport readinessReport) {
        this.readinessReport = readinessReport;
    }

    public String getReadinessAssessment() {
        return readinessAssessment;
    }

    public void setReadinessAssessment(String readinessAssessment) {
        this.readinessAssessment = readinessAssessment;
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

    public SessionSummary getSessionSummary() {
        return sessionSummary;
    }

    public void setSessionSummary(SessionSummary sessionSummary) {
        this.sessionSummary = sessionSummary;
    }

    public List<String> getOutstandingRisks() {
        return outstandingRisks;
    }

    public void setOutstandingRisks(List<String> outstandingRisks) {
        this.outstandingRisks = outstandingRisks != null ? new ArrayList<>(outstandingRisks) : new ArrayList<>();
    }

    public void addOutstandingRisk(String risk) {
        if (!this.outstandingRisks.contains(risk)) {
            this.outstandingRisks.add(risk);
        }
    }

    public List<String> getSuggestedNextActions() {
        return suggestedNextActions;
    }

    public void setSuggestedNextActions(List<String> suggestedNextActions) {
        this.suggestedNextActions = suggestedNextActions != null ? new ArrayList<>(suggestedNextActions) : new ArrayList<>();
    }

    public void addSuggestedNextAction(String action) {
        if (!this.suggestedNextActions.contains(action)) {
            this.suggestedNextActions.add(action);
        }
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }
}