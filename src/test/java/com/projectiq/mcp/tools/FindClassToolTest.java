package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ClassRequest;
import com.projectiq.mcp.client.dto.ClassResponse;
import com.projectiq.mcp.client.dto.ClassType;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FindClassTool MCP tool.
 */
class FindClassToolTest {

    private IndexerRestClient mockIndexerClient;
    private FindClassTool findClassTool;

    @BeforeEach
    void setUp() {
        mockIndexerClient = mock(IndexerRestClient.class);
        findClassTool = new FindClassTool(mockIndexerClient);
    }

    // === Success Tests ===

    @Test
    void findClass_shouldReturnClassMetadataOnSuccess() {
        ClassResponse response = createMockClassResponse();

        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        String result = findClassTool.findClass(
                "spring-framework",
                "RestController",
                "org.springframework.web",
                Arrays.asList("CLASS", "ANNOTATION"),
                "main"
        );

        assertNotNull(result);
        assertTrue(result.contains("Class Search Results"));
        assertTrue(result.contains("Repository: spring-framework"));
        assertTrue(result.contains("Total Results: 2"));
        assertTrue(result.contains("Class: RestController"));
        assertTrue(result.contains("Package: org.springframework.web"));
        assertTrue(result.contains("Fully Qualified Name: org.springframework.web.RestController"));
        assertTrue(result.contains("Type: ANNOTATION"));
        assertTrue(result.contains("Visibility: public"));
        assertTrue(result.contains("Annotations: @Documented, @Retention, @Target"));
        assertTrue(result.contains("Source Location: spring-web/org/springframework/web/RestController.java"));
    }

    @Test
    void findClass_shouldReturnAllClassTypes() {
        ClassResponse response = createClassResponseWithAllTypes();

        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        String result = findClassTool.findClass("test-repo", "Test", null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("Type: CLASS"));
        assertTrue(result.contains("Type: INTERFACE"));
        assertTrue(result.contains("Type: ENUM"));
        assertTrue(result.contains("Type: RECORD"));
        assertTrue(result.contains("Type: ANNOTATION"));
    }

    @Test
    void findClass_shouldReturnInterfacesWhenPresent() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("List");
        classInfo.setPackageName("java.util");
        classInfo.setFullyQualifiedName("java.util.List");
        classInfo.setClassType(ClassType.INTERFACE);
        classInfo.setVisibility("public");
        classInfo.setImplementedInterfaces(Arrays.asList("Serializable", "Comparable"));
        classInfo.setSourceFileLocation("src/main/java/java/util/List.java");

        ClassResponse response = new ClassResponse();
        response.setRepositoryName("jdk");
        response.setTotalResults(1);
        response.setClasses(Arrays.asList(classInfo));

        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        String result = findClassTool.findClass("jdk", "List", null, null, null);

