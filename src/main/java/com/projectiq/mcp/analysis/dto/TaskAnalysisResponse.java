package com.projectiq.mcp.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete result of analyzing a development task.
 * Contains the original task, detected type, entities, required tools,
 * execution plan, reasoning, and complexity assessment.
 */
public class TaskAnalysisResponse {

    private String originalTask;
    private TaskType taskType;
    private ConfidenceLevel confidenceLevel;
    private List<String> detectedEntities;
    private List<String> suggestedTools;
    private List<ExecutionStep> executionPlan;
    private String reasoningSummary;
    private ComplexityLevel estimatedComplexity;

    public TaskAnalysisResponse() {
        this.detectedEntities = new ArrayList<>();
        this.suggestedTools = new ArrayList<>();
        this.executionPlan = new ArrayList<>();
    }

    public String getOriginalTask() {
        return originalTask;
    }

    public void setOriginalTask(String originalTask) {
        this.originalTask = originalTask;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public ConfidenceLevel getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(ConfidenceLevel confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public List<String> getDetectedEntities() {
        return detectedEntities;
    }

    public void setDetectedEntities(List<String> detectedEntities) {
        this.detectedEntities = detectedEntities;
    }

    public void addDetectedEntity(String entity) {
        if (this.detectedEntities == null) {
            this.detectedEntities = new ArrayList<>();
        }
        this.detectedEntities.add(entity);
    }

    public List<String> getSuggestedTools() {
        return suggestedTools;
    }

    public void setSuggestedTools(List<String> suggestedTools) {
        this.suggestedTools = suggestedTools;
    }

    public void addSuggestedTool(String tool) {
        if (this.suggestedTools == null) {
            this.suggestedTools = new ArrayList<>();
        }
        this.suggestedTools.add(tool);
    }

    public List<ExecutionStep> getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(List<ExecutionStep> executionPlan) {
        this.executionPlan = executionPlan;
    }

    public void addExecutionStep(ExecutionStep step) {
        if (this.executionPlan == null) {
            this.executionPlan = new ArrayList<>();
        }
        this.executionPlan.add(step);
    }

    public String getReasoningSummary() {
        return reasoningSummary;
    }

    public void setReasoningSummary(String reasoningSummary) {
        this.reasoningSummary = reasoningSummary;
    }

    public ComplexityLevel getEstimatedComplexity() {
        return estimatedComplexity;
    }

    public void setEstimatedComplexity(ComplexityLevel estimatedComplexity) {
        this.estimatedComplexity = estimatedComplexity;
    }

    /**
     * Represents a single step in the execution plan.
     */
    public static class ExecutionStep {
        private int stepNumber;
        private String toolName;
        private String description;

        public ExecutionStep() {
        }

        public ExecutionStep(int stepNumber, String toolName, String description) {
            this.stepNumber = stepNumber;
            this.toolName = toolName;
            this.description = description;
        }

        public int getStepNumber() {
            return stepNumber;
        }

        public void setStepNumber(int stepNumber) {
            this.stepNumber = stepNumber;
        }

        public String getToolName() {
            return toolName;
        }

        public void setToolName(String toolName) {
            this.toolName = toolName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}