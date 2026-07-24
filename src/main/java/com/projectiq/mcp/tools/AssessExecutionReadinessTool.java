package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.readiness.dto.ReadinessReport;
import com.projectiq.mcp.readiness.service.ExecutionReadinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that performs an Intelligent Execution Readiness Assessment.
 * Evaluates workflow completeness, execution planning, validation results,
 * recommendations, repository readiness, and architecture insights to
 * produce a single deterministic readiness decision.
 *
 * <p>This tool does not modify any workflows, repositories, or execution plans.
 * It produces a readiness assessment based purely on deterministic rules
 * without any AI/LLM involvement.</p>
 */
@Component
public class AssessExecutionReadinessTool {

    private static final Logger logger = LoggerFactory.getLogger(AssessExecutionReadinessTool.class);

    private final ExecutionReadinessService executionReadinessService;
    private final ObjectMapper objectMapper;

    public AssessExecutionReadinessTool(ExecutionReadinessService executionReadinessService) {
        this.executionReadinessService = executionReadinessService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Performs a comprehensive deterministic execution readiness assessment.
     * Evaluates workflow completeness, execution planning, validation results,
     * recommendations, repository readiness, and architecture insights to
     * produce a final implementation decision.
     *
     * @param workflowName     Name of the workflow to assess for readiness (required)
     * @param workflowType     Type of workflow (e.g., "Feature Implementation", "Bug Fix") (optional)
     * @param originalRequest  Original developer request that defines the workflow (optional)
     * @param repositoryName   Name of the target repository (required)
     * @param branch           Branch name for analysis (optional, defaults to "main")
     * @return A structured JSON response containing the complete readiness assessment
     */
    @Tool(description = """
            Performs an intelligent execution readiness assessment for an AI coding agent.
            
            This tool evaluates 6 dimensions of readiness to produce a final implementation decision:
            1. Workflow Completeness - Validates workflow name and type
            2. Execution Planning - Reviews execution plan status and tasks
            3. Validation Results - Analyzes validation findings, blocking issues, and warnings
            4. Recommendations - Evaluates critical/high priority recommendations and risks
            5. Repository Readiness - Analyzes repository health, maintainability, and testing maturity
            6. Architecture Insights - Reviews architectural style and patterns
            
            Returns a readiness assessment report containing:
            - Overall readiness level (Ready, Ready With Warnings, Requires Review, Not Ready)
            - Readiness score (0-100)
            - Blocking issues and warnings
            - Passed checks and category assessments
            - Repository summary and risk overview
            - Final implementation recommendation
            - Next actions for proceeding or resolving issues
            
            The assessment is purely deterministic with no AI/LLM involvement.
            """)
    public String assessExecutionReadiness(
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

            logger.info("Executing assess_execution_readiness tool for workflow: {} in repository: {}",
                    workflowName, repositoryName);

            ReadinessReport report = executionReadinessService.assessReadiness(
                    workflowName.trim(),
                    workflowType != null ? workflowType.trim() : null,
                    originalRequest != null ? originalRequest.trim() : null,
                    repositoryName.trim(),
                    branch != null ? branch.trim() : null);

            return serializeReport(report);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error assessing readiness: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeReport(ReadinessReport report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR",
                    "Failed to serialize readiness report: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new ReadinessToolError(errorType, message)
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
    private static class ReadinessToolError {
        public String errorType;
        public String message;

        public ReadinessToolError(String errorType, String message) {
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