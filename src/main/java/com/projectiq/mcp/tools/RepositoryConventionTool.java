package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.RepositoryConventionResponse;
import com.projectiq.mcp.analysis.service.RepositoryConventionAnalyzerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that analyzes repository conventions and provides deterministic
 * convention summaries. This tool examines the repository structure to identify
 * naming conventions, package organization, coding patterns, annotation usage,
 * and project standards to help AI coding agents align with existing conventions.
 *
 * <p>The tool analyzes package naming, class naming, method naming, DTO patterns,
 * entity patterns, service patterns, repository patterns, controller patterns,
 * REST endpoint conventions, annotation usage, configuration class organization,
 * test naming conventions, and module organization.</p>
 *
 * <p>This tool NEVER modifies repository files or generates code changes.
 * It only provides analysis and insights.</p>
 */
@Component
public class RepositoryConventionTool {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryConventionTool.class);

    private final RepositoryConventionAnalyzerService repositoryConventionAnalyzerService;
    private final ObjectMapper objectMapper;

    public RepositoryConventionTool(RepositoryConventionAnalyzerService repositoryConventionAnalyzerService) {
        this.repositoryConventionAnalyzerService = repositoryConventionAnalyzerService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes the conventions of a repository and returns structured convention summaries.
     *
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing convention analysis
     */
    @Tool(description = """
            Analyzes the conventions of a repository and provides deterministic convention summaries.
            
            This tool performs a comprehensive convention analysis:
            1. Retrieves repository structure and package information
            2. Analyzes naming conventions (package, class, method, DTO, entity, service, repository, controller)
            3. Analyzes package organization and module structure
            4. Identifies architectural style and layers
            5. Detects annotation usage patterns
            6. Identifies REST API endpoint conventions
            7. Analyzes testing conventions and patterns
            8. Provides project-specific observations
            9. Provides confidence level based on data completeness
            
            Returns a structured response containing:
            - Repository Overview: Summary of repository structure
            - Naming Conventions: Detected naming patterns for all class types
            - Package Conventions: Module organization and package structure
            - Architectural Conventions: Detected layers and style
            - Annotation Conventions: Common annotations in use
            - REST API Conventions: Endpoint naming and HTTP method usage
            - Testing Conventions: Test framework and naming patterns
            - Project-Specific Observations: Notable characteristics
            - Confidence Level: Low, Medium, or High based on data completeness
            
            This tool NEVER modifies repository files or generates code changes.
            It only provides analysis and insights.
            """)
    public String analyzeConventions(
            String repositoryName,
            String branch) {
        try {
            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

            logger.info("Executing repository_conventions tool for repository: {} branch: {}",
                    repositoryName, effectiveBranch);

            RepositoryConventionResponse response = repositoryConventionAnalyzerService.analyzeConventions(
                    repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing conventions: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(RepositoryConventionResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize convention analysis: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new RepositoryConventionError(errorType, message)
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
    private static class RepositoryConventionError {
        public String errorType;
        public String message;

        public RepositoryConventionError(String errorType, String message) {
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