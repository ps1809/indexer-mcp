package com.projectiq.mcp.client.dto;

/**
 * Request DTO for querying repository summary from ProjectIQ Indexer.
 */
public class RepositorySummaryRequest {

    private String repositoryName;
    private String branch;

    public RepositorySummaryRequest() {
    }

    public RepositorySummaryRequest(String repositoryName, String branch) {
        this.repositoryName = repositoryName;
        this.branch = branch;
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

    @Override
    public String toString() {
        return "RepositorySummaryRequest{repositoryName='" + repositoryName + "', branch='" + branch + "'}";
    }
}