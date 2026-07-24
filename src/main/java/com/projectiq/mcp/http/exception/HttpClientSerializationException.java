package com.projectiq.mcp.http.exception;

/**
 * Exception thrown when request/response serialization fails.
 */
public class HttpClientSerializationException extends HttpClientException {

    public HttpClientSerializationException(String message) {
        super(message);
    }

    public HttpClientSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}