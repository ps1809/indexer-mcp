package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.ArchitectureInsightsResponse;
import com.projectiq.mcp.analysis.service.ArchitectureInsightsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that analyzes repository architecture and provides deterministic
 * architecture insights. This tool examines the repository structure to identify
 * architectural layers, module relationships, dependency flow, and architectural
 * patterns to help AI coding agents understand the project's design.
 *
 * <p>The tool analyzes project modules, packages, controllers, services,
 * repositories, entities, DTOs, configurations, dependency direction, and
 * layer boundaries to produce a comprehensive architectural overview.</p>
 *
 * <p>This tool NEVER modifies repository files or generates code changes.
 * It only provides analysis and insights.</p>
 */
@Component
public class ArchitectureInsightsTool {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureInsightsTool.class);

    private final ArchitectureInsightsService architectureInsightsService;
    private final ObjectMapper objectMapper;

    public ArchitectureInsightsTool(ArchitectureInsightsService architectureInsightsService) {
        this.architectureInsightsService = architectureInsightsService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes the architecture of a repository and returns structured insights.
     *
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing architecture insights
     */
    @Tool(description = """
            Analyzes the architecture of a repository and provides deterministic architecture insights.
            
            This tool performs a comprehensive architectural analysis:
            1. Retrieves repository structure and package information
            2. Identifies architectural layers (Controller, Service, Repository, Entity, DTO, Configuration)
            3. Detects architectural style (Layered, MVC, Hexagonal, Microservice, etc.)
            4. Identifies module relationships and dependencies
            5. Determines dependency flow direction
            6. Detects cross-layer dependencies
            7. Identifies architectural patterns (Repository, Service Layer, Builder, Factory, Strategy, Observer)
            8. Highlights architectural strengths and potential concerns
            9. Provides confidence level based on data completeness
            
            Returns a structured response containing:
            - Repository Overview: Summary of repository structure
            - Architectural Style: Detected architectural style
            - Detected Layers: Identified architectural layers
            - Module Relationships: Relationships between packages/modules
            - Dependency Flow: Direction of dependency flow
            - Cross-Layer Dependencies: Dependencies that cross layer boundaries
            - Architectural Strengths: Detected architectural patterns and strengths
            - Potential Concerns: Areas that may need attention
            - Confidence Level: Low, Medium, or High based on data completeness
            
            This tool NEVER modifies repository files or generates code changes.
            It only provides analysis and insights.
            """)
    public String analyzeArchitecture(
            String repositoryName,
            String branch) {
        try {
            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

            logger.info("Executing architecture_insights tool for repository: {} branch: {}",
                    repositoryName, effectiveBranch);

            ArchitectureInsightsResponse response = architectureInsightsService.analyzeArchitecture(
                    repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing architecture: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(ArchitectureInsightsResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize architecture insights: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new ArchitectureInsightsError(errorType, message)
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
    private static class ArchitectureInsightsError {
        public String errorType;
        public String message;

        public ArchitectureInsightsError(String errorType, String message) {
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