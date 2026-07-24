package com.projectiq.mcp.client.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for ProjectIQ Indexer connection.
 */
@Validated
@ConfigurationProperties(prefix = "projectiq.indexer")
public class IndexerProperties {

    @NotBlank(message = "Indexer base URL must not be blank")
    private String baseUrl = "http://localhost:8081";

    @Positive(message = "Connect timeout must be positive")
    private int connectTimeout = 5000;

    @Positive(message = "Read timeout must be positive")
    private int readTimeout = 30000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
}