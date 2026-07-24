package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.MethodParameter;
import com.projectiq.mcp.client.dto.MethodRequest;
import com.projectiq.mcp.client.dto.MethodResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FindMethodTool MCP tool.
 */
class FindMethodToolTest {

    private IndexerRestClient mockIndexerClient;
    private FindMethodTool findMethodTool;

    @BeforeEach
    void setUp() {
        mockIndexerClient = mock(IndexerRestClient.class);
        findMethodTool = new FindMethodTool(mockIndexerClient);
    }

    // === Success Tests ===

    @Test
    void findMethod_shouldReturnMethodMetadataOnSuccess() throws Exception {
        MethodInfo methodInfo = createMockMethodInfo();
        MethodResponse response = new MethodResponse();
        response.setRepositoryName("spring-framework");
        response.setTotalResults(1);
        response.setMethods(Arrays.asList(methodInfo));

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        String result = findMethodTool.findMethod(
                "spring-framework",
                "doDispatch",
                "org.springframework.web.servlet",
                "main"
        );

        assertNotNull(result);
        ObjectMapper mapper = new ObjectMapper();
        FindMethodTool.MethodSearchResult parsed = mapper.readValue(result, FindMethodTool.MethodSearchResult.class);
        assertEquals("spring-framework", parsed.getRepositoryName());
        assertEquals(Integer.valueOf(1), parsed.getTotalResults());
        assertEquals(1, parsed.getMethods().size());
        assertEquals("doDispatch", parsed.getMethods().get(0).getMethodName());
        assertEquals("org.springframework.web.servlet.DispatcherServlet.doDispatch", parsed.getMethods().get(0).getFullyQualifiedName());
        assertEquals("DispatcherServlet", parsed.getMethods().get(0).getDeclaringClass());
    }

    @Test
    void findMethod_shouldShowStaticMethodCorrectly() throws Exception {
        MethodInfo methodInfo = createMockMethodInfo();
        methodInfo.setStaticFlag(Boolean.TRUE);
        methodInfo.setMethodName("valueOf");

        MethodResponse response = new MethodResponse();
        response.setRepositoryName("java-core");
        response.setTotalResults(1);
        response.setMethods(Arrays.asList(methodInfo));

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        String result = findMethodTool.findMethod("java-core", "valueOf", null, null);

        assertNotNull(result);
        ObjectMapper mapper = new ObjectMapper();
        FindMethodTool.MethodSearchResult parsed = mapper.readValue(result, FindMethodTool.MethodSearchResult.class);
        assertEquals("true", parsed.getMethods().get(0).getStatic());
    }

    @Test
    void findMethod_shouldShowAbstractMethodCorrectly() throws Exception {
        MethodInfo methodInfo = createMockMethodInfo();
        methodInfo.setAbstractFlag(Boolean.TRUE);
        methodInfo.setMethodName("process");

        MethodResponse response = new MethodResponse();
        response.setRepositoryName("abstract-repo");
        response.setTotalResults(1);
        response.setMethods(Arrays.asList(methodInfo));

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        String result = findMethodTool.findMethod("abstract-repo", "process", null, null);

        assertNotNull(result);
        ObjectMapper mapper = new ObjectMapper();
        FindMethodTool.MethodSearchResult parsed = mapper.readValue(result, FindMethodTool.MethodSearchResult.class);
        assertEquals("true", parsed.getMethods().get(0).getAbstractFlag());
    }

    @Test
    void findMethod_shouldShowParameters() throws Exception {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("execute");
        methodInfo.setFullyQualifiedName("com.example.Task.execute");
        methodInfo.setDeclaringClass("Task");
        methodInfo.setPackageName("com.example");
        methodInfo.setReturnType("void");
        methodInfo.setVisibility("public");
        methodInfo.setStaticFlag(Boolean.FALSE);
        methodInfo.setAbstractFlag(Boolean.FALSE);
        methodInfo.setAnnotations(Collections.emptyList());

        MethodParameter param1 = new MethodParameter();
        param1.setName("taskName");
        param1.setType("String");

        MethodParameter param2 = new MethodParameter();
        param2.setName("priority");
        param2.setType("int");

        methodInfo.setParameters(Arrays.asList(param1, param2));

        MethodResponse response = new MethodResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        response.setMethods(Arrays.asList(methodInfo));

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        String result = findMethodTool.findMethod("test-repo", "execute", null, null);

        assertNotNull(result);
        ObjectMapper mapper = new ObjectMapper();
        FindMethodTool.MethodSearchResult parsed = mapper.readValue(result, FindMethodTool.MethodSearchResult.class);
        assertTrue(parsed.getMethods().get(0).getParameters().contains("taskName"));
        assertTrue(parsed.getMethods().get(0).getParameters().contains("String"));
    }

