package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.orchestration.dto.WorkflowResult;
import com.projectiq.mcp.orchestration.service.WorkflowOrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that orchestrates intelligent workflows by coordinating existing
 * repository intelligence services into a single deterministic execution pipeline.
 *
 * <p>This tool accepts a natural language developer task, builds an execution
 * workflow, executes it by coordinating existing services, and returns a
 * complete workflow report.</p>
 */
@Component
public class OrchestrateWorkflowTool {

    private static final Logger logger = LoggerFactory.getLogger(OrchestrateWorkflowTool.class);

    private final WorkflowOrchestratorService workflowOrchestratorService;
    private final ObjectMapper objectMapper;

    public OrchestrateWorkflowTool(WorkflowOrchestratorService workflowOrchestratorService) {
        this.workflowOrchestratorService = workflowOrchestratorService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Orchestrates a complete developer workflow by coordinating existing
     * repository intelligence services into a single deterministic execution pipeline.
     *
     * @param request        Natural language developer task (e.g., "Add pagination to UserController") (required)
     * @param repositoryName The repository name to analyze (required)
     * @param branch         The git branch to use (optional, defaults to "main")
     * @return A structured JSON response containing the complete workflow report
     */
    @Tool(description = """
            Orchestrates a complete developer workflow by coordinating existing
            repository intelligence services into a single deterministic execution pipeline.
            
            Returns a structured workflow report including:
            - Original Request: The input developer task
            - Workflow Type: Classification of the workflow (Feature Implementation, Bug Fix, etc.)
            - Execution Plan: Ordered list of steps to be executed
            - Completed Steps: Steps that executed successfully
            - Skipped Steps: Steps that were skipped (duplicates)
            - Failed Steps: Steps that encountered errors
            - Repository Insights: Architecture, conventions, health information collected
            - Risks Identified: Potential risks discovered during analysis
            - Suggested Next Actions: Recommended follow-up actions
            - Total Execution Duration: Time taken for the entire workflow
            - Execution Status: Overall status of the workflow execution
            - Summary: Human-readable summary of the workflow results
            
            This tool performs deterministic workflow orchestration only:
            - No AI models or LLMs are used
            - No code generation is performed
            - No repository modification occurs
            - No Git operations are executed
            - Workflow execution is sequential and deterministic
            """)
    public String orchestrateWorkflow(String request, String repositoryName, String branch) {
        try {
            if (request == null || request.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Developer request is required");
            }
            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            logger.info("Executing orchestrate_workflow tool for: {} in repository: {}", request, repositoryName);

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
            WorkflowResult result = workflowOrchestratorService.orchestrate(
                    request.trim(), repositoryName.trim(), effectiveBranch);

            return serializeResponse(result);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error orchestrating workflow: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(WorkflowResult response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize workflow result: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new OrchestrateWorkflowError(errorType, message)
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
    private static class OrchestrateWorkflowError {
        public String errorType;
        public String message;

        public OrchestrateWorkflowError(String errorType, String message) {
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