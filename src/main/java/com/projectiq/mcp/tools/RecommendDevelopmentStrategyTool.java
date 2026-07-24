package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.strategy.dto.DevelopmentStrategyResponse;
import com.projectiq.mcp.strategy.service.DevelopmentStrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that evaluates multiple implementation approaches for a requested
 * feature or change and returns deterministic strategy recommendations.
 *
 * <p>This tool compares alternative development paths using repository
 * intelligence, architecture analysis, dependency analysis, workflow planning,
 * and risk assessment to recommend the safest and most maintainable
 * implementation strategy without generating code.</p>
 */
@Component
public class RecommendDevelopmentStrategyTool {

    private static final Logger logger = LoggerFactory.getLogger(RecommendDevelopmentStrategyTool.class);

    private final DevelopmentStrategyService developmentStrategyService;
    private final ObjectMapper objectMapper;

    public RecommendDevelopmentStrategyTool(DevelopmentStrategyService developmentStrategyService) {
        this.developmentStrategyService = developmentStrategyService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Evaluates multiple implementation strategies for a development request
     * and returns a deterministic strategy recommendation report.
     *
     * @param requestDescription Description of the feature or change to implement (required)
     * @param repositoryName     Name of the target repository (required)
     * @return A structured JSON response containing strategy evaluations, comparison, and recommendation
     */
    @Tool(description = """
            Evaluates multiple implementation strategies for a development request and returns a deterministic strategy recommendation report.
            
            This tool analyzes the request and evaluates 8 strategy categories:
            1. Extend Existing Component - Minimizes new code by extending existing components
            2. Create New Component - Creates a dedicated component for the functionality
            3. Refactor Then Implement - Refactors existing code first, then implements
            4. Modular Implementation - Self-contained module with defined interfaces
            5. Incremental Enhancement - Small, iterative steps across the codebase
            6. Configuration-Based Solution - Behavior driven by configuration changes
            7. Service Layer Enhancement - Extends service layer with new orchestration
            8. API-First Implementation - Contract-driven API design then implementation
            
            Each strategy is scored across 8 dimensions:
            - Complexity (1-10): How complex is the implementation?
            - Repository Impact (1-10): How much does it affect the repository?
            - Dependency Impact (1-10): How much does it affect dependencies?
            - Testing Effort (1-10): How much testing is required?
            - Architectural Consistency (1-10): How well does it align with architecture?
            - Maintainability (1-10): How maintainable is the result?
            - Technical Risk (1-10): How risky is the approach?
            - Sustainability (1-10): How sustainable long-term?
            
            Returns a strategy report containing:
            - Proposed implementation strategies with scores
            - Comparative analysis across all dimensions
            - Pros and cons for each strategy
            - Repository impact assessment
            - Estimated effort for each strategy
            - Risk assessment for each strategy
            - Recommended strategy with decision rationale
            
            This tool never generates or modifies source code.
            Recommendations are deterministic and based purely on rule-based analysis.
            """)
    public String recommendDevelopmentStrategy(
            String requestDescription,
            String repositoryName) {
        try {
            // Validate required parameters
            if (requestDescription == null || requestDescription.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Request description is required");
            }

            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            logger.info("Executing recommend_development_strategy tool for request '{}' in repository '{}'",
                    requestDescription, repositoryName);

            DevelopmentStrategyResponse response = developmentStrategyService.evaluateStrategies(
                    requestDescription.trim(),
                    repositoryName.trim());

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error recommending development strategy: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(DevelopmentStrategyResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize strategy response: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new StrategyToolError(errorType, message)
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
    private static class StrategyToolError {
        public String errorType;
        public String message;

        public StrategyToolError(String errorType, String message) {
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