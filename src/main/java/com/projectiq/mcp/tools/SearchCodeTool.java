package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.SearchCodeRequest;
import com.projectiq.mcp.client.dto.SearchCodeResponse;
import com.projectiq.mcp.client.dto.SearchResult;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP Tool for searching code in ProjectIQ Indexer.
 * Allows AI coding agents to quickly locate relevant code elements.
 */
@Component
public class SearchCodeTool {

    private static final Logger logger = LoggerFactory.getLogger(SearchCodeTool.class);

    private final IndexerRestClient indexerRestClient;

    public SearchCodeTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Searches code in the ProjectIQ Indexer.
     *
     * @param repositoryName the name of the repository to search in
     * @param query the search query to find code elements
     * @param branch the branch name (optional, defaults to main if not specified)
     * @param packageName filter results by package name (optional)
     * @param maxResults maximum number of results to return (optional)
     * @return a formatted string containing the search results
     */
    @Tool(description = "Search code in ProjectIQ Indexer to locate relevant code elements. " +
            "Returns matching classes, methods, fields, and other code symbols with their file paths and line numbers.")
    public String searchCode(
            @ToolParam(description = "The name of the repository to search in") String repositoryName,
            @ToolParam(description = "The search query to find code elements") String query,
            @ToolParam(description = "The branch name to search in (optional)", required = false) String branch,
            @ToolParam(description = "Filter results by package name (optional)", required = false) String packageName,
            @ToolParam(description = "Maximum number of results to return (optional)", required = false) Integer maxResults) {

        logger.debug("Search code tool invoked for repository: {}, query: {}", repositoryName, query);

        try {
            SearchCodeRequest request = new SearchCodeRequest(repositoryName, query);
            request.setBranch(branch);
            request.setPackageName(packageName);
            request.setMaxResults(maxResults);

            SearchCodeResponse response = indexerRestClient.searchCode(request);

            return formatResponse(response);
        } catch (IndexerTimeoutException e) {
            String message = "Unable to search code: Connection to ProjectIQ Indexer timed out. " +
                    "Please ensure the Indexer service is running and accessible.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerConnectionException e) {
            String message = "Unable to search code: Cannot connect to ProjectIQ Indexer. " +
                    "Please ensure the Indexer service is running and the base URL is correctly configured.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerClientException e) {
            String message = "Unable to search code: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (Exception e) {
            String message = "An unexpected error occurred while searching code: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        }
    }

    /**
     * Formats the search code response into a readable string.
     *
     * @param response the search code response
     * @return formatted string representation
     */
    private String formatResponse(SearchCodeResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Code Search Results\n\n");
        sb.append("- **Repository**: ").append(response.getRepositoryName()).append("\n");
        sb.append("- **Query**: `").append(response.getQuery() != null ? response.getQuery() : "").append("`\n");
        sb.append("- **Total Results**: ").append(response.getTotalResults() != null ? response.getTotalResults() : 0).append("\n");

        List<SearchResult> results = response.getResults();
        if (results != null && !results.isEmpty()) {
            sb.append("\n### Matches\n\n");
            for (SearchResult result : results) {
                sb.append("#### ").append(result.getType()).append(": `").append(result.getName()).append("`\n");
                
                if (result.getClassName() != null) {
                    sb.append("- **Class**: ").append(result.getClassName()).append("\n");
                }
                
                if (result.getPackageName() != null) {
                    sb.append("- **Package**: ").append(result.getPackageName()).append("\n");
                }
                
                if (result.getFilePath() != null) {
                    sb.append("- **File**: ").append(result.getFilePath());
                    if (result.getLineNumber() != null) {
                        sb.append(":").append(result.getLineNumber());
                    }
                    sb.append("\n");
                }

                if (result.getDescription() != null) {
                    sb.append("- **Description**: ").append(result.getDescription()).append("\n");
                }

                if (result.getSnippet() != null) {
                    sb.append("```\n").append(result.getSnippet()).append("\n```\n");
                }
                
                sb.append("\n");
            }
        } else {
            sb.append("\nNo results found.\n");
        }

        return sb.toString();
    }
}