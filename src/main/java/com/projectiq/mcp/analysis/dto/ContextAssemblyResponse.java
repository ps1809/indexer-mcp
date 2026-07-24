package com.projectiq.mcp.analysis.dto;

import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.RepositoryContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete result of assembling repository context for a
 * development task. Contains the original task, the execution plan, which
 * tools were executed/skipped/failed, the assembled repository and development
 * contexts, an execution summary, and total execution time.
 */
public class ContextAssemblyResponse {

    private String originalTask;
    private TaskAnalysisResponse taskAnalysis;
    private List<ExecutionStep> executionPlan;
    private List<String> executedTools;
    private List<String> skippedTools;
    private List<String> failedTools;
    private RepositoryContext repositoryContext;
    private DevelopmentContext developmentContext;
    private String executionSummary;
    private long totalExecutionTimeMillis;

    public ContextAssemblyResponse() {
        this.executedTools = new ArrayList<>();
        this.skippedTools = new ArrayList<>();
        this.failedTools = new ArrayList<>();
        this.executionPlan = new ArrayList<>();
    }

    public String getOriginalTask() {
        return originalTask;
    }

    public void setOriginalTask(String originalTask) {
        this.originalTask = originalTask;
    }

    public TaskAnalysisResponse getTaskAnalysis() {
        return taskAnalysis;
    }

    public void setTaskAnalysis(TaskAnalysisResponse taskAnalysis) {
        this.taskAnalysis = taskAnalysis;
    }

    public List<ExecutionStep> getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(List<ExecutionStep> executionPlan) {
        this.executionPlan = executionPlan != null ? executionPlan : new ArrayList<>();
    }

    public List<String> getExecutedTools() {
        return executedTools;
    }

    public void setExecutedTools(List<String> executedTools) {
        this.executedTools = executedTools != null ? executedTools : new ArrayList<>();
    }

    public void addExecutedTool(String toolName) {
        if (this.executedTools == null) {
            this.executedTools = new ArrayList<>();
        }
        this.executedTools.add(toolName);
    }

    public List<String> getSkippedTools() {
        return skippedTools;
    }

    public void setSkippedTools(List<String> skippedTools) {
        this.skippedTools = skippedTools != null ? skippedTools : new ArrayList<>();
    }

    public void addSkippedTool(String toolName) {
        if (this.skippedTools == null) {
            this.skippedTools = new ArrayList<>();
        }
        this.skippedTools.add(toolName);
    }

    public List<String> getFailedTools() {
        return failedTools;
    }

    public void setFailedTools(List<String> failedTools) {
        this.failedTools = failedTools != null ? failedTools : new ArrayList<>();
    }

    public void addFailedTool(String toolName) {
        if (this.failedTools == null) {
            this.failedTools = new ArrayList<>();
        }
        this.failedTools.add(toolName);
    }

    public RepositoryContext getRepositoryContext() {
        return repositoryContext;
    }

    public void setRepositoryContext(RepositoryContext repositoryContext) {
        this.repositoryContext = repositoryContext;
    }

    public DevelopmentContext getDevelopmentContext() {
        return developmentContext;
    }

    public void setDevelopmentContext(DevelopmentContext developmentContext) {
        this.developmentContext = developmentContext;
    }

    public String getExecutionSummary() {
        return executionSummary;
    }

    public void setExecutionSummary(String executionSummary) {
        this.executionSummary = executionSummary;
    }

    public long getTotalExecutionTimeMillis() {
        return totalExecutionTimeMillis;
    }

    public void setTotalExecutionTimeMillis(long totalExecutionTimeMillis) {
        this.totalExecutionTimeMillis = totalExecutionTimeMillis;
    }
}