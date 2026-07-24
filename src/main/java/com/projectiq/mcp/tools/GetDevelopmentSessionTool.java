package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.session.dto.DevelopmentSession.SessionSummary;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that retrieves an existing development session.
 * Returns the full session state including workflow progress, execution history,
 * context package, validation results, recommendations, and session summary.
 */
@Component
public class GetDevelopmentSessionTool {

    private static final Logger logger = LoggerFactory.getLogger(GetDevelopmentSessionTool.class);

    private final DevelopmentSessionService sessionService;
    private final ObjectMapper objectMapper;

    public GetDevelopmentSessionTool(DevelopmentSessionService sessionService) {
        this.sessionService = sessionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Retrieves an existing development session by its session identifier.
     * Returns the complete session state including all preserved information.
     *
     * @param sessionId The unique identifier of the session to retrieve (required)
     * @return A structured JSON response containing the complete session information
     */
    @Tool(description = """
            Retrieves an existing development session by its session identifier.
            
            Returns the complete session state including:
            - Session identifier, repository, developer request
            - Workflow type and current execution stage
            - Session status and progress
            - Completed and pending stages
            - Execution history (immutable)
            - Context package (if assembled)
            - Validation report (if performed)
            - Recommendation report (if generated)
            - Session summary with readiness status and suggested next step
            
            Use this tool to inspect the current state of a development session
            and determine what work has been completed and what remains pending.
            """)
    public String getDevelopmentSession(String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Session ID is required");
            }

            logger.info("Executing get_development_session tool for session: {}", sessionId);

            DevelopmentSession session = sessionService.loadSession(sessionId.trim());
            return serializeSession(session);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error retrieving session: {}", e.getMessage(), e);
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