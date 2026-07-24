package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DependencyInfo DTO.
 */
class DependencyInfoTest {

    @Test
    void testDefaultConstructor() {
        DependencyInfo info = new DependencyInfo();
        assertNull(info.getName());
        assertNull(info.getGroupId());
        assertNull(info.getArtifactId());
        assertNull(info.getVersion());
        assertNull(info.getScope());
        assertNull(info.getType());
    }

    @Test
    void testSetters() {
        DependencyInfo info = new DependencyInfo();

        info.setName("test-name");
        info.setGroupId("test-group");
        info.setArtifactId("test-artifact");
        info.setVersion("1.0.0");
        info.setScope("runtime");
        info.setType(DependencyType.GRADLE);

        assertEquals("test-name", info.getName());
        assertEquals("test-group", info.getGroupId());
        assertEquals("test-artifact", info.getArtifactId());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("runtime", info.getScope());
        assertEquals(DependencyType.GRADLE, info.getType());
    }

    @Test
    void testGetNameDefaultNull() {
        DependencyInfo info = new DependencyInfo();
        assertNull(info.getName());
    }

    @Test
    void testGetGroupIdDefaultNull() {
        DependencyInfo info = new DependencyInfo();
        assertNull(info.getGroupId());
    }

    @Test
    void testGetArtifactIdDefaultNull() {
        DependencyInfo info = new DependencyInfo();
        assertNull(info.getArtifactId());
    }

    @Test
    void testGetVersionDefaultNull() {
        DependencyInfo info = new DependencyInfo();
        assertNull(info.getVersion());
    }

    @Test
    void testGetScopeDefaultNull() {
        DependencyInfo info = new DependencyInfo();
        assertNull(info.getScope());
    }

    @Test
    void testGetTypeDefaultNull() {
        DependencyInfo info = new DependencyInfo();
        assertNull(info.getType());
    }

    @Test
    void testSetNullValues() {
        DependencyInfo info = new DependencyInfo();
        info.setName(null);
        info.setGroupId(null);
        info.setArtifactId(null);
        info.setVersion(null);
        info.setScope(null);
        info.setType(null);

        assertNull(info.getName());
        assertNull(info.getGroupId());
        assertNull(info.getArtifactId());
        assertNull(info.getVersion());
        assertNull(info.getScope());
        assertNull(info.getType());
    }

    @Test
    void testSetEmptyValues() {
        DependencyInfo info = new DependencyInfo();
        info.setName("");
        info.setGroupId("");
        info.setArtifactId("");
        info.setVersion("");
        info.setScope("");

        assertEquals("", info.getName());
        assertEquals("", info.getGroupId());
        assertEquals("", info.getArtifactId());
        assertEquals("", info.getVersion());
        assertEquals("", info.getScope());
    }

    @Test
    void testAllFieldsNonNull() {
        DependencyInfo info = new DependencyInfo();
        info.setName("my-dependency");
        info.setGroupId("com.example");
        info.setArtifactId("my-lib");
        info.setVersion("2.0.0");
        info.setScope("provided");
        info.setType(DependencyType.EXTERNAL_LIBRARY);

        assertEquals("my-dependency", info.getName());
        assertEquals("com.example", info.getGroupId());
        assertEquals("my-lib", info.getArtifactId());
        assertEquals("2.0.0", info.getVersion());
        assertEquals("provided", info.getScope());
        assertEquals(DependencyType.EXTERNAL_LIBRARY, info.getType());
    }

    @Test
    void testDependencyTypeValues() {
        DependencyInfo info = new DependencyInfo();
        for (DependencyType type : DependencyType.values()) {
            info.setType(type);
            assertEquals(type, info.getType());
        }
    }

    @Test
    void testMultipleSetters() {
        DependencyInfo info = new DependencyInfo();
        info.setName("first");
        info.setGroupId("first-group");

        assertEquals("first", info.getName());
        assertEquals("first-group", info.getGroupId());

        // Update values
        info.setName("second");
        info.setGroupId("second-group");

        assertEquals("second", info.getName());
        assertEquals("second-group", info.getGroupId());
    }
}