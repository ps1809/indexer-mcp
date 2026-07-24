package com.projectiq.mcp.client.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClassResponse DTO.
 */
class ClassResponseTest {

    @Test
    void testGettersAndSetters() {
        ClassResponse response = new ClassResponse();

        response.setRepositoryName("test-repo");
        assertEquals("test-repo", response.getRepositoryName());

        response.setTotalResults(42);
        assertEquals(42, response.getTotalResults());

        List<ClassInfo> classes = createTestClasses();
        response.setClasses(classes);
        assertEquals(classes, response.getClasses());
    }

    @Test
    void testNoArgsConstructor() {
        ClassResponse response = new ClassResponse();
        assertNotNull(response);
    }

    @Test
    void testNullValues() {
        ClassResponse response = new ClassResponse();
        assertNull(response.getRepositoryName());
        assertNull(response.getTotalResults());
        assertNull(response.getClasses());
    }

    @Test
    void testEmptyClassesList() {
        ClassResponse response = new ClassResponse();
        List<ClassInfo> emptyClasses = Arrays.asList();
        response.setClasses(emptyClasses);
        assertNotNull(response.getClasses());
        assertTrue(response.getClasses().isEmpty());
    }

    @Test
    void testSingleClassResult() {
        ClassResponse response = new ClassResponse();
        response.setRepositoryName("spring-framework");
        response.setTotalResults(1);

        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("ApplicationContext");
        classInfo.setPackageName("org.springframework.context");
        classInfo.setClassType(ClassType.INTERFACE);

        response.setClasses(Arrays.asList(classInfo));

        assertEquals(1, response.getTotalResults());
        assertEquals(1, response.getClasses().size());
        assertEquals("ApplicationContext", response.getClasses().get(0).getClassName());
    }

    @Test
    void testMultipleClassResults() {
        ClassResponse response = new ClassResponse();
        response.setRepositoryName("test-repo");

        List<ClassInfo> classes = Arrays.asList(
                createClassInfo("ClassA", "com.example.a", ClassType.CLASS),
                createClassInfo("ClassB", "com.example.b", ClassType.INTERFACE),
                createClassInfo("EnumC", "com.example.c", ClassType.ENUM),
                createClassInfo("RecordD", "com.example.d", ClassType.RECORD),
                createClassInfo("AnnoE", "com.example.e", ClassType.ANNOTATION)
        );

        response.setClasses(classes);
        response.setTotalResults(5);

        assertEquals(5, response.getTotalResults());
        assertEquals(5, response.getClasses().size());
    }

    @Test
    void testZeroTotalResults() {
        ClassResponse response = new ClassResponse();
        response.setTotalResults(0);
        response.setClasses(Arrays.asList());

        assertEquals(0, response.getTotalResults());
    }

    @Test
    void testToString() {
        ClassResponse response = new ClassResponse();
        response.setRepositoryName("my-repo");
        response.setTotalResults(10);
        response.setClasses(Arrays.asList(createClassInfo("Test", "com.test", ClassType.CLASS)));

        String toString = response.toString();
        assertTrue(toString.contains("repositoryName='my-repo'"));
        assertTrue(toString.contains("totalResults=10"));
        assertTrue(toString.contains("classes="));
    }

    @Test
    void testClassInfoContents() {
        ClassResponse response = new ClassResponse();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setPackageName("org.junit");
        classInfo.setClassName("Assert");
        classInfo.setFullyQualifiedName("org.junit.Assert");
        classInfo.setClassType(ClassType.CLASS);
        classInfo.setVisibility("public");
        classInfo.setParentClass("Object");
        classInfo.setImplementedInterfaces(Arrays.asList("Serializable"));
        classInfo.setAnnotations(Arrays.asList("@Deprecated"));
        classInfo.setSourceFileLocation("src/main/java/org/junit/Assert.java");

        response.setClasses(Arrays.asList(classInfo));

        ClassInfo retrieved = response.getClasses().get(0);
        assertEquals("org.junit", retrieved.getPackageName());
        assertEquals("Assert", retrieved.getClassName());
        assertEquals("org.junit.Assert", retrieved.getFullyQualifiedName());
        assertEquals(ClassType.CLASS, retrieved.getClassType());
        assertEquals("public", retrieved.getVisibility());
        assertEquals("Object", retrieved.getParentClass());
        assertTrue(retrieved.getImplementedInterfaces().contains("Serializable"));
        assertTrue(retrieved.getAnnotations().contains("@Deprecated"));
        assertEquals("src/main/java/org/junit/Assert.java", retrieved.getSourceFileLocation());
    }

    @Test
    void testRepositoryNameValidation() {
        ClassResponse response = new ClassResponse();
        response.setRepositoryName("projectiq-indexer");
        assertNotNull(response.getRepositoryName());
        assertFalse(response.getRepositoryName().isEmpty());
    }

    @Test
    void testTotalResultsBoundaryValues() {
        ClassResponse response = new ClassResponse();

        response.setTotalResults(0);
        assertEquals(0, response.getTotalResults());

        response.setTotalResults(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, response.getTotalResults());

        response.setTotalResults(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, response.getTotalResults());
    }

    private ClassInfo createClassInfo(String className, String packageName, ClassType classType) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName(className);
        classInfo.setPackageName(packageName);
        classInfo.setClassType(classType);
        return classInfo;
    }

    private List<ClassInfo> createTestClasses() {
        return Arrays.asList(createClassInfo("Test", "com.test", ClassType.CLASS));
    }
}