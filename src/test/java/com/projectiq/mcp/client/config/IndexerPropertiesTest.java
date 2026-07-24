package com.projectiq.mcp.client.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexerPropertiesTest {

    @Test
    void shouldHaveDefaultValues() {
        IndexerProperties properties = new IndexerProperties();

        assertEquals("http://localhost:8081", properties.getBaseUrl());
        assertEquals(5000, properties.getConnectTimeout());
        assertEquals(30000, properties.getReadTimeout());
    }

    @Test
    void shouldSetAndGetBaseUrl() {
        IndexerProperties properties = new IndexerProperties();
        properties.setBaseUrl("http://custom-host:9090");

        assertEquals("http://custom-host:9090", properties.getBaseUrl());
    }

    @Test
    void shouldSetAndGetConnectTimeout() {
        IndexerProperties properties = new IndexerProperties();
        properties.setConnectTimeout(10000);

        assertEquals(10000, properties.getConnectTimeout());
    }

    @Test
    void shouldSetAndGetReadTimeout() {
        IndexerProperties properties = new IndexerProperties();
        properties.setReadTimeout(60000);

        assertEquals(60000, properties.getReadTimeout());
    }
}