package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestApiResponseTest {

    @Test
    void testDefaultConstructor() {
        RestApiResponse response = new RestApiResponse();
        assertNotNull(response);
    }

    @Test
    void testAllSettersAndGetters() {
        RestApiResponse response = new RestApiResponse();
        
        List<RestEndpointInfo> endpoints = List.of(new RestEndpointInfo());
        endpoints.get(0).setEndpointPath("/api/v1/users");
        endpoints.get(0).setHttpMethod("GET");

        response.setRepositoryName("test-repo");
        response.setTotalResults(10);
        response.setEndpoints(endpoints);

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals(Integer.valueOf(10), response.getTotalResults());
        assertNotNull(response.getEndpoints());
        assertEquals(1, response.getEndpoints().size());
    }

    @Test
    void testNullValues() {
        RestApiResponse response = new RestApiResponse();
        
        assertNull(response.getRepositoryName());
        assertNull(response.getTotalResults());
        assertNull(response.getEndpoints());
    }

    @Test
    void testEmptyEndpoints() {
        RestApiResponse response = new RestApiResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(0);
        response.setEndpoints(List.of());

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals(Integer.valueOf(0), response.getTotalResults());
        assertTrue(response.getEndpoints().isEmpty());
    }

    @Test
    void testToString() {
        RestApiResponse response = new RestApiResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(5);

        List<RestEndpointInfo> endpoints = List.of(new RestEndpointInfo());
        endpoints.get(0).setEndpointPath("/api/v1/users");
        response.setEndpoints(endpoints);

        String str = response.toString();
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("5"));
    }
}