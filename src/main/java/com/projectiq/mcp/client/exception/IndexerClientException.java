package com.projectiq.mcp.client.exception;

/**
 * Base exception for all Indexer client-related errors.
 */
public class IndexerClientException extends RuntimeException {

    public IndexerClientException(String message) {
        super(message);
    }

    public IndexerClientException(String message, Throwable cause) {
        super(message, cause);
    }
}