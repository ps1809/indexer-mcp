package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.RepositoryEvolutionAnalysisResponse;
import com.projectiq.mcp.analysis.service.RepositoryEvolutionAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that analyzes the long-term repository evolution impact of proposed
 * features or architectural changes. Using indexed repository intelligence,
 * dependency graphs, architecture insights, and existing development patterns,
 * this tool produces deterministic evolution reports to help AI coding agents
 * make decisions that preserve repository maintainability and architectural
 * consistency.
 *
 * <p>The tool evaluates architecture evolution, package growth, module expansion,
 * dependency evolution, convention consistency, maintainability, technical debt
 * indicators, and scalability readiness to produce a comprehensive evolution
 * report with a repository evolution score.</p>
 *
 * <p>This tool NEVER modifies repository files or generates code changes.
 * It only provides analysis and insights.</p>
 */
@Component
public class AnalyzeRepositoryEvolutionTool {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzeRepositoryEvolutionTool.class);

    private final RepositoryEvolutionAnalysisService repositoryEvolutionAnalysisService;
    private final ObjectMapper objectMapper;

    public AnalyzeRepositoryEvolutionTool(RepositoryEvolutionAnalysisService repositoryEvolutionAnalysisService) {
        this.repositoryEvolutionAnalysisService = repositoryEvolutionAnalysisService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes the long-term repository evolution impact of a proposed change.
     *
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @param proposedChange Description of the proposed enhancement or change (required)
     * @return A structured JSON response containing the repository evolution report
     */
    @Tool(description = """
            Analyzes the long-term repository evolution impact of proposed features or architectural changes.
            
            This tool performs a comprehensive evolution analysis:
            1. Architecture Evolution: Detects architectural drift and alignment
            2. Package Growth: Estimates new packages and package density
            3. Module Expansion: Estimates new classes and module cohesion
            4. Dependency Evolution: Estimates new dependencies and circular dependency risk
            5. Convention Consistency: Evaluates adherence to repository conventions
            6. Maintainability: Assesses maintenance overhead and complexity concerns
            7. Technical Debt Indicators: Identifies potential technical debt accumulation
            8. Scalability Readiness: Evaluates scalability considerations
            9. Long-term Risks: Identifies future technical risks
            10. Recommended Practices: Suggests repository best practices
            11. Repository Evolution Score: Overall score (0-100) for repository evolution health
            
            Returns a structured response containing:
            - Proposed Change Summary: Context of the change in the repository
            - Architectural Impact: Impact on existing architecture
            - Maintainability Assessment: Maintenance implications
            - Technical Debt Indicators: Potential debt accumulation
            - Convention Compliance: Adherence to conventions
            - Scalability Considerations: Scalability implications
            - Long-term Risks: Future technical risks
            - Recommended Repository Practices: Best practice recommendations
            - Repository Evolution Score: Overall score (0-100)
            - Detailed analysis for each evolution category
            
            This tool NEVER modifies repository files or generates code changes.
            It only provides analysis and insights.
            """)
    public String analyzeRepositoryEvolution(
            String repositoryName,
            String branch,
            String proposedChange) {
        try {
            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            if (proposedChange == null || proposedChange.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Proposed change description is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

            logger.info("Executing analyze_repository_evolution tool for repository: {} branch: {}",
                    repositoryName, effectiveBranch);

            RepositoryEvolutionAnalysisResponse response = repositoryEvolutionAnalysisService.analyzeEvolution(
                    repositoryName.trim(), effectiveBranch, proposedChange.trim());

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing repository evolution: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(RepositoryEvolutionAnalysisResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize repository evolution analysis: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new AnalyzeRepositoryEvolutionError(errorType, message)
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
    private static class AnalyzeRepositoryEvolutionError {
        public String errorType;
        public String message;

        public AnalyzeRepositoryEvolutionError(String errorType, String message) {
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