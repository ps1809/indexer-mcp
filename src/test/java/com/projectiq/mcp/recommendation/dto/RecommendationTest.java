package com.projectiq.mcp.recommendation.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;

class RecommendationTest {

    @Test
    void shouldCreateRecommendationWithConstructor() {
        Recommendation rec = new Recommendation(
                "REC-0001",
                RecommendationCategory.IMPLEMENTATION,
                RecommendationPriority.HIGH,
                "Test recommendation",
                "A test recommendation description",
                "Test rationale"
        );

        assertEquals("REC-0001", rec.getId());
        assertEquals(RecommendationCategory.IMPLEMENTATION, rec.getCategory());
        assertEquals(RecommendationPriority.HIGH, rec.getPriority());
        assertEquals("Test recommendation", rec.getTitle());
        assertEquals("A test recommendation description", rec.getDescription());
        assertEquals("Test rationale", rec.getRationale());
        assertNotNull(rec.getActionItems());
        assertTrue(rec.getActionItems().isEmpty());
    }

    @Test
    void shouldSetAndGetActionItems() {
        Recommendation rec = new Recommendation();
        rec.setActionItems(List.of("Action 1", "Action 2"));
        assertEquals(2, rec.getActionItems().size());
        assertTrue(rec.getActionItems().contains("Action 1"));
    }

    @Test
    void shouldAddActionItem() {
        Recommendation rec = new Recommendation();
        rec.addActionItem("New action");
        assertEquals(1, rec.getActionItems().size());
        assertEquals("New action", rec.getActionItems().get(0));
    }

    @Test
    void shouldSetAndGetSource() {
        Recommendation rec = new Recommendation();
        rec.setSource("Validation Analysis");
        assertEquals("Validation Analysis", rec.getSource());
    }

    @Test
    void shouldHandleNullActionItems() {
        Recommendation rec = new Recommendation();
        rec.setActionItems(null);
        assertNotNull(rec.getActionItems());
        assertTrue(rec.getActionItems().isEmpty());
    }

    @Test
    void shouldCreateEmptyRecommendation() {
        Recommendation rec = new Recommendation();
        assertNotNull(rec.getActionItems());
        assertTrue(rec.getActionItems().isEmpty());
    }
}