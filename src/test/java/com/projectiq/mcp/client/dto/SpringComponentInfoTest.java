package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpringComponentInfoTest {

    @Test
    void getters_shouldReturnSetValues() {
        SpringComponentInfo info = new SpringComponentInfo();
        info.setName("UserController");
        info.setComponentType("Controller");
        info.setClassName("UserController");
        info.setPackageName("com.example.controller");
        info.setFilePath("src/main/java/com/example/controller/UserController.java");
        info.setLineNumber(25);
        info.setDescription("REST controller for user operations");

        assertEquals("UserController", info.getName());
        assertEquals("Controller", info.getComponentType());
        assertEquals("UserController", info.getClassName());
        assertEquals("com.example.controller", info.getPackageName());
        assertEquals("src/main/java/com/example/controller/UserController.java", info.getFilePath());
        assertEquals(Integer.valueOf(25), info.getLineNumber());
        assertEquals("REST controller for user operations", info.getDescription());
    }

    @Test
    void defaultConstructor_shouldCreateEmptyObject() {
        SpringComponentInfo info = new SpringComponentInfo();
        assertNull(info.getName());
        assertNull(info.getComponentType());
        assertNull(info.getClassName());
        assertNull(info.getPackageName());
        assertNull(info.getFilePath());
        assertNull(info.getLineNumber());
        assertNull(info.getDescription());
    }

    @Test
    void toString_shouldIncludeName() {
        SpringComponentInfo info = new SpringComponentInfo();
        info.setName("TestComponent");

        String str = info.toString();
        assertNotNull(str);
        assertTrue(str.contains("TestComponent"));
    }
}