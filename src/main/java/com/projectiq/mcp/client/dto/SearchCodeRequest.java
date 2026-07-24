package com.projectiq.mcp.client.dto;

/**
 * Request DTO for searching code in ProjectIQ Indexer.
 */
public class SearchCodeRequest {

    private String repositoryName;
    private String query;
    private String branch;
    private String packageName;
    private Integer maxResults;

    public SearchCodeRequest() {
    }

    public SearchCodeRequest(String repositoryName, String query) {
        this.repositoryName = repositoryName;
        this.query = query;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    @Override
    public String toString() {
        return "SearchCodeRequest{repositoryName='" + repositoryName + "', query='" + query + 
                "', packageName='" + packageName + "', maxResults=" + maxResults + "}";
    }
}