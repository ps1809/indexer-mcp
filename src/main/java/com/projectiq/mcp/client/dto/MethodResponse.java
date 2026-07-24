package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * Response DTO for method search results from ProjectIQ Indexer.
 */
public class MethodResponse {

    private String repositoryName;
    private Integer totalResults;
    private List<MethodInfo> methods;

    public MethodResponse() {
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

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodInfo> methods) {
        this.methods = methods;
    }

    @Override
    public String toString() {
        return "MethodResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", totalResults=" + totalResults +
                ", methods=" + methods +
                '}';
    }
}