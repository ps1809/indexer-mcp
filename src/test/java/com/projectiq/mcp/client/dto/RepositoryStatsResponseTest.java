package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RepositoryStatsResponse}.
 */
class RepositoryStatsResponseTest {

    @Test
    void testDefaultConstructor() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        assertNull(response.getRepositoryName());
        assertNull(response.getBranch());
        assertNull(response.getStatus());
        assertEquals(0L, response.getCommitCount());
        assertEquals(0L, response.getPackageCount());
        assertEquals(0L, response.getClassCount());
        assertEquals(0L, response.getMethodCount());
        assertEquals(0L, response.getFileCount());
        assertEquals(0L, response.getTotalLinesOfCode());
        assertNull(response.getLastIndexedDate());
        assertNull(response.getContributors());
        assertNull(response.getFileTypeStats());
    }

    @Test
    void testIsIndexedWithINDEXEDStatus() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        response.setStatus("INDEXED");
        assertTrue(response.isIndexed());
    }

    @Test
    void testIsIndexedWithNonIndexedImageStatus() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        response.setStatus("INDEXING");
        assertFalse(response.isIndexed());
    }

    @Test
    void testIsIndexedWithNullStatus() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        assertFalse(response.isIndexed());
    }

    @Test
    void testSetAndGetContributors() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        
        ContributorStats contributor1 = new ContributorStats();
        contributor1.setAuthor("John Doe");
        contributor1.setCommitCount(50);
        
        ContributorStats contributor2 = new ContributorStats();
        contributor2.setAuthor("Jane Smith");
        contributor2.setCommitCount(30);
        
        List<ContributorStats> contributors = Arrays.asList(contributor1, contributor2);
        response.setContributors(contributors);
        
        assertEquals(2, response.getContributors().size());
        assertEquals("John Doe", response.getContributors().get(0).getAuthor());
        assertEquals(50L, response.getContributors().get(0).getCommitCount());
    }

    @Test
    void testSetNullContributors() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        response.setContributors(null);
        assertNull(response.getContributors());
    }

    @Test
    void testSetAndGetFileTypeStats() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        
        FileTypeStats javaStats = new FileTypeStats();
        javaStats.setFileType("java");
        javaStats.setFileCount(100);
        javaStats.setTotalLinesOfCode(5000);
        
        FileTypeStats xmlStats = new FileTypeStats();
        xmlStats.setFileType("xml");
        xmlStats.setFileCount(20);
        xmlStats.setTotalLinesOfCode(800);
        
        List<FileTypeStats> fileTypeStats = Arrays.asList(javaStats, xmlStats);
        response.setFileTypeStats(fileTypeStats);
        
        assertEquals(2, response.getFileTypeStats().size());
        assertEquals("java", response.getFileTypeStats().get(0).getFileType());
        assertEquals(100L, response.getFileTypeStats().get(0).getFileCount());
    }

    @Test
    void testSetNullFileTypeStats() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        response.setFileTypeStats(null);
        assertNull(response.getFileTypeStats());
    }

    @Test
    void testToStringWithAllFields() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setStatus("INDEXED");
        response.setCommitCount(500);
        response.setPackageCount(50);
        response.setClassCount(200);
        response.setMethodCount(1000);
        response.setFileCount(300);
        response.setTotalLinesOfCode(25000);
        response.setLastIndexedDate("2024-01-15T10:30:00");

        String str = response.toString();
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("main"));
        assertTrue(str.contains("INDEXED"));
        assertTrue(str.contains("500"));
        assertTrue(str.contains("25000"));
        assertTrue(str.contains("2024-01-15T10:30:00"));
    }

    @Test
    void testBranchNullInToString() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        response.setRepositoryName("repo");
        
        String str = response.toString();
        assertTrue(str.contains("repo"));
    }
}