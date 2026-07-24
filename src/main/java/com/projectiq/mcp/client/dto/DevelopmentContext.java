package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * Development-optimized context DTO containing only the information
 * relevant for implementation tasks. Built from {@link RepositoryContext}
 * with redundant information removed and deterministic ordering.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DevelopmentContext {

    private String task;
    private String repositoryName;
    private String branch;
    private String buildTimestamp;

    // Repository Summary
    private RepositorySummaryResponse repositorySummary;

    // Relevant Classes (sorted)
    private List<ClassInfo> relevantClasses = new ArrayList<>();

    // Relevant Methods (sorted)
    private List<MethodInfo> relevantMethods = new ArrayList<>();

    // Spring Components (sorted)
    private List<SpringComponentInfo> springComponents = new ArrayList<>();

    // REST APIs (sorted)
    private List<RestEndpointInfo> restApis = new ArrayList<>();

    // Dependencies (sorted)
    private List<DependencyInfo> dependencies = new ArrayList<>();

    // Related Files (sorted)
    private List<RelatedFile> relatedFiles = new ArrayList<>();

    // Errors encountered during context building
    private List<ContextBuildError> errors = new ArrayList<>();

    public DevelopmentContext() {
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    public void setBuildTimestamp(String buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    public RepositorySummaryResponse getRepositorySummary() {
        return repositorySummary;
    }

    public void setRepositorySummary(RepositorySummaryResponse repositorySummary) {
        this.repositorySummary = repositorySummary;
    }

    public List<ClassInfo> getRelevantClasses() {
        return relevantClasses;
    }

    public void setRelevantClasses(List<ClassInfo> relevantClasses) {
        this.relevantClasses = relevantClasses != null ? relevantClasses : new ArrayList<>();
    }

    public List<MethodInfo> getRelevantMethods() {
        return relevantMethods;
    }

    public void setRelevantMethods(List<MethodInfo> relevantMethods) {
        this.relevantMethods = relevantMethods != null ? relevantMethods : new ArrayList<>();
    }

    public List<SpringComponentInfo> getSpringComponents() {
        return springComponents;
    }

    public void setSpringComponents(List<SpringComponentInfo> springComponents) {
        this.springComponents = springComponents != null ? springComponents : new ArrayList<>();
    }

    public List<RestEndpointInfo> getRestApis() {
        return restApis;
    }

    public void setRestApis(List<RestEndpointInfo> restApis) {
        this.restApis = restApis != null ? restApis : new ArrayList<>();
    }

    public List<DependencyInfo> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyInfo> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }

    public List<RelatedFile> getRelatedFiles() {
        return relatedFiles;
    }

    public void setRelatedFiles(List<RelatedFile> relatedFiles) {
        this.relatedFiles = relatedFiles != null ? relatedFiles : new ArrayList<>();
    }

    public List<ContextBuildError> getErrors() {
        return errors;
    }

    public void setErrors(List<ContextBuildError> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public void addError(ContextBuildError error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    @Override
    public String toString() {
        return "DevelopmentContext{" +
                "task='" + task + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                ", relevantClasses=" + (relevantClasses != null ? relevantClasses.size() : 0) +
                ", relevantMethods=" + (relevantMethods != null ? relevantMethods.size() : 0) +
                ", springComponents=" + (springComponents != null ? springComponents.size() : 0) +
                ", restApis=" + (restApis != null ? restApis.size() : 0) +
                ", dependencies=" + (dependencies != null ? dependencies.size() : 0) +
                ", relatedFiles=" + (relatedFiles != null ? relatedFiles.size() : 0) +
                ", errors=" + (errors != null ? errors.size() : 0) +
                '}';
    }
}