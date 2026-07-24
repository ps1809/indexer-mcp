package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.RelatedFileRequest;
import com.projectiq.mcp.client.dto.RelatedFileResponse;
import com.projectiq.mcp.client.dto.RelatedFile;
import com.projectiq.mcp.client.dto.RelatedFileRequest.SearchTargetType;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * MCP Tool for listing related files in ProjectIQ Indexer.
 * Allows AI coding agents to quickly identify source files associated
 * with a given class, method, REST endpoint or Spring component.
 */
@Component
public class ListRelatedFilesTool {

    private static final Logger logger = LoggerFactory.getLogger(ListRelatedFilesTool.class);

    /**
     * Supported search target types for related file discovery.
     */
    public static final List<String> SUPPORTED_TARGET_TYPES = Arrays.asList(
            "CLASS",
            "METHOD",
            "REST_API",
            "SPRING_COMPONENT",
            "PACKAGE"
    );

    private final IndexerRestClient indexerRestClient;

    public ListRelatedFilesTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Finds related files in ProjectIQ Indexer.
     *
     * @param repositoryName the name of the repository to search in (required)
     * @param searchTarget the target identifier (class name, method name, REST endpoint path, Spring bean name, or package name) (required)
     * @param targetType the type of search target (CLASS, METHOD, REST_API, SPRING_COMPONENT, PACKAGE) (required)
     * @param branch the branch name (optional, defaults to main if not specified)
     * @return formatted string containing related file metadata results
     */
    @Tool(description = """
            Find related files for a given target in ProjectIQ Indexer.
            
            Returns file information including:
            - File name
            - File path
            - File type (e.g., JAVA, XML, PROPERTIES)
            - Relationship type (e.g., IMPLEMENTATION, DEPENDENCY, CONFIGURATION)
            - Associated package
            
            Supported target types: CLASS, METHOD, REST_API, SPRING_COMPONENT, PACKAGE
            """)
    public String listRelatedFiles(
            String repositoryName,
            String searchTarget,
            String targetType,
            String branch) {

        try {
            if (repositoryName == null || repositoryName.isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "repositoryName is required");
            }

            if (searchTarget == null || searchTarget.isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "searchTarget is required");
            }

            if (targetType == null || targetType.isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "targetType is required");
            }

            SearchTargetType convertedType;
            try {
                convertedType = SearchTargetType.valueOf(targetType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return createErrorResponse("INVALID_ARGUMENT", 
                        "Invalid targetType: " + targetType + ". Supported types: " + String.join(", ", SUPPORTED_TARGET_TYPES));
            }

            RelatedFileRequest request = new RelatedFileRequest();
            request.setRepositoryName(repositoryName);
            request.setSearchTarget(searchTarget);
            request.setTargetType(convertedType);
            request.setBranch(branch);

            logger.info("Executing list_related_files tool for repository: {}, target: {}, type: {}", 
                    repositoryName, searchTarget, targetType);

            RelatedFileResponse response = indexerRestClient.findRelatedFiles(request);

            return formatResponse(response);

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

    private String formatResponse(RelatedFileResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("Related Files Results\n");
        sb.append("====================\n");
        sb.append("Repository: ").append(response.getRepositoryName()).append("\n");
        sb.append("Target: ").append(response.getSearchTarget()).append("\n");
        sb.append("Target Type: ").append(response.getTargetType() != null ? response.getTargetType().name() : "N/A").append("\n");
        sb.append("Total Results: ").append(response.getTotalResults()).append("\n\n");

        if (response.getRelatedFiles() != null && !response.getRelatedFiles().isEmpty()) {
            for (int i = 0; i < response.getRelatedFiles().size(); i++) {
                RelatedFile file = response.getRelatedFiles().get(i);
                sb.append("[").append(i + 1).append("] File: ").append(file.getFileName()).append("\n");
                sb.append("    Path: ").append(file.getFilePath()).append("\n");
                sb.append("    Type: ").append(file.getFileType() != null ? file.getFileType() : "N/A").append("\n");
                sb.append("    Relationship: ").append(file.getRelationshipType() != null ? file.getRelationshipType() : "N/A").append("\n");
                sb.append("    Package: ").append(file.getAssociatedPackage() != null ? file.getAssociatedPackage() : "N/A").append("\n");
                sb.append("\n");
            }
        } else {
            sb.append("No related files found.\n");
        }

        return sb.toString();
    }

    private String createErrorResponse(String errorType, String message) {
        return "Error [" + errorType + "]: " + message;
    }
}