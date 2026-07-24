package com.projectiq.mcp.orchestration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single step within a workflow execution.
 * Each step has a name, description, execution status, and optional result or error.
 */
public class WorkflowStep {

    private final int order;
    private final String name;
    private final String description;
    private StepStatus status;
    private String result;
    private String error;
    private long durationMillis;

    @JsonCreator
    public WorkflowStep(
            @JsonProperty("order") int order,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description) {
        this.order = order;
        this.name = name;
        this.description = description;
        this.status = StepStatus.PENDING;
        this.durationMillis = 0;
    }

    public int getOrder() {
        return order;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    /**
     * Status of a workflow step during execution.
     */
    public enum StepStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        SKIPPED,
        FAILED
    }
}