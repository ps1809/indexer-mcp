package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanDependency;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import com.projectiq.mcp.planning.dto.ExecutionPlanResponse;
import com.projectiq.mcp.planning.service.ExecutionPlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP Tool that generates a deterministic execution roadmap for AI coding agents.
 * Accepts workflow information (steps, dependencies), builds an optimized execution
 * strategy, validates dependencies, estimates effort, identifies risks, and produces
 * a structured implementation roadmap.
 *
 * <p>This tool does not generate or modify any source code. It purely plans the
 * execution strategy based on deterministic rules without any AI/LLM involvement.</p>
 */
@Component
public class PlanExecutionTool {

    private static final Logger logger = LoggerFactory.getLogger(PlanExecutionTool.class);

    private final ExecutionPlanningService planningService;
    private final ObjectMapper objectMapper;

    public PlanExecutionTool(ExecutionPlanningService planningService) {
        this.planningService = planningService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Generates a deterministic execution roadmap for a set of workflow steps.
     * Validates dependencies, determines optimal execution order, identifies risks,
     * estimates effort, and produces a structured implementation strategy.
     *
     * @param workflowName   Name of the workflow to plan (required)
     * @param workflowType   Type of workflow (e.g., "Feature Implementation", "Bug Fix", "Refactoring") (optional)
     * @param originalRequest Original user request that defines the workflow (optional)
     * @param stepsJson      JSON array of step objects with name, description, and category (required)
     * @param dependenciesJson JSON array of dependency objects with stepName, dependsOn, and description (optional)
     * @return A structured JSON response containing the execution roadmap
     */
    @Tool(description = """
            Generates a deterministic execution roadmap for a set of workflow steps.
            
            This tool performs complete execution planning:
            1. Validates workflow steps and dependencies
            2. Detects circular dependencies and missing step references
            3. Determines optimal execution order using topological sort
            4. Groups steps into logical execution phases
            5. Estimates implementation effort and complexity
            6. Identifies potential risks and provides mitigations
            7. Detects the critical path through the execution plan
            8. Generates validation checkpoints and testing points
            9. Produces a structured planning summary with recommendations
            
            Returns a structured execution roadmap containing:
            - Execution Phases: Logical groupings of related tasks
            - Ordered Implementation Tasks: Deterministic step-by-step sequence
            - Required Prerequisites: Dependencies that must be resolved first
            - Validation Checkpoints: Points to verify correctness
            - Recommended Testing Points: When and what to test
            - Potential Risks: Identified risks with severity and mitigations
            - Estimated Implementation Effort: Time and complexity estimates
            - Critical Path: Longest dependency chain that determines duration
            - Planning Summary: Overview with actionable recommendations
            
            Useful for AI coding agents to understand the optimal execution
            sequence before starting any implementation work. This tool never
            generates or modifies source code.
            """)
    public String planExecution(
            String workflowName,
            String workflowType,
            String originalRequest,
            String stepsJson,
            String dependenciesJson) {
        try {
            // Validate required parameters
            if (workflowName == null || workflowName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Workflow name is required");
            }

            if (stepsJson == null || stepsJson.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Steps JSON is required");
            }

            // Parse steps from JSON
            List<PlanStep> steps = parseSteps(stepsJson);
            if (steps == null) {
                return createErrorResponse("INVALID_ARGUMENT", "Failed to parse steps JSON");
            }

            // Parse dependencies from JSON (optional)
            List<PlanDependency> dependencies = new ArrayList<>();
            if (dependenciesJson != null && !dependenciesJson.trim().isEmpty()) {
                dependencies = parseDependencies(dependenciesJson);
                if (dependencies == null) {
                    return createErrorResponse("INVALID_ARGUMENT", "Failed to parse dependencies JSON");
                }
            }

            logger.info("Executing plan_execution tool for workflow: {} (type: {}) with {} steps and {} dependencies",
                    workflowName, workflowType, steps.size(), dependencies.size());

            ExecutionPlanRequest request = new ExecutionPlanRequest(
                    workflowName.trim(),
                    workflowType != null ? workflowType.trim() : "Unknown",
                    originalRequest != null ? originalRequest.trim() : "",
                    steps,
                    dependencies);

            ExecutionPlanResponse response = planningService.generateExecutionPlan(request);
            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating execution plan: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Parses a JSON array of step objects into a list of PlanStep.
     * Expected JSON format: [{"name":"...","description":"...","category":"..."}]
     */
    private List<PlanStep> parseSteps(String stepsJson) {
        try {
            return objectMapper.readValue(stepsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PlanStep.class));
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse steps JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parses a JSON array of dependency objects into a list of PlanDependency.
     * Expected JSON format: [{"stepName":"...","dependsOn":["..."],"description":"..."}]
     */
    private List<PlanDependency> parseDependencies(String dependenciesJson) {
        try {
            return objectMapper.readValue(dependenciesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PlanDependency.class));
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse dependencies JSON: {}", e.getMessage());
            return null;
        }
    }

    private String serializeResponse(ExecutionPlanResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize execution plan: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new PlanExecutionError(errorType, message)
            );
        } catch (JsonProcessingException e) {
            return "{\"errorType\":\"" + escapeJson(errorType)
                    + "\",\"message\":\"" + escapeJson(message) + "\"}";
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Simple error DTO for JSON error responses.
     */
    private static class PlanExecutionError {
        public String errorType;
        public String message;

        public PlanExecutionError(String errorType, String message) {
            this.errorType = errorType;
            this.message = message;
        }

        public String getErrorType() {
            return errorType;
        }

        public String getMessage() {
            return message;
        }
    }
}