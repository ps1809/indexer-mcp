package com.projectiq.mcp.orchestration.dto;

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

    public WorkflowDefinition(WorkflowType workflowType, String originalRequest, List<WorkflowStep> steps, String reasoning) {
        this.workflowType = workflowType;
        this.originalRequest = originalRequest;
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
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