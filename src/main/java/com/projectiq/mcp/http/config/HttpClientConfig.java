package com.projectiq.mcp.http.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration for the generic HTTP client.
 * This is completely generic and does not reference any specific service.
 */
@Configuration
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientConfig {

    @Bean
    public RestClient httpClientHttpClient(HttpClientProperties properties) {
        Duration connectTimeout = Duration.ofMillis(properties.getConnectTimeout());

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
            java.net.http.HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build()
        );

        ObjectMapper mapper = createObjectMapper();

        return RestClient.builder()
            .requestFactory(requestFactory)
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .messageConverters(converters -> converters.add(
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(mapper)))
            .build();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
}