package com.projectiq.mcp.analysis.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskLevelTest {

    @Test
    void enumValues_areCorrect() {
        assertEquals(3, RiskLevel.values().length);
        assertEquals("Low", RiskLevel.LOW.getDisplayName());
        assertEquals("Medium", RiskLevel.MEDIUM.getDisplayName());
        assertEquals("High", RiskLevel.HIGH.getDisplayName());
    }

    @Test
    void fromName_low() {
        assertEquals(RiskLevel.LOW, RiskLevel.valueOf("LOW"));
    }

    @Test
    void fromName_medium() {
        assertEquals(RiskLevel.MEDIUM, RiskLevel.valueOf("MEDIUM"));
    }

    @Test
    void fromName_high() {
        assertEquals(RiskLevel.HIGH, RiskLevel.valueOf("HIGH"));
    }
}