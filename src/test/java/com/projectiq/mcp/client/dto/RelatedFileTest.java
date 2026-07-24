package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RelatedFile DTO.
 */
class RelatedFileTest {

    @Test
    void testDefaultConstructor() {
        RelatedFile file = new RelatedFile();
        assertNull(file.getFileName());
        assertNull(file.getFilePath());
        assertNull(file.getFileType());
        assertNull(file.getRelationshipType());
        assertNull(file.getAssociatedPackage());
    }

    @Test
    void testParameterizedConstructor() {
        RelatedFile file = new RelatedFile(
            "Test.java",
            "/src/main/java/com/example/Test.java",
            "JAVA",
            "IMPLEMENTATION",
            "com.example"
        );

        assertEquals("Test.java", file.getFileName());
        assertEquals("/src/main/java/com/example/Test.java", file.getFilePath());
        assertEquals("JAVA", file.getFileType());
        assertEquals("IMPLEMENTATION", file.getRelationshipType());
        assertEquals("com.example", file.getAssociatedPackage());
    }

    @Test
    void testSetters() {
        RelatedFile file = new RelatedFile();
        
        file.setFileName("Other.java");
        assertEquals("Other.java", file.getFileName());

        file.setFilePath("/path/Other.java");
        assertEquals("/path/Other.java", file.getFilePath());

        file.setFileType("JAVA");
        assertEquals("JAVA", file.getFileType());

        file.setRelationshipType("DEPENDENCY");
        assertEquals("DEPENDENCY", file.getRelationshipType());

        file.setAssociatedPackage("com.other");
        assertEquals("com.other", file.getAssociatedPackage());
    }

    @Test
    void testToString() {
        RelatedFile file = new RelatedFile(
            "Test.java",
            "/path/Test.java",
            "JAVA",
            "IMPLEMENTATION",
            "com.example"
        );

        String str = file.toString();
        assertTrue(str.contains("RelatedFile{"));
        assertTrue(str.contains("Test.java"));
        assertTrue(str.contains("/path/Test.java"));
        assertTrue(str.contains("JAVA"));
        assertTrue(str.contains("IMPLEMENTATION"));
        assertTrue(str.contains("com.example"));
    }

    @Test
    void testEqualsAndHashCode() {
        RelatedFile f1 = new RelatedFile(
            "Test.java",
            "/path/Test.java",
            "JAVA",
            "IMPLEMENTATION",
            "com.example"
        );

        RelatedFile f2 = new RelatedFile(
            "Test.java",
            "/path/Test.java",
            "JAVA",
            "IMPLEMENTATION",
            "com.example"
        );

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    void testNotEquals() {
        RelatedFile f1 = new RelatedFile(
            "Test.java",
            "/path/Test.java",
            "JAVA",
            "IMPLEMENTATION",
            "com.example"
        );

        RelatedFile f2 = new RelatedFile(
            "Other.java",
            "/path/Test.java",
            "JAVA",
            "IMPLEMENTATION",
            "com.example"
        );

        assertNotEquals(f1, f2);
    }

    @Test
    void testEqualsNull() {
        RelatedFile file = new RelatedFile();
        assertNotEquals(file, null);
    }

    @Test
    void testEqualsDifferentClass() {
        RelatedFile file = new RelatedFile();
        assertNotEquals(file, "string");
    }

    @Test
    void testEqualsSelf() {
        RelatedFile file = new RelatedFile("Test.java", null, null, null, null);
        assertEquals(file, file);
    }

    @Test
    void testPartialEquality() {
        RelatedFile f1 = new RelatedFile("Test.java", "/path/Test.java", "JAVA", null, null);
        RelatedFile f2 = new RelatedFile("Test.java", "/path/Test.java", "JAVA", null, null);
        assertEquals(f1, f2);
    }

    @Test
    void testAllFieldsNull() {
        RelatedFile file = new RelatedFile();
        file.setFileName(null);
        file.setFilePath(null);
        file.setFileType(null);
        file.setRelationshipType(null);
        file.setAssociatedPackage(null);

        RelatedFile other = new RelatedFile();
        assertEquals(file, other);
        assertEquals(file.hashCode(), other.hashCode());
    }
}