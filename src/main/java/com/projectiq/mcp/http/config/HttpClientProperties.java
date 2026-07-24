package com.projectiq.mcp.http.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the generic HTTP client.
 */
@ConfigurationProperties(prefix = "http.client")
public class HttpClientProperties {

    private int connectTimeout = 5000;
    private int readTimeout = 30000;
    private boolean loggingEnabled = false;

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

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }
}