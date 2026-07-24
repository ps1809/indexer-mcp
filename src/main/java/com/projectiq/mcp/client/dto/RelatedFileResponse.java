package com.projectiq.mcp.client.dto;

import java.util.List;
import java.util.Objects;

/**
 * Response DTO for related file discovery from the ProjectIQ Indexer.
 */
public class RelatedFileResponse {

    /**
     * Search target type enum matching the request.
     */
    public enum SearchTargetType {
        CLASS,
        METHOD,
        REST_API,
        SPRING_COMPONENT,
        PACKAGE
    }

    private String repositoryName;
    private String searchTarget;
    private SearchTargetType targetType;
    private Integer totalResults;
    private List<RelatedFile> relatedFiles;

    public RelatedFileResponse() {
    }

    public RelatedFileResponse(String repositoryName, String searchTarget, SearchTargetType targetType, Integer totalResults, List<RelatedFile> relatedFiles) {
        this.repositoryName = repositoryName;
        this.searchTarget = searchTarget;
        this.targetType = targetType;
        this.totalResults = totalResults;
        this.relatedFiles = relatedFiles;
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

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public List<RelatedFile> getRelatedFiles() {
        return relatedFiles;
    }

    public void setRelatedFiles(List<RelatedFile> relatedFiles) {
        this.relatedFiles = relatedFiles;
    }

    @Override
    public String toString() {
        return "RelatedFileResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", searchTarget='" + searchTarget + '\'' +
                ", targetType=" + targetType +
                ", totalResults=" + totalResults +
                ", relatedFiles=" + relatedFiles +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelatedFileResponse that = (RelatedFileResponse) o;
        return Objects.equals(repositoryName, that.repositoryName) &&
                Objects.equals(searchTarget, that.searchTarget) &&
                targetType == that.targetType &&
                Objects.equals(totalResults, that.totalResults) &&
                Objects.equals(relatedFiles, that.relatedFiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repositoryName, searchTarget, targetType, totalResults, relatedFiles);
    }
}