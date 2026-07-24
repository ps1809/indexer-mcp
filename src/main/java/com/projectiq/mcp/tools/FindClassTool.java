package com.projectiq.mcp.tools;

import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.ClassRequest;
import com.projectiq.mcp.client.dto.ClassResponse;
import com.projectiq.mcp.client.dto.ClassInfo;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP Tool for finding and discovering Java classes in ProjectIQ Indexer.
 * Allows AI coding agents to quickly locate and inspect Java classes
 * without scanning the repository.
 */
@Component
public class FindClassTool {

    private static final Logger logger = LoggerFactory.getLogger(FindClassTool.class);

    /**
     * Supported class types.
     */
    public static final List<String> SUPPORTED_CLASS_TYPES = Arrays.asList(
            "CLASS",
            "INTERFACE",
            "ENUM",
            "RECORD",
            "ANNOTATION"
    );

    private final IndexerRestClient indexerRestClient;

    public FindClassTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
    }

    /**
     * Searches for Java classes in ProjectIQ Indexer.
     *
     * @param repositoryName the name of the repository to search in (required)
     * @param className the class name or partial name to search for (optional)
     * @param packageName the package name to filter by (optional)
     * @param classTypes the class types to filter by (CLASS, INTERFACE, ENUM, RECORD, ANNOTATION)
     * @param branch the branch name (optional, defaults to main if not specified)
     * @return formatted string containing class metadata results
     */
    @Tool(description = """
            Search for Java classes in ProjectIQ Indexer.
            
            Returns class metadata including:
            - Package name
            - Class name
            - Fully qualified class name
            - Class type (CLASS, INTERFACE, ENUM, RECORD, ANNOTATION)
            - Visibility (public, private, protected, etc.)
            - Parent class
            - Implemented interfaces
            - Annotations
            - Source file location
            
            Supported class types: CLASS, INTERFACE, ENUM, RECORD, ANNOTATION
            """)
    public String findClass(
            String repositoryName,
            String className,
            String packageName,
            List<String> classTypes,
            String branch) {

        try {
            if (repositoryName == null || repositoryName.isEmpty()) {
                return createErrorResponse("INVALID_ARGUMENT", "repositoryName is required");
            }

            ClassRequest request = new ClassRequest();
            request.setRepositoryName(repositoryName);
            request.setClassName(className);
            request.setPackageName(packageName);
            request.setBranch(branch);
            request.setClassTypes(classTypes);

            logger.info("Executing find_class tool for repository: {}, class: {}", 
                    repositoryName, className);

            ClassResponse response = indexerRestClient.findClass(request);

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

    private String formatResponse(ClassResponse response) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class Search Results\n");
        sb.append("====================\n");
        sb.append("Repository: ").append(response.getRepositoryName()).append("\n");
        sb.append("Total Results: ").append(response.getTotalResults()).append("\n\n");

        if (response.getClasses() != null && !response.getClasses().isEmpty()) {
            for (ClassInfo classInfo : response.getClasses()) {
                sb.append("Class: ").append(classInfo.getClassName()).append("\n");
                sb.append("  Package: ").append(classInfo.getPackageName()).append("\n");
                sb.append("  Fully Qualified Name: ").append(classInfo.getFullyQualifiedName()).append("\n");
                sb.append("  Type: ").append(classInfo.getClassType() != null ? classInfo.getClassType().name() : "N/A").append("\n");
                sb.append("  Visibility: ").append(classInfo.getVisibility()).append("\n");
                sb.append("  Parent Class: ").append(classInfo.getParentClass() != null ? classInfo.getParentClass() : "N/A").append("\n");
                
                if (classInfo.getImplementedInterfaces() != null && !classInfo.getImplementedInterfaces().isEmpty()) {
                    sb.append("  Interfaces: ");
                    sb.append(String.join(", ", classInfo.getImplementedInterfaces()));
                    sb.append("\n");
                }
                
                if (classInfo.getAnnotations() != null && !classInfo.getAnnotations().isEmpty()) {
                    sb.append("  Annotations: ");
                    sb.append(String.join(", ", classInfo.getAnnotations()));
                    sb.append("\n");
                }
                
                sb.append("  Source Location: ").append(classInfo.getSourceFileLocation()).append("\n");
                sb.append("\n");
            }
        } else {
            sb.append("No classes found.\n");
        }

        return sb.toString();
    }

    private String createErrorResponse(String errorType, String message) {
        return "Error [" + errorType + "]: " + message;
    }
}