package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchResultTest {

    @Test
    void testDefaultConstructor() {
        SearchResult result = new SearchResult();
        
        assertNull(result.getName());
        assertNull(result.getType());
        assertNull(result.getPackageName());
        assertNull(result.getClassName());
        assertNull(result.getFilePath());
        assertNull(result.getLineNumber());
        assertNull(result.getDescription());
        assertNull(result.getSnippet());
    }

    @Test
    void testSetters() {
        SearchResult result = new SearchResult();
        
        result.setName("myMethod");
        result.setType("METHOD");
        result.setPackageName("com.example.pkg");
        result.setClassName("MyClass");
        result.setFilePath("src/main/java/com/example/MyClass.java");
        result.setLineNumber(42);
        result.setDescription("A sample method");
        result.setSnippet("public void myMethod() {}");

        assertEquals("myMethod", result.getName());
        assertEquals("METHOD", result.getType());
        assertEquals("com.example.pkg", result.getPackageName());
        assertEquals("MyClass", result.getClassName());
        assertEquals("src/main/java/com/example/MyClass.java", result.getFilePath());
        assertEquals(Integer.valueOf(42), result.getLineNumber());
        assertEquals("A sample method", result.getDescription());
        assertEquals("public void myMethod() {}", result.getSnippet());
    }

    @Test
    void testToString() {
        SearchResult result = new SearchResult();
        result.setName("testClass");
        result.setType("CLASS");
        result.setFilePath("src/Test.java");
        result.setLineNumber(10);

        String toString = result.toString();
        assertTrue(toString.contains("testClass"));
        assertTrue(toString.contains("CLASS"));
        assertTrue(toString.contains("Test.java"));
        assertTrue(toString.contains("10"));
    }
}