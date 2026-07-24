package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpringComponentResponseTest {

    @Test
    void getters_shouldReturnSetValues() {
        SpringComponentResponse response = new SpringComponentResponse();
        response.setRepositoryName("test-repo");
        response.setTotalResults(5);

        List<SpringComponentInfo> components = List.of(
            createMockComponent("Controller"),
            createMockComponent("Service")
        );
        response.setComponents(components);

        assertEquals("test-repo", response.getRepositoryName());
        assertEquals(Integer.valueOf(5), response.getTotalResults());
        assertEquals(2, response.getComponents().size());
    }

    @Test
    void defaultConstructor_shouldCreateEmptyObject() {
        SpringComponentResponse response = new SpringComponentResponse();
        assertNull(response.getRepositoryName());
        assertNull(response.getTotalResults());
        assertNull(response.getComponents());
    }

    @Test
    void toString_shouldIncludeRepositoryName() {
        SpringComponentResponse response = new SpringComponentResponse();
        response.setRepositoryName("test-repo");

        String str = response.toString();
        assertNotNull(str);
        assertTrue(str.contains("test-repo"));
    }

    private SpringComponentInfo createMockComponent(String type) {
        SpringComponentInfo info = new SpringComponentInfo();
        info.setName("Test" + type);
        info.setComponentType(type);
        return info;
    }
}