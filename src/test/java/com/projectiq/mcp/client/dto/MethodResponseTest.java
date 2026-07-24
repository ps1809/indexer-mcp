package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MethodResponse}.
 */
class MethodResponseTest {

    @Test
    void testDefaultConstructor() {
        MethodResponse response = new MethodResponse();
        assertNull(response.getRepositoryName());
        assertNull(response.getTotalResults());
        assertNull(response.getMethods());
    }

    @Test
    void testGettersAndSetters() {
        MethodResponse response = new MethodResponse();

        response.setRepositoryName("my-repo");
        assertEquals("my-repo", response.getRepositoryName());

        response.setTotalResults(42);
        assertEquals(Integer.valueOf(42), response.getTotalResults());

        List<MethodInfo> methods = Arrays.asList(new MethodInfo(), new MethodInfo());
        response.setMethods(methods);
        assertEquals(2, response.getMethods().size());
    }

    @Test
    void testToString() {
        MethodResponse response = new MethodResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(10);

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test-repo"));
        assertTrue(toString.contains("10"));
    }

    @Test
    void testEmptyMethodsList() {
        MethodResponse response = new MethodResponse();
        response.setRepositoryName("repo");
        response.setTotalResults(0);
        response.setMethods(Collections.emptyList());

        assertEquals("repo", response.getRepositoryName());
        assertEquals(Integer.valueOf(0), response.getTotalResults());
        assertTrue(response.getMethods().isEmpty());
    }
}