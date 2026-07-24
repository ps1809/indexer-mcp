package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.ImplementationPlanningResponse;
import com.projectiq.mcp.analysis.service.ImplementationPlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that generates a deterministic implementation plan for a requested
 * development task. Invokes task analysis, context assembly, and impact analysis
 * to produce a structured plan that guides AI coding agents through the
 * recommended sequence of implementation steps without generating any code.
 *
 * <p>The plan includes the original task, task type, estimated complexity,
 * recommended implementation order, files to modify/review, affected components,
 * dependencies, validation steps, testing scope, risks, and assumptions.</p>
 */
@Component
public class ImplementationPlanTool {

    private static final Logger logger = LoggerFactory.getLogger(ImplementationPlanTool.class);

    private final ImplementationPlanningService planningService;
    private final ObjectMapper objectMapper;

    public ImplementationPlanTool(ImplementationPlanningService planningService) {
        this.planningService = planningService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Generates a deterministic implementation plan for a requested development task.
     *
     * @param task           Natural language development request (e.g., "Add pagination to UserController") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing the implementation plan
     */
    @Tool(description = """
            Generates a deterministic implementation plan for a requested development task.
            
            This tool performs a complete implementation planning workflow:
            1. Analyzes the task to determine task type, entities, and complexity
            2. Assembles relevant repository context
            3. Analyzes the impact of the proposed change
            4. Produces a structured implementation plan
            
            Returns a structured implementation plan containing:
            - Original Task: The input task description
            - Task Type: The detected development activity type
            - Estimated Complexity: Low, Medium, or High based on analysis
            - Recommended Implementation Order: Numbered steps for implementation sequence
            - Files to Modify: Files that will need changes
            - Files to Review: Files that should be reviewed for context
            - Components Affected: All components impacted by the change
            - Dependencies Involved: External and internal dependencies affected
            - Suggested Validation Steps: Recommended verification steps
            - Suggested Testing Scope: Scope of testing required
            - Risks: Identified risks with mitigation strategies
            - Assumptions: Planning assumptions made during analysis
            
            Useful for AI coding agents to understand the recommended sequence of
            work before writing any code. This tool never generates or modifies code.
            """)
    public String generateImplementationPlan(
            String task,
            String repositoryName,
            String branch) {
        try {
            if (task == null || task.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Task description is required");
            }

            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

            logger.info("Executing implementation_plan tool for task: {} in repository: {} branch: {}",
                    task, repositoryName, effectiveBranch);

            ImplementationPlanningResponse response = planningService.generatePlan(
                    task.trim(), repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating implementation plan: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(ImplementationPlanningResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize implementation plan: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new ImplementationPlanError(errorType, message)
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
    private static class ImplementationPlanError {
        public String errorType;
        public String message;

        public ImplementationPlanError(String errorType, String message) {
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