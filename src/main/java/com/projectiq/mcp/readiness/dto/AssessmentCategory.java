package com.projectiq.mcp.readiness.dto;

/**
 * Assessment categories evaluated during the Intelligent Execution Readiness Assessment.
 * Each category represents a distinct dimension of readiness that is evaluated
 * deterministically to produce the overall readiness level.
 */
public enum AssessmentCategory {
    WORKFLOW,
    REPOSITORY,
    DEPENDENCIES,
    ARCHITECTURE,
    TESTING,
    CONFIGURATION,
    RISK,
    DOCUMENTATION
}