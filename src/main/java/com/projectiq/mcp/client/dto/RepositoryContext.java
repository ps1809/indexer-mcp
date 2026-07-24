package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO representing the unified repository context built from
 * multiple Indexer endpoints. Contains all relevant information for a
 * development task.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryContext {

    private String task;
    private String repositoryName;
    private String branch;
    private String buildTimestamp;

    // Repository Summary
    private RepositorySummaryResponse repositorySummary;

    // Repository Statistics
    private RepositoryStatsResponse repositoryStatistics;

    // Search Results
    private List<SearchResult> searchResults = new ArrayList<>();

    // Spring Components
    private List<SpringComponentInfo> springComponents = new ArrayList<>();

    // REST APIs
    private List<RestEndpointInfo> restApis = new ArrayList<>();

    // Classes
    private List<ClassInfo> classes = new ArrayList<>();

    // Methods
    private List<MethodInfo> methods = new ArrayList<>();

    // Related Files
    private List<RelatedFile> relatedFiles = new ArrayList<>();

    // Dependencies
    private List<DependencyInfo> dependencies = new ArrayList<>();

    // Errors encountered during context building
    private List<ContextBuildError> errors = new ArrayList<>();

    public RepositoryContext() {
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

    public RepositoryStatsResponse getRepositoryStatistics() {
        return repositoryStatistics;
    }

    public void setRepositoryStatistics(RepositoryStatsResponse repositoryStatistics) {
        this.repositoryStatistics = repositoryStatistics;
    }

    public List<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<SearchResult> searchResults) {
        this.searchResults = searchResults != null ? searchResults : new ArrayList<>();
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

    public List<ClassInfo> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassInfo> classes) {
        this.classes = classes != null ? classes : new ArrayList<>();
    }

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods != null ? methods : new ArrayList<>();
    }

    public List<RelatedFile> getRelatedFiles() {
        return relatedFiles;
    }

    public void setRelatedFiles(List<RelatedFile> relatedFiles) {
        this.relatedFiles = relatedFiles != null ? relatedFiles : new ArrayList<>();
    }

    public List<DependencyInfo> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyInfo> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
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
        return "RepositoryContext{" +
                "task='" + task + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                ", searchResults=" + (searchResults != null ? searchResults.size() : 0) +
                ", springComponents=" + (springComponents != null ? springComponents.size() : 0) +
                ", restApis=" + (restApis != null ? restApis.size() : 0) +
                ", classes=" + (classes != null ? classes.size() : 0) +
                ", methods=" + (methods != null ? methods.size() : 0) +
                ", relatedFiles=" + (relatedFiles != null ? relatedFiles.size() : 0) +
                ", dependencies=" + (dependencies != null ? dependencies.size() : 0) +
                ", errors=" + (errors != null ? errors.size() : 0) +
                '}';
    }
}