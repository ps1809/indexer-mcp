package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.recommendation.dto.RecommendationReport;
import com.projectiq.mcp.recommendation.service.RecommendationEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that generates deterministic, prioritized implementation recommendations.
 * Analyzes the complete workflow, validation results, repository intelligence,
 * architecture insights, and conventions to produce actionable guidance for
 * AI coding agents before they begin implementation.
 *
 * <p>This tool does not generate or modify any source code. It produces recommendations
 * based purely on deterministic rules without any AI/LLM involvement.</p>
 */
@Component
public class GenerateRecommendationsTool {

    private static final Logger logger = LoggerFactory.getLogger(GenerateRecommendationsTool.class);

    private final RecommendationEngineService recommendationEngineService;
    private final ObjectMapper objectMapper;

    public GenerateRecommendationsTool(RecommendationEngineService recommendationEngineService) {
        this.recommendationEngineService = recommendationEngineService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Generates deterministic, prioritized implementation recommendations by analyzing
     * the complete workflow, validation results, repository intelligence, architecture
     * insights, and conventions. Provides actionable guidance for AI coding agents
     * before development begins.
     *
     * @param workflowName     Name of the workflow to generate recommendations for (required)
     * @param workflowType     Type of workflow (e.g., "Feature Implementation", "Bug Fix") (optional)
     * @param originalRequest  Original developer request that defines the workflow (optional)
     * @param repositoryName   Name of the target repository (required)
     * @param branch           Branch name for analysis (optional, defaults to "main")
     * @return A structured JSON response containing the recommendation report
     */
    @Tool(description = """
            Generates deterministic, prioritized implementation recommendations for an AI coding agent.
            
            This tool analyzes 7 phases of intelligence to produce comprehensive recommendations:
            1. Workflow Analysis - Validates workflow completeness and type
            2. Validation Analysis - Reviews validation findings and readiness score
            3. Repository Intelligence - Analyzes health, maintainability, dependencies
            4. Architecture Insights - Reviews architectural style, layers, and concerns
            5. Convention Analysis - Checks naming conventions and coding standards
            6. Test Impact Analysis - Identifies affected tests and coverage gaps
            7. Implementation Planning - Reviews execution plan, risks, and prerequisites
            
            Returns a recommendation report containing:
            - Executive summary with key findings
            - Prioritized recommendations (Critical, High, Medium, Low)
            - Implementation advice and testing recommendations
            - Architectural guidance and repository best practices
            - Risk mitigation suggestions
            - Confidence score (0-100)
            - Summary of all recommendations by priority and category
            
            Recommendations are deterministic, stable, and free of duplicates.
            This tool never generates or modifies source code.
            """)
    public String generateRecommendations(
            String workflowName,
            String workflowType,
            String originalRequest,
            String repositoryName,
            String branch) {
        try {
            // Validate required parameters
            if (workflowName == null || workflowName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Workflow name is required");
            }

            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            logger.info("Executing generate_recommendations tool for workflow: {} in repository: {}",
                    workflowName, repositoryName);

            RecommendationReport report = recommendationEngineService.generateRecommendations(
                    workflowName.trim(),
                    workflowType != null ? workflowType.trim() : null,
                    originalRequest != null ? originalRequest.trim() : null,
                    repositoryName.trim(),
                    branch != null ? branch.trim() : null);

            return serializeReport(report);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating recommendations: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeReport(RecommendationReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize recommendation report: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new RecommendationToolError(errorType, message)
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
    private static class RecommendationToolError {
        public String errorType;
        public String message;

        public RecommendationToolError(String errorType, String message) {
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