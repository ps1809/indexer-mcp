package com.projectiq.mcp.client.service;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.config.IndexerProperties;
import com.projectiq.mcp.client.dto.IndexerHealthResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexerConnectivityServiceTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    private IndexerProperties indexerProperties;
    private IndexerConnectivityService connectivityService;

    @BeforeEach
    void setUp() {
        indexerProperties = new IndexerProperties();
        indexerProperties.setBaseUrl("http://localhost:8081");
        indexerProperties.setConnectTimeout(5000);
        indexerProperties.setReadTimeout(30000);
        connectivityService = new IndexerConnectivityService(indexerRestClient, indexerProperties);
    }

    @Test
    void verifyConnectivity_shouldReturnTrueWhenReachable() {
        when(indexerRestClient.isReachable()).thenReturn(true);

        boolean result = connectivityService.verifyConnectivity();

        assertTrue(result);
        verify(indexerRestClient).isReachable();
    }

    @Test
    void verifyConnectivity_shouldReturnFalseWhenNotReachable() {
        when(indexerRestClient.isReachable()).thenReturn(false);

        boolean result = connectivityService.verifyConnectivity();

        assertFalse(result);
        verify(indexerRestClient).isReachable();
    }

    @Test
    void checkHealth_shouldReturnHealthResponse() {
        IndexerHealthResponse healthResponse = new IndexerHealthResponse("UP", "1.0.0");
        when(indexerRestClient.checkHealth()).thenReturn(healthResponse);

        IndexerHealthResponse result = connectivityService.checkHealth();

        assertNotNull(result);
        assertEquals("UP", result.getStatus());
        assertEquals("1.0.0", result.getVersion());
        assertTrue(result.isHealthy());
        verify(indexerRestClient).checkHealth();
    }

    @Test
    void checkHealth_shouldPropagateException() {
        when(indexerRestClient.checkHealth())
                .thenThrow(new IndexerConnectionException("Connection refused"));

        assertThrows(IndexerConnectionException.class, () -> connectivityService.checkHealth());
        verify(indexerRestClient).checkHealth();
    }

    @Test
    void checkHealth_shouldPropagateClientException() {
        when(indexerRestClient.checkHealth())
                .thenThrow(new IndexerClientException("Generic error"));

        assertThrows(IndexerClientException.class, () -> connectivityService.checkHealth());
        verify(indexerRestClient).checkHealth();
    }
}