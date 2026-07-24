package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClassInfo DTO.
 */
class ClassInfoTest {

    @Test
    void testGettersAndSetters() {
        ClassInfo classInfo = new ClassInfo();

        classInfo.setPackageName("java.util");
        assertEquals("java.util", classInfo.getPackageName());

        classInfo.setClassName("List");
        assertEquals("List", classInfo.getClassName());

        classInfo.setFullyQualifiedName("java.util.List");
        assertEquals("java.util.List", classInfo.getFullyQualifiedName());

        classInfo.setClassType(ClassType.INTERFACE);
        assertEquals(ClassType.INTERFACE, classInfo.getClassType());

        classInfo.setVisibility("public");
        assertEquals("public", classInfo.getVisibility());

        classInfo.setParentClass(null);
        assertNull(classInfo.getParentClass());

        classInfo.setSourceFileLocation("src/main/java/java/util/List.java");
        assertEquals("src/main/java/java/util/List.java", classInfo.getSourceFileLocation());
    }

    @Test
    void testImplementedInterfaces() {
        ClassInfo classInfo = new ClassInfo();
        List<String> interfaces = Arrays.asList("Serializable", "Comparable");
        classInfo.setImplementedInterfaces(interfaces);
        assertEquals(interfaces, classInfo.getImplementedInterfaces());
        assertTrue(classInfo.getImplementedInterfaces().contains("Serializable"));
    }

    @Test
    void testAnnotations() {
        ClassInfo classInfo = new ClassInfo();
        List<String> annotations = Arrays.asList("@Override", "@Deprecated");
        classInfo.setAnnotations(annotations);
        assertEquals(annotations, classInfo.getAnnotations());
        assertTrue(classInfo.getAnnotations().contains("@Override"));
    }

    @Test
    void testToString() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setPackageName("java.lang");
        classInfo.setClassName("String");
        classInfo.setFullyQualifiedName("java.lang.String");
        classInfo.setClassType(ClassType.CLASS);
        classInfo.setVisibility("public");
        classInfo.setParentClass(null);
        classInfo.setSourceFileLocation("src/java/lang/String.java");

        String toString = classInfo.toString();
        assertTrue(toString.contains("packageName='java.lang'"));
        assertTrue(toString.contains("className='String'"));
        assertTrue(toString.contains("fullyQualifiedName='java.lang.String'"));
        assertTrue(toString.contains("classType=CLASS"));
        assertTrue(toString.contains("visibility='public'"));
        assertTrue(toString.contains("sourceFileLocation='src/java/lang/String.java'"));
    }

    @Test
    void testNoArgsConstructor() {
        ClassInfo classInfo = new ClassInfo();
        assertNotNull(classInfo);
    }

    @Test
    void testClassTypeAllTypes() {
        ClassInfo classInfo = new ClassInfo();

        classInfo.setClassType(ClassType.CLASS);
        assertEquals(ClassType.CLASS, classInfo.getClassType());

        classInfo.setClassType(ClassType.INTERFACE);
        assertEquals(ClassType.INTERFACE, classInfo.getClassType());

        classInfo.setClassType(ClassType.ENUM);
        assertEquals(ClassType.ENUM, classInfo.getClassType());

        classInfo.setClassType(ClassType.RECORD);
        assertEquals(ClassType.RECORD, classInfo.getClassType());

        classInfo.setClassType(ClassType.ANNOTATION);
        assertEquals(ClassType.ANNOTATION, classInfo.getClassType());
    }

    @Test
    void testNullValues() {
        ClassInfo classInfo = new ClassInfo();
        assertNull(classInfo.getPackageName());
        assertNull(classInfo.getClassName());
        assertNull(classInfo.getFullyQualifiedName());
        assertNull(classInfo.getClassType());
        assertNull(classInfo.getVisibility());
        assertNull(classInfo.getParentClass());
        assertNull(classInfo.getSourceFileLocation());
    }

    @Test
    void testEmptyLists() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setImplementedInterfaces(Arrays.asList());
        assertNotNull(classInfo.getImplementedInterfaces());
        assertTrue(classInfo.getImplementedInterfaces().isEmpty());

        classInfo.setAnnotations(Arrays.asList());
        assertNotNull(classInfo.getAnnotations());
        assertTrue(classInfo.getAnnotations().isEmpty());
    }
}