package com.projectiq.mcp.client.dto;

import java.util.List;

/**
 * Response DTO for code search from ProjectIQ Indexer.
 */
public class SearchCodeResponse {

    private String repositoryName;
    private String query;
    private Long totalResults;
    private List<SearchResult> results;

    public SearchCodeResponse() {
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

    public Long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Long totalResults) {
        this.totalResults = totalResults;
    }

    public List<SearchResult> getResults() {
        return results;
    }

    public void setResults(List<SearchResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "SearchCodeResponse{repositoryName='" + repositoryName + "', query='" + query + 
                "', totalResults=" + totalResults + "}";
    }
}