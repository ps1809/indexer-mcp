package com.projectiq.mcp.client;

import com.projectiq.mcp.client.dto.ContributorStats;
import com.projectiq.mcp.client.dto.IndexerHealthResponse;
import com.projectiq.mcp.client.dto.RepositoryStatsRequest;
import com.projectiq.mcp.client.dto.RepositoryStatsResponse;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.SearchCodeRequest;
import com.projectiq.mcp.client.dto.SearchCodeResponse;

/**
 * Interface for REST client communication with ProjectIQ Indexer.
 * Provides methods to interact with Indexer REST APIs.
 */
public interface IndexerRestClient {

    /**
     * Checks the health status of the Indexer.
     *
     * @return health response from the Indexer
     */
    IndexerHealthResponse checkHealth();

    /**
     * Verifies if the Indexer is reachable.
     *
     * @return true if the Indexer is reachable, false otherwise
     */
    boolean isReachable();

    /**
     * Retrieves the repository summary from the Indexer.
     *
     * @param request the repository summary request containing repository name and branch
     * @return repository summary response
     */
    RepositorySummaryResponse getRepositorySummary(RepositorySummaryRequest request);

    /**
     * Retrieves the repository statistics from the Indexer.
     *
     * @param request the repository statistics request containing repository name and branch
     * @return repository statistics response
     */
    RepositoryStatsResponse getRepositoryStatistics(RepositoryStatsRequest request);

    /**
     * Searches code in the Indexer.
     *
     * @param request the search code request containing search criteria
     * @return search code response with matching results
     */
    SearchCodeResponse searchCode(SearchCodeRequest request);
}
