package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RepositorySummaryRequest;
import com.projectiq.mcp.client.dto.RepositorySummaryResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP Tool for retrieving repository summary from ProjectIQ Indexer.
 * Provides an overview of the repository including commit count, 
 * package count, class count, method count, and file count.
 */
@Component
public class RepositorySummaryTool {

    private static final Logger logger = LoggerFactory.getLogger(RepositorySummaryTool.class);

    private final IndexerRestClient indexerRestClient;

    public RepositorySummaryTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Retrieves the repository summary from ProjectIQ Indexer.
     *
     * @param repositoryName the name of the repository to get summary for
     * @param branch the branch name (optional, defaults to main if not specified)
     * @return a formatted string containing the repository summary information
     */
    @Tool(description = "Retrieve a summary of a repository from ProjectIQ Indexer. " +
            "Returns commit count, package count, class count, method count, file count, " +
            "and indexed packages with their class counts.")
    public String repositorySummary(
            @ToolParam(description = "The name of the repository to summarize") String repositoryName,
            @ToolParam(description = "The branch name to get summary for (optional)", required = false) String branch) {
        
        logger.debug("Repository summary tool invoked for repository: {}, branch: {}", repositoryName, branch);

        try {
            RepositorySummaryRequest request = new RepositorySummaryRequest(repositoryName, branch);
            RepositorySummaryResponse response = indexerRestClient.getRepositorySummary(request);
            
            return formatResponse(response);
        } catch (IndexerTimeoutException e) {
            String message = "Unable to retrieve repository summary: Connection to ProjectIQ Indexer timed out. " +
                    "Please ensure the Indexer service is running and accessible.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerConnectionException e) {
            String message = "Unable to retrieve repository summary: Cannot connect to ProjectIQ Indexer. " +
                    "Please ensure the Indexer service is running and the base URL is correctly configured.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerClientException e) {
            String message = "Unable to retrieve repository summary: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (Exception e) {
            String message = "An unexpected error occurred while retrieving repository summary: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        }
    }

    /**
     * Formats the repository summary response into a readable string.
     *
     * @param response the repository summary response
     * @return formatted string representation
     */
    private String formatResponse(RepositorySummaryResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Repository Summary\n\n");
        sb.append("- **Repository**: ").append(response.getRepositoryName()).append("\n");
        sb.append("- **Branch**: ").append(response.getBranch() != null ? response.getBranch() : "main").append("\n");
        sb.append("- **Status**: ").append(response.getStatus()).append("\n");
        sb.append("- **Indexed**: ").append(response.getLastIndexedDate() != null ? response.getLastIndexedDate() : "N/A").append("\n");
        sb.append("\n### Statistics\n");
        sb.append("- **Commits**: ").append(Long.valueOf(response.getCommitCount())).append("\n");
        sb.append("- **Packages**: ").append(Long.valueOf(response.getPackageCount())).append("\n");
        sb.append("- **Classes**: ").append(Long.valueOf(response.getClassCount())).append("\n");
        sb.append("- **Methods**: ").append(Long.valueOf(response.getMethodCount())).append("\n");
        sb.append("- **Files**: ").append(Long.valueOf(response.getFileCount())).append("\n");

        if (response.getPackages() != null && !response.getPackages().isEmpty()) {
            sb.append("\n### Packages\n");
            for (com.projectiq.mcp.client.dto.PackageSummary pkg : response.getPackages()) {
                sb.append("- **").append(pkg.getPackageName()).append("**: ")
                        .append(Long.valueOf(pkg.getClassCount())).append(" classes, ")
                        .append(Long.valueOf(pkg.getMethodCount())).append(" methods\n");
            }
        }

        return sb.toString();
    }
}