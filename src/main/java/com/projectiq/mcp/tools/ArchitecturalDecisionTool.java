package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.ArchitecturalDecisionResponse;
import com.projectiq.mcp.analysis.service.ArchitecturalDecisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that provides intelligent architectural decision recommendations.
 * Evaluates architectural alternatives and provides pros/cons, impact analysis,
 * and a recommended approach based on repository intelligence.
 *
 * <p>This tool accepts an architectural decision request and returns a deterministic
 * recommendation report covering alternatives, impact, scalability, maintainability,
 * and risks.</p>
 *
 * <p>This tool NEVER modifies repository files or generates code changes.
 * It only provides analysis and recommendations.</p>
 */
@Component
public class ArchitecturalDecisionTool {

    private static final Logger logger = LoggerFactory.getLogger(ArchitecturalDecisionTool.class);

    private final ArchitecturalDecisionService architecturalDecisionService;
    private final ObjectMapper objectMapper;

    public ArchitecturalDecisionTool(ArchitecturalDecisionService architecturalDecisionService) {
        this.architecturalDecisionService = architecturalDecisionService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Provides architectural decision recommendations based on the given request.
     *
     * @param decisionCategory The category of architectural decision (required).
     *                         Supported values: "New Service vs Existing Service",
     *                         "New Module vs Existing Module", "Extend API vs Create API",
     *                         "Event-Driven vs Synchronous", "Composition vs Inheritance",
     *                         "Configuration vs Code", "Shared Component vs Dedicated Component",
     *                         "Package Organization"
     * @param requestDescription A description of the architectural decision to evaluate (required)
     * @param repositoryName The target repository name (required)
     * @return A structured JSON response containing the architectural decision report
     */
    @Tool(description = """
            Provides intelligent architectural decision recommendations for design choices.
            
            This tool evaluates architectural alternatives and provides:
            1. Multiple alternative approaches with pros and cons
            2. Suitability scores and complexity levels for each alternative
            3. Repository impact assessment (files/classes affected, consistency impact)
            4. Dependency implications (new dependencies, affected dependencies, circular risk)
            5. Scalability assessment (horizontal/vertical scalability, performance)
            6. Maintainability assessment (code complexity, testability, reusability)
            7. Architectural risks and concerns
            8. Recommended approach with detailed decision rationale
            
            Supported decision categories:
            - "New Service vs Existing Service" - Whether to create a new service or extend existing
            - "New Module vs Existing Module" - Whether to create a new module or extend existing
            - "Extend API vs Create API" - Whether to extend an existing API or create a new one
            - "Event-Driven vs Synchronous" - Whether to use event-driven or synchronous communication
            - "Composition vs Inheritance" - Whether to use composition or inheritance
            - "Configuration vs Code" - Whether to use configuration-driven or code-based approach
            - "Shared Component vs Dedicated Component" - Whether to share or dedicate components
            - "Package Organization" - Whether to organize packages by layer or by feature
            
            This tool NEVER modifies repository files or generates code changes.
            It only provides analysis and recommendations.
            """)
    public String adviseArchitecture(
            String decisionCategory,
            String requestDescription,
            String repositoryName) {
        try {
            if (decisionCategory == null || decisionCategory.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Decision category is required");
            }
            if (requestDescription == null || requestDescription.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Request description is required");
            }
            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            logger.info("Executing advise_architecture tool for category: {}, repository: {}",
                    decisionCategory, repositoryName);

            ArchitecturalDecisionResponse response = architecturalDecisionService.adviseArchitecture(
                    decisionCategory.trim(),
                    requestDescription.trim(),
                    repositoryName.trim());

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in architectural decision: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(ArchitecturalDecisionResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize architectural decision: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new ArchitecturalDecisionError(errorType, message)
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

    private static class ArchitecturalDecisionError {
        public String errorType;
        public String message;

        public ArchitecturalDecisionError(String errorType, String message) {
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