package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing an error that occurred during context building.
 * Used to track partial failures when aggregating data from multiple endpoints.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextBuildError {

    private String endpoint;
    private String errorType;
    private String message;

    public ContextBuildError() {
    }

    public ContextBuildError(String endpoint, String errorType, String message) {
        this.endpoint = endpoint;
        this.errorType = errorType;
        this.message = message;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ContextBuildError{" +
                "endpoint='" + endpoint + '\'' +
                ", errorType='" + errorType + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}