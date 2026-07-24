package com.projectiq.mcp.client.exception;

/**
 * Exception thrown when a request to the Indexer times out.
 * Covers both connection timeout and read timeout scenarios.
 */
public class IndexerTimeoutException extends IndexerClientException {

    public IndexerTimeoutException(String message) {
        super(message);
    }

    public IndexerTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}