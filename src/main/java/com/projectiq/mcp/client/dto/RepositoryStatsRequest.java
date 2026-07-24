package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * Request DTO representing the repository statistics request to ProjectIQ Indexer.
 */
public class RepositoryStatsRequest {

    private String repositoryName;
    private String branch;

    public RepositoryStatsRequest() {
    }

    public RepositoryStatsRequest(String repositoryName, String branch) {
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
        return "RepositoryStatsRequest{" +
                "repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                '}';
    }
}