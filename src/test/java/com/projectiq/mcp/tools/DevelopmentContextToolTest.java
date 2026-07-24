package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import com.projectiq.mcp.client.service.DevelopmentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevelopmentContextToolTest {

    @Mock
    private DevelopmentContextService developmentContextService;

    private DevelopmentContextTool tool;

    @BeforeEach
    void setUp() {
        tool = new DevelopmentContextTool(developmentContextService);
    }

    @Test
    void developmentContext_withValidRequest_returnsFormattedContext() throws IndexerClientException {
        // Arrange
        DevelopmentContext mockContext = createSampleDevelopmentContext();
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockContext);

        // Act
        String result = tool.developmentContext("Add pagination to UserController", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Development Context"));
        assertTrue(result.contains("Add pagination to UserController"));
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("Repository Summary"));
        assertTrue(result.contains("Relevant Classes"));
        assertTrue(result.contains("Relevant Methods"));
        assertTrue(result.contains("Spring Components"));
        assertTrue(result.contains("REST APIs"));
        assertTrue(result.contains("Dependencies"));
        assertTrue(result.contains("Related Files"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void developmentContext_withNullTask_returnsError() {
        // Act
        String result = tool.developmentContext(null, "test-repo", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(developmentContextService);
    }

    @Test
    void developmentContext_withEmptyTask_returnsError() {
        // Act
        String result = tool.developmentContext("", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Task description is required"));
        verifyNoInteractions(developmentContextService);
    }

    @Test
    void developmentContext_withNullRepository_returnsError() {
        // Act
        String result = tool.developmentContext("Add pagination", null, "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
        verifyNoInteractions(developmentContextService);
    }

    @Test
    void developmentContext_withEmptyRepository_returnsError() {
        // Act
        String result = tool.developmentContext("Add pagination", "", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Repository name is required"));
        verifyNoInteractions(developmentContextService);
    }

    @Test
    void developmentContext_withDefaultBranch_usesMain() throws IndexerClientException {
        // Arrange
        DevelopmentContext mockContext = new DevelopmentContext();
        mockContext.setTask("Test");
        mockContext.setRepositoryName("test-repo");
        mockContext.setBranch("main");
        mockContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockContext);

        // Act
        String result = tool.developmentContext("Test", "test-repo", null);

        // Assert
        assertNotNull(result);
        verify(developmentContextService).createDevelopmentContext(argThat(req -> "main".equals(req.getBranch())));
    }

    @Test
    void developmentContext_withIndexerConnectionException_returnsError() throws IndexerClientException {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any())).thenThrow(new IndexerConnectionException("Connection refused"));

        // Act
        String result = tool.developmentContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_UNREACHABLE"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void developmentContext_withIndexerTimeoutException_returnsError() throws IndexerClientException {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any())).thenThrow(new IndexerTimeoutException("Timeout after 30s"));

        // Act
        String result = tool.developmentContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_TIMEOUT"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void developmentContext_withIndexerHttpException_returnsError() throws IndexerClientException {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any())).thenThrow(new IndexerHttpException("HTTP 500", 500));

        // Act
        String result = tool.developmentContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_HTTP_ERROR"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void developmentContext_withIndexerClientException_returnsError() throws IndexerClientException {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any())).thenThrow(new IndexerClientException("Bad request"));

        // Act
        String result = tool.developmentContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_ERROR"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void developmentContext_withGenericException_returnsError() throws IndexerClientException {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any())).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String result = tool.developmentContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INTERNAL_ERROR"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void developmentContext_withErrors_includesErrorsInOutput() throws IndexerClientException {
        // Arrange
        DevelopmentContext mockContext = new DevelopmentContext();
        mockContext.setTask("Test");
        mockContext.setRepositoryName("test-repo");
        mockContext.setBranch("main");
        mockContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        mockContext.addError(new ContextBuildError("searchCode", "INDEXER_UNREACHABLE", "Cannot connect"));
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockContext);

        // Act
        String result = tool.developmentContext("Test", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Errors"));
        assertTrue(result.contains("INDEXER_UNREACHABLE"));
    }

    @Test
    void developmentContext_withEmptyContext_showsNoResults() throws IndexerClientException {
        // Arrange
        DevelopmentContext mockContext = new DevelopmentContext();
        mockContext.setTask("Test");
        mockContext.setRepositoryName("test-repo");
        mockContext.setBranch("main");
        mockContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockContext);

        // Act
        String result = tool.developmentContext("Test", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("No results"));
    }

    @Test
    void developmentContext_withManyItems_limitsTo10() throws IndexerClientException {
        // Arrange
        DevelopmentContext mockContext = new DevelopmentContext();
        mockContext.setTask("Test");
        mockContext.setRepositoryName("test-repo");
        mockContext.setBranch("main");
        mockContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<ClassInfo> manyClasses = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            ClassInfo ci = new ClassInfo();
            ci.setFullyQualifiedName("com.example.Class" + i);
            ci.setClassName("Class" + i);
            manyClasses.add(ci);
        }
        mockContext.setRelevantClasses(manyClasses);

        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockContext);

        // Act
        String result = tool.developmentContext("Test", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("... and 5 more"));
    }

    /**
     * Creates a sample DevelopmentContext with data in all sections.
     */
    private DevelopmentContext createSampleDevelopmentContext() {
        DevelopmentContext context = new DevelopmentContext();
        context.setTask("Add pagination to UserController");
        context.setRepositoryName("test-repo");
        context.setBranch("main");
        context.setBuildTimestamp("2024-01-01T00:00:00Z");

        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test-repo");
        summary.setBranch("main");
        summary.setStatus("ACTIVE");
        summary.setFileCount(10);
        summary.setClassCount(5);
        summary.setCommitCount(20);
        context.setRepositorySummary(summary);

        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setFullyQualifiedName("com.example.UserController");
        classInfo.setClassName("UserController");
        classes.add(classInfo);
        context.setRelevantClasses(classes);

        List<MethodInfo> methods = new ArrayList<>();
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setDeclaringClass("com.example.UserController");
        methodInfo.setMethodName("findAll");
        methods.add(methodInfo);
        context.setRelevantMethods(methods);

        List<SpringComponentInfo> components = new ArrayList<>();
        SpringComponentInfo component = new SpringComponentInfo();
        component.setName("userService");
        component.setComponentType("SERVICE");
        components.add(component);
        context.setSpringComponents(components);

        List<RestEndpointInfo> apis = new ArrayList<>();
        RestEndpointInfo api = new RestEndpointInfo();
        api.setHttpMethod("GET");
        api.setEndpointPath("/api/users");
        apis.add(api);
        context.setRestApis(apis);

        List<DependencyInfo> deps = new ArrayList<>();
        DependencyInfo dep = new DependencyInfo();
        dep.setGroupId("org.springframework.boot");
        dep.setArtifactId("spring-boot-starter-web");
        deps.add(dep);
        context.setDependencies(deps);

        List<RelatedFile> files = new ArrayList<>();
        RelatedFile file = new RelatedFile();
        file.setFilePath("src/main/java/com/example/UserController.java");
        files.add(file);
        context.setRelatedFiles(files);

        return context;
    }
}