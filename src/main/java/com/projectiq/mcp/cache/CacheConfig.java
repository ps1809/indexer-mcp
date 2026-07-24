package com.projectiq.mcp.cache;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the response cache.
 * Enables cache properties and creates the cache service bean.
 */
@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig {

    @Bean
    public IndexerResponseCache indexerResponseCache(CacheProperties cacheProperties) {
        return new IndexerResponseCache(cacheProperties);
    }
}