package com.projectiq.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.projectiq.mcp.client.IndexerRestClient;
import com.projectiq.mcp.client.dto.MethodInfo;
import com.projectiq.mcp.client.dto.MethodParameter;
import com.projectiq.mcp.client.dto.MethodRequest;
import com.projectiq.mcp.client.dto.MethodResponse;
import com.projectiq.mcp.client.exception.IndexerClientException;
import com.projectiq.mcp.client.exception.IndexerConnectionException;
import com.projectiq.mcp.client.exception.IndexerHttpException;
import com.projectiq.mcp.client.exception.IndexerTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MCP tool for finding Java methods in ProjectIQ Indexer.
 * Exposes method metadata discovery capabilities through MCP.
 */
@Component
public class FindMethodTool {

    private static final Logger logger = LoggerFactory.getLogger(FindMethodTool.class);

    public static final String TOOL_NAME = "find_method";
    public static final String TOOL_DESCRIPTION = """
        Finds Java methods in a ProjectIQ Indexer repository. \
        Returns method metadata including name, declaring class, package, return type, parameters, \
        visibility, static/abstract flags, annotations, and source file location.
        """;

    public static final String TOOL_INPUT_SCHEMA = """
        {
          "type": "object",
          "properties": {
            "repositoryName": {
              "type": "string",
              "description": "Name of the repository to search in"
            },
            "methodName": {
              "type": "string",
              "description": "Method name or partial method name to search for"
            },
            "packageName": {
              "type": "string",
              "description": "Package name to filter results (optional)"
            },
            "branch": {
              "type": "string",
              "description": "Branch name to search in (optional)"
            }
          },
          "required": ["repositoryName"],
          "additionalProperties": false
        }
        """;

    private final IndexerRestClient indexerRestClient;
    private final ObjectMapper objectMapper;

