package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClassRequest DTO.
 */
class ClassRequestTest {

    @Test
    void testGettersAndSetters() {
        ClassRequest request = new ClassRequest();

        request.setRepositoryName("test-repo");
        assertEquals("test-repo", request.getRepositoryName());

        request.setClassName("MyClass");
        assertEquals("MyClass", request.getClassName());

        request.setPackageName("com.example");
        assertEquals("com.example", request.getPackageName());

        request.setBranch("main");
        assertEquals("main", request.getBranch());

        List<String> classTypes = Arrays.asList("CLASS", "INTERFACE");
        request.setClassTypes(classTypes);
        assertEquals(classTypes, request.getClassTypes());
    }

    @Test
    void testNoArgsConstructor() {
        ClassRequest request = new ClassRequest();
        assertNotNull(request);
    }

    @Test
    void testNullValues() {
        ClassRequest request = new ClassRequest();
        assertNull(request.getRepositoryName());
        assertNull(request.getClassName());
        assertNull(request.getPackageName());
        assertNull(request.getBranch());
        assertNull(request.getClassTypes());
    }

    @Test
    void testClassNameNotNull() {
        ClassRequest request = new ClassRequest();
        request.setClassName("String");
        assertEquals("String", request.getClassName());
    }

    @Test
    void testRepositoryNameRequired() {
        ClassRequest request = new ClassRequest();
        request.setRepositoryName("my-repo");
        assertNotNull(request.getRepositoryName());
        assertFalse(request.getRepositoryName().isEmpty());
    }

    @Test
    void testPackageNameFilter() {
        ClassRequest request = new ClassRequest();
        request.setPackageName("org.springframework");
        assertEquals("org.springframework", request.getPackageName());
    }

    @Test
    void testBranchDefaultValue() {
        ClassRequest request = new ClassRequest();
        assertNull(request.getBranch());
        request.setBranch("develop");
        assertEquals("develop", request.getBranch());
    }

    @Test
    void testClassTypesMultiple() {
        ClassRequest request = new ClassRequest();
        List<String> classTypes = Arrays.asList("CLASS", "INTERFACE", "ENUM");
        request.setClassTypes(classTypes);
        assertEquals(3, request.getClassTypes().size());
    }

    @Test
    void testClassTypesSingle() {
        ClassRequest request = new ClassRequest();
        List<String> classTypes = Arrays.asList("RECORD");
        request.setClassTypes(classTypes);
        assertEquals(1, request.getClassTypes().size());
        assertEquals("RECORD", request.getClassTypes().get(0));
    }

    @Test
    void testComplexRequest() {
        ClassRequest request = new ClassRequest();
        request.setRepositoryName("spring-framework");
        request.setClassName("RestController");
        request.setPackageName("org.springframework.web");
        request.setBranch("main");
        request.setClassTypes(Arrays.asList("ANNOTATION", "INTERFACE"));

        assertEquals("spring-framework", request.getRepositoryName());
        assertEquals("RestController", request.getClassName());
        assertEquals("org.springframework.web", request.getPackageName());
        assertEquals("main", request.getBranch());
        assertEquals(2, request.getClassTypes().size());
    }
}