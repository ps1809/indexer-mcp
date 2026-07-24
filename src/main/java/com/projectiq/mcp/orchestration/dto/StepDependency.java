package com.projectiq.mcp.orchestration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Defines a dependency relationship between workflow steps.
 * A step with dependencies must wait for its prerequisite steps to complete first.
 */
public class StepDependency {

    private final String stepName;
    private final List<String> dependsOn;
    private final String description;

    @JsonCreator
    public StepDependency(
            @JsonProperty("stepName") String stepName,
            @JsonProperty("dependsOn") List<String> dependsOn,
            @JsonProperty("description") String description) {
        this.stepName = stepName;
        this.dependsOn = Collections.unmodifiableList(new ArrayList<>(
                dependsOn != null ? dependsOn : Collections.emptyList()));
        this.description = description != null ? description : "";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepDependency that = (StepDependency) o;
        return Objects.equals(stepName, that.stepName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stepName);
    }
}