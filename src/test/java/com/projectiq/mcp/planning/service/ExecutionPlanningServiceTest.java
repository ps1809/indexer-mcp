package com.projectiq.mcp.planning.service;

import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanDependency;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExecutionPlanningService covering all functional requirements.
 */
class ExecutionPlanningServiceTest {

    private ExecutionPlanningService service;

    @BeforeEach
    void setUp() {
        service = new ExecutionPlanningService();
    }

    @Test
    void testFeatureImplementationPlanning() {
        PlanStep step1 = new PlanStep("Analyze Requirements", "Analyze feature requirements", "Analysis");
        PlanStep step2 = new PlanStep("Design Solution", "Design the solution architecture", "Design");
        PlanStep step3 = new PlanStep("Implement Code", "Write implementation code", "Implementation");
        PlanStep step4 = new PlanStep("Write Tests", "Write unit and integration tests", "Testing");
        PlanStep step5 = new PlanStep("Review Changes", "Review and finalize changes", "Review");

        List<PlanStep> steps = List.of(step1, step2, step3, step4, step5);
        List<PlanDependency> deps = List.of(
                new PlanDependency("Design Solution", List.of("Analyze Requirements"), "Requirements must be analyzed first"),
                new PlanDependency("Implement Code", List.of("Design Solution"), "Design must be complete"),
                new PlanDependency("Write Tests", List.of("Implement Code"), "Code must be implemented"),
                new PlanDependency("Review Changes", List.of("Write Tests", "Implement Code"), "Tests and code must be ready")
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "Feature Implementation", "FEATURE_IMPLEMENTATION",
                "Add pagination to UserController", steps, deps);

        ExecutionPlanResponse response = service.generateExecutionPlan(request);

        assertEquals("READY", response.getPlanStatus());
        assertTrue(response.getDependencyValidation().isValid());
        assertFalse(response.getExecutionPhases().isEmpty());
        assertFalse(response.getOrderedImplementationTasks().isEmpty());
        assertEquals(5, response.getOrderedImplementationTasks().size());

        // Verify ordering: Analyze -> Design -> Implement -> Tests -> Review
        assertEquals("Analyze Requirements", response.getOrderedImplementationTasks().get(0).getName());
        assertEquals("Design Solution", response.getOrderedImplementationTasks().get(1).getName());
        assertEquals("Implement Code", response.getOrderedImplementationTasks().get(2).getName());

        // Verify validation checkpoints exist
        assertFalse(response.getValidationCheckpoints().isEmpty());

        // Verify risks assessed
        assertNotNull(response.getPotentialRisks());

        // Verify effort estimated
        assertNotNull(response.getEstimatedImplementationEffort());
        assertEquals(5, response.getEstimatedImplementationEffort().getTotalTasks());

        // Verify planning summary
        assertNotNull(response.getPlanningSummary());
        assertEquals(5, response.getPlanningSummary().getTotalTasks());
    }

    @Test
    void testBugFixPlanning() {
        PlanStep step1 = new PlanStep("Reproduce Bug", "Reproduce the reported bug", "Analysis");
        PlanStep step2 = new PlanStep("Identify Root Cause", "Find the root cause", "Analysis");
        PlanStep step3 = new PlanStep("Apply Fix", "Apply the bug fix", "Implementation");
        PlanStep step4 = new PlanStep("Verify Fix", "Verify the bug is fixed", "Testing");

        List<PlanStep> steps = List.of(step1, step2, step3, step4);
        List<PlanDependency> deps = List.of(
                new PlanDependency("Identify Root Cause", List.of("Reproduce Bug"), "Must reproduce first"),
                new PlanDependency("Apply Fix", List.of("Identify Root Cause"), "Must identify root cause"),
                new PlanDependency("Verify Fix", List.of("Apply Fix"), "Fix must be applied")
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "Bug Fix", "BUG_FIX", "Fix null pointer in UserService", steps, deps);

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("READY", response.getPlanStatus());
        assertEquals(4, response.getOrderedImplementationTasks().size());
        assertEquals("Reproduce Bug", response.getOrderedImplementationTasks().get(0).getName());
    }

    @Test
    void testRefactoringPlanning() {
        PlanStep step1 = new PlanStep("Analyze Code", "Analyze code to refactor", "Analysis");
        PlanStep step2 = new PlanStep("Extract Method", "Extract method for clarity", "Refactoring");
        PlanStep step3 = new PlanStep("Update Callers", "Update all callers", "Refactoring");
        PlanStep step4 = new PlanStep("Run Tests", "Run tests to verify", "Testing");

        List<PlanStep> steps = List.of(step1, step2, step3, step4);
        List<PlanDependency> deps = List.of(
                new PlanDependency("Extract Method", List.of("Analyze Code"), "Must analyze first"),
                new PlanDependency("Update Callers", List.of("Extract Method"), "Method must be extracted"),
                new PlanDependency("Run Tests", List.of("Update Callers"), "Callers must be updated")
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "Refactoring", "REFACTORING", "Refactor UserService methods", steps, deps);

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("READY", response.getPlanStatus());
        assertEquals(4, response.getOrderedImplementationTasks().size());
        assertEquals("Analysis", response.getExecutionPhases().get(0).getName());
    }

    @Test
    void testDependencyValidation() {
        PlanStep step1 = new PlanStep("Step A", "First step", null);
        PlanStep step2 = new PlanStep("Step B", "Second step", null);
        PlanStep step3 = new PlanStep("Step C", "Third step", null);

        List<PlanStep> steps = List.of(step1, step2, step3);
        List<PlanDependency> deps = List.of(
                new PlanDependency("Step B", List.of("Step A"), "A must complete"),
                new PlanDependency("Step C", List.of("Step B"), "B must complete")
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "ValidationTest", "TEST", "Test validation", steps, deps);

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertTrue(response.getDependencyValidation().isValid());
        assertEquals("READY", response.getPlanStatus());
    }

