package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.SpringComponentInfo;
import com.projectiq.mcp.client.dto.SpringComponentRequest;
import com.projectiq.mcp.client.dto.SpringComponentResponse;
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
 * MCP Tool for finding Spring components in ProjectIQ Indexer.
 * Allows AI coding agents to discover Controllers, Services, Repositories,
 * Components, Configurations and other Spring-managed beans.
 */
@Component
public class FindSpringComponentTool {

    private static final Logger logger = LoggerFactory.getLogger(FindSpringComponentTool.class);

    /**
     * Supported Spring component types.
     */
    public static final List<String> SUPPORTED_COMPONENT_TYPES = Arrays.asList(
            "Controller",
            "RestController",
            "Service",
            "Repository",
            "Component",
            "Configuration"
    );

    private final IndexerRestClient indexerRestClient;

    public FindSpringComponentTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Finds Spring components in the ProjectIQ Indexer.
     *
     * @param repositoryName the name of the repository to search in
     * @param componentTypes the types of Spring components to find (e.g., Controller, Service, Repository)
     * @param branch the branch name (optional, defaults to main if not specified)
     * @param packageName filter results by package name (optional)
     * @return a formatted string containing the matching Spring components
     */
    @Tool(description = "Find Spring components (Controllers, Services, Repositories, Components, Configurations) in ProjectIQ Indexer. " +
            "Returns matching Spring-managed beans with their locations and metadata.")
    public String findSpringComponent(
            @ToolParam(description = "The name of the repository to search in") String repositoryName,
            @ToolParam(description = "List of component types to search for (e.g., Controller, Service, Repository, RestController, Component, Configuration). Comma-separated if multiple types.") String componentTypes,
            @ToolParam(description = "The branch name to search in (optional)", required = false) String branch,
            @ToolParam(description = "Filter results by package name (optional)", required = false) String packageName) {

        logger.debug("Find Spring component tool invoked for repository: {}, types: {}", repositoryName, componentTypes);

        try {
            List<String> componentTypeList = parseComponentTypes(componentTypes);
            
            SpringComponentRequest request = new SpringComponentRequest(repositoryName, componentTypeList);
            request.setBranch(branch);
            request.setPackageName(packageName);

            SpringComponentResponse response = indexerRestClient.findSpringComponent(request);

            return formatResponse(response);
        } catch (IndexerTimeoutException e) {
            String message = "Unable to find Spring components: Connection to ProjectIQ Indexer timed out. " +
                    "Please ensure the Indexer service is running and accessible.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerConnectionException e) {
            String message = "Unable to find Spring components: Cannot connect to ProjectIQ Indexer. " +
                    "Please ensure the Indexer service is running and the base URL is correctly configured.";
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (IndexerClientException e) {
            String message = "Unable to find Spring components: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        } catch (Exception e) {
            String message = "An unexpected error occurred while finding Spring components: " + e.getMessage();
            logger.error(message, e);
            return "ERROR: " + message;
        }
    }

    /**
     * Parses the comma-separated component types string into a list.
     *
     * @param componentTypes comma-separated component types
     * @return list of component types
     */
    private List<String> parseComponentTypes(String componentTypes) {
        if (componentTypes == null || componentTypes.trim().isEmpty()) {
            return SUPPORTED_COMPONENT_TYPES;
        }
        return Arrays.stream(componentTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Formats the Spring component response into a readable string.
     *
     * @param response the Spring component response
     * @return formatted string representation
     */
    private String formatResponse(SpringComponentResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("## Spring Components\n\n");
        sb.append("- **Repository**: ").append(response.getRepositoryName()).append("\n");
        sb.append("- **Total Results**: ").append(response.getTotalResults() != null ? response.getTotalResults() : 0).append("\n");

        List<SpringComponentInfo> components = response.getComponents();
        if (components != null && !components.isEmpty()) {
            // Group by component type
            sb.append("\n### Components by Type\n\n");
            
            String currentType = null;
            for (SpringComponentInfo component : components) {
                String type = component.getComponentType() != null ? component.getComponentType() : "Unknown";
                
                if (currentType == null || !currentType.equals(type)) {
                    sb.append("#### ").append(type).append("\n\n");
                    currentType = type;
                }
                
                sb.append("##### `").append(component.getName()).append("`\\n");
                
                if (component.getClassName() != null) {
                    sb.append("- **Class**: `").append(component.getClassName()).append("`\n");
                }
                
                if (component.getPackageName() != null) {
                    sb.append("- **Package**: `").append(component.getPackageName()).append("`\n");
                }
                
                if (component.getFilePath() != null) {
                    sb.append("- **File**: `").append(component.getFilePath());
                    if (component.getLineNumber() != null) {
                        sb.append(":").append(component.getLineNumber());
                    }
                    sb.append("`\n");
                }

                if (component.getDescription() != null) {
                    sb.append("- **Description**: ").append(component.getDescription()).append("\n");
                }
                
                sb.append("\n");
            }
        } else {
            sb.append("\nNo Spring components found.\n");
        }

        return sb.toString();
    }
}