package com.projectiq.mcp.client.dto;

import com.projectiq.mcp.client.dto.PromptContext.RepositoryConventionsInfo;
import com.projectiq.mcp.client.dto.PromptContext.RepositorySummaryInfo;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptContextTest {

    @Test
    void defaultConstructor_createsEmptyContext() {
        PromptContext context = new PromptContext();

        assertNull(context.getTask());
        assertNull(context.getRepositoryName());
        assertNull(context.getBranch());
        assertNull(context.getBuildTimestamp());
        assertNull(context.getRepositorySummary());
        assertTrue(context.getRelevantPackages().isEmpty());
        assertTrue(context.getRelevantClasses().isEmpty());
        assertTrue(context.getRelevantMethods().isEmpty());
        assertTrue(context.getSpringComponents().isEmpty());
        assertTrue(context.getRestApis().isEmpty());
        assertTrue(context.getRelatedFiles().isEmpty());
        assertTrue(context.getRequiredDependencies().isEmpty());
        assertNull(context.getRepositoryConventions());
        assertTrue(context.getErrors().isEmpty());
        assertFalse(context.hasErrors());
    }

    @Test
    void setTask_returnsTask() {
        PromptContext context = new PromptContext();
        context.setTask("Implement JWT authentication");
        assertEquals("Implement JWT authentication", context.getTask());
    }

    @Test
    void setRepositoryName_returnsRepositoryName() {
        PromptContext context = new PromptContext();
        context.setRepositoryName("my-repo");
        assertEquals("my-repo", context.getRepositoryName());
    }

    @Test
    void setBranch_returnsBranch() {
        PromptContext context = new PromptContext();
        context.setBranch("develop");
        assertEquals("develop", context.getBranch());
    }

    @Test
    void setBuildTimestamp_returnsBuildTimestamp() {
        PromptContext context = new PromptContext();
        context.setBuildTimestamp("2024-01-01T00:00:00Z");
        assertEquals("2024-01-01T00:00:00Z", context.getBuildTimestamp());
    }

    @Test
    void setRepositorySummary_returnsRepositorySummary() {
        PromptContext context = new PromptContext();
        RepositorySummaryInfo summary = new RepositorySummaryInfo();
        summary.setName("test-repo");
        context.setRepositorySummary(summary);
        assertNotNull(context.getRepositorySummary());
        assertEquals("test-repo", context.getRepositorySummary().getName());
    }

    @Test
    void setRelevantPackages_returnsPackages() {
        PromptContext context = new PromptContext();
        List<String> packages = new ArrayList<>();
        packages.add("com.example.service");
        packages.add("com.example.controller");
        context.setRelevantPackages(packages);
        assertEquals(2, context.getRelevantPackages().size());
        assertTrue(context.getRelevantPackages().contains("com.example.service"));
    }

    @Test
    void setRelevantClasses_returnsRelevantClasses() {
        PromptContext context = new PromptContext();
        List<ClassInfo> classes = new ArrayList<>();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("UserController");
        classes.add(classInfo);
        context.setRelevantClasses(classes);
        assertEquals(1, context.getRelevantClasses().size());
        assertEquals("UserController", context.getRelevantClasses().get(0).getClassName());
    }

    @Test
    void setRelevantMethods_returnsRelevantMethods() {
        PromptContext context = new PromptContext();
        List<MethodInfo> methods = new ArrayList<>();
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("findAll");
        methods.add(methodInfo);
        context.setRelevantMethods(methods);
        assertEquals(1, context.getRelevantMethods().size());
        assertEquals("findAll", context.getRelevantMethods().get(0).getMethodName());
    }

    @Test
    void setSpringComponents_returnsSpringComponents() {
        PromptContext context = new PromptContext();
        List<SpringComponentInfo> components = new ArrayList<>();
        SpringComponentInfo component = new SpringComponentInfo();
        component.setName("userService");
        components.add(component);
        context.setSpringComponents(components);
        assertEquals(1, context.getSpringComponents().size());
        assertEquals("userService", context.getSpringComponents().get(0).getName());
    }

    @Test
    void setRestApis_returnsRestApis() {
        PromptContext context = new PromptContext();
        List<RestEndpointInfo> apis = new ArrayList<>();
        RestEndpointInfo api = new RestEndpointInfo();
        api.setEndpointPath("/api/users");
        apis.add(api);
        context.setRestApis(apis);
        assertEquals(1, context.getRestApis().size());
        assertEquals("/api/users", context.getRestApis().get(0).getEndpointPath());
    }

    @Test
    void setRelatedFiles_returnsRelatedFiles() {
        PromptContext context = new PromptContext();
        List<RelatedFile> files = new ArrayList<>();
        RelatedFile file = new RelatedFile();
        file.setFilePath("src/main/java/UserController.java");
        files.add(file);
        context.setRelatedFiles(files);
        assertEquals(1, context.getRelatedFiles().size());
        assertEquals("src/main/java/UserController.java", context.getRelatedFiles().get(0).getFilePath());
    }

    @Test
    void setRequiredDependencies_returnsDependencies() {
        PromptContext context = new PromptContext();
        List<DependencyInfo> deps = new ArrayList<>();
        DependencyInfo dep = new DependencyInfo();
        dep.setArtifactId("spring-boot-starter-web");
        deps.add(dep);
        context.setRequiredDependencies(deps);
        assertEquals(1, context.getRequiredDependencies().size());
        assertEquals("spring-boot-starter-web", context.getRequiredDependencies().get(0).getArtifactId());
    }

    @Test
    void setRepositoryConventions_returnsConventions() {
        PromptContext context = new PromptContext();
        RepositoryConventionsInfo conventions = new RepositoryConventionsInfo();
        conventions.setBuildTool("Maven");
        context.setRepositoryConventions(conventions);
        assertNotNull(context.getRepositoryConventions());
        assertEquals("Maven", context.getRepositoryConventions().getBuildTool());
    }

    @Test
    void setNullLists_createsEmptyLists() {
        PromptContext context = new PromptContext();
        context.setRelevantPackages(null);
        context.setRelevantClasses(null);
        context.setRelevantMethods(null);
        context.setSpringComponents(null);
        context.setRestApis(null);
        context.setRelatedFiles(null);
        context.setRequiredDependencies(null);
        context.setErrors(null);

        assertTrue(context.getRelevantPackages().isEmpty());
        assertTrue(context.getRelevantClasses().isEmpty());
        assertTrue(context.getRelevantMethods().isEmpty());
        assertTrue(context.getSpringComponents().isEmpty());
        assertTrue(context.getRestApis().isEmpty());
        assertTrue(context.getRelatedFiles().isEmpty());
        assertTrue(context.getRequiredDependencies().isEmpty());
        assertTrue(context.getErrors().isEmpty());
    }

    @Test
    void addError_errorsListPopulated() {
        PromptContext context = new PromptContext();
        context.addError(new ContextBuildError("classes", "INDEXER_UNREACHABLE", "Cannot connect"));
        assertTrue(context.hasErrors());
        assertEquals(1, context.getErrors().size());
        assertEquals("classes", context.getErrors().get(0).getEndpoint());
    }

    @Test
    void hasErrors_withErrors_returnsTrue() {
        PromptContext context = new PromptContext();
        assertFalse(context.hasErrors());
        context.addError(new ContextBuildError("test", "ERROR", "test error"));
        assertTrue(context.hasErrors());
    }

    @Test
    void toString_containsContextInfo() {
        PromptContext context = new PromptContext();
        context.setTask("Test task");
        context.setRepositoryName("test-repo");
        String str = context.toString();
        assertTrue(str.contains("Test task"));
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("PromptContext{"));
    }

    // RepositorySummaryInfo Tests

    @Test
    void repositorySummaryInfo_defaultConstructor() {
        RepositorySummaryInfo info = new RepositorySummaryInfo();
        assertNull(info.getName());
        assertNull(info.getBranch());
        assertNull(info.getDescription());
        assertEquals(0, info.getFileCount());
        assertEquals(0, info.getClassCount());
        assertEquals(0, info.getMethodCount());
        assertEquals(0, info.getCommitCount());
        assertEquals(0, info.getPackageCount());
    }

    @Test
    void repositorySummaryInfo_settersAndGetters() {
        RepositorySummaryInfo info = new RepositorySummaryInfo();
        info.setName("test-repo");
        info.setBranch("main");
        info.setDescription("ACTIVE");
        info.setFileCount(10);
        info.setClassCount(5);
        info.setMethodCount(20);
        info.setCommitCount(30);
        info.setPackageCount(3);

        assertEquals("test-repo", info.getName());
        assertEquals("main", info.getBranch());
        assertEquals("ACTIVE", info.getDescription());
        assertEquals(10, info.getFileCount());
        assertEquals(5, info.getClassCount());
        assertEquals(20, info.getMethodCount());
        assertEquals(30, info.getCommitCount());
        assertEquals(3, info.getPackageCount());
    }

    // RepositoryConventionsInfo Tests

    @Test
    void repositoryConventionsInfo_defaultConstructor() {
        RepositoryConventionsInfo conventions = new RepositoryConventionsInfo();
        assertNull(conventions.getNamingConventions());
        assertNull(conventions.getPackageStructure());
        assertNull(conventions.getFrameworkVersion());
        assertNull(conventions.getBuildTool());
        assertNull(conventions.getJavaVersion());
    }

    @Test
    void repositoryConventionsInfo_settersAndGetters() {
        RepositoryConventionsInfo conventions = new RepositoryConventionsInfo();
        conventions.setNamingConventions("Standard Java naming conventions");
        conventions.setPackageStructure("com.example");
        conventions.setFrameworkVersion("Spring Boot 3.x");
        conventions.setBuildTool("Maven");
        conventions.setJavaVersion("21");

        assertEquals("Standard Java naming conventions", conventions.getNamingConventions());
        assertEquals("com.example", conventions.getPackageStructure());
        assertEquals("Spring Boot 3.x", conventions.getFrameworkVersion());
        assertEquals("Maven", conventions.getBuildTool());
        assertEquals("21", conventions.getJavaVersion());
    }
}