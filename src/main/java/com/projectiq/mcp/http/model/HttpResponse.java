package com.projectiq.mcp.http.model;

import java.util.Map;

/**
 * Generic HTTP response wrapper.
 * Contains the body, status code, and response headers from an HTTP call.
 */
public class HttpResponse<T> {

    private final T body;
    private final int statusCode;
    private final Map<String, Object> headers;

    public HttpResponse(T body, int statusCode, Map<String, Object> headers) {
        this.body = body;
        this.statusCode = statusCode;
        this.headers = headers;
    }

    public T getBody() {
        return body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }
}