package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RestApiResponse;
import com.projectiq.mcp.client.dto.RestApiRequest;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
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
class FindRestApiToolTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    @InjectMocks
    private FindRestApiTool findRestApiTool;

    private RestApiResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new RestApiResponse();
        mockResponse.setRepositoryName("test-repo");
        mockResponse.setTotalResults(2);

        List<RestEndpointInfo> endpoints = List.of(createEndpoint("/api/v1/users", "GET", "UserController", "findAllUsers"),
                createEndpoint("/api/v1/orders", "POST", "OrderController", "createOrder"));
        mockResponse.setEndpoints(endpoints);
    }

    private RestEndpointInfo createEndpoint(String path, String method, String controller, String methodName) {
        RestEndpointInfo endpoint = new RestEndpointInfo();
        endpoint.setEndpointPath(path);
        endpoint.setHttpMethod(method);
        endpoint.setControllerName(controller);
        endpoint.setMethodName(methodName);
        endpoint.setRequestMapping("@GetMapping");
        endpoint.setResponse_type("String");
        endpoint.setPackageName("com.example.controller");
        endpoint.setFilePath("src/main/java/com/example/controller/ExampleController.java");
        endpoint.setLineNumber(42);
        return endpoint;
    }

    @Test
    void testFindRestApi_successWithAllParameters() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(mockResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET,POST",
                "UserController",
                "main",
                "com.example.controller"
        );

        assertNotNull(result);
        assertTrue(result.contains("REST API Endpoints"));
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("2"));
        assertTrue(result.contains("GET"));
        assertTrue(result.contains("/api/v1/users"));
        verify(indexerRestClient).findRestApi(argThat(request ->
                "test-repo".equals(request.getRepositoryName()) &&
                        request.getHttpMethods().contains("GET") &&
                        request.getHttpMethods().contains("POST") &&
                        "main".equals(request.getBranch()) &&
                        "com.example.controller".equals(request.getPackageName())
        ));
    }

    @Test
    void testFindRestApi_successWithNoHttpMethods() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(mockResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                null,
                null,
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.contains("REST API Endpoints"));
        verify(indexerRestClient).findRestApi(argThat(request ->
                request.getHttpMethods().size() == 5 // All supported methods
        ));
    }

    @Test
    void testFindRestApi_successWithEmptyHttpMethods() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(mockResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "",
                null,
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.contains("REST API Endpoints"));
    }

    @Test
    void testFindRestApi_successWithSingleHttpMethod() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(mockResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "DELETE",
                null,
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.contains("REST API Endpoints"));
        verify(indexerRestClient).findRestApi(argThat(request ->
                request.getHttpMethods().contains("DELETE") &&
                        request.getHttpMethods().size() == 1
        ));
    }

    @Test
    void testFindRestApi_controllerFilterNoMatch() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(mockResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET",
                "NonExistentController",
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.contains("No endpoints found for controller: NonExistentController"));
    }

    @Test
    void testFindRestApi_controllerFilterWithMatch() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(mockResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET",
                "UserController",
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.contains("UserController"));
    }

    @Test
    void testFindRestApi_indexerTimeout() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenThrow(new IndexerTimeoutException("Connection timed out"));

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET",
                null,
                null,
                null
        );

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("timed out"));
    }

    @Test
    void testFindRestApi_connectionFailure() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenThrow(new IndexerConnectionException("Connection refused"));

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET",
                null,
                null,
                null
        );

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Cannot connect"));
    }

    @Test
    void testFindRestApi_genericClientException() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenThrow(new IndexerClientException("Invalid response"));

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET",
                null,
                null,
                null
        );

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Invalid response"));
    }

    @Test
    void testFindRestApi_unexpectedException() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET",
                null,
                null,
                null
        );

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("unexpected error"));
    }

    @Test
    void testFindRestApi_nullRepositoryName() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(mockResponse);

        String result = findRestApiTool.findRestApi(
                null,
                "GET",
                null,
                null,
                null
        );

        assertNotNull(result);
        verify(indexerRestClient).findRestApi(argThat(request ->
                request.getRepositoryName() == null
        ));
    }

    @Test
    void testFindRestApi_caseInsensitiveHttpMethods() {
        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(mockResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "get,post",
                null,
                null,
                null
        );

        assertNotNull(result);
        verify(indexerRestClient).findRestApi(argThat(request ->
                request.getHttpMethods().contains("GET") &&
                        request.getHttpMethods().contains("POST")
        ));
    }

    @Test
    void testFindRestApi_successWithNullEndpoints() {
        RestApiResponse nullEndpointsResponse = new RestApiResponse();
        nullEndpointsResponse.setRepositoryName("test-repo");
        nullEndpointsResponse.setTotalResults(0);
        nullEndpointsResponse.setEndpoints(null);

        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(nullEndpointsResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET",
                null,
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.contains("No REST API endpoints found"));
    }

    @Test
    void testFindRestApi_successWithNullTotalResults() {
        RestApiResponse nullTotalResponse = new RestApiResponse();
        nullTotalResponse.setRepositoryName("test-repo");
        nullTotalResponse.setTotalResults(null);
        nullTotalResponse.setEndpoints(List.of());

        when(indexerRestClient.findRestApi(any(RestApiRequest.class))).thenReturn(nullTotalResponse);

        String result = findRestApiTool.findRestApi(
                "test-repo",
                "GET",
                null,
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.contains("0"));
    }
}