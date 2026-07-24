package com.projectiq.mcp.validation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * The complete validation report produced by the Intelligent Validation Pipeline.
 * Contains the overall validation status, all findings, blocking issues, warnings,
 * repository health summary, risk summary, readiness score, and recommended actions.
 *
 * <p>All collections maintain stable ordering. No duplicate findings are produced.
 * This report is fully deterministic.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "overallStatus",
        "passedValidations",
        "failedValidations",
        "warnings",
        "blockingIssues",
        "findings",
        "repositoryHealthSummary",
        "riskSummary",
        "readinessScore",
        "readinessLabel",
        "recommendedActions",
        "errors"
})
public class ValidationReport {

    private String overallStatus;
    private int passedValidations;
    private int failedValidations;
    private int warnings;
    private int blockingIssues;
    private List<ValidationFinding> findings;
    private RepositoryHealthSummary repositoryHealthSummary;
    private RiskSummary riskSummary;
    private int readinessScore;
    private String readinessLabel;
    private List<String> recommendedActions;
    private List<String> errors;

    public ValidationReport() {
        this.findings = new ArrayList<>();
        this.recommendedActions = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public int getPassedValidations() {
        return passedValidations;
    }

    public void setPassedValidations(int passedValidations) {
        this.passedValidations = passedValidations;
    }

    public int getFailedValidations() {
        return failedValidations;
    }

    public void setFailedValidations(int failedValidations) {
        this.failedValidations = failedValidations;
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public int getBlockingIssues() {
        return blockingIssues;
    }

    public void setBlockingIssues(int blockingIssues) {
        this.blockingIssues = blockingIssues;
    }

    public List<ValidationFinding> getFindings() {
        return findings;
    }

    public void setFindings(List<ValidationFinding> findings) {
        this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
    }

    public void addFinding(ValidationFinding finding) {
        this.findings.add(finding);
    }

    public RepositoryHealthSummary getRepositoryHealthSummary() {
        return repositoryHealthSummary;
    }

    public void setRepositoryHealthSummary(RepositoryHealthSummary repositoryHealthSummary) {
        this.repositoryHealthSummary = repositoryHealthSummary;
    }

    public RiskSummary getRiskSummary() {
        return riskSummary;
    }

    public void setRiskSummary(RiskSummary riskSummary) {
        this.riskSummary = riskSummary;
    }

    public int getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(int readinessScore) {
        this.readinessScore = readinessScore;
    }

    public String getReadinessLabel() {
        return readinessLabel;
    }

    public void setReadinessLabel(String readinessLabel) {
        this.readinessLabel = readinessLabel;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions != null ? new ArrayList<>(recommendedActions) : new ArrayList<>();
    }

    public void addRecommendedAction(String action) {
        this.recommendedActions.add(action);
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    // ========== Nested DTOs ==========

    /**
     * Summary of repository health metrics.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RepositoryHealthSummary {
        private int healthScore;
        private String maintainabilityRating;
        private String complexityRating;
        private String testingMaturity;
        private String dependencyHealth;
        private String architectureConsistency;

        public RepositoryHealthSummary() {
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

        public String getTestingMaturity() {
            return testingMaturity;
        }

        public void setTestingMaturity(String testingMaturity) {
            this.testingMaturity = testingMaturity;
        }

        public String getDependencyHealth() {
            return dependencyHealth;
        }

        public void setDependencyHealth(String dependencyHealth) {
            this.dependencyHealth = dependencyHealth;
        }

        public String getArchitectureConsistency() {
            return architectureConsistency;
        }

        public void setArchitectureConsistency(String architectureConsistency) {
            this.architectureConsistency = architectureConsistency;
        }
    }

    /**
     * Summary of risks identified during validation.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskSummary {
        private int totalRisks;
        private int criticalRisks;
        private int highRisks;
        private int mediumRisks;
        private List<String> topRisks;

        public RiskSummary() {
            this.topRisks = new ArrayList<>();
        }

        public int getTotalRisks() {
            return totalRisks;
        }

        public void setTotalRisks(int totalRisks) {
            this.totalRisks = totalRisks;
        }

        public int getCriticalRisks() {
            return criticalRisks;
        }

        public void setCriticalRisks(int criticalRisks) {
            this.criticalRisks = criticalRisks;
        }

        public int getHighRisks() {
            return highRisks;
        }

        public void setHighRisks(int highRisks) {
            this.highRisks = highRisks;
        }

        public int getMediumRisks() {
            return mediumRisks;
        }

        public void setMediumRisks(int mediumRisks) {
            this.mediumRisks = mediumRisks;
        }

        public List<String> getTopRisks() {
            return topRisks;
        }

        public void setTopRisks(List<String> topRisks) {
            this.topRisks = topRisks != null ? new ArrayList<>(topRisks) : new ArrayList<>();
        }
    }
}