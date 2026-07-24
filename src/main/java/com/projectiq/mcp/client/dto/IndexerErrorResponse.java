package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing an error response from ProjectIQ Indexer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexerErrorResponse {

    private String message;
    private String error;
    private int status;
    private long timestamp;

    public IndexerErrorResponse() {
    }

    public IndexerErrorResponse(String message, String error, int status, long timestamp) {
        this.message = message;
        this.error = error;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "IndexerErrorResponse{message='" + message + "', error='" + error +
                "', status=" + status + ", timestamp=" + timestamp + "}";
    }
}