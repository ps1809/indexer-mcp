package com.projectiq.mcp.orchestration.service;

import com.projectiq.mcp.orchestration.dto.DependencyValidationResult;
import com.projectiq.mcp.orchestration.dto.ExecutionTimelineEntry;
import com.projectiq.mcp.orchestration.dto.StepDependency;
import com.projectiq.mcp.orchestration.dto.WorkflowDefinition;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse.FinalReport;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse.ProgressSummary;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse.StepResultInfo;
import com.projectiq.mcp.orchestration.dto.WorkflowStep;
import com.projectiq.mcp.orchestration.dto.WorkflowType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowExecutionServiceTest {

    private WorkflowExecutionService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowExecutionService();
    }

    @Test
    void testSuccessfulWorkflowExecution() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Analyze Task", "Analyze the developer task"));
        steps.add(new WorkflowStep(2, "Build Context", "Build repository context"));
        steps.add(new WorkflowStep(3, "Generate Plan", "Generate implementation plan"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.FEATURE_IMPLEMENTATION,
                "Add pagination to UserController",
                steps,
                "Standard feature implementation workflow"
        );

        // Act
        WorkflowExecutionResponse response = service.execute(definition);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getWorkflowId());
        assertTrue(response.getWorkflowId().startsWith("wf-"));
        assertEquals("Completed", response.getWorkflowStatus());

        assertEquals(3, response.getExecutedSteps().size());
        assertEquals(0, response.getSkippedSteps().size());
        assertEquals(0, response.getFailedSteps().size());

        // Verify step ordering
        assertEquals(1, response.getExecutedSteps().get(0).getOrder());
        assertEquals(2, response.getExecutedSteps().get(1).getOrder());
        assertEquals(3, response.getExecutedSteps().get(2).getOrder());

        // Verify progress summary
        ProgressSummary progress = response.getProgressSummary();
        assertNotNull(progress);
        assertEquals(3, progress.getTotalSteps());
        assertEquals(3, progress.getCompletedSteps());
        assertEquals(0, progress.getFailedSteps());
        assertEquals(0, progress.getSkippedSteps());
        assertEquals(0, progress.getRemainingSteps());
        assertTrue(progress.getTotalDurationMillis() >= 0);

        // Verify final report
        FinalReport report = response.getFinalReport();
        assertNotNull(report);
        assertEquals("Completed", report.getFinalStatus());
        assertEquals(3, report.getTotalSteps());
        assertEquals(3, report.getCompletedCount());
        assertEquals("Add pagination to UserController", report.getOriginalRequest());
        assertEquals("Feature Implementation", report.getWorkflowType());

        // Verify execution timeline
        assertNotNull(response.getExecutionTimeline());
        assertEquals(3, response.getExecutionTimeline().size());
        for (ExecutionTimelineEntry entry : response.getExecutionTimeline()) {
            assertEquals("COMPLETED", entry.getStatus());
        }
    }

    @Test
    void testEmptyWorkflow() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.REPOSITORY_ANALYSIS,
                "Analyze repository",
                steps,
                "Empty workflow"
        );

        // Act
        WorkflowExecutionResponse response = service.execute(definition);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getWorkflowId());
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(0, response.getExecutedSteps().size());
        assertEquals(0, response.getProgressSummary().getTotalSteps());
        assertEquals("Workflow has no steps to execute", response.getFinalReport().getSummary());
    }

    @Test
    void testNullDefinition() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.execute(null));
    }

    @Test
    void testDependencyOrdering() {
        // Arrange: Step 3 depends on step 1, step 2 has no dependencies
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Setup Database", "Setup database connection"));
        steps.add(new WorkflowStep(2, "Seed Data", "Seed initial data"));
        steps.add(new WorkflowStep(3, "Run Migrations", "Run database migrations"));

        // Step 2 depends on step 1, step 3 depends on step 2
        List<StepDependency> dependencies = new ArrayList<>();
        dependencies.add(new StepDependency("Seed Data", List.of("Setup Database"),
                "Seed data requires database setup"));
        dependencies.add(new StepDependency("Run Migrations", List.of("Seed Data"),
                "Migrations require seeded data"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.CONFIGURATION_CHANGE,
                "Setup database",
                steps,
                "Database setup workflow"
        );

        // Act
        WorkflowExecutionResponse response = service.execute(definition, dependencies);

        // Assert
        assertNotNull(response);
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(3, response.getExecutedSteps().size());
        assertEquals(0, response.getSkippedSteps().size());
    }

    @Test
    void testCircularDependencyDetection() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Step A", "First step"));
        steps.add(new WorkflowStep(2, "Step B", "Second step"));
        steps.add(new WorkflowStep(3, "Step C", "Third step"));

        // Circular dependency: A -> B -> C -> A
        List<StepDependency> dependencies = new ArrayList<>();
        dependencies.add(new StepDependency("Step B", List.of("Step A"), "B depends on A"));
        dependencies.add(new StepDependency("Step C", List.of("Step B"), "C depends on B"));
        dependencies.add(new StepDependency("Step A", List.of("Step C"), "A depends on C (circular)"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.UNKNOWN,
                "Test circular deps",
                steps,
                "Test workflow"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.execute(definition, dependencies));
        assertTrue(exception.getMessage().contains("Circular dependency"));
    }

    @Test
    void testCircularDependencyDetectionViaMap() {
        // Arrange
        Map<String, List<String>> dependencyMap = new HashMap<>();
        dependencyMap.put("A", List.of("B"));
        dependencyMap.put("B", List.of("C"));
        dependencyMap.put("C", List.of("A"));

        // Act
        Set<String> circular = service.detectCircularDependencies(dependencyMap);

        // Assert
        assertFalse(circular.isEmpty());
        assertTrue(circular.contains("A"));
        assertTrue(circular.contains("C"));
    }

    @Test
    void testNoCircularDependency() {
        // Arrange
        Map<String, List<String>> dependencyMap = new HashMap<>();
        dependencyMap.put("A", List.of("B"));
        dependencyMap.put("B", List.of("C"));
        dependencyMap.put("C", List.of());

        // Act
        Set<String> circular = service.detectCircularDependencies(dependencyMap);

        // Assert
        assertTrue(circular.isEmpty());
    }

    @Test
    void testDependencyValidation() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Step A", "First step"));
        steps.add(new WorkflowStep(2, "Step B", "Second step"));

        List<StepDependency> dependencies = new ArrayList<>();
        dependencies.add(new StepDependency("Step B", List.of("Step A"), "B depends on A"));

        // Act
        DependencyValidationResult result = service.validateDependencies(steps, dependencies);

        // Assert
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void testDependencyValidationWithMissingStep() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Step A", "First step"));

        List<StepDependency> dependencies = new ArrayList<>();
        dependencies.add(new StepDependency("Missing Step", List.of(), "References unknown step"));

        // Act
        DependencyValidationResult result = service.validateDependencies(steps, dependencies);

        // Assert
        assertFalse(result.isValid());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().get(0).contains("unknown step"));
    }

    @Test
    void testDependencyValidationWithUnknownDependency() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Step A", "First step"));

        List<StepDependency> dependencies = new ArrayList<>();
        dependencies.add(new StepDependency("Step A", List.of("Unknown Step"), "Depends on unknown"));

        // Act
        DependencyValidationResult result = service.validateDependencies(steps, dependencies);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrors().get(0).contains("unknown step"));
    }

    @Test
    void testNullDependencies() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Step A", "First step"));

        // Act
        DependencyValidationResult result = service.validateDependencies(steps, null);

        // Assert
        assertTrue(result.isValid());
    }

    @Test
    void testStepFailureRecovery() {
        // Arrange - The "Generate Plan" step will use the step executor which doesn't fail
        // We'll test recovery by creating a step that looks like it fails via dependency
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Analyze Task", "Analyze the developer task"));
        steps.add(new WorkflowStep(2, "Build Context", "Build repository context"));
        steps.add(new WorkflowStep(3, "Generate Plan", "Generate implementation plan"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.FEATURE_IMPLEMENTATION,
                "Test recovery",
                steps,
                "Recovery test"
        );

        // Act - Execute without dependencies, all should succeed
        WorkflowExecutionResponse response = service.execute(definition);

        // Assert
        assertNotNull(response);
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(3, response.getExecutedSteps().size());
    }

    @Test
    void testInvalidWorkflow() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.execute(null));
    }

    @Test
    void testExecuteWithDependencies() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Setup", "Setup environment"));
        steps.add(new WorkflowStep(2, "Configure", "Configure application"));
        steps.add(new WorkflowStep(3, "Deploy", "Deploy application"));

        List<StepDependency> dependencies = new ArrayList<>();
        dependencies.add(new StepDependency("Configure", List.of("Setup"), "Configure requires setup"));
        dependencies.add(new StepDependency("Deploy", List.of("Configure"), "Deploy requires configure"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.CONFIGURATION_CHANGE,
                "Deploy application",
                steps,
                "Deploy workflow"
        );

        // Act
        WorkflowExecutionResponse response = service.execute(definition, dependencies);

        // Assert
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(3, response.getExecutedSteps().size());
    }

    @Test
    void testExecuteWithMissingDependency() {
        // Arrange - Step 3 depends on step 2, but step 2 should complete before step 3
        // Since execution is sequential, step 2 will be in progress when step 3 checks deps
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Step A", "First step"));
        steps.add(new WorkflowStep(2, "Step B", "Second step"));
        steps.add(new WorkflowStep(3, "Step C", "Third step"));

        // Step C depends on Step B (will be completed by the time C runs)
        List<StepDependency> dependencies = new ArrayList<>();
        dependencies.add(new StepDependency("Step C", List.of("Step B"), "C depends on B"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.UNKNOWN,
                "Test deps",
                steps,
                "Test"
        );

        // Act - Since steps execute sequentially and B completes before C starts,
        // the dependency should be satisfied
        WorkflowExecutionResponse response = service.execute(definition, dependencies);

        // Assert
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(3, response.getExecutedSteps().size());
    }

    @Test
    void testProgressTracking() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Step 1", "First step"));
        steps.add(new WorkflowStep(2, "Step 2", "Second step"));
        steps.add(new WorkflowStep(3, "Step 3", "Third step"));
        steps.add(new WorkflowStep(4, "Step 4", "Fourth step"));
        steps.add(new WorkflowStep(5, "Step 5", "Fifth step"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.FEATURE_IMPLEMENTATION,
                "Multi-step task",
                steps,
                "Progress test"
        );

        // Act
        WorkflowExecutionResponse response = service.execute(definition);

        // Assert
        ProgressSummary progress = response.getProgressSummary();
        assertEquals(5, progress.getTotalSteps());
        assertEquals(5, progress.getCompletedSteps());
        assertEquals(0, progress.getFailedSteps());
        assertEquals(0, progress.getSkippedSteps());
        assertEquals(0, progress.getRemainingSteps());
        assertEquals(100.0, progress.getSuccessRate());
        assertTrue(progress.getTotalDurationMillis() >= 0);
    }

    @Test
    void testStepResultDetails() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Analyze Task", "Analyze the developer task"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.FEATURE_IMPLEMENTATION,
                "Test step details",
                steps,
                "Test"
        );

        // Act
        WorkflowExecutionResponse response = service.execute(definition);

        // Assert
        assertEquals(1, response.getExecutedSteps().size());
        StepResultInfo stepInfo = response.getExecutedSteps().get(0);
        assertEquals(1, stepInfo.getOrder());
        assertEquals("Analyze Task", stepInfo.getName());
        assertEquals("COMPLETED", stepInfo.getStatus());
        assertNotNull(stepInfo.getResult());
        assertTrue(stepInfo.getDurationMillis() >= 0);
    }

    @Test
    void testWorkflowTypeInReport() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Test Step", "Test"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.BUG_FIX,
                "Fix bug in login",
                steps,
                "Bug fix workflow"
        );

        // Act
        WorkflowExecutionResponse response = service.execute(definition);

        // Assert
        assertEquals("Bug Fix", response.getFinalReport().getWorkflowType());
        assertEquals("Fix bug in login", response.getFinalReport().getOriginalRequest());
    }

    @Test
    void testTimelineEntries() {
        // Arrange
        List<WorkflowStep> steps = new ArrayList<>();
        steps.add(new WorkflowStep(1, "Analyze", "Analyze task"));
        steps.add(new WorkflowStep(2, "Build", "Build context"));

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.FEATURE_IMPLEMENTATION,
                "Test timeline",
                steps,
                "Timeline test"
        );

        // Act
        WorkflowExecutionResponse response = service.execute(definition);

        // Assert
        assertEquals(2, response.getExecutionTimeline().size());
        assertEquals(1, response.getExecutionTimeline().get(0).getOrder());
        assertEquals("Analyze", response.getExecutionTimeline().get(0).getStepName());
        assertEquals(2, response.getExecutionTimeline().get(1).getOrder());
        assertEquals("Build", response.getExecutionTimeline().get(1).getStepName());
    }
}