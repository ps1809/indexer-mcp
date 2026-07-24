package com.projectiq.mcp.orchestration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single entry in the workflow execution timeline.
 * Captures the step execution order, timestamps, and duration.
 */
public class ExecutionTimelineEntry {

    private final int order;
    private final String stepName;
    private final String stepDescription;
    private final String status;
    private final long startedAtMillis;
    private final long durationMillis;
    private final String result;

    @JsonCreator
    public ExecutionTimelineEntry(
            @JsonProperty("order") int order,
            @JsonProperty("stepName") String stepName,
            @JsonProperty("stepDescription") String stepDescription,
            @JsonProperty("status") String status,
            @JsonProperty("startedAtMillis") long startedAtMillis,
            @JsonProperty("durationMillis") long durationMillis,
            @JsonProperty("result") String result) {
        this.order = order;
        this.stepName = stepName;
        this.stepDescription = stepDescription;
        this.status = status;
        this.startedAtMillis = startedAtMillis;
        this.durationMillis = durationMillis;
        this.result = result;
    }

    public int getOrder() {
        return order;
    }

    public String getStepName() {
        return stepName;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public String getStatus() {
        return status;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public String getResult() {
        return result;
    }
}