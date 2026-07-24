package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Request DTO for the build_context MCP tool.
 * Contains the developer task and repository information needed to build context.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildContextRequest {

    private String task;
    private String repositoryName;
    private String branch;

    public BuildContextRequest() {
    }

    public BuildContextRequest(String task, String repositoryName) {
        this.task = task;
        this.repositoryName = repositoryName;
    }

    public BuildContextRequest(String task, String repositoryName, String branch) {
        this.task = task;
        this.repositoryName = repositoryName;
        this.branch = branch;
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

    @Override
    public String toString() {
        return "BuildContextRequest{" +
                "task='" + task + '\'' +
                ", repositoryName='" + repositoryName + '\'' +
                ", branch='" + branch + '\'' +
                '}';
    }
}