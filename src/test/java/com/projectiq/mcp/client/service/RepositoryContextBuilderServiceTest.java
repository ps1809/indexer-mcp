package com.projectiq.mcp.client.service;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.*;
import com.projectiq.mcp.client.exception.IndexerClientException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryContextBuilderServiceTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    private RepositoryContextBuilderService service;

    @BeforeEach
    void setUp() {
        service = new RepositoryContextBuilderService(indexerRestClient);
    }

    @Test
    void buildContext_successWithAllEndpoints() throws IndexerClientException {
        // Arrange
        BuildContextRequest request = createValidRequest();
        
        when(indexerRestClient.getRepositorySummary(any())).thenReturn(createSummaryResponse());
        when(indexerRestClient.getRepositoryStatistics(any())).thenReturn(createStatsResponse());
        when(indexerRestClient.searchCode(any())).thenReturn(createSearchResponse());
        when(indexerRestClient.findSpringComponent(any())).thenReturn(createSpringComponentResponse());
        when(indexerRestClient.findRestApi(any())).thenReturn(createRestApiResponse());
        when(indexerRestClient.findClass(any())).thenReturn(createClassResponse());
        when(indexerRestClient.findMethod(any())).thenReturn(createMethodResponse());
        when(indexerRestClient.findRelatedFiles(any())).thenReturn(createRelatedFileResponse());
        when(indexerRestClient.findDependency(any())).thenReturn(createDependencyResponse());

        // Act
        RepositoryContext context = service.buildContext(request);

        // Assert
        assertNotNull(context);
        assertEquals("Test Task", context.getTask());
        assertNotNull(context.getBuildTimestamp());
        assertTrue(context.getErrors().isEmpty());
        assertNotNull(context.getRepositorySummary());
        assertNotNull(context.getRepositoryStatistics());
        assertNotNull(context.getSearchResults());
        assertNotNull(context.getSpringComponents());
        assertNotNull(context.getRestApis());
        assertNotNull(context.getClasses());
        assertNotNull(context.getMethods());
        assertNotNull(context.getRelatedFiles());
        assertNotNull(context.getDependencies());

        verify(indexerRestClient).getRepositorySummary(any());
        verify(indexerRestClient).getRepositoryStatistics(any());
        verify(indexerRestClient).searchCode(any());
        verify(indexerRestClient).findSpringComponent(any());
        verify(indexerRestClient).findRestApi(any());
        verify(indexerRestClient).findClass(any());
        verify(indexerRestClient).findMethod(any());
        verify(indexerRestClient).findRelatedFiles(any());
        verify(indexerRestClient).findDependency(any());
    }

    @Test
    void buildContext_partialFailure() throws IndexerClientException {
        // Arrange - some endpoints fail
        BuildContextRequest request = createValidRequest();
        
        when(indexerRestClient.getRepositorySummary(any())).thenReturn(createSummaryResponse());
        when(indexerRestClient.getRepositoryStatistics(any())).thenThrow(new RuntimeException("Connection refused"));
        when(indexerRestClient.searchCode(any())).thenReturn(createSearchResponse());
        when(indexerRestClient.findSpringComponent(any())).thenThrow(new IndexerClientException("Timeout"));
        when(indexerRestClient.findRestApi(any())).thenReturn(createRestApiResponse());
        when(indexerRestClient.findClass(any())).thenReturn(null);
        when(indexerRestClient.findMethod(any())).thenReturn(createMethodResponse());
        when(indexerRestClient.findRelatedFiles(any())).thenThrow(new RuntimeException("Unknown error"));
        when(indexerRestClient.findDependency(any())).thenReturn(createDependencyResponse());

        // Act
        RepositoryContext context = service.buildContext(request);

        // Assert
        assertNotNull(context);
        assertEquals(3, context.getErrors().size());
        
        assertNotNull(context.getRepositorySummary());
        assertNotNull(context.getSearchResults());
        assertNotNull(context.getRestApis());
        assertNotNull(context.getMethods());
        assertNotNull(context.getDependencies());

        boolean hasStatsError = context.getErrors().stream()
            .anyMatch(e -> "repositoryStatistics".equals(e.getEndpoint()));
        boolean hasSpringError = context.getErrors().stream()
            .anyMatch(e -> "springComponents".equals(e.getEndpoint()));
        boolean hasFilesError = context.getErrors().stream()
            .anyMatch(e -> "relatedFiles".equals(e.getEndpoint()));
        
        assertTrue(hasStatsError);
        assertTrue(hasSpringError);
        assertTrue(hasFilesError);
    }

    @Test
    void buildContext_allEndpointsFail() throws IndexerClientException {
        // Arrange - all endpoints fail
        BuildContextRequest request = createValidRequest();
        
        when(indexerRestClient.getRepositorySummary(any())).thenThrow(new RuntimeException("All failed"));
        when(indexerRestClient.getRepositoryStatistics(any())).thenThrow(new RuntimeException("All failed"));
        when(indexerRestClient.searchCode(any())).thenThrow(new RuntimeException("All failed"));
        when(indexerRestClient.findSpringComponent(any())).thenThrow(new RuntimeException("All failed"));
        when(indexerRestClient.findRestApi(any())).thenThrow(new RuntimeException("All failed"));
        when(indexerRestClient.findClass(any())).thenThrow(new RuntimeException("All failed"));
        when(indexerRestClient.findMethod(any())).thenThrow(new RuntimeException("All failed"));
        when(indexerRestClient.findRelatedFiles(any())).thenThrow(new RuntimeException("All failed"));
        when(indexerRestClient.findDependency(any())).thenThrow(new RuntimeException("All failed"));

        // Act
        RepositoryContext context = service.buildContext(request);

        // Assert
        assertNotNull(context);
        assertEquals(9, context.getErrors().size());
        assertTrue(context.hasErrors());
        
        assertNull(context.getRepositorySummary());
        assertNull(context.getRepositoryStatistics());
    }

    @Test
    void buildContext_emptyResponses() throws IndexerClientException {
        // Arrange - all endpoints return null or empty
        BuildContextRequest request = createValidRequest();
        
        when(indexerRestClient.getRepositorySummary(any())).thenReturn(null);
        when(indexerRestClient.getRepositoryStatistics(any())).thenReturn(null);
        when(indexerRestClient.searchCode(any())).thenReturn(null);
        when(indexerRestClient.findSpringComponent(any())).thenReturn(null);
        when(indexerRestClient.findRestApi(any())).thenReturn(null);
        when(indexerRestClient.findClass(any())).thenReturn(null);
        when(indexerRestClient.findMethod(any())).thenReturn(null);
        when(indexerRestClient.findRelatedFiles(any())).thenReturn(null);
        when(indexerRestClient.findDependency(any())).thenReturn(null);

        // Act
        RepositoryContext context = service.buildContext(request);

        // Assert
        assertNotNull(context);
        assertTrue(context.getErrors().isEmpty());
        
        assertNull(context.getRepositorySummary());
        assertNull(context.getRepositoryStatistics());
    }

    @Test
    void buildContext_duplicateElimination() {
        // This test verifies the service runs without errors when duplicates are present.
        // The actual deduplication is internal to the service.
        BuildContextRequest request = createValidRequest();
        
        ClassInfo duplicateClass = new ClassInfo();
        duplicateClass.setFullyQualifiedName("com.example.Foo");

        ClassResponse classResp = new ClassResponse();
        classResp.setClasses(List.of(duplicateClass, duplicateClass));

        when(indexerRestClient.getRepositorySummary(any())).thenReturn(createSummaryResponse());
        when(indexerRestClient.getRepositoryStatistics(any())).thenReturn(createStatsResponse());
        when(indexerRestClient.searchCode(any())).thenReturn(createSearchResponse());
        when(indexerRestClient.findSpringComponent(any())).thenReturn(createSpringComponentResponse());
        when(indexerRestClient.findRestApi(any())).thenReturn(createRestApiResponse());
        when(indexerRestClient.findClass(any())).thenReturn(classResp);
        when(indexerRestClient.findMethod(any())).thenReturn(createMethodResponse());
        when(indexerRestClient.findRelatedFiles(any())).thenReturn(createRelatedFileResponse());
        when(indexerRestClient.findDependency(any())).thenReturn(createDependencyResponse());

        RepositoryContext context = service.buildContext(request);

        assertNotNull(context);
        assertFalse(context.hasErrors());
        assertNotNull(context.getClasses());
        // After deduplication, only one Foo should remain
        assertEquals(1, context.getClasses().size());
    }

    private BuildContextRequest createValidRequest() {
        return new BuildContextRequest("Test Task", "test-repo", "main");
    }

    private RepositorySummaryResponse createSummaryResponse() {
        RepositorySummaryResponse response = new RepositorySummaryResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setStatus("INDEXED");
        response.setFileCount(100);
        return response;
    }

    private RepositoryStatsResponse createStatsResponse() {
        RepositoryStatsResponse response = new RepositoryStatsResponse();
        response.setRepositoryName("test-repo");
        response.setBranch("main");
        response.setStatus("INDEXED");
        response.setTotalLinesOfCode(50000);
        response.setContributors(List.of());
        return response;
    }

    private SearchCodeResponse createSearchResponse() {
        SearchCodeResponse response = new SearchCodeResponse();
        response.setResults(List.of());
        return response;
    }

    private SpringComponentResponse createSpringComponentResponse() {
        SpringComponentResponse response = new SpringComponentResponse();
        response.setComponents(List.of());
        return response;
    }

    private RestApiResponse createRestApiResponse() {
        RestApiResponse response = new RestApiResponse();
        response.setEndpoints(List.of());
        return response;
    }

    private ClassResponse createClassResponse() {
        ClassResponse response = new ClassResponse();
        response.setClasses(List.of(createClassInfo("com.example.Foo")));
        return response;
    }

    private ClassInfo createClassInfo(String name) {
        ClassInfo info = new ClassInfo();
        info.setFullyQualifiedName(name);
        return info;
    }

    private MethodResponse createMethodResponse() {
        MethodResponse response = new MethodResponse();
        response.setMethods(List.of());
        return response;
    }

    private RelatedFileResponse createRelatedFileResponse() {
        RelatedFileResponse response = new RelatedFileResponse();
        response.setRelatedFiles(List.of());
        return response;
    }

    private DependencyResponse createDependencyResponse() {
        DependencyResponse response = new DependencyResponse();
        response.setDependencies(List.of());
        return response;
    }
}