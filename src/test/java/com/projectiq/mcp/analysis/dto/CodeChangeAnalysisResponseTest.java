package com.projectiq.mcp.analysis.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CodeChangeAnalysisResponse}.
 * Verifies construction, getters, setters, and add methods.
 */
class CodeChangeAnalysisResponseTest {

    @Test
    void testDefaultConstructor() {
        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();

        assertNull(response.getProposedChangeSummary());
        assertNotNull(response.getImpactedFiles());
        assertTrue(response.getImpactedFiles().isEmpty());
        assertNotNull(response.getImpactedClasses());
        assertTrue(response.getImpactedClasses().isEmpty());
        assertNotNull(response.getImpactedMethods());
        assertTrue(response.getImpactedMethods().isEmpty());
        assertNotNull(response.getImpactedRestApis());
        assertTrue(response.getImpactedRestApis().isEmpty());
        assertNotNull(response.getDependencyChanges());
        assertTrue(response.getDependencyChanges().isEmpty());
        assertNotNull(response.getTestingRecommendations());
        assertTrue(response.getTestingRecommendations().isEmpty());
        assertNotNull(response.getRiskAssessment());
        assertTrue(response.getRiskAssessment().isEmpty());
        assertNotNull(response.getSuggestedImplementationOrder());
        assertTrue(response.getSuggestedImplementationOrder().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();

        response.setProposedChangeSummary("Add pagination to UserController. Change Type: New Feature. This analysis predicts the repository-wide impact.");
        assertEquals("Add pagination to UserController. Change Type: New Feature. This analysis predicts the repository-wide impact.",
                response.getProposedChangeSummary());

        List<String> files = Arrays.asList("UserController.java", "UserService.java", "UserRepository.java");
        response.setImpactedFiles(files);
        assertEquals(files, response.getImpactedFiles());

        List<String> classes = Arrays.asList("UserController (Controller)", "UserService (Service)");
        response.setImpactedClasses(classes);
        assertEquals(classes, response.getImpactedClasses());

        List<String> methods = Arrays.asList("findAll()", "findById()");
        response.setImpactedMethods(methods);
        assertEquals(methods, response.getImpactedMethods());

        List<String> apis = Arrays.asList("GET /api/users", "GET /api/users/{id}");
        response.setImpactedRestApis(apis);
        assertEquals(apis, response.getImpactedRestApis());

        List<String> deps = Arrays.asList("New dependencies may be required");
        response.setDependencyChanges(deps);
        assertEquals(deps, response.getDependencyChanges());

        List<String> recommendations = Arrays.asList("Write unit tests for all new classes");
        response.setTestingRecommendations(recommendations);
        assertEquals(recommendations, response.getTestingRecommendations());

        List<String> risks = Arrays.asList("New feature may introduce integration issues");
        response.setRiskAssessment(risks);
        assertEquals(risks, response.getRiskAssessment());

        List<String> order = Arrays.asList("1. Define data models", "2. Implement repository layer");
        response.setSuggestedImplementationOrder(order);
        assertEquals(order, response.getSuggestedImplementationOrder());
    }

    @Test
    void testAddMethods() {
        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();

        response.addImpactedFile("UserController.java");
        response.addImpactedFile("UserService.java");
        assertEquals(2, response.getImpactedFiles().size());
        assertEquals("UserController.java", response.getImpactedFiles().get(0));
        assertEquals("UserService.java", response.getImpactedFiles().get(1));

        response.addImpactedClass("UserController (Controller)");
        assertEquals(1, response.getImpactedClasses().size());
        assertEquals("UserController (Controller)", response.getImpactedClasses().get(0));

        response.addImpactedMethod("findAll()");
        assertEquals(1, response.getImpactedMethods().size());
        assertEquals("findAll()", response.getImpactedMethods().get(0));

        response.addImpactedRestApi("GET /api/users");
        assertEquals(1, response.getImpactedRestApis().size());
        assertEquals("GET /api/users", response.getImpactedRestApis().get(0));

        response.addDependencyChange("New dependency required");
        assertEquals(1, response.getDependencyChanges().size());
        assertEquals("New dependency required", response.getDependencyChanges().get(0));

        response.addTestingRecommendation("Write unit tests");
        assertEquals(1, response.getTestingRecommendations().size());
        assertEquals("Write unit tests", response.getTestingRecommendations().get(0));

        response.addRisk("Integration risk");
        assertEquals(1, response.getRiskAssessment().size());
        assertEquals("Integration risk", response.getRiskAssessment().get(0));

        response.addImplementationStep("1. Implement changes");
        assertEquals(1, response.getSuggestedImplementationOrder().size());
        assertEquals("1. Implement changes", response.getSuggestedImplementationOrder().get(0));
    }

    @Test
    void testAddMethodsWithNullInitialLists() {
        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();
        response.setImpactedFiles(null);
        response.setImpactedClasses(null);
        response.setImpactedMethods(null);
        response.setImpactedRestApis(null);
        response.setDependencyChanges(null);
        response.setTestingRecommendations(null);
        response.setRiskAssessment(null);
        response.setSuggestedImplementationOrder(null);

        response.addImpactedFile("file.java");
        response.addImpactedClass("Class (Type)");
        response.addImpactedMethod("method()");
        response.addImpactedRestApi("GET /api");
        response.addDependencyChange("dep");
        response.addTestingRecommendation("test");
        response.addRisk("risk");
        response.addImplementationStep("step");

        assertEquals(1, response.getImpactedFiles().size());
        assertEquals(1, response.getImpactedClasses().size());
        assertEquals(1, response.getImpactedMethods().size());
        assertEquals(1, response.getImpactedRestApis().size());
        assertEquals(1, response.getDependencyChanges().size());
        assertEquals(1, response.getTestingRecommendations().size());
        assertEquals(1, response.getRiskAssessment().size());
        assertEquals(1, response.getSuggestedImplementationOrder().size());
    }

    @Test
    void testListImmutability() {
        // Verify that the lists are separate instances from the ones passed in
        CodeChangeAnalysisResponse response = new CodeChangeAnalysisResponse();
        List<String> original = new java.util.ArrayList<>(Arrays.asList("Item1", "Item2"));
        response.setImpactedFiles(new java.util.ArrayList<>(original));

        original.add("Item3"); // Modify original - should not affect response
        assertEquals(2, response.getImpactedFiles().size());
    }
}