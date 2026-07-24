package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.dto.SpringComponentRequest;
import com.projectiq.mcp.client.dto.SpringComponentResponse;
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
class FindSpringComponentToolTest {

    @Mock
    private IndexerRestClient indexerRestClient;

    @InjectMocks
    private FindSpringComponentTool findSpringComponentTool;

    private SpringComponentResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = new SpringComponentResponse();
        mockResponse.setRepositoryName("test-repo");
        mockResponse.setTotalResults(3);

        SpringComponentInfo controller = new SpringComponentInfo();
        controller.setName("UserController");
        controller.setComponentType("Controller");
        controller.setClassName("UserController");
        controller.setPackageName("com.example.controller");
        controller.setFilePath("src/main/java/com/example/controller/UserController.java");
        controller.setLineNumber(25);
        controller.setDescription("REST controller for user operations");

        SpringComponentInfo service = new SpringComponentInfo();
        service.setName("UserService");
        service.setComponentType("Service");
        service.setClassName("UserService");
        service.setPackageName("com.example.service");
        service.setFilePath("src/main/java/com/example/service/UserService.java");
        service.setLineNumber(15);
        service.setDescription("Service layer for user management");

        SpringComponentInfo repository = new SpringComponentInfo();
        repository.setName("UserRepository");
        repository.setComponentType("Repository");
        repository.setClassName("UserRepository");
        repository.setPackageName("com.example.repository");
        repository.setFilePath("src/main/java/com/example/repository/UserRepository.java");
        repository.setLineNumber(10);
        repository.setDescription("Spring Data JPA repository for User entity");

        mockResponse.setComponents(List.of(controller, service, repository));
    }

    @Test
    void findSpringComponent_shouldReturnFormattedResponseOnSuccess() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(mockResponse);

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller,Service,Repository", "main", null);

        assertNotNull(result);
        assertTrue(result.contains("## Spring Components"));
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("UserController"));
        assertTrue(result.contains("UserService"));
        assertTrue(result.contains("UserRepository"));
        assertTrue(result.contains("Controller"));
        assertTrue(result.contains("Service"));
        assertTrue(result.contains("Repository"));
        verify(indexerRestClient).findSpringComponent(any(SpringComponentRequest.class));
    }

    @Test
    void findSpringComponent_shouldWorkWithSingleComponentType() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(mockResponse);

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Service", null, null);

        assertNotNull(result);
        assertTrue(result.contains("## Spring Components"));
        verify(indexerRestClient).findSpringComponent(argThat(request ->
                request.getRepositoryName().equals("test-repo") &&
                request.getComponentTypes().equals(List.of("Service"))
        ));
    }

    @Test
    void findSpringComponent_shouldUseAllTypesWhenNoneSpecified() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(mockResponse);

        String result = findSpringComponentTool.findSpringComponent("test-repo", null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("## Spring Components"));
        verify(indexerRestClient).findSpringComponent(argThat(request ->
                request.getComponentTypes().size() == 6
        ));
    }

    @Test
    void findSpringComponent_shouldWorkWithOptionalParameters() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(mockResponse);

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", null, null);

        assertNotNull(result);
        verify(indexerRestClient).findSpringComponent(argThat(request ->
                request.getRepositoryName().equals("test-repo") &&
                request.getBranch() == null &&
                request.getPackageName() == null
        ));
    }

    @Test
    void findSpringComponent_shouldReturnErrorMessageOnTimeout() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenThrow(new IndexerTimeoutException("Connection timed out"));

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", null, null);

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("timed out"));
    }

    @Test
    void findSpringComponent_shouldReturnErrorMessageOnConnectionFailure() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenThrow(new IndexerConnectionException("Connection refused"));

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", null, null);

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Cannot connect"));
    }

    @Test
    void findSpringComponent_shouldReturnErrorMessageOnClientException() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenThrow(new IndexerClientException("Invalid response"));

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", null, null);

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("Invalid response"));
    }

    @Test
    void findSpringComponent_shouldReturnErrorMessageOnUnexpectedException() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", null, null);

        assertTrue(result.startsWith("ERROR:"));
        assertTrue(result.contains("unexpected error"));
    }

    @Test
    void findSpringComponent_shouldReturnNoResultsWhenEmptyList() {
        SpringComponentResponse emptyResponse = new SpringComponentResponse();
        emptyResponse.setRepositoryName("test-repo");
        emptyResponse.setTotalResults(0);
        emptyResponse.setComponents(List.of());

        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(emptyResponse);

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", null, null);

        assertNotNull(result);
        assertTrue(result.contains("No Spring components found"));
    }

    @Test
    void findSpringComponent_shouldReturnNoResultsWhenNullComponents() {
        SpringComponentResponse nullResponse = new SpringComponentResponse();
        nullResponse.setRepositoryName("test-repo");
        nullResponse.setTotalResults(0);
        nullResponse.setComponents(null);

        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(nullResponse);

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", null, null);

        assertNotNull(result);
        assertTrue(result.contains("No Spring components found"));
    }

    @Test
    void findSpringComponent_shouldHandleNullTotalResults() {
        SpringComponentResponse response = new SpringComponentResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(null);
        response.setComponents(List.of());

        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(response);

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", null, null);

        assertNotNull(result);
    }

    @Test
    void findSpringComponent_shouldFilterByPackageName() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(mockResponse);

        String result = findSpringComponentTool.findSpringComponent("test-repo", "Controller", "main", "com.example.controller");

        assertNotNull(result);
        verify(indexerRestClient).findSpringComponent(argThat(request ->
                request.getPackageName().equals("com.example.controller")
        ));
    }

    @Test
    void findSpringComponent_shouldParseEmptyComponentTypesAsAllTypes() {
        when(indexerRestClient.findSpringComponent(any(SpringComponentRequest.class)))
                .thenReturn(mockResponse);

        String result = findSpringComponentTool.findSpringComponent("test-repo", "", null, null);

        assertNotNull(result);
        verify(indexerRestClient).findSpringComponent(argThat(request ->
                request.getComponentTypes().size() == 6
        ));
    }

    @Test
    void findSpringComponent_shouldSupportAllComponentTypes() {
        // Verify SUPPORTED_COMPONENT_TYPES contains expected types
        assertEquals(6, FindSpringComponentTool.SUPPORTED_COMPONENT_TYPES.size());
        assertTrue(FindSpringComponentTool.SUPPORTED_COMPONENT_TYPES.contains("Controller"));
        assertTrue(FindSpringComponentTool.SUPPORTED_COMPONENT_TYPES.contains("RestController"));
        assertTrue(FindSpringComponentTool.SUPPORTED_COMPONENT_TYPES.contains("Service"));
        assertTrue(FindSpringComponentTool.SUPPORTED_COMPONENT_TYPES.contains("Repository"));
        assertTrue(FindSpringComponentTool.SUPPORTED_COMPONENT_TYPES.contains("Component"));
        assertTrue(FindSpringComponentTool.SUPPORTED_COMPONENT_TYPES.contains("Configuration"));
    }
}