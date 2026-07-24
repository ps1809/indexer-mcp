package com.projectiq.mcp.planning.dto;

import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanDependency;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExecutionPlanRequest and its nested types.
 */
class ExecutionPlanRequestTest {

    @Test
    void testConstructorAndGetters() {
        PlanStep step1 = new PlanStep("Analyze", "Analyze codebase", "Analysis");
        PlanStep step2 = new PlanStep("Implement", "Implement changes", "Implementation");
        List<PlanStep> steps = List.of(step1, step2);

        PlanDependency dep = new PlanDependency("Implement", List.of("Analyze"), "Analysis must complete first");
        List<PlanDependency> deps = List.of(dep);

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "TestWorkflow", "Feature Implementation", "Add feature X", steps, deps);

        assertEquals("TestWorkflow", request.getWorkflowName());
        assertEquals("Feature Implementation", request.getWorkflowType());
        assertEquals("Add feature X", request.getOriginalRequest());
        assertEquals(2, request.getSteps().size());
        assertEquals(1, request.getDependencies().size());
        assertThrows(UnsupportedOperationException.class, () -> request.getSteps().add(step1));
        assertThrows(UnsupportedOperationException.class, () -> request.getDependencies().add(dep));
    }

    @Test
    void testNullStepsDefaultsToEmpty() {
        ExecutionPlanRequest request = new ExecutionPlanRequest("Test", null, null, null, null);
        assertTrue(request.getSteps().isEmpty());
        assertTrue(request.getDependencies().isEmpty());
    }

    @Test
    void testPlanStep() {
        PlanStep step = new PlanStep("Validate", "Validate configuration", "Validation");
        assertEquals("Validate", step.getName());
        assertEquals("Validate configuration", step.getDescription());
        assertEquals("Validation", step.getCategory());
    }

    @Test
    void testPlanDependency() {
        PlanDependency dep = new PlanDependency("StepB", List.of("StepA"), "StepA must complete first");
        assertEquals("StepB", dep.getStepName());
        assertEquals(1, dep.getDependsOn().size());
        assertTrue(dep.getDependsOn().contains("StepA"));
        assertEquals("StepA must complete first", dep.getDescription());
    }

    @Test
    void testPlanDependencyNullDependsOn() {
        PlanDependency dep = new PlanDependency("StepB", null, null);
        assertTrue(dep.getDependsOn().isEmpty());
        assertNull(dep.getDescription());
    }

    @Test
    void testUnmodifiableDependencyList() {
        PlanDependency dep = new PlanDependency("StepB", List.of("StepA"), null);
        assertThrows(UnsupportedOperationException.class, () -> dep.getDependsOn().add("StepC"));
    }
}