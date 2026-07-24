package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlanExecutionTool MCP tool.
 */
class PlanExecutionToolTest {

    private PlanExecutionTool tool;

    @BeforeEach
    void setUp() {
        tool = new PlanExecutionTool(new com.projectiq.mcp.planning.service.ExecutionPlanningService());
    }

    @Test
    void testPlanExecutionWithValidInput() throws JsonProcessingException {
        String stepsJson = """
                [
                    {"name":"Analyze","description":"Analyze requirements","category":"Analysis"},
                    {"name":"Implement","description":"Implement solution","category":"Implementation"},
                    {"name":"Test","description":"Test implementation","category":"Testing"}
                ]
                """;

        String depsJson = """
                [
                    {"stepName":"Implement","dependsOn":["Analyze"],"description":"Analysis first"},
                    {"stepName":"Test","dependsOn":["Implement"],"description":"Implement first"}
                ]
                """;

        String result = tool.planExecution("TestWorkflow", "FEATURE_IMPLEMENTATION",
                "Test feature", stepsJson, depsJson);

        assertNotNull(result);
        assertFalse(result.contains("errorType"));

        ObjectMapper mapper = new ObjectMapper();
        ExecutionPlanResponse response = mapper.readValue(result, ExecutionPlanResponse.class);
        assertEquals("READY", response.getPlanStatus());
        assertEquals("TestWorkflow", response.getWorkflowName());
        assertEquals(3, response.getOrderedImplementationTasks().size());
    }

    @Test
    void testPlanExecutionWithoutDependencies() throws JsonProcessingException {
        String stepsJson = """
                [
                    {"name":"Step1","description":"First step","category":"Phase1"},
                    {"name":"Step2","description":"Second step","category":"Phase2"}
                ]
                """;

        String result = tool.planExecution("SimpleWorkflow", null, null, stepsJson, null);

        assertNotNull(result);
        assertFalse(result.contains("errorType"));

        ObjectMapper mapper = new ObjectMapper();
        ExecutionPlanResponse response = mapper.readValue(result, ExecutionPlanResponse.class);
        assertEquals("READY", response.getPlanStatus());
        assertEquals(2, response.getOrderedImplementationTasks().size());
    }

    @Test
    void testPlanExecutionWithEmptyWorkflowName() {
        String stepsJson = """
                [{"name":"Step1","description":"First step","category":"Phase1"}]
                """;

        String result = tool.planExecution("", "TEST", "", stepsJson, null);
        assertTrue(result.contains("errorType"));
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testPlanExecutionWithNullSteps() {
        String result = tool.planExecution("Test", "TEST", null, null, null);
        assertTrue(result.contains("errorType"));
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testPlanExecutionWithInvalidStepsJson() {
        String result = tool.planExecution("Test", "TEST", null, "invalid json", null);
        assertTrue(result.contains("errorType"));
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testPlanExecutionWithInvalidDepsJson() {
        String stepsJson = """
                [{"name":"Step1","description":"First step","category":"Phase1"}]
                """;

        String result = tool.planExecution("Test", "TEST", null, stepsJson, "invalid json");
        assertTrue(result.contains("errorType"));
        assertTrue(result.contains("INVALID_ARGUMENT"));
    }

    @Test
    void testPlanExecutionWithCircularDependency() throws JsonProcessingException {
        String stepsJson = """
                [
                    {"name":"A","description":"Step A","category":null},
                    {"name":"B","description":"Step B","category":null},
                    {"name":"C","description":"Step C","category":null}
                ]
                """;

        String depsJson = """
                [
                    {"stepName":"B","dependsOn":["A"],"description":"A first"},
                    {"stepName":"C","dependsOn":["B"],"description":"B first"},
                    {"stepName":"A","dependsOn":["C"],"description":"C first"}
                ]
                """;

        String result = tool.planExecution("Circular", "TEST", null, stepsJson, depsJson);

        assertNotNull(result);
        ObjectMapper mapper = new ObjectMapper();
        ExecutionPlanResponse response = mapper.readValue(result, ExecutionPlanResponse.class);
        assertEquals("BLOCKED", response.getPlanStatus());
    }

    @Test
    void testPlanExecutionWithEmptySteps() throws JsonProcessingException {
        String result = tool.planExecution("Test", "TEST", null, "[]", null);
        // Empty steps returns a valid response with INVALID status, not an error
        ObjectMapper mapper = new ObjectMapper();
        ExecutionPlanResponse response = mapper.readValue(result, ExecutionPlanResponse.class);
        assertEquals("INVALID", response.getPlanStatus());
        assertFalse(response.getErrors().isEmpty());
    }

    @Test
    void testPlanExecutionGeneratesPhases() throws JsonProcessingException {
        String stepsJson = """
                [
                    {"name":"Analyze","description":"Analysis","category":"Analysis"},
                    {"name":"Design","description":"Design","category":"Design"},
                    {"name":"Implement","description":"Implementation","category":"Implementation"}
                ]
                """;

        String result = tool.planExecution("Phased", "FEATURE_IMPLEMENTATION",
                "Test phases", stepsJson, null);

        ObjectMapper mapper = new ObjectMapper();
        ExecutionPlanResponse response = mapper.readValue(result, ExecutionPlanResponse.class);
        assertFalse(response.getExecutionPhases().isEmpty());
        assertNotNull(response.getPlanningSummary());
        assertNotNull(response.getEstimatedImplementationEffort());
    }
}