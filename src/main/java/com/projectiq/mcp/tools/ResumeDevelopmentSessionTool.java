package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projectiq.mcp.session.dto.DevelopmentSession;
import com.projectiq.mcp.session.service.DevelopmentSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that resumes an interrupted development session.
 * Restores the session to IN_PROGRESS status and rebuilds the session summary
 * so an AI coding agent can continue the workflow from where it left off.
 */
@Component
public class ResumeDevelopmentSessionTool {

    private static final Logger logger = LoggerFactory.getLogger(ResumeDevelopmentSessionTool.class);

    private final DevelopmentSessionService sessionService;
    private final ObjectMapper objectMapper;

    public ResumeDevelopmentSessionTool(DevelopmentSessionService sessionService) {
        this.sessionService = sessionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Resumes an interrupted development session by its session identifier.
     * The session status is set to IN_PROGRESS and its summary is rebuilt
     * with the current state, allowing the agent to continue the workflow.
     *
     * @param sessionId The unique identifier of the session to resume (required)
     * @return A structured JSON response containing the resumed session information
     */
    @Tool(description = """
            Resumes an interrupted development session.
            
            Sets the session status back to IN_PROGRESS and rebuilds the session summary
            with the current workflow state, completed stages, pending stages, and all
            preserved context. This allows an AI coding agent to seamlessly continue
            a development task from where it left off.
            
            Required parameters:
            - sessionId: The unique identifier of the session to resume
            
            Cannot resume sessions that are already COMPLETED or ARCHIVED.
            """)
    public String resumeDevelopmentSession(String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Session ID is required");
            }

            logger.info("Executing resume_development_session tool for session: {}", sessionId);

            DevelopmentSession session = sessionService.resumeSession(sessionId.trim());
            return serializeSession(session);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (IllegalStateException e) {
            return createErrorResponse("INVALID_STATE", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error resuming session: {}", e.getMessage(), e);
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