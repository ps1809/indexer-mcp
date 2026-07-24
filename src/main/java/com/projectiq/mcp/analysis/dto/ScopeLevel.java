package com.projectiq.mcp.analysis.dto;

/**
 * Represents the estimated scope of implementation or testing for an impact analysis.
 * Determined through deterministic rule-based analysis.
 */
public enum ScopeLevel {

    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large");

    private final String displayName;

    ScopeLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}