package com.projectiq.mcp.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing the health check response from ProjectIQ Indexer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexerHealthResponse {

    private String status;
    private String version;

    public IndexerHealthResponse() {
    }

    public IndexerHealthResponse(String status, String version) {
        this.status = status;
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isHealthy() {
        return "UP".equalsIgnoreCase(status) || "OK".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "IndexerHealthResponse{status='" + status + "', version='" + version + "'}";
    }
}