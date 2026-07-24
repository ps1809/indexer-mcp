package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * Response DTO for Spring component search results from ProjectIQ Indexer.
 */
public class SpringComponentResponse {

    private String repositoryName;
    private Integer totalResults;
    private List<SpringComponentInfo> components;

    public SpringComponentResponse() {
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

    public List<SpringComponentInfo> getComponents() {
        return components;
    }

    public void setComponents(List<SpringComponentInfo> components) {
        this.components = components;
    }

    @Override
    public String toString() {
        return "SpringComponentResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", totalResults=" + totalResults +
                ", components=" + components +
                '}';
    }
}