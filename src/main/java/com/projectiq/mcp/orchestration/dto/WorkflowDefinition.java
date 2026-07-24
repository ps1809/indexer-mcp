package com.projectiq.mcp.orchestration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines a workflow with its type and ordered list of steps to execute.
 * A workflow definition is built deterministically based on the developer request.
 */
public class WorkflowDefinition {

    private final WorkflowType workflowType;
    private final String originalRequest;
    private final List<WorkflowStep> steps;
    private final String reasoning;

    @JsonCreator
    public WorkflowDefinition(
            @JsonProperty("workflowType") WorkflowType workflowType,
            @JsonProperty("originalRequest") String originalRequest,
            @JsonProperty("steps") List<WorkflowStep> steps,
            @JsonProperty("reasoning") String reasoning) {
        this.workflowType = workflowType;
        this.originalRequest = originalRequest;
        this.steps = steps != null ? Collections.unmodifiableList(new ArrayList<>(steps)) : List.of();
        this.reasoning = reasoning;
    }

    public WorkflowType getWorkflowType() {
        return workflowType;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public List<WorkflowStep> getSteps() {
        return steps;
    }

    public String getReasoning() {
        return reasoning;
    }
}