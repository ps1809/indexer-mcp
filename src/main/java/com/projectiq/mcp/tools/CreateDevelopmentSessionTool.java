package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projectiq.mcp.orchestration.dto.WorkflowType;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tool that creates a new intelligent development session.
 * Sessions preserve workflow progress, execution context, collected repository
 * intelligence, validation results, recommendations, and implementation history
 * for AI coding agents to continue complex development tasks across multiple
 * MCP interactions.
 */
@Component
public class CreateDevelopmentSessionTool {

    private static final Logger logger = LoggerFactory.getLogger(CreateDevelopmentSessionTool.class);

    private final DevelopmentSessionService sessionService;
    private final ObjectMapper objectMapper;

    public CreateDevelopmentSessionTool(DevelopmentSessionService sessionService) {
        this.sessionService = sessionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Creates a new development session for an AI coding agent.
     * The session preserves workflow state, execution progress, assembled context,
     * validation results, recommendations, and implementation history so the agent
     * can seamlessly continue complex development tasks across multiple interactions.
     *
     * @param repositoryId     Identifier of the target repository (required)
     * @param developerRequest The original developer request that defines the workflow (required)
     * @param workflowType     Type of workflow (e.g., "FEATURE_IMPLEMENTATION", "BUG_FIX", "REFACTORING") (optional)
     * @param pendingStages    Comma-separated list of pending workflow stages (optional)
     * @return A structured JSON response containing the created session information
     */
    @Tool(description = """
            Creates a new intelligent development session for an AI coding agent.
            
            The session preserves the complete state of an AI-assisted development session,
            including workflow progress, execution context, collected repository intelligence,
            validation results, recommendations, and implementation history.
            
            Required parameters:
            - repositoryId: The identifier of the target repository
            - developerRequest: The original developer request
            
            Optional parameters:
            - workflowType: Type of workflow (FEATURE_IMPLEMENTATION, BUG_FIX, REFACTORING, etc.)
            - pendingStages: Comma-separated list of pending workflow stages
            
            Returns a structured JSON response containing:
            - Session identifier
            - Repository identifier
            - Developer request
            - Workflow type and current stage
            - Session status and progress
            - Completed and pending stages
            - Execution history
            - Session timestamps
            - Session summary with next steps
            
            The session is stored in-memory and can be retrieved, updated, resumed,
            completed, or archived using the corresponding session tools.
            """)
    public String createDevelopmentSession(
            String repositoryId,
            String developerRequest,
            String workflowType,
            String pendingStages) {
        try {
            // Validate required parameters
            if (repositoryId == null || repositoryId.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository ID is required");
            }

            if (developerRequest == null || developerRequest.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Developer request is required");
            }

            logger.info("Executing create_development_session tool for repository: {}", repositoryId);

            // Parse workflow type
            WorkflowType parsedWorkflowType = null;
            if (workflowType != null && !workflowType.trim().isEmpty()) {
                try {
                    parsedWorkflowType = WorkflowType.valueOf(workflowType.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return createErrorResponse("INVALID_ARGUMENT",
                            "Invalid workflow type: " + workflowType
                            + ". Valid types: FEATURE_IMPLEMENTATION, BUG_FIX, REFACTORING, "
                            + "REST_API_ENHANCEMENT, CONFIGURATION_CHANGE, DOCUMENTATION_UPDATE, "
                            + "TEST_IMPROVEMENT, REPOSITORY_ANALYSIS, UNKNOWN");
                }
            }

            // Parse pending stages
            List<String> parsedPendingStages = null;
            if (pendingStages != null && !pendingStages.trim().isEmpty()) {
                parsedPendingStages = List.of(pendingStages.split(","));
                parsedPendingStages = parsedPendingStages.stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }

            DevelopmentSession session = sessionService.createSession(
                    repositoryId.trim(),
                    developerRequest.trim(),
                    parsedWorkflowType,
                    parsedPendingStages);

            return serializeSession(session);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating development session: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeSession(DevelopmentSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize session: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new SessionToolError(errorType, message)
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

    private static class SessionToolError {
        public String errorType;
        public String message;

        public SessionToolError(String errorType, String message) {
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