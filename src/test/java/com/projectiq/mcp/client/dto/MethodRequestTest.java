package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MethodRequest}.
 */
class MethodRequestTest {

    @Test
    void testDefaultConstructor() {
        MethodRequest request = new MethodRequest();
        assertNull(request.getRepositoryName());
        assertNull(request.getMethodName());
        assertNull(request.getPackageName());
        assertNull(request.getMethodTypes());
        assertNull(request.getBranch());
    }

    @Test
    void testParameterizedConstructor() {
        MethodRequest request = new MethodRequest("my-repo", "findUser");

        assertEquals("my-repo", request.getRepositoryName());
        assertEquals("findUser", request.getMethodName());
        assertNull(request.getPackageName());
        assertNull(request.getMethodTypes());
        assertNull(request.getBranch());
    }

    @Test
    void testGettersAndSetters() {
        MethodRequest request = new MethodRequest();

        request.setRepositoryName("test-repo");
        assertEquals("test-repo", request.getRepositoryName());

        request.setMethodName("calculateTotal");
        assertEquals("calculateTotal", request.getMethodName());

        request.setPackageName("com.example.service");
        assertEquals("com.example.service", request.getPackageName());

        request.setMethodTypes(Arrays.asList("PUBLIC", "STATIC"));
        assertEquals(2, request.getMethodTypes().size());

        request.setBranch("main");
        assertEquals("main", request.getBranch());
    }

    @Test
    void testAddMethodTypeNullList() {
        MethodRequest request = new MethodRequest();
        assertNull(request.getMethodTypes());

        request.addMethodType("PUBLIC");

        assertNotNull(request.getMethodTypes());
        assertEquals(1, request.getMethodTypes().size());
        assertEquals("PUBLIC", request.getMethodTypes().get(0));
    }

    @Test
    void testAddMethodTypeMultiple() {
        MethodRequest request = new MethodRequest();

        request.addMethodType("PUBLIC");
        request.addMethodType("PROTECTED");
        request.addMethodType("PRIVATE");

        assertEquals(3, request.getMethodTypes().size());
    }

    @Test
    void testToString() {
        MethodRequest request = new MethodRequest("repo", "method");
        request.setPackageName("com.example");
        request.setBranch("develop");

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("repo"));
        assertTrue(toString.contains("method"));
        assertTrue(toString.contains("com.example"));
        assertTrue(toString.contains("develop"));
    }
}