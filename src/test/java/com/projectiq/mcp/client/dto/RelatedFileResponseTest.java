package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RelatedFileResponse DTO.
 */
class RelatedFileResponseTest {

    @Test
    void testDefaultConstructor() {
        RelatedFileResponse response = new RelatedFileResponse();
        assertNull(response.getRepositoryName());
        assertNull(response.getSearchTarget());
        assertNull(response.getTargetType());
        assertNull(response.getTotalResults());
        assertNull(response.getRelatedFiles());
    }

    @Test
    void testParameterizedConstructor() {
        List<RelatedFile> relatedFiles = Arrays.asList(
            new RelatedFile("Test.java", "/path/Test.java", "JAVA", "IMPLEMENTATION", "com.example"),
            new RelatedFile("Test.xml", "/path/Test.xml", "XML", "DEPENDENCY", "com.example")
        );

        RelatedFileResponse response = new RelatedFileResponse(
            "test-repo",
            "TestClass",
            RelatedFileResponse.SearchTargetType.CLASS,
            2,
            relatedFiles
        );

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals("TestClass", response.getSearchTarget());
        assertEquals(RelatedFileResponse.SearchTargetType.CLASS, response.getTargetType());
        assertEquals(2, response.getTotalResults());
        assertEquals(2, response.getRelatedFiles().size());
    }

    @Test
    void testSetters() {
        RelatedFileResponse response = new RelatedFileResponse();
        
        response.setRepositoryName("my-repo");
        assertEquals("my-repo", response.getRepositoryName());

        response.setSearchTarget("MyClass");
        assertEquals("MyClass", response.getSearchTarget());

        response.setTargetType(RelatedFileResponse.SearchTargetType.METHOD);
        assertEquals(RelatedFileResponse.SearchTargetType.METHOD, response.getTargetType());

        response.setTotalResults(5);
        assertEquals(5, response.getTotalResults());

        List<RelatedFile> files = Arrays.asList(new RelatedFile());
        response.setRelatedFiles(files);
        assertEquals(files, response.getRelatedFiles());
    }

    @Test
    void testSearchTargetTypeEnumValues() {
        RelatedFileResponse.SearchTargetType[] values = RelatedFileResponse.SearchTargetType.values();
        assertEquals(5, values.length);
        
        assertEquals(RelatedFileResponse.SearchTargetType.CLASS, values[0]);
        assertEquals(RelatedFileResponse.SearchTargetType.METHOD, values[1]);
        assertEquals(RelatedFileResponse.SearchTargetType.REST_API, values[2]);
        assertEquals(RelatedFileResponse.SearchTargetType.SPRING_COMPONENT, values[3]);
        assertEquals(RelatedFileResponse.SearchTargetType.PACKAGE, values[4]);
    }

    @Test
    void testToString() {
        RelatedFileResponse response = new RelatedFileResponse(
            "test-repo",
            "TestClass",
            RelatedFileResponse.SearchTargetType.CLASS,
            3,
            null
        );

        String str = response.toString();
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("TestClass"));
        assertTrue(str.contains("CLASS"));
        assertTrue(str.contains("3"));
    }

    @Test
    void testEqualsAndHashCode() {
        RelatedFileResponse r1 = new RelatedFileResponse(
            "repo",
            "target",
            RelatedFileResponse.SearchTargetType.CLASS,
            1,
            null
        );

        RelatedFileResponse r2 = new RelatedFileResponse(
            "repo",
            "target",
            RelatedFileResponse.SearchTargetType.CLASS,
            1,
            null
        );

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEquals() {
        RelatedFileResponse r1 = new RelatedFileResponse(
            "repo",
            "target",
            RelatedFileResponse.SearchTargetType.CLASS,
            1,
            null
        );

        RelatedFileResponse r2 = new RelatedFileResponse(
            "other-repo",
            "target",
            RelatedFileResponse.SearchTargetType.CLASS,
            1,
            null
        );

        assertNotEquals(r1, r2);
    }

    @Test
    void testEqualsNull() {
        RelatedFileResponse response = new RelatedFileResponse();
        assertNotEquals(response, null);
    }

    @Test
    void testEqualsDifferentClass() {
        RelatedFileResponse response = new RelatedFileResponse();
        assertNotEquals(response, "string");
    }

    @Test
    void testEmptyRelatedFiles() {
        List<RelatedFile> emptyList = Arrays.asList();
        RelatedFileResponse response = new RelatedFileResponse(
            "repo",
            "target",
            RelatedFileResponse.SearchTargetType.CLASS,
            0,
            emptyList
        );

        assertEquals(0, response.getTotalResults());
        assertNotNull(response.getRelatedFiles());
        assertTrue(response.getRelatedFiles().isEmpty());
    }
}