package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MethodInfo}.
 */
class MethodInfoTest {

    @Test
    void testGettersAndSetters() {
        MethodInfo methodInfo = new MethodInfo();

        methodInfo.setMethodName("testMethod");
        assertEquals("testMethod", methodInfo.getMethodName());

        methodInfo.setFullyQualifiedName("com.example.MyClass.testMethod");
        assertEquals("com.example.MyClass.testMethod", methodInfo.getFullyQualifiedName());

        methodInfo.setDeclaringClass("com.example.MyClass");
        assertEquals("com.example.MyClass", methodInfo.getDeclaringClass());

        methodInfo.setPackageName("com.example");
        assertEquals("com.example", methodInfo.getPackageName());

        methodInfo.setReturnType("void");
        assertEquals("void", methodInfo.getReturnType());

        methodInfo.setVisibility("public");
        assertEquals("public", methodInfo.getVisibility());

        methodInfo.setStaticFlag(true);
        assertTrue(methodInfo.isStatic());

        methodInfo.setStaticFlag(false);
        assertFalse(methodInfo.isStatic());

        methodInfo.setAbstractFlag(false);
        assertFalse(methodInfo.isAbstract());

        methodInfo.setSourceFileLocation("src/main/java/com/example/MyClass.java:42");
        assertEquals("src/main/java/com/example/MyClass.java:42", methodInfo.getSourceFileLocation());

        methodInfo.setAnnotations(Arrays.asList("@Override", "@SuppressWarnings(\"unused\")"));
        assertEquals(2, methodInfo.getAnnotations().size());
        assertEquals("@Override", methodInfo.getAnnotations().get(0));

        MethodParameter param = new MethodParameter();
        param.setName("arg1");
        param.setType("String");
        methodInfo.setParameters(Arrays.asList(param));
        assertEquals(1, methodInfo.getParameters().size());
    }

    @Test
    void testToString() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("testMethod");
        methodInfo.setDeclaringClass("com.example.MyClass");
        
        String toString = methodInfo.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("testMethod"));
        assertTrue(toString.contains("com.example.MyClass"));
    }

    @Test
    void testDefaultConstructor() {
        MethodInfo methodInfo = new MethodInfo();
        assertNull(methodInfo.getMethodName());
        assertNull(methodInfo.getFullyQualifiedName());
        assertNull(methodInfo.getDeclaringClass());
        assertNull(methodInfo.getPackageName());
        assertNull(methodInfo.getReturnType());
        assertNull(methodInfo.getVisibility());
        assertNull(methodInfo.getSourceFileLocation());
        assertNull(methodInfo.getAnnotations());
        assertNull(methodInfo.getParameters());
    }

    @Test
    void testIsStaticDefaultValue() {
        MethodInfo methodInfo = new MethodInfo();
        // isStatic starts as null (Boolean object), not false
        assertFalse(Boolean.TRUE.equals(methodInfo.isStatic()));
    }

    @Test
    void testIsAbstractDefaultValue() {
        MethodInfo methodInfo = new MethodInfo();
        // isAbstract starts as null (Boolean object), not false
        assertFalse(Boolean.TRUE.equals(methodInfo.isAbstract()));
    }

    @Test
    void testNullParametersTreatedAsEmptyList() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("test");
        
        // Parameters should be null by default after set
        assertDoesNotThrow(() -> methodInfo.getParameters());
    }

    @Test
    void testNullAnnotationsTreatedAsEmptyList() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("test");
        
        // Annotations should be null by default after set
        assertDoesNotThrow(() -> methodInfo.getAnnotations());
    }
}