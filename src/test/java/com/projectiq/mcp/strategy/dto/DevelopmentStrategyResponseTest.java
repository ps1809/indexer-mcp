package com.projectiq.mcp.strategy.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse.StrategyCategory;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse.StrategyEvaluation;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse.StrategyComparison;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DevelopmentStrategyResponse DTO.
 */
class DevelopmentStrategyResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testDefaultConstructor() {
        DevelopmentStrategyResponse response = new DevelopmentStrategyResponse();
        assertNotNull(response.getStrategies());
        assertTrue(response.getStrategies().isEmpty());
        assertNull(response.getRequestDescription());
        assertNull(response.getRepositoryName());
        assertNull(response.getRecommendedStrategy());
        assertNull(response.getDecisionRationale());
        assertNull(response.getComparison());
        assertNull(response.getWarning());
    }

    @Test
    void testSetAndGetFields() {
        DevelopmentStrategyResponse response = new DevelopmentStrategyResponse();
        response.setRequestDescription("Add new API endpoint");
        response.setRepositoryName("my-repo");

        StrategyEvaluation eval = new StrategyEvaluation();
        eval.setStrategyName("API-First Implementation");
        eval.setComplexityScore(6);
        eval.setOverallScore(7);

        response.setStrategies(Arrays.asList(eval));
        response.setRecommendedStrategy("API-First Implementation");
        response.setDecisionRationale("Best overall score");
        response.setWarning("Test warning");

        assertEquals("Add new API endpoint", response.getRequestDescription());
        assertEquals("my-repo", response.getRepositoryName());
        assertEquals(1, response.getStrategies().size());
        assertEquals("API-First Implementation", response.getRecommendedStrategy());
        assertEquals("Best overall score", response.getDecisionRationale());
        assertEquals("Test warning", response.getWarning());
    }

    @Test
    void testStrategiesImmutableCopy() {
        DevelopmentStrategyResponse response = new DevelopmentStrategyResponse();
        List<StrategyEvaluation> original = new ArrayList<>(Arrays.asList(new StrategyEvaluation()));
        response.setStrategies(original);
        original.add(new StrategyEvaluation());
        assertEquals(1, response.getStrategies().size());
    }

    @Test
    void testStrategyCategoryEnum() {
        assertEquals("Extend Existing Component", StrategyCategory.EXTEND_EXISTING_COMPONENT.getDisplayName());
        assertEquals("Create New Component", StrategyCategory.CREATE_NEW_COMPONENT.getDisplayName());
        assertEquals("Refactor Then Implement", StrategyCategory.REFACTOR_THEN_IMPLEMENT.getDisplayName());
        assertEquals("Modular Implementation", StrategyCategory.MODULAR_IMPLEMENTATION.getDisplayName());
        assertEquals("Incremental Enhancement", StrategyCategory.INCREMENTAL_ENHANCEMENT.getDisplayName());
        assertEquals("Configuration-Based Solution", StrategyCategory.CONFIGURATION_BASED_SOLUTION.getDisplayName());
        assertEquals("Service Layer Enhancement", StrategyCategory.SERVICE_LAYER_ENHANCEMENT.getDisplayName());
        assertEquals("API-First Implementation", StrategyCategory.API_FIRST_IMPLEMENTATION.getDisplayName());
        assertEquals(8, StrategyCategory.values().length);
    }

    @Test
    void testStrategyEvaluationDefaultConstructor() {
        StrategyEvaluation eval = new StrategyEvaluation();
        assertNotNull(eval.getPros());
        assertTrue(eval.getPros().isEmpty());
        assertNotNull(eval.getCons());
        assertTrue(eval.getCons().isEmpty());
        assertNull(eval.getStrategyName());
        assertNull(eval.getDescription());
        assertNull(eval.getComplexityScore());
        assertNull(eval.getOverallScore());
    }

    @Test
    void testStrategyEvaluationSetAndGet() {
        StrategyEvaluation eval = new StrategyEvaluation();
        eval.setStrategyName("Modular Implementation");
        eval.setDescription("A modular approach");
        eval.setComplexityScore(7);
        eval.setRepositoryImpactScore(7);
        eval.setDependencyImpactScore(7);
        eval.setTestingEffortScore(6);
        eval.setArchitecturalConsistencyScore(9);
        eval.setMaintainabilityScore(9);
        eval.setTechnicalRiskScore(7);
        eval.setSustainabilityScore(9);
        eval.setOverallScore(8);
        eval.setEstimatedEffort("Medium");
        eval.setRiskAssessment("Low");
        eval.setPros(Arrays.asList("Pro1", "Pro2"));
        eval.setCons(Arrays.asList("Con1"));

        assertEquals("Modular Implementation", eval.getStrategyName());
        assertEquals("A modular approach", eval.getDescription());
        assertEquals(Integer.valueOf(7), eval.getComplexityScore());
        assertEquals(Integer.valueOf(7), eval.getRepositoryImpactScore());
        assertEquals(Integer.valueOf(7), eval.getDependencyImpactScore());
        assertEquals(Integer.valueOf(6), eval.getTestingEffortScore());
        assertEquals(Integer.valueOf(9), eval.getArchitecturalConsistencyScore());
        assertEquals(Integer.valueOf(9), eval.getMaintainabilityScore());
        assertEquals(Integer.valueOf(7), eval.getTechnicalRiskScore());
        assertEquals(Integer.valueOf(9), eval.getSustainabilityScore());
        assertEquals(Integer.valueOf(8), eval.getOverallScore());
        assertEquals("Medium", eval.getEstimatedEffort());
        assertEquals("Low", eval.getRiskAssessment());
        assertEquals(2, eval.getPros().size());
        assertEquals(1, eval.getCons().size());
    }

    @Test
    void testStrategyComparisonSetAndGet() {
        StrategyComparison comparison = new StrategyComparison();
        comparison.setBestComplexity("Configuration-Based Solution");
        comparison.setBestRepositoryImpact("Configuration-Based Solution");
        comparison.setBestDependencyImpact("Configuration-Based Solution");
        comparison.setBestTestingEffort("Configuration-Based Solution");
        comparison.setBestArchitecturalConsistency("Service Layer Enhancement");
        comparison.setBestMaintainability("Modular Implementation");
        comparison.setLowestRisk("Configuration-Based Solution");
        comparison.setBestSustainability("Modular Implementation");
        comparison.setOverallBestScore("Configuration-Based Solution");

        assertEquals("Configuration-Based Solution", comparison.getBestComplexity());
        assertEquals("Configuration-Based Solution", comparison.getBestRepositoryImpact());
        assertEquals("Configuration-Based Solution", comparison.getBestDependencyImpact());
        assertEquals("Configuration-Based Solution", comparison.getBestTestingEffort());
        assertEquals("Service Layer Enhancement", comparison.getBestArchitecturalConsistency());
        assertEquals("Modular Implementation", comparison.getBestMaintainability());
        assertEquals("Configuration-Based Solution", comparison.getLowestRisk());
        assertEquals("Modular Implementation", comparison.getBestSustainability());
        assertEquals("Configuration-Based Solution", comparison.getOverallBestScore());
    }

    @Test
    void testSerialization() throws Exception {
        DevelopmentStrategyResponse response = new DevelopmentStrategyResponse();
        response.setRequestDescription("Add new feature");
        response.setRepositoryName("test-repo");

        StrategyEvaluation eval = new StrategyEvaluation();
        eval.setStrategyName("Incremental Enhancement");
        eval.setOverallScore(8);
        eval.setComplexityScore(9);
        eval.setPros(Arrays.asList("Low risk"));
        eval.setCons(Arrays.asList("May fragment"));
        response.setStrategies(Arrays.asList(eval));

        response.setRecommendedStrategy("Incremental Enhancement");
        response.setDecisionRationale("Best approach");

        StrategyComparison comparison = new StrategyComparison();
        comparison.setOverallBestScore("Incremental Enhancement");
        response.setComparison(comparison);

        String json = objectMapper.writeValueAsString(response);
        assertTrue(json.contains("Add new feature"));
        assertTrue(json.contains("test-repo"));
        assertTrue(json.contains("Incremental Enhancement"));
        assertTrue(json.contains("8"));
    }

    @Test
    void testStrategyEvaluationImmutablePros() {
        StrategyEvaluation eval = new StrategyEvaluation();
        List<String> original = Arrays.asList("Pro1");
        eval.setPros(original);
        original.set(0, "Modified");
        assertEquals("Pro1", eval.getPros().get(0));
    }

    @Test
    void testStrategyEvaluationImmutableCons() {
        StrategyEvaluation eval = new StrategyEvaluation();
        List<String> original = Arrays.asList("Con1");
        eval.setCons(original);
        original.set(0, "Modified");
        assertEquals("Con1", eval.getCons().get(0));
    }

    @Test
    void testStrategiesSetWithNull() {
        DevelopmentStrategyResponse response = new DevelopmentStrategyResponse();
        response.setStrategies(null);
        assertNotNull(response.getStrategies());
        assertTrue(response.getStrategies().isEmpty());
    }

    @Test
    void testStrategyEvaluationSetProsWithNull() {
        StrategyEvaluation eval = new StrategyEvaluation();
        eval.setPros(null);
        assertNotNull(eval.getPros());
        assertTrue(eval.getPros().isEmpty());
    }

    @Test
    void testStrategyEvaluationSetConsWithNull() {
        StrategyEvaluation eval = new StrategyEvaluation();
        eval.setCons(null);
        assertNotNull(eval.getCons());
        assertTrue(eval.getCons().isEmpty());
    }
}