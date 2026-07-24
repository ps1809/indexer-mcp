package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.client.dto.BuildContextRequest;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.PromptContext;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import com.projectiq.mcp.client.service.DevelopmentContextService;
import com.projectiq.mcp.client.service.PromptContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * MCP Tool for generating an AI-ready structured prompt context from a
 * natural language development task. This is the final intelligence layer
 * before an AI coding agent begins implementation, transforming the structured
 * Development Context into an AI-friendly prompt context that gives the coding
 * agent everything it needs while minimizing unnecessary repository exploration
 * and token usage.
 */
@Component
public class PromptContextTool {

    private static final Logger logger = LoggerFactory.getLogger(PromptContextTool.class);

    private final DevelopmentContextService developmentContextService;
    private final PromptContextService promptContextService;
    private final ObjectMapper objectMapper;

    public PromptContextTool(DevelopmentContextService developmentContextService,
                             PromptContextService promptContextService) {
        this.developmentContextService = developmentContextService;
        this.promptContextService = promptContextService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * Generates an AI-ready structured prompt context for the given development task.
     *
     * @param task           Natural language development task (e.g., "Implement JWT authentication") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured JSON prompt context optimized for AI consumption
     */
    @Tool(description = """
            Generates an AI-ready structured prompt context for a development task.
            
            This is the final intelligence layer before an AI coding agent begins implementation.
            It transforms the structured Development Context into an AI-friendly prompt context
            that gives the coding agent everything it needs while minimizing token usage.
            
            Returns a structured JSON prompt context including:
            - Repository Summary (name, branch, file/class/method/commit counts)
            - Relevant Packages (deduplicated package names extracted from classes, methods, components)
            - Relevant Classes (class metadata, annotations, source locations)
            - Relevant Methods (method signatures, parameters, declaring classes)
            - Spring Components (beans, services, controllers, repositories)
            - REST APIs (endpoints, HTTP methods, paths, request/response types)
            - Related Files (files related to the task)
            - Required Dependencies (project dependencies with groupId, artifactId, version, scope)
            - Repository Conventions (naming conventions, package structure, framework version, build tool, Java version)
            
            All results are:
            - Filtered based on the provided task description
            - Sorted deterministically for consistent output
            - Free of duplicate information
            - Optimized for downstream AI consumption
            - Returned as structured JSON
            
            Useful for providing AI coding agents with a consolidated, implementation-ready context
            in a single invocation, reducing token usage and repository exploration.
            """)
    public String promptContext(
            String task,
            String repositoryName,
            String branch) {

        try {
            if (task == null || task.isEmpty()) {
                return createErrorJson("INVALID_ARGUMENT", "Task description is required");
            }

            if (repositoryName == null || repositoryName.isEmpty()) {
                return createErrorJson("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.isEmpty()) ? branch : "main";

            BuildContextRequest request = new BuildContextRequest();
            request.setTask(task);
            request.setRepositoryName(repositoryName);
            request.setBranch(effectiveBranch);

            logger.info("Executing prompt_context tool for task: {} in repository: {}", task, repositoryName);

            DevelopmentContext developmentContext = developmentContextService.createDevelopmentContext(request);
            PromptContext promptContext = promptContextService.createPromptContext(developmentContext);

            return serializePromptContext(promptContext);

        } catch (IndexerConnectionException e) {
            return createErrorJson("INDEXER_UNREACHABLE", "Cannot connect to ProjectIQ Indexer: " + e.getMessage());
        } catch (IndexerTimeoutException e) {
            return createErrorJson("INDEXER_TIMEOUT", "ProjectIQ Indexer request timed out: " + e.getMessage());
        } catch (IndexerHttpException e) {
            return createErrorJson("INDEXER_HTTP_ERROR", "Indexer HTTP error: " + e.getMessage());
        } catch (IndexerClientException e) {
            return createErrorJson("INDEXER_ERROR", "Indexer client error: " + e.getMessage());
        } catch (JsonProcessingException e) {
            return createErrorJson("SERIALIZATION_ERROR", "Failed to serialize prompt context: " + e.getMessage());
        } catch (Exception e) {
            return createErrorJson("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    /**
     * Serializes the prompt context to a formatted JSON string.
     */
    private String serializePromptContext(PromptContext promptContext) throws JsonProcessingException {
        return objectMapper.writeValueAsString(promptContext);
    }

    /**
     * Creates a JSON error response with the given error type and message.
     */
    private String createErrorJson(String errorType, String message) {
        try {
            return objectMapper.writeValueAsString(
                    new PromptContextError(errorType, message)
            );
        } catch (JsonProcessingException e) {
            return "{\"errorType\":\"" + escapeJson(errorType) + "\",\"message\":\"" + escapeJson(message) + "\"}";
        }
    }

    /**
     * Escapes a string for safe inclusion in JSON.
     */
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
    private static class PromptContextError {
        public String errorType;
        public String message;

        public PromptContextError(String errorType, String message) {
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