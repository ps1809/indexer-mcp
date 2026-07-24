package com.projectiq.mcp.pipeline.dto;

/**
 * Enumeration of all context source types that can contribute to the
 * intelligent context pipeline. Each source represents a specific type
 * of repository intelligence gathered from existing services.
 */
public enum ContextSourceType {

    REPOSITORY_SUMMARY,
    REPOSITORY_STATISTICS,
    SEARCH_RESULTS,
    CLASS_ANALYSIS,
    METHOD_ANALYSIS,
    DEPENDENCY_ANALYSIS,
    REST_APIS,
    SPRING_COMPONENTS,
    RELATED_FILES,
    REPOSITORY_CONTEXT,
    DEVELOPMENT_CONTEXT,
    PROMPT_CONTEXT,
    ARCHITECTURE_INSIGHTS,
    REPOSITORY_CONVENTIONS,
    REPOSITORY_HEALTH,
    IMPACT_ANALYSIS
}