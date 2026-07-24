package com.projectiq.mcp.strategy.service;

import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse.StrategyEvaluation;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse.StrategyComparison;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DevelopmentStrategyService.
 */
class DevelopmentStrategyServiceTest {

    private final DevelopmentStrategyService service = new DevelopmentStrategyService();

    @Test
    void testEvaluateStrategies_NewFeature() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Implement a new user notification feature",
                "my-project");

        assertNotNull(response);
        assertEquals("Implement a new user notification feature", response.getRequestDescription());
        assertEquals("my-project", response.getRepositoryName());
        assertNotNull(response.getStrategies());
        assertEquals(8, response.getStrategies().size());
        assertNotNull(response.getRecommendedStrategy());
        assertNotNull(response.getDecisionRationale());
        assertNotNull(response.getComparison());
    }

    @Test
    void testEvaluateStrategies_RefactoringRequest() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Refactor the authentication module to improve security",
                "auth-service");

        assertNotNull(response);
        assertEquals(8, response.getStrategies().size());
        assertNotNull(response.getRecommendedStrategy());
        assertTrue(response.getDecisionRationale().contains("Recommended strategy"));
    }

    @Test
    void testEvaluateStrategies_ApiEnhancement() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Add a new REST API endpoint for user management",
                "api-gateway");

        assertNotNull(response);
        assertEquals(8, response.getStrategies().size());
        assertNotNull(response.getRecommendedStrategy());
    }

    @Test
    void testEvaluateStrategies_ConfigurationChange() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Add a configuration flag to enable dark mode",
                "web-app");

        assertNotNull(response);
        assertEquals(8, response.getStrategies().size());
        assertNotNull(response.getRecommendedStrategy());
    }

    @Test
    void testEvaluateStrategies_EmptyRequestDescription() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.evaluateStrategies("", "my-repo"));
        assertEquals("Request description is required", exception.getMessage());
    }

    @Test
    void testEvaluateStrategies_NullRequestDescription() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.evaluateStrategies(null, "my-repo"));
        assertEquals("Request description is required", exception.getMessage());
    }

    @Test
    void testEvaluateStrategies_EmptyRepositoryName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.evaluateStrategies("Add feature", ""));
        assertEquals("Repository name is required", exception.getMessage());
    }

    @Test
    void testEvaluateStrategies_NullRepositoryName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.evaluateStrategies("Add feature", null));
        assertEquals("Repository name is required", exception.getMessage());
    }

    @Test
    void testEvaluateStrategies_AllStrategiesHaveScores() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Implement a new dashboard widget",
                "dashboard-app");

        for (StrategyEvaluation eval : response.getStrategies()) {
            assertNotNull(eval.getStrategyName(), "Strategy name should not be null");
            assertNotNull(eval.getDescription(), "Description should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getComplexityScore(), "Complexity score should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getRepositoryImpactScore(), "Repository impact should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getDependencyImpactScore(), "Dependency impact should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getTestingEffortScore(), "Testing effort should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getArchitecturalConsistencyScore(), "Architectural consistency should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getMaintainabilityScore(), "Maintainability should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getTechnicalRiskScore(), "Technical risk should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getSustainabilityScore(), "Sustainability should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getOverallScore(), "Overall score should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getEstimatedEffort(), "Estimated effort should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getRiskAssessment(), "Risk assessment should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getPros(), "Pros should not be null for " + eval.getStrategyName());
            assertNotNull(eval.getCons(), "Cons should not be null for " + eval.getStrategyName());
            assertFalse(eval.getPros().isEmpty(), "Pros should not be empty for " + eval.getStrategyName());
            assertFalse(eval.getCons().isEmpty(), "Cons should not be empty for " + eval.getStrategyName());
        }
    }

    @Test
    void testEvaluateStrategies_ScoresInRange() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Add a new feature to the system",
                "test-repo");

        for (StrategyEvaluation eval : response.getStrategies()) {
            assertTrue(eval.getComplexityScore() >= 1 && eval.getComplexityScore() <= 10,
                    "Complexity score out of range for " + eval.getStrategyName());
            assertTrue(eval.getRepositoryImpactScore() >= 1 && eval.getRepositoryImpactScore() <= 10,
                    "Repository impact out of range for " + eval.getStrategyName());
            assertTrue(eval.getDependencyImpactScore() >= 1 && eval.getDependencyImpactScore() <= 10,
                    "Dependency impact out of range for " + eval.getStrategyName());
            assertTrue(eval.getTestingEffortScore() >= 1 && eval.getTestingEffortScore() <= 10,
                    "Testing effort out of range for " + eval.getStrategyName());
            assertTrue(eval.getArchitecturalConsistencyScore() >= 1 && eval.getArchitecturalConsistencyScore() <= 10,
                    "Architectural consistency out of range for " + eval.getStrategyName());
            assertTrue(eval.getMaintainabilityScore() >= 1 && eval.getMaintainabilityScore() <= 10,
                    "Maintainability out of range for " + eval.getStrategyName());
            assertTrue(eval.getTechnicalRiskScore() >= 1 && eval.getTechnicalRiskScore() <= 10,
                    "Technical risk out of range for " + eval.getStrategyName());
            assertTrue(eval.getSustainabilityScore() >= 1 && eval.getSustainabilityScore() <= 10,
                    "Sustainability out of range for " + eval.getStrategyName());
            assertTrue(eval.getOverallScore() >= 1 && eval.getOverallScore() <= 10,
                    "Overall score out of range for " + eval.getStrategyName());
        }
    }

    @Test
    void testEvaluateStrategies_ComparisonHasAllFields() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Create a new reporting module",
                "reporting-app");

        StrategyComparison comparison = response.getComparison();
        assertNotNull(comparison);
        assertNotNull(comparison.getBestComplexity());
        assertNotNull(comparison.getBestRepositoryImpact());
        assertNotNull(comparison.getBestDependencyImpact());
        assertNotNull(comparison.getBestTestingEffort());
        assertNotNull(comparison.getBestArchitecturalConsistency());
        assertNotNull(comparison.getBestMaintainability());
        assertNotNull(comparison.getLowestRisk());
        assertNotNull(comparison.getBestSustainability());
        assertNotNull(comparison.getOverallBestScore());
    }

    @Test
    void testEvaluateStrategies_RecommendedStrategyIsInStrategies() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Implement a new search functionality",
                "search-service");

        boolean found = response.getStrategies().stream()
                .anyMatch(e -> e.getStrategyName().equals(response.getRecommendedStrategy()));
        assertTrue(found, "Recommended strategy should be one of the evaluated strategies");
    }

    @Test
    void testEvaluateStrategies_DeterministicOutput() {
        DevelopmentStrategyResponse first = service.evaluateStrategies(
                "Add a new API endpoint for data export",
                "data-service");

        DevelopmentStrategyResponse second = service.evaluateStrategies(
                "Add a new API endpoint for data export",
                "data-service");

        assertEquals(first.getRecommendedStrategy(), second.getRecommendedStrategy(),
                "Output should be deterministic");
        assertEquals(first.getStrategies().size(), second.getStrategies().size(),
                "Number of strategies should be deterministic");

        for (int i = 0; i < first.getStrategies().size(); i++) {
            assertEquals(first.getStrategies().get(i).getOverallScore(),
                    second.getStrategies().get(i).getOverallScore(),
                    "Scores should be deterministic for " + first.getStrategies().get(i).getStrategyName());
        }
    }

    @Test
    void testEvaluateStrategies_ComplexRequest() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "Implement a complex cross-cutting concern for audit logging across multiple services",
                "enterprise-app");

        assertNotNull(response);
        assertEquals(8, response.getStrategies().size());
    }

    @Test
    void testEvaluateStrategies_RequestWithWhitespace() {
        DevelopmentStrategyResponse response = service.evaluateStrategies(
                "  Add a new feature  ",
                "  my-repo  ");

        assertEquals("Add a new feature", response.getRequestDescription());
        assertEquals("my-repo", response.getRepositoryName());
    }
}