package com.projectiq.mcp.tools;

import com.projectiq.mcp.handoff.service.AgentHandoffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that exports a complete AI Agent Handoff Package for a
 * development session. The handoff package captures the complete session
 * state including workflow progress, repository intelligence, execution
 * history, validation results, recommendations, and readiness assessment
 * into a deterministic, self-contained artifact.
 *
 * <p>Any compatible AI coding agent can use the exported package to
 * immediately continue work without re-analyzing the repository or
 * rebuilding execution context.</p>
 */
@Component
public class ExportAgentHandoffTool {

    private static final Logger logger = LoggerFactory.getLogger(ExportAgentHandoffTool.class);

    private final AgentHandoffService handoffService;

    public ExportAgentHandoffTool(AgentHandoffService handoffService) {
        this.handoffService = handoffService;
    }

    /**
     * Exports a complete agent handoff package for the specified session.
     *
     * @param sessionId The unique identifier of the session to export (required)
     * @return A structured JSON response containing the handoff package with
     *         complete session state, workflow progress, execution history,
     *         validation results, recommendations, risks, and suggested next actions
     */
    @Tool(description = """
            Exports a complete AI Agent Handoff Package for a development session.
            
            The handoff package captures the complete session state including:
            - Session identifier and repository information
            - Original developer request
            - Current workflow, completed and pending stages
            - Repository context and intelligence
            - Validation report summary
            - Recommendation report summary
            - Readiness assessment
            - Execution history (immutable)
            - Outstanding risks
            - Suggested next actions
            - Integrity hash for verification
            
            The exported package is self-contained and can be imported by any
            compatible AI coding agent to continue work immediately.
            
            Required parameters:
            - sessionId: The unique identifier of the session to export
            """)
    public String exportAgentHandoff(String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Session ID is required");
            }

            logger.info("Executing export_agent_handoff tool for session: {}", sessionId);

            return handoffService.exportHandoffPackage(sessionId.trim());

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error exporting handoff package: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        return "{\"errorType\":\"" + escapeJson(errorType)
                + "\",\"message\":\"" + escapeJson(message) + "\"}";
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
}