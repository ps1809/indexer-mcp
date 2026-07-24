package com.projectiq.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectiq.mcp.client.dto.BuildContextRequest;
import com.projectiq.mcp.client.dto.ContextBuildError;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RepositoryContext;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
import com.projectiq.mcp.client.dto.SearchResult;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import com.projectiq.mcp.client.service.RepositoryContextBuilderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tool for building a unified repository context by aggregating information
 * from multiple ProjectIQ Indexer endpoints.
 */
@Component
public class BuildContextTool {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BuildContextTool.class);

    private final RepositoryContextBuilderService contextBuilderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BuildContextTool(RepositoryContextBuilderService contextBuilderService) {
        this.contextBuilderService = contextBuilderService;
    }

    /**
     * Builds a unified repository context for the given development task.
     *
     * @param task Natural language development task description (e.g., "Add pagination to UserController") (required)
     * @param repositoryName Name of the repository to analyze (required)
     * @param branch Git branch to analyze (optional, defaults to "main")
     * @return JSON string containing the unified repository context
     */
    @Tool(description = """
            Builds a unified repository context by aggregating information from multiple ProjectIQ Indexer endpoints.
            
            Returns a structured repository context including:
            - Repository summary (name, branch, description)
            - Repository statistics (file counts, contributor stats, commit info)
            - Search results (relevant code snippets)
            - Matching Spring components (beans, services, controllers)
            - Matching REST APIs (endpoints, HTTP methods, paths)
            - Matching classes (class metadata and details)
            - Matching methods (method signatures and parameters)
            - Related files (files related to the task)
            - Dependencies (project dependencies)
            
            All results are filtered based on the provided task description.
            Duplicate entries are eliminated automatically.
            
            Useful for providing AI coding agents with comprehensive repository context.
            """)
    public String buildContext(
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

            logger.info("Executing build_context tool for task: {} in repository: {}", task, repositoryName);

            RepositoryContext context = contextBuilderService.buildContext(request);

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

    private String formatResponse(RepositoryContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Repository Context\n");
        sb.append("==================\n");
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

        // Repository Statistics
        sb.append("Repository Statistics\n");
        sb.append("--------------------\n");
        if (context.getRepositoryStatistics() != null) {
            var stats = context.getRepositoryStatistics();
            sb.append("  File Count: ").append(stats.getFileCount()).append("\n");
            sb.append("  Total Lines of Code: ").append(stats.getTotalLinesOfCode()).append("\n");
            sb.append("  Class Count: ").append(stats.getClassCount()).append("\n");
            sb.append("  Method Count: ").append(stats.getMethodCount()).append("\n");
            if (stats.getContributors() != null) {
                sb.append("  Contributors: ").append(stats.getContributors().size()).append("\n");
            }
        } else {
            sb.append("  Not available\n");
        }
        sb.append("\n");

        // Search Results
        appendSection(sb, "Search Results", context.getSearchResults(), SearchResult.class);

        // Spring Components
        appendSection(sb, "Spring Components", context.getSpringComponents(), SpringComponentInfo.class);

        // REST APIs
        appendSection(sb, "REST APIs", context.getRestApis(), RestEndpointInfo.class);

        // Classes
        appendSection(sb, "Classes", context.getClasses(), ClassInfo.class);

        // Methods
        appendSection(sb, "Methods", context.getMethods(), MethodInfo.class);

        // Related Files
        appendSection(sb, "Related Files", context.getRelatedFiles(), RelatedFile.class);

        // Dependencies
        appendSection(sb, "Dependencies", context.getDependencies(), DependencyInfo.class);

        return sb.toString();
    }

    private <T> void appendSection(StringBuilder sb, String title, List<T> items, Class<T> clazz) {
        sb.append(title).append("\n");
        sb.append(new String(new char[title.length()]).replace("\0", "-")).append("\n");
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < Math.min(items.size(), 10); i++) { // Limit to first 10 items
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