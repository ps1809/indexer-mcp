package com.projectiq.mcp.planning.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Input request for generating an execution roadmap.
 * Contains workflow definition information with steps and optional dependencies.
 */
public class ExecutionPlanRequest {

    private final String workflowName;
    private final String workflowType;
    private final String originalRequest;
    private final List<PlanStep> steps;
    private final List<PlanDependency> dependencies;

    @JsonCreator
    public ExecutionPlanRequest(
            @JsonProperty("workflowName") String workflowName,
            @JsonProperty("workflowType") String workflowType,
            @JsonProperty("originalRequest") String originalRequest,
            @JsonProperty("steps") List<PlanStep> steps,
            @JsonProperty("dependencies") List<PlanDependency> dependencies) {
        this.workflowName = workflowName;
        this.workflowType = workflowType;
        this.originalRequest = originalRequest;
        this.steps = steps != null ? Collections.unmodifiableList(new ArrayList<>(steps)) : List.of();
        this.dependencies = dependencies != null ? Collections.unmodifiableList(new ArrayList<>(dependencies)) : List.of();
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    public List<PlanStep> getSteps() {
        return steps;
    }

    public List<PlanDependency> getDependencies() {
        return dependencies;
    }

    /**
     * A single step within the workflow execution plan.
     */
    public static class PlanStep {
        private final String name;
        private final String description;
        private final String category;

        @JsonCreator
        public PlanStep(
                @JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("category") String category) {
            this.name = name;
            this.description = description;
            this.category = category;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }
    }

    /**
     * A dependency relationship between workflow steps.
     */
    public static class PlanDependency {
        private final String stepName;
        private final List<String> dependsOn;
        private final String description;

        @JsonCreator
        public PlanDependency(
                @JsonProperty("stepName") String stepName,
                @JsonProperty("dependsOn") List<String> dependsOn,
                @JsonProperty("description") String description) {
            this.stepName = stepName;
            this.dependsOn = dependsOn != null
                    ? Collections.unmodifiableList(new ArrayList<>(dependsOn))
                    : List.of();
            this.description = description;
        }

        public String getStepName() {
            return stepName;
        }

        public List<String> getDependsOn() {
            return dependsOn;
        }

        public String getDescription() {
            return description;
        }
    }
}