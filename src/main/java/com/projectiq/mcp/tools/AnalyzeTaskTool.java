package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse;
import com.projectiq.mcp.analysis.dto.TaskAnalysisResponse.ExecutionStep;
import com.projectiq.mcp.analysis.service.TaskAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool that analyzes a natural language development task and produces a
 * deterministic execution plan without querying any repository or using any AI model.
 * 
 * This is the entry point for Intelligent Task Analysis, providing developers
 * and AI agents with a structured understanding of what needs to be done and
 * which MCP tools are required.
 */
@Component
public class AnalyzeTaskTool {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzeTaskTool.class);

    private final TaskAnalysisService taskAnalysisService;
    private final ObjectMapper objectMapper;

    public AnalyzeTaskTool(TaskAnalysisService taskAnalysisService) {
        this.taskAnalysisService = taskAnalysisService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Analyzes a natural language development task and returns a structured
     * execution plan with detected task type, entities, required tools,
     * and complexity assessment.
     *
     * @param task Natural language development task (e.g., "Add pagination to UserController") (required)
     * @return A structured JSON response containing the execution plan
     */
    @Tool(description = """
            Analyzes a natural language development task and generates a deterministic execution plan.
            
            Returns a structured analysis including:
            - Original Task: The input task description
            - Detected Task Type: Classification of the development activity
            - Confidence Level: How confident the analysis is (HIGH, MEDIUM, LOW)
            - Detected Entities: Repository entities mentioned in the task
            - Suggested MCP Tools: Which MCP tools will be needed
            - Ordered Execution Plan: Step-by-step plan with tool names and descriptions
            - Reasoning Summary: Deterministic explanation of the analysis
            - Estimated Complexity: Task complexity assessment (LOW, MEDIUM, HIGH)
            
            This tool performs deterministic rule-based analysis only:
            - No AI models or LLMs are used
            - No repository queries are executed
            - No code generation is performed
            - No automatic tool execution occurs
            
            Useful for understanding the scope and requirements of a development task
            before executing any repository queries or generating code.
            """)
    public String analyzeTask(String task) {
        try {
            if (task == null || task.trim().isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Task description is required");
            }

            logger.info("Executing analyze_task tool for: {}", task);

            TaskAnalysisResponse response = taskAnalysisService.analyze(task);

            return serializeResponse(response);

        } catch (IllegalArgumentException e) {
            return createErrorResponse("INVALID_ARGUMENT", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error analyzing task: {}", e.getMessage(), e);
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String serializeResponse(TaskAnalysisResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            return createErrorResponse("SERIALIZATION_ERROR", "Failed to serialize analysis response: " + e.getMessage());
        }
    }

    private String createErrorResponse(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new AnalyzeTaskError(errorType, message)
            );
        } catch (JsonProcessingException e) {
            return "{\"errorType\":\"" + escapeJson(errorType) + "\",\"message\":\"" + escapeJson(message) + "\"}";
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
    private static class AnalyzeTaskError {
        public String errorType;
        public String message;

        public AnalyzeTaskError(String errorType, String message) {
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