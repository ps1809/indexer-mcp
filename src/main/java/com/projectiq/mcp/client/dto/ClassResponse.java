package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * Response DTO for class search results from ProjectIQ Indexer.
 */
public class ClassResponse {

    private String repositoryName;
    private Integer totalResults;
    private List<ClassInfo> classes;

    public ClassResponse() {
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

    public List<ClassInfo> getClasses() {
        return classes;
    }

    public void setClasses(List<ClassInfo> classes) {
        this.classes = classes;
    }

    @Override
    public String toString() {
        return "ClassResponse{" +
                "repositoryName='" + repositoryName + '\'' +
                ", totalResults=" + totalResults +
                ", classes=" + classes +
                '}';
    }
}