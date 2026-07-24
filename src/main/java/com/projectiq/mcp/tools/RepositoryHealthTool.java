package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.RepositoryHealthResponse;
import com.projectiq.mcp.analysis.service.RepositoryHealthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that analyzes repository health and provides deterministic health
 * metrics. This tool evaluates repository maintainability, complexity,
 * architecture consistency, dependency health, testing maturity, and
 * documentation maturity to help AI coding agents understand the overall
 * quality and maintainability of a repository before implementation begins.
 *
 * <p>The tool analyzes package organization, class distribution,
 * Controller-Service-Repository balance, configuration complexity, dependency
 * density, REST API distribution, test coverage, and documentation coverage
 * to produce a comprehensive health report.</p>
 *
 * <p>This tool NEVER modifies repository files or generates code changes.
 * All outputs are deterministic and based solely on indexed metadata.</p>
 */
@Component
public class RepositoryHealthTool {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryHealthTool.class);

    private final RepositoryHealthService repositoryHealthService;
    private final ObjectMapper objectMapper;

    public RepositoryHealthTool(RepositoryHealthService repositoryHealthService) {
        this.repositoryHealthService = repositoryHealthService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes the health of a repository and returns structured health metrics.
     *
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing repository health analysis
     */
    @Tool(description = """
            Analyzes the health of a repository and provides deterministic health metrics.
            
            This tool performs a comprehensive repository health analysis:
            1. Evaluates package organization quality
            2. Analyzes class distribution across packages
            3. Assesses Controller-Service-Repository balance
            4. Reviews configuration complexity
            5. Measures dependency density
            6. Analyzes REST API distribution
            7. Evaluates test coverage availability (based on indexed tests)
            8. Reviews documentation coverage (based on indexed artifacts)
            9. Assesses repository size statistics
            
            Returns a structured response containing:
            - Repository Overview: Summary of repository structure
            - Health Score: Overall health score (0-100)
            - Maintainability Rating: Excellent, Good, Fair, Poor, or Very Poor
            - Complexity Rating: Low, Moderate, High, or Very High
            - Architecture Consistency: Consistent, Mostly Consistent, Inconsistent, or Unstructured
            - Dependency Health: Healthy, Moderate, Concerning, or Critical
            - Testing Maturity: Mature, Developing, Minimal, Limited, or None
            - Documentation Maturity: Comprehensive, Adequate, Minimal, Limited, or None
            - Maintainability Summary: Human-readable summary
            - Strengths: Identified positive aspects
            - Observations: Notable observations about the repository
            - Potential Risks: Areas that may require attention
            - Suggested Review Areas: Specific components to review
            - Confidence Level: Low, Medium, or High based on data completeness
            
            This tool NEVER modifies repository files or generates code changes.
            It only provides analysis and insights based on indexed metadata.
            """)
    public String analyzeHealth(
            String repositoryName,
            String branch) {
        try {
            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

            logger.info("Executing repository_health tool for repository: {} branch: {}",
                    repositoryName, effectiveBranch);

            RepositoryHealthResponse response = repositoryHealthService.analyzeHealth(
                    repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing repository health: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(RepositoryHealthResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize repository health: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new RepositoryHealthError(errorType, message)
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
    private static class RepositoryHealthError {
        public String errorType;
        public String message;

        public RepositoryHealthError(String errorType, String message) {
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