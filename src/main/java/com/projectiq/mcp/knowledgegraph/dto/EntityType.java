package com.projectiq.mcp.knowledgegraph.dto;

/**
 * Enum representing supported entity types in the repository knowledge graph.
 */
public enum EntityType {
    REPOSITORY,
    PACKAGE,
    CLASS,
    INTERFACE,
    METHOD,
    REST_API,
    SPRING_COMPONENT,
    CONFIGURATION,
    DEPENDENCY,
    DATABASE_ENTITY,
    MODULE
}