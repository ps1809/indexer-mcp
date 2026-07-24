package com.projectiq.mcp.recommendation.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RecommendationCategoryTest {

    @Test
    void shouldHaveNineCategories() {
        RecommendationCategory[] categories = RecommendationCategory.values();
        assertEquals(9, categories.length);
    }

    @Test
    void shouldIncludeAllCategories() {
        assertNotNull(RecommendationCategory.valueOf("IMPLEMENTATION"));
        assertNotNull(RecommendationCategory.valueOf("ARCHITECTURE"));
        assertNotNull(RecommendationCategory.valueOf("TESTING"));
        assertNotNull(RecommendationCategory.valueOf("PERFORMANCE"));
        assertNotNull(RecommendationCategory.valueOf("DEPENDENCY"));
        assertNotNull(RecommendationCategory.valueOf("CONFIGURATION"));
        assertNotNull(RecommendationCategory.valueOf("REFACTORING"));
        assertNotNull(RecommendationCategory.valueOf("DOCUMENTATION"));
        assertNotNull(RecommendationCategory.valueOf("RISK_MITIGATION"));
    }
}