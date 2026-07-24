package com.projectiq.mcp.analysis.dto;

/**
 * Represents the estimated risk level for a potential impact or risk item
 * in an impact analysis. Determined through deterministic rule-based analysis.
 */
public enum RiskLevel {

    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String displayName;

    RiskLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}