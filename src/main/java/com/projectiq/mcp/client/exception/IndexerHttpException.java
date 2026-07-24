package com.projectiq.mcp.client.exception;

/**
 * Exception thrown when the Indexer returns an HTTP error response.
 * Covers 4xx and 5xx HTTP status codes.
 */
public class IndexerHttpException extends IndexerClientException {

    private final int statusCode;

    public IndexerHttpException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public IndexerHttpException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}