        assertTrue(result.contains("Interfaces: Serializable, Comparable"));
    }

    @Test
    void findClass_shouldShowParentClassWhenPresent() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("MyClass");
        classInfo.setParentClass("Object");
        classInfo.setFullyQualifiedName("com.example.MyClass");

        ClassResponse response = new ClassResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        response.setClasses(Arrays.asList(classInfo));

        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        String result = findClassTool.findClass("test-repo", "MyClass", null, null, null);

        assertTrue(result.contains("Parent Class: Object"));
    }

    @Test
    void findClass_shouldHandleEmptyResults() {
        ClassResponse response = new ClassResponse();
        response.setRepositoryName("empty-repo");
        response.setTotalResults(0);
        response.setClasses(Arrays.asList());

        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        String result = findClassTool.findClass("empty-repo", "NonExistent", null, null, null);

        assertTrue(result.contains("No classes found."));
    }

    // === Error Handling Tests ===

    @Test
    void findClass_shouldReturnErrorWhenRepositoryNameIsNull() {
        String result = findClassTool.findClass(null, "TestClass", null, null, null);

        assertTrue(result.contains("Error [INVALID_ARGUMENT]"));
        assertTrue(result.contains("repositoryName is required"));
    }

    @Test
    void findClass_shouldReturnErrorWhenRepositoryNameIsEmpty() {
        String result = findClassTool.findClass("", "TestClass", null, null, null);

        assertTrue(result.contains("Error [INVALID_ARGUMENT]"));
        assertTrue(result.contains("repositoryName is required"));
    }

    private static class ConnectionRefusedException extends RuntimeException {
        public ConnectionRefusedException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    @Test
    void findClass_shouldHandleConnectionFailure() {
        when(mockIndexerClient.findClass(any(ClassRequest.class)))
                .thenThrow(new IndexerConnectionException("Connection refused"));

        String result = findClassTool.findClass("test-repo", "TestClass", null, null, null);

        assertTrue(result.contains("Error [INDEXER_UNREACHABLE]"));
        assertTrue(result.contains("Cannot connect to ProjectIQ Indexer"));
    }

    @Test
    void findClass_shouldHandleTimeout() {
        when(mockIndexerClient.findClass(any(ClassRequest.class)))
                .thenThrow(new IndexerTimeoutException("Read timed out"));

        String result = findClassTool.findClass("test-repo", "TestClass", null, null, null);

        assertTrue(result.contains("Error [INDEXER_TIMEOUT]"));
        assertTrue(result.contains("request timed out"));
    }

    @Test
    void findClass_shouldHandleHttpError() {
        when(mockIndexerClient.findClass(any(ClassRequest.class)))
                .thenThrow(new IndexerHttpException("Indexer HTTP error: 500 - Internal Server Error", 500));

        String result = findClassTool.findClass("test-repo", "TestClass", null, null, null);

        assertTrue(result.contains("Error [INDEXER_HTTP_ERROR]"));
        assertTrue(result.contains("Indexer HTTP error"));
    }

    @Test
    void findClass_shouldHandleHttpClientException() {
        when(mockIndexerClient.findClass(any(ClassRequest.class)))
                .thenThrow(new IndexerClientException("Failed to deserialize response"));

        String result = findClassTool.findClass("test-repo", "TestClass", null, null, null);

        assertTrue(result.contains("Error [INDEXER_ERROR]"));
        assertTrue(result.contains("Indexer client error"));
    }

    @Test
    void findClass_shouldHandleUnexpectedException() {
        when(mockIndexerClient.findClass(any(ClassRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        String result = findClassTool.findClass("test-repo", "TestClass", null, null, null);

        assertTrue(result.contains("Error [INTERNAL_ERROR]"));
        assertTrue(result.contains("Internal error"));
    }

    // === Optional Parameter Tests ===

    @Test
    void findClass_shouldWorkWithOnlyRequiredParameter() {
        ClassResponse response = createMockClassResponse();
        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        String result = findClassTool.findClass("test-repo", null, null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("Class Search Results"));
    }

    @Test
    void findClass_shouldIncludeOptionalFiltersWhenProvided() {
        ClassResponse response = createMockClassResponse();
        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        findClassTool.findClass(
                "test-repo",
                "MyClass",
                "com.example",
                Arrays.asList("CLASS"),
                "develop"
        );

        verify(mockIndexerClient).findClass(argThat(request -> {
            assertEquals("test-repo", request.getRepositoryName());
            assertEquals("MyClass", request.getClassName());
            assertEquals("com.example", request.getPackageName());
            assertEquals("develop", request.getBranch());
            assertEquals(1, request.getClassTypes().size());
            return true;
        }));
    }

    // === SUPPORTED_CLASS_TYPES constant test ===

    @Test
    void supportedClassTypes_shouldContainAllTypes() {
        List<String> classTypes = FindClassTool.SUPPORTED_CLASS_TYPES;

        assertEquals(5, classTypes.size());
        assertTrue(classTypes.contains("CLASS"));
        assertTrue(classTypes.contains("INTERFACE"));
        assertTrue(classTypes.contains("ENUM"));
        assertTrue(classTypes.contains("RECORD"));
        assertTrue(classTypes.contains("ANNOTATION"));
    }

    // === Null Safety Tests ===

    @Test
    void findClass_shouldHandleNullClassTypeInResponse() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("TestClass");
        classInfo.setClassType(null);
        classInfo.setParentClass(null);

        ClassResponse response = new ClassResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        response.setClasses(Arrays.asList(classInfo));

        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        String result = findClassTool.findClass("test-repo", "TestClass", null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("N/A"));
    }

    @Test
    void findClass_shouldHandleNullAnnotationsAndInterfaces() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("TestClass");
        classInfo.setFullyQualifiedName("com.example.TestClass");
        classInfo.setClassType(ClassType.CLASS);
        classInfo.setVisibility("public");
        classInfo.setParentClass(null);
        classInfo.setImplementedInterfaces(null);
        classInfo.setAnnotations(null);
        classInfo.setSourceFileLocation("src/main/java/com/example/TestClass.java");

        ClassResponse response = new ClassResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(1);
        response.setClasses(Arrays.asList(classInfo));

        when(mockIndexerClient.findClass(any(ClassRequest.class))).thenReturn(response);

        String result = findClassTool.findClass("test-repo", "TestClass", null, null, null);

        assertNotNull(result);
        assertTrue(result.contains("Class: TestClass"));
    }

    // === Helper Methods ===

    private ClassResponse createMockClassResponse() {
        ClassInfo classInfo1 = new ClassInfo();
        classInfo1.setPackageName("org.springframework.web");
        classInfo1.setClassName("RestController");
        classInfo1.setFullyQualifiedName("org.springframework.web.RestController");
        classInfo1.setClassType(ClassType.ANNOTATION);
        classInfo1.setVisibility("public");
        classInfo1.setParentClass(null);
        classInfo1.setImplementedInterfaces(Arrays.asList());
        classInfo1.setAnnotations(Arrays.asList("@Documented", "@Retention", "@Target"));
        classInfo1.setSourceFileLocation("spring-web/org/springframework/web/RestController.java");

        ClassInfo classInfo2 = new ClassInfo();
        classInfo2.setPackageName("com.example.controller");
        classInfo2.setClassName("RestController");
        classInfo2.setFullyQualifiedName("com.example.controller.RestController");
        classInfo2.setClassType(ClassType.CLASS);
        classInfo2.setVisibility("public");
        classInfo2.setParentClass("ControllerSupport");
        classInfo2.setImplementedInterfaces(Arrays.asList("InitializingBean"));
        classInfo2.setAnnotations(Arrays.asList("@RestController", "@RequestMapping"));
        classInfo2.setSourceFileLocation("src/main/java/com/example/controller/RestController.java");

        ClassResponse response = new ClassResponse();
        response.setRepositoryName("spring-framework");
        response.setTotalResults(2);
        response.setClasses(Arrays.asList(classInfo1, classInfo2));

        return response;
    }

    private ClassResponse createClassResponseWithAllTypes() {
        List<ClassInfo> classes = Arrays.asList(
                createClassInfo("TestClass", "com.example", ClassType.CLASS),
                createClassInfo("TestInterface", "com.example", ClassType.INTERFACE),
                createClassInfo("TestEnum", "com.example", ClassType.ENUM),
                createClassInfo("TestRecord", "com.example", ClassType.RECORD),
                createClassInfo("TestAnnotation", "com.example", ClassType.ANNOTATION)
        );

        ClassResponse response = new ClassResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(5);
        response.setClasses(classes);

        return response;
    }

    private ClassInfo createClassInfo(String className, String packageName, ClassType classType) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(className);
        classInfo.setPackageName(packageName);
        classInfo.setFullyQualifiedName(packageName + "." + className);
        classInfo.setClassType(classType);
        classInfo.setVisibility("public");
        classInfo.setParentClass(null);
        classInfo.setImplementedInterfaces(Arrays.asList());
        classInfo.setAnnotations(Arrays.asList());
        classInfo.setSourceFileLocation("src/main/java/" + packageName.replace(".", "/") + "/" + className + ".java");
        return classInfo;
    }
}