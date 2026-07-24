package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpringComponentRequestTest {

    @Test
    void constructor_shouldInitializeAllFields() {
        List<String> types = List.of("Controller", "Service");
        SpringComponentRequest request = new SpringComponentRequest("test-repo", types);

        assertEquals("test-repo", request.getRepositoryName());
        assertEquals(types, request.getComponentTypes());
        assertNull(request.getBranch());
        assertNull(request.getPackageName());
    }

    @Test
    void setters_shouldUpdateFields() {
        SpringComponentRequest request = new SpringComponentRequest("test-repo", List.of("Controller"));
        request.setBranch("develop");
        request.setPackageName("com.example");

        assertEquals("develop", request.getBranch());
        assertEquals("com.example", request.getPackageName());
    }

    @Test
    void toString_shouldIncludeRepositoryName() {
        SpringComponentRequest request = new SpringComponentRequest("test-repo", List.of("Service"));
        String str = request.toString();

        assertNotNull(str);
        assertTrue(str.contains("test-repo"));
    }
}