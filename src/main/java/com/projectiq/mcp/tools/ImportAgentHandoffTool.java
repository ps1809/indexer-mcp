package com.projectiq.mcp.tools;

import com.projectiq.mcp.handoff.service.AgentHandoffService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that imports an AI Agent Handoff Package and restores the
 * development session. The handoff package is validated for integrity,
 * version compatibility, and repository match before restoring the full
 * session state including workflow progress, execution history, validation
 * results, recommendations, and context.
 *
 * <p>This enables any compatible AI coding agent to immediately continue
 * work from where the previous agent left off.</p>
 */
@Component
public class ImportAgentHandoffTool {

    private static final Logger logger = LoggerFactory.getLogger(ImportAgentHandoffTool.class);

    private final AgentHandoffService handoffService;

    public ImportAgentHandoffTool(AgentHandoffService handoffService) {
        this.handoffService = handoffService;
    }

    /**
     * Imports an agent handoff package and restores the development session.
     *
     * @param handoffPackage The serialized JSON handoff package (required)
     * @return A structured JSON response containing the restored development session
     */
    @Tool(description = """
            Imports an AI Agent Handoff Package and restores the development session.
            
            The handoff package is validated for:
            - Package version compatibility
            - Integrity hash verification
            - Repository ID match (if session already exists)
            - Complete field presence
            
            After validation, the following session state is restored:
            - Workflow progress and current stage
            - Completed and pending stages
            - Execution history (immutable, append only)
            - Repository context and intelligence
            - Validation and recommendation reports
            - Session summary
            
            Required parameters:
            - handoffPackage: The complete serialized JSON handoff package
            """)
    public String importAgentHandoff(String handoffPackage) {
        try {
            if (handoffPackage == null || handoffPackage.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Handoff package is required");
            }

            logger.info("Executing import_agent_handoff tool");

            return handoffService.importHandoffPackage(handoffPackage.trim());

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.contains("integrity hash mismatch")) {
                return createErrorResponse("CORRUPTED_PACKAGE", message);
            }
            if (message.contains("Version incompatibility")) {
                return createErrorResponse("VERSION_INCOMPATIBLE", message);
            }
            if (message.contains("Repository mismatch")) {
                return createErrorResponse("REPOSITORY_MISMATCH", message);
            }
            if (message.contains("missing session ID") || message.contains("missing repository ID")) {
                return createErrorResponse("INVALID_PACKAGE", message);
            }
            if (message.contains("Invalid handoff package")) {
                return createErrorResponse("INVALID_PACKAGE", message);
            }
            return createErrorResponse("INVALID_ARGUMENT", message);
        } catch (Exception e) {
            logger.error("Unexpected error importing handoff package: {}", e.getMessage(), e);
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