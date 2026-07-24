package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClassType enum.
 */
class ClassTypeTest {

    @Test
    void testValuesReturnsAllTypes() {
        ClassType[] values = ClassType.values();
        assertEquals(5, values.length);
        assertEquals(ClassType.CLASS, values[0]);
        assertEquals(ClassType.INTERFACE, values[1]);
        assertEquals(ClassType.ENUM, values[2]);
        assertEquals(ClassType.RECORD, values[3]);
        assertEquals(ClassType.ANNOTATION, values[4]);
    }

    @Test
    void testValueOfClass() {
        ClassType type = ClassType.valueOf("CLASS");
        assertEquals(ClassType.CLASS, type);
    }

    @Test
    void testValueOfInterface() {
        ClassType type = ClassType.valueOf("INTERFACE");
        assertEquals(ClassType.INTERFACE, type);
    }

    @Test
    void testValueOfEnum() {
        ClassType type = ClassType.valueOf("ENUM");
        assertEquals(ClassType.ENUM, type);
    }

    @Test
    void testValueOfRecord() {
        ClassType type = ClassType.valueOf("RECORD");
        assertEquals(ClassType.RECORD, type);
    }

    @Test
    void testValueOfAnnotation() {
        ClassType type = ClassType.valueOf("ANNOTATION");
        assertEquals(ClassType.ANNOTATION, type);
    }

    @Test
    void testValueOfInvalidThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ClassType.valueOf("INVALID_TYPE");
        });
    }
}