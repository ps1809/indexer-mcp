package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RepositorySummaryRequest}.
 */
class RepositorySummaryRequestTest {

    @Test
    void testDefaultConstructor() {
        RepositorySummaryRequest request = new RepositorySummaryRequest();
        assertNull(request.getRepositoryName());
        assertNull(request.getBranch());
    }

    @Test
    void testParameterizedConstructor() {
        RepositorySummaryRequest request = new RepositorySummaryRequest("my-repo", "develop");
        assertEquals("my-repo", request.getRepositoryName());
        assertEquals("develop", request.getBranch());
    }

    @Test
    void testSetters() {
        RepositorySummaryRequest request = new RepositorySummaryRequest();
        request.setRepositoryName("test-repo");
        request.setBranch("main");
        assertEquals("test-repo", request.getRepositoryName());
        assertEquals("main", request.getBranch());
    }

    @Test
    void testToString() {
        RepositorySummaryRequest request = new RepositorySummaryRequest("my-repo", "feature-branch");
        String str = request.toString();
        assertTrue(str.contains("my-repo"));
        assertTrue(str.contains("feature-branch"));
        assertTrue(str.startsWith("RepositorySummaryRequest{"));
    }

    @Test
    void testWithNullBranch() {
        RepositorySummaryRequest request = new RepositorySummaryRequest("repo", null);
        assertEquals("repo", request.getRepositoryName());
        assertNull(request.getBranch());
    }

    @Test
    void testWithEmptyBranch() {
        RepositorySummaryRequest request = new RepositorySummaryRequest("repo", "");
        assertEquals("repo", request.getRepositoryName());
        assertEquals("", request.getBranch());
    }
}