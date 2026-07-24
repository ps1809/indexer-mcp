package com.projectiq.mcp.pipeline.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the final AI-ready context package produced by the
 * IntelligentContextPipelineService. Contains all prioritized, filtered,
 * and deduplicated context items assembled into a structured package.
 */
public class ContextPackage {

    private String workflowSummary;
    private String repositorySummary;
    private List<String> relevantClasses;
    private List<String> relevantMethods;
    private List<String> relatedApis;
    private List<String> dependencies;
    private List<String> configuration;
    private List<String> risks;
    private List<String> conventions;
    private List<String> architectureInsights;
    private String suggestedImplementationFocus;
    private int totalContextItems;
    private int highPriorityCount;
    private int mediumPriorityCount;
    private int lowPriorityCount;
    private List<String> warnings;
    private long processingTimeMillis;

    public ContextPackage() {
        this.relevantClasses = new ArrayList<>();
        this.relevantMethods = new ArrayList<>();
        this.relatedApis = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.configuration = new ArrayList<>();
        this.risks = new ArrayList<>();
        this.conventions = new ArrayList<>();
        this.architectureInsights = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public String getWorkflowSummary() {
        return workflowSummary;
    }

    public void setWorkflowSummary(String workflowSummary) {
        this.workflowSummary = workflowSummary;
    }

    public String getRepositorySummary() {
        return repositorySummary;
    }

    public void setRepositorySummary(String repositorySummary) {
        this.repositorySummary = repositorySummary;
    }

    public List<String> getRelevantClasses() {
        return relevantClasses;
    }

    public void setRelevantClasses(List<String> relevantClasses) {
        this.relevantClasses = relevantClasses != null ? relevantClasses : new ArrayList<>();
    }

    public void addRelevantClass(String className) {
        if (this.relevantClasses == null) {
            this.relevantClasses = new ArrayList<>();
        }
        this.relevantClasses.add(className);
    }

    public List<String> getRelevantMethods() {
        return relevantMethods;
    }

    public void setRelevantMethods(List<String> relevantMethods) {
        this.relevantMethods = relevantMethods != null ? relevantMethods : new ArrayList<>();
    }

    public void addRelevantMethod(String methodName) {
        if (this.relevantMethods == null) {
            this.relevantMethods = new ArrayList<>();
        }
        this.relevantMethods.add(methodName);
    }

    public List<String> getRelatedApis() {
        return relatedApis;
    }

    public void setRelatedApis(List<String> relatedApis) {
        this.relatedApis = relatedApis != null ? relatedApis : new ArrayList<>();
    }

    public void addRelatedApi(String api) {
        if (this.relatedApis == null) {
            this.relatedApis = new ArrayList<>();
        }
        this.relatedApis.add(api);
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }

    public void addDependency(String dependency) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.add(dependency);
    }

    public List<String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(List<String> configuration) {
        this.configuration = configuration != null ? configuration : new ArrayList<>();
    }

    public void addConfiguration(String config) {
        if (this.configuration == null) {
            this.configuration = new ArrayList<>();
        }
        this.configuration.add(config);
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks != null ? risks : new ArrayList<>();
    }

    public void addRisk(String risk) {
        if (this.risks == null) {
            this.risks = new ArrayList<>();
        }
        this.risks.add(risk);
    }

    public List<String> getConventions() {
        return conventions;
    }

    public void setConventions(List<String> conventions) {
        this.conventions = conventions != null ? conventions : new ArrayList<>();
    }

    public void addConvention(String convention) {
        if (this.conventions == null) {
            this.conventions = new ArrayList<>();
        }
        this.conventions.add(convention);
    }

    public List<String> getArchitectureInsights() {
        return architectureInsights;
    }

    public void setArchitectureInsights(List<String> architectureInsights) {
        this.architectureInsights = architectureInsights != null ? architectureInsights : new ArrayList<>();
    }

    public void addArchitectureInsight(String insight) {
        if (this.architectureInsights == null) {
            this.architectureInsights = new ArrayList<>();
        }
        this.architectureInsights.add(insight);
    }

    public String getSuggestedImplementationFocus() {
        return suggestedImplementationFocus;
    }

    public void setSuggestedImplementationFocus(String suggestedImplementationFocus) {
        this.suggestedImplementationFocus = suggestedImplementationFocus;
    }

    public int getTotalContextItems() {
        return totalContextItems;
    }

    public void setTotalContextItems(int totalContextItems) {
        this.totalContextItems = totalContextItems;
    }

    public int getHighPriorityCount() {
        return highPriorityCount;
    }

    public void setHighPriorityCount(int highPriorityCount) {
        this.highPriorityCount = highPriorityCount;
    }

    public int getMediumPriorityCount() {
        return mediumPriorityCount;
    }

    public void setMediumPriorityCount(int mediumPriorityCount) {
        this.mediumPriorityCount = mediumPriorityCount;
    }

    public int getLowPriorityCount() {
        return lowPriorityCount;
    }

    public void setLowPriorityCount(int lowPriorityCount) {
        this.lowPriorityCount = lowPriorityCount;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public long getProcessingTimeMillis() {
        return processingTimeMillis;
    }

    public void setProcessingTimeMillis(long processingTimeMillis) {
        this.processingTimeMillis = processingTimeMillis;
    }
}