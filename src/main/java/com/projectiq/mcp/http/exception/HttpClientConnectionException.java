package com.projectiq.mcp.http.exception;

/**
 * Exception thrown when a connection error occurs.
 * Covers connection refused, connection timeout scenarios.
 */
public class HttpClientConnectionException extends HttpClientException {

    public HttpClientConnectionException(String message) {
        super(message);
    }

    public HttpClientConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}