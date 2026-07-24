package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PackageSummary}.
 */
class PackageSummaryTest {

    @Test
    void testDefaultConstructor() {
        PackageSummary pkg = new PackageSummary();
        assertNull(pkg.getPackageName());
        assertEquals(0L, pkg.getClassCount());
        assertEquals(0L, pkg.getMethodCount());
        assertNull(pkg.getClasses());
    }

    @Test
    void testSettersAndGetters() {
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.pkg");
        pkg.setClassCount(10);
        pkg.setMethodCount(50);

        assertEquals("com.example.pkg", pkg.getPackageName());
        assertEquals(10L, pkg.getClassCount());
        assertEquals(50L, pkg.getMethodCount());
    }

    @Test
    void testSetClasses() {
        PackageSummary pkg = new PackageSummary();
        
        ClassSummary cls1 = new ClassSummary();
        cls1.setClassName("ClassA");
        
        ClassSummary cls2 = new ClassSummary();
        cls2.setClassName("ClassB");
        
        List<ClassSummary> classes = Arrays.asList(cls1, cls2);
        pkg.setClasses(classes);

        assertEquals(2, pkg.getClasses().size());
        assertEquals("ClassA", pkg.getClasses().get(0).getClassName());
    }

    @Test
    void testToString() {
        PackageSummary pkg = new PackageSummary();
        pkg.setPackageName("com.example.pkg");
        pkg.setClassCount(10);
        pkg.setMethodCount(50);

        String str = pkg.toString();
        assertTrue(str.contains("com.example.pkg"));
        assertTrue(str.contains("10"));
        assertTrue(str.contains("50"));
    }

    @Test
    void testSetNullClasses() {
        PackageSummary pkg = new PackageSummary();
        pkg.setClasses(null);
        assertNull(pkg.getClasses());
    }
}