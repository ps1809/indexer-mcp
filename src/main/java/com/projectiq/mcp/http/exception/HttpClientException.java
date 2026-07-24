package com.projectiq.mcp.http.exception;

/**
 * Base exception for all HTTP client errors.
 * This is a generic exception that does not contain any business logic.
 */
public class HttpClientException extends RuntimeException {

    private final int statusCode;

    public HttpClientException(String message) {
        super(message);
        this.statusCode = -1;
    }

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    public HttpClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpClientException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}