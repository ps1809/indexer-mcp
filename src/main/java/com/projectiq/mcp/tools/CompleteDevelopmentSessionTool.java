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
 * MCP Tool that completes a development session.
 * Sets the session status to COMPLETED, progress to 100%, and records the
 * completion timestamp. Once completed, a session cannot be modified or resumed.
 */
@Component
public class CompleteDevelopmentSessionTool {

    private static final Logger logger = LoggerFactory.getLogger(CompleteDevelopmentSessionTool.class);

    private final DevelopmentSessionService sessionService;
    private final ObjectMapper objectMapper;

    public CompleteDevelopmentSessionTool(DevelopmentSessionService sessionService) {
        this.sessionService = sessionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Completes a development session by its session identifier.
     * The session status is set to COMPLETED, progress is set to 100%,
     * and the completion timestamp is recorded.
     *
     * @param sessionId The unique identifier of the session to complete (required)
     * @return A structured JSON response containing the completed session
     */
    @Tool(description = """
            Completes a development session.
            
            Sets the session status to COMPLETED, progress to 100%, and records
            the completion timestamp. Once completed, a session cannot be modified
            or resumed. The final session summary is generated with all preserved
            context, validation results, and recommendations.
            
            Required parameters:
            - sessionId: The unique identifier of the session to complete
            
            Cannot complete a session that is already ARCHIVED.
            """)
    public String completeDevelopmentSession(String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Session ID is required");
            }

            logger.info("Executing complete_development_session tool for session: {}", sessionId);

            DevelopmentSession session = sessionService.completeSession(sessionId.trim());
            return serializeSession(session);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (IllegalStateException e) {
            return createErrorResponse("INVALID_STATE", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error completing session: {}", e.getMessage(), e);
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