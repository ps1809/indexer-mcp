package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.PromptContext;
import com.projectiq.mcp.client.dto.PromptContext.RepositoryConventionsInfo;
import com.projectiq.mcp.client.dto.PromptContext.RepositorySummaryInfo;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import com.projectiq.mcp.client.service.DevelopmentContextService;
import com.projectiq.mcp.client.service.PromptContextService;
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
class PromptContextToolTest {

    @Mock
    private DevelopmentContextService developmentContextService;

    @Mock
    private PromptContextService promptContextService;

    private PromptContextTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tool = new PromptContextTool(developmentContextService, promptContextService);
        objectMapper = new ObjectMapper();
    }

    @Test
    void promptContext_withValidRequest_returnsJsonContext() throws Exception {
        // Arrange
        DevelopmentContext mockDevContext = createSampleDevelopmentContext();
        PromptContext mockPromptContext = createSamplePromptContext();

        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockDevContext);
        when(promptContextService.createPromptContext(any())).thenReturn(mockPromptContext);

        // Act
        String result = tool.promptContext("Implement JWT authentication", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Implement JWT authentication"));
        assertTrue(result.contains("test-repo"));
        assertTrue(result.contains("main"));
        assertTrue(result.startsWith("{") || result.startsWith("[")); // Valid JSON start
        verify(developmentContextService).createDevelopmentContext(any());
        verify(promptContextService).createPromptContext(any());
    }

    @Test
    void promptContext_returnsValidJson() throws Exception {
        // Arrange
        DevelopmentContext mockDevContext = createSampleDevelopmentContext();
        PromptContext mockPromptContext = createSamplePromptContext();

        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockDevContext);
        when(promptContextService.createPromptContext(any())).thenReturn(mockPromptContext);

        // Act
        String result = tool.promptContext("Implement JWT authentication", "test-repo", "main");

        // Assert
        assertDoesNotThrow(() -> objectMapper.readTree(result));
    }

    @Test
    void promptContext_withNullTask_returnsError() throws Exception {
        // Act
        String result = tool.promptContext(null, "test-repo", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        verifyNoInteractions(developmentContextService);
        verifyNoInteractions(promptContextService);
    }

    @Test
    void promptContext_withEmptyTask_returnsError() throws Exception {
        // Act
        String result = tool.promptContext("", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        verifyNoInteractions(developmentContextService);
        verifyNoInteractions(promptContextService);
    }

    @Test
    void promptContext_withNullRepository_returnsError() throws Exception {
        // Act
        String result = tool.promptContext("Implement JWT", null, "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        verifyNoInteractions(developmentContextService);
        verifyNoInteractions(promptContextService);
    }

    @Test
    void promptContext_withEmptyRepository_returnsError() throws Exception {
        // Act
        String result = tool.promptContext("Implement JWT", "", "main");

        // Assert
        assertTrue(result.contains("INVALID_ARGUMENT"));
        verifyNoInteractions(developmentContextService);
        verifyNoInteractions(promptContextService);
    }

    @Test
    void promptContext_withDefaultBranch_usesMain() throws Exception {
        // Arrange
        DevelopmentContext mockDevContext = new DevelopmentContext();
        mockDevContext.setTask("Test");
        mockDevContext.setRepositoryName("test-repo");
        mockDevContext.setBranch("main");
        mockDevContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        PromptContext mockPromptContext = new PromptContext();
        mockPromptContext.setTask("Test");
        mockPromptContext.setRepositoryName("test-repo");
        mockPromptContext.setBranch("main");
        mockPromptContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockDevContext);
        when(promptContextService.createPromptContext(any())).thenReturn(mockPromptContext);

        // Act
        String result = tool.promptContext("Test", "test-repo", null);

        // Assert
        assertNotNull(result);
        verify(developmentContextService).createDevelopmentContext(argThat(req -> "main".equals(req.getBranch())));
        verify(promptContextService).createPromptContext(any());
    }

    @Test
    void promptContext_withIndexerConnectionException_returnsError() throws Exception {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any()))
                .thenThrow(new IndexerConnectionException("Connection refused"));

        // Act
        String result = tool.promptContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_UNREACHABLE"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void promptContext_withIndexerTimeoutException_returnsError() throws Exception {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any()))
                .thenThrow(new IndexerTimeoutException("Timeout after 30s"));

        // Act
        String result = tool.promptContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_TIMEOUT"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void promptContext_withIndexerHttpException_returnsError() throws Exception {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any()))
                .thenThrow(new IndexerHttpException("HTTP 500", 500));

        // Act
        String result = tool.promptContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_HTTP_ERROR"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void promptContext_withIndexerClientException_returnsError() throws Exception {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any()))
                .thenThrow(new IndexerClientException("Bad request"));

        // Act
        String result = tool.promptContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INDEXER_ERROR"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void promptContext_withGenericException_returnsError() throws Exception {
        // Arrange
        when(developmentContextService.createDevelopmentContext(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String result = tool.promptContext("Test", "test-repo", "main");

        // Assert
        assertTrue(result.contains("INTERNAL_ERROR"));
        verify(developmentContextService).createDevelopmentContext(any());
    }

    @Test
    void promptContext_withErrors_includesErrorsInJson() throws Exception {
        // Arrange
        DevelopmentContext mockDevContext = new DevelopmentContext();
        mockDevContext.setTask("Test");
        mockDevContext.setRepositoryName("test-repo");
        mockDevContext.setBranch("main");
        mockDevContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        mockDevContext.addError(new ContextBuildError("searchCode", "INDEXER_UNREACHABLE", "Cannot connect"));

        PromptContext mockPromptContext = new PromptContext();
        mockPromptContext.setTask("Test");
        mockPromptContext.setRepositoryName("test-repo");
        mockPromptContext.setBranch("main");
        mockPromptContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        mockPromptContext.addError(new ContextBuildError("searchCode", "INDEXER_UNREACHABLE", "Cannot connect"));

        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockDevContext);
        when(promptContextService.createPromptContext(any())).thenReturn(mockPromptContext);

        // Act
        String result = tool.promptContext("Test", "test-repo", "main");

        // Assert
        assertNotNull(result);
        // Verify JSON is valid
        assertDoesNotThrow(() -> objectMapper.readTree(result));
        assertTrue(result.contains("searchCode"));
        assertTrue(result.contains("INDEXER_UNREACHABLE"));
    }

    @Test
    void promptContext_withEmptyContext_returnsJsonWithEmptySections() throws Exception {
        // Arrange
        DevelopmentContext mockDevContext = new DevelopmentContext();
        mockDevContext.setTask("Test");
        mockDevContext.setRepositoryName("test-repo");
        mockDevContext.setBranch("main");
        mockDevContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        PromptContext mockPromptContext = new PromptContext();
        mockPromptContext.setTask("Test");
        mockPromptContext.setRepositoryName("test-repo");
        mockPromptContext.setBranch("main");
        mockPromptContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockDevContext);
        when(promptContextService.createPromptContext(any())).thenReturn(mockPromptContext);

        // Act
        String result = tool.promptContext("Test", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertDoesNotThrow(() -> objectMapper.readTree(result));
        assertTrue(result.contains("Test"));
    }

    @Test
    void promptContext_outputContainsRequiredFields() throws Exception {
        // Arrange
        DevelopmentContext mockDevContext = createSampleDevelopmentContext();
        PromptContext mockPromptContext = createSamplePromptContext();

        when(developmentContextService.createDevelopmentContext(any())).thenReturn(mockDevContext);
        when(promptContextService.createPromptContext(any())).thenReturn(mockPromptContext);

        // Act
        String result = tool.promptContext("Implement JWT authentication", "test-repo", "main");

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("repositorySummary"));
        assertTrue(result.contains("relevantPackages"));
        assertTrue(result.contains("relevantClasses"));
        assertTrue(result.contains("relevantMethods"));
        assertTrue(result.contains("springComponents"));
        assertTrue(result.contains("restApis"));
        assertTrue(result.contains("relatedFiles"));
        assertTrue(result.contains("requiredDependencies"));
        assertTrue(result.contains("repositoryConventions"));
    }

    /**
     * Creates a sample DevelopmentContext for testing.
     */
    private DevelopmentContext createSampleDevelopmentContext() {
        DevelopmentContext context = new DevelopmentContext();
        context.setTask("Implement JWT authentication");
        context.setRepositoryName("test-repo");
        context.setBranch("main");
        context.setBuildTimestamp("2024-01-01T00:00:00Z");

        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test-repo");
        summary.setBranch("main");
        summary.setStatus("ACTIVE");
        summary.setFileCount(10);
        summary.setClassCount(5);
        summary.setMethodCount(20);
        summary.setCommitCount(30);
        summary.setPackageCount(3);
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
        methodInfo.setMethodName("authenticate");
        methods.add(methodInfo);
        context.setRelevantMethods(methods);

        List<SpringComponentInfo> components = new ArrayList<>();
        SpringComponentInfo component = new SpringComponentInfo();
        component.setName("authService");
        component.setComponentType("SERVICE");
        components.add(component);
        context.setSpringComponents(components);

        List<RestEndpointInfo> apis = new ArrayList<>();
        RestEndpointInfo api = new RestEndpointInfo();
        api.setHttpMethod("POST");
        api.setEndpointPath("/api/auth/login");
        apis.add(api);
        context.setRestApis(apis);

        List<DependencyInfo> deps = new ArrayList<>();
        DependencyInfo dep = new DependencyInfo();
        dep.setGroupId("org.springframework.boot");
        dep.setArtifactId("spring-boot-starter-security");
        deps.add(dep);
        context.setDependencies(deps);

        List<RelatedFile> files = new ArrayList<>();
        RelatedFile file = new RelatedFile();
        file.setFilePath("src/main/java/com/example/AuthController.java");
        files.add(file);
        context.setRelatedFiles(files);

        return context;
    }

    /**
     * Creates a sample PromptContext for testing.
     */
    private PromptContext createSamplePromptContext() {
        PromptContext context = new PromptContext();
        context.setTask("Implement JWT authentication");
        context.setRepositoryName("test-repo");
        context.setBranch("main");
        context.setBuildTimestamp("2024-01-01T00:00:00Z");

        RepositorySummaryInfo summary = new RepositorySummaryInfo();
        summary.setName("test-repo");
        summary.setBranch("main");
        summary.setDescription("ACTIVE");
        summary.setFileCount(10);
        summary.setClassCount(5);
        summary.setMethodCount(20);
        summary.setCommitCount(30);
        summary.setPackageCount(3);
        context.setRepositorySummary(summary);

        List<String> packages = new ArrayList<>();
        packages.add("com.example");
        packages.add("com.example.config");
        context.setRelevantPackages(packages);

        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setFullyQualifiedName("com.example.UserController");
        classInfo.setClassName("UserController");
        classes.add(classInfo);
        context.setRelevantClasses(classes);

        List<MethodInfo> methods = new ArrayList<>();
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setDeclaringClass("com.example.UserController");
        methodInfo.setMethodName("authenticate");
        methods.add(methodInfo);
        context.setRelevantMethods(methods);

        List<SpringComponentInfo> components = new ArrayList<>();
        SpringComponentInfo component = new SpringComponentInfo();
        component.setName("authService");
        component.setComponentType("SERVICE");
        components.add(component);
        context.setSpringComponents(components);

        List<RestEndpointInfo> apis = new ArrayList<>();
        RestEndpointInfo api = new RestEndpointInfo();
        api.setHttpMethod("POST");
        api.setEndpointPath("/api/auth/login");
        apis.add(api);
        context.setRestApis(apis);

        List<DependencyInfo> deps = new ArrayList<>();
        DependencyInfo dep = new DependencyInfo();
        dep.setGroupId("org.springframework.boot");
        dep.setArtifactId("spring-boot-starter-security");
        deps.add(dep);
        context.setRequiredDependencies(deps);

        List<RelatedFile> files = new ArrayList<>();
        RelatedFile file = new RelatedFile();
        file.setFilePath("src/main/java/com/example/AuthController.java");
        files.add(file);
        context.setRelatedFiles(files);

        RepositoryConventionsInfo conventions = new RepositoryConventionsInfo();
        conventions.setNamingConventions("Standard Java naming conventions");
        conventions.setPackageStructure("com.example");
        conventions.setFrameworkVersion("Spring Boot 3.x");
        conventions.setBuildTool("Maven");
        conventions.setJavaVersion("21");
        context.setRepositoryConventions(conventions);

        return context;
    }
}