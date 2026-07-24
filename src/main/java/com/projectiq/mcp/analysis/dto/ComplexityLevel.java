package com.projectiq.mcp.analysis.dto;

/**
 * Represents the estimated complexity of the development task.
 * Determined deterministically based on the number of required tools and detected entities.
 */
public enum ComplexityLevel {
    LOW,
    MEDIUM,
    HIGH
}