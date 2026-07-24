package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestEndpointInfoTest {

    @Test
    void testDefaultConstructor() {
        RestEndpointInfo endpoint = new RestEndpointInfo();
        assertNotNull(endpoint);
    }

    @Test
    void testAllSettersAndGetters() {
        RestEndpointInfo endpoint = new RestEndpointInfo();
        
        endpoint.setEndpointPath("/api/v1/users");
        endpoint.setHttpMethod("GET");
        endpoint.setControllerName("UserController");
        endpoint.setMethodName("findAllUsers");
        endpoint.setRequestMapping("@GetMapping(\"/users\")");
        endpoint.setResponse_type("List<UserDto>");
        endpoint.setPackageName("com.example.controller");
        endpoint.setFilePath("src/main/java/com/example/controller/UserController.java");
        endpoint.setLineNumber(42);

        assertEquals("/api/v1/users", endpoint.getEndpointPath());
        assertEquals("GET", endpoint.getHttpMethod());
        assertEquals("UserController", endpoint.getControllerName());
        assertEquals("findAllUsers", endpoint.getMethodName());
        assertEquals("@GetMapping(\"/users\")", endpoint.getRequestMapping());
        assertEquals("List<UserDto>", endpoint.getResponse_type());
        assertEquals("com.example.controller", endpoint.getPackageName());
        assertEquals("src/main/java/com/example/controller/UserController.java", endpoint.getFilePath());
        assertEquals(Integer.valueOf(42), endpoint.getLineNumber());
    }

    @Test
    void testToString() {
        RestEndpointInfo endpoint = new RestEndpointInfo();
        endpoint.setEndpointPath("/api/v1/users");
        endpoint.setHttpMethod("GET");
        endpoint.setControllerName("UserController");

        String str = endpoint.toString();
        assertTrue(str.contains("/api/v1/users"));
        assertTrue(str.contains("GET"));
        assertTrue(str.contains("UserController"));
    }

    @Test
    void testNullValues() {
        RestEndpointInfo endpoint = new RestEndpointInfo();
        
        assertNull(endpoint.getEndpointPath());
        assertNull(endpoint.getHttpMethod());
        assertNull(endpoint.getControllerName());
        assertNull(endpoint.getMethodName());
        assertNull(endpoint.getRequestMapping());
        assertNull(endpoint.getResponse_type());
        assertNull(endpoint.getPackageName());
        assertNull(endpoint.getFilePath());
        assertNull(endpoint.getLineNumber());
    }
}