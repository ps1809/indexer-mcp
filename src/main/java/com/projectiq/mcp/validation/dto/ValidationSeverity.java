package com.projectiq.mcp.validation.dto;

/**
 * Severity levels for validation findings.
 * Each finding is classified with one of these deterministic severity levels.
 */
public enum ValidationSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    INFORMATIONAL
}