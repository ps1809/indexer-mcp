package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * Response DTO for REST API endpoint discovery from ProjectIQ Indexer.
 */
public class RestApiResponse {

    private String repositoryName;
    private Integer totalResults;
    private List<RestEndpointInfo> endpoints;

    public RestApiResponse() {
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

    public List<RestEndpointInfo> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<RestEndpointInfo> endpoints) {
        this.endpoints = endpoints;
    }

    @Override
    public String toString() {
        return "RestApiResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", totalResults=" + totalResults +
                ", endpoints=" + endpoints +
                '}';
    }
}