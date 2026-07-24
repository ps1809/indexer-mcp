package com.projectiq.mcp.strategy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO containing a deterministic development strategy recommendation report.
 * Evaluates multiple implementation approaches for a requested feature or change and
 * produces a comparative analysis with a recommended strategy based on complexity,
 * repository impact, dependency impact, testing effort, architectural consistency,
 * maintainability, technical risk, and long-term sustainability.
 *
 * <p>All collections use stable ordering. No duplicate entries are produced.
 * This DTO is serialized to JSON for the MCP tool response.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DevelopmentStrategyResponse {

    private String requestDescription;
    private String repositoryName;
    private List<StrategyEvaluation> strategies;
    private String recommendedStrategy;
    private String decisionRationale;
    private StrategyComparison comparison;
    private String warning;

    public DevelopmentStrategyResponse() {
        this.strategies = new ArrayList<>();
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public List<StrategyEvaluation> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<StrategyEvaluation> strategies) {
        this.strategies = strategies != null ? new ArrayList<>(strategies) : new ArrayList<>();
    }

    public String getRecommendedStrategy() {
        return recommendedStrategy;
    }

    public void setRecommendedStrategy(String recommendedStrategy) {
        this.recommendedStrategy = recommendedStrategy;
    }

    public String getDecisionRationale() {
        return decisionRationale;
    }

    public void setDecisionRationale(String decisionRationale) {
        this.decisionRationale = decisionRationale;
    }

    public StrategyComparison getComparison() {
        return comparison;
    }

    public void setComparison(StrategyComparison comparison) {
        this.comparison = comparison;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    // --- Strategy Category Enum ---

    /**
     * Strategy categories representing different implementation approaches.
     */
    public enum StrategyCategory {
        EXTEND_EXISTING_COMPONENT("Extend Existing Component"),
        CREATE_NEW_COMPONENT("Create New Component"),
        REFACTOR_THEN_IMPLEMENT("Refactor Then Implement"),
        MODULAR_IMPLEMENTATION("Modular Implementation"),
        INCREMENTAL_ENHANCEMENT("Incremental Enhancement"),
        CONFIGURATION_BASED_SOLUTION("Configuration-Based Solution"),
        SERVICE_LAYER_ENHANCEMENT("Service Layer Enhancement"),
        API_FIRST_IMPLEMENTATION("API-First Implementation");

        private final String displayName;

        StrategyCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // --- Strategy Evaluation DTO ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StrategyEvaluation {
        private String strategyName;
        private String description;
        private Integer complexityScore;
        private Integer repositoryImpactScore;
        private Integer dependencyImpactScore;
        private Integer testingEffortScore;
        private Integer architecturalConsistencyScore;
        private Integer maintainabilityScore;
        private Integer technicalRiskScore;
        private Integer sustainabilityScore;
        private Integer overallScore;
        private List<String> pros;
        private List<String> cons;
        private String estimatedEffort;
        private String riskAssessment;

        public StrategyEvaluation() {
            this.pros = new ArrayList<>();
            this.cons = new ArrayList<>();
        }

        public String getStrategyName() {
            return strategyName;
        }

        public void setStrategyName(String strategyName) {
            this.strategyName = strategyName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getComplexityScore() {
            return complexityScore;
        }

        public void setComplexityScore(Integer complexityScore) {
            this.complexityScore = complexityScore;
        }

        public Integer getRepositoryImpactScore() {
            return repositoryImpactScore;
        }

        public void setRepositoryImpactScore(Integer repositoryImpactScore) {
            this.repositoryImpactScore = repositoryImpactScore;
        }

        public Integer getDependencyImpactScore() {
            return dependencyImpactScore;
        }

        public void setDependencyImpactScore(Integer dependencyImpactScore) {
            this.dependencyImpactScore = dependencyImpactScore;
        }

        public Integer getTestingEffortScore() {
            return testingEffortScore;
        }

        public void setTestingEffortScore(Integer testingEffortScore) {
            this.testingEffortScore = testingEffortScore;
        }

        public Integer getArchitecturalConsistencyScore() {
            return architecturalConsistencyScore;
        }

        public void setArchitecturalConsistencyScore(Integer architecturalConsistencyScore) {
            this.architecturalConsistencyScore = architecturalConsistencyScore;
        }

        public Integer getMaintainabilityScore() {
            return maintainabilityScore;
        }

        public void setMaintainabilityScore(Integer maintainabilityScore) {
            this.maintainabilityScore = maintainabilityScore;
        }

        public Integer getTechnicalRiskScore() {
            return technicalRiskScore;
        }

        public void setTechnicalRiskScore(Integer technicalRiskScore) {
            this.technicalRiskScore = technicalRiskScore;
        }

        public Integer getSustainabilityScore() {
            return sustainabilityScore;
        }

        public void setSustainabilityScore(Integer sustainabilityScore) {
            this.sustainabilityScore = sustainabilityScore;
        }

        public Integer getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(Integer overallScore) {
            this.overallScore = overallScore;
        }

        public List<String> getPros() {
            return pros;
        }

        public void setPros(List<String> pros) {
            this.pros = pros != null ? new ArrayList<>(pros) : new ArrayList<>();
        }

        public List<String> getCons() {
            return cons;
        }

        public void setCons(List<String> cons) {
            this.cons = cons != null ? new ArrayList<>(cons) : new ArrayList<>();
        }

        public String getEstimatedEffort() {
            return estimatedEffort;
        }

        public void setEstimatedEffort(String estimatedEffort) {
            this.estimatedEffort = estimatedEffort;
        }

        public String getRiskAssessment() {
            return riskAssessment;
        }

        public void setRiskAssessment(String riskAssessment) {
            this.riskAssessment = riskAssessment;
        }
    }

    // --- Strategy Comparison DTO ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StrategyComparison {
        private String bestComplexity;
        private String bestRepositoryImpact;
        private String bestDependencyImpact;
        private String bestTestingEffort;
        private String bestArchitecturalConsistency;
        private String bestMaintainability;
        private String lowestRisk;
        private String bestSustainability;
        private String overallBestScore;

        public StrategyComparison() {
        }

        public String getBestComplexity() {
            return bestComplexity;
        }

        public void setBestComplexity(String bestComplexity) {
            this.bestComplexity = bestComplexity;
        }

        public String getBestRepositoryImpact() {
            return bestRepositoryImpact;
        }

        public void setBestRepositoryImpact(String bestRepositoryImpact) {
            this.bestRepositoryImpact = bestRepositoryImpact;
        }

        public String getBestDependencyImpact() {
            return bestDependencyImpact;
        }

        public void setBestDependencyImpact(String bestDependencyImpact) {
            this.bestDependencyImpact = bestDependencyImpact;
        }

        public String getBestTestingEffort() {
            return bestTestingEffort;
        }

        public void setBestTestingEffort(String bestTestingEffort) {
            this.bestTestingEffort = bestTestingEffort;
        }

        public String getBestArchitecturalConsistency() {
            return bestArchitecturalConsistency;
        }

        public void setBestArchitecturalConsistency(String bestArchitecturalConsistency) {
            this.bestArchitecturalConsistency = bestArchitecturalConsistency;
        }

        public String getBestMaintainability() {
            return bestMaintainability;
        }

        public void setBestMaintainability(String bestMaintainability) {
            this.bestMaintainability = bestMaintainability;
        }

        public String getLowestRisk() {
            return lowestRisk;
        }

        public void setLowestRisk(String lowestRisk) {
            this.lowestRisk = lowestRisk;
        }

        public String getBestSustainability() {
            return bestSustainability;
        }

        public void setBestSustainability(String bestSustainability) {
            this.bestSustainability = bestSustainability;
        }

        public String getOverallBestScore() {
            return overallBestScore;
        }

        public void setOverallBestScore(String overallBestScore) {
            this.overallBestScore = overallBestScore;
        }
    }
}