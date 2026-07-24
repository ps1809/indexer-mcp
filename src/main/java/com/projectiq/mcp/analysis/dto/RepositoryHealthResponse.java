package com.projectiq.mcp.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO containing deterministic repository health analysis.
 * Provides an overall health assessment of a repository based on indexed metadata,
 * including maintainability, complexity, architecture consistency, testing maturity,
 * and documentation maturity.
 *
 * <p>All collections use stable ordering. No duplicate entries are produced.
 * This DTO is serialized to JSON for the MCP tool response.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepositoryHealthResponse {

    private String repositoryName;
    private String branch;
    private String repositoryOverview;
    private int healthScore;
    private String maintainabilityRating;
    private String complexityRating;
    private String architectureConsistency;
    private String dependencyHealth;
    private String testingMaturity;
    private String documentationMaturity;
    private String maintainabilitySummary;
    private List<String> strengths;
    private List<String> observations;
    private List<String> potentialRisks;
    private List<String> suggestedReviewAreas;
    private String confidenceLevel;

    public RepositoryHealthResponse() {
        this.strengths = new ArrayList<>();
        this.observations = new ArrayList<>();
        this.potentialRisks = new ArrayList<>();
        this.suggestedReviewAreas = new ArrayList<>();
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRepositoryOverview() {
        return repositoryOverview;
    }

    public void setRepositoryOverview(String repositoryOverview) {
        this.repositoryOverview = repositoryOverview;
    }

    public int getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(int healthScore) {
        this.healthScore = healthScore;
    }

    public String getMaintainabilityRating() {
        return maintainabilityRating;
    }

    public void setMaintainabilityRating(String maintainabilityRating) {
        this.maintainabilityRating = maintainabilityRating;
    }

    public String getComplexityRating() {
        return complexityRating;
    }

    public void setComplexityRating(String complexityRating) {
        this.complexityRating = complexityRating;
    }

    public String getArchitectureConsistency() {
        return architectureConsistency;
    }

    public void setArchitectureConsistency(String architectureConsistency) {
        this.architectureConsistency = architectureConsistency;
    }

    public String getDependencyHealth() {
        return dependencyHealth;
    }

    public void setDependencyHealth(String dependencyHealth) {
        this.dependencyHealth = dependencyHealth;
    }

    public String getTestingMaturity() {
        return testingMaturity;
    }

    public void setTestingMaturity(String testingMaturity) {
        this.testingMaturity = testingMaturity;
    }

    public String getDocumentationMaturity() {
        return documentationMaturity;
    }

    public void setDocumentationMaturity(String documentationMaturity) {
        this.documentationMaturity = documentationMaturity;
    }

    public String getMaintainabilitySummary() {
        return maintainabilitySummary;
    }

    public void setMaintainabilitySummary(String maintainabilitySummary) {
        this.maintainabilitySummary = maintainabilitySummary;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths != null ? new ArrayList<>(strengths) : new ArrayList<>();
    }

    public List<String> getObservations() {
        return observations;
    }

    public void setObservations(List<String> observations) {
        this.observations = observations != null ? new ArrayList<>(observations) : new ArrayList<>();
    }

    public List<String> getPotentialRisks() {
        return potentialRisks;
    }

    public void setPotentialRisks(List<String> potentialRisks) {
        this.potentialRisks = potentialRisks != null ? new ArrayList<>(potentialRisks) : new ArrayList<>();
    }

    public List<String> getSuggestedReviewAreas() {
        return suggestedReviewAreas;
    }

    public void setSuggestedReviewAreas(List<String> suggestedReviewAreas) {
        this.suggestedReviewAreas = suggestedReviewAreas != null ? new ArrayList<>(suggestedReviewAreas) : new ArrayList<>();
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
}