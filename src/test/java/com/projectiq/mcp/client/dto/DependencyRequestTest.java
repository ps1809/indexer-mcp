package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DependencyRequest DTO.
 */
class DependencyRequestTest {

    @Test
    void testDefaultConstructor() {
        DependencyRequest request = new DependencyRequest();
        assertNull(request.getRepositoryName());
        assertNull(request.getBranch());
        assertNull(request.getPackageName());
        assertNull(request.getDependencyTypes());
    }

    @Test
    void testSetters() {
        DependencyRequest request = new DependencyRequest();
        List<String> types = Arrays.asList("MAVEN", "GRADLE");

        request.setRepositoryName("test-repo");
        request.setBranch("develop");
        request.setPackageName("com.example");
        request.setDependencyTypes(types);

        assertEquals("test-repo", request.getRepositoryName());
        assertEquals("develop", request.getBranch());
        assertEquals("com.example", request.getPackageName());
        assertEquals(types, request.getDependencyTypes());
    }

    @Test
    void testSetNullValues() {
        DependencyRequest request = new DependencyRequest();
        request.setRepositoryName(null);
        request.setBranch(null);
        request.setPackageName(null);
        request.setDependencyTypes(null);

        assertNull(request.getRepositoryName());
        assertNull(request.getBranch());
        assertNull(request.getPackageName());
        assertNull(request.getDependencyTypes());
    }

    @Test
    void testSetEmptyList() {
        DependencyRequest request = new DependencyRequest();
        request.setDependencyTypes(Arrays.asList());

        assertNotNull(request.getDependencyTypes());
        assertTrue(request.getDependencyTypes().isEmpty());
    }

    @Test
    void testRepositoryNameRequired() {
        DependencyRequest request = new DependencyRequest();
        request.setRepositoryName("my-repo");
        assertEquals("my-repo", request.getRepositoryName());
    }

    @Test
    void testBranchOptional() {
        DependencyRequest request = new DependencyRequest();
        assertNull(request.getBranch());

        request.setBranch("main");
        assertEquals("main", request.getBranch());

        request.setBranch("");
        assertEquals("", request.getBranch());
    }

    @Test
    void testPackageNameOptional() {
        DependencyRequest request = new DependencyRequest();
        assertNull(request.getPackageName());

        request.setPackageName("org.springframework");
        assertEquals("org.springframework", request.getPackageName());
    }

    @Test
    void testDependencyTypesMultiple() {
        DependencyRequest request = new DependencyRequest();
        List<String> types = Arrays.asList("MAVEN", "GRADLE", "EXTERNAL_LIBRARY");
        request.setDependencyTypes(types);

        assertNotNull(request.getDependencyTypes());
        assertEquals(3, request.getDependencyTypes().size());
        assertTrue(request.getDependencyTypes().contains("MAVEN"));
        assertTrue(request.getDependencyTypes().contains("GRADLE"));
        assertTrue(request.getDependencyTypes().contains("EXTERNAL_LIBRARY"));
    }

    @Test
    void testDependencyTypesSingle() {
        DependencyRequest request = new DependencyRequest();
        List<String> types = Arrays.asList("INTERNAL_MODULE");
        request.setDependencyTypes(types);

        assertNotNull(request.getDependencyTypes());
        assertEquals(1, request.getDependencyTypes().size());
        assertEquals("INTERNAL_MODULE", request.getDependencyTypes().get(0));
    }

    @Test
    void testSetDependencyTypesWithNullElements() {
        DependencyRequest request = new DependencyRequest();
        List<String> types = Arrays.asList("MAVEN", null, "GRADLE");
        request.setDependencyTypes(types);

        assertNotNull(request.getDependencyTypes());
        assertEquals(3, request.getDependencyTypes().size());
        assertNull(request.getDependencyTypes().get(1));
    }

    @Test
    void testAllFieldsSet() {
        DependencyRequest request = new DependencyRequest();
        List<String> types = Arrays.asList("MAVEN", "INTERNAL_MODULE");

        request.setRepositoryName("project-repo");
        request.setBranch("feature-branch");
        request.setPackageName("io.projectiq");
        request.setDependencyTypes(types);

        assertEquals("project-repo", request.getRepositoryName());
        assertEquals("feature-branch", request.getBranch());
        assertEquals("io.projectiq", request.getPackageName());
        assertEquals(types, request.getDependencyTypes());
    }

    @Test
    void testOverrideValues() {
        DependencyRequest request = new DependencyRequest();
        request.setRepositoryName("first");
        assertEquals("first", request.getRepositoryName());

        request.setRepositoryName("second");
        assertEquals("second", request.getRepositoryName());
    }

    @Test
    void testSetBranchToNullAfterSet() {
        DependencyRequest request = new DependencyRequest();
        request.setBranch("main");
        assertEquals("main", request.getBranch());

        request.setBranch(null);
        assertNull(request.getBranch());
    }

    @Test
    void testGetDependencyTypesReturnsSetList() {
        DependencyRequest request = new DependencyRequest();
        List<String> types = Arrays.asList("MAVEN");
        request.setDependencyTypes(types);

        List<String> returned = request.getDependencyTypes();
        // Getters return the stored reference (not a copy)
        assertEquals(types, returned);
        assertTrue(returned.contains("MAVEN"));
    }
}