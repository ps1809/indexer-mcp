package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RelatedFileRequest DTO.
 */
class RelatedFileRequestTest {

    @Test
    void testDefaultConstructor() {
        RelatedFileRequest request = new RelatedFileRequest();
        assertNull(request.getRepositoryName());
        assertNull(request.getSearchTarget());
        assertNull(request.getTargetType());
        assertNull(request.getBranch());
    }

    @Test
    void testSetters() {
        RelatedFileRequest request = new RelatedFileRequest();
        
        request.setRepositoryName("my-repo");
        assertEquals("my-repo", request.getRepositoryName());

        request.setSearchTarget("MyClass");
        assertEquals("MyClass", request.getSearchTarget());

        request.setTargetType(RelatedFileRequest.SearchTargetType.CLASS);
        assertEquals(RelatedFileRequest.SearchTargetType.CLASS, request.getTargetType());

        request.setBranch("develop");
        assertEquals("develop", request.getBranch());
    }

    @Test
    void testSearchTargetTypeEnumValues() {
        RelatedFileRequest.SearchTargetType[] values = RelatedFileRequest.SearchTargetType.values();
        assertEquals(5, values.length);
        
        assertEquals(RelatedFileRequest.SearchTargetType.CLASS, values[0]);
        assertEquals(RelatedFileRequest.SearchTargetType.METHOD, values[1]);
        assertEquals(RelatedFileRequest.SearchTargetType.REST_API, values[2]);
        assertEquals(RelatedFileRequest.SearchTargetType.SPRING_COMPONENT, values[3]);
        assertEquals(RelatedFileRequest.SearchTargetType.PACKAGE, values[4]);
    }

    @Test
    void testNullFieldsEquality() {
        RelatedFileRequest r1 = new RelatedFileRequest();
        RelatedFileRequest r2 = new RelatedFileRequest();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testEquals() {
        RelatedFileRequest r1 = new RelatedFileRequest();
        r1.setRepositoryName("repo");
        r1.setSearchTarget("target");
        r1.setTargetType(RelatedFileRequest.SearchTargetType.CLASS);
        r1.setBranch("main");

        RelatedFileRequest r2 = new RelatedFileRequest();
        r2.setRepositoryName("repo");
        r2.setSearchTarget("target");
        r2.setTargetType(RelatedFileRequest.SearchTargetType.CLASS);
        r2.setBranch("main");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testNotEquals() {
        RelatedFileRequest r1 = new RelatedFileRequest();
        r1.setRepositoryName("repo1");
        r1.setSearchTarget("target");
        r1.setTargetType(RelatedFileRequest.SearchTargetType.CLASS);
        r1.setBranch("main");

        RelatedFileRequest r2 = new RelatedFileRequest();
        r2.setRepositoryName("repo2");
        r2.setSearchTarget("target");
        r2.setTargetType(RelatedFileRequest.SearchTargetType.CLASS);
        r2.setBranch("main");

        assertNotEquals(r1, r2);
    }

    @Test
    void testEqualsNull() {
        RelatedFileRequest request = new RelatedFileRequest();
        assertNotEquals(request, null);
    }

    @Test
    void testEqualsDifferentClass() {
        RelatedFileRequest request = new RelatedFileRequest();
        assertNotEquals(request, "string");
    }

    @Test
    void testToString() {
        RelatedFileRequest request = new RelatedFileRequest();
        request.setRepositoryName("test-repo");
        request.setSearchTarget("TestClass");
        request.setTargetType(RelatedFileRequest.SearchTargetType.CLASS);
        request.setBranch("main");

        String str = request.toString();
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("TestClass"));
        assertTrue(str.contains("CLASS"));
        assertTrue(str.contains("main"));
    }

    @Test
    void testBranchNullDefaultsToMain() {
        RelatedFileRequest request = new RelatedFileRequest();
        assertNull(request.getBranch());
        // Branch is optional, null means default to main on server side
    }
}