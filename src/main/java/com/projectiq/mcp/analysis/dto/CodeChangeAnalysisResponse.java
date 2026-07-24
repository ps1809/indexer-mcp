package com.projectiq.mcp.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the complete result of analyzing a proposed code change.
 * Contains the proposed change summary, impacted files, classes, methods,
 * REST APIs, dependency changes, testing recommendations, risk assessment,
 * and suggested implementation order.
 *
 * <p>This response is deterministic and does not include any generated code
 * or repository modification. It is designed to guide AI coding agents in
 * understanding the complete scope of a requested modification before
 * implementation begins.</p>
 */
public class CodeChangeAnalysisResponse {

    private String proposedChangeSummary;
    private List<String> impactedFiles;
    private List<String> impactedClasses;
    private List<String> impactedMethods;
    private List<String> impactedRestApis;
    private List<String> dependencyChanges;
    private List<String> testingRecommendations;
    private List<String> riskAssessment;
    private List<String> suggestedImplementationOrder;

    public CodeChangeAnalysisResponse() {
        this.impactedFiles = new ArrayList<>();
        this.impactedClasses = new ArrayList<>();
        this.impactedMethods = new ArrayList<>();
        this.impactedRestApis = new ArrayList<>();
        this.dependencyChanges = new ArrayList<>();
        this.testingRecommendations = new ArrayList<>();
        this.riskAssessment = new ArrayList<>();
        this.suggestedImplementationOrder = new ArrayList<>();
    }

    public String getProposedChangeSummary() {
        return proposedChangeSummary;
    }

    public void setProposedChangeSummary(String proposedChangeSummary) {
        this.proposedChangeSummary = proposedChangeSummary;
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
        this.impactedFiles.add(file);
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
        this.impactedClasses.add(clazz);
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
        this.impactedMethods.add(method);
    }

    public List<String> getImpactedRestApis() {
        return impactedRestApis;
    }

    public void setImpactedRestApis(List<String> impactedRestApis) {
        this.impactedRestApis = impactedRestApis;
    }

    public void addImpactedRestApi(String api) {
        if (this.impactedRestApis == null) {
            this.impactedRestApis = new ArrayList<>();
        }
        this.impactedRestApis.add(api);
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
        this.dependencyChanges.add(change);
    }

    public List<String> getTestingRecommendations() {
        return testingRecommendations;
    }

    public void setTestingRecommendations(List<String> testingRecommendations) {
        this.testingRecommendations = testingRecommendations;
    }

    public void addTestingRecommendation(String recommendation) {
        if (this.testingRecommendations == null) {
            this.testingRecommendations = new ArrayList<>();
        }
        this.testingRecommendations.add(recommendation);
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
        this.riskAssessment.add(risk);
    }

    public List<String> getSuggestedImplementationOrder() {
        return suggestedImplementationOrder;
    }

    public void setSuggestedImplementationOrder(List<String> suggestedImplementationOrder) {
        this.suggestedImplementationOrder = suggestedImplementationOrder;
    }

    public void addImplementationStep(String step) {
        if (this.suggestedImplementationOrder == null) {
            this.suggestedImplementationOrder = new ArrayList<>();
        }
        this.suggestedImplementationOrder.add(step);
    }
}