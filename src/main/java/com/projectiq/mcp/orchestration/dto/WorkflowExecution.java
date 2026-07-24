package com.projectiq.mcp.orchestration.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the state of a workflow execution including its steps,
 * timing information, and overall status.
 */
public class WorkflowExecution {

    private final WorkflowDefinition definition;
    private final List<WorkflowStep> steps;
    private final long startTimeMillis;
    private long endTimeMillis;
    private ExecutionStatus status;

    public WorkflowExecution(WorkflowDefinition definition) {
        this.definition = definition;
        this.steps = new ArrayList<>();
        this.startTimeMillis = System.currentTimeMillis();
        this.status = ExecutionStatus.PENDING;
    }

    public WorkflowDefinition getDefinition() {
        return definition;
    }

    public List<WorkflowStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public void addStep(WorkflowStep step) {
        this.steps.add(step);
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public long getTotalDurationMillis() {
        if (endTimeMillis > 0) {
            return endTimeMillis - startTimeMillis;
        }
        return System.currentTimeMillis() - startTimeMillis;
    }

    public List<WorkflowStep> getCompletedSteps() {
        List<WorkflowStep> completed = new ArrayList<>();
        for (WorkflowStep step : steps) {
            if (step.getStatus() == WorkflowStep.StepStatus.COMPLETED) {
                completed.add(step);
            }
        }
        return completed;
    }

    public List<WorkflowStep> getSkippedSteps() {
        List<WorkflowStep> skipped = new ArrayList<>();
        for (WorkflowStep step : steps) {
            if (step.getStatus() == WorkflowStep.StepStatus.SKIPPED) {
                skipped.add(step);
            }
        }
        return skipped;
    }

    public List<WorkflowStep> getFailedSteps() {
        List<WorkflowStep> failed = new ArrayList<>();
        for (WorkflowStep step : steps) {
            if (step.getStatus() == WorkflowStep.StepStatus.FAILED) {
                failed.add(step);
            }
        }
        return failed;
    }

    /**
     * Overall execution status of a workflow.
     */
    public enum ExecutionStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        COMPLETED_WITH_SKIPPED,
        COMPLETED_WITH_FAILURES,
        FAILED
    }
}