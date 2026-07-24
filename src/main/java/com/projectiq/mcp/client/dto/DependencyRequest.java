package com.projectiq.mcp.client.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Request DTO for searching dependencies in ProjectIQ Indexer.
 */
public class DependencyRequest {

    private String repositoryName;
    private String branch;
    private String packageName;
    private List<String> dependencyTypes;
    private String searchPattern;

    public DependencyRequest() {
    }

    public DependencyRequest(String repositoryName, List<String> dependencyTypes) {
        this.repositoryName = repositoryName;
        this.dependencyTypes = dependencyTypes;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getDependencyTypes() {
        return dependencyTypes;
    }

    public void setDependencyTypes(List<String> dependencyTypes) {
        this.dependencyTypes = dependencyTypes;
    }

    public String getSearchPattern() {
        return searchPattern;
    }

    public void setSearchPattern(String searchPattern) {
        this.searchPattern = searchPattern;
    }

    /**
     * Adds a single dependency type to the list.
     * If the current list is immutable, it will be replaced with a mutable copy.
     */
    public void addDependencyType(String dependencyType) {
        if (this.dependencyTypes == null) {
            this.dependencyTypes = new java.util.ArrayList<>();
        } else if (!(this.dependencyTypes instanceof java.util.ArrayList)) {
            this.dependencyTypes = new java.util.ArrayList<>(this.dependencyTypes);
        }
        this.dependencyTypes.add(dependencyType);
    }

    @Override
    public String toString() {
        return "DependencyRequest{" +
                "repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                ", packageName='" + packageName + '\'' +
                ", dependencyTypes=" + dependencyTypes +
                ", searchPattern='" + searchPattern + '\'' +
                '}';
    }
}