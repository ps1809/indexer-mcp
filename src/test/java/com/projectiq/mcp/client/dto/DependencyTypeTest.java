package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DependencyType enum.
 */
class DependencyTypeTest {

    @Test
    void testValuesReturnsAllTypes() {
        DependencyType[] values = DependencyType.values();
        assertEquals(4, values.length);
        assertEquals(DependencyType.MAVEN, values[0]);
        assertEquals(DependencyType.GRADLE, values[1]);
        assertEquals(DependencyType.INTERNAL_MODULE, values[2]);
        assertEquals(DependencyType.EXTERNAL_LIBRARY, values[3]);
    }

    @Test
    void testValueOfMaven() {
        DependencyType type = DependencyType.valueOf("MAVEN");
        assertEquals(DependencyType.MAVEN, type);
    }

    @Test
    void testValueOfGradle() {
        DependencyType type = DependencyType.valueOf("GRADLE");
        assertEquals(DependencyType.GRADLE, type);
    }

    @Test
    void testValueOfInternalModule() {
        DependencyType type = DependencyType.valueOf("INTERNAL_MODULE");
        assertEquals(DependencyType.INTERNAL_MODULE, type);
    }

    @Test
    void testValueOfExternalLibrary() {
        DependencyType type = DependencyType.valueOf("EXTERNAL_LIBRARY");
        assertEquals(DependencyType.EXTERNAL_LIBRARY, type);
    }

    @Test
    void testValueOfInvalidThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DependencyType.valueOf("INVALID_TYPE");
        });
    }

    @Test
    void testValueOfNullThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            DependencyType.valueOf(null);
        });
    }

    @Test
    void testEnumNameReturnsUpperCasedName() {
        assertEquals("MAVEN", DependencyType.MAVEN.name());
        assertEquals("GRADLE", DependencyType.GRADLE.name());
        assertEquals("INTERNAL_MODULE", DependencyType.INTERNAL_MODULE.name());
        assertEquals("EXTERNAL_LIBRARY", DependencyType.EXTERNAL_LIBRARY.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, DependencyType.MAVEN.ordinal());
        assertEquals(1, DependencyType.GRADLE.ordinal());
        assertEquals(2, DependencyType.INTERNAL_MODULE.ordinal());
        assertEquals(3, DependencyType.EXTERNAL_LIBRARY.ordinal());
    }

    @Test
    void testToStringReturnsName() {
        assertEquals("MAVEN", DependencyType.MAVEN.toString());
        assertEquals("GRADLE", DependencyType.GRADLE.toString());
        assertEquals("INTERNAL_MODULE", DependencyType.INTERNAL_MODULE.toString());
        assertEquals("EXTERNAL_LIBRARY", DependencyType.EXTERNAL_LIBRARY.toString());
    }

    @Test
    void testEqualsSameInstance() {
        assertSame(DependencyType.MAVEN, DependencyType.MAVEN);
    }

    @Test
    void testEqualsWithSameValue() {
        DependencyType type = DependencyType.valueOf("MAVEN");
        assertEquals(DependencyType.MAVEN, type);
    }

    @Test
    void testNotEqual() {
        assertNotEquals(DependencyType.MAVEN, DependencyType.GRADLE);
        assertNotEquals(DependencyType.MAVEN, null);
        assertNotEquals(DependencyType.MAVEN, "MAVEN");
    }

    @Test
    void testHashCodeConsistent() {
        int hashCode1 = DependencyType.MAVEN.hashCode();
        int hashCode2 = DependencyType.MAVEN.hashCode();
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testEqualsContract() {
        DependencyType type1 = DependencyType.valueOf("MAVEN");
        DependencyType type2 = DependencyType.valueOf("MAVEN");
        assertTrue(type1.equals(type2));
        assertTrue(type1.hashCode() == type2.hashCode());
    }

    @Test
    void testValuesReturnsNewArray() {
        DependencyType[] values1 = DependencyType.values();
        DependencyType[] values2 = DependencyType.values();
        assertNotSame(values1, values2);
    }

    @Test
    void testValuesImmutable() {
        DependencyType[] values = DependencyType.values();
        // Modifying the returned array should not affect the enum
        values[0] = DependencyType.GRADLE;
        assertEquals(DependencyType.MAVEN, DependencyType.values()[0]);
    }
}