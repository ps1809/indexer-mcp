package com.projectiq.mcp.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * The complete recommendation report produced by the Intelligent Recommendation Engine.
 * Contains an executive summary, prioritized recommendations, implementation advice,
 * testing recommendations, architectural guidance, repository best practices,
 * risk mitigation suggestions, and a confidence score.
 *
 * <p>All collections maintain stable ordering. No duplicate recommendations are produced.
 * This report is fully deterministic.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "workflowName",
        "workflowType",
        "executiveSummary",
        "prioritizedRecommendations",
        "implementationAdvice",
        "testingRecommendations",
        "architecturalGuidance",
        "repositoryBestPractices",
        "riskMitigationSuggestions",
        "confidenceScore",
        "recommendationSummary",
        "errors"
})
public class RecommendationReport {

    private String workflowName;
    private String workflowType;
    private String executiveSummary;
    private List<Recommendation> prioritizedRecommendations;
    private List<String> implementationAdvice;
    private List<String> testingRecommendations;
    private List<String> architecturalGuidance;
    private List<String> repositoryBestPractices;
    private List<String> riskMitigationSuggestions;
    private int confidenceScore;
    private ReportSummary recommendationSummary;
    private List<String> errors;

    public RecommendationReport() {
        this.prioritizedRecommendations = new ArrayList<>();
        this.implementationAdvice = new ArrayList<>();
        this.testingRecommendations = new ArrayList<>();
        this.architecturalGuidance = new ArrayList<>();
        this.repositoryBestPractices = new ArrayList<>();
        this.riskMitigationSuggestions = new ArrayList<>();
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

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(String executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public List<Recommendation> getPrioritizedRecommendations() {
        return prioritizedRecommendations;
    }

    public void setPrioritizedRecommendations(List<Recommendation> prioritizedRecommendations) {
        this.prioritizedRecommendations = prioritizedRecommendations != null
                ? new ArrayList<>(prioritizedRecommendations) : new ArrayList<>();
    }

    public void addRecommendation(Recommendation recommendation) {
        this.prioritizedRecommendations.add(recommendation);
    }

    public List<String> getImplementationAdvice() {
        return implementationAdvice;
    }

    public void setImplementationAdvice(List<String> implementationAdvice) {
        this.implementationAdvice = implementationAdvice != null
                ? new ArrayList<>(implementationAdvice) : new ArrayList<>();
    }

    public void addImplementationAdvice(String advice) {
        this.implementationAdvice.add(advice);
    }

    public List<String> getTestingRecommendations() {
        return testingRecommendations;
    }

    public void setTestingRecommendations(List<String> testingRecommendations) {
        this.testingRecommendations = testingRecommendations != null
                ? new ArrayList<>(testingRecommendations) : new ArrayList<>();
    }

    public void addTestingRecommendation(String recommendation) {
        this.testingRecommendations.add(recommendation);
    }

    public List<String> getArchitecturalGuidance() {
        return architecturalGuidance;
    }

    public void setArchitecturalGuidance(List<String> architecturalGuidance) {
        this.architecturalGuidance = architecturalGuidance != null
                ? new ArrayList<>(architecturalGuidance) : new ArrayList<>();
    }

    public void addArchitecturalGuidance(String guidance) {
        this.architecturalGuidance.add(guidance);
    }

    public List<String> getRepositoryBestPractices() {
        return repositoryBestPractices;
    }

    public void setRepositoryBestPractices(List<String> repositoryBestPractices) {
        this.repositoryBestPractices = repositoryBestPractices != null
                ? new ArrayList<>(repositoryBestPractices) : new ArrayList<>();
    }

    public void addRepositoryBestPractice(String practice) {
        this.repositoryBestPractices.add(practice);
    }

    public List<String> getRiskMitigationSuggestions() {
        return riskMitigationSuggestions;
    }

    public void setRiskMitigationSuggestions(List<String> riskMitigationSuggestions) {
        this.riskMitigationSuggestions = riskMitigationSuggestions != null
                ? new ArrayList<>(riskMitigationSuggestions) : new ArrayList<>();
    }

    public void addRiskMitigationSuggestion(String suggestion) {
        this.riskMitigationSuggestions.add(suggestion);
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(int confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public ReportSummary getRecommendationSummary() {
        return recommendationSummary;
    }

    public void setRecommendationSummary(ReportSummary recommendationSummary) {
        this.recommendationSummary = recommendationSummary;
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

    // ========== Nested DTO ==========

    /**
     * Summary of the recommendation report.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ReportSummary {
        private int totalRecommendations;
        private int criticalCount;
        private int highCount;
        private int mediumCount;
        private int lowCount;
        private int implementationCount;
        private int architectureCount;
        private int testingCount;
        private int riskCount;

        public ReportSummary() {
        }

        public int getTotalRecommendations() {
            return totalRecommendations;
        }

        public void setTotalRecommendations(int totalRecommendations) {
            this.totalRecommendations = totalRecommendations;
        }

        public int getCriticalCount() {
            return criticalCount;
        }

        public void setCriticalCount(int criticalCount) {
            this.criticalCount = criticalCount;
        }

        public int getHighCount() {
            return highCount;
        }

        public void setHighCount(int highCount) {
            this.highCount = highCount;
        }

        public int getMediumCount() {
            return mediumCount;
        }

        public void setMediumCount(int mediumCount) {
            this.mediumCount = mediumCount;
        }

        public int getLowCount() {
            return lowCount;
        }

        public void setLowCount(int lowCount) {
            this.lowCount = lowCount;
        }

        public int getImplementationCount() {
            return implementationCount;
        }

        public void setImplementationCount(int implementationCount) {
            this.implementationCount = implementationCount;
        }

        public int getArchitectureCount() {
            return architectureCount;
        }

        public void setArchitectureCount(int architectureCount) {
            this.architectureCount = architectureCount;
        }

        public int getTestingCount() {
            return testingCount;
        }

        public void setTestingCount(int testingCount) {
            this.testingCount = testingCount;
        }

        public int getRiskCount() {
            return riskCount;
        }

        public void setRiskCount(int riskCount) {
            this.riskCount = riskCount;
        }
    }
}