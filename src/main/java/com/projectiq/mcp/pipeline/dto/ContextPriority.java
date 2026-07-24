package com.projectiq.mcp.pipeline.dto;

/**
 * Priority levels for context items in the intelligent context pipeline.
 * Used to rank and order context items by importance.
 */
public enum ContextPriority {

    /**
     * Critical context that must always be included.
     * Examples: workflow summary, repository summary, implementation focus.
     */
    HIGH,

    /**
     * Important context that should be included unless space is limited.
     * Examples: relevant classes, related APIs, dependencies.
     */
    MEDIUM,

    /**
     * Supplementary context that can be omitted if needed.
     * Examples: detailed statistics, file type breakdowns.
     */
    LOW
}