    @Test
    void findMethod_shouldHandleEmptyResults() throws Exception {
        MethodResponse response = new MethodResponse();
        response.setRepositoryName("empty-repo");
        response.setTotalResults(0);
        response.setMethods(Arrays.asList());

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        String result = findMethodTool.findMethod("empty-repo", "nonexistent", null, null);

        assertNotNull(result);
        ObjectMapper mapper = new ObjectMapper();
        FindMethodTool.EmptyMethodSearchResult parsed = mapper.readValue(result, FindMethodTool.EmptyMethodSearchResult.class);
        assertEquals("empty-repo", parsed.getRepositoryName());
        assertEquals(Integer.valueOf(0), parsed.getTotalResults());
    }

    @Test
    void findMethod_shouldReturnMultipleMethods() throws Exception {
        MethodInfo methodInfo1 = createMockMethodInfo();
        methodInfo1.setMethodName("doDispatch");

        MethodInfo methodInfo2 = new MethodInfo();
        methodInfo2.setMethodName("init");
        methodInfo2.setFullyQualifiedName("org.springframework.web.servlet.HttpServletBean.init");
        methodInfo2.setDeclaringClass("HttpServletBean");
        methodInfo2.setPackageName("org.springframework.web.servlet");
        methodInfo2.setReturnType("void");
        methodInfo2.setVisibility("public");
        methodInfo2.setStaticFlag(Boolean.FALSE);
        methodInfo2.setAbstractFlag(Boolean.FALSE);
        methodInfo2.setParameters(Collections.emptyList());
        methodInfo2.setAnnotations(Arrays.asList("@PostConstruct"));

        MethodResponse response = new MethodResponse();
        response.setRepositoryName("spring-framework");
        response.setTotalResults(2);
        response.setMethods(Arrays.asList(methodInfo1, methodInfo2));

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        String result = findMethodTool.findMethod("spring-framework", "do", null, null);

        assertNotNull(result);
        ObjectMapper mapper = new ObjectMapper();
        FindMethodTool.MethodSearchResult parsed = mapper.readValue(result, FindMethodTool.MethodSearchResult.class);
        assertEquals(Integer.valueOf(2), parsed.getTotalResults());
    }

    // === Error Handling Tests ===

