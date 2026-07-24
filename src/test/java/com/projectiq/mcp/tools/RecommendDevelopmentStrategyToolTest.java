package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse;
import com.projectiq.mcp.strategy.service.DevelopmentStrategyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RecommendDevelopmentStrategyTool MCP tool.
 */
class RecommendDevelopmentStrategyToolTest {

    private RecommendDevelopmentStrategyTool tool;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        DevelopmentStrategyService service = new DevelopmentStrategyService();
        tool = new RecommendDevelopmentStrategyTool(service);
    }

    @Test
    void testRecommendDevelopmentStrategy_NewFeature() throws Exception {
        String result = tool.recommendDevelopmentStrategy(
                "Implement a new dashboard widget",
                "my-project");

        assertNotNull(result);
        DevelopmentStrategyResponse response = objectMapper.readValue(result, DevelopmentStrategyResponse.class);
        assertEquals("Implement a new dashboard widget", response.getRequestDescription());
        assertEquals("my-project", response.getRepositoryName());
        assertNotNull(response.getRecommendedStrategy());
        assertNotNull(response.getDecisionRationale());
        assertEquals(8, response.getStrategies().size());
        assertNotNull(response.getComparison());
    }

    @Test
    void testRecommendDevelopmentStrategy_Refactoring() throws Exception {
        String result = tool.recommendDevelopmentStrategy(
                "Refactor the authentication service to improve performance",
                "auth-service");

        assertNotNull(result);
        DevelopmentStrategyResponse response = objectMapper.readValue(result, DevelopmentStrategyResponse.class);
        assertEquals("Refactor the authentication service to improve performance", response.getRequestDescription());
        assertEquals("auth-service", response.getRepositoryName());
        assertEquals(8, response.getStrategies().size());
    }

    @Test
    void testRecommendDevelopmentStrategy_ApiEnhancement() throws Exception {
        String result = tool.recommendDevelopmentStrategy(
                "Add a new REST API endpoint for user profile management",
                "api-gateway");

        assertNotNull(result);
        DevelopmentStrategyResponse response = objectMapper.readValue(result, DevelopmentStrategyResponse.class);
        assertEquals("Add a new REST API endpoint for user profile management", response.getRequestDescription());
        assertEquals("api-gateway", response.getRepositoryName());
    }

    @Test
    void testRecommendDevelopmentStrategy_ConfigurationStrategy() throws Exception {
        String result = tool.recommendDevelopmentStrategy(
                "Add a configuration flag to toggle feature visibility",
                "web-app");

        assertNotNull(result);
        DevelopmentStrategyResponse response = objectMapper.readValue(result, DevelopmentStrategyResponse.class);
        assertEquals("Add a configuration flag to toggle feature visibility", response.getRequestDescription());
    }

    @Test
    void testRecommendDevelopmentStrategy_EmptyRequestDescription() throws Exception {
        String result = tool.recommendDevelopmentStrategy("", "my-repo");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Request description is required"));
    }

    @Test
    void testRecommendDevelopmentStrategy_NullRequestDescription() throws Exception {
        String result = tool.recommendDevelopmentStrategy(null, "my-repo");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Request description is required"));
    }

    @Test
    void testRecommendDevelopmentStrategy_EmptyRepositoryName() throws Exception {
        String result = tool.recommendDevelopmentStrategy("Add feature", "");
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testRecommendDevelopmentStrategy_NullRepositoryName() throws Exception {
        String result = tool.recommendDevelopmentStrategy("Add feature", null);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
    }

    @Test
    void testRecommendDevelopmentStrategy_RequestWithWhitespace() throws Exception {
        String result = tool.recommendDevelopmentStrategy("  Add new feature  ", "  my-repo  ");
        assertNotNull(result);
        DevelopmentStrategyResponse response = objectMapper.readValue(result, DevelopmentStrategyResponse.class);
        assertEquals("Add new feature", response.getRequestDescription());
        assertEquals("my-repo", response.getRepositoryName());
    }

    @Test
    void testRecommendDevelopmentStrategy_DeterministicOutput() throws Exception {
        String first = tool.recommendDevelopmentStrategy(
                "Implement a new search feature",
                "search-service");
        String second = tool.recommendDevelopmentStrategy(
                "Implement a new search feature",
                "search-service");

        assertEquals(first, second, "Output should be deterministic");
    }

    @Test
    void testRecommendDevelopmentStrategy_AllStrategiesComplete() throws Exception {
        String result = tool.recommendDevelopmentStrategy(
                "Create a new reporting dashboard",
                "reporting-app");

        DevelopmentStrategyResponse response = objectMapper.readValue(result, DevelopmentStrategyResponse.class);

        assertEquals(8, response.getStrategies().size());

        // Verify all strategies have complete data
        for (DevelopmentStrategyResponse.StrategyEvaluation eval : response.getStrategies()) {
            assertNotNull(eval.getStrategyName());
            assertNotNull(eval.getDescription());
            assertNotNull(eval.getComplexityScore());
            assertNotNull(eval.getOverallScore());
            assertFalse(eval.getPros().isEmpty());
            assertFalse(eval.getCons().isEmpty());
            assertNotNull(eval.getEstimatedEffort());
            assertNotNull(eval.getRiskAssessment());
        }

        // Verify comparison is complete
        assertNotNull(response.getComparison().getBestComplexity());
        assertNotNull(response.getComparison().getOverallBestScore());
    }

    @Test
    void testRecommendDevelopmentStrategy_InvalidRepositoryNameOnlyTriggeredWhenRequestEmpty() throws Exception {
        // This tests the tool handles both validations independently
        String nullRequest = tool.recommendDevelopmentStrategy(null, null);
        assertTrue(nullRequest.contains("Request description is required"));

        String nullRepo = tool.recommendDevelopmentStrategy("Add feature", null);
        assertTrue(nullRepo.contains("Repository name is required"));
    }
}