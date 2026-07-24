package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RepositoryStatsRequest}.
 */
class RepositoryStatsRequestTest {

    @Test
    void testDefaultConstructor() {
        RepositoryStatsRequest request = new RepositoryStatsRequest();
        assertNull(request.getRepositoryName());
        assertNull(request.getBranch());
    }

    @Test
    void testParameterizedConstructor() {
        RepositoryStatsRequest request = new RepositoryStatsRequest("test-repo", "develop");
        assertEquals("test-repo", request.getRepositoryName());
        assertEquals("develop", request.getBranch());
    }

    @Test
    void testSettersAndGetters() {
        RepositoryStatsRequest request = new RepositoryStatsRequest();
        
        request.setRepositoryName("my-repo");
        assertEquals("my-repo", request.getRepositoryName());
        
        request.setBranch("main");
        assertEquals("main", request.getBranch());
    }

    @Test
    void testSetNullValues() {
        RepositoryStatsRequest request = new RepositoryStatsRequest("repo", "branch");
        
        request.setRepositoryName(null);
        assertNull(request.getRepositoryName());
        
        request.setBranch(null);
        assertNull(request.getBranch());
    }

    @Test
    void testToString() {
        RepositoryStatsRequest request = new RepositoryStatsRequest("test-repo", "feature-branch");
        String str = request.toString();
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("feature-branch"));
        assertTrue(str.startsWith("RepositoryStatsRequest{"));
    }

    @Test
    void testNullBranchInToString() {
        RepositoryStatsRequest request = new RepositoryStatsRequest("repo", null);
        String str = request.toString();
        assertTrue(str.contains("repo"));
        assertTrue(str.contains("null") || str.startsWith("RepositoryStatsRequest{"));
    }
}