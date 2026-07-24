package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RestApiResponse;
import com.projectiq.mcp.client.dto.RestApiRequest;
import com.projectiq.mcp.client.dto.RestEndpointInfo;
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
 * MCP Tool for finding REST API endpoints in ProjectIQ Indexer.
 * Allows AI coding agents to discover REST endpoints, controllers,
 * HTTP methods, request mappings and endpoint metadata.
 */
@Component
public class FindRestApiTool {

    private static final Logger logger = LoggerFactory.getLogger(FindRestApiTool.class);

    /**
     * Supported HTTP methods.
     */
    public static final List<String> SUPPORTED_HTTP_METHODS = Arrays.asList(
            "GET",
            "POST",
            "PUT",
            "PATCH",
            "DELETE"
    );

    private final IndexerRestClient indexerRestClient;

    public FindRestApiTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Finds REST API endpoints in the ProjectIQ Indexer.
     *
     * @param repositoryName the name of the repository to search in
     * @param httpMethods the HTTP methods to filter by (e.g., GET, POST, PUT)
     * @param controllerFilter filter results by controller name (optional)
     * @param branch the branch name (optional, defaults to main if not specified)
     * @param packageName filter results by package name (optional)
     * @return a formatted string containing the matching REST endpoints
     */
    @Tool(description = "Find REST API endpoints in ProjectIQ Indexer. " +
            "Returns matching REST endpoints with their HTTP methods, controller names, " +
            "method names, request mappings, and response types.")
    public String findRestApi(
            @ToolParam(description = "The name of the repository to search in") String repositoryName,
            @ToolParam(description = "List of HTTP methods to filter by (e.g., GET, POST, PUT, PATCH, DELETE). Comma-separated if multiple methods.") String httpMethods,
            @ToolParam(description = "Filter results by controller name (optional)", required = false) String controllerFilter,
            @ToolParam(description = "The branch name to search in (optional)", required = false) String branch,
            @ToolParam(description = "Filter results by package name (optional)", required = false) String packageName) {

        logger.debug("Find REST API tool invoked for repository: {}, methods: {}", repositoryName, httpMethods);

        try {
            List<String> methodList = parseHttpMethods(httpMethods);

            RestApiRequest request = new RestApiRequest(repositoryName, methodList);
            request.setBranch(branch);
            request.setPackageName(packageName);

            RestApiResponse response = indexerRestClient.findRestApi(request);

            return formatResponse(response, controllerFilter);
        } catch (IndexerTimeoutException e) {
            String message = "Unable to find REST API endpoints: Connection to ProjectIQ Indexer timed out. " +
                    "Please ensure the Indexer service is running and accessible.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerConnectionException e) {
            String message = "Unable to find REST API endpoints: Cannot connect to ProjectIQ Indexer. " +
                    "Please ensure the Indexer service is running and the base URL is correctly configured.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerClientException e) {
            String message = "Unable to find REST API endpoints: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (Exception e) {
            String message = "An unexpected error occurred while finding REST API endpoints: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        }
    }

    /**
     * Parses the comma-separated HTTP methods string into a list.
     *
     * @param httpMethods comma-separated HTTP methods
     * @return list of HTTP methods
     */
    private List<String> parseHttpMethods(String httpMethods) {
        if (httpMethods == null || httpMethods.trim().isEmpty()) {
            return SUPPORTED_HTTP_METHODS;
        }
        return Arrays.stream(httpMethods.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Formats the REST API response into a readable string.
     *
     * @param response the REST API response
     * @param controllerFilter optional controller filter for display
     * @return formatted string representation
     */
    private String formatResponse(RestApiResponse response, String controllerFilter) {
        StringBuilder sb = new StringBuilder();
        sb.append("## REST API Endpoints\n\n");
        sb.append("- **Repository**: ").append(response.getRepositoryName()).append("\n");
        sb.append("- **Total Results**: ").append(response.getTotalResults() != null ? response.getTotalResults() : 0).append("\n");

        List<RestEndpointInfo> endpoints = response.getEndpoints();
        if (endpoints != null && !endpoints.isEmpty()) {
            // Filter by controller if specified
            if (controllerFilter != null && !controllerFilter.isEmpty()) {
                endpoints = endpoints.stream()
                        .filter(e -> e.getControllerName() != null && 
                                e.getControllerName().equalsIgnoreCase(controllerFilter))
                        .toList();
                
                if (endpoints.isEmpty()) {
                    sb.append("\nNo endpoints found for controller: ").append(controllerFilter).append("\n");
                    return sb.toString();
                }
            }

            sb.append("\n### Endpoints\n\n");

            for (RestEndpointInfo endpoint : endpoints) {
                // HTTP method badge
                String method = endpoint.getHttpMethod() != null ? endpoint.getHttpMethod() : "UNKNOWN";
                sb.append("#### `").append(method).append(" ` ").append(endpoint.getEndpointPath()).append("\n\n");

                if (endpoint.getControllerName() != null) {
                    sb.append("- **Controller**: `").append(endpoint.getControllerName()).append("`\\n");
                }

                if (endpoint.getMethodName() != null) {
                    sb.append("- **Method**: `").append(endpoint.getMethodName()).append("`\\n");
                }

                if (endpoint.getRequestMapping() != null) {
                    sb.append("- **Request Mapping**: `").append(endpoint.getRequestMapping()).append("`\n");
                }

                if (endpoint.getResponse_type() != null) {
                    sb.append("- **Response Type**: `").append(endpoint.getResponse_type()).append("`\n");
                }

                if (endpoint.getPackageName() != null) {
                    sb.append("- **Package**: `").append(endpoint.getPackageName()).append("`\n");
                }

                if (endpoint.getFilePath() != null) {
                    sb.append("- **File**: `").append(endpoint.getFilePath());
                    if (endpoint.getLineNumber() != null) {
                        sb.append(":").append(endpoint.getLineNumber());
                    }
                    sb.append("`\n");
                }

                sb.append("\n");
            }
        } else {
            sb.append("\nNo REST API endpoints found.\n");
        }

        return sb.toString();
    }
}