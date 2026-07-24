package com.projectiq.mcp.client.service;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.config.IndexerProperties;
import com.projectiq.mcp.client.dto.IndexerHealthResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Internal service for verifying connectivity to ProjectIQ Indexer.
 * This service is for internal verification only and is not exposed as an MCP tool.
 */
@Service
public class IndexerConnectivityService {

    private static final Logger logger = LoggerFactory.getLogger(IndexerConnectivityService.class);

    private final IndexerRestClient indexerRestClient;
    private final IndexerProperties indexerProperties;

    public IndexerConnectivityService(IndexerRestClient indexerRestClient, IndexerProperties indexerProperties) {
        this.indexerRestClient = indexerRestClient;
        this.indexerProperties = indexerProperties;
    }

    /**
     * Verifies whether the configured ProjectIQ Indexer instance is reachable.
     *
     * @return true if the Indexer is reachable, false otherwise
     */
    public boolean verifyConnectivity() {
        String baseUrl = indexerProperties.getBaseUrl();
        logger.info("Verifying connectivity to ProjectIQ Indexer at {}", baseUrl);

        boolean reachable = indexerRestClient.isReachable();

        if (reachable) {
            logger.info("Successfully connected to ProjectIQ Indexer at {}", baseUrl);
        } else {
            logger.warn("Failed to connect to ProjectIQ Indexer at {}", baseUrl);
        }

        return reachable;
    }

    /**
     * Checks the health status of the Indexer and returns the health response.
     *
     * @return health response from the Indexer
     * @throws IndexerClientException if the health check fails
     */
    public IndexerHealthResponse checkHealth() {
        logger.debug("Performing Indexer health check");
        return indexerRestClient.checkHealth();
    }
}