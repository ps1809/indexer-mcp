package com.projectiq.mcp.readiness.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * The complete readiness assessment report produced by the Intelligent Execution
 * Readiness Assessment. Contains the overall readiness level, readiness score,
 * blocking issues, warnings, passed checks, repository summary, risk overview,
 * final implementation recommendation, and next actions.
 *
 * <p>All collections maintain stable ordering. No duplicate findings are produced.
 * This report is fully deterministic.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "workflowName",
        "workflowType",
        "overallReadinessLevel",
        "readinessScore",
        "blockingIssues",
        "warnings",
        "passedChecks",
        "categoryAssessments",
        "repositorySummary",
        "riskOverview",
        "finalImplementationRecommendation",
        "nextActions",
        "assessmentSummary",
        "errors"
})
public class ReadinessReport {

    private String workflowName;
    private String workflowType;
    private ReadinessLevel overallReadinessLevel;
    private int readinessScore;
    private List<String> blockingIssues;
    private List<String> warnings;
    private List<String> passedChecks;
    private List<CategoryAssessment> categoryAssessments;
    private RepositorySummary repositorySummary;
    private RiskOverview riskOverview;
    private String finalImplementationRecommendation;
    private List<String> nextActions;
    private AssessmentSummary assessmentSummary;
    private List<String> errors;

    public ReadinessReport() {
        this.blockingIssues = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.passedChecks = new ArrayList<>();
        this.categoryAssessments = new ArrayList<>();
        this.nextActions = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    public ReadinessLevel getOverallReadinessLevel() {
        return overallReadinessLevel;
    }

    public void setOverallReadinessLevel(ReadinessLevel overallReadinessLevel) {
        this.overallReadinessLevel = overallReadinessLevel;
    }

    public int getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(int readinessScore) {
        this.readinessScore = readinessScore;
    }

    public List<String> getBlockingIssues() {
        return blockingIssues;
    }

    public void setBlockingIssues(List<String> blockingIssues) {
        this.blockingIssues = blockingIssues != null ? new ArrayList<>(blockingIssues) : new ArrayList<>();
    }

    public void addBlockingIssue(String issue) {
        this.blockingIssues.add(issue);
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? new ArrayList<>(warnings) : new ArrayList<>();
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public List<String> getPassedChecks() {
        return passedChecks;
    }

    public void setPassedChecks(List<String> passedChecks) {
        this.passedChecks = passedChecks != null ? new ArrayList<>(passedChecks) : new ArrayList<>();
    }

    public void addPassedCheck(String check) {
        this.passedChecks.add(check);
    }

    public List<CategoryAssessment> getCategoryAssessments() {
        return categoryAssessments;
    }

    public void setCategoryAssessments(List<CategoryAssessment> categoryAssessments) {
        this.categoryAssessments = categoryAssessments != null ? new ArrayList<>(categoryAssessments) : new ArrayList<>();
    }

    public void addCategoryAssessment(CategoryAssessment assessment) {
        this.categoryAssessments.add(assessment);
    }

    public RepositorySummary getRepositorySummary() {
        return repositorySummary;
    }

    public void setRepositorySummary(RepositorySummary repositorySummary) {
        this.repositorySummary = repositorySummary;
    }

    public RiskOverview getRiskOverview() {
        return riskOverview;
    }

    public void setRiskOverview(RiskOverview riskOverview) {
        this.riskOverview = riskOverview;
    }

    public String getFinalImplementationRecommendation() {
        return finalImplementationRecommendation;
    }

    public void setFinalImplementationRecommendation(String finalImplementationRecommendation) {
        this.finalImplementationRecommendation = finalImplementationRecommendation;
    }

    public List<String> getNextActions() {
        return nextActions;
    }

    public void setNextActions(List<String> nextActions) {
        this.nextActions = nextActions != null ? new ArrayList<>(nextActions) : new ArrayList<>();
    }

    public void addNextAction(String action) {
        this.nextActions.add(action);
    }

    public AssessmentSummary getAssessmentSummary() {
        return assessmentSummary;
    }

    public void setAssessmentSummary(AssessmentSummary assessmentSummary) {
        this.assessmentSummary = assessmentSummary;
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
     * Assessment of a single readiness category.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CategoryAssessment {
        private AssessmentCategory category;
        private String status;
        private int score;
        private List<String> findings;
        private List<String> recommendations;

        public CategoryAssessment() {
            this.findings = new ArrayList<>();
            this.recommendations = new ArrayList<>();
        }

        public AssessmentCategory getCategory() {
            return category;
        }

        public void setCategory(AssessmentCategory category) {
            this.category = category;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public List<String> getFindings() {
            return findings;
        }

        public void setFindings(List<String> findings) {
            this.findings = findings != null ? new ArrayList<>(findings) : new ArrayList<>();
        }

        public void addFinding(String finding) {
            this.findings.add(finding);
        }

        public List<String> getRecommendations() {
            return recommendations;
        }

        public void setRecommendations(List<String> recommendations) {
            this.recommendations = recommendations != null ? new ArrayList<>(recommendations) : new ArrayList<>();
        }

        public void addRecommendation(String recommendation) {
            this.recommendations.add(recommendation);
        }
    }

    /**
     * Summary of repository intelligence.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RepositorySummary {
        private String repositoryName;
        private int healthScore;
        private String maintainabilityRating;
        private String testingMaturity;
        private String architectureConsistency;
        private String dependencyHealth;

        public RepositorySummary() {
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
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

        public String getTestingMaturity() {
            return testingMaturity;
        }

        public void setTestingMaturity(String testingMaturity) {
            this.testingMaturity = testingMaturity;
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
    }

    /**
     * Overview of risks identified during assessment.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RiskOverview {
        private int totalRisks;
        private int criticalRisks;
        private int highRisks;
        private int mediumRisks;
        private List<String> topRisks;

        public RiskOverview() {
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

        public void addTopRisk(String risk) {
            this.topRisks.add(risk);
        }
    }

    /**
     * Summary of the entire readiness assessment.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AssessmentSummary {
        private int totalCategories;
        private int passedCategories;
        private int warningCategories;
        private int failedCategories;
        private int totalBlockingIssues;
        private int totalWarnings;
        private int totalPassedChecks;
        private String decision;

        public AssessmentSummary() {
        }

        public int getTotalCategories() {
            return totalCategories;
        }

        public void setTotalCategories(int totalCategories) {
            this.totalCategories = totalCategories;
        }

        public int getPassedCategories() {
            return passedCategories;
        }

        public void setPassedCategories(int passedCategories) {
            this.passedCategories = passedCategories;
        }

        public int getWarningCategories() {
            return warningCategories;
        }

        public void setWarningCategories(int warningCategories) {
            this.warningCategories = warningCategories;
        }

        public int getFailedCategories() {
            return failedCategories;
        }

        public void setFailedCategories(int failedCategories) {
            this.failedCategories = failedCategories;
        }

        public int getTotalBlockingIssues() {
            return totalBlockingIssues;
        }

        public void setTotalBlockingIssues(int totalBlockingIssues) {
            this.totalBlockingIssues = totalBlockingIssues;
        }

        public int getTotalWarnings() {
            return totalWarnings;
        }

        public void setTotalWarnings(int totalWarnings) {
            this.totalWarnings = totalWarnings;
        }

        public int getTotalPassedChecks() {
            return totalPassedChecks;
        }

        public void setTotalPassedChecks(int totalPassedChecks) {
            this.totalPassedChecks = totalPassedChecks;
        }

        public String getDecision() {
            return decision;
        }

        public void setDecision(String decision) {
            this.decision = decision;
        }
    }
}