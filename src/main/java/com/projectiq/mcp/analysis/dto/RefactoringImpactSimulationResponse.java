package com.projectiq.mcp.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete result of simulating a proposed refactoring operation's
 * repository-wide impact. Contains the refactoring summary, impacted components,
 * broken references, dependency changes, architectural effects, testing impact,
 * risk assessment, and suggested implementation sequence.
 *
 * <p>This response is deterministic, contains no duplicate entries, and
 * never modifies repository code or generates automatic changes.</p>
 */
public class RefactoringImpactSimulationResponse {

    private String refactoringSummary;
    private String refactoringType;
    private String targetEntity;
    private String sourceContext;
    private List<String> impactedFiles;
    private List<String> impactedClasses;
    private List<String> impactedMethods;
    private List<String> brokenReferences;
    private List<String> dependencyChanges;
    private List<String> architecturalEffects;
    private List<String> testingImpact;
    private List<String> riskAssessment;
    private List<String> suggestedImplementationSequence;
    private String estimatedEffort;

    public RefactoringImpactSimulationResponse() {
        this.impactedFiles = new ArrayList<>();
        this.impactedClasses = new ArrayList<>();
        this.impactedMethods = new ArrayList<>();
        this.brokenReferences = new ArrayList<>();
        this.dependencyChanges = new ArrayList<>();
        this.architecturalEffects = new ArrayList<>();
        this.testingImpact = new ArrayList<>();
        this.riskAssessment = new ArrayList<>();
        this.suggestedImplementationSequence = new ArrayList<>();
    }

    public String getRefactoringSummary() {
        return refactoringSummary;
    }

    public void setRefactoringSummary(String refactoringSummary) {
        this.refactoringSummary = refactoringSummary;
    }

    public String getRefactoringType() {
        return refactoringType;
    }

    public void setRefactoringType(String refactoringType) {
        this.refactoringType = refactoringType;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getSourceContext() {
        return sourceContext;
    }

    public void setSourceContext(String sourceContext) {
        this.sourceContext = sourceContext;
    }

    public List<String> getImpactedFiles() {
        return impactedFiles;
    }

    public void setImpactedFiles(List<String> impactedFiles) {
        this.impactedFiles = impactedFiles;
    }

    public void addImpactedFile(String file) {
        if (this.impactedFiles == null) {
            this.impactedFiles = new ArrayList<>();
        }
        if (!this.impactedFiles.contains(file)) {
            this.impactedFiles.add(file);
        }
    }

    public List<String> getImpactedClasses() {
        return impactedClasses;
    }

    public void setImpactedClasses(List<String> impactedClasses) {
        this.impactedClasses = impactedClasses;
    }

    public void addImpactedClass(String clazz) {
        if (this.impactedClasses == null) {
            this.impactedClasses = new ArrayList<>();
        }
        if (!this.impactedClasses.contains(clazz)) {
            this.impactedClasses.add(clazz);
        }
    }

    public List<String> getImpactedMethods() {
        return impactedMethods;
    }

    public void setImpactedMethods(List<String> impactedMethods) {
        this.impactedMethods = impactedMethods;
    }

    public void addImpactedMethod(String method) {
        if (this.impactedMethods == null) {
            this.impactedMethods = new ArrayList<>();
        }
        if (!this.impactedMethods.contains(method)) {
            this.impactedMethods.add(method);
        }
    }

    public List<String> getBrokenReferences() {
        return brokenReferences;
    }

    public void setBrokenReferences(List<String> brokenReferences) {
        this.brokenReferences = brokenReferences;
    }

    public void addBrokenReference(String reference) {
        if (this.brokenReferences == null) {
            this.brokenReferences = new ArrayList<>();
        }
        if (!this.brokenReferences.contains(reference)) {
            this.brokenReferences.add(reference);
        }
    }

    public List<String> getDependencyChanges() {
        return dependencyChanges;
    }

    public void setDependencyChanges(List<String> dependencyChanges) {
        this.dependencyChanges = dependencyChanges;
    }

    public void addDependencyChange(String change) {
        if (this.dependencyChanges == null) {
            this.dependencyChanges = new ArrayList<>();
        }
        if (!this.dependencyChanges.contains(change)) {
            this.dependencyChanges.add(change);
        }
    }

    public List<String> getArchitecturalEffects() {
        return architecturalEffects;
    }

    public void setArchitecturalEffects(List<String> architecturalEffects) {
        this.architecturalEffects = architecturalEffects;
    }

    public void addArchitecturalEffect(String effect) {
        if (this.architecturalEffects == null) {
            this.architecturalEffects = new ArrayList<>();
        }
        if (!this.architecturalEffects.contains(effect)) {
            this.architecturalEffects.add(effect);
        }
    }

    public List<String> getTestingImpact() {
        return testingImpact;
    }

    public void setTestingImpact(List<String> testingImpact) {
        this.testingImpact = testingImpact;
    }

    public void addTestingImpact(String impact) {
        if (this.testingImpact == null) {
            this.testingImpact = new ArrayList<>();
        }
        if (!this.testingImpact.contains(impact)) {
            this.testingImpact.add(impact);
        }
    }

    public List<String> getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(List<String> riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    public void addRisk(String risk) {
        if (this.riskAssessment == null) {
            this.riskAssessment = new ArrayList<>();
        }
        if (!this.riskAssessment.contains(risk)) {
            this.riskAssessment.add(risk);
        }
    }

    public List<String> getSuggestedImplementationSequence() {
        return suggestedImplementationSequence;
    }

    public void setSuggestedImplementationSequence(List<String> suggestedImplementationSequence) {
        this.suggestedImplementationSequence = suggestedImplementationSequence;
    }

    public void addImplementationStep(String step) {
        if (this.suggestedImplementationSequence == null) {
            this.suggestedImplementationSequence = new ArrayList<>();
        }
        if (!this.suggestedImplementationSequence.contains(step)) {
            this.suggestedImplementationSequence.add(step);
        }
    }

    public String getEstimatedEffort() {
        return estimatedEffort;
    }

    public void setEstimatedEffort(String estimatedEffort) {
        this.estimatedEffort = estimatedEffort;
    }
}