    @Test
    void findMethod_shouldReturnErrorWhenRepositoryNameIsNull() {
        String result = findMethodTool.findMethod(null, "findUser", null, null);

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("repositoryName is required"));
    }

    @Test
    void findMethod_shouldReturnErrorWhenRepositoryNameIsEmpty() {
        String result = findMethodTool.findMethod("", "findUser", null, null);

        assertNotNull(result);
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("repositoryName is required"));
    }

    @Test
    void findMethod_shouldHandleConnectionFailure() {
        when(mockIndexerClient.findMethod(any(MethodRequest.class)))
                .thenThrow(new IndexerConnectionException("Connection refused"));

        String result = findMethodTool.findMethod("test-repo", "findUser", null, null);

        assertNotNull(result);
        assertTrue(result.contains("INDEXER_UNREACHABLE"));
        assertTrue(result.contains("Cannot connect to ProjectIQ Indexer"));
    }

    @Test
    void findMethod_shouldHandleTimeout() {
        when(mockIndexerClient.findMethod(any(MethodRequest.class)))
                .thenThrow(new IndexerTimeoutException("Read timed out"));

        String result = findMethodTool.findMethod("test-repo", "findUser", null, null);

        assertNotNull(result);
        assertTrue(result.contains("INDEXER_TIMEOUT"));
        assertTrue(result.contains("timed out"));
    }

    @Test
    void findMethod_shouldHandleHttpError() {
        when(mockIndexerClient.findMethod(any(MethodRequest.class)))
                .thenThrow(new IndexerHttpException("Indexer HTTP error: 500 - Internal Server Error", 500));

        String result = findMethodTool.findMethod("test-repo", "findUser", null, null);

        assertNotNull(result);
        assertTrue(result.contains("INDEXER_HTTP_ERROR"));
        assertTrue(result.contains("Indexer HTTP error"));
    }

    @Test
    void findMethod_shouldHandleHttpClientException() {
        when(mockIndexerClient.findMethod(any(MethodRequest.class)))
                .thenThrow(new IndexerClientException("Failed to deserialize response"));

        String result = findMethodTool.findMethod("test-repo", "findUser", null, null);

        assertNotNull(result);
        assertTrue(result.contains("INDEXER_ERROR"));
        assertTrue(result.contains("Indexer client error"));
    }

    @Test
    void findMethod_shouldHandleUnexpectedException() {
        when(mockIndexerClient.findMethod(any(MethodRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = findMethodTool.findMethod("test-repo", "findUser", null, null);

        assertNotNull(result);
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Internal error"));
    }

    // === Optional Parameter Tests ===

    @Test
    void findMethod_shouldWorkWithNullOptionalParameters() throws Exception {
        MethodInfo methodInfo = createMockMethodInfo();
        MethodResponse response = new MethodResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        response.setMethods(Arrays.asList(methodInfo));

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        String result = findMethodTool.findMethod("test-repo", "findUser", null, null);

        assertNotNull(result);
    }

    @Test
    void findMethod_shouldIncludeOptionalFiltersWhenProvided() {
        MethodInfo methodInfo = createMockMethodInfo();
        MethodResponse response = new MethodResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        response.setMethods(Arrays.asList(methodInfo));

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        findMethodTool.findMethod(
                "test-repo",
                "findUser",
                "com.example.repository",
                "develop"
        );

        verify(mockIndexerClient).findMethod(argThat(request -> {
            assertEquals("test-repo", request.getRepositoryName());
            assertEquals("findUser", request.getMethodName());
            assertEquals("com.example.repository", request.getPackageName());
            assertEquals("develop", request.getBranch());
            return true;
        }));
    }

    // === Null Safety Tests ===

    @Test
    void findMethod_shouldHandleNullValuesInResponse() throws Exception {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("testMethod");
        methodInfo.setFullyQualifiedName(null);
        methodInfo.setDeclaringClass(null);
        methodInfo.setPackageName(null);
        methodInfo.setReturnType(null);
        methodInfo.setVisibility(null);
        methodInfo.setStaticFlag(null);
        methodInfo.setAbstractFlag(null);
        methodInfo.setAnnotations(null);
        methodInfo.setParameters(null);
        methodInfo.setSourceFileLocation(null);

        MethodResponse response = new MethodResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        response.setMethods(Arrays.asList(methodInfo));

        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(response);

        String result = findMethodTool.findMethod("test-repo", "testMethod", null, null);

        // Should not throw exception
        assertNotNull(result);
    }

    @Test
    void findMethod_shouldHandleNullResponse() {
        when(mockIndexerClient.findMethod(any(MethodRequest.class))).thenReturn(null);

        String result = findMethodTool.findMethod("test-repo", "findUser", null, null);

        assertNotNull(result);
        assertTrue(result.contains("INDEXER_ERROR"));
        assertTrue(result.contains("null response"));
    }

    // === Helper Methods ===

    private MethodInfo createMockMethodInfo() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("doDispatch");
        methodInfo.setFullyQualifiedName("org.springframework.web.servlet.DispatcherServlet.doDispatch");
        methodInfo.setDeclaringClass("DispatcherServlet");
        methodInfo.setPackageName("org.springframework.web.servlet");
        methodInfo.setReturnType("void");
        methodInfo.setVisibility("protected");
        methodInfo.setStaticFlag(Boolean.FALSE);
        methodInfo.setAbstractFlag(Boolean.FALSE);
        methodInfo.setAnnotations(Arrays.asList("@SuppressWarnings"));
        methodInfo.setSourceFileLocation("spring-webmvc/org/springframework/web/servlet/DispatcherServlet.java");

        MethodParameter param1 = new MethodParameter();
        param1.setName("request");
        param1.setType("HttpServletRequest");

        MethodParameter param2 = new MethodParameter();
        param2.setName("response");
        param2.setType("HttpServletResponse");

        methodInfo.setParameters(Arrays.asList(param1, param2));

        return methodInfo;
    }
}