package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.PackageSummary;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositorySummaryToolTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    @InjectMocks
    private RepositorySummaryTool repositorySummaryTool;

    private RepositorySummaryResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new RepositorySummaryResponse();
        mockResponse.setRepositoryName("test-repo");
        mockResponse.setBranch("main");
        mockResponse.setStatus("INDEXED");
        mockResponse.setCommitCount(100);
        mockResponse.setPackageCount(10);
        mockResponse.setClassCount(50);
        mockResponse.setMethodCount(200);
        mockResponse.setFileCount(75);
        mockResponse.setLastIndexedDate("2024-01-15T10:30:00");

        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.pkg");
        pkg.setClassCount(25);
        pkg.setMethodCount(100);
        mockResponse.setPackages(java.util.List.of(pkg));
    }

    @Test
    void repositorySummary_shouldReturnFormattedResponseOnSuccess() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(mockResponse);

        String result = repositorySummaryTool.repositorySummary("test-repo", "main");

        assertNotNull(result);
        assertTrue(result.contains("## Repository Summary"));
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("main"));
        assertTrue(result.contains("INDEXED"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("10"));
        assertTrue(result.contains("50"));
        assertTrue(result.contains("200"));
        assertTrue(result.contains("75"));
        assertTrue(result.contains("com.example.pkg"));
        verify(indexerRestClient).getRepositorySummary(any(RepositorySummaryRequest.class));
    }

    @Test
    void repositorySummary_shouldReturnFormattedResponseWithNullBranch() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(mockResponse);

        String result = repositorySummaryTool.repositorySummary("test-repo", null);

        assertNotNull(result);
        assertTrue(result.contains("main"));
    }

    @Test
    void repositorySummary_shouldReturnFormattedResponseWithoutPackages() {
        mockResponse.setPackages(null);
        
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(mockResponse);

        String result = repositorySummaryTool.repositorySummary("test-repo", null);

        assertNotNull(result);
        assertTrue(result.contains("## Repository Summary"));
        assertFalse(result.contains("### Packages"));
    }

    @Test
    void repositorySummary_shouldReturnErrorMessageOnTimeout() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new IndexerTimeoutException("Connection timed out"));

        String result = repositorySummaryTool.repositorySummary("test-repo", "main");

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("timed out"));
    }

    @Test
    void repositorySummary_shouldReturnErrorMessageOnConnectionFailure() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new IndexerConnectionException("Connection refused"));

        String result = repositorySummaryTool.repositorySummary("test-repo", "main");

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Cannot connect"));
    }

    @Test
    void repositorySummary_shouldReturnErrorMessageOnClientException() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new IndexerClientException("Invalid response"));

        String result = repositorySummaryTool.repositorySummary("test-repo", "main");

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Invalid response"));
    }

    @Test
    void repositorySummary_shouldReturnErrorMessageOnUnexpectedException() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = repositorySummaryTool.repositorySummary("test-repo", "main");

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("unexpected error"));
    }

    @Test
    void repositorySummary_shouldIncludeLastIndexedInResponse() {
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(mockResponse);

        String result = repositorySummaryTool.repositorySummary("test-repo", null);

        assertTrue(result.contains("2024-01-15T10:30:00"));
    }

    @Test
    void repositorySummary_shouldReturnNullLastIndexedWhenNull() {
        mockResponse.setLastIndexedDate(null);
        
        when(indexerRestClient.getRepositorySummary(any(RepositorySummaryRequest.class)))
                .thenReturn(mockResponse);

        String result = repositorySummaryTool.repositorySummary("test-repo", null);

        assertTrue(result.contains("N/A"));
    }
}