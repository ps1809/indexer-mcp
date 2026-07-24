package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RelatedFileRequest;
import com.projectiq.mcp.client.dto.RelatedFileResponse.SearchTargetType;
import com.projectiq.mcp.client.dto.RelatedFileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ListRelatedFilesTool MCP tool.
 */
class ListRelatedFilesToolTest {

    private IndexerRestClient mockIndexerClient;
    private ListRelatedFilesTool tool;

    @BeforeEach
    void setUp() {
        mockIndexerClient = mock(IndexerRestClient.class);
        tool = new ListRelatedFilesTool(mockIndexerClient);
    }

    @Test
    void testListRelatedFilesSuccess() {
        // Arrange
        List<RelatedFile> relatedFiles = Arrays.asList(
            new RelatedFile("TestImpl.java", "/src/TestImpl.java", "JAVA", "IMPLEMENTATION", "com.example"),
            new RelatedFile("Test.xml", "/config/Test.xml", "XML", "CONFIGURATION", "com.example")
        );

        RelatedFileResponse response = new RelatedFileResponse();
        response.setRepositoryName("test-repo");
        response.setSearchTarget("TestClass");
        response.setTargetType(SearchTargetType.CLASS);
        response.setTotalResults(2);
        response.setRelatedFiles(relatedFiles);

        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class))).thenReturn(response);

        // Act
        String result = tool.listRelatedFiles("test-repo", "TestClass", "CLASS", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Related Files Results"));
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("TestClass"));
        assertTrue(result.contains("JAVA"));
        assertTrue(result.contains("IMPLEMENTATION"));
        verify(mockIndexerClient).findRelatedFiles(any(RelatedFileRequest.class));
    }

    @Test
    void testListRelatedFilesEmptyResult() {
        // Arrange
        RelatedFileResponse response = new RelatedFileResponse();
        response.setRepositoryName("test-repo");
        response.setSearchTarget("UnknownClass");
        response.setTargetType(SearchTargetType.CLASS);
        response.setTotalResults(0);
        response.setRelatedFiles(Arrays.asList());

        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class))).thenReturn(response);

        // Act
        String result = tool.listRelatedFiles("test-repo", "UnknownClass", "CLASS", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("No related files found"));
    }

    @Test
    void testListRelatedFilesMissingRepositoryName() {
        // Act
        String result = tool.listRelatedFiles("", "TestClass", "CLASS", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("repositoryName is required"));
    }

    @Test
    void testListRelatedFilesMissingSearchTarget() {
        // Act
        String result = tool.listRelatedFiles("test-repo", "", "CLASS", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("searchTarget is required"));
    }

    @Test
    void testListRelatedFilesMissingTargetType() {
        // Act
        String result = tool.listRelatedFiles("test-repo", "TestClass", "", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("targetType is required"));
    }

    @Test
    void testListRelatedFilesInvalidTargetType() {
        // Act
        String result = tool.listRelatedFiles("test-repo", "TestClass", "INVALID_TYPE", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Invalid targetType"));
    }

    @Test
    void testListRelatedFilesMethodType() {
        // Arrange
        RelatedFileResponse response = new RelatedFileResponse();
        response.setRepositoryName("test-repo");
        response.setSearchTarget("testMethod");
        response.setTargetType(SearchTargetType.METHOD);
        response.setTotalResults(1);
        response.setRelatedFiles(Arrays.asList(
            new RelatedFile("Test.java", "/src/Test.java", "JAVA", "DECLARATION", "com.example")
        ));

        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class))).thenReturn(response);

        // Act
        String result = tool.listRelatedFiles("test-repo", "testMethod", "METHOD", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("testMethod"));
        assertTrue(result.contains("METHOD"));
    }

    @Test
    void testListRelatedFilesRestApiType() {
        // Arrange
        RelatedFileResponse response = new RelatedFileResponse();
        response.setRepositoryName("test-repo");
        response.setSearchTarget("/api/v1/users");
        response.setTargetType(SearchTargetType.REST_API);
        response.setTotalResults(1);
        response.setRelatedFiles(Arrays.asList(
            new RelatedFile("UserController.java", "/src/UserController.java", "JAVA", "CONTROLLER", "com.example.controller")
        ));

        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class))).thenReturn(response);

        // Act
        String result = tool.listRelatedFiles("test-repo", "/api/v1/users", "REST_API", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("/api/v1/users"));
        assertTrue(result.contains("REST_API"));
    }

    @Test
    void testListRelatedFilesSpringComponentType() {
        // Arrange
        RelatedFileResponse response = new RelatedFileResponse();
        response.setRepositoryName("test-repo");
        response.setSearchTarget("MyService");
        response.setTargetType(SearchTargetType.SPRING_COMPONENT);
        response.setTotalResults(1);
        response.setRelatedFiles(Arrays.asList(
            new RelatedFile("MyService.java", "/src/MyService.java", "JAVA", "COMPONENT", "com.example.service")
        ));

        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class))).thenReturn(response);

        // Act
        String result = tool.listRelatedFiles("test-repo", "MyService", "SPRING_COMPONENT", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("MyService"));
        assertTrue(result.contains("SPRING_COMPONENT"));
    }

    @Test
    void testListRelatedFilesPackageType() {
        // Arrange
        RelatedFileResponse response = new RelatedFileResponse();
        response.setRepositoryName("test-repo");
        response.setSearchTarget("com.example.service");
        response.setTargetType(SearchTargetType.PACKAGE);
        response.setTotalResults(3);
        response.setRelatedFiles(Arrays.asList(
            new RelatedFile("Service1.java", "/src/Service1.java", "JAVA", "MEMBER", "com.example.service"),
            new RelatedFile("Service2.java", "/src/Service2.java", "JAVA", "MEMBER", "com.example.service"),
            new RelatedFile("Service3.java", "/src/Service3.java", "JAVA", "MEMBER", "com.example.service")
        ));

        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class))).thenReturn(response);

        // Act
        String result = tool.listRelatedFiles("test-repo", "com.example.service", "PACKAGE", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("com.example.service"));
        assertTrue(result.contains("PACKAGE"));
        assertTrue(result.contains("3"));
    }

    @Test
    void testListRelatedFilesConnectionError() {
        // Arrange
        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class)))
            .thenThrow(new com.projectiq.mcp.client.exception.IndexerConnectionException("Connection refused"));

        // Act
        String result = tool.listRelatedFiles("test-repo", "TestClass", "CLASS", "main");

        // Assert
        assertTrue(result.contains("INDEXER_UNREACHABLE"));
        assertTrue(result.contains("Cannot connect to ProjectIQ Indexer"));
    }

    @Test
    void testListRelatedFilesTimeoutError() {
        // Arrange
        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class)))
            .thenThrow(new com.projectiq.mcp.client.exception.IndexerTimeoutException("Request timed out"));

        // Act
        String result = tool.listRelatedFiles("test-repo", "TestClass", "CLASS", "main");

        // Assert
        assertTrue(result.contains("INDEXER_TIMEOUT"));
        assertTrue(result.contains("timed out"));
    }

    @Test
    void testListRelatedFilesHttpError() {
        // Arrange
        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class)))
            .thenThrow(new com.projectiq.mcp.client.exception.IndexerHttpException("Internal Server Error", 500));

        // Act
        String result = tool.listRelatedFiles("test-repo", "TestClass", "CLASS", "main");

        // Assert
        assertTrue(result.contains("INDEXER_HTTP_ERROR"));
    }

    @Test
    void testListRelatedFilesNullRepositoryName() {
        // Act
        String result = tool.listRelatedFiles(null, "TestClass", "CLASS", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testListRelatedFilesNullSearchTarget() {
        // Act
        String result = tool.listRelatedFiles("test-repo", null, "CLASS", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testListRelatedFilesNullTargetType() {
        // Act
        String result = tool.listRelatedFiles("test-repo", "TestClass", null, "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testListRelatedFilesInternalError() {
        // Arrange
        when(mockIndexerClient.findRelatedFiles(any(RelatedFileRequest.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String result = tool.listRelatedFiles("test-repo", "TestClass", "CLASS", "main");

        // Assert
        assertTrue(result.contains("INTERNAL_ERROR"));
        assertTrue(result.contains("Unexpected error"));
    }

    @Test
    void testSupportedTargetTypes() {
        List<String> types = ListRelatedFilesTool.SUPPORTED_TARGET_TYPES;
        assertEquals(5, types.size());
        assertTrue(types.contains("CLASS"));
        assertTrue(types.contains("METHOD"));
        assertTrue(types.contains("REST_API"));
        assertTrue(types.contains("SPRING_COMPONENT"));
        assertTrue(types.contains("PACKAGE"));
    }
}