    @Test
    void testCircularDependencyDetection() {
        PlanStep step1 = new PlanStep("Step A", "First step", null);
        PlanStep step2 = new PlanStep("Step B", "Second step", null);
        PlanStep step3 = new PlanStep("Step C", "Third step", null);

        List<PlanStep> steps = List.of(step1, step2, step3);
        List<PlanDependency> deps = List.of(
                new PlanDependency("Step B", List.of("Step A"), "A must complete"),
                new PlanDependency("Step C", List.of("Step B"), "B must complete"),
                new PlanDependency("Step A", List.of("Step C"), "C must complete")
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "CircularTest", "TEST", "Test circular", steps, deps);

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("BLOCKED", response.getPlanStatus());
        assertFalse(response.getDependencyValidation().isValid());
        assertTrue(response.getDependencyValidation().getErrors().stream()
                .anyMatch(e -> e.contains("Circular dependency")));
    }

    @Test
    void testEmptyWorkflow() {
        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "Empty", "TEST", "Empty workflow", List.of(), List.of());

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("INVALID", response.getPlanStatus());
        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void testNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.generateExecutionPlan(null));
    }

    @Test
    void testMissingWorkflowName() {
        List<PlanStep> steps = List.of(new PlanStep("Step1", "First step", null));
        ExecutionPlanRequest request = new ExecutionPlanRequest(null, "TEST", "", steps, List.of());

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("INVALID", response.getPlanStatus());
    }

    @Test
    void testNoDependencies() {
        PlanStep step1 = new PlanStep("Step A", "First step", "Phase1");
        PlanStep step2 = new PlanStep("Step B", "Second step", "Phase2");

        List<PlanStep> steps = List.of(step1, step2);

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "NoDeps", "TEST", "No dependencies", steps, List.of());

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("READY", response.getPlanStatus());
        assertEquals(2, response.getOrderedImplementationTasks().size());
    }

    @Test
    void testUnknownStepInDependency() {
        PlanStep step1 = new PlanStep("Step A", "First step", null);
        List<PlanStep> steps = List.of(step1);
        List<PlanDependency> deps = List.of(
                new PlanDependency("Unknown Step", List.of("Step A"), "References unknown")
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "UnknownDep", "TEST", "Unknown dependency", steps, deps);

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("BLOCKED", response.getPlanStatus());
        assertFalse(response.getDependencyValidation().isValid());
    }

    @Test
    void testDuplicateStepNames() {
        List<PlanStep> steps = List.of(
                new PlanStep("Duplicate", "First", null),
                new PlanStep("Duplicate", "Second", null)
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "Dups", "TEST", "Duplicate steps", steps, List.of());

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("INVALID", response.getPlanStatus());
    }

    @Test
    void testCriticalPathDetection() {
        PlanStep step1 = new PlanStep("Step A", "First step", null);
        PlanStep step2 = new PlanStep("Step B", "Depends on A", null);
        PlanStep step3 = new PlanStep("Step C", "Depends on B", null);
        PlanStep step4 = new PlanStep("Step D", "Independent", null);

        List<PlanStep> steps = List.of(step1, step2, step3, step4);
        List<PlanDependency> deps = List.of(
                new PlanDependency("Step B", List.of("Step A"), "A first"),
                new PlanDependency("Step C", List.of("Step B"), "B first")
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "CriticalPath", "TEST", "Test critical path", steps, deps);

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertNotNull(response.getCriticalPath());
        assertFalse(response.getCriticalPath().isEmpty());
    }

    @Test
    void testDetectCircularDependencies() {
        Map<String, List<String>> circularMap = Map.of(
                "A", List.of("B"),
                "B", List.of("C"),
                "C", List.of("A")
        );

        Set<String> result = service.detectCircularDependencies(circularMap);
        assertFalse(result.isEmpty());
    }

    @Test
    void testNoCircularDependencies() {
        Map<String, List<String>> validMap = Map.of(
                "A", List.of(),
                "B", List.of("A"),
                "C", List.of("B")
        );

        Set<String> result = service.detectCircularDependencies(validMap);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSelfDependency() {
        PlanStep step1 = new PlanStep("Step A", "First step", null);
        List<PlanStep> steps = List.of(step1);
        List<PlanDependency> deps = List.of(
                new PlanDependency("Step A", List.of("Step A"), "Self dependency")
        );

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "SelfDep", "TEST", "Self dependency", steps, deps);

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals("BLOCKED", response.getPlanStatus());
    }

    @Test
    void testCategorizedPhases() {
        PlanStep step1 = new PlanStep("Analyze", "Analysis step", "Analysis");
        PlanStep step2 = new PlanStep("Implement", "Implementation step", "Implementation");
        PlanStep step3 = new PlanStep("Test", "Testing step", "Testing");

        List<PlanStep> steps = List.of(step1, step2, step3);

        ExecutionPlanRequest request = new ExecutionPlanRequest(
                "Categorized", "TEST", "Categorized steps", steps, List.of());

        ExecutionPlanResponse response = service.generateExecutionPlan(request);
        assertEquals(3, response.getExecutionPhases().size());
        assertEquals("Analysis", response.getExecutionPhases().get(0).getName());
        assertEquals("Implementation", response.getExecutionPhases().get(1).getName());
        assertEquals("Testing", response.getExecutionPhases().get(2).getName());
    }
}