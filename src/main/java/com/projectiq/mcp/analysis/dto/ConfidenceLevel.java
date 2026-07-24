package com.projectiq.mcp.analysis.dto;

/**
 * Represents the confidence level of the task analysis.
 * Determined deterministically based on keyword matching strength and entity detection.
 */
public enum ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW
}