package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MethodParameter}.
 */
class MethodParameterTest {

    @Test
    void testGettersAndSetters() {
        MethodParameter param = new MethodParameter();

        param.setName("arg1");
        assertEquals("arg1", param.getName());

        param.setType("String");
        assertEquals("String", param.getType());
    }

    @Test
    void testDefaultConstructor() {
        MethodParameter param = new MethodParameter();
        assertNull(param.getName());
        assertNull(param.getType());
    }

    @Test
    void testToString() {
        MethodParameter param = new MethodParameter();
        param.setName("arg1");
        param.setType("String");

        String toString = param.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("arg1"));
        assertTrue(toString.contains("String"));
    }
}