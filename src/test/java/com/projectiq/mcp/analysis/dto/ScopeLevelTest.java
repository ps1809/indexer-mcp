package com.projectiq.mcp.analysis.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScopeLevelTest {

    @Test
    void enumValues_areCorrect() {
        assertEquals(3, ScopeLevel.values().length);
        assertEquals("Small", ScopeLevel.SMALL.getDisplayName());
        assertEquals("Medium", ScopeLevel.MEDIUM.getDisplayName());
        assertEquals("Large", ScopeLevel.LARGE.getDisplayName());
    }

    @Test
    void fromName_small() {
        assertEquals(ScopeLevel.SMALL, ScopeLevel.valueOf("SMALL"));
    }

    @Test
    void fromName_medium() {
        assertEquals(ScopeLevel.MEDIUM, ScopeLevel.valueOf("MEDIUM"));
    }

    @Test
    void fromName_large() {
        assertEquals(ScopeLevel.LARGE, ScopeLevel.valueOf("LARGE"));
    }
}