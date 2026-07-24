package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.orchestration.dto.StepDependency;
import com.projectiq.mcp.orchestration.dto.WorkflowDefinition;
import com.projectiq.mcp.orchestration.dto.WorkflowExecutionResponse;
import com.projectiq.mcp.orchestration.dto.WorkflowStep;
import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.orchestration.service.WorkflowExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecuteWorkflowToolTest {

    private ExecuteWorkflowTool tool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        WorkflowExecutionService service = new WorkflowExecutionService();
        tool = new ExecuteWorkflowTool(service);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSuccessfulWorkflowExecution() throws Exception {
        // Arrange
        List<WorkflowStep> steps = List.of(
                new WorkflowStep(1, "Analyze Task", "Analyze the developer task"),
                new WorkflowStep(2, "Build Context", "Build repository context"),
                new WorkflowStep(3, "Generate Plan", "Generate implementation plan")
        );

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.FEATURE_IMPLEMENTATION,
                "Add pagination to UserController",
                steps,
                "Standard feature implementation workflow"
        );

        String workflowJson = objectMapper.writeValueAsString(definition);

        // Act
        String result = tool.executeWorkflow(workflowJson, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("workflowId"));
        assertTrue(result.contains("workflowStatus"));
        assertTrue(result.contains("Completed"));
        assertTrue(result.contains("executedSteps"));
        assertTrue(result.contains("finalReport"));

        WorkflowExecutionResponse response = objectMapper.readValue(result, WorkflowExecutionResponse.class);
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(3, response.getExecutedSteps().size());
    }

    @Test
    void testEmptyWorkflow() throws Exception {
        // Arrange
        List<WorkflowStep> steps = List.of();
        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.REPOSITORY_ANALYSIS,
                "Analyze repository",
                steps,
                "Empty workflow"
        );
        String workflowJson = objectMapper.writeValueAsString(definition);

        // Act
        String result = tool.executeWorkflow(workflowJson, null);

        // Assert
        assertNotNull(result);
        WorkflowExecutionResponse response = objectMapper.readValue(result, WorkflowExecutionResponse.class);
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(0, response.getProgressSummary().getTotalSteps());
    }

    @Test
    void testInvalidWorkflowJson() {
        // Act
        String result = tool.executeWorkflow("invalid json", null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("errorType"));
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testNullWorkflowJson() {
        // Act
        String result = tool.executeWorkflow(null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("errorType"));
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testExecuteWithDependencies() throws Exception {
        // Arrange
        List<WorkflowStep> steps = List.of(
                new WorkflowStep(1, "Setup", "Setup environment"),
                new WorkflowStep(2, "Configure", "Configure application"),
                new WorkflowStep(3, "Deploy", "Deploy application")
        );

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.CONFIGURATION_CHANGE,
                "Deploy application",
                steps,
                "Deploy workflow"
        );

        List<StepDependency> dependencies = List.of(
                new StepDependency("Configure", List.of("Setup"), "Configure requires setup"),
                new StepDependency("Deploy", List.of("Configure"), "Deploy requires configure")
        );

        String workflowJson = objectMapper.writeValueAsString(definition);
        String depsJson = objectMapper.writeValueAsString(dependencies);

        // Act
        String result = tool.executeWorkflow(workflowJson, depsJson);

        // Assert
        assertNotNull(result);
        WorkflowExecutionResponse response = objectMapper.readValue(result, WorkflowExecutionResponse.class);
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(3, response.getExecutedSteps().size());
    }

    @Test
    void testCircularDependencyDetection() throws Exception {
        // Arrange
        List<WorkflowStep> steps = List.of(
                new WorkflowStep(1, "Step A", "First step"),
                new WorkflowStep(2, "Step B", "Second step"),
                new WorkflowStep(3, "Step C", "Third step")
        );

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.UNKNOWN,
                "Test circular deps",
                steps,
                "Test workflow"
        );

        List<StepDependency> dependencies = List.of(
                new StepDependency("Step B", List.of("Step A"), "B depends on A"),
                new StepDependency("Step C", List.of("Step B"), "C depends on B"),
                new StepDependency("Step A", List.of("Step C"), "A depends on C (circular)")
        );

        String workflowJson = objectMapper.writeValueAsString(definition);
        String depsJson = objectMapper.writeValueAsString(dependencies);

        // Act
        String result = tool.executeWorkflow(workflowJson, depsJson);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("errorType") || result.contains("INVALID_ARGUMENT"));
        assertTrue(result.contains("Circular dependency") || result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testExecuteWorkflowSimple() throws Exception {
        // Arrange
        List<WorkflowStep> steps = List.of(
                new WorkflowStep(1, "Test Step", "Test step description")
        );

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.BUG_FIX,
                "Fix bug",
                steps,
                "Bug fix workflow"
        );

        String workflowJson = objectMapper.writeValueAsString(definition);

        // Act
        String result = tool.executeWorkflowSimple(workflowJson);

        // Assert
        assertNotNull(result);
        WorkflowExecutionResponse response = objectMapper.readValue(result, WorkflowExecutionResponse.class);
        assertEquals("Completed", response.getWorkflowStatus());
        assertEquals(1, response.getExecutedSteps().size());
    }

    @Test
    void testResponseHasAllRequiredFields() throws Exception {
        // Arrange
        List<WorkflowStep> steps = List.of(
                new WorkflowStep(1, "Analyze", "Analyze task")
        );

        WorkflowDefinition definition = new WorkflowDefinition(
                WorkflowType.FEATURE_IMPLEMENTATION,
                "Test task",
                steps,
                "Test"
        );

        String workflowJson = objectMapper.writeValueAsString(definition);

        // Act
        String result = tool.executeWorkflow(workflowJson, null);
        WorkflowExecutionResponse response = objectMapper.readValue(result, WorkflowExecutionResponse.class);

        // Assert - All required fields present
        assertNotNull(response.getWorkflowId());
        assertNotNull(response.getWorkflowStatus());
        assertNotNull(response.getExecutedSteps());
        assertNotNull(response.getSkippedSteps());
        assertNotNull(response.getFailedSteps());
        assertNotNull(response.getProgressSummary());
        assertNotNull(response.getExecutionTimeline());
        assertNotNull(response.getFinalReport());
    }
}