package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.SearchCodeRequest;
import com.projectiq.mcp.client.dto.SearchCodeResponse;
import com.projectiq.mcp.client.dto.SearchResult;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchCodeToolTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    @InjectMocks
    private SearchCodeTool searchCodeTool;

    private SearchCodeResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new SearchCodeResponse();
        mockResponse.setRepositoryName("test-repo");
        mockResponse.setQuery("findMethod");
        mockResponse.setTotalResults(2L);

        SearchResult result1 = new SearchResult();
        result1.setName("myMethod");
        result1.setType("METHOD");
        result1.setClassName("MyClass");
        result1.setPackageName("com.example.pkg");
        result1.setFilePath("src/main/java/com/example/MyClass.java");
        result1.setLineNumber(42);
        result1.setDescription("Sample method description");
        result1.setSnippet("public void myMethod() {\n    // implementation\n}");

        SearchResult result2 = new SearchResult();
        result2.setName("findUser");
        result2.setType("METHOD");
        result2.setClassName("UserService");
        result2.setPackageName("com.example.service");
        result2.setFilePath("src/main/java/com/example/service/UserService.java");
        result2.setLineNumber(105);
        result2.setDescription("Finds a user by ID");
        result2.setSnippet("public User findUser(Long id) {\n    return repository.findById(id);\n}");

        mockResponse.setResults(List.of(result1, result2));
    }

    @Test
    void searchCode_shouldReturnFormattedResponseOnSuccess() {
        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenReturn(mockResponse);

        String result = searchCodeTool.searchCode("test-repo", "findMethod", "main", "com.example", 10);

        assertNotNull(result);
        assertTrue(result.contains("## Code Search Results"));
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("findMethod"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("METHOD"));
        assertTrue(result.contains("myMethod"));
        assertTrue(result.contains("MyClass"));
        assertTrue(result.contains("com.example.pkg"));
        assertTrue(result.contains("src/main/java/com/example/MyClass.java:42"));
        assertTrue(result.contains("Sample method description"));
        verify(indexerRestClient).searchCode(any(SearchCodeRequest.class));
    }

    @Test
    void searchCode_shouldWorkWithOptionalParameters() {
        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenReturn(mockResponse);

        String result = searchCodeTool.searchCode("test-repo", "query", null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("## Code Search Results"));
        verify(indexerRestClient).searchCode(argThat(request ->
                request.getRepositoryName().equals("test-repo") &&
                request.getQuery().equals("query") &&
                request.getBranch() == null &&
                request.getPackageName() == null &&
                request.getMaxResults() == null
        ));
    }

    @Test
    void searchCode_shouldReturnErrorMessageOnTimeout() {
        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenThrow(new IndexerTimeoutException("Connection timed out"));

        String result = searchCodeTool.searchCode("test-repo", "query", null, null, null);

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("timed out"));
    }

    @Test
    void searchCode_shouldReturnErrorMessageOnConnectionFailure() {
        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenThrow(new IndexerConnectionException("Connection refused"));

        String result = searchCodeTool.searchCode("test-repo", "query", null, null, null);

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Cannot connect"));
    }

    @Test
    void searchCode_shouldReturnErrorMessageOnClientException() {
        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenThrow(new IndexerClientException("Invalid response"));

        String result = searchCodeTool.searchCode("test-repo", "query", null, null, null);

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Invalid response"));
    }

    @Test
    void searchCode_shouldReturnErrorMessageOnUnexpectedException() {
        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = searchCodeTool.searchCode("test-repo", "query", null, null, null);

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("unexpected error"));
    }

    @Test
    void searchCode_shouldReturnNoResultsWhenEmptyList() {
        SearchCodeResponse emptyResponse = new SearchCodeResponse();
        emptyResponse.setRepositoryName("test-repo");
        emptyResponse.setQuery("nonexistent");
        emptyResponse.setTotalResults(0L);
        emptyResponse.setResults(List.of());

        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenReturn(emptyResponse);

        String result = searchCodeTool.searchCode("test-repo", "nonexistent", null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("No results found"));
    }

    @Test
    void searchCode_shouldReturnNoResultsWhenNullResults() {
        SearchCodeResponse nullResultsResponse = new SearchCodeResponse();
        nullResultsResponse.setRepositoryName("test-repo");
        nullResultsResponse.setQuery("query");
        nullResultsResponse.setTotalResults(0L);
        nullResultsResponse.setResults(null);

        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenReturn(nullResultsResponse);

        String result = searchCodeTool.searchCode("test-repo", "query", null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("No results found"));
    }

    @Test
    void searchCode_shouldHandleNullTotalResults() {
        SearchCodeResponse response = new SearchCodeResponse();
        response.setRepositoryName("test-repo");
        response.setQuery("query");
        response.setTotalResults(null);
        response.setResults(List.of());

        when(indexerRestClient.searchCode(any(SearchCodeRequest.class)))
                .thenReturn(response);

        String result = searchCodeTool.searchCode("test-repo", "query", null, null, null);

        assertNotNull(result);
    }
}