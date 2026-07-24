package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ClassSummary}.
 */
class ClassSummaryTest {

    @Test
    void testDefaultConstructor() {
        ClassSummary cls = new ClassSummary();
        assertNull(cls.getClassName());
        assertNull(cls.getFullyQualifiedName());
        assertEquals(0L, cls.getMethodCount());
        assertEquals(0L, cls.getFieldCount());
        assertNull(cls.getSuperClass());
        assertNull(cls.getInterfaces());
    }

    @Test
    void testSettersAndGetters() {
        ClassSummary cls = new ClassSummary();
        cls.setClassName("MyClass");
        cls.setFullyQualifiedName("com.example.MyClass");
        cls.setMethodCount(10);
        cls.setFieldCount(5);
        cls.setSuperClass("java.lang.Object");
        cls.setInterfaces(new String[]{"Serializable", "Cloneable"});

        assertEquals("MyClass", cls.getClassName());
        assertEquals("com.example.MyClass", cls.getFullyQualifiedName());
        assertEquals(10L, cls.getMethodCount());
        assertEquals(5L, cls.getFieldCount());
        assertEquals("java.lang.Object", cls.getSuperClass());
        assertArrayEquals(new String[]{"Serializable", "Cloneable"}, cls.getInterfaces());
    }

    @Test
    void testToString() {
        ClassSummary cls = new ClassSummary();
        cls.setClassName("MyClass");
        cls.setFullyQualifiedName("com.example.MyClass");
        cls.setMethodCount(10);
        cls.setFieldCount(5);

        String str = cls.toString();
        assertTrue(str.contains("MyClass"));
        assertTrue(str.contains("com.example.MyClass"));
        assertTrue(str.contains("10"));
        assertTrue(str.contains("5"));
    }

    @Test
    void testSetNullFields() {
        ClassSummary cls = new ClassSummary();
        cls.setClassName("TestClass");
        cls.setSuperClass(null);
        cls.setInterfaces(null);
        cls.setFullyQualifiedName(null);

        assertEquals("TestClass", cls.getClassName());
        assertNull(cls.getSuperClass());
        assertNull(cls.getInterfaces());
        assertNull(cls.getFullyQualifiedName());
    }
}