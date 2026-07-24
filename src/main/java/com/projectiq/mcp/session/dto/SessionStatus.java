package com.projectiq.mcp.session.dto;

/**
 * Represents the lifecycle status of a development session.
 * Sessions follow a deterministic state machine: CREATED -> IN_PROGRESS -> COMPLETED / ARCHIVED.
 */
public enum SessionStatus {

    CREATED("Created"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    ARCHIVED("Archived");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}