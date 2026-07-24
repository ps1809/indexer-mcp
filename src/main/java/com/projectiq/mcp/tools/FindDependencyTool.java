package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.DependencyInfo;
import com.projectiq.mcp.client.dto.DependencyRequest;
import com.projectiq.mcp.client.dto.DependencyResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * MCP Tool for finding dependencies in ProjectIQ Indexer.
 * Allows AI coding agents to quickly discover project dependencies,
 * modules, libraries and internal project relationships.
 */
@Component
public class FindDependencyTool {

    private static final Logger logger = LoggerFactory.getLogger(FindDependencyTool.class);

    /**
     * Supported dependency types.
     */
    public static final List<String> SUPPORTED_DEPENDENCY_TYPES = Arrays.asList(
            "MAVEN",
            "GRADLE",
            "INTERNAL_MODULE",
            "EXTERNAL_LIBRARY"
    );

    private final IndexerRestClient indexerRestClient;

    public FindDependencyTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Finds dependencies in the ProjectIQ Indexer.
     *
     * @param repositoryName the name of the repository to search in
     * @param dependencyTypes the dependency types to filter by (e.g., MAVEN, GRADLE, INTERNAL_MODULE, EXTERNAL_LIBRARY)
     * @param branch the branch name (optional, defaults to main if not specified)
     * @param packageName filter results by package name (optional)
     * @return a formatted string containing the matching dependencies
     */
    @Tool(description = "Find dependencies in ProjectIQ Indexer. " +
            "Returns matching dependencies with their name, group ID, artifact ID, " +
            "version, scope and type.")
    public String findDependency(
            @ToolParam(description = "The name of the repository to search in") String repositoryName,
            @ToolParam(description = "List of dependency types to filter by (e.g., MAVEN, GRADLE, INTERNAL_MODULE, EXTERNAL_LIBRARY). Comma-separated if multiple types. If not specified, returns all dependency types.", required = false) String dependencyTypes,
            @ToolParam(description = "The branch name to search in (optional)", required = false) String branch,
            @ToolParam(description = "Filter results by package name (optional)", required = false) String packageName) {

        logger.debug("find_dependency tool invoked for repository: {}, types: {}", repositoryName, dependencyTypes);

        try {
            DependencyRequest request = new DependencyRequest();
            request.setRepositoryName(repositoryName);

            // Parse dependency types
            if (dependencyTypes != null && !dependencyTypes.trim().isEmpty()) {
                List<String> typeList = Arrays.stream(dependencyTypes.split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .filter(s -> !s.isEmpty())
                        .toList();
                request.setDependencyTypes(typeList);
            }

            request.setBranch(branch);
            request.setPackageName(packageName);

            DependencyResponse response = indexerRestClient.findDependency(request);

            return formatDependencyResponse(response);
        } catch (IndexerTimeoutException e) {
            String message = "Unable to find dependencies: Connection to ProjectIQ Indexer timed out. " +
                    "Please ensure the Indexer service is running and accessible.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerConnectionException e) {
            String message = "Unable to find dependencies: Cannot connect to ProjectIQ Indexer. " +
                    "Please ensure the Indexer service is running and the base URL is correctly configured.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerClientException e) {
            String message = "Unable to find dependencies: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (Exception e) {
            String message = "An unexpected error occurred while finding dependencies: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        }
    }

    /**
     * Formats the dependency response into a readable string.
     *
     * @param response the dependency response
     * @return formatted string representation
     */
    private String formatDependencyResponse(DependencyResponse response) {
        if (response == null || response.getDependencies() == null || response.getDependencies().isEmpty()) {
            return "No dependencies found for repository: " + (response != null ? response.getRepositoryName() : "unknown");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Dependencies\n\n");
        sb.append("- **Repository**: ").append(response.getRepositoryName()).append("\n");
        sb.append("- **Total Results**: ").append(response.getTotalResults() != null ? response.getTotalResults() : 0).append("\n\n");

        List<DependencyInfo> dependencies = response.getDependencies();

        sb.append("### Dependency List\n\n");
        sb.append("| # | Name | Group ID | Artifact ID | Version | Scope | Type |\n");
        sb.append("|---|------|----------|-------------|---------|-------|------|\n");

        for (int i = 0; i < dependencies.size(); i++) {
            DependencyInfo dep = dependencies.get(i);
            sb.append("| ").append(i + 1)
                    .append(" | ").append(formatValue(dep.getName()))
                    .append(" | ").append(formatValue(dep.getGroupId()))
                    .append(" | ").append(formatValue(dep.getArtifactId()))
                    .append(" | ").append(formatValue(dep.getVersion()))
                    .append(" | ").append(formatValue(dep.getScope()))
                    .append(" | ").append(formatValue(dep.getType() != null ? dep.getType().toString() : null))
                    .append(" |\n");
        }

        sb.append("\n");

        return sb.toString();
    }

    /**
     * Formats a value for display, returning "N/A" for null or empty values.
     */
    private String formatValue(String value) {
        return (value != null && !value.isEmpty()) ? value : "N/A";
    }
}