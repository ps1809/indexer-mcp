package com.projectiq.mcp.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete result of generating an implementation plan for a
 * development task. Contains the original task, task type, complexity assessment,
 * recommended implementation order, files to modify/review, affected components,
 * dependencies, validation steps, testing scope, risks, and assumptions.
 *
 * <p>This response is deterministic and does not include any generated code.
 * It is designed to guide AI coding agents through the recommended sequence
 * of work before any code is written.</p>
 */
public class ImplementationPlanningResponse {

    private String originalTask;
    private String taskType;
    private String estimatedComplexity;
    private List<String> recommendedImplementationOrder;
    private List<String> filesToModify;
    private List<String> filesToReview;
    private List<String> componentsAffected;
    private List<String> dependenciesInvolved;
    private List<String> suggestedValidationSteps;
    private String suggestedTestingScope;
    private List<String> risks;
    private List<String> assumptions;

    public ImplementationPlanningResponse() {
        this.recommendedImplementationOrder = new ArrayList<>();
        this.filesToModify = new ArrayList<>();
        this.filesToReview = new ArrayList<>();
        this.componentsAffected = new ArrayList<>();
        this.dependenciesInvolved = new ArrayList<>();
        this.suggestedValidationSteps = new ArrayList<>();
        this.risks = new ArrayList<>();
        this.assumptions = new ArrayList<>();
    }

    public String getOriginalTask() {
        return originalTask;
    }

    public void setOriginalTask(String originalTask) {
        this.originalTask = originalTask;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getEstimatedComplexity() {
        return estimatedComplexity;
    }

    public void setEstimatedComplexity(String estimatedComplexity) {
        this.estimatedComplexity = estimatedComplexity;
    }

    public List<String> getRecommendedImplementationOrder() {
        return recommendedImplementationOrder;
    }

    public void setRecommendedImplementationOrder(List<String> recommendedImplementationOrder) {
        this.recommendedImplementationOrder = recommendedImplementationOrder;
    }

    public void addRecommendedStep(String step) {
        if (this.recommendedImplementationOrder == null) {
            this.recommendedImplementationOrder = new ArrayList<>();
        }
        this.recommendedImplementationOrder.add(step);
    }

    public List<String> getFilesToModify() {
        return filesToModify;
    }

    public void setFilesToModify(List<String> filesToModify) {
        this.filesToModify = filesToModify;
    }

    public void addFileToModify(String file) {
        if (this.filesToModify == null) {
            this.filesToModify = new ArrayList<>();
        }
        this.filesToModify.add(file);
    }

    public List<String> getFilesToReview() {
        return filesToReview;
    }

    public void setFilesToReview(List<String> filesToReview) {
        this.filesToReview = filesToReview;
    }

    public void addFileToReview(String file) {
        if (this.filesToReview == null) {
            this.filesToReview = new ArrayList<>();
        }
        this.filesToReview.add(file);
    }

    public List<String> getComponentsAffected() {
        return componentsAffected;
    }

    public void setComponentsAffected(List<String> componentsAffected) {
        this.componentsAffected = componentsAffected;
    }

    public void addComponentAffected(String component) {
        if (this.componentsAffected == null) {
            this.componentsAffected = new ArrayList<>();
        }
        this.componentsAffected.add(component);
    }

    public List<String> getDependenciesInvolved() {
        return dependenciesInvolved;
    }

    public void setDependenciesInvolved(List<String> dependenciesInvolved) {
        this.dependenciesInvolved = dependenciesInvolved;
    }

    public void addDependencyInvolved(String dependency) {
        if (this.dependenciesInvolved == null) {
            this.dependenciesInvolved = new ArrayList<>();
        }
        this.dependenciesInvolved.add(dependency);
    }

    public List<String> getSuggestedValidationSteps() {
        return suggestedValidationSteps;
    }

    public void setSuggestedValidationSteps(List<String> suggestedValidationSteps) {
        this.suggestedValidationSteps = suggestedValidationSteps;
    }

    public void addValidationStep(String step) {
        if (this.suggestedValidationSteps == null) {
            this.suggestedValidationSteps = new ArrayList<>();
        }
        this.suggestedValidationSteps.add(step);
    }

    public String getSuggestedTestingScope() {
        return suggestedTestingScope;
    }

    public void setSuggestedTestingScope(String suggestedTestingScope) {
        this.suggestedTestingScope = suggestedTestingScope;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public void addRisk(String risk) {
        if (this.risks == null) {
            this.risks = new ArrayList<>();
        }
        this.risks.add(risk);
    }

    public List<String> getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(List<String> assumptions) {
        this.assumptions = assumptions;
    }

    public void addAssumption(String assumption) {
        if (this.assumptions == null) {
            this.assumptions = new ArrayList<>();
        }
        this.assumptions.add(assumption);
    }
}