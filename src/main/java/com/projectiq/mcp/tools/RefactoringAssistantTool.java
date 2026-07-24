package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.RefactoringAssistantResponse;
import com.projectiq.mcp.analysis.service.RefactoringAssistantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that analyzes proposed refactoring tasks and provides a deterministic,
 * repository-aware refactoring plan. This tool invokes task analysis, context
 * assembly, impact analysis, implementation planning, and test impact analysis
 * to produce a structured refactoring plan.
 *
 * <p>The plan includes the original task, refactoring type, affected classes,
 * affected methods, affected packages, dependencies involved, suggested execution
 * order, validation checklist, recommended tests, risks, and confidence level.</p>
 *
 * <p>This tool NEVER modifies repository files or generates code changes.
 * It only provides recommendations and analysis.</p>
 */
@Component
public class RefactoringAssistantTool {

    private static final Logger logger = LoggerFactory.getLogger(RefactoringAssistantTool.class);

    private final RefactoringAssistantService refactoringAssistantService;
    private final ObjectMapper objectMapper;

    public RefactoringAssistantTool(RefactoringAssistantService refactoringAssistantService) {
        this.refactoringAssistantService = refactoringAssistantService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes a proposed refactoring task and produces a structured refactoring plan.
     *
     * @param task           Natural language refactoring request (e.g., "Rename class UserService to UserManagementService") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing the refactoring plan
     */
    @Tool(description = """
            Analyzes a proposed refactoring task and produces a structured refactoring plan.
            
            This tool performs a complete refactoring analysis workflow:
            1. Analyzes the task to determine refactoring type, entities, and complexity
            2. Assembles relevant repository context
            3. Analyzes the impact of the proposed refactoring
            4. Generates an implementation plan
            5. Analyzes test impact
            6. Produces a structured refactoring plan
            
            Supported refactoring types:
            - Rename Class: Rename a class, type, or interface
            - Rename Method: Rename a method or function
            - Move Class: Move a class to a different package
            - Move Package: Move an entire package to a new location
            - Extract Method: Extract a code block into a new method
            - Extract Class: Extract related responsibilities into a new class
            - Inline Method: Inline a method's body at all call sites
            - Delete Dead Code: Remove unused or dead code
            - Split Large Class: Split a large class into multiple smaller classes
            - General Refactoring: General code restructuring
            
            Returns a structured refactoring plan containing:
            - Original Task: The input refactoring request
            - Refactoring Type: The detected type of refactoring
            - Affected Classes: Classes impacted by the refactoring
            - Affected Methods: Methods impacted by the refactoring
            - Affected Packages: Packages impacted by the refactoring
            - Dependencies Involved: Dependencies that need attention
            - Suggested Execution Order: Recommended sequence of steps
            - Validation Checklist: Items to verify after refactoring
            - Recommended Tests: Tests to run or create
            - Risks: Potential risks and concerns
            - Confidence Level: Low, Medium, or High
            
            This tool NEVER modifies repository files or generates code changes.
            It only provides recommendations and analysis.
            """)
    public String analyzeRefactoring(
            String task,
            String repositoryName,
            String branch) {
        try {
            if (task == null || task.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Refactoring task description is required");
            }

            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

            logger.info("Executing refactoring_assistant tool for task: {} in repository: {} branch: {}",
                    task, repositoryName, effectiveBranch);

            RefactoringAssistantResponse response = refactoringAssistantService.analyzeRefactoring(
                    task.trim(), repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing refactoring: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(RefactoringAssistantResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize refactoring analysis: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new RefactoringError(errorType, message)
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
    private static class RefactoringError {
        public String errorType;
        public String message;

        public RefactoringError(String errorType, String message) {
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