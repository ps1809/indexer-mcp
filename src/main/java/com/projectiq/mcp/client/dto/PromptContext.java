package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * AI-ready structured prompt context output DTO. Contains the essential
 * repository information needed for an AI coding agent to begin implementation,
 * optimized for deterministic output and downstream AI consumption.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "task", "repositoryName", "branch", "buildTimestamp",
    "repositorySummary", "relevantPackages", "relevantClasses",
    "relevantMethods", "springComponents", "restApis",
    "relatedFiles", "requiredDependencies", "repositoryConventions",
    "errors"
})
public class PromptContext {

    private String task;
    private String repositoryName;
    private String branch;
    private String buildTimestamp;

    // Repository Summary
    private RepositorySummaryInfo repositorySummary;

    // Relevant Packages
    private List<String> relevantPackages = new ArrayList<>();

    // Relevant Classes
    private List<ClassInfo> relevantClasses = new ArrayList<>();

    // Relevant Methods
    private List<MethodInfo> relevantMethods = new ArrayList<>();

    // Spring Components
    private List<SpringComponentInfo> springComponents = new ArrayList<>();

    // REST APIs
    private List<RestEndpointInfo> restApis = new ArrayList<>();

    // Related Files
    private List<RelatedFile> relatedFiles = new ArrayList<>();

    // Required Dependencies
    private List<DependencyInfo> requiredDependencies = new ArrayList<>();

    // Repository Conventions
    private RepositoryConventionsInfo repositoryConventions;

    // Errors encountered during context building
    private List<ContextBuildError> errors = new ArrayList<>();

    public PromptContext() {
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

    public RepositorySummaryInfo getRepositorySummary() {
        return repositorySummary;
    }

    public void setRepositorySummary(RepositorySummaryInfo repositorySummary) {
        this.repositorySummary = repositorySummary;
    }

    public List<String> getRelevantPackages() {
        return relevantPackages;
    }

    public void setRelevantPackages(List<String> relevantPackages) {
        this.relevantPackages = relevantPackages != null ? relevantPackages : new ArrayList<>();
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

    public List<RelatedFile> getRelatedFiles() {
        return relatedFiles;
    }

    public void setRelatedFiles(List<RelatedFile> relatedFiles) {
        this.relatedFiles = relatedFiles != null ? relatedFiles : new ArrayList<>();
    }

    public List<DependencyInfo> getRequiredDependencies() {
        return requiredDependencies;
    }

    public void setRequiredDependencies(List<DependencyInfo> requiredDependencies) {
        this.requiredDependencies = requiredDependencies != null ? requiredDependencies : new ArrayList<>();
    }

    public RepositoryConventionsInfo getRepositoryConventions() {
        return repositoryConventions;
    }

    public void setRepositoryConventions(RepositoryConventionsInfo repositoryConventions) {
        this.repositoryConventions = repositoryConventions;
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
        return "PromptContext{" +
                "task='" + task + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                ", relevantPackages=" + (relevantPackages != null ? relevantPackages.size() : 0) +
                ", relevantClasses=" + (relevantClasses != null ? relevantClasses.size() : 0) +
                ", relevantMethods=" + (relevantMethods != null ? relevantMethods.size() : 0) +
                ", springComponents=" + (springComponents != null ? springComponents.size() : 0) +
                ", restApis=" + (restApis != null ? restApis.size() : 0) +
                ", relatedFiles=" + (relatedFiles != null ? relatedFiles.size() : 0) +
                ", requiredDependencies=" + (requiredDependencies != null ? requiredDependencies.size() : 0) +
                ", errors=" + (errors != null ? errors.size() : 0) +
                '}';
    }

    /**
     * Simplified repository summary information for AI consumption.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({"name", "branch", "description", "fileCount", "classCount", "methodCount", "commitCount", "packageCount"})
    public static class RepositorySummaryInfo {
        private String name;
        private String branch;
        private String description;
        private long fileCount;
        private long classCount;
        private long methodCount;
        private long commitCount;
        private long packageCount;

        public RepositorySummaryInfo() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public long getFileCount() {
            return fileCount;
        }

        public void setFileCount(long fileCount) {
            this.fileCount = fileCount;
        }

        public long getClassCount() {
            return classCount;
        }

        public void setClassCount(long classCount) {
            this.classCount = classCount;
        }

        public long getMethodCount() {
            return methodCount;
        }

        public void setMethodCount(long methodCount) {
            this.methodCount = methodCount;
        }

        public long getCommitCount() {
            return commitCount;
        }

        public void setCommitCount(long commitCount) {
            this.commitCount = commitCount;
        }

        public long getPackageCount() {
            return packageCount;
        }

        public void setPackageCount(long packageCount) {
            this.packageCount = packageCount;
        }
    }

    /**
     * Repository conventions extracted for AI consumption.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder({"namingConventions", "packageStructure", "frameworkVersion", "buildTool", "javaVersion"})
    public static class RepositoryConventionsInfo {
        private String namingConventions;
        private String packageStructure;
        private String frameworkVersion;
        private String buildTool;
        private String javaVersion;

        public RepositoryConventionsInfo() {
        }

        public String getNamingConventions() {
            return namingConventions;
        }

        public void setNamingConventions(String namingConventions) {
            this.namingConventions = namingConventions;
        }

        public String getPackageStructure() {
            return packageStructure;
        }

        public void setPackageStructure(String packageStructure) {
            this.packageStructure = packageStructure;
        }

        public String getFrameworkVersion() {
            return frameworkVersion;
        }

        public void setFrameworkVersion(String frameworkVersion) {
            this.frameworkVersion = frameworkVersion;
        }

        public String getBuildTool() {
            return buildTool;
        }

        public void setBuildTool(String buildTool) {
            this.buildTool = buildTool;
        }

        public String getJavaVersion() {
            return javaVersion;
        }

        public void setJavaVersion(String javaVersion) {
            this.javaVersion = javaVersion;
        }
    }
}