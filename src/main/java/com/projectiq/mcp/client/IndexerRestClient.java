package com.projectiq.mcp.client;

import com.projectiq.mcp.client.dto.IndexerHealthResponse;

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
}