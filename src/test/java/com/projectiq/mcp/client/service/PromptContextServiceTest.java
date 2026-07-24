package com.projectiq.mcp.client.service;

import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.PackageSummary;
import com.projectiq.mcp.client.dto.PromptContext;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptContextServiceTest {

    private PromptContextService promptContextService;

    @BeforeEach
    void setUp() {
        promptContextService = new PromptContextService();
    }

    @Test
    void createPromptContext_withValidContext_returnsPopulatedPromptContext() {
        // Arrange
        DevelopmentContext devContext = createSampleDevelopmentContext();

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertNotNull(result);
        assertEquals("Add pagination to UserController", result.getTask());
        assertEquals("test-repo", result.getRepositoryName());
        assertEquals("main", result.getBranch());
        assertNotNull(result.getBuildTimestamp());
        assertNotNull(result.getRepositorySummary());
        assertEquals("test-repo", result.getRepositorySummary().getName());
        assertEquals("main", result.getRepositorySummary().getBranch());
        assertEquals(10, result.getRepositorySummary().getFileCount());
        assertEquals(5, result.getRepositorySummary().getClassCount());
        assertEquals(20, result.getRepositorySummary().getMethodCount());
        assertEquals(30, result.getRepositorySummary().getCommitCount());
        assertEquals(3, result.getRepositorySummary().getPackageCount());
    }

    @Test
    void createPromptContext_withValidContext_containsAllSections() {
        // Arrange
        DevelopmentContext devContext = createSampleDevelopmentContext();

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertNotNull(result.getRelevantPackages());
        assertNotNull(result.getRelevantClasses());
        assertNotNull(result.getRelevantMethods());
        assertNotNull(result.getSpringComponents());
        assertNotNull(result.getRestApis());
        assertNotNull(result.getRelatedFiles());
        assertNotNull(result.getRequiredDependencies());
        assertNotNull(result.getRepositoryConventions());
    }

    @Test
    void createPromptContext_withEmptyContext_returnsEmptySections() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertNotNull(result);
        assertTrue(result.getRelevantPackages().isEmpty());
        assertTrue(result.getRelevantClasses().isEmpty());
        assertTrue(result.getRelevantMethods().isEmpty());
        assertTrue(result.getSpringComponents().isEmpty());
        assertTrue(result.getRestApis().isEmpty());
        assertTrue(result.getRelatedFiles().isEmpty());
        assertTrue(result.getRequiredDependencies().isEmpty());
        assertNull(result.getRepositorySummary());
        assertNotNull(result.getRepositoryConventions());
    }

    @Test
    void createPromptContext_withErrors_copiesErrors() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        devContext.addError(new ContextBuildError("searchCode", "INDEXER_UNREACHABLE", "Cannot connect"));

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertEquals("searchCode", result.getErrors().get(0).getEndpoint());
    }

    @Test
    void createPromptContext_deterministicOrdering_classes() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo c1 = new ClassInfo();
        c1.setFullyQualifiedName("z.example.LastClass");
        ClassInfo c2 = new ClassInfo();
        c2.setFullyQualifiedName("a.example.FirstClass");
        ClassInfo c3 = new ClassInfo();
        c3.setFullyQualifiedName("m.example.MiddleClass");
        classes.add(c1);
        classes.add(c2);
        classes.add(c3);
        devContext.setRelevantClasses(classes);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertEquals(3, result.getRelevantClasses().size());
        assertEquals("a.example.FirstClass", result.getRelevantClasses().get(0).getFullyQualifiedName());
        assertEquals("m.example.MiddleClass", result.getRelevantClasses().get(1).getFullyQualifiedName());
        assertEquals("z.example.LastClass", result.getRelevantClasses().get(2).getFullyQualifiedName());
    }

    @Test
    void createPromptContext_deterministicOrdering_methods() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<MethodInfo> methods = new ArrayList<>();
        MethodInfo m1 = new MethodInfo();
        m1.setDeclaringClass("com.example.UserController");
        m1.setMethodName("findAll");
        MethodInfo m2 = new MethodInfo();
        m2.setDeclaringClass("com.example.UserController");
        m2.setMethodName("create");
        methods.add(m1);
        methods.add(m2);
        devContext.setRelevantMethods(methods);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertEquals(2, result.getRelevantMethods().size());
        assertEquals("create", result.getRelevantMethods().get(0).getMethodName());
        assertEquals("findAll", result.getRelevantMethods().get(1).getMethodName());
    }

    @Test
    void createPromptContext_deterministicOrdering_springComponents() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<SpringComponentInfo> components = new ArrayList<>();
        SpringComponentInfo s1 = new SpringComponentInfo();
        s1.setName("userService");
        SpringComponentInfo s2 = new SpringComponentInfo();
        s2.setName("authService");
        components.add(s1);
        components.add(s2);
        devContext.setSpringComponents(components);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertEquals(2, result.getSpringComponents().size());
        assertEquals("authService", result.getSpringComponents().get(0).getName());
        assertEquals("userService", result.getSpringComponents().get(1).getName());
    }

    @Test
    void createPromptContext_deterministicOrdering_restApis() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<RestEndpointInfo> apis = new ArrayList<>();
        RestEndpointInfo r1 = new RestEndpointInfo();
        r1.setHttpMethod("POST");
        r1.setEndpointPath("/api/users");
        RestEndpointInfo r2 = new RestEndpointInfo();
        r2.setHttpMethod("GET");
        r2.setEndpointPath("/api/users");
        apis.add(r1);
        apis.add(r2);
        devContext.setRestApis(apis);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertEquals(2, result.getRestApis().size());
        assertEquals("GET", result.getRestApis().get(0).getHttpMethod());
        assertEquals("POST", result.getRestApis().get(1).getHttpMethod());
    }

    @Test
    void createPromptContext_deterministicOrdering_dependencies() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<DependencyInfo> deps = new ArrayList<>();
        DependencyInfo d1 = new DependencyInfo();
        d1.setGroupId("org.springframework.boot");
        d1.setArtifactId("spring-boot-starter-web");
        DependencyInfo d2 = new DependencyInfo();
        d2.setGroupId("org.springframework.boot");
        d2.setArtifactId("spring-boot-starter-data-jpa");
        deps.add(d1);
        deps.add(d2);
        devContext.setDependencies(deps);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertEquals(2, result.getRequiredDependencies().size());
        assertEquals("spring-boot-starter-data-jpa", result.getRequiredDependencies().get(0).getArtifactId());
        assertEquals("spring-boot-starter-web", result.getRequiredDependencies().get(1).getArtifactId());
    }

    @Test
    void createPromptContext_deterministicOrdering_relatedFiles() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<RelatedFile> files = new ArrayList<>();
        RelatedFile f1 = new RelatedFile();
        f1.setFilePath("z/file.txt");
        RelatedFile f2 = new RelatedFile();
        f2.setFilePath("a/file.txt");
        files.add(f1);
        files.add(f2);
        devContext.setRelatedFiles(files);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertEquals(2, result.getRelatedFiles().size());
        assertEquals("a/file.txt", result.getRelatedFiles().get(0).getFilePath());
        assertEquals("z/file.txt", result.getRelatedFiles().get(1).getFilePath());
    }

    @Test
    void createPromptContext_extractsPackagesFromClasses() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo c1 = new ClassInfo();
        c1.setFullyQualifiedName("com.example.controller.UserController");
        ClassInfo c2 = new ClassInfo();
        c2.setFullyQualifiedName("com.example.service.UserService");
        classes.add(c1);
        classes.add(c2);
        devContext.setRelevantClasses(classes);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertTrue(result.getRelevantPackages().contains("com.example.controller"));
        assertTrue(result.getRelevantPackages().contains("com.example.service"));
    }

    @Test
    void createPromptContext_deduplicatesPackages() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo c1 = new ClassInfo();
        c1.setFullyQualifiedName("com.example.service.UserService");
        ClassInfo c2 = new ClassInfo();
        c2.setFullyQualifiedName("com.example.service.AuthService");
        classes.add(c1);
        classes.add(c2);
        devContext.setRelevantClasses(classes);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertEquals(1, result.getRelevantPackages().size());
        assertEquals("com.example.service", result.getRelevantPackages().get(0));
    }

    @Test
    void createPromptContext_extractsPackagesFromMethods() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<MethodInfo> methods = new ArrayList<>();
        MethodInfo m1 = new MethodInfo();
        m1.setDeclaringClass("com.example.controller.UserController");
        methods.add(m1);
        devContext.setRelevantMethods(methods);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertTrue(result.getRelevantPackages().contains("com.example.controller"));
    }

    @Test
    void createPromptContext_extractsPackagesFromSpringComponents() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<SpringComponentInfo> components = new ArrayList<>();
        SpringComponentInfo sci = new SpringComponentInfo();
        sci.setClassName("com.example.service.UserService");
        components.add(sci);
        devContext.setSpringComponents(components);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertTrue(result.getRelevantPackages().contains("com.example.service"));
    }

    @Test
    void createPromptContext_extractsPackagesFromRepositorySummary() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.controller");
        packages.add(pkg);
        summary.setPackages(packages);
        devContext.setRepositorySummary(summary);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertTrue(result.getRelevantPackages().contains("com.example.controller"));
    }

    @Test
    void createPromptContext_repositoryConventions_inferred() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");

        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo c1 = new ClassInfo();
        c1.setFullyQualifiedName("com.example.controller.UserController");
        classes.add(c1);
        devContext.setRelevantClasses(classes);

        List<DependencyInfo> deps = new ArrayList<>();
        DependencyInfo dep = new DependencyInfo();
        dep.setGroupId("org.springframework.boot");
        dep.setArtifactId("spring-boot-starter-web");
        dep.setVersion("3.2.0");
        deps.add(dep);
        devContext.setDependencies(deps);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertNotNull(result.getRepositoryConventions());
        assertEquals("com.example.controller", result.getRepositoryConventions().getPackageStructure());
        assertTrue(result.getRepositoryConventions().getNamingConventions().contains("Spring"));
        assertEquals("Spring Boot 3.2.0", result.getRepositoryConventions().getFrameworkVersion());
        assertEquals("Maven", result.getRepositoryConventions().getBuildTool());
        assertEquals("21", result.getRepositoryConventions().getJavaVersion());
    }

    @Test
    void createPromptContext_withNullRepositorySummary_returnsNullSummary() {
        // Arrange
        DevelopmentContext devContext = new DevelopmentContext();
        devContext.setTask("Test");
        devContext.setRepositoryName("test-repo");
        devContext.setBranch("main");
        devContext.setBuildTimestamp("2024-01-01T00:00:00Z");
        devContext.setRepositorySummary(null);

        // Act
        PromptContext result = promptContextService.createPromptContext(devContext);

        // Assert
        assertNull(result.getRepositorySummary());
    }

    @Test
    void createPromptContext_deterministicOutput_sameInputProducesSameOutput() {
        // Arrange
        DevelopmentContext devContext = createSampleDevelopmentContext();

        // Act
        PromptContext result1 = promptContextService.createPromptContext(devContext);
        PromptContext result2 = promptContextService.createPromptContext(devContext);

        // Assert
        assertEquals(result1.getTask(), result2.getTask());
        assertEquals(result1.getRepositoryName(), result2.getRepositoryName());
        assertEquals(result1.getBranch(), result2.getBranch());
        assertEquals(result1.getRelevantPackages(), result2.getRelevantPackages());
        assertEquals(result1.getRelevantClasses().size(), result2.getRelevantClasses().size());
        assertEquals(result1.getRelevantMethods().size(), result2.getRelevantMethods().size());
        assertEquals(result1.getSpringComponents().size(), result2.getSpringComponents().size());
        assertEquals(result1.getRestApis().size(), result2.getRestApis().size());
        assertEquals(result1.getRelatedFiles().size(), result2.getRelatedFiles().size());
        assertEquals(result1.getRequiredDependencies().size(), result2.getRequiredDependencies().size());
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
        summary.setMethodCount(20);
        summary.setCommitCount(30);
        summary.setPackageCount(3);

        List<PackageSummary> packages = new ArrayList<>();
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.controller");
        packages.add(pkg);
        summary.setPackages(packages);

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
        component.setClassName("com.example.service.UserService");
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