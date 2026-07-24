package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchCodeResponseTest {

    @Test
    void testSettersAndGetters() {
        SearchCodeResponse response = new SearchCodeResponse();
        
        SearchResult result = new SearchResult();
        result.setName("testMethod");
        
        response.setRepositoryName("test-repo");
        response.setQuery("findMethod");
        response.setTotalResults(10L);
        response.setResults(List.of(result));

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals("findMethod", response.getQuery());
        assertEquals(Long.valueOf(10), response.getTotalResults());
        assertNotNull(response.getResults());
        assertEquals(1, response.getResults().size());
        assertEquals("testMethod", response.getResults().get(0).getName());
    }

    @Test
    void testNullValues() {
        SearchCodeResponse response = new SearchCodeResponse();
        
        assertNull(response.getRepositoryName());
        assertNull(response.getQuery());
        assertNull(response.getTotalResults());
        assertNull(response.getResults());
    }

    @Test
    void testToString() {
        SearchCodeResponse response = new SearchCodeResponse();
        response.setRepositoryName("test-repo");
        response.setQuery("search");
        response.setTotalResults(5L);

        String toString = response.toString();
        assertTrue(toString.contains("test-repo"));
        assertTrue(toString.contains("search"));
        assertTrue(toString.contains("5"));
    }
}