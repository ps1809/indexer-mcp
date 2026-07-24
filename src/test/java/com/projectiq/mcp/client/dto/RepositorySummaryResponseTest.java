package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RepositorySummaryResponse}.
 */
class RepositorySummaryResponseTest {

    @Test
    void testDefaultConstructor() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        assertNull(response.getRepositoryName());
        assertNull(response.getBranch());
        assertNull(response.getStatus());
        assertEquals(0L, response.getCommitCount());
        assertEquals(0L, response.getPackageCount());
        assertEquals(0L, response.getClassCount());
        assertEquals(0L, response.getMethodCount());
        assertEquals(0L, response.getFileCount());
        assertNull(response.getLastIndexedDate());
        assertNull(response.getPackages());
    }

    @Test
    void testIsIndexedWithINDEXEDStatus() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        response.setStatus("INDEXED");
        assertTrue(response.isIndexed());
    }

    @Test
    void testIsIndexedWithNonIndexedImageStatus() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        response.setStatus("INDEXING");
        assertFalse(response.isIndexed());
    }

    @Test
    void testIsIndexedWithNullStatus() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        assertFalse(response.isIndexed());
    }

    @Test
    void testToStringWithAllFields() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setStatus("INDEXED");
        response.setCommitCount(100);
        response.setPackageCount(10);
        response.setClassCount(50);
        response.setMethodCount(200);
        response.setFileCount(75);
        response.setLastIndexedDate("2024-01-15T10:30:00");

        String str = response.toString();
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("main"));
        assertTrue(str.contains("INDEXED"));
        assertTrue(str.contains("100"));
        assertTrue(str.contains("10"));
        assertTrue(str.contains("50"));
        assertTrue(str.contains("200"));
        assertTrue(str.contains("75"));
        assertTrue(str.contains("2024-01-15T10:30:00"));
    }

    @Test
    void testSetAndGetPackages() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        
        PackageSummary pkg1 = new PackageSummary();
        pkg1.setPackageName("com.example.pkg1");
        
        PackageSummary pkg2 = new PackageSummary();
        pkg2.setPackageName("com.example.pkg2");
        
        List<PackageSummary> packages = Arrays.asList(pkg1, pkg2);
        response.setPackages(packages);
        
        assertEquals(2, response.getPackages().size());
        assertEquals("com.example.pkg1", response.getPackages().get(0).getPackageName());
    }

    @Test
    void testSetNullPackages() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        response.setPackages(null);
        assertNull(response.getPackages());
    }

    @Test
    void testBranchNullInToString() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        response.setRepositoryName("repo");
        
        String str = response.toString();
        assertTrue(str.contains("repo"));
        // The default toString includes all fields, so "null" will appear for null branch
        assertTrue(str.contains("null") || str.startsWith("RepositorySummaryResponse{"));
    }
}