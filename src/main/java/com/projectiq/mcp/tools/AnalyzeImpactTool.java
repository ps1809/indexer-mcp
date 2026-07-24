package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.ImpactAnalysisResponse;
import com.projectiq.mcp.analysis.service.ImpactAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that analyzes the potential impact of a proposed development task
 * on the repository. Invokes task analysis, assembles repository context,
 * and produces a structured impact report identifying affected components,
 * scope, risks, and dependencies.
 *
 * <p>This tool helps AI coding agents understand the scope of a modification
 * before implementation, reducing unintended side effects and unnecessary
 * repository exploration.</p>
 */
@Component
public class AnalyzeImpactTool {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzeImpactTool.class);

    private final ImpactAnalysisService impactAnalysisService;
    private final ObjectMapper objectMapper;

    public AnalyzeImpactTool(ImpactAnalysisService impactAnalysisService) {
        this.impactAnalysisService = impactAnalysisService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes the potential impact of a proposed development task on the repository.
     *
     * @param task           Natural language development request (e.g., "Add pagination to UserController") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing the impact analysis report
     */
    @Tool(description = """
            Analyzes the potential impact of a proposed development task on the repository.
            
            This tool performs a complete impact analysis workflow:
            1. Analyzes the task to determine task type, entities, and complexity
            2. Assembles relevant repository context
            3. Identifies directly affected components (classes, methods, REST APIs, etc.)
            4. Identifies indirectly affected components (dependents, related services, etc.)
            5. Estimates dependency impact
            6. Estimates implementation scope (Small, Medium, Large)
            7. Estimates testing scope (Small, Medium, Large)
            8. Identifies potential risks with severity levels and mitigations
            9. Provides confidence level for the analysis
            
            Returns a structured impact report containing:
            - Original Task: The input task description
            - Task Type: The detected development activity type
            - Primary Targets: The main components targeted by the task
            - Directly Affected Components: Components directly requiring changes
            - Indirectly Affected Components: Components that may need updates
            - Dependency Impact: Impact on project dependencies
            - Estimated Implementation Scope: Size of implementation effort
            - Estimated Testing Scope: Size of testing effort required
            - Potential Risks: Identified risks with severity and mitigations
            - Confidence Level: Confidence in the analysis results
            
            Useful for AI coding agents to understand change scope and
            potential risks before implementation begins.
            """)
    public String analyzeImpact(
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

            logger.info("Executing analyze_impact tool for task: {} in repository: {} branch: {}",
                    task, repositoryName, effectiveBranch);

            ImpactAnalysisResponse response = impactAnalysisService.analyzeImpact(
                    task.trim(), repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing impact: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(ImpactAnalysisResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize impact response: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new AnalyzeImpactError(errorType, message)
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
    private static class AnalyzeImpactError {
        public String errorType;
        public String message;

        public AnalyzeImpactError(String errorType, String message) {
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