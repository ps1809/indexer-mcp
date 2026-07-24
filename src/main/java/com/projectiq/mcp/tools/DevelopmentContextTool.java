package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.client.dto.BuildContextRequest;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DevelopmentContext;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import com.projectiq.mcp.client.service.DevelopmentContextService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tool for providing AI coding agents with a consolidated development
 * context optimized for implementation tasks. Builds upon the Repository
 * Context Builder and returns only the information required for coding,
 * reducing unnecessary MCP tool invocations.
 */
@Component
public class DevelopmentContextTool {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DevelopmentContextTool.class);

    private final DevelopmentContextService developmentContextService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DevelopmentContextTool(DevelopmentContextService developmentContextService) {
        this.developmentContextService = developmentContextService;
    }

    /**
     * Generates a development-focused context for the given task.
     *
     * @param task           Natural language development task (e.g., "Add pagination to UserController") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch         Git branch to analyze (optional, defaults to "main")
     * @return A structured, implementation-focused context as a formatted string
     */
    @Tool(description = """
            Generates a development-focused context optimized for implementation tasks.
            
            Returns a structured context including:
            - Repository Summary (name, branch, description, file/class/commit counts)
            - Relevant Classes (class metadata, annotations, source locations)
            - Relevant Methods (method signatures, parameters, declaring classes)
            - Spring Components (beans, services, controllers, repositories)
            - REST APIs (endpoints, HTTP methods, paths, request/response types)
            - Dependencies (project dependencies with groupId, artifactId, version, scope)
            - Related Files (files related to the task)
            
            All results are:
            - Filtered based on the provided task description
            - Sorted deterministically for consistent output
            - Free of duplicate information
            - Optimized for software development tasks
            
            Useful for providing AI coding agents with a consolidated, implementation-ready context.
            Reduces the need for multiple individual MCP tool invocations.
            """)
    public String developmentContext(
            String task,
            String repositoryName,
            String branch) {

        try {
            if (task == null || task.isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Task description is required");
            }

            if (repositoryName == null || repositoryName.isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "Repository name is required");
            }

            String effectiveBranch = (branch != null && !branch.isEmpty()) ? branch : "main";

            BuildContextRequest request = new BuildContextRequest();
            request.setTask(task);
            request.setRepositoryName(repositoryName);
            request.setBranch(effectiveBranch);

            logger.info("Executing development_context tool for task: {} in repository: {}", task, repositoryName);

            DevelopmentContext context = developmentContextService.createDevelopmentContext(request);

            return formatResponse(context);

        } catch (IndexerConnectionException e) {
            return createErrorResponse("INDEXER_UNREACHABLE", "Cannot connect to ProjectIQ Indexer: " + e.getMessage());
        } catch (IndexerTimeoutException e) {
            return createErrorResponse("INDEXER_TIMEOUT", "ProjectIQ Indexer request timed out: " + e.getMessage());
        } catch (IndexerHttpException e) {
            return createErrorResponse("INDEXER_HTTP_ERROR", "Indexer HTTP error: " + e.getMessage());
        } catch (IndexerClientException e) {
            return createErrorResponse("INDEXER_ERROR", "Indexer client error: " + e.getMessage());
        } catch (Exception e) {
            return createErrorResponse("INTERNAL_ERROR", "Internal error: " + e.getMessage());
        }
    }

    private String formatResponse(DevelopmentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Development Context\n");
        sb.append("===================\n");
        sb.append("Task: ").append(context.getTask()).append("\n");
        sb.append("Repository: ").append(context.getRepositoryName()).append("\n");
        sb.append("Branch: ").append(context.getBranch()).append("\n");
        sb.append("Build Timestamp: ").append(context.getBuildTimestamp()).append("\n\n");

        // Errors
        if (context.hasErrors()) {
            sb.append("Errors (").append(context.getErrors().size()).append(")\n");
            sb.append("-------------------\n");
            for (ContextBuildError error : context.getErrors()) {
                sb.append("  [").append(error.getEndpoint()).append("] ")
                  .append(error.getErrorType()).append(": ").append(error.getMessage()).append("\n");
            }
            sb.append("\n");
        }

        // Repository Summary
        sb.append("Repository Summary\n");
        sb.append("------------------\n");
        if (context.getRepositorySummary() != null) {
            var summary = context.getRepositorySummary();
            sb.append("  Name: ").append(summary.getRepositoryName()).append("\n");
            sb.append("  Branch: ").append(summary.getBranch()).append("\n");
            sb.append("  Status: ").append(summary.getStatus()).append("\n");
            sb.append("  File Count: ").append(summary.getFileCount()).append("\n");
            sb.append("  Class Count: ").append(summary.getClassCount()).append("\n");
            sb.append("  Commit Count: ").append(summary.getCommitCount()).append("\n");
        } else {
            sb.append("  Not available\n");
        }
        sb.append("\n");

        // Relevant Classes
        appendSection(sb, "Relevant Classes", context.getRelevantClasses(), ClassInfo.class);

        // Relevant Methods
        appendSection(sb, "Relevant Methods", context.getRelevantMethods(), MethodInfo.class);

        // Spring Components
        appendSection(sb, "Spring Components", context.getSpringComponents(), SpringComponentInfo.class);

        // REST APIs
        appendSection(sb, "REST APIs", context.getRestApis(), RestEndpointInfo.class);

        // Dependencies
        appendSection(sb, "Dependencies", context.getDependencies(), DependencyInfo.class);

        // Related Files
        appendSection(sb, "Related Files", context.getRelatedFiles(), RelatedFile.class);

        return sb.toString();
    }

    private <T> void appendSection(StringBuilder sb, String title, List<T> items, Class<T> clazz) {
        sb.append(title).append("\n");
        sb.append(new String(new char[title.length()]).replace("\0", "-")).append("\n");
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < Math.min(items.size(), 10); i++) {
                try {
                    String json = objectMapper.writeValueAsString(items.get(i));
                    sb.append("  [").append(i + 1).append("] ").append(json.substring(0, Math.min(200, json.length())));
                    if (json.length() > 200) {
                        sb.append("...");
                    }
                    sb.append("\n");
                } catch (JsonProcessingException e) {
                    sb.append("  [").append(i + 1).append("] Error serializing item\n");
                }
            }
            if (items.size() > 10) {
                sb.append("  ... and ").append(items.size() - 10).append(" more\n");
            }
        } else {
            sb.append("  No results\n");
        }
        sb.append("\n");
    }

    private String createErrorResponse(String errorType, String message) {
        return "Error [" + errorType + "]: " + message;
    }
}