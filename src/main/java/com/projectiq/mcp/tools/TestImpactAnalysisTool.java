package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.TestImpactAnalysisResponse;
import com.projectiq.mcp.analysis.service.TestImpactAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that performs Test Impact Analysis for a proposed repository change.
 * Invokes task analysis, context assembly, impact analysis, and implementation
 * planning to produce a structured test impact report.
 *
 * <p>The report includes the original task, affected production classes,
 * related test classes, missing tests, recommended test execution order,
 * estimated testing effort, confidence level, and testing rationale.</p>
 *
 * <p>This tool NEVER executes any tests. It only recommends which tests
 * should be considered for execution or update.</p>
 */
@Component
public class TestImpactAnalysisTool {

    private static final Logger logger = LoggerFactory.getLogger(TestImpactAnalysisTool.class);

    private final TestImpactAnalysisService testImpactAnalysisService;
    private final ObjectMapper objectMapper;

    public TestImpactAnalysisTool(TestImpactAnalysisService testImpactAnalysisService) {
        this.testImpactAnalysisService = testImpactAnalysisService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes the test impact of a proposed development task and produces a
     * structured test impact report.
     *
     * @param task           Natural language development request (e.g., "Add pagination to UserController") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON response containing the test impact analysis report
     */
    @Tool(description = """
            Analyzes the test impact of a proposed development task and produces a structured test impact report.
            
            This tool performs a complete test impact analysis workflow:
            1. Analyzes the task to determine task type, entities, and complexity
            2. Assembles relevant repository context
            3. Analyzes the impact of the proposed change
            4. Generates an implementation plan
            5. Produces a structured test impact report
            
            Returns a structured test impact report containing:
            - Original Task: The input task description
            - Affected Production Classes: Production classes impacted by the change
            - Related Test Classes: Test classes related to affected production classes
            - Missing Tests: Tests that may need to be created
            - Recommended Test Execution Order: Suggested order for running tests
            - Estimated Testing Effort: Low, Medium, or High
            - Confidence Level: Low, Medium, or High
            - Testing Rationale: Human-readable explanation of the analysis
            
            This tool NEVER executes any tests. It only recommends which tests
            should be considered for execution or update.
            """)
    public String analyzeTestImpact(
            String task,
            String repositoryName,
            String branch) {
        try {
            if (task == null || task.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Task description is required");
            }

            if (repositoryName == null || repositoryName.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.trim().isEmpty()) ? branch.trim() : "main";

            logger.info("Executing test_impact_analysis tool for task: {} in repository: {} branch: {}",
                    task, repositoryName, effectiveBranch);

            TestImpactAnalysisResponse response = testImpactAnalysisService.analyzeTestImpact(
                    task.trim(), repositoryName.trim(), effectiveBranch);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing test impact: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(TestImpactAnalysisResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize test impact analysis: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new TestImpactError(errorType, message)
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
    private static class TestImpactError {
        public String errorType;
        public String message;

        public TestImpactError(String errorType, String message) {
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