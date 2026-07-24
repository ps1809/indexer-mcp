package com.projectiq.mcp.http.exception;

/**
 * Exception thrown when a read or connect timeout occurs.
 */
public class HttpClientTimeoutException extends HttpClientException {

    public HttpClientTimeoutException(String message) {
        super(message);
    }

    public HttpClientTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}