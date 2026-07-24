package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.RefactoringImpactSimulationResponse;
import com.projectiq.mcp.analysis.service.RefactoringImpactSimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that simulates proposed refactoring operations before implementation.
 * Predicts repository-wide effects of structural code changes using indexed
 * repository intelligence, dependency analysis, architecture insights, and
 * workflow context, enabling AI coding agents to safely plan refactoring
 * activities without modifying source code.
 *
 * <p>This tool invokes code change analysis, dependency change prediction,
 * refactoring assistant, impact analysis, architecture insights, and context
 * pipeline services to produce a deterministic simulation report.</p>
 *
 * <p>This tool NEVER modifies repository files or generates code changes.
 * It only provides simulation and analysis.</p>
 */
@Component
public class SimulateRefactoringTool {

    private static final Logger logger = LoggerFactory.getLogger(SimulateRefactoringTool.class);

    private final RefactoringImpactSimulationService simulationService;
    private final ObjectMapper objectMapper;

    public SimulateRefactoringTool(RefactoringImpactSimulationService simulationService) {
        this.simulationService = simulationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Simulates a proposed refactoring operation and produces a deterministic
     * simulation report predicting the repository-wide impact.
     *
     * @param refactoringType  the type of refactoring (e.g., "Rename Class", "Move Class") (required)
     * @param targetEntity     the target entity name (e.g., class name, method name) (required)
     * @param sourceContext    additional context describing the refactoring (e.g., new name, target package) (optional)
     * @param repositoryName   the name of the repository to analyze (required)
     * @param branch           the git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing the simulation report
     */
    @Tool(description = """
            Simulates a proposed refactoring operation and produces a deterministic
            simulation report predicting the repository-wide impact.
            
            This tool performs a complete refactoring simulation workflow:
            1. Analyzes the proposed code change impact
            2. Predicts dependency changes
            3. Analyzes the refactoring impact
            4. Assesses architectural effects
            5. Builds context pipeline
            6. Produces a structured simulation report
            
            Supported refactoring types:
            - Rename Class: Rename a class, type, or interface
            - Rename Method: Rename a method or function
            - Move Class: Move a class to a different package
            - Move Package: Move an entire package to a new location
            - Extract Interface: Extract an interface from a class
            - Extract Service: Extract a service from a class
            - Split Class: Split a large class into multiple classes
            - Merge Classes: Merge two classes into one
            - Delete Dead Code: Remove unused or dead code
            
            Returns a structured simulation report containing:
            - Refactoring Summary: Overview of the proposed refactoring
            - Refactoring Type: The type of refactoring being simulated
            - Target Entity: The entity being refactored
            - Source Context: Additional context for the refactoring
            - Impacted Files: Files that will be impacted
            - Impacted Classes: Classes that will be impacted
            - Impacted Methods: Methods that will be impacted
            - Broken References: References that will break
            - Dependency Changes: Dependency changes required
            - Architectural Effects: Effects on architecture
            - Testing Impact: Impact on testing
            - Risk Assessment: Potential risks
            - Suggested Implementation Sequence: Recommended steps
            - Estimated Effort: Low, Medium, or High
            
            This tool NEVER modifies repository files or generates code changes.
            It only provides simulation and analysis.
            """)
    public String simulateRefactoring(
            String refactoringType,
            String targetEntity,
            String sourceContext,
            String repositoryName,
            String branch) {
        try {
            if (refactoringType == null || refactoringType.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Refactoring type is required");
            }

            if (targetEntity == null || targetEntity.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Target entity is required");
            }

            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";
            String effectiveContext = (sourceContext != null && !sourceContext.trim().isEmpty()) ? sourceContext.trim() : "";

            logger.info("Executing simulate_refactoring tool: type={}, target={}, context={}, repository={}, branch={}",
                    refactoringType, targetEntity, effectiveContext, repositoryName, effectiveBranch);

            RefactoringImpactSimulationResponse response = simulationService.simulateRefactoring(
                    refactoringType.trim(), targetEntity.trim(), effectiveContext,
                    repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error simulating refactoring: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(RefactoringImpactSimulationResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize simulation report: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new SimulationError(errorType, message)
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
    private static class SimulationError {
        public String errorType;
        public String message;

        public SimulationError(String errorType, String message) {
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