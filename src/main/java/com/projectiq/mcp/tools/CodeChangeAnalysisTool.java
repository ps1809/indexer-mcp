package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.CodeChangeAnalysisResponse;
import com.projectiq.mcp.analysis.service.CodeChangeAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that performs Intelligent Code Change Analysis for proposed source
 * code modifications. Invokes task analysis, context assembly, impact analysis,
 * and implementation planning to produce a comprehensive change impact report.
 *
 * <p>The report includes the proposed change summary, impacted files, classes,
 * methods, REST APIs, dependency changes, testing recommendations, risk
 * assessment, and suggested implementation order.</p>
 *
 * <p>This tool NEVER generates code, modifies the repository, performs git
 * operations, or uses any AI/LLM reasoning. It only predicts the impact of
 * proposed changes before implementation begins.</p>
 */
@Component
public class CodeChangeAnalysisTool {

    private static final Logger logger = LoggerFactory.getLogger(CodeChangeAnalysisTool.class);

    private final CodeChangeAnalysisService codeChangeAnalysisService;
    private final ObjectMapper objectMapper;

    public CodeChangeAnalysisTool(CodeChangeAnalysisService codeChangeAnalysisService) {
        this.codeChangeAnalysisService = codeChangeAnalysisService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes a proposed code change and produces a comprehensive change
     * impact report. Performs repository-wide impact analysis and returns a
     * deterministic change impact report.
     *
     * @param proposedChange Description of the proposed code change (e.g., "Add pagination to UserController") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing the change impact analysis report
     */
    @Tool(description = """
            Analyzes a proposed code change and produces a comprehensive change impact report.
            
            This tool performs a complete code change analysis workflow:
            1. Analyzes the proposed change to determine change type, entities, and complexity
            2. Assembles relevant repository context
            3. Analyzes the impact of the proposed change
            4. Generates an implementation plan
            5. Produces a structured change impact report
            
            Returns a structured change impact report containing:
            - Proposed Change Summary: Human-readable summary of the proposed change
            - Impacted Files: Files that will be affected by the change
            - Impacted Classes: Classes that will be affected by the change
            - Impacted Methods: Methods that will be affected by the change
            - Impacted REST APIs: REST API endpoints that will be affected
            - Dependency Changes: Required dependency modifications
            - Testing Recommendations: Recommended testing strategy
            - Risk Assessment: Identified risks and mitigations
            - Suggested Implementation Order: Recommended order of implementation
            
            This tool NEVER generates code, modifies the repository, performs git operations,
            or uses any AI/LLM reasoning. It only predicts the impact of proposed changes
            before implementation begins.
            """)
    public String analyzeCodeChange(
            String proposedChange,
            String repositoryName,
            String branch) {
        try {
            if (proposedChange == null || proposedChange.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Proposed change description is required");
            }

            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

            logger.info("Executing analyze_code_change tool for change: {} in repository: {} branch: {}",
                    proposedChange, repositoryName, effectiveBranch);

            CodeChangeAnalysisResponse response = codeChangeAnalysisService.analyzeCodeChange(
                    proposedChange.trim(), repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing code change: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(CodeChangeAnalysisResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize code change analysis: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new CodeChangeError(errorType, message)
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
    private static class CodeChangeError {
        public String errorType;
        public String message;

        public CodeChangeError(String errorType, String message) {
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