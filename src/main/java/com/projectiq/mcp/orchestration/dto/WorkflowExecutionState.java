package com.projectiq.mcp.orchestration.dto;

/**
 * Represents the overall state of a workflow execution.
 * Supports states for tracking execution lifecycle including failure and cancellation.
 */
public enum WorkflowExecutionState {

    PENDING("Pending"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    SKIPPED("Skipped"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String displayName;

    WorkflowExecutionState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}