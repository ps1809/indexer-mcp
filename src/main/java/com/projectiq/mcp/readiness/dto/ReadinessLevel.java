package com.projectiq.mcp.readiness.dto;

/**
 * Readiness levels for the Intelligent Execution Readiness Assessment.
 * Each level represents the overall readiness of a workflow for implementation.
 *
 * <p>Ordered from most ready to least ready:
 * <ul>
 *   <li>READY - All prerequisites satisfied, implementation can proceed</li>
 *   <li>READY_WITH_WARNINGS - Prerequisites satisfied with minor warnings</li>
 *   <li>REQUIRES_REVIEW - Some issues found that need human review</li>
 *   <li>NOT_READY - Blocking issues prevent implementation</li>
 * </ul>
 */
public enum ReadinessLevel {
    READY,
    READY_WITH_WARNINGS,
    REQUIRES_REVIEW,
    NOT_READY
}