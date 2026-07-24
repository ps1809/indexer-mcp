package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.ContextAssemblyResponse;
import com.projectiq.mcp.analysis.service.ContextAssemblyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that accepts a natural language development task, automatically
 * invokes task analysis, executes the generated execution plan, assembles
 * all retrieved information, and returns a consolidated development context.
 *
 * <p>This is the primary entry point for Automatic Context Assembly, enabling
 * AI coding agents to obtain a complete repository and development context
 * with a single MCP tool invocation.</p>
 */
@Component
public class AssembleContextTool {

    private static final Logger logger = LoggerFactory.getLogger(AssembleContextTool.class);

    private final ContextAssemblyService contextAssemblyService;
    private final ObjectMapper objectMapper;

    public AssembleContextTool(ContextAssemblyService contextAssemblyService) {
        this.contextAssemblyService = contextAssemblyService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes a natural language development task, executes the required
     * MCP tools, and assembles a complete development context with all
     * retrieved information.
     *
     * @param task           Natural language development task (e.g., "Add pagination to UserController") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing the assembled context
     */
    @Tool(description = """
            Automatically analyzes a development task and assembles a complete repository context.
            
            This tool performs the complete context assembly workflow:
            1. Analyzes the task to determine task type, entities, and required tools
            2. Generates an ordered execution plan
            3. Executes all required MCP tools (repository_summary, search_code, find_class, etc.)
            4. Eliminates duplicate information
            5. Assembles a consolidated development context
            
            Returns a structured response containing:
            - Original Task: The input task description
            - Execution Plan: The generated execution plan with ordered steps
            - Executed Tools: Which tools were executed
            - Skipped Tools: Which tools were skipped (e.g., duplicates)
            - Failed Tools: Which tools failed (with error messages)
            - Repository Context: Aggregated repository information
            - Development Context: Implementation-focused development context
            - Execution Summary: Summary of the assembly process
            - Total Execution Time: Time taken in milliseconds
            
            Useful for AI coding agents to obtain complete repository context
            with a single MCP tool invocation.
            """)
    public String assembleContext(
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

            logger.info("Executing assemble_context tool for task: {} in repository: {} branch: {}",
                    task, repositoryName, effectiveBranch);

            ContextAssemblyResponse response = contextAssemblyService.assembleContext(
                    task.trim(), repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error assembling context: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(ContextAssemblyResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize assembly response: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new AssembleContextError(errorType, message)
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
    private static class AssembleContextError {
        public String errorType;
        public String message;

        public AssembleContextError(String errorType, String message) {
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