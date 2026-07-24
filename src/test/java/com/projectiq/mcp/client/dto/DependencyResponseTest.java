package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DependencyResponse DTO.
 */
class DependencyResponseTest {

    @Test
    void testDefaultConstructor() {
        DependencyResponse response = new DependencyResponse();
        assertNull(response.getRepositoryName());
        assertNull(response.getTotalResults());
        assertNull(response.getDependencies());
    }

    @Test
    void testSetters() {
        DependencyResponse response = new DependencyResponse();
        List<DependencyInfo> deps = Arrays.asList(
                new DependencyInfo(),
                new DependencyInfo()
        );

        response.setRepositoryName("test-repo");
        response.setTotalResults(2);
        response.setDependencies(deps);

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals(Integer.valueOf(2), response.getTotalResults());
        assertEquals(deps, response.getDependencies());
    }

    @Test
    void testSetNullValues() {
        DependencyResponse response = new DependencyResponse();
        response.setRepositoryName(null);
        response.setTotalResults(null);
        response.setDependencies(null);

        assertNull(response.getRepositoryName());
        assertNull(response.getTotalResults());
        assertNull(response.getDependencies());
    }

    @Test
    void testSetEmptyDependencies() {
        DependencyResponse response = new DependencyResponse();
        response.setDependencies(Arrays.asList());

        assertNotNull(response.getDependencies());
        assertTrue(response.getDependencies().isEmpty());
    }

    @Test
    void testTotalResultsZero() {
        DependencyResponse response = new DependencyResponse();
        response.setTotalResults(0);
        assertEquals(Integer.valueOf(0), response.getTotalResults());
    }

    @Test
    void testTotalResultsPositive() {
        DependencyResponse response = new DependencyResponse();
        response.setTotalResults(100);
        assertEquals(Integer.valueOf(100), response.getTotalResults());
    }

    @Test
    void testRepositoryNameSetAndGet() {
        DependencyResponse response = new DependencyResponse();
        assertNull(response.getRepositoryName());

        response.setRepositoryName("my-project");
        assertEquals("my-project", response.getRepositoryName());
    }

    @Test
    void testDependenciesWithSingleItem() {
        DependencyResponse response = new DependencyResponse();
        DependencyInfo dep = new DependencyInfo();
        dep.setName("test-dep");
        dep.setGroupId("com.test");
        dep.setArtifactId("test-artifact");
        dep.setVersion("1.0.0");
        dep.setScope("compile");
        dep.setType(DependencyType.MAVEN);

        response.setDependencies(Arrays.asList(dep));

        assertEquals(1, response.getDependencies().size());
        assertEquals("test-dep", response.getDependencies().get(0).getName());
    }

    @Test
    void testDependenciesWithMultipleItems() {
        DependencyResponse response = new DependencyResponse();
        List<DependencyInfo> deps = Arrays.asList(
                createDependency("dep1", "com.test", "artifact1", "1.0.0", "compile", DependencyType.MAVEN),
                createDependency("dep2", "org.gradle", "artifact2", "2.0.0", "runtime", DependencyType.GRADLE),
                createDependency("dep3", null, "internal-mod", null, null, DependencyType.INTERNAL_MODULE)
        );

        response.setTotalResults(3);
        response.setDependencies(deps);

        assertEquals(3, response.getTotalResults());
        assertEquals(3, response.getDependencies().size());
    }

    @Test
    void testAllFieldsSet() {
        DependencyResponse response = new DependencyResponse();
        List<DependencyInfo> deps = Arrays.asList(new DependencyInfo());

        response.setRepositoryName("complete-repo");
        response.setTotalResults(1);
        response.setDependencies(deps);

        assertEquals("complete-repo", response.getRepositoryName());
        assertEquals(Integer.valueOf(1), response.getTotalResults());
        assertEquals(1, response.getDependencies().size());
    }

    @Test
    void testOverrideValues() {
        DependencyResponse response = new DependencyResponse();
        response.setRepositoryName("first");
        response.setTotalResults(1);
        assertEquals("first", response.getRepositoryName());
        assertEquals(Integer.valueOf(1), response.getTotalResults());

        response.setRepositoryName("second");
        response.setTotalResults(2);
        assertEquals("second", response.getRepositoryName());
        assertEquals(Integer.valueOf(2), response.getTotalResults());
    }

    @Test
    void testGetDependenciesReturnsSetList() {
        DependencyResponse response = new DependencyResponse();
        List<DependencyInfo> deps = Arrays.asList(new DependencyInfo());
        response.setDependencies(deps);

        List<DependencyInfo> returned = response.getDependencies();
        // Getters return the stored reference (not a copy)
        assertEquals(deps, returned);
        assertEquals(1, returned.size());
    }

    @Test
    void testSetTotalResultsToNull() {
        DependencyResponse response = new DependencyResponse();
        response.setTotalResults(10);
        assertEquals(Integer.valueOf(10), response.getTotalResults());

        response.setTotalResults(null);
        assertNull(response.getTotalResults());
    }

    @Test
    void testDependenciesWithNullFields() {
        DependencyResponse response = new DependencyResponse();
        DependencyInfo dep = new DependencyInfo();
        // All fields null by default

        response.setDependencies(Arrays.asList(dep));
        response.setTotalResults(1);

        assertEquals(1, response.getTotalResults());
        assertNull(response.getDependencies().get(0).getName());
        assertNull(response.getDependencies().get(0).getGroupId());
        assertNull(response.getDependencies().get(0).getArtifactId());
        assertNull(response.getDependencies().get(0).getVersion());
        assertNull(response.getDependencies().get(0).getScope());
        assertNull(response.getDependencies().get(0).getType());
    }

    private DependencyInfo createDependency(String name, String groupId, String artifactId,
                                              String version, String scope, DependencyType type) {
        DependencyInfo dep = new DependencyInfo();
        dep.setName(name);
        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);
        dep.setVersion(version);
        dep.setScope(scope);
        dep.setType(type);
        return dep;
    }
}