package com.projectiq.mcp.http.exception;

/**
 * Exception thrown when the server returns an HTTP error status code.
 */
public class HttpClientResponseException extends HttpClientException {

    public HttpClientResponseException(int statusCode, String message) {
        super(statusCode, message);
    }

    public HttpClientResponseException(int statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }
}