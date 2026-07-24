package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.CrossRepositoryAnalysisResponse;
import com.projectiq.mcp.analysis.service.CrossRepositoryAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tool that performs intelligent cross-repository analysis.
 * Compares multiple indexed repositories to identify shared architecture,
 * reusable components, common patterns, dependency differences, and
 * potential code reuse opportunities.
 *
 * <p>This tool accepts multiple repository names and returns a deterministic
 * comparison report covering architecture, dependencies, APIs, conventions,
 * and reuse opportunities across the specified repositories.</p>
 *
 * <p>This tool NEVER modifies repository files or generates code changes.
 * It only provides analysis and insights.</p>
 */
@Component
public class CrossRepositoryAnalysisTool {

    private static final Logger logger = LoggerFactory.getLogger(CrossRepositoryAnalysisTool.class);

    private final CrossRepositoryAnalysisService crossRepositoryAnalysisService;
    private final ObjectMapper objectMapper;

    public CrossRepositoryAnalysisTool(CrossRepositoryAnalysisService crossRepositoryAnalysisService) {
        this.crossRepositoryAnalysisService = crossRepositoryAnalysisService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Performs cross-repository analysis on the given list of repositories.
     *
     * @param repositories Comma-separated list of repository names to analyze (required)
     * @return A structured JSON response containing the cross-repository comparison report
     */
    @Tool(description = """
            Performs intelligent cross-repository analysis on multiple indexed repositories.
            
            This tool compares multiple repositories and identifies:
            1. Shared architectural patterns and styles
            2. Common components and package structures
            3. Similar REST API endpoints across repositories
            4. Common and unique dependencies with version differences
            5. Convention similarities across repositories
            6. Reuse opportunities (shared libraries, extractable services, shared config, reusable APIs)
            7. Architectural differences and gaps
            8. Risk assessment including incompatibilities and integration challenges
            
            Accepts a comma-separated list of repository names (e.g., "repo1,repo2,repo3").
            
            Returns a structured response containing:
            - Repository Summaries: Overview of each analyzed repository
            - Common Architecture: Shared architectural patterns and similarity score
            - Shared Components: Common classes, package prefixes, and component matches
            - Similar APIs: REST API endpoints found in multiple repositories
            - Dependency Comparison: Common/unique dependencies and version differences
            - Convention Comparison: Shared conventions across all categories
            - Reuse Opportunities: Libraries, services, config, and API contracts that could be shared
            - Architectural Differences: Variances in architectural approaches
            - Risk Assessment: Potential risks, incompatibilities, and integration challenges
            
            This tool NEVER modifies repository files or generates code changes.
            It only provides analysis and insights.
            """)
    public String analyzeCrossRepository(String repositories) {
        try {
            if (repositories == null || repositories.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository list is required");
            }

            // Parse comma-separated repository names
            List<String> repoList = List.of(repositories.split(","));
            repoList = repoList.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (repoList.isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "At least one repository name is required");
            }

            logger.info("Executing analyze_cross_repository tool for repositories: {}", repoList);

            CrossRepositoryAnalysisResponse response = crossRepositoryAnalysisService
                    .analyzeCrossRepository(repoList);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in cross-repository analysis: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(CrossRepositoryAnalysisResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize cross-repository analysis: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new CrossRepositoryError(errorType, message)
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
    private static class CrossRepositoryError {
        public String errorType;
        public String message;

        public CrossRepositoryError(String errorType, String message) {
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