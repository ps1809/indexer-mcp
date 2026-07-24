package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * Response DTO for dependency discovery from ProjectIQ Indexer.
 */
public class DependencyResponse {

    private String repositoryName;
    private Integer totalResults;
    private List<DependencyInfo> dependencies;

    public DependencyResponse() {
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public List<DependencyInfo> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyInfo> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public String toString() {
        return "DependencyResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", totalResults=" + totalResults +
                ", dependencies=" + dependencies +
                '}';
    }
}