    public FindMethodTool(IndexerRestClient indexerRestClient) {
        this.indexerRestClient = indexerRestClient;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Executes the find method tool with the given criteria.
     *
     * @param repositoryName the repository name (required)
     * @param methodName the method name or partial name to search for
     * @param packageName the package name filter (optional)
     * @param branch the branch name filter (optional)
     * @return JSON string with method metadata or error message
     */
    public String findMethod(String repositoryName, String methodName, String packageName, String branch) {
        // Validate required parameters
        if (repositoryName == null || repositoryName.isEmpty()) {
            try {
                return objectMapper.writeValueAsString(createErrorResponse("INVALID_ARGUMENT",
                        "repositoryName is required"));
            } catch (Exception e) {
                return "{\"error\":{\"code\":\"SERIALIZATION_ERROR\",\"message\":\"Failed to serialize error response\"}}";
            }
        }

        try {
            MethodRequest request = new MethodRequest(repositoryName, methodName);
            request.setPackageName(packageName);
            request.setBranch(branch);

            logger.debug("Searching methods: repository={}, method={}, package={}, branch={}",
                    repositoryName, methodName, packageName, branch);

            MethodResponse response = indexerRestClient.findMethod(request);

            if (response == null) {
                return objectMapper.writeValueAsString(createErrorResponse("INDEXER_ERROR",
                        "Received null response from Indexer"));
            }

            if (response.getTotalResults() == null || response.getTotalResults() == 0) {
                return objectMapper.writeValueAsString(createEmptyResponse(repositoryName));
            }

            List<MethodInfo> methods = response.getMethods();
            if (methods == null || methods.isEmpty()) {
                return objectMapper.writeValueAsString(createEmptyResponse(repositoryName));
            }

            MethodSearchResult result = new MethodSearchResult(
                    repositoryName,
                    response.getTotalResults(),
                    convertMethodsToSummary(methods)
            );

            return objectMapper.writeValueAsString(result);

        } catch (IndexerConnectionException e) {
            logger.error("Indexer connection failure while finding method: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(createErrorResponse("INDEXER_UNREACHABLE",
                        "Cannot connect to ProjectIQ Indexer: " + e.getMessage()));
            } catch (Exception jacksonEx) {
                return "{\"error\":{\"code\":\"INDEXER_UNREACHABLE\",\"message\":\"Cannot connect to ProjectIQ Indexer\"}}";
            }
        } catch (IndexerTimeoutException e) {
            logger.error("Indexer timeout while finding method: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(createErrorResponse("INDEXER_TIMEOUT",
                        "Indexer request timed out: " + e.getMessage()));
            } catch (Exception jacksonEx) {
                return "{\"error\":{\"code\":\"INDEXER_TIMEOUT\",\"message\":\"Indexer request timed out\"}}";
            }
        } catch (IndexerHttpException e) {
            logger.error("Indexer HTTP error while finding method: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(createErrorResponse("INDEXER_HTTP_ERROR",
                        "Indexer HTTP error: " + e.getMessage()));
            } catch (Exception jacksonEx) {
                return "{\"error\":{\"code\":\"INDEXER_HTTP_ERROR\",\"message\":\"Indexer HTTP error\"}}";
            }
        } catch (IndexerClientException e) {
            logger.error("Indexer client error while finding method: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(createErrorResponse("INDEXER_ERROR",
                        "Indexer client error: " + e.getMessage()));
            } catch (Exception jacksonEx) {
                return "{\"error\":{\"code\":\"INDEXER_ERROR\",\"message\":\"Indexer client error\"}}";
            }
        } catch (Exception e) {
            logger.error("Unexpected error while finding method: {}", e.getMessage());
            try {
                return objectMapper.writeValueAsString(createErrorResponse("INTERNAL_ERROR",
                        "Internal error: " + e.getMessage()));
            } catch (Exception jacksonEx) {
                return "{\"error\":{\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal error\"}}";
            }
        }
    }

    private List<MethodResultItem> convertMethodsToSummary(List<MethodInfo> methods) {
        return methods.stream()
                .map(m -> {
                    MethodResultItem item = new MethodResultItem();
                    item.setMethodName(m.getMethodName());
                    item.setFullyQualifiedName(m.getFullyQualifiedName());
                    item.setDeclaringClass(m.getDeclaringClass());
                    item.setPackage_name(m.getPackageName());
                    item.setReturn_type(m.getReturnType());

                    if (m.getParameters() != null && !m.getParameters().isEmpty()) {
                        String params = m.getParameters().stream()
                                .map(p -> p.getType() + " " + p.getName())
                                .collect(Collectors.joining(", "));
                        item.setParameters(params);
                    } else {
                        item.setParameters("none");
                    }

                    item.setVisibility(m.getVisibility());
                    item.setStatic(Boolean.TRUE.equals(m.isStatic()) ? "true" : "false");
                    item.setAbstractFlag(Boolean.TRUE.equals(m.isAbstract()) ? "true" : "false");

                    if (m.getAnnotations() != null && !m.getAnnotations().isEmpty()) {
                        item.setAnnotations(String.join(", ", m.getAnnotations()));
                    } else {
                        item.setAnnotations("none");
                    }

                    item.setSource_location(m.getSourceFileLocation());
                    return item;
                })
                .collect(Collectors.toList());
    }

    private ErrorResponse createErrorResponse(String code, String message) {
        ErrorResponse error = new ErrorResponse();
        error.setCode(code);
        error.setMessage(message);
        return error;
    }

    private EmptyMethodSearchResult createEmptyResponse(String repositoryName) {
        EmptyMethodSearchResult result = new EmptyMethodSearchResult();
        result.setRepositoryName(repositoryName);
        result.setTotalResults(0);
        return result;
    }

    // === DTO Classes ===

    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse() {
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class MethodSearchResult {
        private String repositoryName;
        private Integer totalResults;
        private List<MethodResultItem> methods;

        public MethodSearchResult() {
        }

        public MethodSearchResult(String repositoryName, Integer totalResults, List<MethodResultItem> methods) {
            this.repositoryName = repositoryName;
            this.totalResults = totalResults;
            this.methods = methods;
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

        public Integer getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(Integer totalResults) {
            this.totalResults = totalResults;
        }

        public List<MethodResultItem> getMethods() {
            return methods;
        }

        public void setMethods(List<MethodResultItem> methods) {
            this.methods = methods;
        }
    }

    public static class MethodResultItem {
        private String method_name;
        private String fully_qualified_name;
        private String declaring_class;
        private String package_name;
        private String return_type;
        private String parameters;
        private String visibility;
        private String static_flag;
        private String abstract_flag;
        private String annotations;
        private String source_location;

        public MethodResultItem() {
        }

        public String getMethodName() {
            return method_name;
        }

        public void setMethodName(String methodName) {
            this.method_name = methodName;
        }

        public String getFullyQualifiedName() {
            return fully_qualified_name;
        }

        public void setFullyQualifiedName(String fullyQualifiedName) {
            this.fully_qualified_name = fullyQualifiedName;
        }

        public String getDeclaringClass() {
            return declaring_class;
        }

        public void setDeclaringClass(String declaringClass) {
            this.declaring_class = declaringClass;
        }

        public String getPackage_name() {
            return package_name;
        }

        public void setPackage_name(String packageName) {
            this.package_name = packageName;
        }

        public String getReturn_type() {
            return return_type;
        }

        public void setReturn_type(String returnType) {
            this.return_type = returnType;
        }

        public String getParameters() {
            return parameters;
        }

        public void setParameters(String parameters) {
            this.parameters = parameters;
        }

        public String getVisibility() {
            return visibility;
        }

        public void setVisibility(String visibility) {
            this.visibility = visibility;
        }

        public String getStatic() {
            return static_flag;
        }

        public void setStatic(String staticFlag) {
            this.static_flag = staticFlag;
        }

        public String getAbstractFlag() {
            return abstract_flag;
        }

        public void setAbstractFlag(String abstractFlag) {
            this.abstract_flag = abstractFlag;
        }

        public String getAnnotations() {
            return annotations;
        }

        public void setAnnotations(String annotations) {
            this.annotations = annotations;
        }

        public String getSource_location() {
            return source_location;
        }

        public void setSource_location(String sourceLocation) {
            this.source_location = sourceLocation;
        }
    }

    public static class EmptyMethodSearchResult {
        private String repositoryName;
        private Integer totalResults;

        public EmptyMethodSearchResult() {
        }

        public String getRepositoryName() {
            return repositoryName;
        }

        public void setRepositoryName(String repositoryName) {
            this.repositoryName = repositoryName;
        }

        public Integer getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(Integer totalResults) {
            this.totalResults = totalResults;
        }
    }
}