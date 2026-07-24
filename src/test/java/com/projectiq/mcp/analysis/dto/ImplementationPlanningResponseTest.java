package com.projectiq.mcp.analysis.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImplementationPlanningResponseTest {

    @Test
    void defaultConstructor_initializesEmptyLists() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        assertNotNull(response.getRecommendedImplementationOrder());
        assertNotNull(response.getFilesToModify());
        assertNotNull(response.getFilesToReview());
        assertNotNull(response.getComponentsAffected());
        assertNotNull(response.getDependenciesInvolved());
        assertNotNull(response.getSuggestedValidationSteps());
        assertNotNull(response.getRisks());
        assertNotNull(response.getAssumptions());
        assertTrue(response.getRecommendedImplementationOrder().isEmpty());
        assertTrue(response.getFilesToModify().isEmpty());
        assertTrue(response.getFilesToReview().isEmpty());
        assertTrue(response.getComponentsAffected().isEmpty());
        assertTrue(response.getDependenciesInvolved().isEmpty());
        assertTrue(response.getSuggestedValidationSteps().isEmpty());
        assertTrue(response.getRisks().isEmpty());
        assertTrue(response.getAssumptions().isEmpty());
    }

    @Test
    void setAndGetOriginalTask() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setOriginalTask("Add pagination to UserController");
        assertEquals("Add pagination to UserController", response.getOriginalTask());
    }

    @Test
    void setAndGetTaskType() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setTaskType("New Feature");
        assertEquals("New Feature", response.getTaskType());
    }

    @Test
    void setAndGetEstimatedComplexity() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setEstimatedComplexity("MEDIUM");
        assertEquals("MEDIUM", response.getEstimatedComplexity());
    }

    @Test
    void addRecommendedStep() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.addRecommendedStep("1. Analyze the repository structure");
        response.addRecommendedStep("2. Search the codebase");
        assertEquals(2, response.getRecommendedImplementationOrder().size());
        assertTrue(response.getRecommendedImplementationOrder().get(0).contains("Analyze"));
        assertTrue(response.getRecommendedImplementationOrder().get(1).contains("Search"));
    }

    @Test
    void addFileToModify() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.addFileToModify("UserController.java");
        response.addFileToModify("UserService.java");
        assertEquals(2, response.getFilesToModify().size());
        assertTrue(response.getFilesToModify().contains("UserController.java"));
        assertTrue(response.getFilesToModify().contains("UserService.java"));
    }

    @Test
    void addFileToReview() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.addFileToReview("UserRepository.java");
        assertEquals(1, response.getFilesToReview().size());
        assertTrue(response.getFilesToReview().contains("UserRepository.java"));
    }

    @Test
    void addComponentAffected() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.addComponentAffected("UserController (Class)");
        response.addComponentAffected("UserService (Class)");
        assertEquals(2, response.getComponentsAffected().size());
        assertTrue(response.getComponentsAffected().contains("UserController (Class)"));
    }

    @Test
    void addDependencyInvolved() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.addDependencyInvolved("Spring Data JPA");
        assertEquals(1, response.getDependenciesInvolved().size());
        assertTrue(response.getDependenciesInvolved().contains("Spring Data JPA"));
    }

    @Test
    void addValidationStep() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.addValidationStep("Verify the changes compile without errors");
        assertEquals(1, response.getSuggestedValidationSteps().size());
        assertTrue(response.getSuggestedValidationSteps().contains("Verify the changes compile without errors"));
    }

    @Test
    void setAndGetSuggestedTestingScope() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.setSuggestedTestingScope("Medium scope: Unit tests and integration tests");
        assertEquals("Medium scope: Unit tests and integration tests", response.getSuggestedTestingScope());
    }

    @Test
    void addRisk() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.addRisk("Data migration errors could cause data loss");
        assertEquals(1, response.getRisks().size());
        assertTrue(response.getRisks().contains("Data migration errors could cause data loss"));
    }

    @Test
    void addAssumption() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        response.addAssumption("Bug reproduction steps are available");
        assertEquals(1, response.getAssumptions().size());
        assertTrue(response.getAssumptions().contains("Bug reproduction steps are available"));
    }

    @Test
    void setRecommendedImplementationOrder_replacesList() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        List<String> steps = new ArrayList<>();
        steps.add("Step 1");
        steps.add("Step 2");
        response.setRecommendedImplementationOrder(steps);
        assertEquals(2, response.getRecommendedImplementationOrder().size());
    }

    @Test
    void setFilesToModify_replacesList() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        List<String> files = new ArrayList<>();
        files.add("File1.java");
        response.setFilesToModify(files);
        assertEquals(1, response.getFilesToModify().size());
    }

    @Test
    void setFilesToReview_replacesList() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        List<String> files = new ArrayList<>();
        files.add("File1.java");
        response.setFilesToReview(files);
        assertEquals(1, response.getFilesToReview().size());
    }

    @Test
    void setComponentsAffected_replacesList() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        List<String> components = new ArrayList<>();
        components.add("Component1");
        response.setComponentsAffected(components);
        assertEquals(1, response.getComponentsAffected().size());
    }

    @Test
    void setDependenciesInvolved_replacesList() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        List<String> deps = new ArrayList<>();
        deps.add("Dependency1");
        response.setDependenciesInvolved(deps);
        assertEquals(1, response.getDependenciesInvolved().size());
    }

    @Test
    void setSuggestedValidationSteps_replacesList() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        List<String> steps = new ArrayList<>();
        steps.add("Validation step 1");
        response.setSuggestedValidationSteps(steps);
        assertEquals(1, response.getSuggestedValidationSteps().size());
    }

    @Test
    void setRisks_replacesList() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        List<String> risks = new ArrayList<>();
        risks.add("Risk 1");
        response.setRisks(risks);
        assertEquals(1, response.getRisks().size());
    }

    @Test
    void setAssumptions_replacesList() {
        ImplementationPlanningResponse response = new ImplementationPlanningResponse();
        List<String> assumptions = new ArrayList<>();
        assumptions.add("Assumption 1");
        response.setAssumptions(assumptions);
        assertEquals(1, response.getAssumptions().size());
    }
}