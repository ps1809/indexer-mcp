package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchCodeRequestTest {

    @Test
    void testDefaultConstructor() {
        SearchCodeRequest request = new SearchCodeRequest();
        assertNull(request.getRepositoryName());
        assertNull(request.getQuery());
        assertNull(request.getBranch());
        assertNull(request.getPackageName());
        assertNull(request.getMaxResults());
    }

    @Test
    void testParameterizedConstructor() {
        SearchCodeRequest request = new SearchCodeRequest("test-repo", "findMethod");
        
        assertEquals("test-repo", request.getRepositoryName());
        assertEquals("findMethod", request.getQuery());
        assertNull(request.getBranch());
        assertNull(request.getPackageName());
        assertNull(request.getMaxResults());
    }

    @Test
    void testSetters() {
        SearchCodeRequest request = new SearchCodeRequest();
        
        request.setRepositoryName("my-repo");
        request.setQuery("searchQuery");
        request.setBranch("develop");
        request.setPackageName("com.example.pkg");
        request.setMaxResults(50);

        assertEquals("my-repo", request.getRepositoryName());
        assertEquals("searchQuery", request.getQuery());
        assertEquals("develop", request.getBranch());
        assertEquals("com.example.pkg", request.getPackageName());
        assertEquals(Integer.valueOf(50), request.getMaxResults());
    }

    @Test
    void testToString() {
        SearchCodeRequest request = new SearchCodeRequest("test-repo", "query");
        request.setBranch("main");
        request.setMaxResults(10);

        String toString = request.toString();
        assertTrue(toString.contains("SearchCodeRequest"));
        assertTrue(toString.contains("test-repo"));
        assertTrue(toString.contains("query"));
    }
}
