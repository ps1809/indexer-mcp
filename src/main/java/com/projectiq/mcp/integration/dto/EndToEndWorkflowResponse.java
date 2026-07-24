package com.projectiq.mcp.integration.dto;

import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.handoff.dto.AgentHandoffPackage;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.pipeline.dto.ContextPackage;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.readiness.dto.ReadinessReport;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.validation.dto.ValidationReport;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the complete result of an end-to-end integration workflow execution.
 * Captures every stage from Task Analysis through Agent Handoff.
 */
public class EndToEndWorkflowResponse {

    private String workflowId;
    private String originalRequest;
    private String repositoryName;
    private long totalDurationMillis;

    // Stage 1: Task Analysis
    private TaskAnalysisResponse taskAnalysis;

    // Stage 2: Workflow Generation & Execution
    private WorkflowResult workflowResult;

    // Stage 3: Context Assembly
    private ContextPackage contextPackage;

    // Stage 4: Execution Planning
    private ExecutionPlanResponse executionPlan;

    // Stage 5: Workflow Validation
    private ValidationReport validationReport;

    // Stage 6: Recommendation Generation
    private RecommendationReport recommendationReport;

    // Stage 7: Readiness Assessment
    private ReadinessReport readinessReport;

    // Stage 8: Development Session
    private DevelopmentSession developmentSession;

    // Stage 9: Agent Handoff
    private String handoffPackage;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private String overallStatus;

    public EndToEndWorkflowResponse() {
    }

    // --- Builder-like setters ---

    public EndToEndWorkflowResponse withWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public EndToEndWorkflowResponse withOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
        return this;
    }

    public EndToEndWorkflowResponse withRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    public EndToEndWorkflowResponse withTotalDurationMillis(long totalDurationMillis) {
        this.totalDurationMillis = totalDurationMillis;
        return this;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    // --- Getters and Setters ---

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public long getTotalDurationMillis() {
        return totalDurationMillis;
    }

    public void setTotalDurationMillis(long totalDurationMillis) {
        this.totalDurationMillis = totalDurationMillis;
    }

    public TaskAnalysisResponse getTaskAnalysis() {
        return taskAnalysis;
    }

    public void setTaskAnalysis(TaskAnalysisResponse taskAnalysis) {
        this.taskAnalysis = taskAnalysis;
    }

    public WorkflowResult getWorkflowResult() {
        return workflowResult;
    }

    public void setWorkflowResult(WorkflowResult workflowResult) {
        this.workflowResult = workflowResult;
    }

    public ContextPackage getContextPackage() {
        return contextPackage;
    }

    public void setContextPackage(ContextPackage contextPackage) {
        this.contextPackage = contextPackage;
    }

    public ExecutionPlanResponse getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(ExecutionPlanResponse executionPlan) {
        this.executionPlan = executionPlan;
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

    public DevelopmentSession getDevelopmentSession() {
        return developmentSession;
    }

    public void setDevelopmentSession(DevelopmentSession developmentSession) {
        this.developmentSession = developmentSession;
    }

    public String getHandoffPackage() {
        return handoffPackage;
    }

    public void setHandoffPackage(String handoffPackage) {
        this.handoffPackage = handoffPackage;
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

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}