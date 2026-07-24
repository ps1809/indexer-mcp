package com.projectiq.mcp.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete result of analyzing a proposed refactoring task.
 * Contains original task information, refactoring type, affected components,
 * dependencies, execution order, validation checklist, recommended tests,
 * risks, and confidence level.
 *
 * <p>This response is deterministic, contains no duplicate entries, and
 * never recommends automatic code changes.</p>
 */
public class RefactoringAssistantResponse {

    private String originalTask;
    private String refactoringType;
    private List<String> affectedClasses;
    private List<String> affectedMethods;
    private List<String> affectedPackages;
    private List<String> dependenciesInvolved;
    private List<String> suggestedExecutionOrder;
    private List<String> validationChecklist;
    private List<String> recommendedTests;
    private List<String> risks;
    private String confidenceLevel;

    public RefactoringAssistantResponse() {
        this.affectedClasses = new ArrayList<>();
        this.affectedMethods = new ArrayList<>();
        this.affectedPackages = new ArrayList<>();
        this.dependenciesInvolved = new ArrayList<>();
        this.suggestedExecutionOrder = new ArrayList<>();
        this.validationChecklist = new ArrayList<>();
        this.recommendedTests = new ArrayList<>();
        this.risks = new ArrayList<>();
    }

    public String getOriginalTask() {
        return originalTask;
    }

    public void setOriginalTask(String originalTask) {
        this.originalTask = originalTask;
    }

    public String getRefactoringType() {
        return refactoringType;
    }

    public void setRefactoringType(String refactoringType) {
        this.refactoringType = refactoringType;
    }

    public List<String> getAffectedClasses() {
        return affectedClasses;
    }

    public void setAffectedClasses(List<String> affectedClasses) {
        this.affectedClasses = affectedClasses;
    }

    public void addAffectedClass(String affectedClass) {
        if (this.affectedClasses == null) {
            this.affectedClasses = new ArrayList<>();
        }
        if (!this.affectedClasses.contains(affectedClass)) {
            this.affectedClasses.add(affectedClass);
        }
    }

    public List<String> getAffectedMethods() {
        return affectedMethods;
    }

    public void setAffectedMethods(List<String> affectedMethods) {
        this.affectedMethods = affectedMethods;
    }

    public void addAffectedMethod(String affectedMethod) {
        if (this.affectedMethods == null) {
            this.affectedMethods = new ArrayList<>();
        }
        if (!this.affectedMethods.contains(affectedMethod)) {
            this.affectedMethods.add(affectedMethod);
        }
    }

    public List<String> getAffectedPackages() {
        return affectedPackages;
    }

    public void setAffectedPackages(List<String> affectedPackages) {
        this.affectedPackages = affectedPackages;
    }

    public void addAffectedPackage(String affectedPackage) {
        if (this.affectedPackages == null) {
            this.affectedPackages = new ArrayList<>();
        }
        if (!this.affectedPackages.contains(affectedPackage)) {
            this.affectedPackages.add(affectedPackage);
        }
    }

    public List<String> getDependenciesInvolved() {
        return dependenciesInvolved;
    }

    public void setDependenciesInvolved(List<String> dependenciesInvolved) {
        this.dependenciesInvolved = dependenciesInvolved;
    }

    public void addDependency(String dependency) {
        if (this.dependenciesInvolved == null) {
            this.dependenciesInvolved = new ArrayList<>();
        }
        if (!this.dependenciesInvolved.contains(dependency)) {
            this.dependenciesInvolved.add(dependency);
        }
    }

    public List<String> getSuggestedExecutionOrder() {
        return suggestedExecutionOrder;
    }

    public void setSuggestedExecutionOrder(List<String> suggestedExecutionOrder) {
        this.suggestedExecutionOrder = suggestedExecutionOrder;
    }

    public void addExecutionStep(String step) {
        if (this.suggestedExecutionOrder == null) {
            this.suggestedExecutionOrder = new ArrayList<>();
        }
        if (!this.suggestedExecutionOrder.contains(step)) {
            this.suggestedExecutionOrder.add(step);
        }
    }

    public List<String> getValidationChecklist() {
        return validationChecklist;
    }

    public void setValidationChecklist(List<String> validationChecklist) {
        this.validationChecklist = validationChecklist;
    }

    public void addValidationItem(String item) {
        if (this.validationChecklist == null) {
            this.validationChecklist = new ArrayList<>();
        }
        if (!this.validationChecklist.contains(item)) {
            this.validationChecklist.add(item);
        }
    }

    public List<String> getRecommendedTests() {
        return recommendedTests;
    }

    public void setRecommendedTests(List<String> recommendedTests) {
        this.recommendedTests = recommendedTests;
    }

    public void addRecommendedTest(String test) {
        if (this.recommendedTests == null) {
            this.recommendedTests = new ArrayList<>();
        }
        if (!this.recommendedTests.contains(test)) {
            this.recommendedTests.add(test);
        }
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
        if (!this.risks.contains(risk)) {
            this.risks.add(risk);
        }
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
}