package com.projectiq.mcp.client.service;

import com.projectiq.mcp.client.dto.BuildContextRequest;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RepositoryContext;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevelopmentContextServiceTest {

    @Mock
    private RepositoryContextBuilderService contextBuilderService;

    private DevelopmentContextService developmentContextService;

    @BeforeEach
    void setUp() {
        developmentContextService = new DevelopmentContextService(contextBuilderService);
    }

    @Test
    void createDevelopmentContext_withValidRequest_returnsDevelopmentContext() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = createSampleRepositoryContext();
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        BuildContextRequest request = new BuildContextRequest("Add pagination", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertNotNull(result);
        assertEquals("Add pagination", result.getTask());
        assertEquals("test-repo", result.getRepositoryName());
        assertEquals("main", result.getBranch());
        assertNotNull(result.getBuildTimestamp());
        assertNotNull(result.getRepositorySummary());
        assertEquals("test-repo", result.getRepositorySummary().getRepositoryName());
        assertFalse(result.getRelevantClasses().isEmpty());
        assertFalse(result.getRelevantMethods().isEmpty());
        assertFalse(result.getSpringComponents().isEmpty());
        assertFalse(result.getRestApis().isEmpty());
        assertFalse(result.getDependencies().isEmpty());
        assertFalse(result.getRelatedFiles().isEmpty());
        assertFalse(result.hasErrors());

        verify(contextBuilderService).buildContext(any());
    }

    @Test
    void createDevelopmentContext_withEmptyContext_returnsEmptyDevelopmentContext() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRelevantClasses().isEmpty());
        assertTrue(result.getRelevantMethods().isEmpty());
        assertTrue(result.getSpringComponents().isEmpty());
        assertTrue(result.getRestApis().isEmpty());
        assertTrue(result.getDependencies().isEmpty());
        assertTrue(result.getRelatedFiles().isEmpty());
        assertFalse(result.hasErrors());
    }

    @Test
    void createDevelopmentContext_withPartialErrors_includesErrors() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");
        repoContext.addError(new ContextBuildError("searchCode", "INDEXER_UNREACHABLE", "Cannot connect"));
        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);

        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("searchCode", result.getErrors().get(0).getEndpoint());
    }

    @Test
    void createDevelopmentContext_withIndexerConnectionException_throwsException() throws IndexerClientException {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenThrow(new IndexerConnectionException("Connection refused"));
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act & Assert
        assertThrows(IndexerConnectionException.class, () -> {
            developmentContextService.createDevelopmentContext(request);
        });
    }

    @Test
    void createDevelopmentContext_withIndexerTimeoutException_throwsException() throws IndexerClientException {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenThrow(new IndexerTimeoutException("Timeout"));
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act & Assert
        assertThrows(IndexerTimeoutException.class, () -> {
            developmentContextService.createDevelopmentContext(request);
        });
    }

    @Test
    void createDevelopmentContext_withIndexerHttpException_throwsException() throws IndexerClientException {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenThrow(new IndexerHttpException("Internal error", 500));
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act & Assert
        assertThrows(IndexerHttpException.class, () -> {
            developmentContextService.createDevelopmentContext(request);
        });
    }

    @Test
    void createDevelopmentContext_withIndexerClientException_throwsException() throws IndexerClientException {
        // Arrange
        when(contextBuilderService.buildContext(any())).thenThrow(new IndexerClientException("Bad request"));
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act & Assert
        assertThrows(IndexerClientException.class, () -> {
            developmentContextService.createDevelopmentContext(request);
        });
    }

    @Test
    void createDevelopmentContext_withDeterministicOrdering_classesSorted() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");

        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo classB = new ClassInfo();
        classB.setFullyQualifiedName("com.example.BController");
        ClassInfo classA = new ClassInfo();
        classA.setFullyQualifiedName("com.example.AController");
        classes.add(classB);
        classes.add(classA);
        repoContext.setClasses(classes);

        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert - should be sorted A then B
        assertEquals(2, result.getRelevantClasses().size());
        assertEquals("com.example.AController", result.getRelevantClasses().get(0).getFullyQualifiedName());
        assertEquals("com.example.BController", result.getRelevantClasses().get(1).getFullyQualifiedName());
    }

    @Test
    void createDevelopmentContext_withDeterministicOrdering_methodsSorted() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");

        List<MethodInfo> methods = new ArrayList<>();
        MethodInfo methodB = new MethodInfo();
        methodB.setDeclaringClass("com.example.UserController");
        methodB.setMethodName("getUser");
        MethodInfo methodA = new MethodInfo();
        methodA.setDeclaringClass("com.example.AdminController");
        methodA.setMethodName("createAdmin");
        methods.add(methodB);
        methods.add(methodA);
        repoContext.setMethods(methods);

        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertEquals(2, result.getRelevantMethods().size());
        assertEquals("com.example.AdminController", result.getRelevantMethods().get(0).getDeclaringClass());
    }

    @Test
    void createDevelopmentContext_withDeterministicOrdering_springComponentsSorted() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");

        List<SpringComponentInfo> components = new ArrayList<>();
        SpringComponentInfo compB = new SpringComponentInfo();
        compB.setName("userService");
        SpringComponentInfo compA = new SpringComponentInfo();
        compA.setName("adminService");
        components.add(compB);
        components.add(compA);
        repoContext.setSpringComponents(components);

        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertEquals(2, result.getSpringComponents().size());
        assertEquals("adminService", result.getSpringComponents().get(0).getName());
        assertEquals("userService", result.getSpringComponents().get(1).getName());
    }

    @Test
    void createDevelopmentContext_withDeterministicOrdering_restApisSorted() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");

        List<RestEndpointInfo> apis = new ArrayList<>();
        RestEndpointInfo apiB = new RestEndpointInfo();
        apiB.setHttpMethod("POST");
        apiB.setEndpointPath("/api/users");
        RestEndpointInfo apiA = new RestEndpointInfo();
        apiA.setHttpMethod("GET");
        apiA.setEndpointPath("/api/users");
        apis.add(apiB);
        apis.add(apiA);
        repoContext.setRestApis(apis);

        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert - GET before POST
        assertEquals(2, result.getRestApis().size());
        assertEquals("GET", result.getRestApis().get(0).getHttpMethod());
        assertEquals("POST", result.getRestApis().get(1).getHttpMethod());
    }

    @Test
    void createDevelopmentContext_withDeterministicOrdering_dependenciesSorted() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");

        List<DependencyInfo> deps = new ArrayList<>();
        DependencyInfo depB = new DependencyInfo();
        depB.setGroupId("org.springframework.boot");
        depB.setArtifactId("spring-boot-starter-web");
        DependencyInfo depA = new DependencyInfo();
        depA.setGroupId("com.example");
        depA.setArtifactId("my-lib");
        deps.add(depB);
        deps.add(depA);
        repoContext.setDependencies(deps);

        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertEquals(2, result.getDependencies().size());
        assertEquals("com.example", result.getDependencies().get(0).getGroupId());
        assertEquals("org.springframework.boot", result.getDependencies().get(1).getGroupId());
    }

    @Test
    void createDevelopmentContext_withDeterministicOrdering_relatedFilesSorted() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");

        List<RelatedFile> files = new ArrayList<>();
        RelatedFile fileB = new RelatedFile();
        fileB.setFilePath("z_file.java");
        RelatedFile fileA = new RelatedFile();
        fileA.setFilePath("a_file.java");
        files.add(fileB);
        files.add(fileA);
        repoContext.setRelatedFiles(files);

        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertEquals(2, result.getRelatedFiles().size());
        assertEquals("a_file.java", result.getRelatedFiles().get(0).getFilePath());
        assertEquals("z_file.java", result.getRelatedFiles().get(1).getFilePath());
    }

    @Test
    void createDevelopmentContext_withRepositorySummary_includesSummary() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");

        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test-repo");
        summary.setBranch("main");
        summary.setStatus("ACTIVE");
        summary.setFileCount(100);
        summary.setClassCount(50);
        summary.setCommitCount(200);
        repoContext.setRepositorySummary(summary);

        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertNotNull(result.getRepositorySummary());
        assertEquals("test-repo", result.getRepositorySummary().getRepositoryName());
        assertEquals("ACTIVE", result.getRepositorySummary().getStatus());
        assertEquals(100, result.getRepositorySummary().getFileCount());
    }

    @Test
    void createDevelopmentContext_withNullList_handlesGracefully() throws IndexerClientException {
        // Arrange
        RepositoryContext repoContext = new RepositoryContext();
        repoContext.setTask("Test");
        repoContext.setRepositoryName("test-repo");
        repoContext.setBranch("main");
        repoContext.setClasses(null);
        repoContext.setMethods(null);
        repoContext.setSpringComponents(null);
        repoContext.setRestApis(null);
        repoContext.setDependencies(null);
        repoContext.setRelatedFiles(null);

        when(contextBuilderService.buildContext(any())).thenReturn(repoContext);
        BuildContextRequest request = new BuildContextRequest("Test", "test-repo", "main");

        // Act
        DevelopmentContext result = developmentContextService.createDevelopmentContext(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRelevantClasses().isEmpty());
        assertTrue(result.getRelevantMethods().isEmpty());
        assertTrue(result.getSpringComponents().isEmpty());
        assertTrue(result.getRestApis().isEmpty());
        assertTrue(result.getDependencies().isEmpty());
        assertTrue(result.getRelatedFiles().isEmpty());
    }

    /**
     * Creates a sample RepositoryContext with data in all sections.
     */
    private RepositoryContext createSampleRepositoryContext() {
        RepositoryContext context = new RepositoryContext();
        context.setTask("Add pagination");
        context.setRepositoryName("test-repo");
        context.setBranch("main");

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
        context.setClasses(classes);

        List<MethodInfo> methods = new ArrayList<>();
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setDeclaringClass("com.example.UserController");
        methodInfo.setMethodName("findAll");
        methodInfo.setReturnType("List<User>");
        methods.add(methodInfo);
        context.setMethods(methods);

        List<SpringComponentInfo> components = new ArrayList<>();
        SpringComponentInfo component = new SpringComponentInfo();
        component.setName("userService");
        component.setComponentType("SERVICE");
        component.setClassName("UserService");
        components.add(component);
        context.setSpringComponents(components);

        List<RestEndpointInfo> apis = new ArrayList<>();
        RestEndpointInfo api = new RestEndpointInfo();
        api.setHttpMethod("GET");
        api.setEndpointPath("/api/users");
        api.setControllerName("UserController");
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