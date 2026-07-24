package com.projectiq.mcp.client;

import com.projectiq.mcp.client.dto.ContributorStats;
import com.projectiq.mcp.client.dto.IndexerHealthResponse;
import com.projectiq.mcp.client.dto.RepositoryStatsRequest;
import com.projectiq.mcp.client.dto.RepositoryStatsResponse;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.SearchCodeRequest;
import com.projectiq.mcp.client.dto.SearchCodeResponse;
import com.projectiq.mcp.client.dto.ClassRequest;
import com.projectiq.mcp.client.dto.ClassResponse;
import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
import com.projectiq.mcp.client.dto.MethodRequest;
import com.projectiq.mcp.client.dto.MethodResponse;
import com.projectiq.mcp.client.dto.RestApiRequest;
import com.projectiq.mcp.client.dto.RestApiResponse;
import com.projectiq.mcp.client.dto.SpringComponentRequest;
import com.projectiq.mcp.client.dto.SpringComponentResponse;

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

    /**
     * Finds Spring components in the Indexer.
     *
     * @param request the spring component request containing search criteria
     * @return spring component response with matching components
     */
    SpringComponentResponse findSpringComponent(SpringComponentRequest request);

    /**
     * Finds REST API endpoints in the Indexer.
     *
     * @param request the REST API request containing search criteria
     * @return REST API response with matching endpoints
     */
    RestApiResponse findRestApi(RestApiRequest request);

    /**
     * Finds dependencies in the Indexer.
     *
     * @param request the dependency request containing search criteria
     * @return dependency response with matching dependencies
     */
    DependencyResponse findDependency(DependencyRequest request);

    /**
     * Finds Java classes in the Indexer.
     *
     * @param request the class request containing search criteria
     * @return class response with matching class metadata
     */
    ClassResponse findClass(ClassRequest request);

    /**
     * Finds Java methods in the Indexer.
     *
     * @param request the method request containing search criteria
     * @return method response with matching method metadata
     */
    MethodResponse findMethod(MethodRequest request);
}
