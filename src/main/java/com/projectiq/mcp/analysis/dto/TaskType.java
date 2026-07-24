package com.projectiq.mcp.analysis.dto;

/**
 * Represents the type of development activity detected from a natural language task.
 * Determined through deterministic rule-based analysis without AI or LLM involvement.
 */
public enum TaskType {

    NEW_FEATURE("New Feature"),
    BUG_FIX("Bug Fix"),
    REFACTORING("Refactoring"),
    REST_API_CHANGE("REST API Change"),
    DATABASE_CHANGE("Database Change"),
    PERFORMANCE_IMPROVEMENT("Performance Improvement"),
    CONFIGURATION_CHANGE("Configuration Change"),
    UNIT_TEST("Unit Test"),
    DOCUMENTATION("Documentation"),
    UNKNOWN("Unknown");

    private final String displayName;

    TaskType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}