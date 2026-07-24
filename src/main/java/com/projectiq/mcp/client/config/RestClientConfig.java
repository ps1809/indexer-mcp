package com.projectiq.mcp.client.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration for the REST client used to communicate with ProjectIQ Indexer.
 */
@Configuration
@EnableConfigurationProperties(IndexerProperties.class)
public class RestClientConfig {

    private final IndexerProperties indexerProperties;

    public RestClientConfig(IndexerProperties indexerProperties) {
        this.indexerProperties = indexerProperties;
    }

    /**
     * Creates a RestClient configured with the Indexer base URL and timeouts.
     *
     * @return configured RestClient instance
     */
    @Bean
    public RestClient indexerRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(indexerProperties.getConnectTimeout());
        requestFactory.setReadTimeout(indexerProperties.getReadTimeout());

        return RestClient.builder()
                .baseUrl(indexerProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}