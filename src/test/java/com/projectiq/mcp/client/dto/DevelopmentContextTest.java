package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DevelopmentContextTest {

    @Test
    void defaultConstructor_createsEmptyContext() {
        DevelopmentContext context = new DevelopmentContext();

        assertNull(context.getTask());
        assertNull(context.getRepositoryName());
        assertNull(context.getBranch());
        assertNull(context.getBuildTimestamp());
        assertNull(context.getRepositorySummary());
        assertTrue(context.getRelevantClasses().isEmpty());
        assertTrue(context.getRelevantMethods().isEmpty());
        assertTrue(context.getSpringComponents().isEmpty());
        assertTrue(context.getRestApis().isEmpty());
        assertTrue(context.getDependencies().isEmpty());
        assertTrue(context.getRelatedFiles().isEmpty());
        assertTrue(context.getErrors().isEmpty());
        assertFalse(context.hasErrors());
    }

    @Test
    void setTask_returnsTask() {
        DevelopmentContext context = new DevelopmentContext();
        context.setTask("Add pagination to UserController");
        assertEquals("Add pagination to UserController", context.getTask());
    }

    @Test
    void setRepositoryName_returnsRepositoryName() {
        DevelopmentContext context = new DevelopmentContext();
        context.setRepositoryName("my-repo");
        assertEquals("my-repo", context.getRepositoryName());
    }

    @Test
    void setBranch_returnsBranch() {
        DevelopmentContext context = new DevelopmentContext();
        context.setBranch("develop");
        assertEquals("develop", context.getBranch());
    }

    @Test
    void setBuildTimestamp_returnsBuildTimestamp() {
        DevelopmentContext context = new DevelopmentContext();
        context.setBuildTimestamp("2024-01-01T00:00:00Z");
        assertEquals("2024-01-01T00:00:00Z", context.getBuildTimestamp());
    }

    @Test
    void setRepositorySummary_returnsRepositorySummary() {
        DevelopmentContext context = new DevelopmentContext();
        RepositorySummaryResponse summary = new RepositorySummaryResponse();
        summary.setRepositoryName("test-repo");
        context.setRepositorySummary(summary);
        assertNotNull(context.getRepositorySummary());
        assertEquals("test-repo", context.getRepositorySummary().getRepositoryName());
    }

    @Test
    void setRelevantClasses_returnsRelevantClasses() {
        DevelopmentContext context = new DevelopmentContext();
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
        DevelopmentContext context = new DevelopmentContext();
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
        DevelopmentContext context = new DevelopmentContext();
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
        DevelopmentContext context = new DevelopmentContext();
        List<RestEndpointInfo> apis = new ArrayList<>();
        RestEndpointInfo api = new RestEndpointInfo();
        api.setEndpointPath("/api/users");
        apis.add(api);
        context.setRestApis(apis);
        assertEquals(1, context.getRestApis().size());
        assertEquals("/api/users", context.getRestApis().get(0).getEndpointPath());
    }

    @Test
    void setDependencies_returnsDependencies() {
        DevelopmentContext context = new DevelopmentContext();
        List<DependencyInfo> dependencies = new ArrayList<>();
        DependencyInfo dep = new DependencyInfo();
        dep.setArtifactId("spring-boot-starter-web");
        dependencies.add(dep);
        context.setDependencies(dependencies);
        assertEquals(1, context.getDependencies().size());
        assertEquals("spring-boot-starter-web", context.getDependencies().get(0).getArtifactId());
    }

    @Test
    void setRelatedFiles_returnsRelatedFiles() {
        DevelopmentContext context = new DevelopmentContext();
        List<RelatedFile> files = new ArrayList<>();
        RelatedFile file = new RelatedFile();
        file.setFilePath("src/main/java/UserController.java");
        files.add(file);
        context.setRelatedFiles(files);
        assertEquals(1, context.getRelatedFiles().size());
        assertEquals("src/main/java/UserController.java", context.getRelatedFiles().get(0).getFilePath());
    }

    @Test
    void setNullLists_createsEmptyLists() {
        DevelopmentContext context = new DevelopmentContext();
        context.setRelevantClasses(null);
        context.setRelevantMethods(null);
        context.setSpringComponents(null);
        context.setRestApis(null);
        context.setDependencies(null);
        context.setRelatedFiles(null);
        context.setErrors(null);

        assertTrue(context.getRelevantClasses().isEmpty());
        assertTrue(context.getRelevantMethods().isEmpty());
        assertTrue(context.getSpringComponents().isEmpty());
        assertTrue(context.getRestApis().isEmpty());
        assertTrue(context.getDependencies().isEmpty());
        assertTrue(context.getRelatedFiles().isEmpty());
        assertTrue(context.getErrors().isEmpty());
    }

    @Test
    void addError_errorsListPopulated() {
        DevelopmentContext context = new DevelopmentContext();
        context.addError(new ContextBuildError("classes", "INDEXER_UNREACHABLE", "Cannot connect"));
        assertTrue(context.hasErrors());
        assertEquals(1, context.getErrors().size());
        assertEquals("classes", context.getErrors().get(0).getEndpoint());
    }

    @Test
    void hasErrors_withErrors_returnsTrue() {
        DevelopmentContext context = new DevelopmentContext();
        assertFalse(context.hasErrors());
        context.addError(new ContextBuildError("test", "ERROR", "test error"));
        assertTrue(context.hasErrors());
    }

    @Test
    void toString_containsContextInfo() {
        DevelopmentContext context = new DevelopmentContext();
        context.setTask("Test task");
        context.setRepositoryName("test-repo");
        String str = context.toString();
        assertTrue(str.contains("Test task"));
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("DevelopmentContext{"));
    }
}