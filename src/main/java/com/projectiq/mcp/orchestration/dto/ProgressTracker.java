package com.projectiq.mcp.orchestration.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tracks the execution progress of a workflow.
 * Provides current step information, completed/remaining steps, duration, and success rate.
 */
public class ProgressTracker {

    private final int totalSteps;
    private final List<String> completedSteps;
    private final List<String> remainingSteps;
    private final List<String> failedSteps;
    private final List<String> skippedSteps;
    private final String currentStep;
    private final long startedAtMillis;
    private final long durationMillis;
    private final double successRate;

    private ProgressTracker(Builder builder) {
        this.totalSteps = builder.totalSteps;
        this.completedSteps = Collections.unmodifiableList(new ArrayList<>(builder.completedSteps));
        this.remainingSteps = Collections.unmodifiableList(new ArrayList<>(builder.remainingSteps));
        this.failedSteps = Collections.unmodifiableList(new ArrayList<>(builder.failedSteps));
        this.skippedSteps = Collections.unmodifiableList(new ArrayList<>(builder.skippedSteps));
        this.currentStep = builder.currentStep;
        this.startedAtMillis = builder.startedAtMillis;
        this.durationMillis = builder.durationMillis;
        this.successRate = builder.successRate;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public List<String> getCompletedSteps() {
        return completedSteps;
    }

    public List<String> getRemainingSteps() {
        return remainingSteps;
    }

    public List<String> getFailedSteps() {
        return failedSteps;
    }

    public List<String> getSkippedSteps() {
        return skippedSteps;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int totalSteps;
        private List<String> completedSteps = new ArrayList<>();
        private List<String> remainingSteps = new ArrayList<>();
        private List<String> failedSteps = new ArrayList<>();
        private List<String> skippedSteps = new ArrayList<>();
        private String currentStep;
        private long startedAtMillis;
        private long durationMillis;
        private double successRate;

        public Builder totalSteps(int totalSteps) {
            this.totalSteps = totalSteps;
            return this;
        }

        public Builder completedSteps(List<String> completedSteps) {
            this.completedSteps = completedSteps != null ? new ArrayList<>(completedSteps) : new ArrayList<>();
            return this;
        }

        public Builder remainingSteps(List<String> remainingSteps) {
            this.remainingSteps = remainingSteps != null ? new ArrayList<>(remainingSteps) : new ArrayList<>();
            return this;
        }

        public Builder failedSteps(List<String> failedSteps) {
            this.failedSteps = failedSteps != null ? new ArrayList<>(failedSteps) : new ArrayList<>();
            return this;
        }

        public Builder skippedSteps(List<String> skippedSteps) {
            this.skippedSteps = skippedSteps != null ? new ArrayList<>(skippedSteps) : new ArrayList<>();
            return this;
        }

        public Builder currentStep(String currentStep) {
            this.currentStep = currentStep;
            return this;
        }

        public Builder startedAtMillis(long startedAtMillis) {
            this.startedAtMillis = startedAtMillis;
            return this;
        }

        public Builder durationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
            return this;
        }

        public Builder successRate(double successRate) {
            this.successRate = successRate;
            return this;
        }

        public ProgressTracker build() {
            return new ProgressTracker(this);
        }
    }
}