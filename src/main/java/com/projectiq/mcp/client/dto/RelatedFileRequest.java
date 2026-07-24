package com.projectiq.mcp.client.dto;

import java.util.Objects;

/**
 * Request DTO for querying related files from the ProjectIQ Indexer.
 */
public class RelatedFileRequest {

    private String repositoryName;
    private String searchTarget;
    private SearchTargetType targetType;
    private String branch;

    public RelatedFileRequest() {
    }

    public RelatedFileRequest(String repositoryName, String searchTarget, SearchTargetType targetType) {
        this.repositoryName = repositoryName;
        this.searchTarget = searchTarget;
        this.targetType = targetType;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getSearchTarget() {
        return searchTarget;
    }

    public void setSearchTarget(String searchTarget) {
        this.searchTarget = searchTarget;
    }

    public SearchTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(SearchTargetType targetType) {
        this.targetType = targetType;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        return "RelatedFileRequest{" +
                "repositoryName='" + repositoryName + '\'' +
                ", searchTarget='" + searchTarget + '\'' +
                ", targetType=" + targetType +
                ", branch='" + branch + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelatedFileRequest that = (RelatedFileRequest) o;
        return Objects.equals(repositoryName, that.repositoryName) &&
                Objects.equals(searchTarget, that.searchTarget) &&
                targetType == that.targetType &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryName, searchTarget, targetType, branch);
    }

    /**
     * Supported search target types for related file discovery.
     */
    public enum SearchTargetType {
        CLASS,
        METHOD,
        REST_API,
        SPRING_COMPONENT,
        PACKAGE
    }
}