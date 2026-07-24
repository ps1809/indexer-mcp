package com.projectiq.mcp.analysis.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO representing a deterministic dependency change impact prediction.
 * Contains all information needed to evaluate the downstream impact of
 * introducing, removing, or modifying project dependencies.
 *
 * <p>This DTO is produced by {@link com.projectiq.mcp.analysis.service.DependencyChangePredictionService}
 * and is returned by the {@code predict_dependency_change} MCP tool.</p>
 */
public class DependencyChangePredictionResponse {

    private String proposedDependencyChange;
    private String dependencyName;
    private String changeType;
    private String predictionCategory;
    private String currentVersion;
    private String newVersion;
    private List<String> impactedModules;
    private List<String> impactedServices;
    private List<String> transitiveDependencyEffects;
    private List<String> compatibilityRisks;
    private List<String> buildRisks;
    private List<String> testingImpact;
    private List<String> migrationRecommendations;
    private List<String> suggestedValidationChecklist;
    private boolean circularDependencyDetected;
    private String migrationEffortEstimate;

    public DependencyChangePredictionResponse() {
        this.impactedModules = new ArrayList<>();
        this.impactedServices = new ArrayList<>();
        this.transitiveDependencyEffects = new ArrayList<>();
        this.compatibilityRisks = new ArrayList<>();
        this.buildRisks = new ArrayList<>();
        this.testingImpact = new ArrayList<>();
        this.migrationRecommendations = new ArrayList<>();
        this.suggestedValidationChecklist = new ArrayList<>();
        this.circularDependencyDetected = false;
    }

    public String getProposedDependencyChange() {
        return proposedDependencyChange;
    }

    public void setProposedDependencyChange(String proposedDependencyChange) {
        this.proposedDependencyChange = proposedDependencyChange;
    }

    public String getDependencyName() {
        return dependencyName;
    }

    public void setDependencyName(String dependencyName) {
        this.dependencyName = dependencyName;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getPredictionCategory() {
        return predictionCategory;
    }

    public void setPredictionCategory(String predictionCategory) {
        this.predictionCategory = predictionCategory;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getNewVersion() {
        return newVersion;
    }

    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }

    public List<String> getImpactedModules() {
        return impactedModules;
    }

    public void setImpactedModules(List<String> impactedModules) {
        this.impactedModules = impactedModules;
    }

    public List<String> getImpactedServices() {
        return impactedServices;
    }

    public void setImpactedServices(List<String> impactedServices) {
        this.impactedServices = impactedServices;
    }

    public List<String> getTransitiveDependencyEffects() {
        return transitiveDependencyEffects;
    }

    public void setTransitiveDependencyEffects(List<String> transitiveDependencyEffects) {
        this.transitiveDependencyEffects = transitiveDependencyEffects;
    }

    public List<String> getCompatibilityRisks() {
        return compatibilityRisks;
    }

    public void setCompatibilityRisks(List<String> compatibilityRisks) {
        this.compatibilityRisks = compatibilityRisks;
    }

    public List<String> getBuildRisks() {
        return buildRisks;
    }

    public void setBuildRisks(List<String> buildRisks) {
        this.buildRisks = buildRisks;
    }

    public List<String> getTestingImpact() {
        return testingImpact;
    }

    public void setTestingImpact(List<String> testingImpact) {
        this.testingImpact = testingImpact;
    }

    public List<String> getMigrationRecommendations() {
        return migrationRecommendations;
    }

    public void setMigrationRecommendations(List<String> migrationRecommendations) {
        this.migrationRecommendations = migrationRecommendations;
    }

    public List<String> getSuggestedValidationChecklist() {
        return suggestedValidationChecklist;
    }

    public void setSuggestedValidationChecklist(List<String> suggestedValidationChecklist) {
        this.suggestedValidationChecklist = suggestedValidationChecklist;
    }

    public boolean isCircularDependencyDetected() {
        return circularDependencyDetected;
    }

    public void setCircularDependencyDetected(boolean circularDependencyDetected) {
        this.circularDependencyDetected = circularDependencyDetected;
    }

    public String getMigrationEffortEstimate() {
        return migrationEffortEstimate;
    }

    public void setMigrationEffortEstimate(String migrationEffortEstimate) {
        this.migrationEffortEstimate = migrationEffortEstimate;
    }

    @Override
    public String toString() {
        return "DependencyChangePredictionResponse{" +
                "proposedDependencyChange='" + proposedDependencyChange + '\'' +
                ", dependencyName='" + dependencyName + '\'' +
                ", changeType='" + changeType + '\'' +
                ", predictionCategory='" + predictionCategory + '\'' +
                ", currentVersion='" + currentVersion + '\'' +
                ", newVersion='" + newVersion + '\'' +
                ", impactedModules=" + impactedModules +
                ", impactedServices=" + impactedServices +
                ", transitiveDependencyEffects=" + transitiveDependencyEffects +
                ", compatibilityRisks=" + compatibilityRisks +
                ", buildRisks=" + buildRisks +
                ", testingImpact=" + testingImpact +
                ", migrationRecommendations=" + migrationRecommendations +
                ", suggestedValidationChecklist=" + suggestedValidationChecklist +
                ", circularDependencyDetected=" + circularDependencyDetected +
                ", migrationEffortEstimate='" + migrationEffortEstimate + '\'' +
                '}';
    }
}