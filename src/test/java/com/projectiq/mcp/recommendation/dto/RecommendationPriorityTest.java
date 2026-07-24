package com.projectiq.mcp.recommendation.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RecommendationPriorityTest {

    @Test
    void shouldHaveFourPriorityLevels() {
        RecommendationPriority[] priorities = RecommendationPriority.values();
        assertEquals(4, priorities.length);
    }

    @Test
    void shouldIncludeAllPriorities() {
        assertNotNull(RecommendationPriority.valueOf("CRITICAL"));
        assertNotNull(RecommendationPriority.valueOf("HIGH"));
        assertNotNull(RecommendationPriority.valueOf("MEDIUM"));
        assertNotNull(RecommendationPriority.valueOf("LOW"));
    }

    @Test
    void shouldMaintainCorrectOrder() {
        assertTrue(RecommendationPriority.CRITICAL.ordinal() < RecommendationPriority.HIGH.ordinal());
        assertTrue(RecommendationPriority.HIGH.ordinal() < RecommendationPriority.MEDIUM.ordinal());
        assertTrue(RecommendationPriority.MEDIUM.ordinal() < RecommendationPriority.LOW.ordinal());
    }
}