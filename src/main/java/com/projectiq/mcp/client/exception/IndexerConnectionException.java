package com.projectiq.mcp.client.exception;

/**
 * Exception thrown when a connection to the Indexer cannot be established.
 * Covers connection refused and connection timeout scenarios.
 */
public class IndexerConnectionException extends IndexerClientException {

    public IndexerConnectionException(String message) {
        super(message);
    }

    public IndexerConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}