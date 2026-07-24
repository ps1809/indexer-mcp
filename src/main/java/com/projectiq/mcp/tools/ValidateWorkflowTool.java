package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanDependency;
import com.projectiq.mcp.planning.dto.ExecutionPlanRequest.PlanStep;
import com.projectiq.mcp.validation.dto.ValidationReport;
import com.projectiq.mcp.validation.service.WorkflowValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP Tool that validates a complete execution workflow before implementation begins.
 * Performs an intelligent validation pipeline covering workflow completeness, repository
 * readiness, dependency consistency, architecture compliance, convention validation,
 * test readiness, risk assessment, and execution prerequisites.
 *
 * <p>This tool does not generate or modify any source code. It purely validates the
 * workflow based on deterministic rules without any AI/LLM involvement.</p>
 */
@Component
public class ValidateWorkflowTool {

    private static final Logger logger = LoggerFactory.getLogger(ValidateWorkflowTool.class);

    private final WorkflowValidationService validationService;
    private final ObjectMapper objectMapper;

    public ValidateWorkflowTool(WorkflowValidationService validationService) {
        this.validationService = validationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Validates a complete execution workflow before an AI coding agent begins implementation.
     * The validation pipeline checks eight categories: Workflow, Repository, Dependency,
     * Architecture, Convention, Test Coverage, Risk, and Execution Readiness.
     *
     * @param workflowName      Name of the workflow to validate (required)
     * @param workflowType      Type of workflow (e.g., "Feature Implementation", "Bug Fix") (optional)
     * @param originalRequest   Original developer request that defines the workflow (optional)
     * @param stepsJson         JSON array of step objects with name, description, and category (required)
     * @param dependenciesJson  JSON array of dependency objects with stepName, dependsOn, and description (optional)
     * @param repositoryName    Name of the target repository (required)
     * @param branch            Branch name for analysis (optional, defaults to "main")
     * @return A structured JSON response containing the validation report
     */
    @Tool(description = """
            Validates a complete execution workflow before an AI coding agent begins implementation.
            
            This tool performs an intelligent validation pipeline covering 8 categories:
            1. Workflow Validation - Checks workflow completeness (name, type, steps)
            2. Repository Validation - Validates repository health and readiness
            3. Dependency Validation - Checks for circular dependencies and missing references
            4. Architecture Validation - Validates architecture compliance
            5. Convention Validation - Checks repository coding conventions
            6. Test Coverage Validation - Evaluates test readiness
            7. Risk Validation - Identifies implementation risks using impact analysis
            8. Execution Readiness - Validates prerequisites for execution
            
            Returns a structured validation report containing:
            - Overall validation status (PASSED, PASSED_WITH_WARNINGS, WARNINGS, BLOCKED)
            - Passed/Failed/Warning/Blocking counts
            - All validation findings with category, severity, and details
            - Repository health summary with key metrics
            - Risk summary with critical, high, and medium risks
            - Readiness score (0-100) and readiness label
            - Recommended actions for addressing issues
            
            Useful for AI coding agents to validate the execution environment before
            starting any implementation work. This tool never generates or modifies
            source code.
            """)
    public String validateWorkflow(
            String workflowName,
            String workflowType,
            String originalRequest,
            String stepsJson,
            String dependenciesJson,
            String repositoryName,
            String branch) {
        try {
            // Validate required parameters
            if (workflowName == null || workflowName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Workflow name is required");
            }

            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
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

            logger.info("Executing validate_workflow tool for workflow: {} in repository: {}",
                    workflowName, repositoryName);

            ValidationReport report = validationService.validateWorkflow(
                    workflowName.trim(),
                    workflowType != null ? workflowType.trim() : null,
                    originalRequest != null ? originalRequest.trim() : null,
                    steps,
                    dependencies,
                    repositoryName.trim(),
                    branch != null ? branch.trim() : null);

            return serializeReport(report);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error validating workflow: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Parses a JSON array of step objects into a list of PlanStep.
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

    private String serializeReport(ValidationReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize validation report: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new ValidationToolError(errorType, message)
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
    private static class ValidationToolError {
        public String errorType;
        public String message;

        public ValidationToolError(String errorType, String message) {
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