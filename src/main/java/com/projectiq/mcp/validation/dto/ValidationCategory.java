package com.projectiq.mcp.validation.dto;

/**
 * Categories for validation checks in the Intelligent Validation Pipeline.
 * Each category represents a distinct area of validation.
 */
public enum ValidationCategory {
    WORKFLOW_VALIDATION,
    REPOSITORY_VALIDATION,
    DEPENDENCY_VALIDATION,
    ARCHITECTURE_VALIDATION,
    CONVENTION_VALIDATION,
    TEST_COVERAGE_VALIDATION,
    RISK_VALIDATION,
    EXECUTION_READINESS
}