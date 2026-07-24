package com.projectiq.mcp.client;

import com.projectiq.mcp.client.dto.IndexerHealthResponse;
import com.projectiq.mcp.client.dto.RepositoryStatsRequest;
import com.projectiq.mcp.client.dto.RepositoryStatsResponse;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class IndexerRestClientImplTest {

    private MockRestServiceServer mockServer;
    private IndexerRestClientImpl indexerRestClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:8081");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        indexerRestClient = new IndexerRestClientImpl(builder.build());
    }

    @Test
    void checkHealth_shouldReturnHealthResponse() {
        String responseBody = """
                {
                    "status": "UP",
                    "version": "1.0.0"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        IndexerHealthResponse response = indexerRestClient.checkHealth();

        assertNotNull(response);
        assertEquals("UP", response.getStatus());
        assertEquals("1.0.0", response.getVersion());
        assertTrue(response.isHealthy());
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn4xx() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest().body("Bad Request").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(400, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("400"));
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn5xx() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("Internal Server Error").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(500, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("500"));
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldThrowIndexerHttpExceptionOn404() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("Not Found").contentType(MediaType.TEXT_PLAIN));

        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.checkHealth());

        assertEquals(404, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void isReachable_shouldReturnTrueWhenServerResponds() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        boolean reachable = indexerRestClient.isReachable();

        assertTrue(reachable);
        mockServer.verify();
    }

    @Test
    void isReachable_shouldReturnFalseOnHttpError() {
        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("Error").contentType(MediaType.TEXT_PLAIN));

        boolean reachable = indexerRestClient.isReachable();

        assertFalse(reachable);
        mockServer.verify();
    }

    @Test
    void checkHealth_shouldHandleDeserializationError() {
        String invalidJson = "not valid json {{{";

        mockServer.expect(requestTo("http://localhost:8081/actuator/health"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        assertThrows(IndexerClientException.class, () -> indexerRestClient.checkHealth());
        mockServer.verify();
    }

    @Test
    void getRepositorySummary_shouldReturnSummaryResponse() {
        String responseBody = """
                {
                    "repositoryName": "test-repo",
                    "branch": "main",
                    "status": "INDEXED",
                    "commitCount": 100,
                    "packageCount": 10,
                    "classCount": 50,
                    "methodCount": 200,
                    "fileCount": 75,
                    "lastIndexedDate": "2024-01-15T10:30:00"
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/summary?branch=main"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        RepositorySummaryRequest request = new RepositorySummaryRequest("test-repo", "main");
        RepositorySummaryResponse response = indexerRestClient.getRepositorySummary(request);

        assertNotNull(response);
        assertEquals("test-repo", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertEquals("INDEXED", response.getStatus());
        assertEquals(100L, response.getCommitCount());
        assertEquals(10L, response.getPackageCount());
        assertEquals(50L, response.getClassCount());
        assertEquals(200L, response.getMethodCount());
        assertEquals(75L, response.getFileCount());
        assertEquals("2024-01-15T10:30:00", response.getLastIndexedDate());
        assertTrue(response.isIndexed());
        mockServer.verify();
    }

    @Test
    void getRepositorySummary_shouldThrowOnNullResponse() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/summary"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound().body("Not Found").contentType(MediaType.TEXT_PLAIN));

        RepositorySummaryRequest request = new RepositorySummaryRequest("test-repo", null);
        
        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.getRepositorySummary(request));

        assertEquals(404, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getRepositorySummary_shouldThrowOnDeserializationError() {
        String invalidJson = "not valid json {{{";

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/summary"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        RepositorySummaryRequest request = new RepositorySummaryRequest("test-repo", null);
        
        IndexerClientException exception = assertThrows(IndexerClientException.class,
                () -> indexerRestClient.getRepositorySummary(request));

        assertTrue(exception.getMessage().contains("deserialize"));
        mockServer.verify();
    }

    @Test
    void getRepositoryStatistics_shouldReturnStatsResponse() {
        String responseBody = """
                {
                    "repositoryName": "test-repo",
                    "branch": "main",
                    "status": "INDEXED",
                    "commitCount": 500,
                    "packageCount": 50,
                    "classCount": 200,
                    "methodCount": 1000,
                    "fileCount": 300,
                    "totalLinesOfCode": 25000,
                    "lastIndexedDate": "2024-01-15T10:30:00",
                    "contributors": [
                        {
                            "author": "John Doe",
                            "commitCount": 200,
                            "lastActiveDate": "2024-01-14"
                        },
                        {
                            "author": "Jane Smith",
                            "commitCount": 150,
                            "lastActiveDate": "2024-01-13"
                        }
                    ],
                    "fileTypeStats": [
                        {
                            "fileType": "java",
                            "fileCount": 200,
                            "totalLinesOfCode": 20000
                        },
                        {
                            "fileType": "xml",
                            "fileCount": 50,
                            "totalLinesOfCode": 3000
                        }
                    ]
                }
                """;

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/stats?branch=main"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        RepositoryStatsRequest request = new RepositoryStatsRequest("test-repo", "main");
        RepositoryStatsResponse response = indexerRestClient.getRepositoryStatistics(request);

        assertNotNull(response);
        assertEquals("test-repo", response.getRepositoryName());
        assertEquals("main", response.getBranch());
        assertEquals("INDEXED", response.getStatus());
        assertEquals(500L, response.getCommitCount());
        assertEquals(50L, response.getPackageCount());
        assertEquals(200L, response.getClassCount());
        assertEquals(1000L, response.getMethodCount());
        assertEquals(300L, response.getFileCount());
        assertEquals(25000L, response.getTotalLinesOfCode());
        assertEquals("2024-01-15T10:30:00", response.getLastIndexedDate());
        assertTrue(response.isIndexed());
        assertEquals(2, response.getContributors().size());
        assertEquals("John Doe", response.getContributors().get(0).getAuthor());
        assertEquals(200L, response.getContributors().get(0).getCommitCount());
        assertEquals("java", response.getFileTypeStats().get(0).getFileType());
        assertEquals(200L, response.getFileTypeStats().get(0).getFileCount());
        mockServer.verify();
    }

    @Test
    void getRepositoryStatistics_shouldThrowOnHttpError() {
        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/stats"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError().body("Internal Server Error").contentType(MediaType.TEXT_PLAIN));

        RepositoryStatsRequest request = new RepositoryStatsRequest("test-repo", null);
        
        IndexerHttpException exception = assertThrows(IndexerHttpException.class,
                () -> indexerRestClient.getRepositoryStatistics(request));

        assertEquals(500, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void getRepositoryStatistics_shouldThrowOnDeserializationError() {
        String invalidJson = "not valid json {{{";

        mockServer.expect(requestTo("http://localhost:8081/api/v1/indexer/test-repo/stats"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(invalidJson, MediaType.APPLICATION_JSON));

        RepositoryStatsRequest request = new RepositoryStatsRequest("test-repo", null);
        
        IndexerClientException exception = assertThrows(IndexerClientException.class,
                () -> indexerRestClient.getRepositoryStatistics(request));

        assertTrue(exception.getMessage().contains("deserialize"));
        mockServer.verify();
    }
}
