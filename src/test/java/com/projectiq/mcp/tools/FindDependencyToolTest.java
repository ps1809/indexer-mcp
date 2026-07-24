package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FindDependencyTool MCP Tool.
 */
@ExtendWith(MockitoExtension.class)
class FindDependencyToolTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    private FindDependencyTool tool;

    @BeforeEach
    void setUp() {
        tool = new FindDependencyTool(indexerRestClient);
    }

    @Test
    void testFindDependencyWithAllParameters() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                "test-repo",
                "MAVEN,GRADLE",
                "main",
                "com.example"
        );

        assertNotNull(result);
        assertTrue(result.contains("## Dependencies"));
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("2")); // total results

        verify(indexerRestClient).findDependency(argThat(request ->
                "test-repo".equals(request.getRepositoryName())
                        && request.getDependencyTypes().contains("MAVEN")
                        && request.getDependencyTypes().contains("GRADLE")
                        && "main".equals(request.getBranch())
                        && "com.example".equals(request.getPackageName())
        ));
    }

    @Test
    void testFindDependencyWithSingleDependencyType() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                "test-repo",
                "INTERNAL_MODULE",
                null,
                null
        );

        assertNotNull(result);
        assertTrue(result.contains("## Dependencies"));

        verify(indexerRestClient).findDependency(argThat(request ->
                request.getDependencyTypes().size() == 1
                        && request.getDependencyTypes().contains("INTERNAL_MODULE")
        ));
    }

    @Test
    void testFindDependencyWithNoDependencyTypes() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                "test-repo",
                null,
                null,
                null
        );

        assertNotNull(result);
        verify(indexerRestClient).findDependency(argThat(request ->
                request.getDependencyTypes() == null
        ));
    }

    @Test
    void testFindDependencyWithEmptyDependencyTypes() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                "test-repo",
                "",
                null,
                null
        );

        assertNotNull(result);
    }

    @Test
    void testFindDependencyWithWhitespaceDependencyTypes() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                "test-repo",
                "  MAVEN  ,  GRADLE  ",
                null,
                null
        );

        assertNotNull(result);
        verify(indexerRestClient).findDependency(argThat(request ->
                request.getDependencyTypes().size() == 2
        ));
    }

    @Test
    void testFindDependencyWithLowercaseDependencyTypes() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                "test-repo",
                "maven,gradle",
                null,
                null
        );

        assertNotNull(result);
        verify(indexerRestClient).findDependency(argThat(request ->
                request.getDependencyTypes().contains("MAVEN")
                        && request.getDependencyTypes().contains("GRADLE")
        ));
    }

    @Test
    void testFindDependencyWithMixedCaseDependencyTypes() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                "test-repo",
                "maven,GrAdLe,internal_module",
                null,
                null
        );

        assertNotNull(result);
        verify(indexerRestClient).findDependency(argThat(request ->
                request.getDependencyTypes().contains("MAVEN")
                        && request.getDependencyTypes().contains("GRADLE")
                        && request.getDependencyTypes().contains("INTERNAL_MODULE")
        ));
    }

    @Test
    void testFindDependencyWithEmptyResponse() throws Exception {
        DependencyResponse emptyResponse = new DependencyResponse();
        emptyResponse.setRepositoryName("empty-repo");
        emptyResponse.setDependencies(Collections.emptyList());
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(emptyResponse);

        String result = tool.findDependency(
                "empty-repo",
                null,
                null,
                null
        );

        assertTrue(result.contains("No dependencies found"));
    }

    @Test
    void testFindDependencyWithNullDependencies() throws Exception {
        DependencyResponse nullResponse = new DependencyResponse();
        nullResponse.setRepositoryName("null-deps-repo");
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(nullResponse);

        String result = tool.findDependency(
                "null-deps-repo",
                null,
                null,
                null
        );

        assertTrue(result.contains("No dependencies found"));
    }

    @Test
    void testFindDependencyWithTimeoutException() throws Exception {
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenThrow(new IndexerTimeoutException("Connection timed out"));

        String result = tool.findDependency(
                "test-repo",
                null,
                null,
                null
        );

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("timed out"));
    }

    @Test
    void testFindDependencyWithConnectionException() throws Exception {
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenThrow(new IndexerConnectionException("Cannot connect to indexer"));

        String result = tool.findDependency(
                "test-repo",
                null,
                null,
                null
        );

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Cannot connect"));
    }

    @Test
    void testFindDependencyWithClientException() throws Exception {
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenThrow(new IndexerClientException("Invalid response from indexer"));

        String result = tool.findDependency(
                "test-repo",
                null,
                null,
                null
        );

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Invalid response"));
    }

    @Test
    void testFindDependencyWithGenericException() throws Exception {
        when(indexerRestClient.findDependency(any(DependencyRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = tool.findDependency(
                "test-repo",
                null,
                null,
                null
        );

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Unexpected error"));
    }

    @Test
    void testFindDependencyWithNullRepositoryName() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                null,
                null,
                null,
                null
        );

        assertNotNull(result);
    }

    @Test
    void testFindDependencyWithAllDependencyTypes() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency(
                "test-repo",
                "MAVEN,GRADLE,INTERNAL_MODULE,EXTERNAL_LIBRARY",
                null,
                null
        );

        assertNotNull(result);
        verify(indexerRestClient).findDependency(argThat(request ->
                request.getDependencyTypes().size() == 4
        ));
    }

    @Test
    void testFormatResponseWithValidDependencies() throws Exception {
        DependencyResponse mockResponse = createMockResponse();
        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(mockResponse);

        String result = tool.findDependency("test-repo", null, null, null);

        assertTrue(result.contains("| # | Name | Group ID | Artifact ID | Version | Scope | Type |"));
        assertTrue(result.contains("spring-boot-starter-web"));
        assertTrue(result.contains("org.springframework.boot"));
    }

    @Test
    void testFormatResponseWithNullDependencyFields() throws Exception {
        DependencyResponse response = new DependencyResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);

        DependencyInfo dep = new DependencyInfo();
        // All fields null by default
        response.setDependencies(Collections.singletonList(dep));

        when(indexerRestClient.findDependency(any(DependencyRequest.class))).thenReturn(response);

        String result = tool.findDependency("test-repo", null, null, null);

        assertTrue(result.contains("N/A"));
    }

    @Test
    void testSupportedDependencyTypesExists() {
        List<String> types = FindDependencyTool.SUPPORTED_DEPENDENCY_TYPES;
        assertNotNull(types);
        assertEquals(4, types.size());
        assertTrue(types.contains("MAVEN"));
        assertTrue(types.contains("GRADLE"));
        assertTrue(types.contains("INTERNAL_MODULE"));
        assertTrue(types.contains("EXTERNAL_LIBRARY"));
    }

    private DependencyResponse createMockResponse() {
        DependencyResponse response = new DependencyResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(2);

        DependencyInfo dep1 = new DependencyInfo();
        dep1.setName("spring-boot-starter-web");
        dep1.setGroupId("org.springframework.boot");
        dep1.setArtifactId("spring-boot-starter-web");
        dep1.setVersion("3.2.0");
        dep1.setScope("compile");
        dep1.setType(com.projectiq.mcp.client.dto.DependencyType.MAVEN);

        DependencyInfo dep2 = new DependencyInfo();
        dep2.setName("lombok");
        dep2.setGroupId("org.projectlombok");
        dep2.setArtifactId("lombok");
        dep2.setVersion("1.18.30");
        dep2.setScope("provided");
        dep2.setType(com.projectiq.mcp.client.dto.DependencyType.MAVEN);

        response.setDependencies(Arrays.asList(dep1, dep2));
        return response;
    }
}