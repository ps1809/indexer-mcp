package com.projectiq.mcp.client.dto;

/**
 * Enum representing the types of dependencies supported by the find_dependency tool.
 */
public enum DependencyType {
    MAVEN,
    GRADLE,
    INTERNAL_MODULE,
    EXTERNAL_LIBRARY
}