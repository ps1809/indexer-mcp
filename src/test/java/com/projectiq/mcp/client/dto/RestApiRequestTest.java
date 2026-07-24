package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestApiRequestTest {

    @Test
    void testDefaultConstructor() {
        RestApiRequest request = new RestApiRequest();
        assertNotNull(request);
        assertNull(request.getRepositoryName());
        assertNull(request.getBranch());
        assertNull(request.getPackageName());
        assertNull(request.getHttpMethods());
    }

    @Test
    void testParameterizedConstructor() {
        List<String> methods = List.of("GET", "POST");
        RestApiRequest request = new RestApiRequest("test-repo", methods);

        assertEquals("test-repo", request.getRepositoryName());
        assertEquals(methods, request.getHttpMethods());
        assertNull(request.getBranch());
        assertNull(request.getPackageName());
    }

    @Test
    void testSetters() {
        RestApiRequest request = new RestApiRequest();
        
        request.setRepositoryName("my-repo");
        request.setBranch("develop");
        request.setPackageName("com.example.api");
        request.setHttpMethods(List.of("GET", "POST", "PUT"));

        assertEquals("my-repo", request.getRepositoryName());
        assertEquals("develop", request.getBranch());
        assertEquals("com.example.api", request.getPackageName());
        assertEquals(3, request.getHttpMethods().size());
    }

    @Test
    void testAddHttpMethod_nullHttpMethods() {
        RestApiRequest request = new RestApiRequest();
        assertNull(request.getHttpMethods());

        request.addHttpMethod("GET");

        assertNotNull(request.getHttpMethods());
        assertEquals(1, request.getHttpMethods().size());
        assertTrue(request.getHttpMethods().contains("GET"));
    }

    @Test
    void testAddHttpMethod_existingList() {
        RestApiRequest request = new RestApiRequest();
        request.setHttpMethods(List.of("GET"));

        request.addHttpMethod("POST");
        request.addHttpMethod("DELETE");

        assertEquals(3, request.getHttpMethods().size());
        assertTrue(request.getHttpMethods().contains("GET"));
        assertTrue(request.getHttpMethods().contains("POST"));
        assertTrue(request.getHttpMethods().contains("DELETE"));
    }

    @Test
    void testToString() {
        RestApiRequest request = new RestApiRequest("test-repo", List.of("GET"));
        request.setBranch("main");
        request.setPackageName("com.example");

        String str = request.toString();
        assertTrue(str.contains("test-repo"));
        assertTrue(str.contains("main"));
        assertTrue(str.contains("com.example"));
        assertTrue(str.contains("GET"));
    }
}