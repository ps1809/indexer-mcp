package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.ContributorStats;
import com.projectiq.mcp.client.dto.FileTypeStats;
import com.projectiq.mcp.client.dto.RepositoryStatsRequest;
import com.projectiq.mcp.client.dto.RepositoryStatsResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP Tool for retrieving repository statistics from ProjectIQ Indexer.
 * Provides detailed statistics including contributor information and file type breakdown.
 */
@Component
public class RepositoryStatisticsTool {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryStatisticsTool.class);

    private final IndexerRestClient indexerRestClient;

    public RepositoryStatisticsTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Retrieves repository statistics from ProjectIQ Indexer.
     *
     * @param repositoryName the name of the repository to get statistics for
     * @param branch the branch name (optional, defaults to main if not specified)
     * @return a formatted string containing the repository statistics information
     */
    @Tool(description = "Retrieve detailed statistics of a repository from ProjectIQ Indexer. " +
            "Returns commit count, package count, class count, method count, file count, " +
            "total lines of code, top contributors with their commit counts, and file type breakdown.")
    public String repositoryStatistics(
            @ToolParam(description = "The name of the repository to get statistics for") String repositoryName,
            @ToolParam(description = "The branch name to get statistics for (optional)", required = false) String branch) {

        logger.debug("Repository statistics tool invoked for repository: {}, branch: {}", repositoryName, branch);

        try {
            RepositoryStatsRequest request = new RepositoryStatsRequest(repositoryName, branch);
            RepositoryStatsResponse response = indexerRestClient.getRepositoryStatistics(request);

            return formatResponse(response);
        } catch (IndexerTimeoutException e) {
            String message = "Unable to retrieve repository statistics: Connection to ProjectIQ Indexer timed out. " +
                    "Please ensure the Indexer service is running and accessible.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerConnectionException e) {
            String message = "Unable to retrieve repository statistics: Cannot connect to ProjectIQ Indexer. " +
                    "Please ensure the Indexer service is running and the base URL is correctly configured.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerClientException e) {
            String message = "Unable to retrieve repository statistics: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (Exception e) {
            String message = "An unexpected error occurred while retrieving repository statistics: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        }
    }

    /**
     * Formats the repository statistics response into a readable string.
     *
     * @param response the repository statistics response
     * @return formatted string representation
     */
    private String formatResponse(RepositoryStatsResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Repository Statistics\n\n");
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
        sb.append("- **Total Lines of Code**: ").append(Long.valueOf(response.getTotalLinesOfCode())).append("\n");

        if (response.getContributors() != null && !response.getContributors().isEmpty()) {
            sb.append("\n### Top Contributors\n");
            int contributorCount = 0;
            for (ContributorStats contributor : response.getContributors()) {
                contributorCount++;
                sb.append("- **").append(contributor.getAuthor()).append("**: ")
                        .append(Long.valueOf(contributor.getCommitCount())).append(" commits");
                if (contributor.getLastActiveDate() != null) {
                    sb.append(", last active: ").append(contributor.getLastActiveDate());
                }
                sb.append("\n");
                if (contributorCount >= 10) {
                    break;
                }
            }
        }

        if (response.getFileTypeStats() != null && !response.getFileTypeStats().isEmpty()) {
            sb.append("\n### File Type Breakdown\n");
            for (FileTypeStats fileTypeStat : response.getFileTypeStats()) {
                sb.append("- **").append(fileTypeStat.getFileType()).append("**: ")
                        .append(Long.valueOf(fileTypeStat.getFileCount())).append(" files, ")
                        .append(Long.valueOf(fileTypeStat.getTotalLinesOfCode())).append(" LOC\n");
            }
        }

        return sb.toString();
    }
}