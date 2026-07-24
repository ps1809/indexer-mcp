package com.projectiq.mcp.knowledgegraph.dto;

/**
 * Enum representing supported relationship types in the repository knowledge graph.
 */
public enum RelationshipType {
    CONTAINS,
    CALLS,
    DEPENDS_ON,
    IMPLEMENTS,
    EXTENDS,
    INJECTS,
    USES,
    REFERENCES,
    CONFIGURES,
    EXPOSES
}