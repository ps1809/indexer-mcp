package com.projectiq.mcp.orchestration.dto;

/**
 * Represents the type of workflow to be orchestrated.
 * Determined through deterministic rule-based analysis without AI or LLM involvement.
 */
public enum WorkflowType {

    FEATURE_IMPLEMENTATION("Feature Implementation"),
    BUG_FIX("Bug Fix"),
    REFACTORING("Refactoring"),
    REST_API_ENHANCEMENT("REST API Enhancement"),
    CONFIGURATION_CHANGE("Configuration Change"),
    DOCUMENTATION_UPDATE("Documentation Update"),
    TEST_IMPROVEMENT("Test Improvement"),
    REPOSITORY_ANALYSIS("Repository Analysis"),
    UNKNOWN("Unknown");

    private final String displayName;

    WorkflowType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}