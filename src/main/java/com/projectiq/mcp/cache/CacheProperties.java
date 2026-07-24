package com.projectiq.mcp.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Configuration properties for the response cache.
 * Controls cache behavior including enablement, TTL, and maximum size.
 */
@ConfigurationProperties(prefix = "projectiq.cache")
public class CacheProperties {

    /**
     * Whether the response cache is enabled.
     */
    private boolean enabled = true;

    /**
     * Time-to-live for cache entries in seconds.
     */
    private long ttlSeconds = 300;

    /**
     * Maximum number of entries in the cache.
     */
    private int maxSize = 1000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}